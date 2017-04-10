/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;

/**
 * DisplayClimateResponse
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 10, 2017 33532      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DisplayClimateResponse {

    @DynamicSerializeElement
    private String cpgSessionId;

    @DynamicSerializeElement
    private ClimateGlobal globalConfig;

    @DynamicSerializeElement
    private ClimateCreatorResponse reportData = null;

    /**
     * Empty constructor
     */
    public DisplayClimateResponse() {

    }

    /**
     * Constructor
     * 
     * @param cpgSessionId
     */
    public DisplayClimateResponse(String cpgSessionId) {
        this.cpgSessionId = cpgSessionId;
    }

    /**
     * @return the cpgSessionId
     */
    public String getCpgSessionId() {
        return cpgSessionId;
    }

    /**
     * @param cpgSessionId
     *            the cpgSessionId to set
     */
    public void setCpgSessionId(String cpgSessionId) {
        this.cpgSessionId = cpgSessionId;
    }

    /**
     * @return the globalConfig
     */
    public ClimateGlobal getGlobalConfig() {
        return globalConfig;
    }

    /**
     * @param globalConfig
     *            the globalConfig to set
     */
    public void setGlobalConfig(ClimateGlobal globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * @return the reportData
     */
    public ClimateCreatorResponse getReportData() {
        return reportData;
    }

    /**
     * @param reportData
     *            the reportData to set
     */
    public void setReportData(ClimateCreatorResponse reportData) {
        this.reportData = reportData;
    }

}
