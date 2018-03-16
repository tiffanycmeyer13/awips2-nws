/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc;

/**
 * CheckDataType Enum of data types used by Site defined QC check
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 6, 2017  35729      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public enum CheckDataType {

    SHORT(1), INT(2), LONG(2), FLOAT(3), DOUBLE(4), WINDOBJ(5), UNKNOWN(-1);

    private int value;

    private CheckDataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
