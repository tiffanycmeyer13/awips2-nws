/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen;

import java.time.LocalDateTime;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.response.F6ServiceResponse;
import gov.noaa.nws.ocp.edex.climate.f6builder.F6Builder;

/**
 * Cron job run of F6 builder.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * MAR 28 2017  30171      amoore      Initial creation
 * APR 06 2017  30171      amoore      Clean up messaging.
 * MAY 03 2017  33533      amoore      Clean up.
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public final class ClimateProdGenerateF6 {
    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerateF6.class);

    private final static String PLUGIN_ID = "ClimateProdGenerateF6";

    private final String sessionID;

    /**
     * default constructor
     */
    public ClimateProdGenerateF6() {
        sessionID = "f6_" + LocalDateTime.now().toString();
    }

    /**
     * This method will only be called by cron job
     * 
     * @throws Exception
     */
    public void generateClimate() throws Exception {
        try {
            /*
             * F6 is executed for all stations, previous day's month. Builder
             * handles sending to text DB automatically and success alerts.
             */
            F6ServiceResponse response = new F6Builder().buildF6();

            if (!response.isSuccess()) {
                EDEXUtil.sendMessageAlertViz(Priority.PROBLEM, PLUGIN_ID,
                        ClimateProdGenerateSession.EDEX,
                        ClimateProdGenerateSession.CATEGORY,
                        "F6 encountered error",
                        "F6 session: " + sessionID
                                + " failed to execute at least one F6 product, completing at"
                                + LocalDateTime.now().toString() + ".\n"
                                + response.getMessage(),
                        null);
            }
            // Viz should show successful products/messages from Builder
        } catch (Exception e) {
            logger.error("Failed to execute build F6", e);
            EDEXUtil.sendMessageAlertViz(Priority.PROBLEM, PLUGIN_ID,
                    ClimateProdGenerateSession.EDEX,
                    ClimateProdGenerateSession.CATEGORY,
                    "F6 encountered error", "F6 session: " + sessionID
                            + " failed to execute F6.\n" + e.getMessage(),
                    null);
            throw e;
        }
    }
}