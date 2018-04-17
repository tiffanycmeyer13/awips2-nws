/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data Dispatch Visibility, from METAR report text. From
 * metar.h#Dispatch_VisRange
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
public class DispatchVisRange {

    /** Boolean if Dispatch variable Visual Range is present */
    private boolean variableVisualRange;

    /** Boolean if min DVR is present */
    private boolean belowMinDVR;

    /** Boolean if max DVR is present */
    private boolean aboveMaxDVR;

    /** variable visual range */
    private int visRange;

    /** max variable DVR */
    private int maxVisualRange;

    /** min variable DVR */
    private int minVisualRange;

    /**
     * Empty constructor.
     */
    public DispatchVisRange() {
    }

    /**
     * @return the variableVisualRange
     */
    public boolean isVariableVisualRange() {
        return variableVisualRange;
    }

    /**
     * @param variableVisualRange
     *            the variableVisualRange to set
     */
    public void setVariableVisualRange(boolean variableVisualRange) {
        this.variableVisualRange = variableVisualRange;
    }

    /**
     * @return the belowMinDVR
     */
    public boolean isBelowMinDVR() {
        return belowMinDVR;
    }

    /**
     * @param belowMinDVR
     *            the belowMinDVR to set
     */
    public void setBelowMinDVR(boolean belowMinDVR) {
        this.belowMinDVR = belowMinDVR;
    }

    /**
     * @return the aboveMaxDVR
     */
    public boolean isAboveMaxDVR() {
        return aboveMaxDVR;
    }

    /**
     * @param aboveMaxDVR
     *            the aboveMaxDVR to set
     */
    public void setAboveMaxDVR(boolean aboveMaxDVR) {
        this.aboveMaxDVR = aboveMaxDVR;
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
     * @return the maxVisualRange
     */
    public int getMaxVisualRange() {
        return maxVisualRange;
    }

    /**
     * @param maxVisualRange
     *            the maxVisualRange to set
     */
    public void setMaxVisualRange(int maxVisualRange) {
        this.maxVisualRange = maxVisualRange;
    }

    /**
     * @return the minVisualRange
     */
    public int getMinVisualRange() {
        return minVisualRange;
    }

    /**
     * @param minVisualRange
     *            the minVisualRange to set
     */
    public void setMinVisualRange(int minVisualRange) {
        this.minVisualRange = minVisualRange;
    }
}
