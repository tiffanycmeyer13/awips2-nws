/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

/**
 * Constants used for drawing.
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

public final class ElementConstants {

    // Private constructor
    private ElementConstants() {
    }

    public static final String LABELED_SYMBOL = "labeledSymbol";

    public static final String LABELED_FRONT = "labeledFront";

    public static final String DELETE_LABEL = "Delete Label";

    public static final String ADD_LABEL = "Add Label";

    public static final String EDIT_LABEL = "Edit Label";

    public static final String ALWAYS_VISIBLE = "alwaysVisible";

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    public static final String LABEL = "label";

    public static final String ICON = "icon";

    public static final String NAME = "name";

    public static final String ACTIONSXTRA = "actionsxtra";

    public static final String SYMBOL = "Symbol";

    public static final String ACTIONS = "actions";

    public static final String CLASSNAME = "className";

    public static final String PLUGINXML_ATTRIBUTE_DELIMETER = "\\s*,\\s*";

    /*
     * Pgen actions as defined in pgen plugin.xml <action name="Connect", ...>
     */
    public static final String ACTION_CONNECT = "Connect";

    public static final String ACTION_COPY = "Copy";

    public static final String ACTION_EXTRAP = "Extrap";

    public static final String ACTION_FLIP = "Flip";

    public static final String ACTION_INTERP = "Interp";

    public static final String ACTION_MODIFY = "Modify";

    public static final String ACTION_MOVE = "Move";

    public static final String ACTION_MULTISELECT = "MultiSelect";

    public static final String ACTION_ROTATE = "Rotate";

    public static final String ACTION_SELECT = "Select";

    public static final String UNDO = "Undo";

    public static final String REDO = "Redo";

    /*
     * Categories (aka, classes) as defined in pgen plugin.xml <class
     * name="Front", ...>
     */
    public static final String CATEGORY_ARC = "Arc";

    public static final String CATEGORY_COMBO = "Combo";

    public static final String CATEGORY_FRONT = "Front";

    public static final String CATEGORY_LINES = "Lines";

    public static final String CATEGORY_MET = "MET";

    public static final String CATEGORY_SIGMET = "SIGMET";

    public static final String CATEGORY_SYMBOLS = "Symbols";

    public static final String CATEGORY_TEXT = "Text";

    public static final String CATEGORY_ANY = "Any";

    /*
     * Objects (aka, elements) as defined in pgen plugin.xml <object
     * name="General Text", ...>
     */
    public static final String TYPE_GENERAL_TEXT = "General Text";

    public static final String TYPE_OUTLOOK = "Outlook";

    public static final String TYPE_VOLCANO = "Volcano";

    public static final String TYPE_WATCH = "Watch";

    public static final String TYPE_INTL_SIGMET = "INTL_SIGMET";

    public static final String TYPE_VOLC_SIGMET = "VOLC_SIGMET";

    public static final String TYPE_VACL_SIGMET = "VACL_SIGMET";

    public static final String TYPE_CCFP_SIGMET = "CCFP_SIGMET";

    public static final String CIRCLE = "Circle";

    public static final String PARM = "Parm";

    public static final String LEVEL = "Level";

    public static final String FORECAST_HOUR = "ForecastHour";

    public static final String NONE = "None";

    public static final String DEFAULT_ACTIVITY_TYPE = "Default";

    public static final String DEFAULT_ACTIVITY_LABEL = "Default.DDMMYYYY.HH.xml";

    public static final String DEFAULT_SUBTYPE = "None";

    public static final String CONTOURS = "Contours";

    public static final String EVENT_DEFAULT_TEXT = "defaultTxt";

    public static final String EVENT_LABEL = "addLabel";

    public static final String EVENT_PREV_COLOR = "usePrevColor";

    public static final String EVENT_OTHER = "Other";

    public static final String GENERAL_DEFAULT = "Default";

    public static final String OPTION_ALL = "All";

    public static final String DESK = "DESK";

    public static final String G2G_BOUND_MARK = "9999";

}