/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen;

import java.time.LocalDateTime;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.F6ServiceResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateMessageUtils;
import gov.noaa.nws.ocp.edex.climate.f6builder.F6Builder;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateAlertUtils;

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
 * NOV 06 2017  35731      pwang       added logic to enable ON/OFF for F6 auto generation
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public final class ClimateProdGenerateF6 {
    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerateF6.class);

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
        // site can set F6 Auto generation ON/OFF
        // check the switch, if it is OFF, stop to proceed
        ClimateGlobal globalConfig = ClimateGlobalConfiguration.getGlobal();
        if (!globalConfig.isAutoF6()) {
            String msg = "Auto generate F6 product is not on in the GlobalDay configure properties";
            logger.info(msg);
            EDEXUtil.sendMessageAlertViz(Priority.EVENTB,
                    ClimateMessageUtils.F6_PLUGIN_ID,
                    ClimateAlertUtils.SOURCE_EDEX,
                    ClimateAlertUtils.CATEGORY_CLIMATE,
                    "F6 will not automatically run", msg, null);
            // do nothing
            return;
        }

        try {
            /*
             * F6 is executed for all stations, previous day's month. Builder
             * handles sending to text DB automatically and success alerts.
             */
            F6ServiceResponse response = new F6Builder().buildF6();

            if (!response.isSuccess()) {
                EDEXUtil.sendMessageAlertViz(Priority.PROBLEM,
                        ClimateMessageUtils.F6_PLUGIN_ID,
                        ClimateAlertUtils.SOURCE_EDEX,
                        ClimateAlertUtils.CATEGORY_CLIMATE,
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
            EDEXUtil.sendMessageAlertViz(Priority.PROBLEM,
                    ClimateMessageUtils.F6_PLUGIN_ID,
                    ClimateAlertUtils.SOURCE_EDEX,
                    ClimateAlertUtils.CATEGORY_CLIMATE,
                    "F6 encountered error", "F6 session: " + sessionID
                            + " failed to execute F6.\n" + e.getMessage(),
                    null);
            throw e;
        }
    }
}