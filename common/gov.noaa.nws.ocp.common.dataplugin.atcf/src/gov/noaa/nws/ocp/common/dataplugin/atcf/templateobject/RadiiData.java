/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class to represent the formatted wind radii (34/50/64 kt) or wave radii (12
 * ft seas).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2021 87783      jwu         Initial coding.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class RadiiData {

    /**
     * Radii, 64, 50, 34, or 12.
     */
    @DynamicSerializeElement
    private String rad;

    /**
     * Radii in each quadrant (unit - nm).
     */
    @DynamicSerializeElement
    private String[] quad;

    /**
     * Constructor
     */
    public RadiiData() {
        this.rad = "";

        this.quad = new String[4];
        for (int ii = 0; ii < 4; ii++) {
            this.quad[ii] = "///";
        }
    }

    /**
     * @return the rad
     */
    public String getRad() {
        return rad;
    }

    /**
     * @param rad
     *            the rad to set
     */
    public void setRad(String rad) {
        this.rad = rad;
    }

    /**
     * @return the quad
     */
    public String[] getQuad() {
        return quad;
    }

    /**
     * @param quad
     *            the quad to set
     */
    public void setQuad(String[] quad) {
        this.quad = quad;
    }

}
