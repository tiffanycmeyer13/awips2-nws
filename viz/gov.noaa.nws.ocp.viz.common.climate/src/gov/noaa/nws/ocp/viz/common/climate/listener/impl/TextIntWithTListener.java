package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * This class validates the user-typing input. A valid input is an integer and
 * in range of [minimum, maximum] or 'T' for trace or equal to default. Trace
 * value is assumed to be {@link ParameterFormatClimate#TRACE}, with immediate
 * replacement with trace symbol. Trace symbol is assumed to be
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
 * 03/19/2019  DR21197   wpaintsil    Initial creation
 * </pre>
 *
 * @author wpaintsil
 */

public final class TextIntWithTListener extends TextIntListener {

    /**
     * Constructor
     * 
     * @param maxDefault
     */
    public TextIntWithTListener(Integer maxDefault) {
        super(maxDefault);
    }

    /**
     * Constructor
     * 
     * @param min
     * @param maxDefault
     */
    public TextIntWithTListener(Integer min, Integer maxDefault) {
        super(min, maxDefault);
    }

    /**
     * Contructor
     * 
     * @param min
     * @param max
     * @param maxDefault
     */
    public TextIntWithTListener(Integer min, Integer max, Integer maxDefault) {
        super(min, max, maxDefault);
    }

    @Override
    public void verifyText(Event event) {

        Text text = (Text) event.widget;

        // This is what's in the text field
        String originalText = text.getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);

        // verify user input only
        if (!((Control) event.widget).isFocusControl()) {

            if (newText.equals(
                    String.valueOf((int) ParameterFormatClimate.TRACE))) {
                event.doit = false;
                text.setText(ParameterFormatClimate.TRACE_SYMBOL);
            }

            return;
        }

        if (!newText.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
            /*
             * Verify user input if not exactly the trace symbol. No rounding
             * for user input.
             */
            if (newText.equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                    || newText.equals(String
                            .valueOf((int) ParameterFormatClimate.TRACE))) {
                // Correct symbol casing
                // Change trace value to explicitly be trace symbol
                event.doit = false;
                text.setText(ParameterFormatClimate.TRACE_SYMBOL);
            } else if (!newText.isEmpty() && !newText.equals("-")) {
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
        } else {
            setBackground(text, true);
        }
    }

    @Override
    public void focusLost(Event e) {
        Text textField = (Text) e.widget;

        setBackground(textField, true);

        if (!textField.getText().matches(INT_REGEX) && !textField.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)) {
            textField.setText(String.valueOf(getDefault().intValue()));
        }
    }

}
