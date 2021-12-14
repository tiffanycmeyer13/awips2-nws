/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Sandbox version of {@link ADeckRecord}.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2018 #53502     dfriedman   Initial creation
 * Mar 29, 2019 #61590     dfriedman   Implement ISandboxRecord
 * Jun 11, 2019 #68118     wpaintsil   Add @DynamicSerialize
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "sandbox_adeck")
@IdClass(SandboxEntryPK.class)
@DynamicSerialize
public class SandboxADeckRecord extends BaseADeckRecord
        implements ISandboxRecord {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sandbox_id", insertable = false, updatable = false)
    private Sandbox sandbox;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_GEN)
    @SequenceGenerator(initialValue = 1, name = ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
            + "adeckseq", schema = AtcfDB.SCHEMA)
    private int id;

    @Column(name = "change_cd", nullable = false)
    private int changeCD;

    @Override
    public Sandbox getSandbox() {
        return sandbox;
    }

    @Override
    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        this.id = id;
    }

    @Override
    public int getChangeCD() {
        return changeCD;
    }

    @Override
    public void setChangeCD(int changeCD) {
        this.changeCD = changeCD;
    }

}
