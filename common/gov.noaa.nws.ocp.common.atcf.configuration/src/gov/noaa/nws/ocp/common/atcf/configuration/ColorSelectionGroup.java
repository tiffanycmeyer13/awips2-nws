/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represents the color selection groups in colsel.dat, including map
 * display (MAPDSPLY), printer (PRINTER_COLORS), and plotter (PLOTTER_COLORS).
 *
 * <pre>
 * 
 * The objective aid colors are specified in the techlist.dat file.
 *
 * The color numbers are defined as follows:
 * Refer to AtcfCustomColors (colortable.dat) for the color number definitions.
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
@XmlRootElement(name = "ColorSelectionGroup")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum ColorSelectionGroup {

    MAPDSPLY("MAPDSPLY"), PRINTER("PRINTER-COLORS"), PLOTTER("PLOTTER-COLORS");

    @DynamicSerializeElement
    private String name;

    /**
     * @param iValue
     */
    private ColorSelectionGroup(final String name) {
        this.name = name;
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

}