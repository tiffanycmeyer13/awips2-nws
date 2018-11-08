/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.DegreeDaysControlFlags;

/**
 * Composite to contain all sub-categories for climate snow fall in setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * 22 JUN 2017  33104      amoore       Fix copy-paste error.
 * 11 JUL 2017  33104      amoore       Fix copy-paste error.
 * 16 AUG 2017             jwu          Rename "Year to Date" to "Since ...".
 * 06 NOV 2018  55207      jwu          Enable some legacy behavior(DR 20889).
 * </pre>
 * 
 * @author jwu
 */
public class DegreeDaysComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Buttons for total heating days
    protected Composite totalHDDMinorComp;

    private Button totalHDDMeasuredBtn;

    private Button totalHDDDepartureBtn;

    private Button totalHDDNormBtn;

    private Button totalHDDLastYearBtn;

    private Button totalHDDMonth2DateBtn;

    private Button totalHDDSeason2DateBtn;

    private Button totalHDDSinceJuly1Btn;

    // Buttons for heating days since 7/1
    private Composite seasonComp;

    protected Composite seasonHDDMinorComp;

    private Button seasonHDDMeasuredBtn;

    private Button seasonHDDDepartureBtn;

    private Button seasonHDDNormBtn;

    private Button seasonHDDLastYearBtn;

    // Buttons for total cooling days
    protected Composite totalCDDMinorComp;

    private Button totalCDDMeasuredBtn;

    private Button totalCDDDepartureBtn;

    private Button totalCDDNormBtn;

    private Button totalCDDLastYearBtn;

    private Button totalCDDMonth2DateBtn;

    private Button totalCDDSeason2DateBtn;

    private Button totalCDDSinceJan1Btn;

    // Buttons for cooling days since 1/1
    protected Composite seasonCDDMinorComp;

    private Button seasonCDDMeasuredBtn;

    private Button seasonCDDDepartureBtn;

    private Button seasonCDDNormBtn;

    private Button seasonCDDLastYearBtn;

    // Buttons for earliest free date
    private Composite freezeComp;

    protected Composite earlyFreezeMinorComp;

    private Button earlyFreezeMeasuredBtn;

    private Button earlyFreezeRecordBtn;

    private Button earlyFreezeNormBtn;

    // Buttons for latest free date
    protected Composite lateFreezeMinorComp;

    private Button lateFreezeMeasuredBtn;

    private Button lateFreezeRecordBtn;

    private Button lateFreezeNormBtn;

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
    public DegreeDaysComp(Composite parent, int style,
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
        subcatCompLayout.marginTop = 1;
        subcatCompLayout.spacing = 5;
        subcatCompLayout.justify = true;

        this.setLayout(subcatCompLayout);

        mainCompLayout = new RowLayout(SWT.HORIZONTAL);
        majorCompLayout = new RowLayout(SWT.VERTICAL);
        minorCompLayout = new RowLayout(SWT.VERTICAL);

        // Layouts for components.
        mainCompLayout.spacing = 50;
        mainCompLayout.marginWidth = 8;

        majorCompLayout.marginLeft = 10;

        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 15;

        // Total HDD/CDD composite at top
        createTotalComp(this);

        // Composite for total HDD since July 1, CDD since Jan-1 at middle
        seasonComp = createSeasonComp(this);

        // Composite for earliest/latest freeze dates at bottom
        freezeComp = createFreezeComp(this);

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

        // Total heating degree days composite
        Composite totalHDDComp = new Composite(totalMainComp, SWT.NONE);
        totalHDDComp.setLayout(majorCompLayout);

        totalHDDMeasuredBtn = new Button(totalHDDComp, SWT.CHECK);
        totalHDDMeasuredBtn.setText("Heating Degree Days");
        totalHDDMeasuredBtn.setFont(majorFont);
        totalHDDMeasuredBtn.setSelection(true);

        totalHDDMinorComp = new Composite(totalHDDComp, SWT.NONE);
        totalHDDMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(totalHDDMeasuredBtn, totalHDDMinorComp);

        totalHDDDepartureBtn = new Button(totalHDDMinorComp, SWT.CHECK);
        totalHDDDepartureBtn.setText("Departure");

        totalHDDNormBtn = new Button(totalHDDMinorComp, SWT.CHECK);
        totalHDDNormBtn.setText("Normal");

        totalHDDLastYearBtn = new Button(totalHDDMinorComp, SWT.CHECK);
        totalHDDLastYearBtn.setText("Last Year's");

        totalHDDMonth2DateBtn = new Button(totalHDDMinorComp, SWT.CHECK);
        totalHDDMonth2DateBtn.setText("Month to Date");

        totalHDDSeason2DateBtn = new Button(totalHDDMinorComp, SWT.CHECK);
        totalHDDSeason2DateBtn.setText("Season to Date");

        totalHDDSinceJuly1Btn = new Button(totalHDDMinorComp, SWT.CHECK);
        totalHDDSinceJuly1Btn.setText("Since July 1");

        // Total cooling degree days composite
        Composite totalCDDComp = new Composite(totalMainComp, SWT.NONE);
        totalCDDComp.setLayout(majorCompLayout);

        totalCDDMeasuredBtn = new Button(totalCDDComp, SWT.CHECK);
        totalCDDMeasuredBtn.setText("Cooling Degree Days");
        totalCDDMeasuredBtn.setFont(majorFont);
        totalCDDMeasuredBtn.setSelection(true);

        totalCDDMinorComp = new Composite(totalCDDComp, SWT.NONE);
        totalCDDMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(totalCDDMeasuredBtn, totalCDDMinorComp);

        totalCDDDepartureBtn = new Button(totalCDDMinorComp, SWT.CHECK);
        totalCDDDepartureBtn.setText("Departure");

        totalCDDNormBtn = new Button(totalCDDMinorComp, SWT.CHECK);
        totalCDDNormBtn.setText("Normal");

        totalCDDLastYearBtn = new Button(totalCDDMinorComp, SWT.CHECK);
        totalCDDLastYearBtn.setText("Last Year's");

        totalCDDMonth2DateBtn = new Button(totalCDDMinorComp, SWT.CHECK);
        totalCDDMonth2DateBtn.setText("Month to Date");

        totalCDDSeason2DateBtn = new Button(totalCDDMinorComp, SWT.CHECK);
        totalCDDSeason2DateBtn.setText("Season to Date");

        totalCDDSinceJan1Btn = new Button(totalCDDMinorComp, SWT.CHECK);
        totalCDDSinceJan1Btn.setText("Since January 1");

        return totalMainComp;
    }

    /**
     * Create a composite at middle to elements for heating days from July 1st
     * and cooling days since Jan. 1st.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createSeasonComp(Composite parent) {

        Composite seasonMainComp;
        seasonMainComp = new Composite(parent, SWT.NONE);
        RowLayout seasonCompLayout = new RowLayout(SWT.HORIZONTAL);
        seasonCompLayout.spacing = 82;
        seasonCompLayout.marginWidth = 8;
        seasonMainComp.setLayout(seasonCompLayout);

        // Composite for total heating degree days since July 1st.
        Composite seasonHDDComp = new Composite(seasonMainComp, SWT.NONE);
        seasonHDDComp.setLayout(majorCompLayout);

        seasonHDDMeasuredBtn = new Button(seasonHDDComp, SWT.CHECK);
        seasonHDDMeasuredBtn.setText("HDD Since July 1");
        seasonHDDMeasuredBtn.setFont(majorFont);
        seasonHDDMeasuredBtn.setSelection(true);

        seasonHDDMinorComp = new Composite(seasonHDDComp, SWT.NONE);
        seasonHDDMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(seasonHDDMeasuredBtn, seasonHDDMinorComp);

        seasonHDDDepartureBtn = new Button(seasonHDDMinorComp, SWT.CHECK);
        seasonHDDDepartureBtn.setText("Departure");

        seasonHDDNormBtn = new Button(seasonHDDMinorComp, SWT.CHECK);
        seasonHDDNormBtn.setText("Normal");

        seasonHDDLastYearBtn = new Button(seasonHDDMinorComp, SWT.CHECK);
        seasonHDDLastYearBtn.setText("Last Year's");

        // Composite for cooling degree days since Jan. 1st
        Composite seasonCDDComp = new Composite(seasonMainComp, SWT.NONE);
        seasonCDDComp.setLayout(majorCompLayout);

        seasonCDDMeasuredBtn = new Button(seasonCDDComp, SWT.CHECK);
        seasonCDDMeasuredBtn.setText("CDD Since January 1");
        seasonCDDMeasuredBtn.setFont(majorFont);
        seasonCDDMeasuredBtn.setSelection(true);

        seasonCDDMinorComp = new Composite(seasonCDDComp, SWT.NONE);
        seasonCDDMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(seasonCDDMeasuredBtn, seasonCDDMinorComp);

        seasonCDDDepartureBtn = new Button(seasonCDDMinorComp, SWT.CHECK);
        seasonCDDDepartureBtn.setText("Departure");

        seasonCDDNormBtn = new Button(seasonCDDMinorComp, SWT.CHECK);
        seasonCDDNormBtn.setText("Normal");

        seasonCDDLastYearBtn = new Button(seasonCDDMinorComp, SWT.CHECK);
        seasonCDDLastYearBtn.setText("Last Year's");

        return seasonMainComp;
    }

    /**
     * Create a composite at bottom to hold earliest freezing date and latest
     * freezing date.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createFreezeComp(Composite parent) {

        RowLayout freezeMainCompLayout = new RowLayout(SWT.VERTICAL);
        freezeMainCompLayout.spacing = 5;
        freezeMainCompLayout.fill = true;
        // freezeMainCompLayout.marginWidth = 8;
        Composite freezeMainComp = new Composite(parent, SWT.NONE);
        freezeMainComp.setLayout(freezeMainCompLayout);

        Label freezeLbl = new Label(freezeMainComp, SWT.None);
        freezeLbl.setText("Freeze Dates");
        // freezeMainCompLayout.marginWidth = 8;
        freezeLbl.setAlignment(SWT.CENTER);

        RowLayout freezeCompLayout = new RowLayout(SWT.HORIZONTAL);
        freezeCompLayout.spacing = 90;
        freezeCompLayout.marginWidth = 8;

        Composite freezeComp = new Composite(freezeMainComp, SWT.NONE);
        freezeComp.setLayout(freezeCompLayout);

        // Composite for total heating degree days since July 1st.
        Composite earlyFreezeComp = new Composite(freezeComp, SWT.NONE);

        earlyFreezeComp.setLayout(majorCompLayout);

        earlyFreezeMeasuredBtn = new Button(earlyFreezeComp, SWT.CHECK);
        earlyFreezeMeasuredBtn.setText("Earliest Freeze");
        earlyFreezeMeasuredBtn.setFont(majorFont);
        earlyFreezeMeasuredBtn.setSelection(true);

        earlyFreezeMinorComp = new Composite(earlyFreezeComp, SWT.NONE);
        earlyFreezeMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(earlyFreezeMeasuredBtn, earlyFreezeMinorComp);

        earlyFreezeNormBtn = new Button(earlyFreezeMinorComp, SWT.CHECK);
        earlyFreezeNormBtn.setText("  - Normal");

        earlyFreezeRecordBtn = new Button(earlyFreezeMinorComp, SWT.CHECK);
        earlyFreezeRecordBtn.setText("  - Record");

        // Composite for cooling degree days since Jan. 1st
        Composite lateFreezeComp = new Composite(freezeComp, SWT.NONE);
        lateFreezeComp.setLayout(majorCompLayout);

        lateFreezeMeasuredBtn = new Button(lateFreezeComp, SWT.CHECK);
        lateFreezeMeasuredBtn.setText("Latest Freeze");
        lateFreezeMeasuredBtn.setFont(majorFont);
        lateFreezeMeasuredBtn.setSelection(true);

        lateFreezeMinorComp = new Composite(lateFreezeComp, SWT.NONE);
        lateFreezeMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(lateFreezeMeasuredBtn, lateFreezeMinorComp);

        lateFreezeNormBtn = new Button(lateFreezeMinorComp, SWT.CHECK);
        lateFreezeNormBtn.setText("  - Normal");

        lateFreezeRecordBtn = new Button(lateFreezeMinorComp, SWT.CHECK);
        lateFreezeRecordBtn.setText("  - Record");

        return freezeMainComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {

        updateButton(totalHDDNormBtn, !isNWR || !isDaily);
        updateButton(totalHDDLastYearBtn, !isNWR);
        updateButton(totalCDDNormBtn, !isNWR || !isDaily);
        updateButton(totalCDDLastYearBtn, !isNWR);
        updateButton(seasonHDDLastYearBtn, !isNWR);
        updateButton(seasonCDDLastYearBtn, !isNWR);
        updateButton(earlyFreezeRecordBtn, !isNWR);
        updateButton(lateFreezeRecordBtn, !isNWR);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        seasonComp.setVisible(!isDaily);
        freezeComp.setVisible(!isDaily);

        totalHDDMonth2DateBtn.setVisible(isDaily);
        totalHDDSeason2DateBtn.setVisible(isDaily);
        totalHDDSinceJuly1Btn.setVisible(isDaily);

        totalCDDMonth2DateBtn.setVisible(isDaily);
        totalCDDSeason2DateBtn.setVisible(isDaily);
        totalCDDSinceJan1Btn.setVisible(isDaily);

    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - DegreeDaysControlFlags
     */
    public DegreeDaysControlFlags getControlFlags() {
        DegreeDaysControlFlags flags = DegreeDaysControlFlags.getDefaultFlags();

        // These 4 are for all both NWR/NWWS & all types
        flags.getTotalHDD().setMeasured(totalHDDMeasuredBtn.getSelection());
        flags.getTotalHDD().setDeparture(totalHDDDepartureBtn.getSelection());

        flags.getTotalCDD().setMeasured(totalCDDMeasuredBtn.getSelection());
        flags.getTotalCDD().setDeparture(totalCDDDepartureBtn.getSelection());

        // Total HDD/CDD's "Norm" are needed for NWR period or NWWS only
        if (!isNWR || !isDaily) {
            flags.getTotalHDD().setNorm(totalHDDNormBtn.getSelection());
            flags.getTotalCDD().setNorm(totalCDDNormBtn.getSelection());
        }

        // Total HDD/CDD's "Last Year" are needed for NWWS only
        if (!isNWR) {
            flags.getTotalCDD().setLastYear(totalCDDLastYearBtn.getSelection());
            flags.getTotalHDD().setLastYear(totalHDDLastYearBtn.getSelection());
        }

        if (isDaily) {
            // These 6 are for daily type only
            flags.getTotalHDD()
                    .setTotalMonth(totalHDDMonth2DateBtn.getSelection());
            flags.getTotalHDD()
                    .setTotalSeason(totalHDDSeason2DateBtn.getSelection());
            flags.getTotalHDD()
                    .setTotalYear(totalHDDSinceJuly1Btn.getSelection());

            flags.getTotalCDD()
                    .setTotalMonth(totalCDDMonth2DateBtn.getSelection());
            flags.getTotalCDD()
                    .setTotalSeason(totalCDDSeason2DateBtn.getSelection());
            flags.getTotalCDD()
                    .setTotalYear(totalCDDSinceJan1Btn.getSelection());
        } else {

            // These are for period type only
            flags.getSeasonHDD()
                    .setMeasured(seasonHDDMeasuredBtn.getSelection());
            flags.getSeasonHDD()
                    .setDeparture(seasonHDDDepartureBtn.getSelection());
            flags.getSeasonHDD().setNorm(seasonHDDNormBtn.getSelection());

            flags.getSeasonCDD()
                    .setMeasured(seasonCDDMeasuredBtn.getSelection());
            flags.getSeasonCDD()
                    .setDeparture(seasonCDDDepartureBtn.getSelection());
            flags.getSeasonCDD().setNorm(seasonCDDNormBtn.getSelection());

            flags.getEarlyFreeze()
                    .setMeasured(earlyFreezeMeasuredBtn.getSelection());
            flags.getEarlyFreeze().setNorm(earlyFreezeNormBtn.getSelection());

            flags.getLateFreeze()
                    .setMeasured(lateFreezeMeasuredBtn.getSelection());
            flags.getLateFreeze().setNorm(lateFreezeNormBtn.getSelection());

            if (!isNWR) { // period NWWS only
                flags.getSeasonHDD()
                        .setLastYear(seasonHDDLastYearBtn.getSelection());
                flags.getSeasonCDD()
                        .setLastYear(seasonCDDLastYearBtn.getSelection());
                flags.getEarlyFreeze()
                        .setRecord(earlyFreezeRecordBtn.getSelection());
                flags.getLateFreeze()
                        .setRecord(lateFreezeRecordBtn.getSelection());
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

        DegreeDaysControlFlags flags = ctrlFlg.getDegreeDaysControl();

        // These 4 are for all both NWR/NWWS & all types
        totalHDDMeasuredBtn.setSelection(flags.getTotalHDD().isMeasured());
        totalHDDDepartureBtn.setSelection(flags.getTotalHDD().isDeparture());

        totalCDDMeasuredBtn.setSelection(flags.getTotalCDD().isMeasured());
        totalCDDDepartureBtn.setSelection(flags.getTotalCDD().isDeparture());

        // Total HDD/CDD's "Norm" are needed for NWR period or NWWS only
        if (!isNWR || !isDaily) {
            totalHDDNormBtn.setSelection(flags.getTotalHDD().isNorm());
            totalCDDNormBtn.setSelection(flags.getTotalCDD().isNorm());
        }

        // Total HDD/CDD's "Last Year" are needed for NWWS only
        if (!isNWR) {
            totalCDDLastYearBtn.setSelection(flags.getTotalCDD().isLastYear());
            totalHDDLastYearBtn.setSelection(flags.getTotalHDD().isLastYear());
        }

        if (isDaily) {
            // These 6 are for daily type only
            totalHDDMonth2DateBtn
                    .setSelection(flags.getTotalHDD().isTotalMonth());
            totalHDDSeason2DateBtn
                    .setSelection(flags.getTotalHDD().isTotalSeason());
            totalHDDSinceJuly1Btn
                    .setSelection(flags.getTotalHDD().isTotalYear());

            totalCDDMonth2DateBtn
                    .setSelection(flags.getTotalCDD().isTotalMonth());
            totalCDDSeason2DateBtn
                    .setSelection(flags.getTotalCDD().isTotalSeason());
            totalCDDSinceJan1Btn
                    .setSelection(flags.getTotalCDD().isTotalYear());
        } else {

            // These are for period type only
            seasonHDDMeasuredBtn
                    .setSelection(flags.getSeasonHDD().isMeasured());
            seasonHDDDepartureBtn
                    .setSelection(flags.getSeasonHDD().isDeparture());
            seasonHDDNormBtn.setSelection(flags.getSeasonHDD().isNorm());

            seasonCDDMeasuredBtn
                    .setSelection(flags.getSeasonCDD().isMeasured());
            seasonCDDDepartureBtn
                    .setSelection(flags.getSeasonCDD().isDeparture());
            seasonCDDNormBtn.setSelection(flags.getSeasonCDD().isNorm());

            earlyFreezeMeasuredBtn
                    .setSelection(flags.getEarlyFreeze().isMeasured());
            earlyFreezeNormBtn.setSelection(flags.getEarlyFreeze().isNorm());

            lateFreezeMeasuredBtn
                    .setSelection(flags.getLateFreeze().isMeasured());
            lateFreezeNormBtn.setSelection(flags.getLateFreeze().isNorm());

            if (!isNWR) { // period NWWS only
                seasonHDDLastYearBtn
                        .setSelection(flags.getSeasonHDD().isLastYear());
                seasonCDDLastYearBtn
                        .setSelection(flags.getSeasonCDD().isLastYear());
                earlyFreezeRecordBtn
                        .setSelection(flags.getEarlyFreeze().isRecord());
                lateFreezeRecordBtn
                        .setSelection(flags.getLateFreeze().isRecord());
            }
        }

    }

}