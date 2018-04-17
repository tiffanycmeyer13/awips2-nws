/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * SessionState
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2016 20637      pwang       Initial creation
 * Nov 03, 2017 36749      amoore      Enum should be separate from desc field.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class StateStatus {

    /**
     * State Statuses.
     * 
     * @author amoore
     */
    @DynamicSerialize
    public enum Status {
        WORKING(1), SUCCESS(2), CANCELLED(3), FAILED(4), ERROR(5), UNKNOWN(-1);

        @DynamicSerializeElement
        private int value;

        private Status(int code) {
            this.value = code;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * @param value
         *            the value to set
         */
        public void setValue(int value) {
            this.value = value;
        }

        public static Status valueOf(int value) {
            for (Status status : Status.values()) {
                if (value == status.getValue()) {
                    return status;
                }
            }

            return Status.UNKNOWN;
        }
    }

    /**
     * Status description.
     */
    @DynamicSerializeElement
    private String description;

    /**
     * Current status.
     */
    @DynamicSerializeElement
    private Status status;

    /**
     * Empty constructor for serialization.
     */
    public StateStatus() {
    }

    /**
     * Constructor. Use empty description.
     * 
     * @param code
     */
    public StateStatus(int code) {
        this(Status.valueOf(code));
    }

    /**
     * Constructor. Use empty description.
     * 
     * @param status
     */
    public StateStatus(Status status) {
        this(status, "");
    }

    /**
     * Constructor.
     * 
     * @param code
     * @param desc
     */
    public StateStatus(int code, String desc) {
        this(Status.valueOf(code), desc);
    }

    /**
     * Constructor.
     * 
     * @param status
     * @param desc
     */
    public StateStatus(Status status, String desc) {
        this.status = status;
        this.description = desc;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
