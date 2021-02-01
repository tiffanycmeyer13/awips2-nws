/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the CWS formatter dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 05/23/2018  17469    wkwock      Fix practice mode issue
 * 06/03/2020  75767    wkwock      Moved from PGEN to NWS
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CWSDefaultFormatterDlg extends CaveSWTDialog {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWSDefaultFormatterDlg.class);

    /** product text */
    private StyledText productTxt;

    /** routine radio button */
    private Button routineRdo;

    /** correction radio button */
    private Button corRdo;

    /** start time check button */
    private Button startTimeChk;

    /** day Spinner */
    private Spinner daySpinner;

    /** start time */
    private DateTime startTime;

    /** duration combo */
    private Combo durationCbo;

    /** cancel check button */
    private Button cancelChk;

    /** no update check button */
    private Button noUpdateChk;

    /** CWA generator configurations */
    private CWAGeneratorConfig cwaConfigs;

    /** product ID */
    private String productId;

    /** product ID to retrieve product from textDB */
    private String retrievalProductId;

    /** allow the product text modification */
    private boolean allowMod = false;

    /** ttaaii for CWS products */
    private String ttaaii = "FAUS20 ";

    /** base calendar */
    private Calendar baseCalendar = TimeUtil.newGmtCalendar();

    private Composite defaultComp;

    private CWAProductDlg owner;

    /**
     * Constructor
     * 
     * @param parShell
     * @param productId
     * @param cwaConfigs
     */
    public CWSDefaultFormatterDlg(Shell parShell, String productId,
            CWAGeneratorConfig cwaConfigs, CWAProductDlg owner) {
        super(parShell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        this.retrievalProductId = cwaConfigs.getAwipsNode() + productId;
        this.productId = cwaConfigs.getKcwsuId() + productId;
        if (!cwaConfigs.isOperational()) {
            this.productId = cwaConfigs.getAwipsNode() + productId;
        }
        this.cwaConfigs = cwaConfigs;
        this.owner = owner;
        setText("MIS/CWS Formatter - Site: " + cwaConfigs.getCwsuId()
                + " - Product: " + productId);

        if (cwaConfigs.getCwsuId().equalsIgnoreCase("ZAN")) {
            ttaaii = "FAAK20 ";
        }

    }

    @Override
    protected void initializeComponents(Shell shell) {
        createDefaultTab();
        createProductTextBox();
        createBottomButtons();
        this.preOpened();
    }

    private void createStartTimeComp(Composite parent) {
        Composite timeComp = new Composite(parent, SWT.NONE);
        timeComp.setLayout(new GridLayout(5, false));
        GridData timeGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        timeComp.setLayoutData(timeGd);

        // Start time
        startTimeChk = new Button(timeComp, SWT.CHECK);
        startTimeChk.setText("Start Time:");
        daySpinner = new Spinner(timeComp, SWT.BORDER);
        daySpinner.setMinimum(1);
        daySpinner.setMaximum(
                baseCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        daySpinner.setSelection(baseCalendar.get(Calendar.DAY_OF_MONTH));
        GridData dayGd = new GridData();
        GC gc = new GC(daySpinner);
        dayGd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 2);
        gc.dispose();
        daySpinner.setLayoutData(dayGd);
        startTime = new DateTime(timeComp, SWT.TIME | SWT.BORDER);

        // duration
        Label durationLbl = new Label(timeComp, SWT.NONE);
        durationLbl.setText("Duration in Hours:");
        GridData durationGd = new GridData();
        durationGd.horizontalIndent = 20;
        durationLbl.setLayoutData(durationGd);

        durationCbo = new Combo(timeComp, SWT.READ_ONLY);
        String items[] = { "1", "2", "3", "6", "12", "24", "36", "48" };
        durationCbo.setItems(items);
        durationCbo.select(0);
    }

    /**
     * create CWS controls
     */
    private void createDefaultTab() {

        defaultComp = new Composite(shell, SWT.NONE);
        defaultComp.setLayout(new GridLayout(1, false));

        createStartTimeComp(defaultComp);

        // report composite
        Composite reportComp = new Composite(defaultComp, SWT.NONE);
        reportComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        reportComp.setLayout(new GridLayout(2, false));

        cancelChk = new Button(reportComp, SWT.CHECK);
        cancelChk.setText("Cancel " + cwaConfigs.getCwsuId() + " CWS  ");

        noUpdateChk = new Button(reportComp, SWT.CHECK);
        noUpdateChk.setText("No Updt Aft");
    }

    /**
     * create the product text box
     */
    private void createProductTextBox() {
        productTxt = new StyledText(getShell(),
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
        gd.heightHint = productTxt.getLineHeight() * 10;
        GC gc = new GC(productTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 90);
        gc.dispose();
        productTxt.setLayoutData(gd);
        productTxt.setEditable(cwaConfigs.isEditable());
        productTxt.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // lock the 1st two lines
                if (!allowMod && (productTxt.getLineAtOffset(e.start) < 2
                        || productTxt.getLineAtOffset(e.end) < 2)) {
                    e.doit = false;
                }
            }
        });
    }

    /**
     * Create bottom buttons
     */
    public void createBottomButtons() {
        Composite bottomComp = new Composite(getShell(), SWT.NONE);
        bottomComp.setLayout(new GridLayout(6, false));

        routineRdo = new Button(bottomComp, SWT.RADIO);
        routineRdo.setText("Rtn");
        routineRdo.setSelection(true);

        corRdo = new Button(bottomComp, SWT.RADIO);
        corRdo.setText("COR");
        corRdo.setSelection(false);

        Button newBtn = new Button(bottomComp, SWT.PUSH);
        newBtn.setText("Create New Text");
        newBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewText();
            }
        });

        Button previousBtn = new Button(bottomComp, SWT.PUSH);
        previousBtn.setText("Use Previous CWS Text");
        previousBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createPreviousText();
            }
        });

        Button sendBtn = new Button(bottomComp, SWT.PUSH);
        sendBtn.setText("Send Text");
        sendBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sendProduct();
            }
        });

        Button exitBtn = new Button(bottomComp, SWT.PUSH);
        exitBtn.setText("Exit");
        exitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * get calendar
     * 
     * @return calendar
     */
    private Calendar getCalendar() {
        Calendar calendar = TimeUtil.newGmtCalendar();

        if (startTimeChk.getSelection()) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar = (Calendar) baseCalendar.clone();
            if (day == maxDay && daySpinner.getSelection() == 1) {
                // This must be for next month
                calendar.add(Calendar.MONTH, 1);
            }
            calendar.set(Calendar.DAY_OF_MONTH, daySpinner.getSelection());
            calendar.set(Calendar.HOUR_OF_DAY, startTime.getHours());
            calendar.set(Calendar.MINUTE, startTime.getMinutes());

        }
        return calendar;
    }

    /**
     * create new product text
     */
    private void createNewText() {
        // Get starting and ending times.
        Calendar calendar = this.getCalendar();

        SimpleDateFormat sdfDate = new SimpleDateFormat("ddHHmm");
        String startDateTime = sdfDate.format(calendar.getTime());
        int duration = Integer.parseInt(durationCbo.getText());
        calendar.add(Calendar.HOUR, duration);
        String endDateTime = sdfDate.format(calendar.getTime());

        CWAProduct cwaProduct = new CWAProduct(retrievalProductId, "",
                cwaConfigs.isOperational(), null);
        String[] lines = cwaProduct.getProductTxt().split("\n");

        int oldSeriesNum = 99;
        int seriesNum = 1;
        if (lines.length > 1) {

            // Extract Message type and CWA number. Re-use previous number
            // if this is a correction.
            String[] items = lines[1].split("\\s+");
            try {
                if (items[2].length() <= 2 && items[2].matches("\\d+")) {
                    // This must be the default CWS/MIS format
                    oldSeriesNum = Integer.parseInt(items[2]);
                } else {
                    // This must be the with state ID CWS/MIS format
                    items = lines[2].split("\\s+");
                    oldSeriesNum = Integer.parseInt(items[2]);
                }
                seriesNum = oldSeriesNum;
                if (!corRdo.getSelection()) {
                    seriesNum++;
                    if (seriesNum > 99) {
                        seriesNum = 1;
                    }
                }
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse: " + items[2], nfe);
            }
        }
        String seriesStr = String.format("%02d", seriesNum);

        StringBuilder output = new StringBuilder();
        output.append(ttaaii).append(cwaConfigs.getKcwsuId()).append(" ")
                .append(startDateTime).append(" \n");
        output.append(cwaConfigs.getCwsuId()).append(" MIS ").append(seriesStr)
                .append(" VALID ");
        output.append(startDateTime).append("-").append(endDateTime)
                .append("Z");
        if (corRdo.getSelection()) {
            output.append(" COR");
        }
        output.append(" \n");
        output.append("... FOR ATC PLANNING PURPOSES ONLY...\n");
        if (cancelChk.getSelection()) {
            String prevalid = String.format("%02d", oldSeriesNum);
            output.append("CANCEL " + cwaConfigs.getCwsuId() + " MIS "
                    + prevalid + ". ");
        }
        if (noUpdateChk.getSelection()) {
            output.append("NO UPDT AFT ").append(endDateTime).append("Z.\n\n");
        }
        output.append(AbstractCWAComp.FINAL_LINE);

        setProductText(output.toString());
    }

    /**
     * Create a new product with previous product
     */
    private void createPreviousText() {
        // Get start and end times.
        Calendar calendar = TimeUtil.newGmtCalendar();
        if (startTimeChk.getSelection()) {
            calendar.set(Calendar.DAY_OF_MONTH, daySpinner.getSelection());
            calendar.set(Calendar.HOUR_OF_DAY, startTime.getHours());
            calendar.set(Calendar.MINUTE, startTime.getMinutes());
        }
        SimpleDateFormat sdfDate = new SimpleDateFormat("ddHHmm");
        String startDateTime = sdfDate.format(calendar.getTime());
        int duration = Integer.parseInt(durationCbo.getText());
        calendar.add(Calendar.HOUR, duration);
        String endDateTime = sdfDate.format(calendar.getTime());

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

        String body2 = "";
        String body3 = "";
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
        output.append("\n");
        output.append("...FOR ATC PLANNING PURPOSES ONLY...\n");
        output.append(body2).append("\n");
        output.append(body3).append("\n");

        if (this.cancelChk.getSelection()) {
            int prevalid = seriesId - 1;
            String prevalidStr = String.format("CANCEL %s CWS %02d. ",
                    cwaConfigs.getCwsuId(), prevalid);
            output.append(prevalidStr);
        }
        if (this.noUpdateChk.getSelection()) {
            output.append("NO UPDT AFT ").append(endDateTime).append("Z.");
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
     * save product and disseminate to DEFAULTNCF
     */
    private void sendProduct() {
        String product = productTxt.getText();
        if (product.trim().isEmpty()) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK);
            messageBox.setText("No Product to Save");
            messageBox.setMessage("There is no product to save.");
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
        boolean success = cwaProduct.sendText(cwaConfigs.getCwsuId());

        if (cwaConfigs.isOperational()) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK);
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
        productXml.setWeatherName(WeatherType.MIS.getName());
        productXMLList.add(productXml);

        try {
            CWAGeneratorUtil.writeProductsXML(cwaConfigs.isOperational(),
                    productXMLList);
        } catch (LocalizationException | SerializationException | IOException
                | JAXBException e) {
            logger.error("Failed to save product XML", e);
        }
    }

}
