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
 * MergeDeckRequest
 * deckFile should be a full path /configured_dir/subdir/filename.dat
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2019  #78298    pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class MergeDeckRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private String deckFile;

    @DynamicSerializeElement
    private Storm storm;

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
     * @return the deckFile
     */
    public String getDeckFile() {
        return deckFile;
    }

    /**
     * @param deckFile the deckFile to set
     */
    public void setDeckFile(String deckFile) {
        this.deckFile = deckFile;
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

}


