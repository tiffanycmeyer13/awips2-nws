/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.atcf.configuration;

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
 * List of forecaster initials.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 04 2020  82576      jwu         Created
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ForecasterInitials")
@XmlAccessorType(XmlAccessType.NONE)
public class ForecasterInitials {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Initials
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Initial", type = String.class) })
    private List<String> initials;

    /**
     * Constructor
     */
    public ForecasterInitials() {
        initials = new ArrayList<>();
    }

    /**
     * Constructor
     * 
     * @param list
     *            List of forecaster initials
     */
    public ForecasterInitials(List<String> list) {
        this.initials = list;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the initials
     */
    public List<String> getInitials() {
        return initials;
    }

    /**
     * @param initials
     *            the initials to set
     */
    public void setInitials(List<String> initials) {
        this.initials = initials;
    }

}
