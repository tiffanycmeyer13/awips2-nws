/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * DeleteStormRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2020 82622      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DeleteStormRequest implements IServerRequest {

    @DynamicSerializeElement
    private String basin;

    @DynamicSerializeElement
    private int cycloneNum;

    @DynamicSerializeElement
    private int year;

    public DeleteStormRequest() {

    }

    public DeleteStormRequest(String basin, int cycloneNum, int year) {
        this.basin = basin;
        this.cycloneNum = cycloneNum;
        this.year = year;
    }

    public String getBasin() {
        return basin;
    }

    public void setBasin(String basin) {
        this.basin = basin;
    }

    public int getCycloneNum() {
        return cycloneNum;
    }

    public void setCycloneNum(int cycloneNum) {
        this.cycloneNum = cycloneNum;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
