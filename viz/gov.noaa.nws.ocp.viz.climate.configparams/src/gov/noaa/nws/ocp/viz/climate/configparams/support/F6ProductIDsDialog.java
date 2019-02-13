package gov.noaa.nws.ocp.viz.climate.configparams.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;

/**
 * Dialog to edit overrides of the AFOS Designator (the XXX part of the PIL)
 * assigned to F6 products.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Oct 12, 2018 DR 20897    dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class F6ProductIDsDialog extends ClimateCaveChangeTrackDialog {

    private static final int STATION_ID_COLUMN = 0;

    private static final int DESIGNATOR_COLUMN = 1;

    /**
     * Provided preferences instance
     */
    private ClimateGlobal preferenceValues;

    /**
     * Observable list of stations used by table viewer
     */
    private WritableList<Station> stationList = new WritableList<>();

    /**
     * Modifiable copy of {@code preferenceValues.stationDesignatorOverrides}
     */
    private Map<String, String> stationDesignatorOverrides;

    /**
     * Label provider for the table viewer
     */
    protected ProdIDLabelProvider labelProvider = new ProdIDLabelProvider();

    public F6ProductIDsDialog(Shell parent, ClimateGlobal preferenceValues) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        this.preferenceValues = preferenceValues;
        setText("Edit F6 Product ID Overrides");
        // Initialize "returnValue" to "false" to indicate no changes so far.
        this.setReturnValue(false);
    }

    /**
     * Load stations and copy {@code stationDesignatorOverrides} values from provided
     * preferences.
     */
    private void loadData() {
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);
        try {
            stationList
                    .addAll((List<Station>) ThriftClient.sendRequest(request));
        } catch (VizException e) {
            logger.error("Unable to get stations from cli_sta_setup table.", e);
            return;
        }
        stationDesignatorOverrides = new HashMap<>(
                preferenceValues.getStationDesignatorOverrides());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new GridLayout(2, true));
        loadData();
        buildTable();
        buildBottomButtons();
    }

    /**
     * Build the table viewer.
     */
    private void buildTable() {
        TableViewer viewer = new TableViewer(shell,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        String[] columnTitles = { "Station ID", "Designator\nOverride" };
        int[] columnWidths = { 12, 12 };
        GC gc = new GC(viewer.getTable());
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();
        for (int i = 0; i < columnTitles.length; ++i) {
            TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
            column.getColumn().setText(columnTitles[i]);
            column.getColumn().setWidth(columnWidths[i] * fontWidth);
            column.getColumn().setResizable(true);
            column.setEditingSupport(new ProdIDEditingSupport(viewer, i));
        }
        viewer.setLabelProvider(labelProvider);
        viewer.setContentProvider(new ObservableListContentProvider());
        viewer.setInput(stationList);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
    }

    /**
     * build save and cancel buttons
     */
    private void buildBottomButtons() {

        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        final Button saveBtn = new Button(shell, SWT.PUSH);
        GridData saveGd = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,
                1);
        GC gc = new GC(saveBtn);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        saveGd.widthHint = fontWidth * 22;
        saveBtn.setLayoutData(saveGd);
        saveBtn.setText("Save Overrides");

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveProductIDs();
            }

        });

        Button cancelBtn = new Button(shell, SWT.PUSH);
        GridData cancelGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        cancelGd.widthHint = saveGd.widthHint;
        cancelBtn.setLayoutData(cancelGd);
        cancelBtn.setText("Cancel");

        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * Update the provided preferences instances with modified
     * stationDesignatorOverrides values and save them.
     */
    private void saveProductIDs() {
        preferenceValues
                .setStationDesignatorOverrides(stationDesignatorOverrides);
        boolean isSuccess = writePreferences(preferenceValues);
        if (isSuccess) {
            changeListener.setChangesUnsaved(false);
            // Set "returnValue" to "true" to indicate values have been changed.
            this.setReturnValue(true);
            close();
        }
    }

    private class ProdIDLabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Station station = (Station) element;
            if (columnIndex == STATION_ID_COLUMN) {
                return station.getIcaoId();
            } else if (columnIndex == DESIGNATOR_COLUMN) {
                String value = stationDesignatorOverrides.get(station.getIcaoId());
                return value != null ? value : "";
            }
            return null;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // nothing
        }

        @Override
        public void dispose() {
            // nothing
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return true;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // nothing
        }
    }

    protected class ProdIDEditingSupport extends EditingSupport {
        private int columnIndex;

        private CellEditor editor;

        public ProdIDEditingSupport(ColumnViewer viewer, int columnIndex) {
            super(viewer);
            this.columnIndex = columnIndex;
            this.editor = new TextCellEditor((Composite) viewer.getControl());
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return columnIndex == DESIGNATOR_COLUMN;
        }

        @Override
        protected Object getValue(Object element) {
            return labelProvider.getColumnText(element, columnIndex);
        }

        /**
         * Updates product ID values when edited by user.
         * <p>
         * Only changes {@code stationDesignatorOverrides} and sets the modified flag if
         * there is an actual change from the current value. Coerces the value
         * to upper case and maximum length of 3. Blank values are treated as
         * "not set".
         */
        @Override
        protected void setValue(Object element, Object value) {
            Station station = (Station) element;
            if (columnIndex == DESIGNATOR_COLUMN) {
                String icao = station.getIcaoId();
                String oldValue = stationDesignatorOverrides.get(icao);
                String newValue = (String) value;
                if (newValue != null) {
                    newValue = newValue.trim().toUpperCase();
                    if (newValue.length() < 1) {
                        newValue = null;
                    } else if (newValue.length() > 3) {
                        newValue = newValue.substring(0, 3);
                    }
                }
                if ((oldValue == null) != (newValue == null)
                        || (oldValue != null && !oldValue.equals(newValue))) {
                    if (newValue != null) {
                        stationDesignatorOverrides.put(icao, newValue);
                    } else {
                        stationDesignatorOverrides.remove(icao);
                        newValue = null;
                    }
                    changeListener.setChangesUnsaved(true);
                }
                // Cause the table view to update
                int i = stationList.indexOf(element);
                if (i != -1) {
                    stationList.set(i, station);
                }
            }
        }
    }

    /**
     * Write preferences to file
     *
     * @param preferenceValues
     */
    private boolean writePreferences(ClimateGlobal preferenceValues) {
        String message = "F6 Product ID overrides saved.";
        ClimateRequest cr = new ClimateRequest();
        cr.setRequestType(RequestType.SAVE_GLOBAL);
        cr.setClimateGlobal(preferenceValues);
        boolean result = false;
        try {
            int status = (int) ThriftClient.sendRequest(cr);

            if (status == 0) {
                changeListener.setChangesUnsaved(false);
                result = true;
            } else if (status == -1) {
                message = "Error: Could not open data file for writing.";
            } else if (status == -2) {
                message = "Error: Could not close data file after writing.";
            }
        } catch (VizException e) {
            logger.error("Failed to save preferences.", e);
            message = "Error: Failed to save data.";
        }

        MessageDialog.openInformation(shell, "F6 Product ID Overrides",
                message);
        return result;
    }
}
