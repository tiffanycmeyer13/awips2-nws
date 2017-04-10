/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;

/**
 * SaveModifiedClimateProdAfterReviewRequest
 * 
 * This Request should only be called when Climate Product Reviewer When one
 * product being modified and user confirm to save the changes
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016 20637      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class SaveModifiedClimateProdAfterReviewRequest
        implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionId;

    @DynamicSerializeElement
    private String prodKey;

    @DynamicSerializeElement
    private ClimateProductType prodType;

    @DynamicSerializeElement
    private ClimateProduct saveProd;

    // TODO: may need to send back the climate product objects if the product
    // modified

    /**
     * Empty constructor.
     */
    public SaveModifiedClimateProdAfterReviewRequest() {
    }

    /**
     * Constructor
     * 
     * @param cpgSessionId
     * @param prodKey
     * @param prod
     */
    public SaveModifiedClimateProdAfterReviewRequest(String cpgSessionId,
            ClimateProductType prodType, String prodKey, ClimateProduct prod) {
        this.cpgSessionId = cpgSessionId;
        this.prodType = prodType;
        this.prodKey = prodKey;
        this.saveProd = prod;
    }

    /**
     * @return the cpgSessionId
     */
    public String getCpgSessionId() {
        return cpgSessionId;
    }

    /**
     * @param cpgSessionId
     *            the cpgSessionId to set
     */
    public void setCpgSessionId(String cpgSessionId) {
        this.cpgSessionId = cpgSessionId;
    }

    /**
     * @return the prodKey
     */
    public String getProdKey() {
        return prodKey;
    }

    /**
     * @param prodKey
     *            the prodKey to set
     */
    public void setProdKey(String prodKey) {
        this.prodKey = prodKey;
    }

    /**
     * @return the saveProd
     */
    public ClimateProduct getSaveProd() {
        return saveProd;
    }

    /**
     * @param saveProd
     *            the saveProd to set
     */
    public void setSaveProd(ClimateProduct saveProd) {
        this.saveProd = saveProd;
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
