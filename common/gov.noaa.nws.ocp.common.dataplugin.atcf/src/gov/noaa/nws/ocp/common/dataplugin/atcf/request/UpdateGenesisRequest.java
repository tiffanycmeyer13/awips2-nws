/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;

/**
 * UpdateGenesisRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 10, 2020 79571      wpaintsil   Initial creation
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
public class UpdateGenesisRequest implements IServerRequest {

    @DynamicSerializeElement
    private Genesis genesis;


    /**
     * @return the genesis
     */
    public Genesis getGenesis() {
        return genesis;
    }

    /**
     * @param genesis
     *            the genesis to set
     */
    public void setGenesis(Genesis genesis) {
        this.genesis = genesis;
    }

}
