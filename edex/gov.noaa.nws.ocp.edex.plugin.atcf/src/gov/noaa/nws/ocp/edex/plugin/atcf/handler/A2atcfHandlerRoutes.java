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

package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.edex.esb.camel.EDEXRouteBuilder;

/**
 * Camel routes converted from file "a2atcf-request.xml", context "a2atcf-handler"
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2024-07-11   2037702    aford       Initial creation (from auto-generated)
 *
 * </pre>
 */


public class A2atcfHandlerRoutes extends EDEXRouteBuilder {

    private final String atcfSboxPurgeCron;

    public A2atcfHandlerRoutes(String atcfSboxPurgeCron) {
        this.atcfSboxPurgeCron = atcfSboxPurgeCron;
    }

    @Override
    public void configure() throws Exception {
        from("clusteredquartz://a2atcf/sboxPurger/?cron=" + this.atcfSboxPurgeCron + "")
          .doTry()
              .bean("atcfSandboxPurger", "purgeSandbox")
          .doCatch(Throwable.class)
              .to("log:atcfSandboxPurger?level=ERROR")
          .endDoTry()
          .end()
          .setId("sboxPurgeWork");
        from("seda:edex.atcfNotification")
          .bean("serializationUtil", "transformToThrift")
          .to("jms-generic:topic:edex.a2atcf.msg")
          .setId("a2atcfNotify");
    }
}
