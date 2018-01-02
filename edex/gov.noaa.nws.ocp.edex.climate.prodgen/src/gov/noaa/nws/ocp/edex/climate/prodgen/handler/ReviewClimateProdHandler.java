/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;
import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ReviewClimateProdRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;

/**
 * ReviewClimateProdHandler ReviewClimateProdHandler will only be issued when
 * the user respond to formatClimate is done
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
public class ReviewClimateProdHandler
        implements IRequestHandler<ReviewClimateProdRequest> {

    @Override
    public Object handleRequest(ReviewClimateProdRequest request)
            throws Exception {

        String cpgSessionId = request.getCpgSessionID();

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(cpgSessionId);

        if (session.getState() != SessionState.FORMATTED
                && session.getState() != SessionState.REVIEW) {
            String msg = "Climate products had been reviewed, current state is "
                    + session.getState();
            ClimateProdGenerateSession.sendAlertVizMessage(Priority.INFO, msg,
                    null);
        }

        return session.startReviewProdData(ClimateProductType.NWWS,
                request.getUserId());

    }

}
