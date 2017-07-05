/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.creator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyDataMethod;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterBounds;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorDailyResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateCreatorDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateCreatorDAO.FSSReportResult;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDailyNormDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodNormDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateDAOUtils;
import gov.noaa.nws.ocp.edex.common.climate.util.MetarUtils;
import gov.noaa.nws.ocp.edex.common.climate.util.SunLib;

/**
 * Migrated from create_climate.c. Daily logic.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 JUL 2017  33104      amoore      Initial creation
 * 13 JUL 2017  33104      amoore      Fix index translation error for
 *                                     humidity hours.
 * 14 JUL 2017  33104      amoore      Add checks for missing temp/dew in
 *                                     humidity calculations. Move humidity
 *                                     calculations to within loop.
 * 24 JUL 2017  33104      amoore      Use 24-hour time.
 * 01 AUG 2017  36639      amoore      Discrepancies #62 and #63, allowing a
 *                                     missing average wind speed QC value and
 *                                     saving on potentially unnecessary calculations
 *                                     for average wind speed.
 * 08 AUG 2017  33104      amoore      Fix logic branches improperly migrated.
 * 23 AUG 2017  37318      amoore      Fix precip logic branches improperly migrated.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public final class DailyClimateCreator {
    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DailyClimateCreator.class);

    private static final String LOCAL_TIME_ZONE_PREFIX = "L";

    private static final String CHAMORRO_TIME_ZONE_PREFIX = "CH";

    private static final String SAMOAN_TIME_ZONE_PREFIX = "S";

    private static final String HAWAIIAN_TIME_ZONE_PREFIX = "H";

    private static final String ALASKAN_TIME_ZONE_PREFIX = "AK";

    private static final String PACIFIC_TIME_ZONE_PREFIX = "P";

    private static final String MOUNTAIN_TIME_ZONE_PREFIX = "M";

    private static final String CENTRAL_TIME_ZONE_PREFIX = "C";

    private static final String EASTERN_TIME_ZONE_PREFIX = "E";

    private static final String ATLANTIC_TIME_ZONE_PREFIX = "A";

    private static final String STANDARD_TIME_ZONE_SUFFIX = "ST";

    private static final String DAYLIGHT_SAVINGS_TIME_ZONE_SUFFIX = "DT";

    /**
     * SCD retrieval window. 10 minutes.
     */
    private static final int SCD_RETRIEVAL_WINDOW = 10;

    private static final int SCD_10_HOURS = SCD_RETRIEVAL_WINDOW
            * TimeUtil.MINUTES_PER_HOUR;

    private final ClimateDailyNormDAO climateDailyNormDao = new ClimateDailyNormDAO();

    private final ClimatePeriodNormDAO climatePeriodNormDAO = new ClimatePeriodNormDAO();

    private final DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

    private final ClimateCreatorDAO climateCreatorDAO = new ClimateCreatorDAO();

    /**
     * Constructor.
     * 
     */
    protected DailyClimateCreator() {
    }

    /**
     * 
     * @param periodType
     * @param beginDate
     * @param validTime
     * @param climateStations
     * @param cronOrManualMostRecent
     *            True if initiated as cronjob or user selected most recent
     *            date(s) option.
     * @return
     * @throws Exception
     */
    protected ClimateCreatorDailyResponse createDailyClimate(
            PeriodType periodType, ClimateDate beginDate, ClimateTime validTime,
            List<Station> climateStations, boolean cronOrManualMostRecent)
                    throws Exception {

        List<ClimateTime[]> sunrise = new ArrayList<ClimateTime[]>();
        List<ClimateTime[]> sunset = new ArrayList<ClimateTime[]>();

        /*
         * daily climate data structures
         */
        List<DailyClimateData> lastYear = new ArrayList<>();

        /*
         * daily historical record structures
         */
        /*
         * Today's climate data.
         */
        List<ClimateRecordDay> tClimate = new ArrayList<>();
        /*
         * Yesterday's climate data.
         */
        List<ClimateRecordDay> yClimate = new ArrayList<>();

        /*********************************************
         * Retrieve the daily historical climatology
         **********************************************/
        getDailyHisClimo(beginDate, climateStations, yClimate, tClimate);

        /*
         * go to the dailyClimate table and retrieved stored values
         */
        List<DailyClimateData> yesterday = new ArrayList<>();

        buildDailyObsClimo(beginDate, climateStations, periodType, validTime,
                yesterday);

        if (!cronOrManualMostRecent) {
            for (int i = 0; i < climateStations.size(); i++) {
                // Using snowGround from ASOS rather than from daily_climate.
                float asosSnowGround = yesterday.get(i).getSnowGround();
                dailyClimateDao.getLastYear(beginDate,
                        climateStations.get(i).getInformId(), yesterday.get(i));
                if (asosSnowGround != ParameterFormatClimate.MISSING_SNOW) {
                    yesterday.get(i).setSnowGround(asosSnowGround);
                }
            }
        }

        /************************************************************************
         * Now retrieve last year's daily observed climatology from the data
         * base
         *************************************************************************/

        getClimoFromLastYear(beginDate, climateStations, lastYear);

        /* establish the sunrise and sunset times */

        riseAndSet(beginDate, climateStations, sunrise, sunset, periodType);

        /*
         * Organize report data. Already organized by station in lists. TODO
         * organize above code to use the map from the start.
         */
        HashMap<Integer, ClimateDailyReportData> reportMap = new HashMap<>();
        for (int i = 0; i < climateStations.size(); i++) {
            reportMap.put(climateStations.get(i).getInformId(),
                    new ClimateDailyReportData(climateStations.get(i),
                            sunrise.get(i), sunset.get(i), yesterday.get(i),
                            lastYear.get(i), yClimate.get(i), tClimate.get(i)));
        }

        return new ClimateCreatorDailyResponse(periodType, beginDate,
                reportMap);
    }

    /**
     * Converted from get_snow_last.f.
     * 
     * Original description:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             snowfall for last year.  It determines the 
    *             monthly snowfall first, followed by the 
    *             seasonal and lastly the annual snowfall.
    * 
    *
    *   Variables
    *
    *      Input
    *        inform_id      - INFORMIX station id
    *        l_date         - derived TYPE that contains the date for this
    *                         climate summary for last year
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *        snow_season    - derived TYPE that contains the begin and end
    *                         dates for the snowfall season
    *        snow_year      - derived TYPE that contains the begin and end
    *                         dates for the snowfall year
    *
    *      Output
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *
    *      Local
    *        missing        - flag for missing data
    *
    *      Non-system routines used
    *        sum_snow      - * routine that sums the daily snowfall
    *                        for this station for the period between
    *                        a start and end date
     * </pre>
     * 
     * @param lastYearDate
     * @param informID
     * @param snowSeason
     * @param snowYear
     * @param data
     *            data to set snow information for.
     */
    private void getSnowLast(ClimateDate lastYearDate, int informID,
            ClimateDates snowSeason, ClimateDates snowYear,
            DailyClimateData data) {
        /*
         * Calculate the accumulated monthly snowfall. Treat the first of the
         * month as a special case that requires INFORMIX calls.
         */
        if (lastYearDate.getDay() == 1) {
            data.setSnowMonth(data.getSnowDay());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated snowfall for days other than the first of the
             * month.
             */
            data.setSnowMonth(
                    dailyClimateDao.sumSnow(
                            new ClimateDate(1, lastYearDate.getMon(),
                                    lastYearDate.getYear()),
                            lastYearDate, informID));
        }

        /*
         * Set up the beginning and ending dates for the retrieval of the
         * seasonal accumulated snowfall for days other than the first of the
         * season.
         */
        /*
         * Legacy did not check for first of the season despite documentation
         * and similarity to precip, heat, and cool.
         */
        if (lastYearDate.getDay() == snowSeason.getStart().getDay()
                && lastYearDate.getMon() == snowSeason.getStart().getMon()) {
            data.setSnowSeason(data.getSnowDay());
        } else {
            data.setSnowSeason(dailyClimateDao.sumSnow(snowSeason.getStart(),
                    lastYearDate, informID));
        }

        /*
         * Calculate the annual accumulated snowfall. Treat the first day of the
         * year as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == snowYear.getStart().getDay()
                && lastYearDate.getMon() == snowYear.getStart().getMon()) {
            data.setSnowYear(data.getSnowDay());
        } else {
            data.setSnowYear(dailyClimateDao.sumSnow(snowYear.getStart(),
                    lastYearDate, informID));
        }
    }

    /**
     * Converted from get_precip_last.f.
     * 
     * Original description:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             precipitation for last year.  It determines the 
    *             monthly precipitation first, followed by the 
    *             seasonal and lastly the annual precipitation.
    * 
    *
    *   Variables
    *
    *      Input
    *        inform_id      - INFORMIX station id
    *        l_date         - derived TYPE that contains the date for this
    *                         climate summary for last year
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *        precip_season  - derived TYPE that contains the begin and end
    *                         dates for the precipitation season
    *        precip_year    - derived TYPE that contains the begin and end
    *                         dates for theprecipitation year
    *
    *      Output
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *
    *      Local
    *        missing        - flag for missing data
    *
    *      Non-system routines used
    *        sum_precip     - * routine that sums the daily heating
    *                         degree days for this station for the
    *                         period between a start and end date
     * </pre>
     * 
     * @param lastYearDate
     * @param informID
     * @param precipSeason
     * @param precipYear
     * @param data
     *            data to set precip information for.
     */
    private void getPrecipLast(ClimateDate lastYearDate, int informID,
            ClimateDates precipSeason, ClimateDates precipYear,
            DailyClimateData data) {
        /*
         * Calculate the accumulated monthly precipitation. Treat the first of
         * the month as a special case that requires INFORMIX calls.
         */
        if (lastYearDate.getDay() == 1) {
            data.setPrecipMonth(data.getPrecip());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated precipitation for days other than the first
             * of the month.
             */
            data.setPrecipMonth(
                    dailyClimateDao.sumPrecip(
                            new ClimateDate(1, lastYearDate.getMon(),
                                    lastYearDate.getYear()),
                            lastYearDate, informID));
        }

        /*
         * Calculate the seasonal accumulated precipitation. Treat the first day
         * of the season as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == precipSeason.getStart().getDay()
                && lastYearDate.getMon() == precipSeason.getStart().getMon()) {
            data.setPrecipSeason(data.getPrecip());
        } else {
            data.setPrecipSeason(dailyClimateDao.sumPrecip(
                    precipSeason.getStart(), lastYearDate, informID));
        }

        /*
         * Set up the beginning and ending dates for the retrieval of the annual
         * accumulated precipitation for days other than the first of the year.
         */
        /*
         * Legacy did not check for first of the year despite documentation and
         * similarity to snow, heating, and cooling methods.
         */
        if (lastYearDate.getDay() == precipYear.getStart().getDay()
                && lastYearDate.getMon() == precipYear.getStart().getMon()) {
            data.setPrecipYear(data.getPrecip());
        } else {
            data.setPrecipYear(dailyClimateDao.sumPrecip(precipYear.getStart(),
                    lastYearDate, informID));
        }
    }

    /**
     * Converted from get_cool_last.f.
     * 
     * Original description:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             cooling degree days for last year.  It determines the 
    *             monthly accumulated cooling degree days first, followed
    *             by the seasonal and lastly the annual cooling degree days.
    * 
    *
    *   Variables
    *
    *      Input
    *        cool_season    - derived TYPE that contains the begin and end
    *                         dates for the cooling degree day season
    *        cool_year      - derived TYPE that contains the begin and end
    *                         dates for the cooling degree day year
    *        l_date         - derived TYPE that contains the date for this
    *                         climate summary for last year
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *        inform_id      - INFORMIX station id
    *
    *      Output
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *
    *      Local
    *
    *      Non-system routines used
    *        sum_cool_degree_days  - * routine that sums the daily cooling
    *                                degree days for this station for the
    *                                period between a start and end date
     * </pre>
     * 
     * @param lastYearDate
     * @param informID
     * @param coolSeason
     * @param coolYear
     * @param data
     *            data to set cooling degree days information for.
     */
    private void getCoolLast(ClimateDate lastYearDate, int informID,
            ClimateDates coolSeason, ClimateDates coolYear,
            DailyClimateData data) {
        /*
         * Calculate the accumulated monthly cooling degree days. Treat the
         * first of the month as a special case that requires INFORMIX calls.
         */
        if (lastYearDate.getDay() == 1) {
            data.setNumCoolMonth(data.getNumCool());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the month.
             */
            data.setNumCoolMonth(
                    dailyClimateDao.sumCoolDegreeDays(
                            new ClimateDate(1, lastYearDate.getMon(),
                                    lastYearDate.getYear()),
                            lastYearDate, informID));
        }

        /*
         * Calculate the seasonal accumulated cooling degree days. Treat the
         * first day of the season as a special case that requires no INFORMIX
         * calls.
         */
        if (lastYearDate.getDay() == coolSeason.getStart().getDay()
                && lastYearDate.getMon() == coolSeason.getStart().getMon()) {
            data.setNumCoolSeason(data.getNumCool());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the season.
             */
            data.setNumCoolSeason(dailyClimateDao.sumCoolDegreeDays(
                    coolSeason.getStart(), lastYearDate, informID));
        }

        /*
         * Calculate the annual accumulated cooling degree days. Treat the first
         * day of the season as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == coolYear.getStart().getDay()
                && lastYearDate.getMon() == coolYear.getStart().getMon()) {
            data.setNumCoolYear(data.getNumCool());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the year.
             */
            data.setNumCoolYear(dailyClimateDao.sumCoolDegreeDays(
                    coolYear.getStart(), lastYearDate, informID));
        }
    }

    /**
     * Converted from get_heat_last.f.
     * 
     * Original description:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             heating degree days for last year.  It determines the 
    *             monthly accumulated heating degree days first, followed
    *             by the seasonal and lastly the annual heating degree days.
    * 
    *
    *   Variables
    *
    *      Input
    *        heat_season    - derived TYPE that contains the begin and end
    *                         dates for the heating degree day season
    *        heat_year      - derived TYPE that contains the begin and end
    *                         dates for the heating degree day year
    *        l_date         - derived TYPE that contains the date for this
    *                         climate summary for last year
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *        inform_id      - INFORMIX station id
    *
    *      Output
    *        last           - derived TYPE that holds the daily climate
    *                         data for this station for last year
    *
    *      Local
    *        missing        - flag for missing data
    *
    *      Non-system routines used
    *        sum_heat_degree_days  - * routine that sums the daily heating
    *                                degree days for this station for the
    *                                period between a start and end date
     * </pre>
     * 
     * @param lastYearDate
     * @param informID
     * @param heatSeason
     * @param heatYear
     * @param data
     *            data to set heating degree days information for.
     */
    private void getHeatLast(ClimateDate lastYearDate, int informID,
            ClimateDates heatSeason, ClimateDates heatYear,
            DailyClimateData data) {
        /*
         * Calculate the accumulated monthly heating degree days. Treat the
         * first of the month as a special case that requires INFORMIX calls.
         */
        if (lastYearDate.getDay() == 1) {
            data.setNumHeatMonth(data.getNumHeat());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated heating degree days for days other than the
             * first of the month.
             */
            data.setNumHeatMonth(
                    dailyClimateDao.sumHeatDegreeDays(
                            new ClimateDate(1, lastYearDate.getMon(),
                                    lastYearDate.getYear()),
                            lastYearDate, informID));
        }

        /*
         * Calculate the seasonal accumulated heating degree days. Treat the
         * first day of the season as a special case that requires no INFORMIX
         * calls.
         */
        if (lastYearDate.getDay() == heatSeason.getStart().getDay()
                && lastYearDate.getMon() == heatSeason.getStart().getMon()) {
            data.setNumHeatSeason(data.getNumHeat());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated heating degree days for days other than the
             * first of the season.
             */
            data.setNumHeatSeason(dailyClimateDao.sumHeatDegreeDays(
                    heatSeason.getStart(), lastYearDate, informID));
        }

        /*
         * Calculate the annual accumulated heating degree days. Treat the first
         * day of the season as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == heatYear.getStart().getDay()
                && lastYearDate.getMon() == heatYear.getStart().getMon()) {
            data.setNumHeatYear(data.getNumHeat());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated heating degree days for days other than the
             * first of the year.
             */
            data.setNumHeatYear(dailyClimateDao.sumHeatDegreeDays(
                    heatYear.getStart(), lastYearDate, informID));
        }
    }

    /**
     * Migrated from build_daily_obs_sun.f
     * 
     * <pre>
     *   August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine calculates the percent sunshine from
    *             the observed # of minutes of sunshine and the
    *             the elapsed time between sunrise and sunset.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date           - date for this cclimate report
    *        climate_stations - derived TYPE containing station info
    *
    *      Output
    *        percent_sun      - percentage of sunshine
    *
    *      Local
    *        minutes_of_daylight - # of minutes of daylight (i.e., time
    *                              between sunrise and sunset
    *        minutes_of_sun      - # of minutes of sunshine ("98" group)
    *                              reported by the station
    *
    *      Non-system routines used
    *        get_minutes_of_sun - gets the number of minutes of sunshine for
    *                             a given station and date
    *
    *      Non-system functions used
    *        daylight         - calculates the minutes between sunrise and sunset
     * 
     * </pre>
     * 
     * @param aDate
     * @param station
     * @param dailyClimateData
     * @throws ClimateQueryException
     */
    private void buildDailyObsSun(ClimateDate aDate, Station station,
            DailyClimateData dailyClimateData) throws ClimateQueryException {
        int minSun = dailyClimateData.getMinutesSun();
        int qcMin = dailyClimateData.getDataMethods().getMinSunQc();
        int qcSun = dailyClimateData.getDataMethods().getPossSunQc();

        int minutesOfSun = ParameterFormatClimate.MISSING;

        ClimateTime sunset = ClimateTime.getMissingClimateTime();
        ClimateTime sunrise = ClimateTime.getMissingClimateTime();

        int percentSun = ParameterFormatClimate.MISSING;

        // Calculate SR/SS for a_date and this station
        double dlat = station.getDlat();
        double dlon = station.getDlon();
        int informId = station.getInformId();
        // Call the library routine that calculates sunrise and sunset
        // Note that sunrise and sunset are returned as fractional days
        // offset from the input date. isun =1 for sunrise, isun=0 for sunset
        int numOffUTC = station.getNumOffUTC();
        SunLib.setSun(aDate, dlat, dlon, numOffUTC, sunset);

        SunLib.riseSun(aDate, dlat, dlon, numOffUTC, sunrise);

        // Build the time and date for which the sunshine is reported
        // We will start with 0800 UTC. If that time isn't available,
        // we will try 12Z and then quit. The FMH says that the
        // duration of sunshine reported at 0800 UTC is for the day
        // before, hence the need to add one to the valid date.

        // Get next day of year
        int iday = aDate.julday() + 1;

        ClimateDate nowDate = ClimateDate.getMissingClimateDate();
        nowDate.setYear(aDate.getYear());

        // Set nowDate to next day of year
        nowDate.convertJulday(iday);

        ClimateTime thisTime = new ClimateTime(8);

        // The minutes of sunshine is reported in the 0800 UTC report
        // for the previous day. If the station isn't open at 0800
        // UTC, the minutes of sunshine is reported the first six
        // hourly report after the station opens.

        // Get the # of minutes of sunshine for this station

        if (qcMin != QCValues.MIN_SUN_FROM_DSM) {
            minutesOfSun = getMinutesOfSun(nowDate, thisTime, informId);

            if (minutesOfSun != ParameterFormatClimate.MISSING) {
                minSun = minutesOfSun;
                qcMin = 2;
            }
        }
        // If the number of minutes of sunshine isn't missing,
        // (1) determine the # of minutes of possible sunshine
        // from the elapsed time between sunrise and sunset,
        // and (2) take the ratio of the two

        if ((minSun != ParameterFormatClimate.MISSING)
                && (qcSun != QCValues.POSS_SUN_FROM_DSM)) {
            int minutesOfDaylight = daylight(sunrise, sunset);

            if (minutesOfDaylight != ParameterFormatClimate.MISSING) {

                percentSun = ClimateUtilities.nint(
                        (((double) minutesOfSun) / minutesOfDaylight) * 100);
                qcSun = 2;
            }

        }

        // return
        dailyClimateData.setMinutesSun(minSun);
        dailyClimateData.setPercentPossSun(percentSun);
        dailyClimateData.getDataMethods().setMinSunQc(qcMin);
        dailyClimateData.getDataMethods().setPossSunQc(qcSun);
    }

    /**
     * Migrated from daylight.f
     * 
     * <pre>
     *   August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine calculates the number of minutes between
    *             sunrise and sunset.  The number of minutes between 
    *             sunrise and sunset is calculated only if both are not
    *             missing.
    *              
    *   Variables
    *
    *      Input
    *        sunrise        - derived TYPE that contains the time of sunrise 
    *        sunset         - derived TYPE that contains the time of sunset 
    *
    *      Output
    *        daylight       - # of minutes between sunrise and sunset
    *
    *      Local
    *        missing        - flag for missing
    *        num_sunrise    - number of minutes from midnight for sunrise
    *        num_sumset     - number of minutes from midnight for sunset
     * </pre>
     * 
     * @param sunrise
     * @param sunset
     * @return
     */
    private int daylight(ClimateTime sunrise, ClimateTime sunset) {
        /*
         * Legacy documentation:
         * 
         * Calculate the number of minutes between sunrise and sunset if they
         * are not missing. Do this by first calculating the number of minutes
         * from midnight for each variable and then taking the difference.
         */

        int oDaylight = ParameterFormatClimate.MISSING;
        if (!sunrise.isMissingHourMin() && !sunset.isMissingHourMin()) {
            // this calculation depends on 24-hour format
            int numSunrise = sunrise.getHour() * 60 + sunrise.getMin();
            int numSunset = sunset.getHour() * 60 + sunset.getMin();

            oDaylight = numSunset - numSunrise;
        }

        return oDaylight;
    }

    /**
     * Migrated from get_minutes_of_sun.c
     * 
     * <pre>
     *  void int get_minutes_of_sun (   climate_date    now_date,
    *                      climate_time    now_time,
    *                      long        station_id,            
    *                               )
    *
    *   Jason Tuell        PRC/TDL             HP 9000/7xx
    *   Dan Zipper                 PRC/TDL
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *  This function retrieves the minutes of sunshine for a given station
    *      and period of time.  This is reported in the "98" group in the 
    *      additive data in the METAR code.  The minutes of sunshine is reported
    *      at 0800 UTC for the previous day.  If the station is closed at
    *      0800 UTC, it will be reported in the first six hourly group after
    *      the station reopens.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
    *      this_date           - the date for climate retrieval
    *      now_time            - the current time including the hour for which metar retrieval is used
    *      station_id          - station id of type int for which this function
    *                is called
    *
    *    Output
    *      minutes_of_sunshine - The function get_minutes_of_sunshine will return an integer minutes_of_sunshine.
    *
    *    Local
    *    char
    *      max_db_dqd          - Array of data quality descriptor returned by metar retrieval functions
    *      nominal_dtime       - A character string which holds the date and nominal hour in a
    *                            "yyyy-dd-mm hh" format.
    *    float 
    *
    *    int
    *      db_status           - The success/failure code returned by dbUtils and Informix functions.
    *
    *      max_db_status       - Integer array which holds the Informix SQL-code error checks. (See below)
    * 
    *  POSSIBLE STATUS VALUES
    *  ======================
    *
    *    STATUS_OK             The desired value was successfully found and
    *                            returned.
    *    STATUS_FAILURE        The desired value was not found.
    *    CURSOR_OPEN_ERROR     Informix encountered trouble declaring and/or
    *                            opening a "cursor".
    *    NO_HITS               No rows satisfied the "condition".
    *    MULTIPLE_HITS         More than one row satisfied the "condition".
    *    SELECT_ERROR          An Informix SELECT failed.
    *    STATUS_BUG            An undiagnosed problem occurred; the function
    *                            therefore aborted.
    *    (these are included from STATUS.h)  
    *
    *  MODIFICATION HISTORY
    *  ====================
    *    3/21/01  Doug Murphy            cleaned out unnecessary code
    *    2/06/03  Bob Morris             Replace call to nominal_time() with
    *                                    convert_ticks_2_string()
     * </pre>
     * 
     * @param nowDate
     * @param thisTime
     * @param informId
     * @return minutes of sunshine, or the missing value.
     * @throws ClimateQueryException
     */
    private int getMinutesOfSun(ClimateDate nowDate, ClimateTime thisTime,
            int informId) throws ClimateQueryException {
        Calendar cal = nowDate.getCalendarFromClimateDate();
        cal.set(Calendar.HOUR_OF_DAY, thisTime.getHour());
        cal.set(Calendar.MINUTE, thisTime.getMin());

        /* Now retrieve the minutes of sunshine reported for this station */
        String datetime = ClimateDate.getFullDateTimeFormat()
                .format(cal.getTime());
        FSSReportResult result = climateCreatorDAO.getMetarConreal(informId,
                MetarUtils.METAR_SUNSHINE_DURATION, datetime);

        if (!result.isMissing()) {
            // value is not the missing value, so actual data was returned
            // discard minutes not in the valid range
            // Legacy made the maximum "999", but this is likely a typo of
            // "9999"
            int minutes = (int) result.getValue();
            if (minutes < ParameterBounds.MIN_SUN_LOWER_BOUND
                    || minutes > ParameterBounds.MIN_SUN_UPPER_BOUND) {
                logger.warn("Invalid value [" + minutes
                        + "] for minutes of sunshine on [" + datetime + "]");

                return ParameterFormatClimate.MISSING;
            } else {
                return minutes;
            }
        } else {
            /* try to get data from 12Z report if no data at 08Z */
            logger.info("Could not retrieve minutes of sunshine for ["
                    + datetime + "]");

            cal.set(Calendar.HOUR_OF_DAY, 12);

            datetime = ClimateDate.getFullDateTimeFormat()
                    .format(cal.getTime());

            int minutes = (int) (climateCreatorDAO.getMetarConreal(informId,
                    MetarUtils.METAR_SUNSHINE_DURATION, datetime)).getValue();

            if (minutes < ParameterBounds.MIN_SUN_LOWER_BOUND
                    || minutes > ParameterBounds.MIN_SUN_UPPER_BOUND) {
                logger.warn("Invalid value [" + minutes
                        + "] for minutes of sunshine on [" + datetime + "]");

                return ParameterFormatClimate.MISSING;
            } else {
                return minutes;
            }
        }
    }

    /**
     * Rewritten from build_daily_obs_wind.f
     * 
     * <pre>
     *   June 1998     Jason P. Tuell        PRC/TDL
    *   Sept 1998     David O. Miller       PRC/TDL
    *
    *
    *   Purpose:  This routine controls building the daily wind climatology.
    *             The daily wind climatology consists of the maximum wind
    *             direction and speed, the time the maximum wind was observed,
    *             the maximum gust direction and speed, the time the maximum
    *             gust was observed and the resultant wind direction and speed.
    * 
    *
    *   Variables
    *
    *      Input
    *        begin_date         - derived TYPE that contains the starting
    *                             date for the period of this climate summary
    *        begin_time         - derived TYPE that contains the starting
    *                             time for the period of this climate summary
    *        end_date           - derived TYPE that contains the ending
    *                             date for the period of this climate summary
    *        end_time           - derived TYPE that contains the ending
    *                             time for the period of this climate summary
    *        inform_id          - INFORMIX id of a climate station
    *        itype              - flag which controls the type of climate
    *                             summary being generated;
    *                             =1  NWR morning daily climate summary
    *                             =2  NWR evening daily climate summary
    *                             =3  NWWS morning daily climate summary
    *                             =4  NWWS evening daily climate summary
    *                             =5  NWR monthly radio climate summary
    *                             =6  NWWS monthly climate summary
    *                             =7  NWWS annual climate summary
    *        num_off_UTC        - number of hours off UTC; used to calculate local
    *                             time
    *
    *      Output
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        qc                 - derived TYPE that contains the flags
    *                             which specify the method used to ohtain
    *                             the climate data
    *
    *      Local
    *        a_gust             - derived TYPE containing the gust direction and
    *                             speed from the METAR gust reports
    *        a_peak_wind        - derived TYPE containing the peak wind direction and
    *                             speed from the METAR peak wind remark
    *        a_peak_wind_time   - time of peak wind from METAR peak wind remark
    *        avg_wind_speed     - mean scalar wind speed
    *        delta_hours        - number of hours between the start and end times
    *        iday               - Julian date
    *        ihour              - hour (UTC) for wind data retrieval
    *        missing            - flag for missing data
    *        now_date           - derived TYPE that contains base date for
    *                             hourly wind retrieval
    *        result             - derived TYPE that contains the resultant
    *                             wind direction and speed
    *        save_max_time      - derived TYPE containing the max wind time of
    *                             occurance
    *        save_max_wind      - derived TYPE containing the max wind speed and 
    *                             direction
    *        save_peak_time     - derived TYPE containing the max gust time of
    *                             occurance
    *        save_peak_wind     - derived TYPE containing the max gust speed and 
    *                             direction
    *        speci_time         - derived TYPE containing the max wind time of
    *                             occurance from SPECI report
    *        speci_wind         - derived TYPE containing the max gust speed and 
    *                             direction from SPECI report
    *        test_speed         - max wind speed; temporary holding variable
    *        test_speed_max     - max gust speed; temporary holding variable
    *        this_date          - derived TYPE that contains date for 
    *                             hourly wind retrieval
    *        winds              - derived TYPE that contains an array of
    *                             hourly wind directions and speeds
    *
    *      Non-system functions used
    *        calculate_delta_hours - This routine calculates the number of hours
    *                                difference in the starting and ending periods 
    *                                of a daily climate summary
    *        julday                - returns a Julian day for an input date
    *
    *      Non-system routines used
    *        build_resultant_wind  - given an input on wind directions and
    *                                speeds, returns the resultant wind
    *                                direction and speed
    *        get_all_hourly_gusts  - obtains hourly gust directions and speeds
    *                                for a specified period of time
    *        get_all_speci_winds   - obtains SPECI wind directions and speeds
    *                                for a specified period of time
    *        get_hourly_peak_winds - obtains hourly gust directions and speeds
    *                                for a specified period of time from the
    *                                peak wind remarks
    *        get_hourly_winds      - obtains hourly wind directions and speeds
    *                                for a specified period of time
    *        convert_julday        - returns a date for an input Julian date
    *
    *    MODIFICATION HISTORY
    *    --------------------
    *      David T. Miller        7/19/99  Small logic error in using the greatest 
    *                                      speci wind found. Also, use the daily 
    *                                      summary message if available
    *      David T. Miller        8/13/99  Another small logic error in  trying to 
    *                                      retrieve peak wind. Must add one more to 
    *                                      delta_hour and separate hourlies from peaks
    *                                      so can retrieve peak during last hour 
    *                                      of the period
    *      David T. Miller        8/31/99  Observed several instances where
    *                                      A special wind gust was the 
    *                                      highest gust of the day but
    *                                      daily climate did not report it
    *                                      as such.  Therefore, had to 
    *                                      modify the call to get_all_speci_
    *                                      winds so first call gets winds
    *                                      and second call gets gusts.  
    *                                      Modified this routine to retrieve
    *                                      SPECI gusts.
    *      David T. Miller        7/12/00  Changed references of wind speed 
    *                                      data type from INTEGER to REAL.
    *      Doug Murphy            2/16/01  The wind speeds taken from METAR's must
    *                                      be converted from kts to mph. Wind 
    *                                      speeds are now mph throughout climate.
     * </pre>
     * 
     * @param window
     *            datetime range to process. Assumed to be start and end
     *            datetimes within 24 hours of each other.
     * @param itype
     * @param numOffUTC
     * @param dailyClimateData
     *            data to set, assumed to already have inform ID (station ID)
     *            set.
     * @throws ClimateQueryException
     */
    private void buildDailyObsWind(ClimateDates window, PeriodType itype,
            short numOffUTC, DailyClimateData dailyClimateData)
                    throws ClimateQueryException {
        /*
         * Retrieve up to 24 hours of wind directions and speeds from the data
         * base. Build the array of wind directions and speeds one hour at a
         * time.
         */
        ClimateTime currTime = new ClimateTime(window.getStartTime());

        logger.debug("Calculating delta hours.");
        int deltaHours;
        if (PeriodType.MORN_NWWS.equals(itype)
                || PeriodType.MORN_RAD.equals(itype)) {
            deltaHours = TimeUtil.HOURS_PER_DAY;
            logger.debug("Morning delta hours is defaulting to 24.");
        } else {
            deltaHours = ClimateUtilities.calculateDeltaHours(window);
            logger.debug("Delta hours for period " + window.toString()
                    + " calculated as " + deltaHours);
        }
        /*
         * 8/13/99 Peak winds are reported on the METARs on the hour but they
         * occurred in the period after the last hour. Therefore, for peak
         * winds, need to add 1 to delta_hours so we account for any peak winds
         * which occurred just before the period ended.
         */
        deltaHours++;

        float maxWindSpeed = -1;
        float maxGustSpeed = -1;

        /*
         * Initialize the hourly wind arrays to be missing
         */
        List<ClimateWind> winds = new ArrayList<>();
        for (int i = 0; i < deltaHours; i++) {
            winds.add(ClimateWind.getMissingClimateWind());
        }

        /*
         * Check for daily summary message values and use if not missing
         */
        if ((dailyClimateData.getMaxWind()
                .getDir() == ParameterFormatClimate.MISSING)
                || (dailyClimateData.getMaxGust()
                        .getDir() == ParameterFormatClimate.MISSING)) {

            int windQC = ParameterFormatClimate.MISSING;
            int gustQC = ParameterFormatClimate.MISSING;

            ClimateTime maxWindTime = new ClimateTime(window.getStartTime());

            /*
             * Get all speci winds for a given time period
             * 
             * Determine the maximum value and time of the speci_wind
             */
            ClimateWind speciWind = ClimateWind.getMissingClimateWind();
            ClimateTime speciWindTime = ClimateTime.getMissingClimateTime();

            climateCreatorDAO.getAllSpeciWinds(window,
                    dailyClimateData.getInformId(), speciWind, speciWindTime,
                    true);

            ClimateWind saveWind = new ClimateWind(speciWind);
            ClimateTime saveWindTime = new ClimateTime(speciWindTime);

            if (saveWind.getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                // currently speed is in knots, convert to mph
                saveWind.setSpeed((float) (saveWind.getSpeed()
                        * ClimateUtilities.KNOTS_TO_MPH));
                windQC = QCValues.MAX_WIND_FROM_SPECI;

                // correct the hour by timezone
                int newHour = saveWindTime.getHour() + numOffUTC;

                if (newHour < 0) {
                    newHour += TimeUtil.HOURS_PER_DAY;
                } else if (newHour >= TimeUtil.HOURS_PER_DAY) {
                    newHour -= TimeUtil.HOURS_PER_DAY;
                }
                saveWindTime.setHour(newHour);
            }

            /*
             * Get all speci wind gusts for a given time period
             * 
             * Determine the maximum value and time of the speci_wind
             */
            ClimateWind speciGust = ClimateWind.getMissingClimateWind();
            ClimateTime speciGustTime = ClimateTime.getMissingClimateTime();

            climateCreatorDAO.getAllSpeciWinds(window,
                    dailyClimateData.getInformId(), speciGust, speciGustTime,
                    false);

            ClimateWind saveGust = new ClimateWind(speciGust);
            ClimateTime saveGustTime = new ClimateTime(speciGustTime);

            if (saveGust.getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                // currently speed is in knots, convert to mph
                saveGust.setSpeed((float) (saveGust.getSpeed()
                        * ClimateUtilities.KNOTS_TO_MPH));
                gustQC = QCValues.MAX_GUST_FROM_GUST;
            }

            for (int i = 0; i < deltaHours - 1; i++) {
                /*
                 * Build the new date and time for the wind retrieval. Don't
                 * forget to adjust the date if we go into the next day.
                 */
                int ihour = window.getStartTime().getHour() + i;

                ClimateDate currDate = new ClimateDate(window.getStart());
                if (ihour >= TimeUtil.HOURS_PER_DAY) {
                    ihour -= TimeUtil.HOURS_PER_DAY;
                    // Set current date to next day of year
                    currDate.convertJulday(currDate.julday() + 1);
                }

                // ! current hour UTC time
                currTime.setHour(ihour);
                // ! current hour local time
                maxWindTime.setHour(i);

                if (i < deltaHours) {
                    /*
                     * Get the hourly winds from which to estimate maximum wind
                     * speed and to calculate the resultant wind.
                     */
                    climateCreatorDAO.getHourlyWinds(currDate, currTime,
                            dailyClimateData.getInformId(), winds.get(i));

                    /*
                     * Now test to see if the most recent wind is greater than a
                     * previous value from either the hourly or SPECI winds. If
                     * so, save the value and the time.
                     */
                    if ((winds.get(i)
                            .getSpeed() != ParameterFormatClimate.MISSING_SPEED)
                            && (winds.get(i).getSpeed() != 0)) {
                        // currently speed is in knots, convert to mph
                        winds.get(i).setSpeed((float) (winds.get(i).getSpeed()
                                * ClimateUtilities.KNOTS_TO_MPH));

                        if (winds.get(i).getSpeed() >= maxGustSpeed) {
                            maxGustSpeed = winds.get(i).getSpeed();

                            if ((saveWind
                                    .getSpeed() == ParameterFormatClimate.MISSING_SPEED)
                                    || (maxGustSpeed >= saveWind.getSpeed())) {
                                saveWind = new ClimateWind(winds.get(i));
                                saveWindTime = new ClimateTime(maxWindTime);
                                windQC = QCValues.MAX_WIND_FROM_HOURLY;
                            }
                        }
                    }
                }

                if ((i + 1) < deltaHours) {
                    /*
                     * Get the wind gusts from all reports for a given station,
                     * date and nominal time
                     */
                    ClimateWind aGust = ClimateWind.getMissingClimateWind();
                    climateCreatorDAO.getAllHourlyGusts(currDate, currTime,
                            dailyClimateData.getInformId(), aGust);

                    /*
                     * Now test against to see if the most recent hourly gust is
                     * greater than a previous value. If so, save the value and
                     * time.
                     */
                    if (aGust
                            .getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                        // currently speed is in knots, convert to mph
                        aGust.setSpeed((float) (aGust.getSpeed()
                                * ClimateUtilities.KNOTS_TO_MPH));

                        if (aGust.getSpeed() > maxWindSpeed) {
                            maxWindSpeed = aGust.getSpeed();

                            if ((saveGust
                                    .getSpeed() == ParameterFormatClimate.MISSING_SPEED)
                                    || (maxWindSpeed >= saveGust.getSpeed())) {
                                saveGust = new ClimateWind(aGust);
                                saveGustTime = new ClimateTime(currTime);
                                gustQC = QCValues.MAX_GUST_FROM_GUST;
                            }
                        }
                    }
                }

                if (i > 0) {
                    /*
                     * Get the peak wind data from the hourly report for a given
                     * station, date and time. The peak wind is a field that is
                     * reported separately from the gusts in the METAR code,
                     * hence the need for two different routines.
                     */

                    ClimateWind aPeakWind = ClimateWind.getMissingClimateWind();
                    ClimateTime aPeakWindTime = ClimateTime
                            .getMissingClimateTime();
                    climateCreatorDAO.getHourlyPeakWinds(currDate, currTime,
                            dailyClimateData.getInformId(), aPeakWind,
                            aPeakWindTime);

                    /*
                     * Now test to see if the most recent hourly peak wind is
                     * greater than a previous value. If so, save the value and
                     * the time.
                     */
                    if (aPeakWind
                            .getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                        // currently speed is in knots, convert to mph
                        aPeakWind.setSpeed((float) (aPeakWind.getSpeed()
                                * ClimateUtilities.KNOTS_TO_MPH));

                        if (aPeakWind.getSpeed() > maxWindSpeed) {
                            maxWindSpeed = aPeakWind.getSpeed();

                            if ((saveGust
                                    .getSpeed() == ParameterFormatClimate.MISSING_SPEED)
                                    || (maxWindSpeed >= saveGust.getSpeed())) {
                                saveGust = new ClimateWind(aPeakWind);
                                saveGustTime = new ClimateTime(aPeakWindTime);
                                gustQC = QCValues.MAX_GUST_FROM_PEAK;
                            }
                        }
                    }
                }

                /*
                 * Update the data structure with the max wind information
                 */
                if (dailyClimateData.getMaxWind()
                        .getDir() == ParameterFormatClimate.MISSING) {
                    dailyClimateData.setMaxWind(saveWind);
                    dailyClimateData.setMaxWindTime(saveWindTime);
                    dailyClimateData.getDataMethods().setMaxWindQc(windQC);
                }

                /*
                 * Update the data structure with the gust information
                 */
                if (dailyClimateData.getMaxGust()
                        .getDir() == ParameterFormatClimate.MISSING) {
                    dailyClimateData.setMaxGust(saveGust);
                    dailyClimateData.setMaxGustTime(saveGustTime);

                    if (dailyClimateData.getMaxGustTime()
                            .getHour() != ParameterFormatClimate.MISSING_HOUR) {
                        // correct for time zone
                        int newHour = dailyClimateData.getMaxGustTime()
                                .getHour() + numOffUTC;

                        if (newHour < 0) {
                            newHour += TimeUtil.HOURS_PER_DAY;
                        } else if (newHour >= TimeUtil.HOURS_PER_DAY) {
                            newHour -= TimeUtil.HOURS_PER_DAY;
                        }
                        dailyClimateData.getMaxGustTime().setHour(newHour);
                    }

                    dailyClimateData.getDataMethods().setMaxGustQc(gustQC);
                }
            }
        }

        /*
         * Build the resultant wind direction and speed, and average wind, but
         * only if daily summary message average was unavailable. Discrepancies
         * #62 and #63, allowing a missing average wind speed QC value and
         * saving on potentially unnecessary calculations for average wind
         * speed.
         */
        if (dailyClimateData.getDataMethods()
                .getAvgWindQc() == ParameterFormatClimate.MISSING) {
            float avgWindSpeedSum = 0;
            float sumX = 0;
            float sumY = 0;
            int validHours = 0;

            for (ClimateWind wind : winds) {
                if ((wind.getDir() != ParameterFormatClimate.MISSING) && (wind
                        .getSpeed() != ParameterFormatClimate.MISSING_SPEED)) {
                    validHours++;
                    avgWindSpeedSum += wind.getSpeed();
                    sumX += wind.getSpeed()
                            * Math.sin(Math.toRadians(wind.getDir()));
                    sumY += wind.getSpeed()
                            * Math.cos(Math.toRadians(wind.getDir()));
                }
            }

            /*
             * Only calculate the resultant and average wind if the number of
             * hours is less than zero. Set to missing if there was no valid
             * wind data (i.e., num_hours= 0)
             */
            float avgWindSpeed;

            if (validHours == 0) {
                avgWindSpeed = ParameterFormatClimate.MISSING_SPEED;
                dailyClimateData
                        .setResultX(ParameterFormatClimate.MISSING_SPEED);
                dailyClimateData
                        .setResultY(ParameterFormatClimate.MISSING_SPEED);
                dailyClimateData
                        .setResultWind(ClimateWind.getMissingClimateWind());
            } else {
                dailyClimateData.setResultX(sumX / validHours);
                dailyClimateData.setResultY(sumY / validHours);
                dailyClimateData.setResultWind(ClimateUtilities
                        .buildResultantWind(sumX, sumY, validHours));

                avgWindSpeed = avgWindSpeedSum / validHours;
                dailyClimateData.getDataMethods()
                        .setAvgWindQc(QCValues.AVG_WIND_CALCULATED);
                dailyClimateData.setAvgWindSpeed(avgWindSpeed);
            }
        }
    }

    /**
     * Migrated from build_daily_obs_rh.f
     * 
     * <pre>
     *   June 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls building the daily relative
    *             humidity climatology, which consists of the following:
    *
    *             maximum RH       time (local) of maximum RH
    *             minimum RH       time (local) of minimum RH
    *             mean    RH
    *             
    *             The relative humidity is calculated from the hourly
    *             temperature and dewpoint.  
    *
    *   Variables
    *
    *      Input
    *        begin_date         - derived TYPE that contains the starting
    *                             date for the period of this climate summary
    *        begin_date         - derived TYPE that contains the starting
    *                             time for the period of this climate summary
    *        end_date           - derived TYPE that contains the ending
    *                             date for the period of this climate summary
    *        end_date           - derived TYPE that contains the ending
    *                             time for the period of this climate summary
    *        inform_id          - INFORMIX id of a climate station
    *
    *      Output
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *
    *      Local
    *        dew                - array of dewpoints
    *        iday               - Julian date
    c        ihour              - hour (UTC) for wind data retrieval
    *        max_RH             - maximum RH
    *        min_RH             - minimum RH
    *        now_date           - derived TYPE that contains base date for
    *                             hourly wind retrieval
    *        this_date          - derived TYPE that contains date for 
    *                             hourly wind retrieval
    *        temp               - array of temperatures
    *        dew                - array of dewpoints
    *        
    *
    *
    *      Non-system functions used
    *        e_from_t           - returns vapor pressure (mb) for an input 
    *                             temperature
    *        julday             - returns a Julian day for an input date
    *
    *      Non-system routines used
    *        build_resultant_wind - given an input on wind directions and
    *                               speeds, returns the resultant wind
    *                               direction and speed
    *        get_hourly_winds     - obtains hourly wind directions and speeds
    *                               for a specified period of time
    *        convert_julday       _ returns a date for an input Julian date
     * </pre>
     * 
     * @param window
     *            datetime range to process. Assumed to be start and end
     *            datetimes within 24 hours of each other. If more than 24
     *            hours, then calculations will be cut short at 24 hours of
     *            data.
     * @param itype
     * @param dailyClimateData
     *            data to set. Assumed to already have inform ID (station ID)
     *            set.
     */
    private void buildDailyObsRh(ClimateDates window, PeriodType itype,
            DailyClimateData dailyClimateData) {
        int stationID = dailyClimateData.getInformId();

        int hourMaxRH = 0;
        int hourMinRH = 0;

        int minRH = Integer.MAX_VALUE;
        int maxRH = Integer.MIN_VALUE;

        /*
         * Retrieve 24 hours of temperatures and dewpoints from the data base.
         * Build the array of temperatures and dewpoints one hour at a time.
         */
        ClimateDate currDate = new ClimateDate(window.getStart());
        ClimateTime currTime = new ClimateTime(window.getStartTime());

        logger.debug("Calculating delta hours.");
        int deltaHours;
        if (PeriodType.MORN_NWWS.equals(itype)
                || PeriodType.MORN_RAD.equals(itype)) {
            deltaHours = TimeUtil.HOURS_PER_DAY;
            logger.debug("Morning delta hours is defaulting to 24.");
        } else {
            deltaHours = ClimateUtilities.calculateDeltaHours(window);
            logger.debug("Delta hours for period " + window.toString()
                    + " calculated as " + deltaHours);
        }

        if (deltaHours >= TimeUtil.HOURS_PER_DAY) {
            logger.warn("Delta hours for period " + window.toString()
                    + " calculated as " + deltaHours
                    + ". This is above the expected limit of 23."
                    + " Calculations will halt at 24 hours of data despite this value.");
        }

        for (int i = 0; (i < deltaHours) && (i < TimeUtil.HOURS_PER_DAY); i++) {
            ClimateDate tempDate = new ClimateDate(currDate);
            /*
             * Build the new date and time for temperature and dew point
             * retrieval. Don't forget to adjust the date if we go into the next
             * day.
             */
            int ihour = window.getStartTime().getHour() + i;

            if (ihour >= TimeUtil.HOURS_PER_DAY) {
                ihour -= TimeUtil.HOURS_PER_DAY;
                // Set tempDate to next day of year
                tempDate.convertJulday(currDate.julday() + 1);
            }

            currTime.setHour(ihour);

            int temperature = getTemp(tempDate, currTime, stationID);

            int dewpoint = getDew(tempDate, currTime, stationID);

            // Now calculate RH for each valid temperature and dewpoint.
            if ((temperature != ParameterFormatClimate.MISSING)
                    && (dewpoint != ParameterFormatClimate.MISSING)) {
                int currHumidity = ClimateUtilities.nint(((ClimateUtilities
                        .vaporPressureFromTemperature(dewpoint))
                        / (ClimateUtilities
                                .vaporPressureFromTemperature(temperature)))
                        * 100);

                /*
                 * Now determine the max and min RH and the hours at which they
                 * were observed. TODO Task 36087 In Migrated Climate, as in
                 * Legacy, the hour of max or mean relative humidity is
                 * determined by an index number representative of how many
                 * hours have been processed thus far. However, this is not
                 * necessarily the same as the actual hour of the data,
                 * depending on what time of day the looped processing started
                 * at. The actual hour seems to already be calculated within the
                 * loop.
                 */
                if (currHumidity > maxRH) {
                    maxRH = currHumidity;
                    hourMaxRH = i;
                }

                if (currHumidity < minRH) {
                    minRH = currHumidity;
                    hourMinRH = i;
                }
            }
        }

        /*
         * Now set up the data structure if the various pieces aren't missing.
         */
        if (maxRH != Integer.MIN_VALUE) {
            dailyClimateData.setMaxRelHumid(maxRH);
            dailyClimateData.setMaxRelHumidHour(hourMaxRH);
        }

        if (minRH != Integer.MAX_VALUE) {
            dailyClimateData.setMinRelHumid(minRH);
            dailyClimateData.setMinRelHumidHour(hourMinRH);
        }

        if ((maxRH != Integer.MIN_VALUE) && (minRH != Integer.MAX_VALUE)) {
            /*
             * mean RH is calculated as the average of max and min
             */
            dailyClimateData.setMeanRelHumid(
                    ClimateUtilities.nint((maxRH + minRH) / 2.0));
        }
    }

    /**
     * Migrated from build_daily_obs_precip.f and compute_daily_precip.c.
     * 
     * <pre>
     *   June 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the retrieval and determination
    *             of the daily observed precipitation.  It calls a routine
    *             that returns the precipitation that fell between
    *             a specified starting and ending time for a given station.
    *             It also sets the precip quality control flag.
    * 
    *
    *   Variables
    *
    *      Input
    *        begin_date         - derived TYPE that contains the starting
    *                             date for the period of this climate summary
    *        begin_date         - derived TYPE that contains the starting
    *                             time for the period of this climate summary
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        end_date           - derived TYPE that contains the ending
    *                             date for the period of this climate summary
    *        end_date           - derived TYPE that contains the ending
    *                             time for the period of this climate summary
    *        inform_id          - INFORMIX id of a climate station
    *
    *      Output
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        precip_qc          - flag which specifies the method used to
    *                             determine the precipitation
    *
    *
    *      Local
    *        precip             - amount of precipitation that fell in a 
    *                             specified period.  Value is returned as
    *                             a real number with precision to the
    *                             hundredths
    *
    *      Non-system routines used
    *        compute_daily_precip - returns the amount of precipitation between
    *                               the starting and ending periods for a given
    *                               station
    ********************************************************************************
    * FILENAME:             compute_daily_precip.c
    * FILE DESCRIPTION:
    * NUMBER OF MODULES:    3
    * GENERAL INFORMATION:
    *   MODULE 1:           compute_daily_amt
    *   DESCRIPTION:        Contains the code to process the observed
    *                       precipitation amount.
    *   MODULE 2:           tally_rain_amount
    *   DESCRIPTION:        Contains the code to maintain a cumulative rain amount
    *                       taking into account special precipitation situations
    *                       such as a trace of rain, etc.
    *   MODULE 3:           get_period_rain_amt
    *   DESCRIPTION:        Contains the code to maintain a cumulative rain 
    *                       amount over a user-specified period.
    * ORIGINAL AUTHOR:      Bryon Lawrence
    * CREATION DATE:        August 23, 1998
    * ORGANIZATION:         GSC / TDL
    * MACHINE:              HP9000
    * COPYRIGHT:
    * DISCLAIMER:
    * MODIFICATION HISTORY:
    *   MODULE #        DATE         PROGRAMMER        DESCRIPTION/REASON
    *          1        8/23/98      Bryon Lawrence    Original Coding
    *          2        8/23/98      Bryon Lawrence    Original Coding
    *          3        8/23/98      Bryon Lawrence    Original Coding
    *          1        1/24/00      Bryon Lawrence    Fixed an error in this
    *                                                  routine that was causing
    *                                                  inflated rain amounts.
    *          3        1/24/00      Bryon Lawrence    Slightly simplified the
    *                                                  logic in this routine.
    ********************************************************************************
    * MODULE NUMBER:   1
    * MODULE NAME:     compute_daily_precip
    * PURPOSE:            This routine looks through decoded METAR data to
    *                  determine how much rain fell during a user-specified time
    *                  period. This routine checks to see if the 
    *                  precipitation sensor was operational at the time
    *                  time the observations were taken. If it wasn't, then
    *                  the rain amount for the period is reported as missing.
    *                  If no rain fell during the period, then the rain amount
    *                  is reported as 0. If only a trace of rain occurred, then
    *                  the rain amount is reported as a -2. If no METAR reports
    *                  can be found in the period OR if the precipitation
    *                  sensor on the automated observing system is not operational,
    *                  then a precipitation amount of R_MISS is reported.
    *
    *                  Note that it is the responsibility of the user to
    *                  open and close the connection with the HM database.
    *                  This routine is designed to work with a version
    *                  7.1 or newer of INFORMIX.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE    NAME                DESCRIPTION/UNITS
    *      I   climate_date *begin_date         A pointer to a climate_date
    *                                           structure.
    *      I   climate_time *begin_time         A pointer to a climate_time
    *                                           structure.
    *      I   climate_date *end_date           A pointer to a climate_date
    *                                           structure.
    *      I   climate_time *end_time           A pointer to a climate_time
    *                                           structure.
    *      I   long         *station_id         A pointer to a memory location
    *                                           containing the numeric identifier
    *                                           of the station to process data 
    *                                           for.
    *      O   float        *precip_amount      The total rainfall accumulation. 
    *                                           
    * RETURNS:
    *   DATA TYPE   NAME                        DESCRIPTION
    *   STATUS      status                      Reports any fatal SQL or 
    *                                           memory allocation errors.
    *                                           (See below for a break down
    *                                            of the error codes).
    *
    * APIs UTILIZED:
    *   NAME                    HEADER FILE          DESCRIPTION
    *   get_metar_conreal_val   hm_dbutils.h         Retrieves a 
    *                                                FSS continous real value
    *                                                for a specified element id
    *                                                and nominal time. 
    *   tally_rain_amount     compute_daily_precip.h Maintains a cumulative total
    *                                                of all of the rain amounts
    *                                                retrieved for a 12 hour
    *                                                period.
    *   log_write               log_write.h          Contains the C wrapper around
    *                                                the C++ logstream.
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * These are documented in the body of the code.
    *
    * DATA FILES AND/OR DATABASE:
    * In order for this routine to run, the FSS_report and the FSS_contin_real
    * tables in the HM database must be populated with decoded METAR
    * data.
    *
    * ERROR HANDLING:
    *    ERROR CODE                        DESCRIPTION
    *
    *    STATUS_OK                         The routine executed correctly. 
    *    MALLOC_ERROR                      Memory could not be dynamically 
    *                                      allocated in the 
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the 
    *                                      "condition" in a query that was 
    *                                      expecting only one row in the 
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the 
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
    *
    *    (Note that all of these error codes are defined in STATUS.h).
    *
    * MACROS
    *    Name                     Header             Description
    * CHECK_FOR_FATAL_ERROR  compute_daily_precip.h  Checks for fatal INFORMIX
    *                                                errors.
    * CREATE_INFORMIX_TIME   AEV_time_util.h         Converts a time value in UNIX
    *                                                ticks to an INFORMIX 
    *                                                compatible string.
     * </pre>
     * 
     * @param window
     * @param dailyClimateData
     *            data to set. Assumed to already have inform ID (station ID)
     *            set.
     */
    private void buildDailyObsPrecip(ClimateDates window,
            DailyClimateData dailyClimateData) {
        /*
         * Contains the beginning base time in UNIX ticks representation.
         */
        Calendar beginCal = window.getStart().getCalendarFromClimateDate();
        beginCal.set(Calendar.HOUR_OF_DAY, window.getStartTime().getHour());
        beginCal.set(Calendar.MINUTE, window.getStartTime().getMin());
        long beginBaseMilliTicks = beginCal.getTimeInMillis();

        int beginHour = window.getStartTime().getHour();

        if (window.getStartTime().getMin() > 44) {
            beginBaseMilliTicks += TimeUtil.MILLIS_PER_HOUR;
            beginHour++;

            if (beginHour >= TimeUtil.HOURS_PER_DAY) {
                beginHour = 0;
            }
        }

        /*
         * Contains the ending base time in UNIX ticks representation.
         */
        Calendar endCal = window.getEnd().getCalendarFromClimateDate();
        endCal.set(Calendar.HOUR_OF_DAY, window.getEndTime().getHour());
        endCal.set(Calendar.MINUTE, window.getEndTime().getMin());
        long endBaseMilliTicks = endCal.getTimeInMillis();

        int endHour = window.getEndTime().getHour();

        if (window.getEndTime().getMin() > 44) {
            endBaseMilliTicks += TimeUtil.MILLIS_PER_HOUR;
            endHour++;

            if (endHour >= TimeUtil.HOURS_PER_DAY) {
                endHour = 0;
            }
        }

        /*
         * The number of "extra" hours at the beginning of the user-specified
         * period.
         */
        int beginOffset = 0;

        /* Compute the base start time. */
        if (beginHour % 6 != 0) {
            /* The begin time is not "in sync" with GMT time. */
            beginOffset = 6 - (beginHour % 6);
            beginBaseMilliTicks += (beginOffset * TimeUtil.MILLIS_PER_HOUR);
        }

        /*
         * The number of "extra" hours at the end of the user-specified period.
         */
        int endOffset = 0;

        /* Compute the base end time. */
        if (endHour % 6 != 0) {
            /* The end time is not "in sync" with GMT time. */
            endOffset = endHour % 6;
            endBaseMilliTicks -= (endOffset * TimeUtil.MILLIS_PER_HOUR);
        }

        /* Determine the length of the time period being computed. */
        int num6HourPeriods = (int) ((endBaseMilliTicks - beginBaseMilliTicks)
                / ClimateUtilities.MILLISECONDS_IN_SIX_HOURS);

        boolean oneHour = false;
        boolean threeHour = false;
        boolean sixHour = false;

        for (int j = num6HourPeriods - 1; j >= 0; j--) {
            boolean missingOrError6Hour = false;
            /*
             * Check to see if there was a rain amount reported for the 6 hour
             * period.
             */
            long baseMilliTicks = endBaseMilliTicks
                    - (ClimateUtilities.MILLISECONDS_IN_SIX_HOURS * j);
            long nominalMilliTicks = baseMilliTicks;

            Calendar nominalTimeCal = TimeUtil.newCalendar();
            nominalTimeCal.setTimeInMillis(nominalMilliTicks);
            String nominalTimeString = ClimateDate.getFullDateTimeFormat()
                    .format(nominalTimeCal.getTime());

            try {
                FSSReportResult result = climateCreatorDAO.getMetarConreal(
                        dailyClimateData.getInformId(),
                        MetarUtils.METAR_6HR_PRECIP, nominalTimeString);

                if (!result.isMissing()) {
                    /*
                     * The 6xxx precip group containing the 6 hour rain amount
                     * was present.
                     */
                    dailyClimateData.setPrecip(tallyRainAmount(
                            dailyClimateData.getPrecip(), result.getValue()));
                    sixHour = true;
                } else {
                    missingOrError6Hour = true;
                }
            } catch (ClimateQueryException e) {
                logger.error(
                        "Unexpected issue on METAR query for 6-hourly precip",
                        e);
                missingOrError6Hour = true;
            }

            if (missingOrError6Hour) {
                boolean missingOrError3Hour = false;
                /*
                 * The METAR report is either missing, the PNO indicator is
                 * present, or the 6xxx precip group is missing. We must check
                 * to see if there is a 3 hr precipitation amount report.
                 */
                nominalMilliTicks = baseMilliTicks
                        - ClimateUtilities.MILLISECONDS_IN_THREE_HOURS;

                nominalTimeCal.setTimeInMillis(nominalMilliTicks);
                nominalTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(nominalTimeCal.getTime());

                try {
                    FSSReportResult result = climateCreatorDAO.getMetarConreal(
                            dailyClimateData.getInformId(),
                            MetarUtils.METAR_3HR_PRECIP, nominalTimeString);

                    if (!result.isMissing()) {
                        /*
                         * The 6xxx precip group containing the 3 hourly rain
                         * amount was present. We need to individually process
                         * the remaining 3 hours in the 6 hour period.
                         */
                        dailyClimateData.setPrecip(
                                tallyRainAmount(dailyClimateData.getPrecip(),
                                        result.getValue()));
                        dailyClimateData.setPrecip(getPrecipRainAmount(-2, 0,
                                dailyClimateData.getInformId(), baseMilliTicks,
                                dailyClimateData.getPrecip()));
                        threeHour = true;
                        oneHour = true;
                    } else {
                        missingOrError3Hour = true;
                    }
                } catch (ClimateQueryException e1) {
                    logger.error(
                            "Unexpected issue on METAR query for 3-hourly precip",
                            e1);
                    missingOrError3Hour = true;
                }

                if (missingOrError3Hour) {
                    /*
                     * The METAR report is either missing, the PNO indicator is
                     * present, or the 6xxx group containing the 3 hourly rain
                     * amount is missing. We must now check the individual
                     * hourly reports for the entire 6 hour period.
                     */
                    dailyClimateData.setPrecip(getPrecipRainAmount(-5, 0,
                            dailyClimateData.getInformId(), baseMilliTicks,
                            dailyClimateData.getPrecip()));
                    oneHour = true;
                }
            }
        }

        /*
         * Check to see if there are any remaining "leading" or "tailing" hours
         * to process.
         */
        if (beginOffset != 0) {
            float rainAmount = ClimateCreatorDAO.R_MISS;

            if (beginOffset > 3) {
                /*
                 * Check to see if there was a 6xxx group reported in the base
                 * report.
                 */
                long nominalMilliTicks = beginBaseMilliTicks;

                Calendar nominalTimeCal = TimeUtil.newCalendar();
                nominalTimeCal.setTimeInMillis(nominalMilliTicks);
                String nominalTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(nominalTimeCal.getTime());

                boolean sixHourMissingOrError = false;
                try {
                    FSSReportResult sixHourResult = climateCreatorDAO
                            .getMetarConreal(dailyClimateData.getInformId(),
                                    MetarUtils.METAR_6HR_PRECIP,
                                    nominalTimeString);

                    if (sixHourResult.isMissing()) {
                        sixHourMissingOrError = true;
                    } else {
                        rainAmount = tallyRainAmount(rainAmount,
                                sixHourResult.getValue());
                        sixHour = true;

                        /*
                         * See if there is a 6xxx group reported 3 hours prior
                         * to the base time.
                         */
                        nominalMilliTicks = beginBaseMilliTicks
                                - ClimateUtilities.MILLISECONDS_IN_THREE_HOURS;
                        nominalTimeCal.setTimeInMillis(nominalMilliTicks);
                        nominalTimeString = ClimateDate.getFullDateTimeFormat()
                                .format(nominalTimeCal.getTime());

                        boolean threeHourMissingOrError = false;
                        try {
                            FSSReportResult threeHourResult = climateCreatorDAO
                                    .getMetarConreal(
                                            dailyClimateData.getInformId(),
                                            MetarUtils.METAR_3HR_PRECIP,
                                            nominalTimeString);

                            if (threeHourResult.isMissing()) {
                                threeHourMissingOrError = true;
                            } else {
                                threeHour = true;
                                if ((rainAmount > 0)
                                        && (threeHourResult.getValue() > 0)) {

                                    if (threeHourResult
                                            .getValue() > rainAmount) {
                                        logger.warn(
                                                "For station ID ["
                                                        + dailyClimateData
                                                                .getInformId()
                                                        + "] the 3 hour rain total ["
                                                        + threeHourResult
                                                                .getValue()
                                                        + "] for time ["
                                                        + nominalTimeString
                                                        + "] is greater than the 6 hour rain total ["
                                                        + sixHourResult
                                                                .getValue()
                                                        + "]");

                                        rainAmount = ClimateCreatorDAO.R_MISS;

                                        rainAmount = getPrecipRainAmount(
                                                -1 * (beginOffset - 1), 0,
                                                dailyClimateData.getInformId(),
                                                beginBaseMilliTicks,
                                                rainAmount);
                                        oneHour = true;
                                    } else if (ClimateUtilities.floatingEquals(
                                            rainAmount,
                                            threeHourResult.getValue())) {
                                        rainAmount = ClimateCreatorDAO.R_MISS;

                                        /*
                                         * Process the remaining hours in the
                                         * period.
                                         */
                                        rainAmount = getPrecipRainAmount(
                                                -1 * (beginOffset - 1), -3,
                                                dailyClimateData.getInformId(),
                                                beginBaseMilliTicks,
                                                rainAmount);
                                        oneHour = true;
                                    } else {
                                        rainAmount -= threeHourResult
                                                .getValue();

                                        /*
                                         * Process the remaining hours in the
                                         * period.
                                         */
                                        rainAmount = getPrecipRainAmount(
                                                -1 * (beginOffset - 1), -3,
                                                dailyClimateData.getInformId(),
                                                beginBaseMilliTicks,
                                                rainAmount);
                                        oneHour = true;
                                    }
                                } else if ((threeHourResult.getValue() > 0)
                                        && (rainAmount == ParameterFormatClimate.TRACE)) {
                                    logger.warn("For station ID ["
                                            + dailyClimateData.getInformId()
                                            + "] the 3 hour rain total ["
                                            + threeHourResult.getValue()
                                            + "] for time [" + nominalTimeString
                                            + "] is greater than the 6 hour rain total ["
                                            + sixHourResult.getValue() + "]");

                                    rainAmount = getPrecipRainAmount(
                                            -1 * (beginOffset - 1), -3,
                                            dailyClimateData.getInformId(),
                                            beginBaseMilliTicks,
                                            ClimateCreatorDAO.R_MISS);
                                }
                            }
                        } catch (ClimateQueryException e) {
                            logger.error(
                                    "Error with METAR query for 3-hourly precip.",
                                    e);
                            threeHourMissingOrError = true;
                        }

                        if (threeHourMissingOrError) {
                            /*
                             * The 3 hour report was not present; so, process
                             * the hours individually.
                             */
                            rainAmount = getPrecipRainAmount(
                                    -1 * (beginOffset - 1), 0,
                                    dailyClimateData.getInformId(),
                                    beginBaseMilliTicks,
                                    ClimateCreatorDAO.R_MISS);
                            oneHour = true;
                        }
                    }
                } catch (ClimateQueryException e) {
                    logger.error("Error with METAR query for 6-hourly precip.",
                            e);
                    sixHourMissingOrError = true;
                }

                if (sixHourMissingOrError) {
                    /*
                     * The 6xxx group was not present. Process the reports
                     * individually.
                     */
                    rainAmount = getPrecipRainAmount(-1 * (beginOffset - 1), 0,
                            dailyClimateData.getInformId(), beginBaseMilliTicks,
                            ClimateCreatorDAO.R_MISS);
                    oneHour = true;
                }
            } else {
                /*
                 * There is under 3 hours worth of data --> process the reports
                 * individually.
                 */
                rainAmount = getPrecipRainAmount(-1 * (beginOffset - 1), 0,
                        dailyClimateData.getInformId(), beginBaseMilliTicks,
                        rainAmount);
                oneHour = true;
            }

            if (rainAmount != ClimateCreatorDAO.R_MISS) {
                if (((rainAmount >= 0)
                        || (rainAmount == ParameterFormatClimate.TRACE))
                        && ((dailyClimateData
                                .getPrecip() == ClimateCreatorDAO.R_MISS)
                                || (dailyClimateData
                                        .getPrecip() == ParameterFormatClimate.MISSING_PRECIP)
                                || (dailyClimateData.getPrecip() == 0))) {
                    dailyClimateData.setPrecip(rainAmount);
                } else if ((rainAmount > 0)
                        && (dailyClimateData.getPrecip() > 0)
                        && (dailyClimateData
                                .getPrecip() != ParameterFormatClimate.MISSING_PRECIP)) {
                    dailyClimateData.setPrecip(
                            dailyClimateData.getPrecip() + rainAmount);
                } else if ((rainAmount > 0) && (dailyClimateData
                        .getPrecip() == ParameterFormatClimate.TRACE)) {
                    dailyClimateData.setPrecip(rainAmount);
                }
            }

            /* Process the "tailing" hours. */
            rainAmount = ParameterFormatClimate.MISSING_PRECIP;

            if (endOffset >= 3) {
                /*
                 * Search for the 6xxx group containing the 3 hour rain amount.
                 */
                long nominalMilliTicks = endBaseMilliTicks
                        + ClimateUtilities.MILLISECONDS_IN_THREE_HOURS;

                Calendar nominalTimeCal = TimeUtil.newCalendar();
                nominalTimeCal.setTimeInMillis(nominalMilliTicks);
                String nominalTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(nominalTimeCal.getTime());

                boolean threeHourMissingOrError = false;
                try {
                    FSSReportResult threeHourResult = climateCreatorDAO
                            .getMetarConreal(dailyClimateData.getInformId(),
                                    MetarUtils.METAR_3HR_PRECIP,
                                    nominalTimeString);

                    if (threeHourResult.isMissing()) {
                        threeHourMissingOrError = true;
                    } else {
                        rainAmount = tallyRainAmount(rainAmount,
                                threeHourResult.getValue());
                        threeHour = true;

                        /* Process the remaining hours... if there are any... */
                        if (endOffset > 3) {
                            rainAmount = getPrecipRainAmount(4, endOffset,
                                    dailyClimateData.getInformId(),
                                    endBaseMilliTicks, rainAmount);
                            oneHour = true;
                        }
                    }
                } catch (ClimateQueryException e) {
                    logger.error("Error with METAR query for 3-hourly precip.",
                            e);
                    threeHourMissingOrError = true;
                }

                if (threeHourMissingOrError) {
                    /*
                     * The 6xxx group was not present; so, process the hours
                     * individually.
                     */
                    rainAmount = getPrecipRainAmount(1, endOffset,
                            dailyClimateData.getInformId(), endBaseMilliTicks,
                            rainAmount);
                    oneHour = true;
                }

            } else {
                /*
                 * There is under 3 hours worth of additional data to retrieve.
                 */
                rainAmount = getPrecipRainAmount(1, endOffset,
                        dailyClimateData.getInformId(), endBaseMilliTicks,
                        rainAmount);
                oneHour = true;
            }

            if (rainAmount != ClimateCreatorDAO.R_MISS) {
                if (((rainAmount >= 0)
                        || (rainAmount == ParameterFormatClimate.TRACE))
                        && ((dailyClimateData
                                .getPrecip() == ClimateCreatorDAO.R_MISS)
                                || (dailyClimateData
                                        .getPrecip() == ParameterFormatClimate.MISSING_PRECIP)
                                || (dailyClimateData.getPrecip() == 0))) {
                    dailyClimateData.setPrecip(rainAmount);
                } else if ((rainAmount > 0)
                        && (dailyClimateData.getPrecip() > 0)
                        && (dailyClimateData
                                .getPrecip() != ParameterFormatClimate.MISSING_PRECIP)) {
                    dailyClimateData.setPrecip(
                            dailyClimateData.getPrecip() + rainAmount);
                } else if ((rainAmount > 0) && (dailyClimateData
                        .getPrecip() == ParameterFormatClimate.TRACE)) {
                    dailyClimateData.setPrecip(rainAmount);
                }
            }
        }

        /* Set the QC flag */
        if ((dailyClimateData
                .getPrecip() != ParameterFormatClimate.MISSING_PRECIP)
                && (dailyClimateData.getPrecip() != ClimateCreatorDAO.R_MISS)) {
            if (sixHour && threeHour && oneHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_631HR);
            } else if (sixHour && threeHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_63HR);
            } else if (sixHour && oneHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_61HR);
            } else if (threeHour && oneHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_31HR);
            } else if (sixHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_6HR);
            } else if (threeHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_3HR);
            } else if (oneHour) {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(QCValues.PRECIP_FROM_1HR);
            } else {
                dailyClimateData.getDataMethods()
                        .setPrecipQc(ParameterFormatClimate.MISSING);
            }
        } else {
            dailyClimateData.getDataMethods()
                    .setPrecipQc(ParameterFormatClimate.MISSING);
        }
    }

    /**
     * Migrated from build_daily_obs_temp.ec
     * 
     * <pre>
     * 
     * FILENAME:             build_daily_obs_temp.ec
    * FILE DESCRIPTION:
    * NUMBER OF MODULES:    11
    * GENERAL INFORMATION:
    *   MODULE 1:    build_daily_obs_temp
    *   DESCRIPTION: Determines the maximum and minimum temperatures in a time
    *                period defined by the user.
    *
    *   MODULE 2:    get_24hr_maxmin_temp
    *   DESCRIPTION: Looks into the HM database to see if there is a 24 hour
    *                maximum and minimum temperature reported (a 4xxxxxxxx group 
    *                in the METAR code).
    *
    *   MODULE 3:    get_6hr_maxmin_temp
    *   DESCRIPTION: Looks in the HM database to see if there are 1xxxx and 2xxxx
    *                6 hourly maximum and minimum temperature groups.
    *
    *   MODULE 4:    det_6h_max
    *   DESCRIPTION: Looks for a 1xxxx group 6 hour maximum temperature.
    *
    *   MODULE 5:    det_6h_min
    *   DESCRIPTION: Looks for a 2xxxx group 6 hour minimum temperature.
    *              
    *   MODULE 6:    det_pd_max
    *   DESCRIPTION: Determines the maximum temperature for a user-specified
    *                subperiod.
    *              
    *   MODULE 7:    det_pd_min
    *   DESCRIPTION: Determines the minimum temperature for a user_specified
    *                subperiod.
    *              
    *   MODULE 8:    get_pd_metar_max
    *   DESCRIPTION: Determines the maximum temperature for a group of METARs.
    *
    *   MODULE 9:    get_pd_metar_min
    *   DESCRIPTION: Determines the minimum temperature for a group of METARs.
    *
    *   MODULE 10:   get_pd_speci_max
    *   DESCRIPTION: Determines the maximum temperature for a period of one
    *                or more SPECI's.
    *
    *   MODULE 11:   get_pd_speci_min
    *   DESCRIPTION: Determines the minimum temperature for a period of one
    *                or more SPECI's.
    *
    * ORIGINAL AUTHOR:      Bryon Lawrence
    * CREATION DATE:        September 29, 1998
    * ORGANIZATION:         GSC / TDL
    * MACHINE:              HP9000
    * COPYRIGHT:
    * DISCLAIMER:
    * MODIFICATION HISTORY:
    *   MODULE #        DATE         PROGRAMMER        DESCRIPTION/REASON
    *          1        9/29/98      Bryon Lawrence    Completed Original Coding
    *          2        9/29/98      Bryon Lawrence    Completed Original Coding
    *          3        9/29/98      Bryon Lawrence    Completed Original Coding
    *          4        9/29/98      Bryon Lawrence    Completed Original Coding
    *          5        9/29/98      Bryon Lawrence    Completed Original Coding
    *          6        9/29/98      Bryon Lawrence    Completed Original Coding
    *          7        9/29/98      Bryon Lawrence    Completed Original Coding
    *          8        9/29/98      Bryon Lawrence    Completed Original Coding
    *          9        9/29/98      Bryon Lawrence    Completed Original Coding
    *         10        9/29/98      Bryon Lawrence    Completed Original Coding
    *         10        4/01/99      Bryon Lawrence    Fixed duplicate cursor
    *                                                  name problem by renaming
    *                                                  the query cursor from
    *                                                  querycursor to
    *                                                  speci_curse.
    *         11        9/29/98      Bryon Lawrence    Completed Original Coding
    *          1 & 2    7/19/99      Dave Miller       Accounted for climate using 
    *                                                  the Daily Summary Message
    *                                                  for max and min temperatures
    *          6 & 7    8/02/99      Bryon Lawrence    Corrected code causing METAR
    *                                                  max/min times to be
    *                                                  overwritten with SPECI
    *                                                  max/min times.
    *         1-10      01/26/05     Manan Dalal       Ported from Informix to Postgres
     * 
    * MODULE NUMBER:   1
    * MODULE NAME:     build_daily_obs_temp 
    * PURPOSE:         This utility determines the maximum and minimum temperatures
    *          in a period defined by two user-specified times. This temperature
    *          extreme information is taken from decoded METAR weather observations
    *          stored in the HM database. The alogorithm used by this routine is
    *          as follows:
    *                       First the 24 hour temperature group is checked to
    *                       determine the local daily climatological maximum
    *                       and minimum.
    *
    *                       If the 24 hour group is not present or the user is not
    *                       looking for a 24 hour maximum_minimum centered on
    *                       local midnight, then this routine resorts to using
    *                       the 1xxxx and 2xxxx maximum and minimum temperature
    *                       groups reported every 6 hours.
    *
    *                       If the 1xxxx and 2xxxx groups are not present, then the
    *                       hourly Txxxx groups are processed.
    *
    *                       If the hourly Txxxx groups are not present, then
    *                       the hourly rounded temperatures are processed.
    *
    *                       If no maximum and minimum temperature information
    *                       can be determined then these extremes are reported 
    *                       as 9999.
    *
    *                       If the maximum or minimum temperature is determined
    *                       from an hourly METAR observation, then the time
    *                       of the extreme in question is recorded and sent back
    *                       to the calling routine.
    *
    *                       Also, a quality control flag is sent back to the 
    *                       calling routine, and this flag indicates
    *                       exactly how the extreme temperature information
    *                       was arrived at. 
    *
    *                       Assumptions: The calling routine is responsible for
    *                                    establishing a connection with the
    *                                    HM database.
    *
    *                                    The FSS_report and FSS_contin_real tables
    *                                    in the HM database must be populated with
    *                                    decoded METAR data information.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE    NAME                DESCRIPTION/UNITS
    *      I   climate_date *begin_date         A pointer to a climate_date
    *                                           structure.
    *      I   climate_time *begin_time         A pointer to a climate_time
    *                                           structure.
    *      I   climate_date *end_date           A pointer to a climate_date
    *                                           structure.
    *      I   climate_time *end_time           A pointer to a climate_time
    *                                           structure.
    *      I   long         *station_id         A pointer to a memory location
    *                                           containing the numeric identifier
    *                                           of the station to process data 
    *                                           for.
    *      I   int          *itype              Flag indicating whether this
    *                                           is an AM or PM run.
    *      O   daily_climate_data *yesterday    The maximum and minimum
    *                                           temperatures as well as the times
    *                                           of these extremes (if determinable)
    *                                           are written out to this structure.
    *      O  int          *max_temp_qc         Contains quality control
    *                                           information for the maximum
    *                                           temperature.
    *      O  int          *min_temp_qc         Contains the quality control
    *                                           information for the minimum
    *                                           temperature.
    *
    * RETURNS:
    *   None.
    *
    * APIs UTILIZED:
    *   NAME                    HEADER FILE          DESCRIPTION
    *
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * These are documented in the body of the code.
    *
    * DATA FILES AND/OR DATABASE:
    * In order for this routine to run, the FSS_report and the FSS_contin_real
    * tables in the HM database must be populated with decoded METAR
    * data.
    *
    * ERROR HANDLING:
    *    ERROR CODE                        DESCRIPTION
    *
    *    STATUS_OK                         The routine executed correctly.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
    *
    *    (Note that all of these error codes are defined in STATUS.h).
    *
    * MACROS
    *    Name                     Header             Description
    * CHECK_FOR_FATAL_ERROR  compute_daily_precip.h  Checks for fatal INFORMIX
    *                                                errors.
    * CREATE_INFORMIX_TIME   AEV_time_util.h         Converts a time value in UNIX
    *                                                ticks to an INFORMIX
    *                                                compatible string.
    *******************************************************************************
    * MODULE NUMBER:   2
    * MODULE NAME:     get_24hr_maxmin_temp
    * PURPOSE:         This routine searches for a "4" group in a decoded METAR 
    *                  observation in the HM database.  The "4" group is the
    *                  report of local maximum and minimum temperature, usually
    *                  reported at 0000 LST (Local Standard Time). Since some
    *                  reporting sites don't always report their "4" group 
    *                  in the 0000 LST report, this routine searches from an 
    *                  hour before 0000 LST to 4 hours after 0000 LST for this
    *                  group. If this group cannot be found, then the maximum
    *                  and minimum temperatures are returned with values of
    *                  R_MISS.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      long        station_id           The numeric identifier of the 
    *                                           station to find the temperature
    *                                           information for.
    *   I      time_t      end_base_ticks       The time in UNIX ticks of the 
    *                                           0000 LST hour.
    *   O      float       *max_temp            The maximum temperature found in
    *                                           the "4" group.
    *   O      float       *min_temp            The minimum temperature found in
    *                                           the "4" group.
    *
    * RETURNS:
    *   DATA TYPE   NAME                        DESCRIPTION
    *   STATUS      status                      Contains any error or diagnostic
    *                                           codes generated in this routine.
    *
    * APIs UTILIZED:
    *   NAME                     HEADER FILE    DESCRIPTION
    *   get_metar_conreal_val    metar_utils.h  Retrieves a value from the 
    *                                           fss_contin_real table in the 
    *                                           hm database.
    *
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * ( These are defined in the body of the code. )
    *
    * DATA FILES AND/OR DATABASE:
    * This routine references the following tables in the HM database:
    *      FSS_report
    *      FSS_contin_real
    *
    * ERROR HANDLING:
    *    ERROR CODE                        DESCRIPTION
    *    STATUS_OK                         This routine ran to completion.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
    *******************************************************************************
    * MODULE NUMBER:  3
    * MODULE NAME:    get_6hr_maxmin_temp
    * PURPOSE:        This routine looks for maximum and minimum temperature 
    *                 information provided by the 1xxxx and the 2xxxx groups
    *                 in a METAR report. The number of 6 hour periods that this
    *                 routine processes is determined by the calling routine.
    *
    *                 Important! It is the responsibility of the calling routine
    *                 to ensure that the base time is divisible by six hours, 
    *                 i.e. that the time is either 00Z, 06Z, 12Z, or 18Z.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      long        station_id           The numeric identifier of the
    *                                           station to retrieve the temperature
    *                                           data for.
    *   I      int         num_6hr_periods      The number of 6 hour periods to 
    *                                           search over. 
    *   I      time_t      end_base_ticks       The base time search for the
    *                                           temperature infomation from.
    *   O      float       *max_temp            The maximum temperature.
    *   O      float       *min_temp            The minimum temperature.
    *   O      int         *max_temp_qc         Indicates how the maximum
    *                                           was arrived at. 
    *   O      int         *min_temp_qc         Indicates how the minimum was
    *                                           arrived at.
    *                                           
    * RETURNS:
    *   DATA TYPE   NAME                        DESCRIPTION
    *   STATUS      status                      Contains any error or 
    *                                           diagnostic codes generated by 
    *                                           this routine.
    *
    * APIs UTILIZED:
    *   NAME                   HEADER FILE      DESCRIPTION
    *   det_6h_max             N/A              Finds a 6 hour METAR temp max.
    *   det_6h_min             N/A              Finds a 6 hour METAR temp min.
    *
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * ( These are defined in the body of the code. )
    *
    * DATA FILES AND/OR DATABASE:
    * This routine references the following routines in the HM database.
    *      fss_report
    *      fss_contin_real
    *
    * ERROR HANDLING:
    *    ERROR CODE                            DESCRIPTION
    *    STATUS_OK                         This routine ran to completion.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
    *******************************************************************************
    * MODULE NUMBER:   4
    * MODULE NAME:     det_6h_max
    * PURPOSE:         This routine looks for the 1xxxx group which denotes 
    *                  the 6 hour maximum temperature in a METAR. If this group
    *                  cannot be found then this routine determines the 6 hour
    *                  maximum by reading in all of the temperature reports 
    *                  for the six hour period and taking the maximum. 
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      long        station_id           Contains the numeric identifier 
    *                                           of the station to process the 
    *                                           temperature data for.
    *   I      time_t      beg_dtime            The beginning time of the 6 hour
    *                                           period in UNIX ticks.
    *   I      time_t      end_dtime            The ending time of the period
    *                                           in UNIX ticks.
    *   O      float       *maxtemp             The maximum temperature determined
    *                                           by this routine.
    *   O      char        *dqd                 The data quality description
    *                                           associated with the maximum
    *                                           temperature found by this routine.
    *   O      int         *temp_flag           This indicates how the maximum
    *                                           temperature was arrived at.
    *   O      STATUS      *status              Contains any error or diagnostic
    *                                           codes generated by this routine.
    * RETURNS:
    * None.
    *
    * APIs UTILIZED:
    *   NAME                     HEADER FILE      DESCRIPTION
    *   convert_ticks_2_string   AEV_time_util.h  Converts a ticks value to an
    *                                             Informix-compatible string.
    *   det_pd_max               N/A              Determines the max temperature
    *                                             over a period of time.
    *   get_metar_conreal_val    metar_utils.h    Retrieves a continuous real
    *                                             value from the HM database. 
    *
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * ( These are defined in the body of the routine. )
    *
    * DATA FILES AND/OR DATABASE:
    * This routine references the following tables in the HM database:
    *    FSS_report
    *    FSS_contin_real
    *
    * ERROR HANDLING:
    *    ERROR CODE                            DESCRIPTION
    *    STATUS_OK                         This routine ran to completion.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
    *******************************************************************************
    * MODULE NUMBER:   5
    * MODULE NAME:     det_6h_min
    * PURPOSE:      This routine looks for the 2xxxx group which denotes 
    *           the 6 hour minimum temperature in a METAR. If this group
    *           cannot be found then this routine determines the 6 hour
    *           minimum by reading in all of the temperature reports
    *           for the six hour period and then taking the minimum.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      long        station_id           The numeric identifier
    *                                           of the station to retrieve the
    *                                           temperature data for.
    *   I      time_t      beg_dtime            The time (in UNIX ticks) of the
    *                                           start of the period to determine
    *                                           the min in.
    *   I      time_t      end_dtime            The time (in UNIX ticks) of the
    *                                           end of the period to determine
    *                                           the min in.
    *   O      float       *mintemp             The minimum temperature determined
    *                                           by this routine.
    *   O      char        *dqd                 The data quality descriptor flag 
    *                                           associated with the data value
    *                                           taken from the data tables in the
    *                                           HM database. 
    *   O      int         *temp_flag           Contains the numeric code that
    *                                           indicates from where a temperature
    *                                           value has come from.
    *   O      STATUS      *status              Contains any error or diagnostic
    *                                           codes generated by this routine.
    *
    * RETURNS:
    *  None.
    *
    * APIs UTILIZED:
    *   NAME                       HEADER FILE       DESCRIPTION
    *   convert_ticks_2_string     AEV_time_util.h   Converts a time in UNIX ticks
    *                                                into an Informix-compatible
    *                                                string.
    *   det_pd_min                 N/A               Determines the min temperature
    *                                                for a given time period.
    *   get_metar_conreal_val      metar_utils.h     Reads a continuous real value
    *                                                from the HM database.
    *
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * ( These are defined in the body of this code. )
    *
    * DATA FILES AND/OR DATABASE:
    * The following tables in the HM database are referenced by this routine:
    *     FSS_report
    *     FSS_contin_real
    *
    * ERROR HANDLING:
    *    ERROR CODE                            DESCRIPTION
    *    STATUS_OK                         This routine ran to completion.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
     * </pre>
     * 
     * @param window
     * @param itype
     * @param dailyClimateData
     *            data to set. Assumed to already have inform ID (station ID)
     *            set.
     */
    private void buildDailyObsTemp(ClimateDates window, PeriodType itype,
            DailyClimateData dailyClimateData) {
        /*
         * Initialize the structure that contains the time of the max/min
         * temperatures (if these were determined via hourly observations).
         */
        ClimateTime maxTime = ClimateTime.getMissingClimateTime();
        ClimateTime minTime = ClimateTime.getMissingClimateTime();

        Calendar beginCal = window.getStart().getCalendarFromClimateDate();
        int beginHour = window.getStartTime().getHour();
        beginCal.set(Calendar.HOUR_OF_DAY, beginHour);
        beginCal.set(Calendar.MINUTE, window.getStartTime().getMin());
        long beginBaseMilliTicks = beginCal.getTimeInMillis();

        if (window.getStartTime().getMin() > 44) {
            beginBaseMilliTicks += TimeUtil.MILLIS_PER_HOUR;
            beginHour++;

            if (beginHour >= TimeUtil.HOURS_PER_DAY) {
                beginHour = 0;
            }
        }

        Calendar endCal = window.getEnd().getCalendarFromClimateDate();
        int endHour = window.getEndTime().getHour();
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, window.getEndTime().getMin());
        long endBaseMilliTicks = endCal.getTimeInMillis();

        /* Compute the base start time. */
        int beginOffset = 0;
        if ((beginHour % 6) != 0) {
            /* The begin time is not "in sync" with GMT time. */
            beginOffset = 6 - (beginHour % 6);
            beginBaseMilliTicks += beginOffset * TimeUtil.MILLIS_PER_HOUR;
        }

        /* Compute the base end time. */
        int endOffset = 0;
        if ((endHour % 6) != 0) {
            /* The end time is not "in sync" with GMT time. */
            endOffset = endHour % 6;
            endBaseMilliTicks -= endOffset * TimeUtil.MILLIS_PER_HOUR;
        }

        /* Determine the length of the time period being computed. */
        int num6HourPeriods = (int) ((endBaseMilliTicks - beginBaseMilliTicks)
                / ClimateUtilities.MILLISECONDS_IN_SIX_HOURS);

        /*
         * Check to see if the 24 hour maximum and minimum temperature groups
         * are present. We will check in a 3 hr window to make sure we don't
         * miss the report.
         */
        float maxTemp = dailyClimateData.getMaxTemp();
        float minTemp = dailyClimateData.getMinTemp();

        if (maxTemp == (-1 * ClimateCreatorDAO.R_MISS)) {
            maxTemp = ClimateCreatorDAO.R_MISS;
        }

        if (minTemp == (-1 * ClimateCreatorDAO.R_MISS)) {
            minTemp = ClimateCreatorDAO.R_MISS;
        }

        try {
            if (PeriodType.MORN_NWWS.equals(itype)
                    || PeriodType.MORN_RAD.equals(itype)) {
                if (maxTemp == ClimateCreatorDAO.R_MISS
                        || minTemp == ClimateCreatorDAO.R_MISS) {
                    /*
                     * From #get_24hr_maxmin_temp
                     */
                    long adjustEndBaseMilliTicks = endBaseMilliTicks
                            + (TimeUtil.MILLIS_PER_HOUR * endOffset);

                    for (int j = -1; (j <= 4)
                            && (maxTemp == ClimateCreatorDAO.R_MISS); j++) {
                        long nominalMilliTicks = adjustEndBaseMilliTicks
                                + (TimeUtil.MILLIS_PER_HOUR * j);

                        Calendar nominalCal = TimeUtil.newCalendar();
                        nominalCal.setTimeInMillis(nominalMilliTicks);
                        String nominalTimeString = ClimateDate
                                .getFullDateTimeFormat()
                                .format(nominalCal.getTime());

                        FSSReportResult result = climateCreatorDAO
                                .getMetarConreal(dailyClimateData.getInformId(),
                                        MetarUtils.METAR_24HR_MAXTEMP,
                                        nominalTimeString);

                        if (!result.isMissing()) {
                            maxTemp = (float) result.getValue();
                        } else {
                            logger.warn("The [" + nominalTimeString
                                    + "]Z maximum 24-hour temperature report is missing.");
                        }
                    }

                    for (int j = -1; (j <= 4)
                            && (minTemp == ClimateCreatorDAO.R_MISS); j++) {
                        long nominalMilliTicks = adjustEndBaseMilliTicks
                                + (TimeUtil.MILLIS_PER_HOUR * j);

                        Calendar nominalCal = TimeUtil.newCalendar();
                        nominalCal.setTimeInMillis(nominalMilliTicks);
                        String nominalTimeString = ClimateDate
                                .getFullDateTimeFormat()
                                .format(nominalCal.getTime());

                        FSSReportResult result = climateCreatorDAO
                                .getMetarConreal(dailyClimateData.getInformId(),
                                        MetarUtils.METAR_24HR_MINTEMP,
                                        nominalTimeString);

                        if (!result.isMissing()) {
                            minTemp = (float) result.getValue();
                        } else {
                            logger.warn("The [" + nominalTimeString
                                    + "]Z minimum 24-hour temperature report is missing.");
                        }
                    }
                    /*
                     * End #get_24hr_maxmin_temp
                     */

                    if (maxTemp != ClimateCreatorDAO.R_MISS && dailyClimateData
                            .getDataMethods()
                            .getMaxTempQc() != ClimateCreatorDAO.R_MISS) {
                        dailyClimateData.getDataMethods()
                                .setMaxTempQc(QCValues.TEMP_FROM_24);
                    }

                    if (minTemp != ClimateCreatorDAO.R_MISS && dailyClimateData
                            .getDataMethods()
                            .getMinTempQc() != ClimateCreatorDAO.R_MISS) {
                        dailyClimateData.getDataMethods()
                                .setMinTempQc(QCValues.TEMP_FROM_24);
                    }
                }
            }

            if (maxTemp == ClimateCreatorDAO.R_MISS
                    || minTemp == ClimateCreatorDAO.R_MISS) {
                /*
                 * The 24 hour temperature information was not present for the
                 * max and/or min temperature. Use the 6 hourly max/min info to
                 * do this.
                 */
                /*
                 * From #get_6hr_maxmin_temp
                 */
                if (maxTemp == ClimateCreatorDAO.R_MISS) {
                    for (int j = (num6HourPeriods - 1); j >= 0; j--) {
                        /*
                         * Check to see if there was a max temperature reported
                         * for this 6 hour period.
                         */
                        long adjustEndMilliTime = endBaseMilliTicks
                                - (ClimateUtilities.MILLISECONDS_IN_SIX_HOURS
                                        * j);
                        long adjustBeginMilliTime = adjustEndMilliTime
                                - ClimateUtilities.MILLISECONDS_IN_SIX_HOURS;

                        boolean foundResult = false;
                        float value = 0;
                        int qcFlag = 0;
                        ClimateTime maxExtremeTime = ClimateTime
                                .getMissingClimateTime();

                        /*
                         * From #det_6h_max
                         */
                        Calendar adjustEndTimeCal = TimeUtil.newCalendar();
                        adjustEndTimeCal.setTimeInMillis(adjustEndMilliTime);
                        String adjustEndTimeString = ClimateDate
                                .getFullDateTimeFormat()
                                .format(adjustEndTimeCal.getTime());
                        /*
                         * try to get the value of the max temperature field for
                         * the time period's end time metar
                         */
                        FSSReportResult tempFrom6Result = climateCreatorDAO
                                .getMetarConreal(dailyClimateData.getInformId(),
                                        MetarUtils.METAR_6HR_MAXTEMP,
                                        adjustEndTimeString);

                        if (!tempFrom6Result.isMissing()) {
                            value = (float) tempFrom6Result.getValue();
                            qcFlag = QCValues.TEMP_FROM_6;
                            foundResult = true;
                        } else {
                            /*
                             * no max temperature, so determine the highest
                             * temperature of the time period's metars and
                             * specis
                             */
                            ClimateCreatorDAO.ExtremeTempPeriodResult result = determinePeriodMax(
                                    dailyClimateData.getInformId(),
                                    adjustBeginMilliTime, adjustEndMilliTime);
                            if (!result.isMissing()) {
                                value = result.getTemp();
                                qcFlag = result.getFlag();
                                maxExtremeTime = result.getTime();
                                foundResult = true;
                            } else {
                                logger.warn(
                                        "Could not get temperature max between ticks ["
                                                + adjustBeginMilliTime
                                                + "] and [" + adjustEndMilliTime
                                                + "] for station ID ["
                                                + dailyClimateData.getInformId()
                                                + "].");
                            }
                        }
                        /*
                         * End #det_6h_max
                         */
                        if (foundResult && (value >= maxTemp
                                || maxTemp == ClimateCreatorDAO.R_MISS)) {
                            maxTemp = value;
                            dailyClimateData.getDataMethods()
                                    .setMaxTempQc(qcFlag);
                            if (qcFlag == QCValues.TEMP_FROM_HOURLY
                                    || qcFlag == QCValues.TEMP_FROM_WHOLE_HOURLY) {
                                maxTime = new ClimateTime(maxExtremeTime);
                            }
                        }
                    }
                }

                if (minTemp == ClimateCreatorDAO.R_MISS) {
                    for (int j = (num6HourPeriods - 1); j >= 0; j--) {
                        /*
                         * Check to see if there was a min temperature reported
                         * for this 6 hour period.
                         */
                        long adjustEndTimeMilli = endBaseMilliTicks
                                - (ClimateUtilities.MILLISECONDS_IN_SIX_HOURS
                                        * j);
                        long adjustBeginTimeMilli = adjustEndTimeMilli
                                - ClimateUtilities.MILLISECONDS_IN_SIX_HOURS;

                        boolean foundResult = false;
                        float value = 0;
                        int qcFlag = 0;

                        ClimateTime minExtremeTime = ClimateTime
                                .getMissingClimateTime();

                        /*
                         * From #det_6h_min
                         */
                        Calendar adjustEndTimeCal = TimeUtil.newCalendar();
                        adjustEndTimeCal.setTimeInMillis(adjustEndTimeMilli);
                        String adjustEndTimeString = ClimateDate
                                .getFullDateTimeFormat()
                                .format(adjustEndTimeCal.getTime());
                        /*
                         * try to get the value of the max temperature field for
                         * the time period's end time metar
                         */
                        FSSReportResult tempFrom6Result = climateCreatorDAO
                                .getMetarConreal(dailyClimateData.getInformId(),
                                        MetarUtils.METAR_6HR_MINTEMP,
                                        adjustEndTimeString);

                        if (!tempFrom6Result.isMissing()) {
                            value = (float) tempFrom6Result.getValue();
                            qcFlag = QCValues.TEMP_FROM_6;
                            foundResult = true;
                        } else {
                            /*
                             * no min temperature, so determine the lowest
                             * temperature of the time period's metars and
                             * specis
                             */
                            ClimateCreatorDAO.ExtremeTempPeriodResult result = determinePeriodMin(
                                    dailyClimateData.getInformId(),
                                    adjustBeginTimeMilli, adjustEndTimeMilli);
                            if (!result.isMissing()) {
                                value = result.getTemp();
                                qcFlag = result.getFlag();
                                minExtremeTime = result.getTime();
                                foundResult = true;
                            } else {
                                logger.warn(
                                        "Could not get temperature min between ticks ["
                                                + adjustBeginTimeMilli
                                                + "] and [" + adjustEndTimeMilli
                                                + "] for station ID ["
                                                + dailyClimateData.getInformId()
                                                + "].");
                            }
                        }
                        /*
                         * End #det_6h_min
                         */

                        if (foundResult && (value <= minTemp
                                || minTemp == ClimateCreatorDAO.R_MISS)) {
                            minTemp = value;
                            dailyClimateData.getDataMethods()
                                    .setMinTempQc(qcFlag);
                            if (qcFlag == QCValues.TEMP_FROM_HOURLY
                                    || qcFlag == QCValues.TEMP_FROM_WHOLE_HOURLY) {
                                minTime = new ClimateTime(minExtremeTime);
                            }
                        }
                    }
                }
                /*
                 * End #get_6hr_maxmin_temp
                 */

                /*
                 * Check to see if there are any remaining "leading" or
                 * "tailing" hours to process.
                 */
                if (beginOffset != 0) {
                    long endDTimeMilli = beginBaseMilliTicks;
                    long beginDTimeMilli = beginBaseMilliTicks
                            - (TimeUtil.MILLIS_PER_HOUR * beginOffset);

                    ClimateCreatorDAO.ExtremeTempPeriodResult maxResult = determinePeriodMax(
                            dailyClimateData.getInformId(), beginDTimeMilli,
                            endDTimeMilli);
                    if (!maxResult.isMissing()) {
                        float maxValue = maxResult.getTemp();
                        int maxFlag = maxResult.getFlag();
                        ClimateTime maxExtremeTime = maxResult.getTime();
                        if ((ClimateUtilities.floatingEquals(maxValue, maxTemp)
                                && (dailyClimateData.getDataMethods()
                                        .getMaxTempQc() != QCValues.TEMP_FROM_6
                                        && dailyClimateData.getDataMethods()
                                                .getMaxTempQc() != QCValues.TEMP_FROM_DSM))
                                || maxValue > maxTemp
                                || ClimateUtilities.floatingEquals(maxTemp,
                                        ClimateCreatorDAO.R_MISS)) {
                            maxTemp = maxValue;
                            dailyClimateData.getDataMethods()
                                    .setMaxTempQc(maxFlag);
                            maxTime = new ClimateTime(maxExtremeTime);
                        }
                    } else {
                        logger.warn(
                                "Could not get temperature max between ticks ["
                                        + beginDTimeMilli + "] and ["
                                        + endDTimeMilli + "] for station ID ["
                                        + dailyClimateData.getInformId()
                                        + "].");
                    }

                    ClimateCreatorDAO.ExtremeTempPeriodResult minResult = determinePeriodMin(
                            dailyClimateData.getInformId(), beginDTimeMilli,
                            endDTimeMilli);
                    if (!minResult.isMissing()) {
                        float minValue = minResult.getTemp();
                        int minFlag = minResult.getFlag();
                        ClimateTime minExtremeTime = minResult.getTime();
                        if ((ClimateUtilities.floatingEquals(minValue, minTemp)
                                && (dailyClimateData.getDataMethods()
                                        .getMinTempQc() != QCValues.TEMP_FROM_6
                                        && dailyClimateData.getDataMethods()
                                                .getMinTempQc() != QCValues.TEMP_FROM_DSM))
                                || minValue < minTemp
                                || ClimateUtilities.floatingEquals(minTemp,
                                        ClimateCreatorDAO.R_MISS)) {
                            minTemp = minValue;
                            dailyClimateData.getDataMethods()
                                    .setMinTempQc(minFlag);
                            minTime = new ClimateTime(minExtremeTime);
                        }
                    } else {
                        logger.warn(
                                "Could not get temperature min between ticks ["
                                        + beginDTimeMilli + "] and ["
                                        + endDTimeMilli + "] for station ID ["
                                        + dailyClimateData.getInformId()
                                        + "].");
                    }
                }

                /* Process the "tailing" hours. */
                if (endOffset != 0) {
                    long endDTimeMilli = endBaseMilliTicks;
                    long beginDTimeMilli = endBaseMilliTicks
                            + (TimeUtil.MILLIS_PER_HOUR * endOffset);

                    ClimateCreatorDAO.ExtremeTempPeriodResult maxResult = determinePeriodMax(
                            dailyClimateData.getInformId(), beginDTimeMilli,
                            endDTimeMilli);
                    if (!maxResult.isMissing()) {
                        float maxValue = maxResult.getTemp();
                        int maxFlag = maxResult.getFlag();
                        ClimateTime maxExtremeTime = maxResult.getTime();
                        if ((ClimateUtilities.floatingEquals(maxValue, maxTemp)
                                && (dailyClimateData.getDataMethods()
                                        .getMaxTempQc() != QCValues.TEMP_FROM_6
                                        && dailyClimateData.getDataMethods()
                                                .getMaxTempQc() != QCValues.TEMP_FROM_DSM))
                                || maxValue > maxTemp
                                || ClimateUtilities.floatingEquals(maxTemp,
                                        ClimateCreatorDAO.R_MISS)) {
                            maxTemp = maxValue;
                            dailyClimateData.getDataMethods()
                                    .setMaxTempQc(maxFlag);
                            maxTime = new ClimateTime(maxExtremeTime);
                        }
                    } else {
                        logger.warn(
                                "Could not get temperature max between ticks ["
                                        + beginDTimeMilli + "] and ["
                                        + endDTimeMilli + "] for station ID ["
                                        + dailyClimateData.getInformId()
                                        + "].");
                    }

                    ClimateCreatorDAO.ExtremeTempPeriodResult minResult = determinePeriodMin(
                            dailyClimateData.getInformId(), beginDTimeMilli,
                            endDTimeMilli);
                    if (!minResult.isMissing()) {
                        float minValue = minResult.getTemp();
                        int minFlag = minResult.getFlag();
                        ClimateTime minExtremeTime = minResult.getTime();
                        if ((ClimateUtilities.floatingEquals(minValue, minTemp)
                                && (dailyClimateData.getDataMethods()
                                        .getMinTempQc() != QCValues.TEMP_FROM_6
                                        && dailyClimateData.getDataMethods()
                                                .getMinTempQc() != QCValues.TEMP_FROM_DSM))
                                || minValue < minTemp
                                || ClimateUtilities.floatingEquals(minTemp,
                                        ClimateCreatorDAO.R_MISS)) {
                            minTemp = minValue;
                            dailyClimateData.getDataMethods()
                                    .setMinTempQc(minFlag);
                            minTime = new ClimateTime(minExtremeTime);
                        }
                    } else {
                        logger.warn(
                                "Could not get temperature min between ticks ["
                                        + beginDTimeMilli + "] and ["
                                        + endDTimeMilli + "] for station ID ["
                                        + dailyClimateData.getInformId()
                                        + "].");
                    }
                }
            }

            if (dailyClimateData.getMaxTemp() == Math
                    .abs(ClimateCreatorDAO.I_MISS)) {
                if (maxTemp == ClimateCreatorDAO.R_MISS) {
                    /*
                     * Legacy errored out of remaining work. Use normal flow
                     * control and log instead.
                     */
                    logger.warn(
                            "Built maximum temperature was the invalid value ["
                                    + ClimateCreatorDAO.R_MISS
                                    + "] and current maximum temperature is missing.");
                } else {
                    maxTemp = (float) ClimateUtilities
                            .celsiusToFahrenheit(maxTemp);
                    dailyClimateData.setMaxTemp(ClimateUtilities.nint(maxTemp));
                    if (dailyClimateData.getDataMethods()
                            .getMaxTempQc() == QCValues.TEMP_FROM_HOURLY
                            || dailyClimateData.getDataMethods()
                                    .getMaxTempQc() == QCValues.TEMP_FROM_WHOLE_HOURLY) {
                        dailyClimateData.setMaxTempTime(maxTime);
                    }
                }
            }

            if (dailyClimateData.getMinTemp() == Math
                    .abs(ClimateCreatorDAO.I_MISS)) {
                if (minTemp == ClimateCreatorDAO.R_MISS) {
                    /*
                     * Legacy errored out of remaining work. Use normal flow
                     * control and log instead.
                     */
                    logger.warn(
                            "Built minimum temperature was the invalid value ["
                                    + ClimateCreatorDAO.R_MISS
                                    + "] and current minimum temperature is missing.");
                } else {
                    minTemp = (float) ClimateUtilities
                            .celsiusToFahrenheit(minTemp);

                    dailyClimateData.setMinTemp(ClimateUtilities.nint(minTemp));
                    if (dailyClimateData.getDataMethods()
                            .getMinTempQc() == QCValues.TEMP_FROM_HOURLY
                            || dailyClimateData.getDataMethods()
                                    .getMinTempQc() == QCValues.TEMP_FROM_WHOLE_HOURLY) {
                        dailyClimateData.setMinTempTime(minTime);
                    }
                }
            }
        } catch (ClimateQueryException e) {
            logger.error(
                    "Error with querying while building daily temperature observations",
                    e);
        }
    }

    /**
     * Migrated from build_daily_obs_climo.f
     * 
     * <pre>
     *
    *   June 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the retrieval and construction of
    *             the daily observed climatology.  It loops on the number 
    *             of stations and builds the observed climatology one
    *             station at a time, one weather element at a time.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date            - derived TYPE that contains valid date for
    *                            this climate summary retrieval
    *        climate_stations  - derived TYPE which contains the station ids
    *                            and plain language stations names for the
    *                            stations in this climate summary
    *        itype             - flag which controls the type of climate
    *                            summary being generated;
    *                            =1  NWR morning daily climate summary
    *                            =2  NWR evening daily climate summary
    *                            =3  NWWS morning daily climate summary
    *                            =4  NWWS evening daily climate summary
    *                            =5  NWR monthly radio climate summary
    *                            =6  NWWS monthly climate summary
    *                            =7  NWWS annual climate summary
    *        num_stations      - number of stations in this group
    *        valid_time        - derived TYPE which contains the valid time
    *                            (i.e., ending time) for this climate summary
    *
    *      Output
    *        yesterday         - derived TYPE that contains the observed climate
    *                            data for a given set of stations
    *        qc                - derived TYPE that contains the qc flags
    *
    *      Local
    *        begin_date       - derived TYPE that contains the starting
    *                           date for data retrieval
    *        begin_time       - derived TYPE that contains the starting
    *                           time for data retrieval
    *        end_date         - derived TYPE that contains the ending
    *                           date for data retrieval
    *        end_time         - derived TYPE that contains the ending
    *                           time for data retrieval
    *        i                - loop counter
    *        ihour            - hour of the day 
    *
    *
    *      Non-system routines used
    *
    *      Non-system functions used
    *        build_daily_obs_precip  - builds the precipitation climatology
    *        build_daily_obs_rh      - builds the relative humidity climato
    *        build_daily_obs_sky     - builds the sky condition climatology
    *        build_daily_obs_snow    - builds the snow climatology
    *        build_daily_obs_sun     - builds the percent sunshine
    *        build_daily_obs_temp    - builds the temperature climatology
    *        build_daily_obs_weather - builds the weather climatology
    *        build_daily_obs_wind    - builds the wind climatology
    *        determine_window        - determines the window for data
    *                                  retrieval; adjusts for differences
    *                                  between local and UTC
    *
    *  MODIFICATION HISTORY  
    *     NAME                DATE    CHANGE
    *     Dave Miller        7/19/99  Added routine to retrieve values from 
    *                                 the ASOS DSM climate table.  Added checks
    *                                 in this routine to see if the particular 
    *                                 climate parameter is missing.  If not, 
    *                                 skip that subroutine.
    *     Doug Murphy       12/12/00  Added extra assumption for snowfall -
    *                                 if precip total is 0 or the min temp
    *                                 doesn't fall below 50, snowfall is 0.0
    *                                 (SPR 1752/DR 6899)
    *     Doug Murphy        1/30/01  Removed call to build_daily_obs_snow.
    *                                 Added snow depth and avg wind qc methods.
    *
    *     Baoyu Yin          12/26/07 Added snow depth retrieval from DSM for
    *                                 morning/evening/intermediate daily climate.
     * </pre>
     * 
     * @param aDate
     * @param climateStations
     * @param itype
     * @param numStations
     * @param validTime
     *            valid time.
     * @param yesterdaysDatas
     *            empty daily data list.
     * @throws ClimateException
     */
    private void buildDailyObsClimo(ClimateDate aDate,
            List<Station> climateStations, PeriodType itype,
            ClimateTime validTime, List<DailyClimateData> yesterdaysDatas)
                    throws ClimateException {

        // Loop on the stations for which to retrieve/build
        // observed climatology
        for (int i = 0; i < climateStations.size(); i++) {
            Station currStation = climateStations.get(i);

            // Set the Informix id from the station
            DailyClimateData yesterday = DailyClimateData
                    .getMissingDailyClimateData();
            yesterday.setInformId(currStation.getInformId());

            DailyDataMethod yesterdayQC = yesterday.getDataMethods();

            // Determine the window for retrieving the observations.
            // NOTE: the starting and ending times are UTC!!!!

            ClimateDates window = DailyClimateDAO.determineWindow(aDate,
                    currStation, itype, validTime);

            // *************************************************************************
            // 7-19-99
            // Routine retrieves the weather values from ASOS daily summary
            // message
            // *************************************************************************
            try {
                dailyClimateDao.retrieveDailySummary(aDate, yesterday, itype,
                        window.getEndTime(), currStation.getNumOffUTC());
            } catch (ClimateQueryException e) {
                logger.error("Error with DSM query. Some data will be missing.",
                        e);
            }

            // Build/retrieve the daily observed temperature climatology
            // First check to see if max and min temps

            if (yesterday.getMaxTemp() != ParameterFormatClimate.MISSING) {
                yesterdayQC.setMaxTempQc(QCValues.TEMP_FROM_DSM);
            } else {
                yesterdayQC.setMaxTempQc(ParameterFormatClimate.MISSING);
            }

            if (yesterday.getMinTemp() != ParameterFormatClimate.MISSING) {
                yesterdayQC.setMinTempQc(QCValues.TEMP_FROM_DSM);
            } else {
                yesterdayQC.setMinTempQc(ParameterFormatClimate.MISSING);
            }

            if ((yesterday.getMaxTemp() == ParameterFormatClimate.MISSING)
                    || (yesterday
                            .getMinTemp() == ParameterFormatClimate.MISSING)) {

                buildDailyObsTemp(window, itype, yesterday);
            }

            // If the time the max and min was reported, convert that time to
            // local time, IF max/min are not from the DSM. Insure that the
            // hours are only within 0-23!
            if (yesterdayQC.getMaxTempQc() != QCValues.TEMP_FROM_DSM) {
                if (yesterday.getMaxTempTime()
                        .getHour() != ParameterFormatClimate.MISSING_HOUR) {
                    int ihour = yesterday.getMaxTempTime().getHour();
                    ihour = ihour + currStation.getNumOffUTC();
                    if (ihour < 0) {
                        ihour = ihour + TimeUtil.HOURS_PER_DAY;
                    }
                    if (ihour >= TimeUtil.HOURS_PER_DAY) {
                        ihour = ihour - TimeUtil.HOURS_PER_DAY;
                    }
                    yesterday.getMaxTempTime().setHour(ihour);
                }
            }

            if (yesterdayQC.getMinTempQc() != QCValues.TEMP_FROM_DSM) {

                if (yesterday.getMinTempTime()
                        .getHour() != ParameterFormatClimate.MISSING_HOUR) {
                    int ihour = yesterday.getMinTempTime().getHour();
                    ihour = ihour + currStation.getNumOffUTC();
                    if (ihour < 0) {
                        ihour = ihour + TimeUtil.HOURS_PER_DAY;
                    }
                    if (ihour >= TimeUtil.HOURS_PER_DAY) {
                        ihour = ihour - TimeUtil.HOURS_PER_DAY;
                    }
                    yesterday.getMinTempTime().setHour(ihour);
                }
            }

            // Build/retrieve the daily observed precipitation climatology
            if (yesterday
                    .getPrecip() != ParameterFormatClimate.MISSING_PRECIP) {
                yesterdayQC.setPrecipQc(QCValues.PRECIP_FROM_DSM);
            } else {
                buildDailyObsPrecip(window, yesterday);
            }
            // Build/retrieve the daily observed snow climatology
            if (yesterday.getSnowDay() != ParameterFormatClimate.MISSING_SNOW) {
                yesterdayQC.setSnowQc(QCValues.SNOW_FROM_DSM);
            } else {
                yesterdayQC.setSnowQc(ParameterFormatClimate.MISSING);
            }

            float snowGround = ParameterFormatClimate.MISSING_SNOW;
            if (yesterday
                    .getSnowGround() != ParameterFormatClimate.MISSING_SNOW) {
                yesterdayQC.setDepthQc(QCValues.SNOW_FROM_DSM);
                snowGround = yesterday.getSnowGround();
            } else {
                yesterdayQC.setDepthQc(ParameterFormatClimate.MISSING);
            }
            if ((yesterdayQC.getSnowQc() == ParameterFormatClimate.MISSING)
                    || (yesterdayQC
                            .getDepthQc() == ParameterFormatClimate.MISSING)) {
                computeDailySnow(window, itype, yesterday);
            }
            // Using snow_ground from DSM if snow_ground is not missing.
            if (snowGround != ParameterFormatClimate.MISSING_SNOW) {
                yesterday.setSnowGround(snowGround);
                yesterdayQC.setDepthQc(QCValues.SNOW_FROM_DSM);
            }
            // 12/12/00 - One final quality check on snow total
            // Snow will be assumed to be 0 if the precip total is 0
            // or the minimum temp did not fall below 50 degrees
            if (yesterday.getSnowDay() == ParameterFormatClimate.MISSING_SNOW) {
                if ((yesterday.getPrecip() == 0)
                        || ((yesterday.getMinTemp() > 50) && (yesterday
                                .getMinTemp() != ParameterFormatClimate.MISSING))) {
                    yesterday.setSnowDay(0);
                    yesterdayQC.setSnowQc(QCValues.SNOW_ASSUMED);
                }
            }

            // Build/retrieve the daily observed RH climatology
            buildDailyObsRh(window, itype, yesterday);

            // Build/retrieve the daily observed wind climatology
            if (yesterday.getMaxWind()
                    .getDir() != ParameterFormatClimate.MISSING) {
                yesterdayQC.setMaxWindQc(QCValues.MAX_WIND_FROM_DSM);
            } else {
                yesterdayQC.setMaxWindQc(ParameterFormatClimate.MISSING);
            }

            if (yesterday.getMaxGust()
                    .getDir() != ParameterFormatClimate.MISSING) {
                yesterdayQC.setMaxGustQc(QCValues.MAX_GUST_FROM_DSM);
            } else {
                yesterdayQC.setMaxGustQc(ParameterFormatClimate.MISSING);
            }
            if (yesterday
                    .getAvgWindSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                yesterdayQC.setAvgWindQc(QCValues.AVG_WIND_FROM_DSM);
            } else {
                yesterdayQC.setAvgWindQc(ParameterFormatClimate.MISSING);
            }
            buildDailyObsWind(window, itype, currStation.getNumOffUTC(),
                    yesterday);

            // Build/retrieve the daily observed sky condition climatology
            if (yesterday.getSkyCover() != ParameterFormatClimate.MISSING) {
                // Legacy incorrectly set value as SNOW FROM DSM
                yesterdayQC.setSkyCoverQc(QCValues.SKY_COVER_FROM_DSM);
            } else {
                climateCreatorDAO.getSkyCover(window, yesterday, yesterdayQC);
            }

            // Build the % possible sunshine
            if (yesterday
                    .getPercentPossSun() != ParameterFormatClimate.MISSING) {
                yesterdayQC.setPossSunQc(QCValues.POSS_SUN_FROM_DSM);
            } else {
                yesterdayQC.setPossSunQc(ParameterFormatClimate.MISSING);
            }
            if (yesterday.getMinutesSun() != ParameterFormatClimate.MISSING) {
                yesterdayQC.setMinSunQc(QCValues.MIN_SUN_FROM_DSM);
            } else {
                yesterdayQC.setMinSunQc(ParameterFormatClimate.MISSING);
            }

            if ((yesterdayQC.getPossSunQc() == ParameterFormatClimate.MISSING)
                    || (yesterdayQC
                            .getMinSunQc() == ParameterFormatClimate.MISSING)) {
                buildDailyObsSun(aDate, currStation, yesterday);
            }

            // Build/retrieve the daily observed weather type climatology
            if (yesterday.getNumWx() > 0) {
                yesterdayQC.setWeatherQc(QCValues.WX_FROM_DSM);
            } else if (yesterday.getNumWx() == 0) {
                yesterdayQC.setWeatherQc(QCValues.WX_BORING);
            } else {
                yesterdayQC.setWeatherQc(ParameterFormatClimate.MISSING);
            }
            climateCreatorDAO.buildDailyObsWeather(window, yesterday,
                    yesterdayQC);

            // add daily datas
            yesterdaysDatas.add(yesterday);
        }

    }

    /**
     * Migrated from rise_and_set.f
     * 
     * <pre>
     *   June 1998     Jason P. Tuell        PRC/TDL
    *   June 1999     Jason P. Tuell        PRC/TDL
    *               - Modified the info_file structure to allow two
    *                   days worth of SR/SS data to be created
    *
    *   Purpose:  This routine controls calculating sunrise and sunset for 
    *             the stations in the climate summary.  The date for which
    *             sunrise and sunset are calculated vary according to the 
    *             type of climate summary.  The morning climate summary 
    *             provides the sunrise and sunset for the current day, i.e.,
    *             the day after the day for which the climate summary is valid.
    *             The sunrise and sunset for the evening climate summary are for
    *             the following day.
    *
    *             NOTE:  There will be stations at higher latitudes 
    *                    for which there will be be no sunrise or sunset.
    *                    For example, stations above the Arctic circle
    *                    will not have a sunrise or sunset at dates near the 
    *                    summer or winter solstice.  The sunrise and sunset
    *                    will be reported as missing for these cases.
    *        
    *
    *   Variables
    *
    *      Input
    *        a_date           - derived TYPE that defines the valid date of
    *                           the climate summary
    *        climate_stations - derived TYPE that contains the stations for
    *                           this climate summary
    *        itype            - type of climate summary;
    *                           =1 morning NWR daily climate summary
    *                           =2 evening NWR daily climate summary
    *                           =3 morning NWWS daily climate summary
    *                           =4 evening NWWS daily climate summary
    *                           =5 monthly NWR climate summary
    *                           =6 monthly NWWS climate summary
    *                           =7 annual climate summary
    *        num_stations     - number of stations in this climate summary
    *
    *      Output
    *        sunrise          - derived TYPE that contains the times of sunrise 
    *                           for the stations in this climate summary
    *        sunset           - derived TYPE that contains the times of sunset
    *                           for the stations in this climate summary           
    *
    *
    *      Local
    *        dlat             - decimal station latititude
    *        dlon             - decimal station longitude
    *        i                - loop index
    *        iday             - Julian day for which to calculate sunrise 
    *                           and sunset
    *        is_dst           - flag returned from HWRUtils::stdTime.
    *                           = 0; daylight savings time in effect at this time
    *                           = 1; standard time in effect at this time
    *        num_off_UTC      - hours off of UTC
    *        ssday            - fractional day offset of input date for sunset
    *
    *
    *      Non-system routines used
    *        convert_fractional_date  - converts fractional days into local time
    *        rise_set                 - library routine from HWR that returns
    *                                   sunrise and sunset in terms of 
    *                                   fractional days off the input date
    *
    *      Non-system functions used
    *        julday           - returns Julian day for an input date
    *
    *     MODIFICATION HISTORY
    *     --------------------
    *       12/7/00    Doug Murphy           Call to determine DST now properly 
    *                                        use the sunrise/sunset dates (s_date),
    *                                        instead of the run date (a_date)
    *       5/21/01    Doug Murphy           Added section which sets time zone
    *       6/22/01    Doug Murphy           Had to move where DST is determined.
    *                                        sunrise/sunset routine now requires
    *                                        num_off_UTC and calculates the local
    *                                        time of the sunrises/sunsets for us.
    *
    *     Sept, 2007  Mohammed Sikder      Code Modified for accommodating modified Alaska
    *                                     Time Zone. DR_19422
     * </pre>
     * 
     * @param aDate
     * @param climateStations
     * @param sunrise
     *            list of sunrise arrays for the stations. Assumed empty at this
     *            point. Each list entry will have a length 2 array after
     *            processing.
     * @param sunset
     *            list of sunset arrays for the stations. Assumed empty at this
     *            point. Each list entry will have a length 2 array after
     *            processing.
     * @param itype
     * @throws ClimateInvalidParameterException
     */
    private static void riseAndSet(ClimateDate aDate,
            List<Station> climateStations, List<ClimateTime[]> sunrise,
            List<ClimateTime[]> sunset, PeriodType itype)
                    throws ClimateInvalidParameterException {
        // Loop on the number of stations and calculate sunrise and sunset
        // for each station
        for (int i = 0; i < climateStations.size(); i++) {
            ClimateTime[] currSunriseTime = new ClimateTime[] {
                    ClimateTime.getMissingClimateTime(),
                    ClimateTime.getMissingClimateTime() };
            sunrise.add(currSunriseTime);

            ClimateTime[] currSunsetTime = new ClimateTime[] {
                    ClimateTime.getMissingClimateTime(),
                    ClimateTime.getMissingClimateTime() };
            sunset.add(currSunsetTime);

            boolean isDst;
            Station currStation = climateStations.get(i);
            double dlat = currStation.getDlat();
            double dlon = currStation.getDlon();

            for (int j = 0; j <= 1; j++) {
                /*
                 * Determine the date for which to calculate sunrise, sunset
                 * indexing in Legacy started at 1; calculations offset here to
                 * compensate
                 */
                int iday;
                switch (itype) {
                case MORN_NWWS:
                case MORN_RAD:
                    // Get next day of year with offset
                    iday = aDate.julday() + j + 1;
                    break;
                default:
                    // Get current day of year with offset
                    iday = aDate.julday() + j;
                    break;
                }

                ClimateDate sDate = new ClimateDate(aDate);
                // Set calculation date for next or current day of year with
                // offset
                sDate.convertJulday(iday);

                if (currStation.getStdAllYear() == 0) {
                    isDst = ClimateUtilities.determineDaylightSavings(sDate,
                            currStation.getNumOffUTC());
                } else {
                    isDst = false;
                }

                /*
                 * Legacy documentation:
                 * 
                 * Call the library routine that calculates sunrise and sunset
                 * Note that sunrise and sunset are returned as fractional days
                 * offset from the input date. isun =1 for sunrise, isun=0 for
                 * sunset
                 * 
                 * If NOT observing standard time on this day (flag = 0) then
                 * (or, observing daylight savings time) adjust time.
                 */
                int adjustedNumOffUTC = currStation.getNumOffUTC();
                if (isDst) {
                    adjustedNumOffUTC++;
                }

                SunLib.setSun(sDate, dlat, dlon, adjustedNumOffUTC,
                        currSunsetTime[j]);

                SunLib.riseSun(sDate, dlat, dlon, adjustedNumOffUTC,
                        currSunriseTime[j]);

                /*
                 * Legacy documentation:
                 * 
                 * If NOT observing standard time on this day (flag = 0) then
                 * (or, observing daylight savings time) adjust time.
                 */
                // Set sunrise and sunset time zones
                StringBuilder timeZone;
                if (isDst) {
                    timeZone = new StringBuilder(
                            DAYLIGHT_SAVINGS_TIME_ZONE_SUFFIX);
                } else {
                    timeZone = new StringBuilder(STANDARD_TIME_ZONE_SUFFIX);
                }

                switch (currStation.getNumOffUTC()) {
                case -4:
                    // Atlantic time
                    timeZone.insert(0, ATLANTIC_TIME_ZONE_PREFIX);
                    break;
                case -5:
                    // Eastern time
                    timeZone.insert(0, EASTERN_TIME_ZONE_PREFIX);
                    break;
                case -6:
                    // Central time
                    timeZone.insert(0, CENTRAL_TIME_ZONE_PREFIX);
                    break;
                case -7:
                    // Mountain time
                    timeZone.insert(0, MOUNTAIN_TIME_ZONE_PREFIX);
                    break;
                case -8:
                    // Pacific time
                    timeZone.insert(0, PACIFIC_TIME_ZONE_PREFIX);
                    break;
                case -9:
                    // Alaskan time, DR 19422
                    timeZone.insert(0, ALASKAN_TIME_ZONE_PREFIX);
                    break;
                case -10:
                    // Hawaiian time
                    timeZone.insert(0, HAWAIIAN_TIME_ZONE_PREFIX);
                    break;
                case -11:
                    // Samoan time
                    timeZone.insert(0, SAMOAN_TIME_ZONE_PREFIX);
                    break;
                case 10:
                    // Guam time, DR 18744
                    timeZone.insert(0, CHAMORRO_TIME_ZONE_PREFIX);
                    break;
                default:
                    // Local time
                    timeZone.insert(0, LOCAL_TIME_ZONE_PREFIX);
                    break;
                }

                currSunriseTime[j].setZone(timeZone.toString());
                currSunsetTime[j].setZone(timeZone.toString());
            }
        }
    }

    /**
     * Migrated from get_climo_from_last_year.f
     * 
     * <pre>
     *   July 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine retrieves the observed climo data for this
    *             date last year.  It takes into account leap years because
    *             there is no data from the previous year for 29 Feb.  It 
    *             uses 28 Feb from the previous years for the 29 Feb in
    *             leap years.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date           - derived TYPE that contains date for this
    *                           climate summary
    *        climate_stations - derived TYPE that contains the stations 
    *                           for this climate summary
    *        num_stations     - number of stations in this climate summary
    *       
    *
    *      Output
    *        last_year        - derived TYPE that contains the observed
    *                           climo data for this date last year
    *        last_year_qc     - derived TYPE that contains the qc flags
    *
    *      Local
    *        cool_season      - derived TYPE that defines the date for the
    *                           start of the cooling season
    *        cool_year        - derived TYPE that defines the date for the
    *                           start of the cooling year
    *        heat_season      - derived TYPE that defines the date for the
    *                           start of the heating season
    *        heat_year        - derived TYPE that defines the date for the
    *                           start of the heating year
    *        i                - loop counter
    *        l_date           - derived TYPE that contains the date for 
    *                           last year
    *        precip_season    - derived TYPE that defines the date for the
    *                           start of the precipitation season
    *        precip_year      - derived TYPE that defines the date for the
    *                           start of the precipitation year
    *        snow_season      - derived TYPE that defines the date for the
    *                           start of the snow season
    *        snow_season      - derived TYPE that defines the date for the
    *                           start of the snow year
    *
    *      Non-system routines used
    *        get_last year    - retrieve's the observed climo for 
    *                           a given station and l_date
    *        set_season       - This routine defines the dates for the start
    *                           period for seasonal and annual accumulation 
    *                           periods.
    *
    *      Non-system functions used
    *        leap   - Returns TRUE if input year is a leap year
     * </pre>
     * 
     * @param aDate
     * @param climateStations
     * @param lastYear
     *            empty data list that will be filled out in this method.
     * @throws Exception
     */
    private void getClimoFromLastYear(ClimateDate aDate,
            List<Station> climateStations, List<DailyClimateData> lastYear)
                    throws Exception {
        ClimateDates coolSeason = ClimateDates.getMissingClimateDates();
        ClimateDates coolYear = ClimateDates.getMissingClimateDates();
        ClimateDates heatSeason = ClimateDates.getMissingClimateDates();
        ClimateDates heatYear = ClimateDates.getMissingClimateDates();
        ClimateDates precipSeason = ClimateDates.getMissingClimateDates();
        ClimateDates precipYear = ClimateDates.getMissingClimateDates();
        ClimateDates snowSeason = ClimateDates.getMissingClimateDates();
        ClimateDates snowYear = ClimateDates.getMissingClimateDates();

        /*
         * Legacy documentation:
         * 
         * We need to handle leap years as special cases. You can't simply
         * retrieve data for the previous year when the date is the 29th of
         * February, because it doesn't exist.
         *
         * Use the 28 February for 29 February in leap years when retrieving
         * last year's climatology.
         * 
         *
         * Determine the date for retrieving last year's observed climo data
         */
        ClimateDate lastYearDate = new ClimateDate(aDate);
        lastYearDate.setYear(lastYearDate.getYear() - 1);
        if (aDate.isLeapYear() && aDate.getMon() == 2 && aDate.getDay() == 29) {
            lastYearDate.setDay(28);
        }

        // Retrieve and reset current day of year to account for leap year
        // offset
        int iday = lastYearDate.julday();
        lastYearDate.convertJulday(iday);

        /*
         * Now set the seasonal and yearly dates for the cumulative parameters
         */
        ClimateDAOUtils.setSeason(lastYearDate, coolSeason, coolYear,
                heatSeason, heatYear, precipSeason, precipYear, snowSeason,
                snowYear);

        // Now loop and retrieve the climatology one station at a time.
        for (int i = 0; i < climateStations.size(); i++) {
            int informID = climateStations.get(i).getInformId();

            DailyClimateData data = DailyClimateData
                    .getMissingDailyClimateData();
            data.setInformId(informID);

            lastYear.add(data);

            dailyClimateDao.getLastYear(lastYearDate, informID, data);

            getHeatLast(lastYearDate, informID, heatSeason, heatYear, data);

            getCoolLast(lastYearDate, informID, coolSeason, coolYear, data);

            getPrecipLast(lastYearDate, informID, precipSeason, precipYear,
                    data);

            getSnowLast(lastYearDate, informID, snowSeason, snowYear, data);
        }
    }

    /**
     * converted from get_daily_his_climo.f
     * 
     * @param aDate
     * @param climateStations
     * @param numStations
     * @param yClimate
     * @param tClimate
     * @throws Exception
     */
    private void getDailyHisClimo(ClimateDate yDate,
            List<Station> climateStations, List<ClimateRecordDay> yClimate,
            List<ClimateRecordDay> tClimate) throws Exception {

        int iday;

        // First calculate the date for retrieving tomorrow's
        // normals. First set the dates equal, so the base year
        // is set
        ClimateDate tDate = new ClimateDate(yDate);

        // Get next day of year from yDate
        iday = yDate.julday() + 1;

        // Set tDate to next year of day from yDate
        tDate.convertJulday(iday);

        // Loop on the stations for which to retieve/build
        // observed climatology. Get the historical normals
        // for both y_date and t_date.
        for (int i = 0; i < climateStations.size(); i++) {
            int stationID = climateStations.get(i).getInformId();

            yClimate.add(
                    climateDailyNormDao.getHistoricalNorms(yDate, stationID));

            tClimate.add(
                    climateDailyNormDao.getHistoricalNorms(tDate, stationID));
        }

        // Now calculate the accumulated historical data
        sumHisNorms(yDate, climateStations, yClimate);
    }

    /**
     * Migrated from sum_his_norms.f
     * 
     * <pre>
    *   Jan 1999     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This subroutine controls the calculation of the derived
    *             fields from the historical data base.  
    *             These fields include heating and cooling degree
    *             days, cumulative totals of snow, precipitation, heating 
    *             cooling degree days, etc.  Derived fields are listed 
    *             below:
    *
    *             precip
    *               precip_month      monthly accumulated precip
    *               precip_season     seasonal accumulated precip
    *               precip_year       yearly accumulated precip
    *
    *             snow
    *               snow_month        monthly accumulated snow
    *               snow_season       seassonal accumulated snow
    *               snow_year         yearly accumulated snow
    *
    *             num_heat            heating degree days yesterday
    *             num_heat_month      monthly accumulated heating degree days
    *             num_heat_season     seasonal accumulated heating degree days
    *             num_heat_year       yearly accumulated heating degree days
    *
    *             num_cool            cooling degree days yesterday
    *             num_cool_month      monthly accumulated cooling degree days
    *             num_cool_season     seasonal accumulated cooling degree days
    *             num_cool_year       yearly accumulated cooling degree days
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date           - derived TYPE that contains the date for this
    *                           climate summary
    *        climate_stations - derived TYPE that contains the stations (and 
    *                           associated data) in this climate summary
    *        num_stations     - number of stations in this climate summary
    *        y_climate        - derived TYPE that contains the historical climate
    *                           data
    *
    *      Output
    *
    *
    *      Local
    *        cool_season      - derived TYPE that defines the date for the
    *                           start of the cooling season
    *        cool_year        - derived TYPE that defines the date for the
    *                           start of the cooling year
    *        heat_season      - derived TYPE that defines the date for the
    *                           start of the heating season
    *        heat_year        - derived TYPE that defines the date for the
    *                           start of the heating year
    *        precip_season    - derived TYPE that defines the date for the
    *                           start of the precipitation season
    *        precip_year      - derived TYPE that defines the date for the
    *                           start of the precipitation year
    *        snow_season      - derived TYPE that defines the date for the
    *                           start of the snow season
    *        snow_season      - derived TYPE that defines the date for the
    *                           start of the snow year
    *        i               - loop counter
    *
    *      Non-system routines used
    *        set_season      - set the dates that define the start of the season and 
    *                          year for various parameters
    *        update_his_cool - sum the cooling degree day climate data
    *        update_his_heat -  update the heating degree day climate data
    *        update_his_precip - update the precipitation climate data
    *        update_his_snow - update the snowfall climate data
    *
    *      Non-system functions used
     *
     * </pre>
     * 
     * @param aDate
     * @param climateStations
     * @param yClimate
     * @throws Exception
     */
    private void sumHisNorms(ClimateDate aDate, List<Station> climateStations,
            List<ClimateRecordDay> yClimate) throws Exception {
        // Now set the seasonal and yearly dates for the cumulative
        // parameters
        ClimateDates coolSeason = ClimateDates.getMissingClimateDates();
        ClimateDates coolYear = ClimateDates.getMissingClimateDates();
        ClimateDates heatSeason = ClimateDates.getMissingClimateDates();
        ClimateDates heatYear = ClimateDates.getMissingClimateDates();
        ClimateDates precipSeason = ClimateDates.getMissingClimateDates();
        ClimateDates precipYear = ClimateDates.getMissingClimateDates();
        ClimateDates snowSeason = ClimateDates.getMissingClimateDates();
        ClimateDates snowYear = ClimateDates.getMissingClimateDates();

        // Now set the seasonal and yearly dates for the cumulative
        // parameters
        ClimateDAOUtils.setSeason(aDate, coolSeason, coolYear, heatSeason,
                heatYear, precipSeason, precipYear, snowSeason, snowYear);

        // Loop on the number of stations for the daily climate summaries.
        // Calculate the accumulated values for the heating and cooling degree
        // days, precipitation and snowfall.

        for (int i = 0; i < climateStations.size(); i++) {
            int stationId = climateStations.get(i).getInformId();
            updateHisPrecip(aDate, stationId, precipSeason, precipYear,
                    yClimate.get(i));
            updateHisSnow(aDate, stationId, snowSeason, snowYear,
                    yClimate.get(i));

            updateHisHeat(aDate, stationId, heatSeason, heatYear,
                    yClimate.get(i));

            updateHisCool(aDate, stationId, coolSeason, coolYear,
                    yClimate.get(i));
        }

    }

    /**
     * Migrated from update_his_cool.f
     * 
     * <pre>
    *   August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             historical cooling degree days.  It determines the monthly 
    *             accumulated historical cooling degree days first, followed by the
    *             seasonal and lastly the annual historical cooling degree days.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date         - derived TYPE that contains the date for this
    *                         climate summary
    *        inform_id      - INFORMIX station id
    *        cool_season    - derived TYPE that contains the begin and end
    *                         dates for the cooling degree day season
    *        cool_year      - derived TYPE that contains the begin and end
    *                         dates for the cooling degree day year
    *        y_climate      - derived TYPE that holds the historical climate
    *                         data for this station
    *
    *      Output
    *        y_climate      - derived TYPE that holds the daily historical climate
    *                         data for this station
    *
    *      Local
    *
    *      Non-system routines used
    *
    *
    *
    *      Non-system functions used
    *        sum_his_cool   - calculates the accumulated cooling degree days
    *                         from the historical data base
    *   Modifications  
    *   May 1999        David T. Miller       PRC/TDL
    *   January 2001    Doug Murphy           PRC/MDL
    *                   sum_his_cool is now able to sum using dates that span
    *                   the first of the year....removed those sections from this
    *                   routine which called the sum routine twice in these cases
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param coolSeason
     * @param coolYear
     * @param yClimate
     */
    private void updateHisCool(ClimateDate aDate, int stationId,
            ClimateDates coolSeason, ClimateDates coolYear,
            ClimateRecordDay yClimate) {
        ClimateDate first = new ClimateDate(aDate);
        ClimateDate last = new ClimateDate(aDate);

        // First do the monthly total. We need to consider leap years
        // for summing in the month of February

        first.setDay(1);

        yClimate.setNumCoolMonth(
                climatePeriodNormDAO.sumHisCool(first, last, stationId));

        // Now handle the seasonal summations. This is a little
        // more involved since we need to take into account seasons
        // that cross yearly boundaries.
        first = coolSeason.getStart();

        yClimate.setNumCoolSeason(
                climatePeriodNormDAO.sumHisCool(first, last, stationId));

        // Now do the annual cooling degree days
        // We have the same potential problems as with the seasonal
        // cool

        first = coolYear.getStart();
        yClimate.setNumCoolYear(
                climatePeriodNormDAO.sumHisCool(first, last, stationId));
    }

    /**
     * Migrated from update_his_heat.f
     * 
     * <pre>
    *   August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             historical heating degree days.  It determines the monthly 
    *             accumulated historical heating degree days first, followed by the
    *             seasonal and lastly the annual historical heating degree days.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date         - derived TYPE that contains the date for this
    *                         climate summary
    *        inform_id      - INFORMIX station id
    *        heat_season    - derived TYPE that contains the begin and end
    *                         dates for the heating degree day season
    *        heat_year      - derived TYPE that contains the begin and end
    *                         dates for the heating degree day year
    *        y_climate      - derived TYPE that holds the historical climate
    *                         data for this station
    *
    *      Output
    *        y_climate      - derived TYPE that holds the daily historical climate
    *                         data for this station
    *
    *      Local
    *
    *      Non-system routines used
    *
    *
    *
    *      Non-system functions used
    *        sum_his_heat   - calculates the accumulated heating degree days
    *                         from the historical data base
    *     Modifications
    *     May 1999         David T. Miller                PRC/TDL
    *     January 2001     Doug Murphy                    PRC/MDL
    *                      sum_his_heat is now able to sum using dates that span
    *                      the first of the year....removed those sections from this
    *                      routine which called the sum routine twice in these cases
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param heatSeason
     * @param heatYear
     * @param yClimate
     */
    private void updateHisHeat(ClimateDate aDate, int stationId,
            ClimateDates heatSeason, ClimateDates heatYear,
            ClimateRecordDay yClimate) {
        ClimateDate first = new ClimateDate(aDate);
        ClimateDate last = new ClimateDate(aDate);

        // First do the monthly total. We need to consider leap years
        // for summing in the month of February

        first.setDay(1);

        yClimate.setNumHeatMonth(
                climatePeriodNormDAO.sumHisHeat(first, last, stationId));

        // Now handle the seasonal summations. This is a little
        // more involved since we need to take into account seasons
        // that cross yearly boundaries.
        first = heatSeason.getStart();
        // First handle the case where there isn't a problem
        // across the first day of the year

        yClimate.setNumHeatSeason(
                climatePeriodNormDAO.sumHisHeat(first, last, stationId));
        // Now do the annual heating degree days
        // We have the same potential problems as with the seasonal
        // heat

        first = heatYear.getStart();
        yClimate.setNumHeatYear(
                climatePeriodNormDAO.sumHisHeat(first, last, stationId));

    }

    /**
     * Migrated from update_his_snow.f
     * 
     * <pre>
    *   January 1999     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             historical snowfall.  It determines the monthly 
    *             accumulated historical snowfall first, followed by the
    *             seasonal and lastly the annual historical snowfall.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date         - derived TYPE that contains the date for this
    *                         climate summary
    *        inform_id      - INFORMIX station id
    *        snow_season    - derived TYPE that contains the begin and end
    *                         dates for the heating degree day season
    *        snow_year      - derived TYPE that contains the begin and end
    *                         dates for the heating degree day year
    *        y_climate      - derived TYPE that holds the historical climate
    *                         data for this station
    *
    *      Output
    *        y_climate      - derived TYPE that holds the daily historical climate
    *                         data for this station
    *
    *      Local
    *
    *      Non-system routines used
    
    *
    *
    *      Non-system functions used
    *
    *      Modifications
    *      May 1999             David T. Miller             PRC/TDL
    *      January 2001         Doug Murphy                 PRC/MDL
    *                           sum_his_snow is now able to sum using dates that span
    *                           the first of the year....removed those sections from this
    *                           routine which called the sum routine twice in these cases
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param snowSeason
     * @param snowYear
     * @param yClimate
     */
    private void updateHisSnow(ClimateDate aDate, int stationId,
            ClimateDates snowSeason, ClimateDates snowYear,
            ClimateRecordDay yClimate) {
        ClimateDate first = new ClimateDate(aDate);
        ClimateDate last = new ClimateDate(aDate);

        // First do the monthly total. We need to consider leap years
        // for summing in the month of February

        first.setDay(1);
        yClimate.setSnowMonthMean(
                climatePeriodNormDAO.sumHisSnow(first, last, stationId));

        // Now handle the seasonal summations. This is a little
        // more involved since we need to take into account seasons
        // that cross yearly boundaries.
        first = snowSeason.getStart();
        yClimate.setSnowSeasonMean(
                climatePeriodNormDAO.sumHisSnow(first, last, stationId));

        // Now do the annual snow
        // We have the same potential problems as with the seasonal
        // snow

        first = snowYear.getStart();
        yClimate.setSnowYearMean(
                climatePeriodNormDAO.sumHisSnow(first, last, stationId));
    }

    /**
     * Migrated from update_his_precip.f
     * 
     * <pre>
    *   January 1999     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the determination of accumulated
    *             historical precipitation.  It determines the monthly 
    *             accumulated historical precipitation first, followed by the
    *             seasonal and lastly the annual historical precipitation.
    * 
    *
    *   Variables
    *
    *      Input
    *        a_date         - derived TYPE that contains the date for this
    *                         climate summary
    *        inform_id      - INFORMIX station id
    *        precip_season  - derived TYPE that contains the begin and end
    *                         dates for the precipitation season
    *        precip_year    - derived TYPE that contains the begin and end
    *                         dates for the precipitation year
    *        y_climate      - derived TYPE that holds the historical climate
    *                         data for this station
    *
    *      Output
    *        current        - derived TYPE that holds the daily historical climate
    *                         data for this station
    *
    *      Local
    *
    *      Non-system routines used
    *
    *
    *
    *      Non-system functions used
    *
    *                                date
    *      Modifications
    *      May 1999          David T. Miller             PRC/TDL
    *      January 2001      Doug Murphy                 PRC/MDL
    *                        sum_his_precip is now able to sum using dates that span
    *                        the first of the year....removed those sections from this
    *                        routine which called the sum routine twice in these cases
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param precipSeason
     * @param precipYear
     * @param yClimate
     */
    private void updateHisPrecip(ClimateDate aDate, int stationId,
            ClimateDates precipSeason, ClimateDates precipYear,
            ClimateRecordDay yClimate) {
        // First do the monthly total. We need to consider leap years
        // for summing in the month of February
        ClimateDate first = new ClimateDate(aDate);
        ClimateDate last = new ClimateDate(aDate);

        first.setDay(1);

        yClimate.setPrecipMonthMean(
                climatePeriodNormDAO.sumHisPrecip(first, last, stationId));

        // Now handle the seasonal summations. This is a little
        // more involved since we need to take into account seasons
        // that cross yearly boundaries.
        first = precipSeason.getStart();
        yClimate.setPrecipSeasonMean(
                climatePeriodNormDAO.sumHisPrecip(first, last, stationId));

        // Now do the annual precipitation
        // We have the same potential problems as with the seasonal
        // precip

        first = precipYear.getStart();
        yClimate.setPrecipYearMean(
                climatePeriodNormDAO.sumHisPrecip(first, last, stationId));
    }

    /**
     * Migrated from get_dew.c.
     * 
     * <pre>
     * void int get_dew     (   climate_date    *this_date,
    *                           climate_time       *now_time,
    *               long               *station_id
    *                 
    *                       )
    *
    *   Jason Tuell        PRC/TDL             HP 9000/7xx
    *   Dan Zipper                 PRC/TDL
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *  This function retrieves up to 24 hours of hourly temperatures
    *      for the period btween begin date and time and end date and time.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
    *      this_date           - the date for climate retrieval
    *      now_time            - the current time including the hour for which metar retrieval is used
    *      station_id          - station id of type int for which this function
    *                is called
    *
    *    Output
    *      dew_pt              - The function get_dew will return an integer dew_pt.
    *
    *    Local
    *    char
    *      max_db_dqd          - Array of data quality descriptor returned by metar retrieval functions
    *      nominal_dtime       - A character string which holds the date and nominal hour in a
    *                            "yyyy-dd-mm hh" format.
    *    float 
    *      peak_wind           - The holding variable for a returned value in the metar retrieval function
    *      peak_time           - The holding variable for a returned value in the metar retrieval function
    *    int
    *      db_status           - The success/failure code returned by dbUtils and Informix functions.
    *      dew_pt              - The value returned by the function
    *      hour_dew            - The variable name for a value returned by a metar function.
    *      max_db_status       - Integer array which holds the Informix SQL-code error checks. (See below) 
    *      time_status         - Used to convert time and date to an INFORMIX readable string.
    *
    *    struct
    *      now_tmtime          - the holding structure required for use when using the mktime function to
    *                             convert to a readable time for the begining time. 
    *  POSSIBLE STATUS VALUES
    *  ======================
    *
    *    STATUS_OK             The desired value was successfully found and
    *                            returned.
    *    STATUS_FAILURE        The desired value was not found.
    *    CURSOR_OPEN_ERROR     Informix encountered trouble declaring and/or
    *                            opening a "cursor".
    *    NO_HITS               No rows satisfied the "condition".
    *    MULTIPLE_HITS         More than one row satisfied the "condition".
    *    SELECT_ERROR          An Informix SELECT failed.
    *    STATUS_BUG            An undiagnosed problem occurred; the function
    *                            therefore aborted.
    *    (these are included from STATUS.h)
    *  
    * Modification Log
    * Name                    Date         Change
    * Bob Morris              Feb 2003     - Get rid of call to nominal_time()
    *                                        in favor of unused convert_ticks...
    *                                      - Reordered "get" logic to make sense
     * </pre>
     * 
     * @param date
     * @param time
     * @param stationID
     * @return
     */
    private int getDew(ClimateDate date, ClimateTime time, int stationID) {
        Calendar cal = date.getCalendarFromClimateDate();
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMin());

        String datetime = ClimateDate.getFullDateTimeFormat()
                .format(cal.getTime());

        int dewpoint;
        try {
            /*
             * This function will retrieve the dew_pt reported during a single
             * hour Using the max_db_status as an error check of the INFORMIX
             * database, it will check to see the data is missing, not reported,
             * or if there was another type of error. Once the error status has
             * been determined, a quality check done on the data to see whether
             * or not the data is legitimate.
             */
            FSSReportResult result = climateCreatorDAO.getMetarConreal(
                    stationID, MetarUtils.METAR_DEWPOINT_2_TENTHS, datetime);

            if (result.isMissing()) {
                dewpoint = ParameterFormatClimate.MISSING;
            } else {
                dewpoint = (int) (ClimateUtilities
                        .celsiusToFahrenheit(result.getValue()));
            }
        } catch (ClimateQueryException e) {
            logger.error(
                    "Failed to get dewpoint in tenths from METAR. Attempting whole degrees.",
                    e);
            /*
             * Try for the hourly rounded dew_pts only if we found a METAR
             * report but no dew_point_in_tenths (i.e., STATUS_FAILURE)
             */
            try {
                FSSReportResult result = climateCreatorDAO.getMetarConreal(
                        stationID, MetarUtils.METAR_DEWPOINT, datetime);

                if (result.isMissing()) {
                    dewpoint = ParameterFormatClimate.MISSING;
                } else {
                    dewpoint = (int) (ClimateUtilities
                            .celsiusToFahrenheit(result.getValue()));
                }
            } catch (ClimateQueryException e2) {
                logger.error(
                        "Failed to get dewpoint from METAR in whole degrees. Returning missing value.",
                        e2);
                return ParameterFormatClimate.MISSING;
            }
        }

        /*
         * Legacy checked initial query return against Dew bounds, and second
         * query return against Temp bounds. Assumed to be a bug from copying
         * code from get_temp.c to get_dew.c.
         */
        if (dewpoint == ParameterFormatClimate.MISSING
                || dewpoint < ParameterBounds.MIN_DEW_QC
                || dewpoint > ParameterBounds.MAX_DEW_QC) {
            logger.warn("Dewpoint [" + dewpoint
                    + "] is missing or outside acceptable range. Missing value will be returned.");
            return ParameterFormatClimate.MISSING;
        } else {
            return dewpoint;
        }
    }

    /**
     * Migrated from get_temp.c.
     * 
     * <pre>
     * void int get_temp    (   climate_date    *this_date,
    *                           climate_time       *now_time,
    *               long               *station_id )
    *
    *   Jason Tuell        PRC/TDL             HP 9000/7xx
    *   Dan Zipper                 PRC/TDL
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *  This function retrieves up to 24 hours of hourly temperatures
    *      for the period btween begin date and time and end date and time.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
    *      this_date           - the date for climate retrieval
    *      now_time            - the current time including the hour for which metar retrieval is used
    *      station_id          - station id of type int for which this function
    *                is called
    *
    *    Output
    *      temperature         - The function get_dew will return an integer dew_pt.
    *
    *    Local
    *    char
    *      max_db_dqd          - Array of data quality descriptor returned by metar retrieval functions
    *      nominal_dtime       - A character string which holds the date and nominal hour in a
    *                            "yyyy-dd-mm hh" format.
    *    float 
    *      peak_wind           - The holding variable for a returned value in the metar retrieval function
    *      peak_time           - The holding variable for a returned value in the metar retrieval function
    *    int
    *      db_status           - The success/failure code returned by dbUtils and Informix functions.
    *      temperature         - The value returned by the function
    *      hour_temp           - The variable name for a value returned by a metar function.
    *      max_db_status       - Integer array which holds the Informix SQL-code error checks. (See below) 
    *      time_status         - Used to convert time and date to an INFORMIX readable string.
    *
    *    struct
    *      now_tmtime          - the holding structure required for use when using the mktime function to
    *                             convert to a readable time for the begining time. 
    *  POSSIBLE STATUS VALUES
    *  ======================
    *
    *    STATUS_OK             The desired value was successfully found and
    *                            returned.
    *    STATUS_FAILURE        The desired value was not found.
    *    CURSOR_OPEN_ERROR     Informix encountered trouble declaring and/or
    *                            opening a "cursor".
    *    NO_HITS               No rows satisfied the "condition".
    *    MULTIPLE_HITS         More than one row satisfied the "condition".
    *    SELECT_ERROR          An Informix SELECT failed.
    *    STATUS_BUG            An undiagnosed problem occurred; the function
    *                            therefore aborted.
    *    (these are included from STATUS.h)  
    *
    * Modification Log
    * Name                    Date         Change
    * Bob Morris              Feb 2003     - Get rid of call to nominal_time()
    *                                        in favor of unused convert_ticks...
    *                                      - Re-order "get" logic to make sense
     * </pre>
     * 
     * @param date
     * @param time
     * @param stationID
     * @return
     */
    private int getTemp(ClimateDate date, ClimateTime time, int stationID) {
        Calendar cal = date.getCalendarFromClimateDate();
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMin());

        String datetime = ClimateDate.getFullDateTimeFormat()
                .format(cal.getTime());

        int temperature;
        try {
            /*
             * This function will retrieve the temperature reported during a
             * single hour Using the max_db_status as an error check of the
             * INFORMIX database, it will check to see the data is missing, not
             * reported, or if there was another type of error. Once the error
             * status has been determined, a quality check done on the data to
             * see whether or not the data is legitimate.
             */
            FSSReportResult result = climateCreatorDAO.getMetarConreal(
                    stationID, MetarUtils.METAR_TEMP_2_TENTHS, datetime);

            if (result.isMissing()) {
                temperature = ParameterFormatClimate.MISSING;
            } else {
                temperature = (int) (ClimateUtilities
                        .celsiusToFahrenheit(result.getValue()));
            }

        } catch (ClimateQueryException e) {
            logger.error(
                    "Failed to get temperature in tenths from METAR. Attempting whole degrees.",
                    e);
            /*
             * If nothing was found using the metar_temp_in_tenths position,
             * then we will try to get the hourly temp in whole deg. C
             */
            try {
                FSSReportResult result = climateCreatorDAO.getMetarConreal(
                        stationID, MetarUtils.METAR_TEMP, datetime);

                if (result.isMissing()) {
                    temperature = ParameterFormatClimate.MISSING;
                } else {
                    temperature = (int) (ClimateUtilities
                            .celsiusToFahrenheit(result.getValue()));
                }
            } catch (ClimateQueryException e2) {
                logger.error(
                        "Failed to get temperature from METAR in whole degrees. Returning missing value.",
                        e2);
                return ParameterFormatClimate.MISSING;
            }
        }

        if (temperature == ParameterFormatClimate.MISSING
                || temperature < ParameterBounds.TEMP_LOWER_BOUND
                || temperature > ParameterBounds.TEMP_UPPER_BOUND_QC) {
            logger.warn("Temperature [" + temperature
                    + "] is missing or outside acceptable range. Missing value will be returned.");
            return ParameterFormatClimate.MISSING;
        } else {
            return temperature;
        }
    }

    /**
     * Migrated from compute_daily_snow.ec
     * 
     * <pre>
     * FILENAME:            compute_daily_snow.c
    * FILE DESCRIPTION:
    * NUMBER OF MODULES:   2
    * GENERAL INFORMATION:
    *       MODULE 1:      compute_daily_snow
    *       DESCRIPTION:   Computes the snowfall amount for a station
    *                      using snow reports gleaned from Supplemental Climate
    *                      Data (SCD).
    *       MODULE 2:      get_SCD_snow
    *       DESCRIPTION:   Retrieves a SCD snow amount from the HM database 
    *                      based upon a user-inputted time range.
    *
    * ORIGINAL AUTHOR:     Bryon Lawrence
    * CREATION DATE:       August 30, 1998
    * ORGANIZATION:        TDL / GSC
    * MACHINE:             HP9000
    * COPYRIGHT:
    * DISCLAIMER:
    * MODIFICATION HISTORY:
    *   MODULE #        DATE         PROGRAMMER        DESCRIPTION/REASON
    *          1        8/30/98      Bryon Lawrence    Original Coding
    *                   1/09/01      Doug Murphy       Added check for AUTO METAR
    *                                                  reports - snow depth is not
    *                                                  assumed to be 0 in case of AUTO
    *                   1/30/01      Doug Murphy       Added more robust snow depth
    *                                                  code
    *          2        8/31/98      Bryon Lawrence    Original Coding
    *                   9/29/00      Doug Murphy       Removed extra include file
    *          3        1/09/01      Doug Murphy       Original coding
    *         ALL       1/26/05      Manan Dalal       Ported from Informix to Postgres
    *         *******************************************************************************
    * MODULE NUMBER: 1
    * MODULE NAME: compute_daily_snow
    * PURPOSE:
    *    This routine computes the snow accumulation over a user-specified
    *    period using SCD snow reports. The snow amount is computed between
    *    a user-specified begin and end time. If no SCD reports could
    *    be found then the snow amount is returned with a value of SNOW_MISS. If no
    *    snow actually fell during the period, then a snow amount of 0 is 
    *    returned. Actual snow amounts are reported to the nearest tenth of an
    *    inch.
    *
    *    Note that it is often not possible to compute exactly how much snow
    *    fell in during period based upon SCD data since the local time of 
    *    the station may be offset from the 00, 06, 12, 18 GMT schedule of the 
    *    SCDs.
    *
    *    This routine also tries to find the METAR-reported 12Z snow depth. If 
    *    the 12Z METAR report cannot be found, then this will have a value of
    *    SNOW_MISS. If the report existed but no snow depth was reported, then
    *    the snow depth will be given a value of 0. The 12Z snow depth is also
    *    reported to the nearest tenbth of an inch. 
    *
    *    Assumptions.....
    *    It is the responsibility of the calling routine to establish a connection
    *    with the hm database.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      c_date*     begin_date           The begin year, month, day of
    *                                           the 24 hour period.
    *   I      c_time*     begin_time           The begin hour of the 24 hour
    *                                           period.
    *   I      c_date*     end_date             The end year, month, day of the
    *                                           24 hour period.
    *   I      c_time*     end_time             The end hour of the 24 hour 
    *                                           period.
    *   I      int*        itype                The type of climate report
    *   I      long*       station_id           The numeric identifier of the
    *                                           the station to retrieve the
    *   I      int*        qc                   Method of retrieval for snow amount
    *   I      int*        depth_qc             Retrieval method for snow depth
    *   O      float*      snow_amount          The cumulative snow amount.
    *   O      float*      snow_12Z_depth       The 12Z METAR observed depth 
    *                                           of snow.
    *                                           
    * RETURNS:
    *   None.
    *
    * APIs UTILIZED:
    *   NAME                        HEADER FILE          DESCRIPTION
    *   convert_ticks_2_string      AEV_time_util.h      Converts a time in UNIX
    *                                                    ticks into an INFORMIX
    *                                                    compatible time string.
    *   get_SCD_snow                compute_daily_snow.h Retrieves a snow
    *                                                    accumulation from 
    *                                                    a scheduled SCD report.
    *   get_metar_conreal_val       adapt_utils.h        Searchs the
    *                                                    FSS_contin_real table in
    *                                                    the HM database.
    *   
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * These are defined in the body of the code.
    *
    * DATA FILES AND/OR DATABASE:
    * This routine utilizes the following tables in the hm database:
    * FSS_report, FSS_contin_real.
    *
    * ERROR HANDLING:
    *    ERROR CODE                            DESCRIPTION
    *
    *    STATUS_OK                         The routine executed correctly.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
    *
    *    (Note that all of these error codes are defined in STATUS.h).
    *
    * MACROS
    *    Name                     Header             Description
    * CHECK_FOR_FATAL_ERROR  compute_daily_precip.h  Checks for fatal INFORMIX
    *                                                errors.
    * CREATE_INFORMIX_TIME   AEV_time_util.h         Converts a time value in UNIX
    *                                                ticks to an INFORMIX
    *                                                compatible string.
     * </pre>
     * 
     * @param window
     * @param itype
     * @param data
     *            data to set. Assumed to already have inform ID (station ID)
     *            set.
     */
    private void computeDailySnow(ClimateDates window, PeriodType itype,
            DailyClimateData data) {
        /*
         * Contains the beginning base time in UNIX ticks representation.
         */
        Calendar beginCal = window.getStart().getCalendarFromClimateDate();
        beginCal.set(Calendar.HOUR_OF_DAY, window.getStartTime().getHour());
        beginCal.set(Calendar.MINUTE, window.getStartTime().getMin());
        long beginBaseMilliTicks = beginCal.getTimeInMillis();

        int beginHour = window.getStartTime().getHour();

        if (window.getStartTime().getMin() > 44) {
            beginBaseMilliTicks += TimeUtil.MILLIS_PER_HOUR;
            beginHour++;

            if (beginHour >= TimeUtil.HOURS_PER_DAY) {
                beginHour = 0;
            }
        }

        /*
         * Contains the ending base time in UNIX ticks representation.
         */
        Calendar endCal = window.getEnd().getCalendarFromClimateDate();
        endCal.set(Calendar.HOUR_OF_DAY, window.getEndTime().getHour());
        endCal.set(Calendar.MINUTE, window.getEndTime().getMin());
        long endBaseMilliTicks = endCal.getTimeInMillis();

        int endHour = window.getEndTime().getHour();

        /*
         * Set the hour for which we'll look for a snow depth report. For a
         * morning report, we'll always look at the 12Z report.
         */
        int depthHour;
        if ((PeriodType.MORN_RAD.equals(itype)
                || PeriodType.MORN_NWWS.equals(itype))
                || ((window.getEndTime().getHour() >= 12)
                        && (window.getEndTime().getHour() < 18))) {
            depthHour = 12;
        } else if (window.getEndTime().getHour() < 6) {
            depthHour = 0;
        } else if (window.getEndTime().getHour() < 12) {
            depthHour = 6;
        } else if (window.getEndTime().getHour() < 24) {
            depthHour = 18;
        } else {
            depthHour = ParameterFormatClimate.MISSING;
        }

        /* Find the ticks for the hour previously determined */
        long depthMilliTicks;
        if ((PeriodType.MORN_RAD.equals(itype)
                || PeriodType.MORN_NWWS.equals(itype))
                && (window.getEndTime().getHour() < depthHour)) {
            depthMilliTicks = endBaseMilliTicks
                    - (((TimeUtil.HOURS_PER_DAY + window.getEndTime().getHour())
                            - depthHour) * TimeUtil.MILLIS_PER_HOUR);
        } else {
            depthMilliTicks = endBaseMilliTicks
                    - ((window.getEndTime().getHour() - depthHour)
                            * TimeUtil.MILLIS_PER_HOUR);
        }

        if (window.getEndTime().getMin() > 44) {
            endBaseMilliTicks += TimeUtil.MILLIS_PER_HOUR;
            endHour++;

            if (endHour >= TimeUtil.HOURS_PER_DAY) {
                endHour = 0;
            }
        }

        /*
         * The number of "extra" hours at the beginning of the user-specified
         * period.
         */
        int beginOffset = 0;

        /* Compute the base start time. */
        if (beginHour % 6 != 0) {
            /* The begin time is not "in sync" with GMT time. */
            beginOffset = 6 - (beginHour % 6);
            beginBaseMilliTicks += (beginOffset * TimeUtil.MILLIS_PER_HOUR);
        }

        /*
         * The number of "extra" hours at the end of the user-specified period.
         */
        int endOffset = 0;

        /* Compute the base end time. */
        if (endHour % 6 != 0) {
            /* The end time is not "in sync" with GMT time. */
            endOffset = endHour % 6;
            endBaseMilliTicks -= (endOffset * TimeUtil.MILLIS_PER_HOUR);
        }

        if (data.getDataMethods()
                .getSnowQc() == ParameterFormatClimate.MISSING) {
            /* Determine the length of the time period being computed. */
            int num6HourPeriods = (int) ((endBaseMilliTicks
                    - beginBaseMilliTicks)
                    / ClimateUtilities.MILLISECONDS_IN_SIX_HOURS);

            for (int j = (num6HourPeriods - 1); j >= 0; j--) {
                /*
                 * Check each of the 6 hour GMT periods that apply to see if
                 * there was a snowfall amount reported.
                 */
                /* Create the range of SCD valid times to search over. */
                long upperTimeMilli = endBaseMilliTicks
                        - (ClimateUtilities.MILLISECONDS_IN_SIX_HOURS * j)
                        + SCD_10_HOURS;
                Calendar upperTimeCal = TimeUtil.newCalendar();
                upperTimeCal.setTimeInMillis(upperTimeMilli);
                String upperTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(upperTimeCal.getTime());

                long lowerTimeMilli = endBaseMilliTicks
                        - (ClimateUtilities.MILLISECONDS_IN_SIX_HOURS * j)
                        - SCD_10_HOURS;
                Calendar lowerTimeCal = TimeUtil.newCalendar();
                lowerTimeCal.setTimeInMillis(lowerTimeMilli);
                String lowerTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(lowerTimeCal.getTime());

                try {
                    FSSReportResult result = climateCreatorDAO.getSCDSnow(
                            data.getInformId(), lowerTimeString,
                            upperTimeString);

                    if (!result.isMissing()) {
                        data.getDataMethods().setSnowQc(QCValues.SNOW_FROM_SCD);

                        if ((data
                                .getSnowDay() == ParameterFormatClimate.MISSING_SNOW)
                                || (data.getSnowDay() == 0)) {
                            data.setSnowDay((float) result.getValue());
                        } else {
                            data.setSnowDay((float) (data.getSnowDay()
                                    + result.getValue()));
                        }
                    } else {
                        /*
                         * The report that was supposed to contain the snow data
                         * was not found.
                         */
                        logger.warn("The " + lowerTimeString + "Z to "
                                + upperTimeString
                                + "Z SCD snow report could not be found and is being recorded as missing.");
                    }
                } catch (ClimateQueryException e) {
                    logger.error("Error querying SCD snow.", e);
                    if (data.getSnowDay() == ParameterFormatClimate.MISSING_SNOW) {
                        data.setSnowDay(0);
                        data.getDataMethods().setSnowQc(QCValues.SNOW_FROM_SCD);
                    }
                }
            }
        }

        /* Check to see if the snow depth report exists */
        if ((depthMilliTicks >= beginBaseMilliTicks) && (data.getDataMethods()
                .getDepthQc() == ParameterFormatClimate.MISSING)) {
            Calendar nominalTimeCal = TimeUtil.newCalendar();
            nominalTimeCal.setTimeInMillis(depthMilliTicks);
            String nominalTimeString = ClimateDate.getFullDateTimeFormat()
                    .format(nominalTimeCal.getTime());

            try {
                String correction = climateCreatorDAO
                        .getCorrection(data.getInformId(), nominalTimeString);
                if (correction == null) {
                    logger.warn("No METAR observation found for station ID: ["
                            + data.getInformId() + "] and time: ["
                            + nominalTimeString + "]");
                } else if (!correction.equals("A")) {
                    try {
                        FSSReportResult result = climateCreatorDAO
                                .getMetarConreal(data.getInformId(),
                                        MetarUtils.METAR_SNOW_DEPTH,
                                        nominalTimeString);
                        data.setSnowGround((float) result.getValue());
                    } catch (ClimateQueryException e) {
                        logger.error(
                                "The " + nominalTimeString
                                        + "Z METAR snow depth observation query encountered an error and will be recorded as missing.",
                                e);
                        data.setSnowGround(0);
                    }
                    /*
                     * Set this to -1 for 0Z report, since "Entered Manually"
                     * already uses 0
                     */
                    if (depthHour == 0) {
                        data.getDataMethods().setDepthQc(QCValues.DEPTH_00Z);
                    } else {
                        data.getDataMethods().setDepthQc(depthHour);
                    }
                } else {
                    /* The report is auto .... log a message stating so. */
                    logger.info("The " + nominalTimeString
                            + "Z METAR observation is an auto, meaning snow depth is being recorded as missing.");
                }
            } catch (ClimateQueryException e) {
                logger.error("Error querying for METAR observation.", e);
            }
        }

        if ((data.getSnowGround() == ParameterFormatClimate.MISSING_SNOW)
                && (depthHour != 12) && (depthHour != 0)) {
            /*
             * Couldn't find a 6Z or 18Z snow depth, so we'll try the 0Z or 12Z
             * report.
             */
            depthMilliTicks -= ClimateUtilities.MILLISECONDS_IN_SIX_HOURS;
            depthHour -= 6;

            /*
             * Only want to check when it occurs within the begin and end hours
             * though.
             */
            if (depthMilliTicks >= beginBaseMilliTicks) {
                Calendar nominalTimeCal = TimeUtil.newCalendar();
                nominalTimeCal.setTimeInMillis(depthMilliTicks);
                String nominalTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(nominalTimeCal.getTime());

                try {
                    String correction = climateCreatorDAO.getCorrection(
                            data.getInformId(), nominalTimeString);
                    if (!correction.equals("A")) {
                        try {
                            FSSReportResult result = climateCreatorDAO
                                    .getMetarConreal(data.getInformId(),
                                            MetarUtils.METAR_SNOW_DEPTH,
                                            nominalTimeString);
                            data.setSnowGround((float) result.getValue());
                        } catch (ClimateQueryException e) {
                            logger.error(
                                    "The " + nominalTimeString
                                            + "Z METAR snow depth observation query encountered an error and will be recorded as missing.",
                                    e);
                            data.setSnowGround(0);
                        }
                        /*
                         * Set this to -1 for 0Z report, since
                         * "Entered Manually" already uses 0
                         */
                        if (depthHour == 0) {
                            data.getDataMethods()
                                    .setDepthQc(QCValues.DEPTH_00Z);
                        } else {
                            data.getDataMethods().setDepthQc(depthHour);
                        }
                    } else {
                        /* The report is auto .... log a message stating so. */
                        logger.info("The " + nominalTimeString
                                + "Z METAR observation is an auto, meaning snow depth is being recorded as missing.");
                    }
                } catch (ClimateQueryException e) {
                    logger.error("Error querying for METAR observation.", e);
                }
            }
        }
    }

    /**
     * Migrated from compute_daily_precip.c.
     * 
     * <pre>
     * June 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the retrieval and determination
    *             of the daily observed precipitation.  It calls a routine
    *             that returns the precipitation that fell between
    *             a specified starting and ending time for a given station.
    *             It also sets the precip quality control flag.
    * 
    *
    *   Variables
    *
    *      Input
    *        begin_date         - derived TYPE that contains the starting
    *                             date for the period of this climate summary
    *        begin_date         - derived TYPE that contains the starting
    *                             time for the period of this climate summary
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        end_date           - derived TYPE that contains the ending
    *                             date for the period of this climate summary
    *        end_date           - derived TYPE that contains the ending
    *                             time for the period of this climate summary
    *        inform_id          - INFORMIX id of a climate station
    *
    *      Output
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        precip_qc          - flag which specifies the method used to
    *                             determine the precipitation
    *
    *
    *      Local
    *        precip             - amount of precipitation that fell in a 
    *                             specified period.  Value is returned as
    *                             a real number with precision to the
    *                             hundredths
    *
    *      Non-system routines used
    *        compute_daily_precip - returns the amount of precipitation between
    *                               the starting and ending periods for a given
    *                               station
    ********************************************************************************
    * FILENAME:             compute_daily_precip.c
    * FILE DESCRIPTION:
    * NUMBER OF MODULES:    3
    * GENERAL INFORMATION:
    *   MODULE 1:           compute_daily_amt
    *   DESCRIPTION:        Contains the code to process the observed
    *                       precipitation amount.
    *   MODULE 2:           tally_rain_amount
    *   DESCRIPTION:        Contains the code to maintain a cumulative rain amount
    *                       taking into account special precipitation situations
    *                       such as a trace of rain, etc.
    *   MODULE 3:           get_period_rain_amt
    *   DESCRIPTION:        Contains the code to maintain a cumulative rain 
    *                       amount over a user-specified period.
    * ORIGINAL AUTHOR:      Bryon Lawrence
    * CREATION DATE:        August 23, 1998
    * ORGANIZATION:         GSC / TDL
    * MACHINE:              HP9000
    * COPYRIGHT:
    * DISCLAIMER:
    * MODIFICATION HISTORY:
    *   MODULE #        DATE         PROGRAMMER        DESCRIPTION/REASON
    *          1        8/23/98      Bryon Lawrence    Original Coding
    *          2        8/23/98      Bryon Lawrence    Original Coding
    *          3        8/23/98      Bryon Lawrence    Original Coding
    *          1        1/24/00      Bryon Lawrence    Fixed an error in this
    *                                                  routine that was causing
    *                                                  inflated rain amounts.
    *          3        1/24/00      Bryon Lawrence    Slightly simplified the
    *                                                  logic in this routine.
    ********************************************************************************
    * MODULE NUMBER: 3
    * MODULE NAME:   get_period_rain_amt
    * PURPOSE: Given the upper and lower bounds of a range of hours as offset
    *          from a base time, this routine will determine the rainfall
    *          that accumulated during that time period. If the hour bounds 
    *          denote a period that is before the base time, then the 
    *          the values must be negative. If the hour bounds denote a period
    *          that is after the base time, then the values must be positive.
    *
    *          NOTE: It is the responsibility of the calling routine to
    *                establish a connection with the HM database.
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *      I   int         bottom_time          The lower time bound of the 
    *                                           period to retrieve a rainfall
    *                                           total from. 
    *      I   int         top_time             The upper time bound of the 
    *                                           period to retrieve a rainfall 
    *                                           total from.
    *      I   long        station_id           The identifier of the station that
    *                                           rainfall is being accumulated for.
    *      I   time_t      base_time            The time the above time_bounds are
    *                                           are added to / subtracted from.
    *      O   float       *rain_amount         The rainfall amount determined
    *                                           by this routine over the 
    *                                           specified period for the 
    *                                           specified station.
    *      I   STATUS      *status              Contains any error or 
    *                                           diagnostic codes created in
    *                                           this routine. 
    *                                            
    * RETURNS:
    * None.
    *
    * APIs UTILIZED:
    *   NAME                  HEADER FILE            DESCRIPTION
    *   get_metar_conreal_val metar_utils.h          Retrieves data from
    *                                                the FSS_contin_real table.
    *                                                This is where the rainfall
    *                                                data is stored.
    *   tally_rain_amount     compute_daily_precip.h Adds two rainfall amounts
    *                                                together paying attention
    *                                                too trace amounts, etc.
    * LOCAL DATA ELEMENTS (OPTIONAL):
    *  (These are defined in the body of the code).
    *
    * DATA FILES AND/OR DATABASE:
    * This routine utilizes the following tables in the HM database:
    * FSS_report, FSS_contin_real.
    *
    * ERROR HANDLING:
    *    ERROR CODE            DESCRIPTION
    *    STATUS_OK             The desired value was successfully found and
    *                            returned.
    *    STATUS_FAILURE        The desired value was not found.
    *    CURSOR_OPEN_ERROR     Informix encountered trouble declaring and/or
    *                            opening a "cursor".
    *    NO_HITS               No rows satisfied the "condition".
    *    MULTIPLE_HITS         More than one row satisfied the "condition".
    *    SELECT_ERROR          An Informix SELECT failed.
    *    STATUS_BUG            An undiagnosed problem occurred; the function
    *                            therefore aborted.
    *    (these are included from STATUS.h)
     * </pre>
     * 
     * @param bottomTime
     * @param topTime
     * @param informId
     * @param baseMilliTicks
     * @param precip
     *            current precip value
     * @return new precip value
     */
    private float getPrecipRainAmount(int bottomTime, int topTime, int informId,
            long baseMilliTicks, float precip) {
        try {
            for (int i = topTime; i >= bottomTime; i--) {
                long nominalMilliTicks = baseMilliTicks
                        + (TimeUtil.MILLIS_PER_HOUR * i);

                Calendar nominalTimeCal = TimeUtil.newCalendar();
                nominalTimeCal.setTimeInMillis(nominalMilliTicks);
                String nominalTimeString = ClimateDate.getFullDateTimeFormat()
                        .format(nominalTimeCal.getTime());

                FSSReportResult result = climateCreatorDAO.getMetarConreal(
                        informId, MetarUtils.METAR_1HR_PRECIP,
                        nominalTimeString);

                if (!result.isMissing()) {
                    precip = tallyRainAmount(precip, result.getValue());
                } else {
                    logger.warn("The [" + nominalTimeString
                            + "]Z observed rain amount could not be retrieved for station ID ["
                            + informId + "].");
                }
            }
        } catch (ClimateQueryException e) {
            logger.error("Error with METAR query for hourly precip", e);
        }

        return precip;
    }

    /**
     * Migrated from build_daily_obs_temp.ec
     * 
     * <pre>
     * MODULE NUMBER:  7
    * MODULE NAME:    det_pd_min
    * PURPOSE:        This routine determines the minimum temperature in a 
    *         user-specified time period.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      long        station_id           The numeric identifier of the
    *                                           station to retrieve the temperature
    *                                           information for (as defined in the 
    *                                           station_location table of the
    *                                           HM database).
    *   I      time_t      beg_dtime            The beginning time in UNIX ticks
    *                                           representation of the time 
    *                                           period to find the minimum
    *                                           temperature in.
    *   I      time_t      end_dtime            The ending time in UNIX ticks
    *                                           representation of the time period
    *                                           to find the minimum temperature in.
    *   I      float       *mintemp             The minimum temperature found in
    *                                           this period.
    *   O      char        *dqd                 The data quality descriptor flag
    *                                           associated with the minimum
    *                                           temperature retrieved from the
    *                                           HM database.
    *   O      int         *temp_flag           Contains a numeric code indicating
    *                                           how the minimum temperature was
    *                                           was arrived at.
    *   O      STATUS      *status              Contains any error or diagnostic
    *                                           codes generated by this routine.
    *
    * RETURNS:
    *   None.
    *
    * APIs UTILIZED:
    *   NAME                     HEADER FILE        DESCRIPTION
    *   convert_ticks_2_string   AEV_time_util.h    Converts a time in UNIX ticks
    *                                               representation to an INFORMIX
    *                                               comaptible string.
    *   get_metar_conreal_val    metar_utils.h      Retrieves a continous datum
    *                                               from the FSS_contin_real table
    *                                               in the HM database.
    *   get_pd_metar_min         N/A                This routine finds the minimum
    *                                               temperature for a
    *                                               user-specified period of
    *                                               METARs.
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * ( These are defined in the body of this routine. )
    *
    * DATA FILES AND/OR DATABASE:
    * This routine utilizes the following routines in the HM database:
    *    FSS_report
    *    FSS_contin_real
    *
    * ERROR HANDLING:
    *    ERROR CODE                            DESCRIPTION
    *    STATUS_OK                         This routine ran to completion.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
     * </pre>
     * 
     * @param informId
     * @param beginTimeMilli
     *            in milliseconds
     * @param endTimeMilli
     *            in milliseconds
     * @return
     */
    private ClimateCreatorDAO.ExtremeTempPeriodResult determinePeriodMin(
            int informId, long beginTimeMilli, long endTimeMilli) {
        Calendar beginCal = TimeUtil.newCalendar();
        beginCal.setTimeInMillis(beginTimeMilli);
        String beginTimeString = ClimateDate.getFullDateTimeFormat()
                .format(beginCal.getTime());

        if (beginTimeMilli == endTimeMilli) {
            /* should be only one metar; get its temperature */
            try {
                FSSReportResult tenthsResult = climateCreatorDAO
                        .getMetarConreal(informId,
                                MetarUtils.METAR_TEMP_2_TENTHS,
                                beginTimeString);

                if (!tenthsResult.isMissing()) {
                    return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                            new ClimateTime(beginCal),
                            (float) tenthsResult.getValue(),
                            QCValues.TEMP_FROM_HOURLY);
                }
            } catch (ClimateQueryException e) {
                logger.error(
                        "Error querying METAR for temperature in tenths. Will attempt backup query.",
                        e);
            }

            /* See if the rounded temperature is there. */
            try {
                FSSReportResult result = climateCreatorDAO.getMetarConreal(
                        informId, MetarUtils.METAR_TEMP, beginTimeString);

                if (result.isMissing()) {
                    logger.warn(
                            "Missing results querying METAR for temperature.");
                } else {
                    return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                            new ClimateTime(beginCal),
                            (float) result.getValue(),
                            QCValues.TEMP_FROM_WHOLE_HOURLY);
                }
            } catch (ClimateQueryException e) {
                logger.error(
                        "Error querying METAR for temperature. Will return missing.",
                        e);
            }
        } else {
            /* get the lowest metar temperature for the period */
            ClimateCreatorDAO.ExtremeTempPeriodResult metarResult = climateCreatorDAO
                    .getPeriodMetarMin(informId, beginTimeMilli, endTimeMilli);

            if (metarResult.isMissing()) {
                logger.warn("Could not find METAR result for min temperature.");
            } else {
                /* subtract one hour from end_dtime */
                long nominalEndTimeMilli = endTimeMilli
                        - TimeUtil.MILLIS_PER_HOUR;

                Calendar nominalCal = TimeUtil.newCalendar();
                nominalCal.setTimeInMillis(nominalEndTimeMilli);
                String nominalEndTimeString = ClimateDate
                        .getFullDateTimeFormat().format(nominalCal.getTime());

                /* get the lowest speci temperature for the period */
                ClimateCreatorDAO.ExtremeTempPeriodResult speciResult = climateCreatorDAO
                        .getPeriodSpeciMin(informId, beginTimeString,
                                nominalEndTimeString);

                if (speciResult.isMissing()) {
                    logger.warn(
                            "Could not find SPECI result for min temperature.");
                } else {
                    /*
                     * Select the lower of the metar high and the speci high and
                     * assign the correct time information to the extreme_struct
                     * structure.
                     */
                    if (speciResult.getTemp() < metarResult.getTemp()) {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                new ClimateTime(nominalCal),
                                speciResult.getTemp(), speciResult.getFlag());
                    } else {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                new ClimateTime(beginCal),
                                metarResult.getTemp(), metarResult.getFlag());
                    }
                }
            }
        }

        return new ClimateCreatorDAO.ExtremeTempPeriodResult();
    }

    /**
     * Migrated from compute_daily_precip.c
     * 
     * <pre>
     *  June 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine controls the retrieval and determination
    *             of the daily observed precipitation.  It calls a routine
    *             that returns the precipitation that fell between
    *             a specified starting and ending time for a given station.
    *             It also sets the precip quality control flag.
    * 
    *
    *   Variables
    *
    *      Input
    *        begin_date         - derived TYPE that contains the starting
    *                             date for the period of this climate summary
    *        begin_date         - derived TYPE that contains the starting
    *                             time for the period of this climate summary
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        end_date           - derived TYPE that contains the ending
    *                             date for the period of this climate summary
    *        end_date           - derived TYPE that contains the ending
    *                             time for the period of this climate summary
    *        inform_id          - INFORMIX id of a climate station
    *
    *      Output
    *        data               - derived TYPE that contains the observed
    *                             climate data for a given station
    *        precip_qc          - flag which specifies the method used to
    *                             determine the precipitation
    *
    *
    *      Local
    *        precip             - amount of precipitation that fell in a 
    *                             specified period.  Value is returned as
    *                             a real number with precision to the
    *                             hundredths
    *
    *      Non-system routines used
    *        compute_daily_precip - returns the amount of precipitation between
    *                               the starting and ending periods for a given
    *                               station
    ********************************************************************************
    * FILENAME:             compute_daily_precip.c
    * FILE DESCRIPTION:
    * NUMBER OF MODULES:    3
    * GENERAL INFORMATION:
    *   MODULE 1:           compute_daily_amt
    *   DESCRIPTION:        Contains the code to process the observed
    *                       precipitation amount.
    *   MODULE 2:           tally_rain_amount
    *   DESCRIPTION:        Contains the code to maintain a cumulative rain amount
    *                       taking into account special precipitation situations
    *                       such as a trace of rain, etc.
    *   MODULE 3:           get_period_rain_amt
    *   DESCRIPTION:        Contains the code to maintain a cumulative rain 
    *                       amount over a user-specified period.
    * ORIGINAL AUTHOR:      Bryon Lawrence
    * CREATION DATE:        August 23, 1998
    * ORGANIZATION:         GSC / TDL
    * MACHINE:              HP9000
    * COPYRIGHT:
    * DISCLAIMER:
    * MODIFICATION HISTORY:
    *   MODULE #        DATE         PROGRAMMER        DESCRIPTION/REASON
    *          1        8/23/98      Bryon Lawrence    Original Coding
    *          2        8/23/98      Bryon Lawrence    Original Coding
    *          3        8/23/98      Bryon Lawrence    Original Coding
    *          1        1/24/00      Bryon Lawrence    Fixed an error in this
    *                                                  routine that was causing
    *                                                  inflated rain amounts.
    *          3        1/24/00      Bryon Lawrence    Slightly simplified the
    *                                                  logic in this routine.
    ********************************************************************************
    * MODULE NUMBER:  2
    * MODULE NAME:    tally_rain_amount 
    * PURPOSE:        This routine adds a rain_amount to a "running" total
    *                 taking into account trace amounts, missing values, etc.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I/O    float*      precip_amount        A pointer to the memory location
    *                                           which contains the rain amount
    *                                           accumulated over the period.
    *   I      float       element_value        The rain fall amount to be added
    *                                           to the cumulative rain amount.
    *   I      STATUS      status               Contains the exit status from 
    *                                           the get_metar_conreal_ele routine.
    *                                           This is used to determine how
    *                                           exactly to handle the value in
    *                                           the element_value variable.
    * RETURNS:
    *   None.
    *
    * APIs UTILIZED:
    *   None.
    *
    * LOCAL DATA ELEMENTS (OPTIONAL):
    *
    * DATA FILES AND/OR DATABASE:
    *   None.
    *
    * ERROR HANDLING:
    *   None.
     * </pre>
     * 
     * @param precip
     *            current precip value.
     * @param elementValue
     *            retrieved precip value.
     * @return new precip value to use.
     */
    private static float tallyRainAmount(float precip, double elementValue) {
        if ((elementValue == ParameterFormatClimate.MISSING)
                && (precip == ParameterFormatClimate.MISSING_PRECIP
                        || precip == ClimateCreatorDAO.R_MISS)) {
            return 0;
        } else if (((elementValue == ParameterFormatClimate.TRACE)
                && ((precip == ClimateCreatorDAO.R_MISS) || (precip == 0)
                        || (precip == ParameterFormatClimate.MISSING_PRECIP)))) {
            return ParameterFormatClimate.TRACE;
        } else if (((elementValue != ParameterFormatClimate.TRACE)
                && ((precip == ClimateCreatorDAO.R_MISS)
                        || (precip == ParameterFormatClimate.TRACE)
                        || (precip == ParameterFormatClimate.MISSING_PRECIP)))
                && (elementValue != ParameterFormatClimate.MISSING)) {
            return (float) elementValue;
        } else if ((elementValue != ParameterFormatClimate.TRACE)
                && (elementValue != ParameterFormatClimate.MISSING)) {
            return (float) (precip + elementValue);
        } else {
            return precip;
        }
    }

    /**
     * Migrated from build_daily_obs_temp.ec
     * 
     * <pre>
     * MODULE NUMBER:  6
    * MODULE NAME:    det_pd_max
    * PURPOSE:        This routine determines the maximum temperature in
    *                 a user-specified time period. It does this by looking for
    *                 the hourly temperature groups (Txxxxxxxx) and if these are
    *                 not present then it looks through the rounded temperature 
    *                 groups.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      long        station_id           The numeric identifier of the
    *                                           station to find the maximum and
    *                                           minimum temperature information.
    *   I      time_t      beg_dtime            The beginning time in ticks
    *                                           of the time period.
    *   I      time_t      end_dtime            The ending time in UNIX ticks of
    *                                           of the time period to find the
    *                                           maximum time in.
    *   O      float       *maxtemp             The maximum temperature of the 
    *                                           time period.
    * RETURNS:
    *  None.
    *
    * APIs UTILIZED:
    *   NAME                     HEADER FILE      DESCRIPTION
    *   convert_ticks_2_string   AEV_time_util.h  Converts a time in UNIX ticks
    *                                             into an INFORMIX-compatible
    *                                             time string.
    *   get_metar_conreal_val    metar_utils.h    Retrieves a continous real value
    *                                             from the HM database.
    *   get_pd_metar_max         N/A              This routine determines the
    *                                             maximum temperature from a
    *                                             period of METAR observations. 
    *   
    * LOCAL DATA ELEMENTS (OPTIONAL):
    * ( These are defined in the body of the code. )
    *
    * DATA FILES AND/OR DATABASE:
    * This routine utilizes the following tables in the HM database:
    *      FSS_report
    *      FSS_contin_real
    *
    * ERROR HANDLING:
    *    ERROR CODE                            DESCRIPTION
    *    STATUS_OK                         This routine ran to completion.
    *    MALLOC_ERROR                      Memory could not be dynamically
    *                                      allocated in the
    *                                      get_metar_conreal_val routine.
    *    CURSOR_OPEN_ERROR                 A query cursor could not be opened
    *                                      in the get_metar_conreal_val routine.
    *    MULTIPLE_HITS                     More than one row satisfied the
    *                                      "condition" in a query that was
    *                                      expecting only one row in the
    *                                      get_metar_conreal_val.
    *    SELECT_ERROR                      An Informix select failed in the
    *                                      get_metar_conreal_val routine.
    *    STATUS_BUG                        An undiagnosed problem occurred in the
    *                                      get_metar_conreal_val routine forcing
    *                                      it to abort execution.
     * </pre>
     * 
     * @param informId
     * @param beginTimeMilli
     *            in milliseconds
     * @param endTimeMilli
     *            in milliseconds
     * @return
     */
    private ClimateCreatorDAO.ExtremeTempPeriodResult determinePeriodMax(
            int informId, long beginTimeMilli, long endTimeMilli) {
        Calendar beginCal = TimeUtil.newCalendar();
        beginCal.setTimeInMillis(beginTimeMilli);
        String beginTimeString = ClimateDate.getFullDateTimeFormat()
                .format(beginCal.getTime());

        if (beginTimeMilli == endTimeMilli) {
            /* should be only one metar; get its temperature */
            boolean tenthsFailure = false;
            try {
                FSSReportResult tenthsResult = climateCreatorDAO
                        .getMetarConreal(informId,
                                MetarUtils.METAR_TEMP_2_TENTHS,
                                beginTimeString);

                if (tenthsResult.isMissing()) {
                    tenthsFailure = true;
                } else {
                    return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                            new ClimateTime(beginCal),
                            (float) tenthsResult.getValue(),
                            QCValues.TEMP_FROM_HOURLY);
                }
            } catch (ClimateQueryException e) {
                logger.error(
                        "Error querying METAR for temperature in tenths. Will attempt backup query.",
                        e);
                tenthsFailure = true;
            }

            if (tenthsFailure) {
                /* See if the rounded temperature is there. */
                try {
                    FSSReportResult result = climateCreatorDAO.getMetarConreal(
                            informId, MetarUtils.METAR_TEMP, beginTimeString);

                    if (result.isMissing()) {
                        logger.warn(
                                "Missing results querying METAR for temperature.");
                    } else {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                new ClimateTime(beginCal),
                                (float) result.getValue(),
                                QCValues.TEMP_FROM_WHOLE_HOURLY);
                    }
                } catch (ClimateQueryException e) {
                    logger.error(
                            "Error querying METAR for temperature. Will return missing.",
                            e);
                }
            }
        } else {
            /* get the highest metar temperature for the period */
            ClimateCreatorDAO.ExtremeTempPeriodResult metarResult = climateCreatorDAO
                    .getPeriodMetarMax(informId, beginTimeMilli, endTimeMilli);

            if (metarResult.isMissing()) {
                logger.warn("Could not find METAR result for max temperature.");
            } else {
                /* subtract one hour from end_dtime */
                long nominalEndTimeMilli = endTimeMilli
                        - TimeUtil.MILLIS_PER_HOUR;

                Calendar nominalCal = TimeUtil.newCalendar();
                nominalCal.setTimeInMillis(nominalEndTimeMilli);
                String nominalEndTimeString = ClimateDate
                        .getFullDateTimeFormat().format(nominalCal.getTime());

                /* get the highest speci temperature for the period */
                ClimateCreatorDAO.ExtremeTempPeriodResult speciResult = climateCreatorDAO
                        .getPeriodSpeciMax(informId, beginTimeString,
                                nominalEndTimeString);

                if (speciResult.isMissing()) {
                    logger.warn(
                            "Could not find SPECI result for max temperature.");
                } else {
                    /*
                     * Select the higher of the metar high and the speci high
                     * and assign the correct time information to the
                     * extreme_struct structure.
                     */
                    if (speciResult.getTemp() > metarResult.getTemp()) {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                new ClimateTime(nominalCal),
                                speciResult.getTemp(), speciResult.getFlag());
                    } else {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                new ClimateTime(beginCal),
                                metarResult.getTemp(), metarResult.getFlag());
                    }
                }
            }
        }

        return new ClimateCreatorDAO.ExtremeTempPeriodResult();
    }
}
