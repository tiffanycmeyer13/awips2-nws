/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.TemperatureControlFlags;

/**
 * Composite to contain all sub-categories for climate temperature in setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * FEB 13, 2017 20640      jwu          Adjust GUI for preference values.
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * 13 MAR 2018  44624      amoore       Address discovered ATAN bug where Last Year's
 *                                      "Date/Time" is not selectable for NWWS.
 * 06 NOV 2018  55207      jwu         Enable some legacy behavior(DR 20889).
 * </pre>
 * 
 * @author jwu
 */
public class TemperatureComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Buttons for maximum temperature
    private Button maxMeasuredBtn;

    private Button maxTimeOfMeasuredBtn;

    protected Button maxNormBtn;

    protected Button maxRecordBtn;

    protected Button maxRecordYearBtn;

    protected Button maxDepartureBtn;

    private Button maxLastYearBtn;

    private Button maxDateOfLastBtn;

    // Buttons for minimum temperature
    private Button minMeasuredBtn;

    private Button minTimeOfMeasuredBtn;

    protected Button minNormBtn;

    protected Button minRecordBtn;

    protected Button minRecordYearBtn;

    protected Button minDepartureBtn;

    private Button minLastYearBtn;

    private Button minDateOfLastBtn;

    // Buttons for mean temperature
    private Button meanMeasuredBtn;

    protected Button meanDepartureBtn;

    protected Button meanNormBtn;

    private Button meanLastYearBtn;

    // Buttons for average daily maximum temperature
    private Composite meanMaxComp;

    private Button meanMaxMeasuredBtn;

    private Button meanMaxNormBtn;

    private Button meanMaxDepartureBtn;

    private Button meanMaxLastYearBtn;

    // Buttons for average daily minimum temperature
    private Composite meanMinComp;

    private Button meanMinMeasuredBtn;

    private Button meanMinNormBtn;

    private Button meanMinDepartureBtn;

    private Button meanMinLastYearBtn;

    // Buttons for days with maximum temperature >= 90F
    private Composite maxGE90Comp;

    private Button maxGE90MeasuredBtn;

    private Button maxGE90DepartureBtn;

    private Button maxGE90NormBtn;

    private Button maxGE90LastYearBtn;

    // Buttons for days with maximum temperature <= 32F
    private Composite maxLE32Comp;

    private Button maxLE32MeasuredBtn;

    private Button maxLE32DepartureBtn;

    private Button maxLE32NormBtn;

    private Button maxLE32LastYearBtn;

    // Buttons for days with maximum temperature >= T1
    private Composite maxGET1Comp;

    private Button maxGET1MeasuredBtn;

    private Button maxGET1LastYearBtn;

    // Buttons for days with maximum temperature >= T2
    private Composite maxGET2Comp;

    private Button maxGET2MeasuredBtn;

    private Button maxGET2LastYearBtn;

    // Buttons for days with maximum temperature <= T3
    private Composite maxLET3Comp;

    private Button maxLET3MeasuredBtn;

    private Button maxLET3LastYearBtn;

    // Buttons for days with minimum temperature LE <= 32F
    private Composite minLE32Comp;

    private Button minLE32MeasuredBtn;

    private Button minLE32DepartureBtn;

    private Button minLE32NormBtn;

    private Button minLE32LastYearBtn;

    // Buttons for days with minimum temperature LE <= 0F
    private Composite minLE0Comp;

    private Button minLE0MeasuredBtn;

    private Button minLE0DepartureBtn;

    private Button minLE0NormBtn;

    private Button minLE0LastYearBtn;

    // Buttons for days with minimum temperature GE <= T4
    private Composite minGET4Comp;

    private Button minGET4MeasuredBtn;

    private Button minGET4LastYearBtn;

    // Buttons for days with minimum temperature LE <= T5
    private Composite minLET5Comp;

    private Button minLET5MeasuredBtn;

    private Button minLET5LastYearBtn;

    // Buttons for days with minimum temperature LE <= T6
    private Composite minLET6Comp;

    private Button minLET6MeasuredBtn;

    private Button minLET6LastYearBtn;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent composite
     * @param style
     *            composite style
     * @param customThresholds
     *            User-defined threshold values
     * @param majorFont
     *            Font to be used for major check buttons
     */
    public TemperatureComp(Composite parent, int style,
            ClimateGlobal preferenceValues, Font majorFont) {

        super(parent, style, preferenceValues, majorFont);

    }

    /**
     * Create/initialize components.
     */
    protected void initializeComponents() {
        /*
         * Layout
         */
        RowLayout subcatCompLayout = new RowLayout(SWT.HORIZONTAL);
        subcatCompLayout.marginTop = 1;
        subcatCompLayout.spacing = 15;
        subcatCompLayout.justify = true;

        this.setLayout(subcatCompLayout);

        // Layouts for components.
        mainCompLayout = new RowLayout(SWT.VERTICAL);

        majorCompLayout = new RowLayout(SWT.VERTICAL);

        minorCompLayout = new RowLayout(SWT.VERTICAL);

        mainCompLayout.spacing = 0;
        mainCompLayout.marginWidth = 3;

        majorCompLayout.spacing = 0;
        majorCompLayout.marginLeft = 5;

        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 10;

        // Maximum composite at left
        createMaximumComp(this);

        // Minimum composite at center
        createMinimumComp(this);

        // Minimum composite at right
        createMeanTempComp(this);

    }

    /**
     * Create a composite for all maximum temperature elements.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createMaximumComp(Composite parent) {

        Composite maximumComp;
        maximumComp = new Composite(parent, SWT.NONE);
        maximumComp.setLayout(mainCompLayout);

        // maximum temperature composite
        Composite maxTempComp = new Composite(maximumComp, SWT.NONE);
        maxTempComp.setLayout(majorCompLayout);

        maxMeasuredBtn = new Button(maxTempComp, SWT.CHECK);
        maxMeasuredBtn.setText("Maximum");
        maxMeasuredBtn.setFont(majorFont);
        maxMeasuredBtn.setSelection(true);

        final Composite maxTempMinorComp = new Composite(maxTempComp, SWT.NONE);
        maxTempMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(maxMeasuredBtn, maxTempMinorComp);

        maxTimeOfMeasuredBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxTimeOfMeasuredBtn.setText("Time/Date");

        maxRecordBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxRecordBtn.setText("Record");
        maxRecordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Disable "-Date/Year" if "Record" is de-selected.
                updateButton(maxRecordYearBtn, maxRecordBtn.getSelection());
            }
        });

        maxRecordYearBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxRecordYearBtn.setText(" - Date/Year");
        maxRecordYearBtn.setEnabled(maxRecordBtn.getSelection());

        maxDepartureBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxDepartureBtn.setText("Departure");
        maxDepartureBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // Select "Normal" if "Departure is selected.
                    if (maxDepartureBtn.getSelection()) {
                        maxNormBtn.setSelection(true);
                    }
                }
            }
        });

        maxNormBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxNormBtn.setText("Normal");
        maxNormBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // De-select "Departure" if "Normal" is de-selected.
                    if (!maxNormBtn.getSelection()) {
                        maxDepartureBtn.setSelection(false);
                    }
                }
            }
        });

        maxLastYearBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxLastYearBtn.setText("Last Year's");
        maxLastYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Disable "-Time/Date" if "Last Year's" is de-selected.
                updateButton(maxDateOfLastBtn, maxLastYearBtn.getSelection());
            }
        });

        maxDateOfLastBtn = new Button(maxTempMinorComp, SWT.CHECK);
        maxDateOfLastBtn.setText(" - Time/Date");

        // Composite for maximum temperature >= 90F
        maxGE90Comp = new Composite(maximumComp, SWT.NONE);
        maxGE90Comp.setLayout(majorCompLayout);

        maxGE90MeasuredBtn = new Button(maxGE90Comp, SWT.CHECK);
        maxGE90MeasuredBtn.setText("Days Max GE 90F");
        maxGE90MeasuredBtn.setFont(majorFont);
        maxGE90MeasuredBtn.setSelection(true);

        final Composite maxGE90MinorComp = new Composite(maxGE90Comp, SWT.NONE);
        maxGE90MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(maxGE90MeasuredBtn, maxGE90MinorComp);

        maxGE90DepartureBtn = new Button(maxGE90MinorComp, SWT.CHECK);
        maxGE90DepartureBtn.setText("Departure");

        maxGE90NormBtn = new Button(maxGE90MinorComp, SWT.CHECK);
        maxGE90NormBtn.setText("Normal");

        maxGE90LastYearBtn = new Button(maxGE90MinorComp, SWT.CHECK);
        maxGE90LastYearBtn.setText("Last Year's");

        // Composite for maximum temperature <= 32F
        maxLE32Comp = new Composite(maximumComp, SWT.NONE);
        maxLE32Comp.setLayout(majorCompLayout);

        maxLE32MeasuredBtn = new Button(maxLE32Comp, SWT.CHECK);
        maxLE32MeasuredBtn.setText("Days Max LE 32F");
        maxLE32MeasuredBtn.setFont(majorFont);
        maxLE32MeasuredBtn.setSelection(true);

        final Composite maxLE32MinorComp = new Composite(maxLE32Comp, SWT.NONE);
        maxLE32MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(maxLE32MeasuredBtn, maxLE32MinorComp);

        maxLE32DepartureBtn = new Button(maxLE32MinorComp, SWT.CHECK);
        maxLE32DepartureBtn.setText("Departure");

        maxLE32NormBtn = new Button(maxLE32MinorComp, SWT.CHECK);
        maxLE32NormBtn.setText("Normal");

        maxLE32LastYearBtn = new Button(maxLE32MinorComp, SWT.CHECK);
        maxLE32LastYearBtn.setText("Last Year's");

        // Check if T1, T2 and T3 are defined
        String T1Str = DEF_THRESHOLD_STR;
        String T2Str = DEF_THRESHOLD_STR;
        String T3Str = DEF_THRESHOLD_STR;
        if (preferenceValues != null) {
            if (preferenceValues.getT1() != ParameterFormatClimate.MISSING) {
                T1Str = String.valueOf(preferenceValues.getT1());
            }

            if (preferenceValues.getT2() != ParameterFormatClimate.MISSING) {
                T2Str = String.valueOf(preferenceValues.getT2());
            }

            if (preferenceValues.getT3() != ParameterFormatClimate.MISSING) {
                T3Str = String.valueOf(preferenceValues.getT3());
            }
        }

        // Composite for days with max temperature >= T1 F
        maxGET1Comp = new Composite(maximumComp, SWT.NONE);
        maxGET1Comp.setLayout(majorCompLayout);

        maxGET1MeasuredBtn = new Button(maxGET1Comp, SWT.CHECK);
        maxGET1MeasuredBtn.setText("Days GE " + T1Str + " F");
        maxGET1MeasuredBtn.setFont(majorFont);
        maxGET1MeasuredBtn.setSelection(false);

        final Composite maxGET1MinorComp = new Composite(maxGET1Comp, SWT.NONE);
        maxGET1MinorComp.setLayout(minorCompLayout);
        maxGET1MinorComp.setVisible(maxGET1MeasuredBtn.getSelection());

        subCategoryMap.put(maxGET1MeasuredBtn, maxGET1MinorComp);

        maxGET1LastYearBtn = new Button(maxGET1MinorComp, SWT.CHECK);
        maxGET1LastYearBtn.setText("Last Year's");

        // Composite for days with max temperature >= T2 F
        maxGET2Comp = new Composite(maximumComp, SWT.NONE);
        maxGET2Comp.setLayout(majorCompLayout);

        maxGET2MeasuredBtn = new Button(maxGET2Comp, SWT.CHECK);
        maxGET2MeasuredBtn.setText("Days GE " + T2Str + " F");
        maxGET2MeasuredBtn.setFont(majorFont);
        maxGET2MeasuredBtn.setSelection(false);

        final Composite maxGET2MinorComp = new Composite(maxGET2Comp, SWT.NONE);
        maxGET2MinorComp.setLayout(minorCompLayout);
        maxGET2MinorComp.setVisible(maxGET2MeasuredBtn.getSelection());

        subCategoryMap.put(maxGET2MeasuredBtn, maxGET2MinorComp);

        maxGET2LastYearBtn = new Button(maxGET2MinorComp, SWT.CHECK);
        maxGET2LastYearBtn.setText("Last Year's");

        // Composite for days with max temperature <= T3 F
        maxLET3Comp = new Composite(maximumComp, SWT.NONE);
        maxLET3Comp.setLayout(majorCompLayout);

        maxLET3MeasuredBtn = new Button(maxLET3Comp, SWT.CHECK);
        maxLET3MeasuredBtn.setText("Days GE " + T3Str + " F");
        maxLET3MeasuredBtn.setFont(majorFont);
        maxLET3MeasuredBtn.setSelection(false);

        final Composite maxLET3MinorComp = new Composite(maxLET3Comp, SWT.NONE);
        maxLET3MinorComp.setLayout(minorCompLayout);
        maxLET3MinorComp.setVisible(maxLET3MeasuredBtn.getSelection());

        subCategoryMap.put(maxLET3MeasuredBtn, maxLET3MinorComp);

        maxLET3LastYearBtn = new Button(maxLET3MinorComp, SWT.CHECK);
        maxLET3LastYearBtn.setText("Last Year's");

        return maximumComp;
    }

    /**
     * Create a composite for all minimum temperature elements.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createMinimumComp(Composite parent) {

        Composite minimumComp;
        minimumComp = new Composite(parent, SWT.NONE);
        minimumComp.setLayout(mainCompLayout);

        // Minimum temperature composite
        Composite minTempComp = new Composite(minimumComp, SWT.NONE);
        minTempComp.setLayout(majorCompLayout);

        minMeasuredBtn = new Button(minTempComp, SWT.CHECK);
        minMeasuredBtn.setText("Minimum");
        minMeasuredBtn.setFont(majorFont);
        minMeasuredBtn.setSelection(true);

        final Composite minTempMinorComp = new Composite(minTempComp, SWT.NONE);
        minTempMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(minMeasuredBtn, minTempMinorComp);

        minTimeOfMeasuredBtn = new Button(minTempMinorComp, SWT.CHECK);
        minTimeOfMeasuredBtn.setText("Time/Date");

        minRecordBtn = new Button(minTempMinorComp, SWT.CHECK);
        minRecordBtn.setText("Record");
        minRecordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date/Year" if "Record" is de-selected.
                updateButton(minRecordYearBtn, minRecordBtn.getSelection());
            }
        });

        minRecordYearBtn = new Button(minTempMinorComp, SWT.CHECK);
        minRecordYearBtn.setText(" - Date/Year");
        minRecordYearBtn.setEnabled(minRecordBtn.getSelection());

        minDepartureBtn = new Button(minTempMinorComp, SWT.CHECK);
        minDepartureBtn.setText("Departure");
        minDepartureBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // Select "Normal" if "Departure is selected.
                    if (minDepartureBtn.getSelection()) {
                        minNormBtn.setSelection(true);
                    }
                }
            }
        });

        minNormBtn = new Button(minTempMinorComp, SWT.CHECK);
        minNormBtn.setText("Normal");
        minNormBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // De-select "Departure" if "Normal" is de-selected.
                    if (!minNormBtn.getSelection()) {
                        minDepartureBtn.setSelection(false);
                    }
                }
            }
        });

        minLastYearBtn = new Button(minTempMinorComp, SWT.CHECK);
        minLastYearBtn.setText("Last Year's");
        minLastYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Time/Date" if "Last Year's" is de-selected.
                updateButton(minDateOfLastBtn, minLastYearBtn.getSelection());
            }
        });

        minDateOfLastBtn = new Button(minTempMinorComp, SWT.CHECK);
        minDateOfLastBtn.setText(" - Time/Date");

        // Composite for minimum temperature <= 32F
        minLE32Comp = new Composite(minimumComp, SWT.NONE);
        minLE32Comp.setLayout(majorCompLayout);

        minLE32MeasuredBtn = new Button(minLE32Comp, SWT.CHECK);
        minLE32MeasuredBtn.setText("Days Min LE 32F");
        minLE32MeasuredBtn.setFont(majorFont);
        minLE32MeasuredBtn.setSelection(true);

        final Composite minLE32MinorComp = new Composite(minLE32Comp, SWT.NONE);
        minLE32MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(minLE32MeasuredBtn, minLE32MinorComp);

        minLE32DepartureBtn = new Button(minLE32MinorComp, SWT.CHECK);
        minLE32DepartureBtn.setText("Departure");

        minLE32NormBtn = new Button(minLE32MinorComp, SWT.CHECK);
        minLE32NormBtn.setText("Normal");

        minLE32LastYearBtn = new Button(minLE32MinorComp, SWT.CHECK);
        minLE32LastYearBtn.setText("Last Year's");

        // Composite for maximum temperature <= 0F
        minLE0Comp = new Composite(minimumComp, SWT.NONE);
        minLE0Comp.setLayout(majorCompLayout);

        minLE0MeasuredBtn = new Button(minLE0Comp, SWT.CHECK);
        minLE0MeasuredBtn.setText("Days Min LE 0F");
        minLE0MeasuredBtn.setFont(majorFont);
        minLE0MeasuredBtn.setSelection(true);

        final Composite minLE0MinorComp = new Composite(minLE0Comp, SWT.NONE);
        minLE0MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(minLE0MeasuredBtn, minLE0MinorComp);

        minLE0DepartureBtn = new Button(minLE0MinorComp, SWT.CHECK);
        minLE0DepartureBtn.setText("Departure");

        minLE0NormBtn = new Button(minLE0MinorComp, SWT.CHECK);
        minLE0NormBtn.setText("Normal");

        minLE0LastYearBtn = new Button(minLE0MinorComp, SWT.CHECK);
        minLE0LastYearBtn.setText("Last Year's");

        // Check if T4, T5 and T6 are defined
        String T4Str = DEF_THRESHOLD_STR;
        String T5Str = DEF_THRESHOLD_STR;
        String T6Str = DEF_THRESHOLD_STR;
        if (preferenceValues != null) {
            if (preferenceValues.getT4() != ParameterFormatClimate.MISSING) {
                T4Str = String.valueOf(preferenceValues.getT4());
            }
            if (preferenceValues.getT5() != ParameterFormatClimate.MISSING) {
                T5Str = String.valueOf(preferenceValues.getT5());
            }
            if (preferenceValues.getT6() != ParameterFormatClimate.MISSING) {
                T6Str = String.valueOf(preferenceValues.getT6());
            }
        }

        // Composite for days with min temperature >= T1 F
        minGET4Comp = new Composite(minimumComp, SWT.NONE);
        minGET4Comp.setLayout(majorCompLayout);

        minGET4MeasuredBtn = new Button(minGET4Comp, SWT.CHECK);
        minGET4MeasuredBtn.setText("Days GE " + T4Str + " F");
        minGET4MeasuredBtn.setFont(majorFont);
        minGET4MeasuredBtn.setSelection(false);

        final Composite minGET4MinorComp = new Composite(minGET4Comp, SWT.NONE);
        minGET4MinorComp.setLayout(minorCompLayout);
        minGET4MinorComp.setVisible(minGET4MeasuredBtn.getSelection());

        subCategoryMap.put(minGET4MeasuredBtn, minGET4MinorComp);

        minGET4LastYearBtn = new Button(minGET4MinorComp, SWT.CHECK);
        minGET4LastYearBtn.setText("Last Year's");

        // Composite for days with min temperature <= T5 F
        minLET5Comp = new Composite(minimumComp, SWT.NONE);
        minLET5Comp.setLayout(majorCompLayout);

        minLET5MeasuredBtn = new Button(minLET5Comp, SWT.CHECK);
        minLET5MeasuredBtn.setText("Days LE " + T5Str + " F");
        minLET5MeasuredBtn.setFont(majorFont);
        minLET5MeasuredBtn.setSelection(false);

        final Composite minLET5MinorComp = new Composite(minLET5Comp, SWT.NONE);
        minLET5MinorComp.setLayout(minorCompLayout);
        minLET5MinorComp.setVisible(minLET5MeasuredBtn.getSelection());

        subCategoryMap.put(minLET5MeasuredBtn, minLET5MinorComp);

        minLET5LastYearBtn = new Button(minLET5MinorComp, SWT.CHECK);
        minLET5LastYearBtn.setText("Last Year's");

        // Composite for days with min temperature <= T6 F
        minLET6Comp = new Composite(minimumComp, SWT.NONE);
        minLET6Comp.setLayout(majorCompLayout);

        minLET6MeasuredBtn = new Button(minLET6Comp, SWT.CHECK);
        minLET6MeasuredBtn.setText("Days LE " + T6Str + " F");
        minLET6MeasuredBtn.setFont(majorFont);
        minLET6MeasuredBtn.setSelection(false);

        final Composite minLET6MinorComp = new Composite(minLET6Comp, SWT.NONE);
        minLET6MinorComp.setLayout(minorCompLayout);
        minLET6MinorComp.setVisible(minLET6MeasuredBtn.getSelection());

        subCategoryMap.put(minLET6MeasuredBtn, minLET6MinorComp);

        minLET6LastYearBtn = new Button(minLET6MinorComp, SWT.CHECK);
        minLET6LastYearBtn.setText("Last Year's");

        return minimumComp;
    }

    /**
     * Create a composite for all mean temperature elements.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createMeanTempComp(Composite parent) {

        Composite meanComp;
        meanComp = new Composite(parent, SWT.NONE);
        meanComp.setLayout(mainCompLayout);

        // Minimum temperature composite
        Composite meanTempComp = new Composite(meanComp, SWT.NONE);
        RowLayout majorCompLayout = new RowLayout(SWT.VERTICAL);
        majorCompLayout.marginLeft = 5;
        meanTempComp.setLayout(majorCompLayout);

        meanMeasuredBtn = new Button(meanTempComp, SWT.CHECK);
        meanMeasuredBtn.setText("Mean");
        meanMeasuredBtn.setFont(majorFont);
        meanMeasuredBtn.setSelection(true);

        final Composite meanTempMinorComp = new Composite(meanTempComp,
                SWT.NONE);
        RowLayout minorCompLayout = new RowLayout(SWT.VERTICAL);
        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 10;
        meanTempMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(meanMeasuredBtn, meanTempMinorComp);

        meanDepartureBtn = new Button(meanTempMinorComp, SWT.CHECK);
        meanDepartureBtn.setText("Departure");
        meanDepartureBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // Select "Normal" if "Departure is selected.
                    if (meanDepartureBtn.getSelection()) {
                        meanNormBtn.setSelection(true);
                    }
                }
            }
        });

        meanNormBtn = new Button(meanTempMinorComp, SWT.CHECK);
        meanNormBtn.setText("Normal");
        meanNormBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // De-select "Departure" if "Normal" is de-selected.
                    if (!meanNormBtn.getSelection()) {
                        meanDepartureBtn.setSelection(false);
                    }
                }
            }
        });

        meanLastYearBtn = new Button(meanTempMinorComp, SWT.CHECK);
        meanLastYearBtn.setText("Last Year's");

        // Composite for average daily maximum temperature
        meanMaxComp = new Composite(meanComp, SWT.NONE);
        meanMaxComp.setLayout(majorCompLayout);

        meanMaxMeasuredBtn = new Button(meanMaxComp, SWT.CHECK);
        meanMaxMeasuredBtn.setText("Avg. Daily Max");
        meanMaxMeasuredBtn.setFont(majorFont);
        meanMaxMeasuredBtn.setSelection(true);

        final Composite meanMaxMinorComp = new Composite(meanMaxComp, SWT.NONE);
        meanMaxMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(meanMaxMeasuredBtn, meanMaxMinorComp);

        meanMaxDepartureBtn = new Button(meanMaxMinorComp, SWT.CHECK);
        meanMaxDepartureBtn.setText("Departure");

        meanMaxNormBtn = new Button(meanMaxMinorComp, SWT.CHECK);
        meanMaxNormBtn.setText("Normal");

        meanMaxLastYearBtn = new Button(meanMaxMinorComp, SWT.CHECK);
        meanMaxLastYearBtn.setText("Last Year's");

        // Composite for average minimum temperature
        meanMinComp = new Composite(meanComp, SWT.NONE);
        meanMinComp.setLayout(majorCompLayout);

        meanMinMeasuredBtn = new Button(meanMinComp, SWT.CHECK);
        meanMinMeasuredBtn.setText("Avg. Daily Min");
        meanMinMeasuredBtn.setFont(majorFont);
        meanMinMeasuredBtn.setSelection(true);

        final Composite meanMinMinorComp = new Composite(meanMinComp, SWT.NONE);
        meanMinMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(meanMinMeasuredBtn, meanMinMinorComp);

        meanMinDepartureBtn = new Button(meanMinMinorComp, SWT.CHECK);
        meanMinDepartureBtn.setText("Departure");

        meanMinNormBtn = new Button(meanMinMinorComp, SWT.CHECK);
        meanMinNormBtn.setText("Normal");

        meanMinLastYearBtn = new Button(meanMinMinorComp, SWT.CHECK);
        meanMinLastYearBtn.setText("Last Year's");

        return meanComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {

        updateButton(maxLastYearBtn, !isNWR);
        updateButton(minLastYearBtn, !isNWR);
        updateButton(meanLastYearBtn, !isNWR);
        updateButton(meanMaxLastYearBtn, !isNWR);
        updateButton(meanMinLastYearBtn, !isNWR);
        updateButton(maxGE90LastYearBtn, !isNWR);
        updateButton(maxLE32LastYearBtn, !isNWR);
        updateButton(maxGET1LastYearBtn, !isNWR);
        updateButton(maxGET2LastYearBtn, !isNWR);
        updateButton(maxLET3LastYearBtn, !isNWR);
        updateButton(minLE32LastYearBtn, !isNWR);
        updateButton(minLE0LastYearBtn, !isNWR);
        updateButton(minGET4LastYearBtn, !isNWR);
        updateButton(minLET5LastYearBtn, !isNWR);
        updateButton(minLET6LastYearBtn, !isNWR);

        updateButton(maxDateOfLastBtn, !isNWR);
        updateButton(minDateOfLastBtn, !isNWR);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        maxGE90Comp.setVisible(!isDaily);
        maxLE32Comp.setVisible(!isDaily);
        maxGET1Comp.setVisible(!isDaily);
        maxGET2Comp.setVisible(!isDaily);
        maxLET3Comp.setVisible(!isDaily);

        minLE32Comp.setVisible(!isDaily);
        minLE0Comp.setVisible(!isDaily);
        minGET4Comp.setVisible(!isDaily);
        minLET5Comp.setVisible(!isDaily);
        minLET6Comp.setVisible(!isDaily);

        meanMaxComp.setVisible(!isDaily);
        meanMinComp.setVisible(!isDaily);

        updatePreferences(preferenceValues);

    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - TemperatureControlFlags
     */
    public TemperatureControlFlags getControlFlags() {
        TemperatureControlFlags flags = TemperatureControlFlags
                .getDefaultFlags();

        // Maximum temperature
        flags.getMaxTemp().setMeasured(maxMeasuredBtn.getSelection());
        flags.getMaxTemp()
                .setTimeOfMeasured(maxTimeOfMeasuredBtn.getSelection());
        flags.getMaxTemp().setRecord(maxRecordBtn.getSelection());
        flags.getMaxTemp().setRecordYear(maxRecordYearBtn.getSelection());
        flags.getMaxTemp().setDeparture(maxDepartureBtn.getSelection());
        flags.getMaxTemp().setNorm(maxNormBtn.getSelection());
        if (!isNWR) {
            flags.getMaxTemp().setLastYear(maxLastYearBtn.getSelection());
            if (maxLastYearBtn.getSelection()) {
                flags.getMaxTemp()
                        .setDateOfLast(maxDateOfLastBtn.getSelection());
            }
        }
        // Minimum temperature
        flags.getMinTemp().setMeasured(minMeasuredBtn.getSelection());
        flags.getMinTemp()
                .setTimeOfMeasured(minTimeOfMeasuredBtn.getSelection());
        flags.getMinTemp().setRecord(minRecordBtn.getSelection());
        flags.getMinTemp().setRecordYear(minRecordYearBtn.getSelection());
        flags.getMinTemp().setDeparture(minDepartureBtn.getSelection());
        flags.getMinTemp().setNorm(minNormBtn.getSelection());
        if (!isNWR) {
            flags.getMinTemp().setLastYear(minLastYearBtn.getSelection());
            if (minLastYearBtn.getSelection()) {
                flags.getMinTemp()
                        .setDateOfLast(minDateOfLastBtn.getSelection());
            }
        }

        // Mean temperature
        flags.getMeanTemp().setMeasured(meanMeasuredBtn.getSelection());
        flags.getMeanTemp().setDeparture(meanDepartureBtn.getSelection());
        flags.getMeanTemp().setNorm(meanNormBtn.getSelection());
        if (!isNWR) {
            flags.getMeanTemp().setLastYear(meanLastYearBtn.getSelection());
        }

        // Update flags for period type
        if (!isDaily) {
            // Mean maximum temperature
            flags.getMeanMaxTemp()
                    .setMeasured(meanMaxMeasuredBtn.getSelection());
            flags.getMeanMaxTemp()
                    .setDeparture(meanMaxDepartureBtn.getSelection());
            flags.getMeanMaxTemp().setNorm(meanMaxNormBtn.getSelection());
            if (!isNWR) {
                flags.getMeanMaxTemp()
                        .setLastYear(meanMaxLastYearBtn.getSelection());
            }

            // Mean minimum temperature
            flags.getMeanMinTemp()
                    .setMeasured(meanMinMeasuredBtn.getSelection());
            flags.getMeanMinTemp()
                    .setDeparture(meanMinDepartureBtn.getSelection());
            flags.getMeanMinTemp().setNorm(meanMinNormBtn.getSelection());
            if (!isNWR) {
                flags.getMeanMinTemp()
                        .setLastYear(meanMinLastYearBtn.getSelection());
            }

            // Days with maximum temperature >= 90F
            flags.getMaxTempGE90()
                    .setMeasured(maxGE90MeasuredBtn.getSelection());
            flags.getMaxTempGE90()
                    .setDeparture(maxGE90DepartureBtn.getSelection());
            flags.getMaxTempGE90().setNorm(maxGE90NormBtn.getSelection());
            if (!isNWR) {
                flags.getMaxTempGE90()
                        .setLastYear(maxGE90LastYearBtn.getSelection());
            }

            // Days with maximum temperature <= 32F
            flags.getMaxTempLE32()
                    .setMeasured(maxLE32MeasuredBtn.getSelection());
            flags.getMaxTempLE32()
                    .setDeparture(maxLE32DepartureBtn.getSelection());
            flags.getMaxTempLE32().setNorm(maxLE32NormBtn.getSelection());
            if (!isNWR) {
                flags.getMaxTempLE32()
                        .setLastYear(maxLE32LastYearBtn.getSelection());
            }

            // Days with maximum temperature >= T1 F
            if (maxGET1MeasuredBtn.isEnabled()) {
                flags.getMaxTempGET1()
                        .setMeasured(maxGET1MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getMaxTempGET1()
                            .setLastYear(maxGET1LastYearBtn.getSelection());
                }
            }

            // Days with maximum temperature >= T2 F
            if (maxGET2MeasuredBtn.isEnabled()) {
                flags.getMaxTempGET2()
                        .setMeasured(maxGET2MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getMaxTempGET2()
                            .setLastYear(maxGET2LastYearBtn.getSelection());
                }
            }

            // Days with maximum temperature <= T3 F
            if (maxLET3MeasuredBtn.isEnabled()) {
                flags.getMaxTempLET3()
                        .setMeasured(maxLET3MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getMaxTempLET3()
                            .setLastYear(maxLET3LastYearBtn.getSelection());
                }
            }

            // Days with minimum temperature <= 32F
            flags.getMinTempLE32()
                    .setMeasured(minLE32MeasuredBtn.getSelection());
            flags.getMinTempLE32()
                    .setDeparture(minLE32DepartureBtn.getSelection());
            flags.getMinTempLE32().setNorm(minLE32NormBtn.getSelection());
            if (!isNWR) {
                flags.getMinTempLE32()
                        .setLastYear(minLE32LastYearBtn.getSelection());
            }

            // Days with minimum temperature <= 0F
            flags.getMinTempLE0().setMeasured(minLE0MeasuredBtn.getSelection());
            flags.getMinTempLE0()
                    .setDeparture(minLE0DepartureBtn.getSelection());
            flags.getMinTempLE0().setNorm(minLE0NormBtn.getSelection());
            if (!isNWR) {
                flags.getMinTempLE0()
                        .setLastYear(minLE0LastYearBtn.getSelection());
            }

            // Days with minimum temperature >= T4 F
            if (minGET4MeasuredBtn.isEnabled()) {
                flags.getMinTempGET4()
                        .setMeasured(minGET4MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getMinTempGET4()
                            .setLastYear(minGET4LastYearBtn.getSelection());
                }
            }

            // Days with minimum temperature <= T5 F
            if (minLET5MeasuredBtn.isEnabled()) {
                flags.getMinTempLET5()
                        .setMeasured(minLET5MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getMinTempLET5()
                            .setLastYear(minLET5LastYearBtn.getSelection());
                }
            }

            // Days with minimum temperature <= T6 F
            if (minLET6MeasuredBtn.isEnabled()) {
                flags.getMinTempLET6()
                        .setMeasured(minLET6MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getMinTempLET6()
                            .setLastYear(minLET6LastYearBtn.getSelection());
                }
            }
        }

        return flags;
    }

    /**
     * Set flags on the this composite based on a given ClimateProductControl.
     * 
     * @param isNWR
     *            boolean - if source is NWR or NWWS
     * @param isDaily
     *            boolean - if the period type is daily or period.
     * @param flags
     *            ClimateProductControl
     */
    public void setControlFlags(boolean isNWR, boolean isDaily,
            ClimateProductControl ctrlFlg) {

        this.isNWR = isNWR;
        this.isDaily = isDaily;

        TemperatureControlFlags flags = ctrlFlg.getTempControl();

        // Maximum temperature
        maxMeasuredBtn.setSelection(flags.getMaxTemp().isMeasured());
        maxTimeOfMeasuredBtn
                .setSelection(flags.getMaxTemp().isTimeOfMeasured());
        maxRecordBtn.setSelection(flags.getMaxTemp().isRecord());

        maxRecordYearBtn.setSelection(flags.getMaxTemp().isRecordYear());

        updateButton(maxRecordYearBtn, maxRecordBtn.getSelection());

        maxDepartureBtn.setSelection(flags.getMaxTemp().isDeparture());
        maxNormBtn.setSelection(flags.getMaxTemp().isNorm());
        if (!isNWR) {
            maxLastYearBtn.setSelection(flags.getMaxTemp().isLastYear());
            if (maxLastYearBtn.getSelection()) {
                maxDateOfLastBtn
                        .setSelection(flags.getMaxTemp().isDateOfLast());
            }
        }

        // Minimum temperature
        minMeasuredBtn.setSelection(flags.getMinTemp().isMeasured());
        minTimeOfMeasuredBtn
                .setSelection(flags.getMinTemp().isTimeOfMeasured());
        minRecordBtn.setSelection(flags.getMinTemp().isRecord());

        minRecordYearBtn.setSelection(flags.getMinTemp().isRecordYear());

        updateButton(minRecordYearBtn, minRecordBtn.getSelection());

        minDepartureBtn.setSelection(flags.getMinTemp().isDeparture());
        minNormBtn.setSelection(flags.getMinTemp().isNorm());
        if (!isNWR) {
            minLastYearBtn.setSelection(flags.getMinTemp().isLastYear());
            if (minLastYearBtn.getSelection()) {
                minDateOfLastBtn
                        .setSelection(flags.getMinTemp().isDateOfLast());
            }
        }

        // Mean temperature
        meanMeasuredBtn.setSelection(flags.getMeanTemp().isMeasured());
        meanDepartureBtn.setSelection(flags.getMeanTemp().isDeparture());
        meanNormBtn.setSelection(flags.getMeanTemp().isNorm());
        if (!isNWR) {
            meanLastYearBtn.setSelection(flags.getMeanTemp().isLastYear());
        }

        // Update flags for period type
        if (!isDaily) {
            // Mean maximum temperature
            meanMaxMeasuredBtn
                    .setSelection(flags.getMeanMaxTemp().isMeasured());
            meanMaxDepartureBtn
                    .setSelection(flags.getMeanMaxTemp().isDeparture());
            meanMaxNormBtn.setSelection(flags.getMeanMaxTemp().isNorm());
            if (!isNWR) {
                meanMaxLastYearBtn
                        .setSelection(flags.getMeanMaxTemp().isLastYear());
            }

            // Mean minimum temperature
            meanMinMeasuredBtn
                    .setSelection(flags.getMeanMinTemp().isMeasured());
            meanMinDepartureBtn
                    .setSelection(flags.getMeanMinTemp().isDeparture());
            meanMinNormBtn.setSelection(flags.getMeanMinTemp().isNorm());
            if (!isNWR) {
                meanMinLastYearBtn
                        .setSelection(flags.getMeanMinTemp().isLastYear());
            }

            // Days with maximum temperature >= 90F
            maxGE90MeasuredBtn
                    .setSelection(flags.getMaxTempGE90().isMeasured());
            maxGE90DepartureBtn
                    .setSelection(flags.getMaxTempGE90().isDeparture());
            maxGE90NormBtn.setSelection(flags.getMaxTempGE90().isNorm());
            if (!isNWR) {
                maxGE90LastYearBtn
                        .setSelection(flags.getMaxTempGE90().isLastYear());
            }

            // Days with maximum temperature <= 32F
            maxLE32MeasuredBtn
                    .setSelection(flags.getMaxTempLE32().isMeasured());
            maxLE32DepartureBtn
                    .setSelection(flags.getMaxTempLE32().isDeparture());
            maxLE32NormBtn.setSelection(flags.getMaxTempLE32().isNorm());
            if (!isNWR) {
                maxLE32LastYearBtn
                        .setSelection(flags.getMaxTempLE32().isLastYear());
            }

            // Days with maximum temperature >= T1 F
            if (maxGET1MeasuredBtn.isEnabled()) {
                maxGET1MeasuredBtn
                        .setSelection(flags.getMaxTempGET1().isMeasured());
                if (!isNWR) {
                    maxGET1LastYearBtn
                            .setSelection(flags.getMaxTempGET1().isLastYear());
                }
            }

            // Days with maximum temperature >= T2 F
            if (maxGET2MeasuredBtn.isEnabled()) {
                maxGET2MeasuredBtn
                        .setSelection(flags.getMaxTempGET2().isMeasured());
                if (!isNWR) {
                    maxGET2LastYearBtn
                            .setSelection(flags.getMaxTempGET2().isLastYear());
                }
            }

            // Days with maximum temperature <= T3 F
            if (maxLET3MeasuredBtn.isEnabled()) {
                maxLET3MeasuredBtn
                        .setSelection(flags.getMaxTempLET3().isMeasured());
                if (!isNWR) {
                    maxLET3LastYearBtn
                            .setSelection(flags.getMaxTempLET3().isLastYear());
                }
            }

            // Days with minimum temperature <= 32F
            minLE32MeasuredBtn
                    .setSelection(flags.getMinTempLE32().isMeasured());
            minLE32DepartureBtn
                    .setSelection(flags.getMinTempLE32().isDeparture());
            minLE32NormBtn.setSelection(flags.getMinTempLE32().isNorm());
            if (!isNWR) {
                minLE32LastYearBtn
                        .setSelection(flags.getMinTempLE32().isLastYear());
            }

            // Days with minimum temperature <= 0F
            minLE0MeasuredBtn.setSelection(flags.getMinTempLE0().isMeasured());
            minLE0DepartureBtn
                    .setSelection(flags.getMinTempLE0().isDeparture());
            minLE0NormBtn.setSelection(flags.getMinTempLE0().isNorm());
            if (!isNWR) {
                minLE0LastYearBtn
                        .setSelection(flags.getMinTempLE0().isLastYear());
            }

            // Days with minimum temperature >= T4 F
            if (minGET4MeasuredBtn.isEnabled()) {
                minGET4MeasuredBtn
                        .setSelection(flags.getMinTempGET4().isMeasured());
                if (!isNWR) {
                    minGET4LastYearBtn
                            .setSelection(flags.getMinTempGET4().isLastYear());
                }
            }

            // Days with minimum temperature <= T5 F
            if (minLET5MeasuredBtn.isEnabled()) {
                minLET5MeasuredBtn
                        .setSelection(flags.getMinTempLET5().isMeasured());
                if (!isNWR) {
                    minLET5LastYearBtn
                            .setSelection(flags.getMinTempLET5().isLastYear());
                }
            }

            // Days with minimum temperature <= T6 F
            if (minLET6MeasuredBtn.isEnabled()) {
                minLET6MeasuredBtn
                        .setSelection(flags.getMinTempLET6().isMeasured());
                if (!isNWR) {
                    minLET6LastYearBtn
                            .setSelection(flags.getMinTempLET6().isLastYear());
                }
            }
        }
    }

    /**
     * Update preferenceValues and associated GUI changes.
     * 
     * @param preferenceValues
     *            A ClimateGlobal for the new preferenceValues
     */
    @Override
    public void updatePreferences(ClimateGlobal preferenceValues) {

        super.updatePreferences(preferenceValues);

        // Update button status for T1 through T6.
        if (preferenceValues != null) {
            updatePreferenceBtn(maxGET1MeasuredBtn, maxGET1LastYearBtn,
                    preferenceValues.getT1(), "Max GE", !isNWR);
            updatePreferenceBtn(maxGET2MeasuredBtn, maxGET2LastYearBtn,
                    preferenceValues.getT2(), "Max GE", !isNWR);
            updatePreferenceBtn(maxLET3MeasuredBtn, maxLET3LastYearBtn,
                    preferenceValues.getT3(), "Max LE", !isNWR);
            updatePreferenceBtn(minGET4MeasuredBtn, minGET4LastYearBtn,
                    preferenceValues.getT4(), "Min GE", !isNWR);
            updatePreferenceBtn(minLET5MeasuredBtn, minLET5LastYearBtn,
                    preferenceValues.getT5(), "Min LE", !isNWR);
            updatePreferenceBtn(minLET6MeasuredBtn, minLET6LastYearBtn,
                    preferenceValues.getT6(), "Min LE", !isNWR);
        }

    }

    /**
     * Update status for a temperature threshold button based on the given
     * user-defined preference value (T1, T2, T3, T4, T5, T6).
     * 
     * @param measuredBtn
     *            Major button to report this value
     * @param lastYearBtn
     *            Button to report this value's last year value
     * @param relationship
     *            String to indicate max/min and GE/LE
     * @param isNWWS
     *            boolean - if it is NWR/NWWS
     */
    private void updatePreferenceBtn(Button measuredBtn, Button lastYearBtn,
            int prefValue, String relationship, boolean isNWWS) {

        // Disable/de-select if the preference is missing.
        String prefStr = DEF_THRESHOLD_STR;
        if (prefValue == ParameterFormatClimate.MISSING) {
            measuredBtn.setEnabled(false);
            measuredBtn.setSelection(false);
            lastYearBtn.setEnabled(false);
            lastYearBtn.setSelection(false);
        } else {
            // Enable buttons for new preference.
            prefStr = String.valueOf(prefValue);
            if (!measuredBtn.isEnabled()) {
                measuredBtn.setEnabled(true);
                measuredBtn.setSelection(false);
                if (isNWWS) {
                    lastYearBtn.setEnabled(true);
                    lastYearBtn.setSelection(false);
                }
            }
        }

        // Update button's label & show/hide "Last Year" button.
        measuredBtn.setText("Days " + relationship + " " + prefStr + " F");
        subCategoryMap.get(measuredBtn).setVisible(
                measuredBtn.isEnabled() && measuredBtn.getSelection());

        // Resize the button's layout.
        measuredBtn.getParent().getParent().layout();

    }

}