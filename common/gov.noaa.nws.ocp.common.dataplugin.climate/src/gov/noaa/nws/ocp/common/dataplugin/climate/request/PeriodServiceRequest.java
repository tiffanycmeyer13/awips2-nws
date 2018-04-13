/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Request for period data from monthly/seasonal/annual table for a stationID
 * between given dates (inclusive).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 SEP 2016  20636      wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class PeriodServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDates dates;

    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * Empty constructor.
     */
    public PeriodServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param iStationID
     *            station ID to search for.
     * @param iPeriodType
     *            period type (monthly/seasonal/annual)
     * @param iDates
     *            start/end dates (inclusive).
     */
    public PeriodServiceRequest(int iStationID, PeriodType iPeriodType,
            ClimateDates iDates) {
        stationID = iStationID;
        periodType = iPeriodType;
        dates = iDates;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int iStationID) {
        this.stationID = iStationID;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType iPeriodType) {
        this.periodType = iPeriodType;
    }

    public ClimateDates getDates() {
        return dates;
    }

    public void setDates(ClimateDates iDates) {
        this.dates = iDates;
    }
}
