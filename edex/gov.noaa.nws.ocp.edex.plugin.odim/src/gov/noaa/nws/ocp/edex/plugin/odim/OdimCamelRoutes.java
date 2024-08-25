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

package gov.noaa.nws.ocp.edex.plugin.odim;

import com.raytheon.uf.edex.routes.EDEXRouteBuilder;

/**
 * Camel routes converted from file "odim-ingest.xml", context "odim-camel"
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

public class OdimCamelRoutes extends EDEXRouteBuilder {

    public OdimCamelRoutes() {
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        from("jms-durable:queue:Ingest.ODIM?concurrentConsumers=1")
                .setHeader("dataType", constant("odim"))
                .doTry()
                        .multicast()
                                .pipeline()
                                        .bean("stringToFile")
                                        .bean("odimDecoder", "decode")
                                        .to("direct:persistIndexAlert")
                                .end()
                        .end()
                .endDoTry()
                .doCatch(com.raytheon.uf.common.dataplugin.exception.MalformedDataException.class)
                        .to("direct:logFailureAsInfo")
                .doCatch(Throwable.class)
                        .to("direct:logFailedData")
                .endDoTry()
                .end()
                .setId("odimIngestRoute");
        // @formatter:on
    }
}
