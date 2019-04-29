/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductHeader;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductTypeManager;
import gov.noaa.nws.ocp.viz.climate.configparams.support.AnnualDialog;
import gov.noaa.nws.ocp.viz.climate.configparams.support.StationEditDialog;
import gov.noaa.nws.ocp.viz.climate.configparams.support.ThresholdsDialog;
import gov.noaa.nws.ocp.viz.climate.setupclimate.ClimateReportElementCategory;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.AbstractCategoryComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.CategoryCompositeFactory;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.DegreeDaysComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.PrecipitationComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.RelativeHumidityComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.SkycoverComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.SnowfallComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.SunriseSunsetComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.TemperatureComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.TemperatureRecordComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.WeatherComp;
import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp.WindComp;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * 
 * Main dialog for "Setup/Edit Climate". It enables the user to create, edit,
 * and delete a climate product type by report period type (am - Daily Morning,
 * im - Daily Intermediate, pm - Daily Evening, mon - Monthly, sea (Seasonal, or
 * ann - Annual), report source (NWR or NWWS), and the station that is been
 * broadcasted (e.g,OAX)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#   Engineer    Description
 * ------------ --------- ----------- --------------------------------
 * 06 OCT 2016   20640    jwu         Initial creation
 * 21 DEC 2016   26904    wpaintsil   Center child dialogs.
 * 12 Jan 2017   20640    jwu         Implemented backend Save/Delete
 * 26 Jan 2017   20640    jwu         Implemented backend for menu items.
 * 27 Jan 2017   20640    jwu         Check existing type when source/period/id changes.
 * 07 Feb 2017   20640    jwu         Add defaults & update snow/heat/cool dates.
 * 13 Feb 2017   20640    jwu         Update preference & station changes.
 * 24 FEB 2017   27420    amoore      Address warnings in code.
 * 24 Feb 2017   29585    jwu         Update nnnLbl to reflect report type.
 * 27 Feb 2017   29584    jwu         Disable "Delete Product" button if no product is open.
 * 28 Feb 2017   29599    jwu         Config product only at SITE level.
 * 28 Mar 2017   29748    jwu         Add functionality to convert legacy product types to XML format.
 * 10 MAY 2017   33104    amoore      Address FindBugs dead store variables.
 * 17 MAY 2017   33104    amoore      FindBugs. Package reorg.
 * 23 MAY 2017   29748    jwu         Handle null station list in product types.
 * 03 AUG 2017   36716    amoore      Minor button size adjustments.
 * 16 AUG 2017   37100    jwu         Update category section when opening a product changes report period.
 * 06 OCT 2017   38974    amoore      Reporting Periods dialog should not be singleton.
 * 12 OCT 2017   39354    amoore      No need to store dialogs in fields; more stable code.
 * 16 OCT 2017   39454    amoore      Minimum periodicity is 0, not 1.
 * 15 NOV 2017   39338    amoore      When loading a default product, use current localization site as node.
 * 13 MAR 2018   44624    amoore      Resolved issue found where only CONUS sites could properly define products.
 * 06 NOV 2018   55583    jwu         Fix some layout & alignment issues (DR20915).
 * 30 APR 2019   DR20915  wpaintsil   Further adjustments to alignment.
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class ClimateSetupDialog extends ClimateCaveDialog {
    /**
     * Font with different size to try to match legacy GUI layout.
     */
    private Font size14Font;

    private Font size12Font;

    private Font size12ItalicFont;

    private Font size10Font;

    private Font size10ItalicFont;

    private Font size10BoldFont;

    private Font size8Font;

    /**
     * Stations retrieved from backend.
     */
    private java.util.List<Station> stationList = new ArrayList<>();

    /**
     * Report Type (period + source)
     */
    protected PeriodType reportType = PeriodType.MORN_RAD;

    /**
     * Source of report - "NWR" or "NWWS"
     */
    protected String reportSource = PeriodType.CLIMATE_SOURCES[0];

    /**
     * Period of report, including Daily Morning, Daily Intermediate, Daily
     * Evening, Monthly, Seasonal, Annual
     */
    protected String reportPeriod = PeriodType.CLIMATE_PERIODS[0];

    /**
     * Type of report, including Daily Morning, Daily Intermediate, Daily
     * Evening, Monthly, Seasonal, Annual
     */
    protected ClimateReportElementCategory reportCategory = ClimateReportElementCategory.TEMPERATURE;

    /**
     * Composite for climate configuration section
     */
    protected Composite reportConfigComp;

    private Composite reportConfigInfoComp;

    /**
     * Buttons for report sources (NWR or NWWS)
     */
    private Button[] sourceBtn = new Button[PeriodType.CLIMATE_SOURCES.length];

    /**
     * Buttons for report period (am, im, pm, mon, sea, ann)
     */
    private Button[] reportTypeBtn = new Button[PeriodType.CLIMATE_PERIODS.length];

    /**
     * Header info for report (Ccccnnnxxx)
     * 
     * For nnnLbl: CLI = CLIMATE DAILY; CLM = CLIMATE MONTHLY; CLS = CLIMATE
     * SEASON; CLA = CLIMATE ANNUAL.
     * 
     */
    private Combo nodePrefixCombo;

    private Text nodeTxt;

    protected Label nnnLbl;

    private Text idTxt;

    /**
     * Periodicity spinner for NWR report.
     */
    private Spinner periodicitySpinner;

    /**
     * Effective time (hour, minute, am/pm ) for NWR report.
     */
    private DateTime effectiveTime;

    private Label effectiveTimeZoneLbl;

    /**
     * Expiration time (hour, minute, am/pm ) for NWR report.
     */
    private DateTime expirationTime;

    private Label expirationTimeZoneLbl;

    /**
     * LAC (Listening Area Code) for NWR report.
     */
    private Text lacTxt;

    /**
     * Address for NWWS report.
     */
    private Text addressTxt;

    /**
     * List widget to present all stations.
     */
    private List stationNames;

    /**
     * Dialog listing product types for current report period - "Options" menu.
     */
    private CurrentProductTypesDialog optionsDialog;

    /**
     * Button to indicate if temperature in C should be included.
     */
    protected Button includeCelsiusTempBtn;

    /**
     * Lists for report categories.
     */
    protected java.util.List<Button> categoryList;

    protected java.util.List<Button> auxCategoryList;

    /**
     * Label above category - YESTERDAY'S or TODAY's.
     */
    protected Label catListLbl;

    /**
     * Widgets for show today/tomorrow's temperature and sunrise/sunset.
     */
    protected Composite auxCatChkAllNoneComp;

    /**
     * Label above auxiliary category - TODAY'S or TOMORROW's.
     */
    protected Label auxCatChkAllNoneLbl;

    /**
     * Group to hold all sub-category elements for a selected category.
     */
    private Group subCatContentGrp;

    /**
     * Preference values used in ThresholdsDialog.
     */
    protected ClimateGlobal preferenceValues;

    /**
     * StackLayout & composite for configure info (switch between NWR & NWWS)
     */
    private StackLayout configInfoStackLayout;

    private Composite reportConfigInfoNWRComp;

    private Composite reportConfigInfoNWWSComp;

    /**
     * Map of "All/None" check buttons to turn on/off contents in sub-category
     * group.
     */
    protected java.util.Map<ClimateReportElementCategory, Button> includeAllChkBtns;

    protected java.util.Map<ClimateReportElementCategory, Button> includeNoneChkBtns;

    /**
     * StackLayout to switch between weather element's sub-categories
     */
    private StackLayout subcatStackLayout;

    /**
     * Map of composites for each category
     */
    protected HashMap<ClimateReportElementCategory, AbstractCategoryComp> categoryCompMap;

    /**
     * Climate product type manager.
     */
    private ClimateProductTypeManager cptManager;

    /**
     * Product ID pattern.
     */
    private static final Pattern PROD_ID_PATTERN = Pattern
            .compile("([A-Z,0-9]{3})");

    /**
     * A ModifyListener for product ID.
     */
    private ModifyListener idTxtModifyListner;

    /**
     * Product ID for "default" new product type.
     */
    private static final String DEFAULT_PROD_ID = "DEF";

    /**
     * Default node origination site (CCC).
     */
    private static final String DEFAULT_NODE = ClimateGUIUtils.getCurrentSite();

    /**
     * Available node prefixes (big C in Cccc).
     */
    private static final String[] NODE_PREFIX_OPTIONS = new String[] { "K", "P",
            "T" };

    /**
     * Default WMO ID prefix (big C in Cccc).
     */
    private static final String DEFAULT_NODE_PREFIX = NODE_PREFIX_OPTIONS[0];

    /**
     * Default "address" string.
     */
    private static final String DEFAULT_ADDRESS = "000";

    /**
     * Control button title strings.
     */
    private static final String[] CONTROL_BUTTON_NAMES = new String[] {
            "Save Product", "Delete Product", "Close" };

    /**
     * Product control buttons.
     */
    private Button saveProductBtn;

    private Button deleteProductBtn;

    private Button closeBtn;

    /**
     * Product control dates.
     */
    private ClimateDates snowDates = new ClimateDates(
            new ClimateDate(ClimateReportPeriodsDialog.SNOW_DEFAULT_START_DAY,
                    ClimateReportPeriodsDialog.SNOW_DEFAULT_START_MON),
            new ClimateDate(ClimateReportPeriodsDialog.SNOW_DEFAULT_END_DAY,
                    ClimateReportPeriodsDialog.SNOW_DEFAULT_END_MON));

    private ClimateDates heatDates = new ClimateDates(
            new ClimateDate(ClimateReportPeriodsDialog.HDD_DEFAULT_START_DAY,
                    ClimateReportPeriodsDialog.HDD_DEFAULT_START_MON),
            new ClimateDate(ClimateReportPeriodsDialog.HDD_DEFAULT_END_DAY,
                    ClimateReportPeriodsDialog.HDD_DEFAULT_END_MON));

    private ClimateDates coolDates = new ClimateDates(
            new ClimateDate(ClimateReportPeriodsDialog.CDD_DEFAULT_START_DAY,
                    ClimateReportPeriodsDialog.CDD_DEFAULT_START_MON),
            new ClimateDate(ClimateReportPeriodsDialog.CDD_DEFAULT_END_DAY,
                    ClimateReportPeriodsDialog.CDD_DEFAULT_END_MON));

    /**
     * Constructor.
     * 
     * @param parentShell
     *            parent shell for this dialog.
     */
    public ClimateSetupDialog(Shell parent) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
                ClimateLayoutValues.CLIMATE_DIALOG_CAVE_STYLE
                        | CAVE.INDEPENDENT_SHELL);
        setText("Climate Report Format");

        // Read preference from globalDay.properties
        loadPreferences();

        // Request stations
        loadStations();

        // Load all existing climate product types.
        cptManager = ClimateProductTypeManager.getInstance();
    }

    /**
     * Initialize the dialog components.
     */
    @Override
    protected void initializeComponents(final Shell shell) {
        // Prepare fonts.
        createFonts();

        // Listener when dialog closes.
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (closeThisDialog()) {
                    disposeFontsColors();
                    close();
                } else {
                    event.doit = false;
                }
            }
        });

        // Menus.
        createMenuBar();

        // Main layout.
        shell.setLayout(new GridLayout(1, false));

        // Main comp to hold all widgets.
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(new GridLayout(1, false));
        mainComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));

        // Top comp holds report ID section and four editing buttons
        Composite topComp = new Composite(mainComp, SWT.NONE);
        GridLayout topGL = new GridLayout(1, false);
        topComp.setLayout(topGL);
        topComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));

        // Comp to hold report type/configuration/stations
        Composite idComp = new Composite(topComp, SWT.NONE);
        GridLayout idGL = new GridLayout(3, false);
        idGL.horizontalSpacing = 60;
        idComp.setLayout(idGL);
        idComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        createReportTypeSection(idComp);
        createReportConfigSection(idComp, reportSource);
        createReportStationsSection(idComp);

        // Editing buttons
        createEditingButtons(topComp);

        /*
         * Bottom comp holds weather elements (categories & sub-categories)
         */
        Composite botComp = new Composite(mainComp, SWT.NONE);
        botComp.setLayout(new GridLayout(1, false));
        botComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        createWeatherElementSection(botComp);

        // Open NWR, "Daily Morning" to start with.
        openNewProductType();
    }

    /**
     * Create fonts used in this dialog.
     */
    private void createFonts() {
        FontData fontData = getShell().getFont().getFontData()[0];
        size14Font = new Font(getDisplay(),
                new FontData(fontData.getName(), 14, SWT.NORMAL));
        size12Font = new Font(getDisplay(),
                new FontData(fontData.getName(), 12, SWT.NORMAL));
        size12ItalicFont = new Font(getDisplay(),
                new FontData(fontData.getName(), 12, SWT.ITALIC));
        size10Font = new Font(getDisplay(),
                new FontData(fontData.getName(), 10, SWT.NORMAL));
        size10ItalicFont = new Font(getDisplay(),
                new FontData(fontData.getName(), 10, SWT.ITALIC));
        size10BoldFont = new Font(getDisplay(),
                new FontData(fontData.getName(), 10, SWT.BOLD));
        size8Font = new Font(getDisplay(),
                new FontData(fontData.getName(), 8, SWT.NORMAL));
    }

    /**
     * Create menu bar.
     */
    private void createMenuBar() {
        Menu mainMenuBar = new Menu(shell, SWT.BAR);

        // "File" menu
        MenuItem fileMenuHeader = new MenuItem(mainMenuBar, SWT.CASCADE);
        fileMenuHeader.setText("&File");

        Menu fileMenu = new Menu(mainMenuBar);
        fileMenuHeader.setMenu(fileMenu);

        MenuItem newProductMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        newProductMenuItem.setText("&New Product");
        newProductMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openNewProductType();
            }
        });

        // "Open Existing Product" and its sub-menu
        MenuItem openProductMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
        openProductMenuItem.setText("&Open Existing Product");

        final Menu openProductMenu = new Menu(mainMenuBar);
        openProductMenuItem.setMenu(openProductMenu);

        openProductMenu.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event event) {
                createOpenExistingProductMenu(openProductMenu);
            }
        });

        MenuItem deleteProductMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        deleteProductMenuItem.setText("&Delete Product");
        deleteProductMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                deleteProductType();
            }
        });

        // first separator file menu item
        new MenuItem(fileMenu, SWT.SEPARATOR);

        MenuItem saveProductMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        saveProductMenuItem.setText("&Save Product");
        saveProductMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveProductType();
            }
        });

        // second separator file menu item
        new MenuItem(fileMenu, SWT.SEPARATOR);

        MenuItem convertProductMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        convertProductMenuItem.setText("&Convert Legacy Product Formats");
        convertProductMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ClimateConvertDialog convertDlg = new ClimateConvertDialog(
                        shell);
                openEditingDlg(convertDlg);
            }
        });

        // third separator file menu item
        new MenuItem(fileMenu, SWT.SEPARATOR);

        MenuItem closeMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        closeMenuItem.setText("&Close");
        closeMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (closeThisDialog()) {
                    disposeFontsColors();
                    close();
                } else {
                    event.doit = false;
                }
            }
        });

        // "Edit" menu
        MenuItem editMenuHeader = new MenuItem(mainMenuBar, SWT.CASCADE);
        editMenuHeader.setText("&Edit");

        Menu editMenu = new Menu(mainMenuBar);
        editMenuHeader.setMenu(editMenu);

        MenuItem editReportPeriodMenuItem = new MenuItem(editMenu, SWT.PUSH);
        editReportPeriodMenuItem.setText("&Edit Reporting Periods");
        editReportPeriodMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openReportingPeriodDlg();
            }
        });

        MenuItem editAnnualPeriodMenuItem = new MenuItem(editMenu, SWT.PUSH);
        editAnnualPeriodMenuItem.setText("&Edit Annual Periods");
        editAnnualPeriodMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openEditingDlg(new AnnualDialog(shell));
            }
        });

        MenuItem changeThreshholdMenuItem = new MenuItem(editMenu, SWT.PUSH);
        changeThreshholdMenuItem.setText("&Change Threshhold Values");
        changeThreshholdMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openThresholdDlg();
            }
        });

        MenuItem editStationListMenuItem = new MenuItem(editMenu, SWT.PUSH);
        editStationListMenuItem.setText("&Edit Station List");
        editStationListMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openStationEditDlg();
            }
        });

        // separator edit menu item
        new MenuItem(editMenu, SWT.SEPARATOR);

        MenuItem SelectAllElementsMenuItem = new MenuItem(editMenu, SWT.PUSH);
        SelectAllElementsMenuItem.setText("&Select All Elements");
        SelectAllElementsMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                selectAllElements(true);
            }
        });

        MenuItem deselectAllElementsMenuItem = new MenuItem(editMenu, SWT.PUSH);
        deselectAllElementsMenuItem.setText("&De-select All Elements");
        deselectAllElementsMenuItem
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        selectAllElements(false);
                    }
                });

        // "Options" menu
        MenuItem optionsMenuHeader = new MenuItem(mainMenuBar, SWT.CASCADE);
        optionsMenuHeader.setText("&Options");

        Menu optionsMenu = new Menu(mainMenuBar);
        optionsMenuHeader.setMenu(optionsMenu);

        MenuItem currentProductWindowMenuItem = new MenuItem(optionsMenu,
                SWT.PUSH);
        currentProductWindowMenuItem.setText("&Current Product Window");
        currentProductWindowMenuItem
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        openCurrentPeriodDlg();
                    }
                });

        // "Help" menu
        MenuItem helpMenuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
        helpMenuItem.setText("&Help");

        Menu helpMenu = new Menu(mainMenuBar);
        helpMenuItem.setMenu(helpMenu);

        MenuItem aboutMI = new MenuItem(helpMenu, SWT.NORMAL);
        aboutMI.setText("Handbook");
        aboutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("report_format.html");
            }
        });

        shell.setMenuBar(mainMenuBar);
    }

    /**
     * Create report source/type section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createReportTypeSection(Composite parent) {
        // Create report type
        Composite reportTypeComp = new Composite(parent, SWT.NONE);
        RowLayout reportTypeLayout = new RowLayout(SWT.VERTICAL);
        reportTypeLayout.spacing = 6;
        reportTypeLayout.marginWidth = 5;
        reportTypeLayout.center = true;
        reportTypeComp.setLayout(reportTypeLayout);

        Label typeLbl = new Label(reportTypeComp, SWT.NORMAL);
        typeLbl.setText("Report Type");
        typeLbl.setFont(size14Font);

        // Report source - NWS or NWWS
        Composite sourceComp = new Composite(reportTypeComp, SWT.NONE);
        RowLayout sourceLayout = new RowLayout(SWT.HORIZONTAL);
        sourceLayout.spacing = 9;
        sourceLayout.fill = true;
        sourceComp.setLayout(sourceLayout);
        sourceComp.setFont(size12Font);

        int ibi = 0;
        for (String orig : PeriodType.CLIMATE_SOURCES) {
            sourceBtn[ibi] = new Button(sourceComp, SWT.RADIO);
            sourceBtn[ibi].setText(orig);
            sourceBtn[ibi].setData(orig);
            if (orig.equals(PeriodType.CLIMATE_SOURCES[0])) {
                sourceBtn[ibi].setSelection(true);
            }

            sourceBtn[ibi].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    String newOrig = (String) event.widget.getData();
                    if (!newOrig.equals(reportSource)) {
                        reportSource = newOrig;
                        reportType = PeriodType.getPeriodType(reportSource,
                                reportPeriod);
                        createReportConfigInfo(reportConfigComp, reportSource,
                                false);

                        // Update flags for all categories for the new source
                        updateAllCategoryComps();

                        if (PeriodType.isNWR(reportSource)
                                && categoryList.get(0).getSelection()) {
                            includeCelsiusTempBtn.setVisible(true);
                        } else {
                            includeCelsiusTempBtn.setVisible(false);
                        }

                        // Ask to open an existing product type if existing.
                        if (((Button) event.widget).getSelection()) {
                            openCurrentProductType();
                        }
                    }
                }
            });

            ibi++;
        }

        // Report Type
        Composite subtypeComp = new Composite(reportTypeComp, SWT.NONE);
        RowLayout subtypeLayout = new RowLayout(SWT.VERTICAL);
        subtypeLayout.spacing = 5;
        subtypeLayout.fill = true;
        subtypeLayout.marginLeft = 25;
        subtypeComp.setLayout(subtypeLayout);

        int ibr = 0;
        for (String rptType : PeriodType.CLIMATE_PERIODS) {
            reportTypeBtn[ibr] = new Button(subtypeComp, SWT.RADIO);
            if (rptType.equals(PeriodType.CLIMATE_PERIODS[0])) {
                reportTypeBtn[0].setSelection(true);
            }
            reportTypeBtn[ibr].setText(rptType);
            reportTypeBtn[ibr].setData(rptType);
            reportTypeBtn[ibr].setFont(size10Font);
            reportTypeBtn[ibr].addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent event) {

                    String prevReportPeriod = reportPeriod;
                    reportPeriod = (String) event.widget.getData();
                    reportType = PeriodType.getPeriodType(reportSource,
                            reportPeriod);

                    // Update flags for all categories for the new source
                    updateAllCategoryComps();

                    // Update "Options" dialog
                    updateCurrentPeriodDlg();

                    // Update Category section
                    updateCatogeryForPeriod(prevReportPeriod);

                    // Ask to open an existing product type if existing.
                    if (((Button) event.widget).getSelection()) {
                        openCurrentProductType();
                    }
                }

            });

            ibr++;
        }
    }

    /**
     * Create report configuration section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createReportConfigSection(Composite parent, String orig) {

        reportConfigComp = new Composite(parent, SWT.NONE);
        RowLayout reportConfigLayout = new RowLayout(SWT.VERTICAL);
        reportConfigLayout.spacing = 1;
        reportConfigLayout.center = true;
        reportConfigComp.setLayout(reportConfigLayout);

        // Create report configuration header part
        createReportConfigHeader(reportConfigComp);

        // Create report configuration information part
        createReportConfigInfo(reportConfigComp, orig, true);
    }

    /**
     * Create report configuration header part.
     * 
     * @param parent
     *            parent composite.
     */
    private void createReportConfigHeader(Composite parent) {

        Composite reportConfigHeaderComp = new Composite(parent, SWT.NONE);

        RowLayout reportConfigHeaderLayout = new RowLayout(SWT.VERTICAL);
        reportConfigHeaderLayout.spacing = 1;
        reportConfigHeaderLayout.marginLeft = 40;
        reportConfigHeaderLayout.center = true;
        reportConfigHeaderComp.setLayout(reportConfigHeaderLayout);

        Label configLbl = new Label(reportConfigHeaderComp, SWT.NORMAL);
        configLbl.setText("Report Configuration");
        configLbl.setFont(size14Font);
        configLbl.setAlignment(SWT.CENTER);

        // Create report configuration header part
        Composite reportConfigIDComp = new Composite(reportConfigHeaderComp,
                SWT.NORMAL);
        GridLayout reportConfigIDLayout = new GridLayout(5, false);
        reportConfigIDLayout.horizontalSpacing = 5;
        reportConfigIDComp.setLayout(reportConfigIDLayout);

        Label headerLbl = new Label(reportConfigIDComp, SWT.NORMAL);
        headerLbl.setText("Header:");
        GridData headerLblGD = new GridData(SWT.LEFT, SWT.TOP, false, false, 1,
                2);
        headerLblGD.verticalIndent = 3;
        headerLbl.setLayoutData(headerLblGD);

        nodePrefixCombo = new Combo(reportConfigIDComp, SWT.POP_UP);
        for (String prefixOption : NODE_PREFIX_OPTIONS) {
            nodePrefixCombo.add(prefixOption);
        }
        nodePrefixCombo.setText(DEFAULT_NODE_PREFIX);

        nodeTxt = new Text(reportConfigIDComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        GridData nodeTxtGD = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        nodeTxtGD.widthHint = 40;
        nodeTxtGD.heightHint = 15;
        nodeTxt.setLayoutData(nodeTxtGD);
        nodeTxt.setTextLimit(3);
        nodeTxt.setText(DEFAULT_NODE);
        nodeTxt.setToolTipText(
                "Node origination site (CCC) in AFOS product identifier (CCCNNNXXX) e.g., "
                        + DEFAULT_NODE + ".");

        nodeTxt.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent ee) {
                for (int ii = 0; ii < ee.text.length(); ii++) {
                    if (!Character.isAlphabetic(ee.text.charAt(ii))) {
                        ee.doit = false;
                        return;
                    } else {
                        ee.text = ee.text.toUpperCase();
                    }
                }
            }
        });

        nnnLbl = new Label(reportConfigIDComp, SWT.NORMAL);
        nnnLbl.setText("CLI");
        GridData nnnLblGD = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        nnnLblGD.widthHint = 28;
        nnnLbl.setLayoutData(nnnLblGD);

        idTxt = new Text(reportConfigIDComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        idTxt.setTextLimit(3);
        idTxt.setLayoutData(nodeTxtGD);
        idTxt.setToolTipText(
                "The three letter code for the station that is being broadcasted, e.g., BWI.");

        idTxt.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent ee) {
                for (int ii = 0; ii < ee.text.length(); ii++) {
                    if (!Character.isAlphabetic(ee.text.charAt(ii))
                            && !Character.isDigit(ee.text.charAt(ii))) {
                        ee.doit = false;
                        return;
                    } else {
                        ee.text = ee.text.toUpperCase();
                    }
                }
            }
        });

        idTxtModifyListner = new ModifyListener() {
            public void modifyText(ModifyEvent ee) {
                openCurrentProductType();
            }
        };

        idTxt.addModifyListener(idTxtModifyListner);

        Label nodePrefixHintLbl = new Label(reportConfigIDComp, SWT.NORMAL);
        nodePrefixHintLbl.setText("Node (C)");
        nodePrefixHintLbl.setFont(size8Font);

        Label nodeHintLbl = new Label(reportConfigIDComp, SWT.NORMAL);
        nodeHintLbl.setText("Node (ccc)");
        nodeHintLbl.setFont(size8Font);

        Label nnnHintLbl = new Label(reportConfigIDComp, SWT.NORMAL);
        nnnHintLbl.setText("(nnn)");
        nnnHintLbl.setFont(size8Font);

        Label idHintLbl = new Label(reportConfigIDComp, SWT.NORMAL);
        idHintLbl.setText("Prod ID (xxx)");
        idHintLbl.setFont(size8Font);

    }

    /**
     * Create report configuration info part.
     * 
     * @param parent
     *            parent composite.
     * @param orig
     *            ClimateReportSource.
     * @param create
     *            flag to create (true) or create (false).
     */
    private void createReportConfigInfo(Composite parent, String orig,
            boolean create) {

        if (create) {

            configInfoStackLayout = new StackLayout();
            reportConfigInfoComp = new Composite(parent, SWT.NONE);
            reportConfigInfoComp.setLayout(configInfoStackLayout);

            reportConfigInfoNWRComp = new Composite(reportConfigInfoComp,
                    SWT.NONE);
            reportConfigInfoNWWSComp = new Composite(reportConfigInfoComp,
                    SWT.NONE);

            // Create and layout widgets
            RowLayout reportConfigInfoLayout = new RowLayout(SWT.VERTICAL);
            reportConfigInfoLayout.spacing = 1;
            reportConfigInfoLayout.fill = true;
            reportConfigInfoNWRComp.setLayout(reportConfigInfoLayout);
            reportConfigInfoNWWSComp.setLayout(reportConfigInfoLayout);

            Composite periodicityComp = new Composite(reportConfigInfoNWRComp,
                    SWT.NONE);
            RowLayout periodicityLayout = new RowLayout(SWT.HORIZONTAL);
            periodicityLayout.spacing = 5;
            periodicityLayout.center = true;
            periodicityComp.setLayout(periodicityLayout);
            Label periodicityLbl = new Label(periodicityComp, SWT.NORMAL);
            periodicityLbl.setText("Periodicity:");
            periodicityLbl.setFont(size10Font);
            periodicitySpinner = new Spinner(periodicityComp, SWT.BORDER);

            GC gc = new GC(periodicitySpinner);
            int fontWidth = gc.getFontMetrics().getAverageCharWidth();
            int fontHeight = gc.getFontMetrics().getHeight();
            gc.dispose();

            RowData periodicitySpinnerRD = new RowData(6 * fontWidth,
                    fontHeight);
            periodicitySpinner.setLayoutData(periodicitySpinnerRD);
            periodicitySpinner.setMaximum(3600);
            periodicitySpinner.setMinimum(0);
            periodicitySpinner.setSelection(60);
            periodicitySpinner.setPageIncrement(5);
            periodicitySpinner.setIncrement(1);

            Label minuteLbl = new Label(periodicityComp, SWT.NORMAL);
            minuteLbl.setText("Min");
            minuteLbl.setFont(size10Font);

            Composite effectiveTimeComp = new Composite(reportConfigInfoNWRComp,
                    SWT.NONE);
            RowLayout effectiveTimeLayout = new RowLayout(SWT.HORIZONTAL);
            effectiveTimeLayout.spacing = 5;
            effectiveTimeLayout.center = true;
            effectiveTimeComp.setLayout(effectiveTimeLayout);

            Label effectiveTimeLbl = new Label(effectiveTimeComp, SWT.NORMAL);
            effectiveTimeLbl.setText("Effective Time:");
            effectiveTimeLbl.setFont(size10Font);
            effectiveTime = new DateTime(effectiveTimeComp,
                    SWT.BORDER | SWT.TIME | SWT.SHORT);

            effectiveTimeZoneLbl = new Label(effectiveTimeComp, SWT.NORMAL);
            effectiveTimeZoneLbl.setText("LST");
            effectiveTimeZoneLbl.setFont(size10Font);

            Composite expirationTimeComp = new Composite(
                    reportConfigInfoNWRComp, SWT.NONE);
            RowLayout expirationTimeLayout = new RowLayout(SWT.HORIZONTAL);

            Label expirationTimeLbl = new Label(expirationTimeComp, SWT.NORMAL);

            expirationTimeLayout.spacing = 5;
            expirationTimeLayout.center = true;
            expirationTimeComp.setLayout(expirationTimeLayout);
            expirationTimeLbl.setText("Expiration Time:");
            expirationTimeLbl.setFont(size10Font);

            expirationTime = new DateTime(expirationTimeComp,
                    SWT.BORDER | SWT.TIME | SWT.SHORT);

            expirationTimeZoneLbl = new Label(expirationTimeComp, SWT.NORMAL);
            expirationTimeZoneLbl.setText("LST");
            expirationTimeZoneLbl.setFont(size10Font);

            Composite lacComp = new Composite(reportConfigInfoNWRComp,
                    SWT.NONE);
            RowLayout lacLayout = new RowLayout(SWT.HORIZONTAL);
            lacLayout.spacing = 5;
            lacLayout.center = true;
            lacComp.setLayout(lacLayout);
            Label lacLbl = new Label(lacComp, SWT.NORMAL);
            lacLbl.setText("LAC:");
            lacTxt = new Text(lacComp, ClimateLayoutValues.TEXT_FIELD_STYLE);

            RowData lacTxtRD = new RowData(25 * fontWidth, fontHeight);
            lacTxt.setLayoutData(lacTxtRD);
            lacTxt.setTextLimit(7);
            lacTxt.setToolTipText(
                    "A seven letter code ending with a lowercase c, such as VAC999c");

            // Composite for NWWS, only have address.
            Composite addressComp = new Composite(reportConfigInfoNWWSComp,
                    SWT.NONE);
            RowLayout addressLayout = new RowLayout(SWT.HORIZONTAL);
            addressLayout.spacing = 5;
            addressLayout.center = true;
            addressComp.setLayout(addressLayout);
            Label addressLbl = new Label(addressComp, SWT.NORMAL);
            addressLbl.setText("Address:");
            addressTxt = new Text(addressComp,
                    ClimateLayoutValues.TEXT_FIELD_STYLE);

            RowData addressTxtRD = new RowData(18 * fontWidth, fontHeight);
            addressTxt.setLayoutData(addressTxtRD);
            addressTxt.setTextLimit(3);
        }

        // Layout/pack the composite to reflect the update.
        if (PeriodType.isNWR(orig)) {
            configInfoStackLayout.topControl = reportConfigInfoNWRComp;
        } else {
            configInfoStackLayout.topControl = reportConfigInfoNWWSComp;
        }

        reportConfigInfoComp.layout();
    }

    /**
     * Create report station list section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createReportStationsSection(Composite parent) {
        Composite stationListComp = new Composite(parent, SWT.NONE);
        RowLayout stationListLayout = new RowLayout(SWT.VERTICAL);
        stationListLayout.spacing = 5;
        stationListLayout.marginLeft = 15;
        stationListLayout.center = true;
        stationListComp.setLayout(stationListLayout);

        Label stationLbl = new Label(stationListComp, SWT.NORMAL);
        stationLbl.setText("Select Stations\n   for Product");

        stationNames = new List(stationListComp,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

        RowData stationListData = new RowData(SWT.DEFAULT,
                7 * stationNames.getItemHeight());
        stationNames.setLayoutData(stationListData);

        // Add all stations to the list.
        for (Station station : stationList) {
            stationNames.add(station.getIcaoId());
        }

        stationNames.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            }
        });
    }

    /**
     * Create buttons to edit report periods, threshold values and station list.
     * 
     * @param parent
     *            parent composite.
     */
    private void createEditingButtons(Composite parent) {

        Composite editComp = new Composite(parent, SWT.NONE);
        GridLayout editCompLayout = new GridLayout(4, true);
        editCompLayout.horizontalSpacing = 10;
        editCompLayout.marginWidth = 20;
        editCompLayout.marginTop = 5;
        editComp.setLayout(editCompLayout);

        editComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        GridData editingBtnGd = new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1);
        editingBtnGd.minimumWidth = 180;

        Button editReportPeriodBtn = new Button(editComp, SWT.NORMAL);
        editReportPeriodBtn.setText("Edit Reporting Periods");
        editReportPeriodBtn.setFont(size10Font);
        editReportPeriodBtn.setLayoutData(editingBtnGd);
        editReportPeriodBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openReportingPeriodDlg();
            }
        });

        GridData annualPeriodGd = new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1);
        annualPeriodGd.minimumWidth = 180;
        Button annualPeriodBtn = new Button(editComp, SWT.NORMAL);
        annualPeriodBtn.setText("Edit Annual Periods");
        annualPeriodBtn.setFont(size10Font);
        annualPeriodBtn.setLayoutData(annualPeriodGd);
        annualPeriodBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openEditingDlg(new AnnualDialog(shell));
            }
        });

        GridData userValuesBtnGd = new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1);
        userValuesBtnGd.minimumWidth = 180;
        Button userValuesBtn = new Button(editComp, SWT.NORMAL);
        userValuesBtn.setText("Change Threshold Values");
        userValuesBtn.setFont(size10Font);
        userValuesBtn.setLayoutData(userValuesBtnGd);
        userValuesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openThresholdDlg();
            }
        });

        GridData editStationsGd = new GridData(SWT.FILL, SWT.CENTER, true,
                false, 1, 1);
        editStationsGd.minimumWidth = 180;
        Button editStationsBtn = new Button(editComp, SWT.NORMAL);
        editStationsBtn.setText("Edit Station List");
        editStationsBtn.setFont(size10Font);
        editStationsBtn.setLayoutData(editStationsGd);
        editStationsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openStationEditDlg();
            }
        });
    }

    /**
     * Create weather elements section
     *
     * @param parent
     *            parent composite.
     */
    private void createWeatherElementSection(Composite parent) {
        Composite weatherElemComp = new Composite(parent, SWT.NONE);
        RowLayout weatherElemLayout = new RowLayout(SWT.VERTICAL);
        weatherElemLayout.spacing = 5;
        weatherElemLayout.fill = true;
        weatherElemComp.setLayout(weatherElemLayout);

        Composite captionComp = new Composite(weatherElemComp, SWT.NONE);
        GridLayout captionLayout = new GridLayout(3, true);
        captionComp.setLayout(captionLayout);

        Label emptyLbl = new Label(captionComp, SWT.NORMAL);
        emptyLbl.setText("");

        Label weatherElemLbl = new Label(captionComp, SWT.NORMAL);
        weatherElemLbl.setText("Weather Elements");
        weatherElemLbl.setFont(size14Font);
        GridData weatherElemLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        weatherElemLblGD.horizontalIndent = 10;
        weatherElemLbl.setLayoutData(weatherElemLblGD);

        // Check box to include temperature in Celsius
        includeCelsiusTempBtn = new Button(captionComp, SWT.CHECK);
        includeCelsiusTempBtn.setText("Include temp in deg C?");
        includeCelsiusTempBtn.setFont(size10Font);
        GridData includeCelsiusTempBtnGD = new GridData(SWT.RIGHT, SWT.BOTTOM,
                false, false, 1, 1);
        includeCelsiusTempBtnGD.horizontalIndent = 60;
        includeCelsiusTempBtnGD.verticalIndent = 15;
        includeCelsiusTempBtn.setLayoutData(includeCelsiusTempBtnGD);

        // SashForm to hold report categories/sub-categories
        SashForm elemSashForm = new SashForm(weatherElemComp, SWT.HORIZONTAL);
        RowLayout elemSashLayout = new RowLayout(SWT.HORIZONTAL);
        elemSashLayout.spacing = 15;
        elemSashLayout.marginWidth = 10;
        elemSashLayout.fill = true;
        elemSashForm.SASH_WIDTH = 15;
        elemSashForm.setLayout(elemSashLayout);

        // Create categories composite
        createCategorySection(elemSashForm);

        // Create sub-categories composite
        createSubcategorySection(elemSashForm);
        elemSashForm.setWeights(new int[] { 35, 65 });
    }

    /**
     * Create weather elements' categories section
     *
     * @param parent
     *            parent composite.
     */
    private void createCategorySection(Composite parent) {
        Group catContentGrp = new Group(parent, SWT.NONE);
        GridLayout catContentLayout = new GridLayout(1, true);
        catContentLayout.marginWidth = 3;
        catContentGrp.setLayout(catContentLayout);
        catContentGrp.setText("Categories");
        catContentGrp.setFont(size12ItalicFont);

        // Create all/none check buttons
        Composite allNoneComp = new Composite(catContentGrp, SWT.NONE);

        GridData allNoneGD = new GridData();
        allNoneGD.grabExcessHorizontalSpace = true;
        allNoneComp.setLayoutData(allNoneGD);

        GridLayout allNoneLayout = new GridLayout(2, false);
        allNoneLayout.horizontalSpacing = 25;
        allNoneLayout.verticalSpacing = 0;
        allNoneComp.setLayout(allNoneLayout);

        Label catChkAllNoneLbl = new Label(allNoneComp, SWT.NORMAL);
        catChkAllNoneLbl.setText(" Include\n  All  None");
        catChkAllNoneLbl.setFont(size10Font);
        catChkAllNoneLbl.setAlignment(SWT.CENTER);

        includeAllChkBtns = new HashMap<>();
        includeNoneChkBtns = new HashMap<>();

        catListLbl = new Label(allNoneComp, SWT.NORMAL);
        catListLbl.setText("YESTERDAY'S");
        catListLbl.setAlignment(SWT.LEFT);
        catListLbl.setFont(size10BoldFont);

        categoryList = new ArrayList<>();

        for (ClimateReportElementCategory cat : ClimateReportElementCategory
                .values()) {
            if (!cat.isAuxiliary()) {
                Composite catChkAllNoneComp = new Composite(allNoneComp,
                        SWT.NONE);

                GridData catChkAllNoneGD = new GridData();
                catChkAllNoneGD.grabExcessHorizontalSpace = true;
                catChkAllNoneComp.setLayoutData(catChkAllNoneGD);

                GridLayout catChkAllNoneLayout = new GridLayout(2, true);
                catChkAllNoneLayout.verticalSpacing = 0;
                catChkAllNoneComp.setLayout(catChkAllNoneLayout);

                Button allChkBtn = new Button(catChkAllNoneComp, SWT.CHECK);
                allChkBtn.setData(cat);
                includeAllChkBtns.put(cat, allChkBtn);
                allChkBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        ClimateReportElementCategory rptCat = (ClimateReportElementCategory) event.widget
                                .getData();
                        includeNoneChkBtns.get(rptCat).setSelection(false);

                        // Update flags for all categories for the new source
                        updateAllCategoryComps();
                    }
                });

                Button noneChkBtn = new Button(catChkAllNoneComp, SWT.CHECK);
                noneChkBtn.setData(cat);
                includeNoneChkBtns.put(cat, noneChkBtn);
                noneChkBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        ClimateReportElementCategory rptCat = (ClimateReportElementCategory) event.widget
                                .getData();
                        includeAllChkBtns.get(rptCat).setSelection(false);

                        // Update flags for all categories for the new source
                        updateAllCategoryComps();
                    }
                });

                Button catButton = new Button(allNoneComp, SWT.RADIO);
                catButton.setText(cat.getCategory());

                catButton.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        reportCategory = cat;

                        if (PeriodType.isNWR(reportSource)
                                && categoryList.get(0).getSelection()) {
                            includeCelsiusTempBtn.setVisible(true);
                        } else {
                            includeCelsiusTempBtn.setVisible(false);
                        }

                        // Switch category composite
                        switchElementCategory(reportCategory);
                    }
                });

                categoryList.add(catButton);
            }
        }

        categoryList.get(0).setSelection(true);

        // Create auxiliary categories
        auxCatChkAllNoneComp = new Composite(catContentGrp, SWT.NONE);

        GridData auxCatChkAllNoneGD = new GridData();
        auxCatChkAllNoneGD.grabExcessHorizontalSpace = true;
        auxCatChkAllNoneComp.setLayoutData(auxCatChkAllNoneGD);

        GridLayout auxChkLayout = new GridLayout(2, false);
        auxChkLayout.verticalSpacing = 0;
        auxChkLayout.horizontalSpacing = 30;
        auxCatChkAllNoneComp.setLayout(auxChkLayout);

        Label spaceLbl = new Label(auxCatChkAllNoneComp, SWT.NORMAL);
        spaceLbl.setText("");
        spaceLbl.setAlignment(SWT.LEFT);
        spaceLbl.setFont(size10BoldFont);

        auxCatChkAllNoneLbl = new Label(auxCatChkAllNoneComp, SWT.NORMAL);
        auxCatChkAllNoneLbl.setText("TODAY's");
        auxCatChkAllNoneLbl.setAlignment(SWT.LEFT);
        auxCatChkAllNoneLbl.setFont(size10BoldFont);

        auxCategoryList = new ArrayList<>();

        for (ClimateReportElementCategory cat : ClimateReportElementCategory
                .values()) {
            if (cat.isAuxiliary()) {

                Composite catChkAllNoneComp = new Composite(
                        auxCatChkAllNoneComp, SWT.NONE);

                GridData catChkAllNoneGD = new GridData();
                catChkAllNoneGD.grabExcessHorizontalSpace = true;
                catChkAllNoneComp.setLayoutData(catChkAllNoneGD);

                GridLayout catChkAllNoneLayout = new GridLayout(2, true);
                catChkAllNoneLayout.verticalSpacing = 0;
                catChkAllNoneComp.setLayout(catChkAllNoneLayout);

                Button allChkBtn = new Button(catChkAllNoneComp, SWT.CHECK);
                allChkBtn.setData(cat);
                includeAllChkBtns.put(cat, allChkBtn);
                allChkBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        ClimateReportElementCategory rptCat = (ClimateReportElementCategory) event.widget
                                .getData();
                        includeNoneChkBtns.get(rptCat).setSelection(false);

                        // Update button status for this category composite
                        AbstractCategoryComp catComp = categoryCompMap
                                .get(rptCat);
                        catComp.selectAllButtons();
                    }
                });

                Button noneChkBtn = new Button(catChkAllNoneComp, SWT.CHECK);
                noneChkBtn.setData(cat);
                includeNoneChkBtns.put(cat, noneChkBtn);
                noneChkBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        ClimateReportElementCategory rptCat = (ClimateReportElementCategory) event.widget
                                .getData();
                        includeAllChkBtns.get(rptCat).setSelection(false);

                        // Update button status for this category composite
                        AbstractCategoryComp catComp = categoryCompMap
                                .get(rptCat);
                        catComp.deselectAllButtons();
                    }
                });

                Button catButton = new Button(auxCatChkAllNoneComp, SWT.RADIO);
                catButton.setText(cat.getCategory());

                catButton.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        reportCategory = cat;

                        // Switch category composite
                        switchElementCategory(reportCategory);
                    }
                });

                auxCategoryList.add(catButton);
            }
        }

        // Create a separator
        Label separator = new Label(catContentGrp,
                SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

        // Create control buttons - Save/Delete/Close
        createControlButtons(catContentGrp);
    }

    /**
     * Create weather elements' sub-categories section
     *
     * @param parent
     *            parent composite.
     */
    private void createControlButtons(Composite parent) {
        GridData actionBtnGD = new GridData();
        actionBtnGD.verticalIndent = 10;
        actionBtnGD.horizontalSpan = 2;
        actionBtnGD.horizontalAlignment = SWT.FILL;
        actionBtnGD.heightHint = 40;
        actionBtnGD.verticalAlignment = SWT.BOTTOM;

        saveProductBtn = new Button(parent, SWT.NORMAL);
        saveProductBtn.setText(CONTROL_BUTTON_NAMES[0]);
        saveProductBtn.setLayoutData(actionBtnGD);
        saveProductBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveProductType();
            }
        });

        deleteProductBtn = new Button(parent, SWT.NORMAL);
        deleteProductBtn.setText(CONTROL_BUTTON_NAMES[1]);
        deleteProductBtn.setEnabled(false);
        deleteProductBtn.setLayoutData(actionBtnGD);
        deleteProductBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                deleteProductType();
            }
        });

        closeBtn = new Button(parent, SWT.NORMAL);
        closeBtn.setText(CONTROL_BUTTON_NAMES[2]);
        closeBtn.setLayoutData(actionBtnGD);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (closeThisDialog()) {
                    disposeFontsColors();
                    close();
                } else {
                    event.doit = false;
                }
            }
        });
    }

    /**
     * Create weather elements' sub-categories section. It creates a composites
     * each category (a total of 10). A StackLayout is used to hide/show the
     * proper composite when the category changes.
     *
     * 
     * @param parent
     *            parent composite.
     */
    private void createSubcategorySection(Composite parent) {

        subCatContentGrp = new Group(parent, SWT.NONE);
        subcatStackLayout = new StackLayout();
        subCatContentGrp.setLayout(subcatStackLayout);
        subCatContentGrp.setText("Subcategories");
        subCatContentGrp.setFont(size12ItalicFont);

        // Create a HashMap to hold composites for each category.
        categoryCompMap = new HashMap<>();
        for (ClimateReportElementCategory cat : ClimateReportElementCategory
                .values()) {
            AbstractCategoryComp catComp = CategoryCompositeFactory
                    .create(subCatContentGrp, cat, null, preferenceValues);

            if (cat.equals(ClimateReportElementCategory.TEMPRECORD) || cat
                    .equals(ClimateReportElementCategory.SUNRISE_SUNSET)) {
                catComp.setMajorFont(size12Font);
            } else {
                catComp.setMajorFont(size10ItalicFont);
            }

            categoryCompMap.put(cat, catComp);
        }

        /*
         * Initialize flags(isDaily, isNWR,includeAll, includeNone) for all
         * composites.
         */
        updateAllCategoryComps();

        // Present the first composite
        switchElementCategory(ClimateReportElementCategory.TEMPERATURE);
    }

    /**
     * Ask confirmation to close this dialog.
     * 
     * @return boolean
     */
    protected boolean closeThisDialog() {
        return MessageDialog.openConfirm(getShell(), "Close?",
                "Are you sure you wish to close? "
                        + "Unsaved changes will be lost.");
    }

    /**
     * Dispose the fonts/colors created in this dialog.
     */
    protected void disposeFontsColors() {
        if (!size14Font.isDisposed()) {
            size14Font.dispose();
        }

        if (!size12Font.isDisposed()) {
            size12Font.dispose();
        }

        if (!size10Font.isDisposed()) {
            size10Font.dispose();
        }

        if (!size8Font.isDisposed()) {
            size8Font.dispose();
        }

        if (!size12ItalicFont.isDisposed()) {
            size12ItalicFont.dispose();
        }

        if (!size10ItalicFont.isDisposed()) {
            size10ItalicFont.dispose();
        }

        if (!size10BoldFont.isDisposed()) {
            size10BoldFont.dispose();
        }
    }

    /**
     * Switch to a composite associated with a report category and update button
     * status in it.
     * 
     * @param rptCat
     *            ClimateReportElementCategory
     */
    protected void switchElementCategory(ClimateReportElementCategory rptCat) {

        // Switch topControl to composite associated with the category
        AbstractCategoryComp catComp = categoryCompMap.get(rptCat);
        subcatStackLayout.topControl = catComp;

        // update the composite
        updateCategoryComp(rptCat);

        subCatContentGrp.layout();

    }

    /**
     * Update current composite with current report source & type
     */
    private void updateCategoryComp(ClimateReportElementCategory rptCat) {

        AbstractCategoryComp catComp = categoryCompMap.get(rptCat);

        // Update all buttons' status on this composite.
        catComp.display(PeriodType.isNWR(reportSource),
                PeriodType.isDaily(reportPeriod),
                includeAllChkBtns.get(rptCat).getSelection(),
                includeNoneChkBtns.get(rptCat).getSelection());

        // Show "Include temp in deg C" button
        if (catComp instanceof TemperatureComp
                && PeriodType.isNWR(reportSource)) {
            includeCelsiusTempBtn.setVisible(true);
        }

        // Layout if it is the current category
        if (rptCat.equals(reportCategory)) {
            catComp.layout(true, true);
        }
    }

    /**
     * Update all category composites when source or period type changes.
     */
    protected void updateAllCategoryComps() {
        for (ClimateReportElementCategory rptCat : categoryCompMap.keySet()) {
            updateCategoryComp(rptCat);
        }

    }

    /**
     * Select all enabled elements.
     * 
     * @param select
     *            boolean true - select; false - de-select
     */
    private void selectAllElements(boolean select) {

        for (ClimateReportElementCategory rptCat : categoryCompMap.keySet()) {

            includeAllChkBtns.get(rptCat).setSelection(select);
            includeNoneChkBtns.get(rptCat).setSelection(!select);

            updateCategoryComp(rptCat);
        }

    }

    /**
     * Save the current product type
     */
    protected void saveProductType() {

        if (!isReadyToSave()) {
            return;
        }

        ClimateProductType currentType = getProductType();

        boolean doSave = true;
        if (cptManager.isTypeOnClimateLocalizationLevel(currentType)) {
            MessageDialog confirmDlg = new MessageDialog(this.getShell(),
                    "Save Product Type", null,
                    "Product type [" + currentType.getFileName()
                            + "] exists. Overwrite?",
                    MessageDialog.CONFIRM, new String[] { "Yes", "No" }, 1);
            confirmDlg.open();

            if (confirmDlg.getReturnCode() != MessageDialog.OK) {
                doSave = false;
            }
        }

        if (doSave) {
            cptManager.saveProductType(currentType);
            // Update "Options" dialog
            updateCurrentPeriodDlg();

            // Give user a confirmation for the save.
            MessageDialog.openInformation(shell, "Saved Product",
                    "Product has been saved sucessfully to "
                            + currentType.getFileName());

            // Enable "Delete Product" button.
            deleteProductBtn.setEnabled(true);

        }

    }

    /**
     * Delete the current product type
     */
    protected void deleteProductType() {

        ClimateProductType ptyp = getProductType();

        if (cptManager.getProductTypeMap().keySet().contains(ptyp.getName())) {
            if (ptyp.getFileName() != null) {
                MessageDialog confirmDlg = new MessageDialog(this.getShell(),
                        "Delete Product Type", null,
                        "Are you sure you want to delete product type ["
                                + ptyp.getName() + "]?",
                        MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);
                confirmDlg.open();

                if (confirmDlg.getReturnCode() == MessageDialog.OK) {
                    cptManager.deleteProductType(ptyp);

                    // Disbale "Delete" button and open with a new default type.
                    deleteProductBtn.setEnabled(false);
                    openNewProductType();

                    // Update "Options" dialog
                    updateCurrentPeriodDlg();

                }
            }
        }
    }

    /**
     * Retrieve current product type settings from GUI.
     * 
     * @return getProductType() ClimateProductType
     */
    private ClimateProductType getProductType() {

        ClimateProductType productType = new ClimateProductType();

        productType.setReportType(reportType);

        String pid = idTxt.getText();
        productType.setProdId(pid);

        productType.setItype(reportType.getValue());

        productType.setFileName(productType.getPreferedFileName());

        productType.setStations(getSelectedStations());

        productType.setHeader(getProductHeader());

        productType.setControl(getProductControl());

        return productType;
    }

    /**
     * Retrieve currently selection stations.
     * 
     * @return getSelectedStations() A list of selected stations
     */
    private java.util.List<Station> getSelectedStations() {

        ArrayList<Station> selectedStations = new ArrayList<>();
        for (Station stn : stationList) {
            for (String stationID : stationNames.getSelection()) {
                if (stn.getIcaoId().equals(stationID)) {
                    selectedStations.add(stn);
                    break;
                }
            }
        }

        return selectedStations;
    }

    /**
     * Retrieve current product header settings set in the GUI.
     * 
     * @return getProductHeader() ClimateProductHeader
     */
    private ClimateProductHeader getProductHeader() {

        ClimateProductHeader productHeader = ClimateProductHeader
                .getDefaultHeader();

        // AFOS product identifier (CCCNNNXXX)
        String nodeStr = nodeTxt.getText();
        if (nodeStr.trim().length() != 3) {
            logger.warn(
                    "Chosen node: [" + nodeStr + "] is not 3 characters long. ["
                            + DEFAULT_NODE + "] will be used instead.");
            nodeStr = DEFAULT_NODE;
        }
        productHeader.setNodeOrigSite(nodeStr);
        productHeader.setProductCategory(nnnLbl.getText());
        productHeader.setThirdN(nnnLbl.getText().substring(2));
        productHeader.setStationId(idTxt.getText());

        // Periodicity
        productHeader
                .setPeriodicity(Integer.parseInt(periodicitySpinner.getText()));

        // Effective time
        productHeader.setEffectiveTime(
                getClimateTime(effectiveTime, effectiveTimeZoneLbl.getText()));
        productHeader.setEffectiveDate(ClimateDate.getLocalDate());

        // Expiration time
        productHeader.setExpirationTime(getClimateTime(expirationTime,
                expirationTimeZoneLbl.getText()));
        productHeader.setExpirationDate(ClimateDate.getLocalDate());

        // Listening area code
        productHeader.setListenAreaCode(lacTxt.getText());

        // Address (NWWS only?)
        productHeader.setAddress(addressTxt.getText());

        // Other, time
        productHeader.setCreationDate(ClimateDate.getLocalDate());
        productHeader.setCreationTime(ClimateTime.getLocalGMTTime());

        // other, WMO header-related
        String nodePrefix = nodePrefixCombo.getText();
        if (nodePrefix.isEmpty()) {
            nodePrefix = DEFAULT_NODE_PREFIX;
            logger.warn("No C prefix was selected, so the default of ["
                    + DEFAULT_NODE_PREFIX + "] will be used.");
        }
        productHeader.setCDE2(nodePrefix);
        productHeader.setCDE3(nodeStr);

        productHeader.setCDE4_Date(ClimateDate.getLocalDate());
        productHeader.setCDE4_Time(ClimateTime.getLocalGMTTime());

        return productHeader;
    }

    /**
     * Build a ClimateTime from a DateTime widget.
     * 
     * @Param wid a DateTime widget
     * @Param zone a time zone string
     * 
     * @return getClimateTime() ClimateTime
     */
    private ClimateTime getClimateTime(DateTime wid, String zone) {
        ClimateTime ctime = new ClimateTime();
        ctime.setHour(wid.getHours());
        ctime.setMin(wid.getMinutes());
        ctime.setAmpm((wid.getHours() >= 12) ? "PM" : "AM");
        ctime.setZone(zone);
        return ctime;
    }

    /**
     * Retrieve current product control flags set in the sub-category GUI.
     * 
     * @return getProductControl() ClimateProductControl
     */
    private ClimateProductControl getProductControl() {

        ClimateProductControl productCtrl = ClimateProductControl
                .getDefaultControl();

        // Flag for "Include degree in C" (only for NWR temperature)
        productCtrl.setDoCelsius(includeCelsiusTempBtn.isVisible()
                && includeCelsiusTempBtn.getSelection());

        // Get HDD/CDD/Snow periods
        productCtrl.setSnowDates(new ClimateDates(snowDates));
        productCtrl.setHeatDates(new ClimateDates(heatDates));
        productCtrl.setCoolDates(new ClimateDates(coolDates));

        // Get flags from composites for each category.
        for (Entry<ClimateReportElementCategory, AbstractCategoryComp> entry : categoryCompMap
                .entrySet()) {
            AbstractCategoryComp catComp = entry.getValue();
            switch (entry.getKey()) {

            case TEMPERATURE:
                productCtrl.setTempControl(
                        ((TemperatureComp) catComp).getControlFlags());
                break;

            case PRECIPITATION:
                productCtrl.setPrecipControl(
                        ((PrecipitationComp) catComp).getControlFlags());
                break;

            case SNOWFALL:
                productCtrl.setSnowControl(
                        ((SnowfallComp) catComp).getControlFlags());
                break;

            case DEGREE_DAYS:
                productCtrl.setDegreeDaysControl(
                        ((DegreeDaysComp) catComp).getControlFlags());
                break;

            case WIND:
                productCtrl
                        .setWindControl(((WindComp) catComp).getControlFlags());
                break;
            case RELATIVE_HUMIDITY:
                productCtrl.setRelHumidityControl(
                        ((RelativeHumidityComp) catComp).getControlFlags());
                break;

            case SKY_COVER:
                productCtrl.setSkycoverControl(
                        ((SkycoverComp) catComp).getControlFlags());
                break;
            case WEATHER:
                productCtrl.setWeatherControl(
                        ((WeatherComp) catComp).getControlFlags());
                break;
            case TEMPRECORD:
                productCtrl.setTempRecordControl(
                        ((TemperatureRecordComp) catComp).getControlFlags());
                break;

            case SUNRISE_SUNSET:
                productCtrl.setSunControl(
                        ((SunriseSunsetComp) catComp).getControlFlags());
                break;

            default:
                // Do nothing
                break;
            }
        }

        return productCtrl;

    }

    /**
     * Verify product ID, stations, LAC have proper info before save.
     * 
     * @return verifyProductInfo() boolean
     */
    private boolean isReadyToSave() {

        boolean readyToSave = true;

        // Check product ID first.
        String idStr = idTxt.getText();
        if (idStr.trim().length() != 3) {

            readyToSave = false;

            MessageDialog confirmDlg = new MessageDialog(this.getShell(),
                    "Product ID", null,
                    "A proper product ID must be designated before you can save the product type.\nIt is the three letter code for the station that is being broadcasted.",
                    MessageDialog.WARNING, new String[] { "Ok" }, 0);
            confirmDlg.open();
        }

        // Check if there are stations selected.
        if (readyToSave && stationNames.getSelectionCount() == 0) {
            readyToSave = false;
            MessageDialog confirmDlg = new MessageDialog(this.getShell(),
                    "Select Stations", null,
                    "At least one station must be selected before you can save the product type.",
                    MessageDialog.WARNING, new String[] { "Ok" }, 0);
            confirmDlg.open();
        }

        // Check listening area code (NWR only)
        if (PeriodType.isNWR(reportSource) && readyToSave) {
            String lacStr = lacTxt.getText();
            if (lacStr.trim().length() != 7 || !lacStr.trim().endsWith("c")) {

                readyToSave = false;

                if (lacStr.trim().length() == 6) {
                    MessageDialog confirmDlg = new MessageDialog(
                            this.getShell(), "Listening Area Code", null,
                            "A proper listening area code must be designated before you can save the product type.\nIt is a seven letter code ending with a lowercase c, such as VAC999c.\nProceed with 'c' attached? ",
                            MessageDialog.CONFIRM,
                            new String[] { "Ok", "Cancel" }, 0);
                    confirmDlg.open();
                    if (confirmDlg.getReturnCode() == MessageDialog.OK) {
                        lacTxt.setText(lacStr.trim() + "c");
                        readyToSave = true;
                    }
                } else {
                    MessageDialog confirmDlg = new MessageDialog(
                            this.getShell(), "Listening Area Code", null,
                            "A proper listening area code must be designated before you can save the product type.\nIt is a seven letter code ending with a lowercase c, such as VAC999c.",
                            MessageDialog.WARNING, new String[] { "Ok" }, 0);
                    confirmDlg.open();
                }
            }
        }

        // Check address - set to default address "000" if no input.
        if (readyToSave) {
            String addStr = addressTxt.getText();
            if (addStr.trim().length() == 0) {
                addressTxt.setText(DEFAULT_ADDRESS);
            }
        }

        return readyToSave;
    }

    /**
     * Start a new product type based on the default product type defined for
     * the current source - NWR or NWWS and current report time type
     * am/im/pm/mon/sea/ann.
     * 
     * Note: the default product type's file name is defined in form of:
     * 
     * product_[am|im|pm|mon|sea|ann]_DEF_{NWR|NWWS].xml
     * 
     * A DEFAULT_PROD_ID ("DEF") is reserved for this purpose.
     */
    protected void openNewProductType() {

        // Find the default product type.
        String typeName = ClimateProductType.buildName(
                reportType.getPeriodName(), reportSource, DEFAULT_PROD_ID);
        ClimateProductType eType = cptManager.getBaseTypeByName(typeName);

        // Open the default product type (if null, nothing changes).
        openProductType(eType);
        /*
         * Use the current site localization as the node to save user some time.
         */
        nodeTxt.setText(DEFAULT_NODE);
        nodePrefixCombo.setText(DEFAULT_NODE_PREFIX);

        // Clear station selections
        stationNames.deselectAll();
    }

    /**
     * Create "Open Existing Product" menu from existing product types.
     *
     * @param openProductMenu
     *            Menu to add menuItems and submenu.
     */
    private void createOpenExistingProductMenu(Menu openProductMenu) {

        // Clear the previous one, if existing
        MenuItem[] menuItems = openProductMenu.getItems();
        for (int ii = 0; ii < menuItems.length; ii++) {
            menuItems[ii].dispose();
        }

        // Create a new one.
        for (String rptPeriod : PeriodType.CLIMATE_PERIODS) {

            // Get all existing product types for this report period.
            java.util.List<ClimateProductType> typeByPeriod = cptManager
                    .getTypesByPeriodDescripter(rptPeriod);

            // Add the existing product types to a submenu.
            MenuItem reportPeriodMenuItem;
            if (typeByPeriod.isEmpty()) {
                reportPeriodMenuItem = new MenuItem(openProductMenu, SWT.PUSH);
            } else {
                reportPeriodMenuItem = new MenuItem(openProductMenu,
                        SWT.CASCADE);

                Menu submenu = new Menu(reportPeriodMenuItem);
                reportPeriodMenuItem.setMenu(submenu);

                for (final ClimateProductType rp : typeByPeriod) {
                    MenuItem subtypeItem = new MenuItem(submenu, SWT.PUSH);
                    subtypeItem.setText(rp.getProdId() + "_"
                            + rp.getReportType().getSource());
                    subtypeItem.setData(rp);
                    subtypeItem.addSelectionListener(new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent ee) {
                            String prevReportPeriod = reportPeriod;

                            openProductType(rp);

                            // Update "Options" dialog
                            updateCurrentPeriodDlg();

                            // Update Category section
                            updateCatogeryForPeriod(prevReportPeriod);

                        }
                    });
                }
            }

            reportPeriodMenuItem.setText("&" + rptPeriod);
        }
    }

    /**
     * Open a product type and apply its settings to GUI.
     * 
     * @param cpt
     *            ClimateProductType to be opened.
     */
    protected void openProductType(ClimateProductType cpt) {

        // Do nothing for null type.
        if (cpt == null) {
            logger.warn("Cannot open null product definition.");
            return;
        }

        // Update source
        reportType = cpt.getReportType();
        reportSource = reportType.getSource();
        reportPeriod = reportType.getPeriodDescriptor();

        boolean isNWR = PeriodType.isNWR(reportSource);
        boolean isDaily = reportType.isDaily();

        sourceBtn[0].setSelection(isNWR);
        sourceBtn[1].setSelection(!isNWR);

        // Update period
        for (Button btn : reportTypeBtn) {
            btn.setSelection(false);
        }

        for (Button btn : reportTypeBtn) {
            if (btn.getText().equals(reportPeriod)) {
                btn.setSelection(true);
                break;
            }
        }

        // Update header info
        setProductHeader(cpt.getHeader());

        createReportConfigInfo(reportConfigComp, reportSource, false);

        // Update nnnLbl for monthly/seasonal/annual.
        if (!isDaily) {
            nnnLbl.setText("CL"
                    + reportType.getPeriodName().substring(0, 1).toUpperCase());
        }

        // Update station selection
        setSelectedStations(cpt.getStations());

        // Update all control buttons
        ClimateProductControl pCtrl = cpt.getControl();

        includeCelsiusTempBtn.setSelection(pCtrl.isDoCelsius());
        for (AbstractCategoryComp catComp : categoryCompMap.values()) {
            catComp.setControlFlags(isNWR, isDaily, pCtrl);
        }

        // Update all composite and show current category's composite.
        updateAllCategoryComps();

        // Update report periods for snow/heat/cool.
        snowDates = new ClimateDates(pCtrl.getSnowDates());
        heatDates = new ClimateDates(pCtrl.getHeatDates());
        coolDates = new ClimateDates(pCtrl.getCoolDates());

        // Enable "Delete Product" button for non-default product type.
        deleteProductBtn.setEnabled(!cpt.getProdId().equals(DEFAULT_PROD_ID));

    }

    /**
     * Open the "Options" dialog for current product period
     * (am/im/pm/mon/sea/ann).
     */
    protected void openCurrentPeriodDlg() {
        // Open the GUI to list all types for current period.
        if (optionsDialog == null) {
            optionsDialog = new CurrentProductTypesDialog(shell, this,
                    cptManager.getTypesByPeriodDescripter(reportPeriod));
        }

        optionsDialog.open();

        // Align at top-left of the parent dialog
        Point parentLoc = optionsDialog.getParent().getLocation();
        int optionsWidth = optionsDialog.getShell().getSize().x;

        optionsDialog.getShell().setLocation(
                new Point(parentLoc.x - optionsWidth - 2, parentLoc.y));

        /*
         * Update the content (report source/type may have changed after first
         * creation)
         */
        optionsDialog.updateProductTypes(reportPeriod,
                cptManager.getTypesByPeriodDescripter(reportPeriod));

    }

    /**
     * Update content in current product types dialog ("Options").
     * 
     */
    protected void updateCurrentPeriodDlg() {

        if (optionsDialog != null && optionsDialog.isOpen()) {
            optionsDialog.updateProductTypes(reportPeriod,
                    cptManager.getTypesByPeriodDescripter(reportPeriod));
        }

    }

    /**
     * Set product header settings on GUI based on the given product header.
     * 
     * @param productHeader
     *            ClimateProductHeader
     */
    private void setProductHeader(ClimateProductHeader productHeader) {

        // Check if this is a "default" product type.
        boolean isDefault = productHeader.getStationId()
                .equals(DEFAULT_PROD_ID);

        // AFOS product identifier (CCCNNNXXX)
        nodePrefixCombo.setText(productHeader.getCDE2());
        nodeTxt.setText(productHeader.getNodeOrigSite());
        nnnLbl.setText(productHeader.getProductCategory());

        idTxt.removeModifyListener(idTxtModifyListner);
        if (isDefault) {
            idTxt.setText("");
        } else {
            idTxt.setText(productHeader.getStationId());
        }
        idTxt.addModifyListener(idTxtModifyListner);

        // Periodicity
        periodicitySpinner.setSelection(productHeader.getPeriodicity());

        // Effective time
        ClimateTime etime = productHeader.getEffectiveTime();
        effectiveTime.setHours(etime.getHour());
        effectiveTime.setMinutes(etime.getMin());
        effectiveTimeZoneLbl.setText(etime.getZone());

        // Expiration time
        ClimateTime expTime = productHeader.getExpirationTime();
        expirationTime.setHours(expTime.getHour());
        expirationTime.setMinutes(expTime.getMin());
        expirationTimeZoneLbl.setText(expTime.getZone());

        // Listening area code
        lacTxt.setText(productHeader.getListenAreaCode());

        // Address (NWWS only?)
        addressTxt.setText(productHeader.getAddress());

    }

    /**
     * Set currently selection stations from a give product type.
     * 
     * @param stations
     *            A list of stations
     */
    private void setSelectedStations(java.util.List<Station> stations) {

        stationNames.deselectAll();

        if (stations != null) {
            int ii = 0;
            for (String stationID : stationNames.getItems()) {
                for (Station stn : stations) {
                    if (stn.getIcaoId().equals(stationID)) {
                        stationNames.select(ii);
                    }
                }

                ii++;
            }
        }
    }

    /**
     * Open one of the editing dialog at the center.
     * 
     * @param dlg
     *            Dialog to be opened
     */
    protected void openEditingDlg(CaveSWTDialog dlg) {
        if (dlg == null || dlg.isDisposed()) {
            logger.error("Cannot open null or disposed dialog.");
        } else {
            dlg.open();

            // Center the dialog based on cave window dimensions & location.
            Point parentLocation = this.getParent().getLocation();
            int parentWidth = this.getParent().getSize().x;
            int parentHeight = this.getParent().getSize().y;

            int dlgWidth = dlg.getShell().getSize().x;
            int dlgHeight = dlg.getShell().getSize().y;
            dlg.getShell()
                    .setLocation(new Point(
                            parentLocation.x + (parentWidth - dlgWidth) / 2,
                            parentLocation.y + (parentHeight - dlgHeight) / 2));
        }
    }

    /**
     * Check/open if an product type exists with current source/period/prodId
     * info on dialog. If found, open it with user confirmation.
     */
    protected void openCurrentProductType() {

        ClimateProductType eType = findCurrentProductType();
        if (eType != null) {
            MessageDialog confirmDlg = new MessageDialog(this.getShell(),
                    "Open Product Type", null,
                    "The Product type [" + eType.getName()
                            + "] already exists. Open it?",
                    MessageDialog.CONFIRM, new String[] { "Ok", "Cancel" }, 1);
            confirmDlg.open();

            if (confirmDlg.getReturnCode() == MessageDialog.OK) {
                openProductType(eType);
            }
        }

        // Enable "Delete Product" button if the product exist.
        deleteProductBtn.setEnabled(eType != null);
    }

    /**
     * Open Reporting Period dialog to edit snow, heat, and cool periods.
     */
    protected void openReportingPeriodDlg() {
        ClimateReportPeriodsDialog reportPeriodDialog = new ClimateReportPeriodsDialog(
                shell, snowDates, heatDates, coolDates);

        openEditingDlg(reportPeriodDialog);

        reportPeriodDialog.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                boolean update = (boolean) reportPeriodDialog.getReturnValue();
                if (update) {
                    snowDates = reportPeriodDialog.getSnowDates();
                    heatDates = reportPeriodDialog.getHeatDates();
                    coolDates = reportPeriodDialog.getCoolDates();
                }
            }
        });
    }

    /**
     * Open ThresholdDialog to edit threshold values.
     */
    protected void openThresholdDlg() {
        ThresholdsDialog thresholdsDialog = new ThresholdsDialog(shell,
                preferenceValues);

        openEditingDlg(thresholdsDialog);

        thresholdsDialog.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                boolean update = (boolean) thresholdsDialog.getReturnValue();
                if (update) {
                    loadPreferences();
                    for (AbstractCategoryComp rptCatComp : categoryCompMap
                            .values()) {
                        rptCatComp.updatePreferences(preferenceValues);
                    }

                }
            }
        });
    }

    /**
     * Load climate preferences from file
     */
    protected void loadPreferences() {
        preferenceValues = ClimateGlobal.getDefaultGlobalValues();

        ClimateRequest cr = new ClimateRequest();
        cr.setRequestType(RequestType.GET_GLOBAL);

        try {
            ClimateGlobal tmpPreferences = (ClimateGlobal) ThriftClient
                    .sendRequest(cr);
            if (tmpPreferences != null) {
                preferenceValues = tmpPreferences;
            } else {
                logger.warn(
                        "ClimateSetupDialog: failed to read preferences, using defaults. ");
            }
        } catch (VizException e) {
            logger.warn(
                    "ClimateSetupDialog: failed to read preferences, using defaults. "
                            + e);
        }
    }

    /**
     * Open StationEditDialog to edit stations.
     */
    protected void openStationEditDlg() {
        StationEditDialog stationEditDialog = new StationEditDialog(shell);

        openEditingDlg(stationEditDialog);

        stationEditDialog.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                boolean update = (boolean) stationEditDialog.getReturnValue();
                updateStations(update);
            }
        });
    }

    /**
     * Load stations from DB.
     */
    @SuppressWarnings("unchecked")
    private void loadStations() {
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        try {
            stationList = (java.util.List<Station>) ThriftClient
                    .sendRequest(request);
        } catch (VizException e) {
            logger.error("ClimateSetupDialog: Could not retrieve stations. ",
                    e);
        }

    }

    /**
     * Update stations.
     * 
     * @param update
     *            Flag to indicate if update is needed.
     */
    private void updateStations(boolean update) {

        if (update) {
            // Save away the current selections
            java.util.List<Station> selectedStations = getSelectedStations();

            // Load stations.
            loadStations();

            // Add all stations to the list
            stationNames.removeAll();
            for (Station station : stationList) {
                stationNames.add(station.getIcaoId());
            }

            // Try to restore the selected ones.
            setSelectedStations(selectedStations);
        }
    }

    /**
     * Find if an product type exists with current source/period/prodId info on
     * dialog.
     * 
     * @return ClimateProductType null if not found.
     */
    private ClimateProductType findCurrentProductType() {

        ClimateProductType eType = null;
        String prdID = idTxt.getText();
        if (PROD_ID_PATTERN.matcher(prdID).matches()) {
            String typeName = ClimateProductType
                    .buildName(reportType.getPeriodName(), reportSource, prdID);
            eType = cptManager.getTypeByName(typeName);

        }

        return eType;
    }

    /**
     * Update the title/layout for category section when the report period
     * changes.
     * 
     * @param prevReportPeriod.
     */
    private void updateCatogeryForPeriod(String prevReportPeriod) {

        if (PeriodType.isPeriod(reportPeriod)) {
            // Update labels
            nnnLbl.setText("CL"
                    + reportType.getPeriodName().substring(0, 1).toUpperCase());
            catListLbl.setText(reportPeriod);

            // Hide aux-category & set category to temperature.
            if (PeriodType.isDaily(prevReportPeriod)) {
                auxCatChkAllNoneComp.setVisible(false);
                auxCatChkAllNoneLbl.setVisible(false);

                for (Button auxButton : auxCategoryList) {
                    auxButton.setSelection(false);
                    auxButton.setVisible(false);
                }

                if (reportCategory.isAuxiliary()) {
                    categoryList.get(0).setSelection(true);
                    reportCategory = ClimateReportElementCategory.TEMPERATURE;
                    switchElementCategory(reportCategory);
                }
            }
        } else {

            // Show aux-category for daily type
            if (PeriodType.isPeriod(prevReportPeriod)) {
                auxCatChkAllNoneComp.setVisible(true);
                auxCatChkAllNoneLbl.setVisible(true);

                for (Button auxButton : auxCategoryList) {
                    auxButton.setVisible(true);
                }
            }

            // Update labels
            nnnLbl.setText("CLI");
            if (PeriodType.isMorning(reportPeriod)) {
                catListLbl.setText("YESTERDAY's");
                auxCatChkAllNoneLbl.setText("TODAY'S");
            } else {
                catListLbl.setText("TODAY'S");
                auxCatChkAllNoneLbl.setText("TOMORROW'S");
                auxCatChkAllNoneLbl.getParent().layout();
            }
        }

        catListLbl.getParent().layout(true);

    }

}