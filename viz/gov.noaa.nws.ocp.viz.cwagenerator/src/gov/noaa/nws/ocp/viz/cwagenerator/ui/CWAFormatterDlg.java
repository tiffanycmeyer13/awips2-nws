/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
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
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.viz.ui.UiUtil;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.viz.common.SnapUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.action.IUpdateFormatter;
import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.action.CWAGeneratorResource;
import gov.noaa.nws.ocp.viz.cwagenerator.action.CWAGeneratorResourceData;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsDrawingTool;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsSelectHandler;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsSelectingTool;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.BlduBlsaNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CancelManualNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.IcingFrzaNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.IfrLifrNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ThunderstormNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.TurbLlwsNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.LineDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PolygonDrawable;

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
 * 06/27/2021  92561    wkwock      Fix the multiple panel resource clean up issue
 * 09/10/2021  28802    wkwock      Remove PGEN dependence
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CWAFormatterDlg extends CaveSWTDialog implements IUpdateFormatter {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAFormatterDlg.class);

    private VorDrawingComp vorDrawingComp = null;

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

    private CWAProductDlg owner;

    private CWAGeneratorResource cwaResource;

    protected AbstractEditor mapEditor = null;

    public CWAFormatterDlg(Shell parShell, String site, String productId,
            CWAGeneratorConfig cwaConfigs, CWAProductDlg owner) {
        super(parShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        this.site = site;
        this.productId = productId;
        this.cwaConfigs = cwaConfigs;
        this.owner = owner;
        activateCWAResource();
        cwaResource.setOwnerDlg(this);
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
    protected void preOpened() {
        setParameters(site, productId, cwaConfigs);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new GridLayout(1, false));
        vorDrawingComp = new VorDrawingComp(shell, cwaResource, this);
        createFormatterArea();
        createButtonBar(shell);
    }

    /**
     * clear drawings
     */
    public void clearDrawings() {
        // clear all drawings
        cwaResource.clearDrawings();
    }

    /**
     * save VORs to file
     */
    public void saveVORs() {
        Control control = tabFolder.getItem(tabFolder.getSelectionIndex())
                .getControl();
        if (!(control instanceof AbstractCWAComp)) {
            return;
        }
        AbstractCWAComp cwaComp = (AbstractCWAComp) control;
        AbstractCWANewConfig config = cwaComp.getConfig();
        config.setStartTimeChk(cwaComp.getStartTimeChk());
        config.setStartTime(cwaComp.getStartTime());
        config.setEndTime(cwaComp.getEndTime());
        config.setProductTxt(productTxt.getText());
        config.setRoutine(routineRdo.getSelection());
        config.setAuthor(System.getProperty("user.name"));
        config.setTime(TimeUtil.newGmtCalendar().toString());

        // save VORs
        config.setDrawable(cwaResource.getDrawable());

        SaveVorsDlg saveVorsDlg = new SaveVorsDlg(getShell(),
                productId + cwaComp.getWeatherName(), config);
        saveVorsDlg.open();
    }

    public void openVORs() {
        // read product configurations
        CWAProductNewConfig tmpProductconfig = null;
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
        cwaResource.setProductConfigs(tmpProductconfig);
        changeMouseMode(false);
        cwaResource.setEditable(true);
        mapEditor.refresh();
    }

    private void createFormatterArea() {
        tabFolder = new TabFolder(shell, SWT.TOP | SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
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
        if (currentTabIndex != newIndex && !productTxt.getText().isEmpty()) {
            // ask if user wants to change tab
            MessageBox messageBox = new MessageBox(getShell(),
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

    /**
     * create the product text box
     */
    private void createProductTextBox() {
        productTxt = new StyledText(shell,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
        gd.heightHint = productTxt.getLineHeight() * 10;
        GC gc = new GC(productTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 90);
        productTxt.setLayoutData(gd);
        gc.dispose();

        productTxt.addVerifyListener((VerifyEvent e) -> {
            // lock the 1st four lines
            if (!allowMod && (productTxt.getLineAtOffset(e.start) < 4
                    || productTxt.getLineAtOffset(e.end) < 4)) {
                e.doit = false;
            }
        });
    }

    /**
     * Create bottom buttons
     */
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

    public CWAGeneratorResource createNewResource() {
        CWAGeneratorResource drawingLayer = null;
        AbstractEditor editor = CWAGeneratorUtil.getActiveEditor();
        if (editor != null) {
            try {
                CWAGeneratorResourceData rscData = new CWAGeneratorResourceData();
                drawingLayer = new CWAGeneratorResource(rscData,
                        new LoadProperties());
                for (IDisplayPane pane : editor.getDisplayPanes()) {
                    IDescriptor idesc = pane.getDescriptor();
                    if (!idesc.getResourceList().isEmpty()) {
                        idesc.getResourceList().add(drawingLayer);
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
        cwaResource = createNewResource();
        mapEditor = CWAGeneratorUtil.getActiveEditor();
        drawTool = new VorsDrawingTool();
        drawTool.setEnabled(true);
        drawTool.setDrawingLayer(cwaResource);
        drawTool.setMapEditor(mapEditor);

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
        if (vorDrawingComp.getDrawingType() != DrawingType.AREA
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
        if (fromline != null) {
            fromline = fromline.replaceAll("(\\r|\\n)", "");
        }

        double tmpWidth = vorDrawingComp.getWidth();
        String body = "LINE ..." + tmpWidth + " NM WIDE...";
        if (vorDrawingComp.getDrawingType() == DrawingType.AREA
                || (vorDrawingComp.getDrawingType() == DrawingType.LINE
                        && tmpWidth >= 20.0)) {
            body = "AREA";
        } else if (vorDrawingComp.getDrawingType() == DrawingType.ISOLATED) {
            // Remove from text with ISOL IFA point
            body = "ISOL DIAM " + tmpWidth + "NM";
        }

        String productStr = "";
        List<PointLatLon> points = cwaResource.getCoordinates();
        String stateIDStr = "";
        if (points != null && !points.isEmpty()) {
            Coordinate[] coors = new Coordinate[points.size()];
            for (int i = 0; i < points.size(); i++) {
                coors[i] = new Coordinate(points.get(i).getLat(),
                        points.get(i).getLon());
            }
            stateIDStr = CWAGeneratorUtil.getStates(coors,
                    vorDrawingComp.getDrawingType());
        }
        if (control instanceof AbstractCWAComp) {
            AbstractCWAComp cwaComp = (AbstractCWAComp) control;
            productStr = cwaComp.createText(wmoHeader, header, fromline, body,
                    cwaConfigs.getCwsuId(), retrievalProductId,
                    corRdo.getSelection(), cwaConfigs.isOperational(),
                    vorDrawingComp.getDrawingType(), tmpWidth, stateIDStr);
        }

        setProductText(productStr);
    }

    /**
     * get from line
     * 
     * @return from line
     */
    private String getFromLine() {
        String fromline = getEditableAttrFromLine();

        if (vorDrawingComp.getDrawingType() != DrawingType.ISOLATED) {
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
                .append(" VALID UNTIL ").append(endDateTime).append("\n");
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
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK);
            messageBox.setText("No Product to Save");
            messageBox.setMessage("There's no product to save.");
            messageBox.open();
            return;
        } else {
            MessageBox messageBox = new MessageBox(getShell(),
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
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK);
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

    public void updateFormatter(AbstractDrawableComponent drawable) {
        AbstractCWANewConfig config = this.cwaResource
                .getSelectedConfig(drawable);
        if (config == null) {
            return;
        }

        AbstractCWAComp cwaComp = null;
        if (config instanceof BlduBlsaNewConfig) {
            tabFolder.setSelection(blduTab);
            cwaComp = blduComp;
        } else if (config instanceof CancelManualNewConfig) {
            tabFolder.setSelection(cancelTab);
            cwaComp = cancelComp;
        } else if (config instanceof IcingFrzaNewConfig) {
            tabFolder.setSelection(icingTab);
            cwaComp = icingComp;
        } else if (config instanceof IfrLifrNewConfig) {
            tabFolder.setSelection(ifrTab);
            cwaComp = ifrComp;
        } else if (config instanceof ThunderstormNewConfig) {
            tabFolder.setSelection(thunderTab);
            cwaComp = thunderComp;
        } else if (config instanceof TurbLlwsNewConfig) {
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

        if (config.getDrawable() instanceof PolygonDrawable) {
            vorDrawingComp.setDrawingType(DrawingType.AREA);
            cwaResource.setDrawType(DrawingType.AREA);
        } else if (config.getDrawable() instanceof LineDrawable) {
            vorDrawingComp.setDrawingType(DrawingType.LINE);
            LineDrawable lineDrawable = (LineDrawable) config.getDrawable();
            vorDrawingComp.setWidth(lineDrawable.getWidth());
            cwaResource.setDrawType(DrawingType.LINE);
        } else {
            vorDrawingComp.setDrawingType(DrawingType.ISOLATED);
            cwaResource.setDrawType(DrawingType.ISOLATED);
        }

        setProductText(config.getProductTxt());
        routineRdo.setSelection(config.isRoutine());
        corRdo.setSelection(!config.isRoutine());
    }

    /**
     * Change the mouse mode: draw VORs mode or select VORs mode
     * 
     * @param isDrawMode
     */
    public void changeMouseMode(boolean isDrawMode) {
        if (isDrawMode) {// draw mode
            if (selectTool != null) {
                IInputHandler mouseHandler = selectTool.getMouseHandler();
                mapEditor.unregisterMouseHandler(mouseHandler);
            }
            mapEditor.registerMouseHandler(drawTool.getMouseHandler());
            cwaResource.setEditable(true);
        } else {// select mode
            mapEditor.unregisterMouseHandler(drawTool.getMouseHandler());
            if (selectTool == null) {
                selectTool = new VorsSelectingTool(mapEditor, cwaResource,
                        this);
                selectTool.setDrawingLayer(cwaResource);
                IInputHandler selectHandler = new VorsSelectHandler(mapEditor,
                        cwaResource, this);
                selectTool.setHandler(selectHandler);
                selectTool.activate();
            }
            mapEditor.registerMouseHandler(selectTool.getMouseHandler());
        }
    }

    @Override
    protected void disposed() {
        if (drawTool != null) {
            mapEditor.unregisterMouseHandler(drawTool.getMouseHandler());
            drawTool.dispose();
        }
        if (selectTool != null) {
            mapEditor.unregisterMouseHandler(selectTool.getMouseHandler());
        }

        cwaResource.dispose();
        mapEditor.refresh();

        for (IRenderableDisplay display : UiUtil
                .getDisplaysFromContainer(mapEditor)) {
            for (ResourcePair rp : display.getDescriptor().getResourceList()) {
                if (rp.getResource() instanceof CWAGeneratorResource) {
                    CWAGeneratorResource rsc = (CWAGeneratorResource) rp
                            .getResource();
                    rsc.unload();
                }
            }
        }
    }

    private String getEditableAttrFromLine() {
        List<PointLatLon> points = cwaResource.getCoordinates();
        if (points == null || points.isEmpty()) {
            return "";
        }
        Coordinate[] coors = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            coors[i] = new Coordinate(points.get(i).getLat(),
                    points.get(i).getLon());
        }
        return SnapUtil.getVORText(coors, "-",
                vorDrawingComp.getDrawingType().typeName, 6, false, true, true);
    }
}
