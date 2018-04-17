/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;

/**
 * This enum holds the season report type (DJF, MAM, JJA, SON).
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 22 JUL 2016  20414      amoore      Initial creation
 * 31 AUG 2016  20414      amoore      Winter clarification.
 * 07 OCT 2016  20636      wpaintsil   Get SeasonType from month
 * 19 OCT 2016  20636      wpaintsil   Add SEASON_END_MONTH constant
 * 15 MAY 2017  33104      amoore      Move SEASON_END_MONTH.
 * </pre>
 * 
 * @author amoore
 */
@DynamicSerialize
public enum SeasonType {

    /**
     * December, January, February (Winter). The year of a Winter is the year of
     * January and February. For instance, Winter 2017 is from December 2016 -
     * February 2017.
     */
    DJF_WINTER("DJF", 0),

    /**
     * March, April, May (Spring).
     */
    MAM_SPRING("MAM", 1),

    /**
     * June, July, August (Summer).
     */
    JJA_SUMMER("JJA", 2),

    /**
     * September, October, November (Fall)
     */
    SON_FALL("SON", 3);

    @DynamicSerializeElement
    private String value;

    private int index;

    /**
     * @param iValue
     */
    private SeasonType(final String iValue, int iIndex) {
        this.value = iValue;
        this.index = iIndex;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * @param iValue
     *            value.
     */
    public void setValue(String iValue) {
        value = iValue;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Get a month's(1-12) season.
     * 
     * @param iMonth
     * @return SeasonType
     * @throws ClimateInvalidParameterException
     */
    public static SeasonType getSeasonTypeFromMonth(int iMonth)
            throws ClimateInvalidParameterException {
        int month = iMonth - 1; // Calendar type months start at 0
        switch (month) {
        case Calendar.DECEMBER:
        case Calendar.JANUARY:
        case Calendar.FEBRUARY:
            return DJF_WINTER;
        case Calendar.MARCH:
        case Calendar.APRIL:
        case Calendar.MAY:
            return MAM_SPRING;
        case Calendar.JUNE:
        case Calendar.JULY:
        case Calendar.AUGUST:
            return JJA_SUMMER;
        case Calendar.SEPTEMBER:
        case Calendar.OCTOBER:
        case Calendar.NOVEMBER:
            return SON_FALL;
        default:
            throw new ClimateInvalidParameterException(
                    "Invalid Month: [" + iMonth + "] is not in range [1-12].");

        }
    }
}
