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
 * This class represents the maximum wind/gust pair entries in gust.xml, which
 * is used for converting a maximum wind value into a gust value.
 *
 * The format in original gust.dat is as following:
 *
 * <pre>
 *
 *This file contains:
 *        A table of default gust data dependent upon maximum wind speed (kts).
 *The data are used to generate text warnings and web graphic labels.
 *The format is:
 *
 *intensity              gust 
 *  (kts)                (kts)
 *
 *
 *
 *
 *DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *START_OF_DATA:
 *
 *0                0
 *5               10
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2020 71724      jwu         Initial creation.
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "MaxWindGustPairs")
@XmlAccessorType(XmlAccessType.NONE)
public class MaxWindGustPairs {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "Wind-Gust", type = GustPairEntry.class) })
    private List<GustPairEntry> gustPairs;

    /**
     * Constructor
     */
    public MaxWindGustPairs() {
        super();
        gustPairs = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param gustList
     *            List of max. wind/gust entry
     */
    public MaxWindGustPairs(List<GustPairEntry> gustList) {
        gustPairs = new ArrayList<>(gustList);
    }

    /**
     * @return the gustPairs
     */
    public List<GustPairEntry> getGustPairs() {
        return gustPairs;
    }

    /**
     * @param gustPairs
     *            the gustPairs to set
     */
    public void setGustPairs(List<GustPairEntry> gustPairs) {
        this.gustPairs = gustPairs;
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
     * Find the gust value for the given max. wind. Defaults to the max. wind.
     *
     * @return gust
     */
    public int getGustValue(int maxWnd) {
        int gust = maxWnd;
        for (GustPairEntry gp : gustPairs) {
            if (gp.getMaxWind() == maxWnd) {
                gust = gp.getGust();
                break;
            }
        }

        return gust;
    }

    /**
     * Find the max wind value for the given gust. Defaults to the gust vlaue.
     *
     * @return max. wind
     */
    public int getMaxWindValue(int gust) {
        int maxWnd = gust;
        for (GustPairEntry gp : gustPairs) {
            if (gp.getGust() == gust) {
                maxWnd = gp.getMaxWind();
                break;
            }
        }

        return maxWnd;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * gust.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("    This file contains:");
        sb.append(
                "        A table of default gust data dependent upon maximum wind speed (kts).");
        sb.append(
                "The data are used to generate text warnings and web graphic labels.");
        sb.append("The format is:");
        sb.append(newline);
        sb.append("intensity              gust");
        sb.append("  (kts)                (kts)");
        sb.append(newline);
        sb.append(newline);
        sb.append(newline);
        sb.append(newline);

        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        for (GustPairEntry gp : gustPairs) {
            sb.append(String.format("%d", gp.getMaxWind()));
            sb.append(String.format("%15d", gp.getGust()));
            sb.append(newline);
        }

        return sb.toString();
    }
}