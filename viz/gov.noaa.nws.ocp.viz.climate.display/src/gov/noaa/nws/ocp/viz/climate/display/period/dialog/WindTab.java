/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.climate.display.common.DisplayValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;

/**
 * Wind tab of the Period Display dialog.
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
public class WindTab extends DisplayStationPeriodTabItem {
    /**
     * Common string for wind speed labels.
     */
    private static final String SPEED_STRING = "Speed";

    /**
     * Checkbox for wind speed to be in knots.
     */
    private Button myWindSpeedsKnotsCheckbox;

    /**
     * Flag for current dialog setting for wind speed in knots.
     */
    private boolean myIsWindSpeedsKnots = false;

    /**
     * Label for maximum gust speed.
     */
    private Label myMaxGustSpeedLbl;

    /**
     * Data input for maximum gust speed.
     */
    private Text myMaxGustSpeedTF;

    /**
     * Data inputs for maximum gust direction.
     */
    private Text[] myMaxGustDirTFs = new Text[3];

    /**
     * Date inputs for maximum gust.
     */
    private DateSelectionComp[] myMaxGustDates = new DateSelectionComp[3];

    /**
     * Label for average wind speed.
     */
    private Label myAverageWindSpeedLbl;

    /**
     * Data input for average wind speed.
     */
    private Text myAverageWindSpeedTF;

    /**
     * Label for maximum wind speed.
     */
    private Label myMaxWindSpeedLbl;

    /**
     * Data input for maximum wind speed.
     */
    private Text myMaxWindSpeedTF;

    /**
     * Data inputs for maximum wind direction.
     */
    private Text[] myMaxWindDirTFs = new Text[3];

    /**
     * Date inputs for maximum wind.
     */
    private DateSelectionComp[] myMaxWindDates = new DateSelectionComp[3];

    /**
     * Data input for resultant wind direction.
     */
    private Text myResultantWindDirTF;

    /**
     * Label for resultant wind speed.
     */
    private Label myResultantWindSpeedLbl;

    /**
     * Data input for resultant wind speed.
     */
    private Text myResultantWindSpeedTF;

    public WindTab(TabFolder parent, int style,
            DisplayStationPeriodDialog periodDialog) {
        super(parent, style, periodDialog);

        myTabItem.setText("Wind");
        Composite windComp = new Composite(parent, SWT.NONE);
        RowLayout windRL = new RowLayout(SWT.VERTICAL);
        windRL.center = true;
        windRL.marginLeft = 90;
        windComp.setLayout(windRL);
        myTabItem.setControl(windComp);
        createWindTab(windComp);
    }

    /**
     * Create wind tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createWindTab(Composite parent) {
        // wind speed in knots checkbox, align left
        Composite windSpeedKnotsComp = new Composite(parent, SWT.NONE);
        GridLayout windSpeedKnotsGL = new GridLayout(3, true);
        windSpeedKnotsComp.setLayout(windSpeedKnotsGL);

        myWindSpeedsKnotsCheckbox = new Button(windSpeedKnotsComp, SWT.CHECK);
        myWindSpeedsKnotsCheckbox.setText("Select for wind speed in knots");
        myWindSpeedsKnotsCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // convert between knots and mph
                if (myIsWindSpeedsKnots != myWindSpeedsKnotsCheckbox
                        .getSelection()) {
                    myPeriodDialog.getChangeListener().setIgnoreChanges(true);

                    // selection value changed
                    myIsWindSpeedsKnots = myWindSpeedsKnotsCheckbox
                            .getSelection();

                    // figure out conversion
                    // also convert labels here
                    double multiplier;
                    if (myIsWindSpeedsKnots) {
                        // convert to knots
                        multiplier = 1 / ClimateUtilities.KNOTS_TO_MPH;
                        myMaxWindSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
                        myResultantWindSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
                        myMaxGustSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
                        myAverageWindSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
                    } else {
                        // convert to mph
                        multiplier = ClimateUtilities.KNOTS_TO_MPH;
                        myMaxWindSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.MPH_ABB);
                        myResultantWindSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.MPH_ABB);
                        myMaxGustSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.MPH_ABB);
                        myAverageWindSpeedLbl.setText(
                                SPEED_STRING + " " + DisplayValues.MPH_ABB);
                    }
                    // pack the wind tab to ensure labels do not get cut off
                    // myWindTab.getControl().pack();

                    // convert all wind fields, if they are not set to missing
                    // value
                    try {
                        float maxWindSpeedToConvert = Float
                                .parseFloat(myMaxWindSpeedTF.getText());

                        if ((int) maxWindSpeedToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            myMaxWindSpeedTF.setText(String.valueOf(
                                    maxWindSpeedToConvert * multiplier));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse max wind speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myMaxWindSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }

                    try {
                        float resultWindSpeedToConvert = Float
                                .parseFloat(myResultantWindSpeedTF.getText());

                        if ((int) resultWindSpeedToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            myResultantWindSpeedTF.setText(String.valueOf(
                                    resultWindSpeedToConvert * multiplier));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse resultant wind speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myResultantWindSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }

                    try {
                        float maxWindGustToConvert = Float
                                .parseFloat(myMaxGustSpeedTF.getText());

                        if ((int) maxWindGustToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            myMaxGustSpeedTF.setText(String.valueOf(
                                    maxWindGustToConvert * multiplier));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse max gust speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myMaxGustSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }

                    try {
                        float avgWindSpeedToConvert = Float
                                .parseFloat(myAverageWindSpeedTF.getText());

                        if ((int) avgWindSpeedToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            myAverageWindSpeedTF.setText(String.valueOf(
                                    avgWindSpeedToConvert * multiplier));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse average wind speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myAverageWindSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }

                    myPeriodDialog.getChangeListener().setIgnoreChanges(false);
                }
            }
        });

        // data fields
        Composite windComp = new Composite(parent, SWT.NONE);
        GridLayout windGL = new GridLayout(2, true);
        windGL.horizontalSpacing = 20;
        windComp.setLayout(windGL);

        // left
        Composite windLeftComp = new Composite(windComp, SWT.BORDER);
        RowLayout windLeftRL = new RowLayout(SWT.VERTICAL);
        windLeftComp.setLayout(windLeftRL);
        createWindTabLeft(windLeftComp);

        // right
        Composite windRightComp = new Composite(windComp, SWT.BORDER);
        RowLayout windRightRL = new RowLayout(SWT.VERTICAL);
        windRightRL.marginBottom = 45;
        windRightComp.setLayout(windRightRL);
        createWindTabRight(windRightComp);
    }

    /**
     * Create right half of wind tab (gust).
     * 
     * @param parent
     *            parent composite.
     */
    private void createWindTabRight(Composite parent) {
        // max gust
        Composite maxGustFieldComp = new Composite(parent, SWT.NONE);
        GridLayout maxGustFieldGL = new GridLayout(3, false);
        maxGustFieldGL.horizontalSpacing = 30;
        maxGustFieldComp.setLayout(maxGustFieldGL);

        Label maxGustLbl = new Label(maxGustFieldComp, SWT.NORMAL);
        maxGustLbl.setText("Maximum Gust");

        myMaxGustSpeedLbl = new Label(maxGustFieldComp, SWT.NORMAL);
        if (myIsWindSpeedsKnots) {
            myMaxGustSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
        } else {
            myMaxGustSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.MPH_ABB);
        }

        myMaxGustSpeedTF = new Text(maxGustFieldComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMaxGustSpeedTF);
        myMaxGustSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myMaxGustSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myMaxGustSpeedTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // max gust dir and dates
        Composite maxGustDirDatesComp = new Composite(parent, SWT.NONE);
        GridLayout maxGustDirDatesGL = new GridLayout(2, false);
        maxGustDirDatesComp.setLayout(maxGustDirDatesGL);

        // first dir and date
        Composite maxGustDir1Comp = new Composite(maxGustDirDatesComp,
                SWT.NONE);
        RowLayout maxGustDir1RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDir1RL.center = true;
        maxGustDir1Comp.setLayout(maxGustDir1RL);

        Label maxGustDir1Lbl = new Label(maxGustDir1Comp, SWT.NORMAL);
        maxGustDir1Lbl.setText("Dir (deg)");

        myMaxGustDirTFs[0] = new Text(maxGustDir1Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxGustDirTFs[0]);
        myMaxGustDirTFs[0].addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTFs[0].addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTFs[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite maxGustDate1Comp = new Composite(maxGustDirDatesComp,
                SWT.NONE);
        RowLayout maxGustDate1RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDate1RL.center = true;
        maxGustDate1Comp.setLayout(maxGustDate1RL);

        Label maxGustDate1Lbl = new Label(maxGustDate1Comp, SWT.NORMAL);
        maxGustDate1Lbl.setText("Date");

        myMaxGustDates[0] = new DateSelectionComp(maxGustDate1Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxGustDates[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // second dir and date
        Composite maxGustDir2Comp = new Composite(maxGustDirDatesComp,
                SWT.NONE);
        RowLayout maxGustDir2RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDir2RL.center = true;
        maxGustDir2Comp.setLayout(maxGustDir2RL);

        Label maxGustDir2Lbl = new Label(maxGustDir2Comp, SWT.NORMAL);
        maxGustDir2Lbl.setText("Dir (deg)");

        myMaxGustDirTFs[1] = new Text(maxGustDir2Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxGustDirTFs[1]);
        myMaxGustDirTFs[1].addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTFs[1].addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTFs[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite maxGustDate2Comp = new Composite(maxGustDirDatesComp,
                SWT.NONE);
        RowLayout maxGustDate2RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDate2RL.center = true;
        maxGustDate2Comp.setLayout(maxGustDate2RL);

        Label maxGustDate2Lbl = new Label(maxGustDate2Comp, SWT.NORMAL);
        maxGustDate2Lbl.setText("Date");

        myMaxGustDates[1] = new DateSelectionComp(maxGustDate2Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxGustDates[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // third dir and date
        Composite maxGustDir3Comp = new Composite(maxGustDirDatesComp,
                SWT.NONE);
        RowLayout maxGustDir3RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDir3RL.center = true;
        maxGustDir3Comp.setLayout(maxGustDir3RL);

        Label maxGustDir3Lbl = new Label(maxGustDir3Comp, SWT.NORMAL);
        maxGustDir3Lbl.setText("Dir (deg)");

        myMaxGustDirTFs[2] = new Text(maxGustDir3Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxGustDirTFs[2]);
        myMaxGustDirTFs[2].addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTFs[2].addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTFs[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite maxGustDate3Comp = new Composite(maxGustDirDatesComp,
                SWT.NONE);
        RowLayout maxGustDate3RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDate3RL.center = true;
        maxGustDate3Comp.setLayout(maxGustDate3RL);

        Label maxGustDate3Lbl = new Label(maxGustDate3Comp, SWT.NORMAL);
        maxGustDate3Lbl.setText("Date");

        myMaxGustDates[2] = new DateSelectionComp(maxGustDate3Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxGustDates[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // average wind
        Composite averageWindComp = new Composite(parent, SWT.NONE);
        GridLayout averageWindGL = new GridLayout(3, false);
        averageWindGL.horizontalSpacing = 30;
        averageWindComp.setLayout(averageWindGL);

        Label averageWindLbl = new Label(averageWindComp, SWT.NORMAL);
        averageWindLbl.setText("Average Wind");

        myAverageWindSpeedLbl = new Label(averageWindComp, SWT.NORMAL);
        if (myIsWindSpeedsKnots) {
            myAverageWindSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
        } else {
            myAverageWindSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.MPH_ABB);
        }

        myAverageWindSpeedTF = new Text(averageWindComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myAverageWindSpeedTF);
        myAverageWindSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myAverageWindSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myAverageWindSpeedTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());
    }

    /**
     * Create left half of wind tab (wind).
     * 
     * @param parent
     *            parent composite.
     */
    private void createWindTabLeft(Composite parent) {
        // max wind
        Composite maxWindFieldComp = new Composite(parent, SWT.NONE);
        GridLayout maxWindFieldGL = new GridLayout(3, false);
        maxWindFieldGL.horizontalSpacing = 30;
        maxWindFieldComp.setLayout(maxWindFieldGL);

        Label maxWindLbl = new Label(maxWindFieldComp, SWT.NORMAL);
        maxWindLbl.setText("Maximum Wind");

        myMaxWindSpeedLbl = new Label(maxWindFieldComp, SWT.NORMAL);
        if (myIsWindSpeedsKnots) {
            myMaxWindSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
        } else {
            myMaxWindSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.MPH_ABB);
        }

        myMaxWindSpeedTF = new Text(maxWindFieldComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMaxWindSpeedTF);
        myMaxWindSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myMaxWindSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myMaxWindSpeedTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // max wind dir and dates
        Composite maxWindDirDatesComp = new Composite(parent, SWT.NONE);
        GridLayout maxWindDirDatesGL = new GridLayout(2, false);
        maxWindDirDatesComp.setLayout(maxWindDirDatesGL);

        // first dir and date
        Composite maxWindDir1Comp = new Composite(maxWindDirDatesComp,
                SWT.NONE);
        RowLayout maxWindDir1RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDir1RL.center = true;
        maxWindDir1Comp.setLayout(maxWindDir1RL);

        Label maxWindDir1Lbl = new Label(maxWindDir1Comp, SWT.NORMAL);
        maxWindDir1Lbl.setText("Dir (deg)");

        myMaxWindDirTFs[0] = new Text(maxWindDir1Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxWindDirTFs[0]);
        myMaxWindDirTFs[0].addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTFs[0].addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTFs[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite maxWindDate1Comp = new Composite(maxWindDirDatesComp,
                SWT.NONE);
        RowLayout maxWindDate1RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDate1RL.center = true;
        maxWindDate1Comp.setLayout(maxWindDate1RL);

        Label maxWindDate1Lbl = new Label(maxWindDate1Comp, SWT.NORMAL);
        maxWindDate1Lbl.setText("Date");

        myMaxWindDates[0] = new DateSelectionComp(maxWindDate1Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxWindDates[0].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // second dir and date
        Composite maxWindDir2Comp = new Composite(maxWindDirDatesComp,
                SWT.NONE);
        RowLayout maxWindDir2RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDir2RL.center = true;
        maxWindDir2Comp.setLayout(maxWindDir2RL);

        Label maxWindDir2Lbl = new Label(maxWindDir2Comp, SWT.NORMAL);
        maxWindDir2Lbl.setText("Dir (deg)");

        myMaxWindDirTFs[1] = new Text(maxWindDir2Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxWindDirTFs[1]);
        myMaxWindDirTFs[1].addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTFs[1].addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTFs[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite maxWindDate2Comp = new Composite(maxWindDirDatesComp,
                SWT.NONE);
        RowLayout maxWindDate2RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDate2RL.center = true;
        maxWindDate2Comp.setLayout(maxWindDate2RL);

        Label maxWindDate2Lbl = new Label(maxWindDate2Comp, SWT.NORMAL);
        maxWindDate2Lbl.setText("Date");

        myMaxWindDates[1] = new DateSelectionComp(maxWindDate2Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxWindDates[1].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // third dir and date
        Composite maxWindDir3Comp = new Composite(maxWindDirDatesComp,
                SWT.NONE);
        RowLayout maxWindDir3RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDir3RL.center = true;
        maxWindDir3Comp.setLayout(maxWindDir3RL);

        Label maxWindDir3Lbl = new Label(maxWindDir3Comp, SWT.NORMAL);
        maxWindDir3Lbl.setText("Dir (deg)");

        myMaxWindDirTFs[2] = new Text(maxWindDir3Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxWindDirTFs[2]);
        myMaxWindDirTFs[2].addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTFs[2].addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTFs[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        Composite maxWindDate3Comp = new Composite(maxWindDirDatesComp,
                SWT.NONE);
        RowLayout maxWindDate3RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDate3RL.center = true;
        maxWindDate3Comp.setLayout(maxWindDate3RL);

        Label maxWindDate3Lbl = new Label(maxWindDate3Comp, SWT.NORMAL);
        maxWindDate3Lbl.setText("Date");

        myMaxWindDates[2] = new DateSelectionComp(maxWindDate3Comp, SWT.NONE,
                myPeriodDialog.myLowerBoundDate,
                myPeriodDialog.myUpperBoundDate);
        myMaxWindDates[2].addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // resultant wind
        Composite resultantWindComp = new Composite(parent, SWT.NONE);
        GridLayout resultantWindGL = new GridLayout(2, false);
        resultantWindGL.horizontalSpacing = 30;
        resultantWindComp.setLayout(resultantWindGL);

        Label resultantWindLbl = new Label(resultantWindComp, SWT.NORMAL);
        resultantWindLbl.setText("Resultant Wind");

        // resultant wind dir
        Composite resultantWindDirComp = new Composite(resultantWindComp,
                SWT.NONE);
        RowLayout resultantWindDirRL = new RowLayout(SWT.HORIZONTAL);
        resultantWindDirRL.center = true;
        resultantWindDirComp.setLayout(resultantWindDirRL);

        Label resultantWindDirLbl = new Label(resultantWindDirComp, SWT.NORMAL);
        resultantWindDirLbl.setText("Direction (deg)");

        myResultantWindDirTF = new Text(resultantWindDirComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myResultantWindDirTF);
        myResultantWindDirTF.addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myResultantWindDirTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myResultantWindDirTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());

        // resultant wind speed
        Label resultantWindSpeedSpacer = new Label(resultantWindComp,
                SWT.NORMAL);
        resultantWindSpeedSpacer.setText("");

        Composite resultantWindSpeedComp = new Composite(resultantWindComp,
                SWT.NONE);
        RowLayout resultantWindSpeedRL = new RowLayout(SWT.HORIZONTAL);
        resultantWindSpeedRL.center = true;
        resultantWindSpeedRL.spacing = 15;
        resultantWindSpeedComp.setLayout(resultantWindSpeedRL);

        myResultantWindSpeedLbl = new Label(resultantWindSpeedComp, SWT.NORMAL);
        if (myIsWindSpeedsKnots) {
            myResultantWindSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.KNOTS_ABB);
        } else {
            myResultantWindSpeedLbl
                    .setText(SPEED_STRING + " " + DisplayValues.MPH_ABB);
        }

        myResultantWindSpeedTF = new Text(resultantWindSpeedComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myResultantWindSpeedTF);
        myResultantWindSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myResultantWindSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myResultantWindSpeedTF.addListener(SWT.Modify,
                myPeriodDialog.getChangeListener());
    }

    @Override
    protected void saveValues(PeriodData dataToSave)
            throws VizException, NumberFormatException, ParseException {
        // convert wind speeds to mph if needed
        // knots/mph multiple
        float knotsmphMultiplier;
        if (myIsWindSpeedsKnots) {
            knotsmphMultiplier = (float) ClimateUtilities.KNOTS_TO_MPH;
        } else {
            knotsmphMultiplier = 1;
        }

        dataToSave.getMaxWindList().clear();
        float maxWindSpeed = Float.parseFloat(myMaxWindSpeedTF.getText());
        if ((int) maxWindSpeed != ParameterFormatClimate.MISSING_SPEED) {
            maxWindSpeed *= knotsmphMultiplier;
        }
        for (Text text : myMaxWindDirTFs) {
            int maxWindSpeedDir = Integer.parseInt(text.getText());
            if (maxWindSpeedDir != ParameterFormatClimate.MISSING) {
                dataToSave.getMaxWindList()
                        .add(new ClimateWind(maxWindSpeedDir, maxWindSpeed));
            }
        }
        // should have at least one max wind
        if (dataToSave.getMaxWindList().isEmpty()) {
            dataToSave.getMaxWindList().add(new ClimateWind(
                    ParameterFormatClimate.MISSING, maxWindSpeed));
        }

        dataToSave.getMaxWindDayList().clear();
        for (DateSelectionComp dateComp : myMaxWindDates) {
            ClimateDate date = dateComp.getDate();
            if (!date.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getMaxWindDayList().add(date);
            }
        }

        float avgWindSpeed = Float.parseFloat(myAverageWindSpeedTF.getText());
        if ((int) avgWindSpeed != ParameterFormatClimate.MISSING_SPEED) {
            avgWindSpeed *= knotsmphMultiplier;
        }
        dataToSave.setAvgWindSpd(avgWindSpeed);

        float resultWindSpeed = Float
                .parseFloat(myResultantWindSpeedTF.getText());
        if ((int) resultWindSpeed != ParameterFormatClimate.MISSING_SPEED) {
            resultWindSpeed *= knotsmphMultiplier;
        }
        dataToSave.setResultWind(new ClimateWind(
                Integer.parseInt(myResultantWindDirTF.getText()),
                resultWindSpeed));

        dataToSave.getMaxGustList().clear();
        float maxGustSpeed = Float.parseFloat(myMaxGustSpeedTF.getText());
        if ((int) maxGustSpeed != ParameterFormatClimate.MISSING_SPEED) {
            maxGustSpeed *= knotsmphMultiplier;
        }
        for (Text text : myMaxGustDirTFs) {
            int maxGustSpeedDir = Integer.parseInt(text.getText());
            if (maxGustSpeedDir != ParameterFormatClimate.MISSING) {
                dataToSave.getMaxGustList()
                        .add(new ClimateWind(maxGustSpeedDir, maxGustSpeed));
            }
        }
        // should have at least one max gust
        if (dataToSave.getMaxGustList().isEmpty()) {
            dataToSave.getMaxGustList().add(new ClimateWind(
                    ParameterFormatClimate.MISSING, maxGustSpeed));
        }

        dataToSave.getMaxGustDayList().clear();
        for (DateSelectionComp dateComp : myMaxGustDates) {
            ClimateDate date = dateComp.getDate();
            if (!date.toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dataToSave.getMaxGustDayList().add(date);
            }
        }
    }

    @Override
    protected boolean loadSavedData(PeriodData iSavedPeriodData,
            PeriodData msmPeriodData, PeriodData dailyPeriodData) {
        // max gust speed
        double maxGustSpeed;
        if (iSavedPeriodData.getMaxGustList().isEmpty()
                || ClimateUtilities.floatingEquals(
                        iSavedPeriodData.getMaxGustList().get(0).getSpeed(),
                        ParameterFormatClimate.MISSING_SPEED)) {
            // no speed data available
            maxGustSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // speed available, but need to show in knots
            maxGustSpeed = iSavedPeriodData.getMaxGustList().get(0).getSpeed()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            // speed is available
            maxGustSpeed = iSavedPeriodData.getMaxGustList().get(0).getSpeed();
        }
        myMaxGustSpeedTF.setText(String.valueOf(maxGustSpeed));

        // max gust directions
        for (int i = 0; i < myMaxGustDirTFs.length; i++) {
            if (iSavedPeriodData.getMaxGustList().size() < (i + 1)) {
                myMaxGustDirTFs[i].setText(
                        String.valueOf(ParameterFormatClimate.MISSING));
            } else {
                myMaxGustDirTFs[i].setText(String.valueOf(
                        iSavedPeriodData.getMaxGustList().get(i).getDir()));
            }
        }

        // max gust dates
        for (int i = 0; i < myMaxGustDates.length; i++) {
            if (iSavedPeriodData.getMaxGustDayList().size() < (i + 1)) {
                myMaxGustDates[i].setDate(ClimateDate.getMissingClimateDate());
            } else {
                myMaxGustDates[i]
                        .setDate(iSavedPeriodData.getMaxGustDayList().get(i));

            }
        }

        // average wind speed
        double avgWindSpeed;
        if (ClimateUtilities.floatingEquals(iSavedPeriodData.getAvgWindSpd(),
                ParameterFormatClimate.MISSING_SPEED)) {
            // missing speed
            avgWindSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // convert to knots
            avgWindSpeed = iSavedPeriodData.getAvgWindSpd()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            avgWindSpeed = iSavedPeriodData.getAvgWindSpd();
        }
        myAverageWindSpeedTF.setText(String.valueOf(avgWindSpeed));

        // max wind speed
        double maxWindSpeed;
        if (iSavedPeriodData.getMaxWindList().isEmpty()
                || ClimateUtilities.floatingEquals(
                        iSavedPeriodData.getMaxWindList().get(0).getSpeed(),
                        ParameterFormatClimate.MISSING_SPEED)) {
            // no speed data available
            maxWindSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // speed available, but need to show in knots
            maxWindSpeed = iSavedPeriodData.getMaxWindList().get(0).getSpeed()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            // speed is available
            maxWindSpeed = iSavedPeriodData.getMaxWindList().get(0).getSpeed();
        }
        myMaxWindSpeedTF.setText(String.valueOf(maxWindSpeed));

        // max wind directions
        for (int i = 0; i < myMaxGustDirTFs.length; i++) {
            if (iSavedPeriodData.getMaxWindList().size() < (i + 1)) {
                myMaxWindDirTFs[i].setText(
                        String.valueOf(ParameterFormatClimate.MISSING));
            } else {
                myMaxWindDirTFs[i].setText(String.valueOf(
                        iSavedPeriodData.getMaxWindList().get(i).getDir()));
            }
        }

        // max wind dates
        for (int i = 0; i < myMaxWindDates.length; i++) {
            if (iSavedPeriodData.getMaxWindDayList().size() < (i + 1)) {
                myMaxWindDates[i].setDate(ClimateDate.getMissingClimateDate());
            } else {
                myMaxWindDates[i]
                        .setDate(iSavedPeriodData.getMaxWindDayList().get(i));

            }
        }

        // resultant wind speed
        double resultWindSpeed;
        if (ClimateUtilities.floatingEquals(
                iSavedPeriodData.getResultWind().getSpeed(),
                ParameterFormatClimate.MISSING_SPEED)) {
            // missing speed
            resultWindSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // convert to knots
            resultWindSpeed = iSavedPeriodData.getResultWind().getSpeed()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            resultWindSpeed = iSavedPeriodData.getResultWind().getSpeed();
        }
        myResultantWindSpeedTF.setText(String.valueOf(resultWindSpeed));

        // resultant wind direction
        myResultantWindDirTF.setText(
                String.valueOf(iSavedPeriodData.getResultWind().getDir()));

        // MSM has no wind data for this display to mismatch with
        return false;
    }

    @Override
    protected boolean displayDailyBuildData(PeriodData iMonthlyAsosData,
            PeriodData iDailyBuildData) {
        // max gust speed
        double maxGustSpeed;
        if (iDailyBuildData.getMaxGustList().isEmpty()
                || ClimateUtilities.floatingEquals(
                        iDailyBuildData.getMaxGustList().get(0).getSpeed(),
                        ParameterFormatClimate.MISSING_SPEED)) {
            // no speed data available
            maxGustSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // speed available, but need to show in knots
            maxGustSpeed = iDailyBuildData.getMaxGustList().get(0).getSpeed()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            // speed is available
            maxGustSpeed = iDailyBuildData.getMaxGustList().get(0).getSpeed();
        }
        myMaxGustSpeedTF.setText(String.valueOf(maxGustSpeed));

        // max gust directions
        for (int i = 0; i < myMaxGustDirTFs.length; i++) {
            if (iDailyBuildData.getMaxGustList().size() < (i + 1)) {
                myMaxGustDirTFs[i].setText(
                        String.valueOf(ParameterFormatClimate.MISSING));
            } else {
                myMaxGustDirTFs[i].setText(String.valueOf(
                        iDailyBuildData.getMaxGustList().get(i).getDir()));
            }
        }

        // max gust dates
        for (int i = 0; i < myMaxGustDates.length; i++) {
            if (iDailyBuildData.getMaxGustDayList().size() < (i + 1)) {
                myMaxGustDates[i].setDate(ClimateDate.getMissingClimateDate());
            } else {
                myMaxGustDates[i]
                        .setDate(iDailyBuildData.getMaxGustDayList().get(i));

            }
        }

        // average wind speed
        double avgWindSpeed;
        if (ClimateUtilities.floatingEquals(iDailyBuildData.getAvgWindSpd(),
                ParameterFormatClimate.MISSING_SPEED)) {
            // missing speed
            avgWindSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // convert to knots
            avgWindSpeed = iDailyBuildData.getAvgWindSpd()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            avgWindSpeed = iDailyBuildData.getAvgWindSpd();
        }
        myAverageWindSpeedTF.setText(String.valueOf(avgWindSpeed));

        // max wind speed
        double maxWindSpeed;
        if (iDailyBuildData.getMaxWindList().isEmpty()
                || ClimateUtilities.floatingEquals(
                        iDailyBuildData.getMaxWindList().get(0).getSpeed(),
                        ParameterFormatClimate.MISSING_SPEED)) {
            // no speed data available
            maxWindSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // speed available, but need to show in knots
            maxWindSpeed = iDailyBuildData.getMaxWindList().get(0).getSpeed()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            // speed is available
            maxWindSpeed = iDailyBuildData.getMaxWindList().get(0).getSpeed();
        }
        myMaxWindSpeedTF.setText(String.valueOf(maxWindSpeed));

        // max wind directions
        for (int i = 0; i < myMaxGustDirTFs.length; i++) {
            if (iDailyBuildData.getMaxWindList().size() < (i + 1)) {
                myMaxWindDirTFs[i].setText(
                        String.valueOf(ParameterFormatClimate.MISSING));
            } else {
                myMaxWindDirTFs[i].setText(String.valueOf(
                        iDailyBuildData.getMaxWindList().get(i).getDir()));
            }
        }

        // max wind dates
        for (int i = 0; i < myMaxWindDates.length; i++) {
            if (iDailyBuildData.getMaxWindDayList().size() < (i + 1)) {
                myMaxWindDates[i].setDate(ClimateDate.getMissingClimateDate());
            } else {
                myMaxWindDates[i]
                        .setDate(iDailyBuildData.getMaxWindDayList().get(i));

            }
        }

        // resultant wind speed
        double resultWindSpeed;
        if (ClimateUtilities.floatingEquals(
                iDailyBuildData.getResultWind().getSpeed(),
                ParameterFormatClimate.MISSING_SPEED)) {
            // missing speed
            resultWindSpeed = ParameterFormatClimate.MISSING_SPEED;
        } else if (myIsWindSpeedsKnots) {
            // convert to knots
            resultWindSpeed = iDailyBuildData.getResultWind().getSpeed()
                    / ClimateUtilities.KNOTS_TO_MPH;
        } else {
            resultWindSpeed = iDailyBuildData.getResultWind().getSpeed();
        }
        myResultantWindSpeedTF.setText(String.valueOf(resultWindSpeed));

        // resultant wind direction
        myResultantWindDirTF.setText(
                String.valueOf(iDailyBuildData.getResultWind().getDir()));

        // MSM has no wind data for this display to mismatch with
        return false;
    }

    @Override
    protected void displayMonthlyASOSData() {
        // MSM has no wind data for this display
    }

    @Override
    protected void clearValues() {
        myMaxWindSpeedTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SPEED));
        for (Text text : myMaxWindDirTFs) {
            text.setText(String.valueOf(ParameterFormatClimate.MISSING));
        }
        for (DateSelectionComp dateComp : myMaxWindDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myResultantWindSpeedTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SPEED));
        myResultantWindDirTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        myMaxGustSpeedTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SPEED));
        for (Text text : myMaxGustDirTFs) {
            text.setText(String.valueOf(ParameterFormatClimate.MISSING));
        }
        for (DateSelectionComp dateComp : myMaxGustDates) {
            dateComp.setDate(ClimateDate.getMissingClimateDate());
        }

        myAverageWindSpeedTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SPEED));
    }

}
