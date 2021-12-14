/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.locationtech.jts.geom.Coordinate;

/**
 * This class is a JAXB XmlAdapter, which converts a Coordinate[] to/from a
 * string of Coordinate pairs, where each pair is separated by a blank " ". Each
 * Coordinate pair uses a comma as the delimiter to separate the first
 * coordinate from the second coordinate.
 *
 * This Adapter can be used by JAXB when marshaling/unmarshaling a Coordinate[]
 * array.
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
public class CoordinateArrayAdapter extends XmlAdapter<String, Coordinate[]> {

    /**
     * Write out the Coordinate[] to a string.
     */
    @Override
    public String marshal(Coordinate[] v) throws Exception {

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < v.length; i++) {
            buffer.append(Double.toString(v[i].x)).append(",")
            .append(Double.toString(v[i].y));
            if (i != (v.length - 1)) {
                buffer.append(' ');
            }
        }

        return buffer.toString();
    }

    /**
     * convert coordinate pairs in a String to Coordinate[] array
     */
    @Override
    public Coordinate[] unmarshal(String v) throws Exception {

        Coordinate[] points = null;

        if (v != null) {
            String[] pairs = v.split(" ");
            points = new Coordinate[pairs.length];

            for (int i = 0; i < pairs.length; i++) {
                String[] value = pairs[i].split(",");
                points[i] = new Coordinate(Double.valueOf(value[0]),
                        Double.valueOf(value[1]));
            }
        }

        return points;
    }
}
