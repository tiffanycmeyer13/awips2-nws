/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * GenesisEDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 3, 2020 # 77134     pwang       Initial creation
 * Jul 6, 2020 # 79696     pwang       add constructor to convert EDeckRecord to GenesisEDeckRecord
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "genesisedeck", indexes = {
        @Index(name = "gedeck_reftime_index", columnList = "reftime"),
        @Index(name = "gedeck_stormid_index", columnList = "basin,year,genesisnum"),
        @Index(name = "gedeck_probformat_index", columnList = "probFormat") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
        + "edeckseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class GenesisEDeckRecord extends GenesisBaseEDeckRecord {

    private static final long serialVersionUID = 1L;

    public GenesisEDeckRecord() {
        super();
    }

    public GenesisEDeckRecord(EDeckRecord rec) {
        super(rec);
    }

}
