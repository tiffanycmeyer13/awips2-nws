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
 * FDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 28, 2018            pwang     Initial creation
 * Aug 23, 2018 #53502     dfriedman   Modify for Hibernate implementation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@Entity
@Table(schema = AtcfDB.SCHEMA, name = "fdeck", indexes = {
        @Index(name = "fdeck_reftime_index", columnList = "reftime"),
        @Index(name = "fdeck_stormid_index", columnList = "basin,year,cyclonenum"),
        @Index(name = "fdeck_fixformat_index", columnList = "fixFormat") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
        + "fdeckseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class FDeckRecord extends BaseFDeckRecord {

    private static final long serialVersionUID = 1L;
}
