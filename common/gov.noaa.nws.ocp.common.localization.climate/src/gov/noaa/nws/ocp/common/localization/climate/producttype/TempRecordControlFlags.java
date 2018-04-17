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
 * Class containing flags for determining if to report max/min temperature
 * record.
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
@XmlRootElement(name = "TemperatureRecord")
@XmlAccessorType(XmlAccessType.NONE)
public class TempRecordControlFlags {

    /**
     * Flag for including max. temp normal
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempNorm")
    private boolean maxTempNorm;

    /**
     * Flag for including max. temp record
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempRecord")
    private boolean maxTempRecord;

    /**
     * Flag for including year of max. temp record
     */
    @DynamicSerializeElement
    @XmlElement(name = "MaxTempYear")
    private boolean maxTempYear;

    /**
     * Flag for including min. temp normal
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempNorm")
    private boolean minTempNorm;

    /**
     * Flag for including min. temp record
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempRecord")
    private boolean minTempRecord;

    /**
     * Flag for including year of min. temp record
     */
    @DynamicSerializeElement
    @XmlElement(name = "MinTempYear")
    private boolean minTempYear;

    /**
     * @return the maxTempNorm
     */
    public boolean isMaxTempNorm() {
        return maxTempNorm;
    }

    /**
     * @param maxTempNorm
     *            the maxTempNorm to set
     */
    public void setMaxTempNorm(boolean maxTempNorm) {
        this.maxTempNorm = maxTempNorm;
    }

    /**
     * @return the maxTempRecord
     */
    public boolean isMaxTempRecord() {
        return maxTempRecord;
    }

    /**
     * @param maxTempRecord
     *            the maxTempRecord to set
     */
    public void setMaxTempRecord(boolean maxTempRecord) {
        this.maxTempRecord = maxTempRecord;
    }

    /**
     * @return the maxTempYear
     */
    public boolean isMaxTempYear() {
        return maxTempYear;
    }

    /**
     * @param maxTempYear
     *            the maxTempYear to set
     */
    public void setMaxTempYear(boolean maxTempYear) {
        this.maxTempYear = maxTempYear;
    }

    /**
     * @return the minTempNorm
     */
    public boolean isMinTempNorm() {
        return minTempNorm;
    }

    /**
     * @param minTempNorm
     *            the minTempNorm to set
     */
    public void setMinTempNorm(boolean minTempNorm) {
        this.minTempNorm = minTempNorm;
    }

    /**
     * @return the minTempRecord
     */
    public boolean isMinTempRecord() {
        return minTempRecord;
    }

    /**
     * @param minTempRecord
     *            the minTempRecord to set
     */
    public void setMinTempRecord(boolean minTempRecord) {
        this.minTempRecord = minTempRecord;
    }

    /**
     * @return the minTempYear
     */
    public boolean isMinTempYear() {
        return minTempYear;
    }

    /**
     * @param minTempYear
     *            the minTempYear to set
     */
    public void setMinTempYear(boolean minTempYear) {
        this.minTempYear = minTempYear;
    }

    /**
     * Default constructor
     */
    public TempRecordControlFlags() {
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.maxTempNorm = false;
        this.maxTempRecord = false;
        this.maxTempYear = false;
        this.minTempNorm = false;
        this.minTempRecord = false;
        this.minTempYear = false;
    }

    /**
     * @return a deep copy of this object.
     */
    public TempRecordControlFlags copy() {

        TempRecordControlFlags flags = new TempRecordControlFlags();
        flags.maxTempNorm = this.maxTempNorm;
        flags.maxTempRecord = this.maxTempRecord;
        flags.maxTempYear = this.maxTempYear;
        flags.minTempNorm = this.minTempNorm;
        flags.minTempRecord = this.minTempRecord;
        flags.minTempYear = this.minTempYear;

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static TempRecordControlFlags getDefaultFlags() {
        TempRecordControlFlags flags = new TempRecordControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
