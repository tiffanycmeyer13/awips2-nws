/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 *
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 *
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/

package gov.noaa.nws.ocp.edex.metartoclimate;

import com.raytheon.uf.edex.routes.EDEXRouteBuilder;

/**
 * Camel routes converted from file "metartoclimate-spring.xml", context
 * "metarToClimateSvr-camel"
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2024-07-11   2037701    aford       Initial creation (from auto-generated)
 *
 * </pre>
 */

public class MetarToClimateSvrCamelRoutes extends EDEXRouteBuilder {

    private final String metartoclimatepurgeCron;

    public MetarToClimateSvrCamelRoutes(String metartoclimatepurgeCron) {
        this.metartoclimatepurgeCron = metartoclimatepurgeCron;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off

        // purge climate DB rpt table
        from("quartz://metartoclimate/climatepurge/?cron=" + this.metartoclimatepurgeCron)
                .doTry()
                        .bean("metarToClimatePurger", "purgeClimateReport")
                .doCatch(Throwable.class)
                        .to("log:metartoclimate?level=ERROR")
                .endDoTry()
                .end()
                .setId("metarClimatePurgeWork");
        // @formatter:on
    }
}
