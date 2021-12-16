/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * class for the cancel/manual configuration. The old CancelManualConfig
 * can be removed once all CWSU sites no longer use the old configure format.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/10/2021  22802    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CancelManualNewConfig extends AbstractCWANewConfig {
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
    public CancelManualNewConfig() {

    }

    public CancelManualNewConfig(CancelManualConfig oldConfig) {
        super(oldConfig);
        cancelChk = oldConfig.isCancelChk();
        seeChk = oldConfig.isSeeChk();
        addnlInfo = oldConfig.getAddnlInfo();
        noUpdateChk = oldConfig.isNoUpdateChk();
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
