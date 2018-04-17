/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.SaveModifiedClimateProdAfterReviewRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;

/**
 * CompleteDisplayClimateHandler
 * 
 * CompleteDisplayClimateHandler will only be issued when the user take
 * following actions: On the ReviewClimate GUI, the user click "Save" to save
 * modified product
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016 20637      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class SaveModifiedClimateProdAfterReviewHandler
        implements IRequestHandler<SaveModifiedClimateProdAfterReviewRequest> {

    @Override
    public Object handleRequest(
            SaveModifiedClimateProdAfterReviewRequest request)
                    throws Exception {

        String cpgSessionId = request.getCpgSessionId();

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(cpgSessionId);

        // Save and update database
        try {
            session.saveModifiedClimateProd(request.getProdType(),
                    request.getProdKey(), request.getSaveProd());
        } catch (Exception e) {
            throw new Exception("Save modified climate product failed", e);
        }

        return null;
    }
}