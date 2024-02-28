/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc.bg;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.maps.rsc.DbMapResource;
import com.raytheon.uf.viz.core.maps.rsc.DbMapResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionNames;
import gov.noaa.nws.ocp.common.atcf.configuration.IColorConfigurationChangedListener;

/**
 * Extension of {@code DbMapResource} that allows shading with a single color.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 22, 2021 87890       dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 */
public class AtcfShapeBackgroundResource extends DbMapResource
        implements IColorConfigurationChangedListener {

    private static class SingleValueMapEntry<K, V> implements Map.Entry<K, V> {

        private V value;

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            // ignored
            return null;
        }

        public SingleValueMapEntry(V value) {
            this.value = value;
        }
    }

    private static class SingleValueMap<K, V> extends AbstractMap<K, V> {

        private SingleValueMapEntry<K, V> entry;

        @Override
        public Set<java.util.Map.Entry<K, V>> entrySet() {
            return Collections.singleton(entry);
        }

        @Override
        public V get(Object key) {
            return entry.value;
        }

        @Override
        public V put(K key, V value) {
            // ignored
            return null;
        }

        private SingleValueMap(V value) {
            this.entry = new SingleValueMapEntry<>(value);
        }
    }

    private RGB cachedColor;

    public AtcfShapeBackgroundResource(DbMapResourceData data,
            LoadProperties loadProperties) {
        super(data, loadProperties);
        colorMap = new SingleValueMap<>(getSingleColor());
        AtcfConfigurationManager.getInstance()
                .addColorConfigurationChangeListener(this);
    }

    @Override
    protected void disposeInternal() {
        AtcfConfigurationManager.getInstance()
                .removeColorConfigurationChangeListener(this);
        super.disposeInternal();
    }

    protected RGB getSingleColor() {
        if (cachedColor == null) {
            cachedColor = BackgroundManager
                    .getAtcfColor(ColorSelectionNames.LAND);
        }
        return cachedColor;
    }

    @Override
    public void colorsChanged() {
        cachedColor = null;
        lastShadingField = null;
        colorMap = new SingleValueMap<>(getSingleColor());
        issueRefresh();
    }

}
