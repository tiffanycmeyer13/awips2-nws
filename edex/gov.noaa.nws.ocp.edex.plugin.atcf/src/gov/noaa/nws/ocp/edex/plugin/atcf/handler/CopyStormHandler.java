/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CopyStormRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * CopyStormHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2020 82622      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class CopyStormHandler implements IRequestHandler<CopyStormRequest> {

    @Override
    public String handleRequest(CopyStormRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.copyStorm(request.getOldStorm(), request.getNewStorm());
        } catch (Exception de) {
            throw new Exception("Copy the storm to new failed", de);
        }

    }

}
