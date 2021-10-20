/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;


import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * GenesisBDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 3, 2020 # 77134     pwang       Initial creation
 * Jul 6, 2020 # 79696     pwang       add constructor to convert BDeckRecord to GenesisBDeckRecord
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "genesisbdeck", uniqueConstraints = {
        @UniqueConstraint(name = "uk_a2atcf_gbdeck", columnNames = { "basin",
                "genesisNum", "year", "refTime", "fcstHour",
                "radWind" }) }, indexes = {
                        @Index(name = "gbdeck_reftime_index", columnList = "reftime"),
                        @Index(name = "gbdeck_stormid_index", columnList = "basin,year,genesisnum") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
        + "bdeckseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class GenesisBDeckRecord extends GenesisBaseBDeckRecord {

    private static final long serialVersionUID = 1L;

    public GenesisBDeckRecord() {

    }

    public GenesisBDeckRecord(BDeckRecord rec) {
        super(rec);
    }
}
