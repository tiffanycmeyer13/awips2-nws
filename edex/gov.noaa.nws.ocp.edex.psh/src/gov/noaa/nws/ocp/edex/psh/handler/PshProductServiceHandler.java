/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshProductServiceRequest;
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
 * 07 JUL 2017  #35738     jwu         Initial creation
 *
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */

public class PshProductServiceHandler
        implements IRequestHandler<PshProductServiceRequest> {

    @Override
    public Object handleRequest(PshProductServiceRequest request)
            throws Exception {
        return new PshProductBuilder().buildPshProduct(request.getPshData());
    }
}