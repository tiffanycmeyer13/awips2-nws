/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf;

/**
 * Class to identify a B-deck record within a given storm.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer    Description
 * ------------- ---------- ----------- --------------------------
 * Jul 15, 2020  79573      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class BDeckRecordKey extends RecordKey {

    // Technique for B-Deck, always "BEST".
    private static final String BDECK_TECHNIQUE = "BEST";

    /**
     * Default constructor
     */
    public BDeckRecordKey() {
        super();
    }

    /**
     * Constructor
     *
     * Note: BDeck record always has technique as "BEST" and forecast hour as 0.
     *
     * @param dtg
     * @param windRad
     */
    public BDeckRecordKey(String dtg, int windRad) {
        super(BDECK_TECHNIQUE, dtg, 0, windRad);
    }
}
