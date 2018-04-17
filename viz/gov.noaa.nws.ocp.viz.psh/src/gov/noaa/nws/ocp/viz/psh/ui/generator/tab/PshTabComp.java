/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.spellchecker.dialogs.SpellCheckDlg;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.PshLSRDialog;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTable;

/**
 * Composite containing the skeleton for each tab in the PSH Generator dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2017 #34810      wpaintsil   Initial creation.
 * Jul 05, 2017 #35463      wpaintsil   Delete PshOtherTabComp.java 
 *                                      and merge its functionality back 
 *                                      into PshTabComp.
 * Sep 19, 2017 #36924      astrakovsky Implemented user file parsing.
 * Sep 26, 2017 #38085      wpaintsil   Preview section implementation. 
 *                                      Refactor user file parsing. 
 *                                      Move button to PshRainfallTabComp.
 * Oct 02, 2017 #38376      wpaintsil   Add separate save/edit for Final Remarks.
 * Oct 31, 2017 #40221      jwu         Remove "hasData" checks.
 * Nov 20, 2017 #39868      wpaintsil   Don't call save method if there is no 
 *                                      table data added and the tab began 
 *                                      with no table data.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public abstract class PshTabComp extends Composite {
    /**
     * Enum type indicating which of the tabs is being created.
     */
    protected PshDataCategory tabType;

    /**
     * Composite that can be dynamically created or disposed.
     */
    protected Composite expansionComp = null;

    /**
     * Composite holding most of the controls and text fields for the tab
     */
    protected Composite mainComposite = null;

    /**
     * A table holding data for the tab.
     */
    protected PshTable table;

    /**
     * Font for the title area
     */
    private Font pshTitleFont;

    /**
     * Font for the subtitle area
     */
    private Font otherTitlesFont;

    /**
     * Font for the checkboxes
     */
    protected Font checkboxFont;

    /**
     * Font for the preview area
     */
    private Font previewFont;

    protected StyledText remarksText;

    protected String currentRemarks;

    /**
     * Object pointing to the Psh Generator main window. Used to manipulate its
     * PshData field.
     */
    protected IPshData pshGeneratorData;

    protected Text previewText;

    protected Composite loadExternalComp;

    private Button spellCheckButton;

    private Button cancelButton;

    private Button saveEditButton;

    protected boolean emptyData = false;

    /**
     * Title for data column
     */
    public static final String SITE_LBL_STRING = "Site";

    public static final String LAT_LBL_STRING = "Lat";

    public static final String LON_LBL_STRING = "Lon";

    public static final String PRESSURE_LBL_STRING = "Lowest\nPressure";

    public static final String DATE_LBL_STRING = "Date/Time";

    public static final String SUST_WIND_LBL_STRING = "Sust Wind";

    public static final String PK_WIND_LBL_STRING = "Pk Wind";

    public static final String ANEMHGT_LBL_STRING = "Anemhght";

    public static final String COUNTY_LBL_STRING = "County";

    /**
     * Title for incomplete data column
     */
    protected static final String INCOMPLETE = "I";

    /**
     * Title for estimated data column
     */
    protected static final String ESTIMATED = "E";

    /**
     * Width for some common table columns
     */
    protected static final int SITE_WIDTH = 270;

    protected static final int LAT_WIDTH = 60;

    protected static final int LON_WIDTH = 65;

    protected static final int PRES_WIDTH = 75;

    protected static final int SUST_WIND_WIDTH = 85;

    protected static final int PK_WIND_WIDTH = 80;

    protected static final int DATETIME_WIDTH = 87;

    protected static final int INCOMPLETE_WIDTH = 25;

    protected static final int ANEM_WIDTH = 72;

    protected static final int CITY_WIDTH = 200;

    protected static final int COUNTY_WIDTH = 160;

    protected static final int DIR_DIST_WIDTH = 80;

    /**
     * Direction list
     */
    protected static final List<String> directionList = Arrays.asList(
            new String[] { "None", "N", "NNE", "NE", "ENE", "E", "ESE", "SE",
                    "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" });

    /**
     * The logger
     */
    protected static final IUFStatusHandler logger = UFStatus
            .getHandler(PshTabComp.class);

    /**
     * Constructor
     * 
     * @param parent
     *            The tabfolder object that holds the tabs
     * @param tabType
     *            indicates which of the tabs is being created
     */
    protected PshTabComp(IPshData pshGeneratorData, TabFolder parent,
            PshDataCategory tabType) {
        super(parent, SWT.NONE);

        this.pshGeneratorData = pshGeneratorData;
        TabItem tab = new TabItem(parent, SWT.NONE);
        tab.setText(StringUtils.center(tabType.getName(), 23));

        GridLayout tabLayout = new GridLayout(1, false);

        tabLayout.marginHeight = 0;
        tabLayout.marginWidth = 0;
        tabLayout.verticalSpacing = 0;

        this.setLayout(tabLayout);
        this.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        tab.setControl(this);

        this.tabType = tabType;

        pshTitleFont = PshUtil.createFont(24, SWT.BOLD);

        otherTitlesFont = PshUtil.createFont(15, SWT.BOLD);

        checkboxFont = PshUtil.createFont(9, SWT.NORMAL);

        previewFont = PshUtil.createFont("Courier", 10, SWT.BOLD);

        // Dispose fonts
        this.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (pshTitleFont != null) {
                    pshTitleFont.dispose();
                }
                if (otherTitlesFont != null) {
                    otherTitlesFont.dispose();
                }
                if (checkboxFont != null) {
                    checkboxFont.dispose();
                }

                if (previewFont != null) {
                    previewFont.dispose();
                }
            }
        });

        createMainComposite();

        createControls();
    }

    /**
     * 
     * Create the common title and composite object present in all tabs.
     * 
     * @param tabFolder
     */
    private void createMainComposite() {

        mainComposite = new Composite(this, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);

        mainLayout.marginTop = 10;
        mainLayout.marginBottom = 0;
        mainLayout.marginWidth = 0;
        mainLayout.verticalSpacing = 0;

        mainComposite.setLayout(mainLayout);
        mainComposite
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

    }

    /**
     * The enum type indicating which tab is being created
     * 
     * @return the tab type
     */
    public PshDataCategory getTabType() {
        return tabType;
    }

    /**
     * Create a common set of fields used in three different tabs.
     * 
     * @param parent
     * @param obsLabels
     *            list of label strings used to label the set of text fields
     * @return a map storing the Text objects for each field
     */
    protected Map<String, Text> createCommonTextFields(Composite parent,
            String[] obsLabels) {
        Composite fieldComp = new Composite(parent, SWT.NONE);
        RowLayout fieldLayout = new RowLayout(SWT.HORIZONTAL);
        fieldLayout.spacing = 0;
        fieldComp.setLayout(fieldLayout);

        Map<String, Text> obsFieldMap = new HashMap<>();
        int dateCount = 0;
        for (String labelString : obsLabels) {
            Composite obsComp = new Composite(fieldComp, SWT.NONE);
            GridLayout obsLayout = new GridLayout(1, true);
            obsComp.setLayout(obsLayout);
            obsLayout.horizontalSpacing = 0;
            obsLayout.marginWidth = 0;

            // add a newline character for equal alignment with
            // "LOWEST\nPRESSURE".
            Label obsLabel = new Label(obsComp, SWT.NORMAL);
            obsLabel.setText(labelString
                    + (labelString.equals(PRESSURE_LBL_STRING) ? "" : "\n"));
            obsLabel.setLayoutData(new GridData(GridData.CENTER,
                    GridData.CENTER, true, false));

            Text obsText = new Text(obsComp, SWT.BORDER);
            obsText.setLayoutData(new GridData(70, 15));

            // There is more than one "DATE/TIME" label. Append an index to
            // them to differentiate in the map.
            if (labelString.equals(DATE_LBL_STRING)) {
                labelString += dateCount;
                dateCount++;
            }

            obsFieldMap.put(labelString, obsText);
        }
        return obsFieldMap;
    }

    /**
     * Create a final remarks section used in most tabs.
     * 
     * @param parent
     * @param loadExternal
     *            add the "Load External Files" button if true; false otherwise
     * @param saveEdit
     *            add the "Edit Final Remarks" button if true; false otherwise
     */
    protected void createRemarksArea(Composite parent, boolean loadExternal,
            boolean saveEdit, String labelString) {

        Composite remarksAreaComp = new Composite(parent, SWT.NONE);
        GridLayout remarksLayout = new GridLayout(1, false);
        remarksLayout.marginBottom = 0;
        remarksLayout.marginWidth = 0;
        remarksAreaComp.setLayout(remarksLayout);
        GridData remarksCompData = new GridData(SWT.CENTER, SWT.BOTTOM, false,
                true);
        remarksAreaComp.setLayoutData(remarksCompData);

        SashForm horizontalSashForm = new SashForm(remarksAreaComp,
                SWT.HORIZONTAL);
        GridData sashData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashData.heightHint = 200;
        horizontalSashForm.setLayoutData(sashData);
        horizontalSashForm.SASH_WIDTH = 5;

        // Remarks text field
        createRemarksText(horizontalSashForm, labelString, saveEdit);

        // Preview area
        createPreviewArea(horizontalSashForm);

        horizontalSashForm.setWeights(new int[] { 40, 60 });

        // "Load External Files" button
        if (loadExternal) {

            loadExternalComp = new Composite(remarksAreaComp, SWT.NONE);
            GridLayout loadExternalLayout = new GridLayout(2, true);
            loadExternalLayout.marginWidth = 0;
            loadExternalComp.setLayout(loadExternalLayout);
            loadExternalComp.setLayoutData(
                    new GridData(SWT.LEFT, SWT.FILL, false, false));

            Button loadLSRButton = new Button(loadExternalComp, SWT.PUSH);

            loadLSRButton.setText("Load LSR\nFiles");
            loadLSRButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    new PshLSRDialog(getShell(), PshTabComp.this).open();
                }
            });

        }
    }

    protected void createPreviewArea(Composite parent) {
        createPreviewArea(parent, new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    protected void createRemarksText(Composite parent, String labelString,
            boolean saveEdit) {
        Group remarksTextComp = new Group(parent, SWT.SHADOW_IN);
        remarksTextComp.setText(labelString);
        GridLayout remarksTextLayout = new GridLayout(1, false);
        remarksTextComp.setLayout(remarksTextLayout);
        remarksTextComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        remarksText = new StyledText(remarksTextComp,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        remarksText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttonComp = new Composite(remarksTextComp, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(4, false);
        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        if (saveEdit) {
            setRemarksTextEditable(false);

            saveEditButton = new Button(buttonComp, SWT.PUSH);
            saveEditButton.setText("Edit Final Remarks");

            GridData saveLayoutData = new GridData(SWT.LEFT, SWT.FILL, true,
                    false);
            saveEditButton.setLayoutData(saveLayoutData);

            saveEditButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (saveEditButton.getText().equals("Edit Final Remarks")) {
                        setRemarksTextEditable(true);
                        currentRemarks = remarksText.getText();
                    } else {
                        saveFinalRemarks();
                        setRemarksTextEditable(false);
                    }

                }

            });

            cancelButton = new Button(buttonComp, SWT.PUSH);
            cancelButton.setText("Cancel");

            GridData cancelLayoutData = new GridData(SWT.CENTER, SWT.FILL, true,
                    false);
            cancelLayoutData.horizontalSpan = 2;
            cancelButton.setLayoutData(cancelLayoutData);

            cancelButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    setRemarksText(currentRemarks);
                    setRemarksTextEditable(false);

                }
            });

            cancelButton.setVisible(false);

        }

        spellCheckButton = new Button(buttonComp, SWT.PUSH);
        spellCheckButton.setText("Spell Check");

        GridData spellLayoutData = new GridData(SWT.RIGHT, SWT.FILL, true,
                false);
        spellCheckButton.setLayoutData(spellLayoutData);

        spellCheckButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkSpelling(remarksText);

            }
        });
    }

    /**
     * Toggle editability of remarks.
     * 
     * @param editable
     *            true if editable, false otherwise
     */
    public void setRemarksTextEditable(boolean editable) {
        if (remarksText != null) {
            if (editable) {
                remarksText.setEditable(true);
                remarksText.setBackground(
                        this.getDisplay().getSystemColor(SWT.COLOR_WHITE));

                if (saveEditButton != null) {
                    saveEditButton.setText("Save Final Remarks");
                }

                if (spellCheckButton != null) {
                    spellCheckButton.setEnabled(true);
                }
                if (cancelButton != null) {
                    cancelButton.setVisible(true);
                }

                remarksText.setFocus();
                remarksText.setSelection(remarksText.getText().length());
            } else {
                remarksText.setEditable(false);
                remarksText.setBackground(this.getDisplay()
                        .getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

                if (saveEditButton != null) {
                    saveEditButton.setText("Edit Final Remarks");
                }
                if (spellCheckButton != null) {
                    spellCheckButton.setEnabled(false);
                }
                if (cancelButton != null) {
                    cancelButton.setVisible(false);
                }
            }
        }
    }

    /**
     * Set the text of the remarks text field.
     * 
     * @param text
     */
    public void setRemarksText(String text) {
        if (remarksText != null && text != null) {
            remarksText.setText(text);
        }
    }

    public String getRemarksText() {
        return remarksText == null ? "" : remarksText.getText();
    }

    /**
     * Create a non-editable text field to preview the product text.
     * 
     * @param parent
     */
    protected void createPreviewArea(Composite parent, GridData layoutData) {
        Group previewComp = new Group(parent, SWT.SHADOW_IN);
        previewComp.setText("Preview");
        GridLayout previewLayout = new GridLayout(1, false);
        previewComp.setLayout(previewLayout);
        previewComp.setLayoutData(layoutData);

        previewText = new Text(previewComp,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        GridData previewData = new GridData(SWT.FILL, SWT.FILL, true, true);
        previewData.widthHint = 600;

        previewText.setLayoutData(previewData);
        previewText.setEditable(false);
        previewText.setBackground(this.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        previewText.setFont(previewFont);

    }

    /**
     * Set the preview text to whatever data is currently displayed in the
     * table.
     */
    public abstract void updatePreviewArea();

    /**
     * Add an entry to the current tab's table.
     * 
     * @param data
     *            StormData to add to the table
     */
    public void addItem(StormDataEntry data) {
        if (table != null) {
            table.addItem(data);
        }
    }

    /**
     * Delete all items in the table
     */
    public void clearTable() {
        if (table != null) {
            while (table.size() > 0) {
                table.deleteRow(0);
            }
        }

        setRemarksText("");
    }

    public void cancelEditing() {
        setRemarksTextEditable(false);
        table.cancelEditing();
    }

    /**
     * Displays the spell checker dialog to initiate spell checking.
     */
    protected void checkSpelling(StyledText text) {
        SpellCheckDlg spellCheckDlg = new SpellCheckDlg(getShell(), text, true);
        spellCheckDlg.open();
    }

    protected void saveAlert(boolean saveSuccessful) {
        if (saveSuccessful) {
            new MessageDialog(getShell(), "Save Successful", null,
                    getTabType().getName() + " data for the storm, "
                            + pshGeneratorData.getPshData().getStormName()
                            + ", was successfully saved by the forecaster, "
                            + pshGeneratorData.getPshData().getForecaster()
                            + ".",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();
        } else {
            new MessageDialog(getShell(), "Save Error", null, "Error saving "
                    + getTabType().getName() + " data for the storm, "
                    + pshGeneratorData.getPshData().getStormName() + ".",
                    MessageDialog.ERROR, new String[] { "OK" }, 0).open();
        }
    }

    /**
     * Add the main controls to a tab.
     */
    public abstract void createControls();

    /**
     * Set the tab data shown in the table to data retrieved from the database.
     * 
     * @param pshData
     */
    public abstract void setDataList();

    /**
     * Save data currently entered in the table to the database.
     */
    public abstract void savePshData(List<StormDataEntry> entries);

    /**
     * Save Final Remarks separately in tabs that have Final Remarks.
     */
    public abstract void saveFinalRemarks();
}
