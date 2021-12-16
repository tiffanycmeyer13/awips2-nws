/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * SandboxDateTimeGroups
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 15, 2019  #63859    pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@Entity
@Table(schema = AtcfDB.SCHEMA, name = "sandbox_dtg", indexes = {
        @Index(name = "sandbox_dtg_index", columnList = "sandbox_id,dtg,conflictgroup") })
@DynamicSerialize
public class SandboxDateTimeGroups implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sandbox_id", insertable = false, updatable = false)
    private Sandbox sandbox;

    @Id
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar dtg;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String conflictGroup;

    /**
     * @return the sandbox
     */
    public Sandbox getSandbox() {
        return sandbox;
    }

    /**
     * @param sandbox the sandbox to set
     */
    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    /**
     * @return the dtg
     */
    public Calendar getDtg() {
        return dtg;
    }

    /**
     * @param dtg the dtg to set
     */
    public void setDtg(Calendar dtg) {
        this.dtg = dtg;
    }

    /**
     * @return the conflictGroup
     */
    public String getConflictGroup() {
        return conflictGroup;
    }

    /**
     * @param conflictGroup the conflictGroup to set
     */
    public void setConflictGroup(String conflictGroup) {
        this.conflictGroup = conflictGroup;
    }



}
