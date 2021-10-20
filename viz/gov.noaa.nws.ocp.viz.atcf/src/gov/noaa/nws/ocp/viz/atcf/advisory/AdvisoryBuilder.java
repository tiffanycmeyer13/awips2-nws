/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.raytheon.uf.common.dataplugin.text.AfosWmoIdDataContainer;
import com.raytheon.uf.common.dataplugin.text.request.GetPartialAfosIdRequest;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSiteEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSites;
import gov.noaa.nws.ocp.common.atcf.configuration.FullInitials;
import gov.noaa.nws.ocp.common.atcf.configuration.StormStateEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.StormStates;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryDiscussion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryHeader;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryInfo;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisorySummary;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryTimeZone;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryTimer;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AtcfAdvisory;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AviationAdvisory;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.Discussion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.ForecastAdvisory;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.ForecastData;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.ForecastTime;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.IcaoDataInfo;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.IcaoForecast;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.PublicAdvisory;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.RadiiData;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.WindData;
import gov.noaa.nws.ocp.common.product.build.text.TextProductBuilder;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;

/**
 * Builds tropical cyclone advisory for a storm.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 19, 2020 82721      jwu         Initial creation
 * Oct 29, 2020 81820      wpaintsil   Add Forecast Advisory
 * Nov 23, 2020 85264      wpaintsil   Missing summary info.
 * Jan 26, 2021 86746      jwu         Overhaul TCA.
 * Feb 12, 2021 87783      jwu         Overhaul TCM.
 * Feb 17, 2021 87781      jwu         Overhaul TCP.
 * Feb 26, 2021 88638      jwu         Handle date/time with AdvisoryTimer.
 * Feb 26, 2021 85386      wpaintsil   Add Tropical Cyclone Discussion.
 * Mar 04, 2021 88931      jwu         Add forecast/outlook range.
 * Mar 22, 2021 88518      dfriedman   Remove use of AFOS node.
 * Mar 25, 2021 90014      dfriedman   Use request to look up AFOS node.
 * May 14, 2021 88584      wpaintsil   Implement TCP_A
 * Jun 04, 2021 91765      jwu         Minor fixes.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class AdvisoryBuilder {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AdvisoryBuilder.class);

    // Suffix for a handleBar template file.
    private static final String TEMPLATE_SUFFIX = ".hbs";

    // Suffix JSON schema file.
    private static final String JSON_SUFFIX = "_schema.json";

    // Normal forecast TAUs (TCM)
    private static final int[] TCM_FORECAST_TAUS = new int[] { 12, 24, 36, 48,
            60, 72, 96, 120, 144, 168 };

    // Ending TAU for forecast & outlook in advisory.
    private int forecastRange = 72;

    private int outlookRange = 120;

    // The storm.
    private Storm storm;

    // The DTG
    private String dtg;

    // Storm states
    private StormStates stmStates;

    // Forecast track records for the storm.
    private Map<String, List<ForecastTrackRecord>> fcstTrackData;

    // TAU 0
    private ForecastTrackRecord firstFcstRecord;

    /*
     * Current TAU & its first record. Current TAU is read from "special_TAU" in
     * advInfo and normally at TAU 3.
     */
    private int currentTau;

    private ForecastTrackRecord currentFcstRecord;

    private AdvisoryInfo advInfo;

    private AdvisorySummary advSummary;

    private AdvisoryTimer advTimer;

    private AdvisoryHeader advHeader;

    // Advisories
    private String tcp = "";

    private String tcpa = "";

    private String tcm = "";

    private String summary = "";

    private String warnings = "";

    private String headline = "";

    private String hazards = "";

    private String discus = "";

    private String previousTCD = "";

    private String tca = "";

    private String tcd = "";

    // Time zone
    protected String timezone = "EST";

    // Flag to generate intermediate advisory (TCP-A)
    protected boolean isIntermediate = false;

    // Flag for whether this is a special advisory
    protected boolean isSpecialAdv = false;

    // Flag for whether the storm is a potential tropical cyclone
    protected boolean isPTC = false;

    private FullInitials fullInitials;

    private AtcfSites atcfSites;

    private AtcfSiteEntry mainSite;

    private AtcfSiteEntry backupSite;

    private String advBasin;

    private IcaoDataInfo icaoInfo;

    // Formatted forecast track sorted by TAU
    private Map<Integer, ForecastData> advData;

    // Flag for if storm is stationary.
    private boolean isStationary;

    /**
     * Default constructor
     */
    public AdvisoryBuilder() {
        this.storm = null;
        this.dtg = "";
        this.isIntermediate = false;
        this.advInfo = new AdvisoryInfo();
        this.advSummary = new AdvisorySummary();
        this.icaoInfo = new IcaoDataInfo();
    }

    /**
     * Constructor
     *
     * @param storm
     * @param dtg
     * @param isIntermidate
     * @param advInfo
     * @param stmStates
     * @param fcstTrackData
     * @param warnings
     * @param headline
     * @param hazards
     * @param forecastRange
     * @param outlookRange
     */
    public AdvisoryBuilder(final Storm storm, final String dtg,
            final boolean isIntermidate, AdvisoryInfo advInfo,
            StormStates stmStates,
            Map<String, List<ForecastTrackRecord>> fcstTrackData,
            String warnings, String headline, String hazards, int forecastRange,
            int outlookRange) {
        this.storm = storm;
        this.dtg = dtg;
        this.isIntermediate = isIntermidate;
        this.advInfo = advInfo;
        this.stmStates = stmStates;
        this.fcstTrackData = fcstTrackData;
        this.warnings = warnings;
        this.headline = headline;
        this.hazards = hazards;
        this.forecastRange = forecastRange;
        this.outlookRange = outlookRange;

        // Retrieve the full first names for all initials.
        this.fullInitials = AtcfConfigurationManager.getInstance()
                .getFullInitials();

        // Retrieve site information.
        this.atcfSites = AtcfConfigurationManager.getInstance().getAtcfSites();

        AtcfEnvironmentConfig envConfig = AtcfConfigurationManager
                .getEnvConfig();

        String mSite = envConfig.getAtcfSite();
        String bSite = envConfig.getBackupSite();

        this.mainSite = atcfSites.getAtcfSitebyID(mSite);
        this.backupSite = atcfSites.getAtcfSitebyID(bSite);

        initialize();

    }

    /*
     * Initialize, prepare data, and pre-build some general information.
     */
    private void initialize() {

        this.isPTC = advInfo.isPotentialTC();
        this.isSpecialAdv = advInfo.isSpecialAdv();

        // Sort records at each TAU by wind radii in descending order (64/50/34)
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {
            Collections.sort(recs,
                    Comparator.comparing(ForecastTrackRecord::getRadWind));
            Collections.reverse(recs);
        }

        // Find first record and the current record at special TAU (3)
        firstFcstRecord = fcstTrackData.get(dtg).get(0);

        currentTau = advInfo.getSpecialTAU();

        currentFcstRecord = getSingleTauData(currentTau,
                WindRadii.RADII_34_KNOT.getValue());

        isStationary = isStormStationary();

        // Timer
        advTimer = new AdvisoryTimer(dtg, advInfo, fcstTrackData, true);

        // Adjust advisory basin based on storm location.
        advBasin = advTimer.getAdvBasin();

        // Build a general advisory header.
        advHeader = buildAdvisoryHeader(advBasin, AdvisoryType.TCM.name());

        // Derive data used for summary section.
        advSummary = AdvisoryUtil.buildAdvisorySummary(advInfo,
                currentFcstRecord);

        // Derive ICAO data information for TCA.
        icaoInfo = AdvisoryUtil.deriveIcaoDataInfo(advInfo.getSpecialTAU(),
                fcstTrackData);

        // Read previous discussion/outlook section in TCP.
        discus = AdvisoryUtil.readDiscussion(storm.getStormId().toLowerCase());

        // Format forecast track data & sort by TAU.
        advData = prepareAdvData();

        // Read previous TCD discussion.
        previousTCD = AdvisoryUtil
                .readTCDiscussion(storm.getStormId().toLowerCase());

    }

    /**
     * Build advisory.
     */
    public void buildAdvisory() {

        /*
         * Build advisories.
         * 
         * Note: TCM is always built. TCP/TCD/TCA/PWS built for non-Intermediate
         * while TCP-A only built for intermediate (and pubFreq != 6).
         */
        ForecastAdvisory fcsts = getForecastAdvisoryObject();
        tcm = buildAdvFromJson(fcsts);

        if (isIntermediate) {
            tcpa = buildAdvFromJson(getPublicAdvisoryObject());
        } else {
            tcp = buildAdvFromJson(getPublicAdvisoryObject());
            tca = buildAdvFromJson(getICAOAdvisoryObject());
            tcd = buildAdvFromJson(getDiscussionObject());
        }

    }

    /**
     * Put deck data in a Forecast Advisory object.
     * 
     * @return ForecastAdvisory
     */
    private ForecastAdvisory getForecastAdvisoryObject() {
        ForecastAdvisory fstAdvObj = new ForecastAdvisory();

        // Adjust header information
        AdvisoryHeader hdr = new AdvisoryHeader(advHeader);
        hdr.setCategory(AdvisoryType.TCM.name());
        hdr.setAdvClass(hdr.getAdvClass().toUpperCase());
        hdr.setIssuedBy(hdr.getIssuedBy().toUpperCase());
        hdr.setIssuedByBackup(hdr.getIssuedByBackup().toUpperCase());
        hdr.setTimeZone(AdvisoryTimeZone.UTC.name());
        hdr.setForecaster(hdr.getForecaster().toUpperCase());

        // Adjust wfo id/node/basin.
        String bsnDsg = advTimer.getBasinDesignator();
        if (!"C".equals(bsnDsg) && !hdr.isIssuedByCPHC()) {
            hdr.setWfo("KNHC");
        } else {
            hdr.setWfo("PHFO");
            hdr.setBasin("CP");
        }

        // Adjust primary issuing office address.
        String issueBy = atcfSites.getAtcfSitebyID("CPHC").getIssueByOffice();
        if (!("C".equals(bsnDsg)) && !hdr.isIssuedByCPHC()) {
            issueBy = atcfSites.getAtcfSitebyID("NHC").getIssueByOffice();
        }
        hdr.setIssuedBy(issueBy.toUpperCase());

        // Check who is issuing the final advisory.
        String lastAdvFrom = atcfSites.getAtcfSitebyID("NHC").getCenter();
        if ("C".equals(bsnDsg)) {
            lastAdvFrom = atcfSites.getAtcfSitebyID("CPHC").getCenter();
        }
        hdr.setLastAdvFrom(lastAdvFrom);

        // Adjust advisory number phrase
        String advNumPhrase = AdvisoryUtil.getAdvNumPhase(AdvisoryType.TCM,
                advInfo.isSpecialAdv(), hdr.getAdvClass(), hdr.getAdvName(),
                hdr.getAdvNumber());
        hdr.setAdvNumPhrase(advNumPhrase.toUpperCase());

        fstAdvObj.setHeader(hdr);

        // Set warning message.
        fstAdvObj.setWatchWarn(warnings.toUpperCase());

        // Set accuracy.
        fstAdvObj
                .setAccuracy(String.format("%3d", advInfo.getCenterAccuracy()));

        // Set eye diameter.
        if (currentFcstRecord.getEyeSize() > 0) {
            String eyeSize = String.format("%3d",
                    (int) currentFcstRecord.getEyeSize());
            fstAdvObj.setEyeSize(eyeSize);
        }

        /*
         * Set direction, speed, and mslp
         *
         * If the storm is stationary (storm motion = zero), set its direction
         * of motion to North/360 degrees.
         */
        float stormDir = currentFcstRecord.getStormDrct();
        float stormSpd = currentFcstRecord.getStormSped();
        if (isStationary) {
            stormSpd = 0;
            stormDir = 360;
            // setStationary(true);?
        }

        fstAdvObj.setDirection(
                AdvisoryUtil.getDirection(stormDir, false).toUpperCase());
        fstAdvObj.setDegrees(String.format("%3d", (int) stormDir));
        fstAdvObj.setMovementKt(String.format("%3d", (int) stormSpd));
        fstAdvObj.setPressureMb(String.format("%4d", advInfo.getPres()));

        // Set forecast data, include TAU 0, 3, forecast, outlook.
        setForecastData(fstAdvObj);

        // Set average track error (basin-dependent).
        setForecastTrackError(fstAdvObj);

        /*
         * Set intermediate public advisory header & its time (+6 from TAU=0)..
         * 
         * Note: the final advisory message will likely be replaced by the
         * forecaster with a snippet in the text editor.
         */
        String ipub = getIPubHeader(advBasin, hdr.getBinNumber(),
                hdr.isIssuedByCPHC(), hdr.isIssuedByWPC());
        fstAdvObj.setIntermPubHeader(ipub);

        ForecastTime nit = advTimer.getUTCForecastTime(6, false);
        fstAdvObj.setNextIntermTime(nit);

        // Set next complete advisory time (+9 from TAU=0).
        ForecastTime nft = advTimer.getUTCForecastTime(9, false);
        fstAdvObj.setNextAdvTime(nft);

        return fstAdvObj;
    }

    /**
     * Put deck data in a PublicAdvisory object.
     * 
     * @return
     */
    private PublicAdvisory getPublicAdvisoryObject() {

        PublicAdvisory pubAdvObj = new PublicAdvisory();
        pubAdvObj.setIntermediate(isIntermediate);

        // Set forecast data, include TAU 0, 3, forecast, outlook.
        setForecastData(pubAdvObj);

        // Adjust advisory header info
        AdvisoryHeader hdr = new AdvisoryHeader(advHeader);
        hdr.setCategory(AdvisoryType.TCP.name());

        // Local time zone.
        hdr.setTimeZone(advTimer.getTimeZone().name());
        hdr.setAdvName(WordUtils.capitalizeFully(hdr.getAdvName()));

        // Adjust wfo id/node/basin.
        String bsnDsg = advTimer.getBasinDesignator();
        if (!"C".equals(bsnDsg) && !hdr.isIssuedByCPHC()) {
            if (hdr.isIssuedByWPC()) {
                hdr.setWfo("KWNH");
            } else {
                hdr.setWfo("KNHC");
            }
        } else {
            hdr.setWfo("PHFO");
        }

        // Check who is issuing the final advisory.
        String lastAdvFrom = atcfSites.getAtcfSitebyID("NHC").getCenter();
        if ("C".equals(bsnDsg)) {
            lastAdvFrom = atcfSites.getAtcfSitebyID("CPHC").getCenter();
        } else {
            if (hdr.isIssuedByWPC()) {
                lastAdvFrom = atcfSites.getAtcfSitebyID("WPC").getCenter();
            }
        }
        hdr.setLastAdvFrom(lastAdvFrom);

        // Adjust advisory number phrase
        String advNumPhrase = AdvisoryUtil.getAdvNumPhase(
                isIntermediate ? AdvisoryType.TCP_A : AdvisoryType.TCP,
                advInfo.isSpecialAdv(), hdr.getAdvClass(), hdr.getAdvName(),
                hdr.getAdvNumber());
        hdr.setAdvNumPhrase(advNumPhrase);

        pubAdvObj.setHeader(hdr);

        // Set info for headline
        if (headline.trim().startsWith(".")) {
            pubAdvObj.setHeadline(headline.trim().toUpperCase());
        }

        // Set info for summary section
        pubAdvObj.setSummary(advSummary);

        // Set watch/warning.
        pubAdvObj.setWatchWarn(warnings);

        // Set reminder for watch warning (nhc_writeadv.f line 3197-3331)
        if (!warnings
                .startsWith(AdvisoryUtil.DEFAULT_WARNINGS.substring(0, 13))) {

            String wwRemind = getWatchWarnReminder(hdr.isIssuedByCPHC(),
                    advInfo.isWwUS(), advInfo.isWwIntl());
            pubAdvObj.setWatchWarnReminder(wwRemind);
        }

        // Set hazards sections.
        pubAdvObj.setHazards(hazards);

        // Build discussion data.
        AdvisoryDiscussion advDis = buildAdvisoryDiscussion(
                pubAdvObj.getTau3());
        pubAdvObj.setDiscus(advDis);

        /*
         * Set intermediate public advisory time (+6 from TAU=0)..
         * 
         * Note: the final advisory message will likely be replaced by the
         * forecaster with a snippet in the text editor.
         */
        ForecastTime nit = advTimer.getLocalForecastTime(6, false);
        pubAdvObj.setNextIntermTime(nit);

        // Set next complete advisory time (+9 from TAU=0).
        ForecastTime nft = advTimer.getLocalForecastTime(9, false);
        pubAdvObj.setNextAdvTime(nft);

        // Set previous discussion.
        pubAdvObj.setPrevDiscus(discus);

        return pubAdvObj;
    }

    /**
     * Put deck data into an AviationcAdvisory object.
     *
     * <pre>
     * Create ICAO aviation bulletin (2008 Version), which includes routine
     * advisory time-based +06, +12, +18 and +24 hour interpolated forecast
     * information.
     *
     * Input:
     *      Forecast track
     *
     * Output: 
     *     stormID.icaoms.new    ICAO aviation bulletin storm.
     *     stormID.icaoms.dat    Diagnostic meta file for quick plots
     * </pre>
     *
     * @return AviationAdvisory
     */
    private AviationAdvisory getICAOAdvisoryObject() {

        // Check for special TAU.

        AviationAdvisory icaoAdvObj = new AviationAdvisory();

        // Compose HEADER record.
        AdvisoryHeader hdr = new AdvisoryHeader(advHeader);
        hdr.setCategory(AdvisoryType.TCA.name());
        hdr.setAdvClass(hdr.getAdvClass().toUpperCase());
        hdr.setIssuedBy(hdr.getIssuedBy().toUpperCase());
        hdr.setIssuedByBackup(hdr.getIssuedByBackup().toUpperCase());
        hdr.setTimeZone(AdvisoryTimeZone.UTC.name());

        String bsnDsg = advTimer.getBasinDesignator();

        /*
         * Adjust node and basin.
         *
         * Important: This is specific to TCA while other advisories use
         * AT/EP/CP for basin.
         *
         * World Meteorological Organization (AWIPS) headers:
         *
         * FKNT21-25 KNHC (MIATCANT1-5) - Atlantic
         *
         * FKPZ21-25 KNHC (MIATCAPZ1-5) - E. Pacific
         *
         * FKPA21-25 PHFO (HFOTCAPA1-5) - C. Pacific
         */
        if ("A".equals(bsnDsg) && !hdr.isIssuedByCPHC()) {
            // NHC, Atlantic
            hdr.setBasin("NT");
        } else if ("P".equals(bsnDsg) && !hdr.isIssuedByCPHC()) {
            // NHC, East Pacific
            hdr.setBasin("PZ");
        } else if ("P".equals(bsnDsg) && hdr.isIssuedByCPHC()) {
            // CPHC, East Pacific
            hdr.setBasin("PZ");
        } else if ("C".equals(bsnDsg)) {
            // CPHC, Central Pacific
            hdr.setBasin("PA");
        }

        // Adjust wfo id.
        if (!"C".equals(bsnDsg) && !hdr.isIssuedByCPHC()) {
            hdr.setWfo("KNHC");
        } else {
            hdr.setWfo("PHFO");
        }

        // Adjust advisory number phrase
        String advNumPhrase = AdvisoryUtil.getAdvNumPhase(AdvisoryType.TCA,
                advInfo.isSpecialAdv(), hdr.getAdvClass(), hdr.getAdvName(),
                hdr.getAdvNumber());
        hdr.setAdvNumPhrase(advNumPhrase.toUpperCase());

        // Adjust primary issuing office address.
        String issueBy = atcfSites.getAtcfSitebyID("CPHC").getIssueByOffice();
        if (!("C".equals(bsnDsg)) && !hdr.isIssuedByCPHC()) {
            issueBy = atcfSites.getAtcfSitebyID("NHC").getIssueByOffice();
        }
        hdr.setIssuedBy(issueBy.toUpperCase());

        icaoAdvObj.setHeader(hdr);

        /*
         * Adjust TCAC - issue center.
         * 
         * Note: this is legacy code but may not work if WPC is somehow the
         * primary site?
         */
        if (!"C".equals(bsnDsg)) {
            icaoAdvObj.setIssueCenter("KNHC");
        } else {
            icaoAdvObj.setIssueCenter("PHFO");
        }

        /*
         * Set date in advisory based on current date/time and forecast
         * date/time at +6, +12, +18, +24 hours
         */
        for (int ii = 0; ii < 5; ii++) {
            IcaoForecast fcst = new IcaoForecast();

            // Forecast time in UTC
            int tau = icaoInfo.getFcstTime()[ii];
            ForecastTime fdt = advTimer.getUTCForecastTime(tau, true);
            fcst.setTime(fdt);

            float vmax = (int) icaoInfo.getFcstMaxWind()[ii];
            if (vmax >= 0) {
                float lat = icaoInfo.getFcstLat()[ii];
                fcst.setLat((lat < 0) ? "S" : "N" + minPos(lat).substring(1));

                float lon = icaoInfo.getFcstLon()[ii];
                // Dateline crossing
                if (lon > 180.0) {
                    lon = 360.0f - lon;
                }
                fcst.setLon((lon > 0) ? "E" : "W" + minPos(lon));

                // Round max wind to nearest 5.
                int vm = AdvisoryUtil.round(vmax, 5);
                fcst.setMaxWind(String.format("%03d", vm));
            }

            if (ii == 0) {
                icaoAdvObj.setTau3Fcst(fcst);
            } else {
                icaoAdvObj.getInterpFcst().add(fcst);
            }
        }

        /*
         * Set extra info neeeded at TAU 3.
         */
        IcaoForecast tau3Fcst = icaoAdvObj.getTau3Fcst();

        // Storm movement (direction + speed) - check stationary case.
        String dir1 = AdvisoryUtil.getDirection(icaoInfo.getDir(), true);
        int spd1 = icaoInfo.getSpd();
        if (isStationary) {
            tau3Fcst.setMovement("STNRY");
        } else {
            tau3Fcst.setMovement(dir1 + " " + spd1);
        }

        // MSLP
        tau3Fcst.setMslp(String.format("%04d", icaoInfo.getMslp()));

        /*
         * Note: Storm intensity change (a new field added in 2021). Update when
         * NHC implements it.
         *
         * Values: INTSF --> Intensifying; WKN -->Weakening NC --> No change
         */
        tau3Fcst.setIntensityChange("NC");

        // Set next complete advisory time (+9 from TAU=0).
        ForecastTime nft = advTimer.getUTCForecastTime(9, false);
        icaoAdvObj.setNextAdvTime(nft);

        return icaoAdvObj;
    }

    /**
     * Put deck data in a Discussion object.
     * 
     * @return
     */
    private Discussion getDiscussionObject() {

        Discussion disAdvObj = new Discussion();

        // Set forecast data, include TAU 0, 3, forecast, outlook.
        setForecastData(disAdvObj);

        // Adjust advisory header info
        AdvisoryHeader hdr = new AdvisoryHeader(advHeader);
        hdr.setCategory(AdvisoryType.TCD.name());

        // Local time zone.
        hdr.setTimeZone(advTimer.getTimeZone().name());
        hdr.setAdvName(WordUtils.capitalizeFully(hdr.getAdvName()));

        // Adjust wfo id/node/basin.
        String bsnDsg = advTimer.getBasinDesignator();
        if (!"C".equals(bsnDsg) && !hdr.isIssuedByCPHC()) {
            hdr.setWfo("KNHC");
        } else {
            hdr.setWfo("PHFO");
            hdr.setBasin("CP");
        }

        // Adjust advisory number phrase
        String advNumPhrase = AdvisoryUtil.getAdvNumPhase(AdvisoryType.TCD,
                advInfo.isSpecialAdv(), hdr.getAdvClass(), hdr.getAdvName(),
                hdr.getAdvNumber());
        hdr.setAdvNumPhrase(advNumPhrase);

        disAdvObj.setHeader(hdr);

        // Set previous TCD data.
        disAdvObj.setPrevTCD(previousTCD);

        return disAdvObj;
    }

    /**
     * Create a json schema and data object to parse Handlebars-formatted text.
     * 
     * @param pubAdvObj
     * @return
     */
    @SuppressWarnings({ "deprecation", "rawtypes" })
    private static String buildAdvFromJson(Object advObj) {

        String advisory = "";
        Class cls = PublicAdvisory.class;
        String templateFile = AdvisoryType.TCP.getTemplate();

        if (advObj instanceof ForecastAdvisory) {
            cls = ForecastAdvisory.class;
            templateFile = AdvisoryType.TCM.getTemplate();
        } else if (advObj instanceof AviationAdvisory) {
            cls = AviationAdvisory.class;
            templateFile = AdvisoryType.TCA.getTemplate();
        } else if (advObj instanceof Discussion) {
            cls = Discussion.class;
            templateFile = AdvisoryType.TCD.getTemplate();
        } else if (advObj instanceof PublicAdvisory
                && ((PublicAdvisory) advObj).isIntermediate()) {
            templateFile = AdvisoryType.TCP_A.getTemplate();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(advObj);

            JsonSchema schema = mapper.generateJsonSchema(cls);

            String baseDir = AtcfConfigurationManager.getInstance()
                    .getTemplateDirectory();

            LocalizationFile schemaFile = AtcfConfigurationManager.getInstance()
                    .saveAtcfJson(templateFile + JSON_SUFFIX,
                            mapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(schema));

            TextProductBuilder b = new TextProductBuilder(baseDir, templateFile,
                    TEMPLATE_SUFFIX, schemaFile.getPath());

            // Note: validation?
            advisory = b.build(jsonString, false);
        } catch (Exception e) {
            logger.error("AdvisoryBuilder - Failed to build text product.", e);
        }

        return advisory;
    }

    /**
     * Build advisory header information.
     */
    private AdvisoryHeader buildAdvisoryHeader(String basin, String category) {

        AdvisoryHeader header = new AdvisoryHeader();
        header.setSiteId(mainSite.getId());
        header.setWfo(mainSite.getWfoId());
        header.setIssuedBy(mainSite.getIssueByOffice());

        header.setBasin(basin);
        header.setAdvNumber(advInfo.getAdvNum());
        header.setStormId(storm.getStormId().toUpperCase());
        header.setBinNumber(advInfo.getAwipsBinNum());

        header.setCategory(category);

        header.setAdvClass(AdvisoryUtil.getAdvisoryClass(
                currentFcstRecord.getIntensity(), advInfo.isPotentialTC()));

        String advName = AdvisoryUtil.getAdvisoryName(storm.getStormName(),
                currentFcstRecord.getIntensity(), advBasin, isPTC);
        header.setAdvName(advName);

        // Get forecaster's first name by initial.
        String forecaster = fullInitials.getFirstName(advInfo.getForecaster());
        header.setForecaster(WordUtils.capitalizeFully(forecaster));

        /*
         * Advisory time zone adjust based on basin, daylight saving, and
         * longitude.
         */
        AdvisoryTimeZone advTz = advTimer.getTimeZone();
        header.setTimeZone(advTz.name());

        ZonedDateTime cal = ZonedDateTime.now()
                .withHour(firstFcstRecord.getFcstHour());
        String issueTime = cal.format(
                DateTimeFormatter.ofPattern("HH00 a z EEE MMM dd yyyy"));

        header.setAdvTime(issueTime);
        header.setIssuedTime(issueTime);

        // Flags for final adv, PTC, and pub frequency is 3.
        header.setFinalAdv(advInfo.isFinalAdv());
        header.setPotentialTC(advInfo.isPotentialTC());
        header.setPubFreqEq3(advInfo.getFrequency() == 3);

        // Set the main issuing office.
        String mSite = mainSite.getId();
        if ("NHC".equalsIgnoreCase(mSite)) {
            header.setIssuedByNHC(true);
        } else if ("CPHC".equalsIgnoreCase(mSite)) {
            header.setIssuedByCPHC(true);
        } else if ("WPC".equalsIgnoreCase(mSite)) {
            header.setIssuedByWPC(true);
        }

        // Check if an office other than the main office is set as the backup.
        header.setBackup(false);
        if (backupSite != null) {
            String bSite = backupSite.getId();
            if (!bSite.equalsIgnoreCase(mSite)) {
                header.setBackup(true);
                header.setIssuedByBackup(
                        "Issued by " + backupSite.getIssueByOffice());
                if ("NHC".equalsIgnoreCase(bSite)) {
                    header.setBackupByNHC(true);
                } else if ("CPHC".equalsIgnoreCase(bSite)) {
                    header.setBackupByCPHC(true);
                } else if ("WPC".equalsIgnoreCase(bSite)) {
                    header.setBackupByWPC(true);
                }
            }
        }

        return header;
    }

    /*
     * Get a map of ForecastData for TAUs existing in forecast track.
     * 
     * @ return Map<Integer, ForecastData>
     */
    private Map<Integer, ForecastData> prepareAdvData() {

        Map<Integer, ForecastData> advDataMap = new LinkedHashMap<>();
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {

            if (recs != null && !recs.isEmpty()) {
                int tau = recs.get(0).getFcstHour();

                if (advInfo.isSpecialAdv() && tau == AtcfTaus.TAU3.getValue()) {
                    continue;
                }

                ForecastData fData = new ForecastData();

                // Set all times.
                setForecastDataTimes(fData, tau);

                /*
                 * Set storm state. Need special handling for TAU 3 - see
                 * fxTable() in nhc_writeeadv.f (line 10314-10320)
                 */
                String stormState = "";
                if (tau > 0 && tau < 12) {
                    if (advInfo.isPotentialTC()) {
                        stormState = "...POTENTIAL TROP CYCLONE";
                    } else {
                        String intensity = recs.get(0).getIntensity()
                                .toUpperCase();
                        if ("LO".equals(intensity) || "EX".equals(intensity)) {
                            stormState = "...POST-TROPICAL";
                        }
                    }
                } else {
                    stormState = getStormState(tau);
                }

                fData.setState(stormState);

                // Set lat/lon
                float lat = recs.get(0).getClat();
                fData.setLat(String.format("%4.1f%s", Math.abs(lat),
                        (lat < 0) ? "S" : "N"));
                fData.setFullLat(String.format("%.1f %s", Math.abs(lat),
                        (lat < 0) ? "South" : "North"));

                float lon = recs.get(0).getClon();
                // Dateline crossing
                if (lon > 180.0) {
                    lon = 360.0f - lon;
                }
                fData.setLon(String.format("%5.1f%s", Math.abs(lon),
                        (lon > 0) ? "E" : "W"));
                fData.setFullLon(String.format("%.1f %s", Math.abs(lon),
                        (lon > 0) ? "East" : "West"));

                // Set max wind, gust, wind radii, and wave radii (TAU 3).
                float windMax = recs.get(0).getWindMax();
                if (windMax > 0 && windMax < AbstractAtcfRecord.RMISSD) {

                    WindData wndData = new WindData();
                    // Round max wind/gust to nearest 5.
                    int vm = AdvisoryUtil.round(windMax, 5);
                    wndData.setMaxWind(String.format("%3d", vm));

                    int mvm = AdvisoryUtil.round(((float) (windMax
                            * AtcfVizUtil.NAUTICAL_MILES_TO_MILES)), 5);
                    wndData.setMaxWindMph(String.format("%3d", mvm));

                    int kvm = AdvisoryUtil.round(
                            (float) (windMax * AtcfVizUtil.NM2M / 1000.0), 5);
                    wndData.setMaxWindKmh(String.format("%3d", kvm));

                    int gst = AdvisoryUtil.round(recs.get(0).getGust(), 5);
                    wndData.setGust(String.format("%3d", gst));

                    // Wind Radii if existing in at least one quadrant
                    List<RadiiData> wndRii = new ArrayList<>();
                    for (ForecastTrackRecord rec : recs) {
                        int radWind = (int) rec.getRadWind();
                        int[] wi = rec.getWindRadii();
                        if (wi[0] > .1 || wi[1] > .1 || wi[2] > .1
                                || wi[3] > .1) {
                            RadiiData rad = new RadiiData();
                            rad.setRad(String.format("%2d", radWind));
                            for (int ii = 0; ii < 4; ii++) {
                                rad.getQuad()[ii] = String.format("%3d",
                                        wi[ii]);
                            }
                            wndRii.add(rad);
                        }
                    }

                    if (!wndRii.isEmpty()) {
                        wndData.setWindRadii(wndRii);
                    }

                    // 12 ft seas radii (TAU 3), if in at least one quadrant
                    int radWave = (int) recs.get(0).getRadWave();
                    if (radWave == 12 && tau > 0 && tau < 12) {
                        int[] wi = recs.get(0).getWaveRadii();
                        if (wi[0] > .1 || wi[1] > .1 || wi[2] > .1
                                || wi[3] > .1) {
                            RadiiData rad = new RadiiData();
                            rad.setRad(String.format("%2d", radWave));
                            for (int ii = 0; ii < 4; ii++) {
                                rad.getQuad()[ii] = String.format("%3d",
                                        wi[ii]);
                            }

                            wndData.setWaveRadii(rad);
                        }
                    }

                    fData.setWindData(wndData);
                }

                advDataMap.put(tau, fData);
            }
        }

        return advDataMap;
    }

    /*
     * Builds a AdvisoryDiscussion instance from a ForecastData.
     *
     * @param fcstData ForecastData
     *
     * @return AdvisoryDiscussion
     */
    private AdvisoryDiscussion buildAdvisoryDiscussion(ForecastData fcstData) {

        AdvisoryDiscussion advDiscus = new AdvisoryDiscussion();

        /*
         * Current position info (re-stated)
         */
        advDiscus.setLocalTime(fcstData.getLocalTimes().getHhmmaz());
        advDiscus.setUtcTime(fcstData.getTimes().getHhmm());
        advDiscus.setFullLat(fcstData.getFullLat().trim());
        advDiscus.setFullLon(fcstData.getFullLon().trim());
        advDiscus.setWindMph(advSummary.getWindMph());
        advDiscus.setWindKmh(advSummary.getWindKmh());
        advDiscus.setPressureMb(advSummary.getPressureMb());
        advDiscus.setPressureIn(advSummary.getPressureIn());

        advDiscus.setAdvClass(advHeader.getAdvClass());
        advDiscus.setAdvName(WordUtils.capitalizeFully(advHeader.getAdvName()));

        // Intensity Status & "centered/located"
        String intensityStatus = "center of";
        String centerOrLocated = "was located";
        String advCls = advDiscus.getAdvClass().toUpperCase();
        if (isPTC) {
            intensityStatus = "disturbance";
            centerOrLocated = "was centered";
        } else if (advCls.contains("REMNANTS")) {
            intensityStatus = "remnants of";
            centerOrLocated = "were located";
        }

        advDiscus.setIntensityStatus(intensityStatus);
        advDiscus.setCenterOrLocated(centerOrLocated);

        // Direction of motion
        String longIntensity = "The ";
        String movingOrStay = isStationary ? " is stationary"
                : " is moving toward the ";

        String expectedMotion = advDiscus.getExpectedMotion();
        String insertMotion = advDiscus.getInsertMotion();

        String intensity = currentFcstRecord.getIntensity().toUpperCase();
        switch (intensity) {
        case "TD":
        case "SD":
            longIntensity += "depression";
            break;
        case "SS":
            longIntensity += "storm";
            if (!isStationary) {
                expectedMotion = ".";
                insertMotion = "";
            }
            break;
        case "PT":
        case "EX":
        case "LO":
            longIntensity += "post-tropical cyclone";
            if (isStationary) {
                expectedMotion = " ";
            }
            break;
        case "DB":
            longIntensity += "remnants";
            movingOrStay = movingOrStay.replace("is", "are");
            if (!isStationary) {
                expectedMotion = " ";
            }
            break;
        case "TS":
        case "HU":
            longIntensity = advDiscus.getAdvName();
            break;
        default:
            break;
        }

        if (isPTC) {
            longIntensity = "system";
            if (isStationary) {
                expectedMotion = " ";
            }
        }

        advDiscus.setLongIntensity(longIntensity);
        advDiscus.setMovingOrStay(movingOrStay);
        advDiscus.setExpectedMotion(expectedMotion);
        advDiscus.setInsertMotion(insertMotion);

        // Direction & speed
        String fullDir = AdvisoryUtil
                .getDirection(currentFcstRecord.getStormSped(), false);
        String spdMph = advSummary.getMovementMph();
        String spdKmh = advSummary.getMovementKmh();

        if (!isStationary) {
            String movingDirSpd = String.format("%s near %s mph (%s km/h)",
                    fullDir, spdMph.trim(), spdKmh.trim());
            advDiscus.setMovingDirSpd(movingDirSpd);
        }

        /*
         * Storm category discussion
         * 
         * Note: HSU - only write cat for major hurricane status (Cat3-5)
         */
        String categoryNum = "";
        int maxWind = AdvisoryUtil
                .round(((float) (currentFcstRecord.getWindMax()
                        * AtcfVizUtil.NAUTICAL_MILES_TO_MILES)), 5);

        if (maxWind >= 65 && maxWind <= 80) {
            categoryNum = "one";
        }

        if (maxWind >= 85 && maxWind <= 95) {
            categoryNum = "two";
        }

        if (maxWind >= 100 && maxWind <= 110) {
            categoryNum = "3";
        }

        if (maxWind >= 115 && maxWind <= 135) {
            categoryNum = "4";
        }

        if (maxWind > 135) {
            categoryNum = "5";
        }

        if (categoryNum.length() == 1) {
            advDiscus.setStormCategory(categoryNum);
        }

        // Intensity tendency
        float vmax48 = findTau48MaxWind();
        float vcur = currentFcstRecord.getWindMax();
        float vdiff = vmax48 - vcur;
        String intensityTendency = "";
        if (vmax48 > 0) {
            if (vdiff > 5) {
                intensityTendency = "Some strengthening";
            } else if (vdiff < -5) {
                intensityTendency = "Some weakening";
            } else {
                intensityTendency = "Little change in strength";
            }
        }
        advDiscus.setIntensityTendency(intensityTendency);

        // Flag to genesis information
        if (isPTC || "DB".equals(intensity) || "LO".equals(intensity)) {
            advDiscus.setAddGenesisInfo(true);
        }

        // Wind radii extent of the cyclone for max wind > 64 kts.
        if (vcur > 64) {
            ForecastTrackRecord rec64 = getSingleTauData(currentTau,
                    WindRadii.RADII_64_KNOT.getValue());

            int windRad = 0;
            if (rec64 != null) {
                int[] radii = rec64.getWindRadii();

                for (int ii = 0; ii < 4; ii++) {
                    if (radii[ii] > windRad) {
                        windRad = radii[ii];
                    }
                }
            }

            int windRad64Mph = AdvisoryUtil.round(
                    (float) (windRad * AtcfVizUtil.NAUTICAL_MILES_TO_MILES), 5);
            int windRad64Kmh = AdvisoryUtil
                    .round((windRad * AtcfVizUtil.NM2M / 1000), 5);

            advDiscus.setWindGr64(true);
            advDiscus.setWindRad64Mph(String.format("%3d", windRad64Mph));
            advDiscus.setWindRad64Kmh(String.format("%3d", windRad64Kmh));
        }

        // Wind radii extent of the cyclone for max wind > 34 kts.
        if (vcur > 34) {
            int windRad = 0;
            if (currentFcstRecord != null) {
                int[] radii = currentFcstRecord.getWindRadii();

                for (int ii = 0; ii < 4; ii++) {
                    if (radii[ii] > windRad) {
                        windRad = radii[ii];
                    }
                }
            }

            int windRad34Mph = AdvisoryUtil.round(
                    (float) (windRad * AtcfVizUtil.NAUTICAL_MILES_TO_MILES), 5);
            int windRad34Kmh = AdvisoryUtil
                    .round((windRad * AtcfVizUtil.NM2M / 1000), 5);

            advDiscus.setWindGr34(true);
            advDiscus.setWindRad34Mph(String.format("%3d", windRad34Mph));
            advDiscus.setWindRad34Kmh(String.format("%3d", windRad34Kmh));
        }

        // Check if this is a subtropical storm.
        if ("SS".equals(intensity)) {
            advDiscus.setSubtropicalStorm(true);
        }

        /*
         * Minimum central pressure of cyclone - info already in
         * AdvsiorySummary.
         */

        return advDiscus;
    }

    /*
     * Set forecast track data for TCM.
     * 
     * @param advObj ForecastAdvisory
     */
    private void setForecastData(AtcfAdvisory advObj) {

        advObj.setTau0(advData.get(0));

        int tau3 = 3;
        for (Integer tau : advData.keySet()) {
            if (tau > 0 && tau < 12) {
                tau3 = tau;
                break;
            }
        }

        advObj.setTau3(advData.get(tau3));

        List<ForecastData> fcst = new ArrayList<>();
        List<ForecastData> outlook = new ArrayList<>();
        List<ForecastData> extended = new ArrayList<>();

        boolean dissipated = false;
        for (Integer tau : TCM_FORECAST_TAUS) {
            ForecastData fdt = advData.get(tau);

            /*
             * Handle "Dissipated" case. Generate a "dissipated" line for TAU
             * only if when:
             * 
             * (1) there is not forecast data for the TAU, and (2) its storm
             * status is set as "DD" (dissipated), and (3) the storm has not
             * been set as "dissipated" before in the previous TAU.
             */
            if (fdt != null) {
                dissipated = false;
            } else {
                String stateId = getStormState(tau);
                if ("DD".equalsIgnoreCase(stateId) && !dissipated) {
                    fdt = new ForecastData();
                    setForecastDataTimes(fdt, tau);
                    fdt.setState("..." + stmStates.getStormStatebyID("DD")
                            .getDescription().toUpperCase());
                    fdt.setDissipated(true);
                }
            }

            // Group data into forecast/outlook/extended.
            if (fdt != null) {
                if (tau <= forecastRange) {
                    fcst.add(fdt);
                } else if (tau <= outlookRange) {
                    outlook.add(fdt);
                } else {
                    extended.add(fdt);
                }
            }
        }

        if (!fcst.isEmpty()) {
            advObj.setForecast(fcst);
        }

        if (!outlook.isEmpty()) {
            advObj.setOutlook(outlook);
        }

        if (!extended.isEmpty()) {
            advObj.setExtended(extended);
        }

    }

    /*
     * Set times for a ForecastData at a given TAU, in UTC time.
     * 
     * @param fData ForecastData
     * 
     * @param tau
     */
    private void setForecastDataTimes(ForecastData fData, int tau) {

        // Forecast time in UTC
        ForecastTime fdt = advTimer.getUTCForecastTime(tau, true);
        fData.setTimes(fdt);

        // Forecast time in local
        ForecastTime flt = advTimer.getLocalForecastTime(tau, true);
        fData.setLocalTimes(flt);
    }

    /*
     * Set average track error for Atlantic, East Pacific, or Central Pacific 4-
     * and 5-day disclaimer in the Extended Outlook section.
     *
     * Taken from legacy nhc_writeadv.f (line 1261-1320) and data is provided by
     * Mike Brennan, 2018 & Tom Birchard, CPHC, 2017.
     *
     * TODO Set via a configuration file in the future.
     *
     * @param advObj
     */
    private void setForecastTrackError(ForecastAdvisory advObj) {
        String advBsn = advObj.getHeader().getBasin();
        if ("AT".equalsIgnoreCase(advBsn)) {
            advObj.setAvgTrkErr4Day("150");
            advObj.setAvgTrkErr5Day("175");
            advObj.setAvgIntensityErr("15");
        } else if ("EP".equalsIgnoreCase(advBsn)) {
            advObj.setAvgTrkErr4Day("100");
            advObj.setAvgTrkErr5Day("150");
            advObj.setAvgIntensityErr("15");
        } else {
            advObj.setAvgTrkErr4Day("150");
            advObj.setAvgTrkErr5Day("200");
            advObj.setAvgIntensityErr("20");
        }
    }

    /*
     * Get storm state description for a given TAU.
     * 
     * Note: For storm state "NR", keep state as blank.
     */
    private String getStormState(int tau) {
        String state = "";
        String fcstType = advInfo.getForecastType(tau);
        StormStateEntry ssEntry = stmStates.getStormStatebyID(fcstType);
        if (ssEntry != null && !(ssEntry.getDescription().trim().isEmpty())) {
            state = "..." + (ssEntry.getName().toUpperCase());
        }

        return state;
    }

    /*
     * Create a product header reference for the intermediate public advisory
     * (iTCP) to embed in the TCM (forecast advisory)
     * 
     * IPubHeader is a 21-character header reference for the iTCP WMO
     * product/AWIPS product). Example: WTNT32 KNHC/MIATCPAT2
     * 
     * @param basin2 2-letter advisory basin id ("AT", "EP", "CP")
     * 
     * @param bin Bin number for a storm (1-5)
     * 
     * @return iPubHeader
     */
    private String getIPubHeader(String basin2, int bin, boolean issuedByCPHC,
            boolean issuedByWPC) {
        // Find the site.
        String siteId = "CPHC";
        if (!basin2.toUpperCase().startsWith("C") && !issuedByCPHC) {
            siteId = "NHC";
            if (issuedByWPC) {
                siteId = "WPC";
            }
        }

        AtcfSiteEntry site = atcfSites.getAtcfSitebyID(siteId);
        String ccc = null;
        String nnn = "TCP";
        String xxx = basin2.toUpperCase() + bin;

        GetPartialAfosIdRequest request = new GetPartialAfosIdRequest();
        request.setCccc(site.getWfoId());
        request.setNnn(nnn);
        request.setXxx(xxx);
        try {
            AfosWmoIdDataContainer result = (AfosWmoIdDataContainer) ThriftClient
                    .sendRequest(request);
            if (!result.getIdList().isEmpty()) {
                ccc = result.getIdList().get(0).getAfosid().substring(0, 3);
            }
        } catch (VizException e) {
            logger.error("failed to look up AFOS ID: " + e.toString(), e);
        }

        StringBuilder ihdr = new StringBuilder();
        ihdr.append("WTNT3" + bin + " " + site.getWfoId() + "/");
        ihdr.append(ccc != null ? ccc : "").append(nnn).append(xxx);

        return ihdr.toString();
    }

    /*
     * Get the reminder for watch warning (nhc_writeadv.f line 3197-3331)
     * 
     * @param isIssuedByCPHC Flag to indicate if advisory is issued by CPHC
     * 
     * @param isWwUs Flag to indicate if this watch/warn is for US
     * 
     * @param isWwIntl Flag to indicate if this watch/warn is for international
     * 
     * @return String Watch/Warn reminder string
     */
    private String getWatchWarnReminder(boolean isIssuedByCPHC, boolean isWwUS,
            boolean isWwIntl) {

        final String areaInfo = "For storm information specific to your area, please monitor \n";
        String wwRemind;
        if (!isIssuedByCPHC) {
            // Atlantic/East Pacific Region
            if (isWwUS) {
                if (isWwIntl) {
                    // US + International
                    wwRemind = "For storm information specific to your area in the United\n"
                            + "States, including possible inland watches and warnings, please \n"
                            + "monitor products issued by your local National Weather Service \n"
                            + "forecast office. For storm information specific to your area \n"
                            + "outside of the United States, please monitor products issued by \n"
                            + "your national meteorological service.";

                } else {
                    // US only
                    wwRemind = "For storm information specific to your area, including possible\n"
                            + "inland watches and warnings, please monitor products issued by your\n"
                            + "local National Weather Service forecast office.";
                }
            } else {
                // International only
                wwRemind = areaInfo
                        + "products issued by your national meteorological service.";
            }
        } else {
            // Central Pacific Region
            if (isWwUS) {
                if (isWwIntl) {
                    // US + International
                    wwRemind = areaInfo
                            + "products issued by the National Weather Service in\n"
                            + "Honolulu Hawaii. For storm information specific to your area \n"
                            + "outside of the United States, please monitor products issued by \n"
                            + "your national meteorological service.";

                } else {
                    // US only
                    wwRemind = areaInfo
                            + "products issued by the National Weather Service in\n"
                            + "Honolulu Hawaii.";
                }
            } else {
                // International only
                wwRemind = areaInfo
                        + "products issued by your national meteorological service.";
            }
        }

        return wwRemind;

    }

    /*
     * Determine whether storm is stationary (storm motion = zero). If so, the
     * caller may choose to set its direction of motion to North/360 degrees.
     * 
     * Note: storm speed is in range of 0 to 999 kt & missing value is 99999.
     * 
     * @return boolean True/false
     */
    private boolean isStormStationary() {
        float stormSpd = currentFcstRecord.getStormSped();
        return (stormSpd <= 0 || stormSpd > 1000);
    }

    /*
     * Obtain/determine the 48-hour forecast maximum wind speed by back
     * searching records from TAU 48.
     *
     * @return float Max wind at TAU 48.
     */
    private float findTau48MaxWind() {
        float vmax48 = 0;
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {
            if (recs != null && !recs.isEmpty()) {
                if (recs.get(0).getFcstHour() > 48) {
                    break;
                }

                vmax48 = recs.get(0).getWindMax();
            }
        }

        return vmax48;
    }

    /**
     * Convert a floating lat/lon position to a character minute lat/lon
     * position.
     *
     * nhc_writeAdv.f (line 8152 ~ 8202)
     *
     * @param pos
     *            Latitude or longitude in decimal degrees
     * @return minPos Latitude or longitude in DDDMM notation
     *
     */
    private static String minPos(float pos) {
        float fltPos;
        float p;
        int iMinute;
        int i;

        // Check bad data
        if (pos < -900.) {
            iMinute = 99999;
        } else {

            // Use positive value to convert.
            pos = Math.abs(pos);

            fltPos = pos * 10;

            if (fltPos < 10.) {
                iMinute = (int) (fltPos * 6);
            } else if (fltPos < 100.) {
                i = (int) (fltPos * 0.1);
                p = i * 10;
                iMinute = (int) ((i * 100) + ((fltPos % p) * 6));
            } else {
                i = (int) (fltPos * 0.1);
                p = i;
                iMinute = (int) (i * 100 + ((fltPos % p) * 6));
            }
        }

        return String.format("%05d", iMinute);
    }

    /*
     * Get forecast track data for a given TAU.
     * 
     * @param tau Forecast hour
     * 
     * @param windRadii WindRadii
     * 
     * @return ForecastTrackRecord (null if not found)
     */
    private ForecastTrackRecord getSingleTauData(int tau, int windRadii) {
        ForecastTrackRecord frec = null;
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {
            if (recs != null && !recs.isEmpty()) {
                for (ForecastTrackRecord rec : recs) {
                    if (rec.getFcstHour() == tau
                            && (int) rec.getRadWind() == windRadii) {
                        frec = rec;
                        break;
                    }
                }
            }

            if (frec != null) {
                break;
            }
        }

        return frec;
    }

    /**
     * @return the forecastRange
     */
    public int getForecastRange() {
        return forecastRange;
    }

    /**
     * @param forecastRange
     *            the forecastRange to set
     */
    public void setForecastRange(int forecastRange) {
        this.forecastRange = forecastRange;
    }

    /**
     * @return the outlookRange
     */
    public int getOutlookRange() {
        return outlookRange;
    }

    /**
     * @param outlookRange
     *            the outlookRange to set
     */
    public void setOutlookRange(int outlookRange) {
        this.outlookRange = outlookRange;
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    /**
     * @return the dtg
     */
    public String getDtg() {
        return dtg;
    }

    /**
     * @param dtg
     *            the dtg to set
     */
    public void setDtg(String dtg) {
        this.dtg = dtg;
    }

    /**
     * @return the fcstTrackData
     */
    public Map<String, List<ForecastTrackRecord>> getFcstTrackData() {
        return fcstTrackData;
    }

    /**
     * @param fcstTrackData
     *            the fcstTrackData to set
     */
    public void setFcstTrackData(
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {
        this.fcstTrackData = fcstTrackData;
    }

    /**
     * @return the advInfo
     */
    public AdvisoryInfo getAdvInfo() {
        return advInfo;
    }

    /**
     * @param advInfo
     *            the advInfo to set
     */
    public void setAdvInfo(AdvisoryInfo advInfo) {
        this.advInfo = advInfo;
    }

    /**
     * @return the advSummary
     */
    public AdvisorySummary getAdvSummary() {
        return advSummary;
    }

    /**
     * @param advSummary
     *            the advSummary to set
     */
    public void setAdvSummary(AdvisorySummary advSummary) {
        this.advSummary = advSummary;
    }

    /**
     * @return the headline
     */
    public String getHeadline() {
        return headline;
    }

    /**
     * @param headline
     *            the headline to set
     */
    public void setHeadline(String headline) {
        this.headline = headline;
    }

    /**
     * @return the tcp
     */
    public String getTcp() {
        return tcp;
    }

    /**
     * return the tcpa
     */
    public String getTcpA() {
        return tcpa;
    }

    /**
     * @param tcp
     *            the tcp to set
     */
    public void setTcp(String tcp) {
        this.tcp = tcp;
    }

    /**
     * @param tcp
     *            the tcp to set
     */
    public void setTcpA(String tcpa) {
        this.tcpa = tcpa;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary
     *            the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the warnings
     */
    public String getWarnings() {
        return warnings;
    }

    /**
     * @param warnings
     *            the warnings to set
     */
    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    /**
     * @return the hazards
     */
    public String getHazards() {
        return hazards;
    }

    /**
     * @param hazards
     *            the hazards to set
     */
    public void setHazards(String hazards) {
        this.hazards = hazards;
    }

    /**
     * @return the tcm
     */
    public String getTcm() {
        return tcm;
    }

    /**
     * @return the tca
     */
    public String getTca() {
        return tca;
    }

    /**
     * @return the tcd
     */
    public String getTcd() {
        return tcd;
    }

    /**
     * @param tcd
     *            the tcd to set
     */
    public void setTcd(String tcd) {
        this.tcd = tcd;
    }

}