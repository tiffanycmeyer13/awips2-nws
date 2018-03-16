/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate;

/**
 * Climatology input types
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ---------- --------------------------
 * 04/29/2016   18469       wkwock     Initial creation.
 * 10/27/2016   20635       wkwock     Clean up
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public enum ClimatologyInputType {
    DAILY("Daily"), MONTHLY("Monthly"), SEASONAL("Seasonal"), ANNUAL("Annual");

    private String stringValue;

    private ClimatologyInputType(String value) {
        this.stringValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
