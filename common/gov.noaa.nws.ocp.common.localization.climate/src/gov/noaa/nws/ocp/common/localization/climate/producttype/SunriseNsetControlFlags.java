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
 * Class containing flags for determining if to report astronomical data
 * (Sunrise/Sunset). Converted from "TYPE_astro.h".
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
@XmlRootElement(name = "SunriseNset")
@XmlAccessorType(XmlAccessType.NONE)
public class SunriseNsetControlFlags {

    /**
     * Flag for including sunrise in report
     */
    @DynamicSerializeElement
    @XmlElement(name = "Sunrise")
    private boolean sunrise;

    /**
     * Flag for including sunset in report
     */
    @DynamicSerializeElement
    @XmlElement(name = "Sunset")
    private boolean sunset;

    /**
     * Default constructor
     */
    public SunriseNsetControlFlags() {
    }

    /**
     * @return the sunrise
     */
    public boolean isSunrise() {
        return sunrise;
    }

    /**
     * @param sunrise
     *            the sunrise to set
     */
    public void setSunrise(boolean sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * @return the sunset
     */
    public boolean isSunset() {
        return sunset;
    }

    /**
     * @param sunset
     *            the sunset to set
     */
    public void setSunset(boolean sunset) {
        this.sunset = sunset;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.sunrise = false;
        this.sunset = false;
    }

    /**
     * @return a deep copy of this object.
     */
    public SunriseNsetControlFlags copy() {

        SunriseNsetControlFlags flags = new SunriseNsetControlFlags();

        flags.sunrise = this.sunrise;
        flags.sunset = this.sunset;

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static SunriseNsetControlFlags getDefaultFlags() {
        SunriseNsetControlFlags flags = new SunriseNsetControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
