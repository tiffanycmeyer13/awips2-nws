/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.viz.common.climate.listener.TextFocusListener;

/**
 * This class is a focus listener for Text fields that should show hours and
 * minutes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 18 MAY 2016  18384      amoore      Use appropriate missing values and ClimateTime functionality.
 * 19 SEP 2017  38124      amoore      Address review comments.
 * </pre>
 * 
 * @author amoore
 */
public class TimeSelectorFocusListener extends TextFocusListener {
    /**
     * True to only display and process for hour, false to use both hour and
     * minute.
     */
    private final boolean myHourOnly;

    /**
     * Text field to watch over and modify.
     */
    private final Text myTextField;

    /**
     * Constructor. Use both minute and hour.
     * 
     * @param iText
     *            text field to watch over.
     */
    public TimeSelectorFocusListener(Text iText) {
        this(iText, false);
    }

    /**
     * Constructor.
     * 
     * @param iHourOnly
     *            true to only display and process for hour, false to use both
     *            hour and minute.
     */
    public TimeSelectorFocusListener(Text iText, boolean iHourOnly) {
        super();

        myTextField = iText;

        myHourOnly = iHourOnly;

        // initialize text to missing data
        if (myHourOnly) {
            myTextField.setText(
                    Integer.toString(ParameterFormatClimate.MISSING_HOUR));
        } else {
            myTextField.setText(ClimateTime.MISSING_TIME_STRING);
        }
    }

    @Override
    public void focusLost(FocusEvent event) {
        // validate the time text
        String timeText = myTextField.getText();
        if (timeText.isEmpty()) {
            if (myHourOnly) {
                myTextField.setText(
                        Integer.toString(ParameterFormatClimate.MISSING_HOUR));
            } else {
                myTextField.setText(ClimateTime.MISSING_TIME_STRING);
            }
        } else if (!myHourOnly
                && !timeText.equals(ClimateTime.MISSING_TIME_STRING)) {
            // use both minute and hour
            // time is not empty or set to missing time string
            // validate
            ClimateTime newTime = new ClimateTime(timeText);

            int hour = newTime.getHour();
            int minute = newTime.getMin();

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                myTextField.setText(ClimateTime.MISSING_TIME_STRING);
            } else {
                // valid time, so ensure format
                myTextField.setText(newTime.toHourMinString());
            }
        } else if (myHourOnly && !timeText.equals(
                Integer.toString(ParameterFormatClimate.MISSING_HOUR))) {
            // use only hour
            // time is not empty or set to missing hour string
            // validate
            try {
                int hour = Integer.parseInt(timeText);

                if (hour < 0 || hour > 23) {
                    myTextField.setText(Integer
                            .toString(ParameterFormatClimate.MISSING_HOUR));
                } else {
                    // valid hour, so ensure format
                    myTextField.setText(Integer.toString(hour));
                }
            } catch (NumberFormatException exception) {
                myTextField.setText(
                        Integer.toString(ParameterFormatClimate.MISSING_HOUR));
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        // unimplemented
    }
}
