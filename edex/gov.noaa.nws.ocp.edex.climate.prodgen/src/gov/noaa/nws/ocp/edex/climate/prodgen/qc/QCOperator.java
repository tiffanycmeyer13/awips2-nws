/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc;

/**
 * QCOperator
 * 
 * An Enum of QC checking operators
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
public enum QCOperator {
    M(1), LT(2), GT(2), UNKNOWN(-1);

    private int value;

    private QCOperator(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
