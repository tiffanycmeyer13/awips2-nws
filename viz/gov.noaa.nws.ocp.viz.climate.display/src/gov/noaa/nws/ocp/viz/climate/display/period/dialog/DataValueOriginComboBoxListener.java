/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.DataValueOrigin;

/**
 * This class handles selection changes to the {@link DataValueOrigin} combo
 * boxes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 10 AUG 2016  20414      amoore      Initial creation
 * 16 JUN 2017  35185      amoore      Comment fixes.
 * 20 NOV 2017  41128      amoore      Moved to separate file.
 * </pre>
 * 
 * @author amoore
 */
public abstract class DataValueOriginComboBoxListener
        implements ISelectionChangedListener {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DataValueOriginComboBoxListener.class);

    /**
     * The combo box to load data for.
     */
    private final ComboViewer myComboBox;

    /**
     * Previous selection. Stored to prevent redundant loading.
     */
    private DataValueOrigin myPreviousSelection = null;

    /**
     * The parent dialog.
     */
    private final DisplayStationPeriodDialog myParent;

    /**
     * Constructor.
     * 
     * @param iComboBox
     *            combo box to load data for.
     * @param parent
     */
    public DataValueOriginComboBoxListener(ComboViewer iComboBox,
            DisplayStationPeriodDialog parent) {
        myComboBox = iComboBox;
        myParent = parent;
    }

    /**
     * @return the ComboBox
     */
    public ComboViewer getComboBox() {
        return myComboBox;
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (myParent.myCurrStation != null) {
            DataValueOrigin newOrigin = (DataValueOrigin) getComboBox()
                    .getStructuredSelection().getFirstElement();

            if (!myParent.myLoadingData
                    && DataValueOrigin.OTHER.equals(myPreviousSelection)) {
                /*
                 * we are not currently loading data (programmatic change of
                 * combo selection), we are moving away from "Other" (and thus
                 * the value should be saved): then save the "Other" data for
                 * fields associated with this combo box.
                 */
                saveOtherData();
            }

            // only do work if the selection has changed or we are loading
            // data (selection may not have changed, but new data is
            // available)
            if (myParent.myLoadingData || (myPreviousSelection == null
                    || !myPreviousSelection.equals(newOrigin))) {
                if (!myParent.myLoadingData) {
                    /*
                     * if choice is by user, not from loading of data, then
                     * consider this an unsaved change (user is selecting
                     * possible values to save).
                     */
                    myParent.getChangeListener().setChangesUnsaved(true);
                }

                myPreviousSelection = newOrigin;

                PeriodData data;
                switch (newOrigin) {
                case MONTHLY_SUMMARY_MESSAGE:
                    // display MSM data
                    data = myParent.myMSMPeriodDataByStation
                            .get(myParent.myCurrStation.getInformId());
                    break;
                case DAILY_DATABASE:
                    // display Daily DB data
                    data = myParent.myOriginalDataMap
                            .get(myParent.myCurrStation.getInformId())
                            .getData();
                    break;
                case OTHER:
                    if (myParent.myLoadingData || !isCurrentlyEditing()) {
                        /*
                         * user not currently editing the field(s) (based on
                         * focus) or data is being loaded. These two cases
                         * should be synonymous, but both are placed just in
                         * case; display Other data
                         */
                        data = myParent.myOtherPeriodDataByStation
                                .get(myParent.myCurrStation.getInformId());
                    } else {
                        // user is currently editing the field(s); save
                        // Other data
                        data = null;
                        saveOtherData();
                    }
                    break;
                default:
                    data = null;
                    break;
                }

                if (data != null) {
                    loadFieldData(data);
                } else if (myParent.myLoadingData || !isCurrentlyEditing()) {
                    logger.warn("Could not retrieve data.");
                }
            }
        }
    }

    /**
     * @return "Other" period data for the current station. Creates missing
     *         value data and puts to the "Other" map if no data currently
     *         exists in the map for the station.
     */
    protected PeriodData getOtherPeriodData() {
        PeriodData dataToSave;
        if (myParent.myOtherPeriodDataByStation
                .get(myParent.myCurrStation.getInformId()) == null) {
            myParent.myOtherPeriodDataByStation.put(
                    myParent.myCurrStation.getInformId(),
                    PeriodData.getMissingPeriodData());
        }
        dataToSave = myParent.myOtherPeriodDataByStation
                .get(myParent.myCurrStation.getInformId());
        return dataToSave;
    }

    /**
     * Load data into the fields associated with the combo box of this listener.
     * 
     * @param iData
     */
    protected abstract void loadFieldData(PeriodData iData);

    /**
     * @return true if the fields associated with the combo box of this listener
     *         are currently being edited.
     */
    protected abstract boolean isCurrentlyEditing();

    /**
     * Save the data for the fields associated with the combo box into "Other"
     * for this station.
     */
    protected abstract void saveOtherData();
}