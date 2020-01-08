/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * This class validates the user-typing input. A valid input is a decimal
 * number, and in range of [minimum, maximum] or 'T' for trace or equal to
 * default. Trace value is assumed to be {@link ParameterFormatClimate#TRACE},
 * with immediate replacement with trace symbol. Trace symbol is assumed to be
 * {@link ParameterFormatClimate#TRACE_SYMBOL}.
 * 
 * Invalid input will change to default at lost focus. See
 * {@link TextDefaultValueFocusListener}.
 * 
 * This class intents to validates the user-typing inputs, programmatic use of
 * #setText() to set a invalid value could not be verified. Example: minimum=0,
 * maximum=100, default=9999. setText(120) will display 120.00. Therefore, extra
 * care should be taken to ensure valid values are set using #setText.
 * 
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/21/2016  20639    wkwock      Initial creation
 * 10/25/2016  20639    wkwock      Change background color on invalid input.
 * 11/02/2016  20635    wkwock      Add rounding up for programmatic inputs.
 * 27 DEC 2016 22450    amoore      Handle negative precision. Double with T should extend Double.
 * 15 JUN 2017 35187    amoore      Add check against Trace value (-1.0), replacing with T.
 * </pre>
 *
 * @author wkwock
 */

public final class TextDoubleWithTListener extends TextDoubleListener {
    /**
     * Constructor. Assume 0.0 is the minimum and 2 decimal places.
     * 
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleWithTListener(BigDecimal maxDefaultVal) {
        super(maxDefaultVal);
    }

    /**
     * Constructor. Assume 0.0 is the minimum and 2 decimal places.
     * 
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleWithTListener(Double maxDefaultVal) {
        super(maxDefaultVal);
    }

    /**
     * Constructor. Assume 2 decimal places.
     * 
     * @param minVal
     *            minimum value.
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleWithTListener(BigDecimal minVal,
            BigDecimal maxDefaultVal) {
        super(minVal, maxDefaultVal);
    }

    /**
     * Constructor. Assume 2 decimal places.
     * 
     * @param minVal
     *            minimum value.
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleWithTListener(Double minVal, Double maxDefaultVal) {
        super(minVal, maxDefaultVal);
    }

    /**
     * Constructor. Assume 2 decimal places.
     * 
     * @param minVal
     *            minimum value.
     * @param maxVal
     *            maximum value.
     * @param defaultVal
     *            default value.
     */
    public TextDoubleWithTListener(BigDecimal minVal, BigDecimal maxVal,
            BigDecimal defaultVal) {
        super(minVal, maxVal, defaultVal);
    }

    /**
     * Constructor. Assume 2 decimal places.
     * 
     * @param minVal
     *            minimum value.
     * @param maxVal
     *            maximum value.
     * @param defaultVal
     *            default value.
     */
    public TextDoubleWithTListener(Double minVal, Double maxVal,
            Double defaultVal) {
        super(minVal, maxVal, defaultVal);
    }

    /**
     * Constructor.
     * 
     * @param minVal
     *            minimum value.
     * @param maxVal
     *            maximum value.
     * @param defaultVal
     *            default value.
     * @param iDecimalPlaces
     *            number of decimal places.
     */
    public TextDoubleWithTListener(BigDecimal minVal, BigDecimal maxVal,
            BigDecimal defaultVal, int iDecimalPlaces) {
        super(minVal, maxVal, defaultVal, iDecimalPlaces);
    }

    /**
     * Constructor.
     * 
     * @param minVal
     *            minimum value.
     * @param maxVal
     *            maximum value.
     * @param defaultVal
     *            default value.
     * @param iDecimalPlaces
     *            number of decimal places.
     */
    public TextDoubleWithTListener(Double minVal, Double maxVal,
            Double defaultVal, int iDecimalPlaces) {
        super(minVal, maxVal, defaultVal, iDecimalPlaces);
    }

    @Override
    public void verifyText(Event event) {
        Text textField = (Text) event.widget;

        String originalText = textField.getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);

        /*
         * For programmatic input, check if text field is trace value, otherwise
         * check precision.
         */
        if (!((Control) event.widget).isFocusControl()) {
            if (newText.equals(String.valueOf(ParameterFormatClimate.TRACE))) {
                event.doit = false;
                textField.setText(ParameterFormatClimate.TRACE_SYMBOL);
            } else {
                checkPrecision(event, textField, newText);
            }
            return;
        }

        if (!newText.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
            /*
             * Verify user input if not exactly the trace symbol. No rounding
             * for user input.
             */
            if (newText.equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                    || newText.equals(
                            String.valueOf(ParameterFormatClimate.TRACE))) {
                // Correct symbol casing
                // Change trace value to explicitly be trace symbol
                event.doit = false;
                textField.setText(ParameterFormatClimate.TRACE_SYMBOL);
            } else if (!newText.isEmpty() && !newText.equals("-")) {
                checkDoubleBounds(event, textField, newText);
            } else {
                setBackground(textField, true);
            }
        } else {
            setBackground(textField, true);
        }
    }

    @Override
    protected boolean isValid(String text) {
        if (outOfBounds(text)
                || ((myNumDecimal > 0 && !text.matches(DOUBLE_REGEX))
                        || (myNumDecimal <= 0
                                && !text.matches(TextIntListener.INT_REGEX)))
                        && !text.equalsIgnoreCase(
                                ParameterFormatClimate.TRACE_SYMBOL)) {
            return false;
        }
        return true;

    }
}