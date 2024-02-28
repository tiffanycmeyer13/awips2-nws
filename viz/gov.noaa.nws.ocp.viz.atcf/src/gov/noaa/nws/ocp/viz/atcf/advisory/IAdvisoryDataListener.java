/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;

/**
 * Interface for data exchange between Advisory Composition dialog and Advisory
 * Data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 01, 2020 77847      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public interface IAdvisoryDataListener {

    /**
     * @return Current storm
     */
    public Storm getStorm();

    /**
     * @return Current DTG.
     */
    public String getCurrentDTG();

    /**
     * @return TAUs used.
     */
    public List<AtcfTaus> getWorkingTaus();

    /**
     * @return Special TAU hour if special advisory is on.
     */
    public int getSpecialTau();

    /**
     * @return ATCF resource
     */
    public AtcfResource getDrawingLayer();

    /**
     * @return Original forecast track records for current storm.
     */
    public Map<String, List<ForecastTrackRecord>> getFcstTrackData();

    /**
     * @return Working copy of forecast track records - may only contains info
     *         that will could be used or edited.
     */
    public Map<ForecastTrackRecord, ForecastTrackRecord> getFcstTrackEditMap();

    /**
     * @return Working copy of forecast track records sorted by RecordKey.
     */
    public Map<RecordKey, ForecastTrackRecord> getFcstTrackRecordMap();

    /**
     * @return Best track records for current storm.
     */
    public Map<String, List<BDeckRecord>> getCurrentBDeckRecords();

    /**
     * @return the fcstTrackSandBoxID
     */
    public int getFcstTrackSandBoxID();

    /**
     * This method is called to notify/pass data changes.
     * 
     * @param recMap
     *            Map<RecordKey, ForecastTrackRecord>
     */
    public void advisoryDataChanged(Map<RecordKey, ForecastTrackRecord> recMap,
            boolean update);

}