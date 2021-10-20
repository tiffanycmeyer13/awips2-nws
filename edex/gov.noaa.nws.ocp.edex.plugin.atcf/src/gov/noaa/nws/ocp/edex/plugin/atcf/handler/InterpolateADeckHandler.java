/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NoPermissionException;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.database.DataAccessLayerException;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation.DeckDataInterpolator;
import gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation.InterpolateADeckRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfDeckDao;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * Runs the interpolator on a subset of the A deck.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul  8, 2020 78599      dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public class InterpolateADeckHandler
        implements IRequestHandler<InterpolateADeckRequest> {

    @Override
    public Void handleRequest(InterpolateADeckRequest request)
            throws Exception {
        AtcfProcessDao dao = new AtcfProcessDao();
        List<? extends AbstractDeckRecord> inputRecords;
        Storm storm = request.getStorm();
        if (storm == null) {
            throw new IllegalArgumentException("storm must not be null");
        }
        Date dtg = request.getArgs().getDtg();
        if (dtg == null) {
            throw new NoPermissionException("DTG must not be null");
        }
        try {
            Map<String, Object> queryConditions = new HashMap<>();
            queryConditions.put("basin", storm.getRegion());
            queryConditions.put("year", storm.getYear());
            queryConditions.put("cycloneNum", storm.getCycloneNum());
            queryConditions.put("refTime", request.getArgs().getDtg());
            inputRecords = dao.getDeckList(AtcfDeckType.A, queryConditions, 0);
        } catch (Exception de) {
            throw new DataAccessLayerException("Retrieve A Deck records failed", de);
        }

        List<BaseADeckRecord> outputRecs = DeckDataInterpolator
                .interpolateRecords(request.getStorm(), request.getArgs(),
                        (List<BaseADeckRecord>) inputRecords,
                        request.getInitialValues(), null);
        AtcfDeckDao deckDao = new AtcfDeckDao();
        deckDao.replaceADeckStormDTGsAndModels(
                outputRecs.toArray(new ADeckRecord[outputRecs.size()]));
        return null;
    }

}
