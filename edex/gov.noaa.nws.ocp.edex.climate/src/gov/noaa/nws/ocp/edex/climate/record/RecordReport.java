/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.record;

/**
 * RecordClimate-related class for wrapping record report data.
 * 
 * <pre>
* SOFTWARE HISTORY
* 
* Date         Ticket#    Engineer    Description
* ------------ ---------- ----------- --------------------------
* NOV 28 2016  21100      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 *
 */
public class RecordReport {

    private final String afosID;

    private final String reportText;

    /**
     * Constructor.
     * 
     * @param id
     *            AFOS ID
     * @param text
     *            report body text.
     */
    public RecordReport(String id, String text) {
        afosID = id;
        reportText = text;
    }

    /**
     * @return the afosID
     */
    public String getAfosID() {
        return afosID;
    }

    /**
     * @return the reportText
     */
    public String getReportText() {
        return reportText;
    }
}
