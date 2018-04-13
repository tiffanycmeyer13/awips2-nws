/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct.ProductStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.ProductSetStatus;

/**
 * SendClimateProductsResponse
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 18, 2017            pwang       Initial creation
 * May 05, 2017            pwang       Re-designed status and error handling
 * 11 MAY 2017  33104      amoore      More Find Bugs minor issues.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class SendClimateProductsResponse {

    @DynamicSerializeElement
    private String cpgSessionId;

    @DynamicSerializeElement
    private Map<String, ClimateProduct> sendingProducts = new HashMap<>();;

    @DynamicSerializeElement
    private ProductSetStatus setLevelStatus = ProductSetStatus.UNKNOWN;

    /**
     * Empty constructor
     */
    public SendClimateProductsResponse() {

    }

    /**
     * Constructor
     * 
     * @param cpgSessionId
     */
    public SendClimateProductsResponse(String cpgSessionId) {
        this.cpgSessionId = cpgSessionId;
    }

    /**
     * hasError
     * 
     * @return true if there set level status has error
     */
    public boolean hasError() {
        return (setLevelStatus == ProductSetStatus.HAS_ERROR
                || setLevelStatus == ProductSetStatus.FATAL_ERROR) ? true
                        : false;
    }

    /**
     * numberOfProductsForStatus
     * 
     * @param status
     * @return number of products match given ProductStatus
     */
    public int numberOfProductsForStatus(ProductStatus status) {
        int numProds = 0;
        if (this.sendingProducts == null || this.sendingProducts.isEmpty()) {
            return -1;
        }
        for (ClimateProduct cp : this.sendingProducts.values()) {
            if (cp.getStatus() == status) {
                numProds++;
            }
        }
        return numProds;
    }

    /**
     * getProductsForStatus
     * 
     * @param status
     * @return product map which match given status
     */
    public Map<String, ClimateProduct> getProductsForStatus(
            ProductStatus status) {
        Map<String, ClimateProduct> stProducts = new HashMap<>();

        if (this.sendingProducts == null || this.sendingProducts.isEmpty()) {
            return null;
        }
        for (Entry<String, ClimateProduct> entry : this.sendingProducts
                .entrySet()) {
            ClimateProduct cp = entry.getValue();
            if (cp.getStatus() == status) {
                String key = entry.getKey();
                stProducts.put(key, cp);
            }
        }
        return stProducts;
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
     * @return the sendingProducts
     */
    public Map<String, ClimateProduct> getSendingProducts() {
        return sendingProducts;
    }

    /**
     * @param sendingProducts
     *            the sendingProducts to set
     */
    public void setSendingProducts(
            Map<String, ClimateProduct> sendingProducts) {
        this.sendingProducts = sendingProducts;
    }

    /**
     * @return the setLevelStatus
     */
    public ProductSetStatus getSetLevelStatus() {
        return setLevelStatus;
    }

    /**
     * @param setLevelStatus
     *            the setLevelStatus to set
     */
    public void setSetLevelStatus(ProductSetStatus setLevelStatus) {
        this.setLevelStatus = setLevelStatus;
    }

    /**
     * @param setLevelStatus
     * @param statusDesc
     */
    public void setSetLevelStatus(ProductSetStatus setLevelStatus,
            String statusDesc) {
        this.setLevelStatus = setLevelStatus;
        this.setLevelStatus.setDescription(statusDesc);
    }

}
