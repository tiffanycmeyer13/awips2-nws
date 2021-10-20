/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.listeners;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * This class validates the user input for integers in a CCombo widget. A valid
 * input is a whole number, and in range of [minimum, maximum] or equal to
 * default. The user could define a range of [minimum, maximum] and it will be
 * combined with the range from the CCombo's items, if any (and assuming the
 * items in CCombo are ordered).
 *
 * Invalid input will change to default at lost focus. See
 * {@link TextDefaultValueFocusListener}.
 *
 * This class intents to validates the user-typing inputs, programmatic use of
 * #setText() to set a invalid value could not be verified. Example: minimum=0,
 * maximum=100, default=9999. setText(120) will display 120. Therefore, extra
 * care should be taken to ensure valid values are set using #setText.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#  Engineer    Description
 * -----------  -------- ----------- --------------------------
 * Aug 14, 2019 65562    jwu         Initial coding.
 * Jul 16, 2021 93152    jwu         Correct logic for valid input.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class CComboIntVerifyListener extends TextIntListener
        implements VerifyListener {

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
    public CComboIntVerifyListener(int iMin, int iMax, int iDefault,
            int iLimit) {
        super(iMin, iMax, iDefault, iLimit);
    }

    @Override
    public void focusLost(Event e) {
        CCombo textField = (CCombo) e.widget;

        setBackground(textField, true);

        if (!textField.getText().matches(INT_REGEX)) {
            textField.setText(String.valueOf(getDefaultValue()));
        }
    }

    @Override
    public void verifyText(VerifyEvent event) {

        if (!((Control) event.widget).isFocusControl()) {
            return;
        }

        CCombo cmb = (CCombo) event.widget;

        // Find low/high limits from the combo items
        int[] limits = findLimits(cmb.getItems());

        // This is what's in the text field
        String originalText = cmb.getText();
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

                // Check if it is outside of valid range
                boolean isOutOfRange = (newInt < limits[0]
                        || newInt > limits[1]) && newInt != getDefaultValue();
                setBackground(cmb, !isOutOfRange);
            } else {
                // it's not a whole number
                event.doit = false;
            }
        } else {
            setBackground(cmb, true);
        }
    }

    @Override
    public void verifyText(Event event) {
        // Only verify in VerifyEvent.
    }

    /*
     * Find limits from an array of items.
     *
     * @param items An array of items.
     *
     * @return int[2] The first one is the lower while the second one is the
     * higher limit.
     */
    private int[] findLimits(String[] items) {
        int lowLimit = getMinValue();
        int highLimit = getMaxValue();
        if (items != null && items.length > 0) {

            int newLow = lowLimit;
            if (items[0].matches(INT_REGEX)) {
                newLow = Integer.parseInt(items[0]);
            }

            int newHigh = highLimit;
            if (items[items.length - 1].matches(INT_REGEX)) {
                newHigh = Integer.parseInt(items[items.length - 1]);
            }

            if (newLow > newHigh) {
                int temp = newLow;
                newLow = newHigh;
                newHigh = temp;
            }

            lowLimit = Math.max(lowLimit, newLow);
            highLimit = Math.min(highLimit, newHigh);
        }

        return new int[] { lowLimit, highLimit };
    }
}
