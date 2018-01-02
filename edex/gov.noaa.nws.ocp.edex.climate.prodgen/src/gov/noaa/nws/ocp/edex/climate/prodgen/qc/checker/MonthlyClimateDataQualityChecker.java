/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc.checker;

import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.CheckResult;

/**
 * MonthlyClimateDataQualityChecker
 * 
 * All MSM parameters defined in the qcparams.properties will be handled by this
 * checker. Not yet implemented.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2017  35729      amoore      Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class MonthlyClimateDataQualityChecker
        extends ClimateDataQualityChecker {

    /**
     * Constructor
     */
    public MonthlyClimateDataQualityChecker() {
        super();
    }

    @Override
    public CheckResult check(ClimateRunData data) throws Exception {
        logger.debug("MSM QC checking for Climate not yet supported.");
        return new CheckResult();
    }
}
