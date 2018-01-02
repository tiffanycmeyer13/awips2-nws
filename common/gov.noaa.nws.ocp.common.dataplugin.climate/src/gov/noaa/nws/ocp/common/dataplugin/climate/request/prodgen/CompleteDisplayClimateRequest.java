/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import java.util.HashMap;
import java.util.Set;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;

/**
 * Request from the user when created climate report data: 1) User saves
 * modified or unmodified data and confirm it 2) If user rejects the data, they
 * should use an Abort option.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016            pwang       Initial creation
 * APR 04, 2017 30166      amoore      Additional data. Clean up.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CompleteDisplayClimateRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID;

    @DynamicSerializeElement
    private ClimateRunData userData;

    @DynamicSerializeElement
    private HashMap<Integer, ClimatePeriodReportData> originalDataMap;

    @DynamicSerializeElement
    private Set<Integer> msmOverwriteApproved;

    /**
     * Empty constructor.
     */
    public CompleteDisplayClimateRequest() {
    }

    /**
     * Daily constructor.
     * 
     * @param cpgSessionId
     *            session ID.
     * @param data
     *            potentially user-modified data for the report.
     */
    public CompleteDisplayClimateRequest(String cpgSessionId,
            ClimateRunDailyData data) {
        this.cpgSessionID = cpgSessionId;
        this.userData = data;
    }

    /**
     * Period constructor.
     * 
     * @param cpgSessionID
     *            session ID.
     * @param userData
     *            potentially user-modified data for the report.
     * @param originalDataMap
     *            original data, pre-modification. Used to potentially overwrite
     *            Daily data with MSM data.
     * @param msmOverwriteApproved
     *            set of station IDs approved for overwriting Daily data with
     *            MSM data.
     */
    public CompleteDisplayClimateRequest(String cpgSessionID,
            ClimateRunPeriodData userData,
            HashMap<Integer, ClimatePeriodReportData> originalDataMap,
            Set<Integer> msmOverwriteApproved) {
        this.cpgSessionID = cpgSessionID;
        this.userData = userData;
        this.originalDataMap = originalDataMap;
        this.msmOverwriteApproved = msmOverwriteApproved;
    }

    /**
     * @return the cpgSessionID
     */
    public String getCpgSessionID() {
        return cpgSessionID;
    }

    /**
     * @param cpgSessionID
     *            the cpgSessionID to set
     */
    public void setCpgSessionID(String cpgSessionID) {
        this.cpgSessionID = cpgSessionID;
    }

    /**
     * @return the userData
     */
    public ClimateRunData getUserData() {
        return userData;
    }

    /**
     * @param userData
     *            the userData to set
     */
    public void setUserData(ClimateRunData userData) {
        this.userData = userData;
    }

    public HashMap<Integer, ClimatePeriodReportData> getOriginalDataMap() {
        return originalDataMap;
    }

    public void setOriginalDataMap(
            HashMap<Integer, ClimatePeriodReportData> originalDataMap) {
        this.originalDataMap = originalDataMap;
    }

    public Set<Integer> getMsmOverwriteApproved() {
        return msmOverwriteApproved;
    }

    public void setMsmOverwriteApproved(Set<Integer> msmOverwriteApproved) {
        this.msmOverwriteApproved = msmOverwriteApproved;
    }
}
