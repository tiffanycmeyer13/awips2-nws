/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ClimateProdData A wrapper class for a list of ClimateProduct
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2017 20637      pwang       Initial creation
 * 13 APR  2017 33104      amoore      Address comments from review.
 * 17 APR  2017 33104      amoore      Address comments from review.
 * 11 MAY 2017  33104      amoore      More Find Bugs minor issues.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class ClimateProdData {

    // @DynamicSerializeElement
    // private Map<String, ClimateProduct> prodData;

    @DynamicSerializeElement
    private ClimateProductSet nwrProd;

    @DynamicSerializeElement
    private ClimateProductSet nwwsProd;

    public ClimateProdData() {

    }

    /**
     * Constructor
     * 
     * @param prodData
     */
    public ClimateProdData(Map<String, ClimateProduct> prodData) {
        this.initProductData(prodData);

    }

    /**
     * initProductData
     * 
     * @param prodData
     */
    public void initProductData(Map<String, ClimateProduct> prodData) {
        Map<String, ClimateProduct> nwrProd = new HashMap<>();
        Map<String, ClimateProduct> nwwsProd = new HashMap<>();

        for (Entry<String, ClimateProduct> entry : prodData.entrySet()) {
            String key = entry.getKey();
            ClimateProduct cp = entry.getValue();
            if (PeriodType.isNWWS(cp.getProdType().getSource())) {
                nwwsProd.put(key, cp);
            } else if (PeriodType.isNWR(cp.getProdType().getSource())) {
                nwrProd.put(key, cp);
            }
        }

        this.nwrProd = new ClimateProductSet(ClimateProductType.NWR, nwrProd);
        this.nwwsProd = new ClimateProductSet(ClimateProductType.NWWS,
                nwwsProd);
    }

    /**
     * getMaxExpiration get the latest expiration DateTime among all products
     * 
     * @return
     */
    public LocalDateTime getMaxExpiration() {

        if (this.nwrProd == null || this.nwwsProd == null) {
            return null;
        }

        LocalDateTime nwrExpiration = this.nwrProd.getMaxExpiration();
        LocalDateTime nwwsExpiration = this.nwwsProd.getMaxExpiration();

        return (nwrExpiration.isAfter(nwwsExpiration)) ? nwrExpiration
                : nwwsExpiration;
    }

    /**
     * isAllProductSent
     * 
     * @return true when all NWR and NWWS have been sent
     */
    public boolean isAllProductSent() {
        return (this.nwrProd.isAllProductSent()
                && this.nwwsProd.isAllProductSent());
    }

    /**
     * isAllNWRProductSent
     * 
     * @return true if all NWR products have been sent
     */
    public boolean isAllNWRProductSent() {
        return this.nwrProd.isAllProductSent();
    }

    /**
     * isAllNWWSProductSent
     * 
     * @return true if all NWWS products have been sent
     */
    public boolean isAllNWWSProductSent() {
        return this.nwwsProd.isAllProductSent();
    }

    /**
     * isEmpty
     * 
     * @return true if no product
     */
    public boolean isEmpty() {
        return (this.nwrProd.isEmpty() && this.nwwsProd.isEmpty());
    }

    /**
     * containsClimateProd
     * 
     * @param key
     * @return true if key exists
     */
    public boolean containsClimateProd(String key) {
        return (this.nwrProd.containsClimateProd(key)
                || this.nwwsProd.containsClimateProd(key));
    }

    /**
     * replaceClimateProd
     * 
     * @param key
     * @param prod
     */
    public void replaceClimateProd(ClimateProductType prodType, String key,
            ClimateProduct prod) {
        switch (prodType) {
        case NWR:
            this.nwrProd.replaceClimateProd(key, prod);
            break;

        case NWWS:
            this.nwwsProd.replaceClimateProd(key, prod);
            break;
        default:
            break;

        }
    }

    /**
     * deleteClimateProd
     * 
     * @param key
     * @return true if deleted, false if key did not exist.
     */
    public boolean deleteClimateProd(ClimateProductType prodType, String key) {
        boolean deleted = false;

        switch (prodType) {
        case NWR:
            deleted = this.nwrProd.deleteClimateProd(key);
            break;

        case NWWS:
            deleted = this.nwwsProd.deleteClimateProd(key);
            break;
        default:
            break;
        }
        return deleted;
    }

    /**
     * updateProductGrooupLevelStatus
     * 
     * @param prodType
     * @param status
     * @param statusDesc
     */
    public void updateProductSetLevelStatus(ClimateProductType prodType,
            ProductSetStatus status, String statusDesc) {
        switch (prodType) {
        case NWR:
            this.nwrProd.setProdStatus(status, statusDesc);
            break;

        case NWWS:
            this.nwwsProd.setProdStatus(status, statusDesc);
            break;
        default:
            break;
        }
    }

    /**
     * getProductGrooupLevelStatus
     * 
     * @param prodType
     * @return the review status for NWR or NWWS product groups
     */
    public ProductSetStatus getProductSetLevelStatus(
            ClimateProductType prodType) {
        ProductSetStatus glevelStatus = ProductSetStatus.HAS_ERROR;
        glevelStatus.setDescription(
                "Unknown Group Level review status for the products: "
                        + prodType);
        switch (prodType) {
        case NWR:
            glevelStatus = this.nwrProd.getProdStatus();
            break;

        case NWWS:
            glevelStatus = this.nwwsProd.getProdStatus();
            break;
        default:
            break;
        }

        return glevelStatus;
    }

    /**
     * @return the nwrProd
     */
    public ClimateProductSet getNwrProd() {
        return nwrProd;
    }

    /**
     * getNwrProdusts
     * 
     * @return
     */
    public Map<String, ClimateProduct> getNwrProdusts() {
        return nwrProd.getProdData();
    }

    /**
     * @param nwrProd
     *            the nwrProd to set
     */
    public void setNwrProd(ClimateProductSet nwrProd) {
        this.nwrProd = nwrProd;
    }

    /**
     * @return the nwwsProd
     */
    public ClimateProductSet getNwwsProd() {
        return nwwsProd;
    }

    /**
     * getNwwsProdusts
     * 
     * @return
     */
    public Map<String, ClimateProduct> getNwwsProdusts() {
        return nwwsProd.getProdData();
    }

    /**
     * @param nwwsProd
     *            the nwwsProd to set
     */
    public void setNwwsProd(ClimateProductSet nwwsProd) {
        this.nwwsProd = nwwsProd;
    }

}
