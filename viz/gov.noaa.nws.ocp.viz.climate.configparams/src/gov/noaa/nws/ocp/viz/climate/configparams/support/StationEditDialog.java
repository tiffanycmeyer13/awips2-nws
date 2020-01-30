/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.configparams.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.StationLocationRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.configparams.ReplaceStationsServiceRequest;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;

/**
 * 
 * Dialog for "Edit Station List"
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ --------  ----------- --------------------------
 * Mar 8, 2016            xzhang      Initial creation
 * OCT 3, 2016  20639     wkwock      Added warning message before save stations
 * OCT 18, 2016 20639     wkwock      Better way to delete a table item. Add modify listener
 * 20 DEC 2016  26404     amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 09 FEB 2017  20640     jwu         Set return value to indicate changes.
 * 27 FEB 2017  27420     amoore      Clarify some variable names. Cleanup table column widths.
 * 12 MAY 2017  33104     amoore      Address FindBugs.
 * 17 MAY 2017  33104     amoore      Fix general logic and errors.
 * 27 JUL 2017  33104     amoore      Do not use effectively final functionality, for 1.7 build.
 * 19 SEP 2017  38124     amoore      Use GC for text control sizes.
 * 17 OCT 2017  38808     amoore      Well-defined ordering for saving stations, following the table.
 *                                    Found and fixed issue where one could not remove and add back in
 *                                    the same station and save successfully. Don't let users save
 *                                    incomplete rows.
 * 08 JAN 2020  DR21753   wpaintsil   Truncate the station name before adding it to the database
 *                                    if it is longer than 30 characters.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public class StationEditDialog extends ClimateCaveChangeTrackDialog {
    /**
     * The maximum length for a station.
     */
    private static final int MAX_STATION_NAME_LENGTH = 30;

    /**
     * Stations retrieved from DB this dialog session, so as not to need
     * repeated DB calls based on codes.
     */
    private Map<String, Station> pastStations = new HashMap<>();

    /**
     * displayStations are stations displaying on GUI
     */
    protected Map<TableItem, Station> displayStations = new HashMap<>();

    /**
     * Station table
     */
    private Table stationTable;

    /**
     * Red color to indicate an invalid input
     */
    private Color redColor = null;

    /**
     * Black color to indicate a valid input
     */
    private Color blackColor = null;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public StationEditDialog(Shell parent) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        setText("Edit Station List");

        // Initialize "returnValue" to "false" to indicate no changes so far.
        this.setReturnValue(false);
    }

    @Override
    protected void initializeComponents(final Shell shell) {
        shell.setLayout(new GridLayout(2, true));

        getColors();

        buildStationTable();

        buildAddButton();
        buildBottomButtons();

        displayStations();
    }

    /**
     * get colors
     */
    private void getColors() {
        redColor = this.getDisplay().getSystemColor(SWT.COLOR_RED);
        blackColor = this.getDisplay().getSystemColor(SWT.COLOR_BLACK);
    }

    /**
     * build the station table
     */
    private void buildStationTable() {
        stationTable = new Table(shell,
                SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
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

        TableColumn checkColumn = new TableColumn(stationTable, SWT.NONE);
        checkColumn.setWidth(11 * fontWidth);
        checkColumn.setText("Std time\nAll Year");

        TableColumn codeColumn = new TableColumn(stationTable, SWT.NONE);
        codeColumn.setWidth(14 * fontWidth);
        codeColumn.setText("  Station ID");

        TableColumn nameColumn = new TableColumn(stationTable, SWT.NONE);
        nameColumn.setWidth(40 * fontWidth);
        nameColumn.setText("        Station Name");

        TableColumn deleteColumn = new TableColumn(stationTable, SWT.NONE);
        deleteColumn.setWidth(11 * fontWidth);
        deleteColumn.setResizable(false);

        stationTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    changeListener.setChangesUnsaved(true);
                }
            }
        });
    }

    /**
     * build the add new station button
     */
    private void buildAddButton() {
        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        final Button addNewBtn = new Button(shell, SWT.PUSH);
        GridData addnewGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                2, 1);
        addnewGd.widthHint = 240;
        addNewBtn.setLayoutData(addnewGd);
        addNewBtn.setText("Add New Station");

        addNewBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Station newStation = new Station();
                addATableItem(newStation);
            }
        });
    }

    /**
     * add an item to the table
     * 
     * @param station
     */
    private void addATableItem(Station station) {
        if (station == null) {
            return;
        }

        final TableItem item = new TableItem(stationTable, SWT.BORDER);
        item.setChecked(station.getStdAllYear() == 1);

        final Text stationCodeTxt = new Text(stationTable, SWT.BORDER);
        if (station.getIcaoId() != null) {
            stationCodeTxt.setText(station.getIcaoId());
        }

        final TableEditor codeEditor = new TableEditor(stationTable);
        codeEditor.grabHorizontal = true;
        codeEditor.setEditor(stationCodeTxt, item, 1);

        final Text stationNametxt = new Text(stationTable, SWT.BORDER);
        if (station.getStationName() != null) {
            stationNametxt.setText(station.getStationName());
        }

        final TableEditor nameEditor = new TableEditor(stationTable);
        nameEditor.grabHorizontal = true;
        nameEditor.setEditor(stationNametxt, item, 2);

        final Button deleteBtn = new Button(stationTable, SWT.PUSH);
        deleteBtn.setText("Delete");

        final TableEditor deleteEditor = new TableEditor(stationTable);
        deleteEditor.grabHorizontal = true;
        deleteEditor.setEditor(deleteBtn, item, 3);

        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeTableItem(item, nameEditor, stationNametxt, codeEditor,
                        stationCodeTxt, deleteEditor, deleteBtn);
            }
        });

        displayStations.put(item, station);

        stationCodeTxt.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                updateStationName(e, stationNametxt, item);
            }
        });
        stationCodeTxt.addListener(SWT.Modify, changeListener);

        stationNametxt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Station station = displayStations.get(item);
                if (station != null) {
                    station.setStationName(stationNametxt.getText());
                } else {
                    logger.debug("Cannot assign station name to null station.");
                }
            }
        });
        stationNametxt.addListener(SWT.Modify, changeListener);
    }

    /**
     * Update station name base on station ID
     * 
     * @param event
     * @param stationNametxt
     * @param tableItem
     */
    private void updateStationName(VerifyEvent event, Text stationNametxt,
            TableItem tableItem) {
        String originalText = ((Text) event.widget).getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);
        if (newText.length() > 4) {
            event.doit = false;
        } else if (newText.length() == 4) {
            // Search for duplication
            boolean found = false;
            for (Entry<TableItem, Station> entry : displayStations.entrySet()) {
                TableItem item = entry.getKey();
                if (item != tableItem
                        && newText.equals(entry.getValue().getIcaoId())) {
                    stationNametxt.setText("Duplicated ID");
                    stationNametxt.setForeground(redColor);
                    found = true;
                }
            }

            // Search historical list
            if (!found) {
                Station foundStation = pastStations.get(newText);
                if (foundStation != null) {
                    stationNametxt.setText(foundStation.getStationName());
                    stationNametxt.setForeground(blackColor);
                    found = true;
                    displayStations.put(tableItem, foundStation);
                }
            }

            // Still not found, let's try DB
            if (!found) {
                StationLocationRequest slr = new StationLocationRequest(
                        newText);
                try {
                    Station newStation = (Station) ThriftClient
                            .sendRequest(slr);
                    if (newStation == null) {
                        logger.debug("Station code: [" + newText
                                + "] is not present in the station location table.");
                        stationNametxt.setText("Invalid ID");
                        stationNametxt.setForeground(redColor);
                    } else {
                        stationNametxt.setText(newStation.getStationName());
                        stationNametxt.setForeground(blackColor);
                        displayStations.put(tableItem, newStation);
                        pastStations.put(newStation.getIcaoId(), newStation);
                    }
                } catch (VizException e) {
                    stationNametxt.setText("Invalid ID");
                    stationNametxt.setForeground(redColor);
                }
            }
        } else {
            stationNametxt.setText("");
            displayStations.remove(tableItem);
        }
    }

    /**
     * remove an item from the table
     * 
     * @param item
     * @param nameEditor
     * @param stationNametxt
     * @param idEditor
     * @param stationIDTxt
     * @param deleteEditor
     * @param deleteBtn
     */
    private void removeTableItem(TableItem item, TableEditor nameEditor,
            Text stationNametxt, TableEditor idEditor, Text stationIDTxt,
            TableEditor deleteEditor, Button deleteBtn) {
        item.dispose();
        displayStations.remove(item);
        nameEditor.dispose();
        stationNametxt.dispose();
        idEditor.dispose();
        stationIDTxt.dispose();
        deleteEditor.dispose();
        deleteBtn.dispose();

        changeListener.setChangesUnsaved(true);

        // update table view
        stationTable.redraw();
        stationTable.update();
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
        saveGd.widthHint = 240;
        saveBtn.setLayoutData(saveGd);
        saveBtn.setText("Save Stations");

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveStations();
            }
        });

        Button cancelBtn = new Button(shell, SWT.PUSH);
        GridData cancelGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        cancelGd.widthHint = 240;
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
     * Replace records in DB with these records
     */
    protected void saveStations() {
        boolean save = MessageDialog.openQuestion(shell, "Continue to save?",
                "If you save these changes, you should also retrieve and save \n"
                        + "all existing products related to the changes. \nDo you wish to continue?");
        if (!save) {
            return;
        }

        List<Station> stations;
        try {
            stations = getUpdatedStations();
        } catch (ClimateInvalidParameterException e) {
            logger.error(
                    "Some row in the Edit Station Dialog is not completed.", e);
            MessageDialog.openError(shell, "Incomplete Row",
                    "At least one row is not completed. Delete incomplete rows and try again.");
            return;
        }

        boolean isSuccess = false;
        try {
            ReplaceStationsServiceRequest rssr = new ReplaceStationsServiceRequest(
                    stations);
            isSuccess = (boolean) ThriftClient.sendRequest(rssr);
        } catch (VizException e) {
            logger.error("Failed to update stations.", e);
        }

        if (isSuccess) {
            MessageDialog.openInformation(shell, "Save successful",
                    "Updating of stations completed.");
        } else {
            MessageDialog.openError(shell, "Save unsuccessful",
                    "Failed to update stations.");
        }

        if (isSuccess) {
            changeListener.setChangesUnsaved(false);
            // Set "returnValue" to "true" to indicate values have been changed.
            this.setReturnValue(true);

            close();
        }
    }

    /**
     * get the updated displayStations with latest stdAllYears values
     * 
     * @return station list
     * @throws ClimateInvalidParameterException
     *             if some row is not completed.
     */
    private List<Station> getUpdatedStations()
            throws ClimateInvalidParameterException {
        List<Station> stations = new ArrayList<>();
        // get stations in table order

        StringBuilder warningMessage = new StringBuilder();
        for (TableItem item : stationTable.getItems()) {
            short stdAllYear = (short) (item.getChecked() ? 1 : 0);

            Station station = displayStations.get(item);
            if (station.getStationName() == null
                    || station.getStationName().isEmpty()) {
                throw new ClimateInvalidParameterException(
                        "Null or empty station name at row: ["
                                + (stationTable.indexOf(item) + 1) + "]");
            }

            if (station.getStationName().length() > MAX_STATION_NAME_LENGTH) {
                String truncatedName = station.getStationName().substring(0,
                        MAX_STATION_NAME_LENGTH);

                warningMessage.append("'" + station.getStationName()
                        + "' was truncated to '" + truncatedName + ".'\n\n");

                station.setStationName(truncatedName);
            }

            station.setStdAllYear(stdAllYear);
            stations.add(station);
        }

        if (!warningMessage.toString().isEmpty()) {
            MessageDialog.openWarning(shell,
                    "One or More Station Names Were Truncated",
                    "The following station names were longer than the maximum length of "
                            + MAX_STATION_NAME_LENGTH + " characters:\n\n"
                            + warningMessage.toString());
        }
        return stations;
    }

    /**
     * Get stations from DB and display them.
     */
    @SuppressWarnings("unchecked")
    private void displayStations() {
        try {
            ClimateRequest request = new ClimateRequest();
            request.setRequestType(RequestType.GET_STATIONS);

            List<Station> existingStations = (List<Station>) ThriftClient
                    .sendRequest(request);

            changeListener.setIgnoreChanges(true);
            for (Station station : existingStations) {
                Station newStation = new Station(station);
                addATableItem(newStation);
                pastStations.put(station.getIcaoId(), station);
            }
            changeListener.setIgnoreChanges(false);
            changeListener.setChangesUnsaved(false);
        } catch (VizException e) {
            logger.error("Unable to get stations from cli_sta_setup table.", e);
            return;
        }
    }
}
