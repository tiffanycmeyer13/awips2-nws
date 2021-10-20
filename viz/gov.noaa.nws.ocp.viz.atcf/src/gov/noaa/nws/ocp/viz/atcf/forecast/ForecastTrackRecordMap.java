/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.map.WritableMap;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;

/**
 * An observable {@code Map<RecordKey, ForecastTrackRecord>} with extra support
 * for forecast editing UIs.
 *
 * <pre>
 * The class makes the following assumptions about its use:
 * * A single {@code RecordKey.aid} value is used for all keys.
 * * A single {@code RecordKey.dtg} value is used for all keys.
 *
 * SOFTWARE HISTORY
 * Date         Ticket#   Engineer       Description
 * ------------ --------- -------------- --------------------------
 * Apr 16, 2021 88721     dfriedman      initial creation
 *
 * </pre>
 *
 * @author dfriedman
 *
 */
public class ForecastTrackRecordMap
        extends WritableMap<RecordKey, ForecastTrackRecord> {

    /**
     * Same as
     * {@link org.eclipse.core.databinding.observable.map.WritableMap#put(java.lang.Object, java.lang.Object)},
     * but fires a change event even if called with an existing (key, value)
     * pair.
     */
    @Override
    public ForecastTrackRecord put(RecordKey key, ForecastTrackRecord value) {
        checkRealm();

        boolean containedKeyBefore = wrappedMap.containsKey(key);
        ForecastTrackRecord result = wrappedMap.put(key, value);
        boolean containedKeyAfter = wrappedMap.containsKey(key);

        MapDiff<RecordKey, ForecastTrackRecord> diff;
        if (containedKeyBefore) {
            if (containedKeyAfter) {
                diff = Diffs.createMapDiffSingleChange(key, result, value);
            } else {
                diff = Diffs.createMapDiffSingleRemove(key, result);
            }
        } else {
            diff = Diffs.createMapDiffSingleAdd(key, value);
        }
        fireMapChange(diff);
        return result;
    }

    @Override
    public void putAll(
            Map<? extends RecordKey, ? extends ForecastTrackRecord> map) {
        for (Map.Entry<? extends RecordKey, ? extends ForecastTrackRecord> entry : map
                .entrySet()) {
            super.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Fires a MapChangeEvent for the given record. This should be called after
     * making changes to a ForecastTrackRecord owned by this map.
     *
     * @param rec
     */
    public void fireRecordChanged(ForecastTrackRecord rec) {
        RecordKey key = createKey(rec.getFcstHour(),
                (int) rec.getRadWind());
        // Assumes safe to get(null)
        if (get(key) == rec) {
            fireMapChange(Diffs.createMapDiffSingleChange(key, rec, rec));
        }
    }

    /**
     * Find the forecast track record stored in the map for a TAU/Wind Radii.
     *
     * @param tau
     *            AtcfTaus (forecast hour)
     *
     * @param radii
     *            WindRadii
     *
     * @return ForecastTrackRecord
     */
    public ForecastTrackRecord getByTauRadii(AtcfTaus tau, WindRadii radii) {
        // Assumes safe to get(null)
        return get(createKey(tau.getValue(), radii.getValue()));
    }

    /**
     * Find the forecast track record stored in the map for a TAU/Wind Radii.
     *
     * @param tau
     *            AtcfTaus (forecast hour)
     *
     * @param radii
     *            WindRadii
     *
     * @return ForecastTrackRecord
     */
    public ForecastTrackRecord getByTauRadii(int tau, int radii) {
        // Assumes safe to get(null)
        return get(createKey(tau, radii));
    }

    /**
     * Creates a RecordKey to search for the given tau and radii. This works by
     * assuming all existing keys have the same aid and dtg values. If there are
     * no existing entries, returns null which is assumed to be safe to pass to
     * get().
     *
     * @param tau
     * @param radii
     * @return
     */
    private RecordKey createKey(int tau, int radii) {
        if (size() > 0) {
            RecordKey templateKey = entrySet().iterator().next().getKey();
            return new RecordKey(templateKey.getAid(), templateKey.getDtg(),
                    tau, radii);
        } else {
            return null;
        }
    }

}
