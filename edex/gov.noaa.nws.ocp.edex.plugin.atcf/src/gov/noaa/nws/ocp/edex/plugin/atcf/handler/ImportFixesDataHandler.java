/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfSubregion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecordKey;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ImportFixesRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.util.DtgUtil;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;
import gov.noaa.nws.ocp.edex.plugin.atcf.decoder.AtcfDeckProcessor;

/**
 * Handler to import fix (F-Deck) data for a storm.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 14, 2020 81449      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ImportFixesDataHandler
        implements IRequestHandler<ImportFixesRequest> {

    // Logger.
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ImportFixesDataHandler.class);

    // Field separator in fix data file.
    private static final String FIELD_SEPARATOR = ",";

    // File name extension for fix data file.
    private static final String FIX_FILE_EXT = "_FIX";

    private static final String FIX_AMSU_AFX = ".AFX";

    private static final String FIX_DAT = ".dat";

    // File name extension for the combined fix data file.
    private static final String FIX_FILE_IMP = ".new";

    // Special fix data type.
    private static final String TYPE_ALL = "ALL";

    private static final String TYPE_AMSU = "AMSU";

    // Storm to import fix data for.
    private Storm storm = null;

    // Directory where new fix data files could be pushed in.
    private String fin;

    // File name extensions for fix data.
    private String suffix1 = null;

    private String suffix2 = null;

    @Override
    public Boolean handleRequest(ImportFixesRequest request) throws Exception {

        storm = request.getStorm();

        String stormId = storm.getStormId().toUpperCase();
        String fixType = request.getFixType().toUpperCase();
        String stormYr = stormId.substring(4, 8);
        boolean acceptDatFile = request.isAcceptDatFile();

        fin = AtcfConfigurationManager.getEnvConfig().getFixDataIn();
        String fout = AtcfConfigurationManager.getEnvConfig().getFixDataOut();

        // Check if the directory for fix data exists.
        File fixDataDir = new File(fin);
        if (!fixDataDir.isDirectory()) {
            logger.warn(
                    "ImportFixesDataHandler: " + fin + " is not a directory.");

            if (fixDataDir.mkdirs()) {
                logger.info("ImportFixesDataHandler: " + fin
                        + " created. Drop the fix data files there for importing.");
            } else {
                logger.error(
                        "ImportFixesDataHandler: failed to create fix data directory - "
                                + fin);
                return false;
            }
        }

        // Check if the directory exists for backing up imported fix data files.
        File fixMsgDir = new File(fout);
        if (!fixMsgDir.isDirectory() && !fixMsgDir.mkdirs()) {
            logger.warn(
                    "ImportFixesDataHandler: failed to create fix messages directory - "
                            + fin);
        }

        // Find fix data extensions allowed based on the storm's basin
        loadFileExtensions(stormId);

        // Get a file name filter to fix data files.
        FilenameFilter filter = getFilenameFilter(fixType, stormId, stormYr,
                acceptDatFile);

        // Find files that match the above-defined filter
        String[] files = fixDataDir.list(filter);
        if (files.length == 0) {
            logger.info("ImportFixesDataHandler: no fix data found for storm "
                    + stormId + " with fix type " + fixType);
            return false;
        }

        // Read and find unique records in fdeck table.
        Map<FDeckRecordKey, FDeckRecord> eRecMap = loadExistingRecords();

        // Read and find unique records in all matching fix data files.
        List<String> recToAdd = getRecords(files, eRecMap);
        if (recToAdd.isEmpty()) {
            logger.info(
                    "ImportFixesDataHandler: no unique fix data found for storm "
                            + stormId + " with fix type " + fixType);
            return false;
        }

        // Write to a file (i.e. fal112017.new) & decode into fdeck table.
        String outFileName = AtcfDeckType.F.name().toLowerCase()
                + stormId.toLowerCase() + FIX_FILE_IMP;
        Path outFile = Paths.get(fin, outFileName);

        writeListIntoFile(recToAdd, outFile);

        AtcfDeckProcessor decoder = new AtcfDeckProcessor();
        try {
            decoder.mergeDeck(outFile.toString());
        } catch (Exception e) {
            logger.error("ImportFixesDataHandler: Failed to import fixes from "
                    + outFile + ".", e);
            return false;
        }

        // Move files to the backup directory.
        try {
            for (String fileName : files) {
                Path src = Paths.get(fin, fileName);
                Path des = Paths.get(fout, fileName);
                Files.move(src, des, StandardCopyOption.REPLACE_EXISTING);
            }

            // Decoded file is backed up with millisecond in file name.
            Path src = outFile;
            Calendar cal = TimeUtil.newGmtCalendar();
            Path des = Paths.get(fout,
                    outFileName + "." + DtgUtil.formatLong(cal)
                            + Long.toString(cal.get(Calendar.MILLISECOND)));

            Files.move(src, des, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.warn(
                    "ImportFixesDataHandler: Failed to move fix data files to "
                            + fout,
                    e);
        }

        return true;
    }

    /*
     * Read a file into a list of strings.
     *
     * @param fileName File to be read (with full path)
     *
     * @return List<String>
     */
    private List<String> readFileAsList(Path path) {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            return Files.readAllLines(path);
        } catch (Exception e) {
            logger.error("ImportFixesDataHandler: Failed to read fix data file "
                    + path, e);
            return Collections.emptyList();
        }
    }

    /*
     * Write a list of strings into a text file. File will be created if not
     * existing and the existing content will be overwritten.
     *
     * @param List<String> Strings to be written into file.
     *
     * @param fileName File to be read (with full path)
     *
     * @return true/false
     */
    private boolean writeListIntoFile(List<String> content, Path path) {

        try {
            Files.write(path, content, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.error(
                    "ImportFixesDataHandler: Failed to write fix data tofile - "
                            + path,
                    e);
            return false;
        }

        return true;
    }

    /*
     * Check if the fix type from the file matches a given type.
     *
     * @param fixType Fix type to be matched.
     *
     * @param typeFromName Fix Type derived from file name pattern.
     *
     * @param name Fix data file name.
     *
     * @return true/false
     */
    private boolean typeMatched(String fixType, String typeFromName,
            String name) {
        boolean matched = false;

        // Check type ALL and the given type
        if (TYPE_ALL.equals(fixType)
                || fixType.equals(typeFromName.toUpperCase())) {
            matched = true;
        } else {
            /*
             * Check to see if the type in the records of the file matches the
             * given type (i.e. NSOF_MTCSWA_201709010600_11L_FIX).
             */
            List<String> lines = readFileAsList(Paths.get(fin, name));
            if (!lines.isEmpty()) {
                String[] fields = lines.get(0).split(FIELD_SEPARATOR);
                if (fields.length > 4) {
                    String type = fields[4].trim().toUpperCase();
                    if (fixType.equals(type)) {
                        matched = true;
                    }
                }
            }
        }

        return matched;
    }

    /*-
     * Create a file name filter to match those match the criteria.
     *
     * The file name is in format of "site_type_yyyymmddhhmm_ccb_FIX".
     *        Where "cc" is storm number, "b" is storm sub basin.
     * Special case 1 :
     *        AMSU - it could be <storm-id>_yyyymmddhh_NOAA1[568].AFX
     * Special case 2:  type from file name cannot match known types.
     *        NSOF_MTCSWA_201709010600_11L_FIX
     *
     * @param fixType Fix type to be matched.
     * @param stormId
     * @param stormYr
     * @boolean acceptDat Flag to accept ".dat" file
     *
     * @return FilenameFilter
     */
    private FilenameFilter getFilenameFilter(String fixType, String stormId,
            String stormYr, boolean acceptDat) {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {

                // First check if it is a ".dat" file, i.e, fal112017.dat
                if (acceptDat && name
                        .equalsIgnoreCase(AtcfDeckType.F.name().toLowerCase()
                                + storm.getStormId() + FIX_DAT)) {
                    return true;
                }

                // Special case for AMSU: <storm-id>_yyyymmddhh_NOAA1[568].AFX
                if (TYPE_AMSU.equals(fixType)
                        && (name.endsWith(FIX_AMSU_AFX) && name.toUpperCase()
                                .startsWith(stormId + "_" + stormYr))) {
                    return true;
                } else {
                    // Check file name extensions
                    if ((suffix1 != null && name.endsWith(suffix1))
                            || (suffix2 != null && name.endsWith(suffix2))) {

                        String[] tokens = name.split("_");

                        // Check storm year.
                        String yr = null;
                        if (tokens.length > 4) {
                            // TAFB_DVTO_yyyymmddhhmm_ccb_FIX
                            yr = tokens[2];

                            // CIRA_AMSU_NOAA1[5689]_yyyymmddhhmm_ccb_FIX
                            if (tokens.length > 5) {
                                yr = tokens[3];
                            }
                        }

                        // Check fix type.
                        if (yr == null || yr.startsWith(stormYr)) {
                            return typeMatched(fixType, tokens[1], name);
                        }
                    }
                }

                return false;
            }
        };
    }

    /*
     * Read all matching fix data files and combine unique records into a list.
     * The uniqueness is checking using basin, storm number, fix data dtg, fix
     * format, fix type, fix site (wmo id), and wind radius (34/50/64). Also
     * checked against the existing records in fdeck table.
     *
     * @param files Array of fix data files.
     *
     * @param eRecMap Map of records existing in fdeck table.
     *
     * @return List<String> Lines of unique records.
     */
    private List<String> getRecords(String[] files,
            Map<FDeckRecordKey, FDeckRecord> eRecMap) {

        List<String> recToAdd = new ArrayList<>();

        Map<FDeckRecordKey, String> newRecordMap = new HashMap<>();
        for (String fileName : files) {
            List<String> lines = readFileAsList(Paths.get(fin, fileName));

            for (String str : lines) {
                String[] fields = str.trim().split(FIELD_SEPARATOR);
                /*- 1-5 are basin, cy, dtg, fix format, fix type
                 *          AL, 11, 201709111745, 10, DVTS
                 *  17 is wind radius (34, 50 or 64)
                 *  31 is WMO ID (site)
                 */
                if (fields.length >= 32) {
                    String dtg = fields[2].trim();
                    String fmt = fields[3].trim();
                    String type = fields[4].trim();
                    String site = fields[30].trim();

                    float rad = AbstractAtcfRecord.RMISSD;
                    try {
                        rad = Float.parseFloat(fields[16].trim());
                    } catch (NumberFormatException ne) {
                        // invalid, use default.
                    }

                    int radWind = (int) rad;
                    FDeckRecordKey key = new FDeckRecordKey(type, fmt, dtg,
                            site, radWind);

                    FDeckRecord erec = eRecMap.get(key);
                    if (erec == null) {
                        String eline = newRecordMap.get(key);
                        if (eline == null) {
                            newRecordMap.put(key, str);
                            recToAdd.add(str);
                        }
                    }
                }
            }
        }

        return recToAdd;
    }

    /*
     * Loads all existing FDeckRecords in fdeck tables into a map.
     *
     * @return Map<FDeckRecordKey, FDeckRecord>
     */
    private Map<FDeckRecordKey, FDeckRecord> loadExistingRecords() {

        Map<FDeckRecordKey, FDeckRecord> eRecMap = new HashMap<>();

        AtcfProcessDao processDao = null;
        try {
            processDao = new AtcfProcessDao();
        } catch (Exception e) {
            logger.warn(
                    "ImportFixesDataHandler: AtcfProcessDao object creation failed."
                            + e);
            return eRecMap;
        }

        /*
         * Get all FDeckRecord in DB for the storm and put in map (duplicates
         * are removed)
         */
        Map<String, Object> queryConditions = new HashMap<>();
        queryConditions.put("basin", storm.getRegion());
        queryConditions.put("year", storm.getYear());
        queryConditions.put("cycloneNum", storm.getCycloneNum());

        eRecMap = new HashMap<>();

        try {
            List<? extends AbstractDeckRecord> results = processDao
                    .getDeckList(AtcfDeckType.F, queryConditions, -1);

            for (AbstractDeckRecord arecord : results) {
                FDeckRecord rec = (FDeckRecord) arecord;
                String type = rec.getFixType();
                String fmt = rec.getFixFormat();
                String site = rec.getFixSite();
                int windRad = (int) rec.getRadWind();
                String dtg = DtgUtil.formatLong(rec.getRefTime());

                FDeckRecordKey key = new FDeckRecordKey(type, fmt, dtg, site,
                        windRad);
                eRecMap.put(key, rec);
            }
        } catch (Exception e) {
            logger.warn(
                    "ImportFixesDataHandler: Failed to retrieve existing F-Deck records."
                            + e);
        }

        return eRecMap;
    }

    /*
     * Builds fix data file name extensions based on storm's basin.
     */
    private void loadFileExtensions(String stormId) {

        suffix1 = null;
        suffix2 = null;

        // Find storm's basin
        String basinName = stormId.substring(0, 2).toUpperCase();

        AtcfBasin basin = AtcfBasin.getBasin(basinName);
        if (basin != null) {
            // Find storm's sub-basins
            AtcfSubregion[] subRegs = basin.getSubRegions();
            if (subRegs.length > 0) {
                String subExt1 = subRegs[0].name();

                String stormNum = stormId.substring(2, 4);
                suffix1 = stormNum + subExt1.toUpperCase() + FIX_FILE_EXT;

                if (subRegs.length > 1) {
                    suffix2 = stormNum + subRegs[1].name().toUpperCase()
                            + FIX_FILE_EXT;
                }
            }
        }
    }

}