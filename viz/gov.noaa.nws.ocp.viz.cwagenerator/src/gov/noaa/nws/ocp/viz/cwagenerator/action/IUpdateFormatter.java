/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import gov.noaa.nws.ncep.ui.pgen.elements.Product;

/**
 * Abstract class for update formatter
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 06/02/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
public interface IUpdateFormatter {
    public void updateFormatter(Product product);
}
