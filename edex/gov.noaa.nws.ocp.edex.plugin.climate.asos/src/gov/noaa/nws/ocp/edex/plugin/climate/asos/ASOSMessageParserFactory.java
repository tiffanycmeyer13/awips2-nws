package gov.noaa.nws.ocp.edex.plugin.climate.asos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory implementation for DSM / MSM parser
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 12, 2016 16962      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class ASOSMessageParserFactory {
    /** Regular expression for ASOS message types: DS | MS */
    protected static final Pattern ASOS_MSG_TYPE = Pattern
            .compile("\\w{3,4}\\s+(?<msgType>\\w{2})\\s+.*");

    public static ASOSMessageParser getASOSMessageParser(String message) {
        ASOSMessageParser parser = null;

        String msgType = null;
        Matcher matcher = ASOS_MSG_TYPE.matcher(message);
        if (matcher.matches()) {
            msgType = matcher.group("msgType");
        }

        if (msgType != null) {
            if (msgType.equalsIgnoreCase("DS")) {
                parser = new DailySummaryMessageParser();
            } else if (msgType.equalsIgnoreCase("MS")) {
                parser = new MonthlySummaryMessageParser();
            }
        }

        return parser;
    }

}