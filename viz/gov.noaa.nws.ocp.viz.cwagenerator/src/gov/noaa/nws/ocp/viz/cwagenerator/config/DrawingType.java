/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

/**
 * 
 * Enum for drawing type
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 30, 2021 28802      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public enum DrawingType {
    AREA("Area"), LINE("Line"), ISOLATED("Isolated");

    public final String typeName;

    private DrawingType(String name) {
        this.typeName = name;
    }
}
