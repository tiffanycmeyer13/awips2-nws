/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener;

import org.eclipse.swt.events.FocusListener;

/**
 * This abstract class is a focus listener for Text fields, to perform various
 * actions as needed on the fields when focus is gained/lost (usually just for
 * lost).
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 11 JUL 2016  20414      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 */
public abstract class TextFocusListener extends TextListener
        implements FocusListener {

    /**
     * Constructor.
     * 
     */
    public TextFocusListener() {
        super();
    }

}