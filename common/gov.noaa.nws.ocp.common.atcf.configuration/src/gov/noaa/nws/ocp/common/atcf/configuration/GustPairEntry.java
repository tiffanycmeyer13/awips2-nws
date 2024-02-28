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
 * This class represents a maximum wind/gust pair entry in gust.dat, which is
 * used for converting a maximum wind value into a gust value.
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
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class GustPairEntry {

    /**
     * Maximum wind value
     */
    @DynamicSerializeElement
    @XmlAttribute
    private int maxWind;

    /**
     * Gust value
     */
    @DynamicSerializeElement
    @XmlAttribute
    private int gust;

    /**
     * Constructor.
     */
    public GustPairEntry() {
    }

    /**
     * Constructor.
     * 
     * @param maxWnd
     *            Maximum wind value
     * @param gust
     *            Gust value
     */
    public GustPairEntry(int maxWnd, int gust) {
        this.maxWind = maxWnd;
        this.gust = gust;
    }

    /**
     * @return the maxWind
     */
    public int getMaxWind() {
        return maxWind;
    }

    /**
     * @param maxWind
     *            the maxWind to set
     */
    public void setMaxWind(int maxWind) {
        this.maxWind = maxWind;
    }

    /**
     * @return the gust
     */
    public int getGust() {
        return gust;
    }

    /**
     * @param gust
     *            the gust to set
     */
    public void setGust(int gust) {
        this.gust = gust;
    }

}