/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallStormData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.IPshData;
import gov.noaa.nws.ocp.viz.psh.ui.generator.PshUserFileDialog;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshControlType;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshRainfallTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Composite for the Storm Rainfall tab.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2017 #34810      wpaintsil   Initial creation.
 * Aug 22, 2017 #36922      astrakovsky Added autocomplete fields for rainfall and tornadoes.
 * Sep 08, 2017 #36923      astrakovsky Added direction control type.
 * Sep 25, 2017 #36924      astrakovsky Moved user file dialog into its own file.
 * Sep 26, 2017 #38085      wpaintsil   Implement rainfall start/end date selection.
 * Nov,08  2017 #40423      jwu         Use PshCity for location.
 * May 24, 2021 20652       wkwock      Re-factor load user files
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshRainfallTabComp extends PshTabComp {

    private CCombo startDayCombo;

    private CCombo startHourCombo;

    private CCombo endMonCombo;

    private CCombo endDayCombo;

    private CCombo endHourCombo;

    private CCombo startMonCombo;

    public static final String RAINFALL_LBL = "Rainfall";

    public PshRainfallTabComp(IPshData psh, TabFolder parent) {
        super(psh, parent, PshDataCategory.RAINFALL);
    }

    @Override
    public void createControls() {
        createRainFields();

        SashForm verticalSashForm = new SashForm(mainComposite, SWT.VERTICAL);

        Group dataComp = new Group(verticalSashForm, SWT.SHADOW_IN);
        dataComp.setText(tabType.getDesc());
        GridLayout dataLayout = new GridLayout(1, false);
        dataComp.setLayout(dataLayout);
        dataComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        table = new PshRainfallTable(this,
                new PshTableColumn[] {
                        new PshTableColumn("City/Town", CITY_WIDTH,
                                PshControlType.NONEMPTY_TEXT),
                        new PshTableColumn("ID:", 80, PshControlType.TEXT),
                        new PshTableColumn("Lat", LAT_WIDTH,
                                PshControlType.NUMBER_TEXT),
                        new PshTableColumn("Lon", LON_WIDTH,
                                PshControlType.NUMBER_TEXT),
                        new PshTableColumn("County", 160, PshControlType.TEXT),
                        new PshTableColumn(RAINFALL_LBL, 85,
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

        GridData sashData = new GridData(SWT.CENTER, SWT.FILL, false, false);
        sashData.heightHint = 500;
        verticalSashForm.setLayoutData(sashData);

        createRemarksArea(verticalSashForm, true, true, true, "Final Remarks");

    }

    /**
     * Open the dialog for loading a rainfall user file and read file into
     * table.
     */
    @Override
    protected void loadUserFile() {

        // create and open user file dialog
        PshUserFileDialog userFileLoader = new PshUserFileDialog(getShell(),
                "Example User File",
                "The data fields must follow the example shown below"
                        + "\n\nCity,Latitude,Longitude,Disance from City,"
                        + "Direction from City,County,StationID,rainfall,incomplete"
                        + "\n\nWhere:\n*Distance from City is expressed in whole miles"
                        + "\n*Direction from City is expressed in Cardinal Direction (N, S, NE, SE, etc)"
                        + "\n\n-------------------------------------------------------------------------------------------------"
                        + "\nAn Example file with two rainfall observations will look as follows:"
                        + "\n\nPalm Harbor,28.08,-82.76,1,SE,Pinellas,-,2.34,I"
                        + "\nTampa,28.05,-82.40,2,E,Hillsborough,KBRA,5.78,-");
        userFileLoader.open();

        List<String[]> fieldsList = userFileLoader.getFieldsList(9);
        if (!fieldsList.isEmpty()) {
            for (String[] fields : fieldsList) {
                createRainfallTableItem(fields);
            }

            updatePreviewArea();
        }
    }

    /**
     * Create and populate a table item.
     * 
     * @param fields
     *            the fields for populating the item
     */
    private void createRainfallTableItem(String[] fields) {

        RainfallDataEntry rainfallData = new RainfallDataEntry();

        // Map fields to table.
        PshCity city = new PshCity();
        city.setName(fields[0].toUpperCase());

        if (!fields[6].equals("-")) {
            city.setStationID(fields[6].toUpperCase());
        } else {
            city.setStationID("");
        }

        if (NumberUtils.isNumber(fields[1])) {
            city.setLat(PshUtil.parseFloat(fields[1]));
        }
        if (NumberUtils.isNumber(fields[2])) {
            city.setLon(PshUtil.parseFloat(fields[2]));
        }

        city.setCounty(fields[5].toUpperCase());

        if (NumberUtils.isNumber(fields[7])) {
            rainfallData.setRainfall(PshUtil.parseFloat(fields[7]));
        }

        rainfallData.setDirection(fields[4].toUpperCase());

        if (NumberUtils.isNumber(fields[3])) {
            rainfallData.setDistance(PshUtil.parseFloat(fields[3]));
        }

        if (fields[8].equals("I")) {
            rainfallData.setIncomplete(fields[8].toUpperCase());
        } else {
            rainfallData.setIncomplete("");
        }

        rainfallData.setCity(city);
        table.addItem(rainfallData);

    }

    /**
     * Create rain date/time fields.
     */
    private void createRainFields() {
        Composite rainTimeFields = new Composite(mainComposite, SWT.NONE);
        GridLayout rainTimeLayout = new GridLayout(2, false);
        rainTimeLayout.marginHeight = 0;
        rainTimeFields.setLayout(rainTimeLayout);
        GridData rainTimeData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        rainTimeFields.setLayoutData(rainTimeData);

        GridLayout fieldsLayout = new GridLayout(2, false);
        fieldsLayout.marginHeight = 0;

        Group startComp = new Group(rainTimeFields, SWT.SHADOW_IN);
        startComp.setText("Rain Start");
        startComp.setLayout(new GridLayout(3, false));

        Group endComp = new Group(rainTimeFields, SWT.SHADOW_IN);
        endComp.setText("Rain End");
        endComp.setLayout(new GridLayout(3, false));

        Composite startMonComp = new Composite(startComp, SWT.NONE);
        startMonComp.setLayout(fieldsLayout);

        new Label(startMonComp, SWT.NORMAL).setText("Month:");
        startMonCombo = new CCombo(startMonComp,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);

        Composite startDayComp = new Composite(startComp, SWT.NONE);
        startDayComp.setLayout(fieldsLayout);

        new Label(startDayComp, SWT.NORMAL).setText("Day:");
        startDayCombo = new CCombo(startDayComp,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);

        Composite startHourComp = new Composite(startComp, SWT.NONE);
        startHourComp.setLayout(fieldsLayout);

        new Label(startHourComp, SWT.NORMAL).setText("Hour (UTC):");
        startHourCombo = new CCombo(startHourComp,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);

        Composite endMonComp = new Composite(endComp, SWT.NONE);
        endMonComp.setLayout(fieldsLayout);

        new Label(endMonComp, SWT.NORMAL).setText("Month:");
        endMonCombo = new CCombo(endMonComp,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);

        Composite endDayComp = new Composite(endComp, SWT.NONE);
        endDayComp.setLayout(fieldsLayout);

        new Label(endDayComp, SWT.NORMAL).setText("Day:");
        endDayCombo = new CCombo(endDayComp,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);

        Composite endHourComp = new Composite(endComp, SWT.NONE);
        endHourComp.setLayout(fieldsLayout);

        new Label(endHourComp, SWT.NORMAL).setText("Hour (UTC):");
        endHourCombo = new CCombo(endHourComp,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);

        // Fill month combos
        for (int mm = 1; mm < 13; mm++) {
            startMonCombo.add(String.format("%02d", mm));
            endMonCombo.add(String.format("%02d", mm));
        }

        // Fill hour combos
        for (int hh = 0; hh <= 2345; hh += 15) {
            if (hh % 100 == 60) {
                hh += 40;
            }
            startHourCombo.add(String.format("%04d", hh));
            endHourCombo.add(String.format("%04d", hh));
        }

        // Fill day combos based on the month and year
        startMonCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int startMonth = PshUtil.parseInt(startMonCombo.getText());
                if (startMonth == 0) {
                    startMonth = 1;
                }
                startDayCombo.setItems(getDayItems(startMonth - 1));
                startDayCombo.select(0);
            }
        });

        endMonCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int endMonth = PshUtil.parseInt(endMonCombo.getText());
                if (endMonth == 0) {
                    endMonth = 1;
                }
                endDayCombo.setItems(getDayItems(endMonth - 1));
                endDayCombo.select(0);
            }
        });

    }

    /**
     * Get a list of days in a particular month and year to add to a Combo.
     * 
     * @param month
     *            a month of the year (starting at 0)
     * @return a String[] containing days
     */
    private String[] getDayItems(int month) {
        int year = pshGeneratorData.getPshData().getYear();

        Calendar cal = new GregorianCalendar(year, month, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] dayItems = new String[daysInMonth];
        for (int ii = 0; ii < dayItems.length; ii++) {
            dayItems[ii] = String.format("%02d", ii + 1);
        }

        return dayItems;
    }

    @Override
    public void setDataList() {
        RainfallStormData rainfall = pshGeneratorData.getPshData()
                .getRainfall();

        List<RainfallDataEntry> rainfallDataList = rainfall.getData();

        if (rainfallDataList == null || rainfallDataList.isEmpty()) {
            emptyData = true;
        } else {
            for (RainfallDataEntry dataItem : rainfallDataList) {
                addItem(dataItem);
            }
            ((PshRainfallTable) table).initialSort();
        }

        String startMonth = rainfall.getStartMon();
        String startDay = rainfall.getStartDay();
        String startHour = rainfall.getStartHour();

        String endMonth = rainfall.getEndMon();
        String endDay = rainfall.getEndDay();
        String endHour = rainfall.getEndHour();

        Calendar startCal = TimeUtil.newCalendar();
        Calendar endCal = TimeUtil.newCalendar();

        if (startMonth != null && startMonth.trim().length() == 3) {
            try {
                startCal.setTime(new SimpleDateFormat("MMM", Locale.ENGLISH)
                        .parse(startMonth));
            } catch (ParseException e) {
                logger.warn("Could not parse month string: " + startMonth);
            }
        }

        if (endMonth != null && endMonth.trim().length() == 3) {
            try {
                endCal.setTime(new SimpleDateFormat("MMM", Locale.ENGLISH)
                        .parse(endMonth));
            } catch (ParseException e) {
                logger.warn("Could not parse month string: " + endMonth);
            }
        }

        int startMonthNum = startCal.get(Calendar.MONTH) + 1;
        startMonCombo.setText(String.format("%02d", startMonthNum));
        startDayCombo.setItems(getDayItems(startMonthNum));

        if (startDay != null && startDay.trim().length() > 0) {
            startDayCombo.setText(startDay);
        } else {
            startDayCombo.setText("" + startCal.get(Calendar.DAY_OF_MONTH));
        }

        if (startHour != null && startHour.trim().length() == 4) {
            startHourCombo.setText(startHour);
        } else {
            startHourCombo.select(0);
        }

        int endMonthNum = endCal.get(Calendar.MONTH) + 1;
        endMonCombo.setText(String.format("%02d", endMonthNum));
        endDayCombo.setItems(getDayItems(endMonthNum));

        if (endDay != null && endDay.trim().length() > 0) {
            endDayCombo.setText(endDay);
        } else {
            endDayCombo.setText("" + endCal.get(Calendar.DAY_OF_MONTH));
        }

        if (endHour != null && endHour.trim().length() == 4) {
            endHourCombo.setText(endHour);
        } else {
            endHourCombo.select(0);
        }

        String remarks = pshGeneratorData.getPshData().getRainfall()
                .getRemarks();
        if (!remarks.equals(StormData.NO_REMARKS)) {
            setRemarksText(remarks);
        }

        updatePreviewArea();
    }

    @Override
    public void clearTable() {
        super.clearTable();

        startMonCombo.deselectAll();
        startDayCombo.deselectAll();
        startHourCombo.deselectAll();

        endMonCombo.deselectAll();
        endDayCombo.deselectAll();
        endHourCombo.deselectAll();
    }

    @Override
    public void savePshData(List<StormDataEntry> entries) {
        if ((entries == null || entries.isEmpty()) && emptyData) {
            // do nothing if the was no data and we're trying to save without
            // entering any new data
        } else {
            List<RainfallDataEntry> rainfallDataList = new ArrayList<>();

            if (entries != null && !entries.isEmpty()) {
                emptyData = false;

                for (StormDataEntry entry : entries) {
                    rainfallDataList.add((RainfallDataEntry) entry);
                }
            }
            PshData pshData = pshGeneratorData.getPshData();
            pshData.getRainfall().setData(rainfallDataList);
            pshData.getRainfall().setStartMon(new DateFormatSymbols()
                    .getShortMonths()[PshUtil.parseInt(startMonCombo.getText())
                            - 1].toUpperCase());
            pshData.getRainfall()
                    .setEndMon(new DateFormatSymbols().getShortMonths()[PshUtil
                            .parseInt(endMonCombo.getText()) - 1]
                                    .toUpperCase());
            pshData.getRainfall().setStartDay(startDayCombo.getText());
            pshData.getRainfall().setEndDay(endDayCombo.getText());
            pshData.getRainfall().setStartHour(startHourCombo.getText());
            pshData.getRainfall().setEndHour(startHourCombo.getText());

            saveAlert(PshUtil.savePshData(pshData));

            pshGeneratorData.setPshData(pshData);
        }

    }

    @Override
    public void updatePreviewArea() {
        PshData tempData = new PshData();
        tempData.getRainfall()
                .setData(table.getTableData(RainfallDataEntry.class));

        previewText.setText(
                PshUtil.buildPshPreview(tempData, PshDataCategory.RAINFALL));

    }

    @Override
    public void saveFinalRemarks() {
        List<RainfallDataEntry> entries = pshGeneratorData.getPshData()
                .getRainfall().getData();
        List<RainfallDataEntry> dataList = new ArrayList<>(entries);

        PshData pshData = pshGeneratorData.getPshData();
        pshData.getRainfall().setData(dataList);

        saveAlert(PshUtil.savePshData(pshData));

        pshGeneratorData.setPshData(pshData);

    }

}