/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data, from METAR report text. From metar.h#decoded_METAR
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 01 FEB 2017  28609      amoore      Initial creation
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class DecodedMetar {

    /**
     * Max number of Runway Visual Reports for array.
     */
    public static final int NUM_RVR = 12;

    /**
     * Max number of metar recent weather events for array.
     */
    public static final int NUM_REWX = 10;

    /**
     * Max number of cloud conditions for array.
     */
    public static final int NUM_CLOUD_CONDITIONS = 6;

    /**
     * synoptic cloud type
     */
    private String synopticCloudType;

    /**
     * snow depth ASCII group
     */
    private String snowDepthGroup;

    /** Name of group token belongs to. Also called "code name" in legacy. */
    private String reportTypeCodeName;

    /** Horizontial visibility character value */
    private String horizVsby;

    /** auto indicator */
    private String autoIndicator;

    /** correction indicator */
    private String corIndicator;

    /** location of visibility at second site */
    private String vsby2ndSiteLoc;

    /** location of sky condition at second site */
    private String sky2ndSiteLoc;

    /** sky condition at second site */
    private String sky2ndSite;

    /** sector visibility direction */
    private String sectorVsbyDir;

    /** current obscuration */
    private String obscuration;

    /** sky condition obscuration */
    private String obscurationSkyCondition;

    /** variable sky below indicator */
    private String variableSkyBelow;

    /** variable sky above indicator */
    private String variableSkyAbove;

    /** lightning direction */
    private String lightningDirection;

    /** low cloud type */
    private String cloudLow;

    /** mid level cloud type */
    private String cloudMedium;

    /** upper level cloud type */
    private String cloudHigh;

    /** ceiling at second site location */
    private String cigSecondSiteLoc;

    /** indicator if a ridged ceiling a 2nd site */
    private String cigChar;

    /** direction of VIRGA indicator */
    private String virgaDirection;

    /** type or tornadic activity */
    private String tornadicType;

    /**
     * Task 29242: Tornadic location, direction, and movement data is parsed but
     * never used.
     */
    /** tornado location */
    private String tornadicLoc;

    /** tornado direction */
    private String tornadicDir;

    /** tornado movement */
    private String tornadicMov;

    /**
     * Task 29241: Thunderstorm data is parsed but never used.
     */
    /** thunderstorm movement */
    private String thunderStormMovement;

    /** thunderstorm location */
    private String thunderStormLocation;

    /** thunderstorm direction */
    private String thunderStormDirection;

    /** location of secondary ceiling height indicator */
    private String noSecondaryCeilingHeightLocation;

    /** location of secondary visibility sensor */
    private String noSecondaryVisualsLocation;

    /** Boolean if no sea level pressure is reported */
    private boolean slpNo;

    /** Boolean if no specials will be reported */
    private boolean noSpeci;

    /** Boolean if the is the first report from station */
    private boolean first;

    /** Boolean if this is the last report from the station */
    private boolean last;

    /** Boolean is the sun sensor is out */
    private boolean sunSensorOut;

    /** Boolean if aurora borealis indicator present */
    private boolean auroraBorealis;

    /** Boolean if AUTO indicator is present */
    private boolean auto;

    /** Boolean if Correction indicator present */
    private boolean cor;

    /** Boolean if no RVR is reported */
    private boolean noRunwayVisualRange;

    /** boolean if altimeter is present */
    private boolean altimeterSet;

    /** Boolean if VIRGA indicator present */
    private boolean virga;

    /** Boolean if hail size report is present */
    private boolean hail;

    /** Boolean if CHINO indicator is present */
    private boolean noSecondaryCeilingHeight;

    /** Boolean if VISNO indicator is present */
    private boolean noSecondaryVisuals;

    /** Boolean if PNO indicator is present */
    private boolean noRain;

    /** Boolean if PWINO is present */
    private boolean noPeakWind;

    /** Boolean if no freezing rain is present */
    private boolean noFreezingRain;

    /** Boolean in no Thunderstorm Report is present */
    private boolean noThunderStorms;

    /** Boolean if maintenance indicator is present */
    private boolean maintenance;

    /** Boolean id pressure is rising rapidly */
    private boolean pressureRisingRapidly;

    /** Boolean if pressure if falling rapidly */
    private boolean pressureFallingRapidly;

    /** Boolean that a wind shift was a result of a frontal passage */
    private boolean wShftFroPa;

    /** Boolean that occasional lightning is reported */
    private boolean occasionalLightning;

    /** Boolean that frequent lightning is reported */
    private boolean frequentLightning;

    /** Boolean that Continuous lightning is reported */
    private boolean constantLightning;

    /** Boolean that cloud to ground lightning is present */
    private boolean cgLtg;

    /** Boolean that in cloud lightning is present */
    private boolean icLtg;

    /** Boolean the cloud to cloud lightning is present */
    private boolean ccLtg;

    /** Boolean the cloud to air lightning is present */
    private boolean caLtg;

    /** Boolean the lightning is distant */
    private boolean dsntLtg;

    /** Boolean the apparent lightning is present */
    private boolean apLtg;

    /** Boolean the lightning is in the vicinity of the reporting station */
    private boolean vcyStnLtg;

    /** Boolean the lightning is over the reporting station */
    private boolean ovhdLtg;

    /** Boolean that lightning is a result of a thunderstorm in the area */
    private boolean lightningVCTS;

    /** Boolean the lightning is a result of a thunderstorm */
    private boolean lightningTS;

    /** min wind direction */
    private int minWnDir;

    /** max wnd direction */
    private int maxWnDir;

    /** air temperature */
    private int temp;

    /** dew poprivate int temperature */
    private int dewPtTemp;

    /** pressure tendency */
    private int charPressureTendency;

    /** min cloud ceiling height value */
    private int minCeiling;

    /** max cloud ceiling height value */
    private int maxCeiling;

    /** hour of wind shift */
    private int wshfTimeHour;

    /** minutes of wind shift */
    private int wshfTimeMinute;

    /** peak wind direction */
    private int pkWndDir;

    /** peak wind speed */
    private int pkWndSpeed;

    /** peak wind hour */
    private int pkWndHour;

    /** peak wind minutes */
    private int pkWndMinute;

    /** sky height at a second site in meters */
    private int sky2ndSiteMeters;

    /** snow increasing */
    private int snowIncrease;

    /** snow increasing depth */
    private int snowIncreaseTotalDepth;

    /** sunshine duration */
    private int sunshineDur;

    /** obscuration height */
    private int obscurationHeight;

    /** variable sky layer height */
    private int variableSkyLayerHeight;

    /** ceiling height value in meters */
    private int cig2ndSiteMeters;

    /** snow depth */
    private int snowDepth;

    /** beginning hour of tornado */
    private int bTornadicHour;

    /** begining minutes of tornado */
    private int bTornadicMinute;

    /** ending hour of tornado */
    private int eTornadicHour;

    /** ending minutes of tornado */
    private int eTornadicMinute;

    /** tornado location value */
    private int tornadicLocNum;

    /** sector visibility */
    private float sectorVsby;

    /** water equivalent of snow */
    private float waterEquivSnow;

    /** visibility at second site */
    private float vsby2ndSite;

    /** pressure tendency */
    private float pressure3HourTendency;

    /** amount of precip */
    private float precipAmt;

    /** 24 hour precip amount */
    private float precip24Amt;

    /** max air temperature */
    private float maxTemp;

    /** min air temperature */
    private float minTemp;

    /** max 24 hour air temperature */
    private float max24Temp;

    /** min 24 hour air temperature */
    private float min24Temp;

    /** min visibility */
    private float minVsby;

    /** max visibility */
    private float maxVsby;

    /** hourly precip amount */
    private float hourlyPrecip;

    /** tower visibility */
    private float twrVsby;

    /** surface visibility */
    private float sfcVsby;

    /** tempearture in tenths of a degree */
    private float tempToTenths;

    /** dew point in tenths of a degree */
    private float dewpointTempToTenths;

    /** sea level pressure */
    private float slp;

    /** hail size */
    private float hailSize;

    /** altimeter reading in inches of mercury */
    private double inchesAltstng;

    /** structure for RVR data */
    private RunwayVisRange[] rrvr = new RunwayVisRange[NUM_RVR];

    /** structure for DVR data */
    private DispatchVisRange dvr;

    /** array of recent weather data */
    private RecentWx[] recentWeathers = new RecentWx[NUM_REWX];

    /**
     * Task 29240: Significant Cloud data is never actually used in processing
     * after parsing.
     * 
     * structure for significant clouds
     */
    private SignificantCloud[] significantClouds = new SignificantCloud[3];

    /** structure for common data */
    private PedCmn cmnData;

    /**
     * @return an instance of {@link DecodedMetar} with default initialized
     *         values from hmPED_InitDcdMETAR.c.
     */
    public static DecodedMetar getInitializedDecodedMetar() {
        DecodedMetar metar = new DecodedMetar();

        metar.setTornadicType("");
        metar.setTornadicLoc("");
        metar.setTornadicDir("");
        metar.setTornadicMov("");
        metar.setThunderStormLocation("");
        metar.setThunderStormMovement("");
        metar.setThunderStormDirection("");
        metar.setbTornadicHour(Integer.MAX_VALUE);
        metar.setbTornadicMinute(Integer.MAX_VALUE);
        metar.seteTornadicHour(Integer.MAX_VALUE);
        metar.seteTornadicMinute(Integer.MAX_VALUE);
        metar.setTornadicLocNum(Integer.MAX_VALUE);

        metar.setAutoIndicator("");
        metar.setCorIndicator("");

        metar.setNoRunwayVisualRange(false);
        metar.setHail(false);
        metar.setHailSize((float) Integer.MAX_VALUE);

        metar.setNoSecondaryCeilingHeight(false);
        metar.setNoSecondaryCeilingHeightLocation("");

        metar.setNoSecondaryVisuals(false);
        metar.setNoSecondaryVisualsLocation("");

        metar.setNoRain(false);
        metar.setNoPeakWind(false);
        metar.setNoFreezingRain(false);
        metar.setNoThunderStorms(false);
        metar.setCmnData(new PedCmn());
        metar.getCmnData().setNil(false);
        metar.getCmnData().setCavok(false);
        metar.setMaintenance(false);
        metar.setHourlyPrecip((float) Integer.MAX_VALUE);

        metar.setObscurationHeight(Integer.MAX_VALUE);
        metar.setObscuration("");
        metar.setObscurationSkyCondition("");

        metar.setVariableSkyBelow("");
        metar.setVariableSkyAbove("");
        metar.setVariableSkyLayerHeight(Integer.MAX_VALUE);

        metar.setSectorVsby((float) Integer.MAX_VALUE);
        metar.setSectorVsbyDir("");

        metar.setReportTypeCodeName("");
        metar.getCmnData().setStationID("");
        metar.getCmnData().setObHour(Integer.MAX_VALUE);
        metar.getCmnData().setObMinute(Integer.MAX_VALUE);
        metar.getCmnData().setObMon(Integer.MAX_VALUE);
        metar.getCmnData().setObDay(Integer.MAX_VALUE);
        metar.getCmnData().setObYear(Integer.MAX_VALUE);
        metar.getCmnData().setDecodeStatus(0);

        metar.setSynopticCloudType("");

        metar.setCloudLow("");
        metar.setCloudMedium("");
        metar.setCloudHigh("");

        metar.setSnowDepthGroup("");
        metar.setSnowDepth(Integer.MAX_VALUE);

        metar.setTempToTenths((float) Integer.MAX_VALUE);
        metar.setDewpointTempToTenths((float) Integer.MAX_VALUE);

        metar.setOccasionalLightning(false);
        metar.setFrequentLightning(false);
        metar.setConstantLightning(false);
        metar.setCgLtg(false);
        metar.setIcLtg(false);
        metar.setCcLtg(false);
        metar.setCaLtg(false);
        metar.setApLtg(false);
        metar.setApLtg(false);
        metar.setDsntLtg(false);
        metar.setVcyStnLtg(false);

        metar.setLightningDirection("");

        for (int i = 0; i < metar.getRecentWeathers().length; i++) {
            RecentWx recentWx = new RecentWx();
            recentWx.setRecentWeatherName("");
            recentWx.setBeginHour(Integer.MAX_VALUE);
            recentWx.setBeginMinute(Integer.MAX_VALUE);
            recentWx.setEndHour(Integer.MAX_VALUE);
            recentWx.setEndMinute(Integer.MAX_VALUE);
            metar.getRecentWeathers()[i] = recentWx;
        }

        for (int i = 0; i < metar.getSignificantClouds().length; i++) {
            SignificantCloud sigClouds = new SignificantCloud();
            sigClouds.setSignificantCloudType("");
            sigClouds.setSignificantCloudLocation("");
            sigClouds.setSignificantCloudDirection("");
            sigClouds.setSignificantCloudMovement("");
            metar.getSignificantClouds()[i] = sigClouds;
        }

        metar.setAuroraBorealis(false);
        metar.setAuto(false);
        metar.setCor(false);

        metar.getCmnData().setWinData(new MetarWind());
        metar.getCmnData().getWinData().setWindDir(Integer.MAX_VALUE);
        metar.getCmnData().getWinData().setWindSpeed(Integer.MAX_VALUE);
        metar.getCmnData().getWinData().setWindGust(Integer.MAX_VALUE);
        metar.getCmnData().getWinData().setWindVrb(false);
        metar.getCmnData().getWinData().setWindUnits("KT");

        metar.setMinWnDir(Integer.MAX_VALUE);
        metar.setMaxWnDir(Integer.MAX_VALUE);

        metar.getCmnData().setPrevailingVisibilitySM((float) Integer.MAX_VALUE);

        for (int i = 0; i < metar.getRrvr().length; i++) {
            RunwayVisRange runwayVisRange = new RunwayVisRange();
            runwayVisRange.setRunwayDesignator("");
            runwayVisRange.setVisRange(Integer.MAX_VALUE);
            runwayVisRange.setVrblVisRange(false);
            runwayVisRange.setBelowMinRVR(false);
            runwayVisRange.setAboveMaxRVR(false);
            runwayVisRange.setMaxVisRange(Integer.MAX_VALUE);
            runwayVisRange.setMinVisRange(Integer.MAX_VALUE);
            metar.getRrvr()[i] = runwayVisRange;
        }

        metar.setDvr(new DispatchVisRange());
        ;
        metar.getDvr().setVisRange(Integer.MAX_VALUE);
        metar.getDvr().setVariableVisualRange(false);
        metar.getDvr().setBelowMinDVR(false);
        metar.getDvr().setAboveMaxDVR(false);
        metar.getDvr().setMaxVisualRange(Integer.MAX_VALUE);
        metar.getDvr().setMinVisualRange(Integer.MAX_VALUE);

        for (int i = 0; i < metar.getCmnData().getWxObstruct().length; i++) {
            metar.getCmnData().getWxObstruct()[i] = "";
        }

        for (int i = 0; i < metar.getCmnData()
                .getCloudConditions().length; i++) {
            CloudConditions cloudConditions = new CloudConditions();
            cloudConditions.setCloudType("");
            cloudConditions.setCloudHgtChar("");
            cloudConditions.setCloudHgtMeters(Integer.MAX_VALUE);
            cloudConditions.setOtherCldPhenom("");
            metar.getCmnData().getCloudConditions()[i] = cloudConditions;
        }

        metar.getCmnData().setVerticalVisibility(Integer.MAX_VALUE);

        metar.setTemp(Integer.MAX_VALUE);
        metar.setDewPtTemp(Integer.MAX_VALUE);

        metar.setSlpNo(false);
        metar.setSlp((float) Integer.MAX_VALUE);

        metar.setAltimeterSet(false);
        metar.setInchesAltstng((double) Integer.MAX_VALUE);

        metar.setCharPressureTendency(Integer.MAX_VALUE);
        metar.setPressure3HourTendency((float) Integer.MAX_VALUE);

        metar.setPrecipAmt((float) Integer.MAX_VALUE);

        metar.setPrecip24Amt((float) Integer.MAX_VALUE);
        metar.setMaxTemp((float) Integer.MAX_VALUE);
        metar.setMinTemp((float) Integer.MAX_VALUE);
        metar.setMax24Temp((float) Integer.MAX_VALUE);
        metar.setMin24Temp((float) Integer.MAX_VALUE);

        metar.setVirga(false);
        metar.setVirgaDirection("");

        metar.setMinCeiling(Integer.MAX_VALUE);
        metar.setMaxCeiling(Integer.MAX_VALUE);

        metar.setCig2ndSiteMeters(Integer.MAX_VALUE);
        metar.setCigSecondSiteLoc("");
        metar.setCigChar("");

        metar.setMinVsby((float) Integer.MAX_VALUE);
        metar.setMaxVsby((float) Integer.MAX_VALUE);
        metar.setVsby2ndSite((float) Integer.MAX_VALUE);
        metar.setVsby2ndSiteLoc("");

        metar.setNoSpeci(false);
        metar.setLast(false);
        metar.setFirst(false);

        metar.setSnowIncrease(Integer.MAX_VALUE);
        metar.setSnowIncreaseTotalDepth(Integer.MAX_VALUE);

        metar.setWaterEquivSnow((float) Integer.MAX_VALUE);

        metar.setSunshineDur(Integer.MAX_VALUE);
        metar.setSunSensorOut(false);

        metar.setWshfTimeHour(Integer.MAX_VALUE);
        metar.setWshfTimeMinute(Integer.MAX_VALUE);
        metar.setwShftFroPa(false);

        metar.setPressureRisingRapidly(false);
        metar.setPressureFallingRapidly(false);

        metar.setTwrVsby((float) Integer.MAX_VALUE);
        metar.setSfcVsby((float) Integer.MAX_VALUE);

        metar.setPkWndDir(Integer.MAX_VALUE);
        metar.setPkWndSpeed(Integer.MAX_VALUE);
        metar.setPkWndHour(Integer.MAX_VALUE);
        metar.setPkWndMinute(Integer.MAX_VALUE);

        return metar;
    }

    /**
     * Empty constructor.
     */
    public DecodedMetar() {
    }

    /**
     * @return the synopticCloudType
     */
    public String getSynopticCloudType() {
        return synopticCloudType;
    }

    /**
     * @param synopticCloudType
     *            the synopticCloudType to set
     */
    public void setSynopticCloudType(String synopticCloudType) {
        this.synopticCloudType = synopticCloudType;
    }

    /**
     * @return the snowDepthGroup
     */
    public String getSnowDepthGroup() {
        return snowDepthGroup;
    }

    /**
     * @param snowDepthGroup
     *            the snowDepthGroup to set
     */
    public void setSnowDepthGroup(String snowDepthGroup) {
        this.snowDepthGroup = snowDepthGroup;
    }

    /**
     * Also called "code name" in legacy.
     * 
     * @return the reportTypeCodeName
     */
    public String getReportTypeCodeName() {
        return reportTypeCodeName;
    }

    /**
     * Also called "code name" in legacy.
     * 
     * @param reportTypeCodeName
     *            the reportTypeCodeName to set
     */
    public void setReportTypeCodeName(String reportTypeCodeName) {
        this.reportTypeCodeName = reportTypeCodeName;
    }

    /**
     * @return the horizVsby
     */
    public String getHorizVsby() {
        return horizVsby;
    }

    /**
     * @param horizVsby
     *            the horizVsby to set
     */
    public void setHorizVsby(String horizVsby) {
        this.horizVsby = horizVsby;
    }

    /**
     * @return the autoIndicator
     */
    public String getAutoIndicator() {
        return autoIndicator;
    }

    /**
     * @param autoIndicator
     *            the autoIndicator to set
     */
    public void setAutoIndicator(String autoIndicator) {
        this.autoIndicator = autoIndicator;
    }

    /**
     * @return the corIndicator
     */
    public String getCorIndicator() {
        return corIndicator;
    }

    /**
     * @param corIndicator
     *            the corIndicator to set
     */
    public void setCorIndicator(String corIndicator) {
        this.corIndicator = corIndicator;
    }

    /**
     * @return the vsby2ndSiteLoc
     */
    public String getVsby2ndSiteLoc() {
        return vsby2ndSiteLoc;
    }

    /**
     * @param vsby2ndSiteLoc
     *            the vsby2ndSiteLoc to set
     */
    public void setVsby2ndSiteLoc(String vsby2ndSiteLoc) {
        this.vsby2ndSiteLoc = vsby2ndSiteLoc;
    }

    /**
     * @return the sky2ndSiteLoc
     */
    public String getSky2ndSiteLoc() {
        return sky2ndSiteLoc;
    }

    /**
     * @param sky2ndSiteLoc
     *            the sky2ndSiteLoc to set
     */
    public void setSky2ndSiteLoc(String sky2ndSiteLoc) {
        this.sky2ndSiteLoc = sky2ndSiteLoc;
    }

    /**
     * @return the sky2ndSite
     */
    public String getSky2ndSite() {
        return sky2ndSite;
    }

    /**
     * @param sky2ndSite
     *            the sky2ndSite to set
     */
    public void setSky2ndSite(String sky2ndSite) {
        this.sky2ndSite = sky2ndSite;
    }

    /**
     * @return the sectorVsbyDir
     */
    public String getSectorVsbyDir() {
        return sectorVsbyDir;
    }

    /**
     * @param sectorVsbyDir
     *            the sectorVsbyDir to set
     */
    public void setSectorVsbyDir(String sectorVsbyDir) {
        this.sectorVsbyDir = sectorVsbyDir;
    }

    /**
     * @return the obscuration
     */
    public String getObscuration() {
        return obscuration;
    }

    /**
     * @param obscur
     *            the obscur to set
     */
    public void setObscuration(String obscuration) {
        this.obscuration = obscuration;
    }

    /**
     * @return the obscurationSkyCondition
     */
    public String getObscurationSkyCondition() {
        return obscurationSkyCondition;
    }

    /**
     * @param obscurationSkyCondition
     *            the obscurationSkyCondition to set
     */
    public void setObscurationSkyCondition(String obscurationSkyCondition) {
        this.obscurationSkyCondition = obscurationSkyCondition;
    }

    /**
     * @return the variableSkyBelow
     */
    public String getVariableSkyBelow() {
        return variableSkyBelow;
    }

    /**
     * @param variableSkyBelow
     *            the variableSkyBelow to set
     */
    public void setVariableSkyBelow(String variableSkyBelow) {
        this.variableSkyBelow = variableSkyBelow;
    }

    /**
     * @return the variableSkyAbove
     */
    public String getVariableSkyAbove() {
        return variableSkyAbove;
    }

    /**
     * @param variableSkyAbove
     *            the variableSkyAbove to set
     */
    public void setVariableSkyAbove(String variableSkyAbove) {
        this.variableSkyAbove = variableSkyAbove;
    }

    /**
     * @return the lightningDirection
     */
    public String getLightningDirection() {
        return lightningDirection;
    }

    /**
     * @param lightningDirection
     *            the lightningDirection to set
     */
    public void setLightningDirection(String lightningDirection) {
        this.lightningDirection = lightningDirection;
    }

    /**
     * @return the cloudLow
     */
    public String getCloudLow() {
        return cloudLow;
    }

    /**
     * @param cloudLow
     *            the cloudLow to set
     */
    public void setCloudLow(String cloudLow) {
        this.cloudLow = cloudLow;
    }

    /**
     * @return the cloudMedium
     */
    public String getCloudMedium() {
        return cloudMedium;
    }

    /**
     * @param cloudMedium
     *            the cloudMedium to set
     */
    public void setCloudMedium(String cloudMedium) {
        this.cloudMedium = cloudMedium;
    }

    /**
     * @return the cloudHigh
     */
    public String getCloudHigh() {
        return cloudHigh;
    }

    /**
     * @param cloudHigh
     *            the cloudHigh to set
     */
    public void setCloudHigh(String cloudHigh) {
        this.cloudHigh = cloudHigh;
    }

    /**
     * @return the cigSecondSiteLoc
     */
    public String getCigSecondSiteLoc() {
        return cigSecondSiteLoc;
    }

    /**
     * @param cigSecondSiteLoc
     *            the cigSecondSiteLoc to set
     */
    public void setCigSecondSiteLoc(String cigSecondSiteLoc) {
        this.cigSecondSiteLoc = cigSecondSiteLoc;
    }

    /**
     * @return the cigChar
     */
    public String getCigChar() {
        return cigChar;
    }

    /**
     * @param cigChar
     *            the cigChar to set
     */
    public void setCigChar(String cigChar) {
        this.cigChar = cigChar;
    }

    /**
     * @return the virgaDirection
     */
    public String getVirgaDirection() {
        return virgaDirection;
    }

    /**
     * @param virgaDirection
     *            the virgaDirection to set
     */
    public void setVirgaDirection(String virgaDirection) {
        this.virgaDirection = virgaDirection;
    }

    /**
     * @return the tornadicType
     */
    public String getTornadicType() {
        return tornadicType;
    }

    /**
     * @param tornadicType
     *            the tornadicType to set
     */
    public void setTornadicType(String tornadicType) {
        this.tornadicType = tornadicType;
    }

    /**
     * @return the tornadicLoc
     */
    public String getTornadicLoc() {
        return tornadicLoc;
    }

    /**
     * @param tornadicLoc
     *            the tornadicLoc to set
     */
    public void setTornadicLoc(String tornadicLoc) {
        this.tornadicLoc = tornadicLoc;
    }

    /**
     * @return the tornadicDir
     */
    public String getTornadicDir() {
        return tornadicDir;
    }

    /**
     * @param tornadicDir
     *            the tornadicDir to set
     */
    public void setTornadicDir(String tornadicDir) {
        this.tornadicDir = tornadicDir;
    }

    /**
     * @return the tornadicMov
     */
    public String getTornadicMov() {
        return tornadicMov;
    }

    /**
     * @param tornadicMov
     *            the tornadicMov to set
     */
    public void setTornadicMov(String tornadicMov) {
        this.tornadicMov = tornadicMov;
    }

    /**
     * @return the thunderStormMovement
     */
    public String getThunderStormMovement() {
        return thunderStormMovement;
    }

    /**
     * @param thunderStormMovement
     *            the thunderStormMovement to set
     */
    public void setThunderStormMovement(String thunderStormMovement) {
        this.thunderStormMovement = thunderStormMovement;
    }

    /**
     * @return the thunderStormLocation
     */
    public String getThunderStormLocation() {
        return thunderStormLocation;
    }

    /**
     * @param thunderStormLocation
     *            the thunderStormLocation to set
     */
    public void setThunderStormLocation(String thunderStormLocation) {
        this.thunderStormLocation = thunderStormLocation;
    }

    /**
     * @return the thunderStormDirection
     */
    public String getThunderStormDirection() {
        return thunderStormDirection;
    }

    /**
     * @param thunderStormDirection
     *            the thunderStormDirection to set
     */
    public void setThunderStormDirection(String thunderStormDirection) {
        this.thunderStormDirection = thunderStormDirection;
    }

    /**
     * Also called "ChinoLoc" in legacy.
     * 
     * @return the noSecondaryCeilingHeightLocation
     */
    public String getNoSecondaryCeilingHeightLocation() {
        return noSecondaryCeilingHeightLocation;
    }

    /**
     * Also called "ChinoLoc" in legacy.
     * 
     * @param noSecondaryCeilingHeightLocation
     *            the noSecondaryCeilingHeightLocation to set
     */
    public void setNoSecondaryCeilingHeightLocation(
            String noSecondaryCeilingHeightLocation) {
        this.noSecondaryCeilingHeightLocation = noSecondaryCeilingHeightLocation;
    }

    /**
     * Also called "VISNO Loc" in legacy
     * 
     * @return the noSecondaryVisualsLocation
     */
    public String getNoSecondaryVisualsLocation() {
        return noSecondaryVisualsLocation;
    }

    /**
     * Also called "VISNO Loc" in legacy
     * 
     * @param noSecondaryVisualsLocation
     *            the noSecondaryVisualsLocation to set
     */
    public void setNoSecondaryVisualsLocation(
            String noSecondaryVisualsLocation) {
        this.noSecondaryVisualsLocation = noSecondaryVisualsLocation;
    }

    /**
     * @return the slpNo
     */
    public boolean isSlpNo() {
        return slpNo;
    }

    /**
     * @param slpNo
     *            the slpNo to set
     */
    public void setSlpNo(boolean slpNo) {
        this.slpNo = slpNo;
    }

    /**
     * @return the noSpeci
     */
    public boolean isNoSpeci() {
        return noSpeci;
    }

    /**
     * @param noSpeci
     *            the noSpeci to set
     */
    public void setNoSpeci(boolean noSpeci) {
        this.noSpeci = noSpeci;
    }

    /**
     * @return the first
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * @param first
     *            the first to set
     */
    public void setFirst(boolean first) {
        this.first = first;
    }

    /**
     * @return the last
     */
    public boolean isLast() {
        return last;
    }

    /**
     * @param last
     *            the last to set
     */
    public void setLast(boolean last) {
        this.last = last;
    }

    /**
     * @return the sunSensorOut
     */
    public boolean isSunSensorOut() {
        return sunSensorOut;
    }

    /**
     * @param sunSensorOut
     *            the sunSensorOut to set
     */
    public void setSunSensorOut(boolean sunSensorOut) {
        this.sunSensorOut = sunSensorOut;
    }

    /**
     * @return the auroraBorealis
     */
    public boolean isAuroraBorealis() {
        return auroraBorealis;
    }

    /**
     * @param auroraBorealis
     *            the auroraBorealis to set
     */
    public void setAuroraBorealis(boolean auroraBorealis) {
        this.auroraBorealis = auroraBorealis;
    }

    /**
     * @return the auto
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * @param auto
     *            the auto to set
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    /**
     * @return the cor
     */
    public boolean isCor() {
        return cor;
    }

    /**
     * @param cor
     *            the cor to set
     */
    public void setCor(boolean cor) {
        this.cor = cor;
    }

    /**
     * @return the noRunwayVisualRange
     */
    public boolean isNoRunwayVisualRange() {
        return noRunwayVisualRange;
    }

    /**
     * @param noRunwayVisualRange
     *            the noRunwayVisualRange to set
     */
    public void setNoRunwayVisualRange(boolean noRunwayVisualRange) {
        this.noRunwayVisualRange = noRunwayVisualRange;
    }

    /**
     * Also called "aAltStng" in legacy
     * 
     * @return the altimeterSet
     */
    public boolean isAltimeterSet() {
        return altimeterSet;
    }

    /**
     * Also called "aAltStng" in legacy
     * 
     * @param altimeterSet
     *            the altimeterSet to set
     */
    public void setAltimeterSet(boolean altimeterSet) {
        this.altimeterSet = altimeterSet;
    }

    /**
     * @return the virga
     */
    public boolean isVirga() {
        return virga;
    }

    /**
     * @param virga
     *            the virga to set
     */
    public void setVirga(boolean virga) {
        this.virga = virga;
    }

    /**
     * Also called "gr" in legacy
     * 
     * @return the hail
     */
    public boolean isHail() {
        return hail;
    }

    /**
     * Also called "gr" in legacy
     * 
     * @param hail
     *            the hail to set
     */
    public void setHail(boolean hail) {
        this.hail = hail;
    }

    /**
     * Also called "CHINO" in legacy
     * 
     * @return the noSecondaryCeilingHeight
     */
    public boolean isNoSecondaryCeilingHeight() {
        return noSecondaryCeilingHeight;
    }

    /**
     * Also called "CHINO" in legacy
     * 
     * @param noSecondaryCeilingHeight
     *            the noSecondaryCeilingHeight to set
     */
    public void setNoSecondaryCeilingHeight(boolean noSecondaryCeilingHeight) {
        this.noSecondaryCeilingHeight = noSecondaryCeilingHeight;
    }

    /**
     * Also called "VISNO" in legacy
     * 
     * @return the noSecondaryVisuals
     */
    public boolean isNoSecondaryVisuals() {
        return noSecondaryVisuals;
    }

    /**
     * Also called "VISNO" in legacy
     * 
     * @param noSecondaryVisuals
     *            the noSecondaryVisuals to set
     */
    public void setNoSecondaryVisuals(boolean noSecondaryVisuals) {
        this.noSecondaryVisuals = noSecondaryVisuals;
    }

    /**
     * Also called "pno" in legacy
     * 
     * @return the noRain
     */
    public boolean isNoRain() {
        return noRain;
    }

    /**
     * Also called "pno" in legacy
     * 
     * @param noRain
     *            the noRain to set
     */
    public void setNoRain(boolean noRain) {
        this.noRain = noRain;
    }

    /**
     * @return the noPeakWind
     */
    public boolean isNoPeakWind() {
        return noPeakWind;
    }

    /**
     * @param noPeakWind
     *            the noPeakWind to set
     */
    public void setNoPeakWind(boolean noPeakWind) {
        this.noPeakWind = noPeakWind;
    }

    /**
     * @return the noFreezingRain
     */
    public boolean isNoFreezingRain() {
        return noFreezingRain;
    }

    /**
     * @param noFreezingRain
     *            the noFreezingRain to set
     */
    public void setNoFreezingRain(boolean noFreezingRain) {
        this.noFreezingRain = noFreezingRain;
    }

    /**
     * @return the noThunderStorms
     */
    public boolean isNoThunderStorms() {
        return noThunderStorms;
    }

    /**
     * @param noThunderStorms
     *            the noThunderStorms to set
     */
    public void setNoThunderStorms(boolean noThunderStorms) {
        this.noThunderStorms = noThunderStorms;
    }

    /**
     * Also called "dollarSign" in legacy.
     * 
     * @return the maintenance
     */
    public boolean isMaintenance() {
        return maintenance;
    }

    /**
     * Also called "dollarSign" in legacy.
     * 
     * @param maintenance
     *            the maintenance to set
     */
    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    /**
     * @return the pressureRisingRapidly
     */
    public boolean isPressureRisingRapidly() {
        return pressureRisingRapidly;
    }

    /**
     * @param pressureRisingRapidly
     *            the pressureRisingRapidly to set
     */
    public void setPressureRisingRapidly(boolean pressureRisingRapidly) {
        this.pressureRisingRapidly = pressureRisingRapidly;
    }

    /**
     * @return the pressureFallingRapidly
     */
    public boolean isPressureFallingRapidly() {
        return pressureFallingRapidly;
    }

    /**
     * @param pressureFallingRapidly
     *            the pressureFallingRapidly to set
     */
    public void setPressureFallingRapidly(boolean pressureFallingRapidly) {
        this.pressureFallingRapidly = pressureFallingRapidly;
    }

    /**
     * @return the wShftFroPa
     */
    public boolean iswShftFroPa() {
        return wShftFroPa;
    }

    /**
     * @param wShftFroPa
     *            the wShftFroPa to set
     */
    public void setwShftFroPa(boolean wShftFroPa) {
        this.wShftFroPa = wShftFroPa;
    }

    /**
     * @return the occasionalLightning
     */
    public boolean isOccasionalLightning() {
        return occasionalLightning;
    }

    /**
     * @param occasionalLightning
     *            the occasionalLightning to set
     */
    public void setOccasionalLightning(boolean occasionalLightning) {
        this.occasionalLightning = occasionalLightning;
    }

    /**
     * @return the frequentLightning
     */
    public boolean isFrequentLightning() {
        return frequentLightning;
    }

    /**
     * @param frequentLightning
     *            the frequentLightning to set
     */
    public void setFrequentLightning(boolean frequentLightning) {
        this.frequentLightning = frequentLightning;
    }

    /**
     * @return the constantLightning
     */
    public boolean isConstantLightning() {
        return constantLightning;
    }

    /**
     * @param constantLightning
     *            the constantLightning to set
     */
    public void setConstantLightning(boolean constantLightning) {
        this.constantLightning = constantLightning;
    }

    /**
     * @return the cgLtg
     */
    public boolean isCgLtg() {
        return cgLtg;
    }

    /**
     * @param cgLtg
     *            the cgLtg to set
     */
    public void setCgLtg(boolean cgLtg) {
        this.cgLtg = cgLtg;
    }

    /**
     * @return the icLtg
     */
    public boolean isIcLtg() {
        return icLtg;
    }

    /**
     * @param icLtg
     *            the icLtg to set
     */
    public void setIcLtg(boolean icLtg) {
        this.icLtg = icLtg;
    }

    /**
     * @return the ccLtg
     */
    public boolean isCcLtg() {
        return ccLtg;
    }

    /**
     * @param ccLtg
     *            the ccLtg to set
     */
    public void setCcLtg(boolean ccLtg) {
        this.ccLtg = ccLtg;
    }

    /**
     * @return the caLtg
     */
    public boolean isCaLtg() {
        return caLtg;
    }

    /**
     * @param caLtg
     *            the caLtg to set
     */
    public void setCaLtg(boolean caLtg) {
        this.caLtg = caLtg;
    }

    /**
     * @return the dsntLtg
     */
    public boolean isDsntLtg() {
        return dsntLtg;
    }

    /**
     * @param dsntLtg
     *            the dsntLtg to set
     */
    public void setDsntLtg(boolean dsntLtg) {
        this.dsntLtg = dsntLtg;
    }

    /**
     * @return the apLtg
     */
    public boolean isApLtg() {
        return apLtg;
    }

    /**
     * @param apLtg
     *            the apLtg to set
     */
    public void setApLtg(boolean apLtg) {
        this.apLtg = apLtg;
    }

    /**
     * @return the vcyStnLtg
     */
    public boolean isVcyStnLtg() {
        return vcyStnLtg;
    }

    /**
     * @param vcyStnLtg
     *            the vcyStnLtg to set
     */
    public void setVcyStnLtg(boolean vcyStnLtg) {
        this.vcyStnLtg = vcyStnLtg;
    }

    /**
     * @return the ovhdLtg
     */
    public boolean isOvhdLtg() {
        return ovhdLtg;
    }

    /**
     * @param ovhdLtg
     *            the ovhdLtg to set
     */
    public void setOvhdLtg(boolean ovhdLtg) {
        this.ovhdLtg = ovhdLtg;
    }

    /**
     * @return the lightningVCTS
     */
    public boolean isLightningVCTS() {
        return lightningVCTS;
    }

    /**
     * @param lightningVCTS
     *            the lightningVCTS to set
     */
    public void setLightningVCTS(boolean lightningVCTS) {
        this.lightningVCTS = lightningVCTS;
    }

    /**
     * @return the lightningTS
     */
    public boolean isLightningTS() {
        return lightningTS;
    }

    /**
     * @param lightningTS
     *            the lightningTS to set
     */
    public void setLightningTS(boolean lightningTS) {
        this.lightningTS = lightningTS;
    }

    /**
     * @return the minWnDir
     */
    public int getMinWnDir() {
        return minWnDir;
    }

    /**
     * @param minWnDir
     *            the minWnDir to set
     */
    public void setMinWnDir(int minWnDir) {
        this.minWnDir = minWnDir;
    }

    /**
     * @return the maxWnDir
     */
    public int getMaxWnDir() {
        return maxWnDir;
    }

    /**
     * @param maxWnDir
     *            the maxWnDir to set
     */
    public void setMaxWnDir(int maxWnDir) {
        this.maxWnDir = maxWnDir;
    }

    /**
     * @return the temp
     */
    public int getTemp() {
        return temp;
    }

    /**
     * @param temp
     *            the temp to set
     */
    public void setTemp(int temp) {
        this.temp = temp;
    }

    /**
     * @return the dewPtTemp
     */
    public int getDewPtTemp() {
        return dewPtTemp;
    }

    /**
     * @param dewPtTemp
     *            the dewPtTemp to set
     */
    public void setDewPtTemp(int dewPtTemp) {
        this.dewPtTemp = dewPtTemp;
    }

    /**
     * @return the charPressureTendency
     */
    public int getCharPressureTendency() {
        return charPressureTendency;
    }

    /**
     * @param charPressureTendency
     *            the charPressureTendency to set
     */
    public void setCharPressureTendency(int charPressureTendency) {
        this.charPressureTendency = charPressureTendency;
    }

    /**
     * @return the minCeiling
     */
    public int getMinCeiling() {
        return minCeiling;
    }

    /**
     * @param minCeiling
     *            the minCeiling to set
     */
    public void setMinCeiling(int minCeiling) {
        this.minCeiling = minCeiling;
    }

    /**
     * @return the maxCeiling
     */
    public int getMaxCeiling() {
        return maxCeiling;
    }

    /**
     * @param maxCeiling
     *            the maxCeiling to set
     */
    public void setMaxCeiling(int maxCeiling) {
        this.maxCeiling = maxCeiling;
    }

    /**
     * @return the wshfTimeHour
     */
    public int getWshfTimeHour() {
        return wshfTimeHour;
    }

    /**
     * @param wshfTimeHour
     *            the wshfTimeHour to set
     */
    public void setWshfTimeHour(int wshfTimeHour) {
        this.wshfTimeHour = wshfTimeHour;
    }

    /**
     * @return the wshfTimeMinute
     */
    public int getWshfTimeMinute() {
        return wshfTimeMinute;
    }

    /**
     * @param wshfTimeMinute
     *            the wshfTimeMinute to set
     */
    public void setWshfTimeMinute(int wshfTimeMinute) {
        this.wshfTimeMinute = wshfTimeMinute;
    }

    /**
     * @return the pkWndDir
     */
    public int getPkWndDir() {
        return pkWndDir;
    }

    /**
     * @param pkWndDir
     *            the pkWndDir to set
     */
    public void setPkWndDir(int pkWndDir) {
        this.pkWndDir = pkWndDir;
    }

    /**
     * @return the pkWndSpeed
     */
    public int getPkWndSpeed() {
        return pkWndSpeed;
    }

    /**
     * @param pkWndSpeed
     *            the pkWndSpeed to set
     */
    public void setPkWndSpeed(int pkWndSpeed) {
        this.pkWndSpeed = pkWndSpeed;
    }

    /**
     * @return the pkWndHour
     */
    public int getPkWndHour() {
        return pkWndHour;
    }

    /**
     * @param pkWndHour
     *            the pkWndHour to set
     */
    public void setPkWndHour(int pkWndHour) {
        this.pkWndHour = pkWndHour;
    }

    /**
     * @return the pkWndMinute
     */
    public int getPkWndMinute() {
        return pkWndMinute;
    }

    /**
     * @param pkWndMinute
     *            the pkWndMinute to set
     */
    public void setPkWndMinute(int pkWndMinute) {
        this.pkWndMinute = pkWndMinute;
    }

    /**
     * @return the sky2ndSiteMeters
     */
    public int getSky2ndSiteMeters() {
        return sky2ndSiteMeters;
    }

    /**
     * @param sky2ndSiteMeters
     *            the sky2ndSiteMeters to set
     */
    public void setSky2ndSiteMeters(int sky2ndSiteMeters) {
        this.sky2ndSiteMeters = sky2ndSiteMeters;
    }

    /**
     * @return the snowIncrease
     */
    public int getSnowIncrease() {
        return snowIncrease;
    }

    /**
     * @param snowIncrease
     *            the snowIncrease to set
     */
    public void setSnowIncrease(int snowIncrease) {
        this.snowIncrease = snowIncrease;
    }

    /**
     * @return the snowIncreaseTotalDepth
     */
    public int getSnowIncreaseTotalDepth() {
        return snowIncreaseTotalDepth;
    }

    /**
     * @param snowIncreaseTotalDepth
     *            the snowIncreaseTotalDepth to set
     */
    public void setSnowIncreaseTotalDepth(int snowIncreaseTotalDepth) {
        this.snowIncreaseTotalDepth = snowIncreaseTotalDepth;
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
     * @return the obscurationHeight
     */
    public int getObscurationHeight() {
        return obscurationHeight;
    }

    /**
     * @param obscurationHeight
     *            the obscurationHeight to set
     */
    public void setObscurationHeight(int obscurationHeight) {
        this.obscurationHeight = obscurationHeight;
    }

    /**
     * @return the variableSkyLayerHeight
     */
    public int getVariableSkyLayerHeight() {
        return variableSkyLayerHeight;
    }

    /**
     * @param variableSkyLayerHeight
     *            the variableSkyLayerHeight to set
     */
    public void setVariableSkyLayerHeight(int variableSkyLayerHeight) {
        this.variableSkyLayerHeight = variableSkyLayerHeight;
    }

    /**
     * @return the cig2ndSiteMeters
     */
    public int getCig2ndSiteMeters() {
        return cig2ndSiteMeters;
    }

    /**
     * @param cig2ndSiteMeters
     *            the cig2ndSiteMeters to set
     */
    public void setCig2ndSiteMeters(int cig2ndSiteMeters) {
        this.cig2ndSiteMeters = cig2ndSiteMeters;
    }

    /**
     * @return the snowDepth
     */
    public int getSnowDepth() {
        return snowDepth;
    }

    /**
     * @param snowDepth
     *            the snowDepth to set
     */
    public void setSnowDepth(int snowDepth) {
        this.snowDepth = snowDepth;
    }

    /**
     * @return the bTornadicHour
     */
    public int getbTornadicHour() {
        return bTornadicHour;
    }

    /**
     * @param bTornadicHour
     *            the bTornadicHour to set
     */
    public void setbTornadicHour(int bTornadicHour) {
        this.bTornadicHour = bTornadicHour;
    }

    /**
     * @return the bTornadicMinute
     */
    public int getbTornadicMinute() {
        return bTornadicMinute;
    }

    /**
     * @param bTornadicMinute
     *            the bTornadicMinute to set
     */
    public void setbTornadicMinute(int bTornadicMinute) {
        this.bTornadicMinute = bTornadicMinute;
    }

    /**
     * @return the eTornadicHour
     */
    public int geteTornadicHour() {
        return eTornadicHour;
    }

    /**
     * @param eTornadicHour
     *            the eTornadicHour to set
     */
    public void seteTornadicHour(int eTornadicHour) {
        this.eTornadicHour = eTornadicHour;
    }

    /**
     * @return the eTornadicMinute
     */
    public int geteTornadicMinute() {
        return eTornadicMinute;
    }

    /**
     * @param eTornadicMinute
     *            the eTornadicMinute to set
     */
    public void seteTornadicMinute(int eTornadicMinute) {
        this.eTornadicMinute = eTornadicMinute;
    }

    /**
     * @return the tornadicLocNum
     */
    public int getTornadicLocNum() {
        return tornadicLocNum;
    }

    /**
     * @param tornadicLocNum
     *            the tornadicLocNum to set
     */
    public void setTornadicLocNum(int tornadicLocNum) {
        this.tornadicLocNum = tornadicLocNum;
    }

    /**
     * @return the sectorVsby
     */
    public float getSectorVsby() {
        return sectorVsby;
    }

    /**
     * @param sectorVsby
     *            the sectorVsby to set
     */
    public void setSectorVsby(float sectorVsby) {
        this.sectorVsby = sectorVsby;
    }

    /**
     * @return the waterEquivSnow
     */
    public float getWaterEquivSnow() {
        return waterEquivSnow;
    }

    /**
     * @param waterEquivSnow
     *            the waterEquivSnow to set
     */
    public void setWaterEquivSnow(float waterEquivSnow) {
        this.waterEquivSnow = waterEquivSnow;
    }

    /**
     * @return the vsby2ndSite
     */
    public float getVsby2ndSite() {
        return vsby2ndSite;
    }

    /**
     * @param vsby2ndSite
     *            the vsby2ndSite to set
     */
    public void setVsby2ndSite(float vsby2ndSite) {
        this.vsby2ndSite = vsby2ndSite;
    }

    /**
     * @return the pressure3HourTendency
     */
    public float getPressure3HourTendency() {
        return pressure3HourTendency;
    }

    /**
     * @param pressure3HourTendency
     *            the pressure3HourTendency to set
     */
    public void setPressure3HourTendency(float pressure3HourTendency) {
        this.pressure3HourTendency = pressure3HourTendency;
    }

    /**
     * @return the precipAmt
     */
    public float getPrecipAmt() {
        return precipAmt;
    }

    /**
     * @param precipAmt
     *            the precipAmt to set
     */
    public void setPrecipAmt(float precipAmt) {
        this.precipAmt = precipAmt;
    }

    /**
     * @return the precip24Amt
     */
    public float getPrecip24Amt() {
        return precip24Amt;
    }

    /**
     * @param precip24Amt
     *            the precip24Amt to set
     */
    public void setPrecip24Amt(float precip24Amt) {
        this.precip24Amt = precip24Amt;
    }

    /**
     * @return the maxTemp
     */
    public float getMaxTemp() {
        return maxTemp;
    }

    /**
     * @param maxTemp
     *            the maxTemp to set
     */
    public void setMaxTemp(float maxTemp) {
        this.maxTemp = maxTemp;
    }

    /**
     * @return the minTemp
     */
    public float getMinTemp() {
        return minTemp;
    }

    /**
     * @param minTemp
     *            the minTemp to set
     */
    public void setMinTemp(float minTemp) {
        this.minTemp = minTemp;
    }

    /**
     * @return the max24Temp
     */
    public float getMax24Temp() {
        return max24Temp;
    }

    /**
     * @param max24Temp
     *            the max24Temp to set
     */
    public void setMax24Temp(float max24Temp) {
        this.max24Temp = max24Temp;
    }

    /**
     * @return the min24Temp
     */
    public float getMin24Temp() {
        return min24Temp;
    }

    /**
     * @param min24Temp
     *            the min24Temp to set
     */
    public void setMin24Temp(float min24Temp) {
        this.min24Temp = min24Temp;
    }

    /**
     * @return the minVsby
     */
    public float getMinVsby() {
        return minVsby;
    }

    /**
     * @param minVsby
     *            the minVsby to set
     */
    public void setMinVsby(float minVsby) {
        this.minVsby = minVsby;
    }

    /**
     * @return the maxVsby
     */
    public float getMaxVsby() {
        return maxVsby;
    }

    /**
     * @param maxVsby
     *            the maxVsby to set
     */
    public void setMaxVsby(float maxVsby) {
        this.maxVsby = maxVsby;
    }

    /**
     * @return the hourlyPrecip
     */
    public float getHourlyPrecip() {
        return hourlyPrecip;
    }

    /**
     * @param hourlyPrecip
     *            the hourlyPrecip to set
     */
    public void setHourlyPrecip(float hourlyPrecip) {
        this.hourlyPrecip = hourlyPrecip;
    }

    /**
     * @return the twrVsby
     */
    public float getTwrVsby() {
        return twrVsby;
    }

    /**
     * @param twrVsby
     *            the twrVsby to set
     */
    public void setTwrVsby(float twrVsby) {
        this.twrVsby = twrVsby;
    }

    /**
     * @return the sfcVsby
     */
    public float getSfcVsby() {
        return sfcVsby;
    }

    /**
     * @param sfcVsby
     *            the sfcVsby to set
     */
    public void setSfcVsby(float sfcVsby) {
        this.sfcVsby = sfcVsby;
    }

    /**
     * @return the tempToTenths
     */
    public float getTempToTenths() {
        return tempToTenths;
    }

    /**
     * @param tempToTenths
     *            the tempToTenths to set
     */
    public void setTempToTenths(float tempToTenths) {
        this.tempToTenths = tempToTenths;
    }

    /**
     * @return the dewpointTempToTenths
     */
    public float getDewpointTempToTenths() {
        return dewpointTempToTenths;
    }

    /**
     * @param dewpointTempToTenths
     *            the dewpointTempToTenths to set
     */
    public void setDewpointTempToTenths(float dewpointTempToTenths) {
        this.dewpointTempToTenths = dewpointTempToTenths;
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
     * Also called "gr size" in legacy
     * 
     * @return the hailSize
     */
    public float getHailSize() {
        return hailSize;
    }

    /**
     * Also called "gr size" in legacy
     * 
     * @param hailSize
     *            the hailSize to set
     */
    public void setHailSize(float hailSize) {
        this.hailSize = hailSize;
    }

    /**
     * @return the inchesAltstng
     */
    public double getInchesAltstng() {
        return inchesAltstng;
    }

    /**
     * @param inchesAltstng
     *            the inches_altstng to set
     */
    public void setInchesAltstng(double inchesAltstng) {
        this.inchesAltstng = inchesAltstng;
    }

    /**
     * @return the rrvr
     */
    public RunwayVisRange[] getRrvr() {
        return rrvr;
    }

    /**
     * @param rrvr
     *            the rrvr to set
     */
    public void setRrvr(RunwayVisRange[] rrvr) {
        this.rrvr = rrvr;
    }

    /**
     * @return the dvr
     */
    public DispatchVisRange getDvr() {
        return dvr;
    }

    /**
     * @param dvr
     *            the dvr to set
     */
    public void setDvr(DispatchVisRange dvr) {
        this.dvr = dvr;
    }

    /**
     * @return the recentWeathers
     */
    public RecentWx[] getRecentWeathers() {
        return recentWeathers;
    }

    /**
     * @param recentWeathers
     *            the recentWeathers to set
     */
    public void setRecentWeathers(RecentWx[] recentWeathers) {
        this.recentWeathers = recentWeathers;
    }

    /**
     * @return the significantClouds
     */
    public SignificantCloud[] getSignificantClouds() {
        return significantClouds;
    }

    /**
     * @param significantClouds
     *            the significantClouds to set
     */
    public void setSignificantClouds(SignificantCloud[] significantClouds) {
        this.significantClouds = significantClouds;
    }

    /**
     * @return the cmnData
     */
    public PedCmn getCmnData() {
        return cmnData;
    }

    /**
     * @param cmnData
     *            the cmnData to set
     */
    public void setCmnData(PedCmn cmnData) {
        this.cmnData = cmnData;
    }
}
