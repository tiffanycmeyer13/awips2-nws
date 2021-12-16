/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.forecast.ForecastTrackDialog;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;
import gov.noaa.nws.ocp.viz.drawing.display.IText.FontStyle;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextJustification;
import gov.noaa.nws.ocp.viz.drawing.display.IText.TextRotation;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.Line;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;
import gov.noaa.nws.ocp.viz.drawing.elements.Text;

/**
 * Mouse handler for ATCF interactive track forecast.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2020 71722      jwu         Initial creation.
 * Jun 10, 2020 78027      jwu         Enhance functionality.
 * Apr 19, 2021 88712      jwu         Revise on NHC feedback.
 * May 27, 2021 91757      jwu         Revise mouse actions on NHC feedback.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ForecastTrackHandler extends DefaultInputHandler {

    /** Editor, dialog, AtcfReosurce, current storm for forecast track. */
    private final AbstractEditor mapEditor;

    private final ForecastTrackDialog forecastTrackDlg;

    private final AtcfResource drawingLayer;

    private final Storm curStorm;

    /** TAUs */
    private final List<AtcfTaus> workingTaus;

    /** Forecast track records for current storm. */
    private final Map<String, List<ForecastTrackRecord>> fcstTrackData;

    /** Track location for all TAUs. */
    private Map<Integer, Coordinate> trackPos;

    /** Current TAU for forecasting. */
    private AtcfTaus currentTau;

    /** Tau where ghost line starts. */
    private int startingTau = 0;

    /** Speed and direction from startingTau to currentTau. */
    private int speed;

    private int direction;

    /** Special forecast between hour 0 and 12. */
    private final TreeMap<Integer, Coordinate> specialFcst;

    /**
     * Constructor
     *
     * @param fcstTrackTool
     *            ForecastTrackTool this handler associated with.
     */
    public ForecastTrackHandler(ForecastTrackTool fcstTrackTool) {

        this.forecastTrackDlg = ForecastTrackTool.getForecastTrackDlg();
        this.mapEditor = fcstTrackTool.getMapEditor();
        this.drawingLayer = fcstTrackTool.getDrawingLayer();
        this.curStorm = drawingLayer.getResourceData().getActiveStorm();
        trackPos = new TreeMap<>();
        workingTaus = forecastTrackDlg.getWorkingTaus();
        fcstTrackData = forecastTrackDlg.getFcstTrkData();
        currentTau = forecastTrackDlg.getCurrentTau();
        specialFcst = new TreeMap<>();


        initialize();
    }

    /**
     * Handle mouse down events - when left mouse is down, update the ghost
     * track line with current mouse location.
     *
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int, int,
     *      int)
     */
    @Override
    public boolean handleMouseDown(Event event) {
        Coordinate loc = handleMouseClick(event.x, event.y, event.button == 1, isShiftDown(event));
        return (loc != null);
    }

    /**
     * Handle mouse down move events
     */
    @Override
    public boolean handleMouseDownMove(Event event) {
        int button = 0;
        if ((event.stateMask & SWT.BUTTON1) != 0) {
            button = 1;
        } else if ((event.stateMask & SWT.BUTTON2) != 0) {
            button = 2;
        } else if ((event.stateMask & SWT.BUTTON3) != 0) {
            button = 3;
        } else if ((event.stateMask & SWT.BUTTON4) != 0) {
            button = 4;
        } else if ((event.stateMask & SWT.BUTTON5) != 0) {
            button = 5;
        }
        return handleMouseDownMove(event.x, event.y, button, isShiftDown(event));
    }

    /**
     * Handle mouse down move events - when left mouse is down move, update the
     * ghost track line with current mouse location.
     *
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseMove(int, int)
     */
    protected boolean handleMouseDownMove(int x, int y, int button, boolean shiftDown) {
        Coordinate loc = handleMouseClick(x, y, button == 1, shiftDown);
        return (loc != null);
    }

    /**
     * Handle mouse move events - when left mouse moves, update the ghost track
     * line with current mouse location.
     *
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseMove(int, int)
     */
    @Override
    public boolean handleMouseMove(Event event) {
        Coordinate loc = handleMouseClick(event.x, event.y, true, isShiftDown(event));
        return (loc != null);
    }

    /**
     * Handle mouse up events. When left mouse is up, update the forecast track
     * with current mouse location & deactivate the tool. When right mouse is
     * up, deactivate the tool without updating the forecast track.
     *
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseUp(int, int, int)
     */
    @Override
    public boolean handleMouseUp(Event event) {

        Coordinate loc = handleMouseClick(event.x, event.y, false, isShiftDown(event));
        if (loc == null) {
            return false;
        }

        /*
         * Left mouse up: update/create forecast track records, save to sandbox
         * & remove tool. Right mouse up: Remove tool.
         */
        if (event.button == 1) {

            forecastTrackDlg.setSaveStatus(true);

            trackPos.put(currentTau.getValue(), loc);

            // Update the location in the existing records.
            updateForecast(loc);

            forecastTrackDlg.redrawForecastTrack();
            forecastTrackDlg.updateTrackInfoList();
            forecastTrackDlg.getFcstTrackTool().removeTool();

            removeGhost();

            return true;

        } else if (event.button == 3) {
            forecastTrackDlg.getFcstTrackTool().removeTool();
            removeGhost();
            return true;
        } else {
            return false;
        }
    }

    /*
     * Initialize data for this handler and draw ghost track.
     */
    private void initialize() {

        Map<String, List<BDeckRecord>> currentBDeckRecords = forecastTrackDlg
                .getCurrentBDeckRecords();

        // Check for special forecast, which should be displayed in ghost.
        for (Map.Entry<String, List<ForecastTrackRecord>> entry : fcstTrackData
                .entrySet()) {
            ForecastTrackRecord rc = entry.getValue().get(0);
            int fcstHr = rc.getFcstHour();
            if (fcstHr > 0 && fcstHr < 12) {
                specialFcst.put(fcstHr,
                        new Coordinate(rc.getClon(), rc.getClat()));
            }
        }

        // Make a copy of all track locations for editing.
        for (AtcfTaus tau : workingTaus) {
            Coordinate pos = null;
            for (List<ForecastTrackRecord> entry : fcstTrackData.values()) {
                ForecastTrackRecord rc = entry.get(0);
                if (tau.getValue() == rc.getFcstHour()) {
                    pos = new Coordinate(AtcfVizUtil.snapToTenth(rc.getClon()),
                            AtcfVizUtil.snapToTenth(rc.getClat()));
                    break;
                }
            }

            trackPos.put(tau.getValue(), pos);
        }

        if (trackPos.get(AtcfTaus.TAU0.getValue()) == null) {
            BDeckRecord brec = currentBDeckRecords
                    .get(forecastTrackDlg.getCurrentDTG()).get(0);
            trackPos.put(AtcfTaus.TAU0.getValue(),
                    new Coordinate(AtcfVizUtil.snapToTenth(brec.getClon()),
                            AtcfVizUtil.snapToTenth(brec.getClat())));
        }

        // Draw the initial ghost track, if any.
        drawGhost(currentTau, trackPos, null);
    }

    /**
     * Draw ghost track for the given TAU.
     *
     * @param tau
     *            the forecasting TAU
     *
     * @param coords
     *            Coordinates of track location for all TAUs.
     *
     * @param curPos
     *            Current forecast position.
     */
    public void drawGhost(AtcfTaus tau, Map<Integer, Coordinate> coords,
            Coordinate curPos) {

        /*
         * Location for the first TAU before current TAU that has forecast.
         * Normally, it should be TAU0 unless we have a special forecasts
         * between TAU0 and TAU 12.
         */
        int indexOfCurrentTau = workingTaus.indexOf(tau);

        Coordinate pos0 = null;
        Integer tau0 = null;
        for (int ii = indexOfCurrentTau - 1; ii >= 0; ii--) {
            tau0 = workingTaus.get(ii).getValue();
            pos0 = coords.get(tau0);
            if (pos0 != null) {
                startingTau = tau0;
                break;
            }
        }

        /*
         * Check if we need to start from special forecast, there are may be
         * more than one special forecasts between 0 and 12.
         */
        if (!specialFcst.isEmpty() && startingTau == 0) {
            startingTau = specialFcst.lastKey();
            pos0 = specialFcst.get(startingTau);
        }

        // Just in case.
        if (pos0 == null) {
            return;
        }

        // Location for current TAU
        Coordinate fcstPt = coords.get(tau.getValue());

        if (curPos != null) {
            fcstPt = curPos;
        }

        if (fcstPt != null) {
            drawingLayer.removeGhost();

            int hours = tau.getValue() - startingTau;
            DECollection ghost = createGhostTrack(pos0, fcstPt, hours);
            drawingLayer.setGhost(ghost);
        }

        mapEditor.refresh();
    }

    /*
     * Create graphic elements for the ghost track.
     *
     * @param pos0 Starting position to calculate speed/dir
     *
     * @param fcstPos Forecast position
     *
     * @param hours hours between starting point & forecast position.
     *
     */
    private DECollection createGhostTrack(Coordinate pos0, Coordinate fcstPos,
            int hours) {

        DECollection ghost = new DECollection();

        Color ghostColor = Color.WHITE;

        List<Coordinate> ghostTrackloc = new ArrayList<>();
        for (AtcfTaus tau : workingTaus) {
            int fcstHr = tau.getValue();
            Coordinate pos = trackPos.get(fcstHr);
            if (tau == currentTau) {
                pos = fcstPos;
            }

            // Insert special forecast position.
            if (fcstHr > 0 && fcstHr <= 12 && !specialFcst.isEmpty()) {
                ghostTrackloc.addAll(specialFcst.values());
            }

            if (pos != null) {
                ghostTrackloc.add(pos);
            }
        }

        // Draw track line & text box to show location/speed/direction
        Line gline = new Line(null, new Color[] { ghostColor }, 2.0f, 1.5,
                false, false, ghostTrackloc, 2, null, "Lines", "LINE_SOLID");

        ghost.add(gline);

        // Add a text box to show lat/lon/spd/dir
        double[] distDir = AtcfVizUtil.getDistNDir(pos0, fcstPos);
        speed = (int) Math.round((distDir[0] / AtcfVizUtil.NM2M / hours));
        direction = (int) distDir[1];

        Text ghostTextBox = createGhostTextBox(fcstPos, currentTau, speed,
                direction);
        ghost.add(ghostTextBox);

        // Draw a filled circle at current location
        Symbol sym = new Symbol(null, new Color[] { ghostColor }, 2.5f, 0.8,
                false, fcstPos, "Symbol", "FILLED_CIRCLE");
        ghost.add(sym);

        return ghost;
    }

    /*
     * Create a new ForecastRecord with info from a given record.
     *
     * Note: Max wind, gust, and storm development is set later via Forecast
     * Intensity.
     *
     * @param rec ForecastTrackRecord as base record
     *
     * @param lat Latitude of the new record.
     *
     * @param lon Longitude of the new record.
     *
     * @param fcstHr Forecast hour of the new record.
     *
     * @param speed Storm speed of the new record.
     *
     * @param direction Storm Direction of the new record.
     *
     * @return ForecastTrackRecord
     */
    private ForecastTrackRecord makeFcstTrackRecord(ForecastTrackRecord rec,
            double lat, double lon, int fcstHr, int speed, int direction) {

        ForecastTrackRecord frec = new ForecastTrackRecord();

        // Set info derived from GUI.
        frec.setClat((float) lat);
        frec.setClon((float) lon);
        frec.setFcstHour(fcstHr);
        frec.setStormSped(speed);
        frec.setStormDrct(direction);

        // Set other info.
        frec.setBasin(rec.getBasin());
        frec.setRefTime(rec.getRefTime());
        frec.setStormName(rec.getStormName());
        frec.setCycloneNum(rec.getCycloneNum());
        frec.setYear(rec.getYear());
        frec.setForecaster(rec.getForecaster());

        frec.setRadWind(34);
        frec.setRadWindQuad("NEQ");

        frec.setReportType(rec.getReportType());

        frec.setSubRegion(rec.getSubRegion());
        frec.setTechnique("OFCL");
        frec.setTechniqueNum(3);

        return frec;
    }

    /*
     * Create a Text box to show forecast hour, lat/lon/spd/dir for the forecast
     * TAU.
     *
     * @param pos Coordinate for forecasting TAU
     *
     * @param tau The TAU in forecasting
     *
     * @param spd Storm speed
     *
     * @param dir Storm direction
     *
     * @return Text
     */
    private Text createGhostTextBox(Coordinate pos, AtcfTaus tau, int spd,
            int dir) {

        String[] text = new String[2];
        text[0] = String.format(" %3dz %4.1f%2s, %5.1f%2s ", tau.getValue(),
                Math.abs(pos.y), (pos.y < 0) ? "S" : "N", Math.abs(pos.x),
                        (pos.x > 0) ? "E" : "W");
        text[1] = String.format(" %3d kts at %3d deg ", spd, dir);

        return new Text(null, "Courier", 12.0f, TextJustification.CENTER, pos,
                0.0, TextRotation.SCREEN_RELATIVE, text, FontStyle.REGULAR,
                Color.WHITE, 6, 6, true, DisplayType.BOX, "Text",
                "General Text");
    }

    /**
     * @param trackPos
     *            the trackPos to set
     */
    public void setTrackPos(Map<Integer, Coordinate> trackPos) {
        this.trackPos = trackPos;
    }

    /**
     * @param currentTau
     *            the currentTau to set
     */
    public void setCurrentTau(AtcfTaus currentTau) {
        this.currentTau = currentTau;
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
     * Update forecast - new records may be created and interpolated .
     *
     * @param newLoc Coordinate of the new forecast position.
     */
    private void updateForecast(Coordinate newLoc) {
        String dtg = AtcfDataUtil.getNewDTG(forecastTrackDlg.getCurrentDTG(),
                currentTau.getValue());

        List<ForecastTrackRecord> records = fcstTrackData.get(dtg);
        if (records == null || records.isEmpty()) {
            List<ForecastTrackRecord> recs = fcstTrackData
                    .get(forecastTrackDlg.getCurrentDTG());

            ForecastTrackRecord frec = makeFcstTrackRecord(recs.get(0),
                    newLoc.y, newLoc.x, currentTau.getValue(), speed,
                    direction);

            List<ForecastTrackRecord> recList = new ArrayList<>();
            recList.add(frec);
            fcstTrackData.put(dtg, recList);

            trackPos.put(currentTau.getValue(), newLoc);

            /*
             * Check if there are any hours between startingTau and curentTau
             * that have no forecast yet. If so, interpolate and create records.
             */
            int start = 0;
            int indx = 0;
            for (AtcfTaus tau : workingTaus) {
                if (tau.getValue() == startingTau) {
                    start = indx;
                    break;
                }

                indx++;
            }

            int end = workingTaus.indexOf(currentTau);
            int num = end - start - 1;
            if (num > 0) {
                Coordinate startPos = trackPos.get(startingTau);
                if (startingTau > 0 && startingTau < 12) {
                    startPos = specialFcst.get(startingTau);
                }

                for (int ii = start + 1; ii < end; ii++) {
                    int nfcstHr = workingTaus.get(ii).getValue();

                    ForecastTrackRecord nrec = new ForecastTrackRecord(frec);
                    nrec.setFcstHour(nfcstHr);

                    // Extrapolate location from starting location.
                    double dist = ((double) speed) * (nfcstHr - startingTau);
                    Coordinate interpLoc = AtcfVizUtil.computePoint(startPos,
                            (float) dist, direction);
                    interpLoc.x = AtcfVizUtil.snapToTenth(interpLoc.x);
                    interpLoc.y = AtcfVizUtil.snapToTenth(interpLoc.y);
                    nrec.setClat((float) interpLoc.y);
                    nrec.setClon((float) interpLoc.x);
                    nrec.setStormSped(speed);
                    nrec.setStormDrct(direction);

                    List<ForecastTrackRecord> nrecList = new ArrayList<>();
                    nrecList.add(nrec);

                    String ndtg = AtcfDataUtil.getNewDTG(
                            forecastTrackDlg.getCurrentDTG(), nfcstHr);

                    fcstTrackData.put(ndtg, nrecList);
                    trackPos.put(workingTaus.get(ii).getValue(), interpLoc);
                }
            }

        } else {
            for (ForecastTrackRecord rec : records) {
                rec.setClon((float) newLoc.x);
                rec.setClat((float) newLoc.y);
                rec.setStormSped(speed);
                rec.setStormDrct(direction);
            }
        }
    }

    /*
     * Remove ghost track from displaying.
     */
    private void removeGhost() {
        drawingLayer.removeGhost();
        mapEditor.refresh();
    }

    /*
     * Handle Mouse clicks - when left mouse is down or down move, or mouse
     * move, update the ghost line with current mouse location. The actual track
     * is updated when left mouse is up, which is handled separately.
     *
     * @param x X-coordinate of mouse location
     *
     * @param y Y-coordinate of mouse location
     *
     * @param drawGhost Flag to update ghost (use true when left mouse down or
     * down move, or mouse move)
     *
     * @param shiftDown True if the shift key is pressed
     *
     * @return Coordinate Mouse location (A null value means the caller should
     * return false and a non-null value means the caller should return true or
     * continue for further processing.
     */
    private Coordinate handleMouseClick(int x, int y, boolean drawGhost,
            boolean shiftDown) {

        if (forecastTrackDlg == null || !forecastTrackDlg.isForecast()) {
            return null;
        }

        forecastTrackDlg.bringToTop();

        // Get mouse location.
        Coordinate loc = translateToLatLon(x, y);

        /*
         * When mouse is not in geographic area or "Shift" is holding down,
         * return "null" to indicate that the control needs to be returned to
         * other handlers.
         */
        if (loc == null || shiftDown) {
            return null;
        }

        // Ghost the track line with current mouse location.
        if (drawGhost) {
            drawGhost(currentTau, trackPos, loc);
        }

        return loc;
    }

}
