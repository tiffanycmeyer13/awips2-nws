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
import gov.noaa.nws.ocp.common.localization.climate.producttype.PrecipitationControlFlags;

/**
 * Composite to contain all sub-categories for climate precipitation in setup
 * GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * FEB 13, 2017 20640      jwu          Adjust GUI for preference values.
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * 
 * </pre>
 * 
 * @author jwu
 */
public class PrecipitationComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Buttons for total precipitation
    private Button totalMeasuredBtn;

    protected Button totalRecordMaxBtn;

    protected Button totalRecYearBtn;

    protected Button totalRecordMinBtn;

    protected Button totalRecordMinYearBtn;

    protected Button totalDepartureBtn;

    protected Button totalNormBtn;

    private Button totalLastYearBtn;

    private Button totalMonth2DateBtn;

    private Button totalSeason2DateBtn;

    private Button totalYear2DateBtn;

    // Buttons for days with precip. >= 0.01"
    private Composite precipGE01Comp;

    private Button precipGE01MeasuredBtn;

    private Button precipGE01DepartureBtn;

    private Button precipGE01NormBtn;

    private Button precipGE01LastYearBtn;

    // Buttons for days with precip. >= 0.1"
    private Composite precipGE10Comp;

    private Button precipGE10MeasuredBtn;

    private Button precipGE10DepartureBtn;

    private Button precipGE10NormBtn;

    private Button precipGE10LastYearBtn;

    // Buttons for days with precip. >= 0.5"
    private Composite precipGE50Comp;

    private Button precipGE50MeasuredBtn;

    private Button precipGE50DepartureBtn;

    private Button precipGE50NormBtn;

    private Button precipGE50LastYearBtn;

    // Buttons for days with precip. >= 1.0"
    private Composite precipGE100Comp;

    private Button precipGE100MeasuredBtn;

    private Button precipGE100DepartureBtn;

    private Button precipGE100NormBtn;

    private Button precipGE100LastYearBtn;

    // Buttons for average daily precip.amount
    private Composite precipAvgComp;

    private Button precipAvgMeasuredBtn;

    private Button precipAvgDepartureBtn;

    private Button precipAvgNormBtn;

    private Button precipAvgLastYearBtn;

    // Buttons for max storm precip. amount
    private Composite precipStormMaxComp;

    private Button precipStormMaxMeasuredBtn;

    private Button precipStormMaxTimeOfMeasuredBtn;

    private Button precipStormMaxLastYearBtn;

    private Button precipStormMaxDateOfLastBtn;

    // Buttons for 24 hour max precip.
    private Composite precip24HRComp;

    private Button precip24HRMeasuredBtn;

    private Button precip24HRTimeOfMeasuredBtn;

    private Button precip24HRLastYearBtn;

    private Button precip24HRDateOfLastBtn;

    // Buttons for days w/ precip >= P1
    private Composite precipGEP1Comp;

    private Button precipGEP1MeasuredBtn;

    private Button precipGEP1LastYearBtn;

    // Buttons for days w/ precip >= P2
    private Composite precipGEP2Comp;

    private Button precipGEP2MeasuredBtn;

    private Button precipGEP2LastYearBtn;

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
    public PrecipitationComp(Composite parent, int style,
            ClimateGlobal preferenceValues, Font majorFont) {

        super(parent, style, preferenceValues, majorFont);
    }

    /**
     * Create/initialize components.
     */
    protected void initializeComponents() {
        // Layout
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
     * Create a composite at left to hold all total precipitation/P1/P2
     * elements.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite totalMainComp;
        totalMainComp = new Composite(parent, SWT.NONE);
        totalMainComp.setLayout(mainCompLayout);

        // Total precipitation composite
        Composite totalComp = new Composite(totalMainComp, SWT.NONE);
        totalComp.setLayout(majorCompLayout);

        totalMeasuredBtn = new Button(totalComp, SWT.CHECK);
        totalMeasuredBtn.setText("Total");
        totalMeasuredBtn.setFont(majorFont);
        totalMeasuredBtn.setSelection(true);

        final Composite totalPrecipMinorComp = new Composite(totalComp,
                SWT.NONE);
        totalPrecipMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(totalMeasuredBtn, totalPrecipMinorComp);

        totalRecordMaxBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalRecordMaxBtn.setText("Record Max");
        totalRecordMaxBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date/Year" if "Record" is de-selected.
                updateButton(totalRecYearBtn, totalRecordMaxBtn.getSelection());
            }
        });

        totalRecYearBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalRecYearBtn.setText(" - Date/Year");
        totalRecYearBtn.setEnabled(totalRecordMaxBtn.getSelection());

        totalRecordMinBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalRecordMinBtn.setText("Record Min");
        totalRecordMinBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // De-select "-Date/Year" if "Record" is de-selected.
                updateButton(totalRecordMinYearBtn,
                        totalRecordMinBtn.getSelection());
            }
        });

        totalRecordMinYearBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalRecordMinYearBtn.setText(" - Date/Year");
        totalRecordMinYearBtn.setEnabled(totalRecordMinBtn.getSelection());

        totalDepartureBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
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

        totalNormBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
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

        totalLastYearBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalLastYearBtn.setText("Last Year's");

        totalMonth2DateBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalMonth2DateBtn.setText("Month to Date");

        totalSeason2DateBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalSeason2DateBtn.setText("Season to Date");

        totalYear2DateBtn = new Button(totalPrecipMinorComp, SWT.CHECK);
        totalYear2DateBtn.setText("Year to Date");

        // Clicking on "major" button will show/hide all items in it.
        totalMeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                totalPrecipMinorComp.setVisible(selected);
                totalPrecipMinorComp.layout(true, true);
            }
        });

        // Check if P1 and P2 are defined
        String P1Str = "??";
        String P2Str = "??";

        if (preferenceValues != null) {
            P1Str = getThresholdString(preferenceValues.getP1());
            P2Str = getThresholdString(preferenceValues.getP2());
        }

        // Composite for days with precip. >= P1 inch
        precipGEP1Comp = new Composite(totalComp, SWT.NONE);
        precipGEP1Comp.setLayout(majorCompLayout);

        precipGEP1MeasuredBtn = new Button(precipGEP1Comp, SWT.CHECK);
        precipGEP1MeasuredBtn.setText("Days GE " + P1Str + " in");
        precipGEP1MeasuredBtn.setFont(majorFont);
        precipGEP1MeasuredBtn.setSelection(false);

        final Composite precipGEP1MinorComp = new Composite(precipGEP1Comp,
                SWT.NONE);
        precipGEP1MinorComp.setLayout(minorCompLayout);
        precipGEP1MinorComp.setVisible(precipGEP1MeasuredBtn.getSelection());

        subCategoryMap.put(precipGEP1MeasuredBtn, precipGEP1MinorComp);

        precipGEP1LastYearBtn = new Button(precipGEP1MinorComp, SWT.CHECK);
        precipGEP1LastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipGEP1MeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipGEP1MinorComp.setVisible(selected);
                precipGEP1MinorComp.layout(true, true);
            }
        });

        // Composite for days with precip. >= P2 inch
        precipGEP2Comp = new Composite(totalComp, SWT.NONE);
        precipGEP2Comp.setLayout(majorCompLayout);

        precipGEP2MeasuredBtn = new Button(precipGEP2Comp, SWT.CHECK);
        precipGEP2MeasuredBtn.setText("Days GE " + P2Str + " in");
        precipGEP2MeasuredBtn.setFont(majorFont);
        precipGEP2MeasuredBtn.setSelection(false);

        final Composite precipGEP2MinorComp = new Composite(precipGEP2Comp,
                SWT.NONE);
        precipGEP2MinorComp.setLayout(minorCompLayout);
        precipGEP2MinorComp.setVisible(precipGEP2MeasuredBtn.getSelection());

        subCategoryMap.put(precipGEP2MeasuredBtn, precipGEP2MinorComp);

        precipGEP2LastYearBtn = new Button(precipGEP2MinorComp, SWT.CHECK);
        precipGEP2LastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipGEP2MeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipGEP2MinorComp.setVisible(selected);
                precipGEP2MinorComp.layout(true, true);
            }
        });

        return totalMainComp;
    }

    /**
     * Create a composite at center to elements for days with precipitation >=
     * 0.01, 0.10, 0.50, and 1.00 inch.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createDaysComp(Composite parent) {

        Composite daysComp;
        daysComp = new Composite(parent, SWT.NONE);
        daysComp.setLayout(mainCompLayout);

        // Composite for days with precip. >= 0.01 inch
        precipGE01Comp = new Composite(daysComp, SWT.NONE);
        precipGE01Comp.setLayout(majorCompLayout);

        precipGE01MeasuredBtn = new Button(precipGE01Comp, SWT.CHECK);
        precipGE01MeasuredBtn.setText("Days GE 0.01 in");
        precipGE01MeasuredBtn.setFont(majorFont);
        precipGE01MeasuredBtn.setSelection(true);

        final Composite precipGE01MinorComp = new Composite(precipGE01Comp,
                SWT.NONE);
        precipGE01MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precipGE01MeasuredBtn, precipGE01MinorComp);

        precipGE01DepartureBtn = new Button(precipGE01MinorComp, SWT.CHECK);
        precipGE01DepartureBtn.setText("Departure");

        precipGE01NormBtn = new Button(precipGE01MinorComp, SWT.CHECK);
        precipGE01NormBtn.setText("Normal");

        precipGE01LastYearBtn = new Button(precipGE01MinorComp, SWT.CHECK);
        precipGE01LastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipGE01MeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipGE01MinorComp.setVisible(selected);
                precipGE01MinorComp.layout(true, true);
            }
        });

        // Composite for days with precip. >= 0.10 inch
        precipGE10Comp = new Composite(daysComp, SWT.NONE);
        precipGE10Comp.setLayout(majorCompLayout);

        precipGE10MeasuredBtn = new Button(precipGE10Comp, SWT.CHECK);
        precipGE10MeasuredBtn.setText("Days GE 0.10 in");
        precipGE10MeasuredBtn.setFont(majorFont);
        precipGE10MeasuredBtn.setSelection(true);

        final Composite precipGE10MinorComp = new Composite(precipGE10Comp,
                SWT.NONE);
        precipGE10MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precipGE10MeasuredBtn, precipGE10MinorComp);

        precipGE10DepartureBtn = new Button(precipGE10MinorComp, SWT.CHECK);
        precipGE10DepartureBtn.setText("Departure");

        precipGE10NormBtn = new Button(precipGE10MinorComp, SWT.CHECK);
        precipGE10NormBtn.setText("Normal");

        precipGE10LastYearBtn = new Button(precipGE10MinorComp, SWT.CHECK);
        precipGE10LastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipGE10MeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipGE10MinorComp.setVisible(selected);
                precipGE10MinorComp.layout(true, true);
            }
        });

        // Composite for days with precip. >= 0.50 inch
        precipGE50Comp = new Composite(daysComp, SWT.NONE);
        precipGE50Comp.setLayout(majorCompLayout);

        precipGE50MeasuredBtn = new Button(precipGE50Comp, SWT.CHECK);
        precipGE50MeasuredBtn.setText("Days GE 0.50 in");
        precipGE50MeasuredBtn.setFont(majorFont);
        precipGE50MeasuredBtn.setSelection(true);

        final Composite precipGE50MinorComp = new Composite(precipGE50Comp,
                SWT.NONE);
        precipGE50MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precipGE50MeasuredBtn, precipGE50MinorComp);

        precipGE50DepartureBtn = new Button(precipGE50MinorComp, SWT.CHECK);
        precipGE50DepartureBtn.setText("Departure");

        precipGE50NormBtn = new Button(precipGE50MinorComp, SWT.CHECK);
        precipGE50NormBtn.setText("Normal");

        precipGE50LastYearBtn = new Button(precipGE50MinorComp, SWT.CHECK);
        precipGE50LastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipGE50MeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipGE50MinorComp.setVisible(selected);
                precipGE50MinorComp.layout(true, true);
            }
        });

        // Composite for days with precip. >= 1.00 inch
        precipGE100Comp = new Composite(daysComp, SWT.NONE);
        precipGE100Comp.setLayout(majorCompLayout);

        precipGE100MeasuredBtn = new Button(precipGE100Comp, SWT.CHECK);
        precipGE100MeasuredBtn.setText("Days GE 1.00 in");
        precipGE100MeasuredBtn.setFont(majorFont);
        precipGE100MeasuredBtn.setSelection(true);

        final Composite precipGE100MinorComp = new Composite(precipGE100Comp,
                SWT.NONE);
        precipGE100MinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precipGE100MeasuredBtn, precipGE100MinorComp);

        precipGE100DepartureBtn = new Button(precipGE100MinorComp, SWT.CHECK);
        precipGE100DepartureBtn.setText("Departure");

        precipGE100NormBtn = new Button(precipGE100MinorComp, SWT.CHECK);
        precipGE100NormBtn.setText("Normal");

        precipGE100LastYearBtn = new Button(precipGE100MinorComp, SWT.CHECK);
        precipGE100LastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipGE100MeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipGE100MinorComp.setVisible(selected);
                precipGE100MinorComp.layout(true, true);
            }
        });

        return daysComp;
    }

    /**
     * Create a composite at right to hold elements for daily average
     * precipitation, storm maximum, and 24 hour precipitation.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createAverageComp(Composite parent) {

        Composite averageComp;
        averageComp = new Composite(parent, SWT.NONE);
        averageComp.setLayout(mainCompLayout);

        // Composite for daily average precip.
        precipAvgComp = new Composite(averageComp, SWT.NONE);
        precipAvgComp.setLayout(majorCompLayout);

        precipAvgMeasuredBtn = new Button(precipAvgComp, SWT.CHECK);
        precipAvgMeasuredBtn.setText("Average Daily");
        precipAvgMeasuredBtn.setFont(majorFont);
        precipAvgMeasuredBtn.setSelection(true);

        final Composite precipAvgMinorComp = new Composite(precipAvgComp,
                SWT.NONE);
        precipAvgMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precipAvgMeasuredBtn, precipAvgMinorComp);

        precipAvgDepartureBtn = new Button(precipAvgMinorComp, SWT.CHECK);
        precipAvgDepartureBtn.setText("Departure");

        precipAvgNormBtn = new Button(precipAvgMinorComp, SWT.CHECK);
        precipAvgNormBtn.setText("Normal");

        precipAvgLastYearBtn = new Button(precipAvgMinorComp, SWT.CHECK);
        precipAvgLastYearBtn.setText("Last Year's");

        // Clicking on "major" button will show/hide all items in it.
        precipAvgMeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipAvgMinorComp.setVisible(selected);
                precipAvgMinorComp.layout(true, true);
            }
        });

        // Composite for storm max. precip.
        precipStormMaxComp = new Composite(averageComp, SWT.NONE);
        precipStormMaxComp.setLayout(majorCompLayout);

        precipStormMaxMeasuredBtn = new Button(precipStormMaxComp, SWT.CHECK);
        precipStormMaxMeasuredBtn.setText("Storm Max");
        precipStormMaxMeasuredBtn.setFont(majorFont);
        precipStormMaxMeasuredBtn.setSelection(true);

        final Composite precipStormMaxAvgMinorComp = new Composite(
                precipStormMaxComp, SWT.NONE);
        precipStormMaxAvgMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precipStormMaxMeasuredBtn,
                precipStormMaxAvgMinorComp);

        precipStormMaxTimeOfMeasuredBtn = new Button(precipStormMaxAvgMinorComp,
                SWT.CHECK);
        precipStormMaxTimeOfMeasuredBtn.setText("Date");

        precipStormMaxLastYearBtn = new Button(precipStormMaxAvgMinorComp,
                SWT.CHECK);
        precipStormMaxLastYearBtn.setText(" Last Year's");

        precipStormMaxDateOfLastBtn = new Button(precipStormMaxAvgMinorComp,
                SWT.CHECK);
        precipStormMaxDateOfLastBtn.setText("  - Date");

        // Clicking on "major" button will show/hide all items in it.
        precipStormMaxMeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precipStormMaxAvgMinorComp.setVisible(selected);
                precipStormMaxAvgMinorComp.layout(true, true);
            }
        });

        // Composite for 24 hour precip.
        precip24HRComp = new Composite(averageComp, SWT.NONE);
        precip24HRComp.setLayout(majorCompLayout);

        precip24HRMeasuredBtn = new Button(precip24HRComp, SWT.CHECK);
        precip24HRMeasuredBtn.setText("24 Hour Max");
        precip24HRMeasuredBtn.setFont(majorFont);
        precip24HRMeasuredBtn.setSelection(true);

        final Composite precip24HRMinorComp = new Composite(precip24HRComp,
                SWT.NONE);
        precip24HRMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(precip24HRMeasuredBtn, precip24HRMinorComp);

        precip24HRTimeOfMeasuredBtn = new Button(precip24HRMinorComp,
                SWT.CHECK);
        precip24HRTimeOfMeasuredBtn.setText("Date");

        precip24HRLastYearBtn = new Button(precip24HRMinorComp, SWT.CHECK);
        precip24HRLastYearBtn.setText("Last Year's");

        precip24HRDateOfLastBtn = new Button(precip24HRMinorComp, SWT.CHECK);
        precip24HRDateOfLastBtn.setText(" - Date");

        // Clicking on "major" button will show/hide all items in it.
        precip24HRMeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                precip24HRMinorComp.setVisible(selected);
                precip24HRMinorComp.layout(true, true);
            }
        });

        return averageComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
        updateButton(totalLastYearBtn, !isNWR);

        updateButton(precipGE01LastYearBtn, !isNWR);
        updateButton(precipGE10LastYearBtn, !isNWR);
        updateButton(precipGE50LastYearBtn, !isNWR);
        updateButton(precipGE100LastYearBtn, !isNWR);
        updateButton(precipGEP1LastYearBtn, !isNWR);
        updateButton(precipGEP2LastYearBtn, !isNWR);
        updateButton(precipAvgLastYearBtn, !isNWR);
        updateButton(precipStormMaxLastYearBtn, !isNWR);
        updateButton(precip24HRLastYearBtn, !isNWR);

        updateButton(precipStormMaxDateOfLastBtn, false);
        updateButton(precip24HRDateOfLastBtn, false);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        precipGE01Comp.setVisible(!isDaily);
        precipGE10Comp.setVisible(!isDaily);
        precipGE50Comp.setVisible(!isDaily);
        precipGE100Comp.setVisible(!isDaily);
        precipGEP1Comp.setVisible(!isDaily);
        precipGEP2Comp.setVisible(!isDaily);
        precip24HRComp.setVisible(!isDaily);
        precipStormMaxComp.setVisible(!isDaily);
        precipAvgComp.setVisible(!isDaily);

        if (precipGEP1MeasuredBtn.getText().contains("??")) {
            updateButton(precipGEP1MeasuredBtn, false);
        } else {
            updateButton(precipGEP1MeasuredBtn, true);
        }

        if (precipGEP2MeasuredBtn.getText().contains("??")) {
            updateButton(precipGEP2MeasuredBtn, false);
        } else {
            updateButton(precipGEP2MeasuredBtn, true);
        }

        updateButton(totalMonth2DateBtn, isDaily);
        updateButton(totalSeason2DateBtn, isDaily);
        updateButton(totalYear2DateBtn, isDaily);

        updateButton(totalRecordMinBtn, !isDaily);
        updateButton(totalRecordMinYearBtn, false);
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - PrecipitationControlFlags
     */
    public PrecipitationControlFlags getControlFlags() {
        PrecipitationControlFlags flags = PrecipitationControlFlags
                .getDefaultFlags();

        // Precipitation total & maximum
        flags.getPrecipTotal().setMeasured(totalMeasuredBtn.getSelection());
        flags.getPrecipTotal().setRecord(totalRecordMaxBtn.getSelection());
        flags.getPrecipTotal().setRecordYear(totalRecYearBtn.getSelection());
        flags.getPrecipTotal().setDeparture(totalDepartureBtn.getSelection());
        flags.getPrecipTotal().setNorm(totalNormBtn.getSelection());

        if (!isNWR) {
            flags.getPrecipTotal().setLastYear(totalLastYearBtn.getSelection());
        }

        if (isDaily) {
            flags.getPrecipTotal()
                    .setTotalMonth(totalMonth2DateBtn.getSelection());
            flags.getPrecipTotal()
                    .setTotalSeason(totalSeason2DateBtn.getSelection());
            flags.getPrecipTotal()
                    .setTotalYear(totalYear2DateBtn.getSelection());
        }

        // Update flags for period type
        if (!isDaily) {

            /*
             * Precipitation total minimum - this button is grouped in "Total"
             * section but holds its own flags.
             */
            flags.getPrecipMin().setRecord(totalRecordMinBtn.getSelection());
            if (totalRecordMinBtn.getSelection()) {
                flags.getPrecipMin()
                        .setRecordYear(totalRecordMinYearBtn.getSelection());
            }

            // Days with precipitation total >= 0.01 inch
            flags.getPrecipGE01()
                    .setMeasured(precipGE01MeasuredBtn.getSelection());
            flags.getPrecipGE01()
                    .setDeparture(precipGE01DepartureBtn.getSelection());
            flags.getPrecipGE01().setNorm(precipGE01NormBtn.getSelection());
            if (!isNWR) {
                flags.getPrecipGE01()
                        .setLastYear(precipGE01LastYearBtn.getSelection());
            }

            // Days with precipitation total >= 0.10 inch
            flags.getPrecipGE10()
                    .setMeasured(precipGE10MeasuredBtn.getSelection());

            flags.getPrecipGE10()
                    .setDeparture(precipGE10DepartureBtn.getSelection());
            flags.getPrecipGE10().setNorm(precipGE10NormBtn.getSelection());
            if (!isNWR) {
                flags.getPrecipGE10()
                        .setLastYear(precipGE10LastYearBtn.getSelection());

                // Days with precipitation total >= 0.50 inch
                flags.getPrecipGE50()
                        .setMeasured(precipGE50MeasuredBtn.getSelection());
                flags.getPrecipGE50()
                        .setDeparture(precipGE50DepartureBtn.getSelection());
                flags.getPrecipGE50().setNorm(precipGE50NormBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecipGE50()
                            .setLastYear(precipGE50LastYearBtn.getSelection());
                }

                // Days with precipitation total >= 1.00 inch
                flags.getPrecipGE100()
                        .setMeasured(precipGE100MeasuredBtn.getSelection());
                flags.getPrecipGE100()
                        .setDeparture(precipGE100DepartureBtn.getSelection());
                flags.getPrecipGE100()
                        .setNorm(precipGE100NormBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecipGE100()
                            .setLastYear(precipGE100LastYearBtn.getSelection());
                }

                // Days with precipitation total >= P1 inch
                flags.getPrecipGEP1()
                        .setMeasured(precipGEP1MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecipGEP1()
                            .setLastYear(precipGEP1LastYearBtn.getSelection());
                }

                // Days with precipitation total >= P2 inch
                flags.getPrecipGEP2()
                        .setMeasured(precipGEP2MeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecipGEP2()
                            .setLastYear(precipGEP2LastYearBtn.getSelection());
                }

                // Precipitation 24 hour maximum
                flags.getPrecip24HR()
                        .setMeasured(precip24HRMeasuredBtn.getSelection());
                flags.getPrecip24HR().setTimeOfMeasured(
                        precip24HRTimeOfMeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecip24HR()
                            .setLastYear(precip24HRLastYearBtn.getSelection());
                    if (precip24HRLastYearBtn.getSelection()) {
                        flags.getPrecip24HR().setDateOfLast(
                                precip24HRDateOfLastBtn.getSelection());
                    }
                }

                // Precipitation storm maximum
                flags.getPrecipStormMax()
                        .setMeasured(precipStormMaxMeasuredBtn.getSelection());
                flags.getPrecipStormMax().setTimeOfMeasured(
                        precipStormMaxTimeOfMeasuredBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecipStormMax().setLastYear(
                            precipStormMaxLastYearBtn.getSelection());
                    if (precipStormMaxLastYearBtn.getSelection()) {
                        flags.getPrecipStormMax().setDateOfLast(
                                precipStormMaxDateOfLastBtn.getSelection());
                    }
                }

                // Precipitation average daily
                flags.getPrecipAvg()
                        .setMeasured(precipAvgMeasuredBtn.getSelection());
                flags.getPrecipAvg()
                        .setDeparture(precipAvgDepartureBtn.getSelection());
                flags.getPrecipAvg().setNorm(precipAvgNormBtn.getSelection());
                if (!isNWR) {
                    flags.getPrecipAvg()
                            .setLastYear(precipAvgLastYearBtn.getSelection());
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

        PrecipitationControlFlags flags = ctrlFlg.getPrecipControl();

        // Precipitation total & maximum
        totalMeasuredBtn.setSelection(flags.getPrecipTotal().isMeasured());
        totalRecordMaxBtn.setSelection(flags.getPrecipTotal().isRecord());

        totalRecYearBtn.setSelection(flags.getPrecipTotal().isRecordYear());

        updateButton(totalRecYearBtn, totalRecordMaxBtn.getSelection());

        totalDepartureBtn.setSelection(flags.getPrecipTotal().isDeparture());
        totalNormBtn.setSelection(flags.getPrecipTotal().isNorm());

        if (!isNWR) {
            totalLastYearBtn.setSelection(flags.getPrecipTotal().isLastYear());
        }

        if (isDaily) {
            totalMonth2DateBtn
                    .setSelection(flags.getPrecipTotal().isTotalMonth());
            totalSeason2DateBtn
                    .setSelection(flags.getPrecipTotal().isTotalSeason());
            totalYear2DateBtn
                    .setSelection(flags.getPrecipTotal().isTotalYear());
        }

        // Update flags for period type
        if (!isDaily) {

            /*
             * Precipitation total minimum - this button is grouped in "Total"
             * section but holds its own flags.
             */
            totalRecordMinBtn.setSelection(flags.getPrecipMin().isRecord());
            if (totalRecordMinBtn.getSelection()) {
                totalRecordMinYearBtn
                        .setSelection(flags.getPrecipMin().isRecordYear());
            }

            updateButton(totalRecordMinYearBtn,
                    totalRecordMinBtn.getSelection());

            // Days with precipitation total >= 0.01 inch
            precipGE01MeasuredBtn
                    .setSelection(flags.getPrecipGE01().isMeasured());
            precipGE01DepartureBtn
                    .setSelection(flags.getPrecipGE01().isDeparture());
            precipGE01NormBtn.setSelection(flags.getPrecipGE01().isNorm());
            if (!isNWR) {
                precipGE01LastYearBtn
                        .setSelection(flags.getPrecipGE01().isLastYear());
            }

            // Days with precipitation total >= 0.10 inch
            precipGE10MeasuredBtn
                    .setSelection(flags.getPrecipGE10().isMeasured());

            precipGE10DepartureBtn
                    .setSelection(flags.getPrecipGE10().isDeparture());
            precipGE10NormBtn.setSelection(flags.getPrecipGE10().isNorm());
            if (!isNWR) {
                precipGE10LastYearBtn
                        .setSelection(flags.getPrecipGE10().isLastYear());

                // Days with precipitation total >= 0.50 inch
                precipGE50MeasuredBtn
                        .setSelection(flags.getPrecipGE50().isMeasured());
                precipGE50DepartureBtn
                        .setSelection(flags.getPrecipGE50().isDeparture());
                precipGE50NormBtn.setSelection(flags.getPrecipGE50().isNorm());
                precipGE50LastYearBtn
                        .setSelection(flags.getPrecipGE50().isLastYear());

                // Days with precipitation total >= 1.00 inch
                precipGE100MeasuredBtn
                        .setSelection(flags.getPrecipGE100().isMeasured());
                precipGE100DepartureBtn
                        .setSelection(flags.getPrecipGE100().isDeparture());
                precipGE100NormBtn
                        .setSelection(flags.getPrecipGE100().isNorm());
                precipGE100LastYearBtn
                        .setSelection(flags.getPrecipGE100().isLastYear());

                // Days with precipitation total >= P1 inch
                precipGEP1MeasuredBtn
                        .setSelection(flags.getPrecipGEP1().isMeasured());
                precipGEP1LastYearBtn
                        .setSelection(flags.getPrecipGEP1().isLastYear());

                // Days with precipitation total >= P2 inch
                precipGEP2MeasuredBtn
                        .setSelection(flags.getPrecipGEP2().isMeasured());
                precipGEP2LastYearBtn
                        .setSelection(flags.getPrecipGEP2().isLastYear());

                // Precipitation 24 hour maximum
                precip24HRMeasuredBtn
                        .setSelection(flags.getPrecip24HR().isMeasured());

                precip24HRTimeOfMeasuredBtn
                        .setSelection(flags.getPrecip24HR().isTimeOfMeasured());
                precip24HRLastYearBtn
                        .setSelection(flags.getPrecip24HR().isLastYear());
                if (precip24HRLastYearBtn.getSelection()) {

                    precip24HRDateOfLastBtn
                            .setSelection(flags.getPrecip24HR().isDateOfLast());
                }

                // Precipitation storm maximum
                precipStormMaxMeasuredBtn
                        .setSelection(flags.getPrecipStormMax().isMeasured());

                precipStormMaxTimeOfMeasuredBtn.setSelection(
                        flags.getPrecipStormMax().isTimeOfMeasured());

                precipStormMaxLastYearBtn
                        .setSelection(flags.getPrecipStormMax().isLastYear());
                if (precipStormMaxLastYearBtn.getSelection()) {
                    precipStormMaxDateOfLastBtn.setSelection(
                            flags.getPrecipStormMax().isDateOfLast());
                }

                // Precipitation average daily
                precipAvgMeasuredBtn
                        .setSelection(flags.getPrecipAvg().isMeasured());
                precipAvgDepartureBtn
                        .setSelection(flags.getPrecipAvg().isDeparture());
                precipAvgNormBtn.setSelection(flags.getPrecipAvg().isNorm());
                precipAvgLastYearBtn
                        .setSelection(flags.getPrecipAvg().isLastYear());
            }
        }
    }

    /**
     * Get a string based on the given precipitation threshold value.
     * 
     * @return getThresholdString() A string for given threshhold
     */
    private String getThresholdString(float threshold) {
        String thresholdStr = DEF_THRESHOLD_STR;
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (threshold == ParameterFormatClimate.TRACE) {
            thresholdStr = ParameterFormatClimate.TRACE_SYMBOL;
        } else {
            if (threshold != ParameterFormatClimate.MISSING_PRECIP) {
                thresholdStr = decimalFormat.format(threshold);
            }
        }

        return thresholdStr;
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

        // Update button status for P1 & P2.
        if (preferenceValues != null) {
            updatePreferenceBtn(precipGEP1MeasuredBtn, precipGEP1LastYearBtn,
                    preferenceValues.getP1(), !isNWR);
            updatePreferenceBtn(precipGEP2MeasuredBtn, precipGEP2LastYearBtn,
                    preferenceValues.getP2(), !isNWR);
        }

    }

    /**
     * Update status for a precipitation threshold button based on the given
     * user-defined preference value (P1, P2).
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
        if (prefValue == ParameterFormatClimate.MISSING_PRECIP) {
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

}