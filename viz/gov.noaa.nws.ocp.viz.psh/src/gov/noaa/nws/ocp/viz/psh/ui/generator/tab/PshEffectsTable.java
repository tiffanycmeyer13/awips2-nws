/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import gov.noaa.nws.ocp.common.dataplugin.psh.EffectDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTable;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table.PshTableColumn;

/**
 * Table for Effects tab
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
public class PshEffectsTable extends PshTable {

    public PshEffectsTable(PshTabComp tab, PshTableColumn[] columns) {
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

        newItem.setText(0, ((EffectDataEntry) data).getCounty());
        newItem.setText(1,
                String.valueOf(((EffectDataEntry) data).getDeaths()));
        newItem.setText(2,
                String.valueOf(((EffectDataEntry) data).getInjuries()));
        newItem.setText(3,
                String.valueOf(((EffectDataEntry) data).getEvacuations()));

        insertTableData(newItem, data, tableIndex);
    }

}
