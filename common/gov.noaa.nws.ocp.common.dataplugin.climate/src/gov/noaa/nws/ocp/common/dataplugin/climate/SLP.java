/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Sea level pressure data. Originates from the use of the max_slp/min_slp
 * fields in the daily_climate_data
 * structure(rehost-adapt/adapt/climate/include/TYPE_daily_climate_data.h)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 10, 2016            xzhang      Initial creation
 * 22 FEB 2017  28609      amoore      Address TODOs. Fix comments.
 * 12 APR 2017  30171      amoore      Create missing data getter.
 *                                     Use camel casing.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class SLP {
    /**
     * Day of max SLP.
     */
    @DynamicSerializeElement
    private int dayMaxSLP = ParameterFormatClimate.MISSING_DATE;

    /**
     * Day of min SLP.
     */
    @DynamicSerializeElement
    private int dayMinSLP = ParameterFormatClimate.MISSING_DATE;

    /**
     * Max SLP.
     */
    @DynamicSerializeElement
    private double maxSLP = ParameterFormatClimate.MISSING_SLP;

    /**
     * Min SLP.
     */
    @DynamicSerializeElement
    private double minSLP = ParameterFormatClimate.MISSING_SLP;

    /**
     * Empty constructor.
     */
    public SLP() {
    }

    /**
     * @return day of max SLP.
     */
    public int getDayMaxSLP() {
        return dayMaxSLP;
    }

    /**
     * @param daymaxslp
     *            day of max SLP.
     */
    public void setDayMaxSLP(int daymaxslp) {
        this.dayMaxSLP = daymaxslp;
    }

    /**
     * @return day of min SLP.
     */
    public int getDayMinSLP() {
        return dayMinSLP;
    }

    /**
     * @param dayminslp
     *            day of min SLP.
     */
    public void setDayMinSLP(int dayminslp) {
        this.dayMinSLP = dayminslp;
    }

    /**
     * @return max SLP.
     */
    public double getMaxSLP() {
        return maxSLP;
    }

    /**
     * @param maxslp
     *            max SLP.
     */
    public void setMaxSLP(double maxslp) {
        this.maxSLP = maxslp;
    }

    /**
     * @return min SLP.
     */
    public double getMinSLP() {
        return minSLP;
    }

    /**
     * @param minslp
     *            min SLP.
     */
    public void setMinSLP(double minslp) {
        this.minSLP = minslp;
    }

    /**
     * @return {@link SLP} instance will all missing data.
     */
    public static SLP getMissingSLP() {
        SLP slp = new SLP();
        slp.setDayMaxSLP(ParameterFormatClimate.MISSING_DATE);
        slp.setDayMinSLP(ParameterFormatClimate.MISSING_DATE);
        slp.setMaxSLP(ParameterFormatClimate.MISSING_SLP);
        slp.setMinSLP(ParameterFormatClimate.MISSING_SLP);

        return slp;
    }
}
