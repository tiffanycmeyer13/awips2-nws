/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.qualitycontrol.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodDataMethod;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodDesc;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.QueryData;
import gov.noaa.nws.ocp.common.dataplugin.climate.SeasonType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.BuildPeriodServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.CompareUpdatePeriodRecordsRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.PeriodClimateServiceUpdateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.PeriodServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.climate.qualitycontrol.QCDataComposite;
import gov.noaa.nws.ocp.viz.common.climate.comp.AbstractDateComp;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.comp.MonthlyDayComp;
import gov.noaa.nws.ocp.viz.common.climate.comp.QCTextComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.TimeSelectorFocusListener;

/**
 * QCDataComposite child class representing a Period composite.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2016  20636      wpaintsil   Initial creation
 * Sep 02, 2016  20636      wpaintsil   Extend QCDataComposite
 * Sep 15, 2016  20636      wpaintsil   Period front end Implementation
 * Oct 04, 2016  20636      wpaintsil   loadData() Implementation
 * Oct 11, 2016  20636      wpaintsil   saveData() Implementation 
 * Oct 14, 2016  20636      wpaintsil   Change date text fields for monthly to show only the day.
 * Oct 21, 2016  22135      wpaintsil   Add data method tooltips.
 * Nov 17, 2016  20636      wpaintsil   Implementation of cascading updates to seasonal/annual 
 *                                      data after updating monthly data.
 * Nov 28, 2016  20636      wpaintsil   Update Records
 * Dec 02, 2016  20636      wpaintsil   Refactor some repetitive text creation
 * </pre>
 * 
 * @author wpaintsil
 */

public class PeriodSection extends QCDataComposite {
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PeriodSection.class);

    private final QCDialog qcDialog;

    private TabItem tempTab;

    private QCTextComp heatingDaysTF;

    private QCTextComp coolingDaysTF;

    private QCTextComp minTempLess32DegTF;

    private QCTextComp minTempLess0DegTF;

    private Text minTempGreaterDegCustomTF;

    private Text minTempLessDegCustom1TF;

    private Text minTempLessDegCustom2TF;

    private QCTextComp minTempTF;

    private AbstractDateComp[] minTempDates = new AbstractDateComp[3];

    private QCTextComp avgMinTempTF;

    private QCTextComp maxTempGreater90DegTF;

    private QCTextComp maxTempLess32DegTF;

    private Text maxTempGreaterDegCustom1TF;

    private Text maxTempGreaterDegCustom2TF;

    private Text maxTempLessDegCustomTF;

    private QCTextComp maxTempTF;

    private AbstractDateComp[] maxTempDates = new AbstractDateComp[3];

    private QCTextComp avgMaxTempTF;

    private QCTextComp meanTempTF;

    private TabItem precipTab;

    private QCTextComp maxPrecip24HourTF;

    private AbstractDateComp[] maxPrecip24HourBeginDates = new AbstractDateComp[3];

    private AbstractDateComp[] maxPrecip24HourEndDates = new AbstractDateComp[3];

    private Text greatestPrecipStormTF;

    private AbstractDateComp[] greatestPrecipStormBeginDates = new AbstractDateComp[3];

    private Text[] greatestPrecipStormBeginHourTFs = new Text[3];

    private AbstractDateComp[] greatestPrecipStormEndDates = new AbstractDateComp[3];

    private Text[] greatestPrecipStormEndHourTFs = new Text[3];

    private Text totalPrecipTF;

    private Text avgDailyPrecipTF;

    private QCTextComp precipInchesGreater01TF;

    private QCTextComp precipInchesGreater10TF;

    private QCTextComp precipInchesGreater50TF;

    private QCTextComp precipInchesGreater100TF;

    private Text precipInchesGreaterCustom1TF;

    private Text precipInchesGreaterCustom2TF;

    private QCTextComp maxSnow24HourTF;

    private AbstractDateComp[] maxSnow24HourBeginDates = new AbstractDateComp[3];

    private AbstractDateComp[] maxSnow24HourEndDates = new AbstractDateComp[3];

    private Text greatestSnowStormTF;

    private AbstractDateComp[] greatestSnowStormBeginDates = new AbstractDateComp[3];

    private Text[] greatestSnowStormBeginHourTFs = new Text[3];

    private AbstractDateComp[] greatestSnowStormEndDates = new AbstractDateComp[3];

    private Text[] greatestSnowStormEndHourTFs = new Text[3];

    private Text totalSnowTF;

    private Text totalSnowWaterEquivTF;

    private Text anySnowGreaterThanTraceTF;

    private Text snowGreaterThan1TF;

    private Text snowCustomGreaterTF;

    private Text avgSnowDepthGroundTF;

    private QCTextComp maxSnowDepthGroundTF;

    private AbstractDateComp[] maxSnowDepthGroundDates = new AbstractDateComp[3];

    private TabItem snowTab;

    private Text maxGustSpeedTF;

    private Text[] maxGustDirTFs = new Text[3];

    private AbstractDateComp[] maxGustDates = new AbstractDateComp[3];

    private Text averageWindSpeedTF;

    private Text maxWindSpeedTF;

    private Text[] maxWindDirTFs = new Text[3];

    private AbstractDateComp[] maxWindDates = new AbstractDateComp[3];

    private Text resultantWindDirTF;

    private Text resultantWindSpeedTF;

    private TabItem windTab;

    private Text thunderTF;

    private Text freezingRainTF;

    private Text lightSnowTF;

    private Text mixedPrecipTF;

    private Text lightFreezingRainTF;

    private Text icePelletsTF;

    private Text heavyRainTF;

    private Text hailTF;

    private Text fogTF;

    private Text rainTF;

    private Text heavySnowTF;

    private Text heavyFogTF;

    private Text lightRainTF;

    private Text snowTF;

    private Text hazeTF;

    private QCTextComp fairTF;

    private QCTextComp partlyCloudyTF;

    private QCTextComp mostlyCloudyTF;

    private QCTextComp percentPossSunshineTF;

    private Text skyCoverTF;

    private Text meanRelHumdTF;

    private TabItem skyAndWeatherTab;

    private final boolean isMonthly;

    public PeriodSection(QCDialog qcDialog, Composite parent, int style)
            throws ClimateInvalidParameterException {
        super(parent, style);

        this.qcDialog = qcDialog;

        isMonthly = this.qcDialog.dataType.equals(QCDialog.MONTHLY_SELECTION);

        this.setLayout(new GridLayout(1, false));

        setPeriodDesc();

        createTabbedSections();

    }

    /**
     * Set the periodDesc field for use in loadData() and saveData().
     * 
     * @throws ClimateInvalidParameterException
     */
    private void setPeriodDesc() throws ClimateInvalidParameterException {

        switch (this.qcDialog.dataType) {
        case QCDialog.MONTHLY_SELECTION:
            this.qcDialog.periodDesc = new PeriodDesc(
                    this.qcDialog.monthlyDate.getYear(),
                    this.qcDialog.monthlyDate.getMon());
            break;

        case QCDialog.SEASONAL_SELECTION:
            try {
                this.qcDialog.periodDesc = new PeriodDesc(
                        SeasonType.getSeasonTypeFromMonth(
                                this.qcDialog.seasonalDate.getMon()),
                        this.qcDialog.seasonalDate.getYear());
            } catch (ClimateInvalidParameterException e) {
                logger.error("Error: " + e.getMessage(), e);
            }
            break;

        case QCDialog.ANNUAL_SELECTION:
            this.qcDialog.periodDesc = new PeriodDesc(this.qcDialog.annualYear);
            break;

        default:
            this.qcDialog.periodDesc = new PeriodDesc();
            this.qcDialog.periodDesc
                    .setDates(ClimateDates.getMissingClimateDates());
            this.qcDialog.periodDesc.setPeriodType(PeriodType.OTHER);
            break;
        }
    }

    /**
     * Create date composite
     * 
     * @param parent
     * @return dateComp
     */
    private AbstractDateComp createDateComp(Composite parent) {
        AbstractDateComp dateComp;
        if (isMonthly) {
            dateComp = new MonthlyDayComp(parent, SWT.NONE,
                    this.qcDialog.monthlyDate);
        } else {
            dateComp = new DateSelectionComp(parent, SWT.NONE);
        }
        dateComp.addListener(SWT.Modify, this.qcDialog.getChangeListener());

        return dateComp;
    }

    /**
     * Create the tabbed section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTabbedSections() {
        final TabFolder tabFolder = new TabFolder(this, SWT.TOP | SWT.BORDER);

        tempTab = new TabItem(tabFolder, SWT.NONE);
        tempTab.setText("Temperature");
        Composite tempComp = new Composite(tabFolder, SWT.NONE);
        RowLayout tempRL = new RowLayout(SWT.VERTICAL);
        tempRL.center = true;
        tempRL.marginLeft = 120;
        tempComp.setLayout(tempRL);
        tempTab.setControl(tempComp);
        createTemperatureTab(tempComp);

        precipTab = new TabItem(tabFolder, SWT.NONE);
        precipTab.setText("Precipitation");
        Composite precipComp = new Composite(tabFolder, SWT.NONE);
        RowLayout precipRL = new RowLayout(SWT.VERTICAL);
        precipRL.center = true;
        precipRL.marginLeft = 40;
        precipComp.setLayout(precipRL);
        precipTab.setControl(precipComp);
        createPrecipitationTab(precipComp);

        snowTab = new TabItem(tabFolder, SWT.NONE);
        snowTab.setText("Snow");
        Composite snowComp = new Composite(tabFolder, SWT.NONE);
        RowLayout snowRL = new RowLayout(SWT.VERTICAL);
        snowRL.center = true;
        snowRL.marginWidth = 20;
        snowComp.setLayout(snowRL);
        snowTab.setControl(snowComp);
        createSnowTab(snowComp);

        windTab = new TabItem(tabFolder, SWT.NONE);
        windTab.setText("Wind");
        Composite windComp = new Composite(tabFolder, SWT.NONE);
        RowLayout windRL = new RowLayout(SWT.VERTICAL);
        windRL.center = true;
        windRL.marginLeft = 180;
        windRL.marginTop = 10;
        windComp.setLayout(windRL);
        windTab.setControl(windComp);
        createWindTab(windComp);

        skyAndWeatherTab = new TabItem(tabFolder, SWT.NONE);
        skyAndWeatherTab.setText("Sky and Weather");
        Composite skyComp = new Composite(tabFolder, SWT.NONE);
        RowLayout skyRL = new RowLayout(SWT.VERTICAL);
        skyRL.center = true;
        skyRL.spacing = 30;
        skyRL.marginLeft = 170;
        skyComp.setLayout(skyRL);
        skyAndWeatherTab.setControl(skyComp);
        createSkyTab(skyComp);

        tabFolder.setSelection(this.qcDialog.currentPeriodTabIndex);
        // Stay on the same tab when switching between data types.
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                PeriodSection.this.qcDialog.currentPeriodTabIndex = tabFolder
                        .getSelectionIndex();
            }
        });
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
        degreeDaysGL.horizontalSpacing = 100;
        degreeDaysComp.setLayout(degreeDaysGL);

        // heating days
        Composite heatingDaysComp = new Composite(degreeDaysComp, SWT.NONE);
        GridLayout heatingDaysGL = new GridLayout(2, false);
        heatingDaysComp.setLayout(heatingDaysGL);

        heatingDaysTF = createTextWithComposite(heatingDaysComp,
                "Heating Degree Days", "Heating Degree Days",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDegreeDaysListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // cooling days
        Composite coolingDaysComp = new Composite(degreeDaysComp, SWT.NONE);
        GridLayout coolingDaysGL = new GridLayout(2, false);
        coolingDaysComp.setLayout(coolingDaysGL);

        coolingDaysTF = createTextWithComposite(coolingDaysComp,
                "Cooling Degree Days", "Cooling Degree Days",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDegreeDaysListener(),
                this.qcDialog.getChangeListener(), isMonthly);
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
        GridLayout tempDataRightBotFieldsGL = new GridLayout(2, false);
        tempDataRightBotFields.setLayout(tempDataRightBotFieldsGL);

        minTempLess32DegTF = createText(tempDataRightBotFields,
                "32 deg F or Lower", "Days of Max Temp LE 32",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        minTempLess0DegTF = createText(tempDataRightBotFields,
                "0 deg F or Lower", "Days of Max Temp LE 0", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        boolean customMissing = this.qcDialog.climateGlobals
                .getT4() == ParameterFormatClimate.MISSING;
        minTempGreaterDegCustomTF = createTextWithComposite(
                tempDataRightBotFields,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getT4())
                        + " deg F or Higher",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(minTempGreaterDegCustomTF);

        customMissing = this.qcDialog.climateGlobals
                .getT5() == ParameterFormatClimate.MISSING;
        minTempLessDegCustom1TF = createTextWithComposite(
                tempDataRightBotFields,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getT5())
                        + " deg F or Lower",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(minTempLessDegCustom1TF);

        customMissing = this.qcDialog.climateGlobals
                .getT6() == ParameterFormatClimate.MISSING;
        minTempLessDegCustom2TF = createTextWithComposite(
                tempDataRightBotFields,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getT6())
                        + " deg F or Lower",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(minTempLessDegCustom2TF);

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
        GridLayout tempDataRightTopGL = new GridLayout(2, false);
        tempDataRightTop.setLayout(tempDataRightTopGL);

        minTempTF = createText(tempDataRightTop, "Minimum Temperature (F)",
                "Minimum Temperature", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getTempIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // dates of min temp
        Label minTempDatesLbl = new Label(tempDataRightTop, SWT.NORMAL);
        minTempDatesLbl.setText("Date(s) of\nMinimum Temperature");

        Composite minTempDatesComp = new Composite(tempDataRightTop, SWT.NONE);
        GridLayout minTempDatesGL = new GridLayout(1, false);
        minTempDatesComp.setLayout(minTempDatesGL);
        minTempDates[0] = createDateComp(minTempDatesComp);
        minTempDates[1] = createDateComp(minTempDatesComp);
        minTempDates[2] = createDateComp(minTempDatesComp);

        avgMinTempTF = createText(tempDataRightTop,
                "Average\nMinimum Temperature (F)",
                "Average Minimum Temperature", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultDoubleListener(),
                this.qcDialog.getChangeListener(), isMonthly);

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
        GridLayout tempDataLeftBotFieldsGL = new GridLayout(2, false);
        tempDataLeftBotFields.setLayout(tempDataLeftBotFieldsGL);

        maxTempGreater90DegTF = createText(tempDataLeftBotFields,
                "90 deg F or Higher", "Days of Max Temp GE 90",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        maxTempLess32DegTF = createText(tempDataLeftBotFields,
                "32 deg F or Lower", "Days of Max Temp LE 32",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        boolean customMissing = this.qcDialog.climateGlobals
                .getT1() == ParameterFormatClimate.MISSING;
        maxTempGreaterDegCustom1TF = createTextWithComposite(
                tempDataLeftBotFields,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getT1())
                        + " deg F or Higher",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(maxTempGreaterDegCustom1TF);

        customMissing = this.qcDialog.climateGlobals
                .getT2() == ParameterFormatClimate.MISSING;
        maxTempGreaterDegCustom2TF = createTextWithComposite(
                tempDataLeftBotFields,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getT2())
                        + " deg F or Higher",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(maxTempGreaterDegCustom2TF);

        customMissing = this.qcDialog.climateGlobals
                .getT3() == ParameterFormatClimate.MISSING;
        maxTempLessDegCustomTF = createTextWithComposite(tempDataLeftBotFields,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getT3())
                        + " deg F or Lower",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(maxTempLessDegCustomTF);

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
        GridLayout tempDataLeftTopGL = new GridLayout(2, false);
        tempDataLeftTop.setLayout(tempDataLeftTopGL);

        maxTempTF = createText(tempDataLeftTop, "Maximum Temperature (F)",
                "Maximum Temperature", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getTempIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // dates of max temp
        Label maxTempDatesLbl = new Label(tempDataLeftTop, SWT.NORMAL);
        maxTempDatesLbl.setText("Date(s) of\nMaximum Temperature");

        Composite maxTempDatesComp = new Composite(tempDataLeftTop, SWT.NONE);
        GridLayout maxTempDatesRL = new GridLayout(1, false);
        maxTempDatesComp.setLayout(maxTempDatesRL);
        maxTempDates[0] = createDateComp(maxTempDatesComp);
        maxTempDates[1] = createDateComp(maxTempDatesComp);
        maxTempDates[2] = createDateComp(maxTempDatesComp);

        avgMaxTempTF = createText(tempDataLeftTop,
                "Average\nMaximum Temperature (F)",
                "Average Maximum Temperature", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultDoubleListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        meanTempTF = createText(tempDataLeftTop, "Mean Temperature (F)",
                "Mean Temperature", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultDoubleListener(),
                this.qcDialog.getChangeListener(), isMonthly);
    }

    /**
     * Create precipitation tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createPrecipitationTab(Composite parent) {
        Composite precipComp = new Composite(parent, SWT.NONE);
        RowLayout precipRL = new RowLayout(SWT.HORIZONTAL);
        precipRL.center = true;
        precipRL.spacing = 20;
        precipComp.setLayout(precipRL);

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
        GridLayout maxPrecip24HourGL = new GridLayout(2, false);
        maxPrecip24HourComp.setLayout(maxPrecip24HourGL);

        maxPrecip24HourTF = createText(maxPrecip24HourComp,
                "24-Hour Maximum\nPrecipitation (in)",
                "24-Hour Maximum Precipitation", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getPrecipListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // 24-hour max precip dates
        Composite maxPrecip24HourDatesComp = new Composite(topComp, SWT.NONE);
        GridLayout maxPrecip24HourDatesGL = new GridLayout(4, false);
        maxPrecip24HourDatesComp.setLayout(maxPrecip24HourDatesGL);

        createMax24HourDatesSection(maxPrecip24HourDatesComp,
                maxPrecip24HourBeginDates, maxPrecip24HourEndDates);

        // greatest precip storm
        Composite bottomComp = new Composite(parent, SWT.NONE);
        RowLayout bottomRL = new RowLayout(SWT.VERTICAL);
        bottomRL.center = true;
        bottomComp.setLayout(bottomRL);

        // greatest storm measurement
        Composite greatestPrecipStormComp = new Composite(bottomComp, SWT.NONE);
        GridLayout greastestPrecipStormGL = new GridLayout(2, false);
        greatestPrecipStormComp.setLayout(greastestPrecipStormGL);

        greatestPrecipStormTF = createTextWithComposite(greatestPrecipStormComp,
                "Greatest Storm Total (in)",
                this.qcDialog.displayListeners.getPrecipListener(),
                this.qcDialog.getChangeListener());

        createGreatestStormSection(bottomComp, greatestPrecipStormBeginDates,
                greatestPrecipStormEndDates, greatestPrecipStormBeginHourTFs,
                greatestPrecipStormEndHourTFs);
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
        GridLayout topGL = new GridLayout(2, false);
        topComp.setLayout(topGL);

        totalPrecipTF = createTextWithComposite(topComp,
                "Total Precipitation (in)",
                this.qcDialog.displayListeners.getPrecipListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(totalPrecipTF);

        // average daily
        avgDailyPrecipTF = createTextWithComposite(topComp,
                "Average Daily\nPrecipitation (in)",
                this.qcDialog.displayListeners.getPrecipListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(avgDailyPrecipTF);

        // days with precip
        Label daysWithPrecipLbl = new Label(parent, SWT.NORMAL);
        daysWithPrecipLbl.setText("Days with Precipitation");

        Composite bottomComp = new Composite(parent, SWT.NONE);
        GridLayout bottomGL = new GridLayout(2, false);
        bottomComp.setLayout(bottomGL);

        precipInchesGreater01TF = createText(bottomComp, "0.01 in, or more",
                "Days of Precip GE 0.01 in", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        precipInchesGreater10TF = createText(bottomComp, "0.10 in, or more",
                "Days of Precip GE 0.10 in", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        precipInchesGreater50TF = createText(bottomComp, "0.50 in, or more",
                "Days of Precip GE 0.50 in", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        precipInchesGreater100TF = createText(bottomComp, "1.00 in, or more",
                "Days of Precip GE 1.00 in", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // first custom inches
        boolean customMissing = this.qcDialog.climateGlobals
                .getP1() == ParameterFormatClimate.MISSING;
        precipInchesGreaterCustom1TF = createTextWithComposite(bottomComp,

                (customMissing ? "??" : this.qcDialog.climateGlobals.getP1())
                        + " in, or more",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(precipInchesGreaterCustom1TF);

        // second custom inches
        customMissing = this.qcDialog.climateGlobals
                .getP2() == ParameterFormatClimate.MISSING;
        precipInchesGreaterCustom2TF = createTextWithComposite(bottomComp,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getP2())
                        + " in, or more",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsRD(precipInchesGreaterCustom2TF);
    }

    /**
     * Create snow tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createSnowTab(Composite parent) {
        Composite snowComp = new Composite(parent, SWT.NONE);
        RowLayout snowGL = new RowLayout(SWT.HORIZONTAL);
        snowGL.center = true;
        snowGL.spacing = 20;
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
        snowRightComp.setLayout(snowRightRL);
        createSnowTabRight(snowRightComp);
    }

    /**
     * Create section of paired-off begin/end dates for some max 24-hour
     * measurement (precip or snow).
     * 
     * @param parent
     *            parent composite
     * @param beginDates
     *            begin dates array to assign values to
     * @param endDates
     *            end dates array to assign values to
     */
    private void createMax24HourDatesSection(Composite parent,
            AbstractDateComp[] beginDates, AbstractDateComp[] endDates) {
        // first 24-hour max date
        Label max24HourBeginDate1Lbl = new Label(parent, SWT.NORMAL);
        max24HourBeginDate1Lbl.setText("Begin Date");

        beginDates[0] = createDateComp(parent);

        Label max24HourEndDate1Lbl = new Label(parent, SWT.NORMAL);
        max24HourEndDate1Lbl.setText("End Date");

        endDates[0] = createDateComp(parent);

        // second 24-hour max date
        Label max24HourBeginDate2Lbl = new Label(parent, SWT.NORMAL);
        max24HourBeginDate2Lbl.setText("Begin Date");

        beginDates[1] = createDateComp(parent);
        Label max24HourEndDate2Lbl = new Label(parent, SWT.NORMAL);
        max24HourEndDate2Lbl.setText("End Date");

        endDates[1] = createDateComp(parent);

        // third 24-hour max date
        Label max24HourBeginDate3Lbl = new Label(parent, SWT.NORMAL);
        max24HourBeginDate3Lbl.setText("Begin Date");

        beginDates[2] = createDateComp(parent);

        Label max24HourEndDate3Lbl = new Label(parent, SWT.NORMAL);
        max24HourEndDate3Lbl.setText("End Date");

        endDates[2] = createDateComp(parent);
    }

    /**
     * Create section of paired-off begin/end dates for some max 24-hour
     * measurement (precip or snow).
     * 
     * @param parent
     *            parent composite
     * @param beginDates
     *            begin dates array to assign values to
     * @param endDates
     *            end dates array to assign values to
     * @param beginHourTFs
     *            begin hours array to assign values to
     * @param endHourTFs
     *            end hours array to assign values to
     */
    private void createGreatestStormSection(Composite parent,
            AbstractDateComp[] beginDates, AbstractDateComp[] endDates,
            Text[] beginHourTFs, Text[] endHourTFs) {
        // greatest storm dates and hours
        Composite greatestStormDatesHoursComp = new Composite(parent, SWT.NONE);
        GridLayout greatestSnowStormDatesHoursGL = new GridLayout(2, false);
        greatestStormDatesHoursComp.setLayout(greatestSnowStormDatesHoursGL);

        // greatest storm begin dates and hours
        Composite greatestStormBeginDatesHoursComp = new Composite(
                greatestStormDatesHoursComp, SWT.NONE);
        RowLayout greatestStormBeginDatesHoursRL = new RowLayout(SWT.VERTICAL);
        greatestStormBeginDatesHoursRL.center = true;
        greatestStormBeginDatesHoursComp
                .setLayout(greatestStormBeginDatesHoursRL);

        // first begin date and hour
        Composite greatestStormBeginDate1Comp = new Composite(
                greatestStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestStormBeginDate1GL = new GridLayout(2, false);
        greatestStormBeginDate1Comp.setLayout(greatestStormBeginDate1GL);

        Label greatestStormBeginDate1Lbl = new Label(
                greatestStormBeginDate1Comp, SWT.NORMAL);
        greatestStormBeginDate1Lbl.setText("Begin Date");

        beginDates[0] = createDateComp(greatestStormBeginDate1Comp);

        Composite greatestStormBeginHour1Comp = new Composite(
                greatestStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestStormBeginHour1GL = new GridLayout(2, false);
        greatestStormBeginHour1Comp.setLayout(greatestStormBeginHour1GL);

        Label greatestStormBeginHour1Lbl = new Label(
                greatestStormBeginHour1Comp, SWT.NORMAL);
        greatestStormBeginHour1Lbl.setText("Hour");

        beginHourTFs[0] = new Text(greatestStormBeginHour1Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(beginHourTFs[0]);
        beginHourTFs[0].addFocusListener(
                new TimeSelectorFocusListener(beginHourTFs[0], true));
        beginHourTFs[0].addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        // second begin date and hour
        Composite greatestStormBeginDate2Comp = new Composite(
                greatestStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormBeginDate2GL = new GridLayout(2, false);
        greatestStormBeginDate2Comp.setLayout(greatestSnowStormBeginDate2GL);

        Label greatestStormBeginDate2Lbl = new Label(
                greatestStormBeginDate2Comp, SWT.NORMAL);
        greatestStormBeginDate2Lbl.setText("Begin Date");

        beginDates[1] = createDateComp(greatestStormBeginDate2Comp);

        Composite greatestStormBeginHour2Comp = new Composite(
                greatestStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestStormBeginHour2GL = new GridLayout(2, false);
        greatestStormBeginHour2Comp.setLayout(greatestStormBeginHour2GL);

        Label greatestStormBeginHour2Lbl = new Label(
                greatestStormBeginHour2Comp, SWT.NORMAL);
        greatestStormBeginHour2Lbl.setText("Hour");

        beginHourTFs[1] = new Text(greatestStormBeginHour2Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(beginHourTFs[1]);
        beginHourTFs[1].addFocusListener(
                new TimeSelectorFocusListener(beginHourTFs[1], true));
        beginHourTFs[1].addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        // third begin date and hour
        Composite greatestStormBeginDate3Comp = new Composite(
                greatestStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestStormBeginDate3GL = new GridLayout(4, false);
        greatestStormBeginDate3Comp.setLayout(greatestStormBeginDate3GL);

        Label greatestStormBeginDate3Lbl = new Label(
                greatestStormBeginDate3Comp, SWT.NORMAL);
        greatestStormBeginDate3Lbl.setText("Begin Date");

        beginDates[2] = createDateComp(greatestStormBeginDate3Comp);

        Composite greatestStormBeginHour3Comp = new Composite(
                greatestStormBeginDatesHoursComp, SWT.NONE);
        GridLayout greatestStormBeginHour3GL = new GridLayout(2, false);
        greatestStormBeginHour3Comp.setLayout(greatestStormBeginHour3GL);

        Label greatestStormBeginHour3Lbl = new Label(
                greatestStormBeginHour3Comp, SWT.NORMAL);
        greatestStormBeginHour3Lbl.setText("Hour");

        beginHourTFs[2] = new Text(greatestStormBeginHour3Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(beginHourTFs[2]);
        beginHourTFs[2].addFocusListener(
                new TimeSelectorFocusListener(beginHourTFs[2], true));
        beginHourTFs[2].addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        // greatest storm end dates and hours
        Composite greatestStormEndDatesHoursComp = new Composite(
                greatestStormDatesHoursComp, SWT.NONE);
        RowLayout greatestStormEndDatesHoursRL = new RowLayout(SWT.VERTICAL);
        greatestStormEndDatesHoursRL.center = true;
        greatestStormEndDatesHoursComp.setLayout(greatestStormEndDatesHoursRL);

        // first end date and hour
        Composite greatestStormEndDate1Comp = new Composite(
                greatestStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestStormEndDate1GL = new GridLayout(4, false);
        greatestStormEndDate1Comp.setLayout(greatestStormEndDate1GL);

        Label greatestStormEndDate1Lbl = new Label(greatestStormEndDate1Comp,
                SWT.NORMAL);
        greatestStormEndDate1Lbl.setText("End Date");

        endDates[0] = createDateComp(greatestStormEndDate1Comp);

        Composite greatestStormEndHour1Comp = new Composite(
                greatestStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndHour1GL = new GridLayout(2, false);
        greatestStormEndHour1Comp.setLayout(greatestSnowStormEndHour1GL);

        Label greatestStormEndHour1Lbl = new Label(greatestStormEndHour1Comp,
                SWT.NORMAL);
        greatestStormEndHour1Lbl.setText("Hour");

        endHourTFs[0] = new Text(greatestStormEndHour1Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(endHourTFs[0]);
        endHourTFs[0].addFocusListener(
                new TimeSelectorFocusListener(endHourTFs[0], true));
        endHourTFs[0].addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        // second end date and hour
        Composite greatestStormEndDate2Comp = new Composite(
                greatestStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestSnowStormEndDate2GL = new GridLayout(4, false);
        greatestStormEndDate2Comp.setLayout(greatestSnowStormEndDate2GL);

        Label greatestStormEndDate2Lbl = new Label(greatestStormEndDate2Comp,
                SWT.NORMAL);
        greatestStormEndDate2Lbl.setText("End Date");

        endDates[1] = createDateComp(greatestStormEndDate2Comp);

        Composite greatestStormEndHour2Comp = new Composite(
                greatestStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestStormEndHour2GL = new GridLayout(2, false);
        greatestStormEndHour2Comp.setLayout(greatestStormEndHour2GL);

        Label greatestStormEndHour2Lbl = new Label(greatestStormEndHour2Comp,
                SWT.NORMAL);
        greatestStormEndHour2Lbl.setText("Hour");

        endHourTFs[1] = new Text(greatestStormEndHour2Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(endHourTFs[1]);
        endHourTFs[1].addFocusListener(
                new TimeSelectorFocusListener(endHourTFs[1], true));
        endHourTFs[1].addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        // third end date and hour
        Composite greatestStormEndDate3Comp = new Composite(
                greatestStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestStormEndDate3GL = new GridLayout(4, false);
        greatestStormEndDate3Comp.setLayout(greatestStormEndDate3GL);

        Label greatestStormEndDate3Lbl = new Label(greatestStormEndDate3Comp,
                SWT.NORMAL);
        greatestStormEndDate3Lbl.setText("End Date");

        endDates[2] = createDateComp(greatestStormEndDate3Comp);

        Composite greatestStormEndHour3Comp = new Composite(
                greatestStormEndDatesHoursComp, SWT.NONE);
        GridLayout greatestStormEndHour3GL = new GridLayout(2, false);
        greatestStormEndHour3Comp.setLayout(greatestStormEndHour3GL);

        Label greatestStormEndHour3Lbl = new Label(greatestStormEndHour3Comp,
                SWT.NORMAL);
        greatestStormEndHour3Lbl.setText("Hour");

        endHourTFs[2] = new Text(greatestStormEndHour3Comp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(endHourTFs[2]);
        endHourTFs[2].addFocusListener(
                new TimeSelectorFocusListener(endHourTFs[2], true));
        endHourTFs[2].addListener(SWT.Modify,
                this.qcDialog.getChangeListener());
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

        maxSnow24HourTF = createText(maxSnow24HourComp,
                "24-Hour Maximum\nSnowfall (in)", "24-Hour Maximum Snowfall",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // 24-hour max snow dates
        Composite maxSnow24HourDatesComp = new Composite(parent, SWT.NONE);
        GridLayout maxSnow24HourDatesGL = new GridLayout(4, false);
        maxSnow24HourDatesComp.setLayout(maxSnow24HourDatesGL);

        createMax24HourDatesSection(maxSnow24HourDatesComp,
                maxSnow24HourBeginDates, maxSnow24HourEndDates);

        // greatest snow storm
        Composite greatestSnowStormComp = new Composite(parent, SWT.NONE);
        GridLayout greatestSnowStormGL = new GridLayout(2, false);
        greatestSnowStormComp.setLayout(greatestSnowStormGL);

        greatestSnowStormTF = createText(greatestSnowStormComp,
                "Greatest Storm\nTotal (in)",
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(greatestSnowStormTF);

        createGreatestStormSection(parent, greatestSnowStormBeginDates,
                greatestSnowStormEndDates, greatestSnowStormBeginHourTFs,
                greatestSnowStormEndHourTFs);
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
        totalSnowTF = createText(topComp, "Total Snowfall (in)",
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(totalSnowTF);

        // total water
        totalSnowWaterEquivTF = createText(topComp,
                "Total Water Equivalent (in)",
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(totalSnowWaterEquivTF);

        // days with snowfall
        Label daysWithSnowLbl = new Label(parent, SWT.NORMAL);
        daysWithSnowLbl.setText("Days with Snowfall");

        Composite bottomComp = new Composite(parent, SWT.NONE);
        GridLayout bottomGL = new GridLayout(2, false);
        bottomComp.setLayout(bottomGL);

        // any snowfall
        anySnowGreaterThanTraceTF = createText(bottomComp, "Any Snowfall",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(anySnowGreaterThanTraceTF);

        // snow 1.00 or more
        snowGreaterThan1TF = createText(bottomComp, "1.00 in. or more",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(snowGreaterThan1TF);

        boolean customMissing = this.qcDialog.climateGlobals
                .getS1() == ParameterFormatClimate.MISSING_SNOW;
        snowCustomGreaterTF = createText(bottomComp,
                (customMissing ? "??" : this.qcDialog.climateGlobals.getS1())
                        + " in. or more",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), !customMissing);
        ClimateLayoutValues.assignFieldsGD(snowCustomGreaterTF);

        // maximum snow depth
        Composite maxSnowDepthGroundComp = new Composite(parent, SWT.NONE);
        RowLayout maxSnowDepthGroundRL = new RowLayout(SWT.HORIZONTAL);
        maxSnowDepthGroundRL.center = true;
        maxSnowDepthGroundComp.setLayout(maxSnowDepthGroundRL);

        maxSnowDepthGroundTF = createText(maxSnowDepthGroundComp,
                "Maximum Snow Depth (in)", "Maximum Snow Depth",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // dates of max snowfall
        Composite maxSnowDatesComp = new Composite(parent, SWT.NONE);
        GridLayout maxSnowDatesGL = new GridLayout(4, false);
        maxSnowDatesComp.setLayout(maxSnowDatesGL);

        Label maxSnowDatesLbl = new Label(maxSnowDatesComp, SWT.NORMAL);
        maxSnowDatesLbl.setText("Date(s)");

        // first date of max snowfall
        maxSnowDepthGroundDates[0] = createDateComp(maxSnowDatesComp);

        // second date of max snowfall
        maxSnowDepthGroundDates[1] = createDateComp(maxSnowDatesComp);

        // third date of max snowfall
        maxSnowDepthGroundDates[2] = createDateComp(maxSnowDatesComp);

        // average snow depth
        Composite avgDepthComp = new Composite(parent, SWT.NONE);
        GridLayout avgDepthGL = new GridLayout(2, false);
        avgDepthComp.setLayout(avgDepthGL);

        avgSnowDepthGroundTF = createText(avgDepthComp,
                "Average Snow Depth (in)",
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(avgSnowDepthGroundTF);
    }

    /**
     * Create wind tab.
     * 
     * @param parent
     *            parent composite.
     */
    private void createWindTab(Composite parent) {
        // wind speed in knots checkbox, align left

        // data fields
        Composite windMainComp = new Composite(parent, SWT.BORDER);
        RowLayout windMainRL = new RowLayout(SWT.VERTICAL);
        windMainComp.setLayout(windMainRL);
        createWindTabMain(windMainComp);
    }

    /**
     * Create left half of wind tab (wind).
     * 
     * @param parent
     *            parent composite.
     */
    private void createWindTabMain(Composite parent) {
        // max wind
        Composite windFieldComp = new Composite(parent, SWT.NONE);
        GridLayout windFieldGL = new GridLayout(3, true);
        windFieldComp.setLayout(windFieldGL);

        Label maxWindLbl = new Label(windFieldComp, SWT.NORMAL);
        maxWindLbl.setText("Maximum Wind");

        Composite maxWindSpeedComp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindSpeedRL = new RowLayout(SWT.HORIZONTAL);
        maxWindSpeedRL.center = true;
        maxWindSpeedComp.setLayout(maxWindSpeedRL);

        maxWindSpeedTF = createText(maxWindSpeedComp, "Speed (MPH)",
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxWindSpeedTF);

        Label spacer0 = new Label(windFieldComp, SWT.NORMAL);
        spacer0.setText("");

        // max wind dir and dates
        Label spacer1 = new Label(windFieldComp, SWT.NORMAL);
        spacer1.setText("");

        // first dir and date
        Composite maxWindDir1Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindDir1RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDir1RL.center = true;
        maxWindDir1Comp.setLayout(maxWindDir1RL);

        maxWindDirTFs[0] = createText(maxWindDir1Comp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxWindDirTFs[0]);

        Composite maxWindDate1Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindDate1RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDate1RL.center = true;
        maxWindDate1Comp.setLayout(maxWindDate1RL);

        Label maxWindDate1Lbl = new Label(maxWindDate1Comp, SWT.NORMAL);
        maxWindDate1Lbl.setText("Date");

        maxWindDates[0] = createDateComp(maxWindDate1Comp);

        Label spacer2 = new Label(windFieldComp, SWT.NORMAL);
        spacer2.setText("");

        // second dir and date
        Composite maxWindDir2Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindDir2RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDir2RL.center = true;
        maxWindDir2Comp.setLayout(maxWindDir2RL);

        maxWindDirTFs[1] = createText(maxWindDir2Comp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxWindDirTFs[1]);

        Composite maxWindDate2Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindDate2RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDate2RL.center = true;
        maxWindDate2Comp.setLayout(maxWindDate2RL);

        Label maxWindDate2Lbl = new Label(maxWindDate2Comp, SWT.NORMAL);
        maxWindDate2Lbl.setText("Date");

        maxWindDates[1] = createDateComp(maxWindDate2Comp);

        Label spacer3 = new Label(windFieldComp, SWT.NORMAL);
        spacer3.setText("");

        // third dir and date
        Composite maxWindDir3Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindDir3RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDir3RL.center = true;
        maxWindDir3Comp.setLayout(maxWindDir3RL);

        maxWindDirTFs[2] = createText(maxWindDir3Comp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxWindDirTFs[2]);

        Composite maxWindDate3Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxWindDate3RL = new RowLayout(SWT.HORIZONTAL);
        maxWindDate3RL.center = true;
        maxWindDate3Comp.setLayout(maxWindDate3RL);

        Label maxWindDate3Lbl = new Label(maxWindDate3Comp, SWT.NORMAL);
        maxWindDate3Lbl.setText("Date");

        maxWindDates[2] = createDateComp(maxWindDate3Comp);

        GridData rowSpaceData = new GridData();
        rowSpaceData.horizontalAlignment = GridData.FILL;
        rowSpaceData.horizontalSpan = 3;
        Label rowSpacer0 = new Label(windFieldComp, SWT.NORMAL);
        rowSpacer0.setText("");
        rowSpacer0.setLayoutData(rowSpaceData);

        // max gust
        Label maxGustLbl = new Label(windFieldComp, SWT.NORMAL);
        maxGustLbl.setText("Maximum Gust");

        Composite maxGustSpeedComp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustSpeedRL = new RowLayout(SWT.HORIZONTAL);
        maxGustSpeedRL.center = true;
        maxGustSpeedComp.setLayout(maxGustSpeedRL);

        maxGustSpeedTF = createText(maxGustSpeedComp, "Speed (MPH)",
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxGustSpeedTF);

        Label spacer4 = new Label(windFieldComp, SWT.NORMAL);
        spacer4.setText("");

        // max gust dir and dates
        Label spacer5 = new Label(windFieldComp, SWT.NORMAL);
        spacer5.setText("");

        // first dir and date
        Composite maxGustDir1Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustDir1RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDir1RL.center = true;
        maxGustDir1Comp.setLayout(maxGustDir1RL);

        maxGustDirTFs[0] = createText(maxGustDir1Comp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxGustDirTFs[0]);

        Composite maxGustDate1Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustDate1RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDate1RL.center = true;
        maxGustDate1Comp.setLayout(maxGustDate1RL);

        Label maxGustDate1Lbl = new Label(maxGustDate1Comp, SWT.NORMAL);
        maxGustDate1Lbl.setText("Date");

        maxGustDates[0] = createDateComp(maxGustDate1Comp);

        Label spacer6 = new Label(windFieldComp, SWT.NORMAL);
        spacer6.setText("");

        // second dir and date
        Composite maxGustDir2Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustDir2RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDir2RL.center = true;
        maxGustDir2Comp.setLayout(maxGustDir2RL);

        maxGustDirTFs[1] = createText(maxGustDir2Comp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxGustDirTFs[1]);

        Composite maxGustDate2Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustDate2RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDate2RL.center = true;
        maxGustDate2Comp.setLayout(maxGustDate2RL);

        Label maxGustDate2Lbl = new Label(maxGustDate2Comp, SWT.NORMAL);
        maxGustDate2Lbl.setText("Date");

        maxGustDates[1] = createDateComp(maxGustDate2Comp);

        Label spacer7 = new Label(windFieldComp, SWT.NORMAL);
        spacer7.setText("");

        // third dir and date
        Composite maxGustDir3Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustDir3RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDir3RL.center = true;
        maxGustDir3Comp.setLayout(maxGustDir3RL);

        maxGustDirTFs[2] = createText(maxGustDir3Comp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(maxGustDirTFs[2]);

        Composite maxGustDate3Comp = new Composite(windFieldComp, SWT.NONE);
        RowLayout maxGustDate3RL = new RowLayout(SWT.HORIZONTAL);
        maxGustDate3RL.center = true;
        maxGustDate3Comp.setLayout(maxGustDate3RL);

        Label maxGustDate3Lbl = new Label(maxGustDate3Comp, SWT.NORMAL);
        maxGustDate3Lbl.setText("Date");

        maxGustDates[2] = createDateComp(maxGustDate3Comp);

        Label rowSpacer1 = new Label(windFieldComp, SWT.NORMAL);
        rowSpacer1.setText("");
        rowSpacer1.setLayoutData(rowSpaceData);

        // resultant wind
        Label resultantWindLbl = new Label(windFieldComp, SWT.NORMAL);
        resultantWindLbl.setText("Resultant Wind");

        // resultant wind speed
        Composite resultantWindSpeedComp = new Composite(windFieldComp,
                SWT.NONE);
        RowLayout resultantWindSpeedRL = new RowLayout(SWT.HORIZONTAL);
        resultantWindSpeedRL.center = true;
        resultantWindSpeedComp.setLayout(resultantWindSpeedRL);

        resultantWindSpeedTF = createText(resultantWindSpeedComp, "Speed (MPH)",
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(resultantWindSpeedTF);

        Label spacer8 = new Label(windFieldComp, SWT.NORMAL);
        spacer8.setText("");

        // resultant wind dir
        Label spacer9 = new Label(windFieldComp, SWT.NORMAL);
        spacer9.setText("");

        Composite resultantWindDirComp = new Composite(windFieldComp, SWT.NONE);
        RowLayout resultantWindDirRL = new RowLayout(SWT.HORIZONTAL);
        resultantWindDirRL.center = true;
        resultantWindDirComp.setLayout(resultantWindDirRL);

        resultantWindDirTF = createText(resultantWindDirComp, "Direction (deg)",
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(resultantWindDirTF);

        Label spacer10 = new Label(windFieldComp, SWT.NORMAL);
        spacer10.setText("");

        Label rowSpacer2 = new Label(windFieldComp, SWT.NORMAL);
        rowSpacer2.setText("");
        rowSpacer2.setLayoutData(rowSpaceData);

        // average wind
        Label averageWindLbl = new Label(windFieldComp, SWT.NORMAL);
        averageWindLbl.setText("Average Wind");

        Composite averageWindSpeedComp = new Composite(windFieldComp, SWT.NONE);
        RowLayout averageWindSpeedRL = new RowLayout(SWT.HORIZONTAL);
        averageWindSpeedRL.center = true;
        averageWindSpeedComp.setLayout(averageWindSpeedRL);

        averageWindSpeedTF = createText(averageWindSpeedComp, "Speed (MPH)",
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(averageWindSpeedTF);
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
        GridLayout weatherFieldsGL = new GridLayout(4, false);
        weatherFieldsGL.horizontalSpacing = 10;
        weatherFieldsGL.verticalSpacing = 10;
        weatherFieldsComp.setLayout(weatherFieldsGL);

        // thunder
        thunderTF = createText(weatherFieldsComp, "Thunder (TS)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(thunderTF);

        // heavy snow
        heavySnowTF = createText(weatherFieldsComp, "Heavy Snow (S+)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(heavySnowTF);

        // mixed precip
        mixedPrecipTF = createText(weatherFieldsComp, "Mixed Precip (RASN)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(mixedPrecipTF);

        // snow
        snowTF = createText(weatherFieldsComp, "Snow (S)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(snowTF);

        // heavy rain
        heavyRainTF = createText(weatherFieldsComp, "Heavy Rain (R+)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(heavyRainTF);

        // light snow
        lightSnowTF = createText(weatherFieldsComp, "Light Snow (S-)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(lightSnowTF);

        // rain
        rainTF = createText(weatherFieldsComp, "Rain (R)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(rainTF);

        // ice pellets
        icePelletsTF = createText(weatherFieldsComp, "Ice Pellets (PL)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(icePelletsTF);

        // light rain
        lightRainTF = createText(weatherFieldsComp, "Light Rain (R-)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(lightRainTF);

        // fog
        fogTF = createText(weatherFieldsComp, "Fog (F)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(fogTF);

        // freezing rain
        freezingRainTF = createText(weatherFieldsComp, "Freezing Rain (ZR)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(freezingRainTF);

        // heavy fog
        heavyFogTF = createText(weatherFieldsComp, "Heavy Fog (F <= 1/4)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(heavyFogTF);

        // light freezing rain
        lightFreezingRainTF = createText(weatherFieldsComp,
                "Lgt. Fr. Rain (ZR-)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(lightFreezingRainTF);

        // haze
        hazeTF = createText(weatherFieldsComp, "Haze (H)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(hazeTF);

        // hail
        hailTF = createText(weatherFieldsComp, "Hail (GR)",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(hailTF);
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
        GridLayout skyMiddleGL = new GridLayout(2, false);
        skyMiddle.setLayout(skyMiddleGL);

        fairTF = createText(skyMiddle, "Fair (0 - 0.3)", "Number of Fair Days",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        partlyCloudyTF = createText(skyMiddle, "Partly Cloudy (0.4 - 0.7)",
                "Number of Partly Cloudy Days", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        mostlyCloudyTF = createText(skyMiddle, "Cloudy (0.8 - 1.0)",
                "Number of Cloudy Days", QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);
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
        skyTopComp.setLayout(skyTopGL);

        // sunshine
        Composite sunshineComp = new Composite(skyTopComp, SWT.NONE);
        GridLayout sunshineGL = new GridLayout(2, false);
        sunshineComp.setLayout(sunshineGL);

        percentPossSunshineTF = createText(sunshineComp,
                "Percent Possible Sunshine", "Percent Possible Sunshine",
                QCValueType.PERIOD,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener(), isMonthly);

        // sky cover
        Composite skyCoverComp = new Composite(skyTopComp, SWT.NONE);
        GridLayout skyCoverGL = new GridLayout(2, false);
        skyCoverComp.setLayout(skyCoverGL);

        skyCoverTF = createTextWithComposite(skyCoverComp, "Average Sky Cover",
                this.qcDialog.displayListeners.getSkyCoverListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(skyCoverTF);

        // mean relative humidity
        Composite meanRelHumdComp = new Composite(skyTopComp, SWT.NONE);
        GridLayout relHumdGL = new GridLayout(2, false);
        meanRelHumdComp.setLayout(relHumdGL);

        meanRelHumdTF = createTextWithComposite(meanRelHumdComp,
                "Mean Relative Humidity",
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsRD(meanRelHumdTF);
    }

    /**
     * Set date text for List<ClimateDate>
     * 
     * @param dateComps
     * @param dateList
     */
    private void setDateText(AbstractDateComp[] dateComps,
            java.util.List<ClimateDate> dateList) {
        for (int i = 0; i < dateComps.length && i < dateList.size(); i++) {
            dateComps[i].setDate(dateList.get(i));
        }
    }

    /**
     * Set date text for List<ClimateDates>
     * 
     * @param beginDateComps
     * @param endDateComps
     * @param datesList
     */
    private void setBeginEndDateText(AbstractDateComp[] beginDateComps,
            AbstractDateComp[] endDateComps,
            java.util.List<ClimateDates> datesList) {
        for (int i = 0; i < beginDateComps.length
                && i < datesList.size(); i++) {
            beginDateComps[i].setDate(datesList.get(i).getStart());
        }
        for (int i = 0; i < endDateComps.length && i < datesList.size(); i++) {
            endDateComps[i].setDate(datesList.get(i).getEnd());
        }
    }

    /**
     * Load period data.
     * 
     * @throws ClimateException
     * 
     */
    @Override
    public void loadData() throws ClimateException {
        setPeriodDesc();
        this.qcDialog.getChangeListener().setIgnoreChanges(true);
        clearValues();

        int selectedStationIndex = this.qcDialog.stationNames
                .getSelectionIndex();

        if (selectedStationIndex >= 0
                && selectedStationIndex < this.qcDialog.stations.size()) {
            Station newStation = this.qcDialog.stations
                    .get(selectedStationIndex);

            PeriodServiceRequest request = new PeriodServiceRequest(
                    newStation.getInformId(),
                    this.qcDialog.periodDesc.getPeriodType(),
                    this.qcDialog.periodDesc.getDates());

            try {
                QueryData queryData = (QueryData) ThriftClient
                        .sendRequest(request);

                PeriodData data = (PeriodData) queryData.getData();

                this.qcDialog.periodData = data;

                if (!queryData.getExists()) {
                    MessageDialog.openWarning(getShell(), "Missing Data",
                            "There is no data available for the station ID ["
                                    + newStation.getInformId() + "] and dates ["
                                    + this.qcDialog.periodDesc.getDates()
                                            .getStart().toFullDateString()
                                    + " - "
                                    + this.qcDialog.periodDesc.getDates()
                                            .getEnd().toFullDateString()
                                    + "].");

                    this.qcDialog.periodData = PeriodData
                            .getMissingPeriodData();
                    this.qcDialog.periodData
                            .setInformId(newStation.getInformId());
                }

                else {

                    maxTempTF.setTextAndTip(String.valueOf(data.getMaxTemp()),
                            data.getDataMethods().getMaxTempQc());
                    setDateText(maxTempDates, data.getDayMaxTempList());

                    avgMaxTempTF.setTextAndTip(
                            String.valueOf(data.getMaxTempMean()),
                            data.getDataMethods().getAvgMaxTempQc());

                    meanTempTF.setTextAndTip(String.valueOf(data.getMeanTemp()),
                            data.getDataMethods().getMeanTempQc());

                    maxTempGreater90DegTF.setTextAndTip(
                            String.valueOf(data.getNumMaxGreaterThan90F()),
                            data.getDataMethods().getMaxTempGE90Qc());
                    maxTempLess32DegTF.setTextAndTip(
                            String.valueOf(data.getNumMaxLessThan32F()),
                            data.getDataMethods().getMaxTempLE32Qc());
                    if (maxTempGreaterDegCustom1TF.isEnabled()) {
                        maxTempGreaterDegCustom1TF.setText(
                                String.valueOf(data.getNumMaxGreaterThanT1F()));
                    }
                    if (maxTempGreaterDegCustom2TF.isEnabled()) {
                        maxTempGreaterDegCustom2TF.setText(
                                String.valueOf(data.getNumMaxGreaterThanT2F()));
                    }
                    if (maxTempLessDegCustomTF.isEnabled()) {
                        maxTempLessDegCustomTF.setText(
                                String.valueOf(data.getNumMaxLessThanT3F()));
                    }

                    minTempTF.setTextAndTip(String.valueOf(data.getMinTemp()),
                            data.getDataMethods().getMinTempQc());
                    setDateText(minTempDates, data.getDayMinTempList());

                    avgMinTempTF.setTextAndTip(
                            String.valueOf(data.getMinTempMean()),
                            data.getDataMethods().getAvgMinTempQc());
                    minTempLess32DegTF.setTextAndTip(
                            String.valueOf(data.getNumMinLessThan32F()),
                            data.getDataMethods().getMinLE32Qc());
                    minTempLess0DegTF.setTextAndTip(
                            String.valueOf(data.getNumMinLessThan0F()),
                            data.getDataMethods().getMinLE0Qc());
                    if (minTempGreaterDegCustomTF.isEnabled()) {
                        minTempGreaterDegCustomTF.setText(
                                String.valueOf(data.getNumMinGreaterThanT4F()));
                    }
                    if (minTempLessDegCustom1TF.isEnabled()) {
                        minTempLessDegCustom1TF.setText(
                                String.valueOf(data.getNumMinLessThanT5F()));
                    }
                    if (minTempLessDegCustom2TF.isEnabled()) {
                        minTempLessDegCustom2TF.setText(
                                String.valueOf(data.getNumMinLessThanT6F()));
                    }

                    heatingDaysTF.setTextAndTip(
                            String.valueOf(data.getNumHeatTotal()),
                            data.getDataMethods().getHeatQc());
                    coolingDaysTF.setTextAndTip(
                            String.valueOf(data.getNumCoolTotal()),
                            data.getDataMethods().getCoolQc());

                    totalPrecipTF
                            .setText(String.valueOf(data.getPrecipTotal()));
                    if (data.getPrecipTotal() == ParameterFormatClimate.TRACE) {
                        totalPrecipTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }

                    avgDailyPrecipTF
                            .setText(String.valueOf(data.getPrecipMeanDay()));
                    if (data.getPrecipMeanDay() == ParameterFormatClimate.TRACE) {
                        avgDailyPrecipTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }

                    precipInchesGreater01TF.setTextAndTip(
                            String.valueOf(data.getNumPrcpGreaterThan01()),
                            data.getDataMethods().getPrecipGE01Qc());
                    precipInchesGreater10TF.setTextAndTip(
                            String.valueOf(data.getNumPrcpGreaterThan10()),
                            data.getDataMethods().getPrecipGE10Qc());
                    precipInchesGreater50TF.setTextAndTip(
                            String.valueOf(data.getNumPrcpGreaterThan50()),
                            data.getDataMethods().getPrecipGE50Qc());
                    precipInchesGreater100TF.setTextAndTip(
                            String.valueOf(data.getNumPrcpGreaterThan100()),
                            data.getDataMethods().getPrecipGE100Qc());
                    if (precipInchesGreaterCustom1TF.isEnabled()) {
                        precipInchesGreaterCustom1TF.setText(
                                String.valueOf(data.getNumPrcpGreaterThanP1()));
                    }
                    if (precipInchesGreaterCustom2TF.isEnabled()) {
                        precipInchesGreaterCustom2TF.setText(
                                String.valueOf(data.getNumPrcpGreaterThanP2()));
                    }

                    maxPrecip24HourTF.setTextAndTip(
                            String.valueOf(data.getPrecipMax24H()),
                            data.getDataMethods().getPrecip24hrMaxQc());
                    if (data.getPrecipMax24H() == ParameterFormatClimate.TRACE) {
                        maxPrecip24HourTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    setBeginEndDateText(maxPrecip24HourBeginDates,
                            maxPrecip24HourEndDates, data.getPrecip24HDates());

                    greatestPrecipStormTF
                            .setText(String.valueOf(data.getPrecipStormMax()));
                    if (data.getPrecipStormMax() == ParameterFormatClimate.TRACE) {
                        greatestPrecipStormTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    setBeginEndDateText(greatestPrecipStormBeginDates,
                            greatestPrecipStormEndDates,
                            data.getPrecipStormList());
                    for (int i = 0; i < greatestPrecipStormBeginHourTFs.length
                            && i < data.getPrecipStormList().size(); i++) {
                        greatestPrecipStormBeginHourTFs[i].setText(
                                String.valueOf(data.getPrecipStormList().get(i)
                                        .getStartTime().getHour()));
                    }
                    for (int i = 0; i < greatestPrecipStormEndHourTFs.length
                            && i < data.getPrecipStormList().size(); i++) {
                        greatestPrecipStormEndHourTFs[i].setText(
                                String.valueOf(data.getPrecipStormList().get(i)
                                        .getEndTime().getHour()));
                    }

                    totalSnowTF.setText(String.valueOf(data.getSnowTotal()));
                    if (data.getSnowTotal() == ParameterFormatClimate.TRACE) {
                        totalSnowTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    totalSnowWaterEquivTF
                            .setText(String.valueOf(data.getSnowWater()));
                    if (data.getSnowWater() == ParameterFormatClimate.TRACE) {
                        totalSnowWaterEquivTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    anySnowGreaterThanTraceTF.setText(
                            String.valueOf(data.getNumSnowGreaterThanTR()));
                    snowGreaterThan1TF.setText(
                            String.valueOf(data.getNumSnowGreaterThan1()));
                    if (snowCustomGreaterTF.isEnabled()) {
                        snowCustomGreaterTF.setText(
                                String.valueOf(data.getNumSnowGreaterThanS1()));
                    }

                    avgSnowDepthGroundTF
                            .setText(String.valueOf(data.getSnowGroundMean()));
                    if (data.getSnowGroundMean() == ParameterFormatClimate.TRACE) {
                        avgSnowDepthGroundTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }

                    maxSnowDepthGroundTF.setTextAndTip(
                            String.valueOf(data.getSnowGroundMax()),
                            data.getDataMethods().getMaxDepthQc());
                    setDateText(maxSnowDepthGroundDates,
                            data.getSnowGroundMaxDateList());

                    maxSnow24HourTF.setTextAndTip(
                            String.valueOf(data.getSnowMax24H()),
                            data.getDataMethods().getSnow24hrMaxQc());
                    if (data.getSnowMax24H() == ParameterFormatClimate.TRACE) {
                        maxSnow24HourTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    setBeginEndDateText(maxSnow24HourBeginDates,
                            maxSnow24HourEndDates, data.getSnow24HDates());

                    greatestSnowStormTF
                            .setText(String.valueOf(data.getSnowMaxStorm()));
                    if (data.getSnowMaxStorm() == ParameterFormatClimate.TRACE) {
                        greatestSnowStormTF
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    setBeginEndDateText(greatestSnowStormBeginDates,
                            greatestSnowStormEndDates, data.getSnowStormList());
                    for (int i = 0; i < greatestSnowStormBeginHourTFs.length
                            && i < data.getSnowStormList().size(); i++) {
                        greatestSnowStormBeginHourTFs[i]
                                .setText(String.valueOf(data.getSnowStormList()
                                        .get(i).getStartTime().getHour()));
                    }
                    for (int i = 0; i < greatestSnowStormEndHourTFs.length
                            && i < data.getSnowStormList().size(); i++) {
                        greatestSnowStormEndHourTFs[i]
                                .setText(String.valueOf(data.getSnowStormList()
                                        .get(i).getEndTime().getHour()));
                    }

                    if (!data.getMaxWindList().isEmpty()) {
                        maxWindSpeedTF.setText(String.valueOf(
                                data.getMaxWindList().get(0).getSpeed()));
                    }
                    for (int i = 0; i < maxWindDirTFs.length
                            && i < data.getMaxWindList().size(); i++) {
                        maxWindDirTFs[i].setText(String.valueOf(
                                data.getMaxWindList().get(i).getDir()));
                    }
                    setDateText(maxWindDates, data.getMaxWindDayList());

                    resultantWindSpeedTF.setText(
                            String.valueOf(data.getResultWind().getSpeed()));
                    resultantWindDirTF.setText(
                            String.valueOf(data.getResultWind().getDir()));

                    if (!data.getMaxGustList().isEmpty()) {
                        maxGustSpeedTF.setText(String.valueOf(
                                data.getMaxGustList().get(0).getSpeed()));
                    }
                    for (int i = 0; i < maxGustDirTFs.length
                            && i < data.getMaxGustList().size(); i++) {
                        maxGustDirTFs[i].setText(String.valueOf(
                                data.getMaxGustList().get(i).getDir()));
                    }
                    setDateText(maxGustDates, data.getMaxGustDayList());

                    averageWindSpeedTF
                            .setText(String.valueOf(data.getAvgWindSpd()));

                    percentPossSunshineTF.setTextAndTip(
                            String.valueOf(data.getPossSun()),
                            data.getDataMethods().getPossSunQc());

                    skyCoverTF.setText(String.valueOf(data.getMeanSkyCover()));

                    meanRelHumdTF.setText(String.valueOf(data.getMeanRh()));

                    fairTF.setTextAndTip(String.valueOf(data.getNumFairDays()),
                            data.getDataMethods().getFairDaysQc());
                    partlyCloudyTF.setTextAndTip(
                            String.valueOf(data.getNumPartlyCloudyDays()),
                            data.getDataMethods().getPcDaysQc());
                    mostlyCloudyTF.setTextAndTip(
                            String.valueOf(data.getNumMostlyCloudyDays()),
                            data.getDataMethods().getCloudyDaysQc());

                    thunderTF.setText(
                            String.valueOf(data.getNumThunderStorms()));
                    mixedPrecipTF
                            .setText(String.valueOf(data.getNumMixedPrecip()));
                    heavyRainTF.setText(String.valueOf(data.getNumHeavyRain()));
                    rainTF.setText(String.valueOf(data.getNumRain()));
                    lightRainTF.setText(String.valueOf(data.getNumLightRain()));
                    freezingRainTF
                            .setText(String.valueOf(data.getNumFreezingRain()));
                    lightFreezingRainTF.setText(
                            String.valueOf(data.getNumLightFreezingRain()));
                    hailTF.setText(String.valueOf(data.getNumHail()));
                    heavySnowTF.setText(String.valueOf(data.getNumHeavySnow()));
                    snowTF.setText(String.valueOf(data.getNumSnow()));
                    lightSnowTF.setText(String.valueOf(data.getNumLightSnow()));
                    icePelletsTF
                            .setText(String.valueOf(data.getNumIcePellets()));
                    fogTF.setText(String.valueOf(data.getNumFog()));
                    heavyFogTF
                            .setText(String.valueOf(data.getNumFogQuarterSM()));
                    hazeTF.setText(String.valueOf(data.getNumHaze()));

                }

            } catch (VizException e) {
                String message = "Could not retrieve data for Climate Daily Display dialog, with station ID ["
                        + newStation.getInformId() + "] and dates ["
                        + this.qcDialog.periodDesc.getDates().getStart()
                                .toFullDateString()
                        + " - "
                        + this.qcDialog.periodDesc.getDates().getEnd()
                                .toFullDateString()
                        + "]. Select a different station.";
                logger.error(message, e);
                MessageDialog.openError(getShell(), "Data Retrieval Error",
                        message);
                this.qcDialog.periodData = null;
            }

            this.qcDialog.getChangeListener().setIgnoreChanges(false);
            this.qcDialog.getChangeListener().setChangesUnsaved(false);
        }

    }

    /**
     * Save period data.
     */
    @Override
    public void saveData() {

        PeriodData data = this.qcDialog.periodData;
        PeriodDataMethod dataMethods = this.qcDialog.periodData
                .getDataMethods();

        saveValue(this.qcDialog.periodData.getMaxTemp(), maxTempTF.getText(),
                data::setMaxTemp, dataMethods::setMaxTempQc);

        saveValue(this.qcDialog.periodData.getMinTemp(), minTempTF.getText(),
                data::setMinTemp, dataMethods::setMinTempQc);

        saveValue(this.qcDialog.periodData.getMaxTempMean(),
                avgMaxTempTF.getText(), data::setMaxTempMean,
                dataMethods::setAvgMaxTempQc);

        saveValue(this.qcDialog.periodData.getMinTempMean(),
                avgMinTempTF.getText(), data::setMinTempMean,
                dataMethods::setAvgMinTempQc);

        saveValue(this.qcDialog.periodData.getMeanTemp(), meanTempTF.getText(),
                data::setMeanTemp, dataMethods::setMeanTempQc);

        saveValue(this.qcDialog.periodData.getNumMaxGreaterThan90F(),
                maxTempGreater90DegTF.getText(), data::setNumMaxGreaterThan90F,
                dataMethods::setMaxTempGE90Qc);

        saveValue(this.qcDialog.periodData.getNumMaxLessThan32F(),
                maxTempLess32DegTF.getText(), data::setNumMaxLessThan32F,
                dataMethods::setMaxTempLE32Qc);

        if (this.qcDialog.climateGlobals
                .getT1() != ParameterFormatClimate.MISSING) {
            saveValue(this.qcDialog.periodData.getNumMaxGreaterThanT1F(),
                    maxTempGreaterDegCustom1TF.getText(),
                    data::setNumMaxGreaterThanT1F);
        }
        if (this.qcDialog.climateGlobals
                .getT2() != ParameterFormatClimate.MISSING) {
            saveValue(this.qcDialog.periodData.getNumMaxGreaterThanT2F(),
                    maxTempGreaterDegCustom2TF.getText(),
                    data::setNumMaxGreaterThanT2F);
        }
        if (this.qcDialog.climateGlobals
                .getT3() != ParameterFormatClimate.MISSING) {
            saveValue(this.qcDialog.periodData.getNumMaxLessThanT3F(),
                    maxTempLessDegCustomTF.getText(),
                    data::setNumMaxLessThanT3F);
        }

        saveValue(this.qcDialog.periodData.getNumMinLessThan32F(),
                minTempLess32DegTF.getText(), data::setNumMinLessThan32F,
                dataMethods::setMinLE32Qc);

        saveValue(this.qcDialog.periodData.getNumMinLessThan0F(),
                minTempLess0DegTF.getText(), data::setNumMinLessThan0F,
                dataMethods::setMinLE0Qc);

        if (this.qcDialog.climateGlobals
                .getT4() != ParameterFormatClimate.MISSING) {
            saveValue(this.qcDialog.periodData.getNumMinGreaterThanT4F(),
                    minTempGreaterDegCustomTF.getText(),
                    data::setNumMinGreaterThanT4F);
        }
        if (this.qcDialog.climateGlobals
                .getT5() != ParameterFormatClimate.MISSING) {
            saveValue(this.qcDialog.periodData.getNumMinLessThanT5F(),
                    minTempLessDegCustom1TF.getText(),
                    data::setNumMinLessThanT5F);
        }
        if (this.qcDialog.climateGlobals
                .getT6() != ParameterFormatClimate.MISSING) {
            saveValue(this.qcDialog.periodData.getNumMinLessThanT6F(),
                    minTempLessDegCustom2TF.getText(),
                    data::setNumMinLessThanT6F);
        }

        saveValue(this.qcDialog.periodData.getNumHeatTotal(),
                heatingDaysTF.getText(), data::setNumHeatTotal,
                dataMethods::setHeatQc);

        saveValue(this.qcDialog.periodData.getNumCoolTotal(),
                coolingDaysTF.getText(), data::setNumCoolTotal,
                dataMethods::setCoolQc);

        data.setDayMinTempList(getClimateDateDayList(minTempDates));
        data.setDayMaxTempList(getClimateDateDayList(maxTempDates));

        saveValue(this.qcDialog.periodData.getPrecipTotal(),
                totalPrecipTF.getText(), data::setPrecipTotal);

        saveValue(this.qcDialog.periodData.getPrecipMeanDay(),
                avgDailyPrecipTF.getText(), data::setPrecipMeanDay);

        saveValue(this.qcDialog.periodData.getNumPrcpGreaterThan01(),
                precipInchesGreater01TF.getText(),
                data::setNumPrcpGreaterThan01, dataMethods::setPrecipGE01Qc);

        saveValue(this.qcDialog.periodData.getNumPrcpGreaterThan10(),
                precipInchesGreater10TF.getText(),
                data::setNumPrcpGreaterThan10, dataMethods::setPrecipGE10Qc);

        saveValue(this.qcDialog.periodData.getNumPrcpGreaterThan50(),
                precipInchesGreater50TF.getText(),
                data::setNumPrcpGreaterThan50, dataMethods::setPrecipGE50Qc);

        saveValue(this.qcDialog.periodData.getNumPrcpGreaterThan100(),
                precipInchesGreater100TF.getText(),
                data::setNumPrcpGreaterThan100, dataMethods::setPrecipGE100Qc);

        if (this.qcDialog.climateGlobals
                .getP1() != ParameterFormatClimate.MISSING_PRECIP) {
            saveValue(this.qcDialog.periodData.getNumPrcpGreaterThanP1(),
                    precipInchesGreaterCustom1TF.getText(),
                    data::setNumPrcpGreaterThanP1);
        }
        if (this.qcDialog.climateGlobals
                .getP2() != ParameterFormatClimate.MISSING_PRECIP) {
            saveValue(this.qcDialog.periodData.getNumPrcpGreaterThanP2(),
                    precipInchesGreaterCustom2TF.getText(),
                    data::setNumPrcpGreaterThanP2);
        }

        saveValue(this.qcDialog.periodData.getPrecipMax24H(),
                maxPrecip24HourTF.getText(), data::setPrecipMax24H,
                dataMethods::setPrecip24hrMaxQc);

        data.setPrecip24HDates(getClimateDatesBeginEndList(
                maxPrecip24HourBeginDates, greatestPrecipStormEndDates));

        saveValue(this.qcDialog.periodData.getPrecipStormMax(),
                greatestPrecipStormTF.getText(), data::setPrecipStormMax);

        data.setPrecipStormList(getClimateDatesBeginEndList(
                greatestPrecipStormBeginDates, greatestPrecipStormEndDates,
                greatestPrecipStormBeginHourTFs,
                greatestPrecipStormEndHourTFs));

        saveValue(this.qcDialog.periodData.getSnowTotal(),
                totalSnowTF.getText(), data::setSnowTotal);

        saveValue(this.qcDialog.periodData.getSnowWater(),
                totalSnowWaterEquivTF.getText(), data::setSnowWater);

        saveValue(this.qcDialog.periodData.getNumSnowGreaterThanTR(),
                anySnowGreaterThanTraceTF.getText(),
                data::setNumSnowGreaterThanTR);

        saveValue(this.qcDialog.periodData.getNumSnowGreaterThan1(),
                snowGreaterThan1TF.getText(), data::setNumSnowGreaterThan1);

        if (this.qcDialog.climateGlobals
                .getS1() != ParameterFormatClimate.MISSING_SNOW) {
            saveValue(this.qcDialog.periodData.getNumSnowGreaterThanS1(),
                    snowCustomGreaterTF.getText(),
                    data::setNumSnowGreaterThanS1);
        }

        saveValue(this.qcDialog.periodData.getSnowGroundMax(),
                maxSnowDepthGroundTF.getText(), data::setSnowGroundMax,
                dataMethods::setMaxDepthQc);

        data.setSnowGroundMaxDateList(
                getClimateDateDayList(maxSnowDepthGroundDates));

        saveValue(this.qcDialog.periodData.getSnowGroundMean(),
                avgSnowDepthGroundTF.getText(), data::setSnowGroundMean);

        saveValue(this.qcDialog.periodData.getSnowMax24H(),
                maxSnow24HourTF.getText(), data::setSnowMax24H,
                dataMethods::setSnow24hrMaxQc);

        data.setSnow24HDates(getClimateDatesBeginEndList(
                maxSnow24HourBeginDates, maxSnow24HourEndDates));

        saveValue(this.qcDialog.periodData.getSnowMaxStorm(),
                greatestSnowStormTF.getText(), data::setSnowMaxStorm);

        data.setSnowStormList(getClimateDatesBeginEndList(
                greatestSnowStormBeginDates, greatestSnowStormEndDates,
                greatestSnowStormBeginHourTFs, greatestSnowStormEndHourTFs));

        float maxWindSpeed = Float.parseFloat(maxWindSpeedTF.getText());
        java.util.List<ClimateWind> maxWindList = new ArrayList<ClimateWind>();
        for (Text text : maxWindDirTFs) {
            int maxWindSpeedDir = Integer.parseInt(text.getText());
            if (maxWindSpeedDir != ParameterFormatClimate.MISSING) {
                maxWindList.add(new ClimateWind(maxWindSpeedDir, maxWindSpeed));
            }
        }
        data.setMaxWindList(maxWindList);
        data.setMaxWindDayList(getClimateDateDayList(maxWindDates));

        float maxGustSpeed = Float.parseFloat(maxWindSpeedTF.getText());
        java.util.List<ClimateWind> maxGustList = new ArrayList<ClimateWind>();
        for (Text text : maxGustDirTFs) {
            int maxGustSpeedDir = Integer.parseInt(text.getText());
            if (maxGustSpeedDir != ParameterFormatClimate.MISSING) {
                maxGustList.add(new ClimateWind(maxGustSpeedDir, maxGustSpeed));
            }
        }
        data.setMaxGustList(maxGustList);
        data.setMaxGustDayList(getClimateDateDayList(maxGustDates));

        data.setResultWind(
                new ClimateWind(Integer.parseInt(resultantWindDirTF.getText()),
                        Float.parseFloat(resultantWindSpeedTF.getText())));

        saveValue(this.qcDialog.periodData.getAvgWindSpd(),
                averageWindSpeedTF.getText(), data::setAvgWindSpd);

        saveValue(this.qcDialog.periodData.getPossSun(),
                percentPossSunshineTF.getText(), data::setPossSun,
                dataMethods::setPossSunQc);

        saveValue(this.qcDialog.periodData.getMeanSkyCover(),
                skyCoverTF.getText(), data::setMeanSkyCover);

        saveValue(this.qcDialog.periodData.getMeanRh(), meanRelHumdTF.getText(),
                data::setMeanRh);

        saveValue(this.qcDialog.periodData.getNumFairDays(), fairTF.getText(),
                data::setNumFairDays, dataMethods::setFairDaysQc);

        saveValue(this.qcDialog.periodData.getNumPartlyCloudyDays(),
                partlyCloudyTF.getText(), data::setNumPartlyCloudyDays,
                dataMethods::setPcDaysQc);

        saveValue(this.qcDialog.periodData.getNumMostlyCloudyDays(),
                mostlyCloudyTF.getText(), data::setNumMostlyCloudyDays,
                dataMethods::setCloudyDaysQc);

        saveValue(this.qcDialog.periodData.getNumThunderStorms(),
                thunderTF.getText(), data::setNumThunderStorms);

        saveValue(this.qcDialog.periodData.getNumFreezingRain(),
                freezingRainTF.getText(), data::setNumFreezingRain);

        saveValue(this.qcDialog.periodData.getNumLightSnow(),
                lightSnowTF.getText(), data::setNumLightSnow);

        saveValue(this.qcDialog.periodData.getNumMixedPrecip(),
                mixedPrecipTF.getText(), data::setNumMixedPrecip);

        saveValue(this.qcDialog.periodData.getNumLightFreezingRain(),
                lightFreezingRainTF.getText(), data::setNumLightFreezingRain);

        saveValue(this.qcDialog.periodData.getNumIcePellets(),
                icePelletsTF.getText(), data::setNumIcePellets);

        saveValue(this.qcDialog.periodData.getNumHeavyRain(),
                heavyRainTF.getText(), data::setNumHeavyRain);

        saveValue(this.qcDialog.periodData.getNumHail(), hailTF.getText(),
                data::setNumHail);

        saveValue(this.qcDialog.periodData.getNumFog(), fogTF.getText(),
                data::setNumFog);

        saveValue(this.qcDialog.periodData.getNumRain(), rainTF.getText(),
                data::setNumRain);

        saveValue(this.qcDialog.periodData.getNumHeavySnow(),
                heavySnowTF.getText(), data::setNumHeavySnow);

        saveValue(this.qcDialog.periodData.getNumFogQuarterSM(),
                heavyFogTF.getText(), data::setNumFogQuarterSM);

        saveValue(this.qcDialog.periodData.getNumLightRain(),
                lightRainTF.getText(), data::setNumLightRain);

        saveValue(this.qcDialog.periodData.getNumSnow(), snowTF.getText(),
                data::setNumSnow);

        saveValue(this.qcDialog.periodData.getNumHaze(), hazeTF.getText(),
                data::setNumHaze);

        data.setDataMethods(dataMethods);

        PeriodClimateServiceUpdateRequest request = new PeriodClimateServiceUpdateRequest(
                this.qcDialog.periodData.getInformId(),
                this.qcDialog.periodDesc.getDates(),
                this.qcDialog.periodDesc.getPeriodType(), data);
        try {
            ThriftClient.sendRequest(request);

            this.qcDialog.getChangeListener().setChangesUnsaved(false);
            loadData();

            refreshDatabase();
        } catch (VizException | ClimateException e) {
            logger.error("Could not save values for station ID "
                    + this.qcDialog.periodData.getInformId(), e);
        }

        if (MessageDialog.openQuestion(getShell(), "Update Records",
                "New value(s) might set or tie record(s) in historical database. "
                        + "\nIs it OK to update record(s)?")) {
            data.setInformId(this.qcDialog.periodData.getInformId());
            CompareUpdatePeriodRecordsRequest recordsRequest = new CompareUpdatePeriodRecordsRequest(
                    this.qcDialog.periodDesc.getPeriodType(),
                    this.qcDialog.periodDesc.getDates().getEnd(), data);
            try {
                ThriftClient.sendRequest(recordsRequest);
            } catch (VizException e) {
                logger.error("Could not update period records for station ID "
                        + this.qcDialog.periodData.getInformId(), e);
            }
        }
    }

    /**
     * Update season/annual entries according to a changed monthly entry.
     */
    private void refreshDatabase() {
        if (isMonthly) {

            ClimateDates dates;
            String periodName;
            PeriodType periodType;

            try {
                dates = new ClimateDates(
                        SeasonType.getSeasonTypeFromMonth(
                                this.qcDialog.monthlyDate.getMon()),
                        this.qcDialog.monthlyDate.getYear());
            } catch (ClimateInvalidParameterException e) {
                logger.error("Error: " + e.getMessage(), e);
                dates = ClimateDates.getMissingClimateDates();
            }
            periodName = QCDialog.SEASONAL_SELECTION.toLowerCase();
            periodType = PeriodType.SEASONAL_RAD;

            refreshDatabaseSupport(dates, periodName, periodType);

            dates = new ClimateDates(this.qcDialog.monthlyDate.getYear());
            periodName = QCDialog.ANNUAL_SELECTION.toLowerCase();
            periodType = PeriodType.ANNUAL_RAD;

            refreshDatabaseSupport(dates, periodName, periodType);
        }
    }

    /**
     * Update period entries according to a changed monthly entry.
     * 
     * @param dates
     * @param periodName
     * @param periodType
     */
    private void refreshDatabaseSupport(ClimateDates dates, String periodName,
            PeriodType periodType) {
        Station currentStation = this.qcDialog.stations
                .get(this.qcDialog.stationNames.getSelectionIndex());

        PeriodServiceRequest request = new PeriodServiceRequest(
                currentStation.getInformId(), periodType, dates);
        PeriodData tempData;
        QueryData queryData = new QueryData();

        try {
            queryData = (QueryData) ThriftClient.sendRequest(request);
            tempData = (PeriodData) queryData.getData();
        } catch (VizException e) {
            tempData = PeriodData.getMissingPeriodData();
            logger.error("Unable to retrieve the corresponding " + periodName
                    + " data for this date.", e);
        }

        if (queryData.getExists()) {

            boolean update = MessageDialog.openQuestion(getShell(),
                    "Update Database", "Is it OK to update the " + periodName
                            + " record for this date?");

            if (update) {
                PeriodData newData;
                BuildPeriodServiceRequest buildRequest = new BuildPeriodServiceRequest(
                        currentStation.getInformId(), dates,
                        this.qcDialog.climateGlobals, periodType);

                try {
                    newData = (PeriodData) ThriftClient
                            .sendRequest(buildRequest);

                    newData.setDataMethods(tempData.getDataMethods());
                    // update database
                    PeriodClimateServiceUpdateRequest updateRequest = new PeriodClimateServiceUpdateRequest(
                            newData.getInformId(), dates, periodType, newData);
                    try {
                        ThriftClient.sendRequest(updateRequest);
                    } catch (VizException e) {
                        logger.error("Could not save values for station ID "
                                + tempData.getInformId(), e);
                    }

                } catch (VizException e) {
                    logger.error(
                            "Unable to refresh the database for the corresponding "
                                    + periodName + " data for this date.",
                            e);
                }
            }
        }
    }

    /**
     * A helper method for getting a List<ClimateDate> from a set of
     * DateSelectionComps
     * 
     * @param dayFields
     * @return dayList
     */
    private java.util.List<ClimateDate> getClimateDateDayList(
            AbstractDateComp[] dayFields) {
        java.util.List<ClimateDate> dayList = new ArrayList<ClimateDate>();
        for (AbstractDateComp dateComp : dayFields) {
            if (isMonthly) {
                String date = ((MonthlyDayComp) dateComp).getText();
                if (!date.equals(ClimateDate.MISSING_DATE_NUM_STRING)) {
                    int dayInt = Integer.parseInt(date);
                    dayList.add(new ClimateDate(dayInt,
                            this.qcDialog.monthlyDate.getMon(),
                            this.qcDialog.monthlyDate.getYear()));
                }
            } else if (!dateComp.getDate().toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                dayList.add(dateComp.getDate());
            }
        }
        return dayList;
    }

    /**
     * A helper method for getting a List<ClimateDates> from a set of Begin and
     * End date DateSelectionComps
     * 
     * @param beginDayFields
     * @param endDayFields
     * @return
     */
    private java.util.List<ClimateDates> getClimateDatesBeginEndList(
            AbstractDateComp[] beginDayFields,
            AbstractDateComp[] endDayFields) {
        java.util.List<ClimateDates> dayList = new ArrayList<ClimateDates>();

        for (int i = 0; i < beginDayFields.length; i++) {
            if (isMonthly) {
                String startDate = ((MonthlyDayComp) beginDayFields[i])
                        .getText();
                String endDate = ((MonthlyDayComp) endDayFields[i]).getText();

                if (!startDate.equals(ClimateDate.MISSING_DATE_NUM_STRING)
                        && !endDate
                                .equals(ClimateDate.MISSING_DATE_NUM_STRING)) {
                    int startDayInt = Integer.parseInt(startDate);
                    int endDayInt = Integer.parseInt(endDate);
                    dayList.add(new ClimateDates(
                            new ClimateDate(startDayInt,
                                    this.qcDialog.monthlyDate.getMon(),
                                    this.qcDialog.monthlyDate.getYear()),
                            new ClimateDate(endDayInt,
                                    this.qcDialog.monthlyDate.getMon(),
                                    this.qcDialog.monthlyDate.getYear())));
                }
            } else if (!beginDayFields[i].getDate().toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && !endDayFields[i].getDate().toMonthDayDateString().equals(
                            ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {

                ClimateDate startClimateDate = beginDayFields[i].getDate();

                ClimateDate endClimateDate = endDayFields[i].getDate();

                dayList.add(new ClimateDates(startClimateDate, endClimateDate));
            }
        }

        return dayList;

    }

    /**
     * A helper method for getting a List<ClimateDates> from a set of Begin and
     * End date DateSelectionComps with Hour fields
     * 
     * @param beginDayFields
     * @param endDayFields
     * @param beginHourFields
     * @param endHourFields
     * @return dayHourList
     */
    private java.util.List<ClimateDates> getClimateDatesBeginEndList(
            AbstractDateComp[] beginDayFields, AbstractDateComp[] endDayFields,
            Text[] beginHourFields, Text[] endHourFields) {
        java.util.List<ClimateDates> dayHourList = new ArrayList<ClimateDates>();

        for (int i = 0; i < beginDayFields.length; i++) {
            int startHour = Integer.parseInt(beginHourFields[i].getText());
            int endHour = Integer.parseInt(endHourFields[i].getText());

            if (isMonthly) {
                String startDate = ((MonthlyDayComp) beginDayFields[i])
                        .getText();
                String endDate = ((MonthlyDayComp) endDayFields[i]).getText();
                if (!startDate.equals(ClimateDate.MISSING_DATE_NUM_STRING)
                        && !endDate.equals(ClimateDate.MISSING_DATE_NUM_STRING)
                        && startHour != ParameterFormatClimate.MISSING_HOUR
                        && endHour != ParameterFormatClimate.MISSING_HOUR) {

                    int startDayInt = Integer.parseInt(startDate);
                    int endDayInt = Integer.parseInt(endDate);

                    dayHourList.add(
                            new ClimateDates(
                                    new ClimateDate(startDayInt,
                                            this.qcDialog.monthlyDate.getMon(),
                                            this.qcDialog.monthlyDate
                                                    .getYear()),
                                    new ClimateDate(endDayInt,
                                            this.qcDialog.monthlyDate.getMon(),
                                            this.qcDialog.monthlyDate
                                                    .getYear()),
                                    startHour, endHour));
                }
            } else if (!beginDayFields[i].getDate().toMonthDayDateString()
                    .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && !endDayFields[i].getDate().toMonthDayDateString()
                            .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)
                    && startHour != ParameterFormatClimate.MISSING_HOUR
                    && endHour != ParameterFormatClimate.MISSING_HOUR) {

                ClimateDate startClimateDate = beginDayFields[i].getDate();

                ClimateDate endClimateDate = endDayFields[i].getDate();

                dayHourList.add(new ClimateDates(startClimateDate,
                        endClimateDate, startHour, endHour));
            }
        }

        return dayHourList;

    }

    /**
     * Clear displayed values to all missing.
     */
    private void clearValues() {
        String missingString = String.valueOf(ParameterFormatClimate.MISSING);
        String missingPrecipString = String
                .valueOf(ParameterFormatClimate.MISSING_PRECIP);
        String missingHourString = String
                .valueOf(ParameterFormatClimate.MISSING_HOUR);
        String missingSnowString = String
                .valueOf(ParameterFormatClimate.MISSING_SNOW);
        String missingSpeedString = String
                .valueOf(ParameterFormatClimate.MISSING_SPEED);

        maxTempTF.setTextAndTip(missingString, ParameterFormatClimate.MISSING);
        for (AbstractDateComp dateComp : maxTempDates) {
            dateComp.setMissing();
        }

        avgMaxTempTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);

        meanTempTF.setTextAndTip(missingString, ParameterFormatClimate.MISSING);

        maxTempGreater90DegTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        maxTempLess32DegTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        if (maxTempGreaterDegCustom1TF.isEnabled()) {
            maxTempGreaterDegCustom1TF.setText(missingString);
        }
        if (maxTempGreaterDegCustom2TF.isEnabled()) {
            maxTempGreaterDegCustom2TF.setText(missingString);
        }
        if (maxTempLessDegCustomTF.isEnabled()) {
            maxTempLessDegCustomTF.setText(missingString);
        }

        minTempTF.setTextAndTip(missingString, ParameterFormatClimate.MISSING);
        for (AbstractDateComp dateComp : minTempDates) {
            dateComp.setMissing();
        }

        avgMinTempTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        minTempLess32DegTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        minTempLess0DegTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        if (minTempGreaterDegCustomTF.isEnabled()) {
            minTempGreaterDegCustomTF.setText(missingString);
        }
        if (minTempLessDegCustom1TF.isEnabled()) {
            minTempLessDegCustom1TF.setText(missingString);
        }
        if (minTempLessDegCustom2TF.isEnabled()) {
            minTempLessDegCustom2TF.setText(missingString);
        }

        heatingDaysTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_DEGREE_DAY),
                ParameterFormatClimate.MISSING);
        coolingDaysTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_DEGREE_DAY),
                ParameterFormatClimate.MISSING);

        totalPrecipTF.setText(missingPrecipString);
        avgDailyPrecipTF.setText(missingPrecipString);

        precipInchesGreater01TF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        precipInchesGreater10TF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        precipInchesGreater50TF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        precipInchesGreater100TF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        if (precipInchesGreaterCustom1TF.isEnabled()) {
            precipInchesGreaterCustom1TF.setText(missingString);
        }
        if (precipInchesGreaterCustom2TF.isEnabled()) {
            precipInchesGreaterCustom2TF.setText(missingString);
        }

        maxPrecip24HourTF.setTextAndTip(missingPrecipString,
                ParameterFormatClimate.MISSING);
        for (AbstractDateComp dateComp : maxPrecip24HourBeginDates) {
            dateComp.setMissing();
        }
        for (AbstractDateComp dateComp : maxPrecip24HourEndDates) {
            dateComp.setMissing();
        }

        greatestPrecipStormTF.setText(missingPrecipString);
        for (AbstractDateComp dateComp : greatestPrecipStormBeginDates) {
            dateComp.setMissing();
        }
        for (AbstractDateComp dateComp : greatestPrecipStormEndDates) {
            dateComp.setMissing();
        }
        for (Text text : greatestPrecipStormBeginHourTFs) {
            text.setText(missingHourString);
        }
        for (Text text : greatestPrecipStormEndHourTFs) {
            text.setText(missingHourString);
        }

        totalSnowTF.setText(missingSnowString);
        totalSnowWaterEquivTF.setText(missingSnowString);

        anySnowGreaterThanTraceTF.setText(missingString);
        snowGreaterThan1TF.setText(missingString);
        if (snowCustomGreaterTF.isEnabled()) {
            snowCustomGreaterTF.setText(missingString);
        }

        avgSnowDepthGroundTF.setText(missingSnowString);

        maxSnowDepthGroundTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        for (AbstractDateComp dateComp : maxSnowDepthGroundDates) {
            dateComp.setMissing();
        }

        maxSnow24HourTF.setTextAndTip(missingSnowString,
                ParameterFormatClimate.MISSING);
        for (AbstractDateComp dateComp : maxSnow24HourBeginDates) {
            dateComp.setMissing();
        }
        for (AbstractDateComp dateComp : maxSnow24HourEndDates) {
            dateComp.setMissing();
        }

        greatestSnowStormTF.setText(missingSnowString);
        for (AbstractDateComp dateComp : greatestSnowStormBeginDates) {
            dateComp.setMissing();
        }
        for (AbstractDateComp dateComp : greatestSnowStormEndDates) {
            dateComp.setMissing();
        }
        for (Text text : greatestSnowStormBeginHourTFs) {
            text.setText(missingHourString);
        }
        for (Text text : greatestSnowStormEndHourTFs) {
            text.setText(missingHourString);
        }

        maxWindSpeedTF.setText(missingSpeedString);
        for (Text text : maxWindDirTFs) {
            text.setText(missingString);
        }
        for (AbstractDateComp dateComp : maxWindDates) {
            dateComp.setMissing();
        }

        resultantWindSpeedTF.setText(missingSpeedString);
        resultantWindDirTF.setText(missingString);

        maxGustSpeedTF.setText(missingSpeedString);
        for (Text text : maxGustDirTFs) {
            text.setText(missingString);
        }
        for (AbstractDateComp dateComp : maxGustDates) {
            dateComp.setMissing();
        }

        averageWindSpeedTF.setText(missingSpeedString);

        percentPossSunshineTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);

        skyCoverTF.setText(missingString);

        meanRelHumdTF.setText(missingString);

        fairTF.setTextAndTip(missingString, ParameterFormatClimate.MISSING);
        partlyCloudyTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);
        mostlyCloudyTF.setTextAndTip(missingString,
                ParameterFormatClimate.MISSING);

        thunderTF.setText(missingString);
        mixedPrecipTF.setText(missingString);
        heavyRainTF.setText(missingString);
        rainTF.setText(missingString);
        lightRainTF.setText(missingString);
        freezingRainTF.setText(missingString);
        lightFreezingRainTF.setText(missingString);
        hailTF.setText(missingString);
        heavySnowTF.setText(missingString);
        snowTF.setText(missingString);
        lightSnowTF.setText(missingString);
        icePelletsTF.setText(missingString);
        fogTF.setText(missingString);
        heavyFogTF.setText(missingString);
        hazeTF.setText(missingString);
    }

}