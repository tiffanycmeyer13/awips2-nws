/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.auth.user.IUser;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;

/**
 * Transmit request for a PSH product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2017 #36930     jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * Mar 23, 2018 #48177     wpaintsil   Add user field.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class PshProductTransmitRequest implements IServerRequest {

    @DynamicSerializeElement
    private PshData product;

    @DynamicSerializeElement
    private boolean operational;

    @DynamicSerializeElement
    private IUser user;

    /**
     * Constructor
     */
    public PshProductTransmitRequest() {
    }

    /**
     * Constructor
     * 
     * @param pshData
     * @param operational
     */
    public PshProductTransmitRequest(PshData pshData, boolean operational,
            IUser user) {
        this.product = pshData;
        this.operational = operational;
        this.user = user;
    }

    /**
     * @return the product
     */
    public PshData getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(PshData product) {
        this.product = product;
    }

    /**
     * @return the operational
     */
    public boolean isOperational() {
        return operational;
    }

    /**
     * @param operational
     *            the operational to set
     */
    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    /**
     * @return the user
     */
    public IUser getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(IUser user) {
        this.user = user;
    }

}