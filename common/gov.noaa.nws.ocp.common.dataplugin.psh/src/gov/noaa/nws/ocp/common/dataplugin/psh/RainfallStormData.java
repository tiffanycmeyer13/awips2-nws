/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Contains a list of PSH storm rainfall data entries.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 12, 2017 #39468     jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@XmlRootElement(name = "RainfallStormData")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class RainfallStormData extends StormData {

    @DynamicSerializeElement
    @XmlElements(@XmlElement(name = "RainfallData", type = RainfallDataEntry.class))
    private List<RainfallDataEntry> data;

    @DynamicSerializeElement
    @XmlElement
    private String startMon;

    @DynamicSerializeElement
    @XmlElement
    private String endMon;

    @DynamicSerializeElement
    @XmlElement
    private String startDay;

    @DynamicSerializeElement
    @XmlElement
    private String endDay;

    @DynamicSerializeElement
    @XmlElement
    private String startHour;

    @DynamicSerializeElement
    @XmlElement
    private String endHour;

    public RainfallStormData() {
        super();
    }

    /**
     * @return the data
     */
    public List<RainfallDataEntry> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(List<RainfallDataEntry> data) {
        this.data = data;
    }

    /**
     * @return the startMon
     */
    public String getStartMon() {
        return startMon;
    }

    /**
     * @param startMon
     *            the startMon to set
     */
    public void setStartMon(String startMon) {
        this.startMon = startMon;
    }

    /**
     * @return the endMon
     */
    public String getEndMon() {
        return endMon;
    }

    /**
     * @param endMon
     *            the endMon to set
     */
    public void setEndMon(String endMon) {
        this.endMon = endMon;
    }

    /**
     * @return the startDay
     */
    public String getStartDay() {
        return startDay;
    }

    /**
     * @param startDay
     *            the startDay to set
     */
    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    /**
     * @return the endDay
     */
    public String getEndDay() {
        return endDay;
    }

    /**
     * @param endDay
     *            the endDay to set
     */
    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    /**
     * @return the startHour
     */
    public String getStartHour() {
        return startHour;
    }

    /**
     * @param startHour
     *            the startHour to set
     */
    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    /**
     * @return the endHour
     */
    public String getEndHour() {
        return endHour;
    }

    /**
     * @param endHour
     *            the endHour to set
     */
    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

}
