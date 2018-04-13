/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Response for Climate Creator.
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
public abstract class ClimateRunData {

    /**
     * Period type of this response.
     */
    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * Begin date (or for Daily data, the only date). In Legacy Daily was used
     * by write_daily_info.f, write_climate_data.f, and write_daily_data.f to
     * make the files of format "info_*", "history_*", and "data_*",
     * respectively. In Legacy Period was used by write_period_data.c and
     * write_period_climo.c to make the files of format "data_*" and
     * "history_*", respectively.
     */
    @DynamicSerializeElement
    private ClimateDate beginDate;

    /**
     * Empty constructor.
     */
    public ClimateRunData() {
    }

    /**
     * 
     * @param iPeriodType
     * @param iBeginDate
     * @param iStations
     */
    public ClimateRunData(PeriodType iPeriodType,
            ClimateDate iBeginDate) {
        periodType = iPeriodType;
        beginDate = iBeginDate;
    }

    /**
     * @return the periodType
     */
    public PeriodType getPeriodType() {
        return periodType;
    }

    /**
     * @param periodType
     *            the periodType to set
     */
    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    /**
     * @return the beginDate
     */
    public ClimateDate getBeginDate() {
        return beginDate;
    }

    /**
     * @param beginDate
     *            the beginDate to set
     */
    public void setBeginDate(ClimateDate beginDate) {
        this.beginDate = beginDate;
    }
}