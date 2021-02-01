/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.awt.Color;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.commands.ICommandService;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.viz.ui.UiUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.ui.pgen.PgenConstant;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.SigmetCommAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;
import gov.noaa.nws.ncep.ui.pgen.file.ProductConverter;
import gov.noaa.nws.ncep.ui.pgen.file.Products;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceData;
import gov.noaa.nws.ncep.ui.pgen.sigmet.AbstractSigmet;
import gov.noaa.nws.ncep.ui.pgen.sigmet.Sigmet;
import gov.noaa.nws.ncep.viz.common.ui.color.ColorButtonSelector;
import gov.noaa.nws.ocp.viz.cwagenerator.action.IUpdateFormatter;
import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.action.CWAResource;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsDrawingTool;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsSelectHandler;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsSelectingTool;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWAConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.BlduBlsaConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CancelManualConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.IcingFrzaConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.IfrLifrConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ThunderstormConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.TurbLlwsConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the CWA formatter dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 11/21/2016  17469    wkwock      Initial creation
 * 05/23/2018  17469    wkwock      Fix practice mode issue
 * 06/02/2020  75767    wkwock      Migrated from PGEN to NWS
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CWAFormatterDlg extends SigmetCommAttrDlg
        implements IUpdateFormatter {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAFormatterDlg.class);

    private Composite top = null;

    public static final String AREA = "Area";

    public static final String LINE = "Line";

    public static final String ISOLATED = "Isolated";

    public static final String LINE_SEPERATER = ":::";

    private String lineType = AREA;

    private String lastLineType = null;

    private String origLineType = lineType;

    private static final String WIDTH = "10.00";

    private String width = WIDTH;

    protected ColorButtonSelector cs = null;

    /** product text field */
    private StyledText productTxt;

    /** tab folder */
    private TabFolder tabFolder;

    /** Thunderstorm composite */
    private ThunderstormComp thunderComp;

    /** IFR/LIFR composite */
    private IfrLifrComp ifrComp;

    /** turbulence composite */
    private TurbLlwsComp turbulenceComp;

    /** icing composite */
    private IcingFrzaComp icingComp;

    /** Blowing dust/blowing sand composite */
    private BlduBlsaComp blduComp;

    /** volcano composite */
    private VolcanoComp volcanoComp;

    /** cancel/manual composite */
    private CancelManualComp cancelComp;

    private TabItem thunderTab;

    private TabItem ifrTab;

    private TabItem turbulenceTab;

    private TabItem icingTab;

    private TabItem blduTab;

    private TabItem volcanoTab;

    private TabItem cancelTab;

    /** routine radio button */
    private Button routineRdo;

    /** correction radio button */
    private Button corRdo;

    /** site */
    private String site;

    /** product ID */
    private String productId;

    /** product ID for retrieval from textDB */
    private String retrievalProductId;

    /** CWA configurations */
    private CWAGeneratorConfig cwaConfigs;

    /** current tab index */
    private int currentTabIndex = 0;

    /** allow the product text modification */
    private boolean allowMod = false;

    /** ttaaii for CWA products */
    private String ttaaii = "FAUS2";

    private VorsDrawingTool drawTool;

    private VorsSelectingTool selectTool;

    private Map<Product, AbstractCWAConfig> productList = new HashMap<>();

    private CWAProductDlg owner;

    public CWAFormatterDlg(Shell parShell, String site, String productId,
            CWAGeneratorConfig cwaConfigs, CWAProductDlg owner)
            throws VizException {
        super(parShell);
        this.site = site;
        this.productId = productId;
        this.cwaConfigs = cwaConfigs;
        this.owner = owner;
        setShellStyle(SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        activateCWAResource();
    }

    /**
     * Set parameters
     * 
     * @param site
     * @param productId
     * @param cwaConfigs
     */
    public void setParameters(String site, String productId,
            CWAGeneratorConfig cwaConfigs) {
        this.site = site;
        this.retrievalProductId = cwaConfigs.getAwipsNode() + productId;
        this.productId = cwaConfigs.getKcwsuId() + productId;
        if (!cwaConfigs.isOperational()) {
            this.productId = cwaConfigs.getAwipsNode() + productId;
        }
        this.cwaConfigs = cwaConfigs;
        volcanoComp.updateVolcano(cwaConfigs);
        cancelComp.updateCwsuId(site);
        getShell().setText("CWA Formatter - Site: " + cwaConfigs.getCwsuId()
                + " - Product: " + productId);
        productTxt.setEditable(cwaConfigs.isEditable());

        if (cwaConfigs.getCwsuId().equalsIgnoreCase("ZAN")) {
            ttaaii = "FAAK2";
        }
    }

    @Override
    public Control createDialogArea(Composite parent) {
        top = parent;
        top.setLayout(new GridLayout(1, false));
        createTopBar();
        createFormatterArea();
        return top;
    }

    private void createTopBar() {
        Composite topBar = new Composite(top, SWT.NONE);
        GridLayout mainLayout = new GridLayout(10, false);
        topBar.setLayout(mainLayout);

        Button areaBttn = new Button(topBar, SWT.RADIO);
        areaBttn.setText("Area");
        setLineType(AREA);
        areaBttn.setSelection(true);

        Button lineBttn = new Button(topBar, SWT.RADIO);
        lineBttn.setText("Line");

        Button isolatedBttn = new Button(topBar, SWT.RADIO);
        isolatedBttn.setText("Isolated  ");

        Label widthLbl = new Label(topBar, SWT.LEFT);
        widthLbl.setText("Width: ");
        Text widthTxt = new Text(topBar, SWT.SINGLE | SWT.BORDER);
        widthTxt.setText("10.00");
        widthTxt.setEnabled(false);

        areaBttn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                widthTxt.setEnabled(false);

                setLineType(AREA);
                if (!AREA.equals(lastLineType)) {
                    clearDrawings();
                }
                lastLineType = AREA;
            }
        });

        lineBttn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                widthTxt.setEnabled(true);

                setLineType(LINE + LINE_SEPERATER + getSideOfLine());
                if (!LINE.equals(lastLineType)) {
                    clearDrawings();
                }
                lastLineType = LINE;
            }
        });

        isolatedBttn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                widthTxt.setEnabled(true);

                setLineType(ISOLATED);
                if (!ISOLATED.equals(lastLineType)) {
                    clearDrawings();
                }
                lastLineType = ISOLATED;
            }
        });

        widthTxt.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                setWidth(widthTxt.getText());
            }
        });

        Label colorLbl = new Label(topBar, SWT.LEFT);
        colorLbl.setText("Color:");

        cs = new ColorButtonSelector(topBar);
        Color clr = Color.yellow;
        cs.setColorValue(new RGB(clr.getRed(), clr.getGreen(), clr.getBlue()));

        Button clearBttn = new Button(topBar, SWT.PUSH);
        clearBttn.setText("Clear");
        GridData gd = new GridData();
        gd.horizontalIndent = 40;
        clearBttn.setLayoutData(gd);
        clearBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearDrawings();
                changeMouseMode(true);// draw VORs mode
            }
        });

        Button saveBttn = new Button(topBar, SWT.PUSH);
        saveBttn.setText("Save");
        saveBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveVORs();
            }
        });
        Button openBttn = new Button(topBar, SWT.PUSH);
        openBttn.setText("Open");
        openBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openVORs();
            }
        });
    }

    /**
     * clear drawings
     */
    private void clearDrawings() {
        drawingLayer.removeAllProducts();
        drawingLayer.removeAllActiveDEs();
        drawingLayer.resetAllElements();
        mapEditor.refresh();
    }

    /**
     * save VORs to file
     */
    private void saveVORs() {
        Control control = tabFolder.getItem(tabFolder.getSelectionIndex())
                .getControl();
        if (!(control instanceof AbstractCWAComp)) {
            return;
        }
        AbstractCWAComp cwaComp = (AbstractCWAComp) control;
        AbstractCWAConfig config = cwaComp.getConfig();
        config.setStartTimeChk(cwaComp.getStartTimeChk());
        config.setStartTime(cwaComp.getStartTime());
        config.setEndTime(cwaComp.getEndTime());
        config.setProductTxt(productTxt.getText());
        config.setRoutine(routineRdo.getSelection());
        config.setAuthor(System.getProperty("user.name"));
        config.setTime(TimeUtil.newGmtCalendar().toString());

        // save VORs
        ArrayList<Product> prds = new ArrayList<>();
        prds.add(drawingLayer.getActiveProduct());
        Products products = ProductConverter.convert(prds);
        config.setVorProduct(products);

        SaveVorsDlg saveVorsDlg = new SaveVorsDlg(getShell(),
                productId + cwaComp.getWeatherName(), config);
        saveVorsDlg.open();
    }

    private void openVORs() {
        // read product configurations
        CWAProductConfig tmpProductconfig = null;
        String fileName = CWAGeneratorUtil.CWA_PRODUCT_CONFIG_FILE;
        if (!this.cwaConfigs.isOperational()) {
            fileName = CWAGeneratorUtil.CWA_PRACTICE_PRODUCT_CONFIG_FILE;
        }

        try {
            tmpProductconfig = CWAGeneratorUtil
                    .readProductConfigurations(fileName);
        } catch (SerializationException | LocalizationException | IOException
                | JAXBException e) {
            logger.error("Failed to read product configurations.", e);
        }

        // display VORs product
        productList.clear();
        if (tmpProductconfig != null) {
            for (AbstractCWAConfig config : tmpProductconfig.getCwaProducts()) {
                java.util.List<Product> prds = ProductConverter
                        .convert(config.getVorProduct());
                addProduct(prds);
                for (Product product : prds) {
                    productList.put(product, config);
                }
            }
        }

        changeMouseMode(false);
        drawingLayer.setEditable(true);
        mapEditor.refresh();
    }

    private void addProduct(java.util.List<Product> prds) {
        PgenResourceData prd = drawingLayer.getResourceData();
        if (prd.removeEmptyDefaultProduct()) {
            if (prds != null && !prds.isEmpty()) {
                prd.getProductList().clear();
            }
        }

        // Find the active Product.
        int index = -1;
        if (!prd.getProductList().isEmpty()) {
            index = prd.getProductList().indexOf(prd.getActiveProduct());
        }

        // Append all products
        prd.getProductList().addAll(prds);

        /*
         * Set active product and layer to start layering control.
         */
        if (index < 0) {
            prd.setActiveProduct(prd.getProductList().get(0));
            prd.setActiveLayer(prd.getProductList().get(0).getLayer(0));
        }
    }

    private void createFormatterArea() {
        tabFolder = new TabFolder(top, SWT.TOP | SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent event) {
                changeTab();
            }
        });

        thunderTab = new TabItem(tabFolder, SWT.NONE);
        thunderTab.setText("Thunderstorm");
        thunderComp = new ThunderstormComp(tabFolder, cwaConfigs);
        thunderTab.setControl(thunderComp);

        ifrTab = new TabItem(tabFolder, SWT.NONE);
        ifrTab.setText("IFR/LIFR");
        ifrComp = new IfrLifrComp(tabFolder, cwaConfigs);
        ifrTab.setControl(ifrComp);

        turbulenceTab = new TabItem(tabFolder, SWT.NONE);
        turbulenceTab.setText("Turb/LLWS");
        turbulenceComp = new TurbLlwsComp(tabFolder, cwaConfigs);
        turbulenceTab.setControl(turbulenceComp);

        icingTab = new TabItem(tabFolder, SWT.NONE);
        icingTab.setText("Icing/FRZA");
        icingComp = new IcingFrzaComp(tabFolder, cwaConfigs);
        icingTab.setControl(icingComp);

        blduTab = new TabItem(tabFolder, SWT.NONE);
        blduTab.setText(WeatherType.BLDUBLSA.getName());
        blduComp = new BlduBlsaComp(tabFolder, cwaConfigs);
        blduTab.setControl(blduComp);

        volcanoTab = new TabItem(tabFolder, SWT.NONE);
        volcanoTab.setText("Volcano");
        volcanoComp = new VolcanoComp(tabFolder, cwaConfigs);
        volcanoTab.setControl(volcanoComp);

        cancelTab = new TabItem(tabFolder, SWT.NONE);
        cancelTab.setText("Can/Man");
        cancelComp = new CancelManualComp(tabFolder, cwaConfigs);
        cancelTab.setControl(cancelComp);

        createProductTextBox();
    }

    /**
     * Change tab action
     */
    private void changeTab() {
        int newIndex = tabFolder.getSelectionIndex();
        if (currentTabIndex != newIndex) {
            if (!productTxt.getText().isEmpty()) {
                MessageBox messageBox = new MessageBox(top.getShell(),
                        SWT.YES | SWT.NO);
                messageBox.setText(
                        "Moving to " + tabFolder.getItem(newIndex).getText());
                messageBox.setMessage(
                        "Move to " + tabFolder.getItem(newIndex).getText()
                                + "?\nProduct will be lost.");
                int buttonId = messageBox.open();
                if (buttonId == SWT.YES) {
                    setProductText("");
                    currentTabIndex = newIndex;
                } else {
                    tabFolder.setSelection(currentTabIndex);
                }
            }
        }
    }

    /**
     * create the product text box
     */
    private void createProductTextBox() {
        productTxt = new StyledText(top,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
        gd.heightHint = productTxt.getLineHeight() * 10;
        GC gc = new GC(productTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 90);
        productTxt.setLayoutData(gd);
        gc.dispose();

        productTxt.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // lock the 1st four lines
                if (!allowMod && (productTxt.getLineAtOffset(e.start) < 4
                        || productTxt.getLineAtOffset(e.end) < 4)) {
                    e.doit = false;
                }
            }
        });
    }

    /**
     * Create bottom buttons
     */
    @Override
    public void createButtonsForButtonBar(Composite parent) {
        parent.setLayout(new GridLayout(6, false));

        routineRdo = new Button(parent, SWT.RADIO);
        routineRdo.setText("Rtn");
        routineRdo.setSelection(true);

        corRdo = new Button(parent, SWT.RADIO);
        corRdo.setText("COR");
        corRdo.setSelection(false);

        Button newBtn = new Button(parent, SWT.PUSH);
        newBtn.setText("Create New Text");
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewText();
            }
        });

        Button previousBtn = new Button(parent, SWT.PUSH);
        previousBtn.setText("Use Previous CWA Text");
        previousBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPreviousText();
            }
        });

        Button sendBtn = new Button(parent, SWT.PUSH);
        sendBtn.setText("Send Text");
        sendBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sendProduct();
            }
        });

        Button exitBtn = new Button(parent, SWT.PUSH);
        exitBtn.setText("Exit");
        exitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        this.open();
    }

    @Override
    public Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        composite.setLayoutData(gd);
        composite.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }

    public CWAResource createNewResource() {
        CWAResource drawingLayer = null;
        AbstractEditor editor = PgenUtil.getActiveEditor();
        if (editor != null) {
            try {
                PgenResourceData rscData = new PgenResourceData();

                for (IDisplayPane pane : editor.getDisplayPanes()) {
                    IDescriptor idesc = pane.getDescriptor();
                    if (!idesc.getResourceList().isEmpty()) {
                        drawingLayer = new CWAResource(rscData,
                                new LoadProperties());
                        idesc.getResourceList().add(drawingLayer);
                        idesc.getResourceList()
                                .addPreRemoveListener(drawingLayer);
                        drawingLayer.init(pane.getTarget());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get a CWA resource", e);
            }
        }
        return drawingLayer;
    }

    private void activateCWAResource() {
        drawingLayer = createNewResource();
        mapEditor = PgenUtil.getActiveEditor();
        drawTool = new VorsDrawingTool();
        drawTool.setEnabled(true);
        drawTool.setDrawingLayer(drawingLayer);
        drawTool.setMapEditor(mapEditor);
        //
        // Get the commandId for this item
        String commandId = "gov.noaa.nws.ncep.ui.pgen.rsc.PgenSelect";
        IEditorPart part = VizWorkbenchManager.getInstance().getActiveEditor();
        ICommandService service = (ICommandService) part.getSite()
                .getService(ICommandService.class);
        Command cmd = service.getCommand(commandId);

        if (cmd != null) {

            // Set up information to pass to the AbstractHandler
            HashMap<String, Object> params = new HashMap<>();
            params.put("editor", part);
            params.put(PgenConstant.NAME, "CONV_SIGMET");
            params.put(PgenConstant.CLASSNAME, "SIGMET");
            ExecutionEvent exec = new ExecutionEvent(cmd, params, null,
                    "Select");

            try {
                drawTool.execute(exec);
            } catch (ExecutionException e) {
                logger.error(
                        "Failed to set PGEN drawing mode for the current map",
                        e);
            }
        }

        drawTool.setAttrDlg(this);
        drawTool.activate();
        mapEditor.registerMouseHandler(drawTool.getMouseHandler());
        mapEditor.refresh();
    }

    /**
     * create new product text
     */
    private void createNewText() {
        Control control = tabFolder.getItem(tabFolder.getSelectionIndex())
                .getControl();

        // https://www.nws.noaa.gov/directives/sym/pd01008003curr.pdf
        // page 20 section 8. Area only for these 3 products.
        if (!getLineType().equals(SigmetCommAttrDlg.AREA)
                && ((control instanceof IfrLifrComp)
                        || (control instanceof TurbLlwsComp)
                        || (control instanceof IcingFrzaComp))) {
            MessageBox msgBox = new MessageBox(this.getShell(), SWT.OK);
            msgBox.setText("Area Only");
            msgBox.setMessage(
                    "IFR/LIFR, Turb/LLWS, and Icing/FRZA defines location as an area only. Please select Area at top and draw a polygon.");
            msgBox.open();
            return;
        }

        if (getEditableAttrFromLine().isEmpty()) {
            MessageBox msgBox = new MessageBox(this.getShell(), SWT.OK);
            msgBox.setText("Empty EditableAttrFromLine");
            msgBox.setMessage(
                    "No VORs Drawn. Please draw the VORs and try again.");
            msgBox.open();
            return;
        }

        String wmoHeader = ttaaii + productId.substring(productId.length() - 1)
                + " " + cwaConfigs.getKcwsuId();
        String header = cwaConfigs.getCwsuId()
                + productId.substring(productId.length() - 1) + " CWA";

        String fromline = getFromLine();

        double tmpWidth = Double.parseDouble(width);
        String body = "LINE ..." + tmpWidth + " NM WIDE...";
        if (getLineType().equals(SigmetCommAttrDlg.AREA)
                || (getLineType().startsWith(SigmetCommAttrDlg.LINE)
                        && tmpWidth >= 20.0)) {
            body = "AREA";
        } else if (getLineType().equals(SigmetCommAttrDlg.ISOLATED)) {
            // Remove from text with ISOL IFA point
            body = "ISOL DIAM " + tmpWidth + "NM";
        }

        String productStr = "";
        String stateIDStr = CwaUtil
                .getStates(getAbstractSigmet().getLinePoints(), getLineType());
        if (control instanceof AbstractCWAComp) {
            AbstractCWAComp cwaComp = (AbstractCWAComp) control;
            productStr = cwaComp.createText(wmoHeader, header, fromline, body,
                    cwaConfigs.getCwsuId(), retrievalProductId,
                    corRdo.getSelection(), cwaConfigs.isOperational(),
                    getLineType(), tmpWidth, stateIDStr);
        }

        setProductText(productStr);
    }

    /**
     * get from line
     * 
     * @return from line
     */
    private String getFromLine() {
        String fromline = "";
        double tmpWidth = Double.parseDouble(width);
        if (getLineType().equals(SigmetCommAttrDlg.AREA)
                || (getLineType().startsWith(SigmetCommAttrDlg.LINE)
                        && tmpWidth >= 20.0)) {
            fromline = getEditableAttrFromLine();
        } else {

            // for ISOLATED and LINE remove the last repeated vertices
            fromline = getEditableAttrFromLine();
            String[] item = fromline.split("-");
            if (item[0].equals(item[item.length - 1])) {
                fromline = fromline.substring(0, fromline.lastIndexOf("-"));
            }
        }

        if (!getLineType().equals(SigmetCommAttrDlg.ISOLATED)) {
            // NWSI 10-803: The third line ... starts with the word “FROM”
            // except when
            // the location is defined by a single point.
            fromline = "FROM " + fromline;
        }

        return fromline;
    }

    /**
     * create previous product text
     */
    private void createPreviousText() {
        Control control = tabFolder.getItem(tabFolder.getSelectionIndex())
                .getControl();
        if (!(control instanceof AbstractCWAComp)) {
            return;
        }

        AbstractCWAComp cwaComp = (AbstractCWAComp) control;

        String startDateTime = cwaComp.getStartTime();
        String endDateTime = cwaComp.getEndTime();

        StringBuilder output = new StringBuilder();

        String wmoHeader = ttaaii + productId.substring(productId.length() - 1)
                + " " + cwaConfigs.getKcwsuId();
        String header = cwaConfigs.getCwsuId()
                + productId.substring(productId.length() - 1) + " CWA";

        CWAProduct cwaProduct = new CWAProduct(retrievalProductId,
                cwaConfigs.getCwsuId(), cwaConfigs.isOperational(), null);
        String[] lines = cwaProduct.getProductTxt().split("\n");

        // Extract Site ID and CWA number. Re-use previous number
        // if this is a correction.
        int seriesId = 1;
        if (lines.length > 2) {
            String[] items = lines[2].split("\\s+");
            if (items.length > 3) {
                try {
                    seriesId = Integer.parseInt(items[2]);
                } catch (NumberFormatException nfe) {
                    logger.error("Failed to parse " + lines[2], nfe);
                }
            }
        }

        StringBuilder body = new StringBuilder();
        for (int index = 3; index < lines.length; index++) {
            body.append(lines[index]).append("\n");
        }

        output.append(wmoHeader).append(" ").append(startDateTime).append("\n");
        output.append(header).append(" ").append(startDateTime);
        if (corRdo.getSelection()) {
            output.append(" COR");
        }
        output.append("\n");
        output.append(cwaConfigs.getCwsuId()).append(" CWA ").append(seriesId)
                .append(" VALID UNTIL ").append(endDateTime).append("Z\n");
        output.append(body);

        setProductText(output.toString());
    }

    /**
     * Set product text
     * 
     * @param text
     */
    void setProductText(String text) {
        allowMod = true;
        // Note: Request from user, product text should be modifiable for now
        // but not in the future.
        // try {
        // productTxt.setText(text);
        // } finally {
        // allowMod = false;
        // }

        productTxt.setText(text);
    }

    /**
     * save and send product
     */
    private void sendProduct() {
        String product = productTxt.getText();
        if (product.trim().isEmpty()) {
            MessageBox messageBox = new MessageBox(top.getShell(), SWT.OK);
            messageBox.setText("No Product to Save");
            messageBox.setMessage("There's no product to save.");
            messageBox.open();
            return;
        } else {
            MessageBox messageBox = new MessageBox(top.getShell(),
                    SWT.YES | SWT.NO);
            messageBox.setText("Save Product");
            String message = "Save product " + productId + " to textdb?";
            if (cwaConfigs.isOperational()) {
                message += "\nAnd distribute to DEFAULTNCF?";
            }
            messageBox.setMessage(message);
            int buttonId = messageBox.open();
            if (buttonId != SWT.YES) {
                return;
            }
        }

        CWAProduct cwaProduct = new CWAProduct(productId,
                cwaConfigs.isOperational());
        cwaProduct.setProductTxt(product);
        boolean success = cwaProduct.sendText(site);
        if (cwaConfigs.isOperational()) {
            MessageBox messageBox = new MessageBox(top.getShell(), SWT.OK);
            messageBox.setText("Product Distribution");
            if (success) {
                messageBox.setMessage(
                        "Product " + productId + " successfully distributed.");
                saveProductForReport(product);
                messageBox.open();
                close();
            } else {
                messageBox.setMessage(
                        "Failed to distribute product " + productId + ".");
                messageBox.open();
            }
        } else {
            saveProductForReport(product);
            close();
        }

        owner.updateProductStatus();
    }

    private void saveProductForReport(String productTxt) {
        List<ProductConfig> productConfigList = null;
        try {
            productConfigList = CWAGeneratorUtil.readProductsXML(
                    cwaConfigs.isOperational(), cwaConfigs.getRetainInDays());
        } catch (LocalizationException | SerializationException | IOException
                | JAXBException e) {
            logger.error("Failed to read product XML.", e);
        }

        if (productConfigList == null) {
            productConfigList = new ArrayList<>();
        }
        ProductConfig productConfig = new ProductConfig();
        productConfig.setAuthor(System.getProperty("user.name"));
        productConfig.setTime(TimeUtil.newGmtCalendar().getTimeInMillis());
        productConfig.setProductTxt(productTxt);
        Control control = tabFolder.getItem(tabFolder.getSelectionIndex())
                .getControl();
        if (!(control instanceof AbstractCWAComp)) {
            return;
        }
        AbstractCWAComp cwaComp = (AbstractCWAComp) control;
        productConfig.setWeatherName(cwaComp.getWeatherName());
        productConfigList.add(productConfig);

        try {
            CWAGeneratorUtil.writeProductsXML(cwaConfigs.isOperational(),
                    productConfigList);
        } catch (LocalizationException | SerializationException | IOException
                | JAXBException e) {
            logger.error("Failed to save product XML", e);
        }
    }

    @Override
    public void copyEditableAttrToSigmetAttrDlg(AbstractSigmet sig) {
        this.setEditableAttrArea(sig.getEditableAttrArea());
        this.setEditableAttrFromLine(sig.getEditableAttrFromLine());
        this.setEditableAttrId(sig.getEditableAttrId());
        this.setEditableAttrSequence(sig.getEditableAttrSeqNum());
        this.setLineType(sig.getType());
    }

    @Override
    public Color[] getColors() {

        Color[] colors = new Color[2];
        colors[0] = new java.awt.Color(cs.getColorValue().red,
                cs.getColorValue().green, cs.getColorValue().blue);
        colors[1] = Color.green;

        return colors;
    }

    private void setColor(Color clr) {
        cs.setColorValue(new RGB(clr.getRed(), clr.getGreen(), clr.getBlue()));
    }

    @Override
    public String getLineType() {
        return this.lineType;
    }

    public void setLineType(String lType) {
        setOrigLineType(getLineType());
        this.lineType = lType;
    }

    public String getOrigLineType() {
        return this.origLineType;
    }

    public void setOrigLineType(String lType) {
        this.origLineType = lType;
    }

    @Override
    public double getWidth() {
        return Double.parseDouble(this.width) / 2.0;
    }

    public void setWidth(String widthString) {
        this.width = widthString;
    }

    @Override
    public void setAttrForDlg(IAttribute attr) {
        Color clr = attr.getColors()[0];
        if (clr != null) {
            this.setColor(clr);
        }
    }

    @Override
    public String getPatternName() {
        return null;
    }

    @Override
    public int getSmoothFactor() {
        return 0;
    }

    @Override
    public Boolean isClosedLine() {
        return null;
    }

    @Override
    public Boolean isFilled() {
        return null;
    }

    @Override
    public FillPattern getFillPattern() {
        return null;
    }

    @Override
    public Coordinate[] getLinePoints() {
        return null;
    }

    public void updateFormatter(Product selectedProduct) {
        AbstractCWAConfig config = productList.get(selectedProduct);
        if (config == null) {
            return;
        }

        AbstractCWAComp cwaComp = null;
        if (config instanceof BlduBlsaConfig) {
            tabFolder.setSelection(blduTab);
            cwaComp = blduComp;
        } else if (config instanceof CancelManualConfig) {
            tabFolder.setSelection(cancelTab);
            cwaComp = cancelComp;
        } else if (config instanceof IcingFrzaConfig) {
            tabFolder.setSelection(icingTab);
            cwaComp = icingComp;
        } else if (config instanceof IfrLifrConfig) {
            tabFolder.setSelection(ifrTab);
            cwaComp = ifrComp;
        } else if (config instanceof ThunderstormConfig) {
            tabFolder.setSelection(thunderTab);
            cwaComp = thunderComp;
        } else if (config instanceof TurbLlwsConfig) {
            tabFolder.setSelection(turbulenceTab);
            cwaComp = turbulenceComp;
        } else {// VolcanoConfig
            tabFolder.setSelection(volcanoTab);
            cwaComp = volcanoComp;
        }

        try {
            cwaComp.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse product time.");
        }

        setProductText(config.getProductTxt());
        routineRdo.setSelection(config.isRoutine());
        corRdo.setSelection(!config.isRoutine());
        if (!selectedProduct.getLayers().isEmpty()) {
            Layer layer = selectedProduct.getLayer(0);
            AbstractDrawableComponent element = layer.getElement(0);
            if (element instanceof Sigmet) {
                Sigmet sigmet = (Sigmet) element;
                setEditableAttrFromLine(sigmet.getEditableAttrFromLine());
            }
        }
    }

    /**
     * Change the mouse mode: draw VORs mode or select VORs mode
     * 
     * @param isDrawMode
     */
    private void changeMouseMode(boolean isDrawMode) {
        if (isDrawMode) {// draw mode
            if (selectTool != null) {
                IInputHandler mouseHandler = selectTool.getMouseHandler();
                mapEditor.unregisterMouseHandler(mouseHandler);
            }
            mapEditor.registerMouseHandler(drawTool.getMouseHandler());
            drawingLayer.setEditable(true);
        } else {// select mode
            mapEditor.unregisterMouseHandler(drawTool.getMouseHandler());
            if (selectTool == null) {
                selectTool = new VorsSelectingTool(mapEditor, drawingLayer,
                        this);
                selectTool.setDrawingLayer(drawingLayer);
                IInputHandler selectHandler = new VorsSelectHandler(mapEditor,
                        drawingLayer, this);
                selectTool.setHandler(selectHandler);
                selectTool.activate();
            }
            mapEditor.registerMouseHandler(selectTool.getMouseHandler());
        }
    }

    @Override
    public boolean close() {
        drawingLayer.removeAllProducts();
        drawingLayer.removeAllActiveDEs();
        drawingLayer.resetAllElements();
        drawingLayer.deactivatePgenTools();
        if (drawTool != null) {
            mapEditor.unregisterMouseHandler(drawTool.getMouseHandler());
        }
        if (selectTool != null) {
            mapEditor.unregisterMouseHandler(selectTool.getMouseHandler());
        }

        drawTool.dispose();
        drawingLayer.dispose();
        mapEditor.refresh();

        for (IRenderableDisplay display : UiUtil
                .getDisplaysFromContainer(mapEditor)) {
            for (ResourcePair rp : display.getDescriptor().getResourceList()) {
                if (rp.getResource() instanceof PgenResource) {
                    PgenResource rsc = (PgenResource) rp.getResource();
                    rsc.unload();
                    display.getDescriptor().getResourceList()
                            .removePreRemoveListener(rsc);
                }
            }
        }
        return super.close();
    }
}
