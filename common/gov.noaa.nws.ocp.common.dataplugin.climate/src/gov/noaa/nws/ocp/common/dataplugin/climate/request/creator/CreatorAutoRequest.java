/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.creator;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Climate creator auto (cron/scheduled) request.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 21 NOV 2016  21378      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class CreatorAutoRequest implements IServerRequest {

    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * Empty constructor.
     */
    public CreatorAutoRequest() {

    }

    /**
     * 
     * @param iPeriodType
     */
    public CreatorAutoRequest(PeriodType iPeriodType) {
        periodType = iPeriodType;
    }

    /**
     * @return the periodType
     */
    public PeriodType getPeriodType() {
        return periodType;
    }

    /**
     * @param periodType
     *            the periodType to set
     */
    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }
}
