/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.raytheon.edex.esb.Headers;
import com.raytheon.edex.exception.DecoderException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.wmo.WMOHeader;

import gov.noaa.nws.ocp.common.dataplugin.climate.asos.ClimateASOSMessageRecord;
import gov.noaa.nws.ocp.edex.plugin.climate.asos.config.ClimateIngestConfigurationManager;
import gov.noaa.nws.ocp.edex.plugin.climate.asos.config.ClimateIngestFilterXML;
import gov.noaa.nws.ocp.edex.plugin.climate.asos.dao.ClimateASOSMessageDAO;

/**
 * DSM and SMS message decoder
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 6, 2016  16962      pwang     Initial creation
 * Aug 4, 2016  20905      pwang     Add filter to only decode configured Sites / Stations
 * 24 FEB 2017  27420      amoore    Address warnings in code.
 * 05 MAY 2017  33104      amoore    Minor clean up.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateASOSMessageDecoder {

    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(ClimateASOSMessageDecoder.class);

    private static final String WMO_HEADER = "[A-Z]{3}[A-Z0-9](?:\\d{0,2}|[A-Z]{0,2})\\s+[A-Z0-9]{4}\\s+\\d{6}(?:[A-Z]{3})?";

    private static final Pattern WMO_HEADER_PATTERN = Pattern
            .compile(WMO_HEADER);

    private static final String MSG_BEGIN = "\\w{3,4}\\s+[DM]S\\s+.*";

    private static final String MSG_END = ".*=";

    private static final Pattern MSG_BEGIN_PATTERN = Pattern.compile(MSG_BEGIN);

    private static final Pattern MSG_END_PATTERN = Pattern.compile(MSG_END);

    private ClimateASOSMessageDAO dao;

    private ClimateIngestConfigurationManager cicm;

    /** The WMO header */
    private WMOHeader header;

    /**
     * Constructor.
     */
    public ClimateASOSMessageDecoder() {
        try {
            dao = new ClimateASOSMessageDAO();
            cicm = ClimateIngestConfigurationManager.getInstance();

            logger.debug("Successfully constructed ASOS decoder.");
        } catch (Exception e) {
            logger.error("ClimateASOSMessageDecoder creation failed", e);
        }
    }

    /**
     * @param inputData
     * @param headers
     * @return
     * @throws DecoderException
     */
    public void decode(File ingestFile, Headers headers)
            throws DecoderException {

        BufferedReader br = null;
        String oneline = "";
        List<String> messageList = new ArrayList<String>();

        ClimateIngestFilterXML theFilter = null;
        ArrayList<ClimateIngestFilterXML> filters = cicm.getIngestFilters();
        if (filters == null || filters.isEmpty()) {
            // No climate_ingest_config.xml exists?
            theFilter = new ClimateIngestFilterXML();
        } else {
            theFilter = filters.get(0);
        }

        try {
            logger.debug("Decoding ASOS file: [" + ingestFile.getName() + "]");

            br = new BufferedReader(new FileReader(ingestFile));
            StringBuilder sb = null;
            while ((oneline = br.readLine()) != null) {
                // Trim spaces of the line
                String line = oneline.trim();
                line = line.replaceAll("\\r", "").replaceAll("\\n", "");

                if (line.isEmpty()) {
                    continue;
                }

                logger.debug("Decoding line: [" + line + "] from ASOS file: ["
                        + ingestFile.getName() + "]");

                if (WMO_HEADER_PATTERN.matcher(line).matches()) {
                    // WMOHeader is expecting "\r\n" for pattern match
                    line = line + "\r\n";
                    header = new WMOHeader(line.getBytes());

                    // If site specify the SITE filter, ONLY decode
                    // configured sites
                    if (!theFilter.ingestSourceSiteAllowed(header.getCccc())) {
                        // no need continue
                        logger.info("DSM / MSM data from the site: ["
                                + header.getCccc() + "] will not be decoded!");
                        return;
                    }

                } else if (MSG_BEGIN_PATTERN.matcher(line).lookingAt()) {
                    // Found a new message
                    String station = line.substring(0, line.indexOf(" "));

                    if (theFilter.ingestSourceStationAllowed(station.trim())) {
                        sb = new StringBuilder();
                        sb.append(line);
                    } else {
                        // MSM from the station is not ingested
                        logger.info("DSM / MSM data from the station: ["
                                + station.trim() + "] under site: ["
                                + header.getCccc() + "] will not be decoded!");
                        sb = null;
                    }

                } else if (MSG_END_PATTERN.matcher(line).lookingAt()) {
                    // Reach the end of message
                    line = line.substring(0, line.indexOf("="));
                    if (sb != null) {
                        sb.append(line);
                        // add to message list
                        messageList.add(sb.toString());
                        logger.debug(
                                "New ASOS message: [" + sb.toString() + "]");

                        // restart message builder
                        sb = null;
                    }

                } else {
                    // Message lines
                    if (sb != null) {
                        sb.append(line);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new DecoderException(
                    "DSM or MSM Ingest file " + ingestFile + " is not found!",
                    e);
        } catch (IOException e) {
            throw new DecoderException(
                    "I/O exception for reading " + ingestFile, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new DecoderException(
                            "I/O exception for closing " + ingestFile, e);
                }
            }
        }

        // Parsing each message
        List<ClimateASOSMessageRecord> records = new ArrayList<ClimateASOSMessageRecord>();

        for (String message : messageList) {
            logger.debug("Decoding ASOS message: [" + message + "]");

            ASOSMessageParser parser = ASOSMessageParserFactory
                    .getASOSMessageParser(message);
            if (null == parser) {
                throw new DecoderException(
                        "No ASOS mesage parser for the message: [" + message
                                + "] from file: [" + ingestFile.getName()
                                + "]");
            }

            ClimateASOSMessageRecord record = parser.parse(message);

            if (record == null) {
                logger.error("The parser return no records");
            } else {
                records.add(record);
            }
        }

        // Persist to the database
        for (ClimateASOSMessageRecord record : records) {
            dao.storeToTable(record);
        }
    }
}
