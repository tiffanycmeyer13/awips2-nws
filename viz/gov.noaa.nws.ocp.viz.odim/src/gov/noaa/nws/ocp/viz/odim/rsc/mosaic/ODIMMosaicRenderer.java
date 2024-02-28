/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc.mosaic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.DrawableImage;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.PixelCoverage;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.drawables.ext.IMosaicImageExtension;
import com.raytheon.uf.viz.core.drawables.ext.IMosaicImageExtension.IMosaicImage;
import com.raytheon.uf.viz.core.drawables.ext.IMosaicMaxValImageExtension;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IRefreshListener;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ImagingCapability;
import com.raytheon.uf.viz.core.rsc.groups.BestResResource;
import com.raytheon.viz.radar.rsc.MosaicPaintProperties;

import gov.noaa.nws.ocp.viz.odim.rsc.ODIMRadialResource;
import gov.noaa.nws.ocp.viz.odim.rsc.mosaic.ODIMMosaicRendererFactory.IRadarMosaicRenderer;

/**
 * Copied from com.raytheon.viz.radar.rsc.mosaic.RadarMosaicRenderer and
 * modified.
 *
 * Radar Mosaic rendering class using IRadarMosaicImageExtension to render
 * mosaic image
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMMosaicRenderer
        implements IRadarMosaicRenderer, IRefreshListener {

    /** This instances offscreen texture */
    private IMosaicImage writeTo = null;

    /** Last extent painted, if extent changes, repaint to texture */
    private IExtent lastExtent = null;

    /** The coverage of the offscreen texture (used for drawing on screen) */
    private PixelCoverage writeToCoverage = null;

    /**
     * Default constructor, needed since class is instantiated through eclipse
     * extension point
     */
    public ODIMMosaicRenderer() {

    }

    @Override
    public void mosaic(IGraphicsTarget target, PaintProperties paintProps,
            ODIMMosaicResource mosaicToRender) throws VizException {

        // Get the image tiles we are going to render

        ColorMapParameters params = mosaicToRender
                .getCapability(ColorMapCapability.class)
                .getColorMapParameters();

        // If first paint, initialize and wait for next paint
        if (writeTo == null) {
            init(target, paintProps, params);

            // Listen for refreshes on the underlying resources
            for (ResourcePair rp : mosaicToRender.getResourceList()) {
                if (rp.getResource() != null) {
                    rp.getResource().registerListener(this);
                }
            }
            mosaicToRender.registerListener(this);
        } else if (!Arrays.equals(
                new int[] { writeTo.getWidth(), writeTo.getHeight() },
                new int[] { paintProps.getCanvasBounds().width,
                        paintProps.getCanvasBounds().height })) {
            // If Window size changed, recreate the off screen buffer
            dispose();
            init(target, paintProps, params);
        }

        MosaicPaintProperties props = (MosaicPaintProperties) paintProps;

        synchronized (this) {
            if (props.isForceRepaint()
                    || !paintProps.getView().getExtent().equals(lastExtent)) {
                List<DrawableImage> images = new ArrayList<>();

                // paint radar using mosaic target
                for (ResourcePair rp : mosaicToRender.getResourceList()) {
                    AbstractVizResource<?, ?> rsc = rp.getResource();
                    DataTime time = mosaicToRender.getTimeForResource(rsc);
                    if (rsc instanceof BestResResource) {
                        rsc = ((BestResResource) rsc).getBestResResource(time);
                    }
                    if (rsc instanceof ODIMRadialResource && time != null) {
                        ODIMRadialResource rr = (ODIMRadialResource) rsc;
                        DrawableImage di = rr.getImage(target, time);
                        if (di != null && di.getImage() != null
                                && di.getCoverage() != null
                                && di.getCoverage().getMesh() != null) {
                            // If image is ready to go, add
                            images.add(di);
                        } else {
                            mosaicToRender.issueRefresh();
                        }
                    }
                }

                writeTo.setImagesToMosaic(
                        images.toArray(new DrawableImage[images.size()]));
                lastExtent = paintProps.getView().getExtent().clone();
                writeTo.setImageExtent(lastExtent);

                Coordinate ul = new Coordinate(lastExtent.getMinX(),
                        lastExtent.getMaxY());
                Coordinate ur = new Coordinate(lastExtent.getMaxX(),
                        lastExtent.getMaxY());
                Coordinate lr = new Coordinate(lastExtent.getMaxX(),
                        lastExtent.getMinY());
                Coordinate ll = new Coordinate(lastExtent.getMinX(),
                        lastExtent.getMinY());

                writeToCoverage = new PixelCoverage(ul, ur, lr, ll);
            }

            writeTo.setContrast(mosaicToRender
                    .getCapability(ImagingCapability.class).getContrast());
            writeTo.setBrightness(mosaicToRender
                    .getCapability(ImagingCapability.class).getBrightness());

            target.drawRaster(writeTo, writeToCoverage, paintProps);
        }
    }

    /**
     * @param target
     */
    private void init(IGraphicsTarget target, PaintProperties paintProps,
            ColorMapParameters params) throws VizException {
        IMosaicImageExtension ext = target
                .getExtension(IMosaicMaxValImageExtension.class);
        if (ext == null) {
            // This could return about any mosaicing algorithm but it is better
            // than drawing nothing
            ext = target.getExtension(IMosaicImageExtension.class);
        }
        if (ext == null) {

        }
        // Construct texture for mosaicing
        writeTo = ext.initializeRaster(
                new int[] { paintProps.getCanvasBounds().width,
                        paintProps.getCanvasBounds().height },
                paintProps.getView().getExtent(), params);
    }

    @Override
    public void dispose() {
        // Dispose of all data, offscreen texture
        if (writeTo != null) {
            writeTo.dispose();
            writeTo = null;
        }
    }

    @Override
    public void refresh() {
        synchronized (this) {
            lastExtent = null;
        }
    }

}
