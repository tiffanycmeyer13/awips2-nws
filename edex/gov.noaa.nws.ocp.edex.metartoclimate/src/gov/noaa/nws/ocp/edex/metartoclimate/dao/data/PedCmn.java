/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Decoded METAR data PED Common variables, from METAR report text. From
 * hmPED_cmn.h#PedCmnStruct
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 01 FEB 2017  28609      amoore      Initial creation
 * 22 APR 2022  21456      pwang       Fix missing wx from RMK
 * 04 DEC 2023  2036600    pwang       Fix moderate precip
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class PedCmn {

    /**
     * Station ID length.
     */
    public static final int STATION_ID_LENGTH = 4;

    /** station ID */
    private String stationID;

    /** types of weather obscurations */
    private String[] wxObstruct = new String[DecodedMetar.NUM_REWX];

    /** hour of observation */
    private int obHour;

    /** minutes of observation */
    private int obMinute;

    /** day of observation */
    private int obDay;

    /** month of observation */
    private int obMon;

    /** year of observation */
    private int obYear;

    /** vertical visibility */
    private int verticalVisibility;

    /** is a nil report */
    private boolean nil;

    /** cavok indicator */
    private boolean cavok;

    /**
     * flag for status from decoder 0 = successful, 1 = undefined token, 2 =
     * syntax error in token
     */
    private int decodeStatus;

    /** prevail visibility in kilometers */
    private float prevailingVisibilitySM;

    /** wind data structure */
    private MetarWind winData;

    /** array for cloud type data */
    private CloudConditions[] cloudConditions = new CloudConditions[DecodedMetar.NUM_CLOUD_CONDITIONS];

    /**
     * Empty constructor.
     */
    public PedCmn() {
    }

    /**
     * @return the stationID
     */
    public String getStationID() {
        return stationID;
    }

    /**
     * @param stationID
     *            the stationID to set
     */
    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    /**
     * @return the wxObstruct
     */
    public String[] getWxObstruct() {
        return wxObstruct;
    }

    /**
     * @param wxObstruct
     *            the wxObstruct to set
     */
    public void setWxObstruct(String[] wxObstruct) {
        this.wxObstruct = wxObstruct;
    }

    /**
     * setOneWxObstruct: this method will be used for add a wx from METAR RMK
     * section The logic will be: if the current Obstruct (10 elements array)
     * doesn't contain same type of wx, add it in, otherwise, ignore it.
     * 
     * @param wx
     */
    public void setOneWxObstruct(String wx) {
        int index = 0;
        // build a unique wx type set to avoid duplication
        Set<String> addedWxSet = new HashSet<>();
        for (String currWx : wxObstruct) {
            if (!currWx.isBlank()) {
                if (currWx.startsWith("-") || currWx.startsWith("+")) {
                    addedWxSet.add(currWx.substring(1));
                } else {
                    addedWxSet.add(currWx);
                }
                index++;
            }
        }
        // input wx may have intensity, "-" or "+"
        String intensity = "";
        if (wx.startsWith("-") || wx.startsWith("+")) {
            intensity = wx.substring(0, 1);
            wx = wx.substring(1);
        }
        if (!addedWxSet.contains(wx)) {
            this.wxObstruct[index] = intensity + wx;
        }
    }

    /**
     * @return the obHour
     */
    public int getObHour() {
        return obHour;
    }

    /**
     * @param obHour
     *            the obHour to set
     */
    public void setObHour(int obHour) {
        this.obHour = obHour;
    }

    /**
     * @return the obMinute
     */
    public int getObMinute() {
        return obMinute;
    }

    /**
     * @param obMinute
     *            the obMinute to set
     */
    public void setObMinute(int obMinute) {
        this.obMinute = obMinute;
    }

    /**
     * @return the obDay
     */
    public int getObDay() {
        return obDay;
    }

    /**
     * @param obDay
     *            the obDay to set
     */
    public void setObDay(int obDay) {
        this.obDay = obDay;
    }

    /**
     * @return the obMon
     */
    public int getObMon() {
        return obMon;
    }

    /**
     * @param obMon
     *            the obMon to set
     */
    public void setObMon(int obMon) {
        this.obMon = obMon;
    }

    /**
     * @return the obYear
     */
    public int getObYear() {
        return obYear;
    }

    /**
     * @param obYear
     *            the obYear to set
     */
    public void setObYear(int obYear) {
        this.obYear = obYear;
    }

    /**
     * @return the verticalVisibility
     */
    public int getVerticalVisibility() {
        return verticalVisibility;
    }

    /**
     * @param verticalVisibility
     *            the verticalVisibility to set
     */
    public void setVerticalVisibility(int verticalVisibility) {
        this.verticalVisibility = verticalVisibility;
    }

    /**
     * @return the nil
     */
    public boolean isNil() {
        return nil;
    }

    /**
     * @param nil
     *            the nil to set
     */
    public void setNil(boolean nil) {
        this.nil = nil;
    }

    /**
     * @return the cavok
     */
    public boolean isCavok() {
        return cavok;
    }

    /**
     * @param cavok
     *            the cavok to set
     */
    public void setCavok(boolean cavok) {
        this.cavok = cavok;
    }

    /**
     * @return the decodeStatus
     */
    public int getDecodeStatus() {
        return decodeStatus;
    }

    /**
     * @param decodeStatus
     *            the decodeStatus to set
     */
    public void setDecodeStatus(int decodeStatus) {
        this.decodeStatus = decodeStatus;
    }

    /**
     * @return the prevailingVisibilitySM
     */
    public float getPrevailingVisibilitySM() {
        return prevailingVisibilitySM;
    }

    /**
     * @param prevailingVisibilitySM
     *            the prevailingVisibilitySM to set
     */
    public void setPrevailingVisibilitySM(float prevailingVisibilitySM) {
        this.prevailingVisibilitySM = prevailingVisibilitySM;
    }

    /**
     * @return the winData
     */
    public MetarWind getWinData() {
        return winData;
    }

    /**
     * @param winData
     *            the winData to set
     */
    public void setWinData(MetarWind winData) {
        this.winData = winData;
    }

    /**
     * @return the cloudConditions
     */
    public CloudConditions[] getCloudConditions() {
        return cloudConditions;
    }

    /**
     * @param cloudConditions
     *            the cloudConditions to set
     */
    public void setCloudConditions(CloudConditions[] cloudConditions) {
        this.cloudConditions = cloudConditions;
    }
}
