package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.ServiceStatus;
import org.apache.camel.StatefulService;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.processor.aggregate.AggregateProcessor;
import org.apache.camel.support.ExchangeHelper;

/**
* Class to aggregate the incoming files by satellite and time. Also handles times when incomplete list of files arrives.
*
* <pre>
*
* SOFTWARE HISTORY
*
* Date          Ticket#   Engineer  Description
* ------------- --------  --------- -----------------
* Oct 20, 2018  DCS-18691 jburks    Initial creation
* Mar  3, 2021  8326      tgurney   Camel 3 fixes
*
* </pre>
*
* @author jburks
*/

/**
 * The Class TimeBasedAggregationStrategy collects incoming NUCAPS sounding
 * files and sorts them by satellite and time. Since NUCAPS soundings are
 * produced in chunks while the satellite is ascending or descending parts need
 * to be collected together that comprise an area. Then when the aggregation has
 * several rows, to pushes those to the next processing step by emitting a
 * message. The aggregation is done by time, and satellite. In order to keep the
 * processing down we provide the files in batches around 5 files at a time. The
 * aggregation strategy also has a timer that periodically checks aggregated
 * files in temporary storage lists. If they have been in storage longer than a
 * threshold amount of milliseconds they are processed. This is likely due to
 * additional files in the same aggregation being missing. For a particular
 * satellite the aggregation should just be active during the couple times a day
 * when data is downlinked. As more satellites become active the down-time for
 * the aggregation should decrease.
 */
public class TimeBasedAggregationStrategy
        implements AggregationStrategy, StagedListener, StatefulService {

    /**
     * The camel context used to look up processor for pushing finished
     * aggregation.
     */
    private CamelContext camelContext;

    /** The aggregate processor used to push aggregated list of files. */
    private AggregateProcessor _aggregateProcessor;

    /** The aggregate processor id. */
    private String aggregateProcessorId;

    /** The last file max age. */
    private long lastFileMaxAge = 300000;

    /** The milliseconds in batch in total. */
    private long millisInBatch = 160000;

    /** The files in batch. */
    private int filesInBatch = 5;

    /** The aggregate exchange to push outbound messages. */
    public Exchange aggregateExchange;

    /** The timed checker that makes sure files are unendlessly piling up. */
    TimedChecker checker;

    /** The holders holding aggregated files. */
    List<FileHolder> holders = new ArrayList<>();

    ServiceStatus serviceStatus;

    /*
     * Messages arrive to this method from the pipeline
     *
     * @see
     * org.apache.camel.processor.aggregate.AggregationStrategy#aggregate(org.
     * apache.camel.Exchange, org.apache.camel.Exchange)
     */
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        aggregateExchange.getIn().getBody(String.class);
        appendMessage(aggregateExchange, newExchange.getIn());
        return aggregateExchange;
    }

    /**
     * Append message to the appropriate storage location.
     *
     * @param aggregateExchange
     *            the aggregate exchange
     * @param message
     *            the message
     */
    protected void appendMessage(Exchange aggregateExchange, Message message) {

        if (!checker.isRunning()) {
            checker.start();
        }
        String satelliteId = (String) message.getHeader("satelliteId");
        String filePath = (String) message.getHeader("filePath");
        Date maxTime = new Date((Long) message.getHeader("maxTime"));
        Date minTime = new Date((Long) message.getHeader("minTime"));
        Date ingestTime = new Date((Long) message.getHeader("ingestTime"));
        FileToProcess fileToProcess = new FileToProcess(filePath, minTime,
                maxTime, ingestTime, satelliteId);

        FileHolder holder = findHolder(fileToProcess);
        if (holder == null) {
            FileHolder newHolder = new FileHolder(fileToProcess, millisInBatch);
            holders.add(newHolder);
        } else {
            holder.add(fileToProcess);
            checkHolder(holder);
        }

    }

    /**
     * Check holder the to see if it is full.
     *
     * @param holder
     *            the holder
     */
    private void checkHolder(FileHolder holder) {
        int size = holder.getSize();
        if (size >= filesInBatch) {
            holders.remove(holder);
            processFileBatch(holder);
        }

    }

    /**
     * Process file batch if it is either full or passed the time it should have
     * gotten a file.
     *
     * @param holder
     *            the holder
     */
    private void processFileBatch(FileHolder holder) {

        List<String> filePaths = new ArrayList<>();
        List<Long> ingestTimes = new ArrayList<>();
        for (FileToProcess file : holder.getFileToProcess()) {
            filePaths.add(file.getFilePath());
            ingestTimes.add(file.getIngestTime().getTime());
        }
        String listOfFiles = String.join(",", filePaths);
        if (filePaths.size() > 0) {
            Exchange exchangeToBeEmitted = ExchangeHelper
                    .copyExchangeAndSetCamelContext(aggregateExchange,
                            camelContext);
            exchangeToBeEmitted.getOut().setBody(listOfFiles);
            exchangeToBeEmitted.getOut().setHeader("enqueueTime",
                    holder.getFileToProcess()[0].ingestTime.getTime());
            try {
                for (Processor processor : _aggregateProcessor().next()) {
                    processor.process(exchangeToBeEmitted);
                }
            } catch (Exception e) {
                throw new RuntimeCamelException(e);
            }
        }

    }

    /**
     * Find holder to push the files.
     *
     * @param fileToProcess
     *            the file to process
     * @return the file holder
     */
    private FileHolder findHolder(FileToProcess fileToProcess) {
        for (FileHolder holder : holders) {
            boolean isMatch = holder.checkFileToProcess(fileToProcess);
            if (isMatch) {
                return holder;
            }
        }
        return null;
    }

    /*
     * Timer event callback to have the aggregator check to see if FileHolders
     * have not received files within a time. If they have not then they are
     * forced to be processed.
     *
     * @see gov.noaa.nws.sti.mdl.dsb.container.StagedListener#notifyEvent()
     */
    @Override
    public void notifyEvent() {
        // Check all holders and see if any contain anything that needs to be
        // processed.
        int size = holders.size();
        List<FileHolder> toRemove = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            if (holders.get(i).getLastAddedFile().getTime() >= lastFileMaxAge) {
                toRemove.add(holders.get(i));
            }
        }
        for (FileHolder holderToRemove : toRemove) {
            holders.remove(holderToRemove);
            processFileBatch(holderToRemove);
        }

        if (holders.size() == 0) {
            checker.stop();
        }

    }

    /**
     * Flush all holders.
     */
    private void flushAllHolders() {

        int size = holders.size();
        for (int i = 0; i < size; ++i) {
            FileHolder holderToRemove = holders.remove(i);
            if (holderToRemove.getSize() > 0) {
                processFileBatch(holderToRemove);
            }
        }
    }

    /**
     * Aggregate processor is the processer that helps publish the messages
     * emitted from this aggregation strategy.
     *
     * @return the aggregate processor
     */
    protected AggregateProcessor _aggregateProcessor() {
        if (_aggregateProcessor == null) {
            _aggregateProcessor = camelContext.getProcessor(
                    aggregateProcessorId, AggregateProcessor.class);
        }
        return _aggregateProcessor;
    }

    /**
     * Sets the delay.
     *
     * @param checkInterval
     *            the new check interval
     */
    public void setCheckInterval(long checkInterval) {
        checker = new TimedChecker(this, checkInterval);
    }

    /**
     * Sets the camel context.
     *
     * @param camelContext
     *            the new camel context
     */
    public void setCamelContext(CamelContext camelContext) {

        this.camelContext = camelContext;
        this.aggregateExchange = ExchangeBuilder.anExchange(camelContext)
                .build();
    }

    /**
     * Sets the millis in batch.
     *
     * @param millisInBatch
     *            the new millis in batch
     */
    public void setMillisInBatch(long millisInBatch) {
        this.millisInBatch = millisInBatch;
    }

    /**
     * Sets the files in batch.
     *
     * @param filesInBatch
     *            the new files in batch
     */
    public void setFilesInBatch(int filesInBatch) {
        this.filesInBatch = filesInBatch;
    }

    /**
     * Sets the aggregate processor id.
     *
     * @param aggregateProcessorId
     *            the new aggregate processor id
     */
    public void setAggregateProcessorId(String aggregateProcessorId) {
        this.aggregateProcessorId = aggregateProcessorId;
    }

    /**
     * Sets the last file max age.
     *
     * @param lastFileMaxAge
     *            the new last file max age
     */
    public void setLastFileMaxAge(long lastFileMaxAge) {
        this.lastFileMaxAge = lastFileMaxAge;
    }

    @Override
    public void suspend() {
        serviceStatus = ServiceStatus.Suspended;
    }

    @Override
    public void resume() {
        serviceStatus = ServiceStatus.Started;
    }

    @Override
    public boolean isSuspended() {
        return serviceStatus.equals(ServiceStatus.Suspended);
    }

    @Override
    public void start() {
        serviceStatus = ServiceStatus.Started;
    }

    @Override
    public void stop() {
        serviceStatus = ServiceStatus.Stopped;
    }

    /*
     * If the route is shutting down I need to push all data in the Aggregator
     * out.
     */
    @Override
    public void shutdown() {
        serviceStatus = ServiceStatus.Stopping;
        flushAllHolders();
        serviceStatus = ServiceStatus.Stopped;
    }

    @Override
    public ServiceStatus getStatus() {
        return serviceStatus;
    }

    @Override
    public boolean isStarted() {
        return serviceStatus.equals(ServiceStatus.Started);
    }

    @Override
    public boolean isStarting() {
        return serviceStatus.equals(ServiceStatus.Starting);
    }

    @Override
    public boolean isStopping() {
        return serviceStatus.equals(ServiceStatus.Stopping);
    }

    @Override
    public boolean isStopped() {
        return serviceStatus.equals(ServiceStatus.Stopped);
    }

    @Override
    public boolean isSuspending() {
        return serviceStatus.equals(ServiceStatus.Suspending);
    }

    @Override
    public boolean isRunAllowed() {
        // noop
        return true;
    }

}
