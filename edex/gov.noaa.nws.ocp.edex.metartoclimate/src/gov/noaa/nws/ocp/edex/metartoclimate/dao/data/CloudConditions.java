/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data cloud conditions, from METAR report text. From
 * hmPED_cmn.h#Cloud_Conditions
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 01 FEB 2017  28609      amoore      Initial creation
 * 21 FEB 2017  28609      amoore      Clean up and clarification.
 * 03 MAY 2017  33104      amoore      Addressing FindBugs. Define equals for string.
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class CloudConditions {

    /**
     * cloud type
     */
    private String cloudType;

    /**
     * The original height in feet as displayed in the METAR. 3 or 4 characters.
     * 
     */
    private String cloudHgtChar;

    /**
     * other types of cloud phenomena
     */
    private String otherCldPhenom;

    /**
     * cloud height in meters
     */
    private int cloudHgtMeters;

    /**
     * TCU Cloud type value. From hmPED_WMO.h.
     */
    public static final int TCU_CLOUD_TYPE = 3;

    /**
     * CB Cloud type value. From hmPED_WMO.h.
     */
    public static final int CB_CLOUD_TYPE = 2;

    /**
     * Broken clouds cover value. From hmPED_WMO.h.
     */
    public static final float BKN_COVER = 0.8f;

    /**
     * Scattered clouds cover value. From hmPED_WMO.h.
     */
    public static final float SCT_COVER = 0.4f;

    /**
     * Few clouds cover value. From hmPED_WMO.h.
     */
    public static final float FEW_COVER = 0.3f;

    /**
     * Overcast cover value. From hmPED_WMO.h.
     */
    public static final float OVC_COVER = 1.0f;

    /**
     * Clear cover value. From hmPED_WMO.h.
     */
    public static final float CLR_COVER = 0;

    /**
     * Empty constructor.
     */
    public CloudConditions() {
    }

    /**
     * @return the cloudType
     */
    public String getCloudType() {
        return cloudType;
    }

    /**
     * @param cloudType
     *            the cloudType to set
     */
    public void setCloudType(String cloudType) {
        this.cloudType = cloudType;
    }

    /**
     * The original height in feet as displayed in the METAR. 3 or 4 characters.
     * 
     * @return the cloudHgtChar
     */
    public String getCloudHgtChar() {
        return cloudHgtChar;
    }

    /**
     * The original height in feet as displayed in the METAR. 3 or 4 characters.
     * 
     * @param cloudHgtChar
     *            the cloudHgtChar to set
     */
    public void setCloudHgtChar(String cloudHgtChar) {
        this.cloudHgtChar = cloudHgtChar;
    }

    /**
     * @return the otherCldPhenom
     */
    public String getOtherCldPhenom() {
        return otherCldPhenom;
    }

    /**
     * @param otherCldPhenom
     *            the otherCldPhenom to set
     */
    public void setOtherCldPhenom(String otherCldPhenom) {
        this.otherCldPhenom = otherCldPhenom;
    }

    /**
     * @return the cloudHgtMeters
     */
    public int getCloudHgtMeters() {
        return cloudHgtMeters;
    }

    /**
     * @param cloudHgtMeters
     *            the cloudHgtMeters to set
     */
    public void setCloudHgtMeters(int cloudHgtMeters) {
        this.cloudHgtMeters = cloudHgtMeters;
    }

    /**
     * @param other
     *            cloud type
     * @return true if both this cloud type and the given one are null or both
     *         are the same.
     */
    public boolean matchedType(String other) {
        return (other == null && cloudType == null)
                || (cloudType != null && cloudType.equals(other));
    }
}
