/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;

/**
 * Define the base class to represent a drawable element.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public abstract class DrawableElement extends AbstractDrawableComponent
implements IAttribute {

    private ElementRangeRecord range;

    /**
     * Default Constructor
     */
    protected DrawableElement() {
        range = null;
    }

    /**
     * @param range
     * @param elemCategory
     * @param elemType
     */
    protected DrawableElement(Coordinate[] range, String elemCategory,
            String elemType) {
        this.range = new ElementRangeRecord(range, false);
        this.elemCategory = elemCategory;
        this.elemType = elemType;
    }

    /**
     * Gets the range
     *
     * @return range
     */
    public ElementRangeRecord getRange() {
        if (range == null) {
            range = new ElementRangeRecord();
        }
        return range;
    }

    /**
     * Sets the range
     */
    public void setRange(ElementRangeRecord range) {
        this.range = range;
    }

    /**
     * Sets the range - build from an array of points.
     */
    public void createRange(Coordinate[] points, boolean closed) {
        this.range = new ElementRangeRecord(points, closed);
    }

    /**
     * Gets array of colors associated with the object
     *
     * @return Color array
     */
    @Override
    public Color[] getColors() {
        return new Color[1];
    }

    /**
     * Gets the width of the line pattern
     *
     * @return line width
     */
    @Override
    public float getLineWidth() {
        return 0;
    }

    /**
     * Gets the size scale factor for the object
     *
     * @return size scale factor
     */
    @Override
    public double getSizeScale() {
        return 0.0;
    }

    /**
     * Updates the elements' information.
     */
    public abstract void update(IAttribute attr);

    /*
     * setPoints sets the points and may perform other actions, such as snap for
     * jet.
     */
    public void setPoints(List<Coordinate> pts) {
        setPointsOnly(pts);
    }

    public abstract void setPointsOnly(List<Coordinate> pts);

    /**
     * Return an iterator for itself. This is to make sure DEs and DeCollections
     * have the save behavior
     */
    @Override
    public Iterator<DrawableElement> createDEIterator() {
        return new SelfIterator(this);
    }

    /**
     * Return itself
     */
    @Override
    public DrawableElement getPrimaryDE() {
        return this;
    }

    /**
     * return element type
     */
    @Override
    public String getName() {
        return getElemType();
    }

}
