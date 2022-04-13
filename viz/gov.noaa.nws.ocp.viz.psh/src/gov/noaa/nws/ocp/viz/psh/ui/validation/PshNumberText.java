/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.validation;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Text control for numbers.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 01, 2017 #40067      wpaintsil   Initial creation.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshNumberText extends PshAbstractText {

    /**
     * Regular expression for a number.
     */
    private static final String NUMBER_VALIDATION_REGEX = "^-?\\d*\\.?\\d*";

    public PshNumberText(Composite parent) {
        this(parent, false);
    }

    public PshNumberText(Composite parent, boolean emptyAllowed) {
        super(parent, emptyAllowed);

        addFocusListener();

        textField.addVerifyListener(verifyListener());
    }

    protected void addFocusListener() {

        textField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

                highlightInput();
            }
        });

    }

    @Override
    public boolean highlightInput() {
        valid = NumberUtils.isNumber(textField.getText())
                || (emptyAllowed && textField.getText().isEmpty());

        Color bgColor = valid
                ? textField.getDisplay().getSystemColor(SWT.COLOR_WHITE)
                : textField.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

        textField.setBackground(bgColor);

        return valid;
    }

    /**
     * Allows only numbers to be entered.
     */
    @Override
    protected VerifyListener verifyListener() {
        return new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent event) {
                // verify user input only
                if (!((Control) event.widget).isFocusControl()) {
                    return;
                }

                Text textField = (Text) event.widget;

                String originalText = textField.getText();
                String newText = originalText.substring(0, event.start)
                        + event.text + originalText.substring(event.end);

                if (!newText.isEmpty() && !newText.equals("-")) {

                    boolean isFloat = true;
                    try {
                        Float.parseFloat(newText);
                    } catch (NumberFormatException ex) {
                        isFloat = false;
                    }

                    if (!isFloat) {
                        event.doit = false;
                    }
                }

            }
        };
    }

    /**
     * Unnecessary for this control.
     */
    @Override
    protected String validationRegex() {
        return NUMBER_VALIDATION_REGEX;
    }

    /**
     * Unnecessary for this control.
     */
    @Override
    protected String template() {
        return null;
    }
}
