/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * class for the CWS/MIS configuration.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 06/02/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "CWSConfig")
public class CWSConfig extends AbstractCWAConfig {
    /** type */
    @XmlElement(name = "type")
    private String type;

    /** intensity */
    @XmlElement(name = "intst")
    private String intst;

    /** wind direction */
    @XmlElement(name = "dir")
    private String dir;

    /** wind speed */
    @XmlElement(name = "spd")
    private String spd;

    /** Tops from */
    @XmlElement(name = "topsFrom")
    private String topsFrom;

    /** Tops to */
    @XmlElement(name = "topsTo")
    private String topsTo;

    /** continue after # Z */
    @XmlElement(name = "contAfter")
    private int contAfter;

    /** state IDs */
    @XmlElement(name = "stateID")
    private String stateID;

    /**
     * constructor
     */
    public CWSConfig() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIntst() {
        return intst;
    }

    public void setIntst(String intst) {
        this.intst = intst;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getSpd() {
        return spd;
    }

    public void setSpd(String spd) {
        this.spd = spd;
    }

    public String getTopsFrom() {
        return topsFrom;
    }

    public void setTopsFrom(String topsFrom) {
        this.topsFrom = topsFrom;
    }

    public String getTopsTo() {
        return topsTo;
    }

    public void setTopsTo(String topsTo) {
        this.topsTo = topsTo;
    }

    public int getContAfter() {
        return contAfter;
    }

    public void setContAfter(int contAfter) {
        this.contAfter = contAfter;
    }

    public String getStateID() {
        return stateID;
    }

    public void setStateID(String stateID) {
        this.stateID = stateID;
    }
}
