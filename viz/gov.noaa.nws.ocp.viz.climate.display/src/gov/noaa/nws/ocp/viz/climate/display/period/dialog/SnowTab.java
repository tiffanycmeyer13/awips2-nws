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

/**
 * Snow tab of the Period Display dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20 NOV 2017  41128      amoore      Initial creation.
 * </pre>
 * 
 * @author amoore
 */
public class SnowTab extends DisplayStationPeriodTabItem {

    /**
     * Data value type selection for maximum snow in 24 hours.
     */
    private ComboViewer myMaxSnow24HourComboBox;

    /**
     * Data input for maximum snow in 24 hours.
     */
    private QCTextComp myMaxSnow24HourTF;

    /**
     * Being dates input for maximum snow in 24 hours.
     */
    private DateSelectionComp[] myMaxSnow24HourBeginDates = new DateSelectionComp[3];

    /**
     * End dates input for maximum snow in 24 hours.
     */
    private DateSelectionComp[] myMaxSnow24HourEndDates = new DateSelectionComp[3];

    /**
     * Data input for greatest snow storm.
     */
    private Text myGreatestSnowStormTF;

    /**
     * Begin date inputs for greatest snow storm.
     */
    private DateSelectionComp[] myGreatestSnowStormBeginDates = new DateSelectionComp[3];

    /**
     * Begin hour inputs for greatest snow storm.
     */
    private Text[] myGreatestSnowStormBeginHourTFs = new Text[3];

    /**
     * End date inputs for greatest snow storm.
     */
    private DateSelectionComp[] myGreatestSnowStormEndDates = new DateSelectionComp[3];

    /**
     * End hour inputs for greatest snow storm.
     */
    private Text[] myGreatestSnowStormEndHourTFs = new Text[3];

    /**
     * Data input for total snow.
     */
    private Text myTotalSnowTF;

    /**
     * Data input for total water equivalent of snow.
     */
    private Text myTotalSnowWaterEquivTF;

    /**
     * Data input for days with any snow.
     */
    private Text myAnySnowGreaterThanTraceTF;

    /**
     * Data input for days with 1.00 inches or more snow.
     */
    private Text mySnowGreaterThan1TF;

    /**
     * Data input for days with some custom amount of inches of snow or more.
     */
    private Text mySnowCustomGreaterS1TF;

    /**
     * Data input for average snow depth.
     */
    private Text myAvgSnowDepthGroundTF;

    /**
     * Data value type selection for maximum snow depth.
     */
    private ComboViewer myMaxSnowDepthGroundComboBox;

    /**
     * Data input for maximum snow depth.
     */
    private QCTextComp myMaxSnowDepthGroundTF;

    /**
     * Date inputs for maximum snow depth.
     */
    private DateSelectionComp[] myMaxSnowDepthGroundDates = new DateSelectionComp[3];

    /**
     * Maximum snow in 24 hours label.
     */
    private MismatchLabel myMaxSnow24HourLabel;

    /**
     * Maximum snow depth on ground label.
     */
    private MismatchLabel myMaxSnowDepthGroundLabel;

    public SnowTab(TabFolder parent, int style,
            DisplayStationPeriodDialog periodDialog) {
        super(parent, style, periodDialog);

        myTabItem.setText("Snow");
        Composite snowComp = new Composite(parent, SWT.NONE);
        RowLayout snowRL = new RowLayout(SWT.VERTICAL);
        snowRL.center = true;
        snowRL.marginWidth = 20;
        snowComp.setLayout(snowRL);
        myTabItem.setControl(snowComp);
        createSnowTab(snowComp);
    }

    /**
     * Create snow tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createSnowTab(Composite parent) {
        Composite snowComp = new Composite(parent, SWT.NONE);
        GridLayout snowGL = new GridLayout(2, true);
        snowComp.setLayout(snowGL);

        // left
        Composite snowLeftComp = new Composite(snowComp, SWT.BORDER);
        RowLayout snowLeftRL = new RowLayout(SWT.VERTICAL);
        snowLeftRL.marginBottom = 175;
        snowLeftComp.setLayout(snowLeftRL);
        createSnowTabLeft(snowLeftComp);

        // right
        Composite snowRightComp = new Composite(snowComp, SWT.BORDER);
        RowLayout snowRightRL = new RowLayout(SWT.VERTICAL);
        snowRightRL.marginLeft = 20;
        snowRightComp.setLayout(snowRightRL);
        createSnowTabRight(snowRightComp);
    }

    /**
     * Create right half of snow tab (maximum measurements).
     * 
     * @param parent
     *            parent composite.
     */
    private void createSnowTabRight(Composite parent) {
        // 24-hour max snow
        Composite maxSnow24HourComp = new Composite(parent, SWT.NONE);
        GridLayout maxSnow24HourGL = new GridLayout(2, false);
        maxSnow24HourComp.setLayout(maxSnow24HourGL);

        myMaxSnow24HourLabel = new MismatchLabel(maxSnow24HourComp, SWT.NORMAL);
        myMaxSnow24HourLabel.setText("24-Hour Maximum\nSnowfall (in)");

        Composite maxSnow24HourFieldComp = new Composite(maxSnow24HourComp,
                SWT.NONE);
        RowLayout maxSnow24HourFieldRL = new RowLayout(SWT.HORIZONTAL);
        maxSnow24HourFieldRL.center = true;
        maxSnow24HourFieldComp.setLayout(maxSnow24HourFieldRL);

        myMaxSnow24HourComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(maxSnow24HourFieldComp);
        myMaxSnow24HourComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMaxSnow24HourComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMaxSnow24HourTF.setTextAndTip(
                                String.valueOf(iData.getSnowMax24H()),
                                iData.getDataMethods().getSnow24hrMaxQc());
                        int i = 0;
                        for (i = 0; i < myMaxSnow24HourBeginDates.length
                                && i < iData.getSnow24HDates().size(); i++) {
                            myMaxSnow24HourBeginDates[i].setDate(
                                    iData.getSnow24HDates().get(i).getStart());
                            myMaxSnow24HourEndDates[i].setDate(
                                    iData.getSnow24HDates().get(i).getEnd());
                        }
                        // clear any remaining fields
                        for (int j = i; j < myMaxSnow24HourBeginDates.length; j++) {
                            myMaxSnow24HourBeginDates[j].setMissing();
                            myMaxSnow24HourEndDates[j].setMissing();
                        }
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        if (myMaxSnow24HourTF.isFocusControl()) {
                            return true;
                        }
                        for (int i = 0; i < myMaxSnow24HourBeginDates.length; i++) {
                            if (myMaxSnow24HourBeginDates[i].isFocusControl()
                                    || myMaxSnow24HourEndDates[i]
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
                            recordSnowMax24Hour(data);
                            if (data.getSnowMax24H() != ParameterFormatClimate.MISSING_SNOW) {
                                data.getDataMethods().setSnow24hrMaxQc(
                                        QCValues.MANUAL_ENTRY);
                            } else {
                                data.getDataMethods().setSnow24hrMaxQc(
                                        ParameterFormatClimate.MISSING);
                            }
                        } catch (ParseException e) {
                            logger.error("Error parsing date.", e);
                        }
                    }
                });

        myMaxSnow24HourTF = new QCTextComp(maxSnow24HourFieldComp, SWT.NONE,
                "24-Hour Maximum Snowfall (in)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMaxSnow24HourTF.useRowData();
        myMaxSnow24HourTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        myMaxSnow24HourTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        myMaxSnow24HourTF
                .addKeyListener(new DataFieldListener(myMaxSnow24HourComboBox,
                        myPeriodDialog.myUnsavedChangesListener));

        // 24-hour max snow dates
        Composite maxSnow24HourDatesComp = new Composite(parent, SWT.NONE);
        GridLayout maxSnow24HourDatesGL = new GridLayout(4, false);
        maxSnow24HourDatesComp.setLayout(maxSnow24HourDatesGL);

        // first 24-hour max snow date
        Label maxSnow24HourBeginDate1Lbl = new Label(maxSnow24HourDatesComp,
                SWT.NORMAL);
        maxSnow24HourBeginDate1Lbl.setText("Begin Date");

        myMaxSnow24HourBeginDates[0] = new DateSelectionComp(
                maxSnow24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        DataFieldListener maxSnow24HourDatesListener = new DataFieldListener(
                myMaxSnow24HourComboBox,
                myPeriodDialog.myUnsavedChangesListener);
        myMaxSnow24HourBeginDates[0].addKeyListener(maxSnow24HourDatesListener);
        myMaxSnow24HourBeginDates[0]
                .addSelectionListener(maxSnow24HourDatesListener);

        Label maxSnow24HourEndDate1Lbl = new Label(maxSnow24HourDatesComp,
                SWT.NORMAL);
        maxSnow24HourEndDate1Lbl.setText("End Date");

        myMaxSnow24HourEndDates[0] = new DateSelectionComp(
                maxSnow24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnow24HourEndDates[0].addKeyListener(maxSnow24HourDatesListener);
        myMaxSnow24HourEndDates[0]
                .addSelectionListener(maxSnow24HourDatesListener);

        // second 24-hour max snow date
        Label maxSnow24HourBeginDate2Lbl = new Label(maxSnow24HourDatesComp,
                SWT.NORMAL);
        maxSnow24HourBeginDate2Lbl.setText("Begin Date");

        myMaxSnow24HourBeginDates[1] = new DateSelectionComp(
                maxSnow24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnow24HourBeginDates[1].addKeyListener(maxSnow24HourDatesListener);
        myMaxSnow24HourBeginDates[1]
                .addSelectionListener(maxSnow24HourDatesListener);

        Label maxSnow24HourEndDate2Lbl = new Label(maxSnow24HourDatesComp,
                SWT.NORMAL);
        maxSnow24HourEndDate2Lbl.setText("End Date");

        myMaxSnow24HourEndDates[1] = new DateSelectionComp(
                maxSnow24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnow24HourEndDates[1].addKeyListener(maxSnow24HourDatesListener);
        myMaxSnow24HourEndDates[1]
                .addSelectionListener(maxSnow24HourDatesListener);

        // third 24-hour max snow date
        Label maxSnow24HourBeginDate3Lbl = new Label(maxSnow24HourDatesComp,
                SWT.NORMAL);
        maxSnow24HourBeginDate3Lbl.setText("Begin Date");

        myMaxSnow24HourBeginDates[2] = new DateSelectionComp(
                maxSnow24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnow24HourBeginDates[2].addKeyListener(maxSnow24HourDatesListener);
        myMaxSnow24HourBeginDates[2]
                .addSelectionListener(maxSnow24HourDatesListener);

        Label maxSnow24HourEndDate3Lbl = new Label(maxSnow24HourDatesComp,
                SWT.NORMAL);
        maxSnow24HourEndDate3Lbl.setText("End Date");

        myMaxSnow24HourEndDates[2] = new DateSelectionComp(
                maxSnow24HourDatesComp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnow24HourEndDates[2].addKeyListener(maxSnow24HourDatesListener);
        myMaxSnow24HourEndDates[2]
                .addSelectionListener(maxSnow24HourDatesListener);

        // greatest snow storm
        Composite greatestSnowStormComp = new Composite(parent, SWT.NONE);
        GridLayout greatestSnowStormGL = new GridLayout(2, false);
        greatestSnowStormComp.setLayout(greatestSnowStormGL);

        Label greatestSnowStormLbl = new Label(greatestSnowStormComp,
                SWT.NORMAL);
        greatestSnowStormLbl.setText("Greatest Storm\nTotal (in)");

        myGreatestSnowStormTF = new Text(greatestSnowStormComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myGreatestSnowStormTF);
        myGreatestSnowStormTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        myGreatestSnowStormTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        myGreatestSnowStormTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // greatest snow storm dates and hours
        Composite greatestSnowStormDatesHoursComp = new Composite(parent,
                SWT.NONE);
        GridLayout greatestSnowStormDatesHoursGL = new GridLayout(2, false);
        greatestSnowStormDatesHoursComp
                .setLayout(greatestSnowStormDatesHoursGL);

        // greatest snow storm begin dates and hours
        Composite greatestSnowStormBeginDatesHoursComp = new Composite(
                greatestSnowStormDatesHoursComp, SWT.NONE);
        RowLayout greatestSnowStormBeginDatesHoursRL = new RowLayout(
                SWT.VERTICAL);
        greatestSnowStormBeginDatesHoursRL.center = true;
        greatestSnowStormBeginDatesHoursComp
                .setLayout(greatestSnowStormBeginDatesHoursRL);

        // first begin date and hour
        Composite greatestSnowStormBeginDate1Comp = new Composite(
                greatestSnowStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginDate1GL = new GridLayout(2, false);
        greatestSnowStormBeginDate1Comp
                .setLayout(greatestSnowStormBeginDate1GL);

        Label greatestSnowStormBeginDate1Lbl = new Label(
                greatestSnowStormBeginDate1Comp, SWT.NORMAL);
        greatestSnowStormBeginDate1Lbl.setText("Begin Date");

        myGreatestSnowStormBeginDates[0] = new DateSelectionComp(
                greatestSnowStormBeginDate1Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestSnowStormBeginDates[0].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite greatestSnowStormBeginHour1Comp = new Composite(
                greatestSnowStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginHour1GL = new GridLayout(2, false);
        greatestSnowStormBeginHour1Comp
                .setLayout(greatestSnowStormBeginHour1GL);

        Label greatestSnowStormBeginHour1Lbl = new Label(
                greatestSnowStormBeginHour1Comp, SWT.NORMAL);
        greatestSnowStormBeginHour1Lbl.setText("Hour");

        myGreatestSnowStormBeginHourTFs[0] = new Text(
                greatestSnowStormBeginHour1Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestSnowStormBeginHourTFs[0]);
        myGreatestSnowStormBeginHourTFs[0]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestSnowStormBeginHourTFs[0], true));
        myGreatestSnowStormBeginHourTFs[0].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // second begin date and hour
        Composite greatestSnowStormBeginDate2Comp = new Composite(
                greatestSnowStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginDate2GL = new GridLayout(2, false);
        greatestSnowStormBeginDate2Comp
                .setLayout(greatestSnowStormBeginDate2GL);

        Label greatestSnowStormBeginDate2Lbl = new Label(
                greatestSnowStormBeginDate2Comp, SWT.NORMAL);
        greatestSnowStormBeginDate2Lbl.setText("Begin Date");

        myGreatestSnowStormBeginDates[1] = new DateSelectionComp(
                greatestSnowStormBeginDate2Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestSnowStormBeginDates[1].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite greatestSnowStormBeginHour2Comp = new Composite(
                greatestSnowStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginHour2GL = new GridLayout(2, false);
        greatestSnowStormBeginHour2Comp
                .setLayout(greatestSnowStormBeginHour2GL);

        Label greatestSnowStormBeginHour2Lbl = new Label(
                greatestSnowStormBeginHour2Comp, SWT.NORMAL);
        greatestSnowStormBeginHour2Lbl.setText("Hour");

        myGreatestSnowStormBeginHourTFs[1] = new Text(
                greatestSnowStormBeginHour2Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestSnowStormBeginHourTFs[1]);
        myGreatestSnowStormBeginHourTFs[1]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestSnowStormBeginHourTFs[1], true));
        myGreatestSnowStormBeginHourTFs[1].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // third begin date and hour
        Composite greatestSnowStormBeginDate3Comp = new Composite(
                greatestSnowStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginDate3GL = new GridLayout(4, false);
        greatestSnowStormBeginDate3Comp
                .setLayout(greatestSnowStormBeginDate3GL);

        Label greatestSnowStormBeginDate3Lbl = new Label(
                greatestSnowStormBeginDate3Comp, SWT.NORMAL);
        greatestSnowStormBeginDate3Lbl.setText("Begin Date");

        myGreatestSnowStormBeginDates[2] = new DateSelectionComp(
                greatestSnowStormBeginDate3Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestSnowStormBeginDates[2].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite greatestSnowStormBeginHour3Comp = new Composite(
                greatestSnowStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginHour3GL = new GridLayout(2, false);
        greatestSnowStormBeginHour3Comp
                .setLayout(greatestSnowStormBeginHour3GL);

        Label greatestSnowStormBeginHour3Lbl = new Label(
                greatestSnowStormBeginHour3Comp, SWT.NORMAL);
        greatestSnowStormBeginHour3Lbl.setText("Hour");

        myGreatestSnowStormBeginHourTFs[2] = new Text(
                greatestSnowStormBeginHour3Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestSnowStormBeginHourTFs[2]);
        myGreatestSnowStormBeginHourTFs[2]
                .addFocusListener(new TimeSelectorFocusListener(
                        myGreatestSnowStormBeginHourTFs[2], true));
        myGreatestSnowStormBeginHourTFs[2].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // greatest snow storm end dates and hours
        Composite greatestSnowStormEndDatesHoursComp = new Composite(
                greatestSnowStormDatesHoursComp, SWT.NONE);
        RowLayout greatestSnowStormEndDatesHoursRL = new RowLayout(
                SWT.VERTICAL);
        greatestSnowStormEndDatesHoursRL.center = true;
        greatestSnowStormEndDatesHoursComp
                .setLayout(greatestSnowStormEndDatesHoursRL);

        // first end date and hour
        Composite greatestSnowStormEndDate1Comp = new Composite(
                greatestSnowStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndDate1GL = new GridLayout(4, false);
        greatestSnowStormEndDate1Comp.setLayout(greatestSnowStormEndDate1GL);

        Label greatestSnowStormEndDate1Lbl = new Label(
                greatestSnowStormEndDate1Comp, SWT.NORMAL);
        greatestSnowStormEndDate1Lbl.setText("End Date");

        myGreatestSnowStormEndDates[0] = new DateSelectionComp(
                greatestSnowStormEndDate1Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestSnowStormEndDates[0].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite greatestSnowStormEndHour1Comp = new Composite(
                greatestSnowStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndHour1GL = new GridLayout(2, false);
        greatestSnowStormEndHour1Comp.setLayout(greatestSnowStormEndHour1GL);

        Label greatestSnowStormEndHour1Lbl = new Label(
                greatestSnowStormEndHour1Comp, SWT.NORMAL);
        greatestSnowStormEndHour1Lbl.setText("Hour");

        myGreatestSnowStormEndHourTFs[0] = new Text(
                greatestSnowStormEndHour1Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestSnowStormEndHourTFs[0]);
        myGreatestSnowStormEndHourTFs[0].addFocusListener(
                new TimeSelectorFocusListener(myGreatestSnowStormEndHourTFs[0],
                        true));
        myGreatestSnowStormEndHourTFs[0].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // second end date and hour
        Composite greatestSnowStormEndDate2Comp = new Composite(
                greatestSnowStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndDate2GL = new GridLayout(4, false);
        greatestSnowStormEndDate2Comp.setLayout(greatestSnowStormEndDate2GL);

        Label greatestSnowStormEndDate2Lbl = new Label(
                greatestSnowStormEndDate2Comp, SWT.NORMAL);
        greatestSnowStormEndDate2Lbl.setText("End Date");

        myGreatestSnowStormEndDates[1] = new DateSelectionComp(
                greatestSnowStormEndDate2Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestSnowStormEndDates[1].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite greatestSnowStormEndHour2Comp = new Composite(
                greatestSnowStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndHour2GL = new GridLayout(2, false);
        greatestSnowStormEndHour2Comp.setLayout(greatestSnowStormEndHour2GL);

        Label greatestSnowStormEndHour2Lbl = new Label(
                greatestSnowStormEndHour2Comp, SWT.NORMAL);
        greatestSnowStormEndHour2Lbl.setText("Hour");

        myGreatestSnowStormEndHourTFs[1] = new Text(
                greatestSnowStormEndHour2Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestSnowStormEndHourTFs[1]);
        myGreatestSnowStormEndHourTFs[1].addFocusListener(
                new TimeSelectorFocusListener(myGreatestSnowStormEndHourTFs[1],
                        true));
        myGreatestSnowStormEndHourTFs[1].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // third end date and hour
        Composite greatestSnowStormEndDate3Comp = new Composite(
                greatestSnowStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndDate3GL = new GridLayout(4, false);
        greatestSnowStormEndDate3Comp.setLayout(greatestSnowStormEndDate3GL);

        Label greatestSnowStormEndDate3Lbl = new Label(
                greatestSnowStormEndDate3Comp, SWT.NORMAL);
        greatestSnowStormEndDate3Lbl.setText("End Date");

        myGreatestSnowStormEndDates[2] = new DateSelectionComp(
                greatestSnowStormEndDate3Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myGreatestSnowStormEndDates[2].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite greatestSnowStormEndHour3Comp = new Composite(
                greatestSnowStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndHour3GL = new GridLayout(2, false);
        greatestSnowStormEndHour3Comp.setLayout(greatestSnowStormEndHour3GL);

        Label greatestSnowStormEndHour3Lbl = new Label(
                greatestSnowStormEndHour3Comp, SWT.NORMAL);
        greatestSnowStormEndHour3Lbl.setText("Hour");

        myGreatestSnowStormEndHourTFs[2] = new Text(
                greatestSnowStormEndHour3Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues
                .assignShortFieldsGD(myGreatestSnowStormEndHourTFs[2]);
        myGreatestSnowStormEndHourTFs[2].addFocusListener(
                new TimeSelectorFocusListener(myGreatestSnowStormEndHourTFs[2],
                        true));
        myGreatestSnowStormEndHourTFs[2].addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);
    }

    /**
     * Create left half of snow tab (overall measurements).
     * 
     * @param parent
     *            parent composite.
     */
    private void createSnowTabLeft(Composite parent) {
        Composite topComp = new Composite(parent, SWT.NONE);
        GridLayout topGL = new GridLayout(2, false);
        topComp.setLayout(topGL);

        // total snow and water
        // total snow
        Label totalSnowLbl = new Label(topComp, SWT.NORMAL);
        totalSnowLbl.setText("Total Snowfall (in)");

        myTotalSnowTF = new Text(topComp, ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myTotalSnowTF);
        myTotalSnowTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        myTotalSnowTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        myTotalSnowTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // total water
        Label totalWaterEquivLbl = new Label(topComp, SWT.NORMAL);
        totalWaterEquivLbl.setText("Total Water Equivalent (in)");

        myTotalSnowWaterEquivTF = new Text(topComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myTotalSnowWaterEquivTF);
        myTotalSnowWaterEquivTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        myTotalSnowWaterEquivTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        myTotalSnowWaterEquivTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // days with snowfall
        Label daysWithSnowLbl = new Label(parent, SWT.NORMAL);
        daysWithSnowLbl.setText("Days with Snowfall");

        Composite bottomComp = new Composite(parent, SWT.NONE);
        GridLayout bottomGL = new GridLayout(3, false);
        bottomComp.setLayout(bottomGL);

        // any snowfall
        Label anySnowLbl = new Label(bottomComp, SWT.NORMAL);
        anySnowLbl.setText("Any Snowfall");

        myAnySnowGreaterThanTraceTF = new Text(bottomComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myAnySnowGreaterThanTraceTF);
        myAnySnowGreaterThanTraceTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myAnySnowGreaterThanTraceTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myAnySnowGreaterThanTraceTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite anySnowFiller = new Composite(bottomComp, SWT.NONE);
        anySnowFiller.setLayoutData(ClimateLayoutValues.getFillerGD());

        // snow 1.00 or more
        Label snow100GreaterLbl = new Label(bottomComp, SWT.NORMAL);
        snow100GreaterLbl.setText("1.00 in. or more");

        mySnowGreaterThan1TF = new Text(bottomComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(mySnowGreaterThan1TF);
        mySnowGreaterThan1TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        mySnowGreaterThan1TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        mySnowGreaterThan1TF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite snowGreaterThan1Filler = new Composite(bottomComp, SWT.NONE);
        snowGreaterThan1Filler.setLayoutData(ClimateLayoutValues.getFillerGD());

        // custom snow
        Label snowCustomGreaterS1Lbl = new Label(bottomComp, SWT.NORMAL);

        mySnowCustomGreaterS1TF = new Text(bottomComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(mySnowCustomGreaterS1TF);
        mySnowCustomGreaterS1TF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        mySnowCustomGreaterS1TF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        mySnowCustomGreaterS1TF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        if (myPeriodDialog.myGlobals
                .getS1() == ParameterFormatClimate.MISSING_SNOW) {
            snowCustomGreaterS1Lbl.setText("?? in. or more");
            snowCustomGreaterS1Lbl.setEnabled(false);
            mySnowCustomGreaterS1TF.setEnabled(false);
        } else {
            snowCustomGreaterS1Lbl
                    .setText(myPeriodDialog.myGlobals.getS1() + " in. or more");
        }

        Composite snowGreaterThanS1Filler = new Composite(bottomComp, SWT.NONE);
        snowGreaterThanS1Filler
                .setLayoutData(ClimateLayoutValues.getFillerGD());

        // average snow depth
        Label avgSnowDepthLbl = new Label(bottomComp, SWT.NORMAL);
        avgSnowDepthLbl.setText("Average Snow Depth (in)");

        myAvgSnowDepthGroundTF = new Text(bottomComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myAvgSnowDepthGroundTF);
        myAvgSnowDepthGroundTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        myAvgSnowDepthGroundTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        myAvgSnowDepthGroundTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        Composite avgSnowDepthFiller = new Composite(bottomComp, SWT.NONE);
        avgSnowDepthFiller.setLayoutData(ClimateLayoutValues.getFillerGD());

        myMaxSnowDepthGroundLabel = new MismatchLabel(bottomComp, SWT.NORMAL);
        myMaxSnowDepthGroundLabel.setText("Maximum Snow Depth (in)");

        myMaxSnowDepthGroundComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(bottomComp);
        myMaxSnowDepthGroundComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myMaxSnowDepthGroundComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMaxSnowDepthGroundTF.setTextAndTip(
                                String.valueOf(iData.getSnowGroundMax()),
                                iData.getDataMethods().getMaxDepthQc());
                        int i = 0;
                        for (i = 0; i < myMaxSnowDepthGroundDates.length
                                && i < iData.getSnow24HDates().size(); i++) {
                            myMaxSnowDepthGroundDates[i].setDate(
                                    iData.getSnow24HDates().get(i).getStart());
                        }
                        // clear any remaining fields
                        for (int j = i; j < myMaxSnowDepthGroundDates.length; j++) {
                            myMaxSnowDepthGroundDates[j].setMissing();
                        }
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        if (myMaxSnowDepthGroundTF.isFocusControl()) {
                            return true;
                        }
                        for (int i = 0; i < myMaxSnowDepthGroundDates.length; i++) {
                            if (myMaxSnowDepthGroundDates[i].isFocusControl()) {
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    protected void saveOtherData() {
                        try {
                            PeriodData data = getOtherPeriodData();
                            recordSnowGroundDepthMax(data);
                            if (data.getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW_VALUE) {
                                data.getDataMethods()
                                        .setMaxDepthQc(QCValues.MANUAL_ENTRY);
                            } else {
                                data.getDataMethods().setMaxDepthQc(
                                        ParameterFormatClimate.MISSING);
                            }
                        } catch (ParseException e) {
                            logger.error("Error parsing date.", e);
                        }
                    }
                });

        myMaxSnowDepthGroundTF = new QCTextComp(bottomComp, SWT.NONE,
                "Maximum Snow Depth (in)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMaxSnowDepthGroundTF.useGridData();
        myMaxSnowDepthGroundTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMaxSnowDepthGroundTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMaxSnowDepthGroundTF.addKeyListener(
                new DataFieldListener(myMaxSnowDepthGroundComboBox,
                        myPeriodDialog.myUnsavedChangesListener));

        // dates of max snowfall
        Composite maxSnowDatesComp = new Composite(parent, SWT.NONE);
        GridLayout maxSnowDatesGL = new GridLayout(4, false);
        maxSnowDatesComp.setLayout(maxSnowDatesGL);

        Label maxSnowDatesLbl = new Label(maxSnowDatesComp, SWT.NORMAL);
        maxSnowDatesLbl.setText("Date(s)");

        // first date of max snowfall
        myMaxSnowDepthGroundDates[0] = new DateSelectionComp(maxSnowDatesComp,
                SWT.NONE, myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnowDepthGroundDates[0].addKeyListener(
                new DataFieldListener(myMaxSnowDepthGroundComboBox,
                        myPeriodDialog.myUnsavedChangesListener));

        // second date of max snowfall
        myMaxSnowDepthGroundDates[1] = new DateSelectionComp(maxSnowDatesComp,
                SWT.NONE, myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnowDepthGroundDates[1].addKeyListener(
                new DataFieldListener(myMaxSnowDepthGroundComboBox,
                        myPeriodDialog.myUnsavedChangesListener));

        // third date of max snowfall
        myMaxSnowDepthGroundDates[2] = new DateSelectionComp(maxSnowDatesComp,
                SWT.NONE, myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxSnowDepthGroundDates[2].addKeyListener(
                new DataFieldListener(myMaxSnowDepthGroundComboBox,
                        myPeriodDialog.myUnsavedChangesListener));
    }

    @Override
    protected void saveValues(PeriodData dataToSave)
            throws VizException, NumberFormatException, ParseException {
        recordSnowMax24Hour(dataToSave);

        dataToSave.setSnowMaxStorm(myGreatestSnowStormTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(myGreatestSnowStormTF.getText()));
        dataToSave.getSnowStormList().clear();
        for (int i = 0; i < myGreatestSnowStormBeginDates.length; i++) {
            ClimateDate startDate = myGreatestSnowStormBeginDates[i].getDate();
            ClimateDate endDate = myGreatestSnowStormEndDates[i].getDate();
            int startHour = Integer
                    .parseInt(myGreatestSnowStormBeginHourTFs[i].getText());
            int endHour = Integer
                    .parseInt(myGreatestSnowStormEndHourTFs[i].getText());
            if (!startDate.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && !endDate.toMonthDayDateString()
                            .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && startHour != ParameterFormatClimate.MISSING_HOUR
                    && endHour != ParameterFormatClimate.MISSING_HOUR) {
                dataToSave.getSnowStormList().add(new ClimateDates(

                        startDate, endDate, startHour, endHour));
            }
        }

        dataToSave.setSnowTotal(myTotalSnowTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(myTotalSnowTF.getText()));
        dataToSave.setSnowWater(myTotalSnowWaterEquivTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(myTotalSnowWaterEquivTF.getText()));
        dataToSave.setNumSnowGreaterThanTR(
                Integer.parseInt(myAnySnowGreaterThanTraceTF.getText()));
        dataToSave.setNumSnowGreaterThan1(
                Integer.parseInt(mySnowGreaterThan1TF.getText()));

        if (mySnowCustomGreaterS1TF.isEnabled()) {
            dataToSave.setNumSnowGreaterThanS1(
                    Integer.parseInt(mySnowCustomGreaterS1TF.getText()));
        }

        dataToSave.setSnowGroundMean(myAvgSnowDepthGroundTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(myAvgSnowDepthGroundTF.getText()));

        recordSnowGroundDepthMax(dataToSave);
    }

    @Override
    protected boolean loadSavedData(PeriodData iSavedPeriodData,
            PeriodData msmPeriodData, PeriodData dailyPeriodData) {
        boolean snowTabMismatch = false;

        // snow total
        myTotalSnowTF.setText(String.valueOf(iSavedPeriodData.getSnowTotal()));

        // max snow 24H
        // check MSM first
        if (msmPeriodData != null && isPeriodDataEqualForFloat(
                iSavedPeriodData.getSnowMax24H(), msmPeriodData.getSnowMax24H(),
                iSavedPeriodData.getSnow24HDates(),
                msmPeriodData.getSnow24HDates(),
                myMaxSnow24HourBeginDates.length)) {
            DataFieldListener.setComboViewerSelection(myMaxSnow24HourComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && isPeriodDataEqualForFloat(iSavedPeriodData.getSnowMax24H(),
                        dailyPeriodData.getSnowMax24H(),
                        iSavedPeriodData.getSnow24HDates(),
                        dailyPeriodData.getSnow24HDates(),
                        myMaxSnow24HourBeginDates.length)) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(myMaxSnow24HourComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMaxSnow24HourComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (!ClimateUtilities.floatingEquals(
                        dailyPeriodData.getSnowMax24H(),
                        ParameterFormatClimate.MISSING_SNOW))
                && (!ClimateUtilities.floatingEquals(
                        msmPeriodData.getSnowMax24H(),
                        ParameterFormatClimate.MISSING_SNOW))) {
            if (!ClimateUtilities.floatingEquals(msmPeriodData.getSnowMax24H(),
                    dailyPeriodData.getSnowMax24H())) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxSnow24HourLabel.setNotMatched(
                        msmPeriodData.getSnowMax24H(),
                        dailyPeriodData.getSnowMax24H());
                snowTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxSnow24HourBeginDates.length,
                        dailyPeriodData.getSnow24HDates(),
                        msmPeriodData.getSnow24HDates())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxSnow24HourLabel.setNotMatched(
                            msmPeriodData.getSnow24HDates()
                                    .toArray(new ClimateDates[msmPeriodData
                                            .getSnow24HDates().size()]),
                            dailyPeriodData.getSnow24HDates()
                                    .toArray(new ClimateDates[dailyPeriodData
                                            .getSnow24HDates().size()]));
                    snowTabMismatch = true;
                } else {
                    myMaxSnow24HourLabel.setMatched();
                }
            }
        } else {
            myMaxSnow24HourLabel.setMatched();
        }

        // days with snow
        myAnySnowGreaterThanTraceTF.setText(
                String.valueOf(iSavedPeriodData.getNumSnowGreaterThanTR()));

        // days with snow => 1.0
        mySnowGreaterThan1TF.setText(
                String.valueOf(iSavedPeriodData.getNumSnowGreaterThan1()));

        // snow s1
        if (mySnowCustomGreaterS1TF.isEnabled()) {
            mySnowCustomGreaterS1TF.setText(
                    String.valueOf(iSavedPeriodData.getNumSnowGreaterThanS1()));
        }

        // max snow depth on ground 24H
        // check MSM first
        if (msmPeriodData != null && isPeriodDataEqualForFloat(
                iSavedPeriodData.getSnowGroundMax(),
                msmPeriodData.getSnowGroundMax(),
                iSavedPeriodData.getSnowGroundMaxDateList(),
                msmPeriodData.getSnowGroundMaxDateList(),
                myMaxSnowDepthGroundDates.length)) {
            DataFieldListener.setComboViewerSelection(
                    myMaxSnowDepthGroundComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null && isPeriodDataEqualForFloat(
                iSavedPeriodData.getSnowGroundMax(),
                dailyPeriodData.getSnowGroundMax(),
                iSavedPeriodData.getSnowGroundMaxDateList(),
                dailyPeriodData.getSnowGroundMaxDateList(),
                myMaxSnowDepthGroundDates.length)) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(
                    myMaxSnowDepthGroundComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myMaxSnowDepthGroundComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW)
                && (msmPeriodData
                        .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW)) {
            if (msmPeriodData.getSnowGroundMax() != dailyPeriodData
                    .getSnowGroundMax()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxSnow24HourLabel.setNotMatched(
                        msmPeriodData.getSnowGroundMax(),
                        dailyPeriodData.getSnowGroundMax());
                snowTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxSnowDepthGroundDates.length,
                        dailyPeriodData.getSnow24HDates(),
                        msmPeriodData.getSnow24HDates())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxSnowDepthGroundLabel.setNotMatched(
                            msmPeriodData.getSnowGroundMaxDateList()
                                    .toArray(new ClimateDate[msmPeriodData
                                            .getSnowGroundMaxDateList()
                                            .size()]),
                            dailyPeriodData.getSnowGroundMaxDateList()
                                    .toArray(new ClimateDate[dailyPeriodData
                                            .getSnowGroundMaxDateList()
                                            .size()]));
                    snowTabMismatch = true;
                } else {
                    myMaxSnowDepthGroundLabel.setMatched();
                }
            }
        } else {
            myMaxSnowDepthGroundLabel.setMatched();
        }

        if (snowTabMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return snowTabMismatch;
    }

    @Override
    protected boolean displayDailyBuildData(PeriodData iMonthlyAsosData,
            PeriodData iDailyBuildData) {
        boolean snowTabMismatch = false;

        // snow total
        myTotalSnowTF.setText(String.valueOf(iDailyBuildData.getSnowTotal()));

        // max snow 24H
        if (iMonthlyAsosData == null || !ClimateUtilities.floatingEquals(
                iDailyBuildData.getSnowMax24H(),
                ParameterFormatClimate.MISSING_SNOW)) {
            if (iMonthlyAsosData == null || ClimateUtilities.floatingEquals(
                    iMonthlyAsosData.getSnowMax24H(),
                    ParameterFormatClimate.MISSING_SNOW)) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMaxSnow24HourComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMaxSnow24HourLabel.setMatched();
            } else if (!ClimateUtilities.floatingEquals(
                    iMonthlyAsosData.getSnowMax24H(),
                    iDailyBuildData.getSnowMax24H())) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxSnow24HourLabel.setNotMatched(
                        iMonthlyAsosData.getSnowMax24H(),
                        iDailyBuildData.getSnowMax24H());
                snowTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxSnow24HourBeginDates.length,
                        iDailyBuildData.getSnow24HDates(),
                        iMonthlyAsosData.getSnow24HDates())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxSnow24HourLabel.setNotMatched(
                            iMonthlyAsosData.getSnow24HDates()
                                    .toArray(new ClimateDates[iMonthlyAsosData
                                            .getSnow24HDates().size()]),
                            iDailyBuildData.getSnow24HDates()
                                    .toArray(new ClimateDates[iDailyBuildData
                                            .getSnow24HDates().size()]));
                    snowTabMismatch = true;
                } else {
                    myMaxSnow24HourLabel.setMatched();
                }
            }
        } else {
            myMaxSnow24HourLabel.setMatched();
        }

        // days with snow
        myAnySnowGreaterThanTraceTF.setText(
                String.valueOf(iDailyBuildData.getNumSnowGreaterThanTR()));

        // days with snow => 1.0
        mySnowGreaterThan1TF.setText(
                String.valueOf(iDailyBuildData.getNumSnowGreaterThan1()));

        // snow s1
        if (mySnowCustomGreaterS1TF.isEnabled()) {
            mySnowCustomGreaterS1TF.setText(
                    String.valueOf(iDailyBuildData.getNumSnowGreaterThanS1()));
        }

        // max snow depth on ground 24H
        if (iMonthlyAsosData == null || iDailyBuildData
                .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW_VALUE) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getSnowGroundMax() == ParameterFormatClimate.MISSING_SNOW_VALUE) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMaxSnowDepthGroundComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myMaxSnowDepthGroundLabel.setMatched();
            } else if (iMonthlyAsosData.getSnowGroundMax() != iDailyBuildData
                    .getSnowGroundMax()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMaxSnowDepthGroundLabel.setNotMatched(
                        iMonthlyAsosData.getSnowGroundMax(),
                        iDailyBuildData.getSnowGroundMax());
                snowTabMismatch = true;
            } else {
                // neither monthly nor daily data is missing, and value is the
                // same, but need to check dates/times as well
                if (!ClimateUtilities.isListsEqualUpToCapacity(
                        myMaxSnowDepthGroundDates.length,
                        iDailyBuildData.getSnowGroundMaxDateList(),
                        iMonthlyAsosData.getSnowGroundMaxDateList())) {
                    // neither monthly nor daily data is missing, and the values
                    // match, but the dates/times do not, so flag the box
                    myMaxSnowDepthGroundLabel.setNotMatched(
                            iMonthlyAsosData.getSnowGroundMaxDateList()
                                    .toArray(new ClimateDate[iMonthlyAsosData
                                            .getSnowGroundMaxDateList()
                                            .size()]),
                            iDailyBuildData.getSnowGroundMaxDateList()
                                    .toArray(new ClimateDate[iDailyBuildData
                                            .getSnowGroundMaxDateList()
                                            .size()]));
                    snowTabMismatch = true;
                } else {
                    myMaxSnowDepthGroundLabel.setMatched();
                }
            }
        } else {
            myMaxSnowDepthGroundLabel.setMatched();
        }

        if (snowTabMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return snowTabMismatch;
    }

    @Override
    protected void displayMonthlyASOSData() {
        DataFieldListener.setComboViewerSelection(myMaxSnow24HourComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(myMaxSnowDepthGroundComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
    }

    @Override
    protected void clearValues() {
        myTotalSnowTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SNOW));
        myTotalSnowWaterEquivTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SNOW));

        myAnySnowGreaterThanTraceTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        mySnowGreaterThan1TF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        myAvgSnowDepthGroundTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SNOW));

        myMaxSnowDepthGroundTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SNOW));
        for (DateSelectionComp dateComp : myMaxSnowDepthGroundDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myMaxSnow24HourTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SNOW));
        for (DateSelectionComp dateComp : myMaxSnow24HourBeginDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }
        for (DateSelectionComp dateComp : myMaxSnow24HourEndDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myGreatestSnowStormTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SNOW));
        for (DateSelectionComp dateComp : myGreatestSnowStormBeginDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }
        for (DateSelectionComp dateComp : myGreatestSnowStormEndDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }
        for (Text text : myGreatestSnowStormBeginHourTFs) {
            text.setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        }
        for (Text text : myGreatestSnowStormEndHourTFs) {
            text.setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        }
    }

    /**
     * Record max 24-hour snow value and dates and QC to the given period data
     * object.
     * 
     * @param dataToSave
     */
    private void recordSnowMax24Hour(PeriodData dataToSave)
            throws ParseException {
        /*
         * explicitly use MSM/Daily DB values instead of text, for safer
         * checking of values in loading (otherwise may get false "Other"
         * selections on loading due to rounding)
         */
        String saveValue;
        DataValueOrigin comboSelection = (DataValueOrigin) myMaxSnow24HourComboBox
                .getStructuredSelection().getFirstElement();
        if (DataValueOrigin.MONTHLY_SUMMARY_MESSAGE.equals(comboSelection)) {
            saveValue = String.valueOf(myPeriodDialog.myMSMPeriodDataByStation
                    .get(myPeriodDialog.myCurrStation.getInformId())
                    .getSnowMax24H());
        } else if (DataValueOrigin.DAILY_DATABASE.equals(comboSelection)) {
            saveValue = String
                    .valueOf(
                            myPeriodDialog.myOriginalDataMap
                                    .get(myPeriodDialog.myCurrStation
                                            .getInformId())
                                    .getData().getSnowMax24H());
        } else {
            saveValue = myMaxSnow24HourTF.getText();
        }
        dataToSave.setSnowMax24H(
                saveValue.equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(saveValue));
        dataToSave.getSnow24HDates().clear();
        for (int i = 0; i < myMaxSnow24HourBeginDates.length; i++) {
            ClimateDate startDate = myMaxSnow24HourBeginDates[i].getDate();
            ClimateDate endDate = myMaxSnow24HourEndDates[i].getDate();
            if (!startDate.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && !endDate.toMonthDayDateString().equals(
                            ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getSnow24HDates().add(new ClimateDates(

                        startDate, endDate));
            }
        }
        dataToSave.getDataMethods()
                .setSnow24hrMaxQc(myMaxSnow24HourTF.getToolTip().getQcValue());
    }

    /**
     * Record max snow ground depth value and dates and QC to the given period
     * data object.
     * 
     * @param dataToSave
     */
    private void recordSnowGroundDepthMax(PeriodData dataToSave)
            throws ParseException {
        dataToSave.setSnowGroundMax(
                Integer.parseInt(myMaxSnowDepthGroundTF.getText()));
        dataToSave.getSnowGroundMaxDateList().clear();
        for (DateSelectionComp dateComp : myMaxSnowDepthGroundDates) {
            ClimateDate date = dateComp.getDate();
            if (!date.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getSnowGroundMaxDateList().add(date);
            }
        }
        dataToSave.getDataMethods().setMaxDepthQc(
                myMaxSnowDepthGroundTF.getToolTip().getQcValue());
    }
}
