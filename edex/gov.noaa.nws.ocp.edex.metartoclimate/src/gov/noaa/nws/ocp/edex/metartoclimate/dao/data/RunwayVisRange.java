/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data Runway Visibility, from METAR report text. From
 * metar.h#Runway_VisRange
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
public class RunwayVisRange {

    /** runway number */
    private String runwayDesignator;

    /** Boolean if variable visual; range value is present */
    private boolean vrblVisRange;

    /** boolean if min runway visable range is present */
    private boolean belowMinRVR;

    /** Boolean if max runway visable range is present */
    private boolean aboveMaxRVR;

    /** visable range */
    private int visRange;

    /** Max runway visable range */
    private int maxVisRange;

    /** min runway visable range */
    private int minVisRange;

    /**
     * Empty constructor.
     */
    public RunwayVisRange() {
    }

    /**
     * @return the runwayDesignator
     */
    public String getRunwayDesignator() {
        return runwayDesignator;
    }

    /**
     * @param runwayDesignator
     *            the runwayDesignator to set
     */
    public void setRunwayDesignator(String runwayDesignator) {
        this.runwayDesignator = runwayDesignator;
    }

    /**
     * @return the vrblVisRange
     */
    public boolean isVrblVisRange() {
        return vrblVisRange;
    }

    /**
     * @param vrblVisRange
     *            the vrblVisRange to set
     */
    public void setVrblVisRange(boolean vrblVisRange) {
        this.vrblVisRange = vrblVisRange;
    }

    /**
     * @return the belowMinRVR
     */
    public boolean isBelowMinRVR() {
        return belowMinRVR;
    }

    /**
     * @param belowMinRVR
     *            the belowMinRVR to set
     */
    public void setBelowMinRVR(boolean belowMinRVR) {
        this.belowMinRVR = belowMinRVR;
    }

    /**
     * @return the aboveMaxRVR
     */
    public boolean isAboveMaxRVR() {
        return aboveMaxRVR;
    }

    /**
     * @param aboveMaxRVR
     *            the aboveMaxRVR to set
     */
    public void setAboveMaxRVR(boolean aboveMaxRVR) {
        this.aboveMaxRVR = aboveMaxRVR;
    }

    /**
     * @return the visRange
     */
    public int getVisRange() {
        return visRange;
    }

    /**
     * @param visRange
     *            the visRange to set
     */
    public void setVisRange(int visRange) {
        this.visRange = visRange;
    }

    /**
     * @return the maxVisRange
     */
    public int getMaxVisRange() {
        return maxVisRange;
    }

    /**
     * @param maxVisRange
     *            the maxVisRange to set
     */
    public void setMaxVisRange(int maxVisRange) {
        this.maxVisRange = maxVisRange;
    }

    /**
     * @return the minVisRange
     */
    public int getMinVisRange() {
        return minVisRange;
    }

    /**
     * @param minVisRange
     *            the minVisRange to set
     */
    public void setMinVisRange(int minVisRange) {
        this.minVisRange = minVisRange;
    }

}