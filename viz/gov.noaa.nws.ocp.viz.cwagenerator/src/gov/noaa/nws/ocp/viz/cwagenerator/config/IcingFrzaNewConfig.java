/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the Icing/Freezing configuration. The old IcingFrzaConfig
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
public class IcingFrzaNewConfig extends AbstractCWANewConfig {
    /** frequency */
    @XmlElement(name = "freq")
    private String freq;

    /** intensity */
    @XmlElement(name = "intsty")
    private String intsty;

    /** type */
    @XmlElement(name = "type")
    private String type;

    /** flight from */
    @XmlElement(name = "flightFrom")
    private String flightFrom;

    /** flight to */
    @XmlElement(name = "flightTo")
    private String flightTo;

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
    @XmlElement(name = "aircraft")
    private boolean aircraft;

    /** additional information */
    @XmlElement(name = "addnlInfo")
    private String addnlInfo;

    /** no update check */
    @XmlElement(name = "noUpdate")
    private boolean noUpdate;

    /**
     * Constructor
     */
    public IcingFrzaNewConfig() {

    }

    public IcingFrzaNewConfig(IcingFrzaConfig oldConfig) {
        super(oldConfig);
        freq = oldConfig.getFreq();
        intsty = oldConfig.getIntsty();
        type = oldConfig.getType();
        flightFrom = oldConfig.getFlightFrom();
        flightTo = oldConfig.getFlightTo();
        noCond = oldConfig.isNoCond();
        contg = oldConfig.isContg();
        impr = oldConfig.isImpr();
        aircraft = oldConfig.isAircraft();
        addnlInfo = oldConfig.getAddnlInfo();
        noUpdate = oldConfig.isNoUpdate();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isAircraft() {
        return aircraft;
    }

    public void setAircraft(boolean aircraft) {
        this.aircraft = aircraft;
    }

    public String getAddnlInfo() {
        return addnlInfo;
    }

    public void setAddnlInfo(String addnlInfo) {
        this.addnlInfo = addnlInfo;
    }

    public boolean isNoUpdate() {
        return noUpdate;
    }

    public void setNoUpdate(boolean noUpdate) {
        this.noUpdate = noUpdate;
    }

}
