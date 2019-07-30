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
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
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
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.TimeSelectorFocusListener;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Precip tab of the Period Display dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20 NOV 2017  41128      amoore      Initial creation.
 * 14 NOV 2018  DR20977    wpaintsil   Add NumberFormatException handling.
 * 13 JUN 2019  DR21432    wpaintsil   Greatest Storm Total data population not
 *                                     implemented. Results in missing values 
 *                                     in the text product.
 * 18 JUL 2019  DR21454    wpaintsil   Faulty conditional logic results in
 *                                     mismatch symbols appearing inappropriately.
 * </pre>
 * 
 * @author amoore
 */
public class PrecipTab extends DisplayStationPeriodTabItem {
    /**
     * Data value type selection for maximum precipitation for 24 hours.
     */
    private ComboViewer myMaxPrecip24HourComboBox;

    /**
     * Data input for maximum precipitation for 24 hours.
     */
    private QCTextComp myMaxPrecip24HourTF;

    /**
     * Begin dates input for maximum precipitation for 24 hours.
     */
    private DateSelectionComp[] myMaxPrecip24HourBeginDates = new DateSelectionComp[3];

    /**
     * End dates input for maximum precipitation for 24 hours.
     */
    private DateSelectionComp[] myMaxPrecip24HourEndDates = new DateSelectionComp[3];

    /**
     * Data input for greatest precipitation storm.
     */
    private Text myGreatestPrecipStormTF;

    /**
     * Begin date inputs for greatest precipitation storm.
     */
    private DateSelectionComp[] myGreatestPrecipStormBeginDates = new DateSelectionComp[3];

    /**
     * Begin hour inputs for greatest precipitation storm.
     */
    private Text[] myGreatestPrecipStormBeginHourTFs = new Text[3];

    /**
     * End date inputs for greatest precipitation storm.
     */
    private DateSelectionComp[] myGreatestPrecipStormEndDates = new DateSelectionComp[3];

    /**
     * End hour inputs for greatest precipitation storm.
     */
    private Text[] myGreatestPrecipStormEndHourTFs = new Text[3];

    /**
     * Data value type selection for total precipitation.
     */
    private ComboViewer myTotalPrecipComboBox;

    /**
     * Data input for total precipitation.
     */
    private QCTextComp myTotalPrecipTF;

    /**
     * Data input for average daily precipitation.
     */
    private Text myAvgDailyPrecipTF;

    /**
     * Data value type selection for days with precipitation inches greater than
     * 0.01.
     */
    private ComboViewer myPrecipInchesGreater01ComboBox;

    /**
     * Data input for days with precipitation inches greater than 0.01.
     */
    private QCTextComp myPrecipInchesGreater01TF;

    /**
     * Data value type selection for days with precipitation inches greater than
     * 0.10.
     */
    private ComboViewer myPrecipInchesGreater10ComboBox;

    /**
     * Data input for days with precipitation inches greater than 0.10.
     */
    private QCTextComp myPrecipInchesGreater10TF;

    /**
     * Data value type selection for days with precipitation inches greater than
     * 0.50.
     */
    private ComboViewer myPrecipInchesGreater50ComboBox;

    /**
     * Data input for days with precipitation inches greater than 0.50.
     */
    private QCTextComp myPrecipInchesGreater50TF;

    /**
     * Data value type selection for days with precipitation inches greater than
     * 1.00.
     */
    private ComboViewer myPrecipInchesGreater100ComboBox;

    /**
     * Data input for days with precipitation inches greater than 1.00.
     */
    private QCTextComp myPrecipInchesGreater100TF;

    /**
     * First data input for days with precipitation inches greater than some
     * custom value.
     */
    private Text myPrecipInchesGreaterP1TF;

    /**
     * Second data input for days with precipitation inches greater than some
     * custom value.
     */
    private Text myPrecipInchesGreaterP2TF;

    /**
     * Maximum precipitation in 24 hours label.
     */
    private MismatchLabel myMaxPrecip24HourLbl;

    /**
     * Total precipitation label.
     */
    private MismatchLabel myTotalPrecipLbl;

    /**
     * Precip => 0.01 inches label.
     */
    private MismatchLabel myPrecipInchesGreater01Lbl;

    /**
     * Precip => 0.1 inches label.
     */
    private MismatchLabel myPrecipInchesGreater10Lbl;

    /**
     * Precip => 0.5 inches label.
     */
    private MismatchLabel myPrecipInchesGreater50Lbl;

    /**
     * Precip => 1.0 inches label.
     */
    private MismatchLabel myPrecipInchesGreater100Lbl;

    /**
     * 
     * @param parent
     * @param style
     * @param periodDialog
     */
    public PrecipTab(TabFolder parent, int style,
            DisplayStationPeriodDialog periodDialog) {
        super(parent, style, periodDialog);

        myTabItem.setText("Precipitation");
        Composite precipComp = new Composite(parent, SWT.NONE);
        RowLayout precipRL = new RowLayout(SWT.VERTICAL);
        precipRL.center = true;
        precipRL.marginLeft = 20;
        precipComp.setLayout(precipRL);
        myTabItem.setControl(precipComp);
        createPrecipitationTab(precipComp);
    }

    /**
     * Create precipitation tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createPrecipitationTab(Composite parent) {
        Composite precipComp = new Composite(parent, SWT.NONE);
        GridLayout precipGL = new GridLayout(2, false);
        precipGL.horizontalSpacing = 10;
        precipComp.setLayout(precipGL);

        Composite precipLeftComp = new Composite(precipComp, SWT.BORDER);
        RowLayout precipLeftRL = new RowLayout(SWT.VERTICAL);
        precipLeftRL.center = true;
        precipLeftRL.marginBottom = 80;
        precipLeftComp.setLayout(precipLeftRL);

        createPrecipitationTabLeft(precipLeftComp);

        Composite precipRightComp = new Composite(precipComp, SWT.BORDER);
        RowLayout precipRightRL = new RowLayout(SWT.VERTICAL);
        precipRightRL.center = true;
        precipRightComp.setLayout(precipRightRL);

        createPrecipitationTabRight(precipRightComp);
    }

    /**
     * Create right half of precipitation tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createPrecipitationTabRight(Composite parent) {
        // 24-hour max precip
        Composite topComp = new Composite(parent, SWT.NONE);
        RowLayout topRL = new RowLayout(SWT.VERTICAL);
        topRL.center = true;
        topComp.setLayout(topRL);

        // 24-hour max precip measurement
        Composite maxPrecip24HourComp = new Composite(topComp, SWT.NONE);
        GridLayout maxPrecip24HourGL = new GridLayout(3, false);
        maxPrecip24HourComp.setLayout(maxPrecip24HourGL);

        myMaxPrecip24HourLbl = new MismatchLabel(maxPrecip24HourComp,
                SWT.NORMAL);
        myMaxPrecip24HourLbl.setText("24-Hour Maximum\nPrecipitation (in)");

        myMaxPrecip24HourComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(maxPrecip24HourComp);
        myMaxPrecip24HourComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMaxPrecip24HourComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMaxPrecip24HourTF.setTextAndTip(
                                String.valueOf(iData.getPrecipMax24H()),
                                iData.getDataMethods().getPrecip24hrMaxQc());
                        int i = 0;
                        for (i = 0; i < myMaxPrecip24HourBeginDates.length
                                && i < iData.getPrecip24HDates().size(); i++) {
                            myMaxPrecip24HourBeginDates[i].setDate(iData
                                    .getPrecip24HDates().get(i).getStart());
                            myMaxPrecip24HourEndDates[i].setDate(
                                    iData.getPrecip24HDates().get(i).getEnd());
                        }
                        // clear any remaining fields
                        for (int j = i; j < myMaxPrecip24HourBeginDates.length; j++) {
                            myMaxPrecip24HourBeginDates[j].setMissing();
                            myMaxPrecip24HourEndDates[j].setMissing();
                        }
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        if (myMaxPrecip24HourTF.isFocusControl()) {
                            return true;
                        }
                        for (int i = 0; i < myMaxPrecip24HourBeginDates.length; i++) {
                            if (myMaxPrecip24HourBeginDates[i].isFocusControl()
                                    || myMaxPrecip24HourEndDates[i]
                                            .isFocusControl()) {
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    protected void saveOtherData() {
                        try {
                            PeriodData data = getOtherPeriodData();
                            recordMaxPrecip24Hour(data);
                            if (data.getPrecipMax24H() != ParameterFormatClimate.MISSING_PRECIP) {
                                data.getDataMethods().setPrecip24hrMaxQc(
                                        QCValues.MANUAL_ENTRY);
                            } else {
                                data.getDataMethods().setPrecip24hrMaxQc(
                                        ParameterFormatClimate.MISSING);
                            }
                        } catch (ParseException e) {
                            logger.error("Error parsing date.", e);
                        }
                    }
                });

        myMaxPrecip24HourTF = new QCTextComp(maxPrecip24HourComp, SWT.NONE,
                "24-Hour Maximum Precipitation (in)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMaxPrecip24HourTF.useGridData();
        myMaxPrecip24HourTF.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        myMaxPrecip24HourTF.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        myMaxPrecip24HourTF.addKeyListener(new DataFieldListener(
                myMaxPrecip24HourComboBox, myPeriodDialog.getChangeListener()));

        // 24-hour max precip dates
        Composite maxPrecip24HourDatesComp = new Composite(topComp, SWT.NONE);
        GridLayout maxPrecip24HourDatesGL = new GridLayout(4, false);
        maxPrecip24HourDatesComp.setLayout(maxPrecip24HourDatesGL);

        // first 24-hour max precip date
        Label maxPrecip24HourBeginDate1Lbl = new Label(maxPrecip24HourDatesComp,
                SWT.NORMAL);
        maxPrecip24HourBeginDate1Lbl.setText("Begin Date");

        myMaxPrecip24HourBeginDates[0] = new DateSelectionComp(
                maxPrecip24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        DataFieldListener maxPrecip24HourDatesListener = new DataFieldListener(
                myMaxPrecip24HourComboBox, myPeriodDialog.getChangeListener());
        myMaxPrecip24HourBeginDates[0]
                .addKeyListener(maxPrecip24HourDatesListener);
        myMaxPrecip24HourBeginDates[0]
                .addSelectionListener(maxPrecip24HourDatesListener);

        Label maxPrecip24HourEndDate1Lbl = new Label(maxPrecip24HourDatesComp,
                SWT.NORMAL);
        maxPrecip24HourEndDate1Lbl.setText("End Date");

        myMaxPrecip24HourEndDates[0] = new DateSelectionComp(
                maxPrecip24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxPrecip24HourEndDates[0]
                .addKeyListener(maxPrecip24HourDatesListener);
        myMaxPrecip24HourEndDates[0]
                .addSelectionListener(maxPrecip24HourDatesListener);

        // second 24-hour max precip date
        Label maxPrecip24HourBeginDate2Lbl = new Label(maxPrecip24HourDatesComp,
                SWT.NORMAL);
        maxPrecip24HourBeginDate2Lbl.setText("Begin Date");

        myMaxPrecip24HourBeginDates[1] = new DateSelectionComp(
                maxPrecip24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxPrecip24HourBeginDates[1]
                .addKeyListener(maxPrecip24HourDatesListener);
        myMaxPrecip24HourBeginDates[1]
                .addSelectionListener(maxPrecip24HourDatesListener);

        Label maxPrecip24HourEndDate2Lbl = new Label(maxPrecip24HourDatesComp,
                SWT.NORMAL);
        maxPrecip24HourEndDate2Lbl.setText("End Date");

        myMaxPrecip24HourEndDates[1] = new DateSelectionComp(
                maxPrecip24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxPrecip24HourEndDates[1]
                .addKeyListener(maxPrecip24HourDatesListener);
        myMaxPrecip24HourEndDates[1]
                .addSelectionListener(maxPrecip24HourDatesListener);

        // third 24-hour max precip date
        Label maxPrecip24HourBeginDate3Lbl = new Label(maxPrecip24HourDatesComp,
                SWT.NORMAL);
        maxPrecip24HourBeginDate3Lbl.setText("Begin Date");

        myMaxPrecip24HourBeginDates[2] = new DateSelectionComp(
                maxPrecip24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxPrecip24HourBeginDates[2]
                .addKeyListener(maxPrecip24HourDatesListener);
        myMaxPrecip24HourBeginDates[2]
                .addSelectionListener(maxPrecip24HourDatesListener);

        Label maxPrecip24HourEndDate3Lbl = new Label(maxPrecip24HourDatesComp,
                SWT.NORMAL);
        maxPrecip24HourEndDate3Lbl.setText("End Date");

        myMaxPrecip24HourEndDates[2] = new DateSelectionComp(
                maxPrecip24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxPrecip24HourEndDates[2]
                .addKeyListener(maxPrecip24HourDatesListener);
        myMaxPrecip24HourEndDates[2]
                .addSelectionListener(maxPrecip24HourDatesListener);

        // greatest precip storm
        Composite bottomComp = new Composite(parent, SWT.NONE);
        RowLayout bottomRL = new RowLayout(SWT.VERTICAL);
        bottomRL.center = true;
        bottomComp.setLayout(bottomRL);

        // greatest storm measurement
        Composite greatestPrecipStormComp = new Composite(bottomComp, SWT.NONE);
        GridLayout greastestPrecipStormGL = new GridLayout(2, false);
        greatestPrecipStormComp.setLayout(greastestPrecipStormGL);

        Label greatestPrecipStormLbl = new Label(greatestPrecipStormComp,
                SWT.NORMAL);
        greatestPrecipStormLbl.setText("Greatest Storm Total (in)");

        Composite greastestPrecipStormFieldComp = new Composite(
                greatestPrecipStormComp, SWT.NONE);
        RowLayout greatestPrecipStormFieldRL = new RowLayout(SWT.HORIZONTAL);
        greatestPrecipStormFieldRL.center = true;
        greastestPrecipStormFieldComp.setLayout(greatestPrecipStormFieldRL);

        myGreatestPrecipStormTF = new Text(greastestPrecipStormFieldComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myGreatestPrecipStormTF);
        myGreatestPrecipStormTF.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        myGreatestPrecipStormTF.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        myGreatestPrecipStormTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // greatest precip storm dates
        Composite greastestPrecipStormDatesComp = new Composite(bottomComp,
                SWT.NONE);
        GridLayout greastestPrecipStormDatesGL = new GridLayout(8, false);
        greastestPrecipStormDatesGL.verticalSpacing = 8;
        greastestPrecipStormDatesComp.setLayout(greastestPrecipStormDatesGL);

        // first greatest precip storm date
        // begin date
        Label greatestPrecipStormBeginDate1Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormBeginDate1Lbl.setText("Begin Date");

        myGreatestPrecipStormBeginDates[0] = new DateSelectionComp(
                greastestPrecipStormDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestPrecipStormBeginDates[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Label greatestPrecipStormBeginHour1Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormBeginHour1Lbl.setText("Hour");

        myGreatestPrecipStormBeginHourTFs[0] = new Text(
                greastestPrecipStormDatesComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestPrecipStormBeginHourTFs[0]);
        myGreatestPrecipStormBeginHourTFs[0]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestPrecipStormBeginHourTFs[0], true));
        myGreatestPrecipStormBeginHourTFs[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // end date
        Label greatestPrecipStormEndDate1Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormEndDate1Lbl.setText("End Date");

        myGreatestPrecipStormEndDates[0] = new DateSelectionComp(
                greastestPrecipStormDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestPrecipStormEndDates[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Label greatestPrecipStormEndHour1Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormEndHour1Lbl.setText("Hour");

        myGreatestPrecipStormEndHourTFs[0] = new Text(
                greastestPrecipStormDatesComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestPrecipStormEndHourTFs[0]);
        myGreatestPrecipStormEndHourTFs[0]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestPrecipStormEndHourTFs[0], true));
        myGreatestPrecipStormEndHourTFs[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // second greatest precip storm date
        // begin date
        Label greatestPrecipStormBeginDate2Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormBeginDate2Lbl.setText("Begin Date");

        myGreatestPrecipStormBeginDates[1] = new DateSelectionComp(
                greastestPrecipStormDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestPrecipStormBeginDates[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Label greatestPrecipStormBeginHour2Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormBeginHour2Lbl.setText("Hour");

        myGreatestPrecipStormBeginHourTFs[1] = new Text(
                greastestPrecipStormDatesComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestPrecipStormBeginHourTFs[1]);
        myGreatestPrecipStormBeginHourTFs[1]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestPrecipStormBeginHourTFs[1], true));
        myGreatestPrecipStormBeginHourTFs[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // end date
        Label greatestPrecipStormEndDate2Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormEndDate2Lbl.setText("End Date");

        myGreatestPrecipStormEndDates[1] = new DateSelectionComp(
                greastestPrecipStormDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestPrecipStormEndDates[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Label greatestPrecipStormEndHour2Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormEndHour2Lbl.setText("Hour");

        myGreatestPrecipStormEndHourTFs[1] = new Text(
                greastestPrecipStormDatesComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestPrecipStormEndHourTFs[1]);
        myGreatestPrecipStormEndHourTFs[1]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestPrecipStormEndHourTFs[1], true));
        myGreatestPrecipStormEndHourTFs[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // third greatest precip storm date
        // begin date
        Label greatestPrecipStormBeginDate3Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormBeginDate3Lbl.setText("Begin Date");

        myGreatestPrecipStormBeginDates[2] = new DateSelectionComp(
                greastestPrecipStormDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestPrecipStormBeginDates[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Label greatestPrecipStormBeginHour3Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormBeginHour3Lbl.setText("Hour");

        myGreatestPrecipStormBeginHourTFs[2] = new Text(
                greastestPrecipStormDatesComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestPrecipStormBeginHourTFs[2]);
        myGreatestPrecipStormBeginHourTFs[2]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestPrecipStormBeginHourTFs[2], true));
        myGreatestPrecipStormBeginHourTFs[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // end date
        Label greatestPrecipStormEndDate3Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormEndDate3Lbl.setText("End Date");

        myGreatestPrecipStormEndDates[2] = new DateSelectionComp(
                greastestPrecipStormDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestPrecipStormEndDates[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Label greatestPrecipStormEndHour3Lbl = new Label(
                greastestPrecipStormDatesComp, SWT.NORMAL);
        greatestPrecipStormEndHour3Lbl.setText("Hour");

        myGreatestPrecipStormEndHourTFs[2] = new Text(
                greastestPrecipStormDatesComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestPrecipStormEndHourTFs[2]);
        myGreatestPrecipStormEndHourTFs[2]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestPrecipStormEndHourTFs[2], true));
        myGreatestPrecipStormEndHourTFs[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());
    }

    /**
     * Create left half of precipitation tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createPrecipitationTabLeft(Composite parent) {
        // total and average
        Composite topComp = new Composite(parent, SWT.NONE);
        GridLayout topGL = new GridLayout(3, false);
        topComp.setLayout(topGL);

        myTotalPrecipLbl = new MismatchLabel(topComp, SWT.NORMAL);
        myTotalPrecipLbl.setText("Total Precipitation (in)");

        myTotalPrecipComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(topComp);
        myTotalPrecipComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myTotalPrecipComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myTotalPrecipTF.setTextAndTip(
                                String.valueOf(iData.getPrecipTotal()),
                                iData.getDataMethods().getPrecipQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myTotalPrecipTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordTotalPrecip(data);
                        if (data.getPrecipTotal() != ParameterFormatClimate.MISSING_PRECIP) {
                            data.getDataMethods()
                                    .setPrecipQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPrecipQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myTotalPrecipTF = new QCTextComp(topComp, SWT.NONE,
                "Total Precipitation (in)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myTotalPrecipTF.useGridData();
        myTotalPrecipTF.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        myTotalPrecipTF.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        myTotalPrecipTF.addKeyListener(new DataFieldListener(
                myTotalPrecipComboBox, myPeriodDialog.getChangeListener()));

        // average daily
        Label avgDailyPrecipLbl = new Label(topComp, SWT.NORMAL);
        avgDailyPrecipLbl.setText("Average Daily\nPrecipitation (in)");

        myAvgDailyPrecipTF = new Text(topComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myAvgDailyPrecipTF);
        myAvgDailyPrecipTF.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        myAvgDailyPrecipTF.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        myAvgDailyPrecipTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite avgDailyPrecipFiller = new Composite(topComp, SWT.NONE);
        avgDailyPrecipFiller.setLayoutData(ClimateLayoutValues.getFillerGD());

        // days with precip
        Label daysWithPrecipLbl = new Label(parent, SWT.NORMAL);
        daysWithPrecipLbl.setText("Days with Precipitation");

        Composite bottomComp = new Composite(parent, SWT.NONE);
        GridLayout bottomGL = new GridLayout(3, false);
        bottomComp.setLayout(bottomGL);

        myPrecipInchesGreater01Lbl = new MismatchLabel(bottomComp, SWT.NORMAL);
        myPrecipInchesGreater01Lbl.setText("0.01 in, or more");

        myPrecipInchesGreater01ComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(bottomComp);
        myPrecipInchesGreater01ComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myPrecipInchesGreater01ComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myPrecipInchesGreater01TF.setTextAndTip(
                                String.valueOf(iData.getNumPrcpGreaterThan01()),
                                iData.getDataMethods().getPrecipGE01Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myPrecipInchesGreater01TF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordPrecipGreater01(data);
                        if (data.getNumPrcpGreaterThan01() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setPrecipGE01Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPrecipGE01Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myPrecipInchesGreater01TF = new QCTextComp(bottomComp, SWT.NONE,
                "0.01 in, or more", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myPrecipInchesGreater01TF.useGridData();
        myPrecipInchesGreater01TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreater01TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());

        myPrecipInchesGreater10Lbl = new MismatchLabel(bottomComp, SWT.NORMAL);
        myPrecipInchesGreater10Lbl.setText("0.10 in, or more");

        myPrecipInchesGreater10ComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(bottomComp);
        myPrecipInchesGreater10ComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myPrecipInchesGreater10ComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myPrecipInchesGreater10TF.setTextAndTip(
                                String.valueOf(iData.getNumPrcpGreaterThan10()),
                                iData.getDataMethods().getPrecipGE10Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myPrecipInchesGreater10TF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordPrecipGreater10(data);
                        if (data.getNumPrcpGreaterThan10() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setPrecipGE10Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPrecipGE10Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myPrecipInchesGreater10TF = new QCTextComp(bottomComp, SWT.NONE,
                "0.10 in, or more", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myPrecipInchesGreater10TF.useGridData();
        myPrecipInchesGreater10TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreater10TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());

        myPrecipInchesGreater50Lbl = new MismatchLabel(bottomComp, SWT.NORMAL);
        myPrecipInchesGreater50Lbl.setText("0.50 in, or more");

        myPrecipInchesGreater50ComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(bottomComp);
        myPrecipInchesGreater50ComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myPrecipInchesGreater50ComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myPrecipInchesGreater50TF.setTextAndTip(
                                String.valueOf(iData.getNumPrcpGreaterThan50()),
                                iData.getDataMethods().getPrecipGE50Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myPrecipInchesGreater50TF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordPrecipGreater50(data);
                        if (data.getNumPrcpGreaterThan50() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setPrecipGE50Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPrecipGE50Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myPrecipInchesGreater50TF = new QCTextComp(bottomComp, SWT.NONE,
                "0.50 in, or more", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myPrecipInchesGreater50TF.useGridData();
        myPrecipInchesGreater50TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreater50TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());

        myPrecipInchesGreater100Lbl = new MismatchLabel(bottomComp, SWT.NORMAL);
        myPrecipInchesGreater100Lbl.setText("1.00 in, or more");

        myPrecipInchesGreater100ComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(bottomComp);
        myPrecipInchesGreater100ComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myPrecipInchesGreater100ComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myPrecipInchesGreater100TF.setTextAndTip(
                                String.valueOf(
                                        iData.getNumPrcpGreaterThan100()),
                                iData.getDataMethods().getPrecipGE100Qc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myPrecipInchesGreater100TF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordPrecipGreater100(data);
                        if (data.getNumPrcpGreaterThan100() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setPrecipGE100Qc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPrecipGE100Qc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myPrecipInchesGreater100TF = new QCTextComp(bottomComp, SWT.NONE,
                "1.00 in, or more", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myPrecipInchesGreater100TF.useGridData();
        myPrecipInchesGreater100TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreater100TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());

        // first custom inches
        Label precipInchesGreaterP1Lbl = new Label(bottomComp, SWT.NORMAL);

        myPrecipInchesGreaterP1TF = new Text(bottomComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myPrecipInchesGreaterP1TF);
        myPrecipInchesGreaterP1TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreaterP1TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreaterP1TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getP1() == ParameterFormatClimate.MISSING_PRECIP) {
            precipInchesGreaterP1Lbl.setText("?? in, or more");
            precipInchesGreaterP1Lbl.setEnabled(false);
            myPrecipInchesGreaterP1TF.setEnabled(false);
        } else {
            precipInchesGreaterP1Lbl
                    .setText(myPeriodDialog.myGlobals.getP1() + " in, or more");
        }

        Composite precipInchesGreaterP1Filler = new Composite(bottomComp,
                SWT.NONE);
        precipInchesGreaterP1Filler
                .setLayoutData(ClimateLayoutValues.getFillerGD());

        // second custom inches
        Label precipInchesGreaterP2Lbl = new Label(bottomComp, SWT.NORMAL);

        myPrecipInchesGreaterP2TF = new Text(bottomComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myPrecipInchesGreaterP2TF);
        myPrecipInchesGreaterP2TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreaterP2TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myPrecipInchesGreaterP2TF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        if (myPeriodDialog.myGlobals
                .getP2() == ParameterFormatClimate.MISSING_PRECIP) {
            precipInchesGreaterP2Lbl.setText("?? in, or more");
            precipInchesGreaterP2Lbl.setEnabled(false);
            myPrecipInchesGreaterP2TF.setEnabled(false);
        } else {
            precipInchesGreaterP2Lbl
                    .setText(myPeriodDialog.myGlobals.getP2() + " in, or more");
        }

        Composite precipInchesGreaterP2Filler = new Composite(bottomComp,
                SWT.NONE);
        precipInchesGreaterP2Filler
                .setLayoutData(ClimateLayoutValues.getFillerGD());
    }

    @Override
    protected void saveValues(PeriodData dataToSave)
            throws VizException, NumberFormatException, ParseException {
        recordMaxPrecip24Hour(dataToSave);

        dataToSave.setPrecipStormMax(myGreatestPrecipStormTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : ClimateGUIUtils
                                .parseFloat(myGreatestPrecipStormTF.getText()));
        dataToSave.getPrecipStormList().clear();
        for (int i = 0; i < myGreatestPrecipStormBeginDates.length; i++) {
            ClimateDate startDate = myGreatestPrecipStormBeginDates[i]
                    .getDate();
            ClimateDate endDate = myGreatestPrecipStormEndDates[i].getDate();
            int startHour = Integer
                    .parseInt(myGreatestPrecipStormBeginHourTFs[i].getText());
            int endHour = Integer
                    .parseInt(myGreatestPrecipStormEndHourTFs[i].getText());
            if (!startDate.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && !endDate.toMonthDayDateString()
                            .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && startHour != ParameterFormatClimate.MISSING_HOUR
                    && endHour != ParameterFormatClimate.MISSING_HOUR) {
                dataToSave.getPrecipStormList().add(new ClimateDates(

                        startDate, endDate, startHour, endHour));
            }
        }

        recordTotalPrecip(dataToSave);
        dataToSave.setPrecipMeanDay(myAvgDailyPrecipTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : ClimateGUIUtils
                                .parseFloat(myAvgDailyPrecipTF.getText()));

        recordPrecipGreater01(dataToSave);
        recordPrecipGreater10(dataToSave);
        recordPrecipGreater50(dataToSave);
        recordPrecipGreater100(dataToSave);

        if (myPrecipInchesGreaterP1TF.isEnabled()) {
            dataToSave.setNumPrcpGreaterThanP1(ClimateGUIUtils
                    .parseInt(myPrecipInchesGreaterP1TF.getText()));
        }

        if (myPrecipInchesGreaterP2TF.isEnabled()) {
            dataToSave.setNumPrcpGreaterThanP2(ClimateGUIUtils
                    .parseInt(myPrecipInchesGreaterP2TF.getText()));
        }
    }

    @Override
    protected boolean loadSavedData(PeriodData iSavedPeriodData,
            PeriodData msmPeriodData, PeriodData dailyPeriodData) {
        boolean precipTabMismatch = false;

        // total precip
        // check MSM first
        if (msmPeriodData != null && ClimateUtilities.floatingEquals(
                iSavedPeriodData.getPrecipTotal(),
                msmPeriodData.getPrecipTotal())) {
            DataFieldListener.setComboViewerSelection(myTotalPrecipComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null && ClimateUtilities.floatingEquals(
                iSavedPeriodData.getPrecipTotal(),
                dailyPeriodData.getPrecipTotal())) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(myTotalPrecipComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myTotalPrecipComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (!ClimateUtilities.floatingEquals(
                        dailyPeriodData.getPrecipTotal(),
                        ParameterFormatClimate.MISSING_PRECIP))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getPrecipTotal(),
                        ParameterFormatClimate.MISSING_PRECIP))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getPrecipTotal(),
                        dailyPeriodData.getPrecipTotal()))) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myTotalPrecipLbl.setNotMatched(msmPeriodData.getPrecipTotal(),
                    dailyPeriodData.getPrecipTotal());
            precipTabMismatch = true;
        } else {
            myTotalPrecipLbl.setMatched();
        }

        // avg precip
        myAvgDailyPrecipTF
                .setText(String.valueOf(iSavedPeriodData.getPrecipMeanDay()));

        // precip >= 0.01
        // check MSM first
        if (msmPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan01() == msmPeriodData
                        .getNumPrcpGreaterThan01()) {
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater01ComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan01() == dailyPeriodData
                        .getNumPrcpGreaterThan01()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater01ComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater01ComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumPrcpGreaterThan01() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData
                        .getNumPrcpGreaterThan01() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData.getNumPrcpGreaterThan01() != dailyPeriodData
                        .getNumPrcpGreaterThan01())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myPrecipInchesGreater01Lbl.setNotMatched(
                    msmPeriodData.getNumPrcpGreaterThan01(),
                    dailyPeriodData.getNumPrcpGreaterThan01());
            precipTabMismatch = true;
        } else {
            myPrecipInchesGreater01Lbl.setMatched();
        }

        // precip >= 0.1
        // check MSM first
        if (msmPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan10() == msmPeriodData
                        .getNumPrcpGreaterThan10()) {
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater10ComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan10() == dailyPeriodData
                        .getNumPrcpGreaterThan10()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater10ComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater10ComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumPrcpGreaterThan10() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData
                        .getNumPrcpGreaterThan10() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData.getNumPrcpGreaterThan10() != dailyPeriodData
                        .getNumPrcpGreaterThan10())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myPrecipInchesGreater10Lbl.setNotMatched(
                    msmPeriodData.getNumPrcpGreaterThan10(),
                    dailyPeriodData.getNumPrcpGreaterThan10());
            precipTabMismatch = true;
        } else {
            myPrecipInchesGreater10Lbl.setMatched();
        }

        // precip >= 0.5
        // check MSM first
        if (msmPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan50() == msmPeriodData
                        .getNumPrcpGreaterThan50()) {
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater50ComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan50() == dailyPeriodData
                        .getNumPrcpGreaterThan50()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater50ComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater50ComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumPrcpGreaterThan50() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData
                        .getNumPrcpGreaterThan50() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData.getNumPrcpGreaterThan50() != dailyPeriodData
                        .getNumPrcpGreaterThan50())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myPrecipInchesGreater50Lbl.setNotMatched(
                    msmPeriodData.getNumPrcpGreaterThan50(),
                    dailyPeriodData.getNumPrcpGreaterThan50());
            precipTabMismatch = true;
        } else {
            myPrecipInchesGreater50Lbl.setMatched();
        }

        // precip >= 1.0
        // check MSM first
        if (msmPeriodData != null
                && iSavedPeriodData.getNumPrcpGreaterThan100() == msmPeriodData
                        .getNumPrcpGreaterThan100()) {
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater100ComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null && iSavedPeriodData
                .getNumPrcpGreaterThan100() == dailyPeriodData
                        .getNumPrcpGreaterThan100()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater100ComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myPrecipInchesGreater100ComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumPrcpGreaterThan100() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData
                        .getNumPrcpGreaterThan100() != ParameterFormatClimate.MISSING_PRECIP)
                && (msmPeriodData.getNumPrcpGreaterThan100() != dailyPeriodData
                        .getNumPrcpGreaterThan100())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myPrecipInchesGreater100Lbl.setNotMatched(
                    msmPeriodData.getNumPrcpGreaterThan100(),
                    dailyPeriodData.getNumPrcpGreaterThan100());
            precipTabMismatch = true;
        } else {
            myPrecipInchesGreater100Lbl.setMatched();
        }

        // precip p1
        if (myPrecipInchesGreaterP1TF.isEnabled()) {
            myPrecipInchesGreaterP1TF.setText(
                    String.valueOf(iSavedPeriodData.getNumPrcpGreaterThanP1()));
        }

        // precip p2
        if (myPrecipInchesGreaterP2TF.isEnabled()) {
            myPrecipInchesGreaterP2TF.setText(
                    String.valueOf(iSavedPeriodData.getNumPrcpGreaterThanP2()));
        }

        // max precip 24H
        // check MSM first
        if (msmPeriodData != null
                && isPeriodDataEqualForFloat(iSavedPeriodData.getPrecipMax24H(),
                        msmPeriodData.getPrecipMax24H(),
                        iSavedPeriodData.getPrecip24HDates(),
                        msmPeriodData.getPrecip24HDates(),
                        myMaxPrecip24HourBeginDates.length)) {
            DataFieldListener.setComboViewerSelection(myMaxPrecip24HourComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && isPeriodDataEqualForFloat(iSavedPeriodData.getPrecipMax24H(),
                        dailyPeriodData.getPrecipMax24H(),
                        iSavedPeriodData.getPrecip24HDates(),
                        dailyPeriodData.getPrecip24HDates(),
                        myMaxPrecip24HourBeginDates.length)) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(myMaxPrecip24HourComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMaxPrecip24HourComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (!ClimateUtilities.floatingEquals(
                        dailyPeriodData.getPrecipMax24H(),
                        ParameterFormatClimate.MISSING_PRECIP))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getPrecipMax24H(),
                        ParameterFormatClimate.MISSING_PRECIP))) {
            if (!ClimateUtilities.floatingEquals(
                    msmPeriodData.getPrecipMax24H(),
                    dailyPeriodData.getPrecipMax24H())) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxPrecip24HourLbl.setNotMatched(
                        msmPeriodData.getPrecipMax24H(),
                        dailyPeriodData.getPrecipMax24H());
                precipTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxPrecip24HourBeginDates.length,
                        dailyPeriodData.getPrecip24HDates(),
                        msmPeriodData.getPrecip24HDates())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxPrecip24HourLbl.setNotMatched(
                            msmPeriodData.getPrecip24HDates()
                                    .toArray(new ClimateDates[msmPeriodData
                                            .getPrecip24HDates().size()]),
                            dailyPeriodData.getPrecip24HDates()
                                    .toArray(new ClimateDates[dailyPeriodData
                                            .getPrecip24HDates().size()]));
                    precipTabMismatch = true;
                } else {
                    myMaxPrecip24HourLbl.setMatched();
                }
            }
        } else {
            myMaxPrecip24HourLbl.setMatched();
        }

        // Greatest Storm Total
        myGreatestPrecipStormTF
                .setText(String.valueOf(iSavedPeriodData.getPrecipStormMax()));

        if (precipTabMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return precipTabMismatch;
    }

    @Override
    protected boolean displayDailyBuildData(PeriodData iMonthlyAsosData,
            PeriodData iDailyBuildData) {
        boolean precipTabMismatch = false;

        // total precip
        if (iMonthlyAsosData == null || !ClimateUtilities.floatingEquals(
                iDailyBuildData.getPrecipTotal(),
                ParameterFormatClimate.MISSING_PRECIP)) {
            if (iMonthlyAsosData == null || ClimateUtilities.floatingEquals(
                    iMonthlyAsosData.getPrecipTotal(),
                    ParameterFormatClimate.MISSING_PRECIP)) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myTotalPrecipComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myTotalPrecipLbl.setMatched();
            } else if (!ClimateUtilities.floatingEquals(
                    iMonthlyAsosData.getPrecipTotal(),
                    iDailyBuildData.getPrecipTotal())) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myTotalPrecipLbl.setNotMatched(
                        iMonthlyAsosData.getPrecipTotal(),
                        iDailyBuildData.getPrecipTotal());
                precipTabMismatch = true;
            } else {
                myTotalPrecipLbl.setMatched();
            }
        } else {
            myTotalPrecipLbl.setMatched();
        }

        // avg precip
        myAvgDailyPrecipTF.setText(String.valueOf(
                ClimateUtilities.nint(iDailyBuildData.getPrecipMeanDay(), 2)));

        // precip >= 0.01
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumPrcpGreaterThan01() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumPrcpGreaterThan01() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myPrecipInchesGreater01ComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myPrecipInchesGreater01Lbl.setMatched();
            } else if (iMonthlyAsosData
                    .getNumPrcpGreaterThan01() != iDailyBuildData
                            .getNumPrcpGreaterThan01()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myPrecipInchesGreater01Lbl.setNotMatched(
                        iMonthlyAsosData.getNumPrcpGreaterThan01(),
                        iDailyBuildData.getNumPrcpGreaterThan01());
                precipTabMismatch = true;
            } else {
                myPrecipInchesGreater01Lbl.setMatched();
            }
        } else {
            myPrecipInchesGreater01Lbl.setMatched();
        }

        // precip >= 0.1
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumPrcpGreaterThan10() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumPrcpGreaterThan10() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myPrecipInchesGreater10ComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myPrecipInchesGreater10Lbl.setMatched();
            } else if (iMonthlyAsosData
                    .getNumPrcpGreaterThan10() != iDailyBuildData
                            .getNumPrcpGreaterThan10()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myPrecipInchesGreater10Lbl.setNotMatched(
                        iMonthlyAsosData.getNumPrcpGreaterThan10(),
                        iDailyBuildData.getNumPrcpGreaterThan10());
                precipTabMismatch = true;
            } else {
                myPrecipInchesGreater10Lbl.setMatched();
            }
        } else {
            myPrecipInchesGreater10Lbl.setMatched();
        }

        // precip >= 0.5
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumPrcpGreaterThan50() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumPrcpGreaterThan50() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myPrecipInchesGreater50ComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myPrecipInchesGreater50Lbl.setMatched();
            } else if (iMonthlyAsosData
                    .getNumPrcpGreaterThan50() != iDailyBuildData
                            .getNumPrcpGreaterThan50()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myPrecipInchesGreater50Lbl.setNotMatched(
                        iMonthlyAsosData.getNumPrcpGreaterThan50(),
                        iDailyBuildData.getNumPrcpGreaterThan50());
                precipTabMismatch = true;
            } else {
                myPrecipInchesGreater50Lbl.setMatched();
            }
        } else {
            myPrecipInchesGreater50Lbl.setMatched();
        }

        // precip >= 1.0
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumPrcpGreaterThan100() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumPrcpGreaterThan100() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myPrecipInchesGreater100ComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myPrecipInchesGreater100Lbl.setMatched();
            } else if (iMonthlyAsosData
                    .getNumPrcpGreaterThan100() != iDailyBuildData
                            .getNumPrcpGreaterThan100()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myPrecipInchesGreater100Lbl.setNotMatched(
                        iMonthlyAsosData.getNumPrcpGreaterThan100(),
                        iDailyBuildData.getNumPrcpGreaterThan100());
                precipTabMismatch = true;
            } else {
                myPrecipInchesGreater100Lbl.setMatched();
            }
        } else {
            myPrecipInchesGreater100Lbl.setMatched();
        }

        // precip p1
        if (myPrecipInchesGreaterP1TF.isEnabled()) {
            myPrecipInchesGreaterP1TF.setText(
                    String.valueOf(iDailyBuildData.getNumPrcpGreaterThanP1()));
        }

        // precip p2
        if (myPrecipInchesGreaterP2TF.isEnabled()) {
            myPrecipInchesGreaterP2TF.setText(
                    String.valueOf(iDailyBuildData.getNumPrcpGreaterThanP2()));
        }

        // max precip 24H
        if (iMonthlyAsosData == null || !ClimateUtilities.floatingEquals(
                iDailyBuildData.getPrecipMax24H(),
                ParameterFormatClimate.MISSING_PRECIP)) {
            if (iMonthlyAsosData == null || ClimateUtilities.floatingEquals(
                    iMonthlyAsosData.getPrecipMax24H(),
                    ParameterFormatClimate.MISSING_PRECIP)) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMaxPrecip24HourComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMaxPrecip24HourLbl.setMatched();
            } else if (!ClimateUtilities.floatingEquals(
                    iMonthlyAsosData.getPrecipMax24H(),
                    iDailyBuildData.getPrecipMax24H())) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxPrecip24HourLbl.setNotMatched(
                        iMonthlyAsosData.getPrecipMax24H(),
                        iDailyBuildData.getPrecipMax24H());
                precipTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxPrecip24HourBeginDates.length,
                        iDailyBuildData.getPrecip24HDates(),
                        iMonthlyAsosData.getPrecip24HDates())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxPrecip24HourLbl.setNotMatched(
                            iMonthlyAsosData.getPrecip24HDates()
                                    .toArray(new ClimateDates[iMonthlyAsosData
                                            .getPrecip24HDates().size()]),
                            iDailyBuildData.getPrecip24HDates()
                                    .toArray(new ClimateDates[iDailyBuildData
                                            .getPrecip24HDates().size()]));
                    precipTabMismatch = true;
                } else {
                    myMaxPrecip24HourLbl.setMatched();
                }
            }
        } else {
            myMaxPrecip24HourLbl.setMatched();
        }

        // Greatest Storm Total
        myGreatestPrecipStormTF
                .setText(String.valueOf(iDailyBuildData.getPrecipStormMax()));

        if (precipTabMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return precipTabMismatch;
    }

    @Override
    protected void displayMonthlyASOSData() {
        DataFieldListener.setComboViewerSelection(myTotalPrecipComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(
                myPrecipInchesGreater01ComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(
                myPrecipInchesGreater10ComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(
                myPrecipInchesGreater50ComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(
                myPrecipInchesGreater100ComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(myMaxPrecip24HourComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
    }

    @Override
    protected void clearValues() {
        myTotalPrecipTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_PRECIP));
        myAvgDailyPrecipTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_PRECIP));

        myPrecipInchesGreater01TF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myPrecipInchesGreater10TF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myPrecipInchesGreater50TF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myPrecipInchesGreater100TF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMaxPrecip24HourTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_PRECIP));
        for (DateSelectionComp dateComp : myMaxPrecip24HourBeginDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }
        for (DateSelectionComp dateComp : myMaxPrecip24HourEndDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myGreatestPrecipStormTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_PRECIP));
        for (DateSelectionComp dateComp : myGreatestPrecipStormBeginDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }
        for (DateSelectionComp dateComp : myGreatestPrecipStormEndDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }
        for (Text text : myGreatestPrecipStormBeginHourTFs) {
            text.setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        }
        for (Text text : myGreatestPrecipStormEndHourTFs) {
            text.setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        }
    }

    /**
     * Record max 24-hour precip value and dates and QC to the given period data
     * object.
     * 
     * @param dataToSave
     */
    private void recordMaxPrecip24Hour(PeriodData dataToSave)
            throws ParseException {
        /*
         * explicitly use MSM/Daily DB values instead of text, for safer
         * checking of values in loading (otherwise may get false "Other"
         * selections on loading due to rounding)
         */
        String saveValue;
        DataValueOrigin comboSelection = (DataValueOrigin) myMaxPrecip24HourComboBox
                .getStructuredSelection().getFirstElement();
        if (DataValueOrigin.MONTHLY_SUMMARY_MESSAGE.equals(comboSelection)) {
            saveValue = String.valueOf(myPeriodDialog.myMSMPeriodDataByStation
                    .get(myPeriodDialog.myCurrStation.getInformId())
                    .getPrecipMax24H());
        } else if (DataValueOrigin.DAILY_DATABASE.equals(comboSelection)) {
            saveValue = String.valueOf(myPeriodDialog.myOriginalDataMap
                    .get(myPeriodDialog.myCurrStation.getInformId()).getData()
                    .getPrecipMax24H());
        } else {
            saveValue = myMaxPrecip24HourTF.getText();
        }
        dataToSave.setPrecipMax24H(
                saveValue.equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : ClimateGUIUtils.parseFloat(saveValue));
        dataToSave.getPrecip24HDates().clear();
        for (int i = 0; i < myMaxPrecip24HourBeginDates.length; i++) {
            ClimateDate startDate = myMaxPrecip24HourBeginDates[i].getDate();
            ClimateDate endDate = myMaxPrecip24HourEndDates[i].getDate();
            if (!startDate.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && !endDate.toMonthDayDateString().equals(
                            ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getPrecip24HDates()
                        .add(new ClimateDates(startDate, endDate));
            }
        }
        dataToSave.getDataMethods().setPrecip24hrMaxQc(
                myMaxPrecip24HourTF.getToolTip().getQcValue());
    }

    /**
     * Record total precip and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordTotalPrecip(PeriodData dataToSave) {
        /*
         * explicitly use MSM/Daily DB values instead of text, for safer
         * checking of values in loading (otherwise may get false "Other"
         * selections on loading due to rounding)
         */
        String saveValue;
        DataValueOrigin comboSelection = (DataValueOrigin) myTotalPrecipComboBox
                .getStructuredSelection().getFirstElement();
        if (DataValueOrigin.MONTHLY_SUMMARY_MESSAGE.equals(comboSelection)) {
            saveValue = String.valueOf(myPeriodDialog.myMSMPeriodDataByStation
                    .get(myPeriodDialog.myCurrStation.getInformId())
                    .getPrecipTotal());
        } else if (DataValueOrigin.DAILY_DATABASE.equals(comboSelection)) {
            saveValue = String.valueOf(myPeriodDialog.myOriginalDataMap
                    .get(myPeriodDialog.myCurrStation.getInformId()).getData()
                    .getPrecipTotal());
        } else {
            saveValue = myTotalPrecipTF.getText();
        }
        dataToSave.setPrecipTotal(
                saveValue.equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : ClimateGUIUtils.parseFloat(saveValue));
        dataToSave.getDataMethods()
                .setPrecipQc(myTotalPrecipTF.getToolTip().getQcValue());
    }

    /**
     * Record precip >= 0.01 inches and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordPrecipGreater01(PeriodData dataToSave) {
        dataToSave.setNumPrcpGreaterThan01(
                ClimateGUIUtils.parseInt(myPrecipInchesGreater01TF.getText()));
        dataToSave.getDataMethods().setPrecipGE01Qc(
                myPrecipInchesGreater01TF.getToolTip().getQcValue());
    }

    /**
     * Record precip >= 0.10 inches and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordPrecipGreater10(PeriodData dataToSave) {
        dataToSave.setNumPrcpGreaterThan10(
                ClimateGUIUtils.parseInt(myPrecipInchesGreater10TF.getText()));
        dataToSave.getDataMethods().setPrecipGE10Qc(
                myPrecipInchesGreater10TF.getToolTip().getQcValue());
    }

    /**
     * Record precip >= 0.50 inches and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordPrecipGreater50(PeriodData dataToSave) {
        dataToSave.setNumPrcpGreaterThan50(
                ClimateGUIUtils.parseInt(myPrecipInchesGreater50TF.getText()));
        dataToSave.getDataMethods().setPrecipGE50Qc(
                myPrecipInchesGreater50TF.getToolTip().getQcValue());
    }

    /**
     * Record precip >= 1.00 inches and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordPrecipGreater100(PeriodData dataToSave) {
        dataToSave.setNumPrcpGreaterThan100(
                ClimateGUIUtils.parseInt(myPrecipInchesGreater100TF.getText()));
        dataToSave.getDataMethods().setPrecipGE100Qc(
                myPrecipInchesGreater100TF.getToolTip().getQcValue());
    }
}
