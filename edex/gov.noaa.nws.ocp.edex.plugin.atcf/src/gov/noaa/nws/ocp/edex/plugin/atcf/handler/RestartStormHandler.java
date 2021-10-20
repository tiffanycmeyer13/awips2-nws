/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.RestartStormRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * RestartStormHandler
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
public class RestartStormHandler
        implements IRequestHandler<RestartStormRequest> {

    @Override
    public Void handleRequest(RestartStormRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            dao.restartStorm(request.getStorm());
            return null;
        } catch (Exception de) {
            throw new Exception("Restart the given ended storm failed", de);
        }

    }

}
