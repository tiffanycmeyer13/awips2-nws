/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Load and edit a Forecast/Advisory (TCM) text product.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 29, 2020 81820       wpaintsil   Initial creation.
 * Mar 22, 2021 88518      dfriedman   Rework product headers.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class ForecastAdvisoryHandler extends BaseEditProductHandler {

    @Override
    protected AdvisoryType getAdvisoryType() {
        return AdvisoryType.TCM;
    }

}
