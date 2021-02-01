/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;

/**
 * The class for CWA generator dialog
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------   ----------- --------------------------
 * 12/19/2016   17469       wkwock      Initial Creation.
 * 06/02/2020   75767       wkwock      Migrated from PGEN to NWS
 * 
 * </pre>
 * 
 * @author wkwock
 */

public class CWAProductDlg extends CaveSWTDialog {
    /** the top Composite of this dialog. */
    private Composite top = null;

    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAProductDlg.class);

    /** CWA configuration file */
    private static final String CONFIG_FILE = LocalizationUtil.join("cwsu",
            "cwaGeneratorConfig.xml");

    /** CWA generator configurations */
    private CWAGeneratorConfig cwaConfigs = null;

    /** Text box width */
    private static final int NUM_TEXT_CHARS = 23;

    /** button width */
    private static final int NUM_BUTTON_CHARS = 13;

    /** Maximum number of CWA widgets */
    private static final int MAX_CWA = 6;

    /** CWSU sites */
    private static final String[] sites = { "ZAB", "ZAN", "ZAU", "ZBW", "ZDC",
            "ZDV", "ZFW", "ZHU", "ZID", "ZJX", "ZKC", "ZLA", "ZLC", "ZMA",
            "ZME", "ZMP", "ZNY", "ZOA", "ZOB", "ZSE", "ZTL" };

    /** time label */
    private Label timeLbl;

    /** CSIG composite */
    private Composite csigComp;

    /** normal background color */
    private Color normalBgColor;

    /** practice background color */
    private Color praticeBgColor;

    /** CWA PI Lid */
    private String cwaPiLid = "CWA";

    /** CWS PI Lid */
    private String cwsPiLid = "CWS";

    /** CWA composite */
    private Composite cwaComposite;

    /** CWAs label */
    private Label cwaLbl;

    /** series label */
    private Label seriesLbl;

    /** CWA buttons */
    private Button[] cwaButtons;

    /** CWA texts */
    private Text[] cwaTexts;

    /** MIS composite */
    private Composite misComposite;

    /** MIS label */
    private Label misLbl;

    /** MIS Series Label */
    private Label misSeriesLbl;

    /** MIS button */
    private Button misBtn;

    /** MIS text */
    private Text misTxt;

    /** product view listener */
    private MouseTrackAdapter productViewListener;

    /** formatter dialog listener */
    private SelectionAdapter formaterListener;

    /** CWA formatter dialog */
    private CWAFormatterDlg cwaDlg;

    /** CWS formatter dialog with state IDs and VORs */
    private CWSStateFormatterDlg cwsStateDlg;

    /** CWS default formatter dialog */
    private CWSDefaultFormatterDlg cwsDefaultDlg;

    /** CSIG dialog */
    private CSIGDlg csigDlg;

    /** Report dialog */
    private ReportDlg reportDlg;

    /** bottom composite */
    private Composite bottomComp;

    /** update interval in minutes */
    private Duration updateInterval = Duration.ofMinutes(1);

    private String originalCwsuID = null;

    private CWAToolTip toolTip = null;

    /**
     * constructor for this class.
     * 
     * @param Shell:
     *            parent Shell of this dialog.
     * @throws VizException
     */
    public CWAProductDlg(Shell parShell) {
        super(parShell);
        cwaButtons = new Button[MAX_CWA];

        cwaTexts = new Text[MAX_CWA];
    }

    @Override
    protected void initializeComponents(Shell shell) {
        this.top = new Composite(shell, SWT.NONE);
        this.getShell().setText("CWA Generator");

        prepareColors();
        buildButtonListeners();
        getCWAConfigs();

        top.setLayout(new GridLayout(1, false));

        createTopControls();
        createCWAsControls();
        createMISControls();

        createCSIGButtons();

        createBottomButtons();
        createTimeControl();

        changeModeGui();
        updateProductStatus();
    }

    /**
     * Prepare colors
     */
    private void prepareColors() {
        normalBgColor = top.getBackground();

        praticeBgColor = new Color(top.getDisplay(), 255, 140, 0);

        getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (praticeBgColor != null) {
                    praticeBgColor.dispose();
                }
            }
        });
    }

    /**
     * Build button listeners
     */
    private void buildButtonListeners() {
        productViewListener = new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (e.widget instanceof Button) {
                    quickViewProduct(e);
                }
            }

            @Override
            public void mouseExit(MouseEvent e) {
                if (toolTip != null) {
                    toolTip.close();
                    toolTip = null;
                }

            }
        };

        formaterListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.widget;
                openCWAFormatterDlg(button.getText());
            }
        };
    }

    /**
     * Open CWA formatter dialog
     * 
     * @param productId
     */
    private void openCWAFormatterDlg(String productId) {
        if (cwaDlg != null && cwaDlg.getShell() != null
                && !cwaDlg.getShell().isDisposed()) {
            cwaDlg.getShell().setFocus();
            return;
        }

        String site = productId.substring(productId.length() - 3);
        try {
            cwaDlg = new CWAFormatterDlg(getShell(), site, productId,
                    cwaConfigs, this);
            cwaDlg.setParameters(site, productId, cwaConfigs);
            cwaDlg.open();
        } catch (VizException e) {
            logger.error("Failed to open CWA formatter dialog.", e);
        }
    }

    /**
     * open CWS formatter dialog
     */
    private void openCWSFormatterDlg() {
        if (cwaConfigs.isCwsUseStateIDs()) {
            cwsStateDlg = new CWSStateFormatterDlg(getShell(), misBtn.getText(),
                    cwaConfigs, this);
            cwsStateDlg.open();
        } else {
            cwsDefaultDlg = new CWSDefaultFormatterDlg(getShell(),
                    misBtn.getText(), cwaConfigs, this);
            cwsDefaultDlg.open();
        }
    }

    /**
     * make a quick view on a product
     * 
     * @param product
     */
    private void quickViewProduct(MouseEvent e) {
        Button button = (Button) e.widget;
        Point p = button.getLocation();
        Point p2 = button.toDisplay(p.x + button.getBounds().width,
                button.getBounds().height);

        String productId = cwaConfigs.getAwipsNode() + button.getText();
        CWAProduct cwaProduct = new CWAProduct(productId, "",
                cwaConfigs.isOperational(), null);
        String product = cwaProduct.getProductTxt();
        String message = cwaProduct.getProductTxt();
        String title = "Product: " + productId;
        if (product == null || product.isEmpty()) {
            message = productId;
            title = null;
        }
        if (toolTip == null) {
            toolTip = new CWAToolTip(shell, title, message, p2.x, p2.y);
            toolTip.open();
        }

    }

    /**
     * Create top controls
     */
    private void createTopControls() {
        Composite topComp = new Composite(top, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        topComp.setLayout(gl);

        Label siteLbl = new Label(topComp, SWT.NONE);
        siteLbl.setText("Site:");
        Combo siteCbo = new Combo(topComp, SWT.DROP_DOWN);
        siteCbo.setItems(sites);
        siteCbo.select(0);
        for (int i = 0; i < sites.length; i++) {
            if (sites[i].equalsIgnoreCase(cwaConfigs.getCwsuId())) {
                siteCbo.select(i);
            }
        }

        Label modeLbl = new Label(topComp, SWT.NONE);
        modeLbl.setText("Mode:");
        Combo modeCbo = new Combo(topComp, SWT.DROP_DOWN);
        modeCbo.add("Normal Operational");
        modeCbo.add("Practice");
        if (cwaConfigs.isOperational()) {
            modeCbo.select(0);
        } else {
            modeCbo.select(1);
        }
        modeCbo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                cwaConfigs.setOperational(modeCbo.getSelectionIndex() == 0);
                changeModeGui();
                updateProductStatus();
            }
        });

        siteCbo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                cwaConfigs.setCwsuId(siteCbo.getText());
                cwaConfigs.setKcwsuId("K" + siteCbo.getText());
                int index = modeCbo.getSelectionIndex();
                if (siteCbo.getText().equalsIgnoreCase(originalCwsuID)) {
                    modeCbo.setItem(0, "Normal Operational");
                } else {
                    modeCbo.setItem(0, "Backup Operational");
                }
                // Refresh the text
                modeCbo.select(index);
                changeModeGui();
                updateProductStatus();
            }
        });
    }

    /**
     * Set the PI LIDs
     */
    private void setPiLids() {
        if (cwaConfigs.isOperational()) {
            cwaPiLid = "CWA";
            cwsPiLid = "CWS";
        } else {
            cwaPiLid = "WRK";
            cwsPiLid = "WRK";
        }
    }

    /**
     * Change color and button names base on the mode
     * 
     */
    private void changeModeGui() {
        Color bgColor = praticeBgColor;
        if (cwaConfigs.isOperational()) {
            bgColor = normalBgColor;
        }

        setPiLids();

        top.setBackground(bgColor);
        cwaComposite.setBackground(bgColor);
        cwaLbl.setBackground(bgColor);
        seriesLbl.setBackground(bgColor);
        misComposite.setBackground(bgColor);
        misLbl.setBackground(bgColor);
        misSeriesLbl.setBackground(bgColor);
        csigComp.setBackground(bgColor);
        bottomComp.setBackground(bgColor);

        String site = cwaConfigs.getCwsuId().substring(1);
        for (int i = 0; i < MAX_CWA; i++) {
            cwaButtons[i].setText(cwaPiLid + site + (i + 1));
        }

        misBtn.setText(cwsPiLid + cwaConfigs.getCwsuId());
    }

    /**
     * Create CWAs controls
     */
    private void createCWAsControls() {
        cwaComposite = new Composite(top, SWT.NONE);
        GridLayout cwaGl = new GridLayout(2, false);
        cwaComposite.setLayout(cwaGl);
        GridData cwaGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        cwaComposite.setLayoutData(cwaGd);

        cwaLbl = new Label(cwaComposite, SWT.CENTER);
        GridData cwaLblGd = new GridData();
        GC gc = new GC(cwaLbl);
        int buttonWidth = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * NUM_BUTTON_CHARS);
        cwaLblGd.widthHint = buttonWidth;

        cwaLbl.setLayoutData(cwaLblGd);
        cwaLbl.setText("CWAs");

        seriesLbl = new Label(cwaComposite, SWT.CENTER);
        seriesLbl.setText("series      expire");
        int textWidth = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * NUM_TEXT_CHARS);
        gc.dispose();

        for (int i = 0; i < MAX_CWA; i++) {
            cwaButtons[i] = new Button(cwaComposite, SWT.PUSH);
            GridData cwaBtnGd = new GridData();
            cwaBtnGd.widthHint = buttonWidth;
            cwaButtons[i].setLayoutData(cwaBtnGd);
            cwaButtons[i].addMouseTrackListener(productViewListener);
            cwaButtons[i].addSelectionListener(formaterListener);

            cwaTexts[i] = new Text(cwaComposite, SWT.BORDER);
            GridData cwa1Gd = new GridData();
            cwa1Gd.widthHint = textWidth;

            cwaTexts[i].setLayoutData(cwa1Gd);
            cwaTexts[i].setEditable(false);
        }
    }

    /**
     * create MIS controls
     */
    private void createMISControls() {
        misComposite = new Composite(top, SWT.NONE);
        misComposite.setLayout(new GridLayout(2, false));
        GridData misGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        misComposite.setLayoutData(misGd);

        misLbl = new Label(misComposite, SWT.CENTER);
        GridData misLblGd = new GridData();
        GC gc = new GC(cwaLbl);
        int buttonWidth = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * NUM_BUTTON_CHARS);
        misLblGd.widthHint = buttonWidth;
        misLbl.setLayoutData(misLblGd);
        misLbl.setText("MIS");
        gc.dispose();

        misSeriesLbl = new Label(misComposite, SWT.CENTER);
        misSeriesLbl.setText("series      expire");

        misBtn = new Button(misComposite, SWT.PUSH);
        GridData misBtnGd = new GridData();
        misBtnGd.widthHint = buttonWidth;
        misBtn.setLayoutData(misBtnGd);
        misBtn.addMouseTrackListener(productViewListener);
        misBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openCWSFormatterDlg();
            }
        });

        misTxt = new Text(misComposite, SWT.BORDER);
        misGd = new GridData();
        gc = new GC(cwaLbl);
        misGd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * NUM_TEXT_CHARS);
        misTxt.setLayoutData(misGd);
        misTxt.setEditable(false);
        gc.dispose();
    }

    /**
     * create CSIG buttons
     */
    private void createCSIGButtons() {
        csigComp = new Composite(top, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        gl.marginTop = 10;
        csigComp.setLayout(gl);
        GridData csigGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        csigComp.setLayoutData(csigGd);

        Button csigwBtn = new Button(csigComp, SWT.PUSH);
        csigwBtn.setText("CSIG-W");
        csigwBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openCSIGDlg(cwaConfigs.getAwipsNode() + "SIGW");
            }
        });

        Button csigcBtn = new Button(csigComp, SWT.PUSH);
        csigcBtn.setText("CSIG-C");
        csigcBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openCSIGDlg(cwaConfigs.getAwipsNode() + "SIGC");
            }
        });

        Button csigeBtn = new Button(csigComp, SWT.PUSH);
        csigeBtn.setText("CSIG-E");
        csigeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openCSIGDlg(cwaConfigs.getAwipsNode() + "SIGE");
            }
        });
    }

    /**
     * Open CSIG dialog
     * 
     * @param productId
     */
    private void openCSIGDlg(String productId) {
        if (csigDlg != null && !csigDlg.isDisposed()) {
            if (csigDlg.getText().equals(productId)) {
                csigDlg.bringToTop();
                return;
            }

            csigDlg.close();
        }

        csigDlg = new CSIGDlg(top.getShell(), productId,
                cwaConfigs.isOperational());
        csigDlg.open();
    }

    /**
     * create bottom buttons
     */
    private void createBottomButtons() {
        bottomComp = new Composite(top, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        gl.marginTop = 10;
        gl.marginBottom = 10;
        bottomComp.setLayout(gl);
        GridData bottomCompGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        bottomComp.setLayoutData(bottomCompGd);
        bottomComp.setBackground(top.getBackground());

        Button refreshBtn = new Button(bottomComp, SWT.PUSH);
        refreshBtn.setText("Refresh");
        refreshBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                updateProductStatus();
            }
        });

        Button reportBtn = new Button(bottomComp, SWT.PUSH);
        reportBtn.setText("Report");
        reportBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openReportDlg();
            }
        });

        Button exitBtn = new Button(bottomComp, SWT.PUSH);
        exitBtn.setText("Exit");
        exitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

    }

    /**
     * Open report dialog
     * 
     */
    private void openReportDlg() {
        if (reportDlg != null && !reportDlg.isDisposed()) {
            reportDlg.bringToTop();
            return;
        }

        reportDlg = new ReportDlg(top.getShell(), cwaConfigs.isOperational());
        reportDlg.open();
    }

    /**
     * Get CWA product configurations
     */
    private void getCWAConfigs() {
        LocalizationLevel[] levels = new LocalizationLevel[] {
                LocalizationLevel.USER, LocalizationLevel.SITE,
                LocalizationLevel.REGION, LocalizationLevel.BASE };

        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile configFile = null;

        for (int i = 0; i < levels.length; i++) {
            LocalizationContext lc = pm
                    .getContext(LocalizationType.COMMON_STATIC, levels[i]);

            configFile = pm.getLocalizationFile(lc, CONFIG_FILE);
            if (configFile.exists()) {
                break;
            }
        }

        if (configFile == null || !configFile.exists()) {
            MessageBox messageBox = new MessageBox(shell, SWT.OK);
            messageBox.setText("Missing configuration file");
            messageBox.setMessage(
                    "Failed to find configuration file " + CONFIG_FILE);
            messageBox.open();
            return;
        }

        try {
            SingleTypeJAXBManager<CWAGeneratorConfig> jaxb = new SingleTypeJAXBManager<>(
                    CWAGeneratorConfig.class);
            cwaConfigs = jaxb
                    .unmarshalFromInputStream(configFile.openInputStream());
        } catch (LocalizationException | SerializationException
                | JAXBException e) {
            logger.error("Failed to get localization for CWA Generator.", e);
            return;
        }

        cwaConfigs.filterVolcanoStations();
        originalCwsuID = cwaConfigs.getCwsuId();

        boolean found = false;
        String badID = originalCwsuID;
        for (String site : sites) {
            if (site.equalsIgnoreCase(originalCwsuID)) {
                found = true;
                break;
            }
        }

        if (found) {
            if (!cwaConfigs.getKcwsuId()
                    .equalsIgnoreCase("K" + originalCwsuID)) {
                found = false;
                badID = cwaConfigs.getKcwsuId();
            }
        }
        if (!found) {
            MessageBox messageBox = new MessageBox(getShell(),
                    SWT.YES | SWT.NO);
            messageBox.setText("Invalid CWSU SITE Name");
            String message = "CWSU site name " + badID
                    + " is invalid. Continue?";
            messageBox.setMessage(message);
            int buttonId = messageBox.open();
            if (buttonId == SWT.NO) {
                close();
            }
        }
    }

    /**
     * Create Time Control
     */
    private void createTimeControl() {
        timeLbl = new Label(top, SWT.CENTER);
        GridData timeGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        GC gc = new GC(timeLbl);
        timeGd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 21);
        timeLbl.setLayoutData(timeGd);
        updateTimeLbl();
        gc.dispose();

        UIJob timeLblJob = new UIJob(this.getDisplay(), "time label") {
            public IStatus runInUIThread(IProgressMonitor monitor) {
                updateTimeLbl();
                schedule(1000);
                return Status.OK_STATUS;
            }
        };

        timeLblJob.setPriority(Job.INTERACTIVE);
        timeLblJob.schedule();

        UIJob productStatusJob = new UIJob(this.getDisplay(),
                "product status") {
            public IStatus runInUIThread(IProgressMonitor monitor) {
                updateProductStatus();
                schedule(updateInterval.toMillis());
                return Status.OK_STATUS;
            }
        };

        productStatusJob.setPriority(Job.INTERACTIVE);
        productStatusJob.schedule(updateInterval.toMillis());
    }

    /**
     * update time label
     */
    private void updateTimeLbl() {
        if (!timeLbl.isDisposed()) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss");
            Calendar cal = TimeUtil.newGmtCalendar();
            timeLbl.setText(sdf.format(cal.getTime()) + "Z");
        }
    }

    /**
     * update Product status
     */
    public void updateProductStatus() {
        for (int i = 0; i < MAX_CWA; i++) {
            CWAProduct cwaProduct = new CWAProduct(
                    cwaConfigs.getAwipsNode() + cwaButtons[i].getText(),
                    cwaConfigs.getCwsuId(), cwaConfigs.isOperational(), null);
            cwaTexts[i].setEditable(true);
            cwaTexts[i].setText(
                    cwaProduct.getSeries() + "      " + cwaProduct.getExpire());

            cwaTexts[i].setForeground(cwaProduct.getColor());
        }

        CWAProduct cwaProduct = new CWAProduct(
                cwaConfigs.getAwipsNode() + misBtn.getText(),
                cwaConfigs.getCwsuId(), cwaConfigs.isOperational(), null);
        misTxt.setText(
                cwaProduct.getSeries() + "      " + cwaProduct.getExpire());
        misTxt.setForeground(cwaProduct.getColor());

    }

}
