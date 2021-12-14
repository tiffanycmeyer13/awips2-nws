/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum representing ATCF subregions in each ATCF basin.
 * 
 * Note - Each basin may have one or more subregion, but each subregion belongs
 * to only one basin.
 *
 * <pre>
 *
 * WP Northwest Pacific
 *     W Western Pacific
 * --
 * EP Northeast Pacific
 *     E Eastern North Pacific
 * --
 * CP Central North Pacific
 *     Central North Pacific
 * --
 * IO North Indian Ocean
 *     A Arabian Sea
 *     B Bay of Bengal
 * --
 * SH Southern Hemisphere
 *     S South Indian Ocean
 *     P South Pacific
 * --
 * AL North Atlantic
 *     L North Atlantic
 * --
 * LS South Atlantic
 *     Q South Atlantic
 * --
 * NB New Basin     
 *     N New Basin     
 * --
 *
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Oct 27, 2020 82623       jwu         Initial creation.
 * Nov 20, 2020 85240       jwu         Fixed cross-reference to AtcfBasin.
 *
 * </pre>
 *
 * @author jwu
 * @version 1
 */
public enum AtcfSubregion {
    L("North Atlantic", "AL"),
    W("Western Pacific", "WP"),
    E("Eastern North Pacific", "EP"),
    C("Central North Pacific", "CP"),
    A("Arabian Sea", "IO"),
    B("Bay of Bengal", "IO"),
    S("South Indian Ocean", "SH"),
    P("South Pacific", "SH"),
    Q("South Atlantic", "LS"),
    N("New Basin", "NB");

    private String description;

    private String basin;

    /**
     * Constructor
     *
     * @param description
     * @param basin
     */
    AtcfSubregion(final String description, final String basin) {
        this.description = description;
        this.basin = basin;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the basin name
     */
    public String getBasin() {
        return basin;
    }

    /**
     * Get all AtcfSubregion descriptions.
     *
     * @return String[]
     */
    public static String[] getDescriptions() {
        List<String> desc = new ArrayList<>();
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            desc.add(sbg.getDescription());
        }

        return desc.toArray(new String[desc.size()]);
    }

    /**
     * Get AtcfSubregion based on its Enum name (i.e., "L").
     *
     * @param subRegionCode
     *            AtcfSubregion's code
     *
     * @return AtcfSubregion
     */
    public static AtcfSubregion getSubregion(String subRegionCode) {
        AtcfSubregion subReg = null;
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            if (sbg.name().equalsIgnoreCase(subRegionCode)) {
                subReg = sbg;
                break;
            }
        }

        return subReg;
    }

    /**
     * Get AtcfSubregion's name based on its description.
     *
     * @param desc
     *            AtcfSubregion's description
     *
     * @return AtcfSubregion
     */
    public static String getNameByDesc(String desc) {
        String name = desc;
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            if (sbg.getDescription().equalsIgnoreCase(desc)) {
                name = sbg.name();
                break;
            }
        }

        return name;
    }

    /**
     * Get AtcfSubregion based on its description.
     *
     * @param desc
     *            AtcfSubregion's description
     *
     * @return AtcfSubregion
     */
    public static AtcfSubregion getSubregionByDesc(String desc) {
        AtcfSubregion subReg = null;
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            if (sbg.getDescription().equalsIgnoreCase(desc)) {
                subReg = sbg;
                break;
            }
        }

        return subReg;
    }

    /**
     * Get an AtcfSubregion's basin based on its description.
     *
     * @param desc
     *            AtcfSubregion's description
     *
     * @return AtcfBasin
     */
    public static AtcfBasin getBasinByDesc(String desc) {
        AtcfBasin bsn = null;
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            if (sbg.getDescription().equalsIgnoreCase(desc)) {
                bsn = AtcfBasin.getBasin(sbg.getBasin());
                break;
            }
        }

        return bsn;
    }

    /**
     * Get an AtcfSubregion's basin name based on its description.
     *
     * @param desc
     *            AtcfSubregion's description
     *
     * @return AtcfBasin's name
     */
    public static String getBasinNameByDesc(String desc) {
        String name = "";
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            if (sbg.getDescription().equalsIgnoreCase(desc)) {
                name = sbg.basin;
                break;
            }
        }

        return name;
    }

    /**
     * Get an AtcfSubregion basin's description based on its description.
     *
     * @param desc
     *            AtcfSubregion's description
     *
     * @return String Basin's description
     */
    public static String getBasinDescription(String desc) {
        String bsnDesc = "";
        for (AtcfSubregion sbg : AtcfSubregion.values()) {
            if (sbg.getDescription().equalsIgnoreCase(desc)) {
                bsnDesc = AtcfBasin.getBasin(sbg.getBasin()).getDescription();
                break;
            }
        }

        return bsnDesc;
    }

}