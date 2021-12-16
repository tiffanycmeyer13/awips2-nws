/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

import com.raytheon.edex.exception.DecoderException;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ListComputeDataRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.parser.AbstractAtcfParser;
import gov.noaa.nws.ocp.edex.plugin.atcf.parser.AtcfParserFactory;

/**
 * Request handler for ListComputeDataRequest
 *
 * Retrieve CARQ lines from com file, format data fields, write out to
 * <strmid>_prtcarq.txt, and return formatted string to requester.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Jun 15, 2020 79543      mporricelli  Initial creation
 * Aug 11, 2020 76541      mporricelli  Get directory defs from
 *                                      properties file
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
public class ListComputeDataRequestHandler
        implements IRequestHandler<ListComputeDataRequest> {

    private static final String CARQ = "CARQ";

    private static final String PARAM_HDR_STR = ("   Hour Latitude  Longitude  Max Wind  Direction  Speed\n")
            + ("          deg        deg        kts       deg      kts\n\n");

    private static final String FORMAT1 = "%7.1f%s%10.1f%s%9s%10s%9s";

    private static final String FORMAT2 = "   %-19s%s%5s%3s";

    private static final String FORMAT3 = "  %-13s%s%3s%3s";

    private static final String FORMAT4 = "  %-22s%s%5s%3s";

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ListComputeDataRequestHandler.class);

    @Override
    public String handleRequest(ListComputeDataRequest request) {

        AtcfEnvironmentConfig envConfig = AtcfConfigurationManager
                .getEnvConfig();

        String atcfStrmsDir = envConfig.getAtcfstrms();

        String atcfTmpDir = envConfig.getAtcftmp();

        Storm storm = request.getStorm();

        String stormid = storm.getStormId().toLowerCase();

        File comFile = new File(
                atcfStrmsDir + File.separator + stormid + ".com");

        if (!comFile.exists()) {
            statusHandler.handle(Priority.PROBLEM,
                    "Compute file " + comFile + " does not exist.");
            return "";
        }

        String prtCarqFile = atcfStrmsDir + File.separator + stormid
                + "_prtcarq.txt";
        File tmpCarqFile = null;

        File tmpDir = new File(atcfTmpDir);
        if (!tmpDir.isDirectory()) {
            if (!tmpDir.mkdir()) {
                statusHandler.handle(Priority.PROBLEM,
                        "Unable to create directory " + tmpDir);
            }
            return "";
        }

        try {
            tmpCarqFile = File.createTempFile(prtCarqFile, null, tmpDir);
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "List Compute: Unable to create temporary output file. ",
                    e);
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfh = new SimpleDateFormat("HH");

        String stormName = null;
        String past24hrStr = null;
        String past18hrStr = null;
        String past12hrStr = null;
        String past6hrStr = null;
        String cur0hrStr = null;
        String date = "";
        String stormDepth = null;
        int refHr = 0;
        int fcstHr = 0;
        float mslp = 0;
        float closedP = 0;
        float radClosedP = 0;
        float eyeSize = 0;
        float maxWindRad = 0;
        int wndRad = 0;
        int[] radWnd34 = new int[4];
        int[] radWnd50 = new int[4];
        int[] radWnd64 = new int[4];

        // Extract the data field values from the compute file's 'CARQ' lines
        BaseADeckRecord[] results;
        AbstractAtcfParser parser = AtcfParserFactory.getParser(AtcfDeckType.A);

        try {
            results = (BaseADeckRecord[]) parser.parse(comFile, storm);
        } catch (DecoderException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "List Compute: Parsing of " + comFile + " failed. ", e);
            return "";
        }

        for (BaseADeckRecord result : results) {
            boolean isCarq = result.getTechnique().trim().equals(CARQ);

            if (isCarq) {
                stormName = result.getStormName();
                date = sdf.format(result.getForecastDateTime());
                refHr = Integer.parseInt(
                        sdfh.format(result.getForecastDateTime()));
                fcstHr = result.getFcstHour();
                String outputStr = getOutputStr(result);

                switch (fcstHr) {
                case -24:
                    past24hrStr = outputStr;
                    break;
                case -18:
                    past18hrStr = outputStr;
                    break;
                case -12:
                    past12hrStr = outputStr;
                    break;
                case -6:
                    past6hrStr = outputStr;
                    break;
                case 0:
                    cur0hrStr = outputStr;
                    int[] quadwndrad = result.getWindRadii();
                    maxWindRad = result.getMaxWindRad();
                    eyeSize = result.getEyeSize();
                    stormDepth = result.getStormDepth();
                    mslp = result.getMslp();
                    closedP = result.getClosedP();
                    radClosedP = result.getRadClosedP();

                    wndRad = (int) result.getRadWind();

                    switch (wndRad) {
                    case 34:
                        radWnd34 = quadwndrad;
                        break;

                    case 50:
                        radWnd50 = quadwndrad;
                        break;

                    case 64:
                        radWnd64 = quadwndrad;
                        break;
                    default:
                        break;
                    }
                    break;
                default:
                    break;
                }
            }
        }

        // Format the CARQ data and write out to storm's prtcarq.txt file
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("   CURRENT CARQ (COMPUTE) DATA");
        sb.append("\n\n");
        sb.append("   STORM ID = " + stormid + "      STORM NAME =");
        sb.append(String.format("%11s", stormName));
        sb.append("\n\n");
        sb.append("   DATE     = " + date + "          HOUR = ");
        sb.append(String.format("%02d%s", refHr, "Z"));
        sb.append("\n\n\n\n");
        sb.append("   PAST STORM PARAMETERS");
        sb.append("\n\n");
        sb.append(PARAM_HDR_STR);
        sb.append(past24hrStr);
        sb.append(past18hrStr);
        sb.append(past12hrStr);
        sb.append(past6hrStr);
        sb.append("\n\n");
        sb.append("   CURRENT STORM PARAMETERS");
        sb.append("\n\n");
        sb.append(PARAM_HDR_STR);
        sb.append(cur0hrStr);
        sb.append("\n");
        sb.append(String.format(FORMAT2, "Radius of max wind", "=",
                (int) maxWindRad, "nm"));
        sb.append(String.format(FORMAT3, "Eye diameter", "=", (int) eyeSize,
                "nm"));
        sb.append("  System depth = " + String.format("%1s", stormDepth));
        sb.append("\n\n");
        sb.append(String.format(FORMAT2, "Central pressure", "=", (int) mslp,
                "mb"));
        sb.append("\n\n");
        sb.append(String.format(FORMAT2, "Outer pressure", "=", (int) closedP,
                "mb"));
        sb.append(String.format(FORMAT4, "Radius outer pressure", "=",
                (int) radClosedP, "nm"));
        sb.append("\n\n\n");
        sb.append("   Radius of quadrant winds in nautical miles");
        sb.append("\n\n");
        sb.append("                  NE       SE       SW       NW");
        sb.append("\n\n");

        sb.append(getRadWndFmtStr("64kts", radWnd64));
        sb.append(getRadWndFmtStr("50kts", radWnd50));
        sb.append(getRadWndFmtStr("34kts", radWnd34));

        sb.append("\n\n\n");
        sb.append("  Are you setting up the GFDL/HWRF models?\n");
        sb.append("  If YES, inform the SDM at 301-683-1500.\n\n");
        sb.append(
                "  NHC/WPC make sure there is no conflict with CPHC 808-973-5284.\n\n");
        sb.append(
                "  CPHC make sure there is no conflict with NHC/WPC 305-229-4419.\n");

        try (Writer fos = Files.newBufferedWriter(tmpCarqFile.toPath())) {
            fos.write(sb.toString());
        } catch (FileNotFoundException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "List Compute: Error opening " + tmpCarqFile
                            + " for writing, " + e);
            return "";
        } catch (IOException e1) {
            statusHandler.handle(Priority.PROBLEM,
                    "List Compute: Error writing to" + tmpCarqFile + ", " + e1);
            return "";
        }

        try {
            Files.move(tmpCarqFile.toPath(), Paths.get(prtCarqFile),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error moving " + tmpCarqFile + "to " + prtCarqFile, e);
            return "";
        }

        // Return formatted string for GUI display
        return sb.toString();
    }

    /**
     * Create output line formatted like:
     *
     * <pre>
     *  fcsthr lat         lon      wdmx      dir      spd
     *  e.g:
     *  -12   25.1N      69.1W       90       328       11
     * </pre>
     *
     * @param rec
     * @return formatted string
     */
    private String getOutputStr(BaseADeckRecord rec) {

        int fcstHr = rec.getFcstHour();
        float lat = rec.getClat();
        float lon = rec.getClon();
        float wndMx = rec.getWindMax();
        float wndDir = rec.getStormDrct();
        float wndSpd = rec.getStormSped();
        String fcstHrFmt = fcstHr == 0 ? "     %02d" : "    %03d";

        StringBuilder sbd = new StringBuilder();
        sbd.append(String.format(fcstHrFmt, fcstHr));
        sbd.append(String.format(FORMAT1, (Math.abs(lat)),
                (lat >= 0.0f ? "N" : "S"), (Math.abs(lon)),
                (lon >= 0.0f ? "E" : "W"), (int) wndMx,
                (wndSpd == 0 ? "" : (int) wndDir),
                (wndSpd == 0 ? "" : (int) wndSpd)));
        sbd.append("\n\n");

        return sbd.toString();
    }

    /**
     * Create output line formatted like:
     *
     * <pre>
     *
     *    64kts          20       20       10       15
     * </pre>
     *
     * @param wndRad
     * @param radWndVal
     * @return formatted radWndStr
     */
    private String getRadWndFmtStr(String wndRad, int[] radWndVal) {
        StringBuilder radWndStr = new StringBuilder(
                String.format("   %-8s", wndRad));
        for (int i = 0; i < 4; i++) {
            radWndStr.append(String.format("%9s", radWndVal[i]));
        }
        radWndStr.append("\n\n");
        return radWndStr.toString();
    }

}