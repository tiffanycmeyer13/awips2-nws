/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc.mosaic;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.radar.rsc.mosaic.RadarMosaicRendererFactory.MosaicType;

/**
 * Copied from com.raytheon.viz.radar.rsc.mosaic.RadarMosaicRendererFactory and
 * modified.
 *
 * Radar mosaic renderer factory, creates mosaic renderers
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
public class ODIMMosaicRendererFactory {

    public interface IRadarMosaicRenderer {

        void mosaic(IGraphicsTarget target, PaintProperties paintProps,
                ODIMMosaicResource mosaicToRender) throws VizException;

        void dispose();

    }

    public static IRadarMosaicRenderer createNewRenderer(MosaicType mosaicType)
            throws VizException {
        switch (mosaicType) {
        case MergeRaster:
            return new MergeRasterODIMMosaicRenderer();
        case MaxValue:
            return new ODIMMosaicRenderer();
        }
        throw new VizException(
                "Could not find mosaic renderer for type = " + mosaicType);
    }

}
