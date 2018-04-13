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
 * Class containing the sky cover flags. It was part of "TYPE_sky_control.h".
 *
 * <pre>
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
@XmlRootElement(name = "SkyCover")
@XmlAccessorType(XmlAccessType.NONE)
public class SkycoverControlFlags {

    /**
     * Flag for possible sunshine
     */
    @DynamicSerializeElement
    @XmlElement(name = "PossibleSunshine")
    private boolean possSunshine;

    /**
     * Flag for number of days with fair sky (0-0.3)
     */
    @DynamicSerializeElement
    @XmlElement(name = "FairDays")
    private boolean fairDays;

    /**
     * Flag for number of days with partly. cloudy sky (0.4-0.7)
     */
    @DynamicSerializeElement
    @XmlElement(name = "PartlyCloudyDays")
    private boolean partlyCloudyDays;

    /**
     * Flag for number of days with cloudy sky (0.8-1.0)
     */
    @DynamicSerializeElement
    @XmlElement(name = "CouldyDays")
    private boolean cloudyDays;

    /**
     * Flag for average sky cover
     */
    @DynamicSerializeElement
    @XmlElement(name = "AvgSkycover")
    private boolean avgSkycover;

    /**
     * Default constructor
     */
    public SkycoverControlFlags() {
    }

    /**
     * @return the possSunshine
     */
    public boolean isPossSunshine() {
        return possSunshine;
    }

    /**
     * @param possSunshine
     *            the possSunshine to set
     */
    public void setPossSunshine(boolean possSunshine) {
        this.possSunshine = possSunshine;
    }

    /**
     * @return the fairDays
     */
    public boolean isFairDays() {
        return fairDays;
    }

    /**
     * @param fairDays
     *            the fairDays to set
     */
    public void setFairDays(boolean fairDays) {
        this.fairDays = fairDays;
    }

    /**
     * @return the partlyCloudyDays
     */
    public boolean isPartlyCloudyDays() {
        return partlyCloudyDays;
    }

    /**
     * @param partlyCloudyDays
     *            the partlyCloudyDays to set
     */
    public void setPartlyCloudyDays(boolean partlyCloudyDays) {
        this.partlyCloudyDays = partlyCloudyDays;
    }

    /**
     * @return the cloudyDays
     */
    public boolean isCloudyDays() {
        return cloudyDays;
    }

    /**
     * @param cloudyDays
     *            the cloudyDays to set
     */
    public void setCloudyDays(boolean cloudyDays) {
        this.cloudyDays = cloudyDays;
    }

    /**
     * @return the avgSkycover
     */
    public boolean isAvgSkycover() {
        return avgSkycover;
    }

    /**
     * @param avgSkycover
     *            the avgSkycover to set
     */
    public void setAvgSkycover(boolean avgSkycover) {
        this.avgSkycover = avgSkycover;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.possSunshine = false;
        this.fairDays = false;
        this.partlyCloudyDays = false;
        this.cloudyDays = false;
        this.avgSkycover = false;
    }

    /**
     * @return a deep copy of this object.
     */
    public SkycoverControlFlags copy() {

        SkycoverControlFlags flags = new SkycoverControlFlags();

        flags.possSunshine = this.possSunshine;
        flags.fairDays = this.fairDays;
        flags.partlyCloudyDays = this.partlyCloudyDays;
        flags.cloudyDays = this.cloudyDays;
        flags.avgSkycover = this.avgSkycover;

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static SkycoverControlFlags getDefaultFlags() {
        SkycoverControlFlags flags = new SkycoverControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
