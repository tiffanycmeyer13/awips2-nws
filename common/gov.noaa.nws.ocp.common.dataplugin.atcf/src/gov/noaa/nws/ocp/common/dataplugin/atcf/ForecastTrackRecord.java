/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Represents ATCF Automated Tropical Cyclone Forecast track. This contains
 * getters and setters for the main parent table atcf.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 07, 2019 69593      pwang       Initial creation
 * Apr 16, 2020 71986      mporricelli Added sorting of forecast
 *                                     track records for creating
 *                                     .fst output file
 * Apr 24, 2020 77847      jwu         Add constructors
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@Table(schema = AtcfDB.SCHEMA, name = "fst", uniqueConstraints = {
        @UniqueConstraint(name = "uk_a2atcf_fst", columnNames = { "basin",
                "cycloneNum", "year", "refTime", "fcstHour",
                "radWind" }) }, indexes = {
                        @Index(name = "fst_reftime_index", columnList = "reftime"),
                        @Index(name = "fst_stormid_index", columnList = "basin,year,cyclonenum") })
@SequenceGenerator(initialValue = 1, name = AbstractAtcfRecord.ID_GEN, sequenceName = AtcfDB.SEQUENCE_NAME_PREFIX
        + "fstseq", schema = AtcfDB.SCHEMA)
@DynamicSerialize
public class ForecastTrackRecord extends BaseBDeckRecord {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public ForecastTrackRecord() {
        super();
    }

    /**
     * A Copy constructor for convenience.
     *
     * @param rec
     *            ForecastTrackRecord
     * @return ForecastTrackRecord
     */
    public ForecastTrackRecord(ForecastTrackRecord rec) {
        super(rec);
    }

    /**
     * Sort forecast records by fcst time and rad wind fields to make storm list
     * output match format of Legacy ATCF .fst file
     *
     * @param fstRecs
     */
    public static void sortFstDeck(List<ForecastTrackRecord> fstRecs) {
        if (!fstRecs.isEmpty()) {
            Comparator<ForecastTrackRecord> fcstTimeCmp = Comparator
                    .comparing(ForecastTrackRecord::getFcstHour);
            Comparator<ForecastTrackRecord> radWndCmp = Comparator
                    .comparing(ForecastTrackRecord::getRadWind);

            Collections.sort(fstRecs, fcstTimeCmp.thenComparing(radWndCmp));
        }
    }
}
