/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences.PreferenceOptions;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionNames;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.TrackColorUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;
import gov.noaa.nws.ocp.viz.drawing.display.IText.FontStyle;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextJustification;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextRotation;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.Arc;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.Tcm;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.TcmWindQuarters;

/**
 * Class to create/display graphic elements for a storm's best track (B-Deck).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 23, 2019 60613       jwu         Initial creation.
 * Mar 12, 2019 61359       jwu         Implement "Toggle Bold Track Line".
 * Mar 18, 2019 61439       jwu         Implement track color option.
 * Mar 22, 2019 61613       jwu         Add flag to show/hide best track.
 * Apr 02, 2019 61882       jwu         Add options for wind radii/intensity/labels.
 * Apr 03, 2019 61789       dmanzella   Add support for best track radii
 * Apr 08, 2019 62029       jwu         Add support for drawing from "preference".
 * Apr 10  2018 62427       jwu         Recreate only when BDeck layer's display is on.
 * Apr 16, 2019 62175       dmanzella   Added support for drawing rmw/roci
 * May 03, 2019 62845       dmanzella   Implemented some best track drawing options
 * May 07, 2019 63005       jwu         Remove reserved color for active storm.
 * May 16, 2019 63773       dmanzella   Implemented drawing different symbols
 * May 28, 2019 63377       jwu         Allow overlay of baseline best track.
 * Dec 17, 2020 86027       jwu         Update for displaying genesis.
 * May 17, 2021 91567       jwu         Draw storm landing record as a FILLED_SQUARE.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class BestTrackGenerator {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(BestTrackGenerator.class);

    // Max wind limit (knots)
    private static final int MAX_WIND_SPEED = 250;

    // ATCF resource for the storm.
    private AtcfResource atcfResource;

    // Drawing options for creating/displaying best track.
    private BestTrackProperties displayProperties;

    // The storm to work on.
    private Storm storm;

    // Flag to indicate if baseline B-Deck needs to be drawn.
    private boolean showBaseline;

    /**
     * Constructor
     */
    public BestTrackGenerator() {
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        displayProperties = new BestTrackProperties();
        storm = ((AtcfProduct) atcfResource.getActiveProduct()).getStorm();
        showBaseline = false;
    }

    /**
     * Constructor
     *
     * @param rsc
     *            AtcfResource
     * @param displayProperties
     *            BestTrackProperties
     * @param storm
     *            Storm
     *
     */
    public BestTrackGenerator(AtcfResource rsc,
            BestTrackProperties displayProperties, Storm storm) {
        this.atcfResource = rsc;
        this.displayProperties = displayProperties;
        this.storm = storm;
        this.showBaseline = false;
    }

    /**
     * Constructor
     *
     * @param rsc
     *            AtcfResource
     * @param displayProperties
     *            BestTrackProperties
     * @param storm
     *            Storm
     * @param baseline
     *            boolean
     *
     */
    public BestTrackGenerator(AtcfResource rsc,
            BestTrackProperties displayProperties, Storm storm,
            boolean baseline) {
        this.atcfResource = rsc;
        this.displayProperties = displayProperties;
        this.storm = storm;
        this.showBaseline = baseline;
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
    public BestTrackProperties getDisplayProperties() {
        return displayProperties;
    }

    /**
     * @param displayProperties
     *            the displayProperties to set
     */
    public void setDisplayProperties(BestTrackProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    public boolean isShowBaseline() {
        return showBaseline;
    }

    public void setShowBaseline(boolean showBaseline) {
        this.showBaseline = showBaseline;
    }

    /**
     * Create and display the best track. This method always turn on the display
     * of B-Deck layer.
     */
    public void create() {
        create(true);
    }

    /**
     * Create and display the best track. Track is recreated if "display" flag
     * is true.
     *
     * @param display
     *            boolean, flag to indicate if B-Deck needs to display or hide.
     */
    public void create(boolean display) {

        // Sanity check
        if (atcfResource == null || displayProperties == null
                || storm == null) {
            logger.warn(
                    "BestTrackGenerator - missing information, no best track is generated.");
            return;
        }

        AtcfProduct prd = atcfResource.getResourceData().getAtcfProduct(storm);
        prd.getBDeckLayer().setOnOff(display);

        // Recreate if B-Deck layer display is on.
        if (display) {

            prd.setBestTrackProperties(displayProperties);

            // Retrieve all B-Deck records.
            Map<String, List<BDeckRecord>> bdecks = prd.getBDeckDataMap();

            // Create all elements.
            List<AbstractDrawableComponent> elems = createElements(
                    bdecks, displayProperties);

            // Remove elements in B-Deck layer.
            atcfResource.resetLayer(prd.getBDeckLayer());

            // Add elements onto B-Deck layer with the new set of elements.
            if (!elems.isEmpty()) {
                atcfResource.addElements(elems, prd, prd.getBDeckLayer());
            }

            // Create elements from baseline track if needed.
            if (isShowBaseline()) {
                Map<String, List<BDeckRecord>> bdks = AtcfDataUtil
                        .getBDeckBaseLineRecords(storm);
                List<AbstractDrawableComponent> baseLineElems = createElements(
                        bdks, displayProperties);
                atcfResource.addElements(baseLineElems, prd,
                        prd.getBDeckLayer());
            }
        }

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor != null) {
            editor.refresh();
        }

    }

    /**
     * Redraws active storm's best track based on preference options.
     *
     * @param ps
     *            PreferenceOptions
     * @param sel
     *            value of the PreferenceOptions
     */
    public static void redraw(PreferenceOptions ps, boolean sel) {

        AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
        Storm activeStorm = rsc.getResourceData().getActiveStorm();

        if (activeStorm == null) {
            logger.warn(
                    "Atcf BestTrackGenerator: No active storm is selected yet.");
            return;
        }

        AtcfProduct prd = rsc.getResourceData().getAtcfProduct(activeStorm);
        BestTrackProperties prop = prd.getBestTrackProperties();
        boolean redraw = false;
        // Update drawing properties.
        if (prop != null) {
            redraw = true;
            switch (ps) {
            case BTRACKDASHNDOTSON:
                prop.setTrackLineTypes(sel);
                break;
            case BTRACKINTENSITIESON:
                prop.setTrackIntensities(sel);
                break;
            case BTRACKWINDRADIION:
                // TODO control all wind radii?
                break;
            case BTRACKSPECIALSTTYPE:
                prop.setSpecialTypePosition(sel);
                break;
            case DISPSTORMSYMBOLS:
                prop.setStormSymbols(sel);
                break;
            case DISPSTORMNUMBER:
                prop.setStormNumber(sel);
                break;
            case DISPTRACKLINESLEGEND:
                prop.setTrackLineLegend(sel);
                break;
            case BTRACKCOLORINTENSITY:
                prop.setColorsOnIntensity(sel);
                break;
            case DISPTRACKCOLORLEG:
                prop.setIntensityColorLegend(sel);
                break;
            case STRACKCOLORSSSSCALE:
                prop.setColorsOnCategory(sel);
                break;
            case DISPSSCOLORLEGEND:
                prop.setCategoryColorLegend(sel);
                break;
            case DISPTRACKLABELS:
                prop.setTrackInfoLabel(sel);
                break;
            case BTRACKLABELSON:
                prop.setTrackLabels(sel);
                break;
            default:
                redraw = false;
                break;
            }
        }

        // Redraw if needed.
        if (redraw) {
            BestTrackGenerator btkGen = new BestTrackGenerator(rsc, prop,
                    activeStorm);
            btkGen.create();
        }
    }

    /*
     * Create graphic elements for selected B-Deck records.
     *
     * @param bdeck Map<String,List<BDeckRecord>>
     *
     * @param attr BestTrackProperties
     *
     * @return List<AbstractDrawableComponent>
     */
    private List<AbstractDrawableComponent> createElements(
            Map<String, List<BDeckRecord>> bdeck, BestTrackProperties attr) {

        List<AbstractDrawableComponent> elems = new ArrayList<>();

        // TODO More logic needs to be added here for drawing options stored in
        // BestTrackProperties, which may be modified for "Preference" dialog,
        // Track menu, or sidebar actions.
        if (bdeck != null && !bdeck.isEmpty()) {

            List<AbstractDrawableComponent> symbs = new ArrayList<>();
            List<Coordinate> points = new ArrayList<>();

            // Check if bold line is desired.
            float lineWidth = 2.0f;
            if (attr.isBoldLine()) {
                lineWidth = 5.0f;
            }

            // Find the color to be used for track line.
            List<Integer> usedColors = atcfResource.getResourceData()
                    .getUsedTrackColorIndexes();
            TrackColorUtil tcu = new TrackColorUtil(usedColors);

            Color trackLineElemColor = tcu
                    .getAtcfCustomColor(attr.getTrackColor());
            Color stormTrackColor = tcu
                    .getAtcfCustomColor(attr.getTrackColor());

            // Get colors for wind radii.
            Color track34ktClr = tcu
                    .getPreferedColor(ColorSelectionNames.WIND_RAD_34);
            Color track50ktClr = tcu
                    .getPreferedColor(ColorSelectionNames.WIND_RAD_50);
            Color track64ktClr = tcu
                    .getPreferedColor(ColorSelectionNames.WIND_RAD_64);
            float prevLon = -999;
            float prevLat = -999;
            Color prevCol = trackLineElemColor;
            int prevWind = 0;
            String lineType = "";
            String intensity = "";

            /*
             * Check if there is only one DTG (location). If so, a symbol will
             * be drawn regardless.
             */
            boolean singleDtg = (bdeck.size() == 1);

            /*
             * Check if this is a genesis. If so, a symbol will be drawn
             * regardless.
             */
            boolean isGenesis = storm.getStormName().startsWith("GENESIS");

            /*
             * Each DTG may have up to 3 records (34kt, 50kt, 64kt), but the
             * location is the same.
             */
            String subRegion = "";
            for (Map.Entry<String, List<BDeckRecord>> entry : bdeck
                    .entrySet()) {
                String dtg = entry.getKey();
                boolean specialDtg = false;

                // Check if the DTG has minutes.
                if (dtg.length() > 10) {
                    specialDtg = true;
                }

                // Check if this DTG is within the selected range.
                if (isSelectedRange(dtg, attr)) {
                    List<BDeckRecord> recs = entry.getValue();

                    if (recs != null && !recs.isEmpty()) {
                        int wind = (int) recs.get(0).getWindMax();

                        float clat = recs.get(0).getClat();
                        float clon = recs.get(0).getClon();
                        intensity = recs.get(0).getIntensity();

                        if (Math.abs(clat) > 90.0 || Math.abs(clon) > 360.0) {
                            continue;
                        }

                        Coordinate loc = new Coordinate(clon, clat);
                        points.add(loc);

                        // Find sub-region.
                        if (subRegion.isEmpty()) {
                            subRegion = recs.get(0).getSubRegion().trim();
                        }

                        // dot/dash/solid toggle
                        if (attr.isTrackLineTypes()) {
                            lineType = makeDashDot(prevWind);
                        } else {
                            lineType = LineType.SOLID.getLineType();
                        }

                        // Colors for the line
                        trackLineElemColor = getLineColor(wind, stormTrackColor,
                                track34ktClr, track64ktClr, tcu, attr);

                        // Best Track Line
                        if (prevLon != -999 && prevLat != -999) {

                            List<Coordinate> currSeg = new ArrayList<>();
                            Coordinate prevLoc = new Coordinate(prevLon,
                                    prevLat);

                            currSeg.add(prevLoc);
                            currSeg.add(loc);
                            Line line = createLineElement(currSeg, prevCol,
                                    lineWidth, lineType);
                            elems.add(line);
                        }

                        // Best track symbols
                        if (attr.isStormSymbols() || singleDtg) {
                            Color symbolColor = trackLineElemColor;
                            if (attr.isColorsOnCategory()) {
                                symbolColor = getColorFromSS(wind, tcu,
                                        stormTrackColor);
                            } else if (!attr.isColorsOnCategory()
                                    && attr.isColorsOnIntensity()) {
                                symbolColor = getColorFromIntensity(wind,
                                        stormTrackColor, track34ktClr,
                                        track64ktClr);
                            }

                            Symbol symbol;
                            if (!specialDtg) {
                                if (!isGenesis
                                        && attr.isSpecialTypePosition()) {

                                    if (intensity.equals(
                                            StormDevelopment.DB.toString())
                                            || intensity
                                            .equals(StormDevelopment.HU
                                                    .toString())
                                            || intensity
                                            .equals(StormDevelopment.TS
                                                    .toString())
                                            || intensity
                                            .equals(StormDevelopment.TD
                                                    .toString())) {
                                        symbol = createSymbolElement(loc,
                                                symbolColor,
                                                getSymbolType(wind, clat));
                                        symbs.add(symbol);

                                    } else {
                                        Text textSymbol = getTextSymbol(
                                                intensity, loc, symbolColor);
                                        symbs.add(textSymbol);
                                    }
                                } else {
                                    SymbolType stype = SymbolType.TROPICAL_STORM_NORTH;
                                    if (clat < 0) {
                                        stype = SymbolType.TROPICAL_STORM_SOUTH;
                                    }

                                    symbol = createSymbolElement(loc,
                                            symbolColor, stype);
                                    symbs.add(symbol);
                                }
                            } else {
                                /*
                                 * Draw a square for special DTG (storm landing
                                 * location)
                                 */
                                Symbol spSymb = createSymbolElement(loc,
                                        symbolColor, SymbolType.FILLED_SQUARE);
                                symbs.add(spSymb);
                            }
                        }

                        // Intensity label string if requested
                        if (attr.isTrackIntensities() && wind > 0
                                && wind <= MAX_WIND_SPEED) {
                            StringBuilder label = new StringBuilder();
                            label.append(wind);
                            String[] text = new String[] { label.toString() };

                            Text textElement = createTextElement(text, loc,
                                    stormTrackColor, 2, 0);

                            elems.add(textElement);
                        }

                        // Create a label string in format of "DD/HHZ"
                        if (attr.isTrackLabels()) {

                            StringBuilder label = new StringBuilder();
                            label.append(dtg.substring(6));
                            label.append("Z");
                            String[] text = new String[] { label.toString() };

                            Text textElement = createTextElement(text, loc,
                                    stormTrackColor, 2, 1);

                            elems.add(textElement);
                        }

                        prevLon = clon;
                        prevLat = clat;
                        prevWind = wind;
                        prevCol = trackLineElemColor;

                        // Best track radii display options.
                        if (!isGenesis) {
                            for (BDeckRecord bRec : recs) {
                                if (isSelectedRadiiRange(dtg, attr)) {

                                    /*
                                     * TODO needs to change
                                     * DisplayElementsFactory to make the color
                                     * work.
                                     */
                                    Tcm tcm = createTcmElement(bRec, attr, loc);

                                    if (tcm != null) {
                                        int speed = (int) bRec.getRadWind();

                                        setTcmWindRadiiColor(tcm, track34ktClr,
                                                track50ktClr, track64ktClr,
                                                speed);
                                        elems.add(tcm);
                                    }

                                    if (attr.isDrawRMW()) {

                                        Arc rmwArc = createRmwRoci(
                                                tcu.getPreferedColor(
                                                        ColorSelectionNames.RMW),
                                                loc, bRec.getMaxWindRad());

                                        elems.add(rmwArc);
                                    }

                                    if (attr.isDrawROCI()) {

                                        Arc rociArc = createRmwRoci(
                                                tcu.getPreferedColor(
                                                        ColorSelectionNames.ROCI),
                                                loc, bRec.getRadClosedP());

                                        elems.add(rociArc);
                                    }

                                }
                            }
                        }
                    }
                }
                elems.addAll(symbs);
            }

            /*
             * Add storm number at the selected first & last DTGs, i.e. "11L" -
             * "11" is the storm number & "L" is storm sub-region.
             */
            if (!isGenesis && !points.isEmpty() && attr.isStormNumber()) {

                Optional<List<BDeckRecord>> brecs = bdeck.values().stream()
                        .findFirst();
                if (brecs.isPresent()) {
                    BDeckRecord fstRec = brecs.get().get(0);
                    StringBuilder label = new StringBuilder();
                    label.append(fstRec.getCycloneNum());
                    label.append(subRegion);
                    String[] text = new String[] { label.toString() };

                    Text fTextElem = createTextElement(text, points.get(0),
                            stormTrackColor, 2, 2);
                    fTextElem.setFontSize(12.0f);

                    elems.add(fTextElem);

                    if (points.size() > 1) {
                        Text lTextElem = createTextElement(text,
                                points.get(points.size() - 1), stormTrackColor,
                                2, 2);
                        lTextElem.setFontSize(12.0f);
                        elems.add(lTextElem);
                    }
                }
            }

        }

        return elems;

    }

    /*
     * Check if a best track DTG is within a selected time range
     *
     * @param dtg DTG to be checked
     *
     * @param attr BestTrackProperties that holds a selected DTGs
     *
     * @return true - in range; false - not in range.
     */
    private boolean isSelectedRange(String dtg, BestTrackProperties attr) {

        boolean selected = true;
        if (attr != null) {
            String[] selectedDTGs = attr.getSelectedDTGs();
            if (selectedDTGs != null) {
                int len = attr.getSelectedDTGs().length;
                if (len > 0 && (dtg.compareTo(selectedDTGs[0]) < 0
                        || dtg.compareTo(selectedDTGs[len - 1]) > 0)) {
                    selected = false;
                }
            }
        }

        return selected;
    }

    /*
     * Check if a best track radii DTG is within a selected time range
     *
     * @param dtg DTG to be checked
     *
     * @param attr BestTrackProperties that holds a selected DTGs
     *
     * @return true - in range; false - not in range.
     */
    private boolean isSelectedRadiiRange(String dtg, BestTrackProperties attr) {

        boolean selected = true;
        if (attr != null) {
            String[] selectedradiiDTGs = attr.getSelectedRadiiDTGs();
            if (selectedradiiDTGs != null) {
                int len = attr.getSelectedRadiiDTGs().length;
                if (len > 0) {
                    if (dtg.compareTo(selectedradiiDTGs[0]) < 0
                            || dtg.compareTo(selectedradiiDTGs[len - 1]) > 0) {
                        selected = false;
                    }
                } else {
                    selected = false;
                }
            }
        }

        return selected;
    }

    /*
     * Set the color for a Tcm element based on the given wind radii.
     *
     * @param tcm Tcm element
     *
     * @param clr34 Color for 34KT wind radii
     *
     * @param clr50 Color for 50KT wind radii
     *
     * @param clr64 Color for 64KT wind radii
     *
     * @param speed wind radii speed
     */
    private void setTcmWindRadiiColor(Tcm tcm, Color clr34, Color clr50,
            Color clr64, int speed) {

        /*
         * TODO Tcm used hard-coded colors for wind radii now. Need to change
         * that to make this code work. Also, this could be applied to other
         * places where wind radii is drawing.
         */
        if (speed == 34) {
            tcm.setColors(new Color[] { clr34 });
        } else if (speed == 50) {
            tcm.setColors(new Color[] { clr50 });
        } else if (speed == 64) {
            tcm.setColors(new Color[] { clr64 });
        }
    }

    /*
     * Create a Line element with some default attributes.
     *
     * @param points List of coordinates
     *
     * @param clr color ofline
     *
     * @param lineWidth width of line
     *
     * @param lineType string for the line type
     *
     * @return A Line element.
     */
    private Line createLineElement(List<Coordinate> points, Color clr,
            float lineWidth, String lineType) {
        if (lineType.equals(LineType.BOLD.getLineType())) {
            lineType = LineType.SOLID.getLineType();
            lineWidth = 5.0f;
        }
        return new Line(null, new Color[] { clr }, lineWidth, 2.0, false, false,
                points, 2, null, "Lines", lineType);

    }

    /*
     * Create a Text element with some default attributes.
     *
     * @param text content of text
     *
     * @param loc Location of text.
     *
     * @param clr color of text
     *
     * @param xoffset Half-character offset in the X direction
     *
     * @param yoofset Half-character offset in the Y direction
     *
     * @return A Text element.
     */
    private Text createTextElement(String[] text, Coordinate loc, Color clr,
            int xoffset, int yoffset) {

        Coordinate[] range = null;
        String fontName = "Courier";
        float fontSize = 10.0f;
        TextJustification justification = TextJustification.CENTER;
        double rotation = 0.0;
        TextRotation rotationRelativity = TextRotation.SCREEN_RELATIVE;
        FontStyle style = FontStyle.REGULAR;
        boolean mask = false;
        DisplayType outline = DisplayType.NORMAL;
        String atcfCategory = "Text";
        String atcfType = "General Text";

        return new Text(range, fontName, fontSize, justification, loc, rotation,
                rotationRelativity, text, style, clr, xoffset, yoffset, mask,
                outline, atcfCategory, atcfType);
    }

    /**
     *
     * Create a Symbol element with some default attributes.
     *
     * @param loc
     *            Location of symbol.
     *
     * @param clr
     *            color of symbol
     *
     * @param symbol
     *            symbol type
     *
     * @return A Symbol element.
     */
    private Symbol createSymbolElement(Coordinate loc, Color clr,
            SymbolType symbol) {
        double scale = 1.5;
        float width = 2.0f;
        if (symbol.equals(SymbolType.EMPTY_CIRCLE)
                || symbol.equals(SymbolType.FILLED_SQUARE)) {
            scale = .6;
            width = 1.0f;
        }

        if (symbol.equals(SymbolType.HURRICANE_NORTH) && loc.y < 0) {
            symbol = SymbolType.HURRICANE_SOUTH;
        }

        return new Symbol(null, new Color[] { clr }, width, scale, false, loc,
                "Symbol", symbol.getSymbolType());
    }

    /*
     * Create a Tcm element with some default attributes for wind radii.
     *
     * @param bRec BDeckRecord .
     *
     * @param attr BestTrackProperties.
     *
     * @param loc Location of Tcm.
     *
     * @return A Tcm element.
     *
     */
    private Tcm createTcmElement(BDeckRecord bRec, BestTrackProperties attr,
            Coordinate loc) {

        Tcm tcm = null;
        int speed = (int) bRec.getRadWind();
        if ((attr.isRadiiFor34Knot() && speed == 34)
                || (attr.isRadiiFor50Knot() && speed == 50)
                || (attr.isRadiiFor64Knot() && speed == 64)) {
            float[] windRadii = AtcfVizUtil.adjustWindQuadrants(
                    bRec.getRadWindQuad(), bRec.getQuad1WindRad(),
                    bRec.getQuad2WindRad(), bRec.getQuad3WindRad(),
                    bRec.getQuad4WindRad());

            TcmWindQuarters windQuarters = new TcmWindQuarters(loc, speed,
                    windRadii[0], windRadii[1], windRadii[2], windRadii[3]);
            tcm = new Tcm();
            tcm.setWaveQuatro(windQuarters);
            tcm.setAdvisoryTime(bRec.getRefTimeAsCalendar());
        }

        return tcm;
    }

    /**
     * Create an arc for the ROCI and RMW elements
     *
     * @param color
     *            - color of the arc
     * @param start
     *            - center of the arc
     * @param rad
     *            - length of the radius of the arc
     * @return Arc
     */
    private Arc createRmwRoci(Color color, Coordinate start, Float rad) {
        Coordinate newP = AtcfVizUtil.computePointMiles(start.y, start.x,
                (rad * AtcfVizUtil.NAUTICAL_MILES_TO_MILES), 0.0);

        /*
         * TODO Arc currently only supports solid lines, these are drawn with
         * dashed lines in legacy
         */
        return new Arc(null, color, (float) 1.5, 1.0, false, false, 0, null,
                "Circle", start, newP, "Arc", 1, 0, 360);
    }

    /*
     * Gets a color based of Hurricane category
     *
     * @param windSpeed
     *
     * @param tcu
     *
     * @param defaultColor
     *
     * @return Line color
     */
    private Color getColorFromSS(int windSpeed, TrackColorUtil tcu,
            Color defaultColor) {
        Color lineColor = defaultColor;
        if (windSpeed < 65) {
            // Default
        } else if (windSpeed < 83) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_1);
        } else if (windSpeed < 96) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_2);
        } else if (windSpeed < 113) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_3);
        } else if (windSpeed < 137) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_4);
        } else if (windSpeed <= MAX_WIND_SPEED) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_5);
        }
        return lineColor;
    }

    /*
     * Gets a color based of of hurricane speed
     *
     * @param speed
     *
     * @param defaultColor
     *
     * @param track34ktClr
     *
     * @param track64ktClr
     *
     * @return Line color
     */
    private Color getColorFromIntensity(int speed, Color defaultColor,
            Color track34ktClr, Color track64ktClr) {
        Color lineColor = defaultColor;
        if (speed < 35) {
            // Default
        } else if (speed < 65) {
            lineColor = track34ktClr;
        } else if (speed <= MAX_WIND_SPEED) {
            lineColor = track64ktClr;
        }
        return lineColor;
    }

    /*
     * Make the line dashed or dotted based off of intensity
     *
     * @param speed
     *
     * @return Line Type
     */
    private String makeDashDot(int speed) {
        String ret;
        if (speed < 35) {
            // Dotted line
            ret = LineType.DOTTED.getLineType();
        } else if (speed < 65) {
            // Dashed line
            ret = LineType.DASHED.getLineType();
        } else {
            // Solid bold line
            ret = LineType.BOLD.getLineType();
        }

        return ret;
    }

    /*
     * Decides the color for each segment of the track line
     *
     * @param wind - the max wind speed
     *
     * @param stormTrackColor - the default line color
     *
     * @param track34ktClr - the 34kt speed line color
     *
     * @param track64ktClr - the 64kt speed line color
     *
     * @param tcu - track color utility
     *
     * @param attr - best track property
     *
     * @return the line color
     */
    private Color getLineColor(int wind, Color stormTrackColor,
            Color track34ktClr, Color track64ktClr, TrackColorUtil tcu,
            BestTrackProperties attr) {

        Color lnColor = stormTrackColor;
        if (attr.isColorsOnCategory()) {
            lnColor = getColorFromSS(wind, tcu, stormTrackColor);
        } else if (!attr.isColorsOnCategory() && attr.isColorsOnIntensity()) {
            lnColor = getColorFromIntensity(wind, stormTrackColor, track34ktClr,
                    track64ktClr);
        }

        return lnColor;
    }

    /*
     * Decides which symbol appears at each point of the track. If the storms
     * Intensity is DB, HU, TD, or TS, then the point gets a symbol based on its
     * wind speed. ( circle for <35, tropical storm for < 65, hurricane for >
     * 65).
     *
     * @param wind - the max wind speed
     *
     * @return
     */
    private SymbolType getSymbolType(int wind, float lat) {
        SymbolType ret;
        if (wind < 35) {
            ret = SymbolType.EMPTY_CIRCLE;
        } else if (wind < 65) {
            ret = (lat >= 0) ? SymbolType.TROPICAL_STORM_NORTH
                    : SymbolType.TROPICAL_STORM_SOUTH;
        } else {
            ret = (lat >= 0) ? SymbolType.HURRICANE_NORTH
                    : SymbolType.HURRICANE_SOUTH;
        }

        return ret;
    }

    /*
     * Creates a text element for a storm symbol. If the Storm's intensity is
     * not DB, HU, TD, or TS, then its symbol is simply a text representation of
     * its intensity.
     *
     *
     * @param intensity - storm intensity
     *
     * @param loc - label location
     *
     * @param clr - label color
     *
     * @return the text element
     */
    private Text getTextSymbol(String intensity, Coordinate loc, Color clr) {
        String text = ("LO".equals(intensity)) ? "L" : intensity;

        return new Text(null, "Liberation Serif", 15.0f,
                TextJustification.CENTER, loc, 0.0,
                TextRotation.SCREEN_RELATIVE, new String[] { text },
                FontStyle.REGULAR, clr, 0, 0, false, DisplayType.NORMAL, "Text",
                "General Text");
    }

    /**
     * Enum for line types
     */
    public enum LineType {
        SOLID("LINE_SOLID"),
        DOTTED("LINE_DASHED_2"),
        DASHED("LINE_DASHED_3"),
        BOLD("LINE_BOLD");

        private String lnType;

        private LineType(String lineType) {
            this.lnType = lineType;
        }

        public String getLineType() {
            return lnType;
        }
    }

    /**
     * Enum for symbol types
     */
    public enum SymbolType {
        FILLED_SQUARE("FILLED_SQUARE"),
        EMPTY_CIRCLE("CIRCLE"),
        HURRICANE_NORTH("HURRICANE_NH"),
        HURRICANE_SOUTH("HURRICANE_SH"),
        TROPICAL_STORM_NORTH("TROPICAL_STORM_NH"),
        TROPICAL_STORM_SOUTH("TROPICAL_STORM_SH");

        private String symbType;

        private SymbolType(String symbolType) {
            this.symbType = symbolType;
        }

        public String getSymbolType() {
            return symbType;
        }
    }

}
