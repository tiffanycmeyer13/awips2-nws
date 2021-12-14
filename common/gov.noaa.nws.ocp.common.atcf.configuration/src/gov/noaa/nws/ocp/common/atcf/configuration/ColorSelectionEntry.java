/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represents a color selection entry in colsel.dat, which is used
 * for map display (MAPDSPLY), printer (PRINTER_COLORS), and plotter
 * (PLOTTER_COLORS).
 *
 * <pre>
 *
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
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 #52692     jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class ColorSelectionEntry {

    /**
     * Name
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Color index(es)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private int[] colorIndex;

    /**
     * Constructor.
     */
    public ColorSelectionEntry() {
    }

    /**
     * Constructor.
     * 
     * @param name
     *            site name
     * @param colorIndex
     *            indexes of the colors in colortable.dat
     */
    public ColorSelectionEntry(String name, int[] index) {
        this.name = name;
        this.colorIndex = index;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the colorIndex
     */
    public int[] getColorIndex() {
        return colorIndex;
    }

    /**
     * @param colorIndex
     *            the colorIndex to set
     */
    public void setColorIndex(int[] colorIndex) {
        this.colorIndex = colorIndex;
    }

}