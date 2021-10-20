/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Load and edit an Public Advisory text product.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2020 81818      wpaintsil   Initial creation.
 * Oct 19, 2020 82721      jwu         Move composition to AdvisoryBuilder.
 * Mar 22, 2021 88518      dfriedman   Rework product headers.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PublicAdvisoryHandler extends BaseEditProductHandler {

    @Override
    protected AdvisoryType getAdvisoryType() {
        return AdvisoryType.TCP;
    }
}