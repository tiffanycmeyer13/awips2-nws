/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.util;

/**
 * Run type of session (manual or auto).
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2016 20637      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public enum RunType {
    AUTO(1), MANUAL(2), UNKNOWN(-1);

    private int value;

    private RunType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}