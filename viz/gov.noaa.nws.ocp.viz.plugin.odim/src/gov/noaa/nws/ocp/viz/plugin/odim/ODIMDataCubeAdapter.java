/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.plugin.odim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequestSet;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.TimeQueryRequest;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponseSet;
import com.raytheon.uf.common.inventory.exception.DataCubeException;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.time.BinOffset;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.viz.pointdata.util.PointDataCubeAdapter;

/**
 * Data cube adapter for ODIM plugin
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMDataCubeAdapter extends PointDataCubeAdapter {

    private static final String DATA_TIME_FIELD = "dataTime";

    private static final String LATEST_DATA_TIME_FIELD = "dataTime.refTime";

    private static final String LEVEL_FIELD = "primaryElevationAngle";

    @Override
    public String[] getSupportedPlugins() {
        return new String[] { "odim" };
    }

    private DbQueryRequest getTimeQueryRequest(
            Map<String, RequestConstraint> queryParams, boolean latestOnly) {
        DbQueryRequest request = new DbQueryRequest();
        request.setConstraints(queryParams);

        String dataTimefield = DATA_TIME_FIELD;
        if (latestOnly) {
            dataTimefield = LATEST_DATA_TIME_FIELD;
        }
        request.addRequestField(dataTimefield, latestOnly);
        if (!latestOnly) {
            request.addRequestField(LEVEL_FIELD);
        }
        request.setDistinct(true);
        return request;
    }

    private Collection<DataTime> processTimeQueryResponse(
            DbQueryResponse response, boolean latestOnly, BinOffset binOffset) {
        String dataTimefield = DATA_TIME_FIELD;
        if (latestOnly) {
            dataTimefield = LATEST_DATA_TIME_FIELD;
        }
        Collection<DataTime> results = new HashSet<>();
        for (Map<String, Object> map : response.getResults()) {
            DataTime time = null;
            if (latestOnly) {
                time = new DataTime((Date) map.get(dataTimefield), 0);
            } else {
                time = (DataTime) map.get(dataTimefield);
                time.setLevelValue(
                        ((Number) map.get(LEVEL_FIELD)).doubleValue());
            }
            results.add(time);
        }

        if (binOffset != null) {
            Set<DataTime> scaledDates = new TreeSet<>();
            for (DataTime dt : results) {
                scaledDates.add(binOffset.getNormalizedTime(dt));
            }
            results = scaledDates;
        }

        return results;
    }

    @Override
    public List<List<DataTime>> timeQuery(List<TimeQueryRequest> requests)
            throws DataCubeException {
        List<DbQueryRequest> dbRequests = new ArrayList<>(requests.size());
        for (TimeQueryRequest request : requests) {
            dbRequests.add(getTimeQueryRequest(request.getQueryTerms(),
                    request.isMaxQuery()));
        }
        DbQueryRequestSet requestSet = new DbQueryRequestSet();
        requestSet.setQueries(dbRequests.toArray(new DbQueryRequest[0]));
        DbQueryResponseSet responseSet;
        try {
            responseSet = (DbQueryResponseSet) RequestRouter.route(requestSet);
        } catch (Exception e) {
            throw new DataCubeException(e);
        }
        List<List<DataTime>> result = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); i++) {
            DbQueryResponse response = responseSet.getResults()[i];
            TimeQueryRequest request = requests.get(i);
            Collection<DataTime> times = processTimeQueryResponse(response,
                    request.isMaxQuery(), request.getBinOffset());

            result.add(new ArrayList<>(times));
        }
        return result;
    }
}
