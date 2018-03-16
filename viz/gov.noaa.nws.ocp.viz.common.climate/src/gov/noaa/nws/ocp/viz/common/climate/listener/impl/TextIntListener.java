/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.viz.common.climate.listener.AbstractTextNumberListener;

/**
 * This class validates the user input. A valid input is a whole number, and in
 * range of [minimum, maximum] or equal to default.
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
 *  SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 08/12/2016  21198    wkwock      Initial creation
 * 10/25/2016  20639    wkwock      Change background color on invalid input.
 * 27 DEC 2016 22450    amoore      Make integer regex public.
 * </pre>
 * 
 * @author amoore
 */
public final class TextIntListener extends AbstractTextNumberListener {

    /**
     * integer regex
     */
    public final static String INT_REGEX = "^\\-?\\d+";

    /**
     * Constructor. Assume 0 is the minimum.
     * 
     * @param iMaxDefault
     *            maximum value, also default value.
     */
    public TextIntListener(Integer iMaxDefault) {
        this(0, iMaxDefault);
    }

    /**
     * Constructor.
     * 
     * @param iMin
     *            minimum value.
     * @param iMaxDefault
     *            maximum value, also default value.
     */
    public TextIntListener(Integer iMin, Integer iMaxDefault) {
        this(iMin, iMaxDefault, iMaxDefault);
    }

    /**
     * Constructor.
     * 
     * @param iMin
     *            minimum value.
     * @param iMax
     *            maximum value.
     * @param iDefault
     *            default value.
     */
    public TextIntListener(Integer iMin, Integer iMax, Integer iDefault) {
        super(iMin, iMax, iDefault);
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

        if (!newText.isEmpty() && !newText.equals("-")) {
            // only parse if text is non-empty, and not negative sign
            if (newText.matches(INT_REGEX)) {
                int newInt = Integer.parseInt(newText);
                if ((newInt < getMin().intValue()
                        || newInt > getMax().intValue())
                        && newInt != getDefault().intValue()) {
                    // outside of valid range
                    setBackground(text, false);
                } else {
                    setBackground(text, true);
                }
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
            textField.setText(String.valueOf(getDefault().intValue()));
        }
    }
}
