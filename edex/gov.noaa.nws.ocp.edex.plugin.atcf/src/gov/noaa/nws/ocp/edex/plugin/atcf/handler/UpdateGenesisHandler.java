/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.UpdateGenesisRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * UpdateGenesisHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 10, 2020 79571      wpaintsil   Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class UpdateGenesisHandler
        implements IRequestHandler<UpdateGenesisRequest> {

    @Override
    public Boolean handleRequest(UpdateGenesisRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.updateGenesis(request.getGenesis());

        } catch (Exception de) {
            throw new Exception("Update the Genesiss failed", de);
        }

    }

}
