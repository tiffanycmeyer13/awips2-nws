/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.io.File;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Define default properties
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Aug 13,2020  76541      mporricelli  Initial creation
 * Aug 28, 2020 81449      jwu          Add directories for fix data.
 * Oct 05, 2020 81818      wpaintsil    Add wfo id.
 * Oct 21, 2020 82721      jwu          Add more for advisory composition.
 * Jan 26, 2021 86746      jwu          Add backup site.
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
@DynamicSerialize
public class AtcfEnvironmentConfig {

    // TODO: need a batter way to define ATCF_DATA_PATH
    private static final String ATCF_DATA_PATH = "/awips2" + File.separator
            + "edex" + File.separator + "data" + File.separator + "atcf"
            + File.separator;

    private static final String ATCF_STORM_DIR = ATCF_DATA_PATH + File.separator
            + "storms" + File.separator;

    /**
     * ATCF base directory.
     */
    @DynamicSerializeElement
    private String atcfstrms;

    /**
     * Directory where data prepared for WCOSS computation is pushed to.
     */
    @DynamicSerializeElement
    private String atcf_outgoing;

    /**
     * Directory where WCOSS results are stored for ATCF to retrieve from.
     */
    @DynamicSerializeElement
    private String atcf_retrieved;

    /**
     * Intermediate directory for moving ATCF computation data around.
     */
    @DynamicSerializeElement
    private String atcftmp;

    /**
     * Directory for archiving ATCF data.
     */
    @DynamicSerializeElement
    private String atcf_arch;

    /**
     * Number of minutes to wait for WCOSS processing after sending data
     */
    @DynamicSerializeElement
    private String wcoss_timeout;

    /**
     * Directory where new fix data files could be pushed in.
     */
    @DynamicSerializeElement
    private String fixDataIn;

    /**
     * Directory where new fix data files will be moved to after importing.
     */
    @DynamicSerializeElement
    private String fixDataOut;

    /**
     * Directory where aids messages will be stored.
     */
    @DynamicSerializeElement
    private String aidMessagesDir;

    /**
     * Main ATCF site such as NHC/WPC/CPHC
     */
    @DynamicSerializeElement
    private String atcfSite;

    /**
     * ATCF backup site such as NHC/WPC/CPHC
     */
    @DynamicSerializeElement
    private String backupSite;

    /**
     * Directory where advisory products will be stored.
     */
    @DynamicSerializeElement
    private String advisoryPath;

    /**
     * Directory where advisory products will be archived.
     */
    @DynamicSerializeElement
    private String advisoryArchivePath;

    /**
     * Constructor.
     */
    public AtcfEnvironmentConfig() {
        this.setToDefault();
    }

    public String getAtcfstrms() {
        return atcfstrms;
    }

    public void setAtcfstrms(String atcfstrms) {
        this.atcfstrms = ATCF_DATA_PATH + atcfstrms;
    }

    public String getAtcf_outgoing() {
        return atcf_outgoing;
    }

    public void setAtcf_outgoing(String atcf_outgoing) {
        this.atcf_outgoing = ATCF_STORM_DIR + atcf_outgoing;
    }

    public String getAtcf_retrieved() {
        return atcf_retrieved;
    }

    public void setAtcf_retrieved(String atcf_retrieved) {
        this.atcf_retrieved = ATCF_STORM_DIR + atcf_retrieved;
    }

    public String getAtcftmp() {
        return atcftmp;
    }

    public void setAtcftmp(String atcftmp) {
        this.atcftmp = ATCF_STORM_DIR + atcftmp;
    }

    public String getAtcf_arch() {
        return atcf_arch;
    }

    public void setAtcf_arch(String atcf_arch) {
        this.atcf_arch = ATCF_DATA_PATH + atcf_arch;
    }

    public String getWcoss_timeout() {
        return wcoss_timeout;
    }

    public void setWcoss_timeout(String wcoss_timeout) {
        this.wcoss_timeout = wcoss_timeout;
    }

    public String getFixDataIn() {
        return fixDataIn;
    }

    public void setFixDataIn(String fixDataIn) {
        this.fixDataIn = ATCF_DATA_PATH + fixDataIn;
    }

    public String getFixDataOut() {
        return fixDataOut;
    }

    public void setFixDataOut(String fixDataOut) {
        this.fixDataOut = ATCF_DATA_PATH + fixDataOut;
    }

    public String getAidMessagesDir() {
        return aidMessagesDir;
    }

    public void setAidMessagesDir(String aidMessagesDir) {
        this.aidMessagesDir = ATCF_DATA_PATH + aidMessagesDir;
    }

    /**
     * @return the atcfSite
     */
    public String getAtcfSite() {
        return atcfSite;
    }

    /**
     * @param atcfSite
     *            the atcfSite to set
     */
    public void setAtcfSite(String atcfSite) {
        this.atcfSite = atcfSite;
    }

    /**
     * @return the backupSite
     */
    public String getBackupSite() {
        return backupSite;
    }

    /**
     * @param backupSite
     *            the backupSite to set
     */
    public void setBackupSite(String backupSite) {
        this.backupSite = backupSite;
    }

    /**
     * @return the advisoryPath
     */
    public String getAdvisoryPath() {
        return advisoryPath;
    }

    /**
     * @param advisoryPath
     *            the advisoryPath to set
     */
    public void setAdvisoryPath(String advisoryPath) {
        this.advisoryPath = ATCF_DATA_PATH + advisoryPath;
    }

    /**
     * @return the advisoryArchivePath
     */
    public String getAdvisoryArchivePath() {
        return advisoryArchivePath;
    }

    /**
     * @param advisoryArchivePath
     *            the advisoryArchivePath to set
     */
    public void setAdvisoryArchivePath(String advisoryArchivePath) {
        this.advisoryArchivePath = ATCF_DATA_PATH + advisoryArchivePath;
    }

    /**
     * setTo Default only when somehow the base or site level localization
     * atcf.property couldn't be accessed
     */
    private void setToDefault() {
        this.atcfstrms = ATCF_STORM_DIR;
        this.atcf_outgoing = ATCF_STORM_DIR + "outgoing";
        this.atcf_retrieved = ATCF_STORM_DIR + "retrieved";
        this.atcftmp = ATCF_STORM_DIR + "tmp";
        this.atcf_arch = ATCF_DATA_PATH + "archives";
        this.wcoss_timeout = String.valueOf(8);
        this.fixDataIn = ATCF_DATA_PATH + "fnmocin";
        this.fixDataOut = ATCF_DATA_PATH + "fnmocout";
        this.aidMessagesDir = ATCF_DATA_PATH + "aidmessages";
        this.atcfSite = "NHC";
        this.backupSite = "";
        this.advisoryPath = ATCF_DATA_PATH + "nhc_messages";
        this.advisoryArchivePath = this.advisoryPath + File.separator
                + "archive";
    }

}