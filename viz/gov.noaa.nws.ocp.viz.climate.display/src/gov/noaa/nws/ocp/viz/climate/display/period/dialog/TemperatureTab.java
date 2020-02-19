/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog;

import java.text.ParseException;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.DataFieldListener;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.DataValueOrigin;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.MismatchLabel;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.comp.QCTextComp;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Temperature tab of the Period Display dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20 NOV 2017  41128      amoore      Initial creation.
 * 14 NOV 2018  DR20977    wpaintsil   Add NumberFormatException handling.
 * 14 DEC 2018  DR21053    wpaintsil   Data population missing for some fields.
 * 28 MAR 2019  DR21159    wpaintsil   Caution tooltip indicating unequal values
 *                                     Appears for temperature fields though those
 *                                     Values are close enough. E.g. "The MSM 
 *                                     value 17.3 does not match the Daily DB value 17.321428"
 * 30 APR 2019  DR21261    wpaintsil   Some temperature fields need to be rounded.
 * 13 JUN 2019  DR21417    wpaintsil   Float entry required for temperature averages.
 * 18 JUL 2019  DR21454    wpaintsil   Faulty conditional logic results in
 *                                     mismatch symbols appearing inappropriately.
 * 09 JAN 2020  DR21783    wpaintsil   Display Daily DB instead of MSM by default.
 * </pre>
 * 
 * @author amoore
 */
public class TemperatureTab extends DisplayStationPeriodTabItem {

    /**
     * Data value type selection for heating days.
     */
    private ComboViewer myHeatingDaysComboBox;

    /**
     * Data input of heating days.
     */
    private QCTextComp myHeatingDaysTF;

    /**
     * Data value type selection for cooling days.
     */
    private ComboViewer myCoolingDaysComboBox;

    /**
     * Data input of cooling days.
     */
    private QCTextComp myCoolingDaysTF;

    /**
     * Data value type selection for minimum temperature less than 32 degrees.
     */
    private ComboViewer myMinTempLess32DegComboBox;

    /**
     * Data input of minimum temperature less than 32 degrees.
     */
    private QCTextComp myMinTempLess32DegTF;

    /**
     * Data value type selection for minimum temperature less than 0 degrees.
     */
    private ComboViewer myMinTempLess0DegComboBox;

    /**
     * Data input of minimum temperature less than 0 degrees.
     */
    private QCTextComp myMinTempLess0DegTF;

    /**
     * Data input of minimum temperature greater than some custom value.
     */
    private Text myMinTempGreaterDegT4TF;

    /**
     * First data input of minimum temperature less than some custom value.
     */
    private Text myMinTempLessDegT5TF;

    /**
     * Second data input of minimum temperature less than some custom value.
     */
    private Text myMinTempLessDegT6TF;

    /**
     * Data value type selection for minimum temperature.
     */
    private ComboViewer myMinTempComboBox;

    /**
     * Data input for minimum temperature.
     */
    private QCTextComp myMinTempTF;

    /**
     * Date inputs for minimum temperature.
     */
    private DateSelectionComp[] myMinTempDates = new DateSelectionComp[3];

    /**
     * Data value type selection for average minimum temperature.
     */
    private ComboViewer myAvgMinTempComboBox;

    /**
     * Data input for average minimum temperature.
     */
    private QCTextComp myAvgMinTempTF;

    /**
     * Data input for mean relative humidity.
     */
    private Text myMeanRelHumTF;

    /**
     * Data value type selection for maximum temperature greater than 90
     * degrees.
     */
    private ComboViewer myMaxTempGreater90DegComboBox;

    /**
     * Data input for maximum temperature greater than 90 degrees.
     */
    private QCTextComp myMaxTempGreater90DegTF;

    /**
     * Data value type selection for maximum temperature less than 32 degrees.
     */
    private ComboViewer myMaxTempLess32DegComboBox;

    /**
     * Data input for maximum temperature less than 32 degrees.
     */
    private QCTextComp myMaxTempLess32DegTF;

    /**
     * First data input for maximum temperature greater than some custom value.
     */
    private Text myMaxTempGreaterDegT1TF;

    /**
     * Second data input for maximum temperature greater than some custom value.
     */
    private Text myMaxTempGreaterDegT2TF;

    /**
     * Data input for maximum temperature less than some custom value.
     */
    private Text myMaxTempLessDegT3TF;

    /**
     * Data value type selection for maximum temperature.
     */
    private ComboViewer myMaxTempComboBox;

    /**
     * Data input for maximum temperature.
     */
    private QCTextComp myMaxTempTF;

    /**
     * Dates of maximum temperature.
     */
    private DateSelectionComp[] myMaxTempDates = new DateSelectionComp[3];

    /**
     * Data value type selection for average maximum temperature.
     */
    private ComboViewer myAvgMaxTempComboBox;

    /**
     * Data input for average maximum temperature.
     */
    private QCTextComp myAvgMaxTempTF;

    /**
     * Data value type selection for mean temperature.
     */
    private ComboViewer myMeanTempComboBox;

    /**
     * Data input for mean temperature.
     */
    private QCTextComp myMeanTempTF;

    /**
     * Heating days label.
     */
    private MismatchLabel myHeatingDaysLabel;

    /**
     * Cooling days label.
     */
    private MismatchLabel myCoolingDaysLabel;

    /**
     * Minimum temp <= 32F label.
     */
    private MismatchLabel myMinTempLess32DegLabel;

    /**
     * Minimum temp <= 0F label.
     */
    private MismatchLabel myMinTempLess0DegLabel;

    /**
     * Minimum temp label.
     */
    private MismatchLabel myMinTempLbl;

    /**
     * Average minimum temp label.
     */
    private MismatchLabel myAvgMinTempLbl;

    /**
     * Maximum temp => 90F label.
     */
    private MismatchLabel myMaxTempGreater90DegLabel;

    /**
     * Maximum temp <= 32F label.
     */
    private MismatchLabel myMaxTempLess32DegLabel;

    /**
     * Maximum temp label.
     */
    private MismatchLabel myMaxTempLbl;

    /**
     * Average maximum temp label.
     */
    private MismatchLabel myAvgMaxTempLbl;

    /**
     * Mean temp label.
     */
    private MismatchLabel myMeanTempLbl;

    /**
     * 
     * @param parent
     * @param style
     * @param periodDialog
     */
    public TemperatureTab(TabFolder parent, int style,
            DisplayStationPeriodDialog periodDialog) {
        super(parent, style, periodDialog);

        myTabItem.setText("Temperature");
        Composite tempComp = new Composite(parent, SWT.NONE);
        RowLayout tempRL = new RowLayout(SWT.VERTICAL);
        tempRL.center = true;
        tempRL.marginLeft = 45;
        tempComp.setLayout(tempRL);
        myTabItem.setControl(tempComp);
        createTemperatureTab(tempComp);
    }

    /**
     * Create temperature tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTab(Composite parent) {
        Composite tempDataComp = new Composite(parent, SWT.NONE);
        GridLayout tempDataGL = new GridLayout(2, true);
        tempDataGL.horizontalSpacing = 20;
        tempDataComp.setLayout(tempDataGL);

        // left half, max temps
        createTemperatureTabLeft(tempDataComp);

        // right half, min temps
        createTemperatureTabRight(tempDataComp);

        // bottom, degree days
        createTemperatureTabBottom(parent);
    }

    /**
     * Create bottom part of temp tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabBottom(Composite parent) {
        Label degreeDaysLbl = new Label(parent, SWT.NORMAL);
        degreeDaysLbl.setText("Degree Days");

        Composite degreeDaysComp = new Composite(parent, SWT.BORDER);
        GridLayout degreeDaysGL = new GridLayout(2, true);
        degreeDaysComp.setLayout(degreeDaysGL);

        // heating days
        Composite heatingDaysComp = new Composite(degreeDaysComp, SWT.NONE);
        GridLayout heatingDaysGL = new GridLayout(3, false);
        heatingDaysComp.setLayout(heatingDaysGL);

        myHeatingDaysLabel = new MismatchLabel(heatingDaysComp, SWT.NORMAL);
        myHeatingDaysLabel.setText("Heating Degree Days");

        myHeatingDaysComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(heatingDaysComp);
        myHeatingDaysComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myHeatingDaysComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myHeatingDaysTF.setTextAndTip(
                                String.valueOf(iData.getNumHeatTotal()),
                                iData.getDataMethods().getHeatQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myHeatingDaysTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordHeatingDegreeDays(data);
                        if (data.getNumHeatTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                            data.getDataMethods()
                                    .setHeatQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods()
                                    .setHeatQc(ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myHeatingDaysTF = new QCTextComp(heatingDaysComp, SWT.NONE,
                "Heating Degree Days", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myHeatingDaysTF.useGridData();
        myHeatingDaysTF.addListener(SWT.Verify,
                myDisplayListeners.getDegreeDaysListener());
        myHeatingDaysTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDegreeDaysListener());
        myHeatingDaysTF.addKeyListener(new DataFieldListener(
                myHeatingDaysComboBox, myPeriodDialog.getChangeListener()));

        // cooling days
        Composite coolingDaysComp = new Composite(degreeDaysComp, SWT.NONE);
        GridLayout coolingDaysGL = new GridLayout(3, false);
        coolingDaysComp.setLayout(coolingDaysGL);

        myCoolingDaysLabel = new MismatchLabel(coolingDaysComp, SWT.NORMAL);
        myCoolingDaysLabel.setText("Cooling Degree Days");

        myCoolingDaysComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(coolingDaysComp);
        myCoolingDaysComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myCoolingDaysComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myCoolingDaysTF.setTextAndTip(
                                String.valueOf(iData.getNumCoolTotal()),
                                iData.getDataMethods().getCoolQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myCoolingDaysTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordCoolingDegreeDays(data);
                        if (data.getNumCoolTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                            data.getDataMethods()
                                    .setCoolQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods()
                                    .setCoolQc(ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myCoolingDaysTF = new QCTextComp(coolingDaysComp, SWT.NONE,
                "Cooling Degree Days", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myCoolingDaysTF.useGridData();
        myCoolingDaysTF.addListener(SWT.Verify,
                myDisplayListeners.getDegreeDaysListener());
        myCoolingDaysTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDegreeDaysListener());
        myCoolingDaysTF.addKeyListener(new DataFieldListener(
                myCoolingDaysComboBox, myPeriodDialog.getChangeListener()));
    }

    /**
     * Create left half of temp tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabRight(Composite parent) {
        Composite tempDataRight = new Composite(parent, SWT.BORDER);
        RowLayout tempDataRightRL = new RowLayout(SWT.VERTICAL);
        tempDataRightRL.center = true;
        tempDataRight.setLayout(tempDataRightRL);

        createTemperatureTabRightTop(tempDataRight);

        createTemperatureTabRightBottom(tempDataRight);
    }

    /**
     * Create bottom of right half of temperature tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabRightBottom(Composite parent) {
        Composite tempDataRightBot = new Composite(parent, SWT.NONE);
        RowLayout tempDataRightBotRL = new RowLayout(SWT.VERTICAL);
        tempDataRightBotRL.center = true;
        tempDataRightBot.setLayout(tempDataRightBotRL);

        Label tempDataRightBotLbl = new Label(tempDataRightBot, SWT.NORMAL);
        tempDataRightBotLbl.setText("Days with Minimum Temperature");

        Composite tempDataRightBotFields = new Composite(tempDataRightBot,
                SWT.NONE);
        GridLayout tempDataRightBotFieldsGL = new GridLayout(3, false);
        tempDataRightBotFields.setLayout(tempDataRightBotFieldsGL);

        myMinTempLess32DegLabel = new MismatchLabel(tempDataRightBotFields,
                SWT.NORMAL);
        myMinTempLess32DegLabel.setText("32 deg F or Lower");

        myMinTempLess32DegComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataRightBotFields);
        myMinTempLess32DegComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMinTempLess32DegComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMinTempLess32DegTF.setTextAndTip(
                                String.valueOf(iData.getNumMinLessThan32F()),
                                iData.getDataMethods().getMinLE32Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myMinTempLess32DegTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordMinTempLess32(data);
                        if (data.getNumMinLessThan32F() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setMinLE32Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setMinLE32Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myMinTempLess32DegTF = new QCTextComp(tempDataRightBotFields, SWT.NONE,
                "32 deg F or Lower", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMinTempLess32DegTF.useGridData();
        myMinTempLess32DegTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLess32DegTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLess32DegTF.addKeyListener(
                new DataFieldListener(myMinTempLess32DegComboBox,
                        myPeriodDialog.getChangeListener()));

        myMinTempLess0DegLabel = new MismatchLabel(tempDataRightBotFields,
                SWT.NORMAL);
        myMinTempLess0DegLabel.setText("0 deg F or Lower");

        myMinTempLess0DegComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataRightBotFields);
        myMinTempLess0DegComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMinTempLess0DegComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMinTempLess0DegTF.setTextAndTip(
                                String.valueOf(iData.getNumMinLessThan0F()),
                                iData.getDataMethods().getMinLE0Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myMinTempLess0DegTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordMinTempLess0(data);
                        if (data.getNumMinLessThan0F() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setMinLE0Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setMinLE0Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myMinTempLess0DegTF = new QCTextComp(tempDataRightBotFields, SWT.NONE,
                "0 deg F or Lower", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMinTempLess0DegTF.useGridData();
        myMinTempLess0DegTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLess0DegTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLess0DegTF.addKeyListener(new DataFieldListener(
                myMinTempLess0DegComboBox, myPeriodDialog.getChangeListener()));

        Label minTempGreaterDegT4Label = new Label(tempDataRightBotFields,
                SWT.NORMAL);

        myMinTempGreaterDegT4TF = new Text(tempDataRightBotFields,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMinTempGreaterDegT4TF);
        myMinTempGreaterDegT4TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMinTempGreaterDegT4TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMinTempGreaterDegT4TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getT4() == ParameterFormatClimate.MISSING) {
            minTempGreaterDegT4Label.setText("?? deg F or Higher");
            minTempGreaterDegT4Label.setEnabled(false);
            myMinTempGreaterDegT4TF.setEnabled(false);
        } else {
            minTempGreaterDegT4Label.setText(
                    myPeriodDialog.myGlobals.getT4() + " deg F or Higher");
        }

        Composite minTempGreaterDegT4Filler = new Composite(
                tempDataRightBotFields, SWT.NONE);
        minTempGreaterDegT4Filler
                .setLayoutData(ClimateLayoutValues.getFillerGD());

        Label minTempLessDegT5Label = new Label(tempDataRightBotFields,
                SWT.NORMAL);

        myMinTempLessDegT5TF = new Text(tempDataRightBotFields,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMinTempLessDegT5TF);
        myMinTempLessDegT5TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLessDegT5TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLessDegT5TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getT5() == ParameterFormatClimate.MISSING) {
            minTempLessDegT5Label.setText("?? deg F or Lower");
            minTempLessDegT5Label.setEnabled(false);
            myMinTempLessDegT5TF.setEnabled(false);
        } else {
            minTempLessDegT5Label.setText(
                    myPeriodDialog.myGlobals.getT5() + " deg F or Lower");
        }

        Composite minTempLessDegT5Filler = new Composite(tempDataRightBotFields,
                SWT.NONE);
        minTempLessDegT5Filler.setLayoutData(ClimateLayoutValues.getFillerGD());

        Label minTempLessDegT6Label = new Label(tempDataRightBotFields,
                SWT.NORMAL);

        myMinTempLessDegT6TF = new Text(tempDataRightBotFields,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMinTempLessDegT6TF);
        myMinTempLessDegT6TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLessDegT6TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMinTempLessDegT6TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getT6() == ParameterFormatClimate.MISSING) {
            minTempLessDegT6Label.setText("?? deg F or Lower");
            minTempLessDegT6Label.setEnabled(false);
            myMinTempLessDegT6TF.setEnabled(false);
        } else {
            minTempLessDegT6Label.setText(
                    myPeriodDialog.myGlobals.getT6() + " deg F or Lower");
        }

        Composite minTempLessDegT6Filler = new Composite(tempDataRightBotFields,
                SWT.NONE);
        minTempLessDegT6Filler.setLayoutData(ClimateLayoutValues.getFillerGD());
    }

    /**
     * Create top of right half of temperature tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabRightTop(Composite parent) {
        // right top
        Composite tempDataRightTop = new Composite(parent, SWT.NONE);
        GridLayout tempDataRightTopGL = new GridLayout(3, false);
        tempDataRightTop.setLayout(tempDataRightTopGL);

        myMinTempLbl = new MismatchLabel(tempDataRightTop, SWT.NORMAL);
        myMinTempLbl.setText("Minimum Temperature (F)");

        myMinTempComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataRightTop);
        myMinTempComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMinTempComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMinTempTF.setTextAndTip(
                                String.valueOf(iData.getMinTemp()),
                                iData.getDataMethods().getMinTempQc());
                        int i = 0;
                        for (i = 0; i < myMinTempDates.length
                                && i < iData.getDayMinTempList().size(); i++) {
                            myMinTempDates[i]
                                    .setDate(iData.getDayMinTempList().get(i));
                        }
                        // clear any remaining fields
                        for (int j = i; j < myMinTempDates.length; j++) {
                            myMinTempDates[j].setMissing();
                        }
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        if (myMinTempTF.isFocusControl()) {
                            return true;
                        }
                        for (int i = 0; i < myMinTempDates.length; i++) {
                            if (myMinTempDates[i].isFocusControl()) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    protected void saveOtherData() {
                        try {
                            PeriodData data = getOtherPeriodData();
                            recordMinTemp(data);
                            if (data.getMinTemp() != ParameterFormatClimate.MISSING) {
                                data.getDataMethods()
                                        .setMinTempQc(QCValues.MANUAL_ENTRY);
                            } else {
                                data.getDataMethods().setMinTempQc(
                                        ParameterFormatClimate.MISSING);
                            }
                        } catch (ParseException e) {
                            logger.error("Error parsing date.", e);
                        }
                    }
                });

        myMinTempTF = new QCTextComp(tempDataRightTop, SWT.NONE,
                "Minimum Temperature (F)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMinTempTF.useGridData();
        myMinTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        myMinTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        myMinTempTF.addKeyListener(new DataFieldListener(myMinTempComboBox,
                myPeriodDialog.getChangeListener()));

        // dates of min temp
        Label minTempDatesLbl = new Label(tempDataRightTop, SWT.NORMAL);
        minTempDatesLbl.setText("Dates(s) of\nMinimum Temperature");

        Composite minTempDatesComp = new Composite(tempDataRightTop, SWT.NONE);
        GridLayout minTempDatesGL = new GridLayout(1, false);
        minTempDatesComp.setLayout(minTempDatesGL);

        myMinTempDates[0] = new DateSelectionComp(minTempDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        DataFieldListener minTempDatesListener = new DataFieldListener(
                myMinTempComboBox, myPeriodDialog.getChangeListener());
        myMinTempDates[0].addKeyListener(minTempDatesListener);
        myMinTempDates[0].addSelectionListener(minTempDatesListener);

        myMinTempDates[1] = new DateSelectionComp(minTempDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMinTempDates[1].addKeyListener(minTempDatesListener);
        myMinTempDates[1].addSelectionListener(minTempDatesListener);

        myMinTempDates[2] = new DateSelectionComp(minTempDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMinTempDates[2].addKeyListener(minTempDatesListener);
        myMinTempDates[2].addSelectionListener(minTempDatesListener);

        Composite minTempDatesFillerComp = new Composite(tempDataRightTop,
                SWT.NONE);
        minTempDatesFillerComp.setLayoutData(ClimateLayoutValues.getFillerGD());

        myAvgMinTempLbl = new MismatchLabel(tempDataRightTop, SWT.NORMAL);
        myAvgMinTempLbl.setText("Average\nMinimum Temperature (F)");

        myAvgMinTempComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataRightTop);
        myAvgMinTempComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myAvgMinTempComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myAvgMinTempTF.setTextAndTip(
                                String.valueOf(ClimateUtilities
                                        .nint(iData.getMinTempMean(), 1)),
                                iData.getDataMethods().getAvgMinTempQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myAvgMinTempTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordAvgMinTemp(data);
                        if (data.getMinTempMean() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setAvgMinTempQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setAvgMinTempQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myAvgMinTempTF = new QCTextComp(tempDataRightTop, SWT.NONE,
                "Average Minimum Temperature (F)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myAvgMinTempTF.useGridData();
        myAvgMinTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        myAvgMinTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        myAvgMinTempTF.addKeyListener(new DataFieldListener(
                myAvgMinTempComboBox, myPeriodDialog.getChangeListener()));

        // mean relative humidity
        Label meanRelHumLbl = new Label(tempDataRightTop, SWT.NORMAL);
        meanRelHumLbl.setText("Mean Relative Humidity");

        Composite meanRelHumComp = new Composite(tempDataRightTop, SWT.NONE);
        RowLayout meanRelHumRL = new RowLayout(SWT.HORIZONTAL);
        meanRelHumRL.center = true;
        meanRelHumComp.setLayout(meanRelHumRL);

        myMeanRelHumTF = new Text(meanRelHumComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMeanRelHumTF);
        myMeanRelHumTF.addListener(SWT.Verify,
                myDisplayListeners.getRelHumListener());
        myMeanRelHumTF.addListener(SWT.FocusOut,
                myDisplayListeners.getRelHumListener());
        myMeanRelHumTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());
    }

    /**
     * Create left half of temp tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabLeft(Composite parent) {
        Composite tempDataLeft = new Composite(parent, SWT.BORDER);
        RowLayout tempDataLeftRL = new RowLayout(SWT.VERTICAL);
        tempDataLeftRL.center = true;
        tempDataLeft.setLayout(tempDataLeftRL);

        createTemperatureTabLeftTop(tempDataLeft);

        createTemperatureTabLeftBottom(tempDataLeft);
    }

    /**
     * Create bottom of left half of temperature tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabLeftBottom(Composite parent) {
        // left bottom
        Composite tempDataLeftBot = new Composite(parent, SWT.NONE);
        RowLayout tempDataLeftBotRL = new RowLayout(SWT.VERTICAL);
        tempDataLeftBotRL.center = true;
        tempDataLeftBot.setLayout(tempDataLeftBotRL);

        Label tempDataLeftBotLbl = new Label(tempDataLeftBot, SWT.NORMAL);
        tempDataLeftBotLbl.setText("Days with Maximum Temperature");

        Composite tempDataLeftBotFields = new Composite(tempDataLeftBot,
                SWT.NONE);
        GridLayout tempDataLeftBotFieldsGL = new GridLayout(3, false);
        tempDataLeftBotFields.setLayout(tempDataLeftBotFieldsGL);

        myMaxTempGreater90DegLabel = new MismatchLabel(tempDataLeftBotFields,
                SWT.NORMAL);
        myMaxTempGreater90DegLabel.setText("90 deg F or Higher");

        myMaxTempGreater90DegComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataLeftBotFields);
        myMaxTempGreater90DegComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myMaxTempGreater90DegComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMaxTempGreater90DegTF.setTextAndTip(
                                String.valueOf(iData.getNumMaxGreaterThan90F()),
                                iData.getDataMethods().getMaxTempGE90Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myMaxTempGreater90DegTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordMaxTempGreater90(data);
                        if (data.getNumMaxGreaterThan90F() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setMaxTempGE90Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setMaxTempGE90Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myMaxTempGreater90DegTF = new QCTextComp(tempDataLeftBotFields,
                SWT.NONE, "90 deg F or Higher", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMaxTempGreater90DegTF.useGridData();
        myMaxTempGreater90DegTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempGreater90DegTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempGreater90DegTF.addKeyListener(
                new DataFieldListener(myMaxTempGreater90DegComboBox,
                        myPeriodDialog.getChangeListener()));

        myMaxTempLess32DegLabel = new MismatchLabel(tempDataLeftBotFields,
                SWT.NORMAL);
        myMaxTempLess32DegLabel.setText("32 deg F or Lower");

        myMaxTempLess32DegComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataLeftBotFields);
        myMaxTempLess32DegComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMaxTempLess32DegComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMaxTempLess32DegTF.setTextAndTip(
                                String.valueOf(iData.getNumMaxLessThan32F()),
                                iData.getDataMethods().getMaxTempLE32Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myMaxTempLess32DegTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordMaxTempLess32(data);
                        if (data.getNumMaxLessThan32F() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setMaxTempLE32Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setMaxTempLE32Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myMaxTempLess32DegTF = new QCTextComp(tempDataLeftBotFields, SWT.NONE,
                "32 deg F or Lower", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMaxTempLess32DegTF.useGridData();
        myMaxTempLess32DegTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempLess32DegTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempLess32DegTF.addKeyListener(
                new DataFieldListener(myMaxTempLess32DegComboBox,
                        myPeriodDialog.getChangeListener()));

        Label maxTempGreaterDegT1Label = new Label(tempDataLeftBotFields,
                SWT.NORMAL);

        myMaxTempGreaterDegT1TF = new Text(tempDataLeftBotFields,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMaxTempGreaterDegT1TF);
        myMaxTempGreaterDegT1TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempGreaterDegT1TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempGreaterDegT1TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getT1() == ParameterFormatClimate.MISSING) {
            maxTempGreaterDegT1Label.setText("?? deg F or Higher");
            maxTempGreaterDegT1Label.setEnabled(false);
            myMaxTempGreaterDegT1TF.setEnabled(false);
        } else {
            maxTempGreaterDegT1Label.setText(
                    myPeriodDialog.myGlobals.getT1() + " deg F or Higher");
        }

        Composite maxTempGreaterDegT1Filler = new Composite(
                tempDataLeftBotFields, SWT.NONE);
        maxTempGreaterDegT1Filler
                .setLayoutData(ClimateLayoutValues.getFillerGD());

        Label maxTempGreaterDegT2Label = new Label(tempDataLeftBotFields,
                SWT.NORMAL);

        myMaxTempGreaterDegT2TF = new Text(tempDataLeftBotFields,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMaxTempGreaterDegT2TF);
        myMaxTempGreaterDegT2TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempGreaterDegT2TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempGreaterDegT2TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getT2() == ParameterFormatClimate.MISSING) {
            maxTempGreaterDegT2Label.setText("?? deg F or Higher");
            maxTempGreaterDegT2Label.setEnabled(false);
            myMaxTempGreaterDegT2TF.setEnabled(false);
        } else {
            maxTempGreaterDegT2Label.setText(
                    myPeriodDialog.myGlobals.getT2() + " deg F or Higher");
        }

        Composite maxTempGreaterDegT2Filler = new Composite(
                tempDataLeftBotFields, SWT.NONE);
        maxTempGreaterDegT2Filler
                .setLayoutData(ClimateLayoutValues.getFillerGD());

        Label maxTempLessDegT3Label = new Label(tempDataLeftBotFields,
                SWT.NORMAL);

        myMaxTempLessDegT3TF = new Text(tempDataLeftBotFields,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMaxTempLessDegT3TF);
        myMaxTempLessDegT3TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempLessDegT3TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMaxTempLessDegT3TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getT3() == ParameterFormatClimate.MISSING) {
            maxTempLessDegT3Label.setText("?? deg F or Lower");
            maxTempLessDegT3Label.setEnabled(false);
            myMaxTempLessDegT3TF.setEnabled(false);
        } else {
            maxTempLessDegT3Label.setText(
                    myPeriodDialog.myGlobals.getT3() + " deg F or Lower");
        }

        Composite maxTempLessDegT3Filler = new Composite(tempDataLeftBotFields,
                SWT.NONE);
        maxTempLessDegT3Filler.setLayoutData(ClimateLayoutValues.getFillerGD());
    }

    /**
     * Create top of left half of temperature tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTemperatureTabLeftTop(Composite parent) {
        // left top
        Composite tempDataLeftTop = new Composite(parent, SWT.NONE);
        GridLayout tempDataLeftTopGL = new GridLayout(3, false);
        tempDataLeftTop.setLayout(tempDataLeftTopGL);

        myMaxTempLbl = new MismatchLabel(tempDataLeftTop, SWT.NORMAL);
        myMaxTempLbl.setText("Maximum Temperature (F)");

        myMaxTempComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataLeftTop);
        myMaxTempComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMaxTempComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMaxTempTF.setTextAndTip(
                                String.valueOf(iData.getMaxTemp()),
                                iData.getDataMethods().getMaxTempQc());
                        int i = 0;
                        for (i = 0; i < myMaxTempDates.length
                                && i < iData.getDayMaxTempList().size(); i++) {
                            myMaxTempDates[i]
                                    .setDate(iData.getDayMaxTempList().get(i));
                        }
                        // clear any remaining fields
                        for (int j = i; j < myMaxTempDates.length; j++) {
                            myMaxTempDates[j].setMissing();
                        }
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        if (myMaxTempTF.isFocusControl()) {
                            return true;
                        }
                        for (int i = 0; i < myMaxTempDates.length; i++) {
                            if (myMaxTempDates[i].isFocusControl()) {
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    protected void saveOtherData() {
                        try {
                            PeriodData data = getOtherPeriodData();
                            recordMaxTemp(data);
                            if (data.getMaxTemp() != ParameterFormatClimate.MISSING) {
                                data.getDataMethods()
                                        .setMaxTempQc(QCValues.MANUAL_ENTRY);
                            } else {
                                data.getDataMethods().setMaxTempQc(
                                        ParameterFormatClimate.MISSING);
                            }
                        } catch (ParseException e) {
                            logger.error("Error parsing date.", e);
                        }
                    }
                });

        myMaxTempTF = new QCTextComp(tempDataLeftTop, SWT.NONE,
                "Maximum Temperature (F)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMaxTempTF.useGridData();
        myMaxTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        myMaxTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        myMaxTempTF.addKeyListener(new DataFieldListener(myMaxTempComboBox,
                myPeriodDialog.getChangeListener()));

        // dates of max temp
        Label maxTempDatesLbl = new Label(tempDataLeftTop, SWT.NORMAL);
        maxTempDatesLbl.setText("Dates(s) of\nMaximum Temperature");

        Composite maxTempDatesComp = new Composite(tempDataLeftTop, SWT.NONE);
        GridLayout maxTempDatesRL = new GridLayout(1, false);
        maxTempDatesComp.setLayout(maxTempDatesRL);

        myMaxTempDates[0] = new DateSelectionComp(maxTempDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        DataFieldListener maxTempDatesListener = new DataFieldListener(
                myMaxTempComboBox, myPeriodDialog.getChangeListener());
        myMaxTempDates[0].addKeyListener(maxTempDatesListener);
        myMaxTempDates[0].addSelectionListener(maxTempDatesListener);

        myMaxTempDates[1] = new DateSelectionComp(maxTempDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxTempDates[1].addKeyListener(maxTempDatesListener);
        myMaxTempDates[1].addSelectionListener(maxTempDatesListener);

        myMaxTempDates[2] = new DateSelectionComp(maxTempDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxTempDates[2].addKeyListener(maxTempDatesListener);
        myMaxTempDates[2].addSelectionListener(maxTempDatesListener);

        Composite maxTempDatesFillerComp = new Composite(tempDataLeftTop,
                SWT.NONE);
        maxTempDatesFillerComp.setLayoutData(ClimateLayoutValues.getFillerGD());

        myAvgMaxTempLbl = new MismatchLabel(tempDataLeftTop, SWT.NORMAL);
        myAvgMaxTempLbl.setText("Average\nMaximum Temperature (F)");

        myAvgMaxTempComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataLeftTop);
        myAvgMaxTempComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myAvgMaxTempComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myAvgMaxTempTF.setTextAndTip(
                                String.valueOf(ClimateUtilities
                                        .nint(iData.getMaxTempMean(), 1)),
                                iData.getDataMethods().getAvgMaxTempQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myAvgMaxTempTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordAvgMaxTemp(data);
                        if (data.getMaxTempMean() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setAvgMaxTempQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setAvgMaxTempQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myAvgMaxTempTF = new QCTextComp(tempDataLeftTop, SWT.NONE,
                "Average Maximum Temperature (F)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myAvgMaxTempTF.useGridData();
        myAvgMaxTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        myAvgMaxTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        myAvgMaxTempTF.addKeyListener(new DataFieldListener(
                myAvgMaxTempComboBox, myPeriodDialog.getChangeListener()));

        myMeanTempLbl = new MismatchLabel(tempDataLeftTop, SWT.NORMAL);
        myMeanTempLbl.setText("Mean Temperature (F)");

        myMeanTempComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(tempDataLeftTop);
        myMeanTempComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMeanTempComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMeanTempTF.setTextAndTip(
                                String.valueOf(ClimateUtilities
                                        .nint(iData.getMeanTemp(), 1)),
                                iData.getDataMethods().getMeanTempQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myMeanTempTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordMeanTemp(data);
                        if (data.getMeanTemp() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setMeanTempQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setMeanTempQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myMeanTempTF = new QCTextComp(tempDataLeftTop, SWT.NONE,
                "Mean Temperature (F)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMeanTempTF.useGridData();
        myMeanTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        myMeanTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        myMeanTempTF.addKeyListener(new DataFieldListener(myMeanTempComboBox,
                myPeriodDialog.getChangeListener()));
    }

    @Override
    protected void saveValues(PeriodData dataToSave)
            throws VizException, NumberFormatException, ParseException {
        recordHeatingDegreeDays(dataToSave);
        recordCoolingDegreeDays(dataToSave);

        recordMinTempLess32(dataToSave);
        recordMinTempLess0(dataToSave);

        if (myMinTempGreaterDegT4TF.isEnabled()) {
            dataToSave.setNumMinGreaterThanT4F(ClimateGUIUtils
                    .parseInt(myMinTempGreaterDegT4TF.getText()));
        }

        if (myMinTempLessDegT5TF.isEnabled()) {
            dataToSave.setNumMinLessThanT5F(
                    ClimateGUIUtils.parseInt(myMinTempLessDegT5TF.getText()));
        }

        if (myMinTempLessDegT6TF.isEnabled()) {
            dataToSave.setNumMinLessThanT6F(
                    ClimateGUIUtils.parseInt(myMinTempLessDegT6TF.getText()));
        }

        recordMinTemp(dataToSave);

        recordAvgMinTemp(dataToSave);

        dataToSave
                .setMeanRh(ClimateGUIUtils.parseInt(myMeanRelHumTF.getText()));

        recordMaxTempGreater90(dataToSave);
        recordMaxTempLess32(dataToSave);

        if (myMaxTempGreaterDegT1TF.isEnabled()) {
            dataToSave.setNumMaxGreaterThanT1F(ClimateGUIUtils
                    .parseInt(myMaxTempGreaterDegT1TF.getText()));
        }

        if (myMaxTempGreaterDegT2TF.isEnabled()) {
            dataToSave.setNumMaxGreaterThanT2F(ClimateGUIUtils
                    .parseInt(myMaxTempGreaterDegT2TF.getText()));
        }

        if (myMaxTempLessDegT3TF.isEnabled()) {
            dataToSave.setNumMaxLessThanT3F(
                    ClimateGUIUtils.parseInt(myMaxTempLessDegT3TF.getText()));
        }

        recordMaxTemp(dataToSave);

        recordAvgMaxTemp(dataToSave);

        recordMeanTemp(dataToSave);
    }

    @Override
    protected boolean loadSavedData(PeriodData iSavedPeriodData,
            PeriodData msmPeriodData, PeriodData dailyPeriodData) {
        boolean tempTabMismatch = false;

        // max temp
        if (dailyPeriodData != null && isPeriodDataEqualForInt(
                iSavedPeriodData.getMaxTemp(), dailyPeriodData.getMaxTemp(),
                iSavedPeriodData.getDayMaxTempList(),
                dailyPeriodData.getDayMaxTempList(), myMaxTempDates.length)) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myMaxTempComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && isPeriodDataEqualForInt(
                iSavedPeriodData.getMaxTemp(), msmPeriodData.getMaxTemp(),
                iSavedPeriodData.getDayMaxTempList(),
                msmPeriodData.getDayMaxTempList(), myMaxTempDates.length)) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(myMaxTempComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMaxTempComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getMaxTemp() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getMaxTemp() != ParameterFormatClimate.MISSING)) {
            if (msmPeriodData.getMaxTemp() != dailyPeriodData.getMaxTemp()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxTempLbl.setNotMatched(msmPeriodData.getMaxTemp(),
                        dailyPeriodData.getMaxTemp());
                tempTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxTempDates.length,
                        dailyPeriodData.getDayMaxTempList(),
                        msmPeriodData.getDayMaxTempList())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxTempLbl.setNotMatched(
                            msmPeriodData.getDayMaxTempList()
                                    .toArray(new ClimateDate[msmPeriodData
                                            .getDayMaxTempList().size()]),
                            dailyPeriodData.getDayMaxTempList()
                                    .toArray(new ClimateDate[dailyPeriodData
                                            .getDayMaxTempList().size()]));
                    tempTabMismatch = true;
                } else {
                    myMaxTempLbl.setMatched();
                }
            }
        } else {
            myMaxTempLbl.setMatched();
        }

        // average max temp
        if (dailyPeriodData != null && ClimateUtilities.floatingEquals(
                ClimateUtilities.nint(iSavedPeriodData.getMaxTempMean(), 1),
                ClimateUtilities.nint(dailyPeriodData.getMaxTempMean(), 1))) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myAvgMaxTempComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && ClimateUtilities.floatingEquals(
                ClimateUtilities.nint(iSavedPeriodData.getMaxTempMean(), 1),
                ClimateUtilities.nint(msmPeriodData.getMaxTempMean(), 1))) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(myAvgMaxTempComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myAvgMaxTempComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (!ClimateUtilities.floatingEquals(
                        dailyPeriodData.getMaxTempMean(),
                        ParameterFormatClimate.MISSING))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getMaxTempMean(),
                        ParameterFormatClimate.MISSING))
                && (!ClimateUtilities.floatingEquals(
                        ClimateUtilities.nint(msmPeriodData.getMaxTempMean(),
                                1),
                        ClimateUtilities.nint(dailyPeriodData.getMaxTempMean(),
                                1)))) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myAvgMaxTempLbl.setNotMatched(msmPeriodData.getMaxTempMean(),
                    dailyPeriodData.getMaxTempMean());
            tempTabMismatch = true;
        } else {
            myAvgMaxTempLbl.setMatched();
        }

        // average temp
        if (dailyPeriodData != null && ClimateUtilities.floatingEquals(
                ClimateUtilities.nint(iSavedPeriodData.getMeanTemp(), 1),
                ClimateUtilities.nint(dailyPeriodData.getMeanTemp(), 1))) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myMeanTempComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && ClimateUtilities.floatingEquals(
                iSavedPeriodData.getMeanTemp(), msmPeriodData.getMeanTemp())) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(myMeanTempComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMeanTempComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (!ClimateUtilities.floatingEquals(
                        dailyPeriodData.getMeanTemp(),
                        ParameterFormatClimate.MISSING))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getMeanTemp(),
                        ParameterFormatClimate.MISSING))
                && (!ClimateUtilities.floatingEquals(
                        ClimateUtilities.nint(msmPeriodData.getMeanTemp(), 1),
                        ClimateUtilities.nint(dailyPeriodData.getMeanTemp(),
                                1)))) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myMeanTempLbl.setNotMatched(msmPeriodData.getMeanTemp(),
                    dailyPeriodData.getMeanTemp());
            tempTabMismatch = true;
        } else {
            myMeanTempLbl.setMatched();
        }

        // max temp over 90
        if (dailyPeriodData != null
                && iSavedPeriodData.getNumMaxGreaterThan90F() == dailyPeriodData
                        .getNumMaxGreaterThan90F()) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(
                    myMaxTempGreater90DegComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null
                && iSavedPeriodData.getNumMaxGreaterThan90F() == msmPeriodData
                        .getNumMaxGreaterThan90F()) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(
                    myMaxTempGreater90DegComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myMaxTempGreater90DegComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumMaxGreaterThan90F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumMaxGreaterThan90F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumMaxGreaterThan90F() != dailyPeriodData
                        .getNumMaxGreaterThan90F())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myMaxTempGreater90DegLabel.setNotMatched(
                    msmPeriodData.getNumMaxGreaterThan90F(),
                    dailyPeriodData.getNumMaxGreaterThan90F());
            tempTabMismatch = true;
        } else {
            myMaxTempGreater90DegLabel.setMatched();
        }

        // max temp under 32
        if (dailyPeriodData != null
                && iSavedPeriodData.getNumMaxLessThan32F() == dailyPeriodData
                        .getNumMaxLessThan32F()) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(
                    myMaxTempLess32DegComboBox, DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null
                && iSavedPeriodData.getNumMaxLessThan32F() == msmPeriodData
                        .getNumMaxLessThan32F()) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(
                    myMaxTempLess32DegComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myMaxTempLess32DegComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumMaxLessThan32F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumMaxLessThan32F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumMaxLessThan32F() != dailyPeriodData
                        .getNumMaxLessThan32F())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myMaxTempLess32DegLabel.setNotMatched(
                    msmPeriodData.getNumMaxLessThan32F(),
                    dailyPeriodData.getNumMaxLessThan32F());
            tempTabMismatch = true;
        } else {
            myMaxTempLess32DegLabel.setMatched();
        }

        // max temp t1
        if (myMaxTempGreaterDegT1TF.isEnabled()) {
            myMaxTempGreaterDegT1TF.setText(
                    String.valueOf(iSavedPeriodData.getNumMaxGreaterThanT1F()));
        }

        // max temp t2
        if (myMaxTempGreaterDegT2TF.isEnabled()) {
            myMaxTempGreaterDegT2TF.setText(
                    String.valueOf(iSavedPeriodData.getNumMaxGreaterThanT2F()));
        }

        // max temp t3
        if (myMaxTempLessDegT3TF.isEnabled()) {
            myMaxTempLessDegT3TF.setText(
                    String.valueOf(iSavedPeriodData.getNumMaxLessThanT3F()));
        }

        // min temp
        if (dailyPeriodData != null && isPeriodDataEqualForInt(
                iSavedPeriodData.getMinTemp(), dailyPeriodData.getMinTemp(),
                iSavedPeriodData.getDayMinTempList(),
                dailyPeriodData.getDayMinTempList(), myMinTempDates.length)) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myMinTempComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && isPeriodDataEqualForInt(
                iSavedPeriodData.getMinTemp(), msmPeriodData.getMinTemp(),
                iSavedPeriodData.getDayMinTempList(),
                msmPeriodData.getDayMinTempList(), myMinTempDates.length)) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(myMinTempComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMinTempComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getMinTemp() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getMinTemp() != ParameterFormatClimate.MISSING)) {
            if (msmPeriodData.getMinTemp() != dailyPeriodData.getMinTemp()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMinTempLbl.setNotMatched(msmPeriodData.getMinTemp(),
                        dailyPeriodData.getMinTemp());
                tempTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMinTempDates.length,
                        dailyPeriodData.getDayMinTempList(),
                        msmPeriodData.getDayMinTempList())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMinTempLbl.setNotMatched(
                            msmPeriodData.getDayMinTempList()
                                    .toArray(new ClimateDate[msmPeriodData
                                            .getDayMinTempList().size()]),
                            dailyPeriodData.getDayMinTempList()
                                    .toArray(new ClimateDate[dailyPeriodData
                                            .getDayMinTempList().size()]));
                    tempTabMismatch = true;
                } else {
                    myMinTempLbl.setMatched();
                }
            }
        } else {
            myMinTempLbl.setMatched();
        }

        // average min temp
        if (dailyPeriodData != null && ClimateUtilities.floatingEquals(
                ClimateUtilities.nint(iSavedPeriodData.getMinTempMean(), 1),
                ClimateUtilities.nint(dailyPeriodData.getMinTempMean(), 1))) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myAvgMinTempComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && ClimateUtilities.floatingEquals(
                ClimateUtilities.nint(iSavedPeriodData.getMinTempMean(), 1),
                ClimateUtilities.nint(msmPeriodData.getMinTempMean(), 1))) {
            // check MSM first
            DataFieldListener.setComboViewerSelection(myAvgMinTempComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myAvgMinTempComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (!ClimateUtilities.floatingEquals(
                        dailyPeriodData.getMinTempMean(),
                        ParameterFormatClimate.MISSING))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getMinTempMean(),
                        ParameterFormatClimate.MISSING))
                && (!ClimateUtilities.floatingEquals(
                        ClimateUtilities.nint(msmPeriodData.getMinTempMean(),
                                1),
                        ClimateUtilities.nint(dailyPeriodData.getMinTempMean(),
                                1)))) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myAvgMinTempLbl.setNotMatched(msmPeriodData.getMinTempMean(),
                    dailyPeriodData.getMinTempMean());
            tempTabMismatch = true;
        } else {
            myAvgMinTempLbl.setMatched();
        }

        // Mean Relative Humidity
        myMeanRelHumTF.setText(String.valueOf(iSavedPeriodData.getMeanRh()));

        // min temp under 32
        if (dailyPeriodData != null
                && iSavedPeriodData.getNumMinLessThan32F() == dailyPeriodData
                        .getNumMinLessThan32F()) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(
                    myMinTempLess32DegComboBox, DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null
                && iSavedPeriodData.getNumMinLessThan32F() == msmPeriodData
                        .getNumMinLessThan32F()) {
            DataFieldListener.setComboViewerSelection(
                    myMinTempLess32DegComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myMinTempLess32DegComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumMinLessThan32F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumMinLessThan32F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumMinLessThan32F() != dailyPeriodData
                        .getNumMinLessThan32F())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myMinTempLess32DegLabel.setNotMatched(
                    msmPeriodData.getNumMinLessThan32F(),
                    dailyPeriodData.getNumMinLessThan32F());
            tempTabMismatch = true;
        } else {
            myMinTempLess32DegLabel.setMatched();
        }

        // min temp under 0
        if (dailyPeriodData != null
                && iSavedPeriodData.getNumMinLessThan0F() == dailyPeriodData
                        .getNumMinLessThan0F()) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myMinTempLess0DegComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && iSavedPeriodData
                .getNumMinLessThan0F() == msmPeriodData.getNumMinLessThan0F()) {
            DataFieldListener.setComboViewerSelection(myMinTempLess0DegComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMinTempLess0DegComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumMinLessThan0F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumMinLessThan0F() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumMinLessThan0F() != dailyPeriodData
                        .getNumMinLessThan0F())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myMinTempLess0DegLabel.setNotMatched(
                    msmPeriodData.getNumMinLessThan0F(),
                    dailyPeriodData.getNumMinLessThan0F());
            tempTabMismatch = true;
        } else {
            myMinTempLess0DegLabel.setMatched();
        }

        // min temp t4
        if (myMinTempGreaterDegT4TF.isEnabled()) {
            myMinTempGreaterDegT4TF.setText(
                    String.valueOf(iSavedPeriodData.getNumMinGreaterThanT4F()));
        }

        // min temp t5
        if (myMinTempLessDegT5TF.isEnabled()) {
            myMinTempLessDegT5TF.setText(
                    String.valueOf(iSavedPeriodData.getNumMinLessThanT5F()));
        }

        // min temp t6
        if (myMinTempLessDegT6TF.isEnabled()) {
            myMinTempLessDegT6TF.setText(
                    String.valueOf(iSavedPeriodData.getNumMinLessThanT6F()));
        }

        // heating days
        if (dailyPeriodData != null && iSavedPeriodData
                .getNumHeatTotal() == dailyPeriodData.getNumHeatTotal()) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myHeatingDaysComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && iSavedPeriodData
                .getNumHeatTotal() == msmPeriodData.getNumHeatTotal()) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(myHeatingDaysComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myHeatingDaysComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumHeatTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY)
                && (msmPeriodData
                        .getNumHeatTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY)
                && (msmPeriodData.getNumHeatTotal() != dailyPeriodData
                        .getNumHeatTotal())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myHeatingDaysLabel.setNotMatched(msmPeriodData.getNumHeatTotal(),
                    dailyPeriodData.getNumHeatTotal());
            tempTabMismatch = true;
        } else {
            myHeatingDaysLabel.setMatched();
        }

        // cooling days
        if (dailyPeriodData != null && iSavedPeriodData
                .getNumCoolTotal() == dailyPeriodData.getNumCoolTotal()) {
            // check daily DB (could be null) first
            DataFieldListener.setComboViewerSelection(myCoolingDaysComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else if (msmPeriodData != null && iSavedPeriodData
                .getNumCoolTotal() == msmPeriodData.getNumCoolTotal()) {
            // check MSM second
            DataFieldListener.setComboViewerSelection(myCoolingDaysComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myCoolingDaysComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumCoolTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY)
                && (msmPeriodData
                        .getNumCoolTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY)
                && (msmPeriodData.getNumCoolTotal() != dailyPeriodData
                        .getNumCoolTotal())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myCoolingDaysLabel.setNotMatched(msmPeriodData.getNumCoolTotal(),
                    dailyPeriodData.getNumCoolTotal());
            tempTabMismatch = true;
        } else {
            myCoolingDaysLabel.setMatched();
        }

        if (tempTabMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return tempTabMismatch;
    }

    @Override
    protected boolean displayComparedData(PeriodData iMonthlyAsosData,
            PeriodData iDailyBuildData) {
        boolean tempTabMismatch = false;

        // max temp
        if (iMonthlyAsosData == null || iDailyBuildData
                .getMaxTemp() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getMaxTemp() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myMaxTempComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMaxTempLbl.setMatched();
            } else if (iMonthlyAsosData.getMaxTemp() != iDailyBuildData
                    .getMaxTemp()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxTempLbl.setNotMatched(iMonthlyAsosData.getMaxTemp(),
                        iDailyBuildData.getMaxTemp());
                tempTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxTempDates.length,
                        iDailyBuildData.getDayMaxTempList(),
                        iMonthlyAsosData.getDayMaxTempList())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxTempLbl.setNotMatched(
                            iMonthlyAsosData.getDayMaxTempList()
                                    .toArray(new ClimateDate[iMonthlyAsosData
                                            .getDayMaxTempList().size()]),
                            iDailyBuildData.getDayMaxTempList()
                                    .toArray(new ClimateDate[iDailyBuildData
                                            .getDayMaxTempList().size()]));
                    tempTabMismatch = true;
                } else {
                    myMaxTempLbl.setMatched();
                }
            }
        } else {
            myMaxTempLbl.setMatched();
        }

        // average max temp
        if (iMonthlyAsosData == null || iDailyBuildData
                .getMaxTempMean() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getMaxTempMean() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myAvgMaxTempComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myAvgMaxTempLbl.setMatched();
            } else if (!ClimateUtilities.floatingEquals(
                    ClimateUtilities.nint(iMonthlyAsosData.getMaxTempMean(), 1),
                    ClimateUtilities.nint(iDailyBuildData.getMaxTempMean(),
                            1))) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myAvgMaxTempLbl.setNotMatched(iMonthlyAsosData.getMaxTempMean(),
                        iDailyBuildData.getMaxTempMean());
                tempTabMismatch = true;
            } else {
                myAvgMaxTempLbl.setMatched();
            }
        } else {
            myAvgMaxTempLbl.setMatched();
        }

        // mean temperature
        if (iMonthlyAsosData == null || iDailyBuildData
                .getMeanTemp() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getMeanTemp() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myMeanTempComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMeanTempLbl.setMatched();
            } else if (!ClimateUtilities.floatingEquals(
                    ClimateUtilities.nint(iMonthlyAsosData.getMeanTemp(), 1),
                    ClimateUtilities.nint(iDailyBuildData.getMeanTemp(), 1))) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMeanTempLbl.setNotMatched(iMonthlyAsosData.getMeanTemp(),
                        iDailyBuildData.getMeanTemp());
                tempTabMismatch = true;
            } else {
                myMeanTempLbl.setMatched();
            }
        } else {
            myMeanTempLbl.setMatched();
        }

        // max temp over 90
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumMaxGreaterThan90F() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumMaxGreaterThan90F() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMaxTempGreater90DegComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMaxTempGreater90DegLabel.setMatched();
            } else if (iMonthlyAsosData
                    .getNumMaxGreaterThan90F() != iDailyBuildData
                            .getNumMaxGreaterThan90F()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxTempGreater90DegLabel.setNotMatched(
                        iMonthlyAsosData.getNumMaxGreaterThan90F(),
                        iDailyBuildData.getNumMaxGreaterThan90F());
                tempTabMismatch = true;
            } else {
                myMaxTempGreater90DegLabel.setMatched();
            }
        } else {
            myMaxTempGreater90DegLabel.setMatched();
        }

        // max temp under 32
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumMaxLessThan32F() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumMaxLessThan32F() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMaxTempLess32DegComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMaxTempLess32DegLabel.setMatched();
            } else if (iMonthlyAsosData
                    .getNumMaxLessThan32F() != iDailyBuildData
                            .getNumMaxLessThan32F()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxTempLess32DegLabel.setNotMatched(
                        iMonthlyAsosData.getNumMaxLessThan32F(),
                        iDailyBuildData.getNumMaxLessThan32F());
                tempTabMismatch = true;
            } else {
                myMaxTempLess32DegLabel.setMatched();
            }
        } else {
            myMaxTempLess32DegLabel.setMatched();
        }

        // max temp t1
        if (myMaxTempGreaterDegT1TF.isEnabled()) {
            myMaxTempGreaterDegT1TF.setText(
                    String.valueOf(iDailyBuildData.getNumMaxGreaterThanT1F()));
        }

        // max temp t2
        if (myMaxTempGreaterDegT2TF.isEnabled()) {
            myMaxTempGreaterDegT2TF.setText(
                    String.valueOf(iDailyBuildData.getNumMaxGreaterThanT2F()));
        }

        // max temp t3
        if (myMaxTempLessDegT3TF.isEnabled()) {
            myMaxTempLessDegT3TF.setText(
                    String.valueOf(iDailyBuildData.getNumMaxLessThanT3F()));
        }

        // min temp
        if (iMonthlyAsosData == null || iDailyBuildData
                .getMinTemp() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getMinTemp() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myMinTempComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMinTempLbl.setMatched();
            } else if (iMonthlyAsosData.getMinTemp() != iDailyBuildData
                    .getMinTemp()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMinTempLbl.setNotMatched(iMonthlyAsosData.getMinTemp(),
                        iDailyBuildData.getMinTemp());
                tempTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMinTempDates.length,
                        iDailyBuildData.getDayMinTempList(),
                        iMonthlyAsosData.getDayMinTempList())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMinTempLbl.setNotMatched(
                            iMonthlyAsosData.getDayMinTempList()
                                    .toArray(new ClimateDate[iMonthlyAsosData
                                            .getDayMinTempList().size()]),
                            iDailyBuildData.getDayMinTempList()
                                    .toArray(new ClimateDate[iDailyBuildData
                                            .getDayMinTempList().size()]));
                    tempTabMismatch = true;
                } else {
                    myMinTempLbl.setMatched();
                }
            }
        } else {
            myMinTempLbl.setMatched();
        }

        // average min temp
        if (iMonthlyAsosData == null || iDailyBuildData
                .getMinTempMean() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getMinTempMean() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myAvgMinTempComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myAvgMinTempLbl.setMatched();
            } else if (!ClimateUtilities.floatingEquals(
                    ClimateUtilities.nint(iMonthlyAsosData.getMinTempMean(), 1),
                    ClimateUtilities.nint(iDailyBuildData.getMinTempMean(),
                            1))) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myAvgMinTempLbl.setNotMatched(iMonthlyAsosData.getMinTempMean(),
                        iDailyBuildData.getMinTempMean());
                tempTabMismatch = true;
            } else {
                myAvgMinTempLbl.setMatched();
            }
        } else {
            myAvgMinTempLbl.setMatched();
        }

        // Mean Relative Humidity
        myMeanRelHumTF.setText(String.valueOf(iDailyBuildData.getMeanRh()));

        // min temp under 32
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumMinLessThan32F() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumMinLessThan32F() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMinTempLess32DegComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMinTempLess32DegLabel.setMatched();
            } else if (iMonthlyAsosData
                    .getNumMinLessThan32F() != iDailyBuildData
                            .getNumMinLessThan32F()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMinTempLess32DegLabel.setNotMatched(
                        iMonthlyAsosData.getNumMinLessThan32F(),
                        iDailyBuildData.getNumMinLessThan32F());
                tempTabMismatch = true;
            } else {
                myMinTempLess32DegLabel.setMatched();
            }
        } else {
            myMinTempLess32DegLabel.setMatched();
        }

        // min temp under 0
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumMinLessThan0F() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumMinLessThan0F() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMinTempLess0DegComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMinTempLess0DegLabel.setMatched();
            } else if (iMonthlyAsosData.getNumMinLessThan0F() != iDailyBuildData
                    .getNumMinLessThan0F()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMinTempLess0DegLabel.setNotMatched(
                        iMonthlyAsosData.getNumMinLessThan0F(),
                        iDailyBuildData.getNumMinLessThan0F());
                tempTabMismatch = true;
            } else {
                myMinTempLess0DegLabel.setMatched();
            }
        } else {
            myMinTempLess0DegLabel.setMatched();
        }

        // min temp t4
        if (myMinTempGreaterDegT4TF.isEnabled()) {
            myMinTempGreaterDegT4TF.setText(
                    String.valueOf(iDailyBuildData.getNumMinGreaterThanT4F()));
        }

        // min temp t5
        if (myMinTempLessDegT5TF.isEnabled()) {
            myMinTempLessDegT5TF.setText(
                    String.valueOf(iDailyBuildData.getNumMinLessThanT5F()));
        }

        // min temp t6
        if (myMinTempLessDegT6TF.isEnabled()) {
            myMinTempLessDegT6TF.setText(
                    String.valueOf(iDailyBuildData.getNumMinLessThanT6F()));
        }

        // heating days
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumHeatTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumHeatTotal() == ParameterFormatClimate.MISSING_DEGREE_DAY) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myHeatingDaysComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myHeatingDaysLabel.setMatched();
            } else if (iMonthlyAsosData.getNumHeatTotal() != iDailyBuildData
                    .getNumHeatTotal()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myHeatingDaysLabel.setNotMatched(
                        iMonthlyAsosData.getNumHeatTotal(),
                        iDailyBuildData.getNumHeatTotal());
                tempTabMismatch = true;
            } else {
                myHeatingDaysLabel.setMatched();
            }
        } else {
            myHeatingDaysLabel.setMatched();
        }

        // cooling days
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumCoolTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumCoolTotal() == ParameterFormatClimate.MISSING_DEGREE_DAY) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myCoolingDaysComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myCoolingDaysLabel.setMatched();
            } else if (iMonthlyAsosData.getNumCoolTotal() != iDailyBuildData
                    .getNumCoolTotal()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myCoolingDaysLabel.setNotMatched(
                        iMonthlyAsosData.getNumCoolTotal(),
                        iDailyBuildData.getNumCoolTotal());
                tempTabMismatch = true;
            } else {
                myCoolingDaysLabel.setMatched();
            }
        } else {
            myCoolingDaysLabel.setMatched();
        }

        if (tempTabMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return tempTabMismatch;
    }

    @Override
    protected void displayDailyBuildData() {
        // max temp
        DataFieldListener.setComboViewerSelection(myMaxTempComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // min temp
        DataFieldListener.setComboViewerSelection(myMinTempComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // avg max temp
        DataFieldListener.setComboViewerSelection(myAvgMaxTempComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // avg min temp
        DataFieldListener.setComboViewerSelection(myAvgMinTempComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // avg temp
        DataFieldListener.setComboViewerSelection(myMeanTempComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // max temp measures
        DataFieldListener.setComboViewerSelection(myMaxTempLess32DegComboBox,
                DataValueOrigin.DAILY_DATABASE);
        DataFieldListener.setComboViewerSelection(myMaxTempGreater90DegComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // min temp measures
        DataFieldListener.setComboViewerSelection(myMinTempLess32DegComboBox,
                DataValueOrigin.DAILY_DATABASE);
        DataFieldListener.setComboViewerSelection(myMinTempLess0DegComboBox,
                DataValueOrigin.DAILY_DATABASE);

        // heating/cooling days
        DataFieldListener.setComboViewerSelection(myHeatingDaysComboBox,
                DataValueOrigin.DAILY_DATABASE);
        DataFieldListener.setComboViewerSelection(myCoolingDaysComboBox,
                DataValueOrigin.DAILY_DATABASE);
    }

    @Override
    protected void clearValues() {
        myMaxTempTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        for (DateSelectionComp dateComp : myMaxTempDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myAvgMaxTempTF.setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMeanTempTF.setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMaxTempGreater90DegTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myMaxTempLess32DegTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMinTempTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        for (DateSelectionComp dateComp : myMinTempDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myAvgMinTempTF.setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMeanRelHumTF.setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMinTempLess32DegTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myMinTempLess0DegTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        myHeatingDaysTF.setText(
                String.valueOf(ParameterFormatClimate.MISSING_DEGREE_DAY));
        myCoolingDaysTF.setText(
                String.valueOf(ParameterFormatClimate.MISSING_DEGREE_DAY));
    }

    /**
     * Record heating degree days value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordHeatingDegreeDays(PeriodData dataToSave) {
        dataToSave.setNumHeatTotal(
                ClimateGUIUtils.parseInt(myHeatingDaysTF.getText()));
        dataToSave.getDataMethods()
                .setHeatQc(myHeatingDaysTF.getToolTip().getQcValue());
    }

    /**
     * Record cooling degree days value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordCoolingDegreeDays(PeriodData dataToSave) {
        dataToSave.setNumCoolTotal(
                ClimateGUIUtils.parseInt(myCoolingDaysTF.getText()));
        dataToSave.getDataMethods()
                .setCoolQc(myCoolingDaysTF.getToolTip().getQcValue());
    }

    /**
     * Record min temp <= 32 value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMinTempLess32(PeriodData dataToSave) {
        dataToSave.setNumMinLessThan32F(
                ClimateGUIUtils.parseInt(myMinTempLess32DegTF.getText()));
        dataToSave.getDataMethods()
                .setMinLE32Qc(myMinTempLess32DegTF.getToolTip().getQcValue());
    }

    /**
     * Record min temp <= 0 value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMinTempLess0(PeriodData dataToSave) {
        dataToSave.setNumMinLessThan0F(
                ClimateGUIUtils.parseInt(myMinTempLess0DegTF.getText()));
        dataToSave.getDataMethods()
                .setMinLE0Qc(myMinTempLess0DegTF.getToolTip().getQcValue());
    }

    /**
     * Record min temp value and dates and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMinTemp(PeriodData dataToSave) throws ParseException {
        dataToSave.setMinTemp(ClimateGUIUtils.parseInt(myMinTempTF.getText()));
        dataToSave.getDayMinTempList().clear();
        for (DateSelectionComp dateComp : myMinTempDates) {
            ClimateDate date = dateComp.getDate();
            if (!date.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getDayMinTempList().add(date);
            }
        }
        dataToSave.getDataMethods()
                .setMinTempQc(myMinTempTF.getToolTip().getQcValue());
    }

    /**
     * Record avg min temp value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordAvgMinTemp(PeriodData dataToSave) {
        /*
         * explicitly use MSM/Daily DB values instead of text, for safer
         * checking of values in loading (otherwise may get false "Other"
         * selections on loading due to rounding)
         */
        float saveValue;
        DataValueOrigin comboSelection = (DataValueOrigin) myAvgMinTempComboBox
                .getStructuredSelection().getFirstElement();
        if (DataValueOrigin.MONTHLY_SUMMARY_MESSAGE.equals(comboSelection)) {
            saveValue = myPeriodDialog.myMSMPeriodDataByStation
                    .get(myPeriodDialog.myCurrStation.getInformId())
                    .getMinTempMean();
        } else if (DataValueOrigin.DAILY_DATABASE.equals(comboSelection)) {
            saveValue = myPeriodDialog.myOriginalDataMap
                    .get(myPeriodDialog.myCurrStation.getInformId()).getData()
                    .getMinTempMean();
        } else {
            saveValue = ClimateGUIUtils.parseFloat(myAvgMinTempTF.getText());
        }
        dataToSave.setMinTempMean(saveValue);
        dataToSave.getDataMethods()
                .setAvgMinTempQc(myAvgMinTempTF.getToolTip().getQcValue());
    }

    /**
     * Record max temp >= 90 value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMaxTempGreater90(PeriodData dataToSave) {
        dataToSave.setNumMaxGreaterThan90F(
                ClimateGUIUtils.parseInt(myMaxTempGreater90DegTF.getText()));
        dataToSave.getDataMethods().setMaxTempGE90Qc(
                myMaxTempGreater90DegTF.getToolTip().getQcValue());
    }

    /**
     * Record max temp <= value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMaxTempLess32(PeriodData dataToSave) {
        dataToSave.setNumMaxLessThan32F(
                ClimateGUIUtils.parseInt(myMaxTempLess32DegTF.getText()));
        dataToSave.getDataMethods().setMaxTempLE32Qc(
                myMaxTempLess32DegTF.getToolTip().getQcValue());
    }

    /**
     * Record max temp value and dates and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMaxTemp(PeriodData dataToSave) throws ParseException {
        dataToSave.setMaxTemp(ClimateGUIUtils.parseInt(myMaxTempTF.getText()));
        dataToSave.getDayMaxTempList().clear();
        for (DateSelectionComp dateComp : myMaxTempDates) {
            ClimateDate date = dateComp.getDate();
            if (!date.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getDayMaxTempList().add(date);
            }
        }
        dataToSave.getDataMethods()
                .setMaxTempQc(myMaxTempTF.getToolTip().getQcValue());
    }

    /**
     * Record avg max temp value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordAvgMaxTemp(PeriodData dataToSave) {
        /*
         * explicitly use MSM/Daily DB values instead of text, for safer
         * checking of values in loading (otherwise may get false "Other"
         * selections on loading due to rounding)
         */
        float saveValue;
        DataValueOrigin comboSelection = (DataValueOrigin) myAvgMaxTempComboBox
                .getStructuredSelection().getFirstElement();
        if (DataValueOrigin.MONTHLY_SUMMARY_MESSAGE.equals(comboSelection)) {
            saveValue = myPeriodDialog.myMSMPeriodDataByStation
                    .get(myPeriodDialog.myCurrStation.getInformId())
                    .getMaxTempMean();
        } else if (DataValueOrigin.DAILY_DATABASE.equals(comboSelection)) {
            saveValue = myPeriodDialog.myOriginalDataMap
                    .get(myPeriodDialog.myCurrStation.getInformId()).getData()
                    .getMaxTempMean();
        } else {
            saveValue = ClimateGUIUtils.parseFloat(myAvgMaxTempTF.getText());
        }
        dataToSave.setMaxTempMean(saveValue);
        dataToSave.getDataMethods()
                .setAvgMaxTempQc(myAvgMaxTempTF.getToolTip().getQcValue());
    }

    /**
     * Record avg temp value and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMeanTemp(PeriodData dataToSave) {
        /*
         * explicitly use MSM/Daily DB values instead of text, for safer
         * checking of values in loading (otherwise may get false "Other"
         * selections on loading due to rounding)
         */
        float saveValue;
        DataValueOrigin comboSelection = (DataValueOrigin) myMeanTempComboBox
                .getStructuredSelection().getFirstElement();
        if (DataValueOrigin.MONTHLY_SUMMARY_MESSAGE.equals(comboSelection)) {
            saveValue = myPeriodDialog.myMSMPeriodDataByStation
                    .get(myPeriodDialog.myCurrStation.getInformId())
                    .getMeanTemp();
        } else if (DataValueOrigin.DAILY_DATABASE.equals(comboSelection)) {
            saveValue = myPeriodDialog.myOriginalDataMap
                    .get(myPeriodDialog.myCurrStation.getInformId()).getData()
                    .getMeanTemp();
        } else {
            saveValue = ClimateGUIUtils.parseFloat(myMeanTempTF.getText());
        }
        dataToSave.setMeanTemp(saveValue);
        dataToSave.getDataMethods()
                .setMeanTempQc(myMeanTempTF.getToolTip().getQcValue());
    }
}
