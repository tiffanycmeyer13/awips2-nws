/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewStormRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * NewStormHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 14, 2018            pwang     Initial creation
 * Jun 28, 2019            pwang     support to add BDeckRecords for a new storm
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class NewStormHandler implements IRequestHandler<NewStormRequest> {

    @Override
    public Boolean handleRequest(NewStormRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.addNewStorm(request.getNewStorm(), request.getBdeckRecord());

        } catch (Exception de) {
            throw new Exception("Add a new Storms failed", de);
        }

    }

}
