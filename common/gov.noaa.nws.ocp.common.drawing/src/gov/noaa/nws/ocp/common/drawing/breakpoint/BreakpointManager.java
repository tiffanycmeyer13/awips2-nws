/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;

import com.raytheon.uf.common.dataquery.db.QueryResult;

import gov.noaa.nws.ocp.common.drawing.DrawingConfigManager;

/**
 * This singleton reads in tropical cyclone breakpoint information from various
 * sources, and stores the breakpoint information in several different forms
 * allowing for easy queries. The class also contains some utility methods
 * useful in combining or segmenting breakpoint geographies.
 *
 * Users can get a reference to this object using the static method
 * getInstance().
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public class BreakpointManager {

    /**
     * The singleton instance;
     */
    private static BreakpointManager instance = null;

    private static final double DIST = 1.0;

    private GeodeticCalculator gc;

    /*
     * The list contains BPGeography objects deserialized from XML files using
     * JAXB.
     */
    private CoastBreakpointList coasts = null;

    /*
     * Maps of Breakpoints with the breakpoint name as the key.
     */
    private Map<String, BreakpointSegment> coastMap = null;

    private Map<String, IslandBreakpoint> islandMap = null;

    private Map<String, WaterBreakpoint> waterwayMap = null;

    /*
     * Map used to associate breakpoint name (key) with its corresponding coast
     * name (value)
     */
    private Map<String, String> coastNameMap = null;

    /*
     * Map used to associate forecast zone (key) with its corresponding WFO id
     * (value)
     */
    private Map<String, String> zoneMap = null;

    /*
     * Quadtree indexes are used to store breakpoints for easy spatial query by
     * lat/lon Coordinate
     */
    private Quadtree coastTree = null;

    private Quadtree islandTree = null;

    private Quadtree waterwayTree = null;

    /**
     * constructor used by the getInstance method.
     */
    protected BreakpointManager() {

        gc = new GeodeticCalculator();

        // read in and store breakpoint segments along each coast
        initializeCoasts();

        // read in and store all Island breakpoints
        initializeIslands();

        // read in and store all waterway breakpoints
        initializeWaterways();

        // initialize forecast zone/WFO map
        initializeZoneMap();

    }

    /**
     * Static method used to request the instance of the BreakpointManager
     * object.
     *
     * @return reference to this object
     */
    public static synchronized BreakpointManager getInstance() {

        if (instance == null) {
            instance = new BreakpointManager();
        }
        return instance;

    }

    /*
     * read in and store breakpoint segments along each coast
     */
    private void initializeCoasts() {

        coasts = DrawingConfigManager.getInstance().getCoastBrkpts();
        coastNameMap = new HashMap<>();
        coastMap = new HashMap<>();
        coastTree = new Quadtree();
        for (CoastBreakpoint coast : coasts.getCoasts()) {

            /*
             * save coast breakpoints in a hash map so, they are easily accessed
             * by name.
             */
            for (BreakpointSegment seg : coast.getSegments()) {
                coastMap.put(seg.getBreakpoint().getName(), seg);
                coastNameMap.put(seg.getBreakpoint().getName(),
                        coast.getName());
            }

            /*
             * save coast breakpoints in a Quadtree for efficient spatial query
             */
            for (BreakpointSegment seg : coast.getSegments()) {
                Coordinate c = seg.getBreakpoint().getLocation();
                Envelope env = new Envelope(c);
                coastTree.insert(env, seg.getBreakpoint());
            }

        }

    }

    /**
     * read in and store Island breakpoints
     */
    private void initializeIslands() {

        IslandBreakpointList islands = DrawingConfigManager.getInstance()
                .getIslandBrkpts();
        /*
         * save island breakpoints in a hash map so, they are easily accessed by
         * name.
         */
        islandMap = new HashMap<>();
        for (IslandBreakpoint bkpt : islands.getIslands()) {
            islandMap.put(bkpt.getBreakpoint().getName(), bkpt);
        }

        /*
         * save island breakpoints in a Quadtree for efficient spatial query
         */
        islandTree = new Quadtree();
        for (IslandBreakpoint bkpt : islands.getIslands()) {
            Coordinate c = bkpt.getBreakpoint().getLocation();
            Envelope env = new Envelope(c.x - DIST, c.x + DIST, c.y - DIST,
                    c.y + DIST);
            islandTree.insert(env, bkpt);
        }

    }

    /**
     * read in and store Waterway breakpoints
     */
    private void initializeWaterways() {

        final double dist = 2.0;

        WaterBreakpointList waterways = DrawingConfigManager.getInstance()
                .getWaterBrkpts();

        /*
         * save waterway breakpoints in a hash map so, they are easily accessed
         * by name.
         */
        waterwayMap = new HashMap<>();
        for (WaterBreakpoint bkpt : waterways.getWaterways()) {
            waterwayMap.put(bkpt.getBreakpoint().getName(), bkpt);
        }

        /*
         * save waterway breakpoints in a Quadtree for efficient spatial query
         */
        waterwayTree = new Quadtree();
        for (WaterBreakpoint bkpt : waterways.getWaterways()) {
            Coordinate c = bkpt.getBreakpoint().getLocation();
            Envelope env = new Envelope(c.x - dist, c.x + dist, c.y - dist,
                    c.y + dist);
            waterwayTree.insert(env, bkpt);
        }

    }

    /**
     * Returns the nearest Island breakpoint geography for the given lat/lon
     * location
     *
     * @param loc
     *            lat/lon Coordinate
     * @return Island breakpoint
     */
    @SuppressWarnings("rawtypes")
    public BPGeography getNearestIsland(Coordinate loc) {

        double min = Double.MAX_VALUE;
        BPGeography found = null;
        gc.setStartingGeographicPoint(loc.x, loc.y);

        Envelope searchEnv = new Envelope(loc.x - DIST, loc.x + DIST,
                loc.y - DIST, loc.y + DIST);

        /*
         * query the appropriate quadtree index to get a list of nearby
         * breakpoints
         */
        List res = islandTree.query(searchEnv);
        Iterator iter = res.iterator();

        while (iter.hasNext()) {
            IslandBreakpoint ibkpt = (IslandBreakpoint) iter.next();
            Coordinate where = ibkpt.getBreakpoint().getLocation();
            gc.setDestinationGeographicPoint(where.x, where.y);
            double dist = gc.getOrthodromicDistance();
            if (dist < min) {
                min = dist;
                found = ibkpt;
            }

        }

        return found;
    }

    /**
     * Returns the nearest Waterway breakpoint geography for the given lat/lon
     * location
     *
     * @param loc
     *            lat/lon Coordinate
     * @return waterway breakpoint
     */
    @SuppressWarnings("rawtypes")
    public BPGeography getNearestWaterway(Coordinate loc) {

        double min = Double.MAX_VALUE;
        BPGeography found = null;
        gc.setStartingGeographicPoint(loc.x, loc.y);

        Envelope searchEnv = new Envelope(loc.x - DIST, loc.x + DIST,
                loc.y - DIST, loc.y + DIST);

        /*
         * query the appropriate quadtree index to get a list of nearby
         * breakpoints
         */
        List res = waterwayTree.query(searchEnv);
        Iterator iter = res.iterator();

        while (iter.hasNext()) {
            WaterBreakpoint ibkpt = (WaterBreakpoint) iter.next();
            Coordinate where = ibkpt.getBreakpoint().getLocation();
            gc.setDestinationGeographicPoint(where.x, where.y);
            double dist = gc.getOrthodromicDistance();
            if (dist < min) {
                min = dist;
                found = ibkpt;
            }

        }

        return found;
    }

    public Breakpoint getNearestBreakpoint(Coordinate loc) {
        return this.getNearestBreakpoint(loc, new BreakpointFilter());
    }

    @SuppressWarnings("rawtypes")
    public Breakpoint getNearestBreakpoint(Coordinate loc,
            BreakpointFilter filter) {

        double min = Double.MAX_VALUE;
        Breakpoint found = null;
        gc.setStartingGeographicPoint(loc.x, loc.y);

        Envelope searchEnv = new Envelope(loc);

        /*
         * query the appropriate quadtree index to get a list of nearby
         * breakpoints
         */
        List res = coastTree.query(searchEnv);
        Iterator iter = res.iterator();

        while (iter.hasNext()) {
            Breakpoint ibkpt = (Breakpoint) iter.next();
            if (!filter.isAccepted(ibkpt)) {
                continue;
            }
            Coordinate where = ibkpt.getLocation();
            gc.setDestinationGeographicPoint(where.x, where.y);
            double dist = gc.getOrthodromicDistance();
            if (dist < min) {
                min = dist;
                found = ibkpt;
            }

        }

        return found;
    }

    /**
     * Creates a BreakpointPair geography from two breakpoints on the same coast
     *
     * @param bkpt1
     * @param bkpt2
     * @return
     */
    public BreakpointPair getBreakpointPair(Breakpoint bkpt1,
            Breakpoint bkpt2) {

        BreakpointPair bp = null;

        // find which coast breakpoint is on
        String coastName = coastNameMap.get(bkpt1.getName());

        if (isCoastIsland(coastName)) {
            bp = getIslandPair(bkpt1, bkpt2);
        } else {
            bp = getCoastPair(bkpt1, bkpt2);
        }

        return bp;
    }

    /*
     * Creates a BreakpointPair geography from two breakpoints along a non
     * island Coast. Collects all the drawing points and land zones from all the
     * official breakpoints in between.
     */
    private BreakpointPair getCoastPair(Breakpoint bkpt1, Breakpoint bkpt2) {

        int first;
        int last;
        CoordinateList clist = new CoordinateList();

        BreakpointPair bp = new BreakpointPair();

        // Get a list of all breakpoints along this coast.
        String coastName = coastNameMap.get(bkpt1.getName());
        List<BreakpointSegment> seglist = coasts.getCoast(coastName);

        /*
         * find location of bkpt1 and bkpt2 in the list
         */
        BreakpointSegment seg1 = coastMap.get(bkpt1.getName());
        int index1 = seglist.indexOf(seg1);
        BreakpointSegment seg2 = coastMap.get(bkpt2.getName());
        int index2 = seglist.indexOf(seg2);
        if (index1 == index2) {
            return null;
        }

        /*
         * Reorder so that the first breakpoint in the list is always the first
         * breakpoint in the pair geography
         */
        if (index1 < index2) {
            first = index1;
            last = index2;
            bp.addBreakpoint(bkpt1);
            bp.addBreakpoint(bkpt2);
        } else {
            first = index2;
            last = index1;
            bp.addBreakpoint(bkpt2);
            bp.addBreakpoint(bkpt1);
        }

        /*
         * Add first breakpoint info
         */
        clist.add(seglist.get(first).getBreakpoint().getLocation(), true);
        if (!seglist.get(first).getPaths().isEmpty()) {
            clist.add(seglist.get(first).getPaths().get(0), true);
        }
        bp.addZones(seglist.get(first).getZones());

        /*
         * Add location of each official breakpoint between bkpt1 and bkpt2. Add
         * drawing points and land zones as well.
         */
        for (int j = first + 1; j < last; j++) {
            if (seglist.get(j).getBreakpoint().isOfficial()) {
                clist.add(seglist.get(j).getBreakpoint().getLocation(), true);
            }
            if (!seglist.get(j).getPaths().isEmpty()) {
                clist.add(seglist.get(j).getPaths().get(0), true);
            }
            bp.addZones(seglist.get(j).getZones());
        }

        // Add last breakpoint location
        clist.add(seglist.get(last).getBreakpoint().getLocation(), true);

        bp.addPath(clist.toCoordinateArray());

        return bp;
    }

    /*
     * Creates a BreakpointPair geography from two breakpoints along an island
     * Coast. The island coast breakpoints wrap around the island so traversing
     * the breakpoint list may need to wrap around to the beginning of the list.
     * Collects all the drawing points and land zones from all the official
     * breakpoints in between.
     */
    private BreakpointPair getIslandPair(Breakpoint bkpt1, Breakpoint bkpt2) {

        CoordinateList clist = new CoordinateList();

        BreakpointPair bp = new BreakpointPair();
        bp.addBreakpoint(bkpt1);
        bp.addBreakpoint(bkpt2);

        // Get a list of all breakpoints along this coast.
        String coastName = coastNameMap.get(bkpt1.getName());
        List<BreakpointSegment> seglist = coasts.getCoast(coastName);
        int n = seglist.size();

        /*
         * find location of bkpt1 and bkpt2 in the list
         */
        BreakpointSegment seg1 = coastMap.get(bkpt1.getName());
        int first = seglist.indexOf(seg1);
        BreakpointSegment seg2 = coastMap.get(bkpt2.getName());
        int last = seglist.indexOf(seg2);

        /*
         * Add first breakpoint info
         */
        clist.add(seglist.get(first).getBreakpoint().getLocation(), true);
        if (!seglist.get(first).getPaths().isEmpty()) {
            clist.add(seglist.get(first).getPaths().get(0), true);
        }
        bp.addZones(seglist.get(first).getZones());

        /*
         * Add location of each official breakpoint between bkpt1 and bkpt2.
         * Note, this may require wrapping around back to the beginning of the
         * breakpoint list. Add drawing points and land zones as well.
         */
        for (int j = (first + 1 + n) % n; j != last; j = (j + 1 + n) % n) {
            if (seglist.get(j).getBreakpoint().isOfficial()) {
                clist.add(seglist.get(j).getBreakpoint().getLocation(), true);
            }
            if (!seglist.get(j).getPaths().isEmpty()) {
                clist.add(seglist.get(j).getPaths().get(0), true);
            }
            bp.addZones(seglist.get(j).getZones());
        }

        clist.add(seglist.get(last).getBreakpoint().getLocation(), true);

        bp.addPath(clist.toCoordinateArray());

        return bp;
    }

    /**
     * Returns the name of the coast containing the given breakpoint
     *
     * @param bkpt
     * @return name of coast
     */
    public String findCoastName(Breakpoint bkpt) {
        return coastNameMap.get(bkpt.getName());
    }

    /**
     * Determines whether the given coast is an island or a non-circular coast
     * line
     *
     * @param name
     * @return
     */
    public boolean isCoastIsland(String name) {
        for (CoastBreakpoint coast : coasts.getCoasts()) {
            if (coast.getName().equals(name)) {
                return coast.isIsland();
            }
        }
        return false;
    }

    /**
     * Finds the US breakpoint closest to a US border between two given
     * breakpoints along a coast line
     *
     * @param bkpt1
     * @param bkpt2
     * @return breakpoint to use as the US Border
     */
    public Breakpoint findBorderPoint(Breakpoint bkpt1, Breakpoint bkpt2) {

        int first;
        int last;

        // Get a list of all breakpoints along this coast.
        String coastName = coastNameMap.get(bkpt1.getName());
        List<BreakpointSegment> seglist = coasts.getCoast(coastName);

        /*
         * find location of bkpt1 and bkpt2 in the list
         */
        BreakpointSegment seg1 = coastMap.get(bkpt1.getName());
        int index1 = seglist.indexOf(seg1);
        BreakpointSegment seg2 = coastMap.get(bkpt2.getName());
        int index2 = seglist.indexOf(seg2);

        // start w/ country not in US
        if ("US".equals(bkpt1.getCountry())) {
            first = index2;
            last = index1;
        } else {
            first = index1;
            last = index2;
        }

        int inc = 1;
        if (first > last) {
            inc = -1;
        }

        /*
         * find the first breakpoint whose country specifies "US"
         */
        Breakpoint border = null;
        for (int j = first; j != last; j += inc) {
            if ("US".equals(seglist.get(j).getBreakpoint().getCountry())) {
                border = seglist.get(j).getBreakpoint();
                break;
            }
        }

        return border;
    }

    /**
     * find a breakpoint given its name
     *
     * @param name
     *            name of the breakpoint
     * @return
     */
    public Breakpoint getBreakpoint(String name) {

        if (coastMap.containsKey(name)) {
            return coastMap.get(name).getBreakpoint();
        } else if (islandMap.containsKey(name)) {
            return islandMap.get(name).getBreakpoint();
        } else if (waterwayMap.containsKey(name)) {
            return waterwayMap.get(name).getBreakpoint();
        } else {
            return null;
        }

    }

    /*
     * Read land zone codes and corresponding WFO ids from EDEX database and the
     * info in a hashmap for later query.
     */
    private void initializeZoneMap() {
        zoneMap = getZoneMap();
    }

    /*
     * Read land zone codes and corresponding WFO ids from EDEX database and the
     * info in a hashmap for later query.
     */
    public Map<String, String> getZoneMap() {

        // query used to grab forecast zones from EDEX Database
        // TODO - find the table and move it if necessary - since this code may
        // be used for both ATCF and PGEN.
        String zoneQuery = "select state,zone,cwa from mapdata.zone;";

        Map<String, String> zoneMap = new HashMap<>();
        QueryResult results = null;

        /*-
        try {
            results = NcDirectDbQuery.executeMappedQuery(zoneQuery, "maps", QueryLanguage.SQL);
            QueryResultRow[] rows = results.getRows();
            Map<String, Integer> columns = results.getColumnNames();
            // System.out.println("column nmaes: "+columns.keySet().toString());
            for (QueryResultRow row : rows) {
                Object state = row.getColumn(columns.get("state"));
                Object zone = row.getColumn(columns.get("zone"));
                Object cwa = row.getColumn(columns.get("cwa"));
                if (state != null && zone != null && cwa != null) {
                    zoneMap.put(state.toString() + "Z" + zone.toString(), cwa.toString());
                }
            }
        } catch (VizException ve) {
            ve.printStackTrace();
        }
         */
        return zoneMap;
    }

    /**
     * Get WFO abbrev associated with the given a land zone
     */
    public String getCWA(String zone) {
        return zoneMap.get(zone);
    }

    /**
     * Determines whether the segments defined by two breakpoint pairs overlap
     *
     * @param thispair
     *            first breakpoint pair
     * @param thatpair
     *            second breakpoint pair
     * @return true, if segments overlap
     */
    public boolean pairsOverlap(BreakpointPair thispair,
            BreakpointPair thatpair) {

        Breakpoint thisbkpt1 = thispair.getBreakpoints().get(0);
        Breakpoint thisbkpt2 = thispair.getBreakpoints().get(1);
        Breakpoint thatbkpt1 = thatpair.getBreakpoints().get(0);
        Breakpoint thatbkpt2 = thatpair.getBreakpoints().get(1);

        /*
         * return false if the breakpoint pairs are on different coastlines
         */
        String coastName = findCoastName(thisbkpt1);
        if (!coastName.equals(findCoastName(thatbkpt1))) {
            return false;
        }

        /*
         * find the position of each breakpoint along the coast.
         */
        List<BreakpointSegment> seglist = coasts.getCoast(coastName);
        BreakpointSegment seg = coastMap.get(thisbkpt1.getName());
        int thisidx1 = seglist.indexOf(seg);
        seg = coastMap.get(thisbkpt2.getName());
        int thisidx2 = seglist.indexOf(seg);
        seg = coastMap.get(thatbkpt1.getName());
        int thatidx1 = seglist.indexOf(seg);
        seg = coastMap.get(thatbkpt2.getName());
        int thatidx2 = seglist.indexOf(seg);

        if ((thisidx1 < thisidx2) && (thisidx2 < thatidx1)
                && (thatidx1 < thatidx2)) {
            return false;
        }

        return !((thatidx1 < thatidx2) && (thatidx2 < thisidx1)
                && (thisidx1 < thisidx2));
    }

    /**
     * Finds the position of the given breakpoint in the list of breakpoints
     * defining its coastline
     *
     * @param bkpt
     *            Breakpoint to search for
     * @return index position of the breakpoint
     */
    public int coastIndexOf(Breakpoint bkpt) {

        String coastName = findCoastName(bkpt);
        List<BreakpointSegment> seglist = coasts.getCoast(coastName);
        BreakpointSegment seg = coastMap.get(bkpt.getName());
        return seglist.indexOf(seg);
    }

    /**
     * Creates a BreakpointPair geography from the specified index of two
     * breakpoints on the same coast
     *
     * @param coastName
     * @param index1
     *            index of first breakpoint
     * @param index2
     *            index of second breakpoint
     * @return a breakpoint pair geography
     */
    public BreakpointPair getBreakpointPair(String coastName, int index1,
            int index2) {

        BreakpointPair bp = null;

        List<BreakpointSegment> seglist = coasts.getCoast(coastName);
        Breakpoint bkpt1 = seglist.get(index1).getBreakpoint();
        Breakpoint bkpt2 = seglist.get(index2).getBreakpoint();

        if (isCoastIsland(coastName)) {
            bp = getIslandPair(bkpt1, bkpt2);
        } else {
            bp = getCoastPair(bkpt1, bkpt2);
        }

        return bp;
    }

}
