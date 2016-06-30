package gov.noaa.nws.ocp.uf.viz.gisdatastore.directory;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.localization.HierarchicalPreferenceStore;


/**
 * Activator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2016  17912       pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0	
 */

public class Activator extends AbstractUIPlugin {
    public static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(Activator.class);

    // The plug-in ID
    public static final String PLUGIN_ID = "gov.noaa.nws.ocp.uf.viz.gisdatastore.directory";

    // The shared instance
    private static Activator plugin;

    // pref store
    private HierarchicalPreferenceStore prefs = new HierarchicalPreferenceStore(
            this);

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
     */
    @Override
    public IPersistentPreferenceStore getPreferenceStore() {
        return prefs;
    }

}
