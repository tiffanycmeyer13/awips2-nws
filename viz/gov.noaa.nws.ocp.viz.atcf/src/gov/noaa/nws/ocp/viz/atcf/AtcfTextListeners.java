/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.atcf;

import gov.noaa.nws.ocp.viz.ui.listeners.CComboIntVerifyListener;
import gov.noaa.nws.ocp.viz.ui.listeners.TextDoubleListener;
import gov.noaa.nws.ocp.viz.ui.listeners.TextIntListener;

/**
 * This class holds custom ATCF GUI input verification listeners.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 14, 2019 67323      jwu         Created.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AtcfTextListeners {

    // Custom latitude input limits.
    public static final double LAT_LOWER_BOUND = 0;

    public static final double LAT_UPPER_BOUND = 90;

    public static final double LAT_MISSING_VALUE = 0;

    public static final int LAT_TEXT_LIMIT = 5;

    public static final int LAT_DECIMALS = 2;

    // Custom longitude input limits.
    public static final double LON_LOWER_BOUND = 0;

    public static final double LON_UPPER_BOUND = 180;

    public static final double LON_MISSING_VALUE = 0;

    public static final int LON_TEXT_LIMIT = 6;

    public static final int LON_DECIMALS = 2;

    // Custom wind speed input limits.
    public static final int WIND_LOWER_BOUND = 0;

    public static final int WIND_UPPER_BOUND = 300;

    public static final int WIND_MISSING_VALUE = 0;

    public static final int WIND_TEXT_LIMIT = 3;

    // Custom wind gust input limits.
    public static final int GUST_LOWER_BOUND = 0;

    public static final int GUST_UPPER_BOUND = 995;

    public static final int GUST_MISSING_VALUE = 0;

    public static final int GUST_TEXT_LIMIT = 3;

    // Custom wind gust input limits.
    public static final int RADII_LOWER_BOUND = 0;

    public static final int RADII_UPPER_BOUND = 1200;

    public static final int RADII_MISSING_VALUE = 0;

    public static final int RADII_TEXT_LIMIT = 4;

    /**
     * Latitude text verify listener.
     * 
     * Latitude should be 0.0 to 90.00 and allow two decimals.
     */
    private final TextDoubleListener latVerifyListener = new TextDoubleListener(
            LAT_LOWER_BOUND, LAT_UPPER_BOUND, LAT_MISSING_VALUE, LAT_DECIMALS,
            LAT_TEXT_LIMIT);

    /**
     * Longitude text verify listener.
     * 
     * Longitude should be 0.0 to 180.00 and allow two decimals.
     */
    private final TextDoubleListener lonVerifyListener = new TextDoubleListener(
            LON_LOWER_BOUND, LON_UPPER_BOUND, LON_MISSING_VALUE, LON_DECIMALS,
            LON_TEXT_LIMIT);

    /**
     * Latitude text verify listener.
     * 
     * Latitude should be 0.0 to 90.0 and allow one decimals and length is 4.
     */
    private final TextDoubleListener latVerifyListener1 = new TextDoubleListener(
            LAT_LOWER_BOUND, LAT_UPPER_BOUND, LAT_MISSING_VALUE, 1, 4);

    /**
     * Longitude text verify listener.
     * 
     * Longitude should be 0.0 to 180.0 and allow one decimals and length is 5.
     */
    private final TextDoubleListener lonVerifyListener1 = new TextDoubleListener(
            LON_LOWER_BOUND, LON_UPPER_BOUND, LON_MISSING_VALUE, 1, 5);

    /**
     * Max wind text verify listener.
     * 
     * Maximum wind should be 0 to 250.
     */
    private final TextIntListener windTextVerifyListener = new TextIntListener(
            WIND_LOWER_BOUND, WIND_UPPER_BOUND, WIND_MISSING_VALUE,
            WIND_TEXT_LIMIT);

    /**
     * Max wind CCombo verify listener.
     * 
     * Maximum wind is 0 to 999, and this range also depends on the items in the
     * CCombo.
     */
    private final CComboIntVerifyListener windCmbVerifyListener = new CComboIntVerifyListener(
            WIND_LOWER_BOUND, WIND_UPPER_BOUND, WIND_MISSING_VALUE,
            WIND_TEXT_LIMIT);

    /**
     * Wind gust CCombo verify listener.
     * 
     * Maximum gust is 0 to 995, and this range also depends on the items in the
     * CCombo.
     */
    private final CComboIntVerifyListener gustCmbVerifyListener = new CComboIntVerifyListener(
            GUST_LOWER_BOUND, GUST_UPPER_BOUND, GUST_MISSING_VALUE,
            GUST_TEXT_LIMIT);

    /**
     * Wind intensity quadrant radius CCombo verify listener.
     * 
     * Maximum gust is 0 to 1200, and this range also depends on the items in
     * the CCombo.
     */
    private final CComboIntVerifyListener radiiCmbVerifyListener = new CComboIntVerifyListener(
            RADII_LOWER_BOUND, RADII_UPPER_BOUND, RADII_MISSING_VALUE,
            RADII_TEXT_LIMIT);

    /**
     * @return the latVerifyListener
     */
    public TextDoubleListener getLatVerifyListener() {
        return latVerifyListener;
    }

    /**
     * @return the lonVerifyListener
     */
    public TextDoubleListener getLonVerifyListener() {
        return lonVerifyListener;
    }

    /**
     * @return the latVerifyListener1
     */
    public TextDoubleListener getLatVerifyListener1() {
        return latVerifyListener1;
    }

    /**
     * @return the lonVerifyListener1
     */
    public TextDoubleListener getLonVerifyListener1() {
        return lonVerifyListener1;
    }

    /**
     * @return the windTextVerifyListener
     */
    public TextIntListener getWindTextVerifyListener() {
        return windTextVerifyListener;
    }

    /**
     * @return the gustCmbVerifyListener
     */
    public CComboIntVerifyListener getWindCmbVerifyListener() {
        return windCmbVerifyListener;
    }

    /**
     * @return the windCmbVerifyListener
     */
    public CComboIntVerifyListener getGustCmbVerifyListener() {
        return gustCmbVerifyListener;
    }

    /**
     * @return the radiiCmbVerifyListener
     */
    public CComboIntVerifyListener getRadiiCmbVerifyListener() {
        return radiiCmbVerifyListener;
    }

}