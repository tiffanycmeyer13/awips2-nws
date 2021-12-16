package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ConflictSandbox ConflictSandbox contains necessary information to inform the
 * sandbox (identified by its ID) become invalid to checkin when other sandbox
 * has been checked into the baseline.
 *
 * ConflictSandbox is only being used when a sandbox being checked in The caller
 * will receive a list of ConflictSandbox when CheckinDeckRequest is issued.
 *
 * If no ConflictSandbox returned, means the checkin is successful without found
 * any other sandbox was conflicted with the one just checked in.
 *
 * If returned ConflictSandbox with a id < 0, means the check in failed because
 * checking in sandbox is not valid to check in.
 *
 * If cheking in an A Deck sandbox, conflicted DTG may be set.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class ConflictSandbox {

    public static final int SBOX_NOT_EXIST = -1;

    public static final int SBOX_INVALID = -2;

    public static final int SBOX_SUBMITTED = -3;

    @DynamicSerializeElement
    private int sandboxId;

    @DynamicSerializeElement
    private Calendar dtg = null;

    @DynamicSerializeElement
    private String description = "";

    public ConflictSandbox() {

    }

    public ConflictSandbox(int sandboxId, Calendar dtg) {
        if (sandboxId < 0) {
            this.description = setInvalidSandboxAccessMessage(sandboxId);
        }
        this.sandboxId = sandboxId;
        if (dtg != null) {
            this.dtg = dtg;
        }
    }

    private static String setInvalidSandboxAccessMessage(int id) {
        String msg = "";
        switch (id) {
        case SBOX_NOT_EXIST:
            msg = "Submitting sandbox is not existing";
            break;
        case SBOX_INVALID:
            msg = "Submitting sandbox is invalid for checkin";
            break;
        case SBOX_SUBMITTED:
            msg = "Submitting sandbox had been submitted";
            break;
        default:
            break;
        }
        return msg;
    }

    public int getsandboxId() {
        return sandboxId;
    }

    public void setSandboxId(int sandboxId) {
        if (sandboxId < 0) {
            this.description = setInvalidSandboxAccessMessage(sandboxId);
        }
        this.sandboxId = sandboxId;
    }

    public Calendar getDtg() {
        return dtg;
    }

    public String getDtgString() {
        String timeString = "";

        if (dtg != null) {
            int year = dtg.get(Calendar.YEAR);
            int month = dtg.get(Calendar.MONTH);
            int day = dtg.get(Calendar.DAY_OF_MONTH);
            int hour = dtg.get(Calendar.HOUR_OF_DAY);
            timeString = String.format("%04d", year)
                    + String.format("%02d", month + 1)
                    + String.format("%02d", day) + String.format("%02d", hour);
        }
        return timeString;
    }

    public void setDtg(Calendar dtg) {
        this.dtg = dtg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("(SandboxID:%d, dtg:%s)", sandboxId,
                getDtgString());
    }

}
