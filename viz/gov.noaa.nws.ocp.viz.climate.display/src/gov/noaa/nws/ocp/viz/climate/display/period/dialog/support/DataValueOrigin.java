/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog.support;

/**
 * This enum holds the data value origins that a report could have: Daily DB,
 * MSM, or Other.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 18 JUL 2016  20414      amoore      Clarify data value origin. Rename class.
 * 13 MAR 2017  27420      amoore      Moved out of common area, since it is
 *                                     only used by the Period Display frontend.
 * 20 NOV 2017  41128      amoore      Moved to separate file.
 * </pre>
 * 
 * @author amoore
 */
public enum DataValueOrigin {

    /**
     * Values based on daily sums (usually monthly display only).
     * "Daily Database".
     */
    DAILY_DATABASE("Daily DB"),

    /**
     * Values based on monthly data. "Monthly Summary Message".
     */
    MONTHLY_SUMMARY_MESSAGE("MSM"),

    /**
     * Values input by user overwrite.
     */
    OTHER("Other");

    private final String value;

    /**
     * @param iValue
     */
    private DataValueOrigin(final String iValue) {
        this.value = iValue;
    }

    @Override
    public String toString() {
        return value;
    }
}