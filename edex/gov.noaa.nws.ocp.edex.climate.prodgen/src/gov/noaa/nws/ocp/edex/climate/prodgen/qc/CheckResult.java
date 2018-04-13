/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc;

import java.util.ArrayList;
import java.util.List;

/**
 * CheckResult Wrapper class used to represent site defined QC check
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

public class CheckResult {

    /* final status of site defined QC check */
    private boolean passed = true;

    /* detailed information if one or more checking failed */
    private final List<String> details;

    /**
     * Constructor
     */
    public CheckResult() {
        details = new ArrayList<>();
    }

    /**
     * @return the passed
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * @param passed
     *            the passed to set
     */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * @return the details, printed out from associated list. Ends in newline.
     */
    public String getDetails() {
        StringBuilder sb = new StringBuilder();

        for (String detail : details) {
            sb.append(detail).append("\n");
        }

        return sb.toString();
    }

    /**
     * addDetail
     * 
     * @param msg
     */
    public void addDetail(String msg) {
        this.details.add(msg);
    }

    /**
     * get size of details logs
     * 
     * @return
     */
    public int size() {
        return this.details.size();
    }

}
