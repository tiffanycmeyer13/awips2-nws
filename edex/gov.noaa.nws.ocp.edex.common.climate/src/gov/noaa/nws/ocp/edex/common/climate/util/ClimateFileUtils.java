/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.RunProcess;

/**
 * Common File operations class for EDEX Climate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 12, 2015            xzhang      Initial creation
 * 19 AUG 2016  20753      amoore      EDEX common utils consolidation and cleanup.
 * 23 NOV 2016  21381      amoore      Cleaning up F6 printing.
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 10 MAR 2017  30130      amoore      Removal of files.
 * 19 JUN 2017  33104      amoore      Use common process exec class.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public final class ClimateFileUtils {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateFileUtils.class);

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateFileUtils() {
    }

    /**
     * Write the given lines to the given file in the given directory. If any
     * part of the directory path does not exist, create it. If the file does
     * not exist, create it.
     * 
     * @param lines
     *            lines to write.
     * @param dir
     *            directory the desired file exists in.
     * @param fileName
     *            file to write to.
     * @throws IOException
     *             on failure to write/create a directory.
     */
    public static void writeStringsToFile(List<String> lines, String dir,
            String fileName) throws IOException {
        Path file = Paths.get(dir + fileName);
        Files.createDirectories(Paths.get(dir));
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /**
     * Print the given files using the EDEX server's default printer.
     * 
     * @param files
     * @throws IOException
     */
    public static void printFiles(List<String> files) throws IOException {
        for (String f : files) {
            RunProcess process = RunProcess.getRunProcess()
                    .exec(new String[] { "bash", "-c", "lpr " + f });
            int status = process.waitFor();

            if (status == RunProcess.INTERRUPTED) {
                logger.error(
                        "Print file process interrupted for file: [" + f + "]");
            } else if (status == RunProcess.UNKNOWN) {
                logger.warn("Unknown status for print file process for file: ["
                        + f + "]");
            }

            logger.debug("Standard out from printing for file: [" + f + "]: "
                    + process.getStdout());

            if (!process.getStderr().isEmpty()) {
                logger.error("Some error occurred printing file: [" + f + "]: "
                        + process.getStderr());
            }
        }

    }

    /**
     * Delete the given files.
     * 
     * @param files
     * @throws IOException
     */
    public static void deleteFiles(List<String> files) throws IOException {
        for (String f : files) {
            Files.deleteIfExists(Paths.get(f));
        }
    }
}
