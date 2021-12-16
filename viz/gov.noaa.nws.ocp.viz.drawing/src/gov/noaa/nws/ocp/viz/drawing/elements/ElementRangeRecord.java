/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

/**
 * Class used to hold the range record for a drawable element.
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
public class ElementRangeRecord {

    public static final double RANGE_OFFSET = 10;

    private List<Coordinate> points;

    private List<Coordinate> extent;

    private boolean closed;

    /**
     * @param extent
     * @param points
     */
    public ElementRangeRecord() {
        super();
        this.extent = new ArrayList<>();
        this.points = new ArrayList<>();
        this.closed = false;
    }

    /**
     * @param extent
     * @param points
     */
    public ElementRangeRecord(List<Coordinate> points, boolean closed) {
        super();
        this.points = new ArrayList<>();
        this.extent = new ArrayList<>();
        if (points != null && !points.isEmpty()) {
            for (Coordinate cc : points) {
                this.points.add(new Coordinate(cc.x, cc.y));
            }

            this.extent.addAll(buildRangeBox(this.points, RANGE_OFFSET));
        }

        this.closed = closed;
    }

    /**
     * @param extent
     * @param points
     */
    public ElementRangeRecord(Coordinate[] points, boolean closed) {
        super();
        this.points = new ArrayList<>();
        this.extent = new ArrayList<>();
        if (points != null && points.length > 0) {
            for (Coordinate cc : points) {
                this.points.add(new Coordinate(cc.x, cc.y));
            }

            this.extent.addAll(buildRangeBox(this.points, RANGE_OFFSET));
        }
        this.closed = closed;
    }

    /**
     * @param extent
     * @param points
     */
    public ElementRangeRecord(List<Coordinate> extent, List<Coordinate> points,
            boolean closed) {
        super();
        this.extent = extent;
        this.points = points;
        this.closed = closed;
    }

    /**
     * @return the extent
     */
    public List<Coordinate> getExtent() {
        return extent;
    }

    /**
     * @return the extent
     */
    public List<Coordinate> getExtentWithoutBuffer() {

        List<Coordinate> rngBox = new ArrayList<>();
        rngBox.add(new Coordinate(this.extent.get(0).x + RANGE_OFFSET,
                this.extent.get(0).y - ElementRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(this.extent.get(1).x - RANGE_OFFSET,
                this.extent.get(1).y - RANGE_OFFSET));
        rngBox.add(new Coordinate(this.extent.get(2).x - RANGE_OFFSET,
                this.extent.get(2).y + RANGE_OFFSET));
        rngBox.add(new Coordinate(this.extent.get(3).x + RANGE_OFFSET,
                this.extent.get(3).y + RANGE_OFFSET));

        return rngBox;

    }

    /**
     * @param extent
     *            the extent to set
     */
    public void setExtent(List<Coordinate> extent) {
        this.extent = extent;
    }

    /**
     * @param extent
     * @param points
     */
    public void setRange(List<Coordinate> extent, List<Coordinate> points,
            boolean closed) {
        this.extent = extent;
        this.points = points;
        this.closed = closed;
    }

    /**
     * @return the points
     */
    public List<Coordinate> getPoints() {
        return points;
    }

    /**
     * @param points
     *            the points to set
     */
    public void setPoints(List<Coordinate> points) {
        this.points = points;
    }

    /**
     * @return the closed flag
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed
     *            the closed to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * @param points
     *            the points to set
     */
    public void setPointsOnly(List<Coordinate> points, boolean closed) {
        if (points != null && !points.isEmpty()) {
            for (Coordinate cc : points) {
                this.points.add(new Coordinate(cc.x, cc.y));
            }

            this.extent.addAll(buildRangeBox(this.points, RANGE_OFFSET));
        }

        this.closed = closed;
    }

    /**
     * @param extent
     * @param points
     */
    public static List<Coordinate> buildRangeBox(List<Coordinate> points,
            double buffer) {

        List<Coordinate> rangeBox = new ArrayList<>();

        // Find the extent in x, y direction.
        double maxX = points.get(0).x;
        double maxY = points.get(0).y;
        double minX = points.get(0).x;
        double minY = points.get(0).y;
        for (Coordinate pp : points) {
            maxX = Math.max(maxX, pp.x);
            minX = Math.min(minX, pp.x);
            maxY = Math.max(maxY, pp.y);
            minY = Math.min(minY, pp.y);
        }

        // Add buffer
        maxX += buffer;
        minX -= buffer;
        maxY += buffer;
        minY -= buffer;

        // Build a rectangle (start from lower_left, go counter clockwise.
        rangeBox.add(new Coordinate(minX, maxY));
        rangeBox.add(new Coordinate(maxX, maxY));
        rangeBox.add(new Coordinate(maxX, minY));
        rangeBox.add(new Coordinate(minX, minY));

        return rangeBox;
    }

    /**
     * @return the max in x direction
     */
    public double getMaxx() {
        if (extent != null && extent.size() > 2) {
            return Math.max(extent.get(0).x, extent.get(2).x);
        } else {
            return Double.NaN;
        }
    }

    /**
     * @return the min in x direction
     */
    public double getMinx() {
        if (extent != null && extent.size() > 2) {
            return Math.min(extent.get(0).x, extent.get(2).x);
        } else {
            return Double.NaN;
        }
    }

    /**
     * @return the max in y direction
     */
    public double getMaxy() {
        if (extent != null && extent.size() > 2) {
            return Math.max(extent.get(0).y, extent.get(2).y);
        } else {
            return Double.NaN;
        }
    }

    /**
     * @return the min in y direction
     */
    public double getMiny() {
        if (extent != null && extent.size() > 2) {
            return Math.min(extent.get(0).y, extent.get(2).y);
        } else {
            return Double.NaN;
        }
    }

    /**
     * Check if this record is within another record.
     */
    public boolean within(ElementRangeRecord rr) {

        return (this.getMaxx() < rr.getMaxx() && this.getMinx() > rr.getMinx()
                && this.getMaxy() < rr.getMaxy()
                && this.getMiny() > rr.getMiny());
    }

    /**
     * Get the farest distance this record can placed within another record.
     */
    public double maxExtention(ElementRangeRecord rr) {
        double maxd = Double.MIN_VALUE;
        for (int ii = 0; ii < extent.size(); ii++) {
            maxd = Math.max(maxd, (extent.get(ii).x - rr.getExtent().get(ii).x)
                    * (extent.get(ii).x - rr.getExtent().get(ii).x)
                    + (extent.get(ii).y - rr.getExtent().get(ii).y)
                    * (extent.get(ii).y - rr.getExtent().get(ii).y));
        }

        return Math.sqrt(maxd);
    }

    /**
     * Deep copy
     */
    public ElementRangeRecord copy() {
        ElementRangeRecord newprr = new ElementRangeRecord();
        if (this.points != null && !this.points.isEmpty()) {
            for (Coordinate cc : this.points) {
                newprr.getPoints().add(new Coordinate(cc.x, cc.y));
            }
        }

        if (this.extent != null && !this.extent.isEmpty()) {
            for (Coordinate cc : this.extent) {
                newprr.getExtent().add(new Coordinate(cc.x, cc.y));
            }
        }

        newprr.closed = closed;

        return newprr;

    }

}
