/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.rer;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Migration of RecordClimateRawData legacy adapt structure. Contains new and
 * old dates, the record-breaking element, and station ID.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * NOV 28 2016  21100      amoore      Initial creation
 * 22 FEB 2017  28609      amoore      Address TODOs.
 * </pre>
 * 
 * @author amoore
 *
 */
@DynamicSerialize
public class RecordClimateRawData {

    /**
     * RecordClimate.C record element constants.
     */
    /**
     * Temperature max raw text.
     */
    public static final String TEMP_MAX_RAW_TEXT = "temperature max";

    /**
     * High temperature report text (lower case).
     */
    public static final String HIGH_TEMP_REP_TEXT = "high temperature";

    /**
     * Temperature min raw text.
     */
    public static final String TEMP_MIN_RAW_TEXT = "temperature min";

    /**
     * Low temperature report text (lower case).
     */
    public static final String LOW_TEMP_REP_TEXT = "low temperature";

    /**
     * Precipitation max raw text.
     */
    public static final String PRECIP_MAX_RAW_TEXT = "precipitation max";

    /**
     * Daily max rain report text (lower case).
     */
    public static final String DAILY_MAX_RAIN_REP_TEXT = "daily maximum rainfall";

    /**
     * Shortened daily max rain report text (lower case).
     */
    public static final String DAILY_MAX_RAIN_SHORT_REP_TEXT = "rainfall";

    /**
     * Snow max raw text.
     */
    public static final String SNOW_MAX_RAW_TEXT = "snowfall max";

    /**
     * Daily max snow report text (lower case).
     */
    public static final String DAILY_MAX_SNOW_REP_TEXT = "daily maximum snowfall";

    /**
     * Shortened daily max snow report text (lower case).
     */
    public static final String DAILY_MAX_SNOW_SHORT_REP_TEXT = "snowfall";

    /**
     * Unknown record element report text (lower case).
     */
    public static final String UNKNOWN_REC_ELEM_REP_TEXT = "unknown record element";
    /**
     * End RecordClimate.C record element constants.
     */

    @DynamicSerializeElement
    private String validDate;

    @DynamicSerializeElement
    private String stationID;

    @DynamicSerializeElement
    private String recordElement;

    @DynamicSerializeElement
    private String newRecord;

    @DynamicSerializeElement
    private String oldRecord;

    @DynamicSerializeElement
    private String oldRecordDate;

    /**
     * Empty constructor.
     */
    public RecordClimateRawData() {
    }

    /**
     * Constructor.
     * 
     * @param validDate
     *            readable string of valid dates (could be more than one).
     * @param stationID
     *            station ID.
     * @param recordElement
     *            record element text that should be one of the "raw" text
     *            constants of this class.
     * @param newRecord
     *            new record value.
     * @param oldRecord
     *            old record value.
     * @param oldRecordDate
     *            readable string of old record dates (could be more than one).
     */
    public RecordClimateRawData(String validDate, String stationID,
            String recordElement, String newRecord, String oldRecord,
            String oldRecordDate) {
        this.validDate = validDate;
        this.stationID = stationID;
        this.recordElement = recordElement;
        this.newRecord = newRecord;
        this.oldRecord = oldRecord;
        this.oldRecordDate = oldRecordDate;
    }

    /**
     * @return the validDate
     */
    public String getValidDate() {
        return validDate;
    }

    /**
     * @param validDate
     *            the validDate to set
     */
    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }

    /**
     * @return the stationID
     */
    public String getStationID() {
        return stationID;
    }

    /**
     * @param stationID
     *            the stationID to set
     */
    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    /**
     * @return the recordElement
     */
    public String getRecordElement() {
        return recordElement;
    }

    /**
     * @param recordElement
     *            the recordElement to set
     */
    public void setRecordElement(String recordElement) {
        this.recordElement = recordElement;
    }

    /**
     * @return the newRecord
     */
    public String getNewRecord() {
        return newRecord;
    }

    /**
     * @param newRecord
     *            the newRecord to set
     */
    public void setNewRecord(String newRecord) {
        this.newRecord = newRecord;
    }

    /**
     * @return the oldRecord
     */
    public String getOldRecord() {
        return oldRecord;
    }

    /**
     * @param oldRecord
     *            the oldRecord to set
     */
    public void setOldRecord(String oldRecord) {
        this.oldRecord = oldRecord;
    }

    /**
     * @return the oldRecordDate
     */
    public String getOldRecordDate() {
        return oldRecordDate;
    }

    /**
     * @param oldRecordDate
     *            the oldRecordDate to set
     */
    public void setOldRecordDate(String oldRecordDate) {
        this.oldRecordDate = oldRecordDate;
    }

    /**
     * From RecordClimate.C#recordElement.
     * 
     * @return report text equivalent of this instance's record element string,
     *         or the unknown text. Returns in mixed case.
     */
    public String getRecordElementReportText() {
        switch (recordElement) {
        case TEMP_MAX_RAW_TEXT:
            return HIGH_TEMP_REP_TEXT;
        case TEMP_MIN_RAW_TEXT:
            return LOW_TEMP_REP_TEXT;
        case PRECIP_MAX_RAW_TEXT:
            return DAILY_MAX_RAIN_REP_TEXT;
        case SNOW_MAX_RAW_TEXT:
            return DAILY_MAX_SNOW_REP_TEXT;
        default:
            return UNKNOWN_REC_ELEM_REP_TEXT;
        }
    }

    /**
     * From RecordClimate.C#recordElementExpanded.
     * 
     * @return shortened report text equivalent of this instance's record
     *         element string, or the unknown text. Returns in mixed case.
     */
    public String getRecordElementReportShortenedText() {
        switch (recordElement) {
        case TEMP_MAX_RAW_TEXT:
            return HIGH_TEMP_REP_TEXT;
        case TEMP_MIN_RAW_TEXT:
            return LOW_TEMP_REP_TEXT;
        case PRECIP_MAX_RAW_TEXT:
            return DAILY_MAX_RAIN_SHORT_REP_TEXT;
        case SNOW_MAX_RAW_TEXT:
            return DAILY_MAX_SNOW_SHORT_REP_TEXT;
        default:
            return UNKNOWN_REC_ELEM_REP_TEXT;
        }
    }
}
