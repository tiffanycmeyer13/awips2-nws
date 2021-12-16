/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;

/**
 * CheckinDeckRequest
 * 
 * This service will physically merge all changes made in the sandbox back to
 * the baseline b_deck. After successfully checked in, the sandbox will be
 * cleared up. The caller should check returned value (Integer), which may
 * indicated some error conditions: 1) -1: no such sandbox ( wrong sandboxId) 2)
 * -2: the sandbox is no longer valid to be checked in, someone might checked in
 *
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2019 #60291     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CheckinDeckRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private int sandboxid;

    @DynamicSerializeElement
    private String userId;

    public CheckinDeckRequest() {

    }

    public CheckinDeckRequest(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the deckType
     */
    public AtcfDeckType getDeckType() {
        return deckType;
    }

    /**
     * @param deckType
     *            the deckType to set
     */
    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the sandboxid
     */
    public int getSandboxid() {
        return sandboxid;
    }

    /**
     * @param sandboxid
     *            the sandboxid to set
     */
    public void setSandboxid(int sandboxid) {
        this.sandboxid = sandboxid;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
