/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the report dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 03/02/2017  17469    wkwock      Initial creation
 * 05/23/2018  17469    wkwock      Fix no practice mode report issue
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class ReportDlg extends CaveSWTDialog {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAProduct.class);

    private static final String ALL = "All";

    /** product text */
    private StyledText productTxt;

    /** is operational mode */
    private boolean isOperational;

    /** year combo */
    private Combo yearCbo;

    /** month combo */
    private Combo monthCbo;

    /** type combo */
    private Combo typeCbo;

    /**
     * Constructor
     * 
     * @param display
     * @param productId
     */
    public ReportDlg(Shell shell, Boolean isOperational) {
        super(shell, SWT.DIALOG_TRIM);
        setText("Report Generation");
        this.isOperational = isOperational;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        final int HORIZONTAL_INDENT = 20;

        GridLayout mainLayout = new GridLayout(1, false);
        shell.setLayout(mainLayout);

        SelectionAdapter generateReportListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                generateReport();
            }
        };

        Composite timeComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(6, false);
        timeComp.setLayout(gl);
        timeComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        Label yearLbl = new Label(timeComp, SWT.NONE);
        yearLbl.setText("Year:");
        yearCbo = new Combo(timeComp, SWT.READ_ONLY);
        int year = TimeUtil.newGmtCalendar().get(Calendar.YEAR);
        for (int i = year - 5; i <= year; i++) {
            yearCbo.add(Integer.toString(i));
        }
        yearCbo.select(5);
        yearCbo.addSelectionListener(generateReportListener);

        Label monthLbl = new Label(timeComp, SWT.NONE);
        monthLbl.setText("Month:");
        GridData gd = new GridData();
        gd.horizontalIndent = HORIZONTAL_INDENT;
        monthLbl.setLayoutData(gd);

        monthCbo = new Combo(timeComp, SWT.READ_ONLY);
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getShortMonths();
        for (int i = 0; i < 12; i++) {
            monthCbo.add(months[i]);
        }
        int month = TimeUtil.newGmtCalendar().get(Calendar.MONTH);
        monthCbo.select(month);
        monthCbo.addSelectionListener(generateReportListener);

        Label typeLbl = new Label(timeComp, SWT.NONE);
        typeLbl.setText("Report Type:");
        gd = new GridData();
        gd.horizontalIndent = HORIZONTAL_INDENT;
        typeLbl.setLayoutData(gd);

        typeCbo = new Combo(timeComp, SWT.READ_ONLY);
        String names[] = new String[WeatherType.values().length + 1];
        for (int i = 0; i < WeatherType.values().length; i++) {
            names[i] = WeatherType.values()[i].getName();
        }
        names[WeatherType.values().length] = ALL;
        typeCbo.setItems(names);
        typeCbo.select(0);
        typeCbo.addSelectionListener(generateReportListener);

        productTxt = new StyledText(shell,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.heightHint = productTxt.getLineHeight() * 20;
        GC gc = new GC(productTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 90);
        productTxt.setLayoutData(gd);
        productTxt.setEditable(false);
        gc.dispose();

        Composite bottomComp = new Composite(shell, SWT.NONE);
        gl = new GridLayout(3, false);
        bottomComp.setLayout(gl);
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        bottomComp.setLayoutData(gd);

        Button saveBtn = new Button(bottomComp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveReport();
            }
        });

        Button exitBtn = new Button(bottomComp, SWT.PUSH | SWT.CENTER);
        exitBtn.setText("Exit");
        exitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * save report to a file
     */
    private void saveReport() {
        if (productTxt.getText().isEmpty()) {
            return;
        }

        String typeName = ALL;
        if (typeCbo.getSelectionIndex() < (typeCbo.getItemCount() - 1)) {
            typeName = WeatherType.values()[typeCbo.getSelectionIndex()]
                    .getType();
        }
        String fileName = monthCbo.getText() + yearCbo.getText() + "-"
                + typeName + "-CWA.txt";
        DirectoryDialog dlg = new DirectoryDialog(shell);
        dlg.setText("Select a Directory");
        dlg.setMessage("Select a directory to save file " + fileName);
        String dir = dlg.open();
        if (dir != null) {
            try (PrintWriter out = new PrintWriter(
                    dir + File.separator + fileName)) {
                out.println(productTxt.getText());
            } catch (FileNotFoundException e) {
                logger.error("Failed to save report to file " + fileName, e);
            }
        }
    }

    /**
     * generate report
     */
    private void generateReport() {
        List<ProductConfig> productXMLList = null;
        try {
            productXMLList = CWAGeneratorUtil.readProductsXML(isOperational,
                    Integer.MAX_VALUE);
        } catch (LocalizationException | SerializationException | IOException
                | JAXBException e) {
            logger.error("Failed to read product XML.", e);
        }

        if (productXMLList == null || productXMLList.isEmpty()) {
            productTxt.setText("");
            return;
        }

        Map<WeatherType, List<ProductConfig>> productsList = new HashMap<>();
        for (WeatherType type : WeatherType.values()) {
            productsList.put(type, new ArrayList<ProductConfig>());
        }

        // sort by type
        for (ProductConfig productXML : productXMLList) {
            Calendar refTime = TimeUtil.newGmtCalendar(productXML.getTime());
            if (Integer.toString(refTime.get(Calendar.YEAR))
                    .equals(yearCbo.getText())
                    && monthCbo.getSelectionIndex() == refTime
                            .get(Calendar.MONTH)) {
                for (WeatherType type : WeatherType.values()) {
                    if (productXML.getWeatherName().equals(type.getName())) {
                        productsList.get(type).add(productXML);
                        break;
                    }
                }
            }
        }

        // generate the report
        StringBuilder report = new StringBuilder();

        if (this.typeCbo.getText().equals(ALL)) {
            WeatherType[] types = WeatherType.values();
            Arrays.sort(types);
            for (WeatherType type : types) {
                List<ProductConfig> tmpList = productsList.get(type);
                for (ProductConfig productXML : tmpList) {
                    report.append(productXML.getProductTxt()).append("\n");
                }
            }
        } else {
            WeatherType type = WeatherType.values()[typeCbo
                    .getSelectionIndex()];
            List<ProductConfig> tmpList = productsList.get(type);
            for (ProductConfig productXML : tmpList) {
                report.append(productXML.getProductTxt()).append("\n");
            }
        }

        // display report on the text field
        productTxt.setText(report.toString());
    }

    @Override
    public void preOpened() {
        generateReport();
    }
}
