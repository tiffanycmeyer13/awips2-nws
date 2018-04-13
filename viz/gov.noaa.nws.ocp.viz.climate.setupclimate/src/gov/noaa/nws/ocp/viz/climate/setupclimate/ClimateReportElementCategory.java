/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate;

/**
 * 
 * Choices for report element's categories in "Setup/Edit Climate".
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#   Engineer    Description
 * ------------ --------- ----------- --------------------------------
 * 28 OCT 2016    20640    jwu      Initial creation
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public enum ClimateReportElementCategory {

    TEMPERATURE("Temperature"),

    PRECIPITATION("Precipitation"),

    SNOWFALL("Snowfall"),

    DEGREE_DAYS("Degree Days"),

    WIND("Wind"),

    RELATIVE_HUMIDITY("Relative Humidity"),

    SKY_COVER("Sky Cover"),

    WEATHER("Weather"),

    TEMPRECORD("Temperature"),

    SUNRISE_SUNSET("Sunrise/Sunset");

    private final String category;

    /**
     * Constructor
     *
     * @param category
     */
    private ClimateReportElementCategory(final String cat) {
        this.category = cat;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Check if a category is a auxiliary one (last two)
     * 
     * @param cat
     * @return
     */
    public boolean isAuxiliary() {
        switch (this) {
        case TEMPRECORD:
        case SUNRISE_SUNSET:
            return true;
        default:
            return false;
        }
    }
}
