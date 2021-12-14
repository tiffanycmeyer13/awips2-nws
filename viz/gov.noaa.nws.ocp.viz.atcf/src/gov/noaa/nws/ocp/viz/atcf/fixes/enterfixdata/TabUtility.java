
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.advisory.AdvisoryBuilder;

/**
 * Class to hold utility methods used by multiple tabs in Enter/Edit Fix Record
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------- ---------- ----------- --------------------------
 * Oct 22, 2019 68738      dmanzella   Initial creation.
 * Apr 01, 2021 87786      wpaintsil   Revise UI.
 * Apr 27, 2021 91322      wpaintsil   Fix date-time formatting error.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
public class TabUtility {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AdvisoryBuilder.class);

    public static final int DEFAULT = (int) AbstractAtcfRecord.RMISSD;

    private static final DateTimeFormatter DTG_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmm");

    private static final Date DEFAULTDATE = TimeUtil.newGmtCalendar(1000, 1, 1)
            .getTime();

    /**
     * Date time group parse error message
     */
    public static final String DTG_ERROR_STRING = " - Failed to parse date time group.";

    /**
     * Default constructor
     */
    private TabUtility() {
    }

    /**
     * Saves the latitude
     * 
     * @param lat
     * @param northButton
     * @param southButton
     * @return
     */
    static final String saveLat(float lat, Button northButton,
            Button southButton) {
        if (lat == DEFAULT) {
            southButton.setSelection(false);
            northButton.setSelection(true);
            return "";
        }

        if (lat < 0) {
            southButton.setSelection(true);
            northButton.setSelection(false);
        } else {
            southButton.setSelection(false);
            northButton.setSelection(true);
        }
        return String.valueOf(Math.abs(lat));
    }

    /**
     * Saves the longitude
     * 
     * @param lon
     * @param eastButton
     * @param westButton
     * @return
     */
    static final String saveLon(float lon, Button eastButton,
            Button westButton) {
        if (lon == DEFAULT) {
            eastButton.setSelection(false);
            westButton.setSelection(true);
            return "";
        }

        eastButton.setSelection(lon > 0);
        westButton.setSelection(lon <= 0);

        return String.valueOf(Math.abs(lon));
    }

    /**
     * Sets the Center Intensity
     * 
     * @param c
     * @param i
     * @param r
     * @param p
     * @return
     */
    static final String setCenterIntensity(boolean c, boolean i, boolean r,
            boolean p) {
        StringBuilder centerInts = new StringBuilder();
        if (c) {
            centerInts.append('C');
        }

        if (i) {
            centerInts.append('I');
        }

        if (r) {
            centerInts.append('R');
        }

        if (p) {
            centerInts.append('P');
        }

        return centerInts.toString();
    }

    /**
     * Saves the Center Intensity
     * 
     * All dialogs have at least the first two buttons, only have to check the
     * latter two for null
     * 
     * @param center
     * @param centerFixChk
     * @param maxWindFixChk
     * @param windRadChk
     * @param minSfcChk
     */
    static final void saveCenterIntensity(String center, Button centerFixChk,
            Button maxWindFixChk, Button windRadChk, Button minSfcChk) {
        centerFixChk.setSelection(center.contains("C"));
        maxWindFixChk.setSelection(center.contains("I"));

        if (windRadChk != null) {
            windRadChk.setSelection(center.contains("R"));
        }

        if (minSfcChk != null) {
            minSfcChk.setSelection(center.contains("P"));
        }
    }

    /**
     * Mark records as Deleted in the sandbox
     *
     * @param dtgCombo
     * @param sandBoxID
     * @param selectedRecords
     * @param shell
     */
    static final void deleteRecords(Text dtgText, Storm storm,
            org.eclipse.swt.widgets.List dtgSelectionList, int sandBoxID,
            List<FDeckRecord> selectedRecords, Shell shell) {
        MessageBox confirmationDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmationDialog.setText("Alert");

        String confirmationString = "Are you sure you want to delete this record?";

        confirmationDialog.setMessage(confirmationString);
        int result = confirmationDialog.open();
        if (result == SWT.YES) {
            if (dtgSelectionList != null && !dtgSelectionList.isDisposed()
                    && dtgSelectionList.getItemCount() > 0) {
                // delete dtg from list
                for (int ii = 0; ii < dtgSelectionList.getItemCount(); ii++) {
                    if (dtgSelectionList.getItem(ii)
                            .contains(dtgText.getText())) {
                        dtgSelectionList.remove(ii);
                        if (dtgSelectionList.getItemCount() > 0) {
                            dtgSelectionList.select(0);
                        } else {
                            dtgText.setText(TabUtility.defaultDtgText(storm));
                        }
                        break;
                    }
                }
            }

            AtcfDataUtil.updateFDeckRecords(selectedRecords, sandBoxID,
                    RecordEditType.DELETE);
        }

    }

    /**
     * Get a default dtg string for the dtg text box based on the latest b-deck
     * data for the current storm.
     * 
     * @param storm
     * @return
     */
    public static String defaultDtgText(Storm storm) {
        String dtgTextString = ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Map<String, List<BDeckRecord>> currentBDeckRecords = AtcfDataUtil
                .getBDeckRecords(storm, false);

        List<String> bDeckDtgList = new ArrayList<>(
                currentBDeckRecords.keySet());
        if (!bDeckDtgList.isEmpty()) {
            dtgTextString = bDeckDtgList.get(bDeckDtgList.size() - 1);
            return dtgTextString.substring(0, 8);
        }

        return dtgTextString;
    }

    /**
     * Flags the selected record
     * 
     * @param selectedRecords
     * @param sandBoxID
     */
    static final void flagRecord(List<FDeckRecord> selectedRecords,
            int sandBoxID) {
        for (FDeckRecord fRec : selectedRecords) {
            fRec.setFlaggedIndicator("F");
        }

        AtcfDataUtil.updateFDeckRecords(selectedRecords, sandBoxID,
                RecordEditType.MODIFY);

    }

    /**
     * Sets Good, Fair, Poor settings based on confidence
     * 
     * @param conf
     * @param good
     * @param fair
     * @param poor
     */
    static final void setConfidence(String conf, Button good, Button fair,
            Button poor) {
        if ("1".equals(conf)) {
            good.setSelection(true);
            poor.setSelection(false);
            fair.setSelection(false);
        } else if ("2".equals(conf)) {
            good.setSelection(false);
            poor.setSelection(false);
            fair.setSelection(true);
        } else if ("3".equals(conf)) {
            good.setSelection(false);
            poor.setSelection(true);
            fair.setSelection(false);
        }
    }

    /**
     * Saves the confidence
     * 
     * @param good
     * @param fair
     * @param poor
     * @return confidence
     */
    static final String saveConfidence(Button good, Button fair, Button poor) {
        String conf = "";
        if (good.getSelection()) {
            conf = "1";
        } else if (fair.getSelection()) {
            conf = "2";
        } else if (poor.getSelection()) {
            conf = "3";
        }

        return conf;
    }

    /**
     * Checks the validity of the selected DTG
     * 
     * @param date
     * @return
     */
    static final Date checkDtg(String date) {
        try {
            return Date.from(LocalDateTime.parse(date, DTG_FORMAT)
                    .toInstant(ZoneOffset.UTC));
        } catch (RuntimeException e) {
            logger.warn("TabUtility" + DTG_ERROR_STRING, e);
            return DEFAULTDATE;
        }
    }

    /**
     * Saves the sensor type
     * 
     * @param visualButton
     * @param infraButton
     * @param microButton
     * @return
     */
    static final String saveSensorType(Button visualButton, Button infraButton,
            Button microButton) {
        String sensorType = "";

        if (visualButton.getSelection()) {
            sensorType += "V";
        }
        if (infraButton.getSelection()) {
            sensorType += "I";
        }
        if (microButton.getSelection()) {
            sensorType += "M";
        }
        return sensorType;
    }

    /**
     * Sets the Sensor type data
     * 
     * @param sensorType
     */
    static final void setSensorType(String sensorType, Button visualButton,
            Button infraButton, Button microButton) {
        visualButton.setSelection(sensorType.contains("V"));
        infraButton.setSelection(sensorType.contains("I"));
        microButton.setSelection(sensorType.contains("M"));
    }

    /**
     * Sets the tropics
     * 
     * @param trop
     * @param tropicalButton
     * @param subTropButton
     * @param extraTropButton
     */
    static final void setTropics(String trop, Button tropicalButton,
            Button subTropButton, Button extraTropButton) {
        if ("T".equals(trop)) {
            tropicalButton.setSelection(true);
            subTropButton.setSelection(false);
            extraTropButton.setSelection(false);
        } else if ("S".equals(trop)) {
            tropicalButton.setSelection(false);
            subTropButton.setSelection(true);
            extraTropButton.setSelection(false);
        } else {
            tropicalButton.setSelection(false);
            subTropButton.setSelection(false);
            extraTropButton.setSelection(true);
        }

    }

    /**
     * Saves the tropics
     * 
     * @param tropicalButton
     * @param subTropButton
     * @param extraTropButton
     * @return
     */
    static final String saveTropics(Button tropicalButton, Button subTropButton,
            Button extraTropButton) {
        String ret = "";
        if (tropicalButton.getSelection()) {
            ret = "T";
        } else if (subTropButton.getSelection()) {
            ret = "S";
        } else if (extraTropButton.getSelection()) {
            ret = "E";
        }
        return ret;
    }

    /**
     * Used for populating combos with passed in Doubles, by a passed in
     * increment
     * 
     * @param combo
     *            the combo to fill
     * @param start
     *            the number to start at
     * @param end
     *            the number to end at
     * @param increment
     *            the number to increment by
     */
    static final void populateNumericCombo(Combo combo, double start,
            double end, double increment) {
        for (double ii = start; ii <= end; ii += increment) {
            combo.add(String.valueOf(Math.round(ii * 10.0) / 10.0));
        }
    }

    /**
     * Used for populating combos with passed in Integers, by a passed in
     * increment.
     * 
     * @param combo
     *            the combo to fill
     * @param start
     *            the number to start at
     * @param end
     *            the number to end at
     * @param increment
     *            the number to increment by
     */
    static final void populateIntNumericCombo(Combo combo, int start, int end,
            int increment) {
        for (int ii = start; ii <= end; ii += increment) {
            combo.add(String.valueOf(ii));
        }
    }

    /**
     * Creates the error dialog text
     * 
     * @param dtgValid
     * @param dtgUnique
     * @param latValid
     * @param lonValid
     * @param centerValid
     * @param fixSiteValid
     * @param satValid
     * @param sensorValid
     * @param ciNumValid
     * @param sceneValid
     * @param wmoValid
     * @param sondValid
     * @param startDtgValid
     * @param endDtgValid
     * 
     * @return The error dialog text
     */
    static final String buildConfirmationText(boolean dtgValid,
            boolean dtgUnique, boolean latValid, boolean lonValid,
            boolean centerValid, boolean fixSiteValid, boolean satValid,
            boolean sensorValid, boolean ciNumValid, boolean sceneValid,
            boolean wmoValid, boolean sondValid) {
        String confirmationString = "";
        if (!dtgValid) {
            confirmationString = "The DTG is invalid, the correct format is YYYYMMDDHHMN";
        } else if (!dtgUnique) {
            confirmationString = "The combination of DTG, Fix Site, and Satellite Type is not unique";
        } else if (!latValid) {
            confirmationString = "The Latitude is invalid";
        } else if (!lonValid) {
            confirmationString = "The Longitude is invalid";
        } else if (!centerValid) {
            confirmationString = "You must select a Center Type";
        } else if (!satValid) {
            confirmationString = "You must enter a Satellite type";
        } else if (!fixSiteValid) {
            confirmationString = "You must enter at least 3 character for the Fix Site";
        } else if (!sensorValid) {
            confirmationString = "You must select at least one sensor type";
        } else if (!ciNumValid) {
            confirmationString = "You must select a CI Number";
        } else if (!sceneValid) {
            confirmationString = "You must select a Scene type";
        } else if (!wmoValid) {
            confirmationString = "You must enter at least 3 character for the WMO Identifier";
        } else if (!sondValid) {
            confirmationString = "You must select a Sonde Environment";
        }

        return confirmationString;
    }

    /**
     * Validates all of the necessary editable fields
     * 
     * @param lat
     * @param lon
     * @param north
     * @param south
     * @param east
     * @param west
     * @param centerValid
     * @param dtgUnique
     * @param currDate
     * @param shell
     * @param satValid
     * @param sensorValid
     * @param fixSiteValid
     * @param sondeValid
     * @param sceneValid
     * @param ciValid
     * @param wmoValid
     * 
     * @return
     */
    static final boolean isRecordValid(String lat, String lon, Button north,
            Button west, boolean centerValid, boolean dtgUnique, Date currDate,
            Shell shell, boolean satValid, boolean sensorValid,
            boolean fixSiteValid, boolean sondeValid, boolean sceneValid,
            boolean ciValid, boolean wmoValid) {
        boolean dtgValid = true;
        boolean latValid = true;
        boolean lonValid = true;

        if (!AtcfVizUtil.isValueValid(AtcfVizUtil.getStringAsFloat(lat,
                !north.getSelection(), false, 0))) {
            latValid = false;
        }

        if (!AtcfVizUtil.isValueValid(AtcfVizUtil.getStringAsFloat(lon,
                west.getSelection(), false, 0))) {
            lonValid = false;
        }

        if (TabUtility.DEFAULTDATE.equals(currDate)) {
            dtgValid = false;
        }

        if (dtgValid && latValid && lonValid && centerValid && dtgUnique
                && satValid && sensorValid && fixSiteValid && sondeValid) {

            return true;
        } else {
            MessageBox confirmationDialog = new MessageBox(shell,
                    SWT.ICON_QUESTION | SWT.OK);
            confirmationDialog.setText("Alert");
            confirmationDialog.setMessage(TabUtility.buildConfirmationText(
                    dtgValid, dtgUnique, latValid, lonValid, centerValid,
                    fixSiteValid, satValid, sensorValid, ciValid, sceneValid,
                    wmoValid, sondeValid));
            confirmationDialog.open();
            return false;
        }
    }

    public static String formatDtg(Date date) {
        return DTG_FORMAT.format(date.toInstant().atOffset(ZoneOffset.UTC));
    }

    public static Date parseDtg(String text) {
        return Date.from(LocalDateTime.parse(text, DTG_FORMAT)
                .toInstant(ZoneOffset.UTC));
    }

}
