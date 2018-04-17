/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.WaterLevelDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshWaterLevelTabComp;

/**
 * Sortable table for Water Level tabs
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 22, 2017 #40299      wpaintsil   Initial creation.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshWaterLevelTable extends PshSortableTable {

    /**
     * Constructor
     * 
     * @param tab
     * @param columns
     */
    public PshWaterLevelTable(PshTabComp tab, PshTableColumn[] columns) {
        super(tab, columns);
    }

    /**
     * Add SelectionListeners for column sorting.
     */
    @Override
    public void addColumnSelectionListeners() {
        for (TableColumn column : table.getColumns()) {
            switch (column.getText()) {
            case PshTabComp.COUNTY_LBL_STRING:
            case PshWaterLevelTabComp.WATER_LVL_LBL_STRING:
                addColumnSelectionListener(column);
                break;
            }
        }
    }

    /**
     * Sort the table entries.
     * 
     * @param columnName
     * @param descending
     *            true if sorting in descending order; false if sorting in
     *            ascending order
     */
    @Override
    protected void sortData(String columnName, boolean descending) {
        if (tableData != null && !tableData.isEmpty()) {

            List<WaterLevelDataEntry> dataList = new ArrayList<>();

            for (StormDataEntry data : tableData.values()) {
                dataList.add((WaterLevelDataEntry) data);
            }
            int compareReturn = descending ? -1 : 1;

            switch (columnName) {
            case PshWaterLevelTabComp.WATER_LVL_LBL_STRING:
                Collections.sort(dataList,
                        new Comparator<WaterLevelDataEntry>() {
                            @Override
                            public int compare(WaterLevelDataEntry o1,
                                    WaterLevelDataEntry o2) {

                                float waterLvl1 = o1.getWaterLevel();

                                float waterLvl2 = o2.getWaterLevel();

                                if (waterLvl1 > waterLvl2) {
                                    return 1 * compareReturn;
                                } else if (waterLvl1 < waterLvl2) {
                                    return -1 * compareReturn;
                                } else {
                                    return 0;
                                }
                            }
                        });

                break;
            case PshTabComp.COUNTY_LBL_STRING:
                Collections.sort(dataList,
                        new Comparator<WaterLevelDataEntry>() {
                            @Override
                            public int compare(WaterLevelDataEntry o1,
                                    WaterLevelDataEntry o2) {

                                return o1.getLocation().getCounty()
                                        .compareTo(o2.getLocation().getCounty())
                                        * compareReturn;
                            }
                        });
                break;
            }

            tableData.clear();
            table.removeAll();
            for (WaterLevelDataEntry entry : dataList) {
                addItem(entry);
            }
            tab.updatePreviewArea();
        }
    }

    @Override
    public void addItem(StormDataEntry data, int tableIndex) {
        TableItem newItem;

        if (tableIndex > -1) {
            newItem = new TableItem(table, SWT.NONE, tableIndex);
        } else {
            newItem = new TableItem(table, SWT.NONE);
        }

        WaterLevelDataEntry waterLevel = (WaterLevelDataEntry) data;
        PshCity city0 = waterLevel.getLocation();
        newItem.setText(0, city0.getName());
        newItem.setText(1, String.valueOf(city0.getStationID()));
        newItem.setText(2, city0.getCounty());
        newItem.setText(3, city0.getState());
        newItem.setText(4, String.valueOf(city0.getLat()));
        newItem.setText(5, String.valueOf(city0.getLon()));
        newItem.setText(6, String.valueOf(waterLevel.getWaterLevel()));
        newItem.setText(7, waterLevel.getDatum());
        newItem.setText(8, waterLevel.getDatetime());
        newItem.setText(9, waterLevel.getSource());

        newItem.setText(10, String.valueOf(waterLevel.getIncomplete()));

        insertTableData(newItem, data, tableIndex);
    }

}
