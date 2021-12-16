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

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.FixError;
import gov.noaa.nws.ocp.common.atcf.configuration.FixErrorEntry;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for editing ATCF configuration, such as Fix Error Information.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 04, 2019 64494       dmanzella  initial creation
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class FixErrorDialog extends OcpCaveSWTDialog {

    private FixError fixError;

    private Table table;

    private static final Integer EMPTY = 999;

    /**
     * A Map containing editors for each TableItem in the table.
     */
    private Map<TableItem, ArrayList<TableEditor>> editors = new LinkedHashMap<>();

    /**
     * Constructor
     *
     * @param parent
     * @param fixMark
     */
    public FixErrorDialog(Shell parent) {
        super(parent);

        setText("Fix Error Preferences");
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

        fixError = AtcfConfigurationManager.getInstance().getFixError();

        createTableSection(mainComposite);
        createControlButtons(mainComposite);
        createInfoEntries();
    }

    /**
     * Puts the site entries into the table
     */
    protected void createInfoEntries() {
        // Load the site fix error into the table
        int len = fixError.getFixError().size();

        for (int ii = 0; ii < len; ii++) {
            String type = fixError.getFixError().get(ii).getType();
            Text typeNameText = new Text(table, SWT.BORDER);
            typeNameText.setTextLimit(4);
            typeNameText.setText(type);
            typeNameText.addVerifyListener(e -> {
                e.text = e.text.replace(" ", "");
                e.text = e.text.toUpperCase();
            });

            String site = fixError.getFixError().get(ii).getSite();
            Text siteNameText = new Text(table, SWT.BORDER);
            siteNameText.setTextLimit(4);
            siteNameText.setText(site);
            siteNameText.addVerifyListener(e -> {
                e.text = e.text.replace(" ", "");
                e.text = e.text.toUpperCase();
            });

            String pos1 = fixError.getFixError().get(ii).getPositions().get(0)
                    .toString();
            Text pos1Text = new Text(table, SWT.BORDER);
            pos1Text.setText(pos1);

            String pos2 = fixError.getFixError().get(ii).getPositions().get(1)
                    .toString();
            Text pos2Text = new Text(table, SWT.BORDER);
            pos2Text.setText(pos2);

            String pos3 = fixError.getFixError().get(ii).getPositions().get(2)
                    .toString();
            Text pos3Text = new Text(table, SWT.BORDER);
            pos3Text.setText(pos3);

            String intens1 = fixError.getFixError().get(ii).getIntensities()
                    .get(0).toString();
            Text intens1Text = new Text(table, SWT.BORDER);
            intens1Text.setText(intens1);

            String intens2 = fixError.getFixError().get(ii).getIntensities()
                    .get(1).toString();
            Text intens2Text = new Text(table, SWT.BORDER);
            intens2Text.setText(intens2);

            String intens3 = fixError.getFixError().get(ii).getIntensities()
                    .get(2).toString();
            Text intens3Text = new Text(table, SWT.BORDER);
            intens3Text.setText(intens3);

            String radii1 = fixError.getFixError().get(ii).getRadii().get(0)
                    .toString();
            Text radii1Text = new Text(table, SWT.BORDER);
            radii1Text.setText(radii1);

            String radii2 = fixError.getFixError().get(ii).getRadii().get(1)
                    .toString();
            Text radii2Text = new Text(table, SWT.BORDER);
            radii2Text.setText(radii2);

            String radii3 = fixError.getFixError().get(ii).getRadii().get(2)
                    .toString();
            Text radii3Text = new Text(table, SWT.BORDER);
            radii3Text.setText(radii3);

            boolean isResearch = fixError.getFixError().get(ii).isResearch();
            Text research = new Text(table, SWT.BORDER);
            research.setText(isResearch ? "1" : "0");

            TableItem item = new TableItem(table, SWT.NULL);

            TableEditor typeNameEditor = new TableEditor(table);
            typeNameEditor.grabHorizontal = true;
            typeNameEditor.setEditor(typeNameText, item, 0);

            TableEditor siteNameEditor = new TableEditor(table);
            siteNameEditor.grabHorizontal = true;
            siteNameEditor.setEditor(siteNameText, item, 1);

            TableEditor pos1Editor = new TableEditor(table);
            pos1Editor.grabHorizontal = true;
            pos1Editor.setEditor(pos1Text, item, 2);

            TableEditor pos2Editor = new TableEditor(table);
            pos2Editor.grabHorizontal = true;
            pos2Editor.setEditor(pos2Text, item, 3);

            TableEditor pos3Editor = new TableEditor(table);
            pos3Editor.grabHorizontal = true;
            pos3Editor.setEditor(pos3Text, item, 4);

            TableEditor intens1Editor = new TableEditor(table);
            intens1Editor.grabHorizontal = true;
            intens1Editor.setEditor(intens1Text, item, 5);

            TableEditor intens2Editor = new TableEditor(table);
            intens2Editor.grabHorizontal = true;
            intens2Editor.setEditor(intens2Text, item, 6);

            TableEditor intens3Editor = new TableEditor(table);
            intens3Editor.grabHorizontal = true;
            intens3Editor.setEditor(intens3Text, item, 7);

            TableEditor radii1Editor = new TableEditor(table);
            radii1Editor.grabHorizontal = true;
            radii1Editor.setEditor(radii1Text, item, 8);

            TableEditor radii2Editor = new TableEditor(table);
            radii2Editor.grabHorizontal = true;
            radii2Editor.setEditor(radii2Text, item, 9);

            TableEditor radii3Editor = new TableEditor(table);
            radii3Editor.grabHorizontal = true;
            radii3Editor.setEditor(radii3Text, item, 10);

            TableEditor researchEditor = new TableEditor(table);
            researchEditor.grabHorizontal = true;
            researchEditor.setEditor(research, item, 11);

            ArrayList<TableEditor> editorList = new ArrayList<>();
            editorList.add(siteNameEditor);
            editorList.add(typeNameEditor);
            editorList.add(pos1Editor);
            editorList.add(pos2Editor);
            editorList.add(pos3Editor);
            editorList.add(intens1Editor);
            editorList.add(intens2Editor);
            editorList.add(intens3Editor);
            editorList.add(radii1Editor);
            editorList.add(radii2Editor);
            editorList.add(radii3Editor);
            editorList.add(researchEditor);

            editors.put(item, editorList);
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

        table = new Table(topComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        table.setLinesVisible(true);

        if (editors == null) {
            editors = new HashMap<>();
        }

        GridData gdTable = new GridData(SWT.CENTER, SWT.NONE, true, false, 3,
                1);
        gdTable.heightHint = 557;
        gdTable.widthHint = 1300;

        table.setLayoutData(gdTable);

        table.setHeaderVisible(true);
        TableColumn available = new TableColumn(table, SWT.NULL);
        available.setText("Type");
        available.setWidth(60);

        TableColumn sites = new TableColumn(table, SWT.NULL);
        sites.setText("Site");
        sites.setWidth(60);

        TableColumn position1 = new TableColumn(table, SWT.NULL);
        position1.setText("Position Weight 1");
        position1.setWidth(125);

        TableColumn position2 = new TableColumn(table, SWT.NULL);
        position2.setText("Position Weight 2");
        position2.setWidth(125);

        TableColumn position3 = new TableColumn(table, SWT.NULL);
        position3.setText("Position Weight 3");
        position3.setWidth(125);

        TableColumn intensity1 = new TableColumn(table, SWT.NULL);
        intensity1.setText("Intensity Weight 1");
        intensity1.setWidth(130);

        TableColumn intensity2 = new TableColumn(table, SWT.NULL);
        intensity2.setText("Intensity Weight 2");
        intensity2.setWidth(130);

        TableColumn intensity3 = new TableColumn(table, SWT.NULL);
        intensity3.setText("Intensity Weight 3");
        intensity3.setWidth(130);

        TableColumn radii1 = new TableColumn(table, SWT.NULL);
        radii1.setText("Radii Weight 1");
        radii1.setWidth(105);

        TableColumn radii2 = new TableColumn(table, SWT.NULL);
        radii2.setText("Radii Weight 2");
        radii2.setWidth(105);

        TableColumn radii3 = new TableColumn(table, SWT.NULL);
        radii3.setText("Radii Weight 3");
        radii3.setWidth(105);

        TableColumn research = new TableColumn(table, SWT.NULL);
        research.setText("Research Fix");
        research.setWidth(100);

    }

    /**
     * Adds a new site to the list
     */
    protected void add() {

        TableItem item = new TableItem(table, SWT.NULL);

        TableEditor typeEditor = new TableEditor(table);
        typeEditor.grabHorizontal = true;
        Text typeText = new Text(table, SWT.BORDER);
        typeText.setTextLimit(4);
        typeText.addVerifyListener(e -> {
            e.text = e.text.replace(" ", "");
            e.text = e.text.toUpperCase();
        });
        typeEditor.setEditor(typeText, item, 0);

        TableEditor siteEditor = new TableEditor(table);
        siteEditor.grabHorizontal = true;
        Text siteText = new Text(table, SWT.BORDER);
        siteText.setTextLimit(4);
        siteText.addVerifyListener(e -> {
            e.text = e.text.replace(" ", "");
            e.text = e.text.toUpperCase();
        });
        siteEditor.setEditor(siteText, item, 1);

        Text pos1Text = new Text(table, SWT.BORDER);
        TableEditor pos1Editor = new TableEditor(table);
        pos1Editor.grabHorizontal = true;
        pos1Editor.setEditor(pos1Text, item, 2);

        Text pos2Text = new Text(table, SWT.BORDER);
        TableEditor pos2Editor = new TableEditor(table);
        pos2Editor.grabHorizontal = true;
        pos2Editor.setEditor(pos2Text, item, 3);

        Text pos3Text = new Text(table, SWT.BORDER);
        TableEditor pos3Editor = new TableEditor(table);
        pos3Editor.grabHorizontal = true;
        pos3Editor.setEditor(pos3Text, item, 4);

        Text intens1Text = new Text(table, SWT.BORDER);
        TableEditor intens1Editor = new TableEditor(table);
        intens1Editor.grabHorizontal = true;
        intens1Editor.setEditor(intens1Text, item, 5);

        Text intens2Text = new Text(table, SWT.BORDER);
        TableEditor intens2Editor = new TableEditor(table);
        intens2Editor.grabHorizontal = true;
        intens2Editor.setEditor(intens2Text, item, 6);

        Text intens3Text = new Text(table, SWT.BORDER);
        TableEditor intens3Editor = new TableEditor(table);
        intens3Editor.grabHorizontal = true;
        intens3Editor.setEditor(intens3Text, item, 7);

        Text radii1Text = new Text(table, SWT.BORDER);
        TableEditor radii1Editor = new TableEditor(table);
        radii1Editor.grabHorizontal = true;
        radii1Editor.setEditor(radii1Text, item, 8);

        Text radii2Text = new Text(table, SWT.BORDER);
        TableEditor radii2Editor = new TableEditor(table);
        radii2Editor.grabHorizontal = true;
        radii2Editor.setEditor(radii2Text, item, 9);

        Text radii3Text = new Text(table, SWT.BORDER);
        TableEditor radii3Editor = new TableEditor(table);
        radii3Editor.grabHorizontal = true;
        radii3Editor.setEditor(radii3Text, item, 10);

        Text research = new Text(table, SWT.BORDER);
        TableEditor researchEditor = new TableEditor(table);
        researchEditor.grabHorizontal = true;
        researchEditor.setEditor(research, item, 11);

        ArrayList<TableEditor> editorList = new ArrayList<>();
        editorList.add(typeEditor);
        editorList.add(siteEditor);
        editorList.add(pos1Editor);
        editorList.add(pos2Editor);
        editorList.add(pos3Editor);
        editorList.add(intens1Editor);
        editorList.add(intens2Editor);
        editorList.add(intens3Editor);
        editorList.add(radii1Editor);
        editorList.add(radii2Editor);
        editorList.add(radii3Editor);
        editorList.add(researchEditor);

        editors.put(item, editorList);

        typeText.setFocus();
        table.setTopIndex(table.getItemCount() - 1);
    }

    /**
     * Saves back to fixerror.prefs
     */
    protected void saveError() {
        ArrayList<FixErrorEntry> errorList = new ArrayList<>();

        for (int i = 0; i < table.getItemCount(); i++) {
            TableEditor typeEditor = editors.get(table.getItem(i)).get(0);
            TableEditor siteEditor = editors.get(table.getItem(i)).get(1);
            TableEditor pos1Editor = editors.get(table.getItem(i)).get(2);
            TableEditor pos2Editor = editors.get(table.getItem(i)).get(3);
            TableEditor pos3Editor = editors.get(table.getItem(i)).get(4);
            TableEditor inten1Editor = editors.get(table.getItem(i)).get(5);
            TableEditor inten2Editor = editors.get(table.getItem(i)).get(6);
            TableEditor inten3Editor = editors.get(table.getItem(i)).get(7);
            TableEditor radii1Editor = editors.get(table.getItem(i)).get(8);
            TableEditor radii2Editor = editors.get(table.getItem(i)).get(9);
            TableEditor radii3Editor = editors.get(table.getItem(i)).get(10);
            TableEditor researchEditor = editors.get(table.getItem(i)).get(11);

            String site = "";
            String type = "";
            ArrayList<Integer> weights = new ArrayList<>();
            int pos1 = 0;
            int pos2 = 0;
            int pos3 = 0;
            int inten1 = 0;
            int inten2 = 0;
            int inten3 = 0;
            int radii1 = 0;
            int radii2 = 0;
            int radii3 = 0;
            weights.add(pos1);
            weights.add(pos2);
            weights.add(pos3);
            weights.add(inten1);
            weights.add(inten2);
            weights.add(inten3);
            weights.add(radii1);
            weights.add(radii2);
            weights.add(radii3);

            boolean isResearch = false;
            if (typeEditor != null) {

                Text typeTxt = (Text) typeEditor.getEditor();
                type = typeTxt.getText();

                Text siteTxt = (Text) siteEditor.getEditor();
                site = siteTxt.getText();

                ArrayList<Text> texts = new ArrayList<>();

                Text pos1Txt = (Text) pos1Editor.getEditor();
                texts.add(pos1Txt);

                Text pos2Txt = (Text) pos2Editor.getEditor();
                texts.add(pos2Txt);

                Text pos3Txt = (Text) pos3Editor.getEditor();
                texts.add(pos3Txt);

                Text ints1Txt = (Text) inten1Editor.getEditor();
                texts.add(ints1Txt);

                Text ints2Txt = (Text) inten2Editor.getEditor();
                texts.add(ints2Txt);

                Text ints3Txt = (Text) inten3Editor.getEditor();
                texts.add(ints3Txt);

                Text rad1Txt = (Text) radii1Editor.getEditor();
                texts.add(rad1Txt);

                Text rad2Txt = (Text) radii2Editor.getEditor();
                texts.add(rad2Txt);

                Text rad3Txt = (Text) radii3Editor.getEditor();
                texts.add(rad3Txt);

                for (int ii = 0; ii < texts.size(); ii++) {
                    if (texts.get(ii).getText().isEmpty()) {
                        weights.set(ii, EMPTY);
                    } else {
                        try {
                            weights.set(ii,
                                    Integer.parseInt(texts.get(ii).getText()));
                        } catch (NumberFormatException e) {
                            weights.set(ii, EMPTY);
                        }
                    }
                }

                Text rTxt = (Text) researchEditor.getEditor();
                isResearch = "1".equals(rTxt.getText());

            }

            List<Integer> positions = new ArrayList<>();
            positions.add(weights.get(0));
            positions.add(weights.get(1));
            positions.add(weights.get(2));

            List<Integer> intensities = new ArrayList<>();
            intensities.add(weights.get(3));
            intensities.add(weights.get(4));
            intensities.add(weights.get(5));

            List<Integer> radii = new ArrayList<>();
            radii.add(weights.get(6));
            radii.add(weights.get(7));
            radii.add(weights.get(8));

            FixErrorEntry temp = new FixErrorEntry(site, type, positions,
                    intensities, radii, isResearch);

            if (site != null && type != null) {
                errorList.add(temp);
            }
        }

        FixError fixErr = new FixError(errorList);
        AtcfConfigurationManager.getInstance().saveFixError(fixErr);
    }

    /**
     * Creates the add, save, and cancel buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        GridLayout okCancelGridLayout = new GridLayout(3, true);
        okCancelGridLayout.horizontalSpacing = 275;
        okCancelGridLayout.marginWidth = 100;
        Composite okCancelComposite = new Composite(parent, SWT.NONE);
        okCancelComposite.setLayout(okCancelGridLayout);

        Button addButton = new Button(okCancelComposite, SWT.NONE);
        GridData addGridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        addGridData.minimumWidth = 200;
        addButton.setLayoutData(addGridData);
        addButton.setText("Add");
        addButton.setToolTipText("Add a new entry");
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                add();
            }
        });

        Button saveButton = new Button(okCancelComposite, SWT.NONE);
        GridData saveGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        saveButton.setLayoutData(saveGridData);
        saveButton.setToolTipText("Save the entries");
        saveButton.setText("Save");
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveError();
            }
        });

        Button cancelButton = new Button(okCancelComposite, SWT.NONE);
        GridData cancelGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        cancelButton.setLayoutData(cancelGridData);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

}