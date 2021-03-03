package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.LoggerFactory;

/**
 * Adds the Add delete on completion to the exchange for the files
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#   Engineer  Description
 * ------------- --------  --------- -----------------
 * Oct 24, 2018  DCS-18691 jburks    Initial creation
 * Mar  3, 2021  8326      tgurney   Camel 3 fixes
 *
 * </pre>
 *
 * .
 *
 * @author jburks
 */
public class AddDeleteOnCompletion implements Processor {

    /*
     * Add the DeleteFileOnCompletion for each file to the exchange.
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        Object obj = exchange.getIn().getBody();
        if (obj instanceof String) {
            String filesAsString = (String) exchange.getIn().getBody();
            if (filesAsString != null) {
                String[] filePathsAsString = filesAsString.split(",");
                for (String fileString : filePathsAsString) {
                    if (exchange instanceof DefaultExchange) {
                        DefaultExchange defaultExchange = (DefaultExchange) exchange;
                        defaultExchange
                                .addOnCompletion(new DeleteFileOnCompletion(
                                        new File(fileString)));
                    } else {
                        exchange.getUnitOfWork()
                                .addSynchronization(new DeleteFileOnCompletion(
                                        new File(fileString)));
                    }
                }
            }
        } else {
            exchange.getIn().setBody("");
        }

    }

    /**
     * An inner class that handles the Deleting the file on completion of the
     * pipeline.
     */
    private static class DeleteFileOnCompletion implements Synchronization {

        /** The file to be deleted. */
        private final File file;

        /**
         * Instantiates a new delete file on completion.
         *
         * @param file
         *            the file marked for deletion.
         */
        public DeleteFileOnCompletion(File file) {
            this.file = file;
        }

        @Override
        public void onComplete(Exchange exchange) {
            delete();
        }

        @Override
        public void onFailure(Exchange exchange) {
            delete();
        }

        /**
         * Delete the file.
         */
        private void delete() {
            if (file.exists()) {
                if (!file.delete()) {
                    LoggerFactory.getLogger(AddDeleteOnCompletion.class)
                            .error("Unable to delete file: {}", file);
                }
            }
        }

    }

}
