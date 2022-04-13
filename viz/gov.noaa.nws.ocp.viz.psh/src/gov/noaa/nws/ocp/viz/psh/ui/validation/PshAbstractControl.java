/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.validation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

/**
 * Control with validation used in PSH Generator tables.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2017 #39233      wpaintsil   Initial creation.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public abstract class PshAbstractControl extends Composite {

    /**
     * Flag for whether the input in the control is valid.
     */
    protected boolean valid = true;

    /**
     * Constructor
     * 
     * @param parent
     */
    public PshAbstractControl(Composite parent) {
        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        this.setLayout(layout);
        this.setLayoutData(layoutData);

    }

    /**
     * Change background color to yellow if the input is invalid.
     * 
     * @return true if the input is valid, false otherwise.
     */
    public abstract boolean highlightInput();

    /**
     * Add a listener to the text field.
     * 
     * @param eventType
     * @param listener
     */
    public abstract void addListener(int eventType, Listener listener);

    /**
     * Return the text field text.
     * 
     * @return textField
     */
    public abstract String getText();

    /**
     * Set the text field text.
     * 
     * @param value
     */
    public abstract void setText(String value);

    /**
     * @return true if the input is valid, false otherwise.
     */
    public boolean isValid() {
        return valid;
    }
}
