/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.report;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Data for Climate Daily Reports.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05 DEC 2016  20414      amoore      Initial creation.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
@DynamicSerialize
public class ClimateDailyReportData extends ClimateReportData {

    /**
     * Info data, originally written out using write_daily_info.f alongside date
     * and station info to a file of format "info_*".
     */
    /**
     * Sunrise times.
     */
    @DynamicSerializeElement
    private ClimateTime[] sunrise;

    /**
     * Sunset times.
     */
    @DynamicSerializeElement
    private ClimateTime[] sunset;
    /**
     * End info data.
     */

    /**
     * Daily data, originally written out using write_daily_data.f alongside the
     * date to a file of format "data_*".
     */
    /**
     * Yesterday's/today's data (depending on period type).
     */
    @DynamicSerializeElement
    private DailyClimateData data;

    /**
     * Last year's data for the equivalent day.
     */
    @DynamicSerializeElement
    private DailyClimateData lastYearData;
    /**
     * End daily data.
     */

    /**
     * Historical data, originally written out to using write_climate_data.f
     * alongside the date to a file of format "history_*".
     */
    /**
     * Yesterday's climate record data.
     */
    @DynamicSerializeElement
    private ClimateRecordDay yClimate;

    /**
     * Today's climate record data.
     */
    @DynamicSerializeElement
    private ClimateRecordDay tClimate;
    /**
     * End historical data.
     */

    /**
     * Empty constructor.
     */
    public ClimateDailyReportData() {
    }

    /**
     * Constructor.
     * 
     * @param iStation
     *            station.
     * @param iSunrise
     *            sunrise info for station.
     * @param iSunset
     *            sunset info for station.
     * @param iData
     *            yesterday's/today's data, depending on the report.
     * @param iLastYearData
     *            last year's data for the equivalent day.
     * @param iYClimate
     *            yesterday's record data.
     * @param iTClimate
     *            today's record data.
     */
    public ClimateDailyReportData(Station iStation, ClimateTime[] iSunrise,
            ClimateTime[] iSunset, DailyClimateData iData,
            DailyClimateData iLastYearData, ClimateRecordDay iYClimate,
            ClimateRecordDay iTClimate) {
        super(iStation);
        sunrise = iSunrise;
        sunset = iSunset;
        data = iData;
        lastYearData = iLastYearData;
        yClimate = iYClimate;
        tClimate = iTClimate;
    }

    /**
     * @return the sunrise
     */
    public ClimateTime[] getSunrise() {
        return sunrise;
    }

    /**
     * @param sunrise
     *            the sunrise to set
     */
    public void setSunrise(ClimateTime[] sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * @return the sunset
     */
    public ClimateTime[] getSunset() {
        return sunset;
    }

    /**
     * @param sunset
     *            the sunset to set
     */
    public void setSunset(ClimateTime[] sunset) {
        this.sunset = sunset;
    }

    /**
     * @return the data
     */
    public DailyClimateData getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(DailyClimateData data) {
        this.data = data;
    }

    /**
     * @return the lastYearData
     */
    public DailyClimateData getLastYearData() {
        return lastYearData;
    }

    /**
     * @param lastYearData
     *            the lastYearData to set
     */
    public void setLastYearData(DailyClimateData lastYearData) {
        this.lastYearData = lastYearData;
    }

    /**
     * @return the yClimate
     */
    public ClimateRecordDay getyClimate() {
        return yClimate;
    }

    /**
     * @param yClimate
     *            the yClimate to set
     */
    public void setyClimate(ClimateRecordDay yClimate) {
        this.yClimate = yClimate;
    }

    /**
     * @return the tClimate
     */
    public ClimateRecordDay gettClimate() {
        return tClimate;
    }

    /**
     * @param tClimate
     *            the tClimate to set
     */
    public void settClimate(ClimateRecordDay tClimate) {
        this.tClimate = tClimate;
    }
}