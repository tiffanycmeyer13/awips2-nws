/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;
import com.raytheon.uf.edex.database.query.DatabaseQuery;
import com.raytheon.uf.edex.database.query.QueryUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractGenesisDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseBDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ChangedSandboxDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictMergingRecordSet;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictRecordPair;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictSandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.DataChangeCode;
import gov.noaa.nws.ocp.common.dataplugin.atcf.DeckMergeLog;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisBDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisEDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ISandboxRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Sandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.SandboxForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfDataAccessException;
import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;
import org.springframework.cglib.beans.BeanMap;

/**
 * Data access object that primarily supports client requests to edit ATCF data.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2018            pwang       Initial creation
 * Aug 23, 2018 53502      dfriedman   Use Hibernate.
 * Aug 31, 2018 53950      jwu         Update checkin/checkout for B Deck.
 * Feb 20, 2019 60291      pwang       Added generic cin/cout for A, B, E and F decks
 * Mar 29, 2019 61590      dfriedman   Update sandbox records by ID.  Covert results
 *                                     to main table type.  Remove duplicate code.
 * May 13, 2019 63859      pwang       Added new dao function
 * Aug 05, 2019 66888      pwang       Add modifysandboxDeckRecord().
 * Aug 07, 2019 66999      jwu         Update checkin for A-deck.
 * Sep 12, 2019 #68237     dfriedman   Add getStormById.
 * Oct 16, 2019 69593      pwang       Add dao methods for FST
 * Jan 06, 2020 72989      pwang       Add/modify methods to handle FST update and delete
 * Apr 08, 2020 77134      pwang       Add dao methods for Genesis
 * May 28, 2020 78298      pwang       Add functions to handle deck record merging
 * Jun 10, 2020 78027      jwu         Add sandbox ID for add/delete forecast track records.
 * Aug 10, 2020 79571      wpaintsil   Revise promoteGenesisToStorm() query to include storm name.
 * Nov 09, 2020 84705      jwu         Fix deleteStorm().
 * Jun 25, 2021 92918      dfriedman   Refactor data classes.
 * Apr 21, 2022 8709       tjensen     Remove net.sf.cglib
 * </pre>
 *
 * @author pwang
 */

public class AtcfProcessDao extends CoreDao {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfProcessDao.class);

    private static final String ATCF_DB_NAME = "metadata";

    private static final String SANDBOX_TYPE_CHECKOUT = "CHECKOUT";

    /**
     * Constructor
     */
    public AtcfProcessDao() {
        super(DaoConfig.forDatabase(ATCF_DB_NAME));
    }

    /**
     * addNewStorm
     *
     * @param record
     * @return
     */
    public boolean addNewStorm(Storm srecord, List<BDeckRecord> bdeckRecords) {
        try {
            create(srecord);
            if (bdeckRecords != null) {
                for (BDeckRecord bdr : bdeckRecords) {
                    create(bdr);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(
                    "Failed to insert the Storm record into atcf.storm table"
                            + e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * updateExistStorm
     *
     * @param record
     * @return
     */
    public boolean updateExistStorm(Storm srecord) {
        try {
            saveOrUpdate(srecord);
            return true;
        } catch (Exception e) {
            logger.error(
                    "Failed to update the Storm record in the table atcf.Storm",
                    e);
            return false;
        }
    }

    /**
     * getStormList
     *
     * @param queryConditionParams
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Storm> getStormList(Map<String, Object> queryConditionParams)
            throws Exception {
        DatabaseQuery query = new DatabaseQuery(Storm.class);
        for (Entry<String, Object> entry : queryConditionParams.entrySet()) {
            query.addQueryParam(entry.getKey(), entry.getValue());
        }
        return (List<Storm>) queryByCriteria(query);
    }

    /**
     * @param stormId
     * @return
     */
    public Storm getStormById(String stormId) {
        return supplyInTransaction(
                () -> getCurrentSession().get(Storm.class, stormId));
    }

    /**
     * @param sandboxId
     * @return
     */
    public Sandbox getSandboxById(int sandboxId) {
        return supplyInTransaction(
                () -> getCurrentSession().get(Sandbox.class, sandboxId));
    }

    /**
     * @param queryConditionParams
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<Sandbox> getSandboxList(
            Map<String, Object> queryConditionParams) throws Exception {
        DatabaseQuery query = new DatabaseQuery(Sandbox.class);
        for (Map.Entry<String, Object> entry : queryConditionParams
                .entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                query.addQueryParam(entry.getKey(), value);
            } else {
                query.addQueryParam(entry.getKey(), null, QueryUtil.ISNULL);
            }
        }
        return (List<Sandbox>) queryByCriteria(query);
    }

    /**
     *
     * @param queryConditionParams
     * @param sandboxId
     * @param query
     */
    protected void addDeckQueryParams(Map<String, Object> queryConditionParams,
            int sandboxId, DatabaseQuery query) {
        for (Map.Entry<String, Object> entry : queryConditionParams
                .entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                query.addQueryParam(entry.getKey(), value);
            } else {
                query.addQueryParam(entry.getKey(), null, QueryUtil.ISNULL);
            }
        }

        // For sandbox, only retrieve all non-DELETE records.
        if (sandboxId > 0) {
            query.addQueryParam("sandbox.id", sandboxId);
            query.addQueryParam("changeCD", AbstractAtcfRecord.CHANG_CD_DELETE,
                    "!=");
        }
    }

    /**
     *
     * @param list
     * @param abstractClass
     * @param resultClass
     * @return
     * @throws Exception
     */
    private <T> List<T> convertTrackRecords(List<AbstractDeckRecord> list,
            Class<? extends AbstractDeckRecord> abstractClass,
            Class<T> resultClass) throws Exception {

        List<T> result = new ArrayList<>(list.size());
        BeanMap.Generator sandboxGenerator = new BeanMap.Generator();
        sandboxGenerator.setBeanClass(abstractClass);

        BeanMap.Generator targetGenerator = new BeanMap.Generator();
        targetGenerator.setBeanClass(resultClass);

        for (AbstractDeckRecord src : list) {
            T dst = resultClass.newInstance();
            targetGenerator.setBean(dst);

            BeanMap dstBM = targetGenerator.create();
            sandboxGenerator.setBean(src);
            dstBM.putAll(sandboxGenerator.create());

            result.add(dst);
        }

        return result;
    }

    /**
     * Query deck records of the given type from the ATCF database or a sandbox.
     * At least basin, year, cycloneNum need to be specified to ensure all deck
     * records are from one storm.
     *
     * @param queryConditionParams
     * @param sandboxId
     *            if > 0, the sandbox to query; if <= 0, query the main deck
     *            table.
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<? extends AbstractDeckRecord> getDeckList(AtcfDeckType deckType,
            Map<String, Object> queryConditionParams, int sandboxId)
            throws Exception {
        DatabaseQuery query = new DatabaseQuery(
                sandboxId <= 0 ? deckType.getMainRecordClass()
                        : deckType.getSandboxRecordClass());
        addDeckQueryParams(queryConditionParams, sandboxId, query);
        List<?> result = queryByCriteria(query);
        return sandboxId <= 0 ? (List<? extends AbstractDeckRecord>) result
                : convertTrackRecords((List<AbstractDeckRecord>) result,
                        deckType.getAbstractRecordClass(),
                        deckType.getMainRecordClass());
    }

    /**
     * get deck records when changeCd > 0
     *
     * @param deckType
     * @param sandboxId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, ChangedSandboxDeckRecord> getModifiedDeckRecords(
            AtcfDeckType deckType, int sandboxId) throws Exception {

        Map<Integer, ChangedSandboxDeckRecord> mrecs = new HashMap<>();

        DatabaseQuery query = new DatabaseQuery(
                deckType.getSandboxRecordClass());
        query.addQueryParam("sandbox.id", sandboxId);
        query.addQueryParam("changeCD", 0, ">");
        List<AbstractDeckRecord> result = (List<AbstractDeckRecord>) queryByCriteria(
                query);

        for (AbstractDeckRecord ar : result) {
            ISandboxRecord sdr = (ISandboxRecord) ar;
            mrecs.put(ar.getId(), new ChangedSandboxDeckRecord(ar.getId(),
                    sdr.getChangeCD(), ar));
        }
        return mrecs;
    }

    /**
     * @param deckType
     * @param stormName
     * @param basin
     * @param year
     * @param cycloneNum
     * @param userId
     * @return
     */
    public int checkOutDeckToEdit(AtcfDeckType deckType, String stormName,
            String basin, int year, int cycloneNum, String userId) {
        String scopeCD = deckType.getValue() + "DECK";
        String query = String.format(
                "SELECT atcf.checkout_%sdeck_to_sandbox(:id)",
                deckType.getValue().toLowerCase());
        Sandbox sandbox = new Sandbox();
        sandbox.setStormName(stormName);
        sandbox.setRegion(basin);
        sandbox.setYear(year);
        sandbox.setCycloneNum(cycloneNum);
        sandbox.setUserId(userId);
        sandbox.setScopeCd(scopeCD);
        sandbox.setSandboxType(SANDBOX_TYPE_CHECKOUT);

        if ("SAFEMODE".equals(sandbox.getSandboxType())) {
            sandbox.setValidFlag(2);
        }
        sandbox.setCreatedDT(TimeUtil.newCalendar(System.currentTimeMillis()));
        sandbox.setLastUpdated(sandbox.getCreatedDT());
        create(sandbox);

        int sboxId = sandbox.getId();
        Map<String, Object> params = new HashMap<>();
        params.put("id", sboxId);
        executeSQLQuery(query, params);
        return sboxId;
    }

    /**
     *
     * @param sandboxId
     * @param stormName
     * @param basin
     * @param year
     * @param cycloneNum
     * @param dtgYYYYMMDDHH
     * @param userId
     * @return
     */
    public int checkOutADeckDtgToEdit(int sandboxId, String stormName,
            String basin, int year, int cycloneNum, Calendar dtg,
            String userId) {

        int sboxId = sandboxId;

        String query = String
                .format("SELECT atcf.checkout_adeck_dtg_to_sandbox(:id, :dtg)");
        if (sandboxId < 0) {
            Sandbox sandbox = new Sandbox();
            sandbox.setStormName(stormName);
            sandbox.setRegion(basin);
            sandbox.setYear(year);
            sandbox.setCycloneNum(cycloneNum);
            sandbox.setUserId(userId);
            sandbox.setScopeCd("ADECK");
            sandbox.setSandboxType(SANDBOX_TYPE_CHECKOUT);

            if ("SAFEMODE".equals(sandbox.getSandboxType())) {
                sandbox.setValidFlag(2);
            }
            sandbox.setCreatedDT(
                    TimeUtil.newCalendar(System.currentTimeMillis()));
            sandbox.setLastUpdated(sandbox.getCreatedDT());
            create(sandbox);

            sboxId = sandbox.getId();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("id", sboxId);

        Timestamp ts = new Timestamp(dtg.getTime().getTime());
        params.put("dtg", ts);

        executeSQLQuery(query, params);
        return sboxId;
    }

    /**
     * Instantiate a new sandbox record instance
     *
     * @param deckType
     * @return
     * @throws Exception
     */
    private ISandboxRecord newSandboxRecordInstance(AtcfDeckType deckType)
            throws Exception {
        ISandboxRecord sandboxRecord = null;
        try {
            sandboxRecord = deckType.getSandboxRecordClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            String message = "Failed to instantiate a new instance of "
                    + deckType.getSandboxRecordClass().getSimpleName()
                    + " with error " + e.getMessage();
            logger.error(message);
            throw new AtcfDataAccessException(message, e);

        }
        return sandboxRecord;
    }

    public void addNewDeckRecordInSandbox(int sandboxId,
            AbstractAtcfRecord arecord) throws Exception {

        AtcfDeckType deckType = getTypeOfRecord(arecord);
        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(arecord));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));
        sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_NEW);
        create(sandboxRecord);

    }

    public void updateSandboxDeckRecord(int sandboxId,
            AbstractAtcfRecord record, int changeCode) throws Exception {
        AtcfDeckType deckType = getTypeOfRecord(record);
        ISandboxRecord sandboxRecord = deckType.getSandboxRecordClass()
                .newInstance();
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(record));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));
        sandboxRecord.setChangeCD(changeCode);
        saveOrUpdate(sandboxRecord);
    }

    public void saveModifiedDeckInSandbox(int sandboxId,
            AbstractDeckRecord arecord) throws Exception {
        updateSandboxDeckRecord(sandboxId, arecord,
                AbstractAtcfRecord.CHANG_CD_MODIFY);
    }

    public void markDeckInSandboxAsDeleted(int sandboxId,
            AbstractDeckRecord arecord) throws Exception {
        updateSandboxDeckRecord(sandboxId, arecord,
                AbstractAtcfRecord.CHANG_CD_DELETE);
    }

    /**
     * deleteRecordInSandbox
     *
     * Mark the record as delete based on current changeCD
     *
     * @param sandboxId
     * @param rc
     * @throws Exception
     */
    public void deleteRecordInSandbox(int sandboxId, int currentChangeCd,
            ModifiedDeckRecord rc) throws Exception {

        AtcfDeckType deckType = getTypeOfRecord(rc.getRecord());
        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(rc.getRecord()));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));

        if (currentChangeCd == AbstractAtcfRecord.CHANG_CD_UNCHANGE
                || currentChangeCd == AbstractAtcfRecord.CHANG_CD_MODIFY) {
            updateSandboxDeckRecord(sandboxId, rc.getRecord(),
                    AbstractAtcfRecord.CHANG_CD_DELETE);
        } else if (currentChangeCd == AbstractAtcfRecord.CHANG_CD_NEW) {
            undoModifiedRecord(sandboxId, rc.getRecord().getId());
        }
    }

    /**
     * modifySandboxDeckRecord User may modified a newly added, or modified and
     * a baseline record
     *
     * @param sandboxId
     * @param arecord
     * @param changeCode
     * @throws Exception
     */
    public void modifySandboxDeckRecord(int sandboxId,
            AbstractDeckRecord arecord, RecordEditType recType)
            throws Exception {

        AtcfDeckType deckType = getTypeOfRecord(arecord);
        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(arecord));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));

        /* conn't modify deleted record */
        if (sandboxRecord.getChangeCD() == AbstractAtcfRecord.CHANG_CD_DELETE) {
            throw new AtcfDataAccessException(
                    "Tried to modified a deleted record.");
        }

        if (sandboxRecord
                .getChangeCD() == AbstractAtcfRecord.CHANG_CD_UNCHANGE) {
            sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_MODIFY);
        }

        if (recType == RecordEditType.NEW) {
            sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_NEW);
        }

        saveOrUpdate(sandboxRecord);

    }

    /**
     * modifyDeckRecord
     *
     * Enable to modify any record in the sandbox except deleted record
     *
     * @param sandboxId
     * @param rc
     * @throws Exception
     */
    public void modifyDeckRecord(int sandboxId, int currentChangeCd,
            ModifiedDeckRecord rc) throws Exception {

        AtcfDeckType deckType = getTypeOfRecord(rc.getRecord());

        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(rc.getRecord()));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));

        /* conn't modify deleted record */
        if (currentChangeCd == AbstractAtcfRecord.CHANG_CD_DELETE) {
            throw new AtcfDataAccessException("Can't modify a deleted record.");
        } else if (currentChangeCd == AbstractAtcfRecord.CHANG_CD_UNCHANGE) {
            sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_MODIFY);
        } else if (currentChangeCd == AbstractAtcfRecord.CHANG_CD_NEW) {
            sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_NEW);
        } else if (currentChangeCd == AbstractAtcfRecord.CHANG_CD_MODIFY) {
            sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_MODIFY);
        }

        saveOrUpdate(sandboxRecord);

    }

    /**
     * This method will delete a record in sandbox if it is NEW, override it
     * with its baseline values if it exists in baseline, resets its delete flag
     * to 0 if it is marked as "DELETE".
     *
     * @param sandboxId
     * @param recordId
     * @throws Exception
     */
    public void undoModifiedRecord(int sandboxId, int recordId) {
        String query = String
                .format("SELECT atcf.undo_modified_record(:sboxid, :recid)");
        Map<String, Object> params = new HashMap<>();
        params.put("sboxid", sandboxId);
        params.put("recid", recordId);
        executeSQLQuery(query, params);
    }

    public void mergeNewDeckRecordInSandbox(int sandboxId,
            AbstractDeckRecord arecord) throws Exception {
        AtcfDeckType deckType = getTypeOfRecord(arecord);
        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(arecord));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));
        sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_UNCHANGE);
        create(sandboxRecord);
    }

    /**
     * Merge baseline updated / mixed record into target sandbox Baseline
     * record: CHANG_CD_UNCHANGE, Mixed record: CHANG_CD_MODIFY Keep sandbox
     * record: changeCode value will dependent upon: 1) [NEW, NEW] =>
     * CHANG_CD_MODIFY 2) [DELETE, MODIFIED] => CHANG_CD_NEW
     *
     * @param sandboxId
     * @param record
     * @param changeCode
     * @throws Exception
     */
    public void mergeUpdatedSandboxDeckRecord(int sandboxId,
            AbstractDeckRecord arecord, int changeCode) throws Exception {
        AtcfDeckType deckType = getTypeOfRecord(arecord);
        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(arecord));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));
        sandboxRecord.setChangeCD(changeCode);
        saveOrUpdate(sandboxRecord);
    }

    /**
     * Physically remove the record from the sandbox if baseline has deleted the
     * record
     *
     * @param sandboxId
     * @param record
     * @throws Exception
     */
    public void mergeDeletedDeckRecordInSandbox(int sandboxId,
            AbstractDeckRecord arecord) throws Exception {
        AtcfDeckType deckType = getTypeOfRecord(arecord);
        ISandboxRecord sandboxRecord = newSandboxRecordInstance(deckType);
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(arecord));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));
        sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_UNCHANGE);
        delete(sandboxRecord);
    }

    private AtcfDeckType getTypeOfRecord(AbstractAtcfRecord arecord) {
        for (AtcfDeckType deckType : AtcfDeckType.values()) {
            if (deckType.getMainRecordClass().isInstance(arecord)
                    || deckType.getSandboxRecordClass().isInstance(arecord)) {
                return deckType;
            }
        }
        throw new IllegalArgumentException(
                "Unknown deck record class " + arecord.getClass().getName());
    }

    public int submitModifiedDeckSandboxIntoBaseline(AtcfDeckType deckType,
            int sandboxId) {
        if (deckType == null) {
            throw new IllegalArgumentException("Deck type must not be null");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("id", sandboxId);
        int impactRecordCount = -1;
        String query = String.format(
                "SELECT atcf.checkin_%sdeck_from_sandbox(:id)",
                deckType.getValue().toLowerCase());
        Object[] results = executeSQLQuery(query, params);

        if (results.length == 1) {
            Object row = results[0];
            if (row instanceof Object[]) {
                impactRecordCount = ((Number) ((Object[]) row)[0]).intValue();
            } else if (row instanceof Number) {
                impactRecordCount = ((Number) row).intValue();
            }
        }
        return impactRecordCount;
    }

    /**
     * submitModifiedDeckSandbox return a list of ConflictSandbox
     *
     * @param deckType
     * @param sandboxId
     * @return
     */
    public List<ConflictSandbox> submitModifiedDeckSandbox(
            AtcfDeckType deckType, int sandboxId) {
        if (deckType == null) {
            throw new IllegalArgumentException("Deck type must not be null");
        }
        List<ConflictSandbox> conflictList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("id", sandboxId);

        String query;
        if (deckType == AtcfDeckType.A) {
            query = String.format(
                    "SELECT sid, dtg from atcf.checkin_%sdeck_from_sandbox(:id)",
                    deckType.getValue().toLowerCase());
        } else {
            query = String.format(
                    "SELECT sid from atcf.checkin_%sdeck_from_sandbox(:id)",
                    deckType.getValue().toLowerCase());
        }

        Object[] results = executeSQLQuery(query, params);

        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                ConflictSandbox rec = new ConflictSandbox();
                rec.setSandboxId((int) oa[0]);
                if (oa.length > 1 && oa[1] != null) {
                    rec.setDtg(timestampToCalendar((Timestamp) oa[1]));
                }
                conflictList.add(rec);
            }
        }

        return conflictList;
    }

    /**
    *
    * @param threshold
    * @return
    */
   private List<Integer> purgeSandboxes(Calendar threshold, String timeField) {
       List<Integer> purgedSboxList = new ArrayList<>();
       Map<String, Object> params = Collections.singletonMap("threshold", threshold);
       StringBuilder sb = new StringBuilder(
               "SELECT atcf.delete_deck_sandbox(id)");
       sb.append(" From atcf.sandbox");
       sb.append(" WHERE submitted IS NOT NULL");
       sb.append(" AND scopecd LIKE '%DECK%'");
       sb.append(String.format(" AND %s < :threshold", timeField));

       Object[] results = executeSQLQuery(sb.toString(), params);

       for (Object result : results) {
           int tag = rowSingleColumn(result, Number.class).intValue();
           if (tag >= 0) {
               purgedSboxList.add(tag);
           }
       }
       return purgedSboxList;
   }

   /**
    *
    * @param threshold
    * @return
    */
   public List<Integer> purgeSubmittedSandboxes(Calendar threshold) {
       return purgeSandboxes(threshold, "submitted");
   }

   /**
    *
    * @param threshold
    * @return
    */
   public List<Integer> purgeIdleSandboxes(Calendar threshold) {
       return purgeSandboxes(threshold, "lastupdated");
   }

    public ConflictMergingRecordSet checkSandboxMergeable(final int sandboxId) {
        ConflictMergingRecordSet cmrs = new ConflictMergingRecordSet();

        // check mergeable first
        Map<String, Object> params = new HashMap<>();
        params.put("id", sandboxId);

        int baselineSandboxId = -1;
        int changeCode = 0;
        String deck = "NO";
        int count = 0;

        String query = String.format(
                "SELECT scope, base_sbox_id, base_chg_cd, change_counts FROM atcf.check_sandbox_mergeable(:id)");
        Object[] results = executeSQLQuery(query, params);

        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                /* deck type and baseline sandboxId will have same value */
                deck = (String) oa[0];
                baselineSandboxId = (Integer) oa[1];

                changeCode = (Integer) oa[2];

                // number of NEW | UPDATED | DELETED
                count = (Integer) oa[3];

                if ("NO".equalsIgnoreCase(deck)) {
                    break;
                }
                cmrs.setTotal(DataChangeCode.valueOf(changeCode), count);
            }
        }
        cmrs.setBaselineId(baselineSandboxId);
        cmrs.setDeck(deck);
        return cmrs;
    }

    /**
     * checkConflictRecords Output: modified ConflictMergingRecordSet
     *
     * @param sandboxId
     * @param cmrs
     * @param baseline
     * @param mergingSandbox
     */
    public void checkConflictRecords(int sandboxId,
            ConflictMergingRecordSet cmrs,
            final Map<Integer, ChangedSandboxDeckRecord> baseline,
            final Map<Integer, ChangedSandboxDeckRecord> mergingSandbox)
            throws AtcfException {

        // Check conflicted records
        Map<String, Object> params = new HashMap<>();
        params.put("baselineId", cmrs.getBaselineId());
        params.put("sandboxId", sandboxId);
        params.put("scopeCd", cmrs.getDeck());

        String query2 = String.format(
                "SELECT conf_rec_id, base_sbox_id, tg_sbox_id, base_chg_cd, tg_chg_cd, scope FROM atcf.check_conflict_record(:baselineId, :sandboxId, :scopeCd)");
        Object[] results2 = executeSQLQuery(query2, params);

        for (Object result2 : results2) {
            if (result2 instanceof Object[]) {
                Object[] row = (Object[]) result2;
                ConflictRecordPair crp = new ConflictRecordPair();
                int recordId = (Integer) row[0];
                crp.setConflictRecordId(recordId);
                crp.setBaselineRecordChangeCode(
                        DataChangeCode.valueOf((Integer) row[3]));
                crp.setMergingRecordChangeCode(
                        DataChangeCode.valueOf((Integer) row[4]));
                crp.setBaselineRecord(
                        baseline.get(recordId).getModifiedRecord());
                crp.setMergingRecord(
                        mergingSandbox.get(recordId).getModifiedRecord());
                crp.findConflictedFields();

                cmrs.addConflictedRecord(crp);
            }
        }

    }

    /**
     *
     * @param sandboxId
     * @throws Exception
     */
    public int discardInvalidSandbox(int sandboxId) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("id", sandboxId);

        String query = "SELECT atcf.delete_deck_sandbox(:id)";
        Object[] results = executeSQLQuery(query, params);
        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                int deletedSandboxId = Integer.getInteger((String) oa[0]);
                if (deletedSandboxId != sandboxId) {
                    throw new AtcfDataAccessException("the sandbox " + sandboxId
                            + " wasn't deleted successfully");
                }
                return deletedSandboxId;
            }
        }
        return -1;

    }

    /**
     *
     * @param sandboxId
     * @throws Exception
     */
    public void updateSandboxLastupdated(int sandboxId) {
        Map<String, Object> params = new HashMap<>();
        params.put("sboxid", sandboxId);

        String query = "UPDATE atcf.sandbox SET lastupdated=NOW() WHERE id=:sboxid";
        executeSQLUpdate(query, params);
    }

    /**
     *
     * @param sandboxId
     * @throws Exception
     */
    public void validateSandbox(int sandboxId) {
        Map<String, Object> params = new HashMap<>();
        params.put("rid", sandboxId);

        String query = "UPDATE atcf.sandbox SET createddt=NOW(),lastupdated=NOW(), validflag=0 WHERE id=:rid;";
        executeSQLUpdate(query, params);
    }

    /**
     *
     * @param sandboxId
     * @throws Exception
     */
    public void invalidateSandbox(int sandboxId) {
        Map<String, Object> params = new HashMap<>();
        params.put("rid", sandboxId);

        String query = "UPDATE atcf.sandbox SET lastupdated=NOW(), validflag=1 WHERE id=:rid;";
        executeSQLUpdate(query, params);
    }

    /**
     * getCurrentChangeCD
     *
     * @param deckType
     * @param sandboxId
     * @param recIdList
     * @param changedOnly
     * @return
     */
    public Map<Integer, Integer> getCurrentChangeCD(AtcfDeckType deckType,
            int sandboxId, List<Integer> recIdList, boolean changedOnly) {
        if (deckType == null || sandboxId <= 0) {
            throw new IllegalArgumentException(
                    "sandboxId and deck type must be specified");
        }
        Map<Integer, Integer> changeCDs = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("sboxId", sandboxId);

        StringBuilder qsb = new StringBuilder(
                "SELECT id, change_cd FROM atcf.sandbox_%sdeck WHERE sandbox_id=:sboxId");
        if (recIdList != null && !recIdList.isEmpty()) {
            if (recIdList.size() == 1) {
                qsb.append(" AND id=").append(recIdList.get(0));
            } else if (recIdList.size() > 1) {
                qsb.append(" AND id IN(").append(recIdList.get(0));
                for (int i = 1; i < recIdList.size(); i++) {
                    qsb.append(",").append(recIdList.get(i));
                }
                qsb.append(")");
            }
        }
        if (changedOnly) {
            qsb.append(" AND change_cd>0");
        }
        qsb.append(" ORDER BY id");

        String query = String.format(qsb.toString(),
                deckType.getValue().toLowerCase());
        Object[] results = executeSQLQuery(query, params);

        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                changeCDs.put((Integer) oa[0], (Integer) oa[1]);
            }
        }
        return changeCDs;
    }

    /**
     *
     * @param queryConditionParams
     * @param sandboxId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<? extends AbstractDeckRecord> getForecastTrackRecordList(
            Map<String, Object> queryConditionParams, int sandboxId)
            throws Exception {
        DatabaseQuery query = new DatabaseQuery(sandboxId <= 0
                ? ForecastTrackRecord.class : SandboxForecastTrackRecord.class);
        addDeckQueryParams(queryConditionParams, sandboxId, query);
        List<?> result = queryByCriteria(query);
        return sandboxId <= 0 ? (List<? extends AbstractDeckRecord>) result
                : convertTrackRecords((List<AbstractDeckRecord>) result,
                        BaseBDeckRecord.class, ForecastTrackRecord.class);
    }

    /**
     *
     * @param basin
     * @param year
     * @param cycloneNum
     * @param stormName
     * @param userId
     * @throws Exception
     */
    public int createSandboxForecastTrack(String basin, int year,
            int cycloneNum, String stormName, String userId) {
        Sandbox sandbox = new Sandbox();
        sandbox.setStormName(stormName);
        sandbox.setRegion(basin);
        sandbox.setYear(year);
        sandbox.setCycloneNum(cycloneNum);
        sandbox.setUserId(userId);
        sandbox.setScopeCd("FST");
        sandbox.setSandboxType(SANDBOX_TYPE_CHECKOUT);

        sandbox.setCreatedDT(TimeUtil.newCalendar(System.currentTimeMillis()));
        sandbox.setLastUpdated(sandbox.getCreatedDT());
        create(sandbox);
        return sandbox.getId();
    }

    /**
     *
     * @param sandboxId
     * @param record
     * @throws Exception
     */
    public void addNewForecastTrackRecordInSandbox(int sandboxId,
            ForecastTrackRecord arecord) throws Exception {
        ISandboxRecord sandboxRecord = null;
        try {
            sandboxRecord = SandboxForecastTrackRecord.class.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            String message = "Failed to instantiate a new instance of "
                    + SandboxForecastTrackRecord.class.getName()
                    + " with error " + e.getMessage();
            logger.error(message);
            throw new AtcfDataAccessException(message, e);
        }
        BeanMap.create(sandboxRecord).putAll(BeanMap.create(arecord));
        sandboxRecord.setSandbox(new Sandbox(sandboxId));
        sandboxRecord.setChangeCD(AbstractAtcfRecord.CHANG_CD_NEW);

        // Delete the arecord if it exist in the sandbox
        this.deleteForecastTrackRecordInSandbox(sandboxId, arecord);

        // Store the record.
        saveOrUpdate(sandboxRecord);
    }

    /**
     *
     * @param sandboxId
     * @param record
     * @throws Exception
     */
    public void deleteForecastTrackRecordInSandbox(int sandboxId,
            ForecastTrackRecord arecord) throws Exception {
        deleteForecastTrackRecordsInSandbox(sandboxId, List.of(arecord));
    }

    /**
     * Delete the given forecast records from the given sandbox.
     *
     * @param sandboxId
     * @param records
     */
    public void deleteForecastTrackRecordsInSandbox(int sandboxId,
            List<ForecastTrackRecord> records) {

        if (sandboxId <= 0) {
            throw new IllegalArgumentException(
                    "A valid sandbox ID must be specified");
        }

        runInTransaction(() -> {
            for (ForecastTrackRecord rec : records) {
                DatabaseQuery query = new DatabaseQuery(
                        SandboxForecastTrackRecord.class);
                query.addQueryParam("sandbox.id", sandboxId);
                query.addQueryParam("basin", rec.getBasin());
                query.addQueryParam("year", rec.getYear());
                query.addQueryParam("cycloneNum", rec.getCycloneNum());
                query.addQueryParam("refTime", rec.getRefTime());
                query.addQueryParam("fcstHour", rec.getFcstHour());
                query.addQueryParam("radWind", rec.getRadWind());
                String queryString = query.createHQLDelete();
                Query hibQuery = getCurrentSession().createQuery(queryString);
                try {
                    query.populateHQLQuery(hibQuery, getSessionFactory());
                } catch (DataAccessLayerException e) {
                    throw new org.hibernate.TransactionException(
                            "Error populating delete statement", e);
                }
                hibQuery.executeUpdate();
            }
        });

    }

    /**
     *
     * @param sandboxId
     * @return
     * @throws Exception
     */
    public int submitForecastTrackSandboxIntoBaseline(int sandboxId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", sandboxId);
        int impactRecordCount = -1;
        String query = String
                .format("SELECT atcf.checkin_fst_from_sandbox(:id)");
        Object[] results = executeSQLQuery(query, params);

        if (results.length == 1) {
            Object row = results[0];
            if (row instanceof Object[]) {
                impactRecordCount = ((Number) ((Object[]) row)[0]).intValue();
            } else if (row instanceof Number) {
                impactRecordCount = ((Number) row).intValue();
            }
        }
        return impactRecordCount;
    }

    /**
     * addNewGenesis
     *
     * @param record
     * @return
     */
    public boolean addNewGenesis(Genesis grecord,
            List<GenesisBDeckRecord> gbdeckRecords) {
        try {
            create(grecord);

            if (gbdeckRecords != null) {
                for (GenesisBDeckRecord gbdr : gbdeckRecords) {
                    create(gbdr);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(
                    "Failed to create new Genesis record into atcf.genesis table"
                            + e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * getGenesisList
     *
     * @param queryConditionParams
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Genesis> getGenesisList(
            Map<String, Object> queryConditionParams) throws Exception {
        DatabaseQuery query = new DatabaseQuery(Genesis.class);
        for (Entry<String, Object> entry : queryConditionParams.entrySet()) {
            query.addQueryParam(entry.getKey(), entry.getValue());
        }
        return (List<Genesis>) queryByCriteria(query);
    }

    /**
     * promoteGenesisToStorm
     *
     * @param region
     * @param genesisNum
     * @param year
     * @param cycloneNum
     * @param stormName
     * @return
     * @throws Exception
     */
    public String promoteGenesisToStorm(String region, int genesisNum, int year,
            int cycloneNum, String stormName) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("region", region);
        params.put("genesisNum", genesisNum);
        params.put("year", year);
        params.put("cycloneNum", cycloneNum);
        params.put("stormName", stormName);

        String stormId = "";
        String query = String.format(
                "SELECT atcf.genesis_to_TC(:region, :year, :genesisNum, :cycloneNum, :stormName)");
        Object[] results = executeSQLQuery(query, params);

        if (results.length == 1) {
            Object row = results[0];
            if (row instanceof Object[]) {
                stormId = (String) ((Object[]) row)[0];
            } else if (row instanceof String) {
                stormId = (String) row;
            }
            // handle possible failures from genesis_to_TC
            if ("NO SUCH GENESIS".equals(stormId)
                    || "NO VALID GENESIS".equals(stormId)) {
                throw new AtcfDataAccessException("Genesis (" + genesisNum
                        + ") is not valid to a storm " + stormId);
            }
        }

        return stormId;
    }

    /**
     * getGenesisDeckRecordList
     *
     * @param deckType
     * @param queryConditionParams
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<? extends AbstractGenesisDeckRecord> getGenesisDeckRecordList(
            String deckType, Map<String, Object> queryConditionParams)
            throws Exception {
        DatabaseQuery query = new DatabaseQuery("B".equalsIgnoreCase(deckType)
                ? GenesisBDeckRecord.class : GenesisEDeckRecord.class);
        addDeckQueryParams(queryConditionParams, 0, query);
        List<?> result = queryByCriteria(query);
        return (List<? extends AbstractGenesisDeckRecord>) result;
    }

    /**
     * updateGenesis
     *
     * @param arecord
     * @return
     */
    public boolean updateGenesis(Genesis arecord) {
        try {
            saveOrUpdate(arecord);
            return true;
        } catch (Exception e) {
            logger.error(
                    "Failed to update the Storm record in the table atcf.Storm",
                    e);
            return false;
        }
    }

    /**
     *
     * @param deckType
     * @param basin
     * @param year
     * @param cyclonenum
     * @param minDTG
     * @param maxDTG
     * @return
     */
    public DeckMergeLog getMergeOverlapRange(AtcfDeckType deckType,
            String basin, int year, int cyclonenum, Date minDTG,
            Date maxDTG) {

        DeckMergeLog dml = new DeckMergeLog();
        dml.setBasin(basin);
        dml.setYear(year);
        dml.setCycloneNum(cyclonenum);
        dml.setDeckType(deckType.getValue());

        // Check conflicted records
        Map<String, Object> params = new HashMap<>();
        params.put("basin", basin);
        params.put("year", year);
        params.put("cyclonenum", cyclonenum);
        params.put("decktype", deckType.getValue());
        params.put("mindtg", minDTG);
        params.put("maxdtg", maxDTG);

        String query = String.format("SELECT max_id, begin_dtg, end_dtg "
                + "FROM atcf.get_overlap_dtgs(:basin, :year, :cyclonenum, :decktype, :mindtg, :maxdtg)");
        Object[] results2 = executeSQLQuery(query, params);

        for (Object result2 : results2) {
            if (result2 instanceof Object[]) {
                Object[] row = (Object[]) result2;
                Integer maxDeckId = (row[0] == null) ? 0 : (Integer) row[0];
                dml.setEndRecordId(maxDeckId);
                dml.setBeginDTG(timestampToCalendar((Timestamp) row[1]));
                dml.setEndDTG(timestampToCalendar((Timestamp) row[2]));
            }
        }
        return dml;
    }

    /**
     * backupOverlappedDeck
     *
     * @param dml
     * @return
     */
    public void backupOverlappedDeck(DeckMergeLog dml) {
        String scopeCD = dml.getDeckType() + "DECK";
        String query = String.format(
                "SELECT sid FROM atcf.backup_%sdeck_to_sandbox(:id, :begindtg, :enddtg)",
                dml.getDeckType().toLowerCase());
        Sandbox sandbox = new Sandbox();
        sandbox.setRegion(dml.getBasin());
        sandbox.setYear(dml.getYear());
        sandbox.setCycloneNum(dml.getCycloneNum());
        sandbox.setScopeCd(scopeCD);
        sandbox.setUserId("merger");
        sandbox.setSandboxType("BACKUP");
        sandbox.setCreatedDT(
                TimeUtil.newGmtCalendar(System.currentTimeMillis()));
        sandbox.setLastUpdated(sandbox.getCreatedDT());
        create(sandbox);

        int sboxId = sandbox.getId();
        dml.setSandboxId(sboxId);

        Map<String, Object> params = new HashMap<>();
        params.put("id", sboxId);
        params.put("begindtg", dml.getBeginDTG());
        params.put("enddtg", dml.getEndDTG());
        Object[] results2 = executeSQLQuery(query, params);
        for (Object result2 : results2) {
            if (result2 instanceof Object) {
                dml.addOneConflictSbox((Integer) result2);
            }
        }

    }

    /**
     *
     * @param deckType
     * @param dml
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public DeckMergeLog getLatestRecordId(AtcfDeckType deckType,
            DeckMergeLog dml) {

        String query = String.format(
                "SELECT max(id) FROM atcf.%sdeck WHERE basin=:basin AND year=:year AND cyclonenum=:cyclonenum",
                deckType.getValue().toLowerCase());
        Map<String, Object> params = new HashMap<>();
        params.put("basin", dml.getBasin());
        params.put("year", dml.getYear());
        params.put("cyclonenum", dml.getCycloneNum());
        Object[] results = executeSQLQuery(query, params);

        int latestRecordId = -1;
        if (results.length == 1) {
            Object row = results[0];
            if (row instanceof Integer) {
                latestRecordId = (Integer) row;
            }
        }
        dml.setNewEndRecordId(latestRecordId);
        return dml;
    }

    /**
     *
     * @param dml
     * @return
     */
    public int addNewDeckMergeLog(DeckMergeLog dml) {
        try {
            create(dml);
            // maintain the deck merge log to newest 3
            this.updateDeckMergeLog(dml);
            return dml.getId();
        } catch (Exception e) {
            logger.error(
                    "Failed to insert the DeckMergeLog record into deckmergelog table"
                            + e.getMessage(),
                    e);
            return 0;
        }
    }

    /**
     * updateDeckMergeLog This dao method should be called each new DeckMergeLog
     * added It keep newest three merge logs for possible rollback
     *
     * @param dml
     * @return a list of merge log IDs which valid for roll back
     * @throws Exception
     */
    public void updateDeckMergeLog(DeckMergeLog dml) {
        String query = "SELECT atcf.manage_deckmerge_log(:basin, :year, :cyclonenum, :decktype)";
        Map<String, Object> params = new HashMap<>();
        params.put("basin", dml.getBasin());
        params.put("year", dml.getYear());
        params.put("cyclonenum", dml.getCycloneNum());
        params.put("decktype", dml.getDeckType().toUpperCase());
        executeSQLQuery(query, params);
    }

    /**
     *
     * @param dmlId
     * @return
     */
    public DeckMergeLog getDeckMergeLogById(int dmlId) {
        return supplyInTransaction(
                () -> getCurrentSession().get(DeckMergeLog.class, dmlId));
    }

    /**
     *
     * @param deckType
     * @param basin
     * @param year
     * @param cycloneNum
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<DeckMergeLog> getAllDeckMergeLog(AtcfDeckType deckType,
            String basin, int year, int cycloneNum) throws Exception {
        DatabaseQuery query = new DatabaseQuery(DeckMergeLog.class);
        query.addQueryParam("basin", basin);
        query.addQueryParam("year", year);
        query.addQueryParam("cyclonenum", cycloneNum);
        query.addQueryParam("decktype", deckType.getValue().toUpperCase());
        query.addOrder("mergetime", false);

        return (List<DeckMergeLog>) queryByCriteria(query);
    }

    /**
     *
     * @param deckType
     * @param dmlId
     * @return
     */
    public List<ConflictSandbox> rollbackMergedDeck(AtcfDeckType deckType,
            int dmlId) {
        if (deckType == null) {
            throw new IllegalArgumentException("Deck type must not be null");
        }
        List<ConflictSandbox> conflictList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("id", dmlId);

        String query = String.format(
                "SELECT sid from atcf.fallback_merged_%sdeck(:id)",
                deckType.getValue().toLowerCase());

        Object[] results = executeSQLQuery(query, params);

        for (Object result : results) {
            int sboxid = -1;
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                sboxid = (Integer) oa[0];
            } else if (result instanceof Number) {
                sboxid = (Integer) result;
            }
            if (sboxid > 0) {
                ConflictSandbox rec = new ConflictSandbox();
                rec.setSandboxId(sboxid);
                conflictList.add(rec);
            }
        }

        return conflictList;
    }

    /**
     * Copy an existing storm to a new one with new storm name & new cycloneNum,
     * The basin & year stay the same. All deck records will also be copied.
     *
     * @param sourceStorm
     * @param newStorm
     * @return
     * @throws Exception
     */
    public String copyStorm(Storm sourceStorm, Storm newStorm)
            throws Exception {
        String msg = "FAILED to copy the given storm!";

        if (sourceStorm.getStormId() == null
                || sourceStorm.getStormId().isEmpty()) {
            throw new AtcfDataAccessException(
                    "No valid source Storm ID is provided!");
        }

        if ((newStorm.getRegion() == null || newStorm.getRegion().isEmpty())
                || (newStorm.getCycloneNum() < 1)) {
            throw new AtcfDataAccessException(
                    "No valid new Storm infomation is provided!");
        }

        String newRegion = newStorm.getRegion();
        int newYear = newStorm.getYear();
        int newCyclonenum = newStorm.getCycloneNum();
        String newStormName = newStorm.getStormName();

        String query = String.format(
                "SELECT atcf.copy_storm(:ostormid, :nbasin, :ncyclonenum, :nyear, :nstormname)");

        Map<String, Object> params = new HashMap<>();
        params.put("ostormid", sourceStorm.getStormId());
        params.put("nbasin", newRegion);
        params.put("nyear", newYear);
        params.put("ncyclonenum", newCyclonenum);
        params.put("nstormname", newStormName);
        Object[] results = executeSQLQuery(query, params);

        if (results.length == 1) {
            Object row = results[0];
            if (row instanceof String) {
                msg = (String) row;
            }
        }
        return msg;
    }

    /**
     * Update up to 6 fields for a existing storm If region and/or cyclonenum
     * changed, it will impact storm deck, fst and sandboxes If the storm name
     * changed, it will impact stor, A B decks and fast and sandboxes Other
     * fields changes may only impact storm record.
     *
     * @param sourceStorm
     * @param newStorm
     * @return
     * @throws Exception
     */
    public String changeStorm(Storm sourceStorm, Storm newStorm)
            throws Exception {
        String resultStormId = "";

        if (sourceStorm.getStormId() == null
                || sourceStorm.getStormId().isEmpty()) {
            throw new AtcfDataAccessException(
                    "No valid source Storm ID is provided!");
        }

        Map<String, Object> changeFields = sourceStorm
                .stormChangedFields(newStorm);

        String query = String.format(
                "SELECT atcf.update_storm(:ostormid, :nbasin, :ncyclonenum, :nstormname, :nstormstate, :nsubregion, :nmove, :nwtnum)");
        Map<String, Object> params = new HashMap<>();

        params.put("ostormid", sourceStorm.getStormId());
        params.put("nbasin", changeFields.get("region"));
        params.put("ncyclonenum", changeFields.get("cycloneNum"));
        params.put("nstormname", changeFields.get("stormName"));
        params.put("nstormstate", changeFields.get("stormState"));
        params.put("nsubregion", changeFields.get("subRegion"));
        params.put("nmove", changeFields.get("mover"));
        params.put("nwtnum", changeFields.get("wtNum"));
        Object[] results = executeSQLQuery(query, params);

        if (results.length == 1) {
            Object row = results[0];
            if (row instanceof String) {
                resultStormId = (String) row;
            }
        }
        return resultStormId;
    }

    /**
     *
     * @param s
     */
    public void endStorm(Storm s) {
        Map<String, Object> params = new HashMap<>();
        params.put("region", s.getRegion());
        params.put("cyclonenum", s.getCycloneNum());
        params.put("year", s.getYear());
        params.put("enddtg", s.getEndDTG());
        params.put("mover", s.getMover());

        String query = String.format(
                "UPDATE atcf.storm SET enddtg=:enddtg, mover=:mover WHERE region=:region AND cyclonenum=:cyclonenum AND year=:year");
        executeSQLUpdate(query, params);
    }

    /**
     * Delete Storm will not able to rollback, the callers need to ensure the
     * action
     *
     * @param basin
     * @param cyclonenum
     * @param year
     */
    public void deleteStorm(String basin, int cyclonenum, int year) {
        Map<String, Object> params = new HashMap<>();
        params.put("region", basin);
        params.put("cyclonenum", cyclonenum);
        params.put("year", year);
        String query = String.format(
                "SELECT 1 from atcf.delete_storm(:region, :cyclonenum, :year)");
        executeSQLQuery(query, params);
    }

    /**
     *
     * @param s
     */
    public void restartStorm(Storm s) {
        Map<String, Object> params = new HashMap<>();
        params.put("region", s.getRegion());
        params.put("cyclonenum", s.getCycloneNum());
        params.put("year", s.getYear());

        String query = String.format(
                "UPDATE atcf.storm SET enddtg=NULL WHERE region=:region AND cyclonenum=:cyclonenum AND year=:year");
        executeSQLUpdate(query, params);
    }

    /**
     * Update a e-deck entries, or create them if they don't exist
     *
     * @param records
     */
    public boolean updateGenesisEDeck(List<GenesisEDeckRecord> records) {
        try {
            records.forEach(this::saveOrUpdate);
            return true;
        } catch (Exception e) {
            logger.error(
                    "AtcfProcessDao.UpdateGenesisEDeck - Failed to update genesisedeck: \n"
                            + e.getMessage(),
                    e);
            return false;
        }

    }

    private static <T> T rowSingleColumn(Object row, Class<T> c) {
        if (row instanceof Object[]) {
            Object[] columns = (Object[]) row;
            if (columns.length == 1) {
                return c.cast(columns[0]);
            } else {
                throw new IllegalArgumentException(
                        "Epxected row with single column");
            }
        } else {
            return c.cast(row);
        }
    }

    private static Calendar timestampToCalendar(Timestamp ts) {
        return ts != null ? TimeUtil.newGmtCalendar(ts.getTime()) : null;
    }

}