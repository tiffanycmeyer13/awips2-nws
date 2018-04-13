/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.producttype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 *
 * Class containing the snow control flags. Variables P1 are user-defined
 * precipitation thresholds. Converted from "TYPE_snow_precip_control_h.h".
 * 
 * Note: snowMonth, snowSeason, snowYear are not modifiable from GUI.
 *
 * <pre>
 *
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 * Feb 07, 2017 20640      jwu          Add snowMonth/snowSeason/snowYear.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "Snow")
@XmlAccessorType(XmlAccessType.NONE)
public class SnowControlFlags {

    /**
     * Flags for total snowfall
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowTotal")
    private ClimateProductFlags snowTotal;

    /**
     * Flags for monthly snowfall
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowMonth")
    private ClimateProductFlags snowMonth;

    /**
     * Flags for seasonal snowfall
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowSeason")
    private ClimateProductFlags snowSeason;

    /**
     * Flags for annual snowfall
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowYear")
    private ClimateProductFlags snowYear;

    /**
     * Flags for average snow depth
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowDepthAvg")
    private ClimateProductFlags snowDepthAvg;

    /** Flags for snowfall since July 1 */
    @DynamicSerializeElement
    @XmlElement(name = "SnowJuly1")
    private ClimateProductFlags snowJuly1;

    /**
     * Flags for number of days with any snowfall
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowAny")
    private ClimateProductFlags snowAny;

    /**
     * Flags for number of days w/ snowfall >1.0in
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowGE100")
    private ClimateProductFlags snowGE100;

    /**
     * Flags for number of days w/ snowfall >P1
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowGEP1")
    private ClimateProductFlags snowGEP1;

    /**
     * Flags for maximum 24 hour snowfall amount
     */
    @DynamicSerializeElement
    @XmlElement(name = "Snow24hr")
    private ClimateProductFlags snow24hr;

    /**
     * Flags for maximum snow storm amount
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowStormMax")
    private ClimateProductFlags snowStormMax;

    /**
     * Flags for total water equivalent
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowWaterTotal")
    private ClimateProductFlags snowWaterTotal;

    /**
     * Flags for total water equiv. since July 1
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowWaterJuly1")
    private ClimateProductFlags snowWaterJuly1;

    /**
     * Flags for max snow depth
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowDepthMax")
    private ClimateProductFlags snowDepthMax;

    /**
     * Default constructor
     */
    public SnowControlFlags() {
    }

    /**
     * @return the snowTotal
     */
    public ClimateProductFlags getSnowTotal() {
        return snowTotal;
    }

    /**
     * @param snowTotal
     *            the snowTotal to set
     */
    public void setSnowTotal(ClimateProductFlags snowTotal) {
        this.snowTotal = snowTotal;
    }

    /**
     * @return the snowMonth
     */
    public ClimateProductFlags getSnowMonth() {
        return snowMonth;
    }

    /**
     * @param snowMonth
     *            the snowMonth to set
     */
    public void setSnowMonth(ClimateProductFlags snowMonth) {
        this.snowMonth = snowMonth;
    }

    /**
     * @return the snowSeason
     */
    public ClimateProductFlags getSnowSeason() {
        return snowSeason;
    }

    /**
     * @param snowSeason
     *            the snowSeason to set
     */
    public void setSnowSeason(ClimateProductFlags snowSeason) {
        this.snowSeason = snowSeason;
    }

    /**
     * @return the snowYear
     */
    public ClimateProductFlags getSnowYear() {
        return snowYear;
    }

    /**
     * @param snowYear
     *            the snowYear to set
     */
    public void setSnowYear(ClimateProductFlags snowYear) {
        this.snowYear = snowYear;
    }

    /**
     * @return the snowJuly1
     */
    public ClimateProductFlags getSnowJuly1() {
        return snowJuly1;
    }

    /**
     * @param snowJuly1
     *            the snowJuly1 to set
     */
    public void setSnowJuly1(ClimateProductFlags snowJuly1) {
        this.snowJuly1 = snowJuly1;
    }

    /**
     * @return the snowAny
     */
    public ClimateProductFlags getSnowAny() {
        return snowAny;
    }

    /**
     * @param snowAny
     *            the snowAny to set
     */
    public void setSnowAny(ClimateProductFlags snowAny) {
        this.snowAny = snowAny;
    }

    /**
     * @return the snowGE100
     */
    public ClimateProductFlags getSnowGE100() {
        return snowGE100;
    }

    /**
     * @param snowGE100
     *            the snowGE100 to set
     */
    public void setSnowGE100(ClimateProductFlags snowGE100) {
        this.snowGE100 = snowGE100;
    }

    /**
     * @return the snowGEP1
     */
    public ClimateProductFlags getSnowGEP1() {
        return snowGEP1;
    }

    /**
     * @param snowGEP1
     *            the snowGEP1 to set
     */
    public void setSnowGEP1(ClimateProductFlags snowGEP1) {
        this.snowGEP1 = snowGEP1;
    }

    /**
     * @return the snow24hr
     */
    public ClimateProductFlags getSnow24hr() {
        return snow24hr;
    }

    /**
     * @param snow24hr
     *            the snow24hr to set
     */
    public void setSnow24hr(ClimateProductFlags snow24hr) {
        this.snow24hr = snow24hr;
    }

    /**
     * @return the snowStormMax
     */
    public ClimateProductFlags getSnowStormMax() {
        return snowStormMax;
    }

    /**
     * @param snowStormMax
     *            the snowStormMax to set
     */
    public void setSnowStormMax(ClimateProductFlags snowStormMax) {
        this.snowStormMax = snowStormMax;
    }

    /**
     * @return the snowWaterTotal
     */
    public ClimateProductFlags getSnowWaterTotal() {
        return snowWaterTotal;
    }

    /**
     * @param snowWaterTotal
     *            the snowWaterTotal to set
     */
    public void setSnowWaterTotal(ClimateProductFlags snowWaterTotal) {
        this.snowWaterTotal = snowWaterTotal;
    }

    /**
     * @return the snowWaterJuly1
     */
    public ClimateProductFlags getSnowWaterJuly1() {
        return snowWaterJuly1;
    }

    /**
     * @param snowWaterJuly1
     *            the snowWaterJuly1 to set
     */
    public void setSnowWaterJuly1(ClimateProductFlags snowWaterJuly1) {
        this.snowWaterJuly1 = snowWaterJuly1;
    }

    /**
     * @return the snowDepthMax
     */
    public ClimateProductFlags getSnowDepthMax() {
        return snowDepthMax;
    }

    /**
     * @param snowDepthMax
     *            the snowDepthMax to set
     */
    public void setSnowDepthMax(ClimateProductFlags snowDepthMax) {
        this.snowDepthMax = snowDepthMax;
    }

    /**
     * @return the snowDepthAvg
     */
    public ClimateProductFlags getSnowDepthAvg() {
        return snowDepthAvg;
    }

    /**
     * @param snowDepthAvg
     *            the snowDepthAvg to set
     */
    public void setSnowDepthAvg(ClimateProductFlags snowDepthAvg) {
        this.snowDepthAvg = snowDepthAvg;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.snowTotal = ClimateProductFlags.getDefaultFlags();
        this.snowMonth = ClimateProductFlags.getDefaultFlags();
        this.snowSeason = ClimateProductFlags.getDefaultFlags();
        this.snowYear = ClimateProductFlags.getDefaultFlags();
        this.snowJuly1 = ClimateProductFlags.getDefaultFlags();
        this.snowAny = ClimateProductFlags.getDefaultFlags();
        this.snowGE100 = ClimateProductFlags.getDefaultFlags();
        this.snowGEP1 = ClimateProductFlags.getDefaultFlags();
        this.snow24hr = ClimateProductFlags.getDefaultFlags();
        this.snowStormMax = ClimateProductFlags.getDefaultFlags();
        this.snowWaterTotal = ClimateProductFlags.getDefaultFlags();
        this.snowWaterJuly1 = ClimateProductFlags.getDefaultFlags();
        this.snowDepthMax = ClimateProductFlags.getDefaultFlags();
        this.snowDepthAvg = ClimateProductFlags.getDefaultFlags();

        /*
         * Note - snowMonth/snowSeason/snowYear cannot be changed via GUI and
         * the following flags are set to true in legacy by default......
         */
        this.snowMonth.setMeasured(true);
        this.snowMonth.setRecordYear(true);
        this.snowYear.setTimeOfMeasured(true);
    }

    /**
     * @return a deep copy of this object.
     */
    public SnowControlFlags copy() {

        SnowControlFlags flags = new SnowControlFlags();

        flags.snowTotal = this.snowTotal.copy();
        flags.snowMonth = this.snowMonth.copy();
        flags.snowSeason = this.snowSeason.copy();
        flags.snowYear = this.snowYear.copy();
        flags.snowJuly1 = this.snowJuly1.copy();
        flags.snowAny = this.snowAny.copy();
        flags.snowGE100 = this.snowGE100.copy();
        flags.snowGEP1 = this.snowGEP1.copy();
        flags.snow24hr = this.snow24hr.copy();
        flags.snowStormMax = this.snowStormMax.copy();
        flags.snowWaterTotal = this.snowWaterTotal.copy();
        flags.snowWaterJuly1 = this.snowWaterJuly1.copy();
        flags.snowDepthMax = this.snowDepthMax.copy();
        flags.snowDepthAvg = this.snowDepthAvg.copy();

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static SnowControlFlags getDefaultFlags() {
        SnowControlFlags flags = new SnowControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
