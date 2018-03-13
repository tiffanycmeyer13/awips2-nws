/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import gov.noaa.nws.ocp.common.dataplugin.psh.FloodingDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;

/**
 * Table for Flooding tab
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2018 #46636      wpaintsil   Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshFloodingTable extends PshTable {

    public PshFloodingTable(PshTabComp tab, PshTableColumn[] columns) {
        super(tab, columns);
    }

    @Override
    public void addItem(StormDataEntry data, int tableIndex) {
        TableItem newItem;

        if (tableIndex > -1) {
            newItem = new TableItem(table, SWT.NONE, tableIndex);
        } else {
            newItem = new TableItem(table, SWT.NONE);
        }

        newItem.setText(0, ((FloodingDataEntry) data).getCounty());

        insertTableData(newItem, data, tableIndex);

    }

}
