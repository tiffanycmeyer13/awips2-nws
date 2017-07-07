/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate;

import com.raytheon.edex.esb.Headers;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.obs.metar.MetarRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.wmo.WMOHeader;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateMetarDecodingException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAOValues;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.ClimateFSSInsertionDAO;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.ClimateReport;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.ClimateReportDao;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.MetarDecoder;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.MetarDecoderUtil;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.DecodedMetar;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.SurfaceObs;

/**
 * Stores MetarRecords as ClimateReport in Climate database
 * 
 * This plugin is a copy of MetarToHMDBSrv, which redirect metar data to rpt
 * table in the climate database
 * 
 * TODO: When hmdb retired, MetarToHMDBSrv plugin should be cleanup
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2016            pwang       Initial creation
 * 15 MAY 2017  33104      amoore      Logic fix for report truncation.
 * 07 SEP 2017  37754      amoore      Exceptions instead of boolean returns.
 *                                     Reorganize related methods for METAR decoding and storing.
 *                                     Throw exception on failure.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class MetarToClimateDBServer {

    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(MetarToClimateDBServer.class);

    /**
     * Report DAO.
     */
    private final ClimateReportDao reportDAO;

    /**
     * Fixed surface stations dao.
     */
    private final ClimateFSSInsertionDAO fssInsertionDAO;

    /**
     * Construct an instance of this transformer.
     * 
     * @throws ClimateException
     */
    public MetarToClimateDBServer() throws ClimateException {
        try {
            reportDAO = new ClimateReportDao();
        } catch (Exception e) {
            throw new ClimateException("Error constructing ClimateReportDao",
                    e);
        }

        try {
            fssInsertionDAO = new ClimateFSSInsertionDAO();
        } catch (ClimateQueryException e) {
            throw new ClimateException("Error constructing FSS Insertion DAO",
                    e);
        }
    }

    /**
     * 
     * @param objects
     */
    public void process(PluginDataObject[] objects, Headers headers) {
        logger.debug("Inside MetarToClimateDBServer.process()");

        for (PluginDataObject report : objects) {
            if (report instanceof MetarRecord) {
                try {
                    writeObs((MetarRecord) report, headers);
                } catch (Exception e) {
                    logger.error(
                            "Error processing METAR to Climate for report: ["
                                    + report.toString() + "]",
                            e);
                }
            }
        }
    }

    /**
     * @param report
     * @param headers
     * @throws ClimateException
     */
    private void writeObs(MetarRecord report, Headers headers)
            throws ClimateException {
        ClimateReport rpt = new ClimateReport();

        // Get the report data. This contains both the
        // WMOHeader and METAR report, so we need to split them.
        String rptData = report.getReport();
        String fileName = (String) headers.get(WMOHeader.INGEST_FILE_NAME);
        WMOHeader hdr = new WMOHeader(rptData.getBytes(), fileName);
        if (hdr.isValid()) {
            rpt.setWmo_dd(hdr.getWmoHeader().substring(0, 6));
            // if the report data is longer than 255 characters,
            // truncate it.
            String obsData = rptData.substring(hdr.getMessageDataStart());
            if (obsData.length() > 255) {
                obsData = obsData.substring(0, 255);
            }
            rpt.setReport(obsData);
        } else {
            // wmo_dd is a not_null field, so if we can't find it, exit now.
            return;
        }
        rpt.setDate(report.getTimeObs());
        // We don't have the origin time available,
        // so use the observation time.
        rpt.setOrigin(report.getTimeObs());
        rpt.setNominal(report.getRefHour());

        rpt.setReport_type(report.getReportType());

        rpt.setIcao_loc_id(report.getStationId());

        rpt.setLat(report.getLatitude());
        rpt.setLon(report.getLongitude());

        try {
            reportDAO.storeToTable(rpt);
        } catch (ClimateException e) {
            throw new ClimateException("Error storing report to table.", e);
        }

        // decode for FSS tables
        try {
            /*
             * In Legacy, station ID would not be searched for until after
             * decoding. However, decoding does take up processing time that
             * would be wasted if the METAR is not for a location that the
             * system has information on (stations in the cli_sta_setup table).
             * So save time and do the station check first.
             */
            // get station ID
            int stationID;
            try {
                stationID = reportDAO.getStationIDByCode(rpt.getIcao_loc_id());
            } catch (ClimateQueryException e) {
                logger.warn(
                        "Error getting Climate Station ID from surface observation location ID (station code): ["
                                + rpt.getIcao_loc_id()
                                + "]. Ensure that all applicable stations are in the Climate stations table. This report will be ignored: ["
                                + rpt.getReport() + "]. " + e.getMessage());
                return;
            }

            /*
             * In Legacy, get_METARs.c would call db_report.ecpp to get a list
             * of reports. However, we already have our report right here, and
             * can process immediately.
             */
            /*
             * Decode the report into a surface observation. Partially from
             * hmPED_decodeMetar.c.
             */
            DecodedMetar decodedMetar = DecodedMetar
                    .getInitializedDecodedMetar();

            MetarDecoder.decodeMetar(decodedMetar, rpt);

            /* store the decoded data in the common surface Obs structure */
            SurfaceObs surfaceObs = MetarDecoderUtil
                    .insertSurfaceData(decodedMetar, rpt);

            /*
             * From retrieve_OBS.C, write_FSS_data.ecpp, and
             * check_METAR_quality.c.
             */
            MetarDecoderUtil.checkMetarQuality(surfaceObs);

            /*
             * Get value of report instance serial.
             */
            String fssReportInstanceQuery = "SELECT NEXTVAL('"
                    + ClimateDAOValues.FSS_REPORT_FSS_RPT_INSTANCE_SEQ + "')";
            int fssReportInstance = ((Number) reportDAO.queryForOneValue(
                    fssReportInstanceQuery, Integer.MIN_VALUE)).intValue();

            if (fssReportInstance == Integer.MIN_VALUE) {
                throw new ClimateQueryException(
                        "Could not get valid fss_rpt_instance using: ["
                                + fssReportInstanceQuery + "].");
            }
            logger.debug("Current FSS report instance for report: ["
                    + report.getReport() + "] is: [" + fssReportInstance
                    + "].");

            fssInsertionDAO.storeFSSData(surfaceObs, stationID,
                    fssReportInstance);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ClimateException(
                    "Invalid indexing on decoding METAR report: [" + report
                            + "].",
                    e);
        } catch (NumberFormatException e) {
            throw new ClimateException(
                    "Invalid number parsing on decoding METAR report: ["
                            + report + "].",
                    e);
        } catch (ClimateMetarDecodingException e) {
            throw new ClimateException(
                    "Invalid pattern detected on decoding METAR report: ["
                            + report + "].",
                    e);
        } catch (Exception e) {
            throw new ClimateException("Error writing FSS data using report: ["
                    + report.getReport() + "].", e);
        }
    }
}
