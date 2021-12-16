package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.util.DtgUtil;

/**
 * BaseFDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2019            pwang       Initial creation
 * Sep 12, 2019 68523      jwu         Cleanup.
 * Apr 16, 2020 71986      mporricelli Added sorting of f-deck
 *                                     records for creating deck files
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@MappedSuperclass
public class BaseFDeckRecord extends AbstractDeckRecord {

    private static final long serialVersionUID = 1L;

    private static final String EMPTY = "";

    private static final String BLANK = " ";

    private static final String SEP = ", ";

    private static final String SUBJECTIVE_DVORAK_FIX_FORMAT = "10";

    private static final String OBJECTIVE_DVORAK_FIX_FORMAT = "20";

    private static final String MICROWAVE_FIX_FORMAT = "30";

    private static final String SCATTEROMETER_FIX_FORMAT = "31";

    private static final String RADAR_FIX_FORMAT = "40";

    private static final String AIRCRAFT_FIX_FORMAT = "50";

    private static final String DROPSONDE_FIX_FORMAT = "60";

    private static final String ANALYSIS_FIX_FORMAT = "70";

    /*****************
     * COMMON FIELDS *
     *****************/

    /**
     * @formatter:off Fix Format (3 characters or 2 digits) 10 - subjective
     *                dvorak 20 - objective dvorak 30 - microwave 31 -
     *                scatterometer 40 - radar 50 - aircraft 60 - dropsonde 70 -
     *                analysis
     * @formatter:on
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String fixFormat = EMPTY;

    /**
     * @formatter:off Fix Type (4 characters) DVTS - subjective Dvorak DVTO -
     *                objective Dvorak SSMI - Special Sensor Microwave Imager
     *                SSMS - SSM/IS (Shared Processing Program) QSCT - QuikSCAT
     *                ERS2 - European Remote Sensing System scatterometer SEAW -
     *                SeaWinds scatterometer TRMM - Tropical Rainfall Measuring
     *                Mission microwave and vis/ir imager WIND - WindSat,
     *                polarimetric microwave radiometer MMHS - Microwave
     *                Humidity Sounder ALTG - GEOSAT altimeter AMSU - Advanced
     *                Microwave Sounding Unit RDRC - conventional radar RDRD -
     *                doppler radar RDRT - TRMM radar SYNP - synoptic AIRC -
     *                aircraft ANAL - analysis DRPS - dropsonde UNKN - unknown
     * @formatter:on
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String fixType = EMPTY;

    /**
     * Center/Intensity (10 characters; 1 or more of C I R P F)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String centerOrIntensity = EMPTY;

    /**
     * Flagged Indicator (1 character; 0 or 1 of C I R P F)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String flaggedIndicator = EMPTY;

    /**
     * Height of observation - 0-99999 meters
     */
    @DynamicSerializeElement
    private float obHeight = RMISSD;

    /**
     * Position Confidence (1 character; 1-good, 2-fair, 3-poor)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String positionConfidence = EMPTY;

    /**
     * Maximum sustained wind speed in knots: 0 through 300
     */
    @DynamicSerializeElement
    private float windMax = RMISSD;

    /**
     * Wind speed Confidence (1 character; 1-good, 2-fair, 3-poor)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String windMaxConfidence = EMPTY;

    /**
     * Minimum sea level pressure, 1 through 1100MB
     */
    @DynamicSerializeElement
    private float mslp = RMISSD;

    /**
     * Pressure Confidence (1 character; 1-good, 2-fair, 3-poor)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String pressureConfidence = EMPTY;

    /**
     * @formatter:off Pressure Derivation (4 characters) DVRK - dvorak AKHL -
     *                atkinson-holiday table XTRP - extrapolated MEAS - measured
     * @formatter:on
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String pressureDerivation = EMPTY;

    /**
     * Wind intensity (kts) for the radii defined i this record: 34, 50, 64
     */
    @DynamicSerializeElement
    private float radWind = RMISSD;

    /**
     * Radius Code for wind intensity: AAA = full circle; QQQ = quadrant (NNQ,
     * NEQ, EEQ, SEQ, SSQ, SWQ, WWQ, NWQ)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radWindQuad = EMPTY;

    /**
     * If full circle, radius of specified wind intensity. If semicircle or
     * quadrant, radius of specified wind intensity of circle portion specified
     * in radius code. 0 - 1200 nm.
     */
    @DynamicSerializeElement
    private float quad1WindRad = RMISSD;

    /**
     * If full circle, this field not used. If semicircle, radius (nm) of
     * specified wind intensity for semicircle not specified in radius code. If
     * quadrant, radius (nm) of specified wind intensity for 2nd quadrant
     * (counting clockwise from quadrant specified in radius code). 0 - 1200 nm.
     */
    @DynamicSerializeElement
    private float quad2WindRad = RMISSD;

    /**
     * If full circle or semicircle this field not used. If quadrant, radius
     * (nm) of specified wind intensity for 3rd quadrant (counting clockwise
     * from quadrant specified in radius code). 0 - 1200 nm.
     */
    @DynamicSerializeElement
    private float quad3WindRad = RMISSD;

    /**
     * If full circle or semicircle this field not used. If quadrant, radius
     * (nm) of specified wind intensity for 4th quadrant (counting clockwise
     * from quadrant specified in radius code). 0 - 1200 nm.
     */
    @DynamicSerializeElement
    private float quad4WindRad = RMISSD;

    /**
     * RadMod1 (1 character; E-Edge of Pass, C-Cut off by Land, B-Both)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radMod1 = EMPTY;

    /**
     * RadMod2 (1 character; E-Edge of Pass, C-Cut off by Land, B-Both)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radMod2 = EMPTY;

    /**
     * RadMod3 (1 character; E-Edge of Pass, C-Cut off by Land, B-Both)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radMod3 = EMPTY;

    /**
     * RadMod4 (1 character; E-Edge of Pass, C-Cut off by Land, B-Both)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radMod4 = EMPTY;

    /**
     * Radii Confidence (1 character; 1-good, 2-fair, 3-poor)
     */
    @DynamicSerializeElement
    private String radiiConfidence = EMPTY;

    /**
     * Radius of max winds. 0 - 999 nm
     */
    @DynamicSerializeElement
    private float radiusOfMaximumWind = RMISSD;

    /**
     * Eye diameter. 0 - 999nm
     */
    @DynamicSerializeElement
    private float eyeSize = RMISSD;

    /**
     * @formatter:off Subregion code (1 character): A - Arabian Sea B - Bay of
     *                Bengal C - Central Pacific E - Eastern Pacific L -
     *                Atlantic P - South Pacific (135E - 120W) Q - South
     *                Atlantic S - South IO (20E - 135E) W - Western Pacific
     * @formatter:on
     */
    @DynamicSerializeElement
    private String subRegion = EMPTY;

    /**
     * @formatter:off Fix site/WMO Identifier (5 characters)
     * @formatter:on
     */
    @DynamicSerializeElement
    private String fixSite = EMPTY;

    /**
     * @formatter:off Initials - 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private String initials = EMPTY;

    /**
     * @formatter:off Comments - 52 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private String comments = EMPTY;

    /****************************************************
     * FIELDS COMMON TO MORE THAN 1 (BUT NOT ALL) TYPES *
     ****************************************************/

    /**
     * @formatter:off Satellite type - 6 char. GMS, DMSP, DMSP45, TRMM, NOAA...
     * @formatter:on
     */
    @DynamicSerializeElement
    private String satelliteType = EMPTY;

    /**
     * @formatter:off Sensor type - vis, ir, microwave... 4 char. V, I, M, VI,
     *                IM, VM, VIM
     * @formatter:on
     */
    @DynamicSerializeElement
    private String sensorType = EMPTY;

    /**
     * @formatter:off Tropical indicator - 1 char. S-subtropical,
     *                E-extratropical, T-tropical
     * @formatter:on
     */
    @DynamicSerializeElement
    private String tropicalIndicator = EMPTY;

    /**
     * @formatter:off Rad - Wind intensity (kts) - 34, 50, 64 3 char. for the
     *                radii defined in this record.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float radiusOfWindIntensity = RMISSD;

    /**
     * @formatter:off Windcode - Radius code: 4 char. AAA - full circle quadrant
     *                designations: NEQ - northeast quadrant octant
     *                designations: XXXO - octants (NNEO, ENEO, ESEO, SSEO,
     *                SSWO, WSWO, WNWO NNWO)
     * @formatter:on
     */
    @DynamicSerializeElement
    private String windCode = EMPTY;

    /**
     * @formatter:off Rad1 - If full circle, radius of specified wind intensity.
     *                Otherwise radius of specified wind intensity of wind
     *                intensity of circle portion specified in windcode. 0 -
     *                1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad1 = RMISSD;

    /**
     * @formatter:off Rad2 - Radius of specified wind intensity for 2nd
     *                quadrant/octant (counting clockwise from quadrant
     *                specified in windcode). 0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad2 = RMISSD;

    /**
     * @formatter:off Rad3 - Radius of specified wind intensity for 3rd
     *                quadrant/octant (counting clockwise from quadrant
     *                specified in windcode). 0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad3 = RMISSD;

    /**
     * @formatter:off Rad4 - Radius of specified wind intensity for 4th
     *                quadrant/octant (counting clockwise from quadrant
     *                specified in windcode). 0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad4 = RMISSD;

    /**
     * @formatter:off Rad5 - Radius of specified wind intensity for 5th octant
     *                (counting clockwise from quadrant specified in windcode).
     *                0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad5 = RMISSD;

    /**
     * @formatter:off Rad6 - Radius of specified wind intensity for 6th octant
     *                (counting clockwise from quadrant specified in windcode).
     *                0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad6 = RMISSD;

    /**
     * @formatter:off Rad7 - Radius of specified wind intensity for 7th octant
     *                (counting clockwise from quadrant specified in windcode).
     *                0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad7 = RMISSD;

    /**
     * @formatter:off Rad8 - Radius of specified wind intensity for 8th octant
     *                (counting clockwise from quadrant specified in windcode).
     *                0 - 1200 nm. 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float windRad8 = RMISSD;

    /**
     * @formatter:off RadMod5 - E-Edge of Pass, C-Cut off by Land, B-Both 1
     *                char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radMod5 = EMPTY;

    /**
     * @formatter:off RadMod6 - E-Edge of Pass, C-Cut off by Land, B-Both 1
     *                char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radMod6 = EMPTY;

    /**
     * @formatter:off RadMod7 - E-Edge of Pass, C-Cut off by Land, B-Both 1
     *                char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radMod7 = EMPTY;

    /**
     * @formatter:off RadMod8 - E-Edge of Pass, C-Cut off by Land, B-Both 1
     *                char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radMod8 = EMPTY;

    /**
     * Microwave Radii Confidence (1 character; 1-good, 2-fair, 3-poor)
     */
    @Column(length = 4)
    @DynamicSerializeElement
    private String microwaveRadiiConfidence = EMPTY;

    /**
     * @formatter:off Eye - Shape: CI-circ.; EL-elliptic; CO-concentric 2 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String eyeShape = EMPTY;

    /**
     * @formatter:off Eye - Orientation - degrees 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float eyeOrientation = RMISSD;

    /**
     * @formatter:off Eye - Diameter (long axis if elliptical) - NM 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float eyeDiameterNM = RMISSD;

    /**
     * @formatter:off Eye - Short Axis (blank if not elliptical) - NM 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float eyeShortAxis = RMISSD;

    /**
     * @formatter:off Sea Surface Temperature 0 to 40 Celsius 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float seaSurfaceTemp = RMISSD;

    /********************
     * SATELLITE - DVTS *
     ********************/

    // sensorType

    /**
     * @formatter:off PCN code (deprecated) - 1 char. 1 = Eye/Geography 2 =
     *                Eye/Ephemeris 3 = Well Defined Circ. Center/Geography 4 =
     *                Well Defined Circ. Center/Ephemeris 5 = Poorly Defined
     *                Circ. Center/Geography 6 = Poorly Defined Circ.
     *                Center/Ephemeris
     * @formatter:on
     */
    @DynamicSerializeElement
    private String pcnCode = EMPTY;

    /**
     * @formatter:off Dvorak code - long term trend, 10 char. (18-30 hour change
     *                in t-number) T num, none or 0.0 - 8.0 CI num, none or 0.0
     *                - 8.0 Forecast intensity change, + - or blank Past change
     *                - developed, steady, weakened (long term trend) Amount of
     *                t num change, none or 0.0 - 8.0 Hours since previous eval,
     *                18 - 30 HRS Example: T4.0/4.0+/D1.0/24HRS Entry:
     *                4040+D1024
     * @formatter:on
     */
    @Column(length = 10)
    @DynamicSerializeElement
    private String dvorakCodeLongTermTrend = EMPTY;

    /**
     * @formatter:off Dvorak code - short term trend, 5 char. ( < 18 hour change
     *                in t-number. Only used when significant difference from
     *                long term trend, i.e., LTT is +1.0 strengthening, but STT
     *                over 6 hours shows - 0.5 weakening.) Past change -
     *                developed, steady, weakened (short term trend) Amount of t
     *                num change, none or 0.0 - 8.0 1 since previous eval, 0 -
     *                17 HRS Example: W0.5/06HRS Entry: W0506
     * @formatter:on
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String dvorakCodeShortTermTrend = EMPTY;

    /**
     * @formatter:off CI 24 hr forecast - none or 0.0 - 8.0 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float ci24hourForecast = RMISSD;

    // satelliteType

    /**
     * @formatter:off center type - CSC, LLCC, ULCC 4 char. LLCC - lower level
     *                cloud center ULCC - upper level cloud center CSC - cloud
     *                system center
     * @formatter:on
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String centerType = EMPTY;

    // tropicalIndicator

    // comments

    /***********************************************************
     * SATELLITE - DVTO * Objective Dvorak Technique Data (IR) *
     ***********************************************************/

    /**
     * @formatter:off CI num - 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float ciNum = RMISSD;

    /**
     * @formatter:off CI confidence - 1-good, 2-fair, 3-poor, 1 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String ciConfidence = EMPTY;

    /**
     * @formatter:off T num (average) 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tNumAverage = RMISSD;

    /**
     * @formatter:off T num averaging time period - hours 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tNumAveragingTimePeriod = RMISSD;

    /**
     * @formatter:off T num averaging derivation - 1 char. L-straight linear,
     *                T-time weighted
     * @formatter:on
     */
    @DynamicSerializeElement
    private String tNumAveragingDerivation = EMPTY;

    /**
     * @formatter:off T num (raw) 2 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String tNumRaw = EMPTY;

    /**
     * @formatter:off Temperature (eye), -99 - 50 celsius 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tempEye = RMISSD;

    /**
     * @formatter:off Temperature (cloud surrounding eye) - celsius 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tempCloudSurroundingEye = RMISSD;

    /**
     * @formatter:off Scene type - CDO, EYE, EEYE, SHER... 4 char. CDO - central
     *                dense overcast EYE - definable eye EMBC - embedded center
     *                SHER - partially exposed eye due to strong wind shear with
     *                asymmetric convective structure
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String sceneType = EMPTY;

    /**
     * @formatter:off Algorithm (Rule 9 flag, Rapid flag) - R9, RP 2 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String algorithm = EMPTY;

    /**************************************************************************
     * SATELLITE - MICROWAVE * SSMI, TRMM, AMSU, ADOS, ALTI, ERS2, QSCT, SEAW *
     **************************************************************************/

    /**
     * @formatter:off Rain flagged - "R" or blank 1 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String rainFlag = BLANK;

    /**
     * @formatter:off Rainrate - 0-500 mm/h 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float rainRate = RMISSD;

    /**
     * @formatter:off Process - 6 char. FNMOC algorithm, NESDIS algorithm,
     *                RSS...
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String process = EMPTY;

    /**
     * @formatter:off Wave height (active micr) - 0-99 ft 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float waveHeight = RMISSD;

    /**
     * @formatter:off Temp (passive micr) - celsius 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tempPassiveMicrowave = RMISSD;

    /**
     * @formatter:off SLP (raw, AMSU only) - mb 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float slpRaw = RMISSD;

    /**
     * @formatter:off SLP (retrieved, AMSU only) - mb 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float slpRetrieved = RMISSD;

    /**
     * @formatter:off Max Seas - (alti) 0-999 ft. 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxSeas = RMISSD;

    /**
     * @formatter:off Satellite type - 6 char. GMS, DMSP, DMSP45, TRMM, NOAA...
     * @formatter:on
     */

    /*********************************************************************
     * RADAR * RDRC, RDRD, RDRT -- Radar, Conventional, Doppler and TRMM *
     *********************************************************************/

    /**
     * @formatter:off Radar Type: 1 char. L - Land; S - Ship; A - Aircraft; T -
     *                Satellite
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radarType = EMPTY;

    /**
     * @formatter:off Radar Format: 1 char. R - RADOB; P - plain language; D -
     *                Doppler
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radarFormat = EMPTY;

    /**
     * @formatter:off RADOB CODE - A S W a r t d d f f 10 char. c c c c t e s s
     *                s s See description below. Also enter slashes if reported
     *                and in place of blanks.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radobCode = EMPTY;

    /**
     * @formatter:off Percent of Eye Wall Observed (99 = 100%) - 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float percentOfEyewallObserved = RMISSD;

    /**
     * @formatter:off Spiral Overlay (degrees) - 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float spiralOverlayDegrees = RMISSD;

    /**
     * @formatter:off Radar Site Position Lat N/S - 5 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float radarSitePosLat = RMISSD;

    /**
     * @formatter:off Radar Site Position Lon E/W - 6 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float radarSitePosLon = RMISSD;

    /**
     * @formatter:off Inbound Max Wind - 0-300 kts 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float inboundMaxWindSpeed = RMISSD;

    /**
     * @formatter:off Inbound Max Wind - Azimuth - degrees, 1-360 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float inboundMaxWindAzimuth = RMISSD;

    /**
     * @formatter:off Inbound Max Wind - Range - less than 400 nm, 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float inboundMaxWindRangeNM = RMISSD;

    /**
     * @formatter:off Inbound Max Wind - Elevation - feet 5 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float inboundMaxWindElevationFeet = RMISSD;

    /**
     * @formatter:off Outbound Max Wind - 0-300 kts 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float outboundMaxWindSpeed = RMISSD;

    /**
     * @formatter:off Outbound Max Wind - Azimuth - degrees, 1-360 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float outboundMaxWindAzimuth = RMISSD;

    /**
     * @formatter:off Outbound Max Wind - Range - less than 400 nm, 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float outboundMaxWindRangeNM = RMISSD;

    /**
     * @formatter:off Outbound Max Wind - Elevation - feet 5 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float outboundMaxWindElevationFeet = RMISSD;

    /**
     * @formatter:off Max Cloud Height (trmm radar) - 70,000ft 5 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxCloudHeightFeet = RMISSD;

    /*
     * Rain accumulation:
     */

    /**
     * @formatter:off Rain accumulation - Max. rain accumulation, hundredths of
     *                inches 0-10000 5 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxRainAccumulation = RMISSD;

    /**
     * @formatter:off Rain accumulation - Time interval, 1 - 120 hours 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float rainAccumulationTimeInterval = RMISSD;

    /**
     * @formatter:off Rain accumulation - Lat N/S - Latitude (hundredths of
     *                degrees), 0-9000 5 char. N/S is the hemispheric index.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float rainAccumulationLat = RMISSD;

    /**
     * @formatter:off Rain accumulation - Lon E/W - Longitude (hundredths of
     *                degrees), 0-18000 6 char. E/W is the hemispheric index.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float rainAccumulationLon = RMISSD;

    /*
     * Note: The greater of the two inbound and outbound winds should be
     * assigned to the V field. It is not mandatory to have both inbound and
     * outbound winds.
     */

    /**
     * @formatter:off
     *
     *                RADAR OBSERVATION CODE description
     *
     *                10314 30222 AcScWcacrt tedsdsfsfs Char Mvmnt
     *
     *                Char. AcScWcacrt. Coded characteristics of the eye of the
     *                system Ac--Accuracy of the position of the center or the
     *                eye Code 1 Eye visible on radar scope, accuracy good
     *                (within 10km). 2 Eye visible on radar scope, accuracy fair
     *                (within 30 km). 3 Eye visible on radar scope, accuracy
     *                poor (within 50 km). 4 Position of the center within the
     *                area covered by the radar scope, determination by means of
     *                the spiral-band overlay, accuracy good (within 10km). 5
     *                Position of the center within the area covered by the
     *                radar scope, determination by means of the spiral-band
     *                overlay, accuracy fair (within 30km). 6 Position of the
     *                center within the area covered by the radar scope,
     *                determination by means of the spiral-band overlay,
     *                accuracy poor (within 50km). 7 Position of the center
     *                outside the area covered by the radar scope, extrapolation
     *                by means of the spiral-band overlay. / Accuracy
     *                undetermined.
     *
     *                Sc--Shape and definition of the eye. Code 0 Circular. 1
     *                Elliptical-the minor axis at least 3/4 the length of the
     *                major axis. 2 Elliptical-the minor axis is less than 3/4
     *                the length of the major axis. 3 Apparent double eye. 4
     *                Other shape. 5 Ill defined. / Undetermined.
     *
     *                Wc--Diameter or length of axis of the eye. Code 0 Less
     *                than 5 km. 1 5 to less than 10 km. 2 10 to less than 15
     *                km. 3 15 to less than 20 km. 4 20 to less than 25 km. 5 25
     *                to less than 30 km. 6 30 to less than 35 km. 7 35 to less
     *                than 40 km. 8 40 to less than 50 km. 9 50 km and greater.
     *                / Undetermined.
     *
     *                ac--Change in character of the eye during the past 30
     *                minutes preceding the time of observation. Code 0 Eye has
     *                first become visible during the past 30 minutes. 1 No
     *                significant change in characteristics or size of the eye.
     *                2 Eye has become smaller with no other significant change
     *                in characteristics. 3 Eye has become larger with no other
     *                significant change in characterisitcs. 4 Eye has become
     *                less distinct, no significant change in size. 5 Eye has
     *                become less distinct and decreased in size. 6 Eye has
     *                become less distinct and increased in size. 7 Eye has
     *                become more distinct, no significant change in size. 8 Eye
     *                has become more distinct and decreased in size. 9 Eye has
     *                become more distinct and increased in size. / Change in
     *                character and size of eye cannot be determined.
     *
     *                rt--Distance between the end of the outermost spiral band
     *                and the center. Code 0 0 to less than 100 km. 1 100 to
     *                less than 200 km. 2 200 to less than 300 km. 3 300 to less
     *                than 400 km. 4 400 to less than 500 km. 5 500 to less than
     *                600 km. 6 600 to less than 800 km. 7 800 km or more. /
     *                Doubtful or undetermined.
     *
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radarObservationCodeCharacteristics = EMPTY;

    /**
     * @formatter:off Mvmnt. tedsdsfsfs. Movement of the system. te--Time
     *                interval for which the system has been evaluated.
     *                dsds--The direction in tens of degrees toward which the
     *                system is moving. fsfs--The speed in knots at which the
     *                system is moving.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String radarObservationCodeMovement = EMPTY;

    /************
     * AIRCRAFT *
     ************/

    /**
     * @formatter:off Flight Level - 100's of feet 2 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float flightLevel100Feet = RMISSD;

    /**
     * @formatter:off Flight Level - millibars 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float flightLevelMillibars = RMISSD;

    /**
     * @formatter:off Flight Level -? Minimum height - meters 4 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float flightLevelMinimumHeightMeters = RMISSD;

    /**
     * @formatter:off Maximum Surface Wind (inbound leg) Intensity - kts 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxSurfaceWindInboundLegIntensity = RMISSD;

    /**
     * @formatter:off Bearing - degrees 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxSurfaceWindInboundLegBearing = RMISSD;

    /**
     * @formatter:off Range - nm 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxSurfaceWindInboundLegRangeNM = RMISSD;

    /**
     * @formatter:off Maximum Flight Level Wind (inbound leg) - Direction -
     *                degrees 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxFLWindInboundDirection = RMISSD;

    /**
     * @formatter:off Maximum Flight Level Wind (inbound leg) - Intensity - kts
     *                3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxFLWindInboundIntensity = RMISSD;

    /**
     * @formatter:off Maximum Flight Level Wind (inbound leg) - Bearing -
     *                degrees 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxFLWindInboundBearing = RMISSD;

    /**
     * @formatter:off Maximum Flight Level Wind (inbound leg) - Range - NM 3
     *                char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float maxFLWindInboundRangeNM = RMISSD;

    /**
     * @formatter:off Temperature Outside Eye -99 to 99 Celsius 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tempOutsideEye = RMISSD;

    /**
     * @formatter:off Temperature Inside Eye -99 to 99 Celsius 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float tempInsideEye = RMISSD;

    /**
     * @formatter:off Dew Point Temperature -99 to 99 Celsius 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float dewPointTemp = RMISSD;

    /**
     * @formatter:off Eye Character or Wall Cld Thickness (pre-2015) 2 char. NA
     *                - < 50% eyewall CL - Closed Wall PD - Poorly Defined N -
     *                open North NE - open Northeast E - open East SE - open
     *                Southeast S - open South SW - open Southwest W - open West
     *                NW - open Northwest SB - spiral band
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String eyeCharacterOrWallCloudThickness = EMPTY;

    /**
     * Eye Shape/Orientation/Diameter
     */

    /**
     * @formatter:off Accuracy - Navigational - tenths of nm 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float accuracyNavigational = RMISSD;

    /**
     * @formatter:off Accuracy - Meteorological - tenths of nm 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float accuracyMeteorological = RMISSD;

    /**
     * @formatter:off Mission Number 2 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String missionNumber = EMPTY;

    /**********************************
     * DROPSONDE * DRPS -- Dropsondes *
     **********************************/

    /**
     * @formatter:off Sonde environment - 10 char. EYEWALL, EYE, RAINBAND,
     *                MXWNDBND, SYNOPTIC
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String sondeEnvironment = EMPTY;

    /**
     * @formatter:off Height of midpoint over lowest 150 m of drop, meters (75 -
     *                999 m) 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float heightMidpointLowest150m = RMISSD;

    /**
     * @formatter:off Speed of mean wind, lowest 150 m of drop - kts 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float speedMeanWindLowest150mKt = RMISSD;

    /**
     * @formatter:off Speed of mean wind, 0-500 m layer - kts 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float speedMeanWind0to500mKt = RMISSD;

    /**********************************************************************
     * ANALYSIS * * ANAL -- Analysis( HRD, personal, aircraft, model(GFD) *
     **********************************************************************/

    /**
     * @formatter:off Analyst initials - 3 char.
     * @formatter:on
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String analysisInitials = EMPTY;

    /**
     * @formatter:off Start time - YYYYMMDDHHMM 12 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private Date startTime = null;

    /**
     * @formatter:off End time - YYYYMMDDHHMM 12 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private Date endTime = null;

    /**
     * @formatter:off Distance to Nearest Data, 0 - 300 nm 3 char.
     * @formatter:on
     */
    @DynamicSerializeElement
    private float distanceToNearestDataNM = RMISSD;

    /**
     * @formatter:off Observation sources - 24 char. b - buoy, l - land station,
     *                m - SSMI, c - scat, t - trmm, i - ir, v - vis, p - ship, d
     *                - dropsonde, a - aircraft, r - radar, x - other 1 char.
     *                identifier for each ob source, concatenate the selected 1
     *                char. identifiers to list all of the sources.
     * @formatter:on
     */
    @Column(length = 80)
    @DynamicSerializeElement
    private String observationSources = EMPTY;

    /**
     * Default Constructor
     */
    public BaseFDeckRecord() {
        this.setReportType("ATCF_FDECK");
    }

    /**
     * @formatter:off Constructs a CSV string representation of the data in the
     *                AtcfFDeckRecord compatible with ATCF "deck" files. Sample
     *                F-deck line (wrapped):
     *
     *                AL, 14, 201609250545, 10, DVTS, CI, , 770N, 3520W, , 3,
     *                25, 2, , 2, DVRK, , , , , , , , , , , , , , L, TAFB, ASL,
     *                I, 5, 1010 /////, , , MET10, CSC, T, A
     * @formatter:on
     */
    public String toFDeckString() {

        StringBuilder sb = new StringBuilder();

        // The first 32 common fields
        append(sb, 2, getBasin());
        // use leading zero
        appendLZ(sb, 2, getCycloneNum());
        // 12 chars
        appendDTGLong(sb, getRefTime());
        append(sb, 3, fixFormat);
        append(sb, 4, fixType);
        append(sb, 10, centerOrIntensity);
        append(sb, 1, flaggedIndicator);
        // 5 chars
        appendLatitude(sb, getClat());
        // 6 chars
        appendLongitude(sb, getClon());
        append(sb, 5, obHeight);
        append(sb, 1, positionConfidence);
        append(sb, 3, windMax);
        append(sb, 1, windMaxConfidence);
        append(sb, 4, mslp);
        append(sb, 1, pressureConfidence);
        append(sb, 4, pressureDerivation);
        append(sb, 3, radWind);
        append(sb, 4, radWindQuad);
        append(sb, 4, quad1WindRad);
        append(sb, 4, quad2WindRad);
        append(sb, 4, quad3WindRad);
        append(sb, 4, quad4WindRad);
        append(sb, 1, radMod1);
        append(sb, 1, radMod2);
        append(sb, 1, radMod3);
        append(sb, 1, radMod4);
        append(sb, 1, radiiConfidence);
        append(sb, 3, radiusOfMaximumWind);
        append(sb, 3, eyeDiameterNM);
        append(sb, 1, subRegion);
        append(sb, 5, fixSite);
        append(sb, 3, initials);

        // Fields based on fix format
        if (fixFormat.equalsIgnoreCase(SUBJECTIVE_DVORAK_FIX_FORMAT)) {
            appendSubjectiveDvorakFields(sb);
        } else if (fixFormat.equalsIgnoreCase(OBJECTIVE_DVORAK_FIX_FORMAT)) {
            appendObjectiveDvorakFields(sb);
        } else if (fixFormat.equalsIgnoreCase(MICROWAVE_FIX_FORMAT)) {
            appendMicrowaveFields(sb);
        } else if (fixFormat.equalsIgnoreCase(SCATTEROMETER_FIX_FORMAT)) {
            appendMicrowaveFields(sb);
        } else if (fixFormat.equalsIgnoreCase(RADAR_FIX_FORMAT)) {
            appendRadarFields(sb);
        } else if (fixFormat.equalsIgnoreCase(AIRCRAFT_FIX_FORMAT)) {
            appendAircraftFields(sb);
        } else if (fixFormat.equalsIgnoreCase(DROPSONDE_FIX_FORMAT)) {
            appendDropsondeFields(sb);
        } else if (fixFormat.equalsIgnoreCase(ANALYSIS_FIX_FORMAT)) {
            appendAnalysisFields(sb);
        }

        return sb.toString();
    }

    /*
     * Constructs a CSV string representation of Subjective Dvorak fields in an
     * F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendSubjectiveDvorakFields(StringBuilder sb) {
        append(sb, 4, sensorType);
        append(sb, 1, pcnCode);
        append(sb, 10, dvorakCodeLongTermTrend);
        append(sb, 5, dvorakCodeShortTermTrend);
        append(sb, 2, ci24hourForecast);
        append(sb, 6, satelliteType);
        append(sb, 4, centerType);
        if (tropicalIndicator.length() > 0 || comments.length() > 0) {
            append(sb, 1, tropicalIndicator);
        }
        // 52 chars
        sb.append(comments);
    }

    /*
     * Constructs a CSV string representation of objective Dvorak fields in an
     * F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendObjectiveDvorakFields(StringBuilder sb) {
        append(sb, 4, sensorType);
        append(sb, 2, ciNum);
        append(sb, 1, ciConfidence);
        append(sb, 2, tNumAverage);
        append(sb, 3, tNumAveragingTimePeriod);
        append(sb, 1, tNumAveragingDerivation);
        append(sb, 2, tNumRaw);
        append(sb, 4, tempEye);
        append(sb, 4, tempCloudSurroundingEye);
        append(sb, 4, sceneType);
        append(sb, 2, algorithm);
        append(sb, 6, satelliteType);
        append(sb, 1, tropicalIndicator);
        // 52 char
        sb.append(comments);
    }

    /*
     * Constructs a CSV string representation of microwave/scatterometer fields
     * in an F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendMicrowaveFields(StringBuilder sb) {
        append(sb, 1, rainFlag);
        append(sb, 3, rainRate);
        append(sb, 6, process);
        append(sb, 2, waveHeight);
        append(sb, 4, tempPassiveMicrowave);
        append(sb, 4, slpRaw);
        append(sb, 4, slpRetrieved);
        append(sb, 3, maxSeas);
        append(sb, 6, satelliteType);
        append(sb, 3, radiusOfWindIntensity);
        append(sb, 4, windCode);
        append(sb, 4, windRad1);
        append(sb, 4, windRad2);
        append(sb, 4, windRad3);
        append(sb, 4, windRad4);
        append(sb, 4, windRad5);
        append(sb, 4, windRad6);
        append(sb, 4, windRad7);
        append(sb, 4, windRad8);
        append(sb, 1, radMod1);
        append(sb, 1, radMod2);
        append(sb, 1, radMod3);
        append(sb, 1, radMod4);
        append(sb, 1, radMod5);
        append(sb, 1, radMod6);
        append(sb, 1, radMod7);
        append(sb, 1, radMod8);
        append(sb, 1, microwaveRadiiConfidence);
        // 52 char
        sb.append(comments);
    }

    /*
     * Constructs a CSV string representation of radar fields in an F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendRadarFields(StringBuilder sb) {
        append(sb, 1, radarType);
        append(sb, 1, radarFormat);
        append(sb, 10, radobCode);
        append(sb, 2, eyeShape);
        append(sb, 2, percentOfEyewallObserved);
        append(sb, 2, spiralOverlayDegrees);
        // 5 chars
        appendLatitude(sb, radarSitePosLat);
        // 6 chars
        appendLongitude(sb, radarSitePosLon);
        append(sb, 3, inboundMaxWindSpeed);
        append(sb, 3, inboundMaxWindAzimuth);
        append(sb, 3, inboundMaxWindRangeNM);
        append(sb, 5, inboundMaxWindElevationFeet);
        append(sb, 3, outboundMaxWindSpeed);
        append(sb, 3, outboundMaxWindAzimuth);
        append(sb, 3, outboundMaxWindRangeNM);
        append(sb, 5, outboundMaxWindElevationFeet);
        append(sb, 5, maxCloudHeightFeet);
        append(sb, 5, maxRainAccumulation);
        append(sb, 3, rainAccumulationTimeInterval);
        // 5 chars
        appendLatitude(sb, rainAccumulationLat);
        // 6 chars
        appendLongitude(sb, rainAccumulationLon);
        // 52 chars
        sb.append(comments);
    }

    /*
     * Constructs a CSV string representation of Aircraft fields in an F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendAircraftFields(StringBuilder sb) {
        append(sb, 2, flightLevel100Feet);
        append(sb, 3, flightLevelMillibars);
        append(sb, 4, flightLevelMinimumHeightMeters);
        append(sb, 3, maxSurfaceWindInboundLegIntensity);
        append(sb, 3, maxSurfaceWindInboundLegBearing);
        append(sb, 3, maxSurfaceWindInboundLegRangeNM);
        append(sb, 3, maxFLWindInboundDirection);
        append(sb, 3, maxFLWindInboundIntensity);
        append(sb, 3, maxFLWindInboundBearing);
        append(sb, 3, maxFLWindInboundRangeNM);
        append(sb, 4, mslp);
        append(sb, 3, tempOutsideEye);
        append(sb, 3, tempInsideEye);
        append(sb, 3, dewPointTemp);
        append(sb, 2, seaSurfaceTemp);
        append(sb, 2, eyeCharacterOrWallCloudThickness);
        append(sb, 2, eyeShape);
        append(sb, 3, eyeOrientation);
        append(sb, 2, eyeDiameterNM);
        append(sb, 2, eyeShortAxis);
        append(sb, 3, accuracyNavigational);
        append(sb, 3, accuracyMeteorological);
        append(sb, 2, missionNumber);
        // 52 chars
        sb.append(comments);
    }

    /*
     * Constructs a CSV string representation of dropsonde fields in an F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendDropsondeFields(StringBuilder sb) {
        append(sb, 10, sondeEnvironment);
        append(sb, 3, heightMidpointLowest150m);
        append(sb, 3, speedMeanWindLowest150mKt);
        append(sb, 3, speedMeanWind0to500mKt);
        // 52 chars
        sb.append(comments);
    }

    /*
     * Constructs a CSV string representation of analysis/synoptic fields in an
     * F-Deck.
     *
     * @param sb StringBuilder
     */
    private void appendAnalysisFields(StringBuilder sb) {
        append(sb, 3, analysisInitials);
        // 12 chars for start and end times
        appendDTGLong(sb, startTime);
        appendDTGLong(sb, endTime);
        append(sb, 3, distanceToNearestDataNM);
        append(sb, 4, seaSurfaceTemp);
        append(sb, 24, observationSources);
        // 52 chars
        sb.append(comments);
    }

    /*
     * Append a String value
     */
    private void append(StringBuilder sb, int length, String value) {
        String formats = "%" + length + "s";
        if (value != null) {
            sb.append(String.format(formats, value));
        }
        sb.append(SEP);
    }

    /*
     * Append an int value with leading zeroes
     */
    private void appendLZ(StringBuilder sb, int length, int value) {
        String formats = "%" + length + "s";
        String formatd = "%0" + length + "d";
        if (value == IMISSD) {
            sb.append(String.format(formats, BLANK));
        } else {
            sb.append(String.format(formatd, value));
        }
        sb.append(SEP);
    }

    /*
     * Append a float value (as int)
     */
    private void append(StringBuilder sb, int length, float value) {
        String formats = "%" + length + "s";
        String formatd = "%" + length + "d";
        if (value == RMISSD) {
            sb.append(String.format(formats, BLANK));
        } else {
            sb.append(String.format(formatd, (int) value));
        }
        sb.append(SEP);
    }

    /*
     * Append a latitude value with formatting
     */
    private void appendLatitude(StringBuilder sb, float value) {
        // LAT format; example: 26.800001 -> "2680N"
        if (value == RMISSD) {
            sb.append("     ");
        } else {
            sb.append(
                    String.format("%4s", Math.round((Math.abs(value) * 100))));
            sb.append(value >= 0.0f ? "N" : "S");
        }
        sb.append(SEP);
    }

    /*
     * Append a longitude value with formatting
     */
    private void appendLongitude(StringBuilder sb, float value) {
        // LON format; example: -79.200005 -> " 7920W"
        if (value == RMISSD) {
            sb.append("      ");
        } else {
            sb.append(
                    String.format("%5s", Math.round((Math.abs(value) * 100))));
            sb.append(value >= 0.0f ? "E" : "W");
        }
        sb.append(SEP);
    }

    private void appendDTGLong(StringBuilder sb, Date value) {
        if (value == null) {
            sb.append("            ");
        } else {
            sb.append(DtgUtil.formatLong(value));
        }
        sb.append(SEP);
    }

    public String getFixFormat() {
        return fixFormat;
    }

    public void setFixFormat(String fixFormat) {
        this.fixFormat = fixFormat;
    }

    public String getFixType() {
        return fixType;
    }

    public void setFixType(String fixType) {
        this.fixType = fixType;
    }

    public String getCenterOrIntensity() {
        return centerOrIntensity;
    }

    public void setCenterOrIntensity(String centerOrIntensity) {
        this.centerOrIntensity = centerOrIntensity;
    }

    public String getFlaggedIndicator() {
        return flaggedIndicator;
    }

    public void setFlaggedIndicator(String flaggedIndicator) {
        this.flaggedIndicator = flaggedIndicator;
    }

    public float getObHeight() {
        return obHeight;
    }

    public void setObHeight(float obHeight) {
        this.obHeight = obHeight;
    }

    public String getPositionConfidence() {
        return positionConfidence;
    }

    public void setPositionConfidence(String positionConfidence) {
        this.positionConfidence = positionConfidence;
    }

    public float getWindMax() {
        return windMax;
    }

    public void setWindMax(float windMax) {
        this.windMax = windMax;
    }

    public String getWindMaxConfidence() {
        return windMaxConfidence;
    }

    public void setWindMaxConfidence(String windMaxConfidence) {
        this.windMaxConfidence = windMaxConfidence;
    }

    public float getMslp() {
        return mslp;
    }

    public void setMslp(float mslp) {
        this.mslp = mslp;
    }

    public String getPressureConfidence() {
        return pressureConfidence;
    }

    public void setPressureConfidence(String pressureConfidence) {
        this.pressureConfidence = pressureConfidence;
    }

    public String getPressureDerivation() {
        return pressureDerivation;
    }

    public void setPressureDerivation(String pressureDerivation) {
        this.pressureDerivation = pressureDerivation;
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

    public String getRadMod1() {
        return radMod1;
    }

    public void setRadMod1(String radMod1) {
        this.radMod1 = radMod1;
    }

    public String getRadMod2() {
        return radMod2;
    }

    public void setRadMod2(String radMod2) {
        this.radMod2 = radMod2;
    }

    public String getRadMod3() {
        return radMod3;
    }

    public void setRadMod3(String radMod3) {
        this.radMod3 = radMod3;
    }

    public String getRadMod4() {
        return radMod4;
    }

    public void setRadMod4(String radMod4) {
        this.radMod4 = radMod4;
    }

    public String getRadiiConfidence() {
        return radiiConfidence;
    }

    public void setRadiiConfidence(String radiiConfidence) {
        this.radiiConfidence = radiiConfidence;
    }

    public float getRadiusOfMaximumWind() {
        return radiusOfMaximumWind;
    }

    public void setRadiusOfMaximumWind(float radiusOfMaximumWind) {
        this.radiusOfMaximumWind = radiusOfMaximumWind;
    }

    public float getEyeSize() {
        return eyeSize;
    }

    public void setEyeSize(float eyeSize) {
        this.eyeSize = eyeSize;
    }

    public String getSubRegion() {
        return subRegion;
    }

    public void setSubRegion(String subRegion) {
        this.subRegion = subRegion;
    }

    public String getFixSite() {
        return fixSite;
    }

    public void setFixSite(String fixSite) {
        this.fixSite = fixSite;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSatelliteType() {
        return satelliteType;
    }

    public void setSatelliteType(String satelliteType) {
        this.satelliteType = satelliteType;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getTropicalIndicator() {
        return tropicalIndicator;
    }

    public void setTropicalIndicator(String tropicalIndicator) {
        this.tropicalIndicator = tropicalIndicator;
    }

    public float getRadiusOfWindIntensity() {
        return radiusOfWindIntensity;
    }

    public void setRadiusOfWindIntensity(float radiusOfWindIntensity) {
        this.radiusOfWindIntensity = radiusOfWindIntensity;
    }

    public String getWindCode() {
        return windCode;
    }

    public void setWindCode(String windCode) {
        this.windCode = windCode;
    }

    public float getWindRad1() {
        return windRad1;
    }

    public void setWindRad1(float windRad1) {
        this.windRad1 = windRad1;
    }

    public float getWindRad2() {
        return windRad2;
    }

    public void setWindRad2(float windRad2) {
        this.windRad2 = windRad2;
    }

    public float getWindRad3() {
        return windRad3;
    }

    public void setWindRad3(float windRad3) {
        this.windRad3 = windRad3;
    }

    public float getWindRad4() {
        return windRad4;
    }

    public void setWindRad4(float windRad4) {
        this.windRad4 = windRad4;
    }

    public float getWindRad5() {
        return windRad5;
    }

    public void setWindRad5(float windRad5) {
        this.windRad5 = windRad5;
    }

    public float getWindRad6() {
        return windRad6;
    }

    public void setWindRad6(float windRad6) {
        this.windRad6 = windRad6;
    }

    public float getWindRad7() {
        return windRad7;
    }

    public void setWindRad7(float windRad7) {
        this.windRad7 = windRad7;
    }

    public float getWindRad8() {
        return windRad8;
    }

    public void setWindRad8(float windRad8) {
        this.windRad8 = windRad8;
    }

    public String getRadMod5() {
        return radMod5;
    }

    public void setRadMod5(String radMod5) {
        this.radMod5 = radMod5;
    }

    public String getRadMod6() {
        return radMod6;
    }

    public void setRadMod6(String radMod6) {
        this.radMod6 = radMod6;
    }

    public String getRadMod7() {
        return radMod7;
    }

    public void setRadMod7(String radMod7) {
        this.radMod7 = radMod7;
    }

    public String getRadMod8() {
        return radMod8;
    }

    public void setRadMod8(String radMod8) {
        this.radMod8 = radMod8;
    }

    public String getMicrowaveRadiiConfidence() {
        return microwaveRadiiConfidence;
    }

    public void setMicrowaveRadiiConfidence(String microwaveRadiiConfidence) {
        this.microwaveRadiiConfidence = microwaveRadiiConfidence;
    }

    public String getEyeShape() {
        return eyeShape;
    }

    public void setEyeShape(String eyeShape) {
        this.eyeShape = eyeShape;
    }

    public float getEyeOrientation() {
        return eyeOrientation;
    }

    public void setEyeOrientation(float eyeOrientation) {
        this.eyeOrientation = eyeOrientation;
    }

    public float getEyeDiameterNM() {
        return eyeDiameterNM;
    }

    public void setEyeDiameterNM(float eyeDiameterNM) {
        this.eyeDiameterNM = eyeDiameterNM;
    }

    public float getEyeShortAxis() {
        return eyeShortAxis;
    }

    public void setEyeShortAxis(float eyeShortAxis) {
        this.eyeShortAxis = eyeShortAxis;
    }

    public float getSeaSurfaceTemp() {
        return seaSurfaceTemp;
    }

    public void setSeaSurfaceTemp(float seaSurfaceTemp) {
        this.seaSurfaceTemp = seaSurfaceTemp;
    }

    public String getPcnCode() {
        return pcnCode;
    }

    public void setPcnCode(String pcnCode) {
        this.pcnCode = pcnCode;
    }

    public String getDvorakCodeLongTermTrend() {
        return dvorakCodeLongTermTrend;
    }

    public void setDvorakCodeLongTermTrend(String dvorakCodeLongTermTrend) {
        this.dvorakCodeLongTermTrend = dvorakCodeLongTermTrend;
    }

    public String getDvorakCodeShortTermTrend() {
        return dvorakCodeShortTermTrend;
    }

    public void setDvorakCodeShortTermTrend(String dvorakCodeShortTermTrend) {
        this.dvorakCodeShortTermTrend = dvorakCodeShortTermTrend;
    }

    public float getCi24hourForecast() {
        return ci24hourForecast;
    }

    public void setCi24hourForecast(float ci24hourForecast) {
        this.ci24hourForecast = ci24hourForecast;
    }

    public String getCenterType() {
        return centerType;
    }

    public void setCenterType(String centerType) {
        this.centerType = centerType;
    }

    public float getCiNum() {
        return ciNum;
    }

    public void setCiNum(float ciNum) {
        this.ciNum = ciNum;
    }

    public String getCiConfidence() {
        return ciConfidence;
    }

    public void setCiConfidence(String ciConfidence) {
        this.ciConfidence = ciConfidence;
    }

    public float gettNumAverage() {
        return tNumAverage;
    }

    public void settNumAverage(float tNumAverage) {
        this.tNumAverage = tNumAverage;
    }

    public float gettNumAveragingTimePeriod() {
        return tNumAveragingTimePeriod;
    }

    public void settNumAveragingTimePeriod(float tNumAveragingTimePeriod) {
        this.tNumAveragingTimePeriod = tNumAveragingTimePeriod;
    }

    public String gettNumAveragingDerivation() {
        return tNumAveragingDerivation;
    }

    public void settNumAveragingDerivation(String tNumAveragingDerivation) {
        this.tNumAveragingDerivation = tNumAveragingDerivation;
    }

    public String gettNumRaw() {
        return tNumRaw;
    }

    public void settNumRaw(String tNumRaw) {
        this.tNumRaw = tNumRaw;
    }

    public float getTempEye() {
        return tempEye;
    }

    public void setTempEye(float tempEye) {
        this.tempEye = tempEye;
    }

    public float getTempCloudSurroundingEye() {
        return tempCloudSurroundingEye;
    }

    public void setTempCloudSurroundingEye(float tempCloudSurroundingEye) {
        this.tempCloudSurroundingEye = tempCloudSurroundingEye;
    }

    public String getSceneType() {
        return sceneType;
    }

    public void setSceneType(String sceneType) {
        this.sceneType = sceneType;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getRainFlag() {
        return rainFlag;
    }

    public void setRainFlag(String rainFlag) {
        this.rainFlag = rainFlag;
    }

    public float getRainRate() {
        return rainRate;
    }

    public void setRainRate(float rainRate) {
        this.rainRate = rainRate;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public float getWaveHeight() {
        return waveHeight;
    }

    public void setWaveHeight(float waveHeight) {
        this.waveHeight = waveHeight;
    }

    public float getTempPassiveMicrowave() {
        return tempPassiveMicrowave;
    }

    public void setTempPassiveMicrowave(float tempPassiveMicrowave) {
        this.tempPassiveMicrowave = tempPassiveMicrowave;
    }

    public float getSlpRaw() {
        return slpRaw;
    }

    public void setSlpRaw(float slpRaw) {
        this.slpRaw = slpRaw;
    }

    public float getSlpRetrieved() {
        return slpRetrieved;
    }

    public void setSlpRetrieved(float slpRetrieved) {
        this.slpRetrieved = slpRetrieved;
    }

    public float getMaxSeas() {
        return maxSeas;
    }

    public void setMaxSeas(float maxSeas) {
        this.maxSeas = maxSeas;
    }

    public String getRadarType() {
        return radarType;
    }

    public void setRadarType(String radarType) {
        this.radarType = radarType;
    }

    public String getRadarFormat() {
        return radarFormat;
    }

    public void setRadarFormat(String radarFormat) {
        this.radarFormat = radarFormat;
    }

    public String getRadobCode() {
        return radobCode;
    }

    public void setRadobCode(String radobCode) {
        this.radobCode = radobCode;
    }

    public float getPercentOfEyewallObserved() {
        return percentOfEyewallObserved;
    }

    public void setPercentOfEyewallObserved(float percentOfEyewallObserved) {
        this.percentOfEyewallObserved = percentOfEyewallObserved;
    }

    public float getSpiralOverlayDegrees() {
        return spiralOverlayDegrees;
    }

    public void setSpiralOverlayDegrees(float spiralOverlayDegrees) {
        this.spiralOverlayDegrees = spiralOverlayDegrees;
    }

    public float getRadarSitePosLat() {
        return radarSitePosLat;
    }

    public void setRadarSitePosLat(float radarSitePosLat) {
        this.radarSitePosLat = radarSitePosLat;
    }

    public float getRadarSitePosLon() {
        return radarSitePosLon;
    }

    public void setRadarSitePosLon(float radarSitePosLon) {
        this.radarSitePosLon = radarSitePosLon;
    }

    public float getInboundMaxWindSpeed() {
        return inboundMaxWindSpeed;
    }

    public void setInboundMaxWindSpeed(float inboundMaxWindSpeed) {
        this.inboundMaxWindSpeed = inboundMaxWindSpeed;
    }

    public float getInboundMaxWindAzimuth() {
        return inboundMaxWindAzimuth;
    }

    public void setInboundMaxWindAzimuth(float inboundMaxWindAzimuth) {
        this.inboundMaxWindAzimuth = inboundMaxWindAzimuth;
    }

    public float getInboundMaxWindRangeNM() {
        return inboundMaxWindRangeNM;
    }

    public void setInboundMaxWindRangeNM(float inboundMaxWindRangeNM) {
        this.inboundMaxWindRangeNM = inboundMaxWindRangeNM;
    }

    public float getInboundMaxWindElevationFeet() {
        return inboundMaxWindElevationFeet;
    }

    public void setInboundMaxWindElevationFeet(
            float inboundMaxWindElevationFeet) {
        this.inboundMaxWindElevationFeet = inboundMaxWindElevationFeet;
    }

    public float getOutboundMaxWindSpeed() {
        return outboundMaxWindSpeed;
    }

    public void setOutboundMaxWindSpeed(float outboundMaxWindSpeed) {
        this.outboundMaxWindSpeed = outboundMaxWindSpeed;
    }

    public float getOutboundMaxWindAzimuth() {
        return outboundMaxWindAzimuth;
    }

    public void setOutboundMaxWindAzimuth(float outboundMaxWindAzimuth) {
        this.outboundMaxWindAzimuth = outboundMaxWindAzimuth;
    }

    public float getOutboundMaxWindRangeNM() {
        return outboundMaxWindRangeNM;
    }

    public void setOutboundMaxWindRangeNM(float outboundMaxWindRangeNM) {
        this.outboundMaxWindRangeNM = outboundMaxWindRangeNM;
    }

    public float getOutboundMaxWindElevationFeet() {
        return outboundMaxWindElevationFeet;
    }

    public void setOutboundMaxWindElevationFeet(
            float outboundMaxWindElevationFeet) {
        this.outboundMaxWindElevationFeet = outboundMaxWindElevationFeet;
    }

    public float getMaxCloudHeightFeet() {
        return maxCloudHeightFeet;
    }

    public void setMaxCloudHeightFeet(float maxCloudHeightFeet) {
        this.maxCloudHeightFeet = maxCloudHeightFeet;
    }

    public float getMaxRainAccumulation() {
        return maxRainAccumulation;
    }

    public void setMaxRainAccumulation(float maxRainAccumulation) {
        this.maxRainAccumulation = maxRainAccumulation;
    }

    public float getRainAccumulationTimeInterval() {
        return rainAccumulationTimeInterval;
    }

    public void setRainAccumulationTimeInterval(
            float rainAccumulationTimeInterval) {
        this.rainAccumulationTimeInterval = rainAccumulationTimeInterval;
    }

    public float getRainAccumulationLat() {
        return rainAccumulationLat;
    }

    public void setRainAccumulationLat(float rainAccumulationLat) {
        this.rainAccumulationLat = rainAccumulationLat;
    }

    public float getRainAccumulationLon() {
        return rainAccumulationLon;
    }

    public void setRainAccumulationLon(float rainAccumulationLon) {
        this.rainAccumulationLon = rainAccumulationLon;
    }

    public String getRadarObservationCodeCharacteristics() {
        return radarObservationCodeCharacteristics;
    }

    public void setRadarObservationCodeCharacteristics(
            String radarObservationCodeCharacteristics) {
        this.radarObservationCodeCharacteristics = radarObservationCodeCharacteristics;
    }

    public String getRadarObservationCodeMovement() {
        return radarObservationCodeMovement;
    }

    public void setRadarObservationCodeMovement(
            String radarObservationCodeMovement) {
        this.radarObservationCodeMovement = radarObservationCodeMovement;
    }

    public float getFlightLevel100Feet() {
        return flightLevel100Feet;
    }

    public void setFlightLevel100Feet(float flightLevel100Feet) {
        this.flightLevel100Feet = flightLevel100Feet;
    }

    public float getFlightLevelMillibars() {
        return flightLevelMillibars;
    }

    public void setFlightLevelMillibars(float flightLevelMillibars) {
        this.flightLevelMillibars = flightLevelMillibars;
    }

    public float getFlightLevelMinimumHeightMeters() {
        return flightLevelMinimumHeightMeters;
    }

    public void setFlightLevelMinimumHeightMeters(
            float flightLevelMinimumHeightMeters) {
        this.flightLevelMinimumHeightMeters = flightLevelMinimumHeightMeters;
    }

    public float getMaxSurfaceWindInboundLegIntensity() {
        return maxSurfaceWindInboundLegIntensity;
    }

    public void setMaxSurfaceWindInboundLegIntensity(
            float maxSurfaceWindInboundLegIntensity) {
        this.maxSurfaceWindInboundLegIntensity = maxSurfaceWindInboundLegIntensity;
    }

    public float getMaxSurfaceWindInboundLegBearing() {
        return maxSurfaceWindInboundLegBearing;
    }

    public void setMaxSurfaceWindInboundLegBearing(
            float maxSurfaceWindInboundLegBearing) {
        this.maxSurfaceWindInboundLegBearing = maxSurfaceWindInboundLegBearing;
    }

    public float getMaxSurfaceWindInboundLegRangeNM() {
        return maxSurfaceWindInboundLegRangeNM;
    }

    public void setMaxSurfaceWindInboundLegRangeNM(
            float maxSurfaceWindInboundLegRangeNM) {
        this.maxSurfaceWindInboundLegRangeNM = maxSurfaceWindInboundLegRangeNM;
    }

    public float getMaxFLWindInboundDirection() {
        return maxFLWindInboundDirection;
    }

    public void setMaxFLWindInboundDirection(float maxFLWindInboundDirection) {
        this.maxFLWindInboundDirection = maxFLWindInboundDirection;
    }

    public float getMaxFLWindInboundIntensity() {
        return maxFLWindInboundIntensity;
    }

    public void setMaxFLWindInboundIntensity(float maxFLWindInboundIntensity) {
        this.maxFLWindInboundIntensity = maxFLWindInboundIntensity;
    }

    public float getMaxFLWindInboundBearing() {
        return maxFLWindInboundBearing;
    }

    public void setMaxFLWindInboundBearing(float maxFLWindInboundBearing) {
        this.maxFLWindInboundBearing = maxFLWindInboundBearing;
    }

    public float getMaxFLWindInboundRangeNM() {
        return maxFLWindInboundRangeNM;
    }

    public void setMaxFLWindInboundRangeNM(float maxFLWindInboundRangeNM) {
        this.maxFLWindInboundRangeNM = maxFLWindInboundRangeNM;
    }

    public float getTempOutsideEye() {
        return tempOutsideEye;
    }

    public void setTempOutsideEye(float tempOutsideEye) {
        this.tempOutsideEye = tempOutsideEye;
    }

    public float getTempInsideEye() {
        return tempInsideEye;
    }

    public void setTempInsideEye(float tempInsideEye) {
        this.tempInsideEye = tempInsideEye;
    }

    public float getDewPointTemp() {
        return dewPointTemp;
    }

    public void setDewPointTemp(float dewPointTemp) {
        this.dewPointTemp = dewPointTemp;
    }

    public String getEyeCharacterOrWallCloudThickness() {
        return eyeCharacterOrWallCloudThickness;
    }

    public void setEyeCharacterOrWallCloudThickness(
            String eyeCharacterOrWallCloudThickness) {
        this.eyeCharacterOrWallCloudThickness = eyeCharacterOrWallCloudThickness;
    }

    public float getAccuracyNavigational() {
        return accuracyNavigational;
    }

    public void setAccuracyNavigational(float accuracyNavigational) {
        this.accuracyNavigational = accuracyNavigational;
    }

    public float getAccuracyMeteorological() {
        return accuracyMeteorological;
    }

    public void setAccuracyMeteorological(float accuracyMeteorological) {
        this.accuracyMeteorological = accuracyMeteorological;
    }

    public String getMissionNumber() {
        return missionNumber;
    }

    public void setMissionNumber(String missionNumber) {
        this.missionNumber = missionNumber;
    }

    public String getSondeEnvironment() {
        return sondeEnvironment;
    }

    public void setSondeEnvironment(String sondeEnvironment) {
        this.sondeEnvironment = sondeEnvironment;
    }

    public float getHeightMidpointLowest150m() {
        return heightMidpointLowest150m;
    }

    public void setHeightMidpointLowest150m(float heightMidpointLowest150m) {
        this.heightMidpointLowest150m = heightMidpointLowest150m;
    }

    public float getSpeedMeanWindLowest150mKt() {
        return speedMeanWindLowest150mKt;
    }

    public void setSpeedMeanWindLowest150mKt(float speedMeanWindLowest150mKt) {
        this.speedMeanWindLowest150mKt = speedMeanWindLowest150mKt;
    }

    public float getSpeedMeanWind0to500mKt() {
        return speedMeanWind0to500mKt;
    }

    public void setSpeedMeanWind0to500mKt(float speedMeanWind0to500mKt) {
        this.speedMeanWind0to500mKt = speedMeanWind0to500mKt;
    }

    public String getAnalysisInitials() {
        return analysisInitials;
    }

    public void setAnalysisInitials(String analysisInitials) {
        this.analysisInitials = analysisInitials;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public float getDistanceToNearestDataNM() {
        return distanceToNearestDataNM;
    }

    public void setDistanceToNearestDataNM(float distanceToNearestDataNM) {
        this.distanceToNearestDataNM = distanceToNearestDataNM;
    }

    public String getObservationSources() {
        return observationSources;
    }

    public void setObservationSources(String observationSources) {
        this.observationSources = observationSources;
    }

    /**
     * Sort records by reference time, fix site, fix type
     *
     * @param fDeckRecs
     */
    public static void sortFDeck(List<BaseFDeckRecord> fDeckRecs) {
        if (!fDeckRecs.isEmpty()) {
            Comparator<BaseFDeckRecord> refTimeCmp = Comparator
                    .comparing(BaseFDeckRecord::getRefTime);
            Comparator<BaseFDeckRecord> fixSiteCmp = Comparator
                    .comparing(BaseFDeckRecord::getFixSite);
            Comparator<BaseFDeckRecord> fixTypCmp = Comparator
                    .comparing(BaseFDeckRecord::getFixType);

            Collections.sort(fDeckRecs, refTimeCmp.thenComparing(fixTypCmp)
                    .thenComparing(fixSiteCmp));

        }
    }

    @Override
    public Map<String, Object> getUniqueId() {
        return Collections.emptyMap();
    }

}