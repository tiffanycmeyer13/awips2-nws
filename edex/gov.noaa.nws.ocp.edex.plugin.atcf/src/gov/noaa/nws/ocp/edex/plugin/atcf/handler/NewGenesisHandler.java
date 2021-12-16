/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewGenesisRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;


/**
 * NewGenesisHandler
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
public class NewGenesisHandler implements IRequestHandler<NewGenesisRequest> {

    @Override
    public Boolean handleRequest(NewGenesisRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.addNewGenesis(request.getNewGenesis(), request.getGenesisBDeckRecord());

        } catch (Exception e) {
            throw new Exception("Add a new genesis failed", e);
        }

    }

}

