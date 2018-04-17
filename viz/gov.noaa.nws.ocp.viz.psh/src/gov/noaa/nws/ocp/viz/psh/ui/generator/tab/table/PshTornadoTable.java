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
import gov.noaa.nws.ocp.common.dataplugin.psh.TornadoDataEntry;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;

/**
 * Sortable table for Tornado tabs
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
public class PshTornadoTable extends PshSortableTable {

    /**
     * Constructor
     * 
     * @param tab
     * @param columns
     */
    public PshTornadoTable(PshTabComp tab, PshTableColumn[] columns) {
        super(tab, columns);
    }

    @Override
    public void addColumnSelectionListeners() {
        for (TableColumn column : table.getColumns()) {
            if (column.getText().equals(PshTabComp.COUNTY_LBL_STRING)) {
                addColumnSelectionListener(column);
            }
        }
    }

    @Override
    protected void sortData(String columnName, boolean descending) {

        if (tableData != null && !tableData.isEmpty()) {

            List<TornadoDataEntry> dataList = new ArrayList<>();

            for (StormDataEntry data : tableData.values()) {
                dataList.add((TornadoDataEntry) data);
            }
            int compareReturn = descending ? -1 : 1;

            if (columnName.equals(PshTabComp.COUNTY_LBL_STRING)) {
                Collections.sort(dataList, new Comparator<TornadoDataEntry>() {
                    @Override
                    public int compare(TornadoDataEntry o1,
                            TornadoDataEntry o2) {

                        return o1.getLocation().getCounty().compareTo(
                                o2.getLocation().getCounty()) * compareReturn;
                    }
                });
            }

            tableData.clear();
            table.removeAll();
            for (TornadoDataEntry entry : dataList) {
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

        TornadoDataEntry tornado = (TornadoDataEntry) data;
        PshCity city1 = tornado.getLocation();

        newItem.setText(0, city1.getName());
        newItem.setText(1, city1.getCounty());
        newItem.setText(2, tornado.getMagnitude());
        newItem.setText(3, tornado.getDatetime());
        newItem.setText(4, String.valueOf(city1.getLat()));
        newItem.setText(5, String.valueOf(city1.getLon()));
        newItem.setText(6, tornado.getDirection());
        newItem.setText(7, String.valueOf(tornado.getDistance()));
        newItem.setText(8, String.valueOf(tornado.getIncomplete()));

        insertTableData(newItem, data, tableIndex);
    }

}
