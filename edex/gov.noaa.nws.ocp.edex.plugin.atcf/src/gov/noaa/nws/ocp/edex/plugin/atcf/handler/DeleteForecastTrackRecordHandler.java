/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.DeleteForecastTrackRecordRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * DeleteForecastTrackRecordHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 06, 2020 72989      pwang     Initial creation
 * May 28, 2020 78027      jwu       Pass in sandbox ID.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class DeleteForecastTrackRecordHandler
        implements IRequestHandler<DeleteForecastTrackRecordRequest> {

    @Override
    public Integer handleRequest(DeleteForecastTrackRecordRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            dao.deleteForecastTrackRecordsInSandbox(request.getSandboxId(),
                    request.getFstRecords());

            return request.getSandboxId();
        } catch (Exception de) {
            throw new Exception("Delete fst records failed", de);
        }
    }

}