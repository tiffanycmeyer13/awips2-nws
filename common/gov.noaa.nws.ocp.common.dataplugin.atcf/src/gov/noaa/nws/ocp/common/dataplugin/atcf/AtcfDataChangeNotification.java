/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Arrays;
import java.util.Date;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Object to contain notification of ATCF baseline data changes.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 28, 2019 67881      jwu         Initial creation
 * May 12, 2020 #78298     pwang       Add functions to handle deck record merging
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

@DynamicSerialize
public class AtcfDataChangeNotification {

    @DynamicSerializeElement
    private Date modTime;

    // Deck type for submitted changes
    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private BaselineChangeActionType actionType = BaselineChangeActionType.EDIT;

    // sandbox that is submitted and caused changes
    @DynamicSerializeElement
    private int sourceSandboxId;

    // user who submits and makes changes
    @DynamicSerializeElement
    private String sourceUserId;

    // sandboxes affected by the changes
    @DynamicSerializeElement
    private ConflictSandbox[] affectedSandboxes;

    /**
     * Constructor
     */
    public AtcfDataChangeNotification() {
    }

    /**
     * Constructor
     *
     * @param modTime
     * @param deckType
     * @param modSandboxId
     * @param modUserId
     * @param affectedSandboxes
     */
    public AtcfDataChangeNotification(Date modTime, AtcfDeckType deckType,
            int sourceSandboxId, String sourceUserId,
            ConflictSandbox[] affectedSandboxes) {
        this.modTime = modTime;
        this.deckType = deckType;
        this.sourceSandboxId = sourceSandboxId;
        this.sourceUserId = sourceUserId;
        this.affectedSandboxes = affectedSandboxes;
    }

    /**
     * @return the modTime
     */
    public Date getModTime() {
        return modTime;
    }

    /**
     * @param modTime
     *            the modTime to set
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * @return the deckType
     */
    public AtcfDeckType getDeckType() {
        return deckType;
    }

    /**
     * @return the actionType
     */
    public BaselineChangeActionType getActionType() {
        return actionType;
    }

    /**
     * @param actionType
     *            the actionType to set
     */
    public void setActionType(BaselineChangeActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * @param deckType
     *            the deckType to set
     */
    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the sourceSandboxId
     */
    public int getSourceSandboxId() {
        return sourceSandboxId;
    }

    /**
     * @param sourceSandboxId
     *            the sourceSandboxId to set
     */
    public void setSourceSandboxId(int sourceSandboxId) {
        this.sourceSandboxId = sourceSandboxId;
    }

    /**
     * @return the sourceUserId
     */
    public String getSourceUserId() {
        return sourceUserId;
    }

    /**
     * @param sourceUserId
     *            the sourceUserId to set
     */
    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    /**
     * @return the affectedSandboxes
     */
    public ConflictSandbox[] getAffectedSandboxes() {
        return affectedSandboxes;
    }

    /**
     * @param affectedSandboxes
     *            the affectedSandboxes to set
     */
    public void setAffectedSandboxes(ConflictSandbox[] affectedSandboxes) {
        this.affectedSandboxes = affectedSandboxes;
    }

    @Override
    public String toString() {
        return " modTime:" + modTime.toString() + " deckType:"
                + deckType.getValue() + " sourceSandboxId:" + sourceSandboxId
                + " sourceUserId:" + sourceUserId + " affectedSandboxes:"
                + Arrays.toString(affectedSandboxes);
    }
}
