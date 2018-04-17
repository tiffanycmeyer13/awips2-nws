/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.f6builder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.core.mode.CAVEMode;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.F6ServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.F6ServiceResponse;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Dialog for F6_builder
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 7, 2015            xzhang       Initial creation
 * Sep 19, 2016           jwu          View product in browser
 * Sep 20, 2016           jwu          Set remarks as "#FINAL-MM-YY#" for completed months
 * 28 SEP 2016  22166     jwu          Send product to text DB with PIL.
 * 14 NOV 2016  22166     jwu          Use local tmp files to view product in browser & send to text DB.
 * 23 NOV 2016  21381     amoore       Cleaning up F6 printing. Add helpful tooltips.
 * 02 DEC 2016  26345     astrakovsky  Added work in progress cursor when building F6 report.
 * 21 DEC 2016  26904     wpaintsil    Make this dialog appear in the task bar.
 * 28 FEB 2017  27858     amoore       Clean up of dialog. Not all products must be successful to
 *                                     send the successful ones to Text DB/browser viewing. Text DB
 *                                     work/browser viewing should not rely on message dialog.
 * 10 MAR 2017  30130     amoore       F6 should not keep output in awips directory, and should
 *                                     delete after printing. Send to textDB on EDEX side, not VIZ.
 * 13 APR 2017  33104     amoore       Address comments from review.
 * 03 MAY 2017  33104     amoore       Use abstract map.
 * 09 MAY 2017  33104     amoore       Clean up CAVE operational mode testing.
 * 12 MAY 2017  33104     amoore       Address minor FindBugs.
 * 02 JUN 2017  34778     jwu          Change "Remarks" text to multi-line.
 * 12 SEP 2017  23215     amoore       Add Select All stations capability.
 * 19 SEP 2017  38124     amoore       Use GC for text control sizes.
 * 17 OCT 2017  39614     amoore       Address review comments.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

public class F6BuilderDialog extends ClimateCaveDialog {
    /**
     * Station list.
     */
    protected List<Station> stations = new ArrayList<>();

    /**
     * Buttons to generate current F6.
     */
    protected Button currentF6button;

    /**
     * Buttons to generate previous F6.
     */
    protected Button previousF6button;

    /**
     * Table to list stations.
     */
    protected Table stationTable;

    /**
     * List of years.
     */
    protected String[] years;

    /**
     * Drop down combo to select year.
     */
    protected Combo yearDropDown;

    /**
     * Drop down combo to select month.
     */
    protected Combo monthDropDown;

    /**
     * Button to print report.
     */
    protected Button printCheckButton;

    /**
     * Button to view report.
     */
    protected Button viewCheckButton;

    /**
     * Remark text field.
     */
    protected Text remarksText;

    /**
     * Month label.
     */
    private Label monthLabel;

    /**
     * Year label.
     */
    private Label yearLabel;

    /**
     * A location to hold temporary f6 files to be viewed in browser & sent to
     * text DB.
     */
    private static String LOCAL_F6_TEMP_DIR = System.getProperty("user.home")
            + File.separator + "tmp";

    /**
     * Constructor.
     * 
     * @param parent
     *            parent shell for this dialog.
     */
    @SuppressWarnings("unchecked")
    public F6BuilderDialog(Shell parent) {
        super(parent);

        setText("F6 Product Date and Stations");
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        try {
            stations = (List<Station>) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Could not retrieve stations for F6 report dialog", e);
        }
    }

    @Override
    protected void initializeComponents(final Shell shell) {
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 10;
        gl.marginHeight = 10;
        shell.setLayout(gl);

        buildSelectButtons();
        buildStationTable();
        buildDateSelection();
        buildOptions();
        buildButtons();
    }

    /**
     * Build (de)select all buttons.
     */
    private void buildSelectButtons() {
        Composite selectButtonsComp = new Composite(shell, SWT.NONE);
        RowLayout rl = new RowLayout();
        rl.spacing = 5;
        selectButtonsComp.setLayout(rl);
        // select all button
        Button selectAllButton = new Button(selectButtonsComp, SWT.PUSH);
        // deselect all button
        Button deselectAllButton = new Button(selectButtonsComp, SWT.PUSH);

        selectAllButton.setText("Select All");
        selectAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem item : stationTable.getItems()) {
                    item.setChecked(true);
                }
            }
        });

        deselectAllButton.setText("Deselect All");
        deselectAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem item : stationTable.getItems()) {
                    item.setChecked(false);
                }
            }
        });
    }

    /**
     * Build station table.
     */
    private void buildStationTable() {
        stationTable = new Table(shell, SWT.CHECK | SWT.BORDER | SWT.MULTI);
        stationTable.setHeaderVisible(true);

        GC gc = new GC(stationTable);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        GridData gd_table = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
        stationTable.setLayoutData(gd_table);

        TableColumn checkColumn = new TableColumn(stationTable, SWT.NONE);
        checkColumn.setWidth(3 * fontWidth);
        checkColumn.setText("");

        TableColumn codeColumn = new TableColumn(stationTable, SWT.NONE);
        codeColumn.setWidth(22 * fontWidth);
        codeColumn.setText("Station ID");

        TableColumn nameColumn = new TableColumn(stationTable, SWT.NONE);
        nameColumn.setWidth(43 * fontWidth);
        nameColumn.setText("Station Name");

        for (int i = 0; i < stations.size(); i++) {
            TableItem item = new TableItem(stationTable, SWT.NONE);
            item.setText(new String[] { "", stations.get(i).getIcaoId(),
                    stations.get(i).getStationName() });
        }
    }

    /**
     * Build date selection section.
     */
    private void buildDateSelection() {
        Composite selectDateComp = new Composite(shell, SWT.BORDER);
        selectDateComp.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        selectDateComp.setLayout(new GridLayout(5, false));

        currentF6button = new Button(selectDateComp, SWT.RADIO);
        currentF6button.setText("Generate Current F6");
        currentF6button.setSelection(true);
        currentF6button.pack();

        new Label(selectDateComp, SWT.NONE);
        new Label(selectDateComp, SWT.NONE);
        new Label(selectDateComp, SWT.NONE);
        new Label(selectDateComp, SWT.NONE);

        previousF6button = new Button(selectDateComp, SWT.RADIO);
        previousF6button.setText("Previous Month:");
        previousF6button.pack();

        monthLabel = new Label(selectDateComp, SWT.NULL);

        monthLabel.setText("Month:");
        monthLabel.pack();
        monthDropDown = new Combo(selectDateComp,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);

        yearLabel = new Label(selectDateComp, SWT.NULL);
        yearLabel.setText("Year:");

        yearDropDown = new Combo(selectDateComp,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);

        setDateSelectionEnabled(false);

        currentF6button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setDateSelectionEnabled(false);
            }
        });
        previousF6button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setDateSelectionEnabled(true);
            }
        });

        final String[] months = new DateFormatSymbols().getMonths();
        for (int i = 0; i < 12; i++) {
            monthDropDown.add(months[i]);
        }

        int year = TimeUtil.newCalendar().get(Calendar.YEAR);
        years = new String[5];
        for (int i = 0; i < years.length; i++) {
            years[i] = (year - i) + "";
            yearDropDown.add(years[i]);
        }

        Calendar cal = TimeUtil.newCalendar();
        cal.add(Calendar.MONTH, -1);
        int pYear = cal.get(Calendar.YEAR);
        int pMonth = cal.get(Calendar.MONTH);
        yearDropDown.select(year - pYear);
        monthDropDown.select(pMonth);
    }

    /**
     * @param enable
     */
    private void setDateSelectionEnabled(boolean enable) {
        monthLabel.setEnabled(enable);
        monthDropDown.setEnabled(enable);
        yearLabel.setEnabled(enable);
        yearDropDown.setEnabled(enable);
    }

    /**
     * Build option section - "Print", "View", and "Remarks".
     */
    private void buildOptions() {

        Composite printOpenComp = new Composite(shell, SWT.NONE);
        printOpenComp.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        GridLayout gl0 = new GridLayout(2, false);
        printOpenComp.setLayout(gl0);

        // print button
        printCheckButton = new Button(printOpenComp, SWT.CHECK);

        GC gc = new GC(printCheckButton);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        /*
         * Height to ensure the remarks box has multiple easily viewable rows
         */
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        GridData gd_printCheckButton = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 1, 1);
        gd_printCheckButton.widthHint = 29 * fontWidth;
        printCheckButton.setLayoutData(gd_printCheckButton);
        printCheckButton.setText("Print selected F6s");
        printCheckButton.setToolTipText(
                "Print F6 reports for the selected stations on the EDEX server.");

        // "View selected F6s" button
        viewCheckButton = new Button(printOpenComp, SWT.CHECK);
        GridData gd_viewCheckButton = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 1, 1);
        gd_viewCheckButton.widthHint = 38 * fontWidth;
        viewCheckButton.setLayoutData(gd_viewCheckButton);
        viewCheckButton.setText("View F6 copies in browser");
        viewCheckButton.setToolTipText(
                "View copies of generated F6 reports stored on the EDEX server.");

        new Label(shell, SWT.NONE);

        // remarks
        Composite centeredComp = new Composite(shell, SWT.NONE);
        centeredComp.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        GridLayout gl = new GridLayout(2, false);
        centeredComp.setLayout(gl);

        Label label = new Label(centeredComp, SWT.NONE);
        label.setText("Remarks:");

        int style = SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
        remarksText = new Text(centeredComp, style);

        GridData textGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1,
                1);
        textGd.widthHint = 58 * fontWidth;
        textGd.heightHint = 3 * fontHeight;
        remarksText.setLayoutData(textGd);
    }

    /**
     * Build control buttons - "OK" & "Cancel".
     */
    private void buildButtons() {
        Composite centeredComp = new Composite(shell, SWT.NONE);
        centeredComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        GridLayout gl = new GridLayout(2, false);
        centeredComp.setLayout(gl);

        final Button okBtn = new Button(centeredComp, SWT.NONE);

        GC gc = new GC(okBtn);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        GridData gd_okBtn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,
                1);
        gd_okBtn.widthHint = 9 * fontWidth;
        okBtn.setLayoutData(gd_okBtn);
        okBtn.setText("OK");

        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                F6ServiceRequest request = new F6ServiceRequest();
                ClimateDate date = new ClimateDate();
                Calendar cal = TimeUtil.newCalendar();
                if (currentF6button.getSelection()) {

                    date.setYear(cal.get(Calendar.YEAR));
                    date.setMon(cal.get(Calendar.MONTH) + 1);
                    date.setDay(cal.get(Calendar.DAY_OF_MONTH) - 1);
                } else {
                    Calendar preCal = new GregorianCalendar(
                            Integer.parseInt(
                                    years[yearDropDown.getSelectionIndex()]),
                            monthDropDown.getSelectionIndex(), 1);
                    if (preCal.after(cal)) {
                        MessageDialog.openWarning(shell, "Error Selecting Time",
                                "A future date was selected.");
                        return;
                    }

                    date.setYear(preCal.get(Calendar.YEAR));
                    date.setMon(preCal.get(Calendar.MONTH) + 1);
                    date.setDay(preCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                }

                request.setAdate(date);

                request.setPrint(printCheckButton.getSelection());

                List<Station> selectedStations = new ArrayList<>();
                for (int i = 0; i < stationTable.getItemCount(); i++) {
                    if (stationTable.getItem(i).getChecked()) {
                        selectedStations.add(stations.get(i));
                    }
                }

                if (selectedStations.size() == 0) {
                    MessageDialog.openWarning(shell, "Error Selecting Station",
                            "At least one station must be selected to continue.");
                    return;
                }

                request.setStations(selectedStations);

                /*
                 * Set remarks - prefix remarks with "#FINAL-MM-YY#" for
                 * completed months.
                 */
                request.setRemarks(remarksText.getText());

                /*
                 * Generate reports, save into F6_OUTPUT_LOCATION =
                 * "/awips2/climate/output/", then print/view if desired.
                 */
                String responseMessage = "Error getting response from EDEX.";
                try {
                    // 26345 - add work in progress cursor icon
                    ClimateGUIUtils.setCursorWait(shell);

                    // Send product to text DB based on CAVE Mode.
                    CAVEMode mode = CAVEMode.getMode();
                    boolean operationalMode = (CAVEMode.OPERATIONAL.equals(mode)
                            || CAVEMode.TEST.equals(mode));
                    request.setOperational(operationalMode);

                    F6ServiceResponse response = (F6ServiceResponse) ThriftClient
                            .sendRequest(request);
                    responseMessage = response.getMessage();

                    if (viewCheckButton.getSelection()
                            && response.getContentMap() != null) {
                        // Write report contents to temporary files.
                        Map<String, String> filesToView = writeTempFiles(
                                response.getContentMap());

                        // Open F6 product files in browser.
                        viewFilesInBrowser(filesToView);
                    }

                    close();
                } catch (VizException e) {
                    logger.error("Error generating F6 report", e);
                } finally {
                    MessageDialog.openInformation(shell, "Build F6 Completed",
                            responseMessage);

                    // 26345 - add work in progress cursor icon
                    if (!shell.isDisposed()) {
                        ClimateGUIUtils.resetCursor(shell);
                    }
                }
            }
        });

        Button cancelBtn = new Button(centeredComp, SWT.NONE);
        GridData gd_cancelBtn = new GridData(SWT.LEFT, SWT.CENTER, false, false,
                1, 1);
        gd_cancelBtn.widthHint = 9 * fontWidth;
        cancelBtn.setLayoutData(gd_cancelBtn);
        cancelBtn.setText("Cancel");

        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * View a list of F6 product files in browser.
     * 
     * @param filesToView
     *            A list of F6 files to be viewed.
     */
    private void viewFilesInBrowser(Map<String, String> filesToView) {

        for (Entry<String, String> entry : filesToView.entrySet()) {
            String pil = entry.getKey();
            String path = entry.getValue();
            try {

                URL fileURL = new URL("file://" + path);
                try {
                    PlatformUI.getWorkbench().getBrowserSupport()
                            .getExternalBrowser().openURL(fileURL);
                } catch (PartInitException e) {
                    logger.error("Cannot open file: [" + fileURL,
                            "] with PIL: [" + pil + "] in F6 report dialog.",
                            e);
                }
            } catch (MalformedURLException e) {
                logger.error("Incorrect path for file: [" + path,
                        "] with PIL: [" + pil + "] in F6 report dialog.", e);
            }
        }
    }

    /**
     * Write a list of strings to a list of temporary files.
     * 
     * @param stringsToWrite
     *            A list of strings to be written.
     * @return A map of files with full path.
     */
    protected Map<String, String> writeTempFiles(
            Map<String, List<String>> stringsToWrite) {

        Map<String, String> tmpF6Files = new HashMap<>();

        for (Entry<String, List<String>> entry : stringsToWrite.entrySet()) {
            String pil = entry.getKey();
            String fileName = LOCAL_F6_TEMP_DIR + File.separator + pil;
            Path file = Paths.get(fileName);

            try {
                Files.createDirectories(Paths.get(LOCAL_F6_TEMP_DIR));
                Files.write(file, entry.getValue(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("Fail to write F6 temporary file for PIL: [" + pil,
                        "] in F6 report dialog.", e);
            }

            tmpF6Files.put(pil, fileName);
        }

        return tmpF6Files;
    }

}
