/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum representing the ATCF basins, which is used to identify storm data.
 *
 * Note - Each basin may have one or more subregion, but each subregion belongs
 * to only one basin.
 *
 * <pre>
 *
 * The tropical cyclone description data is stored in ascii files. Each storm is 
 * given a unique eight character code called the storm ID to identify it in the 
 * database. The storm ID is of  the format RECYYYYY, where
 *
 *     RE = Region (basin) of Origin
 *     CY = Annual Cyclone Number(01-99)
 *     YYYY = Year
 *
 * Basins include:
 *
 *     WP Northwest Pacific
 *          W Western Pacific
 *      --
 *     EP Northeast Pacific
 *         E Eastern North Pacific
 *     --
 *     CP Central North Pacific
 *         Central North Pacific
 *     --
 *     IO North Indian Ocean
 *         A Arabian Sea
 *         B Bay of Bengal
 *     --
 *     SH Southern Hemisphere
 *         S South Indian Ocean
 *         P South Pacific
 *     --
 *     AL North Atlantic
 *         L North Atlantic
 *     --
 *     LS South Atlantic
 *         Q South Atlantic
 *     --
 *     NB New Basin     
 *         N New Basin     
 *     --
 *
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Oct 27, 2020 82623       jwu         Initial creation.
 *
 * </pre>
 * 
 * @author jwu
 * @version 1
 */
public enum AtcfBasin {
    AL("North Atlantic", new AtcfSubregion[] { AtcfSubregion.L }),
    WP("Northwest Pacific", new AtcfSubregion[] { AtcfSubregion.W }),
    EP("Northeast Pacific", new AtcfSubregion[] { AtcfSubregion.E }),
    CP("Central North Pacific", new AtcfSubregion[] { AtcfSubregion.C }),
    IO(
            "North Indian Ocean",
            new AtcfSubregion[] { AtcfSubregion.A, AtcfSubregion.B }),
    SH(
            "Southern Hemisphere",
            new AtcfSubregion[] { AtcfSubregion.S, AtcfSubregion.P }),
    LS("South Atlantic", new AtcfSubregion[] { AtcfSubregion.Q }),
    NB("New Basin", new AtcfSubregion[] { AtcfSubregion.N });

    // Description of the basin.
    private String description;

    // Subregions in the basin.
    private AtcfSubregion[] subRegions;

    /**
     * Constructor
     */
    AtcfBasin(final String description, AtcfSubregion[] subRegions) {
        this.description = description;
        this.subRegions = subRegions;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the subRegions
     */
    public AtcfSubregion[] getSubRegions() {
        return subRegions;
    }

    /**
     * Get all AtcfBasin descriptions.
     *
     * @return String[]
     */
    public static String[] getDescriptions() {
        List<String> desc = new ArrayList<>();
        for (AtcfBasin bsn : AtcfBasin.values()) {
            desc.add(bsn.getDescription());
        }

        return desc.toArray(new String[desc.size()]);
    }

    /**
     * Get AtcfBasin based on its Enum name (i.e., "AL").
     *
     * @param name
     *            Name of the basin
     *
     * @return AtcfBasin
     */
    public static AtcfBasin getBasin(String name) {
        AtcfBasin basin = null;
        for (AtcfBasin bsn : AtcfBasin.values()) {
            if (bsn.name().equalsIgnoreCase(name)) {
                basin = bsn;
                break;
            }
        }

        return basin;
    }

    /**
     * Get AtcfBasin based on its description (i.e., "North Atlantic").
     *
     * @param desc
     *            Description of basin
     *
     * @return AtcfBasin
     */
    public static AtcfBasin getBasinbyDesc(String desc) {
        AtcfBasin basin = null;
        for (AtcfBasin bsn : AtcfBasin.values()) {
            if (bsn.getDescription().equalsIgnoreCase(desc)) {
                basin = bsn;
                break;
            }
        }

        return basin;
    }

    /**
     * Get AtcfBasin based on its description (i.e., "North Atlantic").
     *
     * @param name
     *            Name of basin
     *
     * @return String Description of basin
     */
    public static String getDescByName(String name) {
        String desc = name;
        for (AtcfBasin bsn : AtcfBasin.values()) {
            if (bsn.name().equalsIgnoreCase(name)) {
                desc = bsn.getDescription();
                break;
            }
        }

        return desc;
    }

}
