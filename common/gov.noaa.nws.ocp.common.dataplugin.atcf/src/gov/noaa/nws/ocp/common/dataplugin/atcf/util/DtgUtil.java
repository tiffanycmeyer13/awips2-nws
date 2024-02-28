/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Functions to format Date Time Group (DTG) strings.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 20, 2021 93824      dfriedman   Initial version
 *
 * </pre>
 *
 * @version 1.0
 */
public class DtgUtil {

    private static final DateTimeFormatter DTG_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMddHH");

    private static final DateTimeFormatter DTG_FORMATTER_LONG = DateTimeFormatter
            .ofPattern("yyyyMMddHHmm");

    public static String format(LocalDateTime dt) {
        return DTG_FORMATTER.format(dt);
    }

    public static String format(Date date) {
        return format(LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC));
    }

    public static String format(Calendar calendar) {
        return format(calendar.getTime());
    }

    public static String formatLong(LocalDateTime dt) {
        return DTG_FORMATTER_LONG.format(dt);
    }

    public static String formatLong(Date date) {
        return format(LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC));
    }

    public static String formatLong(Calendar calendar) {
        return format(calendar.getTime());
    }

}
