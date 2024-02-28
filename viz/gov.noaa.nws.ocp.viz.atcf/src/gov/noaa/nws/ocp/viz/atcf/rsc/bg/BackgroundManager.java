/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc.bg;

import java.awt.Color;
import java.util.IdentityHashMap;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.colormap.ColorMap;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.maps.rsc.MapResourceGroupData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged.ChangeType;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.uf.viz.core.rsc.ResourceList.AddListener;
import com.raytheon.uf.viz.core.rsc.ResourceList.RemoveListener;
import com.raytheon.uf.viz.core.rsc.ResourceProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ShadeableCapability;
import com.raytheon.uf.viz.topo.TopoResourceData;
import com.raytheon.viz.ui.color.BackgroundColor;
import com.raytheon.viz.ui.color.IBackgroundColorChangedListener;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionNames;
import gov.noaa.nws.ocp.common.atcf.configuration.IColorConfigurationChangedListener;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;

/**
 * Manages switching between the default CAVE background and ATCF colored
 * background.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 22, 2021 87890       dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 */
public class BackgroundManager implements IColorConfigurationChangedListener,
        AddListener, RemoveListener, IBackgroundColorChangedListener {

    /**
     * Display name for the ATCF background layer resource.
     */
    protected static final String RESOURCE_MAP_NAME = "ATCF Background";

    protected static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BackgroundManager.class);

    public enum Mode {
        CAVE_DEFAULT("CAVE Default"), SHAPEFILE("Shapefile"), TOPO("Topo");

        private Mode(String label) {
            this.label = label;
        }

        private String label;

        public String getLabel() {
            return label;
        }

        protected void setLabel(String label) {
            this.label = label;
        }

    }

    private Mode mode = Mode.CAVE_DEFAULT;

    /**
     * Selected ocean color. There is currently no state for this elsewhere in
     * the ATCF software, so this is the "source of truth".
     */
    private ColorSelectionNames oceanColorSelection = ColorSelectionNames.OCEAN;

    private boolean autoAdjustResourceColors = false;

    /**
     * "Contrast" threshold, measured in color cube distance from the background
     * colors, below which resource colors should be adjusted.
     */
    private float contrastThreshold = 1 / 3f;

    /**
     * Reference to a CAVE BackgroundColor control object.
     */
    private BackgroundColor backgroundColor;

    private RGB originalBackgroundColor = null;

    private AtcfResource resource;

    /** Original resource color and associated resource change listener. */
    private IdentityHashMap<AbstractVizResource<?, ?>, Pair<RGB, IResourceDataChanged>> backupResourceColors = new IdentityHashMap<>();

    private BackgroundManagerDialog dialog;

    private ResourceList monitoredResourecList = null;

    private ResourcePair shapeRP = null;

    private ResourcePair topoRP = null;

    public BackgroundManager(AtcfResource resource) {
        this.resource = resource;
        AtcfConfigurationManager.getInstance()
                .addColorConfigurationChangeListener(this);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (mode == this.mode) {
            return;
        }

        this.mode = mode;
        switch (mode) {
        case SHAPEFILE:
            removeRPIfNeeded(getTopoRP(false));
            addRPIfNeeded(getShapefileRP(true));
            setDisplayColor(getAtcfColor(oceanColorSelection));
            break;
        case TOPO:
            removeRPIfNeeded(getShapefileRP(false));
            addRPIfNeeded(getTopoRP(true));
            setDisplayColor(getAtcfColor(oceanColorSelection));
            break;
        case CAVE_DEFAULT:
            removeRPIfNeeded(getShapefileRP(false));
            removeRPIfNeeded(getTopoRP(false));
            setDisplayColor(originalBackgroundColor);
            break;
        }
        realizeResourceColors();

        if (dialog != null) {
            dialog.realizeState();
        }
    }

    public ColorSelectionNames getOceanColorSelection() {
        return oceanColorSelection;
    }

    public void setOceanColorSelection(
            ColorSelectionNames oceanColorSelection) {
        this.oceanColorSelection = oceanColorSelection;
        if (mode != Mode.CAVE_DEFAULT) {
            setDisplayColor(getAtcfColor(oceanColorSelection));
        }
        realizeResourceColors();
    }

    public boolean isAutoAdjustResourceColors() {
        return autoAdjustResourceColors;
    }

    public void setAutoAdjustResourceColors(boolean autoAdjustResourceColors) {
        this.autoAdjustResourceColors = autoAdjustResourceColors;
        realizeResourceColors();
    }

    public float getContrastThreshold() {
        return contrastThreshold;
    }

    public void setContrastThreshold(float contrastThreshold) {
        this.contrastThreshold = contrastThreshold;
        if (mode != Mode.CAVE_DEFAULT) {
            realizeResourceColors();
        }
        if (dialog != null) {
            dialog.realizeState();
        }
    }

    public RGB getOriginalBackgroundColor() {
        return originalBackgroundColor;
    }

    public void showDialog() {
        if (dialog == null || dialog.getShell() == null
                || dialog.getShell().isDisposed()) {
            dialog = new BackgroundManagerDialog(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), this);
        }
        dialog.open();
    }

    /**
     * Should be called when associated AtcfResource is disposed. Restores
     * original colors and removes the ATCF background layer.
     */
    public void dispose() {
        AtcfConfigurationManager.getInstance()
                .removeColorConfigurationChangeListener(this);
        if (backgroundColor != null) {
            backgroundColor.removeListener(BGColorMode.EDITOR, this);
        }
        if (mode != Mode.CAVE_DEFAULT) {
            setMode(Mode.CAVE_DEFAULT);
        }
        if (dialog != null) {
            dialog.close();
            dialog = null;
        }
    }

    /* package */ static RGB getAtcfColor(ColorSelectionNames colorName) {
        int i = AtcfConfigurationManager.getInstance().getAtcfColorSelections()
                .getMapColorSelection(colorName).getColorIndex()[0];
        Color c = AtcfConfigurationManager.getInstance().getAtcfCustomColors()
                .getColor(i);

        // Default background and coast line colors.
        if (c == null) {
            c = (colorName == ColorSelectionNames.COASTLINES) ? Color.WHITE
                    : Color.BLACK;
        }

        return new RGB(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Set the CAVE background color for the IRenderableDisplay associated with
     * the AtcfResource based on the current settings.
     *
     * @param color
     */
    protected void setDisplayColor(RGB color) {
        if (resource != null && color != null) {
            IDisplayPaneContainer container = resource.getResourceContainer();
            if (container != null) {
                if (originalBackgroundColor == null) {
                    originalBackgroundColor = container.getDisplayPanes()[0]
                            .getRenderableDisplay().getBackgroundColor();
                }
                for (IDisplayPane pane : container.getDisplayPanes()) {
                    pane.getRenderableDisplay().setBackgroundColor(color);
                }
                container.refresh();
            }
        }
    }

    /**
     * Get the shapefile background layer resource, creating and instantiating
     * it if requested.
     *
     * @param create
     *            true if resource should be created if it does not currently
     *            exist
     * @return ResourcePair for the layer or null if it does not exist or could
     *         not be created
     */
    private ResourcePair getShapefileRP(boolean create) {
        if (shapeRP == null && create) {
            ResourcePair rp = new ResourcePair();

            MapResourceGroupData data = new MapResourceGroupData();
            data.getResourceList().add(createMapRP("mapdata.world"));
            data.getResourceList().add(createMapRP("mapdata.states"));
            data.setMapName(RESOURCE_MAP_NAME);
            rp.setResourceData(data);

            LoadProperties loadProps = new LoadProperties();
            ColorableCapability colorCap = new ColorableCapability();
            colorCap.setColor(getAtcfColor(ColorSelectionNames.COASTLINES));
            loadProps.getCapabilities().addCapability(colorCap);
            rp.setLoadProperties(loadProps);

            setMapResourceProperties(rp);

            if (instantiate(rp)) {
                shapeRP = rp;
            }
        }
        return shapeRP;
    }

    /**
     * Create a map background resource to be used as a component of the
     * shapefile-based background layer.
     */
    private ResourcePair createMapRP(String table) {
        ResourcePair rp = new ResourcePair();

        AtcfShapeBackgroundResourceData data = new AtcfShapeBackgroundResourceData();
        data.setMapName(RESOURCE_MAP_NAME);
        data.setTable(table);
        rp.setResourceData(data);

        LoadProperties loadProps = new LoadProperties();
        ShadeableCapability shadeCap = new ShadeableCapability();
        shadeCap.setShadingField("gid");
        loadProps.getCapabilities().addCapability(shadeCap);
        ColorableCapability colorCap = new ColorableCapability();
        colorCap.setColor(getAtcfColor(ColorSelectionNames.COASTLINES));
        loadProps.getCapabilities().addCapability(colorCap);
        rp.setLoadProperties(loadProps);

        setMapResourceProperties(rp);

        return rp;
    }

    /**
     * Get the topo background layer resource, creating and instantiating it if
     * requested.
     *
     * @param create
     *            true if resource should be created if it does not currently
     *            exist
     * @return ResourcePair for the layer or null if it does not exist or could
     *         not be created
     */
    private ResourcePair getTopoRP(boolean create) {
        if (topoRP == null && create) {
            ResourcePair rp = new ResourcePair();

            /*
             * TODO: Should load from a bundle rather than directly using the
             * topo plugin, but this prototype is intended to be viz-only.
             */
            TopoResourceData data = new TopoResourceData();
            data.setMapName(RESOURCE_MAP_NAME);
            rp.setResourceData(data);

            LoadProperties loadProps = new LoadProperties();
            ColorMapCapability capability = new ColorMapCapability();
            setLandColorMap(capability);
            loadProps.getCapabilities().addCapability(capability);
            rp.setLoadProperties(loadProps);

            ResourceProperties props = rp.getProperties();
            if (props == null) {
                props = new ResourceProperties();
            }
            props.setMapLayer(true);
            props.setSystemResource(true);
            props.setRenderingOrderId("MAP_IMAGE");
            rp.setProperties(props);

            if (instantiate(rp)) {
                topoRP = rp;
            }
        }
        return topoRP;
    }

    /**
     * Set common resource properties for map background layers.
     */
    private void setMapResourceProperties(ResourcePair rp) {
        ResourceProperties props = new ResourceProperties();
        props.setMapLayer(true);
        rp.setProperties(props);
    }

    /**
     * Returns the descriptor associated with the AtcfResource. May return null
     * if the AtcfResource has not been instantiated yet.
     * <p>
     * Also registers listeners and records other state the first time the
     * descriptor is found to be available.
     */
    private IMapDescriptor getDescriptor() {
        IMapDescriptor descriptor = resource != null ? resource.getDescriptor()
                : null;
        if (descriptor != null && monitoredResourecList == null) {
            synchronized (this) {
                if (monitoredResourecList == null) {
                    backgroundColor = BackgroundColor
                            .getInstance(AtcfVizUtil.getActiveEditor().getSite()
                                    .getPage().getPerspective());
                    backgroundColor.addListener(BGColorMode.EDITOR, this);
                    monitoredResourecList = descriptor.getResourceList();
                    monitoredResourecList.addPostAddListener(this);
                    monitoredResourecList.addPostRemoveListener(this);
                }
            }
        }
        return descriptor;
    }

    private boolean instantiate(ResourcePair rp) {
        IMapDescriptor descriptor = getDescriptor();
        if (descriptor != null) {
            try {
                rp.instantiateResource(descriptor, true);
                return true;
            } catch (VizException e) {
                statusHandler
                        .error("Could not instantiate background resource: "
                                + e.toString(), e);
            }
        }
        return false;
    }

    private void addRPIfNeeded(ResourcePair rp) {
        IMapDescriptor descriptor = getDescriptor();
        if (descriptor != null && rp != null && rp.getResource() != null
                && !descriptor.getResourceList()
                        .containsRsc(rp.getResource())) {
            descriptor.getResourceList().add(rp);
        }
    }

    private void removeRPIfNeeded(ResourcePair rp) {
        IMapDescriptor descriptor = getDescriptor();
        if (descriptor != null && rp != null) {
            if (rp == shapeRP) {
                shapeRP = null;
            } else if (rp == topoRP) {
                topoRP = null;
            }
            if (descriptor.getResourceList().containsRsc(rp.getResource())) {
                descriptor.getResourceList().removeRsc(rp.getResource());
            }
        }
    }

    @Override
    public void notifyAdd(ResourcePair rp) throws VizException {
        if (isShouldUseAdjustedResourceColors()) {
            realizeResourceColors(rp.getResource(), true,
                    getBgReferenceColors());
        }
    }

    @Override
    public void notifyRemove(ResourcePair rp) throws VizException {
        if (rp == shapeRP) {
            shapeRP = null;
            if (mode == Mode.SHAPEFILE) {
                setMode(Mode.CAVE_DEFAULT);
            }
        } else if (rp == topoRP) {
            topoRP = null;
            if (mode == Mode.TOPO) {
                setMode(Mode.CAVE_DEFAULT);
            }
        } else {
            Pair<RGB, IResourceDataChanged> pair = backupResourceColors
                    .remove(rp.getResource());
            if (pair != null) {
                rp.getResourceData().removeChangeListener(pair.getSecond());
            }
        }
    }

    /**
     * Handle changes to the ATCF color configuration.
     */
    @Override
    public void colorsChanged() {
        if (topoRP != null) {
            AbstractVizResource<?, ?> rsc = topoRP.getResource();
            if (rsc != null) {
                ColorMapCapability capability = rsc
                        .getCapability(ColorMapCapability.class);
                setLandColorMap(capability);
            }
        }
        if (mode != Mode.CAVE_DEFAULT) {
            setDisplayColor(getAtcfColor(oceanColorSelection));
            realizeResourceColors();
        }
    }

    protected void setLandColorMap(ColorMapCapability capability) {
        ColorMapParameters params = capability.getColorMapParameters();
        if (params == null) {
            params = new ColorMapParameters();
            capability.setColorMapParameters(params);
        }
        ColorMap colorMap = new ColorMap(2);
        RGB color = getAtcfColor(ColorSelectionNames.LAND);
        com.raytheon.uf.common.colormap.Color cmColor = new com.raytheon.uf.common.colormap.Color(
                color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f,
                1.0f);
        colorMap.setColor(0, cmColor);
        colorMap.setColor(1, cmColor);
        params.setColorMap(colorMap);
    }

    /**
     * @return true if resource colors should be automatically adjusted given
     *         the current settings
     */
    protected boolean isShouldUseAdjustedResourceColors() {
        return mode != Mode.CAVE_DEFAULT && autoAdjustResourceColors;
    }

    private void realizeResourceColors() {
        IDescriptor descriptor = getDescriptor();
        if (descriptor != null) {
            realizeResourceColors(descriptor.getResourceList(),
                    isShouldUseAdjustedResourceColors(), getBgReferenceColors());
        }
    }

    private void realizeResourceColors(ResourceList resourceList,
            boolean setColors, ReferenceColors referenceColors) {
        for (ResourcePair rp : resourceList) {
            realizeResourceColors(rp.getResource(), setColors, referenceColors);
        }
    }

    private void realizeResourceColors(AbstractVizResource<?, ?> resource,
            boolean setColors, ReferenceColors referenceColors) {

        if (resource == null
                || resource instanceof AtcfShapeBackgroundResource) {
            return;
        }

        if (resource.hasCapability(ColorableCapability.class)) {
            ColorableCapability cap = resource
                    .getCapability(ColorableCapability.class);
            Pair<RGB, IResourceDataChanged> pair = backupResourceColors.get(resource);
            RGB originalColor = pair != null ? pair.getFirst() : null;
            if (setColors) {
                if (originalColor == null) {
                    originalColor = cloneRGB(cap.getColor());
                    IResourceDataChanged listener = (type, ov) -> {
                        if (type == ChangeType.CAPABILITY
                                && ov instanceof ColorableCapability) {
                            resourceColorChanged(resource,
                                    (ColorableCapability) ov);
                        }
                    };
                    backupResourceColors.put(resource, new Pair(originalColor, listener));
                    resource.getResourceData().addChangeListener(listener);
                }
                cap.setColor(adjustColor(referenceColors, originalColor));
            } else {
                if (originalColor != null) {
                    cap.setColor(originalColor);
                }
            }
        }
    }

    /**
     * Adjusts the given {@code color} if its contrast with the given reference
     * colors falls below the threshold.
     *
     * @param referenceColors
     * @param color
     * @return
     */
    private RGB adjustColor(ReferenceColors referenceColors, RGB color) {
        double threshold = contrastThreshold;
        for (RGB refColor : referenceColors.colors) {
            double rd = color.red / 255.0 - refColor.red / 255.0;
            double gd = color.green / 255.0 - refColor.green / 255.0;
            double bd = color.blue / 255.0 - refColor.blue / 255.0;
            if (Math.sqrt(rd * rd + gd * gd + bd * bd) < threshold) {
                float[] colorHSB = color.getHSB();
                float[] bgHSB = referenceColors.average.getHSB();
                if (bgHSB[2] >= 0.5) {
                    return new RGB(colorHSB[0], colorHSB[1],
                            Math.max(0f, bgHSB[2] - (float) threshold));
                } else {
                    return new RGB(colorHSB[0], colorHSB[1],
                            Math.min(1f, bgHSB[2] + (float) threshold));
                }
            }
        }
        return color;
    }

    private static RGB cloneRGB(RGB rgb) {
        return new RGB(rgb.red, rgb.green, rgb.blue);
    }

    private void resourceColorChanged(AbstractVizResource<?, ?> resource,
            ColorableCapability cap) {
        if (!isShouldUseAdjustedResourceColors()) {
            Pair<RGB, IResourceDataChanged> pair = backupResourceColors.get(resource);
            if (pair != null) {
                pair.setFirst(cap.getColor());
            }
        }
    }

    /**
     * Represents colors that should be considered when automatically adjusting
     * resource colors. Currently the ATCF land and (currently selected) ocean
     * colors.
     */
    private static class ReferenceColors {
        private RGB[] colors;

        /**
         * Color cube midpoint of {@code colors}
         */
        private RGB average;
    }

    private ReferenceColors getBgReferenceColors() {
        RGB ocean = getAtcfColor(oceanColorSelection);
        RGB land = getAtcfColor(ColorSelectionNames.LAND);
        ReferenceColors result = new ReferenceColors();
        result.colors = new RGB[] { ocean, land };
        result.average = new RGB(avg(ocean.red, land.red),
                avg(ocean.green, land.green), avg(ocean.blue, land.blue));
        return result;
    }

    private static int avg(int a, int b) {
        return Math.round((a + b) / 2.0f);
    }

    @Override
    public void setColor(BGColorMode mode, RGB newColor) {
        if (this.mode == Mode.CAVE_DEFAULT) {
            /*
             * This is called for any background color change in the
             * perspective. Make sure we are getting the background color for
             * our display.
             */
            IMapDescriptor descriptor = getDescriptor();
            if (descriptor != null) {
                originalBackgroundColor = cloneRGB(
                        descriptor.getRenderableDisplay().getBackgroundColor());
            }
        }
    }

}
