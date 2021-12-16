/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged.ChangeType;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;
import com.raytheon.viz.ui.tools.AbstractModalTool;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.handler.ForecastTrackTool;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;
import gov.noaa.nws.ocp.viz.drawing.command.AddElementCommand;
import gov.noaa.nws.ocp.viz.drawing.command.AddElementsCommand;
import gov.noaa.nws.ocp.viz.drawing.command.DeleteAllCommand;
import gov.noaa.nws.ocp.viz.drawing.command.DeleteElementCommand;
import gov.noaa.nws.ocp.viz.drawing.command.DeleteSelectedElementsCommand;
import gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand;
import gov.noaa.nws.ocp.viz.drawing.command.DrawingCommandManager;
import gov.noaa.nws.ocp.viz.drawing.command.DrawingCommandStackListener;
import gov.noaa.nws.ocp.viz.drawing.command.ReplaceElementCommand;
import gov.noaa.nws.ocp.viz.drawing.command.ReplaceElementsCommand;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * Contains all the ATCF drawable Products, layers, and Elements behind the
 * AtcfResource. Also holds the command manager to undo/redo changes to the data
 * in the product list.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jun 05, 2018 48178       jwu         Initial Creation.
 * Jul 05, 2018 52119       jwu         Added AtcfProduct.
 * Aug 31, 2018 53950       jwu         Added activeSandbox.
 * Oct 02, 2018 55733       jwu         add removeActiveStorm().
 * Feb 05, 2019 58990       jwu         Added support for storm switcher.
 * Feb 19, 2019 60097       jwu         Add methods to storm elements onto specific product/layer.
 * Feb 23, 2019 60613       jwu         Add support for best track options.
 * Mar 18, 2019 61605       jwu         Add best track color option.
 * May 07, 2019 63005       jwu         Add methods to get A-Deck sandbox id.
 * Jun 06, 2019 63375       jwu         Add methods to get F-Deck sandbox id.
 * Jan 25, 2020 71722       jwu         Manage ForecastTrackTool.
 * Mar 13, 2021 90825       jnengel     Fixes stormInResource to not return true for previews.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfResourceData extends AbstractResourceData
        implements DrawingCommandStackListener {

    private List<Product> productList;

    private DrawingCommandManager commandMgr;

    /**
     * Current active product in the drawing layer.
     */
    private Product activeProduct = null;

    /**
     * Current active layer in the drawing layer' active product.
     */
    private Layer activeLayer = null;

    /*
     * This group of fields used for the Autosave and recovery feature
     */
    private boolean needsSaving = false;

    private boolean needsDisplay = false;

    private ArrayList<AtcfResource> rscList = new ArrayList<>();

    /**
     * Constructor
     */
    public AtcfResourceData() {
        super();

        productList = new ArrayList<>();
        commandMgr = new DrawingCommandManager();
        commandMgr.addStackListener(this);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.rsc.AbstractResourceData#construct(com.raytheon
     *      .uf.viz.core.comm.LoadProperties,
     *      com.raytheon.uf.viz.core.drawables.IDescriptor)
     */
    @Override
    public AtcfResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        AtcfResource rsc = new AtcfResource(this, loadProperties);
        rscList.add(rsc);
        return rsc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.AbstractResourceData#update(java.lang.Object
     * )
     */
    @Override
    public void update(Object updateData) {
        // Not used.
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.AbstractResourceData#equals(java.lang.Object
     * )
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        return true;
    }

    /**
     * @return the productList
     */
    public List<Product> getProductList() {
        return productList;
    }

    /**
     * @return the activeProduct
     */
    public Product getActiveProduct() {
        return activeProduct;
    }

    /**
     * @return the activeProduct
     */
    public AtcfProduct getActiveAtcfProduct() {
        return (AtcfProduct) activeProduct;
    }

    /**
     * @param activeProduct
     *            the activeProduct to set
     */
    public void setActiveProduct(Product activeProduct) {
        this.activeProduct = activeProduct;
    }

    /**
     * @return the activeLayer
     */
    public Layer getActiveLayer() {
        return activeLayer;
    }

    /**
     * @param activeLayer
     *            the activeLayer to set
     */
    public void setActiveLayer(Layer activeLayer) {
        this.activeLayer = activeLayer;
    }

    /**
     * @return the commandMgr
     */
    public DrawingCommandManager getCommandMgr() {
        return commandMgr;
    }

    /**
     * @return the activeStorm
     */
    public Storm getActiveStorm() {
        Storm storm = null;
        if (activeProduct != null) {
            storm = getActiveAtcfProduct().getStorm();
        }

        return storm;
    }

    /**
     * @param activeStorm
     *            the activeStorm to set
     */
    public void setActiveStorm(Storm activeStorm) {
        Product stmPrd = null;
        for (Product prd : productList) {
            if (activeStorm == ((AtcfProduct) prd).getStorm()) {
                stmPrd = prd;
                break;
            }
        }

        if (stmPrd == null) {
            stmPrd = new AtcfProduct(activeStorm);
            productList.add(stmPrd);
        }

        activeProduct = stmPrd;
        activeLayer = ((AtcfProduct) activeProduct).getGhostLayer();

        // Update legend to reflect current storm ID.
        refreshLegend();

    }

    /**
     * Refresh the legend for this ATCF Resource.
     */
    public void refreshLegend() {
        // Update legend to reflect current storm ID.
        if (!rscList.isEmpty()) {
            rscList.get(0).issueRefresh();
        }
    }

    /**
     * @param storm
     *            the storm to check
     */
    public boolean stormInResource(Storm storm) {
        AtcfProduct prd = getAtcfProduct(storm);
        return (prd != null && prd.isAccepted());
    }

    /**
     * @param storm
     *            the storm to check
     */
    public AtcfProduct getAtcfProduct(Storm storm) {

        AtcfProduct stormPrd = null;
        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                stormPrd = (AtcfProduct) prd;
                break;
            }
        }

        return stormPrd;
    }

    /**
     * Remove the activeStorm
     */
    public void removeActiveStorm() {
        productList.remove(activeProduct);
    }

    /**
     * 
     * @param Storm
     * @return sandbox ID for the given storm
     */
    public int getBdeckSandbox(Storm storm) {
        int sandboxID = -1;

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                sandboxID = ((AtcfProduct) prd).getBdeckSandboxID();
                break;
            }
        }

        return sandboxID;
    }

    /**
     * @return BDeck sandbox ID for the active storm
     */
    public int getActiveBdeckSandbox() {
        return getBdeckSandbox(getActiveAtcfProduct().getStorm());
    }

    /**
     * @param storm
     *            the storm
     * @param sandbox
     *            the B-Deck sandbox to set for the storm
     */
    public void setBdeckSandbox(Storm storm, int sandboxID) {

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                ((AtcfProduct) prd).setBdeckSandboxID(sandboxID);
                break;
            }
        }
    }

    /**
     * @param activeSandbox
     *            the activeSandbox to set for B-Deck
     */
    public void setActiveBdeckSandbox(int activeSandbox) {
        setBdeckSandbox(getActiveAtcfProduct().getStorm(), activeSandbox);
    }

    /**
     * 
     * @param Storm
     * @return A-deck sandbox ID for the given storm
     */
    public int getAdeckSandbox(Storm storm) {
        int sandboxID = -1;

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                sandboxID = ((AtcfProduct) prd).getAdeckSandboxID();
                break;
            }
        }

        return sandboxID;
    }

    /**
     * @return ADeck sandbox ID for the active storm
     */
    public int getActiveAdeckSandbox() {
        return getAdeckSandbox(getActiveAtcfProduct().getStorm());
    }

    /**
     * @param storm
     *            the storm
     * @param sandbox
     *            the A-Deck sandbox to set for the storm
     */
    public void setAdeckSandbox(Storm storm, int sandboxID) {

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                ((AtcfProduct) prd).setAdeckSandboxID(sandboxID);
                break;
            }
        }
    }

    /**
     * @param activeSandbox
     *            the activeSandbox to set for A-Deck
     */
    public void setActiveAdeckSandbox(int activeSandbox) {
        setAdeckSandbox(getActiveAtcfProduct().getStorm(), activeSandbox);
    }

    /**
     * 
     * @param Storm
     * @return F-deck sandbox ID for the given storm
     */
    public int getFdeckSandbox(Storm storm) {
        int sandboxID = -1;

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                sandboxID = ((AtcfProduct) prd).getFdeckSandboxID();
                break;
            }
        }

        return sandboxID;
    }

    /**
     * @return FDeck sandbox ID for the active storm
     */
    public int getActiveFdeckSandbox() {
        return getFdeckSandbox(getActiveAtcfProduct().getStorm());
    }

    /**
     * @param storm
     *            the storm
     * @param sandbox
     *            the F-Deck sandbox to set for the storm
     */
    public void setFdeckSandbox(Storm storm, int sandboxID) {

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                ((AtcfProduct) prd).setFdeckSandboxID(sandboxID);
                break;
            }
        }
    }

    /**
     * @param activeSandbox
     *            the activeSandbox to set for F-Deck
     */
    public void setActiveFdeckSandbox(int activeSandbox) {
        setFdeckSandbox(getActiveAtcfProduct().getStorm(), activeSandbox);
    }

    /**
     * @param storm
     *            the storm
     * @param sandbox
     *            the forecast track sandbox to set for the storm
     */
    public void setFcstTrackSandbox(Storm storm, int sandboxID) {

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                ((AtcfProduct) prd).setFcstTrackSandboxID(sandboxID);
                break;
            }
        }
    }

    /**
     * 
     * @param Storm
     * @return F-deck sandbox ID for the given storm
     */
    public int getFcstTrackSandbox(Storm storm) {
        int sandboxID = -1;

        for (Product prd : productList) {
            if (storm == ((AtcfProduct) prd).getStorm()) {
                sandboxID = ((AtcfProduct) prd).getFcstTrackSandboxID();
                break;
            }
        }

        return sandboxID;
    }

    /**
     * @return FDeck sandbox ID for the active storm
     */
    public int getActiveFcstTrackSandbox() {
        return getFcstTrackSandbox(getActiveAtcfProduct().getStorm());
    }

    /**
     * @param activeSandbox
     *            the activeSandbox to set for F-Deck
     */
    public void setActiveFcstTrackSandbox(int activeSandbox) {
        setFcstTrackSandbox(getActiveAtcfProduct().getStorm(), activeSandbox);
    }

    /**
     * Add a product to the existing products.
     * 
     * @param prd
     */
    public void addProduct(Product prd) {

        productList.add(prd);

        /*
         * Set active product and layer.
         */
        if (productList.size() == 1) {
            activeProduct = prd;
            if (!prd.getLayers().isEmpty()) {
                activeLayer = prd.getLayer(prd.getLayers().size() - 1);
            }
        }

    }

    /**
     * Get a list of color indexes already used by the storm tracks in this
     * resource.
     * 
     * @return List<Integer>
     */
    public List<Integer> getUsedTrackColorIndexes() {

        List<Integer> indexes = new ArrayList<>();

        for (Product prd : productList) {
            BestTrackProperties prop = ((AtcfProduct) prd)
                    .getBestTrackProperties();
            if (prop != null) {
                indexes.add(prop.getTrackColor());
            }
        }

        return indexes;
    }

    /**
     * Clear all elements on all GHOST layers.
     * 
     */
    public void clearGhostLayers() {
        for (Product prd : productList) {
            if (prd instanceof AtcfProduct) {
                ((AtcfProduct) prd).getGhostLayer().clear();
            }
        }
    }

    /**
     * Initialize product list for the resource.
     */
    public void initializeProducts() {

        // Create a new ATCF product.
        if (productList.isEmpty()) {
            productList.add(new AtcfProduct());
        }

    }

    /**
     * Uses a DrawingCommand to replace one drawable element in the product list
     * with another drawable element.
     * 
     * @param old
     *            Element to replace
     * @param Element
     *            new drawable element
     */
    public void replaceElement(AbstractDrawableComponent old,
            AbstractDrawableComponent newde) {

        /*
         * create a new ReplaceElementCommand and send it to the Command Manager
         */
        DrawingCommand cmd = new ReplaceElementCommand(productList, old, newde);
        commandMgr.addCommand(cmd);

    }

    /**
     * Uses a DrawingCommand to replace a set of drawable element in the active
     * layer with another set of drawable elements. If the parent is not null,
     * old elements will be removed from the parent and new elements will be add
     * in the parent. If the parent is null, the old element list should have
     * same number of elements as the new list has. Loop through each of the
     * elements in the old list, find the parent, remove the old element and add
     * the new element.
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
         * create a new ReplaceElementsCommand and send it to the Command
         * Manager
         */
        if (old == null || old.isEmpty()) {
            parent = activeLayer;
        }

        DrawingCommand cmd = new ReplaceElementsCommand(parent, old, newde);
        commandMgr.addCommand(cmd);

    }

    /**
     * Replace the active product with a new product.
     */
    public void replaceProduct(List<Product> prds) {

        int index = 0;
        if (!productList.isEmpty()) {
            index = productList.indexOf(activeProduct);
            productList.set(index, prds.get(0));
        } else {
            productList.addAll(prds);
        }

        /*
         * Set active product and layer to start product management.
         */
        activeProduct = productList.get(index);
        activeLayer = productList.get(index).getLayer(0);
    }

    /**
     * Uses a DrawingCommand to remove an element from the product list
     * 
     * @param de
     *            Element to be removed
     */
    public void removeElement(AbstractDrawableComponent adc) {

        /*
         * create a new DeleteElementCommand and send it to the Command Manager
         */
        DrawingCommand cmd = new DeleteElementCommand(productList, adc);
        commandMgr.addCommand(cmd);

    }

    /**
     * Deletes all selected elements.
     */
    public void removeElements(List<AbstractDrawableComponent> adc) {

        DrawingCommand cmd = new DeleteSelectedElementsCommand(productList,
                adc);
        commandMgr.addCommand(cmd);

    }

    /**
     * Uses a DrawingCommand to remove all elements from the active layer
     * 
     * @param de
     *            Element to be removed
     */
    public void removeAllActiveDEs() {
        /*
         * create a new DeleteAllSelectedElementsCommand with a list of all
         * elements on the active layer and send it to the Command Manager
         */
        if (activeLayer != null) {
            DrawingCommand cmd = new DeleteSelectedElementsCommand(productList,
                    activeLayer.getDrawables());

            commandMgr.addCommand(cmd);
        }
    }

    /**
     * Uses a DrawingCommand to remove all elements from the product list
     * 
     * @param de
     *            Element to be removed
     */
    public void removeAllProducts() {

        /*
         * create a new DeleteAllCommand and send it to the Command Manager
         */
        DrawingCommand cmd = new DeleteAllCommand(productList);
        commandMgr.addCommand(cmd);

    }

    /**
     * Uses a DrawingCommand to add a DrawableElement to the productList.
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

        DrawingCommand cmd = new AddElementCommand(productList, prd, lyr, de);
        commandMgr.addCommand(cmd);

    }

    /**
     * Uses a DrawingCommand to add a List of DrawableElements to the
     * productList.
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

        DrawingCommand cmd = new AddElementsCommand(productList, prd, lyr,
                elems);
        commandMgr.addCommand(cmd);

    }

    /**
     * Uses a DrawingCommand to add a DrawableElement to the productList.
     * 
     * @param de
     *            The DrawableElement being added.
     */
    public void addElement(AbstractDrawableComponent de) {

        DrawingCommand cmd = new AddElementCommand(productList, activeProduct,
                activeLayer, de);
        commandMgr.addCommand(cmd);

    }

    /**
     * Uses a DrawingCommand to add a List of DrawableElements to the
     * productList.
     * 
     * @param elems
     *            List of DrawableElement being added.
     */
    public void addElements(List<AbstractDrawableComponent> elems) {

        DrawingCommand cmd = new AddElementsCommand(productList, activeProduct,
                activeLayer, elems);
        commandMgr.addCommand(cmd);

    }

    /**
     * If there are no more dataChangedListeners registered with this Data
     * object, clean up command manager stacks and listeners, and determines if
     * data needs to be saved.
     * 
     * @param paneImage
     */
    public synchronized void cleanup() {

        // Close sidebar?
        AtcfSession.getInstance().closeSideBar();

        commandMgr.flushStacks();
        commandMgr.removeStackListener(this);

        deactivateAtcfTools();
    }

    /**
     * De-activates all ATCF tools (called when ATCF resource is removed)
     */
    private void deactivateAtcfTools() {
        AbstractVizPerspectiveManager mgr = VizPerspectiveListener
                .getCurrentPerspectiveManager();
        if (mgr != null) {
            for (AbstractModalTool mt : mgr.getToolManager()
                    .getSelectedModalTools()) {
                if (mt instanceof ForecastTrackTool) {
                    ((ForecastTrackTool) mt).deactivateTool();
                }
            }
        }
    }

    /*
     * (non-Javadoc) Invoked by CommandManager when change has been made to the
     * ProductList
     * 
     * @see
     * gov.noaa.nws.ocp.viz.drawing.command.CommandStackListener#stacksUpdated
     * (int, int)
     */
    @Override
    public void stacksUpdated(int undoSize, int redoSize) {

        if (undoSize + redoSize == 0) {
            return;
        }

        needsSaving = true;
        needsDisplay = true;

        fireChangeListeners(ChangeType.DATA_UPDATE, null);

    }

    /**
     * Return the "needSaving" flag.
     * 
     * @return
     */
    public boolean isNeedsSaving() {
        return needsSaving;
    }

    /**
     * @param save
     */
    public void setNeedsSaving(boolean save) {
        needsSaving = save;
    }

    /**
     * 
     * @return boolean
     */
    public boolean isNeedsDisplay() {
        return needsDisplay;
    }

    /**
     * 
     * @param needsDisplay
     */
    public void setNeedsDisplay(boolean needsDisplay) {
        this.needsDisplay = needsDisplay;
    }
}