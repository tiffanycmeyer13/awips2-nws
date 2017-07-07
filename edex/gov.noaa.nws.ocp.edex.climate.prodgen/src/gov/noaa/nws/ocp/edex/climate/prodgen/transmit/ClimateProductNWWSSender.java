/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.transmit;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.raytheon.uf.common.auth.resp.SuccessfulExecution;
import com.raytheon.uf.common.auth.user.User;
import com.raytheon.uf.common.dataplugin.text.db.AfosToAwips;
import com.raytheon.uf.common.dissemination.OUPRequest;
import com.raytheon.uf.common.dissemination.OUPResponse;
import com.raytheon.uf.common.dissemination.OfficialUserProduct;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.plugin.text.dao.AfosToAwipsDao;
import com.raytheon.uf.edex.plugin.text.db.TextDB;

import gov.noaa.nws.ocp.common.dataplugin.climate.ActionOnProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdSendRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct.ProductStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateProdSendRecordDAO;

/**
 * Handle transmission of the NWWS climate products to OUP for broadcasting
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 5, 2017  20637     pwang     Initial creation
 * Jun 2, 2017  20642     pwang     Fixed AwipsWanPIL issue
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateProductNWWSSender {

    /** The logger */
    public static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProductNWWSSender.class);

    private String commsHeader;

    private String localProductText;

    private String afosId;

    private String afosNode;

    private String awipsSiteId;

    private String wmoTtaaii;

    private String wmoCccc;

    private String ddhhmm;

    protected ClimateProdSendRecordDAO dao;

    private ClimateProdGenerateSession session;

    /**
     * Empty constructor
     */
    public ClimateProductNWWSSender() {
    }

    public ClimateProductNWWSSender(ClimateProdGenerateSession session) {
        this.session = session;
        dao = new ClimateProdSendRecordDAO();
    }

    /**
     * processCommsHeader
     * 
     * @param prod
     * @return
     */
    public boolean processCommsHeader(ClimateProduct prod) {
        boolean noError = true;
        StringBuilder sb = new StringBuilder();

        String[] lines = prod.getProdText().split("\\r?\\n");
        if (lines == null) {

            String expMsg = "The NWWS prod text may not be formatted properly: ["
                    + prod.getPil() + "] ";
            prod.setStatus(ProductStatus.ERROR);
            prod.setLastAction(ActionOnProduct.SEND, expMsg);

            // Notify users
            session.sendAlertVizMessage(Priority.PROBLEM, expMsg,
                    prod.getProdText());
            noError = false;
            logger.error(expMsg);
        }

        this.commsHeader = lines[0];

        // Skip communication header line
        for (int i = 1; i < lines.length; i++) {
            sb.append(lines[i]);
        }

        // localProductText: product text without communication header
        this.localProductText = sb.toString();

        return noError;
    }

    /**
     * 
     * Extract following attributes from the product's communication header: 1)
     * TTAAii of WMO Header 2) CCCC of WMO header 3) AFOSId: first 9 characters
     * of the header 4) AFOSNode: 3 characters following AFOSId 5) User
     * DateTime, ddHHMM
     * 
     * @param prod
     * @return
     */
    public boolean extractAfosInfo(ClimateProduct prod) {
        boolean noError = true;
        String[] afosElements = commsHeader.split("\\s+");
        if (afosElements == null || afosElements.length < 3) {
            String expMsg = "Communication header in the NWWS prod has a wrong format: ["
                    + prod.getPil() + "]";
            prod.setStatus(ProductStatus.ERROR);
            prod.setLastAction(ActionOnProduct.SEND, expMsg);

            // Notify users
            session.sendAlertVizMessage(Priority.PROBLEM, expMsg,
                    prod.getProdText());
            noError = false;
            logger.error(expMsg);
            return noError;
        }

        // extract afosInfo ttaaii, cccc
        String afosInfo = commsHeader.substring(0, 12).toUpperCase();
        if (afosElements[0].length() > 12) {
            afosInfo = afosElements[0].substring(0, 12).toUpperCase();
            this.wmoTtaaii = afosElements[0].substring(12, 18);
            this.wmoCccc = afosElements[1];
        } else if (afosElements[1].length() == 6) {
            afosInfo = afosElements[0];
            this.wmoTtaaii = afosElements[1];
            this.wmoCccc = afosElements[2];
        }

        // extract ddhhmm
        this.ddhhmm = afosElements[afosElements.length - 1];

        // Further extract aforsId, afosNone
        String[] idAndNode = afosInfo.split("\\s+");
        if (idAndNode == null || idAndNode.length == 0) {
            String expMsg = "Communication header in the NWWS prod has a wrong format: ["
                    + prod.getPil() + "]";
            prod.setStatus(ProductStatus.ERROR);
            prod.setLastAction(ActionOnProduct.SEND, expMsg);

            // Notify users
            session.sendAlertVizMessage(Priority.PROBLEM, expMsg,
                    prod.getProdText());
            noError = false;
            logger.error(expMsg);
        } else if (idAndNode.length == 2) {
            // Normal case: one space in the afosinfo
            this.afosId = idAndNode[0];
            this.afosNode = idAndNode[1];
        } else if (idAndNode[0].length() > 9
                || idAndNode[0].equalsIgnoreCase(afosInfo)) {
            // The case without space before AfosNode
            this.afosId = afosInfo.substring(0, 9);
            this.afosNode = afosInfo.substring(9);
        }

        if (this.afosNode == null || this.afosNode.isEmpty()) {
            this.afosNode = "DEF";
        }

        logger.info("AFOS ID = " + this.afosId);
        logger.info("AFOS routing node = " + this.afosNode);

        return noError;
    }

    /**
     * The logic is based on transferNWWS.pl sub createWMOAbbrevHeader()
     * 
     * @return
     */
    public void addAFOSHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.afosId).append(" ").append(this.afosNode).append("\n");

        sb.append("TTAA00 ");
        sb.append(EDEXUtil.getEdexSite()).append(" ");

        // Legacy used date -u
        LocalDateTime dt = LocalDateTime.now(Clock.systemUTC());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddHHmm");
        String storageTime = dt.format(formatter);
        sb.append(storageTime).append("\n");

        // Add AfosHeader in the local product
        sb.append(this.localProductText);
        this.localProductText = sb.toString();
    }

    /**
     * The logic is based on transferNWWS.pl sub mapToAwipsID
     * 
     * @param afosId
     * @return
     */
    public String mapToAwipsID(String afosId) {
        AfosToAwipsDao dao = new AfosToAwipsDao();
        String prodAwipsID = "";
        try {
            List<AfosToAwips> list = dao.lookupWmoId(afosId).getIdList();
            for (AfosToAwips ata : list) {
                String cccc = ata.getWmocccc();
                String awipsId = afosId.substring(3);
                if (afosId.equals(ata.getAfosid())) {
                    prodAwipsID = cccc + awipsId;
                    break;
                }
            }
        } catch (DataAccessLayerException e) {
            String msg = "Failed to excute lookup via AfosToAwipsDao for the AFOS ID: "
                    + this.afosId;
            logger.error(msg);
        }
        return prodAwipsID;
    }

    /**
     * Store sent product in Climate database
     * 
     * @param fileName
     * @param prod
     * @param user
     * @param localProd
     */
    public void recordSentNWWSProduct(String fileName, ClimateProduct prod,
            String user, boolean localProd) {
        ClimateProdSendRecord rec = new ClimateProdSendRecord();
        rec.setProd_id(prod.getPil());
        rec.setPeriod_type(prod.getProdType().name());

        rec.setProd_type("NWWS");
        rec.setFile_name(fileName);

        if (localProd) {
            rec.setProd_text(this.localProductText);
        } else {
            rec.setProd_text(prod.getProdText());
        }

        LocalDateTime sendTime = LocalDateTime.now();
        rec.setSend_time(Timestamp.valueOf(sendTime));
        rec.setUser_id(user);

        // Save a record into the database
        if (dao == null) {
            String expMsg = "DAO is null, can't insert the record: "
                    + rec.getProd_id() + " into DB";
            prod.setStatus(ProductStatus.ERROR);
            prod.setLastAction(ActionOnProduct.SEND, expMsg);

            // Notify users
            session.sendAlertVizMessage(Priority.PROBLEM, expMsg, null);
            logger.error(expMsg);
        }

        try {
            dao.insertSentClimateProdRecord(rec);
        } catch (ClimateQueryException e) {
            String expMsg = "Insert the record: " + rec.getProd_id()
                    + " failed";
            prod.setStatus(ProductStatus.ERROR);
            prod.setLastAction(ActionOnProduct.SEND, expMsg);

            // Notify users
            session.sendAlertVizMessage(Priority.PROBLEM, expMsg, null);
            logger.error(expMsg);
        }
    }

    /**
     * The logic is based on transferNWWS.pl sub storeInTextDB
     * 
     * @param cp
     * @param operational
     */
    public void storeLocalProductInTextDB(ClimateProduct cp,
            boolean operational) {
        TextDB textdb = new TextDB();

        if (cp.getStatus() == ProductStatus.PENDING) {
            // writeProduct will return Long.MIN_VALUE if write failed
            long insertTime = textdb.writeProduct(cp.getPil(),
                    this.localProductText, operational, null);

            if (insertTime != Long.MIN_VALUE) {
                // successfully stored
                String msg = "Storing Climate Products with PIL [" + cp.getPil()
                        + "] to textdb successfully.";

                cp.setStatus(ProductStatus.STORED);
                cp.setLastAction(ActionOnProduct.STORE, msg);

                logger.info(msg);

            } else {
                // Failed to store the product into the Text DB,
                // not throw the exception so user can try late

                String desc = "Store the climate product: " + cp.getPil()
                        + " to TextDB failed";
                cp.setStatus(ProductStatus.ERROR);
                cp.setLastAction(ActionOnProduct.STORE, desc);

                // Notify users
                session.sendAlertVizMessage(Priority.PROBLEM, desc, null);
                logger.error(
                        desc + " ptoduct text: [" + cp.getProdText() + "]");

            }
        }
    }

    /**
     * Use awips2 OUP handler
     * 
     * @param prod
     * @param operational
     * @return
     */
    public boolean forwardToOUP(ClimateProduct prod, boolean operational) {
        boolean noError = false;

        OfficialUserProduct oup = new OfficialUserProduct();
        oup.setFilename(prod.getName());
        oup.setProductText(prod.getProdText());
        String awipsWanPil = mapToAwipsID(this.afosId);
        if (awipsWanPil == null || awipsWanPil.isEmpty()) {
            // Product ID may be not configured
            // Checking fxatext DB, afos_to_awips
            String msg = "Unable to send to OUP, the AFOSId : " + this.afosId
                    + " is not exist in fxatext.afos_to_awips table for the product: "
                    + prod.getPil();
            logger.error(msg);
            session.sendAlertVizMessage(Priority.PROBLEM, msg, null);
            prod.setStatus(ProductStatus.ERROR);
            prod.setLastAction(ActionOnProduct.SEND, msg);
            return noError;
        }

        oup.setAwipsWanPil(awipsWanPil);
        oup.setUserDateTimeStamp(this.ddhhmm);
        oup.setSource("Climate");

        OUPRequest req = new OUPRequest();
        req.setUser(new User(OUPRequest.EDEX_ORIGINATION));
        req.setProduct(oup);
        OUPResponse resp = null;
        String additionalInfo = "";

        try {
            Object object = RequestRouter.route(req);
            if (!(object instanceof SuccessfulExecution)) {
                String msg = "Error transmitting NWWS climate products. Unexpected response class: "
                        + object.getClass().getName();
                logger.error(msg);
                session.sendAlertVizMessage(Priority.PROBLEM, msg, null);
                prod.setStatus(ProductStatus.ERROR);
                prod.setLastAction(ActionOnProduct.SEND, msg);
            } else {
                resp = (OUPResponse) ((SuccessfulExecution) object)
                        .getResponse();

                if (resp.hasFailure()) {
                    // check which kind of failure
                    Priority p = Priority.EVENTA;
                    if (!resp.isAttempted()) {
                        // if was never attempted to send or store even locally
                        p = Priority.CRITICAL;
                        additionalInfo = "ERROR local store never attempted";
                    } else if (!resp.isSendLocalSuccess()) {
                        // if send/store locally failed
                        p = Priority.CRITICAL;
                        additionalInfo = "ERROR store locally failed";
                    } else if (!resp.isSendWANSuccess()) {
                        // if send to WAN failed
                        if (resp.getNeedAcknowledgment()) {
                            // if ack was needed, if it never sent then no ack
                            // was received
                            p = Priority.CRITICAL;
                            additionalInfo = "ERROR send to WAN failed and no acknowledgment received";
                        } else {
                            // if no ack was needed
                            p = Priority.EVENTA;
                            additionalInfo = "WARNING send to WAN failed";
                        }
                    } else if (resp.getNeedAcknowledgment()
                            && !resp.isAcknowledged()) {
                        // if sent but not acknowledged when acknowledgment is
                        // needed
                        p = Priority.CRITICAL;
                        additionalInfo = "ERROR no acknowledgment received";
                    }
                    // Notify user
                    session.sendAlertVizMessage(p, resp.getMessage(),
                            additionalInfo);

                    logger.error(resp.getMessage());

                    prod.setStatus(ProductStatus.ERROR);
                    prod.setStatusDesc(resp.getMessage()
                            + " -- Additional Information: " + additionalInfo);
                    prod.setLastAction(ActionOnProduct.SEND, additionalInfo);
                } else {
                    prod.setStatus(ProductStatus.SENT);
                    noError = true;
                }
            }
        } catch (Exception e) {
            String msg = "Transmit to NWWS failed with exception: "
                    + e.getLocalizedMessage();
            logger.error(msg);
            prod.setStatus(ProductStatus.ERROR);
            prod.setStatusDesc(msg);
            prod.setLastAction(ActionOnProduct.SEND, msg);
            session.sendAlertVizMessage(Priority.PROBLEM, msg, null);
        }

        return noError;
    }

    /**
     * 1) Store the product in the TextDB if the product is a local 2)
     * Otherwise, Forward to OUP to disseminate the product
     * 
     * @param session
     * @param fileName
     * @param prod
     * @param operational
     */
    public static void transmitNWWSProduct(ClimateProdGenerateSession session,
            String fileName, ClimateProduct prod, boolean operational,
            String user) {

        ClimateProductNWWSSender cpns = new ClimateProductNWWSSender(session);

        // Remove the communication header in the product
        if (!cpns.processCommsHeader(prod)) {
            // Fatal error, just return
            return;
        }

        // extract afosId, afosNode, etc
        if (!cpns.extractAfosInfo(prod)) {
            // Fatal error, just return
            return;
        }

        // Process local use only product
        if (cpns.afosNode.equals("000") || cpns.afosNode.equals("LOC")) {
            logger.info(
                    "This is a local product, AFOS originating site (CCC) = "
                            + cpns.getAfosId().substring(0, 3));

            // Add a AFOS message header at the top of product text
            cpns.addAFOSHeader();

            // Store in TextDB
            cpns.storeLocalProductInTextDB(prod, operational);

            // Record sent product
            cpns.recordSentNWWSProduct(fileName, prod, user, true);

            if (prod.getStatus() == ProductStatus.STORED) {
                // For local product, treat it as SENT
                prod.setStatus(ProductStatus.SENT);
            }

            // Complete sending
            return;
        }

        // Not local product, forward to OUP handler
        if (cpns.forwardToOUP(prod, operational)) {
            // Record sent product
            cpns.recordSentNWWSProduct(fileName, prod, user, false);
        }

    }

    /**
     * @return the commsHeader
     */
    public String getCommsHeader() {
        return commsHeader;
    }

    /**
     * @param commsHeader
     *            the commsHeader to set
     */
    public void setCommsHeader(String commsHeader) {
        this.commsHeader = commsHeader;
    }

    /**
     * @return the localProductText
     */
    public String getLocalProductText() {
        return localProductText;
    }

    /**
     * @param localProductText
     *            the localProductText to set
     */
    public void setLocalProductText(String localProductText) {
        this.localProductText = localProductText;
    }

    /**
     * @return the afosId
     */
    public String getAfosId() {
        return afosId;
    }

    /**
     * @param afosId
     *            the afosId to set
     */
    public void setAfosId(String afosId) {
        this.afosId = afosId;
    }

    /**
     * @return the afosNode
     */
    public String getAfosNode() {
        return afosNode;
    }

    /**
     * @param afosNode
     *            the afosNode to set
     */
    public void setAfosNode(String afosNode) {
        this.afosNode = afosNode;
    }

    /**
     * @return the awipsSiteId
     */
    public String getAwipsSiteId() {
        return awipsSiteId;
    }

    /**
     * @param awipsSiteId
     *            the awipsSiteId to set
     */
    public void setAwipsSiteId(String awipsSiteId) {
        this.awipsSiteId = awipsSiteId;
    }

    /**
     * @return the dao
     */
    public ClimateProdSendRecordDAO getDao() {
        return dao;
    }

    /**
     * @param dao
     *            the dao to set
     */
    public void setDao(ClimateProdSendRecordDAO dao) {
        this.dao = dao;
    }

    /**
     * @return the wmoTtaaii
     */
    public String getWmoTtaaii() {
        return wmoTtaaii;
    }

    /**
     * @param wmoTtaaii
     *            the wmoTtaaii to set
     */
    public void setWmoTtaaii(String wmoTtaaii) {
        this.wmoTtaaii = wmoTtaaii;
    }

    /**
     * @return the wmoCccc
     */
    public String getWmoCccc() {
        return wmoCccc;
    }

    /**
     * @param wmoCccc
     *            the wmoCccc to set
     */
    public void setWmoCccc(String wmoCccc) {
        this.wmoCccc = wmoCccc;
    }

    /**
     * @return the ddhhmm
     */
    public String getDdhhmm() {
        return ddhhmm;
    }

    /**
     * @param ddhhmm
     *            the ddhhmm to set
     */
    public void setDdhhmm(String ddhhmm) {
        this.ddhhmm = ddhhmm;
    }

    /**
     * @return the session
     */
    public ClimateProdGenerateSession getSession() {
        return session;
    }

}
