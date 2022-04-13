/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;

import gov.noaa.nws.ocp.common.dataplugin.psh.MarineDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshStation;
import gov.noaa.nws.ocp.common.localization.psh.PshStations;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.PshUserFileDialog;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshControlType;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshMarineTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Composite for the Marine Observations tab.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2017 #34810      wpaintsil   Initial creation.
 * May 24, 2021 20652       wkwock      Add load user files
 * Jun 18, 2021 DCS22100    mporricelli Add checks to alert user that their
 *                                      changes have not been saved
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshMarineTabComp extends PshTabComp {

    public PshMarineTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.MARINE);
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

        createRemarksArea(verticalSashForm, false, true, true, "Final Remarks");

        verticalSashForm.setWeights(new int[] { 60, 40 });

        List<String> comboList = new ArrayList<>();
        PshStations marineStations = PshConfigurationManager.getInstance()
                .getMarineStations();

        if (marineStations == null || marineStations.getStations() == null
                || marineStations.getStations().isEmpty()) {
            comboList.add("[empty]");
        } else {
            // Check if "mixed case" is required.
            boolean mixedCase = PshConfigurationManager.getInstance()
                    .getConfigHeader().isUseMixedCase();

            for (PshStation station : marineStations.getStations()) {
                String name = PshUtil.buildStationFullName(station);
                if (!mixedCase) {
                    name = name.toUpperCase();
                }
                comboList.add(name);
            }
        }

        table = new PshMarineTable(this,
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
                        new PshTableColumn(ANEMHGT_LBL_STRING, ANEM_WIDTH,
                                PshControlType.TEXT) });

        table.createTopControls(dataComp);
        table.createTable(dataComp);
        table.createBottomControls(dataComp);
        ((PshMarineTable) table).addColumnSelectionListeners();

    }

    @Override
    public void setDataList() {
        List<MarineDataEntry> marineDataList = pshGeneratorData.getPshData()
                .getMarine().getData();
        if (marineDataList == null || marineDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (MarineDataEntry dataItem : marineDataList) {
                addItem(dataItem);
            }
        }
        String remarks = pshGeneratorData.getPshData().getMarine().getRemarks();
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
            List<MarineDataEntry> marineDataList = new ArrayList<>();

            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    marineDataList.add((MarineDataEntry) entry);
                }
            }
            PshData pshData = pshGeneratorData.getPshData();
            pshData.getMarine().setRemarks(getRemarksText());
            pshData.getMarine().setData(marineDataList);

            saveAlert(PshUtil.savePshData(pshData));
            table.setUnsavedChanges(false);
            pshGeneratorData.setPshData(pshData);
        }
    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getMarine().setData(table.getTableData(MarineDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.MARINE));

    }

    @Override
    public void saveFinalRemarks() {
        List<MarineDataEntry> entries = pshGeneratorData.getPshData()
                .getMarine().getData();
        List<MarineDataEntry> dataList = new ArrayList<>(entries);

        PshData pshData = pshGeneratorData.getPshData();
        pshData.getMarine().setRemarks(getRemarksText());
        pshData.getMarine().setData(dataList);

        saveAlert(PshUtil.savePshData(pshData));

        pshGeneratorData.setPshData(pshData);
    }

    @Override
    protected void loadUserFile() {
        // create and open user file dialog
        PshUserFileDialog userFileLoader = new PshUserFileDialog(getShell(),
                "Example User File",
                "The data fields must follow the example shown below"
                        + "\n\nSite,Latitude,Longitude,Lowest Pressure,"
                        + "Date/Time,incomplete,Sust Wind,Date/Time,Incomplete,"
                        + "Peak Wind,Wind Date Time,Incomplete,Anemhght"
                        + "\n\nWhere:\n*Date/Time is in dd/HHmm format"
                        + "\n\n-------------------------------------------------------------------------------------------------"
                        + "\nAn Example file with two marine observations will look as follows:"
                        + "\n\nCDRF1-Cedar Key, FL/V,28.08,-82.76,1,123,22/1234,0,"
                        + "\n50,22/1234,1,100,22/0212,1,213");
        userFileLoader.open();

        List<String[]> fieldsList = userFileLoader.getFieldsList(13);
        if (!fieldsList.isEmpty()) {
            for (String[] fields : fieldsList) {
                createMarineTableItem(fields);
            }

            updatePreviewArea();
        }
    }

    private void createMarineTableItem(String[] fields) {
        MarineDataEntry marineData = new MarineDataEntry();

        marineData.setSite(fields[0]);
        if (NumberUtils.isNumber(fields[1])) {
            marineData.setLat(PshUtil.parseFloat(fields[1]));
        }
        if (NumberUtils.isNumber(fields[2])) {
            marineData.setLon(PshUtil.parseFloat(fields[2]));
        }

        marineData.setMinSeaLevelPres(fields[3]);
        marineData.setMinSeaLevelPresTime(fields[4]);
        if (fields[5].equalsIgnoreCase("I")) {
            marineData.setMinSeaLevelComplete(fields[5].toUpperCase());
        } else {
            marineData.setMinSeaLevelComplete("");
        }

        marineData.setSustWind(fields[6]);
        marineData.setSustWindTime(fields[7]);
        if (fields[8].equalsIgnoreCase("I")) {
            marineData.setSustWindComplete(fields[8].toUpperCase());
        } else {
            marineData.setSustWindComplete("");
        }

        marineData.setPeakWind(fields[9]);
        marineData.setPeakWindTime(fields[10]);
        if (fields[11].equalsIgnoreCase("I")) {
            marineData.setPeakWindComplete(fields[11].toUpperCase());
        } else {
            marineData.setPeakWindComplete("");
        }

        marineData.setAnemHgmt(fields[12]);

        table.addItem(marineData);
    }
}
