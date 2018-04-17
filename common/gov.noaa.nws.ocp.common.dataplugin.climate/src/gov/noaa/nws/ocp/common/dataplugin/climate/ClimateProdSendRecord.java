/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Record of a sent Climate product.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 26, 2017            pwang       Initial creation
 * 03 MAY 2017  33533      amoore      Make file name nullable.
 * 11 MAY 2017  33104      amoore      User ID nullable (match DB).
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateProdSendRecord
        implements Comparable<ClimateProdSendRecord> {

    @Column(nullable = false)
    @DynamicSerializeElement
    @XmlAttribute
    private String prod_id;

    @DynamicSerializeElement
    @XmlAttribute
    private String period_type;

    @Column(nullable = false)
    @DynamicSerializeElement
    @XmlAttribute
    private String prod_type;

    @DynamicSerializeElement
    @XmlAttribute
    private String file_name;

    @Column(nullable = false)
    @DynamicSerializeElement
    @XmlAttribute
    private String prod_text;

    @Column(nullable = false)
    @DynamicSerializeElement
    @XmlAttribute
    private Timestamp send_time;

    @DynamicSerializeElement
    @XmlAttribute
    private String user_id;

    /**
     * Constructor
     */
    public ClimateProdSendRecord() {
    }

    public Map<String, Object> getColumnValues() {
        Map<String, Object> rval = new LinkedHashMap<>();

        rval.put("prod_id", prod_id);
        rval.put("period_type", period_type);
        rval.put("prod_type", prod_type);
        rval.put("file_name", file_name);
        rval.put("prod_text", prod_text);
        rval.put("send_time", send_time);
        rval.put("user_id", user_id);

        return rval;
    }

    @Override
    public int compareTo(ClimateProdSendRecord obj) {
        return this.send_time.compareTo(obj.getSend_time());
    }

    /**
     * @return the prod_id
     */
    public String getProd_id() {
        return prod_id;
    }

    /**
     * @param prod_id
     *            the prod_id to set
     */
    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }

    /**
     * @return the period_type
     */
    public String getPeriod_type() {
        return period_type;
    }

    /**
     * @param period_type
     *            the period_type to set
     */
    public void setPeriod_type(String period_type) {
        this.period_type = period_type;
    }

    /**
     * @return the prod_type
     */
    public String getProd_type() {
        return prod_type;
    }

    /**
     * @param prod_type
     *            the prod_type to set
     */
    public void setProd_type(String prod_type) {
        this.prod_type = prod_type;
    }

    /**
     * @return the file_name
     */
    public String getFile_name() {
        return file_name;
    }

    /**
     * @param file_name
     *            the file_name to set
     */
    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    /**
     * @return the prod_text
     */
    public String getProd_text() {
        return prod_text;
    }

    /**
     * @param prod_text
     *            the prod_text to set
     */
    public void setProd_text(String prod_text) {
        this.prod_text = prod_text;
    }

    /**
     * @return the send_time
     */
    public Timestamp getSend_time() {
        return send_time;
    }

    /**
     * @param send_time
     *            the send_time to set
     */
    public void setSend_time(Timestamp send_time) {
        this.send_time = send_time;
    }

    /**
     * @return the user_id
     */
    public String getUser_id() {
        return user_id;
    }

    /**
     * @param user_id
     *            the user_id to set
     */
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}
