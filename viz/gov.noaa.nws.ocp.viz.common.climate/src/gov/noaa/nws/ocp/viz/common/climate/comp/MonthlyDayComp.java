/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.comp;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

/**
 * Create a Composite with a combo holding only a single day of the month.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 10, 2016 20636      wpaintsil   Initial creation
 * 12 MAY 2017  33104      amoore      Address minor FindBugs.
 * 03 JUL 2017  35694      amoore      Alter for more generic solution.
 * 19 SEP 2017  38124      amoore      Address review comments.
 * </pre>
 * 
 * @author wpaintsil
 */
public class MonthlyDayComp extends AbstractDateComp {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MonthlyDayComp.class);

    /**
     * The combo box for month selection.
     */
    protected CCombo monthlyDayCombo;

    /**
     * Track if background color is changed (from validation).
     */
    protected boolean isBGColorChanged = false;

    /**
     * Current full selected date.
     */
    private ClimateDate myCurrDate = null;

    /**
     * Constructor. Automatically creates its own layout.
     * 
     * @param parent
     * @param style
     * @param date
     */
    public MonthlyDayComp(Composite parent, int style, ClimateDate date) {
        super(parent, style);

        RowLayout daySelectorRL = new RowLayout(SWT.HORIZONTAL);
        daySelectorRL.center = true;

        this.setLayout(daySelectorRL);
        this.setBackgroundMode(SWT.INHERIT_FORCE);

        monthlyDayCombo = new CCombo(this, SWT.DROP_DOWN | SWT.BORDER);

        daySelectorRL.marginRight = 40;

        myCurrDate = new ClimateDate(date);

        setNumDays(date);

        monthlyDayCombo.addFocusListener(new FocusAdapter() {
            // Set to missing day if user entered string is not in the
            // days range for that month.
            @Override
            public void focusLost(FocusEvent e) {
                validateText();
            }

        });

        monthlyDayCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (containsDay()) {
                    if (isBGColorChanged) {
                        monthlyDayCombo.setBackground(null);
                        isBGColorChanged = false;
                    }
                } else {
                    if (!isBGColorChanged) {
                        Color yellow = monthlyDayCombo.getDisplay()
                                .getSystemColor(SWT.COLOR_YELLOW);
                        monthlyDayCombo.setBackground(yellow);
                        isBGColorChanged = true;
                    }
                }
            }

        });

        GC gc = new GC(monthlyDayCombo);
        monthlyDayCombo.setLayoutData(new RowData(
                (21 * gc.getFontMetrics().getAverageCharWidth()) / 2,
                (3 * gc.getFontMetrics().getHeight()) / 2));
        gc.dispose();
    }

    /**
     * @return the text in the combo. Consider using {@link #getDate()} instead
     *         for precise date.
     */
    public String getText() {
        return monthlyDayCombo.getText();
    }

    /**
     * Add a listener to the control.
     * 
     * @param eventType
     * @param listener
     */
    @Override
    public void addListener(int eventType, Listener listener) {
        monthlyDayCombo.addListener(eventType, listener);
    }

    /**
     * @param date
     *            The number of days in the combo will be set to the number of
     *            days in the month of this date.
     */
    private void setNumDays(ClimateDate date) {
        int numDays = ClimateUtilities.daysInMonth(date);
        String days[] = new String[numDays];
        for (int i = 0; i < numDays; i++) {
            days[i] = String.valueOf(i + 1);
        }
        monthlyDayCombo.setItems(days);
    }

    /**
     * @return true if the month contains the given date, false otherwise
     */
    private boolean containsDay() {
        String[] days = monthlyDayCombo.getItems();
        String text = monthlyDayCombo.getText();
        return (new ArrayList<String>(Arrays.asList(days))).contains(text)
                || text.equals(ClimateDate.MISSING_DATE_NUM_STRING);
    }

    /**
     * Set the date to missing.
     */
    @Override
    public void setMissing() {
        monthlyDayCombo.setText(ClimateDate.MISSING_DATE_NUM_STRING);
    }

    /**
     * @return copy of the current full date represented in this component.
     */
    public ClimateDate getDate() {
        return new ClimateDate(myCurrDate);
    }

    /**
     * Set using only the day from the current date. For other months and years,
     * construct a new instance.
     */
    public void setDate(ClimateDate date) {
        monthlyDayCombo.setText(String.valueOf(date.getDay()));
        validateText();
    }

    /**
     * Validate the current selected day.
     */
    protected void validateText() {
        if (!containsDay()) {
            logger.warn("Invalid day of month: [" + monthlyDayCombo.getText()
                    + "] entered. Setting to missing.");
            monthlyDayCombo.setText(ClimateDate.MISSING_DATE_NUM_STRING);
            myCurrDate.setDay(ParameterFormatClimate.MISSING_DATE);
        } else {
            myCurrDate.setDay(Integer.parseInt(monthlyDayCombo.getText()));
        }
    }

}