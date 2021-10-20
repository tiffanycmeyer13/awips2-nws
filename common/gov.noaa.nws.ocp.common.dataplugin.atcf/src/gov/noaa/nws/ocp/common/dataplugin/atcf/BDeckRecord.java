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

import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;

/**
 * Represents ATCF Automated Tropical Cyclone Forecast (deck b). This contains
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
 * Aug 23, 2018 53502      dfriedman   Modify for Hibernate implementation
 * Sep 12, 2019 68237      dfriedman   Remove stormName from unique fields
 * Apr 24, 2020 77847      jwu         Add copy constructor
 * Jul 06, 2020 79696      pwang       Add method to convert BDeckRecord to GenesisBDeckRecord
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "bdeck", uniqueConstraints = {
        @UniqueConstraint(name = "uk_a2atcf_bdeck", columnNames = { "basin",
                "cycloneNum", "year", "refTime", "fcstHour",
        "radWind" }) }, indexes = {
                @Index(name = "bdeck_reftime_index", columnList = "reftime"),
                @Index(name = "bdeck_stormid_index", columnList = "basin,year,cyclonenum") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
+ "bdeckseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class BDeckRecord extends BaseBDeckRecord {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public BDeckRecord() {
        super();
    }

    /**
     * A Copy constructor for convenience.
     *
     * @param rec
     *            BDeckRecord
     * @return BDeckRecord
     */
    public BDeckRecord(BDeckRecord rec) {
        super(rec);
    }

    /**
     * Convert to its Genesis Deck Record map cyclone number to genesis number
     */
    @Override
    public AbstractAtcfRecord toGenesisDeckRecord() throws AtcfException {
        return new GenesisBDeckRecord(this);
    }

}
