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
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateSeason;
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
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
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
 * 02 OCT 2017  38590      amoore      Fix resultant wind calculation.
 * 04 OCT 2017  38800      amoore      Handle possible null correction value from FSS.
 * 04 OCT 2017  38067      amoore      Fix PM/IM delay in data reports.
 * 25 OCT 2017  39814      wpaintsil   Avoiding rounding until necessary in relative
 *                                     humidity calculations.
 * 25 OCT 2017  39813      amoore      Fix missing resultant wind issue where calculations
 *                                     would not happen unless either DSM wind or gust was
 *                                     missing. Reorg build of wind to be more side-by-side
 *                                     comparable to legacy. Fix potential bug where if peak
 *                                     gust was the first hour, it would be missed.
 * 26 OCT 2017  40051      amoore      Fix humidity check logic against double max/min instead
 *                                     of still using int max/min.
 * 01 NOV 2017  39954      amoore      Fix and make clearer tallying of rain logic.
 * 13 NOV 2017  39660      wpaintsil   Fix conditional checking for missing rainfall.
 * 17 NOV 2017  41018      amoore      Handle case where legacy has precip values less than trace
 *                                     from METAR decoding. Assign these values as trace.
 * 13 DEC 2017  41565      wpaintsil   Fix IM/PM discrepancies.
 * 13 APR 2018  DR17116    wpaintsil   Wherever precipSeason and snowSeason are used, change
 *                                     the algorithm to account for multiple seasons.
 * 23 OCT 2018  DR20945    wpaintsil   Include SPECI observations for relative humidity.
 * 20 MAR 2019  DR 21189   dfriedman   Change queries to compare timestamps
 *                                     instead of formatted strings.
 * 26 APR 2019  DR 21195   dfriedman   Handle both special case precipitation values.
 * 30 APR 2019  DR21261    wpaintsil   numWindObs field was not set, causing missing resultant wind.
 *                                     Also wrong values set in resultX and resultY fields.
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
    protected ClimateRunDailyData createDailyClimate(PeriodType periodType,
            ClimateDate beginDate, ClimateTime validTime,
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

        return new ClimateRunDailyData(periodType, beginDate, reportMap);
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
            List<ClimateDate> snowSeasons, ClimateDate snowYear,
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
        List<Float> snowSeasonValues = new ArrayList<>();
        for (ClimateDate snowSeason : snowSeasons) {
            if (lastYearDate.getDay() == snowSeason.getDay()
                    && lastYearDate.getMon() == snowSeason.getMon()) {
                snowSeasonValues.add(data.getSnowDay());
            } else {
                snowSeasonValues.add(dailyClimateDao.sumSnow(snowSeason,
                        lastYearDate, informID));
            }
        }
        data.setSnowSeasons(snowSeasonValues);

        /*
         * Calculate the annual accumulated snowfall. Treat the first day of the
         * year as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == snowYear.getDay()
                && lastYearDate.getMon() == snowYear.getMon()) {
            data.setSnowYear(data.getSnowDay());
        } else {
            data.setSnowYear(
                    dailyClimateDao.sumSnow(snowYear, lastYearDate, informID));
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
            List<ClimateDate> precipSeasons, ClimateDate precipYear,
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
        List<Float> precipSeasonValues = new ArrayList<>();
        for (ClimateDate precipSeason : precipSeasons) {
            if (lastYearDate.getDay() == precipSeason.getDay()
                    && lastYearDate.getMon() == precipSeason.getMon()) {
                precipSeasonValues.add(data.getPrecip());
            } else {
                precipSeasonValues.add(dailyClimateDao.sumPrecip(precipSeason,
                        lastYearDate, informID));
            }
        }

        data.setPrecipSeasons(precipSeasonValues);

        /*
         * Set up the beginning and ending dates for the retrieval of the annual
         * accumulated precipitation for days other than the first of the year.
         */
        /*
         * Legacy did not check for first of the year despite documentation and
         * similarity to snow, heating, and cooling methods.
         */
        if (lastYearDate.getDay() == precipYear.getDay()
                && lastYearDate.getMon() == precipYear.getMon()) {
            data.setPrecipYear(data.getPrecip());
        } else {
            data.setPrecipYear(dailyClimateDao.sumPrecip(precipYear,
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
            ClimateDate coolSeason, ClimateDate coolYear,
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
        if (lastYearDate.getDay() == coolSeason.getDay()
                && lastYearDate.getMon() == coolSeason.getMon()) {
            data.setNumCoolSeason(data.getNumCool());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the season.
             */
            data.setNumCoolSeason(dailyClimateDao.sumCoolDegreeDays(coolSeason,
                    lastYearDate, informID));
        }

        /*
         * Calculate the annual accumulated cooling degree days. Treat the first
         * day of the season as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == coolYear.getDay()
                && lastYearDate.getMon() == coolYear.getMon()) {
            data.setNumCoolYear(data.getNumCool());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the year.
             */
            data.setNumCoolYear(dailyClimateDao.sumCoolDegreeDays(coolYear,
                    lastYearDate, informID));
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
            ClimateDate heatSeason, ClimateDate heatYear,
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
        if (lastYearDate.getDay() == heatSeason.getDay()
                && lastYearDate.getMon() == heatSeason.getMon()) {
            data.setNumHeatSeason(data.getNumHeat());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated heating degree days for days other than the
             * first of the season.
             */
            data.setNumHeatSeason(dailyClimateDao.sumHeatDegreeDays(heatSeason,
                    lastYearDate, informID));
        }

        /*
         * Calculate the annual accumulated heating degree days. Treat the first
         * day of the season as a special case that requires no INFORMIX calls.
         */
        if (lastYearDate.getDay() == heatYear.getDay()
                && lastYearDate.getMon() == heatYear.getMon()) {
            data.setNumHeatYear(data.getNumHeat());
        } else {
            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated heating degree days for days other than the
             * first of the year.
             */
            data.setNumHeatYear(dailyClimateDao.sumHeatDegreeDays(heatYear,
                    lastYearDate, informID));
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
                MetarUtils.METAR_SUNSHINE_DURATION, cal);

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
                    MetarUtils.METAR_SUNSHINE_DURATION, cal)).getValue();

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
                dailyClimateData.getInformId(), speciWind, speciWindTime, true);

        // wind from special reports is current max
        ClimateWind saveMaxWind = new ClimateWind(speciWind);
        ClimateTime saveMaxWindTime = new ClimateTime(speciWindTime);

        if (saveMaxWind.getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
            // currently speed is in knots, convert to mph
            saveMaxWind.setSpeed((float) (saveMaxWind.getSpeed()
                    * ClimateUtilities.KNOTS_TO_MPH));
            windQC = QCValues.MAX_WIND_FROM_SPECI;

            // correct the hour by timezone
            int newHour = saveMaxWindTime.getHour() + numOffUTC;

            if (newHour < 0) {
                newHour += TimeUtil.HOURS_PER_DAY;
            } else if (newHour >= TimeUtil.HOURS_PER_DAY) {
                newHour -= TimeUtil.HOURS_PER_DAY;
            }
            saveMaxWindTime.setHour(newHour);
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

        // gust from special reports is current max
        ClimateWind saveMaxGust = new ClimateWind(speciGust);
        ClimateTime saveMaxGustTime = new ClimateTime(speciGustTime);

        if (saveMaxGust.getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
            // currently speed is in knots, convert to mph
            saveMaxGust.setSpeed((float) (saveMaxGust.getSpeed()
                    * ClimateUtilities.KNOTS_TO_MPH));
            gustQC = QCValues.MAX_GUST_FROM_GUST;
        }

        for (int i = 0; i < deltaHours; i++) {
            /*
             * Build the new date and time for the wind retrieval. Don't forget
             * to adjust the date if we go into the next day.
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

            if (i < deltaHours - 1) {
                /*
                 * Get the hourly winds from which to estimate maximum wind
                 * speed and to calculate the resultant wind.
                 */
                climateCreatorDAO.getHourlyWinds(currDate, currTime,
                        dailyClimateData.getInformId(), winds.get(i));

                /*
                 * Now test to see if the most recent wind is greater than a
                 * previous value from either the hourly or SPECI winds. If so,
                 * save the value and the time.
                 */
                if ((winds.get(i)
                        .getSpeed() != ParameterFormatClimate.MISSING_SPEED)
                        && (winds.get(i).getSpeed() != 0)) {
                    // currently speed is in knots, convert to mph
                    winds.get(i).setSpeed((float) (winds.get(i).getSpeed()
                            * ClimateUtilities.KNOTS_TO_MPH));

                    if (winds.get(i).getSpeed() >= maxWindSpeed) {
                        maxWindSpeed = winds.get(i).getSpeed();

                        if ((saveMaxWind
                                .getSpeed() == ParameterFormatClimate.MISSING_SPEED)
                                || (maxWindSpeed >= saveMaxWind.getSpeed())) {
                            saveMaxWind = new ClimateWind(winds.get(i));
                            saveMaxWindTime = new ClimateTime(maxWindTime);
                            windQC = QCValues.MAX_WIND_FROM_HOURLY;
                        }
                    }
                }

                /*
                 * Get the wind gusts from all reports for a given station, date
                 * and nominal time
                 */
                ClimateWind aGust = ClimateWind.getMissingClimateWind();
                climateCreatorDAO.getAllHourlyGusts(currDate, currTime,
                        dailyClimateData.getInformId(), aGust);

                /*
                 * Now test against to see if the most recent hourly gust is
                 * greater than a previous value. If so, save the value and
                 * time.
                 */
                if (aGust.getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                    // currently speed is in knots, convert to mph
                    aGust.setSpeed((float) (aGust.getSpeed()
                            * ClimateUtilities.KNOTS_TO_MPH));

                    if (aGust.getSpeed() > maxGustSpeed) {
                        maxGustSpeed = aGust.getSpeed();

                        if ((saveMaxGust
                                .getSpeed() == ParameterFormatClimate.MISSING_SPEED)
                                || (maxGustSpeed >= saveMaxGust.getSpeed())) {
                            saveMaxGust = new ClimateWind(aGust);
                            saveMaxGustTime = new ClimateTime(currTime);
                            gustQC = QCValues.MAX_GUST_FROM_GUST;
                        }
                    }
                }
            }

            if (i > 0) {
                /*
                 * Get the peak wind data from the hourly report for a given
                 * station, date and time. The peak wind is a field that is
                 * reported separately from the gusts in the METAR code, hence
                 * the need for two different routines.
                 */

                ClimateWind aPeakWind = ClimateWind.getMissingClimateWind();
                ClimateTime aPeakWindTime = ClimateTime.getMissingClimateTime();
                climateCreatorDAO.getHourlyPeakWinds(currDate, currTime,
                        dailyClimateData.getInformId(), aPeakWind,
                        aPeakWindTime);

                /*
                 * Now test to see if the most recent hourly peak wind is
                 * greater than a previous value. If so, save the value and the
                 * time.
                 */
                if (aPeakWind
                        .getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                    // currently speed is in knots, convert to mph
                    aPeakWind.setSpeed((float) (aPeakWind.getSpeed()
                            * ClimateUtilities.KNOTS_TO_MPH));

                    if (aPeakWind.getSpeed() > maxGustSpeed) {
                        maxGustSpeed = aPeakWind.getSpeed();

                        if ((saveMaxGust
                                .getSpeed() == ParameterFormatClimate.MISSING_SPEED)
                                || (maxGustSpeed >= saveMaxGust.getSpeed())) {
                            saveMaxGust = new ClimateWind(aPeakWind);
                            saveMaxGustTime = new ClimateTime(aPeakWindTime);
                            gustQC = QCValues.MAX_GUST_FROM_PEAK;
                        }
                    }
                }
            }
        } // end loop

        /*
         * Update the data structure with the max wind information. Check for
         * daily summary message values and use if currently missing.
         */
        if (dailyClimateData.getMaxWind()
                .getDir() == ParameterFormatClimate.MISSING) {
            dailyClimateData.setMaxWind(saveMaxWind);
            dailyClimateData.setMaxWindTime(saveMaxWindTime);
            dailyClimateData.getDataMethods().setMaxWindQc(windQC);
        }

        /*
         * Update the data structure with the gust information. Check for daily
         * summary message values and use if currently missing.
         */
        if (dailyClimateData.getMaxGust()
                .getDir() == ParameterFormatClimate.MISSING) {
            dailyClimateData.setMaxGust(saveMaxGust);
            dailyClimateData.setMaxGustTime(saveMaxGustTime);

            if (dailyClimateData.getMaxGustTime()
                    .getHour() != ParameterFormatClimate.MISSING_HOUR) {
                // correct for time zone
                int newHour = dailyClimateData.getMaxGustTime().getHour()
                        + numOffUTC;

                if (newHour < 0) {
                    newHour += TimeUtil.HOURS_PER_DAY;
                } else if (newHour >= TimeUtil.HOURS_PER_DAY) {
                    newHour -= TimeUtil.HOURS_PER_DAY;
                }
                dailyClimateData.getMaxGustTime().setHour(newHour);
            }

            dailyClimateData.getDataMethods().setMaxGustQc(gustQC);
        }

        /*
         * Calculate resultant and average wind. From build_resultant_wind.f.
         */
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
         * Only calculate the resultant and average wind if the number of hours
         * is non-zero. Set to missing if there was no valid wind data (i.e.,
         * num_hours= 0)
         */
        float avgWindSpeed;

        if (validHours == 0) {
            avgWindSpeed = ParameterFormatClimate.MISSING_SPEED;
            dailyClimateData.setResultX(ParameterFormatClimate.MISSING_SPEED);
            dailyClimateData.setResultY(ParameterFormatClimate.MISSING_SPEED);
            dailyClimateData.setNumWndObs(ParameterFormatClimate.MISSING);
            dailyClimateData.setResultWind(ClimateWind.getMissingClimateWind());
        } else {
            dailyClimateData.setResultX(sumX);
            dailyClimateData.setResultY(sumY);
            dailyClimateData.setNumWndObs(validHours);
            dailyClimateData.setResultWind(ClimateUtilities
                    .buildResultantWind(sumX, sumY, validHours));

            avgWindSpeed = avgWindSpeedSum / validHours;
        }

        /*
         * Only assign average wind speed if it is currently missing from DSM
         * data.
         */
        if (dailyClimateData.getDataMethods()
                .getAvgWindQc() == ParameterFormatClimate.MISSING) {
            dailyClimateData.getDataMethods()
                    .setAvgWindQc(QCValues.AVG_WIND_CALCULATED);
            dailyClimateData.setAvgWindSpeed(avgWindSpeed);
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

        double minRH = Double.MAX_VALUE;
        double maxRH = Double.MIN_VALUE;

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

            List<Double> temperature = getTemp(tempDate, currTime, stationID);

            List<Double> dewpoint = getDew(tempDate, currTime, stationID);

            for (int jj = 0; jj < temperature.size()
                    && jj < dewpoint.size(); jj++) {
                double jjTemp = temperature.get(jj);
                double jjDew = dewpoint.get(jj);

                // Now calculate RH for each valid temperature and dewpoint.
                if ((jjTemp != ParameterFormatClimate.MISSING)
                        && (jjDew != ParameterFormatClimate.MISSING)) {
                    /*
                     * Discrepancy with Legacy #69/DAI #74: In Legacy, humidity
                     * calculations would round temp/dew values after unit
                     * conversion of each, then round the resulting humidity,
                     * and then do comparisons for max/min humidity. Rounding
                     * should not be done until the last possible moment, and so
                     * in Migrated values may be different, but additionally
                     * times may be different as improper rounding may have been
                     * ignoring true min/max relative humidity values.
                     */
                    double currHumidity = ((ClimateUtilities
                            .vaporPressureFromTemperature(jjDew))
                            / (ClimateUtilities
                                    .vaporPressureFromTemperature(jjTemp)))
                            * 100;

                    /*
                     * Now determine the max and min RH and the hours at which
                     * they were observed. TODO Task 36087 In Migrated Climate,
                     * as in Legacy, the hour of max or mean relative humidity
                     * is determined by an index number representative of how
                     * many hours have been processed thus far. However, this is
                     * not necessarily the same as the actual hour of the data,
                     * depending on what time of day the looped processing
                     * started at. The actual hour seems to already be
                     * calculated within the loop.
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
        }

        /*
         * Now set up the data structure if the various pieces aren't missing.
         */
        if (maxRH != Double.MIN_VALUE) {
            dailyClimateData.setMaxRelHumid(ClimateUtilities.nint(maxRH));
            dailyClimateData.setMaxRelHumidHour(hourMaxRH);
        }

        if (minRH != Double.MAX_VALUE) {
            dailyClimateData.setMinRelHumid(ClimateUtilities.nint(minRH));
            dailyClimateData.setMinRelHumidHour(hourMinRH);
        }

        if ((maxRH != Double.MIN_VALUE) && (minRH != Double.MAX_VALUE)) {
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
        // expected data has 0 minute
        beginCal.set(Calendar.MINUTE, 0);
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
        // expected data has 0 minute
        endCal.set(Calendar.MINUTE, 0);
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

            try {
                FSSReportResult result = climateCreatorDAO.getMetarConreal(
                        dailyClimateData.getInformId(),
                        MetarUtils.METAR_6HR_PRECIP, nominalTimeCal);

                if (!result.isMissing()
                        && result.getValue() != MetarUtils.PNO_PRESENT) {
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

                try {
                    FSSReportResult result = climateCreatorDAO.getMetarConreal(
                            dailyClimateData.getInformId(),
                            MetarUtils.METAR_3HR_PRECIP, nominalTimeCal);

                    if (!result.isMissing()
                            && result.getValue() != MetarUtils.PNO_PRESENT) {
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

                boolean sixHourMissingOrError = false;
                try {
                    FSSReportResult sixHourResult = climateCreatorDAO
                            .getMetarConreal(dailyClimateData.getInformId(),
                                    MetarUtils.METAR_6HR_PRECIP,
                                    nominalTimeCal);

                    if (sixHourResult.isMissing() || sixHourResult
                            .getValue() == MetarUtils.PNO_PRESENT) {
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
                        String nominalTimeString = ClimateDate
                                .getFullDateTimeFormat()
                                .format(nominalTimeCal.getTime());

                        boolean threeHourMissingOrError = false;
                        try {
                            FSSReportResult threeHourResult = climateCreatorDAO
                                    .getMetarConreal(
                                            dailyClimateData.getInformId(),
                                            MetarUtils.METAR_3HR_PRECIP,
                                            nominalTimeCal);

                            if (threeHourResult.isMissing() || threeHourResult
                                    .getValue() == MetarUtils.PNO_PRESENT) {
                                threeHourMissingOrError = true;
                            } else {
                                threeHour = true;
                                if ((rainAmount > 0)
                                        && (threeHourResult.getValue() > 0)) {

                                    if (threeHourResult
                                            .getValue() > rainAmount) {
                                        logger.warn("For station ID ["
                                                + dailyClimateData.getInformId()
                                                + "] the 3 hour rain total ["
                                                + threeHourResult.getValue()
                                                + "] for time ["
                                                + nominalTimeString
                                                + "] is greater than the 6 hour rain total ["
                                                + sixHourResult.getValue()
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

            dailyClimateData.setPrecip(
                    tallyRainAmount(dailyClimateData.getPrecip(), rainAmount));

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

                boolean threeHourMissingOrError = false;
                try {
                    FSSReportResult threeHourResult = climateCreatorDAO
                            .getMetarConreal(dailyClimateData.getInformId(),
                                    MetarUtils.METAR_3HR_PRECIP,
                                    nominalTimeCal);

                    if (threeHourResult.isMissing() || threeHourResult
                            .getValue() == MetarUtils.PNO_PRESENT) {
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

            dailyClimateData.setPrecip(
                    tallyRainAmount(dailyClimateData.getPrecip(), rainAmount));
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
     *******************************************************************************
     * MODULE NUMBER:   4
     * MODULE NAME:     det_6h_max
     * PURPOSE:         This routine looks for the 1xxxx group which denotes 
     *                  the 6 hour maximum temperature in a METAR. If this group
     *                  cannot be found then this routine determines the 6 hour
     *                  maximum by reading in all of the temperature reports 
     *                  for the six hour period and taking the maximum. 
     *
     *******************************************************************************
     * MODULE NUMBER:   5
     * MODULE NAME:     det_6h_min
     * PURPOSE:      This routine looks for the 2xxxx group which denotes 
     *           the 6 hour minimum temperature in a METAR. If this group
     *           cannot be found then this routine determines the 6 hour
     *           minimum by reading in all of the temperature reports
     *           for the six hour period and then taking the minimum.
     *
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
        // expected data has 0 minute
        beginCal.set(Calendar.MINUTE, 0);
        long beginBaseMilliTicks = beginCal.getTimeInMillis();

        // advance start millis to next hour if within 15 minutes of it
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
        // expected data has 0 minute
        endCal.set(Calendar.MINUTE, 0);
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
                                        nominalCal);

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
                                        nominalCal);

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

                        /*
                         * try to get the value of the max temperature field for
                         * the time period's end time metar
                         */
                        FSSReportResult tempFrom6Result = climateCreatorDAO
                                .getMetarConreal(dailyClimateData.getInformId(),
                                        MetarUtils.METAR_6HR_MAXTEMP,
                                        adjustEndTimeCal);

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
                        /*
                         * try to get the value of the max temperature field for
                         * the time period's end time metar
                         */
                        FSSReportResult tempFrom6Result = climateCreatorDAO
                                .getMetarConreal(dailyClimateData.getInformId(),
                                        MetarUtils.METAR_6HR_MINTEMP,
                                        adjustEndTimeCal);

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
                    long beginDTimeMilli = endBaseMilliTicks;
                    long endDTimeMilli = endBaseMilliTicks
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

            ClimateDates window = determineWindow(aDate, currStation, itype,
                    validTime);

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
                    int ihour = yesterday.getMaxTempTime().to24HourTime()
                            .getHour();
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
                    int ihour = yesterday.getMinTempTime().to24HourTime()
                            .getHour();
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
        ClimateSeason season = ClimateDAOUtils.getSeason(lastYearDate);

        // Now loop and retrieve the climatology one station at a time.
        for (int i = 0; i < climateStations.size(); i++) {
            int informID = climateStations.get(i).getInformId();

            DailyClimateData data = DailyClimateData
                    .getMissingDailyClimateData();
            data.setInformId(informID);

            lastYear.add(data);

            dailyClimateDao.getLastYear(lastYearDate, informID, data);

            getHeatLast(lastYearDate, informID, season.getHeatSeason(),
                    season.getHeatYear(), data);

            getCoolLast(lastYearDate, informID, season.getCoolSeason(),
                    season.getCoolYear(), data);

            getPrecipLast(lastYearDate, informID, season.getPrecipSeasons(),
                    season.getPrecipYear(), data);

            getSnowLast(lastYearDate, informID, season.getSnowSeasons(),
                    season.getSnowYear(), data);
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
     * </pre>
     * 
     * @param aDate
     * @param climateStations
     * @param yClimate
     * @throws Exception
     */
    private void sumHisNorms(ClimateDate aDate, List<Station> climateStations,
            List<ClimateRecordDay> yClimate) throws Exception {

        // Now get the seasonal and yearly dates for the cumulative
        // parameters
        ClimateSeason season = ClimateDAOUtils.getSeason(aDate);

        // Loop on the number of stations for the daily climate summaries.
        // Calculate the accumulated values for the heating and cooling degree
        // days, precipitation and snowfall.

        for (int i = 0; i < climateStations.size(); i++) {
            int stationId = climateStations.get(i).getInformId();

            ClimateDAOUtils.updateHisPrecip(aDate, stationId,
                    season.getPrecipSeasons(), season.getPrecipYear(),
                    yClimate.get(i));
            ClimateDAOUtils.updateHisSnow(aDate, stationId,
                    season.getSnowSeasons(), season.getSnowYear(),
                    yClimate.get(i));

            updateHisHeat(aDate, stationId, season.getHeatSeason(),
                    season.getHeatYear(), yClimate.get(i));

            updateHisCool(aDate, stationId, season.getCoolSeason(),
                    season.getCoolYear(), yClimate.get(i));
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
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param coolSeason
     * @param coolYear
     * @param yClimate
     */

    private void updateHisCool(ClimateDate aDate, int stationId,
            ClimateDate coolSeason, ClimateDate coolYear,
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
        first = coolSeason;

        yClimate.setNumCoolSeason(
                climatePeriodNormDAO.sumHisCool(first, last, stationId));

        // Now do the annual cooling degree days
        // We have the same potential problems as with the seasonal
        // cool

        first = coolYear;
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
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param heatSeason
     * @param heatYear
     * @param yClimate
     */
    private void updateHisHeat(ClimateDate aDate, int stationId,
            ClimateDate heatSeason, ClimateDate heatYear,
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
        first = heatSeason;
        // First handle the case where there isn't a problem
        // across the first day of the year

        yClimate.setNumHeatSeason(
                climatePeriodNormDAO.sumHisHeat(first, last, stationId));
        // Now do the annual heating degree days
        // We have the same potential problems as with the seasonal
        // heat

        first = heatYear;
        yClimate.setNumHeatYear(
                climatePeriodNormDAO.sumHisHeat(first, last, stationId));

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
     *      for the period between begin date and time and end date and time.
     *
     * </pre>
     * 
     * Modified to include SPECI observations.
     * 
     * @param date
     * @param time
     * @param stationID
     * @return
     */
    private List<Double> getDew(ClimateDate date, ClimateTime time,
            int stationID) {
        Calendar cal = date.getCalendarFromClimateDate();
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMin());

        List<Double> dewpoint = new ArrayList<>();
        try {
            /*
             * This function will retrieve the dew_pt reported during a single
             * hour Using the max_db_status as an error check of the INFORMIX
             * database, it will check to see the data is missing, not reported,
             * or if there was another type of error. Once the error status has
             * been determined, a quality check done on the data to see whether
             * or not the data is legitimate.
             */
            List<FSSReportResult> results = climateCreatorDAO.getMetarConreal(
                    stationID, MetarUtils.METAR_DEWPOINT_2_TENTHS, cal, true);
            for (FSSReportResult result : results) {
                if (result.isMissing()) {
                    dewpoint.add((double) ParameterFormatClimate.MISSING);
                } else {
                    dewpoint.add(ClimateUtilities
                            .celsiusToFahrenheit(result.getValue()));
                }
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
                List<FSSReportResult> results = climateCreatorDAO
                        .getMetarConreal(stationID, MetarUtils.METAR_DEWPOINT,
                                cal, true);
                for (FSSReportResult result : results) {
                    if (result.isMissing()) {
                        dewpoint.add((double) ParameterFormatClimate.MISSING);
                    } else {
                        dewpoint.add(ClimateUtilities
                                .celsiusToFahrenheit(result.getValue()));
                    }
                }
            } catch (ClimateQueryException e2) {
                logger.error(
                        "Failed to get dewpoint from METAR in whole degrees. Returning missing value.",
                        e2);
                dewpoint.add((double) ParameterFormatClimate.MISSING);
            }
        }

        /*
         * Legacy checked initial query return against Dew bounds, and second
         * query return against Temp bounds. Assumed to be a bug from copying
         * code from get_temp.c to get_dew.c.
         */
        for (int ii = 0; ii < dewpoint.size(); ii++) {
            double iiDew = dewpoint.get(ii);
            if (iiDew == ParameterFormatClimate.MISSING
                    || iiDew < ParameterBounds.MIN_DEW_QC
                    || iiDew > ParameterBounds.MAX_DEW_QC) {
                logger.warn("Dewpoint [" + iiDew
                        + "] is missing or outside acceptable range. Missing value will be returned.");
                dewpoint.set(ii, (double) ParameterFormatClimate.MISSING);
            }
        }
        return dewpoint;
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
     *      for the period between begin date and time and end date and time.
     *
     * </pre>
     * 
     * Modified to include SPECI observations.
     * 
     * @param date
     * @param time
     * @param stationID
     * @return
     */
    private List<Double> getTemp(ClimateDate date, ClimateTime time,
            int stationID) {
        Calendar cal = date.getCalendarFromClimateDate();
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMin());

        List<Double> temperature = new ArrayList<>();
        try {
            /*
             * This function will retrieve the temperature reported during a
             * single hour Using the max_db_status as an error check of the
             * INFORMIX database, it will check to see the data is missing, not
             * reported, or if there was another type of error. Once the error
             * status has been determined, a quality check done on the data to
             * see whether or not the data is legitimate.
             */
            List<FSSReportResult> results = climateCreatorDAO.getMetarConreal(
                    stationID, MetarUtils.METAR_TEMP_2_TENTHS, cal, true);

            for (FSSReportResult result : results) {
                if (result.isMissing()) {
                    temperature.add((double) ParameterFormatClimate.MISSING);
                } else {
                    temperature.add(ClimateUtilities
                            .celsiusToFahrenheit(result.getValue()));
                }
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
                List<FSSReportResult> results = climateCreatorDAO
                        .getMetarConreal(stationID, MetarUtils.METAR_TEMP, cal,
                                true);
                for (FSSReportResult result : results) {
                    if (result.isMissing()) {
                        temperature
                                .add((double) ParameterFormatClimate.MISSING);
                    } else {
                        temperature.add(ClimateUtilities
                                .celsiusToFahrenheit(result.getValue()));
                    }
                }
            } catch (ClimateQueryException e2) {
                logger.error(
                        "Failed to get temperature from METAR in whole degrees. Returning missing value.",
                        e2);
                temperature.add((double) ParameterFormatClimate.MISSING);
            }
        }

        for (int ii = 0; ii < temperature.size(); ii++) {
            double iiTemp = temperature.get(ii);
            if (iiTemp == ParameterFormatClimate.MISSING
                    || iiTemp < ParameterBounds.TEMP_LOWER_BOUND
                    || iiTemp > ParameterBounds.TEMP_UPPER_BOUND_QC) {
                logger.warn("Temperature [" + iiTemp
                        + "] is missing or outside acceptable range. Missing value will be returned.");
                temperature.set(ii, (double) ParameterFormatClimate.MISSING);
            }
        }

        return temperature;
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
        // expected data has 0 minute
        beginCal.set(Calendar.MINUTE, 0);
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
        // expected data has 0 minute
        endCal.set(Calendar.MINUTE, 0);
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
                            data.getInformId(), lowerTimeCal, upperTimeCal);

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
                        .getCorrection(data.getInformId(), nominalTimeCal);
                if (correction == null) {
                    logger.warn("No METAR observation found for station ID: ["
                            + data.getInformId() + "] and time: ["
                            + nominalTimeString + "]");
                } else if (!correction.equals("A")) {
                    try {
                        FSSReportResult result = climateCreatorDAO
                                .getMetarConreal(data.getInformId(),
                                        MetarUtils.METAR_SNOW_DEPTH,
                                        nominalTimeCal);
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
                    String correction = climateCreatorDAO
                            .getCorrection(data.getInformId(), nominalTimeCal);
                    if (correction == null) {
                        logger.warn(
                                "No METAR observation found for station ID: ["
                                        + data.getInformId() + "] and time: ["
                                        + nominalTimeString + "]");
                    } else if (!correction.equals("A")) {
                        try {
                            FSSReportResult result = climateCreatorDAO
                                    .getMetarConreal(data.getInformId(),
                                            MetarUtils.METAR_SNOW_DEPTH,
                                            nominalTimeCal);
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
                        informId, MetarUtils.METAR_1HR_PRECIP, nominalTimeCal);

                if (!result.isMissing()
                        && result.getValue() != MetarUtils.PNO_PRESENT) {
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

        if (beginTimeMilli == endTimeMilli) {
            /* should be only one metar; get its temperature */
            try {
                FSSReportResult tenthsResult = climateCreatorDAO
                        .getMetarConreal(informId,
                                MetarUtils.METAR_TEMP_2_TENTHS, beginCal);

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
                        informId, MetarUtils.METAR_TEMP, beginCal);

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
                        .getPeriodSpeciMin(informId, beginCal, nominalCal);

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
                                speciResult.getTime(), speciResult.getTemp(),
                                speciResult.getFlag());
                    } else {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                metarResult.getTime(), metarResult.getTemp(),
                                metarResult.getFlag());
                    }
                }

                /*
                 * Just return the metar high if the speci result is missing.
                 */
                return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                        metarResult.getTime(), metarResult.getTemp(),
                        metarResult.getFlag());
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
     ********************************************************************************
     * MODULE NUMBER:  2
     * MODULE NAME:    tally_rain_amount 
     * PURPOSE:        This routine adds a rain_amount to a "running" total
     *                 taking into account trace amounts, missing values, etc.
     *
     * </pre>
     * 
     * @param precip
     *            current precip value.
     * @param elementValue
     *            retrieved precip value.
     * @return new precip value to use.
     */

    private static float tallyRainAmount(float precip, double elementValue) {
        boolean basePrecipMissing = (ClimateUtilities.floatingEquals(precip,
                ParameterFormatClimate.MISSING_PRECIP)
                || ClimateUtilities.floatingEquals(precip,
                        ClimateCreatorDAO.R_MISS));
        boolean retrievedPrecipMissing = (ClimateUtilities.floatingEquals(
                elementValue, ParameterFormatClimate.MISSING_PRECIP)
                || ClimateUtilities.floatingEquals(elementValue,
                        ClimateCreatorDAO.R_MISS));
        /*
         * Legacy parsing of METARs allowed for precip values less than the
         * trace value of -1, so cannot just return a potentially bad value.
         * Clear out potentially bad trace indicators.
         *
         * Also explicitly handle the special value of -2, indicating a trace of
         * precipitation, here. Legacy climate converts the -2 to CLIMATE_TRACE
         * (-1) at the end of compute_daily_precip.
         */
        precip = precip < 0 ? ParameterFormatClimate.TRACE : precip;
        elementValue = elementValue == MetarUtils.FSS_CONTIN_TRACE
                || elementValue < 0 ? ParameterFormatClimate.TRACE
                        : elementValue;

        /*
         * Check what case we are in. Want to sum the 2 values, but should not
         * directly sum TRACE or MISSING values as these are special indicators.
         */
        if (retrievedPrecipMissing && basePrecipMissing) {
            /*
             * both values are missing, so return 0 per Legacy logic.
             */
            return 0;
        } else if (retrievedPrecipMissing) {
            /*
             * retrieved value is missing but not base value, so just return
             * base value
             */
            return precip;
        } else if (basePrecipMissing) {
            /*
             * base value is missing but not retrieved value, so just return
             * retrieved value
             */
            return (float) elementValue;
            /*
             * no value below here is missing
             */
        } else if ((precip == ParameterFormatClimate.TRACE
                && elementValue == ParameterFormatClimate.TRACE)) {
            /*
             * Both values are trace, so return trace
             */
            return ParameterFormatClimate.TRACE;
        } else if ((precip == ParameterFormatClimate.TRACE
                && elementValue != ParameterFormatClimate.TRACE)) {
            /*
             * base value is trace and retrieved value is not, so return
             * retrieved value.
             */
            return (float) elementValue;
        } else if ((precip != ParameterFormatClimate.TRACE
                && elementValue == ParameterFormatClimate.TRACE)) {
            /*
             * retrieved value is trace and base value is not, so return base
             * value.
             */
            return precip;
        } else {
            /*
             * Neither value is missing or trace, so just return their sum
             */
            return precip + (float) elementValue;
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

        if (beginTimeMilli == endTimeMilli) {
            /* should be only one metar; get its temperature */
            boolean tenthsFailure = false;
            try {
                FSSReportResult tenthsResult = climateCreatorDAO
                        .getMetarConreal(informId,
                                MetarUtils.METAR_TEMP_2_TENTHS, beginCal);

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
                            informId, MetarUtils.METAR_TEMP, beginCal);

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

                /* get the highest speci temperature for the period */
                ClimateCreatorDAO.ExtremeTempPeriodResult speciResult = climateCreatorDAO
                        .getPeriodSpeciMax(informId, beginCal, nominalCal);

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
                                speciResult.getTime(), speciResult.getTemp(),
                                speciResult.getFlag());
                    } else {
                        return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                                metarResult.getTime(), metarResult.getTemp(),
                                metarResult.getFlag());
                    }
                }

                /*
                 * Just return the metar high if the speci result is missing.
                 */
                return new ClimateCreatorDAO.ExtremeTempPeriodResult(
                        metarResult.getTime(), metarResult.getTemp(),
                        metarResult.getFlag());
            }
        }

        return new ClimateCreatorDAO.ExtremeTempPeriodResult();
    }

    /**
     * Migrated from determine_window.f
     * 
     * <pre>
     *   July 1998     Jason P. Tuell        PRC/TDL
     *   Oct. 1998     David O. Miller       PRC/TDL
     *
     *
     *   Purpose:  This routine determines the starting and ending times
     *             and dates for data retrievals for the different climate
     *             summaries.  It only makes one assumption regarding the
     *             valid_time for the evening climate summaries: the valid_time
     *             must be later than or equal to 1300 local!!  (i.e., the 
     *             evening climate summary must be run in the afternoon or
     *             evening.  The begin and start times are only used
     *             for building the daily summaries.
     *
     *             The month and year in the valid date is used to specify
     *             month and year of the monthly climate summary.  The day
     *             is used to specify the ending day of the month (typically
     *             the last day of the month).  However, by using the day from
     *             a_date, it allows the user to produce mid-month climate
     *             summaries.
     *
     *             The year is used to determine the year for an annual 
     *             climate summary.  In a normal run, the month and day will
     *             be that of the last day of the year.  However, using the day
     *             and year from a_date will enable the user to produce mid_year
     *             climate summaries.
     * 
     * </pre>
     * 
     * @param aDate
     * @param station
     * @param itype
     * @param validTime
     */

    public ClimateDates determineWindow(ClimateDate aDate, Station station,
            PeriodType itype, ClimateTime validTime) {
        ClimateDate beginDate = ClimateDate.getMissingClimateDate();
        ClimateTime beginTime = ClimateTime.getMissingClimateTime();
        ClimateDate endDate = ClimateDate.getMissingClimateDate();
        ClimateTime endTime = ClimateTime.getMissingClimateTime();

        switch (itype) {

        // Daily climate summary. We need to take into account the
        // differences in the different time zones.
        // Keep in mind that the daily climate summary is done for
        // midnight to midnight local standard time.

        case MORN_NWWS:
        case MORN_RAD:

            beginTime.setMin(0);
            endTime.setMin(59);

            if (station.getNumOffUTC() < 0) {

                int ihour = Math.abs(station.getNumOffUTC());
                beginTime.setHour(ihour);
                endTime.setHour(ihour - 1);

                beginDate = new ClimateDate(aDate);
                endDate.setYear(aDate.getYear());
                // Get next day of year
                int iday = aDate.julday() + 1;

                // Set endDate to next day of year
                endDate.convertJulday(iday);

            } else if (station.getNumOffUTC() > 0) {
                int ihour = TimeUtil.HOURS_PER_DAY - station.getNumOffUTC();
                beginTime.setHour(ihour);
                endTime.setHour(ihour - 1);

                int iday = aDate.julday() - 1;
                endDate = new ClimateDate(aDate);
                beginDate.setYear(aDate.getYear());

                beginDate.convertJulday(iday);

            } else {

                beginTime.setHour(0);
                endTime.setHour(23);
                beginDate = new ClimateDate(aDate);
                endDate = new ClimateDate(aDate);

            }
            break;

        case INTER_NWWS:
        case INTER_RAD:
        case EVEN_NWWS:
        case EVEN_RAD:

            // Evening and intermediate climate summary. These are
            // generated for the period from midnight to valid_time, which
            // the user specifies via the set up GUI.
            beginTime.setMin(0);
            endTime.setMin(0);

            if (station.getNumOffUTC() < 0) {

                int iday;
                int ihour = Math.abs(station.getNumOffUTC());
                beginTime.setHour(ihour);
                int ihourValidUTC = ihour + validTime.getHour();

                if (ihourValidUTC >= TimeUtil.HOURS_PER_DAY) {
                    endTime.setHour(ihourValidUTC - TimeUtil.HOURS_PER_DAY);
                    // Get next day of year
                    iday = aDate.julday() + 1;
                } else {
                    endTime.setHour(ihourValidUTC);
                    // Get current day of year
                    iday = aDate.julday();
                }

                beginDate = new ClimateDate(aDate);
                endDate.setYear(aDate.getYear());

                // Set endDate to next or current day of year
                endDate.convertJulday(iday);

            } else if (station.getNumOffUTC() > 0) {
                int ihour = TimeUtil.HOURS_PER_DAY - station.getNumOffUTC();
                beginTime.setHour(ihour);
                int ihourValidUTC = beginTime.getHour() + validTime.getHour();

                int iday = aDate.julday() - 1;
                beginDate.convertJulday(iday);
                beginDate.setYear(aDate.getYear());

                if (ihourValidUTC >= TimeUtil.HOURS_PER_DAY) {
                    endTime.setHour(ihourValidUTC - TimeUtil.HOURS_PER_DAY);
                    endDate = new ClimateDate(aDate);
                } else {
                    endTime.setHour(ihourValidUTC);
                    endDate = new ClimateDate(beginDate);
                }

            } else {

                beginTime.setHour(0);
                endTime.setHour(validTime.getHour());
                beginDate = new ClimateDate(aDate);
                endDate = new ClimateDate(aDate);

            }
            break;
        //
        // Monthly climate report case
        // Here we assume that the monthly reports are generated
        // sometime after the last day of the month for which the
        // report is valid
        //
        case MONTHLY_NWWS:
        case MONTHLY_RAD:

            beginDate.setDay(1);

            int imon;

            // You need to handle January as a special case
            if (aDate.getMon() == 1) {
                imon = 12;
                beginDate.setMon(imon);
                beginDate.setYear(aDate.getYear() - 1);
            } else {
                imon = aDate.getMon() - 1;
                beginDate.setMon(imon);
                beginDate.setYear(aDate.getYear());
            }
            // Now you need to handle leap years as a special case
            // keeping replaced code for viewing by Creator implementor
            endDate.setMon(beginDate.getMon());
            endDate.setYear(beginDate.getYear());
            endDate.setDay(ClimateUtilities.daysInMonth(endDate));
            break;
        // Annual climate report case
        // Here we assume that the annual summary is generated in
        // the next year for which the summary is valid
        case ANNUAL_NWWS:
        case ANNUAL_RAD:
            beginDate.setDay(1);
            beginDate.setMon(1);
            beginDate.setYear(aDate.getYear() - 1);

            endDate.setDay(
                    ClimateUtilities.daysInMonth(beginDate.getYear(), 12));
            endDate.setMon(12);
            endDate.setYear(beginDate.getYear());
            break;

        default:
            logger.error("Unknown period type [" + itype.getValue() + "]");
            break;
        }

        return new ClimateDates(beginDate, endDate, beginTime, endTime);
    }
}
