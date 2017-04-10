/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.asos;

import java.time.LocalDate;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Abstract class for Climate ASOS Messages DSM / MSM
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 14, 2016 16962      pwang       Initial creation
 * 25 JAN 2017  23221      amoore      Safer default value for times,
 *                                     the String "null". Correct naming
 *                                     of trace precip constant.
 * 05 MAY 2017  33104      amoore      Abstract methods for subclasses.
 *                                     Remove PDO extension.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ClimateASOSMessageRecord {

    public static final short MISSING_VAL_4_DIGITS = ParameterFormatClimate.MISSING;

    /**
     * Missing value for time. A null string.
     */
    public static final String MISSING_VAL_TIME = null;

    /**
     * Missing value for some String that is not represented as a time or date
     * object in the database. An empty string.
     */
    public static final String MISSING_VAL_STRING = "";

    public static final short MISSING_DEGREE_DAYS = MISSING_VAL_4_DIGITS * -1;

    /**
     * maps to YYYY field, Alphanumeric station identifier, 3 or 4 characters
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String stationCode;

    /**
     * Constructor.
     */
    public ClimateASOSMessageRecord() {
        stationCode = "";
    }

    /**
     * 
     * @param queryParams
     *            query map to fill out with parameters.
     * @return parameterized query to insert record for this object
     */
    public abstract String toInsertSQL(Map<String, Object> queryParams);

    /**
     * 
     * @param queryParams
     *            query map to fill out with parameters.
     * @return parameterized query to update the record for this object
     */
    public abstract String toUpdateSQL(Map<String, Object> queryParams);

    /**
     * 
     * @param queryParams
     *            query map to fill out with parameters.
     * @return parameterized query for existing records for this object's date
     *         and station
     */
    public abstract String queryExistingRecordSQL(
            Map<String, Object> queryParams);

    /**
     * DSM and MSM don't come with generation year. if the DMS or MSM generated
     * at the end of Calendar year and received at the beginning of new year,
     * the year should be corrected by minus 1
     * 
     * @param month
     * @return
     */
    protected short getAsosSummaryMessageGenerationYear(short month) {
        /*
         * DMS and MSM data is always use local Standard Date Time
         */
        LocalDate localDate = LocalDate.now();
        int gYear = localDate.getYear();
        short localMonth = (short) localDate.getMonthValue();

        /*
         * This correction may only happen when the data transmit to site
         * crossing the new year
         */
        if (month > localMonth) {
            gYear--;
        }
        return (short) gYear;
    }

    /**
     * @return the stationId
     */
    public final String getStationCode() {
        return stationCode;
    }

    /**
     * @param stationCode
     *            the stationCode to set
     */
    public final void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }
}
