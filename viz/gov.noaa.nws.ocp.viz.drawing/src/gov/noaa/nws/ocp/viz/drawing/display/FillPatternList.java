/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.util.EnumMap;

/**
 * This object contains a list of defined Fill Patterns that can be applied to a
 * multi-point line path.
 *
 * <P>
 * This class is implemented as a singleton, and the predefined Fill Patterns
 * are constructed when the instance is created. Users can get a reference to
 * this object using the static method getInstance().
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public class FillPatternList {

    /**
     * An Enumeration of available Fill Patterns
     */
    public enum FillPattern {
        FILL_PATTERN_0,
        FILL_PATTERN_1,
        FILL_PATTERN_2,
        FILL_PATTERN_3,
        FILL_PATTERN_4,
        FILL_PATTERN_5,
        FILL_PATTERN_6,
        SOLID,
        TRANSPARENCY
    }

    /*
     * The singleton instance
     */
    private static FillPatternList instance = null;

    /**
     * An EnumMap holding the Fill Patterns
     */
    private EnumMap<FillPattern, byte[]> patternMap;

    /**
     * Constructor used by the getInstance method.
     */
    protected FillPatternList() {
        patternMap = new EnumMap<>(FillPattern.class);
        initialize();
    }

    /**
     * Static method used to request the instance of the FillPatternList object.
     * 
     * @return reference to this object
     */
    public static synchronized FillPatternList getInstance() {

        if (instance == null) {
            instance = new FillPatternList();
        }
        return instance;

    }

    /**
     * Initialize the HashMap holding the Fill Patterns
     */
    private void initialize() {

        loadInternal();
    }

    /**
     * Gets the fill pattern mapped to the requested FillPattern enumerated type
     * 
     * @param key
     *            Requested Fill Pattern
     * @return The requested Fill Pattern. Returns null, if a SOLID or
     *         TRANSPARENCY FillPattern is requested.
     */
    public byte[] getFillPattern(FillPattern key) {
        if (key.equals(FillPattern.SOLID)
                || key.equals(FillPattern.TRANSPARENCY)) {
            return new byte[1];
        } else {
            return patternMap.get(key);
        }
    }

    /**
     * Constructs the EnumMap holding all the FillPatterns.
     * <P>
     * Currently all FillPatterns are constructed in line in the code. We will
     * likely want all these patterns defined in an an external source in the
     * future. At that point, this method may load the LinePatterns from an
     * external file(s), instead of constructing them explicitly.
     */
    private void loadInternal() {

        int[] pattern0 = { 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA,
                0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA,
                0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55,
                0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55,
                0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA,
                0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA,
                0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55,
                0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55,
                0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA,
                0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA,
                0xAA, 0x55, 0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55,
                0x55, 0x55, 0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55,
                0x55, 0xAA, 0xAA, 0xAA, 0xAA, 0x55, 0x55, 0x55, 0x55, };

        byte[] ptn0 = new byte[pattern0.length];
        for (int i = 0; i < pattern0.length; i++) {
            ptn0[i] = (byte) pattern0[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_0, ptn0);

        int[] pattern1 = { 0x80, 0x80, 0x80, 0x80, 0x40, 0x40, 0x40, 0x40, 0x20,
                0x20, 0x20, 0x20, 0x10, 0x10, 0x10, 0x10,

                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                0x80, 0x80, 0x80, 0x80, 0x40, 0x40, 0x40, 0x40, 0x20, 0x20,
                0x20, 0x20, 0x10, 0x10, 0x10, 0x10,

                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                0x80, 0x80, 0x80, 0x80, 0x40, 0x40, 0x40, 0x40, 0x20, 0x20,
                0x20, 0x20, 0x10, 0x10, 0x10, 0x10,

                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                0x80, 0x80, 0x80, 0x80, 0x40, 0x40, 0x40, 0x40, 0x20, 0x20,
                0x20, 0x20, 0x10, 0x10, 0x10, 0x10,

                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        byte[] ptn1 = new byte[pattern1.length];
        for (int i = 0; i < pattern1.length; i++) {
            ptn1[i] = (byte) pattern1[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_1, ptn1);

        int[] pattern2 = { 0x80, 0x00, 0x80, 0x00, 0x40, 0x00, 0x40, 0x00, 0x20,
                0x00, 0x20, 0x00, 0x10, 0x00, 0x10, 0x00, 0x08, 0x00, 0x08,
                0x00, 0x04, 0x00, 0x04, 0x00, 0x02, 0x00, 0x02, 0x00, 0x01,
                0x00, 0x01, 0x00, 0x00, 0x80, 0x00, 0x80, 0x00, 0x40, 0x00,
                0x40, 0x00, 0x20, 0x00, 0x20, 0x00, 0x10, 0x00, 0x10, 0x00,
                0x08, 0x00, 0x08, 0x00, 0x04, 0x00, 0x04, 0x00, 0x02, 0x00,
                0x02, 0x00, 0x01, 0x00, 0x01, 0x80, 0x00, 0x80, 0x00, 0x40,
                0x00, 0x40, 0x00, 0x20, 0x00, 0x20, 0x00, 0x10, 0x00, 0x10,
                0x00, 0x08, 0x00, 0x08, 0x00, 0x04, 0x00, 0x04, 0x00, 0x02,
                0x00, 0x02, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x80, 0x00,
                0x80, 0x00, 0x40, 0x00, 0x40, 0x00, 0x20, 0x00, 0x20, 0x00,
                0x10, 0x00, 0x10, 0x00, 0x08, 0x00, 0x08, 0x00, 0x04, 0x00,
                0x04, 0x00, 0x02, 0x00, 0x02, 0x00, 0x01, 0x00, 0x01, };

        byte[] ptn2 = new byte[pattern2.length];
        for (int i = 0; i < pattern2.length; i++) {
            ptn2[i] = (byte) pattern2[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_2, ptn2);

        int[] pattern3 = { 0x08, 0x08, 0x08, 0x08, 0x04, 0x04, 0x04, 0x04, 0x02,
                0x02, 0x02, 0x02, 0x01, 0x01, 0x01, 0x01, 0x80, 0x80, 0x80,
                0x80, 0x40, 0x40, 0x40, 0x40, 0x20, 0x20, 0x20, 0x20, 0x10,
                0x10, 0x10, 0x10, 0x08, 0x08, 0x08, 0x08, 0x04, 0x04, 0x04,
                0x04, 0x02, 0x02, 0x02, 0x02, 0x01, 0x01, 0x01, 0x01, 0x80,
                0x80, 0x80, 0x80, 0x40, 0x40, 0x40, 0x40, 0x20, 0x20, 0x20,
                0x20, 0x10, 0x10, 0x10, 0x10, 0x08, 0x08, 0x08, 0x08, 0x04,
                0x04, 0x04, 0x04, 0x02, 0x02, 0x02, 0x02, 0x01, 0x01, 0x01,
                0x01, 0x80, 0x80, 0x80, 0x80, 0x40, 0x40, 0x40, 0x40, 0x20,
                0x20, 0x20, 0x20, 0x10, 0x10, 0x10, 0x10, 0x08, 0x08, 0x08,
                0x08, 0x04, 0x04, 0x04, 0x04, 0x02, 0x02, 0x02, 0x02, 0x01,
                0x01, 0x01, 0x01, 0x80, 0x80, 0x80, 0x80, 0x40, 0x40, 0x40,
                0x40, 0x20, 0x20, 0x20, 0x20, 0x10, 0x10, 0x10, 0x10, };

        byte[] ptn3 = new byte[pattern3.length];
        for (int i = 0; i < pattern3.length; i++) {
            ptn3[i] = (byte) pattern3[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_3, ptn3);

        // Modified pattern 4 to match fill patterns in NMAP -- J. Wu 05/21/2013
        int[] pattern4 = { 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80,
                0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80,
                0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0xff,
                0x80, 0xff, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00,
                0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00,
                0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00,
                0x80, 0x80, 0xff, 0x80, 0xff, 0x80, 0x00, 0x80, 0x00, 0x80,
                0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80,
                0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80,
                0x00, 0x80, 0x00, 0xff, 0x80, 0xff, 0x80, 0x00, 0x80, 0x00,
                0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00,
                0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00, 0x80, 0x00,
                0x80, 0x00, 0x80, 0x00, 0x80, 0x80, 0xff, 0x80, 0xff };

        byte[] ptn4 = new byte[pattern4.length];
        for (int i = 0; i < pattern4.length; i++) {
            ptn4[i] = (byte) pattern4[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_4, ptn4);

        int[] pattern5 = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x33,
                0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC,
                0xCC, 0xCC, 0xCC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xCC, 0xCC, 0xCC,
                0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33,
                0x33, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xCC,
                0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x33, 0x33, 0x33, 0x33,
                0x33, 0x33, 0x33, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, };

        byte[] ptn5 = new byte[pattern5.length];
        for (int i = 0; i < pattern5.length; i++) {
            ptn5[i] = (byte) pattern5[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_5, ptn5);

        int[] pattern6 = { 0x88, 0x88, 0x88, 0x88, 0x44, 0x44, 0x44, 0x44, 0x22,
                0x22, 0x22, 0x22, 0x11, 0x11, 0x11, 0x11, 0x88, 0x88, 0x88,
                0x88, 0x44, 0x44, 0x44, 0x44, 0x22, 0x22, 0x22, 0x22, 0x11,
                0x11, 0x11, 0x11, 0x88, 0x88, 0x88, 0x88, 0x44, 0x44, 0x44,
                0x44, 0x22, 0x22, 0x22, 0x22, 0x11, 0x11, 0x11, 0x11, 0x88,
                0x88, 0x88, 0x88, 0x44, 0x44, 0x44, 0x44, 0x22, 0x22, 0x22,
                0x22, 0x11, 0x11, 0x11, 0x11, 0x88, 0x88, 0x88, 0x88, 0x44,
                0x44, 0x44, 0x44, 0x22, 0x22, 0x22, 0x22, 0x11, 0x11, 0x11,
                0x11, 0x88, 0x88, 0x88, 0x88, 0x44, 0x44, 0x44, 0x44, 0x22,
                0x22, 0x22, 0x22, 0x11, 0x11, 0x11, 0x11, 0x88, 0x88, 0x88,
                0x88, 0x44, 0x44, 0x44, 0x44, 0x22, 0x22, 0x22, 0x22, 0x11,
                0x11, 0x11, 0x11, 0x88, 0x88, 0x88, 0x88, 0x44, 0x44, 0x44,
                0x44, 0x22, 0x22, 0x22, 0x22, 0x11, 0x11, 0x11, 0x11, };

        byte[] ptn6 = new byte[pattern6.length];
        for (int i = 0; i < pattern6.length; i++) {
            ptn6[i] = (byte) pattern6[i];
        }
        patternMap.put(FillPattern.FILL_PATTERN_6, ptn6);

    }

}
