/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class to represent the formatted wind information, include maximum wind,
 * gust, wind radii (34/50/64 kt) or wave radii (12 ft seas).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2021 87783      jwu         Initial coding.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class WindData {

    /**
     * Forecast max wind in knots and round to nearest 5, i.e, "85".
     */
    @DynamicSerializeElement
    private String maxWind;

    /**
     * Forecast max wind in MPH and round to nearest 5, i.e, "90".
     */
    @DynamicSerializeElement
    private String maxWindMph;

    /**
     * Forecast max wind in km/h and round to nearest 5, i.e, "90".
     */
    @DynamicSerializeElement
    private String maxWindKmh;

    /**
     * Forecast gust in knots and round to nearest 5, i.e, "85".
     */
    @DynamicSerializeElement
    private String gust;

    /**
     * Forecast wind radii.
     */
    @DynamicSerializeElement
    private List<RadiiData> windRadii;

    /**
     * Forecast 12 ft seas radii (TAU 3).
     */
    @DynamicSerializeElement
    private RadiiData waveRadii;

    /**
     * Constructor
     */
    public WindData() {
        this.maxWind = "";
        this.maxWindMph = "";
        this.maxWindKmh = "";
        this.gust = "";

        this.windRadii = null;
        this.waveRadii = null;
    }

    /**
     * @return the maxWind
     */
    public String getMaxWind() {
        return maxWind;
    }

    /**
     * @param maxWind
     *            the maxWind to set
     */
    public void setMaxWind(String maxWind) {
        this.maxWind = maxWind;
    }

    /**
     * @return the maxWindMph
     */
    public String getMaxWindMph() {
        return maxWindMph;
    }

    /**
     * @param maxWindMph
     *            the maxWindMph to set
     */
    public void setMaxWindMph(String maxWindMph) {
        this.maxWindMph = maxWindMph;
    }

    /**
     * @return the maxWindKmh
     */
    public String getMaxWindKmh() {
        return maxWindKmh;
    }

    /**
     * @param maxWindKmh
     *            the maxWindKmh to set
     */
    public void setMaxWindKmh(String maxWindKmh) {
        this.maxWindKmh = maxWindKmh;
    }

    /**
     * @return the gust
     */
    public String getGust() {
        return gust;
    }

    /**
     * @param gust
     *            the gust to set
     */
    public void setGust(String gust) {
        this.gust = gust;
    }

    /**
     * @return the windRadii
     */
    public List<RadiiData> getWindRadii() {
        return windRadii;
    }

    /**
     * @param windRadii
     *            the windRadii to set
     */
    public void setWindRadii(List<RadiiData> windRadii) {
        this.windRadii = windRadii;
    }

    /**
     * @return the waveRadii
     */
    public RadiiData getWaveRadii() {
        return waveRadii;
    }

    /**
     * @param waveRadii
     *            the waveRadii to set
     */
    public void setWaveRadii(RadiiData waveRadii) {
        this.waveRadii = waveRadii;
    }

    /**
     * Write as an information string.
     * 
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ( !maxWind.isEmpty() ) {
            sb.append(String
                    .format("Max Wind: %s KT...Gust: %s KT.\n", maxWind, gust)
                    .toUpperCase());
            for (RadiiData rd : windRadii) {
                sb.append(String.format("%s KT.......%sNE %sSE %sSW %sNW.\n",
                        rd.getRad(), rd.getQuad()[0], rd.getQuad()[1],
                        rd.getQuad()[2], rd.getQuad()[3]));
            }

            if (waveRadii != null) {
             sb.append(String.format("%s FT SEAS..%sNE %sSE %sSW %sNW.\n",
                        waveRadii.getRad(), waveRadii.getQuad()[0],
                        waveRadii.getQuad()[1], waveRadii.getQuad()[2],
                        waveRadii.getQuad()[3]));
            }
        }

        return sb.toString();
    }


}
