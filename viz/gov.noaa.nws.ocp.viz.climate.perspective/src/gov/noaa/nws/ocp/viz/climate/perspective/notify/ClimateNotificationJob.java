/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective.notify;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.alertviz.AlertService;

/**
 * ClimateNotificationJob
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 29, 2017            pwang       Initial creation
 * May 22, 2017            jwu         Some cleanup.
 * Apr 19, 2018  7013      tgurney     Add "canExit" field and getter/setter
 *
 * </pre>
 *
 * @author pwang
 */
public class ClimateNotificationJob extends Job implements AlertService {

    public static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ClimateNotificationJob.class);

    private static final int MAX_CONNECTION_ATTEMPTS = 10;

    public static final String TCP_CONNECTION = "tcp://0.0.0.0:";

    public static final String STOMP_CONNECTION = "stomp://0.0.0.0:";

    public static final String LOCAL_SERVICE_NAME = "notification";

    public static interface ClimateNotificationJobListener {

        public void receiverConnected();

        public void receiverDisconnected();
    }

    private boolean embedded = true;

    private boolean canExit = false;

    private int exitStatus;

    private final Object waiter = new Object();

    private Set<IClimateMessageCallback> climateCallbacks = new CopyOnWriteArraySet<>();

    private Set<ClimateNotificationJobListener> receiverListeners = new CopyOnWriteArraySet<>();

    private int port;

    private ConnectionFactory factory;

    private BrokerService broker;

    /**
     * actually stores the log messages
     */
    private Connection connection;

    private int connectionAttempts = 0;

    private boolean connectionStarted = false;

    private final ExceptionListener exceptionListener = new ExceptionListener() {
        @Override
        public void onException(JMSException e) {
            connectionException(e);
        }
    };

    private Session session;

    private static ClimateNotificationJob instance = null;

    /**
     * Constructor.
     */
    private ClimateNotificationJob() {
        super("ClimateNotify Receiver");
        setSystem(true);
        setPriority(Job.INTERACTIVE);
    }

    /**
     * Starts the broker and JMS services to act as the receiver for AlertViz,
     * then schedules the job to process any messages.
     *
     * @param port
     */
    public void start(int port) {
        this.broker = new BrokerService();
        this.broker.setBrokerName(LOCAL_SERVICE_NAME);
        this.broker.setPersistent(false);
        this.broker.setUseJmx(false);
        this.port = port;
        ClimateNotificationObserver.registerClimateNotificationObserver();

        String localIP = "localhost";
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // ignore and use localhost
        }

        String jmsURI = "tcp://" + localIP + ":" + port;
        this.factory = new ActiveMQConnectionFactory(jmsURI);
        this.schedule();
    }

    /**
     * @return The main AlertvizJob started by the plugin's activator
     */
    public static synchronized ClimateNotificationJob getInstance() {
        if (instance == null) {
            instance = new ClimateNotificationJob();
        }
        return instance;
    }

    /**
     * Adds a callback for getting notified when an alert is received from the
     * receiver.
     *
     * @param callback
     */
    public void addClimateCallback(IClimateMessageCallback callback) {
        this.climateCallbacks.add(callback);
    }

    /**
     * Removes a callback for getting notified when an alert is received from
     * the receiver.
     *
     * @param callback
     */
    public void removeClimateCallback(IClimateMessageCallback callback) {
        this.climateCallbacks.remove(callback);
    }

    /**
     * Adds a general job status listener for getting notified when state
     * changes for receiver
     *
     * @param listener
     */
    public void addClimateNotificationJobListener(
            ClimateNotificationJobListener listener) {
        this.receiverListeners.add(listener);
        if (broker != null && broker.isStarted()) {
            listener.receiverConnected();
        }
    }

    /**
     * Removes a general job status listener for getting notified when state
     * changes for receiver
     *
     * @param listener
     */
    public void removeClimateNotificationJobListener(
            ClimateNotificationJobListener listener) {
        this.receiverListeners.remove(listener);
    }

    private void connectionException(JMSException e) {
        statusHandler.error(
                "Exception occurred on ClimateView connection, will attempt reconnect",
                e);
        synchronized (this) {
            disconnect();
        }

        // Wake up thread so it reconnects
        synchronized (waiter) {
            waiter.notify();
        }
    }

    /**
     * Handle received message - simply pass onto IClimateMessageCallbacks.
     *
     * @param sm
     */
    public void receive(StatusMessage sm) {
        if (sm != null) {
            for (IClimateMessageCallback callback : climateCallbacks) {
                callback.messageArrived(sm);
            }

            // Notify we have data
            synchronized (waiter) {
                waiter.notify();
            }
        }
    }

    private void connect() {
        try {
            // attempt to create broker
            try {
                broker.addConnector(getTcpConnectionURI());
                broker.addConnector(getStompConnectionURI());
                broker.start();
            } catch (Exception e) {
                // There is probably another broker running on those ports.
                // If not, it will fail to make the connection and we'll log
                // the error there.
            }

            connection = factory.createConnection();
            connection.setExceptionListener(exceptionListener);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            if (broker.isStarted()) {
                for (ClimateNotificationJobListener listener : receiverListeners) {
                    listener.receiverConnected();
                }
            }

            // Start processing
            connection.start();
            connectionStarted = true;

            this.connectionAttempts = 0;
        } catch (JMSException e) {
            statusHandler.error("Error starting receiver connection", e);
            disconnect();
        }
    }

    private void disconnect() {

        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                // Ignore error
            }
            this.session = null;
        }

        if (connectionStarted) {
            try {
                connection.stop();
            } catch (JMSException e) {
                // Ignore error
            }
            this.connectionStarted = false;
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                // Ignore error
            }
            this.connection = null;
        }

        if (broker.isStarted()) {
            try {
                broker.stop();
            } catch (Exception e) {
                // Ignore error
            }
        }

        // copy to avoid concurrent mod error
        List<TransportConnector> copy = new ArrayList<>(
                broker.getTransportConnectors());
        for (TransportConnector connector : copy) {
            try {
                broker.removeConnector(connector);
            } catch (Exception e) {
                // Ignore error
            }
        }
    }

    private String getTcpConnectionURI() {
        return TCP_CONNECTION + port;
    }

    private String getStompConnectionURI() {
        return STOMP_CONNECTION + (port + 1);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        while (monitor.isCanceled() == false) {
            if (connection == null
                    && connectionAttempts < MAX_CONNECTION_ATTEMPTS) {
                connectionAttempts += 1;
                synchronized (this) {
                    connect();
                }
                if (connection == null) {
                    try {
                        Thread.sleep(10 * 1000L);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            } else {
                synchronized (waiter) {
                    try {
                        waiter.wait();
                    } catch (InterruptedException e) {
                        // Ignore interruption
                    }
                }
            }
        }

        try {
            this.connection.setExceptionListener(null);
        } catch (JMSException e) {
            statusHandler.error("Error with Climate Monitor", e);
        }
        disconnect();

        return Status.OK_STATUS;
    }

    @Override
    protected void canceling() {
        // Wake up the thread
        synchronized (waiter) {
            waiter.notify();
        }

        // clear callbacks
        climateCallbacks.clear();

        // set instance to null
        instance = null;
    }

    @Override
    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    @Override
    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public void setCanExit(boolean canExit) {
        this.canExit = canExit;
    }

    @Override
    public boolean isCanExit() {
        return canExit;
    }

}