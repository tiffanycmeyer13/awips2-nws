/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc.mosaic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.viz.radar.rsc.mosaic.RadarMosaicResourceData;

/**
 * Metadata and constructor for ODIMMosaicResource
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
@XmlAccessorType(XmlAccessType.NONE)
public class ODIMMosaicResourceData extends RadarMosaicResourceData {

    @Override
    protected AbstractVizResource<?, ?> constructResource(
            LoadProperties loadProperties, PluginDataObject[] objects)
            throws VizException {
        return new ODIMMosaicResource(this, loadProperties);
    }

}
