/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog.support;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.viz.climate.display.common.DisplayValues;

/**
 * This class extends {@link CLabel} for convenience of displaying common
 * functionality of the "mismatched value" icon and a tooltip displaying the
 * mismatched values.
 * 
 * Tooltip will display information when hovering on the control its attached
 * to, or left/middle-clicking on the control. Tooltip will only display if
 * there is a discrepancy (internally, if tooltip has been activated).
 * 
 * NOTE: Adds a hover and mouse click listener (for left/middle-click) to
 * itself.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 15 AUG 2016  20414      amoore      Initial creation
 * 16 JUN 2017  35185      amoore      Add matched icon (blank). Set matched
 *                                     by default. These changes together
 *                                     ensure consistent element formatting
 *                                     in the dialog.
 * 07 JUL 2017  33104      amoore      Extract out MismatchLabel as its own class.
 * 03 AUG 2017  36643      amoore      Show tooltip right away if left/middle click.
 * </pre>
 * 
 * @author amoore
 */
public class MismatchLabel extends CLabel {

    /**
     * Value mismatch icon. Currently a yellow triangle with exclamation.
     */
    public static final Image VALUE_MISMATCH_ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin(DisplayValues.PLUGIN_ID,
                    "icons/valueMismatch.png")
            .createImage();
    /**
     * Value match icon. Currently empty, but could make something else to make
     * functionality more clear. Ensure the icons are the same dimensions for
     * consistent element formatting in the dialog.
     */
    public static final Image VALUE_MATCH_ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin(DisplayValues.PLUGIN_ID,
                    "icons/valueMatched.png")
            .createImage();

    /**
     * 1000 milliseconds delay in showing tooltip.
     */
    private static final int TOOLTIP_DELAY_SHOW = (int) TimeUtil.MILLIS_PER_SECOND;

    /**
     * Tooltip object.
     */
    private final DefaultToolTip myToolTip;

    /**
     * Flag for whether to (true) instantly show tooltip due to user having
     * middle-clicked on this tooltip's control, or (false) have the regular
     * delay for tooltip appearing due to a second middle-click by the user or
     * the user hovering away from the control.
     */
    private boolean instantPopup = false;

    /**
     * Constructor. See {@link CLabel#CLabel(Composite, int)}. Set matched by
     * default.
     * 
     * @param parent
     * @param style
     */
    public MismatchLabel(Composite parent, int style) {
        super(parent, style);
        myToolTip = new DefaultToolTip(this, DefaultToolTip.NO_RECREATE, false);

        // Delay tooltip showing by 1 second.
        myToolTip.setPopupDelay(TOOLTIP_DELAY_SHOW);

        addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                // no implementation
            }

            @Override
            public void mouseDown(MouseEvent e) {
                /*
                 * on left-click or middle-click, switch instantPopup flag and
                 * set new delay
                 */
                if (e.button == 1 || e.button == 2) {
                    instantPopup = !instantPopup;

                    if (instantPopup) {
                        // no delay
                        myToolTip.setPopupDelay(0);
                    } else {
                        // default delay
                        myToolTip.setPopupDelay(TOOLTIP_DELAY_SHOW);
                    }
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // no implementation
            }
        });

        addMouseTrackListener(new MouseTrackListener() {

            @Override
            public void mouseHover(MouseEvent e) {
                // no implementation
            }

            @Override
            public void mouseExit(MouseEvent e) {
                /*
                 * on exiting hover, no longer have instant tooltip popup
                 */
                instantPopup = false;
                myToolTip.setPopupDelay(TOOLTIP_DELAY_SHOW);
            }

            @Override
            public void mouseEnter(MouseEvent e) {
                // no implementation
            }
        });

        setMatched();
    }

    /**
     * The monthly and daily values associated with this label match, or one or
     * both are missing. Set image to blank and erase the tooltip text, and
     * deactivate tooltip. Label (or ancestor) will need to be packed.
     */
    public void setMatched() {
        setImage(MismatchLabel.VALUE_MATCH_ICON);
        myToolTip.setText(null);
        myToolTip.deactivate();
    }

    /**
     * The monthly and daily values associated with this label do not match. Set
     * image to the mismatched values icon and set appropriate tooltip text,
     * activate tooltip, displaying the mismatched values. Label (or ancestor)
     * will need to be packed.
     * 
     * @param iMonthlyValue
     * @param iDailyValue
     */
    public void setNotMatched(Number iMonthlyValue, Number iDailyValue) {
        setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        myToolTip.setText("The MSM value " + iMonthlyValue
                + " does not match the Daily DB value " + iDailyValue + ".");
        myToolTip.activate();
    }

    /**
     * The monthly and daily values associated with this label do not match. Set
     * image to the mismatched values icon and set appropriate tooltip text,
     * activate tooltip, displaying the mismatched values. Label (or ancestor)
     * will need to be packed.
     * 
     * @param iMonthlyValue
     * @param iDailyValue
     */
    public void setNotMatched(ClimateDate[] iMonthlyValue,
            ClimateDate[] iDailyValue) {
        setImage(MismatchLabel.VALUE_MISMATCH_ICON);

        StringBuilder sb = new StringBuilder();
        if (iMonthlyValue.length > 0) {
            sb.append("The MSM date(s) ");
            for (ClimateDate date : iMonthlyValue) {
                sb.append(date.toMonthDayDateString());
                sb.append(" ");
            }
            sb.append("do ");
        } else {
            sb.append("The MSM has no dates, which does ");
        }

        if (iDailyValue.length > 0) {
            sb.append("not match the Daily DB date(s)");
            for (ClimateDate date : iDailyValue) {
                sb.append(" ");
                sb.append(date.toMonthDayDateString());
            }
            sb.append(".");
        } else {
            sb.append("not match the empty set of Daily DB dates.");
        }

        myToolTip.setText(sb.toString());
        myToolTip.activate();
    }

    /**
     * The monthly and daily values associated with this label do not match. Set
     * image to the mismatched values icon and set appropriate tooltip text,
     * activate tooltip, displaying the mismatched values. Label (or ancestor)
     * will need to be packed.
     * 
     * @param iMonthlyValue
     * @param iDailyValue
     */
    public void setNotMatched(ClimateDates[] iMonthlyValue,
            ClimateDates[] iDailyValue) {
        setImage(MismatchLabel.VALUE_MISMATCH_ICON);

        StringBuilder sb = new StringBuilder();

        if (iMonthlyValue.length > 0) {
            sb.append("The MSM dates ");
            for (ClimateDates dates : iMonthlyValue) {
                sb.append("[");
                sb.append(dates.getStart().toMonthDayDateString());
                sb.append(" to ");
                sb.append(dates.getEnd().toMonthDayDateString());
                sb.append("] ");
            }
            sb.append("do ");
        } else {
            sb.append("The MSM has no dates, which does ");
        }

        if (iDailyValue.length > 0) {
            sb.append("not match the Daily DB dates");
            for (ClimateDates dates : iDailyValue) {
                sb.append(" [");
                sb.append(dates.getStart().toMonthDayDateString());
                sb.append(" to ");
                sb.append(dates.getEnd().toMonthDayDateString());
                sb.append("]");
            }
            sb.append(".");
        } else {
            sb.append("not match the empty set of Daily DB dates.");
        }

        myToolTip.setText(sb.toString());
        myToolTip.activate();
    }
}