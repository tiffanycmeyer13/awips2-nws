/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;

/**
 * GetDeckMergeLogListRequest To retrieve all available merge log for rollback
 * all 4 parameters are required to make this request
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2019  #78298    pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class GetDeckMergeLogListRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private String basin;

    @DynamicSerializeElement
    private int year;

    @DynamicSerializeElement
    private int cyclonenum;

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
     * @return the cyclonenum
     */
    public int getCyclonenum() {
        return cyclonenum;
    }

    /**
     * @param cyclonenum
     *            the cyclonenum to set
     */
    public void setCyclonenum(int cyclonenum) {
        this.cyclonenum = cyclonenum;
    }

}
