/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.raytheon.uf.common.datalisting.impl.DefaultDataListing;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMProductUtil;

/**
 * Implements data listing for ODIM data in the Product Browser.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMDataListing extends DefaultDataListing {
    private static final DecimalFormat ELEVATION_ANGLE_FORMAT = new DecimalFormat(
            "#.##");

    public ODIMDataListing(String pluginName, List<String> keySet) {
        super(pluginName, keySet);
    }

    @Override
    protected Map<String, String> getFormattedValues(String key,
            Collection<String> values) {
        if ("quantity".equals(key)) {
            // @formatter:off
            return values.stream()
                    .sorted()
                    .collect(Collectors.toMap(Function.identity(),
                            v -> {
                                String desc = ODIMProductUtil.getQuantityDescription(v);
                                return desc != null ? desc : v;
                            },
                            (a, b) -> a,
                            LinkedHashMap::new));
            // @formatter:on
        } else if ("primaryElevationAngle".equals(key)) {
            // @formatter:off
            return values.stream()
                    .map(ODIMDataListing::safeParseDouble)
                    .sorted()
                    .map(ELEVATION_ANGLE_FORMAT::format)
                    .collect(Collectors.toMap(Function.identity(),
                            Function.identity(),
                            (a, b) -> a,
                            LinkedHashMap::new));
            // @formatter:on
        } else {
            return super.getFormattedValues(key, values);
        }
    }

    private static Object safeParseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (RuntimeException e) { // NOSONAR
            return Objects.toString(s);
        }
    }

}
