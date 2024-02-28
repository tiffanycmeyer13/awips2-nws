/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.listeners;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * This class validates the user input for integers in a Text Widget. A valid
 * input is a whole number, and in range of [minimum, maximum] or equal to
 * default.
 *
 * Invalid input will change to default at lost focus. See
 * {@link TextDefaultValueFocusListener}.
 *
 * This class intends to validate the user-typing inputs, programmatic use of
 * #setText() to set an invalid value could not be verified. Example: minimum=0,
 * maximum=100, default=9999. setText(120) will display 120. Therefore, extra
 * care should be taken to ensure valid values are set using #setText.
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
public class TextIntListener extends AbstractTextNumberListener {

    /**
     * Minimum value.
     */
    private final int minValue;

    /**
     * Maximum value.
     */
    private final int maxValue;

    /**
     * Default value.
     */
    private final int defaultValue;

    /**
     * Maximum number of characters allowed in input.
     */
    private final int textLimit;

    /**
     * Constructor.
     *
     * @param iMin
     *            minimum value.
     * @param iMax
     *            maximum value.
     * @param iDefault
     *            default value.
     * @param iLimit
     *            text limit.
     */
    public TextIntListener(int iMin, int iMax, int iDefault, int iLimit) {
        minValue = Math.min(iMin, iMax);
        maxValue = Math.max(iMin, iMax);
        defaultValue = iDefault;
        textLimit = iLimit;
    }

    /**
     * @return the minValue
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * @return the maxValue
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * @return the defaultValue
     */
    public int getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the textLimit
     */
    public int getTextLimit() {
        return textLimit;
    }

    /**
     * Check if negative is allowed.
     *
     * @return true/false
     */
    protected boolean isNonNegative() {
        return (minValue >= 0);
    }

    @Override
    public void verifyText(Event event) {
        // verify user input only
        if (!((Control) event.widget).isFocusControl()) {
            return;
        }

        Text text = (Text) event.widget;

        // This is what's in the text field
        String originalText = text.getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);

        // Check if negative is allowed.
        if (isNonNegative() && newText.contains("-")) {
            event.doit = false;
        }

        if (!newText.isEmpty() && !"-".equals(newText)) {

            // only parse if text is non-empty, and not negative sign
            int limit = getTextLimit();
            if (limit > 0 && newText.length() > limit) {
                event.doit = false;
            } else if (newText.matches(INT_REGEX)) {
                int newInt = Integer.parseInt(newText);
                // Check if the value is outside of valid range
                boolean isOutofRange = (newInt < getMinValue()
                        || newInt > getMaxValue())
                        && newInt != getDefaultValue();
                setBackground(text, !isOutofRange);
            } else {
                // it's not a whole number
                event.doit = false;
            }
        } else {
            setBackground(text, true);
        }
    }

    @Override
    public void focusLost(Event e) {
        Text textField = (Text) e.widget;

        setBackground(textField, true);

        if (!textField.getText().matches(INT_REGEX)) {
            textField.setText(String.valueOf(getDefaultValue()));
        }
    }

}
