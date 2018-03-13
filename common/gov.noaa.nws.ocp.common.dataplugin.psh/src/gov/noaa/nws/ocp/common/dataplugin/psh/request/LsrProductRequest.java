/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request for LSR products taken from the textdb.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 07, 2017 35102       wpaintsil   Initial creation
 * Aug 08, 2017 36369       wpaintsil   Product ID field is unnecessary.
 * Aug 09, 2017 36369       wpaintsil   Add lsrHeader and operational fields.
 * Jan 11, 2018 DCS19326    wpaintsil   Baseline version.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
public class LsrProductRequest implements IServerRequest {

    @DynamicSerializeElement
    private String lsrHeader;

    @DynamicSerializeElement
    private boolean operational;

    /**
     * Empty constructor
     */
    public LsrProductRequest() {

    }

    /**
     * Constructor
     * 
     * @param lsrHeader
     * @param operational
     */
    public LsrProductRequest(String lsrHeader, boolean operational) {
        this.lsrHeader = lsrHeader;
        this.operational = operational;
    }

    /**
     * @return the lsrHeader
     */
    public String getLsrHeader() {
        return lsrHeader;
    }

    /**
     * @param lsrHeader
     *            the lsrHeader to set
     */
    public void setLsrHeader(String lsrHeader) {
        this.lsrHeader = lsrHeader;
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

}
