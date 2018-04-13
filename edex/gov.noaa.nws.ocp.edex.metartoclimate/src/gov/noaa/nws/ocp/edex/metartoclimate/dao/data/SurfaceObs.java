/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR surface observations data. From dbtypedefs.h.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 FEB 2017  28609      amoore      Initial creation
 * 22 FEB 2017  28609      amoore      Address unused field TODOs.
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class SurfaceObs {
    /**
     * Location indicator (ICAO)
     */
    private String obsLocID;

    /**
     * name of the station or location
     */
    private String locName;

    /**
     * station priority ranking (1=most,6=least)
     */
    private int priority;

    /**
     * WMO data designator T1T2A1A2ii
     */
    private String wmoDD;

    /**
     * AFOS data designator NNNxxx
     */
    private String afosDD;

    /**
     * observation time
     */
    private long obsTime;

    /**
     * The nominal time of the observation
     */
    private long nominalTime;

    /**
     * latitude (decimal degrees)
     */
    private float lat;

    /**
     * longitude (decimal degrees)
     */
    private float lon;

    /**
     * elevation (meters)
     */
    private float elevation;

    /**
     * state or sub-region station is in
     */
    private String state;

    /** country or region */
    private String region;

    /**
     * QC Metar values. Not originally a part of this structure in Legacy, but
     * makes sense to place it in here due to strong association. From
     * QC_METAR.h.
     */
    private QCMetar qcMetar;

    /**
     * Flag for status from the decoder 0 = successful, 1 = undefined token, 2 =
     * syntax error in token
     */
    private int decodeStatus;

    /**
     * Is this report a SPECIAL issuance? 0 = No, 1 = Yes, MAX_INT = Cannot be
     * be determined.
     */
    private int speciFlag;

    /**
     * automatic/manned station flag. Uses 1 for A01 station, 2 for A02 station,
     * and 0 for manned station.
     */
    private int autoFlag;

    /**
     * Corrected/automated report flag. Uses 2 for an automated report, 1 for a
     * correction, 0 for non_corrected report.
     */
    private int corFlag;

    /** height of lowest cloud layer in 100's ft */
    private float lowCloudHeight;

    /**
     * fraction of sky obscured by low clouds
     */
    private float lowCloudCover;

    /**
     * type of low cloud; values from WMO code table 0513 are used. For METAR
     * reports, Cumulonimbus (CB) = 3 and Towering Cumulus (TCU) = 2.
     */
    private int lowCloudType;

    /**
     * height of middle cloud layer in 100's ft
     */
    private float midCloudHeight;

    /**
     * fraction of sky obscured by mid clouds
     */
    private float midCloudCover;

    /**
     * type of middle clouds; values from WMO code table 0515 are used. Values
     * from METAR format do not apply
     */
    private int midCloudType;

    /**
     * height of highest cloud layer in 100's ft
     */
    private float highCloudHeight;

    /**
     * fraction of sky obscured by high clouds
     */
    private float highCloudCover;

    /**
     * type of high clouds; values from WMO code table 0509 are used. Values
     * from METAR format do not apply.
     */
    private int highCloudType;

    /**
     * height of the 4th cloud layer in 100's ft
     */
    private float layer4CloudHeight;

    /**
     * fraction of sky obscured by 4th layer
     */
    private float layer4CloudCover;

    /**
     * type of clouds in the 4th layer; values from WMO codes table 0509 are
     * used. Values from METAR format do not apply.
     */
    private int layer4CloudType;

    /**
     * height of the 5th cloud layer in 100's ft
     */
    private float layer5CloudHeight;

    /**
     * fraction of sky obscured by 5th layer
     */
    private float layer5CloudCover;

    /**
     * type of clouds in the 5th layer; values from WMO codes table 0509 are
     * used. Values from METAR format do not apply.
     */
    private int layer5CloudType;

    /**
     * height of the 6th cloud layer in 100's ft
     */
    private float layer6CloudHeight;

    /**
     * fraction of sky obscured by 6th layer
     */
    private float layer6CloudCover;

    /**
     * type of clouds in the 6th layer; values from WMO codes table 0509 are
     * used. Values from METAR format do not apply.
     */
    private int layer6CloudType;

    /**
     * flag value for synoptic reports. This is needed in order to know which
     * table to lookup in for the past and present weather. A value of 4 you use
     * Tables 4-12 and 4-14 for present and past whether respectively. A value
     * of 7 use Tables 4-13 and 4-15.This is not needed for METAR.
     */
    private int wxflag;

    /**
     * past weather: Synoptic reports use integer values from WMO code table
     * 4531 for automated stations and 4561 for manned stations. METAR reports
     * use text keywords from WMO code table 4678.
     */
    private String[] pastWx = new String[3];

    /**
     * present (current) weather: Synoptic reports use integer values from WMO
     * code table 4680 for automatic stations and 4677 for manned stations.
     * METAR reports use text keywords from WMO code table 4678.
     */
    private String[] presentWx = new String[3];

    /**
     * horizontal visibility at the surface (mi) Use min value if vsby varies by
     * direction
     */
    private float visibility;

    /**
     * vertical visibility. Reported by METAR, but not synoptic. Stored im
     * meters.
     */
    private float verticalVisibility;

    /** altimeter setting in inches of Hg */
    private float altSetting;

    /** sea level pressure in mb */
    private float slp;

    /** pressure change over last 3 hours in mb */
    private float pressureChange3hr;

    /**
     * pressure tendency over last 3 hours. Uses values 0-8 from WMO code table
     * 0200
     */
    private int pressureTendency;

    /** temperature (degrees C) */
    private float temp;

    /** dew poprivate int temperature (degrees C) */
    private float dewPt;

    /** temperature to nearest tenth of degree C */
    private float temp2Tenths;

    /**
     * dew poprivate int to nearest tenth of degree C
     */
    private float dewPt2Tenths;

    /** maximum temperature over last 6 hours */
    private float maxTemp6hr;

    /** minimum temperature over last 6 hours */
    private float minTemp6hr;

    /**
     * Flag indicating if the precipitation sensor was operational at the time
     * of this observation. (0=No;1=Yes) All precipitation in inches
     */
    private int precipPresent;

    /** precipitation accumulated over last hour */
    private float precip1hr;

    /** precipitation accum'd over last 6 hours */
    private float precip6hr;

    /** precipitation accum'd over last 24 hours */
    private float precip24hr;

    /** depth of snow in inches */
    private float snowDepth;

    /** wind direction in degrees true */
    private float windDir;

    /** wind speed in knots */
    private float windSpd;

    /** wind gust in knots */
    private float gustSpd;

    /** The WMO header time. */
    private long originTime;

    /** The peak wind speed. */
    private float peakWindSpeed;

    /** The peak wind direction. */
    private float peakWindDir;

    /**
     * The time of the peak wind (HHmm where HH is the hour, mm is the minute).
     */
    private int peakWindHHMM;

    /** The total number of minutes of sunshine. */
    private int sunshineDur;

    /**
     * The 24 hour maximum air temperature observed at 0000 LST.
     */
    private float max24temp;

    /**
     * The 24 hour minimum air temperature observed at 0000 LST.
     */
    private float min24temp;

    /**
     * Indicates whether or not the wind was variable (for the case where the
     * wind speed was 6 knots or less). (0 = No; 1 = Yes)
     */
    private int variableWindFlag;

    /**
     * Was a tornado descriptor present in the remarks section of the current
     * METAR? 0 = Not Reported, 1 = Tornado, 2 = Funnel Cloud, 3 = Water Spout
     */
    private int tornadic;

    /**
     * Array containing the starting and ending times of precipitation as
     * specified in the recent weather group in the REMARKS section of a METAR.
     */
    private RecentWx[] weatherBeginEnd = new RecentWx[DecodedMetar.NUM_REWX];

    /**
     * Aurora borealis flag: 1 if present, 0 if not.
     */
    private int auroraBorealisFlag;

    /** reserved space for future expansion. */
    private String futureExpansion;

    /**
     * Empty constructor.
     */
    public SurfaceObs() {
    }

    /**
     * @return the obsLocID
     */
    public String getObsLocID() {
        return obsLocID;
    }

    /**
     * @param obsLocID
     *            the obsLocID to set
     */
    public void setObsLocID(String obsLocID) {
        this.obsLocID = obsLocID;
    }

    /**
     * Task 29492: unused
     * 
     * @return the locName
     */
    public String getLocName() {
        return locName;
    }

    /**
     * @param locName
     *            the locName to set
     */
    public void setLocName(String locName) {
        this.locName = locName;
    }

    /**
     * Task 29492: unused
     * 
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the wmoDD
     */
    public String getWmoDD() {
        return wmoDD;
    }

    /**
     * @param wmoDD
     *            the wmoDD to set
     */
    public void setWmoDD(String wmoDD) {
        this.wmoDD = wmoDD;
    }

    /**
     * @return the afosDD
     */
    public String getAfosDD() {
        return afosDD;
    }

    /**
     * @param afosDD
     *            the afosDD to set
     */
    public void setAfosDD(String afosDD) {
        this.afosDD = afosDD;
    }

    /**
     * @return the obsTime
     */
    public long getObsTime() {
        return obsTime;
    }

    /**
     * @param obsTime
     *            the obsTime to set
     */
    public void setObsTime(long obsTime) {
        this.obsTime = obsTime;
    }

    /**
     * @return the nominalTime
     */
    public long getNominalTime() {
        return nominalTime;
    }

    /**
     * @param nominalTime
     *            the nominalTime to set
     */
    public void setNominalTime(long nominalTime) {
        this.nominalTime = nominalTime;
    }

    /**
     * Task 29492: unused
     * 
     * @return the lat
     */
    public float getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(float lat) {
        this.lat = lat;
    }

    /**
     * Task 29492: unused
     * 
     * @return the lon
     */
    public float getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(float lon) {
        this.lon = lon;
    }

    /**
     * Task 29492: unused
     * 
     * @return the elevation
     */
    public float getElevation() {
        return elevation;
    }

    /**
     * @param elevation
     *            the elevation to set
     */
    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    /**
     * Task 29492: unused
     * 
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Task 29492: unused
     * 
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region
     *            the region to set
     */
    public void setRegion(String region) {
        this.region = region;
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
     * @return the speciFlag
     */
    public int getSpeciFlag() {
        return speciFlag;
    }

    /**
     * @param speciFlag
     *            the speciFlag to set
     */
    public void setSpeciFlag(int speciFlag) {
        this.speciFlag = speciFlag;
    }

    /**
     * @return the autoFlag
     */
    public int getAutoFlag() {
        return autoFlag;
    }

    /**
     * @param autoFlag
     *            the autoFlag to set
     */
    public void setAutoFlag(int autoFlag) {
        this.autoFlag = autoFlag;
    }

    /**
     * @return the corFlag
     */
    public int getCorFlag() {
        return corFlag;
    }

    /**
     * @param corFlag
     *            the corFlag to set
     */
    public void setCorFlag(int corFlag) {
        this.corFlag = corFlag;
    }

    /**
     * @return the lowCloudHeight
     */
    public float getLowCloudHeight() {
        return lowCloudHeight;
    }

    /**
     * @param lowCloudHeight
     *            the lowCloudHeight to set
     */
    public void setLowCloudHeight(float lowCloudHeight) {
        this.lowCloudHeight = lowCloudHeight;
    }

    /**
     * @return the lowCloudCover
     */
    public float getLowCloudCover() {
        return lowCloudCover;
    }

    /**
     * @param lowCloudCover
     *            the lowCloudCover to set
     */
    public void setLowCloudCover(float lowCloudCover) {
        this.lowCloudCover = lowCloudCover;
    }

    /**
     * @return the lowCloudType
     */
    public int getLowCloudType() {
        return lowCloudType;
    }

    /**
     * @param lowCloudType
     *            the lowCloudType to set
     */
    public void setLowCloudType(int lowCloudType) {
        this.lowCloudType = lowCloudType;
    }

    /**
     * @return the midCloudHeight
     */
    public float getMidCloudHeight() {
        return midCloudHeight;
    }

    /**
     * @param midCloudHeight
     *            the midCloudHeight to set
     */
    public void setMidCloudHeight(float midCloudHeight) {
        this.midCloudHeight = midCloudHeight;
    }

    /**
     * @return the midCloudCover
     */
    public float getMidCloudCover() {
        return midCloudCover;
    }

    /**
     * @param midCloudCover
     *            the midCloudCover to set
     */
    public void setMidCloudCover(float midCloudCover) {
        this.midCloudCover = midCloudCover;
    }

    /**
     * @return the midCloudType
     */
    public int getMidCloudType() {
        return midCloudType;
    }

    /**
     * @param midCloudType
     *            the midCloudType to set
     */
    public void setMidCloudType(int midCloudType) {
        this.midCloudType = midCloudType;
    }

    /**
     * @return the highCloudHeight
     */
    public float getHighCloudHeight() {
        return highCloudHeight;
    }

    /**
     * @param highCloudHeight
     *            the highCloudHeight to set
     */
    public void setHighCloudHeight(float highCloudHeight) {
        this.highCloudHeight = highCloudHeight;
    }

    /**
     * @return the highCloudCover
     */
    public float getHighCloudCover() {
        return highCloudCover;
    }

    /**
     * @param highCloudCover
     *            the highCloudCover to set
     */
    public void setHighCloudCover(float highCloudCover) {
        this.highCloudCover = highCloudCover;
    }

    /**
     * @return the highCloudType
     */
    public int getHighCloudType() {
        return highCloudType;
    }

    /**
     * @param highCloudType
     *            the highCloudType to set
     */
    public void setHighCloudType(int highCloudType) {
        this.highCloudType = highCloudType;
    }

    /**
     * @return the layer4CloudHeight
     */
    public float getLayer4CloudHeight() {
        return layer4CloudHeight;
    }

    /**
     * @param layer4CloudHeight
     *            the layer4CloudHeight to set
     */
    public void setLayer4CloudHeight(float layer4CloudHeight) {
        this.layer4CloudHeight = layer4CloudHeight;
    }

    /**
     * @return the layer4CloudCover
     */
    public float getLayer4CloudCover() {
        return layer4CloudCover;
    }

    /**
     * @param layer4CloudCover
     *            the layer4CloudCover to set
     */
    public void setLayer4CloudCover(float layer4CloudCover) {
        this.layer4CloudCover = layer4CloudCover;
    }

    /**
     * @return the layer4CloudType
     */
    public int getLayer4CloudType() {
        return layer4CloudType;
    }

    /**
     * @param layer4CloudType
     *            the layer4CloudType to set
     */
    public void setLayer4CloudType(int layer4CloudType) {
        this.layer4CloudType = layer4CloudType;
    }

    /**
     * @return the layer5CloudHeight
     */
    public float getLayer5CloudHeight() {
        return layer5CloudHeight;
    }

    /**
     * @param layer5CloudHeight
     *            the layer5CloudHeight to set
     */
    public void setLayer5CloudHeight(float layer5CloudHeight) {
        this.layer5CloudHeight = layer5CloudHeight;
    }

    /**
     * @return the layer5CloudCover
     */
    public float getLayer5CloudCover() {
        return layer5CloudCover;
    }

    /**
     * @param layer5CloudCover
     *            the layer5CloudCover to set
     */
    public void setLayer5CloudCover(float layer5CloudCover) {
        this.layer5CloudCover = layer5CloudCover;
    }

    /**
     * @return the layer5CloudType
     */
    public int getLayer5CloudType() {
        return layer5CloudType;
    }

    /**
     * @param layer5CloudType
     *            the layer5CloudType to set
     */
    public void setLayer5CloudType(int layer5CloudType) {
        this.layer5CloudType = layer5CloudType;
    }

    /**
     * @return the layer6CloudHeight
     */
    public float getLayer6CloudHeight() {
        return layer6CloudHeight;
    }

    /**
     * @param layer6CloudHeight
     *            the layer6CloudHeight to set
     */
    public void setLayer6CloudHeight(float layer6CloudHeight) {
        this.layer6CloudHeight = layer6CloudHeight;
    }

    /**
     * @return the layer6CloudCover
     */
    public float getLayer6CloudCover() {
        return layer6CloudCover;
    }

    /**
     * @param layer6CloudCover
     *            the layer6CloudCover to set
     */
    public void setLayer6CloudCover(float layer6CloudCover) {
        this.layer6CloudCover = layer6CloudCover;
    }

    /**
     * @return the layer6CloudType
     */
    public int getLayer6CloudType() {
        return layer6CloudType;
    }

    /**
     * @param layer6CloudType
     *            the layer6CloudType to set
     */
    public void setLayer6CloudType(int layer6CloudType) {
        this.layer6CloudType = layer6CloudType;
    }

    /**
     * @return the wxflag
     */
    public int getWxflag() {
        return wxflag;
    }

    /**
     * @param wxflag
     *            the wxflag to set
     */
    public void setWxflag(int wxflag) {
        this.wxflag = wxflag;
    }

    /**
     * @return the pastWx
     */
    public String[] getPastWx() {
        return pastWx;
    }

    /**
     * @param pastWx
     *            the pastWx to set
     */
    public void setPastWx(String[] pastWx) {
        this.pastWx = pastWx;
    }

    /**
     * @return the presentWx
     */
    public String[] getPresentWx() {
        return presentWx;
    }

    /**
     * @param presentWx
     *            the presentWx to set
     */
    public void setPresentWx(String[] presentWx) {
        this.presentWx = presentWx;
    }

    /**
     * @return the visibility
     */
    public float getVisibility() {
        return visibility;
    }

    /**
     * @param visibility
     *            the visibility to set
     */
    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }

    /**
     * @return the verticalVisibility
     */
    public float getVerticalVisibility() {
        return verticalVisibility;
    }

    /**
     * @param verticalVisibility
     *            the verticalVisibility to set
     */
    public void setVerticalVisibility(float verticalVisibility) {
        this.verticalVisibility = verticalVisibility;
    }

    /**
     * @return the altSetting
     */
    public float getAltSetting() {
        return altSetting;
    }

    /**
     * @param altSetting
     *            the altSetting to set
     */
    public void setAltSetting(float altSetting) {
        this.altSetting = altSetting;
    }

    /**
     * @return the slp
     */
    public float getSlp() {
        return slp;
    }

    /**
     * @param slp
     *            the slp to set
     */
    public void setSlp(float slp) {
        this.slp = slp;
    }

    /**
     * @return the pressureChange3hr
     */
    public float getPressureChange3hr() {
        return pressureChange3hr;
    }

    /**
     * @param pressureChange3hr
     *            the pressureChange3hr to set
     */
    public void setPressureChange3hr(float pressureChange3hr) {
        this.pressureChange3hr = pressureChange3hr;
    }

    /**
     * @return the pressureTendency
     */
    public int getPressureTendency() {
        return pressureTendency;
    }

    /**
     * @param pressureTendency
     *            the pressureTendency to set
     */
    public void setPressureTendency(int pressureTendency) {
        this.pressureTendency = pressureTendency;
    }

    /**
     * @return the temp
     */
    public float getTemp() {
        return temp;
    }

    /**
     * @param temp
     *            the temp to set
     */
    public void setTemp(float temp) {
        this.temp = temp;
    }

    /**
     * @return the dewPt
     */
    public float getDewPt() {
        return dewPt;
    }

    /**
     * @param dewPt
     *            the dewPt to set
     */
    public void setDewPt(float dewPt) {
        this.dewPt = dewPt;
    }

    /**
     * @return the temp2Tenths
     */
    public float getTemp2Tenths() {
        return temp2Tenths;
    }

    /**
     * @param temp2Tenths
     *            the temp2Tenths to set
     */
    public void setTemp2Tenths(float temp2Tenths) {
        this.temp2Tenths = temp2Tenths;
    }

    /**
     * @return the dewPt2Tenths
     */
    public float getDewPt2Tenths() {
        return dewPt2Tenths;
    }

    /**
     * @param dewPt2Tenths
     *            the dewPt2Tenths to set
     */
    public void setDewPt2Tenths(float dewPt2Tenths) {
        this.dewPt2Tenths = dewPt2Tenths;
    }

    /**
     * @return the maxTemp6hr
     */
    public float getMaxTemp6hr() {
        return maxTemp6hr;
    }

    /**
     * @param maxTemp6hr
     *            the maxTemp6hr to set
     */
    public void setMaxTemp6hr(float maxTemp6hr) {
        this.maxTemp6hr = maxTemp6hr;
    }

    /**
     * @return the minTemp6hr
     */
    public float getMinTemp6hr() {
        return minTemp6hr;
    }

    /**
     * @param minTemp6hr
     *            the minTemp6hr to set
     */
    public void setMinTemp6hr(float minTemp6hr) {
        this.minTemp6hr = minTemp6hr;
    }

    /**
     * @return the precipPresent
     */
    public int getPrecipPresent() {
        return precipPresent;
    }

    /**
     * @param precipPresent
     *            the precipPresent to set
     */
    public void setPrecipPresent(int precipPresent) {
        this.precipPresent = precipPresent;
    }

    /**
     * @return the precip1hr
     */
    public float getPrecip1hr() {
        return precip1hr;
    }

    /**
     * @param precip1hr
     *            the precip1hr to set
     */
    public void setPrecip1hr(float precip1hr) {
        this.precip1hr = precip1hr;
    }

    /**
     * @return the precip6hr
     */
    public float getPrecip6hr() {
        return precip6hr;
    }

    /**
     * @param precip6hr
     *            the precip6hr to set
     */
    public void setPrecip6hr(float precip6hr) {
        this.precip6hr = precip6hr;
    }

    /**
     * @return the precip24hr
     */
    public float getPrecip24hr() {
        return precip24hr;
    }

    /**
     * @param precip24hr
     *            the precip24hr to set
     */
    public void setPrecip24hr(float precip24hr) {
        this.precip24hr = precip24hr;
    }

    /**
     * @return the snowDepth
     */
    public float getSnowDepth() {
        return snowDepth;
    }

    /**
     * @param snowDepth
     *            the snowDepth to set
     */
    public void setSnowDepth(float snowDepth) {
        this.snowDepth = snowDepth;
    }

    /**
     * @return the windDir
     */
    public float getWindDir() {
        return windDir;
    }

    /**
     * @param windDir
     *            the windDir to set
     */
    public void setWindDir(float windDir) {
        this.windDir = windDir;
    }

    /**
     * @return the windSpd
     */
    public float getWindSpd() {
        return windSpd;
    }

    /**
     * @param windSpd
     *            the windSpd to set
     */
    public void setWindSpd(float windSpd) {
        this.windSpd = windSpd;
    }

    /**
     * @return the gustSpd
     */
    public float getGustSpd() {
        return gustSpd;
    }

    /**
     * @param gustSpd
     *            the gustSpd to set
     */
    public void setGustSpd(float gustSpd) {
        this.gustSpd = gustSpd;
    }

    /**
     * @return the originTime
     */
    public long getOriginTime() {
        return originTime;
    }

    /**
     * @param originTime
     *            the originTime to set
     */
    public void setOriginTime(long originTime) {
        this.originTime = originTime;
    }

    /**
     * @return the peakWindSpeed
     */
    public float getPeakWindSpeed() {
        return peakWindSpeed;
    }

    /**
     * @param peakWindSpeed
     *            the peakWindSpeed to set
     */
    public void setPeakWindSpeed(float peakWindSpeed) {
        this.peakWindSpeed = peakWindSpeed;
    }

    /**
     * @return the peakWindDir
     */
    public float getPeakWindDir() {
        return peakWindDir;
    }

    /**
     * @param peakWindDir
     *            the peakWindDir to set
     */
    public void setPeakWindDir(float peakWindDir) {
        this.peakWindDir = peakWindDir;
    }

    /**
     * @return the peakWindHHMM
     */
    public int getPeakWindHHMM() {
        return peakWindHHMM;
    }

    /**
     * @param peakWindHHMM
     *            the peakWindHHMM to set
     */
    public void setPeakWindHHMM(int peakWindHHMM) {
        this.peakWindHHMM = peakWindHHMM;
    }

    /**
     * @return the sunshineDur
     */
    public int getSunshineDur() {
        return sunshineDur;
    }

    /**
     * @param sunshineDur
     *            the sunshineDur to set
     */
    public void setSunshineDur(int sunshineDur) {
        this.sunshineDur = sunshineDur;
    }

    /**
     * @return the max24temp
     */
    public float getMax24temp() {
        return max24temp;
    }

    /**
     * @param max24temp
     *            the max24temp to set
     */
    public void setMax24temp(float max24temp) {
        this.max24temp = max24temp;
    }

    /**
     * @return the min24temp
     */
    public float getMin24temp() {
        return min24temp;
    }

    /**
     * @param min24temp
     *            the min24temp to set
     */
    public void setMin24temp(float min24temp) {
        this.min24temp = min24temp;
    }

    /**
     * @return the variableWindFlag
     */
    public int getVariableWindFlag() {
        return variableWindFlag;
    }

    /**
     * @param variableWindFlag
     *            the variableWindFlag to set
     */
    public void setVariableWindFlag(int variableWindFlag) {
        this.variableWindFlag = variableWindFlag;
    }

    /**
     * @return the tornadic
     */
    public int getTornadic() {
        return tornadic;
    }

    /**
     * @param tornadic
     *            the tornadic to set
     */
    public void setTornadic(int tornadic) {
        this.tornadic = tornadic;
    }

    /**
     * @return the weatherBeginEnd
     */
    public RecentWx[] getWeatherBeginEnd() {
        return weatherBeginEnd;
    }

    /**
     * @param weatherBeginEnd
     *            the weatherBeginEnd to set
     */
    public void setWeatherBeginEnd(RecentWx[] weatherBeginEnd) {
        this.weatherBeginEnd = weatherBeginEnd;
    }

    /**
     * @return the auroraBorealisFlag
     */
    public int getAuroraBorealisFlag() {
        return auroraBorealisFlag;
    }

    /**
     * @param auroraBorealisFlag
     *            the auroraBorealisFlag to set
     */
    public void setAuroraBorealisFlag(int auroraBorealisFlag) {
        this.auroraBorealisFlag = auroraBorealisFlag;
    }

    /**
     * @return the futureExpansion
     */
    public String getFutureExpansion() {
        return futureExpansion;
    }

    /**
     * @param futureExpansion
     *            the futureExpansion to set
     */
    public void setFutureExpansion(String futureExpansion) {
        this.futureExpansion = futureExpansion;
    }

    /**
     * @return the qcMetar
     */
    public QCMetar getQcMetar() {
        return qcMetar;
    }

    /**
     * @param qcMetar
     *            the qcMetar to set
     */
    public void setQcMetar(QCMetar qcMetar) {
        this.qcMetar = qcMetar;
    }
}
