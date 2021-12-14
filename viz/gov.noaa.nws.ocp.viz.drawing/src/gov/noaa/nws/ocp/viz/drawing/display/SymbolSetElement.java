/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.DrawableImage;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.PixelCoverage;
import com.raytheon.uf.viz.core.drawables.IImage;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Contains a raster image and information needed to readily display that image
 * on a graphics target at one or more locations.
 * <P>
 * Objects of this class are typically created from Symbol or SymbolLocationSet
 * elements using the DisplayElementFactory class.
 *
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
public class SymbolSetElement implements IDisplayable {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(SymbolSetElement.class);

    /*
     * The raster image to be displayed
     */
    private final IImage raster;

    /*
     * Array of plot locations in pixel coordinates
     */
    private final double[][] locations;

    /**
     * Constructor used to set an image and its associated locations
     *
     * @param raster
     *            Rater image to display
     * @param paintProps
     *            paint properties for the target
     * @param locations
     *            pixel coordinate locations to display the image
     */
    public SymbolSetElement(IImage raster, double[][] locations) {
        this.raster = raster;
        this.locations = locations;
    }

    /**
     * disposes the resources held by the raster image
     *
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#dispose()
     */
    @Override
    public void dispose() {

        raster.dispose();

    }

    /**
     * Plots the image to the specified graphics target at the various locations
     *
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#draw(com.raytheon.viz.core.IGraphicsTarget)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps) {

        /*
         * Scale image
         */
        double screenToWorldRatio = paintProps.getCanvasBounds().width
                / paintProps.getView().getExtent().getWidth();
        double scale = 1 / screenToWorldRatio;

        /*
         * Add image at each location to the list
         */
        List<DrawableImage> images = new ArrayList<>();
        for (double[] loc : locations) {
            PixelCoverage extent = new PixelCoverage(
                    new Coordinate(loc[0], loc[1]), raster.getWidth() * scale,
                    raster.getHeight() * scale);
            images.add(new DrawableImage(raster, extent));
        }

        // Draw all images.
        try {
            target.drawRasters(paintProps,
                    images.toArray(new DrawableImage[0]));
        } catch (VizException e) {
            handler.error("SymbolImageSet - " + e);
        }
    }

}
