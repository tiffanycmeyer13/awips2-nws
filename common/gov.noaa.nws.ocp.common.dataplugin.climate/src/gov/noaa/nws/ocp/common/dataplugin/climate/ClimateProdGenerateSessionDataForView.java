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
 * ClimateProdGenerateSessionDataForView, a limited view of data.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 10 MAY 2017  33532      pwang       Initial creation
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateProdGenerateSessionDataForView
        implements Comparable<ClimateProdGenerateSessionDataForView> {

    private static final long serialVersionUID = 3L;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String cpg_session_id;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int run_type;

    @Column(nullable = false)
    @DynamicSerializeElement
    private PeriodType prod_type;

    @Column(nullable = false)
    @DynamicSerializeElement
    private SessionState state;

    @Column(nullable = false)
    @DynamicSerializeElement
    private StateStatus status;

    @DynamicSerializeElement
    private String status_desc;

    @DynamicSerializeElement
    private ClimateProdData prod_data;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Timestamp start_at;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Timestamp last_updated;

    /**
     * Constructor
     */
    public ClimateProdGenerateSessionDataForView() {
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
        rval.put("prod_data", prod_data);

        rval.put("start_at", start_at);
        rval.put("last_updated", last_updated);

        return rval;

    }

    /*
     * Getter and setter
     */

    /**
     * Compare two ClimateProdGenerateSessionDatas by starting time.
     * 
     * @param obj
     *            ClimateProdGenerateSessionData
     * @return compareTo() 0 = equal; -1 - before; 1 - after.
     */
    @Override
    public int compareTo(ClimateProdGenerateSessionDataForView obj) {
        return this.start_at.compareTo(obj.getStart_at());
    }

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
    public PeriodType getProd_type() {
        return prod_type;
    }

    /**
     * @param prod_type
     *            the prod_type to set
     */
    public void setProd_type(PeriodType prod_type) {
        this.prod_type = prod_type;
    }

    /**
     * @return the state
     */
    public SessionState getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(SessionState state) {
        this.state = state;
    }

    /**
     * @return the status
     */
    public StateStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(StateStatus status) {
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
     * @return the prod_data
     */
    public ClimateProdData getProd_data() {
        return prod_data;
    }

    /**
     * @param prod_data
     *            the prod_data to set
     */
    public void setProd_data(ClimateProdData prod_data) {
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

}
