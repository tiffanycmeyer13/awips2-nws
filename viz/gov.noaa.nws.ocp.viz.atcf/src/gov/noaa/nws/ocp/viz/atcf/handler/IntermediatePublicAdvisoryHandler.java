/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Load and edit an Intermediate Public Advisory text product.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 14, 2021 88584       wpaintsil   Initial creation.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class IntermediatePublicAdvisoryHandler extends BaseEditProductHandler {

    @Override
    protected AdvisoryType getAdvisoryType() {
        return AdvisoryType.TCP_A;
    }

    @Override
    protected String getProductDescription() {
        return "intermediate public advisory";
    }
}