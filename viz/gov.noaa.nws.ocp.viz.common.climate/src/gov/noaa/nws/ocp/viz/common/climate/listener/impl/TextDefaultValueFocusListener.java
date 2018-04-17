/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.viz.common.climate.listener.TextFocusListener;

/**
 * This class is a focus listener for Text fields that displays a default value
 * if the field is left empty.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 13 JUL 2016  20414      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 */
public class TextDefaultValueFocusListener extends TextFocusListener {

    /**
     * Default value.
     */
    private final String myDefault;

    /**
     * Constructor.
     */
    public TextDefaultValueFocusListener(String iDefault) {
        super();
        myDefault = iDefault;
    }

    @Override
    public void focusGained(FocusEvent e) {
        // no implementation
    }

    @Override
    public void focusLost(FocusEvent e) {
        Text textField = (Text) e.getSource();
        if (textField.getText().isEmpty()) {
            textField.setText(myDefault);
        }
    }

}
