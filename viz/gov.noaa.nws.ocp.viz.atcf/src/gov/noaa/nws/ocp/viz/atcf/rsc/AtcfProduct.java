/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
*/
package gov.noaa.nws.ocp.viz.atcf.rsc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.EDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsProperties;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesProperties;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackProperties;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * Define a ATCF product that holds all ATCF data and drawing elements for one
 * storm.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 05, 2018 48178      jwu         Created.
 * Aug 21, 2018 53949      jwu         Store A-Deck record as map of DTG.
 * Dec 05, 2018 57484      jwu         Add DisplayFixesProperties.
 * Feb 19, 2019 60097      jwu         Add storm/sandboxID.
 * Feb 23, 2019 60613      jwu         Add support for best track options.
 * Apr 11, 2019 62487      jwu         Add ObjAidsProperties.
 * May 07, 2019 63005      jwu         Add sanbox ID for all decks.
 * Jun 06, 2019 63375      jwu         Add getter/setter F-Deck sandbox id.
 * Oct 29, 2019 69592      jwu         Added a layer for forecast track.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AtcfProduct extends Product {

    private static final String ATCF_PRODUCT = "Atcf Product";

    /**
     * Storm for this ATCF product.
     */
    private Storm storm;

    /**
     * ID for the sandboxes.
     */
    private Map<AtcfLayers, Integer> sandboxIDs;

    /**
     * Flag to indicate if this storm product has been accepted into this
     * resource.
     */
    private boolean accepted;

    /**
     * Data for this ATCF product.
     */
    private Map<String, List<ADeckRecord>> aDeckDataMap;

    private Map<String, List<BDeckRecord>> bDeckDataMap;

    private List<EDeckRecord> eDeckData;

    private List<FDeckRecord> fDeckData;

    private Map<String, List<ForecastTrackRecord>> fcstTrackDataMap;

    private List<AbstractAtcfRecord> ghostDeckData;

    /**
     * Display properties.
     */
    private ObjAidsProperties objAidsProperties;

    private DisplayFixesProperties displayFixesProperties;

    private BestTrackProperties bestTrackProperties;

    private FcstTrackProperties fcstTrackProperties;

    private AtcfDataChangeNotification currentNotification;

    /**
     * Define the layers for an ATCF product.
     */
    public enum AtcfLayers {
        A_DECK, B_DECK, E_DECK, F_DECK, FCST_TRACK, GHOST
    }

    /**
     * Constructor
     */
    public AtcfProduct() {
        super();
        setName(ATCF_PRODUCT);
        setType(ATCF_PRODUCT);

        this.storm = null;
        this.accepted = false;

        aDeckDataMap = new HashMap<>();
        bDeckDataMap = new LinkedHashMap<>();
        eDeckData = new ArrayList<>();
        fDeckData = new ArrayList<>();
        fcstTrackDataMap = new LinkedHashMap<>();
        ghostDeckData = new ArrayList<>();
        sandboxIDs = new EnumMap<>(AtcfLayers.class);

        // Add layers for this product
        for (AtcfLayers layer : AtcfLayers.values()) {
            Layer lyr = new Layer();
            lyr.setName(layer.name());
            addLayer(lyr);

            sandboxIDs.put(layer, -1);
        }
    }

    /**
     * Constructor for a given storm.
     *
     * @param storm
     */
    public AtcfProduct(Storm storm) {
        super();
        setName(storm.getStormId());
        setType(ATCF_PRODUCT);

        this.storm = storm;
        this.accepted = false;

        aDeckDataMap = new HashMap<>();
        bDeckDataMap = new LinkedHashMap<>();
        eDeckData = new ArrayList<>();
        fDeckData = new ArrayList<>();
        fcstTrackDataMap = new LinkedHashMap<>();
        ghostDeckData = new ArrayList<>();
        sandboxIDs = new EnumMap<>(AtcfLayers.class);

        // Add layers for this product
        for (AtcfLayers layer : AtcfLayers.values()) {
            Layer lyr = new Layer();
            lyr.setName(layer.name());
            addLayer(lyr);

            sandboxIDs.put(layer, -1);
        }
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    /**
     * @return the sandboxID for A-Deck
     */
    public int getAdeckSandboxID() {
        return sandboxIDs.get(AtcfLayers.A_DECK);
    }

    /**
     * @param sandboxID
     *            the sandboxID to set for A-Deck
     */
    public void setAdeckSandboxID(int sandboxID) {
        this.sandboxIDs.put(AtcfLayers.A_DECK, sandboxID);
    }

    /**
     * @return the sandboxID for B-Deck
     */
    public int getBdeckSandboxID() {
        return sandboxIDs.get(AtcfLayers.B_DECK);
    }

    /**
     * @param sandboxID
     *            the sandboxID to set for B-Deck
     */
    public void setBdeckSandboxID(int sandboxID) {
        this.sandboxIDs.put(AtcfLayers.B_DECK, sandboxID);
    }

    /**
     * @return the sandboxID for F-Deck
     */
    public int getFdeckSandboxID() {
        return sandboxIDs.get(AtcfLayers.F_DECK);
    }

    /**
     * @param sandboxID
     *            the sandboxID to set for F-Deck
     */
    public void setFdeckSandboxID(int sandboxID) {
        this.sandboxIDs.put(AtcfLayers.F_DECK, sandboxID);
    }

    /**
     * @return the sandboxID for forecast track data
     */
    public int getFcstTrackSandboxID() {
        return sandboxIDs.get(AtcfLayers.FCST_TRACK);
    }

    /**
     * @param sandboxID
     *            the sandboxID to set for B-Deck
     */
    public void setFcstTrackSandboxID(int sandboxID) {
        this.sandboxIDs.put(AtcfLayers.FCST_TRACK, sandboxID);
    }

    /**
     * @return the accepted
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * @param accepted
     *            the accepted to set
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * @param aDeckData
     *            the aDeckData to add
     */
    public void addADeckData(String dtg, List<ADeckRecord> aDeckData) {
        aDeckDataMap.put(dtg, aDeckData);
    }

    /**
     * @param aDeckData
     *            the aDeckData to get
     */
    public List<ADeckRecord> getADeckData(String dtg) {
        return aDeckDataMap.get(dtg);
    }

    /**
     * @return the aDeckDataMap
     */
    public Map<String, List<ADeckRecord>> getADeckDataMap() {
        return aDeckDataMap;
    }

    /**
     * @param aDeckDataMap
     *            the aDeckDataMap to set
     */
    public void setADeckDataMap(Map<String, List<ADeckRecord>> aDeckDataMap) {
        this.aDeckDataMap = aDeckDataMap;
    }

    /**
     * @return the bDeckDataMap
     */
    public Map<String, List<BDeckRecord>> getBDeckDataMap() {
        return bDeckDataMap;
    }

    /**
     * @param bDeckDataMap
     *            the bDeckDataMap to set
     */
    public void setBDeckData(Map<String, List<BDeckRecord>> bDeckDataMap) {
        this.bDeckDataMap = bDeckDataMap;
    }

    /**
     * @return the eDeckData
     */
    public List<EDeckRecord> getEDeckData() {
        return eDeckData;
    }

    /**
     * @param eDeckData
     *            the eDeckData to set
     */
    public void setEDeckData(List<EDeckRecord> eDeckData) {
        this.eDeckData = eDeckData;
    }

    /**
     * @return the fDeckData
     */
    public List<FDeckRecord> getFDeckData() {
        return fDeckData;
    }

    /**
     * @param fDeckData
     *            the fDeckData to set
     */
    public void setFDeckData(List<FDeckRecord> fDeckData) {
        this.fDeckData = fDeckData;
    }

    /**
     * @return the fcstTrackDataMap
     */
    public Map<String, List<ForecastTrackRecord>> getFcstTrackDataMap() {
        return fcstTrackDataMap;
    }

    /**
     * @param fcstTrackDataMap
     *            the fcstTrackDataMap to set
     */
    public void setFcstTrackDataMap(
            Map<String, List<ForecastTrackRecord>> fcstTrackDataMap) {
        this.fcstTrackDataMap = fcstTrackDataMap;
    }

    /**
     * @return the ghostDeckData
     */
    public List<AbstractAtcfRecord> getGhostDeckData() {
        return ghostDeckData;
    }

    /**
     * @param ghostDeckData
     *            the ghostDeckData to set
     */
    public void setGhostDeckData(List<AbstractAtcfRecord> ghostDeckData) {
        this.ghostDeckData = ghostDeckData;
    }

    /**
     * @return the objAidsProperties
     */
    public ObjAidsProperties getObjAidsProperties() {
        return objAidsProperties;
    }

    /**
     * @param objAidsProperties
     *            the objAidsProperties to set
     */
    public void setObjAidsProperties(ObjAidsProperties objAidsProperties) {
        this.objAidsProperties = objAidsProperties;
    }

    /**
     * @return the displayFixesProperties
     */
    public DisplayFixesProperties getDisplayFixesProperties() {
        return displayFixesProperties;
    }

    /**
     * @param displayFixesProperties
     *            the displayFixesProperties to set
     */
    public void setDisplayFixesProperties(
            DisplayFixesProperties displayFixesProperties) {
        this.displayFixesProperties = displayFixesProperties;
    }

    /**
     * @return the bestTrackProperties
     */
    public BestTrackProperties getBestTrackProperties() {
        return bestTrackProperties;
    }

    /**
     * @param bestTrackProperties
     *            the bestTrackProperties to set
     */
    public void setBestTrackProperties(
            BestTrackProperties bestTrackProperties) {
        this.bestTrackProperties = bestTrackProperties;
    }

    /**
     * @return the fcstTrackProperties
     */
    public FcstTrackProperties getFcstTrackProperties() {
        return fcstTrackProperties;
    }

    /**
     * @param fcstTrackProperties
     *            the fcstTrackProperties to set
     */
    public void setFcstTrackProperties(
            FcstTrackProperties fcstTrackProperties) {
        this.fcstTrackProperties = fcstTrackProperties;
    }

    /**
     * Convenience getter for layer A_DECK
     * 
     * @return Layer A_DECK Layer
     */
    public Layer getADeckLayer() {
        return getLayer(AtcfLayers.A_DECK.name());
    }

    /**
     * Convenience getter for layer B_DECK
     * 
     * @return Layer B_DECK Layer
     */
    public Layer getBDeckLayer() {
        return getLayer(AtcfLayers.B_DECK.name());
    }

    /**
     * Convenience getter for layer E_DECK
     * 
     * @return Layer E_DECK Layer
     */
    public Layer getEDeckLayer() {
        return getLayer(AtcfLayers.E_DECK.name());
    }

    /**
     * Convenience getter for layer F_DECK
     * 
     * @return Layer F_DECK Layer
     */
    public Layer getFDeckLayer() {
        return getLayer(AtcfLayers.F_DECK.name());
    }

    /**
     * Convenience getter for layer forecast track
     * 
     * @return Layer FCST_TRACK Layer
     */
    public Layer getForecastTrackLayer() {
        return getLayer(AtcfLayers.FCST_TRACK.name());
    }

    /**
     * Convenience getter for layer GHOST
     * 
     * @return Layer Ghost Layer
     */
    public Layer getGhostLayer() {
        return getLayer(AtcfLayers.GHOST.name());
    }

    /**
     * @return the currentNotification
     */
    public AtcfDataChangeNotification getCurrentNotification() {
        return currentNotification;
    }

    /**
     * @param currentNotification
     *            the currentNotification to set
     */
    public void setCurrentNotification(
            AtcfDataChangeNotification currentNotification) {
        this.currentNotification = currentNotification;
    }

}