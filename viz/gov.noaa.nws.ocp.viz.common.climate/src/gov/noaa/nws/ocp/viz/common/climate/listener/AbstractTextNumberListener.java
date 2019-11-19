/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * This abstract class valid the user input at each type. A valid input must be
 * a number and in range of the minimum and maximum. Actual validation is up to
 * implementing classes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 08/12/2016  21198    wkwock      Initial creation
 * 10/25/2016  20639    wkwock      Add setBackground
 * 11/01/2016  DR21622  wpaintsil   Ensure invalid text is replaced.
 * 
 * </pre>
 * 
 * @author wkwock
 */
public abstract class AbstractTextNumberListener implements Listener {
    /**
     * Minimum value.
     */
    private final Number myMin;

    /**
     * Maximum value.
     */
    private final Number myMax;

    /**
     * Default value.
     */
    private final Number myDefault;

    /**
     * is background color changed
     */
    private boolean isBGColorChanged = false;

    // protected enum ValidType {
    // VALID, NO_ENTRY, OUT_OF_BOUNDS
    // }

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
    public AbstractTextNumberListener(Number iMin, Number iMax,
            Number iDefault) {
        myMin = iMin;
        myMax = iMax;
        myDefault = iDefault;
    }

    /**
     * @return the Min
     */
    protected final Number getMin() {
        return myMin;
    }

    /**
     * @return the Max
     */
    protected final Number getMax() {
        return myMax;
    }

    /**
     * @return the default
     */
    protected final Number getDefault() {
        return myDefault;
    }

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
                Color white = control.getDisplay()
                        .getSystemColor(SWT.COLOR_WHITE);
                control.setBackground(white);
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

    public abstract void verifyText(Event event);

    public void focusLost(Event e) {
        Text textField = (Text) e.widget;

        if (!isValid(textField.getText())) {
            textField.setText(String.valueOf(getDefault().intValue()));
        }

        setBackground(textField, true);
    }

    protected abstract boolean isValid(String text);
}
