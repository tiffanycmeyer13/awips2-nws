package gov.noaa.nws.ocp.edex.psh.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshLSRProduct;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.LsrProductRequest;
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
 * Aug 04, 2017            wpaintsil   Initial creation
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class LsrProductRequestHandler
        implements IRequestHandler<LsrProductRequest> {

    @Override
    public List<PshLSRProduct> handleRequest(LsrProductRequest request)
            throws Exception {

        return PshTextDB.retrieveLSRProducts(request.getLsrHeader(),
                request.isOperational());

    }

}
