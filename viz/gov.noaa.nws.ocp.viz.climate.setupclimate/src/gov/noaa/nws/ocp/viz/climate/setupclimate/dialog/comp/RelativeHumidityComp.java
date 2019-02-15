/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.RelativeHumidityControlFlags;

/**
 * Composite to contain all sub-categories for climate relative humidity in
 * setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * 06 NOV 2018  55207      jwu          Enable some legacy behavior(DR 20889).
 * </pre>
 * 
 * @author jwu
 */
public class RelativeHumidityComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Composite for daily RH
    private Composite dailyRHComp;

    // Composite for period RH
    private Composite periodRHComp;

    // Buttons for max RH
    protected Composite maxRHMinorComp;

    private Button maxRHMeasuredBtn;

    private Button maxRHTimeOfMeasuredBtn;

    private Button maxRHLastYearBtn;

    // Buttons for min RH
    protected Composite minRHMinorComp;

    private Button minRHMeasuredBtn;

    private Button minRHTimeOfMeasuredBtn;

    private Button minRHLastYearBtn;

    // Buttons for mean RH (Daily)
    protected Composite meanRHMinorComp;

    private Button meanRHMeasuredBtn;

    private Button meanRHLastYearBtn;

    // Buttons for average RH (monthly/seasonal/annual)
    private Button averageRHMeasuredBtn;

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
    public RelativeHumidityComp(Composite parent, int style,
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
        RowLayout subcatCompLayout = new RowLayout(SWT.VERTICAL);
        subcatCompLayout.marginTop = 15;
        subcatCompLayout.spacing = 25;

        this.setLayout(subcatCompLayout);

        mainCompLayout = new RowLayout(SWT.HORIZONTAL);
        majorCompLayout = new RowLayout(SWT.VERTICAL);
        minorCompLayout = new RowLayout(SWT.VERTICAL);

        // Layouts for components.
        mainCompLayout.spacing = 30;
        mainCompLayout.marginWidth = 8;

        majorCompLayout.marginLeft = 15;

        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 15;

        // Average composite for period RH
        periodRHComp = new Composite(this, SWT.NONE);
        RowLayout periodRHLayout = new RowLayout(SWT.VERTICAL);
        periodRHLayout.marginTop = 5;
        periodRHLayout.marginLeft = 180;
        periodRHComp.setLayout(periodRHLayout);

        averageRHMeasuredBtn = new Button(periodRHComp, SWT.CHECK);
        averageRHMeasuredBtn.setText("Average");
        averageRHMeasuredBtn.setFont(majorFont);
        averageRHMeasuredBtn.setSelection(false);

        // Hide this one by default.
        periodRHComp.setVisible(false);

        // Maximum/minimum/mean composite for daily RH
        dailyRHComp = createTotalComp(this);

    }

    /**
     * Create a composite at top to hold elements for max/min/mean RH.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite rhMainComp;
        rhMainComp = new Composite(parent, SWT.NONE);
        rhMainComp.setLayout(mainCompLayout);

        // Maximum RH composite
        Composite maxRHComp = new Composite(rhMainComp, SWT.NONE);
        maxRHComp.setLayout(majorCompLayout);

        maxRHMeasuredBtn = new Button(maxRHComp, SWT.CHECK);
        maxRHMeasuredBtn.setText("Maximum");
        maxRHMeasuredBtn.setFont(majorFont);
        maxRHMeasuredBtn.setSelection(true);

        maxRHMinorComp = new Composite(maxRHComp, SWT.NONE);
        maxRHMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(maxRHMeasuredBtn, maxRHMinorComp);

        maxRHTimeOfMeasuredBtn = new Button(maxRHMinorComp, SWT.CHECK);
        maxRHTimeOfMeasuredBtn.setText("Time/Date\nof Occurrence");

        maxRHLastYearBtn = new Button(maxRHMinorComp, SWT.CHECK);
        maxRHLastYearBtn.setText("Last Year's");

        // Minimum RH composite
        Composite minRHComp = new Composite(rhMainComp, SWT.NONE);
        minRHComp.setLayout(majorCompLayout);

        minRHMeasuredBtn = new Button(minRHComp, SWT.CHECK);
        minRHMeasuredBtn.setText("Minimum");
        minRHMeasuredBtn.setFont(majorFont);
        minRHMeasuredBtn.setSelection(true);

        minRHMinorComp = new Composite(minRHComp, SWT.NONE);
        minRHMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(minRHMeasuredBtn, minRHMinorComp);

        minRHTimeOfMeasuredBtn = new Button(minRHMinorComp, SWT.CHECK);
        minRHTimeOfMeasuredBtn.setText("Time/Date\nof Occurrence");

        minRHLastYearBtn = new Button(minRHMinorComp, SWT.CHECK);
        minRHLastYearBtn.setText("Last Year's");

        // Mean RH composite
        Composite meanRHComp = new Composite(rhMainComp, SWT.NONE);
        meanRHComp.setLayout(majorCompLayout);

        meanRHMeasuredBtn = new Button(meanRHComp, SWT.CHECK);
        meanRHMeasuredBtn.setText("Mean");
        meanRHMeasuredBtn.setFont(majorFont);
        meanRHMeasuredBtn.setSelection(true);

        meanRHMinorComp = new Composite(meanRHComp, SWT.NONE);
        meanRHMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(meanRHMeasuredBtn, meanRHMinorComp);

        meanRHLastYearBtn = new Button(meanRHMinorComp, SWT.CHECK);
        meanRHLastYearBtn.setText("Last Year's");

        return rhMainComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
        updateButton(maxRHTimeOfMeasuredBtn, !isNWR);
        updateButton(maxRHLastYearBtn, false);
        updateButton(minRHTimeOfMeasuredBtn, !isNWR);
        updateButton(minRHLastYearBtn, false);
        updateButton(meanRHLastYearBtn, false);
        updateButton(averageRHMeasuredBtn, !isNWR);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        dailyRHComp.setVisible(isDaily);
        periodRHComp.setVisible(!isDaily);
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - RelativeHumidityControlFlags
     */
    public RelativeHumidityControlFlags getControlFlags() {
        RelativeHumidityControlFlags flags = RelativeHumidityControlFlags
                .getDefaultFlags();

        flags.getMaxRH().setMeasured(maxRHMeasuredBtn.getSelection());
        flags.getMinRH().setMeasured(minRHMeasuredBtn.getSelection());
        flags.getMeanRH().setMeasured(meanRHMeasuredBtn.getSelection());

        // Time of measurement is only for daily NWWS
        if (isDaily && !isNWR) {
            flags.getMaxRH()
                    .setTimeOfMeasured(maxRHTimeOfMeasuredBtn.getSelection());
            flags.getMinRH()
                    .setTimeOfMeasured(minRHTimeOfMeasuredBtn.getSelection());
        }

        // Average RH is only for period NWWS
        if (!isNWR && !isDaily) {
            flags.getAverageRH()
                    .setMeasured(averageRHMeasuredBtn.getSelection());
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

        RelativeHumidityControlFlags flags = ctrlFlg.getRelHumidityControl();

        maxRHMeasuredBtn.setSelection(flags.getMaxRH().isMeasured());
        minRHMeasuredBtn.setSelection(flags.getMinRH().isMeasured());
        meanRHMeasuredBtn.setSelection(flags.getMeanRH().isMeasured());

        // Time of measurement is only for daily NWWS
        if (isDaily && !isNWR) {
            maxRHTimeOfMeasuredBtn
                    .setSelection(flags.getMaxRH().isTimeOfMeasured());
            minRHTimeOfMeasuredBtn
                    .setSelection(flags.getMinRH().isTimeOfMeasured());
        }

        // Average RH is only for period NWWS
        if (!isNWR && !isDaily) {
            averageRHMeasuredBtn
                    .setSelection(flags.getAverageRH().isMeasured());
        }

    }

}