/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos;

import java.util.regex.Pattern;

import gov.noaa.nws.ocp.common.dataplugin.climate.asos.ClimateASOSMessageRecord;

/**
 * Abstract class of ASOS message parser
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 7, 2016  16962      pwang       Initial creation
 * 05 MAY 2017  33104      amoore      Minor clean up.
 * 07 SEP 2017  37725      amoore      Fix time representations in SQL (needs :).
 * 03 NOV 2017  36736      amoore      Make several parts and logic static.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public abstract class ASOSMessageParser {

    /** Regular expression for ASOS message types: DS | MS */
    protected static final Pattern ASOS_MSG_TYPE = Pattern
            .compile("^\\w{3,4}\\s+(?<type>\\w{2})\\s+\\.*");

    /**
     * Pattern for group names in regex.
     */
    protected static final Pattern GROUP_NAME_PATTERN = Pattern
            .compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    /**
     * parse must be implemented
     * 
     * @param message
     * @return
     */
    public abstract ClimateASOSMessageRecord parse(String message);

    /**
     * Convert field name to the corresponding setter method
     * 
     * @param fieldName
     * @return
     */
    public String getSetterMethodName(String fieldName) {
        String prop = Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
        return "set" + prop;
    }

    /**
     * 
     * @param time
     *            some hour-minute time string without a colon in the middle.
     *            Assumed there are two characters present for minutes, and 1 or
     *            2 for hour.
     * @return the same time, but with ":" inserted between hour and minute
     */
    protected static String adjustTimeString(String time) {
        StringBuilder newTime = new StringBuilder();
        if (time.length() < 4) {
            newTime.append(time.substring(0, 1)).append(":")
                    .append(time.substring(1));
        } else {
            newTime.append(time.substring(0, 2)).append(":")
                    .append(time.substring(2));
        }

        return newTime.toString();
    }
}
