/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.psh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * A PSH station.
 *
 * This class holds the info of a single PSG station as in the legacy file
 * "official_stationinfo.txt" (METAR), "unofficial_stationinfo.txt" (non_METAR),
 * "marine_stationinfo.txt" (Marine), which is configured via the setup dialogs.
 *
 * Normally, a PSH station may have a node, station code, station name, state,
 * lat, and lon. But "node" is only required for METAR stations. Station code
 * and state may or not present for non-metar and marine stations and the user
 * may define a non-metar or marine station with simply a name and a pair of
 * lat/lon. So we use a "fullName" attribute to hold the full name (whatever the
 * user types in the GUI) and try to parse from the fullName to fill in
 * code/name/state info when appropriate.
 *
 * <pre>
 *  Sample station entry in official_stationinfo.txt
 *              {MIA}  {KBKV-BROOKSVILLE FL}  {28.47}  {-82.45}
 *
 *  Sample station entries in unofficial_stationinfo.txt
 *              {KVVG-THE VILLAGES FL}  {28.90}  {-82.00}
 *              {BALM FAWN}  {27.76}  {-82.22}
 *
 *  Sample station entries in marine_stationinfo.txt
 *              {CDRF1-CEDAR KEY-CMAN}  {29.14}  {-83.03}
 *              {PAG-PASS-A-GRILLE-COMPS}  {27.68}  {-82.77}
 *              {PORT OF TAMPA-PORTS}  {27.92}  {-82.43}
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 27 JUN 2017  #35269     jwu         Initial creation
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHStation")
@XmlAccessorType(XmlAccessType.NONE)
public class PshStation {

    /**
     * Node (only required for "METAR")
     */
    @DynamicSerializeElement
    @XmlElement(name = "Node")
    private String node;

    /**
     * Station's full name (including station code and state, e.g.,
     * KBKV-BROOKSVILLE FL)
     */
    @DynamicSerializeElement
    @XmlElement(name = "FullName")
    private String fullName;

    /**
     * Station latitude
     */
    @DynamicSerializeElement
    @XmlElement(name = "Lat")
    private double lat;

    /**
     * Station longitude
     */
    @DynamicSerializeElement
    @XmlElement(name = "Lon")
    private double lon;

    /**
     * Station code (e.g. "KBKV")
     */
    @DynamicSerializeElement
    @XmlElement(name = "Code")
    private String code;

    /**
     * Station simple name (e.g., "BROOKSVILLE")
     */
    @DynamicSerializeElement
    @XmlElement(name = "Name")
    private String name;

    /**
     * Station state
     */
    @DynamicSerializeElement
    @XmlElement(name = "State")
    private String state;

    /**
     * Constructor.
     */
    public PshStation() {
    }

    /**
     * Constructor.
     * 
     * @param node
     *            Station node
     * @param lat
     *            Station latitude
     * @param lon
     *            Station longitude
     * @param code
     *            Station code (e.g., "KBKV")
     * @param name
     *            Station's simple name (without station code and state as in
     *            fullName)
     * @param state
     *            Station's state (e.g., FL)
     */
    public PshStation(String node, double lat, double lon, String code,
            String name, String state) {

        this.node = node;
        this.lat = lat;
        this.lon = lon;
        this.code = code;
        this.name = name;
        this.state = state;

        this.buildFullName();
    }

    /**
     * @return the node
     */
    public String getNode() {
        return node;
    }

    /**
     * @param node
     *            the node to set
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName
     *            the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public double getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return a new object filled with default values.
     */
    public static PshStation getDefaultStation() {
        PshStation stn = new PshStation();
        stn.setDataToDefault();
        return stn;
    }

    /**
     * Set data to default values.
     */
    public void setDataToDefault() {

        this.node = "";
        this.fullName = "";
        this.lat = -9999.0;
        this.lon = -9999.0;
        this.code = "";
        this.name = "";
        this.state = "";
    }

    /**
     * Build a built from code/name/state in format of "code-name state".
     * 
     * Note: code or name could be empty, but not both.
     * 
     * @return full name
     */
    public void buildFullName() {

        String fname = "";
        if (code != null && !code.isEmpty()) {
            fname = code.trim();
        }

        if (name != null && !name.isEmpty()) {

            if (fname.length() > 0) {
                fname += "-";
            }
            fname += name.trim();
        }

        if (state != null && !state.isEmpty()) {
            fname = (fname + " " + state).trim();
        }

        this.fullName = fname;
    }

}