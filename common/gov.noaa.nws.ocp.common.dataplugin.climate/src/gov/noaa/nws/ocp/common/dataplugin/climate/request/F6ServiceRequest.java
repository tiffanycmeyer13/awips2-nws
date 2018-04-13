/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Service request for F6 data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 3, 2015             xzhang      Initial creation
 * 22 FEB 2017  28609      amoore      Add description.
 * 10 MAR 2017  30130      amoore      F6 should not keep output in awips directory, and should
 *                                     delete after printing. Send to textDB on EDEX side, not VIZ.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

@DynamicSerialize
public class F6ServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private List<Station> stations;

    @DynamicSerializeElement
    private ClimateDate adate;

    @DynamicSerializeElement
    private String remarks;

    @DynamicSerializeElement
    private boolean print;

    @DynamicSerializeElement
    private boolean operational;

    /**
     * @return the stations
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * @param stations
     *            the stations to set
     */
    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public ClimateDate getAdate() {
        return adate;
    }

    public void setAdate(ClimateDate adate) {
        this.adate = adate;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }
}
