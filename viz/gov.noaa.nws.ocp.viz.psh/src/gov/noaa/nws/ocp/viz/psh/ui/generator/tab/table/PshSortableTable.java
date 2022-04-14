/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;

/**
 * Sortable table for certain tabs
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
public abstract class PshSortableTable extends PshTable {

    /**
     * Constructor
     * 
     * @param tab
     * @param columns
     */
    public PshSortableTable(PshTabComp tab, PshTableColumn[] columns) {
        super(tab, columns);
    }

    /**
     * Add a SelectionListener to the specified column.
     * 
     * @param column
     */
    protected void addColumnSelectionListener(TableColumn column) {
        column.setToolTipText("Sort Descending");

        column.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                sortColumn((TableColumn) e.widget);

            }
        });

    }

    /**
     * Sort columns.
     * 
     * @param column
     */
    protected void sortColumn(TableColumn column) {

        if (column.equals(table.getSortColumn())) {
            table.setSortDirection(
                    table.getSortDirection() == SWT.DOWN ? SWT.UP : SWT.DOWN);
        } else {
            table.setSortColumn(column);
            table.setSortDirection(SWT.DOWN);
        }

        if (table.getSortDirection() == SWT.UP) {
            column.setToolTipText("Sort Descending");
        } else {
            column.setToolTipText("Sort Ascending");
        }

        sortData(column.getText(), table.getSortDirection() == SWT.DOWN);

    }

    /**
     * Add SelectionListeners for column sorting.
     */
    public abstract void addColumnSelectionListeners();

    /**
     * Sort the table entries.
     * 
     * @param columnName
     * @param descending
     *            true if sorting in descending order; false if sorting in
     *            ascending order
     */
    protected abstract void sortData(String columnName, boolean descending);

}
