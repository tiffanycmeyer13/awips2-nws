/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

/**
 * Organize Storm Management dialog properties
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 06, 2018  50987      wpaintsil  Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public enum StormManagementType {

    /**
     * Spawn an Invest
     */
    SPAWN_INVEST("spawnInvest", "Storm Management - Spawn an Invest"),

    /**
     * End a Genesis Area
     */
    END_GENESIS("endGenesis", "Storm Management - End a Genesis Area"),

    /**
     * Restart a Genesis Area
     */
    RESTART_GENESIS("restartGenesis", "Storm Management - Restart a Genesis Area"),

    /**
     * Archive a Genesis Area
     */
    ARCHIVE_GENESIS("archiveGenesis", "Storm Management - Archive a Genesis Area"),

    /**
     * Copy a Storm
     */
    COPY_STORM("copyStorm", "Storm Management - Copy a Storm"),

    /**
     * Renumber a Storm
     */
    RENUMBER_STORM("renumberStorm", "Storm Management - Renumber a Storm"),

    /**
     * Name an Existing Storm
     */
    NAME_EXISTING("nameExisting", "Storm Management - Name an Existing Storm"),

    /**
     * Correct a Storm
     */
    CORRECT_STORM("correctStorm", "Storm Management - Correct a Storm"),

    /**
     * Restart a Storm
     */
    RESTART_STORM("restartStorm", "Storm Management - Restart a Storm"),

    /**
     * End a Storm
     */
    END_STORM("endStorm", "Storm Management - End a Storm"),

    /**
     * Delete a Storm
     */
    DELETE_STORM("deleteStorm", "Storm Management - Delete a Storm");

    /**
     * Command parameter from plugin.xml
     */
    private String commandParam;

    /**
     * Title of the dialog
     */
    private String dialogTitle;

    /**
     * Constructor
     * 
     * @param commandParam
     * @param dialogTitle
     */
    private StormManagementType(String commandParam, String dialogTitle) {
        this.commandParam = commandParam;
        this.dialogTitle = dialogTitle;
    }

    /**
     * @return the command parameter
     */
    public String getCommandParam() {
        return commandParam;
    }

    /**
     * @return the dialogTitle
     */
    public String getDialogTitle() {
        return dialogTitle;
    }

    /**
     * Return a StormDialogType whose command matches the string parameter.
     * 
     * @param dialogTitle
     * @return
     */
    public static StormManagementType getStormDialogType(String commandParam) {
        for (StormManagementType type : StormManagementType.values()) {
            if (type.getCommandParam().equals(commandParam)) {
                return type;
            }
        }

        return null;
    }
}