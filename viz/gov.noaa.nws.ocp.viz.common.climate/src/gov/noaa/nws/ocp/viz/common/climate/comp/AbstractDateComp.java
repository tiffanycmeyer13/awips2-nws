/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.comp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * Allows for more flexibility in how to select a date.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 10, 2016 20636      wpaintsil   Initial creation
 * 03 JUL 2017  35694      amoore      Add #getDate and #setDate.
 *                                     Remove #getText and #setText.
 * </pre>
 * 
 * @author wpaintsil
 */

public abstract class AbstractDateComp extends Composite {

    public AbstractDateComp(Composite parent, int style) {
        super(parent, style);
    }

    public abstract void addListener(int style, Listener listener);

    public abstract void setMissing();

    public abstract ClimateDate getDate();

    public abstract void setDate(ClimateDate date);
}