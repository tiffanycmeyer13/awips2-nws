/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the cancel/manual configuration.
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
public class CancelManualConfig extends AbstractCWAConfig {
    /** cancel check */
    @XmlElement(name = "cancelChk")
    private boolean cancelChk;

    /** see check */
    @XmlElement(name = "seeChk")
    private boolean seeChk;

    /** additional information */
    @XmlElement(name = "addnlInfo")
    private String addnlInfo;

    /** no update check */
    @XmlElement(name = "noUpdateChk")
    private boolean noUpdateChk;

    /**
     * constructor
     */
    public CancelManualConfig() {

    }

    public boolean isCancelChk() {
        return cancelChk;
    }

    public void setCancelChk(boolean cancelChk) {
        this.cancelChk = cancelChk;
    }

    public boolean isSeeChk() {
        return seeChk;
    }

    public void setSeeChk(boolean seeChk) {
        this.seeChk = seeChk;
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
