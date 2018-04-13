/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

/**
 * Enum for action types on CPG products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 8 MAY 2017   33532      pwang       Initial creation.
 * </pre>
 * 
 * @author pwang
 * @version 1.0
 */
public enum ActionOnProduct {
    NEW(0), MODIFY(1), STORE(2), SEND(3), RESEND(4), UNKNOWN(-1);

    private int code;

    private String description;

    private ActionOnProduct(int code) {
        this.code = code;
        this.description = "";
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
