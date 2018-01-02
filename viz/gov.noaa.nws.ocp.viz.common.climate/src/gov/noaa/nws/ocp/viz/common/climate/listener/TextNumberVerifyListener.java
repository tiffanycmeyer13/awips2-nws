/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener;

import java.util.regex.Pattern;

/**
 * This abstract class is a verify listener for number Text fields, to perform
 * validation that input is not less than a minimum or greater than a maximum,
 * and is a number. Actual validation is up to implementing classes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 12 JUL 2016  20414      amoore      Initial creation
 * 22 FEB 2017  28609      amoore      Address TODOs.
 * </pre>
 * 
 * @author amoore
 */
public abstract class TextNumberVerifyListener extends TextVerifyListener {
    /*
     * Task 29502: things with number verify listener also could get rid of
     * leading/trailing 0's on focus lost; may not be worth effort for minimal
     * gain
     */
    /**
     * Non-digit pattern.
     */
    public static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]");

    /**
     * Non-digit non-decimal.
     */
    public static final Pattern NON_DIGIT_NON_DECIMAL_PATTERN = Pattern
            .compile("[^0-9.]");

    /**
     * Multiple decimal.
     */
    public static final Pattern MULTIPLE_DECIMAL = Pattern.compile("\\..*\\.");

    /**
     * Minimum value.
     */
    private final Number myMin;

    /**
     * Maximum value.
     */
    private final Number myMax;

    /**
     * Default value.
     */
    private final Number myDefault;

    /**
     * Constructor.
     * 
     * @param iMin
     *            minimum value.
     * @param iMax
     *            maximum value.
     * @param iDefault
     *            default value.
     */
    public TextNumberVerifyListener(Number iMin, Number iMax, Number iDefault) {
        myMin = iMin;
        myMax = iMax;
        myDefault = iDefault;
    }

    /**
     * @return the Min
     */
    protected final Number getMin() {
        return myMin;
    }

    /**
     * @return the Max
     */
    protected final Number getMax() {
        return myMax;
    }

    /**
     * @return the default
     */
    protected final Number getDefault() {
        return myDefault;
    }
}
