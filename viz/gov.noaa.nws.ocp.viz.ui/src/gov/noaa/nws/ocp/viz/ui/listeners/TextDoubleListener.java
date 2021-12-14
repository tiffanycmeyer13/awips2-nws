/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.listeners;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * This class validates the user-typing input. A valid input is a decimal
 * number, and in range of [minimum, maximum] or equal to default.
 *
 * Invalid input will change to default at lost focus. See
 * {@link TextDefaultValueFocusListener}.
 *
 * This class intends to validate the user-typing inputs, programmatic use of
 * #setText() to set an invalid value could not be verified. Example: minimum=0,
 * maximum=100, default=9999. setText(120) will display 120.00. Therefore, extra
 * care should be taken to ensure valid values are set using #setText.
 *
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#  Engineer    Description
 * -----------  -------- ----------- --------------------------
 * Aug 14, 2019 65562    jwu         Modified from Climate plugin.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class TextDoubleListener extends AbstractTextNumberListener {
    /**
     * Prefix for possible negative double value with some defined number of
     * trailing zeros; no decimal place or digits.
     */
    private static final String DOUBLE_REGEX_NEG_PRECISION_PREFIX = "^-?\\d+0";

    /**
     * Prefix for possibly negative double value with some defined number of
     * decimal digits.
     */
    private static final String DOUBLE_REGEX_POS_PRECISION_PREFIX = "^-?\\d+\\.?\\d";

    /**
     * double regex
     */
    public static final String DOUBLE_REGEX = "^-?\\d+\\.?\\d*";

    /**
     * double regex with precision
     */
    protected String doubleRegexWithPrecision;

    /**
     * Minimum value.
     */
    private final double minValue;

    /**
     * Maximum value.
     */
    private final double maxValue;

    /**
     * Default value.
     */
    private final double defaultValue;

    /**
     * Maximum number of characters allowed in input.
     */
    private final int textLimit;

    /**
     * Number of decimal places.
     */
    private final int numDecimal;

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
    public TextDoubleListener(double minVal, double maxVal, double defaultVal,
            int iDecimalPlaces, int limit) {
        minValue = Math.min(minVal, maxVal);
        maxValue = Math.max(minVal, maxVal);
        defaultValue = defaultVal;
        textLimit = limit;
        numDecimal = iDecimalPlaces;
        buildDoubleRegex();
    }

    /**
     * @return the minValue
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @return the defaultValue
     */
    public double getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the textLimit
     */
    public int getTextLimit() {
        return textLimit;
    }

    /**
     * @return the numDecimal
     */
    public int getNumDecimal() {
        return numDecimal;
    }

    /**
     * Check if negative is allowed.
     *
     * @return true/false
     */
    private boolean isNonNegative() {
        return (minValue >= 0);
    }

    /**
     * Verify text input
     *
     * @param event
     */
    @Override
    public void verifyText(Event event) {
        Text textField = (Text) event.widget;

        String originalText = textField.getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);

        // Check if negative is allowed.
        if (isNonNegative() && newText.contains("-")) {
            event.doit = false;
        }

        // Check against text limit
        if (!newText.isEmpty()) {
            int limit = getTextLimit();
            if (limit > 0 && newText.length() > limit) {
                event.doit = false;
            }
        }

        /*
         * Rounding up programmatic input and doing nothing else.
         */
        if (!((Control) event.widget).isFocusControl()) {
            checkPrecision(event, textField, newText);
            return;
        }

        // Verify user input. No rounding for user input.
        if (!newText.isEmpty() && !"-".equals(newText)) {
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
        if (newText.matches(DOUBLE_REGEX) && !(newText
                .matches(doubleRegexWithPrecision)
                || (Double.parseDouble(newText) == getDefaultValue()))) {
            /*
             * Valid double value with non-matching precision and is not exactly
             * the default value.
             */
            event.doit = false;
            if (numDecimal > 0) {
                /*
                 * Valid double value, but precision is off and precision is
                 * past decimal place.
                 */
                String fmt = "%1." + numDecimal + "f";
                textField.setText(
                        String.format(fmt, Double.parseDouble(newText)));
            } else {
                /*
                 * Valid double value, but precision is off and precision is
                 * before decimal place (some digits before decimal place should
                 * be 0's, and no digits after decimal place).
                 */
                int newValue = (int) (((int) (Double.parseDouble(newText)
                        / Math.pow(10, -1 * (double) numDecimal)))
                        * Math.pow(10, -1 * (double) numDecimal));
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
        try {
            double newVal = Double.parseDouble(newText);

            // Check if it is outside of valid range
            boolean isNormal = !(newVal < getMinValue()
                    || newVal > getMaxValue())
                    && !floatingEquals(newVal, getDefaultValue());
            setBackground(textField, isNormal);
        } catch (NumberFormatException ne) {
            // it's not a number
            event.doit = false;
        }
    }

    @Override
    public void focusLost(Event e) {
        Text textField = (Text) e.widget;

        setBackground(textField, true);

        if ((numDecimal > 0 && !textField.getText().matches(DOUBLE_REGEX))
                || (numDecimal <= 0 && !textField.getText()
                        .matches(TextIntListener.INT_REGEX))) {
            /*
             * Either we expect high precision and the text is not any kind of
             * double, or we expect low precision and the text is not any kind
             * of integer; so set text to default value.
             */
            setToDefaultText(textField);
        }
    }

    /**
     * Text field text does not match expected format, so set text to default
     * value.
     *
     * @param textField
     */
    protected final void setToDefaultText(Text textField) {
        if (numDecimal > 0) {
            String fmt = "%1." + numDecimal + "f";
            textField.setText(String.format(fmt, getDefaultValue()));
        } else {
            textField.setText(String.valueOf(getDefaultValue()));
        }
    }

    /**
     * @param ndecimal
     *            the decimal precision for this listener.
     */
    private void buildDoubleRegex() {
        doubleRegexWithPrecision = String.format("%s{%d}",
                numDecimal > 0 ? DOUBLE_REGEX_POS_PRECISION_PREFIX
                        : DOUBLE_REGEX_NEG_PRECISION_PREFIX,
                Math.abs(numDecimal));
    }

}