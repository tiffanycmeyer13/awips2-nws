/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.IMenuManager;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList.RemoveListener;
import com.raytheon.uf.viz.core.rsc.capabilities.EditableCapability;
import com.raytheon.viz.ui.cmenu.IContextMenuProvider;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.editor.IMultiPaneEditor;
import com.raytheon.viz.ui.input.EditableManager;

import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.rsc.bg.BackgroundManager;
import gov.noaa.nws.ocp.viz.drawing.command.DrawingCommandManager;
import gov.noaa.nws.ocp.viz.drawing.display.AbstractElementContainer;
import gov.noaa.nws.ocp.viz.drawing.display.DisplayElementFactory;
import gov.noaa.nws.ocp.viz.drawing.display.DisplayProperties;
import gov.noaa.nws.ocp.viz.drawing.display.ElementContainerFactory;
import gov.noaa.nws.ocp.viz.drawing.display.IDisplayable;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.Arc;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.DrawableElement;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.MultiPointElement;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;
import gov.noaa.nws.ocp.viz.drawing.elements.SinglePointElement;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.SymbolLocationSet;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;
import gov.noaa.nws.ocp.viz.drawing.elements.tca.TCAElement;
import gov.noaa.nws.ocp.viz.drawing.elements.tca.TropicalCycloneAdvisory;
import gov.noaa.nws.ocp.viz.drawing.elements.tcm.Tcm;

/**
 * Implements a viz resource to draw ATCF data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jun 05, 2018 48178       jwu         Initial Creation.
 * Feb 05, 2019 58990       jwu         Add support for storm switcher.
 * Feb 19, 2019 60097       jwu         Add methods to storm elements onto specific product/layer.
 * Feb 23, 2019 60613       jwu         Add support for best track options.
 * Jan 05, 2020 71722       jwu         Add  AtcfResourceGhost.
 * Feb 22, 2021 87890       dfriedman   Add BackgroundManager.
 *
 * </pre>
 *
 * @author jwu
 * @version 1
 */

public class AtcfResource
        extends AbstractVizResource<AtcfResourceData, MapDescriptor>
        implements RemoveListener, IContextMenuProvider {

    /*
     * List of elements that should be displayed in "selected" mode
     */
    private List<AbstractDrawableComponent> elSelected = null;

    private ConcurrentHashMap<DrawableElement, AbstractElementContainer> displayMap;

    /**
     * Ghost elements.
     */
    private AtcfResourceGhost ghost = null;

    /*
     * selected elements that should be displayed with a marker other than the
     * default gray "DOT"
     */
    private HashMap<AbstractDrawableComponent, Symbol> selectedSymbol = null;

    private List<Integer> ptsSelectedIndex = null;

    private Color ptsSelectedColor = null;

    private static final String RESOURE_NAME = "ATCF Storm";

    private boolean needsDisplay = false;

    private static final int MAX_SELECT_DIST = 30;

    private BackgroundManager backgroundManager;

    /**
     * Default constructor
     */
    protected AtcfResource(AtcfResourceData resourceData,
            LoadProperties loadProperties) {

        super(resourceData, loadProperties);
        getCapability(EditableCapability.class).setEditable(true);

        elSelected = new ArrayList<>();
        selectedSymbol = new HashMap<>();
        displayMap = new ConcurrentHashMap<>();

        backgroundManager = new BackgroundManager(this);
    }

    /**
     * Called when resource is disposed
     *
     * @see com.raytheon.viz.core.rsc.IVizResource#dispose()
     */
    @Override
    public void disposeInternal() {

        /*
         * release IDisplayable resources
         */
        for (AbstractElementContainer disp : displayMap.values()) {
            disp.dispose();
        }
        displayMap.clear();

        if (ghost != null) {
            ghost.dispose();
        }

        backgroundManager.dispose();

    }

    /**
     * Get the coordinate reference cystem
     *
     * @return CoordinateReferenceSystem
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {

        if (descriptor == null) {
            return null;
        }

        return descriptor.getCRS();

    }

    /**
     * Get the resource name.
     *
     * @return String
     */
    @Override
    public String getName() {
        if (resourceData.getActiveStorm() != null) {
            return RESOURE_NAME + " "
                    + resourceData.getActiveStorm().getStormName() + " "
                    + resourceData.getActiveStorm().getStormId();
        } else {
            return RESOURE_NAME;
        }
    }

    /**
     * Gets the AtcfResource's CommandManager
     *
     * @return the commandMgr
     */
    public DrawingCommandManager getCommandMgr() {
        return resourceData.getCommandMgr();
    }

    /**
     * (non-Javadoc)
     *
     * @see com.raytheon.viz.core.rsc.IVizResource#getShortName()
     */
    public String getShortName() {
        return null;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.raytheon.viz.core.rsc.IVizResource#init(com.raytheon.viz.core.
     *      IGraphicsTarget)
     */
    @Override
    public void initInternal(IGraphicsTarget target) throws VizException {
        EditableManager.makeEditable(this,
                getCapability(EditableCapability.class).isEditable());
    }

    /**
     * Draw the elements
     *
     * @param target
     *            IGraphicsTarget
     * @param paintProps
     *            PaintProperties
     */
    @Override
    public void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        IDisplayPaneContainer editor = getResourceContainer();

        // Draw in main editor or side view (IMultiPaneEditor)?
        if (editor instanceof AbstractEditor
                || editor instanceof IMultiPaneEditor) {

            DisplayElementFactory df = new DisplayElementFactory(target,
                    descriptor);

            this.needsDisplay = resourceData.isNeedsDisplay();
            drawProduct(target, paintProps);
            resourceData.setNeedsDisplay(false);

            if (elSelected != null) {
                drawSelected(target, paintProps);
            }

            if (ghost != null) {
                ghost.draw(target, paintProps, df, descriptor);
            }

        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.raytheon.viz.core.rsc.capabilities.IProjectableResource#project(org
     *      .opengis.referencing.crs.CoordinateReferenceSystem)
     */
    @Override
    public void project(CoordinateReferenceSystem mapData) throws VizException {
        for (AbstractElementContainer disp : displayMap.values()) {
            disp.setMapDescriptor(getDescriptor());
        }
    }

    /**
     * Adds a product into the ATCF drawing layer.
     *
     * @param prd
     *            The product being added.
     */
    public void addProduct(Product prd) {
        resourceData.getProductList().add(prd);
    }

    /**
     * Sets the ghost for the ATCF resource.
     *
     * @param ghost
     */
    public void setGhost(AbstractDrawableComponent ghost) {

        if (this.ghost == null) {
            this.ghost = new AtcfResourceGhost();
        } else {
            this.ghost.dispose();
        }

        this.ghost.setGhost(ghost);
    }

    /**
     * Removes the ghost from the ATCF resource.
     */
    public void removeGhost() {

        if (ghost != null) {
            ghost.dispose();
        }

        ghost = null;
    }

    /**
     * Clear all elements on the GHOST layer.
     *
     * @param prd
     *            The product being added.
     */
    public void clearGhostLayers() {
        resourceData.clearGhostLayers();
    }

    /**
     * Finds the nearest element in the products to the input point.
     *
     * @param point
     * @return the nearest element
     */
    public DrawableElement getNearestElement(Coordinate point) {

        DrawableElement nearestElement = null;
        double minDistance = Double.MAX_VALUE;

        Iterator<DrawableElement> iterator = resourceData.getActiveLayer()
                .createDEIterator();
        while (iterator.hasNext()) {
            DrawableElement element = iterator.next();

            double dist = getDistance(element, point);
            if (dist < minDistance) {
                minDistance = dist;
                nearestElement = element;
            }
        }

        if (minDistance < this.getMaxDistToSelect()) {
            return nearestElement;
        } else {
            return null;
        }

    }

    /**
     * Finds the nearest component(DE/DECollection) in the products to the input
     * point.
     *
     * @param point
     * @return the nearest component
     */
    public AbstractDrawableComponent getNearestComponent(Coordinate point) {

        AbstractDrawableComponent nearestComponent = null;
        double minDistance = Double.MAX_VALUE;

        Iterator<AbstractDrawableComponent> iterator = resourceData
                .getActiveLayer().getComponentIterator();

        while (iterator.hasNext()) {
            AbstractDrawableComponent comp = iterator.next();

            double dist = getDistance(comp, point);

            if (dist < minDistance) {
                minDistance = dist;
                nearestComponent = comp;
            }
        }

        if (minDistance < getMaxDistToSelect()) {
            return nearestComponent;
        } else {
            return null;
        }

    }

    /**
     * Draw selected elements
     *
     * @param target
     *            IGraphicsTarget
     * @param paintProps
     *            PaintProperties
     *
     */
    private void drawSelected(IGraphicsTarget target,
            PaintProperties paintProps) {

        if (!elSelected.isEmpty()) {
            DisplayElementFactory df = new DisplayElementFactory(target,
                    descriptor);
            List<IDisplayable> displayEls = new ArrayList<>();
            Map<Symbol, CoordinateList> map = new HashMap<>();

            Symbol defaultSymbol = new Symbol(null,
                    new Color[] { Color.lightGray }, 2.5f, 7.5, false, null,
                    "Marker", "DOT");

            Symbol selectSymbol = new Symbol(null,
                    new Color[] { getPtsSelectedColor() }, 2.5f, 7.5, false,
                    null, "Marker", "DOT");

            CoordinateList defaultPts = new CoordinateList();
            CoordinateList selectPts = new CoordinateList();

            for (AbstractDrawableComponent el : elSelected) {

                if (selectedSymbol.containsKey(el)) {
                    Symbol currSym = selectedSymbol.get(el);
                    Coordinate[] pts = CoordinateArrays
                            .toCoordinateArray(el.getPoints());
                    if (map.containsKey(currSym)) {
                        map.get(currSym).add(pts, true);
                    } else {
                        map.put(currSym, new CoordinateList(pts));
                    }
                } else {
                    for (Coordinate point : el.getPoints()) {
                        int pointIdx = el.getPoints().indexOf(point);
                        if (inSelectedIndex(pointIdx)) {
                            selectPts.add(point, true);
                        } else {
                            defaultPts.add(point, true);
                        }
                    }
                }
            }

            if (!defaultPts.isEmpty()) {
                SymbolLocationSet symset = new SymbolLocationSet(defaultSymbol,
                        defaultPts.toCoordinateArray());
                displayEls.addAll(df.createDisplayElements(symset, paintProps));
            }
            if (!selectPts.isEmpty()) {
                SymbolLocationSet symset = new SymbolLocationSet(selectSymbol,
                        selectPts.toCoordinateArray());
                displayEls.addAll(df.createDisplayElements(symset, paintProps));
            }
            if (!map.isEmpty()) {
                for (Map.Entry<Symbol, CoordinateList> entry : map.entrySet()) {
                    SymbolLocationSet symset = new SymbolLocationSet(
                            entry.getKey(),
                            entry.getValue().toCoordinateArray());
                    displayEls.addAll(
                            df.createDisplayElements(symset, paintProps));
                }
            }

            for (IDisplayable each : displayEls) {
                each.draw(target, paintProps);
                each.dispose();
            }
        }
    }

    /*
     * Check if a point is in the selected index.
     *
     * @param int index for selectd point
     */
    private boolean inSelectedIndex(int pointIdx) {
        if (ptsSelectedIndex != null && !ptsSelectedIndex.isEmpty()) {
            return ptsSelectedIndex.contains(pointIdx);
        }
        return false;
    }

    /**
     * Sets the selected element to the input element.
     *
     * @param element
     */
    public void setSelected(AbstractDrawableComponent comp) {

        elSelected.clear();
        if (comp != null) {
            elSelected.add(comp);
        }
    }

    /**
     * Sets the selected element to the input list.
     *
     * @param adcList
     */
    public void setSelected(List<AbstractDrawableComponent> adcList) {

        elSelected.clear();
        if (adcList != null) {
            elSelected.addAll(adcList);
        }

    }

    /**
     * add an ADC to the selected list.
     *
     * @param adc
     */
    public void addSelected(AbstractDrawableComponent adc) {
        elSelected.add(adc);
    }

    /**
     * add an ADC to the selected list.
     *
     * @param adc
     */
    public void addSelected(List<? extends AbstractDrawableComponent> adcList) {
        elSelected.addAll(adcList);
    }

    /**
     * remove an ADC from the selected list.
     *
     * @param adc
     */
    public void removeSelected(AbstractDrawableComponent adc) {
        if (elSelected.contains(adc)) {
            elSelected.remove(adc);
            removeSelectedSymbol(adc);
        }
    }

    /**
     * Sets the selected element to null.
     */
    public void removeSelected() {

        elSelected.clear();
        clearSelectedSymbol();

        removePtsSelected();
    }

    /**
     * Returns the selected element.
     *
     * @return
     */
    public DrawableElement getSelectedDE() {

        if (elSelected.isEmpty()) {
            return null;
        } else {
            return elSelected.get(0).getPrimaryDE();
        }

    }

    /**
     * Returns the first item(DE or Collection) in the selected list.
     *
     * @return AbstractDrawableComponent
     */
    public AbstractDrawableComponent getSelectedComp() {

        if (elSelected.isEmpty()) {
            return null;
        } else {
            return elSelected.get(0);
        }

    }

    /**
     * returns the list of all selected elements
     *
     * @return List<AbstractDrawableComponent>
     */
    public List<AbstractDrawableComponent> getAllSelected() {
        return elSelected;
    }

    /**
     * Get the product list.
     *
     * @return List<Product>
     */
    public List<Product> getProducts() {

        return resourceData.getProductList();

    }

    /**
     * Replace one drawable element in the product list with another drawable
     * element.
     *
     * @param old
     *            Element to replace
     * @param Element
     *            new drawable element
     */
    public void replaceElement(AbstractDrawableComponent old,
            AbstractDrawableComponent newde) {

        /*
         * displose of resources held by old componenet
         */
        resetADC(old);

        resourceData.replaceElement(old, newde);
    }

    /**
     * Replace a set of drawable element in the active layer with another set of
     * drawable elements.
     *
     * @param old
     *            Elements to replace
     * @param newde
     *            New drawable elements
     */
    public void replaceElements(List<AbstractDrawableComponent> old,
            List<AbstractDrawableComponent> newde) {

        /*
         * release resources held by all the "old" DEs
         */
        for (AbstractDrawableComponent adc : old) {
            resetADC(adc);
        }

        DECollection parent = null;
        if (old != null && !old.isEmpty()) {
            parent = (DECollection) old.get(0).getParent();
        }

        resourceData.replaceElements(parent, old, newde);
    }

    /**
     * Replace a set of drawable element with another set of drawable elements.
     * If the parent is not null, old elements will be removed from the parent
     * and new elements will be add in the parent. If the parent is null, the
     * old element list should have same number of elements as the new list has.
     * Loop through each of the elements in the old list, find the parent,
     * remove the old element and add the new element.
     *
     * @param parent
     *            parent collection of the old elements
     * @param old
     *            Elements to replace
     * @param newde
     *            New drawable elements
     */
    public void replaceElements(DECollection parent,
            List<AbstractDrawableComponent> old,
            List<AbstractDrawableComponent> newde) {
        /*
         * release resources held by all the "old" DEs
         */
        for (AbstractDrawableComponent adc : old) {
            resetADC(adc);
        }

        resourceData.replaceElements(parent, old, newde);
    }

    /**
     * Replace existing products with new products.
     *
     * @param prds
     *            products to replace existing products
     */
    public void replaceProduct(List<Product> prds) {

        for (Layer layer : resourceData.getActiveProduct().getLayers()) {
            resetADC(layer);
        }

        resourceData.replaceProduct(prds);

    }

    /**
     * remove an element from the product list
     *
     * @param de
     *            Element to be removed
     */
    public void removeElement(AbstractDrawableComponent adc) {

        /*
         * reset/dispose elements in the displayMap
         */
        resetADC(adc);

        resourceData.removeElement(adc);

    }

    /**
     * remove all elements from the product list
     *
     * @param de
     *            Element to be removed
     */
    public void removeAllProducts() {

        /*
         * reset/dispose elements in the displayMap
         */
        for (Product prod : resourceData.getProductList()) {
            for (Layer layer : prod.getLayers()) {
                resetADC(layer);
            }
        }

        resourceData.removeAllProducts();

    }

    /**
     * Add a DrawableElement to the productList.
     *
     * @param de
     *            The DrawableElement being added.
     * @param prd
     *            Product the elements to be added into
     * @param lyr
     *            Layer the elements to be added into
     */
    public void addElement(AbstractDrawableComponent de, Product prd,
            Layer lyr) {
        resourceData.addElement(de, prd, lyr);
    }

    /**
     * Add a List of DrawableElements to the productList.
     *
     * @param elems
     *            List of DrawableElement being added.
     * @param prd
     *            Product the elements to be added into
     * @param lyr
     *            Layer the elements to be added into
     */
    public void addElements(List<AbstractDrawableComponent> elems, Product prd,
            Layer lyr) {

        resourceData.addElements(elems, prd, lyr);
    }

    /**
     * add a DrawableElement to the productList.
     *
     * @param de
     *            The DrawableElement being added.
     */
    public void addElement(AbstractDrawableComponent de) {

        resourceData.addElement(de);

    }

    /**
     * add a List of DrawableElements to the productList.
     *
     * @param elems
     *            List of DrawableElement being added.
     */
    public void addElements(List<AbstractDrawableComponent> elems) {

        resourceData.addElements(elems);

    }

    /**
     * Add the selected point
     *
     * @param ptIdx
     *            - index of the selected point
     */
    public void addPtSelected(int ptIdx) {

        if (ptsSelectedIndex == null) {
            ptsSelectedIndex = new ArrayList<>();
        }

        ptsSelectedIndex.add(ptIdx);

    }

    /**
     * Clear the list of the selected points.
     */
    public void removePtsSelected() {
        if (ptsSelectedIndex != null && !ptsSelectedIndex.isEmpty()) {
            ptsSelectedIndex.clear();
        }
    }

    /**
     * Returns the ptsSelectedColor, if it is set. If not, returns RED by
     * default
     *
     * @return color
     */
    public Color getPtsSelectedColor() {
        if (ptsSelectedColor == null) {
            return Color.red;
        } else {
            return ptsSelectedColor;
        }
    }

    /**
     * Sets the color for drawing selected elements
     *
     * @param color
     */
    public void setPtsSelectedColor(Color clr) {
        ptsSelectedColor = clr;
    }

    /**
     * Sets the default color for drawing selected elements
     *
     * @param color
     */
    public void setDefaultPtsSelectedColor() {
        ptsSelectedColor = null;
    }

    /**
     * Selects all elements with the input objType
     *
     * @param objType
     *            - element type
     * @return - total elements selected
     */
    public int selectObj(String objType) {

        int total = 0;
        elSelected.clear();

        Iterator<AbstractDrawableComponent> iterator = resourceData
                .getActiveLayer().getComponentIterator();

        while (iterator.hasNext()) {
            AbstractDrawableComponent element = iterator.next();
            String elType = element.getElemType();
            if (elType != null && elType.equalsIgnoreCase(objType)) {
                elSelected.add(element);
                total++;
            }
        }

        return total;
    }

    /**
     * Deletes all selected elements.
     */
    public void deleteSelectedElements() {

        resourceData.removeElements(elSelected);

    }

    /**
     * @param activeProduct
     *            the activeProduct to set
     */
    public void setActiveProduct(Product activeProduct) {
        resourceData.setActiveProduct(activeProduct);
    }

    /**
     * @return the activeProduct
     */
    public Product getActiveProduct() {
        return resourceData.getActiveProduct();
    }

    /**
     * @param activeLayer
     *            the activeLayer to set
     */
    public void setActiveLayer(Layer activeLayer) {
        resourceData.setActiveLayer(activeLayer);
    }

    /**
     * @return the activeLayer
     */
    public Layer getActiveLayer() {
        return resourceData.getActiveLayer();
    }

    /**
     * remove all elements from the active layer
     */
    public void removeAllActiveDEs() {

        /*
         * release resources held by all DEs in the layer
         */
        if (resourceData != null) {

            if (resourceData.getActiveLayer() != null) {
                resetADC(resourceData.getActiveLayer());
            }

            resourceData.removeAllActiveDEs();
        }

    }

    /**
     * Add a specific marker to use when displaying this element in selected
     * mode
     *
     * @param adc
     *            The selected element
     * @param sym
     *            marker to display
     */
    public void registerSelectedSymbol(AbstractDrawableComponent adc,
            Symbol sym) {
        selectedSymbol.put(adc, sym);
    }

    /**
     * Remove the special marker to use for this selected element.
     *
     * @param adc
     *            the selected element
     */
    public void removeSelectedSymbol(AbstractDrawableComponent adc) {
        selectedSymbol.remove(adc);
    }

    /**
     * remove all elements from the selected element/marker registry
     */
    public void clearSelectedSymbol() {
        selectedSymbol.clear();
    }

    /**
     * Remove a product.
     *
     * @param prd
     */
    public void removeProduct(Product prd) {

        /*
         * Reset/dispose elements in the displayMap
         */
        for (Layer layer : prd.getLayers()) {
            resetADC(layer);
        }

        resourceData.getProductList().remove(prd);

        if (resourceData.getProductList().isEmpty()) {
            resourceData.setActiveProduct(null);
        }
    }

    /**
     * Remove ATCF products that is drawn but not accepted into this resource
     * yet.
     */
    public boolean removeUnacceptedAtcfProduct() {

        boolean removed = false;

        List<Product> unacceptedPrds = new ArrayList<>();
        for (Product prd : resourceData.getProductList()) {
            if (prd instanceof AtcfProduct
                    && !((AtcfProduct) prd).isAccepted()) {
                /*
                 * reset/dispose elements in the displayMap
                 */
                for (Layer layer : prd.getLayers()) {
                    resetADC(layer);
                }

                unacceptedPrds.add(prd);
            }
        }

        if (!unacceptedPrds.isEmpty()) {
            removed = true;
        }

        resourceData.getProductList().removeAll(unacceptedPrds);

        if (resourceData.getProductList().isEmpty()) {
            resourceData.setActiveProduct(null);
        }

        return removed;
    }

    /**
     * Releases the resources held by a DrawableElement
     *
     * @param el
     */
    public void resetElement(DrawableElement el) {

        if (displayMap.containsKey(el)) {
            displayMap.get(el).dispose();
            displayMap.remove(el);
        }
    }

    /**
     * Releases the resources held by a DrawableComponent
     *
     * @param adc
     */
    public void resetADC(AbstractDrawableComponent adc) {

        Iterator<DrawableElement> iterator = adc.createDEIterator();
        while (iterator.hasNext()) {
            DrawableElement el = iterator.next();
            resetElement(el);
        }
    }

    /**
     * Releases the resources held by all DEs in a product.
     *
     * @param prd
     *            Product to be reset.
     */
    public void resetAllElements(Product prd) {
        for (Layer layer : prd.getLayers()) {
            resetADC(layer);
            layer.removeAllElements();
        }
    }

    /**
     * Releases the resources held by all DEs in a layer and remvoe all elements
     * from the layer.
     *
     * @param lyr
     *            layer to be reset.
     */
    public void resetLayer(Layer lyr) {
        resetADC(lyr);
        lyr.removeAllElements();
    }

    /**
     * Releases the resources held by all DEs to refresh all.
     */
    public void resetAllElements() {
        for (Product prd : this.resourceData.getProductList()) {
            for (Layer layer : prd.getLayers()) {
                this.resetADC(layer);
            }
        }
    }

    /**
     * Finds the nearest element in the a DECollection to the input point.
     *
     * @param point
     * @return the nearest element in the collection
     */
    public DrawableElement getNearestElement(Coordinate point,
            DECollection dec) {

        DrawableElement nearestElement = null;
        double minDistance = Double.MAX_VALUE;

        Iterator<DrawableElement> iterator = dec.createDEIterator();
        while (iterator.hasNext()) {
            DrawableElement element = iterator.next();

            double dist = getDistance(element, point);
            if (minDistance < 0 || dist < minDistance) {

                minDistance = dist;
                nearestElement = element;

            }
        }

        return nearestElement;

    }

    /**
     * Finds the nearest Text element in the a DECollection that is close to a
     * specified element (with MaxDistToSelect()/5) in the same DECollection.
     *
     * @param point
     * @param dec
     * @param nearestDe
     * @return the nearest element in the collection
     */
    public DrawableElement getNearestElement(Coordinate point, DECollection dec,
            DrawableElement nearestDe) {

        DrawableElement nearestElement = null;
        double minDistance = Double.MAX_VALUE;
        double distToLine = getDistance(nearestDe, point);

        // Find the closest Text element within "MaxDistToSelect".
        Iterator<DrawableElement> iterator = dec.createDEIterator();
        while (iterator.hasNext()) {
            DrawableElement element = iterator.next();

            if (element instanceof Text) {
                double dist = getDistance(element, point);
                if (dist < this.getMaxDistToSelect() && dist < minDistance) {
                    minDistance = dist;
                    nearestElement = element;
                }
            }
        }

        /*
         * If the closest Text element is not "close" enough to the specified
         * de, return the specified DE.
         */
        if (Math.abs(
                minDistance - distToLine) > (this.getMaxDistToSelect() / 5)) {
            nearestElement = nearestDe;
        }

        return nearestElement;

    }

    /**
     * Invoked by Pane before resource is removed. Checks if changes need to be
     * saved.
     *
     * @param rp
     *            ResoucePair
     */
    @Override
    public void notifyRemove(ResourcePair rp) throws VizException {

        if (rp.getResource() == this) {
            /*
             * this resource is about to be removed, allow resourceData chance
             * to clean up
             */
            resourceData.cleanup();
            backgroundManager.dispose();
        }

    }

    /**
     * Calculate the minimum screen distance from the input location 'loc' to
     * the input DrawableElement(or DECollection) 'adc'. If adc is a
     * MultiPointElement, distances are calculated from the input point to each
     * line segments.
     *
     * @param adc
     * @param loc
     * @return distance
     */
    public double getDistance(AbstractDrawableComponent adc, Coordinate loc) {

        double minDist = Double.MAX_VALUE;

        AbstractEditor mapEditor = AtcfVizUtil.getActiveEditor();
        double[] locScreen = mapEditor.translateInverseClick(loc);

        if (adc instanceof SinglePointElement) {
            double[] pt = mapEditor.translateInverseClick(
                    ((SinglePointElement) adc).getLocation());

            Point ptScreen = new GeometryFactory()
                    .createPoint(new Coordinate(pt[0], pt[1]));
            minDist = ptScreen.distance(new GeometryFactory()
                    .createPoint(new Coordinate(locScreen[0], locScreen[1])));
        } else if (adc instanceof TCAElement) {
            minDist = getTcaDistance((TCAElement) adc, loc);
        } else if (adc instanceof Arc) {
            minDist = getArcDistance((Arc) adc, loc);
        } else if (adc instanceof MultiPointElement) {
            minDist = getMPEDistance((MultiPointElement) adc, loc);
        } else if (adc instanceof DECollection) {
            Iterator<DrawableElement> it = ((DECollection) adc)
                    .createDEIterator();
            while (it.hasNext()) {
                double dist = getDistance(it.next(), loc);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }

        return minDist;
    }

    /*
     * Calculate the minimum screen distance from the input location 'loc' to
     * the input TCAElement.
     *
     * @param tca
     *
     * @param loc
     *
     * @return distance
     */
    private double getTcaDistance(TCAElement tca, Coordinate loc) {

        double minDist = Double.MAX_VALUE;
        double dist;

        for (TropicalCycloneAdvisory advisory : tca.getAdvisories()) {
            for (Coordinate[] coords : advisory.getSegment().getPaths()) {
                for (int ii = 0; ii < coords.length - 1; ii++) {
                    dist = distanceFromLineSegment(loc, coords[ii],
                            coords[ii + 1]);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
            }
        }

        return minDist;
    }

    /*
     * Calculate the minimum screen distance from the input location 'loc' to
     * the input MultiPointElement mpe. Distances are calculated from the input
     * point to each line segments.
     *
     * @param mpe
     *
     * @param loc
     *
     * @return distance
     */
    private double getMPEDistance(MultiPointElement mpe, Coordinate loc) {

        double minDist = Double.MAX_VALUE;

        AbstractEditor mapEditor = AtcfVizUtil.getActiveEditor();
        double[] locScreen = mapEditor.translateInverseClick(loc);

        double dist;

        Object[] pts = mpe.getPoints().toArray();

        for (int ii = 0; ii < pts.length; ii++) {
            if (mpe instanceof Tcm && pts.length == 1) {
                double[] pt = mapEditor
                        .translateInverseClick(mpe.getLinePoints()[0]);
                Point ptScreen = new GeometryFactory()
                        .createPoint(new Coordinate(pt[0], pt[1]));
                minDist = ptScreen.distance(new GeometryFactory().createPoint(
                        new Coordinate(locScreen[0], locScreen[1])));
            } else {
                if (ii == pts.length - 1) {
                    if (mpe instanceof Line && ((Line) mpe).isClosedLine()) {
                        dist = distanceFromLineSegment(loc,
                                (Coordinate) pts[ii], (Coordinate) pts[0]);
                    } else {
                        break;
                    }
                } else {
                    dist = distanceFromLineSegment(loc, (Coordinate) pts[ii],
                            (Coordinate) pts[ii + 1]);
                }

                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }

        return minDist;
    }

    /*
     * Calculate the minimum screen distance from the input location 'loc' to
     * the input Arc element.
     *
     * @param adc
     *
     * @param loc
     *
     * @return distance
     */
    private double getArcDistance(Arc arc, Coordinate loc) {

        double minDist;

        AbstractEditor mapEditor = AtcfVizUtil.getActiveEditor();
        double[] locScreen = mapEditor.translateInverseClick(loc);

        double[] center = mapEditor.translateInverseClick(arc.getCenterPoint());
        double[] circum = mapEditor
                .translateInverseClick(arc.getCircumferencePoint());

        Point ctrScreen = new GeometryFactory()
                .createPoint(new Coordinate(center[0], center[1]));
        minDist = ctrScreen.distance(new GeometryFactory()
                .createPoint(new Coordinate(locScreen[0], locScreen[1])));

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

        double angle = arc.getStartAngle();
        int numpts = (int) Math
                .round(arc.getEndAngle() - arc.getStartAngle() + 1.0);

        for (int j = 0; j < numpts; j++) {
            double thisSine = Math.sin(Math.toRadians(angle));
            double thisCosine = Math.cos(Math.toRadians(angle));

            double xx = center[0] + (major * cosineAxis * thisCosine)
                    - (minor * sineAxis * thisSine);
            double yy = center[1] + (major * sineAxis * thisCosine)
                    + (minor * cosineAxis * thisSine);

            Point ptScreen = new GeometryFactory()
                    .createPoint(new Coordinate(xx, yy));
            double dist = ptScreen.distance(new GeometryFactory()
                    .createPoint(new Coordinate(locScreen[0], locScreen[1])));
            if (dist < minDist) {
                minDist = dist;
            }

            angle += 1.0;
        }

        return minDist;
    }

    /**
     * Calculate SCREEN distance from an input point to a line segment The
     * coordinate of the point and line are lat/lon.
     *
     * @param loc
     *            - input point
     * @param startPt
     *            - start point of the line segment
     * @param endPt
     *            - end point of the line segment
     * @return
     */
    public static double distanceFromLineSegment(Coordinate loc,
            Coordinate startPt, Coordinate endPt) {

        AbstractEditor mapEditor = AtcfVizUtil.getActiveEditor();
        double[] locScreen = mapEditor.translateInverseClick(loc);

        double[] pt1 = mapEditor.translateInverseClick(startPt);
        double[] pt2 = mapEditor.translateInverseClick(endPt);
        LineSegment seg = new LineSegment(new Coordinate(pt1[0], pt1[1]),
                new Coordinate(pt2[0], pt2[1]));

        return seg.distance(new Coordinate(locScreen[0], locScreen[1]));
    }

    /**
     * Loops through all products in the drawing layer to draw display elements.
     *
     * The drawing sequence will be:
     *
     * First draw all filled elements starting from those in the non-active
     * products, then those in non-active layers in active product, finally
     * those in active layer.
     *
     * Then draw non-filled elements, starting from those in the non-active
     * products, then those in non-active layers in active product, finally
     * those in active layer. However, for each layer, if there are text
     * elements in a layer, the text elements will be the last ones to be drawn
     * so they could remain on the top.
     *
     * @param target
     *            Graphic target from the paint() method.
     * @param paintProps
     *            Paint properties from the paint() method.
     */
    private void drawProduct(IGraphicsTarget target,
            PaintProperties paintProps) {

        if (resourceData.getActiveProduct() != null) {
            drawFilledElements(target, paintProps);

            drawNonFilledElements(target, paintProps);
        }
    }

    /**
     * Loops through all products in the drawing layer to draw all filled
     * elements
     *
     * @param target
     *            Graphic target from the paint() method.
     *
     * @param paintProps
     *            Paint properties from the paint() method.
     */
    private void drawFilledElements(IGraphicsTarget target,
            PaintProperties paintProps) {

        // Draw filled elements in the non-active products
        for (Product prod : resourceData.getProductList()) {
            if (prod.isOnOff() && prod != resourceData.getActiveProduct()) {
                for (Layer layer : prod.getLayers()) {
                    drawFilledElements(layer, target, paintProps,
                            prod.isOnOff());
                }
            }
        }

        if (resourceData.getActiveProduct().isOnOff()) {
            // Draw filled elements in the active product's non-active layers
            for (Layer layer : resourceData.getActiveProduct().getLayers()) {
                if (layer != resourceData.getActiveLayer()) {
                    drawFilledElements(layer, target, paintProps, false);
                }
            }

            // Draw filled elements in the active layer
            drawFilledElements(resourceData.getActiveLayer(), target,
                    paintProps, true);
        }

    }

    /*
     * Draw all filled elements in a given layer.
     *
     * @param layer Layer to be drawn.
     *
     * @param target Graphic target from the paint() method.
     *
     * @param paintProps Paint properties from the paint() method.
     *
     * @param displayProduct flag to display this product regardless of layers.
     */
    private void drawFilledElements(Layer layer, IGraphicsTarget target,
            PaintProperties paintProps, boolean displayProduct) {

        /*
         * The layer will be displayed if the displayProduct flag is on or the
         * layer display flag is on or this layer is the active layer.
         */
        if (layer != null && (displayProduct || layer.isOnOff()
                || layer == resourceData.getActiveLayer())) {

            DisplayProperties dprops = new DisplayProperties();

            if (layer != resourceData.getActiveLayer()) {
                dprops.setLayerMonoColor(layer.isMonoColor());
                dprops.setLayerColor(layer.getColor());
                dprops.setLayerFilled(layer.isFilled());
            } else {
                /*
                 * "Filled" should always be set to "true" for displaying the
                 * active layer.
                 */
                dprops.setLayerFilled(true);
            }

            Iterator<DrawableElement> iterator = layer.createDEIterator();
            while (iterator.hasNext()) {
                DrawableElement el = iterator.next();

                if (el instanceof MultiPointElement
                        && ((MultiPointElement) el).getFilled()) {
                    drawElement(el, target, paintProps, dprops);
                }
            }
        }
    }

    /*
     * Loops through all products in the PGEN drawing layer to draw all
     * non-filled elements
     *
     * @param target Graphic target from the paint() method.
     *
     * @param paintProps Paint properties from the paint() method.
     */
    private void drawNonFilledElements(IGraphicsTarget target,
            PaintProperties paintProps) {

        // Draw non-filled elements in the non-active products
        for (Product prod : resourceData.getProductList()) {
            if (prod.isOnOff() && prod != resourceData.getActiveProduct()) {
                for (Layer layer : prod.getLayers()) {
                    drawNonFilledElements(layer, target, paintProps,
                            prod.isOnOff());
                }
            }
        }

        if (resourceData.getActiveProduct().isOnOff()) {
            // Draw non-filled elements in the active product's non-active
            // layers
            for (Layer layer : resourceData.getActiveProduct().getLayers()) {
                if (layer != resourceData.getActiveLayer()) {
                    drawNonFilledElements(layer, target, paintProps, false);
                }
            }

            // Draw non-filled elements in the active layer
            drawNonFilledElements(resourceData.getActiveLayer(), target,
                    paintProps, true);
        }

    }

    /*
     * Draw all non-filled elements in a layer.
     *
     * Non-text elements will drawn FIRST, then text elements - so text elements
     * will be displayed on top of other element.
     *
     * @param layer a PGEN layer to be drawn
     *
     * @param target Graphic target from the paint() method.
     *
     * @param paintProps Paint properties from the paint() method.
     *
     * @param displayProduct flag to display this layer regardless of layers.
     */
    private void drawNonFilledElements(Layer layer, IGraphicsTarget target,
            PaintProperties paintProps, boolean displayProduct) {

        /*
         * The layer will be displayed if the displayProduct flag is on or the
         * layer display flag is on or this layer is the active layer.
         */
        if (layer != null && (displayProduct || layer.isOnOff()
                || layer == resourceData.getActiveLayer())) {

            DisplayProperties dprops = new DisplayProperties();
            if (layer != resourceData.getActiveLayer()) {
                dprops.setLayerMonoColor(layer.isMonoColor());
                dprops.setLayerColor(layer.getColor());
                dprops.setLayerFilled(layer.isFilled());
            }

            Iterator<DrawableElement> iterator = layer.createDEIterator();

            List<DrawableElement> filledElements = new ArrayList<>();
            List<DrawableElement> nonCcfpTextElements = new ArrayList<>();
            List<DrawableElement> otherElements = new ArrayList<>();

            while (iterator.hasNext()) {
                DrawableElement el = iterator.next();

                if (el instanceof Text) {
                    nonCcfpTextElements.add(el);
                } else if (el instanceof MultiPointElement) {
                    if (((MultiPointElement) el).getFilled()) {
                        filledElements.add(el);
                    } else {
                        otherElements.add(el);
                    }
                } else {
                    otherElements.add(el);
                }
            }

            drawElements(otherElements, target, paintProps, dprops);

            drawElements(nonCcfpTextElements, target, paintProps, dprops);
        }

    }

    /*
     * Loops through all elements in a list to draw display elements.
     *
     * @param elements List of elements to be drawn
     *
     * @param target Graphic target from the paint() method.
     *
     * @param paintProps Paint properties from the paint() method.
     *
     * @param dispProps Display properties from the drawLayer() method.
     */
    private void drawElements(List<DrawableElement> elements,
            IGraphicsTarget target, PaintProperties paintProps,
            DisplayProperties dispProps) {

        for (DrawableElement de : elements) {
            drawElement(de, target, paintProps, dispProps);
        }
    }

    /*
     * Draw an element.
     *
     * @param de Element to be drawn
     *
     * @param target Graphic target from the paint() method.
     *
     * @param paintProps Paint properties from the paint() method.
     *
     * @param dispProps Display properties from the drawLayer() method.
     */
    private void drawElement(DrawableElement de, IGraphicsTarget target,
            PaintProperties paintProps, DisplayProperties dispProps) {
        if (!displayMap.containsKey(de)) {
            AbstractElementContainer container = ElementContainerFactory
                    .createContainer(de, descriptor, target);
            displayMap.put(de, container);
        }

        displayMap.get(de).draw(target, paintProps, dispProps);
    }

    /**
     * Check if the resource is currently editable
     *
     * @return editable
     */
    public boolean isEditable() {
        return getCapability(EditableCapability.class).isEditable();
    }

    public void setEditable(boolean enable) {
        getCapability(EditableCapability.class).setEditable(enable);
        EditableManager.makeEditable(this,
                getCapability(EditableCapability.class).isEditable());
    }

    /**
     * Finds the ATCF resource and fill the context menu.
     */
    @Override
    public void provideContextMenuItems(IMenuManager menuManager, int x,
            int y) {

        ResourcePair atcfPair = null;
        for (ResourcePair rp : descriptor.getResourceList()) {
            if (rp.getResource() == this) {
                atcfPair = rp;
                break;
            }
        }

        if (atcfPair != null && atcfPair.getResource() != null) {
            // TODO Not implemented yet;
        }
    }

    /**
     * Get a list of DrawableElements in current ATCF activity that need to be
     * displayed.
     */
    public List<DrawableElement> getActiveDrawableElements() {

        List<DrawableElement> des = new ArrayList<>();

        for (Layer layer : resourceData.getActiveProduct().getLayers()) {
            if (layer != null && layer.isOnOff()) {

                Iterator<DrawableElement> iterator = layer.createDEIterator();

                while (iterator.hasNext()) {
                    des.add(iterator.next());
                }
            }
        }

        return des;
    }

    /**
     * @return the needsDisplay
     */
    public boolean isNeedsDisplay() {
        return needsDisplay;
    }

    /**
     * @param needsDisplay
     *            the needsDisplay to set
     */
    public void setNeedsDisplay(boolean needsDisplay) {
        this.needsDisplay = needsDisplay;
    }

    /**
     * Get the maximum distance for selection.
     *
     * @return maximum distance for selection
     */
    public int getMaxDistToSelect() {
        return MAX_SELECT_DIST;
    }

    public BackgroundManager getBackgroundManager() {
        return backgroundManager;
    }

}
