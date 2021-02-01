/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the volcano configuration.
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
public class VolcanoConfig extends AbstractCWAConfig {
    /** volcano stations */
    @XmlElement(name = "volcano")
    private String volcano;

    /** eruption status */
    @XmlElement(name = "eruptStatus")
    private EruptStatus eruptStatus;

    /** ash */
    @XmlElement(name = "ash")
    private String ash;

    /** tops */
    @XmlElement(name = "tops")
    private String tops;

    /** estimate check */
    @XmlElement(name = "estChk")
    private boolean estChk;

    /** plume direction */
    @XmlElement(name = "plumeDir")
    private String plumeDir;

    /** plume speed */
    @XmlElement(name = "plumeSpd")
    private String plumeSpd;

    /** locations that ash could reach */
    @XmlElement(name = "reach")
    private String reach;

    /** within */
    @XmlElement(name = "within")
    private String within;

    /**
     * constructor
     */
    public VolcanoConfig() {

    }

    public String getVolcano() {
        return volcano;
    }

    public void setVolcano(String volcano) {
        this.volcano = volcano;
    }

    public EruptStatus getEruptStatus() {
        return eruptStatus;
    }

    public void setEruptStatus(EruptStatus eruptStatus) {
        this.eruptStatus = eruptStatus;
    }

    public String getAsh() {
        return ash;
    }

    public void setAsh(String ash) {
        this.ash = ash;
    }

    public String getTops() {
        return tops;
    }

    public void setTops(String tops) {
        this.tops = tops;
    }

    public boolean isEstChk() {
        return estChk;
    }

    public void setEstChk(boolean estChk) {
        this.estChk = estChk;
    }

    public String getPlumeDir() {
        return plumeDir;
    }

    public void setPlumeDir(String plumeDir) {
        this.plumeDir = plumeDir;
    }

    public String getPlumeSpd() {
        return plumeSpd;
    }

    public void setPlumeSpd(String plumeSpd) {
        this.plumeSpd = plumeSpd;
    }

    public String getReach() {
        return reach;
    }

    public void setReach(String reach) {
        this.reach = reach;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

}
