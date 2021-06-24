/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;

import gov.noaa.nws.ocp.common.dataplugin.psh.EffectDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshCounties;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshControlType;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Composite for the Storm Effects tab.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2017 #34810      wpaintsil   Initial creation.
 * JUN 09, 2021  DCS20652   wkwock      Update createControls for load user files
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshEffectsTabComp extends PshTabComp {

    public PshEffectsTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.EFFECT);
    }

    @Override
    public void createControls() {
        SashForm verticalSashForm = new SashForm(mainComposite, SWT.VERTICAL);

        Group dataComp = new Group(verticalSashForm, SWT.SHADOW_IN);
        dataComp.setText(tabType.getDesc());
        GridLayout dataLayout = new GridLayout(1, false);
        dataComp.setLayout(dataLayout);
        dataComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

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

        List<String> numberList = new ArrayList<>();
        for (int nn = 0; nn < 21; nn++) {
            numberList.add(String.valueOf(nn));
        }

        table = new PshEffectsTable(this,
                new PshTableColumn[] {
                        new PshTableColumn("County Selection", 200,
                                PshControlType.NONEMPTY_COMBO, countyList),
                        new PshTableColumn("Deaths", 80, PshControlType.COMBO,
                                numberList),
                        new PshTableColumn("Injuries", 80, PshControlType.COMBO,
                                numberList),
                        new PshTableColumn("Evacuations", 100,
                                PshControlType.COMBO, numberList) });

        table.createTopControls(dataComp);
        table.createTable(dataComp);
        table.createBottomControls(dataComp);

        GridData sashData = new GridData(SWT.CENTER, SWT.FILL, false, true);
        sashData.heightHint = 500;
        verticalSashForm.setLayoutData(sashData);

        createRemarksArea(verticalSashForm, true, false, false, "Remarks");

        setRemarksTextEditable(false);

    }

    @Override
    public void setDataList() {
        List<EffectDataEntry> effectDataList = pshGeneratorData.getPshData()
                .getEffect().getData();

        if (effectDataList == null || effectDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (EffectDataEntry dataItem : effectDataList) {
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
            List<EffectDataEntry> effectDataList = new ArrayList<>();

            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    effectDataList.add((EffectDataEntry) entry);
                }
            }

            PshData pshData = pshGeneratorData.getPshData();
            pshData.getEffect().setData(effectDataList);

            saveAlert(PshUtil.savePshData(pshData));

            pshGeneratorData.setPshData(pshData);
        }
    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getEffect().setData(table.getTableData(EffectDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.EFFECT));

    }

    @Override
    public void saveFinalRemarks() {
        // No Final Remarks for this tab; each entry has a remark.
    }

}
