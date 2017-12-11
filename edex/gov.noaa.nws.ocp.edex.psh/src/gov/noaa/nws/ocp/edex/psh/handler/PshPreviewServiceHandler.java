/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshPreviewServiceRequest;
import gov.noaa.nws.ocp.edex.psh.productbuilder.PshProductBuilder;

/**
 * Handle PSH product request.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 26 SEP 2017  #38085     wpaintsil         Initial creation
 *
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */

public class PshPreviewServiceHandler
        implements IRequestHandler<PshPreviewServiceRequest> {

    @Override
    public Object handleRequest(PshPreviewServiceRequest request)
            throws Exception {
        return new PshProductBuilder().buildPshPreview(request.getPshData(),
                request.getType());

    }
}