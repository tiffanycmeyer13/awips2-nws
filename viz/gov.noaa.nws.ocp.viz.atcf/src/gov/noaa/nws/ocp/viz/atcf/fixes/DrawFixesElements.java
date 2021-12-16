/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FixFormat;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesDialog.NonCenterFixMode;
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
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.Tcm;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.TcmWindQuarters;

/**
 * Draw F-Deck data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Nov 28, 2018 57484       jwu         Initial creation.
 * Mar 28, 2019 61882       jwu         Move adjustWindQuadrant() to AtfcVizUtil.
 *
 * </pre>
 *
 * @author jwu
 * @version 1
 */
public class DrawFixesElements {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DrawFixesElements.class);

    private AtcfResource atcfResource;

    private DisplayFixesProperties displayProperties;

    /**
     * Constructor
     */
    public DrawFixesElements() {
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        displayProperties = new DisplayFixesProperties();
    }

    /**
     * Constructor
     */
    public DrawFixesElements(AtcfResource rsc,
            DisplayFixesProperties displayProperties) {
        this.atcfResource = rsc;
        this.displayProperties = displayProperties;
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
     * @return the displayProperties
     */
    public DisplayFixesProperties getDisplayProperties() {
        return displayProperties;
    }

    /**
     * @param displayProperties
     *            the displayProperties to set
     */
    public void setDisplayProperties(DisplayFixesProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

    /**
     * Draw all F-Deck records
     *
     * @param removeExistingElems
     *            Remove elements on F-Deck layer, if true.
     * @param apply
     *            Move elements onto F-Deck layer, if true.
     */
    public void draw() {

        // If no ATCF resource, get the current AtcfResource.
        if (atcfResource == null) {
            atcfResource = AtcfSession.getInstance().getAtcfResource();
        }

        // Set the display properties.
        if (displayProperties == null) {
            displayProperties = new DisplayFixesProperties();
        }

        atcfResource.getResourceData().getActiveAtcfProduct()
        .setDisplayFixesProperties(displayProperties);

        // Retrieve all F-Deck records.
        List<FDeckRecord> pdos = atcfResource.getResourceData()
                .getActiveAtcfProduct().getFDeckData();

        // Create all elements.
        List<AbstractDrawableComponent> elems = createElements(pdos,
                displayProperties);

        // Add elements onto F-Deck layer.
        Layer fLayer = atcfResource.getResourceData().getActiveAtcfProduct()
                .getFDeckLayer();

        atcfResource.setActiveLayer(fLayer);
        fLayer.removeAllElements();

        if (!elems.isEmpty()) {
            atcfResource.addElements(elems);
        }

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor != null) {
            editor.refresh();
        }
    }

    /**
     * Remove all F-Deck elements
     *
     */
    public void remove() {

        // If no ATCF resource, get the current AtcfResource.
        if (atcfResource == null) {
            atcfResource = AtcfSession.getInstance().getAtcfResource();
        }

        // Remove elements on F-Deck layer.
        Layer fLayer = atcfResource.getResourceData().getActiveAtcfProduct()
                .getFDeckLayer();

        atcfResource.setActiveLayer(fLayer);
        fLayer.removeAllElements();

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor != null) {
            editor.refresh();
        }
    }

    /*
     * Create graphic elements for selected F-Deck records.
     *
     * @param pdos List<FDeckRecord>
     *
     * @return attr DisplayFixesProperties
     */
    private List<AbstractDrawableComponent> createElements(
            List<FDeckRecord> pdos, DisplayFixesProperties attr) {

        List<AbstractDrawableComponent> elems = new ArrayList<>();

        // Get selected fix types and sites for filtering
        List<String> selectedFixSites = Arrays
                .asList(attr.getSelectedFixSites());
        List<String> selectedFixTypes = Arrays
                .asList(attr.getSelectedFixTypes());

        // Draw retrieved F-Deck data on active layer
        for (FDeckRecord fdeckRecord : pdos) {

            // Filter on whether fix site and type are both selected
            if (selectedFixSites.contains(fdeckRecord.getFixSite())
                    && selectedFixTypes.contains(fdeckRecord.getFixType())) {

                Calendar fixTime = fdeckRecord.getRefTimeAsCalendar();

                // Filter on "Limit Fix Display" option and time range,
                if (!attr.getLimitFixDisplay() || isFixTimeInSelectedRange(
                        fixTime, attr.getDateTimeGroup(),
                        attr.getPlusMinusHoursForLimitFixDisplay())) {

                    // TODO check if following is correct criterion in legacy
                    boolean isNonCenterFix = !fdeckRecord.getCenterOrIntensity()
                            .contains("C");

                    if (isNonCenterFix && attr
                            .getNonCenterFixMode() == NonCenterFixMode.HIDE) {
                        continue;
                    }

                    Color[] colors = null;

                    if (!attr.getHighlightFixes() || isFixTimeInSelectedRange(
                            fixTime, attr.getDateTimeGroup(),
                            attr.getPlusMinusHoursForHighlightFixes())) {
                        if (isNonCenterFix && attr
                                .getNonCenterFixMode() == NonCenterFixMode.USE_NON_CENTER_FIX_COLOR) {
                            colors = getNonCenterFixColor();
                        } else {
                            colors = getColorForFixFormat(
                                    fdeckRecord.getFixFormat());
                        }
                    } else {
                        // vanilla for non-highlighted fix
                        colors = new Color[] { new Color(255, 228, 220) };
                    }

                    double clat = fdeckRecord.getClat();
                    double clon = fdeckRecord.getClon();
                    if (Math.abs(clat) > 90.0 || Math.abs(clon) > 360.0) {
                        continue;
                    }

                    Coordinate location = new Coordinate(clon, clat);

                    // symbol type depends on confidence rating of the fix
                    String markerType = null;
                    Double sizeScale = 1.0;
                    if ("1".equals(fdeckRecord.getPositionConfidence())) {
                        markerType = "FILLED_BOX";
                        sizeScale = 0.7;
                    } else if ("2"
                            .equals(fdeckRecord.getPositionConfidence())) {
                        markerType = "FILLED_TRIANGLE";
                    } else if ("3"
                            .equals(fdeckRecord.getPositionConfidence())) {
                        markerType = "FILLED_DIAMOND";
                    }
                    // default case; no CI confidence indicated
                    else {
                        markerType = "FILLED_OCTAGON";
                    }

                    // create a symbol element at the fix point

                    Symbol symbol = new Symbol(null, colors, 1.0f, sizeScale,
                            false, location, "Marker", markerType);

                    elems.add(symbol);

                    // create confidence circle for the fix, if requested
                    if (attr.getConfidence() && isFixTimeInSelectedRange(
                            fixTime, attr.getDateTimeGroup(),
                            attr.getPlusMinusHoursForConfidence())) {

                        float confidenceRadiusNM = calculateConfidenceRadiusNM(
                                fdeckRecord);
                        if (confidenceRadiusNM > 0.0) {
                            float confidenceRadiusDegrees = confidenceRadiusNM
                                    / 60.0f;
                            double latitudeOfCircumferencePoint = location.y
                                    + confidenceRadiusDegrees;
                            if (latitudeOfCircumferencePoint > 90.0) {
                                // because defensive programming
                                // (albeit *way* extratropical !)
                                latitudeOfCircumferencePoint = location.y
                                        - confidenceRadiusDegrees;
                            }

                            Coordinate[] range = null;
                            Color color = colors[0];
                            float lineWidth = 1.0f;
                            double sizeScale2 = 1.0;
                            boolean closed = true;
                            boolean filled = false;
                            int smoothFactor = 2;
                            FillPattern fillPattern = FillPattern.TRANSPARENCY;
                            String atcfType = "Circle";
                            Coordinate centerPoint = location;
                            Coordinate circumferencePoint = new Coordinate(
                                    location.x, latitudeOfCircumferencePoint);
                            String atcfCategory = "Arc";
                            double axisRatio = 1.0;
                            double startAngle = 0.0;
                            double endAngle = 360.0;

                            Arc confidenceCircle = new Arc(range, color,
                                    lineWidth, sizeScale2, closed, filled,
                                    smoothFactor, fillPattern, atcfType,
                                    centerPoint, circumferencePoint,
                                    atcfCategory, axisRatio, startAngle,
                                    endAngle);

                            elems.add(confidenceCircle);
                        }
                    }

                    // Create wind radii circles for the fix, if requested
                    if (attr.getWindRadii()
                            && fdeckRecord.getRadiusOfWindIntensity() < 999
                            && isFixTimeInSelectedRange(fixTime,
                                    attr.getDateTimeGroup(),
                                    attr.getPlusMinusHoursForWindRadii())) {
                        int speed = (int) fdeckRecord
                                .getRadiusOfWindIntensity();
                        float[] windRadii = AtcfVizUtil.adjustWindQuadrants(
                                fdeckRecord.getWindCode(),
                                fdeckRecord.getWindRad1(),
                                fdeckRecord.getWindRad2(),
                                fdeckRecord.getWindRad3(),
                                fdeckRecord.getWindRad4());
                        TcmWindQuarters windQuarters = new TcmWindQuarters(
                                location, speed, windRadii[0], windRadii[1],
                                windRadii[2], windRadii[3]);
                        Tcm tcm = new Tcm();
                        tcm.setWaveQuatro(windQuarters);
                        tcm.setAdvisoryTime(fixTime);
                        elems.add(tcm);
                    }

                    // Create a label string for the fix, if requested
                    if (attr.getAutoLabel() && isFixTimeInSelectedRange(fixTime,
                            attr.getDateTimeGroup(),
                            attr.getPlusMinusHoursForAutoLabel())) {
                        StringBuilder label = new StringBuilder();
                        label.append(" ");
                        label.append(getLabelDTG(fixTime));
                        label.append(" ");
                        label.append(fdeckRecord.getPositionConfidence());
                        // TODO use symbolic constant for unknown value
                        if (fdeckRecord.getWindMax() < 999999.0) {
                            label.append(" ");
                            label.append((int) fdeckRecord.getWindMax());
                            label.append("kt ");
                        }
                        label.append(fdeckRecord.getWindMaxConfidence());
                        if (attr.gettAndCILabels()
                                && (fdeckRecord.getFixFormat().equals(
                                        FixFormat.SUBJECTIVE_DVORAK.getValue())
                                        || fdeckRecord.getFixFormat()
                                        .equals(FixFormat.OBJECTIVE_DVORAK
                                                .getValue()))
                                && fdeckRecord.getDvorakCodeLongTermTrend()
                                .length() >= 4) {
                            label.append(" D");
                            label.append(
                                    fdeckRecord.getDvorakCodeLongTermTrend()
                                    .substring(0, 4));
                        }
                        if (attr.getFixSiteLabels()) {
                            label.append(" ");
                            label.append(fdeckRecord.getFixSite());
                        }
                        if (attr.getShowComments() && !fdeckRecord.getComments()
                                .trim().isEmpty()) {
                            label.append(" \"");
                            label.append(fdeckRecord.getComments().trim());
                            label.append("\"");
                        }

                        // add the constructed label to the display
                        Coordinate[] range = null;
                        String fontName = "Courier";
                        float fontSize = 10.0f;
                        TextJustification justification = TextJustification.LEFT_JUSTIFY;
                        Coordinate position = location;
                        double rotation = 0.0;
                        TextRotation rotationRelativity = TextRotation.SCREEN_RELATIVE;
                        String[] text = new String[] { label.toString() };
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
                                rotationRelativity, text, style, textColor,
                                offset, offset2, mask, outline, atcfCategory,
                                atcfType);

                        elems.add(textElement);
                    }
                }
            }
        }

        return elems;

    }

    /**
     * Determines the confidence radius for a given fix.
     *
     * Algorithm derived from ATCF fixes.c subroutine doConf
     *
     * @param fdeckRecord
     *            FDeckRecord to be calculated.
     * @return confidence radius in nautical miles.
     */
    private float calculateConfidenceRadiusNM(FDeckRecord fdeckRecord) {

        float confidenceRadiusNM;

        // valid values are 1=high 2=medium 3=low
        int confidenceLevel = 0;

        // Get the confidence level from the FDeckRecord
        try {
            confidenceLevel = Integer
                    .parseInt(fdeckRecord.getPositionConfidence());
        } catch (NumberFormatException e) {
            // invalid or missing
            confidenceLevel = 0;
        }

        /*
         * If invalid or missing, try deriving from the (deprecated) PCN code.
         * If that fails, just go with confidence level 3 (low)
         */
        if (confidenceLevel < 1 || confidenceLevel > 3) {
            int pcn;
            try {
                pcn = Integer.parseInt(fdeckRecord.getPcnCode());
            } catch (NumberFormatException e) {
                pcn = 0;
            }
            if (pcn < 1 || pcn > 6) {
                confidenceLevel = 3;
            } else if (pcn > 0 || pcn < 7) {
                confidenceLevel = (pcn - 1) / 2 + 1;
            } else {
                confidenceLevel = 3;
            }
        }

        String fixFormat = fdeckRecord.getFixFormat();
        if (fixFormat
                .equalsIgnoreCase(FixFormat.SUBJECTIVE_DVORAK.getValue())) {
            confidenceRadiusNM = 20 * confidenceLevel;
        } else if (fixFormat
                .equalsIgnoreCase(FixFormat.OBJECTIVE_DVORAK.getValue())) {
            confidenceRadiusNM = 20 * confidenceLevel;
        } else if (fixFormat.equalsIgnoreCase(FixFormat.MICROWAVE.getValue())) {
            confidenceRadiusNM = 20 * confidenceLevel;
        } else if (fixFormat
                .equalsIgnoreCase(FixFormat.SCATTEROMETER.getValue())) {
            confidenceRadiusNM = 20 * confidenceLevel;
        } else if (fixFormat.equalsIgnoreCase(FixFormat.RADAR.getValue())) {
            confidenceRadiusNM = 10 * confidenceLevel;
        } else if (fixFormat.equalsIgnoreCase(FixFormat.AIRCRAFT.getValue())) {
            confidenceRadiusNM = 5 * confidenceLevel;
        } else if (fixFormat.equalsIgnoreCase(FixFormat.DROPSONDE.getValue())) {
            confidenceRadiusNM = 5 * confidenceLevel;
        } else if (fixFormat.equalsIgnoreCase(FixFormat.ANALYSIS.getValue())) {
            confidenceRadiusNM = 5 * confidenceLevel;
        } else {
            confidenceRadiusNM = 60;
        }

        if (confidenceRadiusNM < 0.0f || confidenceRadiusNM > 60.0f) {
            confidenceRadiusNM = 0.0f;
        }
        return confidenceRadiusNM;
    }

    /*
     * Check if a fix is within a selected time range
     *
     * @param fixTime time for fix
     *
     * @return Object a Stringbuilder.
     */
    private Object getLabelDTG(Calendar fixTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", fixTime.get(Calendar.DAY_OF_MONTH)));
        sb.append(String.format("%02d", fixTime.get(Calendar.HOUR_OF_DAY)));
        sb.append(String.format("%02d", fixTime.get(Calendar.MINUTE)));
        sb.append("Z");
        return sb;
    }

    /*
     * Check if a fix is within a selected time range
     *
     * @param fixTime time for fix
     *
     * @param dateTimeGroup Base time
     *
     * @param plusMinusHours hours before/after base time
     *
     * @return true - in rage; false - not in range.
     */
    private boolean isFixTimeInSelectedRange(Calendar fixTime,
            String dateTimeGroup, int plusMinusHours) {
        Calendar selectedDTG = getCalendarFromString(dateTimeGroup);
        if (selectedDTG != null) {
            long msDiff = fixTime.getTimeInMillis()
                    - selectedDTG.getTimeInMillis();
            long hourDiff = msDiff / (3600 * 1000);
            return Math.abs(hourDiff) <= plusMinusHours;
        } else {
            return false;
        }
    }

    /*
     * Get a Calendar (UTC) from a given DTG.
     *
     * @param dateTimeGroup
     *
     * @return Calendar.
     */
    private Calendar getCalendarFromString(String dateTimeGroup) {
        try {
            int year = Integer.parseInt(dateTimeGroup.substring(0, 4));
            int month = Integer.parseInt(dateTimeGroup.substring(4, 6));
            int date = Integer.parseInt(dateTimeGroup.substring(6, 8));
            int hour = Integer.parseInt(dateTimeGroup.substring(8, 10));
            Calendar returnValue = Calendar
                    .getInstance(TimeZone.getTimeZone("UTC"));
            returnValue.set(Calendar.YEAR, year);
            returnValue.set(Calendar.MONTH, month - 1);
            returnValue.set(Calendar.DAY_OF_MONTH, date);
            returnValue.set(Calendar.HOUR_OF_DAY, hour);
            returnValue.set(Calendar.MINUTE, 0);
            returnValue.set(Calendar.SECOND, 0);
            returnValue.set(Calendar.MILLISECOND, 0);
            return returnValue;
        } catch (Exception e) {
            logger.warn("DrawFixesElements - Failed to parse dateTimeGroup: "
                    + dateTimeGroup, e);
            return null;
        }
    }

    /*
     * Get color for given fix format.
     *
     * @return Color[] color for given fix format.
     */
    private Color[] getColorForFixFormat(String fixFormat) {
        // TODO - Need to use localization, maybe fixerror.pref?
        Color[] colors = new Color[] { new Color(0, 255, 0) };

        if (fixFormat
                .equalsIgnoreCase(FixFormat.SUBJECTIVE_DVORAK.getValue())) {
            colors = new Color[] { new Color(255, 0, 0) };
        } else if (fixFormat
                .equalsIgnoreCase(FixFormat.OBJECTIVE_DVORAK.getValue())) {
            colors = new Color[] { new Color(255, 255, 255) };
        } else if (fixFormat.equalsIgnoreCase(FixFormat.MICROWAVE.getValue())) {
            colors = new Color[] { new Color(0, 255, 0) };
        } else if (fixFormat
                .equalsIgnoreCase(FixFormat.SCATTEROMETER.getValue())) {
            colors = new Color[] { new Color(255, 228, 220) };
        } else if (fixFormat.equalsIgnoreCase(FixFormat.RADAR.getValue())) {
            colors = new Color[] { new Color(0, 255, 255) };
        } else if (fixFormat.equalsIgnoreCase(FixFormat.AIRCRAFT.getValue())) {
            colors = new Color[] { new Color(255, 0, 255) };
        } else if (fixFormat.equalsIgnoreCase(FixFormat.DROPSONDE.getValue())) {
            colors = new Color[] { new Color(0, 0, 0) };
        } else if (fixFormat.equalsIgnoreCase(FixFormat.ANALYSIS.getValue())) {
            colors = new Color[] { new Color(255, 255, 255) };
        }

        return colors;
    }

    /*
     * Get color for non-center fixes
     *
     * @return Color[] color for non-center fixes.
     */
    private Color[] getNonCenterFixColor() {
        // TODO for now, using GEMPAK "orange"; adjust if necessary
        return new Color[] { new Color(255, 127, 0) };
    }

}
