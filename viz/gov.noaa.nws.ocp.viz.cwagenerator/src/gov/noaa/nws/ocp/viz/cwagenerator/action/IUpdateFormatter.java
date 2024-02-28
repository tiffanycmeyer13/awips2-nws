/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;

/**
 * Abstract class for update formatter
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 06/02/2020  75767    wkwock      Initial creation
 * 09/10/2021  28802    wkwock      Add clearDrawings, changeMouseMode, saveVORs, openVORs methods
 * 
 * </pre>
 * 
 * @author wkwock
 */
public interface IUpdateFormatter {
    public void updateFormatter(AbstractDrawableComponent drawable);

    public void clearDrawings();

    public void changeMouseMode(boolean isDrawMode);

    public void saveVORs();

    public void openVORs();
}
