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
import java.util.List;
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
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateSessionException;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorDailyResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.DisplayClimateResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.SendClimateProductsResponse;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductTypeManager;
import gov.noaa.nws.ocp.edex.climate.creator.ClimateCreator;
import gov.noaa.nws.ocp.edex.climate.formatter.ClimateFormatter;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;
import gov.noaa.nws.ocp.edex.climate.prodgen.transmit.ClimateProductNWWSSender;
import gov.noaa.nws.ocp.edex.climate.prodgen.transmit.NWRProductForwarder;
import gov.noaa.nws.ocp.edex.climate.prodgen.util.ClimateProdSetting;
import gov.noaa.nws.ocp.edex.climate.prodgen.util.RunType;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;

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
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public final class ClimateProdGenerateSession {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerateSession.class);

    // TODO put in common location
    public final static String EDEX = "EDEX";

    // TODO put in common location
    public final static String CATEGORY = "Climate";

    public final static String PLUGIN_ID = "ClimateProdGenerateSession";

    private final static int DEFAULT_DIS_WAIT = 20;

    private final static int DEFAULT_REV_WAIT = 10;

    // TODO put in common location
    public static final String cpgEndpoint = "climateNotify";

    // DAO
    private ClimateProdGenerateSessionDAO dao;

    // Attributes need to be stored in Climate Database
    private String cpgSessionId;

    private RunType runType;

    private PeriodType prodType;

    // CPG Session Workflow state
    private SessionState state;

    // CPG Session State's status
    private StateStatus status;

    // global configuration
    private ClimateGlobal globalConfig;

    // setting parameters
    private ClimateProdSetting prodSetting;

    // TODO: ClimateCreatorResponse may not a great name for the report data
    private ClimateCreatorResponse reportData = null;

    // Climate Product Data created by ClimateFormatter
    private ClimateProdData prodData = null;

    private LocalDateTime startedAt;

    private LocalDateTime lastUpdated;

    // Session will expired even not at terminate status
    private LocalDateTime pendingExpiration;

    /**
     * Constructor Create a new CPG session
     * 
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
        this.status = StateStatus.WORKING;

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
        List<ClimateProdGenerateSessionData> sessionList = null;

        try {
            sessionList = dao.getCPGSession(cpgSessionId);
        } catch (ClimateQueryException e) {
            logger.error("Retrieve CPG Session data from database failed! ", e);
            failCPGSession("Failed to retrieve session data from the database");
        }

        if (sessionList == null || sessionList.isEmpty()) {
            logger.error("No CPG Session exists for the Session ID: "
                    + cpgSessionId);
            return;
        } else {
            // For given session ID, only one record should be returned
            sessionData = sessionList.get(0);
        }

        this.cpgSessionId = sessionData.getCpg_session_id();
        this.setRunTypeFromValue(sessionData.getRun_type());
        this.setProdTypeFromValue(sessionData.getProd_type());
        this.setStateFromValue(sessionData.getState());
        this.status = getStatusFromCodeAndDesc(sessionData.getStatus(),
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
                this.reportData = (ClimateCreatorResponse) DynamicSerializationManager
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
        sessionData.setStatus(this.status.getCode());
        sessionData.setStatus_desc(this.status.getDescription());
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
            // TODO:
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
     *            formated message contains details
     */
    public void sendAlertVizMessage(Priority priority, String desc,
            String msgBody) {

        EDEXUtil.sendMessageAlertViz(priority, PLUGIN_ID, EDEX, CATEGORY, desc,
                msgBody, null);
    }

    /**
     * 
     * @param priority
     * @param desc
     * @param msgBody
     */
    public void sendNotificationToAlertViz(Priority priority, String desc,
            String msgBody) {

        EDEXUtil.sendMessageAlertViz(priority, PLUGIN_ID, EDEX, CATEGORY, desc,
                msgBody, null);
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
        sb.append("STATUS_CODE=").append(this.status).append(",");
        sb.append("STATUS_DESC=").append(this.status.getDescription())
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
        sm.setPriority(Priority.EVENTA);
        sm.setPlugin(PLUGIN_ID);
        sm.setCategory(CATEGORY);
        sm.setMessage(desc);
        sm.setMachineToCurrent();
        sm.setSourceKey(EDEX);
        sm.setDetails(detailMsg);
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer().sendAsync(cpgEndpoint, sm);
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
        sm.setPlugin(PLUGIN_ID);
        sm.setCategory(CATEGORY);
        sm.setMessage(desc);
        sm.setMachineToCurrent();
        sm.setSourceKey(EDEX);
        sm.setDetails(msgBody.toString());
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer().sendAsync(cpgEndpoint, sm);
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

        // Check if cancelled
        if (this.getCurrentStatus() == StateStatus.CANCELLED) {
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " has been cancelled");
            return;
        }

        // Alert User for Display
        String msgDesc = "Climate Report Created, waiting for display. CPG Session ID = "
                + this.cpgSessionId;
        this.sendNotificationToAlertViz(Priority.SIGNIFICANT, msgDesc, "");

        int dispWait = this.globalConfig.getDisplayWait();
        if (dispWait < 0) {
            dispWait = DEFAULT_DIS_WAIT;
        }
        // Min to seconds
        dispWait *= 60;
        int secondsLeft = dispWait;

        Object waiter = new Object();
        synchronized (waiter) {
            while (this.getCurrentStatus() != StateStatus.CANCELLED
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
        if (this.getCurrentStatus() == StateStatus.CANCELLED) {
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
                failCPGSession("Failed to execute Headless Display !");
                return;
            }

            try {
                this.executeFormatClimate();
            } catch (ClimateSessionException e) {
                logger.error("Failed to execute Format Climate", e);
                failCPGSession("Failed to execute Format Climate !");
                return;
            }
        }

        // Check if cancelled
        if (this.getCurrentStatus() == StateStatus.CANCELLED) {
            logger.info("The CPG session: " + this.getCPGSessionId()
                    + " has been cancelled");
            return;
        }

        // Alert User for review
        msgDesc = "Formatted Climate Product generated, waiting for review. CPG Session ID = "
                + this.cpgSessionId;
        this.sendNotificationToAlertViz(Priority.SIGNIFICANT, msgDesc, "");

        int revWait = this.globalConfig.getReviewWait();
        if (revWait < 0) {
            revWait = DEFAULT_REV_WAIT;
        }
        // Minutes to seconds
        revWait *= 60;
        secondsLeft = revWait;

        Object waiter2 = new Object();
        synchronized (waiter2) {
            while (this.getCurrentStatus() != StateStatus.CANCELLED
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
        if (this.getCurrentStatus() == StateStatus.CANCELLED) {
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
    public ClimateCreatorResponse manualCreateDailyClimate(ClimateDate date,
            boolean nonRecentRun) throws Exception {
        try {
            this.executeCreateDailyClimate(date, nonRecentRun);
        } catch (ClimateSessionException e) {
            logger.error("Failed to execute Create Daily Climate", e);
            failCPGSession("Failed to execute Create Daily Climate!");
            throw new Exception("Failed to execute Create Daily Climate", e);
        }

        return reportData;
    }

    /**
     * A user-initiated run of Creator for a period product type.
     * 
     * @param begin
     * @param end
     * @param nonRecentRun
     * @return
     */
    public ClimateCreatorResponse manualCreatePeriodClimate(ClimateDate begin,
            ClimateDate end, boolean nonRecentRun) throws Exception {
        try {
            this.executeCreatePeriodClimate(begin, end, nonRecentRun);
        } catch (ClimateSessionException e) {
            logger.error("Failed to execute Create Period Climate", e);
            failCPGSession("Failed to execute Create Period Climate!");
            throw new Exception("Failed to execute Create Period Climate", e);
        }

        return reportData;
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
                failCPGSession("Failed to execute Format Climate!");
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
            return this.status.getCode();
        }

        // Update session state to cancelled
        this.updateStateStatus(StateStatus.CANCELLED, reason);

        // Send a message to inform other client who may also access same
        // session
        String msgDesc = "Warning: the user " + who + " cancelled the session: "
                + this.getCPGSessionId();

        // Alert user for cancel the session
        this.sendNotificationToAlertViz(Priority.WARN, msgDesc, "");

        // Send notification to CPG Viewer
        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "CANCEL");
        sendClimateNotifyMessage(msgDesc, actions);

        return this.status.getCode();
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
        ClimateGlobalConfiguration cgc = new ClimateGlobalConfiguration();
        this.globalConfig = cgc.getGlobal();

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
                this.sendNotificationToAlertViz(Priority.INFO, msgDesc,
                        msgDetail);

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
        // Set the session failed and update database
        this.updateStateStatus(StateStatus.FAILED, reason);
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
        ClimateCreatorResponse report;
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
        ClimateCreatorResponse report;
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
        ClimateCreatorResponse report;
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
            cpMap = formatter.formatClimate(this.reportData);
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
                    "The climate product data was not generated by ClimateFormatter");
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
                ClimateCreatorDailyResponse ccdr = (ClimateCreatorDailyResponse) this.reportData;
                dcDao.processDisplayFinalization(this.prodType,
                        ccdr.getBeginDate(), ccdr.getReportMap());
            } else {
                // Period
                ClimatePeriodDAO cpDao = new ClimatePeriodDAO();
                ClimateCreatorPeriodResponse ccpr = (ClimateCreatorPeriodResponse) this.reportData;
                cpDao.processDisplayFinalization(this.prodType,
                        ccpr.getBeginDate(), ccpr.getEndDate(),
                        ccpr.getReportMap());
            }
        } catch (ClimateInvalidParameterException e) {
            String msgDesc = "Called processDisplayFinalization with invalid parameter(s)";
            this.failCPGSession(msgDesc);
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
    public void finalizeDisplayDailyCimate(ClimateCreatorDailyResponse userData)
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
            ClimateCreatorPeriodResponse userData,
            Set<Integer> msmOverwriteApproved) throws ClimateSessionException {

        try {
            ClimatePeriodDAO cpDao = new ClimatePeriodDAO();
            cpDao.processDisplayFinalization(this.prodType,
                    userData.getBeginDate(), userData.getEndDate(), dataMap,
                    userData.getReportMap(), msmOverwriteApproved);
        } catch (ClimateInvalidParameterException e) {
            String msgDesc = "Called processDisplayFinalization with invalid parameter(s)";
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
     * Change to a new state need to reset the status to WORKING
     * 
     * @param state
     *            the state to set
     */
    public void setState(SessionState state) {

        // Change to new state
        this.state = state;
        // Update lastUpdated;
        lastUpdated = LocalDateTime.now();

        this.status = StateStatus.SUCCESS;
        String msg = this.state.name() + " Next: " + this.nextState();
        this.status.setDescription(msg);

        // Update session in the database
        try {
            // Update state in db
            dao.updateCPGSessionState(this.cpgSessionId, this.state.getValue(),
                    this.lastUpdated);

            // Update status accordingly
            this.updateStateStatus(this.status, msg);

        } catch (Exception e) {
            logger.error("Update Session State into database failed", e);
        }

        String msgDesc = "The CPG session state will update to: " + this.state;
        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "Change State");
        sendClimateNotifyMessage(msgDesc, actions);
    }

    /**
     * Update status.
     * 
     * @param status
     * @param desc
     */
    public void updateStateStatus(StateStatus status, String desc) {

        this.status = status;
        this.status.setDescription(desc);

        // Update lastUpdated;
        lastUpdated = LocalDateTime.now();

        // Update session's status in the database
        try {
            dao.updateCPGSessionStatus(this.cpgSessionId, this.status.getCode(),
                    this.status.getDescription(), this.lastUpdated);
        } catch (Exception e) {
            // TODO do need to failed out?
            logger.error("Update Session Status into database failed", e);
        }

        String msgDesc = "The CPG session Status will update to:" + this.status;

        Map<String, String> actions = new HashMap<>();
        actions.put("ACTION", "Update Status");
        sendClimateNotifyMessage(msgDesc, actions);
    }

    /**
     * 
     * @param value
     * @param desc
     * @return
     */
    private StateStatus getStatusFromCodeAndDesc(int value, String desc) {
        for (StateStatus s : StateStatus.values()) {
            if (s.getCode() == value) {
                s.setDescription(desc);
                return s;
            }
        }
        // Invalid value
        return StateStatus.UNKNOWN;

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
        // Get CPG session status from DB, it could be changed by other thread
        try {
            return dao.getCurrentStateStatus(this.cpgSessionId);
        } catch (Exception e) {
            logger.error("Get current CPG Session state from database failed!",
                    e);
        }
        return StateStatus.UNKNOWN;
    }

    /**
     * @return the reportData
     */
    public ClimateCreatorResponse getReportData() {
        return reportData;
    }

    /**
     * Save this session to the DB for any EDEX to access.
     * 
     * @param reportData
     * @throws Exception
     */
    public void setReportDataAndUpdateDatabase(
            ClimateCreatorResponse reportData) throws Exception {
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
                || this.getCurrentStatus() == StateStatus.CANCELLED
                || this.status == StateStatus.FAILED) {
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
            this.sendAlertVizMessage(Priority.PROBLEM, msg, null);
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
            this.sendAlertVizMessage(Priority.PROBLEM, msg, null);
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

        if (nwwsProdSet == null || nwwsProdSet.isEmpty()) {
            String msg = "No NWWS product can be sent in the session: "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            this.sendAlertVizMessage(Priority.PROBLEM, msg, null);
            return res;
        }

        if (nwwsProdSet.getNumberOfUnsent() < 1) {
            String msg = "No NWWS product for sending: " + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(nwwsProdSet.getProdStatus(), msg);
            return res;
        }

        // Store and transmit NWWS product
        if (this.globalConfig.isAllowDisseminate()) {
            for (String key : nwwsProdSet.getUnsentProducts().keySet()) {
                ClimateProduct cp = nwwsProdSet.getUnsentProducts().get(key);

                ClimateProductNWWSSender.transmitNWWSProduct(this, key, cp,
                        operational, user);

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
                this.sendAlertVizMessage(Priority.PROBLEM, msg, null);

            }

        } else {
            String message = "climate.allowDisseminate in globalDay.properties is set to false."
                    + " NWWS products will not send to OUP";
            sendClimateNotifyMessage(message, null);
            logger.info(message);
        }

        // update pendingExpriation
        this.pendingExpiration = this.prodData.getMaxExpiration();

        // Check if all products have been sent
        if (this.prodData.isAllProductSent()) {
            // Update Session level status
            this.setState(SessionState.SENT);
            this.updateStateStatus(StateStatus.SUCCESS, "");
        } else if (this.prodData.isAllNWWSProductSent()) {
            // Update Product Set Level status for NWWS
            this.prodData.updateProductSetLevelStatus(ClimateProductType.NWWS,
                    ProductSetStatus.SENT, "");
        }

        // Notify CPG View the status changed
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

        if (nwrProdSet == null || nwrProdSet.isEmpty()) {
            String msg = "No NWR product can be sent in the session: "
                    + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            this.sendAlertVizMessage(Priority.PROBLEM, msg, null);
            return res;
        }

        if (nwrProdSet.getNumberOfUnsent() < 1) {
            String msg = "No NWR product for sending: " + this.cpgSessionId;
            logger.error(msg);
            res.setSetLevelStatus(nwrProdSet.getProdStatus(), msg);
            return res;
        }

        if (this.globalConfig.isAllowDisseminate()) {
            // Forward to NWR
            NWRProductForwarder.forwardToNWR(this, nwrProdSet, res,
                    this.globalConfig.getCopyNWRTo(), user);

            // Update database with modified status for sent products
            try {
                this.updateWithNewProdData();
            } catch (Exception e) {
                String msg = "Update NWR climate product status to DB failed for "
                        + this.cpgSessionId;
                logger.error(msg);
                res.setSetLevelStatus(ProductSetStatus.HAS_ERROR, msg);
                this.sendAlertVizMessage(Priority.PROBLEM, msg, null);

            }

        } else {
            String message = "climate.allowDisseminate in globalDay.properties is set to false."
                    + " NWR products will not copy to the target NWRWave directory";
            sendClimateNotifyMessage(message, null);
            logger.info(message);
        }

        // update pendingExpriation
        this.pendingExpiration = this.prodData.getMaxExpiration();

        // Check if all products have been sent
        if (this.prodData.isAllProductSent()) {
            // Update Session level status
            this.setState(SessionState.SENT);
            this.updateStateStatus(StateStatus.SUCCESS, "");
        } else if (this.prodData.isAllNWRProductSent()) {
            // Update Product Set Level status for NWR
            this.prodData.updateProductSetLevelStatus(ClimateProductType.NWR,
                    ProductSetStatus.SENT, "");
        }

        // Notify CPG View the status changed
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
                this.sendAlertVizMessage(Priority.PROBLEM, desc, null);
                logger.error(
                        desc + " ptoduct text: [" + cp.getProdText() + "]");

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
