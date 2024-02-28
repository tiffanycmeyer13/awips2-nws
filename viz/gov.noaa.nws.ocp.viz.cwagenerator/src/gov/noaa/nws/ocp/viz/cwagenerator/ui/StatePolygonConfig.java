/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.util.ArrayList;

/**
 * class for CWA configuration list
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/12/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class StatePolygonConfig {
    private String stateID;

    private String stateName;

    // @XmlElements({ @XmlElement(name = "latlon", type = LatLonConfig.class) })
    private ArrayList<LatLonConfig> polygon;

    public StatePolygonConfig() {
    }

    public String getStateID() {
        return stateID;
    }

    public void setStateID(String stateID) {
        this.stateID = stateID;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public ArrayList<LatLonConfig> getPolygon() {
        return polygon;
    }

    public void setPolygon(ArrayList<LatLonConfig> polygon) {
        this.polygon = polygon;
    }
}
