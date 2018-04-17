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

import gov.noaa.nws.ocp.common.dataplugin.psh.NonMetarDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshStation;
import gov.noaa.nws.ocp.common.localization.psh.PshStations;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshControlType;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshNonMetarTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshSortableTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Composite for the Non-Metar Observations tab.
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
public class PshNonMetarTabComp extends PshTabComp {

    public PshNonMetarTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.NON_METAR);
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

        GridData sashData = new GridData(SWT.CENTER, SWT.FILL, false, true);
        sashData.heightHint = 500;
        verticalSashForm.setLayoutData(sashData);

        createRemarksArea(verticalSashForm, false, true, "Final Remarks");

        verticalSashForm.setWeights(new int[] { 60, 40 });

        List<String> comboList = new ArrayList<>();
        PshStations nonMetarStations = PshConfigurationManager.getInstance()
                .getNonMetarStations();

        if (nonMetarStations == null || nonMetarStations.getStations() == null
                || nonMetarStations.getStations().isEmpty()) {
            comboList.add("[empty]");
        } else {
            // Check if "mixed case" is required.
            boolean mixedCase = PshConfigurationManager.getInstance()
                    .getConfigHeader().isUseMixedCase();

            for (PshStation station : nonMetarStations.getStations()) {
                String name = PshUtil.buildStationFullName(station);
                if (!mixedCase) {
                    name = name.toUpperCase();
                }
                comboList.add(name);
            }
        }

        table = new PshNonMetarTable(this,
                new PshTableColumn[] {
                        new PshTableColumn(SITE_LBL_STRING, SITE_WIDTH,
                                PshControlType.NONEMPTY_COMBO, comboList),
                        new PshTableColumn(LAT_LBL_STRING, LAT_WIDTH,
                                PshControlType.NUMBER_TEXT),
                        new PshTableColumn(LON_LBL_STRING, LON_WIDTH,
                                PshControlType.NUMBER_TEXT),
                        new PshTableColumn(PRESSURE_LBL_STRING, PRES_WIDTH,
                                PshControlType.TEXT),
                        new PshTableColumn(DATE_LBL_STRING, DATETIME_WIDTH,
                                PshControlType.DATETIME_TEXT),
                        new PshTableColumn(INCOMPLETE, INCOMPLETE_WIDTH,
                                PshControlType.CHECKBOX),
                        new PshTableColumn(SUST_WIND_LBL_STRING,
                                SUST_WIND_WIDTH, PshControlType.WIND_TEXT),
                        new PshTableColumn(DATE_LBL_STRING, DATETIME_WIDTH,
                                PshControlType.DATETIME_TEXT),
                        new PshTableColumn(INCOMPLETE, INCOMPLETE_WIDTH,
                                PshControlType.CHECKBOX),
                        new PshTableColumn(PK_WIND_LBL_STRING, PK_WIND_WIDTH,
                                PshControlType.WIND_TEXT),
                        new PshTableColumn(DATE_LBL_STRING, DATETIME_WIDTH,
                                PshControlType.DATETIME_TEXT),
                        new PshTableColumn(INCOMPLETE, INCOMPLETE_WIDTH,
                                PshControlType.CHECKBOX),
                        new PshTableColumn(ESTIMATED, INCOMPLETE_WIDTH,
                                PshControlType.CHECKBOX),
                        new PshTableColumn(ANEMHGT_LBL_STRING, ANEM_WIDTH,
                                PshControlType.TEXT) });

        table.createTopControls(dataComp);
        table.createTable(dataComp);
        table.createBottomControls(dataComp);
        ((PshSortableTable) table).addColumnSelectionListeners();

    }

    @Override
    public void setDataList() {
        List<NonMetarDataEntry> nonMetarDataList = pshGeneratorData.getPshData()
                .getNonmetar().getData();

        if (nonMetarDataList == null || nonMetarDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (NonMetarDataEntry dataItem : nonMetarDataList) {
                addItem(dataItem);
            }
        }

        String remarks = pshGeneratorData.getPshData().getNonmetar()
                .getRemarks();
        if (!remarks.equals(StormData.NO_REMARKS)) {
            setRemarksText(remarks);
        }

        updatePreviewArea();
    }

    @Override
    public void savePshData(List<StormDataEntry> entries) {
        if ((entries == null || entries.isEmpty()) && emptyData) {
            // do nothing if the was no data and we're trying to save without
            // entering any new data
        } else {
            List<NonMetarDataEntry> nonMetarDataList = new ArrayList<>();

            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    nonMetarDataList.add((NonMetarDataEntry) entry);
                }
            }

            PshData pshData = pshGeneratorData.getPshData();
            pshData.getNonmetar().setData(nonMetarDataList);

            saveAlert(PshUtil.savePshData(pshData));

            pshGeneratorData.setPshData(pshData);
        }
    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getNonmetar()
                .setData(table.getTableData(NonMetarDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.NON_METAR));

    }

    @Override
    public void saveFinalRemarks() {
        List<NonMetarDataEntry> entries = pshGeneratorData.getPshData()
                .getNonmetar().getData();
        List<NonMetarDataEntry> dataList = new ArrayList<>(entries);

        PshData pshData = pshGeneratorData.getPshData();
        pshData.getNonmetar().setRemarks(getRemarksText());
        pshData.getNonmetar().setData(dataList);

        saveAlert(PshUtil.savePshData(pshData));

        pshGeneratorData.setPshData(pshData);
    }

}
