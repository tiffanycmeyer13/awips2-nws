/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data Wind Data, from METAR report text. From
 * hmPED_cmn.h#WindStruct
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 01 FEB 2017  28609      amoore      Initial creation
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class MetarWind {

    /** units of wind data */
    private String windUnits;

    /** Boolean if wind is variable */
    private boolean windVrb;

    /** wind direction */
    private int windDir;

    /** wind speed */
    private int windSpeed;

    /** wind gust speed */
    private int windGust;

    /**
     * Empty constructor.
     */
    public MetarWind() {
    }

    /**
     * @return the windUnits
     */
    public String getWindUnits() {
        return windUnits;
    }

    /**
     * @param windUnits
     *            the windUnits to set
     */
    public void setWindUnits(String windUnits) {
        this.windUnits = windUnits;
    }

    /**
     * @return the windVrb
     */
    public boolean isWindVrb() {
        return windVrb;
    }

    /**
     * @param windVrb
     *            the windVrb to set
     */
    public void setWindVrb(boolean windVrb) {
        this.windVrb = windVrb;
    }

    /**
     * @return the windDir
     */
    public int getWindDir() {
        return windDir;
    }

    /**
     * @param windDir
     *            the windDir to set
     */
    public void setWindDir(int windDir) {
        this.windDir = windDir;
    }

    /**
     * @return the windSpeed
     */
    public int getWindSpeed() {
        return windSpeed;
    }

    /**
     * @param windSpeed
     *            the windSpeed to set
     */
    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    /**
     * @return the windGust
     */
    public int getWindGust() {
        return windGust;
    }

    /**
     * @param windGust
     *            the windGust to set
     */
    public void setWindGust(int windGust) {
        this.windGust = windGust;
    }
}
