/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data Significant Clouds, from METAR report text. From
 * metar.h#Sig_Clouds
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
public class SignificantCloud {

    /** significant cloud type */
    private String significantCloudType;

    /** significant cloud location */
    private String significantCloudLocation;

    /** significant cloud direction */
    private String significantCloudDirection;

    /** significant cloud movement */
    private String significantCloudMovement;

    /**
     * Empty constructor.
     */
    public SignificantCloud() {
    }

    /**
     * @return the significantCloudType
     */
    public String getSignificantCloudType() {
        return significantCloudType;
    }

    /**
     * @param significantCloudType
     *            the significantCloudType to set
     */
    public void setSignificantCloudType(String significantCloudType) {
        this.significantCloudType = significantCloudType;
    }

    /**
     * @return the significantCloudLocation
     */
    public String getSignificantCloudLocation() {
        return significantCloudLocation;
    }

    /**
     * @param significantCloudLocation
     *            the significantCloudLocation to set
     */
    public void setSignificantCloudLocation(String significantCloudLocation) {
        this.significantCloudLocation = significantCloudLocation;
    }

    /**
     * @return the significantCloudDirection
     */
    public String getSignificantCloudsDirection() {
        return significantCloudDirection;
    }

    /**
     * @param significantCloudDirection
     *            the significantCloudDirection to set
     */
    public void setSignificantCloudDirection(String significantCloudDirection) {
        this.significantCloudDirection = significantCloudDirection;
    }

    /**
     * @return the significantCloudMovement
     */
    public String getSignificantCloudMovement() {
        return significantCloudMovement;
    }

    /**
     * @param significantCloudMovement
     *            the significantCloudMovement to set
     */
    public void setSignificantCloudMovement(String significantCloudMovement) {
        this.significantCloudMovement = significantCloudMovement;
    }
}
