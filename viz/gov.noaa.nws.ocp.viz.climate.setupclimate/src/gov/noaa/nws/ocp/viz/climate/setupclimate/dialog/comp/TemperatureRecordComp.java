/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.TempRecordControlFlags;

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
 * 
 * </pre>
 * 
 * @author jwu
 */
public class TemperatureRecordComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    // Buttons for max temperature record
    private Button maxTempNormBtn;

    private Button maxTempRecordBtn;

    private Button maxTempYearBtn;

    // Buttons for min temperature record
    private Button minTempNormBtn;

    private Button minTempRecordBtn;

    private Button minTempYearBtn;

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
    public TemperatureRecordComp(Composite parent, int style,
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
        mainCompLayout.spacing = 80;
        mainCompLayout.marginWidth = 8;
        mainCompLayout.marginLeft = 80;

        // majorCompLayout.spacing = 5;
        majorCompLayout.marginLeft = 15;

        // Composite for maximum/minimum temperature record
        createTotalComp(this);
    }

    /**
     * Create a composite to hold elements for max/min temperature record.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite recordMainComp;
        recordMainComp = new Composite(parent, SWT.NONE);
        recordMainComp.setLayout(mainCompLayout);

        // Maximum temperature record composite
        Composite maxRecComp = new Composite(recordMainComp, SWT.NONE);
        maxRecComp.setLayout(majorCompLayout);

        Label maxLbl = new Label(maxRecComp, SWT.None);
        maxLbl.setText("Maximum");
        maxLbl.setFont(majorFont);
        maxLbl.setAlignment(SWT.CENTER);

        Label maxSeparator = new Label(maxRecComp,
                SWT.HORIZONTAL | SWT.SEPARATOR);
        maxSeparator.setLayoutData(new RowData(75, 15));

        maxTempNormBtn = new Button(maxRecComp, SWT.CHECK);
        maxTempNormBtn.setText("Normal");
        maxTempNormBtn.setSelection(true);

        maxTempRecordBtn = new Button(maxRecComp, SWT.CHECK);
        maxTempRecordBtn.setText("Record");
        maxTempRecordBtn.setSelection(true);

        maxTempYearBtn = new Button(maxRecComp, SWT.CHECK);
        maxTempYearBtn.setText("  - Year");
        maxTempYearBtn.setSelection(true);

        // Minimum temperature record composite
        Composite minRecComp = new Composite(recordMainComp, SWT.NONE);
        minRecComp.setLayout(majorCompLayout);

        Label minLbl = new Label(minRecComp, SWT.None);
        minLbl.setText("Minimum");
        minLbl.setFont(majorFont);
        minLbl.setAlignment(SWT.CENTER);

        Label minSeparator = new Label(minRecComp,
                SWT.HORIZONTAL | SWT.SEPARATOR);
        minSeparator.setLayoutData(new RowData(75, 15));

        minTempNormBtn = new Button(minRecComp, SWT.CHECK);
        minTempNormBtn.setText("Normal");
        minTempNormBtn.setSelection(true);

        minTempRecordBtn = new Button(minRecComp, SWT.CHECK);
        minTempRecordBtn.setText("Record");
        minTempRecordBtn.setSelection(true);

        minTempYearBtn = new Button(minRecComp, SWT.CHECK);
        minTempYearBtn.setText("  - Year");
        minTempYearBtn.setSelection(true);

        return recordMainComp;
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
     * @return getControlFlags() - TempRecordControlFlags
     */
    public TempRecordControlFlags getControlFlags() {
        TempRecordControlFlags flags = TempRecordControlFlags.getDefaultFlags();

        // Only update for daily type
        if (isDaily) {
            flags.setMaxTempNorm(maxTempNormBtn.getSelection());
            flags.setMaxTempRecord(maxTempRecordBtn.getSelection());
            flags.setMaxTempYear(maxTempYearBtn.getSelection());
            flags.setMinTempNorm(minTempNormBtn.getSelection());
            flags.setMinTempRecord(minTempRecordBtn.getSelection());
            flags.setMinTempYear(minTempYearBtn.getSelection());
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

        TempRecordControlFlags flags = ctrlFlg.getTempRecordControl();

        // Only update for daily type
        if (isDaily) {
            maxTempNormBtn.setSelection(flags.isMaxTempNorm());
            maxTempRecordBtn.setSelection(flags.isMaxTempRecord());
            maxTempYearBtn.setSelection(flags.isMaxTempYear());
            minTempNormBtn.setSelection(flags.isMinTempNorm());
            minTempRecordBtn.setSelection(flags.isMinTempRecord());
            minTempYearBtn.setSelection(flags.isMinTempYear());
        }

    }

}