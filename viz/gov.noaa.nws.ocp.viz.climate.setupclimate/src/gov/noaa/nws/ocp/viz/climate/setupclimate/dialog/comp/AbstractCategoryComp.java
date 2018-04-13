/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;

/**
 * Super class for all composites to hold the sub-categories for each category
 * in climate setup GUI.
 * 
 * Notes:
 * 
 * 1. The Normal of a weather element must be selected before the Departure from
 * Normal can be selected. The rule applies to Daily NWR reports only - if
 * "Departure" is selected, "Normal" should be automatically selected. If
 * "Normal" is de-selected, "Departure" should be automatically de-selected.
 * This rule is used in "Temperature", "Precipitation" and "Snowfall".
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 14 DEC 2016  20640      jwu         Initial creation
 * 09 FEB 2017  20640      jwu         Use ClimateGlobal for preference values.
 * 17 MAY 2017  33104      amoore      FindBugs. Package reorg.
 * 30 AUG 2017  37472     jwu         Do fixed GUI adjustment at the end.
 * 
 * </pre>
 * 
 * @author jwu
 */

public abstract class AbstractCategoryComp extends Composite {

    /**
     * Font for the title of major sub-categories.
     */
    protected Font majorFont;

    /**
     * User-defined preferences, including thresholds for:
     * 
     * Temperature - (T1, T2, T3, T4, T4, T6). Precipitation - ( P1, P2 ). Snow
     * - ( S1 ).
     */
    protected ClimateGlobal preferenceValues;

    /**
     * Default string to be used when a threshold value is not defined.
     */
    protected static final String DEF_THRESHOLD_STR = "??";

    /**
     * Flag to indicate if current report source is NWR (otherwise, NWWS).
     */
    protected boolean isNWR;

    /**
     * Flag to indicate if current report type is daily (otherwise, period).
     */
    protected boolean isDaily;

    /**
     * Map to hold each sub-category's major button and its minor composite.
     */
    protected Map<Button, Composite> subCategoryMap;

    /**
     * Constructor
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
    public AbstractCategoryComp(Composite parent, int style,
            ClimateGlobal preferenceValues, Font majorFont) {
        super(parent, style);

        this.preferenceValues = preferenceValues;
        this.majorFont = majorFont;

        subCategoryMap = new HashMap<Button, Composite>();

        initializeComponents();
    }

    /**
     * @return the majorFont
     */
    public Font getMajorFont() {
        return majorFont;
    }

    /**
     * @param majorFont
     *            the majorFont to set
     */
    public void setMajorFont(Font majorFont) {
        this.majorFont = majorFont;
    }

    /**
     * @return the preferenceValues
     */
    public ClimateGlobal getPreferenceValues() {
        return preferenceValues;
    }

    /**
     * @param preferenceValues
     *            the preferenceValues to set
     */
    public void setPreferenceValues(ClimateGlobal preferenceValues) {
        this.preferenceValues = preferenceValues;
    }

    /**
     * Display this composite based on report source and type.
     */
    public void display(boolean isNWR, boolean isDaily, boolean includeAll,
            boolean includeNone) {

        // Set flags
        this.isNWR = isNWR;
        this.isDaily = isDaily;

        // Update button status if includeAll or includeNone is activated.
        if (includeAll) {
            selectAllButtons();
        } else if (includeNone) {
            deselectAllButtons();
        }

        // Now go through each minor composites to see if it should be visible.
        showComposites();

        /*
         * Do fixed GUI adjustments based on source (NWR/NWWS) and period type
         * (daily/period)
         */
        switchBetweenNWRnNWWS();
        switchBetweenDailyPeriod();
    }

    /**
     * Select all enabled buttons (e.g, when "Include All" is activated).
     */
    public void selectAllButtons() {
        selectButtons(this, true);
    }

    /**
     * De-select all enabled buttons (e.g, when "Include None" is activated).
     */
    public void deselectAllButtons() {
        selectButtons(this, false);
    }

    /**
     * Enable all buttons in the composite.
     */
    public void enableAllButtons() {
        enableButtons(this, true);
    }

    /**
     * Disable all buttons in the composite.
     */
    public void disableAllButtons() {
        enableButtons(this, false);
    }

    /**
     * Select/de-select all enabled buttons in a Composite.
     * 
     * @param comp
     *            composite.
     * @param select
     *            boolean true - select; false - de-select.
     */
    protected void selectButtons(Composite comp, boolean select) {

        if (comp != null && !comp.isDisposed()) {
            Control[] wids = comp.getChildren();

            if (wids != null) {
                for (int kk = 0; kk < wids.length; kk++) {
                    if (wids[kk] instanceof Composite) {
                        selectButtons((Composite) wids[kk], select);
                    } else if (wids[kk] instanceof Button) {
                        if (((Button) wids[kk]).isEnabled()) {
                            ((Button) wids[kk]).setSelection(select);
                        } else {
                            ((Button) wids[kk]).setSelection(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enable/disable all buttons in a Composite.
     * 
     * @param comp
     *            composite.
     * @param select
     *            boolean true - enable; false - disable.
     */
    protected void enableButtons(Composite comp, boolean enable) {

        if (comp != null && !comp.isDisposed()) {
            Control[] wids = comp.getChildren();

            if (wids != null) {
                for (int kk = 0; kk < wids.length; kk++) {
                    if (wids[kk] instanceof Composite) {
                        enableButtons((Composite) wids[kk], enable);
                    } else if (wids[kk] instanceof Button)
                        ((Button) wids[kk]).setEnabled(enable);
                }
            }
        }
    }

    /**
     * Create/initialize components.
     */
    protected void initializeComponents() {
    }

    /**
     * Do GUI adjustments when switching between NWR and NWWS.
     */
    protected abstract void switchBetweenNWRnNWWS();

    /**
     * Do GUI adjustments when switching between daily and period type.
     */
    protected abstract void switchBetweenDailyPeriod();

    /**
     * Set flags on the GUI based on a given ClimateProductControl.
     * 
     * @param isNWR
     *            boolean - if source is NWR or NWWS
     * @param isDaily
     *            boolean - if the period type is daily or period.
     * @param ctrlFlg
     *            ClimateProductControl
     */
    public abstract void setControlFlags(boolean isNWR, boolean isDaily,
            ClimateProductControl ctrlFlg);

    /**
     * @return control flags object, type depending on component.
     */
    public abstract Object getControlFlags();

    /**
     * Show/hide minor composites based on the status of their associated major
     * sub-category buttons.
     */
    protected void showComposites() {
        for (Entry<Button, Composite> entry : subCategoryMap.entrySet()) {
            entry.getValue().setVisible(entry.getKey().isEnabled()
                    && entry.getKey().getSelection());
        }
    }

    /**
     * Enable/disable a given button and de-select it if it is going to be
     * disabled..
     * 
     * @param btn
     *            Button.
     * @param select
     *            boolean true - enable; false - disable.
     */
    protected void updateButton(Button btn, boolean enabled) {
        btn.setEnabled(enabled);
        if (!enabled) {
            btn.setSelection(enabled);
        }
    }

    /**
     * Update preferenceValues and associated GUI changes. The sub-classes could
     * override this method to update GUI for user-defined thresholds.
     * 
     * @param preferenceValues
     *            A ClimateGlobal for the new preferenceValues
     */
    public void updatePreferences(ClimateGlobal preferenceValues) {
        setPreferenceValues(preferenceValues);
    }

}
