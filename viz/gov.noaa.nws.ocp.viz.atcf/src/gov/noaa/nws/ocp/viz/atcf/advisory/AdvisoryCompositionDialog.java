/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences;
import gov.noaa.nws.ocp.common.atcf.configuration.ForecasterInitials;
import gov.noaa.nws.ocp.common.atcf.configuration.GeographyPoint;
import gov.noaa.nws.ocp.common.atcf.configuration.GeographyPoints;
import gov.noaa.nws.ocp.common.atcf.configuration.StormStates;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckinForecastTrackRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.DeleteForecastTrackRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewForecastTrackRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ReadAdvisoryFileRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryInfo;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisorySummary;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryTimer;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackProperties;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.textedit.AtcfTextEditing;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Dialog for "Advisory=>Advisory Composition".
 *
 * This function is designed for use by National Hurricane Center (NHC). The
 * user enters the information asked for in the dialog, such as advisory number,
 * AWIPS bin number, forecast type, etc. The user may review and/or change the
 * advisory forecast data by clicking on the "Advisory Data" button. The "Edit
 * Warning" button will launch editor, allowing the user to edit the current
 * storm warning.
 *
 * When "Ok" is clicked on, the advisory info is reformatted into ATCF files.
 * The NHC forecast is added to the objective aids file, and the best track is
 * updated with the initial location. At this point the four advisory products
 * are created.
 *
 * If "Cancel" is clicked on, all changes will be discarded and the dialog will
 * be closed.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2018 51856      wpaintsil   Initial creation.
 * Apr 24, 2020 77847      jwu         Implement sub-dialog "Advisory Data".
 * Sep 08, 2020 82576      jwu         Add geography points & forecaster initials.
 * Oct 19, 2020 82721      jwu         Save advisory info & generate TCP etc.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Jan 26, 2021 86746      jwu         Move ICAO here & add more logic.
 * Feb 12, 2021 87783      jwu         Update TCM etc.
 * Feb 26, 2021 85386      wpaintsil   Add TCD (Tropical Cyclone Discussion)
 * Mar 04, 2021 88931      jwu         Add forecast/outlook range.
 * Mar 17, 2021 88583      jwu         Implement special advisory.
 * Mar 22, 2021 88518      dfriedman   Rework product headers.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class AdvisoryCompositionDialog extends OcpCaveChangeTrackDialog
        implements IAdvisoryDataListener {

    // Format strings
    private static final String BIN_NUMBER_FORMAT = "%1$10s";

    private static final String CENTER_ENTRY_FORMAT = "%1$8s";

    /*
     * Maximum awips bin number allowed - this is the last character used in the
     * 10-character PIL and legacy ATCF allows up to 5.
     */
    private static final int MAX_AWIPS_BIN_NUMBER = 5;

    /*
     * Special Advisory time - starting from 3 hours later from current DTG per
     * interval. Currently, the interval is every 30 minutes and up to 9 entries
     * (8 hours after the current DTG)
     */
    // # of special adv entries.
    private static final int SPECIAL_ADV_NUM = 9;

    private static final int SPECIAL_ADV_INTERVAL = 30; // Minutes

    private static final int DEFAULT_SPECIAL_ADV_HOUR = 6; // Hours

    private Map<String, Integer> specialAdvTimeEntries;

    // Default selection when special adv is off
    private int dfltAdvIndex;

    // Entries for surface pressure
    private static final String[] SURFACE_PRESSURE = AtcfVizUtil.getEntries(850,
            1050, 1, "%7d");

    // Entries for center accuracy
    private static final String[] CENTER_ACCURACY = AtcfVizUtil.getEntries(0,
            120, 5, CENTER_ENTRY_FORMAT);

    // Forecast Initials
    private ForecasterInitials initials;

    // Flag to turn on TAU 60.
    private boolean useTau60 = true;

    // Ending TAUs for forecast & outlook in advisory.
    private int forecastRange = 72;

    private int outlookRange = 120;

    // Special TAU if "Special Advisory" is On.
    private int specialTau = 3;

    // Current storm && DTG.
    private Storm storm;

    private String stormId;

    private String currentDTG;

    private AdvisoryInfo advInfo;

    private String warnings;

    private String headline;

    private String hazards;

    // Storm states.
    private StormStates stmStates;

    // Geographic points.
    private GeographyPoints geoPoints;

    // Eye diameter.
    private int eyeDiameter;

    // Daylight saving based on storm location.
    private boolean isDaylightSaving;

    // TAUs
    private List<AtcfTaus> workingTaus;

    // Current AtcfResource.
    private AtcfResource drawingLayer;

    // Original forecast track records for current storm.
    private Map<String, List<ForecastTrackRecord>> fcstTrackData;

    // Backup of original forecast track records for current storm.
    private Map<String, List<ForecastTrackRecord>> fcstTrackDataCopy;

    /*
     * Working copy of forecast track records - only contains info that will
     * could be used or edited in this dialog.
     */
    private Map<ForecastTrackRecord, ForecastTrackRecord> fcstTrackEditMap;

    // Working copy of forecast track records sorted by RecordKey.
    private Map<RecordKey, ForecastTrackRecord> fcstTrackRecordMap;

    // Best track records for current storm.
    private Map<String, List<BDeckRecord>> currentBDeckRecords;

    // Sandbox ID.
    private int fcstTrackSandBoxID;

    // Data widgets
    private Button specialAdvChkBtn;

    private CCombo fcstInitialsCmb;

    private Text advisoryNumTxt;

    private Button potentialTCChkBtn;

    private CCombo surfacePresCmb;

    private CCombo centerAccuracyCmb;

    private CCombo eyeDiameterCmb;

    private AdvisoryForecastTypeDialog advFcstTypeDlg;

    private Label advTimeLbl;

    private CCombo specialAdvTimeCmb;

    private Button daylightChkBtn;

    private CCombo awipsBinCmb;

    private CCombo geoRefCmb1;

    private CCombo geoRefCmb2;

    private Button sixHrBtn;

    private Button threeHrBtn;

    private Button usWarnChkBtn;

    private Button intlWarnChkBtn;

    private Button lastAdvChk;

    private Button advDataBtn;

    /*
     * Button to generate intermediate reports when both this button is selected
     * along with "3 hourly" button.
     */
    private Button intermediateBtn;

    /**
     * @param parent
     */
    public AdvisoryCompositionDialog(Shell parent, Storm storm) {
        super(parent);

        this.storm = storm;

        // Lower case ID to match legacy.
        this.stormId = storm.getStormId().toLowerCase();

        // Retrieve forecaster initials.
        initials = AtcfConfigurationManager.getInstance()
                .getForecasterInitials();

        // Retrieve storm states.
        stmStates = AtcfConfigurationManager.getInstance().getStormStates()
                .getAvailableStormStates();

        // Retrieve geography points for the basin.
        geoPoints = AtcfConfigurationManager.getInstance()
                .getBasinGeoPoints(storm.getRegion().toLowerCase());

        AtcfSitePreferences prefs = AtcfConfigurationManager.getInstance()
                .getPreferences();

        // Retrieve flag for TAU 60 & forecast/outlook ranges.
        useTau60 = prefs.getUseTau60();
        forecastRange = prefs.getAdvisoryForecastRange();
        outlookRange = prefs.getAdvisoryOutlookRange();

        initialize();
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite topComposite = new Composite(scrollComposite, SWT.NONE);
        GridLayout topLayout = new GridLayout(1, false);
        topComposite.setLayout(topLayout);
        GridData topLayoutData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        topComposite.setLayoutData(topLayoutData);

        createMainComposite(topComposite);
        createControlButtons(topComposite);
        populateForecastInfo();

        scrollComposite.setContent(topComposite);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    /**
     * Create most of the widgets.
     *
     * @param mainComp
     */
    private void createMainComposite(Composite parent) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, true);

        mainComp.setLayout(mainLayout);
        mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label stormLabel = new Label(mainComp, SWT.NONE);
        stormLabel.setText(String.format("Tropical Cyclone %10s   on %12s",
                storm.getStormId(), currentDTG));

        stormLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Composite forecastTypeComp = new Composite(mainComp, SWT.NONE);
        GridLayout forecastTypeGL = new GridLayout(2, true);
        forecastTypeComp.setLayout(forecastTypeGL);
        forecastTypeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite specialAdvComp = new Composite(forecastTypeComp, SWT.NONE);
        GridLayout specialAdvGL = new GridLayout(3, false);
        specialAdvComp.setLayout(specialAdvGL);

        specialAdvChkBtn = new Button(specialAdvComp, SWT.CHECK);
        specialAdvChkBtn.setText("Special Advisory");
        GridData specialAdvGD = new GridData();
        specialAdvGD.horizontalSpan = 3;
        specialAdvChkBtn.setLayoutData(specialAdvGD);
        specialAdvChkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSpecialAdvisory();
            }
        });

        Label forecasterIntialsLbl = new Label(specialAdvComp, SWT.NONE);
        forecasterIntialsLbl.setText("Forecaster Initials");
        fcstInitialsCmb = new CCombo(specialAdvComp,
                SWT.BORDER | SWT.READ_ONLY);
        GridData forecasterIntialsGD = new GridData();
        forecasterIntialsGD.horizontalSpan = 2;
        fcstInitialsCmb.setLayoutData(forecasterIntialsGD);
        fcstInitialsCmb
                .setItems(initials.getInitials().toArray(new String[] {}));

        Label advisoryNumLbl = new Label(specialAdvComp, SWT.NONE);
        advisoryNumLbl.setText("Advisory number");
        advisoryNumTxt = new Text(specialAdvComp, SWT.BORDER | SWT.READ_ONLY);
        GridData advisoryNumGD = new GridData();
        advisoryNumGD.horizontalSpan = 2;
        advisoryNumTxt.setLayoutData(advisoryNumGD);

        potentialTCChkBtn = new Button(specialAdvComp, SWT.CHECK);
        potentialTCChkBtn.setText("Potential TC");
        GridData potentialTCGD = new GridData();
        potentialTCGD.horizontalSpan = 3;
        potentialTCChkBtn.setLayoutData(potentialTCGD);

        Label surfacePresLbl = new Label(specialAdvComp, SWT.NONE);
        surfacePresLbl.setText("Surface Pressure");
        surfacePresCmb = new CCombo(specialAdvComp, SWT.BORDER);
        surfacePresCmb.setItems(SURFACE_PRESSURE);

        new Label(specialAdvComp, SWT.NONE).setText("mb");

        Label centerAccuracyLbl = new Label(specialAdvComp, SWT.NONE);
        centerAccuracyLbl.setText("Center Accuracy");
        centerAccuracyCmb = new CCombo(specialAdvComp,
                SWT.BORDER | SWT.READ_ONLY);
        centerAccuracyCmb.setItems(CENTER_ACCURACY);

        new Label(specialAdvComp, SWT.NONE).setText("nm");

        Label eyeDiameterLbl = new Label(specialAdvComp, SWT.NONE);
        eyeDiameterLbl.setText("Eye Diameter");
        eyeDiameterCmb = new CCombo(specialAdvComp, SWT.BORDER | SWT.READ_ONLY);
        for (int ii = 0; ii <= 120; ii += 5) {
            eyeDiameterCmb.add(String.format(CENTER_ENTRY_FORMAT, ii));
        }
        eyeDiameterCmb.select(0);

        new Label(specialAdvComp, SWT.NONE).setText("nm");

        eyeDiameterCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String str = eyeDiameterCmb.getText();
                eyeDiameter = getValue(str, eyeDiameter);
            }
        });

        Button fcstTypeBtn = new Button(specialAdvComp, SWT.PUSH);
        fcstTypeBtn.setText("Forecast type...");
        fcstTypeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (advFcstTypeDlg == null) {
                    advFcstTypeDlg = new AdvisoryForecastTypeDialog(shell,
                            getStormStateTaus(), advInfo);
                }

                advFcstTypeDlg.open();
            }
        });

        Composite advTimeComp = new Composite(forecastTypeComp, SWT.NONE);
        GridLayout advTimeGL = new GridLayout(2, false);
        advTimeComp.setLayout(advTimeGL);
        GridData advTimeGD = new GridData(SWT.RIGHT, SWT.TOP, true, false);
        advTimeComp.setLayoutData(advTimeGD);

        advTimeLbl = new Label(advTimeComp, SWT.NONE);
        advTimeLbl.setText("Time of Advisory");
        specialAdvTimeCmb = new CCombo(advTimeComp, SWT.BORDER | SWT.READ_ONLY);
        specialAdvTimeCmb.setItems(
                specialAdvTimeEntries.keySet().toArray(new String[] {}));
        specialAdvTimeCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String str = specialAdvTimeCmb.getText();
                Integer hr = specialAdvTimeEntries.get(str);
                if (hr != null) {
                    specialTau = hr;
                    updateAdvDataBtnStatus();
                }
            }
        });

        daylightChkBtn = new Button(advTimeComp, SWT.CHECK);
        daylightChkBtn.setText("DayLight Time");
        GridData daylightGD = new GridData();
        daylightGD.horizontalSpan = 2;
        daylightGD.horizontalAlignment = SWT.RIGHT;
        daylightChkBtn.setLayoutData(daylightGD);
        daylightChkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Check the daylight saving mode change is appropriate.
                boolean onOff = daylightChkBtn.getSelection();
                String dtsInOut = (isDaylightSaving) ? "in" : "not in";
                String dtsOnOff = (onOff) ? "on" : "off";
                if (isDaylightSaving != onOff) {
                    boolean confirmed = MessageDialog.openQuestion(shell,
                            "Change daysight saving mode",
                            "Looks like " + currentDTG + " is actually "
                                    + dtsInOut
                                    + " daylight saving.\nAre you sure you want to turn it "
                                    + dtsOnOff
                                    + "? It will affect the issue time for all advisories.");

                    if (confirmed) {
                        daylightChkBtn.setSelection(onOff);
                    } else {
                        daylightChkBtn.setSelection(!onOff);
                    }
                }
            }
        });

        Label awipsBinLbl = new Label(advTimeComp, SWT.NONE);
        awipsBinLbl.setText("AWIPS bin number");
        awipsBinCmb = new CCombo(advTimeComp, SWT.BORDER | SWT.READ_ONLY);
        for (int ii = 1; ii <= MAX_AWIPS_BIN_NUMBER; ii++) {
            awipsBinCmb.add(String.format(BIN_NUMBER_FORMAT, ii));
        }

        awipsBinCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean confirmed = MessageDialog.openQuestion(shell,
                        "Change AWIPS Bin Number",
                        "Are you sure you want to change bin #?");

                if (confirmed) {
                    awipsBinCmb.setText(String.format(BIN_NUMBER_FORMAT,
                            advInfo.getAwipsBinNum()));
                }
            }
        });

        Composite geographyComp = new Composite(mainComp, SWT.NONE);
        GridLayout geographyGL = new GridLayout(2, false);
        geographyGL.horizontalSpacing = 0;
        geographyComp.setLayout(geographyGL);
        geographyComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        String geographyString = "Geography Reference";
        Label geoRefLbl1 = new Label(geographyComp, SWT.NONE);
        geoRefLbl1.setText(geographyString);
        geoRefCmb1 = new CCombo(geographyComp, SWT.BORDER | SWT.READ_ONLY);
        geoRefCmb1.add("none");
        for (GeographyPoint gpts : geoPoints.getGeoPoints()) {
            geoRefCmb1.add(gpts.toString());
        }

        Label geoRefLbl2 = new Label(geographyComp, SWT.NONE);
        geoRefLbl2.setText(geographyString);
        geoRefCmb2 = new CCombo(geographyComp, SWT.BORDER | SWT.READ_ONLY);
        geoRefCmb2.add("none");
        for (GeographyPoint gpts : geoPoints.getGeoPoints()) {
            geoRefCmb2.add(gpts.toString());
        }

        Group advIntComp = new Group(geographyComp, SWT.NONE);
        advIntComp.setText("Public advisory frequency");
        GridLayout advIntGL = new GridLayout(2, true);
        advIntComp.setLayout(advIntGL);
        GridData advIntGD = new GridData();
        advIntGD.horizontalSpan = 2;
        advIntComp.setLayoutData(advIntGD);

        Composite advFreqComp = new Composite(advIntComp, SWT.NONE);
        GridLayout advFreqGL = new GridLayout(2, true);
        advFreqGL.horizontalSpacing = 10;
        advFreqGL.marginWidth = 5;
        advFreqComp.setLayout(advFreqGL);

        sixHrBtn = new Button(advFreqComp, SWT.RADIO);
        sixHrBtn.setText("6 hourly");
        sixHrBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                intermediateBtn.setSelection(false);
            }
        });
        sixHrBtn.setSelection(true);

        threeHrBtn = new Button(advFreqComp, SWT.RADIO);
        threeHrBtn.setText("3 hourly");
        threeHrBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                intermediateBtn.setSelection(true);
            }
        });

        intermediateBtn = new Button(advIntComp, SWT.CHECK);
        intermediateBtn.setToolTipText(
                "Select to generate intermiate public advisory A.");
        intermediateBtn.setText("Intermediate");
        intermediateBtn.setLayoutData(
                new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
        intermediateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sixHrBtn.setSelection(false);
                threeHrBtn.setSelection(true);
            }
        });

        Composite watchWarnComp = new Composite(mainComp, SWT.NONE);
        GridLayout watchWarnGL = new GridLayout(1, false);
        watchWarnGL.marginLeft = 20;
        watchWarnComp.setLayout(watchWarnGL);

        usWarnChkBtn = new Button(watchWarnComp, SWT.CHECK);
        usWarnChkBtn.setText("US Watches/Warnings");

        intlWarnChkBtn = new Button(watchWarnComp, SWT.CHECK);
        intlWarnChkBtn.setText("International Watches/Warnings");

        lastAdvChk = new Button(watchWarnComp, SWT.CHECK);
        lastAdvChk.setText("Last Advisory");

        Composite actionBtnComp = new Composite(mainComp, SWT.NONE);
        GridLayout actionBtnCompGL = new GridLayout(4, true);
        actionBtnCompGL.marginLeft = 15;
        actionBtnCompGL.horizontalSpacing = 25;
        actionBtnComp.setLayout(actionBtnCompGL);

        advDataBtn = new Button(actionBtnComp, SWT.PUSH);
        advDataBtn.setText("Advisory Data...");
        advDataBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        advDataBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AdvisoryDataDialog advDataDlg = new AdvisoryDataDialog(
                        AdvisoryCompositionDialog.this.getShell(),
                        AdvisoryCompositionDialog.this);

                // Open "Advisory Data" dialog
                advDataDlg.open();
            }
        });

        updateAdvDataBtnStatus();

        Button editWarningBtn = new Button(actionBtnComp, SWT.PUSH);
        editWarningBtn.setText("Edit Warning...");
        editWarningBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        editWarningBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                warnings = AdvisoryUtil.readWarnings(stormId);
                AtcfTextEditing.editProduct(stormId, warnings,
                        AdvisoryType.WARNINGS);
            }
        });

        Button editHeadlineBtn = new Button(actionBtnComp, SWT.PUSH);
        editHeadlineBtn.setText("Edit Headline...");
        editHeadlineBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        editHeadlineBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                headline = AdvisoryUtil.readHeadline(stormId);
                AtcfTextEditing.editProduct(stormId, headline,
                        AdvisoryType.HEADLINE);
            }
        });

        Button editHazardsBtn = new Button(actionBtnComp, SWT.PUSH);
        editHazardsBtn.setText("Edit Hazards...");
        editHazardsBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        editHazardsBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hazards = AdvisoryUtil.readHazards(stormId);
                AtcfTextEditing.editProduct(stormId, hazards,
                        AdvisoryType.HAZARDS);
            }
        });

        // Initialize data from last-saved stormId.adv
        initializeAdvisoryInfo(advInfo);

    }

    /**
     * Create bottom buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 45;

        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        Button okButton = new Button(buttonComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());

        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateData();

                close();
            }
        });

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        cancelButton.setFocus();
    }

    /**
     * Update data for OK Button.
     */
    private void updateData() {
        // Check if data exists
        boolean hasSpecialTauData = hasSpecialTauData();
        if (!hasSpecialTauData) {
            Shell sh = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            String msg = "No advisories will be generated for " + stormId
                    + " at " + currentDTG
                    + " since no forecast data found for special TAU "
                    + advInfo.getSpecialTAU() + ".";
            MessageDialog.openWarning(sh,
                    "Warning - Advisory Composition Dialog", msg);

            logger.warn("AdvisoryCompositionDialog: " + msg);
        } else {
            // Save/submit & reset ATCF resource
            submitForecast();

            // Update A-Deck.
            updateADeckData();

            // Redraw forecast track?
            drawForecastTrack();

            // Generate Advisories.
            generateAdvisories();
        }
    }

    /*
     * Initialize data.
     */
    private void initialize() {

        // All TAUs to be shown.
        workingTaus = AtcfTaus.getAdvisoryTaus();

        // Get the current AtcfResource.
        drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Retrieve B-Deck data for current storm.
        currentBDeckRecords = AtcfDataUtil.getBDeckRecords(storm, false);

        // Find the latest DTG from B-Deck
        List<String> dtgList = new ArrayList<>(
                currentBDeckRecords.keySet());
        currentDTG = dtgList.get(dtgList.size() - 1);

        // Retrieve forecast track records for the current storm
        fcstTrackData = AtcfDataUtil.getFcstTrackRecords(storm, true);

        // The first DTG in forecast should be the same as current DTG.
        String fcstDTG = null;
        if (fcstTrackData != null && !fcstTrackData.isEmpty()) {
            for (String fdtg : fcstTrackData.keySet()) {
                fcstDTG = fdtg;
                break;
            }
        }

        // Only start advisory when having forecast..
        if (fcstDTG != null) {

            /*
             * TODO Normally, DTG from latest best track should be the same as
             * the first DTG in forecast track. If somehow they do not match, we
             * use the first DTG in forecast track? Need further test on this
             * assumption.
             */
            if (!fcstDTG.equals(currentDTG)) {
                currentDTG = fcstDTG;
            }

            // Copy forecast track records and sort into a RecordKey map.
            fcstTrackRecordMap = new HashMap<>();
            fcstTrackEditMap = new HashMap<>();

            fcstTrackDataCopy = new LinkedHashMap<>();

            for (Map.Entry<String, List<ForecastTrackRecord>> entry : fcstTrackData
                    .entrySet()) {
                List<ForecastTrackRecord> frecs1 = new ArrayList<>();
                for (ForecastTrackRecord fRec : entry.getValue()) {
                    ForecastTrackRecord nrec = new ForecastTrackRecord(fRec);
                    int windRad = (int) fRec.getRadWind();
                    RecordKey key = new RecordKey("FCST", currentDTG,
                            fRec.getFcstHour(), windRad);
                    fcstTrackEditMap.put(nrec, fRec);
                    fcstTrackRecordMap.put(key, nrec);

                    frecs1.add(fRec);
                }

                fcstTrackDataCopy.put(entry.getKey(), frecs1);
            }

            // Update dialog title.
            setText("NHC Advisory Composition - " + storm.getStormName() + " "
                    + storm.getStormId() + " - " + currentDTG.substring(4));

            // Display forecast track.
            drawForecastTrack();

            // Get current forecast track sandbox ID.
            fcstTrackSandBoxID = drawingLayer.getResourceData()
                    .getFcstTrackSandbox(storm);
        }
        // Calculate special advisory time entries.
        specialAdvTimeEntries = getSpecialAdvEntries(currentDTG);
        dfltAdvIndex = getDfltSpecialAdvIndex(specialAdvTimeEntries,
                DEFAULT_SPECIAL_ADV_HOUR);

        // Load info for stormId.adv.
        advInfo = readAdvisoryInfo(stormId);

        advInfo.setStorm(storm);
        advInfo.setDtg(currentDTG);

        // Check daylight saving from storm info, not from stormId.adv
        AdvisoryTimer advTimer = new AdvisoryTimer(currentDTG, advInfo,
                fcstTrackData, useTau60);
        isDaylightSaving = advTimer.isDaylightSaving();
    }

    /*
     * Draw forecast track.
     */
    private void drawForecastTrack() {
        AtcfProduct prd = drawingLayer.getResourceData().getAtcfProduct(storm);
        if (prd.getForecastTrackLayer().getDrawables().isEmpty()
                || !fcstTrackData.isEmpty()) {

            prd.setFcstTrackDataMap(fcstTrackData);

            FcstTrackProperties fcstProp = prd.getFcstTrackProperties();
            if (fcstProp == null) {
                fcstProp = new FcstTrackProperties();
                prd.setFcstTrackProperties(fcstProp);
            }

            FcstTrackGenerator fstTrackGen = new FcstTrackGenerator(
                    drawingLayer, fcstProp, storm);
            fstTrackGen.create(true);
        }
    }

    /**
     * This method to receive notification of data change in "Advisory Data"
     * dialog.
     */
    @Override
    public void advisoryDataChanged(Map<RecordKey, ForecastTrackRecord> recMap,
            boolean update) {

        // Bring up the dialog.
        this.bringToTop();

        // Update the data.
        if (update) {
            List<ForecastTrackRecord> recs = new ArrayList<>();

            fcstTrackRecordMap.clear();
            fcstTrackEditMap.clear();

            /*
             * Note: fcstTrackRecordMap may have TAU 3 & another special Tau
             * data
             */
            for (Map.Entry<RecordKey, ForecastTrackRecord> entry : recMap
                    .entrySet()) {
                // For special Advisory, exclude TAU 3 data.
                int tau = entry.getKey().getTau();
                if (tau > 0 && tau < 9 && tau != specialTau) {
                    continue;
                }

                ForecastTrackRecord orec = entry.getValue();
                recs.add(orec);

                ForecastTrackRecord nrec = new ForecastTrackRecord(orec);
                fcstTrackEditMap.put(nrec, orec);
                fcstTrackRecordMap.put(entry.getKey(), nrec);
            }

            fcstTrackData = AtcfDataUtil.sortForecastTrackDataInDTGs(recs,
                    false);

            AtcfVizUtil.setDefaultBackground(advDataBtn);
        }
    }

    /*
     * Find the forecast track record stored in the map for a TAU/Wind Radii.
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @param radii WindRadii
     *
     * @return ForecastTrackRecord
     */
    private ForecastTrackRecord getForecastByTauNRadii(int tau,
            WindRadii radii) {
        RecordKey key = new RecordKey("FCST", currentDTG, tau,
                radii.getValue());
        return fcstTrackRecordMap.get(key);
    }

    /*
     * Find the forecast track record stored in the map for a TAU/Wind Radii
     * value.
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @param radii WindRadii value
     *
     * @return ForecastTrackRecord
     */
    private ForecastTrackRecord getForecastByTauNRadii(int tau, int radii) {
        RecordKey key = new RecordKey("FCST", currentDTG, tau, radii);
        return fcstTrackRecordMap.get(key);
    }

    /*
     * Populate dialog with existing forecast data.
     */
    private void populateForecastInfo() {

        // Get Forecast Record for surface (TAU 3).
        ForecastTrackRecord rec = getForecastByTauNRadii(specialTau,
                WindRadii.RADII_34_KNOT);

        // Use TAU 0 if no Forecast Record for TAU 3.
        if (rec == null) {
            rec = getForecastByTauNRadii(AtcfTaus.TAU0.getValue(),
                    WindRadii.RADII_34_KNOT);
        }

        // Populate values for those in forecast track records
        if (rec != null) {
            int ind = initials.getInitials().indexOf(rec.getForecaster());
            if (ind >= 0) {
                fcstInitialsCmb.select(ind);
            } else {
                fcstInitialsCmb.setText(rec.getForecaster());
            }

            surfacePresCmb.setText(String.format("%7d", (int) rec.getMslp()));
            eyeDiameterCmb.setText(
                    String.format(CENTER_ENTRY_FORMAT, (int) rec.getEyeSize()));
        }

    }

    /*
     * Reads stormId.adv file into Advisory Instance.
     *
     * @param storm Storm
     *
     * @return AdvisoryInfo
     */
    @SuppressWarnings("unchecked")
    private AdvisoryInfo readAdvisoryInfo(String stormId) {
        AdvisoryInfo curAdvInfo = new AdvisoryInfo();
        try {
            ReadAdvisoryFileRequest req = new ReadAdvisoryFileRequest(stormId,
                    AdvisoryType.TCP_ADV.getSuffix());
            List<String> fileInfo = (List<String>) ThriftClient
                    .sendRequest(req);
            curAdvInfo = AdvisoryInfo.construct(storm, fileInfo, geoPoints);
        } catch (Exception e) {
            logger.warn("AdvisoryCompsitionDialog - Failed to read .... "
                    + (stormId + AdvisoryType.TCP_ADV.getSuffix())
                    + ". Default will be used.", e);
        }

        // Initialize storm states to "Normal".
        if (curAdvInfo.getForecastType().isEmpty()) {
            for (AtcfTaus tau : getStormStateTaus()) {
                curAdvInfo.getForecastType()
                        .add(stmStates.getStormStatebyName("Normal").getId());
                curAdvInfo.getTaus().add(tau.getTime());
            }
        }

        return curAdvInfo;
    }

    /*
     * Initialize data selection on the GUI based on saved stormId.adv.
     *
     * @param storm Storm
     *
     * @return AdvisoryInfo
     */
    private void initializeAdvisoryInfo(AdvisoryInfo adv) {

        /*
         * Always start with "Special Advisory" off and "Time of Advisory"
         * deactivated at the time 6 hours later (next normal adv time).
         */
        specialAdvChkBtn.setSelection(false);
        advTimeLbl.setEnabled(false);
        specialAdvTimeCmb.setEnabled(false);
        specialAdvTimeCmb.select(dfltAdvIndex);

        fcstInitialsCmb.setText(adv.getForecaster());
        advisoryNumTxt.setText(Integer.toString(adv.getAdvNum()));
        potentialTCChkBtn.setSelection(adv.isPotentialTC());
        surfacePresCmb.setText(String.format("%7d", adv.getPres()));
        centerAccuracyCmb.setText(
                String.format(CENTER_ENTRY_FORMAT, adv.getCenterAccuracy()));

        // Info in advFcstTypeDlg is updated by itself.

        // Initialize the daylight saving based on storm DTG & location.
        daylightChkBtn.setSelection(isDaylightSaving);
        adv.setDaylightSaving(isDaylightSaving);

        awipsBinCmb.setText(
                String.format(BIN_NUMBER_FORMAT, adv.getAwipsBinNum()));

        // Note - the first item in these comboes is "none", not in geoPoints.
        geoRefCmb1
                .select(geoPoints.getGeoPoints().indexOf(adv.getGeoRef1()) + 1);
        geoRefCmb2
                .select(geoPoints.getGeoPoints().indexOf(adv.getGeoRef2()) + 1);

        sixHrBtn.setSelection(adv.getFrequency() == 6);
        threeHrBtn.setSelection(adv.getFrequency() == 3);
        adv.setFrequency(sixHrBtn.getSelection() ? 6 : 3);
        usWarnChkBtn.setSelection(adv.isWwUS());
        intlWarnChkBtn.setSelection(adv.isWwIntl());
        lastAdvChk.setSelection(adv.isFinalAdv());
    }

    /*
     * Update advisory information from GUI.
     *
     * @param adv AdvisoryInfo
     */
    private void updateAdvisoryInfo(AdvisoryInfo adv) {

        adv.setSpecialAdv(specialAdvChkBtn.getSelection());
        adv.setSpecialAdvTime(specialAdvTimeCmb.getText());

        // Special TAU -- Default to 3 when "Special Advisory" is off.
        int spTau = 3;
        if (specialAdvChkBtn.getSelection()) {
            String str = specialAdvTimeCmb.getText();
            Integer hr = specialAdvTimeEntries.get(str);
            if (hr != null) {
                spTau = hr;
            }
        }
        adv.setSpecialTAU(spTau);

        adv.setForecaster(fcstInitialsCmb.getText());
        adv.setAdvNum(getValue(advisoryNumTxt.getText(), adv.getAdvNum()));
        adv.setPotentialTC(potentialTCChkBtn.getSelection());
        adv.setPres(getValue(surfacePresCmb.getText(), adv.getPres()));
        adv.setCenterAccuracy(
                getValue(centerAccuracyCmb.getText(), adv.getCenterAccuracy()));

        adv.setSpecialAdvTime(specialAdvTimeCmb.getText());
        adv.setDaylightSaving(daylightChkBtn.getSelection());

        adv.setAwipsBinNum(
                getValue(awipsBinCmb.getText(), adv.getAwipsBinNum()));
        adv.setGeoRef1(getGeoReference(geoRefCmb1));
        adv.setGeoRef2(getGeoReference(geoRefCmb2));

        adv.setFrequency(sixHrBtn.getSelection() ? 6 : 3);
        adv.setWwUS(usWarnChkBtn.getSelection());
        adv.setWwIntl(intlWarnChkBtn.getSelection());
        adv.setFinalAdv(lastAdvChk.getSelection());

    }

    /*
     * Get the integer value from a string, use "def" if input is invalid.
     *
     * @param text String
     *
     * @param def Default value
     *
     * @return int
     */
    private int getValue(String text, int def) {

        int num = def;
        try {
            num = Integer.parseInt(text.trim());
        } catch (NumberFormatException ne) {
            // Invalid, use def.
        }

        return num;
    }

    /*
     * Get the GeographyPoint selected on a geography reference CCombo
     *
     * @param cmb A CCombo listing all GeographyPoints
     *
     * @return GeographyPoint
     */
    private GeographyPoint getGeoReference(CCombo cmb) {

        int ind = cmb.getSelectionIndex();
        GeographyPoint geoRef = null;
        // First one is "none"
        if (ind > 0) {
            geoRef = geoPoints.getGeoPoints().get(ind - 1);
        }

        return geoRef;
    }

    /*
     * Get special advisory times based on current DTG, starting from 3 hours
     * (not included) later from current DTG per interval. Currently, the
     * interval is every 30 minutes and up to 9 entries (8 hours after the
     * current DTG)
     *
     * @param dtg Current DTG
     *
     * @return Map<String, Integer>
     */
    private Map<String, Integer> getSpecialAdvEntries(String dtg) {

        Map<String, Integer> spAdvTimes = new LinkedHashMap<>();
        for (int ii = 0; ii < SPECIAL_ADV_NUM; ii++) {
            int minutes = 3 * 60 + (ii + 1) * SPECIAL_ADV_INTERVAL;
            String ndtg = AtcfDataUtil.getNewDTGWithMinutes(dtg, minutes);

            // Use "HHmm" in "YYYYMMDDHHmm". Special TAU hour is rounded up.
            int spTau = minutes / 60;
            if ((minutes % 60) > 0) {
                spTau++;
            }

            spAdvTimes.put(ndtg.substring(8), spTau);
        }

        return spAdvTimes;
    }

    /*
     * Get the index of a special advisory hour in the special advisory entries.
     *
     * @param spEntries Entries for search
     *
     * @param hour Hour for search
     *
     * @return int Index for the first entry with the hour.
     */
    private int getDfltSpecialAdvIndex(Map<String, Integer> spEntries,
            int hour) {

        int index = 0;

        int ii = 0;
        for (Integer spHr : spEntries.values()) {
            /*
             * Note - spEntries is a one-to-many map. We pick the last one in
             * the map that has the given hour.
             */
            if (spHr > hour) {
                index = ii - 1;
                break;
            }

            ii++;
        }

        return index;
    }

    /**
     * Get TAUs used for advisory storm state.
     *
     * Note: TAU < 12 hour is excluded & TAU 60 is via configuration.
     *
     * @return List<AtcfTaus>
     */
    private List<AtcfTaus> getStormStateTaus() {

        List<AtcfTaus> advTaus = new ArrayList<>();

        for (AtcfTaus tau : workingTaus) {
            int tauVal = tau.getValue();

            if (tauVal >= 12) {
                if (tauVal != 60) {
                    advTaus.add(tau);
                } else {
                    if (useTau60) {
                        advTaus.add(tau);
                    }
                }
            }
        }

        return advTaus;
    }

    /*
     * Update all advisory information and generate advisories. Multiple
     * advisory files are written in ADVISORYPATH directory defined in
     * atcf.properties.
     */
    private void generateAdvisories() {

        // First, update information from GUI for *.adv file.
        updateAdvisoryInfo(advInfo);

        // Get latest warnings/headline/hazards to be edited in text editor.
        warnings = AdvisoryUtil.readWarnings(stormId);
        headline = AdvisoryUtil.readHeadline(stormId);
        hazards = AdvisoryUtil.readHazards(stormId);

        // Build advisories.
        boolean isIntermediate = intermediateBtn.getSelection();
        AdvisoryBuilder advBuilder = new AdvisoryBuilder(storm, currentDTG,
                isIntermediate, advInfo, stmStates, fcstTrackData, warnings,
                headline, hazards, forecastRange, outlookRange);
        advBuilder.buildAdvisory();

        // Get advisories.
        Map<AdvisoryType, String> advisories = new LinkedHashMap<>();
        if (isIntermediate) {
            String tcpa = advBuilder.getTcpA();
            advisories.put(AdvisoryType.TCP_A, tcpa);
        } else {

            String tcp = advBuilder.getTcp();

            AdvisorySummary summary = advBuilder.getAdvSummary();
            String tcm = advBuilder.getTcm();
            String tca = advBuilder.getTca();
            String tcd = advBuilder.getTcd();

            // Add advisories in one map. More could be added later.
            advisories.put(AdvisoryType.TCP_ADV, advInfo.toString());
            advisories.put(AdvisoryType.TCP, tcp);
            advisories.put(AdvisoryType.TCP_SUM,
                    summary.toString().toUpperCase());
            advisories.put(AdvisoryType.WARNINGS, warnings);
            advisories.put(AdvisoryType.HEADLINE, headline);
            advisories.put(AdvisoryType.HAZARDS, hazards);

            advisories.put(AdvisoryType.TCM, tcm);
            advisories.put(AdvisoryType.TCA, tca);
            advisories.put(AdvisoryType.TCD, tcd);
        }

        // Save advisories via one request.
        if (advisories.size() > 0) {
            AdvisoryUtil.writeAdvisories(stormId, advisories);
        }

    }

    /*
     * Save records to sandbox with updated forecast data from GUI.
     */
    private void saveForecast() {

        // Update records for mslp/eye/center accuracy etc.
        for (List<ForecastTrackRecord> recs : fcstTrackData
                .values()) {
            for (ForecastTrackRecord rec : recs) {
                // Update forecaster for all TAUs.
                rec.setForecaster(fcstInitialsCmb.getText());

                // Update mslp and eyesize for special TAU.
                if (rec.getFcstHour() == specialTau) {
                    float mslp = AtcfVizUtil.getStringAsFloat(
                            surfacePresCmb.getText(), false, true,
                            rec.getMslp());
                    rec.setMslp(mslp);

                    float eyeSize = AtcfVizUtil.getStringAsFloat(
                            eyeDiameterCmb.getText(), false, true,
                            rec.getEyeSize());
                    rec.setEyeSize(eyeSize);
                }
            }
        }

        // Delete records if they are removed from "Advisory Data" dialog.
        List<ForecastTrackRecord> deleteRecs = new ArrayList<>();
        for (List<ForecastTrackRecord> recs : fcstTrackDataCopy
                .values()) {
            for (ForecastTrackRecord rec : recs) {
                ForecastTrackRecord existRec = getForecastByTauNRadii(
                        rec.getFcstHour(), (int) rec.getRadWind());
                if (existRec == null) {
                    deleteRecs.add(rec);
                }
            }
        }

        deleteForecastRecords(deleteRecs);

        // Save updated records into sandbox.
        NewForecastTrackRequest fcstTrackReq = new NewForecastTrackRequest();
        fcstTrackReq.setCurrentStorm(storm);
        fcstTrackReq.setSandboxId(fcstTrackSandBoxID);
        fcstTrackReq.setUserId(AtcfSession.getInstance().getUid());

        List<ForecastTrackRecord> records = new ArrayList<>();
        for (List<ForecastTrackRecord> recs : fcstTrackData
                .values()) {
            records.addAll(recs);
        }

        fcstTrackReq.setFstRecords(records);

        // Save to sandbox and update sandbox ID.
        try {
            fcstTrackSandBoxID = (int) ThriftClient.sendRequest(fcstTrackReq);

            fcstTrackReq.setSandboxId(fcstTrackSandBoxID);
            drawingLayer.getResourceData().setFcstTrackSandbox(storm,
                    fcstTrackSandBoxID);
        } catch (Exception e) {
            logger.warn(
                    "AdvisoryCompositionDialog: Failed to save new forecast into sandbox.",
                    e);
        }
    }

    /*
     * Delete records from the forecast track sandbox.
     *
     * @param recs ForecastTrackRecord to be removed.
     */
    private void deleteForecastRecords(
            List<ForecastTrackRecord> recs) {
        if (recs != null && !recs.isEmpty() && fcstTrackSandBoxID > 0) {

            DeleteForecastTrackRecordRequest fcstTrackReq = new DeleteForecastTrackRecordRequest();
            fcstTrackReq.setSandboxId(fcstTrackSandBoxID);
            fcstTrackReq.setFstRecords(recs);

            try {
                ThriftClient.sendRequest(fcstTrackReq);
            } catch (Exception e) {
                logger.error(
                        "AdvisoryCompositionDialog: Failed to delete forecast track records from sandbox for storm "
                                + storm.getStormId() + " - " + currentDTG,
                        e);
            }
        }
    }

    /*
     * Submit forecast records to baseline.
     */
    private void submitForecast() {

        // Save forecast into sandbox
        saveForecast();

        // Submit sandbox into baseline
        CheckinForecastTrackRequest req = new CheckinForecastTrackRequest();
        req.setSandboxid(fcstTrackSandBoxID);
        req.setUserId(AtcfSession.getInstance().getUid());

        try {
            ThriftClient.sendRequest(req);
        } catch (Exception ee) {
            logger.error(
                    "AdvisoryCompositionDialog: Failed to submit new forecast for storm "
                            + storm + ": " + currentDTG,
                    ee);
        }

        // Reset sandbox ID and data is ATCF resource.
        drawingLayer.getResourceData().setFcstTrackSandbox(storm, -1);
        drawingLayer.getResourceData().getAtcfProduct(storm)
                .setFcstTrackDataMap(fcstTrackData);

    }

    /*
     * Check if there is data for TAU 3 or the special TAU when
     * "Special Advisory" is on.
     *
     * @return boolean
     */
    private boolean hasSpecialTauData() {
        boolean hasData = false;
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {
            if (recs != null && !recs.isEmpty()) {
                int fcstHr = recs.get(0).getFcstHour();
                if (fcstHr == specialTau) {
                    hasData = true;
                    break;
                }
            }
        }

        return hasData;
    }

    /*
     * Reset data back to TAU 3 when "Special Advisory" turns off.
     */
    private void resetSpecialTau() {
        // Copy forecast track records and sort into a RecordKey map.
        fcstTrackData.clear();
        fcstTrackRecordMap = new HashMap<>();
        fcstTrackEditMap = new HashMap<>();
        for (Map.Entry<String, List<ForecastTrackRecord>> entry : fcstTrackDataCopy
                .entrySet()) {
            List<ForecastTrackRecord> frecs = entry.getValue();
            List<ForecastTrackRecord> frecs1 = new ArrayList<>();
            for (ForecastTrackRecord rec : frecs) {
                ForecastTrackRecord nrec = new ForecastTrackRecord(rec);
                int windRad = (int) rec.getRadWind();
                RecordKey key = new RecordKey("FCST", currentDTG,
                        rec.getFcstHour(), windRad);
                fcstTrackEditMap.put(nrec, rec);
                fcstTrackRecordMap.put(key, nrec);
                frecs1.add(nrec);
            }

            fcstTrackData.put(entry.getKey(), frecs1);
        }
    }

    /*
     * Reset data back to TAU 3 when "Special Advisory" turns off.
     */
    private void handleSpecialAdvisory() {
        boolean enableSpecialAdvTime = specialAdvChkBtn.getSelection();

        // Always starts from 6.
        if (enableSpecialAdvTime) {
            specialTau = 6;
            specialAdvTimeCmb.select(dfltAdvIndex);
        }

        ForecastTrackRecord spRec = getForecastByTauNRadii(specialTau,
                WindRadii.RADII_34_KNOT);

        if (enableSpecialAdvTime) {
            // Increase advisory number?
            boolean confirmed = MessageDialog.openQuestion(shell,
                    "Special Advisory", "Increase the advisory number?");

            if (confirmed) {
                int num = getValue(advisoryNumTxt.getText(),
                        advInfo.getAdvNum()) + 1;

                advisoryNumTxt.setText(Integer.toString(num));
            }
        } else {
            if (specialTau != 3 && spRec != null) {
                // Abandon edited special TAU data?
                boolean confirmed = MessageDialog.openQuestion(shell,
                        "Deeactivate Special Advisory",
                        "You just edited special advisory data for TAU "
                                + specialTau + " to deliver at "
                                + specialAdvTimeCmb.getText()
                                + ".\nAre you sure you want to abandon the changes?");

                if (confirmed) {
                    specialTau = 3;
                    resetSpecialTau();
                } else {
                    enableSpecialAdvTime = true;
                    specialAdvChkBtn.setSelection(true);
                }
            }
        }

        advTimeLbl.setEnabled(enableSpecialAdvTime);
        specialAdvTimeCmb.setEnabled(enableSpecialAdvTime);

        if (!enableSpecialAdvTime) {
            specialTau = 3;
            specialAdvTimeCmb.select(dfltAdvIndex);
        }

        // Highlight "Advisory Data" button if no data for the special TAU.
        updateAdvDataBtnStatus();

    }

    /*
     * Highlight "Advisory Data" button if no data for the special TAU.
     */
    private void updateAdvDataBtnStatus() {
        ForecastTrackRecord spRec = getForecastByTauNRadii(specialTau,
                WindRadii.RADII_34_KNOT);
        if (spRec == null) {
            AtcfVizUtil.setWarningBackground(advDataBtn);
        } else {
            /*
             * One special TAU may have two special time of deliveries, i.e.,
             * for current DTG at 6:00AM, 9:30 && 10:00 will result in the same
             * special TAU 4.
             */
            if (specialTau != 3 && !advInfo.getSpecialAdvTime()
                    .equals(specialAdvTimeCmb.getText())) {
                AtcfVizUtil.setWarningBackground(advDataBtn);
            } else {
                AtcfVizUtil.setDefaultBackground(advDataBtn);
            }
        }
    }

    /*
     * Update A-Deck data from the new forecast track data.
     */
    private void updateADeckData() {

        // Update records for mslp/eye/center accuracy etc.
        for (ForecastTrackRecord rec : fcstTrackRecordMap.values()) {
            // Update forecaster for all TAUs.
            rec.setForecaster(fcstInitialsCmb.getText());

            // Update mslp and eyesize for special TAU.
            if (rec.getFcstHour() == specialTau) {
                float mslp = AtcfVizUtil.getStringAsFloat(
                        surfacePresCmb.getText(), false, true, rec.getMslp());
                rec.setMslp(mslp);

                float eyeSize = AtcfVizUtil.getStringAsFloat(
                        eyeDiameterCmb.getText(), false, true,
                        rec.getEyeSize());
                rec.setEyeSize(eyeSize);
            }
        }

        // Check out ADeck
        Map<String, List<ADeckRecord>> arecs = AtcfDataUtil
                .retrieveADeckData(storm, new String[] { currentDTG }, true);

        int sandboxId = AtcfDataUtil.getADeckSandbox(storm);

        // Find OFCL and OFCO records.
        Map<RecordKey, ADeckRecord> ofclRecordMap = new LinkedHashMap<>();
        Map<RecordKey, ADeckRecord> ofcoRecordMap = new LinkedHashMap<>();
        boolean isOfclStandard = false;
        for (List<ADeckRecord> recs : arecs.values()) {
            if (recs != null && !recs.isEmpty()) {
                for (ADeckRecord rec : recs) {
                    if ("OFCL".equals(rec.getTechnique())) {
                        RecordKey rkey = new RecordKey("OFCL", currentDTG,
                                rec.getFcstHour(), (int) rec.getRadWind());
                        if (rec.getFcstHour() == AtcfTaus.TAU3.getValue()) {
                            isOfclStandard = true;
                        }
                        ofclRecordMap.put(rkey, rec);
                    } else if ("OFCO".equals(rec.getTechnique())) {
                        RecordKey rkey = new RecordKey("OFCO", currentDTG,
                                rec.getFcstHour(), (int) rec.getRadWind());
                        ofcoRecordMap.put(rkey, rec);
                    }
                }
            }
        }

        // Update
        if (specialTau == 3) {
            updateADeckForStandardAdv(sandboxId, ofclRecordMap);
        } else {
            updateADeckForSpecialAdv(sandboxId, ofclRecordMap, ofcoRecordMap,
                    isOfclStandard);
        }

    }

    /*
     * Update A-Deck data from a standard (TAU 3) advisory.
     *
     * 1. All records in forecast track should go into A-Deck. If cannot find an
     * ADeck record with the same TAU and wind radii of a forecast track record,
     * the forecast record should be added to ADeck as new record.
     *
     * 2. For records in ADeck that has same TAU & wind radii found in forecast
     * track, update them with forecast track.
     *
     * 3. For records in ADeck that cannot find a record with the same TAU &
     * wind radii in forecast track, check if there is at least a record with
     * same TAU and wind radii 34KT in forecast track, if found, such
     * ADeckRecords should be deleted; otherwise, such records should be updated
     * with the forecaster name and stay in A-Deck.
     *
     * @param sandboxId sandboxId for editing ADeck
     *
     * @param ofclRecordMap Current OFCL forecast in ADeck
     */
    private void updateADeckForStandardAdv(int sandboxId,
            Map<RecordKey, ADeckRecord> ofclRecordMap) {

        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
        if (specialTau == 3) {
            for (Map.Entry<RecordKey, ADeckRecord> entry : ofclRecordMap
                    .entrySet()) {
                RecordKey skey = entry.getKey();
                ADeckRecord aRec = entry.getValue();
                ForecastTrackRecord fcstRec = getForecastByTauNRadii(
                        skey.getTau(), skey.getWindRad());

                ForecastTrackRecord fcstRec34 = getForecastByTauNRadii(
                        skey.getTau(), WindRadii.RADII_34_KNOT.getValue());

                // Determine edit type and update records.
                RecordEditType editType = RecordEditType.MODIFY;
                if (fcstRec != null) { // Update
                    updateADeckRecord(aRec, fcstRec);
                } else {
                    // Only update forecaster
                    if (fcstRec34 == null) {
                        aRec.setForecaster(advInfo.getForecaster());
                    } else { // Delete
                        editType = RecordEditType.DELETE;
                        AtcfDataUtil.updateDeckRecord(sandboxId, aRec,
                                editType);
                    }
                }

                if (editType != RecordEditType.DELETE) {
                    ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                    mdr.setEditType(editType);
                    mdr.setRecord(aRec);
                    modifiedRecords.add(mdr);
                }
            }

            /*
             * Check if there are records in forecast track that do not have
             * counterparts in ADeck with same TAU and wind radii.
             */
            for (Map.Entry<RecordKey, ForecastTrackRecord> entry : fcstTrackRecordMap
                    .entrySet()) {
                RecordKey fkey = entry.getKey();
                RecordKey akey = new RecordKey("OFCL", currentDTG,
                        fkey.getTau(), fkey.getWindRad());
                ADeckRecord arec = ofclRecordMap.get(akey);
                if (arec == null) {
                    // Create a new ADeckRecord from forecast track record.
                    ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                    ADeckRecord nArec = new ADeckRecord(entry.getValue());
                    mdr.setEditType(RecordEditType.NEW);
                    mdr.setRecord(nArec);
                    modifiedRecords.add(mdr);
                }
            }
        }

        // Save and submit
        submitADeck(sandboxId, modifiedRecords);
    }

    /*
     * Update A-Deck data from a special advisory (TAU >3 & TAU < 9)
     *
     * 1. Move previous standard advisory data into OFCO but update TAU 0 with
     * the new data.
     *
     * 2. Move the new set of forecast data as OFCL in A-Deck.
     *
     * @param sandboxId sandboxId for editing ADeck
     *
     * @param ofclRecordMap Current OFCL forecast in ADeck
     *
     * @param ofcoRecordMap OFCL forecast from precious standard advisory.
     */
    private void updateADeckForSpecialAdv(int sandboxId,
            Map<RecordKey, ADeckRecord> ofclRecordMap,
            Map<RecordKey, ADeckRecord> ofcoRecordMap, boolean isOfclStandard) {
        if (specialTau != 3) {
            List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
            boolean firstMove = false;
            if (isOfclStandard && ofcoRecordMap.isEmpty()) {
                // Move previous standard OFCL into OFCO,
                firstMove = true;
                for (Map.Entry<RecordKey, ADeckRecord> entry : ofclRecordMap
                        .entrySet()) {
                    ADeckRecord aRec = entry.getValue();
                    aRec.setTechnique("OFCO");
                    RecordEditType editType = RecordEditType.MODIFY;

                    ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                    mdr.setEditType(editType);
                    mdr.setRecord(aRec);
                    modifiedRecords.add(mdr);

                    ofcoRecordMap.put(entry.getKey(), aRec);
                }
            }

            // Update OFCO TAU 0 with TAU 0 in special advisory,
            if (!ofcoRecordMap.isEmpty()) {
                for (Map.Entry<RecordKey, ADeckRecord> entry : ofcoRecordMap
                        .entrySet()) {
                    RecordKey skey = entry.getKey();
                    if (skey.getTau() == 0) {
                        ADeckRecord aRec = entry.getValue();
                        ForecastTrackRecord fcstRec = getForecastByTauNRadii(0,
                                skey.getWindRad());
                        if (aRec != null && fcstRec != null) {
                            updateADeckRecord(aRec, fcstRec);
                        }

                        if (!firstMove) {
                            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                            mdr.setEditType(RecordEditType.MODIFY);
                            mdr.setRecord(aRec);
                            modifiedRecords.add(mdr);
                        }
                    }
                }
            }

            // Save and submit
            submitADeck(sandboxId, modifiedRecords);

            // Move the new set of forecast data as OFCL in A-Deck.
            moveForecastToADeckOFCL();
        }
    }

    /*
     * Move a set of ForecastTrackRecord into ADeck as OFCL for special
     * advisory.
     */
    private void moveForecastToADeckOFCL() {

        if (specialTau != 3) {

            AtcfDataUtil.retrieveADeckData(storm, new String[] { currentDTG },
                    true);
            int sandboxID = AtcfDataUtil.getADeckSandbox(storm);

            List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
            for (ForecastTrackRecord frec : fcstTrackRecordMap.values()) {
                ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                ADeckRecord nArec = new ADeckRecord(frec);
                mdr.setEditType(RecordEditType.NEW);
                mdr.setRecord(nArec);
                modifiedRecords.add(mdr);
            }

            submitADeck(sandboxID, modifiedRecords);
        }
    }

    /*
     * Save changes to sandbox and submit into baseline ADeck
     *
     * @param sandboxId ID of the ADeck sandbox
     *
     * @param modifiedRecords List of modified ADeckRecords
     */
    private void submitADeck(int sandboxId,
            List<ModifiedDeckRecord> modifiedRecords) {

        if (!modifiedRecords.isEmpty()) {
            boolean successfulSave = AtcfDataUtil.updateDeckRecords(sandboxId,
                    AtcfDeckType.A, modifiedRecords);

            if (successfulSave) {
                AtcfDataUtil.checkinADeckRecords(sandboxId);

                // Reset sandbox ID for this storm product.
                AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                        .getResourceData().getAtcfProduct(storm);
                prd.setAdeckSandboxID(-1);
            }
        }
    }

    /*
     * Update an ADeckRecord from a ForecastTrackRecord.
     *
     * Note: Only those fields that could be edited in Advisory Composition
     * dialog and Advisory Data dialog are updated.
     *
     * @param arec ADeckRecord
     *
     * @param fstRec ForecastTrackRecord
     */
    private void updateADeckRecord(ADeckRecord arec,
            ForecastTrackRecord fstRec) {

        arec.setClat(fstRec.getClat());
        arec.setClon(fstRec.getClon());
        arec.setForecaster(fstRec.getForecaster());

        arec.setEyeSize(fstRec.getEyeSize());
        arec.setGust(fstRec.getGust());
        arec.setIntensity(fstRec.getIntensity());
        arec.setMslp(fstRec.getMslp());

        // Wave radii only used for TAU 0, TAU 3 or a special TAU <= 8.
        if (arec.getFcstHour() <= 8) {
            arec.setQuad1WaveRad(fstRec.getQuad1WaveRad());
            arec.setQuad2WaveRad(fstRec.getQuad2WaveRad());
            arec.setQuad3WaveRad(fstRec.getQuad3WaveRad());
            arec.setQuad4WaveRad(fstRec.getQuad4WaveRad());
        }

        arec.setQuad1WindRad(fstRec.getQuad1WindRad());
        arec.setQuad2WindRad(fstRec.getQuad2WindRad());
        arec.setQuad3WindRad(fstRec.getQuad3WindRad());
        arec.setQuad4WindRad(fstRec.getQuad4WindRad());

        arec.setStormSped(fstRec.getStormSped());
        arec.setStormDrct(fstRec.getStormDrct());
        arec.setWindMax(fstRec.getWindMax());
    }

    /**
     * @return the storm
     */
    @Override
    public Storm getStorm() {
        return storm;
    }

    /**
     * @return the currentDTG
     */
    @Override
    public String getCurrentDTG() {
        return currentDTG;
    }

    /**
     * @return the workingTaus
     */
    @Override
    public List<AtcfTaus> getWorkingTaus() {
        return workingTaus;
    }

    /**
     * @return the specialTau
     */
    @Override
    public int getSpecialTau() {
        return specialTau;
    }

    /**
     * @return the drawingLayer
     */
    @Override
    public AtcfResource getDrawingLayer() {
        return drawingLayer;
    }

    /**
     * @return the fcstTrackData
     */
    @Override
    public Map<String, List<ForecastTrackRecord>> getFcstTrackData() {
        return fcstTrackData;
    }

    /**
     * @return the fcstTrackEditMap
     */
    @Override
    public Map<ForecastTrackRecord, ForecastTrackRecord> getFcstTrackEditMap() {
        return fcstTrackEditMap;
    }

    /**
     * @return the fcstTrackRecordMap
     */
    @Override
    public Map<RecordKey, ForecastTrackRecord> getFcstTrackRecordMap() {
        return fcstTrackRecordMap;
    }

    /**
     * @return the currentBDeckRecords
     */
    @Override
    public Map<String, List<BDeckRecord>> getCurrentBDeckRecords() {
        return currentBDeckRecords;
    }

    /**
     * @return the fcstTrackSandBoxID
     */
    @Override
    public int getFcstTrackSandBoxID() {
        return fcstTrackSandBoxID;
    }

}
