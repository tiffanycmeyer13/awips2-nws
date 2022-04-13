/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;

import gov.noaa.nws.ocp.common.localization.psh.PshBasin;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigHeader;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshExportType;
import gov.noaa.nws.ocp.common.localization.psh.PshTimeZone;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Implementation of the PSH Program configuration dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 17, 2017 #34294     astrakovsky  Initial creation.
 * Jun 15, 2017 #34873     astrakovsky  Refactored to use a new superclass.
 * Jun 26  2017 #35269     jwu          Implemented retrival/saving.
 * Jun 28, 2017 #35465     astrakovsky  Fixed error displaying a field.
 * Aug 30, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Sep 08, 2017 #37365     jwu          Change time zone/basin to Combo.
 * Sep 25, 2017 #36924     astrakovsky  Disabled some fields.
 * Dec 05, 2017 #41620     wpaintsil    Add xml product export options.
 * Dec 11, 2017 #41998     jwu          Use localization access control file in base/roles.
 * Feb 15, 2018 #46354     wpaintsil    Various refactorings.
 * May 24, 2021 20652      wkwock       Change East to Eastern
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshSetupConfigDialog extends PshMultiFieldSetupDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int COMMON_BUTTON_HEIGHT = 28;

    private static final int COMMON_BUTTON_WIDTH = 120;

    private static final int SHAPEFILE_BUTTON_HEIGHT = 40;

    private static final int FIELD_BUTTON_WIDTH = 80;

    private static final int FIELD_BUTTON_HEIGHT = 30;

    private static final int BUTTON_SPACING_HORIZONTAL = 45;

    private static final int BUTTON_SPACING_VERTICAL = 24;

    private static final int LONG_FIELD_WIDTH = 490;

    private static final int SHORT_FIELD_WIDTH = 130;

    private static final int BROWSE_FIELD_WIDTH = 240;

    /**
     * Dialogs for configuration and examples.
     */
    private PshSetupExampleFormDialog setupConfigExampleForm = null;

    private PshShapeFileSetupDialog shapeFileSetupDialog = null;

    private PshSetupExampleImageDialog lsrWmoNodeExampleDialog = null;

    /**
     * List containing the text fields.
     */
    private List<Text> textFieldList;

    /**
     * Widgets for time zones.
     */
    private Combo timeZoneCombo;

    private Text timeOffsetText;

    private Text daylightSavingText;

    /**
     * Combo for PSH basin.
     */
    private Combo basinCombo;

    /**
     * Check button to turn on/off "mixed case".
     */
    private Button mixedCaseChkBtn;

    /**
     * PSH configuration header.
     */
    private PshConfigHeader pshConfigHeader;

    /**
     * Combo for file export options
     */
    private Combo exportCombo;

    /**
     * Preserve the last selected user directory in the export field.
     */
    private String userDir = "";

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshSetupConfigDialog(Shell parentShell) {
        super(parentShell);
        setDialogTitle("Post Storm Report Configuration Setup");

        pshConfigHeader = PshConfigurationManager.getInstance()
                .getConfigHeader();

    }

    /**
     * Creates buttons, and other controls in the dialog area
     * 
     * @param top
     */
    @Override
    protected final void initializeComponents(Composite top) {

        // Set dialog title
        getShell().setText(getDialogTitle());

        // Set dialog label
        Label setupLabel = new Label(top, SWT.CENTER);
        setupLabel.setText(getDialogLabel());
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        setupLabel.setLayoutData(gd);

        // Main component for field and button groups
        Composite mainComp = new Composite(top, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        mainComp.setLayout(gl);

        // Create the fields
        Composite fieldsGroup = new Composite(mainComp, SWT.NONE);
        createLabeledFields(fieldsGroup);

        // Create the buttons
        Composite buttonsGroup = new Composite(mainComp, SWT.NONE);
        createButtons(buttonsGroup);

        fieldsGroup.pack();
        buttonsGroup.pack();
        mainComp.pack();

    }

    /**
     * Creates the labeled fields.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected final void createLabeledFields(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        parent.setLayout(gl);

        textFieldList = new ArrayList<>();

        // Create fields with labels
        textFieldList.add(PshUtil.createLabeledField(parent, "WFO Header :  ",
                "", LONG_FIELD_WIDTH, true));
        textFieldList.add(PshUtil.createLabeledField(parent, "WFO NODE :  ", "",
                LONG_FIELD_WIDTH, true));
        textFieldList.add(PshUtil.createLabeledField(parent, "HEADER :  ", "",
                LONG_FIELD_WIDTH, true));
        textFieldList.add(PshUtil.createLabeledField(parent, "STATION :  ", "",
                LONG_FIELD_WIDTH, true));
        textFieldList.add(PshUtil.createLabeledField(parent, "PIL :  ", "",
                LONG_FIELD_WIDTH, true));
        textFieldList.add(PshUtil.createLabeledField(parent, "PILFILE1 :  ", "",
                LONG_FIELD_WIDTH, true));
        textFieldList
                .add(PshUtil.createLabeledField(parent, "GUI DIRECTORY :  ",
                        PshSetupConfigDialog.class.getProtectionDomain()
                                .getCodeSource().getLocation().getPath(),
                        LONG_FIELD_WIDTH, false));
        textFieldList.add(PshUtil.createLabeledField(parent,
                "LSR/CITIES DIRECTORY :  ", "", LONG_FIELD_WIDTH, false));

        // Add a combo for time zone choice.
        Label timeZoneLabel = new Label(parent, SWT.RIGHT);
        timeZoneLabel.setText("TIME ZONE :  ");
        GridData gd1 = new GridData();
        gd1.horizontalAlignment = SWT.RIGHT;
        timeZoneLabel.setLayoutData(gd1);

        timeZoneCombo = new Combo(parent,
                SWT.BORDER | SWT.READ_ONLY | SWT.LEFT);
        GridData gd2 = new GridData();
        gd2.widthHint = 140;
        timeZoneCombo.setLayoutData(gd2);

        for (PshTimeZone ptz : PshTimeZone.values()) {
            timeZoneCombo.add(ptz.getName());
        }

        timeZoneCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PshTimeZone ntz = PshTimeZone
                        .getPshTimeZone(timeZoneCombo.getText());
                timeOffsetText.setText("" + ntz.getTimeOffset());
                daylightSavingText.setText(ntz.isDaylightSaving() ? "Y" : "N");
            }
        });

        timeZoneCombo.select(1);

        // Time offset and daylight saving.
        timeOffsetText = PshUtil.createLabeledField(parent,
                "TIME DIFFERENCE :  ", "", SHORT_FIELD_WIDTH, false);
        textFieldList.add(timeOffsetText);

        daylightSavingText = PshUtil.createLabeledField(parent,
                "DAYLIGHT SAVINGS? (Y/N) :  ", "", SHORT_FIELD_WIDTH, false);
        textFieldList.add(daylightSavingText);

        // Add a combo for PSH basin.
        Label basinLabel = new Label(parent, SWT.RIGHT);
        basinLabel.setText("TC BASIN :  ");
        GridData gd3 = new GridData();
        gd3.horizontalAlignment = SWT.RIGHT;
        basinLabel.setLayoutData(gd3);

        basinCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY | SWT.LEFT);
        GridData gd4 = new GridData();
        gd4.widthHint = 140;
        basinCombo.setLayoutData(gd4);

        for (PshBasin ptz : PshBasin.values()) {
            basinCombo.add(ptz.getName());
        }

        basinCombo.select(0);

        // LSR header.
        textFieldList.add(PshUtil.createLabeledField(parent,
                "LSR HEADER (Ex MIALSRTBW) :  ", "", SHORT_FIELD_WIDTH, true));

        // create special field with example button
        Label fieldName = new Label(parent, SWT.RIGHT);
        fieldName.setText("LSR WMO NODE (Ex NWUS52) :  ");

        Composite subComp = new Composite(parent, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        subComp.setLayout(gl);

        Text textField = new Text(subComp, SWT.BORDER | SWT.LEFT);
        GridData gd = new GridData();
        gd.widthHint = SHORT_FIELD_WIDTH;
        textField.setLayoutData(gd);
        textField.setEnabled(true);
        textFieldList.add(textField);

        createButton(subComp, "Example", FIELD_BUTTON_WIDTH,
                FIELD_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        openLsrWmoNodeExampleDialog();
                    }
                });

        // Add a check button for "mixed case".
        Label mixedCaseLabel = new Label(parent, SWT.RIGHT);
        mixedCaseLabel.setText("USE MIXED CASE :  ");
        GridData gd5 = new GridData();
        gd5.horizontalAlignment = SWT.RIGHT;
        mixedCaseLabel.setLayoutData(gd5);

        mixedCaseChkBtn = new Button(parent, SWT.CHECK | SWT.LEFT | SWT.BORDER);
        mixedCaseChkBtn.setSelection(false);
        mixedCaseChkBtn.setEnabled(true);

        // Options dropdown for product export options
        Label exportDirLabel = new Label(parent, SWT.RIGHT);
        exportDirLabel.setText("EXPORT TO DIRECTORY :  ");
        GridData gd6 = new GridData();
        gd6.horizontalAlignment = SWT.RIGHT; 
        exportDirLabel.setLayoutData(gd6);

        Composite directoryComp = new Composite(parent, SWT.NONE);
        gl = new GridLayout(3, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        directoryComp.setLayout(gl);

        exportCombo = new Combo(directoryComp,
                SWT.BORDER | SWT.READ_ONLY | SWT.LEFT);
        GridData gd7 = new GridData();
        gd7.widthHint = 140;
        exportCombo.setLayoutData(gd7);

        for (PshExportType expt : PshExportType.values()) {
            exportCombo.add(expt.getName());
        }

        exportCombo.select(0);

        Text exportTextField = new Text(directoryComp, SWT.BORDER | SWT.LEFT);
        GridData browseGridData = new GridData();
        browseGridData.widthHint = BROWSE_FIELD_WIDTH;
        exportTextField.setLayoutData(browseGridData);
        exportTextField.setEnabled(false);

        textFieldList.add(exportTextField);

        DirectoryDialog browseDialog = new DirectoryDialog(getShell());
        Button browseButton = createButton(directoryComp, "Browse",
                FIELD_BUTTON_WIDTH, FIELD_BUTTON_HEIGHT,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String folderText = browseDialog.open();
                        if (folderText != null) {
                            exportTextField.setText(folderText);
                            userDir = folderText;
                        }
                    }
                });
        browseButton.setEnabled(false);

        exportCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (exportCombo.getText()
                        .equals(PshExportType.USER.getName())) {
                    browseButton.setEnabled(true);
                    exportTextField.setEnabled(true);
                    exportTextField.setEditable(true);
                    exportTextField.setBackground(exportTextField.getDisplay()
                            .getSystemColor(SWT.COLOR_WHITE));
                    exportTextField.setText(userDir);

                } else if (exportCombo.getText()
                        .equals(PshExportType.LOCALIZATION.getName())) {
                    browseButton.setEnabled(false);
                    exportTextField.setEnabled(true);
                    exportTextField.setBackground(exportTextField.getDisplay()
                            .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                    exportTextField.setEditable(false);

                    IPathManager pm = PathManagerFactory.getPathManager();

                    exportTextField.setText("/awips2/edex/data/utility/"
                            + pm.getContext(LocalizationType.COMMON_STATIC,
                                    LocalizationLevel.SITE).toPath());

                } else {
                    browseButton.setEnabled(false);
                    exportTextField.setEnabled(false);
                    exportTextField.setText("");

                }
            }
        });

        // Fill in the header info
        fillHeader(pshConfigHeader);

    }

    /**
     * Creates the buttons.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected final void createButtons(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = BUTTON_SPACING_HORIZONTAL;
        parent.setLayout(gl);

        Composite topButtons = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.verticalSpacing = 30;
        topButtons.setLayout(topLayout);

        // create get button
        createButton(topButtons, "Get Current", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        pshConfigHeader = PshConfigurationManager.getInstance()
                                .getConfigHeader();
                        fillHeader(pshConfigHeader);
                    }
                });

        // create save button
        createButton(topButtons, "Save", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        saveConfigHeader();
                    }
                });

        // create example form button
        createButton(topButtons, "Example Form", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        openExampleForm();
                    }
                });

        // create select CWAs button
        createButton(topButtons, "Select CWAs", COMMON_BUTTON_WIDTH,
                SHAPEFILE_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        openShapeFileSetupDialog();
                    }
                });

        Composite exitComposite = new Composite(parent, SWT.NONE);
        GridLayout exitLayout = new GridLayout();
        exitLayout.marginTop = 60;
        exitComposite.setLayout(exitLayout);

        // create exit button
        createButton(exitComposite, "Exit", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        close();
                    }
                });
    }

    /**
     * Open Example Form
     */
    private void openExampleForm() {

        // create dialog
        if (setupConfigExampleForm == null
                || !setupConfigExampleForm.isOpen()) {

            // prepare dialog content
            List<String> fieldsList = new ArrayList<String>();
            List<String> notesList = new ArrayList<String>();

            fieldsList.add("WFO HEADER :  ");
            fieldsList.add("PSHTBW");
            fieldsList.add("WFO NODE :  ");
            fieldsList.add("KTBW");
            fieldsList.add("HEADER :  ");
            fieldsList.add("POST TROPICAL CYCLONE REPORT...HURRICANE");
            fieldsList.add("STATION :  ");
            fieldsList
                    .add("NATIONAL WEATHER SERVICE TAMPA BAY AREA - RUSKIN FL");
            fieldsList.add("PIL :  ");
            fieldsList.add("MIAPSHTBW");
            fieldsList.add("PILFILE1 :  ");
            fieldsList.add("KTBWPSHTBW");
            fieldsList.add("GUI DIRECTORY :  ");
            fieldsList.add(PshSetupExampleFormDialog.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath());
            fieldsList.add("LSR/CITIES DIRECTORY :  ");
            fieldsList.add("/data/fxa/tstorm/");
            fieldsList.add("*TIME ZONE (A/E/C/M/P/H/CH) :  ");
            fieldsList.add("E");
            fieldsList.add("**TIME DIFFERENCE :  ");
            fieldsList.add("5");
            fieldsList.add("***DAYLIGHT SAVINGS? (Y/N) :  ");
            fieldsList.add("Y");
            fieldsList.add("****TC BASIN (AT/EP/CP/WP) :  ");
            fieldsList.add("AT");
            fieldsList.add("LSR HEADER :  ");
            fieldsList.add("MIALSRTBW");
            fieldsList.add("LSR WMO NODE :  ");
            fieldsList.add("NWUS52");

            notesList.add("                *");
            notesList
                    .add("A = Atlantic Time Zone, E = Eastern Time Zone, etc.");
            notesList.add("                **");
            notesList.add("Hours to GMT. For EST, Time Difference is 5 hours.");
            notesList.add("                ***");
            notesList.add("Indicate if Daylight Savings is observed (Y/N).");
            notesList.add("                ****");
            notesList.add(
                    "Indicate the Storm Basin area you are in (AT = Atlantic, EP = Eastern Pacific, etc.");

            setupConfigExampleForm = new PshSetupExampleFormDialog(getShell(),
                    "Post Storm Report Configuration Example Form",
                    "POST TROPICAL STORM REPORT CONFIGURATION FILE (EXAMPLE)",
                    fieldsList, notesList, 1);
        }

        // open dialog
        setupConfigExampleForm.open();

    }

    /**
     * Open Shape File Configuration Setup Menu.
     */
    private void openShapeFileSetupDialog() {
        if (shapeFileSetupDialog == null || !shapeFileSetupDialog.isOpen()) {
            shapeFileSetupDialog = new PshShapeFileSetupDialog(getShell());
        }
        shapeFileSetupDialog.open();
    }

    /**
     * Open LSR WMO NODE Example Dialog.
     */
    private void openLsrWmoNodeExampleDialog() {
        if (lsrWmoNodeExampleDialog == null
                || !lsrWmoNodeExampleDialog.isOpen()) {
            lsrWmoNodeExampleDialog = new PshSetupExampleImageDialog(getShell(),
                    "LSRWMO NODE EXAMPLE", "lsr.gif");
        }
        lsrWmoNodeExampleDialog.open();
    }

    /**
     * Fill in the configuration.
     * 
     * @param header
     *            PshConfigHeader
     */
    private void fillHeader(PshConfigHeader header) {
        textFieldList.get(0).setText(header.getWfoHeader());
        textFieldList.get(1).setText(header.getWfoNode());
        textFieldList.get(2).setText(header.getProductHeader());
        textFieldList.get(3).setText(header.getProductStation());
        textFieldList.get(4).setText(header.getProductPil());
        textFieldList.get(5).setText(header.getPilFile());
        textFieldList.get(6).setText(header.getGuiDir());
        textFieldList.get(7).setText(header.getLsrCitiesDir());

        timeZoneCombo.setText(header.getTimeZone().toString());

        textFieldList.get(8).setText("" + header.getTimeDifference());
        textFieldList.get(9).setText(header.isDaylightSaving() ? "Y" : "N");

        basinCombo.setText(header.getTcBasin().getName());

        textFieldList.get(10).setText(header.getLsrHeader());
        textFieldList.get(11).setText(header.getLsrWmoNode());

        mixedCaseChkBtn.setSelection(header.isUseMixedCase());

        exportCombo.setText(header.getExportProduct().getName());
        exportCombo.notifyListeners(SWT.Selection, new Event());

        textFieldList.get(12).setText(header.getExportDir());
        userDir = textFieldList.get(12).getText();

    }

    /**
     * Get configuration values from GUI and save into localization.
     */
    private void saveConfigHeader() {

        // Retrieve from GUI
        PshConfigHeader header = buildConfigHeader();

        // Save to "config_headers.xml" in localization SITE level.
        PshConfigurationManager.getInstance().saveConfigHeader(header);

        // Inform the user
        String message = "PSH program configuration have been saved into Localization store.";
        MessageDialog infoDlg = new MessageDialog(getShell(),
                "Save Program Configuration", null, message,
                MessageDialog.INFORMATION, new String[] { "Ok" }, 0);

        infoDlg.open();

    }

    /**
     * Build a header and fill configuration values from GUI.
     * 
     * @return PshConfigHeader
     */
    private PshConfigHeader buildConfigHeader() {

        // Retrieve value from GUI
        PshConfigHeader header = PshConfigHeader.getDefaultHeader();
        String value = textFieldList.get(0).getText();
        if (value != null) {
            header.setWfoHeader(value);
        }

        value = textFieldList.get(1).getText();
        if (value != null) {
            header.setWfoNode(value);
        }

        value = textFieldList.get(2).getText();
        if (value != null) {
            header.setProductHeader(value);
        }

        value = textFieldList.get(3).getText();
        if (value != null) {
            header.setProductStation(value);
        }

        value = textFieldList.get(4).getText();
        if (value != null) {
            header.setProductPil(value);
        }

        value = textFieldList.get(5).getText();
        if (value != null) {
            header.setPilFile(value);
        }

        value = textFieldList.get(6).getText();
        if (value != null) {
            header.setGuiDir(value);
        }

        value = textFieldList.get(7).getText();
        if (value != null) {
            header.setLsrCitiesDir(value);
        }

        PshTimeZone ptz = PshTimeZone.getPshTimeZone(timeZoneCombo.getText());
        header.setTimeZone(ptz);
        header.setTimeDifference(ptz.getTimeOffset());
        header.setDaylightSaving(ptz.isDaylightSaving());

        value = basinCombo.getText();
        if (value != null) {
            header.setTcBasin(PshBasin.getPshBasin(value));
        }

        value = textFieldList.get(10).getText();
        if (value != null) {
            header.setLsrHeader(value);
        }

        value = textFieldList.get(11).getText();
        if (value != null) {
            header.setLsrWmoNode(value);
        }

        header.setUseMixedCase(mixedCaseChkBtn.getSelection());

        value = exportCombo.getText();
        if (value != null) {
            header.setExportProduct(PshExportType.getType(value));
        }

        value = textFieldList.get(12).getText();
        if (value != null) {
            header.setExportDir(value);
        }

        return header;
    }

    @Override
    protected final void saveItems(boolean displayMessage) {
        // do nothing
    }

    @Override
    protected final void createEditors(TableItem item) {
        // do nothing
    }

}