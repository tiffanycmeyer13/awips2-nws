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
 * Class containing the temperature control flags. Variables T1 through T6 are
 * user-defined temperature thresholds. Converted from "TYPE_temp_control.h".
 *
 * <pre>
 *
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "Temperature")
@XmlAccessorType(XmlAccessType.NONE)
public class TemperatureControlFlags {

    /**
     * Flags for max temp
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTemp")
    private ClimateProductFlags maxTemp;

    /**
     * Flags for min temp
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTemp")
    private ClimateProductFlags minTemp;

    /**
     * Flags for mean temp
     */
    @DynamicSerializeElement
    @XmlElement(name = "MeanTemp")
    private ClimateProductFlags meanTemp;

    /**
     * Flags for mean max temp
     */
    @DynamicSerializeElement
    @XmlElement(name = "MeanMaxTemp")
    private ClimateProductFlags meanMaxTemp;

    /**
     * Flags for mean min temp
     */
    @DynamicSerializeElement
    @XmlElement(name = "MeanMinTemp")
    private ClimateProductFlags meanMinTemp;

    /**
     * Flags for days w/ max >90F
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempGE90")
    private ClimateProductFlags maxTempGE90;

    /**
     * Flags for days w/ max <32F
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempLE32")
    private ClimateProductFlags maxTempLE32;

    /**
     * Flags for days w/ max >T1
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempGET1")
    private ClimateProductFlags maxTempGET1;

    /**
     * Flags for days w/ max >T2
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempGET2")
    private ClimateProductFlags maxTempGET2;

    /**
     * Flags for days w/ max <T3
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempLET3")
    private ClimateProductFlags maxTempLET3;

    /**
     * Flags for days w/ min <32F
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempLE32")
    private ClimateProductFlags minTempLE32;

    /**
     * Flags for days w/ min < 0
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempLE0")
    private ClimateProductFlags minTempLE0;

    /**
     * Flags for days w/ min >T4
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempGET4")
    private ClimateProductFlags minTempGET4;

    /**
     * Flags for days w/ min <T5
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempLET5")
    private ClimateProductFlags minTempLET5;

    /**
     * Flags for days w/ min <T6
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempLET6")
    private ClimateProductFlags minTempLET6;

    /**
     * Default constructor
     */
    public TemperatureControlFlags() {
    }

    /**
     * @return the maxTemp
     */
    public ClimateProductFlags getMaxTemp() {
        return maxTemp;
    }

    /**
     * @param maxTemp
     *            the maxTemp to set
     */
    public void setMaxTemp(ClimateProductFlags maxTemp) {
        this.maxTemp = maxTemp;
    }

    /**
     * @return the minTemp
     */
    public ClimateProductFlags getMinTemp() {
        return minTemp;
    }

    /**
     * @param minTemp
     *            the minTemp to set
     */
    public void setMinTemp(ClimateProductFlags minTemp) {
        this.minTemp = minTemp;
    }

    /**
     * @return the meanTemp
     */
    public ClimateProductFlags getMeanTemp() {
        return meanTemp;
    }

    /**
     * @param meanTemp
     *            the meanTemp to set
     */
    public void setMeanTemp(ClimateProductFlags meanTemp) {
        this.meanTemp = meanTemp;
    }

    /**
     * @return the meanMaxTemp
     */
    public ClimateProductFlags getMeanMaxTemp() {
        return meanMaxTemp;
    }

    /**
     * @param meanMaxTemp
     *            the meanMaxTemp to set
     */
    public void setMeanMaxTemp(ClimateProductFlags meanMaxTemp) {
        this.meanMaxTemp = meanMaxTemp;
    }

    /**
     * @return the meanMinTemp
     */
    public ClimateProductFlags getMeanMinTemp() {
        return meanMinTemp;
    }

    /**
     * @param meanMinTemp
     *            the meanMinTemp to set
     */
    public void setMeanMinTemp(ClimateProductFlags meanMinTemp) {
        this.meanMinTemp = meanMinTemp;
    }

    /**
     * @return the maxTempGE90
     */
    public ClimateProductFlags getMaxTempGE90() {
        return maxTempGE90;
    }

    /**
     * @param maxTempGE90
     *            the maxTempGE90 to set
     */
    public void setMaxTempGE90(ClimateProductFlags maxTempGE90) {
        this.maxTempGE90 = maxTempGE90;
    }

    /**
     * @return the maxTempLE32
     */
    public ClimateProductFlags getMaxTempLE32() {
        return maxTempLE32;
    }

    /**
     * @param maxTempLE32
     *            the maxTempLE32 to set
     */
    public void setMaxTempLE32(ClimateProductFlags maxTempLE32) {
        this.maxTempLE32 = maxTempLE32;
    }

    /**
     * @return the maxTempGET1
     */
    public ClimateProductFlags getMaxTempGET1() {
        return maxTempGET1;
    }

    /**
     * @param maxTempGET1
     *            the maxTempGET1 to set
     */
    public void setMaxTempGET1(ClimateProductFlags maxTempGET1) {
        this.maxTempGET1 = maxTempGET1;
    }

    /**
     * @return the maxTempGET2
     */
    public ClimateProductFlags getMaxTempGET2() {
        return maxTempGET2;
    }

    /**
     * @param maxTempGET2
     *            the maxTempGET2 to set
     */
    public void setMaxTempGET2(ClimateProductFlags maxTempGET2) {
        this.maxTempGET2 = maxTempGET2;
    }

    /**
     * @return the maxTempLET3
     */
    public ClimateProductFlags getMaxTempLET3() {
        return maxTempLET3;
    }

    /**
     * @param maxTempLET3
     *            the maxTempLET3 to set
     */
    public void setMaxTempLET3(ClimateProductFlags maxTempLET3) {
        this.maxTempLET3 = maxTempLET3;
    }

    /**
     * @return the minTempLE32
     */
    public ClimateProductFlags getMinTempLE32() {
        return minTempLE32;
    }

    /**
     * @param minTempLE32
     *            the minTempLE32 to set
     */
    public void setMinTempLE32(ClimateProductFlags minTempLE32) {
        this.minTempLE32 = minTempLE32;
    }

    /**
     * @return the minTempLE0
     */
    public ClimateProductFlags getMinTempLE0() {
        return minTempLE0;
    }

    /**
     * @param minTempLE0
     *            the minTempLE0 to set
     */
    public void setMinTempLE0(ClimateProductFlags minTempLE0) {
        this.minTempLE0 = minTempLE0;
    }

    /**
     * @return the minTempGET4
     */
    public ClimateProductFlags getMinTempGET4() {
        return minTempGET4;
    }

    /**
     * @param minTempGET4
     *            the minTempGET4 to set
     */
    public void setMinTempGET4(ClimateProductFlags minTempGET4) {
        this.minTempGET4 = minTempGET4;
    }

    /**
     * @return the minTempLET5
     */
    public ClimateProductFlags getMinTempLET5() {
        return minTempLET5;
    }

    /**
     * @param minTempLET5
     *            the minTempLET5 to set
     */
    public void setMinTempLET5(ClimateProductFlags minTempLET5) {
        this.minTempLET5 = minTempLET5;
    }

    /**
     * @return the minTempLET6
     */
    public ClimateProductFlags getMinTempLET6() {
        return minTempLET6;
    }

    /**
     * @param minTempLET6
     *            the minTempLET6 to set
     */
    public void setMinTempLET6(ClimateProductFlags minTempLET6) {
        this.minTempLET6 = minTempLET6;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.maxTemp = ClimateProductFlags.getDefaultFlags();
        this.minTemp = ClimateProductFlags.getDefaultFlags();
        this.meanTemp = ClimateProductFlags.getDefaultFlags();
        this.meanMaxTemp = ClimateProductFlags.getDefaultFlags();
        this.meanMinTemp = ClimateProductFlags.getDefaultFlags();
        this.maxTempGE90 = ClimateProductFlags.getDefaultFlags();
        this.maxTempLE32 = ClimateProductFlags.getDefaultFlags();
        this.maxTempGET1 = ClimateProductFlags.getDefaultFlags();
        this.maxTempGET2 = ClimateProductFlags.getDefaultFlags();
        this.maxTempLET3 = ClimateProductFlags.getDefaultFlags();
        this.minTempLE32 = ClimateProductFlags.getDefaultFlags();
        this.minTempLE0 = ClimateProductFlags.getDefaultFlags();
        this.minTempGET4 = ClimateProductFlags.getDefaultFlags();
        this.minTempLET5 = ClimateProductFlags.getDefaultFlags();
        this.minTempLET6 = ClimateProductFlags.getDefaultFlags();
    }

    /**
     * @return a deep copy of this object.
     */
    public TemperatureControlFlags copy() {

        TemperatureControlFlags flags = new TemperatureControlFlags();

        flags.maxTemp = this.maxTemp.copy();
        flags.minTemp = this.minTemp.copy();
        flags.meanTemp = this.meanTemp.copy();
        flags.meanMaxTemp = this.meanMaxTemp.copy();
        flags.meanMinTemp = this.meanMinTemp.copy();
        flags.maxTempGE90 = this.maxTempGE90.copy();
        flags.maxTempLE32 = this.maxTempLE32.copy();
        flags.maxTempGET1 = this.maxTempGET1.copy();
        flags.maxTempGET2 = this.maxTempGET2.copy();
        flags.maxTempLET3 = this.maxTempLET3.copy();
        flags.minTempLE32 = this.minTempLE32.copy();
        flags.minTempLE0 = this.minTempLE0.copy();
        flags.minTempGET4 = this.minTempGET4.copy();
        flags.minTempLET5 = this.minTempLET5.copy();
        flags.minTempLET6 = this.minTempLET6.copy();

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static TemperatureControlFlags getDefaultFlags() {
        TemperatureControlFlags flags = new TemperatureControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
