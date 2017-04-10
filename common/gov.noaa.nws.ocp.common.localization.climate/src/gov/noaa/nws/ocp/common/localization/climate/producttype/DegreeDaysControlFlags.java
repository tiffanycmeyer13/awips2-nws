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
 * Class containing the degree days control flags. Converted from
 * "TYPE_deg_day_control_h.h".
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
@XmlRootElement(name = "DegreeDays")
@XmlAccessorType(XmlAccessType.NONE)
public class DegreeDaysControlFlags {

    /**
     * Flags for total heating days
     */
    @DynamicSerializeElement
    @XmlElement(name = "Heat")
    private ClimateProductFlags totalHDD;

    /**
     * Flags for heating days since 7/1
     */
    @DynamicSerializeElement
    @XmlElement(name = "HeatJuly1")
    private ClimateProductFlags seasonHDD;

    /**
     * Flags for total cooling days
     */
    @DynamicSerializeElement
    @XmlElement(name = "Cool")
    private ClimateProductFlags totalCDD;

    /**
     * Flags for cooling days since 1/1
     */
    @DynamicSerializeElement
    @XmlElement(name = "CoolJuly1")
    private ClimateProductFlags seasonCDD;

    /**
     * Flags for first freeze
     */
    @DynamicSerializeElement
    @XmlElement(name = "EarlyFreeze")
    private ClimateProductFlags earlyFreeze;

    /**
     * Flags for last freeze
     */
    @DynamicSerializeElement
    @XmlElement(name = "LateFreeze")
    private ClimateProductFlags lateFreeze;

    /**
     * Default constructor
     */
    public DegreeDaysControlFlags() {
    }

    /**
     * @return the totalHDD
     */
    public ClimateProductFlags getTotalHDD() {
        return totalHDD;
    }

    /**
     * @param totalHDD
     *            the totalHDD to set
     */
    public void setTotalHDD(ClimateProductFlags totalHDD) {
        this.totalHDD = totalHDD;
    }

    /**
     * @return the seasonHDD
     */
    public ClimateProductFlags getSeasonHDD() {
        return seasonHDD;
    }

    /**
     * @param seasonHDD
     *            the seasonHDD to set
     */
    public void setSeasonHDD(ClimateProductFlags seasonHDD) {
        this.seasonHDD = seasonHDD;
    }

    /**
     * @return the totalCDD
     */
    public ClimateProductFlags getTotalCDD() {
        return totalCDD;
    }

    /**
     * @param totalCDD
     *            the totalCDD to set
     */
    public void setTotalCDD(ClimateProductFlags totalCDD) {
        this.totalCDD = totalCDD;
    }

    /**
     * @return the seasonCDD
     */
    public ClimateProductFlags getSeasonCDD() {
        return seasonCDD;
    }

    /**
     * @param seasonCDD
     *            the seasonCDD to set
     */
    public void setSeasonCDD(ClimateProductFlags seasonCDD) {
        this.seasonCDD = seasonCDD;
    }

    /**
     * @return the earlyFreeze
     */
    public ClimateProductFlags getEarlyFreeze() {
        return earlyFreeze;
    }

    /**
     * @param earlyFreeze
     *            the earlyFreeze to set
     */
    public void setEarlyFreeze(ClimateProductFlags earlyFreeze) {
        this.earlyFreeze = earlyFreeze;
    }

    /**
     * @return the lateFreeze
     */
    public ClimateProductFlags getLateFreeze() {
        return lateFreeze;
    }

    /**
     * @param lateFreeze
     *            the lateFreeze to set
     */
    public void setLateFreeze(ClimateProductFlags lateFreeze) {
        this.lateFreeze = lateFreeze;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.totalHDD = ClimateProductFlags.getDefaultFlags();
        this.seasonHDD = ClimateProductFlags.getDefaultFlags();
        this.totalCDD = ClimateProductFlags.getDefaultFlags();
        this.seasonCDD = ClimateProductFlags.getDefaultFlags();
        this.earlyFreeze = ClimateProductFlags.getDefaultFlags();
        this.lateFreeze = ClimateProductFlags.getDefaultFlags();
    }

    /**
     * @return a deep copy of this object.
     */
    public DegreeDaysControlFlags copy() {

        DegreeDaysControlFlags flags = new DegreeDaysControlFlags();

        flags.totalHDD = this.totalHDD.copy();
        flags.seasonHDD = this.seasonHDD.copy();
        flags.totalCDD = this.totalCDD.copy();
        flags.seasonCDD = this.seasonCDD.copy();
        flags.earlyFreeze = this.earlyFreeze.copy();
        flags.lateFreeze = this.lateFreeze.copy();

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static DegreeDaysControlFlags getDefaultFlags() {
        DegreeDaysControlFlags flags = new DegreeDaysControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
