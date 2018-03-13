/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.common.dataplugin.psh.EffectDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.FloodingDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshLSRProduct;
import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.TornadoDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.LsrProductRequest;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigHeader;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshTimeZone;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;

/**
 * Dialog for LSR File Manager.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 05, 2017 #35102     wpaintsil   Initial creation.
 * Aug 07, 2017 #36369     wpaintsil   Retrieve LSR products from textdb
 * Oct 20, 2017 #39235     astrakovsky Added filter for LSR files based on tab and fixed some formatting issues.
 * Oct 31, 2017 #40221     jwu         Parse "W" as negative for longitude and "S" as negative for latitude.
 * Nov 01, 2017 #40359     jwu         Format remarks in LSR report to lower case.
 * Nov 01, 2017 #39988     astrakovsky Adjusted LSR filtering to work for mixed case.
 * Nov 08  2017 #40423     jwu         Remove tide/surge.
 * Nov 16  2017 #40987     jwu         Use blank instead of "-" when "Incomplete" is not set.
 * Nov 08  2017 #40156     jwu         Adjust local time to UTC when parsing tornadoes.
 * Jan 08, 2017 DCS19326   wpaintsil   Baseline version
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshLSRDialog extends CaveJFACEDialog {

    /**
     * Courier font
     */
    private Font previewFont;

    private Table table;

    private Text previewText;

    private String currentLSRString = "";

    private PshTabComp tab;

    private Button loadButton;

    Map<PshLSRHazard, Boolean> hazardFlags = new HashMap<>();

    /**
     * A regex used to match a latitude string in an LSR product. "[+-]?"
     * indicates a positive or negative number. "\\d+" indicates an integer "."
     * indicates a decimal point. "N" indicates the North/South abbreviation for
     * latitude.
     */
    private static final String LAT_REGEX = "([+-]?\\d+\\.?\\d+)\\s*[N|S]";

    /**
     * A regex used to match a longitude string in an LSR product. "[+-]?"
     * indicates a positive or negative number. "\\d+" indicates an integer "."
     * indicates a decimal point. "W" indicates the West/East abbreviation for
     * longitude.
     */
    private static final String LON_REGEX = "([+-]?\\d+\\.?\\d+)\\s*[W|E]";

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshLSRDialog.class);

    /**
     * @param parentShell
     * @param tab
     */
    public PshLSRDialog(Shell parentShell, PshTabComp tab) {
        super(parentShell);
        this.tab = tab;
    }

    /**
     * @param parentShell
     * @param tab
     * @param perspectiveSpecific
     */
    public PshLSRDialog(Shell parentShell, PshTabComp tab,
            boolean perspectiveSpecific) {
        super(parentShell, perspectiveSpecific);
        this.tab = tab;
    }

    /**
     * Override the method from the parent Dialog class to implement the GUI.
     */
    @Override
    public Control createDialogArea(Composite parent) {
        /*
         * Sets dialog title
         */
        getShell().setText("LSR File Manager");

        previewFont = PshUtil.createFont("Courier", 10, SWT.BOLD);

        Composite top = (Composite) super.createDialogArea(parent);

        /*
         * Create the main layout for the shell.
         */
        GridLayout mainLayout = new GridLayout(2, false);
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        top.setLayoutData(mainLayoutData);

        createTable(top);
        createTextArea(top);
        createButtons(top);

        // Dispose fonts
        top.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (previewFont != null) {
                    previewFont.dispose();
                }
            }
        });

        return top;
    }

    /**
     * Create bottom buttons
     *
     * @param top
     */
    private void createButtons(Composite top) {
        Composite buttonComp = new Composite(top, SWT.NONE);
        RowLayout buttonLayout = new RowLayout();
        buttonLayout.pack = false;
        buttonComp.setLayout(buttonLayout);
        GridData buttonCompData = new GridData(SWT.RIGHT, SWT.BOTTOM, true,
                false);
        buttonCompData.horizontalSpan = 2;
        buttonComp.setLayoutData(buttonCompData);

        loadButton = new Button(buttonComp, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setEnabled(false);
        loadButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                parseLSRData();
            }

        });

        loadButton.setEnabled(false);

        Button closeButton = new Button(buttonComp, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PshLSRDialog.this.close();

            }
        });

    }

    /**
     * Parse storm data from the LSR text string retrieved from the textdb.
     * 
     * @return storm data
     */
    protected void parseLSRData() {

        // Check if "mixed case" is required.
        boolean mixedCase = PshConfigurationManager.getInstance()
                .getConfigHeader().isUseMixedCase();

        if (!currentLSRString.isEmpty()) {
            String[] lsrLines = currentLSRString.split("\n");
            switch (tab.getTabType()) {
            case RAINFALL:
                parseRainfall(lsrLines);
                break;
            case FLOODING:
                parseFlooding(lsrLines, mixedCase);
                break;
            case TORNADO:
                parseTornado(lsrLines, mixedCase);
                break;
            case EFFECT:
                parseEffects(lsrLines, mixedCase);
                break;
            default:
                break;
            }
            tab.updatePreviewArea();
        }
    }

    /**
     * Use a regular expression to search a string.
     * 
     * @param pattern
     *            a regular expression
     * @param searchString
     *            the string to search
     * @return
     */
    private static String findPattern(String pattern, String searchString) {
        String match = "";
        Pattern latPattern = Pattern.compile(pattern);
        Matcher latMatcher = latPattern.matcher(searchString);

        latMatcher.find();
        try {
            match = latMatcher.group(1);
        } catch (IllegalStateException e) {
            logger.debug("Match not found: regex ->" + latPattern.pattern()
                    + " string -> " + searchString);
        }

        return match;
    }

    /**
     * Parse a local date and time form LSR report and convert to UTC.
     * 
     * @param zone
     *            time zone string such as "EST"
     * @param date
     *            date String in the format MM/dd/yyyy
     * @param time
     *            time String in the format hhmm a
     * @return dateTime String in the format dd/HHmm
     */
    private static String parseDateTime(String zone, String date, String time) {
        DateFormat timeFormat = new SimpleDateFormat("hhmm a MM/dd/yyyy");
        Calendar formattedLocalDate = TimeUtil.newCalendar();
        try {
            formattedLocalDate.setTime(timeFormat.parse(time + " " + date));
        } catch (ParseException e) {
            logger.warn("Could not parse date or time string: " + time + " "
                    + date);
        }

        // Adjust to UTC
        PshTimeZone pshTZ = PshTimeZone.getPshTimeZone(zone.substring(0, 1));
        int timeDiff = pshTZ.getTimeOffset(formattedLocalDate.getTime());

        formattedLocalDate.add(Calendar.HOUR_OF_DAY, -timeDiff);

        return formattedLocalDate.get(Calendar.DAY_OF_MONTH) + "/"
                + String.format("%02d",
                        formattedLocalDate.get(Calendar.HOUR_OF_DAY))
                + String.format("%02d",
                        formattedLocalDate.get(Calendar.MINUTE));
    }

    /**
     * Create the section containing a selection of LSR files.
     */
    private void createTable(Composite parent) {
        Group fileComp = new Group(parent, SWT.SHADOW_IN);
        fileComp.setText("LSR Products");
        GridLayout fileLayout = new GridLayout(1, false);
        fileComp.setLayout(fileLayout);
        fileComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ScrolledComposite scrolledComp = new ScrolledComposite(fileComp,
                SWT.NONE);
        scrolledComp.setLayout(new GridLayout());

        GridData scrolledData = new GridData(SWT.FILL, SWT.FILL, true, true);
        scrolledData.heightHint = 500;

        scrolledComp.setLayoutData(scrolledData);

        table = new Table(scrolledComp, SWT.BORDER | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.deselectAll();

        scrolledComp.setContent(table);
        scrolledComp.setExpandHorizontal(true);
        scrolledComp.setExpandVertical(true);
        scrolledComp.setAlwaysShowScrollBars(false);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setWidth(90);
        column1.setText("MM DD YY");

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setWidth(100);
        column2.setText("Issuance Time");

        retrieveLSRProducts();

    }

    /**
     * Retrieve LSR products from the textdb.
     */
    private void retrieveLSRProducts() {
        try {
            PshConfigHeader header = PshConfigurationManager.getInstance()
                    .getConfigHeader();

            @SuppressWarnings("unchecked")
            List<PshLSRProduct> products = (List<PshLSRProduct>) ThriftClient
                    .sendRequest(
                            new LsrProductRequest(header.getLsrHeader(), true));

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss z");

            // check which products are relevant to the current tab and add to
            // table
            boolean relevantProduct;
            List<PshLSRProduct> displayedProducts = new ArrayList<>();
            for (PshLSRProduct product : products) {

                relevantProduct = checkProductRelevance(product.getLsrText());

                if (relevantProduct) {

                    displayedProducts.add(product);

                    Calendar cal = product.getCreationTime();

                    TableItem row = new TableItem(table, SWT.NONE);
                    row.setText(0, dateFormat.format(cal.getTime()));
                    row.setText(1, timeFormat.format(cal.getTime()));
                }

            }

            // When a row in the table is selected, display the LSR text for
            // that entry.
            table.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (loadButton != null) {
                        loadButton.setEnabled(true);
                    }

                    int selectionIndex = table.getSelectionIndex();

                    if (selectionIndex < products.size()
                            && previewText != null) {

                        // Remove header
                        String removedHeader = displayedProducts
                                .get(selectionIndex).getLsrText();
                        int begin = removedHeader
                                .indexOf("PRELIMINARY LOCAL STORM REPORT");
                        removedHeader = removedHeader.substring(begin);

                        previewText.setText(removedHeader);

                        currentLSRString = removedHeader;
                    }

                }
            });

        } catch (VizException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Checks if the contents of an LSR file are relevant to the current tab.
     * 
     * @param productText
     * @return
     */
    private boolean checkProductRelevance(final String productText) {

        String tempText = productText.toUpperCase();

        switch (tab.getTabType()) {
        case RAINFALL:
            if (tempText.contains("HEAVY RAIN")) {
                return true;
            }
            break;
        case FLOODING:
            if (tempText.contains("FLOOD")) {
                return true;
            }
            break;
        case TORNADO:
            if (tempText.contains("TORNADO")) {
                return true;
            }
            break;
        case EFFECT:
            for (PshLSRHazard hazard : PshLSRHazard.values()) {
                if (tempText.contains(hazard.name().toUpperCase())
                        || tempText.contains(hazard.toString().toUpperCase())) {
                    return true;
                }
            }
            break;
        default:
            return true;
        }

        return false;

    }

    /**
     * Create the section containing the text and options for a selected LSR
     * file.
     */
    private void createTextArea(Composite parent) {
        Group lsrAreaComp = new Group(parent, SWT.SHADOW_IN);
        lsrAreaComp.setText("LSR Viewer");
        GridLayout lsrAreaLayout = new GridLayout(2, false);
        lsrAreaComp.setLayout(lsrAreaLayout);
        lsrAreaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        previewText = new Text(lsrAreaComp,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.heightHint = 500;
        textData.widthHint = 600;
        previewText.setLayoutData(textData);
        previewText.setEditable(false);
        previewText.setBackground(parent.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        previewText.setFont(previewFont);

        // "Select a Hazard" section visible with the Storm Effects tab.
        if (tab.getTabType() == PshDataCategory.EFFECT) {
            Group hazardComp = new Group(lsrAreaComp, SWT.SHADOW_IN);
            hazardComp.setText("Hazards");
            GridLayout hazardLayout = new GridLayout(1, false);
            hazardComp.setLayout(hazardLayout);
            hazardComp.setLayoutData(
                    new GridData(SWT.FILL, SWT.TOP, true, false));

            for (PshLSRHazard hazard : PshLSRHazard.values()) {
                Button hazardCheckbox = new Button(hazardComp, SWT.CHECK);
                hazardCheckbox.setText(hazard.getLabel());
                hazardFlags.put(hazard, false);
                hazardCheckbox.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        PshLSRHazard currentHazard = PshLSRHazard
                                .getHazard(hazardCheckbox.getText());
                        hazardFlags.put(currentHazard,
                                hazardCheckbox.getSelection());
                    }

                });

            }
        }

        previewText.setFocus();
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
     * Make this dialog resizable.
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Prevent the controls in the dialog from being cut off when resizing.
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setMinimumSize(600, 200);
    }

    /**
     * Parse storm rainfall in an LSR product. Add them to a tab's table in the
     * main window.
     * 
     * @param lsrLines
     */
    private void parseRainfall(String[] lsrLines) {
        List<RainfallDataEntry> rainDataList = new ArrayList<>();

        Pattern terminator = Pattern.compile("\\$\\$");

        for (int ii = 0; ii < lsrLines.length; ii++) {
            // check if the line has the "$$" terminator string
            if (!terminator.matcher(lsrLines[ii]).matches()) {

                String[] splitLine = lsrLines[ii].split(" ");

                if (ii > 6 && !lsrLines[ii].isEmpty()
                        && !lsrLines[ii].substring(0, 1).equals(" ")) {
                    if (splitLine.length > 1
                            && (splitLine[1].equalsIgnoreCase("AM")
                                    || splitLine[1].equalsIgnoreCase("PM"))) {

                        String line1 = lsrLines[ii];
                        String line2 = lsrLines[ii + 1];

                        String event = line1.substring(12, 29).trim();

                        String cityLocation = line1.substring(29, 53);

                        String latStr = line1.substring(53, 59).trim();
                        String lat = findPattern(LAT_REGEX, latStr);

                        String lonStr = line1
                                .substring(60, Math.min(67, line1.length()))
                                .trim();
                        String lon = findPattern(LON_REGEX, lonStr);

                        String magnitudeString = line2.substring(13, 28);
                        String[] magnitude = magnitudeString.split(" ");

                        String magn, inc;
                        if (magnitude.length > 0 && !magnitude[0].isEmpty()) {
                            magn = magnitude[0];
                            inc = "";
                        } else {
                            magn = "";
                            inc = "I";
                        }

                        String countyLocation = line2.substring(29, 47);

                        String location = "";
                        String[] cityLocationArr = cityLocation.split(" ");

                        String dir, dis;
                        if (Pattern.compile("[0-9]").matcher(cityLocationArr[0])
                                .matches()) {
                            for (int jj = 2; jj < cityLocationArr.length; jj++) {
                                location = location + " " + cityLocationArr[jj];
                            }
                            location = location.substring(1);
                            dir = cityLocationArr[1];
                            dis = cityLocationArr[0];
                        } else {
                            location = cityLocation;
                            dir = "None";
                            dis = "0";
                        }

                        String state = line2.substring(48, 50);

                        if (event.equalsIgnoreCase("HEAVY RAIN")) {

                            magn.replaceAll(" ", "");

                            RainfallDataEntry rainData = new RainfallDataEntry();

                            rainData.setRainfall(PshUtil.parseFloat(magn));
                            rainData.setIncomplete(inc);

                            float tlat = PshUtil.parseFloat(lat);
                            if (latStr.endsWith("S")) {
                                tlat = -tlat;
                            }

                            float tlon = PshUtil.parseFloat(lon);
                            if (lonStr.endsWith("W")) {
                                tlon = -tlon;
                            }

                            PshCity city = new PshCity(location.trim(),
                                    countyLocation.trim(), state.trim(), tlat,
                                    tlon, "", "", "");
                            rainData.setCity(city);

                            rainData.setDirection(dir);
                            rainData.setDistance(PshUtil.parseFloat(dis));

                            rainDataList.add(rainData);
                        }
                    }
                }
            }
        }

        if (rainDataList.isEmpty()) {
            new MessageDialog(getShell(), "", null,
                    "No Rainfall reports were found in the selected LSR.",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();

        } else {
            new MessageDialog(getShell(), "", null,
                    "Rainfall report(s) added to the database from the selected LSR: "
                            + rainDataList.size(),
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();

            for (RainfallDataEntry lsrData : rainDataList) {
                tab.addItem(lsrData);
            }
        }
    }

    /**
     * Parse inland flooding in an LSR product. Add them to a tab's table in the
     * main window.
     * 
     * @param lsrLines
     * @param mixedCase
     *            If mixed case is asked.
     */
    private void parseFlooding(String[] lsrLines, boolean mixedCase) {
        List<FloodingDataEntry> floodDataList = new ArrayList<>();
        Pattern terminator = Pattern.compile("\\$\\$");

        for (int ii = 0; ii < lsrLines.length; ii++) {
            // check if the line has the "$$" terminator string
            if (!terminator.matcher(lsrLines[ii]).matches()) {

                String[] splitLine = lsrLines[ii].split(" ");

                if (ii > 6 && !lsrLines[ii].isEmpty()
                        && !lsrLines[ii].substring(0, 1).equals(" ")) {
                    if (splitLine.length > 1
                            && (splitLine[1].equalsIgnoreCase("AM")
                                    || splitLine[1].equalsIgnoreCase("PM"))) {

                        String line1 = lsrLines[ii];
                        String line2 = lsrLines[ii + 1];

                        String event = line1.substring(12, 29).trim();

                        String countyLocation = line2.substring(29, 47);

                        if (event.equalsIgnoreCase("FLOOD")
                                || event.equalsIgnoreCase("FLASH FLOOD")) {

                            FloodingDataEntry floodData = new FloodingDataEntry();

                            floodData.setCounty(countyLocation.trim());

                            StringBuilder remark = new StringBuilder();
                            int flag = 0;
                            for (int jj = ii + 3; flag < 1; jj++) {
                                String line = lsrLines[jj];
                                String comtest = line.length() > 0
                                        ? line.substring(0, 1) : null;
                                if (comtest != null && comtest.equals(" ")) {
                                    String commLine = line.substring(12,
                                            Math.min(line.length(), 69));
                                    String lastChar = commLine.length() > 0
                                            ? commLine.substring(
                                                    commLine.length() - 1,
                                                    commLine.length())
                                            : "";

                                    remark.append((lastChar).equals(" ")
                                            ? commLine : (commLine + " "));
                                } else {
                                    flag = 1;
                                }

                            }

                            if (mixedCase) {
                                floodData.setRemarks(
                                        capitalize(remark.toString().trim()));
                            } else {
                                floodData.setRemarks(remark.toString().trim());
                            }

                            floodDataList.add(floodData);
                        }
                    }
                }
            }
        }

        if (floodDataList.isEmpty()) {
            new MessageDialog(getShell(), "", null,
                    "No Flood reports were found in the selected LSR.",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();

        } else {
            new MessageDialog(getShell(), "", null,
                    "Flood report(s) added to the database from the selected LSR: "
                            + floodDataList.size(),
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();

            for (FloodingDataEntry lsrData : floodDataList) {
                tab.addItem(lsrData);
            }
        }
    }

    /**
     * Parse tornadoes in an LSR product. Add them to a tab's table in the main
     * window.
     * 
     * @param lsrLines
     * @param mixedCase
     *            If mixed case is asked.
     */
    private void parseTornado(String[] lsrLines, boolean mixedCase) {

        List<TornadoDataEntry> tornadoDataList = new ArrayList<>();

        String zone = "EST";
        if (lsrLines.length > 2) {
            zone = lsrLines[2].split(" ")[2];
        }
        Pattern terminator = Pattern.compile("\\$\\$");

        for (int ii = 0; ii < lsrLines.length; ii++) {

            // check if the line has the "$$" terminator string
            if (!terminator.matcher(lsrLines[ii]).matches()) {

                String[] splitLine = lsrLines[ii].split(" ");

                if (ii > 6 && !lsrLines[ii].isEmpty()
                        && !lsrLines[ii].substring(0, 1).equals(" ")) {
                    if (splitLine.length > 1
                            && (splitLine[1].equalsIgnoreCase("AM")
                                    || splitLine[1].equalsIgnoreCase("PM"))) {

                        String line1 = lsrLines[ii];
                        String line2 = lsrLines[ii + 1];

                        String localTime = line1.substring(0, 7).trim();

                        String event = line1.substring(12, 29).trim();

                        String cityLocation = line1.substring(29, 53);

                        String latStr = line1.substring(53, 59).trim();
                        String lat = findPattern(LAT_REGEX, latStr);

                        String lonStr = line1
                                .substring(60, Math.min(67, line1.length()))
                                .trim();
                        String lon = findPattern(LON_REGEX, lonStr);

                        String date = line2.substring(0, 7).trim();

                        String magnitudeString = line2.substring(13, 28);
                        String[] magnitude = magnitudeString.split(" ");

                        String magn, inc;
                        if (magnitude.length > 0 && !magnitude[0].isEmpty()) {
                            magn = magnitude[0];
                            inc = "";
                        } else {
                            magn = "N/A";
                            inc = "I";
                        }

                        String countyLocation = line2.substring(29, 47);
                        String location = "";
                        String[] cityLocationArr = cityLocation.split(" ");

                        String dir, dis;
                        if (Pattern.compile("[0-9]").matcher(cityLocationArr[0])
                                .matches()) {
                            for (int jj = 2; jj < cityLocationArr.length; jj++) {
                                location = location + " " + cityLocationArr[jj];
                            }
                            location = location.substring(1);
                            dir = cityLocationArr[1];
                            dis = cityLocationArr[0];
                        } else {
                            location = cityLocation;
                            dir = "None";
                            dis = "0";
                        }

                        String state = line2.substring(48, 50);

                        if (event.equalsIgnoreCase("TORNADO")) {

                            magn.replaceAll(" ", "");

                            TornadoDataEntry tornadoData = new TornadoDataEntry();

                            tornadoData.setMagnitude(magn);

                            tornadoData.setDatetime(
                                    parseDateTime(zone, date, localTime));

                            float tlat = PshUtil.parseFloat(lat);
                            if (latStr.endsWith("S")) {
                                tlat = -tlat;
                            }

                            float tlon = PshUtil.parseFloat(lon);
                            if (lonStr.trim().endsWith("W")) {
                                tlon = -tlon;
                            }

                            PshCity city = new PshCity(location.trim(),
                                    countyLocation.trim(), state.trim(), tlat,
                                    tlon, "", "", "");
                            tornadoData.setLocation(city);

                            tornadoData.setDirection(dir);
                            tornadoData.setDistance(PshUtil.parseFloat(dis));
                            tornadoData.setIncomplete(inc);

                            StringBuilder remark = new StringBuilder();
                            int flag = 0;
                            for (int jj = ii + 3; flag < 1; jj++) {
                                String line = lsrLines[jj];
                                String comtest = line.length() > 0
                                        ? line.substring(0, 1) : null;
                                if (comtest != null && comtest.equals(" ")) {
                                    String commLine = line.substring(12,
                                            Math.min(line.length(), 69));
                                    String lastChar = commLine.length() > 0
                                            ? commLine.substring(
                                                    commLine.length() - 1,
                                                    commLine.length())
                                            : "";

                                    remark.append((lastChar).equals(" ")
                                            ? commLine : (commLine + " "));
                                } else {
                                    flag = 1;
                                }

                            }

                            if (mixedCase) {
                                tornadoData.setRemarks(
                                        capitalize(remark.toString().trim()));
                            } else {
                                tornadoData
                                        .setRemarks(remark.toString().trim());
                            }

                            tornadoDataList.add(tornadoData);
                        }
                    }
                }
            }
        }

        if (tornadoDataList.isEmpty()) {
            new MessageDialog(getShell(), "", null,
                    "No Tornado reports were found in the selected LSR.",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();

        } else {
            new MessageDialog(getShell(), "", null,
                    "Tornado reports added to the the database from the selected LSR: "
                            + tornadoDataList.size(),
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();
            for (TornadoDataEntry lsrData : tornadoDataList) {
                tab.addItem(lsrData);
            }
        }

    }

    /**
     * Parse various hazards in an LSR product. Add them to a tab's table in the
     * main window.
     * 
     * @param lsrLines
     * @param mixedCase
     *            If mixed case is asked.
     */
    private void parseEffects(String[] lsrLines, boolean mixedCase) {
        Map<PshLSRHazard, List<EffectDataEntry>> effectDataMap = new HashMap<>();
        boolean hazardSelected = false;
        for (PshLSRHazard hazard : PshLSRHazard.values()) {
            List<EffectDataEntry> effectDataList = new ArrayList<>();
            if (hazardFlags.get(hazard)) {
                hazardSelected = true;
                Pattern terminator = Pattern.compile("\\$\\$");

                for (int ii = 0; ii < lsrLines.length; ii++) {
                    // check if the line has the "$$" terminator string
                    if (!terminator.matcher(lsrLines[ii]).matches()) {

                        String[] splitLine = lsrLines[ii].split(" ");

                        if (ii > 6 && !lsrLines[ii].isEmpty()
                                && !lsrLines[ii].substring(0, 1).equals(" ")) {
                            if (splitLine.length > 1 && (splitLine[1]
                                    .equalsIgnoreCase("AM")
                                    || splitLine[1].equalsIgnoreCase("PM"))) {
                                String line1 = lsrLines[ii];
                                String line2 = lsrLines[ii + 1];

                                String event = line1.substring(12, 29).trim();

                                String countyLocation = line2.substring(29, 47);

                                if (hazard.getLabel().toUpperCase()
                                        .equals(event.toUpperCase())) {
                                    String[] comm1 = lsrLines[ii + 3]
                                            .split(" ");

                                    int fatal = 0, inj = 0, commf = 0;
                                    if (comm1[0].equals("***")
                                            && comm1[2]
                                                    .equalsIgnoreCase("FATAL")
                                            && comm1[4].equalsIgnoreCase("INJ")
                                            && comm1[5].equals("***")) {
                                        commf = 6;
                                        fatal = Integer.parseInt(comm1[1]);
                                        inj = Integer.parseInt(comm1[3]);

                                    } else if (comm1[0].equals("***")
                                            && comm1[3].equals("***")) {
                                        commf = 4;
                                        if (comm1[2]
                                                .equalsIgnoreCase("FATAL")) {
                                            fatal = Integer.parseInt(comm1[1]);
                                            inj = 0;
                                        } else if (comm1[2]
                                                .equalsIgnoreCase("INJ")) {
                                            fatal = 0;
                                            inj = Integer.parseInt(comm1[1]);
                                        }
                                    }

                                    EffectDataEntry stormData = new EffectDataEntry();
                                    stormData.setCounty(countyLocation.trim());
                                    stormData.setDeaths(fatal);
                                    stormData.setInjuries(inj);

                                    // Set to 0 in legacy
                                    stormData.setEvacuations(0);

                                    // Parse remarks for each LSR entry.
                                    StringBuilder remark = new StringBuilder();
                                    int flag = 0;
                                    for (int jj = ii + 3; flag < 1; jj++) {
                                        String line = lsrLines[jj];
                                        String comtest = line.length() > 0
                                                ? line.substring(0, 1) : null;
                                        if (comtest != null
                                                && comtest.equals(" ")) {
                                            String commLine;
                                            if (jj == ii + 3) {
                                                if (commf == 0) {
                                                    commLine = "";
                                                    for (int cf = commf; cf < comm1.length; cf++) {
                                                        commLine = commLine
                                                                + comm1[cf]
                                                                + " ";
                                                    }
                                                    remark.append(commLine);
                                                } else {
                                                    commLine = line.substring(
                                                            12,
                                                            Math.min(
                                                                    line.length(),
                                                                    69));

                                                    String lastChar = commLine
                                                            .length() > 0
                                                                    ? commLine
                                                                            .substring(
                                                                                    commLine.length()
                                                                                            - 1,
                                                                                    commLine.length())
                                                                    : "";

                                                    remark.append((lastChar)
                                                            .equals(" ")
                                                                    ? commLine
                                                                    : (commLine
                                                                            + " "));
                                                }
                                            } else {
                                                commLine = line.substring(12,
                                                        Math.min(line.length(),
                                                                69));

                                                String lastChar = commLine
                                                        .length() > 0
                                                                ? commLine
                                                                        .substring(
                                                                                commLine.length()
                                                                                        - 1,
                                                                                commLine.length())
                                                                : "";

                                                remark.append(
                                                        (lastChar).equals(" ")
                                                                ? commLine
                                                                : (commLine
                                                                        + " "));
                                            }

                                        } else {
                                            flag = 1;
                                        }

                                    }

                                    if (mixedCase) {
                                        stormData.setRemarks(capitalize(
                                                remark.toString().trim()));
                                    } else {
                                        stormData.setRemarks(
                                                remark.toString().trim());
                                    }

                                    effectDataList.add(stormData);
                                    effectDataMap.put(hazard, effectDataList);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hazardSelected) {
            for (PshLSRHazard hazard : PshLSRHazard.values()) {
                if (hazardFlags.get(hazard)) {

                    List<EffectDataEntry> effectList = effectDataMap
                            .get(hazard);
                    if (effectList == null || effectList.isEmpty()) {
                        new MessageDialog(getShell(), "", null,
                                "No " + hazard.getLabel()
                                        + " reports were found in the selected LSR.",
                                MessageDialog.INFORMATION,
                                new String[] { "OK" }, 0).open();
                    } else {
                        new MessageDialog(getShell(), "", null,
                                "Reports for " + hazard.getLabel()
                                        + " added to the database: "
                                        + effectList.size(),
                                MessageDialog.INFORMATION,
                                new String[] { "OK" }, 0).open();
                        for (EffectDataEntry stormData : effectList) {
                            tab.addItem(stormData);
                        }
                    }
                }
            }
        } else {
            new MessageDialog(getShell(), "", null,
                    "Please select at least one hazard on the right.",
                    MessageDialog.WARNING, new String[] { "OK" }, 0).open();
        }
    }

    /**
     * Convert a string to lower case and then capitalize the first word in each
     * sentence of the string.
     * 
     * @param inStr
     *            String to be Capitalized.
     * @return String
     */
    private String capitalize(String inStr) {

        String outStr = inStr;

        if (inStr != null && !inStr.isEmpty()) {
            String lwStr = inStr.trim().toLowerCase();
            String[] sentences = lwStr.split("\\.");
            if (sentences.length > 0) {
                StringBuilder stb = new StringBuilder();
                for (String str : sentences) {
                    stb.append(StringUtils.capitalize(str)).append(". ");
                }
                outStr = stb.toString();
            } else {
                outStr = StringUtils.capitalize(lwStr);
            }
        }

        return outStr;

    }

}