/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;

/**
 * DeleteClimateProdAfterReview
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2, 2017  20637      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DeleteClimateProdAfterReviewRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID;

    @DynamicSerializeElement
    private ClimateProductType prodType;

    @DynamicSerializeElement
    private String deleteProdKey;

    /**
     * Empty constructor.
     */
    public DeleteClimateProdAfterReviewRequest() {
    }

    /**
     * Constructor
     * 
     * @param cpgSessionId
     * @param prodKey
     */
    public DeleteClimateProdAfterReviewRequest(String cpgSessionId,
            ClimateProductType prodType, String prodKey) {
        this.cpgSessionID = cpgSessionId;
        this.prodType = prodType;
        this.deleteProdKey = prodKey;
    }

    /**
     * @return the cpgSessionID
     */
    public String getCpgSessionID() {
        return cpgSessionID;
    }

    /**
     * @param cpgSessionID
     *            the cpgSessionID to set
     */
    public void setCpgSessionID(String cpgSessionID) {
        this.cpgSessionID = cpgSessionID;
    }

    /**
     * @return the deleteProdKey
     */
    public String getDeleteProdKey() {
        return deleteProdKey;
    }

    /**
     * @param deleteProdKey
     *            the deleteProdKey to set
     */
    public void setDeleteProdKey(String deleteProdKey) {
        this.deleteProdKey = deleteProdKey;
    }

    /**
     * @return the prodType
     */
    public ClimateProductType getProdType() {
        return prodType;
    }

    /**
     * @param prodType
     *            the prodType to set
     */
    public void setProdType(ClimateProductType prodType) {
        this.prodType = prodType;
    }

}
