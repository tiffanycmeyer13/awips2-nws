/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.EDeckRecord;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;
import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;
import gov.noaa.nws.ocp.viz.drawing.display.IText.FontStyle;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextJustification;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextRotation;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.Arc;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.Tcm;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.TcmWindQuarters;

/**
 * Draw A-Deck data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Apr 11, 2019 62487       jwu         Initial creation.
 * Nov 13, 2019 71089       jwu         Add support for GPCE & GPCE-AX.
 * Nov 22, 2019 71560       jwu         Add support for GPCE Climatology.
 *
 * </pre>
 *
 * @author jwu
 * @version 1
 */
public class ObjAidsGenerator {

    // Probility format to get GPCE data from E-Deck.
    private static final String TRACK_PROB_FORMAT = "TR";

    // Default technique to get GPCE climatology data from E-Deck.
    private static final String GPCE_CLIMATE_TECH = "GPCC";

    // Color for GPCE climatology.
    private static final Color GPCE_CLIMATE_COLOR = Color.RED;

    // Atcf Resource
    private AtcfResource atcfResource;

    // Properties used to draw obj aids
    private ObjAidsProperties objAidsProperties;

    private Map<String, List<EDeckRecord>> edeckData = null;

    private Set<String> edeckTRTech = null;

    /**
     * Constructor
     */
    public ObjAidsGenerator() {
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        objAidsProperties = new ObjAidsProperties();
    }

    /**
     * Constructor
     *
     * @param rsc
     *            AtcfResource
     * @param objAidsProperties
     *            ObjAidsProperties
     */
    public ObjAidsGenerator(AtcfResource rsc,
            ObjAidsProperties objAidsProperties) {
        this.atcfResource = rsc;
        this.objAidsProperties = objAidsProperties;
    }

    /**
     * @return the atcfResource
     */
    public AtcfResource getAtcfResource() {
        return atcfResource;
    }

    /**
     * @param atcfResource
     *            the atcfResource to set
     */
    public void setAtcfResource(AtcfResource atcfResource) {
        this.atcfResource = atcfResource;
    }

    /**
     * @return the objAidsProperties
     */
    public ObjAidsProperties getObjAidsProperties() {
        return objAidsProperties;
    }

    /**
     * @param objAidsProperties
     *            the objAidsProperties to set
     */
    public void setObjAidsProperties(ObjAidsProperties objAidsProperties) {
        this.objAidsProperties = objAidsProperties;
    }

    /**
     * Draw all A-Deck records by removing elements on A-Deck layer and moving
     * new elements onto A-Deck layer.
     */
    public void draw() {
        draw(true, true);
    }

    /**
     * Remove all A-Deck elements
     *
     */
    public void remove() {

        // If no ATCF resource, get the current AtcfResource.
        if (atcfResource == null) {
            atcfResource = AtcfSession.getInstance().getAtcfResource();
        }

        // Remove elements on A-Deck layer.
        Layer aLayer = atcfResource.getResourceData().getActiveAtcfProduct()
                .getADeckLayer();

        atcfResource.setActiveLayer(aLayer);
        aLayer.removeAllElements();

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor instanceof AbstractEditor) {
            editor.refresh();
        }
    }

    /**
     * Draw all A-Deck records
     *
     * @param removeExistingElems
     *            Remove elements on A-Deck layer, if true.
     * @param apply
     *            Move elements onto A-Deck layer, if true.
     */
    public void draw(boolean removeExistingElems, boolean apply) {

        // Get the current AtcfResource.
        AtcfResource drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Set the display properties.
        if (objAidsProperties == null) {
            objAidsProperties = new ObjAidsProperties();
        }

        atcfResource.getResourceData().getActiveAtcfProduct()
        .setObjAidsProperties(objAidsProperties);

        // Retrieve all selected A-Deck records.
        List<ADeckRecord> pdos = new ArrayList<>();
        for (String dtg : objAidsProperties.getSelectedDateTimeGroups()) {
            List<ADeckRecord> records = drawingLayer.getResourceData()
                    .getActiveAtcfProduct().getADeckData(dtg);
            if (records != null) {
                pdos.addAll(records);
            }
        }

        // Create all elements.
        List<AbstractDrawableComponent> elems = createElements(pdos,
                objAidsProperties);

        // Draw elements on ghost layer
        Layer gLayer = drawingLayer.getResourceData().getActiveAtcfProduct()
                .getGhostLayer();
        gLayer.removeAllElements();
        drawingLayer.setActiveLayer(gLayer);

        Layer aLayer = drawingLayer.getResourceData().getActiveAtcfProduct()
                .getADeckLayer();
        if (removeExistingElems) {
            aLayer.removeAllElements();
        }

        // Add elements on A-Deck layer
        if (!elems.isEmpty()) {
            drawingLayer.addElements(elems);
        }

        // Move to A-Deck layer
        if (apply) {
            aLayer.add(gLayer.getDrawables());
            gLayer.removeAllElements();
        }

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor instanceof AbstractEditor) {
            editor.refresh();
        }

    }

    /*
     * Create graphic elements for selected A-Deck records.
     *
     * @param
     *
     * @return
     */
    private List<AbstractDrawableComponent> createElements(
            List<ADeckRecord> pdos, ObjAidsProperties prop) {

        // Prepare the EDeck data if not retrieved yet.
        prepareEDeckData();

        List<AbstractDrawableComponent> elems = new ArrayList<>();
        Map<RecordKey, ADeckRecord> recordMap = new HashMap<>();

        // Prepare to draw retrieved forecast points
        for (ADeckRecord rec : pdos) {
            String aid = rec.getTechnique();
            String dtg = calendarToString(rec.getRefTimeAsCalendar());
            int tau = rec.getFcstHour();
            int windRad = (int) rec.getRadWind();
            RecordKey key = new RecordKey(aid, dtg, tau, windRad);
            recordMap.put(key, rec);
        }

        /*
         * TODO Set bold font and/or larger text. Looks like this checkbox
         * behaves the same "boldLinesAllCheckbox". Will adjust if we find it is
         * different.
         */
        float lineWidth = 1.0f;
        Double sizeScale = 1.0;
        if (prop.isBoldLinesAll() || prop.isToggleBoldSelectedAids()) {
            lineWidth = 4.8f;
            sizeScale = 1.6;
        }

        // Get selected objective aid techniques for filtering
        List<String> selectedObjectiveAids = prop.getSelectedObjectiveAids();

        // Get TAU limit.
        int tauLimit = 168;
        try {
            tauLimit = Integer.parseInt(prop.getSelectedPartialTime());
        } catch (NumberFormatException e) {
            tauLimit = 168;
        }

        Set<String> aidsUsed = prop.getActiveObjectiveAids();

        // Loops through each Obj. Aids to draw them
        for (String aid : selectedObjectiveAids) {

            String aidCode = AtcfVizUtil.aidCode(aid);

            /*
             * Check if the technique could be used for GPCE related display.
             * Right now, GPCE, GPCE-AX, GPCE-Climatology will be drawn only
             * when TVCN or TCON is selected.
             */
            boolean useGPCE = usableForGPCE(aidCode);

            if (!aidsUsed.contains(aidCode)) {
                // If this aid was selected but doesn't appear in the data, skip
                continue;
            }

            for (String dtg : prop.getSelectedDateTimeGroups()) {
                Color[] colors = new Color[] { Color.WHITE };
                Coordinate previousLocation = null;
                boolean atLeastOneMarkerDrawnForThisAidDtg = false;

                for (AtcfTaus taus : AtcfTaus.getForecastTaus()) {
                    int tau = taus.getValue();
                    if (tau <= tauLimit) {

                        boolean firstDrawForTau = true;

                        for (WindRadii windRads : WindRadii.values()) {

                            int windRad = windRads.getValue();

                            RecordKey key = new RecordKey(aidCode, dtg, tau,
                                    windRad);

                            ADeckRecord rec = recordMap.get(key);
                            if (rec == null || rec.getClat() > 90.0
                                    || rec.getClon() > 360.0) {
                                continue;
                            }

                            colors = getColors(prop, rec);

                            Coordinate location = new Coordinate(rec.getClon(),
                                    rec.getClat());

                            if (firstDrawForTau) {

                                firstDrawForTau = false;

                                if (tau > 0) {

                                    // for tau > 0, create a marker element at
                                    // the forecast point

                                    // symbol type: box if Tau is multiple of
                                    // 24; triangle otherwise
                                    String markerType = null;
                                    double markerSize = sizeScale;
                                    if (tau % 24 == 0) {
                                        markerType = "FILLED_BOX";
                                        markerSize *= 0.84;
                                    } else {
                                        markerType = "FILLED_TRIANGLE";
                                        markerSize *= 1.10;
                                    }

                                    Symbol symbol = new Symbol(null, // Coordinate[]
                                            // range,
                                            colors, // Color[] colors,
                                            lineWidth, // float lineWidth,
                                            markerSize, // double sizeScale,
                                            false, // Boolean clear,
                                            location, // Coordinate location,
                                            "Marker", // String atcfCategory,
                                            markerType // String atcfType
                                            );

                                    elems.add(symbol);
                                    atLeastOneMarkerDrawnForThisAidDtg = true;

                                    // Draw line segment from previous point

                                    if (previousLocation != null) {
                                        String lineStyle;
                                        if (tau <= 72) {
                                            lineStyle = "LINE_SOLID";
                                        } else if (tau <= 120) {
                                            lineStyle = "LINE_DASHED_6"; // dashed
                                        } else {
                                            lineStyle = "LINE_DASHED_2"; // dotted
                                        }
                                        ArrayList<Coordinate> linePoints = new ArrayList<>(
                                                Arrays.asList(previousLocation,
                                                        location));
                                        Line line = new Line(null, // Coordinate[]
                                                // range,
                                                colors, // Color[] colors,
                                                lineWidth, // float lineWidth,
                                                sizeScale, // double sizeScale,
                                                false, // boolean closed,
                                                false, // boolean filled,
                                                linePoints, // List<Coordinate>
                                                // linePoints,
                                                1, // int smoothFactor,
                                                null, // FillPattern
                                                // fillPattern,
                                                "Lines", // String atcfCategory,
                                                lineStyle // String atcfType
                                                );

                                        elems.add(line);
                                    }

                                    // Create an intensity label string for the
                                    // fix, if requested
                                    if (prop.isDisplayAidIntensities()
                                            && (int) rec.getWindMax() > 0) {
                                        StringBuilder label = new StringBuilder();
                                        label.append((int) rec.getWindMax());

                                        // add the constructed label to the
                                        // display
                                        Coordinate[] range = null;
                                        String fontName = "Courier";
                                        float fontSize = 10.0f;
                                        TextJustification justification = TextJustification.CENTER;
                                        Coordinate position = location;
                                        double rotation = 0.0;
                                        TextRotation rotationRelativity = TextRotation.SCREEN_RELATIVE;
                                        String[] text = new String[] {
                                                label.toString(), "", "" };
                                        FontStyle style = FontStyle.REGULAR;
                                        Color textColor = colors[0];
                                        int offset = 0;
                                        int offset2 = 0;
                                        boolean mask = false;
                                        DisplayType outline = DisplayType.NORMAL;
                                        String atcfCategory = "Text";
                                        String atcfType = "General Text";

                                        Text textElement = new Text(range,
                                                fontName, fontSize,
                                                justification, position,
                                                rotation, rotationRelativity,
                                                text, style, textColor, offset,
                                                offset2, mask, outline,
                                                atcfCategory, atcfType);

                                        elems.add(textElement);
                                    }

                                    /*
                                     * Create GPCE, GPCE climatology, and
                                     * GPCE-AX for the forecast point, if
                                     * requested.
                                     */
                                    if (useGPCE) {
                                        elems.addAll(createAllGpceElements(prop,
                                                dtg, rec.getTechnique(), tau,
                                                location, colors[0],
                                                lineWidth));
                                    }
                                }
                            }

                            /*
                             * create wind radii circles for the fix, if
                             * requested
                             */
                            int speed = (int) rec.getRadWind();
                            if ((prop.isShow34ktWindRadii() && speed == 34)
                                    || (prop.isShow50ktWindRadii()
                                            && speed == 50)
                                    || (prop.isShow64ktWindRadii()
                                            && speed == 64)) {
                                float[] windRadii = AtcfVizUtil
                                        .adjustWindQuadrants(
                                                rec.getRadWindQuad(),
                                                rec.getQuad1WindRad(),
                                                rec.getQuad2WindRad(),
                                                rec.getQuad3WindRad(),
                                                rec.getQuad4WindRad());
                                TcmWindQuarters windQuarters = new TcmWindQuarters(
                                        location, speed, windRadii[0],
                                        windRadii[1], windRadii[2],
                                        windRadii[3]);
                                Tcm tcm = new Tcm();
                                tcm.setWaveQuatro(windQuarters);
                                tcm.setAdvisoryTime(rec.getRefTimeAsCalendar());
                                elems.add(tcm);
                            }

                            previousLocation = location;
                        }
                    }
                }

                // draw aid label near last location
                if (atLeastOneMarkerDrawnForThisAidDtg) {

                    StringBuilder label = new StringBuilder();
                    label.append(aidCode);
                    Coordinate[] range = null;
                    String fontName = "Courier";
                    float fontSize = 10.0f;
                    TextJustification justification = TextJustification.LEFT_JUSTIFY;
                    Coordinate position = previousLocation;
                    double rotation = 0.0;
                    TextRotation rotationRelativity = TextRotation.SCREEN_RELATIVE;
                    String[] text = new String[] { " " + label.toString() };
                    FontStyle style = FontStyle.REGULAR;
                    Color textColor = colors[0];
                    int offset = 0;
                    int offset2 = 0;
                    boolean mask = false;
                    DisplayType outline = DisplayType.NORMAL;
                    String atcfCategory = "Text";
                    String atcfType = "General Text";

                    Text textElement = new Text(range, fontName, fontSize,
                            justification, position, rotation,
                            rotationRelativity, text, style, textColor, offset,
                            offset2, mask, outline, atcfCategory, atcfType);

                    // shouldn't happen
                    if (position != null) {
                        elems.add(textElement);
                    }
                }
            }
        }

        return elems;

    }

    /*
     * Calculate a circumference point from E-Deck record and a center point.
     *
     * @param record EDeckRecord
     *
     * @param centerPoint Coordinate (lat/lon from A-Deck)
     *
     * @return Coordinate
     */
    private Coordinate calculateGpceAXCircumPt(EDeckRecord rec,
            Coordinate centerPoint) {

        float dir = -1.0f;
        float crsTrkRds = -1.0f;
        float algTrkRds = -1.0f;

        if (rec != null) {
            dir = rec.getCrossTrackDirection();
            crsTrkRds = rec.getCrossTrackRadius();
            algTrkRds = rec.getAlongTrackRadius();
            float algTrkBias = Math.abs(rec.getAlongTrackBias());
            float crsTrkBias = Math.abs(rec.getCrossTrackBias());
            if (algTrkBias <= 2000.0) {
                algTrkRds += algTrkBias;
            }

            if (crsTrkBias <= 2000.0) {
                crsTrkRds += crsTrkBias;
            }
        }

        Coordinate circumferencePoint = null;
        if (Math.abs(dir) <= 360.0 && Math.abs(crsTrkRds) <= 2000.0
                && Math.abs(algTrkRds) <= 2000.0) {

            dir = 360.0f - dir;
            if (crsTrkRds < algTrkRds) {
                circumferencePoint = new Coordinate(
                        AtcfVizUtil.computePoint(centerPoint, crsTrkRds, dir));
            } else {
                circumferencePoint = new Coordinate(
                        AtcfVizUtil.computePoint(centerPoint, algTrkRds, dir));
            }

        }

        return circumferencePoint;
    }

    /*
     * Calculate aspect ratio from an E-Deck record's cross track and along
     * track radius, if they present; otherwise, return 1.0.
     *
     * @param record EDeckRecord
     *
     * @return ratio
     */
    private float calculateGpceAXRatio(EDeckRecord rec) {

        float crsTrkRds;
        float algTrkRds;
        float ratio = 1.0f;

        if (rec != null) {
            crsTrkRds = rec.getCrossTrackRadius();
            algTrkRds = rec.getAlongTrackRadius();
            float algTrkBias = Math.abs(rec.getAlongTrackBias());
            float crsTrkBias = Math.abs(rec.getCrossTrackBias());
            if (algTrkBias <= 2000.0) {
                algTrkRds += algTrkBias;
            }

            if (crsTrkBias <= 2000.0) {
                crsTrkRds += crsTrkBias;
            }

            if (crsTrkRds < algTrkRds) {
                ratio = crsTrkRds / algTrkRds;
            } else {
                ratio = algTrkRds / crsTrkRds;
            }
        }

        return ratio;
    }

    /*
     * Find GPCE radius from E-Deck record - namely, probability item when
     * probability is "TR".
     *
     * @param record EDeckRecord
     *
     * @return float
     */
    private float calculateGpceRadius(EDeckRecord rec) {
        float gpceRadiusNM = -1.0f;

        if (rec != null) {
            gpceRadiusNM = rec.getProbabilityItem();
        }

        return gpceRadiusNM;
    }

    /*
     * Convert a calendar to s string in format of "YYYYMMDDHH"
     *
     * @param warnTime calendar
     *
     * @return string calendar in string
     */
    private String calendarToString(Calendar warnTime) {
        int year = warnTime.get(Calendar.YEAR);
        int month = warnTime.get(Calendar.MONTH);
        int day = warnTime.get(Calendar.DAY_OF_MONTH);
        int hour = warnTime.get(Calendar.HOUR_OF_DAY);
        // synoptic hours only
        if (hour % 6 == 0) {
            return String.format("%04d", year)
                    + String.format("%02d", month + 1)
                    + String.format("%02d", day) + String.format("%02d", hour);
        }
        return null;
    }

    /*
     * Get colors based on use selection.
     *
     * @return Color[]
     */
    private Color[] getColors(ObjAidsProperties prop, ADeckRecord rec) {

        Color[] colors = new Color[] { Color.WHITE };

        int intensity = (int) rec.getWindMax();

        // Get color based on aids
        String aid = AtcfVizUtil.aidCode(rec.getTechnique());
        Color color = prop.getColorsForObjectiveAids().get(aid);
        if (color == null) {
            colors[0] = Color.WHITE;
        } else {
            colors[0] = color;
        }

        // Adjust color based on intensity
        if (prop.isColorsBySaffirSimpsonScale()) {
            if (intensity >= 137) {
                // (255,0,255)
                colors = new Color[] { Color.MAGENTA };
            } else if (intensity >= 113) {
                // (255, 0, 0)
                colors = new Color[] { Color.RED };
            } else if (intensity >= 96) {
                // TODO Colors may need to be disposed.
                // (255, 127, 0)
                colors = new Color[] { new Color(255, 127, 0) };
            } else if (intensity >= 83) {
                // (255, 255, 0)
                colors = new Color[] { Color.YELLOW };
            } else if (intensity >= 64) {
                // (0, 255, 0)
                colors = new Color[] { Color.GREEN };
            }
        } else if (prop.isColorsByIntensity()) {
            if (intensity >= 64) {
                // (255, 0, 0)
                colors = new Color[] { Color.RED };
            } else if (intensity >= 50) {
                colors = new Color[] { new Color(255, 127, 0) };
            } else if (intensity >= 34) {
                // (255, 255, 0)
                colors = new Color[] { Color.YELLOW };
            }
        }

        return colors;
    }

    /*
     * Finds a E-deck record for given DTG, technique, prob format, and forecast
     * hour. For GPAC_AX, look for record in corrected technique (ending with
     * "X").
     *
     * @param dtg
     *
     * @param tech
     *
     * @param format
     *
     * @param tau
     *
     * @param forAX flag to indicate searching in corrected technique.
     *
     * @return EDeckRecord
     */
    private EDeckRecord findEDeckRecords(String dtg, String tech, String format,
            int tau, boolean forAX) {

        EDeckRecord rec = null;
        List<EDeckRecord> recs = edeckData.get(dtg);
        if (recs != null) {

            // For GPCE-AX, technique ends with "X".
            if (forAX) {
                tech = tech.substring(0, 3) + "X";
            }

            for (EDeckRecord er : recs) {
                if (er.getFcstHour() == tau && "TR".equalsIgnoreCase(format)
                        && tech.equalsIgnoreCase(er.getTechnique())) {
                    rec = er;
                    break;
                }
            }
        }

        return rec;
    }

    /*
     * Find if a technique in A-Deck may have GPCE-related info in E-Deck by
     * checking if the technique is one of E-Deck's techniques with format "TR".
     *
     * @param tech
     *
     * @return boolean
     */
    private boolean usableForGPCE(String tech) {
        return (edeckTRTech != null && edeckTRTech.contains(tech));
    }

    /*
     * Prepare E-Deck data for drawing GPCE etc.
     */
    private void prepareEDeckData() {
        // Get the EDeck data if not retrieved yet.
        if (edeckData == null) {
            edeckData = AtcfDataUtil.getEDeckRecords(
                    atcfResource.getResourceData().getActiveStorm());

            // Find all tech in E-Deck that has format "TR".
            edeckTRTech = new TreeSet<>();
            for (List<EDeckRecord> erecs : edeckData.values()) {
                if (erecs != null) {
                    for (EDeckRecord rec : erecs) {
                        float dir = Math.abs(rec.getCrossTrackDirection());
                        float crsRad = Math.abs(rec.getCrossTrackRadius());
                        float algRad = Math.abs(rec.getAlongTrackRadius());
                        String fmt = rec.getProbFormat();
                        if (TRACK_PROB_FORMAT.equalsIgnoreCase(fmt)
                                && dir == 0.0f && crsRad == 0.0f
                                && algRad == 0.0f) {
                            edeckTRTech.add(rec.getTechnique());
                        }
                    }
                }
            }
        }
    }

    /*
     * Creates GPCE or GPCE Climatology arcs.
     *
     * @param dtg Date/Time Group
     *
     * @param tech Technique ("GPCC" for GPCE Climatology)
     *
     * @param tau Forecast hour
     *
     * @param location forecast location
     *
     * @param color color
     *
     * @param lineWidth line width
     *
     * @return Arc for GPCE-AX.
     */
    private Arc createGPCECircle(String dtg, String tech, int tau,
            Coordinate location, Color color, float lineWidth) {

        Arc confidenceCircle = null;

        EDeckRecord eRec = findEDeckRecords(dtg, tech, TRACK_PROB_FORMAT, tau,
                false);

        float gpceRadiusNM = calculateGpceRadius(eRec);

        if (gpceRadiusNM > 0.0) {
            float gpceRadiusDegrees = gpceRadiusNM / 60.0f;
            double latOfCircumPt = location.y + gpceRadiusDegrees;
            if (latOfCircumPt > 90.0) {
                latOfCircumPt = location.y - gpceRadiusDegrees;
            }

            Coordinate[] range = null;
            double sizeScale2 = 1.0;
            boolean closed = true;
            boolean filled = false;
            int smoothFactor = 2;
            FillPattern fillPattern = FillPattern.TRANSPARENCY;
            String atcfType = "Circle";
            Coordinate centerPoint = location;
            Coordinate circumferencePoint = new Coordinate(location.x,
                    latOfCircumPt);
            String atcfCategory = "Arc";
            double axisRatio = 1.0;
            double startAngle = 0.0;
            double endAngle = 360.0;

            confidenceCircle = new Arc(range, color, lineWidth, sizeScale2,
                    closed, filled, smoothFactor, fillPattern, atcfType,
                    centerPoint, circumferencePoint, atcfCategory, axisRatio,
                    startAngle, endAngle);
        }

        return confidenceCircle;

    }

    /*
     * Creates GPCE-AX arcs.
     *
     * @param dtg Date/Time Group
     *
     * @param tech Technique
     *
     * @param tau Forecast hour
     *
     * @param location forecast location
     *
     * @param color color
     *
     * @param lineWidth line width
     *
     * @return Arc for GPCE-AX.
     */
    private Arc createGpceAXEllipse(String dtg, String tech, int tau,
            Color color, float lineWidth) {

        Arc confidenceCircle = null;

        EDeckRecord eRec = findEDeckRecords(dtg, tech, TRACK_PROB_FORMAT, tau,
                true);

        Coordinate centerPt = null;
        Coordinate circumferencePoint = null;

        /*
         * For GPCE-AX, looks like it uses the lat/lon in EDeckRecord, not the
         * one in the A-DeckRecord.
         */
        if (eRec != null) {
            float elat = eRec.getClat();
            float elon = eRec.getClon();
            if (Math.abs(elat) <= 90.0 && Math.abs(elon) <= 360.0) {
                centerPt = new Coordinate(elon, elat);
                circumferencePoint = calculateGpceAXCircumPt(eRec, centerPt);
            }
        }

        if (circumferencePoint != null) {
            Coordinate[] range = null;
            double sizeScale2 = 1.0;
            boolean closed = true;
            boolean filled = false;
            int smoothFactor = 2;
            FillPattern fillPattern = FillPattern.TRANSPARENCY;
            String atcfType = "Ellipse"; // "Circle"
            String atcfCategory = "Arc";
            double axisRatio = calculateGpceAXRatio(eRec);
            double startAngle = 0.0;
            double endAngle = 360.0;

            confidenceCircle = new Arc(range, color, lineWidth, sizeScale2,
                    closed, filled, smoothFactor, fillPattern, atcfType,
                    centerPt, circumferencePoint, atcfCategory, axisRatio,
                    startAngle, endAngle);
        }

        return confidenceCircle;

    }

    /*
     * Creates GPCE, GPCE Climatology, and GPCE-AX arcs.
     *
     * @param prop ObjAidsProperties
     *
     * @param dtg Date/Time Group
     *
     * @param tech Technique
     *
     * @param tau Forecast hour
     *
     * @param location forecast location
     *
     * @param color color
     *
     * @param lineWidth line width
     *
     * @return List of Arcs.
     */
    private List<AbstractDrawableComponent> createAllGpceElements(
            ObjAidsProperties prop, String dtg, String tech, int tau,
            Coordinate location, Color color, float lineWidth) {

        List<AbstractDrawableComponent> gpceArcs = new ArrayList<>();

        if (prop.isGpce()) {
            Arc gpceCirc = createGPCECircle(dtg, tech, tau, location, color,
                    lineWidth);
            if (gpceCirc != null) {
                gpceArcs.add(gpceCirc);
            }
        }

        if (prop.isGpceClimatology()) {
            Arc gpceClimateCirc = createGPCECircle(dtg, GPCE_CLIMATE_TECH, tau,
                    location, GPCE_CLIMATE_COLOR, lineWidth);
            if (gpceClimateCirc != null) {
                gpceArcs.add(gpceClimateCirc);
            }
        }

        if (prop.isGpceAX()) {
            Arc gpceAxEllip = createGpceAXEllipse(dtg, tech, tau, color,
                    lineWidth);
            if (gpceAxEllip != null) {
                gpceArcs.add(gpceAxEllip);
            }
        }

        return gpceArcs;
    }

}
