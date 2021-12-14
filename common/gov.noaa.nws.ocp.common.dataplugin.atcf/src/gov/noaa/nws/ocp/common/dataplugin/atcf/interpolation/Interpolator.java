/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Based on Matt Onderlinde's awips_atcf_interp.f.
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
public class Interpolator {
    private Configuration config;

    public Interpolator(Configuration config) {
        this.config = config;
    }

    public InterpolatorOutput interpolate(InterpolationInput input) {
        Map<String, Map<Object, ModelData>> inputModelData = input
                .getModelData();
        Map<String, Map<Object, ModelData>> outputModelData = new HashMap<>();
        InterpolationArgs args = input.getArgs();
        int fcstHourOffset = args.getFcstHourOffset();
        float[] initialFields = args.getInitialFields();
        String[] models = args.getModels();
        String[] modelsToUse;

        if (models != null) {
            modelsToUse = models;
        } else {
            modelsToUse = inputModelData.keySet().stream()
                    .toArray(String[]::new);
        }

        for (String inputModelName : modelsToUse) {
            Map<Object, ModelData> inputModelBuckets = inputModelData
                    .get(inputModelName);
            ModelConfig modelConfig = config.getModelConfiguration()
                    .get(inputModelName);

            if (inputModelBuckets == null || modelConfig == null) {
                continue;
            }

            Map<Object, ModelData> outputModelBuckets = new HashMap<>();
            List<Object> sortedBucketKeys = inputModelBuckets.keySet().stream()
                    .sorted().collect(Collectors.toList());
            float[] firstBucketOffsets = null;

            for (Object bucketKey : sortedBucketKeys) {
                ModelData modelData = inputModelBuckets.get(bucketKey);

                // Interpolate to 3-hour periods
                int minFcstHour = Math.max(0,
                        Arrays.stream(modelData.fcstHour).min().getAsInt());
                int maxFcstHour = Arrays.stream(modelData.fcstHour).max()
                        .getAsInt();
                int min3Hour = (minFcstHour / 3) * 3;
                int n3hr = (maxFcstHour - minFcstHour) / 3 + 1;
                int nFields = modelData.fields.length;
                int[] inputFcstHour = modelData.fcstHour;
                int[] h3FcstHour = new int[n3hr];

                float[][] h3Fields = new float[nFields][];
                for (int f = 0; f < nFields; ++f) {
                    h3Fields[f] = new float[n3hr];
                }

                for (int n = 0; n < n3hr; ++n) {
                    int curH = min3Hour + n * 3;
                    h3FcstHour[n] = curH;
                    for (int t = 0; t < modelData.fcstHour.length - 1; ++t) {
                        if (inputFcstHour[t] <= curH
                                && inputFcstHour[t + 1] >= curH) {
                            float s = (float) (curH - inputFcstHour[t])
                                    / (inputFcstHour[t + 1] - inputFcstHour[t]);
                            for (int f = 0; f < nFields; ++f) {
                                float[] fa = modelData.fields[f];
                                h3Fields[f][n] = fa[t]
                                        + s * (fa[t + 1] - fa[t]);
                            }
                            break;
                        }
                    }
                }

                // Perform 3 point smoother on interpolated intensity
                float[][] smoothFields = new float[nFields][];
                for (int f = 0; f < nFields; ++f) {
                    smoothFields[f] = new float[n3hr];
                }

                for (int n = 0; n < config.getnSmooth(); ++n) {
                    for (int f = 0; f < nFields; ++f) {
                        smoothFields[f][0] = h3Fields[f][0];
                        smoothFields[f][n3hr - 1] = h3Fields[f][n3hr - 1];
                    }

                    for (int t = 1; t < n3hr - 1; t++) {
                        for (int f = 0; f < nFields; ++f) {
                            float[] h3 = h3Fields[f];
                            smoothFields[f][t] = h3[t - 1] != 0.0
                                    && h3[t + 1] != 0.0
                                            ? h3[t - 1] * 0.25f + h3[t] * 0.5f
                                                    + h3[t + 1] * 0.25f
                                            : h3[t];
                        }
                    }

                    float[][] tmp;
                    tmp = h3Fields;
                    h3Fields = smoothFields;
                    smoothFields = tmp;
                }

                // Shift forecast hours based on offset forecast hour
                for (int n = 0; n < n3hr; ++n) {
                    h3FcstHour[n] -= fcstHourOffset;
                }

                // "Compute initial lat, lon, and intensity offsets"
                float[] offsets = new float[nFields];
                boolean foundZeroHour = false;

                for (int n = 0; n < n3hr; ++n) {
                    if (h3FcstHour[n] == 0) {
                        for (int f = 0; f < nFields; ++f) {
                            offsets[f] = initialFields[f] - h3Fields[f][n];
                        }
                        foundZeroHour = true;
                        break;
                    }
                }

                if (foundZeroHour) {
                    if (firstBucketOffsets == null) {
                        firstBucketOffsets = offsets;
                    }
                } else {
                    if (firstBucketOffsets != null) {
                        System.arraycopy(firstBucketOffsets, 0, offsets, 0,
                                offsets.length);
                    } else {
                        throw new IllegalArgumentException(
                                "First bucket does not contain a zero hour (after applying forecast hour offset )");
                    }
                }

                // "Apply relaxation to deterministic forecast over time based on input time range"
                int minRelaxFcstHour = modelConfig.getMinRelaxFcstHour();
                int maxRelaxFcstHour = modelConfig.getMaxRelaxFcstHour();
                for (int n = 0; n < n3hr; ++n) {
                    if (h3FcstHour[n] <= minRelaxFcstHour) {
                        for (int f = 0; f < nFields; ++f) {
                            if (!Float.isNaN(offsets[f])) {
                                h3Fields[f][n] += offsets[f];
                            }
                        }
                    } else if (h3FcstHour[n] > minRelaxFcstHour
                            && h3FcstHour[n] < maxRelaxFcstHour) {
                        float s = 1.0f - (h3FcstHour[n] - minRelaxFcstHour)
                                / (maxRelaxFcstHour - minRelaxFcstHour);
                        for (int f = 0; f < nFields; ++f) {
                            if (!Float.isNaN(offsets[f])) {
                                h3Fields[f][n] += offsets[f] * s;
                            }
                        }
                    }
                    // else, Do not adjust by offsets at all.
                }

                // "Set t=0 value to the HSU-prescribed value"
                for (int n = 0; n < n3hr; ++n) {
                    if (h3FcstHour[n] == 0) {
                        for (int f = 0; f < nFields; ++f) {
                            if (!Float.isNaN(initialFields[f])) {
                                h3Fields[f][n] = initialFields[f];
                            }
                        }
                        break;
                    }
                }

                int[] pick = new int[n3hr];
                int[] outputForecastHour = new int[n3hr];
                final int outFreq = config.getFcstHourOutputFrequency();
                int o = 0;

                for (int n = 0; n < n3hr; ++n) {
                    if (h3FcstHour[n] >= minFcstHour
                            && h3FcstHour[n] % outFreq == 0) {
                        pick[o] = n;
                        outputForecastHour[o] = h3FcstHour[n];
                        o += 1;
                    }
                }

                float[][] outFields = new float[nFields][];

                for (int f = 0; f < nFields; ++f) {
                    float[] fi = h3Fields[f];
                    float[] fo = new float[o];
                    for (int n = 0; n < o; ++n) {
                        fo[n] = fi[pick[n]];
                    }
                    outFields[f] = fo;
                }

                ModelData outModelData = new ModelData(
                        Arrays.copyOfRange(outputForecastHour, 0, o),
                        outFields);
                outputModelBuckets.put(bucketKey, outModelData);
            }
            outputModelData.put(modelConfig.getOutputName(),
                    outputModelBuckets);
        }

        long outDateMillis = args.getDtg().getTime()
                + fcstHourOffset * TimeUtil.MILLIS_PER_HOUR;
        return new InterpolatorOutput(TimeUtil.newGmtCalendar(outDateMillis),
                outputModelData);
    }
}
