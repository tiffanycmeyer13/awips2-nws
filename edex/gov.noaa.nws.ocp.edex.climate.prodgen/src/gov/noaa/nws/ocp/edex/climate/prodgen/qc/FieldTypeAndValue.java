/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc;

/**
 * FieldTypeAndValue
 * 
 * A wrapper class to represent a field defined in climate data classes
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

public class FieldTypeAndValue {

    // Field data type
    private CheckDataType type;

    // Value of field
    private Object value;

    /**
     * Empty constructor
     */
    public FieldTypeAndValue() {
        this.type = CheckDataType.UNKNOWN;
        this.value = null;
    }

    /**
     * Constructor
     * 
     * @param t
     * @param v
     */
    public FieldTypeAndValue(CheckDataType t, Object v) {
        this.type = t;
        this.value = v;
    }

    /**
     * @return the type
     */
    public CheckDataType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(CheckDataType type) {
        this.type = type;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

}
