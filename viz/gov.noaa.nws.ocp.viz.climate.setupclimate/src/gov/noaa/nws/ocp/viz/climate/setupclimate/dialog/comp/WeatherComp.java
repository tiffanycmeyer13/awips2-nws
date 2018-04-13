/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductControl;
import gov.noaa.nws.ocp.common.localization.climate.producttype.WeatherControlFlags;

/**
 * Composite to contain all sub-categories for climate weather in setup GUI.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Dec 14, 2016 20640      jwu          Initial creation
 * 
 * </pre>
 * 
 * @author jwu
 */
public class WeatherComp extends AbstractCategoryComp {

    // Layouts
    private RowLayout mainCompLayout;

    private RowLayout majorCompLayout;

    // Composite for daily weather
    private Composite dailyWeatherComp;

    // Composite for period weather
    private Composite periodWeatherComp;

    // Buttons for including weather in daily report
    private Button weatherBtn;

    // Label for period weather
    private Label weatherLbl;

    // Buttons for number of days with thunderstorms
    private Button thunderStormBtn;

    // Buttons for number of days with mixed precip (RASN)
    private Button mixedPrecipBtn;

    // Buttons for number of days with heavy rain (R+)
    private Button heavyRainBtn;

    // Buttons for number of days with with rain (R)
    private Button rainBtn;

    // Buttons for number of days with light rain (R-)
    private Button lightRainBtn;

    // Buttons for number of days with freezing rain (ZR)
    private Button freezingRainBtn;

    // Buttons for number of days with light freezing rain (ZR-)
    private Button lightFreezingRainBtn;

    // Buttons for number of days with hail (A)
    private Button hailBtn;

    // Buttons for number of days with heavy snow (S+)
    private Button heavySnowBtn;

    // Buttons for number of days with snow (S)
    private Button snowBtn;

    // Buttons for number of days with light snow (S-)
    private Button lightSnowBtn;

    // Buttons for number of days with ice pellets (IP)
    private Button icePelletBtn;

    // Buttons for number of days with fog (F)
    private Button fogBtn;

    // Buttons for number of days with heavy fog (vis<1/4 mi)
    private Button heavyFogBtn;

    // Buttons for number of days with haze (H)
    private Button hazeBtn;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent composite
     * @param style
     *            composite style
     * @param customThresholds
     *            User-defined threshold values
     * @param majorFont
     *            Font to be used for major check buttons
     */
    public WeatherComp(Composite parent, int style,
            ClimateGlobal preferenceValues, Font majorFont) {

        super(parent, style, preferenceValues, majorFont);
    }

    /**
     * Create/initialize components.
     */
    protected void initializeComponents() {
        /*
         * Layout
         */
        RowLayout subcatCompLayout = new RowLayout(SWT.VERTICAL);
        subcatCompLayout.marginTop = 1;
        subcatCompLayout.spacing = 5;
        // subcatCompLayout.justify = true;

        this.setLayout(subcatCompLayout);

        mainCompLayout = new RowLayout(SWT.HORIZONTAL);
        majorCompLayout = new RowLayout(SWT.VERTICAL);

        // Layouts for components.
        mainCompLayout.spacing = 10;
        mainCompLayout.marginWidth = 8;

        majorCompLayout.spacing = 5;
        majorCompLayout.marginLeft = 35;

        // Composite to hold weather button for daily product
        dailyWeatherComp = new Composite(this, SWT.NONE);
        RowLayout singleWeatherLayout = new RowLayout(SWT.VERTICAL);
        singleWeatherLayout.marginTop = 15;
        singleWeatherLayout.marginLeft = 180;
        dailyWeatherComp.setLayout(singleWeatherLayout);

        weatherBtn = new Button(dailyWeatherComp, SWT.CHECK);
        weatherBtn.setText("Weather");
        weatherBtn.setFont(majorFont);
        weatherBtn.setSelection(true);

        // Composite to list weather phenom for period type.
        periodWeatherComp = createWeatherComp(this);
        periodWeatherComp.setVisible(false);

    }

    /**
     * Create a composite to hold all weather phenoms.
     * 
     * @param parent
     *            parent composite
     * @return composite
     */
    private Composite createWeatherComp(Composite parent) {

        RowLayout weatherMainCompLayout = new RowLayout(SWT.VERTICAL);
        weatherMainCompLayout.marginTop = 0;
        weatherMainCompLayout.marginLeft = 0;
        weatherMainCompLayout.spacing = 10;
        weatherMainCompLayout.fill = true;

        Composite weatherMainComp = new Composite(parent, SWT.NONE);
        weatherMainComp.setLayout(weatherMainCompLayout);

        weatherLbl = new Label(weatherMainComp, SWT.None);
        weatherLbl.setText("Number of Days with...");
        weatherLbl.setFont(majorFont);
        weatherLbl.setAlignment(SWT.CENTER);

        RowLayout weatherCompLayout = new RowLayout(SWT.HORIZONTAL);
        weatherCompLayout.marginWidth = 8;
        weatherCompLayout.spacing = 60;

        Composite weatherComp = new Composite(weatherMainComp, SWT.NONE);
        weatherComp.setLayout(weatherCompLayout);

        // Left composite.
        Composite leftWeatherComp = new Composite(weatherComp, SWT.NONE);

        leftWeatherComp.setLayout(majorCompLayout);

        thunderStormBtn = new Button(leftWeatherComp, SWT.CHECK);
        thunderStormBtn.setText("Thunder");

        mixedPrecipBtn = new Button(leftWeatherComp, SWT.CHECK);
        mixedPrecipBtn.setText("Mixed Precip");

        heavyRainBtn = new Button(leftWeatherComp, SWT.CHECK);
        heavyRainBtn.setText("Heavy Rain");

        rainBtn = new Button(leftWeatherComp, SWT.CHECK);
        rainBtn.setText("Rain");

        lightRainBtn = new Button(leftWeatherComp, SWT.CHECK);
        lightRainBtn.setText("Light Rain");

        freezingRainBtn = new Button(leftWeatherComp, SWT.CHECK);
        freezingRainBtn.setText("Freezing Rain");

        lightFreezingRainBtn = new Button(leftWeatherComp, SWT.CHECK);
        lightFreezingRainBtn.setText("Light Freezing Rain");

        hailBtn = new Button(leftWeatherComp, SWT.CHECK);
        hailBtn.setText("Hail");

        // Right composite.
        Composite rightWeatherComp = new Composite(weatherComp, SWT.NONE);
        rightWeatherComp.setLayout(majorCompLayout);

        heavySnowBtn = new Button(rightWeatherComp, SWT.CHECK);
        heavySnowBtn.setText("Heavy Snow");

        snowBtn = new Button(rightWeatherComp, SWT.CHECK);
        snowBtn.setText("Snow");

        lightSnowBtn = new Button(rightWeatherComp, SWT.CHECK);
        lightSnowBtn.setText("Light Snow");

        icePelletBtn = new Button(rightWeatherComp, SWT.CHECK);
        icePelletBtn.setText("Sleet (Ice Pellets)");

        fogBtn = new Button(rightWeatherComp, SWT.CHECK);
        fogBtn.setText("Fog");

        heavyFogBtn = new Button(rightWeatherComp, SWT.CHECK);
        heavyFogBtn.setText("Fog (vis <= 1/4 mi)");

        hazeBtn = new Button(rightWeatherComp, SWT.CHECK);
        hazeBtn.setText("Haze");

        return weatherMainComp;
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenNWRnNWWS() {
        if (isNWR) {
            deselectAllButtons();
            disableAllButtons();
        } else {
            enableAllButtons();
        }

        weatherLbl.setEnabled(!isNWR);
    }

    /**
     * Do GUI adjustments when switching between daily/period type.
     */
    protected void switchBetweenDailyPeriod() {
        dailyWeatherComp.setVisible(isDaily);
        periodWeatherComp.setVisible(!isDaily);
        weatherLbl.setVisible(!isDaily);
    }

    /**
     * Retrieve flags set on the GUI.
     * 
     * @return getControlFlags() - WeatherControlFlags
     */
    public WeatherControlFlags getControlFlags() {
        WeatherControlFlags flags = WeatherControlFlags.getDefaultFlags();

        if (!isNWR) {
            if (isDaily) {
                flags.setWeather(weatherBtn.getSelection());
            } else {
                flags.setThunderStorm(thunderStormBtn.getSelection());
                flags.setMixedPrecip(mixedPrecipBtn.getSelection());
                flags.setHeavyRain(heavyRainBtn.getSelection());
                flags.setRain(rainBtn.getSelection());
                flags.setLightRain(lightRainBtn.getSelection());
                flags.setFreezingRain(freezingRainBtn.getSelection());
                flags.setLightFreezingRain(lightFreezingRainBtn.getSelection());
                flags.setHail(hailBtn.getSelection());
                flags.setHeavySnow(heavySnowBtn.getSelection());
                flags.setSnow(snowBtn.getSelection());
                flags.setLightSnow(lightSnowBtn.getSelection());
                flags.setIcePellet(icePelletBtn.getSelection());
                flags.setFog(fogBtn.getSelection());
                flags.setHeavyFog(heavyFogBtn.getSelection());
                flags.setHaze(hazeBtn.getSelection());
            }
        }

        return flags;
    }

    /**
     * Set flags on the this composite based on a given ClimateProductControl.
     * 
     * @param isNWR
     *            boolean - if source is NWR or NWWS
     * @param isDaily
     *            boolean - if the period type is daily or period.
     * @param flags
     *            ClimateProductControl
     */
    public void setControlFlags(boolean isNWR, boolean isDaily,
            ClimateProductControl ctrlFlg) {

        this.isNWR = isNWR;
        this.isDaily = isDaily;

        WeatherControlFlags flags = ctrlFlg.getWeatherControl();

        if (!isNWR) {
            if (isDaily) {
                weatherBtn.setSelection(flags.isWeather());
            } else {
                thunderStormBtn.setSelection(flags.isThunderStorm());
                mixedPrecipBtn.setSelection(flags.isMixedPrecip());
                heavyRainBtn.setSelection(flags.isHeavyRain());
                rainBtn.setSelection(flags.isRain());
                lightRainBtn.setSelection(flags.isLightRain());
                freezingRainBtn.setSelection(flags.isFreezingRain());
                lightFreezingRainBtn.setSelection(flags.isLightFreezingRain());
                hailBtn.setSelection(flags.isHail());
                heavySnowBtn.setSelection(flags.isHeavySnow());
                snowBtn.setSelection(flags.isSnow());
                lightSnowBtn.setSelection(flags.isLightSnow());
                icePelletBtn.setSelection(flags.isIcePellet());
                fogBtn.setSelection(flags.isFog());
                heavyFogBtn.setSelection(flags.isHeavyFog());
                hazeBtn.setSelection(flags.isHaze());
            }
        }
    }

}