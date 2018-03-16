/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Text;

/**
 * This abstract class is a verify listener for decimal Text fields, to perform
 * validation that input is not less than a minimum or greater than a maximum,
 * and is a number. No effect if field is empty.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 12 JUL 2016  20414      amoore      Initial creation
 * 27 DEC 2016  20998      amoore      Remove comment linking to subclass.
 * </pre>
 * 
 * @author amoore
 */
public abstract class TextDecimalVerifyListener
        extends TextNumberVerifyListener {

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
    public TextDecimalVerifyListener(Number iMin, Number iMax,
            Number iDefault) {
        super(iMin, iMax, iDefault);
    }

    @Override
    public final void verifyText(VerifyEvent event) {
        // make sure new text is decimal-like
        if (TextNumberVerifyListener.NON_DIGIT_NON_DECIMAL_PATTERN
                .matcher(event.text).find()) {
            // non-decimal-like text entered
            event.doit = false;
        } else {
            // decimal-like text entered
            Text text = (Text) event.getSource();

            String originalText = text.getText();
            String newText = originalText.substring(0, event.start) + event.text
                    + originalText.substring(event.end);

            if (TextNumberVerifyListener.MULTIPLE_DECIMAL.matcher(newText)
                    .find()) {
                // make sure only 1 decimal place
                event.doit = false;
            } else if (!newText.isEmpty()) {
                // make sure whole text is within range, or is empty
                checkDecimalRange(event, text, newText);
            }
        }
    }

    /**
     * Verify range for decimal number.
     * 
     * @param iEvent
     *            event to validate.
     * @param iText
     *            text field to potentially modify.
     * @param iNewText
     *            total new text to validate.
     */
    protected abstract void checkDecimalRange(VerifyEvent iEvent, Text iText,
            String iNewText);
}
