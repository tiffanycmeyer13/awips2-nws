/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

/**
 * Converted from rehost-adapt/adapt/climate/include/TYPE_climate_wind.h
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2015            xzhang      Initial creation
 * 17 MAY 2016  18384      amoore      Logic cleanup.
 * 18 MAY 2016  18384      amoore      Serialization.
 * 07 JUL 2016  16962      amoore      Fix serialization
 * 02 NOV 2016  21378      amoore      Added copy constructor.
 * 10 NOV 2016  20636      wpaintsil   Add isMissing().
 * 03 JAN 2016  22134      amoore      Added equals().
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateWind {

    /**
     * wind direction, should be 10's of degrees
     */
    @DynamicSerializeElement
    private int dir;

    /**
     * wind speed, ususally knots
     */
    @DynamicSerializeElement
    private float speed;

    /**
     * Empty constructor.
     */
    public ClimateWind() {
    }

    /**
     * Copy constructor.
     * 
     * @param wind
     */
    public ClimateWind(ClimateWind wind) {
        this(wind.getDir(), wind.getSpeed());
    }

    /**
     * Constructor.
     * 
     * @param idir
     *            direction of wind.
     * @param ispeed
     *            speed of wind.
     */
    public ClimateWind(int idir, float ispeed) {
        this.dir = idir;
        this.speed = ispeed;
    }

    /**
     * @return the dir
     */
    public int getDir() {
        return dir;
    }

    /**
     * @return the speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @param idir
     *            the dir to set
     */
    public void setDir(int idir) {
        this.dir = idir;
    }

    /**
     * @param ispeed
     *            the speed to set
     */
    public void setSpeed(float ispeed) {
        this.speed = ispeed;
    }

    /**
     * Set both fields to missing
     */
    public void setDataToMissing() {
        this.dir = ParameterFormatClimate.MISSING;
        this.speed = ParameterFormatClimate.MISSING_SPEED;
    }

    /**
     * @return true if all fields are missing, false otherwise.
     */
    public boolean isMissing() {
        return (dir == ParameterFormatClimate.MISSING
                && speed == ParameterFormatClimate.MISSING_SPEED);
    }

    /**
     * @return new {@link ClimateWind} instance with both fields set to missing.
     */
    public static ClimateWind getMissingClimateWind() {
        ClimateWind wind = new ClimateWind();
        wind.setDataToMissing();
        return wind;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ClimateWind) {
            ClimateWind otherWind = (ClimateWind) other;
            if (dir == otherWind.getDir() && ClimateUtilities
                    .floatingEquals(speed, otherWind.getSpeed())) {
                return true;
            }
        }
        return false;
    }
}
