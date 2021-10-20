/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfCustomColors;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseBDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictMergingRecordSet;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictRecordPair;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictSandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.EDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisBDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisEDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisState;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Sandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.StormState;
import gov.noaa.nws.ocp.common.dataplugin.atcf.StormTable;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckSandboxMergeableRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckinDeckRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckoutADeckDtgRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckoutDeckRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetDeckRecordsRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetForecastTrackRecordsRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetGenesisDeckRecordsRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetGenesisRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetSandboxesRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GetStormsRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.MergeEditingSandboxWithBaselineRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewForecastTrackRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.SaveDeckRecordEditRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.SaveEditDeckRecordSandboxRequest;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResourceData;

/**
 * Utilities to retrieve ATCF deck data from ATCF database.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 28, 2018 51961      jwu         Initial creation.
 * Jul 18, 2018 52658      jwu         Retrieve F_DEck data
 * Jul 27, 2018 52657      wpaintsil   Allow for b-deck data retrieval using
 *                                     the date/time group string as a parameter.
 * Aug 21, 2018 53949      jwu         Retrieve A-Deck DTG & technique.
 * Aug 31, 2018 53950      jwu         Implement checkin/checkout for B Deck.
 * Oct 02, 2018 55733      jwu         Tune checkin/checkout for B Deck.
 * Feb 19, 2019 60097      jwu         Save sandboxID for each product.
 * Feb 26, 2019 60613      jwu         Update getBdeckRecords to return a map.
 * Mar 29, 2019 61590      dfriedman   Use new request types.
 * May 07, 2019 63005      jwu         Implement checkout/checkin for A Deck.
 * May 28, 2019 63377      jwu         Revised data retrieval for B Deck.
 * Jun 06, 2019 63375      jwu         Implement checkout/checkin for F Deck.
 * Jun 20, 2019 65012      jwu         Update checkin/checkout logic.
 * Jul 15, 2019 65307      jwu         Add method for enter/edit ADeck.
 * Aug 05, 2019 66888      jwu         Add recordType for updateDeckRecord().
 * Aug 23, 2019 65564      jwu         Track user id in ATCF session.
 * Oct 01, 2019 68738      dmanzella   Add updateFDeckRecord method
 * Oct 29, 2019 69592      jwu         Add methods to retrieve forecast track.
 * Nov 13, 2019 71089      jwu         Retrieve E-Deck.
 * Jan 06, 2020 71722      jwu         Add methods for forecast track.
 * Mar 02, 2020 71724      wpaintsil   Add methods for text product formatting.
 * Mar 26, 2019 75391      jwu         Add roundToNearest().
 * Jun 11, 2010 68118      wpaintsil   Method for merging sandboxes.
 * Jun 12, 2020 78027      jwu         Update retrieval for forecast track.
 * Aug 10, 2020 79571      wpaintsil   Add getStorm() and other methods
 *                                     for Genesis table retrieval.
 * Oct 22, 2020 82721      jwu         Fix bug in forecast track.
 * Nov 02, 2020 82623      jwu         Add methods to assist storm management.
 * Nov 09, 2020 84705      jwu         Synchronize storm list.
 * Nov 22, 2020 85623      jwu         Fix error in getValidStormNumber().
 * Dec 10, 2020 85849      jwu         Use existing valid sandbox in getBDeckRecords().
 * Dec 17, 2020 86027      jwu         Update for updateGenesisList().
 * Mar 04, 2021 88229      mporricelli Added getObjAidTechniques() for storm and dtg;
 *                                     added getDateTimeGroupsBDeck()
 * Apr 19, 2021 88712      jwu         Add fcstToADeckRecord.
 * May 17, 2021 91567      jwu         Support minutes in B-Deck DTG.
 * Jun 10, 2021 91551      jnengel     Moved synoptic time methods here.
 * Jul 16, 2021 93152      jwu         Retrieve forecast track for latest best track DTG.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class AtcfDataUtil {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfDataUtil.class);

    // Column names used for ATCF DB queries.
    private static final String BASIN = "basin";

    private static final String YEAR = "year";

    private static final String CYCLONE_NUM = "cycloneNum";

    private static final String TECHNIQUE = "technique";

    private static final String RAD_WIND = "radWind";

    private static final String GENESIS_NUM = "genesisNum";

    // List of storms in storm.table - from year 1851 to present.
    private static Map<String, Storm> stormTable = StormTable.getStormTable();

    // List of all storms in storm.table.
    private static List<Storm> fullStormList;

    // List of storms in ATCF DB storm table.
    private static List<Storm> stormList;

    private static List<Genesis> genesisList;

    private static final DateTimeFormatter DTG_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHH");

    private static final DateTimeFormatter DTG_FORMAT_LONG = DateTimeFormatter
            .ofPattern("yyyyMMddHHmm");

    private AtcfDataUtil() {

    }

    /**
     * @return the stormTable
     */
    public static Map<String, Storm> getStormTable() {
        return stormTable;
    }

    /**
     * Retrieve the storm list from storms table.
     *
     * @return List<Storm>
     */
    @SuppressWarnings("unchecked")
    public static synchronized List<Storm> getStormList() {

        if (stormList == null) {
            stormList = new ArrayList<>();
            GetStormsRequest getStormReq = new GetStormsRequest();

            try {
                stormList = (List<Storm>) ThriftClient.sendRequest(getStormReq);
            } catch (Exception e) {
                logger.error("AtcfDataUtil - No storm list retrieved .....", e);
            }

            // Storms are sorted by basin, year (descending), cyclone
            // number
            if (!stormList.isEmpty()) {
                Collections.sort(stormList,
                        Comparator.comparing(Storm::getRegion)
                                .thenComparing(Comparator
                                        .comparing(Storm::getYear).reversed())
                                .thenComparing(Storm::getCycloneNum));
            }
        }

        return stormList;

    }

    /**
     * Storms that have been ended but not archived.
     *
     * @return List<Storm>
     */
    public static List<Storm> getRestartableStorms() {
        List<Storm> storms = getStormList();
        List<Storm> restartList = new ArrayList<>();

        for (int ii = 0; ii < storms.size(); ii++) {
            Storm storm = storms.get(ii);
            if (storm.getEndDTG() != null && !storm.getStormState()
                    .equalsIgnoreCase(StormState.ARCHIVE.name())) {
                restartList.add(storm);
            }
        }
        return restartList;
    }

    /**
     * Storms that has NOT been ended.
     *
     * @return List<Storm>
     */
    public static List<Storm> getActiveStorms() {
        List<Storm> storms = getStormList();
        List<Storm> activeList = new ArrayList<>();
        for (int ii = 0; ii < storms.size(); ii++) {
            Storm storm = storms.get(ii);
            if (storm.getEndDTG() == null) {
                activeList.add(storm);
            }
        }
        return activeList;
    }

    /**
     * Storms that has NOT been ended AND archived.
     *
     * @return List<Storm>
     */
    public static List<Storm> getDeletableStorms() {
        List<Storm> storms = getStormList();
        List<Storm> deleteList = new ArrayList<>();
        for (int ii = 0; ii < storms.size(); ii++) {
            Storm storm = storms.get(ii);
            if (storm.getEndDTG() == null && !storm.getStormState()
                    .equalsIgnoreCase(StormState.ARCHIVE.name())) {
                deleteList.add(storm);
            }
        }

        return deleteList;
    }

    /**
     * Storms that has NOT been archived.
     *
     * @return List<Storm>
     */
    public static List<Storm> getUpdatableStorms() {
        List<Storm> storms = getStormList();
        List<Storm> updateList = new ArrayList<>();
        for (int ii = 0; ii < storms.size(); ii++) {
            Storm storm = storms.get(ii);
            if (!storm.getStormState()
                    .equalsIgnoreCase(StormState.ARCHIVE.name())) {
                updateList.add(storm);
            }
        }

        return updateList;
    }

    /**
     * Retrieve the genesis list from genesis table.
     *
     * @return List<Storm>
     */
    @SuppressWarnings("unchecked")
    public static synchronized List<Genesis> getGenesisList() {
        if (genesisList == null) {

            genesisList = new ArrayList<>();
            GetGenesisRequest getGenesisReq = new GetGenesisRequest();

            try {
                genesisList = (List<Genesis>) ThriftClient
                        .sendRequest(getGenesisReq);
            } catch (Exception e) {
                logger.error("AtcfDataUtil - No genesis list retrieved .....",
                        e);
            }

            // Geneses are sorted by basin, year (descending), cyclone number
            if (!genesisList.isEmpty()) {
                Collections.sort(genesisList,
                        Comparator.comparing(Genesis::getRegion)
                                .thenComparing(Comparator
                                        .comparing(Genesis::getYear).reversed())
                                .thenComparing(Genesis::getGenesisNum));
            }
        }

        return genesisList;
    }

    /**
     * Get a map of all geneses with their basins as keys.
     *
     * @return Map<String, List<Genesis>>
     */
    public static Map<String, List<Genesis>> getGenesisBasinMap() {
        Map<String, List<Genesis>> genesisBasinMap = new HashMap<>();

        for (Genesis gis : getGenesisList()) {

            List<Genesis> gisList = genesisBasinMap.containsKey(gis.getRegion())
                    ? genesisBasinMap.get(gis.getRegion()) : new ArrayList<>();

            gisList.add(gis);

            genesisBasinMap.put(gis.getRegion(), gisList);

        }

        // Ensure geneses are sorted by genesis number.
        for (List<Genesis> gisList : genesisBasinMap.values()) {
            Collections.sort(gisList,
                    Comparator.comparing(Genesis::getGenesisNum));
        }
        return genesisBasinMap;
    }

    /**
     * Get a list of active geneses that can be ended.
     *
     * @return List<Genesis>
     */
    public static List<Genesis> getActiveGenesis() {
        List<Genesis> geneses = getGenesisList();
        List<Genesis> activeList = new ArrayList<>();
        for (int ii = 0; ii < geneses.size(); ii++) {
            Genesis genesis = geneses.get(ii);
            if (genesis.getEndDTG() == null
                    && !genesis.getGenesisId().isEmpty()) {
                activeList.add(genesis);
            }
        }
        return activeList;
    }

    /**
     * Get a list of ended geneses that have not become storms (TC).
     *
     * @return List<Genesis>
     */
    public static List<Genesis> getRestartableGenesis() {
        List<Genesis> geneses = getGenesisList();
        List<Genesis> restartList = new ArrayList<>();
        for (Genesis gen : geneses) {
            if (gen.getEndDTG() != null
                    && gen.getGenesisState()
                            .equalsIgnoreCase(GenesisState.END.toString())
                    && !gen.getGenesisId().isEmpty()) {
                restartList.add(gen);
            }
        }
        return restartList;
    }

    /**
     * Retrieve the full list of storms from ATCF storms table plus list of
     * genesis from genesis table.
     *
     * @return List<Storm>
     */
    public static synchronized List<Storm> getFullStormList() {

        if (fullStormList == null) {
            fullStormList = new ArrayList<>();

            for (Genesis gis : getGenesisList()) {
                Storm stm = gis.toStorm();
                fullStormList.add(stm);
            }

            fullStormList.addAll(getStormList());

        }

        return fullStormList;
    }

    /**
     * Retrieve B-Deck data for a given storm.
     *
     * @param storm
     *            storm to retrieve data from
     *
     * @return List<BDeckRecord>
     */
    public static List<BDeckRecord> getBDeckRecord(Storm storm) {

        List<BDeckRecord> bDeckRec = new ArrayList<>();

        GetDeckRecordsRequest getStormReq = new GetDeckRecordsRequest(
                AtcfDeckType.B);
        getStormReq.addOneQueryCondition(BASIN, storm.getRegion());
        getStormReq.addOneQueryCondition(YEAR, storm.getYear());
        getStormReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        try {
            bDeckRec = requestDeckRecords(getStormReq, BDeckRecord.class);
        } catch (Exception e) {
            logger.error(formatRetrievalError("B-deck records", storm), e);
        }

        // Sort records by warning time
        if (!bDeckRec.isEmpty()) {
            Collections.sort(bDeckRec,
                    Comparator.comparing(BDeckRecord::getRefTime));
        }

        return bDeckRec;
    }

    /**
     * Retrieve B-Deck data for a given genesis.
     *
     * @param genesis
     *            genesis to retrieve data from
     *
     * @return Map<String, List<BDeckRecord>>
     */
    public static Map<String, List<BDeckRecord>> getGenesisBDeckRecordMap(
            Genesis genesis) {

        Map<String, List<BDeckRecord>> bdeckDataMap = new HashMap<>();
        List<BDeckRecord> bdeckData = getGenesisBDeckRecords(genesis);

        if (bdeckData != null && !bdeckData.isEmpty()) {
            // Sort records into a DTG map
            bdeckDataMap = sortBDeckInDTGs(bdeckData);
        }

        return bdeckDataMap;
    }

    /**
     * Retrieve B-Deck data for a given genesis.
     *
     * @param genesis
     *            genesis to retrieve data from
     *
     * @return List<BDeckRecord>
     */
    @SuppressWarnings("unchecked")
    public static List<BDeckRecord> getGenesisBDeckRecords(Genesis genesis) {

        List<BDeckRecord> bdeckData = null;
        List<GenesisBDeckRecord> genesisBdeckData = new ArrayList<>();

        GetGenesisDeckRecordsRequest getGenesisBDeckReq = new GetGenesisDeckRecordsRequest();
        getGenesisBDeckReq.setDeckType("B");
        getGenesisBDeckReq.addOneQueryCondition(BASIN, genesis.getRegion());
        getGenesisBDeckReq.addOneQueryCondition(YEAR, genesis.getYear());
        getGenesisBDeckReq.addOneQueryCondition(GENESIS_NUM,
                genesis.getGenesisNum());

        try {
            genesisBdeckData = (List<GenesisBDeckRecord>) ThriftClient
                    .sendRequest(getGenesisBDeckReq);
            bdeckData = new ArrayList<>();
            for (GenesisBDeckRecord genesisBRec : genesisBdeckData) {
                bdeckData.add(genesisBRec.toBDeckRecord());
            }

        } catch (Exception e) {
            String msg = "AtcfDataUtil - No B-Deck record retrieved for genesis "
                    + genesis.getGenesisId();

            logger.error(msg, e);
        }

        // Warn the user here or let the caller do it?
        if (bdeckData == null || bdeckData.isEmpty()) {
            MessageDialog noBDeckFound = new MessageDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    "No B-Deck Record Found", null,
                    "No B-Deck record found for genesis "
                            + genesis.getGenesisId(),
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);
            noBDeckFound.open();
        } else {
            // Sort records by data time
            Collections.sort(bdeckData,
                    Comparator.comparing(BDeckRecord::getForecastDateTime));
        }

        return bdeckData;
    }

    /**
     * Retrieve E-Deck data, mapped by reftime, for a given genesis.
     *
     * @param genesis
     *            genesis to retrieve data from
     *
     * @return Map<String, List<GenesisEDeckRecord>>
     */
    public static Map<String, List<GenesisEDeckRecord>> getGenesisEDeckRecordMap(
            Genesis genesis) {

        Map<String, List<GenesisEDeckRecord>> edeckDataMap = new LinkedHashMap<>();
        List<GenesisEDeckRecord> edeckData = getGenesisEDeckRecords(genesis);

        if (!edeckData.isEmpty()) {
            for (GenesisEDeckRecord rec : edeckData) {

                Calendar dataTime = rec.getRefTimeAsCalendar();

                String dtg = calendarToDateTimeString(dataTime);

                List<GenesisEDeckRecord> dtgRecords = edeckDataMap
                        .computeIfAbsent(dtg, k -> new ArrayList<>());

                dtgRecords.add(rec);
            }
        }

        return edeckDataMap;
    }

    /**
     * Retrieve E-Deck data for a given genesis.
     *
     * @param genesis
     *            genesis to retrieve data for
     *
     * @return List<EDeckRecord>
     */
    @SuppressWarnings("unchecked")
    public static List<GenesisEDeckRecord> getGenesisEDeckRecords(
            Genesis genesis) {

        List<GenesisEDeckRecord> genesisEdeckData = new ArrayList<>();

        GetGenesisDeckRecordsRequest getGenesisEDeckReq = new GetGenesisDeckRecordsRequest();
        getGenesisEDeckReq.setDeckType("E");
        getGenesisEDeckReq.addOneQueryCondition(BASIN, genesis.getRegion());
        getGenesisEDeckReq.addOneQueryCondition(YEAR, genesis.getYear());
        getGenesisEDeckReq.addOneQueryCondition(GENESIS_NUM,
                genesis.getGenesisNum());
        getGenesisEDeckReq.addOneQueryCondition(TECHNIQUE, "OFCL");
        getGenesisEDeckReq.addOneQueryCondition("probFormat", "GN");

        try {
            genesisEdeckData = (List<GenesisEDeckRecord>) ThriftClient
                    .sendRequest(getGenesisEDeckReq);
        } catch (Exception e) {
            logger.error(
                    "AtcfDataUtil.getGenesisEDeckRecords - Failed to retrieve genesisedeck: ",
                    e);
        }

        return genesisEdeckData;
    }

    /**
     * Retrieve A-Deck data for a given storm..
     *
     * @param storm
     *            storm to retrieve data from
     *
     * @return List<ADeckRecord>
     */
    public static List<ADeckRecord> getADeckRecords(Storm storm) {

        List<ADeckRecord> adeckRecs = new ArrayList<>();

        GetDeckRecordsRequest getAdeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.A);
        getAdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getAdeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getAdeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        try {
            adeckRecs = requestDeckRecords(getAdeckReq, ADeckRecord.class);
        } catch (Exception e) {
            logger.error(formatRetrievalError("A-deck records", storm), e);
        }

        return adeckRecs;
    }

    /**
     * Retrieve baseline A-Deck data for a given storm with specific "tech" and
     * "radii".
     *
     * @param storm
     *            storm to retrieve data from
     * @param tech
     *            objective aid technique
     * @param radii
     *            wind radii (34,50,64)
     *
     * @return List<ADeckRecord>
     */
    public static List<ADeckRecord> getADeckRecords(Storm storm, String tech,
            int radii) {

        List<ADeckRecord> adeckRecs = new ArrayList<>();
        GetDeckRecordsRequest getAdeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.A);
        getAdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getAdeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getAdeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());
        getAdeckReq.addOneQueryCondition(TECHNIQUE, tech);
        getAdeckReq.addOneQueryCondition(RAD_WIND, (float) radii);
        getAdeckReq.setSandboxId(-1);

        try {
            adeckRecs = requestDeckRecords(getAdeckReq, ADeckRecord.class);
        } catch (Exception e) {
            String msg = formatRetrievalError("A-deck records", storm,
                    String.format("for tech %s and radii %s kt", tech, radii));
            logger.error(msg, e);
        }

        return adeckRecs;
    }

    /**
     * Retrieve baseline F-Deck data for a give storm.
     *
     * @param storm
     *            storm to retrieve data from
     *
     * @return List<FDeckRecord>
     */
    public static List<FDeckRecord> getFDeckBaselineRecords(Storm storm) {

        List<FDeckRecord> fdecRecs = new ArrayList<>();

        GetDeckRecordsRequest getFdeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.F);
        getFdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getFdeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getFdeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        try {
            fdecRecs = requestDeckRecords(getFdeckReq, FDeckRecord.class);
        } catch (Exception e) {
            logger.error(formatRetrievalError("F-deck records", storm), e);
        }

        return fdecRecs;
    }

    /**
     * Retrieve F-Deck data for a give storm to display.
     *
     * @param storm
     *            storm to retrieve data from
     *
     * @return List<FDeckRecord>
     */
    public static List<FDeckRecord> getFDeckRecords(Storm storm) {
        return getFDeckRecords(storm, false);
    }

    /**
     * Get the list of date/time groups from the ADeckRecords.
     *
     * @param pdos
     * @return
     */
    public static SortedSet<String> getADeckDateTimeGroups(
            List<ADeckRecord> pdos) {
        // Determine all date/time groups (DTGs) appearing in retrieved records
        SortedSet<String> timeSet = new TreeSet<>();

        for (ADeckRecord rec : pdos) {
            Calendar fixTime = rec.getRefTimeAsCalendar();
            int hour = fixTime.get(Calendar.HOUR_OF_DAY);
            // synoptic hours only
            if (hour % 6 == 0) {
                String fixTimeStr = calendarToDateTimeString(fixTime);
                timeSet.add(fixTimeStr);
            }
        }

        return timeSet;
    }

    /**
     * Get the list of date/time groups from the BDeckRecords.
     *
     * @param pdos
     * @return
     */
    public static SortedSet<String> getBDeckDateTimeGroups(
            List<BDeckRecord> pdos) {
        // Determine all date/time groups (DTGs) appearing in retrieved records
        SortedSet<String> timeSet = new TreeSet<>();

        for (BDeckRecord rec : pdos) {
            Calendar fixTime = rec.getRefTimeAsCalendar();
            int hour = fixTime.get(Calendar.HOUR_OF_DAY);
            // synoptic hours only
            if (hour % 6 == 0) {
                String fixTimeStr = calendarToDateTimeString(fixTime);
                timeSet.add(fixTimeStr);
            }
        }

        return timeSet;
    }

    /**
     * Get the list of date/time groups from the FDeckRecords.
     *
     * @param pdos
     * @return
     */
    public static SortedSet<String> getFDeckDateTimeGroups(
            List<FDeckRecord> pdos) {

        // Determine all date/time groups (DTGs) appearing in retrieved records
        SortedSet<String> timeSet = new TreeSet<>();

        for (FDeckRecord rec : pdos) {
            Calendar fixTime = rec.getRefTimeAsCalendar();
            if (fixTime != null) {
                int hour = fixTime.get(Calendar.HOUR_OF_DAY);
                // synoptic hours only
                if (hour % 6 == 0) {
                    String fixTimeStr = calendarToDateTimeString(fixTime);
                    timeSet.add(fixTimeStr);
                }
            }
        }

        return timeSet;
    }

    /**
     * Convert a Calendar object to the date/time group (DTG) formatted string
     * (yyyyMMddHH)
     *
     * @param time
     * @return
     */
    public static String calendarToDateTimeString(Calendar time) {
        String timeString = null;

        int year = time.get(Calendar.YEAR);
        int month = time.get(Calendar.MONTH);
        int day = time.get(Calendar.DAY_OF_MONTH);
        int hour = time.get(Calendar.HOUR_OF_DAY);
        timeString = String.format("%04d", year)
                + String.format("%02d", month + 1) + String.format("%02d", day)
                + String.format("%02d", hour);
        return timeString;
    }

    /**
     * Convert a Calendar object to the date/time group (DTG) formatted string
     * (yyyyMMddHHMM) - used for F-Deck
     *
     * @param time
     * @return
     */
    public static String calendarToLongDateTimeString(Calendar time) {
        String timeString = null;
        int minutes = time.get(Calendar.MINUTE);
        timeString = calendarToDateTimeString(time)
                + String.format("%02d", minutes);
        return timeString;
    }

    /**
     * Query the B-deck records by storm and warnTime;
     *
     * @param timeString
     * @param storm
     * @return
     */
    public static List<BDeckRecord> queryBDeckByDateString(String timeString,
            Storm storm) {

        List<BDeckRecord> rec = new ArrayList<>();

        GetDeckRecordsRequest getStormReq = new GetDeckRecordsRequest(
                AtcfDeckType.B);
        getStormReq.addOneQueryCondition(BASIN, storm.getRegion());
        getStormReq.addOneQueryCondition(YEAR, storm.getYear());
        getStormReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        Date date;

        try {
            date = parseDate(timeString, DTG_FORMAT);
        } catch (DateTimeException e) {
            logger.error(
                    "AtcfDataUtil.queryBDeckByDate(): Invalid date/time string (yyyyMMddHH): "
                            + timeString,
                    e);
            return rec;
        }

        getStormReq.addOneQueryCondition(AbstractAtcfRecord.REFTIME_ID, date);

        try {
            rec = requestDeckRecords(getStormReq, BDeckRecord.class);
        } catch (Exception e) {
            logger.error(formatRetrievalError("B-deck records", storm), e);
        }

        // Sort records by warning time
        if (!rec.isEmpty()) {
            Collections.sort(rec,
                    Comparator.comparing(BDeckRecord::getRefTime));
        }

        return rec;
    }

    /**
     * Get the list of date/time group from the baseline adeck table of a storm.
     *
     * @param storm
     *            Storm
     * @return List of date/time group in format of (yyyyMMddHH)
     */
    public static List<String> getDateTimeGroups(Storm storm) {

        List<String> timeSet = new ArrayList<>();

        String dtg = "SELECT DISTINCT reftime FROM atcf.adeck "
                + "WHERE basin = '" + storm.getRegion() + "' AND year = "
                + storm.getYear() + " AND cycloneNum = " + storm.getCycloneNum()
                + " ORDER BY reftime";

        try {
            List<Object[]> adtg = DirectDbQuery.executeQuery(dtg, "metadata",
                    DirectDbQuery.QueryLanguage.SQL);
            timeSet = timeStampsToDTGs(adtg);
        } catch (Exception e) {
            logger.error(formatRetrievalError("DTGs", storm), e);
        }

        return timeSet;
    }

    /**
     * Get the list of date/time group from the baseline bdeck table of a storm.
     *
     * @param storm
     *            Storm
     * @return List of date/time group in format of (yyyyMMddHH)
     */
    public static List<String> getDateTimeGroupsBDeck(Storm storm) {

        List<String> timeSet = new ArrayList<>();

        String dtg = "SELECT DISTINCT reftime FROM atcf.bdeck "
                + "WHERE basin = '" + storm.getRegion() + "' AND year = "
                + storm.getYear() + " AND cycloneNum = " + storm.getCycloneNum()
                + " ORDER BY reftime";

        try {
            List<Object[]> adtg = DirectDbQuery.executeQuery(dtg, "metadata",
                    DirectDbQuery.QueryLanguage.SQL);
            timeSet = timeStampsToDTGs(adtg);
        } catch (Exception e) {
            logger.error(formatRetrievalError("B-deck DTGs", storm), e);
        }

        return timeSet;
    }

    /**
     * Get the list of objective techniques from the ADeckRecords for a storm.
     *
     * @param storm
     *            Storm
     * @return List of objective aid technique names
     */
    public static Set<String> getObjAidTechniques(Storm storm) {

        Set<String> techs = new TreeSet<>();

        String dtg = "SELECT DISTINCT technique FROM atcf.adeck "
                + "WHERE basin = '" + storm.getRegion() + "' AND year = "
                + storm.getYear() + " AND cycloneNum = " + storm.getCycloneNum()
                + " ORDER BY technique";

        try {
            List<Object[]> adtg = DirectDbQuery.executeQuery(dtg, "metadata",
                    DirectDbQuery.QueryLanguage.SQL);

            for (Object[] obj : adtg) {
                techs.add((String) obj[0]);
            }
        } catch (Exception e) {
            logger.error(
                    formatRetrievalError("active obj aid techniques", storm),
                    e);
        }

        return techs;
    }

    /**
     * Get the list of objective techniques from the ADeckRecords for a storm at
     * a specific dtg.
     *
     * @param storm
     *            Storm
     * @param dtg
     *            selected dtg
     * @return List of objective aid technique names
     */
    public static Set<String> getObjAidTechniques(Storm storm, String dtg) {

        Date date;
        Set<String> techs = new TreeSet<>();

        try {
            date = parseDate(dtg, DTG_FORMAT);
        } catch (DateTimeParseException e) {
            logger.error(
                    "AtcfDataUtil.getObjAidTechniques(): Invalid date/time string (yyyyMMddHH): "
                            + dtg,
                    e);
            return techs;
        }
        String queryString = "SELECT DISTINCT technique FROM atcf.adeck "
                + "WHERE basin = '" + storm.getRegion() + "' AND cycloneNum = "
                + storm.getCycloneNum() + " AND refTime = '" + date
                + "' ORDER BY technique";

        try {
            List<Object[]> adtg = DirectDbQuery.executeQuery(queryString,
                    "metadata", DirectDbQuery.QueryLanguage.SQL);

            for (Object[] obj : adtg) {
                techs.add((String) obj[0]);
            }
        } catch (Exception e) {
            logger.error(formatRetrievalError("active obj aid techniques",
                    storm, String.format("and dtg %s", dtg)), e);
        }

        return techs;
    }

    private static List<String> timeStampsToDTGs(List<Object[]> rows) {
        return rows.stream().map(r -> toDateTimeString((Timestamp) r[0]))
                .collect(Collectors.toList());
    }

    /**
     * Convert a java.sql.Timestamp object to the date/time group (DTG)
     * formatted string (yyyyMMddHH)
     *
     * @param ts
     * @return
     */
    private static String toDateTimeString(Timestamp ts) {
        return calendarToDateTimeString(TimeUtil.newGmtCalendar(ts));
    }

    /**
     * Query the A-deck records by storm and date-time group.
     *
     * @param storm
     *            Storm
     * @param dtg
     *            a date-time group (yyyyMMddHH)
     * @param sandboxID
     *            ID of sandbox; <=0 - retrieve from baseline adeck; > 0 -
     *            retrieve from this sandbox.
     * @param useBaseline
     *            true: when sandboxID > 0 but cannot find data, check baseline;
     *            false: when sandboxID > 0 but cannot find data, do not check
     *            baseline;
     * @return List<ADeckRecord>
     */
    public static List<ADeckRecord> queryADeckByDTG(Storm storm, String dtg,
            int sandboxID, boolean useBaseline) {

        List<ADeckRecord> emptyResult = new ArrayList<>();

        GetDeckRecordsRequest getADeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.A);
        getADeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getADeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getADeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        if (sandboxID > 0) {
            getADeckReq.setSandboxId(sandboxID);
        }

        Date date;

        try {
            date = parseDate(dtg, DTG_FORMAT);
        } catch (DateTimeException e) {
            logger.error(
                    "AtcfDataUtil.queryADeckByDTG(): Invalid date/time string (yyyyMMddHH): "
                            + dtg,
                    e);
            return emptyResult;
        }

        getADeckReq.addOneQueryCondition(AbstractAtcfRecord.REFTIME_ID, date);

        boolean success = false;
        int nAttempts = useBaseline && sandboxID > 0 ? 2 : 1;

        for (int attempt = 1; attempt <= nAttempts; ++attempt) {
            try {
                success = false;
                List<ADeckRecord> records = requestDeckRecords(getADeckReq,
                        ADeckRecord.class);
                success = true;
                if (records != null && !records.isEmpty()) {
                    return records;
                }
            } catch (Exception e) {
                logger.handle(
                        attempt < nAttempts ? Priority.WARN : Priority.ERROR,
                        formatRetrievalError("A-deck records", storm,
                                String.format("and DTG %s", dtg)),
                        e);
            }

            /*
             * If a sandbox was specified and no records were found in the
             * sandbox and useBaseline is true, try the baseline table.
             */
            getADeckReq.setSandboxId(-1);
        }

        if (useBaseline && success) {
            logger.warn(formatRetrievalError("A-deck records", storm,
                    String.format("and DTG %s: empty result", dtg)));
        }

        return emptyResult;
    }

    /**
     * Get a new DTG (yyyyMMddHH) from a given DTG with time lags.
     *
     * @param dtg
     *            Date time group
     * @param hour
     *            hours negative = before; positive = after
     * @return DTG string
     */
    public static String getNewDTG(String dtg, int hour) {

        String timeString = null;

        if (hour == 0) {
            return dtg;
        }

        try {
            timeString = LocalDateTime.parse(dtg, DTG_FORMAT).plusHours(hour)
                    .atZone(ZoneOffset.UTC).format(DTG_FORMAT);
        } catch (DateTimeParseException e) {
            logger.error(
                    "AtcfDataUtil.getNewDTG(): Invalid date/time string (yyyyMMddHH): "
                            + dtg,
                    e);
        }

        return timeString;
    }

    /**
     * Get a new long DTG (yyyyMMddHHmm) from a given DTG (yyyyMMddHH) with time
     * lags.
     *
     * @param dtg
     *            Date time group (no minutes)
     * @param minutes
     *            minutes negative = before; positive = after
     * @return DTG string (YYYYMMDDHH or YYYYMMDDHHmm) or null if error
     */
    public static String getNewDTGWithMinutes(String dtg, int minutes) {
        if (minutes == 0) {
            return dtg;
        }

        try {
            return LocalDateTime.parse(dtg, DTG_FORMAT).plusMinutes(minutes)
                    .atZone(ZoneOffset.UTC).format(DTG_FORMAT_LONG);
        } catch (DateTimeParseException e) {
            logger.error(
                    "AtcfDataUtil.getNewDTGWithMinutes(): Invalid date/time string (yyyyMMddHH): "
                            + dtg,
                    e);
        }

        return null;
    }

    /**
     * Get the closest synoptic time (Midnight, Noon, 6:00, or 18:00) to the
     * current UTC time.
     *
     * @return A string in yyyyMMddHH format representing a time.
     */
    public static String getClosestSynopticTime() {
        ZonedDateTime time = getClosestSynopticZDT();
        return time.format(DTG_FORMAT);
    }

    /**
     * Get the closest synoptic time (Midnight, Noon, 6:00, or 18:00) to the
     * current UTC time.
     *
     * @return A ZoneDateTime object representing a time.
     */
    public static ZonedDateTime getClosestSynopticZDT() {
        ZonedDateTime time = ZonedDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.HOURS);
        int hour = time.getHour();
        int synoptic;

        if (hour < 3) {
            synoptic = 0;
        } else if (hour < 9) {
            synoptic = 6;
        } else if (hour < 15) {
            synoptic = 12;
        } else if (hour < 21) {
            synoptic = 18;
        } else {
            synoptic = 0;
            time = time.plusDays(1);
        }

        return time.withHour(synoptic);
    }

    /**
     * Retrieve B-Deck data for a given storm for display (set edit to false).
     *
     * @param storm
     *            storm to retrieve data from
     * @return Map<String, List<BDeckRecord>>
     */
    public static Map<String, List<BDeckRecord>> getBDeckRecords(Storm storm) {
        return getBDeckRecords(storm, false);
    }

    /**
     * Retrieve B-Deck data for a given storm.
     *
     * First check if the data has been cached in AtcfProduct; if not, check if
     * data has been check into a sandbox; if not, check if the retrieval is for
     * edit, if yes, check out data into sandbox and return the data; if the
     * retrieval is for display, retrieve data from baseline tables.
     *
     * @param storm
     *            storm to retrieve data from
     * @param edit
     *            true - get data for editing; false - get data for display.
     *
     * @return Map<String, List<BDeckRecord>>
     */
    public static Map<String, List<BDeckRecord>> getBDeckRecords(Storm storm,
            boolean edit) {

        Map<String, List<BDeckRecord>> bdeckDataMap = null;
        List<BDeckRecord> bdeckData = null;

        // Check if B-Deck data has been cached before.
        AtcfResourceData rscData = AtcfSession.getInstance().getAtcfResource()
                .getResourceData();
        AtcfProduct atcfPrd = rscData.getAtcfProduct(storm);

        int sandboxID = atcfPrd.getBdeckSandboxID();

        bdeckDataMap = atcfPrd.getBDeckDataMap();

        // Return existing data from AtcfProduct.
        if (((sandboxID > 0) || !edit) && bdeckDataMap != null
                && !bdeckDataMap.isEmpty()) {
            return bdeckDataMap;
        }

        // Check if sandbox exists; if not, check out one if "edit".
        if (sandboxID <= 0) {
            sandboxID = getBDeckSandbox(storm);

            /*
             * Try to retrieve from the sandbox. Return at success, otherwise
             * reset sandbox to -1 so we need to check out data to a new
             * sandbox.
             *
             * Note: ideally, we should either re-check into the existing
             * sandbox or cleanup the sandbox and its associated deck data
             * table?
             */
            if (sandboxID > 0) {
                GetDeckRecordsRequest getBdeckReq = new GetDeckRecordsRequest(
                        AtcfDeckType.B);
                getBdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
                getBdeckReq.addOneQueryCondition(YEAR, storm.getYear());
                getBdeckReq.addOneQueryCondition(CYCLONE_NUM,
                        storm.getCycloneNum());

                getBdeckReq.setSandboxId(sandboxID);

                try {
                    bdeckData = requestDeckRecords(getBdeckReq,
                            BDeckRecord.class);
                } catch (Exception e) {
                    sandboxID = -1;
                    logger.warn(
                            "AtcfDataUtil.getBDeckRecords(): IInvalid sandbox: ",
                            e);
                }

                if (bdeckData == null || bdeckData.isEmpty()) {
                    sandboxID = -1;
                }

            }

            // Checkout from baseline into sandbox, if "edit".
            if (sandboxID <= 0 && edit) {
                CheckoutDeckRequest checkoutBdeckReq = new CheckoutDeckRequest(
                        AtcfDeckType.B);
                checkoutBdeckReq.setBasin(storm.getRegion());
                checkoutBdeckReq.setYear(storm.getYear());
                checkoutBdeckReq.setCycloneNum(storm.getCycloneNum());
                checkoutBdeckReq.setStormName(storm.getStormName());

                String uid = getUID();
                checkoutBdeckReq.setUserId(uid);

                try {
                    sandboxID = (int) ThriftClient
                            .sendRequest(checkoutBdeckReq);
                    atcfPrd.setBdeckSandboxID(sandboxID);
                } catch (Exception e) {
                    logger.error(formatCheckoutError("B-deck data", storm), e);
                }
            }

            rscData.setBdeckSandbox(storm, sandboxID);

        }

        // Retrieve it from the new sandbox or baseline (sandboxID <= 0).
        if (bdeckData == null || bdeckData.isEmpty()) {

            GetDeckRecordsRequest getBdeckReq = new GetDeckRecordsRequest(
                    AtcfDeckType.B);
            getBdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
            getBdeckReq.addOneQueryCondition(YEAR, storm.getYear());
            getBdeckReq.addOneQueryCondition(CYCLONE_NUM,
                    storm.getCycloneNum());

            getBdeckReq.setSandboxId(sandboxID);

            try {
                bdeckData = requestDeckRecords(getBdeckReq, BDeckRecord.class);
            } catch (Exception e) {
                logger.error(formatRetrievalError("B-Deck records", storm,
                        sandboxID), e);
            }
        }

        // Warn the user here or let the caller do it?
        if (bdeckData == null || bdeckData.isEmpty()) {
            MessageDialog noBDeckFound = new MessageDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    "No B-Deck Record Found", null,
                    "No B-Deck record found for storm " + storm.getStormId(),
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);
            noBDeckFound.open();
        } else {
            // Sort records by data time
            Collections.sort(bdeckData,
                    Comparator.comparing(BDeckRecord::getForecastDateTime));

            // Sort records into a DTG map
            bdeckDataMap = sortBDeckInDTGs(bdeckData);
        }

        return bdeckDataMap;
    }

    /**
     * Retrieve B-Deck baseline data for a given storm.
     *
     * @param storm
     *            storm to retrieve data from
     *
     * @return Map<String, List<BDeckRecord>>
     */
    public static Map<String, List<BDeckRecord>> getBDeckBaseLineRecords(
            Storm storm) {

        Map<String, List<BDeckRecord>> bdeckDataMap = new LinkedHashMap<>();
        List<BDeckRecord> bdeckData = null;

        GetDeckRecordsRequest getBdeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.B);
        getBdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getBdeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getBdeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        getBdeckReq.setSandboxId(-1);

        try {
            bdeckData = requestDeckRecords(getBdeckReq, BDeckRecord.class);
        } catch (Exception e) {
            logger.error(formatRetrievalError("B-deck records", storm), e);
        }

        // Warn the user here or let the caller do it?
        if (bdeckData == null || bdeckData.isEmpty()) {
            MessageDialog noBDeckFound = new MessageDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    "No B-Deck Record Found", null,
                    "No B-Deck baseline record found for storm "
                            + storm.getStormId(),
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);
            noBDeckFound.open();
        } else {
            // Sort records by data time
            Collections.sort(bdeckData,
                    Comparator.comparing(BDeckRecord::getForecastDateTime));

            // Sort records into a DTG map
            bdeckDataMap = sortBDeckInDTGs(bdeckData);
        }

        return bdeckDataMap;
    }

    /**
     * Sort BDeckRecords into a map by date/time groups.
     *
     * @param bdeckRecords
     *            List of BDeckRecords
     * @return Map<String, List<BDeckRecord>>
     */
    public static Map<String, List<BDeckRecord>> sortBDeckInDTGs(
            List<BDeckRecord> bdeckRecords) {

        Map<String, List<BDeckRecord>> dataMap = new LinkedHashMap<>();

        for (BDeckRecord rec : bdeckRecords) {

            Calendar dataTime = rec.getRefTimeAsCalendar();
            int minutes = dataTime.get(Calendar.MINUTE);

            String dtg = calendarToDateTimeString(dataTime);
            if (minutes > 0) {
                dtg = calendarToLongDateTimeString(dataTime);
            }

            List<BDeckRecord> dtgRecords = dataMap.computeIfAbsent(dtg,
                    k -> new ArrayList<>());

            dtgRecords.add(rec);
        }

        return dataMap;
    }

    /**
     * Update a B-Deck record for a given storm.
     *
     * @param sandboxId
     *            Id for the sandbox
     * @param bdeckRecord
     *            BdeckRecord to be updated
     * @param editType
     *            Type for update (NEW, MODIFY, DELETE)
     *
     */
    public static void updateBDeckRecord(int sandboxId, BDeckRecord bdeckRecord,
            RecordEditType editType) {

        boolean success = updateDeckRecord(sandboxId, bdeckRecord, editType);

        // TODO may not need this - could be handled in the "Edit" GUI.
        if (success) {

            // Update cache if it is a new record or the record is deleted.
            AtcfResourceData rscData = AtcfSession.getInstance()
                    .getAtcfResource().getResourceData();

            Calendar dataTime = bdeckRecord.getRefTimeAsCalendar();
            String dtg = calendarToDateTimeString(dataTime);

            Map<String, List<BDeckRecord>> bdeckMap = rscData
                    .getActiveAtcfProduct().getBDeckDataMap();
            List<BDeckRecord> bdeckData = bdeckMap.computeIfAbsent(dtg,
                    k -> new ArrayList<>());

            if (RecordEditType.NEW == editType) {
                bdeckData.add(bdeckRecord);
            } else if (RecordEditType.DELETE == editType) {
                bdeckData.remove(bdeckRecord);
            } else if (RecordEditType.MODIFY == editType) {
                int rad = (int) bdeckRecord.getRadWind();
                for (BDeckRecord bdc : bdeckData) {
                    if ((int) bdc.getRadWind() == rad) {
                        bdeckData.remove(bdc);
                        break;
                    }
                }

                bdeckData.add(bdeckRecord);
            }
        }
    }

    /**
     * Check in deck records for a given storm.
     *
     * @param sandboxId
     *            Id for the sandbox
     * @param userId
     *            userId for the sandbox owner
     * @param deckType
     *            AtcfDeckType
     * @return List<ConflictSandbox>
     */
    @SuppressWarnings("unchecked")
    public static List<ConflictSandbox> checkinDeckRecords(int sandboxId,
            String uid, AtcfDeckType deckType) {

        List<ConflictSandbox> checkInSbx = new ArrayList<>();

        // Dummy checks.
        if (sandboxId <= 0) {
            logger.warn(
                    "AtcfDataUtil.checkinDeckRecords - cannot update record for invalid sandbox ID "
                            + sandboxId);
            return checkInSbx;
        }

        if (uid == null || uid.length() <= 0) {
            logger.warn(
                    "AtcfDataUtil - cannot update record for invalid user ID");
            return checkInSbx;
        }

        // Update the record in the sandbox.
        CheckinDeckRequest checkinDeckReq = new CheckinDeckRequest(deckType);
        checkinDeckReq.setSandboxid(sandboxId);
        checkinDeckReq.setUserId(uid);

        try {
            checkInSbx = (List<ConflictSandbox>) ThriftClient
                    .sendRequest(checkinDeckReq);
        } catch (Exception e) {
            logger.error("AtcfDataUtil - Fail to check in deck records from "
                    + deckType.getValue() + "Deck sandbox " + sandboxId
                    + " for user " + uid, e);
        }

        if (checkInSbx != null && !checkInSbx.isEmpty()) {
            /*
             * TODO Need to present the differences between user's sandbox and
             * baseline to let the user review and make decision. Also, other
             * ATCF sessions will need to handle the notification from this
             * CheckinDeckRequest as well.
             */
            logger.info("AtcfDataUtil - check in deck records from "
                    + deckType.getValue() + "Deck sandbox  " + sandboxId
                    + " for user " + uid
                    + ": this sandbox is no longer valid to be checked in. Update from baseline table first.");
        }

        return checkInSbx;

    }

    /**
     * Check in A-Deck records for a given storm for the current user..
     *
     * @param sandboxId
     *            Id for the sandbox
     * @return List<ConflictSandbox>
     */
    public static List<ConflictSandbox> checkinADeckRecords(int sandboxId) {
        String uid = getUID();
        return checkinDeckRecords(sandboxId, uid, AtcfDeckType.A);
    }

    /**
     * Check in B-Deck records for a given storm for the current user..
     *
     * @param sandboxId
     *            Id for the sandbox
     * @return List<ConflictSandbox>
     */
    public static List<ConflictSandbox> checkinBDeckRecords(int sandboxId) {
        String uid = getUID();
        return checkinDeckRecords(sandboxId, uid, AtcfDeckType.B);
    }

    /**
     * Check in F-Deck records for a given storm for the current user..
     *
     * @param sandboxId
     *            Id for the sandbox
     * @return List<ConflictSandbox>
     */
    public static List<ConflictSandbox> checkinFDeckRecords(int sandboxId) {
        String uid = getUID();
        return checkinDeckRecords(sandboxId, uid, AtcfDeckType.F);
    }

    /**
     * Converts an ATCF Custom color to a regular Color
     *
     * @param shell
     * @param color
     * @param entry
     * @return
     */
    public static Color atcfCustomColorToColor(Shell shell,
            AtcfCustomColors color, int entry) {
        java.awt.Color c = java.awt.Color
                .decode("#" + color.getColorByIndex(entry).toUpperCase());
        return new Color(shell.getDisplay(), c.getRed(), c.getGreen(),
                c.getBlue());
    }

    /**
     * Sends the given request to EDEX and {@code List} response.
     *
     * @param request
     * @param resultType
     * @return a List of the given record type
     * @throws VizException
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> requestDeckRecords(GetDeckRecordsRequest request,
            Class<T> resultType) throws VizException {
        return (List<T>) ThriftClient.sendRequest(request);
    }

    /**
     * Retrieve A-deck records by storm and date-time groups, from baseline,
     * sandbox, or AtcfProduct.
     *
     * @param storm
     *            Storm
     * @param dtgs
     *            List of date-time groups (yyyyMMddHH)
     * @param edit
     *            true - retrieve data for edit; false - retrieve data for
     *            display.
     * @return Map<String, List<ADeckRecord>>
     */
    public static Map<String, List<ADeckRecord>> retrieveADeckData(Storm storm,
            String[] dtgs, boolean edit) {

        Map<String, List<ADeckRecord>> records = new HashMap<>();

        // Retrieve data for each DTG.
        if (storm != null) {

            // Get the AtcfProduct associated with the storm
            AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                    .getResourceData().getAtcfProduct(storm);

            int sandboxID = prd.getAdeckSandboxID();

            // For display, first try to get from storm's AtcfProduct.
            List<String> dtgsNotInPrd = new ArrayList<>();
            if (!edit) {
                for (String dtg : dtgs) {
                    List<ADeckRecord> recOfDtg = prd.getADeckData(dtg);
                    if (recOfDtg != null && !recOfDtg.isEmpty()) {
                        records.put(dtg, recOfDtg);
                    } else {
                        dtgsNotInPrd.add(dtg);
                    }
                }
            } else {
                dtgsNotInPrd.addAll(Arrays.asList(dtgs));
            }

            if (dtgsNotInPrd.isEmpty()) {
                return records;
            }

            // Check if there is working sand box for the storm.
            if (sandboxID <= 0) {
                sandboxID = getADeckSandbox(storm);
                prd.setAdeckSandboxID(sandboxID);
            }

            // Try to get data from an existing sand box
            List<String> dtgsTobeFound = new ArrayList<>();
            if (sandboxID <= 0) {
                dtgsTobeFound.addAll(dtgsNotInPrd);
            } else {
                // Existing sandbox
                for (String dtg : dtgsNotInPrd) {
                    List<ADeckRecord> recOfDtg = AtcfDataUtil
                            .queryADeckByDTG(storm, dtg, sandboxID, false);

                    if (recOfDtg != null && !recOfDtg.isEmpty()) {
                        records.put(dtg, recOfDtg);
                    } else {
                        dtgsTobeFound.add(dtg);
                    }
                }
            }

            // Checkout from baseline table into sandbox if "edit".
            if (edit && !dtgsTobeFound.isEmpty()) {
                sandboxID = checkoutADeckByDTGs(storm, dtgsTobeFound,
                        sandboxID);
                prd.setAdeckSandboxID(sandboxID);
            }

            // Now get data from sandbox or baseline
            if (!dtgsTobeFound.isEmpty()) {
                if (!edit) {
                    sandboxID = -1;
                }

                for (String dtg : dtgsTobeFound) {
                    List<ADeckRecord> recOfDtg = AtcfDataUtil
                            .queryADeckByDTG(storm, dtg, sandboxID, true);
                    records.put(dtg, recOfDtg);
                }
            }

            /*
             * For "display" only, store into AtcfProduct; For "edit", the data
             * should be handled locally before they are committed to sandbox
             * and baseline.
             */
            if (!edit) {
                prd.getADeckDataMap().putAll(records);
            }
        }

        return records;
    }

    /**
     * Checkout a storm's A-Deck data to sandbox (sandbox & sandbox_adeck
     * tables).
     *
     * First check if a sandbox for the user and storm exists; if not check out
     * data into a sandbox and return the new sandbox ID.
     *
     * Checking out the entire A-Deck may cost a few seconds, so use
     * checkoutADeckByDTGS() instead.
     *
     * @param storm
     *            storm
     *
     * @return sandbox id
     */
    @SuppressWarnings("unchecked")
    public static int checkoutADeck(Storm storm) {

        // Check if A-Deck data has been cached before.
        AtcfResourceData rscData = AtcfSession.getInstance().getAtcfResource()
                .getResourceData();
        AtcfProduct atcfPrd = rscData.getAtcfProduct(storm);

        int sandboxID = atcfPrd.getAdeckSandboxID();

        if (sandboxID <= 0) {

            /*
             * Check if the storm's A-Deck has been checked out into a sandbox
             * in other AtcfProducts.
             */
            String uid = getUID();

            List<Sandbox> sandboxes = null;

            GetSandboxesRequest getSandboxReq = new GetSandboxesRequest();
            getSandboxReq.addOneQueryCondition("region", storm.getRegion());
            getSandboxReq.addOneQueryCondition(YEAR, storm.getYear());
            getSandboxReq.addOneQueryCondition(CYCLONE_NUM,
                    storm.getCycloneNum());
            getSandboxReq.addOneQueryCondition("scopeCd", "ADECK");
            getSandboxReq.addOneQueryCondition("userId", uid);

            try {
                sandboxes = (List<Sandbox>) ThriftClient
                        .sendRequest(getSandboxReq);
            } catch (Exception e) {
                logger.error(formatRetrievalError("A-Deck sandbox", storm,
                        String.format("and user %s.  Creating one now...",
                                uid)),
                        e);
            }

            // Use the first sandbox - one sandbox per storm/user.
            if (sandboxes != null && !sandboxes.isEmpty()) {
                sandboxID = sandboxes.get(0).getId();
            } else {
                // Checkout from baseline table into sandbox.
                CheckoutDeckRequest checkoutAdeckReq = new CheckoutDeckRequest(
                        AtcfDeckType.A);
                checkoutAdeckReq.setBasin(storm.getRegion());
                checkoutAdeckReq.setYear(storm.getYear());
                checkoutAdeckReq.setCycloneNum(storm.getCycloneNum());
                checkoutAdeckReq.setStormName(storm.getStormName());
                checkoutAdeckReq.setUserId(uid);

                try {
                    sandboxID = (int) ThriftClient
                            .sendRequest(checkoutAdeckReq);
                } catch (Exception e) {
                    logger.error(formatCheckoutError("A-deck data", storm), e);
                }
            }

            // Set the id into the storm's AtcfProduct.
            atcfPrd.setAdeckSandboxID(sandboxID);
        }

        return sandboxID;
    }

    /**
     * Update a deck record for a given storm.
     *
     * @param sandboxId
     *            Id for the sandbox
     * @param deckRecord
     *            deckRecord to be updated
     * @param editType
     *            Type for update (NEW, MODIFY, DELETE)
     *
     */
    public static boolean updateDeckRecord(int sandboxId,
            AbstractDeckRecord deckRecord, RecordEditType editType) {
        return updateDeckRecord(sandboxId, deckRecord, editType, null);
    }

    /**
     * Update a deck record for a given storm.
     *
     * @param sandboxId
     *            Id for the sandbox
     * @param deckRecord
     *            deckRecord to be updated
     * @param editType
     *            Type for update (NEW, MODIFY, DELETE)
     * @param recType
     *            Type for record after update (NEW, MODIFY, DELETE)
     *
     */
    public static boolean updateDeckRecord(int sandboxId,
            AbstractDeckRecord deckRecord, RecordEditType editType,
            RecordEditType recType) {

        boolean success = false;

        // Dummy checks.
        if (sandboxId <= 0) {
            logger.warn(
                    "AtcfDataUtil.updateDeckRecord - cannot update record for invalid sandbox ID "
                            + sandboxId);
            return success;
        }

        if (deckRecord == null) {
            logger.warn("AtcfDataUtil - cannot update NULL record.");
            return success;
        }

        // Update the record in the sandbox.
        SaveDeckRecordEditRequest updateRecordReq = new SaveDeckRecordEditRequest();
        updateRecordReq.setSandboxId(sandboxId);
        updateRecordReq.setRecord(deckRecord);
        updateRecordReq.setEditType(editType);
        if (recType != null) {
            updateRecordReq.setRecordType(recType);
        }

        try {
            ThriftClient.sendRequest(updateRecordReq);
            success = true;
        } catch (Exception e) {
            logger.error("AtcfDataUtil - Fail to update deck record in sandbox "
                    + sandboxId, e);
        }

        return success;

    }

    /**
     * Update a list of deck records
     *
     * @param sandboxId
     * @param deckType
     * @param deckRecords
     * @return
     */
    public static boolean updateDeckRecords(int sandboxId,
            AtcfDeckType deckType, List<ModifiedDeckRecord> deckRecords) {

        boolean success = false;

        // Dummy checks.
        if (sandboxId <= 0) {
            logger.warn(
                    "AtcfDataUtil.updateDeckRecords - cannot update record for invalid sandbox ID "
                            + sandboxId);
            return success;
        }

        if (deckRecords == null || deckRecords.isEmpty()) {
            logger.warn("AtcfDataUtil - No records specified to be updated.");
            return success;
        }

        SaveEditDeckRecordSandboxRequest updateRecordReq = new SaveEditDeckRecordSandboxRequest();
        updateRecordReq.setSandboxId(sandboxId);
        updateRecordReq.setDeckType(deckType);
        updateRecordReq.setModifiedRecords(deckRecords);

        try {
            ThriftClient.sendRequest(updateRecordReq);
            success = true;
        } catch (Exception e) {
            logger.error("AtcfDataUtil - Fail to update deck record in sandbox "
                    + sandboxId, e);
        }

        return success;

    }

    /**
     * Wrapper to find the sandbox for ADeck.
     *
     * @param storm
     *            storm to to be checked.
     *
     * @return sandboxID
     */
    public static int getADeckSandbox(Storm storm) {
        return getDeckSandbox(storm, AtcfDeckType.A);
    }

    /**
     * Wrapper to find the sandbox for BDeck.
     *
     * @param storm
     *            storm to to be checked.
     *
     * @return sandboxID
     */
    public static int getBDeckSandbox(Storm storm) {
        return getDeckSandbox(storm, AtcfDeckType.B);
    }

    /**
     * Wrapper to find the sandbox for FDeck.
     *
     * @param storm
     *            storm to to be checked.
     *
     * @return sandboxID
     */
    public static int getFDeckSandbox(Storm storm) {
        return getDeckSandbox(storm, AtcfDeckType.F);
    }

    /**
     * Wrapper to find the sandbox for Forecast.
     *
     * @param storm
     *            storm to to be checked.
     *
     * @return sandboxID
     */
    public static int getFcstTrackSandbox(Storm storm) {
        return getDeckSandbox(storm, AtcfDeckType.T);
    }

    /**
     * Find if the data has been checkout into a sandbox by the same user in
     * other AtcfResources, assuming one working (un-submitted) sandbox per
     * storm/user.
     *
     * See #63859 - "Submitted" sandbox has a submitted time. Working sandboxes
     * have a "null" submitted time.
     *
     * @param storm
     *            storm to to be checked.
     * @param deckType
     *            Type of Deck (A,B,E,F,T) (T is for forecast track)
     *
     * @return sandboxID
     */
    @SuppressWarnings("unchecked")
    public static int getDeckSandbox(Storm storm, AtcfDeckType deckType) {

        int sandboxID = -1;
        String uid = getUID();

        /*
         * Check if the data has been checkout into a sandbox by the same user
         * in other AtcfResources.
         */
        List<Sandbox> sandboxes = null;

        GetSandboxesRequest getSandboxReq = new GetSandboxesRequest();
        getSandboxReq.addOneQueryCondition("region", storm.getRegion());
        getSandboxReq.addOneQueryCondition(YEAR, storm.getYear());
        getSandboxReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());
        getSandboxReq.addOneQueryCondition("sandboxType", "CHECKOUT");

        String scopeCD = "";
        switch (deckType) {
        case A:
            scopeCD = "ADECK";
            break;
        case B:
            scopeCD = "BDECK";
            break;
        case E:
            scopeCD = "EDECK";
            break;
        case F:
            scopeCD = "FDECK";
            break;
        case T:
            scopeCD = "FST";
            break;
        default:
            break;
        }

        if (!scopeCD.isEmpty()) {
            getSandboxReq.addOneQueryCondition("scopeCd", scopeCD);
        }

        getSandboxReq.addOneQueryCondition("userId", uid);

        try {
            sandboxes = (List<Sandbox>) ThriftClient.sendRequest(getSandboxReq);
        } catch (Exception e) {
            logger.error("AtcfDataUtil - GetSandboxesRequest failed: ", e);
            sandboxID = -1;
        }

        // Use the working sandbox.
        if (sandboxes != null && !sandboxes.isEmpty()) {
            /*
             * Assume one working (un-submitted) sandbox per storm/user.
             *
             * See #63859 - "Submitted" sandbox has a submitted time.
             */
            for (Sandbox sbx : sandboxes) {
                if (sbx.getSubmitted() == null) {
                    sandboxID = sbx.getId();
                    break;
                }
            }
        }

        return sandboxID;

    }

    /**
     * Performs a merge request
     *
     * @param base
     * @param dirty
     * @return
     */
    public static List<ConflictRecordPair> mergeSandboxes(int baseId) {

        MergeEditingSandboxWithBaselineRequest mergeRequest = new MergeEditingSandboxWithBaselineRequest();
        CheckSandboxMergeableRequest mergecheck = new CheckSandboxMergeableRequest();
        mergeRequest.setSandboxid(baseId);

        mergecheck.setSandboxid(baseId);

        try {
            // Request to check if the records are mergeable
            ConflictMergingRecordSet checkMerge = (ConflictMergingRecordSet) ThriftClient
                    .sendRequest(mergecheck);

            mergeRequest.setMergingRecordSet(checkMerge);

            if (!checkMerge.getConflictedRecords().isEmpty()) {
                return checkMerge.getConflictedRecords();
            } else {
                ThriftClient.sendRequest(mergeRequest);
            }

        } catch (Exception e) {
            logger.error("AtcfDataUtil - Merge Request failed with ", e);
        }

        return new ArrayList<>();
    }

    /**
     * Returns a list of all sandboxes
     *
     * @param storm
     * @param deckType
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public static List<Sandbox> getSandBoxes(Storm storm,
            AtcfDeckType deckType) {
        String uid = getUID();

        /*
         * Check if the data has been checkout into a sandbox by the same user
         * in other AtcfResources.
         */
        List<Sandbox> sandboxes = null;

        GetSandboxesRequest getSandboxReq = new GetSandboxesRequest();
        getSandboxReq.addOneQueryCondition("region", storm.getRegion());
        getSandboxReq.addOneQueryCondition(YEAR, storm.getYear());
        getSandboxReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());
        getSandboxReq.addOneQueryCondition("sandboxType", "CHECKOUT");

        String scopeCD = "";
        switch (deckType) {
        case A:
            scopeCD = "ADECK";
            break;
        case B:
            scopeCD = "BDECK";
            break;
        case E:
            scopeCD = "EDECK";
            break;
        case F:
            scopeCD = "FDECK";
            break;
        case T:
            scopeCD = "FST";
            break;
        default:
            logger.error(
                    "AtcfDataUtil: Invalid deck type: " + deckType.toString());
            return new ArrayList<>();
        }

        if (!scopeCD.isEmpty()) {
            getSandboxReq.addOneQueryCondition("scopeCd", scopeCD);
        }

        getSandboxReq.addOneQueryCondition("userId", uid);

        try {
            sandboxes = (List<Sandbox>) ThriftClient.sendRequest(getSandboxReq);
        } catch (Exception e) {
            sandboxes = null;
            logger.warn("AtcfDataUtil - No sandboxes found. ", e);
        }

        List<Sandbox> workingSandboxes = new ArrayList<>();
        // Use the working sandbox.
        if (sandboxes != null && !sandboxes.isEmpty()) {
            /*
             * Assume one working (un-submitted) sandbox per storm/user.
             *
             * See #63859 - "Submitted" sandbox has a submitted time.
             */
            for (Sandbox sbx : sandboxes) {
                if (sbx.getSubmitted() == null) {
                    workingSandboxes.add(sbx);
                }
            }
        }

        return new ArrayList<>(workingSandboxes);
    }

    /**
     * Returns a list of all invalid sandboxes
     *
     * @param storm
     * @param deckType
     * @return
     */
    public static List<Sandbox> getFlaggedSandboxes(Storm storm,
            AtcfDeckType deckType) {
        List<Sandbox> invalids = new ArrayList<>();
        List<Sandbox> sandboxes = getSandBoxes(storm, deckType);

        for (Sandbox sandbox : sandboxes) {
            if (sandbox.getValidFlag() == 1 && sandbox.getSubmitted() == null) {
                invalids.add(sandbox);
            }
        }

        return invalids;
    }

    /**
     * Retrieve F-Deck data for a given storm.
     *
     * First check if the data has been cached in AtcfProduct; if not, check if
     * data has been check into a sandbox; if not, check if the retrieval is for
     * edit, if yes, check out data into sandbox and return the data; if the
     * retrieval is for display, retrieve data from baseline tables.
     *
     * @param storm
     *            storm to retrieve data from
     * @param edit
     *            true - get data for editing; false - get data for display.
     *
     * @return Map<String, List<BDeckRecord>>
     */
    public static List<FDeckRecord> getFDeckRecords(Storm storm, boolean edit) {

        List<FDeckRecord> fdeckData = null;

        // Check if F-Deck data has been cached before.
        AtcfResourceData rscData = AtcfSession.getInstance().getAtcfResource()
                .getResourceData();
        AtcfProduct atcfPrd = rscData.getAtcfProduct(storm);

        int sandboxID = atcfPrd.getFdeckSandboxID();
        fdeckData = atcfPrd.getFDeckData();

        // Return existing data from AtcfProduct.
        if (((sandboxID > 0) || !edit) && fdeckData != null
                && !fdeckData.isEmpty()) {
            return fdeckData;
        }

        // Check if sandbox exists; if not, check out one if "edit".
        if (sandboxID <= 0) {
            sandboxID = getFDeckSandbox(storm);

            /*
             * Try to retrieve from the sandbox. Return at success, otherwise
             * reset sandbox to -1 so we need to check out data to a new
             * sandbox.
             *
             * Note: ideally, we should either re-check into the existing
             * sandbox or cleanup the sandbox and its associated deck data
             * table?
             */
            if (sandboxID > 0) {
                GetDeckRecordsRequest getFdeckReq = new GetDeckRecordsRequest(
                        AtcfDeckType.F);
                getFdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
                getFdeckReq.addOneQueryCondition(YEAR, storm.getYear());
                getFdeckReq.addOneQueryCondition(CYCLONE_NUM,
                        storm.getCycloneNum());

                getFdeckReq.setSandboxId(sandboxID);

                try {
                    fdeckData = requestDeckRecords(getFdeckReq,
                            FDeckRecord.class);
                } catch (Exception e) {
                    sandboxID = -1;
                    logger.warn("AtcfDataUtil - No sandbox found. ", e);
                }

                if (fdeckData == null || fdeckData.isEmpty()) {
                    sandboxID = -1;
                }

            }

            // Checkout from baseline into sandbox, if "edit".
            if (sandboxID <= 0 && edit) {
                CheckoutDeckRequest checkoutFdeckReq = new CheckoutDeckRequest(
                        AtcfDeckType.F);
                checkoutFdeckReq.setBasin(storm.getRegion());
                checkoutFdeckReq.setYear(storm.getYear());
                checkoutFdeckReq.setCycloneNum(storm.getCycloneNum());
                checkoutFdeckReq.setStormName(storm.getStormName());

                String uid = getUID();
                checkoutFdeckReq.setUserId(uid);

                try {
                    sandboxID = (int) ThriftClient
                            .sendRequest(checkoutFdeckReq);
                    atcfPrd.setFdeckSandboxID(sandboxID);
                } catch (Exception e) {
                    logger.error(formatCheckoutError("F-deck data", storm), e);
                }
            }

            rscData.setFdeckSandbox(storm, sandboxID);

        }

        // Retrieve it from the new sandbox or baseline (sandboxID <= 0).
        if (fdeckData == null || fdeckData.isEmpty()) {

            GetDeckRecordsRequest getFdeckReq = new GetDeckRecordsRequest(
                    AtcfDeckType.F);
            getFdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
            getFdeckReq.addOneQueryCondition(YEAR, storm.getYear());
            getFdeckReq.addOneQueryCondition(CYCLONE_NUM,
                    storm.getCycloneNum());

            getFdeckReq.setSandboxId(sandboxID);

            try {
                fdeckData = requestDeckRecords(getFdeckReq, FDeckRecord.class);
            } catch (Exception e) {
                logger.error(formatRetrievalError("F-deck records", storm,
                        sandboxID), e);
            }
        }

        // Warn the user here or let the caller do it?
        if (fdeckData == null || fdeckData.isEmpty()) {
            MessageDialog noDeckFound = new MessageDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    "No F-Deck Record Found", null,
                    "No F-Deck record found for storm " + storm.getStormId(),
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);
            noDeckFound.open();
        } else {
            // Sort records by data time
            Collections.sort(fdeckData,
                    Comparator.comparing(FDeckRecord::getRefTime));
        }

        return fdeckData;
    }

    /**
     * Checkout a storm's A-Deck data to sandbox for a given date time groups
     * (DTG).
     *
     * @param storm
     *            storm
     * @param dtgs
     *            List<String>
     * @param sanboxID
     *            integer
     *
     * @return sandbox id
     */
    public static int checkoutADeckByDTGs(Storm storm, List<String> dtgs,
            int sandboxID) {

        CheckoutADeckDtgRequest checkoutAdeckReq = new CheckoutADeckDtgRequest();
        checkoutAdeckReq.setBasin(storm.getRegion());
        checkoutAdeckReq.setYear(storm.getYear());
        checkoutAdeckReq.setCycloneNum(storm.getCycloneNum());
        checkoutAdeckReq.setStormName(storm.getStormName());

        String uid = getUID();
        checkoutAdeckReq.setUserId(uid);

        Calendar refTime = TimeUtil.newCalendar();
        for (String dtg : dtgs) {

            checkoutAdeckReq.setSandboxId(sandboxID);

            try {
                Date date = parseDate(dtg, DTG_FORMAT);
                refTime.setTime(date);
            } catch (DateTimeParseException e1) {
                logger.error(
                        "AtcfDataUtil - retrieveAdeckData: Invalid date/time string (yyyyMMddHH): "
                                + dtg,
                        e1);
            }

            checkoutAdeckReq.setDtg(refTime);

            try {
                sandboxID = (int) ThriftClient.sendRequest(checkoutAdeckReq);
            } catch (Exception e) {
                logger.error(formatRetrievalError("A-deck data", storm,
                        String.format(" and DTG %s", dtg)), e);
            }
        }

        return sandboxID;
    }

    /**
     * Get the list of ADeck date/time group from a sandbox_dtg.
     *
     * @param sandboxID
     *            ID of the sand box (user/storm specific)
     * @return List of date/time group in format of (yyyyMMddHH)
     */
    public static List<String> getSandboxADeckDTGs(int sandboxID) {

        List<String> timeSet = new ArrayList<>();

        String dtg = "SELECT DISTINCT dtg FROM atcf.sandbox_dtg "
                + "WHERE sandbox_id = " + sandboxID + " ORDER BY dtg";

        try {
            List<Object[]> adtg = DirectDbQuery.executeQuery(dtg, "metadata",
                    DirectDbQuery.QueryLanguage.SQL);
            timeSet = timeStampsToDTGs(adtg);
        } catch (Exception ee) {
            logger.error(
                    "AtcfDataUti.getSandboxADeckDTGs: Error retrieving DTG for sandbox "
                            + sandboxID,
                    ee);
        }

        return timeSet;
    }

    /**
     * Find if a ADeckRecord exists in baseline or sandbox.
     *
     * @param storm
     *            Storm
     * @param rkey
     *            RecordKey
     * @param sandboxID
     *            -1 - baseline; > 0 sandbox
     * @return true/false
     */
    public static ADeckRecord getADeckRecord(Storm storm, RecordKey rkey,
            int sandboxID) {

        ADeckRecord rec = null;

        GetDeckRecordsRequest getADeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.A);
        getADeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getADeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getADeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());
        getADeckReq.addOneQueryCondition(TECHNIQUE, rkey.getAid());
        getADeckReq.addOneQueryCondition(RAD_WIND, (float) rkey.getWindRad());
        getADeckReq.setSandboxId(sandboxID);

        Date date;

        try {
            date = parseDate(rkey.getDtg(), DTG_FORMAT);
        } catch (DateTimeParseException e) {
            logger.error(String.format("Invalid DTG: %s", rkey.getDtg()), e);
            return rec;
        }

        getADeckReq.addOneQueryCondition(AbstractAtcfRecord.REFTIME_ID, date);

        // Query data.
        try {
            List<ADeckRecord> records = requestDeckRecords(getADeckReq,
                    ADeckRecord.class);
            if (records != null && !records.isEmpty()) {
                for (ADeckRecord datarec : records) {
                    if (datarec.getFcstHour() == rkey.getTau()) {
                        rec = datarec;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("AtcfDataUtil - No A_Deck record retrieved.", e);
            return rec;
        }

        return rec;
    }

    /**
     * Create a calendar from a given DTG String (yyyyMMddHH or yyyyMMddHHmm),
     * null if dtg is invalid.
     *
     * @param dtg
     *            DTG string in format of yyyyMMddHH or yyyyMMddHHmm
     * @return Calendar
     */
    public static Date parseDtg(String dtg) {
        DateTimeFormatter formatter = dtg.length() > 10 ? DTG_FORMAT_LONG
                : DTG_FORMAT;

        try {
            return parseDate(dtg, formatter);
        } catch (DateTimeException e) {
            logger.warn("Invalid DTG: " + dtg, e);
            return null;
        }
    }

    /**
     * Used to help keep track of newly added storms in the Start a Storm Dialog
     *
     * @param storm
     * @delete true to remove, false to add.
     */
    public static synchronized void updateStormList(Storm storm,
            boolean delete) {

        if (stormList == null) {
            stormList = getStormList();
        }

        if (delete) {
            stormList.remove(storm);
        } else {
            stormList.add(storm);
        }

        updateFullStormList(storm, delete);
    }

    /**
     * Used to help keep track of newly added storms in the Start a Storm Dialog
     *
     * @param storm
     * @delete true to remove, false to add.
     */
    public static synchronized void updateFullStormList(Storm storm,
            boolean delete) {

        if (fullStormList == null) {
            fullStormList = getFullStormList();
        }

        if (delete) {
            fullStormList.remove(storm);
        } else {
            fullStormList.add(storm);
        }
    }

    /**
     * Used to help keep track of newly added genesis in the Start a Genesis
     * Dialog
     *
     * @param genesis
     */
    public static synchronized void updateGenesisList(Genesis genesis) {

        if (genesisList == null) {
            genesisList = getGenesisList();
        }

        int index = genesisList.indexOf(genesis);
        if (index < 0) {
            genesisList.add(genesis);
            updateFullStormList(genesis.toStorm(), false);
        } else {
            genesisList.set(index, genesis);
            int ii = 0;
            for (Storm storm : fullStormList) {
                if (storm.getStormId().equalsIgnoreCase(genesis.getGenesisId())
                        && storm.getStormName()
                                .equalsIgnoreCase(genesis.getGenesisName())) {
                    break;
                }

                ii++;
            }

            if (ii >= 0) {
                fullStormList.set(ii, genesis.toStorm());
            }
        }
    }

    /**
     * Get current user ID for the session.
     */
    private static String getUID() {
        return AtcfSession.getInstance().getUid();
    }

    /**
     * Update A-Deck records for a given storm.
     *
     * @param aRecs
     *            List of records to update
     * @param sandBoxID
     *            The sandbox ID
     *
     * @param editType
     *            The type of update to make
     *
     * @return true if successful
     */
    public static boolean updateADeckRecords(List<ADeckRecord> aRecs,
            int sandBoxID, RecordEditType editType) {
        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
        for (ADeckRecord rec : aRecs) {
            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            mdr.setRecord(rec);
            modifiedRecords.add(mdr);
        }
        return AtcfDataUtil.updateDeckRecords(sandBoxID, AtcfDeckType.A,
                modifiedRecords);
    }

    /**
     * Update F-Deck records for a given storm.
     *
     * @param fRecs
     *            List of records to update
     * @param sandBoxID
     *            The sandbox ID
     *
     * @param editType
     *            The type of update to make
     *
     * @return true if successful
     */
    public static boolean updateFDeckRecords(List<FDeckRecord> fRecs,
            int sandBoxID, RecordEditType editType) {
        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
        for (FDeckRecord rec : fRecs) {
            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            mdr.setRecord(rec);
            modifiedRecords.add(mdr);
        }
        return AtcfDataUtil.updateDeckRecords(sandBoxID, AtcfDeckType.F,
                modifiedRecords);
    }

    /**
     * Retrieve forecast track data for a given storm.
     *
     * First check if the data has been cached in AtcfProduct; if not, check if
     * data has been check into a sandbox; if not, check if the retrieval is for
     * edit, if yes, check out data into sandbox and return the data; if the
     * retrieval is for display, retrieve data from baseline tables.
     *
     * @param storm
     *            storm to retrieve data from
     * @param edit
     *            true - get data for editing; false - get data for display.
     *
     * @return Map<String, List<ForecasTtrackRecord>>
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<ForecastTrackRecord>> getFcstTrackRecords(
            Storm storm, boolean edit) {

        Map<String, List<ForecastTrackRecord>> fcstTrackDataMap = null;
        List<ForecastTrackRecord> fcstTrackData = null;

        // Check if the data has been cached before in AtcfProduct.
        AtcfResourceData rscData = AtcfSession.getInstance().getAtcfResource()
                .getResourceData();
        AtcfProduct atcfPrd = rscData.getAtcfProduct(storm);

        int sandboxID = atcfPrd.getFcstTrackSandboxID();
        fcstTrackDataMap = atcfPrd.getFcstTrackDataMap();

        // Always retrieve forecast that matches latest best track DTG.
        Map<String, List<BDeckRecord>> currentBDeckRecords = AtcfDataUtil
                .getBDeckRecords(storm, false);

        String latestBDeckDtg = Collections.max(currentBDeckRecords.keySet());
        boolean latestBDeckMatchFcst = false;
        if (fcstTrackDataMap != null && !fcstTrackDataMap.isEmpty()) {
            String fcstDtg = Collections.min(fcstTrackDataMap.keySet());
            if (fcstDtg.equals(latestBDeckDtg)) {
                latestBDeckMatchFcst = true;
            }
        }

        /*
         * Return existing data from AtcfProduct.
         *
         * Note: This method will set sandbox to AtcfProduct but not the data,
         * so the data may not have been set into AtcfProduct until
         * setFcstTrackMap() is called later.
         */
        if (((sandboxID > 0) || !edit) && latestBDeckMatchFcst) {
            return fcstTrackDataMap;
        } else {
            fcstTrackDataMap = new LinkedHashMap<>();
        }

        // Check if the specified DTG is valid.
        Date dataDate;
        try {
            dataDate = parseDate(latestBDeckDtg, DTG_FORMAT);
        } catch (DateTimeException e) {
            logger.error(
                    "AtcfDataUtil.getFcstTrackRecords(): Invalid date/time string (yyyyMMddHH): "
                            + latestBDeckDtg,
                    e);
            return fcstTrackDataMap;
        }

        /*
         * Try to retrieve from the existing sandbox. Return at success,
         * otherwise reset sandbox to -1 so we need to check out data to a new
         * sandbox.
         */
        if (sandboxID <= 0) {
            sandboxID = getFcstTrackSandbox(storm);
        }

        if (sandboxID > 0) {
            GetForecastTrackRecordsRequest getFcstTrackReq = new GetForecastTrackRecordsRequest();
            getFcstTrackReq.addOneQueryCondition(BASIN, storm.getRegion());
            getFcstTrackReq.addOneQueryCondition(YEAR, storm.getYear());
            getFcstTrackReq.addOneQueryCondition(CYCLONE_NUM,
                    storm.getCycloneNum());
            getFcstTrackReq.addOneQueryCondition(AbstractAtcfRecord.REFTIME_ID,
                    dataDate);

            getFcstTrackReq.setSandboxId(sandboxID);

            try {
                fcstTrackData = (List<ForecastTrackRecord>) ThriftClient
                        .sendRequest(getFcstTrackReq);
            } catch (Exception e) {
                sandboxID = -1;
                logger.warn(
                        "AtcfDataUtil.getFcstTrackRecords(): cannot retrieve forecast track from sandbox.",
                        e);
            }

            if (fcstTrackData == null || fcstTrackData.isEmpty()) {
                sandboxID = -1;
            }
        }

        // Checkout from baseline into a new sandbox for "editing".
        if (sandboxID <= 0) {

            // Read from baseline.
            GetForecastTrackRecordsRequest getFcsttrackReq = new GetForecastTrackRecordsRequest();
            getFcsttrackReq.addOneQueryCondition(BASIN, storm.getRegion());
            getFcsttrackReq.addOneQueryCondition(YEAR, storm.getYear());
            getFcsttrackReq.addOneQueryCondition(CYCLONE_NUM,
                    storm.getCycloneNum());

            List<ForecastTrackRecord> ftData = null;
            fcstTrackData = new ArrayList<>();
            try {
                ftData = (List<ForecastTrackRecord>) ThriftClient
                        .sendRequest(getFcsttrackReq);
                for (ForecastTrackRecord ftd : ftData) {
                    if (dataDate.equals(ftd.getRefTime())) {
                        fcstTrackData.add(ftd);
                    }
                }
            } catch (Exception e) {
                logger.error(formatRetrievalError("forecast track records",
                        storm, " from baseline"), e);
            }

            // Put into sandbox for editing if requested.
            if (edit && ftData != null && !ftData.isEmpty()) {
                NewForecastTrackRequest fcstTrackReq = new NewForecastTrackRequest();
                fcstTrackReq.setCurrentStorm(storm);

                Collections.reverse(ftData);
                fcstTrackReq.setFstRecords(ftData);
                fcstTrackReq.setUserId(AtcfSession.getInstance().getUid());

                try {
                    sandboxID = (int) ThriftClient.sendRequest(fcstTrackReq);
                } catch (Exception e) {
                    sandboxID = -1;
                    logger.warn(
                            "AtcfDataUtil.getFcstTrackRecords(): cannot check out forecast track into sandbox.",
                            e);
                }
            }
        }

        // Store/update sandbox ID in AtcfResoureData
        rscData.setFcstTrackSandbox(storm, sandboxID);

        // Sort records
        if (fcstTrackData != null && !fcstTrackData.isEmpty()) {

            // Sort records by data time
            Collections.sort(fcstTrackData, Comparator
                    .comparing(ForecastTrackRecord::getForecastDateTime));

            /*
             * Sort records into a DTG map. For forecast track, forecast hour is
             * added into DTG and also the forecast hour may have TAU 3, 4, 5,
             * 6, 7, 8 for special advisories.
             */
            fcstTrackDataMap = sortForecastTrackDataInDTGs(fcstTrackData,
                    false);
        } else {
            String msg = "AtcfDataUtil - No forecast track record retrieved for storm "
                    + storm.getStormId();
            if (sandboxID > 0) {
                msg += (" from sandbox " + sandboxID);
            }

            logger.warn(msg);
        }

        return fcstTrackDataMap;
    }

    /**
     * Sort forecast track record into a map by date/time groups.
     *
     * Note:Forecast track may have 3 hour interval.
     *
     * @param fcstTrackRecords
     *            List of ForecastTrackRecord
     * @param synopticHr
     *            Flag to pick records at synoptic hours
     * @return Map<String, List<ForecastTrackRecord>>
     */
    public static Map<String, List<ForecastTrackRecord>> sortForecastTrackDataInDTGs(
            List<ForecastTrackRecord> fcstTrackRecords, boolean synopticHr) {

        Map<String, List<ForecastTrackRecord>> dataMap = new LinkedHashMap<>();

        for (ForecastTrackRecord rec : fcstTrackRecords) {

            Calendar dataTime = rec.getRefTimeAsCalendar();
            dataTime.add(Calendar.HOUR_OF_DAY, rec.getFcstHour());
            int hour = dataTime.get(Calendar.HOUR_OF_DAY);

            // Check if synoptic hours only
            if (!synopticHr || (hour % 3 == 0)) {
                String dtg = calendarToDateTimeString(dataTime);
                List<ForecastTrackRecord> dtgRecords = dataMap
                        .computeIfAbsent(dtg, k -> new ArrayList<>());

                dtgRecords.add(rec);
            }
        }

        SortedSet<String> dtgs = new TreeSet<>(dataMap.keySet());
        Map<String, List<ForecastTrackRecord>> sortedMap = new LinkedHashMap<>();
        for (String dtg : dtgs) {
            sortedMap.put(dtg, dataMap.get(dtg));
        }

        return sortedMap;
    }

    /**
     * Retrieve E-Deck baseline data for a given storm.
     *
     * @param storm
     *            storm to retrieve data from
     *
     * @return Map<String, List<EDeckRecord>>
     */
    public static Map<String, List<EDeckRecord>> getEDeckRecords(Storm storm) {

        Map<String, List<EDeckRecord>> edeckDataMap = new LinkedHashMap<>();
        List<EDeckRecord> edeckData = null;

        GetDeckRecordsRequest getEdeckReq = new GetDeckRecordsRequest(
                AtcfDeckType.E);
        getEdeckReq.addOneQueryCondition(BASIN, storm.getRegion());
        getEdeckReq.addOneQueryCondition(YEAR, storm.getYear());
        getEdeckReq.addOneQueryCondition(CYCLONE_NUM, storm.getCycloneNum());

        getEdeckReq.setSandboxId(-1);

        try {
            edeckData = requestDeckRecords(getEdeckReq, EDeckRecord.class);
        } catch (Exception e) {
            logger.error(formatRetrievalError("E-deck records", storm), e);
        }

        // Warn the user here or let the caller do it?
        if (edeckData == null || edeckData.isEmpty()) {
            MessageDialog noEDeckFound = new MessageDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    "No E-Deck Record Found", null,
                    "No E-Deck baseline record found for storm "
                            + storm.getStormId(),
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);
            noEDeckFound.open();
        } else {
            // Sort records by data time
            Collections.sort(edeckData,
                    Comparator.comparing(EDeckRecord::getForecastDateTime));

            // Sort records into a DTG map
            edeckDataMap = sortEDeckInDTGs(edeckData, true);
        }

        return edeckDataMap;
    }

    /**
     * Sort EDeckRecords into a map by date/time groups.
     *
     * @param edeckRecords
     *            List of EDeckRecords
     * @param synopticHr
     *            Flag to pick records at synoptic hours
     * @return Map<String, List<EDeckRecord>>
     */
    public static Map<String, List<EDeckRecord>> sortEDeckInDTGs(
            List<EDeckRecord> edeckRecords, boolean synopticHr) {

        Map<String, List<EDeckRecord>> dataMap = new LinkedHashMap<>();

        for (EDeckRecord rec : edeckRecords) {

            Calendar dataTime = rec.getRefTimeAsCalendar();
            int hour = dataTime.get(Calendar.HOUR_OF_DAY);

            // Check if synoptic hours only
            if (!synopticHr || (hour % 6 == 0)) {
                String dtg = calendarToDateTimeString(dataTime);
                List<EDeckRecord> dtgRecords = dataMap.computeIfAbsent(dtg,
                        k -> new ArrayList<>());

                dtgRecords.add(rec);
            }
        }

        return dataMap;
    }

    /**
     * Create a ForecastTrackRecord from an A or B deck record.
     *
     * @param irec
     *            AbstractDeckRecord
     *
     * @return ForecastTrackRecord
     */
    public static ForecastTrackRecord makeFcstTrackRecord(
            AbstractDeckRecord irec) {

        ForecastTrackRecord frec = new ForecastTrackRecord();
        frec.setTechnique("OFCL");
        frec.setTechniqueNum(3);

        frec.setBasin(irec.getBasin());
        frec.setClat(irec.getClat());
        frec.setClon(irec.getClon());
        frec.setRefTime(irec.getRefTime());
        frec.setCycloneNum(irec.getCycloneNum());
        frec.setYear(irec.getYear());
        frec.setForecaster(irec.getForecaster());
        frec.setReportType(irec.getReportType());

        if (irec instanceof BaseBDeckRecord) {

            BaseBDeckRecord rec = (BaseBDeckRecord) irec;
            frec.setFcstHour(rec.getFcstHour());
            frec.setStormName(rec.getStormName());
            frec.setClosedP(rec.getClosedP());
            frec.setEyeSize(rec.getEyeSize());
            frec.setGust(rec.getGust());
            frec.setIntensity(rec.getIntensity());
            frec.setMaxSeas(rec.getMaxSeas());
            frec.setMaxWindRad(rec.getMaxWindRad());
            frec.setMslp(rec.getMslp());
            frec.setQuad1WaveRad(rec.getQuad1WaveRad());
            frec.setQuad2WaveRad(rec.getQuad2WaveRad());
            frec.setQuad3WaveRad(rec.getQuad3WaveRad());
            frec.setQuad4WaveRad(rec.getQuad4WaveRad());
            frec.setQuad1WindRad(rec.getQuad1WindRad());
            frec.setQuad2WindRad(rec.getQuad2WindRad());
            frec.setQuad3WindRad(rec.getQuad3WindRad());
            frec.setQuad4WindRad(rec.getQuad4WindRad());
            frec.setRadClosedP(rec.getRadClosedP());
            frec.setRadWave(rec.getRadWave());
            frec.setRadWaveQuad(rec.getRadWaveQuad());
            frec.setRadWind(rec.getRadWind());
            frec.setRadWindQuad(rec.getRadWindQuad());
            frec.setRefTime(new Date(rec.getRefTime().getTime()));
            frec.setStormDepth(rec.getStormDepth());
            frec.setStormDrct(rec.getStormDrct());
            frec.setStormSped(rec.getStormSped());
            frec.setSubRegion(rec.getSubRegion());
            frec.setUserData(rec.getUserData());
            frec.setUserDefined(rec.getUserDefined());
            frec.setWindMax(rec.getWindMax());

        } else if (irec instanceof BaseADeckRecord) {
            BaseADeckRecord rec = (BaseADeckRecord) irec;
            frec.setFcstHour(rec.getFcstHour());
            frec.setStormName(rec.getStormName());
            frec.setClosedP(rec.getClosedP());
            frec.setEyeSize(rec.getEyeSize());
            frec.setGust(rec.getGust());
            frec.setIntensity(rec.getIntensity());
            frec.setMaxSeas(rec.getMaxSeas());
            frec.setMaxWindRad(rec.getMaxWindRad());
            frec.setMslp(rec.getMslp());
            frec.setQuad1WaveRad(rec.getQuad1WaveRad());
            frec.setQuad2WaveRad(rec.getQuad2WaveRad());
            frec.setQuad3WaveRad(rec.getQuad3WaveRad());
            frec.setQuad4WaveRad(rec.getQuad4WaveRad());
            frec.setQuad1WindRad(rec.getQuad1WindRad());
            frec.setQuad2WindRad(rec.getQuad2WindRad());
            frec.setQuad3WindRad(rec.getQuad3WindRad());
            frec.setQuad4WindRad(rec.getQuad4WindRad());
            frec.setRadClosedP(rec.getRadClosedP());
            frec.setRadWave(rec.getRadWave());
            frec.setRadWaveQuad(rec.getRadWaveQuad());
            frec.setRadWind(rec.getRadWind());
            frec.setRadWindQuad(rec.getRadWindQuad());
            frec.setRefTime(new Date(rec.getRefTime().getTime()));
            frec.setStormDepth(rec.getStormDepth());
            frec.setStormDrct(rec.getStormDrct());
            frec.setStormSped(rec.getStormSped());
            frec.setSubRegion(rec.getSubRegion());
            frec.setUserData(rec.getUserData());
            frec.setUserDefined(rec.getUserDefined());
            frec.setWindMax(rec.getWindMax());
        } else {
            logger.warn(
                    "AtcfDataUtil.makeFcstTrackRecord - Not valid deck record, only basic information is filled.");
        }

        return frec;
    }

    /**
     * Round a number to an integer to the "nearest" (i.e, if nearest is 5, then
     * round 61.5 to 60 and round 63.7 to 65). If "nearest" is "0", then it
     * simply cast with (int).
     *
     * @param value
     *            Value to be rounded
     * @param nearest
     *            desired nearest (i.e, 5, 10 etc)
     * @return roundToNearest()
     */
    public static int roundToNearest(double value, int nearest) {

        if (nearest == 0) {
            return (int) value;
        } else {
            return (int) Math.round(value / nearest) * nearest;
        }
    }

    /**
     * Get a storm by its stormId.
     *
     * @param stormId
     * @return Storm
     */
    @SuppressWarnings({ "unchecked" })
    public static Storm getStorm(String stormId) {

        Storm storm = null;
        GetStormsRequest getStormsRequest = new GetStormsRequest();

        getStormsRequest.setQueryConditions(
                Collections.singletonMap("stormId", stormId));

        try {
            storm = ((List<Storm>) ThriftClient.sendRequest(getStormsRequest))
                    .get(0);

        } catch (Exception e) {
            logger.error(
                    "AtcfDataUtil - Could not find Storm with ID: " + stormId,
                    e);
        }
        return storm;
    }

    /**
     * Get a genesis by its genesisId.
     *
     * @param genesisId
     * @return Genesis
     */
    @SuppressWarnings({ "unchecked" })
    public static Genesis getGenesis(String genesisId) {

        Genesis genesis = null;
        GetGenesisRequest getGenesisRequest = new GetGenesisRequest();

        getGenesisRequest.setQueryConditions(
                Collections.singletonMap("genesisId", genesisId));
        try {
            genesis = ((List<Genesis>) ThriftClient
                    .sendRequest(getGenesisRequest)).get(0);

        } catch (Exception e) {
            logger.error("AtcfDataUtil - Could not find Genesis with ID: "
                    + genesisId, e);
        }
        return genesis;
    }

    /**
     * Round a floating point number to a nearest increment.
     *
     * Method adapted from nhc_writeadv.f.
     *
     * @param number
     *            Floating point number to be rounded.
     * @param inc
     *            integer increment to which to round.
     * @return integer Result of 'number' rounded to the nearest increment
     *         'inc'.
     */
    public static int round(float number, int inc) {
        return inc * (int) ((number + .5 * inc) / inc);
    }

    /**
     * Retrieve a set of storm names used for the give year & basin.
     *
     * @param basin
     * @param year
     *
     * @return Set<String> Storm names
     */
    public static Set<String> getStormNames(String basin, int year) {

        java.util.Set<String> names = new TreeSet<>();

        // Check storms in DB
        for (Storm stm : getFullStormList()) {
            if (year == stm.getYear()
                    && basin.equalsIgnoreCase(stm.getRegion())) {
                names.add(stm.getStormName());
            }
        }

        // Check storms in storm.table
        for (Map.Entry<String, Storm> entry : stormTable.entrySet()) {
            String sid = entry.getKey();
            if (sid.startsWith(basin) && sid.endsWith("" + year)) {
                names.add(entry.getValue().getStormName());
            }
        }

        return names;
    }

    /**
     * Check if a storm number has been used for a basin and year.
     *
     * @param storm
     *            Storm
     *
     * @param newNum
     *            New cyclone number.
     *
     * @return boolean
     */
    public static boolean isCycloneNumUsed(Storm storm, int newNum) {
        return isCycloneNumUsed(storm.getRegion(), storm.getYear(), newNum);
    }

    /**
     * Check if a storm number has been used for a basin and year.
     *
     * @param basin
     *            AtcfBasin
     * @param year
     *            Storm year
     * @param newNum
     *            New cyclone number.
     *
     * @return boolean
     */
    public static boolean isCycloneNumUsed(String basin, int year, int newNum) {

        boolean isUsed = false;

        // Check storms in DB
        for (Storm stm : getFullStormList()) {
            if (year == stm.getYear() && basin.equalsIgnoreCase(stm.getRegion())
                    && newNum == stm.getCycloneNum()) {
                isUsed = true;
                break;
            }
        }

        // Check storms in storm.table
        if (!isUsed) {
            String sid = basin.toUpperCase() + String.format("%02d", newNum)
                    + year;
            if (stormTable.containsKey(sid)) {
                isUsed = true;
            }
        }

        return isUsed;
    }

    /**
     * Check if a storm name has been used for a basin and year.
     *
     * @param storm
     *            Storm
     *
     * @param newName
     *            New storm name.
     *
     * @return boolean
     */
    public static boolean isStormNameUsed(String basin, int year,
            String newName) {
        return getStormNames(basin, year).contains(newName);
    }

    /**
     * Validate if a new storm name has been used in the same basin and year. If
     * not, trim and return it as uppercase.
     *
     * @param shell
     *            Shell
     * @param storm
     *            Storm
     * @param newName
     *            New storm name.
     * @param allowSameName
     *            Flag to allow the input storm's name be used, true for update
     *            storm, and false for copying storm.
     * @return String
     */
    public static String getValidStormName(Shell shell, Storm storm,
            String nameIn, boolean allowSameName) {

        String validName = null;

        // Validate storm name.
        String title = "Check Storm Name";
        String msg = null;
        String msgEnd = "\nPlease enter a new storm name.";

        String newName = nameIn.trim().toUpperCase();
        if (newName.isEmpty()) {
            msg = "Storm name cannot be empty.";
        } else {
            boolean checkName = !(allowSameName
                    && newName.equalsIgnoreCase(storm.getStormName()));
            if (checkName && isStormNameUsed(storm.getRegion(), storm.getYear(),
                    newName)) {
                msg = "Storm name " + newName + " has been used for basin "
                        + storm.getRegion() + " in year " + storm.getYear();
            }
        }

        if (msg != null) {
            MessageDialog.openWarning(shell, title, msg + msgEnd);
        } else {
            validName = nameIn.trim().toUpperCase();
        }

        return validName;
    }

    /**
     * Validate if a new storm number has been used in the same basin and year.
     * If not, parse and return it.
     *
     * @param shell
     *            Shell
     * @param storm
     *            Storm
     * @param newNum
     *            New cyclone number.
     * @param allowSameNumber
     *            Flag to allow the input storm's cyclone number be used, true
     *            for update storm, and false for copying storm.
     * @return String
     */

    public static int getValidCycloneNumber(Shell shell, Storm storm,
            String numStr, boolean allowSameNumber) {

        int validNum = -1;

        // Validate cyclone number.
        String title = "Check Cyclone Number";
        int newNum = 0;
        String msg = null;
        String msgEnd = "\nPlease select a new cylone number.";
        if (numStr.isEmpty()) {
            msg = "Cyclone number is missing. It should be 1 to 99.";
        } else {
            try {
                newNum = Integer.parseInt(numStr);
                if (newNum <= 0 || newNum > 99) {
                    msg = "Cyclone number " + newNum
                            + " is invalid. It should be 1 to 99.";
                } else {
                    if (isCycloneNumUsed(storm, newNum) && !(allowSameNumber
                            && storm.getCycloneNum() == newNum)) {
                        msg = "Cylcone number " + newNum
                                + " has been used for basin "
                                + storm.getRegion() + " in year "
                                + storm.getYear();
                    }
                }
            } catch (NumberFormatException e) {
                msg = "Cyclone number " + newNum
                        + " is invalid. It should be 1 to 99.";
            }
        }

        if (msg != null) {
            MessageDialog.openWarning(shell, title, msg + msgEnd);
        } else {
            validNum = newNum;
        }

        return validNum;
    }

    /**
     * Get the next unused new storm number (90 - 99) for a basin and year.
     *
     * @param basin
     *            AtcfBasin
     * @param year
     *            Storm year
     * @return int
     */
    public static int getNextUnusedStormNumber(String basin, int year) {

        int stNum = 90;

        for (int ii = 90; ii < 100; ii++) {
            if (!isCycloneNumUsed(basin, year, ii)) {
                stNum = ii;
                break;
            }
        }

        return stNum;
    }

    /**
     * Create an ADeckRecord from a ForecastTrackRecord.
     *
     * @param fstRec
     *            ForecastTrackRecord
     *
     * @return ADeckRecord
     */
    public static ADeckRecord fcstToADeckRecord(ForecastTrackRecord fstRec) {

        ADeckRecord arec = new ADeckRecord();
        arec.setTechnique("OFCL");
        arec.setTechniqueNum(3);

        arec.setBasin(fstRec.getBasin());
        arec.setClat(fstRec.getClat());
        arec.setClon(fstRec.getClon());
        arec.setRefTime(fstRec.getRefTime());
        arec.setCycloneNum(fstRec.getCycloneNum());
        arec.setYear(fstRec.getYear());
        arec.setForecaster(fstRec.getForecaster());
        arec.setReportType(fstRec.getReportType());

        arec.setFcstHour(fstRec.getFcstHour());
        arec.setStormName(fstRec.getStormName());
        arec.setClosedP(fstRec.getClosedP());
        arec.setEyeSize(fstRec.getEyeSize());
        arec.setGust(fstRec.getGust());
        arec.setIntensity(fstRec.getIntensity());
        arec.setMaxSeas(fstRec.getMaxSeas());
        arec.setMaxWindRad(fstRec.getMaxWindRad());
        arec.setMslp(fstRec.getMslp());
        arec.setQuad1WaveRad(fstRec.getQuad1WaveRad());
        arec.setQuad2WaveRad(fstRec.getQuad2WaveRad());
        arec.setQuad3WaveRad(fstRec.getQuad3WaveRad());
        arec.setQuad4WaveRad(fstRec.getQuad4WaveRad());
        arec.setQuad1WindRad(fstRec.getQuad1WindRad());
        arec.setQuad2WindRad(fstRec.getQuad2WindRad());
        arec.setQuad3WindRad(fstRec.getQuad3WindRad());
        arec.setQuad4WindRad(fstRec.getQuad4WindRad());
        arec.setRadClosedP(fstRec.getRadClosedP());
        arec.setRadWave(fstRec.getRadWave());
        arec.setRadWaveQuad(fstRec.getRadWaveQuad());
        arec.setRadWind(fstRec.getRadWind());
        arec.setRadWindQuad(fstRec.getRadWindQuad());
        arec.setStormDepth(fstRec.getStormDepth());
        arec.setStormSped(fstRec.getStormSped());
        arec.setStormDrct(fstRec.getStormDrct());
        arec.setSubRegion(fstRec.getSubRegion());
        arec.setUserData(fstRec.getUserData());
        arec.setUserDefined(fstRec.getUserDefined());
        arec.setWindMax(fstRec.getWindMax());

        return arec;
    }

    /*
     * Ideally, this would return an Instant, but not all APIs support java.time
     * classes yet.
     */
    private static Date parseDate(String text, DateTimeFormatter formatter) {
        return Date.from(
                LocalDateTime.parse(text, formatter).toInstant(ZoneOffset.UTC));
    }

    private static String formatCheckoutError(String itemType, Storm storm) {
        return String.format("Failed to check out %s for storm %s", itemType,
                storm.getStormId());
    }

    private static String formatRetrievalError(String itemType, Storm storm) {
        return formatRetrievalError(itemType, storm, null);
    }

    private static String formatRetrievalError(String itemType, Storm storm,
            int sandboxId) {
        return formatRetrievalError(itemType, storm, sandboxId > 0
                ? String.format("from sandbox %d", sandboxId) : null);
    }

    private static String formatRetrievalError(String itemType, Storm storm,
            String extra) {
        return String.format("Failed to retrieve %s for storm %s%s%s.",
                itemType, storm.getStormId(), extra != null ? " " : "",
                extra != null ? extra : "");
    }
}
