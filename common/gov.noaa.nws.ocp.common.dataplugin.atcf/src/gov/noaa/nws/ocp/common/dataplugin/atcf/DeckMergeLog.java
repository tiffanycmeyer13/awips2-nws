/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * DeckMergeLog Persistent for tracking latest merged decks for rollback ops
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 28, 2020 #78298      pwang      Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "deckmergelog", uniqueConstraints = {
        @UniqueConstraint(name = "uk_a2atcf_decklog", columnNames = { "basin",
                "cycloneNum", "year", "deckType", "mergeTime" }) })
@SequenceGenerator(initialValue = 1, name = DeckMergeLog.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
        + "mergelogseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class DeckMergeLog {

    public static final String ID_GEN = "idgen";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_GEN)
    @DynamicSerializeElement
    private int id;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String deckType;

    @Column(nullable = false)
    @DynamicSerializeElement
    private String basin;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int year;

    @Column(nullable = false)
    @DynamicSerializeElement
    private int cycloneNum;

    @DynamicSerializeElement
    private int endRecordId;

    @DynamicSerializeElement
    private int newEndRecordId;

    @DynamicSerializeElement
    private Calendar beginDTG;

    @DynamicSerializeElement
    private Calendar endDTG;

    @DynamicSerializeElement
    private int sandboxId = -1;

    @DynamicSerializeElement
    private Calendar mergeTime;

    @Transient
    private Set<Integer> conflictSboxes;

    public DeckMergeLog() {
        this.conflictSboxes = new HashSet<>();
    }

    public DeckMergeLog(int id) {
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
     * @return the deckType
     */
    public String getDeckType() {
        return deckType;
    }

    /**
     * @param deckType
     *            the deckType to set
     */
    public void setDeckType(String deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the basin
     */
    public String getBasin() {
        return basin;
    }

    /**
     * @param basin
     *            the basin to set
     */
    public void setBasin(String basin) {
        this.basin = basin;
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
     * @return the endRecordId
     */
    public int getEndRecordId() {
        return endRecordId;
    }

    /**
     * @param endRecordId
     *            the endRecordId to set
     */
    public void setEndRecordId(int endRecordId) {
        this.endRecordId = endRecordId;
    }

    /**
     * @return the newEndRecordId
     */
    public int getNewEndRecordId() {
        return newEndRecordId;
    }

    /**
     * @param newEndRecordId
     *            the newEndRecordId to set
     */
    public void setNewEndRecordId(int newEndRecordId) {
        this.newEndRecordId = newEndRecordId;
    }

    /**
     * @return the beginDTG
     */
    public Calendar getBeginDTG() {
        return beginDTG;
    }

    /**
     * @param beginDTG
     *            the beginDTG to set
     */
    public void setBeginDTG(Calendar beginDTG) {
        this.beginDTG = beginDTG;
    }

    /**
     * @return the endDTG
     */
    public Calendar getEndDTG() {
        return endDTG;
    }

    /**
     * @param endDTG
     *            the endDTG to set
     */
    public void setEndDTG(Calendar endDTG) {
        this.endDTG = endDTG;
    }

    /**
     * @return the sandboxId
     */
    public int getSandboxId() {
        return sandboxId;
    }

    /**
     * @param sandboxId
     *            the sandboxId to set
     */
    public void setSandboxId(int sandboxId) {
        this.sandboxId = sandboxId;
    }

    /**
     * @return the mergeTime
     */
    public Calendar getMergeTime() {
        return mergeTime;
    }

    /**
     * @param mergeTime
     *            the mergeTime to set
     */
    public void setMergeTime(Calendar mergeTime) {
        this.mergeTime = mergeTime;
    }

    /**
     * @return the conflictSboxes
     */
    public Set<Integer> getConflictSboxes() {
        return conflictSboxes;
    }

    /**
     * @param conflictSboxes
     *            the conflictSboxes to set
     */
    public void setConflictSboxes(Set<Integer> conflictSboxes) {
        this.conflictSboxes = conflictSboxes;
    }

    /**
     * @param sboxId
     */
    public void addOneConflictSbox(Integer sboxId) {
        this.conflictSboxes.add(sboxId);
    }

    /**
     * @return
     */
    public boolean hasConflictSbox() {
        return !this.conflictSboxes.isEmpty();
    }

}
