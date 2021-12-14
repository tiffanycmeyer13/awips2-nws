/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

/**
 * Drawing utilities.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class DrawingUtil {

    /*
     * Statute miles to meters
     */
    public static final float SM2M = 1609.34f;

    /*
     * Nautical miles to meters
     */
    public static final float NM2M = 1852.0f;

    /** Factory */
    private static GeometryFactory geometryFactory = new GeometryFactory();

    // Private constructor
    private DrawingUtil() {
    }

    /**
     * Converts an array of lat/lons to pixel coordinates
     *
     * @param pts
     *            An array of points in lat/lon coordinates
     * @param mapDescriptor
     *            Descriptor to use for world to pixel transform
     * @return The array of points in pixel coordinates
     */
    public static final double[][] latlonToPixel(Coordinate[] pts,
            IMapDescriptor mapDescriptor) {
        double[] point = new double[3];
        double[][] pixels = new double[pts.length][3];
        double[] pixel;
        for (int i = 0; i < pts.length; i++) {
            point[0] = pts[i].x;
            point[1] = pts[i].y;
            point[2] = 0.0;
            pixel = mapDescriptor.worldToPixel(point);
            if (pixel != null) {
                pixels[i] = pixel;
            }
        }

        return pixels;
    }

    /**
     * Converts an array of point in pixel coordinates to lat/lons
     *
     * @param pixels
     *            An array of points in pixel coordinates
     * @param mapDescriptor
     *            Descriptor to use for world to pixel transform
     * @return The array list of points in lat/lon coordinates
     */
    public static final List<Coordinate> pixelToLatlon(double[][] pixels,
            IMapDescriptor mapDescriptor) {
        ArrayList<Coordinate> crd = new ArrayList<>();

        for (double[] pixel : pixels) {
            double[] pt = mapDescriptor.pixelToWorld(pixel);
            crd.add(new Coordinate(pt[0], pt[1]));
        }

        return crd;
    }

    /**
     * Gets the current site id from the localization Manager
     *
     * @return
     */
    public static String getCurrentOffice() {

        String wfo = LocalizationManager.getInstance().getCurrentSite();
        if ("none".equalsIgnoreCase(wfo) || wfo.isEmpty()) {
            wfo = "KNHC";
        }

        return wfo;
    }

    /**
     * This method checks if the input character is digit or delete or
     * backspace.
     *
     * @param event
     *            - Event that activates the listener.
     * @return - true if the input character is digit or delete or backspace.
     */
    public static final boolean validateDigitInput(VerifyEvent event) {
        return (Character.isDigit(event.character)
                || Character.UNASSIGNED == event.character
                || event.character == SWT.BS || event.character == SWT.DEL);
    }

    /**
     * This method checks if a time string is between 0000 and 2359.
     *
     * @param String
     *            - time string.
     * @return - true if the time string is between 0000 and 2359
     */
    public static final boolean validateUTCTime(String utcTime) {

        int time = 0;
        try {
            time = Integer.parseInt(utcTime);
        } catch (NumberFormatException e) {
            return false;
        }

        int hour = time / 100;
        int minute = time % 100;

        return (hour >= 0 && hour <= 23 && minute >= 00 && minute <= 59);
    }

    /**
     * Generate a JTS LineString from a set of points.
     *
     * @param points
     *            array of points
     * @return
     */
    public static Geometry pointsToLineString(Coordinate[] points) {

        CoordinateArraySequence cas = new CoordinateArraySequence(points);

        return new LineString(cas, geometryFactory);
    }

}
