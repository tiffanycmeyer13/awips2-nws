/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to hold all forecast intensity info for a given DTG/tech.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2020 71724      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ForecastInfo {

    // storm ID
    private String stormID;

    // storm name
    private String stormName;

    // DTG
    private String dtg;

    // technique name
    private List<String> aids;

    // Colors for displaying each aid.
    private Map<String, Color> colors;

    // Map of forecast intensity at TAU for each aid.
    private Map<String, Map<Integer, Integer>> intensity;

    /**
     * Constructor
     *
     * @param dtg,
     * @param tech,
     * @param clr
     * @param intenFcst
     */
    public ForecastInfo(String stormID, String stormName, String dtg) {

        this.stormID = stormID;
        this.stormName = stormName;
        this.dtg = dtg;
        this.aids = new ArrayList<>();
        this.colors = new HashMap<>();
        this.intensity = new LinkedHashMap<>();
    }

    /**
     * @return the stormID
     */
    public String getStormID() {
        return stormID;
    }

    /**
     * @param stormID
     *            the stormID to set
     */
    public void setStormID(String stormID) {
        this.stormID = stormID;
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
     * @return the dtg
     */
    public String getDtg() {
        return dtg;
    }

    /**
     * @param dtg
     *            the dtg to set
     */
    public void setDtg(String dtg) {
        this.dtg = dtg;
    }

    /**
     * @return the aids
     */
    public List<String> getAids() {
        return aids;
    }

    /**
     * @param aids
     *            the aids to set
     */
    public void setAids(List<String> aids) {
        this.aids = aids;
    }

    /**
     * @return the colors
     */
    public Map<String, Color> getColors() {
        return colors;
    }

    /**
     * @param colors
     *            the colors to set
     */
    public void setColors(Map<String, Color> colors) {
        this.colors = colors;
    }

    /**
     * @return the intensity
     */
    public Map<String, Map<Integer, Integer>> getIntensity() {
        return intensity;
    }

    /**
     * @param intensity
     *            the intensity to set
     */
    public void setIntensity(Map<String, Map<Integer, Integer>> intensity) {
        this.intensity = intensity;
    }

    /**
     * Add displaying color for an aid.
     *
     * @param aid
     */
    public void addColor(String aid, Color clr) {
        if (!aids.contains(aid)) {
            aids.add(aid);
        }
        colors.put(aid, clr);
    }

    /**
     * Add intensity for an aid at given TAU.
     *
     * @param aid
     */
    public void addIntensity(String aid, int fcstHr, int intenVal) {
        if (!aids.contains(aid)) {
            aids.add(aid);
        }

        Map<Integer, Integer> intenMap = intensity.computeIfAbsent(aid,
                k -> new LinkedHashMap<>());

        intenMap.put(fcstHr, intenVal);
    }

    /**
     * Get intensity map for an aid.
     *
     * @param aid
     */
    public Map<Integer, Integer> getIntensity(String aid) {
        return intensity.get(aid);
    }

}
