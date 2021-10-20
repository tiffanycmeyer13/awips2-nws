package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * GenesisBaseEDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 3, 2020 # 77134     pwang       Initial creation
 * Jul 6, 2020 # 79696     pwang       Add a special copy constructor
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@MappedSuperclass
public class GenesisBaseEDeckRecord extends AbstractGenesisDeckRecord {

    private static final long serialVersionUID = 1L;

    private static final String TRACK_PROBABILITY_FORMAT = "TR";

    private static final String INTENSITY_PROBABILITY_FORMAT = "IN";

    private static final String RAPID_INTENSIFICATION_PROBABILITY_FORMAT = "RI";

    private static final String WIND_RADII_PROBABILITY_FORMAT = "WD";

    private static final String PRESSURE_PROBABILITY_FORMAT = "PR";

    private static final String TC_GENESIS_PROBABILITY_FORMAT = "GN";

    private static final String TC_GENESIS_SHAPE_PROBABILITY_FORMAT = "GS";

    private static final String EMPTY = "";

    private static final String SEP = ", ";

    /*****************
     * COMMON FIELDS *
     *****************/

    /**
     * @formatter:off Probability Format (2 characters): TR - track, "03" also
     *                accepted for existing old E-deck files IN - intensity RI -
     *                rapid intensification WD - wind radii PR - pressure GN -
     *                TC genesis probability GS - TC genesis shape
     * @formatter:on
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String probFormat = EMPTY;

    /**
     * Objective Technique (4 characters)
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String technique = EMPTY;

    /**
     * TAU - forecast period: 0 through 168 hours
     */
    @DynamicSerializeElement
    private int fcstHour = IMISSD;

    /**
     * Probability - probability of ProbItem (see parameter specific definition
     * of ProbItem), 0 - 100%,
     */
    @DynamicSerializeElement
    private float probability = RMISSD;

    /**
     * Probability item (all floats; parameter specific definition)
     */
    @DynamicSerializeElement
    private float probabilityItem = RMISSD;

    /****************************************************
     * FIELDS COMMON TO MORE THAN 1 (BUT NOT ALL) TYPES *
     ****************************************************/

    /**
     * Level of tropical cyclone development; such as DB, TD, TS, TY, ...
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String developmentLevel = EMPTY;

    /************************
     * TRACK RELATED VALUES *
     ************************/

    // probabilityItem = Probability radius, 0 through 2000 nm, 4 char.
    // developmentLevel

    /**
     * Cross track direction, 0 through 359 degrees
     */
    @DynamicSerializeElement
    private float crossTrackDirection = RMISSD;

    /**
     * Radius Code for wind intensity: AAA = full circle; QQQ = quadrant (NNQ,
     * NEQ, EEQ, SEQ, SSQ, SWQ, WWQ, NWQ) [currently unused]
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String radWindQuad = EMPTY;

    /**
     * Cross track radius, 0 through 2000 NM
     */
    @DynamicSerializeElement
    private float crossTrackRadius = RMISSD;

    /**
     * Along track radius, 0 through 2000 NM
     */
    @DynamicSerializeElement
    private float alongTrackRadius = RMISSD;

    /**
     * Cross track bias, -999 through 999 NM
     */
    @DynamicSerializeElement
    private float crossTrackBias = RMISSD;

    /**
     * Along track bias, -999 through 999 NM
     */
    @DynamicSerializeElement
    private float alongTrackBias = RMISSD;

    /*************************
     * INTENSITY PROBABILITY *
     *************************/

    // probabilityItem = Wind speed (bias adjusted), 0 - 300 kts

    // developmentLevel

    /**
     * Half_Range - Half the probability range (radius), 0 - 50 kts
     */
    @DynamicSerializeElement
    private float halfRange = RMISSD;

    /*************************************
     * RAPID INTENSIFICATION PROBABILITY *
     *************************************/

    // probabilityItem = intensity change, 0 - 300 kts

    /**
     * V - final intensity, 0 - 300 kts
     */
    @DynamicSerializeElement
    private float vFinal = RMISSD;

    /**
     * Rapid Intensification Start Time (TAU)
     */
    @DynamicSerializeElement
    private float riStartTAU = RMISSD;

    /**
     * Rapid Intensification Stop Time (TAU)
     */
    @DynamicSerializeElement
    private float riStopTAU = RMISSD;

    /**************************
     * TC GENESIS PROBABILITY *
     **************************/

    // probabilityItem - time period, i.e. genesis during next xxx hours,
    // 0 for genesis or dissipate event, 0 - 240 hrs

    /**
     * GenOrDis - "invest", "genFcst", "genesis", "disFcst" or "dissipate"
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String genOrDis = EMPTY;

    /**
     * DTG - Genesis or dissipated event Date-Time-Group, yyyymmddhhmm:
     * 0000010100 through 9999123123, 12 char.
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String eventDateTimeGroup = EMPTY;

    /**
     * stormId2 - cyclone ID if the genesis developed into an invest area or
     * cyclone ID of dissipated TC, e.g. al032014
     */
    @Column(length = 12)
    @DynamicSerializeElement
    private String stormId2 = EMPTY;

    /**
     * min - minutes, associated with DTG in common fields (3rd field in
     * record), 0 - 59 min
     */
    @DynamicSerializeElement
    private int minutes = IMISSD;

    /**
     * GenesisBaseEDeckRecord undefined - TBD
     */
    @Column(columnDefinition = "text")
    @DynamicSerializeElement
    private String undefined = EMPTY;

    /********************
     * TC GENESIS SHAPE *
     ********************/

    // probabilityItem - time period, i.e. genesis during next xxx hours,
    // * 0 for genesis or dissipate event, 0 - 240 hrs

    /**
     * TCFA MANOP dtg, ddhhmm
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String tcfaManopDtg = EMPTY;

    /**
     * TCFA message dtg, yymmddhhmm
     */
    @Column(length = 16)
    @DynamicSerializeElement
    private String tcfaMsgDtg = EMPTY;

    /**
     * TCFA WT number
     */
    @DynamicSerializeElement
    private int tcfaWtNum = IMISSD;

    /**
     * Shape type, ELP - ellipse, BOX - box, CIR - circle, PLY - polygon, 3
     * char.
     */
    @Column(length = 8)
    @DynamicSerializeElement
    private String shapeType = EMPTY;

    /**
     * EllipseAngle - cross track angle for ellipse (math coords), 3 char.
     */
    @DynamicSerializeElement
    private float ellipseAngle = RMISSD;

    /**
     * EllipseRCross - cross track radius, 0 - 2000 nm, 4 char.
     */
    @DynamicSerializeElement
    private float ellipseRCross = RMISSD;

    /**
     * EllipseRAlong - along track radius, 0 - 2000 nm, 4 char.
     */
    @DynamicSerializeElement
    private float ellipseRAlong = RMISSD;

    /**
     * @formatter:off Box1LatN/S - Latitude for start point for TCFA box center
     *                line or center point for TCFA circle, 4 char. 0 - 900
     *                tenths of degrees N/S is the hemispheric index
     * @formatter:on
     */
    @DynamicSerializeElement
    private float box1LatNS = RMISSD;

    /**
     * @formatter:off Box1LonE/W - Longitude for start point for TCFA box center
     *                line or center point for TCFA circle, 5 char. 0 - 1800
     *                tenths of degrees E/W is the hemispheric index
     * @formatter:off
     */
    @DynamicSerializeElement
    private float box1LonEW = RMISSD;

    /**
     * @formatter:off Box2LatN/S - Latitude for end point for TCFA box center
     *                line, not used for TCFA circle, 4 char. 0 - 900 tenths of
     *                degrees N/S is the hemispheric index
     * @formatter:off
     */
    @DynamicSerializeElement
    private float box2LatNS = RMISSD;

    /**
     * @formatter:off Box2LonE/W - Longitude for start point for TCFA box center
     *                line, not used for TCFA circle, 5 char. 0 - 1800 tenths of
     *                degrees E/W is the hemispheric index
     * @formatter:off
     */
    @DynamicSerializeElement
    private float box2LonEW = RMISSD;

    /**
     * TCFARADIUS - distance from center line to box edge, or radius of circle
     * (nm), 3 char.
     */
    @DynamicSerializeElement
    private float tcfaRadius = RMISSD;

    /**
     * PolygonPts - array of 20 lat, lon points defining a polygon
     */
    @Column(columnDefinition = "text")
    @DynamicSerializeElement
    private String polygonPointsText;

    /**
     * Default constructor
     */
    public GenesisBaseEDeckRecord() {
        super();

        // Fields not declared from AbstractGenesisDeckRecord()
        this.year = IMISSD;
        this.cycloneNum = this.genesisNum;

        // Common Fields
        this.probFormat = EMPTY;
        this.technique = EMPTY;
        this.fcstHour = IMISSD;
        this.probability = RMISSD;
        this.probabilityItem = RMISSD;

        // Track Related Values
        this.developmentLevel = EMPTY;
        this.crossTrackDirection = RMISSD;
        this.radWindQuad = EMPTY;
        this.crossTrackRadius = RMISSD;
        this.alongTrackRadius = RMISSD;
        this.crossTrackBias = RMISSD;
        this.alongTrackBias = RMISSD;

        // Half Range
        this.halfRange = RMISSD;

        // Rapid Intensification Probability
        this.vFinal = RMISSD;
        this.riStartTAU = RMISSD;
        this.riStopTAU = RMISSD;

        // TC Genesis Probability
        this.genOrDis = "genFcst";
        this.eventDateTimeGroup = EMPTY;
        this.stormId2 = EMPTY;
        this.minutes = IMISSD;
        this.undefined = EMPTY;

        // TC Genesis Shape
        this.tcfaManopDtg = EMPTY;
        this.tcfaMsgDtg = EMPTY;
        this.tcfaWtNum = IMISSD;
        this.shapeType = EMPTY;
        this.ellipseAngle = RMISSD;
        this.ellipseRCross = RMISSD;
        this.ellipseRAlong = RMISSD;
        this.box1LatNS = RMISSD;
        this.box1LonEW = RMISSD;
        this.box2LatNS = RMISSD;
        this.box2LonEW = RMISSD;
        this.tcfaRadius = RMISSD;
        this.polygonPointsText = EMPTY;
    }

    public GenesisBaseEDeckRecord(BaseEDeckRecord rec) {
        super(rec);
        this.probFormat = rec.getProbFormat();
        this.technique = rec.getTechnique();
        this.fcstHour = rec.getFcstHour();
        this.probability = rec.getProbability();
        this.probabilityItem = rec.getProbabilityItem();
        this.developmentLevel = rec.getDevelopmentLevel();
        this.crossTrackDirection = rec.getCrossTrackDirection();
        this.radWindQuad = rec.getRadWindQuad();
        this.crossTrackRadius = rec.getCrossTrackRadius();
        this.alongTrackRadius = rec.getAlongTrackRadius();
        this.crossTrackBias = rec.getCrossTrackBias();
        this.alongTrackBias = rec.getAlongTrackBias();
        this.halfRange = rec.getHalfRange();
        this.vFinal = rec.getVFinal();
        this.riStartTAU = rec.getRiStartTAU();
        this.riStopTAU = rec.getRiStopTAU();
        this.genOrDis = rec.getGenOrDis();
        this.eventDateTimeGroup = rec.getEventDateTimeGroup();
        this.stormId2 = rec.getStormId2();
        this.minutes = rec.getMinutes();
        this.undefined = rec.getUndefined();
        this.tcfaManopDtg = rec.getTcfaManopDtg();
        this.tcfaMsgDtg = rec.getTcfaMsgDtg();
        this.tcfaWtNum = rec.getTcfaWtNum();
        this.shapeType = rec.getShapeType();
        this.ellipseAngle = rec.getEllipseAngle();
        this.ellipseRCross = rec.getEllipseRCross();
        this.ellipseRAlong = rec.getEllipseRAlong();
        this.box1LatNS = rec.getBox1LatNS();
        this.box1LonEW = rec.getBox1LonEW();
        this.box2LatNS = rec.getBox2LatNS();
        this.box2LonEW = rec.getBox2LonEW();
        this.tcfaRadius = rec.getTcfaRadius();
        this.polygonPointsText = rec.getPolygonPointsText();

    }

    /**
     * Constructs a CSV string representation of the data in the AtcfEDeckRecord
     * compatible with ATCF "E-deck" (probabilistic data) files. Sample E-deck
     * line (wrapped):
     *
     * AL, 14, 2016093012, TR, GPCC, 12, 135N, 719W, 68, 36, , 0, , 0, 0, 0, 0,
     */
    public String toDeckString() {
        StringBuilder sb = new StringBuilder();

        append(sb, 2, getBasin());

        // use leading zero
        sb.append(String.format("%02d", getGenesisNum()));
        sb.append(SEP);

        appendDTG(sb, DTGformat.YYYYMMDDHH, getRefTimeAsCalendar());

        append(sb, 2, probFormat);
        append(sb, 4, technique);
        append(sb, 3, fcstHour);

        appendLatitude(sb, getClat());
        appendLongitude(sb, getClon());

        // 25.0 -> " 25"
        append(sb, 3, probability);

        if (probFormat.equalsIgnoreCase(TRACK_PROBABILITY_FORMAT)) {
            appendTrackFields(sb);
        } else if (probFormat.equalsIgnoreCase(INTENSITY_PROBABILITY_FORMAT)) {
            appendIntensityFields(sb);
        } else if (probFormat
                .equalsIgnoreCase(RAPID_INTENSIFICATION_PROBABILITY_FORMAT)) {
            appendRapidIntensificationFields(sb);
        } else if (probFormat.equalsIgnoreCase(WIND_RADII_PROBABILITY_FORMAT)) {
            appendWindRadiiFields(sb);
        } else if (probFormat.equalsIgnoreCase(PRESSURE_PROBABILITY_FORMAT)) {
            appendPressureFields(sb);
        } else if (probFormat.equalsIgnoreCase(TC_GENESIS_PROBABILITY_FORMAT)) {
            appendTCGenesisFields(sb);
        } else if (probFormat
                .equalsIgnoreCase(TC_GENESIS_SHAPE_PROBABILITY_FORMAT)) {
            appendTCGenesisShapeFields(sb);
        }

        return sb.toString();

    }

    private void appendTrackFields(StringBuilder sb) {
        append(sb, 4, probabilityItem);
        append(sb, 2, developmentLevel);
        append(sb, 3, crossTrackDirection);
        append(sb, 3, radWindQuad);
        append(sb, 4, crossTrackRadius);
        append(sb, 4, alongTrackRadius);
        append(sb, 4, crossTrackBias);
        append(sb, 4, alongTrackBias);
    }

    private void appendIntensityFields(StringBuilder sb) {
        append(sb, 3, probabilityItem);
        append(sb, 2, developmentLevel);
        append(sb, 4, halfRange);
    }

    private void appendRapidIntensificationFields(StringBuilder sb) {
        append(sb, 3, probabilityItem);
        append(sb, 3, vFinal);
        append(sb, 3, getForecaster());
        append(sb, 3, riStartTAU);
        append(sb, 3, riStopTAU);
    }

    private void appendWindRadiiFields(StringBuilder sb) {
        append(sb, 4, probabilityItem);
    }

    private void appendPressureFields(StringBuilder sb) {
        append(sb, 4, probabilityItem);
    }

    private void appendTCGenesisFields(StringBuilder sb) {
        append(sb, 4, probabilityItem);
        append(sb, 3, getForecaster());
        append(sb, 4, genOrDis);
        append(sb, 10, eventDateTimeGroup);
        append(sb, 8, stormId2);
        append(sb, 2, minutes);
        appendLZ(sb, 3, this.getGenesisNum());
        // unused? append(sb, 4, undefined);
    }

    private void appendTCGenesisShapeFields(StringBuilder sb) {
        append(sb, 4, probabilityItem);
        append(sb, 3, getForecaster());
        append(sb, 6, tcfaManopDtg);
        append(sb, 10, tcfaMsgDtg);
        append(sb, 2, tcfaWtNum);
        append(sb, 3, shapeType);
        append(sb, 3, ellipseAngle);
        append(sb, 4, ellipseRCross);
        append(sb, 4, ellipseRAlong);
        append(sb, 4, box1LatNS);
        append(sb, 5, box1LonEW);
        append(sb, 4, box2LatNS);
        append(sb, 5, box2LonEW);
        append(sb, 3, tcfaRadius);
        if (polygonPointsText != null) {
            sb.append(polygonPointsText);
            sb.append(SEP);
        }
    }

    /*
     * Append a String value
     */
    private void append(StringBuilder sb, int length, String value) {
        String format = "%" + length + "s";
        if (!value.isEmpty() /* && !value.equals(BLANK) */) {
            sb.append(String.format(format, value));
        }
        sb.append(SEP);
    }

    /*
     * Append an int value
     */
    private void append(StringBuilder sb, int length, int value) {
        String format = "%" + length + "s";
        sb.append(value == IMISSD ? "  " : String.format(format, value));
        sb.append(SEP);
    }

    /*
     * Append an int value with leading zeroes
     */
    private void appendLZ(StringBuilder sb, int length, int value) {
        String format = "%0" + length + "d";
        sb.append(value == IMISSD ? "  " : String.format(format, value));
        sb.append(SEP);
    }

    /*
     * Append a float value
     */
    private void append(StringBuilder sb, int length, float value) {
        String format = "%" + length + "s";
        sb.append(value == RMISSD ? "  " : String.format(format, (int) value));
        sb.append(SEP);
    }

    /*
     * Append a latitude value with formatting
     */
    private void appendLatitude(StringBuilder sb, float value) {
        // LAT format; example: 26.800001 -> "268N"
        if (value == RMISSD) {
            sb.append("    ");
        } else {
            sb.append(String.format("%3s", Math.round((Math.abs(value) * 10))));
            sb.append(value >= 0.0f ? "N" : "S");
        }
        sb.append(SEP);
    }

    /*
     * Append a longitude value with formatting
     */
    private void appendLongitude(StringBuilder sb, float value) {
        // LON format; example: -79.200005 -> " 792W"
        if (value == RMISSD) {
            sb.append("     ");
        } else {
            sb.append(String.format("%4s", Math.round((Math.abs(value) * 10))));
            sb.append(value >= 0.0f ? "E" : "W");
        }
        sb.append(SEP);
    }

    enum DTGformat {
        YYYYMMDDHH, YYYYMMDDHHMM, DDHHMM, YYMMDDHHMM
    }

    private void appendDTG(StringBuilder sb, DTGformat format, Calendar value) {
        if (format == DTGformat.YYYYMMDDHH
                || format == DTGformat.YYYYMMDDHHMM) {
            sb.append(String.format("%04d", value.get(Calendar.YEAR)));
        } else if (format == DTGformat.YYMMDDHHMM) {
            sb.append(String.format("%02d", value.get(Calendar.YEAR) % 100));
        }
        sb.append(String.format("%02d", value.get(Calendar.MONTH) + 1));
        sb.append(String.format("%02d", value.get(Calendar.DAY_OF_MONTH)));
        sb.append(String.format("%02d", value.get(Calendar.HOUR_OF_DAY)));
        if (format != DTGformat.YYYYMMDDHH) {
            sb.append(String.format("%02d", value.get(Calendar.MINUTE)));
        }
        sb.append(SEP);
    }

    public String getProbFormat() {
        return probFormat;
    }

    public void setProbFormat(String probFormat) {
        this.probFormat = probFormat;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public int getFcstHour() {
        return fcstHour;
    }

    public void setFcstHour(int fcstHour) {
        this.fcstHour = fcstHour;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public float getProbabilityItem() {
        return probabilityItem;
    }

    public void setProbabilityItem(float probabilityItem) {
        this.probabilityItem = probabilityItem;
    }

    public String getDevelopmentLevel() {
        return developmentLevel;
    }

    public void setDevelopmentLevel(String developmentLevel) {
        this.developmentLevel = developmentLevel;
    }

    public float getCrossTrackDirection() {
        return crossTrackDirection;
    }

    public void setCrossTrackDirection(float crossTrackDirection) {
        this.crossTrackDirection = crossTrackDirection;
    }

    public String getRadWindQuad() {
        return radWindQuad;
    }

    public void setRadWindQuad(String radWindQuad) {
        this.radWindQuad = radWindQuad;
    }

    public float getCrossTrackRadius() {
        return crossTrackRadius;
    }

    public void setCrossTrackRadius(float crossTrackRadius) {
        this.crossTrackRadius = crossTrackRadius;
    }

    public float getAlongTrackRadius() {
        return alongTrackRadius;
    }

    public void setAlongTrackRadius(float alongTrackRadius) {
        this.alongTrackRadius = alongTrackRadius;
    }

    public float getCrossTrackBias() {
        return crossTrackBias;
    }

    public void setCrossTrackBias(float crossTrackBias) {
        this.crossTrackBias = crossTrackBias;
    }

    public float getAlongTrackBias() {
        return alongTrackBias;
    }

    public void setAlongTrackBias(float alongTrackBias) {
        this.alongTrackBias = alongTrackBias;
    }

    public float getHalfRange() {
        return halfRange;
    }

    public void setHalfRange(float halfRange) {
        this.halfRange = halfRange;
    }

    public float getVFinal() {
        return vFinal;
    }

    public void setVFinal(float vFinal) {
        this.vFinal = vFinal;
    }

    public float getRiStartTAU() {
        return riStartTAU;
    }

    public void setRiStartTAU(float riStartTAU) {
        this.riStartTAU = riStartTAU;
    }

    public float getRiStopTAU() {
        return riStopTAU;
    }

    public void setRiStopTAU(float riStopTAU) {
        this.riStopTAU = riStopTAU;
    }

    public String getGenOrDis() {
        return genOrDis;
    }

    public void setGenOrDis(String genOrDis) {
        this.genOrDis = genOrDis;
    }

    public /* Calendar */ String getEventDateTimeGroup() {
        return eventDateTimeGroup;
    }

    public void setEventDateTimeGroup(
            /* Calendar */ String eventDateTimeGroup) {
        this.eventDateTimeGroup = eventDateTimeGroup;
    }

    public String getStormId2() {
        return stormId2;
    }

    public void setStormId2(String stormId2) {
        this.stormId2 = stormId2;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getUndefined() {
        return undefined;
    }

    public void setUndefined(String undefined) {
        this.undefined = undefined;
    }

    public /* Calendar */ String getTcfaManopDtg() {
        return tcfaManopDtg;
    }

    public void setTcfaManopDtg(/* Calendar */ String tcfaManopDtg) {
        this.tcfaManopDtg = tcfaManopDtg;
    }

    public /* Calendar */ String getTcfaMsgDtg() {
        return tcfaMsgDtg;
    }

    public void setTcfaMsgDtg(/* Calendar */ String tcfaMsgDtg) {
        this.tcfaMsgDtg = tcfaMsgDtg;
    }

    public int getTcfaWtNum() {
        return tcfaWtNum;
    }

    public void setTcfaWtNum(int tcfaWtNum) {
        this.tcfaWtNum = tcfaWtNum;
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }

    public float getEllipseAngle() {
        return ellipseAngle;
    }

    public void setEllipseAngle(float ellipseAngle) {
        this.ellipseAngle = ellipseAngle;
    }

    public float getEllipseRCross() {
        return ellipseRCross;
    }

    public void setEllipseRCross(float ellipseRCross) {
        this.ellipseRCross = ellipseRCross;
    }

    public float getEllipseRAlong() {
        return ellipseRAlong;
    }

    public void setEllipseRAlong(float ellipseRAlong) {
        this.ellipseRAlong = ellipseRAlong;
    }

    public float getBox1LatNS() {
        return box1LatNS;
    }

    public void setBox1LatNS(float box1LatNS) {
        this.box1LatNS = box1LatNS;
    }

    public float getBox1LonEW() {
        return box1LonEW;
    }

    public void setBox1LonEW(float box1LonEW) {
        this.box1LonEW = box1LonEW;
    }

    public float getBox2LatNS() {
        return box2LatNS;
    }

    public void setBox2LatNS(float box2LatNS) {
        this.box2LatNS = box2LatNS;
    }

    public float getBox2LonEW() {
        return box2LonEW;
    }

    public void setBox2LonEW(float box2LonEW) {
        this.box2LonEW = box2LonEW;
    }

    public float getTcfaRadius() {
        return tcfaRadius;
    }

    public void setTcfaRadius(float tcfaRadius) {
        this.tcfaRadius = tcfaRadius;
    }

    public String getPolygonPointsText() {
        return polygonPointsText;
    }

    public void setPolygonPointsText(String polygonPointsText) {
        this.polygonPointsText = polygonPointsText;
    }

    /**
     * @return reference time advanced by the forecast hour
     */
    public Date getForecastDateTime() {
        Date refTime = getRefTime();
        return refTime != null ? new Date(refTime.getTime()
                + getFcstHour() * TimeUtil.MILLIS_PER_HOUR) : null;
    }

    @Override
    public Map<String, Object> getUniqueId() {
        return Collections.emptyMap();
    }

}
