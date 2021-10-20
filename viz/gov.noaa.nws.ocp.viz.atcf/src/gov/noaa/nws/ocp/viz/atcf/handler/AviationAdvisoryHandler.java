/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Handle the formatting of an Aviation (ICAO) Advisory text product.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 02, 2020 71720      wpaintsil   Initial creation.
 * Aug 03, 2020 80896      dfriedman   Use TextWS-based editor.
 * Jan 26, 2021 86746      jwu         Move logic to AdvisoryBuilder.
 * Mar 22, 2021 88518      dfriedman   Rework product headers.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class AviationAdvisoryHandler extends BaseEditProductHandler {

    @Override
    protected AdvisoryType getAdvisoryType() {
        return AdvisoryType.TCA;
    }

}