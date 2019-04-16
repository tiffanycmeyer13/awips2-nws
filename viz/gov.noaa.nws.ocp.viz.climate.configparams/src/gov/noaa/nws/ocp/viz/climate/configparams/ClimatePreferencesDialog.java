/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.configparams;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.viz.climate.configparams.support.AnnualDialog;
import gov.noaa.nws.ocp.viz.climate.configparams.support.F6ProductIDsDialog;
import gov.noaa.nws.ocp.viz.climate.configparams.support.StationEditDialog;
import gov.noaa.nws.ocp.viz.climate.configparams.support.ThresholdsDialog;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;

/**
 * 
 * Dialog for "Climate Preferences"
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#   Engineer    Description
 * ------------ --------- ----------- --------------------------
 * Mar 8, 2016            xzhang      Initial creation
 * SEP 15, 2016 20639     wkwock      Add menus and verify/focus-out listener
 * OCT 3, 2016  20639     wkwock      Add background color for hours
 * OCT 14, 2016 20639     wkwock      Correct button text
 * OCT 18, 2016 20639     wkwock      Remove background color. Add modify listener
 * NOV 18, 2016 26407     astrakovsky Disable the "All Uppercase"-"Include Lowercase" toggle in Config
 * 19 DEC 2016  20955     amoore      Move default preferences to common location.
 *                                    Clean up and usability.
 * 20 DEC 2016  26404     amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 21 DEC 2016  26904     wpaintsil   Make this dialog appear in the task bar. Center child dialogs.
 * 27 JUL 2017  33104     amoore      Do not use effectively final functionality, for 1.7 build.
 * 03 AUG 2017  36715     amoore      Adjust sizing to fit text.
 * 12 OCT 2018  DR 20897  dfriedman   Add button to open F6 product ID overrides dialog.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public class ClimatePreferencesDialog extends ClimateCaveChangeTrackDialog {

    /**
     * AM and PM
     */
    private static final String[] AMPM = { "AM", "PM" };

    /**
     * evening time of climate run radio buttons
     */
    private Button eveningTimeOfRdo;

    /**
     * evening as of radio button
     */
    private Button eveningAsOfRdo;

    /**
     * intermediate time of climate run radio button
     */
    private Button intermediateTimeOfRdo;

    /**
     * intermediate as of radio button
     */
    private Button intermediateAsOfRdo;

    /**
     * evening hour spinner
     */
    private Spinner eveningSpnr;

    /**
     * intermediate hour spinner
     */
    private Spinner intermediateSpnr;

    /**
     * Evening AM/PM combo
     */
    private Combo eveningCbo;

    /**
     * intermediate AM/PM combo
     */
    private Combo intermediateCbo;

    /**
     * upper case radio button
     */
    private Button upperCaseRdo;

    /**
     * lower case radio button
     */
    private Button lowerCaseRdo;

    /**
     * negative radio button
     */
    private Button negativeRdo;

    /**
     * M as negative radio button
     */
    private Button negativeMRdo;

    /**
     * colon radio button
     */
    private Button colonYesRdo;

    /**
     * no colon radio button
     */
    private Button colonNoRdo;

    /**
     * asterisk as value radio button
     */
    private Button valueSRdo;

    /**
     * R as value radio button
     */
    private Button valueRRdo;

    /**
     * Edit Station List dialog
     */
    protected StationEditDialog stationEditDialog = null;

    /**
     * View/Edit Annual Period dialog
     */
    protected AnnualDialog altAnnualDialog = null;

    /**
     * Edit user-defined values dialog
     */
    protected ThresholdsDialog thresholdsDialog = null;

    /**
     * Preferences
     */
    protected ClimateGlobal preferenceValues;

    /**
     * bold italic font
     */
    protected Font boldItalicFont;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public ClimatePreferencesDialog(Shell parent) {
        super(parent);
        setText("Climate Preferences");
    }

    @Override
    protected void initializeComponents(final Shell shell) {
        GridLayout shellLayout = new GridLayout(2, true);
        shell.setLayout(shellLayout);

        getFont();

        createMenuBar();
        buildTimeConfigSection();
        buildButtons();
        buildOptionsLine1();
        buildOptionsLine2();
        buildBottomButtons();

        if (!readPreferences()) {
            preferenceValues = ClimateGlobal.getDefaultGlobalValues();
        }
        displayPreferences();
    }

    /**
     * get font
     */
    private void getFont() {
        if (boldItalicFont == null) {
            boldItalicFont = new Font(this.getDisplay(), "Sans", 10,
                    SWT.BOLD | SWT.ITALIC);
        }

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (boldItalicFont != null) {
                    boldItalicFont.dispose();
                }
            }
        });
    }

    /**
     * create menu bar
     */
    private void createMenuBar() {
        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuItem.setText("&File");

        Menu fileMenu = new Menu(menuBar);
        fileMenuItem.setMenu(fileMenu);

        MenuItem saveMI = new MenuItem(fileMenu, SWT.NONE);
        saveMI.setText("&Save");
        saveMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                savePreferences();
            }
        });

        MenuItem closeMI = new MenuItem(fileMenu, SWT.NONE);
        closeMI.setText("&Close");
        closeMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuItem.setText("&Help");

        Menu helpMenu = new Menu(menuBar);
        helpMenuItem.setMenu(helpMenu);

        // Handbook menu item
        MenuItem aboutMI = new MenuItem(helpMenu, SWT.NONE);
        aboutMI.setText("Handbook");
        aboutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("climo_prefs.html");
            }
        });

        shell.setMenuBar(menuBar);
    }

    /**
     * build the text and the negative value option line
     */
    private void buildOptionsLine1() {

        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        Label titleLabel = new Label(shell, SWT.NONE);
        titleLabel.setText("NWWS Product Format");
        titleLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
        titleLabel.setAlignment(SWT.CENTER);
        titleLabel.setFont(boldItalicFont);

        Label caseLabel = new Label(shell, SWT.NONE);
        caseLabel.setText("\nText");
        caseLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 2));
        caseLabel.setAlignment(SWT.CENTER);
        caseLabel.setFont(boldItalicFont);

        Label negativeLabel = new Label(shell, SWT.NONE);
        negativeLabel.setText("Indicate Negative\nValues with");
        negativeLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 2));
        negativeLabel.setAlignment(SWT.CENTER);
        negativeLabel.setFont(boldItalicFont);

        Composite leftComp = new Composite(shell, SWT.NONE);
        leftComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 3));
        leftComp.setLayout(new GridLayout(1, false));

        upperCaseRdo = new Button(leftComp, SWT.RADIO);
        upperCaseRdo.setText("All Uppercase");
        upperCaseRdo.setSelection(true);
        upperCaseRdo.addListener(SWT.Selection, changeListener);
        // Issue #26407 - disable "All Uppercase" option
        upperCaseRdo.setEnabled(false);

        lowerCaseRdo = new Button(leftComp, SWT.RADIO);
        lowerCaseRdo.setText("Include Lowercase");
        lowerCaseRdo.addListener(SWT.Selection, changeListener);
        // Issue #26407 - disable "Include Lowercase" option
        lowerCaseRdo.setEnabled(false);

        Composite rightComp = new Composite(shell, SWT.NONE);
        rightComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 3));
        rightComp.setLayout(new GridLayout(1, false));

        negativeRdo = new Button(rightComp, SWT.RADIO);
        negativeRdo.setText("  -");
        negativeRdo.setSelection(true);
        negativeRdo.addListener(SWT.Selection, changeListener);

        negativeMRdo = new Button(rightComp, SWT.RADIO);
        negativeMRdo.setText("  M");
        negativeMRdo.addListener(SWT.Selection, changeListener);
    }

    /**
     * build the colon and the record indicator option line
     */
    private void buildOptionsLine2() {
        Label caseLabel = new Label(shell, SWT.NONE);
        caseLabel.setText("\nUse Colons");
        caseLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 2));
        caseLabel.setAlignment(SWT.CENTER);
        caseLabel.setFont(boldItalicFont);

        Label negativeLabel = new Label(shell, SWT.NONE);
        negativeLabel.setText("Indicate Record\nValues with");
        negativeLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 2));
        negativeLabel.setAlignment(SWT.CENTER);
        negativeLabel.setFont(boldItalicFont);

        Composite leftComp = new Composite(shell, SWT.NONE);
        leftComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 3));
        leftComp.setLayout(new GridLayout(1, false));

        colonYesRdo = new Button(leftComp, SWT.RADIO);
        colonYesRdo.setText("Yes");

        colonNoRdo = new Button(leftComp, SWT.RADIO);
        colonNoRdo.setText("No");
        colonNoRdo.setSelection(true);
        colonNoRdo.addListener(SWT.Selection, changeListener);

        Composite rightComp = new Composite(shell, SWT.NONE);
        rightComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 3));
        rightComp.setLayout(new GridLayout(1, false));

        valueSRdo = new Button(rightComp, SWT.RADIO);
        valueSRdo.setText("  *");

        valueRRdo = new Button(rightComp, SWT.RADIO);
        valueRRdo.setText("  R");
        valueRRdo.setSelection(true);
        valueRRdo.addListener(SWT.Selection, changeListener);
    }

    /**
     * build the buttons for station list dialog, annual period dialog, and
     * threshold dialog
     */
    private void buildButtons() {
        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        Button stationsBtn = new Button(shell, SWT.PUSH);
        GridData stationGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                2, 1);
        stationGd.widthHint = 320;

        // cave window dimensions and location
        final Point caveLocation = getDisplay().getActiveShell().getLocation();
        final int caveWidth = getDisplay().getActiveShell().getSize().x;
        final int caveHeight = getDisplay().getActiveShell().getSize().y;

        stationsBtn.setLayoutData(stationGd);
        stationsBtn.setText("Set Up Climate Stations");
        stationsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                stationEditDialog = new StationEditDialog(shell);
                stationEditDialog.open();
                // center this dialog based on cave window dimensions and
                // location.
                int stationEditWidth = stationEditDialog.getShell().getSize().x;
                int stationEditHeight = stationEditDialog.getShell()
                        .getSize().y;
                stationEditDialog.getShell().setLocation(new Point(
                        caveLocation.x + (caveWidth - stationEditWidth) / 2,
                        caveLocation.y + (caveHeight - stationEditHeight) / 2));
            }
        });

        Button annualPeriodBtn = new Button(shell, SWT.PUSH);
        GridData annualPeriodGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                false, 2, 1);
        annualPeriodGd.widthHint = 320;
        annualPeriodBtn.setLayoutData(annualPeriodGd);
        annualPeriodBtn.setText("Edit Alternate Annual Periods");
        annualPeriodBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                altAnnualDialog = new AnnualDialog(shell);

                altAnnualDialog.open();

                // center this dialog based on cave window dimensions and
                // location.
                int altAnnualWidth = altAnnualDialog.getShell().getSize().x;
                int altAnnualHeight = altAnnualDialog.getShell().getSize().y;
                altAnnualDialog.getShell().setLocation(new Point(
                        caveLocation.x + (caveWidth - altAnnualWidth) / 2,
                        caveLocation.y + (caveHeight - altAnnualHeight) / 2));
            }
        });

        Button userValuesBtn = new Button(shell, SWT.PUSH);
        GridData userValuesGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                false, 2, 1);
        userValuesGd.widthHint = 320;
        userValuesBtn.setLayoutData(userValuesGd);
        userValuesBtn.setText("Edit User-Defined Threshholds");
        userValuesBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                thresholdsDialog = new ThresholdsDialog(shell,
                        preferenceValues);

                thresholdsDialog.open();

                // center this dialog based on cave window dimensions and
                // location.
                int thresholdsWidth = thresholdsDialog.getShell().getSize().x;
                int thresholdsHeight = thresholdsDialog.getShell().getSize().y;
                thresholdsDialog.getShell().setLocation(new Point(
                        caveLocation.x + (caveWidth - thresholdsWidth) / 2,
                        caveLocation.y + (caveHeight - thresholdsHeight) / 2));
            }
        });

        Button prodIdBtn = new Button(shell, SWT.PUSH);
        GridData prodIdGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                false, 2, 1);
        prodIdGd.widthHint = 320;
        prodIdBtn.setLayoutData(prodIdGd);
        prodIdBtn.setText("Edit F6 Product ID Overrides");
        prodIdBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                F6ProductIDsDialog f6ProductIDsDialog = new F6ProductIDsDialog(
                        shell, preferenceValues);

                f6ProductIDsDialog.open();

                // center this dialog based on cave window dimensions and
                // location.
                int width = f6ProductIDsDialog.getShell().getSize().x;
                int height = f6ProductIDsDialog.getShell().getSize().y;
                f6ProductIDsDialog.getShell().setLocation(new Point(
                        caveLocation.x + (caveWidth - width) / 2,
                        caveLocation.y + (caveHeight - height) / 2));
            }
        });
    }

    /**
     * Build the time configuration section
     */
    private void buildTimeConfigSection() {
        Composite leftGrp = new Composite(shell, SWT.NONE);
        leftGrp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        leftGrp.setLayout(new GridLayout(2, true));

        Label eveningLbl = new Label(leftGrp, SWT.NONE);
        eveningLbl.setText("Valid Time for\nDaily Evening reports:");
        GridData eveningLblGd = new GridData(SWT.FILL, SWT.CENTER, false, false,
                2, 1);
        eveningLblGd.widthHint = 212;
        eveningLbl.setLayoutData(eveningLblGd);
        eveningLbl.setAlignment(SWT.CENTER);
        eveningLbl.setFont(boldItalicFont);

        eveningTimeOfRdo = new Button(leftGrp, SWT.RADIO);
        eveningTimeOfRdo.setText("Time of Climate Run");
        eveningTimeOfRdo.setSelection(true);
        GridData eveningTimeOfRdoGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 2, 1);
        eveningTimeOfRdoGD.horizontalIndent = 20;
        eveningTimeOfRdo.setLayoutData(eveningTimeOfRdoGD);
        eveningTimeOfRdo.addListener(SWT.Selection, changeListener);

        eveningAsOfRdo = new Button(leftGrp, SWT.RADIO);
        eveningAsOfRdo.setText("As Of");
        GridData eveningAsOfRdoGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 2, 1);
        eveningAsOfRdoGD.horizontalIndent = 20;
        eveningAsOfRdo.setLayoutData(eveningAsOfRdoGD);
        eveningAsOfRdo.addListener(SWT.Selection, changeListener);

        eveningSpnr = new Spinner(leftGrp, SWT.BORDER);
        GridData eveningSpnrGd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        eveningSpnrGd.widthHint = 30;
        eveningSpnr.setLayoutData(eveningSpnrGd);
        eveningSpnr.setValues(5, 0, 12, 0, 1, 1);
        eveningSpnr.addListener(SWT.Modify, changeListener);

        eveningCbo = new Combo(leftGrp,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        eveningCbo.setItems(AMPM);
        eveningCbo.select(1);
        eveningCbo.addListener(SWT.Modify, changeListener);

        Composite rightGrp = new Composite(shell, SWT.NONE);
        rightGrp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        rightGrp.setLayout(new GridLayout(2, true));

        Label interLbl = new Label(rightGrp, SWT.NONE);
        interLbl.setText("Valid Time for\nDaily Intermediate reports:");
        GridData interLblGd = new GridData(SWT.FILL, SWT.CENTER, false, false,
                2, 1);
        interLblGd.widthHint = 222;
        interLbl.setLayoutData(interLblGd);
        interLbl.setAlignment(SWT.CENTER);
        interLbl.setFont(boldItalicFont);

        intermediateTimeOfRdo = new Button(rightGrp, SWT.RADIO);
        intermediateTimeOfRdo.setText("Time of Climate Run");
        intermediateTimeOfRdo.setSelection(true);
        GridData interTimeOfRdoGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 2, 1);
        interTimeOfRdoGD.horizontalIndent = 30;
        intermediateTimeOfRdo.setLayoutData(interTimeOfRdoGD);
        intermediateTimeOfRdo.addListener(SWT.Selection, changeListener);

        intermediateAsOfRdo = new Button(rightGrp, SWT.RADIO);
        intermediateAsOfRdo.setText("As Of");
        GridData interAsOfRdoGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 2, 1);
        interAsOfRdoGD.horizontalIndent = 30;
        intermediateAsOfRdo.setLayoutData(interAsOfRdoGD);
        intermediateAsOfRdo.addListener(SWT.Selection, changeListener);

        intermediateSpnr = new Spinner(rightGrp, SWT.BORDER);
        GridData intermediateSpnrGd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        intermediateSpnrGd.widthHint = 30;
        intermediateSpnr.setLayoutData(intermediateSpnrGd);
        intermediateSpnr.setValues(9, 0, 12, 0, 1, 1);
        intermediateSpnr.addListener(SWT.Modify, changeListener);

        intermediateCbo = new Combo(rightGrp,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);

        intermediateCbo.setItems(AMPM);
        intermediateCbo.select(0);
        intermediateCbo.addListener(SWT.Modify, changeListener);
    }

    /**
     * build the save and the cancel buttons
     */
    private void buildBottomButtons() {

        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        Button saveBtn = new Button(shell, SWT.PUSH);
        GridData saveGd = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,
                1);
        saveGd.widthHint = 120;
        saveBtn.setLayoutData(saveGd);
        saveBtn.setText("Save");

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                savePreferences();
            }
        });

        Button cancelBtn = new Button(shell, SWT.PUSH);
        GridData cancelGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        cancelGd.widthHint = 120;
        cancelBtn.setLayoutData(cancelGd);
        cancelBtn.setText("Cancel");

        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * get the preferences from GUI and save it to file
     */
    protected void savePreferences() {
        boolean save = MessageDialog.openQuestion(shell, "Save?",
                "Save preferences to file?");
        if (save) {
            ClimateGlobal preferenceValues = getPreferences();
            writePreferences(preferenceValues);
        }
    }

    /**
     * get preference values from GUI
     * 
     * @return ClimateGlobal
     */
    private ClimateGlobal getPreferences() {
        preferenceValues.setUseValidPm(eveningAsOfRdo.getSelection());
        preferenceValues.setUseValidIm(intermediateAsOfRdo.getSelection());
        preferenceValues.setNoSmallLetters(upperCaseRdo.getSelection());
        preferenceValues.setNoAsterisk(valueRRdo.getSelection());
        preferenceValues.setNoMinus(negativeMRdo.getSelection());
        preferenceValues.setNoColon(colonNoRdo.getSelection());

        int hour = intermediateSpnr.getSelection();

        String amPm = intermediateCbo
                .getItem(intermediateCbo.getSelectionIndex());
        if (amPm.equalsIgnoreCase(AMPM[1]) && hour != 12) {
            hour += 12;
        } else if (amPm.equalsIgnoreCase(AMPM[0]) && hour == 12) {
            hour = 0;
        }

        preferenceValues.getValidIm().setAmpm(amPm);
        preferenceValues.getValidIm().setHour(hour);

        // Get hour of daily evening
        amPm = eveningCbo.getItem(eveningCbo.getSelectionIndex());
        preferenceValues.getValidPm().setAmpm(amPm);
        hour = eveningSpnr.getSelection();

        if (amPm.equalsIgnoreCase(AMPM[1]) && hour != 12) {
            hour += 12;
        } else if (amPm.equalsIgnoreCase(AMPM[0]) && hour == 12) {
            hour = 0;
        }
        preferenceValues.getValidPm().setHour(hour);

        return preferenceValues;
    }

    /**
     * read preferences from file
     * 
     */
    private boolean readPreferences() {
        boolean successFlag = true;
        ClimateRequest cr = new ClimateRequest();
        cr.setRequestType(RequestType.GET_GLOBAL);
        try {
            ClimateGlobal tmpPreferences = (ClimateGlobal) ThriftClient
                    .sendRequest(cr);
            if (tmpPreferences == null) {
                successFlag = false;
            } else {
                preferenceValues = tmpPreferences;
            }
        } catch (VizException e) {
            logger.error("Failed to read preferences.", e);
            successFlag = false;
        }

        return successFlag;
    }

    /**
     * Save preferences to file
     * 
     * @param preferenceValues
     */
    private void writePreferences(ClimateGlobal preferenceValues) {
        String message = "Preferences saved to globalDay.properties";
        ClimateRequest cr = new ClimateRequest();
        cr.setRequestType(RequestType.SAVE_GLOBAL);
        cr.setClimateGlobal(preferenceValues);
        try {
            int status = (int) ThriftClient.sendRequest(cr);

            if (status == 0) {
                changeListener.setChangesUnsaved(false);
            } else if (status == -1) {
                message = "Error: Could not open data file for writing.";
            } else if (status == -2) {
                message = "Error: Could not close data file after writing.";
            }
        } catch (VizException e) {
            logger.error("Failed to save preferences.", e);
            message = "Error: Failed to save data.";
        }

        MessageDialog.openInformation(shell, "Saving of Preferences", message);
    }

    /**
     * display preferences on GUI
     */
    private void displayPreferences() {
        changeListener.setIgnoreChanges(true);

        eveningTimeOfRdo.setSelection(!preferenceValues.isUseValidPm());
        eveningAsOfRdo.setSelection(preferenceValues.isUseValidPm());
        int hour = preferenceValues.getValidPm().getHour() % 12;
        if (preferenceValues.getValidPm().getAmpm().equals(AMPM[1])) {
            eveningCbo.select(1);
            if (hour == 0) {
                hour = 12;
            }
        } else {
            eveningCbo.select(0);
        }
        eveningSpnr.setSelection(hour);

        intermediateTimeOfRdo.setSelection(!preferenceValues.isUseValidIm());
        intermediateAsOfRdo.setSelection(preferenceValues.isUseValidIm());
        hour = preferenceValues.getValidIm().getHour() % 12;
        if (preferenceValues.getValidIm().getAmpm().equals(AMPM[1])) {
            intermediateCbo.select(1);
            if (hour == 0) {
                hour = 12;
            }
        } else {
            intermediateCbo.select(0);
        }
        intermediateSpnr.setSelection(hour);

        upperCaseRdo.setSelection(preferenceValues.isNoSmallLetters());
        lowerCaseRdo.setSelection(!preferenceValues.isNoSmallLetters());

        negativeRdo.setSelection(!preferenceValues.isNoMinus());
        negativeMRdo.setSelection(preferenceValues.isNoMinus());

        colonYesRdo.setSelection(!preferenceValues.isNoColon());
        colonNoRdo.setSelection(preferenceValues.isNoColon());

        valueSRdo.setSelection(!preferenceValues.isNoAsterisk());
        valueRRdo.setSelection(preferenceValues.isNoAsterisk());

        changeListener.setIgnoreChanges(false);
        changeListener.setChangesUnsaved(false);
    }
}
