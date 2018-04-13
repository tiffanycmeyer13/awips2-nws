/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table;

/**
 * Enum with the types of controls that may be used in a table.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2017 #35463      wpaintsil   Initial creation.
 * Nov 20, 2017 #40299      wpaintsil   Move this enum to a separate file.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public enum PshControlType {
    /**
     * A generic Combo control.
     */
    COMBO,
    /**
     * A generic combo control that cannot be empty.
     */
    NONEMPTY_COMBO,
    /**
     * A Text control (SWT.SINGLE)
     */
    TEXT,
    /**
     * A Button control (SWT.CHECK)
     */
    CHECKBOX,
    /**
     * A Text control for wind direction/speed fields
     */
    WIND_TEXT,
    /**
     * A Text control for date/time fields
     */
    DATETIME_TEXT,
    /**
     * A Text control for number fields
     */
    NUMBER_TEXT,
    /**
     * A generic text control that can't be empty
     */
    NONEMPTY_TEXT
}