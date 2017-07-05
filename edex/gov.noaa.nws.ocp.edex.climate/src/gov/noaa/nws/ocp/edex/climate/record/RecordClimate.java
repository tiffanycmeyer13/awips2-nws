/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.record;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.plugin.text.db.TextDB;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdSendRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.rer.RecordClimateRawData;
import gov.noaa.nws.ocp.common.dataplugin.climate.rer.StationInfo;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateProdSendRecordDAO;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;

/**
 * Migration of RecordClimate legacy adapt project. Provides functionality to
 * create text products notifying about record-breaking weather (ex. highest
 * temperature) for a station.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 NOV 2016  21100      amoore      Initial creation
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 05 APR 2017  21100      amoore      Address TODOs, cleanup, use Climate alarm.
 * 06 APR 2017  21100      amoore      Cleanup messaging.
 * 03 MAY 2017  33104      amoore      Use abstract map.
 * 03 MAY 2017  33533      amoore      Store products in new sent product table.
 * 11 MAY 2017  33104      amoore      Logging.
 * </pre>
 * 
 * @author amoore
 *
 */
public final class RecordClimate {

    // TODO put in common location
    public final static String EDEX = "EDEX";

    // TODO put in common location
    public final static String CATEGORY_INFO = "INFO";

    // TODO put in common location
    public static final String cpgEndpoint = "climateNotify";

    /**
     * Plugin ID for alerts.
     */
    public static final String PLUGIN_ID = "RecordClimate";

    /**
     * The default timezone to use.
     */
    private static final String DEFAULT_IFPS_SITE_TIMEZONE = "GMT";

    /**
     * The default site office name to use.
     */
    private static final String DEFAULT_IFPS_SITE_OFFICE_NAME = "National Weather Service";

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(RecordClimate.class);

    /**
     * Private constructor. This is a utility class.
     */
    private RecordClimate() {
    }

    /**
     * Run RecordClimate and create and store the RER products.
     * 
     * @param rawDatas
     *            record-breaking/tying data to report. In Legacy this data was
     *            created by Format Climate and stored in
     *            RecordClimateRawData.dat.
     * @param stationInfoMap
     *            Map of AFOS IDs to list of Station Info for
     *            record-breaking/tying stations. In Legacy this data was
     *            created by Format Climate and stored in
     *            RecordClimateStationInfo.dat.
     * @param yesterday
     *            True if record is for yesterday, false if for today.
     * @param globals
     *            globalDay properties
     * @param operational
     *            True if CAVE is in operational mode, false for test mode.
     */
    public static void generateAndStoreRER(List<RecordClimateRawData> rawDatas,
            Map<String, List<StationInfo>> stationInfoMap, boolean yesterday,
            ClimateGlobal globals, boolean operational) {
        List<RecordReport> recordReports = createRERproducts(rawDatas,
                stationInfoMap, yesterday, globals);
        storeRERs(recordReports, operational);
    }

    /**
     * Write record reports to the text database, and provide notification of
     * each record.
     * 
     * @param recordReports
     *            generated reports to store.
     * @param operational
     *            True if CAVE is in operational mode, false for test mode.
     */
    private static void storeRERs(List<RecordReport> recordReports,
            boolean operational) {
        TextDB textdb = new TextDB();

        ClimateProdSendRecordDAO sendRecordDAO = new ClimateProdSendRecordDAO();

        for (RecordReport report : recordReports) {
            logger.debug("Storing RER report with AFOS ID ["
                    + report.getAfosID() + "] and text body ["
                    + report.getReportText() + "].");

            long insertTime = textdb.writeProduct(report.getAfosID(),
                    report.getReportText(), operational, null);

            if (insertTime != Long.MIN_VALUE) {
                // store record of product to DB
                try {
                    ClimateProdSendRecord record = new ClimateProdSendRecord();
                    record.setProd_id(report.getAfosID());
                    record.setProd_type("RER");
                    record.setProd_text(report.getReportText());
                    record.setSend_time(new Timestamp(insertTime));
                    record.setUser_id("auto");

                    sendRecordDAO.insertSentClimateProdRecord(record);
                } catch (ClimateQueryException e) {
                    logger.error("Failed to track RER report with AFOS ID ["
                            + report.getAfosID() + "] and text body ["
                            + report.getReportText() + "].", e);
                }

                // send product alarm alert
                try {
                    StatusMessage sm = new StatusMessage();
                    sm.setPriority(Priority.EVENTA);
                    sm.setPlugin(PLUGIN_ID);
                    sm.setCategory(CATEGORY_INFO);
                    sm.setMessage("RER product [" + report.getAfosID()
                            + "] generated.");
                    sm.setMachineToCurrent();
                    sm.setSourceKey(EDEX);
                    sm.setDetails("Stored RER report with AFOS ID ["
                            + report.getAfosID() + "] and text body ["
                            + report.getReportText() + "]");
                    sm.setEventTime(new Date(insertTime));

                    EDEXUtil.getMessageProducer().sendAsync(cpgEndpoint, sm);
                } catch (Exception e) {
                    logger.error("Could not send message to ClimateView", e);
                }
            } else {
                String details = "Error detected saving product to textdb. AFOS ID: ["
                        + report.getAfosID() + "], text body: ["
                        + report.getReportText() + "]";
                EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                        "Climate Record Event Report", "EDEX", "WARNINGS",
                        "Failed to save RER product.", details, null);
                logger.error(details);
            }
        }
    }

    /**
     * Create the record-breaking/tying product reports. Example product text:
     * 
     * <pre>
     * RERDCA
     * 
     * Record Event Report
     * National Weather Service Baltimore Md/Washington Dc
     * 123 AM EDT Thu Sep 15 2016
     *
     *
     * ...RECORD HIGH TEMPERATURE SET AT WASHINGTON DC...
     *
     *
     * A record high temperature of 95 degrees was set at Reagan National
     * Airport near Washington DC yesterday. This breaks the old record of
     * 94 set in 1981, 1980 and 1915.
     * 
     * $$
     * </pre>
     * 
     * 
     * Legacy comments: For every afosid in the stationinfo file, there will be
     * a separate RER product formatted, notified on, and stored. We store these
     * internally in the list container recordReports_ as structures of PIL and
     * text. Note that we get the PIL and station for all CLI products in the
     * Climate setup, whether or not there was a record set in the station(s) in
     * that product, so we need to keep track of whether or not to store/notify
     * on a particular afosid by determining whether there is a record for one
     * of its stations in the RecordClimateRawData container.
     * 
     * @param rawDatas
     *            record-breaking/tying data to report.
     * @param stationInfoMap
     *            Map of AFOS IDs to list of Station Info for
     *            record-breaking/tying stations.
     * @param yesterday
     *            True if record is for yesterday, false if for today.
     * @param globals
     *            globalDay properties
     * @return generated reports to store.
     */
    private static List<RecordReport> createRERproducts(
            List<RecordClimateRawData> rawDatas,
            Map<String, List<StationInfo>> stationInfoMap, boolean yesterday,
            ClimateGlobal globals) {
        /*
         * Legacy comments: Get local time zone from env var
         * $IFPS_SITE_TIMEZONE, and WFO name from env var $IFPS_SITE_TIMEZONE,
         * as set in the environment by startRecordClimate.pl (OB2 version).
         * Vars are defined in /awips/adapt/ifps/localbin/ifps_ccc.env, where
         * ccc is the value of $ICWF_SITE.
         */
        String wfoSite = DEFAULT_IFPS_SITE_OFFICE_NAME;
        String timeZone = DEFAULT_IFPS_SITE_TIMEZONE;

        // We'll leave the site and time zone as initialized, unless found
        // in env.
        String wfoEnv = globals.getOfficeName();
        if (wfoEnv != null && !wfoEnv.isEmpty()) {
            wfoSite = wfoEnv;
        }
        String timeEnv = globals.getTimezone();
        if (timeEnv != null && !timeEnv.isEmpty()) {
            timeZone = timeEnv;
        }

        List<RecordReport> recordReports = new ArrayList<>();

        for (Entry<String, List<StationInfo>> entry : stationInfoMap
                .entrySet()) {
            // Get the list of station IDs and their names for this afosid
            String afosID = entry.getKey();

            logger.debug("Generating RER for AFOS ID: [" + afosID + "]");

            List<StationInfo> stations = entry.getValue();

            // Initialize: Do we have a record for a station in this product ID?
            boolean haveRecord = false;

            // Create a recordReport and fill in its "prologue"
            StringBuilder recordBuilder = new StringBuilder(
                    "Record Event Report\n");
            recordBuilder.append(wfoSite).append("\n");

            // get current datetime
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
            recordBuilder.append(getRERDateFormat().format(cal.getTime()));

            /*
             * Legacy comments:
             * 
             * Walk thru all the stations/record values and find those for
             * stations under this afosid. There could more than one record for
             * a given station in this afosid. There could also be records for
             * more than one station in the afosid. Append their climate record
             * descriptions to the recordReport text product.
             */
            for (RecordClimateRawData rawData : rawDatas) {
                logger.debug("Considering record: [" + rawData.getNewRecord()
                        + "], old record: [" + rawData.getOldRecord()
                        + "] old date: [" + rawData.getOldRecordDate()
                        + "], element: [" + rawData.getRecordElement()
                        + "] station ID: [" + rawData.getStationID()
                        + "], date: [" + rawData.getValidDate() + "]");

                for (StationInfo stationInfo : stations) {
                    if (stationInfo.getStationID()
                            .equals(rawData.getStationID())) {

                        haveRecord = true;

                        /*
                         * Legacy comments:
                         * 
                         * create record reports. if the format or text of the
                         * RER is what you want to change this is the function
                         * to modify.
                         * 
                         * NOTE: no line can be over 69 chars. A limitation set
                         * by the textWindow for teletype machines- this forces
                         * the derived requirement that all applications
                         * creating products worry about line length. textWindow
                         * could easily do this for all applications.
                         */
                        recordBuilder.append("\n\n");
                        recordBuilder.append("...RECORD ");

                        recordBuilder.append(rawData
                                .getRecordElementReportText().toUpperCase());
                        recordBuilder.append(" SET AT ");
                        recordBuilder.append(stationInfo.getStationNameTextual()
                                .toUpperCase());
                        recordBuilder.append("...\n\n");

                        recordBuilder.append("A record ");
                        recordBuilder.append(
                                rawData.getRecordElementReportShortenedText());
                        recordBuilder.append(" of ");
                        recordBuilder.append(rawData.getNewRecord());
                        recordBuilder.append(" was set at ");
                        recordBuilder
                                .append(stationInfo.getStationNameTextual());
                        recordBuilder.append(" ");
                        /*
                         * Legacy checked in the Climate tmp directory to see if
                         * AM files existed. If so, printed "yesterday". If not,
                         * printed "today". Since migrated Climate does not use
                         * tmp files and RecordClimate is no longer run
                         * asynchronously and independently of the rest of
                         * Climate, yesterday vs. today is a boolean flag to be
                         * passed in.
                         */
                        String day;
                        if (yesterday) {
                            day = "yesterday";
                        } else {
                            day = "today";
                        }
                        recordBuilder.append(day);
                        recordBuilder.append(".\n");
                        recordBuilder.append("This ");

                        String breaksOrTies;
                        if (rawData.getNewRecord()
                                .equals(rawData.getOldRecord())) {
                            breaksOrTies = "ties ";
                        } else {
                            breaksOrTies = "breaks ";
                        }
                        recordBuilder.append(breaksOrTies);
                        recordBuilder.append("the old record of ");
                        recordBuilder.append(rawData.getOldRecord());
                        recordBuilder.append(" set in ");
                        recordBuilder.append(rawData.getOldRecordDate());
                        recordBuilder.append(".\n");
                    }
                } // end stations
            } // end data

            /*
             * Legacy comments:
             * 
             * The next newly added segment of code will take care of adding
             * double dollar signs ($$) at the end of the RER product. Coder:
             * Mohammed Sikder, RSIS JULY, 2003
             * 
             * Legacy DR 16184 "Climate: RER adding extra $$" fixed. $$ was
             * being added after every full cycling of station was completed,
             * but should only be added after all loops are done and all data
             * (if any) was appended.
             */
            recordBuilder.append("\n\n$$");

            if (haveRecord) {
                /*
                 * Report is currently in mixed case. If Climate is configured
                 * to have products in mixed case, keep as is. If not, put
                 * report text in all-caps.
                 */
                String recordBody = recordBuilder.toString();
                if (ClimateGlobalConfiguration.getGlobal().isNoSmallLetters()) {
                    recordBody = recordBody.toUpperCase();
                }

                /*
                 * Legacy command: rer.afosid.replace(3, 3, "RER"); ie, take the
                 * AFOS ID text and, starting at index 3, remove 3 characters
                 * and insert "RER".
                 * 
                 * PIL is in the format of SSSRERMMM, where SSS is the first 3
                 * characters of the AFOS ID, RER is constant, and MMM is
                 * 7th-9th characters of the AFOS ID.
                 * 
                 * Formatter already places RER in the center of the AFOS ID.
                 * Simply verify that this has been done.
                 */
                String storedID;
                if (afosID.length() < 6
                        || !afosID.substring(3, 6).equals("RER")) {
                    // first 3 characters of AFOS ID
                    storedID = afosID.substring(0, 3)
                            // RER (constant)
                            + "RER"
                            // the 7th-9th characters of AFOS ID, as much as
                            // available (but will likely have all 3
                            // characters)
                            + (afosID.length() <= 6 ? ""
                                    : afosID.substring(6,
                                            Math.min(afosID.length(), 9)));
                    logger.warn("Changed AFOS ID: [" + afosID + "] to: ["
                            + storedID + "] to match RER PIL format.");
                } else {
                    storedID = afosID;
                }

                RecordReport report = new RecordReport(storedID, recordBody);
                recordReports.add(report);
            }
        } // end map

        return recordReports;
    }

    /**
     * Datetime format for Record Climate.
     * 
     * Legacy comments: For product text; e.g. 0237 PM EST THU JUL 16 2004
     * 
     * Fixed hour format for OB2, needs to be 12-h clock
     */
    private static SimpleDateFormat getRERDateFormat() {
        return new SimpleDateFormat("hhmm a z E MMM dd yyyy");
    }
}
