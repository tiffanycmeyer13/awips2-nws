/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for Blowing dust/blowing sand. The old BlduBlsaConfig
 * can be removed once all CWSU sites no longer use the old configure format.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/10/2021  28802    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "BlduBlsaConfig")
public class BlduBlsaNewConfig extends AbstractCWANewConfig {
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
    public BlduBlsaNewConfig() {
    }

    public BlduBlsaNewConfig(BlduBlsaConfig oldConfig) {
        super(oldConfig);
        coverage = oldConfig.getCoverage();
        type = oldConfig.getType();
        dir = oldConfig.getDir();
        gust = oldConfig.getGust();
        vsbyFrom = oldConfig.getVsbyFrom();
        vsbyTo = oldConfig.getVsbyTo();
        condFrom = oldConfig.getCondFrom();
        condTo = oldConfig.getCondTo();
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
