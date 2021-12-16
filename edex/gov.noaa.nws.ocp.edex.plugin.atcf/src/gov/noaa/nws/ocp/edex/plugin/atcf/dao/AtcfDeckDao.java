/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.raytheon.uf.common.dataquery.db.QueryParam.QueryOperand;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.database.query.DatabaseQuery;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractGenesisDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictSandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.DeckMergeLog;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Sandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfDataAccessException;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.NotifyUtil;

/**
 * Data access object that deals primarily with bulk ingest and merging of deck
 * data.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 30, 2018            pwang       Initial creation
 * Aug 23, 2018 #53502     dfriedman   Use Hibernate to store records.
 * Sep 12, 2019 #68237     dfriedman   Persist with replace semantics
 *                                     and handle duplicates.
 * May 12, 2020 #78298     pwang       Add functions to handle deck record merging.
 * Jul 6, 2020 # 79696     pwang       Ingest into genesis table when cyclonenum = 70 - 79
 * Jul  8, 2020 #78599     dfriedman   Add replaceADeckStormDTGsAndModels.
 * Jun 25, 2021 #92918     dfriedman   Refactor data classes.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class AtcfDeckDao extends CoreDao {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfDeckDao.class);

    private static final int MIN_BATCH_COUNT = 100;

    private static final int MAX_BATCH_COUNT = 102_400;

    /**
     * Creates a new AtcfDao
     *
     */
    public AtcfDeckDao() {
        super(DaoConfig.forClass(AbstractAtcfRecord.class));
    }

    /**
     * persistToDatabaseWithReplace
     *
     * @param records
     * @return
     */
    public boolean persistToDatabaseWithReplace(AtcfDeckType deckType,
            AbstractAtcfRecord... records) {
        try {
            replaceRecords(deckType, records);
            return true;
        } catch (Exception e) {
            logger.error("failed to persist records: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * mergeDeckToDatabase
     *
     * @param deckType
     * @param records
     * @return DeckMergeLog ID for the merge if successful, -1 otherwise
     */
    public int mergeDeckToDatabase(AtcfDeckType deckType,
            AbstractAtcfRecord... records) {
        try {
            return mergeRecords(deckType, records);

        } catch (Exception e) {
            logger.error("failed to merge records: " + e.getMessage(), e);
            return -1;
        }
    }

    protected void replaceRecords(AtcfDeckType deckType,
            AbstractAtcfRecord... records) throws Exception {
       AbstractDeckRecord r = records[0] instanceof AbstractDeckRecord
                ? (AbstractDeckRecord) records[0] : null;

        /*
         * Since the legacy ATCF not fully separate the genesis from storms, when the cycloneNum
         * is between 70 - 79, the storm actually is a genesis. In the A2 ATCF, the Genesis is
         * separated from storms, the deck records of Genesis (only B and E decks) are structurally
         * different from regular decks in the storm. The following logic return correct B and E decks
         */
        if (r != null && r.getCycloneNum() >= AbstractGenesisDeckRecord.MIN_GENESIS_CYCLONE_NUM
                && r.getCycloneNum() <= AbstractGenesisDeckRecord.MAX_GENESIS_CYCLONE_NUM
                && (deckType == AtcfDeckType.B || deckType == AtcfDeckType.E)) {
            AbstractAtcfRecord[] grecords = new AbstractAtcfRecord[records.length];
            try {
                for (int i = 0; i < records.length; i++) {
                    grecords[i] = ((AbstractDeckRecord) records[i])
                            .toGenesisDeckRecord();
                }
            } catch (Exception e) {
                logger.error(
                        "Failed to convert deck record into genesis record: ", e);
            }
            if (grecords[0] != null) {
                replaceGenesisDeckRecords(deckType, grecords);
            }
        } else {
            replaceDeckRecords(deckType, records);
        }
    }

    /**
     * replaceRecords
     *
     * @param deckType
     * @param records
     * @return
     */
    protected void replaceDeckRecords(AtcfDeckType deckType,
            AbstractAtcfRecord... records) throws Exception {
        /*
         * Assumption: * records are for a single storm and deck type and
         * inherit from AbstractDeckRecord
         */
        if (records.length < 1) {
            return;
        }
        AbstractDeckRecord r = (AbstractDeckRecord) records[0];
        AtcfProcessDao dao = new AtcfProcessDao();
        // Remove any sandboxes for the storm and deck.
        try {
            Map<String, Object> qp = new HashMap<>();
            qp.put("region", r.getBasin());
            qp.put("year", r.getYear());
            qp.put("cycloneNum", r.getCycloneNum());
            String scopeCD = "FST".equalsIgnoreCase(deckType.getValue()) ? "FST"
                    : deckType.getValue().toUpperCase() + "DECK";
            qp.put("scopeCd", scopeCD);
            // Does not discriminate on sandboxType as there is currently only
            // one type.
            for (Sandbox sandbox : dao.getSandboxList(qp)) {
                dao.discardInvalidSandbox(sandbox.getId());
            }
        } catch (Exception e) {
            logger.error("purging sandboxes for deck ingest: " + e.getMessage(),
                    e);
        }
        // Remove any deck records for the storm and deck.
        try {
            DatabaseQuery q = new DatabaseQuery(r.getClass());
            q.addQueryParam("basin", r.getBasin());
            q.addQueryParam("year", r.getYear());
            q.addQueryParam("cycloneNum", r.getCycloneNum());
            deleteByCriteria(q);
        } catch (Exception e) {
            logger.error("purging existing deck records for deck ingest: "
                    + e.getMessage(), e);
        }
        persistToDatabase(records, false);
    }

    protected void replaceGenesisDeckRecords(AtcfDeckType deckType,
            AbstractAtcfRecord... records) throws Exception {
        /*
         * Assumption: * records are for a single storm and deck type and
         * inherit from AbstractDeckRecord
         */
        if (records.length < 1) {
            return;
        }

        AbstractGenesisDeckRecord r = (AbstractGenesisDeckRecord) records[0];
        // Remove any deck records for the storm and deck.
        try {
            DatabaseQuery q = new DatabaseQuery(r.getClass());
            q.addQueryParam("basin", r.getBasin());
            q.addQueryParam("year", r.getYear());
            q.addQueryParam("genesisNum", r.getGenesisNum());
            deleteByCriteria(q);
        } catch (Exception e) {
            logger.error("purging existing deck records for deck ingest: "
                    + e.getMessage(), e);
        }
        persistToDatabase(records, false);
    }

    /**
     * mergeRecords
     *
     * @param deckType
     * @param records
     * @return
     */
    public int mergeRecords(AtcfDeckType deckType, AbstractAtcfRecord... records)
            throws Exception {
        /*
         * Assumption: * records are for a single storm and deck type and
         * inherit from AbstractDeckRecord
         */
        if (records.length < 1) {
            return -1;
        }

        // Backup deck for rollback
        DeckMergeLog dml = deckBackup(deckType, records);
        // merging
        mergeToDatabase(dml, records);
        return completeDeckMerge(dml, deckType);
    }

    /**
     * @param dml
     * @param deckType
     * @return
     * @throws Exception
     */
    protected int completeDeckMerge(DeckMergeLog dml, AtcfDeckType deckType)
            throws Exception {
        // Insert DeckMergeLog record
        AtcfProcessDao dao = new AtcfProcessDao();

        // get and set new deck record id
        dml = dao.getLatestRecordId(deckType, dml);
        dml.setMergeTime(Calendar.getInstance());

        // log the new deck merge info
        int dmlId = dao.addNewDeckMergeLog(dml);

        /*
         * Invalid the conflicted sandbox and Notify caller if there is any
         * conflict sandbox
         */
        if (dml.hasConflictSbox()) {
            Set<Integer> cbox = dml.getConflictSboxes();
            List<ConflictSandbox> cboxarr = new ArrayList<>();
            try {
                for (Integer i : cbox) {
                    cboxarr.add(new ConflictSandbox(i, null));
                    dao.invalidateSandbox(i);
                }
            } catch (Exception e) {
                throw new AtcfDataAccessException(
                        "Failed to invalidate conflicted sandbox: "
                                + e.getMessage(),
                        e);
            }
            AtcfDataChangeNotification notification = new AtcfDataChangeNotification(
                    new Date(), AtcfDeckType.valueOf(dml.getDeckType()), -1,
                    "awips",
                    cboxarr.toArray(new ConflictSandbox[cboxarr.size()]));
            NotifyUtil.sendNotifyMessage(notification);
        }
        return dmlId;
    }

    /**
     *
     * @param deckType
     * @param records
     * @return
     * @throws Exception
     */
    private DeckMergeLog deckBackup(AtcfDeckType deckType,
            AbstractAtcfRecord... records) {
        AbstractDeckRecord adr = (AbstractDeckRecord) records[0];
        Date minDtg = adr.getRefTime();
        Date maxDtg = adr.getRefTime();
        for (AbstractAtcfRecord r : records) {
            AbstractDeckRecord ar = (AbstractDeckRecord) r;
            if (ar.getRefTime().compareTo(minDtg) < 0) {
                minDtg = ar.getRefTime();
            }
            if (ar.getRefTime().compareTo(maxDtg) > 0) {
                maxDtg = ar.getRefTime();
            }
        }
        AtcfProcessDao dao = new AtcfProcessDao();

        // Check if merging deck overlap with existing deck
        DeckMergeLog dml = dao.getMergeOverlapRange(deckType, adr.getBasin(),
                adr.getYear(), adr.getCycloneNum(), minDtg, maxDtg);

        // if no overlapped DTGs, no need to backup the deck
        if (dml.getBeginDTG() != null && dml.getEndDTG() != null) {
            dao.backupOverlappedDeck(dml);
        } else {
            dml.setSandboxId(-1);
        }
        return dml;

    }

    /**
     * Replace records for a given storm, set of models and set of DTGs.
     * <p>
     * The storm is specified by the first element of {@code records}. Models
     * and DTGs are taken from all elements of {@code records}.
     *
     * @param records
     *            replacement records
     * @return the persisted records
     */
    public void replaceADeckStormDTGsAndModels(
            BaseADeckRecord... records) throws Exception {
        if (records.length < 1) {
            return;
        }
        AbstractDeckRecord r = records[0];

        HashSet<Date> dtgs = new HashSet<>();
        HashSet<String> models = new HashSet<>();
        for (BaseADeckRecord rec : records) {
            dtgs.add(rec.getRefTime());
            models.add(rec.getTechnique());
        }
        AtcfDeckType deckType = getTypeOfMainDeckRecord(records[0]);
        DeckMergeLog dml = deckBackup(deckType, records);
        try {
            DatabaseQuery q = new DatabaseQuery(r.getClass());
            q.addQueryParam("basin", r.getBasin());
            q.addQueryParam("year", r.getYear());
            q.addQueryParam("cycloneNum", r.getCycloneNum());
            q.addQueryParam("refTime", new ArrayList<>(dtgs), QueryOperand.IN);
            q.addQueryParam("technique", new ArrayList<>(models),
                    QueryOperand.IN);
            deleteByCriteria(q);
        } catch (Exception e) {
            logger.error("purging existing deck records for deck ingest: "
                    + e.getMessage(), e);
        }
        persistToDatabase(records, false);
        completeDeckMerge(dml, deckType);
    }

    /**
     * Return the AtcfDeckType of the given record which must be a main deck
     * record, not a sandbox deck record.
     *
     * @param record
     * @return
     */
    private AtcfDeckType getTypeOfMainDeckRecord(AbstractDeckRecord arecord) {
        for (AtcfDeckType deckType : AtcfDeckType.values()) {
            if (deckType.getMainRecordClass().isInstance(arecord)) {
                return deckType;
            }
        }
        throw new IllegalArgumentException(
                "Unknown deck record class " + arecord.getClass().getName());
    }

    /**
     * Based on PluginDao.persistToDatabase. Due to the large number of deck
     * records, need to make some changes to improve performance: Need to avoid
     * the duplicate checking for every record that can be triggered by too many
     * duplicates. Also need to use a larger batch size.
     *
     * @param records
     * @param overwriteOnConflict
     */
    public void persistToDatabase(AbstractAtcfRecord[] records, boolean overwriteOnConflict) {
        if (records == null || records.length == 0) {
            return;
        }
        List<AbstractAtcfRecord> objects = Arrays.asList(records);
        List<AbstractAtcfRecord> duplicates = new ArrayList<>();
        List<AbstractAtcfRecord> persisted = new ArrayList<>(records.length);
        Class<? extends AbstractAtcfRecord> recClass = objects.get(0).getClass();
        int batchCount = MAX_BATCH_COUNT;
        int nGoodBatches = 0;
        boolean supportsDupChecking = !objects.get(0).getUniqueId().isEmpty();

        Session session = null;
        try {
            session = getSession();
            // process them all in fixed sized batches.
            int thisBatch = 0;
            for (int i = 0; i < objects.size(); i += thisBatch) {
                List<AbstractAtcfRecord> subList = objects.subList(i,
                        Math.min(i + batchCount, objects.size()));
                List<AbstractAtcfRecord> subDuplicates = new ArrayList<>();
                boolean constraintViolation = false;
                Transaction tx = null;
                // First attempt is to just shove everything in the database
                // as fast as possible and assume no duplicates.
                try {
                    tx = session.beginTransaction();
                    for (AbstractAtcfRecord object : subList) {
                        if (object == null) {
                            continue;
                        }
                        session.save(object);
                    }
                    tx.commit();
                    persisted.addAll(subList);
                } catch (PersistenceException e) {
                    tx.rollback();
                    if (e.getCause() instanceof ConstraintViolationException) {
                        session.clear();
                        constraintViolation = true;
                    } else {
                        throw e;
                    }
                }
                if (constraintViolation && supportsDupChecking) {
                    nGoodBatches = 0;
                    thisBatch = MIN_BATCH_COUNT;
                    subList = objects.subList(i,
                            Math.min(i + thisBatch, objects.size()));
                    batchCount = Math.max(MIN_BATCH_COUNT, batchCount / 2);
                    /*
                     * Second attempt will do duplicate checking, Do this
                     * checking with the minimum batch size. Then process
                     * another batch normally with half the batch size.
                     */
                    constraintViolation = false;
                    try {
                        tx = session.beginTransaction();
                        List<AbstractAtcfRecord> subPersisted = new ArrayList<>(
                                subList.size());
                        for (AbstractAtcfRecord object : subList) {
                            if (object == null) {
                                continue;
                            }
                            Criteria criteria = session
                                    .createCriteria(recClass);
                            populateCriteria(object, criteria);
                            criteria.setProjection(Projections.id());
                            Integer id = (Integer) criteria.uniqueResult();
                            if (id != null) {
                                object.setId(id);
                                if (overwriteOnConflict) {
                                    session.update(object);
                                    subPersisted.add(object);
                                } else {
                                    subDuplicates.add(object);
                                }
                            } else {
                                session.save(object);
                                subPersisted.add(object);
                            }
                        }
                        tx.commit();
                        persisted.addAll(subPersisted);
                    } catch (PersistenceException e) {
                        tx.rollback();
                        if (e.getCause() instanceof ConstraintViolationException) {
                            constraintViolation = true;
                            session.clear();
                        } else {
                            throw e;
                        }
                    }
                } else {
                    /*
                     * If there have been two batches of this size without
                     * duplicates, double the batch size.
                     */
                    thisBatch = batchCount;
                    nGoodBatches++;
                    if (nGoodBatches >= 2) {
                        nGoodBatches = 0;
                        batchCount = Math.min(MAX_BATCH_COUNT, batchCount * 2);
                    }
                }
                if (constraintViolation) {
                    // Third attempt will commit each pdo individually.
                    subDuplicates.clear();
                    for (AbstractAtcfRecord object : subList) {
                        if (object == null) {
                            continue;
                        }
                        try {
                            tx = session.beginTransaction();
                            Integer id;
                            if (supportsDupChecking) {
                                Criteria criteria = session
                                        .createCriteria(recClass);
                                populateCriteria(object, criteria);
                                criteria.setProjection(Projections.id());
                                id = (Integer) criteria.uniqueResult();
                            } else {
                                id = null;
                            }
                            boolean add = true;
                            if (id != null) {
                                object.setId(id);
                                if (overwriteOnConflict) {
                                    session.update(object);
                                } else {
                                    subDuplicates.add(object);
                                    add = false;
                                }
                            } else {
                                session.save(object);
                            }
                            tx.commit();
                            if (add) {
                                persisted.add(object);
                            }
                        } catch (ConstraintViolationException e) {
                            tx.rollback();

                            String errorMessage = e.getMessage();
                            SQLException nextException = e.getSQLException()
                                    .getNextException();
                            if (nextException != null) {
                                errorMessage = nextException.getMessage();
                            }
                            /*
                             * Unique constraint violations do not need to be
                             * logged as an exception, they are fairly normal
                             * and are logged as just a count.
                             */
                            if (errorMessage.contains(" unique ")) {
                                subDuplicates.add(object);
                            } else {
                                logger.handle(Priority.PROBLEM,
                                        "Query failed: Unable to insert or update "
                                                + object.getUniqueId(),
                                        e);
                            }
                        }
                    }
                }
                if (!subDuplicates.isEmpty()) {
                    duplicates.addAll(subDuplicates);
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        if (!duplicates.isEmpty()) {
            logger.info("Discarded : " + duplicates.size() + " duplicates!");
            if (supportsDupChecking && logger.isPriorityEnabled(Priority.DEBUG)) {
                for (AbstractAtcfRecord rec : duplicates) {
                    logger.debug("Discarding duplicate: " + rec.getUniqueId());
                }
            }
        }
    }

    /*
     * Similar to PluginDao.populateDatauriCriteria, but uses
     * AbstractAtcfRecord.getUniqueId. Does not support compound fields,
     */
    private void populateCriteria(AbstractAtcfRecord rec, Criteria criteria) {
        for (Map.Entry<String, Object> entry: rec.getUniqueId().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                criteria.add(Restrictions.isNull(key));
            } else {
                criteria.add(Restrictions.eq(key, value));
            }
        }
    }

    /**
     * mergeToDatabase
     *
     * @param records
     * @return
     */
    public void mergeToDatabase(DeckMergeLog dml,
            AbstractAtcfRecord... records) {
        persistToDatabase(records, true);
    }

}
