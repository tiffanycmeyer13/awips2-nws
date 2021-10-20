/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * An Object for holding Aviation (ICAO) Advisory data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 02, 2020 71720      wpaintsil   Initial creation.
 * Feb 02, 2021 86746      jwu         Revised.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
@DynamicSerialize
public class AviationAdvisory extends AtcfAdvisory {

    /**
     * TCAC line - most time it is wfo id of the issuing center but may not
     * always be..
     */
    @DynamicSerializeElement
    private String issueCenter;

    /**
     * Dates/Times for advisory.
     */
    @DynamicSerializeElement
    private IcaoForecast tau3Fcst;

    /**
     * List of interpolated forecast at TAU 9(+6), 15(+12), 21(+18), 27 (+24).
     */
    @DynamicSerializeElement
    private List<IcaoForecast> interpFcst;

    /**
     * Constructor.
     */
    public AviationAdvisory() {
        super();

        setType(AdvisoryType.TCA);

        this.issueCenter = "KNHC";
        this.tau3Fcst = new IcaoForecast();
        this.interpFcst = new ArrayList<>();
    }

    /**
     * @return the issueCenter
     */
    public String getIssueCenter() {
        return issueCenter;
    }

    /**
     * @param issueCenter
     *            the issueCenter to set
     */
    public void setIssueCenter(String issueCenter) {
        this.issueCenter = issueCenter;
    }

    /**
     * @return the tau3Fcst
     */
    public IcaoForecast getTau3Fcst() {
        return tau3Fcst;
    }

    /**
     * @param tau3Fcst
     *            the tau3Fcst to set
     */
    public void setTau3Fcst(IcaoForecast tau3Fcst) {
        this.tau3Fcst = tau3Fcst;
    }

    /**
     * @return the interpFcst
     */
    public List<IcaoForecast> getInterpFcst() {
        return interpFcst;
    }

    /**
     * @param interpFcst
     *            the interpFcst to set
     */
    public void setInterpFcst(List<IcaoForecast> interpFcst) {
        this.interpFcst = interpFcst;
    }

}