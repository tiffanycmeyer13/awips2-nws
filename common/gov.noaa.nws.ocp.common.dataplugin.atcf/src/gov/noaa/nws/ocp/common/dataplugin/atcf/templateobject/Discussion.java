/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * An Object for holding Tropical Cyclone Discussion(TCD) data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2021 85386      wpaintsil   Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class Discussion extends AtcfAdvisory {

    /**
     * Previous TCD with first 7 line and forecast table removed.
     */
    @DynamicSerializeElement
    private String prevTCD;

    /**
     * Default constructor
     */
    public Discussion() {
        super();

        setType(AdvisoryType.TCD);
        this.prevTCD = "";
    }

    /**
     * @return the prevTCD
     */
    public String getPrevTCD() {
        return prevTCD;
    }

    /**
     * @param prevTCD
     *            the prevTCD to set
     */
    public void setPrevTCD(String prevTCD) {
        this.prevTCD = prevTCD;
    }

}