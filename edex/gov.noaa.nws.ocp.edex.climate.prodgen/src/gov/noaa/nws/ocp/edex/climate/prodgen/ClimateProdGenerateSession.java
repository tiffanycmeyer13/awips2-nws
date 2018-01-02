/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.serialization.DynamicSerializationManager;
import com.raytheon.uf.common.serialization.DynamicSerializationManager.SerializationType;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.plugin.text.db.TextDB;

import gov.noaa.nws.ocp.common.dataplugin.climate.ActionOnProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdData;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdGenerateSessionData;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct.ProductStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductSet;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.ProductSetStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.StateStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.StateStatus.Status;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateSessionException;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.DisplayClimateResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.SendClimateProductsResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateMessageUtils;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductTypeManager;
import gov.noaa.nws.ocp.edex.climate.creator.ClimateCreator;
import gov.noaa.nws.ocp.edex.climate.formatter.ClimateFormatter;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.CheckResult;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.DefinedDataQualityCheck;
import gov.noaa.nws.ocp.edex.climate.prodgen.transmit.ClimateProductNWWSSender;
import gov.noaa.nws.ocp.edex.climate.prodgen.transmit.NWRProductForwarder;
import gov.noaa.nws.ocp.edex.climate.prodgen.util.ClimateProdSetting;
import gov.noaa.nws.ocp.edex.climate.prodgen.util.RunType;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateAlertUtils;

/**
 * ClimateProdGenerateSession A CPG Session represents a State Machine for
 * generating one Climate Product process.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 2, 2016  20637      pwang       Initial creation
 * MAR 21 2017  30166      amoore      Integration with Display. Remove
 *                                     unneeded field.
 * Apr 18 2017  30166      pwang       Error handling for sending NWWS and NWR products
 * May 05, 2017 34137      pwang       Re-designed status and error handling
 * Jun 02, 2017 34783      pwang       Add logic to control auto send, NWR copyTo, and if really dissemination
 * Jul 18, 2017 33104      amoore      Send Climate alerts if dissemination is set to false.
 * Jul 26, 2017 33104      amoore      Address review comments.
 * Aug 21, 2017 33104      amoore      Better logging messages on failure.
 * Sep 06, 2017 37721      amoore      Failure on Display finalization should fail CPG session
 * Oct 10, 2017 39153      amoore      Less action-blocking from dissemination flag.
 * Nov 03, 2017 36749      amoore      Remove redundant AlertViz method.
 * Nov 06, 2017 36706      amoore      Address issue of all Climate AlertViz notifications showing up
 *                                     as errors. This was due to lack of "CLIMATE" category definition
 *                                     in AlertViz, which is pushed as separate task. Reorg of constants
 *                                     and Alert levels.
 * Nov 07, 2017 35729      pwang       Added logic to support site defined QC check for auto cli generation
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public final class ClimateProdGenerateSession {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerateSession.class);

    private final static int DEFAULT_DIS_WAIT = 20;

    private final static int DEFAULT_REV_WAIT = 10;

    // DAO
    private ClimateProdGenerateSessionDAO dao;

    // Attributes need to be stored in Climate Database
    private String cpgSessionId;

    private RunType runType;

    private PeriodType prodType;

    // CPG Session Workflow state
    private SessionState state;

    // CPG Session State's stateStatus
    private StateStatus stateStatus;

    // global configuration
    private ClimateGlobal globalConfig;

    // setting parameters
    private ClimateProdSetting prodSetting;

    // all report data
    private ClimateRunData reportData = null;

    // Climate Product Data created by ClimateFormatter
    private ClimateProdData prodData = null;

    private LocalDateTime startedAt;

    private LocalDateTime lastUpdated;

    // Session will expired even not at terminate stateStatus
    private LocalDateTime pendingExpiration;

    /**
     * Constructor Create a new CPG session
     * 
     * @param dao
     * @param runTypeValue
     *            valid value will be 1 or 2
     * @param prodTypeValue
     *            valid value will be
     */
    public ClimateProdGenerateSession(ClimateProdGenerateSessionDAO dao,
            int runTypeValue, int prodTypeValue) {
        this.dao = dao;

        this.setRunTypeFromValue(runTypeValue);
        this.setProdTypeFromValue(prodTypeValue);
        this.state = SessionState.STARTED;
        this.stateStatus = new StateStatus(Status.WORKING);

        initNewCPGSession();
    }

    /**
     * 
     * @param dao
     * @param cpgSessionId
     */
    public ClimateProdGenerateSession(ClimateProdGenerateSessionDAO dao,
            String cpgSessionId) {
        this.dao = dao;

        ClimateProdGenerateSessionData sessionData = null;

        try {
            sessionData = dao.getCPGSession(cpgSessionId);
        } catch (ClimateQueryException e) {
            logger.error("Retrieve CPG Session data from database failed! ", e);
            failCPGSession("Failed to retrieve session data from the database");
        }

        if (sessionData == null) {
            logger.error("No CPG Session exists for the Session ID: "
                    + cpgSessionId);
            return;
        }

        this.cpgSessionId = sessionData.getCpg_session_id();
        this.setRunTypeFromValue(sessionData.getRun_type());
        this.setProdTypeFromValue(sessionData.getProd_type());
        this.setStateFromValue(sessionData.getState());
        this.stateStatus = new StateStatus(sessionData.getStatus(),
                sessionData.getStatus_desc());

        // deserialize staved objects
        try {
            // GlobalConfig
            this.globalConfig = (ClimateGlobal) DynamicSerializationManager
                    .getManager(SerializationType.Thrift)
                    .deserialize(sessionData.getGlobal_config());

            // Settings
            this.prodSetting = (ClimateProdSetting) DynamicSerializationManager
                    .getManager(SerializationType.Thrift)
                    .deserialize(sessionData.getProd_setting());

            // Created ReportData
            if (null == sessionData.getReport_data()) {
                this.reportData = null;
            } else {
                this.reportData = (ClimateRunData) DynamicSerializationManager
                        .getManager(SerializationType.Thrift)
                        .deserialize(sessionData.getReport_data());
            }

            // Formatted ProdData
            if (null == sessionData.getProd_data()) {
                this.prodData = null;
            } else {
                this.prodData = (ClimateProdData) DynamicSerializationManager
                        .getManager(SerializationType.Thrift)
                        .deserialize(sessionData.getProd_data());
            }

            this.startedAt = sessionData.getStart_at().toLocalDateTime();
            this.lastUpdated = sessionData.getLast_updated().toLocalDateTime();
            this.pendingExpiration = sessionData.getPending_expire()
                    .toLocalDateTime();

        } catch (SerializationException se) {
            logger.error("Deserialization objects failed ", se);
            failCPGSession("Failed to deserialize object to byte array !");
        }
    }

    /**
     * Convert CPG Session Data into a serializable object for store in database
     * 
     * @return
     */
    public ClimateProdGenerateSessionData getClimateProdGenerateSessionData() {
        ClimateProdGenerateSessionData sessionData = new ClimateProdGenerateSessionData();
        sessionData.setCpg_session_id(this.cpgSessionId);
        sessionData.setRun_type(this.runType.getValue());
        sessionData.setProd_type(this.prodType.getValue());
        sessionData.setState(this.state.getValue());
        sessionData.setStatus(this.stateStatus.getStatus().getValue());
        sessionData.setStatus_desc(this.stateStatus.getDescription());
        sessionData.setStart_at(Timestamp.valueOf(this.startedAt));
        sessionData.setLast_updated(Timestamp.valueOf(this.lastUpdated));
        sessionData
                .setPending_expire(Timestamp.valueOf(this.pendingExpiration));

        try {
            // GlobalConfig is not null
            sessionData.setGlobal_config(DynamicSerializationManager
                    .getManager(SerializationType.Thrift)
                    .serialize(this.globalConfig));

            // ClimateProdSetting is not null
            sessionData.setProd_setting(DynamicSerializationManager
                    .getManager(SerializationType.Thrift)
                    .serialize(this.prodSetting));

            // ReportData created by ClimateCreater
            if (null == this.reportData) {
                sessionData.setReport_data(null);
            } else {
                sessionData.setReport_data(DynamicSerializationManager
                        .getManager(SerializationType.Thrift)
                        .serialize(this.reportData));
            }

            // ReportData created by ClimateCreater
            if (null == this.prodData) {
                sessionData.setProd_data(null);
            } else {
                sessionData.setProd_data(DynamicSerializationManager
                        .getManager(SerializationType.Thrift)
                        .serialize(this.prodData));
            }

        } catch (SerializationException se) {
            logger.error("Serialization failed ", se);
            sessionData = null;
        }
        return sessionData;
    }

    /**
     * 
     * @return
     */
    public String getCPGSessionId() {
        return cpgSessionId;
    }

    /**
     * send message to the AlertViz
     * 
     * @param desc
     *            description about the message
     * @param msgBody
     *            formatted message contains details
     */
    public static void sendAlertVizMessage(Priority priority, String desc,
            String msgBody) {

        EDEXUtil.sendMessageAlertViz(priority,
                ClimateMessageUtils.CPG_PLUGIN_ID,
                ClimateAlertUtils.SOURCE_EDEX,
                ClimateAlertUtils.CATEGORY_CLIMATE, desc, msgBody, null);
    }

    /**
     * sendAlertMessage
     * 
     * @param desc
     * @param moreDetails
     */
    public void sendClimateNotifyMessage(String desc,
            Map<String, String> moreDetails) {

        StringBuilder sb = new StringBuilder();
        sb.append("ID=").append(this.getCPGSessionId()).append(",");
        sb.append("STATE=").append(this.state).append(",");
        sb.append("STATUS_CODE=").append(this.stateStatus.getStatus())
                .append(",");
        sb.append("STATUS_DESC=").append(this.stateStatus.getDescription())
                .append(",");
        sb.append("LAST_UPDATED=").append(this.lastUpdated.toString())
                .append(",");

        if (moreDetails != null && !moreDetails.isEmpty()) {
            moreDetails.forEach(
                    (k, v) -> sb.append(k).append("=").append(v).append(","));
        }

        // Just remove ending comma
        String detailMsg = sb.toString().substring(0, sb.length() - 1);

        StatusMessage sm = new StatusMessage();
        sm.setPriority(Priority.INFO);
        sm.setPlugin(ClimateMessageUtils.CPG_PLUGIN_ID);
        sm.setCategory(ClimateAlertUtils.CATEGORY_CLIMATE);
        sm.setMessage(desc);
        sm.setMachineToCurrent();
        sm.setSourceKey(ClimateAlertUtils.SOURCE_EDEX);
        sm.setDetails(detailMsg);
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer()
                    .sendAsync(ClimateAlertUtils.CPG_ENDPOINT, sm);
        } catch (Exception e) {
            logger.error("Could not send message to ClimateView", e);
        }

    }

    /**
     * Send a countdown message, waiting for user interaction.
     * 
     * @param totalSeconds
     * @param secndsPassed
     */
    public void sendTimeCountdownMessage(final int totalSeconds,
            int secondsPassed) {

        String desc = "Timer countdown ...";
        StringBuilder msgBody = new StringBuilder();
        msgBody.append("ID=").append(this.getCPGSessionId()).append(",");
        msgBody.append("STATE=").append(this.state).append(",");
        msgBody.append("TOTAL_SECONDS=").append(totalSeconds).append(",");
        msgBody.append("SECONDS_PASSED=").append(secondsPassed).append(",");
        double percent = (secondsPassed * 100) / totalSeconds;
        BigDecimal rpercent = new BigDecimal(percent).setScale(2,
                RoundingMode.HALF_UP);
        msgBody.append("PERCENT_PASSED=").append(rpercent);

        StatusMessage sm = new StatusMessage();
        sm.setPriority(Priority.INFO);
        sm.setPlugin(ClimateMessageUtils.CPG_PLUGIN_ID);
        sm.setCategory(ClimateAlertUtils.CATEGORY_CLIMATE);
        sm.setMessage(desc);
        sm.setMachineToCurrent();
        sm.setSourceKey(ClimateAlertUtils.SOURCE_EDEX);
        sm.setDetails(msgBody.toString());
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer()
                    .sendAsync(ClimateAlertUtils.CPG_ENDPOINT, sm);
        } catch (Exception e) {
            logger.error("Could not send message to ClimateView");
        }
    }

    /**
     * is designated to be called by cron job. Automatically run Creator.
     * 
     */
    public void autoCreateClimate() {
        try {
            this.executeCreateClimate();
        } catch (ClimateSessionException e) {
            logger.error("Failed to execute createClimate", e);
            failCPGSession("Failed to execute createClimate !");
            return;
        }

        // QC check
        DefinedDataQualityCheck qcChecker = new DefinedDataQualityCheck(
                this.prodType);

        CheckResult cr = null;

        try {
            cr = qcChecker.check(reportData);
        } catch (Exception e) {
            String msg = "Failed attempting QC check with exception: "
                    + e.getMessage();
            logger.error(msg, e);
            failCPGSession(msg);
            return;
        }

        if (!cr.isPassed()) {
            /*
             * Could not pass the site defined QC check for auto climate product
             * generation. Fail session and advise manual generation.
             */
            String msg = "Failed defined QC check due to at least: "
                    + cr.getDetails() + "Manual generation required.";
            logger.warn(msg);
            failCPGSession(msg);
            return;
        } else {
            logger.debug(
                    "Passed all QC checks. Details: [" + cr.getDetails() + "]");
        }

        // Check if cancelled
        if (this.getCurrentStatus().getStatus()
                .equals(StateStatus.Status.CANCELLED)) {
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " has been cancelled");
            return;
        }

        // Alert User for Display
        String msgDesc = "Climate Report Created, waiting for display. CPG Session ID = "
                + this.cpgSessionId;
        sendAlertVizMessage(Priority.INFO, msgDesc, "");

        int dispWait = this.globalConfig.getDisplayWait();
        if (dispWait < 0) {
            dispWait = DEFAULT_DIS_WAIT;
        }
        // Min to seconds
        dispWait *= 60;
        int secondsLeft = dispWait;

        Object waiter = new Object();
        synchronized (waiter) {
            while (!this.getCurrentStatus().getStatus()
                    .equals(StateStatus.Status.CANCELLED)
                    && this.getCurrentState() != SessionState.DISPLAY
                    && secondsLeft-- > 0) {
                // send time countdown info to the client
                this.sendTimeCountdownMessage(dispWait, dispWait - secondsLeft);

                try {
                    waiter.wait(1000);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }

        // Check if cancelled
        if (this.getCurrentStatus().getStatus()
                .equals(StateStatus.Status.CANCELLED)) {
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " has been cancelled");
            return;
        }

        // Refresh state from DB
        if (this.getCurrentState() == SessionState.DISPLAY
                || this.state == SessionState.DISPLAYED) {
            // User participated before timeout and reviewed
            // exit, hand the control to user
            return;
        } else if (secondsLeft <= 0) {
            // timed out, move on and calling HeadlessDispaly
            try {
                this.executeHeadlessDisplayCimate();
            } catch (ClimateSessionException e) {
                logger.error("Failed to execute Headless Display", e);
                failCPGSession("Failed to execute Headless Display! "
                        + e.getMessage());
                return;
            }

            try {
                this.executeFormatClimate();
            } catch (ClimateSessionException e) {
                logger.error("Failed to execute Format Climate", e);
                failCPGSession(
                        "Failed to execute Format Climate! " + e.getMessage());
                return;
            }
        }

        // Check if cancelled
        if (this.getCurrentStatus().getStatus()
                .equals(StateStatus.Status.CANCELLED)) {
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " has been cancelled");
            return;
        }

        // Alert User for review
        msgDesc = "Formatted Climate Product generated, waiting for review. CPG Session ID = "
                + this.cpgSessionId;
        sendAlertVizMessage(Priority.INFO, msgDesc, "");

        int revWait = this.globalConfig.getReviewWait();
        if (revWait < 0) {
            revWait = DEFAULT_REV_WAIT;
        }
        // Minutes to seconds
        revWait *= 60;
        secondsLeft = revWait;

        Object waiter2 = new Object();
        synchronized (waiter2) {
            while (!this.getCurrentStatus().getStatus()
                    .equals(StateStatus.Status.CANCELLED)
                    && this.getCurrentState() != SessionState.REVIEW
                    && secondsLeft-- > 0) {

                // send time countdown info to the client
                this.sendTimeCountdownMessage(revWait, revWait - secondsLeft);

                try {
                    waiter2.wait(1000);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }

        // Check if cancelled
        if (this.getCurrentStatus().getStatus()
                .equals(StateStatus.Status.CANCELLED)) {
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " has been cancelled");
            return;
        }

        // Refresh state from DB
        if (this.getCurrentState() == SessionState.REVIEW
                || this.state == SessionState.PENDING) {
            // User participated before timeout and reviewed
            // exit, hand the control to user
            return;
        } else if (secondsLeft <= 0) {
            // Timed out without user response
            // Go ahead to send the product
            try {
                this.autoSendClimateProducts();
            } catch (Exception e) {
                logger.error("Send climate products for "
                        + this.getCPGSessionId() + " failed!", e);
                failCPGSession("Send climate products for "
                        + this.getCPGSessionId() + " failed!");
            }
        }

    }

    /**
     * A user-initiated run of Creator for a daily product type.
     * 
     * @param date
     * @param nonRecentRun
     * @return
     */
    public void manualCreateDailyClimate(ClimateDate date, boolean nonRecentRun)
            throws Exception {
        try {
            this.executeCreateDailyClimate(date, nonRecentRun);
        } catch (ClimateSessionException e) {
            logger.error("Failed to execute Create Daily Climate", e);
            failCPGSession("Failed to execute Create Daily Climate!");
            throw new Exception("Failed to execute Create Daily Climate", e);
        }
    }

    /**
     * A user-initiated run of Creator for a period product type.
     * 
     * @param begin
     * @param end
     * @param nonRecentRun
     * @return
     */
    public void manualCreatePeriodClimate(ClimateDate begin, ClimateDate end,
            boolean nonRecentRun) throws Exception {
        try {
            this.executeCreatePeriodClimate(begin, end, nonRecentRun);
        } catch (ClimateSessionException e) {
            logger.error("Failed to execute Create Period Climate", e);
            failCPGSession("Failed to execute Create Period Climate!");
            throw new Exception("Failed to execute Create Period Climate", e);
        }
    }

    /**
     * is designated to be called by request handler. User completed Display.
     * 
     * @param ptype
     */
    public ClimateProdData manualFormatClimate(PeriodType ptype) {
        String msgDesc = "";

        if (this.getCurrentState() != SessionState.DISPLAYED) {
            // Something is wrong
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " must be DISPLAYED at this point");
            msgDesc = "The CPG session state: " + this.state + " is not valid";
            Map<String, String> errors = new HashMap<>();
            errors.put("ERROR", "Wrong State");
            sendClimateNotifyMessage(msgDesc, errors);
            return null;
        } else {
            try {
                this.executeFormatClimate();
            } catch (ClimateSessionException e) {
                logger.error("Failed to execute Format Climate", e);
                failCPGSession(
                        "Failed to execute Format Climate! " + e.getMessage());
                return null;
            }
        }

        return this.prodData;
    }

    /**
     * Cancel this session.
     * 
     * @param who
     * @param reason
     * @return
     */
    public int cancelCPGSession(String who, String reason) {

        if (isSessionTerminated()) {
            // Can not cancel for terminated session
            return this.stateStatus.getStatus().getValue();
        }

        // Update session state to cancelled
        this.updateStateStatus(StateStatus.Status.CANCELLED, reason);

        // Send a message to inform other client who may also access same
        // session
        String msgDesc = "Warning: the user " + who + " cancelled the session: "
                + this.getCPGSessionId();

        // Alert user for cancel the session
        sendAlertVizMessage(Priority.WARN, msgDesc, "");

        // Send notification to CPG Viewer
        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "CANCEL");
        sendClimateNotifyMessage(msgDesc, actions);

        return this.stateStatus.getStatus().getValue();
    }

    /**
     * A user has started Display.
     * 
     * @param who
     * @return
     */
    public DisplayClimateResponse startDisplayReportData(String who) {
        // Move the state to DISPLAY
        if (this.state == SessionState.CREATED) {
            // The state may be already DISPLAY
            setState(SessionState.DISPLAY);
        }

        // Send a message to inform other client who may also access same
        // session
        String msgDesc = "The user " + who
                + " try to display/modify the climate report data";

        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "DISPLAY");
        sendClimateNotifyMessage(msgDesc, actions);

        DisplayClimateResponse response = new DisplayClimateResponse(
                this.cpgSessionId);
        response.setGlobalConfig(this.globalConfig);
        response.setReportData(this.reportData);

        return response;
    }

    /**
     * A user has started Review.
     * 
     * @param prodType
     * @param who
     * @return
     */
    public Map<String, ClimateProduct> startReviewProdData(
            ClimateProductType prodType, String who) {
        // Move the state to DISPLAY
        if (this.state == SessionState.FORMATTED) {
            // the state may be REVIEW already
            setState(SessionState.REVIEW);
        }

        // Send a message to inform other client who may also access same
        // session
        String msgDesc = "The user " + who
                + " try to review/modify the climate product data";
        Map<String, String> actions = new HashMap<>();
        actions.put("USER", who);
        actions.put("ACTION", "REVIEW");
        sendClimateNotifyMessage(msgDesc, actions);

        if (prodType == ClimateProductType.NWWS) {
            return this.prodData.getNwwsProducts();
        }

        return this.prodData.getNwrProducts();
    }

    /**
     * initialize a new CPG session. Set to FAILED if fatal exception occurred
     */
    private void initNewCPGSession() {
        // Load global configuration
        this.globalConfig = ClimateGlobalConfiguration.getGlobal();

        // Load settings for the prodType
        ClimateProductTypeManager cptManager = ClimateProductTypeManager
                .getInstance();
        this.prodSetting = new ClimateProdSetting(
                cptManager.getTypesByPeriodName(this.prodType.getPeriodName()));

        // Assign an ID and add this session to the session map
        LocalDateTime now = LocalDateTime.now();
        this.cpgSessionId = runType + "-"
                + prodType.getPeriodName().toUpperCase() + "-" + now.toString();

        // Init date time
        this.startedAt = LocalDateTime.now();
        this.lastUpdated = startedAt;
        this.pendingExpiration = startedAt;

        try {
            // Persist the session data
            if (dao != null) {
                dao.saveNewCPGSessionData(
                        this.getClimateProdGenerateSessionData());

                // sendMessage to client
                String msgDesc = "A new CPG Session is started";
                String msgDetail = "ID=" + this.getCPGSessionId()
                        + ", STATE=START";
                // Notify to AlertViz
                sendAlertVizMessage(Priority.INFO, msgDesc, msgDetail);

                // Notify to CPG View
                Map<String, String> actions = new HashMap<>();
                actions.put("ACTION", "NEW");
                sendClimateNotifyMessage(msgDesc, actions);
            } else {
                logger.error(
                        "DAO is null, this session can ot be initialized ");
                failCPGSession("Not able to access database, DAO is null");
            }
        } catch (ClimateQueryException qe) {
            logger.error(
                    "Failed to save the saseion data into the Climate database ",
                    qe);
            failCPGSession(
                    "Failed to save the saseion data into the Climate database");
        }

    }

    /**
     * Set CPG Session to FAILED when fatal error occurred
     * 
     * @param reason
     */
    private void failCPGSession(String reason) {
        /*
         * Set the session failed and update database. Send information to CPG
         * View.
         */
        this.updateStateStatus(StateStatus.Status.FAILED, reason);
        // Send AlertViz message
        sendAlertVizMessage(Priority.PROBLEM,
                "CPG Session: [" + cpgSessionId + "] has failed!", reason);
    }

    /**
     * 
     * 
     * @param value
     */
    private void setRunTypeFromValue(int value) {
        for (RunType rType : RunType.values()) {
            if (rType.getValue() == value) {
                this.runType = rType;
                return;
            }
        }
        // Invalid value
        this.runType = RunType.UNKNOWN;
        logger.error("Invalid RunType, it must either be 1 or 2");
        failCPGSession("Invalid RunType, it must either be 1 or 2");
    }

    /**
     * 
     * 
     * @param value
     *            Valid values: 1, 2, 5, 7, 9, 10
     */
    private void setProdTypeFromValue(int value) {
        for (PeriodType pType : PeriodType.values()) {
            if (pType.getValue() == value) {
                this.prodType = pType;
                return;
            }
        }
        // Invalid value
        this.prodType = PeriodType.OTHER;
        logger.error(
                "Invalid PeriodType number, it must be 1, 2, 5, 7, 9 or 10");
        failCPGSession(
                "Invalid PeriodType number, it must be 1, 2, 5, 7, 9 or 10");
    }

    /**
     * 
     * 
     * @param value
     */
    public void setStateFromValue(int value) {
        for (SessionState cstate : SessionState.values()) {
            if (cstate.getValue() == value) {
                this.state = cstate;
                return;
            }
        }
        // Invalid value
        this.state = SessionState.UNKNOWN;
    }

    /**
     * Next State of CPG Session
     * 
     * @return
     */
    public SessionState nextState() {
        int nextValue = this.state.getValue();
        if (nextValue < SessionState.SENT.getValue()) {
            nextValue++;
        }
        for (SessionState cstate : SessionState.values()) {
            if (cstate.getValue() == nextValue) {
                return cstate;
            }
        }
        // Invalid value
        return SessionState.UNKNOWN;

    }

    /**
     * Run Creator.
     * 
     * @throws Exception
     */
    private void executeCreateClimate() throws ClimateSessionException {

        ClimateCreator creator = new ClimateCreator();
        ClimateRunData report;
        try {
            report = creator.createClimate(prodType);

        } catch (Exception e) {
            this.failCPGSession("Failed to execute createClimate!");
            throw new ClimateSessionException(
                    "Failed to execute createClimate!", e);
        }

        if (report != null) {
            try {
                // Update DB with newly created report data
                this.setReportDataAndUpdateDatabase(report);
            } catch (Exception e) {
                throw new ClimateSessionException(
                        "Failed to save created report data into the database",
                        e);
            }
        } else {
            // Report data is not created
            throw new ClimateSessionException(
                    "The climate report data was not created");
        }

        // Create succeed
        this.setState(SessionState.CREATED);
    }

    /**
     * Run Daily Creator.
     * 
     * @param date
     * @param manualNonRecentRun
     * @throws Exception
     */
    private void executeCreateDailyClimate(ClimateDate date,
            boolean manualNonRecentRun) throws ClimateSessionException {

        ClimateCreator creator = new ClimateCreator();
        ClimateRunData report;
        try {
            report = creator.createClimate(manualNonRecentRun, this.prodType,
                    date);

        } catch (Exception e) {
            throw new ClimateSessionException(
                    "Failed to execute createClimate!", e);
        }
        if (report != null) {
            try {
                // Update DB with newly created report data
                this.setReportDataAndUpdateDatabase(report);
            } catch (Exception e) {
                throw new ClimateSessionException(
                        "Failed to save created report data into the database",
                        e);
            }
        } else {
            // Report data is not created
            throw new ClimateSessionException(
                    "The climate report data was not created");
        }

        // Create succeed
        this.setState(SessionState.CREATED);
    }

    /**
     * Run Period Creator.
     * 
     * @param begin
     * @param end
     * @param manualNonRecentRun
     * @throws Exception
     */
    private void executeCreatePeriodClimate(ClimateDate begin, ClimateDate end,
            boolean manualNonRecentRun) throws ClimateSessionException {

        ClimateCreator creator = new ClimateCreator();
        ClimateRunData report;
        try {
            report = creator.createClimate(manualNonRecentRun, this.prodType,
                    begin, end);

        } catch (Exception e) {
            throw new ClimateSessionException(
                    "Failed to execute createClimate!", e);
        }

        if (report != null) {
            try {
                // Update DB with newly created report data
                this.setReportDataAndUpdateDatabase(report);
            } catch (Exception e) {
                throw new ClimateSessionException(
                        "Failed to save created report data into the database",
                        e);
            }
        } else {
            // Report data is not created
            throw new ClimateSessionException(
                    "The climate report data was not created");
        }

        // Create succeed
        this.setState(SessionState.CREATED);
    }

    /**
     * Execute Formatter.
     * 
     * @throws Exception
     */
    private void executeFormatClimate() throws ClimateSessionException {

        ClimateFormatter formatter = new ClimateFormatter(this.globalConfig,
                this.prodSetting.getProdSettingList());

        ClimateProdData prod;
        Map<String, ClimateProduct> cpMap;
        try {
            // TODO: determine operational flag

            cpMap = formatter.formatClimate(this.reportData/* , operational */);
        } catch (Exception e) {
            this.failCPGSession("Failed to execute formatClimate!");
            throw new ClimateSessionException(
                    "Failed to execute formatClimate!", e);
        }
        if (cpMap != null && !cpMap.isEmpty()) {
            prod = new ClimateProdData(cpMap);

            // Initialize pending expiration datetime
            this.pendingExpiration = prod.getMaxExpiration();

            // Update database with newly formatted products
            this.setProdDataAndUpdateDatabase(prod);
        } else {
            throw new ClimateSessionException(
                    "The climate product data was not generated by Formatter."
                            + " Ensure that products were defined for type: ["
                            + prodType.getPeriodName() + "] in Set Up Params.");
        }
        // Create succeed
        this.setState(SessionState.FORMATTED);
    }

    /**
     * Automatically run Display processing without user.
     */
    private void executeHeadlessDisplayCimate() throws ClimateSessionException {
        try {
            if (this.prodType.isDaily()) {
                DailyClimateDAO dcDao = new DailyClimateDAO();
                ClimateRunDailyData ccdr = (ClimateRunDailyData) this.reportData;
                dcDao.processDisplayFinalization(this.prodType,
                        ccdr.getBeginDate(), ccdr.getReportMap());
            } else {
                // Period
                ClimatePeriodDAO cpDao = new ClimatePeriodDAO();
                ClimateRunPeriodData ccpr = (ClimateRunPeriodData) this.reportData;
                cpDao.processDisplayFinalization(this.prodType,
                        ccpr.getBeginDate(), ccpr.getEndDate(),
                        ccpr.getReportMap());
            }
        } catch (ClimateInvalidParameterException e) {
            String msgDesc = "Called processDisplayFinalization with invalid parameter(s)";
            this.failCPGSession(msgDesc);
            throw new ClimateSessionException(msgDesc, e);
        } catch (ClimateSessionException e) {
            String msgDesc = "Failed to execute Display finalization";
            failCPGSession(msgDesc);
            throw new ClimateSessionException(msgDesc, e);
        }

        // Create succeed
        this.setState(SessionState.DISPLAYED);

    }

    /**
     * Run finalization for daily Display step.
     * 
     * @param dataMap
     * @param msmOverwriteApproved
     * @throws ClimateSessionException
     */
    public void finalizeDisplayDailyCimate(ClimateRunDailyData userData)
            throws ClimateSessionException {

        try {
            DailyClimateDAO dcDao = new DailyClimateDAO();
            dcDao.processDisplayFinalization(this.prodType,
                    userData.getBeginDate(), userData.getReportMap());
        } catch (Exception e) {
            String msgDesc = "Called processDisplayFinalization with invalid parameter(s)";
            this.failCPGSession(msgDesc);
            throw new ClimateSessionException(msgDesc, e);
        }

        // Create succeed
        this.setState(SessionState.DISPLAYED);

    }

    /**
     * Run finalization for period Display step.
     * 
     * @param dataMap
     * @param userData
     * @param msmOverwriteApproved
     * @throws ClimateSessionException
     */
    public void finalizeDisplayPeriodCimate(
            Map<Integer, ClimatePeriodReportData> dataMap,
            ClimateRunPeriodData userData, Set<Integer> msmOverwriteApproved)
                    throws ClimateSessionException {

        try {
            ClimatePeriodDAO cpDao = new ClimatePeriodDAO();
            cpDao.processDisplayFinalization(this.prodType,
                    userData.getBeginDate(), userData.getEndDate(), dataMap,
                    userData.getReportMap(), msmOverwriteApproved);
        } catch (ClimateInvalidParameterException e) {
            String msgDesc = "Called processDisplayFinalization with invalid parameter(s)";
            this.failCPGSession(msgDesc);
            throw new ClimateSessionException(msgDesc, e);
        } catch (ClimateSessionException e) {
            String msgDesc = "Error with Display finalization.";
            this.failCPGSession(msgDesc);
            throw new ClimateSessionException(msgDesc, e);
        }

        // Create succeed
        this.setState(SessionState.DISPLAYED);

    }

    /**
     * @return the runType
     */
    public RunType getRunType() {
        return runType;
    }

    /**
     * @param runType
     *            the runType to set
     */
    public void setRunType(RunType runType) {
        this.runType = runType;
    }

    /**
     * @return the prodType
     */
    public PeriodType getProdType() {
        return prodType;
    }

    /**
     * @param prodType
     *            the prodType to set
     */
    public void setProdType(PeriodType prodType) {
        this.prodType = prodType;
    }

    /**
     * @return the state
     */
    public SessionState getState() {
        return state;
    }

    /**
     * 
     * 
     * Change to a new state need to reset the stateStatus to WORKING
     * 
     * @param state
     *            the state to set
     */
    public void setState(SessionState state) {

        // Change to new state
        this.state = state;
        // Update lastUpdated;
        lastUpdated = LocalDateTime.now();

        String msg = this.state.name() + " Next: " + this.nextState();
        this.stateStatus = new StateStatus(StateStatus.Status.SUCCESS, msg);

        // Update session in the database
        try {
            // Update state in db
            dao.updateCPGSessionState(this.cpgSessionId, this.state.getValue(),
                    this.lastUpdated);

            // Update stateStatus accordingly
            this.updateStateStatus(this.stateStatus.getStatus(), msg);

        } catch (Exception e) {
            logger.error("Update Session State into database failed", e);
        }

        String msgDesc = "The CPG session state will update to: " + this.state;
        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "Change State");
        sendClimateNotifyMessage(msgDesc, actions);
    }

    /**
     * Update stateStatus.
     * 
     * @param stateStatus
     * @param desc
     */
    public void updateStateStatus(StateStatus.Status status, String desc) {

        this.stateStatus = new StateStatus(status, desc);

        // Update lastUpdated;
        lastUpdated = LocalDateTime.now();

        // Update session's stateStatus in the database
        try {
            dao.updateCPGSessionStatus(this.cpgSessionId,
                    this.stateStatus.getStatus().getValue(),
                    this.stateStatus.getDescription(), this.lastUpdated);
        } catch (Exception e) {
            // TODO do need to failed out?
            logger.error("Update Session Status into database failed", e);
        }

        String msgDesc = "The CPG session Status will update to:"
                + this.stateStatus.getStatus();

        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "Update Status");
        sendClimateNotifyMessage(msgDesc, actions);
    }

    /**
     * 
     * 
     * @return
     */
    public SessionState getCurrentState() {
        // Get CPG session state from DB, it could be changed by other thread
        int stateValue = -1;
        try {
            stateValue = dao.getCPGSessionState(this.cpgSessionId);
        } catch (Exception e) {
            logger.error("Get current CPG Session state from database failed!",
                    e);
        }
        if (stateValue > 0) {
            setStateFromValue(stateValue);
        }
        return this.state;
    }

    /**
     * 
     * 
     * @return
     */
    public StateStatus getCurrentStatus() {
        // Get CPG session stateStatus from DB, it could be changed by other
        // thread
        try {
            return dao.getCurrentStateStatus(this.cpgSessionId);
        } catch (Exception e) {
            logger.error("Get current CPG Session state from database failed!",
                    e);
        }
        return new StateStatus(StateStatus.Status.UNKNOWN);
    }

    /**
     * @return the reportData
     */
    public ClimateRunData getReportData() {
        return reportData;
    }

    /**
     * Save this session to the DB for any EDEX to access.
     * 
     * @param reportData
     * @throws Exception
     */
    public void setReportDataAndUpdateDatabase(ClimateRunData reportData)
            throws Exception {
        this.reportData = reportData;

        try {
            byte[] rData = DynamicSerializationManager
                    .getManager(SerializationType.Thrift)
                    .serialize(this.reportData);
            dao.updateReportData(this.cpgSessionId, rData);
        } catch (SerializationException se) {
            logger.error("Serialization failed ", se);
            throw new Exception(
                    "Failed to serialize the product data for update database",
                    se);
        } catch (Exception de) {
            logger.error("Update table failed ", de);
            throw new Exception(
                    "Failed to update database with new report data", de);
        }

        String msgDesc = "Climate Product Data is created or updated";

        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "New Report Data");
        sendClimateNotifyMessage(msgDesc, actions);

    }

    /**
     * Save this session and product data to DB for any EDEX to access.
     * 
     * @param prodData
     */
    public void setProdDataAndUpdateDatabase(ClimateProdData prodData) {
        this.prodData = prodData;
        try {
            this.updateWithNewProdData();
        } catch (Exception e) {
            logger.error("Failed to update database for new ProdData ", e);
        }
    }

    /**
     * Update this session's product data.
     * 
     * @throws Exception
     */
    private void updateWithNewProdData() throws Exception {
        try {
            byte[] pData = DynamicSerializationManager
                    .getManager(SerializationType.Thrift)
                    .serialize(this.prodData);
            // update pendingExpiration
            this.pendingExpiration = this.prodData.getMaxExpiration();
            dao.updateProdData(this.cpgSessionId, pData,
                    this.pendingExpiration);
        } catch (SerializationException se) {
            logger.error("Serialization failed ", se);
            throw new Exception(
                    "Failed to serialize the product data for update database",
                    se);
        } catch (Exception de) {
            logger.error("Update table failed ", de);
            throw new Exception(
                    "Failed to update database for modified climate product",
                    de);
        }

        String msgDesc = "Climate Product Data is created or updated";
        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "New Products");
        sendClimateNotifyMessage(msgDesc, actions);
    }

    /**
     * Check if the session has one of terminated states
     * 
     * @param session
     * @return
     */
    private boolean isSessionTerminated() {
        if (this.getCurrentState() == SessionState.SENT
                || this.getCurrentStatus().equals(StateStatus.Status.CANCELLED)
                || this.stateStatus.equals(StateStatus.Status.FAILED)) {
            return true;
        }

        return false;
    }

    /**
     * Save a product that has been modified.
     * 
     * @param prodKey
     * @param modifiedProd
     * @throws Exception
     */
    public void saveModifiedClimateProd(ClimateProductType prodType,
            String prodKey, ClimateProduct modifiedProd) throws Exception {
        if (prodKey == null || prodKey.isEmpty()) {
            throw new Exception("Product Key can not be null or empty");
        }
        if (modifiedProd == null) {
            throw new Exception("Modified Climate Product can not be null");
        }
        if (this.prodData.containsClimateProd(prodKey) == false) {
            throw new Exception("There is no such product: " + prodKey
                    + " in the session: " + this.cpgSessionId);
        }

        this.prodData.replaceClimateProd(prodType, prodKey, modifiedProd);

        this.updateWithNewProdData();
    }

    /**
     * Delete a product.
     * 
     * @param prodKey
     * @throws Exception
     */
    public void deleteClimateProd(ClimateProductType prodType, String prodKey)
            throws Exception {
        if (prodKey == null || prodKey.isEmpty()) {
            throw new Exception("Product Key can not be null or empty");
        }
        if (this.prodData.containsClimateProd(prodKey) == false) {
            throw new Exception("There is no such product: " + prodKey
                    + " in the session: " + this.cpgSessionId);
        }

        this.prodData.deleteClimateProd(prodType, prodKey);

        this.updateWithNewProdData();
    }

    /**
     * @return the prodData
     */
    public ClimateProdData getProdData() {
        return prodData;
    }

    /**
     * Only was called if wait for review timed out. Send out products.
     * 
     * @throws Exception
     */
    public void autoSendClimateProducts() throws Exception {
        if (this.prodData == null || this.prodData.isEmpty()) {
            throw new Exception("No climate product can be sent!");
        }

        // Set to REVIEW
        this.setState(SessionState.REVIEW);

        if (this.globalConfig.isAllowAutoSend()) {
            // Send all NWWS products
            this.sendNWWSClimateProducts(this.prodData.getNwwsProd(), true,
                    "auto");

            // Send all NWR products
            this.sendNWRClimateProducts(this.prodData.getNwrProd(), true,
                    "auto");
        } else {
            logger.info(
                    "allowAutoSend is set to false in the globalDay.property");
        }
    }

    /**
     * Send NWWS products.
     * 
     * @param operational
     * @return
     */
    public SendClimateProductsResponse sendAllNWWSProducts(boolean operational,
            String user) {

        SendClimateProductsResponse res = new SendClimateProductsResponse(
                this.cpgSessionId);

        // Check potential empty climate product
        if (this.prodData == null || this.prodData.isEmpty()) {
            String msg = "No climate product has been created for the session: "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            sendAlertVizMessage(Priority.PROBLEM, msg, null);
            return res;
        }

        // Send all NWWS products
        return this.sendNWWSClimateProducts(this.prodData.getNwwsProd(),
                operational, user);

    }

    /**
     * Send NWR products (to NWR Waves).
     * 
     * @param operational
     * @param user
     * @return
     */
    public SendClimateProductsResponse sendAllNWRProducts(boolean operational,
            String user) {

        logger.info("CPG: sending NWR products to NWR Browser");

        // Just set the SesseionState to REVIEW
        this.setState(SessionState.REVIEW);

        SendClimateProductsResponse res = new SendClimateProductsResponse(
                this.cpgSessionId);

        // Check potential empty climate product
        if (this.prodData == null || this.prodData.isEmpty()) {
            String msg = "No climate product has been created for the session: "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            sendAlertVizMessage(Priority.PROBLEM, msg, null);
            return res;
        }

        // Send all NWWS products
        return this.sendNWRClimateProducts(this.prodData.getNwrProd(),
                operational, user);

    }

    /**
     * Send a NWWS product.
     * 
     * @param nwwsProdSet
     * @param operational
     * @param user
     * @return
     */
    public SendClimateProductsResponse sendNWWSClimateProducts(
            ClimateProductSet nwwsProdSet, boolean operational, String user) {
        SendClimateProductsResponse res = new SendClimateProductsResponse(
                this.cpgSessionId);

        // Add sending NWWS products to the The response
        res.setSendingProducts(nwwsProdSet.getUnsentProducts());

        if (nwwsProdSet.isEmpty()) {
            String msg = "No NWWS product can be sent in the session: "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            sendAlertVizMessage(Priority.PROBLEM, msg, null);
            return res;
        }

        if (nwwsProdSet.getNumberOfUnsent() < 1) {
            String msg = "No NWWS product for sending: " + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(nwwsProdSet.getProdStatus(), msg);
            return res;
        }

        // Store and transmit NWWS product
        if (!this.globalConfig.isAllowDisseminate()) {
            String message = "climate.allowDisseminate in globalDay.properties is set to false."
                    + " NWWS products will not be sent to OUP or stored in Text DB.";
            sendClimateNotifyMessage(message, null);
            logger.info(message);
        }

        for (String key : nwwsProdSet.getUnsentProducts().keySet()) {
            ClimateProduct cp = nwwsProdSet.getUnsentProducts().get(key);

            ClimateProductNWWSSender.transmitNWWSProduct(this, key, cp,
                    operational, user, globalConfig.isAllowDisseminate());
        }

        nwwsProdSet.updateSetLevelStatusFromProductStatus();

        // Update to DB
        try {
            this.updateWithNewProdData();
        } catch (Exception e) {
            String msg = "Update NWWS climate product status to DB failed for "
                    + this.cpgSessionId;
            logger.error(msg);

            nwwsProdSet.setProdStatus(ProductSetStatus.HAS_ERROR, msg);
            res.setSetLevelStatus(ProductSetStatus.HAS_ERROR, msg);
            sendAlertVizMessage(Priority.PROBLEM, msg, null);

        }

        // update pendingExpriation
        this.pendingExpiration = this.prodData.getMaxExpiration();

        // Check if all products have been sent
        if (this.prodData.isAllProductSent()) {
            // Update Session level stateStatus
            this.setState(SessionState.SENT);
            this.updateStateStatus(StateStatus.Status.SUCCESS, "");
        } else if (this.prodData.isAllNWWSProductSent()) {
            // Update Product Set Level stateStatus for NWWS
            this.prodData.updateProductSetLevelStatus(ClimateProductType.NWWS,
                    ProductSetStatus.SENT, "");
        }

        // Notify CPG View the stateStatus changed
        Map<String, String> detailMsg = new HashMap<>();
        detailMsg.put("NWWS_SEND_STATUS", this.prodData
                .getProductSetLevelStatus(ClimateProductType.NWWS).name());
        detailMsg
                .put("NWWS_SEND_STATUS_DESC",
                        this.prodData
                                .getProductSetLevelStatus(
                                        ClimateProductType.NWWS)
                                .getDescription());
        this.sendClimateNotifyMessage("NWWS product sending status", detailMsg);

        return res;
    }

    /**
     * Send a NWR product (to NWR Waves).
     * 
     * @param nwrProd
     * @param operational
     * @return
     */
    public SendClimateProductsResponse sendNWRClimateProducts(
            ClimateProductSet nwrProdSet, boolean operational, String user) {

        SendClimateProductsResponse res = new SendClimateProductsResponse(
                this.cpgSessionId);

        // Add sending NWR products to the The response
        res.setSendingProducts(nwrProdSet.getUnsentProducts());

        if (nwrProdSet.isEmpty()) {
            String msg = "No NWR product can be sent in the session: "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            sendAlertVizMessage(Priority.PROBLEM, msg, null);
            return res;
        }

        if (nwrProdSet.getNumberOfUnsent() < 1) {
            String msg = "No NWR product for sending: " + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(nwrProdSet.getProdStatus(), msg);
            return res;
        }

        if (!globalConfig.isAllowDisseminate()) {
            String message = "climate.allowDisseminate in globalDay.properties is set to false."
                    + " NWR products will not be copied to the target NWRWave directory";
            sendClimateNotifyMessage(message, null);
            logger.info(message);
        }

        // Forward to NWR
        NWRProductForwarder.forwardToNWR(this, nwrProdSet, res,
                this.globalConfig.getCopyNWRTo(), user,
                globalConfig.isAllowDisseminate());

        // Update database with modified stateStatus for sent products
        try {
            this.updateWithNewProdData();
        } catch (Exception e) {
            String msg = "Update NWR climate product status to DB failed for "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.HAS_ERROR, msg);
            sendAlertVizMessage(Priority.PROBLEM, msg, null);
        }

        // update pendingExpriation
        this.pendingExpiration = this.prodData.getMaxExpiration();

        // Check if all products have been sent
        if (this.prodData.isAllProductSent()) {
            // Update Session level stateStatus
            this.setState(SessionState.SENT);
            this.updateStateStatus(StateStatus.Status.SUCCESS, "");
        } else if (this.prodData.isAllNWRProductSent()) {
            // Update Product Set Level stateStatus for NWR
            this.prodData.updateProductSetLevelStatus(ClimateProductType.NWR,
                    ProductSetStatus.SENT, "");
        }

        // Notify CPG View the stateStatus changed
        Map<String, String> detailMsg = new HashMap<>();
        detailMsg.put("NWR_SEND_STATUS", this.prodData
                .getProductSetLevelStatus(ClimateProductType.NWR).name());
        detailMsg
                .put("NWR_SEND_STATUS_DESC",
                        this.prodData
                                .getProductSetLevelStatus(
                                        ClimateProductType.NWR)
                                .getDescription());
        this.sendClimateNotifyMessage("NWR product sending status", detailMsg);

        return res;
    }

    /**
     * Store a finalized product for later reviewing/processing to text DB.
     * 
     * @param cp
     * @param operational
     * @return
     */
    public void storeClimateProduct(ClimateProduct cp, boolean operational) {
        TextDB textdb = new TextDB();

        if (cp.getStatus() == ProductStatus.PENDING) {
            // writeProduct will return Long.MIN_VALUE if write failed
            long insertTime = textdb.writeProduct(cp.getPil(), cp.getProdText(),
                    operational, null);

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
                sendAlertVizMessage(Priority.PROBLEM, desc, null);
                logger.error(
                        desc + ". Product text: [" + cp.getProdText() + "]");

            }
        }
    }

    /**
     * @return the startedAt
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * @return the lastUpdated
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

}
