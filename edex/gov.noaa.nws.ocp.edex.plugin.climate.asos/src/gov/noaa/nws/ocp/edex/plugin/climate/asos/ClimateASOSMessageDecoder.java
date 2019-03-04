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
 * DSM and SMS message decoder. From rehost-adapt/src/asos_sm_decode folder.
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
 * 07 SEP 2017  37754      amoore    Exceptions instead of boolean returns.
 * 31 OCT 2017  40231      amoore    Clean up of MSM/DSM parsing and records. Better
 *                                   logging. Get rid of serialization tags.
 * 20 SEP 2018  20896      pwang     Enable to decode DSM and MSM with C[SX]US4[123456] WMO IDs
 * 12 OCT 2018  20941      pwang     Removed "site level" ingest station filter to avoid confusion
 * 
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

    private static final String MSG_AWIPS_ID = "\\w{3}[A-Z0-9]{1,3}";

    // Only valid for the collective DSM/MSM with C[DS]US27 WMO IDs
    private static final String CDSUS27_MSG_END = ".*=";

    private static final String CSXUS4_WMO_ID = "C[SX]US4[123456]";

    private static final Pattern MSG_BEGIN_PATTERN = Pattern.compile(MSG_BEGIN);

    private static final Pattern MSG_AWIPS_ID_PATTERN = Pattern
            .compile(MSG_AWIPS_ID);

    private static final Pattern CDSUS27_MSG_END_PATTERN = Pattern
            .compile(CDSUS27_MSG_END);

    private static final Pattern CSXUS4_WMO_ID_PATTERN = Pattern
            .compile(CSXUS4_WMO_ID);

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

        try (BufferedReader br = new BufferedReader(
                new FileReader(ingestFile))) {
            logger.debug("Decoding ASOS file: [" + ingestFile.getName() + "]");

            StringBuilder sb = null;
            while ((oneline = br.readLine()) != null) {
                String line = oneline.trim();

                if (line.isEmpty()) {
                    continue;
                }

                logger.debug("Decoding line: [" + line + "] from ASOS file: ["
                        + ingestFile.getName() + "]");

                if (WMO_HEADER_PATTERN.matcher(line).matches()) {
                    // WMOHeader is expecting "\r\n" for pattern match
                    line = line + "\r\n";
                    header = new WMOHeader(line.getBytes());

                    // check if the ingestFile contains DSM/MSM with C[SX]US4*
                    if (CSXUS4_WMO_ID_PATTERN.matcher(header.getTtaaii())
                            .matches()) {
                        /*
                         * In DSM/MSM with C[SX]US4*, subsequent line will be
                         * the AWIPS ID (NNNxxx)
                         */
                        String aiLine = br.readLine();

                        // ensure the AWIPS ID line is valid
                        if (aiLine == null || aiLine.isEmpty()) {
                            aiLine = br.readLine();
                        }

                        aiLine = aiLine.trim();
                        if (MSG_AWIPS_ID_PATTERN.matcher(aiLine).matches()) {
                            String prodCategory = aiLine.substring(0, 3);
                            /*
                             * There are message with CSUS4* header but not
                             * DSM/MSM, stop to decode it if the product is not
                             * a MSM / DSM
                             */
                            if (!prodCategory.equalsIgnoreCase("DSM")
                                    && !prodCategory.equalsIgnoreCase("MSM")) {
                                logger.info("The message in the file "
                                        + ingestFile.getName()
                                        + " is not valid DSM or MSM, ignored");
                                return;
                            }
                        }

                    }
                    // check if the ingestFile contains DSM/MSM with C[SX]US4*
                    if (CSXUS4_WMO_ID_PATTERN.matcher(header.getTtaaii())
                            .matches()) {
                        /*
                         * In DSM/MSM with C[SX]US4*, subsequent line will be
                         * the AWIPS ID (NNNxxx)
                         */
                        String aiLine = br.readLine();

                        // ensure the AWIPS ID line is valid
                        if (aiLine == null || aiLine.isEmpty()) {
                            aiLine = br.readLine();
                        }

                        aiLine = aiLine.trim();
                        if (MSG_AWIPS_ID_PATTERN.matcher(aiLine).matches()) {
                            String prodCategory = aiLine.substring(0, 3);
                            /*
                             * There are message with CSUS4* header but not
                             * DSM/MSM, stop to decode it if the product is not
                             * a MSM / DSM
                             */
                            if (!prodCategory.equalsIgnoreCase("DSM")
                                    && !prodCategory.equalsIgnoreCase("MSM")) {
                                logger.info("The message in the file "
                                        + ingestFile.getName()
                                        + " is not valid DSM or MSM, ignored");
                                return;
                            }
                        }

                    }

                } else if (MSG_BEGIN_PATTERN.matcher(line).lookingAt()) {
                    // Found a new message
                    String stationCode = line.substring(0, line.indexOf(" "));

                    if (theFilter
                            .ingestSourceStationAllowed(stationCode.trim())) {
                        if (sb != null && sb.length() > 0) {
                            /*
                             * If no END_PATTERN detected, before start decoding
                             * a new record, add previous message to the message
                             * list, if it exist.
                             */
                            messageList.add(sb.toString());
                        }
                        sb = new StringBuilder();
                        sb.append(line);
                    } else {
                        logger.info("DSM / MSM data from the station: ["
                                + stationCode.trim() + "] will not be decoded!");

                        sb = null;
                    }

                } else if (CDSUS27_MSG_END_PATTERN.matcher(line).lookingAt()) {
                    /*
                     * END_PATTERN only exists in the collective DSM/MSM with
                     * C[DS]US27. No END_PATTERN exists in the DSM/MSM with
                     * C[SX]US4*
                     */
                    line = line.substring(0, line.indexOf("="));
                    if (sb != null) {
                        sb.append(line);
                        // add to message list
                        messageList.add(sb.toString());
                        logger.debug(
                                "New ASOS message: [" + sb.toString() + "]");

                        sb = null;
                    }

                } else {
                    // add message lines before the END_PATTERN detected
                    // or reach to a new BEGIN_PATTERN
                    if (sb != null) {
                        sb.append(line);
                    }
                }
            }
            /*
             * End of the file reached but no END_PATTERN is detected, add the
             * message to the list for decoding
             */
            if (sb != null && sb.length() > 0) {
                messageList.add(sb.toString());
            }

        } catch (FileNotFoundException e) {
            throw new DecoderException(
                    "DSM or MSM Ingest file " + ingestFile + " was not found!",
                    e);
        } catch (IOException e) {
            throw new DecoderException(
                    "I/O exception for reading " + ingestFile, e);
        }

        // Parsing each message
        List<ClimateASOSMessageRecord> records = new ArrayList<ClimateASOSMessageRecord>();

        for (String message : messageList) {
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
                logger.error(
                        "The ASOS parser returned no records for the message:"
                                + message + " in the file ["
                                + ingestFile.getName() + "].");
            } else {
                records.add(record);
            }
        }

        // Persist to the climate database
        for (ClimateASOSMessageRecord record : records) {
            try {
                dao.storeToTable(record);
            } catch (Exception e) {
                logger.error("Error storing ASOS record: [" + record.toString()
                        + "]", e);
            }
        }
    }
}
