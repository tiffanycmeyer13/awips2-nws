/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;


/**
 * DeckReplaceRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 19, 2020 #          pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DeckReplaceRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private Storm storm;

    @DynamicSerializeElement
    private String absDeckfilePath;


    /**
     * @return the deckType
     */
    public AtcfDeckType getDeckType() {
        return deckType;
    }

    /**
     * @param deckType the deckType to set
     */
    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    /**
     * @return the absDeckfilePath
     */
    public String getAbsDeckfilePath() {
        return absDeckfilePath;
    }

    /**
     * @param absDeckfilePath the absDeckfilePath to set
     */
    public void setAbsDeckfilePath(String absDeckfilePath) {
        this.absDeckfilePath = absDeckfilePath;
    }


}

