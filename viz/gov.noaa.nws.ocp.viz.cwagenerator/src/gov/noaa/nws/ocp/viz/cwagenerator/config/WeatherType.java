/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

/**
 * 
 * Enum for weather types
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 14, 2020  75767     wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public enum WeatherType {
    THUNDERSTORM("Thunderstorm", "Thunderstorm"),
    IFRLIFR("IFR/LIFR", "IFRLIFR"),
    TURBLLWS("Turb/LLWS", "TurbLLWS"),
    ICIINGFRZA("Icing/FRZA", "IcingFRZA"),
    BLDUBLSA("BLDU/BLSA", "BLDUBLSA"),
    VOLCANO("Volcano", "Volcano"),
    CANMAN("Can/Man", "CancelManual"),
    MIS("CWS/MIS", "CWSMIS");

    private final String name;

    private final String type;

    WeatherType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
