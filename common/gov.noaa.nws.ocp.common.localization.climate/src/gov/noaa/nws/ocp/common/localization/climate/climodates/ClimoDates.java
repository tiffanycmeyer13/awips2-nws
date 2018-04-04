/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.climodates;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 02 APR 2018  DR17116     wpaintsil   Initial creation
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimoDates")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimoDates {
    /**
     * Snow season begin dates and end dates.
     */
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "SnowSeasons", type = ClimateDate.class) })
    private List<ClimateDate> snowSeasons;

    /**
     * Snow year begin date and end date.
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowYear")
    private ClimateDate snowYear;

    /**
     * Precip season begin dates and end dates.
     */
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "PrecipSeasons", type = ClimateDate.class) })
    private List<ClimateDate> precipSeasons;

    /**
     * Precip year begin date and end date.
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipYear")
    private ClimateDate precipYear;

    /**
     * default day for precipitation
     */
    public static final int PRECIP_DEFAULT_DAY = 1;

    /**
     * default month for precipitation
     */
    public static final int PRECIP_DEFAULT_MON = 1;

    /**
     * default day for snow
     */
    public static final int SNOW_DEFAULT_DAY = 1;

    /**
     * default month for snow
     */
    public final static int SNOW_DEFAULT_MON = 7;

    public ClimoDates() {
    }

    /**
     * @return the snowSeason
     */
    public List<ClimateDate> getSnowSeasons() {
        return snowSeasons;
    }

    /**
     * @param snowSeason
     *            the snowSeason to set
     */
    public void setSnowSeasons(List<ClimateDate> snowSeason) {
        this.snowSeasons = snowSeason;
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

    /**
     * @return the precipSeason
     */
    public List<ClimateDate> getPrecipSeasons() {
        return precipSeasons;
    }

    /**
     * @param precipSeason
     *            the precipSeason to set
     */
    public void setPrecipSeasons(List<ClimateDate> precipSeason) {
        this.precipSeasons = precipSeason;
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
     * Get default ClimoDates object.
     * 
     * @return ClimoDates
     */
    public static ClimoDates getDefaultClimoDates() {
        ClimoDates defaultDates = new ClimoDates();

        defaultDates.setDefaultClimoDates();

        return defaultDates;
    }

    /**
     * Set the default values.
     */
    public void setDefaultClimoDates() {
        // The climo_dates table from which this class is adapted doesn't store
        // years.
        int year = ParameterFormatClimate.MISSING;

        ClimateDate snowSeasonBeg = new ClimateDate(SNOW_DEFAULT_DAY,
                SNOW_DEFAULT_MON, year);

        ClimateDate precipSeasonBeg = new ClimateDate(PRECIP_DEFAULT_DAY,
                PRECIP_DEFAULT_MON, year);

        snowSeasons = new ArrayList<>();
        snowSeasons.add(snowSeasonBeg);

        precipSeasons = new ArrayList<>();
        precipSeasons.add(precipSeasonBeg);

        // Annual Values Currently ALWAYS stay the same
        snowYear = new ClimateDate(1, 7, year);

        precipYear = new ClimateDate(1, 1, year);

    }
}
