/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ATCF text product header information for advisory/message.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer     Description 
 * ------------ ----------- ------------ ---------------------- 
 * Oct 21, 2020 82721       jwu          Initial creation
 * Jan 26, 2021 86746       jwu          Add more fields.
 * Mar 22, 2021 88518       dfriedman    Remove node; add wmoId.
 * 
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class AdvisoryHeader {

    // TODO more may be added as necessary later.
    /**
     * The TTAAii part of the WMO heading
     *
     */
    @DynamicSerializeElement
    private String wmoId;

    /**
     * site ID
     *
     * <pre>
     * "NHC, "CPHC", or "WPC".
     * </pre>
     */
    @DynamicSerializeElement
    private String siteId;

    /**
     * wfo ID
     *
     * <pre>
     * "KNHC" for site NHC, "PHFO"for site CPHC, and "KNFD" for site WPC.
     * </pre>
     */
    @DynamicSerializeElement
    private String wfo;

    /**
     * Description of main issuing office
     * 
     * <pre>
     * For ATCF, it is like "NWS National Hurricane Center, Miami, FL"
     * </pre>
     */
    @DynamicSerializeElement
    private String issuedBy;

    /**
     * NNN: product category (i.e, TCP, TCM etc.)
     */
    @DynamicSerializeElement
    private String category;

    /**
     * xxx: Three letter code for the station that is being broadcasted.
     * 
     * <pre>
     * For ATCF, xxx = Basin ID + awips bin number, such as "AT1", where
     *      Basin id is AT, CP, EP etc.
     *      Awips bin number is 1 to 5, rotated by cyclone number % 5.
     * </pre>
     */
    @DynamicSerializeElement
    private String basin;

    @DynamicSerializeElement
    private int binNumber;

    /**
     * Advisory number
     */
    @DynamicSerializeElement
    private int advNumber;

    /**
     * Storm ID
     */
    @DynamicSerializeElement
    private String stormId;

    /**
     * Forecast name (first name, not initials).
     */
    @DynamicSerializeElement
    private String forecaster;

    /**
     * Report time
     *
     * <pre>
     * For ATCF, it is like "1000 PM CDT Tue Aug 28 2012"
     * </pre>
     */
    @DynamicSerializeElement
    private String advTime;

    /**
     * Issue time
     *
     * <pre>
     * For ATCF, it is like "DDHHMM" -> "101921"
     * </pre>
     */
    @DynamicSerializeElement
    private String issuedTime;

    /**
     * Time zone. i.e, "EST", "CDT", etc.
     */
    @DynamicSerializeElement
    private String timeZone;

    /**
     * Storm/Advisory name
     */
    @DynamicSerializeElement
    private String advName;

    /**
     * Storm/Advisory intensity class (i.e, Hurricane)
     */
    @DynamicSerializeElement
    private String advClass;

    /**
     * Advisory number phrase after storm class and name. "Special" or "SP" may
     * be added for special advisory depending on the length of the line.
     */
    @DynamicSerializeElement
    private String advNumPhrase;

    /**
     * Flag for NHC.
     */
    @DynamicSerializeElement
    private boolean issuedByNHC;

    /**
     * Flag for CPHC.
     */
    @DynamicSerializeElement
    private boolean issuedByCPHC;

    /**
     * Flag for WPC.
     */
    @DynamicSerializeElement
    private boolean issuedByWPC;

    /**
     * Flag for if it is backup office.
     */
    @DynamicSerializeElement
    private boolean backup;

    /**
     * Description of backup issuing office
     * 
     * <pre>
     * For ATCF, it is like "NWS National Hurricane Center, Miami, FL"
     * </pre>
     */
    @DynamicSerializeElement
    private String issuedByBackup;

    /**
     * Flag for if NHC is the backup.
     */
    @DynamicSerializeElement
    private boolean backupByNHC;

    /**
     * Flag for if CPHC is the backup.
     */
    @DynamicSerializeElement
    private boolean backupByCPHC;

    /**
     * Flag for if WPC is the backup.
     */
    @DynamicSerializeElement
    private boolean backupByWPC;

    /**
     * Flag for final advisory.
     */
    @DynamicSerializeElement
    private boolean finalAdv;

    /**
     * Flag for potential tropicla cyclone.
     */
    @DynamicSerializeElement
    private boolean potentialTC;

    /**
     * Issue center for the final advisory. May vary by product and basin.
     */
    @DynamicSerializeElement
    private String lastAdvFrom;

    /**
     * Flag for if public adv frequency is 3..
     */
    @DynamicSerializeElement
    private boolean pubFreqEq3;

    /**
     * Constructor.
     */
    public AdvisoryHeader() {
        this.wmoId = "TTAAII";
        this.siteId = "NHC";
        this.wfo = "KNHC";
        this.category = "TCP";
        this.basin = "AT";
        this.binNumber = 1;
        this.advNumber = 1;
        this.stormId = "";
        this.forecaster = "";
        this.advTime = "";
        this.issuedBy = "";
        this.issuedTime = "";
        this.timeZone = "";
        this.advName = "";
        this.advClass = "";
        this.advNumPhrase = "";
        this.issuedByNHC = false;
        this.issuedByCPHC = false;
        this.issuedByWPC = false;
        this.backup = false;
        this.issuedByBackup = "";
        this.backupByNHC = false;
        this.backupByCPHC = false;
        this.backupByWPC = false;
        this.finalAdv = false;
        this.potentialTC = false;
        this.lastAdvFrom = "";
        this.pubFreqEq3 = false;
    }

    /**
     * Constructor.
     * 
     * @param hdr
     *            AdvisoryHeader;
     */
    public AdvisoryHeader(AdvisoryHeader hdr) {
        this.wmoId = hdr.wmoId;
        this.siteId = hdr.siteId;
        this.wfo = hdr.wfo;
        this.category = hdr.category;
        this.basin = hdr.basin;
        this.binNumber = hdr.binNumber;
        this.advNumber = hdr.advNumber;
        this.stormId = hdr.stormId;
        this.forecaster = hdr.forecaster;
        this.advTime = hdr.advTime;
        this.issuedBy = hdr.issuedBy;
        this.issuedTime = hdr.issuedTime;
        this.timeZone = hdr.timeZone;
        this.advName = hdr.advName;
        this.advClass = hdr.advClass;
        this.advNumPhrase = hdr.advNumPhrase;
        this.issuedByNHC = hdr.issuedByNHC;
        this.issuedByCPHC = hdr.issuedByCPHC;
        this.issuedByWPC = hdr.issuedByWPC;
        this.backup = hdr.backup;
        this.issuedByBackup = hdr.issuedByBackup;
        this.backupByNHC = hdr.backupByNHC;
        this.backupByCPHC = hdr.backupByCPHC;
        this.backupByWPC = hdr.backupByWPC;
        this.finalAdv = hdr.finalAdv;
        this.potentialTC = hdr.potentialTC;
        this.lastAdvFrom = hdr.lastAdvFrom;
        this.pubFreqEq3 = hdr.pubFreqEq3;
    }

    public String getWmoId() {
        return wmoId;
    }

    public void setWmoId(String wmoId) {
        this.wmoId = wmoId;
    }

    /**
     * @return the siteId
     */
    public String getSiteId() {
        return siteId;
    }

    /**
     * @param siteId
     *            the siteId to set
     */
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    /**
     * @return the wfo
     */
    public String getWfo() {
        return wfo;
    }

    /**
     * @param wfo
     *            the wfo to set
     */
    public void setWfo(String wfo) {
        this.wfo = wfo;
    }

    /**
     * @return the advNumber
     */
    public int getAdvNumber() {
        return advNumber;
    }

    /**
     * @param advNumber
     *            the advNumber to set
     */
    public void setAdvNumber(int advNumber) {
        this.advNumber = advNumber;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the basin
     */
    public String getBasin() {
        return basin;
    }

    /**
     * @param basin
     *            the basin to set
     */
    public void setBasin(String basin) {
        this.basin = basin;
    }

    /**
     * @return the binNumber
     */
    public int getBinNumber() {
        return binNumber;
    }

    /**
     * @param binNumber
     *            the binNumber to set
     */
    public void setBinNumber(int binNumber) {
        this.binNumber = binNumber;
    }

    /**
     * @return the issuedBy
     */
    public String getIssuedBy() {
        return issuedBy;
    }

    /**
     * @param issuedBy
     *            the issuedBy to set
     */
    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    /**
     * @return the advTime
     */
    public String getAdvTime() {
        return advTime;
    }

    /**
     * @param advTime
     *            the advTime to set
     */
    public void setAdvTime(String advTime) {
        this.advTime = advTime;
    }

    /**
     * @return the stormId
     */
    public String getStormId() {
        return stormId;
    }

    /**
     * @param stormId
     *            the stormId to set
     */
    public void setStormId(String stormId) {
        this.stormId = stormId;
    }

    /**
     * @return the forecaster
     */
    public String getForecaster() {
        return forecaster;
    }

    /**
     * @param forecaster
     *            the forecaster to set
     */
    public void setForecaster(String forecaster) {
        this.forecaster = forecaster;
    }

    /**
     * @return the issuedTime
     */
    public String getIssuedTime() {
        return issuedTime;
    }

    /**
     * @param issuedTime
     *            the issuedTime to set
     */
    public void setIssuedTime(String issuedTime) {
        this.issuedTime = issuedTime;
    }

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the advName
     */
    public String getAdvName() {
        return advName;
    }

    /**
     * @param advName the advName to set
     */
    public void setAdvName(String advName) {
        this.advName = advName;
    }

    /**
     * @return the advClass
     */
    public String getAdvClass() {
        return advClass;
    }

    /**
     * @param advClass the advClass to set
     */
    public void setAdvClass(String advClass) {
        this.advClass = advClass;
    }


    /**
     * @return the advNumPhrase
     */
    public String getAdvNumPhrase() {
        return advNumPhrase;
    }

    /**
     * @param advNumPhrase
     *            the advNumPhrase to set
     */
    public void setAdvNumPhrase(String advNumPhrase) {
        this.advNumPhrase = advNumPhrase;
    }

    /**
     * @return the issuedByNHC
     */
    public boolean isIssuedByNHC() {
        return issuedByNHC;
    }

    /**
     * @param issuedByNHC
     *            the issuedByNHC to set
     */
    public void setIssuedByNHC(boolean issuedByNHC) {
        this.issuedByNHC = issuedByNHC;
    }

    /**
     * @return the issuedByCPHC
     */
    public boolean isIssuedByCPHC() {
        return issuedByCPHC;
    }

    /**
     * @param issuedByCPHC
     *            the issuedByCPHC to set
     */
    public void setIssuedByCPHC(boolean issuedByCPHC) {
        this.issuedByCPHC = issuedByCPHC;
    }

    /**
     * @return the issuedByWPC
     */
    public boolean isIssuedByWPC() {
        return issuedByWPC;
    }

    /**
     * @param issuedByWPC
     *            the issuedByWPC to set
     */
    public void setIssuedByWPC(boolean issuedByWPC) {
        this.issuedByWPC = issuedByWPC;
    }

    /**
     * @return the backup
     */
    public boolean isBackup() {
        return backup;
    }

    /**
     * @param backup
     *            the backup to set
     */
    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    /**
     * @return the issuedByBackup
     */
    public String getIssuedByBackup() {
        return issuedByBackup;
    }

    /**
     * @param issuedByBackup
     *            the issuedByBackup to set
     */
    public void setIssuedByBackup(String issuedByBackup) {
        this.issuedByBackup = issuedByBackup;
    }

    /**
     * @return the backupByNHC
     */
    public boolean isBackupByNHC() {
        return backupByNHC;
    }

    /**
     * @param backupByNHC
     *            the backupByNHC to set
     */
    public void setBackupByNHC(boolean backupByNHC) {
        this.backupByNHC = backupByNHC;
    }

    /**
     * @return the backupByCPHC
     */
    public boolean isBackupByCPHC() {
        return backupByCPHC;
    }

    /**
     * @param backupByCPHC
     *            the backupByCPHC to set
     */
    public void setBackupByCPHC(boolean backupByCPHC) {
        this.backupByCPHC = backupByCPHC;
    }

    /**
     * @return the backupByWPC
     */
    public boolean isBackupByWPC() {
        return backupByWPC;
    }

    /**
     * @param backupByWPC
     *            the backupByWPC to set
     */
    public void setBackupByWPC(boolean backupByWPC) {
        this.backupByWPC = backupByWPC;
    }

    /**
     * @return the finalAdv
     */
    public boolean isFinalAdv() {
        return finalAdv;
    }

    /**
     * @param finalAdv
     *            the finalAdv to set
     */
    public void setFinalAdv(boolean finalAdv) {
        this.finalAdv = finalAdv;
    }

    /**
     * @return the potentialTC
     */
    public boolean isPotentialTC() {
        return potentialTC;
    }

    /**
     * @param potentialTC
     *            the potentialTC to set
     */
    public void setPotentialTC(boolean potentialTC) {
        this.potentialTC = potentialTC;
    }

    /**
     * @return the lastAdvFrom
     */
    public String getLastAdvFrom() {
        return lastAdvFrom;
    }

    /**
     * @param lastAdvFrom
     *            the lastAdvFrom to set
     */
    public void setLastAdvFrom(String lastAdvFrom) {
        this.lastAdvFrom = lastAdvFrom;
    }

    /**
     * @return the pubFreqEq3
     */
    public boolean isPubFreqEq3() {
        return pubFreqEq3;
    }

    /**
     * @param pubFreqEq3
     *            the pubFreqEq3 to set
     */
    public void setPubFreqEq3(boolean pubFreqEq3) {
        this.pubFreqEq3 = pubFreqEq3;
    }

}
