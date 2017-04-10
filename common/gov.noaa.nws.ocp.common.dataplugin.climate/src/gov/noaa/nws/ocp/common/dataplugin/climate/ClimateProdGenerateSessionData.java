/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ClimateProdGenerateSessionData
 * 
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 07, 2017 27199      pwang       Initial creation
 * Apr 12, 2017 27199      jwu         Implemented Comparable
 * Apr 13  2017 33104      amoore      Address comments from review.
 * May 05  2017 33532      pwang       removed extends from PersistableObject
 * 
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateProdGenerateSessionData
        implements Comparable<ClimateProdGenerateSessionData> {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String cpg_session_id;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int run_type;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int prod_type;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int state;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int status;

    @DynamicSerializeElement
    private String status_desc;

    @DynamicSerializeElement
    private byte[] global_config;

    @DynamicSerializeElement
    private byte[] prod_setting;

    @DynamicSerializeElement
    private byte[] report_data;

    @DynamicSerializeElement
    private byte[] prod_data;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Timestamp start_at;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Timestamp last_updated;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Timestamp pending_expire;

    /**
     * Constructor
     */
    public ClimateProdGenerateSessionData() {
    }

    public Map<String, Object> getColumnValues() {
        Map<String, Object> rval = new LinkedHashMap<>();

        rval.put("cpg_session_id", cpg_session_id);
        rval.put("run_type", run_type);
        rval.put("prod_type", prod_type);
        rval.put("state", state);
        rval.put("status", status);
        rval.put("status_desc", status_desc);

        // nullable
        rval.put("global_config", global_config);

        // nullable
        rval.put("prod_setting", prod_setting);

        // nullable
        rval.put("report_data", report_data);

        // nullable
        rval.put("prod_data", prod_data);

        rval.put("start_at", start_at);
        rval.put("last_updated", last_updated);
        rval.put("pending_expire", pending_expire);

        return rval;

    }

    /*
     * Getter and setter
     */

    /**
     * @return the cpg_session_id
     */
    public String getCpg_session_id() {
        return cpg_session_id;
    }

    /**
     * @param cpg_session_id
     *            the cpg_session_id to set
     */
    public void setCpg_session_id(String cpg_session_id) {
        this.cpg_session_id = cpg_session_id;
    }

    /**
     * @return the run_type
     */
    public int getRun_type() {
        return run_type;
    }

    /**
     * @param run_type
     *            the run_type to set
     */
    public void setRun_type(int run_type) {
        this.run_type = run_type;
    }

    /**
     * @return the prod_type
     */
    public int getProd_type() {
        return prod_type;
    }

    /**
     * @param prod_type
     *            the prod_type to set
     */
    public void setProd_type(int prod_type) {
        this.prod_type = prod_type;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the status_desc
     */
    public String getStatus_desc() {
        return status_desc;
    }

    /**
     * @param status_desc
     *            the status_desc to set
     */
    public void setStatus_desc(String status_desc) {
        this.status_desc = status_desc;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the global_config
     */
    public byte[] getGlobal_config() {
        return global_config;
    }

    /**
     * @param global_config
     *            the global_config to set
     */
    public void setGlobal_config(byte[] global_config) {
        this.global_config = global_config;
    }

    /**
     * @return the prod_setting
     */
    public byte[] getProd_setting() {
        return prod_setting;
    }

    /**
     * @param prod_setting
     *            the prod_setting to set
     */
    public void setProd_setting(byte[] prod_setting) {
        this.prod_setting = prod_setting;
    }

    /**
     * @return the report_data
     */
    public byte[] getReport_data() {
        return report_data;
    }

    /**
     * @param report_data
     *            the report_data to set
     */
    public void setReport_data(byte[] report_data) {
        this.report_data = report_data;
    }

    /**
     * @return the prod_data
     */
    public byte[] getProd_data() {
        return prod_data;
    }

    /**
     * @param prod_data
     *            the prod_data to set
     */
    public void setProd_data(byte[] prod_data) {
        this.prod_data = prod_data;
    }

    /**
     * @return the start_at
     */
    public Timestamp getStart_at() {
        return start_at;
    }

    /**
     * @param start_at
     *            the start_at to set
     */
    public void setStart_at(Timestamp start_at) {
        this.start_at = start_at;
    }

    /**
     * @return the last_updated
     */
    public Timestamp getLast_updated() {
        return last_updated;
    }

    /**
     * @param last_updated
     *            the last_updated to set
     */
    public void setLast_updated(Timestamp last_updated) {
        this.last_updated = last_updated;
    }

    /**
     * @return the pending_expire
     */
    public Timestamp getPending_expire() {
        return pending_expire;
    }

    /**
     * @param pending_expire
     *            the pending_expire to set
     */
    public void setPending_expire(Timestamp pending_expire) {
        this.pending_expire = pending_expire;
    }

    /**
     * Compare two ClimateProdGenerateSessionDatas by starting time.
     * 
     * @param obj
     *            ClimateProdGenerateSessionData
     * @return compareTo() 0 = equal; -1 - before; 1 - after.
     */
    @Override
    public int compareTo(ClimateProdGenerateSessionData obj) {
        return this.start_at.compareTo(obj.getStart_at());
    }

}
