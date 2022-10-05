/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.plugin.odim;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.productbrowser.datalisting.DataListingProductBrowserDefinition;

import gov.noaa.nws.ocp.viz.plugin.odim.rsc.ODIMResourceData;

/**
 * Product Browser data definition for ODIM plugin
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
public class ODIMProductBrowserDataDefinition
        extends DataListingProductBrowserDefinition {

    public ODIMProductBrowserDataDefinition() {
        super("ODIM", new ODIMDataListing("odim",
                Arrays.asList("node", "quantity", "primaryElevationAngle")));
    }

    @Override
    protected AbstractResourceData createResourceData(
            Map<String, String> keyVals) {
        ODIMResourceData resourceData = new ODIMResourceData();
        Map<String, RequestConstraint> constraints = listing
                .getRequestConstraints(keyVals);
        resourceData.setMetadataMap(new HashMap<>(constraints));
        return resourceData;
    }

}
