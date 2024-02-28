/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
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
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;

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
 * Sep 10, 2021 28802      wkwock      Remove PGEN dependence
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWSStateFormatterDlg extends CaveSWTDialog
        implements IUpdateFormatter {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWSStateFormatterDlg.class);

    private VorDrawingComp vorDrawingComp = null;

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

    private CWAProductDlg owner;

    private CWAGeneratorResource cwaResource;

    protected AbstractEditor mapEditor = null;

    public CWSStateFormatterDlg(Shell parShell, String productId,
            CWAGeneratorConfig cwaConfigs, CWAProductDlg owner) {
        super(parShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
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
        productTxt.setEditable(cwaConfigs.isEditable());
        getShell().setText("MIS/CWS Formatter - Site: " + cwaConfigs.getCwsuId()
                + " - Product: " + productId);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new GridLayout(1, false));
        vorDrawingComp = new VorDrawingComp(shell, cwaResource, this);
        stateComp = new CWSStateIDComp(shell, cwaConfigs);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        stateComp.setLayoutData(gd);
        createProductTextBox();
        createButtonsForButtonBar(shell);
    }

    /**
     * save VORs to file
     */
    public void saveVORs() {
        AbstractCWANewConfig config = stateComp.getConfig();
        config.setStartTimeChk(stateComp.getStartTimeChk());
        config.setStartTime(stateComp.getStartTime());
        config.setEndTime(stateComp.getEndTime());
        config.setProductTxt(productTxt.getText());
        config.setRoutine(routineRdo.getSelection());
        config.setAuthor(System.getProperty("user.name"));
        config.setTime(TimeUtil.newGmtCalendar().toString());

        // save VORs
        config.setDrawable(cwaResource.getDrawable());

        SaveVorsDlg saveVorsDlg = new SaveVorsDlg(getShell(),
                productId + stateComp.getWeatherName(), config);
        saveVorsDlg.open();
    }

    public void openVORs() {
        // read product configurations
        CWAProductNewConfig tmpProductconfig = null;
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

        cwaResource.setProductConfigs(tmpProductconfig);
        changeMouseMode(false);
        cwaResource.setEditable(true);
        mapEditor.refresh();
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
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        composite.setLayoutData(gd);

        composite.setLayout(new GridLayout(6, false));

        routineRdo = new Button(composite, SWT.RADIO);
        routineRdo.setText("Rtn");
        routineRdo.setSelection(true);

        corRdo = new Button(composite, SWT.RADIO);
        corRdo.setText("COR");
        corRdo.setSelection(false);

        Button newBtn = new Button(composite, SWT.PUSH);
        newBtn.setText("Create New Text");
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewText();
            }
        });

        Button previousBtn = new Button(composite, SWT.PUSH);
        previousBtn.setText("Use Previous CWA Text");
        previousBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPreviousText();
            }
        });

        Button sendBtn = new Button(composite, SWT.PUSH);
        sendBtn.setText("Send Text");
        sendBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sendProduct();
            }
        });

        Button exitBtn = new Button(composite, SWT.PUSH);
        exitBtn.setText("Exit");
        exitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        this.open();
    }

    public CWAGeneratorResource createNewResource() {
        CWAGeneratorResource drawingLayer = null;
        AbstractEditor editor = CWAGeneratorUtil.getActiveEditor();
        if (editor != null) {
            try {
                CWAGeneratorResourceData rscData = new CWAGeneratorResourceData();

                for (IDisplayPane pane : editor.getDisplayPanes()) {
                    IDescriptor idesc = pane.getDescriptor();
                    if (!idesc.getResourceList().isEmpty()) {
                        drawingLayer = new CWAGeneratorResource(rscData,
                                new LoadProperties());
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
        if (vorDrawingComp.getDrawingType() == DrawingType.LINE) {
            body = "AREA..." + (int) getWidth() + " NM WIDE...";
        } else if (vorDrawingComp.getDrawingType() == DrawingType.ISOLATED) {
            // Remove from text with ISOL IFA point
            fromline = getEditableAttrFromLine();
            body = "ISOL...DIAM " + (int) getWidth() + "NM...";
        }

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

        String productStr = stateComp.createText(wmoHeader, header, fromline,
                body, cwaConfigs.getCwsuId(), retrievalProductId,
                corRdo.getSelection(), cwaConfigs.isOperational(),
                vorDrawingComp.getDrawingType(), vorDrawingComp.getWidth(),
                stateIDStr);

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
            MessageBox messageBox = new MessageBox(shell.getShell(), SWT.OK);
            messageBox.setText("No Product to Save");
            messageBox.setMessage("There's no product to save.");
            messageBox.open();
            return;
        } else {
            MessageBox messageBox = new MessageBox(shell.getShell(),
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
            MessageBox messageBox = new MessageBox(shell.getShell(), SWT.OK);
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

    public double getWidth() {
        return this.vorDrawingComp.getWidth();
    }

    public void updateFormatter(AbstractDrawableComponent drawable) {
        AbstractCWANewConfig config = this.cwaResource
                .getSelectedConfig(drawable);
        if (config == null) {
            return;
        }

        stateComp.updateProductConfig(config);
        setProductText(config.getProductTxt());
        routineRdo.setSelection(config.isRoutine());
        corRdo.setSelection(!config.isRoutine());
    }

    @Override
    public void clearDrawings() {
        // clear all drawings
        cwaResource.clearDrawings();
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
