/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class holds the color selection entries defined in colsel.xml, which is
 * used for map display (MAPDSPLY), printer (PRINTER_COLORS), and plotter
 * (PLOTTER_COLORS).
 *
 * The format in original colsel.dat is as following:
 *
 * <pre>
 * This file defines the colors for the MAP DISPLAY program.
 * The objective aid colors are specified in the techlist.dat file.
 * The color numbers are defined as follows:
 *  Refer to AtcfCustomColors (colortable.dat) for the color number definitions.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  MAPDSPLY
 *  background   : 1
 *  ocean        :31
 *  ....
 *  PRINTER_COLORS
 *  background   : 1
 *  ocean        :31
 *  ....
 *
 *  PLOTTER_COLORS
 *  background   : 1
 *  ocean        :31
 *  ....
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 52692      jwu         Created
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "AtcfColorSelections")
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfColorSelections {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "MapColors", type = ColorSelectionEntry.class) })
    private List<ColorSelectionEntry> mapDisplayColors;

    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "PrinterColors", type = ColorSelectionEntry.class) })
    private List<ColorSelectionEntry> printerColors;

    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "PlotterColors", type = ColorSelectionEntry.class) })
    private List<ColorSelectionEntry> plotterColors;

    /**
     * Constructor
     */
    public AtcfColorSelections() {
        mapDisplayColors = new ArrayList<>();
        printerColors = new ArrayList<>();
        plotterColors = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param siteList
     *            List of fix site entry
     */
    public AtcfColorSelections(List<ColorSelectionEntry> mapColors,
            List<ColorSelectionEntry> printerColors,
            List<ColorSelectionEntry> plotterColors) {
        this.mapDisplayColors = new ArrayList<>(mapColors);
        this.printerColors = new ArrayList<>(printerColors);
        this.plotterColors = new ArrayList<>(plotterColors);
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the mapDisplayColors
     */
    public List<ColorSelectionEntry> getMapDisplayColors() {
        return mapDisplayColors;
    }

    /**
     * @param mapDisplayColors
     *            the mapDisplayColors to set
     */
    public void setMapDisplayColors(
            List<ColorSelectionEntry> mapDisplayColors) {
        this.mapDisplayColors = mapDisplayColors;
    }

    /**
     * @return the printerColors
     */
    public List<ColorSelectionEntry> getPrinterColors() {
        return printerColors;
    }

    /**
     * @param printerColors
     *            the printerColors to set
     */
    public void setPrinterColors(List<ColorSelectionEntry> printerColors) {
        this.printerColors = printerColors;
    }

    /**
     * @return the plotterColors
     */
    public List<ColorSelectionEntry> getPlotterColors() {
        return plotterColors;
    }

    /**
     * @param plotterColors
     *            the plotterColors to set
     */
    public void setPlotterColors(List<ColorSelectionEntry> plotterColors) {
        this.plotterColors = plotterColors;
    }

    /**
     * Find the map color selection entry with the given name.
     *
     * @param clrName
     *            Color name
     * @return the color selection entry
     */
    public ColorSelectionEntry getMapColorSelection(String clrName) {
        ColorSelectionEntry color = null;
        for (ColorSelectionEntry clr : mapDisplayColors) {
            if (clr.getName().equals(clrName)) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Find the printer color selection entry with the given name.
     *
     * @param clrName
     *            Color name
     *
     * @return the color selection entry
     */
    public ColorSelectionEntry getPrinterColorSelection(String clrName) {
        ColorSelectionEntry color = null;
        for (ColorSelectionEntry clr : printerColors) {
            if (clr.getName().equals(clrName)) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Find the plotter color selection entry with the given name.
     *
     * @return the color selection entry
     */
    public ColorSelectionEntry getPlotterColorSelection(String clrName) {
        ColorSelectionEntry color = null;
        for (ColorSelectionEntry clr : plotterColors) {
            if (clr.getName().equals(clrName)) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Find the map color selection entry with the given name.
     *
     * @param clrName
     *            ColorSelectionNames
     * @return the color selection entry
     */
    public ColorSelectionEntry getMapColorSelection(
            ColorSelectionNames clrName) {
        ColorSelectionEntry color = null;
        for (ColorSelectionEntry clr : mapDisplayColors) {
            if (clr.getName().equals(clrName.getName())) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Find the printer color selection entry with the given name.
     *
     * @param clrName
     *            ColorSelectionNames
     * @return the color selection entry
     */
    public ColorSelectionEntry getPrinterColorSelection(
            ColorSelectionNames clrName) {
        ColorSelectionEntry color = null;
        for (ColorSelectionEntry clr : printerColors) {
            if (clr.getName().equals(clrName.getName())) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Find the plotter color selection entry with the given name.
     *
     * @param clrName
     *            ColorSelectionNames
     * @return the color selection entry
     */
    public ColorSelectionEntry getPlotterColorSelection(
            ColorSelectionNames clrName) {
        ColorSelectionEntry color = null;
        for (ColorSelectionEntry clr : plotterColors) {
            if (clr.getName().equals(clrName.getName())) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * colsel.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        sb.append(ColorSelectionGroup.MAPDSPLY.getName());
        sb.append(newline);
        for (ColorSelectionEntry fs : mapDisplayColors) {
            sb.append(String.format("%-13s", fs.getName()));
            sb.append(":");

            int[] inds = fs.getColorIndex();
            StringBuilder si = new StringBuilder();
            for (int ii = 0; ii < inds.length; ii++) {
                si.append(String.format("%2d", inds[ii]));
                si.append(" ");
            }

            sb.append(si);
            sb.append(newline);
        }

        sb.append(ColorSelectionGroup.PRINTER.getName());
        sb.append(newline);
        for (ColorSelectionEntry fs : printerColors) {
            sb.append(String.format("%-13s", fs.getName()));
            sb.append(":");

            int[] inds = fs.getColorIndex();
            StringBuilder si = new StringBuilder();
            for (int ii = 0; ii < inds.length; ii++) {
                si.append(String.format("%2d", inds[ii]));
                si.append(" ");
            }

            sb.append(si);
            sb.append(newline);
        }

        sb.append(ColorSelectionGroup.PLOTTER.getName());
        sb.append(newline);
        for (ColorSelectionEntry fs : plotterColors) {
            sb.append(String.format("%-13s", fs.getName()));
            sb.append(":");

            int[] inds = fs.getColorIndex();
            StringBuilder si = new StringBuilder();
            for (int ii = 0; ii < inds.length; ii++) {
                si.append(String.format("%2d", inds[ii]));
                si.append(" ");
            }

            sb.append(si);
            sb.append(newline);
        }

        return sb.toString();
    }

}