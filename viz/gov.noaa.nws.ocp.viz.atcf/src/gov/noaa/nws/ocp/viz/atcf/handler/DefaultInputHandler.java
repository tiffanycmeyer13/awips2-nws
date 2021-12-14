/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.raytheon.viz.ui.input.InputAdapter;

/**
 * Default input handler for ATCF mouse/key input. Mainly to track if "SHIFT"
 * key is down or not, since mouseDownMove by default is used for "Pan" tool.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 18, 2019 71722      jwu         Initial creation.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */

public class DefaultInputHandler extends InputAdapter {

    protected boolean isShiftDown(Event event) {
        return (event.stateMask & SWT.SHIFT) != 0;
    }

}
