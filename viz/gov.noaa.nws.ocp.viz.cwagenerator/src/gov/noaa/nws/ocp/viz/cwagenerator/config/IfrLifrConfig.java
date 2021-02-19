/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the IFR/LIFR configuration.
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
public class IfrLifrConfig extends AbstractCWAConfig {
    /** developing check */
    @XmlElement(name = "dvlpgChk")
    private boolean dvlpgChk;

    /** coverage */
    @XmlElement(name = "coverage")
    private String coverage;

    /** flight */
    @XmlElement(name = "flight")
    private String flight;

    /** CIG from */
    @XmlElement(name = "cigFrom")
    private String cigFrom;

    /** COG to */
    @XmlElement(name = "cigTo")
    private String cigTo;

    /** visibility from */
    @XmlElement(name = "vsbyFrom")
    private String vsbyFrom;

    /** visibility to */
    @XmlElement(name = "vsbyTo")
    private String vsbyTo;

    /** BR check */
    @XmlElement(name = "brChk")
    private boolean brChk;

    /** FG check */
    @XmlElement(name = "fgChk")
    private boolean fgChk;

    /** HZ check */
    @XmlElement(name = "hzChk")
    private boolean hzChk;

    /** DZ check */
    @XmlElement(name = "dzChk")
    private boolean dzChk;

    /** RA check */
    @XmlElement(name = "raChk")
    private boolean raChk;

    /** SN check */
    @XmlElement(name = "snChk")
    private boolean snChk;

    /** FU check button */
    @XmlElement(name = "fuChk")
    private boolean fuChk;

    /** DU check */
    @XmlElement(name = "duChk")
    private boolean duChk;

    /** SS check */
    @XmlElement(name = "ssChk")
    private boolean ssChk;

    /** no condition */
    @XmlElement(name = "noCond")
    private boolean noCond;

    /** conditions continue beyond */
    @XmlElement(name = "contg")
    private boolean contg;

    /** conditions improve by */
    @XmlElement(name = "impr")
    private boolean impr;

    /** aircraft */
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
    public IfrLifrConfig() {

    }

    public boolean isDvlpgChk() {
        return dvlpgChk;
    }

    public void setDvlpgChk(boolean dvlpgChk) {
        this.dvlpgChk = dvlpgChk;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public String getCigFrom() {
        return cigFrom;
    }

    public void setCigFrom(String cigFrom) {
        this.cigFrom = cigFrom;
    }

    public String getCigTo() {
        return cigTo;
    }

    public void setCigTo(String cigTo) {
        this.cigTo = cigTo;
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

    public boolean isBrChk() {
        return brChk;
    }

    public void setBrChk(boolean brChk) {
        this.brChk = brChk;
    }

    public boolean isFgChk() {
        return fgChk;
    }

    public void setFgChk(boolean fgChk) {
        this.fgChk = fgChk;
    }

    public boolean isHzChk() {
        return hzChk;
    }

    public void setHzChk(boolean hzChk) {
        this.hzChk = hzChk;
    }

    public boolean isDzChk() {
        return dzChk;
    }

    public void setDzChk(boolean dzChk) {
        this.dzChk = dzChk;
    }

    public boolean isRaChk() {
        return raChk;
    }

    public void setRaChk(boolean raChk) {
        this.raChk = raChk;
    }

    public boolean isSnChk() {
        return snChk;
    }

    public void setSnChk(boolean snChk) {
        this.snChk = snChk;
    }

    public boolean isFuChk() {
        return fuChk;
    }

    public void setFuChk(boolean fuChk) {
        this.fuChk = fuChk;
    }

    public boolean isDuChk() {
        return duChk;
    }

    public void setDuChk(boolean duChk) {
        this.duChk = duChk;
    }

    public boolean isSsChk() {
        return ssChk;
    }

    public void setSsChk(boolean ssChk) {
        this.ssChk = ssChk;
    }

    public boolean isNoCond() {
        return noCond;
    }

    public void setNoCond(boolean noCond) {
        this.noCond = noCond;
    }

    public boolean isContg() {
        return contg;
    }

    public void setContg(boolean contg) {
        this.contg = contg;
    }

    public boolean isImpr() {
        return impr;
    }

    public void setImpr(boolean impr) {
        this.impr = impr;
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
