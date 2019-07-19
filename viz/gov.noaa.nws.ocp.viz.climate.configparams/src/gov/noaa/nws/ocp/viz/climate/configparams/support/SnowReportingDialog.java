package gov.noaa.nws.ocp.viz.climate.configparams.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;

/**
 * Dialog to edit whether any of the available stations report snow
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * May 22, 2019 DR 21099    wpaintsil   Initial creation
 * </pre>
 *
 * @author wpaintsil
 */
public class SnowReportingDialog extends ClimateCaveChangeTrackDialog {

    /**
     * Provided preferences instance
     */
    private ClimateGlobal preferenceValues;

    /**
     * Modifiable copy of {@code preferenceValues.stationDesignatorOverrides}
     */
    private Set<String> snowReportingStations;

    /**
     * Map of all stations and their checkboxes
     */
    private Map<String, Button> checkboxes = new HashMap<>();

    /**
     * Station table
     */
    private Table stationTable;

    public SnowReportingDialog(Shell parent, ClimateGlobal preferenceValues) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        this.preferenceValues = preferenceValues;

        snowReportingStations = new HashSet<String>(
                preferenceValues.getSnowReportingStations());

        setText("Snow-Reporting Stations");
        // Initialize "returnValue" to "false" to indicate no changes so far.
        this.setReturnValue(false);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new GridLayout(2, true));
        buildTable();
        buildBottomButtons();
        displayStations();
    }

    /**
     * Build the table viewer.
     */
    private void buildTable() {
        stationTable = new Table(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        stationTable.setHeaderVisible(true);
        stationTable.setLinesVisible(true);

        GC tableGC = new GC(stationTable);
        int fontWidth = tableGC.getFontMetrics().getAverageCharWidth();
        /*
         * to fit a fair amount of stations within the list, given no other
         * components are in the row
         */
        int fontHeight = tableGC.getFontMetrics().getHeight();
        tableGC.dispose();

        GridData stationTableGd = new GridData(SWT.FILL, SWT.TOP, true, true, 2,
                1);
        stationTableGd.heightHint = 17 * fontHeight;
        stationTable.setLayoutData(stationTableGd);

        TableColumn codeColumn = new TableColumn(stationTable, SWT.NONE);
        codeColumn.setWidth(10 * fontWidth);
        codeColumn.setText("Station ID");
        codeColumn.setMoveable(false);
        codeColumn.setResizable(false);

        TableColumn deleteColumn = new TableColumn(stationTable, SWT.NONE);
        deleteColumn.setWidth(10 * fontWidth);
        deleteColumn.setText("Reports Snow");
        deleteColumn.setMoveable(false);
        deleteColumn.setResizable(false);
    }

    /**
     * add an item to the table
     * 
     * @param station
     */
    private void addTableItem(Station station) {
        if (station == null) {
            return;
        }

        final TableItem item = new TableItem(stationTable, SWT.BORDER);

        item.setText(0, station.getIcaoId());

        final Button nonSnowBtn = new Button(stationTable, SWT.CHECK);
        nonSnowBtn.setSelection(
                snowReportingStations.contains(station.getIcaoId()));

        final TableEditor nonSnowEditor = new TableEditor(stationTable);
        nonSnowEditor.grabHorizontal = true;

        nonSnowEditor.setEditor(nonSnowBtn, item, 1);

        nonSnowBtn.addListener(SWT.Selection, changeListener);
        checkboxes.put(station.getIcaoId(), nonSnowBtn);
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
        saveBtn.setText("Save");

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                savePreferences();
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
     * nonReportingStations values and save them.
     */
    private void savePreferences() {
        for (String stationID : checkboxes.keySet()) {
            if (checkboxes.get(stationID).getSelection()) {
                snowReportingStations.add(stationID);
            } else {
                snowReportingStations.remove(stationID);
            }
        }

        preferenceValues.setSnowReportingStations(snowReportingStations);

        boolean isSuccess = savePreferences(preferenceValues);
        if (isSuccess) {
            changeListener.setChangesUnsaved(false);
            // Set "returnValue" to "true" to indicate values have been changed.
            this.setReturnValue(true);
            close();
        }
    }

    /**
     * Write preferences to file
     *
     * @param preferenceValues
     */
    private boolean savePreferences(ClimateGlobal preferenceValues) {
        String message = "Snow-reporting stations saved.";
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

        MessageDialog.openInformation(shell, "Snow-Reporting Stations",
                message);
        return result;
    }

    /**
     * Get stations from DB and display them.
     */
    @SuppressWarnings("unchecked")
    private void displayStations() {
        try {
            ClimateRequest request = new ClimateRequest();
            request.setRequestType(RequestType.GET_STATIONS);

            List<Station> existingStations = new ArrayList<>();
            existingStations
                    .addAll((List<Station>) ThriftClient.sendRequest(request));

            changeListener.setIgnoreChanges(true);
            for (Station station : existingStations) {
                Station newStation = new Station(station);
                addTableItem(newStation);
            }
            changeListener.setIgnoreChanges(false);
            changeListener.setChangesUnsaved(false);
        } catch (VizException e) {
            logger.error("Unable to get stations from cli_sta_setup table.", e);
            return;
        }
    }
}
