/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.report;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Period data for Climate Reports.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 06 DEC 2016  20414      amoore      Initial creation.
 * 04 APR 2017  30166      amoore      Add copy constructor.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
@DynamicSerialize
public class ClimatePeriodReportData extends ClimateReportData {

    /**
     * Period data, originally written out by write_period_data.c alongside date
     * information to make the file of format "data_*".
     */
    /**
     * Period data for the begin and end dates.
     */
    @DynamicSerializeElement
    private PeriodData data;

    /**
     * Last year's period data for the equivalent period.
     */
    @DynamicSerializeElement
    private PeriodData lastYearData;
    /**
     * End period data.
     */

    /**
     * Normals data for the period, originally written out by
     * write_period_climo.c alongside date information to make the file of
     * format "history_*".
     */
    @DynamicSerializeElement
    private PeriodClimo climo;

    /**
     * Empty constructor.
     */
    public ClimatePeriodReportData() {
    }

    /**
     * Copy constructor.
     * 
     * @param other
     */
    public ClimatePeriodReportData(ClimatePeriodReportData other) {
        super(other);
        data = new PeriodData(other.getData());
        lastYearData = new PeriodData(other.getData());
        climo = new PeriodClimo(other.getClimo());
    }

    /**
     * 
     * @param iStation
     * @param iData
     * @param iLastYearData
     * @param iClimo
     */
    public ClimatePeriodReportData(Station iStation, PeriodData iData,
            PeriodData iLastYearData, PeriodClimo iClimo) {
        super(iStation);
        data = iData;
        lastYearData = iLastYearData;
        climo = iClimo;
    }

    /**
     * @return the data
     */
    public PeriodData getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(PeriodData data) {
        this.data = data;
    }

    /**
     * @return the lastYearData
     */
    public PeriodData getLastYearData() {
        return lastYearData;
    }

    /**
     * @param lastYearData
     *            the lastYearData to set
     */
    public void setLastYearData(PeriodData lastYearData) {
        this.lastYearData = lastYearData;
    }

    /**
     * @return the climo
     */
    public PeriodClimo getClimo() {
        return climo;
    }

    /**
     * @param climo
     *            the climo to set
     */
    public void setClimo(PeriodClimo climo) {
        this.climo = climo;
    }
}