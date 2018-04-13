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
import gov.noaa.nws.ocp.common.localization.climate.producttype.SunriseNsetControlFlags;

/**
 * Composite to contain all sub-categories for climate temperature record in
 * setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * 17 MAY 2017  33104      amoore       FindBugs. Package reorg.
 * </pre>
 * 
 * @author jwu
 */
public class SunriseSunsetComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    // Buttons
    private Button sunriseBtn;

    private Button sunsetBtn;

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
    public SunriseSunsetComp(Composite parent, int style,
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

        // Layouts for components.
        mainCompLayout.marginWidth = 8;
        mainCompLayout.marginLeft = 150;

        majorCompLayout.spacing = 5;
        majorCompLayout.marginLeft = 15;

        // Sunrise/sunset composite
        createTotalComp(this);

    }

    /**
     * Create a composite to hold elements for sunrise/sunset.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite sunMainComp;
        sunMainComp = new Composite(parent, SWT.NONE);
        sunMainComp.setLayout(mainCompLayout);

        // Maximum temperature record composite
        Composite sunriseNsetComp = new Composite(sunMainComp, SWT.NONE);
        sunriseNsetComp.setLayout(majorCompLayout);

        sunriseBtn = new Button(sunriseNsetComp, SWT.CHECK);
        sunriseBtn.setText("Sunrise");
        sunriseBtn.setSelection(true);

        sunsetBtn = new Button(sunriseNsetComp, SWT.CHECK);
        sunsetBtn.setText("Sunset");
        sunsetBtn.setSelection(true);

        return sunMainComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - SunriseNsetControlFlags
     */
    public SunriseNsetControlFlags getControlFlags() {
        SunriseNsetControlFlags flags = SunriseNsetControlFlags
                .getDefaultFlags();

        // Only update for daily type
        if (isDaily) {
            flags.setSunrise(sunriseBtn.getSelection());
            flags.setSunset(sunsetBtn.getSelection());
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

        SunriseNsetControlFlags flags = ctrlFlg.getSunControl();

        // Only update for daily type
        if (isDaily) {
            sunriseBtn.setSelection(flags.isSunrise());
            sunsetBtn.setSelection(flags.isSunset());
        }
    }

}