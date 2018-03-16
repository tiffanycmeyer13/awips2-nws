/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Request for to manually generate one climate product
 * 
 * The request should only be issued when user manually generate a climate
 * product EDEX plugin will 1) instantiate a new ClimateProdGenerateSession and
 * added into the session map Then call manualCreateClimate and return the
 * object of ClimateRunData to the caller
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016            pwang       Initial creation
 * MAR 21  2017 30166      amoore      Add non-recent run flag.
 *                                     Remove unneeded Time field.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ManualGenerateClimateProdRequest implements IServerRequest {

    @DynamicSerializeElement
    private PeriodType prodType;

    @DynamicSerializeElement
    private ClimateDate beginDate;

    @DynamicSerializeElement
    private ClimateDate endDate;

    @DynamicSerializeElement
    private boolean nonRecentRun;

    /**
     * Empty constructor.
     */
    public ManualGenerateClimateProdRequest() {
    }

    /**
     * Constructor.
     * 
     * @param prodType
     *            one of 1(AM), 2(PM), 10(IM), 5(Monthly), 7(Seasonal),
     *            9(Annual).
     */
    public ManualGenerateClimateProdRequest(PeriodType prodType) {
        this.prodType = prodType;
    }

    /**
     * @param prodType
     * @param beginDate
     * @param endDate
     * @param nonRecentRun
     */
    public ManualGenerateClimateProdRequest(PeriodType prodType,
            ClimateDate beginDate, ClimateDate endDate, boolean nonRecentRun) {
        this.prodType = prodType;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.nonRecentRun = nonRecentRun;
    }

    /**
     * @return the prodType
     */
    public PeriodType getProdType() {
        return prodType;
    }

    /**
     * @param prodType
     *            the prodType to set
     */
    public void setProdType(PeriodType prodType) {
        this.prodType = prodType;
    }

    /**
     * @return the beginDate
     */
    public ClimateDate getBeginDate() {
        return beginDate;
    }

    /**
     * @param beginDate
     *            the beginDate to set
     */
    public void setBeginDate(ClimateDate beginDate) {
        this.beginDate = beginDate;
    }

    /**
     * @return the endDate
     */
    public ClimateDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate(ClimateDate endDate) {
        this.endDate = endDate;
    }

    public boolean isNonRecentRun() {
        return nonRecentRun;
    }

    public void setNonRecentRun(boolean nonRecentRun) {
        this.nonRecentRun = nonRecentRun;
    }
}
