/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.edex.psh.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.psh.MetarDataEntry;

/**
 * 
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2017            pwang       Initial creation
 * Sep 14, 2017 #37917     wpaintsil   Tweaked METAR_EXP regex. 
 *                                     Added some logging for when products aren't matched.
 * Oct 05, 2017 #37917     wpaintsil   Revise time range algorithm.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class MetarStormDataParser {

    /** Regular expression for metar/speci type, ICAO and the date */
    private static final Pattern METAR_SPECI_EXP = Pattern
            .compile("(METAR|SPECI)\\s+(\\p{Alnum}{4})\\s+(\\d{6})Z");

    /** Regular expression for metar type, ICAO and the date */
    private static final Pattern METAR_ONLY_EXP = Pattern
            .compile("METAR\\s+(\\p{Alnum}{4})\\s+(\\d{6})Z");

    // Regular expression for the wind group - Imperial
    public static final Pattern WIND_GROUP_EXP_KT = Pattern
            .compile("(\\d{3}|VRB)(\\d{2,3})((G)(\\d{2,3}))?KT");

    public static final Pattern WIND_GUST_KT = Pattern
            .compile("(\\d{3}|VRB)(\\d{2,3})(G)(\\d{2,3})KT");

    public static final Pattern WIND_SUST_KT = Pattern
            .compile("(\\d{3}|VRB)(\\d{2,3})KT");

    /** Regular expression for the correction notifier */
    public static final Pattern COR_EXP = Pattern.compile("\\b(COR)\\b");

    /** Regular expression for the correction notifier */
    public static final Pattern DTZ_EXP = Pattern.compile("\\b(\\d{6})Z\\b");

    /** Regular expression for the sea level pressure */
    public static final Pattern SEA_LEVEL_PRESS_EXP = Pattern
            .compile("\\bSLP(\\d{3}|NO)\\b");

    public static final Pattern PK_TAG_EXP = Pattern.compile("\\b(PK)\\b");

    public static final Pattern PK_WND_EXP = Pattern.compile("\\b(WND)\\b");

    private static final String NEW_LINE = "(\\r?\\n)|\\r";

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MetarStormDataParser.class);

    /**
     * Empty constructor
     */
    public MetarStormDataParser() {

    }

    /**
     * Parse the latest Metar products based on given period
     * 
     * @param metarProducts
     * @param station
     * @param lat
     * @param lon
     * @param period
     * @return
     */
    public MetarDataEntry parse(List<StdTextProduct> metarProducts,
            String station, float lat, float lon, int period) {
        MetarDataEntry osd = new MetarDataEntry();
        // osd.setCategory(PshDataCategory.METAR);
        osd.setSite(station);
        osd.setLat(lat);
        osd.setLon(lon);

        List<MetarTextLine> slpLines = new ArrayList<>();
        List<MetarTextLine> windLines = new ArrayList<>();

        int stop = periodToStopNumber(period);

        int startTime = -1;
        int startDay = -1;

        // each product
        for (StdTextProduct stp : metarProducts) {

            String[] lineArray = stp.getProduct().split(NEW_LINE);

            // each line of text product
            MetarTextLine mline = null;
            for (int i = 0; i < lineArray.length; i++) {

                if (METAR_SPECI_EXP.matcher(lineArray[i]).find()) {
                    mline = new MetarTextLine(lineArray[i]);

                    // Get the start time. Assumes the latest entry has the
                    // latest date-time.
                    if (startTime == -1 || startDay == -1) {
                        startTime = mline.getHhmm();
                        startDay = mline.getDay();
                    }
                } else if (mline != null) {
                    mline.mergeToOneLine(lineArray[i]);
                }
            }

            if (mline == null) {
                logger.warn("Could not parse Metar product:\n ["
                        + stp.getProduct() + "]");
                continue;
            }

            mline.determineContentType();

            // Add into list
            if (mline.isContainSLP()) {
                slpLines.add(mline);
            }
            if (mline.isContainWind()) {
                windLines.add(mline);
            }
        }

        // Sort the lists with hhmm, DESC
        Collections.sort(slpLines, new SortByTime());
        Collections.sort(windLines, new SortByTime());

        // parse only if a start time was found
        if (startTime != -1 && startDay != -1) {
            // Parse SLP
            parseSLP(osd, slpLines, stop, startDay, startTime);

            // Parse Wind
            parseWind(osd, windLines, stop, startDay, startTime);
        }

        return osd;
    }

    /**
     * parseSLP
     * 
     * @param osd
     * @param sortedSLPList
     * @param stop
     */
    private void parseSLP(MetarDataEntry osd,
            final List<MetarTextLine> sortedSLPList, int stop, int startDay,
            int startTime) {

        int length = sortedSLPList.size();
        float slpValue = 0;
        float slpMin = 9999;
        int slpTimeZ = 0;
        int slpDay = 0;

        int stopLine = 0;

        for (int i = 0; i < length; i++) {
            MetarTextLine mtl = sortedSLPList.get(i);
            String[] elems = mtl.getTextLine().split("\\s+");
            boolean updateSLP = false;

            // Extract the SLP value
            for (String e : elems) {
                if (MetarStormDataParser.SEA_LEVEL_PRESS_EXP.matcher(e)
                        .find()) {
                    slpValue = Integer.parseInt(e.trim().substring(3, 6));
                    if (slpValue < 500) {
                        slpValue = (slpValue + 10000) / 10;
                    } else {
                        slpValue = (slpValue + 9000) / 10;
                    }
                    updateSLP = true;
                }
            }

            // Update SLP Min
            if (slpValue <= slpMin && updateSLP) {
                slpMin = slpValue;
                slpTimeZ = mtl.getHhmm();
                slpDay = mtl.getDay();
                updateSLP = false;
            }

            /*
             * Stop searching after reaching an entry with a date and time more
             * than 24/48/72 hours after the date/time of the latest entry.
             */
            if (mtl.getHhmm() == startTime && mtl.getDay() != startDay) {
                stopLine++;
                if (stopLine >= stop) {
                    break;
                }
            }
        }

        // Set SLP data
        osd.setMinSeaLevelPres(String.format("%1$.1f", slpMin));
        osd.setMinSeaLevelPresTime(
                String.format("%1$02d/%2$04d", slpDay, slpTimeZ));

    }

    /**
     * parseWind
     * 
     * @param osd
     * @param sortedWindList
     * @param stop
     */
    private void parseWind(MetarDataEntry osd,
            final List<MetarTextLine> sortedWindList, int stop, int startDay,
            int startTime) {
        int length = sortedWindList.size();

        // Local variables
        int sustWindMax = 0;
        String sustWindDir = "";
        int sustWindDay = 0;
        int sustWindTime = 0;

        int gustWindMax = 0;
        String gustWindDir = "";
        int gustWindDay = 0;
        int gustWindTime = 0;

        int peakWindMax = 0;
        String peakWindDir = "";
        int peakWindDay = 0;
        int peakWindTime = 0;

        boolean gustWindFlag = false;
        boolean peakWindFlag = false;

        int stopLine = 0;

        // For each Wind line
        for (int i = 0; i < length; i++) {

            // Sustained wind
            String sustWind = "";
            int sustWindValue = 0;

            // Gust wind
            String gustWind = "";
            int gustWindValue = 0;

            // Gust wind
            String pkWindDir = "";
            int pkWindSpeed = 0;
            int pkWindDay = 0;
            int pkWindHHMM = 0;

            // Wind direction
            String windDirection = "";

            // Date and Time
            int windDay = 0;
            int windHHMM = 0;

            MetarTextLine mtl = sortedWindList.get(i);
            String[] elems = mtl.getTextLine().split("\\s+");

            // Extract the wind values
            for (int j = 0; j < elems.length; j++) {
                if (MetarStormDataParser.WIND_GUST_KT.matcher(elems[j])
                        .find()) {
                    // Wind with GUST dddddGddKT / ddddddGdddKT
                    String[] windElems = elems[j].split("G");
                    sustWind = windElems[0];
                    gustWind = windElems[1];

                    windDirection = sustWind.substring(0, 3);

                    // Sustaining wind value may be 2-3 digits
                    if (sustWind.length() <= 6) {
                        sustWind = windElems[0].substring(3);
                    }
                    // Gust wind value could be 2-3 digits
                    if (gustWind.length() == 4) {
                        // 2 digits value
                        gustWind = gustWind.substring(0, 2);
                    } else if (gustWind.length() == 5) {
                        // 3 digits value
                        gustWind = gustWind.substring(0, 3);
                    }

                    // Get day and hhmm for the Wind
                    windDay = mtl.getDay();
                    windHHMM = mtl.getHhmm();

                    gustWindFlag = true;

                } else if (MetarStormDataParser.WIND_SUST_KT.matcher(elems[j])
                        .find()) {
                    // Without gust dddddKT / ddddddKT
                    sustWind = elems[j];

                    windDirection = sustWind.substring(0, 3);
                    if (sustWind.length() == 7) {
                        sustWind = sustWind.substring(3, 5);
                    } else if (sustWind.length() == 8) {
                        sustWind = sustWind.substring(3, 6);
                    }

                    // Get day and hhmm for the Wind
                    windDay = mtl.getDay();
                    windHHMM = mtl.getHhmm();

                }

                // Check if contains PK WIND
                if (MetarStormDataParser.PK_TAG_EXP.matcher(elems[j]).find()) {
                    // PK should follow by WND
                    if ((j + 1) < elems.length
                            && MetarStormDataParser.PK_WND_EXP
                                    .matcher(elems[j + 1]).find()) {
                        // PK Wind Data element should be followed
                        if ((j + 2) < elems.length
                                && elems[j + 2].length() == 10) {
                            peakWindFlag = true;
                            pkWindDir = elems[j + 2].substring(0, 3);
                            pkWindSpeed = Integer
                                    .parseInt(elems[j + 2].substring(3, 5));
                            pkWindDay = mtl.getDay();
                            pkWindHHMM = Integer
                                    .parseInt(elems[j + 2].substring(6, 10));
                        } else if ((j + 2) < elems.length
                                && elems[j + 2].length() == 11) {
                            peakWindFlag = true;
                            pkWindDir = elems[j + 2].substring(0, 3);
                            pkWindSpeed = Integer
                                    .parseInt(elems[j + 2].substring(3, 6));
                            pkWindDay = mtl.getDay();
                            pkWindHHMM = Integer
                                    .parseInt(elems[j + 2].substring(7, 11));
                        }
                    }
                }

            }

            sustWindValue = Integer.parseInt(sustWind);
            if (gustWind.length() > 0) {
                gustWindValue = Integer.parseInt(gustWind);
            }

            // Update the max speed of Sustain Wind
            if (sustWindValue > sustWindMax) {
                sustWindMax = sustWindValue;
                sustWindDir = windDirection;
                sustWindDay = windDay;
                sustWindTime = windHHMM;
            }

            // Update the max speed of gust Wind
            if (gustWindFlag && gustWindValue > gustWindMax) {
                gustWindMax = gustWindValue;
                gustWindDir = windDirection;
                gustWindDay = windDay;
                gustWindTime = windHHMM;
            }

            // Update the max of peak wind
            if (peakWindFlag && pkWindSpeed > peakWindMax) {
                peakWindMax = pkWindSpeed;
                peakWindDir = pkWindDir;
                peakWindDay = pkWindDay;
                peakWindTime = pkWindHHMM;
            }

            /*
             * Stop searching after reaching an entry with a date and time more
             * than 24/48/72 hours after the date/time of the latest entry.
             */
            if (mtl.getHhmm() == startTime && mtl.getDay() != startDay) {
                stopLine++;
                if (stopLine >= stop) {
                    break;
                }
            }
        }

        // Set extracted Wind data
        osd.setSustWind(String.format("%1$s/%2$03d", sustWindDir, sustWindMax));
        osd.setSustWindTime(
                String.format("%1$02d/%2$04d", sustWindDay, sustWindTime));
        if (gustWindFlag && peakWindFlag) {
            // There are both Gust and peak Wind in the Wind Group
            if (gustWindMax > peakWindMax) {
                osd.setPeakWind(
                        String.format("%s/%2$03d", gustWindDir, gustWindMax));
                osd.setPeakWindTime(String.format("%1$02d/%2$04d", gustWindDay,
                        gustWindTime));
            } else {
                osd.setPeakWind(
                        String.format("%1$s/%2$03d", peakWindDir, peakWindMax));
                osd.setPeakWindTime(String.format("%1$02d/%2$04d", peakWindDay,
                        peakWindTime));
            }

        } else if (!gustWindFlag && peakWindFlag) {
            // Contains Peak Wind but not Gust wind
            osd.setPeakWind(
                    String.format("%1$s/%2$03d", peakWindDir, peakWindMax));
            osd.setPeakWindTime(
                    String.format("%1$02d/%2$04d", peakWindDay, peakWindTime));
        } else if (gustWindFlag && !peakWindFlag) {
            // Contains Gust but not Peak wind
            osd.setPeakWind(
                    String.format("%1$s/%2$03d", gustWindDir, gustWindMax));
            osd.setPeakWindTime(
                    String.format("%1$02d/%2$04d", gustWindDay, gustWindTime));
        }

    }

    /**
     * Gets a number representing the number of days corresponding to the number
     * of hours.
     * 
     * periodToStopNumber
     * 
     * @param period
     * @return
     */
    private int periodToStopNumber(int period) {
        int stop = 1;

        switch (period) {
        case 24:
            stop = 1;
            break;
        case 48:
            stop = 2;
            break;
        case 72:
            stop = 3;
            break;
        default:
            break;
        }

        return stop;

    }

}
