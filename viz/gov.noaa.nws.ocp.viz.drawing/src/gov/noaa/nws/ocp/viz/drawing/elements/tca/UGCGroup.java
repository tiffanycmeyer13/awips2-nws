/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a Universal Geographic Code (UGC) group consisting of a set of NWS
 * forecast zones and a purge time.
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
public class UGCGroup {

    private static final String ZONE_NUM_FMT = "%03d-";

    private static final String DATE_TIME_FMT = "%02d%02d00-";

    private static final int MAX_LINE_LENGTH = 66;

    private static final char DASH = '-';

    private static final char NEW_LINE = '\n';

    private Calendar purgeTime;

    /*
     * Stores zones by State abbreviation and a set of zone numbers for that
     * state. A TreeSet is used to maintain numeric order of zone numbers. A
     * TreeMap is used to maintain alphabetic order of state abbreviations.
     */
    private TreeMap<String, TreeSet<Integer>> zoneGroups;

    public UGCGroup() {
        zoneGroups = new TreeMap<>();
    }

    /**
     * @return the purgeTime
     */
    public Calendar getPurgeTime() {
        return purgeTime;
    }

    /**
     * @param purgeTime
     *            the purgeTime to set
     */
    public void setPurgeTime(Calendar purgeTime) {
        this.purgeTime = purgeTime;
    }

    /**
     * Adds another forecast zone to this UGC group. Zone should be in SSZNNN
     * format, where 'SS' 2 char abbreviation of the state, and 'NNN' is the
     * three digit forecast zone.
     * 
     * @param zone
     */
    public void addZone(String zone) {

        // separate zone string into state and zone number
        Zone z = new Zone(zone);
        String state = z.getState();
        Integer num = z.getNumber();

        /*
         * Add zone number to appropriate state key.
         */
        if (zoneGroups.containsKey(state)) {
            zoneGroups.get(state).add(num);
        } else {
            /*
             * create a new key for this new state.
             */
            TreeSet<Integer> newgroup = new TreeSet<>();
            newgroup.add(num);
            zoneGroups.put(state, newgroup);
        }

    }

    /**
     * Adds a list of forecast zones to this UGC group. Zone should be in SSZNNN
     * format, where 'SS' 2 char abbreviation of the state, and 'NNN' is the
     * three digit forecast zone.
     * 
     * @param zone
     */
    public void addZones(List<String> zones) {
        for (String zone : zones) {
            this.addZone(zone);
        }
    }

    /**
     * Formats the list Zones and purge time into a UGC Element string
     * 
     * @return
     */
    public String createUGCString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, TreeSet<Integer>> entry : zoneGroups
                .entrySet()) {
            sb.append(entry.getKey() + "Z");
            for (Integer num : entry.getValue()) {
                sb.append(String.format(ZONE_NUM_FMT, num));
            }
        }
        sb.append(String.format(DATE_TIME_FMT,
                purgeTime.get(Calendar.DAY_OF_MONTH),
                purgeTime.get(Calendar.HOUR_OF_DAY)));

        /*
         * break string up into multiple lines if > than 66 characters. Each
         * line must end with a "-".
         */
        int index = MAX_LINE_LENGTH - 1;
        while (index < sb.length()) {
            int idx = index;
            boolean done = false;
            do {
                idx--;
                if (sb.charAt(idx) == DASH) {
                    sb.insert(idx + 1, NEW_LINE);
                    done = true;
                }
            } while (!done);
            index = idx + MAX_LINE_LENGTH;
        }

        return sb.toString();
    }

    /**
     * Gets the list of forecast Zones. Zones will be returned in SSZNNN format,
     * where 'SS' 2 char abbreviation of the state, and 'NNN' is the three digit
     * forecast zone.
     * 
     * @return
     */
    public List<String> getZones() {

        ArrayList<String> lst = new ArrayList<>();
        for (Map.Entry<String, TreeSet<Integer>> entry : zoneGroups
                .entrySet()) {
            for (Integer num : entry.getValue()) {
                StringBuilder sb = new StringBuilder(entry.getKey() + "Z");
                sb.append(String.format("%03d", num));
                lst.add(sb.toString());
            }
        }

        return lst;
    }

}
