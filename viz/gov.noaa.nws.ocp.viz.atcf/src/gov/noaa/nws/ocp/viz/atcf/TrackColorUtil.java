/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfColorSelections;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfCustomColors;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionNames;

/**
 * Utility class to retrieve and find a defined color for drawing a best track.
 * 
 * Note: The first color in "track_colors" is reserved for displaying the active
 * storm and default using Color.GREEN if not defined.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 18, 2019 61605      jwu         Initial creation.
 * Apr 02, 2019 61882      jwu         Add mothods for wind radii colors.
 * May 07, 2019 63005      jwu         Removed reservation of 1st color for active storm.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class TrackColorUtil {

    /*
     * ATCF color selections in colsel.dat, defined via
     * Configuration=>"Change Color".
     */
    private AtcfColorSelections clrSel;

    /*
     * ATCF track color selection entry in colsel.dat, defined via
     * Configuration=>"Change Color"=>"Best Track Color".
     */
    private ColorSelectionEntry trackClrEntry;

    /*
     * ATCF color selections in colsel.dat, defined via
     * Configuration=>"Change Color".
     */
    private AtcfCustomColors atcfColors;

    /*
     * Colors that have been used and should be excluded.
     */
    private List<Integer> usedColors;

    /*
     * Default color for drawing best track.
     */
    private Color defaultColor = Color.GREEN;

    /**
     * Constructor
     */
    public TrackColorUtil() {

        atcfColors = AtcfConfigurationManager.getInstance()
                .getAtcfCustomColors();

        clrSel = AtcfConfigurationManager.getInstance()
                .getAtcfColorSelections();

        trackClrEntry = clrSel.getMapColorSelection(ColorSelectionNames.TRACK);

        usedColors = new ArrayList<>();
    }

    /**
     * Constructor
     */
    public TrackColorUtil(List<Integer> usedColors) {

        atcfColors = AtcfConfigurationManager.getInstance()
                .getAtcfCustomColors();

        clrSel = AtcfConfigurationManager.getInstance()
                .getAtcfColorSelections();

        trackClrEntry = clrSel.getMapColorSelection(ColorSelectionNames.TRACK);

        this.usedColors = usedColors;

    }

    /**
     * @return the usedColors
     */
    public List<Integer> getUsedColors() {
        return usedColors;
    }

    /**
     * @param usedColors
     *            the usedColors to set
     */
    public void setUsedColors(List<Integer> usedColors) {
        this.usedColors = usedColors;
    }

    /**
     * Get the first color defined in track color entry, which should be used to
     * draw the active storm.
     * 
     * @return Color
     */
    public int getActiveStormColorIndex() {
        int clr = -1;
        if (trackClrEntry != null && trackClrEntry.getColorIndex().length > 0) {
            clr = trackClrEntry.getColorIndex()[0];
        }

        return clr;
    }

    /**
     * Get the first color defined in track color entry, which should be used to
     * draw the active storm.
     * 
     * @return Color
     */
    public Color getActiveStormColor() {
        int clr = -1;
        if (trackClrEntry != null && trackClrEntry.getColorIndex().length > 0) {
            clr = trackClrEntry.getColorIndex()[0];
        }

        return getAtcfCustomColor(clr);
    }

    /**
     * Find the next un-used track color index in track color entry.
     *
     * @return index
     */
    public int getNextTrackColorIndex() {
        int index = -1;

        if (trackClrEntry != null) {
            int[] indexes = trackClrEntry.getColorIndex();
            for (int ii = 0; ii < indexes.length; ii++) {
                if (!usedColors.contains(indexes[ii])) {
                    index = indexes[ii];
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Find a given color from ATCF custom color table. If not found, return the
     * default color GREEN.
     * 
     * @param index
     *            index of the color in ATCF custom colors.
     * @return Color
     */
    public Color getAtcfCustomColor(int index) {

        Color clr = atcfColors.getColor(index);

        if (clr == null) {
            clr = defaultColor;
        }

        return clr;
    }

    /**
     * Get the color defined to draw 34kt/50kt/64kn wind radii.
     * 
     * @param windRadii
     * @return Color
     */
    public Color getWindRadiiColor(int windRadii) {
        int clr = -1;

        ColorSelectionEntry clrEntry = null;
        if (windRadii == 34) {
            clrEntry = clrSel
                    .getMapColorSelection(ColorSelectionNames.WIND_RAD_34);
        } else if (windRadii == 50) {
            clrEntry = clrSel
                    .getMapColorSelection(ColorSelectionNames.WIND_RAD_50);
        } else if (windRadii == 64) {
            clrEntry = clrSel
                    .getMapColorSelection(ColorSelectionNames.WIND_RAD_64);
        }

        if (clrEntry != null && clrEntry.getColorIndex().length > 0) {
            clr = clrEntry.getColorIndex()[0];
        }

        return getAtcfCustomColor(clr);
    }

    /**
     * Get the preferred color defined in colsel.dat.
     * 
     * @param clrName
     * @return Color
     */
    public Color getPreferedColor(ColorSelectionNames clrName) {
        int clr = -1;

        ColorSelectionEntry clrEntry = clrSel.getMapColorSelection(clrName);

        if (clrEntry != null && clrEntry.getColorIndex().length > 0) {
            clr = clrEntry.getColorIndex()[0];
        }

        return getAtcfCustomColor(clr);
    }

    /**
     * @return the clrSel
     */
    public AtcfColorSelections getClrSel() {
        return clrSel;
    }

    /**
     * @return the atcfColors
     */
    public AtcfCustomColors getAtcfColors() {
        return atcfColors;
    }

}