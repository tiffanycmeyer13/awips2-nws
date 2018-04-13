/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateMessageUtils;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateAlertUtils;

/**
 * ClimateProdGenerateSessionFactory Create a new / recreate existing CPG
 * Session object based on passed in arguments
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 13, 2017 20637      pwang       Initial creation
 * Jul 26, 2017 33104      amoore      Address review comments.
 * Nov 06, 2017 35731      pwang       added logic to enable ON/OFF for auto product generation
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public final class ClimateProdGenerateSessionFactory {
    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerateSessionFactory.class);

    private ClimateProdGenerateSessionDAO dao;

    private int runType = 0;

    private int prodType = 0;

    /**
     * empty constructor
     */
    public ClimateProdGenerateSessionFactory() {
        this.dao = new ClimateProdGenerateSessionDAO();
    }

    /**
     * Constructor for cron jobs
     * 
     * @param runType
     * @param prodType
     */
    public ClimateProdGenerateSessionFactory(int runType, int prodType) {
        this.dao = new ClimateProdGenerateSessionDAO();
        this.runType = runType;
        this.prodType = prodType;
    }

    /**
     * Create a new CPG Session
     * 
     * @param runType
     * @param prodType
     * @return
     */
    public static ClimateProdGenerateSession getCPGSession(int runType,
            int prodType) {
        return new ClimateProdGenerateSession(
                new ClimateProdGenerateSessionDAO(), runType, prodType);
    }

    /**
     * get existing CPG session by session ID
     * 
     * @param cpgSessionId
     * @return
     */
    public static ClimateProdGenerateSession getCPGSession(
            String cpgSessionId) {
        return new ClimateProdGenerateSession(
                new ClimateProdGenerateSessionDAO(), cpgSessionId);
    }

    /**
     * This method will only be called by cron job
     * 
     * @throws Exception
     */
    public void generateClimate() throws Exception {
        if (dao == null) {
            logger.debug("Data Access Object can not be null");
            throw new Exception("Data Access Object can not be null");
        }

        if (prodType <= 0) {
            logger.debug("Invalid Product Type code: " + prodType);
            throw new Exception("Invalid Product Type code: " + prodType);
        }

        // Check auto generate switch is ON
        if (!isAutoOn()) {
            String periodName = PeriodType.getPeriodTypeDesc(prodType);
            String msg = "Auto generate climate product: " + periodName
                    + " is OFF in the GlobalDay configure properties";
            logger.info(msg);
            EDEXUtil.sendMessageAlertViz(Priority.EVENTB,
                    ClimateMessageUtils.CPG_PLUGIN_ID,
                    ClimateAlertUtils.SOURCE_EDEX,
                    ClimateAlertUtils.CATEGORY_CLIMATE,
                    "Climate [" + periodName + "] will not automatically run",
                    msg, null);
            // do nothing
            return;
        }

        // Instantiate a new CPG Session
        ClimateProdGenerateSession cpgSession = new ClimateProdGenerateSession(
                this.dao, this.runType, this.prodType);

        // call autoCreateClimate
        cpgSession.autoCreateClimate();
    }

    /**
     * check if the product configured for auto-generating
     * 
     * @return
     */
    private boolean isAutoOn() {
        ClimateGlobal globalConfig = ClimateGlobalConfiguration.getGlobal();

        switch (this.prodType) {
        case 1:
            return globalConfig.isAutoAM();
        case 10:
            return globalConfig.isAutoIM();
        case 2:
            return globalConfig.isAutoPM();
        case 5:
            return globalConfig.isAutoCLM();
        case 7:
            return globalConfig.isAutoCLS();
        case 9:
            return globalConfig.isAutoCLA();
        default:
            return false;
        }
    }

    /**
     * @return the dao
     */
    public ClimateProdGenerateSessionDAO getDao() {
        return dao;
    }

    /**
     * @param dao
     *            the dao to set
     */
    public void setDao(ClimateProdGenerateSessionDAO dao) {
        this.dao = dao;
    }

    /**
     * @return the runType
     */
    public int getRunType() {
        return runType;
    }

    /**
     * @param runType
     *            the runType to set
     */
    public void setRunType(int runType) {
        this.runType = runType;
    }

    /**
     * @return the prodType
     */
    public int getProdType() {
        return prodType;
    }

    /**
     * @param prodType
     *            the prodType to set
     */
    public void setProdType(int prodType) {
        this.prodType = prodType;
    }
}
