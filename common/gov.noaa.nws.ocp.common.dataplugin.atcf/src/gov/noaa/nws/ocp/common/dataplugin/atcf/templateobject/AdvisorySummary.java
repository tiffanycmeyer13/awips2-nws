/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class a formatted advisory summary in NHCMESSAGE/stormId.summary.
 *
 * <pre>
 *     LOCATION...29.0N 89.7W
 *     ABOUT 75 MI...120 KM SE OF HOUMA LOUISIANA
 *     ABOUT 75 MI...120 KM SSE OF NEW ORLEANS LOUISIANA
 *     MAXIMUM SUSTAINED WINDS...80 MPH...130 KM/H
 *     PRESENT MOVEMENT...NW OR 310 DEGREES AT 8 MPH...13 KM/H
 *     MINIMUM CENTRAL PRESSURE...968 MB...28.59 INCHES
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 10, 2020 87783      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class AdvisorySummary {

    // Latitude/direction = "S" if the input lat < 0; otherwise "N"
    @DynamicSerializeElement
    private String lat;

    // Longitude/direction = "East" if the input lon > 0; otherwise "West"
    @DynamicSerializeElement
    private String lon;

    // Distance from first geo. reference location in miles & kilometers.
    @DynamicSerializeElement
    private String distanceMi1;

    @DynamicSerializeElement
    private String distanceKm1;

    // Direction from first geo. reference location.
    @DynamicSerializeElement
    private String geoDirection1;

    // Name of first geo. reference location.
    @DynamicSerializeElement
    private String geoReference1;

    // Distance from second geo. reference location in miles & kilometers.
    @DynamicSerializeElement
    private String distanceMi2;

    @DynamicSerializeElement
    private String distanceKm2;

    // Direction from second geo. reference location.
    @DynamicSerializeElement
    private String geoDirection2;

    // Name of second geo. reference location.
    @DynamicSerializeElement
    private String geoReference2;

    // Maximum wind speed in mph & km/h.
    @DynamicSerializeElement
    private String windMph;

    @DynamicSerializeElement
    private String windKmh;

    // Wind direction.
    @DynamicSerializeElement
    private String direction;

    // Wind direction in degrees.
    @DynamicSerializeElement
    private String degrees;

    // Storm direction in knots, mph and km/h.
    @DynamicSerializeElement
    private String movementKt;

    @DynamicSerializeElement
    private String movementMph;

    @DynamicSerializeElement
    private String movementKmh;

    // Storm minimum sea level pressure in mb & inches.
    @DynamicSerializeElement
    private String pressureMb;

    @DynamicSerializeElement
    private String pressureIn;

    // If it is stationary.
    private boolean isStationary;

    /**
     * Default constructor
     */
    public AdvisorySummary() {
        this.lat = "";
        this.lon = "";
        this.distanceMi1 = "";
        this.distanceMi2 = "";
        this.distanceKm1 = "";
        this.distanceKm2 = "";
        this.geoDirection1 = "";
        this.geoDirection2 = "";
        this.geoReference1 = "";
        this.geoReference2 = "";
        this.direction = "";
        this.windMph = "";
        this.windKmh = "";
        this.degrees = "";
        this.movementKt = "";
        this.movementMph = "";
        this.movementKmh = "";
        this.pressureMb = "";
        this.pressureIn = "";
        this.isStationary = false;
    }

    /**
     * Constructor
     */
    public AdvisorySummary(String lat, String lon, String distanceMi1,
            String distanceKm1, String distanceMi2, String distanceKm2,
            String geoDirection1, String geoReference1, String geoDirection2,
            String geoReference2, String windMph, String windKmh,
            String direction, String degrees, String movementKt,
            String movementMph,
            String movementKmh, String pressureMb, String pressureIn, boolean isStationary) {
        this.lat = lat;
        this.lon = lon;
        this.distanceMi1 = distanceMi1;
        this.distanceMi2 = distanceMi2;
        this.distanceKm1 = distanceKm1;
        this.distanceKm2 = distanceKm2;
        this.geoDirection1 = geoDirection1;
        this.geoDirection2 = geoDirection2;
        this.geoReference1 = geoReference1;
        this.geoReference2 = geoReference2;
        this.direction = direction;
        this.windMph = windMph;
        this.windKmh = windKmh;
        this.degrees = degrees;
        this.movementKt = movementKt;
        this.movementMph = movementMph;
        this.movementKmh = movementKmh;
        this.pressureMb = pressureMb;
        this.pressureIn = pressureIn;
        this.isStationary = isStationary;
    }

    /**
     * @return the lat
     */
    public String getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(String lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public String getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(String lon) {
        this.lon = lon;
    }

    /**
     * @return the distanceMi1
     */
    public String getDistanceMi1() {
        return distanceMi1;
    }

    /**
     * @param distanceMi1
     *            the distanceMi1 to set
     */
    public void setDistanceMi1(String distanceMi1) {
        this.distanceMi1 = distanceMi1;
    }

    /**
     * @return the distanceKm1
     */
    public String getDistanceKm1() {
        return distanceKm1;
    }

    /**
     * @param distanceKm1
     *            the distanceKm1 to set
     */
    public void setDistanceKm1(String distanceKm1) {
        this.distanceKm1 = distanceKm1;
    }

    /**
     * @return the geoDirection1
     */
    public String getGeoDirection1() {
        return geoDirection1;
    }

    /**
     * @param geoDirection1
     *            the geoDirection1 to set
     */
    public void setGeoDirection1(String geoDirection1) {
        this.geoDirection1 = geoDirection1;
    }

    /**
     * @return the geoReference1
     */
    public String getGeoReference1() {
        return geoReference1;
    }

    /**
     * @param geoReference1
     *            the geoReference1 to set
     */
    public void setGeoReference1(String geoReference1) {
        this.geoReference1 = geoReference1;
    }

    /**
     * @return the distanceMi2
     */
    public String getDistanceMi2() {
        return distanceMi2;
    }

    /**
     * @param distanceMi2
     *            the distanceMi2 to set
     */
    public void setDistanceMi2(String distanceMi2) {
        this.distanceMi2 = distanceMi2;
    }

    /**
     * @return the distanceKm2
     */
    public String getDistanceKm2() {
        return distanceKm2;
    }

    /**
     * @param distanceKm2
     *            the distanceKm2 to set
     */
    public void setDistanceKm2(String distanceKm2) {
        this.distanceKm2 = distanceKm2;
    }

    /**
     * @return the geoDirection2
     */
    public String getGeoDirection2() {
        return geoDirection2;
    }

    /**
     * @param geoDirection2
     *            the geoDirection2 to set
     */
    public void setGeoDirection2(String geoDirection2) {
        this.geoDirection2 = geoDirection2;
    }

    /**
     * @return the geoReference2
     */
    public String getGeoReference2() {
        return geoReference2;
    }

    /**
     * @param geoReference2
     *            the geoReference2 to set
     */
    public void setGeoReference2(String geoReference2) {
        this.geoReference2 = geoReference2;
    }

    /**
     * @return the windMph
     */
    public String getWindMph() {
        return windMph;
    }

    /**
     * @param windMph
     *            the windMph to set
     */
    public void setWindMph(String windMph) {
        this.windMph = windMph;
    }

    /**
     * @return the windKmh
     */
    public String getWindKmh() {
        return windKmh;
    }

    /**
     * @param windKmh
     *            the windKmh to set
     */
    public void setWindKmh(String windKmh) {
        this.windKmh = windKmh;
    }

    /**
     * @return the direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     * @param direction
     *            the direction to set
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * @return the degrees
     */
    public String getDegrees() {
        return degrees;
    }

    /**
     * @param degrees
     *            the degrees to set
     */
    public void setDegrees(String degrees) {
        this.degrees = degrees;
    }

    /**
     * @return the movementKt
     */
    public String getMovementKt() {
        return movementKt;
    }

    /**
     * @param movementKt
     *            the movementKt to set
     */
    public void setMovementKt(String movementKt) {
        this.movementKt = movementKt;
    }

    /**
     * @return the movementMph
     */
    public String getMovementMph() {
        return movementMph;
    }

    /**
     * @param movementMph
     *            the movementMph to set
     */
    public void setMovementMph(String movementMph) {
        this.movementMph = movementMph;
    }

    /**
     * @return the movementKmh
     */
    public String getMovementKmh() {
        return movementKmh;
    }

    /**
     * @param movementKmh
     *            the movementKmh to set
     */
    public void setMovementKmh(String movementKmh) {
        this.movementKmh = movementKmh;
    }

    /**
     * @return the pressureMb
     */
    public String getPressureMb() {
        return pressureMb;
    }

    /**
     * @param pressureMb
     *            the pressureMb to set
     */
    public void setPressureMb(String pressureMb) {
        this.pressureMb = pressureMb;
    }

    /**
     * @return the pressureIn
     */
    public String getPressureIn() {
        return pressureIn;
    }

    /**
     * @param pressureIn
     *            the pressureIn to set
     */
    public void setPressureIn(String pressureIn) {
        this.pressureIn = pressureIn;
    }

    /**
     * @return the isStationary
     */
    public boolean isStationary() {
        return isStationary;
    }

    /**
     * @param isStationary
     *            the isStationary to set
     */
    public void setStationary(boolean isStationary) {
        this.isStationary = isStationary;
    }

    /**
     * Constructs a string representation of class in pre-defined format as in
     * stormId.summary file.
     * 
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Location...%s %s\n", lat, lon));

        if (!geoReference1.isEmpty()) {
            sb.append(
                    String.format("About %s mi...%s km %s of %s\n", distanceMi1,
                distanceKm1, geoDirection1, geoReference1));
        }

        if (!geoReference2.isEmpty()) {
            sb.append(String.format("About %s mi...%s km %s of %s\n",
                    distanceMi2, distanceKm2, geoDirection2, geoReference2));
        }

        sb.append(String.format("Maximum sustained winds...%s mph...%s km/h\n",
                windMph, windKmh));

        if (!isStationary) {
            sb.append(String.format(
                    "Present movement...%s or %s degrees at %s mph...%s km/h\n",
                    direction, degrees, movementMph, movementKmh));
        } else {
            sb.append("Present movement...Stationary\n");
        }

        sb.append(
                String.format("Minimum central pressure...%s mb...%s inches\n",
                        pressureMb, pressureIn));

        return sb.toString();
    }

}
