/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetDeckRecordsRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GetADeckRecordsHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 14, 2018            pwang       Initial creation
 * Mar 29, 2019 #61590     dfriedman   Merge type-specific handlers into one class.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GetDeckRecordsHandler
        implements IRequestHandler<GetDeckRecordsRequest> {

    @Override
    public List<? extends AbstractDeckRecord> handleRequest(
            GetDeckRecordsRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.getDeckList(request.getDeckType(),
                    request.getQueryConditions(), request.getSandboxId());
        } catch (Exception de) {
            throw new Exception("Retrieve A Deck records failed", de);
        }
    }

}
