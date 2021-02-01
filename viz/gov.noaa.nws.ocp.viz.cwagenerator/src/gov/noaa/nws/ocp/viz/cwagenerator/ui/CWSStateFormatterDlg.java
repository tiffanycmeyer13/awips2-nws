/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
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
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResourceData;
import gov.noaa.nws.ncep.ui.pgen.sigmet.Sigmet;
import gov.noaa.nws.ncep.viz.common.ui.color.ColorButtonSelector;
import gov.noaa.nws.ocp.viz.cwagenerator.action.IUpdateFormatter;
import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.action.CWAResource;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsDrawingTool;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsSelectHandler;
import gov.noaa.nws.ocp.viz.cwagenerator.action.VorsSelectingTool;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWAConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * 
 * Class for CWS/MIS formatter with VORs and state IDs
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 1, 2020  75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWSStateFormatterDlg extends SigmetCommAttrDlg
        implements IUpdateFormatter {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWSStateFormatterDlg.class);

    private Composite top = null;

    public static final String AREA = "Area";

    public static final String LINE = "Line";

    public static final String ISOLATED = "Isolated";

    public static final String LINE_SEPERATER = ":::";

    private String lineType = AREA;

    private String origLineType = lineType;

    private static final String WIDTH = "10.00";

    private String width = WIDTH;

    protected ColorButtonSelector cs = null;

    /** product text field */
    private StyledText productTxt;

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

    private CWSStateIDComp stateComp;

    /** allow the product text modification */
    private boolean allowMod = false;

    /** ttaaii for CWS products */
    private String ttaaii = "FAUS20";

    private VorsDrawingTool drawTool;

    private VorsSelectingTool selectTool;

    private Map<Product, AbstractCWAConfig> productList = new HashMap<>();

    private CWAProductDlg owner;

    public CWSStateFormatterDlg(Shell parShell, String productId,
            CWAGeneratorConfig cwaConfigs, CWAProductDlg owner) {
        super(parShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        this.retrievalProductId = cwaConfigs.getAwipsNode() + productId;
        this.productId = cwaConfigs.getKcwsuId() + productId;
        if (!cwaConfigs.isOperational()) {
            this.productId = cwaConfigs.getAwipsNode() + productId;
        }
        this.cwaConfigs = cwaConfigs;

        this.owner = owner;

        if (cwaConfigs.getCwsuId().equalsIgnoreCase("ZAN")) {
            ttaaii = "FAAK20 ";
        }
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
        productTxt.setEditable(cwaConfigs.isEditable());
        getShell().setText("MIS/CWS Formatter - Site: " + cwaConfigs.getCwsuId()
                + " - Product: " + productId);
    }

    @Override
    public Control createDialogArea(Composite parent) {
        top = parent;
        top.setLayout(new GridLayout(1, false));
        createTopBar();
        stateComp = new CWSStateIDComp(parent, cwaConfigs);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        stateComp.setLayoutData(gd);
        createProductTextBox();
        return top;
    }

    private void createTopBar() {
        Composite topBar = new Composite(top, SWT.NONE);
        GridLayout mainLayout = new GridLayout(10, false);
        topBar.setLayout(mainLayout);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        topBar.setLayoutData(gd);

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
            }
        });

        lineBttn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                widthTxt.setEnabled(true);

                setLineType(LINE + LINE_SEPERATER + getSideOfLine());
            }
        });

        isolatedBttn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                widthTxt.setEnabled(true);

                setLineType(ISOLATED);
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
        gd = new GridData();
        gd.horizontalIndent = 40;
        clearBttn.setLayoutData(gd);
        clearBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawingLayer.removeAllProducts();
                drawingLayer.removeAllActiveDEs();
                mapEditor.refresh();
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
     * save VORs to file
     */
    private void saveVORs() {
        AbstractCWAConfig config = stateComp.getConfig();
        config.setStartTimeChk(stateComp.getStartTimeChk());
        config.setStartTime(stateComp.getStartTime());
        config.setEndTime(stateComp.getEndTime());
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
                productId + stateComp.getWeatherName(), config);
        saveVorsDlg.open();
    }

    private void openVORs() {
        // read product configurations
        CWAProductConfig tmpProductconfig = null;
        String fileName = CWAGeneratorUtil.CWS_PRODUCT_CONFIG_FILE;
        if (!this.cwaConfigs.isOperational()) {
            fileName = CWAGeneratorUtil.CWS_PRACTICE_PRODUCT_CONFIG_FILE;
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
        if (getEditableAttrFromLine().isEmpty()) {
            MessageBox msgBox = new MessageBox(this.getShell(), SWT.OK);
            msgBox.setText("Empty EditableAttrFromLine");
            msgBox.setMessage(
                    "No VORs Drawn. Please draw the VORs and try again.");
            msgBox.open();
        }

        String wmoHeader = ttaaii + " " + cwaConfigs.getKcwsuId();
        String header = cwaConfigs.getCwsuId()
                + productId.substring(productId.length() - 1) + " CWA";

        String fromline = "FROM " + getEditableAttrFromLine();
        String body = "AREA OF ";
        if (getLineType().equals(SigmetCommAttrDlg.LINE)) {
            body = "AREA..." + (int) getWidth() + " NM WIDE...";
        } else if (getLineType().equals(SigmetCommAttrDlg.ISOLATED)) {
            // Remove from text with ISOL IFA point
            fromline = getEditableAttrFromLine();
            body = "ISOL...DIAM " + (int) getWidth() + "NM...";
        }

        String stateIDStr = CwaUtil
                .getStates(getAbstractSigmet().getLinePoints(), getLineType());
        String productStr = stateComp.createText(wmoHeader, header, fromline,
                body, cwaConfigs.getCwsuId(), retrievalProductId,
                corRdo.getSelection(), cwaConfigs.isOperational(),
                getLineType(), Double.parseDouble(width), stateIDStr);

        setProductText(productStr);

    }

    /**
     * Create a new product with previous product
     */
    private void createPreviousText() {
        // Get start and end times.
        Date date = stateComp.getDateTime();
        SimpleDateFormat sdfDate = new SimpleDateFormat("ddHHmm");
        String startDateTime = sdfDate.format(date);

        String endDateTime = stateComp.getEndTime();

        StringBuilder output = new StringBuilder();

        String wmoHeader = ttaaii + cwaConfigs.getKcwsuId();

        // Load previous CWA in to memory. The old header will
        // not be used. Operational products have two header lines.
        // WRK products omit the WMO header information.
        CWAProduct cwaProduct = new CWAProduct(retrievalProductId,
                cwaConfigs.getCwsuId(), cwaConfigs.isOperational(),
                WeatherType.MIS);
        String[] lines = cwaProduct.getProductTxt().split("\n");

        // Extract Site ID and CWA number. Re-use previous number
        // if this is a correction.
        int seriesId = 1;
        if (lines.length > 2) {
            String[] items = lines[1].split("\\s+");
            try {
                seriesId = Integer.parseInt(items[2]);
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse " + lines[2], nfe);
            }
        }
        String seriesStr = String.format("%02d", seriesId);

        String body2 = null;
        String body3 = null;
        String body4 = "";

        if (lines.length > 4) {
            body2 = lines[3];
        }
        if (lines.length > 5) {
            body3 = lines[4];
        }
        if (lines.length > 6) {
            body4 = lines[5];
        }

        output.append(wmoHeader).append(" ").append(startDateTime).append("\n");
        output.append(cwaConfigs.getCwsuId()).append(" MIS ").append(seriesStr)
                .append(" VALID ");
        output.append(startDateTime).append("-").append(endDateTime)
                .append("Z");
        if (this.corRdo.getSelection()) {
            output.append(" COR");
        }
        if (body2 != null) {
            output.append(body2).append("\n");
        }
        if (body3 != null) {
            output.append(body3).append("\n");
        }
        output.append(body4).append("\n=\n");

        setProductText(output.toString());
    }

    /**
     * Set product text
     * 
     * @param text
     */
    void setProductText(String text) {
        allowMod = true;
        try {
            productTxt.setText(text);
        } finally {
            allowMod = false;
        }
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
            } else {
                messageBox.setMessage(
                        "Failed to distribute product " + productId + ".");
            }
            messageBox.open();
        } else {
            saveProductForReport(product);
        }

        owner.updateProductStatus();
    }

    private void saveProductForReport(String productTxt) {
        List<ProductConfig> productXMLList = null;
        try {
            productXMLList = CWAGeneratorUtil.readProductsXML(
                    cwaConfigs.isOperational(), cwaConfigs.getRetainInDays());
        } catch (LocalizationException | SerializationException | IOException
                | JAXBException e) {
            logger.error("Failed to read product XML.", e);
        }

        if (productXMLList == null) {
            productXMLList = new ArrayList<>();
        }
        ProductConfig productXml = new ProductConfig();
        productXml.setAuthor(System.getProperty("user.name"));
        productXml.setTime(TimeUtil.newGmtCalendar().getTimeInMillis());
        productXml.setProductTxt(productTxt);
        productXml.setWeatherName(stateComp.getWeatherName());
        productXMLList.add(productXml);

        try {
            CWAGeneratorUtil.writeProductsXML(cwaConfigs.isOperational(),
                    productXMLList);
        } catch (LocalizationException | SerializationException | IOException
                | JAXBException e) {
            logger.error("Failed to save product XML", e);
        }
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
        return Double.parseDouble(this.width);
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

        stateComp.updateProductConfig(config);
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
                IInputHandler abc = selectTool.getMouseHandler();
                mapEditor.unregisterMouseHandler(abc);
            }
            mapEditor.registerMouseHandler(drawTool.getMouseHandler());
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
        if (drawTool != null) {
            mapEditor.unregisterMouseHandler(drawTool.getMouseHandler());
        }
        if (selectTool != null) {
            mapEditor.unregisterMouseHandler(selectTool.getMouseHandler());
        }
        drawTool.dispose();
        drawingLayer.dispose();
        mapEditor.refresh();

        return super.close();
    }
}
