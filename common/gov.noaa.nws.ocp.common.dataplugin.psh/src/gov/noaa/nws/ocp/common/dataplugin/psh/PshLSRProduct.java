/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class containing LSR product information. The LSR File Manager dialog
 * displays a list of products by texdb creation time.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 07, 2017 35102      wpaintsil   Initial creation
 * Aug 07, 2017 36369      wpaintsil   Rename to PshLSRProduct.
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
public class PshLSRProduct {

    /**
     * The creation time returned from the textdb. E.g. whatever is returned
     * from "textdb -A MIALSRTBW."
     */
    @DynamicSerializeElement
    private Calendar creationTime;

    /**
     * Text of the LSR product.
     */
    @DynamicSerializeElement
    private String lsrText;

    /**
     * Constructor.
     */
    public PshLSRProduct() {
    }

    /**
     * Constructor.
     */
    public PshLSRProduct(Calendar creationTime, String lsrText) {
        this.creationTime = creationTime;
        this.lsrText = lsrText;
    }

    /**
     * @return the creationTime
     */
    public Calendar getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime
     *            the creationTime to set
     */
    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the lsrText
     */
    public String getLsrText() {
        return lsrText;
    }

    /**
     * @param lsrText
     *            the lsrText to set
     */
    public void setLsrText(String lsrText) {
        this.lsrText = lsrText;
    }

}
