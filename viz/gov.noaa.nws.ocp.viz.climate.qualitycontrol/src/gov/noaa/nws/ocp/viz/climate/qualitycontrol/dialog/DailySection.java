/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.qualitycontrol.dialog;

import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyDataMethod;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.QueryData;
import gov.noaa.nws.ocp.common.dataplugin.climate.SeasonType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.BuildPeriodServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.CompareUpdateDailyRecordsRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.DailyClimateServiceUpdateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.GetLastYearRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.PeriodClimateServiceUpdateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.PeriodServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.UpdateFreezeDBRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchClimateDayRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchFreezeDatesRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.qualitycontrol.DetFreezeDatesRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.climate.qualitycontrol.QCDataComposite;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.QCTextComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.QCToolTip;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.TimeSelectorFocusListener;

/**
 * QCDataComposite child class representing a Daily composite.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2016  20636      wpaintsil   Initial creation
 * Sep 02, 2016  20636      wpaintsil   Extend QCDataComposite
 * Sep 15, 2016  20636      wpaintsil   Use UnsavedChangesListener
 * Oct 27, 2016  22135      wpaintsil   Add data method tooltips
 * Nov 17, 2016  20636      wpaintsil   Implementation of cascading updates to monthly/seasonal/annual 
 *                                      data after updating daily data.
 * Nov 28, 2016  20636      wpaintsil   Update Freeze Dates/Records
 * Dec 02, 2016  20636      wpaintsil   Refactor some repetitive text creation
 * Aug 08, 2019  DR21517    wpaintsil   The field holding the number of WX observations was
 *                                      not updated.
 * </pre>
 * 
 * @author wpaintsil
 */

public class DailySection extends QCDataComposite {
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DailySection.class);

    /**
     * Dialog parent.
     */
    private final QCDialog qcDialog;

    private QCTextComp maxTempField;

    private QCTextComp minTempField;

    private Text timeMaxTempField;

    private Text timeMinTempField;

    private Text maxHumdField;

    private Text timeMaxHumdField;

    private Text minHumdField;

    private Text timeMinHumdField;

    private QCTextComp maxWindDirField;

    private QCTextComp maxWindSpeedField;

    private Text timeMaxWindField;

    private QCTextComp maxWindGustDirField;

    private QCTextComp maxWindGustSpeedField;

    private Text timeMaxWindGustField;

    private Text resWindSpeedField;

    private Text resWindDirField;

    private QCTextComp dailyPrecField;

    private QCTextComp dailySnowField;

    private QCTextComp snowDepthField;

    private QCTextComp avgWindField;

    private QCTextComp minutesSunField;

    private QCTextComp possibleSunField;

    private QCTextComp avgSkyCoverField;

    private Text maxPressureField;

    private Text minPressureField;

    private Button checkboxTS;

    private Button checkboxPlusSN;

    private Button checkboxRASN;

    private Button checkboxSN;

    private Button checkboxPlusRA;

    private Button checkboxMinusSN;

    private Button checkboxRA;

    private Button checkboxPL;

    private Button checkboxMinusRA;

    private Button checkboxFG;

    private Button checkboxFZRA;

    private Button checkboxFG14;

    private Button checkboxMinusFRZA;

    private Button checkboxHZ;

    private Button checkboxGR;

    private Button checkboxSS;

    private Button checkboxBLSN;

    private Button checkboxPlusFC;

    private QCToolTip weatherToolTip;

    public DailySection(QCDialog qcDialog, Composite parent, int style) {
        super(parent, style);
        this.qcDialog = qcDialog;

        FormLayout mainDataLayout = new FormLayout();
        mainDataLayout.marginBottom = 20;
        this.setLayout(mainDataLayout);

        Group tempGroup = createTemperatureSection(this);
        Group moistureGroup = createMoistureSection(this);
        Group windGroup = createWindSection(this);

        Group precipGroup = createPrecipitationSection(this);
        Group skyGroup = createSkyConditionsSection(this);
        Group pressureGroup = createPressureSection(this);
        Group observedGroup = createObservedWeatherSection(this);

        GC gc = new GC(tempGroup);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        FormData formData = new FormData();
        formData.left = new FormAttachment(4);
        formData.top = new FormAttachment(2);
        formData.width = 47 * fontWidth;
        tempGroup.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(4);
        formData.top = new FormAttachment(tempGroup, 10);
        formData.width = 47 * fontWidth;
        moistureGroup.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(4);
        formData.top = new FormAttachment(moistureGroup, 10);
        formData.width = 47 * fontWidth;
        windGroup.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(tempGroup, 60);
        formData.top = new FormAttachment(2);
        formData.width = 41 * fontWidth;
        precipGroup.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(tempGroup, 60);
        formData.top = new FormAttachment(precipGroup, 10);
        formData.width = 41 * fontWidth;
        skyGroup.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(tempGroup, 60);
        formData.top = new FormAttachment(skyGroup, 10);
        formData.width = 41 * fontWidth;
        pressureGroup.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(tempGroup, 60);
        formData.top = new FormAttachment(pressureGroup, 10);
        formData.width = 41 * fontWidth;
        observedGroup.setLayoutData(formData);

    }

    private Group createTemperatureSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Temperature");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(2, false);
        dataLayout.verticalSpacing = 10;
        dataLayout.horizontalSpacing = 15;
        dataLayout.marginBottom = 5;
        dataSection.setLayout(dataLayout);

        maxTempField = createText(dataSection, "Maximum Temperature",
                "Maximum Temperature", QCValueType.TEMPERATURE,
                this.qcDialog.displayListeners.getTempIntListener(),
                this.qcDialog.getChangeListener());
        ((RowLayout) maxTempField.getLayout()).marginLeft = 0;

        Label timeMaxLabel = new Label(dataSection, SWT.NORMAL);
        timeMaxLabel.setText("Time of Maximum Temperature");
        timeMaxTempField = new Text(dataSection,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(timeMaxTempField);
        timeMaxTempField.addFocusListener(
                new TimeSelectorFocusListener(timeMaxTempField));
        timeMaxTempField.addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        minTempField = createText(dataSection, "Minimum Temperature",
                "Minimum Temperature", QCValueType.TEMPERATURE,
                this.qcDialog.displayListeners.getTempIntListener(),
                this.qcDialog.getChangeListener());
        ((RowLayout) minTempField.getLayout()).marginLeft = 0;

        Label timeMinLabel = new Label(dataSection, SWT.NORMAL);
        timeMinLabel.setText("Time of Minimum Temperature");
        timeMinTempField = new Text(dataSection,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(timeMinTempField);
        timeMinTempField.addFocusListener(
                new TimeSelectorFocusListener(timeMinTempField));
        timeMinTempField.addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        return dataSection;
    }

    private Group createMoistureSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Moisture");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(2, false);
        dataLayout.verticalSpacing = 10;
        dataLayout.horizontalSpacing = 25;
        dataLayout.marginBottom = 5;
        dataSection.setLayout(dataLayout);

        maxHumdField = createText(dataSection, "Maximum Relative Humidity",
                this.qcDialog.displayListeners.getRelHumListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(maxHumdField);

        Label timeMaxHumdLabel = new Label(dataSection, SWT.NORMAL);
        timeMaxHumdLabel.setText("Hour of Maximum Relative\nHumidity");
        timeMaxHumdField = new Text(dataSection,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(timeMaxHumdField);
        timeMaxHumdField.addFocusListener(
                new TimeSelectorFocusListener(timeMaxHumdField, true));
        timeMaxHumdField.addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        minHumdField = createText(dataSection, "Minimum Relative Humidity",
                this.qcDialog.displayListeners.getRelHumListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(minHumdField);

        Label timeMinHumdLabel = new Label(dataSection, SWT.NORMAL);
        timeMinHumdLabel.setText("Hour of Minimum Relative\nHumidity");
        timeMinHumdField = new Text(dataSection,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignShortFieldsGD(timeMinHumdField);
        timeMinHumdField.addFocusListener(
                new TimeSelectorFocusListener(timeMinHumdField, true));
        timeMinHumdField.addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        return dataSection;
    }

    private Group createWindSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Wind (mph)");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(2, false);
        dataLayout.verticalSpacing = 10;
        dataLayout.marginBottom = 5;
        dataSection.setLayout(dataLayout);

        Label maxWindLabel = new Label(dataSection, SWT.NORMAL);
        maxWindLabel.setText("Maximum Wind\nDirection and Speed");
        Composite dirAndSpeedComp1 = new Composite(dataSection, SWT.NONE);
        RowLayout dirAndSpeedLayout1 = new RowLayout(SWT.HORIZONTAL);
        dirAndSpeedComp1.setLayout(dirAndSpeedLayout1);
        maxWindDirField = new QCTextComp(dirAndSpeedComp1, SWT.NONE,
                "Maximum Wind Direction", QCValueType.WIND);
        ((RowLayout) maxWindDirField.getLayout()).marginLeft = 0;
        ((RowLayout) maxWindDirField.getLayout()).marginRight = 0;
        addTextListeners(maxWindDirField,
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());

        maxWindSpeedField = new QCTextComp(dirAndSpeedComp1, SWT.NONE,
                "Maximum Wind Speed", QCValueType.WIND);
        ((RowLayout) maxWindSpeedField.getLayout()).marginLeft = 0;
        addTextListeners(maxWindSpeedField,
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());

        Label timeMaxWindLabel = new Label(dataSection, SWT.NORMAL);
        timeMaxWindLabel.setText("Time of Maximum\nWind");
        Composite timeMaxWindComp = new Composite(dataSection, SWT.NONE);
        RowLayout timeMaxWindLayout = new RowLayout(SWT.HORIZONTAL);
        timeMaxWindComp.setLayout(timeMaxWindLayout);
        timeMaxWindField = new Text(timeMaxWindComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(timeMaxWindField);
        timeMaxWindField.addFocusListener(
                new TimeSelectorFocusListener(timeMaxWindField));
        timeMaxWindField.addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        Label maxWindGustLabel = new Label(dataSection, SWT.NORMAL);
        maxWindGustLabel.setText("Maximum Wind Gust\nDirection and Speed");
        Composite dirAndSpeedComp2 = new Composite(dataSection, SWT.NONE);
        RowLayout dirAndSpeedLayout2 = new RowLayout(SWT.HORIZONTAL);
        dirAndSpeedComp2.setLayout(dirAndSpeedLayout2);
        maxWindGustDirField = new QCTextComp(dirAndSpeedComp2, SWT.NONE,
                "Maximum Gust Direction", QCValueType.GUST);
        ((RowLayout) maxWindGustDirField.getLayout()).marginLeft = 0;
        ((RowLayout) maxWindGustDirField.getLayout()).marginRight = 0;
        addTextListeners(maxWindGustDirField,
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());

        maxWindGustSpeedField = new QCTextComp(dirAndSpeedComp2, SWT.NONE,
                "Maximum Gust Speed", QCValueType.GUST);
        ((RowLayout) maxWindGustSpeedField.getLayout()).marginLeft = 0;
        addTextListeners(maxWindGustSpeedField,
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());

        Label timeMaxWindGustLabel = new Label(dataSection, SWT.NORMAL);
        timeMaxWindGustLabel.setText("Time of Maximum\nWind Gust");
        Composite timeMaxWindGustComp = new Composite(dataSection, SWT.NONE);
        RowLayout timeMaxWindGustLayout = new RowLayout(SWT.HORIZONTAL);
        timeMaxWindGustComp.setLayout(timeMaxWindGustLayout);
        timeMaxWindGustField = new Text(timeMaxWindGustComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(timeMaxWindGustField);
        timeMaxWindGustField.addFocusListener(
                new TimeSelectorFocusListener(timeMaxWindGustField));
        timeMaxWindGustField.addListener(SWT.Modify,
                this.qcDialog.getChangeListener());

        Label resWindLabel = new Label(dataSection, SWT.NORMAL);
        resWindLabel.setText("Resultant Wind\nDirection and Speed");
        Composite dirAndSpeedComp3 = new Composite(dataSection, SWT.NONE);
        RowLayout dirAndSpeedLayout3 = new RowLayout(SWT.HORIZONTAL);
        dirAndSpeedComp3.setLayout(dirAndSpeedLayout3);
        resWindDirField = new Text(dirAndSpeedComp3,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(resWindDirField);

        addTextListeners(resWindDirField,
                this.qcDialog.displayListeners.getWindDirListener(),
                this.qcDialog.getChangeListener());

        resWindSpeedField = new Text(dirAndSpeedComp3,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(resWindSpeedField);
        addTextListeners(resWindSpeedField,
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());

        avgWindField = createTextWithComposite(dataSection,
                "Average Wind\nSpeed", "Average Wind Speed",
                QCValueType.AVGWIND,
                this.qcDialog.displayListeners.getWindSpdListener(),
                this.qcDialog.getChangeListener());
        ((RowLayout) avgWindField.getLayout()).marginLeft = 0;

        return dataSection;
    }

    private Group createPrecipitationSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Precipitation");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(3, false);
        dataLayout.verticalSpacing = 10;
        dataLayout.marginBottom = 5;
        dataSection.setLayout(dataLayout);

        dailyPrecField = createText(dataSection, "Daily Precipitation",
                "Precipitation Total", QCValueType.PRECIP,
                this.qcDialog.displayListeners.getPrecipListener(),
                this.qcDialog.getChangeListener());
        new Label(dataSection, SWT.NORMAL).setText("inches");

        dailySnowField = createText(dataSection, "Daily Snowfall",
                "Snowfall Total", QCValueType.SNOW,
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener());
        new Label(dataSection, SWT.NORMAL).setText("inches");

        snowDepthField = createText(dataSection, "Snow Depth", "Snow Depth",
                QCValueType.SNOWDEPTH,
                this.qcDialog.displayListeners.getSnowFallListener(),
                this.qcDialog.getChangeListener());
        new Label(dataSection, SWT.NORMAL).setText("inches");

        return dataSection;
    }

    private Group createSkyConditionsSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Sky Conditions");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(2, false);
        dataLayout.verticalSpacing = 10;
        dataLayout.marginBottom = 5;
        dataSection.setLayout(dataLayout);

        minutesSunField = createTextWithComposite(dataSection,
                "Minutes of Sunshine", "Minutes of Sunshine",
                QCValueType.MINSUN,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ((RowLayout) minutesSunField.getLayout()).center = true;
        ((RowLayout) minutesSunField.getLayout()).marginWidth = 10;

        possibleSunField = createTextWithComposite(dataSection,
                "Percent Possible\nSunshine", "Percent Possible\nSunshine",
                QCValueType.SUNSHINE,
                this.qcDialog.displayListeners.getDefaultIntListener(),
                this.qcDialog.getChangeListener());
        ((RowLayout) possibleSunField.getLayout()).center = true;
        ((RowLayout) possibleSunField.getLayout()).marginWidth = 10;

        avgSkyCoverField = createTextWithComposite(dataSection,
                "Average Sky Cover", "Average Sky Cover", QCValueType.SKYCOVER,
                this.qcDialog.displayListeners.getSkyCoverListener(),
                this.qcDialog.getChangeListener());
        ((RowLayout) avgSkyCoverField.getLayout()).center = true;
        ((RowLayout) avgSkyCoverField.getLayout()).marginWidth = 10;

        return dataSection;
    }

    private Group createPressureSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Pressure");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(3, false);
        dataLayout.verticalSpacing = 10;
        dataLayout.marginBottom = 5;
        dataSection.setLayout(dataLayout);

        maxPressureField = createText(dataSection,
                "Maximum Sea Level\nPressure",
                this.qcDialog.displayListeners.getSlpListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(maxPressureField);
        new Label(dataSection, SWT.NORMAL).setText("inches");

        minPressureField = createText(dataSection,
                "Minimum Sea Level\nPressure",
                this.qcDialog.displayListeners.getSlpListener(),
                this.qcDialog.getChangeListener());
        ClimateLayoutValues.assignFieldsGD(minPressureField);
        new Label(dataSection, SWT.NORMAL).setText("inches");

        return dataSection;
    }

    private Group createObservedWeatherSection(Composite parent) {
        Group dataSection = new Group(parent, SWT.SHADOW_IN);
        dataSection.setText("Observed Weather");
        dataSection.setFont(this.qcDialog.boldFont);
        GridLayout dataLayout = new GridLayout(2, true);
        dataLayout.horizontalSpacing = 10;
        dataLayout.verticalSpacing = 10;
        dataLayout.marginBottom = 10;
        dataLayout.marginTop = 10;
        dataLayout.marginLeft = 15;
        dataLayout.marginRight = 15;
        dataSection.setLayout(dataLayout);

        weatherToolTip = new QCToolTip(dataSection, dataSection.getText(),
                QCValueType.WEATHER);
        weatherToolTip.setDataValueVisible(false);

        // TS: thunder storms
        checkboxTS = new Button(dataSection, SWT.CHECK);
        checkboxTS.setText("TS");
        checkboxTS.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // +SN: heavy snow
        checkboxPlusSN = new Button(dataSection, SWT.CHECK);
        checkboxPlusSN.setText("+SN");
        checkboxPlusSN.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // RASN: rain and snow
        checkboxRASN = new Button(dataSection, SWT.CHECK);
        checkboxRASN.setText("RASN");
        checkboxRASN.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // SN: snow
        checkboxSN = new Button(dataSection, SWT.CHECK);
        checkboxSN.setText("SN");
        checkboxSN.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // +RA: heavy rain
        checkboxPlusRA = new Button(dataSection, SWT.CHECK);
        checkboxPlusRA.setText("+RA");
        checkboxPlusRA.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // -SN: light snow
        checkboxMinusSN = new Button(dataSection, SWT.CHECK);
        checkboxMinusSN.setText("-SN");
        checkboxMinusSN.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // RA: rain
        checkboxRA = new Button(dataSection, SWT.CHECK);
        checkboxRA.setText("RA");
        checkboxRA.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // PL: ice pellets
        checkboxPL = new Button(dataSection, SWT.CHECK);
        checkboxPL.setText("PL");
        checkboxPL.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // -RA: light rain
        checkboxMinusRA = new Button(dataSection, SWT.CHECK);
        checkboxMinusRA.setText("-RA");
        checkboxMinusRA.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // FG: fog
        checkboxFG = new Button(dataSection, SWT.CHECK);
        checkboxFG.setText("FG");
        checkboxFG.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // FRZA: freezing rain
        checkboxFZRA = new Button(dataSection, SWT.CHECK);
        checkboxFZRA.setText("FZRA");
        checkboxFZRA.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // FG <=1/4SM: fog, visibility less than 0.25 statute miles
        checkboxFG14 = new Button(dataSection, SWT.CHECK);
        checkboxFG14.setText("FG <=1/4SM");
        checkboxFG14.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // -FRZA: light freezing Rain
        checkboxMinusFRZA = new Button(dataSection, SWT.CHECK);
        checkboxMinusFRZA.setText("-FRZA");
        checkboxMinusFRZA.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // HZ: haze
        checkboxHZ = new Button(dataSection, SWT.CHECK);
        checkboxHZ.setText("HZ");
        checkboxHZ.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // GR: hail
        checkboxGR = new Button(dataSection, SWT.CHECK);
        checkboxGR.setText("GR");
        checkboxGR.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // SS: sandstorm
        checkboxSS = new Button(dataSection, SWT.CHECK);
        checkboxSS.setText("SS");
        checkboxSS.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // BLSN: blowing snow
        checkboxBLSN = new Button(dataSection, SWT.CHECK);
        checkboxBLSN.setText("BLSN");
        checkboxBLSN.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        // +FC: tornado/waterspout
        checkboxPlusFC = new Button(dataSection, SWT.CHECK);
        checkboxPlusFC.setText("+FC");
        checkboxPlusFC.addListener(SWT.Selection,
                this.qcDialog.getChangeListener());

        return dataSection;
    }

    private void clearValues() {
        maxTempField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        minTempField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        timeMaxTempField.setText(ClimateTime.MISSING_TIME_STRING);
        timeMinTempField.setText(ClimateTime.MISSING_TIME_STRING);
        maxHumdField.setText(String.valueOf(ParameterFormatClimate.MISSING));
        timeMaxHumdField
                .setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        minHumdField.setText(String.valueOf(ParameterFormatClimate.MISSING));
        timeMinHumdField
                .setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        maxWindDirField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        maxWindSpeedField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SPEED),
                ParameterFormatClimate.MISSING);
        timeMaxWindField.setText(ClimateTime.MISSING_TIME_STRING);
        maxWindGustDirField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        maxWindGustSpeedField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SPEED),
                ParameterFormatClimate.MISSING);
        timeMaxWindGustField.setText(ClimateTime.MISSING_TIME_STRING);
        resWindSpeedField
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SPEED));
        resWindDirField.setText(String.valueOf(ParameterFormatClimate.MISSING));
        dailyPrecField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_PRECIP),
                ParameterFormatClimate.MISSING);
        dailySnowField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SNOW_VALUE),
                ParameterFormatClimate.MISSING);
        snowDepthField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        avgWindField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        minutesSunField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        possibleSunField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        avgSkyCoverField.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        maxPressureField
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SLP));
        minPressureField
                .setText(String.valueOf(ParameterFormatClimate.MISSING_SLP));
        checkboxTS.setSelection(false);
        checkboxPlusSN.setSelection(false);
        checkboxRASN.setSelection(false);
        checkboxSN.setSelection(false);
        checkboxPlusRA.setSelection(false);
        checkboxMinusSN.setSelection(false);
        checkboxRA.setSelection(false);
        checkboxPL.setSelection(false);
        checkboxMinusRA.setSelection(false);
        checkboxFG.setSelection(false);
        checkboxFZRA.setSelection(false);
        checkboxFG14.setSelection(false);
        checkboxMinusFRZA.setSelection(false);
        checkboxHZ.setSelection(false);
        checkboxGR.setSelection(false);
        checkboxSS.setSelection(false);
        checkboxBLSN.setSelection(false);
        checkboxPlusFC.setSelection(false);
        weatherToolTip.setQCValue(ParameterFormatClimate.MISSING);
    }

    @Override
    public void loadData() {
        this.qcDialog.getChangeListener().setIgnoreChanges(true);

        clearValues();

        int selectedStationIndex = this.qcDialog.stationNames
                .getSelectionIndex();

        if (selectedStationIndex >= 0
                && selectedStationIndex < this.qcDialog.stations.size()) {
            Station newStation = this.qcDialog.stations
                    .get(selectedStationIndex);

            this.qcDialog.dailyRecords = new ClimateDayNorm();
            this.qcDialog.dailyRecords.setDataToMissing();
            // get daily records
            try {
                ClimateDayNorm records = (ClimateDayNorm) ThriftClient
                        .sendRequest(new FetchClimateDayRecordRequest(
                                this.qcDialog.dailyDate.toMonthDayDateString(),
                                newStation.getInformId()));

                this.qcDialog.dailyRecords = records;

            } catch (VizException e) {
                String message = "Could not retrieve daily records, with station ID ["
                        + newStation.getInformId() + "] and date ["
                        + this.qcDialog.dailyDateSelector.getDate()
                                .toFullDateString()
                        + "]. Select a different station.";
                logger.error(message, e);
                MessageDialog.openError(getShell(), "Data Retrieval Error",
                        message);
            }
            GetLastYearRequest request = new GetLastYearRequest(
                    newStation.getInformId(), this.qcDialog.dailyDate);

            try {
                QueryData queryData = (QueryData) ThriftClient
                        .sendRequest(request);
                DailyClimateData data = (DailyClimateData) queryData.getData();

                this.qcDialog.dailyData = data;

                if (!queryData.getExists()) {
                    MessageDialog.openWarning(getShell(), "Missing Data",
                            "There is no data available for the station ID ["
                                    + newStation.getInformId() + "] and date ["
                                    + this.qcDialog.dailyDateSelector.getDate()
                                            .toFullDateString()
                                    + "].");

                    this.qcDialog.dailyData = DailyClimateData
                            .getMissingDailyClimateData();
                    this.qcDialog.dailyData
                            .setInformId(newStation.getInformId());
                } else {
                    maxTempField.setTextAndTip(
                            String.valueOf(data.getMaxTemp()),
                            data.getDataMethods().getMaxTempQc());
                    timeMaxTempField
                            .setText(data.getMaxTempTime().toHourMinString());
                    minTempField.setTextAndTip(
                            String.valueOf(data.getMinTemp()),
                            data.getDataMethods().getMinTempQc());
                    timeMinTempField
                            .setText(data.getMinTempTime().toHourMinString());

                    maxHumdField.setText(String.valueOf(data.getMaxRelHumid()));
                    timeMaxHumdField
                            .setText(String.valueOf(data.getMaxRelHumidHour()));
                    minHumdField.setText(String.valueOf(data.getMinRelHumid()));
                    timeMinHumdField
                            .setText(String.valueOf(data.getMinRelHumidHour()));

                    ClimateWind maxWind = data.getMaxWind();
                    maxWindDirField.setTextAndTip(
                            String.valueOf(maxWind.getDir()),
                            data.getDataMethods().getMaxWindQc());
                    maxWindSpeedField.setTextAndTip(
                            String.valueOf(maxWind.getSpeed()),
                            data.getDataMethods().getMaxWindQc());
                    timeMaxWindField
                            .setText(data.getMaxWindTime().toHourMinString());
                    ClimateWind maxGust = data.getMaxGust();
                    maxWindGustDirField.setTextAndTip(
                            String.valueOf(maxGust.getDir()),
                            data.getDataMethods().getMaxGustQc());
                    maxWindGustSpeedField.setTextAndTip(
                            String.valueOf(maxGust.getSpeed()),
                            data.getDataMethods().getMaxGustQc());
                    timeMaxWindGustField
                            .setText(data.getMaxGustTime().toHourMinString());
                    ClimateWind resWind = data.getResultWind();
                    resWindDirField.setText(String.valueOf(resWind.getDir()));
                    resWindSpeedField
                            .setText(String.valueOf(resWind.getSpeed()));
                    avgWindField.setTextAndTip(
                            String.valueOf(data.getAvgWindSpeed()),
                            data.getDataMethods().getAvgWindQc());

                    dailyPrecField.setTextAndTip(
                            String.valueOf(data.getPrecip()),
                            data.getDataMethods().getPrecipQc());
                    // Change the text to the trace symbol if it's a trace
                    // value.
                    if (data.getPrecip() == ParameterFormatClimate.TRACE) {
                        dailyPrecField
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }

                    dailySnowField.setTextAndTip(
                            String.valueOf(data.getSnowDay()),
                            data.getDataMethods().getSnowQc());
                    if (data.getSnowDay() == ParameterFormatClimate.TRACE) {
                        dailySnowField
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }

                    snowDepthField.setTextAndTip(
                            String.valueOf(data.getSnowGround()),
                            data.getDataMethods().getDepthQc());
                    if (data.getSnowGround() == ParameterFormatClimate.TRACE) {
                        snowDepthField
                                .setText(ParameterFormatClimate.TRACE_SYMBOL);
                    }

                    minutesSunField.setTextAndTip(
                            String.valueOf(data.getMinutesSun()),
                            data.getDataMethods().getMinSunQc());
                    possibleSunField.setTextAndTip(
                            String.valueOf(data.getPercentPossSun()),
                            data.getDataMethods().getPossSunQc());
                    avgSkyCoverField.setTextAndTip(
                            String.valueOf(data.getSkyCover()),
                            data.getDataMethods().getSkyCoverQc());

                    maxPressureField.setText(String.valueOf(data.getMaxSlp()));
                    minPressureField.setText(String.valueOf(data.getMinSlp()));

                    weatherToolTip
                            .setQCValue(data.getDataMethods().getWeatherQc());

                    int[] wxTypes = data.getWxType();

                    try {
                        checkboxTS.setSelection(
                                wxTypes[DailyClimateData.WX_THUNDER_STORM_INDEX] == 1);
                        checkboxPlusSN.setSelection(
                                wxTypes[DailyClimateData.WX_HEAVY_SNOW_INDEX] == 1);
                        checkboxRASN.setSelection(
                                wxTypes[DailyClimateData.WX_MIXED_PRECIP_INDEX] == 1);
                        checkboxSN.setSelection(
                                wxTypes[DailyClimateData.WX_SNOW_INDEX] == 1);
                        checkboxPlusRA.setSelection(
                                wxTypes[DailyClimateData.WX_HEAVY_RAIN_INDEX] == 1);
                        checkboxMinusSN.setSelection(
                                wxTypes[DailyClimateData.WX_LIGHT_SNOW_INDEX] == 1);
                        checkboxRA.setSelection(
                                wxTypes[DailyClimateData.WX_RAIN_INDEX] == 1);
                        checkboxPL.setSelection(
                                wxTypes[DailyClimateData.WX_ICE_PELLETS_INDEX] == 1);
                        checkboxMinusRA.setSelection(
                                wxTypes[DailyClimateData.WX_LIGHT_RAIN_INDEX] == 1);
                        checkboxFG.setSelection(
                                wxTypes[DailyClimateData.WX_FOG_INDEX] == 1);
                        checkboxFZRA.setSelection(
                                wxTypes[DailyClimateData.WX_FREEZING_RAIN_INDEX] == 1);
                        checkboxFG14.setSelection(
                                wxTypes[DailyClimateData.WX_FOG_QUARTER_SM_INDEX] == 1);
                        checkboxMinusFRZA.setSelection(
                                wxTypes[DailyClimateData.WX_LIGHT_FREEZING_RAIN_INDEX] == 1);
                        checkboxHZ.setSelection(
                                wxTypes[DailyClimateData.WX_HAZE_INDEX] == 1);
                        checkboxGR.setSelection(
                                wxTypes[DailyClimateData.WX_HAIL_INDEX] == 1);
                        checkboxSS.setSelection(
                                wxTypes[DailyClimateData.WX_SAND_STORM_INDEX] == 1);
                        checkboxBLSN.setSelection(
                                wxTypes[DailyClimateData.WX_BLOWING_SNOW_INDEX] == 1);
                        checkboxPlusFC.setSelection(
                                wxTypes[DailyClimateData.WX_FUNNEL_CLOUD_INDEX] == 1);

                    } catch (ArrayIndexOutOfBoundsException e) {
                        String message = "Weather Types array for Daily Climate Data is not of expected length."
                                + "\nWeather Types checkboxes may not reflect accurate status."
                                + "\nSelect a different station or date.";
                        logger.error(message, e);
                        MessageDialog.openError(getShell(),
                                "Unexpected Weather Types Length", message);
                        throw new VizException(e);
                    }

                }
            } catch (VizException e) {
                String message = "Could not retrieve data for Climate Daily Display dialog, with station ID ["
                        + newStation.getInformId() + "] and date ["
                        + this.qcDialog.dailyDateSelector.getDate()
                                .toFullDateString()
                        + "]. Select a different station.";
                logger.error(message, e);
                MessageDialog.openError(getShell(), "Data Retrieval Error",
                        message);
                this.qcDialog.dailyData = null;
            }

            this.qcDialog.getChangeListener().setIgnoreChanges(false);
            this.qcDialog.getChangeListener().setChangesUnsaved(false);
        }

    }

    @Override
    public void saveData() {

        DailyClimateData data = this.qcDialog.dailyData;
        DailyDataMethod dataMethods = this.qcDialog.dailyData.getDataMethods();

        recordCheck(data.getMaxTemp(), maxTempField.getText(),
                this.qcDialog.dailyRecords.getMaxTempRecord(), true);
        saveValue(data.getMaxTemp(), maxTempField.getText(), data::setMaxTemp,
                dataMethods::setMaxTempQc, ValueChangedFlag.MAX_TEMP_FLAG);

        saveValue(data.getMaxTempTime(), timeMaxTempField.getText(),
                data::setMaxTempTime);

        recordCheck(data.getMinTemp(), minTempField.getText(),
                this.qcDialog.dailyRecords.getMinTempRecord(), false);
        saveValue(data.getMinTemp(), minTempField.getText(), data::setMinTemp,
                dataMethods::setMinTempQc, ValueChangedFlag.MIN_TEMP_FLAG);

        saveValue(data.getMinTempTime(), timeMinTempField.getText(),
                data::setMinTempTime);

        saveValue(data.getMaxRelHumid(), maxHumdField.getText(),
                data::setMaxRelHumid, ValueChangedFlag.RH_FLAG);

        saveValue(data.getMaxRelHumidHour(), timeMaxHumdField.getText(),
                data::setMaxRelHumidHour);

        saveValue(data.getMinRelHumid(), minHumdField.getText(),
                data::setMinRelHumid, ValueChangedFlag.RH_FLAG);

        saveValue(data.getMinRelHumidHour(), timeMinHumdField.getText(),
                data::setMinRelHumidHour);

        saveValue(data.getMaxWind(), maxWindDirField.getText(),
                maxWindSpeedField.getText(), data::setMaxWind,
                dataMethods::setMaxWindQc, ValueChangedFlag.WIND_FLAG);

        saveValue(data.getMaxWindTime(), timeMaxWindField.getText(),
                data::setMaxWindTime);

        saveValue(data.getMaxGust(), maxWindGustDirField.getText(),
                maxWindGustSpeedField.getText(), data::setMaxGust,
                dataMethods::setMaxGustQc, ValueChangedFlag.GUST_FLAG);

        saveValue(data.getMaxGustTime(), timeMaxWindGustField.getText(),
                data::setMaxGustTime);

        saveValue(data.getResultWind(), resWindDirField.getText(),
                resWindSpeedField.getText(), data::setResultWind);

        saveValue(data.getAvgWindSpeed(), avgWindField.getText(),
                data::setAvgWindSpeed, dataMethods::setAvgWindQc);

        recordCheck(data.getPrecip(), dailyPrecField.getText(),
                this.qcDialog.dailyRecords.getPrecipDayRecord());
        saveValue(data.getPrecip(), dailyPrecField.getText(), data::setPrecip,
                dataMethods::setPrecipQc, ValueChangedFlag.PRECIP_FLAG);

        recordCheck(data.getSnowDay(), dailySnowField.getText(),
                this.qcDialog.dailyRecords.getSnowDayRecord());
        saveValue(data.getSnowDay(), dailySnowField.getText(), data::setSnowDay,
                dataMethods::setSnowQc, ValueChangedFlag.SNOW_FLAG);

        saveValue(data.getMinutesSun(), minutesSunField.getText(),
                data::setMinutesSun, dataMethods::setMinSunQc,
                ValueChangedFlag.SUN_FLAG);

        saveValue(data.getPercentPossSun(), possibleSunField.getText(),
                data::setPercentPossSun, dataMethods::setPossSunQc,
                ValueChangedFlag.SUN_FLAG);

        saveValue(data.getSkyCover(), avgSkyCoverField.getText(),
                data::setSkyCover, dataMethods::setSkyCoverQc,
                ValueChangedFlag.SKY_FLAG);

        saveValue(data.getSnowGround(), snowDepthField.getText(),
                data::setSnowGround, dataMethods::setDepthQc,
                ValueChangedFlag.DEPTH_FLAG);

        saveValue(data.getMaxSlp(), maxPressureField.getText(),
                data::setMaxSlp);

        saveValue(data.getMinSlp(), minPressureField.getText(),
                data::setMinSlp);

        // copy of wxtype array before it's altered
        int[] wxTypeBefore = Arrays.copyOf(this.qcDialog.dailyData.getWxType(),
                this.qcDialog.dailyData.getWxType().length);

        data.setWxType(DailyClimateData.WX_THUNDER_STORM_INDEX,
                checkboxTS.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_LIGHT_FREEZING_RAIN_INDEX,
                checkboxMinusFRZA.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_FOG_INDEX,
                checkboxFG.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_MIXED_PRECIP_INDEX,
                checkboxRASN.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_HAIL_INDEX,
                checkboxGR.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_FOG_QUARTER_SM_INDEX,
                checkboxFG14.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_HEAVY_RAIN_INDEX,
                checkboxPlusRA.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_HEAVY_SNOW_INDEX,
                checkboxPlusSN.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_HAZE_INDEX,
                checkboxHZ.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_RAIN_INDEX,
                checkboxRA.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_SNOW_INDEX,
                checkboxSN.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_BLOWING_SNOW_INDEX,
                checkboxBLSN.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_LIGHT_RAIN_INDEX,
                checkboxMinusRA.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_LIGHT_SNOW_INDEX,
                checkboxMinusSN.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_SAND_STORM_INDEX,
                checkboxSS.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_FREEZING_RAIN_INDEX,
                checkboxFZRA.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_ICE_PELLETS_INDEX,
                checkboxPL.getSelection() ? 1 : 0);
        data.setWxType(DailyClimateData.WX_FUNNEL_CLOUD_INDEX,
                checkboxPlusFC.getSelection() ? 1 : 0);

        int numWx = 0;
        for (int ii = 0; ii < data.getWxType().length; ii++) {
            if (data.getWxType()[ii] == 1) {
                numWx++;
            }
        }
        data.setNumWx(numWx);

        // If any of the checkboxes were changed set the data method for
        // weather to manual entry, and add changed flag.
        if (!Arrays.equals(data.getWxType(), wxTypeBefore)) {
            valueChangedFlags.add(ValueChangedFlag.WX_FLAG);
            dataMethods.setWeatherQc(QCValues.MANUAL_ENTRY);
        }

        // Recalculate cooling and heating degree days. Logic taken from
        // legacy code.
        if (data.getMaxTemp() != ParameterFormatClimate.MISSING
                && data.getMinTemp() != ParameterFormatClimate.MISSING) {
            float maxTemp = (float) data.getMaxTemp();
            float minTemp = (float) data.getMinTemp();
            float avgTemp = (maxTemp + minTemp) / 2.0f;

            if (avgTemp > ClimateUtilities.AVG_TEMP_COOL) {
                data.setNumHeat(0);
                data.setNumCool(ClimateUtilities.calcCoolDays(avgTemp));
            } else if (avgTemp < ClimateUtilities.AVG_TEMP_HEAT) {
                data.setNumHeat(ClimateUtilities.calcHeatDays(avgTemp));
                data.setNumCool(0);
            } else {
                data.setNumHeat(0);
                data.setNumCool(0);
            }

        } else {
            data.setNumHeat(ParameterFormatClimate.MISSING_DEGREE_DAY);
            data.setNumCool(ParameterFormatClimate.MISSING_DEGREE_DAY);
        }

        // save data methods in DailyClimateData dataMethods field
        data.setDataMethods(dataMethods);

        try {
            DailyClimateServiceUpdateRequest request = new DailyClimateServiceUpdateRequest(
                    data.getInformId(), this.qcDialog.dailyDate, data);
            ThriftClient.sendRequest(request);

            this.qcDialog.getChangeListener().setChangesUnsaved(false);

            // check/update records
            if (recordFlag) {
                if (MessageDialog.openQuestion(getShell(), "Update Records",
                        "New value(s) might set or tie record(s) in historical database. "
                                + "\nIs it OK to update record(s)?")) {
                    CompareUpdateDailyRecordsRequest recordRequest = new CompareUpdateDailyRecordsRequest(
                            data.getInformId(), this.qcDialog.dailyDate,
                            data.getMaxTemp(), data.getMinTemp(),
                            data.getPrecip(), data.getSnowDay());

                    try {
                        ThriftClient.sendRequest(recordRequest);
                    } catch (VizException e) {
                        logger.error(
                                "Could not update daily records for station ID "
                                        + data.getInformId(),
                                e);
                    }
                }
            }
            // Check freeze dates and update if necessary
            if (valueChangedFlags.contains(ValueChangedFlag.MIN_TEMP_FLAG)) {

                FetchFreezeDatesRequest freezeRequest = new FetchFreezeDatesRequest(
                        1, data.getInformId());
                PeriodClimo freezeDates = (PeriodClimo) ThriftClient
                        .sendRequest(freezeRequest);

                // Only redetermine freeze dates if data is changed for
                // current freeze season
                if ((this.qcDialog.dailyDate.getYear() == freezeDates
                        .getEarlyFreezeNorm().getYear()
                        && this.qcDialog.dailyDate.getMon() > 6)
                        || (this.qcDialog.dailyDate.getYear() == freezeDates
                                .getLateFreezeNorm().getYear()
                                && this.qcDialog.dailyDate.getMon() < 7)) {
                    // Clears the dates if they already exist
                    UpdateFreezeDBRequest clearFreezeRequest = new UpdateFreezeDBRequest(
                            5, data.getInformId(),
                            ClimateDates.getMissingClimateDates());

                    ClimateDates dummyDates = (ClimateDates) ThriftClient
                            .sendRequest(clearFreezeRequest);
                    dummyDates.setStart(new ClimateDate(1, 7,
                            dummyDates.getStart().getYear()));
                    dummyDates.setEnd(new ClimateDate(30, 6,
                            dummyDates.getEnd().getYear()));

                    if (this.qcDialog.dailyDate.getMon() > 6) {
                        dummyDates.setStart(new ClimateDate(1, 7,
                                this.qcDialog.dailyDate.getYear()));
                        dummyDates.setEnd(new ClimateDate(30, 6,
                                this.qcDialog.dailyDate.getYear() + 1));
                    } else {
                        dummyDates.setStart(new ClimateDate(1, 7,
                                this.qcDialog.dailyDate.getYear() - 1));
                        dummyDates.setEnd(new ClimateDate(30, 6,
                                this.qcDialog.dailyDate.getYear()));
                    }

                    try {
                        DetFreezeDatesRequest detFreezeRequest = new DetFreezeDatesRequest(
                                data.getInformId(), dummyDates);
                        ClimateDates firstLastFreezeDates = (ClimateDates) ThriftClient
                                .sendRequest(detFreezeRequest);

                        UpdateFreezeDBRequest updateFreezeRequest = new UpdateFreezeDBRequest(
                                1, data.getInformId(), firstLastFreezeDates);
                        ThriftClient.sendRequest(updateFreezeRequest);
                    } catch (VizException e) {
                        logger.error(
                                "Could not update freeze dates for station ID "
                                        + data.getInformId(),
                                e);
                    }
                }
            }

            // reload fields after updating
            loadData();
            refreshDatabase();
            valueChangedFlags.clear();
            recordFlag = false;
        } catch (VizException e) {
            logger.error("Could not save values for station ID "
                    + this.qcDialog.dailyData.getInformId(), e);
        }

    }

    /**
     * Update monthly/season/annual entries according to a changed daily entry.
     */
    private void refreshDatabase() {
        ClimateDates dates;
        String periodName;
        PeriodType periodType;

        dates = new ClimateDates(this.qcDialog.dailyDate.getMon(),
                this.qcDialog.dailyDate.getYear());
        periodName = QCDialog.MONTHLY_SELECTION.toLowerCase();
        periodType = PeriodType.MONTHLY_RAD;

        refreshDatabaseSupport(dates, periodName, periodType);

        try {
            dates = new ClimateDates(
                    SeasonType.getSeasonTypeFromMonth(
                            this.qcDialog.dailyDate.getMon()),
                    this.qcDialog.dailyDate.getYear());
        } catch (ClimateInvalidParameterException e) {
            logger.error("Error: " + e.getMessage(), e);
            dates = ClimateDates.getMissingClimateDates();
        }
        periodName = QCDialog.SEASONAL_SELECTION.toLowerCase();
        periodType = PeriodType.SEASONAL_RAD;

        refreshDatabaseSupport(dates, periodName, periodType);

        dates = new ClimateDates(this.qcDialog.dailyDate.getYear());
        periodName = QCDialog.ANNUAL_SELECTION.toLowerCase();
        periodType = PeriodType.ANNUAL_RAD;

        refreshDatabaseSupport(dates, periodName, periodType);
    }

    /**
     * Update period entries according to a changed daily entry.
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
                        this.qcDialog.climateGlobals,
                        /* send OTHER if type here is monthly */
                        periodType.equals(PeriodType.MONTHLY_RAD)
                                ? PeriodType.OTHER : periodType);

                try {
                    newData = (PeriodData) ThriftClient
                            .sendRequest(buildRequest);

                    /*
                     * Logic migrated from legacy code. Update each period field
                     * with build data only if the related daily field was
                     * changed AND the data method value is greater than 2 (i.e.
                     * neither MANUAL_ENTRY, VALUE_FROM_MSM nor
                     * VALUE_FROM_DAILY) in some cases.
                     */

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.MAX_TEMP_FLAG)) {
                        if (tempData.getDataMethods()
                                .getMaxTempQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setMaxTemp(newData.getMaxTemp());
                            tempData.setDayMaxTempList(
                                    newData.getDayMaxTempList());

                        }
                        if (tempData.getDataMethods()
                                .getAvgMaxTempQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setMaxTempMean(newData.getMaxTempMean());
                        }
                        if (tempData.getDataMethods()
                                .getMaxTempGE90Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumMaxGreaterThan90F(
                                    newData.getNumMaxGreaterThan90F());
                        }
                        if (tempData.getDataMethods()
                                .getMaxTempLE32Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumMaxLessThan32F(
                                    newData.getNumMaxLessThan32F());
                        }

                        tempData.setNumMaxGreaterThanT1F(
                                newData.getNumMaxGreaterThanT1F());
                        tempData.setNumMaxGreaterThanT2F(
                                newData.getNumMaxGreaterThanT2F());
                        tempData.setNumMaxLessThanT3F(
                                newData.getNumMaxLessThanT3F());

                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.MIN_TEMP_FLAG)) {
                        if (tempData.getDataMethods()
                                .getMinTempQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setMinTemp(newData.getMinTemp());
                            tempData.setDayMinTempList(
                                    newData.getDayMinTempList());

                        }
                        if (tempData.getDataMethods()
                                .getAvgMinTempQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setMinTempMean(newData.getMinTempMean());
                        }
                        if (tempData.getDataMethods()
                                .getMinLE0Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumMinLessThan0F(
                                    newData.getNumMinLessThan0F());
                        }
                        if (tempData.getDataMethods()
                                .getMinLE32Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumMinLessThan32F(
                                    newData.getNumMinLessThan32F());
                        }

                        tempData.setNumMinGreaterThanT4F(
                                newData.getNumMinGreaterThanT4F());
                        tempData.setNumMinLessThanT5F(
                                newData.getNumMinLessThanT5F());
                        tempData.setNumMinLessThanT6F(
                                newData.getNumMinLessThanT6F());

                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.MAX_TEMP_FLAG)
                            || valueChangedFlags
                                    .contains(ValueChangedFlag.MIN_TEMP_FLAG)) {
                        if (tempData.getDataMethods()
                                .getMeanTempQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setMeanTemp(newData.getMeanTemp());
                        }
                        if (tempData.getDataMethods()
                                .getHeatQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumHeatTotal(newData.getNumHeatTotal());
                        }
                        if (tempData.getDataMethods()
                                .getCoolQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumCoolTotal(newData.getNumCoolTotal());
                        }
                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.PRECIP_FLAG)) {
                        if (tempData.getDataMethods()
                                .getPrecipQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setPrecipMeanDay(
                                    newData.getPrecipMeanDay());
                            tempData.setPrecipTotal(newData.getPrecipTotal());
                        }
                        if (tempData.getDataMethods()
                                .getPrecipGE01Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumPrcpGreaterThan01(
                                    newData.getNumPrcpGreaterThan01());
                        }
                        if (tempData.getDataMethods()
                                .getPrecipGE10Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumPrcpGreaterThan10(
                                    newData.getNumPrcpGreaterThan10());
                        }
                        if (tempData.getDataMethods()
                                .getPrecipGE50Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumPrcpGreaterThan50(
                                    newData.getNumPrcpGreaterThan50());
                        }
                        if (tempData.getDataMethods()
                                .getPrecipGE100Qc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumPrcpGreaterThan100(
                                    newData.getNumPrcpGreaterThan100());
                        }

                        tempData.setNumPrcpGreaterThanP1(
                                newData.getNumPrcpGreaterThanP1());
                        tempData.setNumPrcpGreaterThanP1(
                                newData.getNumPrcpGreaterThanP2());
                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.SNOW_FLAG)) {
                        tempData.setSnowTotal(newData.getSnowTotal());
                        tempData.setSnowWater(newData.getSnowWater());
                        tempData.setNumSnowGreaterThanTR(
                                newData.getNumSnowGreaterThanTR());
                        tempData.setNumSnowGreaterThan1(
                                newData.getNumSnowGreaterThan1());
                        tempData.setNumSnowGreaterThanS1(
                                newData.getNumSnowGreaterThanS1());
                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.DEPTH_FLAG)) {
                        tempData.setSnowGroundMax(newData.getSnowGroundMax());
                        tempData.setSnowGroundMaxDateList(
                                newData.getSnowGroundMaxDateList());
                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.WIND_FLAG)) {
                        tempData.setMaxWindList(newData.getMaxWindList());
                        tempData.setMaxWindDayList(newData.getMaxWindDayList());
                    }

                    if (valueChangedFlags
                            .contains(ValueChangedFlag.GUST_FLAG)) {
                        tempData.setMaxGustList(newData.getMaxGustList());
                        tempData.setMaxGustDayList(newData.getMaxGustDayList());
                    }

                    if (valueChangedFlags.contains(ValueChangedFlag.SUN_FLAG)) {
                        if (tempData.getDataMethods()
                                .getPossSunQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setPossSun(newData.getPossSun());
                        }
                    }

                    if (valueChangedFlags.contains(ValueChangedFlag.SKY_FLAG)) {
                        tempData.setMeanSkyCover(newData.getMeanSkyCover());

                        if (tempData.getDataMethods()
                                .getFairDaysQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumFairDays(newData.getNumFairDays());
                        }
                        if (tempData.getDataMethods()
                                .getPcDaysQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumPartlyCloudyDays(
                                    newData.getNumPartlyCloudyDays());
                        }
                        if (tempData.getDataMethods()
                                .getCloudyDaysQc() > QCValues.VALUE_FROM_DAILY) {
                            tempData.setNumMostlyCloudyDays(
                                    newData.getNumMostlyCloudyDays());
                        }
                    }

                    if (valueChangedFlags.contains(ValueChangedFlag.RH_FLAG)) {
                        tempData.setMeanRh(newData.getMeanRh());
                    }

                    if (valueChangedFlags.contains(ValueChangedFlag.WX_FLAG)) {
                        tempData.setNumThunderStorms(
                                newData.getNumThunderStorms());
                        tempData.setNumMixedPrecip(newData.getNumMixedPrecip());
                        tempData.setNumHeavyRain(newData.getNumHeavyRain());
                        tempData.setNumRain(newData.getNumRain());
                        tempData.setNumLightRain(newData.getNumLightRain());
                        tempData.setNumFreezingRain(
                                newData.getNumFreezingRain());
                        tempData.setNumLightFreezingRain(
                                newData.getNumLightFreezingRain());
                        tempData.setNumHail(newData.getNumHail());
                        tempData.setNumHeavySnow(newData.getNumHeavySnow());
                        tempData.setNumSnow(newData.getNumSnow());
                        tempData.setNumLightSnow(newData.getNumLightSnow());
                        tempData.setNumIcePellets(newData.getNumIcePellets());
                        tempData.setNumFog(newData.getNumFog());
                        tempData.setNumFogQuarterSM(
                                newData.getNumFogQuarterSM());
                        tempData.setNumHaze(newData.getNumHaze());

                    }

                    // update database
                    PeriodClimateServiceUpdateRequest updateRequest = new PeriodClimateServiceUpdateRequest(
                            tempData.getInformId(), dates, periodType,
                            tempData);
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

}