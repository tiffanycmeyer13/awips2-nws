/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import java.util.HashMap;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;

/**
 * Response for Climate Creator daily report.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 NOV 2016  21378      amoore      Initial creation
 * 01 DEC 2016  20414      amoore      Cleanup + comments.
 * 06 DEC 2016  20414      amoore      Reorg of report data.
 * 01 NOV 2017  30504      amoore      Rename and reorg.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
@DynamicSerialize
public class ClimateRunDailyData extends ClimateRunData {

    /**
     * Report data map by station ID.
     */
    @DynamicSerializeElement
    private HashMap<Integer, ClimateDailyReportData> reportMap;

    /**
     * Empty constructor.
     */
    public ClimateRunDailyData() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param iPeriodType
     *            period type.
     * @param iADate
     *            date for the report.
     * @param iReportMap
     *            report data map.
     */
    public ClimateRunDailyData(PeriodType iPeriodType,
            ClimateDate iADate,
            HashMap<Integer, ClimateDailyReportData> iReportMap) {
        super(iPeriodType, iADate);
        reportMap = iReportMap;
    }

    /**
     * @return report data map by station ID.
     */
    public HashMap<Integer, ClimateDailyReportData> getReportMap() {
        return reportMap;
    }

    /**
     * @param iReportMap
     *            by station ID.
     */
    public void setReportMap(
            HashMap<Integer, ClimateDailyReportData> iReportMap) {
        reportMap = iReportMap;
    }
}