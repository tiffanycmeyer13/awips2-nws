/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Control;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;

/**
 * A ToolTip that will display data method information when hovering on the
 * control its attached to, or middle-clicking on the control.
 * 
 * NOTE: Adds a hover and mouse click listener (for middle-click) to the control
 * its attached to.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2016  22135      wpaintsil   Initial creation
 * Nov 01, 2016  22135      wpaintsil   Extend jface.window.DefaultTooltip, 
 *                                      a more versatile tool than swt.widgets.ToolTip.
 * 23 MAR 2017   30515      amoore      Replace constants that are already defined in AWIPS.
 * 02 AUG 2017   36649      amoore      Show tooltip (by making delay 0) on middle-click.
 * 19 SEP 2017   38124      amoore      Address review comments.
 * </pre>
 *
 * @author wpaintsil
 */
public class QCToolTip extends DefaultToolTip {

    /**
     * The label for the field to be displayed in the tooltip.
     */
    private String labelString;

    /**
     * The value of the data for the text field to be displayed in the tooltip.
     */
    private String dataValue;

    /**
     * Flag for whether to show the data value. If false, just show the
     * labelString. True by default;
     */
    private boolean dataValueVisible = true;

    /**
     * The int representing the data method to display in the tooltip.
     */
    private int qcValue;

    /**
     * The enum value representing the type of qcValue(daily field or period).
     */
    private QCValueType qcValueType;

    /**
     * Flag for whether to (true) instantly show tooltip due to user having
     * middle-clicked on this tooltip's control, or (false) have the regular
     * delay for tooltip appearing due to a second middle-click by the user or
     * the user hovering away from the control.
     */
    private boolean instantPopup = false;

    /**
     * 1000 milliseconds delay in showing tooltip.
     */
    private static final int TOOLTIP_DELAY_SHOW = (int) TimeUtil.MILLIS_PER_SECOND;

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(QCToolTip.class);

    /**
     * Constructor with some missing fields.
     * 
     * @param control
     * @param labelString
     * @param qcValueType
     */
    public QCToolTip(Control control, String labelString,
            QCValueType qcValueType) {
        this(control, labelString, QCValues.MISSING_STRING,
                ParameterFormatClimate.MISSING, qcValueType);
    }

    /**
     * Constructor with all necessary fields.
     * 
     * @param control
     * @param labelString
     * @param dataValue
     * @param qcValue
     * @param qcValueType
     */
    public QCToolTip(Control control, String labelString, String dataValue,
            int qcValue, QCValueType qcValueType) {
        super(control, DefaultToolTip.NO_RECREATE, false);

        this.dataValue = dataValue;
        this.labelString = labelString;
        this.qcValue = qcValue;
        this.qcValueType = qcValueType;

        // Delay tooltip showing by 1 second.
        setPopupDelay(TOOLTIP_DELAY_SHOW);

        refreshToolTipText();

        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                /*
                 * on middle-click, switch instantPopup flag and set new delay
                 */
                if (e.button == 2) {
                    instantPopup = !instantPopup;

                    if (instantPopup) {
                        // no delay
                        setPopupDelay(0);
                    } else {
                        // default delay
                        setPopupDelay(TOOLTIP_DELAY_SHOW);
                    }
                }
            }
        });

        control.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                /*
                 * on exiting hover, no longer have instant tooltip popup
                 */
                instantPopup = false;
                setPopupDelay(TOOLTIP_DELAY_SHOW);
            }
        });
    }

    /**
     * Refresh the tooltip message whenever a field changes.
     */
    private void refreshToolTipText() {
        String label = dataValueVisible ? labelString + ": " + dataValue
                : labelString;
        setText(label + "\n" + qcValueType.getQCString(qcValue));
    }

    /**
     * @return dataValue
     */
    public String getDataValue() {
        return dataValue;
    }

    /**
     * @param dataValue
     *            the dataValue to set
     */
    public void setDataValue(String dataValue) {
        try {
            double valueNum = Double.parseDouble(dataValue);
            // Display "Trace" for trace values.
            if (valueNum == ParameterFormatClimate.TRACE)
                this.dataValue = "Trace";

            // Display "Missing" for any missing values.
            else if (valueNum == ParameterFormatClimate.MISSING) {
                this.dataValue = QCValues.MISSING_STRING;
            } else {
                this.dataValue = dataValue;
            }

            refreshToolTipText();
        } catch (NumberFormatException ex) {
            logger.error("Could not parse data value: " + dataValue, ex);
            this.dataValue = QCValues.MISSING_STRING;
        }
    }

    /**
     * @return qcValue
     */
    public int getQcValue() {
        return qcValue;
    }

    /**
     * @param qcValue
     *            the qcValue to set
     */
    public void setQCValue(int qcValue) {
        this.qcValue = qcValue;
        refreshToolTipText();
    }

    /**
     * 
     * @return labelString
     */
    public String getLabelString() {
        return labelString;
    }

    /**
     * @param labelString
     *            the labelString to set
     */
    public void setLabelString(String labelString) {
        this.labelString = labelString;
        refreshToolTipText();
    }

    /**
     * 
     * @return qcValueType
     */
    public QCValueType getQcValueType() {
        return qcValueType;
    }

    /**
     * @param qcValueType
     *            the qcValueType to set
     */
    public void setQcValueType(QCValueType qcValueType) {
        this.qcValueType = qcValueType;
        refreshToolTipText();
    }

    /**
     * 
     * @param dataValueVisible
     *            Set true for dataValue to be visible, false otherwise.
     */
    public void setDataValueVisible(boolean dataValueVisible) {
        this.dataValueVisible = dataValueVisible;
        refreshToolTipText();
    }

}
