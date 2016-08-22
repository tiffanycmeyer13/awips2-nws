package gov.noaa.nws.obs.viz.geodata.rsc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.style.StyleManager;
import com.raytheon.uf.common.style.StyleRule;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

import gov.noaa.nws.obs.common.dataplugin.geodata.FloatAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.GeoDataRecord;
import gov.noaa.nws.obs.common.dataplugin.geodata.IntegerAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.StringAttribute;
import gov.noaa.nws.obs.viz.geodata.style.GeoDataRecordCriteria;

/**
 * GeoDataResourceData
 *
 * Class which is responsible for providing the metadata map by which
 * GeoDataResource(s) may be requested. The metadata map is what is used to
 * populate menu and bundle items as they pertain to the DBGeo plugin.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/25/2016     19064      jburks    Initial checkin (DCS 19064)
 * 08/20/2016     19064    mcomerford  Implementing StyleRule handling.
 * </pre>
 *
 * @author jason.burks
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
public class GeoDataResourceData extends AbstractRequestableResourceData {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GeoDataResourceData.class);

    @Override
    protected AbstractVizResource<?, ?> constructResource(
            LoadProperties loadProperties, PluginDataObject[] objects)
                    throws VizException {
        GeoDataResource rsc = new GeoDataResource(this, loadProperties);

        /*
         * List containing the names of all Attribute(s). This list will be used
         * to determine which styleRule to match against.
         */
        List<String> attNames = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        List<String> products = new ArrayList<>();

        for (PluginDataObject obj : objects) {
            if (obj instanceof GeoDataRecord) {

                GeoDataRecord rec = (GeoDataRecord) obj;

                /*
                 * Generate the lists of Attribute names, sources, and products
                 * to use to generate this GeoDataResourceData's MatchCriteria.
                 */
                for (IntegerAttribute intAtt : rec.getIntegerAtt()) {
                    if (!attNames.contains(intAtt.getName())) {
                        attNames.add(intAtt.getName());
                    }
                }
                for (FloatAttribute floatAtt : rec.getFloatAtt()) {
                    if (!attNames.contains(floatAtt.getName())) {
                        attNames.add(floatAtt.getName());
                    }
                }
                for (StringAttribute stringAtt : rec.getStringAtt()) {
                    if (!attNames.contains(stringAtt.getName())) {
                        attNames.add(stringAtt.getName());
                    }
                }

                if (!sources.contains(rec.getSource())) {
                    sources.add(rec.getSource());
                }
                if (!products.contains(rec.getProduct())) {
                    products.add(rec.getProduct());
                }

                rsc.addRecord(rec);
            } else {
                statusHandler.handle(Priority.PROBLEM,
                        "Not of type" + GeoDataRecord.class);
            }

        }

        StyleRule matchingStyleRule;
        GeoDataRecordCriteria matcher = new GeoDataRecordCriteria(sources,
                products, attNames);
        /*
         * Attempt to get the matching StyleRules instance, otherwise, just
         * default the Resource Style Rule to null.
         */
        try {
            matchingStyleRule = StyleManager.getInstance()
                    .getStyleRule(StyleManager.StyleType.GEOMETRY, matcher);
            rsc.setDefStyleRule(matchingStyleRule);

        } catch (Exception e) {
            rsc.setDefStyleRule(null);
        }

        return rsc;
    }
}
