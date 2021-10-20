/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.DeckMergeLog;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetDeckMergeLogListRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GetDeckMergeLogListHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 4, 2020  #78298     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GetDeckMergeLogListHandler
        implements IRequestHandler<GetDeckMergeLogListRequest> {

    @Override
    public List<DeckMergeLog> handleRequest(GetDeckMergeLogListRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.getAllDeckMergeLog(request.getDeckType(),
                    request.getBasin(), request.getYear(),
                    request.getCyclonenum());

        } catch (Exception de) {
            throw new Exception("Retrieve DeckMergeLog list failed ", de);
        }

    }

}
