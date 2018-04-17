/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener;

import java.util.EventListener;

/**
 * This abstract class is a listener for Text fields, to perform various
 * validation/formatting actions as needed on the fields.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 12 JUL 2016  20414      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 */
public abstract class TextListener implements EventListener {
    /**
     * Constructor.
     */
    public TextListener() {
    }
}