/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.transmitter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.raytheon.uf.common.auth.resp.SuccessfulExecution;
import com.raytheon.uf.common.dataplugin.text.db.AfosToAwips;
import com.raytheon.uf.common.dissemination.OUPRequest;
import com.raytheon.uf.common.dissemination.OUPResponse;
import com.raytheon.uf.common.dissemination.OfficialUserProduct;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.plugin.text.AfosToAwipsLookup;
import com.raytheon.uf.edex.plugin.text.db.TextDB;
import com.raytheon.uf.viz.core.auth.UserController;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.response.PshProductServiceResponse;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigHeader;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.edex.psh.productbuilder.PshProductBuilder;
import gov.noaa.nws.ocp.edex.psh.util.PshEdexUtil;

/**
 * Transmit a PSH product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 11 AUG 2017  #36930     jwu         Initial creation
 * 05 DEC,2017  #41620     wpaintsil   Add export capability
 * 11 DEC,2017  #41998     jwu         Move export to PshEdexUtil
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class PshProductTransmitter {

    /**
     * The logger
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshProductTransmitter.class);

    /**
     * Configuration header for PSH program.
     */
    private PshConfigHeader configHeader;

    /**
     * PSH product data.
     */
    private PshData pshData;

    /**
     * Constructor.
     */
    public PshProductTransmitter() {

        configHeader = PshConfigurationManager.getInstance().getConfigHeader();
    }

    /**
     * Transmit a PSH product.
     * 
     * @param product
     *
     * @param operational
     * 
     * @return
     */
    public boolean transmit(PshData product, boolean operational) {

        boolean hasError = false;

        pshData = product;

        PshProductServiceResponse pshProdResp = new PshProductBuilder()
                .buildPshProduct(product);

        // Export product content to finalpsh.txt, if configured.
        PshEdexUtil.exportProduct(pshProdResp.getMessage(), pshData,
                PshEdexUtil.PSH_TXT_FILE);

        // Store local use only product into TextDB.
        String afosNode = pshData.getRoute();
        if (afosNode.equals("000") || afosNode.equals("LOC")) {
            logger.info(
                    "This is a local product, AFOS originating site (CCC) = "
                            + configHeader.getProductPil().substring(0, 3));
            hasError = storeProductInTextDB(pshProdResp.getMessage(),
                    configHeader.getProductPil(), operational);

            return hasError;
        }

        /*
         * Transmit non-local product via OUP request.
         * 
         * Note: OUP will store product into textDB if transmit is successful.
         */
        hasError = forwardToOUP(pshProdResp.getMessage(), operational);
        if (!hasError) {
            String desc = "Transmit PSH product: "
                    + configHeader.getProductPil() + " sucessfully.";

            // Notify users
            PshEdexUtil.sendAlertVizMessage(Priority.INFO, desc, null);
            logger.info(desc);
        }

        return hasError;
    }

    /**
     * Stores a product into text DB.
     * 
     * @param product
     *            Product content without header.
     * @param pil
     *            PIL
     * @param operational
     */
    public boolean storeProductInTextDB(String product, String pil,
            boolean operational) {

        boolean hasError = false;

        TextDB textdb = new TextDB();

        // Add AFOS header?
        StringBuilder sb = new StringBuilder();
        sb.append(addProductHeader());
        sb.append(product);

        // writeProduct will return Long.MIN_VALUE if write failed
        long insertTime = textdb.writeProduct(pil, sb.toString(), operational,
                null);

        if (insertTime != Long.MIN_VALUE) {
            // Stored successfully
            String msg = "Storing PSH Products with PIL [" + pil
                    + "] to textdb successfully.";

            logger.info(msg);

        } else {
            /*
             * Failed to store the product into the Text DB, do not throw the
             * exception so user can try late
             */
            String desc = "Store PSH product: " + pil + " to TextDB failed.";

            // Notify users
            PshEdexUtil.sendAlertVizMessage(Priority.PROBLEM, desc, null);
            logger.error(desc);
            hasError = true;
        }

        return hasError;

    }

    /**
     * Forward the product to OUP for transmit using awips2 OUP handler.
     * 
     * By default, the service will add the WMO header to the beginning of the
     * productText.
     * 
     * @param prdContent
     *            Product content without header.
     * @param operational
     */
    private boolean forwardToOUP(String prdContent, boolean operational) {

        boolean noError = false;

        // Build an OUP.
        OfficialUserProduct oup = new OfficialUserProduct();

        StringBuilder fileName = new StringBuilder();

        fileName.append(configHeader.getPilFile()).append("_")
                .append(TimeUtil.getUnixTime(TimeUtil.newDate()));

        oup.setFilename(fileName.toString());
        oup.setProductText(prdContent);

        String afosId = configHeader.getProductPil();
        String awipsWanPil = mapToAwipsID(afosId);

        if (awipsWanPil == null || awipsWanPil.isEmpty()) {
            /*
             * Product ID may be not configured. Checking fxatext DB,
             * afos_to_awips
             */
            String msg = "Unable to send to OUP, the AFOSId : " + afosId
                    + " is not exist in fxatext.afos_to_awips table for the product.";
            logger.error(msg);
            return noError;
        }

        oup.setAwipsWanPil(awipsWanPil);
        oup.setSource(PshEdexUtil.CATEGORY);

        // Date/time in format of ddhhmm.
        LocalDateTime dt = LocalDateTime.now(Clock.systemUTC());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddhhmm");
        oup.setUserDateTimeStamp(dt.format(formatter));

        // Make an OUP request.
        OUPRequest req = new OUPRequest();
        req.setUser(UserController.getUserObject());
        req.setProduct(oup);
        OUPResponse resp = null;
        String additionalInfo = "";

        try {
            Object object = RequestRouter.route(req);
            if (!(object instanceof SuccessfulExecution)) {
                String msg = "Error transmitting PSH product. Unexpected response class: "
                        + object.getClass().getName();
                logger.error(msg);
            } else {
                resp = (OUPResponse) ((SuccessfulExecution) object)
                        .getResponse();

                if (resp.hasFailure()) {
                    // Check the type of failure.
                    Priority p = Priority.EVENTA;
                    if (!resp.isAttempted()) {
                        // Never attempted to send or store even locally.
                        p = Priority.CRITICAL;
                        additionalInfo = "ERROR - local storing never attempted";
                    } else if (!resp.isSendLocalSuccess()) {
                        // Failed to send/store locally.
                        p = Priority.CRITICAL;
                        additionalInfo = "ERROR - store locally failed";
                    } else if (!resp.isSendWANSuccess()) {
                        // Failed to send to WAN.
                        if (resp.getNeedAcknowledgment()) {
                            // Acknowledgment needed but not received.
                            p = Priority.CRITICAL;
                            additionalInfo = "ERROR - send to WAN failed and no acknowledgment received";
                        } else {
                            // No acknowledgment needed.
                            p = Priority.EVENTA;
                            additionalInfo = "WARNING - send to WAN failed";
                        }
                    } else if (resp.getNeedAcknowledgment()
                            && !resp.isAcknowledged()) {
                        // Sent but no acknowledged received when needed.
                        p = Priority.CRITICAL;
                        additionalInfo = "ERROR no acknowledgment received";
                    }

                    // Notify user
                    PshEdexUtil.sendAlertVizMessage(p, resp.getMessage(),
                            additionalInfo);
                    logger.error(resp.getMessage());

                } else {
                    noError = true;
                }
            }
        } catch (Exception e) {
            String msg = "Transmit PSH product [ "
                    + configHeader.getProductPil() + "] failed with exception: "
                    + e.getLocalizedMessage();
            logger.error(msg);
        }

        return noError;
    }

    /**
     * Maps an AFOS PIL (CCCNNNXXX) to AWIPS product identifier CCCCNNNXXX using
     * afos2awips.txt to perform table-lookup.
     * 
     * The logic is based on handleOUP.pl sub "mapToAfosAndWmoIds()". For PSH
     * product, the user will define afosId at Program Configuration as "PIL"
     * and "AfosToAwipsLookup" will do the lookup.
     * 
     * @param afosId
     * 
     * @return awipsID
     */
    private String mapToAwipsID(String afosId) {

        String prodAwipsID = "";
        List<AfosToAwips> list = AfosToAwipsLookup.lookupWmoId(afosId)
                .getIdList();
        for (AfosToAwips ata : list) {
            String cccc = ata.getWmocccc();
            String awipsId = afosId.substring(3);
            if (afosId.equals(ata.getAfosid())) {
                prodAwipsID = cccc + awipsId;
                break;
            }
        }

        return prodAwipsID;
    }

    /**
     * Creates an header to be added to the product.
     *
     * For now we creates the 2 line AFOS communications header of the following
     * format based on handleOUP.pl createAFOSHeader().
     * 
     * <pre>
     * CCCNNNXXXADR\n
    *  TTAAii CCCC DDHHMM BBB\n
     * </pre>
     * 
     * @return
     */
    private String addProductHeader() {

        // Creates AFOS header.
        StringBuilder sb = new StringBuilder();
        sb.append(configHeader.getProductPil());

        String route = PshEdexUtil.DEFAULT_ROUTE;
        if (pshData.getRoute() != null) {
            route = pshData.getRoute();
        }
        sb.append(route).append("\n");

        sb.append("TTAA00 ");
        sb.append(EDEXUtil.getEdexSite()).append(" ");

        // Legacy used date -u
        LocalDateTime dt = LocalDateTime.now(Clock.systemUTC());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddHHmm");
        String storageTime = dt.format(formatter);
        sb.append(storageTime);

        // Message type "BBB"
        String msgType = PshEdexUtil.MESSAGE_TYPE;
        String issueType = pshData.getLatestMessageType();
        if (issueType != null) {
            msgType = issueType;
        }

        sb.append(msgType);

        sb.append("\n");

        return sb.toString();
    }

    /**
     * Creates the 2 line AWIPS WAN communications header of the following
     * format based on handleOUP.pl createAwipsWANHeader().
     * 
     * <pre>
     *     TTAAii CCCC DDHHMM BBB\n
     *     NNNXXX\n
     *     
     *     where 
     *           TTAAii CCCC = "WMO abbreviated ID"
     *           DDHHMM = date/time stamp
     *           BBB = WMO message type (optional)
     *           NNNXXX = product category (NNN) + designated site (xxx)
     * Note: The first line is known as the WMO abbreviated header
     * </pre>
     * 
     * @return
     */
    private String addAwipsWanHeader() {

        // Creates AFOS header.
        StringBuilder sb = new StringBuilder();
        sb.append("TTAA00 ");

        sb.append(configHeader.getWfoNode()).append(" ");

        LocalDateTime dt = LocalDateTime.now(Clock.systemUTC());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddHHmm");
        String storageTime = dt.format(formatter);
        sb.append(storageTime).append(" ");

        // Message type "BBB"
        String msgType = PshEdexUtil.MESSAGE_TYPE;
        String issueType = pshData.getLatestMessageType();
        if (issueType != null) {
            msgType = issueType;
        }

        sb.append(msgType).append("\n");

        sb.append(configHeader.getWfoHeader()).append("\n");

        return sb.toString();
    }

}