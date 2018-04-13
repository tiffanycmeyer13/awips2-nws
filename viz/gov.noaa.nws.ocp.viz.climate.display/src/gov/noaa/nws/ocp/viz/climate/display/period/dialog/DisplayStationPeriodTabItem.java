/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog;

import java.text.ParseException;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;

/**
 * This class is a parent to all tabs of the Period Display dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20 NOV 2017  41128      amoore      Initial creation.
 * </pre>
 * 
 * @author amoore
 */
public abstract class DisplayStationPeriodTabItem {

    /**
     * Collection of listeners.
     */
    protected final ClimateTextListeners myDisplayListeners = new ClimateTextListeners();

    /**
     * Logger for this DAO and all children.
     */
    protected final IUFStatusHandler logger = UFStatus.getHandler(getClass());

    /**
     * The parent dialog.
     */
    protected final DisplayStationPeriodDialog myPeriodDialog;

    /**
     * The wrapped tab item.
     */
    protected final TabItem myTabItem;

    /**
     * 
     * @param parent
     * @param style
     * @param periodDialog
     */
    public DisplayStationPeriodTabItem(TabFolder parent, int style,
            DisplayStationPeriodDialog periodDialog) {
        myTabItem = new TabItem(parent, style);

        this.myPeriodDialog = periodDialog;
    }

    /**
     * Save values from fields to given object.
     * 
     * @param dataToSave
     * @throws VizException
     * @throws NumberFormatException
     * @throws ParseException
     */
    protected abstract void saveValues(PeriodData dataToSave)
            throws VizException, NumberFormatException, ParseException;

    /**
     * Load the given saved data into the view.
     * 
     * @param iSavedPeriodData
     *            the saved data.
     * @param msmPeriodData
     * @param dailyPeriodData
     * @return true if there was a mismatch value detected
     */
    protected abstract boolean loadSavedData(PeriodData iSavedPeriodData,
            PeriodData msmPeriodData, PeriodData dailyPeriodData);

    /**
     * Display daily build data (Daily DB) from the Climate Creator data map.
     * Compare with given monthly ASOS data.
     * 
     * @param iMonthlyAsosData
     * @param iDailyBuildData
     * @return true if there was a mismatch value detected
     */
    protected abstract boolean displayDailyBuildData(
            PeriodData iMonthlyAsosData, PeriodData iDailyBuildData);

    /**
     * Display monthly ASOS data.
     */
    protected abstract void displayMonthlyASOSData();

    /**
     * Clear all text fields to missing values.
     */
    protected abstract void clearValues();

    /**
     * @param data1
     * @param data2
     * @param dates1
     * @param dates2
     * @param numDatesToCheck
     * @return true if the data for the two periods are equal (same value and
     *         dates)
     */
    protected static boolean isPeriodDataEqualForInt(int data1, int data2,
            java.util.List<?> dates1, java.util.List<?> dates2,
            int numDatesToCheck) {
        return (data1 == data2) && (ClimateUtilities
                .isListsEqualUpToCapacity(numDatesToCheck, dates1, dates2));
    }

    /**
     * @param data1
     * @param data2
     * @param dates1
     * @param dates2
     * @param numDatesToCheck
     * @return true if the data for the two periods are equal (same value and
     *         dates)
     */
    protected static boolean isPeriodDataEqualForFloat(float data1, float data2,
            java.util.List<?> dates1, java.util.List<?> dates2,
            int numDatesToCheck) {
        return ClimateUtilities.floatingEquals(data1, data2)
                && (ClimateUtilities.isListsEqualUpToCapacity(numDatesToCheck,
                        dates1, dates2));
    }
}
