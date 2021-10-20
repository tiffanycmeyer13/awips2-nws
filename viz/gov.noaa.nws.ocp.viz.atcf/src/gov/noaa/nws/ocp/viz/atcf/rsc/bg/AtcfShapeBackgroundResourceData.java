/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc.bg;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResource;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * Resource data for {@code AtcfShapeBackgroundResource}
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
public class AtcfShapeBackgroundResourceData extends DbMapResourceData {

    @Override
    public DbMapResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        return new AtcfShapeBackgroundResource(this, loadProperties);
    }
}
