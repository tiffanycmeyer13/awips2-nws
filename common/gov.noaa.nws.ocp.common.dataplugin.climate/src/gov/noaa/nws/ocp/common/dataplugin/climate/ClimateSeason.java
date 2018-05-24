/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.List;

/**
 * 
 * Object that holds cool, heat, snow, and precip seasons and years.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 20 APR 2018  DR17116  wpaintsil   Initial creation
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */
public class ClimateSeason {
    private ClimateDate coolSeason;

    private ClimateDate coolYear;

    private ClimateDate heatSeason;

    private ClimateDate heatYear;

    private List<ClimateDate> precipSeasons;

    private ClimateDate precipYear;

    private List<ClimateDate> snowSeasons;

    private ClimateDate snowYear;

    public ClimateSeason() {

    }

    /**
     * @return the coolSeason
     */
    public ClimateDate getCoolSeason() {
        return coolSeason;
    }

    /**
     * @param coolSeason
     *            the coolSeason to set
     */
    public void setCoolSeason(ClimateDate coolSeason) {
        this.coolSeason = coolSeason;
    }

    /**
     * @return the coolYear
     */
    public ClimateDate getCoolYear() {
        return coolYear;
    }

    /**
     * @param coolYear
     *            the coolYear to set
     */
    public void setCoolYear(ClimateDate coolYear) {
        this.coolYear = coolYear;
    }

    /**
     * @return the heatSeason
     */
    public ClimateDate getHeatSeason() {
        return heatSeason;
    }

    /**
     * @param heatSeason
     *            the heatSeason to set
     */
    public void setHeatSeason(ClimateDate heatSeason) {
        this.heatSeason = heatSeason;
    }

    /**
     * @return the heatYear
     */
    public ClimateDate getHeatYear() {
        return heatYear;
    }

    /**
     * @param heatYear
     *            the heatYear to set
     */
    public void setHeatYear(ClimateDate heatYear) {
        this.heatYear = heatYear;
    }

    /**
     * @return the precipSeasons
     */
    public List<ClimateDate> getPrecipSeasons() {
        return precipSeasons;
    }

    /**
     * @param precipSeasons
     *            the precipSeasons to set
     */
    public void setPrecipSeasons(List<ClimateDate> precipSeasons) {
        this.precipSeasons = precipSeasons;
    }

    /**
     * @return the precipYear
     */
    public ClimateDate getPrecipYear() {
        return precipYear;
    }

    /**
     * @param precipYear
     *            the precipYear to set
     */
    public void setPrecipYear(ClimateDate precipYear) {
        this.precipYear = precipYear;
    }

    /**
     * @return the snowSeasons
     */
    public List<ClimateDate> getSnowSeasons() {
        return snowSeasons;
    }

    /**
     * @param snowSeasons
     *            the snowSeasons to set
     */
    public void setSnowSeasons(List<ClimateDate> snowSeasons) {
        this.snowSeasons = snowSeasons;
    }

    /**
     * @return the snowYear
     */
    public ClimateDate getSnowYear() {
        return snowYear;
    }

    /**
     * @param snowYear
     *            the snowYear to set
     */
    public void setSnowYear(ClimateDate snowYear) {
        this.snowYear = snowYear;
    }
}
