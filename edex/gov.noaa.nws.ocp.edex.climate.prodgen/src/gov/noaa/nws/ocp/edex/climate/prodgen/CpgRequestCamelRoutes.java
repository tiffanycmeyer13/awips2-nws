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

package gov.noaa.nws.ocp.edex.climate.prodgen;

import com.raytheon.uf.edex.esb.camel.EDEXRouteBuilder;

/**
 * Camel routes converted from file "cpg-request.xml", context "cpg-request-camel"
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


public class CpgRequestCamelRoutes extends EDEXRouteBuilder {

    private final String cpgAmCron;

    private final String cpgPmCron;

    private final String cpgImCron;

    private final String cpgMonCron;

    private final String cpgSeaCron;

    private final String cpgAnnCron;

    private final String cpgF6Cron;

    public CpgRequestCamelRoutes(String cpgAmCron, String cpgPmCron, String cpgImCron, String cpgMonCron, String cpgSeaCron, String cpgAnnCron, String cpgF6Cron) {
        this.cpgAmCron = cpgAmCron;
        this.cpgPmCron = cpgPmCron;
        this.cpgImCron = cpgImCron;
        this.cpgMonCron = cpgMonCron;
        this.cpgSeaCron = cpgSeaCron;
        this.cpgAnnCron = cpgAnnCron;
        this.cpgF6Cron = cpgF6Cron;
    }

    @Override
    public void configure() throws Exception {
        
        getCamelContext().getPropertiesComponent().addLocation("ref:climateGlobalDayProperties");
        
        from("clusteredquartz://cpg/autocreateclimeAM/?cron=" + this.cpgAmCron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdAM", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgAMWork");
        from("clusteredquartz://cpg/autocreateclimePM/?cron=" + this.cpgPmCron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdPM", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgPMWork");
        from("clusteredquartz://cpg/autocreateclimeIM/?cron=" + this.cpgImCron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdIM", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgIMWork");
        from("clusteredquartz://cpg/autocreateclimeMon/?cron=" + this.cpgMonCron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdMonthly", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgMonWork");
        from("clusteredquartz://cpg/autocreateclimeSea/?cron=" + this.cpgSeaCron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdSeasonal", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgSeaWork");
        from("clusteredquartz://cpg/autocreateclimeAnn/?cron=" + this.cpgAnnCron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdAnnual", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgAnnWork");
        from("clusteredquartz://cpg/autocreateclimeF6/?cron=" + this.cpgF6Cron + "&trigger.timeZone={{climate.cpg.cron.timezone}}")
          .doTry()
              .bean("autoGenerateClimateProdF6", "generateClimate")
          .doCatch(Throwable.class)
              .to("log:cpgCronJob?level=ERROR")
          .endDoTry()
          .end()
          .setId("cpgF6Work");
    }
}
