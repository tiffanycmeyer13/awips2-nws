/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

/**
 * Enum for storm development types
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 07, 2018 52657      wpaintsil   Initial creation.
 * May 03, 2019 62845      dmanzella   added missing entries
 * Aug 05, 2019 66888      jwu         Add getIntensity().
 * Apr 22, 2021 88729      jwu         Update storm type threshold.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public enum StormDevelopment {
    DB("DB", "disturbance"),
    TD("TD", "tropical depression"),
    TS("TS", "tropical storm"),
    TY("TY", "typhoon"),
    ST("ST", "super typhoon"),
    TC("TC", "tropical cyclone"),
    HU("HU", "hurricane"),
    SD("SD", "subtrop. depression"),
    SS("SS", "subtrop. storm"),
    EX("EX", "extratrop. cyclone"),
    IN("IN", "inland"),
    DS("DS", "dissipating"),
    LO("LO", "low"),
    WV("WV", "trop. wave"),
    ET("ET", "extrapolated"),
    PT("PT", "post-tropical cyclone"),
    XX("XX", "unknown");

    private String shortIntensityString;

    private String longIntensityString;

    private StormDevelopment(String shortIntensityString,
            String longIntensityString) {
        this.shortIntensityString = shortIntensityString;
        this.longIntensityString = longIntensityString;
    }

    public static String[] getStormTypeStrings() {
        int size = StormDevelopment.values().length;

        String[] stormTypeStrings = new String[size];
        for (int ii = 0; ii < size; ii++) {
            stormTypeStrings[ii] = StormDevelopment.values()[ii]
                    .getFullIntensityString();
        }

        return stormTypeStrings;
    }

    /**
     * Get all shortIntensityString parameter.
     * 
     * @param String[]
     *            shortIntensityString
     * @return
     */
    public static String[] getStormShortIntensityStrings() {
        int size = StormDevelopment.values().length;

        String[] stormTypeStrings = new String[size];
        for (int ii = 0; ii < size; ii++) {
            stormTypeStrings[ii] = StormDevelopment.values()[ii]
                    .getShortIntensityString();
        }

        return stormTypeStrings;
    }

    /**
     * @return the shortIntensityString
     */
    public String getShortIntensityString() {
        return shortIntensityString;
    }

    /**
     * Return a fullIntensityString matching the shortIntensityString parameter.
     * 
     * @param shortIntensityString
     * @return
     */
    public static String getFullIntensityString(String shortIntensityString) {
        for (StormDevelopment type : StormDevelopment.values()) {
            if (type.getShortIntensityString().equals(shortIntensityString)) {
                return type.getFullIntensityString();
            }
        }
        return null;
    }

    /**
     * Return a shortIntensityString matching the fullIntensityString parameter.
     * 
     * @param fullIntensityString
     * @return
     */
    public static String getShortIntensityString(String fullIntensityString) {
        for (StormDevelopment type : StormDevelopment.values()) {
            if (type.getFullIntensityString().equals(fullIntensityString)) {
                return type.getShortIntensityString();
            }
        }
        return null;
    }

    /**
     * @return the fullIntensityString
     */
    public String getFullIntensityString() {
        return shortIntensityString + " - " + longIntensityString;
    }

    /**
     * Gets a storm's intensity based on it's speed in knots.
     *
     * @param speed
     * @return intensity
     */
    public static String getIntensity(float speed) {
        return getIntensity(null, speed);
    }

    /**
     * Gets a storm's intensity based on a given initial storm type and wind
     * speed in knots.
     * 
     * Note - this is the algorithm from NHC.
     * 
     * @param initialStormType
     * @param speed
     * @return intensity
     */
    public static String getIntensity(String initialStormType, float speed) {

        String ret = "";
        String iniTyp = "";

        if (initialStormType != null) {
            iniTyp = initialStormType.toUpperCase();
        }

        if (DB.name().equals(iniTyp) || LO.name().equals(iniTyp)
                || EX.name().equals(iniTyp)) {
            ret = iniTyp;
        } else if (SD.name().equals(iniTyp) || SS.name().equals(iniTyp)) {
            if (speed < 35) {
                ret = "SD";
            } else if (speed < 64) {
                ret = "SS";
            } else {
                ret = "HU";
            }
        } else {
            if (speed < 35) {
                ret = "TD";
            } else if (speed < 64) {
                ret = "TS";
            } else {
                ret = "HU";
            }
        }

        return ret;
    }

    /**
     * @return the longIntensityString
     */
    public String getLongIntensityString() {
        return longIntensityString;
    }

    /**
     * Return a longIntensityString matching the shortIntensityString parameter.
     * 
     * @param shortIntensityString
     * @return
     */
    public static String getLongIntensityString(String shortIntensityString) {
        for (StormDevelopment type : StormDevelopment.values()) {
            if (type.getShortIntensityString().equals(shortIntensityString)) {
                return type.getLongIntensityString();
            }
        }
        return null;
    }

    /**
     * Return a longIntensityString matching the shortIntensityString
     * parameter.If "replace" is true, the abbreviated word in replaced with its
     * full-spelling.
     * 
     * @param shortIntensityString
     * @return
     */
    public static String getLongIntensityString(String shortIntensityString,
            boolean replace) {
        for (StormDevelopment type : StormDevelopment.values()) {
            if (type.getShortIntensityString().equals(shortIntensityString)) {
                String lstr = type.getLongIntensityString();
                if (("SD".equals(shortIntensityString)
                        || "SS".equals(shortIntensityString)) && replace) {
                    lstr = lstr.replace("subtrop.", "subtropical");
                } else if ("EX".equals(shortIntensityString) && replace) {
                    lstr = lstr.replace("extratrop.", "extratropical");
                }

                return lstr;
            }
        }
        return null;
    }

    /**
     * @param longIntensityString
     *            the longIntensityString to set
     */
    protected void setLongIntensityString(String longIntensityString) {
        this.longIntensityString = longIntensityString;
    }

}