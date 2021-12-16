/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import java.time.ZoneId;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Utility enum determine ATCF time zones used in advisories.
 *
 * <pre>
 *
 * nhc_writeadv.f (setTimeZone()):
 *
 * Time zone is determine from "advisory time" (tau = 3 hours) longitude
 * in OFCL stormID.fst file.
 *
 *   Atlantic:
 *          East of  70.0W: AST
 *          70.0W to 84.9W: EST/EDT
 *          West of  84.9W: CST/CDT
 *
 *   East Pacific (east of 140 W):
 *          East of   106.0W: CST/CDT
 *          106.0W to 114.9W: MST/MDT
 *          West of   114.9W: PST/PDT
 *
 *   Central Pacific (west of 140 W):
 *          HST
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 29, 2020 81820      wpaintsil   Initial creation
 * Jan 26, 2021 86746      jwu         Add zoneId, timeoffset etc.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
@DynamicSerialize
public enum AdvisoryTimeZone {

    /**
     * UTC
     */
    UTC("Coordinated Universal Time", "U", "S", 0, ZoneId.of("Etc/UTC")),

    /**
     * Alaska
     */
    AST("Atlantic Standard Time", "A", "S", -4, ZoneId.of("Canada/Atlantic")),

    /**
     * Eastern daylight savings
     */
    EDT("Eastern Daylight Time", "E", "D", -4, ZoneId.of("US/Eastern")),

    /**
     * Eastern standard
     */
    EST("Eastern Standard Time", "E", "S", -5, ZoneId.of("US/Eastern")),

    /**
     * Central daylight savings
     */
    CDT("Central Daylight Time", "C", "D", -5, ZoneId.of("US/Central")),

    /**
     * Central standard
     */
    CST("Central Standard Time", "C", "S", -6, ZoneId.of("US/Central")),

    /**
     * Mountain daylight savings
     */
    MDT("Mountain Daylight Time", "M", "D", -6, ZoneId.of("US/Mountain")),

    /**
     * Mountain standard
     */
    MST("Mountain Standard Time", "M", "S", -7, ZoneId.of("US/Mountain")),

    /**
     * Pacific daylight savings
     */
    PDT("Pacific Daylight Time", "P", "D", -7, ZoneId.of("US/Pacific")),

    /**
     * Pacific standard
     */
    PST("Pacific Standard Time", "P", "S", -8, ZoneId.of("US/Pacific")),

    /**
     * Hawaii
     */
    HST("Hawaii Standard Time", "H", "S", -10, ZoneId.of("US/Hawaii"));

    // Time zone name.
    @DynamicSerializeElement
    private String fullName;

    // Time zone designator ('A', 'E', 'C', or 'H')
    @DynamicSerializeElement
    private String zoneDesignator;

    /*
     * Daylight Savings Time indicator ('D' or 'S'). Might be adjusted for
     * daylight/standard time.
     */
    @DynamicSerializeElement
    private String dayLightSavingDesignator;

    // Time zone offset to UTC (hours)
    @DynamicSerializeElement
    private int timeOffset;

    // Time zone id
    @DynamicSerializeElement
    private ZoneId zoneId;

    /**
     * Constructor
     *
     * @param fullName
     *            time zone descriptive name
     * @param zoneDesignator
     *            A/E/C/H
     * @param dayLightSavingDesignator
     *            D/S
     * @param timeOffset
     *            Time zone offset to UTC
     * @param zonedId
     *            ZoneId
     */
    private AdvisoryTimeZone(final String fullName, final String zoneDesignator,
            final String dayLightSavingDesignator, final int timeOffset,
            final ZoneId zoneId) {
        this.fullName = fullName;
        this.zoneDesignator = zoneDesignator;
        this.dayLightSavingDesignator = dayLightSavingDesignator;
        this.timeOffset = timeOffset;
        this.zoneId = zoneId;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return the zoneDesignator
     */
    public String getZoneDesignator() {
        return zoneDesignator;
    }

    /**
     * @return the dayLightSavingDesignator
     */
    public String getDayLightSavingDesignator() {
        return dayLightSavingDesignator;
    }

    /**
     * @return the timeOffset
     */
    public int getTimeOffset() {
        return timeOffset;
    }

    /**
     * @return the zoneId
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Determine time zone from "advisory time" (tau = 3 hours) longitude in
     * OFCL stormID.fst file.
     *
     * @param basinDesignator
     *            Basin designator ('A', 'P', or 'C') - previously determined
     *            from initial/Best Track time (tau = 0 hours) in OFCL
     *            stormID.fst file
     * @param tzlon
     *            Longitude (degrees East, not West) at tau = 3
     * @param dst
     *            Daylight Savings Time indicator (true - dayLightsaving on)
     * @param lon5d
     *            Longitude for determining Hawaii time zone
     * @return
     */
    public static AdvisoryTimeZone getTimeZone(String basinDesignator,
            float tzlon, boolean dst, float lon5d) {

        AdvisoryTimeZone tz = UTC;
        if (basinDesignator != null && !basinDesignator.isEmpty()) {
            if ("A".equals(basinDesignator)) {
                // Atlantic basin
                if (tzlon > -70.) {
                    tz = AST;
                } else if (tzlon > -85. && tzlon <= -70.) {
                    tz = (dst) ? EDT : EST;
                } else if (tzlon <= -85.) {
                    tz = (dst) ? CDT : CST;
                }
            } else if ("P".equals(basinDesignator)) {
                // East Pacific basin (Lon >= -140, east of 140W)
                if (tzlon > -106.) {
                    tz = (dst) ? CDT : CST;
                } else if (tzlon > -115. && tzlon <= -106.) {
                    tz = (dst) ? MDT : MST;
                } else if (tzlon <= -115.) {
                    tz = (dst) ? PDT : PST;
                }

                /*
                 * Projected basin crosser. Override time zone based on 120-hr
                 * forecast longitude.
                 */
                if (lon5d < -140.) {
                    tz = HST;
                }
            } else if ("C".equals(basinDesignator)) {
                // Central Pacific basin (Lon < -140, west of 140 W)
                tz = HST;
            }
        }

        return tz;
    }
}
