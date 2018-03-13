package gov.noaa.nws.ocp.edex.psh.handler;

import java.util.List;

import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.psh.MetarDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.MetarStormDataRetrieveRequest;
import gov.noaa.nws.ocp.edex.psh.parser.MetarStormDataParser;
import gov.noaa.nws.ocp.edex.psh.textdb.dao.PshTextDB;

/**
 * MetarStormDataRetrieveHandler
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2017            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class MetarStormDataRetrieveHandler
        implements IRequestHandler<MetarStormDataRetrieveRequest> {

    @Override
    public MetarDataEntry handleRequest(MetarStormDataRetrieveRequest request)
            throws Exception {

        List<StdTextProduct> mtrSet = PshTextDB.retrieveMetarProduct(
                request.getNode(), request.getStation(), null, true);

        return new MetarStormDataParser().parse(mtrSet, request.getStation(),
                request.getLat(), request.getLon(), request.getPeriod());

    }

}
