/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;

/**
 * Represents ATCF Automated Tropical Cyclone Forecast (deck e). This contains
 * getters and setters for the main parent table atcf. This code has been
 * developed by the SIB for use in the AWIPS2 system.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Aug 23, 2018 #53502     dfriedman   Modify for Hibernate implementation
 * Jul 6, 2020  #79696     pwang       Add method to convert EDeckRecord to GenesisEDeckRecord
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@Entity
@Table(schema = AtcfDB.SCHEMA, name = "edeck", indexes = {
        @Index(name = "edeck_reftime_index", columnList = "reftime"),
        @Index(name = "edeck_stormid_index", columnList = "basin,year,cyclonenum"),
        @Index(name = "edeck_probformat_index", columnList = "probFormat") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
        + "edeckseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class EDeckRecord extends BaseEDeckRecord {

    private static final long serialVersionUID = 1L;

    /**
     * Convert to its Genesis Deck Record map cyclone number to genesis number
     */
    @Override
    public AbstractAtcfRecord toGenesisDeckRecord() throws AtcfException {
        return new GenesisEDeckRecord(this);
    }
}
