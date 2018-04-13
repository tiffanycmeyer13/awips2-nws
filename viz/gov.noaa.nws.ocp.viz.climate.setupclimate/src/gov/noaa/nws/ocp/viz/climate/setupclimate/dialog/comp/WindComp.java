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
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.WindControlFlags;

/**
 * Composite to contain all sub-categories for climate wind in setup GUI.
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
public class WindComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    private RowLayout minorCompLayout;

    // Buttons for maximum sustained wind
    protected Composite maxWindMinorComp;

    private Button maxWindMeasuredBtn;

    private Button maxWindTimeOfMeasuredBtn;

    // Buttons for maximum wind gust
    protected Composite maxGustMinorComp;

    private Button maxGustMeasuredBtn;

    private Button maxGustTimeOfMeasuredBtn;

    // Buttons for resultant wind
    private Button resultWindMeasuredBtn;

    // Buttons for mean wind speed
    private Button meanWindMeasuredBtn;

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
    public WindComp(Composite parent, int style, ClimateGlobal preferenceValues,
            Font majorFont) {

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
        mainCompLayout.spacing = 60;
        mainCompLayout.marginWidth = 8;

        majorCompLayout.marginLeft = 35;

        minorCompLayout.spacing = 0;
        minorCompLayout.marginLeft = 15;

        // Maximum/Gust composite at top
        createTotalComp(this);

        // Resultant wind at middle
        Composite resultWindComp = new Composite(this, SWT.NONE);
        RowLayout resultWindLayout = new RowLayout(SWT.VERTICAL);
        resultWindLayout.marginLeft = 170;
        resultWindComp.setLayout(resultWindLayout);

        resultWindMeasuredBtn = new Button(resultWindComp, SWT.CHECK);
        resultWindMeasuredBtn.setText("Resultant");
        resultWindMeasuredBtn.setFont(majorFont);
        resultWindMeasuredBtn.setSelection(true);

        // Average Speed at bottom
        Composite meanWindComp = new Composite(this, SWT.NONE);
        meanWindComp.setLayout(resultWindLayout);
        meanWindMeasuredBtn = new Button(meanWindComp, SWT.CHECK);
        meanWindMeasuredBtn.setText("Average Speed");
        meanWindMeasuredBtn.setFont(majorFont);
        meanWindMeasuredBtn.setSelection(true);

    }

    /**
     * Create a composite at top to hold elements for maximum wind and wind
     * gust.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createTotalComp(Composite parent) {

        Composite windMainComp;
        windMainComp = new Composite(parent, SWT.NONE);
        windMainComp.setLayout(mainCompLayout);

        // Maximum wind composite
        Composite maxWindComp = new Composite(windMainComp, SWT.NONE);
        maxWindComp.setLayout(majorCompLayout);

        maxWindMeasuredBtn = new Button(maxWindComp, SWT.CHECK);
        maxWindMeasuredBtn.setText("Maximum");
        maxWindMeasuredBtn.setFont(majorFont);
        maxWindMeasuredBtn.setSelection(true);

        maxWindMinorComp = new Composite(maxWindComp, SWT.NONE);
        maxWindMinorComp.setLayout(minorCompLayout);

        subCategoryMap.put(maxWindMeasuredBtn, maxWindMinorComp);

        maxWindTimeOfMeasuredBtn = new Button(maxWindMinorComp, SWT.CHECK);
        maxWindTimeOfMeasuredBtn.setText("Time/Date\nof Occurrence");

        // Clicking on "major" button will show/hide all items in it.
        maxWindMeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                // enableButtons(minorComp, false);
                maxWindMinorComp.setVisible(selected);
                maxWindMinorComp.layout(true, true);
            }
        });

        // Maximum gust composite
        Composite maxGustComp = new Composite(windMainComp, SWT.NONE);
        maxGustComp.setLayout(majorCompLayout);

        maxGustMeasuredBtn = new Button(maxGustComp, SWT.CHECK);
        maxGustMeasuredBtn.setText("Maximum Gust");
        maxGustMeasuredBtn.setFont(majorFont);
        maxGustMeasuredBtn.setSelection(true);

        maxGustMinorComp = new Composite(maxGustComp, SWT.NONE);
        maxGustMinorComp.setLayout(minorCompLayout);
        subCategoryMap.put(maxGustMeasuredBtn, maxGustMinorComp);

        maxGustTimeOfMeasuredBtn = new Button(maxGustMinorComp, SWT.CHECK);
        maxGustTimeOfMeasuredBtn.setText("Time/Date\nof Occurrence");
        maxGustTimeOfMeasuredBtn.setAlignment(SWT.TOP);

        // Clicking on "major" button will show/hide all items in it.
        maxGustMeasuredBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = ((Button) event.widget).getSelection();
                // enableButtons(minorComp, false);
                maxGustMinorComp.setVisible(selected);
                maxGustMinorComp.layout(true, true);
            }
        });

        return windMainComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
        updateButton(maxWindTimeOfMeasuredBtn, isNWR || !isDaily);
        updateButton(maxGustTimeOfMeasuredBtn, isNWR || !isDaily);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        updateButton(resultWindMeasuredBtn, !isDaily || !isNWR);
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags()
     */
    public WindControlFlags getControlFlags() {
        WindControlFlags flags = WindControlFlags.getDefaultFlags();

        flags.getMaxWind().setMeasured(maxWindMeasuredBtn.getSelection());
        flags.getMaxGust().setMeasured(maxGustMeasuredBtn.getSelection());

        // Time of measurement are needed only for NWR or period type
        if (isNWR || !isDaily) {
            flags.getMaxWind()
                    .setTimeOfMeasured(maxWindTimeOfMeasuredBtn.getSelection());
            flags.getMaxGust()
                    .setTimeOfMeasured(maxGustTimeOfMeasuredBtn.getSelection());

        }

        // Resultant wind is needed only for NWWS or period type
        if (!isDaily || !isNWR) {
            flags.getResultWind()
                    .setMeasured(resultWindMeasuredBtn.getSelection());
        }

        flags.getMeanWind().setMeasured(meanWindMeasuredBtn.getSelection());

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

        WindControlFlags flags = ctrlFlg.getWindControl();

        maxWindMeasuredBtn.setSelection(flags.getMaxWind().isMeasured());
        maxGustMeasuredBtn.setSelection(flags.getMaxGust().isMeasured());

        // Time of measurement are needed only for NWR or period type
        if (isNWR || !isDaily) {
            maxWindTimeOfMeasuredBtn
                    .setSelection(flags.getMaxWind().isTimeOfMeasured());
            maxGustTimeOfMeasuredBtn
                    .setSelection(flags.getMaxGust().isTimeOfMeasured());

        }

        // Resultant wind is needed only for NWWS or period type
        if (!isDaily || !isNWR) {
            resultWindMeasuredBtn
                    .setSelection(flags.getResultWind().isMeasured());
        }

        meanWindMeasuredBtn.setSelection(flags.getMeanWind().isMeasured());
    }

}