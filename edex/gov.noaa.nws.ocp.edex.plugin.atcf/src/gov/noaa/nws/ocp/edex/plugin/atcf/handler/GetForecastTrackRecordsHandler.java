/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetForecastTrackRecordsRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * GetForecastTrackRecordsHandler
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
public class GetForecastTrackRecordsHandler
        implements IRequestHandler<GetForecastTrackRecordsRequest> {

    @Override
    public List<? extends AbstractDeckRecord> handleRequest(
            GetForecastTrackRecordsRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            return dao.getForecastTrackRecordList(
                    request.getQueryConditions(),
                    request.getSandboxId());
        } catch (Exception de) {
            throw new Exception("Retrieve fst records failed", de);
        }
    }

}

