/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.TimeZone;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * <pre>
 *        TYPE for global climate parameters.  The following derived 
 *        TYPE contains the definitions for the different global elements.
 *  
 *        User-defined Thresholds:
 *             t1   max temperature GE user-defined value T1
 *             t2   max temperature GE user-defined value T2
 *             t3   max temperature LE user-defined value T3
 *             t4   min temperature GE user-defined value T4
 *             t5   min temperature LE user-defined value T5
 *             t6   min temperature LE user-defined value T6
 *             p1   precipitation amount GE user-defined value P1
 *             p2   precipitation amount GE user-defined value P2
 *             s1   snowfall amount GE user-defined value S1
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 9, 2016             xzhang      Initial creation
 * 20 SEP 2016  21378      amoore      Added ability to get instance with missing values.
 * 03 OCT 2016  20639      wkwock      1st char. of DynamicSerializeElement variable should be lower case
 * 19 DEC 2016  20955      amoore      Move default preferences to common location.
 * 22 FEB 2017  28609      amoore      Address TODOs. Fix comments.
 * 23 FEB 2017  29416      wkwock      Added displayWait and reviewWait.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateGlobal {
    /** default display wait time in minutes */
    private static final int DEFAULT_DISPLAY_WAIT = 20;

    /** default review wait time in minutes */
    private static final int DEFAULT_REVIEW_WAIT = 10;

    /**
     * Flag for asterisks in NWWS output
     */
    @DynamicSerializeElement
    private boolean noAsterisk;

    /**
     * Flag for colons in NWWS output
     */
    @DynamicSerializeElement
    private boolean noColon;

    /**
     * Flag for minuses in NWWS output
     */
    @DynamicSerializeElement
    private boolean noMinus;

    /**
     * Flag for lower case in NWWS output
     */
    @DynamicSerializeElement
    private boolean noSmallLetters;

    /**
     * Valid time for Daily Intermediate
     */
    @DynamicSerializeElement
    private ClimateTime validIm;

    /**
     * Valid time for Daily Evening
     */
    @DynamicSerializeElement
    private ClimateTime validPm;

    /**
     * Flag for use of IM valid time
     */
    @DynamicSerializeElement
    private boolean useValidIm;

    /**
     * Flag for use of PM valid time
     */
    @DynamicSerializeElement
    private boolean useValidPm;

    /**
     * value of T1 threshold. max temperature GE user-defined value
     */
    @DynamicSerializeElement
    private int t1;

    /**
     * value of T2 threshold. max temperature GE user-defined value
     */
    @DynamicSerializeElement
    private int t2;

    /**
     * value of T3 threshold. max temperature LE user-defined value
     */
    @DynamicSerializeElement
    private int t3;

    /**
     * value of T4 threshold. min temperature GE user-defined value
     */
    @DynamicSerializeElement
    private int t4;

    /**
     * value of T5 threshold. min temperature LE user-defined value
     */
    @DynamicSerializeElement
    private int t5;

    /**
     * value of T6 threshold. min temperature LE user-defined value
     */
    @DynamicSerializeElement
    private int t6;

    /**
     * value of P1 threshold. precipitation amount GE user-defined value
     */
    @DynamicSerializeElement
    private float p1;

    /**
     * value of P2 threshold. precipitation amount GE user-defined value
     */
    @DynamicSerializeElement
    private float p2;

    /**
     * value of S1 threshold. snowfall amount GE user-defined value
     */
    @DynamicSerializeElement
    private float s1;

    /**
     * Display wait in minutes.
     */
    @DynamicSerializeElement
    private int displayWait = DEFAULT_DISPLAY_WAIT;
    
    /**
     * Review wait in minutes.
     */
    @DynamicSerializeElement
    private int reviewWait = DEFAULT_REVIEW_WAIT;
    
    public int getDisplayWait() {
        return displayWait;
    }

    public void setDisplayWait(int displayWait) {
        this.displayWait = displayWait;
    }

    public int getReviewWait() {
        return reviewWait;
    }

    public void setReviewWait(int reviewWait) {
        this.reviewWait = reviewWait;
    }

    /**
     * Empty constructor.
     */
    public ClimateGlobal() {
    }

    public ClimateTime getValidIm() {
        return validIm;
    }

    public void setValidIm(ClimateTime validIm) {
        this.validIm = validIm;
    }

    public ClimateTime getValidPm() {
        return validPm;
    }

    public void setValidPm(ClimateTime validPm) {
        this.validPm = validPm;
    }

    public boolean isNoAsterisk() {
        return noAsterisk;
    }

    public void setNoAsterisk(boolean noAsterisk) {
        this.noAsterisk = noAsterisk;
    }

    public boolean isNoColon() {
        return noColon;
    }

    public void setNoColon(boolean noColon) {
        this.noColon = noColon;
    }

    public boolean isNoMinus() {
        return noMinus;
    }

    public void setNoMinus(boolean noMinus) {
        this.noMinus = noMinus;
    }

    public boolean isNoSmallLetters() {
        return noSmallLetters;
    }

    public void setNoSmallLetters(boolean noSmallLetters) {
        this.noSmallLetters = noSmallLetters;
    }

    public boolean isUseValidIm() {
        return useValidIm;
    }

    public void setUseValidIm(boolean useValidIm) {
        this.useValidIm = useValidIm;
    }

    public boolean isUseValidPm() {
        return useValidPm;
    }

    public void setUseValidPm(boolean useValidPm) {
        this.useValidPm = useValidPm;
    }

    public int getT1() {
        return t1;
    }

    public void setT1(int t1) {
        this.t1 = t1;
    }

    public int getT2() {
        return t2;
    }

    public void setT2(int t2) {
        this.t2 = t2;
    }

    public int getT3() {
        return t3;
    }

    public void setT3(int t3) {
        this.t3 = t3;
    }

    public int getT4() {
        return t4;
    }

    public void setT4(int t4) {
        this.t4 = t4;
    }

    public int getT5() {
        return t5;
    }

    public void setT5(int t5) {
        this.t5 = t5;
    }

    public int getT6() {
        return t6;
    }

    public void setT6(int t6) {
        this.t6 = t6;
    }

    public float getP1() {
        return p1;
    }

    public void setP1(float p1) {
        this.p1 = p1;
    }

    public float getP2() {
        return p2;
    }

    public void setP2(float p2) {
        this.p2 = p2;
    }

    public float getS1() {
        return s1;
    }

    public void setS1(float s1) {
        this.s1 = s1;
    }

    public void setValidIm(String property) {
        this.validIm = new ClimateTime(property.split(" "));
    }

    public void setValidPm(String property) {
        this.validPm = new ClimateTime(property.split(" "));
    }

    public void setDataToMissing() {
        noAsterisk = false;
        noColon = false;
        noMinus = false;
        noSmallLetters = false;
        validIm = ClimateTime.getMissingClimateTime();
        validPm = ClimateTime.getMissingClimateTime();
        useValidIm = false;
        useValidPm = false;
        t1 = ParameterFormatClimate.MISSING;
        t2 = ParameterFormatClimate.MISSING;
        t3 = ParameterFormatClimate.MISSING;
        t4 = ParameterFormatClimate.MISSING;
        t5 = ParameterFormatClimate.MISSING;
        t6 = ParameterFormatClimate.MISSING;
        p1 = ParameterFormatClimate.MISSING_PRECIP;
        p2 = ParameterFormatClimate.MISSING_PRECIP;
        s1 = ParameterFormatClimate.MISSING_SNOW;
    }

    /**
     * get default preferences
     */
    public static ClimateGlobal getDefaultGlobalValues() {
        ClimateGlobal globals = new ClimateGlobal();
        globals.setNoSmallLetters(true);
        globals.setNoMinus(true);
        globals.setNoColon(false);
        globals.setNoAsterisk(true);

        globals.setUseValidPm(true);
        ClimateTime validPm = ClimateTime.getLocalTime();
        validPm.setAmpm(ClimateTime.PM_STRING);
        validPm.setHour(5);
        validPm.setMin(0);
        validPm.setZone(TimeZone.getDefault().getID().substring(0, 3));
        globals.setValidPm(validPm);

        globals.setUseValidIm(true);
        ClimateTime validIm = ClimateTime.getLocalTime();
        validIm.setAmpm(ClimateTime.AM_STRING);
        validIm.setHour(9);
        validIm.setMin(0);
        validIm.setZone(TimeZone.getDefault().getID().substring(0, 3));
        globals.setValidIm(validIm);

        globals.setT1(ParameterFormatClimate.MISSING);
        globals.setT2(ParameterFormatClimate.MISSING);
        globals.setT3(ParameterFormatClimate.MISSING);
        globals.setT4(ParameterFormatClimate.MISSING);
        globals.setT5(ParameterFormatClimate.MISSING);
        globals.setT6(ParameterFormatClimate.MISSING);

        globals.setP1(ParameterFormatClimate.MISSING_PRECIP);
        globals.setP2(ParameterFormatClimate.MISSING_PRECIP);
        globals.setS1(ParameterFormatClimate.MISSING_SNOW);

        return globals;
    }

    public static ClimateGlobal getMissingClimateGlobal() {
        ClimateGlobal climateGlobal = new ClimateGlobal();
        climateGlobal.setDataToMissing();
        return climateGlobal;
    }
}
