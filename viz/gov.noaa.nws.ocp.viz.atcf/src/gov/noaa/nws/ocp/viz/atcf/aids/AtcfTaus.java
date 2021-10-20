/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.List;

/**
 * TAUS in ATCF.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 11, 2019 62487      jwu         Initial creation
 * Apr 08, 2020 77478      jwu         Add TAU 3 for 12 ft seas wave.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public enum AtcfTaus {
    TAU0(0, "full"),
    TAU3(3, "3"),
    TAU12(12, "12"),
    TAU24(24, "24"),
    TAU36(36, "36"),
    TAU48(48, "48"),
    TAU60(60, "60"),
    TAU72(72, "72"),
    TAU84(84, "84"),
    TAU96(96, "96"),
    TAU108(108, "108"),
    TAU120(120, "120"),
    TAU132(132, "132"),
    TAU144(144, "144"),
    TAU168(168, "168");

    private int value;

    private String time;

    /**
     * @param iValue
     */
    private AtcfTaus(final int iValue, final String time) {
        this.value = iValue;
        this.time = time;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * Get a TAU by its value
     * 
     * @param value
     *            Tau's value
     * @return AtcfTaus
     */
    public static AtcfTaus getTau(int value) {
        AtcfTaus ftau = null;
        for (AtcfTaus tau : AtcfTaus.values()) {
            if (tau.getValue() == value) {
                ftau = tau;
                break;
            }
        }

        return ftau;
    }

    /**
     * Get a TAU by its time
     * 
     * @param time
     *            Tau's time
     * @return AtcfTaus
     */
    public static AtcfTaus getTau(String time) {
        AtcfTaus ftau = null;
        for (AtcfTaus tau : AtcfTaus.values()) {
            if (tau.getTime().equalsIgnoreCase(time)) {
                ftau = tau;
                break;
            }
        }

        return ftau;
    }

    /**
     * Get TAUs used for displaying/editing obj. aids (A-Deck), and Forecast
     * Track, Forecast Intensity tools.
     *
     * @return List<AtcfTaus>
     */
    public static List<AtcfTaus> getForecastTaus() {

        // TAU 3 is only for 12 Ft Seas Wave forecast, exclude it here.
        List<AtcfTaus> aidsTaus = new ArrayList<>();
        for (AtcfTaus tau : AtcfTaus.values()) {
            int tauVal = tau.getValue();
            if ((tauVal <= 72 && tauVal % 12 == 0)
                    || (tauVal > 72 && tauVal % 24 == 0)) {
                aidsTaus.add(tau);
            }
        }

        return aidsTaus;
    }

    /**
     * Get the time strings for TAUs used for displaying/editing obj. aids
     * (A-Deck).
     *
     * @return String[]
     */
    public static String[] getForecastTausTime() {

        List<AtcfTaus> aidsTaus = getForecastTaus();

        String[] tauTimes = new String[aidsTaus.size()];
        for (int ii = 0; ii < aidsTaus.size(); ii++) {
            tauTimes[ii] = aidsTaus.get(ii).getTime();
        }

        return tauTimes;
    }

    /**
     * Get TAUs used for Forecast and Forecast Wind Radii tool.
     * 
     * Note: Forecast Wind Radii uses the same set of TAUs as Forecast Seas
     * except Forecast Seas has TAU3.
     *
     * @return List<AtcfTaus>
     */
    public static List<AtcfTaus> getForecastWindRadiiTaus() {

        // TAU 3 is only for 12 Ft Seas Wave forecast, exclude it here.
        List<AtcfTaus> radiiTaus = new ArrayList<>();
        for (AtcfTaus tau : getForecastSeasTaus()) {
            int tauVal = tau.getValue();
            if ((tauVal <= 72 && tauVal % 12 == 0)
                    || (tauVal > 72 && tauVal <= 120 && tauVal % 24 == 0)) {
                radiiTaus.add(tau);
            }
        }

        return radiiTaus;
    }

    /**
     * Get TAUs used for Forecast Seas tool.
     *
     * Note: Forecast Seas uses the same set of TAUs as Forecast Wind Radii
     * except it has TAU3.
     *
     * @return List<AtcfTaus>
     */
    public static List<AtcfTaus> getForecastSeasTaus() {

        List<AtcfTaus> fcstTaus = new ArrayList<>();

        for (AtcfTaus tau : AtcfTaus.values()) {
            int tauVal = tau.getValue();

            // Note: TAU 3 is included for 12 Ft Seas Wave forecast
            if ((tau == AtcfTaus.TAU3) || (tauVal <= 72 && tauVal % 12 == 0)
                    || (tauVal > 72 && tauVal <= 120 && tauVal % 24 == 0)) {
                fcstTaus.add(tau);
            }
        }

        return fcstTaus;
    }

    /**
     * Get TAUs used for Advisory Composition & Advisory Data.
     *
     * Note: Advisory uses the same set of Forecast plus TAU 3. except it has
     * TAU3.
     *
     * @return List<AtcfTaus>
     */
    public static List<AtcfTaus> getAdvisoryTaus() {

        List<AtcfTaus> advTaus = new ArrayList<>();

        for (AtcfTaus tau : AtcfTaus.values()) {
            int tauVal = tau.getValue();

            // Note: TAU 3 is included for Advisory
            if ((tau == AtcfTaus.TAU3) || (tauVal <= 72 && tauVal % 12 == 0)
                    || (tauVal > 72 && tauVal % 24 == 0)) {
                advTaus.add(tau);
            }
        }

        return advTaus;
    }

}