/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechniques;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckinForecastTrackRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.DeleteForecastTrackRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewForecastTrackRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsProperties;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackProperties;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Entry point for all Forecast dialogs (Track/Intensity/Wind Radii/Seas). It
 * prepares data needed for the forecast and performs some common
 * functionalities such as save and submit.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 28, 2020 78027      jwu         Initial creation.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Apr 19, 2021 88712      jwu         Update OFCL A-Deck from forecast.
 * Apr 22, 2021 88729      jwu         Update OFCL A-Deck from intensity forecast.
 * Apr 29, 2021 88730      jwu         Update ADeck for wind radii & seas.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public abstract class ForecastDialog extends OcpCaveChangeTrackDialog {

    // Consensus technique for Track/Intensity/Radii.
    protected static final String TRACK_CONSENSUS = "TVCN";

    protected static final String INTENSITY_CONSENSUS = "IVCN";

    protected static final String RADII_CONSENSUS = "RVCN";

    // Wind radii quadrant code.
    protected static final String FULL_CIRCLE = "AAA";

    protected static final String DEFAULT_QUAD = "NEQ";

    // Maximum wind direction, speed (intensity), wind radii and wave radii.
    protected static final int MAX_WIND_DRCT = 360;

    protected static final int MAX_WIND_SPEED = 250;

    protected static final int MAXIMUM_WIND_RADII = 995;

    protected static final int MAXIMUM_WAVE_RADII = 995;

    // Number to indicate 12 ft seas forecast.
    protected static final int RAD_WAVE = 12;

    // Forecast type (TRACK, INTENSITY, WIND_RADII, and SEAS).
    protected ForecastType fcstType;

    // Current storm && DTG.
    protected Storm storm;

    protected String currentDTG;

    // Current TAU
    protected AtcfTaus currentTau;

    // TAUs
    protected List<AtcfTaus> workingTaus;

    // Experimental TAU
    protected static final AtcfTaus EXPERIMENTAL_TAU = AtcfTaus.TAU60;

    protected boolean useTau60 = false;

    // Buttons for each TAU
    protected Map<AtcfTaus, Button> tauBtns;

    // Items for wind radii and 12 ft seas wave radii CComboes
    protected static String[] radiiEntries = AtcfVizUtil.getRadiiEntries();

    // Items for wind speed (intensity) CComboes
    protected static String[] windEntries = AtcfVizUtil.getMaxWindEntries();

    // Labels for each quadrant.
    protected static final String[] quadrantLabels = { "NE (nm)", "SE (nm)",
            "SW (nm)", "NW (nm)" };

    // Verify listeners for radius of wave quadrant intensity.
    protected final AtcfTextListeners verifyListener = new AtcfTextListeners();

    // Current AtcfResource.
    protected AtcfResource drawingLayer;

    // Forecast track records from DB for current storm.
    protected Map<String, List<ForecastTrackRecord>> fcstTrackData;

    /*
     * A Map for copies of forecast track records to their original counterpart.
     * Changes are tracked in the "key" record and applied back into the
     * originals only at save or submit.
     */
    protected Map<ForecastTrackRecord, ForecastTrackRecord> fcstTrackEditMap;

    // All forecast track records sorted by RecordKey.
    protected ForecastTrackRecordMap fcstTrackRecordMap;

    // Best track records for current storm.
    protected Map<String, List<BDeckRecord>> currentBDeckRecords;

    // A-Deck records for current DTG.
    protected List<ADeckRecord> currentADeckRecords;

    protected Map<RecordKey, ADeckRecord> aDeckRecordMap;

    // Instance of obj aids display properties.

    protected ObjAidsProperties objAidsProperties = null;

    // Techniques selected on "Display Options" dialog.
    protected List<String> selectedObjAids;

    // Sandbox ID.
    protected int fcstTrackSandBoxID;

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     * @param storm
     *            current storm
     * @param type
     *            ForecastType
     * @param fcstTrackData
     *            Map of forecast track data
     */
    protected ForecastDialog(Shell parent, Storm storm, ForecastType type,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {

        super(parent);

        this.storm = storm;
        this.fcstType = type;
        this.currentTau = AtcfTaus.getTau(type.getDfltTau());
        this.fcstTrackData = fcstTrackData;

        // Initialize data.
        initializeData();
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     * @param storm
     *            current storm
     * @param type
     *            ForecastType
     * @param fcstTrackData
     *            Map of forecast track data
     */
    protected ForecastDialog(Shell parent, int swtStyle, int caveStyle,
            Storm storm, ForecastType type,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {

        super(parent, swtStyle, caveStyle);

        this.storm = storm;
        this.fcstType = type;
        this.currentTau = AtcfTaus.getTau(type.getDfltTau());
        this.fcstTrackData = fcstTrackData;

        // Initialize data.
        initializeData();
    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite scrollAreaComposite = new Composite(scrollComposite,
                SWT.NONE);
        GridLayout scrollLayout = new GridLayout(1, false);
        scrollLayout.marginWidth = 0;
        scrollAreaComposite.setLayout(scrollLayout);

        populateScrollComponent(scrollAreaComposite);

        scrollComposite.setContent(scrollAreaComposite);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setMinSize(
                scrollAreaComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    protected abstract void populateScrollComponent(Composite parent);

    /*
     * Initialize data.
     */
    private void initializeData() {

        // Get forecast hours.
        workingTaus = AtcfTaus.getForecastTaus();

        if (fcstType == ForecastType.WIND_RADII) {
            workingTaus = AtcfTaus.getForecastWindRadiiTaus();
        } else if (fcstType == ForecastType.SEAS) {
            workingTaus = AtcfTaus.getForecastSeasTaus();
        }

        // Get the current AtcfResource.
        drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Retrieve B-Deck data for current storm.
        currentBDeckRecords = AtcfDataUtil.getBDeckRecords(storm, false);

        /*
         * Find the latest DTG from B-Deck
         */
        List<String> dtgList = new ArrayList<>(
                currentBDeckRecords.keySet());
        currentDTG = dtgList.get(dtgList.size() - 1);

        // The first DTG in forecast should be the same as current DTG.
        String fcstDTG = null;
        if (fcstTrackData != null && !fcstTrackData.isEmpty()) {
            for (String fdtg : fcstTrackData.keySet()) {
                fcstDTG = fdtg;
                break;
            }
        }

        if (fcstDTG != null) {
            currentDTG = fcstDTG;
        }

        // Update dialog title.
        setText(fcstType.getTitle() + " - " + storm.getStormName() + " "
                + storm.getStormId() + " - " + currentDTG.substring(4));

        // If forecast track is not for the current DTG, we needs to start over.
        if (fcstDTG != null && !fcstDTG.equals(currentDTG)) {
            logger.warn(fcstType.getDialogName()
                    + ": No forecast being done yet for " + currentDTG);
            fcstTrackData.clear();
        }

        /*
         * IF no forecast has been done yet, start from the latest best track
         * location?
         */
        if (fcstTrackData.get(currentDTG) == null) {
            List<ForecastTrackRecord> frecs = new ArrayList<>();
            for (BDeckRecord rec : currentBDeckRecords.get(currentDTG)) {
                frecs.add(AtcfDataUtil.makeFcstTrackRecord(rec));
            }
            fcstTrackData.put(currentDTG, frecs);
        }

        /*
         * Make a copy of forecast track records and sort into a RecordKey map
         */
        fcstTrackRecordMap = new ForecastTrackRecordMap();
        fcstTrackEditMap = new HashMap<>();
        for (List<ForecastTrackRecord> frecs : fcstTrackData
                .values()) {
            for (ForecastTrackRecord rec : frecs) {
                ForecastTrackRecord nrec = new ForecastTrackRecord(rec);
                int windRad = (int) rec.getRadWind();

                RecordKey key = new RecordKey("FCST", currentDTG,
                        rec.getFcstHour(), windRad);

                fcstTrackEditMap.put(nrec, rec);

                fcstTrackRecordMap.put(key, nrec);
            }
        }

        // Display current forecast track, if any.
        fcstTrackSandBoxID = drawingLayer.getResourceData()
                .getFcstTrackSandbox(storm);

        drawForecastTrack();

        fcstTrackSandBoxID = drawingLayer.getResourceData()
                .getFcstTrackSandbox(storm);

        // Retrieve A-Deck records for current DTG (no display)
        prepareObjAids();

        if (selectedObjAids == null) {
            selectedObjAids = new ArrayList<>();

            selectedObjAids
                    .addAll(objAidsProperties.getSelectedObjectiveAids());

            Collections.sort(selectedObjAids);
        }

        // Find if TAU 60 needs to be turned on.
        useTau60 = AtcfConfigurationManager.getInstance().getPreferences()
                .getUseTau60();

    }

    /*
     * Retrieve objective aids (A-Deck) for 12 ft seas radii forecast.
     */
    private void prepareObjAids() {

        retrieveObjAids();

        // Create an ObjAidsProperties to hold all properties.
        if (objAidsProperties == null) {
            objAidsProperties = new ObjAidsProperties();
        }

        // Get configurations.
        Map<String, ObjectiveAidTechEntry> allObjectiveAids = AtcfConfigurationManager
                .getInstance().getObjectiveAidTechniques()
                .getAvailableTechniques();
        objAidsProperties.setAllObjectiveAids(allObjectiveAids);

        String[] availableProfiles = AtcfConfigurationManager.getInstance()
                .getObjAidsProfileNames();

        objAidsProperties.setAvailableProfiles(availableProfiles);

        Set<String> aidsFound = AtcfDataUtil.getObjAidTechniques(storm);
        objAidsProperties.setActiveObjectiveAids(aidsFound);

        String[] selDtgs = new String[] { currentDTG };

        objAidsProperties.setSelectedDateTimeGroups(selDtgs);
        objAidsProperties.setSelectedPartialTime("" + currentTau.getValue());

        /*
         * TODO Use default techniques defined for each type of forecast in
         * techlist - no default radiis defined for 12 ft seas forecast in
         * techlist.dat and looks like only OFCL may have 12 FT seas at TAU 3.
         */
        ObjectiveAidTechniques objTechs = AtcfConfigurationManager.getInstance()
                .getObjectiveAidTechniques();
        Set<String> dfltTechs = objTechs.getDefObjAidForTrackFcst().keySet();
        if (fcstType == ForecastType.INTENSITY) {
            dfltTechs = objTechs.getDefObjAidForIntensityFcst().keySet();
        } else if (fcstType == ForecastType.WIND_RADII) {
            dfltTechs = objTechs.getDefObjAidForWindRadiiFcst().keySet();
        } else if (fcstType == ForecastType.SEAS) {
            dfltTechs = objTechs.getDefObjAidForWindRadiiFcst().keySet();
        }

        objAidsProperties.setDefaultObjectiveAids(
                dfltTechs.toArray(new String[dfltTechs.size()]));

        objAidsProperties.setSelectedObjectiveAids(
                dfltTechs.stream().collect(Collectors.toList()));
    }

    /**
     * Draw forecast track
     */
    protected void drawForecastTrack() {
        drawForecastTrack(null);
    }

    /**
     * Draw forecast track
     * 
     * @param fcstTrkData
     *            Forecast track data.
     */
    protected void drawForecastTrack(
            Map<String, List<ForecastTrackRecord>> fcstTrkData) {

        if (fcstTrkData == null) {
            fcstTrkData = fcstTrackData;
        }

        AtcfProduct prd = drawingLayer.getResourceData().getAtcfProduct(storm);
        if (prd.getForecastTrackLayer().getDrawables().isEmpty()
                || !fcstTrkData.isEmpty()) {

            prd.setFcstTrackDataMap(fcstTrkData);

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
     * Find the forecast track record stored in the map for a TAU/Wind Radii.
     *
     * @param tau
     *            AtcfTaus (forecast hour)
     *
     * @param radii
     *            WindRadii
     *
     * @return ForecastTrackRecord
     */
    protected ForecastTrackRecord getForecastByTauNRadii(AtcfTaus tau,
            WindRadii radii) {
        RecordKey key = new RecordKey("FCST", currentDTG, tau.getValue(),
                radii.getValue());
        return fcstTrackRecordMap.get(key);
    }

    /**
     * Save records to sandbox with updated forecast track data from GUI.
     */
    protected void saveForecast() {

        // Update records (34, 50, 64 knots if exist).
        applyChanges();

        // Put updated records into sandbox saving.
        NewForecastTrackRequest fcstTrackReq = new NewForecastTrackRequest();
        fcstTrackReq.setCurrentStorm(storm);
        fcstTrackReq.setSandboxId(fcstTrackSandBoxID);
        fcstTrackReq.setUserId(AtcfSession.getInstance().getUid());

        List<ForecastTrackRecord> records = new ArrayList<>();
        for (List<ForecastTrackRecord> recs : fcstTrackData
                .values()) {
            records.addAll(recs);
        }

        // Sort records by data time
        sort(records);
        fcstTrackReq.setFstRecords(records);

        // Save to sandbox and update sandbox ID.
        try {
            fcstTrackSandBoxID = (int) ThriftClient.sendRequest(fcstTrackReq);

            fcstTrackReq.setSandboxId(fcstTrackSandBoxID);
            drawingLayer.getResourceData().setFcstTrackSandbox(storm,
                    fcstTrackSandBoxID);

            // Update flag to indicate changes have been saved.
            changeListener.setChangesUnsaved(false);
        } catch (Exception e) {
            logger.error(fcstType.getDialogName()
                    + "Failed to save forecast into sandbox for storm "
                    + storm.getStormId() + " - " + currentDTG, e);
        }

        // Update/save A-Deck data.
        saveADeck();
    }

    /**
     * Delete records from a forecast track sandbox.
     *
     * @param recs
     *            ForecastTrackRecord to be removed.
     */
    protected void deleteForecast(List<ForecastTrackRecord> recs) {
        if (recs != null && !recs.isEmpty() && fcstTrackSandBoxID > 0) {

            DeleteForecastTrackRecordRequest fcstTrackReq = new DeleteForecastTrackRecordRequest();
            fcstTrackReq.setSandboxId(fcstTrackSandBoxID);
            fcstTrackReq.setFstRecords(recs);

            try {
                ThriftClient.sendRequest(fcstTrackReq);

                // Update flag to indicate changes have been saved.
                changeListener.setChangesUnsaved(false);
            } catch (Exception e) {
                logger.error(fcstType.getDialogName()
                        + "Failed to remove forecast into sandbox for storm "
                        + storm.getStormId() + " - " + currentDTG, e);
            }
        }
    }

    /**
     * Submit records to baseline.
     * 
     * @param save
     *            Flag to save changes into sandbox before submitting.
     */
    protected void submitForecast(boolean save) {

        if (save) {
            saveForecast();
        }

        CheckinForecastTrackRequest req = new CheckinForecastTrackRequest();
        req.setSandboxid(fcstTrackSandBoxID);
        req.setUserId(AtcfSession.getInstance().getUid());

        try {
            ThriftClient.sendRequest(req);

            // Reset sandbox.
            drawingLayer.getResourceData().setFcstTrackSandbox(storm, -1);

        } catch (Exception ee) {
            logger.error(fcstType.getDialogName()
                    + ": Failed to sumbit forecast for storm "
                    + storm.getStormId() + " - " + currentDTG, ee);
        }

        // Submit A-Deck changes
        submitADeck();
    }

    /**
     * Creates ForecastTrackRecord for given forecast hours between two known
     * forecasts by interpolation.
     *
     * Note: if either start or end TAU has no forecast, no records will be
     * created.
     *
     * @param start
     *            Starting TAU
     * @param end
     *            End TAU
     * @param fcstHrs
     *            Array of forecast hours to be interpolated
     * @return List<ForecastTrackRecord>
     */
    protected List<ForecastTrackRecord> createRecord(AtcfTaus start,
            AtcfTaus end, int[] fcstHrs) {

        ForecastTrackRecord startRec = getForecastByTauNRadii(start,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord endRec = getForecastByTauNRadii(end,
                WindRadii.RADII_34_KNOT);

        // Create a record by interpolation.
        return AtcfVizUtil.interpolateRecord(startRec, endRec, fcstHrs, false);
    }

    /**
     * Update a 12 ft seas radii combo's selection or text
     *
     * @param cmb
     *            radii CCombo
     *
     * @param radiiValue
     *            radii value
     */
    protected void updateRadiiCombo(CCombo cmb, int radiiValue) {

        // Round to nearest 5 first.
        int radii = AtcfDataUtil.roundToNearest(radiiValue, 5);
        int ind = Arrays.asList(radiiEntries).indexOf("" + radii);

        if (ind >= 0) {
            cmb.select(ind);
        } else {
            // For invalid radii (likely the default in a new record), set to 0.
            if (radii > MAXIMUM_WIND_RADII) {
                cmb.select(0);
            } else {
                cmb.setText("" + radiiValue);
            }
        }
    }

    /*
     * Retrieve/refresh objective aids (A-Deck).
     */
    private void retrieveObjAids() {

        int sandboxId = AtcfDataUtil.getADeckSandbox(storm);

        // Retrieve A-Deck records for current DTG and display
        currentADeckRecords = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getADeckData(currentDTG);
        if (currentADeckRecords != null) {
            drawingLayer.getResourceData().getActiveAtcfProduct()
                    .getADeckDataMap().remove(currentDTG);
        }

        currentADeckRecords = AtcfDataUtil.queryADeckByDTG(storm, currentDTG,
                sandboxId, true);
        drawingLayer.getResourceData().getActiveAtcfProduct()
                .addADeckData(currentDTG, currentADeckRecords);

        // Sort ADeckRecord into map
        aDeckRecordMap = new HashMap<>();
        for (ADeckRecord rec : currentADeckRecords) {
            String aid = rec.getTechnique();
            int tau = rec.getFcstHour();
            int windRad = (int) rec.getRadWind();
            RecordKey key = new RecordKey(aid, currentDTG, tau, windRad);
            aDeckRecordMap.put(key, rec);
        }
    }

    /*
     * Sort records by TAU and wind radii.
     * 
     * @param fcstRecs List of ForecastTrackRecord
     */
    private void sort(List<ForecastTrackRecord> fcstRecs) {
        if (!fcstRecs.isEmpty()) {
            Comparator<ForecastTrackRecord> fcstTimeCmp = Comparator
                    .comparing(ForecastTrackRecord::getFcstHour);

            Comparator<ForecastTrackRecord> radWndCmp = Comparator
                    .comparing(ForecastTrackRecord::getRadWind);

            Collections.sort(fcstRecs, fcstTimeCmp.thenComparing(radWndCmp));
        }
    }

    /*
     * Update/Save A-Deck data from the new forecast track data.
     *
     * 1. All records in forecast track should go into A-Deck. If cannot find an
     * ADeck record with the same TAU and wind radii of a forecast track record,
     * the forecast record should be added to ADeck as new record.
     *
     * 2. For records in ADeck that has same TAU & wind radii found in forecast
     * track, update them with forecast track.
     *
     */
    protected void saveADeck() {

        // Check out ADeck
        Map<String, List<ADeckRecord>> arecs = AtcfDataUtil
                .retrieveADeckData(storm, new String[] { currentDTG }, true);

        int sandboxId = AtcfDataUtil.getADeckSandbox(storm);

        // Find OFCL and OFCO records.
        Map<RecordKey, ADeckRecord> ofclRecordMap = new LinkedHashMap<>();
        for (List<ADeckRecord> recs : arecs.values()) {
            if (recs != null && !recs.isEmpty()) {
                for (ADeckRecord rec : recs) {
                    if ("OFCL".equals(rec.getTechnique())) {
                        RecordKey rkey = new RecordKey("OFCL", currentDTG,
                                rec.getFcstHour(), (int) rec.getRadWind());
                        ofclRecordMap.put(rkey, rec);
                    }
                }
            }
        }

        // Update & save
        saveADeck(sandboxId, ofclRecordMap);
    }

    /*
     * Save A-Deck data changes into sandbox.
     *
     * @param sandboxId sandboxId for editing ADeck
     *
     * @param ofclRecordMap Current OFCL forecast in ADeck
     */
    private void saveADeck(int sandboxId,
            Map<RecordKey, ADeckRecord> ofclRecordMap) {

        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();

        for (List<ForecastTrackRecord> recs : fcstTrackData
                .values()) {
            if (recs != null && !recs.isEmpty()) {
                for (ForecastTrackRecord fRec : recs) {
                    RecordKey akey = new RecordKey("OFCL", currentDTG,
                            fRec.getFcstHour(), (int) fRec.getRadWind());
                    ADeckRecord aRec = ofclRecordMap.get(akey);

                    // Determine edit type and update records.
                    RecordEditType editType = RecordEditType.MODIFY;
                    if (aRec != null) { // Update
                        updateADeckRecord(aRec, fRec);
                    } else {
                        // Create a new ADeckRecord from forecast
                        editType = RecordEditType.NEW;
                        aRec = AtcfDataUtil.fcstToADeckRecord(fRec);
                    }

                    ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                    mdr.setEditType(editType);
                    mdr.setRecord(aRec);
                    modifiedRecords.add(mdr);
                }
            }
        }

        // Save into sandbox
        if (!modifiedRecords.isEmpty()) {
            boolean success = AtcfDataUtil.updateDeckRecords(sandboxId,
                    AtcfDeckType.A, modifiedRecords);
            if (success) {
                retrieveObjAids();
            }
        }
    }

    /*
     * Submit ADeckRecord sandbox to baseline.
     */
    protected void submitADeck() {

        int sandboxId = AtcfDataUtil.getADeckSandbox(storm);

        AtcfDataUtil.checkinADeckRecords(sandboxId);

        AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                .getResourceData().getAtcfProduct(storm);

        prd.setAdeckSandboxID(-1);
    }

    /*
     * Update an ADeckRecord from a ForecastTrackRecord.
     *
     * @param arec ADeckRecord
     *
     * @param fstRec ForecastTrackRecord
     */
    private void updateADeckRecord(ADeckRecord arec,
            ForecastTrackRecord fstRec) {

        switch (fcstType) {
        case TRACK:
            // Fields changed in Forecast Track tool
            arec.setClat(fstRec.getClat());
            arec.setClon(fstRec.getClon());
            arec.setStormSped(fstRec.getStormSped());
            arec.setStormDrct(fstRec.getStormDrct());
            break;

        case INTENSITY:
            // Fields changed in Forecast Intensity tool
            arec.setWindMax(fstRec.getWindMax());
            arec.setGust(fstRec.getGust());
            arec.setIntensity(fstRec.getIntensity());
            break;

        case WIND_RADII:
            // Fields changed in Forecast Wind radii tool
            arec.setRadWind(arec.getRadWind());
            arec.setRadWindQuad(arec.getRadWindQuad());
            arec.setQuad1WindRad(arec.getQuad1WindRad());
            arec.setQuad2WindRad(arec.getQuad2WindRad());
            arec.setQuad3WindRad(arec.getQuad3WindRad());
            arec.setQuad4WindRad(arec.getQuad4WindRad());
            break;

        case SEAS:
            // Fields changed in Forecast Seas tool
            int aHr = arec.getFcstHour();
            if ((aHr == 0 || aHr == 3)) {
                arec.setRadWave(fstRec.getRadWave());
                arec.setRadWaveQuad(fstRec.getRadWaveQuad());
                arec.setQuad1WaveRad(fstRec.getQuad1WaveRad());
                arec.setQuad2WaveRad(fstRec.getQuad2WaveRad());
                arec.setQuad3WaveRad(fstRec.getQuad3WaveRad());
                arec.setQuad4WaveRad(fstRec.getQuad4WaveRad());
            }
            break;

        default:
            break;
        }
    }

    /**
     * @return the currentDTG
     */
    public String getCurrentDTG() {
        return currentDTG;
    }

    /**
     * @return the currentTau
     */
    public AtcfTaus getCurrentTau() {
        return currentTau;
    }

    /**
     * @return the workingTaus
     */
    public List<AtcfTaus> getWorkingTaus() {
        return workingTaus;
    }

    /**
     * @return the objAidsProperties
     */
    public ObjAidsProperties getObjAidsProperties() {
        return objAidsProperties;
    }

    /**
     * @return the selectedObjAids
     */
    public List<String> getSelectedObjAids() {
        return selectedObjAids;
    }

    /**
     * @return the fcstTrackSandBoxID
     */
    public int getFcstTrackSandBoxID() {
        return fcstTrackSandBoxID;
    }

    /**
     * @return the currentBDeckRecords
     */
    public Map<String, List<BDeckRecord>> getCurrentBDeckRecords() {
        return currentBDeckRecords;
    }

    /**
     * @return the fcstTrackData
     */
    public Map<String, List<ForecastTrackRecord>> getFcstTrackData() {
        return fcstTrackData;
    }

    /**
     * Apply changes into forecast records for saving - subclass overrides this
     * method.
     */
    protected abstract void applyChanges();

}