package gov.noaa.nws.ocp.edex.plugin.atcf.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseBDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseEDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseFDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * Utilities for file manipulation at EDEX side, including creating flat file
 * output of ATCF DB data, reading and writing files to a configured directory
 * etc.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Jul 24, 2020 79366      mporricelli  Initial creation
 * Oct 19, 2020 82721      jwu          Read/write advisory
 * Nov 12, 2020 84446      dfriedman    Refactor error handling
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
public class AtcfFileUtil {

    public static final AtcfEnvironmentConfig envConfig = AtcfConfigurationManager
            .getEnvConfig();

    private static final String ATCFTMP = envConfig.getAtcftmp();

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AtcfFileUtil.class);

    private AtcfFileUtil() {

    }

    /**
     * Create storms.txt file
     *
     * @param queryStormCond
     * @param tmpFile
     * @param stormListFile
     * @param basin
     * @return success (true) or failure (false)
     * @throws Exception
     */
    public static boolean createStormListFile(List<Storm> stormRecs,
            String stormListFile) {

        File tmpDir = createTempDir();
        if (tmpDir == null) {
            return false;
        }

        File tmpFile;
        try {
            tmpFile = File.createTempFile(stormListFile, null, tmpDir);
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error creating temporary storm.txt file", e);
            return false;
        }

        Storm.sortStormList(stormRecs);

        // Write records out to temporary storms.txt file
        try (BufferedWriter fos = Files.newBufferedWriter(tmpFile.toPath())) {
            for (Storm rec : stormRecs) {
                fos.write(rec.toCsvStormString());
                fos.newLine();
            }
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error writing storm data to " + tmpFile, e);
            deleteTmpFile(tmpFile);
            return false;
        }

        // Move temporary file to storms directory
        try {
            Files.move(tmpFile.toPath(), Paths.get(stormListFile),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM, "Error moving " + tmpFile
                    + "to " + stormListFile, e);
            deleteTmpFile(tmpFile);
            return false;
        }

        return true;
    }

    /**
     * Create the CSV-formatted deck files (e.g. aal201711.dat)
     *
     * @param stormid
     * @param type
     * @param queryConditions
     * @return success (true) or failure (false)
     */
    @SuppressWarnings("unchecked")
    public static boolean createDeckFile(
            List<? extends AbstractDeckRecord> deckRecs, AtcfDeckType deckType,
            String deckFile) {

        switch (deckType) {
        case A:
            BaseADeckRecord.sortADeck((List<BaseADeckRecord>) deckRecs);
            break;

        case B:
            BaseBDeckRecord.sortBDeck((List<BaseBDeckRecord>) deckRecs);
            break;

        case E:
            BaseEDeckRecord.sortEDeck((List<BaseEDeckRecord>) deckRecs);
            break;

        case F:
            BaseFDeckRecord.sortFDeck((List<BaseFDeckRecord>) deckRecs);
            break;

        case T:
            ForecastTrackRecord
                    .sortFstDeck((List<ForecastTrackRecord>) deckRecs);
            break;

        default:
            statusHandler.handle(Priority.PROBLEM,
                    "Invalid deck type" + deckType);
            return false;
        }
        return writeOutputFile(deckRecs, deckFile, deckType);
    }

    /**
     * @param deckRecs
     * @param deckFile
     * @param deckType
     * @return true or false
     */
    private static boolean writeOutputFile(
            List<? extends AbstractDeckRecord> deckRecs, String deckFile,
            AtcfDeckType deckType) {

        File tmpDir = createTempDir();
        if (tmpDir == null) {
            return false;
        }

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(deckFile, null, tmpDir);
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Could not create temporary deck file. ", e);
            return false;
        }

        // Write records out to temporary deck .dat file
        try (BufferedWriter fos = Files.newBufferedWriter(tmpFile.toPath())) {
            StringBuilder sb = new StringBuilder();
            for (AbstractDeckRecord rec : deckRecs) {
                switch (deckType) {
                case A:
                    sb.append(((BaseADeckRecord) rec).toADeckString());
                    break;
                case B:
                    sb.append(((BaseBDeckRecord) rec).toBDeckString());
                    break;
                case E:
                    sb.append(((BaseEDeckRecord) rec).toEDeckString());
                    break;
                case F:
                    sb.append(((BaseFDeckRecord) rec).toFDeckString());
                    break;
                case T:
                    sb.append(((BaseBDeckRecord) rec).toBDeckString());
                    break;
                default:
                    statusHandler.handle(Priority.PROBLEM,
                            "Invalid deck type: " + deckType);
                    return false;
                }
                sb.append("\n");
            }
            fos.write(sb.toString());
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM, "Error writing " + deckType
                    + "-deck data to " + tmpFile, e);
            deleteTmpFile(tmpFile);
            return false;
        }

        // Move the temporary deck file to final location
        try {
            Files.move(tmpFile.toPath(), Paths.get(deckFile),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error moving " + tmpFile + "to " + deckFile, e);
            deleteTmpFile(tmpFile);
            return false;
        }
        return true;
    }

    /**
     * Delete the temporary file
     *
     * @param tmpFile
     */
    private static void deleteTmpFile(File tmpFile) {
        try {
            Files.deleteIfExists(tmpFile.toPath());
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error deleting " + tmpFile.getAbsolutePath(), e);
        }
    }

    /**
     * Write into a text file. File will be created if not existing. If teh file
     * exists already, and content will be overwritten.
     *
     * @param stormId
     *            storm ID
     * @param Content
     *            Map of contents to be written into files.
     */
    public static void writeAdvisoryFiles(String stormId,
            Map<AdvisoryType, String> contents) throws Exception {

        String advPath = envConfig.getAdvisoryPath();
        checkPath(advPath);

        // TODO: Try to write other files if one fails?
        for (AdvisoryType advType : contents.keySet()) {
            String outFile = advPath + File.separator + stormId
                    + advType.getSuffix();
            writeFile(contents.get(advType), outFile);
        }
    }

    /**
     * Check if a path exists. If not, try to create.
     */
    public static void checkPath(String path) throws IOException {

        // Check if the directory exists.
        File file = new File(path);

        if (!file.isDirectory()) {
            Files.createDirectories(file.toPath());
            statusHandler.info("AtcfFileUtil: " + path + " created.");
        }
    }

    /**
     * Write a list of strings into a text file. File will be created if not
     * existing and the existing content will be overwritten.
     *
     * @param Content
     *            String to be written into file.
     *
     * @param fileName
     *            File to be written into (with full path)
     */
    public static void writeFile(String content, String fileName)
            throws Exception {

        try {
            Path path = Paths.get(fileName);
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (FileNotFoundException | NoSuchFileException e) {
            throw new IOException(
                    "AtcfFileUtil: Failed to write to file - " + fileName, e);
        }
    }

    /**
     * Write a list of strings into a text file. File will be created if not
     * existing and the existing content will be overwritten.
     *
     * @param List<String>
     *            Strings to be written into file.
     *
     * @param fileName
     *            File to be written into (with full path)
     *
     * @return true/false
     */
    public static boolean writeListIntoFile(List<String> content,
            String fileName) {

        try {
            Path path = Paths.get(fileName);
            Files.write(path, content, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            statusHandler.error(
                    "AtcfFileUtil: Failed to write to file - " + fileName, e);
            return false;
        }

        return true;
    }

    /**
     * Move an advisory file from its storage path to its archive path.
     *
     * @param fileName
     *            File to be moved (no path)
     *
     * @return true/false
     */
    public static boolean moveAdvisoryFile(String fileName) {
        String advPath = envConfig.getAdvisoryPath();
        String archPath = envConfig.getAdvisoryArchivePath();
        return moveFile(advPath, archPath, fileName);
    }

    /**
     * Move a file from one path to another path.
     *
     * @param fromPath
     *            Path the file exists.
     *
     * @param toPath
     *            Path the file to to be moved in.
     *
     * @param fileName
     *            File to be read (with full path)
     *
     * @return true/false
     */
    public static boolean moveFile(String fromPath, String toPath,
            String fileName) {

        try {
            checkPath(fromPath);
            checkPath(toPath);
            Path srcP = Paths.get(fromPath, fileName);
            Path desP = Paths.get(toPath, fileName);

            Files.move(srcP, desP, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            statusHandler.warn("AtcfFileUtil: Failed to move file " + fileName
                    + " from " + fromPath + " to " + toPath + ".", e);
            return false;
        }

        return true;
    }

    /**
     * Read a stormId.adv file into a list of strings.
     *
     * @param fileName
     *            File to be read (no path)
     *
     * @return List<String>
     */
    public static List<String> readAdvInfoFile(String fileName)
            throws Exception {

        String advPath = envConfig.getAdvisoryPath();
        String fil = advPath + File.separator + fileName;

        return readFileAsList(fil);
    }

    /**
     * Read a file into a list of strings.
     *
     * @param fileName
     *            File to be read (with full path)
     *
     * @return List<String>
     */
    public static List<String> readFileAsList(String fileName)
            throws Exception {

        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (FileNotFoundException | NoSuchFileException e) { // NOSONAR
            return Collections.emptyList();
        } catch (Exception e) {
            throw new IOException(
                    "AtcfFileUtil: Failed to read file " + fileName, e);
        }
    }

    /**
     * Read a file's header into a string.
     *
     * @param header
     *            A string to indicate the end of the header.
     *
     * @return String header of file.
     */
    public static String getFileHeader(String fileName, String header)
            throws Exception {

        List<String> fileContent = readFileAsList(fileName);

        StringBuilder sb = new StringBuilder();
        if (header != null && !header.trim().isEmpty()) {
            for (String str : fileContent) {
                if (str.startsWith(header)) {
                    sb.append(str);
                    sb.append("\n");
                } else {
                    break;
                }
            }
        }

        return sb.toString();
    }

    private static final File createTempDir() {
        File tmpDir = new File(ATCFTMP);
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectories(tmpDir.toPath());
            } catch (IOException e) {
                statusHandler.error("Failed to create directory " + tmpDir, e);
                return null;
            }
        }
        return tmpDir;
    }

}