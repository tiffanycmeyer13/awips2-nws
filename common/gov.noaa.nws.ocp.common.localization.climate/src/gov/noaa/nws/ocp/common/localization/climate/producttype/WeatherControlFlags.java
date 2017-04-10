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

/**
 *
 * Class containing the weather cover flags. In legacy, it was part of
 * "TYPE_sky_control.h".
 *
 * <pre>
 *
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "Weather")
@XmlAccessorType(XmlAccessType.NONE)
public class WeatherControlFlags {

    /**
     * Flag for including weather in daily report
     */
    @DynamicSerializeElement
    @XmlElement(name = "Weather")
    private boolean weather;

    /**
     * Flag for number of days with thunderstorms
     */
    @DynamicSerializeElement
    @XmlElement(name = "Thunderstorm")
    private boolean thunderStorm;

    /**
     * Flag for number of days with mixed precip (RASN)
     */
    @DynamicSerializeElement
    @XmlElement(name = "MixedPrecip")
    private boolean mixedPrecip;

    /**
     * Flag for number of days with heavy rain (R+)
     */
    @DynamicSerializeElement
    @XmlElement(name = "HeavyRain")
    private boolean heavyRain;

    /**
     * Flag for number of days with rain (R)
     */
    @DynamicSerializeElement
    @XmlElement(name = "Rain")
    private boolean rain;

    /**
     * Flag for number of days with light rain (R-)
     */
    @DynamicSerializeElement
    @XmlElement(name = "LightRain")
    private boolean lightRain;

    /**
     * Flag for number of days with freezing rain (ZR)
     */
    @DynamicSerializeElement
    @XmlElement(name = "FreezingRain")
    private boolean freezingRain;

    /**
     * Flags for number of days with light freezing rain (ZR-)
     */
    @DynamicSerializeElement
    @XmlElement(name = "LightFreezingRain")
    private boolean lightFreezingRain;

    /**
     * Flags for number of days with hail (A)
     */
    @DynamicSerializeElement
    @XmlElement(name = "Hail")
    private boolean hail;

    /**
     * Flags for number of days with heavy snow (S+)
     */
    @DynamicSerializeElement
    @XmlElement(name = "HeavySnow")
    private boolean heavySnow;

    /**
     * flags for number of days with snow (S)
     */
    @DynamicSerializeElement
    @XmlElement(name = "Snow")
    private boolean snow;

    /**
     * Flags for number of days with light snow (S-)
     */
    @DynamicSerializeElement
    @XmlElement(name = "LightSnow")
    private boolean lightSnow;

    /**
     * flags for number of days with ice pellets (IP)
     */
    @DynamicSerializeElement
    @XmlElement(name = "IcePellet")
    private boolean icePellet;

    /**
     * flags for number of days with fog (F)
     */
    @DynamicSerializeElement
    @XmlElement(name = "Fog")
    private boolean fog;

    /**
     * Flags for number of days with heavy fog (vis<1/4 mi)
     */
    @DynamicSerializeElement
    @XmlElement(name = "HeavyFog")
    private boolean heavyFog;

    /**
     * Flags for number of days with haze (H)
     */
    @DynamicSerializeElement
    @XmlElement(name = "Haze")
    private boolean haze;

    /**
     * Default constructor
     */
    public WeatherControlFlags() {
    }

    /**
     * @return the weather
     */
    public boolean isWeather() {
        return weather;
    }

    /**
     * @param weather
     *            the weather to set
     */
    public void setWeather(boolean weather) {
        this.weather = weather;
    }

    /**
     * @return the thunderStorm
     */
    public boolean isThunderStorm() {
        return thunderStorm;
    }

    /**
     * @param thunderStorm
     *            the thunderStorm to set
     */
    public void setThunderStorm(boolean thunderStorm) {
        this.thunderStorm = thunderStorm;
    }

    /**
     * @return the mixedPrecip
     */
    public boolean isMixedPrecip() {
        return mixedPrecip;
    }

    /**
     * @param mixedPrecip
     *            the mixedPrecip to set
     */
    public void setMixedPrecip(boolean mixedPrecip) {
        this.mixedPrecip = mixedPrecip;
    }

    /**
     * @return the heavyRain
     */
    public boolean isHeavyRain() {
        return heavyRain;
    }

    /**
     * @param heavyRain
     *            the heavyRain to set
     */
    public void setHeavyRain(boolean heavyRain) {
        this.heavyRain = heavyRain;
    }

    /**
     * @return the rain
     */
    public boolean isRain() {
        return rain;
    }

    /**
     * @param rain
     *            the rain to set
     */
    public void setRain(boolean rain) {
        this.rain = rain;
    }

    /**
     * @return the lightRain
     */
    public boolean isLightRain() {
        return lightRain;
    }

    /**
     * @param lightRain
     *            the lightRain to set
     */
    public void setLightRain(boolean lightRain) {
        this.lightRain = lightRain;
    }

    /**
     * @return the freezingRain
     */
    public boolean isFreezingRain() {
        return freezingRain;
    }

    /**
     * @param freezingRain
     *            the freezingRain to set
     */
    public void setFreezingRain(boolean freezingRain) {
        this.freezingRain = freezingRain;
    }

    /**
     * @return the lightFreezingRain
     */
    public boolean isLightFreezingRain() {
        return lightFreezingRain;
    }

    /**
     * @param lightFreezingRain
     *            the lightFreezingRain to set
     */
    public void setLightFreezingRain(boolean lightFreezingRain) {
        this.lightFreezingRain = lightFreezingRain;
    }

    /**
     * @return the hail
     */
    public boolean isHail() {
        return hail;
    }

    /**
     * @param hail
     *            the hail to set
     */
    public void setHail(boolean hail) {
        this.hail = hail;
    }

    /**
     * @return the heavySnow
     */
    public boolean isHeavySnow() {
        return heavySnow;
    }

    /**
     * @param heavySnow
     *            the heavySnow to set
     */
    public void setHeavySnow(boolean heavySnow) {
        this.heavySnow = heavySnow;
    }

    /**
     * @return the snow
     */
    public boolean isSnow() {
        return snow;
    }

    /**
     * @param snow
     *            the snow to set
     */
    public void setSnow(boolean snow) {
        this.snow = snow;
    }

    /**
     * @return the lightSnow
     */
    public boolean isLightSnow() {
        return lightSnow;
    }

    /**
     * @param lightSnow
     *            the lightSnow to set
     */
    public void setLightSnow(boolean lightSnow) {
        this.lightSnow = lightSnow;
    }

    /**
     * @return the icePellet
     */
    public boolean isIcePellet() {
        return icePellet;
    }

    /**
     * @param icePellet
     *            the icePellet to set
     */
    public void setIcePellet(boolean icePellet) {
        this.icePellet = icePellet;
    }

    /**
     * @return the fog
     */
    public boolean isFog() {
        return fog;
    }

    /**
     * @param fog
     *            the fog to set
     */
    public void setFog(boolean fog) {
        this.fog = fog;
    }

    /**
     * @return the heavyFog
     */
    public boolean isHeavyFog() {
        return heavyFog;
    }

    /**
     * @param heavyFog
     *            the heavyFog to set
     */
    public void setHeavyFog(boolean heavyFog) {
        this.heavyFog = heavyFog;
    }

    /**
     * @return the haze
     */
    public boolean isHaze() {
        return haze;
    }

    /**
     * @param haze
     *            the haze to set
     */
    public void setHaze(boolean haze) {
        this.haze = haze;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {
        this.weather = false;
        this.thunderStorm = false;
        this.mixedPrecip = false;
        this.heavyRain = false;
        this.rain = false;
        this.lightRain = false;
        this.freezingRain = false;
        this.lightFreezingRain = false;
        this.hail = false;
        this.heavySnow = false;
        this.snow = false;
        this.lightSnow = false;
        this.icePellet = false;
        this.fog = false;
        this.heavyFog = false;
        this.haze = false;
    }

    /**
     * @return a deep copy of this object.
     */
    public WeatherControlFlags copy() {

        WeatherControlFlags flags = new WeatherControlFlags();

        flags.weather = this.weather;
        flags.thunderStorm = this.thunderStorm;
        flags.mixedPrecip = this.mixedPrecip;
        flags.heavyRain = this.heavyRain;
        flags.rain = this.rain;
        flags.lightRain = this.lightRain;
        flags.freezingRain = this.freezingRain;
        flags.lightFreezingRain = this.lightFreezingRain;
        flags.hail = this.hail;
        flags.heavySnow = this.heavySnow;
        flags.snow = this.snow;
        flags.lightSnow = this.lightSnow;
        flags.icePellet = this.icePellet;
        flags.fog = this.fog;
        flags.heavyFog = this.heavyFog;
        flags.haze = this.haze;

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static WeatherControlFlags getDefaultFlags() {
        WeatherControlFlags flags = new WeatherControlFlags();
        flags.setDataToDefault();
        return flags;
    }

}
