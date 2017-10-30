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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.DataFieldListener;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.DataValueOrigin;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.MismatchLabel;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.QCTextComp;

/**
 * Sky and weather tab of the Period Display dialog.
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
public class SkyAndWeatherTab extends DisplayStationPeriodTabItem {
    /**
     * Data input for thunder.
     */
    private Text myThunderTF;

    /**
     * Data input for freezing rain.
     */
    private Text myFreezingRainTF;

    /**
     * Data input for light snow.
     */
    private Text myLightSnowTF;

    /**
     * Data input for mixed precipitation.
     */
    private Text myMixedPrecipTF;

    /**
     * Data input for light freezing rain.
     */
    private Text myLightFreezingRainTF;

    /**
     * Data input for ice pellets.
     */
    private Text myIcePelletsTF;

    /**
     * Data input for heavy rain.
     */
    private Text myHeavyRainTF;

    /**
     * Data input for hail.
     */
    private Text myHailTF;

    /**
     * Data input for fog.
     */
    private Text myFogTF;

    /**
     * Data input for rain.
     */
    private Text myRainTF;

    /**
     * Data input for heavy snow.
     */
    private Text myHeavySnowTF;

    /**
     * Data input for heavy fog.
     */
    private Text myHeavyFogTF;

    /**
     * Data input for light rain.
     */
    private Text myLightRainTF;

    /**
     * Data input for snow.
     */
    private Text mySnowTF;

    /**
     * Data input for haze.
     */
    private Text myHazeTF;

    /**
     * Data value type selection for fair clouds.
     */
    private ComboViewer myFairComboBox;

    /**
     * Data input for fair clouds.
     */
    private QCTextComp myFairTF;

    /**
     * Data value type selection for partly cloudy.
     */
    private ComboViewer myPartlyCloudyComboBox;

    /**
     * Data input for partly cloudy.
     */
    private QCTextComp myPartlyCloudyTF;

    /**
     * Data value type selection for cloudy.
     */
    private ComboViewer myMostlyCloudyComboBox;

    /**
     * Data input for cloudy.
     */
    private QCTextComp myMostlyCloudyTF;

    /**
     * Data value type selection for sunshine.
     */
    private ComboViewer myPercentPossSunshineComboBox;

    /**
     * Data input for sunshine.
     */
    private QCTextComp myPercentPossSunshineTF;

    /**
     * Data input for sky cover.
     */
    private Text mySkyCoverTF;

    /**
     * Fair days label.
     */
    private MismatchLabel myFairLabel;

    /**
     * Party cloudy days label.
     */
    private MismatchLabel myPartlyCloudyLabel;

    /**
     * Mostly cloudy days label.
     */
    private MismatchLabel myMostlyCloudyLabel;

    /**
     * Percent possible sunshine label.
     */
    private MismatchLabel myPercentPossSunshineLabel;

    public SkyAndWeatherTab(TabFolder parent, int style,
            DisplayStationPeriodDialog periodDialog) {
        super(parent, style, periodDialog);

        myTabItem.setText("Sky and Weather");
        Composite skyComp = new Composite(parent, SWT.NONE);
        RowLayout skyRL = new RowLayout(SWT.VERTICAL);
        skyRL.center = true;
        skyRL.spacing = 30;
        skyRL.marginLeft = 30;
        skyComp.setLayout(skyRL);
        myTabItem.setControl(skyComp);
        createSkyTab(skyComp);
    }

    /**
     * Create sky and weather tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createSkyTab(Composite parent) {
        // sky
        Group skyComp = new Group(parent, SWT.BORDER);
        skyComp.setText("Sky");
        RowLayout skyRL = new RowLayout(SWT.VERTICAL);
        skyRL.center = true;
        skyComp.setLayout(skyRL);

        createSkyTabTop(skyComp);

        createSkyTabMiddle(skyComp);

        // weather
        Group weatherComp = new Group(parent, SWT.BORDER);
        weatherComp.setText("Weather");
        RowLayout weatherRL = new RowLayout(SWT.VERTICAL);
        weatherRL.center = true;
        weatherComp.setLayout(weatherRL);

        createSkyTabBottom(weatherComp);
    }

    /**
     * Create bottom of sky tab (weather).
     * 
     * @param parent
     *            parent composite.
     */
    private void createSkyTabBottom(Composite parent) {
        Label numberOfDaysWithLbl = new Label(parent, SWT.NORMAL);
        numberOfDaysWithLbl.setText("Number of Days with...");

        Composite weatherFieldsComp = new Composite(parent, SWT.NONE);
        GridLayout weatherFieldsGL = new GridLayout(6, false);
        weatherFieldsGL.horizontalSpacing = 10;
        weatherFieldsGL.verticalSpacing = 10;
        weatherFieldsComp.setLayout(weatherFieldsGL);

        // thunder
        Label thunderLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        thunderLbl.setText("Thunder (TS)");

        myThunderTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myThunderTF);
        myThunderTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myThunderTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myThunderTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // freezing rain
        Label freezingRainLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        freezingRainLbl.setText("Freezing Rain (ZR)");

        myFreezingRainTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myFreezingRainTF);
        myFreezingRainTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myFreezingRainTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myFreezingRainTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // light snow
        Label lightSnowLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        lightSnowLbl.setText("Light Snow (S-)");

        myLightSnowTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myLightSnowTF);
        myLightSnowTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myLightSnowTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myLightSnowTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // mixed precip
        Label mixedPrecipLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        mixedPrecipLbl.setText("Mixed Precip");

        myMixedPrecipTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMixedPrecipTF);
        myMixedPrecipTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMixedPrecipTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMixedPrecipTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // light freezing rain
        Label lightFreezingRainLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        lightFreezingRainLbl.setText("Lgt. Fr. Rain (ZR-)");

        myLightFreezingRainTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myLightFreezingRainTF);
        myLightFreezingRainTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myLightFreezingRainTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myLightFreezingRainTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // ice pellets
        Label icePelletsLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        icePelletsLbl.setText("Ice Pellets (PL)");

        myIcePelletsTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myIcePelletsTF);
        myIcePelletsTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myIcePelletsTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myIcePelletsTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // heavy rain
        Label heavyRainLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        heavyRainLbl.setText("Heavy Rain (R+)");

        myHeavyRainTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myHeavyRainTF);
        myHeavyRainTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myHeavyRainTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myHeavyRainTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // hail
        Label hailLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        hailLbl.setText("Hail (GR)");

        myHailTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myHailTF);
        myHailTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myHailTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myHailTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // fog
        Label fogLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        fogLbl.setText("Fog (F)");

        myFogTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myFogTF);
        myFogTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myFogTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myFogTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // rain
        Label rainLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        rainLbl.setText("Rain (R)");

        myRainTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myRainTF);
        myRainTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myRainTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myRainTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // heavy snow
        Label heavySnowLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        heavySnowLbl.setText("Heavy Snow (S+)");

        myHeavySnowTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myHeavySnowTF);
        myHeavySnowTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myHeavySnowTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myHeavySnowTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // heavy fog
        Label heavyFogLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        heavyFogLbl.setText("Heavy Fog (F <= 1/4)");

        myHeavyFogTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myHeavyFogTF);
        myHeavyFogTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myHeavyFogTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myHeavyFogTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // light rain
        Label lightRainLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        lightRainLbl.setText("Light Rain (R-)");

        myLightRainTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myLightRainTF);
        myLightRainTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myLightRainTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myLightRainTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // snow
        Label snowLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        snowLbl.setText("Snow (S)");

        mySnowTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(mySnowTF);
        mySnowTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        mySnowTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        mySnowTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);

        // haze
        Label hazeLbl = new Label(weatherFieldsComp, SWT.NORMAL);
        hazeLbl.setText("Haze (H)");

        myHazeTF = new Text(weatherFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myHazeTF);
        myHazeTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myHazeTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myHazeTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);
    }

    /**
     * Create middle of sky tab (days of clouds).
     * 
     * @param parent
     *            parent composite.
     */
    private void createSkyTabMiddle(Composite parent) {
        Label numberOfDaysLbl = new Label(parent, SWT.NORMAL);
        numberOfDaysLbl.setText("Number of Days...");

        Composite skyMiddle = new Composite(parent, SWT.NONE);
        GridLayout skyMiddleGL = new GridLayout(3, false);
        skyMiddle.setLayout(skyMiddleGL);

        myFairLabel = new MismatchLabel(skyMiddle, SWT.NORMAL);
        myFairLabel.setText("Fair (0 - 0.3)");

        myFairComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(skyMiddle);
        myFairComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myFairComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myFairTF.setTextAndTip(
                                String.valueOf(iData.getNumFairDays()),
                                iData.getDataMethods().getFairDaysQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myFairTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordFairDays(data);
                        if (data.getNumFairDays() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setFairDaysQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setFairDaysQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myFairTF = new QCTextComp(skyMiddle, SWT.NONE, "Fair (0 - 0.3)",
                QCValueType.PERIOD, myPeriodDialog.myIsMonthly);
        myFairTF.useGridData();
        myFairTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myFairTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myFairTF.addKeyListener(new DataFieldListener(myFairComboBox,
                myPeriodDialog.myUnsavedChangesListener));

        myPartlyCloudyLabel = new MismatchLabel(skyMiddle, SWT.NORMAL);
        myPartlyCloudyLabel.setText("Partly Cloudy (0.4 - 0.7)");

        myPartlyCloudyComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(skyMiddle);
        myPartlyCloudyComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myPartlyCloudyComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myPartlyCloudyTF.setTextAndTip(
                                String.valueOf(iData.getNumPartlyCloudyDays()),
                                iData.getDataMethods().getPcDaysQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myPartlyCloudyTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordPartlyCloudyDays(data);
                        if (data.getNumPartlyCloudyDays() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setPcDaysQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPcDaysQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myPartlyCloudyTF = new QCTextComp(skyMiddle, SWT.NONE,
                "Partly Cloudy (0.4 - 0.7)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myPartlyCloudyTF.useGridData();
        myPartlyCloudyTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPartlyCloudyTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myPartlyCloudyTF
                .addKeyListener(new DataFieldListener(myPartlyCloudyComboBox,
                        myPeriodDialog.myUnsavedChangesListener));

        myMostlyCloudyLabel = new MismatchLabel(skyMiddle, SWT.NORMAL);
        myMostlyCloudyLabel.setText("Cloudy (0.8 - 1.0)");

        myMostlyCloudyComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(skyMiddle);
        myMostlyCloudyComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(myMostlyCloudyComboBox,
                        myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myMostlyCloudyTF.setTextAndTip(
                                String.valueOf(iData.getNumMostlyCloudyDays()),
                                iData.getDataMethods().getCloudyDaysQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myMostlyCloudyTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordMostlyCloudyDays(data);
                        if (data.getNumMostlyCloudyDays() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setCloudyDaysQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setCloudyDaysQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myMostlyCloudyTF = new QCTextComp(skyMiddle, SWT.NONE,
                "Cloudy (0.8 - 1.0)", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myMostlyCloudyTF.useGridData();
        myMostlyCloudyTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMostlyCloudyTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMostlyCloudyTF
                .addKeyListener(new DataFieldListener(myMostlyCloudyComboBox,
                        myPeriodDialog.myUnsavedChangesListener));
    }

    /**
     * Create top of sky tab (sunshine and cover).
     * 
     * @param parent
     *            parent composite.
     */
    private void createSkyTabTop(Composite parent) {
        Composite skyTopComp = new Composite(parent, SWT.NONE);
        GridLayout skyTopGL = new GridLayout(2, true);
        skyTopGL.marginLeft = 70;
        skyTopComp.setLayout(skyTopGL);

        // sunshine
        Composite sunshineComp = new Composite(skyTopComp, SWT.NONE);
        GridLayout sunshineGL = new GridLayout(2, false);
        sunshineComp.setLayout(sunshineGL);

        myPercentPossSunshineLabel = new MismatchLabel(sunshineComp,
                SWT.NORMAL);
        myPercentPossSunshineLabel.setText("Percent Possible Sunshine");

        Composite percentPossSunshineFieldComp = new Composite(sunshineComp,
                SWT.NONE);
        RowLayout percentPossSunshineFieldRL = new RowLayout(SWT.HORIZONTAL);
        percentPossSunshineFieldRL.center = true;
        percentPossSunshineFieldComp.setLayout(percentPossSunshineFieldRL);

        myPercentPossSunshineComboBox = myPeriodDialog
                .createDataValueOriginComboViewer(percentPossSunshineFieldComp);
        myPercentPossSunshineComboBox.addSelectionChangedListener(
                new DataValueOriginComboBoxListener(
                        myPercentPossSunshineComboBox, myPeriodDialog) {

                    @Override
                    protected void loadFieldData(PeriodData iData) {
                        myPercentPossSunshineTF.setTextAndTip(
                                String.valueOf(iData.getPossSun()),
                                iData.getDataMethods().getPossSunQc());
                    }

                    @Override
                    protected boolean isCurrentlyEditing() {
                        return myPercentPossSunshineTF.isFocusControl();
                    }

                    @Override
                    protected void saveOtherData() {
                        PeriodData data = getOtherPeriodData();
                        recordPercentPossibleSun(data);
                        if (data.getPossSun() != ParameterFormatClimate.MISSING) {
                            data.getDataMethods()
                                    .setPossSunQc(QCValues.MANUAL_ENTRY);
                        } else {
                            data.getDataMethods().setPossSunQc(
                                    ParameterFormatClimate.MISSING);
                        }
                    }
                });

        myPercentPossSunshineTF = new QCTextComp(percentPossSunshineFieldComp,
                SWT.NONE, "Percent Possible Sunshine", QCValueType.PERIOD,
                myPeriodDialog.myIsMonthly);
        myPercentPossSunshineTF.useRowData();
        myPercentPossSunshineTF.addListener(SWT.Verify,
                myDisplayListeners.getPercentListener());
        myPercentPossSunshineTF.addListener(SWT.FocusOut,
                myDisplayListeners.getPercentListener());
        myPercentPossSunshineTF.addKeyListener(
                new DataFieldListener(myPercentPossSunshineComboBox,
                        myPeriodDialog.myUnsavedChangesListener));

        // sky cover
        Composite skyCoverComp = new Composite(skyTopComp, SWT.NONE);
        GridLayout skyCoverGL = new GridLayout(2, false);
        skyCoverComp.setLayout(skyCoverGL);

        Label skyCoverLabel = new Label(skyCoverComp, SWT.NORMAL);
        skyCoverLabel.setText("Average Sky Cover");

        Composite skyCoverFieldComp = new Composite(skyCoverComp, SWT.NONE);
        RowLayout skyCoverFieldRL = new RowLayout(SWT.HORIZONTAL);
        skyCoverFieldRL.center = true;
        skyCoverFieldComp.setLayout(skyCoverFieldRL);

        mySkyCoverTF = new Text(skyCoverFieldComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(mySkyCoverTF);
        mySkyCoverTF.addListener(SWT.Verify,
                myDisplayListeners.getSkyCoverListener());
        mySkyCoverTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSkyCoverListener());
        mySkyCoverTF.addListener(SWT.Modify,
                myPeriodDialog.myUnsavedChangesListener);
    }

    @Override
    protected void saveValues(PeriodData dataToSave)
            throws VizException, NumberFormatException, ParseException {
        dataToSave.setNumThunderStorms(Integer.parseInt(myThunderTF.getText()));
        dataToSave.setNumFreezingRain(
                Integer.parseInt(myFreezingRainTF.getText()));
        dataToSave.setNumLightSnow(Integer.parseInt(myLightSnowTF.getText()));
        dataToSave
                .setNumMixedPrecip(Integer.parseInt(myMixedPrecipTF.getText()));
        dataToSave.setNumLightFreezingRain(
                Integer.parseInt(myLightFreezingRainTF.getText()));
        dataToSave.setNumIcePellets(Integer.parseInt(myIcePelletsTF.getText()));
        dataToSave.setNumHeavyRain(Integer.parseInt(myHeavyRainTF.getText()));
        dataToSave.setNumHail(Integer.parseInt(myHailTF.getText()));
        dataToSave.setNumFog(Integer.parseInt(myFogTF.getText()));
        dataToSave.setNumRain(Integer.parseInt(myRainTF.getText()));
        dataToSave.setNumHeavySnow(Integer.parseInt(myHeavySnowTF.getText()));
        dataToSave.setNumFogQuarterSM(Integer.parseInt(myHeavyFogTF.getText()));
        dataToSave.setNumLightRain(Integer.parseInt(myLightRainTF.getText()));
        dataToSave.setNumSnow(Integer.parseInt(mySnowTF.getText()));
        dataToSave.setNumHaze(Integer.parseInt(myHazeTF.getText()));

        recordFairDays(dataToSave);
        recordPartlyCloudyDays(dataToSave);
        recordMostlyCloudyDays(dataToSave);

        recordPercentPossibleSun(dataToSave);

        dataToSave.setMeanSkyCover(Float.parseFloat(mySkyCoverTF.getText()));
    }

    @Override
    protected boolean loadSavedData(PeriodData iSavedPeriodData,
            PeriodData msmPeriodData, PeriodData dailyPeriodData) {
        boolean skyAndWeatherMismatch = false;

        mySkyCoverTF
                .setText(String.valueOf(iSavedPeriodData.getMeanSkyCover()));

        // percent possible sun
        // check MSM first
        if (msmPeriodData != null && iSavedPeriodData
                .getPossSun() == msmPeriodData.getPossSun()) {
            DataFieldListener.setComboViewerSelection(
                    myPercentPossSunshineComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null && iSavedPeriodData
                .getPossSun() == dailyPeriodData.getPossSun()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(
                    myPercentPossSunshineComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(
                    myPercentPossSunshineComboBox, DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getPossSun() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getPossSun() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getPossSun() != dailyPeriodData
                        .getPossSun())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myPercentPossSunshineLabel.setNotMatched(msmPeriodData.getPossSun(),
                    dailyPeriodData.getPossSun());
            skyAndWeatherMismatch = true;
        } else {
            myPercentPossSunshineLabel.setMatched();
        }

        // mostly cloudy days
        // check MSM first
        if (msmPeriodData != null
                && iSavedPeriodData.getNumMostlyCloudyDays() == msmPeriodData
                        .getNumMostlyCloudyDays()) {
            DataFieldListener.setComboViewerSelection(myMostlyCloudyComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && iSavedPeriodData.getNumMostlyCloudyDays() == dailyPeriodData
                        .getNumMostlyCloudyDays()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(myMostlyCloudyComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myMostlyCloudyComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumMostlyCloudyDays() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumMostlyCloudyDays() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumMostlyCloudyDays() != dailyPeriodData
                        .getNumMostlyCloudyDays())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myMostlyCloudyLabel.setNotMatched(
                    msmPeriodData.getNumMostlyCloudyDays(),
                    dailyPeriodData.getNumMostlyCloudyDays());
            skyAndWeatherMismatch = true;
        } else {
            myMostlyCloudyLabel.setMatched();
        }

        // partly cloudy days
        // check MSM first
        if (msmPeriodData != null
                && iSavedPeriodData.getNumPartlyCloudyDays() == msmPeriodData
                        .getNumPartlyCloudyDays()) {
            DataFieldListener.setComboViewerSelection(myPartlyCloudyComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null
                && iSavedPeriodData.getNumPartlyCloudyDays() == dailyPeriodData
                        .getNumPartlyCloudyDays()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(myPartlyCloudyComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myPartlyCloudyComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumPartlyCloudyDays() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumPartlyCloudyDays() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumPartlyCloudyDays() != dailyPeriodData
                        .getNumPartlyCloudyDays())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myPartlyCloudyLabel.setNotMatched(
                    msmPeriodData.getNumPartlyCloudyDays(),
                    dailyPeriodData.getNumPartlyCloudyDays());
            skyAndWeatherMismatch = true;
        } else {
            myPartlyCloudyLabel.setMatched();
        }

        // fair days
        // check MSM first
        if (msmPeriodData != null && iSavedPeriodData
                .getNumFairDays() == msmPeriodData.getNumFairDays()) {
            DataFieldListener.setComboViewerSelection(myFairComboBox,
                    DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        } else if (dailyPeriodData != null && iSavedPeriodData
                .getNumFairDays() == dailyPeriodData.getNumFairDays()) {
            // check daily DB (could be null) second
            DataFieldListener.setComboViewerSelection(myFairComboBox,
                    DataValueOrigin.DAILY_DATABASE);
        } else {
            // use Other (never null) last
            DataFieldListener.setComboViewerSelection(myFairComboBox,
                    DataValueOrigin.OTHER);
        }
        // also check for mismatched values
        if (msmPeriodData != null && (dailyPeriodData != null)
                && (dailyPeriodData
                        .getNumFairDays() != ParameterFormatClimate.MISSING)
                && (msmPeriodData
                        .getNumFairDays() != ParameterFormatClimate.MISSING)
                && (msmPeriodData.getNumFairDays() != dailyPeriodData
                        .getNumFairDays())) {
            // neither monthly nor daily data is missing, and they are
            // different, so flag the text box
            myFairLabel.setNotMatched(msmPeriodData.getNumFairDays(),
                    dailyPeriodData.getNumFairDays());
            skyAndWeatherMismatch = true;
        } else {
            myFairLabel.setMatched();
        }

        myThunderTF.setText(
                String.valueOf(iSavedPeriodData.getNumThunderStorms()));
        myMixedPrecipTF
                .setText(String.valueOf(iSavedPeriodData.getNumMixedPrecip()));
        myHeavyRainTF
                .setText(String.valueOf(iSavedPeriodData.getNumHeavyRain()));
        myRainTF.setText(String.valueOf(iSavedPeriodData.getNumRain()));
        myLightRainTF
                .setText(String.valueOf(iSavedPeriodData.getNumLightRain()));
        myFreezingRainTF
                .setText(String.valueOf(iSavedPeriodData.getNumFreezingRain()));
        myLightFreezingRainTF.setText(
                String.valueOf(iSavedPeriodData.getNumLightFreezingRain()));
        myHailTF.setText(String.valueOf(iSavedPeriodData.getNumHail()));
        myHeavySnowTF
                .setText(String.valueOf(iSavedPeriodData.getNumHeavySnow()));
        mySnowTF.setText(String.valueOf(iSavedPeriodData.getNumSnow()));
        myLightSnowTF.setText(String.valueOf(iSavedPeriodData.getNumSnow()));
        myIcePelletsTF
                .setText(String.valueOf(iSavedPeriodData.getNumIcePellets()));
        myFogTF.setText(String.valueOf(iSavedPeriodData.getNumFog()));
        myHeavyFogTF
                .setText(String.valueOf(iSavedPeriodData.getNumFogQuarterSM()));
        myHazeTF.setText(String.valueOf(iSavedPeriodData.getNumHaze()));

        if (skyAndWeatherMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return skyAndWeatherMismatch;
    }

    @Override
    protected boolean displayDailyBuildData(PeriodData iMonthlyAsosData,
            PeriodData iDailyBuildData) {
        boolean skyAndWeatherMismatch = false;

        mySkyCoverTF.setText(String.valueOf(iDailyBuildData.getMeanSkyCover()));

        // mostly cloudy days
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumMostlyCloudyDays() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumMostlyCloudyDays() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myMostlyCloudyComboBox, DataValueOrigin.DAILY_DATABASE);
                myMostlyCloudyLabel.setMatched();
            } else if (iMonthlyAsosData
                    .getNumMostlyCloudyDays() != iDailyBuildData
                            .getNumMostlyCloudyDays()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myMostlyCloudyLabel.setNotMatched(
                        iMonthlyAsosData.getNumMostlyCloudyDays(),
                        iDailyBuildData.getNumMostlyCloudyDays());
                skyAndWeatherMismatch = true;
            }
        } else {
            myMostlyCloudyLabel.setMatched();
        }

        // partly cloudy days
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumPartlyCloudyDays() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumPartlyCloudyDays() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(
                        myPartlyCloudyComboBox, DataValueOrigin.DAILY_DATABASE);
                myPartlyCloudyLabel.setMatched();
            } else if (iMonthlyAsosData
                    .getNumPartlyCloudyDays() != iDailyBuildData
                            .getNumPartlyCloudyDays()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myPartlyCloudyLabel.setNotMatched(
                        iMonthlyAsosData.getNumPartlyCloudyDays(),
                        iDailyBuildData.getNumPartlyCloudyDays());
                skyAndWeatherMismatch = true;
            }
        } else {
            myPartlyCloudyLabel.setMatched();
        }

        // fair days
        if (iMonthlyAsosData == null || iDailyBuildData
                .getNumFairDays() != ParameterFormatClimate.MISSING) {
            if (iMonthlyAsosData == null || iMonthlyAsosData
                    .getNumFairDays() == ParameterFormatClimate.MISSING) {
                // monthly data is missing but daily is not, so set daily data
                DataFieldListener.setComboViewerSelection(myFairComboBox,
                        DataValueOrigin.DAILY_DATABASE);
                myFairLabel.setMatched();
            } else if (iMonthlyAsosData.getNumFairDays() != iDailyBuildData
                    .getNumFairDays()) {
                // neither monthly nor daily data is missing, and they are
                // different, so flag the text box
                myFairLabel.setNotMatched(iMonthlyAsosData.getNumFairDays(),
                        iDailyBuildData.getNumFairDays());
                skyAndWeatherMismatch = true;
            }
        } else {
            myFairLabel.setMatched();
        }

        myThunderTF
                .setText(String.valueOf(iDailyBuildData.getNumThunderStorms()));
        myMixedPrecipTF
                .setText(String.valueOf(iDailyBuildData.getNumMixedPrecip()));
        myHeavyRainTF
                .setText(String.valueOf(iDailyBuildData.getNumHeavyRain()));
        myRainTF.setText(String.valueOf(iDailyBuildData.getNumRain()));
        myLightRainTF
                .setText(String.valueOf(iDailyBuildData.getNumLightRain()));
        myFreezingRainTF
                .setText(String.valueOf(iDailyBuildData.getNumFreezingRain()));
        myLightFreezingRainTF.setText(
                String.valueOf(iDailyBuildData.getNumLightFreezingRain()));
        myHailTF.setText(String.valueOf(iDailyBuildData.getNumHail()));
        myHeavySnowTF
                .setText(String.valueOf(iDailyBuildData.getNumHeavySnow()));
        mySnowTF.setText(String.valueOf(iDailyBuildData.getNumSnow()));
        myLightSnowTF.setText(String.valueOf(iDailyBuildData.getNumSnow()));
        myIcePelletsTF
                .setText(String.valueOf(iDailyBuildData.getNumIcePellets()));
        myFogTF.setText(String.valueOf(iDailyBuildData.getNumFog()));
        myHeavyFogTF
                .setText(String.valueOf(iDailyBuildData.getNumFogQuarterSM()));
        myHazeTF.setText(String.valueOf(iDailyBuildData.getNumHaze()));

        if (skyAndWeatherMismatch) {
            myTabItem.setImage(MismatchLabel.VALUE_MISMATCH_ICON);
        } else {
            myTabItem.setImage(null);
        }
        // some mismatch state may have changed, so re-pack to preserve look and
        // feel
        myTabItem.getControl().pack();

        return skyAndWeatherMismatch;
    }

    @Override
    protected void displayMonthlyASOSData() {
        // percent sun
        DataFieldListener.setComboViewerSelection(myPercentPossSunshineComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);

        // cloud days
        DataFieldListener.setComboViewerSelection(myFairComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(myPartlyCloudyComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
        DataFieldListener.setComboViewerSelection(myMostlyCloudyComboBox,
                DataValueOrigin.MONTHLY_SUMMARY_MESSAGE);
    }

    @Override
    protected void clearValues() {
        myPercentPossSunshineTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        mySkyCoverTF.setText(String.valueOf(ParameterFormatClimate.MISSING));

        myFairTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myPartlyCloudyTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myMostlyCloudyTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));

        myThunderTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myMixedPrecipTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myHeavyRainTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myRainTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myLightRainTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myFreezingRainTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myLightFreezingRainTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING));
        myHailTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myHeavySnowTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        mySnowTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myLightSnowTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myIcePelletsTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myFogTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myHeavyFogTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myHazeTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
    }

    /**
     * Record fair days and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordFairDays(PeriodData dataToSave) {
        dataToSave.setNumFairDays(Integer.parseInt(myFairTF.getText()));
        dataToSave.getDataMethods()
                .setFairDaysQc(myFairTF.getToolTip().getQcValue());
    }

    /**
     * Record partly cloudy days and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordPartlyCloudyDays(PeriodData dataToSave) {
        dataToSave.setNumPartlyCloudyDays(
                Integer.parseInt(myPartlyCloudyTF.getText()));
        dataToSave.getDataMethods()
                .setPcDaysQc(myPartlyCloudyTF.getToolTip().getQcValue());
    }

    /**
     * Record mostly cloudy days and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordMostlyCloudyDays(PeriodData dataToSave) {
        dataToSave.setNumMostlyCloudyDays(
                Integer.parseInt(myMostlyCloudyTF.getText()));
        dataToSave.getDataMethods()
                .setCloudyDaysQc(myMostlyCloudyTF.getToolTip().getQcValue());
    }

    /**
     * Record percent possible sun and QC to the given period data object.
     * 
     * @param dataToSave
     */
    private void recordPercentPossibleSun(PeriodData dataToSave) {
        dataToSave.setPossSun(
                Integer.parseInt(myPercentPossSunshineTF.getText()));
        dataToSave.getDataMethods().setPossSunQc(
                myPercentPossSunshineTF.getToolTip().getQcValue());
    }
}
