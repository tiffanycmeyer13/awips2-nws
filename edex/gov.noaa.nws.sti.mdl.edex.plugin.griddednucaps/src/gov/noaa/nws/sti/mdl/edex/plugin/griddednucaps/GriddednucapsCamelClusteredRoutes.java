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

package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import org.apache.camel.AggregationStrategy;

import com.raytheon.uf.edex.routes.EDEXRouteBuilder;

/**
 * Camel routes converted from file "griddednucaps-ingest.xml", context
 * "griddednucaps-camel-clustered"
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

public class GriddednucapsCamelClusteredRoutes extends EDEXRouteBuilder {

    private AggregationStrategy aggregationStrategy;

    private String aggregateProcessorId;

    public GriddednucapsCamelClusteredRoutes(
            AggregationStrategy aggregationStrategy, String processorId) {
        this.aggregationStrategy = aggregationStrategy;
        this.aggregateProcessorId = processorId;
    }

    @Override
    public void configure() throws Exception {
        /*
         * Files are run through first pipeline in order to aggregate the files
         * into bunches of 5, or after lapse in getting files in 5 min. Then the
         * files are processed. Then they are forwarded onto the second pipeline
         * to get decoded and processed.
         */

        // @formatter:off
        from("jms-durable:queue:Ingest.Griddednucaps")
                .setHeader("pluginName", constant("griddednucaps"))
                .doTry()
                        .pipeline()
                                .bean("stringToFile")
                                .bean("getFileWithoutWmoHeader")
                                .bean("decodeNUCAPSSatelliteInformation")
                                .aggregate(simple("header"), this.aggregationStrategy)
                                        .id(this.aggregateProcessorId)
                                        .completionSize(5)
                                        .to("direct:griddedNucaps2")
                .endDoTry()
                .doCatch(Throwable.class)
                        .to("log:griddednucaps?level=ERROR")
                .endDoTry()
                .end()
                .setId("griddednucapsIngestRoute");

        from("direct:griddedNucaps2")
                .doTry()
                        .pipeline()
                                .bean("addDeleteOnCompletion")
                                .bean("griddedNUCAPSDecoder", "decode")
                                .to("direct:persistIndexAlert")
                .endDoTry()
                .doCatch(Throwable.class)
                        .to("log:griddednucaps?level=ERROR")
                .endDoTry()
                .end()
                .setId("griddednucapsIngestRoute2");
        // @formatter:on
    }
}
