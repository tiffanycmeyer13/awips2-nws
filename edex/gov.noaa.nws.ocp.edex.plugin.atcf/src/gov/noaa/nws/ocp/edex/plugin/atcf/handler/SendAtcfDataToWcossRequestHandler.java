/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.RunProcess;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.SendAtcfDataToWcossRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.AtcfFileUtil;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.ProcessRetrievedFiles;
import jep.JepException;

/**
 * Request handler for SendAtcfDataToWcossRequests.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Aug 12, 2019 66071      mporricelli  Initial creation
 * Nov 20, 2019 70253      mporricelli  Moved creation of .com
 *                                      file to PrepareCompute
 *                                      code
 * Nov 21, 2019 69937      mporricelli  Reworked for sending files
 *                                      to LDM rather than directly
 *                                      to WCOSS
 * Dec 13, 2019 71984      mporricelli  Notify user if storm guidance
 *                                      does not come back from WCOSS
 * Apr 16, 2020 71986      mporricelli  Create CSV deck files, .fst
 *                                      file and storms.txt for
 *                                      sending to external locations
 *                                      (WCOSS, FTP)
 * Jun 03, 2020 78922      mporricelli  Added priority value to runPushScript
 *                                      execution and moved creation of
 *                                      subForecast script to pushToLdm.sh
 * Jun 11, 2020 79366      mporricelli  Modify creation of deck files
 * Aug 10, 2020 78756      mporricelli  Update monitoring of 'retrieved' dir
 * Aug 11, 2020 76541      mporricelli  Get Get directory defs and timeout
 *                                      from properties file
 * Sep 09, 2020 80672      mporricelli  Add call to process retrieved files
 *                                      after their arrival
 * Nov 06, 2020 83637      mporricelli  Make return file name more specific
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
@DynamicSerialize
public class SendAtcfDataToWcossRequestHandler
        implements IRequestHandler<SendAtcfDataToWcossRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SendAtcfDataToWcossRequestHandler.class);

    private static final ExecutorService pool = Executors
            .newSingleThreadExecutor();

    private static final String MSG_SOURCE = "ANNOUNCER";

    private static final String MSG_CATAGORY = "WORKSTATION";

    private static class Entry {
        public Entry(Storm storm, String dtg, Instant expiration) {
            this.expiration = expiration;
            this.storm = storm;
            this.dtg = dtg;
        }

        private Instant expiration;

        private Storm storm;

        private String dtg;
    }

    private static final List<Entry> entries = new ArrayList<>();

    private static String atcfRtrieved;

    private static String pushScript;

    private static long timeout;

    private static Path retrievedPath;

    private static AtcfEnvironmentConfig envConfig;

    private static synchronized void loadConfig() {
        if (envConfig == null) {
            envConfig = AtcfConfigurationManager
                    .getEnvConfig();
            atcfRtrieved = envConfig.getAtcf_retrieved();
            retrievedPath = Paths.get(envConfig.getAtcf_retrieved());
            timeout = Integer.valueOf(envConfig.getWcoss_timeout())
                    * TimeUtil.MILLIS_PER_MINUTE;
            pushScript = AtcfConfigurationManager.getPushScript().toString();
        }
    }

    @Override
    public String handleRequest(SendAtcfDataToWcossRequest request)
            throws Exception {
        loadConfig();

        String atcfStrms = envConfig.getAtcfstrms();

        Storm storm = request.getStorm();
        String dtg = request.getDtg();

        String stormid = storm.getStormId().toLowerCase();

        File comFile = new File(atcfStrms + File.separator + stormid + ".com");

        String scriptOutput = null;

        boolean fileCreated;

        String retval = "Success";

        if (!comFile.exists()) {
            retval = "Compute file " + comFile
                    + " does not exist. Not sending to WCOSS.";
            statusHandler.handle(Priority.SIGNIFICANT, retval);
            sendFailedMsg(stormid, dtg, retval);
            return retval;
        }

        ClusterTask ct = null;
        try {
            do {
                ct = ClusterLockUtils.lock("atcfFileCreateLock",
                        "atcfCreateFiles", 300_000, true);
            } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));

            /*
             * Need to create output files: storms.txt, a-deck,b-deck, e-deck,
             * f-deck, .fst. Get data from storms table, write out storms.txt
             * Loop through all deck types, get data from deck's table, write
             * out deck file
             */

            AtcfProcessDao dao = null;
            List<Storm> stormsRecs = null;
            List<? extends AbstractDeckRecord> deckRecs;

            // Construct output file name
            String stormListFile = atcfStrms + File.separator + "storms.txt";

            try {
                dao = new AtcfProcessDao();
            } catch (Exception e) {
                throw new Exception("AtcfProcessDao object creation failed", e);
            }

            // Establish the query criteria for storms data
            Map<String, Object> queryConditions = request.getQueryConditions();
            request.addOneQueryCondition("region", storm.getRegion());

            // Get storms data from DB and create the storms.txt file
            try {
                stormsRecs = dao.getStormList(queryConditions);
            } catch (DataAccessLayerException e) {
                retval = "Error retrieving storms records. " + e;
                statusHandler.handle(Priority.PROBLEM, retval);
                sendFailedMsg(stormid, dtg, retval);
                return retval;
            }

            // Write storms data to file
            try {
                fileCreated = AtcfFileUtil.createStormListFile(stormsRecs,
                        stormListFile);
            } catch (Exception e) {
                retval = "Exception while trying to create storms.txt file. "
                        + e;
                statusHandler.handle(Priority.SIGNIFICANT, retval);
                sendFailedMsg(stormid, dtg, retval);
                return retval;
            }

            // Check if storms file creation succeeded
            if (!fileCreated) {
                retval = "Failed to create storms.txt-- not sending data to WCOSS";
                statusHandler.handle(Priority.SIGNIFICANT, retval);
                sendFailedMsg(stormid, dtg, retval);
                return retval;
            }

            request.clearQueryConditions();

            // Establish the query criteria for fst and deck data for this storm
            request.addOneQueryCondition("basin", storm.getRegion());
            request.addOneQueryCondition("year", storm.getYear());
            request.addOneQueryCondition("cycloneNum", storm.getCycloneNum());

            /*
             * Get deck data from DB and create the storm's deck files
             * (.dat,.fst)
             */
            for (AtcfDeckType deckType : AtcfDeckType.values()) {
                String deckFile = atcfStrms + File.separator
                        + deckType.getValue().toLowerCase() + stormid + ".dat";
                if (deckType == AtcfDeckType.T) {
                    deckFile = atcfStrms + File.separator + stormid + "."
                            + deckType.getValue().toLowerCase();
                }

                deckRecs = dao.getDeckList(deckType,
                        request.getQueryConditions(), -1);

                // Write deck data to file
                try {
                    fileCreated = AtcfFileUtil.createDeckFile(deckRecs, deckType,
                            deckFile);
                } catch (Exception e) {
                    retval = "Exception while trying to create " + deckType
                            + "-deck file for " + stormid
                            + " -- not sending data to WCOSS. " + e;
                    statusHandler.handle(Priority.SIGNIFICANT, retval);
                    sendFailedMsg(stormid, dtg, retval);
                    return retval;
                }

                // Check if deck file creation succeeded
                if (!fileCreated) {
                    retval = "Failed to create " + deckType + "-deck file for "
                            + stormid + " -- not sending data to WCOSS";
                    statusHandler.handle(Priority.SIGNIFICANT, retval);
                    sendFailedMsg(stormid, dtg, retval);
                    return retval;
                }
            }

            // Run external script to push data to LDM

            try {
                scriptOutput = runPushScript(stormid);
            } catch (IOException e) {
                statusHandler.handle(Priority.CRITICAL,
                        "ATCF: Problem executing " + pushScript + " - "
                                + e.getMessage(), e);
                retval = "Problem executing " + pushScript + " - "
                        + e.getMessage();
            }

        } finally {
            ClusterLockUtils.unlock(ct, false);
        }

        /*
         * Check output from script. If not successful return error, else start
         * monitoring retrieval directory for return from wcoss
         */
        if (!"Finished".equals(scriptOutput)) {
            statusHandler.handle(Priority.CRITICAL,
                    "ATCF: Error running " + pushScript + ": " + scriptOutput);
            retval = "Error running " + pushScript + ": " + scriptOutput;
            sendFailedMsg(stormid, dtg, retval);

        } else {
            EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                    "SendAtcfDataToWcossRequestHandler", MSG_SOURCE,
                    MSG_CATAGORY,
                    "ATCF Storm " + stormid
                            + "  --> GUIDANCE SUBMITTED for Date-Time: " + dtg
                            + ". Should be ready by "
                            + getDateTime("HH:mm:ss", timeout) + " UTC",
                    null, null);

            // Kick off retrieval monitoring
            Instant targetTime = Instant.now().plusMillis(timeout);
            Entry entry = new Entry(storm, dtg, targetTime);

            synchronized (entries) {
                entries.add(entry);
                CheckForWCOSSRetrieval process = new CheckForWCOSSRetrieval();

                try {
                    pool.execute(process);
                } catch (RejectedExecutionException e) {
                    if (pool.isShutdown() || pool.isTerminated()) {
                        retval = "ExecutorService has been shutdown";
                        statusHandler.handle(Priority.CRITICAL, retval, e);
                        return retval;
                    }
                }
            }

        }
        return retval;

    }

    /**
     * Construct and execute command to run pushToLdm.sh. pushToLdm.sh sends the
     * storm files to the LDAD Server and then to the NCEP LDM server for
     * transfer to WCOSS and/or FTP site
     *
     * @param stormid
     * @return output from script
     * @throws IOException
     */
    private String runPushScript(String stormid) throws IOException {
        /*
         * Send with priority value of 0, i.e. no priority set, to distinguish
         * from that sent by NWP Model Priority functionality
         */

        String[] runCmd = new String[] { pushScript, stormid, "0" };

        RunProcess runPushScriptCmd = null;

        try {
            runPushScriptCmd = RunProcess.getRunProcess().exec(runCmd);
        } catch (IOException e) {
            throw new IOException(
                    "Problem executing " + pushScript + " - " + e.getMessage(),
                    e);
        }
        return runPushScriptCmd.getStderr().trim();
    }

    /**
     * Monitor 'retrieved' directory for storm files returned from WCOSS. If no
     * arrival before timeout, post alertviz warning indicating no file If file
     * arrives, move file to a temporary subdirectory of the 'retrieved'
     * directory and post alertviz message that there was a receipt/retrieval
     *
     */

    private class CheckForWCOSSRetrieval implements Runnable {

        @Override
        public void run() {
            WatchService watcher = null;
            Entry entry = null;
            try {
                watcher = setUpWatcher();

                while (true) {

                    entry = getFirstEntry();
                    if (entry == null) {
                        break;
                    }
                    startPolling(entry, watcher);
                }

            } catch (ClosedWatchServiceException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "ATCF: WatchService has been closed. " + e.getMessage(),
                        e);
            } catch (IOException | InterruptedException e) {
                statusHandler.handle(Priority.PROBLEM, e.getMessage(), e);
            } finally {
                try {
                    if (watcher != null) {
                        watcher.close();
                    }
                } catch (IOException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "ATCF: Problem occurred while trying to close WatchService. "
                                    + e.getMessage(),
                            e);
                }
            }
        }

    }

    /**
     * Set up and register watcher to monitor 'retrieved' directory
     *
     * @return the created WatchService
     * @throws IOException
     */
    private WatchService setUpWatcher() throws IOException {
        WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            retrievedPath.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE);

        } catch (IOException e) {
            throw new IOException(
                    "Failed to create WatchService. " + e.getMessage(), e);
        }
        return (watcher);
    }

    /**
     * Get the first entry in the collection of entries
     *
     * @return
     */
    private Entry getFirstEntry() {

        Entry entry = null;
        synchronized (entries) {
            /*
             * If all timeout durations are the same and collection maintains
             * insertion order, the earliest timeout will be the first entry
             */
            if (!entries.isEmpty()) {
                entry = entries.get(0);
            }
        }
        return entry;
    }

    /**
     * Start polling by the watcher for current entry
     *
     * @param entry
     * @param watcher
     * @throws InterruptedException
     */
    private void startPolling(Entry entry, WatchService watcher)
            throws InterruptedException {

        Instant now = Instant.now();
        Instant ptimeout = null;

        ptimeout = entry.expiration.minusSeconds(now.getEpochSecond());

        WatchKey key = watcher.poll(ptimeout.getEpochSecond(), TimeUnit.SECONDS);

        if (now.isBefore(entry.expiration) && key != null) {

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                Path fileName = ((WatchEvent<Path>) event).context();
                String file = fileName.toString();
                /*
                 * If file creation is detected in the 'retrieved' directory,
                 * see if it matches any of the current entries
                 */
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                    synchronized (entries) {
                        boolean fileFound = false;
                        Iterator<Entry> entIter = entries.iterator();
                        while (entIter.hasNext() && !fileFound) {

                            Entry curEntry = entIter.next();

                            Storm storm = curEntry.storm;
                            String dtg = curEntry.dtg;
                            String id = storm.getStormId().toLowerCase();

                            if (file.startsWith("ATCF_ReturnFile_" + id)) {
                                /*
                                 * File has been detected; try to move it to
                                 * unique temp area
                                 */
                                try {
                                    processFile(file, storm, dtg, envConfig);
                                    // Notify user that data has been retrieved
                                    sendDataRetrievedMsg(curEntry);
                                } catch (Exception e) {
                                    statusHandler.handle(Priority.PROBLEM,
                                            e.getMessage());
                                    // Notify user that data processing had
                                    // problem
                                    sendProcessProbMsg(curEntry, e);
                                }

                                // Remove processed entry
                                entIter.remove();

                                fileFound = true;

                            }
                        }
                    }
                }
            }
            if (!key.reset()) {
                statusHandler.handle(Priority.PROBLEM,
                        "ATCF: WatchService could not reset watch key.");
            }

        } else {
            // Polling has timed out. Make a final check of the directory
            checkDirForFile(entry);
        }
    }

    /**
     * If there is a timeout, perform one last check for the existence of an
     * entry's file before reporting. If such a file exists, check its time
     * stamp against the entry's earliest possible time. Older storm files would
     * be expected to have been removed from the directory already, but check in
     * case an older file (e.g. a file from a user's earlier submission for the
     * same storm) is present
     *
     * @param entry
     */
    private void checkDirForFile(Entry entry) {

        Storm storm = entry.storm;
        String id = storm.getStormId();
        Instant exp = entry.expiration;
        long earliestTime = exp.toEpochMilli() - timeout;
        FilenameFilter filter = new FilenameFilter() {

            // @Override
            @Override
            public boolean accept(File retrievedDir, String name) {
                File file = null;

                if (name.startsWith("ATCF_ReturnFile_" + id)) {
                    file = new File(atcfRtrieved + File.separator + name);

                    return (file.lastModified() > earliestTime);
                }

                return false;
            }
        };

        // Find files that match the above-defined filter
        File retrievedDir = new File(atcfRtrieved);
        String[] files = retrievedDir.list(filter);

        if (files.length == 1) {
            // Found a matching file
            File file = new File(files[0]);
            String dtg = entry.dtg;
            try {
                processFile(file.toString(), storm, dtg, envConfig);
            } catch (Exception e) {
                statusHandler.handle(Priority.PROBLEM, e.getMessage());
                sendProcessProbMsg(entry, e);
            }
            sendDataRetrievedMsg(entry);
            synchronized (entries) {
                entries.remove(entry);
            }
        } else {
            /*
             * If no files or more than 1 file meets the criteria, send failure
             * message
             */
            sendDataMissingMsg(entry);
            synchronized (entries) {
                entries.remove(entry);
            }
        }
    }

    /**
     * Move the retrieved file to a unique temp directory
     *
     * @param retName
     * @return
     */
    private void processFile(String fileName, Storm storm, String dtg,
            AtcfEnvironmentConfig envConfig) throws Exception {
        Path oldPath = Paths.get(retrievedPath + File.separator + fileName);
        long modTime = 0;
        // Add file's modified time as part of subdirectory name
        try {
            modTime = Files.getLastModifiedTime(oldPath).toMillis();
        } catch (IOException e) {
            throw new IOException("ATCF: Failed to get modified time of "
                    + oldPath + "  " + e.getMessage(), e);
        }
        String timeStamp = String.valueOf(modTime).substring(0, 10);
        String rootNewDir = retrievedPath.toString() + "/tmp." + timeStamp;
        String newFile = rootNewDir + File.separator + fileName;
        Path newPath = Paths.get(newFile);

        try {
            Files.createDirectories(newPath);
        } catch (IOException e) {
            throw new IOException("ATCF: Failed to create directory " + newPath
                    + "  " + e.getMessage(), e);
        }

        try {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new IOException("ATCF: Failed to move " + oldPath + " to "
                    + newPath + "  " + e.getMessage(), e);
        }
        ProcessRetrievedFiles prc = new ProcessRetrievedFiles();
        try {
            prc.execProcessScript(newFile, rootNewDir, storm, dtg, envConfig);
        } catch (JepException e) {
            throw new JepException(
                    "ATCF: Exception running ProcessRetrievedFiles script. "
                            + e.getMessage(), e);
        }
    }

    /**
     * Get current date/time string plus offset in milliseconds in supplied
     * format
     *
     * @param timeformat
     * @param offset
     * @return the date/time string
     */
    private String getDateTime(String timeformat, long offset) {
        // Get current date/time string plus offset provided
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeformat)
                .withZone(ZoneOffset.UTC);
        return formatter
                .format(Instant.now().plus(offset, ChronoUnit.MILLIS));
    }

    /**
     * Alert user that sending of data did not happen
     *
     * @param stormid
     * @param dtg
     */
    private void sendFailedMsg(String stormid, String dtg, String msg) {
        EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                "SendAtcfDataToWcossRequestHandler", MSG_SOURCE, MSG_CATAGORY,
                "ATCF Storm " + stormid
                        + "  --> GUIDANCE *NOT* SUBMITTED for Date-Time: "
                        + dtg,
                msg, null);

    }

    /**
     * Alert user of data receipt
     *
     * @param entry
     */
    private void sendDataRetrievedMsg(Entry entry) {
        String strmid = entry.storm.getStormId().toLowerCase();
        EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                "SendAtcfDataToWcossRequestHandler", MSG_SOURCE, MSG_CATAGORY,
                "ATCF Storm " + strmid + " --> GUIDANCE for Date-Time: "
                        + entry.dtg + " has been received",
                null, null);

    }

    private void sendProcessProbMsg(Entry entry, Exception e) {
        String strmid = entry.storm.getStormId().toLowerCase();
        EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                "SendAtcfDataToWcossRequestHandler", MSG_SOURCE, MSG_CATAGORY,
                "PROBLEM processing ATCF Storm data for " + strmid
                        + " --> GUIDANCE for Date-Time: " + entry.dtg,
                e.toString(), null);
    }

    /**
     * Alert user data not received in the expected amount of time
     *
     * @param entry
     */
    private void sendDataMissingMsg(Entry entry) {
        String strmid = entry.storm.getStormId().toLowerCase();
        EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                "SendAtcfDataToWcossRequestHandler", MSG_SOURCE, MSG_CATAGORY,
                "ATCF Storm " + strmid
                        + " --> GUIDANCE *NOT* READY for Date-Time: "
                        + entry.dtg + ". Total guidance runtime has expired",
                null, null);

    }
}
