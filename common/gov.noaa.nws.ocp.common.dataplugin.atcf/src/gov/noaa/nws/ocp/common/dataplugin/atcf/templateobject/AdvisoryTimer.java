/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;

/**
 * This class determines the local time zone and basin for ATCF advisories, and
 * provide methods to build the dates and times to be used in advisories.
 *
 * <pre>
 *
 * Notes (from nhc_writeadv.f --- timer()(
 *   1. Forecast advisories (TCMs) issued at 3, 9, 15, 21 UTC.
 *       a. Date line in header is current UTC time:
 *          0300 UTC WED JUL 02 2014
 *       b. TC center time is current UTC day/hour (dd/hhmm):
 *          TROPICAL STORM CENTER LOCATED NEAR ... AT 02/0300Z
 *       c. Current and previous (tau = -3) center location times
 *          are given in UTC day/hour (dd/hhmm):
 *          REPEAT...CENTER LOCATED NEAR ... AT 02/0300Z
 *          AT 02/0000Z CENTER WAS LOCATED NEAR ...
 *       d. Forecast position times are given in UTC at:
 *          tau =       9,  21,  33,  45,  69,  93,  117 hours or
 *          tau = -3 + 12, +24, +36, +48, +72, +96, +120 hours:
 *            9: FORECAST VALID 02/1200Z 28.6N  79.3W
 *           21: FORECAST VALID 03/0000Z 29.7N  79.2W
 *           33: FORECAST VALID 03/1200Z 31.0N  78.7W
 *           45: FORECAST VALID 04/0000Z 32.8N  77.3W
 *           69: FORECAST VALID 05/0000Z 37.5N  72.0W
 *           93: OUTLOOK VALID 06/0000Z 43.0N  64.0W...POST-TROP/
 *                                                     EXTRATROP
 *          117: OUTLOOK VALID 07/0000Z 47.5N  57.0W...POST-TROP/
 *                                                     EXTRATROP
 *       e. Next advisory issuances are in UTC times at tau = 6:
 *          NEXT ADVISORY AT 02/0900Z
 *
 * 2. Public advisories (TCPs) issued at 3, 9, 15, 21 UTC but
 *       labeled in local time (11, 5, 11, 5 EDT; 8, 2, 8, 2 PDT).
 *       a. Date line in header is current local time:
 *          1100 PM EDT MON JUN 30 2014
 *       b. Summary date line has both local and UTC times:
 *          SUMMARY OF 1100 PM EDT...0300 UTC...INFORMATION
 *       c. Discussion date clause has both local and UTC times:
 *          At 1100 PM EDT, 0300 UTC,
 *       d. Next advisory issuances are in local times at
 *          tau = 2 or 3 (intermediate), 6 hours:
 *          Next intermediate advisory at 200 AM EDT.
 *          Next complete advisory at 500 AM EDT.
 *
 *       NHC issues intermediate advisories on 3-hourly or 2-hourly
 *       cycles, depending on the situation (controlled with a
 *       radio button in the Advisory Composition dialog). The
 *       timing of those cycles looks like:
 *       3-hourly cycle
 *          0300 UTC - full advisory
 *          0600 UTC - intermediate A
 *          0900 UTC - full advisory
 *          . . .
 *       2-hourly cycle (deprecated)
 *          0300 UTC - full advisory
 *          0500 UTC - intermediate A
 *          0700 UTC - intermediate B
 *          0900 UTC - full advisory
 *          . . .
 *
 *  3. Discussions (TCDs) issued at 3, 9, 15, 21 UTC but labeled
 *       in local time (11, 5, 11, 5 EDT; 8, 2, 8, 2 PDT).
 *       a. Date line in header is current local time:
 *          1100 AM EDT TUE JUL 01 2014
 *       b. Forecast position times are in UTC at
 *          tau = 0, 12, 24, 36, 48, 72, 96, 120 hours:
 *          INIT  01/1500Z 27.6N  79.3W   35 KT  40 MPH
 *           12H  02/0000Z 27.8N  79.4W   35 KT  40 MPH
 *           24H  02/1200Z 28.7N  79.6W   40 KT  45 MPH
 *           36H  03/0000Z 29.8N  79.5W   50 KT  60 MPH
 *           48H  03/1200Z 31.2N  78.9W   60 KT  70 MPH
 *           72H  04/1200Z 35.4N  75.2W   70 KT  80 MPH
 *           96H  05/1200Z 40.8N  67.3W   65 KT  75 MPH
 *          120H  06/1200Z 45.5N  59.5W   45 KT  50 MPH...POST-TROP
 *                                                       /EXTRATROP
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 26, 2021 86746      jwu         Initial creation.
 * Feb 26, 2021 88638      jwu         Use customized ForcastTime.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AdvisoryTimer {

    // Normal advisory issuance time from the current forecast time (TAU=0).
    protected static final int[] advTau = new int[] { 3, 6, 9 };

    // Normal forecast TAUs
    protected static final int[] marTau = new int[] { 12, 24, 36, 48, 60, 72,
            96, 120, 144, 168 };

    protected static final int[] pbTau = new int[] { 24, 36, 48, 72 };

    protected static final int[] wpbTau = new int[] { 0, 12, 24, 36, 48, 72, 96,
            120 };

    // Default advisory TAU for TCA
    protected static final int tcaFixedTau = 3;

    // Forecast TAUs for TCA
    protected static int[] tcaMarTau = new int[] { 0, 6, 12, 18, 24 };

    // Time format pattern.
    private static final DateTimeFormatter DTG_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMddHHmm z");

    // Input forecast track data.
    private Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData;

    private ForecastTrackRecord firstFcstRec;

    // Advisory time zone from GUI & stormId.adv
    private AdvisoryInfo advInfo;

    // Current forecast time
    private String dtg;

    // Flag to indicate checking for TAU 60.
    private boolean useTAU60;

    // Advisory time zone derived from basin and forecast track.
    private AdvisoryTimeZone timeZone;

    // Advisory basin in one character (A/C/P)
    private String basinDesignator;

    // Advisory basin in 2-character (AT/CP/EP)
    private String advBasin;

    // Initial time in UTC (TAU = 0)
    private ZonedDateTime initialTime;

    // Current TAU - default is 3. 4 to 8 for for special advisory.
    private int currentTau = 3;

    // Current time in UTC (TAU = 3 or the special TAU, HHMM)
    private ZonedDateTime currentTime;

    /**
     * Constructor
     */
    public AdvisoryTimer() {
    }

    /**
     * Constructor
     */
    public AdvisoryTimer(String dtg, AdvisoryInfo advInfo,
            Map<String, List<ForecastTrackRecord>> fcstTrackData,
            boolean useTAU60) {

        this.dtg = dtg;
        this.advInfo = advInfo;
        this.fcstTrackData = fcstTrackData;
        this.useTAU60 = useTAU60;

        this.firstFcstRec = fcstTrackData.get(dtg).get(0);

        buildTime();
    }

    /**
     * @return the basinDesignator
     */
    public String getBasinDesignator() {
        return basinDesignator;
    }

    /**
     * @param basinDesignator
     *            the basinDesignator to set
     */
    public void setBasinDesignator(String basinDesignator) {
        this.basinDesignator = basinDesignator;
    }

    /**
     * @return the advBasin
     */
    public String getAdvBasin() {
        return advBasin;
    }

    /**
     * @param advBasin
     *            the advBasin to set
     */
    public void setAdvBasin(String advBasin) {
        this.advBasin = advBasin;
    }

    /**
     * @return the timeZone
     */
    public AdvisoryTimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(AdvisoryTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Check if the daylight saving is on for TAU 0 time.
     *
     * @return boolean true/false
     */
    public boolean isDaylightSaving() {
        ZonedDateTime zdt = initialTime.plusHours(timeZone.getTimeOffset());
        return timeZone.getZoneId().getRules()
                .isDaylightSavings(zdt.toInstant());
    }

    /**
     * Check if the daylight saving is on for a given ZonedDateTime.
     *
     * @param zdt
     *            ZonedDateTime.
     * @return true/false
     */
    public boolean isDaylightSaving(ZonedDateTime zdt) {
        return timeZone.getZoneId().getRules()
                .isDaylightSavings(zdt.toInstant());
    }

    /**
     * Check if the daylight saving is on.
     *
     * @return boolean true/false
     */
    public static boolean isDaylightSaving(String dtgString, ZoneId zoneId) {

        String fullDtg = dtgString + "00 GMT";
        ZonedDateTime dt = ZonedDateTime.parse(fullDtg, DTG_FORMATTER);
        return zoneId.getRules()
                .isDaylightSavings(dt.toInstant());
    }

    /**
     * Get a short basin designator for advisory composition.
     *
     * From nhc_writeadv.f line 7498: Determine basin from initial (tau =
     * 0)/Best Track longitude
     *
     * @param basin
     *            storm's basin in forecast track
     * @param lon
     *            longitude (negative value means West)
     * @return A/P/C
     */
    public static String getBasinDesignator(String basin, float lon) {

        String bsn = "";

        if (lon < -140) {
            // Central Pacific basin
            bsn = "C";
        } else {
            if ("AL".equalsIgnoreCase(basin)) {
                // Atlantic basin
                bsn = "A";
            } else {
                // East Pacific basin
                bsn = "P";
            }
        }

        return bsn;
    }

    /**
     * Get a full basin for advisory composition - This may be different from
     * the storm's actual basin string in deck record.
     *
     * From nhc_writeadv.f line 7498: Determine basin from initial (tau =
     * 0)/Best Track longitude
     *
     * @param basin
     *            storm's basin in forecast track
     * @param lon
     *            longitude (negative value means West)
     * @return
     */
    public static String getAdvisoryBasin(String basin, float lon) {

        String bsn = "";

        if (lon < -140) {
            // Central Pacific basin
            bsn = "CP";
        } else {
            if ("AL".equalsIgnoreCase(basin)) {
                // Atlantic basin
                bsn = "AT";
            } else {
                // East Pacific basin
                bsn = "EP";
            }
        }

        return bsn;
    }

    /**
     * Get ForecastTime for a given TAU, in UTC time.
     *
     * @param tau
     *            Forecast hour
     *
     * @param specialAdv
     *            Flag to take special adv into account.
     */
    public ForecastTime getUTCForecastTime(int tau, boolean special) {
        return getForecastTime(tau, special, false);
    }

    /**
     * Get ForecastTime for a given TAU, in local time.
     *
     * @param tau
     *            Forecast hour
     *
     * @param speicalAdv
     *            Flag to take special adv into account.
     */
    public ForecastTime getLocalForecastTime(int tau, boolean special) {
        return getForecastTime(tau, special, true);
    }

    /*
     * Get ForecastTime for a given TAU.
     *
     * @param tau Forecast hour
     *
     * @param special Flag to take special adv into account.
     *
     * @param local Flag for UTC or local time zone.
     *
     * @return ForecastTime
     */
    private ForecastTime getForecastTime(int tau, boolean special,
            boolean local) {

        ZonedDateTime fTime;

        // Special TAU is 4 to 8.
        if (tau > 0 && tau < 9 && special) {
            fTime = currentTime;
        } else {
            fTime = initialTime.plusHours(tau);
        }

        ForecastTime fcstTime;
        if (!local) {
            // Forecast time in UTC
            fcstTime = buildUTCForecastTime(fTime, tau);
        } else {
            // Forecast time in advisory local time zone
            int timeOffset = timeZone.getTimeOffset();
            ZonedDateTime localTime = fTime.plusHours(timeOffset);
            fcstTime = buildLocalForecastTime(localTime, tau);
        }

        return fcstTime;
    }

    /*
     * Builds a UTC ForecastTime instance for a given ZonedDateTime.
     *
     * @param zdt ZonedDateTime
     *
     * @param tau Forecast TAU
     *
     * @return ForecastTime
     */
    private ForecastTime buildUTCForecastTime(ZonedDateTime zdt, int tau) {
        return buildForecastTime(zdt, "UTC", tau);
    }

    /*
     * Builds a local ForecastTime instance for a given ZonedDateTime.
     *
     * @param zdt ZonedDateTime
     *
     * @param tau Forecast TAU
     *
     * @return ForecastTime
     */
    private ForecastTime buildLocalForecastTime(ZonedDateTime zdt, int tau) {
        return buildForecastTime(zdt, timeZone.name(), tau);
    }

    /*
     * Builds a ForecastTime instance for a givenZonedDateTime.
     *
     * @param zdt ZonedDateTime
     *
     * @param tz Time zone string
     *
     * @param tau Forecast TAU
     *
     * @return ForecastTime
     */
    private ForecastTime buildForecastTime(ZonedDateTime zdt, String tz,
            int tau) {

        ForecastTime fcstTime = new ForecastTime();
        fcstTime.setTau(tau);
        fcstTime.setFcstHour(String.format("%3d", tau));
        int plusHr = tau - 3;
        if (plusHr < 100) {
            fcstTime.setPlusHour(String.format("%02d", plusHr));
        } else {
            fcstTime.setPlusHour(String.format("%3d", plusHr));
        }

        String ldtStr = zdt
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmEEEMMMa"));
        fcstTime.setTimeZone(tz);
        fcstTime.setYear(ldtStr.substring(0, 4));
        fcstTime.setMon(ldtStr.substring(4, 6));
        fcstTime.setDay(ldtStr.substring(6, 8));
        fcstTime.setHour2(ldtStr.substring(8, 10));
        fcstTime.setMinute(ldtStr.substring(10, 12));
        fcstTime.setWeekDay(ldtStr.substring(12, 15).toUpperCase());
        fcstTime.setMonth(ldtStr.substring(15, 18).toUpperCase());
        fcstTime.setHour(String.format("%d", zdt.getHour()));
        fcstTime.setAmPm(ldtStr.substring(18).toUpperCase());

        fcstTime.setDdHH(
                String.format("%s/%s", fcstTime.getDay(), fcstTime.getHour2()));
        fcstTime.setHhmmaz(String.format("%s%s %s %s", fcstTime.getHour(),
                fcstTime.getMinute(), fcstTime.getAmPm(),
                fcstTime.getTimeZone()));
        fcstTime.setYmdh(String.format("%s%s%s/%s", fcstTime.getYear(),
                fcstTime.getMon(), fcstTime.getDay(), fcstTime.getHour2()));

        /*
         * Advisory issue time
         *
         * For UTC time (TCM/PWS/TCA), i.e., "2100 UTC SAT MAY 16 2020"
         *
         * For local time (TCP/TCD), i.e., "500 PM EDT Sat May 16 2020"
         */
        if ("UTC".equals(tz.toUpperCase())) {
            fcstTime.setHhmm(String.format("%s%s", fcstTime.getHour2(),
                    fcstTime.getMinute()));
            fcstTime.setAdvTime(zdt
                    .format(DateTimeFormatter
                            .ofPattern("HHmm z EEE MMM dd yyyy"))
                    .replace("GMT", "UTC").toUpperCase());
        } else {
            fcstTime.setHhmm(String.format("%s%s", fcstTime.getHour(),
                    fcstTime.getMinute()));
            fcstTime.setAdvTime(zdt
                    .format(DateTimeFormatter
                            .ofPattern("Hmm a z EEE MMM dd yyyy"))
                    .replace("GMT", fcstTime.getTimeZone()));
        }

        return fcstTime;
    }

    /*
     * Determine time zone, basin, basin designator, initial and current time.
     *
     * Referred to nhc_writeadv.f subroutine timer().
     */
    private void buildTime() {

        // Initial UTC time at TAU 0;
        String fullDtg = dtg + "00 GMT";
        initialTime = ZonedDateTime.parse(fullDtg, DTG_FORMATTER);

        // Current UTC advisory time - adjusted for special advisory.
        currentTime = getCurrentAdvTime();

        // Determine basin from initial (tau = 0) forecast Track longitude
        String tmpBasin = firstFcstRec.getBasin();
        float bstLon = firstFcstRec.getClon();
        advBasin = getAdvisoryBasin(tmpBasin, bstLon);
        basinDesignator = getBasinDesignator(tmpBasin, bstLon);

        /*
         * Find local advisory time zone based on basin designator, first non-0
         * forecast's longitude (usually TAU=3), daylight saving, and a
         * longitude extracted from 5-day (120-hr) forecast used to determine
         * whether to switch to Hawaii time zone (HST).
         */

        // Find the first non-zero tau, usually ends at tau = 3 hours.
        int tzTau = 0;
        List<ForecastTrackRecord> tauData = fcstTrackData.get(dtg);
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {
            if (recs != null && !recs.isEmpty()) {
                int fcstHr = recs.get(0).getFcstHour();
                if (fcstHr > 0) {
                    tzTau = fcstHr;
                    tauData = recs;
                    break;
                }
            }
        }

        float tzLon = tauData.get(0).getClon();

        float lon5d = tzLon;
        ForecastTrackRecord lon5dRec = getLon5dRecord(fcstTrackData, useTAU60);
        if (lon5dRec != null) {
            lon5d = lon5dRec.getClon();
        }

        timeZone = AdvisoryTimeZone.getTimeZone(basinDesignator, tzLon,
                advInfo.isDaylightSaving(), lon5d);

    }

    /*
     * Extract longitude from 5-day (120-hr) forecast to determine whether to
     * switch to Hawaii time zone (HST).
     *
     * Notes: If 120-hr forecast not available, search for a previous forecast.
     * Also search for 60-hr forecast when it becomes operational.
     *
     * @param fcstTrackData
     *
     * @param checkTau60 Flag to check TAU 60
     *
     * @return ForecastTrackRecord record found for 120 day forecast.
     */
    private ForecastTrackRecord getLon5dRecord(
            Map<String, List<ForecastTrackRecord>> fcstTrackData,
            boolean checkTau60) {

        Map<Integer, ForecastTrackRecord> fcstTrackRecordMap = new HashMap<>();

        for (List<ForecastTrackRecord> frecs : fcstTrackData.values()) {
            if (frecs != null && !frecs.isEmpty()) {
                fcstTrackRecordMap.put(frecs.get(0).getFcstHour(),
                        frecs.get(0));
            }
        }

        // Start from TAU 120 and go backwards until tau 24.
        ForecastTrackRecord lon5dRecord = fcstTrackRecordMap.get(0);
        for (int tauValue = 120; tauValue >= 24; tauValue -= 24) {
            ForecastTrackRecord rec = fcstTrackRecordMap.get(tauValue);

            if (rec != null) {
                lon5dRecord = rec;
                break;
            }
        }

        // Check Tau 60 if asked.
        if (checkTau60) {
            ForecastTrackRecord rec60 = fcstTrackRecordMap.get(60);
            if (rec60 != null && lon5dRecord != null
                    && lon5dRecord.getFcstHour() < 60) {
                lon5dRecord = rec60;
            }
        }

        // If not found then try to get tau 12.
        if (lon5dRecord == null) {
            lon5dRecord = fcstTrackRecordMap.get(12);
        }

        // If not found, try to use tau 3.
        if (lon5dRecord == null) {
            lon5dRecord = fcstTrackRecordMap.get(3);
        }

        return lon5dRecord;
    }

    /*
     * Determine the current advisory time - adjust for special advisories (See
     * nhc_writeadv.f).
     *
     * @return ZonedDateTime Adjusted advisory time.
     */
    private ZonedDateTime getCurrentAdvTime() {

        /*
         * Increment initial time with specified hours and minutes to obtain a
         * new special time.
         */
        int iHour = currentTau; // Default.
        int iMin = 0; // Default.

        /*
         * Special advisory times starts from 3 hours (not included) later from
         * initial time. The interval is every 30 minutes and up to 9 entries (8
         * hours after the initial time) and the special TAU hour is rounded up
         * if there are minutes.
         */
        if (advInfo.isSpecialAdv()) {
            iHour = advInfo.getSpecialTAU();
            String spTime = advInfo.getSpecialAdvTime();
            iMin = getStringAsInt(spTime.substring(2, 4), false, true, 0);

            // Special TAU hour is rounded up when minutes > 0
            if (iMin > 0) {
                iHour--;
            }
        }

        // This is the way the time is adjusted in legacy.
        /*-
        int lHour = jHour - iHour;
        if (lHour <= 0) {
            lHour = 24 + lHour;
        }*/

        ZonedDateTime specialTime = initialTime.plusHours(iHour);

        if (iMin > 0) {
            specialTime = specialTime.plusMinutes(iMin);
        }

        return specialTime;
    }

    /*
     * Get an int from a String
     *
     * @param valString The string to parse
     *
     * @param reverse Flips value's sign if true
     *
     * @param useDefault Check to see if we use a passed in default value if
     * parsing fails
     *
     * @param defaultValue Default value if parsing fails
     *
     * @return the parsed value, or the invalid/default.
     */
    private static int getStringAsInt(String valString, boolean reverse,
            boolean useDefault, int defaultValue) {
        int val = 0;
        try {
            val = Integer.parseInt(valString);
            if (reverse) {
                val *= -1;
            }
        } catch (NumberFormatException e) {
            if (useDefault) {
                val = defaultValue;
            }
        }

        return val;
    }

}