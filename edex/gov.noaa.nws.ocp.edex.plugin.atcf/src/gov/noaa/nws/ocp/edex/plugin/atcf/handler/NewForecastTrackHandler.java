/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewForecastTrackRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * NewForecastTrackHandler If successful, the sandboxId will be returned
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 14, 2019 #69593     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class NewForecastTrackHandler
        implements IRequestHandler<NewForecastTrackRequest> {

    @Override
    public Integer handleRequest(NewForecastTrackRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            int sboxId = request.getSandboxId();
            if (sboxId < 0) {
                /*
                 * a new sandbox need to be created, the Storm should not be
                 * null
                 */
                if (null == request.getCurrentStorm()) {
                    throw new Exception(
                            "The current storm obj must be proviede if the sandboxId < 0");
                }
                Storm s = request.getCurrentStorm();
                sboxId = dao.createSandboxForecastTrack(s.getRegion(),
                        s.getYear(), s.getCycloneNum(), s.getStormName(),
                        request.getUserId());

            }
            for (ForecastTrackRecord r : request.getFstRecords()) {
                dao.addNewForecastTrackRecordInSandbox(sboxId, r);
            }

            return sboxId;
        } catch (Exception de) {
            throw new Exception("Retrieve fst records failed", de);
        }
    }

}
