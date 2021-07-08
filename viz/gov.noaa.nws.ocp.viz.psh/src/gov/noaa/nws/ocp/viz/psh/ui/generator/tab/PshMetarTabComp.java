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

import gov.noaa.nws.ocp.common.dataplugin.psh.MetarDataEntry;
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
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshMetarTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Composite for the Metar Observations tab.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2017 #34810      wpaintsil   Initial creation.
 * May 24, 2021 20652       wkwock      Update createControls() for no user load files
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshMetarTabComp extends PshTabComp {

    public PshMetarTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.METAR);
    }

    @Override
    public void createControls() {
        SashForm verticalSashForm = new SashForm(mainComposite, SWT.VERTICAL);

        Group dataComp = new Group(verticalSashForm, SWT.SHADOW_IN);
        dataComp.setText(tabType.getDesc());
        GridLayout dataLayout = new GridLayout(1, false);
        dataLayout.marginWidth = 50;
        dataComp.setLayout(dataLayout);
        dataComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        GridData sashData = new GridData(SWT.CENTER, SWT.FILL, false, true);
        sashData.heightHint = 500;
        verticalSashForm.setLayoutData(sashData);

        createRemarksArea(verticalSashForm, false, false, true,
                "Final Remarks");

        verticalSashForm.setWeights(new int[] { 60, 40 });

        List<String> comboList = new ArrayList<>();
        PshStations metarStations = PshConfigurationManager.getInstance()
                .getMetarStations();

        if (metarStations == null || metarStations.getStations() == null
                || metarStations.getStations().isEmpty()) {
            comboList.add("[empty]");
        } else {
            // Check if "mixed case" is required.
            boolean mixedCase = PshConfigurationManager.getInstance()
                    .getConfigHeader().isUseMixedCase();

            for (PshStation station : metarStations.getStations()) {
                String name = PshUtil.buildStationFullName(station);
                if (!mixedCase) {
                    name = name.toUpperCase();
                }
                comboList.add(name);
            }
        }

        table = new PshMetarTable(this,
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
                                PshControlType.CHECKBOX) });

        table.createTopControls(dataComp, true);
        table.createTable(dataComp);
        table.createBottomControls(dataComp);
        ((PshMetarTable) table).addColumnSelectionListeners();

    }

    @Override
    public void setDataList() {
        List<MetarDataEntry> metarDataList = pshGeneratorData.getPshData()
                .getMetar().getData();

        if (metarDataList == null || metarDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (MetarDataEntry dataItem : metarDataList) {
                addItem(dataItem);
            }
        }

        String remarks = pshGeneratorData.getPshData().getMetar().getRemarks();
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
            List<MetarDataEntry> metarDataList = new ArrayList<>();

            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    metarDataList.add((MetarDataEntry) entry);
                }
            }
            PshData pshData = pshGeneratorData.getPshData();
            pshData.getMetar().setRemarks(getRemarksText());
            pshData.getMetar().setData(metarDataList);

            saveAlert(PshUtil.savePshData(pshData));

            pshGeneratorData.setPshData(pshData);
        }
    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getMetar().setData(table.getTableData(MetarDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.METAR));

    }

    @Override
    public void saveFinalRemarks() {
        List<MetarDataEntry> entries = pshGeneratorData.getPshData().getMetar()
                .getData();
        List<MetarDataEntry> dataList = new ArrayList<>(entries);

        PshData pshData = pshGeneratorData.getPshData();
        pshData.getMetar().setRemarks(getRemarksText());
        pshData.getMetar().setData(dataList);

        saveAlert(PshUtil.savePshData(pshData));

        pshGeneratorData.setPshData(pshData);
    }

}
