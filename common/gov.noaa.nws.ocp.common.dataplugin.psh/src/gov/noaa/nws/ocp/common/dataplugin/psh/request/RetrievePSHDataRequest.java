/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataRecord;

/**
 * RetrievePSHDataRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 04, 2017            pwang       Initial creation
 * Aug 10, 2017            pwang       Added getdataURI method
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class RetrievePSHDataRequest implements IServerRequest {

    @DynamicSerializeElement
    private String basin;

    @DynamicSerializeElement
    private int year;

    @DynamicSerializeElement
    private String stormName;

    
    /**
     * Empty Constructor
     */
    public RetrievePSHDataRequest() {
        
    }


    /**
     * Constructor
     * @param basin
     * @param year
     * @param stormName
     */
    public RetrievePSHDataRequest(String basin, int year, String stormName) {
        this.basin = basin;
        
        this.year = year;
        
        this.stormName = stormName;
    }
    
    /**
     * getDataURI for retrieving PSH data
     * @return
     * @throws Exception
     */
    public String getDataURI() throws Exception {
        StormDataRecord pdo = new StormDataRecord();
        pdo.setBasin(this.basin);
        pdo.setYear(this.year);
        pdo.setStormName(this.stormName);
        
        return pdo.getDataURI();
    }
    
    
    
    /**
     * @return the basin
     */
    public String getBasin() {
        return basin;
    }


    /**
     * @param basin the basin to set
     */
    public void setBasin(String basin) {
        this.basin = basin;
    }


    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }


    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }


    /**
     * @return the stormName
     */
    public String getStormName() {
        return stormName;
    }


    /**
     * @param stormName the stormName to set
     */
    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

}


