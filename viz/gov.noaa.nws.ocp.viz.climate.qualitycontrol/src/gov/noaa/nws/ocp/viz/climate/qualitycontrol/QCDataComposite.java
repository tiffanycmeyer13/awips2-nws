/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.qualitycontrol;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.QCTextComp;

/**
 * A parent class lets the currentMainDataSection field point to either type
 * interchangeably. Both a Daily composite and a Period composite need to load
 * and save data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 02, 2016  20636      wpaintsil   Initial creation
 * Oct 31, 2016  22135      wpaintsil   Add saveValue() helper methods.
 * Nov 17, 2016  20636      wpaintsil   Add flags indicating a new value was saved for certain fields.
 * Dec 02, 2016  20636      wpaintsil   Helper methods for text field creation.
 * 03 JAN 2017   22134      amoore      Unnecessary parameter passing.
 * 15 MAR 2017   30162      amoore      Fix exception throwing.
 * 16 MAY 2017   33104      amoore      Floating point equality.
 * 16 MAY 2017   33104      amoore      Remove unnecessary method parameters in Text comp creation.
 * </pre>
 * 
 * @author wpaintsil
 */
public abstract class QCDataComposite extends Composite {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(QCDataComposite.class);

    /**
     * A set containing the ValueChangedFlags. Containing a particular flag
     * indicates the value for the related field(s) was changed.
     */
    protected Set<ValueChangedFlag> valueChangedFlags = new HashSet<ValueChangedFlag>();

    /**
     * In the legacy code, flags for certain fields are set to true if the value
     * is changed and saved.
     */
    public enum ValueChangedFlag {
        /**
         * Represents a change in the max temp field.
         */
        MAX_TEMP_FLAG,
        /**
         * Represents a change in the min temp field.
         */
        MIN_TEMP_FLAG,
        /**
         * Represents a change in a precip field.
         */
        PRECIP_FLAG,
        /**
         * Represents a change in a snow field.
         */
        SNOW_FLAG,
        /**
         * Represents a change in a snow depth field.
         */
        DEPTH_FLAG,
        /**
         * Represents a change in a wind field.
         */
        WIND_FLAG,
        /**
         * Represents a change in a gust field.
         */
        GUST_FLAG,
        /**
         * Represents a change in a relative humidity field.
         */
        RH_FLAG,
        /**
         * Represents a change in a the possible sun field.
         */
        SUN_FLAG,
        /**
         * Represents a change in a sky cover field.
         */
        SKY_FLAG,
        /**
         * Represents a change in an observed weather checkbox.
         */
        WX_FLAG
    };

    /** Flag for if a field with records has been changed **/
    protected boolean recordFlag = false;

    /**
     * Default contructor
     * 
     * @param parent
     * @param style
     */
    public QCDataComposite(Composite parent, int style) {
        super(parent, style);
    }

    public abstract void loadData() throws ClimateException;

    public abstract void saveData();

    /**
     * Helper method to save a text field float value.
     * 
     * @param before
     *            the original value
     * @param after
     *            the new value entered in the text field
     * @param setValueMethodReference
     *            method reference for the PeriodData/DailyClimateData setter
     *            for a field
     * @param setQCMethodReference
     *            method reference for a PeriodDataMethod/DailyDataMethod setter
     *            for a field
     */
    protected void saveValue(float before, String after,
            Consumer<Float> setValueMethodReference,
            Consumer<Integer> setQCMethodReference,
            ValueChangedFlag valueChanged) {
        float valueAfter;
        // Check if the field contains the trace symbol
        if (after.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
            valueAfter = ParameterFormatClimate.TRACE;
        } else {
            try {
                valueAfter = Float.parseFloat(after);
            } catch (NumberFormatException e) {
                logger.error("Bad save value: [" + after + "]. [" + before
                        + "] will be used instead.", e);
                valueAfter = before;
            }
        }
        if (!ClimateUtilities.floatingEquals(before, valueAfter)) {
            // save the value in the text field
            setValueMethodReference.accept(valueAfter);
            // Add the corresponding flag indicating that a new value for that
            // field is saved.
            valueChangedFlags.add(valueChanged);
            // save the data method (if any) as manual entry
            saveManualEntry(setQCMethodReference);
        }
    }

    /**
     * Overload - save a text field int value.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     * @param setQCMethodReference
     */
    protected void saveValue(int before, String after,
            Consumer<Integer> setValueMethodReference,
            Consumer<Integer> setQCMethodReference,
            ValueChangedFlag valueChanged) {
        int valueAfter;
        try {
            valueAfter = Integer.parseInt(after);
        } catch (NumberFormatException e) {
            logger.error("Bad save value: [" + after + "]. [" + before
                    + "] will be used instead.", e);
            valueAfter = before;
        }

        if (before != valueAfter) {
            setValueMethodReference.accept(valueAfter);

            valueChangedFlags.add(valueChanged);

            saveManualEntry(setQCMethodReference);
        }
    }

    /**
     * Overload - save a text field double value.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     * @param setQCMethodReference
     */
    protected void saveValue(double before, String after,
            Consumer<Double> setValueMethodReference,
            Consumer<Integer> setQCMethodReference) {
        double valueAfter;
        try {
            valueAfter = Double.parseDouble(after);
        } catch (NumberFormatException e) {
            logger.error("Bad save value: [" + after + "]. [" + before
                    + "] will be used instead.", e);
            valueAfter = before;
        }
        // don't set if it's the same value as before
        if (!ClimateUtilities.floatingEquals(before, valueAfter)) {
            // save the value in the text field
            setValueMethodReference.accept(valueAfter);
            // save the data method (if any) as manual entry
            saveManualEntry(setQCMethodReference);
        }
    }

    /**
     * Overload - save a text field int value without a data method or
     * valueChangedFlag.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     */
    protected void saveValue(int before, String after,
            Consumer<Integer> setValueMethodReference) {
        saveValue(before, after, setValueMethodReference, null, null);
    }

    /**
     * Overload - save a text field int value without a data method.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     * @param valueChanged
     */
    protected void saveValue(int before, String after,
            Consumer<Integer> setValueMethodReference,
            ValueChangedFlag valueChanged) {
        saveValue(before, after, setValueMethodReference, null, valueChanged);
    }

    /**
     * Overload - save a text field int value without a valueChangedFlag.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     * @param setQCMethodReference
     */
    protected void saveValue(int before, String after,
            Consumer<Integer> setValueMethodReference,
            Consumer<Integer> setQCMethodReference) {
        saveValue(before, after, setValueMethodReference, setQCMethodReference,
                null);
    }

    /**
     * Overload - save a text field float value without a data method or
     * valueChangedFlag.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     */
    protected void saveValue(float before, String after,
            Consumer<Float> setValueMethodReference) {
        saveValue(before, after, setValueMethodReference, null, null);
    }

    /**
     * Overload - save a text field float value without a valueChangedFlag.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     * @param setQCMethodReference
     */
    protected void saveValue(float before, String after,
            Consumer<Float> setValueMethodReference,
            Consumer<Integer> setQCMethodReference) {
        saveValue(before, after, setValueMethodReference, setQCMethodReference,
                null);
    }

    /**
     * Overload - save a text field double value without a data method.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     */
    protected void saveValue(double before, String after,
            Consumer<Double> setValueMethodReference) {
        saveValue(before, after, setValueMethodReference, null);
    }

    /**
     * Overload - save a text field time value.
     * 
     * @param before
     * @param after
     * @param setValueMethodReference
     */
    protected void saveValue(ClimateTime before, String after,
            Consumer<ClimateTime> setValueMethodReference) {
        ClimateTime valueAfter = new ClimateTime(after);

        if (!before.equals(valueAfter)) {
            // save the value in the text field
            setValueMethodReference.accept(valueAfter);
        }
    }

    /**
     * Overload - save a text field ClimateWind value.
     * 
     * @param before
     *            original direction and speed
     * @param directionAfter
     *            new direction entered in the text field
     * @param speedAfter
     *            new speed entered in the text field
     * @param setValueMethodReference
     */
    protected void saveValue(ClimateWind before, String directionAfter,
            String speedAfter, Consumer<ClimateWind> setValueMethodReference,
            Consumer<Integer> setQCMethodReference,
            ValueChangedFlag valueChanged) {
        int newDirection;
        try {
            newDirection = Integer.parseInt(directionAfter);
        } catch (NumberFormatException e) {
            logger.error("Bad save value: [" + directionAfter + "]. ["
                    + before.getDir() + "] will be used instead.", e);
            newDirection = before.getDir();
        }

        float newSpeed;
        try {
            newSpeed = Float.parseFloat(speedAfter);
        } catch (NumberFormatException e) {
            logger.error("Bad save value: [" + speedAfter + "]. ["
                    + before.getSpeed() + "] will be used instead.", e);
            newSpeed = before.getSpeed();
        }

        // set a new ClimateWind field if either field is new
        if (newDirection != before.getDir() || !ClimateUtilities
                .floatingEquals(newSpeed, before.getSpeed())) {
            setValueMethodReference
                    .accept(new ClimateWind(newDirection, newSpeed));

            valueChangedFlags.add(valueChanged);
            saveManualEntry(setQCMethodReference);
        }

    }

    /**
     * Overload - save a text field ClimateWind value without a data method or
     * valueChangedFlag.
     * 
     * @param before
     * @param directionAfter
     * @param speedAfter
     * @param setValueMethodReference
     */
    protected void saveValue(ClimateWind before, String directionAfter,
            String speedAfter, Consumer<ClimateWind> setValueMethodReference) {
        saveValue(before, directionAfter, speedAfter, setValueMethodReference,
                null, null);
    }

    /**
     * Set the qc value for a field to Manual Entry.
     * 
     * @param setQCMethodReference
     */
    private void saveManualEntry(Consumer<Integer> setQCMethodReference) {
        if (setQCMethodReference != null) {
            setQCMethodReference.accept(QCValues.MANUAL_ENTRY);
        }
    }

    /**
     * Determine whether the new value in a text field breaks or ties a record.
     * 
     * @param before
     *            the value before the field was changed
     * @param afterText
     *            the new value entered in the field
     * @param recordValue
     *            the record for that field
     * @param greatOrLess
     *            true if record-breaking would be a greater-than-current value,
     *            false if record-breaking would be less-than-current value
     */
    protected void recordCheck(int before, String afterText, int recordValue,
            boolean greatOrLess) {
        try {
            int after = Integer.parseInt(afterText);
            if (before != after && ((!greatOrLess && after <= recordValue)
                    || (greatOrLess && after >= recordValue
                            && after != ParameterFormatClimate.MISSING))) {
                recordFlag = true;
            }
        } catch (NumberFormatException e) {
            logger.error("Bad potential record value: [" + afterText + "].", e);
            recordFlag = false;
        }
    }

    /**
     * Determine whether the new value in a text field breaks or ties a record.
     * 
     * @param before
     *            the value before the field was changed
     * @param afterText
     *            the new value entered in the field
     * @param recordValue
     *            the record for that field
     */
    protected void recordCheck(float before, String afterText,
            float recordValue) {
        try {
            float after = 0;
            if (afterText.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                after = ParameterFormatClimate.TRACE;
            } else {
                after = Float.parseFloat(afterText);
            }

            if (!ClimateUtilities.floatingEquals(before, after)
                    && ((after >= recordValue && after != 0)
                            || (after == ParameterFormatClimate.TRACE
                                    && recordValue == 0))
                    && !ClimateUtilities.floatingEquals(after,
                            ParameterFormatClimate.MISSING)) {
                recordFlag = true;
            }
        } catch (NumberFormatException e) {
            logger.error("Bad potential record value: [" + afterText + "].", e);
            recordFlag = false;
        }
    }

    /**
     * Create a qc textField in a new composite.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param qcLabelString
     *            label for the qc tooltip
     * @param qcType
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @param tipEnabled
     *            true if the tooltip for qc text should be enabled, false
     *            otherwise
     * @return
     */
    protected static QCTextComp createTextWithComposite(Composite parent,
            String labelString, String qcLabelString, QCValueType qcType,
            Listener fieldListener, Listener unsavedChangesListener,
            boolean tipEnabled) {
        Label maxWindLabel = new Label(parent, SWT.NORMAL);
        maxWindLabel.setText(labelString);
        Composite subComposite = new Composite(parent, SWT.NONE);
        RowLayout subLayout = new RowLayout(SWT.HORIZONTAL);
        subComposite.setLayout(subLayout);
        QCTextComp newText = new QCTextComp(subComposite, SWT.NONE,
                qcLabelString, qcType, tipEnabled);
        addTextListeners(newText, fieldListener, unsavedChangesListener);

        return newText;
    }

    /**
     * Create a textField in a new composite.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @param enabled
     *            true if the text field should be enabled, false otherwise
     * @return
     */
    protected static Text createTextWithComposite(Composite parent,
            String labelString, Listener fieldListener,
            Listener unsavedChangesListener, boolean enabled) {
        Label label = new Label(parent, SWT.NORMAL);
        label.setText(labelString);
        label.setEnabled(enabled);

        Composite subComposite = new Composite(parent, SWT.NONE);
        RowLayout subLayout = new RowLayout(SWT.HORIZONTAL);
        subComposite.setLayout(subLayout);
        Text newText = new Text(subComposite,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        newText.setEnabled(enabled);
        addTextListeners(newText, fieldListener, unsavedChangesListener);

        return newText;
    }

    /**
     * Create a textField in a new composite.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @return
     */
    protected static Text createTextWithComposite(Composite parent,
            String labelString, Listener fieldListener,
            Listener unsavedChangesListener) {

        return createTextWithComposite(parent, labelString, fieldListener,
                unsavedChangesListener, true);
    }

    /**
     * Create a qc textField in a new composite.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param qcLabelString
     *            label for the qc tooltip
     * @param qcType
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @return
     */
    protected static QCTextComp createTextWithComposite(Composite parent,
            String labelString, String qcLabelString, QCValueType qcType,
            Listener fieldListener, Listener unsavedChangesListener) {
        return createTextWithComposite(parent, labelString, qcLabelString,
                qcType, fieldListener, unsavedChangesListener, true);
    }

    /**
     * Create a qc textField.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param qcLabelString
     *            label for the qc tooltip
     * @param qcType
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @param tipEnabled
     *            true if the tooltip for qc text should be enabled, false
     *            otherwise
     * @return
     */
    protected static QCTextComp createText(Composite parent, String labelString,
            String qcLabelString, QCValueType qcType, Listener fieldListener,
            Listener unsavedChangesListener, boolean tipEnabled) {
        Label label = new Label(parent, SWT.NORMAL);
        label.setText(labelString);
        QCTextComp text = new QCTextComp(parent, SWT.NONE, qcLabelString,
                qcType, tipEnabled);

        addTextListeners(text, fieldListener, unsavedChangesListener);
        return text;
    }

    /**
     * Create a qc textField.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param qcLabelString
     *            label for the qc tooltip
     * @param qcType
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @return
     */
    protected static QCTextComp createText(Composite parent, String labelString,
            String qcLabelString, QCValueType qcType, Listener fieldListener,
            Listener unsavedChangesListener) {
        return createText(parent, labelString, qcLabelString, qcType,
                fieldListener, unsavedChangesListener, true);
    }

    /**
     * Create a textField.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @param enabled
     *            true if the text field should be enabled, false otherwise
     * @return
     */
    protected static Text createText(Composite parent, String labelString,
            Listener fieldListener, Listener unsavedChangesListener,
            boolean enabled) {
        Label label = new Label(parent, SWT.NORMAL);
        label.setText(labelString);
        label.setEnabled(enabled);
        Text newText = new Text(parent, ClimateLayoutValues.TEXT_FIELD_STYLE);

        addTextListeners(newText, fieldListener, unsavedChangesListener);
        newText.setEnabled(enabled);
        return newText;
    }

    /**
     * Create a textField.
     * 
     * @param parent
     * @param labelString
     *            label to the left of the text
     * @param fieldListener
     *            verify and focus listener added to the text field
     * @param unsavedChangesListener
     *            unsaved changes listener added to the text field
     * @return
     */
    protected static Text createText(Composite parent, String labelString,
            Listener fieldListener, Listener unsavedChangesListener) {
        return createText(parent, labelString, fieldListener,
                unsavedChangesListener, true);
    }

    /**
     * Add Verify, Focus, and modify Listeners text field.
     * 
     * @param text
     *            the text field to add the listeners to
     * @param fieldListener
     *            verify and focus listener
     * @param unsavedChangesListener
     *            unsaved changes modify listener
     */
    protected static void addTextListeners(Text text, Listener fieldListener,
            Listener unsavedChangesListener) {
        text.addListener(SWT.Verify, fieldListener);
        text.addListener(SWT.FocusOut, fieldListener);
        text.addListener(SWT.Modify, unsavedChangesListener);
    }

    /**
     * Add Verify, Focus, and modify Listeners to a text field.
     * 
     * @param text
     *            the text field to add the listeners to
     * @param fieldListener
     *            verify and focus listener
     * @param unsavedChangesListener
     *            unsaved changes modify listener
     */
    protected static void addTextListeners(QCTextComp text,
            Listener fieldListener, Listener unsavedChangesListener) {
        text.addListener(SWT.Verify, fieldListener);
        text.addListener(SWT.FocusOut, fieldListener);
        text.addListener(SWT.Modify, unsavedChangesListener);
    }
}