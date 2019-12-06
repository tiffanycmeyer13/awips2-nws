/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.common.climate.listener.AbstractTextNumberListener;

/**
 * This class validates the user-typing input. A valid input is a decimal
 * number, and in range of [minimum, maximum] or equal to default.
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
 * 08/12/2016  21198    wkwock      Initial creation
 * 23 AUG 2016 20414    amoore      Add Big Decimal to mitigate rounding and
 *                                  conversion issues. Add #decimal spaces field.
 * 10/25/2016  20639    wkwock      Change background color on invalid input.
 * 11/02/2016  20635    wkwock      Add rounding up for programmatic inputs.
 * 27 DEC 2016 22450    amoore      Handle negative precision. Refactoring.
 * 04 JAN 2016 22134    amoore      Add getter for precision.
 * 16 JUN 2017 35181    amoore      Fix auto-rounding logic when value equal
 *                                  to default value (too relaxed before).
 * 23 OCT 2019 DR21622  wpaintsil   Ensure invalid text is replaced in focusLost().
 * </pre>
 *
 * @author wkwock
 */
public class TextDoubleListener extends AbstractTextNumberListener {
    /**
     * Suffix for possible negative double value with some defined number of
     * trailing zeros or some defined number of decimal digits.
     */
    private static final String DOUBLE_REGEX_PRECISION_SUFFIX = "}";

    /**
     * Prefix for possible negative double value with some defined number of
     * trailing zeros; no decimal place or digits.
     */
    private static final String DOUBLE_REGEX_NEG_PRECISION_PREFIX = "^-?\\d+0{";

    /**
     * Prefix for possibly negative double value with some defined number of
     * decimal digits.
     */
    private static final String DOUBLE_REGEX_POS_PRECISION_PREFIX = "^-?\\d+\\.?\\d{";

    /**
     * double regex
     */
    public final static String DOUBLE_REGEX = "^-?\\d+\\.?\\d*";

    /**
     * double regex with precision
     */
    protected final String doubleRegexWithPrecision;

    /**
     * Default number of decimal places.
     */
    private static final int DEFAULT_NUM_DECIMAL = 2;

    /**
     * Number of decimal places.
     */
    protected final int myNumDecimal;

    /**
     * Constructor. Assume 0.0 is the minimum and 2 decimal places.
     * 
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleListener(BigDecimal maxDefaultVal) {
        this(new BigDecimal(0), maxDefaultVal);
    }

    /**
     * Constructor. Assume 0.0 is the minimum and 2 decimal places.
     * 
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleListener(Double maxDefaultVal) {
        this(0.0, maxDefaultVal);
    }

    /**
     * Constructor. Assume 2 decimal places.
     * 
     * @param minVal
     *            minimum value.
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleListener(BigDecimal minVal, BigDecimal maxDefaultVal) {
        this(minVal, maxDefaultVal, maxDefaultVal, DEFAULT_NUM_DECIMAL);
    }

    /**
     * Constructor. Assume 2 decimal places.
     * 
     * @param minVal
     *            minimum value.
     * @param maxDefaultVal
     *            maximum value, also default value.
     */
    public TextDoubleListener(Double minVal, Double maxDefaultVal) {
        this(minVal, maxDefaultVal, maxDefaultVal, DEFAULT_NUM_DECIMAL);
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
    public TextDoubleListener(BigDecimal minVal, BigDecimal maxVal,
            BigDecimal defaultVal) {
        this(minVal, maxVal, defaultVal, DEFAULT_NUM_DECIMAL);
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
    public TextDoubleListener(Double minVal, Double maxVal, Double defaultVal) {
        this(minVal, maxVal, defaultVal, DEFAULT_NUM_DECIMAL);
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
    public TextDoubleListener(BigDecimal minVal, BigDecimal maxVal,
            BigDecimal defaultVal, int iDecimalPlaces) {
        super(minVal, maxVal, defaultVal);
        myNumDecimal = iDecimalPlaces;

        // Build double regex with precision
        if (myNumDecimal > 0) {
            doubleRegexWithPrecision = DOUBLE_REGEX_POS_PRECISION_PREFIX
                    + myNumDecimal + DOUBLE_REGEX_PRECISION_SUFFIX;
        } else {
            doubleRegexWithPrecision = DOUBLE_REGEX_NEG_PRECISION_PREFIX
                    + (-1 * myNumDecimal) + DOUBLE_REGEX_PRECISION_SUFFIX;
        }
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
    public TextDoubleListener(Double minVal, Double maxVal, Double defaultVal,
            int iDecimalPlaces) {
        super(minVal, maxVal, defaultVal);
        myNumDecimal = iDecimalPlaces;

        // Build double regex with precision
        if (myNumDecimal > 0) {
            doubleRegexWithPrecision = DOUBLE_REGEX_POS_PRECISION_PREFIX
                    + myNumDecimal + DOUBLE_REGEX_PRECISION_SUFFIX;
        } else {
            doubleRegexWithPrecision = DOUBLE_REGEX_NEG_PRECISION_PREFIX
                    + (-1 * myNumDecimal) + DOUBLE_REGEX_PRECISION_SUFFIX;
        }
    }

    @Override
    public void verifyText(Event event) {
        Text textField = (Text) event.widget;

        String originalText = textField.getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);

        /*
         * Rounding up programmatic input and doing nothing else.
         */
        if (!((Control) event.widget).isFocusControl()) {
            checkPrecision(event, textField, newText);
            return;
        }

        // Verify user input. No rounding for user input.
        if (!newText.isEmpty() && !newText.equals("-")) {
            checkDoubleBounds(event, textField, newText);
        } else {
            setBackground(textField, true);
        }
    }

    /**
     * Check against assigned precision. Set event flag and text field text as
     * appropriate.
     * 
     * @param event
     * @param textField
     * @param newText
     */
    protected final void checkPrecision(Event event, Text textField,
            String newText) {
        if (newText.matches(DOUBLE_REGEX)
                && !(newText.matches(doubleRegexWithPrecision) || (Double
                        .parseDouble(newText) == getDefault().doubleValue()))) {
            /*
             * Valid double value with non-matching precision and is not exactly
             * the default value.
             */
            event.doit = false;
            if (myNumDecimal > 0) {
                /*
                 * Valid double value, but precision is off and precision is
                 * past decimal place.
                 */
                textField.setText(String.format("%1." + myNumDecimal + "f",
                        Double.parseDouble(newText)));
            } else {
                /*
                 * Valid double value, but precision is off and precision is
                 * before decimal place (some digits before decimal place should
                 * be 0's, and no digits after decimal place).
                 */
                int newValue = (int) (((int) (Double.parseDouble(newText)
                        / Math.pow(10, -1 * myNumDecimal)))
                        * Math.pow(10, -1 * myNumDecimal));
                textField.setText(String.valueOf(newValue));
            }
        }
    }

    /**
     * Check against assigned bounds. Set background color, event flag, and text
     * field text as appropriate.
     * 
     * @param event
     * @param textField
     * @param newText
     */
    protected final void checkDoubleBounds(Event event, Text textField,
            String newText) {
        if (newText.matches(DOUBLE_REGEX)) {
            if (outOfBounds(newText)) {
                // outside of valid range
                setBackground(textField, false);
            } else {
                setBackground(textField, true);
            }
        } else {
            // it's not a number
            event.doit = false;
        }
    }

    /**
     * @param text
     * @return false if the number taken from a text field is out of bounds
     */
    protected boolean outOfBounds(String text) {
        try {
            double newVal = Double.parseDouble(text);
            if ((newVal < getMin().doubleValue()
                    || newVal > getMax().doubleValue())
                    && !ClimateUtilities.floatingEquals(newVal,
                            getDefault().doubleValue())) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            if (text.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                return false;
            }
            return true;
        }
    }

    @Override
    protected boolean isValid(String text) {
        if (outOfBounds(text)
                || (myNumDecimal > 0 && !text.matches(DOUBLE_REGEX))
                || (myNumDecimal <= 0
                        && !text.matches(TextIntListener.INT_REGEX))) {
            return false;
        }

        return true;
    }

    /**
     * Text field text does not match expected format, so set text to default
     * value.
     * 
     * @param textField
     */
    @Override
    protected final void setToDefaultText(Text textField) {
        if (myNumDecimal > 0) {
            textField.setText(String.format("%1." + myNumDecimal + "f",
                    getDefault().doubleValue()));
        } else {
            textField.setText(String.valueOf(getDefault().intValue()));
        }
    }

    /**
     * @return the decimal precision for this listener.
     */
    public final int getNumDecimal() {
        return myNumDecimal;
    }

}