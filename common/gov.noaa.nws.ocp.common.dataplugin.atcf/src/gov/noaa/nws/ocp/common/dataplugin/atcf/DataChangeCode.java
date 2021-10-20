/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2019 #68118     wpaintsil   Serialize exception caused by lack of setters.
 *
 * </pre>
 *
 * @author
 * @version 1.0
 */
public enum DataChangeCode {
    UNCHANGED(0, "Original"),
    NEW(1, "Added new Record"),
    UPDATED(2, "Elements Modified"),
    DELETED(3, "Mark as deleted");

    private int value;

    private String desc;

    /**
     * @param iValue
     */
    private DataChangeCode(final int value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static DataChangeCode valueOf(int value) {
        for (DataChangeCode ccode : DataChangeCode.values()) {
            if (value == ccode.getValue()) {
                return ccode;
            }
        }
        return DataChangeCode.UNCHANGED;
    }
}
