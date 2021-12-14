/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

/**
 * Abstract super class for DrawableElement and DECollection
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author B. Yin
 * @version 1.0
 */
public abstract class AbstractDrawableComponent {

    protected String elemCategory;

    protected String elemType;

    protected AbstractDrawableComponent parent;

    protected Calendar startTime;

    protected Calendar endtime;

    public abstract void setColors(Color[] colors);

    public abstract List<Coordinate> getPoints();

    public abstract Iterator<DrawableElement> createDEIterator();

    public abstract AbstractDrawableComponent copy();

    public abstract DrawableElement getPrimaryDE();

    public abstract String getName();

    /**
     * @return the elemCategory
     */
    public String getElemCategory() {
        return elemCategory;
    }

    /**
     * @param elemCategory
     *            the elemCategory to set
     */
    public void setElemCategory(String elemCategory) {
        this.elemCategory = elemCategory;
    }

    /**
     * @return the elemType
     */
    public String getElemType() {
        return elemType;
    }

    /**
     * @param elemType
     *            the elemType to set
     */
    public void setElemType(String elemType) {
        this.elemType = elemType;
    }

    /**
     *
     * @return AbstractDrawableComponent
     */
    public AbstractDrawableComponent getParent() {
        return parent;
    }

    /**
     * Is this object a DECollection with internal name "labeledSymbol". Such
     * collections are used for implementing labeled symbols. The parent
     * DrawableElement will have that name and the symbol information. The child
     * element, a Text object will contain the label
     *
     * @return boolean
     */

    public boolean isLabeledSymbolType() {
        boolean isLabeledSymbol = false;

        if (parent == null) {
            return false;
        }

        if (parent.getName() == null) {
            return false;
        }

        if (parent.getName()
                .equalsIgnoreCase(ElementConstants.LABELED_SYMBOL)) {
            isLabeledSymbol = true;
        }

        return isLabeledSymbol;
    }

    /**
     * If this object is DEColleciton uses as a labeled symbol, get the label
     *
     * @return String[] the label of a labeled symbol
     */
    public String[] getSymbolLabel() {
        String[] label = null;

        if (!this.isLabeledSymbolType()) {
            return new String[] {};
        }

        Iterator<AbstractDrawableComponent> it = ((DECollection) this.parent)
                .getComponentIterator();

        while (it.hasNext()) {
            AbstractDrawableComponent item = it.next();
            if (item instanceof Text) {
                label = ((Text) item).getString();
                break;
            }
        }

        return label;

    }

    /**
     * Determine if this is a DEColleciton set up to house a labeled symbol and
     * if there is actually a symbol in this DECollection
     *
     * @return boolean
     */
    public boolean isASymbolAndHasALabel() {

        if (!this.isLabeledSymbolType()) {
            return false;
        }

        return (getSymbolLabel().length > 0);
    }

    /**
     *
     * @param parent
     */
    public void setParent(AbstractDrawableComponent parent) {
        this.parent = parent;
    }

    /**
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     *
     * @return String
     */
    public String getForecastHours() {
        return "";
    }

    /**
     *
     * @return Calendar
     */
    public Calendar getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime
     */
    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    /**
     *
     * @return Calendar
     */
    public Calendar getEndtime() {
        return endtime;
    }

    /**
     *
     * @param endtime
     */
    public void setEndtime(Calendar endtime) {
        this.endtime = endtime;
    }
}
