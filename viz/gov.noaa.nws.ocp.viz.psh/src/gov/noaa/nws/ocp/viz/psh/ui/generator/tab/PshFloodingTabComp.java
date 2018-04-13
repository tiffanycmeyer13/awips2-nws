/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;

import gov.noaa.nws.ocp.common.dataplugin.psh.FloodingDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshCounties;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.PshLSRDialog;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshControlType;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshFloodingTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Composite for the Inland Flooding tab.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2017 #34810      wpaintsil   Initial creation.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshFloodingTabComp extends PshTabComp {

    public PshFloodingTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.FLOODING);
    }

    @Override
    public void createControls() {
        SashForm verticalSashForm = new SashForm(mainComposite, SWT.VERTICAL);

        SashForm horizontalSashForm = new SashForm(verticalSashForm,
                SWT.HORIZONTAL);

        Group dataComp = new Group(horizontalSashForm, SWT.SHADOW_IN);
        dataComp.setText(tabType.getDesc());
        GridLayout dataLayout = new GridLayout(1, false);
        dataComp.setLayout(dataLayout);
        dataComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        List<String> countyList = new ArrayList<>();
        PshCounties counties = PshConfigurationManager.getInstance()
                .getCounties();

        if (counties == null || counties.getCounties() == null
                || counties.getCounties().isEmpty()) {
            countyList.add("[empty]");
        } else {
            for (String countyString : counties.getCounties()) {
                countyList.add(countyString);
            }
        }

        table = new PshFloodingTable(this,
                new PshTableColumn[] { new PshTableColumn("County", 360,
                        PshControlType.NONEMPTY_COMBO, countyList) });

        table.createTopControls(dataComp);
        table.createTable(dataComp);
        table.createBottomControls(dataComp);

        GridData sashData = new GridData(SWT.CENTER, SWT.FILL, false, true);
        sashData.heightHint = 500;
        sashData.widthHint = 1000;
        verticalSashForm.setLayoutData(sashData);

        // Remarks area
        // Remarks text field
        createRemarksText(horizontalSashForm, "Flooding Input", false);
        setRemarksTextEditable(false);

        // Preview area
        Composite previewComp = new Composite(verticalSashForm, SWT.NONE);
        GridLayout previewLayout = new GridLayout(2, true);
        previewComp.setLayout(previewLayout);

        GridData previewData = new GridData(SWT.CENTER, SWT.FILL, true, true);
        previewData.horizontalSpan = 2;
        createPreviewArea(previewComp, previewData);

        verticalSashForm.setWeights(new int[] { 40, 60 });
        horizontalSashForm.setWeights(new int[] { 60, 40 });

        // Load external button
        Composite loadExternalComp = new Composite(previewComp, SWT.NONE);
        GridLayout loadExternalLayout = new GridLayout(2, true);
        loadExternalLayout.marginWidth = 0;
        loadExternalComp.setLayout(loadExternalLayout);
        loadExternalComp
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));

        Button loadLSRButton = new Button(loadExternalComp, SWT.PUSH);

        loadLSRButton.setText("Load LSR\nFiles");
        loadLSRButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                new PshLSRDialog(getShell(), PshFloodingTabComp.this).open();
            }
        });

    }

    @Override
    public void setDataList() {
        List<FloodingDataEntry> floodDataList = pshGeneratorData.getPshData()
                .getFlooding().getData();

        if (floodDataList == null || floodDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (FloodingDataEntry dataItem : floodDataList) {
                addItem(dataItem);
            }
        }
        updatePreviewArea();
    }

    @Override
    public void savePshData(List<StormDataEntry> entries) {
        if ((entries == null || entries.isEmpty()) && emptyData) {
            // do nothing if the was no data and we're trying to save without
            // entering any new data
        } else {
            List<FloodingDataEntry> floodDataList = new ArrayList<>();

            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    floodDataList.add((FloodingDataEntry) entry);
                }
            }
            PshData pshData = pshGeneratorData.getPshData();
            pshData.getFlooding().setData(floodDataList);

            saveAlert(PshUtil.savePshData(pshData));

            pshGeneratorData.setPshData(pshData);
        }
    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getFlooding()
                .setData(table.getTableData(FloodingDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.FLOODING));

    }

    @Override
    public void saveFinalRemarks() {
        // No Final Remarks for this tab; each entry has a remark.
    }

}
