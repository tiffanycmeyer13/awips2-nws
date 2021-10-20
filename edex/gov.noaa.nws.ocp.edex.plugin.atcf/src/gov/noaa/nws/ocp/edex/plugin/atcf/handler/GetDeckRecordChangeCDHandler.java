/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.Map;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetDeckRecordChangeCDRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GetDeckRecordChangeCDRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 14, 2018            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GetDeckRecordChangeCDHandler
        implements IRequestHandler<GetDeckRecordChangeCDRequest> {

    @Override
    public Map<Integer, Integer> handleRequest(
            GetDeckRecordChangeCDRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.getCurrentChangeCD(request.getDeckType(),
                    request.getSandboxid(), request.getRecIdList(),
                    request.isModifiedOnly());

        } catch (Exception de) {
            throw new Exception("Retrieve Map of changeCd failed", de);
        }

    }

}
