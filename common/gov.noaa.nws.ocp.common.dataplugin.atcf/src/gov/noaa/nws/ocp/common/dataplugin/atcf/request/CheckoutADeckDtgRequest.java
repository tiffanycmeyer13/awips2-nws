/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * CheckoutADeckDtgRequest
 *
 * To checkout DTG to a new sandbox, set sandboxId < 0 Set the sandboxId to a
 * valid ADECK sandbox to add more dtg.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CheckoutADeckDtgRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxId;

    @DynamicSerializeElement
    private String stormName;

    @DynamicSerializeElement
    private String basin;

    @DynamicSerializeElement
    private int year;

    @DynamicSerializeElement
    private int cycloneNum;

    @DynamicSerializeElement
    private Calendar  dtg;

    @DynamicSerializeElement
    private String userId;

    /**
     * @return the sandboxId
     */
    public int getSandboxId() {
        return sandboxId;
    }

    /**
     * @param sandboxId the sandboxId to set
     */
    public void setSandboxId(int sandboxId) {
        this.sandboxId = sandboxId;
    }

    /**
     * @return the dtg
     */
    public Calendar getDtg() {
        return dtg;
    }

    /**
     * @param dtg the dtg to set
     */
    public void setDtg(Calendar dtg) {
        this.dtg = dtg;
        this.dtg.set(Calendar.MINUTE, 0);
        this.dtg.set(Calendar.SECOND, 0);
        this.dtg.set(Calendar.MILLISECOND, 0);
    }

    /**
     * @return the stormName
     */
    public String getStormName() {
        return stormName;
    }

    /**
     * @param stormName
     *            the stormName to set
     */
    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

    /**
     * @return the basin
     */
    public String getBasin() {
        return basin;
    }

    /**
     * @param basin
     *            the basin to set
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
     * @param year
     *            the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the cycloneNum
     */
    public int getCycloneNum() {
        return cycloneNum;
    }

    /**
     * @param cycloneNum
     *            the cycloneNum to set
     */
    public void setCycloneNum(int cycloneNum) {
        this.cycloneNum = cycloneNum;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}

