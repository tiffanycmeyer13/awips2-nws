/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetGenesisRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GetGenesisHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 6, 2020 # 77134     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GetGenesisHandler implements IRequestHandler<GetGenesisRequest> {

    @Override
    public List<Genesis> handleRequest(GetGenesisRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.getGenesisList(request.getQueryConditions());

        } catch (Exception de) {
            throw new Exception("Retrieve Genesis List failed", de);
        }

    }

}
