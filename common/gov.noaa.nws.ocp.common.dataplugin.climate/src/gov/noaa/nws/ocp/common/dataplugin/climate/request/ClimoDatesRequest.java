/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;

/**
 * For request service of climo_dates table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 09/27/2016  20639     wkwock      Initial creation
 * 10/07/2016  20639     wkwock      add GET_SNOWPRECIP
 * 10/14/2016  20639     wkwock      remove GET_SNOWPRECIP
 * 03/01/2018  44624     amoore      Remove unused functionality.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class ClimoDatesRequest implements IServerRequest {

    @DynamicSerializeElement
    private ClimateDates precipSeason;
    @DynamicSerializeElement
    private ClimateDates precipYear;
    @DynamicSerializeElement
    private ClimateDates snowSeason;
    @DynamicSerializeElement
    private ClimateDates snowYear;

    public ClimoDatesRequest() {
    }

    public ClimoDatesRequest(
            ClimateDates precipSeason, ClimateDates precipYear,
            ClimateDates snowSeason, ClimateDates snowYear) {
        this.precipSeason = precipSeason;
        this.precipYear = precipYear;
        this.snowSeason = snowSeason;
        this.snowYear = snowYear;
    }

    public ClimateDates getPrecipSeason() {
        return precipSeason;
    }

    public void setPrecipSeason(ClimateDates precipSeason) {
        this.precipSeason = precipSeason;
    }

    public ClimateDates getPrecipYear() {
        return precipYear;
    }

    public void setPrecipYear(ClimateDates precipYear) {
        this.precipYear = precipYear;
    }

    public ClimateDates getSnowSeason() {
        return snowSeason;
    }

    public void setSnowSeason(ClimateDates snowSeason) {
        this.snowSeason = snowSeason;
    }

    public ClimateDates getSnowYear() {
        return snowYear;
    }

    public void setSnowYear(ClimateDates snowYear) {
        this.snowYear = snowYear;
    }
}
