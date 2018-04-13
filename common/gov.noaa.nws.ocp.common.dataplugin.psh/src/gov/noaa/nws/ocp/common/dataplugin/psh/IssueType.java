/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Represents the message types used for transmitting a PSH product, including
 * an ID such as ROU, AAA, AAB ..., CCA, CCB; a category (Routine, Updated,
 * Corrected); A reason for update/correction; and a date.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 02, 2017            pwang       Initial creation
 * Sep 14, 2017 #37365     jwu         Add ROUTINE/UPDATED/CORRECTED for category.
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class IssueType {

    @DynamicSerializeElement
    @XmlAttribute
    protected String id;

    @DynamicSerializeElement
    @XmlAttribute
    protected String category;

    @DynamicSerializeElement
    @XmlAttribute
    protected Calendar date;

    @DynamicSerializeElement
    @XmlAttribute
    protected String reason;

    /**
     * Categories for issue type.
     */
    public static String ROUTINE = "Routine";

    public static String UPDATED = "Updated";

    public static String CORRECTED = "Corrected";

    /**
     * Constructor
     */
    public IssueType() {

    }

    /**
     * Constructor
     *
     * @param id
     * @param category
     * @param date
     * @param reason
     */
    public IssueType(String id, String category, Calendar date, String reason) {
        this.id = id;
        this.date = date;
        this.reason = reason;

        setCategory(category);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            the category to set
     */
    public void setCategory(String category) {
        String cat = ROUTINE;
        if (category != null && (category.equals(ROUTINE)
                || category.equals(UPDATED) || category.equals(CORRECTED))) {
            cat = category;
        }

        this.category = cat;
    }

    /**
     * @return the date
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * @param date
     *            the date to set
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason
     *            the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Check if this is a "Routine" type.
     *
     * @return isRoutine()
     */
    public boolean isRoutine() {
        return category.equalsIgnoreCase(ROUTINE);
    }

    /**
     * Check if this is a "Updated" type.
     *
     * @return isUpdated()
     */
    public boolean isUpdated() {
        return category.equalsIgnoreCase(UPDATED);
    }

    /**
     * Check if this is a "Corrected" type.
     *
     * @return isCorrected()
     */
    public boolean isCorrected() {
        return category.equalsIgnoreCase(CORRECTED);
    }

}