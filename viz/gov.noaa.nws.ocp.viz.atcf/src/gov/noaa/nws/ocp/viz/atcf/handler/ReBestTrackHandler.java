/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Event;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.BDeckRecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.track.ReBestTrackDialog;
import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;
import gov.noaa.nws.ocp.viz.drawing.display.IText.FontStyle;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextJustification;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextRotation;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;

/**
 * Mouse handler to modify ATCF best track positions interactively
 * (Track=>ReBest)
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2020 79573      jwu         Initial creation.
 * May 17, 2021 91567      jwu         Update mouse behavior.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ReBestTrackHandler extends DefaultInputHandler {

    // Editor, dialog, AtcfReosurce, current storm for forecast track.
    private AbstractEditor mapEditor;

    private ReBestTrackDialog reBestTrackDlg;

    private AtcfResource drawingLayer;

    private Storm curStorm;

    // Current storm, and seleted DTG in the list.
    private String selectedDTG;

    // Actual DTGs.
    private List<String> actualDTGs;

    private Map<BDeckRecordKey, BDeckRecord> currentBDeckRecordMap;

    private Map<BDeckRecordKey, BDeckRecord> modifiedBDeckRecordMap;

    // Color for drawing ghost track.
    private Color unSavedTrackColor = Color.PINK;

    private Color currentTrackColor = Color.WHITE;

    /**
     * Constructor
     *
     * @param reBestTrackTool
     *            ReBestTrackTool this handler associated with.
     */
    public ReBestTrackHandler(ReBestTrackTool reBestTrackTool) {

        this.reBestTrackDlg = ReBestTrackTool.getReBestTrackDlg();
        this.mapEditor = reBestTrackTool.getMapEditor();
        this.drawingLayer = reBestTrackTool.getDrawingLayer();
        this.curStorm = drawingLayer.getResourceData().getActiveStorm();

        initialize();
    }

    /**
     * Handle mouse down events.
     *
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int, int,
     *      int)
     */
    @Override
    public boolean handleMouseDown(Event event) {

        if (isShiftDown(event)) {
            return false;
        }

        // Check mouse location.
        Coordinate loc = checkMouseLocation(event.x, event.y);
        if (loc == null) {
            return false;
        }

        // Left mouse click - update track position & exits.
        if (event.button == 1) {
            updateTrackPos(loc);
            reBestTrackDlg.updateDTGStatus(selectedDTG);
            reBestTrackDlg.updateChangeStatus(true);
            reBestTrackDlg.populateInfoTable();

            // Exits from the tool.
            exitTool();

            return true;
        } else if (event.button == 3) {
            // Right mouse click - exits.
            exitTool();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handle mouse move events.
     *
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseMove(int, int)
     */
    @Override
    public boolean handleMouseMove(Event event) {

        if (isShiftDown(event)) {
            return false;
        }

        // Get mouse location.
        Coordinate loc = checkMouseLocation(event.x, event.y);

        // Update the ghost
        if (loc != null) {
            List<Coordinate> ghostPts = buildFullGhostLine(selectedDTG, loc);

            // Ghost for current DTG
            List<Coordinate> changedPts = new ArrayList<>();
            changedPts.add(loc);
            DECollection ghost = createGhostLine(ghostPts, changedPts,
                    currentTrackColor, true);

            // Add a text box to indicate the current location.
            String[] text = new String[2];
            text[0] = " Right click to cancel ";
            String locStr = String.format("%5.1f%2s, %6.1f%2s", Math.abs(loc.y),
                    (loc.y < 0) ? "S" : "N", Math.abs(loc.x),
                            (loc.x > 0) ? "E" : "W");
            text[1] = locStr;

            Text textBox = new Text(null, "Courier", 12.0f,
                    TextJustification.CENTER, loc, 0.0,
                    TextRotation.SCREEN_RELATIVE, text, FontStyle.REGULAR,
                    currentTrackColor, 6, 6, true, DisplayType.BOX, "Text",
                    "General Text");
            ghost.add(textBox);

            // Ghost for all other modified DTGs
            ghost.add(createModifiedTrackLine());

            drawingLayer.removeGhost();
            drawingLayer.setGhost(ghost);

            mapEditor.refresh();

            return true;
        }

        return false;
    }

    /*
     * Check if the mouse location is valid
     *
     * @param x
     *
     * @param y
     *
     * @return Coordinate Mouse location (null if not valid)
     *
     */
    private Coordinate checkMouseLocation(int x, int y) {

        Coordinate loc = null;

        if (reBestTrackDlg != null) {

            reBestTrackDlg.bringToTop();

            // Get mouse location.
            loc = translateToLatLon(x, y);
            reBestTrackDlg.showLatLon(loc);
        }

        return loc;
    }

    /*
     * Initialize data for this handler and draw ghost track for all modified &
     * unsaved track positions.
     */
    private void initialize() {

        selectedDTG = reBestTrackDlg.getSelectedDTG();
        actualDTGs = reBestTrackDlg.getActualDTGs();

        currentBDeckRecordMap = reBestTrackDlg.getCurrentBDeckRecordMap();
        modifiedBDeckRecordMap = reBestTrackDlg.getModifiedBDeckRecordMap();

        // Draw the initial ghost track, if any.
        redrawGhost();
    }

    /*
     * Build a full ghost line for a selected DTG when its track position is
     * modified.
     *
     * @param dtg DTG currently selected
     *
     * @param newLoc Coordinate to be for the selection DTG
     *
     * @return List<Coordinate>
     */
    private List<Coordinate> buildFullGhostLine(String dtg,
            Coordinate newLoc) {

        List<Coordinate> pts = new ArrayList<>();
        if (newLoc == null) {
            return pts;
        }

        for (String dt : actualDTGs) {

            if (dt.equals(dtg)) {
                pts.add(newLoc);
            } else {
                BDeckRecord rec = getWorkingRecord(dt);
                if (rec != null) {
                    pts.add(new Coordinate(rec.getClon(), rec.getClat()));
                }
            }
        }

        return pts;
    }

    /*
     * Update the current DTG's track positions in the record to the given
     * location.
     *
     * @param loc The new location.
     */
    private void updateTrackPos(Coordinate loc) {
        for (WindRadii rad : WindRadii.values()) {
            int radii = rad.getValue();
            if (radii > 0) {
                BDeckRecordKey bk = new BDeckRecordKey(selectedDTG, radii);
                BDeckRecord mrec = modifiedBDeckRecordMap.get(bk);

                // Update if it is modified before.
                if (mrec != null) {
                    mrec.setClat((float) loc.y);
                    mrec.setClon((float) loc.x);
                } else {
                    BDeckRecord orec = currentBDeckRecordMap.get(bk);
                    /*
                     * Store into modifiedBDeckRecordMap & update if it not
                     * modified before.
                     */
                    if (orec != null) {
                        mrec = new BDeckRecord(orec);
                        mrec.setClat((float) loc.y);
                        mrec.setClon((float) loc.x);
                        modifiedBDeckRecordMap.put(bk, mrec);
                    } else {
                        /*
                         * Create a new record & update if the selectDTG is new
                         * and has no records yet.
                         */
                        if (rad == WindRadii.RADII_34_KNOT) { // Create
                            List<BDeckRecord> recs = reBestTrackDlg
                                    .createNewRecords(selectedDTG);

                            for (BDeckRecord rec : recs) {
                                rec.setClat((float) loc.y);
                                rec.setClon((float) loc.x);
                                BDeckRecordKey bkey = new BDeckRecordKey(
                                        selectedDTG, (int) rec.getRadWind());
                                modifiedBDeckRecordMap.put(bkey, rec);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Redraw ghost track for all modified DTGs.
     */
    public void redrawGhost() {
        DECollection ghost = createModifiedTrackLine();

        drawingLayer.removeGhost();
        drawingLayer.setGhost(ghost);

        mapEditor.refresh();
    }

    /*
     * Create a full ghost track graphics, including modified DTGs.
     *
     * @return DECollection Collection of graphic elements
     */
    private DECollection createModifiedTrackLine() {

        DECollection ghost = new DECollection();

        List<Coordinate> mline = new ArrayList<>();
        List<Coordinate> modified = new ArrayList<>();
        for (String dtg : actualDTGs) {
            BDeckRecordKey rkey = new BDeckRecordKey(dtg,
                    WindRadii.RADII_34_KNOT.getValue());
            BDeckRecord rec = modifiedBDeckRecordMap.get(rkey);

            Coordinate pt;
            if (rec != null) {
                pt = new Coordinate(rec.getClon(), rec.getClat());
                modified.add(pt);
                mline.add(pt);
            } else {
                rec = currentBDeckRecordMap.get(rkey);
                if (rec != null) {
                    mline.add(new Coordinate(rec.getClon(), rec.getClat()));
                }
            }
        }

        if (!modified.isEmpty()) {
            ghost.add(
                    createGhostLine(mline, modified, unSavedTrackColor, true));
        }

        return ghost;
    }

    /**
     * @return the drawingLayer
     */
    public AtcfResource getDrawingLayer() {
        return drawingLayer;
    }

    /**
     * @return the curStorm
     */
    public Storm getCurStorm() {
        return curStorm;
    }

    /**
     * @return the selectedDTG
     */
    public String getSelectedDTG() {
        return selectedDTG;
    }

    /*
     * Translate a mouse click to lat/lon and round to tenth place.
     *
     * @param x
     *
     * @param y
     *
     * @return loc Coordinate
     */
    private Coordinate translateToLatLon(double x, double y) {

        // Get mouse location.
        Coordinate loc = mapEditor.translateClick(x, y);

        if (loc != null) {
            loc.x = AtcfVizUtil.snapToTenth(loc.x);
            loc.y = AtcfVizUtil.snapToTenth(loc.y);
        }

        return loc;
    }

    /*
     * Create graphic elements for a ghost track.
     *
     * @param locs List of coordinates
     *
     * @param changedLocs List of changed coordinates
     *
     * @param color Color for the graphic.
     *
     * @param mark Flag to draw markers at locations.
     *
     * @return DECollection Collection of graphic elements
     */
    private DECollection createGhostLine(List<Coordinate> locs,
            List<Coordinate> changedLocs, Color clr, boolean mark) {
        DECollection ghost = new DECollection();

        if (locs.size() > 1) {
            Line gline = new Line(null, new Color[] { clr }, 2.0f, 1.5, false,
                    false, locs, 2, null, "Lines", "LINE_SOLID");

            ghost.add(gline);
        }

        // Add marker at each location
        if (mark && changedLocs != null) {
            for (Coordinate coord : changedLocs) {
                Symbol sym = new Symbol(null, new Color[] { clr }, 1.5f, 0.5,
                        false, coord, "Marker", "SQUARE");
                ghost.add(sym);
            }
        }

        return ghost;
    }

    /*
     * Find a working record by DTG at 34KT.
     *
     * @param dtg DTG
     *
     * @return BDeckRecord
     */
    private BDeckRecord getWorkingRecord(String dtg) {

        BDeckRecordKey rkey = new BDeckRecordKey(dtg,
                WindRadii.RADII_34_KNOT.getValue());
        BDeckRecord rec = modifiedBDeckRecordMap.get(rkey);
        if (rec == null) {
            rec = currentBDeckRecordMap.get(rkey);
        }

        return rec;
    }

    /*
     * Exits from the ReBest tool.
     */
    private void exitTool() {

        redrawGhost();

        if (reBestTrackDlg != null) {
            reBestTrackDlg.bringToTop();
            reBestTrackDlg.showLatLon(null);
            reBestTrackDlg.getReBestTrackTool().removeTool();
        }
    }

}
