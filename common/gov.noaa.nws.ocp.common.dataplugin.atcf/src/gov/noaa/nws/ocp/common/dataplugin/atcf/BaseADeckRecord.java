/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.util.DtgUtil;

/**
 * Contains definitions common to a-deck and b-deck records.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2018 53502      dfriedman   Initial creation based on
 *                                     original a- and b-deck records.
 * Sep 12, 2019 68237      dfriedman   Add DataURI annotations.
 * Nov 08, 2019 70253      mporricelli Tweaked toADeckString() to
 *                                     eliminate some trailing empty
 *                                     CSV fields in output not seen
 *                                     in legacy .dat, .com files
 * Apr 16, 2020 71986      mporricelli Added sorting of a-deck
 *                                     records for creating deck files
 * Apr 24, 2020 77847      jwu         Add a few convenience methods
 * Jun 11, 2019 #68118     wpaintsil   Add @DynamicSerialize
 * Jun 16, 2020 #79546     dfriedman   Fix output formatting.
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
@MappedSuperclass
@DynamicSerialize
public class BaseADeckRecord extends AbstractDeckRecord
        implements ITrackDeckRecord {

    private static final long serialVersionUID = 1L;

    /**
     * Pressure in millibars of the last closed isobar. 900 - 1050 mb
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float closedP;

    /**
     * Eye diameter. 0 - 999nm
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float eyeSize;

    /**
     * Radius of the last closed isobar in nm. 0 - 9999 nm
     */
    @DynamicSerializeElement
    private float radClosedP;

    /**
     * Gusts. 0 - 995 kts.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float gust;

    /**
     * Level of tropical cyclone development; such as DB, TD, TS, TY, ...
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String intensity;

    /**
     * Max seas: 0 through 999 ft.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float maxSeas;

    /**
     * Radius of max winds. 0 - 999 nm
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float maxWindRad;

    /**
     * Minimum sea level pressure, 1 through 1100MB
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float mslp;

    /**
     * Wind intensity (kts) for the radii defined i this record: 34, 50, 64
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float radWind;

    /**
     * Radius Code for wind intensity: AAA = full circle; QQQ = quadrant (NNQ,
     * NEQ, EEQ, SEQ, SSQ, SWQ, WWQ, NWQ)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radWindQuad;

    /**
     * If full circle, radius of specified wind intensity. If semicircle or
     * quadrant, radius of specified wind intensity of circle portion specified
     * in radius code. 0 - 1200 nm.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad1WindRad;

    /**
     * If full circle, this field not used. If semicircle, radius (nm) of
     * specified wind intensity for semicircle not specified in radius code. If
     * quadrant, radius (nm) of specified wind intensity for 2nd quadrant
     * (counting clockwise from quadrant specified in radius code). 0 - 1200 nm.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad2WindRad;

    /**
     * If full circle or semicircle this field not used. If quadrant, radius
     * (nm) of specified wind intensity for 3rd quadrant (counting clockwise
     * from quadrant specified in radius code). 0 - 1200 nm.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad3WindRad;

    /**
     * If full circle or semicircle this field not used. If quadrant, radius
     * (nm) of specified wind intensity for 4th quadrant (counting clockwise
     * from quadrant specified in radius code). 0 - 1200 nm.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad4WindRad;

    /**
     * Radius code for seas wave height
     */
    @Column(length = 8, nullable = false)
    @DynamicSerializeElement
    private String radWaveQuad;

    /**
     * First quadrant seas radius as defined by radWaveQuad, 0 - 999 nm
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad1WaveRad;

    /**
     * Second quadrant seas radius as defined by radWaveQuad, 0 - 999 nm
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad2WaveRad;

    /**
     * Third quadrant seas radius as defined by radWaveQuad, 0 - 999 nm
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad3WaveRad;

    /**
     * Fourth quadrant seas radius as defined by radWaveQuad, 0 - 999 nm
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float quad4WaveRad;

    /**
     * Wave height for radii defined in seas1 - seas4. 0 - 99 ft.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float radWave;

    /**
     * Storm direction in compass coordinates, 0 - 359 degrees
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float stormDrct;

    /**
     * Storm speed. 0 - 999 kts
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float stormSped;

    /**
     * Literal storm name, NONAME, or INVEST
     */
    @Column(length = 32)
    @DynamicSerializeElement
    private String stormName;

    /**
     * System depth, D=deep, M=medium, S=shallow, X=unknown
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String stormDepth;

    /**
     * Subregion code: A, B, C, E, L, P, Q, S, W
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String subRegion;

    /**
     * Forecast hour
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private int fcstHour;

    /**
     * Objective technique sorting number/Minutes for best track: 00-99
     */
    @DynamicSerializeElement
    private int techniqueNum;

    /**
     * Objective Technique or CARQ or WRNG, BEST for best Track
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String technique;

    /*
     * 20 character description of format to follow in userData
     */
    @Column(length = 20)
    @DynamicSerializeElement
    private String userDefined;

    /*
     * User data section as indicated by userDefined parameter
     */
    @Column(length = 200)
    @DynamicSerializeElement
    private String userData;

    /**
     * Maximum sustained wind speed in knots: 0 through 300
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private float windMax;

    public BaseADeckRecord() {
        this.closedP = RMISSD;
        this.radClosedP = RMISSD;
        this.eyeSize = RMISSD;
        // fcstHour not explicitly initialized
        this.gust = RMISSD;
        this.intensity = " ";
        this.maxSeas = RMISSD;
        this.maxWindRad = RMISSD;
        this.mslp = RMISSD;
        this.radWind = RMISSD;
        this.radWindQuad = " ";
        this.quad1WindRad = RMISSD;
        this.quad2WindRad = RMISSD;
        this.quad3WindRad = RMISSD;
        this.quad4WindRad = RMISSD;
        this.radWaveQuad = " ";
        this.quad1WaveRad = RMISSD;
        this.quad2WaveRad = RMISSD;
        this.quad3WaveRad = RMISSD;
        this.quad4WaveRad = RMISSD;
        this.radWave = RMISSD;
        this.stormDrct = RMISSD;
        this.stormSped = RMISSD;
        this.stormName = " ";
        this.stormDepth = " ";
        this.subRegion = " ";
        this.techniqueNum = IMISSD;
        this.technique = " ";
        this.userDefined = " ";
        this.userData = " ";
        this.windMax = RMISSD;
    }

    /**
     * A Copy constructor for convenience.
     *
     * @param rec
     *            BaseADeckRecord
     *
     * @return BaseADeckRecord
     */
    public BaseADeckRecord(BaseADeckRecord rec) {

        super(rec);

        this.setFcstHour(rec.getFcstHour());

        this.closedP = rec.getClosedP();
        this.radClosedP = rec.getRadClosedP();
        this.eyeSize = rec.getEyeSize();
        this.gust = rec.getGust();
        this.intensity = rec.getIntensity();
        this.maxSeas = rec.getMaxSeas();
        this.maxWindRad = rec.getMaxWindRad();
        this.mslp = rec.getMslp();
        this.radWind = rec.getRadWind();
        this.radWindQuad = rec.getRadWindQuad();
        this.quad1WindRad = rec.getQuad1WindRad();
        this.quad2WindRad = rec.getQuad2WindRad();
        this.quad3WindRad = rec.getQuad3WindRad();
        this.quad4WindRad = rec.getQuad4WindRad();
        this.radWaveQuad = rec.getRadWaveQuad();
        this.quad1WaveRad = rec.getQuad1WaveRad();
        this.quad2WaveRad = rec.getQuad2WaveRad();
        this.quad3WaveRad = rec.getQuad3WaveRad();
        this.quad4WaveRad = rec.getQuad4WaveRad();
        this.radWave = rec.getRadWave();
        this.stormDrct = rec.getStormDrct();
        this.stormSped = rec.getStormSped();
        this.stormName = rec.getStormName();
        this.stormDepth = rec.getStormDepth();
        this.subRegion = rec.getSubRegion();
        this.fcstHour = rec.getFcstHour();
        this.techniqueNum = rec.getTechniqueNum();
        this.technique = rec.getTechnique();
        this.userDefined = rec.getUserDefined();
        this.userData = rec.getUserData();
        this.windMax = rec.getWindMax();
    }

    public float getClosedP() {
        return closedP;
    }

    public void setClosedP(float closedP) {
        this.closedP = closedP;
    }

    public float getRadClosedP() {
        return radClosedP;
    }

    public void setRadClosedP(float radClosedP) {
        this.radClosedP = radClosedP;
    }

    public float getEyeSize() {
        return eyeSize;
    }

    public void setEyeSize(float eyeSize) {
        this.eyeSize = eyeSize;
    }

    public int getFcstHour() {
        return fcstHour;
    }

    public void setFcstHour(int fcstHour) {
        this.fcstHour = fcstHour;
    }

    public float getGust() {
        return gust;
    }

    public void setGust(float gust) {
        this.gust = gust;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public float getMaxSeas() {
        return maxSeas;
    }

    public void setMaxSeas(float maxSeas) {
        this.maxSeas = maxSeas;
    }

    public float getMaxWindRad() {
        return maxWindRad;
    }

    public void setMaxWindRad(float maxWindRad) {
        this.maxWindRad = maxWindRad;
    }

    public float getMslp() {
        return mslp;
    }

    public void setMslp(float mslp) {
        this.mslp = mslp;
    }

    public float getRadWind() {
        return radWind;
    }

    public void setRadWind(float radWind) {
        this.radWind = radWind;
    }

    public String getRadWindQuad() {
        return radWindQuad;
    }

    public void setRadWindQuad(String radWindQuad) {
        this.radWindQuad = radWindQuad;
    }

    public float getQuad1WindRad() {
        return quad1WindRad;
    }

    public void setQuad1WindRad(float quad1WindRad) {
        this.quad1WindRad = quad1WindRad;
    }

    public float getQuad2WindRad() {
        return quad2WindRad;
    }

    public void setQuad2WindRad(float quad2WindRad) {
        this.quad2WindRad = quad2WindRad;
    }

    public float getQuad3WindRad() {
        return quad3WindRad;
    }

    public void setQuad3WindRad(float quad3WindRad) {
        this.quad3WindRad = quad3WindRad;
    }

    public float getQuad4WindRad() {
        return quad4WindRad;
    }

    public void setQuad4WindRad(float quad4WindRad) {
        this.quad4WindRad = quad4WindRad;
    }

    public String getRadWaveQuad() {
        return radWaveQuad;
    }

    public void setRadWaveQuad(String radWaveQuad) {
        this.radWaveQuad = radWaveQuad;
    }

    public float getQuad1WaveRad() {
        return quad1WaveRad;
    }

    public void setQuad1WaveRad(float quad1WaveRad) {
        this.quad1WaveRad = quad1WaveRad;
    }

    public float getQuad2WaveRad() {
        return quad2WaveRad;
    }

    public void setQuad2WaveRad(float quad2WaveRad) {
        this.quad2WaveRad = quad2WaveRad;
    }

    public float getQuad3WaveRad() {
        return quad3WaveRad;
    }

    public void setQuad3WaveRad(float quad3WaveRad) {
        this.quad3WaveRad = quad3WaveRad;
    }

    public float getQuad4WaveRad() {
        return quad4WaveRad;
    }

    public void setQuad4WaveRad(float quad4WaveRad) {
        this.quad4WaveRad = quad4WaveRad;
    }

    public float getRadWave() {
        return radWave;
    }

    public void setRadWave(float radWave) {
        this.radWave = radWave;
    }

    public float getStormDrct() {
        return stormDrct;
    }

    public void setStormDrct(float stormDrct) {
        this.stormDrct = stormDrct;
    }

    public float getStormSped() {
        return stormSped;
    }

    public void setStormSped(float stormSped) {
        this.stormSped = stormSped;
    }

    @Override
    public String getStormName() {
        return stormName;
    }

    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

    public String getStormDepth() {
        return stormDepth;
    }

    public void setStormDepth(String stormDepth) {
        this.stormDepth = stormDepth;
    }

    @Override
    public String getSubRegion() {
        return subRegion;
    }

    public void setSubRegion(String subRegion) {
        this.subRegion = subRegion;
    }

    public int getTechniqueNum() {
        return techniqueNum;
    }

    public void setTechniqueNum(int techniqueNum) {
        this.techniqueNum = techniqueNum;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public String getUserDefined() {
        return userDefined;
    }

    public void setUserDefined(String userDefined) {
        this.userDefined = userDefined;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public float getWindMax() {
        return windMax;
    }

    public void setWindMax(float windMax) {
        this.windMax = windMax;
    }

    /**
     * Constructs a CSV string representation of the data in the ADeckRecord
     * compatible with ATCF "deck" files. Sample B-deck line (wrapped):
     *
     * AL, 01, 2015050912, , BEST, 0, 325N, 778W, 50, 1001, TS, 50, NEQ, 50, 50,
     * 0, 0, 1016, 150, 40, 60, 0, L, 0, , 0, 0, ANA, M, 12, NEQ, 180, 120, 90,
     * 120, genesis-num, 001,
     */
    public String toADeckString() {
        final String sep = ", ";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%2s", getBasin()));
        sb.append(sep);
        // use leading zero
        sb.append(String.format("%02d", getCycloneNum()));
        sb.append(sep);
        sb.append(DtgUtil.format(getRefTime()));
        sb.append(sep);
        sb.append(techniqueNum == IMISSD ? "  "
                : String.format("%02d", techniqueNum));
        sb.append(sep);
        sb.append(String.format("%4s", technique));
        sb.append(sep);
        sb.append(String.format("%3s", getFcstHour()));
        sb.append(sep);
        float clat = getClat();
        float clon = getClon();
        // 26.800001 -> "268N"
        sb.append(String.format("%3s", Math.round(Math.abs(clat) * 10)));
        sb.append(clat >= 0.0f ? "N" : "S");
        sb.append(sep);
        // -79.200005 -> "792W"
        int clonInt = Math.round(Math.abs(clon) * 10);
        sb.append(String.format("%4s", clonInt));
        sb.append((clonInt != 0 && clon > 0)
                || (clonInt == 0 && isEastBiasedTechnique(technique)) ? "E"
                        : "W");
        sb.append(sep);
        // 25.0 -> " 25"
        sb.append(String.format("%3s", Math.round(windMax)));
        sb.append(sep);
        // 992.0 -> " 992"
        sb.append(String.format("%4s", Math.round(mslp)));
        sb.append(sep);
        sb.append(String.format("%2s", intensity));
        sb.append(sep);
        // 0.0 -> " 0"
        sb.append(String.format("%3s", Math.round(radWind)));
        sb.append(sep);
        sb.append(String.format("%3s", radWindQuad));
        sb.append(sep);
        // 0.0 -> " 0" for all quadrant wind intensities
        sb.append(String.format("%4s", Math.round(quad1WindRad)));
        sb.append(sep);
        sb.append(String.format("%4s", Math.round(quad2WindRad)));
        sb.append(sep);
        sb.append(String.format("%4s", Math.round(quad3WindRad)));
        sb.append(sep);
        sb.append(String.format("%4s", Math.round(quad4WindRad)));
        sb.append(sep);
        if (closedP != RMISSD) {
            // 998.0 -> " 998"
            sb.append(String.format("%4s", Math.round(closedP)));
            sb.append(sep);
            sb.append(String.format("%4s", Math.round(radClosedP)));
            sb.append(sep);
            sb.append(String.format("%3s", Math.round(maxWindRad)));
            sb.append(sep);
            sb.append(String.format("%3s", Math.round(gust)));
            sb.append(sep);
            sb.append(String.format("%3s", Math.round(eyeSize)));
            sb.append(sep);
            if (maxSeas != RMISSD) {
                sb.append(String.format("%3s", subRegion));
                sb.append(sep);
                sb.append(String.format("%3s", Math.round(maxSeas)));
                sb.append(sep);
                sb.append(String.format("%3s", getForecaster()));
                sb.append(sep);
                sb.append(String.format("%3s", Math.round(stormDrct)));
                sb.append(sep);
                sb.append(String.format("%3s", Math.round(stormSped)));
                sb.append(sep);
                if (stormName.trim().isEmpty() && stormDepth.trim().isEmpty()
                        && radWave == RMISSD && userDefined.trim().isEmpty()) {
                    return sb.toString();
                }
                sb.append(String.format("%10s", stormName));
                sb.append(sep);
                sb.append(String.format("%1s", stormDepth));
                sb.append(sep);
                if ((int) radWave != RMISSD) {
                    sb.append(String.format("%2s", Math.round(radWave)));
                    sb.append(sep);
                    sb.append(String.format("%3s", radWaveQuad));
                    sb.append(sep);
                    sb.append(String.format("%4s", Math.round(quad1WaveRad)));
                    sb.append(sep);
                    sb.append(String.format("%4s", Math.round(quad2WaveRad)));
                    sb.append(sep);
                    sb.append(String.format("%4s", Math.round(quad3WaveRad)));
                    sb.append(sep);
                    sb.append(String.format("%4s", Math.round(quad4WaveRad)));
                    sb.append(sep);
                    boolean hasUserData = !userData.trim().isEmpty();
                    if (!userDefined.trim().isEmpty() || hasUserData) {
                        sb.append(userDefined);
                        sb.append(sep);
                    }
                    if (hasUserData) {
                        sb.append(userData);
                        sb.append(sep);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Sort records by several fields to make .dat deck file output match format
     * of Legacy ATCF deck files
     *
     * @param aDeckRecs
     */
    public static void sortADeck(List<BaseADeckRecord> aDeckRecs) {
        if (!aDeckRecs.isEmpty()) {
            Comparator<BaseADeckRecord> refTimeCmp = Comparator
                    .comparing(BaseADeckRecord::getRefTime);
            Comparator<BaseADeckRecord> techNumCmp = Comparator
                    .comparing(BaseADeckRecord::getTechniqueNum);
            Comparator<BaseADeckRecord> techCmp = Comparator
                    .comparing(BaseADeckRecord::getTechnique);
            Comparator<BaseADeckRecord> techLenCmp = new Comparator<BaseADeckRecord>() {
                @Override
                public int compare(final BaseADeckRecord s1,
                        final BaseADeckRecord s2) {
                    return (Integer.compare(s1.getTechnique().length(),
                            s2.getTechnique().length()));
                }
            };
            Comparator<BaseADeckRecord> fcstTimeCmp = Comparator
                    .comparing(BaseADeckRecord::getFcstHour);
            Comparator<BaseADeckRecord> radWndCmp = Comparator
                    .comparing(BaseADeckRecord::getRadWind);

            Collections.sort(aDeckRecs, refTimeCmp.thenComparing(techNumCmp)
                    .thenComparing(techLenCmp).thenComparing(techCmp)
                    .thenComparing(fcstTimeCmp).thenComparing(radWndCmp));
        }
    }

    /**
     * Convenience function to get all four wind radii.
     *
     * @return int[4] wind radii
     */
    public int[] getWindRadii() {
        int[] windRadii = new int[] { 0, 0, 0, 0 };
        windRadii[0] = (quad1WindRad == AbstractAtcfRecord.RMISSD) ? 0
                : (int) quad1WindRad;
        windRadii[1] = (quad2WindRad == AbstractAtcfRecord.RMISSD) ? 0
                : (int) quad2WindRad;
        windRadii[2] = (quad3WindRad == AbstractAtcfRecord.RMISSD) ? 0
                : (int) quad3WindRad;
        windRadii[3] = (quad4WindRad == AbstractAtcfRecord.RMISSD) ? 0
                : (int) quad4WindRad;

        return windRadii;
    }

    /**
     * Convenience function to get all four seas radii.
     *
     * @return int[4] 12 ft seas radii
     */
    public int[] getWaveRadii() {
        int[] waveRadii = new int[] { 0, 0, 0, 0 };
        if ((int) radWave == 12) {
            waveRadii[0] = (quad1WaveRad == AbstractAtcfRecord.RMISSD) ? 0
                    : (int) quad1WaveRad;
            waveRadii[1] = (quad2WaveRad == AbstractAtcfRecord.RMISSD) ? 0
                    : (int) quad2WaveRad;
            waveRadii[2] = (quad3WaveRad == AbstractAtcfRecord.RMISSD) ? 0
                    : (int) quad3WaveRad;
            waveRadii[3] = (quad4WaveRad == AbstractAtcfRecord.RMISSD) ? 0
                    : (int) quad4WaveRad;
        }

        return waveRadii;
    }

    /**
     * @return reference time advanced by the forecast hour
     */
    public Date getForecastDateTime() {
        Date refTime = getRefTime();
        return refTime != null
                ? new Date(
                        refTime.getTime() + getFcstHour() * TimeUtil.MILLIS_PER_HOUR)
                : null;
    }

    /**
     * Returns true if zero longitude should be written with "E" instead of "W"
     * for the given technique name.
     *
     * @param technique
     * @return
     */
    private static boolean isEastBiasedTechnique(String technique) {
        if (technique.length() == 4) {
            char c = technique.charAt(0);
            if (c == 'D' || c == 'S') {
                switch (technique) {
                case "DSHP":
                case "DSPE":
                case "SHIP":
                case "SHPE":
                    return true;
                default:
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getUniqueId() {
        Map<String, Object> uid = getTrackUniqueId();
        uid.put("technique", getTechnique());
        uid.put("radWind", getRadWind());
        return uid;
    }

}
