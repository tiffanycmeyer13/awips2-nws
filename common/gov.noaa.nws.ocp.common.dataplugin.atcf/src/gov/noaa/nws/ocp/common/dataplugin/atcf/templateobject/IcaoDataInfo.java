/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class to hold data to generate an ICAO Aviation Advisory (TCA).
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 26, 2021 86746      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class IcaoDataInfo {

    // Default value for missing data
    public static final float MISSING_VALUE = -999;

    // Default advisory TAU for TCA
    public static final int TCA_FIXED_TAU = 3;

    // Default Forecast TAUs for TCA.
    protected static final int[] TCA_MAR_TAU = new int[] { 0, 6, 12, 18, 24 };

    // Default advisory TAU for TCA
    public static final int MAX_ITEMS = 5;

    // Forecast TAUs for TCA (could change for special advisory
    @DynamicSerializeElement
    private int[] tcaMarTau;

    // TCA times and data
    @DynamicSerializeElement
    private int mslp;

    @DynamicSerializeElement
    private int dir;

    @DynamicSerializeElement
    private int spd;

    // TCA times and data
    @DynamicSerializeElement
    private int[] startTime;

    @DynamicSerializeElement
    private float[] startLat;

    @DynamicSerializeElement
    private float[] startLon;

    @DynamicSerializeElement
    private float[] startMaxWind;

    @DynamicSerializeElement
    private int[] fcstTime;

    @DynamicSerializeElement
    private float[] fcstLat;

    @DynamicSerializeElement
    private float[] fcstLon;

    @DynamicSerializeElement
    private float[] fcstMaxWind;

    /**
     * Constructor
     */
    public IcaoDataInfo() {

        super();

        tcaMarTau = new int[MAX_ITEMS];
        startTime = new int[MAX_ITEMS];
        startLat = new float[MAX_ITEMS];
        startLon = new float[MAX_ITEMS];
        startMaxWind = new float[MAX_ITEMS];
        fcstTime = new int[MAX_ITEMS];
        fcstLat = new float[MAX_ITEMS];
        fcstLon = new float[MAX_ITEMS];
        fcstMaxWind = new float[MAX_ITEMS];

        for (int ii = 0; ii < MAX_ITEMS; ii++) {
            tcaMarTau[ii] = TCA_MAR_TAU[ii];
            startTime[ii] = ii * 12;
            startLat[ii] = MISSING_VALUE;
            startLon[ii] = MISSING_VALUE;
            startMaxWind[ii] = MISSING_VALUE;
            fcstTime[ii] = (int) MISSING_VALUE;
            fcstLat[ii] = MISSING_VALUE;
            fcstLon[ii] = MISSING_VALUE;
            fcstMaxWind[ii] = MISSING_VALUE;
        }
    }

    /**
     * @return the tcaMarTau
     */
    public int[] getTcaMarTau() {
        return tcaMarTau;
    }

    /**
     * @param tcaMarTau
     *            the tcaMarTau to set
     */
    public void setTcaMarTau(int[] tcaMarTau) {
        this.tcaMarTau = tcaMarTau;
    }

    /**
     * @return the mslp
     */
    public int getMslp() {
        return mslp;
    }

    /**
     * @param mslp
     *            the mslp to set
     */
    public void setMslp(int mslp) {
        this.mslp = mslp;
    }

    /**
     * @return the dir
     */
    public int getDir() {
        return dir;
    }

    /**
     * @param dir
     *            the dir to set
     */
    public void setDir(int dir) {
        this.dir = dir;
    }

    /**
     * @return the spd
     */
    public int getSpd() {
        return spd;
    }

    /**
     * @param spd
     *            the spd to set
     */
    public void setSpd(int spd) {
        this.spd = spd;
    }

    /**
     * @return the startTime
     */
    public int[] getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(int[] startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the startLat
     */
    public float[] getStartLat() {
        return startLat;
    }

    /**
     * @param startLat
     *            the startLat to set
     */
    public void setStartLat(float[] startLat) {
        this.startLat = startLat;
    }

    /**
     * @return the startLon
     */
    public float[] getStartLon() {
        return startLon;
    }

    /**
     * @param startLon
     *            the startLon to set
     */
    public void setStartLon(float[] startLon) {
        this.startLon = startLon;
    }

    /**
     * @return the startMaxWind
     */
    public float[] getStartMaxWind() {
        return startMaxWind;
    }

    /**
     * @param startMaxWind
     *            the startMaxWind to set
     */
    public void setStartMaxWind(float[] startMaxWind) {
        this.startMaxWind = startMaxWind;
    }

    /**
     * @return the fcstTime
     */
    public int[] getFcstTime() {
        return fcstTime;
    }

    /**
     * @param fcstTime
     *            the fcstTime to set
     */
    public void setFcstTime(int[] fcstTime) {
        this.fcstTime = fcstTime;
    }

    /**
     * @return the fcstLat
     */
    public float[] getFcstLat() {
        return fcstLat;
    }

    /**
     * @param fcstLat
     *            the fcstLat to set
     */
    public void setFcstLat(float[] fcstLat) {
        this.fcstLat = fcstLat;
    }

    /**
     * @return the fcstLon
     */
    public float[] getFcstLon() {
        return fcstLon;
    }

    /**
     * @param fcstLon
     *            the fcstLon to set
     */
    public void setFcstLon(float[] fcstLon) {
        this.fcstLon = fcstLon;
    }

    /**
     * @return the fcstMaxWind
     */
    public float[] getFcstMaxWind() {
        return fcstMaxWind;
    }

    /**
     * @param fcstMaxWind
     *            the fcstMaxWind to set
     */
    public void setFcstMaxWind(float[] fcstMaxWind) {
        this.fcstMaxWind = fcstMaxWind;
    }

    /**
     * Constructs a string representation of class
     *
     * @return String
     */
    @Override
    public String toString() {

        StringBuilder infoStr = new StringBuilder();

        infoStr.append("****** Forecast Track Data *********************\n");
        infoStr.append(String.format("%3s\t%6s\t%7s\t\t%3s\t%4s\t%3s\t%3s\n",
                "TAU", "LAT", "LON", "VMAX", "MSLP", "DIR", "SPEED"));
        String fmt1 = "%03d\t%6.1f\t%7.1f\t\t%03d\n";
        String fmt2 = "%03d\t%6.1f\t%7.1f\t\t%03d\t%04d\t%03d\t%03d\n";
        for (int ii = 0; ii < MAX_ITEMS; ii++) {
            if (ii == 0) {
                infoStr.append(String.format(fmt2, startTime[ii], startLat[ii],
                        startLon[ii], (int) startMaxWind[ii], mslp, dir,
                         spd));
            } else {
                infoStr.append(String.format(fmt1, startTime[ii], startLat[ii],
                        startLon[ii], (int) startMaxWind[ii]));
            }
        }

        infoStr.append(
                "\n****** Interpolated Forecast Track Data **********\n");
        infoStr.append(String.format("%3s\t%6s\t%7s\t%3s\t%4s\t%3s\t%3s\n",
                "TAU", "LAT", "LON", "VMAX", "MSLP", "DIR", "SPEED"));
        for (int ii = 0; ii < MAX_ITEMS; ii++) {
            infoStr.append(String.format(fmt1, fcstTime[ii], fcstLat[ii], fcstLon[ii],
                    (int) fcstMaxWind[ii]));
        }

        return infoStr.toString();
    }

}
