/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.util;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * DeckStormInfo: a utility container for the storm infor related to the deck
 * file
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 19, 2020 #78298       pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DeckStormInfo {

    @DynamicSerializeElement
    private boolean status = false;

    @DynamicSerializeElement
    private String message;

    @DynamicSerializeElement
    private Storm storm;

    @DynamicSerializeElement
    private AtcfDeckType deckType = AtcfDeckType.B;

    @DynamicSerializeElement
    private String extType;


    /**
     * @return the status
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
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
     * @return the extTypeDeckStormInfo
     */
    public String getExtType() {
        return extType;
    }

    /**
     * @param extType
     *            the extType to set
     */
    public void setExtType(String extType) {
        this.extType = extType;
    }

}
