/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;

/**
 * CheckoutDeckRequest
 * 
 * To submit the request: basin, year, cycloneNum, and userId are required. the
 * stormName is not deterministic. Expected return is a sandboxId (Integer, the
 * value should > 0) The caller should check if returned value <= 0, if so, the
 * create a sandbox was failed
 * 
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2019 #60291     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CheckoutDeckRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private String stormName;

    @DynamicSerializeElement
    private String basin;

    @DynamicSerializeElement
    private int year;

    @DynamicSerializeElement
    private int cycloneNum;

    @DynamicSerializeElement
    private String userId;

    public CheckoutDeckRequest() {

    }

    public CheckoutDeckRequest(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the deckType
     */
    public AtcfDeckType getDeckType() {
        return deckType;
    }

    /**
     * @param deckType
     *            the deckType to set
     */
    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
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
