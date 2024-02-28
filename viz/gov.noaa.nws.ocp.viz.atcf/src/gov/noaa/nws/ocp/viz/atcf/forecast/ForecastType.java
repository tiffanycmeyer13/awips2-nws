/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

/**
 * Forecast type for ATCF.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 27, 2020 78027      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public enum ForecastType {
    TRACK("Forecast Track", 12),
    INTENSITY("Forecast Intensity", 12),
    WIND_RADII("Forecast Wind Radii ", 12),
    SEAS("Forecast Seas", 3);

    private final String title;

    private final int dfltTau;

    /**
     * @param name
     */
    private ForecastType(String title, int dfltTau) {
        this.title = title;
        this.dfltTau = dfltTau;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the dfltTau
     */
    public int getDfltTau() {
        return dfltTau;
    }

    /**
     * @return the name for the dialog
     */
    public String getDialogName() {
        return title.replaceAll("\\s+", "") + "Dialog";
    }

}