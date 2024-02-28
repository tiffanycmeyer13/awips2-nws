/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Calendar;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Sandbox
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 12, 2018            pwang       Initial creation
 * Aug 23, 2018 53502      dfriedman   Modify for Hibernate implementation
 * May 13, 2019 63859      pwang       added submitTime
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@Entity
@Table(schema = AtcfDB.SCHEMA, name = "sandbox", indexes = {
        @Index(name = "sandbox_stormid_index", columnList = "region,year,cyclonenum"),
        @Index(name = "sandbox_lastupdated_index", columnList = "lastupdated") })
@SequenceGenerator(initialValue = 1, name = Sandbox.ID_GEN,
    sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX + "sandboxseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class Sandbox {
    public static final String ID_GEN = "idgen";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_GEN)
    @DynamicSerializeElement
    private int id;

    @Column(length = 2, nullable = false)
    @DynamicSerializeElement
    private String region;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int cycloneNum;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int year;

    @Column()
    @DynamicSerializeElement
    private String stormName;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String scopeCd;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String sandboxType;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar createdDT;

    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar lastUpdated;

    @Column(nullable = true)
    @DynamicSerializeElement
    private Calendar submitted = null;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String userId;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int validFlag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="sandbox")
    private Collection<SandboxADeckRecord> aDeck;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="sandbox")
    private Collection<SandboxBDeckRecord> bDeck;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="sandbox")
    private Collection<SandboxEDeckRecord> eDeck;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="sandbox")
    private Collection<SandboxFDeckRecord> fDeck;

    /**
     * Empty constructor
     */
    public Sandbox() {

    }

    public Sandbox(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region
     *            the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return the cycloneNum
     */
    public int getCycloneNum() {
        return cycloneNum;
    }

    /**
     * @param cycloneNum
     *            the cycloneNum to set
     */
    public void setCycloneNum(int cycloneNum) {
        this.cycloneNum = cycloneNum;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the stormName
     */
    public String getStormName() {
        return stormName;
    }

    /**
     * @param stormName
     *            the stormName to set
     */
    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

    /**
     * @return the scopeCd
     */
    public String getScopeCd() {
        return scopeCd;
    }

    /**
     * @param scopeCd
     *            the scopeCd to set
     */
    public void setScopeCd(String scopeCd) {
        this.scopeCd = scopeCd;
    }

    /**
     * @return the sandboxType
     */
    public String getSandboxType() {
        return sandboxType;
    }

    /**
     * @param sandboxType
     *            the sandboxType to set
     */
    public void setSandboxType(String sandboxType) {
        this.sandboxType = sandboxType;
    }

    /**
     * @return the createdDT
     */
    public Calendar getCreatedDT() {
        return createdDT;
    }

    /**
     * @param createdDT
     *            the createdDT to set
     */
    public void setCreatedDT(Calendar createdDT) {
        this.createdDT = createdDT;
    }

    /**
     * @return the lastUpdated
     */
    public Calendar getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated
     *            the lastUpdated to set
     */
    public void setLastUpdated(Calendar lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return the submitted
     */
    public Calendar getSubmitted() {
        return submitted;
    }

    /**
     * @param submitted
     *            the submitted to set
     */
    public void setSubmitted(Calendar submitted) {
        this.submitted = submitted;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the validFlag
     */
    public int getValidFlag() {
        return validFlag;
    }

    /**
     * @param validFlag
     *            the validFlag to set
     */
    public void setValidFlag(int validFlag) {
        this.validFlag = validFlag;
    }

}
