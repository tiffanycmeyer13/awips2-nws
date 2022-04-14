/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to pass column information to a PshTable
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2017 #35463      wpaintsil   Initial creation.
 * Sep 19, 2017 #37917      wpaintsil   Add List<String> field for dropdown lists.
 * Nov 20, 2017 #40299      wpaintsil   Move this class to a separate file.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshTableColumn {
    private int width;

    private String name;

    private PshControlType controlType;

    /**
     * A list of Strings to be added to a Combo control.
     */
    private List<String> dropdownList;

    public PshTableColumn(String columnName, int columnWidth,
            PshControlType controlType) {
        this(columnName, columnWidth, controlType, new ArrayList<>());
    }

    public PshTableColumn(String columnName, int columnWidth,
            PshControlType controlType, List<String> dropDownList) {
        this.name = columnName;
        this.width = columnWidth;
        this.controlType = controlType;
        this.setDropdownList(dropDownList);

    }

    /**
     * @return the columnWidth
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the columnName
     */
    public String getName() {
        return name;
    }

    /**
     * @return the controlType
     */
    public PshControlType getControlType() {
        return controlType;
    }

    /**
     * @return the dropdownList
     */
    public List<String> getDropdownList() {
        return dropdownList;
    }

    /**
     * @param dropdownList
     *            the dropdownList to set
     */
    public void setDropdownList(List<String> dropdownList) {
        this.dropdownList = dropdownList;
    }
}