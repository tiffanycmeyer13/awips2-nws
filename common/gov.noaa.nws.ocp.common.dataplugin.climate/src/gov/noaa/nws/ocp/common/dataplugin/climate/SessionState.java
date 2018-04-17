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
 * Nov 03, 2017 36749      amoore      Add integer #valueOf
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public enum SessionState {
    STARTED(1), CREATED(2), DISPLAY(3), DISPLAYED(4), FORMATTED(5), REVIEW(
            6), PENDING(7), SENT(8), UNKNOWN(-1);

    @DynamicSerializeElement
    private int value;

    private SessionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @param code
     * @return
     */
    public static SessionState valueOf(int code) {
        for (SessionState state : SessionState.values()) {
            if (state.getValue() == code) {
                return state;
            }
        }

        return UNKNOWN;
    }
}
