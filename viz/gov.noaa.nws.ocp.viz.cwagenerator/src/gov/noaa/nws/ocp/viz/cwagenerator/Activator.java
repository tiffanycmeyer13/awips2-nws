/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.cwagenerator;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * 
 * activator for CWA generator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 1, 2021  75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class Activator extends AbstractUIPlugin {

    /* The plug-in ID */
    public static final String PLUGIN_ID = "gov.noaa.nws.ocp.viz.cwa";

    /* The shared instance */
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * get this plugin
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

}