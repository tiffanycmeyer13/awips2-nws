/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
**/
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.FixMicrowaveSatelliteTypes;
import gov.noaa.nws.ocp.common.atcf.configuration.FixScatSatTypes;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for editing ATCF Stallite types Microwave and Scatterometer.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2019 63379       dmanzella   initial creation
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChangeSatelliteTypesDialog extends OcpCaveSWTDialog {

    private FixScatSatTypes scatSat;

    private FixMicrowaveSatelliteTypes microSat;

    private Table table;

    /**
     * An enum that differentiates whether this dialog is showing Microwave or
     * Scatterometer data
     */
    private SatelliteTypes satType;

    /**
     * A Map containing editors for each TableItem in the table.
     */
    private Map<TableItem, TableEditor> editors = new LinkedHashMap<>();

    /**
     * Constructor
     * 
     * @param parent
     * @param satelliteType
     */
    public ChangeSatelliteTypesDialog(Shell parent,
            SatelliteTypes satelliteType) {
        super(parent);

        satType = satelliteType;

        setText("Change " + satType.description + " Information");
    }

    /**
     * Initializes the components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createContents();
    }

    /**
     * Create contents.
     */
    protected void createContents() {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        mainComposite.setLayoutData(mainLayoutData);

        if (satType == SatelliteTypes.MICROWAVE) {
            loadMicroSat();
        } else if (satType == SatelliteTypes.SCAT) {
            loadScatSat();
        }

        createTableSection(mainComposite);
        createControlButtons(mainComposite);
        createInfoEntries();
    }

    /**
     * Puts the entries into the table
     */
    protected void createInfoEntries() {
        // Load the satellite entries into the table
        int len = 0;
        if (satType == SatelliteTypes.MICROWAVE) {
            len = microSat.getMicroSats().size();
        } else if (satType == SatelliteTypes.SCAT) {
            len = scatSat.getScatSats().size();
        }

        for (int ii = 0; ii < len; ii++) {
            TableItem item = new TableItem(table, SWT.NULL);
            String name = "";
            if (satType == SatelliteTypes.MICROWAVE) {
                name = microSat.getMicroSats().get(ii);
            } else if (satType == SatelliteTypes.SCAT) {
                name = scatSat.getScatSats().get(ii);
            }

            Text siteNameText = new Text(table, SWT.BORDER);
            siteNameText.setTextLimit(4);
            siteNameText.setText(name);
            siteNameText.addVerifyListener(e -> {
                e.text = e.text.replace(" ", "");
                e.text = e.text.toUpperCase();
            });

            TableEditor siteNameEditor = new TableEditor(table);
            siteNameEditor.grabHorizontal = true;
            siteNameEditor.setEditor(siteNameText, item, 0);

            editors.put(item, siteNameEditor);
        }
    }

    /**
     * Creates the top section of the GUI
     * 
     * @param parent
     */
    protected void createTableSection(Composite parent) {
        Composite topComposite = new Composite(parent, SWT.NONE);
        GridLayout topGridLayout = new GridLayout(1, false);
        topGridLayout.marginWidth = 15;

        topComposite.setLayout(topGridLayout);

        table = new Table(topComposite, SWT.BORDER | SWT.V_SCROLL);
        table.setLinesVisible(true);

        if (editors == null) {
            editors = new HashMap<>();
        }

        GridData gdTable = new GridData(SWT.CENTER, SWT.NONE, true, false, 3,
                1);
        gdTable.heightHint = 500;
        gdTable.widthHint = 240;

        table.setLayoutData(gdTable);

        table.setHeaderVisible(true);

        TableColumn sites = new TableColumn(table, SWT.NULL);
        sites.setText(satType.description);
        sites.setWidth(110);

    }

    /**
     * Loads the scatterometer data
     */
    protected void loadScatSat() {
        scatSat = AtcfConfigurationManager.getInstance().getScatSat();
    }

    /**
     * Loads the microwave data
     */
    protected void loadMicroSat() {
        microSat = AtcfConfigurationManager.getInstance().getMicroSat();
    }

    /**
     * Adds a new satellite to the list
     */
    protected void add() {
        TableItem item = new TableItem(table, SWT.NULL);
        item.setChecked(true);
        TableEditor siteNameEditor = new TableEditor(table);
        siteNameEditor.grabHorizontal = true;
        Text siteNameText = new Text(table, SWT.BORDER);
        siteNameText.setTextLimit(4);
        siteNameText.addVerifyListener(e -> {
            e.text = e.text.replace(" ", "");
            e.text = e.text.toUpperCase();
        });

        siteNameEditor.setEditor(siteNameText, item, 0);
        editors.put(item, siteNameEditor);
        siteNameText.setFocus();
        table.setTopIndex(table.getItemCount() - 1);
    }

    /**
     * Saves back to microsattypes.dat
     */
    private void save(SatelliteTypes type) {
        ArrayList<String> satList = new ArrayList<>();

        for (int i = 0; i < table.getItemCount(); i++) {
            TableEditor editor = editors.get(table.getItem(i));
            String site = "";
            if (editor != null) {
                Text txt = (Text) editor.getEditor();
                site = txt.getText();
            }

            if (site != null && !(site.isEmpty())) {
                satList.add(site);
            }
        }

        if (type.equals(SatelliteTypes.MICROWAVE)) {
            FixMicrowaveSatelliteTypes microsat = new FixMicrowaveSatelliteTypes(
                    satList);
            AtcfConfigurationManager.getInstance().saveMicroSats(microsat);
        }

        if (type.equals(SatelliteTypes.SCAT)) {
            FixScatSatTypes sctSat = new FixScatSatTypes(satList);
            AtcfConfigurationManager.getInstance().saveScatSats(sctSat);
        }
    }

    /**
     * Creates the add, save, and cancel buttons.
     * 
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        GridLayout okCancelGridLayout = new GridLayout(3, true);
        okCancelGridLayout.horizontalSpacing = 40;
        okCancelGridLayout.marginWidth = 25;
        Composite okCancelComposite = new Composite(parent, SWT.NONE);
        okCancelComposite.setLayout(okCancelGridLayout);

        Button addButton = new Button(okCancelComposite, SWT.CENTER);
        GridData addGridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        addButton.setText("Add");
        addButton.setLayoutData(addGridData);
        addButton.setToolTipText("Add a new " + satType.description);
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                add();
            }
        });

        Button saveButton = new Button(okCancelComposite, SWT.CENTER);
        GridData saveGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        saveButton.setToolTipText("Save the " + satType.description);
        saveButton.setText("Save");
        saveButton.setLayoutData(saveGridData);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                save(satType);
            }
        });

        Button cancelButton = new Button(okCancelComposite, SWT.CENTER);
        GridData cancelGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(cancelGridData);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

}