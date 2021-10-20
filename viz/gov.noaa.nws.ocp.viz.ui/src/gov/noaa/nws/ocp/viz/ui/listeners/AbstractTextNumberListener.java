/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * This abstract class validates the user input. Invalid input will be
 * highlighted as yellow. Actual validation is up to the implementing classes.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#   Engineer    Description
 * -----------  --------- ----------- --------------------------
 * Aug 14, 2019 65562     jwu         Modified from Climate plugin.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public abstract class AbstractTextNumberListener implements Listener {

    /**
     * Flag for background color change.
     */
    private boolean isBGColorChanged = false;

    /**
     * Epsilon for floating point comparison.
     */
    private static final double EPSILON = 0.00001;

    /**
     * Integer regex
     */
    protected static final String INT_REGEX = "^\\-?\\d+";

    @Override
    public void handleEvent(Event event) {
        if (event.widget instanceof Text) {
            switch (event.type) {
            case SWT.Verify:
                verifyText(event);
                break;
            case SWT.FocusOut:
                focusLost(event);
                break;
            default:
                // do nothing
                break;
            }
        }
    }

    /**
     * Change background color
     *
     * @param control
     * @param isNormal
     */
    public void setBackground(Control control, boolean isNormal) {
        if (isNormal) {
            if (isBGColorChanged) {
                Color listBackground = control.getDisplay()
                        .getSystemColor(SWT.COLOR_LIST_BACKGROUND);
                control.setBackground(listBackground);
                isBGColorChanged = false;
            }
        } else {
            if (!isBGColorChanged) {
                Color yellow = control.getDisplay()
                        .getSystemColor(SWT.COLOR_YELLOW);
                control.setBackground(yellow);
                isBGColorChanged = true;
            }
        }
    }

    /**
     * Compare two floating numbers.
     *
     * @param d1
     * @param d2
     * @return true if the difference between the two given numbers is less than
     *         a constant epsilon value.
     */
    public boolean floatingEquals(double d1, double d2) {
        return (Math.abs(d1 - d2) < EPSILON);
    }

    /**
     * Methods to be implemented by subclasses.
     */
    public abstract void verifyText(Event event);

    public abstract void focusLost(Event event);
}