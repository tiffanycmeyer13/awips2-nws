/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.rer.RecordClimateRawData;
import gov.noaa.nws.ocp.common.dataplugin.climate.rer.StationInfo;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorDailyResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorResponse;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.edex.climate.record.RecordClimate;

/**
 * Format climate data for NWWS and NWR products. Migrated from
 * c_format_climate.c.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 11, 2017 21099      wpaintsil   Initial creation
 * Feb 27, 2017 21099      wpaintsil   This file is getting too big. 
 *                                     Move nwws/nwr daily/period code 
 *                                     to separate classes.
 * Mar 07, 2017 21099      wpaintsil   Move static methods to ClimateFormat abstract class.
 * Apr 03  2017 21099      wpaintsil   Add call to RecordClimate method
 * 03 MAY 2017  33104      amoore      Use abstract map.
 * 11 MAY 2017  33104      amoore      Logging.
 * 19 MAY 2017  30163      wpaintsil   Consolidate algorithms for checking new daily records
 *                                     in ClimateNWWSDailyFormat.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class ClimateFormatter {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateFormatter.class);

    /**
     * Holds global configuration.
     */
    private ClimateGlobal globalConfig = new ClimateGlobal();

    /**
     * List of settings.
     */
    private List<ClimateProductType> settingsList;

    /**
     * Station map used in writeStationInfo()
     */
    private Map<String, List<StationInfo>> stationInfoMap = new HashMap<>();

    /**
     * Raw data map used in writeBrockenRecs()
     */
    private List<RecordClimateRawData> rawDatas = new ArrayList<>();

    /**
     * String constant used in writeStationInfo()
     */
    private static final String RER_STRING = "RER";

    /**
     * Default empty constructor
     */
    public ClimateFormatter() {

    }

    /**
     * Constructor. Set the list of settings and global configuration.
     * 
     * @param globalConfig
     * @param settingsList
     */
    public ClimateFormatter(ClimateGlobal globalConfig,
            List<ClimateProductType> settingsList) {
        this.globalConfig = globalConfig;
        this.settingsList = settingsList;
    }

    /**
     * Loop through settingsList and call the formatClimate overload method with
     * each.
     * 
     * @param reportData
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    public Map<String, ClimateProduct> formatClimate(
            ClimateCreatorResponse reportData) throws ClimateQueryException,
                    ClimateInvalidParameterException {

        Map<String, ClimateProduct> products = new HashMap<>();

        if (settingsList == null || settingsList.isEmpty()) {
            logger.error("The list of settings is null or empty.");
        } else {

            if ((reportData instanceof ClimateCreatorDailyResponse
                    && ((ClimateCreatorDailyResponse) reportData).getReportMap()
                            .isEmpty())
                    || (reportData instanceof ClimateCreatorPeriodResponse
                            && ((ClimateCreatorPeriodResponse) reportData)
                                    .getReportMap().isEmpty())) {
                logger.warn(
                        "The ClimateCreatorResponse parameter contains an empty report map.");
            }

            // Create text for each settings object in the list

            for (ClimateProductType settings : settingsList) {
                if (settings == null) {
                    logger.error(
                            "A ClimateProductType settings object in the list of settings is null.");
                } else {
                    products.putAll(formatClimate(reportData, settings));
                }
            }

            // write records with RecordClimate
            if (reportData.getPeriodType().isDaily()) {
                RecordClimate.generateAndStoreRER(rawDatas, stationInfoMap,
                        reportData.getPeriodType().isMorning(), globalConfig,
                        true);
            } else {
                logger.debug("Not generating RERs for period type: ["
                        + reportData.getPeriodType() + "]");
            }
            // TODO: confirm whether these are the correct values for yesterday
            // and operational flags
        }
        return products;

    }

    /**
     * Migrated from format_climate.f
     * 
     * <pre>
     * March 1998     Jason P. Tuell        PRC/TDL
    *  Sept. 1999     Dan Zipper            PRC/TDL
    *
    *
    *  Purpose:  This routine controls formatting the climate reports 
    *            for NOAA weather radio (NWR) and NOAA weather wire 
    *            service (NWWS).
    *
    *
    *  Variables
    *
    *     Input
    *       control_file - name of the file which contains the flags which control
    *                      generation of the sentences.
    *          data_file - name of the file which contains the observed climate
    *                      data.
    *        global_file - name of the file which contains the global
    *                      data.
    *        header_file - name of the file which contains the header
    *                      information.
    *       history_file - name of the file which contains the historical
    *                      climatological data.
    *          info_file - name of the file which contains the date, sunrise and
    *                      sunset.
    *        output_file - name of the file to which the header and climate text is
    *                      to be written.
    *       station_file - name of the file which contains the stations for this
    *                      climate summary.
    *
    *     Output
    *
    *
    *     Local
    *
    *         DO_CELSIUS - Flag for reporting temperatures in Celsius
    *                      - TRUE  report temps in Celsius
    *                      - FALSE don't report temps in Celsius
    *        DO_MAX_TEMP - Structure containing the flags which control generation
    *                      of various portions of the max temp report.
    *        DO_MIN_TEMP - Structure containing the flags which control generation
    *                      of various portions of the min temp report.
    *        DO_AVG_TEMP - Structure containing the flags which control generation
    *                      of various portions of the average temp report.
    *      DO_PRECIP_DAY - structure containing the flags which control generation
    *                      of various portions of the daily liquid precip sentences.
    *                      See the structure definition for more details.
    *      DO_PRECIP_MON - structure containing the flags which control generation
    *                      of various portions of the monthly liquid precip sentences.
    *                      See the structure definition for more details.
    *   DO_PRECIP_SEASON - structure containing the flags which control generation
    *                      of various portions of the seasonal liquid precip sentences.
    *                      See the structure definition for more details.
    *     DO_PRECIP_YEAR - structure containing the flags which control generation
    *                      of various portions of the yearly liquid precip sentences.
    *                      See the structure definition for more details.
    *        DO_SNOW_DAY - structure containing the flags which control generation
    *                      of various portions of the daily snowfall sentences.
    *                      See the structure definition for more details.
    *        DO_SNOW_MON - structure containing the flags which control generation
    *                      of various portions of the monthly snowfall sentences.
    *                      See the structure definition for more details.
    *     DO_SNOW_SEASON - structure containing the flags which control generation
    *                      of various portions of the yearly snowfall sentences.
    *                      See the structure definition for more details.
    *  DO_12Z_SNOW_DEPTH - structure containing the flags which control generation
    *                      of various portions of the snow depth sentences.
    *                      See the structure definition for more details.
    *          DO_MAX_RH - structure containg the flags which control generation of
    *                      various portions of the maximum rh sentences.
    *                      See the structure defintion for more details.
    *          DO_MIN_RH - structure containg the flags which control generation of
    *                      various portions of the minimum rh sentences.
    *                      See the structure definition for more details
    *         DO_MEAN_RH - structure containg the flags which control generation of
    *                      various portions of the mean rh sentences.
    *                      See the structure definiton for more details.
    *        DO_MAX_WIND - structure containing the flags which control generation
    *                      of various portions of the maximum wind sentences.
    *                      See the structure definition for more details.
    *        DO_MAX_GUST - structure containing the flags which control generation
    *                      of various portions of the maximum gust wind sentences.
    *                      See the structure definition for more details.
    *     DO_RESULT_WIND - structure containing the flags which control generation
    *                      of various portions of the result wind sentences.
    *                      See the structure definition for more details.
    *            DO_COOL - structure contaning the flags which control generation of
    *                      various portions of the cooling days sentences.
    *                      See the structure defintion for more details.
    *            DO_HEAT - structure containg the flags which control generation of
    *                      various portions of the heating days sentences.
    *                      See the structure definition for more details
    *           DO_ASTRO - structure containing the flags which control generation
    *                      of various portions of the astro sentences.
    *                      See the structure definition for more details.
    *            DO_NORM - structure containing the flags which control generation
    *                      of various portions of the normal sentences.
    *                      See the structure definition for more details.
    *         DO_WEATHER - structure containing the flags which control generation
    *                      of various portions of the sensible weather report.
    *                      See the structure definition for more details.
    *        DO_POSS_SUN - structure containing the flags which control generation
    *                      of various portions of the possible sunshine report.
    *                      See the structure definition for more details.
    *       DO_SKY_COVER - structure containing the flags which control generation
    *                      of various portions of the sky cover sentence.
    *                      See the structure definition for more details.
    *              ITYPE - flag that determines the type of climate summary
    *                      - 1 morning weather radio daily climate summary
    *                      - 2 evening weather radio daily climate summary
    *                      - 3 nwws morning daily climate summary
    *                      - 4 nwws evening daily climate summary
    *                      - 5 monthly radio climate summary
    *                      - 6 monthly nwws climate summary
    *        COOL_REPORT - Structure containing the begin and end dates
    *                      for the cooling degree day summary period.
    *        HEAT_REPORT - Structure containing the begin and end dates
    *                      for the heating degree day summary period.
    *        SNOW_REPORT - Structure containing the begin and end dates
    *                      for the snow summary period.
    *          LAST_YEAR - Structure containing last years climatology.
    *       NUM_STATIONS - number of stations in this group to be summarized.
    *         NWR_HEADER - structure containing the opening data for the CRS.
    *            SUNRISE - structure containing the valid times for the sunrise
    *                      portion of the astro sentence.
    *             SUNSET - structure containing the valid times for the sunset
    *                      portion of the astro sentence.
    *          T_CLIMATE - Structure containing today climatology.
    *             Y_DATE - date structure containing valid date for the climate
    *                      summary.  See the structure definition for more details
    *          Y_CLIMATE - structure containing historical climatology for a given
    *                      date.  See the structure definition for more details
    *          YESTERDAY - structure containing the observed climate data.
    *                      See the structure definition for more details
    *         VALID_TIME - structure containing the valid time which includes hour,
    *                      minutes, AMPM, and zone.
    *
    *     Non-system routines used
    *     build_am_radio - main driver for the NOAA Weather Radio morning daily
    *                      climate summary.
    *      build_am_wire - main driver for the NOAA Weather Wire Service morning
    *                      daily climate summary.
    *     build_pm_radio - main driver for the NOAA Weather Radio evening daily
    *                      climate summary.
    *      build_pm_wire - main driver for the NOAA Weather Wire Service morning
    *                      daily climate summary.
    *build_monthly_radio - main driver for the NOAA Weather Radio monthly climate
    *                      summary.
    *build_monthly_radio - main driver for the NOAA Weather Wire Service monthly
    *                      climate summary.
    *
    *     Non-system functions used
    *
    *
    *    CHANGE LOG    
    *  NAME                  DATE           CHANGE
    * D.T. Miller           6/15/99         Added code for intermediate daily
    *                                       climate summaries
    * Doug Murphy           7/24/00         Problems with global_values and
    *                                       no_colon fixed
    * Doug Murphy       1/10/02         Added call to function creating
    *                                       RER file
     * </pre>
     * 
     * 
     * @param reportData
     * @param settings
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private Map<String, ClimateProduct> formatClimate(
            ClimateCreatorResponse reportData, ClimateProductType settings)
                    throws ClimateQueryException,
                    ClimateInvalidParameterException {

        Map<String, ClimateProduct> productMap;

        switch (settings.getReportType()) {

        case MORN_RAD:
        case EVEN_RAD:
        case INTER_RAD:
            ClimateFormat dailyRadioFormatter = new ClimateNWRDailyFormat(
                    settings, globalConfig);
            productMap = dailyRadioFormatter.buildText(reportData);
            break;
        case MORN_NWWS:
        case EVEN_NWWS:
        case INTER_NWWS:
            ClimateFormat dailyWireFormatter = new ClimateNWWSDailyFormat(
                    settings, globalConfig);
            productMap = dailyWireFormatter.buildText(reportData);

            // Climate record data is stored only with daily nwws.
            writeStationInfo(settings);
            // buildText() for ClimateNWWSDailyFormat also creates a list of any
            // new records.
            // getDailyRecordData() returns that list.
            rawDatas.addAll(((ClimateNWWSDailyFormat) dailyWireFormatter)
                    .getDailyRecordData());
            break;
        case MONTHLY_RAD:
        case SEASONAL_RAD:
        case ANNUAL_RAD:
            ClimateFormat periodRadioFormatter = new ClimateNWRPeriodFormat(
                    settings, globalConfig);
            productMap = periodRadioFormatter.buildText(reportData);
            break;
        case MONTHLY_NWWS:
        case SEASONAL_NWWS:
        case ANNUAL_NWWS:
            ClimateFormat periodWireFormatter = new ClimateNWWSPeriodFormat(
                    settings, globalConfig);
            productMap = periodWireFormatter.buildText(reportData);
            break;
        default:
            throw new ClimateInvalidParameterException("Invalid period type: "
                    + settings.getReportType().toString());

        }

        return productMap;
    }

    /**
     * Migrated from the write_station_info function in RecEvntRoutines.C. Maps
     * station info for each NWWS product to an output file used to create RERs.
     * 
     * @param settings
     */
    private void writeStationInfo(ClimateProductType settings) {

        List<StationInfo> stationInfo = new ArrayList<StationInfo>();
        // AFOS ID
        String afosId = settings.getHeader().getNodeOrigSite() + RER_STRING
                + settings.getHeader().getStationId();
        for (Station station : settings.getStations()) {

            StationInfo info = new StationInfo(
                    String.valueOf(station.getInformId()),
                    station.getStationName());

            stationInfo.add(info);
        }

        stationInfoMap.put(afosId, stationInfo);
    }

}
