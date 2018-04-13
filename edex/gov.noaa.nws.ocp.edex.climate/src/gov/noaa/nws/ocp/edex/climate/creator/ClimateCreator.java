/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.creator;

import java.util.Calendar;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateStationsSetupDAO;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;

/**
 * Migrated from c_create_climate.c
 * 
 * ClimateCreator
 * 
 * create_climate reads the observational data base or daily climate data base
 * and builds climatology for daily, monthly, seasonal or annual use. It outputs
 * the created climatology data base and temporary files that will be used by
 * format_climate.
 * 
 * From the AWIPS SMM Chapter 9 document:
 * 
 * create_climate The create_climate process stores observations for the
 * stations selected by the forecaster in the set_up_climate process. The
 * observations are derived from the Daily Summary Message (DSM) data contained
 * in the cli_asos_daily table, METAR data contained in the rpt table in the
 * hmdb database, and from the Supplementary Climate Data (SCD) stored in the
 * fss_report table. Parameters that are derived from observed variables, such
 * as heating and cooling degree days, precipitation and snowfall to date for
 * the month, season and year, are calculated by the display_climate process
 * after the forecaster has had the opportunity to review and edit the data as
 * necessary.
 *
 * The create_climate process obtains inputs as follows: The type of report to
 * be formatted via the control_am/im/pm and global_day files (for daily report)
 * and control_mon/sea/ann and global_day files for monthly/seasonal/annual
 * reports The setup information from the cli_sta_setup table The daily climate
 * normals from the day_climate_norm The monthly/seasonal/annual climate normals
 * from the mon_climate_norm.
 *
 * The create_climate process composes one listing, (data_am/im/pm file for
 * daily and data_mon/sea/ann file for monthly/seasonal/annual), containing each
 * station’s name and all of the element names for which the day’s observations
 * and climatological information will be inserted and another for yesterday’s
 * data (i.e., history file). The create_climate process also creates a file
 * info_am/im/pm (for daily) and info_mon/sea/ann (for monthly/seasonal/annual)
 * with sunrise and sunset times for each station (i.e., info_file).
 *
 * To View the Current Day’s create_climate Log On PX1 as user fxa or your
 * individual user account, TYPE: cd $LOG_DIR/<yyyymmdd>
 *
 * TYPE: ls -ltr create_climate*
 *
 * TYPE: Where <yyyymmdd> is today’s date
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 22, 2016            xzhang      Initial creation
 * 13 JUL 2016  20414      amoore      Cleaning up DAO's C-structure implementation.
 * 19 SEP 2016  21378      amoore      Major rewrite; previous code was still mostly in old
 *                                     format with no working functionality. Refactoring for
 *                                     use "manually" by service calls and "automatically"
 *                                     by cron job/scheduler.
 * 04 OCT 2016  20636      wpaintsil   Modify getPeriodData for new DAO implementation.
 * 26 OCT 2016  21378      amoore      Some cleanup and implementing more functions.
 * 28 NOV 2016  22930      astrakovsky Added comments explaining Julian day conversions.
 * 01 DEC 2016  20414      amoore      Cleanup + comments.
 * 06 DEC 2016  20414      amoore      Handle error for null Global values.
 * 19 DEC 2016  27015      amoore      Checking against missing date information should
 *                                     require all date data present if logic would need it.
 * 04 JAN 2017  22134      amoore      Fixed default values for sun data issue, as well as
 *                                     default values for building temperature data. Clean
 *                                     up logging.
 * 26 JAN 2017  27017      amoore      Clean up and use more precise calculation of time.
 * 22 FEB 2017  28609      amoore      Address TODOs. Comment fixes.
 * 10 MAR 2017  27420      amoore      Fix up logging.
 * 13 MAR 2017  27420      amoore      Create Climate auto should always assign a date.
 * 15 MAR 2017  30162      amoore      Create Climate buildSetupInfo should modify, not assign,
 *                                     the given date to use.
 * 17 MAR 2017  21099      wpaintsil   Move getDailyValidTime() to ClimateTime. 
 *                                     ClimateFormatter uses it too.
 * 21 MAR 2017  30166      amoore      Address TODOs.
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 *                                     Comment clarification for getting valid/execution datetime.
 * 12 APR 2017  30171      amoore      Fix bug with historical norm querying.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 16 MAY 2017  33104      amoore      Minor logging text adjustment.
 * 18 MAY 2017  33104      amoore      Station coordinates are in degrees.
 * 19 MAY 2017  33104      amoore      Auto-period generation bug from duplicate pointing.
 * 08 JUN 2017  33104      amoore      Touch up of humidity logic; unnecessary arrays.
 * 13 JUN 2017  35122      amoore      Touch up calculation logic for mostRecent runs.
 * 26 JUN 2017  33104      amoore      Fix faulty migration of Legacy branch logic.
 * 30 JUN 2017  35729      amoore      Move #determineWindow from ClimateCreator to Daily DAO.
 * 07 JUL 2017  33104      amoore      Split Daily and Period logic into separate classes.
 * 24 JUL 2017  33104      amoore      Use 24-hour time.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateCreator {

    public static final String LATEST_YEARLY_PARAM = "ann";

    public static final String LATEST_SEASONAL_PARAM = "sea";

    public static final String LATEST_MONTHLY_PARAM = "mon";

    public static final String LATEST_INTERMEDIATE_PARAM = "im";

    public static final String LATEST_EVENING_PARAM = "pm";

    public static final String LATEST_MORNING_PARAM = "am";

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateCreator.class);

    private final ClimateStationsSetupDAO climateStationsSetupDao = new ClimateStationsSetupDAO();

    /**
     * Constructor.
     */
    public ClimateCreator() {
    }

    /**
     * Create climate files. For an automatic run.
     * 
     * Rewritten from c_create_climate.c and read_globals.c.
     * 
     * @param periodType
     *            period type
     * @throws Exception
     */
    public ClimateRunData createClimate(PeriodType periodType)
            throws Exception {
        ClimateTime validTime = ClimateTime.getDailyValidTime(periodType,
                ClimateGlobalConfiguration.getGlobal());

        /*******************************************************************
         * Depending on the type of climatology report, determine the date
         * values.
         ********************************************************************/
        ClimateDate aDate = ClimateDate.getMissingClimateDate();

        List<Station> climateStations = climateStationsSetupDao
                .getMasterStations();

        buildSetUpInfo(periodType, climateStations, aDate, validTime);

        return createClimate(false, periodType, aDate, new ClimateDate(aDate),
                validTime);
    }

    /**
     * For daily requests.
     * 
     * @param isManualNonRecentRun
     * @param iPeriodType
     * @param aDate
     * @return
     * @throws Exception
     */
    public ClimateRunDailyData createClimate(boolean isManualNonRecentRun,
            PeriodType iPeriodType, ClimateDate aDate) throws Exception {
        /*********************************************************************
         * Get the all the climate stations from the master station table. The
         * master station table is configured by the set-up program. Initialize
         * the structure to missing before getting data from the master climate
         * station table.
         **********************************************************************/
        /*
         * holds list of stations
         */
        List<Station> climateStations = climateStationsSetupDao
                .getMasterStations();

        return new DailyClimateCreator().createDailyClimate(iPeriodType, aDate,
                ClimateTime.getDailyValidTime(iPeriodType,
                        ClimateGlobalConfiguration.getGlobal()),
                climateStations, !isManualNonRecentRun);
        /*
         * In Legacy, HM monitor would now be notified of current status.
         */
    }

    /**
     * For period requests.
     * 
     * @param isManualNonRecentRun
     * @param iPeriodType
     * @param beginDate
     * @param endDate
     * @return
     * @throws ClimateException
     */
    public ClimateRunPeriodData createClimate(boolean isManualNonRecentRun,
            PeriodType iPeriodType, ClimateDate beginDate, ClimateDate endDate)
                    throws ClimateException {

        /*********************************************************************
         * Get the all the climate stations from the master station table. The
         * master station table is configured by the set-up program. Initialize
         * the structure to missing before getting data from the master climate
         * station table.
         **********************************************************************/
        /*
         * holds list of stations
         */
        List<Station> climateStations = climateStationsSetupDao
                .getMasterStations();

        return new PeriodClimateCreator().createPeriodClimate(iPeriodType,
                beginDate, endDate, ClimateGlobalConfiguration.getGlobal(),
                climateStations, !isManualNonRecentRun);
        /*
         * In Legacy, HM monitor would now be notified of current status.
         */
    }

    /**
     * Create climate files.
     * 
     * @param isManualNonRecentRun
     *            true if this is being called for a manual run that is NOT the
     *            most recent full valid date(s) for the given period. False
     *            otherwise.
     * @param iPeriodType
     *            period type.
     * @param beginDate
     *            begin date for the period.
     * @param endDate
     *            end date for the period. For daily calls, input the same as
     *            beginDate.
     * @param validTime
     * @return
     * @throws Exception
     */
    public ClimateRunData createClimate(boolean isManualNonRecentRun,
            PeriodType iPeriodType, ClimateDate beginDate, ClimateDate endDate,
            ClimateTime validTime) throws Exception {

        /*********************************************************************
         * Get the all the climate stations from the master station table. The
         * master station table is configured by the set-up program. Initialize
         * the structure to missing before getting data from the master climate
         * station table.
         **********************************************************************/
        /*
         * holds list of stations
         */
        List<Station> climateStations = climateStationsSetupDao
                .getMasterStations();

        /*
         * daily climatology product
         */
        if (iPeriodType.isDaily()) {
            return new DailyClimateCreator().createDailyClimate(iPeriodType,
                    beginDate, validTime, climateStations,
                    !isManualNonRecentRun);

        } else {
            return new PeriodClimateCreator().createPeriodClimate(iPeriodType,
                    beginDate, endDate, ClimateGlobalConfiguration.getGlobal(),
                    climateStations, !isManualNonRecentRun);
        }
        /*
         * In Legacy, HM monitor would now be notified of current status.
         */
    }

    /**
     * converted from build_set_up_info.f
     *
     * <pre>
     *  June 1998     Jason P. Tuell        PRC/TDL
     *  June 1999     Jason P. Tuell        PRC/TDL
     *              - Modified the info_file structure to allow two
     *                  days worth of SR/SS data to be created
     *
     *  Purpose:  This routine builds set up information necessary to
     *            execute create_climate.  It also returns the sunrise
     *            and sunset for the stations in this climate summary
     *            and updates key parameters in the NWR and NWWS headers.
     *
     *
     * </pre>
     * 
     * @param itype
     * @param climateStations
     * @param aDate
     *            date to set
     * @param validTime
     *            time to set
     */
    private void buildSetUpInfo(PeriodType itype, List<Station> climateStations,
            ClimateDate aDate, ClimateTime validTime) {

        /*
         * Legacy documentation:
         * 
         * What are the types of things which are unique from run to run? Date,
         * valid_time, sunrise, sunset, etc We also need to create a routine
         * that updates the header file information, for such parts that change
         * from run to run.
         * 
         * 
         * Get the execution time and date. We'll use these, along with the type
         * of climate summary to automatically determine the date for which the
         * climate summary is valid.
         * 
         * Migration note:
         * 
         * Execution date was not used at all.
         */

        // Get the execution time
        Calendar xCal = TimeUtil.newCalendar();
        xCal.setTimeInMillis(TimeUtil.currentTimeMillis());

        /*
         * Legacy documentation:
         * 
         * Assumptions inherent in the automatic determination of dates I'll
         * explicitly declare these assumptions so that users and future
         * programmers understand where the limitations are buried when the
         * field complains.
         * 
         * 1. I assume that the morning climate summary will be run everyday
         * between 00 and 23 LST.
         * 
         * 2. I assume that the evening climate summary will be run everyday
         * between 13 and 23 LST.
         * 
         * If either the morning or evening climate summary is run outside of
         * these windows, it must be run manually which enables the users to
         * manually enter the date for the climate summary.
         * 
         * These are the rules. Ignore them at your expense. Abandon all hope,
         * ye who enter here. First determine the maximum and minimum offsets of
         * stations in this climate summary
         */
        int maxOff = Integer.MIN_VALUE;
        int minOff = Integer.MAX_VALUE;

        for (Station station : climateStations) {
            if (station.getNumOffUTC() > maxOff) {
                maxOff = station.getNumOffUTC();
            }
            if (station.getNumOffUTC() < minOff) {
                minOff = station.getNumOffUTC();
            }
        }

        switch (itype) {
        // Daily morning reports
        case MORN_NWWS:
        case MORN_RAD:
            aDate.setDateFromDate(ClimateDate.getLocalDate());

            if ((aDate.getDay() < 1) || (aDate.getDay() > 31)
                    || (aDate.getMon() < 1) || (aDate.getMon() > 12)) {

                int localExecutionHour = xCal.get(Calendar.HOUR_OF_DAY)
                        + maxOff;

                if (localExecutionHour < 0) {
                    localExecutionHour += TimeUtil.HOURS_PER_DAY;
                } else if (localExecutionHour >= TimeUtil.HOURS_PER_DAY) {
                    localExecutionHour -= TimeUtil.HOURS_PER_DAY;
                }

                long deltaSecondsToMidnight;

                if (maxOff > 0) {
                    deltaSecondsToMidnight = (localExecutionHour + 1L)
                            * TimeUtil.SECONDS_PER_HOUR;
                } else {
                    deltaSecondsToMidnight = (localExecutionHour - maxOff + 1L)
                            * TimeUtil.SECONDS_PER_HOUR;
                }

                long localSecondsToMidnight = xCal.get(Calendar.SECOND)
                        - deltaSecondsToMidnight;

                Calendar aCal = TimeUtil.newCalendar();
                aCal.setTimeInMillis(
                        localSecondsToMidnight * TimeUtil.MILLIS_PER_SECOND);

                aDate.setDateFromCalendar(aCal);
            } else {
                // Get previous day of year
                int jday = aDate.julday() - 1;

                // Set current day of year to previous day of year
                aDate.convertJulday(jday);
            }

            break;
        // Daily evening and intermediate reports
        // Use today's date for a_date
        // Calculate sunset for today and sunrise for tomorrow
        // Use valid_time as the valid time
        case EVEN_NWWS:
        case EVEN_RAD:
        case INTER_NWWS:
        case INTER_RAD:

            aDate.setDateFromDate(ClimateDate.getLocalDate());

            if ((aDate.getDay() < 1) || (aDate.getDay() > 31)
                    || (aDate.getMon() < 1) || (aDate.getMon() > 12)) {

                long localSecondsToNoon = xCal.get(Calendar.SECOND)
                        + (maxOff * TimeUtil.SECONDS_PER_HOUR);

                Calendar aCal = TimeUtil.newCalendar();
                aCal.setTimeInMillis(
                        localSecondsToNoon * TimeUtil.MILLIS_PER_SECOND);

                aDate.setDateFromCalendar(aCal);
            }

            break;
        // Period NWR and NWWS reports
        case MONTHLY_NWWS:
        case MONTHLY_RAD:
        case SEASONAL_RAD:
        case SEASONAL_NWWS:
        case ANNUAL_RAD:
        case ANNUAL_NWWS:
            aDate.setDateFromCalendar(xCal);
            break;

        default:
            // Error condition
            logger.error("Unhandled period type [" + itype + "]");
            break;
        }

    }
}
