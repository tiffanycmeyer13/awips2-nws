/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.alertviz.AlertService;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import gov.noaa.nws.ocp.viz.climate.perspective.notify.ClimateNotificationJob;
import gov.noaa.nws.ocp.viz.climate.perspective.notify.ClimateNotificationJob.ClimateNotificationJobListener;

/**
 * Controls the plug-in life cycle. This doesn't do anything presently. But it
 * may be necessary for future updates, i.e. custom behavior when the plugin is
 * started or stopped.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2016  20744     wpaintsil   Initial creation
 * Mar 30, 2017  27199     pwang       add ClimateNotificationJob
 * Aug 03, 2017  33104     amoore      Change logging to debug.
 * </pre>
 * 
 * @author wpaintsil
 */

public class Activator extends AbstractUIPlugin {

    public static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(Activator.class);

    // The plug-in ID
    public static final String PLUGIN_ID = "gov.noaa.nws.ocp.viz.climate.perspective";

    // The shared instance
    private static Activator plugin;

    // The ServiceRegistration
    private ServiceRegistration<AlertService> service;

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        statusHandler
                .debug("====== Climate Perspective Activator started. =======");

        /*
         * Prepare the job but do not start it up or schedule it yet.
         */
        ClimateNotificationJob.getInstance().addClimateNotificationJobListener(
                new ClimateNotificationJobListener() {

                    @Override
                    public void receiverConnected() {
                        if (service == null) {
                            service = getBundle().getBundleContext()
                                    .registerService(AlertService.class,
                                            ClimateNotificationJob
                                                    .getInstance(),
                                            null);
                        }
                    }

                    @Override
                    public void receiverDisconnected() {
                        if (service != null) {
                            service.unregister();
                            service = null;
                        }
                    }

                });

        /*
         * Necessary to prevent multiple un-registration.
         */
        context.addServiceListener(new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent event) {
                if (service != null
                        && event.getServiceReference()
                                .equals(service.getReference())
                        && event.getType() == ServiceEvent.UNREGISTERING) {
                    service = null;
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        stopStatusMessageAppender();
        ClimateNotificationJob.getInstance().cancel();
        statusHandler.debug("===== Climate Activator stopped =====");
    }

    /**
     * This isn't pretty but if the AlertvizJob (aka alertviz receiver) is
     * running internally, to prevent errors we have to make sure to stop the
     * AlertvizAppender if it exists before we stop the receiving job.
     */
    private void stopStatusMessageAppender() {
        if (ClimateNotificationJob.getInstance().getState() == Job.RUNNING) {
            Logger logger = (Logger) LoggerFactory.getLogger("CaveLogger");
            Appender<ILoggingEvent> app = logger
                    .getAppender("AlertvizAppender");
            if (app != null) {
                app.stop();
                logger.detachAppender(app);
            }
        }
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

}