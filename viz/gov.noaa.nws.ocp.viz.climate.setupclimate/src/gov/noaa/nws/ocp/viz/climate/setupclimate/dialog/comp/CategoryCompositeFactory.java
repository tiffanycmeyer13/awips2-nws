/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.viz.climate.setupclimate.ClimateReportElementCategory;

/**
 * 
 * This factory class is used to create composites specific for each category in
 * climate report setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#   Engineer    Description
 * ------------ --------- ----------- --------------------------------
 * 21 DEC 2016  20640     jwu        Initial creation
 * 09 FEB 2017  20640     jwu        Use ClimateGlobal for preference values.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class CategoryCompositeFactory {

    public static AbstractCategoryComp create(Composite parent,
            ClimateReportElementCategory elemCat, Font majorFont,
            ClimateGlobal preferenceValues) {

        AbstractCategoryComp catComp = null;

        switch (elemCat) {

        case TEMPERATURE:
            catComp = new TemperatureComp(parent, SWT.NONE, preferenceValues,
                    majorFont);
            break;

        case PRECIPITATION:
            catComp = new PrecipitationComp(parent, SWT.NONE, preferenceValues,
                    majorFont);
            break;

        case SNOWFALL:
            catComp = new SnowfallComp(parent, SWT.NONE, preferenceValues,
                    majorFont);
            break;

        case DEGREE_DAYS:
            catComp = new DegreeDaysComp(parent, SWT.NONE, preferenceValues,
                    majorFont);
            break;

        case WIND:
            catComp = new WindComp(parent, SWT.NONE, preferenceValues,
                    majorFont);

            break;
        case RELATIVE_HUMIDITY:
            catComp = new RelativeHumidityComp(parent, SWT.NONE,
                    preferenceValues, majorFont);
            break;

        case SKY_COVER:
            catComp = new SkycoverComp(parent, SWT.NONE, preferenceValues,
                    majorFont);

            break;
        case WEATHER:
            catComp = new WeatherComp(parent, SWT.NONE, preferenceValues,
                    majorFont);

            break;
        case TEMPRECORD:
            catComp = new TemperatureRecordComp(parent, SWT.NONE,
                    preferenceValues, majorFont);
            break;

        case SUNRISE_SUNSET:
            catComp = new SunriseSunsetComp(parent, SWT.NONE, preferenceValues,
                    majorFont);
            break;

        default:
            /*
             * Do nothing.
             */
            break;
        }

        return catComp;
    }
}
