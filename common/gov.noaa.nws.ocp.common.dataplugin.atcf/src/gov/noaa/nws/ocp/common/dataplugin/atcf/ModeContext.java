/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ModeContext
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 28, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ModeContext {

    @DynamicSerializeElement
    private AtcfModeType modeType = AtcfModeType.OP;


    /**
     * @return the modeType
     */
    public AtcfModeType getModeType() {
        return modeType;
    }


    /**
     * @param modeType the modeType to set
     */
    public void setModeType(AtcfModeType modeType) {
        this.modeType = modeType;
    }



}
