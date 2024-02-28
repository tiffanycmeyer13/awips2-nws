/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckoutADeckDtgRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * CheckoutADeckDtgHandler
 *
 * The handler will return an sandboxId (Integer, > 0) to the caller
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2019 #63859     pwang       initial version
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class CheckoutADeckDtgHandler
        implements IRequestHandler<CheckoutADeckDtgRequest> {

    @Override
    public Integer handleRequest(CheckoutADeckDtgRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.checkOutADeckDtgToEdit(request.getSandboxId(),
                    request.getStormName(), request.getBasin(),
                    request.getYear(), request.getCycloneNum(),
                    request.getDtg(), request.getUserId());

        } catch (Exception de) {
            throw new Exception("Create A Deck sandbox failed ", de);
        }

    }

}
