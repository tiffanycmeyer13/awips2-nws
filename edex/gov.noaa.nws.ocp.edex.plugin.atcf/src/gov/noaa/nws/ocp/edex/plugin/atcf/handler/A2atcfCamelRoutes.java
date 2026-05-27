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

import com.raytheon.uf.edex.routes.EDEXRouteBuilder;

/**
 * Camel routes converted from file "a2atcf-ingest.xml", context "a2atcf-camel"
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

public class A2atcfCamelRoutes extends EDEXRouteBuilder {

    private final String edexHome;

    public A2atcfCamelRoutes(String edexHome) {
        this.edexHome = edexHome;
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        from("file:" + this.edexHome
                + "/data/sbn/a2atcf?noop=true&idempotent=false")
                        .bean("fileToString")
                        .setHeader("pluginName", constant("a2atcf"))
                        .to("jms-durable:queue:Ingest.a2atcf")
                        .setId("a2atcfFileConsumerRoute");

        from("jms-durable:queue:Ingest.a2atcf")
                .setHeader("pluginName", constant("a2atcf"))
                .doTry()
                        .bean("stringToFile")
                        .pipeline()
                                .bean("a2atcfDeckProcessor", "decode")
                                .to("log:a2atcf?level=INFO")
                .endDoTry()
                .doCatch(Throwable.class)
                        .to("log:a2atcf?level=ERROR")
                .endDoTry()
                .end()
                .setId("a2atcfIngestRoute");
        // @formatter:on
    }
}
