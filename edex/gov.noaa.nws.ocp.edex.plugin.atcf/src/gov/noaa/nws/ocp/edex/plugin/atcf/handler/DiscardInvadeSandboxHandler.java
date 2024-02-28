/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.DiscardInvadeSandboxRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;


/**
 * DiscardInvadeSandboxHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 1, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class DiscardInvadeSandboxHandler
        implements IRequestHandler<DiscardInvadeSandboxRequest> {

    @Override
    public Integer handleRequest(DiscardInvadeSandboxRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.discardInvalidSandbox(request.getSandboxid());

        } catch (Exception de) {
            throw new Exception("Discard invalid sandbox failed", de);
        }

    }

}

