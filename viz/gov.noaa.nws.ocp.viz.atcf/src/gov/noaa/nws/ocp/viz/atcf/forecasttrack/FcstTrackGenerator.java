/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecasttrack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences.PreferenceOptions;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionNames;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
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
 * Class to create/display graphic elements for a storm's forecast track (.fst).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Oct 29, 2019 69592       jwu         Initial creation.
 * Jan 15, 2020 71722       jwu         Make sure DTG is sorted.
 * Apr 19, 2021 88712       jwu         Change to smoothed line.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class FcstTrackGenerator {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(FcstTrackGenerator.class);

    // ATCF resource for the storm.
    private AtcfResource atcfResource;

    // Drawing options for creating/displaying forecast track.
    private FcstTrackProperties displayProperties;

    // The storm to work on.
    private Storm storm;

    // Flag to indicate if baseline forecast track needs to be drawn.
    private boolean showBaseline;

    /**
     * Constructor
     */
    public FcstTrackGenerator() {
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        displayProperties = new FcstTrackProperties();
        storm = ((AtcfProduct) atcfResource.getActiveProduct()).getStorm();
        showBaseline = false;
    }

    /**
     * Constructor
     *
     * @param rsc
     *            AtcfResource
     * @param displayProperties
     *            FcstTrackProperties
     * @param storm
     *            Storm
     */
    public FcstTrackGenerator(AtcfResource rsc,
            FcstTrackProperties displayProperties, Storm storm) {
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
     *            FcstTrackProperties
     * @param storm
     *            Storm
     * @param baseline
     *            boolean to show baseline
     *
     */
    public FcstTrackGenerator(AtcfResource rsc,
            FcstTrackProperties displayProperties, Storm storm,
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
    public FcstTrackProperties getDisplayProperties() {
        return displayProperties;
    }

    /**
     * @param displayProperties
     *            the displayProperties to set
     */
    public void setDisplayProperties(FcstTrackProperties displayProperties) {
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
     * Create and display the forecast track. This method always turn on the
     * display of forecast track layer.
     */
    public void create() {
        create(true);
    }

    /**
     * Create and display the forecast track. Track is recreated if "display"
     * flag is true.
     *
     * @param display
     *            boolean, flag to indicate if forecast track needs to display
     *            or hide.
     */
    public void create(boolean display) {

        // Sanity check
        if (atcfResource == null || displayProperties == null
                || storm == null) {
            logger.warn(
                    "FcstTrackGenerator - missing information, no forecast track is generated.");
            return;
        }

        AtcfProduct prd = atcfResource.getResourceData().getAtcfProduct(storm);
        prd.getForecastTrackLayer().setOnOff(display);

        // Recreate if forecast track layer display is on.
        if (display) {

            prd.setFcstTrackProperties(displayProperties);

            // Retrieve all forecast track records.
            Map<String, List<ForecastTrackRecord>> fcstTrack = prd
                    .getFcstTrackDataMap();

            Map<String, List<BDeckRecord>> bDeck = prd.getBDeckDataMap();

            // Find where the best track ends so the forecast track can start
            // from there.
            BDeckRecord lastBDeckRec = null;
            if (bDeck.size() > 0 && fcstTrack.size() > 0) {
                String latestDtg = Collections.max(bDeck.keySet());
                List<BDeckRecord> latestBDeckRecs = bDeck.get(latestDtg);
                if (latestBDeckRecs != null) {
                    lastBDeckRec = latestBDeckRecs.get(0);
                }
            }

            // Create all elements.
            List<AbstractDrawableComponent> elems = createElements(
                    fcstTrack, displayProperties, lastBDeckRec);

            // Remove elements in forecast track layer.
            atcfResource.resetLayer(prd.getForecastTrackLayer());

            // Add elements onto forecast track layer with the new set of
            // elements.
            if (!elems.isEmpty()) {
                atcfResource.addElements(elems, prd,
                        prd.getForecastTrackLayer());
            }

            // Create elements from baseline track if needed.
            if (isShowBaseline()) {
                // TODO Not sure if we need this or not - keep it here for now.
                /*- Map<String, List<ForecastTrackRecord>> bdks = AtcfDataUtil
                        .getForecastTrackBaseLineRecords(storm);
                List<AbstractDrawableComponent> baseLineElems = createElements(
                        bdks, displayProperties);
                atcfResource.addElements(baseLineElems, prd,
                        prd.getForecastTrackLayer()); */
            }
        }

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor != null) {
            editor.refresh();
        }

    }

    /**
     * Redraws active storm's forecast track based on preference options.
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
                    "Atcf ForecastTrackGenerator: No active storm is selected yet.");
            return;
        }

        AtcfProduct prd = rsc.getResourceData().getAtcfProduct(activeStorm);
        FcstTrackProperties prop = prd.getFcstTrackProperties();

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
            FcstTrackGenerator fstTrkGen = new FcstTrackGenerator(rsc, prop,
                    activeStorm);
            fstTrkGen.create();
        }
    }

    /*
     * Create graphic elements for selected forecast records.
     *
     * @param fcsttrack Map<String,List<ForecastTrackRecord>>
     *
     * @param attr ForecastTrackProperties
     *
     * @param lastBDeckRec Where the current forecast track stops.
     *
     * @return List of graphic elements
     */
    private List<AbstractDrawableComponent> createElements(
            Map<String, List<ForecastTrackRecord>> fcstTrack,
            FcstTrackProperties attr, BDeckRecord lastBDeckRec) {

        List<AbstractDrawableComponent> elems = new ArrayList<>();

        // TODO More logic may need to be added here for drawing options stored
        // in ForecastTrackProperties, which may be modified via "Preference"
        // dialog, Graphic menu, or sidebar actions.
        if (fcstTrack != null && !fcstTrack.isEmpty()) {

            List<AbstractDrawableComponent> symbs = new ArrayList<>();
            List<Coordinate> points = new ArrayList<>();

            // Starts from where the current best track ends.
            if (lastBDeckRec != null) {
                points.add(new Coordinate(lastBDeckRec.getClon(),
                        lastBDeckRec.getClat()));
            }

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
                    .getPreferedColor(ColorSelectionNames.FORECAST);

            Color stormTrackColor = tcu
                    .getPreferedColor(ColorSelectionNames.FORECAST);

            Color trackLabelClr = tcu
                    .getPreferedColor(ColorSelectionNames.FORECAST_LABEL);

            // Get colors for wind radii.
            Color track34ktClr = tcu
                    .getPreferedColor(ColorSelectionNames.WIND_RAD_34);
            Color track50ktClr = tcu
                    .getPreferedColor(ColorSelectionNames.WIND_RAD_50);
            Color track64ktClr = tcu
                    .getPreferedColor(ColorSelectionNames.WIND_RAD_64);

            int prevWind = 0;
            String lineType = "";
            String intensity = "";

            /*
             * Each DTG may have up to 3 records (34kt, 50kt, 64kt), but the
             * location is the same.
             */
            String subRegion = "";

            Set<String> dtgs = new TreeSet<>();
            dtgs.addAll(fcstTrack.keySet());

            for (String dtg : dtgs) {
                // Check if this DTG is within the selected range.
                if (isSelectedRange(dtg, attr)) {
                    List<ForecastTrackRecord> recs = fcstTrack.get(dtg);
                    int wind = (int) recs.get(0).getWindMax();
                    if (recs != null && !recs.isEmpty()) {

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

                        // Forecast track symbols
                        if (attr.isStormSymbols()) {
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
                            if (attr.isSpecialTypePosition()) {
                                if (intensity
                                        .equals(StormDevelopment.DB.toString())
                                        || intensity.equals(
                                                StormDevelopment.HU.toString())
                                        || intensity.equals(
                                                StormDevelopment.TS.toString())
                                        || intensity.equals(StormDevelopment.TD
                                                .toString())) {
                                    symbol = createSymbolElement(loc,
                                            symbolColor,
                                            getSymbolType(wind, clat));
                                    symbs.add(symbol);

                                } else {
                                    Text textSymbol = getTextSymbol(intensity,
                                            loc, symbolColor);
                                    symbs.add(textSymbol);
                                }
                            } else {
                                symbol = createSymbolElement(loc, symbolColor,
                                        SymbolType.HURRICANE_NORTH);
                                symbs.add(symbol);
                            }

                        }

                        // Intensity label string if requested
                        int windMax = (int) recs.get(0).getWindMax();
                        if (attr.isTrackIntensities() && windMax > 0) {
                            StringBuilder label = new StringBuilder();
                            label.append(windMax);
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

                            Coordinate newLoc = new Coordinate(loc.x - 4,
                                    loc.y);

                            Text textElement = createTextElement(text, newLoc,
                                    trackLabelClr, 2, 1);

                            elems.add(textElement);

                            // Also add a line to link to the label
                            List<Coordinate> linePts = new ArrayList<>();
                            linePts.add(loc);
                            linePts.add(new Coordinate(loc.x - 3, loc.y + 0.2));
                            Line line = createLineElement(linePts,
                                    trackLabelClr, lineWidth,
                                    LineType.DOTTED.getLineType());
                            elems.add(line);

                        }

                        // Create 12-ft sea radii if requested
                        Tcm waveRadElem = create12FtSeaRadiiTcmElem(recs.get(0),
                                attr);
                        if (waveRadElem != null) {
                            elems.add(waveRadElem);
                        }

                        prevWind = wind;

                        // Forecast track radii display options.
                        for (ForecastTrackRecord fstRec : recs) {
                            if (isSelectedRadiiRange(dtg, attr)) {

                                // TODO needs to change
                                // DisplayElementsFactory
                                // to make the color work.

                                // Draw 34/50/64 knot wind radii.
                                Tcm tcm = createTcmElement(fstRec, attr);

                                if (tcm != null) {
                                    int speed = (int) fstRec.getRadWind();

                                    setTcmWindRadiiColor(tcm, track34ktClr,
                                            track50ktClr, track64ktClr, speed);
                                    elems.add(tcm);
                                }

                                if (attr.isDrawRMW()) {
                                    Color rmwClr = tcu.getPreferedColor(
                                            ColorSelectionNames.RMW);
                                    Arc rmwArc = createRmwRoci(rmwClr, loc,
                                            fstRec.getMaxWindRad());
                                    elems.add(rmwArc);
                                }

                                if (attr.isDrawROCI()) {
                                    Color rociClr = tcu.getPreferedColor(
                                            ColorSelectionNames.ROCI);
                                    Arc rociArc = createRmwRoci(rociClr, loc,
                                            fstRec.getRadClosedP());

                                    elems.add(rociArc);
                                }

                            }
                        }

                    }
                }
                elems.addAll(symbs);
            }

            /*
             * Forecast track
             */
            if (!points.isEmpty()) {
                Line line = createLineElement(points, trackLineElemColor,
                        lineWidth, lineType);
                line.setSmoothFactor(1);
                elems.add(line);
            }

        }

        return elems;

    }

    /*
     * Check if a forecast track DTG is within a selected time range
     *
     * @param dtg DTG to be checked
     *
     * @param attr ForecastTrackProperties that holds a selected DTGs
     *
     * @return true - in range; false - not in range.
     */
    private boolean isSelectedRange(String dtg, FcstTrackProperties attr) {

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
     * Check if a forecast track radii DTG is within a selected time range
     *
     * @param dtg DTG to be checked
     *
     * @param attr ForecastTrackProperties that holds a selected DTGs
     *
     * @return true - in range; false - not in range.
     */
    private boolean isSelectedRadiiRange(String dtg, FcstTrackProperties attr) {

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

        // TODO Tcm used hard-coded colors for wind radii now. Need to change
        // that to make this code work. Also, this could be applied to other
        // places where wind radii is drawing.
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
     * @param clr color of line
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
        if (symbol.equals(SymbolType.EMPTYCIRCLE)) {
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
     * Create a Tcm element with some default attributes for wave radii.
     *
     * @param fstRec ForecastTrackRecord.
     *
     * @param attr FsctTrackProperties.
     *
     * @return A Tcm element.
     */
    private Tcm create12FtSeaRadiiTcmElem(ForecastTrackRecord fstRec,
            FcstTrackProperties attr) {

        Tcm tcm = null;
        int radWave = (int) fstRec.getRadWave();
        if ((radWave == 12 && attr.isRadiiFor12FtSea())) {

            float[] waveRadii = AtcfVizUtil.adjustWindQuadrants(
                    fstRec.getRadWaveQuad(), fstRec.getQuad1WaveRad(),
                    fstRec.getQuad2WaveRad(), fstRec.getQuad3WaveRad(),
                    fstRec.getQuad4WaveRad());

            Coordinate loc = new Coordinate(fstRec.getClon(), fstRec.getClat());

            // In DisplayElementFactory, wind speed for 12 ft sea is set as 0.
            TcmWindQuarters waveQuarters = new TcmWindQuarters(loc, 0,
                    waveRadii[0], waveRadii[1], waveRadii[2], waveRadii[3]);

            tcm = new Tcm();
            tcm.setWaveQuatro(waveQuarters);
            tcm.setAdvisoryTime(fstRec.getRefTimeAsCalendar());
        }

        return tcm;
    }

    /*
     * Create a Tcm element with some default attributes for wind radii.
     *
     * @param fstRec ForecastTrackRecord
     *
     * @param attr FsctTrackProperties.
     *
     * @return A Tcm element.
     *
     */
    private Tcm createTcmElement(ForecastTrackRecord fstRec,
            FcstTrackProperties attr) {

        Tcm tcm = null;
        int speed = (int) fstRec.getRadWind();
        if ((attr.isRadiiFor34Knot() && speed == 34)
                || (attr.isRadiiFor50Knot() && speed == 50)
                || (attr.isRadiiFor64Knot() && speed == 64)) {

            float[] windRadii = AtcfVizUtil.adjustWindQuadrants(
                    fstRec.getRadWindQuad(), fstRec.getQuad1WindRad(),
                    fstRec.getQuad2WindRad(), fstRec.getQuad3WindRad(),
                    fstRec.getQuad4WindRad());

            Coordinate loc = new Coordinate(fstRec.getClon(), fstRec.getClat());

            TcmWindQuarters windQuarters = new TcmWindQuarters(loc, speed,
                    windRadii[0], windRadii[1], windRadii[2], windRadii[3]);

            tcm = new Tcm();
            tcm.setWaveQuatro(windQuarters);
            tcm.setAdvisoryTime(fstRec.getRefTimeAsCalendar());
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

        // TODO Arc currently only supports solid lines, these are drawn with
        // dashed lines in legacy
        return new Arc(null, color, (float) 1.5, 1.0, false, false, 0, null,
                "Circle", start, newP, "Arc", 1, 0, 360);
    }

    /**
     * Gets a color based of Hurricane catagory
     *
     * @param windSpeed
     * @param tcu
     * @param defaultColor
     *
     * @return Line color
     */
    private Color getColorFromSS(int windSpeed, TrackColorUtil tcu,
            Color defaultColor) {
        Color lineColor = defaultColor;
        if (windSpeed < 65) {
            // Default.
        } else if (windSpeed < 83) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_1);
        } else if (windSpeed < 96) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_2);
        } else if (windSpeed < 113) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_3);
        } else if (windSpeed < 137) {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_4);
        } else {
            lineColor = tcu.getPreferedColor(ColorSelectionNames.CAT_5);
        }
        return lineColor;
    }

    /**
     * Gets a color based of of hurricane speed
     *
     * @param speed
     * @param defaultColor
     * @param track34ktClr
     * @param track64ktClr
     * @return Line color
     */
    private Color getColorFromIntensity(int speed, Color defaultColor,
            Color track34ktClr, Color track64ktClr) {
        Color lineColor = defaultColor;
        if (speed < 35) {
            // Default.
        } else if (speed < 65) {
            lineColor = track34ktClr;
        } else {
            lineColor = track64ktClr;
        }
        return lineColor;
    }

    /**
     * Make the line dashed or dotted based off of intensity
     *
     * @param speed
     * @return Line Type
     */
    private String makeDashDot(int speed) {
        String ret = "";
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

    /**
     * Decides the color for each segment of the track line
     *
     * @param wind
     *            - the max wind speed
     * @param stormTrackColor
     *            - the default line color
     * @param track34ktClr
     *            - the 34kt speed line color
     * @param track64ktClr
     *            - the 64kt speed line color
     * @param tcu
     *            - track color utility
     * @param attr
     *            - Forecast track property
     *
     * @return the line color
     */
    private Color getLineColor(int wind, Color stormTrackColor,
            Color track34ktClr, Color track64ktClr, TrackColorUtil tcu,
            FcstTrackProperties attr) {
        if (attr.isColorsOnCategory()) {
            return getColorFromSS(wind, tcu, stormTrackColor);
        } else if (!attr.isColorsOnCategory() && attr.isColorsOnIntensity()) {
            return getColorFromIntensity(wind, stormTrackColor, track34ktClr,
                    track64ktClr);
        }
        return stormTrackColor;
    }

    /**
     * Decides which symbol appears at each point of the track. If the storms
     * Intensity is DB, HU, TD, or TS, then the point gets a symbol based on its
     * wind speed. ( circle for <35, tropical storm for < 65, hurricane for >
     * 65).
     *
     * @param wind
     *            - the max wind speed
     * @return
     */
    private SymbolType getSymbolType(int wind, float lat) {
        SymbolType ret = SymbolType.HURRICANE_NORTH;
        if (lat < 0) {
            ret = SymbolType.HURRICANE_SOUTH;
        }

        if (wind < 35) {
            ret = SymbolType.EMPTYCIRCLE;
        } else if (wind < 65) {
            if (lat >= 0) {
                ret = SymbolType.TROPICAL_STROM_NORTH;
            } else {
                ret = SymbolType.TROPICAL_STORM_SOUTH;
            }
        }

        return ret;
    }

    /**
     * Creates a text element for a storm symbol. If the Storm's intensity is
     * not DB, HU, TD, or TS, then its symbol is simply a text representation of
     * its intensity.
     *
     *
     * @param intensity
     *            - storm intensity
     * @param loc
     *            - label location
     * @param clr
     *            - label color
     * @return the text element
     */
    private Text getTextSymbol(String intensity, Coordinate loc, Color clr) {
        String text = "";
        if ("LO".equals(intensity)) {
            text = "L";
        } else {
            text = intensity;
        }

        Coordinate[] range = null;

        return new Text(range, "Liberation Serif", 15.0f,
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
        EMPTYCIRCLE("CIRCLE"),
        HURRICANE_NORTH("HURRICANE_NH"),
        HURRICANE_SOUTH("HURRICANE_SH"),
        TROPICAL_STROM_NORTH("TROPICAL_STORM_NH"),
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
