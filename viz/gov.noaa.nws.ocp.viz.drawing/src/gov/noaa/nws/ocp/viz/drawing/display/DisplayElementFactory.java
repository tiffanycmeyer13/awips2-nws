/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.linearref.LengthLocationMap;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

import com.raytheon.uf.common.geospatial.util.WorldWrapCorrector;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.TextStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.data.IRenderedImageCallback;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IFont;
import com.raytheon.uf.viz.core.drawables.IFont.Style;
import com.raytheon.uf.viz.core.drawables.IImage;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.viz.ui.color.BackgroundColor;
import com.raytheon.viz.ui.color.IBackgroundColorChangedListener.BGColorMode;

import gov.noaa.nws.ocp.common.drawing.ArrowHead;
import gov.noaa.nws.ocp.common.drawing.ArrowHead.ArrowHeadType;
import gov.noaa.nws.ocp.common.drawing.PatternSegment;
import gov.noaa.nws.ocp.common.drawing.breakpoint.BPGeography;
import gov.noaa.nws.ocp.common.drawing.breakpoint.WaterBreakpoint;
import gov.noaa.nws.ocp.common.drawing.linepattern.LinePattern;
import gov.noaa.nws.ocp.common.drawing.linepattern.LinePatternException;
import gov.noaa.nws.ocp.common.drawing.linepattern.LinePatternManager;
import gov.noaa.nws.ocp.common.drawing.symbolpattern.ISymbolPart;
import gov.noaa.nws.ocp.common.drawing.symbolpattern.SymbolPattern;
import gov.noaa.nws.ocp.common.drawing.symbolpattern.SymbolPatternException;
import gov.noaa.nws.ocp.common.drawing.symbolpattern.SymbolPatternManager;
import gov.noaa.nws.ocp.viz.drawing.display.CornerPatternApplicator.CornerPattern;
import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;
import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;
import gov.noaa.nws.ocp.viz.drawing.display.IText.FontStyle;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextJustification;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextRotation;
import gov.noaa.nws.ocp.viz.drawing.elements.Arc;
import gov.noaa.nws.ocp.viz.drawing.elements.ComboSymbol;
import gov.noaa.nws.ocp.viz.drawing.elements.ElementRangeRecord;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.SymbolLocationSet;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;
import gov.noaa.nws.ocp.viz.drawing.elements.tca.ITca;
import gov.noaa.nws.ocp.viz.drawing.elements.tca.TropicalCycloneAdvisory;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.ITcm;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.ITcmFcst;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.ITcmWindQuarter;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.TcmFcst;
import gov.noaa.nws.ocp.viz.drawing.utils.DrawingUtil;

/**
 * This factory class is used to create IDisplayable elements from objects. A
 * viz Resource can use this factory to create the elements it needs to display
 * without knowing the details of how.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public class DisplayElementFactory {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(DisplayElementFactory.class);

    private static final String FRONT = "Front";

    private static final String LINES = "Lines";

    private static final String SYMBOL = "Symbol";

    private static final String ARC = "Arc";

    private static final String CIRCLE = "Circle";

    private static final String GENERAL_TEXT = "General Text";

    private static final String LINE_DASHED_4 = "LINE_DASHED_4";

    /*
     * LinePattern segment scale types (used to rescale LinePattern so that line
     * ends with a full pattern)
     */
    private enum ScaleType {
        SCALE_ALL_SEGMENTS, SCALE_BLANK_LINE_ONLY
    }

    /**
     * Graphics Target used to create the Wireframe and Shaded shapes
     */
    protected IGraphicsTarget target;

    /**
     * Map Descriptor used for Lat/Lon to pixel coordinate transformations
     */
    protected IDescriptor/* IMapDescriptor */ iDescriptor;

    private GeometryFactory gf;

    /**
     * Array of WireframeShapes used to hold all line segments to be drawn
     */
    private IWireframeShape[] wfs;

    /**
     * A ShadedShape to hold all the filled areas to be drawn
     */
    private IShadedShape ss;

    private IWireframeShape sym;

    private ILine elem;

    // default scale factor for GL device
    protected double deviceScale = 25.0;

    private double symbolScale = 0.65;

    private double screenToExtent = 1.0;

    private double screenToWorldRatio = 1.0;

    private ArrowHead arrow;

    /*
     * The factor to adjust the front pip size (scaleFactor) to visually match
     * those in legacy NMAP2.
     */
    private static double frontPatternFactor = 0.80;

    /**
     * Color mode, color, and fill mode used to draw all elements in a layer
     */
    private boolean layerMonoColor = false;

    private Color layerColor = null;

    private boolean layerFilled = false;

    protected BackgroundColor backgroundColor = BackgroundColor
            .getActivePerspectiveInstance();

    class SymbolImageCallback implements IRenderedImageCallback {
        private String patternName;

        private double scale;

        private float lineWidth;

        private boolean mask;

        private Color color;

        public SymbolImageCallback(String patternName, double scale,
                float lineWidth, boolean mask, Color color) {
            super();
            this.patternName = patternName;
            this.scale = scale;
            this.lineWidth = lineWidth;
            this.mask = mask;
            this.color = color;
        }

        @Override
        public RenderedImage getImage() throws VizException {
            return SymbolImageUtil.createBufferedImage(patternName, scale,
                    lineWidth, mask, color);
        }

    }

    /**
     * Constructor used to set initial Graphics Target and MapDescriptor
     *
     * @param target
     *            The Graphics Target
     * @param mapDescriptor
     *            The Map Descriptor
     */
    public DisplayElementFactory(IGraphicsTarget target,
            IMapDescriptor mapDescriptor) {

        this.target = target;
        this.iDescriptor = mapDescriptor;
        gf = new GeometryFactory();

    }

    public DisplayElementFactory(IGraphicsTarget target,
            IDescriptor iDescriptor) {

        this.target = target;
        this.iDescriptor = iDescriptor;
        gf = new GeometryFactory();

    }

    /**
     * Creates a list of IDisplayable Objects from an IMultiPoint object and
     * applies world wrap if applicable
     *
     * @param drawableElement
     *            A Drawable Element of a multipoint object
     * @param paintProps
     *            The paint properties associated with the target
     * @param worldWrap
     *            The flag to apply world wrap for lines
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ILine drawableElement,
            PaintProperties paintProps, boolean worldWrap) {

        if (worldWrap) {
            return createWorldWrappedDisplayElements(drawableElement,
                    paintProps);
        }

        return createDisplayElements(drawableElement, paintProps);

    }

    /**
     * Applies the World Wrap function to LatLon coordinates
     *
     * @param drawableElement
     *            A Drawable Element of a multipoint object
     * @param paintProps
     *            The paint properties associated with the target
     */
    private List<IDisplayable> createWorldWrappedDisplayElements(
            ILine drawableElement, PaintProperties paintProps) {

        List<IDisplayable> list = new ArrayList<>();
        WorldWrapCorrector corrector = new WorldWrapCorrector(
                iDescriptor.getGridGeometry());
        elem = drawableElement;

        // put line points in a coordinate array
        Coordinate[] coord;

        if (drawableElement.isClosedLine()) {
            coord = new Coordinate[drawableElement.getLinePoints().length + 1];
            for (int ii = 0; ii < drawableElement
                    .getLinePoints().length; ii++) {
                coord[ii] = new Coordinate(
                        drawableElement.getLinePoints()[ii].x,
                        drawableElement.getLinePoints()[ii].y);
            }
            coord[drawableElement.getLinePoints().length] = new Coordinate(
                    drawableElement.getLinePoints()[0].x,
                    drawableElement.getLinePoints()[0].y);
        } else {
            coord = new Coordinate[drawableElement.getLinePoints().length];

            for (int ii = 0; ii < drawableElement
                    .getLinePoints().length; ii++) {
                coord[ii] = new Coordinate(
                        drawableElement.getLinePoints()[ii].x,
                        drawableElement.getLinePoints()[ii].y);
            }

        }

        // Apply world wrap
        Geometry geo = null;
        try {
            geo = corrector.correct(DrawingUtil.pointsToLineString(coord));
        } catch (Exception e) {
            handler.error("World wrap error: " + e.getMessage(), e);
            return list;
        }

        if (geo == null) {
            handler.handle(Priority.PROBLEM,
                    "Error: World wrap Geometry is null");
            return list;
        }

        for (int ii = 0; ii < geo.getNumGeometries(); ii++) {
            Geometry geo1 = geo.getGeometryN(ii);
            double[][] pixelsCordinates = DrawingUtil.latlonToPixel(
                    geo1.getCoordinates(), (IMapDescriptor) iDescriptor);
            double[][] smoothedCoordinates;

            // Apply parametric smoothing
            smoothedCoordinates = applyParametricSmoothing(drawableElement,
                    pixelsCordinates);

            list.addAll(createDisplayElementsForLines(drawableElement,
                    smoothedCoordinates, paintProps));

        }

        return list;

    }

    /**
     * Applies parametric smoothing on pixel coordinates, if required
     *
     * @param drawableElement
     *            A Drawable Element of a multipoint object
     *
     * @param pixelsCordinates
     *            The screen coordinates associated with the target
     *
     * @return The smoothed screen coordinates associated with the target
     *
     */
    private double[][] applyParametricSmoothing(ILine drawableElement,
            double[][] pixelsCordinates) {

        float density;
        if (drawableElement.getSmoothFactor() > 0) {
            float devScale = 50.0f;
            if (drawableElement.getSmoothFactor() == 1) {
                density = devScale / 1.0f;
            } else {
                density = devScale / 5.0f;
            }
            return CurveFitter.fitParametricCurve(pixelsCordinates, density);
        } else {
            return pixelsCordinates;
        }
    }

    /**
     * Creates display elements for multiple points elements. This method gets
     * attributes, such as colors from the input elements, then apply these
     * attributes on the smoothed(if needed) points to create a list of
     * displayable.
     *
     * @param drawableElement
     *            A Drawable Element of a multipoint object
     * @param smoothpts
     *            points of the multipoint object
     * @param paintProps
     *            The paint properties associated with the target
     * @return
     */
    private List<IDisplayable> createDisplayElementsForLines(
            ILine drawableElement, double[][] smoothpts,
            PaintProperties paintProps) {

        setScales(paintProps);

        /*
         * Adjust line width/pattern size for fronts.
         *
         * If line width is 1,2 or 3, it is weak front, draw with line width 1.
         * If line width is 4,5 or 6, it is moderate front, draw with line width
         * 4. If line width is 7,8 or 9, it is strong front, draw with line
         * width 7.
         *
         * Also, the pip size (sizeScale) needs to be adjusted to match NMAP2.
         */
        float drawLineWidth = drawableElement.getLineWidth();
        double drawSizeScale = drawableElement.getSizeScale();
        if (drawableElement instanceof Line && ((Line) drawableElement)
                .getElemCategory().equalsIgnoreCase(FRONT)) {
            if (drawLineWidth <= 3.0) {
                drawLineWidth = 1.0f;
            } else if (drawLineWidth > 6.0) {
                drawLineWidth = 7.0f;
            } else {
                drawLineWidth = 4.0f;
            }

            drawSizeScale *= frontPatternFactor;
        }

        /*
         * Get color for creating displayables.
         */
        Color[] displayColor = getDisplayColors(elem.getColors());

        /*
         * Find Line Pattern associated with this element, if "Solid Line" was
         * not requested.
         */
        LinePattern pattern = null;
        LinePatternManager lpl = LinePatternManager.getInstance();
        try {
            pattern = lpl.getLinePattern(drawableElement.getPatternName());
        } catch (LinePatternException lpe) {
            /*
             * could not find desired line pattern. Used solid line as default.
             */
            handler.warn(lpe.getMessage() + ":  Using Solid Line by default.", lpe);
            pattern = null;
        }

        /*
         * If pattern has some segments whose length is set at runtime based on
         * the desired line width, update the pattern now
         */
        if ((pattern != null) && pattern.needsLengthUpdate()) {
            pattern = pattern.updateLength(screenToExtent * drawLineWidth
                    / (drawSizeScale * deviceScale));
        }

        /*
         * Flip the side of the pattern along the spine
         */
        if ((pattern != null) && (elem instanceof Line)
                && ((Line) elem).isFlipSide()) {
            pattern = pattern.flipSide();
        }

        /*
         * If a LinePattern is found for the object, apply it. Otherwise, just
         * use solid line.
         */
        ScaleType scaleType = null;
        if ((pattern != null) && (pattern.getNumSegments() > 0)) {
            scaleType = ScaleType.SCALE_ALL_SEGMENTS;
            if (elem instanceof Line) {
                Line line = (Line) elem;
                /*
                 * Change scale type for fronts so that only BLANK and LINE
                 * segments are scaled. This is done so that size of front pips
                 * don't vary with length of front.
                 */
                if (line.getElemCategory().equalsIgnoreCase(FRONT)) {
                    scaleType = ScaleType.SCALE_BLANK_LINE_ONLY;
                }
            }
        }

        List<IDisplayable> list = new ArrayList<>();

        list.addAll(createDisplayElementsFromPts(smoothpts, displayColor,
                pattern, scaleType,
                getDisplayFillMode(drawableElement.isFilled()), drawLineWidth));

        return list;
    }

    /**
     * Creates displayable from the input attributes and points of the line
     *
     * @param pts
     * @param dspClr
     * @param pattern
     * @param scaleType
     * @param isFilled
     * @param lineWidth
     * @param paintProps
     * @return
     */
    private List<IDisplayable> createDisplayElementsFromPts(double[][] pts,
            Color[] dspClr, LinePattern pattern, ScaleType scaleType,
            boolean isFilled, float lineWidth) {

        List<IDisplayable> list = new ArrayList<>();
        wfs = new IWireframeShape[dspClr.length];
        for (int i = 0; i < dspClr.length; i++) {
            wfs[i] = target.createWireframeShape(false, iDescriptor);
        }
        ss = target.createShadedShape(false, iDescriptor, true);

        /*
         * Create arrow head, if needed
         */
        if ((pattern != null) && pattern.hasArrowHead()) {
            /*
             * Get scale size from drawable element.
             */
            double scale = elem.getSizeScale();

            /*
             * R6520 - adjust pip size for fronts to match NMAP2's size
             * visually.
             */
            if (elem instanceof Line && ((Line) elem).getElemCategory()
                    .equalsIgnoreCase(FRONT)) {
                scale *= frontPatternFactor;
            }

            if (scale <= 0.0) {
                scale = 1.0;
            }
            double sfactor = deviceScale * scale;

            // Angle of arrow point - defining narrowness
            double pointAngle = 60.0;
            double extent = pattern.getMaxExtent();

            /*
             * Consider distance away from center line, the height should be no
             * less than extent * 1.5. Currently we only have extent 1 and 2
             * available. 3.5 is what we want the size to be.
             */
            double height = sfactor * 3.5;
            if (extent * 1.5 > 3.5) {
                height = sfactor * extent * 1.5;
            }

            int n = pts.length - 1;
            // calculate direction of arrow head
            double slope = Math.toDegrees(Math.atan2(
                    (pts[n][1] - pts[n - 1][1]), (pts[n][0] - pts[n - 1][0])));

            arrow = new ArrowHead(new Coordinate(pts[n][0], pts[n][1]),
                    pointAngle, slope, height, pattern.getArrowHeadType());
            Coordinate[] ahead = arrow.getArrowHeadShape();

            if (pattern.getArrowHeadType() == ArrowHeadType.OPEN) {
                // Add to wireframe
                wfs[0].addLineSegment(toDouble(ahead));
            }
            if (pattern.getArrowHeadType() == ArrowHeadType.FILLED) {
                // Add to shadedshape
                ss.addPolygonPixelSpace(toLineString(ahead),
                        new RGB(dspClr[0].getRed(), dspClr[0].getGreen(),
                                dspClr[0].getBlue()));
            }
        }

        if ((pattern != null) && (pattern.getNumSegments() > 0)) {
            handleLinePattern(pattern, pts, scaleType);
        } else {
            wfs[0].addLineSegment(pts);
        }

        if (isFilled) {
            list.add(createFill(pts));
        }

        /*
         * Compile each IWireframeShape, create its LineDisplayElement, and add
         * to IDisplayable return list
         */
        for (int k = 0; k < wfs.length; k++) {
            wfs[k].compile();
            LineDisplayElement lde = new LineDisplayElement(wfs[k], dspClr[k],
                    lineWidth);
            list.add(lde);
        }

        /*
         * Compile each IShadedShape, create FillDisplayElement, and add to
         * IDisplayable return list
         */
        ss.compile();
        FillDisplayElement fde = new FillDisplayElement(ss,
                elem.getColors()[0].getAlpha());
        list.add(fde);

        return list;
    }

    /**
     * Creates a list of IDisplayable Objects from an IMultiPoint object
     *
     * @param de
     *            A Drawable Element of a multipoint object
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ILine de,
            PaintProperties paintProps) {

        double[][] smoothpts;
        double[][] pixels;

        setScales(paintProps);

        /*
         * save drawable element
         */
        elem = de;

        /*
         * Create the List to be returned, some wireframe shapes and a shaded
         * shape to be used for the IDisplayables
         */
        List<IDisplayable> list = new ArrayList<>();

        /*
         * Get lat/lon coordinates from drawable element
         */
        Coordinate[] pts = de.getLinePoints();

        /*
         * convert lat/lon coordinates to pixel coordinates
         */
        pixels = DrawingUtil.latlonToPixel(pts, (IMapDescriptor) iDescriptor);

        /*
         * If line is closed, make sure last point is same as first point
         */
        if (de.isClosedLine()) {
            pixels = ensureClosed(pixels);
        }
        // Apply parametric smoothing
        smoothpts = applyParametricSmoothing(de, pixels);

        list.addAll(createDisplayElementsForLines(de, smoothpts, paintProps));

        return list;
    }

    /**
     * Method to add ALL symbols of the same color into a single wire-frame.
     * Designed to increase efficiency in rendering symbols.
     *
     * @param paintProps
     * @param listOfSymbolLocSets
     *            - A list of symbols - each of which will be rendered at
     *            multiple locations
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(PaintProperties paintProps,
            List<SymbolLocationSet> listOfSymbolLocSets) {
        List<IDisplayable> listOfDisplayables = new ArrayList<>();
        setScales(paintProps);

        Map<Color, IWireframeShape> mapOfWireFrames = new HashMap<>();
        Map<Color, IWireframeShape> mapOfMasks = new HashMap<>();
        Map<Color, IShadedShape> mapOfShadedShapes = new HashMap<>();
        Map<Color, Float> mapOfLineWidths = new HashMap<>();
        // this assumes that all symbols of the same color have the same
        // lineWidth
        SymbolPatternManager symbolPatternManager = SymbolPatternManager
                .getInstance();

        for (ISymbolSet eachSymbolSet : listOfSymbolLocSets) {
            Symbol symbol = eachSymbolSet.getSymbol();
            if (symbol == null) {
                continue;
            }
            double sfactor = deviceScale * symbol.getSizeScale() * 0.5;
            Float lineWidth = symbol.getLineWidth();
            Color symbolColor = symbol.getColors()[0];
            mapOfLineWidths.put(symbolColor, lineWidth);
            RGB symbolRGB = null;
            if (symbolColor != null) {
                symbolRGB = new RGB(symbolColor.getRed(),
                        symbolColor.getGreen(), symbolColor.getBlue());
            }
            Coordinate[] symbolLocArray = eachSymbolSet.getLocations();
            Color bgColor = null;
            IWireframeShape mask = null;
            IWireframeShape wireFrameShape = mapOfWireFrames.get(symbolColor);
            if (wireFrameShape == null) {
                wireFrameShape = target.createWireframeShape(false,
                        iDescriptor);

            }

            IShadedShape symbolShadedShape = mapOfShadedShapes.get(symbolColor);
            if (symbolShadedShape == null) {
                symbolShadedShape = target.createShadedShape(false,
                        iDescriptor.getGridGeometry(), false);

            }

            if (symbol.isClear()) {
                RGB bgclr = backgroundColor.getColor(BGColorMode.EDITOR);
                bgColor = new Color(bgclr.red, bgclr.green, bgclr.blue);
                mask = mapOfMasks.get(bgColor);
                if (mask == null) {
                    mask = target.createWireframeShape(false, iDescriptor);
                }
                mapOfLineWidths.put(bgColor, lineWidth);
            }

            try {
                SymbolPattern symbolPattern = symbolPatternManager
                        .getSymbolPattern(symbol.getPatternName());

                /* Get the list of parts to draw the symbol */
                List<ISymbolPart> listOfSymbolParts = symbolPattern.getParts();

                /*
                 * Repeat for ALL the locations at which this symbol needs to be
                 * rendered
                 */
                for (Coordinate currWorldCoord : symbolLocArray) {
                    if (currWorldCoord == null) {
                        continue;
                    }
                    double[] symbolLocWorldCoord = new double[] {
                            currWorldCoord.x, currWorldCoord.y };
                    double[] pixCoord = iDescriptor
                            .worldToPixel(symbolLocWorldCoord);

                    for (ISymbolPart sPart : listOfSymbolParts) {
                        Coordinate[] coords = sPart.getPath();
                        double[][] path = new double[coords.length][3];

                        /*
                         * At each location where this symbol is to be drawn,
                         * create a line segment path
                         */
                        for (int j = 0; j < coords.length; j++) {
                            path[j][0] = pixCoord[0] + (sfactor * coords[j].x);
                            path[j][1] = pixCoord[1] + (-sfactor * coords[j].y);
                        }

                        /* If needed - add the line segment part to the mask */
                        if (symbol.isClear() && (mask != null)) {
                            mask.addLineSegment(path);
                        }

                        /* Add the line-segment path to the wire-frame */
                        wireFrameShape.addLineSegment(path);

                        /*
                         * If needed - add the shaded shape corresponding to the
                         * symbol
                         */
                        if (getDisplayFillMode(sPart.isFilled())) {
                            Coordinate[] pixels = new Coordinate[path.length];
                            for (int k = 0; k < path.length; k++) {
                                pixels[k] = new Coordinate(path[k][0],
                                        path[k][1]);
                            }
                            symbolShadedShape.addPolygonPixelSpace(
                                    toLineString(pixels), symbolRGB);
                        }

                    }

                }

                mapOfWireFrames.put(symbolColor, wireFrameShape);
                mapOfShadedShapes.put(symbolColor, symbolShadedShape);
                if ((symbol.isClear()) && (bgColor != null) && (mask != null)) {
                    mapOfMasks.put(bgColor, mask);
                }

            } catch (SymbolPatternException e) {
                handler.warn(e.getMessage(), e);
                return listOfDisplayables;
            }

        }

        Set<Color> maskColorSet = mapOfMasks.keySet();
        Set<Color> wireFrameColorSet = mapOfWireFrames.keySet();
        Set<Color> shadedShapesColorSet = mapOfShadedShapes.keySet();

        float lineWidthScaleFactor = 0.5f;
        for (Color color : maskColorSet) {
            IWireframeShape maskWireframeShape = mapOfMasks.get(color);
            maskWireframeShape.compile();
            Float theLineWidth = mapOfLineWidths.get(color);
            float lineWidth = 1.0f;

            if (theLineWidth != null) {
                lineWidth = theLineWidth.floatValue() * lineWidthScaleFactor;
            }
            listOfDisplayables.add(new LineDisplayElement(maskWireframeShape,
                    color, lineWidth + 25));
        }

        for (Color color : wireFrameColorSet) {
            IWireframeShape symbolWireframeShape = mapOfWireFrames.get(color);
            symbolWireframeShape.compile();
            Float theLineWidth = mapOfLineWidths.get(color);
            float lineWidth = 1.0f;
            if (theLineWidth != null) {
                lineWidth = theLineWidth.floatValue() * lineWidthScaleFactor;
            }
            listOfDisplayables.add(new LineDisplayElement(symbolWireframeShape,
                    color, lineWidth));
        }

        for (Color color : shadedShapesColorSet) {
            IShadedShape shadedSymbolShape = mapOfShadedShapes.get(color);
            shadedSymbolShape.compile();
            listOfDisplayables.add(new FillDisplayElement(shadedSymbolShape,
                    color.getAlpha()));
        }

        return listOfDisplayables;

    }

    /**
     * Creates a list of IDisplayable Objects from an ISinglePoint object. This
     * was the original method used to create symbols made of Wireframe and
     * Shaded shapes. Creation of these symbols is expensive, and has been
     * replaced with raster versions that provide better performance as well as
     * nicer looking graphics, since they can be created with line anti-aliasing
     * and rounded endcaps. Use createDisplayElements(ISinglePoint,
     * PaintProperties) instead, as this method may become deprecated soon, if
     * it is not needed.
     *
     * @param de
     *            A Drawable Element of a multipoint object
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElementsOrig(ISymbol de,
            PaintProperties paintProps) {
        setScales(paintProps);
        double sfactor = deviceScale * de.getSizeScale();

        List<IDisplayable> slist = new ArrayList<>();
        sym = target.createWireframeShape(false, iDescriptor);
        ss = target.createShadedShape(false, iDescriptor, true);
        IWireframeShape mask = target.createWireframeShape(false, iDescriptor);

        double[] tmp = { de.getLocation().x, de.getLocation().y, 0.0 };
        double[] center = iDescriptor.worldToPixel(tmp);

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(de.getColors());

        /*
         * Find Symbol Pattern associated with this element
         */
        SymbolPattern pattern = null;
        SymbolPatternManager spl = SymbolPatternManager.getInstance();
        try {
            pattern = spl.getSymbolPattern(de.getPatternName());
        } catch (SymbolPatternException spe) {
            handler.warn(spe.getMessage(), spe);
            return slist;
        }

        for (ISymbolPart spart : pattern.getParts()) {
            Coordinate[] coords = spart.getPath();
            double[][] path = new double[coords.length][3];

            for (int j = 0; j < coords.length; j++) {
                path[j][0] = center[0] + (sfactor * coords[j].x);
                path[j][1] = center[1] + (-sfactor * coords[j].y);
            }
            sym.addLineSegment(path);
            if (de.isClear()) {
                mask.addLineSegment(path);
            }

            /*
             * This part of the symbol requires a filled Shadedshape
             */
            if (getDisplayFillMode(spart.isFilled())) {
                Coordinate[] pixels = new Coordinate[path.length];
                for (int k = 0; k < path.length; k++) {
                    pixels[k] = new Coordinate(path[k][0], path[k][1]);
                }
                ss.addPolygonPixelSpace(toLineString(pixels),
                        new RGB(dspClr[0].getRed(), dspClr[0].getGreen(),
                                dspClr[0].getBlue()));
            }
        }

        /*
         * Compile the Wireframe and Shaded shapes
         */
        sym.compile();
        ss.compile();

        /*
         * If background should be cleared, make new LineDisplayElement of the
         * symbol in black with a slightly larger line width, and add it to
         * return list
         */
        if (de.isClear()) {
            RGB bgclr = backgroundColor.getColor(BGColorMode.EDITOR);
            Color bgcolor = new Color(bgclr.red, bgclr.green, bgclr.blue);
            mask.compile();
            LineDisplayElement ldbg = new LineDisplayElement(mask, bgcolor,
                    de.getLineWidth() + 25);
            slist.add(ldbg);
        }

        /*
         * Make LineDisplayElement of the Symbol and add it to return list
         */
        LineDisplayElement lde = new LineDisplayElement(sym, dspClr[0],
                de.getLineWidth());
        slist.add(lde);

        /*
         * Make FillDisplayElement and add it to return list
         */
        FillDisplayElement fde = new FillDisplayElement(ss,
                de.getColors()[0].getAlpha());
        slist.add(fde);

        return slist;
    }

    /**
     * Create IDisplayable of a line with a "Kink" in it.
     *
     * @param kline
     *            A Drawable Element of a Kink Line
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(IKink kline,
            PaintProperties paintProps) {
        setScales(paintProps);
        double sfactor = deviceScale * kline.getSizeScale();

        /*
         * Create the List to be returned, and wireframe shape
         */
        List<IDisplayable> slist = new ArrayList<>();
        IWireframeShape kinkLine = target.createWireframeShape(false,
                iDescriptor);

        /*
         * Get color for creating displayables.
         */
        Color dspClr = getDisplayColor(kline.getColor());

        /*
         * Convert Map to pixel coordinates for the start and end points
         */
        double[] tmp = { kline.getStartPoint().x, kline.getStartPoint().y,
                0.0 };
        double[] first = iDescriptor.worldToPixel(tmp);
        Coordinate startPixel = new Coordinate(first[0], first[1]);
        double[] tmp2 = { kline.getEndPoint().x, kline.getEndPoint().y, 0.0 };
        double[] last = iDescriptor.worldToPixel(tmp2);
        Coordinate endPixel = new Coordinate(last[0], last[1]);

        /*
         * Create a LengthIndexedLine used to reference points along the segment
         * at specific distances
         */
        LineString ls = gf
                .createLineString(new Coordinate[] { startPixel, endPixel });
        LengthIndexedLine lil = new LengthIndexedLine(ls);

        /*
         * Use CornerPatternApplicator to calculate slashes of the "X" pattern
         */
        double kinkLocation = ls.getLength() * kline.getKinkPosition();
        double offset = sfactor * 2.0;
        CornerPatternApplicator ex = new CornerPatternApplicator(lil,
                kinkLocation - offset, kinkLocation + offset);
        ex.setHeight(offset);
        ex.setPatternType(CornerPattern.X_PATTERN);
        double[][] exes = ex.calculateLines();

        /*
         * Use the 2nd slash of the "X" pattern as the "kink" in the line
         * segment
         */
        double[][] coords = new double[][] { first, exes[3], exes[2], last };
        kinkLine.addLineSegment(coords);

        /*
         * Calculate the Arrow Head to display at the end of the line.
         */
        double pointAngle = 90.0;
        double slope = Math.toDegrees(
                Math.atan2((last[1] - exes[2][1]), (last[0] - exes[2][0])));

        ArrowHead arrow1 = new ArrowHead(new Coordinate(last[0], last[1]),
                pointAngle, slope, 1.5 * offset, kline.getArrowHeadType());
        Coordinate[] ahead = arrow1.getArrowHeadShape();

        if (kline.getArrowHeadType() == ArrowHeadType.OPEN) {
            // Add to wireframe
            kinkLine.addLineSegment(toDouble(ahead));
        }
        if (kline.getArrowHeadType() == ArrowHeadType.FILLED) {
            /*
             * create new ShadedShape and FillDisplayElement for the filled
             * arrow head, and add it to return list
             */
            IShadedShape head = target.createShadedShape(false, iDescriptor,
                    false);
            head.addPolygonPixelSpace(toLineString(ahead), new RGB(
                    dspClr.getRed(), dspClr.getGreen(), dspClr.getBlue()));
            head.compile();
            slist.add(new FillDisplayElement(head, 1.0f));
        }

        /*
         * Create new LineDisplayElement from wireframe shapes and add it to
         * return list
         */
        kinkLine.compile();
        slist.add(
                new LineDisplayElement(kinkLine, dspClr, kline.getLineWidth()));
        return slist;

    }

    /**
     * Create IDisplayable of a "wind" drawable element. Can create displayables
     * of wind speed and direction as wind barbs, arrows, or hash marks.
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(IVector vect,
            PaintProperties paintProps) {

        setScales(paintProps);

        List<IDisplayable> slist = null;

        /*
         * Create appropriate vector representation
         */
        switch (vect.getVectorType()) {

        case ARROW:
            slist = createArrow(vect);
            break;

        case WIND_BARB:
            slist = createWindBarb(vect);
            break;

        case HASH_MARK:
            slist = createHashMark(vect);
            break;

        default:
            /*
             * Unrecognized vector type; return empty list
             */
            return new ArrayList<>();

        }

        return slist;
    }

    /**
     * Create IDisplayables of multiple "vector" (e.g. wind) drawable elements.
     * Aggregates into relatively few IDisplayables, for (much) faster
     * performance esp. with large numbers of vectors. Can create displayables
     * of speed and direction as wind barbs, arrows, or hash marks.
     *
     * @param vectors
     *            A list of Drawable Elements of Vector (e.g., wind barb or
     *            arrow) objects
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of (relatively few, combined) IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(List<IVector> vectors,
            PaintProperties paintProps) {
        setScales(paintProps);

        /* Sort incoming (potentially mixed type) vectors by type */
        List<IVector> arrowVectors = null;
        List<IVector> barbVectors = null;
        List<IVector> hashVectors = null;

        for (IVector vector : vectors) {
            switch (vector.getVectorType()) {

            case ARROW:
                if (arrowVectors == null) {
                    arrowVectors = new ArrayList<>();
                }
                arrowVectors.add(vector);
                break;

            case WIND_BARB:
                if (barbVectors == null) {
                    barbVectors = new ArrayList<>();
                }
                barbVectors.add(vector);
                break;

            case HASH_MARK:
                if (hashVectors == null) {
                    hashVectors = new ArrayList<>();
                }
                hashVectors.add(vector);
                break;

            default:
                // Unrecognized vector type; ignore
                break;
            }
        }

        /*
         * For each type, create (high-efficiency, aggregated by color)
         * displayables, and add to combined list for return
         */
        List<IDisplayable> slist = new ArrayList<>();

        if (arrowVectors != null) {
            slist.addAll(createArrows(arrowVectors));
        }
        if (barbVectors != null) {
            slist.addAll(createWindBarbs(barbVectors));
        }
        if (hashVectors != null) {
            slist.addAll(createHashMarks(hashVectors));
        }

        return slist;
    }

    /**
     * Create IDisplayable of a text string.
     *
     * @param txt
     *            A Drawable Element of a text string
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(IText txt,
            PaintProperties paintProps) {
        setScales(paintProps);

        /*
         * Create the List to be returned
         */
        List<IDisplayable> slist = new ArrayList<>();

        /*
         * Skip if the "hide" is true
         */
        if (((Text) txt).getHide() != null && ((Text) txt).getHide()) {
            return slist;
        }

        double[] tmp = { txt.getPosition().x, txt.getPosition().y, 0.0 };
        double[] loc = iDescriptor.worldToPixel(tmp);

        double horizRatio = paintProps.getView().getExtent().getWidth()
                / paintProps.getCanvasBounds().width;
        double vertRatio = paintProps.getView().getExtent().getHeight()
                / paintProps.getCanvasBounds().height;
        IFont font = initializeFont(txt.getFontName(), txt.getFontSize(),
                txt.getStyle());

        /*
         * apply X offset in half-characters
         */
        boolean adjustOffset = false;
        if (txt.getXOffset() != 0) {
            double ratio = paintProps.getView().getExtent().getWidth()
                    / paintProps.getCanvasBounds().width;
            Rectangle2D bounds = target.getStringBounds(font,
                    txt.getString()[0]);
            double charSize = ratio * bounds.getWidth()
                    / txt.getString()[0].length();
            loc[0] += 0.5 * charSize * txt.getXOffset();
            adjustOffset = true;
        }

        /*
         * apply Y offset in half-characters
         */
        if (txt.getYOffset() != 0) {
            double ratio = paintProps.getView().getExtent().getHeight()
                    / paintProps.getCanvasBounds().height;
            Rectangle2D bounds = target.getStringBounds(font,
                    txt.getString()[0]);
            double charSize = ratio * bounds.getHeight();
            loc[1] -= 0.5 * charSize * txt.getYOffset();
            adjustOffset = true;
        }

        if (adjustOffset) {
            double[] tmp1 = { loc[0], loc[1], 0.0 };
            double[] newloc = iDescriptor.pixelToWorld(tmp1);
            ((Text) txt).setLocationOnly(new Coordinate(newloc[0], newloc[1]));
            ((Text) txt).setXOffset(0);
            ((Text) txt).setYOffset(0);
        }

        /*
         * Get text color
         */
        Color clr = getDisplayColor(txt.getTextColor());
        RGB textColor = new RGB(clr.getRed(), clr.getGreen(), clr.getBlue());

        /*
         * Get angle rotation for text. If rotation is "North" relative,
         * calculate the rotation for "Screen" relative.
         */
        double rotation = txt.getRotation();
        if (txt.getRotationRelativity() == TextRotation.NORTH_RELATIVE) {
            rotation += northOffsetAngle(txt.getPosition());
        }

        /*
         * create drawableString and calculate its bounds
         */
        DrawableString dstring = new DrawableString(txt.getString(), textColor);
        dstring.font = font;
        dstring.setCoordinates(loc[0], loc[1]);
        dstring.textStyle = TextStyle.NORMAL;
        dstring.horizontalAlignment = HorizontalAlignment.CENTER;
        dstring.verticallAlignment = VerticalAlignment.MIDDLE;
        dstring.rotation = rotation;

        // Set proper alignment
        HorizontalAlignment align = HorizontalAlignment.CENTER;
        if (txt.getJustification() != null) {
            switch (txt.getJustification()) {
            case RIGHT_JUSTIFY:
                align = HorizontalAlignment.RIGHT;
                break;
            case CENTER:
                align = HorizontalAlignment.CENTER;
                break;
            case LEFT_JUSTIFY:
                align = HorizontalAlignment.LEFT;
                break;
            default:
                align = HorizontalAlignment.CENTER;
                break;
            }
        }

        dstring.horizontalAlignment = align;

        /*
         * create new TextDisplayElement and add it to return list
         */
        TextDisplayElement tde = new TextDisplayElement(dstring, txt.maskText(),
                txt.getDisplayType());
        slist.add(tde);

        return slist;
    }

    /**
     * Create IDisplayable of TCM element.
     *
     * @param tcm
     *            A TCM Element
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ITcm tcm,
            PaintProperties paintProps) {
        List<IDisplayable> slist = new ArrayList<>();

        List<Coordinate> trackPts = new ArrayList<>();

        // draw wind forecast quarters and labels
        for (TcmFcst tcmFcst : tcm.getTcmFcst()) {

            String[] txt = new String[2];
            Calendar fcstHr = tcmFcst.getEndtime();
            if (fcstHr == null) {
                fcstHr = (Calendar) tcm.getAdvisoryTime().clone();
                fcstHr.add(Calendar.HOUR_OF_DAY, tcmFcst.getFcstHr());
            }

            if (tcmFcst.equals(tcm.getTcmFcst().get(0))) {
                txt[1] = tcm.getStormName() + "/"
                        + (tcm.getCentralPressure() > 0
                                ? tcm.getCentralPressure() : "xxx")
                        + "mb";
                txt[0] = String.format("%1$td/%1$tH%1$tM", fcstHr);
            } else {
                txt[0] = String.format("%1$td/%1$tH%1$tM", fcstHr);
                txt[1] = "";
            }

            slist.addAll(createDisplayElements(tcmFcst, paintProps, txt));
            trackPts.add(tcmFcst.getQuarters()[0].getLocation());
        }

        // draw wave quarters
        if (tcm.getWaveQuarters() != null) {
            slist.addAll(
                    createDisplayElements(tcm.getWaveQuarters(), paintProps));
        }

        // draw track
        if (trackPts.size() >= 2) {
            Line trackLn = new Line(null,
                    new Color[] { new Color(0, 255, 255) }, 1.5f, .8, false,
                    false, trackPts, 0, null, LINES, "LINE_DASHED_6");
            slist.addAll(createDisplayElements(trackLn, paintProps, true));
        }

        return slist;
    }

    /**
     * Create IDisplayable of TCM forecast
     *
     * @param tcmFcst
     *            A TCM forecast
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createDisplayElements(ITcmFcst tcmFcst,
            PaintProperties paintProps, String[] txt) {
        List<IDisplayable> slist = new ArrayList<>();
        for (ITcmWindQuarter qua : tcmFcst.getQuarters()) {
            slist.addAll(createDisplayElements(qua, paintProps));
        }

        Symbol ts = new Symbol(null, new Color[] { new Color(0, 255, 255) },
                2.5f, 1.5, false, tcmFcst.getQuarters()[0].getLocation(),
                SYMBOL, this.getTcmFcstSymbolType(tcmFcst));
        slist.addAll(createDisplayElements(ts, paintProps));

        if (txt != null) {
            Text label = new Text(null, "Courier", 14.0f,
                    TextJustification.LEFT_JUSTIFY,
                    tcmFcst.getQuarters()[0].getLocation(), 0.0,
                    /* TTR 895 TextRotation.NORTH_RELATIVE, */
                    TextRotation.SCREEN_RELATIVE, txt, FontStyle.BOLD,
                    getDisplayColor(Color.YELLOW), 4, 0, false,
                    DisplayType.NORMAL, "Text", GENERAL_TEXT);

            slist.addAll(createDisplayElements(label, paintProps));
        }

        return slist;
    }

    /**
     * Returns the TCM symbol according to the wind speed. Hurricane >= 64
     * knots, TS >= 34 knots, TD < 34 knots
     *
     * @param tcmFcst
     * @return
     */
    private String getTcmFcstSymbolType(ITcmFcst tcmFcst) {

        int maxWind = 0;

        if (tcmFcst instanceof TcmFcst) {
            maxWind = ((TcmFcst) tcmFcst).getWindMax();
        }

        if (maxWind == 0) {
            maxWind = getTcmMaxWindFromQuadrant(tcmFcst);
        }

        double lat = tcmFcst.getQuarters()[0].getLocation().y;
        String ret = "TROPICAL_DEPRESSION";
        if (maxWind >= 64) {
            ret = (lat >= 0) ? "HURRICANE_NH" : "HURRICANE_SH";
        } else if (maxWind >= 34) {
            ret = (lat >= 0) ? "TROPICAL_STORM_NH" : "TROPICAL_STORM_SH";
        }

        return ret;
    }

    /*
     * Returns maximum wind in a TCM symbol
     *
     * @param tcmFcst
     *
     * @return
     */
    private int getTcmMaxWindFromQuadrant(ITcmFcst tcmFcst) {

        int maxWind = 0;
        for (ITcmWindQuarter qtr : tcmFcst.getQuarters()) {
            double[] radius = qtr.getQuarters();
            for (double r : radius) {
                if (r > 0) {
                    if (qtr.getWindSpeed() > maxWind) {
                        maxWind = qtr.getWindSpeed();
                    }
                    break;
                }
            }
        }

        return maxWind;
    }

    /**
     * Create IDisplayable of TCM wind/wave quarters
     *
     * @param quatros
     *            - TCM wind/wave quarters
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createDisplayElements(ITcmWindQuarter quatros,
            PaintProperties paintProps) {
        List<IDisplayable> slist = new ArrayList<>();

        Coordinate center = quatros.getLocation();
        Color color = Color.GREEN;
        switch (quatros.getWindSpeed()) {
        // 12 feet wave
        case 0:
            Arc quatro1 = new Arc(null, color, (float) 1.5, 1.0, false, false,
                    0, null, CIRCLE, center,
                    this.calculateDestinationPointMap(center, 0,
                            quatros.getQuarters()[0]),
                    ARC, 1, 0, 90);

            Arc quatro2 = new Arc(null, color, (float) 1.5, 1.0, false, false,
                    0, null, CIRCLE, center,
                    this.calculateDestinationPointMap(center, 0,
                            quatros.getQuarters()[1]),
                    ARC, 1, 90, 180);
            Arc quatro3 = new Arc(null, color, (float) 1.5, 1.0, false, false,
                    0, null, CIRCLE, center,
                    this.calculateDestinationPointMap(center, 0,
                            quatros.getQuarters()[2]),
                    ARC, 1, 180, 270);
            Arc quatro4 = new Arc(null, color, (float) 1.5, 1.0, false, false,
                    0, null, CIRCLE, center,
                    this.calculateDestinationPointMap(center, 0,
                            quatros.getQuarters()[3]),
                    ARC, 1, 270, 360);
            slist.addAll(createDisplayElements(quatro1, paintProps, 3));
            slist.addAll(createDisplayElements(quatro2, paintProps, 3));
            slist.addAll(createDisplayElements(quatro3, paintProps, 3));
            slist.addAll(createDisplayElements(quatro4, paintProps, 3));

            Line ln1 = getWindQuatroLine(getPointOnArc(quatro1, 0),
                    getPointOnArc(quatro2, 0), color);
            ln1.setElemType(LINE_DASHED_4);

            Line ln2 = getWindQuatroLine(getPointOnArc(quatro2, 90),
                    getPointOnArc(quatro3, 90), color);
            ln2.setElemType(LINE_DASHED_4);

            Line ln3 = getWindQuatroLine(getPointOnArc(quatro3, 180),
                    getPointOnArc(quatro4, 180), color);
            ln3.setElemType(LINE_DASHED_4);

            Line ln4 = getWindQuatroLine(getPointOnArc(quatro4, 270),
                    getPointOnArc(quatro1, 270), color);
            ln4.setElemType(LINE_DASHED_4);

            slist.addAll(createDisplayElements(ln1, paintProps));
            slist.addAll(createDisplayElements(ln2, paintProps));
            slist.addAll(createDisplayElements(ln3, paintProps));
            slist.addAll(createDisplayElements(ln4, paintProps));

            return slist;
        case 34:
            // make it consistent with NMAP
            color = new Color(0, 150, 255);
            break;
        case 50:
            color = Color.YELLOW;
            break;
        case 64:
            color = Color.RED;
            break;
        default:
            break;
        }

        Arc quatro1 = new Arc(null, color, (float) 1.5, 1.0, false, false, 0,
                null, CIRCLE, center, this.calculateDestinationPointMap(center,
                        0, quatros.getQuarters()[0]),
                ARC, 1, 0, 90);

        Arc quatro2 = new Arc(null, color, (float) 1.5, 1.0, false, false, 0,
                null, CIRCLE, center, this.calculateDestinationPointMap(center,
                        0, quatros.getQuarters()[1]),
                ARC, 1, 90, 180);
        Arc quatro3 = new Arc(null, color, (float) 1.5, 1.0, false, false, 0,
                null, CIRCLE, center, this.calculateDestinationPointMap(center,
                        0, quatros.getQuarters()[2]),
                ARC, 1, 180, 270);
        Arc quatro4 = new Arc(null, color, (float) 1.5, 1.0, false, false, 0,
                null, CIRCLE, center, this.calculateDestinationPointMap(center,
                        0, quatros.getQuarters()[3]),
                ARC, 1, 270, 360);
        slist.addAll(createDisplayElements((IArc) quatro1, paintProps));
        slist.addAll(createDisplayElements((IArc) quatro2, paintProps));
        slist.addAll(createDisplayElements((IArc) quatro3, paintProps));
        slist.addAll(createDisplayElements((IArc) quatro4, paintProps));

        Line ln1 = getWindQuatroLine(getPointOnArc(quatro1, 0),
                getPointOnArc(quatro2, 0), color);

        Line ln2 = getWindQuatroLine(getPointOnArc(quatro2, 90),
                getPointOnArc(quatro3, 90), color);

        Line ln3 = getWindQuatroLine(getPointOnArc(quatro3, 180),
                getPointOnArc(quatro4, 180), color);

        Line ln4 = getWindQuatroLine(getPointOnArc(quatro4, 270),
                getPointOnArc(quatro1, 270), color);

        slist.addAll(createDisplayElements(ln1, paintProps));
        slist.addAll(createDisplayElements(ln2, paintProps));
        slist.addAll(createDisplayElements(ln3, paintProps));
        slist.addAll(createDisplayElements(ln4, paintProps));

        return slist;
    }

    /**
     * Creates a list of IDisplayable Objects from an IArc object using
     * dashed-line.
     *
     * @param arc
     *            A Drawable Element of an arc object
     * @param paintProps
     *            The paint properties associated with the target
     * @param dashlength
     *            length of dash segment in screen pixel.
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(IArc arc,
            PaintProperties paintProps, double dashlength) {
        setScales(paintProps);

        /*
         * Create the List to be returned, and wireframe shape
         */
        List<IDisplayable> slist = new ArrayList<>();
        IWireframeShape arcpts = target.createWireframeShape(false,
                iDescriptor);

        /*
         * Convert center and circumference point from lat/lon to pixel
         * coordinates.
         */
        double[] tmp = { arc.getCenterPoint().x, arc.getCenterPoint().y, 0.0 };
        double[] center = iDescriptor.worldToPixel(tmp);
        double[] tmp2 = { arc.getCircumferencePoint().x,
                arc.getCircumferencePoint().y, 0.0 };
        double[] circum = iDescriptor.worldToPixel(tmp2);

        /*
         * calculate angle of major axis
         */
        double axisAngle = Math.toDegrees(
                Math.atan2((circum[1] - center[1]), (circum[0] - center[0])));
        double cosineAxis = Math.cos(Math.toRadians(axisAngle));
        double sineAxis = Math.sin(Math.toRadians(axisAngle));

        /*
         * calculate half lengths of major and minor axes
         */
        double[] diff = { circum[0] - center[0], circum[1] - center[1] };
        double major = Math.sqrt((diff[0] * diff[0]) + (diff[1] * diff[1]));
        double minor = major * arc.getAxisRatio();

        // ignore circles with major = 0
        if (major / this.screenToExtent < 0.000001) {
            return slist;
        }

        // calculate length of a single dash segment in degree.
        double deltaAngle = (dashlength / (major / this.screenToExtent)) * 180;
        int deltaA = (int) Math.round(deltaAngle);
        if (deltaA <= 1) {
            deltaA = 2;
        }

        /*
         * Calculate points along the arc
         */
        double angle = arc.getStartAngle();
        int numpts = (int) Math
                .round(arc.getEndAngle() - arc.getStartAngle() + 1.0);
        double[][] path = new double[deltaA + 1][3];
        int kk = 0;
        boolean dash = false;

        for (int j = 0; j < numpts; j++) {

            if (!dash) {
                double thisSine = Math.sin(Math.toRadians(angle));
                double thisCosine = Math.cos(Math.toRadians(angle));
                path[kk][0] = center[0] + (major * cosineAxis * thisCosine)
                        - (minor * sineAxis * thisSine);
                path[kk][1] = center[1] + (major * sineAxis * thisCosine)
                        + (minor * cosineAxis * thisSine);

                // add a line segment for dash line
                kk++;
                if (kk - 1 == deltaA) {
                    arcpts.addLineSegment(path);
                    kk = 0;
                    dash = !dash;
                }

            } else {
                // add a blank segement for the dash line
                kk++;
                if (kk >= deltaA - 2) {
                    dash = !dash;
                    kk = 0;
                }
            }
            angle += 1.0;

        }

        /*
         * Create new LineDisplayElement from wireframe shapes and add it to
         * return list
         */
        arcpts.compile();
        slist.add(new LineDisplayElement(arcpts,
                getDisplayColor(arc.getColors()[0]), arc.getLineWidth()));

        return slist;
    }

    /**
     * Creates a list of IDisplayable Objects from an IArc object
     *
     * @param arc
     *            A Drawable Element of an arc object
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(IArc arc,
            PaintProperties paintProps) {

        setScales(paintProps);

        /*
         * Create the List to be returned, and wireframe shape
         */
        List<IDisplayable> slist = new ArrayList<>();
        IWireframeShape arcpts = target.createWireframeShape(false,
                iDescriptor);

        /*
         * Convert center and circumference point from lat/lon to pixel
         * coordinates.
         */
        double[] tmp = { arc.getCenterPoint().x, arc.getCenterPoint().y, 0.0 };
        double[] center = iDescriptor.worldToPixel(tmp);
        double[] tmp2 = { arc.getCircumferencePoint().x,
                arc.getCircumferencePoint().y, 0.0 };
        double[] circum = iDescriptor.worldToPixel(tmp2);

        /*
         * calculate angle of major axis
         */
        double axisAngle = Math.toDegrees(
                Math.atan2((circum[1] - center[1]), (circum[0] - center[0])));
        double cosineAxis = Math.cos(Math.toRadians(axisAngle));
        double sineAxis = Math.sin(Math.toRadians(axisAngle));

        /*
         * calculate half lengths of major and minor axes
         */
        double[] diff = { circum[0] - center[0], circum[1] - center[1] };
        double major = Math.sqrt((diff[0] * diff[0]) + (diff[1] * diff[1]));
        double minor = major * arc.getAxisRatio();

        /*
         * Calculate points along the arc
         */
        double angle = arc.getStartAngle();
        int numpts = (int) Math
                .round(arc.getEndAngle() - arc.getStartAngle() + 1.0);
        double[][] path = new double[numpts][3];
        for (int j = 0; j < numpts; j++) {
            double thisSine = Math.sin(Math.toRadians(angle));
            double thisCosine = Math.cos(Math.toRadians(angle));

            path[j][0] = center[0] + (major * cosineAxis * thisCosine)
                    - (minor * sineAxis * thisSine);
            path[j][1] = center[1] + (major * sineAxis * thisCosine)
                    + (minor * cosineAxis * thisSine);

            angle += 1.0;
        }
        arcpts.addLineSegment(path);

        /*
         * Create new LineDisplayElement from wireframe shapes and add it to
         * return list
         */
        arcpts.compile();
        slist.add(new LineDisplayElement(arcpts,
                getDisplayColor(arc.getColors()[0]), arc.getLineWidth()));

        return slist;
    }

    /**
     * Creates a list of IDisplayable Objects from an ITrack drawable object
     *
     * @param track
     *            A Drawable Element of a storm track object
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ITrack track,
            PaintProperties paintProps) {

        List<IDisplayable> temps;

        List<Coordinate> points = new ArrayList<>();
        setScales(paintProps);

        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");

        /*
         * Create the List to be returned
         */
        List<IDisplayable> slist = new ArrayList<>();

        /*
         * Get color for creating displayable elements.
         */
        Color iniDspClr = getDisplayColor(track.getInitialColor());
        Color expDspClr = getDisplayColor(track.getExtrapColor());

        /*
         * Create Line element from the initial data points, create its
         * Displayables and add them to the return list
         */
        points.clear();
        for (TrackPoint pt : track.getInitialPoints()) {
            points.add(pt.getLocation());
        }
        Line iline = new Line(null, new Color[] { iniDspClr },
                track.getLineWidth(), 1.0, false, false, points, 0,
                FillPattern.SOLID, LINES, track.getInitialLinePattern());
        temps = createDisplayElements(iline, paintProps);
        slist.addAll(temps);

        /*
         * Loop through the initial data points
         */
        int n = 0;
        Coordinate[] initialPoints = new Coordinate[track
                .getInitialPoints().length];
        for (TrackPoint pt : track.getInitialPoints()) {
            // Add Coordinate point to array
            initialPoints[n] = new Coordinate(pt.getLocation());
            n++;
            /*
             * If there is a time associated with this point, create a
             * displayable of it
             */
            if (pt.getTime() != null) {
                String dtime = sdf.format(pt.getTime().getTime());
                Text txt = new Text(null, track.getFontName(),
                        track.getFontSize(), TextJustification.LEFT_JUSTIFY,
                        pt.getLocation(), 0.0, TextRotation.SCREEN_RELATIVE,
                        new String[] { dtime }, track.getFontStyle(), iniDspClr,
                        0, 3, false, DisplayType.NORMAL, "Text", GENERAL_TEXT);
                temps = createDisplayElements(txt, paintProps);
                slist.addAll(temps);
            }
        }
        /*
         * Create a SymbolLocationSet for the markers used for the Initial data
         * points
         */
        SymbolLocationSet imarkers = new SymbolLocationSet(null,
                new Color[] { iniDspClr }, track.getLineWidth(), 1.0, false,
                initialPoints, "SymbolSet", track.getInitialMarker());
        temps = createDisplayElements(imarkers, paintProps);
        slist.addAll(temps);

        /*
         * Create Line element from the extrapolated data points, create its
         * Displayables and add them to the return list
         */
        points.clear();
        /*
         * here add the last initial point first to create Line element for the
         * extrapolated data points
         */
        int lastInitialPointIndex = track.getInitialPoints().length - 1;
        points.add(
                track.getInitialPoints()[lastInitialPointIndex].getLocation());

        for (TrackPoint pt : track.getExtrapPoints()) {
            points.add(pt.getLocation());
        }
        Line eline = new Line(null, new Color[] { expDspClr },
                track.getLineWidth(), 1.0, false, false, points, 0,
                FillPattern.SOLID, LINES, track.getExtrapLinePattern());
        temps = createDisplayElements(eline, paintProps);
        slist.addAll(temps);

        /*
         * Loop through the Extrapolated data points
         */
        int m = 0;
        boolean[] extrapPointTimeTextDisplayIndicator = track
                .getExtraPointTimeTextDisplayIndicator();
        Coordinate[] extrapPoints = new Coordinate[track
                                                   .getExtrapPoints().length];
        for (TrackPoint pt : track.getExtrapPoints()) {
            // Add Coordinate point to array
            extrapPoints[m] = new Coordinate(pt.getLocation());
            /*
             * If there is a time associated with this point, create a
             * displayable of it
             */
            if (pt.getTime() != null
                    && extrapPointTimeTextDisplayIndicator[m]) {
                String dtime = sdf.format(pt.getTime().getTime());
                Text txt = new Text(null, track.getFontName(),
                        track.getFontSize(), TextJustification.LEFT_JUSTIFY,
                        pt.getLocation(), 0.0, TextRotation.SCREEN_RELATIVE,
                        new String[] { dtime }, track.getFontStyle(), expDspClr,
                        0, 3, false, DisplayType.NORMAL, "Text", GENERAL_TEXT);
                temps = createDisplayElements(txt, paintProps);
                slist.addAll(temps);
            }
            m++;
        }
        /*
         * Create a SymbolLocationSet for the markers used for the Extrapolated
         * data points
         */
        SymbolLocationSet emarkers = new SymbolLocationSet(null,
                new Color[] { expDspClr }, track.getLineWidth(), 1.0, false,
                extrapPoints, "SymbolSet", track.getExtrapMarker());
        temps = createDisplayElements(emarkers, paintProps);
        slist.addAll(temps);

        return slist;

    }

    /**
     * Creates a list of IDisplayable Objects from an Point object
     *
     * @param de
     *            A Drawable Element of a multipoint object
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ISymbol de,
            PaintProperties paintProps) {

        if (de instanceof Symbol) {
            Coordinate[] loc = new Coordinate[] { de.getLocation() };
            SymbolLocationSet syms = new SymbolLocationSet((Symbol) de, loc);
            return createDisplayElements(syms, paintProps);
        } else {
            return new ArrayList<>();
        }

    }

    /**
     * Creates a list of IDisplayable Objects from an ISymbolSet object, used to
     * draw one symbol at one or more locations.
     *
     * @param symbolSet
     *            A symbol with associated lat/lon coordinates
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ISymbolSet symbolSet,
            PaintProperties paintProps) {

        // Set up scale factors
        setScales(paintProps);
        double sfactor = deviceScale * symbolScale
                * symbolSet.getSymbol().getSizeScale();

        /*
         * Create the List to be returned
         */
        List<IDisplayable> slist = new ArrayList<>();

        // get Symbol
        Symbol symb = symbolSet.getSymbol();

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(symb.getColors());

        /*
         * create an AWT BufferedImage from the symbol pattern
         */
        sfactor *= screenToWorldRatio;
        IRenderedImageCallback imageCb = new SymbolImageCallback(
                symb.getPatternName(), sfactor, symb.getLineWidth(),
                symb.isClear(), dspClr[0]);
        /*
         * Initialize raster image for use with graphics target
         */
        IImage pic = null;
        try {
            pic = target.initializeRaster(imageCb);
            pic.stage();
        } catch (Exception e) {
            handler.error("SAG:IMAGE CREATION: " + e.getMessage(), e);
            return slist;
        }

        /*
         * convert lat/lons to pixel coords
         */
        double[][] pts = DrawingUtil.latlonToPixel(symbolSet.getLocations(),
                (IMapDescriptor) iDescriptor);

        /*
         * Create SymbolSetElement and return it
         */
        slist.add(new SymbolSetElement(pic, pts));
        return slist;

    }

    /**
     * Creates a list of IDisplayable raster Objects from an ICombo object
     *
     * @param de
     *            A Drawable Element of a ICombo object
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ICombo de,
            PaintProperties paintProps) {

        // Set up scale factors
        setScales(paintProps);
        double scale = deviceScale * symbolScale * de.getSizeScale()
                * SymbolImageUtil.INITIAL_IMAGE_SIZE;

        if (de instanceof ComboSymbol) {
            String[] patterns = de.getPatternNames();

            /*
             * Get pixel value for the world location
             */
            Coordinate[] loc = new Coordinate[] { de.getLocation() };
            double[] worldPixel = new double[] { loc[0].x, loc[0].y, 0.0 };
            double[] pixel = iDescriptor.worldToPixel(worldPixel);

            /*
             * Get color for creating displayables.
             */
            Color[] dspClr = getDisplayColors(de.getColors());

            /*
             * Calculate the offset for the first symbol ( upper left ) and
             * convert back to world coordinates.
             */
            double[] locUL = iDescriptor.pixelToWorld(new double[] {
                    pixel[0] - (0.5 * scale), pixel[1] - (0.25 * scale), 0.0 });
            Coordinate[] loc1 = new Coordinate[] {
                    new Coordinate(locUL[0], locUL[1]) };

            /*
             * Create a Symbol object for the first pattern
             */
            SymbolLocationSet sym1 = new SymbolLocationSet(null, dspClr,
                    de.getLineWidth(), de.getSizeScale(), de.isClear(), loc1,
                    SYMBOL, patterns[0]);

            /*
             * Calculate the offset for the second symbol ( lower right ) and
             * convert back to world coordinates
             */
            double[] locLR = iDescriptor.pixelToWorld(new double[] {
                    pixel[0] + (0.5 * scale), pixel[1] + (0.25 * scale), 0.0 });
            Coordinate[] loc2 = new Coordinate[] {
                    new Coordinate(locLR[0], locLR[1]) };

            /*
             * Create a Symbol object for the second pattern
             */
            SymbolLocationSet sym2 = new SymbolLocationSet(null, dspClr,
                    de.getLineWidth(), de.getSizeScale(), de.isClear(), loc2,
                    SYMBOL, patterns[1]);

            // add the "slash" symbol object
            SymbolLocationSet sym3 = new SymbolLocationSet(null, dspClr,
                    de.getLineWidth(), de.getSizeScale(), de.isClear(), loc,
                    SYMBOL, "SLASH");

            /*
             * create IDisplayables from each Symbol and add to return list.
             */
            List<IDisplayable> stuff = createDisplayElements(sym1, paintProps);
            stuff.addAll(createDisplayElements(sym2, paintProps));
            stuff.addAll(createDisplayElements(sym3, paintProps));
            return stuff;
        } else {
            return new ArrayList<>();
        }

    }

    /**
     * Create IDisplayable of a TCA element.
     *
     * @param tca
     *            A Drawable Element of a TCA element
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public List<IDisplayable> createDisplayElements(ITca tca,
            PaintProperties paintProps) {

        List<IDisplayable> rlist = new ArrayList<>();

        List<TropicalCycloneAdvisory> advisories = tca.getAdvisories();

        /*
         * If there are no current watch/warning advisories, print a text box
         * with some storm info.
         */
        if (advisories.isEmpty()) {
            String[] noneMsg = new String[3];
            noneMsg[0] = tca.getStormType() + " " + tca.getStormName();
            noneMsg[1] = "Advisory " + tca.getAdvisoryNumber();
            noneMsg[2] = "No current Watches/Warnings";

            Text display = new Text(null, "Courier", 14.0f,
                    TextJustification.CENTER, tca.getTextLocation(), 0.0,
                    TextRotation.SCREEN_RELATIVE, noneMsg, FontStyle.REGULAR,
                    getDisplayColor(Color.YELLOW), 0, 0, false, DisplayType.BOX,
                    "Text", GENERAL_TEXT);

            rlist = createDisplayElements(display, paintProps);
            return rlist;
        }

        /*
         * create displayables of each watch/warning type in the following
         * order.
         */
        rlist.addAll(createAdvisoryDisplay(advisories, "Tropical Storm",
                "Watch", Color.YELLOW, 7.0f, paintProps));
        rlist.addAll(createAdvisoryDisplay(advisories, "Hurricane", "Watch",
                Color.PINK, 13.0f, paintProps));
        rlist.addAll(createAdvisoryDisplay(advisories, "Tropical Storm",
                "Warning", Color.BLUE, 7.0f, paintProps));
        rlist.addAll(createAdvisoryDisplay(advisories, "Hurricane", "Warning",
                Color.RED, 13.0f, paintProps));

        return rlist;
    }

    /**
     * Creates displayables for a specific watch/warning type from the advisory
     * information
     *
     * @param advisories
     *            List of current tropical cyclone advisories
     * @param severe
     *            specifies "Tropical Storm" or "Hurricane" severity
     * @param type
     *            specifies whether the advisory type is a watch or warning
     * @param clr
     *            the color to use for the display
     * @param lw
     *            the line width to use for the display
     * @param paintProps
     *            The paint properties associated with the target
     * @return
     */
    private List<IDisplayable> createAdvisoryDisplay(
            List<TropicalCycloneAdvisory> advisories, String severe,
            String type, Color clr, float lw, PaintProperties paintProps) {

        List<IDisplayable> alist = new ArrayList<>();

        /*
         * loop through each advisory and determine if it is of the given
         * severity and type.
         */
        for (TropicalCycloneAdvisory tca : advisories) {
            if (tca.getSeverity().equals(severe)
                    && tca.getAdvisoryType().equals(type)) {

                BPGeography segment = tca.getSegment();

                /*
                 * loop through each path defining the watch/warning segment
                 */
                for (Coordinate[] coords : segment.getPaths()) {

                    // convert Coordinate[] to ArrayList<Coordinate>
                    List<Coordinate> pts = new ArrayList<>();
                    pts.addAll(Arrays.asList(coords));

                    // if the segment is a Waterway, and the segment is closed,
                    // create a filled displayable
                    boolean fill = false;
                    if (coords[0].equals2D(coords[coords.length - 1])
                            && (segment instanceof WaterBreakpoint)) {
                        fill = true;
                    }

                    // create a line for each segment and then create its
                    // displayable
                    Line seg = new Line(null, new Color[] { clr }, lw, 1.0,
                            false, fill, pts, 0, FillPattern.SOLID, LINES,
                            "LINE_SOLID");
                    alist.addAll(createDisplayElements(seg, paintProps));

                }

            }

        }

        return alist;
    }

    /**
     * Applies a given line pattern to a line path.
     *
     * @param pattern
     *            Line pattern definition.
     * @param pts
     *            Data points defining the line path.
     */
    private void handleLinePattern(LinePattern pattern, double[][] pts,
            ScaleType stype) {

        double start;
        double end;

        /*
         * Get scale size and colors from drawable element.
         */
        double scale = elem.getSizeScale();

        /*
         * R6520 - adjust pip size for fronts to match NMAP2's size visually.
         */
        if (elem instanceof Line
                && ((Line) elem).getElemCategory().equalsIgnoreCase(FRONT)) {
            scale *= frontPatternFactor;
        }

        if (scale <= 0.0) {
            scale = 1.0;
        }
        double sfactor = deviceScale * scale;
        Color[] clr = getDisplayColors(elem.getColors());

        /*
         * create a LineString Geometry from the data points defining the line
         * path
         */
        Coordinate[] coords = new Coordinate[pts.length];
        for (int i = 0; i < pts.length; i++) {
            coords[i] = new Coordinate(pts[i][0], pts[i][1]);
        }
        GeometryFactory gf1 = new GeometryFactory();
        LineString ls = gf1.createLineString(coords);

        // Get the total length of the line path
        double totalDist = ls.getLength();

        /*
         * If line contains a FILLED arrow head, decrease total length of line
         * path by the length of the arrow head.
         */
        if (pattern.hasArrowHead()
                && pattern.getArrowHeadType() == ArrowHeadType.FILLED) {
            totalDist -= arrow.getLength();
        }

        /*
         * Create a LengthIndexedLine used to reference points along the path at
         * specific distances
         */
        LengthIndexedLine lil = new LengthIndexedLine(ls);
        LocationIndexedLine lol = new LocationIndexedLine(ls);
        LengthLocationMap llm = new LengthLocationMap(ls);

        /*
         * Calculate number of patterns that can fit on path
         */
        double psize = pattern.getLength() * sfactor;

        double numPatterns = Math.floor(totalDist / psize);

        /*
         * Calculate the amount to increase or decrease the pattern length so
         * that the line path ends on a full complete pattern.
         */
        double leftover = totalDist - (numPatterns * psize);
        if (leftover > 0.5 * psize) {
            // Add one more pattern and decrease size of pattern
            numPatterns += 1.0;
        }

        /*
         * Calculate a scale factor that will be used to adjust the size of each
         * segment in the pattern
         */
        if (stype == ScaleType.SCALE_BLANK_LINE_ONLY) {
            pattern = pattern.scaleBlankLineToLength(
                    totalDist / (numPatterns * sfactor));
        } else {
            pattern = pattern
                    .scaleToLength(totalDist / (numPatterns * sfactor));
        }

        /*
         * If size of line is less than size of a full pattern, then default to
         * solid line
         */
        if (numPatterns < 1) {
            Coordinate[] ncoords = lil.extractLine(0.0, totalDist)
                    .getCoordinates();
            double[][] npts = toDouble(ncoords);
            wfs[0].addLineSegment(npts);
            return;
        }

        /*
         * Loop through the number times the pattern will occur along the line
         * path
         */
        double begPat = 0.0;
        double endPat;
        LinearLocation linloc0 = llm.getLocation(begPat);
        for (int n = 0; n < (int) Math.floor(numPatterns); n++) {

            double patlen = pattern.getLength() * sfactor;
            endPat = begPat + patlen;
            LinearLocation linloc1 = llm.getLocation(endPat);
            LengthIndexedLine sublil = new LengthIndexedLine(
                    lol.extractLine(linloc0, linloc1));

            /*
             * Loop over each segment in the pattern
             */
            double currDist = 0.0;
            double endLoc;
            for (PatternSegment seg : pattern.getSegments()) {
                int colorNum = seg.getColorLocation();

                // if not enough colors specified, default to first color
                if (colorNum >= wfs.length) {
                    colorNum = 0;
                }

                // Calculate end location of this segment
                // first find the size of pattern segment
                double seglen = seg.getLength() * sfactor;
                endLoc = currDist + seglen;

                /*
                 * Apply specific pattern segment
                 */
                switch (seg.getPatternType()) {

                case BLANK:
                    /*
                     * Do nothing
                     */
                    break;

                case LINE:
                    /*
                     * Extract the data points along this line segment
                     */
                    Geometry section = sublil.extractLine(currDist, endLoc);
                    Coordinate[] newcoords = section.getCoordinates();
                    /*
                     * Add line segment path to appropriate WireframeShape
                     */
                    double[][] newpts = toDouble(newcoords);
                    wfs[colorNum].addLineSegment(newpts);
                    break;

                case CIRCLE:
                    /*
                     * Use ArcPatternApplicator to calculate the points around
                     * circle and then add them to the appropriate
                     * WireframeShape
                     */
                    start = 0.0;
                    end = 360.0;
                    ArcPatternApplicator circ = new ArcPatternApplicator(sublil,
                            currDist, endLoc);
                    circ.setArcAttributes(start, end, seg.getNumberInArc());
                    wfs[colorNum].addLineSegment(circ.calculateLines());
                    break;

                case CIRCLE_FILLED:
                    /*
                     * Use ArcPatternApplicator to calculate the points around
                     * circle and then add them to the appropriate ShadedShape
                     */
                    start = 0.0;
                    end = 360.0;
                    ArcPatternApplicator circf = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    circf.setArcAttributes(start, end, seg.getNumberInArc());
                    Coordinate[] carea = circf.calculateFillArea();
                    LineString[] circle = toLineString(carea);
                    ss.addPolygonPixelSpace(circle,
                            new RGB(clr[seg.getColorLocation()].getRed(),
                                    clr[seg.getColorLocation()].getGreen(),
                                    clr[seg.getColorLocation()].getBlue()));
                    break;

                case ARC_180_DEGREE:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 180.0;
                    } else {
                        start = 0.0;
                        end = -180.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 180 degree arc and then add them to the appropriate
                     * WireframeShape
                     */
                    ArcPatternApplicator app180 = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app180.setArcAttributes(start, end, seg.getNumberInArc());
                    wfs[colorNum].addLineSegment(app180.calculateLines());
                    break;

                case ARC_180_DEGREE_FILLED:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 180.0;
                    } else {
                        start = 0.0;
                        end = -180.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 180 degree arc. The addSegmentToFill method ensures
                     * points along path are added to points along the arc,
                     * creating a closed shape
                     */
                    ArcPatternApplicator app = new ArcPatternApplicator(sublil,
                            currDist, endLoc);
                    app.setArcAttributes(start, end, seg.getNumberInArc());
                    app.addSegmentToFill(true);
                    Coordinate[] area = app.calculateFillArea();
                    LineString[] arc = toLineString(area);
                    /*
                     * Add fill area to the appropriate ShadedShape and add line
                     * segment path to the appropriate WireframeShape.
                     */
                    ss.addPolygonPixelSpace(arc,
                            new RGB(clr[seg.getColorLocation()].getRed(),
                                    clr[seg.getColorLocation()].getGreen(),
                                    clr[seg.getColorLocation()].getBlue()));
                    wfs[colorNum].addLineSegment(app.getSegmentPts());
                    break;

                case ARC_180_DEGREE_CLOSED:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 180.0;
                    } else {
                        start = 0.0;
                        end = -180.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 180 degree arc
                     */
                    ArcPatternApplicator app180c = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app180c.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wfs[colorNum].addLineSegment(app180c.calculateLines());
                    wfs[colorNum].addLineSegment(app180c.getSegmentPts());
                    break;

                case ARC_90_DEGREE:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 90.0;
                    } else {
                        start = 0.0;
                        end = -90.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 90 degree arc
                     */
                    ArcPatternApplicator app90 = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app90.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wfs[colorNum].addLineSegment(app90.calculateLines());
                    wfs[colorNum].addLineSegment(app90.getSegmentPts());
                    break;

                case ARC_270_DEGREE:
                    if (seg.isReverseSide()) {
                        start = -45.0;
                        end = 225.0;
                    } else {
                        start = 45.0;
                        end = -225.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 270 degree arc and then add them to the appropriate
                     * WireframeShape
                     */
                    ArcPatternApplicator app270 = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app270.setArcAttributes(start, end, seg.getNumberInArc());
                    wfs[colorNum].addLineSegment(app270.calculateLines());
                    break;

                case ARC_270_DEGREE_WITH_LINE:
                    if (seg.isReverseSide()) {
                        start = -45.0;
                        end = 225.0;
                    } else {
                        start = 45.0;
                        end = -225.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 270 degree arc
                     */
                    ArcPatternApplicator app270l = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app270l.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wfs[colorNum].addLineSegment(app270l.calculateLines());
                    wfs[colorNum].addLineSegment(app270l.getSegmentPts());
                    break;

                case BOX:
                    /*
                     * Use CornerPatternApplicator to calculate the coordinates
                     * of the box and add the pattern segments to the
                     * appropriate WireframeShape
                     */
                    CornerPatternApplicator box = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    box.setHeight(seg.getOffsetSize() * sfactor);
                    box.setPatternType(CornerPattern.BOX);
                    wfs[colorNum].addLineSegment(box.calculateLines());
                    break;

                case BOX_FILLED:
                    /*
                     * Use CornerPatternApplicator to calculate the coordinates
                     * of the box and add the pattern segments to the
                     * appropriate ShadedShape
                     */
                    CornerPatternApplicator boxf = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    boxf.setHeight(seg.getOffsetSize() * sfactor);
                    boxf.setPatternType(CornerPattern.BOX);
                    Coordinate[] boxarea = boxf.calculateFillArea();
                    LineString[] barea = toLineString(boxarea);
                    ss.addPolygonPixelSpace(barea,
                            new RGB(clr[seg.getColorLocation()].getRed(),
                                    clr[seg.getColorLocation()].getGreen(),
                                    clr[seg.getColorLocation()].getBlue()));
                    break;

                case X_PATTERN:
                    /*
                     * Use CornerPatternApplicator to calculate both slashes of
                     * the "X" pattern
                     */
                    CornerPatternApplicator ex = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    ex.setHeight(seg.getOffsetSize() * sfactor);
                    ex.setPatternType(CornerPattern.X_PATTERN);
                    double[][] exes = ex.calculateLines();
                    double[][] slash1 = new double[][] { exes[0], exes[1] };
                    double[][] slash2 = new double[][] { exes[2], exes[3] };
                    /*
                     * Add both slash segments to appropriate WireframeShape
                     */
                    wfs[colorNum].addLineSegment(slash1);
                    wfs[colorNum].addLineSegment(slash2);
                    break;

                case Z_PATTERN:
                    /*
                     * Use CornerPatternApplicator to calculate the "Z" pattern
                     * and add the pattern segments to the appropriate
                     * WireframeShape
                     */
                    CornerPatternApplicator ze = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    ze.setHeight(seg.getOffsetSize() * sfactor);
                    ze.setPatternType(CornerPattern.Z_PATTERN);
                    wfs[colorNum].addLineSegment(ze.calculateLines());
                    break;

                case DOUBLE_LINE:
                    /*
                     * Use CornerPatternApplicator to calculate both top and
                     * bottom line segments along either side of line path.
                     */
                    CornerPatternApplicator dl = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    dl.setHeight(seg.getOffsetSize() * sfactor);
                    dl.setPatternType(CornerPattern.DOUBLE_LINE);
                    double[][] segs = dl.calculateLines();
                    double[][] top = new double[][] { segs[0], segs[1] };
                    double[][] bottom = new double[][] { segs[2], segs[3] };
                    /*
                     * Add top and bottom line segments to appropriate
                     * WireframeShape
                     */
                    wfs[colorNum].addLineSegment(top);
                    wfs[colorNum].addLineSegment(bottom);
                    break;

                case TICK:
                    /*
                     * use CornerPatternApplicator to calculate tick segment
                     */
                    CornerPatternApplicator tick = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    tick.setHeight(seg.getOffsetSize() * sfactor);
                    tick.setPatternType(CornerPattern.TICK);
                    /*
                     * Add tick segment and the line path segment to the
                     * appropriate WireframeShape.
                     */
                    wfs[colorNum].addLineSegment(tick.getSegmentPts());
                    wfs[colorNum].addLineSegment(tick.calculateLines());
                    break;

                case ARROW_HEAD:
                    start = -120.0;
                    end = 120.0;
                    /*
                     * Use ArcPatternApplicator to calculate the points around
                     * the arc
                     */
                    ArcPatternApplicator arrow1 = new ArcPatternApplicator(
                            sublil, currDist, endLoc);
                    arrow1.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wfs[colorNum].addLineSegment(arrow1.calculateLines());
                    wfs[colorNum].addLineSegment(arrow1.getSegmentPts());
                    break;

                default:
                    /*
                     * Do nothing.
                     */
                    handler.warn("Pattern definition: "
                            + seg.getPatternType().toString()
                            + " is not found.  Ignoring...");

                    break;
                }

                /*
                 * Update the starting location of the next segment to the
                 * ending location of the current segment.
                 */
                currDist = endLoc;

            }

            begPat = endPat;
            linloc0 = linloc1;

        }

    }

    /**
     * Change format of an array of points from Coordinate[] to double[][]
     *
     * @param coords
     *            - input data points
     * @return data points in new format
     */
    protected double[][] toDouble(Coordinate[] coords) {

        double[][] dpts = new double[coords.length][3];

        for (int k = 0; k < coords.length; k++) {
            dpts[k][0] = coords[k].x;
            dpts[k][1] = coords[k].y;
        }

        return dpts;
    }

    /**
     * Change format of an array of points from Coordinate[] to LineString
     *
     * @param coords
     *            - input data points
     * @return data points in new format
     */
    protected LineString[] toLineString(Coordinate[] coords) {
        return new LineString[] { gf.createLineString(coords) };
    }

    /**
     * Change format of an array of points from Coordinate[] to LineString
     *
     * @param coords
     *            - input data points
     * @return data points in new format
     */
    protected LineString[] toLineString(double[][] points) {

        Coordinate[] coords = new Coordinate[points.length];
        for (int j = 0; j < points.length; j++) {
            coords[j] = new Coordinate(points[j][0], points[j][1]);
        }

        return new LineString[] { gf.createLineString(coords) };
    }

    /**
     * Makes sure last data point is the same as the first
     *
     * @param data
     *            Input data points
     * @return Same data points with first and last point the same
     */
    private double[][] ensureClosed(double[][] data) {

        int n = data.length - 1;

        /*
         * if first point equals last point, return data
         */
        if ((data[0][0] == data[n][0]) && (data[0][1] == data[n][1])) {
            return data;
        } else {
            /*
             * add first point to end of data, and return new data points
             */
            double[][] newdata = Arrays.copyOf(data, data.length + 1);
            newdata[data.length] = newdata[0];

            return newdata;
        }
    }

    /**
     * Apply a fill pattern to a Line path.
     *
     * @param area
     *            data points defining the area to fill
     * @return A fill element with a ShadedShape ready for display
     */
    private FillDisplayElement createFill(double[][] area) {

        /*
         * create ShadedShape for fill area
         */
        IShadedShape fillarea = target.createShadedShape(false, iDescriptor,
                true);

        /*
         * If Requested Fill is not SOLID or TRANSPARENCY, get the fill pattern
         * and apply it to the ShadedShape
         */
        if (elem.getFillPattern() != FillPattern.TRANSPARENCY
                && elem.getFillPattern() != FillPattern.SOLID) {
            FillPatternList fpl = new FillPatternList();
            byte[] fpattern = fpl.getFillPattern(elem.getFillPattern());
            fillarea.setFillPattern(fpattern);
        }

        /*
         * Convert double[][] to Coordinate[]
         */
        Coordinate[] coords = new Coordinate[area.length];
        for (int i = 0; i < area.length; i++) {
            coords[i] = new Coordinate(area[i][0], area[i][1]);
        }

        /*
         * Create LineString[] from Coordinates[]
         */
        LineString[] ls = toLineString(coords);

        /*
         * Add fill area to Shaded Shape
         */
        Color[] dspClr = getDisplayColors(elem.getColors());
        Color fillClr = dspClr[0];
        if (dspClr.length > 1 && dspClr[1] != null) {
            fillClr = dspClr[1];
        }

        fillarea.addPolygonPixelSpace(ls, new RGB(fillClr.getRed(),
                fillClr.getGreen(), fillClr.getBlue()));
        fillarea.compile();

        float alpha = 1.0f;
        if (elem.getFillPattern() == FillPattern.TRANSPARENCY) {
            alpha = 0.5f;
        }

        /*
         * return new FillDisplayElement with new ShadedShape
         */
        return new FillDisplayElement(fillarea, alpha);

    }

    /**
     * Determines an appropriate scale factor to use when calculating the the
     * coordinates of the Displayables. Also sets a screen to pixel ratio for
     * use when needing to convert the size of something from screen relative to
     * pixel relative
     *
     * @param props
     *            The paint properties associated with the target
     */
    protected void setScales(PaintProperties props) {

        /*
         * Sets the device scale factor based on the current pixel extent
         */
        IExtent pe = props.getView().getExtent();
        deviceScale = pe.getHeight() / 300.0;

        /*
         * Set the screen to pixel ratio
         */
        Rectangle bounds = props.getCanvasBounds();
        screenToExtent = pe.getHeight() / bounds.height;

        screenToWorldRatio = bounds.width / pe.getWidth();
    }

    /**
     * Calculates the angle difference of "north" relative to the screen's
     * y-axis at a given lat/lon location.
     *
     * @param loc
     *            - The point location in Lat/Lon coordinates
     * @return The angle difference of "north" versus pixel coordinate's y-axis
     */
    private double northOffsetAngle(Coordinate loc) {
        double delta = 0.05;

        /*
         * Calculate points in pixel coordinates just south and north of
         * original location.
         */
        double[] south = { loc.x, loc.y - delta, 0.0 };
        double[] pt1 = iDescriptor.worldToPixel(south);

        double[] north = { loc.x, loc.y + delta, 0.0 };
        double[] pt2 = iDescriptor.worldToPixel(north);

        return -90.0 - Math
                .toDegrees(Math.atan2((pt2[1] - pt1[1]), (pt2[0] - pt1[0])));
    }

    /**
     * Creates IDisplayables of a hash mark representing wind direction
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createHashMark(IVector vect) {
        // scale factor for length of hash lines
        double sfactor = 10.0 * deviceScale;

        // scale factor for spacing between hash lines
        double spaceFactor = sfactor * 0.25;

        // distance between hash lines
        double spacing = 1.0 * spaceFactor;
        if (vect.getLineWidth() > 3.0) {
            spacing += (0.25 * spaceFactor * (vect.getLineWidth() - 3));
        }

        // hash line length
        double scaleSize = sfactor * vect.getSizeScale();

        /*
         * Create the List to be returned, and wireframe shape
         */
        List<IDisplayable> slist = new ArrayList<>();
        IWireframeShape hash = target.createWireframeShape(false, iDescriptor);

        /*
         * Convert location from lat/lon coordinates to pixel coordinates
         */
        double[] tmp = { vect.getLocation().x, vect.getLocation().y, 0.0 };
        double[] start = iDescriptor.worldToPixel(tmp);

        /*
         * calculate the angle and distance to the four points defining the hash
         * mark
         *
         * Note: hash direction is clockwise. Rotate to counter clockwise for
         * display.
         */
        double angle = northOffsetAngle(vect.getLocation())
                + (360.0 - vect.getDirection());
        double theta = Math.toDegrees(Math.atan(spacing / scaleSize));
        double dist = 0.5
                * Math.sqrt((spacing * spacing) + (scaleSize * scaleSize));

        /*
         * find the X and Y offsets from the center to the end points of hash
         * lines
         */
        double dX1 = dist * Math.cos(Math.toRadians(angle - theta));
        double dY1 = dist * Math.sin(Math.toRadians(angle - theta));

        double dX2 = dist * Math.cos(Math.toRadians(angle + theta));
        double dY2 = dist * Math.sin(Math.toRadians(angle + theta));

        /*
         * add both hash mark lines to the wireframe
         */
        hash.addLineSegment(
                new double[][] { { start[0] + dX1, start[1] - dY1, 0.0 },
                    { start[0] - dX2, start[1] + dY2, 0.0 } });

        hash.addLineSegment(
                new double[][] { { start[0] - dX1, start[1] + dY1, 0.0 },
                    { start[0] + dX2, start[1] - dY2, 0.0 } });

        /*
         * compile wireframe and add it to the return list
         */
        hash.compile();
        slist.add(new LineDisplayElement(hash, getDisplayColor(vect.getColor()),
                vect.getLineWidth()));
        return slist;
    }

    /**
     * Creates IDisplayables of multiple hash marks representing wind direction
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createHashMarks(List<IVector> vectors) {
        // scale factor for length of hash lines
        double sfactor = 10.0 * deviceScale;

        // scale factor for spacing between hash lines
        double spaceFactor = sfactor * 0.25;

        // distance between hash lines
        double spacing = 1.0 * spaceFactor;

        /*
         * Each returned IDisplayable can have only one color, so must build
         * separate IWireframeShape for each color. Keep track in map...
         */
        Map<Color, IWireframeShape> hashMarkMap = new HashMap<>();

        float lineWidth = vectors.get(0).getLineWidth();

        for (IVector vect : vectors) {

            if (vect.getLineWidth() > 3.0) {
                spacing += (0.25 * spaceFactor * (vect.getLineWidth() - 3));
            }

            // hash line length
            double scaleSize = sfactor * vect.getSizeScale();

            // display color for this vector
            Color color = vect.getColor();

            /*
             * Get the cumulative shape we're (possibly) constructing for this
             * color. If no such shape yet, start one for that color.
             */
            IWireframeShape hashMarks = hashMarkMap.computeIfAbsent(color,
                    k -> target.createWireframeShape(false, iDescriptor));

            /*
             * Convert location from lat/lon coordinates to pixel coordinates
             */
            double[] tmp = { vect.getLocation().x, vect.getLocation().y, 0.0 };
            double[] start = iDescriptor.worldToPixel(tmp);

            /*
             * calculate the angle and distance to the four points defining the
             * hash mark
             */
            double angle = northOffsetAngle(vect.getLocation())
                    + vect.getDirection();
            double theta = Math.toDegrees(Math.atan(spacing / scaleSize));
            double dist = 0.5
                    * Math.sqrt((spacing * spacing) + (scaleSize * scaleSize));

            /*
             * find the X and Y offsets from the center to the end points of
             * hash lines
             */
            double dX1 = dist * Math.cos(Math.toRadians(angle - theta));
            double dY1 = dist * Math.sin(Math.toRadians(angle - theta));

            double dX2 = dist * Math.cos(Math.toRadians(angle + theta));
            double dY2 = dist * Math.sin(Math.toRadians(angle + theta));

            /*
             * add both hash mark lines to the wireframe
             */
            hashMarks.addLineSegment(
                    new double[][] { { start[0] + dX1, start[1] - dY1, 0.0 },
                        { start[0] - dX2, start[1] + dY2, 0.0 } });

            hashMarks.addLineSegment(
                    new double[][] { { start[0] - dX1, start[1] + dY1, 0.0 },
                        { start[0] + dX2, start[1] - dY2, 0.0 } });

        }

        /*
         * Create the List to be returned
         */
        List<IDisplayable> slist = new ArrayList<>();

        /*
         * For each color encountered above, compile the accumulated wireframes
         * of that color, package them into a single display element, and add to
         * return list
         */
        for (Map.Entry<Color, IWireframeShape> entry : hashMarkMap.entrySet()) {
            Color color = entry.getKey();
            IWireframeShape hashMarks = entry.getValue();
            hashMarks.compile();
            slist.add(new LineDisplayElement(hashMarks, color, lineWidth));
        }

        return slist;
    }

    /**
     * Creates IDisplayables of an arrow representing direction (e.g., wind)
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createArrow(IVector vect) {
        double sfactor = deviceScale * vect.getSizeScale();

        /*
         * Create the List to be returned, and wireframe shape
         */
        List<IDisplayable> slist = new ArrayList<>();
        IWireframeShape arrow1 = target.createWireframeShape(false,
                iDescriptor);

        /*
         * Convert location from lat/lon coordinates to pixel
         */
        double[] tmp = { vect.getLocation().x, vect.getLocation().y, 0.0 };
        double[] start = iDescriptor.worldToPixel(tmp);

        /*
         * calculate the length of the arrow, and its direction
         */
        double speed = 10.;
        if (!vect.hasDirectionOnly()) {
            speed = vect.getSpeed();
        }
        double arrowLength = sfactor * speed;

        // Reverse 180 degrees since wind direction is the direction where the
        // wind comes from.
        double angle = 90.0 - northOffsetAngle(vect.getLocation())
                + vect.getDirection();

        /*
         * find the end point (tip of the arrow)
         */
        double[] end = new double[3];
        end[0] = start[0] + (arrowLength * Math.cos(Math.toRadians(angle)));
        end[1] = start[1] + (arrowLength * Math.sin(Math.toRadians(angle)));
        end[2] = 0.0;

        /*
         * add shaft of arrow to wireframe
         */
        arrow1.addLineSegment(new double[][] { start, end });

        /*
         * create shadedshape of the arrow head
         */
        double pointAngle = 60.0;
        double height = deviceScale * vect.getArrowHeadSize() * 2;
        ArrowHead head = new ArrowHead(new Coordinate(end[0], end[1]),
                pointAngle, angle, height, ArrowHeadType.FILLED);
        Coordinate[] ahead = head.getArrowHeadShape();
        Color clr = getDisplayColor(vect.getColor());
        IShadedShape arrowHead = target.createShadedShape(false, iDescriptor,
                false);
        arrowHead.addPolygonPixelSpace(toLineString(ahead),
                new RGB(clr.getRed(), clr.getGreen(), clr.getBlue()));

        /*
         * Create background arrow, if background mask is requested
         */
        if (vect.hasBackgroundMask()) {

            /*
             * get background color
             */
            RGB bg = backgroundColor.getColor(BGColorMode.EDITOR);
            Color bgColor = new Color(bg.red, bg.green, bg.blue);

            /*
             * Add shaft and arrow head coordinates to mask wireframe, and add
             * mask wireframe to return list
             */
            IWireframeShape mask = target.createWireframeShape(false,
                    iDescriptor);
            mask.addLineSegment(new double[][] { start, end });
            mask.addLineSegment(toDouble(ahead));
            mask.compile();
            slist.add(new LineDisplayElement(mask, bgColor,
                    (float) (vect.getLineWidth() + deviceScale)));

        }

        /*
         * Get color for creating displayables.
         */
        Color dspClr = getDisplayColor(vect.getColor());

        /*
         * add shaft wireframe to return list
         */
        arrow1.compile();
        slist.add(new LineDisplayElement(arrow1, dspClr, vect.getLineWidth()));

        /*
         * Add arrow head to return list
         */
        FillDisplayElement fde = new FillDisplayElement(arrowHead,
                vect.getColor().getAlpha());
        slist.add(fde);

        return slist;
    }

    /**
     * Creates IDisplayables of multiple arrows representing wind direction
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createArrows(List<IVector> vectors) {

        // Each returned IDisplayable can have only one color, so must build
        // separate shapes for each color. Keep track of all this in map...
        Map<Color, IWireframeShape> arrowShaftMap = new HashMap<>();
        Map<Color, IWireframeShape> maskMap = new HashMap<>();
        Map<Color, IShadedShape> arrowHeadMap = new HashMap<>();

        // This line of code and higher level code assumes that the line
        // width of arrows will always be uniform across all arrows. It is
        // possible that a new requirement might change that. This line of
        // code sets the line width for all arrows to be the same.
        float lineWidth = vectors.get(0).getLineWidth();

        // For each vector
        for (IVector vect : vectors) {

            // Get the arrowhead type
            ArrowHead.ArrowHeadType arrowHeadType = vect.getArrowHeadType();

            // Get color for creating displayables.
            Color color = getDisplayColor(vect.getColor());
            double sfactor = deviceScale * vect.getSizeScale();

            // Get the 3 cumulative shapes we're (possibly) constructing for
            // this color (or bgColor). If no such shape yet, start one for that
            // color.
            IWireframeShape arrowShafts = arrowShaftMap.computeIfAbsent(color,
                    k -> target.createWireframeShape(false, iDescriptor));

            IWireframeShape masks = null;
            if (vect.hasBackgroundMask()) {
                RGB bg = backgroundColor.getColor(BGColorMode.EDITOR);
                Color bgColor = new Color(bg.red, bg.green, bg.blue);
                masks = maskMap.computeIfAbsent(bgColor,
                        k -> target.createWireframeShape(false, iDescriptor));
            }

            IShadedShape arrowHeads = null;
            if (arrowHeadType == ArrowHead.ArrowHeadType.FILLED) {
                arrowHeads = arrowHeadMap.computeIfAbsent(color,
                        k -> target.createShadedShape(false,
                                iDescriptor.getGridGeometry()));
            }

            // Convert location from lat/lon coordinates to pixel
            double[] tmp = { vect.getLocation().x, vect.getLocation().y, 0.0 };
            double[] start = iDescriptor.worldToPixel(tmp);

            // Calculate the length of the arrow, and its direction
            double speed = 10.;
            if (!vect.hasDirectionOnly()) {
                speed = vect.getSpeed();
            }
            double arrowLength = sfactor * speed;

            // Reverse 180 degrees since wind direction is the direction where
            // the wind comes from.
            double angle = 90.0 - northOffsetAngle(vect.getLocation())
                    + vect.getDirection();

            // Find the end point (tip of the arrow)
            double[] end = new double[3];
            end[0] = start[0] + (arrowLength * Math.cos(Math.toRadians(angle)));
            end[1] = start[1] + (arrowLength * Math.sin(Math.toRadians(angle)));
            end[2] = 0.0;

            // Add shaft of arrow to wireframe
            arrowShafts.addLineSegment(new double[][] { start, end });

            // Create shadedshape of the arrow head
            double pointAngle = 60.0;
            double height = deviceScale * vect.getArrowHeadSize() * 2;
            ArrowHead head = new ArrowHead(new Coordinate(end[0], end[1]),
                    pointAngle, angle, height, arrowHeadType);
            Coordinate[] ahead = head.getArrowHeadShape();

            if (arrowHeadType == ArrowHead.ArrowHeadType.FILLED) {
                arrowHeads.addPolygonPixelSpace(toLineString(ahead), new RGB(
                        color.getRed(), color.getGreen(), color.getBlue()));
            } else if (arrowHeadType == ArrowHead.ArrowHeadType.OPEN) {
                arrowShafts.addLineSegment(toDouble(ahead));
            }

            // Create background arrow, if background mask is requested
            if (masks != null && vect.hasBackgroundMask()) {

                // Add shaft and arrow head coordinates to mask wireframe
                masks.addLineSegment(new double[][] { start, end });
                masks.addLineSegment(toDouble(ahead));
            }

        }

        return combineWindBarbElements(arrowShaftMap, maskMap, arrowHeadMap,
                lineWidth);
    }

    /**
     * Creates IDisplayables of a wind barb
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createWindBarb(IVector vect) {
        double sfactor = deviceScale * vect.getSizeScale() * 10.;
        IWireframeShape mask = null;

        // default black
        Color bgColor = new Color(0, 0, 0);

        /*
         * Create the List to be returned, and wireframe shape
         */
        List<IDisplayable> slist = new ArrayList<>();
        IWireframeShape barb = target.createWireframeShape(false, iDescriptor);
        IShadedShape flags = target.createShadedShape(false, iDescriptor,
                false);
        if (vect.hasBackgroundMask()) {
            mask = target.createWireframeShape(false, iDescriptor);
            RGB bg = backgroundColor.getColor(BGColorMode.EDITOR);
            bgColor = new Color(bg.red, bg.green, bg.blue);
        }

        /*
         * Get color for creating displayables.
         */
        Color dspClr = getDisplayColor(vect.getColor());

        /*
         * Convert location from lat/lon coordinates to pixel
         */
        double[] tmp = { vect.getLocation().x, vect.getLocation().y, 0.0 };
        double[] start = iDescriptor.worldToPixel(tmp);

        /*
         * If calm wind, draw circle
         */
        if (vect.getSpeed() < 0.5) {
            double[][] pts = calculateCircle(start, sfactor * 0.1);
            if (vect.hasBackgroundMask()) {
                mask.addLineSegment(pts);
                mask.compile();
                slist.add(new LineDisplayElement(mask, bgColor,
                        vect.getLineWidth() + (float) deviceScale));
            }
            barb.addLineSegment(pts);
            barb.compile();
            slist.add(
                    new LineDisplayElement(barb, dspClr, vect.getLineWidth()));
            return slist;
        }

        /*
         * Compute the number of flags, whole barbs and half barbs needed to
         * represent the wind speed.
         */
        int speed = (int) Math.floor(vect.getSpeed() + 2.5);
        int numflags = speed / 50;
        int remainder = speed % 50;
        int numbarbs = remainder / 10;
        remainder = remainder % 10;
        int halfbarbs = remainder / 5;

        // Maximum number of segments on original size barb
        double maxSegments = 6.0;
        int numsegs = (2 * numflags) + numbarbs + halfbarbs;
        double segmentSpacing = sfactor / maxSegments;
        double windLength = segmentSpacing * Math.max(maxSegments, numsegs);
        double barbLength = sfactor / 3.0;

        /*
         * find the end point of the wind barb
         */
        double angle = -90.0 - northOffsetAngle(vect.getLocation())
                + vect.getDirection();
        double[] end = new double[3];
        end[0] = start[0] + (windLength * Math.cos(Math.toRadians(angle)));
        end[1] = start[1] + (windLength * Math.sin(Math.toRadians(angle)));
        end[2] = 0.0;
        barb.addLineSegment(new double[][] { start, end });
        if (vect.hasBackgroundMask()) {
            mask.addLineSegment(new double[][] { start, end });
        }

        /*
         * Create a LengthIndexedLine used to reference points along the path at
         * specific distances
         */
        LineString[] ls = toLineString(
                new Coordinate[] { new Coordinate(start[0], start[1]),
                        new Coordinate(end[0], end[1]) });
        LengthIndexedLine lil = new LengthIndexedLine(ls[0]);
        double currentLoc = lil.getEndIndex(); // start from tail end

        double brbAngle = 70.0;
        double barbAngle = angle + brbAngle;
        if (vect.getLocation().y < 0.0) {
            barbAngle = angle - brbAngle;
        }
        double cosineBarbAngle = Math.cos(Math.toRadians(barbAngle));
        double sineBarbAngle = Math.sin(Math.toRadians(barbAngle));

        /*
         * Process flags
         */
        for (int j = 0; j < numflags; j++) {
            Coordinate[] coords = new Coordinate[4];
            coords[0] = lil.extractPoint(currentLoc);
            coords[1] = lil.extractPoint(currentLoc - segmentSpacing);
            double xtip = coords[1].x + (barbLength * cosineBarbAngle);
            double ytip = coords[1].y + (barbLength * sineBarbAngle);
            coords[2] = new Coordinate(xtip, ytip);
            coords[3] = coords[0];
            LineString[] oneFlag = toLineString(coords);
            flags.addPolygonPixelSpace(oneFlag, new RGB(dspClr.getRed(),
                    dspClr.getGreen(), dspClr.getBlue()));
            if (vect.hasBackgroundMask()) {
                mask.addLineSegment(toDouble(coords));
            }
            currentLoc -= 2 * segmentSpacing;
        }

        /*
         * Process barbs
         */
        for (int j = 0; j < numbarbs; j++) {
            Coordinate[] coords = new Coordinate[2];
            coords[0] = lil.extractPoint(currentLoc);
            double xtip = coords[0].x + (barbLength * cosineBarbAngle);
            double ytip = coords[0].y + (barbLength * sineBarbAngle);
            coords[1] = new Coordinate(xtip, ytip);
            double[][] pts = toDouble(coords);
            barb.addLineSegment(pts);
            if (vect.hasBackgroundMask()) {
                mask.addLineSegment(pts);
            }
            currentLoc -= segmentSpacing;
        }

        /*
         * Process half barbs
         *
         * Note - for a five-knot wind barb, drawn it at the end of the second
         * segment (counting from the end of the wind barb).
         */
        if (numflags == 0 && numbarbs == 0 && halfbarbs == 1) {
            currentLoc -= segmentSpacing;
        }

        for (int j = 0; j < halfbarbs; j++) {
            Coordinate[] coords = new Coordinate[2];
            coords[0] = lil.extractPoint(currentLoc);
            double xtip = coords[0].x + (0.5 * barbLength * cosineBarbAngle);
            double ytip = coords[0].y + (0.5 * barbLength * sineBarbAngle);
            coords[1] = new Coordinate(xtip, ytip);
            double[][] pts = toDouble(coords);
            barb.addLineSegment(pts);
            if (vect.hasBackgroundMask()) {
                mask.addLineSegment(pts);
            }
            currentLoc -= segmentSpacing;
        }

        if (vect.hasBackgroundMask()) {
            mask.compile();
            slist.add(new LineDisplayElement(mask, bgColor,
                    vect.getLineWidth() + (float) deviceScale));
        }

        flags.compile();
        FillDisplayElement fde = new FillDisplayElement(flags,
                vect.getColor().getAlpha());
        slist.add(fde);

        /*
         * add shaft wireframe to return list
         */
        barb.compile();
        slist.add(new LineDisplayElement(barb, dspClr, vect.getLineWidth()));

        return slist;
    }

    /**
     * Creates high-efficiency IDisplayables of multiple wind barbs
     *
     * @param vect
     *            A Drawable Element of a wind object
     * @return A list of IDisplayable elements
     */
    private List<IDisplayable> createWindBarbs(List<IVector> vectors) {

        /*
         * Each returned IDisplayable can have only one color, so must build
         * separate shapes for each color. Keep track of all this in map...
         */
        Map<Color, IWireframeShape> barbMap = new HashMap<>();
        Map<Color, IWireframeShape> maskMap = new HashMap<>();
        Map<Color, IShadedShape> flagMap = new HashMap<>();

        float lineWidth = vectors.get(0).getLineWidth();

        for (IVector vect : vectors) {
            // display color for this vector
            Color color = vect.getColor();
            double sfactor = deviceScale * vect.getSizeScale() * 10.;

            /*
             * Get the 3 cumulative shapes we're (possibly) constructing for
             * this color (or bgColor). If no such shape yet, start one for that
             * color.
             */
            IWireframeShape barbs = barbMap.computeIfAbsent(color,
                    k -> target.createWireframeShape(false, iDescriptor));

            IWireframeShape masks = null;
            if (vect.hasBackgroundMask()) {
                RGB bg = backgroundColor.getColor(BGColorMode.EDITOR);
                Color bgColor = new Color(bg.red, bg.green, bg.blue);
                masks = maskMap.computeIfAbsent(bgColor,
                        k -> target.createWireframeShape(false, iDescriptor));
            }

            IShadedShape flags = flagMap.computeIfAbsent(color,
                    k -> target.createShadedShape(false, iDescriptor, false));

            /*
             * Convert location from lat/lon coordinates to pixel
             */
            double[] tmp = { vect.getLocation().x, vect.getLocation().y, 0.0 };
            double[] start = iDescriptor.worldToPixel(tmp);

            /*
             * If calm wind, draw circle
             */
            if (vect.getSpeed() < 0.5) {
                double[][] pts = calculateCircle(start, sfactor * 0.1);
                if (vect.hasBackgroundMask()) {
                    masks.addLineSegment(pts);
                }
                barbs.addLineSegment(pts);
            } else {
                /*
                 * Compute the number of flags, whole barbs and half barbs
                 * needed to represent the wind speed.
                 */
                int speed = (int) Math.floor(vect.getSpeed() + 2.5);
                int numflags = speed / 50;
                int remainder = speed % 50;
                int numbarbs = remainder / 10;
                remainder = remainder % 10;
                int halfbarbs = remainder / 5;

                // Maximum number of segments on original size barb
                double maxSegments = 6.0;

                int numsegs = (2 * numflags) + numbarbs + halfbarbs;
                double segmentSpacing = sfactor / maxSegments;
                double windLength = segmentSpacing
                        * Math.max(maxSegments, numsegs);
                double barbLength = sfactor / 3.0;

                /*
                 * find the end point of the wind barb
                 */
                double angle = -90.0 - northOffsetAngle(vect.getLocation())
                        + vect.getDirection();
                double[] end = new double[3];
                end[0] = start[0]
                        + (windLength * Math.cos(Math.toRadians(angle)));
                end[1] = start[1]
                        + (windLength * Math.sin(Math.toRadians(angle)));
                end[2] = 0.0;
                barbs.addLineSegment(new double[][] { start, end });
                if (vect.hasBackgroundMask()) {
                    masks.addLineSegment(new double[][] { start, end });
                }

                /*
                 * Create a LengthIndexedLine used to reference points along the
                 * path at specific distances
                 */
                LineString[] ls = toLineString(
                        new Coordinate[] { new Coordinate(start[0], start[1]),
                                new Coordinate(end[0], end[1]) });
                LengthIndexedLine lil = new LengthIndexedLine(ls[0]);

                // start from tail end
                double currentLoc = lil.getEndIndex();

                double brbAngle = 70.0;
                double barbAngle = angle + brbAngle;
                if (vect.getLocation().y < 0.0) {
                    barbAngle = angle - brbAngle;
                }
                double cosineBarbAngle = Math.cos(Math.toRadians(barbAngle));
                double sineBarbAngle = Math.sin(Math.toRadians(barbAngle));

                /*
                 * Process flags
                 */
                for (int j = 0; j < numflags; j++) {
                    Coordinate[] coords = new Coordinate[4];
                    coords[0] = lil.extractPoint(currentLoc);
                    coords[1] = lil.extractPoint(currentLoc - segmentSpacing);
                    double xtip = coords[1].x + (barbLength * cosineBarbAngle);
                    double ytip = coords[1].y + (barbLength * sineBarbAngle);
                    coords[2] = new Coordinate(xtip, ytip);
                    coords[3] = coords[0];
                    LineString[] oneFlag = toLineString(coords);
                    flags.addPolygonPixelSpace(oneFlag, new RGB(color.getRed(),
                            color.getGreen(), color.getBlue()));
                    if (vect.hasBackgroundMask()) {
                        masks.addLineSegment(toDouble(coords));
                    }
                    currentLoc -= 2 * segmentSpacing;
                }

                /*
                 * Process barbs
                 */
                for (int j = 0; j < numbarbs; j++) {
                    Coordinate[] coords = new Coordinate[2];
                    coords[0] = lil.extractPoint(currentLoc);
                    double xtip = coords[0].x + (barbLength * cosineBarbAngle);
                    double ytip = coords[0].y + (barbLength * sineBarbAngle);
                    coords[1] = new Coordinate(xtip, ytip);
                    double[][] pts = toDouble(coords);
                    barbs.addLineSegment(pts);
                    if (vect.hasBackgroundMask()) {
                        masks.addLineSegment(pts);
                    }
                    currentLoc -= segmentSpacing;
                }

                /*
                 * Process half barbs
                 */
                for (int j = 0; j < halfbarbs; j++) {
                    Coordinate[] coords = new Coordinate[2];
                    coords[0] = lil.extractPoint(currentLoc);
                    double xtip = coords[0].x
                            + (0.5 * barbLength * cosineBarbAngle);
                    double ytip = coords[0].y
                            + (0.5 * barbLength * sineBarbAngle);
                    coords[1] = new Coordinate(xtip, ytip);
                    double[][] pts = toDouble(coords);
                    barbs.addLineSegment(pts);
                    if (vect.hasBackgroundMask()) {
                        masks.addLineSegment(pts);
                    }
                    currentLoc -= segmentSpacing;
                }
            }
        }

        return combineWindBarbElements(barbMap, maskMap, flagMap, lineWidth);
    }

    /*
     * Combine and compile wind barb elements in to a list.
     *
     * @param barbMap
     *
     * @param maskMap
     *
     * @param flagMap
     *
     * @param lineWidth
     *
     * @return List<IDisplayable>
     */
    private List<IDisplayable> combineWindBarbElements(
            Map<Color, IWireframeShape> barbMap,
            Map<Color, IWireframeShape> maskMap,
            Map<Color, IShadedShape> flagMap, float lineWidth) {

        List<IDisplayable> slist = new ArrayList<>();

        /*
         * For each color, compile the accumulated wireframes (or shaded shapes)
         * of that color, package them into a single display element, and add to
         * return list
         */
        for (Map.Entry<Color, IWireframeShape> entry : barbMap.entrySet()) {
            IWireframeShape barbs = entry.getValue();
            barbs.compile();
            slist.add(new LineDisplayElement(barbs, entry.getKey(), lineWidth));
        }

        for (Map.Entry<Color, IWireframeShape> entry : maskMap.entrySet()) {
            IWireframeShape masks = entry.getValue();
            masks.compile();
            slist.add(new LineDisplayElement(masks, entry.getKey(),
                    lineWidth + (float) deviceScale));
        }

        for (Map.Entry<Color, IShadedShape> entry : flagMap.entrySet()) {
            IShadedShape flags = entry.getValue();
            flags.compile();
            slist.add(new FillDisplayElement(flags, entry.getKey().getAlpha()));
        }

        return slist;
    }

    /**
     * @param center
     * @param radius
     * @return
     */
    protected double[][] calculateCircle(double[] center, double radius) {

        int numpts = 16;
        double[][] arcpts = new double[numpts + 1][3];

        double inc = 360.0 / numpts;
        double angle = 0.0;
        for (int j = 0; j < numpts; j++) {
            arcpts[j][0] = center[0]
                    + (radius * Math.cos(Math.toRadians(angle)));
            arcpts[j][1] = center[1]
                    + (radius * Math.sin(Math.toRadians(angle)));
            angle += inc;
        }
        arcpts[numpts] = arcpts[0];

        return arcpts;
    }

    /*
     * Initialize Font for use with the graphics target
     */
    private IFont initializeFont(String fontName, float fontSize,
            FontStyle fstyle) {
        Style[] styles = null;
        if (fstyle != null) {
            switch (fstyle) {
            case BOLD:
                styles = new Style[] { Style.BOLD };
                break;
            case ITALIC:
                styles = new Style[] { Style.ITALIC };
                break;
            case BOLD_ITALIC:
                styles = new Style[] { Style.BOLD, Style.ITALIC };
                break;
            case REGULAR:
                break;
            }
        }

        /*
         * set smoothing and scaleFont to false to disable anti-aliasing (which
         * cause the fuzziness of the text).
         */
        IFont font = target.initializeFont(fontName, fontSize, styles);
        font.setSmoothing(false);
        font.setScaleFont(false);

        return font;
    }

    /**
     * Set some display attributes for all elements on a layer.
     *
     * @param mono
     * @param clr
     * @param fill
     */
    public void setLayerDisplayAttr(boolean mono, Color clr, boolean fill) {
        this.layerMonoColor = mono;
        this.layerColor = clr;
        this.layerFilled = fill;
    }

    /**
     * Get the colors for displaying an element.
     */
    protected Color[] getDisplayColors(Color[] clr) {

        Color[] newClr = new Color[clr.length];

        for (int ii = 0; ii < clr.length; ii++) {

            if (layerMonoColor && layerColor != null) {
                newClr[ii] = layerColor;
            } else {
                newClr[ii] = clr[ii];
            }
        }

        return newClr;
    }

    /**
     * Get the colors for displaying an element.
     */
    protected Color getDisplayColor(Color clr) {

        if (layerMonoColor && layerColor != null) {
            return layerColor;
        } else {
            return clr;
        }

    }

    /**
     * Get the fill mode for displaying an element.
     */
    private boolean getDisplayFillMode(boolean filled) {

        /*
         * Match NMAP2 behavior, non-filled elements will always be drawn as
         * non-filled. Filled objects should be drawn as filled only when the
         * "filled" flag for its layer is set to "true" or they are on the
         * active layer, so it is necessary to set the "layerFilled" flag to
         * true before generating displayables for such objects (see
         * AtcfResource.drawFilledElement()).
         */
        return (filled && layerFilled);
    }

    /**
     * Calculate end point from a start point, distance and angle.
     *
     * @param startPt
     * @param angle
     * @param distance
     * @return - end point
     */
    private Coordinate calculateDestinationPointMap(Coordinate startPt,
            double angle, double distance) {
        GeodeticCalculator gc = new GeodeticCalculator(DefaultEllipsoid.WGS84);
        gc.setStartingGeographicPoint(startPt.x, startPt.y);
        gc.setDirection(angle, distance * DrawingUtil.NM2M);

        Point2D pt1 = gc.getDestinationGeographicPoint();
        return new Coordinate(pt1.getX(), pt1.getY());
    }

    /**
     * Get the point on arc with a specified angle from the starting angle.
     *
     * @param arc
     * @param angle
     * @return
     */
    private Coordinate getPointOnArc(Arc arc, double angle) {
        /*
         * Convert center and circumference point from lat/lon to pixel
         * coordinates.
         */
        double[] tmp = { arc.getCenterPoint().x, arc.getCenterPoint().y, 0.0 };
        double[] center = iDescriptor.worldToPixel(tmp);
        double[] tmp2 = { arc.getCircumferencePoint().x,
                arc.getCircumferencePoint().y, 0.0 };
        double[] circum = iDescriptor.worldToPixel(tmp2);

        double radius = Math
                .sqrt((center[0] - circum[0]) * (center[0] - circum[0])
                        + (center[1] - circum[1]) * (center[1] - circum[1]));

        /*
         * calculate angle of major axis
         */
        double axisAngle = 90 + Math.toDegrees(
                Math.atan2((circum[1] - center[1]), (circum[0] - center[0])));
        angle += axisAngle;

        double thisSine = Math.sin(Math.toRadians(angle));
        double thisCosine = Math.cos(Math.toRadians(angle));

        double[] pt = new double[] { center[0] + (radius * thisCosine),
                center[1] + (radius * thisSine) };

        double[] mapPt = iDescriptor.pixelToWorld(pt);

        return new Coordinate(mapPt[0], mapPt[1]);
    }

    /**
     * Get the line that connects two TCM wind/wave quarters
     *
     * @param pt1
     *            - start point
     * @param pt2
     *            - end point
     * @param color
     * @return - A Line
     */
    private Line getWindQuatroLine(Coordinate pt1, Coordinate pt2,
            Color color) {
        List<Coordinate> pts = new ArrayList<>();
        pts.add(pt1);
        pts.add(pt2);

        return new Line(null, new Color[] { color }, 1.5f, .5, false, false,
                pts, 0, null, LINES, "LINE_SOLID");
    }

    /**
     * Clear the geometry, but leave open for use.
     */
    public void reset() {
        if (ss != null) {
            ss.reset();
        }
        if (sym != null) {
            sym.reset();
        }
        if (wfs != null) {
            for (IWireframeShape shape : wfs) {
                if (shape != null) {
                    shape.reset();
                }
            }
        }
    }

    /**
     * Generate a box that could hold a text string.
     *
     * @param txt
     *            A Drawable Element of a text string
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    @SuppressWarnings("deprecation")
    public ElementRangeRecord findTextBoxRange(IText txt,
            PaintProperties paintProps) {

        setScales(paintProps);

        double[] tmp = { txt.getPosition().x, txt.getPosition().y, 0.0 };
        double[] loc = iDescriptor.worldToPixel(tmp);

        double horizRatio = paintProps.getView().getExtent().getWidth()
                / paintProps.getCanvasBounds().width;
        double vertRatio = paintProps.getView().getExtent().getHeight()
                / paintProps.getCanvasBounds().height;

        /*
         * Initialize Font Style
         */
        IFont font = initializeFont(txt.getFontName(), txt.getFontSize(),
                txt.getStyle());

        /*
         * apply X offset in half-characters
         */
        boolean adjustOffset = false;
        if (txt.getXOffset() != 0) {
            double ratio = paintProps.getView().getExtent().getWidth()
                    / paintProps.getCanvasBounds().width;
            Rectangle2D bounds = target.getStringBounds(font,
                    txt.getString()[0]);
            double charSize = ratio * bounds.getWidth()
                    / txt.getString()[0].length();
            loc[0] += 0.5 * charSize * txt.getXOffset();
            adjustOffset = true;
        }

        /*
         * apply Y offset in half-characters
         */
        if (txt.getYOffset() != 0) {
            double ratio = paintProps.getView().getExtent().getHeight()
                    / paintProps.getCanvasBounds().height;
            Rectangle2D bounds = target.getStringBounds(font,
                    txt.getString()[0]);
            double charSize = ratio * bounds.getHeight();
            loc[1] -= 0.5 * charSize * txt.getYOffset();
            adjustOffset = true;
        }

        if (adjustOffset) {
            double[] tmp1 = { loc[0], loc[1], 0.0 };
            double[] newloc = iDescriptor.pixelToWorld(tmp1);
            ((Text) txt).setLocationOnly(new Coordinate(newloc[0], newloc[1]));
            ((Text) txt).setXOffset(0);
            ((Text) txt).setYOffset(0);
        }

        /*
         * Get text color
         */
        Color clr = getDisplayColor(txt.getTextColor());
        RGB textColor = new RGB(clr.getRed(), clr.getGreen(), clr.getBlue());

        /*
         * Get angle rotation for text. If rotation is "North" relative,
         * calculate the rotation for "Screen" relative.
         */
        double rotation = txt.getRotation();
        if (txt.getRotationRelativity() == TextRotation.NORTH_RELATIVE) {
            rotation += northOffsetAngle(txt.getPosition());
        }

        /*
         * create drawableString and calculate its bounds
         */
        DrawableString dstring = new DrawableString(txt.getString(), textColor);
        dstring.font = font;
        dstring.setCoordinates(loc[0], loc[1]);
        dstring.textStyle = TextStyle.NORMAL;
        dstring.horizontalAlignment = HorizontalAlignment.CENTER;
        dstring.verticallAlignment = VerticalAlignment.MIDDLE;
        dstring.rotation = rotation;

        Rectangle2D bounds = target.getStringsBounds(dstring);
        double xOffset = (bounds.getWidth() + 1) * horizRatio / 2;
        double yOffset = (bounds.getHeight() + 1) * vertRatio / 2;

        /*
         * Set proper alignment
         */
        HorizontalAlignment align = HorizontalAlignment.CENTER;
        double left = xOffset;
        double right = xOffset;
        if (txt.getJustification() != null) {
            switch (txt.getJustification()) {
            case RIGHT_JUSTIFY:
                align = HorizontalAlignment.RIGHT;
                left = xOffset * 2;
                right = 0.0;
                break;
            case CENTER:
                align = HorizontalAlignment.CENTER;
                break;
            case LEFT_JUSTIFY:
                align = HorizontalAlignment.LEFT;
                left = 0.0;
                right = xOffset * 2;
                break;
            default:
                align = HorizontalAlignment.CENTER;
                break;
            }
        }

        dstring.horizontalAlignment = align;

        IExtent box = new PixelExtent(dstring.basics.x - left,
                dstring.basics.x + right, dstring.basics.y - yOffset,
                dstring.basics.y + yOffset);

        List<Coordinate> rngBox = new ArrayList<>();
        rngBox.add(
                new Coordinate(box.getMinX() - ElementRangeRecord.RANGE_OFFSET,
                        box.getMaxY() + ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(
                new Coordinate(box.getMaxX() + ElementRangeRecord.RANGE_OFFSET,
                        box.getMaxY() + ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(
                new Coordinate(box.getMaxX() + ElementRangeRecord.RANGE_OFFSET,
                        box.getMinY() - ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(
                new Coordinate(box.getMinX() - ElementRangeRecord.RANGE_OFFSET,
                        box.getMinY() - ElementRangeRecord.RANGE_OFFSET));

        List<Coordinate> textPos = new ArrayList<>();
        textPos.add(new Coordinate(loc[0], loc[1]));

        font.dispose();

        return new ElementRangeRecord(rngBox, textPos, false);
    }

    /**
     * Find a range box that holds the symbol image.
     *
     * @param symbolSet
     *            A symbol with associated lat/lon coordinates
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public ElementRangeRecord findSymbolRange(ISymbol sym,
            PaintProperties paintProps) {

        Coordinate[] loc = new Coordinate[] { sym.getLocation() };

        // Set up scale factors
        setScales(paintProps);
        double sfactor = deviceScale * symbolScale * sym.getSizeScale();

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(sym.getColors());

        /*
         * create an AWT BufferedImage from the symbol pattern
         */
        sfactor *= screenToWorldRatio;
        IRenderedImageCallback imageCb = new SymbolImageCallback(
                sym.getPatternName(), sfactor, sym.getLineWidth(),
                sym.isClear(), dspClr[0]);
        /*
         * Initialize raster image for use with graphics target
         */
        IImage pic = null;
        try {
            pic = target.initializeRaster(imageCb);
            pic.stage();
        } catch (Exception e) {
            handler.warn("SAG:IMAGE CREATION: " + e.getMessage(), e);
        }

        /*
         * convert lat/lons to pixel coords
         */
        double[][] pts = DrawingUtil.latlonToPixel(loc,
                (IMapDescriptor) iDescriptor);

        /*
         * Build range
         */
        List<Coordinate> rngBox = new ArrayList<>();
        int halfWidth = (pic != null) ? pic.getWidth() / 2 : 0;
        rngBox.add(new Coordinate(
                pts[0][0] - halfWidth - ElementRangeRecord.RANGE_OFFSET,
                pts[0][1] + halfWidth + ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(
                pts[0][0] + halfWidth + ElementRangeRecord.RANGE_OFFSET,
                pts[0][1] + halfWidth + ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(
                pts[0][0] + halfWidth + ElementRangeRecord.RANGE_OFFSET,
                pts[0][1] - halfWidth - ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(
                pts[0][0] - halfWidth - ElementRangeRecord.RANGE_OFFSET,
                pts[0][1] - halfWidth - ElementRangeRecord.RANGE_OFFSET));

        List<Coordinate> symPos = new ArrayList<>();
        symPos.add(sym.getLocation());

        if (pic != null) {
            pic.dispose();
        }

        return new ElementRangeRecord(rngBox, symPos, false);

    }

    /**
     * Find a range box that holds the image of a combo symbol.
     *
     * @param symbolSet
     *            A symbol with associated lat/lon coordinates
     * @param paintProps
     *            The paint properties associated with the target
     * @return A list of IDisplayable elements
     */
    public ElementRangeRecord findComboSymbolRange(ICombo combo,
            PaintProperties paintProps) {

        // Set up scale factors
        setScales(paintProps);
        double scale = deviceScale * symbolScale * combo.getSizeScale()
                * SymbolImageUtil.INITIAL_IMAGE_SIZE;

        String[] patterns = combo.getPatternNames();

        /*
         * Get pixel value for the world location
         */
        Coordinate[] loc = new Coordinate[] { combo.getLocation() };
        double[] worldPixel = new double[] { loc[0].x, loc[0].y, 0.0 };
        double[] pixel = iDescriptor.worldToPixel(worldPixel);

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(combo.getColors());

        /*
         * Calculate the offset for the first symbol ( upper left ) and convert
         * back to world coordinates.
         */
        double[] locUL = iDescriptor.pixelToWorld(new double[] {
                pixel[0] - (0.5 * scale), pixel[1] - (0.25 * scale), 0.0 });
        Coordinate[] loc1 = new Coordinate[] {
                new Coordinate(locUL[0], locUL[1]) };

        /*
         * Create a Symbol object for the first pattern
         */
        Symbol sym1 = new Symbol(null, dspClr, combo.getLineWidth(),
                combo.getSizeScale(), combo.isClear(), loc1[0], SYMBOL,
                patterns[0]);
        ElementRangeRecord rng1 = findSymbolRange(sym1, paintProps);

        /*
         * Calculate the offset for the second symbol ( lower right ) and
         * convert back to world coordinates
         */
        double[] locLR = iDescriptor.pixelToWorld(new double[] {
                pixel[0] + (0.5 * scale), pixel[1] + (0.25 * scale), 0.0 });
        Coordinate[] loc2 = new Coordinate[] {
                new Coordinate(locLR[0], locLR[1]) };

        /*
         * Create a Symbol object for the second pattern
         */
        Symbol sym2 = new Symbol(null, dspClr, combo.getLineWidth(),
                combo.getSizeScale(), combo.isClear(), loc2[0], SYMBOL,
                patterns[1]);
        ElementRangeRecord rng2 = findSymbolRange(sym2, paintProps);

        // add the "slash" symbol object
        Symbol sym3 = new Symbol(null, dspClr, combo.getLineWidth(),
                combo.getSizeScale(), combo.isClear(), loc[0], SYMBOL, "SLASH");
        ElementRangeRecord rng3 = findSymbolRange(sym3, paintProps);

        /*
         * Build range
         */
        List<Coordinate> rngBox = new ArrayList<>();
        double minX = Math.min(rng1.getExtent().get(0).x,
                Math.min(rng2.getExtent().get(0).x, rng3.getExtent().get(0).x));
        double maxX = Math.max(rng1.getExtent().get(1).x,
                Math.max(rng2.getExtent().get(1).x, rng3.getExtent().get(1).x));
        double minY = Math.min(rng1.getExtent().get(2).y,
                Math.min(rng2.getExtent().get(2).y, rng3.getExtent().get(2).y));
        double maxY = Math.max(rng1.getExtent().get(0).y,
                Math.max(rng2.getExtent().get(0).y, rng3.getExtent().get(0).y));

        rngBox.add(new Coordinate(minX, maxY));
        rngBox.add(new Coordinate(maxX, maxY));
        rngBox.add(new Coordinate(maxX, minY));
        rngBox.add(new Coordinate(minX, minY));

        List<Coordinate> comboPos = new ArrayList<>();
        comboPos.add(combo.getLocation());

        return new ElementRangeRecord(rngBox, comboPos, false);

    }

    /**
     * Find the visible part of the screen.
     *
     * @param paintProps
     *            The paint properties associated with the target
     * @return A ElementRangeRecord
     */
    public ElementRangeRecord findScreenRange(PaintProperties paintProps) {

        double minx = paintProps.getView().getExtent().getMinX();
        double miny = paintProps.getView().getExtent().getMinY();
        double maxx = paintProps.getView().getExtent().getMaxX();
        double maxy = paintProps.getView().getExtent().getMaxY();

        double dx = Math.abs(maxx - minx);
        double dy = Math.abs(maxy - miny);
        double dd = Math.min(dx, dy);

        double ratio = 0.02;
        double offset = dd * ratio;

        minx += offset;
        miny += offset;
        maxx -= offset;
        maxy -= offset;

        /*
         * Build range
         */
        List<Coordinate> rngBox = new ArrayList<>();
        rngBox.add(new Coordinate(minx, maxy));
        rngBox.add(new Coordinate(maxx, maxy));
        rngBox.add(new Coordinate(maxx, miny));
        rngBox.add(new Coordinate(minx, miny));

        List<Coordinate> pos = new ArrayList<>();
        pos.add(new Coordinate((maxx - minx) / 2, (maxy - miny) / 2));

        return new ElementRangeRecord(rngBox, pos, false);

    }

    /**
     * Find a range box that holds the image of a TCA.
     *
     * @param tca
     *            A tca with associated lat/lon coordinates
     * @param paintProps
     *            The paint properties associated with the target
     * @return A ElementRangeRecord
     */
    public ElementRangeRecord findTcaRangeBox(ITca tca,
            PaintProperties paintProps) {

        List<TropicalCycloneAdvisory> advisories = tca.getAdvisories();
        List<Coordinate> allpts = new ArrayList<>();

        // loop through each advisory.
        for (TropicalCycloneAdvisory tt : advisories) {
            BPGeography segment = tt.getSegment();

            // loop through each path defining the watch/warning segment
            for (Coordinate[] coords : segment.getPaths()) {
                allpts.addAll(Arrays.asList(coords));
            }
        }

        double[][] pixels = DrawingUtil.latlonToPixel(
                allpts.toArray(new Coordinate[allpts.size()]),
                (IMapDescriptor) iDescriptor);
        double[][] smoothpts = pixels;
        Coordinate[] pts = new Coordinate[smoothpts.length];

        for (int ii = 0; ii < smoothpts.length; ii++) {
            pts[ii] = new Coordinate(smoothpts[ii][0], smoothpts[ii][1]);
        }

        return new ElementRangeRecord(pts, false);
    }

    /**
     * Find a range box that holds a vector.
     *
     * @param vect
     *            A vector with associated lat/lon coordinates
     * @param paintProps
     *            The paint properties associated with the target
     * @return A ElementRangeRecord
     */
    public ElementRangeRecord findVectorRangeBox(IVector vect,
            PaintProperties paintProps) {

        List<Coordinate> allpts = new ArrayList<>();

        // Determine scales.
        setScales(paintProps);

        // Find appropriate vector representation
        switch (vect.getVectorType()) {

        case ARROW:
            allpts = findArrowRangePoints(vect);
            break;

        case WIND_BARB:
            allpts = findWindBarbRangePoints(vect);
            break;

        case HASH_MARK:
            allpts = findHashMarkRangePoints(vect);
            break;

        default:
            // Unrecognized vector type; return empty list
        }

        return new ElementRangeRecord(allpts, false);
    }

    /*
     * Find all points that represents an ARROW vector.
     *
     * @param vect A vector with associated lat/lon coordinates
     *
     * @param paintProps The paint properties associated with the target
     *
     * @return List<Coordinate>
     */
    private List<Coordinate> findArrowRangePoints(IVector vect) {

        double sfactor;
        double[] start;
        double[] tmp;
        double angle;
        double speed;
        List<Coordinate> allpts = new ArrayList<>();

        sfactor = deviceScale * vect.getSizeScale();

        tmp = new double[] { vect.getLocation().x, vect.getLocation().y, 0.0 };
        start = iDescriptor.worldToPixel(tmp);

        // calculate the length of the arrow, and its direction
        speed = 10.;
        if (!vect.hasDirectionOnly()) {
            speed = vect.getSpeed();
        }
        double arrowLength = sfactor * speed;
        angle = 90.0 - northOffsetAngle(vect.getLocation())
                + vect.getDirection();

        // find the end point (tip of the arrow)
        double[] end = new double[3];
        end[0] = start[0] + (arrowLength * Math.cos(Math.toRadians(angle)));
        end[1] = start[1] + (arrowLength * Math.sin(Math.toRadians(angle)));

        allpts.add(new Coordinate(start[0], start[1]));
        allpts.add(new Coordinate(end[0], end[1]));

        return allpts;
    }

    /*
     * Find all points that represents an wind barb vector.
     *
     * @param vect A vector with associated lat/lon coordinates
     *
     * @return List<Coordinate>
     */
    private List<Coordinate> findWindBarbRangePoints(IVector vect) {

        double sfactor;
        double[] start;
        double[] tmp;
        double angle;
        List<Coordinate> allpts = new ArrayList<>();

        sfactor = deviceScale * vect.getSizeScale() * 10.;

        /*
         * Convert location from lat/lon coordinates to pixel
         */
        tmp = new double[] { vect.getLocation().x, vect.getLocation().y, 0.0 };
        start = iDescriptor.worldToPixel(tmp);

        /*
         * If calm wind, draw circle
         */
        if (vect.getSpeed() < 0.5) {
            double[][] pts = calculateCircle(start, sfactor * 0.1);
            for (double[] pt : pts) {
                allpts.add(new Coordinate(pt[0], pt[1]));
            }

            return allpts;
        }

        // Compute the number of flags, whole barbs and half barbs needed to
        // represent the wind speed.
        int speedt = (int) Math.floor(vect.getSpeed() + 2.5);
        int numflags = speedt / 50;
        int remainder = speedt % 50;
        int numbarbs = remainder / 10;
        remainder = remainder % 10;
        int halfbarbs = remainder / 5;

        // Maximum number of segments on original size barb
        double maxSegments = 6.0;
        int numsegs = (2 * numflags) + numbarbs + halfbarbs;
        double segmentSpacing = sfactor / maxSegments;
        double windLength = segmentSpacing * Math.max(maxSegments, numsegs);
        double barbLength = sfactor / 3.0;

        // find the end point of the wind barb
        angle = -90.0 - northOffsetAngle(vect.getLocation())
                + vect.getDirection();
        double[] end = new double[3];
        end[0] = start[0] + (windLength * Math.cos(Math.toRadians(angle)));
        end[1] = start[1] + (windLength * Math.sin(Math.toRadians(angle)));
        end[2] = 0.0;

        /*
         * Create a LengthIndexedLine used to reference points along the path at
         * specific distances
         */
        LineString[] ls = toLineString(
                new Coordinate[] { new Coordinate(start[0], start[1]),
                        new Coordinate(end[0], end[1]) });
        LengthIndexedLine lil = new LengthIndexedLine(ls[0]);

        // start from tail end
        double currentLoc = lil.getEndIndex();

        double brbAngle = 70.0;
        double barbAngle = angle + brbAngle;
        if (vect.getLocation().y < 0.0) {
            barbAngle = angle - brbAngle;
        }
        double cosineBarbAngle = Math.cos(Math.toRadians(barbAngle));
        double sineBarbAngle = Math.sin(Math.toRadians(barbAngle));

        /*
         * Process flags
         */
        for (int j = 0; j < numflags; j++) {
            Coordinate[] coords = new Coordinate[4];
            coords[0] = lil.extractPoint(currentLoc);
            coords[1] = lil.extractPoint(currentLoc - segmentSpacing);
            double xtip = coords[1].x + (barbLength * cosineBarbAngle);
            double ytip = coords[1].y + (barbLength * sineBarbAngle);
            coords[2] = new Coordinate(xtip, ytip);
            coords[3] = coords[0];
            allpts.add(coords[0]);
            allpts.add(coords[1]);
            allpts.add(coords[2]);
            currentLoc -= 2 * segmentSpacing;
        }

        /*
         * Process barbs
         */
        for (int j = 0; j < numbarbs; j++) {
            Coordinate[] coords = new Coordinate[2];
            coords[0] = lil.extractPoint(currentLoc);
            double xtip = coords[0].x + (barbLength * cosineBarbAngle);
            double ytip = coords[0].y + (barbLength * sineBarbAngle);
            coords[1] = new Coordinate(xtip, ytip);
            allpts.add(coords[0]);
            allpts.add(coords[1]);
            currentLoc -= segmentSpacing;
        }

        /*
         * Process half barbs
         */
        for (int j = 0; j < halfbarbs; j++) {
            Coordinate[] coords = new Coordinate[2];
            coords[0] = lil.extractPoint(currentLoc);
            double xtip = coords[0].x + (0.5 * barbLength * cosineBarbAngle);
            double ytip = coords[0].y + (0.5 * barbLength * sineBarbAngle);
            coords[1] = new Coordinate(xtip, ytip);
            allpts.add(coords[0]);
            allpts.add(coords[1]);
            currentLoc -= segmentSpacing;
        }

        return allpts;
    }

    /*
     * Find all points that represents a hash mark vector.
     *
     * @param vect A vector with associated lat/lon coordinates
     *
     * @return List<Coordinate>
     */
    private List<Coordinate> findHashMarkRangePoints(IVector vect) {

        double sfactor;
        double[] start;
        double[] tmp;
        double angle;
        List<Coordinate> allpts = new ArrayList<>();

        // scale factor for length of hash lines
        sfactor = 10.0 * deviceScale;

        // scale factor for spacing between hash lines
        double spaceFactor = sfactor * 0.25;

        // distance between hash lines
        double spacing = 1.0 * spaceFactor;
        if (vect.getLineWidth() > 3.0) {
            spacing += (0.25 * spaceFactor * (vect.getLineWidth() - 3));
        }

        // hash line length
        double scaleSize = sfactor * vect.getSizeScale();

        // Convert location from lat/lon coordinates to pixel coordinates
        tmp = new double[] { vect.getLocation().x, vect.getLocation().y, 0.0 };
        start = iDescriptor.worldToPixel(tmp);

        // calculate angle & distance to the four points defining hash mark
        angle = northOffsetAngle(vect.getLocation()) + vect.getDirection();
        double theta = Math.toDegrees(Math.atan(spacing / scaleSize));
        double dist = 0.5
                * Math.sqrt((spacing * spacing) + (scaleSize * scaleSize));

        /*
         * find the X and Y offsets from the center to the end points of hash
         * lines
         */
        double dX1 = dist * Math.cos(Math.toRadians(angle - theta));
        double dY1 = dist * Math.sin(Math.toRadians(angle - theta));

        double dX2 = dist * Math.cos(Math.toRadians(angle + theta));
        double dY2 = dist * Math.sin(Math.toRadians(angle + theta));

        // add both hash mark lines
        allpts.add(new Coordinate(start[0] + dX1, start[1] - dY1));
        allpts.add(new Coordinate(start[0] - dX2, start[1] + dY2));
        allpts.add(new Coordinate(start[0] - dX1, start[1] + dY1));
        allpts.add(new Coordinate(start[0] + dX2, start[1] - dY2));

        return allpts;
    }

}
