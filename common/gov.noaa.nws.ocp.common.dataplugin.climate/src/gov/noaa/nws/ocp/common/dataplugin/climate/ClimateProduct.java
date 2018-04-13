/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Climate product contains the text and time generated for a formatted product
 * from Climate Formatter.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2017 21099      wpaintsil   Initial creation
 * Mar 17, 2017 30163      wpaintsil   Change the date-time field from 
 *                                     ZonedDateTime to Calendar.
 * Mar 17, 2017 21099      wpaintsil   Add PIL field
 * Mar 21, 2017 30163      wpaintsil   Add period type, expiration time, 
 *                                     and status fields.
 * Apr 13, 2017 33104      amoore      Address comments from review.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
public class ClimateProduct {

    /**
     * The "file name" of the product output, e.g. "output_am_DCA.nwws"
     */
    @DynamicSerializeElement
    private String name;

    /**
     * The formatted data text contained in the product
     */
    @DynamicSerializeElement
    private String prodText;

    /**
     * The time the product was generated
     */
    @DynamicSerializeElement
    private Calendar time;

    /**
     * The PIL for the product
     */
    @DynamicSerializeElement
    private String pil;

    /**
     * The period type for the text product.
     */
    @DynamicSerializeElement
    private PeriodType prodType;
    
    
    //Action and Status  
    @DynamicSerializeElement
    private ActionOnProduct lastAction = ActionOnProduct.NEW;

    /**
     * The status of the product.
     */
    @DynamicSerializeElement
    private ProductStatus status = ProductStatus.PENDING;
    
    /**
     * The description of status of the product.
     */
    @DynamicSerializeElement
    private String statusDesc = "";

    /**
     * The expiration time of the product.
     */
    @DynamicSerializeElement
    private Calendar expirationTime = null;

    /**
     * Enum representing the possible status of the product.
     * 
     * <pre>
     *
     * SOFTWARE HISTORY
     *
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * Mar 21, 2017 30163      wpaintsil   Initial creation
     *
     * </pre>
     *
     * @author wpaintsil
     * @version 1.0
     */
    @DynamicSerialize
    public enum ProductStatus {
        PENDING(1), STORED(2), ERROR(3), SENT(4),;

        int value;

        private ProductStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Default empty constructor.
     */
    public ClimateProduct() {

    }

    /**
     * Constructor.
     * 
     * @param name
     * @param pil
     * @param prodText
     * @param time
     * @param prodType
     * @param expirationTime
     */
    public ClimateProduct(String name, String pil, String text, Calendar time,
            PeriodType prodType, Calendar expirationTime) {
        this.name = name;
        this.pil = pil;
        this.prodText = text;
        this.time = time;
        this.prodType = prodType;
        this.expirationTime = expirationTime;
        this.status = ProductStatus.PENDING;
    }

    /**
     * Overload Constructor. Current GMT time is passed in by default.
     * 
     * @param name
     * @param pil
     * @param text
     * @param prodType
     * @param expirationTime
     */
    public ClimateProduct(String name, String pil, String text,
            PeriodType prodType, Calendar expirationTime) {
        this(name, pil, text, TimeUtil.newCalendar(), prodType, expirationTime);
    }

    /**
     * Overload Constructor. PIL is not set.
     * 
     * @param name
     * @param text
     * @param time
     */
    public ClimateProduct(String name, String text, Calendar time) {
        this(name, null, text, time, null, null);
    }

    /**
     * Overload Constructor. Current GMT time is passed in by default.
     * 
     * @param name
     * @param text
     * @param prodType
     * @param expirationTime
     */
    public ClimateProduct(String name, String text, PeriodType prodType,
            Calendar expirationTime) {
        this(name, null, text, prodType, expirationTime);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the text
     */
    public String getProdText() {
        return prodText;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setProdText(String text) {
        this.prodText = text;
    }

    /**
     * @return the time
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * @param time
     *            the time to set
     */
    public void setTime(Calendar time) {
        this.time = time;
    }

    /**
     * @return the PIL
     */
    public String getPil() {
        return pil;
    }

    /**
     * @param time
     *            the PIL to set
     */
    public void setPil(String pil) {
        this.pil = pil;
    }

    /**
     * 
     * @return the prodType
     */
    public PeriodType getProdType() {
        return prodType;
    }

    /**
     * @param prodType
     *            the prodType to set
     */
    public void setProdType(PeriodType prodType) {
        this.prodType = prodType;
    }

    /**
     * @return the status
     */
    public ProductStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    /**
     * @return the expirationTime
     */
    public Calendar getExpirationTime() {
        return expirationTime;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Set the expiration time based on a ClimateDate and ClimateTime because
     * the expiration time is stored as a ClimateDate and ClimateTime in
     * {@link ClimateProductHeader};
     * 
     * @param date
     * @param time
     */
    public void setExpirationTime(ClimateDate date, ClimateTime time) {
        expirationTime.set(date.getYear(), date.getMon(), date.getDay(),
                time.getHour(), time.getMin());
    }
    
    /**
     * isNWR
     * @return boolean
     */
    public boolean isNWR() {
        return PeriodType.isNWR(this.getProdType().getSource());
    }
    
    /**
     * isNWWS
     * @return boolean
     */
    public boolean isNWWS() {
        return PeriodType.isNWWS(this.getProdType().getSource());
    }

    /**
     * @return the statusDesc
     */
    public String getStatusDesc() {
        return statusDesc;
    }

    /**
     * @param statusDesc the statusDesc to set
     */
    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    /**
     * @return the lastAction
     */
    public ActionOnProduct getLastAction() {
        return lastAction;
    }

    /**
     * @param lastAction the lastAction to set
     */
    public void setLastAction(ActionOnProduct lastAction) {
        this.lastAction = lastAction;
    }
    
    /**
     * @param lastAction
     * @param actionDesc
     */
    public void setLastAction(ActionOnProduct lastAction, String actionDesc) {
        this.lastAction = lastAction;
        this.lastAction.setDescription(actionDesc);
    }
    
    
}
