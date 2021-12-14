/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import java.util.ArrayList;

/*
 * DeletePartCommand
 *
 * Date created: 7 May 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

import gov.noaa.nws.ocp.common.drawing.DrawingException;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.MultiPointElement;

/**
 * Implements a DrawingCommand to delete part of a MultiPointElement.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author B. Yin
 * @version 1.0
 */

public class DeletePartCommand implements DrawingCommand {

    /*
     * layer from which element should be modfified
     */
    private DECollection parent;

    /*
     * drawable element to modified
     */
    private AbstractDrawableComponent element;

    /*
     * elements after deleting
     */
    private AbstractDrawableComponent element1 = null;

    private AbstractDrawableComponent element2 = null;

    /*
     * Two end point and locations of the deleting part
     */
    private Coordinate firstPt;

    private Coordinate secondPt;

    private LocationIndexedLine lil;

    private LinearLocation firstLoc;

    private LinearLocation secondLoc;

    /*
     * flags of deleting types
     */
    private boolean removeAll;

    private boolean removeOneEnd;

    private boolean removeMiddle;

    /**
     * Constructor used to specify the element, part to delete and product list.
     *
     * @param element
     *            - drawable element to delete.
     * @param point1
     *            - the first point of the deleting part
     * @param point2
     *            - the second point of the deleting part
     */
    public DeletePartCommand(Line element, Coordinate point1,
            Coordinate point2) {
        this.element = element;

        GeometryFactory gf = new GeometryFactory();

        /*
         * For each given point, find the location of its closest point on the
         * line. Save order of points along line.
         */
        CoordinateList clist = new CoordinateList(element.getLinePoints());
        if (element.isClosedLine()) {
            clist.closeRing();
        }
        LineString ls = gf.createLineString(clist.toCoordinateArray());
        lil = new LocationIndexedLine(ls);
        LinearLocation loc1 = lil.project(point1);
        LinearLocation loc2 = lil.project(point2);
        if (loc1.compareTo(loc2) <= 0) {
            firstLoc = loc1;
            secondLoc = loc2;
            this.firstPt = point1;
            this.secondPt = point2;
        } else {
            firstLoc = loc2;
            secondLoc = loc1;
            this.firstPt = point2;
            this.secondPt = point1;
        }
    }

    /**
     * Removes the part to be deleted. Saves the layer for possible undo
     *
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#execute()
     * @throws DrawingException
     *             if the element could not be found in the list
     */
    @Override
    public void execute() throws DrawingException {

        parent = (DECollection) element.getParent();
        if (((Line) element).isClosedLine()) {

            deleteClosedPart();

        } else {

            deleteOpenPart();

        }

    }

    /**
     * adds the part to the drawable element back to the original layer
     *
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#undo()
     */
    @Override
    public void undo() throws DrawingException {

        if (((Line) element).isClosedLine()) {
            undeleteClosedPart();
        } else {
            undeleteOpenPart();
        }

    }

    /**
     * removes part from an open MultiPointElement
     */
    private void deleteOpenPart() {

        List<Coordinate> points = element.getPoints();

        if (lil.getStartIndex().compareTo(firstLoc) == 0 && lil.getEndIndex()
                .getSegmentIndex() == secondLoc.getSegmentIndex()) {

            /*
             * Both points selected were endpoints, remove whole element
             */
            removeAll = true;
            parent.removeElement(element);

        } else if (lil.getStartIndex().compareTo(firstLoc) == 0
                || lil.getEndIndex().getSegmentIndex() == secondLoc
                .getSegmentIndex()) {

            /*
             * One point selected was an endpoint, remove part from one end
             */
            removeOneEnd = true;
            element1 = element.copy();
            ArrayList<Coordinate> newPts = new ArrayList<>();

            if (lil.getStartIndex().compareTo(firstLoc) == 0) {
                newPts.add(secondPt);
                newPts.addAll(points.subList(secondLoc.getSegmentIndex() + 1,
                        points.size()));
            } else if (lil.getEndIndex().getSegmentIndex() == secondLoc
                    .getSegmentIndex()) {
                newPts.addAll(
                        points.subList(0, firstLoc.getSegmentIndex() + 1));
                newPts.add(firstPt);
            }

            ((MultiPointElement) element1).setPoints(newPts);

            parent.addElement(element1);
            parent.removeElement(element);

        } else {

            // remove part in the middle of line
            removeMiddle = true;

            // make sure this part does not execute when Re-Do
            if (element1 == null && element2 == null) {
                element1 = element.copy();
                ArrayList<Coordinate> new1 = new ArrayList<>(
                        points.subList(0, firstLoc.getSegmentIndex() + 1));
                new1.add(firstPt);
                ((MultiPointElement) element1).setPoints(new1);

                element2 = element.copy();
                ArrayList<Coordinate> new2 = new ArrayList<>();
                new2.add(secondPt);
                new2.addAll(points.subList(secondLoc.getSegmentIndex() + 1,
                        points.size()));
                ((MultiPointElement) element2).setPoints(new2);
            }

            parent.addElement(element1);
            parent.addElement(element2);
            parent.removeElement(element);

        }
    }

    /**
     * removes part from a closed MultiPointElement
     */
    private void deleteClosedPart() {

        List<Coordinate> points = element.getPoints();
        int pointsBetween = secondLoc.getSegmentIndex()
                - firstLoc.getSegmentIndex();

        if (pointsBetween > points.size() - pointsBetween) {

            // if there are more points between pt1 and pt2, remove the other
            // part.
            element1 = element.copy();

            ((MultiPointElement) element1).setClosed(false);

            element1.getPoints().clear();
            element1.getPoints().add(firstPt);
            element1.getPoints()
            .addAll(points.subList(firstLoc.getSegmentIndex() + 1,
                    secondLoc.getSegmentIndex() + 1));
            element1.getPoints().add(secondPt);

            parent.addElement(element1);
            parent.removeElement(element);

        } else {

            element1 = element.copy();

            ((MultiPointElement) element1).setClosed(false);
            element1.getPoints().clear();
            element1.getPoints().add(secondPt);
            element1.getPoints().addAll(points
                    .subList(secondLoc.getSegmentIndex() + 1, points.size()));
            element1.getPoints()
            .addAll(points.subList(0, firstLoc.getSegmentIndex() + 1));
            element1.getPoints().add(firstPt);

            parent.addElement(element1);
            parent.removeElement(element);

        }
    }

    /**
     * un-deletes part from an open MultiPointElement
     */
    private void undeleteOpenPart() {

        if (removeAll) {

            parent.addElement(element);
            removeAll = false;

        } else if (removeOneEnd) {

            parent.addElement(element);
            parent.removeElement(element1);
            removeOneEnd = false;

        } else if (removeMiddle) {

            parent.removeElement(element1);
            parent.removeElement(element2);
            parent.addElement(element);

            removeMiddle = false;

        }
    }

    /**
     * un-deletes part from an open MultiPointElement
     */
    private void undeleteClosedPart() {

        parent.removeElement(element1);
        parent.addElement(element);

    }

}
