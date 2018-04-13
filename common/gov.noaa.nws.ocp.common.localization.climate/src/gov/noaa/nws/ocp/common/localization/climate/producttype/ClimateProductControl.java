/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.producttype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;

/**
 * Climate product control (a component in a climate product type), for
 * determining which weather elements are reported upon in the climate summary.
 * 
 * This class holds the same information as in legacy files such as
 * "control_am_BWI".
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 * Feb 16, 2017 21099      wpaintsil    Serialization
 * 13 APR  2017 33104      amoore       Address comments from review.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimateProductControl")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateProductControl {

    /**
     * Flag to include Celsius for Temperature.
     */
    @DynamicSerializeElement
    @XmlElement(name = "DoCelsius")
    private boolean doCelsius;

    /**
     * Start/end dates for cooling degree days.
     */
    @DynamicSerializeElement
    @XmlElement(name = "CoolDates")
    private ClimateDates coolDates;

    /**
     * Start/end dates for heating degree days.
     */
    @DynamicSerializeElement
    @XmlElement(name = "HeatDates")
    private ClimateDates heatDates;

    /**
     * Start/end dates for snow days.
     */
    @DynamicSerializeElement
    @XmlElement(name = "SnowDates")
    private ClimateDates snowDates;

    /**
     * Control flags for Temperature elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Temperature")
    private TemperatureControlFlags tempControl;

    /**
     * Control flags for Precipitation elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Precipitation")
    private PrecipitationControlFlags precipControl;

    /**
     * Control flags for Snow elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Snow")
    private SnowControlFlags snowControl;

    /**
     * Control flags for Degree Days elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "DegreeDays")
    private DegreeDaysControlFlags degreeDaysControl;

    /**
     * Control flags for Relative Humidity elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "RelativeHumidity")
    private RelativeHumidityControlFlags relHumidityControl;

    /**
     * Control flags for Wind elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Wind")
    private WindControlFlags windControl;

    /**
     * Control flags for Skycover elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Skycover")
    private SkycoverControlFlags skycoverControl;

    /**
     * Control flags for Weather elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Weather")
    private WeatherControlFlags weatherControl;

    /**
     * Control flags for Sunrise/Sunset elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "SunriseSunset")
    private SunriseNsetControlFlags sunControl;

    /**
     * Control flags for TemperatureRecord elements.
     */
    @DynamicSerializeElement
    @XmlElement(name = "TemperatureRecord")
    private TempRecordControlFlags tempRecordControl;

    /**
     * Empty constructor.
     */
    public ClimateProductControl() {
    }

    /**
     * @return the doCelsius
     */
    public boolean isDoCelsius() {
        return doCelsius;
    }

    /**
     * @param doCelsius
     *            the doCelsius to set
     */
    public void setDoCelsius(boolean doCelsius) {
        this.doCelsius = doCelsius;
    }

    /**
     * @return the coolDates
     */
    public ClimateDates getCoolDates() {
        return coolDates;
    }

    /**
     * @param coolDates
     *            the coolDates to set
     */
    public void setCoolDates(ClimateDates coolDates) {
        this.coolDates = coolDates;
    }

    /**
     * @return the heatDates
     */
    public ClimateDates getHeatDates() {
        return heatDates;
    }

    /**
     * @param heatDates
     *            the heatDates to set
     */
    public void setHeatDates(ClimateDates heatDates) {
        this.heatDates = heatDates;
    }

    /**
     * @return the snowDates
     */
    public ClimateDates getSnowDates() {
        return snowDates;
    }

    /**
     * @param snowDates
     *            the snowDates to set
     */
    public void setSnowDates(ClimateDates snowDates) {
        this.snowDates = snowDates;
    }

    /**
     * @return the tempControl
     */
    public TemperatureControlFlags getTempControl() {
        return tempControl;
    }

    /**
     * @param tempControl
     *            the tempControl to set
     */
    public void setTempControl(TemperatureControlFlags tempControl) {
        this.tempControl = tempControl;
    }

    /**
     * @return the precipControl
     */
    public PrecipitationControlFlags getPrecipControl() {
        return precipControl;
    }

    /**
     * @param precipControl
     *            the precipControl to set
     */
    public void setPrecipControl(PrecipitationControlFlags precipControl) {
        this.precipControl = precipControl;
    }

    /**
     * @return the snowControl
     */
    public SnowControlFlags getSnowControl() {
        return snowControl;
    }

    /**
     * @param snowControl
     *            the snowControl to set
     */
    public void setSnowControl(SnowControlFlags snowControl) {
        this.snowControl = snowControl;
    }

    /**
     * @return the degreeDaysControl
     */
    public DegreeDaysControlFlags getDegreeDaysControl() {
        return degreeDaysControl;
    }

    /**
     * @param degreeDaysControl
     *            the degreeDaysControl to set
     */
    public void setDegreeDaysControl(DegreeDaysControlFlags degreeDaysControl) {
        this.degreeDaysControl = degreeDaysControl;
    }

    /**
     * @return the relHumidityControl
     */
    public RelativeHumidityControlFlags getRelHumidityControl() {
        return relHumidityControl;
    }

    /**
     * @param relHumidityControl
     *            the relHumidityControl to set
     */
    public void setRelHumidityControl(
            RelativeHumidityControlFlags relHumidityControl) {
        this.relHumidityControl = relHumidityControl;
    }

    /**
     * @return the windControl
     */
    public WindControlFlags getWindControl() {
        return windControl;
    }

    /**
     * @param windControl
     *            the windControl to set
     */
    public void setWindControl(WindControlFlags windControl) {
        this.windControl = windControl;
    }

    /**
     * @return the skycoverControl
     */
    public SkycoverControlFlags getSkycoverControl() {
        return skycoverControl;
    }

    /**
     * @param skycoverControl
     *            the skycoverControl to set
     */
    public void setSkycoverControl(SkycoverControlFlags skycoverControl) {
        this.skycoverControl = skycoverControl;
    }

    /**
     * @return the weatherControl
     */
    public WeatherControlFlags getWeatherControl() {
        return weatherControl;
    }

    /**
     * @param weatherControl
     *            the weatherControl to set
     */
    public void setWeatherControl(WeatherControlFlags weatherControl) {
        this.weatherControl = weatherControl;
    }

    /**
     * @return the sunControl
     */
    public SunriseNsetControlFlags getSunControl() {
        return sunControl;
    }

    /**
     * @param sunControl
     *            the sunControl to set
     */
    public void setSunControl(SunriseNsetControlFlags sunControl) {
        this.sunControl = sunControl;
    }

    /**
     * @return the tempRecordControl
     */
    public TempRecordControlFlags getTempRecordControl() {
        return tempRecordControl;
    }

    /**
     * @param tempRecordControl
     *            the tempRecordControl to set
     */
    public void setTempRecordControl(TempRecordControlFlags tempRecordControl) {
        this.tempRecordControl = tempRecordControl;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.doCelsius = false;
        this.heatDates = new ClimateDates(new ClimateDate(1, 7, 0),
                new ClimateDate(30, 6, 0));
        this.coolDates = new ClimateDates(new ClimateDate(1, 1, 0),
                new ClimateDate(30, 12, 0));
        this.snowDates = new ClimateDates(new ClimateDate(1, 7, 0),
                new ClimateDate(30, 6, 0));

        this.tempControl = TemperatureControlFlags.getDefaultFlags();
        this.precipControl = PrecipitationControlFlags.getDefaultFlags();
        this.snowControl = SnowControlFlags.getDefaultFlags();
        this.degreeDaysControl = DegreeDaysControlFlags.getDefaultFlags();
        this.relHumidityControl = RelativeHumidityControlFlags
                .getDefaultFlags();
        this.windControl = WindControlFlags.getDefaultFlags();
        this.skycoverControl = SkycoverControlFlags.getDefaultFlags();
        this.weatherControl = WeatherControlFlags.getDefaultFlags();
        this.sunControl = SunriseNsetControlFlags.getDefaultFlags();
        this.tempRecordControl = TempRecordControlFlags.getDefaultFlags();
    }

    /**
     * @return a deep copy of this object.
     */
    public ClimateProductControl copy() {

        ClimateProductControl control = new ClimateProductControl();

        control.doCelsius = this.doCelsius;
        control.coolDates = new ClimateDates(this.coolDates.getStart(),
                this.coolDates.getEnd());
        control.heatDates = new ClimateDates(this.heatDates.getStart(),
                this.heatDates.getEnd());
        control.snowDates = new ClimateDates(this.snowDates.getStart(),
                this.snowDates.getEnd());

        control.tempControl = this.tempControl.copy();
        control.precipControl = this.precipControl.copy();
        control.snowControl = this.snowControl.copy();
        control.degreeDaysControl = this.degreeDaysControl.copy();
        control.relHumidityControl = this.relHumidityControl.copy();
        control.windControl = this.windControl.copy();
        control.skycoverControl = this.skycoverControl.copy();
        control.weatherControl = this.weatherControl.copy();
        control.sunControl = this.sunControl.copy();
        control.tempRecordControl = this.tempRecordControl.copy();

        return control;
    }

    /**
     * @return a new object filled with default values.
     */
    public static ClimateProductControl getDefaultControl() {
        ClimateProductControl control = new ClimateProductControl();
        control.setDataToDefault();
        return control;
    }

}
