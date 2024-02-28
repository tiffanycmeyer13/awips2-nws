/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * configuration for Blowing dust/blowing sand
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 04/14/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "BlduBlsaConfig")
public class BlduBlsaConfig extends AbstractCWAConfig {
    /** Coverage */
    @XmlElement(name = "coverage")
    private String coverage;

    /** type */
    @XmlElement(name = "type")
    private String type;

    /** wind direction */
    @XmlElement(name = "dir")
    private String dir;

    /** wind gust */
    @XmlElement(name = "gust")
    private String gust;

    /** visibility from */
    @XmlElement(name = "vsbyFrom")
    private String vsbyFrom;

    /** visibility to */
    @XmlElement(name = "vsbyTo")
    private String vsbyTo;

    /** condition from */
    @XmlElement(name = "condFrom")
    private String condFrom;

    /** condition to */
    @XmlElement(name = "condTo")
    private String condTo;

    /**
     * Constructor
     * 
     */
    public BlduBlsaConfig() {
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getGust() {
        return gust;
    }

    public void setGust(String gust) {
        this.gust = gust;
    }

    public String getVsbyFrom() {
        return vsbyFrom;
    }

    public void setVsbyFrom(String vsbyFrom) {
        this.vsbyFrom = vsbyFrom;
    }

    public String getVsbyTo() {
        return vsbyTo;
    }

    public void setVsbyTo(String vsbyTo) {
        this.vsbyTo = vsbyTo;
    }

    public String getCondFrom() {
        return condFrom;
    }

    public void setCondFrom(String condFrom) {
        this.condFrom = condFrom;
    }

    public String getCondTo() {
        return condTo;
    }

    public void setCondTo(String condTo) {
        this.condTo = condTo;
    }

}
