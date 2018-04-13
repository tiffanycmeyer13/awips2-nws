/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.psh;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * This enum holds the time zone abbreviations in PSH.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 26 JUN 2017  #35269     jwu         Initial creation
 * 05 SEP 2017  #37365     jwu         Add methods to format and find time difference.
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PshTimeZone")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum PshTimeZone {

    /**
     * Atlantic.
     */
    A(0, "Atlantic", ZoneId.of("Canada/Atlantic")),

    /**
     * Eastern.
     */
    E(1, "Eastern", ZoneId.of("US/Eastern")),

    /**
     * Central.
     */
    C(2, "Central", ZoneId.of("US/Central")),

    /**
     * Mountain.
     */
    M(3, "Mountain", ZoneId.of("US/Mountain")),

    /**
     * Pacific.
     */
    P(4, "Pacific", ZoneId.of("US/Pacific")),

    /**
     * Hawaii-Aleutian.
     */
    H(5, "Hawaii-Aleutian", ZoneId.of("US/Hawaii")),

    /**
     * Chamorro (formally Guam).
     */
    CH(6, "Chamorro", ZoneId.of("Pacific/Guam"));

    @DynamicSerializeElement
    private int value;

    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private ZoneId zonedId;

    /**
     * Date/Time format.
     */
    private static DateTimeFormatter DT_FORMATTER = DateTimeFormatter
            .ofPattern("hmm a zzz E MMM d yyyy");

    /**
     * Constructor
     * 
     * @param iValue
     * @param name
     * @param zonedId
     */
    private PshTimeZone(final int iValue, final String name,
            final ZoneId zonedId) {
        this.value = iValue;
        this.name = name;
        this.zonedId = zonedId;
    }

    /**
     * @return enum value.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param iValue
     *            value.
     */
    public void setValue(int iValue) {
        value = iValue;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the zonedId
     */
    public ZoneId getZonedId() {
        return zonedId;
    }

    /**
     * @param zonedId
     *            the zonedId to set
     */
    public void setZonedId(ZoneId zonedId) {
        this.zonedId = zonedId;
    }

    /**
     * Get the time offset (hours) with UTC.
     * 
     * @return time offset
     */
    public int getTimeOffset() {
        return zonedId.getRules().getOffset(TimeUtil.newDate().toInstant())
                .getTotalSeconds() / TimeUtil.SECONDS_PER_HOUR;
    }

    /**
     * Get the time offset (hours) with UTC for a given date.
     * 
     * @return time offset
     */
    public int getTimeOffset(Date date) {
        return zonedId.getRules().getOffset(date.toInstant()).getTotalSeconds()
                / 3600;
    }

    /**
     * Check if the daylight saving is on.
     * 
     * @return boolean
     */
    public boolean isDaylightSaving() {
        return zonedId.getRules()
                .isDaylightSavings(TimeUtil.newDate().toInstant());
    }

    /**
     * Get the formatted date/time string with the same instant of system time.
     *
     * @return Formatted date/time string
     */
    public String getZonedTimeString() {

        ZonedDateTime zdt = getZonedTime();

        return zdt.format(DT_FORMATTER);
    }

    /**
     * Get the date/time with the same instant of system time.
     * 
     * @return zonedDateTime
     */
    public ZonedDateTime getZonedTime() {

        ZoneId defaultZoneId = ZoneId.systemDefault();

        ZonedDateTime zonedDateTime = TimeUtil.newDate().toInstant()
                .atZone(defaultZoneId);

        return zonedDateTime.withZoneSameInstant(zonedId);
    }

    /**
     * Find a time zone based on the short name or full name.
     * 
     * @param name
     *            name such as A/C/E/P/M/CH or "Atlantic".
     */
    public static PshTimeZone getPshTimeZone(String name) {
        PshTimeZone ptz = E;
        if (name != null) {
            String tmzn = name.trim();
            for (PshTimeZone tz : PshTimeZone.values()) {
                if (tz.toString().equals(tmzn.toUpperCase())
                        || tz.name.equals(tmzn)) {
                    ptz = tz;
                    break;
                }
            }
        }

        return ptz;
    }
}