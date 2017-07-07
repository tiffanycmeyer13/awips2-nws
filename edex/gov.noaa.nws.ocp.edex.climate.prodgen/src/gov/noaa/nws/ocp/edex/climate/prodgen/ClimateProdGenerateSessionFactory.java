/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;

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
    }

    /**
     * Constructor for cron jobs
     * 
     * @param dao
     * @param runType
     * @param prodType
     */
    public ClimateProdGenerateSessionFactory(ClimateProdGenerateSessionDAO dao,
            int runType, int prodType) {
        this.dao = dao;
        this.runType = runType;
        this.prodType = prodType;
    }

    /**
     * Create a new CPG Session
     * 
     * @param dao
     * @param runType
     * @param prodType
     * @return
     */
    public static ClimateProdGenerateSession getCPGSession(
            ClimateProdGenerateSessionDAO dao, int runType, int prodType) {
        return new ClimateProdGenerateSession(dao, runType, prodType);
    }

    /**
     * get existing CPG session by session ID
     * 
     * @param dao
     * @param cpgSessionId
     * @return
     */
    public static ClimateProdGenerateSession getCPGSession(
            ClimateProdGenerateSessionDAO dao, String cpgSessionId) {
        return new ClimateProdGenerateSession(dao, cpgSessionId);
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

        // Instantiate a new CPG Session
        ClimateProdGenerateSession cpgSession = new ClimateProdGenerateSession(
                this.dao, this.runType, this.prodType);

        // call autoCreateClimate
        cpgSession.autoCreateClimate();
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
