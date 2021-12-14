/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * This class holds the colors defined in colortable.xml that are used for ATCFd
 * displaying.
 *
 * The format in original colortable.dat is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 *
 * DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 * START_OF_DATA:
 *
 * 000000
 * 808080
 * cfcfcf
 * ......
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
@XmlRootElement(name = "AtcfCustomColors")
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfCustomColors {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfCustomColors.class);

    public static final String DEFAULT_COLOR = "000000";

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Color", type = String.class) })
    private List<String> colors;

    /**
     * Constructor
     */
    public AtcfCustomColors() {
        colors = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param colorList
     *            List of color names
     */
    public AtcfCustomColors(List<String> colorList) {
        colors = new ArrayList<>(colorList);
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
     * @return the colors
     */
    public List<String> getColors() {
        return colors;
    }

    /**
     * @param colors
     *            the colors to set
     */
    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    /**
     * Find the color entry with the given name.
     *
     * @return the color
     */
    public String getColorByName(String clrName) {
        String color = null;
        for (String clr : colors) {
            if (clr.equals(clrName)) {
                color = clr;
                break;
            }
        }

        return color;
    }

    /**
     * Find the color entry with the given index.
     *
     * @return the color
     */
    public String getColorByIndex(int index) {

        String color = null;
        if (index >= 0 && index < colors.size()) {
            color = colors.get(index);
        }

        return color;
    }

    /**
     * Find the color entry with the given index.
     *
     * @return the color
     */
    public Color getColor(int index) {
        return makeColor(getColorByIndex(index));
    }

    /**
     * Make the color with the given name.
     *
     * @return the color
     */
    public Color makeColor(String clrName) {

        Color clr = null;

        try {
            clr = java.awt.Color.decode("#" + clrName);
        } catch (NumberFormatException ee) {
            logger.warn("AtcfCustomColor: invalid color entry - " + clrName);
        }
        return clr;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * colortable.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        for (String fs : colors) {
            sb.append(String.format("%s", fs));
            sb.append(newline);
        }

        return sb.toString();
    }

}