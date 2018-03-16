/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.edex.metartoclimate.dao.ClimateReportDAO;

/**
 * Created to extract purge method from MetarToClimateSvr.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2016            pwang       Initial creation
 * 24 FEB 2017  27420      amoore      Address warnings in code.
 * 07 SEP 2017  37754      amoore      Throw exception on failure.
 * 02 NOV 2017  37755      amoore      Take DAO in constructor.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateMetarPurger {

    /**
     * Logger.
     */
    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(ClimateMetarPurger.class);

    private final ClimateReportDAO dao;

    private int purgeHours = 48;

    /**
     * Construct an instance of this transformer.
     * 
     * @param reportDAO
     */
    public ClimateMetarPurger(ClimateReportDAO reportDAO) {
        dao = reportDAO;
    }

    /**
     * perform purge
     */
    public void purgeClimateReport() {
        logger.info("Purge routine started, purge hours: [" + purgeHours + "]");

        dao.purgeTable(purgeHours);
    }

    /**
     * @return the purgeHours
     */
    public int getPurgeHours() {
        return purgeHours;
    }

    /**
     * @param purgeHours
     *            the purgeHours to set
     */
    public void setPurgeHours(int purgeHours) {
        this.purgeHours = purgeHours;
    }

}
