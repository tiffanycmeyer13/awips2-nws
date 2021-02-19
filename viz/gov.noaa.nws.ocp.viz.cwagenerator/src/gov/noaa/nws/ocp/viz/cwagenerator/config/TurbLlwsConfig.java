/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the turbulence/LLWS configuration.
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
public class TurbLlwsConfig extends AbstractCWAConfig {
    /** frequency */
    @XmlElement(name = "freq")
    private String freq;

    /** intensity */
    @XmlElement(name = "intsty")
    private String intsty;

    /** flight from */
    @XmlElement(name = "flightFrom")
    private String flightFrom;

    /** flight to */
    @XmlElement(name = "flightTo")
    private String flightTo;

    /** Low Level Wind Shear */
    @XmlElement(name = "llwsChk")
    private boolean llwsChk;

    /** no condition */
    @XmlElement(name = "noCond")
    private boolean noCond;

    /** conditions continue beyond */
    @XmlElement(name = "contg")
    private boolean contg;

    /** conditions improve by */
    @XmlElement(name = "impr")
    private boolean impr;

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
     * 
     * @param parent
     */
    public TurbLlwsConfig() {

    }

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getIntsty() {
        return intsty;
    }

    public void setIntsty(String intsty) {
        this.intsty = intsty;
    }

    public String getFlightFrom() {
        return flightFrom;
    }

    public void setFlightFrom(String flightFrom) {
        this.flightFrom = flightFrom;
    }

    public String getFlightTo() {
        return flightTo;
    }

    public void setFlightTo(String flightTo) {
        this.flightTo = flightTo;
    }

    public boolean isLlwsChk() {
        return llwsChk;
    }

    public void setLlwsChk(boolean llwsChk) {
        this.llwsChk = llwsChk;
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
