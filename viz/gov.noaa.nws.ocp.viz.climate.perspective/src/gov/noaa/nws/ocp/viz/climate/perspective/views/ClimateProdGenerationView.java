/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.alertviz.AlertVizPreferences;
import com.raytheon.uf.viz.core.ProgramArguments;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.core.mode.CAVEMode;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdGenerateSessionDataForView;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.ProductSetStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.StateStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CancelClimateProdGenerateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ForwardProdToNWRForReviewRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.GetClimateProdGenerateSessionRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.SendClimateProductsResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateMessageUtils;
import gov.noaa.nws.ocp.viz.climate.perspective.Activator;
import gov.noaa.nws.ocp.viz.climate.perspective.notify.ClimateNotificationJob;
import gov.noaa.nws.ocp.viz.climate.perspective.notify.IClimateMessageCallback;

/**
 * Climate Product Generation monitor.
 * 
 * This dialog is the main control for climate product generation. It presents
 * all product generation sessions to the user and allows the user to start a
 * new session, view, manage, or cancel a session.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 01, 2017 27199      jwu         Initial creation
 * Apr 11, 2017 27199      jwu         Integrated with CPG session/Display/Review.
 * Apr 19, 2017 27199      jwu         Invoke NWR/NWWS review with separate buttons.
 * May 09, 2017 33534      jwu         Re-implement REVIEW/SEND state.
 * May 18, 2017 27199      jwu         Update logic for switching sessions.
 * May 22, 2017 27199      jwu         Remove connection listener (set in Activator already).
 * May 24, 2017 33104      amoore      Small edit to message, spelling correction.
 * Jun 02, 2017 34773      jwu         Add label for identify each session.
 * Jun 02, 2017 34775      jwu         Set NWR sent message as "See NWR Waves".
 * Jun 02, 2017 34792      jwu         Disable NWR/NWWS review if no products.
 * Jun 05, 2017 27199      jwu         Update NWR Send status message.
 * Aug 02, 2017 36648      amoore      Additional null check.
 * Aug 08, 2017 33104      amoore      Check against disposed status to avoid errors on message
 *                                     receipt when CAVE or climate perspective are closing.
 * Aug 16, 2017 36648      amoore      Synchronize on table/session given asynchronous messages.
 * Sep 19, 2017 38124      amoore      Use GC for text control sizes.
 * May 03, 2018 20711      amoore      Climate should stop listening for callbacks when perspective
 *                                     is closed.
 * Jun 07, 2018 20760      amoore      Product Process buttons need min size set, or in some
 *                                     situations they may be squished vertically until CAVE is
 *                                     restarted/the perspective is re-initialized.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ClimateProdGenerationView extends ViewPart
        implements IClimateMessageCallback {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerationView.class);

    /**
     * View ID.
     */
    public static final String ID = "gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateProdGenerationView";

    /**
     * ClimateView to show sent products.
     */
    private ClimateView climateView;

    /**
     * Keys defined in ClimateProdGenerateSession for sending message.
     */
    private static final String[] CLIMATE_MESSAGE_KEYS = new String[] { "ID",
            "STATE", "STATUS_CODE", "STATUS_DESC", "LAST_UPDATE", "ACTION",
            "USER", "TOTAL_SECONDS", "SECONDS_PASSED", "PERCENT_PASSED" };

    /**
     * Separator for parsing a climate message.
     */
    private static final String CLIMATE_MESSAGE_SEPARATOR = ",";

    private static final String CLIMATE_MESSAGE_PAIR = "=";

    /**
     * CPG session types corresponding to PeriodType: one of 1(AM), 10(IM),
     * 2(PM), 5(Monthly), 7(Seasonal), 9(Annual).
     */
    private static final PeriodType[] CLIMATE_SESSION_TYPES = new PeriodType[] {
            PeriodType.MORN_RAD, PeriodType.INTER_RAD, PeriodType.EVEN_RAD,
            PeriodType.MONTHLY_RAD, PeriodType.SEASONAL_RAD,
            PeriodType.ANNUAL_RAD };

    /**
     * Image names/files.
     */
    private static final String[] SESSION_IMAGE_NAMES = new String[] { "AM",
            "IM", "PM", "MON", "SEA", "ANN" };

    private static final String[] SESSION_IMAGE_LABELS = new String[] { " AM",
            "  IM", " PM", "MON", " SEA", " ANN" };

    private static final String[] SESSION_IMAGES_FILES = new String[] {
            "cli_disp_morning.png", "cli_disp_int.png", "cli_disp_evening.png",
            "cli_disp_monthly.png", "cli_disp_seasonal.png",
            "cli_disp_annual.png" };

    /**
     * Session stages.
     */
    private enum SessionStage {
        CREATE, DISPLAY, FORMAT, REVIEW_NWR, REVIEW_NWWS, SEND
    }

    private static final String REVIEW_NWR = "NWR";

    private static final String REVIEW_NWWS = "NWWS";

    /**
     * Session info to be displayed in table.
     */
    private static final String[] CLIMATE_SESSION_COLUMN = new String[] {
            "Product", "Run Type", "Start Time", "Stage", "Status",
            "Last Update", "Waiting" };

    /**
     * Session run types.
     */
    private static final String[] CLIMATE_SESSION_RUNTYPE = new String[] {
            "UNKNOWN", "AUTO", "MANUAL" };

    /**
     * Custom strings.
     */
    private static final String SESSION_ID = "sessionID";

    private static final String UNKNOWN = "UNKNOWN";

    private static final String WAITING = "WAITING";

    private static final String READY = "READY";

    private static final String IS_DAILY = "isDaily";

    private static final String ERROR = "ERROR";

    private static final String NA = "N/A";

    private static final String SEE_NWRWAVES = "See NWRWaves";

    /**
     * User name.
     */
    private String userName;

    /**
     * This View.
     */
    private Composite view;

    private IWorkbenchPage page;

    /**
     * Main composite.
     */
    private ScrolledComposite scroll;

    private static final int MIN_VIEWER_WIDTH = 1000;

    private static final int MIN_VIEWER_HEIGHT = 1000;

    private Composite mainComp;

    /**
     * Fonts.
     */
    private Font size14FontBold;

    private Font size12Font;

    private Font size12FontBold;

    private Font size10Font;

    private Font size10FontBold;

    /**
     * Colors.
     */
    private Color colorDarkGrey;

    private Color colorGreen;

    private Color colorRed;

    private Color colorYellow;

    /**
     * Color.
     */
    private Cursor waitCursor;

    /**
     * Table to list CPG sessions.
     */
    private Table cpgSessionTable;

    /**
     * All existing CPG session.
     */
    private LinkedHashMap<String, ClimateProdGenerateSessionDataForView> allCpgSessions;

    /**
     * Table and sessions lock.
     */
    private final Object tableSessionLock = new Object();

    /**
     * The currently-selected session.
     */
    private String currentSession = UNKNOWN;

    /**
     * Map of available icons
     */
    private HashMap<String, Image> iconMap = null;

    /**
     * Label to show current session's Type/Product type/Start time.
     */
    private Group manageSessionGrp;

    private Label currentType;

    private Label currentProductType;

    private Label startedLbl;

    /**
     * Buttons to show a selected session's state/status.
     */
    private Button createBtn;

    private Button displayBtn;

    private Button formatBtn;

    private Button reviewNWRBtn;

    private Button reviewNWWSBtn;

    private Button sendNWRBtn;

    private Button sendNWWSBtn;

    /**
     * Button to cancel the current CPG session.
     */
    private Button abortBtn;

    /**
     * Default constructor.
     */
    public ClimateProdGenerationView() {

    }

    /**
     * Initialize this View.
     */
    @Override
    public void init(IViewSite site) throws PartInitException {

        try {
            super.init(site);
        } catch (PartInitException pie) {
            logger.error(
                    "ClimateProdGenerationView: failed to initialize the monitor.",
                    pie);
        }

        page = site.getPage();

        // Create resource
        createResources();

        // Load images
        loadIconImages();

        // Check the port - by default we share the port from AlertViz.
        int port = -1;
        Integer checkPort = ProgramArguments.getInstance().getInteger("-p");
        if (checkPort != null) {
            port = checkPort.intValue();
        }

        if (port < 1) {
            // access prefs after localization is initialized
            port = AlertVizPreferences.getAlertVizPort();
        }

        /*
         * Start ClimateNotificationJob - ClimateNotificationJobListener has
         * been instantiated in this bundle's Activator.start().
         */
        ClimateNotificationJob.getInstance().setEmbedded(false);

        ClimateNotificationJob.getInstance().addClimateCallback(this);

        ClimateNotificationJob.getInstance().start(port);

        // Query CPG sessions - ClimateProdGenerateSessionDataForView[]
        retrieveCPGSessions();

        // Get user name.
        userName = System.getProperty("user.name");

    }

    /**
     * Disposes resource.
     */
    @Override
    public void dispose() {
        disposeResources();

        ClimateNotificationJob.getInstance().removeClimateCallback(this);

        super.dispose();
    }

    /**
     * Sets up the SWT controls..
     */
    @Override
    public void createPartControl(Composite parent) {

        view = parent;

        // A scroll-able composite to hold all widgets.
        scroll = new ScrolledComposite(view,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        scroll.setMinSize(MIN_VIEWER_WIDTH, MIN_VIEWER_HEIGHT);
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);

        mainComp = new Composite(scroll, SWT.NONE);

        scroll.setContent(mainComp);

        // mainComp = new Composite(parent, SWT.NONE);

        // Main layout with a single column, no equal width.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.verticalSpacing = 45;
        mainLayout.marginTop = 20;
        mainLayout.marginWidth = 120;
        mainComp.setLayout(mainLayout);

        // Create a main title.
        Label monitorLbl = new Label(mainComp, SWT.NORMAL);
        monitorLbl.setText("Climate Product Generation Monitor");
        monitorLbl.setFont(size14FontBold);

        // Group to start a new session.
        createStartSessionSection(mainComp);

        // Composite to manage a selected session.
        createManageSessionSection(mainComp);

        // Group to list all session's info.
        createSessionTable(mainComp);

        mainComp.layout(true, true);

        // Get the view that shows sent products.
        climateView = (ClimateView) page.findView(ClimateView.ID);

    }

    /**
     * Invoked by the workbench when needed
     */
    @Override
    public void setFocus() {
        view.setFocus();
    }

    /**
     * Create a group to start a new session
     * 
     * @param parent
     *            Composite
     */
    private void createStartSessionSection(Composite parent) {

        // Group to start a new session.
        Composite startSessionComp = new Composite(parent, SWT.NONE);
        startSessionComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        GridLayout startSessionCompLayout = new GridLayout(2, false);
        startSessionCompLayout.horizontalSpacing = 25;
        startSessionCompLayout.marginWidth = 3;
        startSessionComp.setLayout(startSessionCompLayout);

        // Group to start a new session.
        Group startSessionGrp = new Group(startSessionComp, SWT.NONE);
        GridLayout startSessionLayout = new GridLayout(1, false);
        startSessionLayout.horizontalSpacing = 5;
        startSessionLayout.marginWidth = 3;
        startSessionGrp.setLayout(startSessionLayout);
        startSessionGrp.setText("Start a New Product");
        startSessionGrp.setFont(size12FontBold);

        Composite sessionTypeComp = new Composite(startSessionGrp, SWT.NONE);
        RowLayout sessionTypeLayout = new RowLayout(SWT.HORIZONTAL);
        sessionTypeLayout.spacing = 15;
        sessionTypeLayout.fill = true;
        sessionTypeLayout.center = true;
        sessionTypeLayout.marginLeft = 15;
        sessionTypeComp.setLayout(sessionTypeLayout);

        int ibr = 0;
        Button[] sessionTypeBtn = new Button[CLIMATE_SESSION_TYPES.length];
        for (PeriodType sessionType : CLIMATE_SESSION_TYPES) {

            Composite sessionComp = new Composite(sessionTypeComp, SWT.NONE);
            GridLayout sessionLayout = new GridLayout(1, false);
            sessionLayout.horizontalSpacing = 5;
            sessionLayout.marginWidth = 3;
            sessionComp.setLayout(sessionLayout);

            sessionTypeBtn[ibr] = new Button(sessionComp, SWT.PUSH);
            Image iconImg = getIcon(sessionType.getPeriodName().toUpperCase());
            if (iconImg != null) {
                sessionTypeBtn[ibr].setImage(iconImg);
            } else {
                sessionTypeBtn[ibr].setText(sessionType.getPeriodName());
            }

            sessionTypeBtn[ibr].setData(sessionType);
            sessionTypeBtn[ibr].setFont(size12FontBold);
            sessionTypeBtn[ibr].setToolTipText("Click to start a "
                    + sessionType.getPeriodDescriptor() + " product session.");
            sessionTypeBtn[ibr].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    // Start a new session
                    PeriodType sessionType = (PeriodType) event.widget
                            .getData();
                    startSession(sessionType);
                }
            });

            // Create a label below to help identify the session.
            Label nameLbl = new Label(sessionComp, SWT.NORMAL);
            nameLbl.setText(SESSION_IMAGE_LABELS[ibr]);
            nameLbl.setFont(size10FontBold);

            ibr++;
        }

        abortBtn = new Button(startSessionComp, SWT.PUSH);
        abortBtn.setText("Abort");
        abortBtn.setFont(size12FontBold);
        abortBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                abortSession();
            }

        });

    }

    /**
     * Create a composite to manage a selected session
     * 
     * @param parent
     *            Composite
     */
    private void createManageSessionSection(Composite parent) {
        // Group for session info/status.
        manageSessionGrp = new Group(parent, SWT.NONE);
        manageSessionGrp.setFont(size12FontBold);
        GridData manageSessionGrpGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                true);
        GC gc = new GC(manageSessionGrp);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();
        manageSessionGrpGD.minimumWidth = 120 * fontWidth;
        manageSessionGrp.setLayoutData(manageSessionGrpGD);
        GridLayout manageSessionGrpLayout = new GridLayout(1, false);
        manageSessionGrpLayout.verticalSpacing = 3;
        manageSessionGrpLayout.marginWidth = 25;
        manageSessionGrp.setLayout(manageSessionGrpLayout);
        manageSessionGrp.setText("Current Product Process");

        // Composite for session info (type/run type/start time).
        Composite sessionInfoComp = new Composite(manageSessionGrp, SWT.NONE);
        RowLayout sessionInfoLayout = new RowLayout(SWT.HORIZONTAL);
        sessionInfoLayout.spacing = 65;
        sessionInfoLayout.fill = true;
        sessionInfoLayout.marginHeight = 10;
        sessionInfoLayout.marginWidth = 10;
        sessionInfoComp.setLayout(sessionInfoLayout);

        RowLayout sessionIDLayout = new RowLayout(SWT.HORIZONTAL);
        sessionIDLayout.spacing = 5;
        sessionIDLayout.marginLeft = 20;
        sessionIDLayout.fill = true;

        Composite sessionTypeLblComp = new Composite(sessionInfoComp, SWT.NONE);
        sessionTypeLblComp.setLayout(sessionIDLayout);

        Label cpgTypeLbl1 = new Label(sessionTypeLblComp, SWT.NORMAL);
        cpgTypeLbl1.setText("Type: ");
        cpgTypeLbl1.setFont(size10FontBold);

        currentType = new Label(sessionTypeLblComp, SWT.NORMAL);
        currentType.setText(UNKNOWN);
        currentType.setFont(size10Font);

        Composite sessionProdComp = new Composite(sessionInfoComp, SWT.NONE);
        sessionProdComp.setLayout(sessionIDLayout);
        Label cpgProdLbl1 = new Label(sessionProdComp, SWT.NORMAL);
        cpgProdLbl1.setText("Product: ");
        cpgProdLbl1.setFont(size10FontBold);

        currentProductType = new Label(sessionProdComp, SWT.NORMAL);
        currentProductType.setText(UNKNOWN);
        currentProductType.setFont(size10Font);

        Composite sessionTimeComp = new Composite(sessionInfoComp, SWT.NONE);
        sessionTimeComp.setLayout(sessionIDLayout);

        Label cpgTimeLbl = new Label(sessionTimeComp, SWT.NORMAL);
        cpgTimeLbl.setText("Started: ");
        cpgTimeLbl.setFont(size10FontBold);

        startedLbl = new Label(sessionTimeComp, SWT.NORMAL);
        startedLbl.setFont(size10Font);

        // Composite for current session's state/status.
        Composite sessionStateComp = new Composite(manageSessionGrp, SWT.NONE);
        sessionStateComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout sessionStageLayout = new GridLayout(
                SessionStage.values().length - 1, true);
        sessionStageLayout.horizontalSpacing = 16;
        sessionStageLayout.verticalSpacing = 0;
        sessionStageLayout.marginHeight = 1;
        sessionStageLayout.marginWidth = 3;
        sessionStateComp.setLayout(sessionStageLayout);
        sessionStateComp.setFont(size12Font);

        // Labels to identify session state
        GridData stateLblGd1 = new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1);
        createStateLabel(sessionStateComp, SessionStage.CREATE.toString(),
                stateLblGd1);
        GridData stateLblGd2 = new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1);
        createStateLabel(sessionStateComp, SessionStage.DISPLAY.toString(),
                stateLblGd2);
        GridData stateLblGd3 = new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1);
        createStateLabel(sessionStateComp, SessionStage.FORMAT.toString(),
                stateLblGd3);
        GridData stateLblGd4 = new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1);
        createStateLabel(sessionStateComp, "REVIEW", stateLblGd4);
        GridData stateLblGd5 = new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1);
        createStateLabel(sessionStateComp, SessionStage.SEND.toString(),
                stateLblGd5);

        // buttons for current session's status.
        GridData statusBtnGd1 = new GridData(SWT.FILL, SWT.CENTER, true, true,
                1, 1);
        createBtn = createStatusBtn(sessionStateComp, SessionStage.CREATE,
                statusBtnGd1);
        GridData statusBtnGd2 = new GridData(SWT.FILL, SWT.CENTER, true, true,
                1, 1);
        displayBtn = createStatusBtn(sessionStateComp, SessionStage.DISPLAY,
                statusBtnGd2);
        GridData statusBtnGd3 = new GridData(SWT.FILL, SWT.CENTER, true, true,
                1, 1);
        formatBtn = createStatusBtn(sessionStateComp, SessionStage.FORMAT,
                statusBtnGd3);

        createReviewComp(sessionStateComp);

        createSendComp(sessionStateComp);
    }

    /**
     * Create a group to view/list all sessions
     * 
     * @param parent
     *            Composite
     */
    private void createSessionTable(Composite parent) {

        // Create a group to hold the table.
        Group statusGrp = new Group(parent, SWT.NONE);
        statusGrp.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, true, true));
        GridLayout statusLayout = new GridLayout(1, false);
        statusLayout.horizontalSpacing = 5;
        statusLayout.marginWidth = 3;
        statusGrp.setLayout(statusLayout);
        statusGrp.setText("Product Process List");
        statusGrp.setFont(size12FontBold);

        Composite statusComp = new Composite(statusGrp, SWT.NONE);
        GridLayout statusCompLayout = new GridLayout(1, false);
        statusCompLayout.verticalSpacing = 5;
        statusCompLayout.marginTop = 10;
        statusCompLayout.marginWidth = 3;
        statusComp.setLayout(statusCompLayout);

        synchronized (tableSessionLock) {
            cpgSessionTable = new Table(statusComp, SWT.BORDER | SWT.SINGLE
                    | SWT.V_SCROLL | SWT.H_SCROLL | SWT.HIDE_SELECTION);

            GC gc = new GC(cpgSessionTable);
            /*
             * Height to limit space table will occupy
             */
            int fontHeight = gc.getFontMetrics().getHeight();
            int fontWidth = gc.getFontMetrics().getAverageCharWidth();
            gc.dispose();

            cpgSessionTable.setHeaderVisible(true);
            cpgSessionTable.setLinesVisible(true);
            GridData statusTableGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false, 1, 1);
            statusTableGd.heightHint = 14 * fontHeight;
            statusTableGd.minimumWidth = 135 * fontWidth;
            cpgSessionTable.setLayoutData(statusTableGd);

            int[] sessionColWidths = new int[] { 12 * fontWidth, 13 * fontWidth,
                    29 * fontWidth, 15 * fontWidth, 15 * fontWidth,
                    29 * fontWidth, 12 * fontWidth };

            int icol = 0;
            for (String scol : CLIMATE_SESSION_COLUMN) {
                TableColumn sessionColumn = new TableColumn(cpgSessionTable,
                        SWT.NONE);
                sessionColumn.setWidth(sessionColWidths[icol]);
                sessionColumn.setText(scol);
                sessionColumn.setAlignment(SWT.CENTER);
                icol++;
            }

            // Fill table with CPG session info
            for (Entry<String, ClimateProdGenerateSessionDataForView> cpgSess : allCpgSessions
                    .entrySet()) {
                addSession(cpgSess.getValue(), false);
            }

            cpgSessionTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TableItem selected = (TableItem) e.item;
                    String sid = (String) selected.getData();
                    viewSession(sid);
                    mainComp.layout(true, true);
                }
            });

            // Set the first one in the table as the current session.
            if (allCpgSessions.size() > 0) {
                cpgSessionTable.select(0);
                viewSession((String) cpgSessionTable.getItem(0).getData());
            }
        }
    }

    /**
     * Create fonts and colors used in this view.
     */
    private void createResources() {

        Display currentDisplay = Display.getCurrent();
        FontData fontData = currentDisplay.getSystemFont().getFontData()[0];

        size14FontBold = new Font(currentDisplay,
                new FontData(fontData.getName(), 14, SWT.BOLD));
        size12Font = new Font(currentDisplay,
                new FontData(fontData.getName(), 12, SWT.NORMAL));
        size12FontBold = new Font(currentDisplay,
                new FontData(fontData.getName(), 12, SWT.BOLD));

        size10Font = new Font(currentDisplay,
                new FontData(fontData.getName(), 10, SWT.NORMAL));
        size10FontBold = new Font(currentDisplay,
                new FontData(fontData.getName(), 10, SWT.BOLD));

        colorDarkGrey = currentDisplay.getSystemColor(SWT.COLOR_DARK_GRAY);
        colorGreen = currentDisplay.getSystemColor(SWT.COLOR_GREEN);
        colorRed = currentDisplay.getSystemColor(SWT.COLOR_RED);
        colorYellow = currentDisplay.getSystemColor(SWT.COLOR_YELLOW);

        waitCursor = currentDisplay.getSystemCursor(SWT.CURSOR_WAIT);
    }

    /**
     * Dispose the fonts/colors/images created in this view.
     */
    private void disposeResources() {
        size14FontBold.dispose();
        size12Font.dispose();
        size12FontBold.dispose();
        size10Font.dispose();
        size10FontBold.dispose();

        // Dispose of icons
        for (Image icon : iconMap.values()) {
            icon.dispose();
        }

        iconMap.clear();
    }

    /**
     * Returns an icon image
     *
     * @param iconName
     *            name of the icon
     * @return Image Image from the icon file
     */
    private Image getIcon(String iconName) {
        return iconMap.get(iconName);
    }

    /**
     * Load all icon images.
     */
    private void loadIconImages() {

        if (iconMap == null) {
            iconMap = new HashMap<>();
        }

        int ii = 0;
        for (String imgLoc : SESSION_IMAGES_FILES) {
            ImageDescriptor id = Activator.imageDescriptorFromPlugin(
                    Activator.PLUGIN_ID, "icons/" + imgLoc);

            if (id != null) {
                Image img = id.createImage();
                if (img != null) {
                    iconMap.put(SESSION_IMAGE_NAMES[ii], img);
                }
            }

            ii++;
        }
    }

    /**
     * Add an table entry for a CPG session
     * 
     * @param cpgSession
     *            A ClimateProdGenerateSessionDataForView
     * @param insert
     *            Flag to insert at a proper location.
     */
    private void addSession(ClimateProdGenerateSessionDataForView cpgSession,
            boolean insert) {

        // Create a new item.
        TableItem item;

        synchronized (tableSessionLock) {
            if (!insert) {
                item = new TableItem(cpgSessionTable, SWT.NONE);
            } else {
                int loc = 0;
                for (int ii = 0; ii < cpgSessionTable.getItemCount(); ii++) {
                    TableItem itemAt = cpgSessionTable.getItem(ii);
                    String sid = (String) itemAt.getData();
                    ClimateProdGenerateSessionDataForView data = allCpgSessions
                            .get(sid);

                    if (data == null) {
                        logger.warn(
                                "Could not find data for listed session ID: ["
                                        + sid + "]");
                        break;
                    }
                    if (cpgSession.getStart_at().after(data.getStart_at())) {
                        break;
                    }

                    loc = ii;
                }

                item = new TableItem(cpgSessionTable, SWT.NONE, loc);
            }

            // Collect data for columns.
            item.setData(cpgSession.getCpg_session_id());

            PeriodType prdTyp = cpgSession.getProd_type();

            String runTyp = getRunType(cpgSession);

            SessionState prdState = cpgSession.getState();
            String statusStr = getStateStatus(cpgSession);

            // Start time last update time will not show milliseconds.
            String startTime = cpgSession.getStart_at().toLocalDateTime()
                    .toString();
            String endTime = cpgSession.getLast_updated().toLocalDateTime()
                    .toString();

            item.setText(new String[] { prdTyp.getPeriodName().toUpperCase(),
                    runTyp, startTime.substring(0, startTime.length() - 4),
                    prdState.toString(), statusStr,
                    endTime.substring(0, endTime.length() - 4), NA });
        }
    }

    /**
     * Start a new session.
     * 
     * @param sessionType
     *            PeriodType of the session, one of 1(AM),10(IM), 2(PM),
     *            5(Monthly), 7(Seasonal), 9(Annual).
     */
    private void startSession(PeriodType sessionType) {

        switch (sessionType) {

        case MORN_RAD:
            execCommand(
                    "gov.noaa.nws.ocp.viz.climate.display.DisplayStationDailyMorning");
            break;
        case INTER_RAD:
            execCommand(
                    "gov.noaa.nws.ocp.viz.climate.display.DisplayStationDailyIntermediate");
            break;
        case EVEN_RAD:
            execCommand(
                    "gov.noaa.nws.ocp.viz.climate.display.DisplayStationDailyEvening");
            break;
        case MONTHLY_RAD:
            execCommand(
                    "gov.noaa.nws.ocp.viz.climate.display.DisplayStationMonthly");
            break;

        case SEASONAL_RAD:
            execCommand(
                    "gov.noaa.nws.ocp.viz.climate.display.DisplayStationSeasonal");
            break;
        case ANNUAL_RAD:
            execCommand(
                    "gov.noaa.nws.ocp.viz.climate.display.DisplayStationAnnual");
            break;

        default:
            break;
        }
    }

    /**
     * View a session in "Manage a session" panel with its stage, status, and
     * progress updated/colored. This session will be set as the current
     * session.
     * 
     * @param sid
     *            CPG session ID
     */
    private void viewSession(String sid) {

        // First retrieve the session's most recent info from DB
        ClimateProdGenerateSessionDataForView cpgSess = retrieveCPGSession(sid);

        /*
         * Remove from table if the session has been purged. Otherwise, set it
         * as current session and update on "Manage a session" panel.
         */
        if (cpgSess == null) {
            removeSession(sid);
        } else {
            updateManageSection(cpgSess, null);
        }
    }

    /**
     * Terminate currently-selected session. The session stays but the execution
     * stops.
     */
    private void abortSession() {
        synchronized (tableSessionLock) {
            if (allCpgSessions.containsKey(currentSession)) {

                MessageDialog confirmDlg = new MessageDialog(
                        this.getViewSite().getShell(), "Abort Product Process",
                        null,
                        "Are you sure you want to abort process\n["
                                + currentSession + "]?",
                        MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);

                confirmDlg.open();

                if (confirmDlg.getReturnCode() == MessageDialog.OK) {

                    CancelClimateProdGenerateRequest cancelSessionRequest = new CancelClimateProdGenerateRequest(
                            currentSession, "", userName);

                    try {
                        ThriftClient.sendRequest(cancelSessionRequest);
                    } catch (VizException e) {
                        logger.warn(
                                "ClimateProdGenerationView: failed to stop CPG session "
                                        + currentSession + " due to " + e);
                    }
                }
            }
        }
    }

    /**
     * Manage a selected session. This could terminate the "WAIT" status for a
     * auto process and bring up the "Display" or "Review" dialog based on the
     * session's stage and status.
     * 
     * @param stage
     *            Stage of a CPG session
     */
    private void manageSession(SessionStage stage) {

        // Sanity check.
        if (currentSession.equals(UNKNOWN)) {
            return;
        }

        /*
         * If stage is "Display", bring up the Display window. If stage is
         * "Review", bring up the Review window.
         */
        switch (stage) {

        case DISPLAY:

            boolean display = true;
            if (!displayBtn.getText().contains(READY)) {
                MessageDialog confirmDlg = new MessageDialog(
                        this.getViewSite().getShell(), "Re-display a Product",
                        null,
                        "This product has completed the Display phase sucessfully before."
                                + " Are you sure you want to display it again?\n["
                                + currentSession + "]?",
                        MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);

                confirmDlg.open();

                if (confirmDlg.getReturnCode() != MessageDialog.OK) {
                    display = false;
                }
            }

            if (display) {
                execCommand(
                        "gov.noaa.nws.ocp.viz.climate.display.DisplayStationWithData",
                        currentSession);
            }

            break;

        case REVIEW_NWR:

            ForwardProdToNWRForReviewRequest forwardSessionRequest = new ForwardProdToNWRForReviewRequest(
                    currentSession, userName);

            CAVEMode mode = CAVEMode.getMode();
            boolean operationalMode = (CAVEMode.OPERATIONAL.equals(mode)
                    || CAVEMode.TEST.equals(mode));
            forwardSessionRequest.setOperational(operationalMode);

            SendClimateProductsResponse resp = null;
            try {
                resp = (SendClimateProductsResponse) ThriftClient
                        .sendRequest(forwardSessionRequest);
            } catch (VizException e) {
                logger.warn(
                        "ClimateProdGenerationView: Forward product to NWR for review due to "
                                + e);
            }

            if (resp.hasError()) {
                logger.warn(
                        "ClimateProdGenerationView: ForwardProdToNWRForReview failed. ");
            } else {
                reviewNWRBtn.setBackground(colorGreen);
                reviewNWRBtn.setCursor(null);

                execCommand("gov.noaa.nws.ocp.viz.climate.review.reviewnwr",
                        currentSession);
            }

            break;

        case REVIEW_NWWS:
            execCommand("gov.noaa.nws.ocp.viz.climate.review.reviewnwws",
                    currentSession);
            break;

        default:
            break;
        }

    }

    /**
     * Remove a session from the table if it no longer exists (purged).
     * 
     * @param sid
     *            CPG session ID
     */
    private void removeSession(String sid) {
        synchronized (tableSessionLock) {
            int loc = findSession(sid);

            if (loc >= 0) {
                cpgSessionTable.remove(loc);
                allCpgSessions.remove(sid);
            }
            // Select the one after as the current session?
            /*-            
            int select = Math.min(cpgSessionTable.getItemCount() - 1, loc);
            if (select >= 0) {
                viewSession((String) cpgSessionTable.getItem(select).getData());
            }*/
        }
    }

    /**
     * Get a given "command" and execute it.
     *
     * @param command
     *            A command in climate for climate application.
     */
    private void execCommand(String command) {
        execCommand(command, null);
    }

    /**
     * Get a given "command" and execute it with given data object.
     *
     * @param command
     *            A command in climate for climate application.
     * @param sid
     *            CPG Session ID.
     */
    private void execCommand(String command, String sid) {

        // Locate and prepare command
        ICommandService service = (ICommandService) page.getActivePart()
                .getSite().getService(ICommandService.class);
        Command cmd = service.getCommand(command);

        // Execute command with parameters.
        if (cmd != null) {

            try {
                synchronized (tableSessionLock) {
                    HashMap<String, Object> params = new HashMap<String, Object>();

                    if (sid != null) {
                        params.put(SESSION_ID, sid);
                        ClimateProdGenerateSessionDataForView data = allCpgSessions
                                .get(sid);
                        if (data != null) {
                            boolean isDaily = data.getProd_type().isDaily();
                            params.put(IS_DAILY,
                                    Boolean.valueOf(isDaily).toString());
                        }
                    }

                    ExecutionEvent exec = new ExecutionEvent(cmd, params, null,
                            null);

                    cmd.executeWithChecks(exec);
                }
            } catch (Exception e) {
                logger.error(
                        "ClimateProdGenerationView: failed to invoke command "
                                + command,
                        e);
            }
        }
    }

    /**
     * Retrieve all CPG sessions.
     */
    @SuppressWarnings("unchecked")
    private void retrieveCPGSessions() {
        synchronized (tableSessionLock) {
            if (allCpgSessions == null) {
                allCpgSessions = new LinkedHashMap<>();
            }

            GetClimateProdGenerateSessionRequest getSessionRequest = new GetClimateProdGenerateSessionRequest();
            List<ClimateProdGenerateSessionDataForView> cpgSessions = null;

            try {
                cpgSessions = (List<ClimateProdGenerateSessionDataForView>) ThriftClient
                        .sendRequest(getSessionRequest);
            } catch (VizException e) {
                logger.error(
                        "ClimateProdGenerationView: No sessions retrieved.", e);
            }

            // Store as global to update the status in the table.
            if (cpgSessions != null) {

                Collections.sort(cpgSessions);
                Collections.reverse(cpgSessions);

                for (ClimateProdGenerateSessionDataForView sess : cpgSessions) {
                    allCpgSessions.put(sess.getCpg_session_id(), sess);
                }
            }
        }
    }

    /**
     * Get value for defined CPG message keys. The message comes in format of
     * "ID=???,STATE=???,..."
     * 
     * @param msg
     *            CPG message
     * @return Map A map contain message in key/value. The value could be null
     */
    private Map<String, String> parseCPGMessage(String msg) {

        Map<String, String> cpgMsgMap = new HashMap<>();

        Map<String, String> iniMsg = splitCPGMessage(msg);
        for (String msgKey : CLIMATE_MESSAGE_KEYS) {
            cpgMsgMap.put(msgKey, iniMsg.get(msgKey));
        }

        return cpgMsgMap;

    }

    /**
     * Parse a CPG message into a map for query. The message comes in format of
     * "ID=???,STATE=???,..."
     * 
     * @param msg
     *            CPG message
     * @return Map<String, String> A map contain message in key/value.
     */
    private Map<String, String> splitCPGMessage(String msg) {

        Map<String, String> cpgMsgMap = new HashMap<>();

        if (msg != null) {
            String[] msgArray = msg.split(CLIMATE_MESSAGE_SEPARATOR);
            for (String pair : msgArray) {
                String[] msgValue = pair.split(CLIMATE_MESSAGE_PAIR);
                if (msgValue.length > 1) {
                    cpgMsgMap.put(msgValue[0], msgValue[1]);
                }
            }
        }

        return cpgMsgMap;

    }

    /**
     * Callback method to receive messages from CPG sessions.
     * 
     * @param statusMessage
     *            CPG message
     */
    @Override
    public void messageArrived(StatusMessage statusMessage) {
        if (!cpgSessionTable.isDisposed()) {
            // Update the view that shows sent products.
            boolean updateClimateView = false;
            if (statusMessage.getPlugin()
                    .equals(ClimateMessageUtils.F6_PLUGIN_ID)
                    || statusMessage.getPlugin()
                            .equals(ClimateMessageUtils.RER_PLUGIN_ID)) {
                /*
                 * if F6 or RER, just update view as there is no further
                 * information to display
                 */
                updateClimateView = true;
            } else { // CPG session message
                synchronized (tableSessionLock) {
                    Map<String, String> cpgMsg = parseCPGMessage(
                            statusMessage.getDetails());

                    logger.debug("Received CPG Message: [" + cpgMsg + "]");

                    // If this cpg session is not in the table, add it in.
                    String sid = cpgMsg.get(CLIMATE_MESSAGE_KEYS[0]);

                    logger.debug("CPG Message has SID: [" + sid + "]");

                    ClimateProdGenerateSessionDataForView cpgSess = allCpgSessions
                            .get(sid);

                    if (cpgSess == null) {
                        logger.debug(
                                "CPG SID: [" + sid + "] is new for the table.");

                        cpgSess = retrieveCPGSession(sid);

                        if (cpgSess != null) {
                            logger.debug("Retrieved CPG session details for: ["
                                    + sid + "].");
                            allCpgSessions.put(sid, cpgSess);
                            addSession(cpgSess, true);
                            cpgSessionTable.layout();
                        } else {
                            logger.warn(
                                    "Could not find CPG session details for: ["
                                            + sid + "].");
                        }
                    } else {
                        logger.debug("CPG SID: [" + sid
                                + "] is already in the table.");
                    }

                    // Pull this session's status if it has reached "REVIEW"
                    // state.
                    String cpgState = cpgMsg.get(CLIMATE_MESSAGE_KEYS[1]);
                    SessionState sessState = SessionState.valueOf(cpgState);
                    if (sessState.getValue() >= SessionState.REVIEW
                            .getValue()) {
                        ClimateProdGenerateSessionDataForView sess = retrieveCPGSession(
                                sid);
                        allCpgSessions.put(sid, sess);
                    }

                    // Update this session's info in the table.
                    updateSession(sid, cpgMsg);

                    /*
                     * Set this session to current session if this a manual
                     * session. For auto session, let the user decide with the
                     * timer message showing in the table.
                     */
                    if (!currentSession.equals(sid)
                            && !isAutoSession(cpgSess)) {
                        selectSession(sid);
                    }

                    // Manage this session if it is the current session.
                    if (sid.equals(currentSession)) {
                        updateManageSection(allCpgSessions.get(sid), cpgMsg);
                    }

                    // If REVIEW has been reached, update ClimateView.
                    if (sessState.getValue() >= SessionState.REVIEW
                            .getValue()) {

                        String cpgStatus = cpgMsg.get(CLIMATE_MESSAGE_KEYS[2]);

                        if (cpgStatus != null && cpgStatus.equals(
                                StateStatus.Status.SUCCESS.toString())) {
                            updateClimateView = true;
                        }
                    }
                }
            }

            // Inform ClimateView to update for newly-sent products.
            if (updateClimateView) {
                if (climateView == null) {
                    climateView = (ClimateView) page.findView(ClimateView.ID);
                }

                if (climateView != null) {
                    climateView.update();
                }
            }
        } else {
            logger.warn(
                    "CPG table is disposed, but received CPG status message: ["
                            + statusMessage.toString()
                            + "]. CAVE or Climate perspective may be in the process of closing.");
        }
    }

    /**
     * Retrieve a given CPG session.
     * 
     * @param sid
     *            CPG session ID
     * @return ClimateProdGenerateSessionDataForView A CPG session Data object.
     */
    private ClimateProdGenerateSessionDataForView retrieveCPGSession(
            String sid) {

        ClimateProdGenerateSessionDataForView cpgSess = null;

        GetClimateProdGenerateSessionRequest getSessionRequest = new GetClimateProdGenerateSessionRequest(
                sid);

        try {
            cpgSess = (ClimateProdGenerateSessionDataForView) ThriftClient
                    .sendRequest(getSessionRequest);
        } catch (VizException e) {
            logger.error("ClimateProdGenerationView: No session retrieved for ["
                    + sid + "]", e);
        }

        return cpgSess;
    }

    /**
     * Select a session in the table as the current session.
     * 
     * @param sid
     *            A CPG session ID
     */
    private void selectSession(String sid) {
        synchronized (tableSessionLock) {
            int loc = findSession(sid);
            if (loc >= 0) {
                cpgSessionTable.setSelection(loc);
                currentSession = sid;
            }
        }
    }

    /**
     * Update a session's info in the table.
     * 
     * @param sid
     *            CPG session ID
     * @param cpgMsg
     *            CPG message
     */
    private void updateSession(String sid, Map<String, String> cpgMsg) {
        synchronized (tableSessionLock) {
            int loc = findSession(sid);

            if (loc >= 0) {

                // Update table.
                TableItem cpgItem = cpgSessionTable.getItem(loc);

                String cpgState = cpgMsg.get(CLIMATE_MESSAGE_KEYS[1]);
                SessionState sessState = SessionState.valueOf(cpgState);

                if (cpgState != null) {
                    cpgItem.setText(3, cpgState);
                }

                if (cpgState != null && sessState
                        .getValue() >= SessionState.REVIEW.getValue()) {
                    String statusStr = getStateStatus(allCpgSessions.get(sid));
                    cpgItem.setText(4, statusStr);
                } else {
                    String cpgStatus = cpgMsg.get(CLIMATE_MESSAGE_KEYS[2]);
                    if (cpgStatus != null) {
                        cpgItem.setText(4, cpgStatus);
                    }
                }

                String lastUpdate = cpgMsg.get(CLIMATE_MESSAGE_KEYS[4]);
                if (lastUpdate != null) {
                    cpgItem.setText(5, lastUpdate);
                }

                // Show timer message to warn the user.
                String waitingStr = getTimerString(cpgMsg);
                cpgItem.setText(6, waitingStr);
                Color textColor = cpgItem.getBackground();
                if (!waitingStr.equals(NA)) {
                    textColor = colorYellow;
                }

                cpgItem.setBackground(6, textColor);

            }
        }
    }

    /**
     * Find the index of a session in the table.
     * 
     * @param sid
     *            CPG session ID
     */
    private int findSession(String sid) {
        int index = -1;
        synchronized (tableSessionLock) {
            int nsecessions = cpgSessionTable.getItemCount();
            for (int ii = 0; ii < nsecessions; ii++) {
                TableItem cpgItem = cpgSessionTable.getItem(ii);
                String cpgID = (String) cpgItem.getData();
                if (cpgID.equals(sid)) {
                    index = ii;
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Get session run type string for a ClimateProdGenerateSessionDataForView.
     * 
     * @param value
     *            ClimateProdGenerateSessionDataForView
     * @return getRunType() A string representing the run type.
     */
    private String getRunType(ClimateProdGenerateSessionDataForView sess) {
        int value = sess.getRun_type();
        String runTyp = CLIMATE_SESSION_RUNTYPE[0];
        if (value == 1) {
            runTyp = CLIMATE_SESSION_RUNTYPE[1];
        } else if (value == 2) {
            runTyp = CLIMATE_SESSION_RUNTYPE[2];
        }

        return runTyp;
    }

    /**
     * Reset labels/buttons in "Current Product Process" panel to default.
     */
    private void resetManageSectionHeader() {

        currentType.setText(UNKNOWN);
        currentProductType.setText(UNKNOWN);
        startedLbl.setText(UNKNOWN);
    }

    /**
     * Reset button status in "Current Product Process" panel to default.
     */
    private void resetManageSectionStatus() {

        createBtn.setText("");
        createBtn.setEnabled(false);
        createBtn.setBackground(colorDarkGrey);

        displayBtn.setText("");
        displayBtn.setEnabled(false);
        displayBtn.setBackground(colorDarkGrey);
        displayBtn.setCursor(null);

        formatBtn.setText("");
        formatBtn.setBackground(colorDarkGrey);

        reviewNWRBtn.setText("");
        reviewNWRBtn.setEnabled(false);
        reviewNWRBtn.setBackground(colorDarkGrey);
        reviewNWRBtn.setCursor(null);

        reviewNWWSBtn.setText("");
        reviewNWWSBtn.setEnabled(false);
        reviewNWWSBtn.setBackground(colorDarkGrey);
        reviewNWWSBtn.setCursor(null);

        sendNWRBtn.setText("");
        sendNWRBtn.setEnabled(false);
        sendNWRBtn.setBackground(colorDarkGrey);

        sendNWWSBtn.setText("");
        sendNWWSBtn.setEnabled(false);
        sendNWWSBtn.setBackground(colorDarkGrey);

    }

    /**
     * Update labels/buttons in "Current Product Process" panel for a given
     * session.
     * 
     * @param cpgSess
     *            ClimateProdGenerateSessionDataForView
     * @param cpgMsg
     *            Map<String, String> for progress report
     */
    private void updateManageSection(
            ClimateProdGenerateSessionDataForView cpgSess,
            Map<String, String> cpgMsg) {

        if (cpgSess != null) {
            /*
             * Reset and update basic session info for a different session.
             */
            String sid = cpgSess.getCpg_session_id();
            if (!currentSession.equals(sid)) {

                // Reset to defaults.
                resetManageSectionHeader();

                currentSession = sid;

                String runType = getRunType(cpgSess);
                currentType.setText(runType);

                PeriodType prdTyp = cpgSess.getProd_type();
                currentProductType.setText(prdTyp.getPeriodDescriptor());

                String startTime = cpgSess.getStart_at().toLocalDateTime()
                        .toString();
                startedLbl.setText(
                        startTime.substring(0, startTime.length() - 4));
                manageSessionGrp.layout(true, true);

            }

            // Update status for current session.
            boolean updateProgressOnly = false;

            SessionState prdState = cpgSess.getState();
            StateStatus prdStatus = cpgSess.getStateStatus();

            if (cpgMsg != null) {
                if (cpgMsg.get(CLIMATE_MESSAGE_KEYS[9]) != null) {
                    updateProgressOnly = true;
                } else {
                    prdState = SessionState
                            .valueOf(cpgMsg.get(CLIMATE_MESSAGE_KEYS[1]));
                    prdStatus = new StateStatus(StateStatus.Status
                            .valueOf(cpgMsg.get(CLIMATE_MESSAGE_KEYS[2])));

                    cpgSess.setState(prdState);
                    cpgSess.setStateStatus(prdStatus);
                }
            }

            // Allow aborting of the process.
            if (prdStatus.getStatus().equals(StateStatus.Status.CANCELLED)
                    || (prdState.equals(SessionState.SENT) && prdStatus
                            .getStatus().equals(StateStatus.Status.SUCCESS))) {
                abortBtn.setEnabled(false);
            } else {
                abortBtn.setEnabled(true);
            }

            // Update state/status buttons.
            if (updateProgressOnly) {
                // Update progress.
                updateWaitStatus(cpgSess, cpgMsg);
            } else {
                // Reset
                resetManageSectionStatus();

                // Update
                switch (prdState) {

                case STARTED:
                    sessionStarted(prdStatus);
                    break;

                case CREATED:
                    sessionCreated(cpgSess, prdStatus);
                    break;

                case DISPLAY:
                    sessionInDisplay(prdStatus);
                    break;

                case DISPLAYED:
                    sessionDisplayed(prdStatus);
                    break;

                case FORMATTED:
                    sessionFormatted(cpgSess, prdStatus);
                    break;

                case REVIEW:
                case PENDING:
                case SENT:
                    sessionInReview(prdStatus);
                    break;

                default:
                    break;
                }
            }
        }

    }

    /**
     * Check if a CPG session is AUTO.
     * 
     * @param sess
     *            ClimateProdGenerateSessionDataForView
     * @return isAutoSession() True/false.
     */
    private boolean isAutoSession(ClimateProdGenerateSessionDataForView sess) {
        return (sess.getRun_type() == 1);
    }

    /**
     * Process a STARTED state for a CPG session.
     * 
     * @param prdStatus
     *            A CPG session state status
     */
    private void sessionStarted(StateStatus prdStatus) {

        // Set text & color on "CREATE" button to indicate status.
        createBtn.setText(prdStatus.getStatus().toString());

        if (prdStatus.getStatus().equals(StateStatus.Status.WORKING)) {
            createBtn.setBackground(colorYellow);
            createBtn.setText(WAITING);
            createBtn.setCursor(waitCursor);
        } else if (prdStatus.getStatus().equals(StateStatus.Status.SUCCESS)) {
            createBtn.setBackground(colorGreen);
        } else {
            createBtn.setBackground(colorRed);
        }
    }

    /**
     * Process a CREATED state for a CPG session.
     * 
     * @param sess
     *            ClimateProdGenerateSessionDataForView
     * @param prdStatus
     *            A CPG session state status
     */
    private void sessionCreated(ClimateProdGenerateSessionDataForView sess,
            StateStatus prdStatus) {

        // "CREATE" completed & move to "DISPLAY".
        createBtn.setText(prdStatus.getStatus().toString());
        createBtn.setBackground(colorGreen);
        createBtn.setEnabled(false);

        /*
         * Enable "DISPLAY" button and update text & color on "DISPLAY" button
         * to indicate status
         */
        boolean isAutoSession = isAutoSession(sess);

        displayBtn.setEnabled(true);
        displayBtn.setBackground(colorYellow);
        displayBtn.setText(READY);
        displayBtn.setCursor(null);

        if (prdStatus.getStatus().equals(StateStatus.Status.WORKING)
                || prdStatus.getStatus().equals(StateStatus.Status.SUCCESS)) {
            if (isAutoSession) {
                displayBtn.setCursor(waitCursor);
            }
        } else {
            // Failed/Cancelled/Unknown status.
            createBtn.setBackground(colorRed);
            displayBtn.setEnabled(false);
        }
    }

    /**
     * Process a DISPLAY state for a CPG session.
     * 
     * @param prdStatus
     *            A CPG session state status
     */
    private void sessionInDisplay(StateStatus prdStatus) {

        // "CREATE" completed & current in "DISPLAY".
        createBtn.setBackground(colorGreen);
        createBtn.setText(StateStatus.Status.SUCCESS.toString());
        createBtn.setEnabled(false);

        // Update text & color on "DISPLAY" button to indicate status
        displayBtn.setEnabled(true);
        displayBtn.setText(prdStatus.getStatus().toString());
        displayBtn.setCursor(null);

        if (prdStatus.getStatus().equals(StateStatus.Status.WORKING)) {
            displayBtn.setBackground(colorYellow);
            displayBtn.setCursor(waitCursor);
        } else if (prdStatus.getStatus().equals(StateStatus.Status.SUCCESS)) {
            displayBtn.setBackground(colorGreen);
            displayBtn.setText(READY);
        } else {
            // Failed/Cancelled/Unknown status.
            displayBtn.setBackground(colorRed);
            displayBtn.setEnabled(false);
        }

    }

    /**
     * Process a DISPLAYED state for a CPG session.
     * 
     * @param prdStatus
     *            A CPG session state status
     */
    private void sessionDisplayed(StateStatus prdStatus) {

        // "CREATE" & "DISPLAY" completed & current in "FORMAT".
        createBtn.setBackground(colorGreen);
        createBtn.setText(StateStatus.Status.SUCCESS.toString());
        createBtn.setEnabled(false);

        displayBtn.setBackground(colorGreen);
        displayBtn.setText(prdStatus.getStatus().toString());
        if (prdStatus.getStatus().equals(StateStatus.Status.CANCELLED)) {
            displayBtn.setEnabled(false);
        } else {
            displayBtn.setEnabled(true);
        }

        // Update text & color on "FORMAT" button to indicate status
        if (prdStatus.getStatus().equals(StateStatus.Status.WORKING)
                || prdStatus.getStatus().equals(StateStatus.Status.SUCCESS)) {
            formatBtn.setEnabled(true);
            formatBtn.setBackground(colorYellow);
            formatBtn.setText(WAITING);
            formatBtn.setCursor(waitCursor);
        } else {
            // Failed/Cancelled/Unknown status.
            displayBtn.setBackground(colorRed);
            displayBtn.setText(prdStatus.getStatus().toString());
            formatBtn.setEnabled(false);
        }

    }

    /**
     * Process a FORMATTED state for a CPG session.
     * 
     * @param sess
     *            ClimateProdGenerateSessionDataForView
     * @param prdStatus
     *            A CPG session state status
     */
    private void sessionFormatted(ClimateProdGenerateSessionDataForView sess,
            StateStatus prdStatus) {

        // "CREATE", "DISPLAY" & "FORMAT" completed, move to "REVIEW".
        createBtn.setBackground(colorGreen);
        createBtn.setText(StateStatus.Status.SUCCESS.toString());
        createBtn.setEnabled(false);

        displayBtn.setBackground(colorGreen);
        displayBtn.setText(StateStatus.Status.SUCCESS.toString());
        if (prdStatus.getStatus().equals(StateStatus.Status.CANCELLED)) {
            displayBtn.setEnabled(false);
        } else {
            displayBtn.setEnabled(true);
        }

        formatBtn.setBackground(colorGreen);
        formatBtn.setText(prdStatus.getStatus().toString());
        formatBtn.setEnabled(false);

        // Enable "REVIEW" button & update color on it to indicate status.
        boolean isAutoSession = isAutoSession(sess);

        reviewNWRBtn.setBackground(colorYellow);
        reviewNWRBtn.setEnabled(true);
        reviewNWRBtn.setCursor(null);
        reviewNWRBtn.setText(REVIEW_NWR);

        reviewNWWSBtn.setBackground(colorYellow);
        reviewNWWSBtn.setEnabled(true);
        reviewNWWSBtn.setCursor(null);
        reviewNWWSBtn.setText(REVIEW_NWWS);

        if (prdStatus.getStatus().equals(StateStatus.Status.WORKING)
                || prdStatus.getStatus().equals(StateStatus.Status.SUCCESS)) {
            if (isAutoSession) {
                reviewNWRBtn.setCursor(waitCursor);
                reviewNWWSBtn.setCursor(waitCursor);
            }
        } else {
            // Failed/Cancelled/Unknown status.
            formatBtn.setBackground(colorRed);
            reviewNWRBtn.setEnabled(false);
            reviewNWWSBtn.setEnabled(false);
            reviewNWRBtn.setBackground(colorDarkGrey);
            reviewNWWSBtn.setBackground(colorDarkGrey);
        }

        // Check if NWR or NWWS products are generated.
        ClimateProdGenerateSessionDataForView curSess = retrieveCPGSession(
                currentSession);
        disableProdReview(curSess);
    }

    /**
     * Process a REVIEW state for a CPG session.
     * 
     * Note - At review stage, the detailed status is reflected in
     * ProductSetStatus. The StateStatus is always "SUCCESS". StateStatus.
     * 
     * @param prdStatus
     *            A CPG session state status
     */
    private void sessionInReview(StateStatus prdStatus) {

        // "CREATE", "DISPLAY","FORMAT" completed.
        createBtn.setBackground(colorGreen);
        createBtn.setText(StateStatus.Status.SUCCESS.toString());
        createBtn.setEnabled(false);

        displayBtn.setBackground(colorGreen);
        displayBtn.setText(StateStatus.Status.SUCCESS.toString());
        if (prdStatus.getStatus().equals(StateStatus.Status.CANCELLED)) {
            displayBtn.setEnabled(false);
        } else {
            displayBtn.setEnabled(true);
        }

        formatBtn.setBackground(colorGreen);
        formatBtn.setText(StateStatus.Status.SUCCESS.toString());
        formatBtn.setEnabled(false);

        // Still allow review & re-send?
        reviewNWRBtn.setEnabled(true);
        reviewNWWSBtn.setEnabled(true);

        // Update review/send buttons based on session status
        if (!(prdStatus.getStatus().equals(StateStatus.Status.WORKING))
                && !(prdStatus.getStatus()
                        .equals(StateStatus.Status.SUCCESS))) {
            // Failed/Cancelled/Error/Unknown status.
            reviewNWRBtn.setText(REVIEW_NWR);
            reviewNWRBtn.setEnabled(false);
            reviewNWWSBtn.setText(REVIEW_NWWS);
            reviewNWWSBtn.setEnabled(false);
            reviewNWRBtn.setBackground(colorRed);
            reviewNWWSBtn.setBackground(colorRed);

            sendNWRBtn.setText(prdStatus.getStatus().toString());
            sendNWWSBtn.setText(prdStatus.getStatus().toString());
        } else {

            // Retrieve the CPG session to find status for NWR and NWWS.
            ClimateProdGenerateSessionDataForView sess = retrieveCPGSession(
                    currentSession);
            ProductSetStatus nwrSetStatus = sess.getProd_data()
                    .getProductSetLevelStatus(ClimateProductType.NWR);
            ProductSetStatus nwwsSetStatus = sess.getProd_data()
                    .getProductSetLevelStatus(ClimateProductType.NWWS);

            /*
             * Update text/color for NWR buttons to indicate status. For NWR,
             * always ask user to check NWR Waves browser since at this point
             * the products are already out of the hand of Climate Application.
             */
            sessionSent(reviewNWRBtn, sendNWRBtn, nwrSetStatus);
            if (nwrSetStatus != ProductSetStatus.FATAL_ERROR
                    && nwrSetStatus != ProductSetStatus.HAS_ERROR) {
                sendNWRBtn.setText(SEE_NWRWAVES);
            }

            // Update text/color for NWWS buttons to indicate status.
            sessionSent(reviewNWWSBtn, sendNWWSBtn, nwwsSetStatus);

            // Check if review are needed.
            disableProdReview(sess);
        }
    }

    /**
     * Process ProductSetStatus for a CPG session.
     * 
     * @param reviewBtn
     *            Button for review action
     * @param sendBtn
     *            Button to show sent status
     * @param prdStatus
     *            A ProductSetStatus status for either NWR or NWWS
     */
    private void sessionSent(Button reviewBtn, Button sendBtn,
            ProductSetStatus prdStatus) {

        // Update color/text/cursor for the buttons.
        reviewBtn.setBackground(colorYellow);
        reviewBtn.setCursor(null);
        sendBtn.setText(prdStatus.toString());

        if (reviewBtn == reviewNWRBtn) {
            reviewBtn.setText(REVIEW_NWR);
        } else if (reviewBtn == reviewNWWSBtn) {
            reviewBtn.setText(REVIEW_NWWS);
        }

        // Set color/text/ for the buttons based on status.
        switch (prdStatus) {
        case MODIFIED:
        case PENDING:
            sendBtn.setBackground(colorYellow);
            break;

        case FATAL_ERROR:
        case HAS_ERROR:
            sendBtn.setBackground(colorRed);
            sendBtn.setText(ERROR);
            break;

        case SENT:
        case DELETED:
            reviewBtn.setBackground(colorGreen);
            sendBtn.setBackground(colorGreen);
            break;

        case UNKNOWN:
            sendBtn.setBackground(colorRed);
            break;

        default:
            break;

        }

    }

    /**
     * Update waiting time status for DISPLAY and REVIEW.
     * 
     * @param cpgSess
     *            ClimateProdGenerateSessionDataForView
     * @param cpgMsg
     *            Map<String, String> status message
     */
    private void updateWaitStatus(ClimateProdGenerateSessionDataForView cpgSess,
            Map<String, String> cpgMsg) {

        boolean isAutoSession = isAutoSession(cpgSess);

        // Update progress message - time counting down.
        if (isAutoSession) {

            // Progress message only comes for CREATED/FORMATTED state
            if (cpgMsg != null) {
                String prdState = cpgMsg.get(CLIMATE_MESSAGE_KEYS[1]);

                if (prdState.equals(SessionState.CREATED.toString())
                        || prdState.equals(SessionState.FORMATTED.toString())) {

                    String timerStr = getTimerString(cpgMsg);
                    if (!timerStr.equals(NA)) {
                        if (prdState.equals(SessionState.CREATED.toString())) {
                            displayBtn.setFont(size10FontBold);
                            displayBtn.setText(READY + ": " + timerStr);
                        } else if (prdState
                                .equals(SessionState.FORMATTED.toString())) {
                            reviewNWRBtn.setText(REVIEW_NWR + ": " + timerStr);
                            reviewNWWSBtn
                                    .setText(REVIEW_NWWS + ": " + timerStr);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate a minute:second string for a given time.
     * 
     * @param time
     *            total time in seconds
     * @return getTimeString() String in format of "MM:SS"
     */
    private String getTimeString(int time) {

        String outStr = "00:00";

        if (time > 0) {
            int minute = time / 60;
            int second = time - (minute * 60);

            outStr = String.format("%02d:%02d", minute, second);
        }

        return outStr;
    }

    /**
     * Create a label to indicate session state.
     * 
     * @param parent
     *            parent composite
     * @param stage
     *            String.
     * @param grdData
     *            GridData for label layout.
     */
    private void createStateLabel(Composite parent, String stage,
            GridData grdData) {

        Label sessionStageLbl = new Label(parent, SWT.NORMAL);
        sessionStageLbl.setText(stage);
        sessionStageLbl.setFont(size12Font);
        sessionStageLbl.setLayoutData(grdData);
        sessionStageLbl.setAlignment(SWT.CENTER);
    }

    /**
     * Create a button to show session status.
     * 
     * @param parent
     *            parent composite
     * @param stage
     *            Value of a StateState.
     * @param grdData
     *            GridData for button layout. Set min size in this method.
     * @return createStatusBtn() Button
     */
    private Button createStatusBtn(Composite parent, SessionStage stage,
            GridData grdData) {

        Button btn;

        btn = new Button(parent, SWT.PUSH);
        btn.setData(stage);
        btn.setFont(size12FontBold);

        GC gc = new GC(btn);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        grdData.minimumWidth = 20 * fontWidth;
        grdData.minimumHeight = (3 * fontHeight) / 2;

        btn.setLayoutData(grdData);
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Button selected = (Button) event.widget;
                SessionStage stage = (SessionStage) selected.getData();
                manageSession(stage);
            }
        });

        return btn;
    }

    /**
     * Create a composite for session state "REVIEW".
     * 
     * @param parent
     *            parent composite
     */
    private void createReviewComp(Composite parent) {

        Composite reviewComp = new Composite(parent, SWT.NONE);

        GridData reviewCompGd = new GridData(SWT.FILL, SWT.FILL, true, false, 1,
                1);
        reviewComp.setLayoutData(reviewCompGd);

        // Add buttons to review NWR and NWWS.
        GridLayout gLayout = new GridLayout(1, true);
        gLayout.verticalSpacing = 3;
        gLayout.marginHeight = 0;
        gLayout.marginWidth = 0;

        reviewComp.setLayout(gLayout);
        reviewComp.setFont(size10FontBold);

        GridData reviewGd1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        reviewNWRBtn = createStatusBtn(reviewComp, SessionStage.REVIEW_NWR,
                reviewGd1);
        reviewNWRBtn.setText(REVIEW_NWR);
        reviewNWRBtn.setFont(size10FontBold);
        reviewNWRBtn.setLayoutData(reviewGd1);

        GridData reviewGd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        reviewNWWSBtn = createStatusBtn(reviewComp, SessionStage.REVIEW_NWWS,
                reviewGd2);
        reviewNWWSBtn.setText(REVIEW_NWWS);
        reviewNWWSBtn.setFont(size10FontBold);
        reviewNWWSBtn.setLayoutData(reviewGd2);

    }

    /**
     * Create a composite for session state "SEND".
     * 
     * @param parent
     *            parent composite
     */
    private void createSendComp(Composite parent) {

        Composite sendComp = new Composite(parent, SWT.NONE);

        GridData sendCompGd = new GridData(SWT.FILL, SWT.FILL, true, false, 1,
                1);
        sendComp.setLayoutData(sendCompGd);

        // Add buttons to review NWR and NWWS.
        GridLayout gLayout = new GridLayout(1, true);
        gLayout.verticalSpacing = 3;
        gLayout.marginHeight = 0;
        gLayout.marginWidth = 0;

        sendComp.setLayout(gLayout);
        sendComp.setFont(size10FontBold);

        GridData sendGd1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        sendNWRBtn = createStatusBtn(sendComp, SessionStage.REVIEW_NWR,
                sendGd1);
        sendNWRBtn.setText("");
        sendNWRBtn.setFont(size10FontBold);
        sendNWRBtn.setLayoutData(sendGd1);

        GridData sendGd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        sendNWWSBtn = createStatusBtn(sendComp, SessionStage.REVIEW_NWWS,
                sendGd2);
        sendNWWSBtn.setText("");
        sendNWWSBtn.setFont(size10FontBold);
        sendNWWSBtn.setLayoutData(sendGd2);

    }

    /**
     * Gets the status string to describe the session state's status.
     *
     * Note: For session states starting from REVIEW (PENDING, SENT), if the
     * session status is WORKING or SUCCESS, the actual product status will be
     * indicated in ProductSetStatus for NWR & NWWS separately. Use the worst
     * status from NWR/NWWS.
     * 
     * @param cpgSession
     *            ClimateProdGenerateSessionDataForView
     */
    private String getStateStatus(
            ClimateProdGenerateSessionDataForView cpgSession) {

        /*
         * For session states starting from REVIEW (PENDING, SENT), Iif the
         * session status WORKING or SUCCESS, the actual product status will be
         * indicated in ProductSetStatus for NWR & NWWS separately. Use the
         * worst status from NWR/NWWS.
         */
        String statusStr = "";

        if (cpgSession != null) {
            statusStr = cpgSession.getStateStatus().getStatus().toString();
            if (cpgSession.getState().getValue() >= SessionState.REVIEW
                    .getValue()) {
                if (cpgSession.getStateStatus().getStatus()
                        .equals(StateStatus.Status.WORKING)
                        || cpgSession.getStateStatus().getStatus()
                                .equals(StateStatus.Status.SUCCESS)) {

                    ProductSetStatus nwrSetStatus = cpgSession.getProd_data()
                            .getProductSetLevelStatus(ClimateProductType.NWR);
                    ProductSetStatus nwwsSetStatus = cpgSession.getProd_data()
                            .getProductSetLevelStatus(ClimateProductType.NWWS);

                    if (nwrSetStatus == ProductSetStatus.SENT
                            || nwrSetStatus == ProductSetStatus.DELETED) {
                        statusStr = nwwsSetStatus.toString();
                    } else if (nwwsSetStatus == ProductSetStatus.SENT
                            || nwwsSetStatus == ProductSetStatus.DELETED) {
                        statusStr = nwrSetStatus.toString();
                    } else {
                        statusStr = (nwrSetStatus.getCode() > nwwsSetStatus
                                .getCode()) ? nwrSetStatus.toString()
                                        : nwwsSetStatus.toString();
                    }

                    // Show "ERROR" for "HAS_ERROR" and "FATAL_ERROR" status.
                    if (statusStr.contains(ERROR)) {
                        statusStr = ERROR;
                    }
                }
            }
        }

        return statusStr;

    }

    /**
     * Generate a minute:second timer string from a CPG timer message.
     * 
     * @param cpgMsg
     *            CPG message
     * @return getTimeString() String in format of "MM:SS" or "N/A"
     */
    private String getTimerString(Map<String, String> cpgMsg) {

        String outStr = NA;
        String totalTime = cpgMsg.get(CLIMATE_MESSAGE_KEYS[7]);
        String timePassed = cpgMsg.get(CLIMATE_MESSAGE_KEYS[8]);
        if (totalTime != null && timePassed != null) {
            try {
                int total = Integer.parseInt(totalTime);
                int pass = Integer.parseInt(timePassed);

                int timeLeft = total - pass;

                if (timeLeft > 0) {
                    outStr = getTimeString(timeLeft);
                }
            } catch (NumberFormatException ee) {
                logger.warn(
                        "ClimateProdGenerationView: Improper progress report received for "
                                + cpgMsg.get(CLIMATE_MESSAGE_KEYS[0]));
            }
        }

        return outStr;
    }

    /**
     * Disable review for NWR or NWWS if no products are generated.
     * 
     * @param cpgSess
     *            ClimateProdGenerateSessionDataForView
     */
    private void disableProdReview(
            ClimateProdGenerateSessionDataForView curSess) {

        if (curSess.getProd_data().getNwrProducts() == null
                || curSess.getProd_data().getNwrProducts().isEmpty()) {
            reviewNWRBtn.setEnabled(false);
            reviewNWRBtn.setBackground(colorGreen);

            sendNWRBtn.setText("No Products");
            sendNWRBtn.setBackground(colorGreen);
        }

        if (curSess.getProd_data().getNwwsProducts() == null
                || curSess.getProd_data().getNwwsProducts().isEmpty()) {
            reviewNWWSBtn.setEnabled(false);
            reviewNWWSBtn.setBackground(colorGreen);

            sendNWWSBtn.setText("No Products");
            sendNWWSBtn.setBackground(colorGreen);
        }
    }

}