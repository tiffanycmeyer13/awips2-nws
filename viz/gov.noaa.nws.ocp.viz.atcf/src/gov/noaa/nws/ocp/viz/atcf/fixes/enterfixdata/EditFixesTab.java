/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FixFormat;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * General tab structure for Enter/Edit Fixes Data menu.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 01, 2021 87786       wpaintsil   initial creation
 * Jun 24, 2021 91759       wpaintsil   Fix redraw.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public abstract class EditFixesTab extends Composite {

    /**
     * The storm under review.
     */
    protected Storm storm;

    /**
     * The text field with the date time group(DTG) text.
     */
    protected Text dtgText;

    /**
     * A widget holding a list of selectable date time groups.
     */
    protected org.eclipse.swt.widgets.List dtgSelectionList;

    /**
     * A flag for whether a) data is being newly entered (false) or b) already
     * existing data is being edited (true)
     */
    protected boolean editData;

    /**
     * A list of selected f-deck records.
     */
    protected List<FDeckRecord> selectedRecords;

    /**
     * An f-deck sandbox ID.
     */
    protected int sandBoxID;

    /**
     * Save, Delete, and Flag button names
     */
    protected String[] actionBtnNames;

    /**
     * The parent shell
     */
    protected Shell shell;

    /**
     * The name type of data shown in the tab
     */
    protected String tabName;

    protected Map<FDeckRecordKey, List<FDeckRecord>> fixDataMap;

    /**
     * default constructor
     * 
     * @param parent
     * @param style
     */
    protected EditFixesTab(Composite parent, int style,
            org.eclipse.swt.widgets.List dtgSelectionList) {
        super(parent, style);
        this.dtgSelectionList = dtgSelectionList;
    }

    /**
     * Set the string in the dtgText field to dtgString.
     *
     * @param dtgString
     */
    public void setDtgText(String dtgString) {
        if (dtgText != null) {
            dtgText.setText(dtgString);
        }
    }

    /**
     * 
     * @return the String from the dtgText field
     */
    public String getDtgText() {
        return dtgText != null && !dtgText.isDisposed() ? dtgText.getText()
                : "";
    }

    /**
     * Add action buttons.
     */
    protected void addActionButtons() {
        // Add action buttons
        Button[] actionBtns = AtcfVizUtil.creatActionButtons(this,
                this.actionBtnNames, 10, 160);
        actionBtns[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean success;
                // Save Changes Button
                if (editData) {
                    boolean hasWindspeed = tabName
                            .equals(FixFormat.RADAR.getDescription())
                            || tabName.equals(
                                    FixFormat.SCATTEROMETER.getDescription())
                            || tabName.equals(
                                    FixFormat.MICROWAVE.getDescription());

                    success = saveEditedRecords(hasWindspeed);
                } else {
                    // Add New Record Button
                    success = saveNewRecords();
                }

                if (success) {
                    AtcfVizUtil.redrawFixes();
                }
            }
        });

        if (editData) {
            // Delete Record Button
            actionBtns[1].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TabUtility.deleteRecords(dtgText, storm, dtgSelectionList,
                            sandBoxID, selectedRecords, shell);
                }
            });

            // Flag Fix Button
            actionBtns[2].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TabUtility.flagRecord(selectedRecords, sandBoxID);
                }
            });
        }

        // If there are no records to edit, disable these buttons
        if (!findCurrentRec() && editData) {
            for (Button button : actionBtns) {
                button.setEnabled(false);
            }
        }
    }

    /**
     * Sets the fields if there is a currently selected record
     */
    protected void fillData() {
        if (findCurrentRec()) {
            if (selectedRecords != null && !selectedRecords.isEmpty()) {
                setFields((ArrayList<FDeckRecord>) selectedRecords);
            }
        } else {
            clearFields();
        }
    }

    /**
     * When in edit mode, and a new record is selected, fill in fields from the
     * record's data
     * 
     * @param fDeckRecord
     *            The record to get the data from
     */
    protected abstract void setFields(ArrayList<FDeckRecord> fDeckRecords);

    protected abstract boolean findCurrentRec();

    protected abstract boolean saveNewRecords();

    /**
     * Use a blank space instead of the default 999999
     * 
     * @param text
     * @param value
     */
    protected void setNumberField(Text text, float value) {
        text.setText(value == TabUtility.DEFAULT ? "" : String.valueOf(value));
    }

    /**
     * Saves edited records
     * 
     * @param hasWindspeed
     *            true if this tab has windspeed sections
     */
    protected boolean saveEditedRecords(boolean hasWindspeed) {

        for (FDeckRecord fRec : selectedRecords) {
            saveFieldsToRec(fRec);
        }

        if (hasWindspeed) {
            // This assumes there will be at most 3 records in selected records,
            // one for each wind speed section
            createRecordsByWindSpeeds((ArrayList<FDeckRecord>) selectedRecords);
        }

        return AtcfDataUtil.updateFDeckRecords(selectedRecords, sandBoxID,
                RecordEditType.MODIFY);
    }

    /**
     * Update FDeckRecords if we know the record is valid.
     * 
     * @param fDeckRecord
     * @param fDeckRecords
     * @return
     */
    protected boolean validRecordUpdate(List<FDeckRecord> fDeckRecords) {
        boolean ret = false;
        // Only call this if we know the record is valid
        if (!fDeckRecords.isEmpty() && AtcfDataUtil.updateFDeckRecords(
                fDeckRecords, sandBoxID, RecordEditType.NEW)) {
            FDeckRecord fDeckRecord = fDeckRecords.get(0);

            fixDataMap.put(new FDeckRecordKey(
                    TabUtility.formatDtg(fDeckRecord.getRefTime()),
                    fDeckRecord.getFixSite(), fDeckRecord.getFixType(),
                    fDeckRecord.getSatelliteType()), fDeckRecords);

            // Add records to AtcfResource for display
            AtcfSession.getInstance().getAtcfResourceData()
                    .getActiveAtcfProduct().getFDeckData().addAll(fDeckRecords);

            clearFields();

            ret = true;
        }

        return ret;
    }

    /**
     * Handles the cases for multiple FDeckRecords
     * 
     * @param selectedRecords
     * @return
     */
    protected List<FDeckRecord> createRecordsByWindSpeeds(
            List<FDeckRecord> selectedRecords) {
        return new ArrayList<>();
    }

    protected abstract void saveFieldsToRec(FDeckRecord fRec);

    /**
     * Reset all fields for a new entry
     */
    protected abstract void clearFields();

}
