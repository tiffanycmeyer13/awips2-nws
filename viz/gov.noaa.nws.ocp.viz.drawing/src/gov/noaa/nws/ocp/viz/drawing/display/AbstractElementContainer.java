/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

import gov.noaa.nws.ocp.viz.drawing.elements.DrawableElement;
import gov.noaa.nws.ocp.viz.drawing.elements.ElementRangeRecord;
import gov.noaa.nws.ocp.viz.drawing.elements.tca.ITca;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.ITcm;
import gov.noaa.nws.ocp.viz.drawing.utils.DrawingUtil;

/**
 * This Element Container is the base class for all Element Containers. It's
 * function is to hold a DrawableElement along with associated renderable
 * objects that depict the DrawableElement on the graphics target.
 *
 * Subclasses' implementation of the draw method should determine when the
 * IDisplayables for the Drawable Element should be recreated. IDisplayables can
 * be created using the createDisplayables() method.
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
public abstract class AbstractElementContainer {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(AbstractElementContainer.class);

    /*
     * The Drawable Element to be rendered.
     */
    protected DrawableElement element;

    protected IMapDescriptor mapDescriptor;

    protected IGraphicsTarget target;

    protected DisplayElementFactory def;

    // Range offset in screen pixels.
    protected static final double RANGE_OFFSET = 10;

    /*
     * Objects that can be rendered on the graphics target depicting the
     * DrawableElement.
     */
    protected List<IDisplayable> displayEls = null;

    /**
     * @param element
     * @param mapDescriptor
     */
    protected AbstractElementContainer(DrawableElement element,
            IMapDescriptor mapDescriptor, IGraphicsTarget target) {
        this.element = element;
        this.mapDescriptor = mapDescriptor;
        this.target = target;
        def = new DisplayElementFactory(target, mapDescriptor);
    }

    /**
     * Sets a new mapDescriptor. All IDisplayables will be recreated.
     *
     * @param mapDescriptor
     *            the mapDescriptor to set
     */
    public void setMapDescriptor(IMapDescriptor mapDescriptor) {
        this.mapDescriptor = mapDescriptor;
        def = new DisplayElementFactory(target, mapDescriptor);
        dispose();
        displayEls = null;
    }

    /**
     * Draws to the given graphics target. Recreates the IDisplayable objects,
     * if necessary.
     *
     * @param target
     * @param paintProps
     * @param dprops
     *            Layer properties
     */
    public abstract void draw(IGraphicsTarget target,
            PaintProperties paintProps, DisplayProperties dprops);

    /**
     * Draws to the given graphics target. Recreates the IDisplayable objects,
     * if necessary.
     *
     * @param target
     * @param paintProps
     * @param dprops
     *            Layer properties
     * @param needsCreate
     */
    public abstract void draw(IGraphicsTarget target,
            PaintProperties paintProps, DisplayProperties dprops,
            boolean needsCreate);

    /**
     * Uses a DisplayElementFactory to create IDisplayable objects from the
     * Drawable Element
     *
     * @param paintProps
     */
    protected void createDisplayables(PaintProperties paintProps) {

        // Cleanup first
        if ((displayEls != null) && !displayEls.isEmpty()) {
            reset();
        }

        // Set range for this element.
        setRange(element, paintProps);

        // Create displayables
        if (element instanceof IText) {
            displayEls = def.createDisplayElements((IText) element, paintProps);
        } else if (element instanceof IVector) {
            displayEls = def.createDisplayElements((IVector) element,
                    paintProps);
        } else if (element instanceof ICombo) {
            displayEls = def.createDisplayElements((ICombo) element,
                    paintProps);
        } else if (element instanceof ITca) {
            displayEls = def.createDisplayElements((ITca) element, paintProps);
        } else if (element instanceof ISymbol) {
            displayEls = def.createDisplayElements((ISymbol) element,
                    paintProps);
        } else if (element instanceof ITcm) {
            displayEls = def.createDisplayElements((ITcm) element, paintProps);
        } else if (element instanceof IMultiPoint) {
            if (element instanceof IKink) {
                displayEls = def.createDisplayElements((IKink) element,
                        paintProps);
            } else if (element instanceof IArc) {
                displayEls = def.createDisplayElements((IArc) element,
                        paintProps);
            } else if (element instanceof ILine) {
                displayEls = def.createDisplayElements((ILine) element,
                        paintProps, true);
            }
        }
    }

    /*
     * Set an DrawbleElement's range record
     */
    private void setRange(DrawableElement elem, PaintProperties paintProps) {
        setRange(elem, mapDescriptor, paintProps);
    }

    /*
     * Set a text element's range record
     *
     * @param elem
     *
     * @param mapDescriptor2
     *
     * @param paintProps
     */
    private void setRange(DrawableElement elem, IMapDescriptor mapDescriptor2,
            PaintProperties paintProps) {
        if (elem instanceof ITca) {
            elem.setRange(def.findTcaRangeBox((ITca) elem, paintProps));
        } else if (elem instanceof IVector) {
            elem.setRange(def.findVectorRangeBox((IVector) elem, paintProps));
        } else if (elem instanceof ISinglePoint) {
            ElementRangeRecord rng = null;
            if (elem instanceof IText) {
                rng = def.findTextBoxRange((IText) elem, paintProps);
            } else if (elem instanceof ISymbol) {
                rng = def.findSymbolRange((ISymbol) elem, paintProps);
            } else if (elem instanceof ICombo) {
                rng = def.findComboSymbolRange((ICombo) elem, paintProps);
            }

            if (rng != null) {
                elem.setRange(rng);
            }
        } else if (elem instanceof IMultiPoint) {
            setMultiPointElemRange(elem, mapDescriptor2);
        } else {
            handler.warn(
                    "Invalid DrawableElement type. No range record is set!");
        }
    }

    /*
     * Set a multi-point element's range record
     *
     * @param elem
     *
     * @param mapDescriptor2
     */
    private void setMultiPointElemRange(DrawableElement elem,
            IMapDescriptor mapDescriptor2) {
        if (elem instanceof IMultiPoint) {
            double[][] pixels = DrawingUtil.latlonToPixel(
                    ((IMultiPoint) elem).getLinePoints(), mapDescriptor2);
            double[][] smoothpts = pixels;
            float density;
            /*
             * Apply parametric smoothing on pixel coordinates, if required.
             *
             * Note: 1. NMAP2 range calculation does not do smoothing though. 2.
             * Tcm is IMultiPoint but not ILine.
             */
            if (elem instanceof ILine && ((ILine) elem).getSmoothFactor() > 0) {
                float devScale = 50.0f;
                if (((ILine) elem).getSmoothFactor() == 1) {
                    density = devScale / 1.0f;
                } else {
                    density = devScale / 5.0f;
                }

                smoothpts = CurveFitter.fitParametricCurve(pixels, density);
            }

            Coordinate[] pts = new Coordinate[smoothpts.length];

            for (int ii = 0; ii < smoothpts.length; ii++) {
                pts[ii] = new Coordinate(smoothpts[ii][0], smoothpts[ii][1]);
            }

            boolean closed = false;
            if (elem instanceof ILine) {
                closed = ((ILine) elem).isClosedLine();
            }

            elem.createRange(pts, closed);
        }
    }

    // Reset
    private void reset() {
        def.reset();
    }

    /**
     * Releases the resources held by any of the IDisplayables
     */
    public void dispose() {

        if (displayEls == null) {
            return;
        }

        for (IDisplayable each : displayEls) {
            each.dispose();
        }
        displayEls.clear();
    }

    public void setElement(DrawableElement el) {
        this.element = el;
    }

}
