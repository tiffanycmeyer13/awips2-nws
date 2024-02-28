/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.util.Calendar;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.drawing.DrawingRuntimeException;

/**
 * The Track point class is used to storm the lat/lon location of a storm with
 * its actual date/time.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public class TrackPoint {

    /**
     * Lat, lon coordinate of the storm
     */
    private Coordinate location;

    /**
     * date/time
     */
    private Calendar time;

    /**
     * Constructor to set both location and date/time
     *
     * @param location
     * @param time
     */
    public TrackPoint(Coordinate location, Calendar time) {
        this.location = location;
        this.time = time;
    }

    /**
     * Gets the lat/lon coordinate of the storm
     *
     * @return the storm location
     */
    public Coordinate getLocation() {
        return location;
    }

    /**
     * Sets the lat/lon coordinates of the storm
     *
     * @param storm
     *            location to set
     */
    public void setLocation(Coordinate location) {
        this.location = location;
    }

    /**
     * Gets the date/time at that location
     *
     * @return the date/time
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * Sets the storm location date/time
     *
     * @param time
     *            the time to set
     */
    public void setTime(Calendar time) {
        this.time = time;
    }

    public static TrackPoint clone(Coordinate location, Calendar time) {

        if (location == null) {
            throw new DrawingRuntimeException(
                    "Class: TrackPoint, invalid input paremeter, "
                            + " Coordinate location is NULL");
        }

        Coordinate newCoordinate = new Coordinate();
        newCoordinate.x = location.x;
        newCoordinate.y = location.y;

        Calendar newCalendar = null;
        if (time != null) {
            newCalendar = Calendar.getInstance();
            newCalendar.setTimeInMillis(time.getTimeInMillis());
        }
        return new TrackPoint(newCoordinate, newCalendar);
    }
}
