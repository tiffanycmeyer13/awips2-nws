/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import java.util.ArrayList;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * 
 * resource data class for CWA
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 10, 2021 22802      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWAGeneratorResourceData extends AbstractResourceData {
    private ArrayList<CWAGeneratorResource> rscList = new ArrayList<>();

    public CWAGeneratorResourceData() {
        super();
    }

    @Override
    public CWAGeneratorResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        CWAGeneratorResource rsc = new CWAGeneratorResource(this,
                loadProperties);
        rscList.add(rsc);
        return rsc;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
