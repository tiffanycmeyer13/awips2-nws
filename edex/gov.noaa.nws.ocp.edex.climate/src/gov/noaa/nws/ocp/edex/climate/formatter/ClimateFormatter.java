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
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
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
    private final ClimateGlobal globalConfig;

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
     * @param reportData
     * @return
     * @throws ClimateInvalidParameterException
     * @throws ClimateQueryException
     */// TODO: remove this overload when the operational flag is determined in
      // CPG.
    public Map<String, ClimateProduct> formatClimate(
            ClimateRunData reportData) throws ClimateQueryException,
                    ClimateInvalidParameterException {
        return formatClimate(reportData, true);
    }

    /**
     * Loop through settingsList and call the formatClimate overload method with
     * each.
     * 
     * @param reportData
     * @param operational
     *            true if the CAVE mode is operational; false otherwise
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    public Map<String, ClimateProduct> formatClimate(
            ClimateRunData reportData, boolean operational)
                    throws ClimateQueryException,
                    ClimateInvalidParameterException {

        Map<String, ClimateProduct> products = new HashMap<>();

        if (settingsList == null || settingsList.isEmpty()) {
            logger.error("The list of settings is null or empty.");
        } else {

            if ((reportData instanceof ClimateRunDailyData
                    && ((ClimateRunDailyData) reportData).getReportMap()
                            .isEmpty())
                    || (reportData instanceof ClimateRunPeriodData
                            && ((ClimateRunPeriodData) reportData)
                                    .getReportMap().isEmpty())) {
                logger.warn(
                        "The ClimateRunData parameter contains an empty report map.");
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
                        operational);
            } else {
                logger.debug("Not generating RERs for period type: ["
                        + reportData.getPeriodType() + "]");
            }
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
            ClimateRunData reportData, ClimateProductType settings)
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
