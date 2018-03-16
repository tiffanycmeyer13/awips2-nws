/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.DeleteClimateDayRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchClimateDayRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.GetAvailableDayOfYearRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.SaveClimateDayRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;

/**
 * This class display the "DAILY NORMALS, MEANS, EXTREMES" dialog for
 * init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 03/25/2016  18469    wkwock      Initial creation
 * 10/27/2016  20635    wkwock      Clean up
 * 20 DEC 2016 26404    amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 27 DEC 2016 22450    amoore      Init should use common listeners where possible.
 * 12 JAN 2017 26411    wkwock      Merge Modify and Add button to Save button
 * 26 JAN 2017 26511    wkwock      Fix values not loading properly after deletion issue.
 * 27 FEB 2017 27420    amoore      Fix titles.
 * 15 MAY 2017 33104    amoore      FindBugs and logic issues. Class rename.
 * 15 JUN 2017 35187    amoore      Handle trace symbol in text box.
 * 19 SEP 2017 38124    amoore      Use GC for text control sizes.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public class DailyClimateDialog extends ClimateCaveChangeTrackDialog {
    /** station location */
    protected Text stationLocTxt;

    /** mean max temperature */
    private Text meanMaxTempTxt;

    /** record max temperature */
    private Text recordMaxTempTxt;

    /** year 1 record max temperature */
    private Text year1RecordMaxTempTxt;

    /** year 2 record max temperature */
    private Text year2RecordMaxTempTxt;

    /** year 3 record max temperature */
    private Text year3RecordMaxTempTxt;

    /** mean min temperature */
    private Text meanMinTempTxt;

    /** record min temperature */
    private Text recordMinTempTxt;

    /** year 1 record min temperature */
    private Text year1RecordMinTempTxt;

    /** year 2 record min temperature */
    private Text year2RecordMinTempTxt;

    /** year 3 record min temperature */
    private Text year3RecordMinTempTxt;

    /** normal mean temperature */
    private Text normMeanTempTxt;

    /** average precipitation */
    private Text avgPrecipTxt;

    /** record max precipitation */
    private Text recordMaxPrecipTxt;

    /** year 1 record max precipitation */
    private Text year1RecordMaxPrecipTxt;

    /** year 2 record max precipitation */
    private Text year2RecordMaxPrecipTxt;

    /** year 3 record max precipitation */
    private Text year3RecordMaxPrecipTxt;

    /** heating degree */
    private Text heatingDegreeTxt;

    /** cooling degree */
    private Text coolingDegreeTxt;

    /** average snow fall */
    private Text avgSnowfallTxt;

    /** record max snow fall */
    private Text recordMaxSnowfallTxt;

    /** year 1 record max snow fall */
    private Text year1RecordMaxSnowfallTxt;

    /** year 2 record max snow fall */
    private Text year2RecordMaxSnowfallTxt;

    /** year 3 record max snow fall */
    private Text year3RecordMaxSnowfallTxt;

    /** daily snow on ground */
    private Text dailySnowOnGroundTxt;

    /** month combo */
    protected Combo monthCbo;

    /** day combo */
    protected Combo dayCbo;

    /** save record button */
    private Button saveRecordBtn;

    /** delete button */
    private Button deleteBtn;

    /** bold font */
    protected Font boldFont;

    /** last day_of_year */
    protected String lastDayOfYear = null;

    /**
     * Collection of listeners.
     */
    private final ClimateTextListeners myDisplayListeners = new ClimateTextListeners();

    /**
     * Constructor.
     * 
     * @param display
     */
    public DailyClimateDialog(Display display) {
        super(display);
        String hostname = System.getenv("HOSTNAME");
        setText("Daily Normals, Means, Extremes (on " + hostname + ")");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        // prepare boldFont.
        FontData fontData = getShell().getFont().getFontData()[0];
        boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.BOLD));

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                boldFont.dispose();
            }
        });

        createStationControls();
        createDateControls();

        // separate line
        Label separateLbl = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, true);
        gl.horizontalSpacing = 80;
        gl.marginWidth = 15;
        bottomComp.setLayout(gl);

        Composite leftComp = new Composite(bottomComp, SWT.NONE);
        RowLayout leftRl = new RowLayout(SWT.VERTICAL);
        leftRl.spacing = 15;
        leftComp.setLayout(leftRl);
        createTempControls(leftComp);
        createDegreeControls(leftComp);

        Composite rightComp = new Composite(bottomComp, SWT.NONE);
        RowLayout rightRl = new RowLayout(SWT.VERTICAL);
        rightRl.spacing = 80;
        rightComp.setLayout(rightRl);
        createPrecipControls(rightComp);
        createSnowFallControls(rightComp);
    }

    /**
     * Always return true, as this dialog is managed by another.
     */
    public boolean shouldClose() {
        return true;
    }

    /**
     * Create station controls
     */
    private void createStationControls() {
        Composite stationComp = new Composite(shell, SWT.NONE);
        stationComp.setLayout(new RowLayout());

        // Station label
        Label stationLbl = new Label(stationComp, SWT.CENTER);
        stationLbl.setText("Station");

        // Station label
        stationLocTxt = new Text(stationComp, SWT.BORDER);

        GC gc = new GC(stationLocTxt);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        stationLocTxt.setLayoutData(new RowData(43 * fontWidth, SWT.DEFAULT));
        stationLocTxt.setEditable(false);
    }

    /**
     * create date controls
     */
    private void createDateControls() {
        Composite dateComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(11, false);
        dateComp.setLayout(gl);

        // Month label
        Label monthLbl = new Label(dateComp, SWT.NONE);
        monthLbl.setText("Month");

        // Month drop menu
        monthCbo = new Combo(dateComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        // getShortMonths() return 13 items. Use for loop to avoid
        // months[12], which is empty
        for (int i = 0; i < 12; i++) {
            monthCbo.add(months[i]);
        }
        monthCbo.select(0);
        monthCbo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (!getDayOfyear().equals(lastDayOfYear)) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to new record?"
                                        + " Unsaved changes will be lost.");
                    }

                    if (!proceed) {
                        int month = Integer
                                .parseInt(lastDayOfYear.substring(0, 2));
                        monthCbo.select(month - 1);
                        return;
                    } else {
                        updateDayCbo();
                        setDailyClimatologyInfo();
                    }
                }
            }
        });

        // up button
        Composite spinnerComp = new Composite(dateComp, SWT.NONE);
        spinnerComp.setLayout(new GridLayout(1, false));
        Button upMonthBtn = new Button(spinnerComp, SWT.ARROW | SWT.UP);
        upMonthBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (monthCbo.getSelectionIndex() > 0) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to month " + monthCbo.getItem(
                                        monthCbo.getSelectionIndex() - 1) + "?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        monthCbo.select(monthCbo.getSelectionIndex() - 1);
                        updateDayCbo();
                        setDailyClimatologyInfo();
                    }
                }
            }
        });

        // down button
        Button downMonthBtn = new Button(spinnerComp, SWT.ARROW | SWT.DOWN);
        downMonthBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (monthCbo
                        .getSelectionIndex() < (monthCbo.getItemCount() - 1)) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record?",
                                "Change to month " + monthCbo.getItem(
                                        monthCbo.getSelectionIndex() + 1) + "?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        monthCbo.select(monthCbo.getSelectionIndex() + 1);
                        updateDayCbo();
                        setDailyClimatologyInfo();
                    }
                }
            }
        });

        // day label
        Label dayLbl = new Label(dateComp, SWT.NONE);
        dayLbl.setText("Day");
        GridData gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
        gd.horizontalIndent = 20;
        dayLbl.setLayoutData(gd);

        // Day drop menu
        dayCbo = new Combo(dateComp, SWT.POP_UP);

        Calendar cal = new GregorianCalendar(2016, 0, 1); // leap year
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            dayCbo.add(String.valueOf(i));
        }
        dayCbo.select(0);
        dayCbo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (!getDayOfyear().equals(lastDayOfYear)) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record?",
                                "Change to a different record?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (!proceed) {
                        int day = Integer
                                .parseInt(lastDayOfYear.substring(3, 5));
                        dayCbo.select(day - 1);
                        return;
                    } else {
                        setDailyClimatologyInfo();
                    }
                }
            }
        });

        // up button
        Composite spinnerDayComp = new Composite(dateComp, SWT.NONE);
        spinnerDayComp.setLayout(new GridLayout(1, false));
        Button upDayBtn = new Button(spinnerDayComp, SWT.ARROW | SWT.UP);
        upDayBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dayCbo.getSelectionIndex() > 0) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to day "
                                        + dayCbo.getItem(
                                                dayCbo.getSelectionIndex() - 1)
                                        + "?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        dayCbo.select(dayCbo.getSelectionIndex() - 1);
                        setDailyClimatologyInfo();
                    }
                }
            }
        });

        // down button
        Button downDayBtn = new Button(spinnerDayComp, SWT.ARROW | SWT.DOWN);
        downDayBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dayCbo.getSelectionIndex() < (dayCbo.getItemCount() - 1)) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to day "
                                        + dayCbo.getItem(
                                                dayCbo.getSelectionIndex() + 1)
                                        + "?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        dayCbo.select(dayCbo.getSelectionIndex() + 1);
                        setDailyClimatologyInfo();
                    }
                }
            }
        });

        // First button
        Button firstBtn = new Button(dateComp, SWT.PUSH);
        firstBtn.setText("First");
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
        gd.horizontalIndent = 20;
        firstBtn.setLayoutData(gd);
        firstBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int stationId = getStationId(stationLocTxt.getText());

                GetAvailableDayOfYearRequest request = new GetAvailableDayOfYearRequest(
                        true, stationId);
                String newDayOfYear = null;
                try {
                    newDayOfYear = (String) ThriftClient.sendRequest(request);
                } catch (VizException e) {
                    logger.error("Could not retrieve day_of_year for station "
                            + stationId, e);
                }

                if (!getDayOfyear().equals(newDayOfYear)) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to first record?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (!proceed) {
                        return;
                    } else {
                        setAvailableClimatologyInfo(true);
                    }
                }
            }
        });

        // Modify Record button
        saveRecordBtn = new Button(dateComp, SWT.PUSH);
        saveRecordBtn.setText("Save Record");
        saveRecordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveRecord();
            }
        });

        // Delete Record button
        deleteBtn = new Button(dateComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                deleteRecord();
            }
        });

        // Last button
        Button lastBtn = new Button(dateComp, SWT.PUSH);
        lastBtn.setText("Last");
        lastBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int stationId = getStationId(stationLocTxt.getText());

                GetAvailableDayOfYearRequest request = new GetAvailableDayOfYearRequest(
                        false, stationId);
                String newDayOfYear = null;
                try {
                    newDayOfYear = (String) ThriftClient.sendRequest(request);
                } catch (VizException e) {
                    logger.error("Could not retrieve day_of_year for station "
                            + stationId, e);
                }

                if (!getDayOfyear().equals(newDayOfYear)) {
                    boolean proceed = true;
                    if (isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to lastest record?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        setAvailableClimatologyInfo(false);
                    }
                }
            }
        });
    }

    /**
     * create temperature controls
     * 
     * @param bottomComp
     * @return
     */
    private Group createTempControls(Composite bottomComp) {
        Group tempGrp = new Group(bottomComp, SWT.SHADOW_IN);
        tempGrp.setText("Temperature");
        tempGrp.setFont(boldFont);
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        tempGrp.setLayout(gl);

        Label meanLbl = new Label(tempGrp, SWT.LEFT);
        meanLbl.setText("Mean Maximum\nTemperature");
        meanMaxTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        meanMaxTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        meanMaxTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        meanMaxTempTxt.addListener(SWT.Modify, changeListener);

        Label recordLbl = new Label(tempGrp, SWT.LEFT);
        recordLbl.setText("Record Maximum\nTemperature");
        recordMaxTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        recordMaxTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        recordMaxTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        recordMaxTempTxt.addListener(SWT.Modify, changeListener);

        Label year1Lbl = new Label(tempGrp, SWT.LEFT);
        year1Lbl.setText("Year 1 Record Maximum\nTemperature Observed");
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        year1Lbl.setLayoutData(gd);
        year1RecordMaxTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        year1RecordMaxTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMaxTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMaxTempTxt.addListener(SWT.Modify, changeListener);

        Label year2Lbl = new Label(tempGrp, SWT.LEFT);
        year2Lbl.setText("Year 2 Record Maximum\nTemperature Observed");
        year2RecordMaxTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        year2RecordMaxTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMaxTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMaxTempTxt.addListener(SWT.Modify, changeListener);

        Label year3Lbl = new Label(tempGrp, SWT.LEFT);
        year3Lbl.setText("Year 3 Record Maximum\nTemperature Observed");
        year3RecordMaxTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        year3RecordMaxTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMaxTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMaxTempTxt.addListener(SWT.Modify, changeListener);

        Label meanMinLbl = new Label(tempGrp, SWT.LEFT);
        meanMinLbl.setText("Mean Minimum\nTemperature");
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        meanMinLbl.setLayoutData(gd);
        meanMinTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        meanMinTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        meanMinTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        meanMinTempTxt.addListener(SWT.Modify, changeListener);

        Label recordMinLbl = new Label(tempGrp, SWT.LEFT);
        recordMinLbl.setText("Record Minimum\nTemperature");
        recordMinTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        recordMinTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        recordMinTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        recordMinTempTxt.addListener(SWT.Modify, changeListener);

        Label year1MinLbl = new Label(tempGrp, SWT.LEFT);
        year1MinLbl.setText("Year 1 Record Minimum\nTemperature Observed");
        year1RecordMinTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        year1RecordMinTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMinTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMinTempTxt.addListener(SWT.Modify, changeListener);

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        year1MinLbl.setLayoutData(gd);

        Label year2MinLbl = new Label(tempGrp, SWT.LEFT);
        year2MinLbl.setText("Year 2 Record Minimum\nTemperature Observed");
        year2RecordMinTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        year2RecordMinTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMinTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMinTempTxt.addListener(SWT.Modify, changeListener);

        Label year3MinLbl = new Label(tempGrp, SWT.LEFT);
        year3MinLbl.setText("Year 3 Record Minimum\nTemperature Observed");
        year3RecordMinTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        year3RecordMinTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMinTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMinTempTxt.addListener(SWT.Modify, changeListener);

        Label normMeanTempLbl = new Label(tempGrp, SWT.LEFT);
        normMeanTempLbl.setText("Normal Mean\nTemperature");
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        normMeanTempLbl.setLayoutData(gd);
        normMeanTempTxt = new Text(tempGrp, SWT.LEFT | SWT.BORDER);
        normMeanTempTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        normMeanTempTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        normMeanTempTxt.addListener(SWT.Modify, changeListener);

        return tempGrp;
    }

    /**
     * create precipitation controls
     * 
     * @param bottomComp
     * @return
     */
    private Group createPrecipControls(Composite bottomComp) {

        Group precipGrp = new Group(bottomComp, SWT.SHADOW_IN);
        precipGrp.setText("Precipitation");
        precipGrp.setFont(boldFont);
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        precipGrp.setLayout(gl);

        Label avgLbl = new Label(precipGrp, SWT.LEFT);
        avgLbl.setText("Average Precipitation\n(inches)");
        avgPrecipTxt = new Text(precipGrp, SWT.LEFT | SWT.BORDER);
        avgPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        avgPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        avgPrecipTxt.addListener(SWT.Modify, changeListener);

        Label recordLbl = new Label(precipGrp, SWT.LEFT);
        recordLbl.setText("Record Maximum\nPrecipitation (inches)");
        recordMaxPrecipTxt = new Text(precipGrp, SWT.LEFT | SWT.BORDER);
        recordMaxPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        recordMaxPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        recordMaxPrecipTxt.addListener(SWT.Modify, changeListener);

        Label year1Lbl = new Label(precipGrp, SWT.LEFT);
        year1Lbl.setText("Year 1 Record Maximum\nPrecipitation Observed");
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        year1Lbl.setLayoutData(gd);
        year1RecordMaxPrecipTxt = new Text(precipGrp, SWT.LEFT | SWT.BORDER);
        year1RecordMaxPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMaxPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMaxPrecipTxt.addListener(SWT.Modify, changeListener);

        Label year2Lbl = new Label(precipGrp, SWT.LEFT);
        year2Lbl.setText("Year 2 Record Maximum\nPrecipitation Observed");
        year2RecordMaxPrecipTxt = new Text(precipGrp, SWT.LEFT | SWT.BORDER);
        year2RecordMaxPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMaxPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMaxPrecipTxt.addListener(SWT.Modify, changeListener);

        Label year3Lbl = new Label(precipGrp, SWT.LEFT);
        year3Lbl.setText("Year 3 Record Maximum\nPrecipitation Observed");
        year3RecordMaxPrecipTxt = new Text(precipGrp, SWT.LEFT | SWT.BORDER);
        year3RecordMaxPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMaxPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMaxPrecipTxt.addListener(SWT.Modify, changeListener);

        return precipGrp;
    }

    /**
     * create degree controls
     * 
     * @param bottomComp
     * @return
     */
    private Group createDegreeControls(Composite bottomComp) {
        Group heatGrp = new Group(bottomComp, SWT.SHADOW_IN);
        heatGrp.setText("Heating/Cooling Degree Days");
        heatGrp.setFont(boldFont);
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        heatGrp.setLayout(gl);

        Label heatLbl = new Label(heatGrp, SWT.LEFT);
        heatLbl.setText("Heating Degree Days");
        heatingDegreeTxt = new Text(heatGrp, SWT.LEFT | SWT.BORDER);
        heatingDegreeTxt.addListener(SWT.Verify,
                myDisplayListeners.getDegreeDaysListener());
        heatingDegreeTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDegreeDaysListener());
        heatingDegreeTxt.addListener(SWT.Modify, changeListener);

        Label coolLbl = new Label(heatGrp, SWT.LEFT);
        coolLbl.setText("Cooling Degree Days");
        coolingDegreeTxt = new Text(heatGrp, SWT.LEFT | SWT.BORDER);
        coolingDegreeTxt.addListener(SWT.Verify,
                myDisplayListeners.getDegreeDaysListener());
        coolingDegreeTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDegreeDaysListener());
        coolingDegreeTxt.addListener(SWT.Modify, changeListener);

        return heatGrp;
    }

    /**
     * create snow fall controls
     * 
     * @param bottomComp
     * @return
     */
    private Group createSnowFallControls(Composite bottomComp) {
        Group snowGrp = new Group(bottomComp, SWT.SHADOW_IN);
        snowGrp.setText("Snowfall");
        snowGrp.setFont(boldFont);
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        snowGrp.setLayout(gl);

        Label avgLbl = new Label(snowGrp, SWT.LEFT);
        avgLbl.setText("Average Daily\nSnowfall");
        avgSnowfallTxt = new Text(snowGrp, SWT.LEFT | SWT.BORDER);
        avgSnowfallTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        avgSnowfallTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        avgSnowfallTxt.addListener(SWT.Modify, changeListener);

        Label maxLbl = new Label(snowGrp, SWT.LEFT);
        maxLbl.setText("Record Maximum\nSnowfall (inches)");
        recordMaxSnowfallTxt = new Text(snowGrp, SWT.LEFT | SWT.BORDER);
        recordMaxSnowfallTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        recordMaxSnowfallTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        recordMaxSnowfallTxt.addListener(SWT.Modify, changeListener);

        Label year1Lbl = new Label(snowGrp, SWT.LEFT);
        year1Lbl.setText("Year 1 Record Maximum\nsnowfall Observed");
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        year1Lbl.setLayoutData(gd);
        year1RecordMaxSnowfallTxt = new Text(snowGrp, SWT.LEFT | SWT.BORDER);
        year1RecordMaxSnowfallTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMaxSnowfallTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year1RecordMaxSnowfallTxt.addListener(SWT.Modify, changeListener);

        Label year2Lbl = new Label(snowGrp, SWT.LEFT);
        year2Lbl.setText("Year 2 Record Maximum\nsnowfall Observed");
        year2RecordMaxSnowfallTxt = new Text(snowGrp, SWT.LEFT | SWT.BORDER);
        year2RecordMaxSnowfallTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMaxSnowfallTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year2RecordMaxSnowfallTxt.addListener(SWT.Modify, changeListener);

        Label year3Lbl = new Label(snowGrp, SWT.LEFT);
        year3Lbl.setText("Year 3 Record Maximum\nsnowfall Observed");
        year3RecordMaxSnowfallTxt = new Text(snowGrp, SWT.LEFT | SWT.BORDER);
        year3RecordMaxSnowfallTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMaxSnowfallTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        year3RecordMaxSnowfallTxt.addListener(SWT.Modify, changeListener);

        Label dailyLbl = new Label(snowGrp, SWT.LEFT);
        dailyLbl.setText("Daily Snow on\nGround (inches)");
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        dailyLbl.setLayoutData(gd);
        dailySnowOnGroundTxt = new Text(snowGrp, SWT.LEFT | SWT.BORDER);
        dailySnowOnGroundTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        dailySnowOnGroundTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        dailySnowOnGroundTxt.addListener(SWT.Modify, changeListener);

        return snowGrp;
    }

    /**
     * set the station location
     * 
     * @param stationLocation
     */
    public void setStationLoc(String stationLocation) {
        stationLocTxt.setText(stationLocation);
        setAvailableClimatologyInfo(true);
    }

    /**
     * When month selection changed, the day selections should be updated
     */
    protected void updateDayCbo() {
        int dayIndex = dayCbo.getSelectionIndex();
        int monthIndex = monthCbo.getSelectionIndex();

        // 2016 or any leap year
        Calendar mycal = new GregorianCalendar(2016, monthIndex, 1);

        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (daysInMonth != dayCbo.getItemCount()) {
            dayCbo.removeAll();
            for (int i = 1; i <= daysInMonth; i++) {
                dayCbo.add(String.valueOf(i));
            }
            if (dayIndex >= daysInMonth) {
                dayIndex = daysInMonth - 1;
            }
            dayCbo.select(dayIndex);
        }
    }

    /**
     * Get daily climatology from DB and display
     */
    public void setDailyClimatologyInfo() {
        ClimateDayNorm climateDayRcd = null;

        int stationId = getStationId(stationLocTxt.getText());
        String dayOfYear = getDayOfyear();

        FetchClimateDayRecordRequest request = new FetchClimateDayRecordRequest(
                dayOfYear, stationId);
        try {
            climateDayRcd = (ClimateDayNorm) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to fetch record with station ID=" + stationId
                    + " and day_of_year=" + dayOfYear, e);
        }

        if (climateDayRcd == null) {
            climateDayRcd = new ClimateDayNorm();
            climateDayRcd.setDataToMissing();
            climateDayRcd.setStationId(stationId);
            climateDayRcd.setDayOfYear(dayOfYear);
            deleteBtn.setEnabled(false);
        } else {
            deleteBtn.setEnabled(true);
        }

        displayClimatologyInfo(climateDayRcd);
    }

    /**
     * Display daily climatology
     * 
     * @param climateDayRcd
     */
    private void displayClimatologyInfo(ClimateDayNorm climateDayRcd) {
        changeListener.setIgnoreChanges(true);

        meanMaxTempTxt
                .setText(Integer.toString(climateDayRcd.getMaxTempMean()));
        recordMaxTempTxt
                .setText(Integer.toString(climateDayRcd.getMaxTempRecord()));
        year1RecordMaxTempTxt
                .setText(Integer.toString(climateDayRcd.getMaxTempYear()[0]));
        year2RecordMaxTempTxt
                .setText(Integer.toString(climateDayRcd.getMaxTempYear()[1]));
        year3RecordMaxTempTxt
                .setText(Integer.toString(climateDayRcd.getMaxTempYear()[2]));
        meanMinTempTxt
                .setText(Integer.toString(climateDayRcd.getMinTempMean()));
        recordMinTempTxt
                .setText(Integer.toString(climateDayRcd.getMinTempRecord()));
        year1RecordMinTempTxt
                .setText(Integer.toString(climateDayRcd.getMinTempYear()[0]));
        year2RecordMinTempTxt
                .setText(Integer.toString(climateDayRcd.getMinTempYear()[1]));
        year3RecordMinTempTxt
                .setText(Integer.toString(climateDayRcd.getMinTempYear()[2]));

        normMeanTempTxt.setText(Integer
                .toString(ClimateUtilities.nint(climateDayRcd.getMeanTemp())));

        avgPrecipTxt.setText(Float.toString(climateDayRcd.getPrecipMean()));
        recordMaxPrecipTxt
                .setText(Float.toString(climateDayRcd.getPrecipDayRecord()));
        year1RecordMaxPrecipTxt.setText(
                Integer.toString(climateDayRcd.getPrecipDayRecordYear()[0]));
        year2RecordMaxPrecipTxt.setText(
                Integer.toString(climateDayRcd.getPrecipDayRecordYear()[1]));
        year3RecordMaxPrecipTxt.setText(
                Integer.toString(climateDayRcd.getPrecipDayRecordYear()[2]));

        heatingDegreeTxt
                .setText(Integer.toString(climateDayRcd.getNumHeatMean()));
        coolingDegreeTxt
                .setText(Integer.toString(climateDayRcd.getNumCoolMean()));
        avgSnowfallTxt.setText(Float.toString(climateDayRcd.getSnowDayMean()));
        recordMaxSnowfallTxt
                .setText(Float.toString(climateDayRcd.getSnowDayRecord()));
        year1RecordMaxSnowfallTxt.setText(
                Integer.toString(climateDayRcd.getSnowDayRecordYear()[0]));
        year2RecordMaxSnowfallTxt.setText(
                Integer.toString(climateDayRcd.getSnowDayRecordYear()[1]));
        year3RecordMaxSnowfallTxt.setText(
                Integer.toString(climateDayRcd.getSnowDayRecordYear()[2]));
        dailySnowOnGroundTxt
                .setText(Float.toString(climateDayRcd.getSnowGround()));

        lastDayOfYear = getDayOfyear();

        changeListener.setChangesUnsaved(false);
        changeListener.setIgnoreChanges(false);
    }

    /**
     * get the daily climate information from GUI
     * 
     * @return
     */
    public ClimateDayNorm getDailyClimatologyInfo() {
        ClimateDayNorm climateDayRcd = null;

        int stationId = getStationId(stationLocTxt.getText());
        String dayOfYear = getDayOfyear();

        FetchClimateDayRecordRequest request = new FetchClimateDayRecordRequest(
                dayOfYear, stationId);
        try {
            climateDayRcd = (ClimateDayNorm) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to get daily climate data.", e);
        }

        if (climateDayRcd == null) {
            climateDayRcd = new ClimateDayNorm();
            climateDayRcd.setDataToMissing();
            climateDayRcd.setStationId(stationId);
            climateDayRcd.setDayOfYear(dayOfYear);
        }

        // Note: no need to catch NumberFormatException since there's a listener
        // to filter out invalid inputs
        climateDayRcd
                .setMaxTempMean(Short.parseShort(meanMaxTempTxt.getText()));
        climateDayRcd
                .setMaxTempRecord(Short.parseShort(recordMaxTempTxt.getText()));

        short[] maxTempYear = new short[3];
        maxTempYear[0] = Short.parseShort(year1RecordMaxTempTxt.getText());
        maxTempYear[1] = Short.parseShort(year2RecordMaxTempTxt.getText());
        maxTempYear[2] = Short.parseShort(year3RecordMaxTempTxt.getText());
        climateDayRcd.setMaxTempYear(maxTempYear);

        climateDayRcd
                .setMinTempMean(Short.parseShort(meanMinTempTxt.getText()));
        climateDayRcd
                .setMinTempRecord(Short.parseShort(recordMinTempTxt.getText()));

        short[] minTempYear = new short[3];
        minTempYear[0] = Short.parseShort(year1RecordMinTempTxt.getText());
        minTempYear[1] = Short.parseShort(year2RecordMinTempTxt.getText());
        minTempYear[2] = Short.parseShort(year3RecordMinTempTxt.getText());
        climateDayRcd.setMinTempYear(minTempYear);

        climateDayRcd.setMeanTemp(Float.parseFloat(normMeanTempTxt.getText()));

        climateDayRcd.setPrecipMean(avgPrecipTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(avgPrecipTxt.getText()));
        climateDayRcd.setPrecipDayRecord(recordMaxPrecipTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(recordMaxPrecipTxt.getText()));

        short[] maxprecipYear = new short[3];
        maxprecipYear[0] = Short.parseShort(year1RecordMaxPrecipTxt.getText());
        maxprecipYear[1] = Short.parseShort(year2RecordMaxPrecipTxt.getText());
        maxprecipYear[2] = Short.parseShort(year3RecordMaxPrecipTxt.getText());
        climateDayRcd.setPrecipDayRecordYear(maxprecipYear);

        climateDayRcd
                .setNumHeatMean(Integer.parseInt(heatingDegreeTxt.getText()));
        climateDayRcd
                .setNumCoolMean(Integer.parseInt(coolingDegreeTxt.getText()));

        climateDayRcd.setSnowDayMean(avgSnowfallTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(avgSnowfallTxt.getText()));

        climateDayRcd.setSnowDayRecord(recordMaxSnowfallTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(recordMaxSnowfallTxt.getText()));

        short[] maxSnowfall = new short[3];
        maxSnowfall[0] = Short.parseShort(year1RecordMaxSnowfallTxt.getText());
        maxSnowfall[1] = Short.parseShort(year2RecordMaxSnowfallTxt.getText());
        maxSnowfall[2] = Short.parseShort(year3RecordMaxSnowfallTxt.getText());
        climateDayRcd.setSnowDayRecordYear(maxSnowfall);

        climateDayRcd.setSnowGround(dailySnowOnGroundTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(dailySnowOnGroundTxt.getText()));

        return climateDayRcd;
    }

    /**
     * Get day from day selector and month from month selector.
     * 
     * @return dayOfyear in 'mm-dd' format. Same format as day_of_year column in
     *         day_climate_norm table.
     */
    protected String getDayOfyear() {
        int dayIndex = dayCbo.getSelectionIndex() + 1;
        int monthIndex = monthCbo.getSelectionIndex() + 1;

        String dayOfyear = String.format("%02d-%02d", monthIndex, dayIndex);

        return dayOfyear;
    }

    /**
     * get the first/last available record
     * 
     * @param firstOne
     */
    private void setAvailableClimatologyInfo(boolean firstOne) {
        int stationId = getStationId(stationLocTxt.getText());

        GetAvailableDayOfYearRequest request = new GetAvailableDayOfYearRequest(
                firstOne, stationId);
        String dayOfYear = null;
        try {
            dayOfYear = (String) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to get day of year.", e);
        }

        // default in case there's no record available
        if (dayOfYear == null) {
            if (firstOne) {
                dayOfYear = "01-01";
            } else {
                dayOfYear = "12-31";
            }
        }

        boolean isRecordAvailable = false;
        ClimateDayNorm climateDayRcd = null;
        FetchClimateDayRecordRequest fetchRequest = new FetchClimateDayRecordRequest(
                dayOfYear, stationId);
        try {
            climateDayRcd = (ClimateDayNorm) ThriftClient
                    .sendRequest(fetchRequest);
        } catch (VizException e) {
            logger.error("Failed to fetch record. Display default data.", e);
        }

        if (climateDayRcd == null) {
            climateDayRcd = new ClimateDayNorm();
            climateDayRcd.setDataToMissing();
            climateDayRcd.setStationId(stationId);
            climateDayRcd.setDayOfYear(dayOfYear);
        } else {
            isRecordAvailable = true;
        }

        displayClimatologyInfo(climateDayRcd);

        // Update month and day combo
        int monthIndex = 0;
        int dayIndex = 0;

        String[] tokens = dayOfYear.split("-");

        if (tokens.length == 2) {
            try {
                monthIndex = Integer.parseInt(tokens[0]) - 1;
                dayIndex = Integer.parseInt(tokens[1]) - 1;
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse " + dayOfYear, nfe);
            }
        }

        monthCbo.select(monthIndex);
        updateDayCbo();
        dayCbo.select(dayIndex);
        lastDayOfYear = getDayOfyear();

        if (isRecordAvailable) {
            deleteBtn.setEnabled(true);
        } else {
            deleteBtn.setEnabled(false);
        }
    }

    /**
     * save record into DB
     */
    protected void saveRecord() {
        boolean save = MessageDialog.openConfirm(shell, "Save Data?",
                "Warning! You are about to save the changes to database! Proceed?");
        if (!save) {
            return;
        }

        ClimateDayNorm record = getDailyClimatologyInfo();

        boolean recordUpdated = false;
        SaveClimateDayRecordRequest request = new SaveClimateDayRecordRequest(
                record);
        try {
            recordUpdated = (boolean) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to update record with station ID="
                    + record.getStationId() + " and day_of_year="
                    + record.getDayOfYear(), e);
        }

        if (recordUpdated) {
            changeListener.setChangesUnsaved(false);
            deleteBtn.setEnabled(true);
        } else {
            MessageDialog.openError(shell, "Failed to Save",
                    "Failed to save data to database.");
        }
    }

    /**
     * original code secondWindow_DailyClimate.C:activateCB_pBDelete()
     * 
     * Delete a record from day_climate_norm table
     */
    protected void deleteRecord() {
        boolean delete = MessageDialog.openConfirm(shell, "Delete this Record?",
                "Warning! You are about to delete this record! Proceed?");
        if (!delete) {
            return;
        }

        int stationId = getStationId(stationLocTxt.getText());
        String dayOfYear = getDayOfyear();

        boolean recordDeleted = false;
        DeleteClimateDayRecordRequest request = new DeleteClimateDayRecordRequest(
                dayOfYear, stationId);
        try {
            recordDeleted = (boolean) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to delete record with station ID=" + stationId
                    + " and day_of_year=" + dayOfYear, e);
        }
        if (recordDeleted) {
            MessageDialog.openInformation(shell, "Deletion successful",
                    "Record successfully deleted.");
        } else {
            MessageDialog.openInformation(shell, "Deletion unsuccessful",
                    "Failed to delete record.");
        }

        setDailyClimatologyInfo();
    }

    /**
     * is change unsaved?
     * 
     * @return
     */
    public boolean isChangesUnsaved() {
        return changeListener.isChangesUnsaved();
    }

    /**
     * get station ID base in station name
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected int getStationId(String stationName) {
        int stationId = 0;
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        try {
            List<Station> stations = (List<Station>) ThriftClient
                    .sendRequest(request);
            for (Station station : stations) {
                if (stationName.equals(station.getStationName())) {
                    stationId = station.getInformId();
                    break;
                }
            }
        } catch (VizException e) {
            logger.error("Failed to retrieve stations from DB", e);
        }

        return stationId;
    }
}
