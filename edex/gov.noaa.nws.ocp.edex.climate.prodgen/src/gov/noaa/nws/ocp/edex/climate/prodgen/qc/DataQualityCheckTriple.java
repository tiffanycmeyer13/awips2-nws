/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * DataQualityCheckTriple
 * 
 * The triple represents
 * 
 * 1) Subject: a parameter to check
 * 
 * 2) Predicate: an operator
 * 
 * 3) Object: optional valid value / range
 * 
 * Example: dsm.snowdepth=M is designated to check if one DSM parameter,
 * snowdepth is missing
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2017  35729      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class DataQualityCheckTriple {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DataQualityCheckTriple.class);

    // Site define parameter for checking
    private final String paramName;

    private final QCOperator checkOp;

    // Optional value to be matched
    private final Object paramValue;

    /**
     * Constructor
     * 
     * @param name
     * @param op
     * @param value
     */
    public DataQualityCheckTriple(String name, QCOperator op, Object value) {
        this.paramName = name;

        this.checkOp = op;

        this.paramValue = value;
    }

    /**
     * Constructor
     * 
     * @param key
     *            -- Property key
     * @param value
     *            -- Property value
     */
    public DataQualityCheckTriple(String key, String value) {
        logger.debug("Constructing QC check with key [" + key + "] and value ["
                + value + "]");

        this.paramName = key.substring(key.lastIndexOf(".") + 1);

        String operation = value.substring(0, 1);

        if (operation.equalsIgnoreCase("M")) {
            this.checkOp = QCOperator.M;
            this.paramValue = null;
        } else if (operation.equals(">")) {
            this.checkOp = QCOperator.GT;
            // Will be casted to proper type late
            this.paramValue = value.substring(1, value.length());
        } else if (operation.equals("<")) {
            this.checkOp = QCOperator.LT;
            // Will be casted to proper type late
            this.paramValue = value.substring(1, value.length());
        } else {
            logger.warn("Unknown QC check symbol: [" + operation
                    + "] for parameter: [" + paramName + "]");
            this.checkOp = QCOperator.UNKNOWN;
            this.paramValue = null;
        }
    }

    /**
     * @return the paramName
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * @return the checkOp
     */
    public QCOperator getCheckOp() {
        return checkOp;
    }

    /**
     * @return the paramValue
     */
    public Object getParamValue() {
        return paramValue;
    }
}
