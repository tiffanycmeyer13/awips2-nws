/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * GenesisBaseBDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 3, 2020 # 77134     pwang       Initial creation
 * Jul 6, 2020 # 79696     pwang       Add a special copy constructor
 * Aug 18,2020   79571     wpaintsil   Convert to BDeckRecord method
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@MappedSuperclass
public class GenesisBaseBDeckRecord extends AbstractGenesisDeckRecord {

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

    public GenesisBaseBDeckRecord() {
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
     * Special copy constructor
     *
     * @param rec
     */
    public GenesisBaseBDeckRecord(BaseBDeckRecord rec) {

        super(rec);

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
        sb.append(String.format("%02d", getGenesisNum()));
        sb.append(sep);
        Calendar warnTime = getRefTimeAsCalendar();
        sb.append(warnTime.get(Calendar.YEAR));
        sb.append(String.format("%02d", warnTime.get(Calendar.MONTH) + 1));
        sb.append(String.format("%02d", warnTime.get(Calendar.DAY_OF_MONTH)));
        sb.append(String.format("%02d", warnTime.get(Calendar.HOUR_OF_DAY)));
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
        sb.append(String.format("%3s", (int) (Math.abs(clat) * 10)));
        sb.append(clat >= 0.0f ? "N" : "S");
        sb.append(sep);
        // -79.200005 -> " 792W"
        sb.append(String.format("%4s", (int) (Math.abs(clon) * 10)));
        sb.append(((int) (clon * 10)) > 0 || (((int) (clon * 10)) == 0
                && (technique.equals("DSHP") || technique.equals("SHIP"))) ? "E"
                        : "W");
        sb.append(sep);
        // 25.0 -> " 25"
        sb.append(String.format("%3s", (int) windMax));
        sb.append(sep);
        // 992.0 -> " 992"
        sb.append(String.format("%4s", (int) mslp));
        sb.append(sep);
        sb.append(String.format("%2s", intensity));
        sb.append(sep);
        // 0.0 -> " 0"
        sb.append(String.format("%3s", (int) radWind));
        sb.append(sep);
        sb.append(String.format("%3s", radWindQuad));
        sb.append(sep);
        // 0.0 -> " 0" for all quadrant wind intensities
        sb.append(String.format("%4s", (int) quad1WindRad));
        sb.append(sep);
        sb.append(String.format("%4s", (int) quad2WindRad));
        sb.append(sep);
        sb.append(String.format("%4s", (int) quad3WindRad));
        sb.append(sep);
        sb.append(String.format("%4s", (int) quad4WindRad));
        sb.append(sep);
        if (closedP != RMISSD) {
            // 998.0 -> " 998"
            sb.append(String.format("%4s", (int) closedP));
            sb.append(sep);
            sb.append(String.format("%4s", (int) radClosedP));
            sb.append(sep);
            sb.append(String.format("%3s", (int) maxWindRad));
            sb.append(sep);
            sb.append(String.format("%3s", (int) gust));
            sb.append(sep);
            sb.append(String.format("%3s", (int) eyeSize));
            sb.append(sep);
            if (maxSeas != RMISSD) {
                sb.append(String.format("%3s", subRegion));
                sb.append(sep);
                sb.append(String.format("%3s", (int) maxSeas));
                sb.append(sep);
                sb.append(String.format("%3s", getForecaster()));
                sb.append(sep);
                sb.append(String.format("%3s", (int) stormDrct));
                sb.append(sep);
                sb.append(String.format("%3s", (int) stormSped));
                sb.append(sep);
                sb.append(String.format("%10s", stormName));
                sb.append(sep);
                sb.append(String.format("%1s", stormDepth));
                sb.append(sep);
                if ((int) radWave != RMISSD) {
                    sb.append(String.format("%2s", (int) radWave));
                    sb.append(sep);
                    sb.append(String.format("%3s", radWaveQuad));
                    sb.append(sep);
                    sb.append(String.format("%4s", (int) quad1WaveRad));
                    sb.append(sep);
                    sb.append(String.format("%4s", (int) quad2WaveRad));
                    sb.append(sep);
                    sb.append(String.format("%4s", (int) quad3WaveRad));
                    sb.append(sep);
                    sb.append(String.format("%4s", (int) quad4WaveRad));
                    sb.append(sep);
                    if (!userDefined.isEmpty()) {
                        sb.append(userDefined);
                        sb.append(sep);
                        sb.append(userData);
                        sb.append(sep);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Convert a GenesisBDeckRecord to the BDeckRecord equivalent.
     *
     * @return BDeckRecord
     */
    public BDeckRecord toBDeckRecord() {
        BDeckRecord bDeckRecord = new BDeckRecord();

        bDeckRecord.setReportType(reportType);
        bDeckRecord.setBasin(basin);
        bDeckRecord.setClat(clat);
        bDeckRecord.setClon(clon);
        bDeckRecord.setForecaster(forecaster);

        bDeckRecord.setId(id);
        bDeckRecord.setRefTime(new Date(refTime.getTime()));
        bDeckRecord.setClosedP(closedP);
        bDeckRecord.setRadClosedP(radClosedP);
        bDeckRecord.setEyeSize(eyeSize);

        bDeckRecord.setGust(gust);
        bDeckRecord.setIntensity(intensity);
        bDeckRecord.setMaxSeas(maxSeas);
        bDeckRecord.setMaxWindRad(maxWindRad);
        bDeckRecord.setMslp(mslp);
        bDeckRecord.setRadWind(radWind);
        bDeckRecord.setRadWindQuad(radWindQuad);
        bDeckRecord.setQuad1WindRad(quad1WindRad);
        bDeckRecord.setQuad2WindRad(quad2WindRad);
        bDeckRecord.setQuad3WindRad(quad3WindRad);
        bDeckRecord.setQuad4WindRad(quad4WindRad);
        bDeckRecord.setRadWaveQuad(radWaveQuad);
        bDeckRecord.setQuad1WaveRad(quad1WaveRad);
        bDeckRecord.setQuad2WaveRad(quad2WaveRad);
        bDeckRecord.setQuad3WaveRad(quad3WaveRad);
        bDeckRecord.setQuad4WaveRad(quad4WaveRad);
        bDeckRecord.setRadWave(radWave);
        bDeckRecord.setStormDrct(stormDrct);
        bDeckRecord.setStormSped(stormSped);
        bDeckRecord.setStormName(stormName);
        bDeckRecord.setStormDepth(stormDepth);
        bDeckRecord.setSubRegion(subRegion);
        bDeckRecord.setFcstHour(fcstHour);
        bDeckRecord.setTechniqueNum(techniqueNum);
        bDeckRecord.setTechnique(technique);
        bDeckRecord.setUserDefined(userDefined);
        bDeckRecord.setUserData(userData);
        bDeckRecord.setWindMax(windMax);

        return bDeckRecord;
    }

    @Override
    public Map<String, Object> getUniqueId() {
        Map<String, Object> uid = getTrackUniqueId();
        uid.put("radWind", getRadWind());
        return uid;
    }

}
