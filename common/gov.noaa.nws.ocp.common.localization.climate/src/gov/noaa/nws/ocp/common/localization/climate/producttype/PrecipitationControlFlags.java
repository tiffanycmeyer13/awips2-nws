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
 * Class containing the snow control flags. Variables P1 and P2 are user-defined
 * precipitation thresholds. Converted from "TYPE_liquid_precip_control_h.h".
 *
 * Note: precipMonth, precipSeason, precipYear are not modifiable from GUI.
 *
 * <pre>
 *
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 * Feb 07, 2017 20640      jwu          Add precipMonth/precipSeason/precipYear.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "Precipitation")
@XmlAccessorType(XmlAccessType.NONE)
public class PrecipitationControlFlags {

    /**
     * Flags for total precip
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipTotal")
    private ClimateProductFlags precipTotal;

    /**
     * Flags for monthly precip
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipMonth")
    private ClimateProductFlags precipMonth;

    /**
     * Flags for seasonal precip
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipSeason")
    private ClimateProductFlags precipSeason;

    /**
     * Flags for yearly precip
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipYear")
    private ClimateProductFlags precipYear;

    /**
     * Flags for record min precip
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipMin")
    private ClimateProductFlags precipMin;

    /**
     * Flags for # days w/ precip > 0.01"
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipGE01")
    private ClimateProductFlags precipGE01;

    /**
     * Flags for # days w/ precip > 0.10"
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipGE10")
    private ClimateProductFlags precipGE10;

    /**
     * Flags for # days w/ precip > 0.50"
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipGE50")
    private ClimateProductFlags precipGE50;

    /**
     * Flags for # days w/ precip > 1.00"
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipGE100")
    private ClimateProductFlags precipGE100;

    /**
     * Flags for # days w/ precip > P1
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipGEP1")
    private ClimateProductFlags precipGEP1;

    /**
     * Flags for # days w/ precip > P2
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipGEP2")
    private ClimateProductFlags precipGEP2;

    /**
     * Flags for max 24 hour precip amount
     */
    @DynamicSerializeElement
    @XmlElement(name = "Precip24HR")
    private ClimateProductFlags precip24HR;

    /**
     * Flags for max storm precip amount
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipStormMax")
    private ClimateProductFlags precipStormMax;

    /**
     * Flags for avg daily precip amount
     */
    @DynamicSerializeElement
    @XmlElement(name = "PrecipAvg")
    private ClimateProductFlags precipAvg;

    /**
     * Default constructor
     */
    public PrecipitationControlFlags() {
    }

    /**
     * @return the precipTotal
     */
    public ClimateProductFlags getPrecipTotal() {
        return precipTotal;
    }

    /**
     * @param precipTotal
     *            the precipTotal to set
     */
    public void setPrecipTotal(ClimateProductFlags precipTotal) {
        this.precipTotal = precipTotal;
    }

    /**
     * @return the precipMonth
     */
    public ClimateProductFlags getPrecipMonth() {
        return precipMonth;
    }

    /**
     * @param precipMonth
     *            the precipMonth to set
     */
    public void setPrecipMonth(ClimateProductFlags precipMonth) {
        this.precipMonth = precipMonth;
    }

    /**
     * @return the precipSeason
     */
    public ClimateProductFlags getPrecipSeason() {
        return precipSeason;
    }

    /**
     * @param precipSeason
     *            the precipSeason to set
     */
    public void setPrecipSeason(ClimateProductFlags precipSeason) {
        this.precipSeason = precipSeason;
    }

    /**
     * @return the precipYear
     */
    public ClimateProductFlags getPrecipYear() {
        return precipYear;
    }

    /**
     * @param precipYear
     *            the precipYear to set
     */
    public void setPrecipYear(ClimateProductFlags precipYear) {
        this.precipYear = precipYear;
    }

    /**
     * @return the precipMin
     */
    public ClimateProductFlags getPrecipMin() {
        return precipMin;
    }

    /**
     * @param precipMin
     *            the precipMin to set
     */
    public void setPrecipMin(ClimateProductFlags precipMin) {
        this.precipMin = precipMin;
    }

    /**
     * @return the precipGE01
     */
    public ClimateProductFlags getPrecipGE01() {
        return precipGE01;
    }

    /**
     * @param precipGE01
     *            the precipGE01 to set
     */
    public void setPrecipGE01(ClimateProductFlags precipGE01) {
        this.precipGE01 = precipGE01;
    }

    /**
     * @return the precipGE10
     */
    public ClimateProductFlags getPrecipGE10() {
        return precipGE10;
    }

    /**
     * @param precipGE10
     *            the precipGE10 to set
     */
    public void setPrecipGE10(ClimateProductFlags precipGE10) {
        this.precipGE10 = precipGE10;
    }

    /**
     * @return the precipGE50
     */
    public ClimateProductFlags getPrecipGE50() {
        return precipGE50;
    }

    /**
     * @param precipGE50
     *            the precipGE50 to set
     */
    public void setPrecipGE50(ClimateProductFlags precipGE50) {
        this.precipGE50 = precipGE50;
    }

    /**
     * @return the precipGE100
     */
    public ClimateProductFlags getPrecipGE100() {
        return precipGE100;
    }

    /**
     * @param precipGE100
     *            the precipGE100 to set
     */
    public void setPrecipGE100(ClimateProductFlags precipGE100) {
        this.precipGE100 = precipGE100;
    }

    /**
     * @return the precipGEP1
     */
    public ClimateProductFlags getPrecipGEP1() {
        return precipGEP1;
    }

    /**
     * @param precipGEP1
     *            the precipGEP1 to set
     */
    public void setPrecipGEP1(ClimateProductFlags precipGEP1) {
        this.precipGEP1 = precipGEP1;
    }

    /**
     * @return the precipGEP2
     */
    public ClimateProductFlags getPrecipGEP2() {
        return precipGEP2;
    }

    /**
     * @param precipGEP2
     *            the precipGEP2 to set
     */
    public void setPrecipGEP2(ClimateProductFlags precipGEP2) {
        this.precipGEP2 = precipGEP2;
    }

    /**
     * @return the precip24HR
     */
    public ClimateProductFlags getPrecip24HR() {
        return precip24HR;
    }

    /**
     * @param precip24hr
     *            the precip24HR to set
     */
    public void setPrecip24HR(ClimateProductFlags precip24hr) {
        precip24HR = precip24hr;
    }

    /**
     * @return the precipStormMax
     */
    public ClimateProductFlags getPrecipStormMax() {
        return precipStormMax;
    }

    /**
     * @param precipStormMax
     *            the precipStormMax to set
     */
    public void setPrecipStormMax(ClimateProductFlags precipStormMax) {
        this.precipStormMax = precipStormMax;
    }

    /**
     * @return the precipAvg
     */
    public ClimateProductFlags getPrecipAvg() {
        return precipAvg;
    }

    /**
     * @param precipAvg
     *            the precipAvg to set
     */
    public void setPrecipAvg(ClimateProductFlags precipAvg) {
        this.precipAvg = precipAvg;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.precipTotal = ClimateProductFlags.getDefaultFlags();
        this.precipMonth = ClimateProductFlags.getDefaultFlags();
        this.precipSeason = ClimateProductFlags.getDefaultFlags();
        this.precipYear = ClimateProductFlags.getDefaultFlags();
        this.precipMin = ClimateProductFlags.getDefaultFlags();
        this.precipGE01 = ClimateProductFlags.getDefaultFlags();
        this.precipGE10 = ClimateProductFlags.getDefaultFlags();
        this.precipGE50 = ClimateProductFlags.getDefaultFlags();
        this.precipGE100 = ClimateProductFlags.getDefaultFlags();
        this.precipGEP1 = ClimateProductFlags.getDefaultFlags();
        this.precipGEP2 = ClimateProductFlags.getDefaultFlags();
        this.precip24HR = ClimateProductFlags.getDefaultFlags();
        this.precipStormMax = ClimateProductFlags.getDefaultFlags();
        this.precipAvg = ClimateProductFlags.getDefaultFlags();
    }

    /**
     * @return a deep copy of this object.
     */
    public PrecipitationControlFlags copy() {

        PrecipitationControlFlags flags = new PrecipitationControlFlags();

        flags.precipTotal = this.precipTotal.copy();
        flags.precipMonth = this.precipMonth.copy();
        flags.precipSeason = this.precipSeason.copy();
        flags.precipYear = this.precipYear.copy();
        flags.precipMin = this.precipMin.copy();
        flags.precipGE01 = this.precipGE01.copy();
        flags.precipGE10 = this.precipGE10.copy();
        flags.precipGE50 = this.precipGE50.copy();
        flags.precipGE100 = this.precipGE100.copy();
        flags.precipGEP1 = this.precipGEP1.copy();
        flags.precipGEP2 = this.precipGEP2.copy();
        flags.precip24HR = this.precip24HR.copy();
        flags.precipStormMax = this.precipStormMax.copy();
        flags.precipAvg = this.precipAvg.copy();

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static PrecipitationControlFlags getDefaultFlags() {
        PrecipitationControlFlags flags = new PrecipitationControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
