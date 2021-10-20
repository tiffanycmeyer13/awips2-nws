/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XML adapter for {@code Configuration.modelConfiguration}
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
public class ModelConfigMapAdapter
        extends XmlAdapter<ModelConfigList, Map<String, ModelConfig>> {

    @Override
    public Map<String, ModelConfig> unmarshal(ModelConfigList collection) throws Exception {
        Map<String, ModelConfig> map = new HashMap<>();
        for (ModelConfig config : collection.getList()) {
            map.put(config.getInputName(), config);
        }
        return map;
    }

    @Override
    public ModelConfigList marshal(Map<String, ModelConfig> map) throws Exception {
        return new ModelConfigList(map.values());
    }

}
