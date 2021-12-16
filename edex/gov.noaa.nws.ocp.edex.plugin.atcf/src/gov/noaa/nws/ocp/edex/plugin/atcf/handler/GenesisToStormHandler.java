/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GenesisToStormRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GenesisToStormHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 8, 2020 # 77134     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GenesisToStormHandler
        implements IRequestHandler<GenesisToStormRequest> {

    @Override
    public String handleRequest(GenesisToStormRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.promoteGenesisToStorm(request.getCandidate().getRegion(),
                    request.getCandidate().getGenesisNum(),
                    request.getCandidate().getYear(), request.getCycloneNum(),
                    request.getStormName());

        } catch (Exception e) {
            throw new Exception("Promote genesis to TC failed", e);
        }

    }

}
