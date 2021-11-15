/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * class for the thunderstorm configuration. The old ThunderstormConfig
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
@XmlRootElement(name = "ThunderstormConfig")
public class ThunderstormNewConfig extends AbstractCWANewConfig {
    /** developing check */
    @XmlElement(name = "dvlpgChk")
    private boolean dvlpgChk;

    /** embedded check */
    @XmlElement(name = "embdChk")
    private boolean embdChk;

    /** type */
    @XmlElement(name = "type")
    private String type;

    /** intensity */
    @XmlElement(name = "intst")
    private String intst;

    /** wind direction */
    @XmlElement(name = "dir")
    private String dir;

    /** wind speed */
    @XmlElement(name = "spd")
    private String spd;

    /** Tops from */
    @XmlElement(name = "topsFrom")
    private String topsFrom;

    /** Tops to */
    @XmlElement(name = "topsTo")
    private String topsTo;

    /** estimated check */
    @XmlElement(name = "estChk")
    private boolean estChk;

    /** tornado check */
    @XmlElement(name = "tornadoChk")
    private boolean tornadoChk;

    /** hail check */
    @XmlElement(name = "hailChk")
    private boolean hailChk;

    /** gust check */
    @XmlElement(name = "gustChk")
    private boolean gustChk;

    /** no condition */
    @XmlElement(name = "cond")
    private CondType cond;

    /** aircraft check */
    @XmlElement(name = "aircraftChk")
    private boolean aircraftChk;

    /** additional information */
    @XmlElement(name = "addnlInfo")
    private String addnlInfo;

    /** no update check */
    @XmlElement(name = "noUpdateChk")
    private boolean noUpdateChk;

    /**
     * constructor
     */
    public ThunderstormNewConfig() {

    }

    public ThunderstormNewConfig(ThunderstormConfig oldConfig) {
        super(oldConfig);
        dvlpgChk = oldConfig.isDvlpgChk();
        embdChk = oldConfig.isEmbdChk();
        type = oldConfig.getType();
        intst = oldConfig.getIntst();
        dir = oldConfig.getDir();
        spd = oldConfig.getSpd();
        topsFrom = oldConfig.getTopsFrom();
        topsTo = oldConfig.getTopsTo();
        estChk = oldConfig.isEstChk();
        tornadoChk = oldConfig.isTornadoChk();
        hailChk = oldConfig.isHailChk();
        gustChk = oldConfig.isGustChk();
        cond = oldConfig.getCond();
        aircraftChk = oldConfig.isAircraftChk();
        addnlInfo = oldConfig.getAddnlInfo();
        noUpdateChk = oldConfig.isNoUpdateChk();
    }

    public boolean isDvlpgChk() {
        return dvlpgChk;
    }

    public void setDvlpgChk(boolean dvlpgChk) {
        this.dvlpgChk = dvlpgChk;
    }

    public boolean isEmbdChk() {
        return embdChk;
    }

    public void setEmbdChk(boolean embdChk) {
        this.embdChk = embdChk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIntst() {
        return intst;
    }

    public void setIntst(String intst) {
        this.intst = intst;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getSpd() {
        return spd;
    }

    public void setSpd(String spd) {
        this.spd = spd;
    }

    public String getTopsFrom() {
        return topsFrom;
    }

    public void setTopsFrom(String topsFrom) {
        this.topsFrom = topsFrom;
    }

    public String getTopsTo() {
        return topsTo;
    }

    public void setTopsTo(String topsTo) {
        this.topsTo = topsTo;
    }

    public boolean isEstChk() {
        return estChk;
    }

    public void setEstChk(boolean estChk) {
        this.estChk = estChk;
    }

    public boolean isTornadoChk() {
        return tornadoChk;
    }

    public void setTornadoChk(boolean tornadoChk) {
        this.tornadoChk = tornadoChk;
    }

    public boolean isHailChk() {
        return hailChk;
    }

    public void setHailChk(boolean hailChk) {
        this.hailChk = hailChk;
    }

    public boolean isGustChk() {
        return gustChk;
    }

    public void setGustChk(boolean gustChk) {
        this.gustChk = gustChk;
    }

    public CondType getCond() {
        return cond;
    }

    public void setCond(CondType cond) {
        this.cond = cond;
    }

    public boolean isAircraftChk() {
        return aircraftChk;
    }

    public void setAircraftChk(boolean aircraftChk) {
        this.aircraftChk = aircraftChk;
    }

    public String getAddnlInfo() {
        return addnlInfo;
    }

    public void setAddnlInfo(String addnlInfo) {
        this.addnlInfo = addnlInfo;
    }

    public boolean isNoUpdateChk() {
        return noUpdateChk;
    }

    public void setNoUpdateChk(boolean noUpdateChk) {
        this.noUpdateChk = noUpdateChk;
    }

}
