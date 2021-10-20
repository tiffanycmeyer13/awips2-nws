/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Handle the formatting of an Tropical Cyclone Discussion text product.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2021 85386      wpaintsil   Initial creation.
 * Mar 22, 2021 88518      dfriedman   Rework product headers.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class DiscussionHandler extends BaseEditProductHandler {

    @Override
    protected AdvisoryType getAdvisoryType() {
        return AdvisoryType.TCD;
    }

}