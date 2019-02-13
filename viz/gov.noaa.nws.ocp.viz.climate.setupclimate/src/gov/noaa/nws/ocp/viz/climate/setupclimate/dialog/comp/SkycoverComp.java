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
import gov.noaa.nws.ocp.common.localization.climate.producttype.SkycoverControlFlags;

/**
 * Composite to contain all sub-categories for climate sky cover in setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * 16 JUN 2017  33104      amoore       Spelling correction.
 * 06 NOV 2018  55207      jwu          Enable some legacy behavior(DR 20889).
 * </pre>
 * 
 * @author jwu
 */
public class SkycoverComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Buttons for possible sunshine
    protected Composite possSunshineMinorComp;

    private Button possSunshineBtn;

    // Buttons for number of days with fair sky (0-0.3)
    private Button fairDaysBtn;

    // Buttons for number of days with partly cloudy sky (0.4-0.7)
    private Button partlyCloudyDaysBtn;

    // Buttons for number of days with cloudy sky (0.8-1.0)
    private Button cloudyDaysBtn;

    // Buttons for average sky cover
    private Button avgSkycoverBtn;

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
    public SkycoverComp(Composite parent, int style,
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
        mainCompLayout.spacing = 40;
        mainCompLayout.marginWidth = 8;

        majorCompLayout.marginLeft = 50;

        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 25;

        // Possible sunshine & average sky cover composite
        createTotalComp(this);
    }

    /**
     * Create a composite at top to hold elements for possible sunshine &
     * average sky cover.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite skycoverMainComp;
        skycoverMainComp = new Composite(parent, SWT.NONE);
        skycoverMainComp.setLayout(mainCompLayout);

        // Possible sunshine composite
        Composite possSunshineComp = new Composite(skycoverMainComp, SWT.NONE);
        possSunshineComp.setLayout(majorCompLayout);

        possSunshineBtn = new Button(possSunshineComp, SWT.CHECK);
        possSunshineBtn.setText("Possible Sunshine");
        possSunshineBtn.setFont(majorFont);
        possSunshineBtn.setSelection(false);

        possSunshineMinorComp = new Composite(possSunshineComp, SWT.NONE);
        possSunshineMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(possSunshineBtn, possSunshineMinorComp);

        fairDaysBtn = new Button(possSunshineMinorComp, SWT.CHECK);
        fairDaysBtn.setText("Fair Days");

        partlyCloudyDaysBtn = new Button(possSunshineMinorComp, SWT.CHECK);
        partlyCloudyDaysBtn.setText("Partly Cloudy Days");

        cloudyDaysBtn = new Button(possSunshineMinorComp, SWT.CHECK);
        cloudyDaysBtn.setText("Cloudy Days");

        // Average sky Cover composite
        Composite avgSkyComp = new Composite(skycoverMainComp, SWT.NONE);
        avgSkyComp.setLayout(majorCompLayout);

        avgSkycoverBtn = new Button(avgSkyComp, SWT.CHECK);
        avgSkycoverBtn.setText("Average Sky Cover");
        avgSkycoverBtn.setFont(majorFont);
        avgSkycoverBtn.setSelection(false);

        return skycoverMainComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
        updateButton(possSunshineBtn, !isNWR);
        updateButton(fairDaysBtn, !isNWR);
        updateButton(partlyCloudyDaysBtn, !isNWR);
        updateButton(cloudyDaysBtn, !isNWR);
        updateButton(avgSkycoverBtn, !isNWR);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {

        if (!isDaily && !isNWR && possSunshineBtn.getSelection()) {
            possSunshineMinorComp.setVisible(true);
        } else {
            possSunshineMinorComp.setVisible(false);
        }

        if (isDaily) {
            avgSkycoverBtn.setText("Sky Cover");
        } else {
            avgSkycoverBtn.setText("Average Sky Cover");
        }

        this.layout(true, true);
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - SkycoverControlFlags
     */
    public SkycoverControlFlags getControlFlags() {
        SkycoverControlFlags flags = SkycoverControlFlags.getDefaultFlags();

        // Only for NWWS
        if (!isNWR) {
            flags.setPossSunshine(possSunshineBtn.getSelection());
            flags.setAvgSkycover(avgSkycoverBtn.getSelection());

            // These three are only for period NWWS
            if (!isDaily) {
                flags.setFairDays(fairDaysBtn.getSelection());
                flags.setPartlyCloudyDays(partlyCloudyDaysBtn.getSelection());
                flags.setCloudyDays(cloudyDaysBtn.getSelection());
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

        SkycoverControlFlags flags = ctrlFlg.getSkycoverControl();

        // Only for NWWS
        if (!isNWR) {
            possSunshineBtn.setSelection(flags.isPossSunshine());
            avgSkycoverBtn.setSelection(flags.isAvgSkycover());

            // These three are only for period NWWS
            if (!isDaily) {
                fairDaysBtn.setSelection(flags.isFairDays());
                partlyCloudyDaysBtn.setSelection(flags.isPartlyCloudyDays());
                cloudyDaysBtn.setSelection(flags.isCloudyDays());
            }
        }
    }

}