/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * Abstract class for TemperatureComp, PrecipitationComp, SnowfallComp, and
 * DegreeDaysComp classes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 02 MAR 2017 29576    wkwock      Initial creation
 * 21 SEP 2017 38124    amoore      Use better abstraction logic for valid months.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public abstract class AbstractClimateInitComp extends Composite {
    /** period climatology record */
    protected PeriodClimo periodRcd;

    /** bold font */
    protected Font boldFont;

    /** change listener */
    protected final UnsavedChangesListener changesListener;

    /**
     * Collection of listeners.
     */
    protected final ClimateTextListeners myDisplayListeners = new ClimateTextListeners();

    /**
     * Constructor.
     * 
     * @param parent
     * @param style
     * @param changeListener
     */
    protected AbstractClimateInitComp(Composite parent, int style,
            UnsavedChangesListener changeListener) {
        super(parent, style);
        FontData fontData = getShell().getFont().getFontData()[0];
        boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.BOLD));
        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                boldFont.dispose();
            }
        });

        changesListener = changeListener;
    }

    /**
     * @return valid months for the given period type details
     */
    protected int[] getValidMonths() {
        // The month in periodRcd start with 1 but 0 in
        // DateSelectionComp
        int month = periodRcd.getMonthOfYear() - 1;
        int[] months = null;
        // For monthly period, the currently selected month in the only
        // valid one.
        // For seasonal period, there's 3 months are valid: monthOfYear
        // and the 2 months before.
        // For others, all months are valid.
        if (periodRcd.getPeriodType() == PeriodType.MONTHLY_RAD) {
            months = new int[1];
            months[0] = month;
        } else if (periodRcd.getPeriodType() == PeriodType.SEASONAL_RAD) {
            months = new int[3];
            months[0] = (month < 2) ? (month + 10) : (month - 2);
            months[1] = (month == 0) ? 12 : (month - 1);
            months[2] = month;
        }

        return months;
    }

    /**
     * Set all {@link DateSelectionComp}s in this pane to use new set of valid
     * months, because period type details have changed.
     */
    protected abstract void setDateSelectionValidMonths();
}
