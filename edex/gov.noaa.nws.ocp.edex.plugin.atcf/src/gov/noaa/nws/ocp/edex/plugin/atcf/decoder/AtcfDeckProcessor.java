/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.decoder;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.raytheon.edex.esb.Headers;
import com.raytheon.edex.exception.DecoderException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictSandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.util.DeckStormInfo;
import gov.noaa.nws.ocp.common.dataplugin.atcf.util.DeckUtil;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfDeckDao;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;
import gov.noaa.nws.ocp.edex.plugin.atcf.parser.AbstractAtcfParser;
import gov.noaa.nws.ocp.edex.plugin.atcf.parser.AtcfParserFactory;
import gov.noaa.nws.ocp.edex.plugin.atcf.parser.ForecastTrackParser;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.NotifyUtil;

/**
 * AtcfDeckProcessor will parse and decode a, b, e, f deck, and forecast track
 * files then persist the data records into tables accordingly
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Aug 23, 2018 #53502     dfriedman   Modify for Hibernate implementation.
 * Sep 12, 2019 #68237     dfriedman   Improve storm name handling.
 *                                     Allow "a2atcf." prefix in file name.
 * Oct 12, 2019 #69593     pwang       Add support for fst ingestion
 * May 12, 2020 #78298     pwang       Add functions to handle deck record merging.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class AtcfDeckProcessor {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AtcfDeckProcessor.class);

    public static final int MIN_USABLE_STORM_RANK = 1;

    public static final int MAX_STORM_NAME_RANK = 3;

    public static final String DECK_FILE_EXT = "dat";

    public static final String FST_FILE_EXT = "fst";

    private AtcfDeckType deckFileType = AtcfDeckType.T;

    private String extType = DECK_FILE_EXT;

    private AtcfProcessDao stormDao;

    private AtcfDeckDao deckDao;

    /**
     * Constructor validflag
     *
     * @throws DecoderException
     */
    public AtcfDeckProcessor() {
        // start StormDao
        stormDao = new AtcfProcessDao();
        deckDao = new AtcfDeckDao();
    }

    /**
     * decode called by ingest
     *
     * @param inputFile
     * @param headers
     * @throws DecoderException
     */
    public void decode(File inputFile, Headers headers)
            throws DecoderException {
        statusHandler
                .info("ATCF Decoder: start to decode " + inputFile.getName());

        // Parse the file name and extract storm information
        Storm s = null;
        try {
            DeckStormInfo dsi = DeckUtil
                    .inferStormFromDeckfileName(inputFile.getName());
            if (dsi.isStatus()) {
                s = dsi.getStorm();
                this.deckFileType = dsi.getDeckType();
                this.extType = dsi.getExtType();
            }
        } catch (Exception e) {
            throw new DecoderException("Failed to parse deck file name ", e);
        }

        AbstractAtcfRecord[] results = null;

        if (extType.equalsIgnoreCase(DECK_FILE_EXT)) {
            results = this.decodeDeckFile(inputFile, s);
        } else if (extType.equalsIgnoreCase(FST_FILE_EXT)) {
            results = this.decodeFstFile(inputFile, s);
        }

        if (results != null) {
            // persist records deck table
            statusHandler
                    .info("Attempting to store " + results.length + " records.");

            if (!deckDao.persistToDatabaseWithReplace(deckFileType, results)) {
                statusHandler.warn(
                        "Records may duplicated or failed to store in the atcf database.");
            }
        }
    }

    /**
     * mergeDeck for standard deck filename
     *
     * @param deckFilePath
     * @return id of new DeckMergeLog record
     * @throws DecoderException
     */
    public int mergeDeck(final String deckFilePath) throws DecoderException {
        if (!DeckUtil.isDeckfileAccessible(deckFilePath)) {
            throw new DecoderException(
                    "Deck file: " + deckFilePath + " is not accessible!");
        }
        File inputFile = new File(deckFilePath);

        // Parse the file name and extract storm information
        Storm s = null;
        try {
            DeckStormInfo dsi = DeckUtil
                    .inferStormFromDeckfileName(inputFile.getName());
            if (dsi.isStatus()) {
                s = dsi.getStorm();
                this.deckFileType = dsi.getDeckType();
                this.extType = dsi.getExtType();
            }
        } catch (Exception e) {
            String msg = "Failed to parse input file name, not a standard deck file name";
            statusHandler.info(msg);
            throw new DecoderException(msg, e);
        }

        return this.mergeDeck(this.deckFileType, inputFile, s);
    }

    /**
     * mergeDeck for deck with non-standard file name
     *
     * @param deckType
     * @param deckFilePath
     * @param s
     * @return id of new DeckMergeLog record
     * @throws DecoderException
     */
    public int mergeDeck(AtcfDeckType deckType, final String deckFilePath,
            Storm s) throws DecoderException {
        // deck type is required if the input filename format is not standard
        this.deckFileType = deckType;

        if (!DeckUtil.isDeckfileAccessible(deckFilePath)) {
            throw new DecoderException(
                    "Deck file: " + deckFilePath + " is not accessible!");
        }
        File inputFile = new File(deckFilePath);

        return this.mergeDeck(this.deckFileType, inputFile, s);
    }

    /**
     * mergeDeck
     *
     * @param deckType
     * @param inputFile
     * @param s
     * @return id of new DeckMergeLog record
     * @throws DecoderException
     */
    private int mergeDeck(AtcfDeckType deckType, File inputFile, Storm s)
            throws DecoderException {
        // deck type is required if the input filename format is not standard
        this.deckFileType = deckType;
        // decode deck file
        AbstractAtcfRecord[] results = this.decodeDeckFile(inputFile, s);

        // persist records deck table
        statusHandler
                .info("Attempting to merge " + results.length + " records.");
        int deckmergelogId = deckDao.mergeDeckToDatabase(deckFileType, results);
        if (deckmergelogId < 0) {
            statusHandler
                    .debug("Failed to merge records in the atcf database.");
        }
        return deckmergelogId;
    }

    /**
     *
     * @param deckType
     * @param mergedDeckLogId
     * @return
     * @throws Exception
     */
    public boolean rollbackMergedDeck(AtcfDeckType deckType,
            int mergedDeckLogId) throws DecoderException {
        boolean status = true;

        List<ConflictSandbox> csboxList = null;

        try {
            csboxList = stormDao.rollbackMergedDeck(deckType, mergedDeckLogId);
        } catch (Exception e) {
            String msg = "Failed to rollback the merged "
                    + deckType.getValue().toUpperCase() + " deck: "
                    + mergedDeckLogId;
            statusHandler.debug(msg);
            throw new DecoderException(msg, e);
        }

        // notify conflicted sandboxes
        if (!csboxList.isEmpty()) {
            AtcfDataChangeNotification notification = new AtcfDataChangeNotification(
                    new Date(), deckType, -1, "awips",
                    csboxList.toArray(new ConflictSandbox[csboxList.size()]));
            try {
                NotifyUtil.sendNotifyMessage(notification);
            } catch (Exception e) {
                statusHandler
                        .error("Failed to send the notification with error "
                                + e.getMessage(), e);
            }
        }

        return status;
    }

    /**
     * decodeDeckFile mergeDeck
     *
     * @param inputFile
     * @param s
     * @throws DecoderException
     */
    public AbstractAtcfRecord[] decodeDeckFile(File inputFile, Storm s)
            throws DecoderException {
        // Get an appreciate parser for the the type of ATCF data
        AbstractAtcfParser parser = AtcfParserFactory.getParser(deckFileType);

        // parse and return all records
        AbstractAtcfRecord[] results = parser.parse(inputFile, s);

        try {
            /*
             * Insert or update the storm table if the storm is not already
             * present or we have better name/subregion values.
             */

            // Need side effect of calling getStormId.
            s.getStormId();
            Storm curStorm = stormDao.getStormById(s.getStormId());
            if (curStorm == null) {
                statusHandler.info("Adding new storm " + s.getStormId()
                        + " with name " + s.getStormName());
                stormDao.addNewStorm(s, null);
            } else {
                boolean update = false;
                int newRank = stormNameRank(s.getStormName());
                int curRank = stormNameRank(curStorm.getStormName());
                if (newRank > curRank || (newRank >= curRank
                        && deckFileType == AtcfDeckType.B)) {
                    curStorm.setStormName(s.getStormName());
                    update = true;
                }
                if (curStorm.getSubRegion() != null
                        && !curStorm.getSubRegion().isEmpty()
                        && (s.getSubRegion() == null
                                || s.getSubRegion().isEmpty())) {
                    curStorm.setSubRegion(s.getSubRegion());
                    update = true;
                }
                if (update) {
                    statusHandler.info("Updating storm " + curStorm.getStormId()
                            + " with name " + curStorm.getStormName());
                    stormDao.updateExistStorm(curStorm);
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Adding or updating storm from parsed deck records: " + e,
                    e);
        }

        return results;
    }

    /**
     *
     * @param inputFile
     * @param s
     * @return
     * @throws DecoderException
     */
    private AbstractAtcfRecord[] decodeFstFile(File inputFile, Storm s)
            throws DecoderException {
        ForecastTrackParser parser = new ForecastTrackParser();

        // parse and return all records
        return parser.parse(inputFile, s);
    }

    /**
     * Rank the given storm name by suitability for describing a storm in the
     * storm table.
     *
     * @param stormName
     * @return
     */
    public static int stormNameRank(String stormName) {
        if (stormName == null) {
            return 0;
        }
        stormName = stormName.trim();
        if (stormName.isEmpty()) {
            return 0;
        }
        if (stormName.startsWith("GENESIS") || "INVEST".equals(stormName)
                || "TEST".equals(stormName)) {
            return 1;
            // TODO: else Use rank 2 for spelled out numbers?
        } else {
            return 3;
        }
    }

}
