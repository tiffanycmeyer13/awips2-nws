/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
**/
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.FixSiteEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.FixSites;
import gov.noaa.nws.ocp.common.atcf.configuration.FixTypeEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.FixTypes;

/**
 * Dialog for editing ATCF configuration, such as Fix Site and Type Information.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 15, 2018 #56267      dmanzella   initial creation
 * Oct 22, 2018 #56277      dmanzella   added fix type support
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChangeConfigInfoDialog extends CaveSWTDialog {

    private FixSites fixSites;

    private FixTypes fixTypes;

    private Table table;

    /**
     * An enum that differentiates whether this dialog is showing fixType or
     * fixSite data
     */
    private ConfigInfoType infoType;

    /**
     * A Map containing editors for each TableItem in the table.
     */
    private Map<TableItem, TableEditor> editors = new LinkedHashMap<>();

    /**
     * Constructor
     * 
     * @param parent
     * @param fixMark
     */
    public ChangeConfigInfoDialog(Shell parent, ConfigInfoType infoConfigType) {
        super(parent);

        infoType = infoConfigType;

        setText("Change " + infoType.description + " Information");
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

        if (infoType == ConfigInfoType.SITE) {
            loadFixSites();
        } else if (infoType == ConfigInfoType.TYPE) {
            loadFixTypes();
        }

        createTableSection(mainComposite);
        createControlButtons(mainComposite);
        createInfoEntries();
    }

    /**
     * Puts the site entries into the table
     */
    protected void createInfoEntries() {
        // Load the site fix entries into the table
        int len = 0;
        if (infoType == ConfigInfoType.TYPE) {
            len = fixTypes.getFixTypes().size();
        } else if (infoType == ConfigInfoType.SITE) {
            len = fixSites.getFixSites().size();
        }

        for (int ii = 0; ii < len; ii++) {
            TableItem item = new TableItem(table, SWT.NULL);
            boolean isRetired = false;
            String name = "";
            if (infoType == ConfigInfoType.TYPE) {
                isRetired = fixTypes.getFixTypes().get(ii).isRetired();
                name = fixTypes.getFixTypes().get(ii).getName();
            } else if (infoType == ConfigInfoType.SITE) {
                isRetired = fixSites.getFixSites().get(ii).isRetired();
                name = fixSites.getFixSites().get(ii).getName();
            }

            item.setChecked(isRetired);

            Text siteNameText = new Text(table, SWT.BORDER);
            siteNameText.setTextLimit(4);
            siteNameText.setText(name);
            siteNameText.addVerifyListener(e -> {
                e.text = e.text.replace(" ", "");
                e.text = e.text.toUpperCase();
            });

            TableEditor siteNameEditor = new TableEditor(table);
            siteNameEditor.grabHorizontal = true;
            siteNameEditor.setEditor(siteNameText, item, 1);

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

        table = new Table(topComposite,
                SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        table.setLinesVisible(true);

        if (editors == null) {
            editors = new HashMap<>();
        }

        GridData gdTable = new GridData(SWT.CENTER, SWT.NONE, true, false, 3,
                1);
        gdTable.heightHint = 500;
        gdTable.widthHint = 250;

        table.setLayoutData(gdTable);

        table.setHeaderVisible(true);
        TableColumn available = new TableColumn(table, SWT.NULL);
        available.setText("Retired?");
        available.setWidth(110);

        TableColumn sites = new TableColumn(table, SWT.NULL);
        sites.setText(infoType.description);
        sites.setWidth(110);

    }

    /**
     * Loads the fixSite data
     */
    protected void loadFixSites() {
        fixSites = AtcfConfigurationManager.getInstance().getFixSites();
    }

    /**
     * Loads the fixType data
     */
    protected void loadFixTypes() {
        fixTypes = AtcfConfigurationManager.getInstance().getFixTypes();
    }

    /**
     * Adds a new site to the list
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

        siteNameEditor.setEditor(siteNameText, item, 1);
        editors.put(item, siteNameEditor);
        siteNameText.setFocus();
        table.setTopIndex(table.getItemCount() - 1);
    }

    /**
     * Saves back to fixsites.dat
     */
    protected void saveSite() {
        List<FixSiteEntry> siteList = new ArrayList<>();

        for (int i = 0; i < table.getItemCount(); i++) {
            TableEditor editor = editors.get(table.getItem(i));
            String site = "";
            if (editor != null) {
                Text txt = (Text) editor.getEditor();
                site = txt.getText();
            }

            boolean isRetired = table.getItem(i).getChecked();
            FixSiteEntry temp = new FixSiteEntry(site, isRetired);

            if (site != null && !(site.isEmpty())) {
                siteList.add(temp);
            }
        }

        FixSites fxts = new FixSites(siteList);
        AtcfConfigurationManager.getInstance().saveFixSites(fxts);
    }

    /**
     * Saves back to fixtypes.dat
     */
    protected void saveType() {
        List<FixTypeEntry> typeList = new ArrayList<>();

        for (int i = 0; i < table.getItemCount(); i++) {
            TableEditor editor = editors.get(table.getItem(i));
            String site = "";
            if (editor != null) {
                Text txt = (Text) editor.getEditor();
                site = txt.getText();
            }

            boolean isRetired = !(table.getItem(i).getChecked());
            FixTypeEntry temp = new FixTypeEntry(site, isRetired);

            if (site != null && !(site.isEmpty())) {
                typeList.add(temp);
            }
        }

        FixTypes fixTyp = new FixTypes(typeList);
        AtcfConfigurationManager.getInstance().saveFixTypes(fixTyp);
    }

    /**
     * Creates the add, save, and cancel buttons.
     * 
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        GridLayout okCancelGridLayout = new GridLayout(3, false);
        Composite okCancelComposite = new Composite(parent, SWT.NONE);
        okCancelComposite.setLayout(okCancelGridLayout);

        GridData okCancelGridData = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        okCancelGridData.widthHint = 60;
        okCancelGridData.horizontalIndent = 20;

        Button addButton = new Button(okCancelComposite, SWT.CENTER);
        addButton.setText("Add");
        addButton.setLayoutData(okCancelGridData);
        addButton
                .setToolTipText("Add a new " + infoType.description + " entry");
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                add();
            }
        });

        Button saveButton = new Button(okCancelComposite, SWT.CENTER);
        saveButton.setToolTipText(
                "Save the " + infoType.description + " entries");
        saveButton.setText("Save");
        saveButton.setLayoutData(okCancelGridData);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (infoType == ConfigInfoType.SITE) {
                    saveSite();
                } else if (infoType == ConfigInfoType.TYPE) {
                    saveType();
                }
            }
        });

        Button cancelButton = new Button(okCancelComposite, SWT.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(okCancelGridData);
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * enum representing what configuration info this dialog is displaying
     */
    public enum ConfigInfoType {
        SITE("Fix Site"), TYPE("Fix Type");

        private final String description;

        ConfigInfoType(final String text) {
            this.description = text;
        }
    }

}