/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.display;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;

/**
 * Request for monthly build climate data (from Daily climate) for a stationID
 * between given dates (inclusive).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 27 JUL 2016  20414      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class DisplayMonthlyBuildClimateServiceRequest
        implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDates dates;

    /**
     * Empty constructor.
     */
    public DisplayMonthlyBuildClimateServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param iStationID
     *            station ID to search for.
     * @param iDates
     *            start/end dates (inclusive).
     */
    public DisplayMonthlyBuildClimateServiceRequest(int iStationID,
            ClimateDates iDates) {
        stationID = iStationID;
        dates = iDates;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int iStationID) {
        this.stationID = iStationID;
    }

    public ClimateDates getDates() {
        return dates;
    }

    public void setDates(ClimateDates iDates) {
        this.dates = iDates;
    }
}
