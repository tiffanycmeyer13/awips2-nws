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
 * Class containing the relative humidity control flags.
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
@XmlRootElement(name = "RelativeHumidity")
@XmlAccessorType(XmlAccessType.NONE)
public class RelativeHumidityControlFlags {

    /**
     * Flags for max RH
     */
    @DynamicSerializeElement
    @XmlElement(name = "maxRH")
    private ClimateProductFlags maxRH;

    /**
     * Flags for min RH
     */
    @DynamicSerializeElement
    @XmlElement(name = "minRH")
    private ClimateProductFlags minRH;

    /**
     * Flags for mean RH
     */
    @DynamicSerializeElement
    @XmlElement(name = "meanRH")
    private ClimateProductFlags meanRH;

    /**
     * Flags for average RH
     */
    @DynamicSerializeElement
    @XmlElement(name = "averageRH")
    private ClimateProductFlags averageRH;

    /**
     * Default constructor
     */
    public RelativeHumidityControlFlags() {
    }

    /**
     * @return the maxRH
     */
    public ClimateProductFlags getMaxRH() {
        return maxRH;
    }

    /**
     * @param maxRH
     *            the maxRH to set
     */
    public void setMaxRH(ClimateProductFlags maxRH) {
        this.maxRH = maxRH;
    }

    /**
     * @return the minRH
     */
    public ClimateProductFlags getMinRH() {
        return minRH;
    }

    /**
     * @param minRH
     *            the minRH to set
     */
    public void setMinRH(ClimateProductFlags minRH) {
        this.minRH = minRH;
    }

    /**
     * @return the meanRH
     */
    public ClimateProductFlags getMeanRH() {
        return meanRH;
    }

    /**
     * @param meanRH
     *            the meanRH to set
     */
    public void setMeanRH(ClimateProductFlags meanRH) {
        this.meanRH = meanRH;
    }

    /**
     * @return the averageRH
     */
    public ClimateProductFlags getAverageRH() {
        return averageRH;
    }

    /**
     * @param averageRH
     *            the averageRH to set
     */
    public void setAverageRH(ClimateProductFlags averageRH) {
        this.averageRH = averageRH;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.maxRH = ClimateProductFlags.getDefaultFlags();
        this.minRH = ClimateProductFlags.getDefaultFlags();
        this.meanRH = ClimateProductFlags.getDefaultFlags();
        this.averageRH = ClimateProductFlags.getDefaultFlags();
    }

    /**
     * @return a deep copy of this object.
     */
    public RelativeHumidityControlFlags copy() {

        RelativeHumidityControlFlags flags = new RelativeHumidityControlFlags();

        flags.maxRH = this.maxRH.copy();
        flags.minRH = this.minRH.copy();
        flags.meanRH = this.meanRH.copy();
        flags.averageRH = this.averageRH.copy();

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static RelativeHumidityControlFlags getDefaultFlags() {
        RelativeHumidityControlFlags flags = new RelativeHumidityControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
