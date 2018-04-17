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
 * Class containing the wind control flags. Converted from
 * "TYPE_wind_control.h".
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
@XmlRootElement(name = "Wind")
@XmlAccessorType(XmlAccessType.NONE)
public class WindControlFlags {

    /**
     * Flags for resultant wind
     */
    @DynamicSerializeElement
    @XmlElement(name = "ResultWind")
    private ClimateProductFlags resultWind;

    /**
     * Flags for maximum sustained wind
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxWind")
    private ClimateProductFlags maxWind;

    /**
     * Flags for maximum wind gust
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxGust")
    private ClimateProductFlags maxGust;

    /**
     * Flags for mean wind
     */
    @DynamicSerializeElement
    @XmlElement(name = "MeanWind")
    private ClimateProductFlags meanWind;

    /**
     * Default constructor
     */
    public WindControlFlags() {
    }

    /**
     * @return the resultWind
     */
    public ClimateProductFlags getResultWind() {
        return resultWind;
    }

    /**
     * @param resultWind
     *            the resultWind to set
     */
    public void setResultWind(ClimateProductFlags resultWind) {
        this.resultWind = resultWind;
    }

    /**
     * @return the maxWind
     */
    public ClimateProductFlags getMaxWind() {
        return maxWind;
    }

    /**
     * @param maxWind
     *            the maxWind to set
     */
    public void setMaxWind(ClimateProductFlags maxWind) {
        this.maxWind = maxWind;
    }

    /**
     * @return the maxGust
     */
    public ClimateProductFlags getMaxGust() {
        return maxGust;
    }

    /**
     * @param maxGust
     *            the maxGust to set
     */
    public void setMaxGust(ClimateProductFlags maxGust) {
        this.maxGust = maxGust;
    }

    /**
     * @return the meanWind
     */
    public ClimateProductFlags getMeanWind() {
        return meanWind;
    }

    /**
     * @param meanWind
     *            the meanWind to set
     */
    public void setMeanWind(ClimateProductFlags meanWind) {
        this.meanWind = meanWind;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.resultWind = ClimateProductFlags.getDefaultFlags();
        this.maxWind = ClimateProductFlags.getDefaultFlags();
        this.maxGust = ClimateProductFlags.getDefaultFlags();
        this.meanWind = ClimateProductFlags.getDefaultFlags();
    }

    /**
     * @return a deep copy of this object.
     */
    public WindControlFlags copy() {

        WindControlFlags flags = new WindControlFlags();

        flags.resultWind = this.resultWind.copy();
        flags.maxWind = this.maxWind.copy();
        flags.maxGust = this.maxGust.copy();
        flags.meanWind = this.meanWind.copy();

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static WindControlFlags getDefaultFlags() {
        WindControlFlags flags = new WindControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
