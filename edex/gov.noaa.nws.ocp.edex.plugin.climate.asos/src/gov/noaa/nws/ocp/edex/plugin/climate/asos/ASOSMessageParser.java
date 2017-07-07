/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos;

import java.util.Map;
import java.util.Set;
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
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public abstract class ASOSMessageParser {

    /** Regular expression for ASOS message types: DS | MS */
    protected static final Pattern ASOS_MSG_TYPE = Pattern
            .compile("^\\w{3,4}\\s+(?<type>\\w{2})\\s+\\.*");

    protected Set<String> namedGroups = null;

    protected Pattern groupNamePattern = null;

    protected Map<String, String> namedAsosMap = null;

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

}
