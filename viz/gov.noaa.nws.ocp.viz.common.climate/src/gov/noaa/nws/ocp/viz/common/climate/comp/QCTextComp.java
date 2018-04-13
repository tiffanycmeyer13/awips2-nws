/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.QCToolTip;

/**
 * An object containing a Text Field with a ToolTip that will display data
 * method information when hovering on a textbox.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2016 22135      wpaintsil   Initial creation
 * Oct 27, 2016 22135      wpaintsil   Add functionality for daily qc values. 
 *                                      Extract the listener and tooltip to a separate 
 *                                      class for use with more than just a textfield.
 * 03 JAN 2017  22134      amoore      Better method naming.
 * 04 JAN 2017  22134      amoore      Add grid/row data setting method.
 * 19 SEP 2017  38124      amoore      Use GC for text control sizes.
 * </pre>
 *
 * @author wpaintsil
 */
public class QCTextComp extends Composite {

    /**
     * Width padding to fit text field.
     */
    public static final int WIDTH_PADDING = 20;

    /**
     * Height padding to fit text field.
     */
    public static final int HEIGHT_PADDING = 16;

    /**
     * The text field.
     */
    private final Text textField;

    /**
     * The hover listener that toggles the tooltip visibility.
     */
    private final QCToolTip toolTip;

    /**
     * Flag for whether the tooltip is enabled.
     */
    private boolean tipEnabled;

    /**
     * Constructor with missing info.
     * 
     * @param labelString
     *            The field label displayed in the tooltip.
     * @param style
     *            The style for this composite subclass.
     */
    public QCTextComp(Composite parent, int style) {
        this(parent, style, QCValues.MISSING_STRING, QCValueType.MISSING);
    }

    /**
     * Constructor with missing info. ToolTip enabled by default.
     * 
     * @param labelString
     *            The field label displayed in the tooltip.
     * @param style
     *            The style for this composite subclass.
     * @param labelString
     *            The field label displayed in the tooltip.
     * @param qcValueType
     *            The enum value representing the type of qcValue(daily field or
     *            period).
     */
    public QCTextComp(Composite parent, int style, String labelString,
            QCValueType qcValueType) {
        this(parent, style, labelString, QCValues.MISSING_STRING,
                ParameterFormatClimate.MISSING, qcValueType, true);
    }

    /**
     * Constructor with missing info.
     * 
     * @param labelString
     *            The field label displayed in the tooltip.
     * @param style
     *            The style for this composite subclass.
     * @param labelString
     *            The field label displayed in the tooltip.
     * @param qcValueType
     *            The enum value representing the type of qcValue(daily field or
     *            period).
     * @param tipEnabled
     *            Flag for whether the tooltip is enabled.
     */
    public QCTextComp(Composite parent, int style, String labelString,
            QCValueType qcValueType, boolean tipEnabled) {
        this(parent, style, labelString, QCValues.MISSING_STRING,
                ParameterFormatClimate.MISSING, qcValueType, tipEnabled);
    }

    /**
     * Constructor with all necessary fields provided.
     * 
     * @param parent
     *            The parent composite.
     * @param style
     *            The style for this composite subclass.
     * @param labelString
     *            The field label displayed in the tooltip.
     * @param dataValue
     *            The value of the data for text field to be displayed in the
     *            tooltip.
     * @param qcValue
     *            The int representing the data method to display in the
     *            tooltip.
     * @param tipEnabled
     *            Flag for whether the tooltip is enabled.
     */
    public QCTextComp(final Composite parent, int style, String labelString,
            String dataValue, int qcValue, QCValueType qcValueType,
            boolean tipEnabled) {
        super(parent, style);

        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.center = true;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        this.setLayout(layout);

        textField = new Text(this, ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(textField);

        toolTip = new QCToolTip(textField, labelString, dataValue, qcValue,
                qcValueType);

        setTipEnabled(tipEnabled);
    }

    /**
     * Return the data value field.
     * 
     * @return dataValue
     */
    public String getDataValue() {
        return toolTip.getDataValue();
    }

    /**
     * Add a listener to the text field.
     * 
     * @param eventType
     * @param listener
     */
    public void addListener(int eventType, Listener listener) {
        textField.addListener(eventType, listener);
    }

    /**
     * Return the text field text.
     * 
     * @return textField
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * Set the text field text.
     * 
     * @param value
     */
    public void setText(String value) {
        textField.setText(value);
    }

    /**
     * Set both the text value and the tooltip info text.
     * 
     * @param dataValue
     * @param qcValue
     */
    public void setTextAndTip(String dataValue, int qcValue) {
        textField.setText(dataValue);
        if (tipEnabled) {
            toolTip.setDataValue(dataValue);
            toolTip.setQCValue(qcValue);
        }
    }

    /**
     * @param enabled
     *            Flag to enable tool tip hover. True if enabled; disabled
     *            otherwise.
     */
    public void setTipEnabled(boolean enabled) {
        if (enabled) {
            toolTip.activate();
        } else {
            toolTip.deactivate();
        }
        tipEnabled = enabled;
    }

    /**
     * @return the QCToolTipListener
     */
    public QCToolTip getToolTip() {
        return toolTip;
    }

    /**
     * Set layout data to be {@link GridData} fitting the text field that itself
     * has {@link RowData} from {@link ClimateLayoutValues#getFieldsRD()}, which
     * is the same size as {@link ClimateLayoutValues#getFieldsGD()}. Pad with
     * constant padding of this class.
     */
    public void useGridData() {
        ClimateLayoutValues.assignFieldsGD(this);
        GridData data = (GridData) getLayoutData();
        data.widthHint += WIDTH_PADDING;
        data.heightHint += HEIGHT_PADDING;
    }

    /**
     * Set layout data to be {@link RowData} fitting the text field that itself
     * has {@link RowData} from {@link ClimateLayoutValues#getFieldsRD()}. Pad
     * with constant padding of this class.
     */
    public void useRowData() {
        ClimateLayoutValues.assignFieldsRD(this);
        RowData data = (RowData) getLayoutData();
        data.width += WIDTH_PADDING;
        data.height += HEIGHT_PADDING;
    }
}