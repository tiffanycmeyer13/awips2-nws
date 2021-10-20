/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.WindQuadrant;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsGenerator;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsProperties;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesProperties;
import gov.noaa.nws.ocp.viz.atcf.fixes.DrawFixesElements;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResourceData;

/**
 * Utilities for ATCF UI.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 22, 2018 34955      jwu         Initial creation.
 * Mar 14, 2018 34955      dfriedman   Changed to use IContributionManagerOverrides.
 * Jun 05, 2018 48178      jwu         add methods to handle AtcfResource
 * Jun 28, 2018 51961      jwu         Test drawing B-Deck
 * Oct 03, 2018 55873      jwu         Add getSidebarCommands()
 * Feb 19, 2019 60097      jwu         Add new drawBDeck().
 * Mar 28, 2019 61882      jwu         Add executeCommand() & adjustWindQuadrant().
 * Apr 16, 2019 62175      dmanzella   Copied support functionality from PshUtil for finding radii
 * Jul 30, 2019 66618      dfriedman   Remove embedded menu bar.
 * Sep 10, 2019 68279      dfriedman   Fix aidCode logic error.
 * Oct 02, 2019 68738      dmanzella   Add some methods for fDeck
 * Nov 13, 2019 71089      jwu         Add computePoint().
 * Jan 06, 2020 71722      jwu         Add getDistNDir().
 * May 28, 2020 78027      jwu         Add interpolateRecord().
 * Jul 13, 2020 79573      jwu         Add more methods.
 * Mar 24, 2021 88727      mporricelli Add checkWindRadii(), getRadiiValue()
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class AtcfVizUtil {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(AtcfVizUtil.class);

    // The ATCF context
    private static String atcfContextName = "gov.noaa.nws.ocp.viz.atcf.atcfContext";

    public static final String EMBED_INTO_MAIN_MENU_PROPERTY_NAME = "gov.noaa.nws.ocp.viz.atcf.embedIntoMainMenu";

    private static IContextActivation atcfContextActivation;

    // Extension point to find defined commands
    private static final String COMMNAD_EXTENSION_POINT = "org.eclipse.ui.commands";

    private static final String SIDEBAR_COMMANDS_CATEGORY = "gov.noaa.nws.ocp.viz.atcf.commands.sidebar";

    // Parameter name to pass the actual action name to activate command.
    public static final String SIDEBAR_COMMAND_NAME = "sidebarCommandName";

    private static final double MAP_CONSTANT = 0.0006213712;

    // Constant for converting knots to mph (~1.150779 --- nhc_writeadv.f)
    public static final double NAUTICAL_MILES_TO_MILES = 57875. / 50292.;

    // Constant for invalid values in text
    public static final float INVALID_FLOAT = 999999.0f;

    // Constant for invalid values in text
    public static final int INVALID_INT = 999_999;

    // Nautical miles to meters
    public static final float NM2M = 1852.0f;

    // Mbars to inches Hg
    public static final float MB2IN = 0.0295333727f;

    // Max wind speed for ATCF
    public static final int MAX_WIND_SPEED = 250;

    // Max wind radius for ATCF
    public static final int MAX_WIND_RADIUS = 995;

    /**
     * Path for icons.
     */
    private static final String ICONS = "icons/";

    // Map of all registered ATCF sidebar commands with their id as the key
    private static HashMap<String, String> sidebarCommandMap = null;

    private AtcfVizUtil() {

    }

    /**
     * Activate the ATCF context
     */
    public static synchronized void activateAtcfContext() {
        if (atcfContextActivation == null) {
            IContextService ctxSvc = PlatformUI.getWorkbench()
                    .getService(IContextService.class);
            atcfContextActivation = ctxSvc.activateContext(atcfContextName);
        }
    }

    /**
     * Deactivate the ATCF context
     */
    public static synchronized void deactivateAtcfContext() {
        if (atcfContextActivation != null) {
            IContextService ctxSvc = PlatformUI.getWorkbench()
                    .getService(IContextService.class);
            ctxSvc.deactivateContext(atcfContextActivation);
            atcfContextActivation = null;
        }
    }

    /**
     * Execute a command with a parameter.
     *
     * @param commendId
     *            Command ID defined in CAVE.
     * @param paramName
     *            Parameter name
     * @param paramValue
     *            Parameter value
     *
     */
    public static void executeCommand(String commandId, String paramName,
            String paramValue) {

        IEditorPart editor = EditorUtil.getActiveEditor();
        ICommandService commandService = editor.getSite()
                .getService(ICommandService.class);

        Command command = commandService.getCommand(commandId);

        if (command != null) {

            try {
                HashMap<String, Object> params = new HashMap<>();
                if (paramName != null && paramValue != null) {
                    params.put(paramName, paramValue);
                }

                ExecutionEvent exec = new ExecutionEvent(command, params, null,
                        null);
                command.executeWithChecks(exec);
            } catch (Exception e) {
                handler.warn("AtcfVizUtil: cannot execute command " + commandId,
                        e);
            }
        }

    }

    /**
     * Execute a command with a parameter.
     *
     * @param commendId
     *            Command ID defined in CAVE.
     * @param paramName
     *            Parameter name
     * @param paramValue
     *            Parameter value
     *
     */
    public static void executeParameterizedCommand(String commandId,
            String paramName, String paramValue) {

        IEditorPart editor = EditorUtil.getActiveEditor();
        IHandlerService handlerService = editor.getSite()
                .getService(IHandlerService.class);
        ICommandService commandService = editor.getSite()
                .getService(ICommandService.class);

        Command command = commandService.getCommand(commandId);
        if (command == null) {
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        if (paramName != null && paramValue != null) {
            parameters.put(paramName, paramValue);
        }

        ParameterizedCommand parameterizedCommand = ParameterizedCommand
                .generateCommand(command, parameters);

        try {
            handlerService.executeCommand(parameterizedCommand, null);
        } catch (Exception e) {
            handler.warn("AtcfVizUtil: cannot execute command " + commandId, e);
        }
    }

    /**
     * Get a reference to the current editor, if it is a AbstractEditor.
     */
    public static final AbstractEditor getActiveEditor() {
        if (EditorUtil.getActiveEditor() instanceof AbstractEditor) {
            return (AbstractEditor) EditorUtil.getActiveEditor();
        } else {
            return null;
        }
    }

    /**
     * Create a new AtcfResource and add it to the current editor.
     *
     * @return the AtcfResource
     */
    public static final AtcfResource createNewResource() {

        AtcfResource drawingLayer = null;

        AbstractEditor editor = getActiveEditor();
        if (editor != null) {
            try {
                AtcfSession.getInstance().addEditor(editor);

                /*
                 * Use existing (or new) AtcfResourceData to construct new
                 * Resources to add to each Pane's ResourceList
                 */
                AtcfResourceData rscData = new AtcfResourceData();

                for (IDisplayPane pane : editor.getDisplayPanes()) {
                    IDescriptor idesc = pane.getDescriptor();
                    if (!idesc.getResourceList().isEmpty()) {
                        drawingLayer = rscData.construct(new LoadProperties(),
                                idesc);
                        idesc.getResourceList().add(drawingLayer);
                        idesc.getResourceList()
                        .addPreRemoveListener(drawingLayer);
                        drawingLayer.init(pane.getTarget());
                    }
                }
            } catch (Exception e) {
                handler.warn("AtcfVizUtil: cannot create ATCF resource ", e);
            }
        }
        return drawingLayer;
    }

    /**
     * Check the given editor for a AtcfResource. If editor is null then the
     * current CAVE Editor is used. If found, return it.
     *
     * @param editor
     * @return reference to a PgenResource
     */
    public static final AtcfResource findAtcfResource(AbstractEditor editor) {
        return findResource(AtcfResource.class, editor);
    }

    /**
     * Find the given resource in an editor.
     *
     * @param rscClass
     * @param aEdit
     * @return AbstractVizResource
     */
    public static final <T extends AbstractVizResource<?, ?>> T findResource(
            Class<T> rscClass, AbstractEditor aEdit) {

        AbstractEditor editor = (aEdit != null ? aEdit : getActiveEditor());
        if (editor == null) {
            return null;
        }

        IRenderableDisplay disp = editor.getActiveDisplayPane()
                .getRenderableDisplay();

        if (disp == null) {
            return null;
        }

        ResourceList rscList = disp.getDescriptor().getResourceList();

        for (ResourcePair rp : rscList) {
            AbstractVizResource<?, ?> rsc = rp.getResource();

            if (rsc != null && rsc.getClass() == rscClass) {
                return rscClass.cast(rsc);
            }
        }

        return null;
    }

    /**
     * Create an image from a given file.
     *
     * @param imgFile
     *            Filename for the image
     *
     * @return Image Image created from the file
     */
    public static Image createImage(String imgFile) {

        ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, ICONS + imgFile);

        Image img = null;
        if (id != null) {
            img = id.createImage();
        }

        return img;
    }

    /**
     * Get a list from registry of all commands and then create a hash map of
     * the registered ATCF sidebar commands SIDEBAR_COMMANDS_CATEGORY using the
     * command's id attribute as the key.
     */
    public static synchronized Map<String, String> getSidebarCommands() {

        if (sidebarCommandMap == null) {

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint epoint = registry
                    .getExtensionPoint(COMMNAD_EXTENSION_POINT);
            IConfigurationElement[] atcfElements = epoint
                    .getConfigurationElements();

            sidebarCommandMap = new LinkedHashMap<>(atcfElements.length);

            for (IConfigurationElement atcfElement : atcfElements) {
                String cat = atcfElement.getAttribute("categoryId");
                String id = atcfElement.getAttribute("id");
                if (cat != null && cat.equals(SIDEBAR_COMMANDS_CATEGORY)) {
                    sidebarCommandMap.put(atcfElement.getAttribute("name"),
                            id);
                }
            }

        }

        return sidebarCommandMap;

    }

    /**
     * Adjust wind quadrants.
     *
     * @param windCode
     *            wind code
     *
     * @param q1
     *            wind quadrants 1
     *
     * @param q2
     *            wind quadrants 2
     *
     * @param q3
     *            wind quadrants 2
     *
     * @param q3
     *            wind quadrants 4
     *
     * @return float[] adjusted wind quadrants
     */
    public static float[] adjustWindQuadrants(String windCode, float q1,
            float q2, float q3, float q4) {
        if (q1 > 999) {
            q1 = 0.0f;
        }
        if (q2 > 999) {
            q2 = 0.0f;
        }
        if (q3 > 999) {
            q3 = 0.0f;
        }
        if (q4 > 999) {
            q4 = 0.0f;
        }
        float[] result = new float[4];
        if (windCode.equalsIgnoreCase(WindQuadrant.AAA.getValue())) {
            result[0] = q1;
            result[1] = q1;
            result[2] = q1;
            result[3] = q1;
        } else if (windCode.equalsIgnoreCase(WindQuadrant.NEQ.getValue())) {
            result[0] = q1;
            result[1] = q2;
            result[2] = q3;
            result[3] = q4;
        } else if (windCode.equalsIgnoreCase(WindQuadrant.SEQ.getValue())) {
            result[0] = q4;
            result[1] = q1;
            result[2] = q2;
            result[3] = q3;
        } else if (windCode.equalsIgnoreCase(WindQuadrant.SWQ.getValue())) {
            result[0] = q3;
            result[1] = q4;
            result[2] = q1;
            result[3] = q2;
        } else if (windCode.equalsIgnoreCase(WindQuadrant.NWQ.getValue())) {
            result[0] = q2;
            result[1] = q3;
            result[2] = q4;
            result[3] = q1;
        } else {
            // default as if NEQ
            result[0] = q1;
            result[1] = q2;
            result[2] = q3;
            result[3] = q4;
        }

        return result;
    }

    /**
     * This function computes the resulting coordinate (latitude/longitude
     * point) based on the latitude/longitude passed, a distance, and a
     * direction. Copied from PSH to support BestTrackGenerator
     *
     * @param lat
     *            latitude of point
     * @param lon
     *            longitude of point
     * @param distance
     *            distance (dist / earth radius in same units)
     * @param dir
     *            direction (degrees from N)
     * @return newCoor coordinate of the new point created
     */
    public static Coordinate computePoint(double lat, double lon,
            double distance, double dir) {
        final double PI = Math.PI;
        final double HALFPI = PI / 2.0;
        final double TWOPI = 2.0 * PI;

        /*
         * Convert the input values to radians.
         */
        double direction = Math.toRadians(dir);
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        double dLat = Math.asin(Math.sin(latRad) * Math.cos(distance)
                + Math.cos(latRad) * Math.sin(distance) * Math.cos(direction));
        double dLon;
        double dLt;
        double dLn;

        /*
         * Make sure the longitude is between -180 and +180 degrees.
         */
        lonRad = lonRad - (((int) (lonRad / TWOPI)) * TWOPI);
        if (lonRad < -PI) {
            lonRad = lonRad + TWOPI;
        }
        if (lonRad > PI) {
            lonRad = lonRad - TWOPI;
        }

        /*
         * Compute the delta longitude. If the initial latitude is either pole,
         * then use the original longitude, otherwise, compute the new
         * longitude.
         */
        if ((Math.abs(latRad - 90.0F) < 0.000001)
                || (Math.abs(-latRad - 90.0F) < 0.000001)) {
            dLon = lonRad;
        } else {
            dLon = Math.atan2(
                    Math.sin(direction) * Math.sin(distance) * Math.cos(latRad),
                    Math.cos(distance) - Math.sin(latRad) * Math.sin(dLat));
            dLon = (lonRad + dLon + PI % TWOPI) - PI;
        }

        /*
         * Make sure that latitude is between -90 and +90 degrees. Adjust the
         * longitude, if necessary.
         */
        dLt = dLat - (((int) (dLat / PI)) * PI);

        if (dLt > HALFPI) {
            dLt = PI - dLt;
            dLon = -dLon;
        }
        if (dLt < -HALFPI) {
            dLt = -PI - dLt;
            dLon = -dLon;
        }

        /*
         * Make sure the longitude is between -180 and +180 degrees.
         */
        dLn = dLon - (((int) (dLon / TWOPI)) * TWOPI);
        if (dLn < -PI) {
            dLn = dLn + TWOPI;
        }
        if (dLn > PI) {
            dLn = dLn - TWOPI;
        }

        /*
         * Convert the new position to degrees and create coordinate based on
         * new lat/lon
         */
        return new Coordinate((Math.toDegrees(dLn)), (Math.toDegrees(dLt)));
    }

    /**
     * This function computes the resulting coordinate (latitude/longitude
     * point) based on the latitude/longitude passed, a distance in statute
     * miles, and a direction in statute miles. Copied from PSH to support
     * BestTrackGenerator
     *
     * @param lat
     *            latitude of point
     * @param lon
     *            longitude of point
     * @param dist
     *            distance (statute miles)
     * @param dir
     *            direction (degrees from N)
     * @return newCoor coordinate of the new point created
     */
    public static Coordinate computePointMiles(double lat, double lon,
            double dist, double dir) {

        return computePoint(lat, lon,
                dist / (MapUtil.AWIPS_EARTH_RADIUS * MAP_CONSTANT), dir);

    }

    /**
     * Find aid code
     *
     * @param aid
     *            aid ID
     *
     * @return string aid code
     */
    public static String aidCode(String aid) {
        if (aid.length() > 3) {
            return aid.substring(0, 4).trim();
        } else {
            return aid;
        }
    }

    /**
     * Create buttons from a list of strings.
     *
     * @param parent
     *            parent
     * @param BtnNames
     *            List of string for button name.
     * @param marginHeight
     *            Margin at top and bottom of the buttons.
     * @param widthHint
     *            Width hint for the buttons
     *
     * @return Button[] Array of buttons
     */
    public static Button[] creatActionButtons(Composite parent,
            String[] btnNames, int marginHeight, int widthHint) {

        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(btnNames.length, true);
        buttonLayout.marginHeight = marginHeight;
        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        List<Button> btns = new ArrayList<>();
        for (String name : btnNames) {

            Button btn = new Button(buttonComp, SWT.PUSH);
            GridData btnData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            btnData.widthHint = widthHint;
            btn.setText(name);
            btn.setLayoutData(btnData);

            btns.add(btn);

        }

        return btns.toArray(new Button[btns.size()]);
    }

    /**
     * Get a float from a String
     *
     * @param valString
     *            The string to parse
     * @param reverse
     *            Flips value's sign if true
     * @param useDefault
     *            Check to see if we use a passed in default value if parsing
     *            fails
     *
     * @param defaultValue
     *            Default value if parsing fails
     *
     * @return the parsed value, or the invalid/default.
     */
    public static float getStringAsFloat(String valString, boolean reverse,
            boolean useDefault, float defaultValue) {
        float val;
        try {
            val = Float.parseFloat(valString);
            if (reverse) {
                val *= -1;
            }
        } catch (NumberFormatException e) {
            if (useDefault) {
                val = defaultValue;
            } else {
                val = INVALID_FLOAT;
            }
        }

        return val;
    }

    /**
     * Get an int from a String
     *
     * @param valString
     *            The string to parse
     * @param reverse
     *            Flips value's sign if true
     * @param useDefault
     *            Check to see if we use a passed in default value if parsing
     *            fails
     *
     * @param defaultValue
     *            Default value if parsing fails
     *
     * @return the parsed value, or the invalid/default.
     */
    public static int getStringAsInt(String valString, boolean reverse,
            boolean useDefault, int defaultValue) {
        int val;
        try {
            val = Integer.parseInt(valString);
            if (reverse) {
                val *= -1;
            }
        } catch (NumberFormatException e) {
            if (useDefault) {
                val = defaultValue;
            } else {
                val = INVALID_INT;
            }
        }

        return val;
    }

    /**
     * Checks if value is valid
     *
     * @param value
     *            the value to check
     *
     * @return if valid
     */
    public static boolean isValueValid(float value) {
        return value != INVALID_FLOAT;
    }

    /**
     * This function computes the resulting coordinate (latitude/longitude
     * point) based on the coordinate (latitude/longitude point) passed, a
     * distance and a direction.
     *
     * @param coor
     *            coordinate of the point (lat/lon)
     * @param dist
     *            distance (nautical mile)
     * @param dir
     *            direction (degrees from N)
     * @return newCoor coordinate of the new point created
     */
    public static Coordinate computePoint(Coordinate coor, float dist,
            float dir) {
        final double PI = 3.14159265;
        final double HALFPI = PI / 2.0;
        final double TWOPI = 2.0 * PI;

        // Degrees to radians, radians to degrees, & Earth radius
        final double DTR = PI / 180.0;
        final double RTD = 180.0 / PI;
        final double RADIUS = 6371200.0F;
        final double NM2M = 1852.0F;

        /*
         * Convert the input values to radians.
         */

        double direction = dir * DTR;
        double lat = coor.y * DTR;
        double lon = coor.x * DTR;
        double distance = dist * NM2M / RADIUS;

        double dLat = Math.asin(Math.sin(lat) * Math.cos(distance)
                + Math.cos(lat) * Math.sin(distance) * Math.cos(direction));
        double dLon;
        double dLt;
        double dLn;

        /*
         * Make sure the longitude is between -180 and +180 degrees.
         */
        lon = lon - (((int) (lon / TWOPI)) * TWOPI);
        if (lon < -PI) {
            lon = lon + TWOPI;
        }
        if (lon > PI) {
            lon = lon - TWOPI;
        }

        /*
         * Compute the delta longitude. If the initial latitude is either pole,
         * then use the original longitude, otherwise, compute the new
         * longitude.
         */
        if ((Math.abs(lat - 90.0F) < 0.000001)
                || (Math.abs(-lat - 90.0F) < 0.000001)) {
            dLon = lon;
        } else {
            dLon = Math.atan2(
                    Math.sin(direction) * Math.sin(distance) * Math.cos(lat),
                    Math.cos(distance) - Math.sin(lat) * Math.sin(dLat));
            dLon = (lon + dLon + PI % TWOPI) - PI;
        }

        /*
         * Make sure that latitude is between -90 and +90 degrees. Adjust the
         * longitude, if necessary.
         */
        dLt = dLat - (((int) (dLat / PI)) * PI);

        if (dLt > HALFPI) {
            dLt = PI - dLt;
            dLon = -dLon;
        }
        if (dLt < -HALFPI) {
            dLt = -PI - dLt;
            dLon = -dLon;
        }

        /*
         * Make sure the longitude is between -180 and +180 degrees.
         */
        dLn = dLon - (((int) (dLon / TWOPI)) * TWOPI);
        if (dLn < -PI) {
            dLn = dLn + TWOPI;
        }
        if (dLn > PI) {
            dLn = dLn - TWOPI;
        }

        /*
         * Convert the new position to degrees and create coordinate based on
         * new lat/lon
         */
        return new Coordinate((float) (dLn * RTD), (float) (dLt * RTD));
    }

    /**
     * Calculates the distance and the angle of a directional line from point 1
     * to point 2 relative to the North.
     *
     * @param point1
     *            - The starting point in Lat/Lon coordinates
     * @param point2
     *            - The ending point in Lat/Lon coordinates
     * @return double[2] [0]The distance between point1 to point2 [1]The angle
     *         of line point1->point2 relative to the North
     */
    public static double[] getDistNDir(Coordinate point1, Coordinate point2) {

        /*
         * Note - Orientation will be clockwise as following: North 0; East 90;
         * South 180; West 270;
         */
        GeodeticCalculator gc = new GeodeticCalculator(DefaultEllipsoid.WGS84);
        gc.setStartingGeographicPoint(point1.x, point1.y);
        gc.setDestinationGeographicPoint(point2.x, point2.y);

        double[] values = new double[2];

        // distance in meters.
        values[0] = gc.getOrthodromicDistance();
        values[1] = gc.getAzimuth();
        if (values[1] < 0.0) {
            values[1] += 360.0;
        }

        return values;
    }

    /**
     * Convenience method to set a control's background color
     *
     * @param ctrl
     *            Control
     * @param clr
     *            A SWT color constant
     */
    public static void setBackgroundColor(Control ctrl, int clr) {
        ctrl.setBackground(Display.getCurrent().getSystemColor(clr));
    }

    /**
     * Set a control's color to SWT.COLOR_WIDGET_BACKGROUND
     *
     * @param control
     */
    public static void setDefaultBackground(Control control) {
        control.setBackground(null);
    }

    /**
     * Set a control's color to yellow to indicate missing or invalid data.
     *
     * @param control
     */
    public static void setWarningBackground(Control control) {
        setBackgroundColor(control, SWT.COLOR_YELLOW);
    }

    /**
     * Set a control's color to yellow to indicate an active state.
     *
     * @param control
     */
    public static void setActiveButtonBackground(Control control) {
        setBackgroundColor(control, SWT.COLOR_YELLOW);
    }

    /**
     * Create an array for maximum sustained wind speed in knots: 0 through 250
     * with interval of 5 knots (In legacy ATCF).
     *
     * Note: Might be better to make this configurable later.
     *
     * return String[]
     */
    public static String[] getMaxWindEntries() {
        return getMaxWindEntries(null);
    }

    /**
     * Create an array for maximum sustained wind speed in knots: 0 through 250
     * with interval of 5 knots (In legacy ATCF).
     *
     * Note: Might be better to make this configurable later.
     *
     * return String[]
     */
    public static String[] getMaxWindEntries(String format) {
        return getEntries(MAX_WIND_SPEED, 5, format);
    }

    /**
     * Create an array for the wind radius: 0 through 995 nm with interval of 5
     * nm (Legacy range).
     *
     * Note: Might be better to make this configurable later.
     *
     * return String[]
     */
    public static String[] getRadiiEntries() {
        return getRadiiEntries(null);
    }

    /**
     * Create an array for the wind radius: 0 through 995 nm with interval of 5
     * nm (Legacy range).
     *
     * Note: Might be better to make this configurable later.
     *
     * return String[]
     */
    public static String[] getRadiiEntries(String format) {
        return getEntries(MAX_WIND_RADIUS, 5, format);
    }

    /**
     * Create an string array starting from 0 to a given max value with given
     * interval and format.
     *
     * @param top
     *            Maximum value (inclusive)
     * @param interval
     *            Interval
     * @param format
     *            Format of strings
     *
     * @return String[]
     */
    public static String[] getEntries(int top, int interval, String format) {
        return getEntries(0, top, interval, format);
    }

    /**
     * Create an string array starting from minimum to a given max value with
     * given interval and format.
     *
     * @param bottom
     *            minimum value (inclusive)
     * @param top
     *            Maximum value (inclusive)
     * @param interval
     *            Interval
     * @param format
     *            Format of strings
     *
     * @return String[]
     */
    public static String[] getEntries(int bottom, int top, int interval,
            String format) {

        int num = (top - bottom) / interval;
        String[] entries = new String[num + 1];

        for (int kk = 0; kk <= num; kk++) {
            if (format == null) {
                entries[kk] = String.valueOf(bottom + kk * interval);
            } else {
                entries[kk] = String.format(format, bottom + kk * interval);
            }
        }

        return entries;
    }

    /**
     * Creates ForecastTrackRecord for given forecast hours between two known
     * forecasts by interpolation.
     *
     * @param startRec
     *            Starting ForecastTrackRecord
     * @param endRec
     *            End ForecastTrackRecord
     * @param fcstHrs
     *            Array of forecast hours to be interpolated
     * @param useStartRec
     *            Flag to copy starting record if no endRec.
     *
     * @return List<ForecastTrackRecord>
     */
    public static List<ForecastTrackRecord> interpolateRecord(
            ForecastTrackRecord startRec, ForecastTrackRecord endRec,
            int[] fcstHrs, boolean useStartRec) {

        List<ForecastTrackRecord> outRecs = new ArrayList<>();
        if (startRec != null) {
            if (endRec == null) {
                // Copy from startRec for each forecast hour.
                if (useStartRec) {
                    for (int fcstHr : fcstHrs) {
                        ForecastTrackRecord nrec = new ForecastTrackRecord(
                                startRec);
                        nrec.setFcstHour(fcstHr);
                        outRecs.add(nrec);
                    }
                }
            } else { // Interpolate
                Coordinate start = new Coordinate(startRec.getClon(),
                        startRec.getClat());
                Coordinate end = new Coordinate(endRec.getClon(),
                        endRec.getClat());

                // Find distance/speed/direction from starting to end location.
                int timeInHr = endRec.getFcstHour() - startRec.getFcstHour();
                double[] distDir = AtcfVizUtil.getDistNDir(start, end);
                double spd = distDir[0] / AtcfVizUtil.NM2M / timeInHr;

                for (int fcstHr : fcstHrs) {
                    ForecastTrackRecord nrec = new ForecastTrackRecord(
                            startRec);
                    nrec.setFcstHour(fcstHr);
                    outRecs.add(nrec);

                    nrec.setStormSped((float) spd);

                    int dir = AtcfDataUtil.roundToNearest(distDir[1], 5);
                    nrec.setStormDrct(dir);

                    // Extrapolate from starting location.
                    double dist = spd * (fcstHr - startRec.getFcstHour());

                    Coordinate interpLoc = AtcfVizUtil.computePoint(start,
                            (float) dist, (float) distDir[1]);
                    nrec.setClat((float) interpLoc.y);
                    nrec.setClon((float) interpLoc.x);
                }
            }
        }

        return outRecs;
    }

    /**
     * Round a number to tenth place.
     *
     * @param x
     *
     * @return double Rounded number
     */
    public static double snapToTenth(double x) {
        return Math.round(x * 10) / 10.0;
    }

    /**
     * Calculate the speed given two deck records.
     *
     * @param rec0
     *            AbstractDeckRecord
     * @param rec1
     *            AbstractDeckRecord
     *
     * @return float Speed in knots
     */
    public static float getSpeed(AbstractDeckRecord rec0,
            AbstractDeckRecord rec1) {
        float speed = 0;
        double[] distDir = AtcfVizUtil.getDistNDir(
                new Coordinate(rec0.getClon(), rec0.getClat()),
                new Coordinate(rec1.getClon(), rec1.getClat()));
        long timeDiff = Math
                .abs(rec1.getRefTime().getTime() - rec0.getRefTime().getTime());

        // Use float since F-Deck records have minutes in their ref. time.
        speed = Math.round(distDir[0] / AtcfVizUtil.NM2M
                / (((float) timeDiff) / TimeUtil.MILLIS_PER_HOUR));
        return speed;
    }

    /**
     * Redraw objective aids.
     *
     * @param storm
     */
    public static void redrawAids(Storm storm) {
        AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
        AtcfProduct prd = rsc.getResourceData().getActiveAtcfProduct();

        ObjAidsProperties oap = prd.getObjAidsProperties();
        if (oap != null) {
            String[] dtgs = oap.getSelectedDateTimeGroups();
            prd.getADeckDataMap().clear();
            Map<String, List<ADeckRecord>> aDeckDataMap = AtcfDataUtil
                    .retrieveADeckData(storm, dtgs, false);
            prd.setADeckDataMap(aDeckDataMap);

            ObjAidsGenerator oaGen = new ObjAidsGenerator(rsc, oap);
            oaGen.draw();
        }
    }

    /**
     * Refresh the fixes graphics displayed on the map if available.
     */
    public static void redrawFixes() {
        DrawFixesElements drawFixesElements = new DrawFixesElements();

        // Use the last-used display attributes if available.
        DisplayFixesProperties displayProperties = AtcfSession.getInstance()
                .getAtcfResource().getResourceData().getActiveAtcfProduct()
                .getDisplayFixesProperties();

        if (displayProperties != null) {
            drawFixesElements.setDisplayProperties(displayProperties);
            drawFixesElements.draw();
        }
    }

    /**
     * Create a button for help (not implemented yet) in dialogs.
     *
     * @param parent
     * @param layoutData
     * @return
     */
    public static Button createHelpButton(Composite parent, Object layoutData) {
        Button helpButton = new Button(parent, SWT.PUSH);
        helpButton.setLayoutData(layoutData);
        helpButton.setText("Help");
        helpButton.setToolTipText("Help is not yet implemented.");
        helpButton.setEnabled(false);
        return helpButton;
    }

    /**
     * GridData to fill horizontal and grab excess horizontal space
     */
    public static GridData horizontalFillGridData() {
        return new GridData(SWT.FILL, SWT.CENTER, true, false);
    }

    /**
     * GridData for standard dialog buttons
     */
    public static GridData buttonGridData() {
        return horizontalFillGridData();
    }

    /**
     * Determine the average character width and height of a control's font.
     *
     * @param control
     * @return a point with x = width and y = height
     */
    public static Point getCharSize(Control control) {
        GC gc = new GC(control);
        FontMetrics fm = gc.getFontMetrics();
        Point result = new Point(fm.getAverageCharWidth(), fm.getHeight());
        gc.dispose();
        return result;
    }

    /**
     * Determine the average character width a control's font.
     *
     * @param control
     * @return the width
     */
    public static int getCharWidth(Control control) {
        return getCharSize(control).x;
    }

}
