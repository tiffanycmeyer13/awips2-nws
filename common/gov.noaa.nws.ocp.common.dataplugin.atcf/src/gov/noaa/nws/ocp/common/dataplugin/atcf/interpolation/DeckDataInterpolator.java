/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.JAXBManager;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * Performs interpolation on a set of ATCF deck records.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul  8, 2020 78599      dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public class DeckDataInterpolator {

    private static final String DEFAULT_CONFIGURATION_PATH = "atcf"
            + IPathManager.SEPARATOR + "interpolator" + IPathManager.SEPARATOR
            + "interpolator.xml";

    private DeckDataInterpolator() {

    }

    /**
     * Interpolate the given records using the configuration from localization.
     *
     * @param storm
     * @param args
     * @param records
     * @param initial
     * @return
     * @throws Exception
     */
    public static List<BaseADeckRecord> interpolateRecords(Storm storm,
            InterpolationArgs args, List<BaseADeckRecord> records,
            BaseADeckRecord initial) throws Exception {
        return interpolateRecords(storm, args, records, initial, null);
    }

    /**
     * Interpolate the given records using the effective Configuration from
     * localization.
     *
     * @param storm
     *            If not null, selects records for the given storm.
     * @param args
     * @param records
     *            input records; These are not modified.
     * @param initial
     *            If not null, specifies current storm values that are used to
     *            adjust model values. Otherwise, use {@code args.initialFields}
     *            which may also be null.
     * @param configuration
     *            If null, use the ef
     *
     * @return the interpolated records
     * @throws Exception
     */
    public static List<BaseADeckRecord> interpolateRecords(Storm storm,
            InterpolationArgs args, List<BaseADeckRecord> records,
            BaseADeckRecord initial, Configuration configuration)
            throws Exception {

        // Get Configuration from localization if not provided.
        if (configuration == null) {
            LocalizationFile localizationFile = PathManagerFactory
                    .getPathManager()
                    .getStaticLocalizationFile(DEFAULT_CONFIGURATION_PATH);
            if (localizationFile != null) {
                try (InputStream ins = localizationFile.openInputStream()) {
                    configuration = new JAXBManager(Configuration.class)
                            .unmarshalFromInputStream(Configuration.class, ins);
                }
            } else {
                configuration = defaultConfiguration();
            }
        }

        Map<String, ArrayList<BaseADeckRecord>> recordsToUse = new HashMap<>();
        Date dtgCal = args.getDtg();
        Set<String> models = args.getModels() != null
                ? new HashSet<>(Arrays.asList(args.getModels())) : null;

        /*
         * Select records for the given storm (if specified) and for the given
         * subset of models (if specified.) In any case, only models that are
         * actually specified by the Configuration can actually be used.
         */
        for (BaseADeckRecord rec : records) {
            if (rec.getRefTime().equals(dtgCal)
                    && (storm == null || (rec.getYear() == storm.getYear()
                            && rec.getBasin().equals(storm.getRegion())
                            && rec.getCycloneNum() == storm.getCycloneNum()))) {
                String k = rec.getTechnique();
                if (configuration.getModelConfiguration().containsKey(k)
                        && (models == null || models.contains(k))) {
                    ArrayList<BaseADeckRecord> modelRecs = recordsToUse.get(k);
                    if (modelRecs == null) {
                        modelRecs = new ArrayList<>();
                        recordsToUse.put(k, modelRecs);
                    }
                    modelRecs.add(rec);
                }
            }
        }

        /*
         * Separate records into wind radii buckets and create ModelData from
         * the deck data.
         */
        Map<String, Map<Object, ModelData>> modelData = new HashMap<>();
        for (Entry<String, ArrayList<BaseADeckRecord>> entry : recordsToUse
                .entrySet()) {
            Map<Object, ModelData> modelBuckets = new HashMap<>();
            List<BaseADeckRecord> recs = entry.getValue();
            addModelDataBucket(recs, 34, modelBuckets);
            addModelDataBucket(recs, 50, modelBuckets);
            addModelDataBucket(recs, 64, modelBuckets);
            modelData.put(entry.getKey(), modelBuckets);
        }

        InterpolationArgs argsToUse = args.clone();

        // Create initial storm values from the given record if specified.
        if (initial != null) {
            float[] initialFields = new float[7];
            initialFields[0] = initial.getClat();
            initialFields[1] = initial.getClon();
            initialFields[2] = initial.getWindMax();
            initialFields[3] = initial.getQuad1WindRad();
            initialFields[4] = initial.getQuad2WindRad();
            initialFields[5] = initial.getQuad3WindRad();
            initialFields[6] = initial.getQuad4WindRad();
            argsToUse.setInitialFields(initialFields);
        } else if (argsToUse.getInitialFields() == null) {
            float[] initialFields = new float[7];
            Arrays.fill(initialFields, Float.NaN);
            argsToUse.setInitialFields(initialFields);
        }

        // Prepare and run the interpolator.
        InterpolationInput input = new InterpolationInput(argsToUse, modelData);
        Interpolator interpolator = new Interpolator(configuration);
        InterpolatorOutput output = interpolator.interpolate(input);

        ArrayList<BaseADeckRecord> outputRecs = new ArrayList<>();
        Date outDtg = output.getDtg().getTime();

        // Determine identifying storm values for output deck records.
        if (storm == null && !records.isEmpty()) {
            // Use first record if any.
            BaseADeckRecord rec = records.get(0);
            storm = new Storm();
            storm.setYear(rec.getYear());
            storm.setRegion(rec.getBasin());
            storm.setCycloneNum(rec.getCycloneNum());
        }

        // Covert output ModelData to deck records.
        for (Entry<String, Map<Object, ModelData>> entry : output.getModelData()
                .entrySet()) {
            String modelName = entry.getKey();

            for (Entry<Object, ModelData> bucketEntry : entry.getValue()
                    .entrySet()) {
                float radWind = (float) bucketEntry.getKey();
                ModelData md = bucketEntry.getValue();
                float[][] fields = md.fields;
                int n = md.fcstHour.length;

                for (int r = 0; r < n; ++r) {
                    ADeckRecord rec = new ADeckRecord();

                    rec.setRefTime(outDtg);
                    if (storm != null) {
                        rec.setYear(storm.getYear());
                        rec.setBasin(storm.getRegion());
                        rec.setCycloneNum(storm.getCycloneNum());
                    }
                    rec.setTechniqueNum(3);
                    rec.setTechnique(modelName);
                    rec.setFcstHour(md.fcstHour[r]);
                    rec.setClat(fields[0][r]);
                    rec.setClon(fields[1][r]);
                    rec.setWindMax(toDeckMissing(fields[2][r]));
                    rec.setRadWind(radWind);
                    // based on archived ata
                    rec.setMslp(0);
                    rec.setIntensity(""); // "
                    rec.setRadWindQuad("NEQ");
                    rec.setQuad1WindRad(toDeckMissing(fields[3][r]));
                    rec.setQuad2WindRad(toDeckMissing(fields[4][r]));
                    rec.setQuad3WindRad(toDeckMissing(fields[5][r]));
                    rec.setQuad4WindRad(toDeckMissing(fields[6][r]));
                    outputRecs.add(rec);
                }
            }
        }

        BaseADeckRecord.sortADeck(outputRecs);
        return outputRecs;
    }

    /**
     * Create {@code ModelData} from records that match the given
     * {@code radWind} value and add it to {@code modelBuckets}.
     *
     * @param recs
     * @param radWind
     * @param modelBuckets
     */
    private static void addModelDataBucket(List<BaseADeckRecord> recs,
            float radWind, Map<Object, ModelData> modelBuckets) {
        int nRecsInBucket = 0;

        for (BaseADeckRecord rec : recs) {
            if (rec.getRadWind() == radWind) {
                ++nRecsInBucket;
            }
        }

        if (nRecsInBucket < 2) {
            return;
        }

        int[] fcstHour = new int[nRecsInBucket];
        float[][] fields = new float[7][];
        for (int f = 0; f < 7; ++f) {
            fields[f] = new float[nRecsInBucket];
        }

        int r = 0;
        for (BaseADeckRecord rec : recs) {
            if (rec.getRadWind() == radWind) {
                fcstHour[r] = rec.getFcstHour();
                fields[0][r] = rec.getClat();
                fields[1][r] = rec.getClon();
                fields[2][r] = checkMissing(rec.getWindMax());
                fields[3][r] = checkMissing(rec.getQuad1WindRad());
                fields[4][r] = checkMissing(rec.getQuad2WindRad());
                fields[5][r] = checkMissing(rec.getQuad3WindRad());
                fields[6][r] = checkMissing(rec.getQuad4WindRad());
                ++r;
            }
        }

        modelBuckets.put(radWind, new ModelData(fcstHour, fields));
    }

    private static Configuration defaultConfiguration() {
        return new Configuration();
    }

    private static float checkMissing(float v) {
        return v != AbstractAtcfRecord.RMISSD ? v : Float.NaN;
    }

    private static float toDeckMissing(float v) {
        return !Float.isNaN(v) ? v : AbstractAtcfRecord.RMISSD;
    }

}
