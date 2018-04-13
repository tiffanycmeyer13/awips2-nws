/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import java.util.HashMap;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;

/**
 * Response for Climate Creator period report.
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
public class ClimateRunPeriodData extends ClimateRunData {

    /**
     * End date of report. In Legacy Period was used by write_period_data.c and
     * write_period_climo.c to make the files of format "data_*" and
     * "history_*", respectively.
     */
    @DynamicSerializeElement
    private ClimateDate endDate;

    /**
     * Report data map by station ID.
     */
    @DynamicSerializeElement
    private HashMap<Integer, ClimatePeriodReportData> reportMap;

    /**
     * Empty constructor.
     */
    public ClimateRunPeriodData() {
        super();
    }

    /**
     * 
     * @param iPeriodType
     * @param iBeginDate
     * @param iEndDate
     * @param iReportMap
     */
    public ClimateRunPeriodData(PeriodType iPeriodType,
            ClimateDate iBeginDate, ClimateDate iEndDate,
            HashMap<Integer, ClimatePeriodReportData> iReportMap) {
        super(iPeriodType, iBeginDate);
        endDate = iEndDate;
        reportMap = iReportMap;
    }

    /**
     * @return the endDate
     */
    public ClimateDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate(ClimateDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return report data map by station ID.
     */
    public HashMap<Integer, ClimatePeriodReportData> getReportMap() {
        return reportMap;
    }

    /**
     * @param iReportMap
     *            by station ID.
     */
    public void setReportMap(
            HashMap<Integer, ClimatePeriodReportData> iReportMap) {
        reportMap = iReportMap;
    }
}