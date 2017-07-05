/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.creator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateFreezeDatesDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodNormDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;

/**
 * Migrated from create_climate.c. Period logic.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 JUL 2017  33104      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public final class PeriodClimateCreator {
    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PeriodClimateCreator.class);

    private final DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

    private final ClimatePeriodNormDAO climatePeriodNormDAO = new ClimatePeriodNormDAO();

    private final ClimatePeriodDAO climatePeriodDAO = new ClimatePeriodDAO();

    private final ClimateFreezeDatesDAO climateFreezeDatesDAO = new ClimateFreezeDatesDAO();

    /**
     * The freeze dates "module" to access.
     */
    private static final int FREEZE_DATE_MODULE = 2;

    /**
     * Constructor.
     * 
     */
    protected PeriodClimateCreator() {
    }

    /**
     * @param periodType
     * @param beginDate
     * @param endDate
     * @param globalValues
     * @param climateStations
     * @param cronOrManualMostRecent
     *            True if initiated as cronjob or user selected most recent
     *            date(s) option
     * @return
     * @throws ClimateInvalidParameterException
     * @throws ClimateQueryException
     */
    protected ClimateCreatorPeriodResponse createPeriodClimate(
            PeriodType periodType, ClimateDate beginDate, ClimateDate endDate,
            ClimateGlobal globalValues, List<Station> climateStations,
            boolean cronOrManualMostRecent)
                    throws ClimateInvalidParameterException,
                    ClimateQueryException {

        List<PeriodData> lastYearPeriodDatas = new ArrayList<>();

        /*
         * historical period data structure
         */
        List<PeriodClimo> periodClimos = new ArrayList<>();

        /*
         * period climate data structures
         */
        List<PeriodData> periodDatas = new ArrayList<>();

        /****************************************
         * Monthly, seasonal, yearly reports
         *****************************************/

        /*
         * initialize structures to missing values and set the station ids
         */
        for (Station currStation : climateStations) {
            int currStationID = currStation.getInformId();

            PeriodData lastYearPeriodData = PeriodData.getMissingPeriodData();
            lastYearPeriodData.setInformId(currStationID);
            lastYearPeriodDatas.add(lastYearPeriodData);

            PeriodClimo periodClimo = PeriodClimo.getMissingPeriodClimo();
            periodClimo.setInformId(currStationID);
            periodClimos.add(periodClimo);

            PeriodData periodData = PeriodData.getMissingPeriodData();
            periodData.setInformId(currStationID);
            periodDatas.add(periodData);
        }

        /*
         * Legacy comment:
         * 
         * need to check this section for generalization
         */
        if (cronOrManualMostRecent) {
            switch (periodType) {
            case MONTHLY_NWWS:
            case MONTHLY_RAD:
                /* monthly */
                // if this was a cron job or manual selection of most recent
                // period, need to calculate proper begin and end date
                beginDate.setDay(1);

                if (beginDate.getMon() == 1) {
                    beginDate.setMon(12);
                    beginDate.setYear(beginDate.getYear() - 1);
                } else {
                    beginDate.setMon(beginDate.getMon() - 1);
                }

                endDate.setDay(ClimateUtilities.daysInMonth(beginDate));
                endDate.setMon(beginDate.getMon());
                endDate.setYear(beginDate.getYear());

                break;

            case SEASONAL_NWWS:
            case SEASONAL_RAD:
                /* seasonal */
                // season dates here
                // if this was a cron job or manual selection of most recent
                // period, need to calculate proper begin and end date

                // go back to previous season
                beginDate.setMon(beginDate.getMon() - 3);
                if (beginDate.getMon() <= 0) {
                    beginDate.setMon(beginDate.getMon() + 12);
                    beginDate.setYear(beginDate.getYear() - 1);
                }

                // get starting values for easier later calculation
                endDate = new ClimateDate(beginDate);

                beginDate.setDay(1);
                /*
                 * set_month finds beginning month of each season if not already
                 */
                switch (beginDate.getMon()) {
                case 1:
                case 2:
                    // set up begin and end winter
                    beginDate.setMon(12);
                    beginDate.setYear(beginDate.getYear() - 1);
                    endDate.setMon(2);
                    break;
                case 12:
                    // doing winter, and need to add a year to the end date
                    endDate.setYear(endDate.getYear() + 1);
                    endDate.setMon(2);
                    break;
                case 4:
                case 5:
                    // set up begin and end spring
                    beginDate.setMon(3);
                case 3:
                    // set up end spring
                    endDate.setMon(5);
                    break;
                case 7:
                case 8:
                    // set up begin and end summer
                    beginDate.setMon(6);
                case 6:
                    // set up end summer
                    endDate.setMon(8);
                    break;
                case 10:
                case 11:
                    // set up begin and end fall
                    beginDate.setMon(9);
                case 9:
                    // set up end fall
                    endDate.setMon(11);
                    break;
                default:
                    logger.error("Invalid month number: [" + beginDate.getMon()
                            + "]");
                }

                endDate.setDay(ClimateUtilities.daysInMonth(endDate));

                break;
            case ANNUAL_NWWS:
            case ANNUAL_RAD:
                /* annual reports */
                // if this was a cron job or manual selection of most recent
                // period, need to calculate proper begin and end date
                beginDate.setDay(1);
                beginDate.setMon(1);
                beginDate.setYear(beginDate.getYear() - 1);
                endDate.setMon(12);
                endDate.setDay(31);
                endDate.setYear(beginDate.getYear());
                break;
            default:
                throw new ClimateInvalidParameterException(
                        "Unhandled Period Type: [" + periodType + "]");
            }
        }

        ClimateDates lastYear = getLastYearsDates(beginDate, endDate);

        /*
         * loop through number of stations and build the observed period
         * climatology for each station.
         */
        for (int i = 0; i < climateStations.size(); i++) {
            PeriodData currPeriodData = periodDatas.get(i);
            int currStationID = climateStations.get(i).getInformId();
            PeriodClimo currPeriodClimo = periodClimos.get(i);

            climatePeriodDAO.buildPeriodObsClimo(beginDate, endDate,
                    currPeriodData, globalValues, periodType);

            // freeze dates, originally a part of build_period_obs_climo
            buildPeriodObsFreezeDates(periodType, beginDate, endDate,
                    currPeriodData, currStationID);

            climatePeriodDAO.buildPeriodSumClimo(beginDate, endDate,
                    currPeriodData, periodType);

            dailyClimateDao.buildPResultantWind(beginDate, endDate,
                    currPeriodData, PeriodType.OTHER);

            /* build last year's monthly data */
            climatePeriodDAO.getPeriodData(periodType, lastYear.getStart(),
                    lastYear.getEnd(), lastYearPeriodDatas.get(i),
                    lastYearPeriodDatas.get(i).getDataMethods());

            /* next historical data */
            climatePeriodNormDAO.getPeriodHistClimo(beginDate, endDate,
                    currPeriodClimo, periodType);

            climateFreezeDatesDAO.getFreezeDates(FREEZE_DATE_MODULE,
                    currStationID, currPeriodClimo.getEarlyFreezeNorm(),
                    currPeriodClimo.getLateFreezeNorm(),
                    currPeriodClimo.getEarlyFreezeRec(),
                    currPeriodClimo.getLateFreezeRec());
        }

        /*
         * Organize report data. Already organized by station in lists. TODO
         * organize above code to use the map from the start.
         */
        HashMap<Integer, ClimatePeriodReportData> reportMap = new HashMap<>();
        for (int i = 0; i < climateStations.size(); i++) {
            reportMap.put(climateStations.get(i).getInformId(),
                    new ClimatePeriodReportData(climateStations.get(i),
                            periodDatas.get(i), lastYearPeriodDatas.get(i),
                            periodClimos.get(i)));
        }
        return new ClimateCreatorPeriodResponse(periodType, beginDate, endDate,
                reportMap);
    }

    /**
     * Migrated from build_period_obs_climo.ecpp, separated from Climate Period
     * DAO since functionality deals with a different table and is more
     * appropriate to be placed in the corresponding DAO.
     * 
     * @param periodType
     * @param beginDate
     * @param endDate
     * @param periodData
     * @param stationID
     * @throws ClimateQueryException
     */
    private void buildPeriodObsFreezeDates(PeriodType periodType,
            ClimateDate beginDate, ClimateDate endDate, PeriodData periodData,
            int stationID) throws ClimateQueryException {
        if (PeriodType.OTHER.equals(periodType)) {
            periodData.setEarlyFreeze(dailyClimateDao
                    .getEarlyFreezeDate(beginDate, endDate, stationID));

            if (!periodData.getEarlyFreeze().isPartialMissing()) {
                periodData.setLateFreeze(dailyClimateDao
                        .getLateFreezeDate(beginDate, endDate, stationID));
            }
        } // Task 25623 no freeze date info for other period types?
    }

    /**
     * Migrated from c_create_climate.c#get_last_years_dates
     * 
     * @param beginDate
     * @param endDate
     * 
     * @return last year {@link ClimateDates} instance.
     */
    private ClimateDates getLastYearsDates(ClimateDate beginDate,
            ClimateDate endDate) {
        logger.debug("Begin Day: " + beginDate.getDay() + "\nBegin Month: "
                + beginDate.getMon() + "\nBegin Year: " + beginDate.getYear()
                + "\n\nEnd Day: " + endDate.getDay() + "\nEnd Month: "
                + endDate.getMon() + "\nEnd Year: " + endDate.getYear());

        ClimateDate lastYearBeginDate = new ClimateDate();

        // set the begin date from the passed parameter
        lastYearBeginDate.setDay(beginDate.getDay());
        lastYearBeginDate.setMon(beginDate.getMon());
        lastYearBeginDate.setYear(beginDate.getYear() - 1);

        // check for last year's leap year status
        int previousYear = endDate.getYear() - 1;

        boolean leap = ClimateDate.isLeapYear(previousYear);

        // determine the ending day - also check for leap year
        int endDay = endDate.getDay();

        // check to see if the month is Feb.
        if (endDate.getMon() == 2) {
            // now determine if the end day is 28 or 29
            if (leap) {
                logger.debug("Last year is calculated as leap year.");
                endDay = 29;
            } else {
                logger.debug("Last year is not leap year.");
                endDay = 28;
            }
        }

        ClimateDate lastYearEndDate = new ClimateDate();

        lastYearEndDate.setDay(endDay);
        lastYearEndDate.setMon(endDate.getMon());
        lastYearEndDate.setYear(previousYear);

        logger.debug("Last year begin Day: " + lastYearBeginDate.getDay()
                + "\nLast year begin Month: " + lastYearBeginDate.getMon()
                + "\nLast year begin Year: " + lastYearBeginDate.getYear()
                + "\n\nLast year end Day: " + lastYearEndDate.getDay()
                + "\nLast year end Month: " + lastYearEndDate.getMon()
                + "\nLast year end Year: " + lastYearEndDate.getYear());

        return new ClimateDates(lastYearBeginDate, lastYearEndDate);
    }

}
