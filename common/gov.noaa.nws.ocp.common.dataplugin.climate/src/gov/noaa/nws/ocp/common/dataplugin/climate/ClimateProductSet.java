/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct.ProductStatus;

/**
 * TClimateProductsAndStatus
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 3, 2017             pwang       Initial creation
 * 11 MAY 2017  33104      amoore      More Find Bugs minor issues.
 * 02 NOV 2016  40210      wpaintsil   Months for the Calendar object are zero-indexed, 
 *                                     whereas LocalDateTime months are one-indexed.
 * 09 AUG 2018  DR20837    wpaintsil   Shift expiration times to the next day.
 * 16 AUG 2018  DR20837    wpaintsil   Remove code that shifted product expiration to the following day.
 * 
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class ClimateProductSet {

    @DynamicSerializeElement
    private ClimateProductType prodType;

    @DynamicSerializeElement
    private Map<String, ClimateProduct> prodData;

    @DynamicSerializeElement
    private ProductSetStatus prodStatus = ProductSetStatus.PENDING;

    /**
     * The logger.
     */
    private final IUFStatusHandler logger = UFStatus.getHandler(getClass());

    /**
     * Empty Constructor
     */
    public ClimateProductSet() {

    }

    /**
     * Constructor
     * 
     * @param prodType
     * @param prodData
     */
    public ClimateProductSet(ClimateProductType prodType,
            Map<String, ClimateProduct> prodData) {
        this.prodType = prodType;
        this.prodData = prodData;
    }

    /**
     * getMaxExpiration get the latest expiration DateTime among all products
     * 
     * @return
     */
    public LocalDateTime getMaxExpiration() {

        if (prodData == null) {
            return null;
        }

        // Ensure the expiration time is a day later.
        LocalDateTime expiration = LocalDateTime.now();

        for (ClimateProduct cp : prodData.values()) {
            if (cp.getExpirationTime() != null
                    && cp.getStatus() != ProductStatus.SENT) {
                expiration = getLaterExpiration(cp.getExpirationTime(),
                        expiration);

                logger.info(cp.getPil() + " product expiration time: "
                        + new SimpleDateFormat("hh:mm aaa, MM/dd/yyyy")
                                .format(cp.getExpirationTime().getTime()));
            }
        }
        logger.info("Latest expiration time: " + expiration
                .format(DateTimeFormatter.ofPattern("hh:mm a, MM/dd/yyyy")));
        return expiration;
    }

    /**
     * getLaterExpriration
     * 
     * @param prodExp
     * @param currentExp
     * @return latest expiration date time
     */
    private LocalDateTime getLaterExpiration(Calendar prodExp,
            LocalDateTime currentExp) {

        LocalDateTime prodexp = LocalDateTime.ofInstant(prodExp.toInstant(),
                prodExp.getTimeZone() == null ? ZoneId.systemDefault()
                        : prodExp.getTimeZone().toZoneId());

        if (prodexp.isAfter(currentExp)) {
            return prodexp;
        } else {
            return currentExp;
        }
    }

    /**
     * updateProductStatus
     * 
     * @param prodKey
     * @param status
     * @param desc
     * @return false if not able to update
     */
    public boolean updateProductStatus(String prodKey, ProductStatus status,
            String desc) {
        boolean updated = false;

        if (!this.containsClimateProd(prodKey)) {
            return updated;
        }

        this.prodData.get(prodKey).setStatus(status);
        this.prodData.get(prodKey).setStatusDesc(desc);

        return true;
    }

    /**
     * isAllProductSent
     * 
     * @return
     */
    public boolean isAllProductSent() {
        for (ClimateProduct cp : prodData.values()) {
            if (cp.getStatus().getValue() < ProductStatus.SENT.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * updateSetLevelStatusFromProductStatus
     */
    public void updateSetLevelStatusFromProductStatus() {

        if (this.isAllProductSent()) {
            prodStatus = ProductSetStatus.SENT;
        } else if (getNumberOfProductsForStatus(ProductStatus.ERROR) > 0) {
            prodStatus = ProductSetStatus.HAS_ERROR;
            ClimateProduct errorProd = this
                    .getProdWithStatus(ProductStatus.ERROR);
            String msg = "There is error in the " + this.prodType
                    + " products, happened when " + errorProd.getLastAction()
                    + " with reason: "
                    + errorProd.getLastAction().getDescription();

            prodStatus.setDescription(msg);

        } else {
            prodStatus = ProductSetStatus.PENDING;
        }
    }

    /**
     * getProdWithStatus
     * 
     * @param status
     * @return
     */
    private ClimateProduct getProdWithStatus(ProductStatus status) {
        for (ClimateProduct cp : this.prodData.values()) {
            if (cp.getStatus() == status) {
                return cp;
            }
        }
        return null;
    }

    /**
     * @return the prodData
     */
    public Map<String, ClimateProduct> getProdData() {
        return prodData;
    }

    /**
     * @param prodData
     *            the prodData to set
     */
    public void setProdData(Map<String, ClimateProduct> prodData) {
        this.prodData = prodData;
    }

    /**
     * getUnsentProducts
     * 
     * @return all products have not been sent
     */
    public Map<String, ClimateProduct> getUnsentProducts() {
        Map<String, ClimateProduct> unsent = new HashMap<>();
        for (Entry<String, ClimateProduct> entry : this.prodData.entrySet()) {
            String key = entry.getKey();
            ClimateProduct cp = entry.getValue();
            if (cp.getStatus() != ProductStatus.SENT) {
                unsent.put(key, cp);
            }
        }
        return unsent;
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

    /**
     * @return the prodStatus
     */
    public ProductSetStatus getProdStatus() {
        return prodStatus;
    }

    /**
     * @param prodStatus
     *            the prodStatus to set
     */
    public void setProdStatus(ProductSetStatus prodStatus) {
        this.prodStatus = prodStatus;
    }

    public void setProdStatus(ProductSetStatus prodStatus, String desc) {
        this.prodStatus = prodStatus;
        this.prodStatus.setDescription(desc);
    }

    /**
     * isEmpty
     * 
     * @return true if no product
     */
    public boolean isEmpty() {
        return this.prodData.isEmpty();
    }

    /**
     * getNumberOfProductsForStatus
     * 
     * @param status
     * @return -1 if no products: counts for given status
     */
    public int getNumberOfProductsForStatus(ProductStatus status) {
        int numProds = 0;
        if (this.prodData == null || this.prodData.isEmpty()) {
            return -1;
        }
        for (ClimateProduct cp : this.prodData.values()) {
            if (cp.getStatus() == status) {
                numProds++;
            }
        }
        return numProds;
    }

    /**
     * getNumberOfUnsent
     * 
     * @return number of unsent products
     */
    public int getNumberOfUnsent() {
        int numProds = 0;
        if (this.prodData == null || this.prodData.isEmpty()) {
            return -1;
        }
        for (ClimateProduct cp : this.prodData.values()) {
            if (cp.getStatus() != ProductStatus.SENT) {
                numProds++;
            }
        }
        return numProds;
    }

    /**
     * containsClimateProd
     * 
     * @param key
     * @return boolean
     */
    public boolean containsClimateProd(String key) {
        return this.prodData.containsKey(key);
    }

    /**
     * replaceClimateProd
     * 
     * @param key
     * @param prod
     */
    public void replaceClimateProd(String key, ClimateProduct prod) {
        this.prodData.replace(key, prod);
    }

    /**
     * deleteClimateProd
     * 
     * @param key
     * @return true if deleted, false if key did not exist.
     */
    public boolean deleteClimateProd(String key) {
        return this.prodData.remove(key) != null ? true : false;
    }

}
