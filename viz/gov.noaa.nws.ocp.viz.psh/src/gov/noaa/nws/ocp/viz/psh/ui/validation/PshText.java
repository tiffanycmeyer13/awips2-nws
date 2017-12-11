/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.validation;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;

/**
 * Generic text control that cannot be empty.
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
public class PshText extends PshAbstractText {

    public PshText(Composite parent) {
        super(parent, false);
        addFocusListener();
    }

    /**
     * Unnecessary for this control.
     */
    @Override
    protected VerifyListener verifyListener() {
        return null;
    }

    protected void addFocusListener() {
        textField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

                highlightInput();
            }
        });

    }

    /**
     * Unnecessary for this control.
     */
    @Override
    protected String validationRegex() {
        return null;
    }

    /**
     * Unnecessary for this control.
     */
    @Override
    protected String template() {
        return null;
    }

}
