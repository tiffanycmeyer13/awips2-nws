/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.TornadoDataEntry;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshControlType;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTornadoTable;

/**
 * Composite for the Tornadoes tab.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2017 #34810      wpaintsil   Initial creation.
 * Aug 22, 2017 #36922      astrakovsky Added autocomplete fields for rainfall and tornadoes.
 * Sep 08, 2017 #36923      astrakovsky Added direction control type.
 * Jun 10, 2021  20652      wkwock      Update createRemarksArea for load user file
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshTornadoesTabComp extends PshTabComp {

    public PshTornadoesTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.TORNADO);
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

        List<String> magnitudeList = Arrays.asList(new String[] { "EF0", "EF1",
                "EF2", "EF3", "EF4", "EF5", "N/A" });

        table = new PshTornadoTable(this,
                new PshTableColumn[] {
                        new PshTableColumn("City/Town", CITY_WIDTH,
                                PshControlType.NONEMPTY_TEXT),
                        new PshTableColumn(COUNTY_LBL_STRING, COUNTY_WIDTH,
                                PshControlType.TEXT),
                        new PshTableColumn("Magnitude", 80,
                                PshControlType.NONEMPTY_COMBO, magnitudeList),
                        new PshTableColumn(DATE_LBL_STRING + "\n(UTC)", 100,
                                PshControlType.DATETIME_TEXT),
                        new PshTableColumn("Lat", LAT_WIDTH,
                                PshControlType.NUMBER_TEXT),
                        new PshTableColumn("Lon", LON_WIDTH,
                                PshControlType.NUMBER_TEXT),
                        new PshTableColumn("Dir from\nCity", DIR_DIST_WIDTH,
                                PshControlType.COMBO, directionList),
                        new PshTableColumn("Dist from\nCity (MI)",
                                DIR_DIST_WIDTH, PshControlType.NUMBER_TEXT),
                        new PshTableColumn(INCOMPLETE, INCOMPLETE_WIDTH,
                                PshControlType.CHECKBOX) });

        table.createTopControls(dataComp, false);
        table.createTable(dataComp);
        table.createBottomControls(dataComp);
        ((PshTornadoTable) table).addColumnSelectionListeners();

        GridData sashData = new GridData(SWT.CENTER, SWT.FILL, false, true);
        sashData.heightHint = 500;
        verticalSashForm.setLayoutData(sashData);

        createRemarksArea(verticalSashForm, true, false, false,
                "Tornado Remarks");

        setRemarksTextEditable(false);

    }

    @Override
    public void setDataList() {
        List<TornadoDataEntry> tornadoDataList = pshGeneratorData.getPshData()
                .getTornado().getData();
        if (tornadoDataList == null || tornadoDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (TornadoDataEntry dataItem : tornadoDataList) {
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
            List<TornadoDataEntry> tornadoDataList = new ArrayList<>();
            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    tornadoDataList.add((TornadoDataEntry) entry);
                }
            }
            PshData pshData = pshGeneratorData.getPshData();
            pshData.getTornado().setData(tornadoDataList);

            saveAlert(PshUtil.savePshData(pshData));

            pshGeneratorData.setPshData(pshData);
        }
    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getTornado()
                .setData(table.getTableData(TornadoDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.TORNADO));

    }

    @Override
    public void saveFinalRemarks() {
        // No Final Remarks for this tab; each entry has a remark.
    }

}