/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckoutDeckRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * CheckoutDeckHandler
 *
 * The handler will return an sandboxId (Integer, > 0) to the caller
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2019 #60291     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class CheckoutDeckHandler
        implements IRequestHandler<CheckoutDeckRequest> {

    @Override
    public Integer handleRequest(CheckoutDeckRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.checkOutDeckToEdit(request.getDeckType(),
                    request.getStormName(), request.getBasin(),
                    request.getYear(), request.getCycloneNum(),
                    request.getUserId());

        } catch (Exception de) {
            throw new Exception("Create sandbox failed ", de);
        }

    }

}
