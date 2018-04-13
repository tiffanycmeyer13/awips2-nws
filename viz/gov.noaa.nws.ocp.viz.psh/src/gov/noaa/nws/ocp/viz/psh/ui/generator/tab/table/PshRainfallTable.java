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

import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshRainfallTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;

/**
 * Table for Rainfall tabs sorted automatically by rainfall (descending order).
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 27, 2017 #40299      wpaintsil   Initial creation.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshRainfallTable extends PshSortableTable {

    /**
     * Constructor
     * 
     * @param tab
     * @param columns
     */
    public PshRainfallTable(PshTabComp tab, PshTableColumn[] columns) {
        super(tab, columns);
    }

    @Override
    public void addColumnSelectionListeners() {
        for (TableColumn column : table.getColumns()) {
            if (column.getText().equals(PshRainfallTabComp.RAINFALL_LBL)) {
                addColumnSelectionListener(column);
            }
        }

    }

    /**
     * When table data is first loaded, ensure it's sorted by rainfall.
     */
    public void initialSort() {
        table.setSortColumn(table.getColumn(5));
        table.setSortDirection(SWT.DOWN);

        sortData(PshRainfallTabComp.RAINFALL_LBL, true);
    }

    @Override
    protected void setData(TableItem item) {
        super.setData(item);

        initialSort();
    }

    @Override
    protected void sortData(String columnName, boolean descending) {
        if (tableData != null && !tableData.isEmpty()) {

            List<RainfallDataEntry> dataList = new ArrayList<>();

            for (StormDataEntry data : tableData.values()) {
                dataList.add((RainfallDataEntry) data);
            }
            int compareReturn = descending ? -1 : 1;

            if (columnName.equals(PshRainfallTabComp.RAINFALL_LBL)) {
                Collections.sort(dataList, new Comparator<RainfallDataEntry>() {
                    @Override
                    public int compare(RainfallDataEntry o1,
                            RainfallDataEntry o2) {

                        float rainfall1 = o1.getRainfall();

                        float rainfall2 = o2.getRainfall();

                        if (rainfall1 > rainfall2) {
                            return 1 * compareReturn;
                        } else if (rainfall1 < rainfall2) {
                            return -1 * compareReturn;
                        } else {
                            return 0;
                        }
                    }
                });
            }

            tableData.clear();
            table.removeAll();
            for (RainfallDataEntry entry : dataList) {
                super.addItem(entry);
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

        RainfallDataEntry rain = (RainfallDataEntry) data;
        PshCity city = rain.getCity();
        newItem.setText(0, city.getName());
        // null id check
        if (city.getStationID() != null) {
            newItem.setText(1, city.getStationID());
        } else {
            newItem.setText(1, "");
        }
        newItem.setText(2, String.valueOf((float) city.getLat()));
        newItem.setText(3, String.valueOf((float) city.getLon()));
        newItem.setText(4, city.getCounty());
        newItem.setText(5, String.valueOf(rain.getRainfall()));
        newItem.setText(6, rain.getDirection());
        newItem.setText(7, String.valueOf(rain.getDistance()));
        // incomplete indicator check
        if (!rain.getIncomplete().equals("-")) {
            newItem.setText(8, String.valueOf(rain.getIncomplete()));
        } else {
            newItem.setText(8, "");
        }

        insertTableData(newItem, data, tableIndex);
    }

}
