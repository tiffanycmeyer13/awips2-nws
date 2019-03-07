/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import java.text.DecimalFormat;

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
import gov.noaa.nws.ocp.common.localization.climate.producttype.SnowControlFlags;

/**
 * Composite to contain all sub-categories for climate snow fall in setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * 14 DEC 2016  20640      jwu          Initial creation
 * 24 FEB 2017  27420      amoore       Address warnings in code.
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * 30 AUG 2017  37472      jwu          Adjust snow depth rec year button.
 * 14 MAR 2018  44624      amoore       Fix ATAN bug where daily snowfall normals
 *                                      option was not reliably available in
 *                                      NWWS mode.
 * 20 SEP 2018  55207      jwu          Fix relation between "Last Year's" and 
 *                                      "-Date" buttons (DR 20889.
 * </pre>
 * 
 * @author jwu
 */
public class SnowfallComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Buttons for total snow fall
    private Button totalMeasuredBtn;

    protected Button totalRecordBtn;

    protected Button totalRecYearBtn;

    protected Button totalDepartureBtn;

    protected Button totalNormBtn;

    private Button totalLastYearBtn;

    private Button totalMonth2DateBtn;

    private Button totalSeason2DateBtn;

    private Button totalYear2DateBtn;

    // Buttons for total snow since July 1st

    private Composite snowJuly1Comp;

    private Button snowJuly1MeasuredBtn;

    private Button snowJuly1DepartureBtn;

    private Button snowJuly1NormBtn;

    private Button snowJuly1LastYearBtn;

    // Buttons for snow depth average
    private Button snowDepthAvgMeasuredBtn;

    protected Button snowDepthAvgRecordBtn;

    protected Button snowDepthAvgRecYearBtn;

    private Button snowDepthAvgLastYearBtn;

    protected Button snowDepthAvgDepartureBtn;

    protected Button snowDepthAvgNormBtn;

    // Buttons for snow depth maximum
    private Composite snowDepthMaxComp;

    private Button snowDepthMaxMeasuredBtn;

    private Button snowDepthMaxTimeOfMeasuredBtn;

    private Button snowDepthMaxLastYearBtn;

    private Button snowDepthMaxDateOfLastBtn;

    // Buttons for days with any snow
    private Composite snowAnyComp;

    private Button snowAnyMeasuredBtn;

    private Button snowAnyDepartureBtn;

    private Button snowAnyNormBtn;

    private Button snowAnyLastYearBtn;

    // Buttons for days with snow >= 1.0 inch
    private Composite snowGE100Comp;

    private Button snowGE100MeasuredBtn;

    private Button snowGE100DepartureBtn;

    private Button snowGE100NormBtn;

    private Button snowGE100LastYearBtn;

    // Buttons for snow total water equivalent
    private Composite snowWaterTotalComp;

    private Button snowWaterTotalMeasuredBtn;

    private Button snowWaterTotalDepartureBtn;

    private Button snowWaterTotalNormBtn;

    private Button snowWaterTotalLastYearBtn;

    // Buttons for snow total water equivalent since July 1st
    private Composite snowWaterJuly1Comp;

    private Button snowWaterJuly1MeasuredBtn;

    private Button snowWaterJuly1DepartureBtn;

    private Button snowWaterJuly1NormBtn;

    private Button snowWaterJuly1LastYearBtn;

    // Buttons for 24 hour max snow
    private Composite snow24HRComp;

    private Button snow24HRMeasuredBtn;

    private Button snow24HRTimeOfMeasuredBtn;

    private Button snow24HRRecordBtn;

    private Button snow24HRRecYearBtn;

    private Button snow24HRLastYearBtn;

    private Button snow24HRDateOfLastBtn;

    // Buttons for max storm snow amount
    private Composite snowStormMaxComp;

    private Button snowStormMaxMeasuredBtn;

    private Button snowStormMaxTimeOfMeasuredBtn;

    private Button snowStormMaxLastYearBtn;

    private Button snowStormMaxDateOfLastBtn;

    // Buttons for days w/ snow > S1 inch
    private Composite snowGES1Comp;

    private Button snowGES1MeasuredBtn;

    private Button snowGES1LastYearBtn;

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
    public SnowfallComp(Composite parent, int style,
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

        mainCompLayout.spacing = 1;
        mainCompLayout.marginWidth = 3;

        majorCompLayout.marginLeft = 5;

        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 10;

        // Maximum composite at left
        createTotalComp(this);

        // Minimum composite at center
        createDaysComp(this);

        // Minimum composite at right
        createAverageComp(this);

    }

    /**
     * Create a composite at left to hold all elements for snow total, snow
     * since July 1st and Days with snow > P1.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite totalMainComp;
        totalMainComp = new Composite(parent, SWT.NONE);
        totalMainComp.setLayout(mainCompLayout);

        // Total snow composite
        Composite totalsnowComp = new Composite(totalMainComp, SWT.NONE);
        totalsnowComp.setLayout(majorCompLayout);

        totalMeasuredBtn = new Button(totalsnowComp, SWT.CHECK);
        totalMeasuredBtn.setText("Total");
        totalMeasuredBtn.setFont(majorFont);
        totalMeasuredBtn.setSelection(true);

        final Composite totalsnowMinorComp = new Composite(totalsnowComp,
                SWT.NONE);
        totalsnowMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(totalMeasuredBtn, totalsnowMinorComp);

        totalRecordBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalRecordBtn.setText("Record");
        totalRecordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date/Year" if "Record" is de-selected.
                updateButton(totalRecYearBtn, totalRecordBtn.getSelection());
            }
        });

        totalRecYearBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalRecYearBtn.setText("  - Date/Year");
        totalRecYearBtn.setEnabled(totalRecordBtn.getSelection());

        totalDepartureBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalDepartureBtn.setText("Departure");
        totalDepartureBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // Select "Normal" if "Departure is selected.
                    if (totalDepartureBtn.getSelection()) {
                        totalNormBtn.setSelection(true);
                    }
                }
            }
        });

        totalNormBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalNormBtn.setText("Normal");
        totalNormBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isNWR && isDaily) {
                    // De-select "Departure" if "Normal" is de-selected.
                    if (!totalNormBtn.getSelection()) {
                        totalDepartureBtn.setSelection(false);
                    }
                }
            }
        });

        totalLastYearBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalLastYearBtn.setText("Last Year's");

        totalMonth2DateBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalMonth2DateBtn.setText("Month to Date");

        totalSeason2DateBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalSeason2DateBtn.setText("Season to Date");

        totalYear2DateBtn = new Button(totalsnowMinorComp, SWT.CHECK);
        totalYear2DateBtn.setText("Year to Date");

        // Composite for total snow since July 1st
        snowJuly1Comp = new Composite(totalsnowComp, SWT.NONE);
        snowJuly1Comp.setLayout(majorCompLayout);

        snowJuly1MeasuredBtn = new Button(snowJuly1Comp, SWT.CHECK);
        snowJuly1MeasuredBtn.setText("Tot Since Jul 1");
        snowJuly1MeasuredBtn.setFont(majorFont);
        snowJuly1MeasuredBtn.setSelection(true);

        final Composite snowJuly1MinorComp = new Composite(snowJuly1Comp,
                SWT.NONE);
        snowJuly1MinorComp.setLayout(minorCompLayout);
        snowJuly1MinorComp.setVisible(snowJuly1MeasuredBtn.getSelection());

        subCategoryMap.put(snowJuly1MeasuredBtn, snowJuly1MinorComp);

        snowJuly1DepartureBtn = new Button(snowJuly1MinorComp, SWT.CHECK);
        snowJuly1DepartureBtn.setText("Departure");

        snowJuly1NormBtn = new Button(snowJuly1MinorComp, SWT.CHECK);
        snowJuly1NormBtn.setText("Normal");

        snowJuly1LastYearBtn = new Button(snowJuly1MinorComp, SWT.CHECK);
        snowJuly1LastYearBtn.setText("Last Year's");

        // Get a label string for S1
        String S1Str = getThresholdString(preferenceValues.getS1());

        // Composite for days with snow. >= P1 inch
        snowGES1Comp = new Composite(totalsnowComp, SWT.NONE);
        snowGES1Comp.setLayout(majorCompLayout);

        snowGES1MeasuredBtn = new Button(snowGES1Comp, SWT.CHECK);
        snowGES1MeasuredBtn.setText("Days GE " + S1Str + " in");
        snowGES1MeasuredBtn.setFont(majorFont);
        snowGES1MeasuredBtn.setSelection(false);

        final Composite snowGEP1MinorComp = new Composite(snowGES1Comp,
                SWT.NONE);
        snowGEP1MinorComp.setLayout(minorCompLayout);
        snowGEP1MinorComp.setVisible(snowGES1MeasuredBtn.getSelection());

        subCategoryMap.put(snowGES1MeasuredBtn, snowGEP1MinorComp);

        snowGES1LastYearBtn = new Button(snowGEP1MinorComp, SWT.CHECK);
        snowGES1LastYearBtn.setText("Last Year's");

        return totalMainComp;
    }

    /**
     * Create a composite at center for elements snow depth average/
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createDaysComp(Composite parent) {

        Composite daysComp;
        daysComp = new Composite(parent, SWT.NONE);
        daysComp.setLayout(mainCompLayout);

        // Composite for depth average
        Composite snowDepthAvgComp = new Composite(daysComp, SWT.NONE);
        snowDepthAvgComp.setLayout(majorCompLayout);

        snowDepthAvgMeasuredBtn = new Button(snowDepthAvgComp, SWT.CHECK);
        snowDepthAvgMeasuredBtn.setText("Depth Average");
        snowDepthAvgMeasuredBtn.setFont(majorFont);
        snowDepthAvgMeasuredBtn.setSelection(true);

        final Composite snowDepthAvgMinorComp = new Composite(snowDepthAvgComp,
                SWT.NONE);
        snowDepthAvgMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowDepthAvgMeasuredBtn, snowDepthAvgMinorComp);

        snowDepthAvgRecordBtn = new Button(snowDepthAvgMinorComp, SWT.CHECK);
        snowDepthAvgRecordBtn.setText("Record");
        snowDepthAvgRecordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date/Year" if "Record" is de-selected.
                updateButton(snowDepthAvgRecYearBtn,
                        snowDepthAvgRecordBtn.getSelection());
            }
        });

        snowDepthAvgRecYearBtn = new Button(snowDepthAvgMinorComp, SWT.CHECK);
        snowDepthAvgRecYearBtn.setText("  - Date/Year");
        snowDepthAvgRecYearBtn.setEnabled(snowDepthAvgRecordBtn.getSelection());

        snowDepthAvgLastYearBtn = new Button(snowDepthAvgMinorComp, SWT.CHECK);
        snowDepthAvgLastYearBtn.setText("Last Year's");

        snowDepthAvgDepartureBtn = new Button(snowDepthAvgMinorComp, SWT.CHECK);
        snowDepthAvgDepartureBtn.setText("Departure");
        snowDepthAvgDepartureBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                    if (!snowDepthAvgDepartureBtn.getSelection()) {
                        snowDepthAvgNormBtn.setSelection(false);
                    }
            }
        });

        snowDepthAvgNormBtn = new Button(snowDepthAvgMinorComp, SWT.CHECK);
        snowDepthAvgNormBtn.setText("Normal");

        // Composite for snow depth maximum
        snowDepthMaxComp = new Composite(daysComp, SWT.NONE);
        snowDepthMaxComp.setLayout(majorCompLayout);

        snowDepthMaxMeasuredBtn = new Button(snowDepthMaxComp, SWT.CHECK);
        snowDepthMaxMeasuredBtn.setText("Depth Maximum");
        snowDepthMaxMeasuredBtn.setFont(majorFont);
        snowDepthMaxMeasuredBtn.setSelection(true);

        final Composite snowDepthMaxMinorComp = new Composite(snowDepthMaxComp,
                SWT.NONE);
        snowDepthMaxMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowDepthMaxMeasuredBtn, snowDepthMaxMinorComp);

        snowDepthMaxTimeOfMeasuredBtn = new Button(snowDepthMaxMinorComp,
                SWT.CHECK);
        snowDepthMaxTimeOfMeasuredBtn.setText("Date");

        snowDepthMaxLastYearBtn = new Button(snowDepthMaxMinorComp, SWT.CHECK);
        snowDepthMaxLastYearBtn.setText("Last Year's");
        snowDepthMaxLastYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date" if "Last Year" is de-selected.
                updateButton(snowDepthMaxDateOfLastBtn,
                        snowDepthMaxLastYearBtn.getSelection());
            }
        });

        snowDepthMaxDateOfLastBtn = new Button(snowDepthMaxMinorComp,
                SWT.CHECK);
        snowDepthMaxDateOfLastBtn.setText("  - Date");

        // Composite for days with any snow
        snowAnyComp = new Composite(daysComp, SWT.NONE);
        snowAnyComp.setLayout(majorCompLayout);

        snowAnyMeasuredBtn = new Button(snowAnyComp, SWT.CHECK);
        snowAnyMeasuredBtn.setText("Days Any Snow");
        snowAnyMeasuredBtn.setFont(majorFont);
        snowAnyMeasuredBtn.setSelection(true);

        final Composite snowAnyMinorComp = new Composite(snowAnyComp, SWT.NONE);
        snowAnyMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowAnyMeasuredBtn, snowAnyMinorComp);

        snowAnyDepartureBtn = new Button(snowAnyMinorComp, SWT.CHECK);
        snowAnyDepartureBtn.setText("Departure");

        snowAnyNormBtn = new Button(snowAnyMinorComp, SWT.CHECK);
        snowAnyNormBtn.setText("Normal");

        snowAnyLastYearBtn = new Button(snowAnyMinorComp, SWT.CHECK);
        snowAnyLastYearBtn.setText("Last Year's");

        // Composite for days with snow >= 1.00 inch
        snowGE100Comp = new Composite(daysComp, SWT.NONE);
        snowGE100Comp.setLayout(majorCompLayout);

        snowGE100MeasuredBtn = new Button(snowGE100Comp, SWT.CHECK);
        snowGE100MeasuredBtn.setText("Days GE 1.0 in");
        snowGE100MeasuredBtn.setFont(majorFont);
        snowGE100MeasuredBtn.setSelection(true);

        final Composite snowGE100MinorComp = new Composite(snowGE100Comp,
                SWT.NONE);
        snowGE100MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowGE100MeasuredBtn, snowGE100MinorComp);

        snowGE100DepartureBtn = new Button(snowGE100MinorComp, SWT.CHECK);
        snowGE100DepartureBtn.setText("Departure");

        snowGE100NormBtn = new Button(snowGE100MinorComp, SWT.CHECK);
        snowGE100NormBtn.setText("Normal");

        snowGE100LastYearBtn = new Button(snowGE100MinorComp, SWT.CHECK);
        snowGE100LastYearBtn.setText("Last Year's");

        return daysComp;
    }

    /**
     * Create a composite at right to hold elements for snow water equivalent,
     * water equivalent since July 1st, storm maximum, and 24 hour snow maximum.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createAverageComp(Composite parent) {

        Composite averageComp;
        averageComp = new Composite(parent, SWT.NONE);
        averageComp.setLayout(mainCompLayout);

        // Composite for snow total water equivalent
        snowWaterTotalComp = new Composite(averageComp, SWT.NONE);
        snowWaterTotalComp.setLayout(majorCompLayout);

        snowWaterTotalMeasuredBtn = new Button(snowWaterTotalComp, SWT.CHECK);
        snowWaterTotalMeasuredBtn.setText("Water Equiv.");
        snowWaterTotalMeasuredBtn.setFont(majorFont);
        snowWaterTotalMeasuredBtn.setSelection(true);

        final Composite snowWaterTotalMinorComp = new Composite(
                snowWaterTotalComp, SWT.NONE);
        snowWaterTotalMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowWaterTotalMeasuredBtn, snowWaterTotalMinorComp);

        snowWaterTotalDepartureBtn = new Button(snowWaterTotalMinorComp,
                SWT.CHECK);
        snowWaterTotalDepartureBtn.setText("Departure");

        snowWaterTotalNormBtn = new Button(snowWaterTotalMinorComp, SWT.CHECK);
        snowWaterTotalNormBtn.setText("Normal");

        snowWaterTotalLastYearBtn = new Button(snowWaterTotalMinorComp,
                SWT.CHECK);
        snowWaterTotalLastYearBtn.setText("Last Year's");

        // Composite for snow total water equivalent since July 1st
        snowWaterJuly1Comp = new Composite(averageComp, SWT.NONE);
        snowWaterJuly1Comp.setLayout(majorCompLayout);

        snowWaterJuly1MeasuredBtn = new Button(snowWaterJuly1Comp, SWT.CHECK);
        snowWaterJuly1MeasuredBtn.setText("Water Since Jul 1");
        snowWaterJuly1MeasuredBtn.setFont(majorFont);
        snowWaterJuly1MeasuredBtn.setSelection(true);

        final Composite snowWaterJuly1MinorComp = new Composite(
                snowWaterJuly1Comp, SWT.NONE);
        snowWaterJuly1MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowWaterJuly1MeasuredBtn, snowWaterJuly1MinorComp);

        snowWaterJuly1DepartureBtn = new Button(snowWaterJuly1MinorComp,
                SWT.CHECK);
        snowWaterJuly1DepartureBtn.setText("Departure");

        snowWaterJuly1NormBtn = new Button(snowWaterJuly1MinorComp, SWT.CHECK);
        snowWaterJuly1NormBtn.setText("Normal");

        snowWaterJuly1LastYearBtn = new Button(snowWaterJuly1MinorComp,
                SWT.CHECK);
        snowWaterJuly1LastYearBtn.setText("Last Year's");

        // Composite for 24 hour snow.
        snow24HRComp = new Composite(averageComp, SWT.NONE);
        snow24HRComp.setLayout(majorCompLayout);

        snow24HRMeasuredBtn = new Button(snow24HRComp, SWT.CHECK);
        snow24HRMeasuredBtn.setText("24 Hour Max");
        snow24HRMeasuredBtn.setFont(majorFont);
        snow24HRMeasuredBtn.setSelection(true);

        final Composite snow24HRMinorComp = new Composite(snow24HRComp,
                SWT.NONE);
        snow24HRMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snow24HRMeasuredBtn, snow24HRMinorComp);

        snow24HRTimeOfMeasuredBtn = new Button(snow24HRMinorComp, SWT.CHECK);
        snow24HRTimeOfMeasuredBtn.setText("Date");

        snow24HRRecordBtn = new Button(snow24HRMinorComp, SWT.CHECK);
        snow24HRRecordBtn.setText("Record");

        snow24HRRecYearBtn = new Button(snow24HRMinorComp, SWT.CHECK);
        snow24HRRecYearBtn.setText("  - Year");

        snow24HRLastYearBtn = new Button(snow24HRMinorComp, SWT.CHECK);
        snow24HRLastYearBtn.setText("Last Year's");
        snow24HRLastYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date" if "Last Year" is de-selected.
                updateButton(snow24HRDateOfLastBtn,
                        snow24HRLastYearBtn.getSelection());
            }
        });

        snow24HRDateOfLastBtn = new Button(snow24HRMinorComp, SWT.CHECK);
        snow24HRDateOfLastBtn.setText("  - Date");

        // Composite for storm max. snow.
        snowStormMaxComp = new Composite(averageComp, SWT.NONE);
        snowStormMaxComp.setLayout(majorCompLayout);

        snowStormMaxMeasuredBtn = new Button(snowStormMaxComp, SWT.CHECK);
        snowStormMaxMeasuredBtn.setText("Storm Max");
        snowStormMaxMeasuredBtn.setFont(majorFont);
        snowStormMaxMeasuredBtn.setSelection(true);

        final Composite snowStormMaxAvgMinorComp = new Composite(
                snowStormMaxComp, SWT.NONE);
        snowStormMaxAvgMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(snowStormMaxMeasuredBtn, snowStormMaxAvgMinorComp);

        snowStormMaxTimeOfMeasuredBtn = new Button(snowStormMaxAvgMinorComp,
                SWT.CHECK);
        snowStormMaxTimeOfMeasuredBtn.setText("Date");

        snowStormMaxLastYearBtn = new Button(snowStormMaxAvgMinorComp,
                SWT.CHECK);
        snowStormMaxLastYearBtn.setText(" Last Year's");
        snowStormMaxLastYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date" if "Last Year" is de-selected.
                updateButton(snowStormMaxDateOfLastBtn,
                        snowStormMaxLastYearBtn.getSelection());
            }
        });

        snowStormMaxDateOfLastBtn = new Button(snowStormMaxAvgMinorComp,
                SWT.CHECK);
        snowStormMaxDateOfLastBtn.setText("  - Date");

        return averageComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
        updateButton(totalLastYearBtn, !isNWR);
        updateButton(snowDepthAvgLastYearBtn, !isNWR);

        updateButton(snowJuly1LastYearBtn, !isNWR);
        updateButton(snowDepthMaxLastYearBtn, !isNWR);
        updateButton(snowAnyLastYearBtn, !isNWR);
        updateButton(snowGE100LastYearBtn, !isNWR);
        updateButton(snowWaterTotalLastYearBtn, !isNWR);
        updateButton(snowWaterJuly1LastYearBtn, !isNWR);
        updateButton(snow24HRLastYearBtn, !isNWR);
        updateButton(snowStormMaxLastYearBtn, !isNWR);
        updateButton(snowGES1LastYearBtn, !isNWR);

        updateButton(snowDepthMaxDateOfLastBtn,
                !isNWR && snowDepthMaxLastYearBtn.getSelection());
        updateButton(snow24HRDateOfLastBtn,
                !isNWR && snow24HRLastYearBtn.getSelection());
        updateButton(snowStormMaxDateOfLastBtn,
                !isNWR && snowStormMaxLastYearBtn.getSelection());
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        snowJuly1Comp.setVisible(!isDaily);
        snowDepthMaxComp.setVisible(!isDaily);
        snowAnyComp.setVisible(!isDaily);
        snowGE100Comp.setVisible(!isDaily);
        snowWaterTotalComp.setVisible(!isDaily);
        snowWaterJuly1Comp.setVisible(!isDaily);
        snow24HRComp.setVisible(!isDaily);
        snowStormMaxComp.setVisible(!isDaily);
        snowGES1Comp.setVisible(!isDaily);

        if (snowGES1MeasuredBtn.getText().contains("??")) {
            updateButton(snowGES1MeasuredBtn, false);
        } else {
            updateButton(snowGES1MeasuredBtn, true);
        }

        updateButton(totalMonth2DateBtn, isDaily);
        updateButton(totalSeason2DateBtn, isDaily);
        updateButton(totalYear2DateBtn, isDaily);
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - SnowControlFlags
     */
    public SnowControlFlags getControlFlags() {
        SnowControlFlags flags = SnowControlFlags.getDefaultFlags();

        // Snow total
        flags.getSnowTotal().setMeasured(totalMeasuredBtn.getSelection());
        flags.getSnowTotal().setRecord(totalRecordBtn.getSelection());
        flags.getSnowTotal().setRecordYear(totalRecYearBtn.getSelection());
        flags.getSnowTotal().setDeparture(totalDepartureBtn.getSelection());
        flags.getSnowTotal().setNorm(totalNormBtn.getSelection());

        if (!isNWR) {
            flags.getSnowTotal().setLastYear(totalLastYearBtn.getSelection());
        }

        if (isDaily) {
            flags.getSnowTotal()
                    .setTotalMonth(totalMonth2DateBtn.getSelection());
            flags.getSnowTotal()
                    .setTotalSeason(totalSeason2DateBtn.getSelection());
            flags.getSnowTotal().setTotalYear(totalYear2DateBtn.getSelection());
        }

        // Snow depth average
        flags.getSnowDepthAvg()
                .setMeasured(snowDepthAvgMeasuredBtn.getSelection());
        flags.getSnowDepthAvg().setRecord(snowDepthAvgRecordBtn.getSelection());
        flags.getSnowDepthAvg()
                .setRecordYear(snowDepthAvgRecYearBtn.getSelection());
        flags.getSnowDepthAvg()
                .setDeparture(snowDepthAvgDepartureBtn.getSelection());
        flags.getSnowDepthAvg().setNorm(snowDepthAvgNormBtn.getSelection());

        if (!isNWR) {
            flags.getSnowDepthAvg()
                    .setLastYear(snowDepthAvgLastYearBtn.getSelection());
        }

        /*
         * Update flags for period type.
         */
        if (!isDaily) {
            // Total snow since July 1st
            flags.getSnowJuly1()
                    .setMeasured(snowJuly1MeasuredBtn.getSelection());
            flags.getSnowJuly1()
                    .setDeparture(snowJuly1DepartureBtn.getSelection());
            flags.getSnowJuly1().setNorm(snowJuly1NormBtn.getSelection());
            if (!isNWR) {
                flags.getSnowJuly1()
                        .setLastYear(snowJuly1LastYearBtn.getSelection());
            }

            // Days with any snow
            flags.getSnowAny().setMeasured(snowAnyMeasuredBtn.getSelection());
            flags.getSnowAny().setDeparture(snowAnyDepartureBtn.getSelection());
            flags.getSnowAny().setNorm(snowAnyNormBtn.getSelection());
            if (!isNWR) {
                flags.getSnowAny()
                        .setLastYear(snowAnyLastYearBtn.getSelection());
            }

            // Snow total >= 1.0 inch
            flags.getSnowGE100()
                    .setMeasured(snowGE100MeasuredBtn.getSelection());
            flags.getSnowGE100()
                    .setDeparture(snowGE100DepartureBtn.getSelection());
            flags.getSnowGE100().setNorm(snowGE100NormBtn.getSelection());
            if (!isNWR) {
                flags.getSnowGE100()
                        .setLastYear(snowGE100LastYearBtn.getSelection());
            }

            // Snow total >= P1 inch
            if (snowGES1Comp.isVisible() && snowGES1MeasuredBtn.isEnabled()) {
                flags.getSnowGEP1()
                        .setMeasured(snowGES1MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getSnowGEP1()
                            .setLastYear(snowGES1LastYearBtn.getSelection());
                }
            }

            // Snow 24 hour maximum
            flags.getSnow24hr().setMeasured(snow24HRMeasuredBtn.getSelection());
            flags.getSnow24hr().setTimeOfMeasured(
                    snow24HRTimeOfMeasuredBtn.getSelection());
            flags.getSnow24hr().setRecord(snow24HRRecordBtn.getSelection());
            flags.getSnow24hr()
                    .setRecordYear(snow24HRRecYearBtn.getSelection());
            if (!isNWR) {
                flags.getSnow24hr()
                        .setLastYear(snow24HRLastYearBtn.getSelection());
                flags.getSnow24hr()
                        .setDateOfLast(snow24HRDateOfLastBtn.getSelection());
                if (snow24HRLastYearBtn.getSelection()) {
                    flags.getSnow24hr().setDateOfLast(
                            snow24HRDateOfLastBtn.getSelection());
                }
            }

            // Snow storm maximum
            flags.getSnowStormMax()
                    .setMeasured(snowStormMaxMeasuredBtn.getSelection());
            flags.getSnowStormMax().setTimeOfMeasured(
                    snowStormMaxTimeOfMeasuredBtn.getSelection());
            if (!isNWR) {
                flags.getSnowStormMax()
                        .setLastYear(snowStormMaxLastYearBtn.getSelection());
                if (snowStormMaxLastYearBtn.getSelection()) {
                    flags.getSnowStormMax().setDateOfLast(
                            snowStormMaxDateOfLastBtn.getSelection());
                }
            }

            // Snow water equivalent
            flags.getSnowWaterTotal()
                    .setMeasured(snowWaterTotalMeasuredBtn.getSelection());
            flags.getSnowWaterTotal()
                    .setDeparture(snowWaterTotalDepartureBtn.getSelection());
            flags.getSnowWaterTotal()
                    .setNorm(snowWaterTotalNormBtn.getSelection());
            if (!isNWR) {
                flags.getSnowWaterTotal()
                        .setLastYear(snowWaterTotalLastYearBtn.getSelection());
            }

            // Snow water equivalent since July 1st
            flags.getSnowWaterJuly1()
                    .setMeasured(snowWaterJuly1MeasuredBtn.getSelection());
            flags.getSnowWaterJuly1()
                    .setDeparture(snowWaterJuly1DepartureBtn.getSelection());
            flags.getSnowWaterJuly1()
                    .setNorm(snowWaterJuly1NormBtn.getSelection());
            if (!isNWR) {
                flags.getSnowWaterJuly1()
                        .setLastYear(snowWaterJuly1LastYearBtn.getSelection());
            }

            // Snow depth maximum
            flags.getSnowDepthMax()
                    .setMeasured(snowDepthMaxMeasuredBtn.getSelection());
            flags.getSnowDepthMax().setTimeOfMeasured(
                    snowDepthMaxTimeOfMeasuredBtn.getSelection());
            if (!isNWR) {
                flags.getSnowDepthMax()
                        .setLastYear(snowDepthMaxLastYearBtn.getSelection());
                if (snowDepthMaxLastYearBtn.getSelection()) {
                    flags.getSnowDepthMax().setDateOfLast(
                            snowDepthMaxDateOfLastBtn.getSelection());
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

        SnowControlFlags flags = ctrlFlg.getSnowControl();

        // Snow total
        totalMeasuredBtn.setSelection(flags.getSnowTotal().isMeasured());
        totalRecordBtn.setSelection(flags.getSnowTotal().isRecord());

        totalRecYearBtn.setSelection(flags.getSnowTotal().isRecordYear());

        updateButton(totalRecYearBtn, totalRecordBtn.getSelection());

        totalDepartureBtn.setSelection(flags.getSnowTotal().isDeparture());
        totalNormBtn.setSelection(flags.getSnowTotal().isNorm());

        if (!isNWR) {
            totalLastYearBtn.setSelection(flags.getSnowTotal().isLastYear());
        }

        if (isDaily) {
            totalMonth2DateBtn
                    .setSelection(flags.getSnowTotal().isTotalMonth());
            totalSeason2DateBtn
                    .setSelection(flags.getSnowTotal().isTotalSeason());
            totalYear2DateBtn.setSelection(flags.getSnowTotal().isTotalYear());
        }

        // Snow depth average
        snowDepthAvgMeasuredBtn
                .setSelection(flags.getSnowDepthAvg().isMeasured());
        snowDepthAvgRecordBtn.setSelection(flags.getSnowDepthAvg().isRecord());
        snowDepthAvgRecYearBtn
                .setSelection(flags.getSnowDepthAvg().isRecordYear());

        updateButton(snowDepthAvgRecYearBtn,
                snowDepthAvgRecordBtn.getSelection());

        snowDepthAvgDepartureBtn
                .setSelection(flags.getSnowDepthAvg().isDeparture());
        snowDepthAvgNormBtn.setSelection(flags.getSnowDepthAvg().isNorm());

        if (!isNWR) {
            snowDepthAvgLastYearBtn
                    .setSelection(flags.getSnowDepthAvg().isLastYear());
        }

        /*
         * Update flags for period type.
         */
        if (!isDaily) {
            // Total snow since July 1st
            snowJuly1MeasuredBtn
                    .setSelection(flags.getSnowJuly1().isMeasured());
            snowJuly1DepartureBtn
                    .setSelection(flags.getSnowJuly1().isDeparture());
            snowJuly1NormBtn.setSelection(flags.getSnowJuly1().isNorm());
            if (!isNWR) {
                snowJuly1LastYearBtn
                        .setSelection(flags.getSnowJuly1().isLastYear());
            }

            // Days with any snow
            snowAnyMeasuredBtn.setSelection(flags.getSnowAny().isMeasured());
            snowAnyDepartureBtn.setSelection(flags.getSnowAny().isDeparture());
            snowAnyNormBtn.setSelection(flags.getSnowAny().isNorm());
            if (!isNWR) {
                snowAnyLastYearBtn
                        .setSelection(flags.getSnowAny().isLastYear());
            }

            // Snow total >= 1.0 inch
            snowGE100MeasuredBtn
                    .setSelection(flags.getSnowGE100().isMeasured());
            snowGE100DepartureBtn
                    .setSelection(flags.getSnowGE100().isDeparture());
            snowGE100NormBtn.setSelection(flags.getSnowGE100().isNorm());
            if (!isNWR) {
                snowGE100LastYearBtn
                        .setSelection(flags.getSnowGE100().isLastYear());
            }

            // Snow total >= P1 inch
            if (snowGES1Comp.isVisible() && snowGES1MeasuredBtn.isEnabled()) {
                snowGES1MeasuredBtn
                        .setSelection(flags.getSnowGEP1().isMeasured());
                if (!isNWR) {
                    snowGES1LastYearBtn
                            .setSelection(flags.getSnowGEP1().isLastYear());
                }
            }

            // Snow 24 hour maximum
            snow24HRMeasuredBtn.setSelection(flags.getSnow24hr().isMeasured());

            snow24HRTimeOfMeasuredBtn
                    .setSelection(flags.getSnow24hr().isTimeOfMeasured());
            snow24HRRecordBtn.setSelection(flags.getSnow24hr().isRecord());
            snow24HRRecYearBtn.setSelection(flags.getSnow24hr().isRecordYear());
            if (!isNWR) {
                snow24HRLastYearBtn
                        .setSelection(flags.getSnow24hr().isLastYear());
                snow24HRDateOfLastBtn
                        .setSelection(flags.getSnow24hr().isDateOfLast());
                snow24HRDateOfLastBtn
                        .setEnabled(snow24HRLastYearBtn.getSelection());
                if (snow24HRLastYearBtn.getSelection()) {
                    snow24HRDateOfLastBtn
                            .setSelection(flags.getSnow24hr().isDateOfLast());
                }
            }

            // Snow storm maximum
            snowStormMaxMeasuredBtn
                    .setSelection(flags.getSnowStormMax().isMeasured());

            snowStormMaxTimeOfMeasuredBtn
                    .setSelection(flags.getSnowStormMax().isTimeOfMeasured());
            if (!isNWR) {
                snowStormMaxLastYearBtn
                        .setSelection(flags.getSnowStormMax().isLastYear());
                snowStormMaxDateOfLastBtn
                        .setEnabled(snowStormMaxLastYearBtn.getSelection());
                if (snowStormMaxLastYearBtn.getSelection()) {
                    snowStormMaxDateOfLastBtn.setSelection(
                            flags.getSnowStormMax().isDateOfLast());
                }
            }

            // Snow water equivalent
            snowWaterTotalMeasuredBtn
                    .setSelection(flags.getSnowWaterTotal().isMeasured());
            snowWaterTotalDepartureBtn
                    .setSelection(flags.getSnowWaterTotal().isDeparture());
            snowWaterTotalNormBtn
                    .setSelection(flags.getSnowWaterTotal().isNorm());
            if (!isNWR) {
                snowWaterTotalLastYearBtn
                        .setSelection(flags.getSnowWaterTotal().isLastYear());
            }

            // Snow water equivalent since July 1st
            snowWaterJuly1MeasuredBtn
                    .setSelection(flags.getSnowWaterJuly1().isMeasured());
            snowWaterJuly1DepartureBtn
                    .setSelection(flags.getSnowWaterJuly1().isDeparture());
            snowWaterJuly1NormBtn
                    .setSelection(flags.getSnowWaterJuly1().isNorm());
            if (!isNWR) {
                snowWaterJuly1LastYearBtn
                        .setSelection(flags.getSnowWaterJuly1().isLastYear());
            }

            // Snow depth maximum
            snowDepthMaxMeasuredBtn
                    .setSelection(flags.getSnowDepthMax().isMeasured());
            snowDepthMaxTimeOfMeasuredBtn
                    .setSelection(flags.getSnowDepthMax().isTimeOfMeasured());
            if (!isNWR) {
                snowDepthMaxLastYearBtn
                        .setSelection(flags.getSnowDepthMax().isLastYear());
                snowDepthMaxLastYearBtn
                        .setEnabled(snowDepthMaxLastYearBtn.getSelection());
                if (snowDepthMaxLastYearBtn.getSelection()) {
                    snowDepthMaxDateOfLastBtn.setSelection(
                            flags.getSnowDepthMax().isDateOfLast());
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

        // Update button status for S1.
        if (preferenceValues != null) {
            updatePreferenceBtn(snowGES1MeasuredBtn, snowGES1LastYearBtn,
                    preferenceValues.getS1(), !isNWR);
        }

    }

    /**
     * Update status for a snowfall threshold button based on the given
     * user-defined preference value (S1).
     * 
     * @param measuredBtn
     *            Major button to report this value
     * @param lastYearBtn
     *            Button to report this value's last year value
     * @param isNWWS
     *            boolean - if it is NWR/NWWS
     */
    private void updatePreferenceBtn(Button measuredBtn, Button lastYearBtn,
            float prefValue, boolean isNWWS) {

        // Disable/de-select if the preference is missing.
        if (prefValue == ParameterFormatClimate.MISSING_SNOW) {
            measuredBtn.setEnabled(false);
            measuredBtn.setSelection(false);
            lastYearBtn.setEnabled(false);
            lastYearBtn.setSelection(false);
        } else {
            // Enable buttons for new preference.
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
        String prefStr = getThresholdString(prefValue);
        measuredBtn.setText("Days GE " + prefStr + " in");
        subCategoryMap.get(measuredBtn).setVisible(
                measuredBtn.isEnabled() && measuredBtn.getSelection());

        // Resize the button's layout.
        measuredBtn.getParent().getParent().layout();
    }

    /**
     * Get a string based on the given snowfall threshold value.
     * 
     * @return getThresholdString() A string for given threshhold
     */
    private String getThresholdString(float threshold) {
        String thresholdStr = DEF_THRESHOLD_STR;
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        if (threshold == ParameterFormatClimate.TRACE) {
            thresholdStr = ParameterFormatClimate.TRACE_SYMBOL;
        } else {
            if (threshold != ParameterFormatClimate.MISSING_SNOW) {
                thresholdStr = decimalFormat.format(threshold);
            }
        }

        return thresholdStr;
    }

}