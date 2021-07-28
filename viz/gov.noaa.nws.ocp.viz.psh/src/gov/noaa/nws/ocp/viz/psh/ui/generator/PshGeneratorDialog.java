/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.localization.psh.PshBasin;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshCounties;
import gov.noaa.nws.ocp.common.localization.psh.PshForecasters;
import gov.noaa.nws.ocp.viz.psh.PshPrintUtil;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshEffectsTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshFloodingTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshMarineTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshMetarTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshNonMetarTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshRainfallTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTornadoesTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshWaterLevelTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshCitiesSetupDialog;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshCountySetupDialog;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshForecasterSetupDialog;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshMarineSetupDialog;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshMetarSetupDialog;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshNonMetarSetupDialog;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshSetupConfigDialog;

/**
 * The PSH Report Generator dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 31, 2017 #34810      wpaintsil   Initial creation.
 * Jun 16, 2017 #34810      wpaintsil   Refactoring; creation of PshTabComp and TabType classes.
 * Jun 21, 2017 #34810      wpaintsil   Separate control creation for each tab
 *                                      into its own class and in a new package for better 
 *                                      organization and encapsulation.
 * Jul 05, 2017 #35463      wpaintsil   Beginnings of PSH GUI redesign.
 * Jul 06, 2017 #35269      jwu         Update storm list from localization.
 * Sep 14, 2017 #36920      astrakovsky Implemented print function.
 * Sep 21, 2016 #37917      wpaintsil   Added forecaster warning, complete Included Counties 
 *                                      functionality, and banner image.
 *                                      Retrieve/Save pshDate back-end integration.
 * Sep 28, 2016 #38374      wpaintsil   Use a dialog instead of a menu for county selection.
 * Nov 08, 2017 #40423      jwu         Replace tide/surge with water level.
 * Nov 20, 2017 #39868      wpaintsil   Add warning to user to select included counties.
 * Nov 20, 2017 #40417      astrakovsky Added historical report viewer to menu.
 * Dec 06, 2017 #41620      wpaintsil   Add import option to the File menu.
 * Dec 11, 2017 #41998      jwu         Use localization access control file in base/roles.
 * JUN 09, 2021  DCS21225   wkwock      Use storm names from StormNames.py
 * Jun 18, 2021 DCS22100    mporricelli Add checks to alert user that their
 *                                      changes have not been saved
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 * 
 */
public class PshGeneratorDialog extends CaveJFACEDialog implements IPshData {

    /**
     * A Timer object used to create a marquee animation
     */
    private Timer marquee;

    /**
     * A TimeTask object used in conjunction with a Timer to create a marquee
     * animation
     */
    private TimerTask marqueeTask;

    /**
     * A label that will display a marquee animation of selected counties
     */
    private Label countiesLabel;

    private int curTab = 0;

    private int basinIdx;

    private int yearIdx;

    private int stormIdx;

    private int fcstrIdx;

    /**
     * The tab folder holding each tab
     */
    private TabFolder tabFolder;

    /**
     * Current PshData object.
     */
    private PshData pshData;

    private Combo basinCombo;

    private Label yearLbl;

    private Combo yearCombo;

    private Combo stormCombo;

    private Combo forecasterCombo;

    private PshMetarTabComp metarTab;

    private PshNonMetarTabComp nonMetarTab;

    private PshMarineTabComp marineTab;

    private PshRainfallTabComp rainfallTab;

    private PshFloodingTabComp floodingTab;

    private PshWaterLevelTabComp waterLevelTab;

    private PshTornadoesTabComp tornadoesTab;

    private PshEffectsTabComp effectsTab;

    private Composite stackComposite;

    private StackLayout stackLayout;

    private MenuItem viewSendMenuItem;

    private MenuItem printMenuItem;

    private MenuItem viewHistoricalMenuItem;

    private Button countiesButton;

    /**
     * Dialogs for each setup step.
     */
    private PshSetupConfigDialog setupConfigDialog = null;

    private PshCountySetupDialog countySetupDialog = null;

    private PshForecasterSetupDialog forecasterSetupDialog = null;

    private PshCitiesSetupDialog citiesSetupDialog = null;

    private PshMetarSetupDialog metarSetupDialog = null;

    private PshNonMetarSetupDialog nonMetarSetupDialog = null;

    private PshMarineSetupDialog marineSetupDialog = null;

    private Composite hiderComposite;

    private Label bannerLabel;

    private Image bannerImage;

    private Font messageFont;

    private Image scaledBannerImage;

    private MenuItem importProductXML;

    /**
     * Title string used for the window title and the tabs
     */
    public static final String PSH_TITLE = "POST TROPICAL CYCLONE REPORT GENERATOR";

    private Map<String, Map<Long, List<String>>> stormNames = null;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshGeneratorDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);

        pshData = new PshData();
    }

    /**
     * Override the method from the parent Dialog class to implement the GUI.
     */
    @Override
    public Control createDialogArea(Composite parent) {

        Composite top = (Composite) super.createDialogArea(parent);

        messageFont = PshUtil.createFont(15, SWT.NORMAL);

        /*
         * Create the main layout for the shell.
         */
        GridLayout mainLayout = new GridLayout(1, false);
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.CENTER, SWT.BEGINNING, true,
                false);
        top.setLayoutData(mainLayoutData);

        createMenus();
        createTopPanel(top);

        Label separator = new Label(top, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createTabPanel(top);

        Shell shell = getShell();
        /*
         * Sets dialog title
         */
        shell.setText(PSH_TITLE);

        // Dispose fonts
        top.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (messageFont != null) {
                    messageFont.dispose();
                }
            }
        });

        return top;
    }

    /**
     * Create File, Setup, and Help menus.
     */
    private void createMenus() {
        Shell shell = getShell();

        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuHeader.setText("File");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileMenuHeader.setMenu(fileMenu);

        viewSendMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        viewSendMenuItem.setText("View/Send PSH");

        viewSendMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (checkEditStatusOk()) {
                    new PshViewSendDialog(getShell(), pshData).open();
                }
            }
        });

        // disable until storm and forecaster are selected
        viewSendMenuItem.setEnabled(false);

        printMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        printMenuItem.setText("Print Report");
        printMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (checkEditStatusOk()) {

                    String pshReport = PshUtil.buildPshReport(pshData);

                    PshPrintUtil.getPshPrinter().printInput(pshReport);
                }

            }
        });

        // disable until storm and forecaster are selected
        printMenuItem.setEnabled(false);

        // Import PSH product form XML
        importProductXML = new MenuItem(fileMenu, SWT.PUSH);
        importProductXML.setText("Import Product File");

        importProductXML.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openXmlFile();
            }
        });

        // View historical PSH text product file
        viewHistoricalMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        viewHistoricalMenuItem.setText("View Historical Reports");

        viewHistoricalMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                new PshViewHistoricalDialog(getShell()).open();
            }
        });

        // Exit
        MenuItem closeMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        closeMenuItem.setText("Exit");
        closeMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (checkEditStatusOk()) {
                    PshGeneratorDialog.this.close();
                }
            }
        });

        // Setup menu
        MenuItem setupMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        setupMenuItem.setText("Setup");

        Menu setupMenu = new Menu(menuBar);
        setupMenuItem.setMenu(setupMenu);

        // Program Configuration menu item
        MenuItem configMenuItem = new MenuItem(setupMenu, SWT.NONE);
        configMenuItem.setText("Program Configuration");
        configMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openSetupConfigDialog();
            }
        });

        // Setup Counties menu item
        MenuItem countiesMenuItem = new MenuItem(setupMenu, SWT.NONE);
        countiesMenuItem.setText("Counties");
        countiesMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openCountySetupDialog();

            }
        });

        MenuItem forecastersMenuItem = new MenuItem(setupMenu, SWT.NONE);
        forecastersMenuItem.setText("Forecasters");
        forecastersMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openForecasterSetupDialog();

            }
        });

        MenuItem citiesMenuItem = new MenuItem(setupMenu, SWT.NONE);
        citiesMenuItem.setText("Cities/Water Level Stations");
        citiesMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openCitiesSetupDialog();

            }
        });

        MenuItem metarMenuItem = new MenuItem(setupMenu, SWT.NONE);
        metarMenuItem.setText("Metar Stations");
        metarMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openMetarSetupDialog();

            }
        });

        MenuItem nonMetarMenuItem = new MenuItem(setupMenu, SWT.NONE);
        nonMetarMenuItem.setText("Non-Metar Stations");
        nonMetarMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openNonMetarSetupDialog();

            }
        });

        MenuItem marineMenuItem = new MenuItem(setupMenu, SWT.NONE);
        marineMenuItem.setText("Marine Stations");
        marineMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openMarineSetupDialog();

            }
        });

        // Help menu
        MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuItem.setText("&Help");

        Menu helpMenu = new Menu(menuBar);
        helpMenuItem.setMenu(helpMenu);

        // Handbook menu item
        MenuItem handbookMenuItem = new MenuItem(helpMenu, SWT.NONE);
        handbookMenuItem.setText("Handbook");
        handbookMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PshUtil.displayHandbook();
            }
        });

        MenuItem aboutMenuItem = new MenuItem(helpMenu, SWT.NONE);
        aboutMenuItem.setText("About");
        aboutMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // TODO: Open an "About" window with developer and version info.
            }
        });

        shell.setMenuBar(menuBar);
    }

    /**
     * Create the section above the tabs.
     * 
     * @param parent
     */
    private void createTopPanel(Composite parent) {
        Composite topComp = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(6, false);
        GridData topData = new GridData(SWT.LEFT, SWT.TOP, true, true);
        topLayout.horizontalSpacing = 5;
        topLayout.marginWidth = 5;
        topLayout.marginHeight = 0;
        topComp.setLayout(topLayout);
        topComp.setLayoutData(topData);

        // Basin field
        Composite basinComp = new Composite(topComp, SWT.NONE);
        RowLayout basinLayout = new RowLayout(SWT.HORIZONTAL);
        basinLayout.center = true;
        basinLayout.marginBottom = 10;
        basinComp.setLayout(basinLayout);

        new Label(basinComp, SWT.NORMAL).setText("Basin:");

        basinCombo = new Combo(basinComp, SWT.DROP_DOWN | SWT.READ_ONLY);

        for (PshBasin basin : PshBasin.values()) {
            basinCombo.add(basin.getName());
        }

        basinCombo.select(0);
        pshData.setBasinName(basinCombo.getText());
        basinCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkEditStatusOk()) {
                    updateYearList();
                    updateStormList();
                    pshData.setBasinName(basinCombo.getText());
                    showTabContent();
                } else {
                    /*
                     * Do not continue switch to newly selected basin. Change
                     * menu back to previously selected basin.
                     */
                    basinCombo.select(basinIdx);
                }
            }
        });

        // Year field
        Composite yearComp = new Composite(topComp, SWT.NONE);
        RowLayout yearLayout = new RowLayout(SWT.HORIZONTAL);
        yearLayout.center = true;
        yearLayout.marginBottom = 10;
        yearComp.setLayout(yearLayout);

        yearLbl = new Label(yearComp, SWT.NORMAL);
        yearLbl.setText("Year:");

        yearCombo = new Combo(yearComp, SWT.READ_ONLY);

        String[] comboYears = new String[12];
        int currentYear = TimeUtil.newCalendar().get(Calendar.YEAR);
        int ii = 0;
        for (int jj = currentYear + 1; jj >= currentYear - 10; jj--) {
            comboYears[ii] = String.valueOf(jj);
            ii++;
        }
        yearCombo.setItems(comboYears);
        yearCombo.select(1);
        pshData.setYear(Integer.valueOf(yearCombo.getText()));
        yearCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkEditStatusOk()) {
                    updateStormList();
                    pshData.setYear(Integer.valueOf(yearCombo.getText()));
                    showTabContent();
                } else {
                    /*
                     * Do not continue switch to newly selected year. Change
                     * menu back to previously selected year.
                     */
                    yearCombo.select(yearIdx);
                }
            }
        });

        // Storm Name field
        Composite stormNameComp = new Composite(topComp, SWT.NONE);
        RowLayout stormNameLayout = new RowLayout(SWT.HORIZONTAL);
        stormNameLayout.center = true;
        stormNameLayout.marginBottom = 10;
        stormNameComp.setLayout(stormNameLayout);

        new Label(stormNameComp, SWT.NORMAL).setText("Storm Name:");

        stormCombo = new Combo(stormNameComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        stormCombo.setLayoutData(new RowData(105, SWT.DEFAULT));

        updateStormList();
        stormCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkEditStatusOk()) {
                    pshData.setStormName(stormCombo.getText());
                    showTabContent();
                } else {
                    /*
                     * Do not continue switch to newly selected storm. Change
                     * menu back to previously selected storm.
                     */
                    stormCombo.select(stormIdx);
                }
            }
        });

        // Forecaster field
        Composite forecasterComp = new Composite(topComp, SWT.NONE);
        RowLayout forecasterLayout = new RowLayout(SWT.HORIZONTAL);
        forecasterLayout.center = true;
        forecasterLayout.marginBottom = 10;
        forecasterComp.setLayout(forecasterLayout);

        new Label(forecasterComp, SWT.NORMAL).setText("Forecaster:");

        forecasterCombo = new Combo(forecasterComp,
                SWT.DROP_DOWN | SWT.READ_ONLY);
        PshForecasters fcstrs = PshConfigurationManager.getInstance()
                .getForecasters();

        if (fcstrs == null || fcstrs.getForecasters() == null
                || fcstrs.getForecasters().isEmpty()) {
            forecasterCombo.add("[empty]");
        } else {
            for (String fcstr : fcstrs.getForecasters()) {
                forecasterCombo.add(fcstr);
            }
        }

        forecasterCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkEditStatusOk()) {
                    pshData.setForecaster(forecasterCombo.getText());
                    showTabContent();
                } else {
                    /*
                     * Do not continue switch to newly selected forecaster.
                     * Change menu back to previously selected forecaster.
                     */
                    forecasterCombo.select(fcstrIdx);
                }
            }
        });

        // Included counties field
        Composite inclCountiesComp = new Composite(topComp, SWT.NONE);
        RowLayout inclCountiesLayout = new RowLayout(SWT.HORIZONTAL);
        inclCountiesLayout.center = true;
        inclCountiesComp.setLayout(inclCountiesLayout);

        countiesButton = new Button(inclCountiesComp, SWT.PUSH);
        countiesButton.setText("Included\nCounties");

        PshCounties counties = PshConfigurationManager.getInstance()
                .getCounties();

        countiesLabel = new Label(inclCountiesComp, SWT.BORDER | SWT.CENTER);
        countiesLabel.setLayoutData(new RowData(160, SWT.DEFAULT));
        countiesLabel.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        createCountiesMarquee(parent);

        // Change the text in the countiesLabel whenever a county is selected.
        countiesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                new PshCountySelectionDialog(getShell(), counties.getCounties())
                        .open();

            }
        });

        countiesButton.setEnabled(false);

        // Backup site field
        Composite backupComp = new Composite(topComp, SWT.NONE);
        RowLayout backupLayout = new RowLayout(SWT.HORIZONTAL);
        backupLayout.center = true;
        backupComp.setLayout(backupLayout);
        new Label(backupComp, SWT.NORMAL).setText("Backup Site:");

        Text backupTextField = new Text(backupComp, SWT.BORDER | SWT.CENTER);
        backupTextField.setText("Normal");
        backupTextField.setLayoutData(new RowData(50, SWT.DEFAULT));
        backupTextField.setEnabled(false);

    }

    /**
     * Check the current tab's editing state to prevent
     * loss of user input
     *
     * @return
     */
    protected boolean checkEditStatusOk() {
        int selectedIndex = tabFolder.getSelectionIndex();
        if (selectedIndex >= 0) {
            PshTabComp tabComp = (PshTabComp) tabFolder.getItem(selectedIndex).getControl();
            return tabComp.checkEditStatusOk();
        }
        return false;
    }

    /**
     * Create a marquee for the selected county(ies) as seen in the legacy GUI.
     * 
     * @param parent
     */
    private void createCountiesMarquee(Composite parent) {
        marquee = new Timer();

        marqueeTask = new TimerTask() {
            @Override
            public void run() {
                if (parent.isDisposed() == true) {
                    if (marquee != null) {
                        marquee.cancel();
                    }
                    return;
                }
                parent.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (marquee == null
                                || countiesLabel.isDisposed() == true) {
                            if (marquee != null) {
                                marquee.cancel();
                            }
                            return;
                        }

                        // Move the text from right to left
                        if (!countiesLabel.getText().isEmpty()) {
                            countiesLabel.setText(countiesLabel.getText()
                                    .substring(1)
                                    + countiesLabel.getText().charAt(0));
                        }

                    }
                });
            }
        };

        marquee.schedule(marqueeTask, 0, 200);

    }

    /**
     * Create tabs
     * 
     * @param parent
     */
    private void createTabPanel(Composite parent) {
        stackComposite = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        stackComposite.setLayout(stackLayout);
        stackComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tabFolder = new TabFolder(stackComposite, SWT.TOP | SWT.BORDER);

        tabFolder.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));

        metarTab = new PshMetarTabComp(this, tabFolder);
        nonMetarTab = new PshNonMetarTabComp(this, tabFolder);
        marineTab = new PshMarineTabComp(this, tabFolder);
        rainfallTab = new PshRainfallTabComp(this, tabFolder);
        floodingTab = new PshFloodingTabComp(this, tabFolder);
        waterLevelTab = new PshWaterLevelTabComp(this, tabFolder);
        tornadoesTab = new PshTornadoesTabComp(this, tabFolder);
        effectsTab = new PshEffectsTabComp(this, tabFolder);

        curTab = tabFolder.getSelectionIndex();

        // Check row editing status when switching tabs.
        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int newTab = tabFolder.getSelectionIndex();
                tabFolder.setSelection(curTab);
                if (checkEditStatusOk()) {
                    tabFolder.setSelection(newTab);
                    curTab = newTab;
                }
            }
        });

        // Create a composite on top of the tab Folder to hide it.
        hiderComposite = new Composite(stackComposite, SWT.NONE);
        hiderComposite.setLayout(new GridLayout(1, false));
        hiderComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        stackLayout.topControl = hiderComposite;

        // Explain why the tabs are unavailable.
        Label unavailableLabel = new Label(hiderComposite, SWT.NONE);
        unavailableLabel.setText(
                "Select a Storm Name and Forecaster to generate a PSH report.");
        unavailableLabel.setFont(messageFont);
        unavailableLabel
                .setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));

        // Show a decorative banner image
        bannerLabel = new Label(hiderComposite, SWT.NORMAL);
        bannerImage = PshUtil.createImage("psh_storm_isabel.jpg");
        int width = bannerImage.getBounds().width;
        int height = bannerImage.getBounds().height;
        scaledBannerImage = new Image(Display.getCurrent(),
                bannerImage.getImageData().scaledTo((int) (width * 0.5),
                        (int) (height * 0.45)));
        bannerLabel.setImage(scaledBannerImage);
        bannerLabel
                .setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));

        // dispose banner image
        bannerLabel.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (bannerImage != null && !bannerImage.isDisposed()) {
                    bannerImage.dispose();
                    bannerImage = null;
                }

                if (scaledBannerImage != null
                        && !scaledBannerImage.isDisposed()) {
                    scaledBannerImage.dispose();
                    scaledBannerImage = null;
                }
            }

        });

    }

    /**
     * Show or hide tabs. Show tabs and enable View/Send or Print PSH if the
     * appropriate options are selected. Hide/disable them otherwise.
     * 
     */
    private void showTabContent() {
        boolean show = stormCombo != null && forecasterCombo != null
                && forecasterCombo.getSelectionIndex() >= 0
                && stormCombo.getSelectionIndex() >= 0;

        if (stackLayout != null && tabFolder != null
                && stackComposite != null) {
            if (show) {
                basinIdx = basinCombo.getSelectionIndex();
                yearIdx = yearCombo.getSelectionIndex();
                stormIdx = stormCombo.getSelectionIndex();
                fcstrIdx = forecasterCombo.getSelectionIndex();
                stackLayout.topControl = tabFolder;

                countiesButton.setEnabled(true);
                clearTabs();
                populateTabs();

            } else {
                countiesButton.setEnabled(false);
                countiesLabel.setText("");
                stackLayout.topControl = hiderComposite;
            }
            stackComposite.layout();
        }
        viewSendMenuItem.setEnabled(show);
        printMenuItem.setEnabled(show);
    }

    /**
     * Fill each tab with appropriate data from the database.
     */
    private void populateTabs() {
        // retrieve PSH data from the database
        PshData retrievedData = PshUtil.retrievePshData(basinCombo.getText(),
                PshUtil.parseInt(yearCombo.getText()), stormCombo.getText());

        if (retrievedData != null) {
            pshData = retrievedData;
        } else {
            pshData = new PshData();
        }

        pshData.setBasinName(basinCombo.getText());
        pshData.setYear(PshUtil.parseInt(yearCombo.getText()));
        pshData.setStormName(stormCombo.getText());

        metarTab.setDataList();
        nonMetarTab.setDataList();
        marineTab.setDataList();
        rainfallTab.setDataList();
        floodingTab.setDataList();
        waterLevelTab.setDataList();
        tornadoesTab.setDataList();
        effectsTab.setDataList();

        // Warn the user if the current storm data has been edited by
        // another forecaster.
        if (pshData.getForecaster() != null
                && !pshData.getForecaster().equals(forecasterCombo.getText())) {
            new MessageDialog(getShell(), "", null, "The report for the storm, "
                    + pshData.getStormName()
                    + ", has been already created or previously edited by the forecaster, "
                    + pshData.getForecaster() + ".", MessageDialog.WARNING,
                    new String[] { "OK" }, 0).open();

        }

        // Any change to the report will be under the new forecaster.
        pshData.setForecaster(forecasterCombo.getText());

        setCountiesText(pshData.getIncludedCounties());
        if (pshData.getIncludedCounties().isEmpty()) {
            boolean open = new MessageDialog(getShell(), "County Warning", null,
                    "Please select included counties now.",
                    MessageDialog.WARNING,
                    new String[] { IDialogConstants.OK_LABEL, "Later" }, 1)
                            .open() == MessageDialog.OK;

            if (open) {
                new PshCountySelectionDialog(getShell(), PshConfigurationManager
                        .getInstance().getCounties().getCounties()).open();
            }
        }

    }

    /**
     * Set the text of the counties label.
     * 
     * @param countiesList
     */
    private void setCountiesText(List<String> countiesList) {
        StringBuilder labelString = new StringBuilder();
        for (String countyString : countiesList) {
            labelString.append(countyString).append("...");
        }
        countiesLabel.setText(labelString.toString());
    }

    /**
     * Clear all tab tables.
     */
    private void clearTabs() {
        metarTab.clearTable();
        nonMetarTab.clearTable();
        marineTab.clearTable();
        rainfallTab.clearTable();
        floodingTab.clearTable();
        waterLevelTab.clearTable();
        tornadoesTab.clearTable();
        effectsTab.clearTable();
    }

    /**
     * @return pshData
     */
    @Override
    public PshData getPshData() {
        return pshData;
    }

    /**
     * Set pshData
     * 
     * @param pshData
     */
    @Override
    public void setPshData(PshData pshData) {
        this.pshData = pshData;
    }

    /**
     * Remove the default OK/Cancel buttons.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        GridLayout layout = (GridLayout) parent.getLayout();
        layout.marginHeight = 0;
    }

    /**
     * Open Program Configuration Menu.
     */
    private void openSetupConfigDialog() {
        if (setupConfigDialog == null || !setupConfigDialog.isOpen()) {
            setupConfigDialog = new PshSetupConfigDialog(getShell());
        }
        setupConfigDialog.open();
    }

    /**
     * Open County Setup Menu.
     */
    private void openCountySetupDialog() {
        if (countySetupDialog == null || !countySetupDialog.isOpen()) {
            countySetupDialog = new PshCountySetupDialog(getShell());
        }
        countySetupDialog.open();
    }

    /**
     * Open Forecaster Setup Menu.
     */
    private void openForecasterSetupDialog() {
        if (forecasterSetupDialog == null || !forecasterSetupDialog.isOpen()) {
            forecasterSetupDialog = new PshForecasterSetupDialog(getShell());
        }
        forecasterSetupDialog.open();
    }

    /**
     * Open Cities Setup Menu.
     */
    private void openCitiesSetupDialog() {
        if (citiesSetupDialog == null || !citiesSetupDialog.isOpen()) {
            citiesSetupDialog = new PshCitiesSetupDialog(getShell());
        }
        citiesSetupDialog.open();
    }

    /**
     * Open Metar Setup Menu.
     */
    private void openMetarSetupDialog() {
        if (metarSetupDialog == null || !metarSetupDialog.isOpen()) {
            metarSetupDialog = new PshMetarSetupDialog(getShell());
        }
        metarSetupDialog.open();
    }

    /**
     * Open Non-Metar Setup Menu.
     */
    private void openNonMetarSetupDialog() {
        if (nonMetarSetupDialog == null || !nonMetarSetupDialog.isOpen()) {
            nonMetarSetupDialog = new PshNonMetarSetupDialog(getShell());
        }
        nonMetarSetupDialog.open();
    }

    /**
     * Open Marine Setup Menu.
     */
    private void openMarineSetupDialog() {
        if (marineSetupDialog == null || !marineSetupDialog.isOpen()) {
            marineSetupDialog = new PshMarineSetupDialog(getShell());
        }
        marineSetupDialog.open();
    }

    /**
     * Make the dialog resizable.
     */
    // @Override
    // protected boolean isResizable() {
    //
    // return true;
    // }

    /**
     * Add confirm dialog to all close buttons/menu options.
     */
    @Override
    public boolean close() {
        if (checkEditStatusOk() && PshUtil.exitConfirmed(getShell()))  {
            return super.close();
        } else {
            return false;
        }
    }

    /**
     * Update the year combo base on the basin selection
     */
    private void updateYearList() {
        yearCombo.removeAll();
        PshBasin pshBasin = PshBasin.getPshBasin(basinCombo.getText());

        if (pshBasin == PshBasin.AT || pshBasin == PshBasin.EP) {
            // Atlantic and Eastern Pacific
            String[] comboYears = new String[12];
            int currentYear = TimeUtil.newCalendar().get(Calendar.YEAR);
            int ii = 0;
            // 12 years for the yearCombo
            for (int jj = currentYear + 1; jj >= currentYear - 10; jj--) {
                comboYears[ii] = String.valueOf(jj);
                ii++;
            }
            yearCombo.setItems(comboYears);
            yearCombo.select(1);
            yearLbl.setText("Year:");
        } else {
            // Central Pacific and Western Pacific
            Map<Long, List<String>> nameLists = stormNames
                    .get(pshBasin.getName());
            if (nameLists != null) {
                // use the list number from StormNames.py instead of years
                for (Long num : nameLists.keySet()) {
                    yearCombo.add(num.toString());
                }
                yearCombo.select(0);
            }

            yearLbl.setText("List:");
        }
    }

    /**
     * Update storm name list for basin/year selection.
     */
    private void updateStormList() {
        PshBasin basin = PshBasin.getPshBasin(basinCombo.getText());
        if (stormNames == null) {
            // get the storm names from StormNames.py
            stormNames = PshConfigurationManager.getInstance().readStormNames();
        }

        Long listNum = Long.parseLong(yearCombo.getText());
        if (basin == PshBasin.AT || basin == PshBasin.EP) {
            // for Atlantic and Eastern Pacific. Rotate every 6 years
            // In StormNames.py, 3 is for year 2021, 2027, etc, 4 is for year
            // 2022, 2028, etc
            listNum = (listNum - 2) % 6;
        }

        stormCombo.removeAll();

        // get storm names base on the basin name
        Map<Long, List<String>> basinStormNames = stormNames
                .get(basin.getName());

        if (basinStormNames != null) {
            // get the storm names base on the year/list selection
            List<String> names = basinStormNames.get(listNum);

            // populate the storm names to stormCombo
            if (names != null && !names.isEmpty()) {
                for (String name : names) {
                    stormCombo.add(name);
                }
            }
        }

        stormCombo.getParent().layout(true);
    }

    /**
     * Open an XML file with PSH data.
     */
    private void openXmlFile() {

        // browse for file
        FileDialog browseDialog = new FileDialog(getParentShell(), SWT.OPEN);
        browseDialog.setText("Browse");

        String exportDir = PshConfigurationManager.getInstance()
                .getConfigHeader().getExportDir();

        if (exportDir != null && !exportDir.isEmpty()) {
            browseDialog.setFilterPath(exportDir);
        }
        String[] filterExt = { "*.xml" };
        browseDialog.setFilterExtensions(filterExt);

        String filePath = browseDialog.open();

        if (filePath != null) {
            PshData importedData = PshUtil.importPshDataFromXml(filePath);

            if (importedData != null) {
                // check if already exists
                PshData foundData = PshUtil.retrievePshData(
                        importedData.getBasinName(), importedData.getYear(),
                        importedData.getStormName());

                boolean overwrite = true;
                if (foundData != null) {
                    overwrite = new MessageDialog(getShell(), "Data Exists",
                            null,
                            "Data for the storm, " + importedData.getStormName()
                                    + ", already exists. Would you like to overwrite it?",
                            MessageDialog.WARNING,
                            new String[] { IDialogConstants.YES_LABEL,
                                    IDialogConstants.NO_LABEL },
                            1).open() == MessageDialog.OK;

                }

                if (overwrite) {
                    PshUtil.savePshData(importedData);

                    basinCombo.setText(importedData.getBasinName());
                    yearCombo.setText(String.valueOf(importedData.getYear()));
                    stormCombo.setText(importedData.getStormName());
                    forecasterCombo.setText(importedData.getForecaster());

                    forecasterCombo.notifyListeners(SWT.Selection, new Event());
                }
            } else {
                new MessageDialog(getShell(), "File Error", null,
                        "There was an error opening the file, " + filePath
                                + ". It might not be in the proper XML format.",
                        MessageDialog.ERROR,
                        new String[] { IDialogConstants.OK_LABEL }, 0).open();
            }
        }

    }

    /**
     * Dialog used for County selection.
     * 
     * <pre>
     * SOFTWARE HISTORY
     * Date         Ticket#     Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * Jul 18, 2017 #35463      wpaintsil  Initial creation.
     * Sep 28, 2017 #38374      wpaintsil  Change to a dialog with county selection list.
     * 
     * </pre>
     * 
     * @author wpaintsil
     * @version 1.0
     */
    private class PshCountySelectionDialog extends CaveJFACEDialog {

        private List<String> counties;

        private org.eclipse.swt.widgets.List availableList;

        private org.eclipse.swt.widgets.List selectedList;

        private Button okButton;

        private Button addButton;

        private Button removeButton;

        private Button addAllButton;

        private Button removeAllButton;

        /**
         * Constructor
         */
        public PshCountySelectionDialog(Shell parentShell,
                List<String> counties) {
            super(parentShell);
            this.counties = counties;

        }

        @Override
        public Control createDialogArea(Composite parent) {
            /*
             * Sets dialog title
             */
            getShell().setText("County Selection");

            Composite top = (Composite) super.createDialogArea(parent);

            /*
             * Create the main layout for the shell.
             */
            GridLayout mainLayout = new GridLayout(1, false);
            top.setLayout(mainLayout);
            GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                    true);
            top.setLayoutData(mainLayoutData);

            createListArea(top);

            return top;
        }

        /**
         * Set up the list structure for county selection.
         * 
         * @param top
         */
        private void createListArea(Composite top) {
            Composite listComposite = new Composite(top, SWT.NONE);
            GridLayout listLayout = new GridLayout(3, false);
            listComposite.setLayout(listLayout);

            Composite availableComp = new Composite(listComposite, SWT.NONE);
            availableComp.setLayout(new GridLayout(1, true));

            Label availableLabel = new Label(availableComp, SWT.NONE);
            availableLabel.setText("Available Counties");
            availableLabel.setLayoutData(new GridData(GridData.CENTER,
                    GridData.CENTER, true, false));

            availableList = new org.eclipse.swt.widgets.List(availableComp,
                    SWT.MULTI | SWT.V_SCROLL);
            GridData listData = new GridData(SWT.FILL, SWT.FILL, true, false);
            listData.widthHint = 200;
            listData.heightHint = 300;
            availableList.setLayoutData(listData);

            for (String countyString : counties) {
                if (!pshData.getIncludedCounties().contains(countyString)) {
                    availableList.add(countyString);
                }
                sortList(availableList);

            }

            Composite buttonComposite = new Composite(listComposite, SWT.NONE);
            buttonComposite.setLayout(new GridLayout(1, false));

            addButton = new Button(buttonComposite, SWT.PUSH);
            addButton.setText(">");
            addButton.setToolTipText("Add to selection");
            addButton.addSelectionListener(addButtonListener());

            removeButton = new Button(buttonComposite, SWT.PUSH);
            removeButton.setText("<");
            removeButton.setToolTipText("Remove from selection");
            removeButton.addSelectionListener(removeButtonListener());

            addAllButton = new Button(buttonComposite, SWT.PUSH);
            addAllButton.setText(">>");
            addAllButton.setToolTipText("Add all counties to selection");
            addAllButton.addSelectionListener(addAllButtonListener());

            removeAllButton = new Button(buttonComposite, SWT.PUSH);
            removeAllButton.setText("<<");
            removeAllButton
                    .setToolTipText("Remove all counties from selection");
            removeAllButton.addSelectionListener(removeAllButtonListener());

            Composite selectedComp = new Composite(listComposite, SWT.NONE);
            selectedComp.setLayout(new GridLayout(1, true));

            Label selectedLabel = new Label(selectedComp, SWT.NONE);
            selectedLabel.setText("Selected Counties");
            selectedLabel.setLayoutData(new GridData(GridData.CENTER,
                    GridData.CENTER, true, false));

            selectedList = new org.eclipse.swt.widgets.List(selectedComp,
                    SWT.MULTI | SWT.V_SCROLL);
            selectedList.setLayoutData(listData);

            if (!pshData.getIncludedCounties().isEmpty()) {
                for (String includedCounty : pshData.getIncludedCounties()) {
                    selectedList.add(includedCounty);
                }
                sortList(selectedList);
            }

            toggleButtons();
            /*
             * Enable remove buttons only if a county in the selected counties
             * list is selected.
             */
            selectedList.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    removeButton
                            .setEnabled(selectedList.getSelectionCount() > 0);

                }
            });

            /*
             * Enable add buttons only if a county in the available counties
             * list is selected.
             */
            availableList.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    addButton.setEnabled(availableList.getSelectionCount() > 0);
                }
            });

        }

        /**
         * A listener for the addButton
         * 
         * @param button
         */
        private SelectionListener addButtonListener() {
            return new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (String item : availableList.getSelection()) {
                        selectedList.add(item);
                    }
                    sortList(selectedList);

                    availableList.remove(availableList.getSelectionIndices());

                    toggleButtons();
                    okButton.setEnabled(true);
                }
            };
        }

        /**
         * A listener for the removeButton
         * 
         * @return
         */
        private SelectionListener removeButtonListener() {
            return new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (String item : selectedList.getSelection()) {
                        availableList.add(item);
                    }
                    sortList(availableList);

                    selectedList.remove(selectedList.getSelectionIndices());

                    toggleButtons();
                    okButton.setEnabled(true);
                }
            };
        }

        /**
         * A listener for the addAllButton
         * 
         * @return
         */
        private SelectionListener addAllButtonListener() {
            return new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (String item : availableList.getItems()) {
                        selectedList.add(item);
                    }
                    sortList(selectedList);

                    availableList.removeAll();

                    toggleButtons();
                    okButton.setEnabled(true);
                }
            };
        }

        /**
         * A listener for the removeAllButton
         * 
         * @return
         */
        private SelectionListener removeAllButtonListener() {
            return new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (String item : selectedList.getItems()) {
                        availableList.add(item);
                    }
                    sortList(availableList);

                    selectedList.removeAll();

                    toggleButtons();
                    okButton.setEnabled(true);
                }
            };
        }

        /**
         * Disable appropriate buttons when no items in a list are available or
         * selected.
         */
        private void toggleButtons() {

            addButton.setEnabled(availableList.getSelectionCount() > 0);
            addAllButton.setEnabled(availableList.getItems().length > 0);

            removeButton.setEnabled(selectedList.getSelectionCount() > 0);
            removeAllButton.setEnabled(selectedList.getItems().length > 0);
        }

        /**
         * Maintain the alphabetical order of a list
         * 
         * @param list
         */
        private void sortList(org.eclipse.swt.widgets.List list) {
            // maintain alphabetical order
            List<String> sortedList = Arrays.asList(list.getItems());
            Collections.sort(sortedList);
            list.setItems(sortedList.toArray(new String[sortedList.size()]));
        }

        /**
         * Disable the OK button if the list is empty;
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);

            okButton = getButton(IDialogConstants.OK_ID);

            // Disable the OK button until a change is made to the list of
            // selected counties.
            okButton.setEnabled(false);
        }

        /**
         * Add the selected counties to the counties Label and save them to the
         * database.
         */
        @Override
        protected void okPressed() {
            List<String> includedCounties = Arrays
                    .asList(selectedList.getItems());

            super.okPressed();

            setCountiesText(includedCounties);

            pshData.setIncludedCounties(includedCounties);
            PshUtil.savePshData(pshData);
        }

    }

}