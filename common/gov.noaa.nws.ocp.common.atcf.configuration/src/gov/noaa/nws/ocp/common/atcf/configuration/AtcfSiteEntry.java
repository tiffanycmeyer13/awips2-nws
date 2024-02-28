/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represent a ATCF site definition entry, which is used to compose
 * advisory product.
 *
 * <pre>
 * Entry format:
 *
 * <Site id="NHC"  wfoId="KNHC" bureau="NWS"
 *       center="National Hurricane Center" city="Miami" state="FL"/>
 *
 * <Site id="CPHC" wfoId="PHFO" bureau="NWS"
 *       center="Central Pacific Hurricane Center" city="Honolulu" state="HI"/>
 *
 * <Site id="WPC"  wfoId="KWNH" bureau="NWS"
 *       center="Weather Prediction Center" city="College Park" state="MD"/>
 *
 *  where:
 *        id            - site dentifier
 *        wfoId         - WFO station code
 *        bureau    - Department where the forecast center belongs to.
 *        center        - Name of forecast center
 *        city          - City of forecast center
 *        state         - State  of forecast center
 *        issueByOffice - Description of the issuing office - combo of "super center city state"
 *
 * Note: The entries in atcfsites.xml are compiled from nhc_writeadv.f.
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2021 86476      jwu         Created
 * Mar 25, 2021 90014      dfriedman   Remove node field.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfSiteEntry {

    /**
     * ID - Site Identifier
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String id;

    /**
     * FWO station code for the site
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String wfoId;

    /**
     * Department for which this site belongs, i.e., "NWS".
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String bureau;

    /**
     * Full site name.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String center;

    /**
     * City the site locates.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String city;

    /**
     * State the site locates.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String state;

    /**
     * Description of the issuing office, a combo of bureau, center, city,
     * state.
     */
    private String issueByOffice;

    /**
     * Constructor.
     */
    public AtcfSiteEntry() {
    }

    /**
     * Constructor.
     *
     * @param id
     *            Site id
     * @param wfoId
     *            WFO station code
     * @param burean
     *            Department the site belongs to
     * @param center
     *            Site full name
     * @param city
     *            City the site locates
     * @param state
     *            State the site locates
     */
    public AtcfSiteEntry(String id, String wfoId, String bureau, String center,
            String city, String state) {
        this.id = id;
        this.wfoId = wfoId;
        this.bureau = bureau;
        this.center = center;
        this.city = city;
        this.state = state;
        this.issueByOffice = String
                .format("%s %s %s %s", bureau, center, city, state).trim();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the wfoId
     */
    public String getWfoId() {
        return wfoId;
    }

    /**
     * @param wfoId
     *            the wfoId to set
     */
    public void setWfoId(String wfoId) {
        this.wfoId = wfoId;
    }

    /**
     * @return the bureau
     */
    public String getBureau() {
        return bureau;
    }

    /**
     * @param bureau
     *            the bureau to set
     */
    public void setBureau(String bureau) {
        this.bureau = bureau;
    }

    /**
     * @return the center
     */
    public String getCenter() {
        return center;
    }

    /**
     * @param center
     *            the center to set
     */
    public void setCenter(String center) {
        this.center = center;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city
     *            the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the issueByOffice
     */
    public String getIssueByOffice() {
        this.issueByOffice = String
                .format("%s %s %s %s", bureau, center, city, state).trim();
        return issueByOffice;
    }

    /**
     * @param issueByOffice
     *            the issueByOffice to set
     */
    public void setIssueByOffice(String issueByOffice) {
        this.issueByOffice = issueByOffice;
    }

}