/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the turbulence/LLWS configuration. The old TurbLlwsConfig
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
public class TurbLlwsNewConfig extends AbstractCWANewConfig {
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
    public TurbLlwsNewConfig() {

    }

    public TurbLlwsNewConfig(TurbLlwsConfig oldConfig) {
        super(oldConfig);
        freq = oldConfig.getFreq();
        intsty = oldConfig.getIntsty();
        flightFrom = oldConfig.getFlightFrom();
        flightTo = oldConfig.getFlightTo();
        llwsChk = oldConfig.isLlwsChk();
        noCond = oldConfig.isNoCond();
        contg = oldConfig.isContg();
        impr = oldConfig.isImpr();
        aircraftChk = oldConfig.isAircraftChk();
        addnlInfo = oldConfig.getAddnlInfo();
        noUpdateChk = oldConfig.isNoUpdateChk();
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
