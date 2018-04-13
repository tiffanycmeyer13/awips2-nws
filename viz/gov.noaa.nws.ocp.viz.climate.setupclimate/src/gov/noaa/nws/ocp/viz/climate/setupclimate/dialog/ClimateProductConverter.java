/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductHeader;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;

/**
 * 
 * Class to convert a set of legacy climate product types in a source directory
 * into new XML format and save into the given directory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#   Engineer    Description
 * ------------ --------  ----------- --------------------------
 * Mar 27, 2017 29748     jwu         Initial creation
 * Mar 31, 2017 29748     jwu         Check for null ending char.
 * MAY 12, 2017 33104     amoore      Use abstract map.
 * May 12, 2017 29748     jwu         Skip "control_???_NWR" and "control_???_NWWS".
 * May 17, 2017 33104     amoore      FindBugs. Package reorg.
 * May 23, 2017 29748     jwu         Correct daily/period control files.
 * AUG 30, 2017 37472     jwu         Adjust between daily/period types.
 * NOV 15, 2017 38036     dmanzella   Fix discrepancies in snow flags
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class ClimateProductConverter {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProductConverter.class);

    /**
     * File name patterns for legacy product types.
     */
    private static Pattern headerPattern = Pattern
            .compile("header_(am|im|pm|mon|sea|ann)_([A-Z,0-9]{3})");

    private static Pattern controlPattern = Pattern
            .compile("control_(am|im|pm|mon|sea|ann)_([A-Z,0-9]{3})");

    private static Pattern stationPattern = Pattern
            .compile("station_(am|im|pm|mon|sea|ann)_([A-Z,0-9]{3})");

    private static Pattern headerDefaultPattern = Pattern
            .compile("header_default");

    /**
     * Prefixes for control/header/station files.
     */
    private static final String CONTROL_PREFIX = "control_";

    private static final String HEADER_PREFIX = "header_";

    private static final String STATION_PREFIX = "station_";

    private static final String NWR = "NWR";

    private static final String NWWS = "NWWS";

    /**
     * JAXB manager to write out product types.
     */
    private static final SingleTypeJAXBManager<ClimateProductType> jaxbManager = SingleTypeJAXBManager
            .createWithoutException(ClimateProductType.class);

    /**
     * Directory where the legacy product types exists.
     */
    private String sourceDir;

    /**
     * Directory where the XML product types should be sent to.
     */
    private String destDir;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent shell for this dialog.
     * @param src
     *            Source directory for legacy products.
     * @param dest
     *            Destination directory for XML products.
     */
    public ClimateProductConverter(String src, String dest) {
        sourceDir = src;
        destDir = dest;
    }

    /**
     * @return the sourceDir
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * @param sourceDir
     *            the sourceDir to set
     */
    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * @return the destDir
     */
    public String getDestDir() {
        return destDir;
    }

    /**
     * @param destDir
     *            the destDir to set
     */
    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    /**
     * Convert all legacy product types in an directory into XML and place onto
     * SITE.
     */
    public java.util.List<ClimateProductType> convert() {

        java.util.List<ClimateProductType> ptyps = new ArrayList<>();

        // Get a list of control files in the directory
        Map<String, Path> controlFiles = getFiles(sourceDir, controlPattern);
        if (controlFiles.size() == 0) {
            return ptyps;
        }

        // Get a list of header files in the directory
        Map<String, Path> headerFiles = getFiles(sourceDir, headerPattern);

        // Get a default header and apply the setting in the "header_default".
        Map<String, Path> headerDefaultFile = getFiles(sourceDir,
                headerDefaultPattern);
        ClimateProductHeader defaultHeader = ClimateProductHeader
                .getDefaultHeader();
        for (Entry<String, Path> entry : headerDefaultFile.entrySet()) {
            java.util.List<String> cont = readFileAsList(entry.getValue());
            defaultHeader = parseHeader(cont);
            // printHeader(defaultHeader);
            break;
        }

        // Get list of station files in the directory
        Map<String, Path> stationFiles = getFiles(sourceDir, stationPattern);

        /*
         * Loop through all control files.
         * 
         * Each control should have a corresponding header file and a station
         * file. If no header file found for a control, use "header_default". If
         * there is no default, we will use hard-coded default. If no station
         * file is missing, no default is applied and no stations are added in
         * and the user could edit the product type later to add stations.
         */
        for (Entry<String, Path> entry : controlFiles.entrySet()) {
            String ctrlKey = entry.getKey();
            ClimateProductType ctyp = new ClimateProductType();
            ctyp.setHeader(defaultHeader);

            String[] names = ctrlKey.split("_");

            /*
             * Proceed for valid control files, skip files "control_???_NWR" and
             * "control_???_NWWS".
             */
            if (names.length > 2 && !names[2].equals(NWR)
                    && !names[2].equals(NWWS)) {

                java.util.List<String> ctrlInfo = readFileAsList(
                        entry.getValue());

                // Retrieve & set PeriodType, prodID, Itype, fileName
                ctyp.setProdId(names[2].trim());

                ctyp.setItype(getIType(ctrlInfo));

                PeriodType ptyp = getPeriodType(ctrlInfo);
                ctyp.setReportType(ptyp);

                ctyp.setFileName(ClimateProductType.buildPreferedName(
                        ptyp.getPeriodName(), ptyp.getSource(),
                        ctyp.getProdId()));

                // Retrieve & set ClimateProductControl
                ctyp.setControl(parseControl(ctrlInfo));

                // Apply info in the header file for this product type.
                String headerKey = ctrlKey.replace(CONTROL_PREFIX,
                        HEADER_PREFIX);
                Path headerFile = headerFiles.get(headerKey);
                if (headerFile != null) {
                    java.util.List<String> headerInfo = readFileAsList(
                            headerFile);
                    ctyp.setHeader(parseHeader(headerInfo));
                }

                // Apply info in the station file for this product type.
                String stationKey = ctrlKey.replace(CONTROL_PREFIX,
                        STATION_PREFIX);
                Path stationFile = stationFiles.get(stationKey);
                if (stationFile != null) {
                    java.util.List<String> stationInfo = readFileAsList(
                            stationFile);
                    ctyp.setStations(parseStations(stationInfo));
                }

                /*
                 * Adjust product category in the header based on the
                 * PeriodType.
                 * 
                 * The third specific product designator of product NNN in AFOS
                 * product identifier (CCCNNNXXX) is the identifier for which
                 * header product you are going to use.
                 * 
                 * 1 - M = CLIMATE MONTHLY 2 - I = CLIMATE DAILY 3 - S = CLIMATE
                 * SEASON 4 - A = climate annual.
                 * 
                 * In Legacy, only I and M are seen so far.
                 */
                if (ptyp.isPeriod()) {
                    String cln = ptyp.getPeriodName().substring(0, 1)
                            .toUpperCase();
                    ctyp.getHeader().setThirdN(cln);
                    ctyp.getHeader().setProductCategory("CL" + cln);
                }

                // Add to the list.
                ptyps.add(ctyp);
            }
        }

        return ptyps;

    }

    /**
     * Get a map of files and their absolute paths under a given directory,
     * which matching a given file name pattern.
     * 
     * @param path
     *            Directory to look at.
     */
    private Map<String, Path> getFiles(String path, final Pattern namePattern) {

        Filter<Path> filter = new DirectoryStream.Filter<Path>() {
            public boolean accept(Path file) throws IOException {
                return (namePattern.matcher(file.toFile().getName()).matches());
            }
        };

        Map<String, Path> result = new HashMap<>();
        try (DirectoryStream<Path> stream = Files
                .newDirectoryStream(Paths.get(path), filter)) {
            for (Path entry : stream) {
                Path fileName = entry.getFileName();
                if (fileName != null) {
                    result.put(fileName.toString(), entry);
                } else {
                    logger.warn("Got a null file path from entry: ["
                            + entry.toString() + "]");
                }
            }
        } catch (Exception ex) {
            logger.warn("ClimateProductConverter: Cannot retrieve files from "
                    + path + " due to " + ex);
        }

        return result;
    }

    /**
     * Read the contents of a file into a list of strings (one line per string).
     * 
     * @param file
     *            A file to be read.
     * @return List<String> Content of the file.
     */
    private java.util.List<String> readFileAsList(Path file) {

        java.util.List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(file)) {
            list = stream.collect(Collectors.<String> toList());
        } catch (IOException e) {
            logger.warn("ClimateProductConverter: Cannot read files from "
                    + file.getFileName() + " due to " + e);
        }

        return list;
    }

    /**
     * Write a product type to an XML file.
     * 
     * @param ptyp
     *            A ClimateProductType.
     * @param filePath
     *            Path to write into.
     */
    private void writeProductAsXML(ClimateProductType ptyp, String filePath) {

        try {
            jaxbManager.marshalToXmlFile(ptyp,
                    filePath + File.separator + ptyp.getFileName());
        } catch (SerializationException e) {
            logger.warn("ClimateProductConverter: Cannot write XML product  "
                    + ptyp.getName() + " to " + filePath + " due to " + e);
        }

    }

    /**
     * Write a list of product type to XML files.
     * 
     * @param ptyps
     *            List<ClimateProductType>.
     * @param filePath
     *            Path to write into.
     */
    public void writeXMLProducts(java.util.List<ClimateProductType> ptyps,
            String filePath) {

        for (ClimateProductType ptyp : ptyps) {
            writeProductAsXML(ptyp, filePath);
        }
    }

    /**
     * Parse a legacy header file into a ClimateProductHeader.
     * 
     * @param List<String>
     *            Strings read from a legacy header file (one line per string).
     * @return parseHeader() A ClimateProductHeader.
     */
    private ClimateProductHeader parseHeader(java.util.List<String> header) {

        ClimateProductHeader cpheader = ClimateProductHeader.getDefaultHeader();

        int jj = 0;
        while (jj < header.size()) {

            String lineStr = header.get(jj);

            switch (jj) {

            case 1:
                /*
                 * Product number
                 */
                int prdNum = parseInt(lineStr, 5, 8);
                if (prdNum > 0) {
                    cpheader.setProdNum(prdNum);
                }

                /*
                 * Style
                 */
                int style = parseInt(lineStr, 12, 15);
                if (style > 0) {
                    cpheader.setStyle(style);
                }
                break;

            case 3:
                /*
                 * AIS - ACTIVE/INACTIVE STORAGE UPON OCCURRENCE OF EXPIRATION
                 * (ACTIVE=A, INACTIVE=I)
                 */
                String ais = parseStr(lineStr, 3, 5);
                cpheader.setActiveStorage(ais);

                /*
                 * ATM - ALERT TONE TO SEND FRONT OF THE MESSAGE (" "=NO ALERT
                 * TONE BUT NWSAME TONE,"N" NO ALERT TONE AND NO NWRSAME TONE,
                 * "A"=ALERT TONE AND NWRSAME TONE)
                 */
                String atm = parseStr(lineStr, 7, 9);
                cpheader.setAlertTone(atm);

                /*
                 * BMI - BEGIN MESSAGE INDICATOR.
                 */
                String bmi = parseStr(lineStr, 10, 13);
                cpheader.setBeginMsgIndicator(bmi);

                /*
                 * CCC - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE CCC IS THE
                 * NODE ORIGINATION SITE.
                 */
                String ccc = parseStr(lineStr, 14, 18);
                cpheader.setNodeOrigSite(ccc);

                /*
                 * DSM - DELETE/SAVE MESSAGE (DELETE-D, SAVE-S)
                 */
                String dsm = parseStr(lineStr, 20, 22);
                cpheader.setDelSaveMsg(dsm);

                /*
                 * FTN - FIRST TWO SPECIFIC PRODUCT DESIGNATORS OF NNN IN AFOS
                 * PRODUCT IDENTIFIER (CCCNNNXXX) ARE THE CLIMATE IDENTIFIERS. 1
                 * - CL = THIS STANDS FOR THE CLIMATE.
                 */
                String ftx = parseStr(lineStr, 23, 26);
                cpheader.setFirstTwoN(ftx);

                /*
                 * THN - THIRD SPECIFIC PRODUCT DESIGNATOR OF NNN IN AFOS
                 * PRODUCT IDENTIFIER (CCCNNNXXX) IS THE IDENTIFER FOR WHICH
                 * HEADER PRODUCT YOUR GOING TO USE. 1 - M = CLIMATE MONTHLY 2 -
                 * I = CLIMATE DAILY 3 - S = CLIMATE SEASON 4 - A = climate
                 * annual
                 */
                String thn = parseStr(lineStr, 28, 30);
                cpheader.setThirdN(thn);

                /*
                 * IFM - INTERRUPT FLAG FOR THE MESSAGE (I=INTERRUPT, (SPACE)=NO
                 * INTERRUPT).
                 */
                String ifm = parseStr(lineStr, 32, 34);
                cpheader.setInterruptMsg(ifm);

                /*
                 * STATIONID - THE THREE LETTER CODE FOR THE STATION THAT IS
                 * BEING BROADCASTED.
                 */
                String sid = parseStr(lineStr, 35, 39);
                cpheader.setStationId(sid);

                /*
                 * MCO - MESSAGE CONFIRMATION ON OR OFF (ON="C", OFF=(SPACE)).
                 */
                String mco = parseStr(lineStr, 41, 43);
                cpheader.setMessageConfirm(mco);

                /*
                 * MRD - MESSAGE REFERENCE DESCRIPTOR (always 0).
                 */
                String mrd = parseStr(lineStr, 45, 47);
                cpheader.setMsgReference(mrd);

                /*
                 * xxx - THE THREE LETTER CODE FOR THE STATION THAT IS BEING
                 * BROADCASTED (same as station ID).
                 */
                String xxx = parseStr(lineStr, 48, 52);
                cpheader.setStationId(xxx);

                /*
                 * NTM - THE # OF THE CURRENT LISTENING TOWERS (1 - 10).
                 */
                String ntm = parseStr(lineStr, 54, 56);
                cpheader.setNumListenTowers(ntm);

                /*
                 * VTT - MESSAGE FORMAT (V_ENG=ENGLISH VOICE, V_SPA=SPANISH
                 * VOICE, T_ENG=ENGLISH TEXT, T_SPA=SPANISH TEXT).
                 */
                String vtt = parseStr(lineStr, 57, 63);
                cpheader.setMessageFormat(vtt);

                break;

            case 5:
                /*
                 * LAC - THE LISTENING AREA CODE FIELD.
                 */
                int end = Math.min(lineStr.length(), 40);
                String lac = parseStr(lineStr, 1, end);
                cpheader.setListenAreaCode(lac);
                break;

            case 7:
                /*
                 * EXP_MIN - THIS WILL HOLD THE MINUTES UNTIL THE EXPIRATION
                 * TIME IS ACTIAVADE (Actually never updated from GUI.)
                 */
                int exp_min = parseInt(lineStr, 4, 6);
                if (exp_min >= 0) {
                    cpheader.setExpirationMin(exp_min);
                }
                break;

            case 9:
                /*
                 * Effective & expiration day/month/year
                 */
                int eff_day = parseInt(lineStr, 4, 6);
                int eff_mon = parseInt(lineStr, 12, 14);
                int eff_yr = parseInt(lineStr, 18, 22);
                if (eff_day > 0 && eff_mon > 0 && eff_yr > 0) {
                    cpheader.setEffectiveDate(
                            new ClimateDate(eff_day, eff_mon, eff_yr));
                }

                int exp_day = parseInt(lineStr, 28, 30);
                int exp_mon = parseInt(lineStr, 36, 38);
                int exp_yr = parseInt(lineStr, 42, 46);
                if (exp_day > 0 && exp_mon > 0 && exp_yr > 0) {
                    cpheader.setExpirationDate(
                            new ClimateDate(exp_day, exp_mon, exp_yr));
                }

                break;

            case 11:
                /*
                 * Effective & expiration hour/minute
                 */
                int eff_ihr = parseInt(lineStr, 4, 6);
                int eff_min = parseInt(lineStr, 12, 14);
                if (eff_ihr >= 0 && eff_min >= 0) {
                    ClimateTime ctime = new ClimateTime();
                    ctime.setHour(eff_ihr);
                    ctime.setMin(eff_min);
                    ctime.setAmpm((eff_ihr >= 12) ? "PM" : "AM");
                    ctime.setZone("LST");
                    cpheader.setEffectiveTime(ctime);
                }

                int exp_ihr = parseInt(lineStr, 20, 22);
                int exp_imin = parseInt(lineStr, 28, 30);
                if (exp_ihr >= 0 && exp_imin >= 0) {
                    ClimateTime ctime = new ClimateTime();
                    ctime.setHour(exp_ihr);
                    ctime.setMin(exp_imin);
                    ctime.setAmpm((exp_ihr >= 12) ? "PM" : "AM");
                    ctime.setZone("LST");
                    cpheader.setExpirationTime(ctime);
                }

                break;

            case 13:
                /*
                 * Creation day/mon/year
                 */
                int creat_day = parseInt(lineStr, 6, 8);
                int creat_mon = parseInt(lineStr, 16, 18);
                int creat_yr = parseInt(lineStr, 25, 29);
                if (creat_day > 0 && creat_mon > 0 && creat_yr > 0) {
                    cpheader.setCreationDate(
                            new ClimateDate(creat_day, creat_mon, creat_yr));
                }

                break;

            case 15:
                /*
                 * Creation hour/minute
                 */
                int creat_hour = parseInt(lineStr, 6, 8);
                int creat_min = parseInt(lineStr, 17, 19);
                if (creat_hour >= 0 && creat_min >= 0) {
                    ClimateTime ctime = new ClimateTime();
                    ctime.setHour(creat_hour);
                    ctime.setMin(creat_min);
                    ctime.setAmpm((creat_hour >= 12) ? "PM" : "AM");
                    ctime.setZone("LST");
                    cpheader.setCreationTime(ctime);
                }

                break;

            case 17:
                /*
                 * Period day/hour/minute
                 */
                int period_day = parseInt(lineStr, 7, 9);
                int period_hour = parseInt(lineStr, 18, 20);
                int period_min = parseInt(lineStr, 21, 31);
                if (period_day >= 0 && period_hour >= 0 && period_min >= 0) {
                    int prdMin = period_day * 24 * 60 + period_hour * 60
                            + period_min;
                    cpheader.setPeriodicity(prdMin);
                }

                break;

            case 19:
                // CCC NNN xxx AAA CDE1 CDE2 CDE3 CDE4_DAY CDE4_HOUR CDE4_MIN
                /*
                 * CCC - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE CCC IS THE
                 * NODE ORIGINATION SITE.
                 */
                String ccc1 = parseStr(lineStr, 1, 5);
                if (ccc1.length() > 0) {
                    cpheader.setNodeOrigSite(ccc1);
                }

                /*
                 * NNN - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE NNN IS THE
                 * PRODUCT CATEGORY.
                 * 
                 */
                String nnn1 = parseStr(lineStr, 6, 10);
                if (nnn1.length() > 2) {
                    cpheader.setProductCategory(nnn1);
                    cpheader.setFirstTwoN(nnn1.substring(0, 2));
                    cpheader.setThirdN(nnn1.substring(2, 3));
                }

                /*
                 * xxx - THE THREE LETTER CODE FOR THE STATION THAT IS BEING
                 * BROADCASTED (same as station ID).
                 */
                String xxx1 = parseStr(lineStr, 11, 15);
                if (xxx1.length() > 0) {
                    cpheader.setStationId(xxx1);
                }

                /*
                 * Address
                 */
                String aaa = parseStr(lineStr, 16, 20);
                cpheader.setAddress(aaa);

                /*
                 * CDE1/CDE2/CDE3
                 */
                String cde1 = parseStr(lineStr, 21, 28);
                cpheader.setCDE1(cde1);

                String cde2 = parseStr(lineStr, 30, 32);
                cpheader.setCDE2(cde2);

                String cde3 = parseStr(lineStr, 35, 39);
                cpheader.setCDE3(cde3);

                /*
                 * Creation date & time
                 */
                int cde4_day = parseInt(lineStr, 45, 47);

                int cde4_hour = parseInt(lineStr, 54, 56);

                int cde4_min = parseInt(lineStr, 64, 66);

                ClimateDate cdate = ClimateDate.getLocalDate();
                if (cde4_day >= 0) {
                    cdate.setDay(cde4_day);
                }
                cpheader.setCDE4_Date(cdate);

                ClimateTime cdeTime = ClimateTime.getLocalGMTTime();
                if (cde4_hour >= 0 && cde4_min >= 0) {
                    cdeTime.setHour(cde4_hour);
                    cdeTime.setMin(cde4_min);
                    cdeTime.setAmpm((cde4_hour >= 12) ? "PM" : "AM");
                }
                cpheader.setCDE4_Time(cdeTime);

                break;

            default:
                logger.info("ClimateProductConverter - skipping header line: ["
                        + jj + "] - [" + lineStr
                        + "]. Verify climate products are properly converted.");
                break;
            }

            jj++;
        }

        return cpheader;

    }

    /**
     * Parse a substring in a given string for an integer, default to 0.
     * 
     * @param str
     *            Input string.
     * @param start
     *            start index of substring.
     * @param end
     *            end index of substring.
     * @return parseInt() An integer value.
     */
    private int parseInt(String str, int start, int end) {

        int value = 0;
        int len = str.length();
        if (len > 0 && start < len) {
            int iend = Math.min(len, end);
            String inStr = getCleanString(str.substring(start, iend).trim());
            if (inStr.length() > 0) {
                try {
                    value = Integer.parseInt(inStr);
                } catch (NumberFormatException ne) {
                    // Use default value
                    logger.warn(
                            "ClimateProductConverter: error converting to int - "
                                    + ne);
                }
            }
        }

        return value;
    }

    /**
     * Get a substring in a given string, default to "".
     * 
     * @param str
     *            Input string.
     * @param start
     *            start index of substring.
     * @param end
     *            end index of substring.
     * @return parseStr() A trimmed substring.
     */
    private String parseStr(String str, int start, int end) {

        String value = "";
        int len = str.length();

        if (len > 0 && start < len) {
            int iend = Math.min(len, end);
            value = getCleanString(str.substring(start, iend).trim());
        }

        return value;
    }

    /**
     * Parse a substring in a given string to a double number with 4 digit
     * decimal, default to 0.0.
     * 
     * @param str
     *            Input string.
     * @param start
     *            start index of substring.
     * @param end
     *            end index of substring.
     * @return parseDouble() A double value.
     */
    private double parseDouble(String str, int start, int end) {

        double value = 0.0F;
        if (str.length() > 0) {
            int iend = Math.min(str.length(), end);
            String inStr = getCleanString(str.substring(start, iend).trim());
            if (inStr.length() > 0) {
                try {
                    value = Double.parseDouble(inStr);
                    value = Math.floor(value * 10000) / 10000.0;
                } catch (NumberFormatException ne) {
                    // Use default value
                    logger.warn(
                            "ClimateProductConverter: error converting to double -  "
                                    + ne);
                }
            }
        }

        return value;
    }

    // /**
    // * Print out ClimateProductHeader as in legacy text file for comparison.
    // *
    // * @param cpheader
    // * A ClimateProductHeader.
    // */
    // @SuppressWarnings("unused")
    // private void printHeader(ClimateProductHeader cpheader) {
    //
    // System.out.println("****** Climate Product Header info *******");
    // System.out.println("prod_num\tstyle");
    // System.out.println(cpheader.getProdNum() + "\t" + cpheader.getStyle());
    //
    // System.out.println(
    // "ais\tatm\t bmi\tCCC\tDSM\tftn\tthn\tIFM\tsid\tmco\tmrd\txxx\tntm\tvtt");
    // System.out
    // .println(cpheader.getActiveStorage() + "\t"
    // + cpheader.getAlertTone() + "\t" + cpheader
    // .getBeginMsgIndicator()
    // + "\t" + cpheader.getNodeOrigSite() + "\t"
    // + cpheader.getDelSaveMsg() + "\t"
    // + cpheader.getFirstTwoN() + "\t" + cpheader.getThirdN()
    // + "\t" + cpheader.getInterruptMsg() + "\t"
    // + cpheader.getStationId() + "\t"
    // + cpheader.getMessageConfirm() + "\t"
    // + cpheader.getMsgReference() + "\t"
    // + cpheader.getStationId() + "\t"
    // + cpheader.getNumListenTowers() + "\t"
    // + cpheader.getMessageFormat());
    //
    // System.out.println("LAC");
    // System.out.println(cpheader.getListenAreaCode());
    //
    // System.out.println("exp_min");
    // System.out.println(cpheader.getExpirationMin());
    //
    // System.out
    // .println("eff_day\teff_mon\teff_yr\texp_day\texp_mon\texp_yr");
    // ClimateDate cdate = cpheader.getEffectiveDate();
    // ClimateDate edate = cpheader.getExpirationDate();
    // System.out.println(cdate.getDay() + "\t" + cdate.getMon() + "\t"
    // + cdate.getYear() + "\t" + edate.getDay() + "\t"
    // + edate.getMon() + "\t" + edate.getYear());
    //
    // System.out.println("eff_ihr\teff_min\texp_ihr\texp_min");
    // ClimateTime ctime = cpheader.getEffectiveTime();
    // ClimateTime etime = cpheader.getExpirationTime();
    // System.out.println(ctime.getHour() + "\t" + ctime.getMin() + "\t"
    // + etime.getHour() + "\t" + etime.getMin());
    //
    // System.out.println("creat_day\tcreat_mon\tcreat_yr");
    // ClimateDate rdate = cpheader.getCreationDate();
    // System.out.println(rdate.getDay() + "\t\t" + rdate.getMon() + "\t\t"
    // + rdate.getYear());
    //
    // System.out.println("creat_hour\tcreat_min");
    // ClimateTime rtime = cpheader.getCreationTime();
    // System.out.println(rtime.getHour() + "\t\t" + rtime.getMin());
    //
    // System.out.println("period_day\tperiod_hrs\tperiod_min");
    // int emin = cpheader.getPeriodicity();
    // int iday = emin / TimeUtil.MINUTES_PER_DAY;
    // int ihr = (emin - TimeUtil.MINUTES_PER_DAY * iday - 1)
    // / TimeUtil.MINUTES_PER_HOUR;
    // int imin = emin - TimeUtil.MINUTES_PER_DAY * iday
    // - TimeUtil.MINUTES_PER_HOUR * ihr;
    // System.out.println(iday + "\t\t" + ihr + "\t\t" + imin);
    //
    // System.out.println(
    // "CCC\tNNN\tXXX\tAAA\tCDE1\tCDE2\tCDE3\tCDE4_DAY\tCDE4_HOUR\tCDE4_MIN");
    // ClimateDate adate = cpheader.getCDE4_Date();
    // ClimateTime atime = cpheader.getCDE4_Time();
    // System.out.println(cpheader.getNodeOrigSite() + "\t"
    // + cpheader.getProductCategory() + "\t" + cpheader.getStationId()
    // + "\t" + cpheader.getAddress() + "\t" + cpheader.getCDE1()
    // + "\t" + cpheader.getCDE2() + "\t" + cpheader.getCDE3() + "\t"
    // + adate.getDay() + "\t\t" + atime.getHour() + "\t\t"
    // + atime.getMin());
    //
    // }

    /**
     * Parse a legacy station file into a ProductTypeHeader.
     * 
     * @param station
     *            Strings from a legacy station file (one line per string).
     */
    private List<Station> parseStations(List<String> station) {

        List<Station> stationList = new ArrayList<>();

        int hsize = station.size();

        // Check how many stations in this file.
        int nstations = 0;
        if (hsize > 1) {
            nstations = parseInt(station.get(1), 0, 4);
        }

        // Parse stations.
        if (nstations > 0 && hsize > 3) {
            int jj = 3;
            while (jj < hsize) {
                // ICAO Station_name Informix UTC offset Latitude Longitude
                String icao = parseStr(station.get(jj), 1, 6);
                String name = parseStr(station.get(jj), 9, 39);
                int infomix = parseInt(station.get(jj), 41, 49);
                int utc = parseInt(station.get(jj), 56, 59);
                double lat = parseDouble(station.get(jj), 63, 72);
                double lon = parseDouble(station.get(jj), 73, 82);
                Station st = new Station();
                st.setIcaoId(icao);
                st.setStationName(name);
                st.setInformId(infomix);
                st.setNumOffUTC((short) utc);
                st.setDlat(lat);
                st.setDlon(lon);
                st.setStdAllYear((short) 0);
                stationList.add(st);

                jj++;
            }
        }

        // printStations(stationList);

        return stationList;
    }

    // /**
    // * Print out a list of climate station as in legacy text file for
    // * comparison.
    // *
    // * @param stationList
    // * List of stations.
    // */
    // @SuppressWarnings("unused")
    // private void printStations(java.util.List<Station> stationList) {
    //
    // System.out.println("num_station\n" + stationList.size());
    // System.out.println(
    // "ICAO\tStation_name\t\tInformix\tUTC_offset\tLatitude\tLongitude");
    // for (Station st : stationList) {
    // System.out.println(st.getIcaoId() + "\t" + st.getStationName()
    // + "\t\t" + st.getInformId() + "\t\t" + st.getNumOffUTC()
    // + "\t\t" + st.getRlat() + "\t\t" + st.getRlon());
    // }
    //
    // }

    /**
     * Parse a legacy control file into a ClimateProductControl.
     * 
     * @param ctrl
     *            Strings from a legacy control file (one line per string).
     *
     * @return ClimateProductControl A ClimateProductControl.
     */
    private ClimateProductControl parseControl(java.util.List<String> ctrl) {

        ClimateProductControl cpCtrl = ClimateProductControl
                .getDefaultControl();

        // itype => PeriodType
        PeriodType ptyp = getPeriodType(ctrl);

        // do_celsius
        String doCelius = ctrl.get(3).trim();
        boolean doCel = false;
        if (doCelius.equals("1") || doCelius.equals("T")) {
            doCel = true;
        }

        cpCtrl.setDoCelsius(doCel);

        /*
         * Climate periods for heat/cool/snow
         * 
         * TYPE Start day Start mon Start year End day End mon End year
         */
        ClimateDates coolDates = parseClimatePeriods(ctrl.get(5));
        ClimateDates heatDates = parseClimatePeriods(ctrl.get(6));
        ClimateDates snowDates = parseClimatePeriods(ctrl.get(7));

        cpCtrl.setCoolDates(coolDates);
        cpCtrl.setHeatDates(heatDates);
        cpCtrl.setSnowDates(snowDates);

        // Sanity check.
        if (ptyp == PeriodType.OTHER) {
            return cpCtrl;
        }

        /*
         * Now it is different for a daily and a period control file.
         * 
         * A daily product control file has 37 lines, while a period product
         * control file has 83 lines.
         */
        if (ptyp.isPeriod()) {
            // mon/sea/ann control
            parsePeriodControl(ctrl, cpCtrl);
        } else if (ptyp.isDaily()) {
            // am/im/pm control
            parseDailyControl(ctrl, cpCtrl);
        }

        return cpCtrl;

    }

    /**
     * Parse a climate period string in legacy control file into a ClimateDates.
     * 
     * @param periodStr
     *            A string like
     *            "Cool      1         3         0         1        11         0"
     *
     * @return ClimateDates A ClimateDates.
     */
    private ClimateDates parseClimatePeriods(String periodStr) {

        // TYPE Start day Start mon Start year End day End mon End year
        String valueStr = periodStr.trim();
        String[] inFlags = valueStr.split("\\s+");

        ClimateDates cdates = ClimateDates.getMissingClimateDates();

        int len = inFlags.length;

        int startDay = 0;
        if (len > 1) {
            startDay = parseInt(inFlags[1], 0, inFlags[1].length());
            if (startDay > 0) {
                cdates.getStart().setDay(startDay);
            }
        }

        int startMon = 0;
        if (len > 2) {
            startMon = parseInt(inFlags[2], 0, inFlags[2].length());
            if (startMon > 0) {
                cdates.getStart().setMon(startMon);
            }
        }

        int startYr = 0;
        if (len > 3) {
            startYr = parseInt(inFlags[3], 0, inFlags[3].length());
            if (startYr >= 0) {
                cdates.getStart().setYear(startYr);
            }
        }

        int endDay = 0;
        if (len > 4) {
            endDay = parseInt(inFlags[4], 0, inFlags[4].length());
            if (endDay > 0) {
                cdates.getEnd().setDay(endDay);
            }
        }

        int endMon = 0;
        if (len > 5) {
            endMon = parseInt(inFlags[5], 0, inFlags[5].length());
            if (endMon > 0) {
                cdates.getEnd().setMon(endMon);
            }
        }

        int endYr = 0;
        if (len > 6) {
            endYr = parseInt(inFlags[6], 0, inFlags[6].length());
            if (endYr >= 0) {
                cdates.getEnd().setYear(endYr);
            }
        }

        return cdates;
    }

    /**
     * Identify a PeriodType by its value.
     * 
     * @param itype
     *            Value of a PeriodType
     * @return getPeriodType() PeriodType
     */
    public static PeriodType getPeriodType(int itype) {

        PeriodType ptype = PeriodType.OTHER;

        for (PeriodType typ : PeriodType.values()) {
            if (typ.getValue() == itype) {
                ptype = typ;
                break;
            }
        }

        return ptype;
    }

    /**
     * Parse a daily control file (37 lines) and set into a
     * ClimateProductControl.
     * 
     * @param ctrl
     *            Strings from a daily control file (one line per string).
     * @param cpt
     *            The ClimateProductControl to be updated
     */
    private void parseDailyControl(java.util.List<String> ctrl,
            ClimateProductControl cpt) {

        // max_temp
        setControlFlags(ctrl, 9, cpt.getTempControl().getMaxTemp());

        // min_temp
        setControlFlags(ctrl, 10, cpt.getTempControl().getMinTemp());

        // avg_temp
        setControlFlags(ctrl, 11, cpt.getTempControl().getMeanTemp());

        // precip_day
        setControlFlags(ctrl, 12, cpt.getPrecipControl().getPrecipTotal());

        // precip_mon
        setControlFlags(ctrl, 13, cpt.getPrecipControl().getPrecipMonth());

        // precip_sea
        setControlFlags(ctrl, 14, cpt.getPrecipControl().getPrecipSeason());

        // precip_year
        setControlFlags(ctrl, 15, cpt.getPrecipControl().getPrecipYear());

        // snow_day
        setControlFlags(ctrl, 16, cpt.getSnowControl().getSnowTotal());

        // snow_mon
        setControlFlags(ctrl, 17, cpt.getSnowControl().getSnowMonth());

        // snow_sea
        setControlFlags(ctrl, 18, cpt.getSnowControl().getSnowSeason());

        // snow_year
        setControlFlags(ctrl, 19, cpt.getSnowControl().getSnowYear());

        // snow_depth
        setControlFlags(ctrl, 20, cpt.getSnowControl().getSnowDepthAvg());

        // max_rh
        setControlFlags(ctrl, 21, cpt.getRelHumidityControl().getMaxRH());

        // min_rh
        setControlFlags(ctrl, 22, cpt.getRelHumidityControl().getMinRH());

        // mean_rh
        setControlFlags(ctrl, 23, cpt.getRelHumidityControl().getMeanRH());

        // heat
        setControlFlags(ctrl, 24, cpt.getDegreeDaysControl().getTotalHDD());

        // cool
        setControlFlags(ctrl, 25, cpt.getDegreeDaysControl().getTotalCDD());

        // avg_wind
        setControlFlags(ctrl, 26, cpt.getWindControl().getMeanWind());

        // max_wind
        setControlFlags(ctrl, 27, cpt.getWindControl().getMaxWind());

        // max_gust
        setControlFlags(ctrl, 28, cpt.getWindControl().getMaxGust());

        // result_wind
        setControlFlags(ctrl, 29, cpt.getWindControl().getResultWind());

        // poss_sun - start from second token
        if (ctrl.size() > 30) {
            boolean[] possSun = parseControlFlags(ctrl.get(30), 1);
            cpt.getSkycoverControl().setPossSunshine(possSun[0]);
        }

        // sky_cover - start from second token
        if (ctrl.size() > 31) {
            boolean[] skyCover = parseControlFlags(ctrl.get(31), 1);
            cpt.getSkycoverControl().setAvgSkycover(skyCover[0]);
        }

        // weather - start from second token
        if (ctrl.size() > 32) {
            boolean[] weather = parseControlFlags(ctrl.get(32), 1);
            cpt.getWeatherControl().setWeather(weather[0]);
        }

        // sun_rise/sun_set - start from first token
        if (ctrl.size() > 34) {
            boolean[] sun = parseControlFlags(ctrl.get(34), 0);
            cpt.getSunControl().setSunrise(sun[0]);
            cpt.getSunControl().setSunset(sun[1]);
        }

        // temperature record - start from first token
        if (ctrl.size() > 36) {
            boolean[] tempRec = parseControlFlags(ctrl.get(36), 0);
            cpt.getTempRecordControl().setMaxTempNorm(tempRec[0]);
            cpt.getTempRecordControl().setMinTempNorm(tempRec[1]);
            cpt.getTempRecordControl().setMaxTempRecord(tempRec[2]);
            cpt.getTempRecordControl().setMinTempRecord(tempRec[3]);
            cpt.getTempRecordControl().setMaxTempYear(tempRec[4]);
            cpt.getTempRecordControl().setMinTempYear(tempRec[5]);
        }

    }

    /**
     * Set the value of an ClimateProductFlags from a line of control flags.
     * 
     * @param ctrl
     *            Strings from a legacy control file (one line per string).
     * @param flagPos
     *            line number of the flags.
     * @param cptFlags
     *            ClimateProductFlags to be updated.
     */
    private void setControlFlags(java.util.List<String> ctrl, int flagPos,
            ClimateProductFlags cptFlags) {

        int len = ctrl.size();

        PeriodType ptyp = getPeriodType(ctrl);

        // Start from the second flag token - the first token is the name.
        int start = 1;

        if (len > flagPos) {
            setControlFlags(parseControlFlags(ctrl.get(flagPos), start),
                    cptFlags, ptyp);
        }
    }

    /**
     * Set the value of an ClimateProductFlags from an array of control flags
     * based on daily/period types.
     * 
     * @param ctrlFlags
     *            boolean[] - flags read from legacy daily control file.
     * @param cptFlags
     *            ClimateProductFlags to be updated.
     * @param ptyp
     *            PeriodType to determine if it is NWR or NWWS control.
     */
    private void setControlFlags(boolean[] ctrlFlags,
            ClimateProductFlags cptFlags, PeriodType ptyp) {

        /*
         * Daily control has 10 flags in each row, while period control has only
         * 8. The first seven flags are the same for both.
         */
        cptFlags.setMeasured(ctrlFlags[0]);
        cptFlags.setTimeOfMeasured(ctrlFlags[1]);
        cptFlags.setNorm(ctrlFlags[2]);
        cptFlags.setRecord(ctrlFlags[3]);
        cptFlags.setRecordYear(ctrlFlags[4]);
        cptFlags.setDeparture(ctrlFlags[5]);
        cptFlags.setLastYear(ctrlFlags[6]);

        // Difference between daily/period flags
        if (ptyp.isDaily()) {
            cptFlags.setTotalMonth(ctrlFlags[7]);
            cptFlags.setTotalSeason(ctrlFlags[8]);
            cptFlags.setTotalYear(ctrlFlags[9]);
        } else if (ptyp.isPeriod()) {
            cptFlags.setDateOfLast(ctrlFlags[7]);
        }

    }

    /**
     * Parse a string of control flags into booleans. For daily controls, "T"
     * means true and for period controls, "1" means true.
     * 
     * @param flagStr
     *            A string of flags such as
     *            "max_temp      T    F    T    T    T    F    F    F    F    F"
     *            "avg_temp      1    *    1    *    *    1    0    *"
     *            "     F        F        F        F        F        F"
     * @param start
     *            Starting position of flag tokens.
     * @return boolean[] Flags as boolean.
     */
    private boolean[] parseControlFlags(String flagStr, int start) {

        boolean[] flags = new boolean[10];
        for (int ii = 0; ii < 10; ii++) {
            flags[ii] = false;
        }

        String valueStr = flagStr.trim();
        String[] inFlags = valueStr.split("\\s+");
        for (int ii = start; ii < inFlags.length; ii++) {
            String aFlag = inFlags[ii].trim();
            /*
             * "T" means true in daily controls and "1" means true in period
             * controls.
             */
            if (aFlag.equals("T") || aFlag.equals("1")) {
                flags[ii - start] = true;
            }
        }

        return flags;
    }

    /**
     * Parse a period control file (83 lines) and set into a
     * ClimateProductControl.
     * 
     * @param ctrl
     *            Strings from a period control file (one line per string).
     * @param cpt
     *            The ClimateProductControl to be updated
     */
    private void parsePeriodControl(java.util.List<String> ctrl,
            ClimateProductControl cpt) {

        /*
         * Temperature - 15 elements.
         */
        // max_temp
        setControlFlags(ctrl, 10, cpt.getTempControl().getMaxTemp());

        // min_temp
        setControlFlags(ctrl, 11, cpt.getTempControl().getMinTemp());

        // avg_temp
        setControlFlags(ctrl, 12, cpt.getTempControl().getMeanTemp());

        // avg_max_temp
        setControlFlags(ctrl, 13, cpt.getTempControl().getMeanMaxTemp());

        // avg_min_temp
        setControlFlags(ctrl, 14, cpt.getTempControl().getMeanMinTemp());

        // max_GE_90
        setControlFlags(ctrl, 15, cpt.getTempControl().getMaxTempGE90());

        // max_LE_32
        setControlFlags(ctrl, 16, cpt.getTempControl().getMaxTempLE32());

        // max_GE_T1
        setControlFlags(ctrl, 17, cpt.getTempControl().getMaxTempGET1());

        // max_GE_T2
        setControlFlags(ctrl, 18, cpt.getTempControl().getMaxTempGET2());

        // max_LE_T3
        setControlFlags(ctrl, 19, cpt.getTempControl().getMaxTempLET3());

        // min_LE_32
        setControlFlags(ctrl, 20, cpt.getTempControl().getMinTempLE32());

        // min_LE_0
        setControlFlags(ctrl, 21, cpt.getTempControl().getMinTempLE0());

        // min_GE_T4
        setControlFlags(ctrl, 22, cpt.getTempControl().getMinTempGET4());

        // min_LE_T5
        setControlFlags(ctrl, 23, cpt.getTempControl().getMinTempLET5());

        // min_LE_T6
        setControlFlags(ctrl, 24, cpt.getTempControl().getMinTempLET6());

        /*
         * Precipitation - 11 elements
         */
        // precip_total
        setControlFlags(ctrl, 26, cpt.getPrecipControl().getPrecipTotal());

        // precip_min
        setControlFlags(ctrl, 27, cpt.getPrecipControl().getPrecipMin());

        // prec_GE_01
        setControlFlags(ctrl, 28, cpt.getPrecipControl().getPrecipGE01());

        // prec_GE_10
        setControlFlags(ctrl, 29, cpt.getPrecipControl().getPrecipGE10());

        // prec_GE_50
        setControlFlags(ctrl, 30, cpt.getPrecipControl().getPrecipGE50());

        // prec_GE_100
        setControlFlags(ctrl, 31, cpt.getPrecipControl().getPrecipGE100());

        // prec_GE_P1
        setControlFlags(ctrl, 32, cpt.getPrecipControl().getPrecipGEP1());

        // prec_GE_P2
        setControlFlags(ctrl, 33, cpt.getPrecipControl().getPrecipGEP2());

        // prec_24hr
        setControlFlags(ctrl, 34, cpt.getPrecipControl().getPrecip24HR());

        // prec_storm
        setControlFlags(ctrl, 35, cpt.getPrecipControl().getPrecipStormMax());

        // prec_avg
        setControlFlags(ctrl, 36, cpt.getPrecipControl().getPrecipAvg());

        /*
         * Snow - 11 elements.
         */
        // snow_total
        setControlFlags(ctrl, 38, cpt.getSnowControl().getSnowTotal());

        // snow_July1
        setControlFlags(ctrl, 39, cpt.getSnowControl().getSnowJuly1());

        // snow_any
        setControlFlags(ctrl, 40, cpt.getSnowControl().getSnowAny());

        // snow_GE_100
        setControlFlags(ctrl, 41, cpt.getSnowControl().getSnowGE100());

        // snow_GE_P1
        setControlFlags(ctrl, 42, cpt.getSnowControl().getSnowGEP1());

        // snow_24hr
        setControlFlags(ctrl, 43, cpt.getSnowControl().getSnow24hr());

        // snow_storm
        setControlFlags(ctrl, 44, cpt.getSnowControl().getSnowStormMax());

        // water_total
        setControlFlags(ctrl, 45, cpt.getSnowControl().getSnowWaterTotal());

        // water_July1
        setControlFlags(ctrl, 46, cpt.getSnowControl().getSnowWaterJuly1());

        // depth_max
        setControlFlags(ctrl, 47, cpt.getSnowControl().getSnowDepthMax());

        // depth_avg
        setControlFlags(ctrl, 48, cpt.getSnowControl().getSnowDepthAvg());

        /*
         * Degree Days - 6 elements
         */
        // heat
        setControlFlags(ctrl, 50, cpt.getDegreeDaysControl().getTotalHDD());

        // heat_July1
        setControlFlags(ctrl, 51, cpt.getDegreeDaysControl().getSeasonHDD());

        // cool
        setControlFlags(ctrl, 52, cpt.getDegreeDaysControl().getTotalCDD());

        // cool_July1
        setControlFlags(ctrl, 53, cpt.getDegreeDaysControl().getSeasonCDD());

        // early_freeze
        setControlFlags(ctrl, 54, cpt.getDegreeDaysControl().getEarlyFreeze());

        // late_freeze
        setControlFlags(ctrl, 55, cpt.getDegreeDaysControl().getLateFreeze());

        /*
         * Wind - 4 elements
         */
        // mean_wind
        setControlFlags(ctrl, 57, cpt.getWindControl().getMeanWind());

        // max_wind
        setControlFlags(ctrl, 58, cpt.getWindControl().getMaxWind());

        // max_gust
        setControlFlags(ctrl, 59, cpt.getWindControl().getMaxGust());

        // result_wind
        setControlFlags(ctrl, 60, cpt.getWindControl().getResultWind());

        /*
         * Sky/Weather - 21 elements
         */
        // avg_rh
        if (ctrl.size() > 61) {
            boolean[] flg = parseControlFlags(ctrl.get(61), 1);
            cpt.getRelHumidityControl().getAverageRH().setMeasured(flg[0]);
        }

        // sky_cover: poss_sun
        if (ctrl.size() > 62) {
            boolean[] flg = parseControlFlags(ctrl.get(62), 1);
            cpt.getSkycoverControl().setPossSunshine(flg[0]);
        }

        // sky_cover
        if (ctrl.size() > 63) {
            boolean[] flg = parseControlFlags(ctrl.get(63), 1);
            cpt.getSkycoverControl().setAvgSkycover(flg[0]);
        }

        // sky_cover: num_fair
        if (ctrl.size() > 64) {
            boolean[] flg = parseControlFlags(ctrl.get(64), 1);
            cpt.getSkycoverControl().setFairDays(flg[0]);
        }

        // sky_cover: num_pc
        if (ctrl.size() > 65) {
            boolean[] flg = parseControlFlags(ctrl.get(65), 1);
            cpt.getSkycoverControl().setPartlyCloudyDays(flg[0]);
        }

        // sky_cover: num_cloudy
        if (ctrl.size() > 66) {
            boolean[] flg = parseControlFlags(ctrl.get(66), 1);
            cpt.getSkycoverControl().setCloudyDays(flg[0]);
        }

        // num_TS: thunderstorm
        if (ctrl.size() > 67) {
            boolean[] flg = parseControlFlags(ctrl.get(67), 1);
            cpt.getWeatherControl().setThunderStorm(flg[0]);
        }

        // num_RASN: mixed precipitation
        if (ctrl.size() > 68) {
            boolean[] flg = parseControlFlags(ctrl.get(68), 1);
            cpt.getWeatherControl().setMixedPrecip(flg[0]);
        }

        // num_RRR: heavy rain
        if (ctrl.size() > 69) {
            boolean[] flg = parseControlFlags(ctrl.get(69), 1);
            cpt.getWeatherControl().setHeavyRain(flg[0]);
        }

        // num_RR: rain
        if (ctrl.size() > 70) {
            boolean[] flg = parseControlFlags(ctrl.get(70), 1);
            cpt.getWeatherControl().setRain(flg[0]);
        }

        // num_R: light rain
        if (ctrl.size() > 71) {
            boolean[] flg = parseControlFlags(ctrl.get(71), 1);
            cpt.getWeatherControl().setLightRain(flg[0]);
        }

        // num_ZRR: freezing rain
        if (ctrl.size() > 72) {
            boolean[] flg = parseControlFlags(ctrl.get(72), 1);
            cpt.getWeatherControl().setFreezingRain(flg[0]);
        }

        // num_ZR: light freezing rain
        if (ctrl.size() > 73) {
            boolean[] flg = parseControlFlags(ctrl.get(73), 1);
            cpt.getWeatherControl().setLightFreezingRain(flg[0]);
        }

        // num_A: hail
        if (ctrl.size() > 74) {
            boolean[] flg = parseControlFlags(ctrl.get(74), 1);
            cpt.getWeatherControl().setHail(flg[0]);
        }
        // num_SSS: heavy snow
        if (ctrl.size() > 75) {
            boolean[] flg = parseControlFlags(ctrl.get(75), 1);
            cpt.getWeatherControl().setHeavySnow(flg[0]);
        }

        // num_SS: snow
        if (ctrl.size() > 76) {
            boolean[] flg = parseControlFlags(ctrl.get(76), 1);
            cpt.getWeatherControl().setSnow(flg[0]);
        }

        // num_S: light snow
        if (ctrl.size() > 77) {
            boolean[] flg = parseControlFlags(ctrl.get(77), 1);
            cpt.getWeatherControl().setLightSnow(flg[0]);
        }

        // num_IP: ice pellet
        if (ctrl.size() > 78) {
            boolean[] flg = parseControlFlags(ctrl.get(78), 1);
            cpt.getWeatherControl().setIcePellet(flg[0]);
        }

        // num_F: fog
        if (ctrl.size() > 79) {
            boolean[] flg = parseControlFlags(ctrl.get(79), 1);
            cpt.getWeatherControl().setFog(flg[0]);
        }

        // num_Fquarter: heavy fog (vis<1/4 mi)
        if (ctrl.size() > 80) {
            boolean[] flg = parseControlFlags(ctrl.get(80), 1);
            cpt.getWeatherControl().setHeavyFog(flg[0]);
        }

        // num_H: haze
        if (ctrl.size() > 82) {
            boolean[] flg = parseControlFlags(ctrl.get(82), 1);
            cpt.getWeatherControl().setHaze(flg[0]);
        }

        // weather itself (not used by NWWS)
        cpt.getWeatherControl().setWeather(true);
    }

    /**
     * Utility method to get the period type (itype) defined in a control file.
     * 
     * @param ctrl
     *            List<String> - list of content in a control file
     * @return getPeriodType() PeriodType.
     */
    private PeriodType getPeriodType(java.util.List<String> ctrl) {
        return getPeriodType(getIType(ctrl));
    }

    /**
     * Utility method to get an "itype" defined in a control file.
     * 
     * @param ctrl
     *            List<String> - list of content in a control file
     * @return getIType() integer.
     */
    private int getIType(java.util.List<String> ctrl) {
        // itype is on the second line of the file.
        String typStr = ctrl.get(1);
        return parseInt(typStr, 0, typStr.length());
    }

    /**
     * Utility method to end a string when a null (0) character is found.
     * 
     * @param inStr
     *            String
     * @return getCleanString() String.
     */
    private String getCleanString(String inStr) {

        String outStr = "";
        if (inStr != null && inStr.length() > 0) {
            int stop = inStr.length();
            int firstNull = inStr.indexOf('\u0000');
            if (firstNull > 0) {
                stop = firstNull;
            }
            outStr = inStr.substring(0, stop);
        }

        return outStr;
    }

}