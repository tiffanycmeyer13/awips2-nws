/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractGenesisDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetGenesisDeckRecordsRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GetGenesisDeckRecordsHandler
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 8, 2020 # 77134     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GetGenesisDeckRecordsHandler
        implements IRequestHandler<GetGenesisDeckRecordsRequest> {

    @Override
    public List<? extends AbstractGenesisDeckRecord> handleRequest(
            GetGenesisDeckRecordsRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.getGenesisDeckRecordList(request.getDeckType(),
                    request.getQueryConditions());
        } catch (Exception de) {
            throw new Exception("Retrieve genesis deck records failed", de);
        }
    }

}
