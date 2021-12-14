/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferenceEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences.PreferenceOptions;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.ChooseStormDialog;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.track.DisplayPartialBestTrackDialog;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Preferences
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2019 59177      dmanzella   initial creation
 * Feb 15, 2019 59913      dmanzella   save/load support for selections
 * Mar 25, 2019 61648      dmanzella   Best Track Segment functionality
 * Apr 04, 2019 62029      jwu         Overhaul & link to best track drawing.
 * May 03, 2019 62845      dmanzella   Buttons populate correctly
 * May 20, 2019 63773      dmanzella   Minor bug fix
 * Sep 16, 2019 68603      jwu         Check storm existence before redrawing..
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class PreferencesDialog extends OcpCaveSWTDialog {

    /**
     * Forecast track label line types in atcfsite.pref
     */
    private static final String SOLID = "solid";

    private static final String DASH = "dash";

    private static final String DOT = "dot";

    private static final String WEIGHT_AVG = "Weighted Avg";

    private static final String LEAST_SQUARE = "Least Squares";

    private AtcfProduct prd;

    private AtcfResource atcfResource;

    private Storm storm;

    private boolean isStorm;

    /**
     * Logo positions in atcfsite.pref
     */
    private static final String UPPERRIGHT = "upper right";

    private static final String UPPERLEFT = "upper left";

    private static final String LOWERRIGHT = "lower right";

    private static final String LOWERLEFT = "lower left";

    /**
     * Pulls the preferences from localization
     */
    private AtcfSitePreferences preferences;

    /**
     * Constructor
     *
     * @param shell
     */
    public PreferencesDialog(Shell shell) {
        super(shell);

        atcfResource = AtcfSession.getInstance().getAtcfResource();
        this.storm = AtcfSession.getInstance().getActiveStorm();
        setText("Preferences");
    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        if (storm != null) {
            prd = atcfResource.getResourceData().getAtcfProduct(storm);
        }
        createContents();
    }

    /**
     * Create contents.
     *
     * @param parent
     */
    protected void createContents() {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite mainComposite = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginWidth = 20;
        mainLayout.verticalSpacing = 15;
        mainComposite.setLayout(mainLayout);
        preferences = AtcfConfigurationManager.getInstance().getPreferences();

        createTrackPreferences(mainComposite);
        createObjAidPreferences(mainComposite);
        createControlButtons(mainComposite);

        scrollComposite.setContent(mainComposite);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setMinSize(
                mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Creates the main area of the GUI
     *
     * @param parent
     */
    protected void createTrackPreferences(Composite parent) {

        Group bTrkPrefGrp = new Group(parent, SWT.NONE);
        GridLayout bTrkPrefLayout = new GridLayout(2, false);
        bTrkPrefLayout.verticalSpacing = 10;
        bTrkPrefLayout.horizontalSpacing = 40;
        bTrkPrefLayout.marginWidth = 10;
        bTrkPrefGrp.setLayout(bTrkPrefLayout);
        bTrkPrefGrp.setText("Track Preferences");

        GridData btrkPrefGD = new GridData();
        btrkPrefGD.verticalAlignment = SWT.TOP;
        btrkPrefGD.horizontalAlignment = SWT.LEFT;
        bTrkPrefGrp.setLayoutData(btrkPrefGD);

        Composite bTrackOptionComp = new Composite(bTrkPrefGrp, SWT.NONE);
        bTrackOptionComp.setLayout(new GridLayout(1, false));
        isStorm = true;
        if (storm == null) {
            isStorm = false;
        }

        // btrack-special-sttype
        createCheckButton(bTrackOptionComp,
                PreferenceOptions.BTRACKSPECIALSTTYPE,
                isStorm ? prd.getBestTrackProperties().isSpecialTypePosition()
                        : preferences.getBtrackSpecialStype());

        // disp-storm-symbols
        createCheckButton(bTrackOptionComp, PreferenceOptions.DISPSTORMSYMBOLS,
                isStorm ? prd.getBestTrackProperties().isStormSymbols()
                        : preferences.getDispStormSymbols());

        // disp-storm-number
        createCheckButton(bTrackOptionComp, PreferenceOptions.DISPSTORMNUMBER,
                isStorm ? prd.getBestTrackProperties().isStormNumber()
                        : preferences.getDispStormNumber());

        // strack-solidDashDot
        createCheckButton(bTrackOptionComp, PreferenceOptions.BTRACKDASHNDOTSON,
                isStorm ? prd.getBestTrackProperties().isTrackLineTypes()
                        : preferences.getBtrackDashnDotsOn());

        // disp-tracklines-legend
        Button legendBtn = createCheckButton(bTrackOptionComp,
                PreferenceOptions.DISPTRACKLINESLEGEND,
                isStorm ? prd.getBestTrackProperties().isTrackLineLegend()
                        : preferences.getDispTracklinesLegend());
        GridData legendGridData = new GridData();
        legendGridData.horizontalIndent = 30;
        legendBtn.setLayoutData(legendGridData);

        // TODO implement track lines legend placement dialog?

        // strack-color-intensity
        createCheckButton(bTrackOptionComp,
                PreferenceOptions.BTRACKCOLORINTENSITY,
                isStorm ? prd.getBestTrackProperties().isColorsOnIntensity()
                        : preferences.getBtrackColorIntensity());

        // disp-track-color-leg
        Button colorLegendBtn = createCheckButton(bTrackOptionComp,
                PreferenceOptions.DISPTRACKCOLORLEG,
                isStorm ? prd.getBestTrackProperties().isIntensityColorLegend()
                        : preferences.getDispTrackColorLegend());
        GridData clrLegendGD = new GridData();
        clrLegendGD.horizontalIndent = 30;
        colorLegendBtn.setLayoutData(clrLegendGD);

        // TODO implement SS color legend placement dialog?

        // strack-colors-ss-scale
        createCheckButton(bTrackOptionComp,
                PreferenceOptions.STRACKCOLORSSSSCALE,
                isStorm ? prd.getBestTrackProperties().isColorsOnCategory()
                        : preferences.getStrackColorsSSScale());

        // disp-ss-color-legend
        Button ssColorLegendBtn = createCheckButton(bTrackOptionComp,
                PreferenceOptions.DISPSSCOLORLEGEND,
                isStorm ? prd.getBestTrackProperties().isCategoryColorLegend()
                        : preferences.getDispSSColorLegend());
        GridData ssLegendGD = new GridData();
        ssLegendGD.horizontalIndent = 30;
        ssColorLegendBtn.setLayoutData(ssLegendGD);

        // disp-track-labels
        createCheckButton(bTrackOptionComp, PreferenceOptions.DISPTRACKLABELS,
                isStorm ? prd.getBestTrackProperties().isTrackInfoLabel()
                        : preferences.getDispTrackLabels());

        // TODO implement label placement dialog?

        // btrack-intensities-on
        createCheckButton(bTrackOptionComp,
                PreferenceOptions.BTRACKINTENSITIESON,
                isStorm ? prd.getBestTrackProperties().isTrackIntensities()
                        : preferences.getBtrackIntensitiesOn());

        // btrack-labels-on
        createCheckButton(bTrackOptionComp, PreferenceOptions.BTRACKLABELSON,
                isStorm ? prd.getBestTrackProperties().isTrackLabels()
                        : preferences.getBtrackLabelsOn());

        // Best track segment
        Button bestSegmentBtn = new Button(bTrackOptionComp, SWT.NONE);
        bestSegmentBtn.setText("Best Track Segment ...");
        bestSegmentBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

                if (currentStorm == null) {
                    ChooseStormDialog chooseAStormDlg = new ChooseStormDialog(
                            getShell());
                    chooseAStormDlg.open();
                    currentStorm = AtcfSession.getInstance().getActiveStorm();
                }

                if (currentStorm != null) {
                    Map<String, List<BDeckRecord>> bdeckMap = AtcfDataUtil
                            .getBDeckRecords(currentStorm);

                    DisplayPartialBestTrackDialog bestTrack = new DisplayPartialBestTrackDialog(
                            shell, bdeckMap);
                    bestTrack.open();
                }
            }
        });

        // Composite for the obj track groups
        Composite objTrkComp = new Composite(bTrkPrefGrp, SWT.NONE);
        GridLayout objTrkCompLayout = new GridLayout(1, false);
        objTrkCompLayout.verticalSpacing = 30;
        objTrkComp.setLayout(objTrkCompLayout);

        GridData objTrkCompGD = new GridData();
        objTrkCompGD.verticalAlignment = SWT.TOP;
        objTrkCompGD.horizontalAlignment = SWT.RIGHT;
        objTrkComp.setLayoutData(objTrkCompGD);

        Group objTrkGrp = new Group(objTrkComp, SWT.NONE);
        objTrkGrp.setLayout(new RowLayout(SWT.VERTICAL));
        objTrkGrp.setText("Obj Track");
        createRadioButton(objTrkGrp, PreferenceOptions.OBJTRACK, LEAST_SQUARE,
                "0");
        createRadioButton(objTrkGrp, PreferenceOptions.OBJTRACK, WEIGHT_AVG,
                "1");

        Group objTrkIntGrp = new Group(objTrkComp, SWT.NONE);
        objTrkIntGrp.setLayout(new RowLayout(SWT.VERTICAL));
        objTrkIntGrp.setText("Obj Track Int");
        createRadioButton(objTrkIntGrp, PreferenceOptions.OBJTRACKINT,
                LEAST_SQUARE, "0");
        createRadioButton(objTrkIntGrp, PreferenceOptions.OBJTRACKINT,
                WEIGHT_AVG, "1");

        Group objTrkRadiiGrp = new Group(objTrkComp, SWT.NONE);
        objTrkRadiiGrp.setLayout(new RowLayout(SWT.VERTICAL));
        objTrkRadiiGrp.setText("Obj Track Radii");

        createRadioButton(objTrkRadiiGrp, PreferenceOptions.OBJTRACKRADII,
                LEAST_SQUARE, "0");
        createRadioButton(objTrkRadiiGrp, PreferenceOptions.OBJTRACKRADII,
                WEIGHT_AVG, "1");

    }

    /**
     * Creates the Obj Aid section of the dialog
     *
     * @param parent
     */
    protected void createObjAidPreferences(Composite parent) {

        Button fixButton = new Button(parent, SWT.NONE);
        fixButton.setText("Fix Options...");
        GridData fixGridData = new GridData();
        fixButton.setLayoutData(fixGridData);
        fixButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Open an instance of Display Fixes Dialog?
            }
        });

        // Obj Aid and forecast Group
        Group objAidGroup = new Group(parent, SWT.NONE);
        GridLayout objAidGrpLayout = new GridLayout(2, false);
        objAidGrpLayout.marginLeft = 10;
        objAidGrpLayout.marginRight = 105;
        objAidGrpLayout.horizontalSpacing = 10;
        objAidGroup.setLayout(objAidGrpLayout);
        objAidGroup.setText("Obj Aid and Forecast Track Preferences");

        // map-lines-bold?
        Button boldBtn = createCheckButton(objAidGroup,
                PreferenceOptions.MAPLINESBOLD,
                isStorm ? prd.getBestTrackProperties().isBoldLine()
                        : preferences.getMapLinesBold());
        GridData boldLineGD = new GridData();
        boldLineGD.horizontalSpan = 2;
        boldBtn.setLayoutData(boldLineGD);

        // Forecast track label line types
        Label fcstTrkLineLbl = new Label(objAidGroup, SWT.NONE);
        fcstTrkLineLbl.setText("Forecast Track label lines: ");

        Group fcstTrkLineTypGrp = new Group(objAidGroup, SWT.NONE);

        GridLayout fcstTrkLineTypGrpLayout = new GridLayout(3, true);
        fcstTrkLineTypGrpLayout.marginWidth = 10;
        fcstTrkLineTypGrpLayout.horizontalSpacing = 15;
        fcstTrkLineTypGrp.setLayout(fcstTrkLineTypGrpLayout);

        createRadioButton(fcstTrkLineTypGrp,
                PreferenceOptions.FCSTTRACKLABELTYPE, "solid line", SOLID);
        createRadioButton(fcstTrkLineTypGrp,
                PreferenceOptions.FCSTTRACKLABELTYPE, "dashed line", DASH);
        createRadioButton(fcstTrkLineTypGrp,
                PreferenceOptions.FCSTTRACKLABELTYPE, "dotted line", DOT);

    }

    /**
     * Creates the control buttons and the last set of radio buttons
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        // Bottom groups
        Composite btmCtrlComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlCompLayout = new GridLayout(2, false);
        ctrlCompLayout.horizontalSpacing = 35;
        ctrlCompLayout.marginWidth = 1;
        btmCtrlComp.setLayout(ctrlCompLayout);

        Composite leftRadioOptComp = new Composite(btmCtrlComp, SWT.NONE);
        leftRadioOptComp.setLayout(new GridLayout(2, false));

        // logo-position
        Label logoLbl = new Label(leftRadioOptComp, SWT.NONE);
        logoLbl.setText("Logo position: ");

        Group logoGrp = new Group(leftRadioOptComp, SWT.NONE);
        GridLayout logoGrpLayout = new GridLayout(2, true);
        logoGrpLayout.horizontalSpacing = 15;
        logoGrpLayout.marginWidth = 10;
        logoGrp.setLayout(logoGrpLayout);

        createRadioButton(logoGrp, PreferenceOptions.LOGOPOSITION, UPPERLEFT,
                UPPERLEFT);
        createRadioButton(logoGrp, PreferenceOptions.LOGOPOSITION, UPPERRIGHT,
                UPPERRIGHT);
        createRadioButton(logoGrp, PreferenceOptions.LOGOPOSITION, LOWERLEFT,
                LOWERLEFT);
        createRadioButton(logoGrp, PreferenceOptions.LOGOPOSITION, LOWERRIGHT,
                LOWERRIGHT);

        /*
         * latlon-line-freq: lat/lon line frequency: "0" - default, "1" - less,
         * "2" - more
         */
        Label frqLbl = new Label(leftRadioOptComp, SWT.NONE);
        frqLbl.setText("Frequency of lat/lon lines: ");

        Group frqGrp = new Group(leftRadioOptComp, SWT.NONE);
        GridLayout frqGrpLayout = new GridLayout(3, false);
        frqGrpLayout.horizontalSpacing = 15;
        frqGrpLayout.marginWidth = 10;
        frqGrp.setLayout(frqGrpLayout);

        createRadioButton(frqGrp, PreferenceOptions.LATLONLINEFREQ, "less",
                "1");
        createRadioButton(frqGrp, PreferenceOptions.LATLONLINEFREQ, "default",
                "0");
        createRadioButton(frqGrp, PreferenceOptions.LATLONLINEFREQ, "more",
                "2");

        /*
         * map-lines-width: if map-lines-bold, width of map lines in pixels "2"
         * "4"? "6"? "8"?
         */
        Label boldnessLbl = new Label(leftRadioOptComp, SWT.NONE);
        boldnessLbl.setText("Boldness of map boundaries: ");

        Group boldnessGrp = new Group(leftRadioOptComp, SWT.NONE);
        GridLayout boldnessGrpLayout = new GridLayout(4, false);
        boldnessGrpLayout.horizontalSpacing = 15;
        boldnessGrpLayout.marginWidth = 10;
        boldnessGrp.setLayout(boldnessGrpLayout);

        createRadioButton(boldnessGrp, PreferenceOptions.MAPLINESWIDTH, "thin",
                "2");
        createRadioButton(boldnessGrp, PreferenceOptions.MAPLINESWIDTH, "bold",
                "4");
        createRadioButton(boldnessGrp, PreferenceOptions.MAPLINESWIDTH,
                "bolder", "6");
        createRadioButton(boldnessGrp, PreferenceOptions.MAPLINESWIDTH,
                "boldest", "8");

        // geography-labels
        createCheckButton(leftRadioOptComp, PreferenceOptions.GEOGRAPHYLABELS,
                false);

        // Control buttons
        Composite ctrlBtnComp = new Composite(btmCtrlComp, SWT.NONE);
        GridLayout ctrlBtnCompLayout = new GridLayout(1, false);
        ctrlBtnCompLayout.marginTop = 10;
        ctrlBtnCompLayout.verticalSpacing = 10;
        ctrlBtnComp.setLayout(ctrlBtnCompLayout);

        GridData ctrlBtnGD = new GridData();
        ctrlBtnGD.verticalAlignment = SWT.TOP;
        ctrlBtnComp.setLayoutData(ctrlBtnGD);

        AtcfVizUtil.createHelpButton(ctrlBtnComp, AtcfVizUtil.buttonGridData());

        Button applyButton = new Button(ctrlBtnComp, SWT.NONE);
        applyButton.setLayoutData(AtcfVizUtil.buttonGridData());
        applyButton.setText("Apply");
        applyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfConfigurationManager.getInstance()
                        .saveAtcfSitePreferences(preferences);
            }
        });

        Button okButton = new Button(ctrlBtnComp, SWT.NONE);
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.setText("OK");
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfConfigurationManager.getInstance()
                        .saveAtcfSitePreferences(preferences);
                close();
            }
        });

        Button cancelButton = new Button(ctrlBtnComp, SWT.NONE);
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /**
     * Create a check button for a given preference options.
     *
     * @param parent
     *            Parent composite
     * @param ps
     *            PreferenceOptions
     * @return Button
     */
    private Button createCheckButton(Composite parent, PreferenceOptions ps,
            boolean checked) {

        Button btn = new Button(parent, SWT.CHECK);
        btn.setText(ps.getDescription());
        btn.setData(ps);

        AtcfSitePreferenceEntry pEntry = preferences
                .getPreference(ps.toString());

        String val = pEntry.getValue();
        btn.setSelection(Boolean.valueOf(val));

        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean sel = btn.getSelection();

                preferences.getPreference(ps.toString())
                        .setValue(Boolean.toString(sel).toUpperCase());

                AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
                Storm activeStorm = rsc.getResourceData().getActiveStorm();

                if (activeStorm != null) {
                    BestTrackGenerator.redraw(ps, sel);
                }
            }
        });

        if (prd != null && (prd.getBestTrackProperties() != null)) {
            btn.setSelection(checked);
        }

        return btn;
    }

    /**
     * Create a radio button for a given preference options.
     *
     * @param parent
     *            Parent composite
     * @param ps
     *            PreferenceOptions
     * @param title
     *            Title for the button
     * @param data
     *            Data string for the button
     *
     * @return Button
     */
    private Button createRadioButton(Composite parent, PreferenceOptions ps,
            String title, String data) {

        Button btn = new Button(parent, SWT.RADIO);
        if (title != null) {
            btn.setText(title);
        } else {
            btn.setText(ps.getDescription());
        }

        btn.setData(ps);

        AtcfSitePreferenceEntry pEntry = preferences
                .getPreference(ps.toString());

        String val = pEntry.getValue();
        boolean sel = val.equals(data);
        btn.setSelection(sel);

        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (btn.getSelection()) {
                    preferences.getPreference(ps.toString()).setValue(data);
                }
            }
        });

        return btn;
    }

}