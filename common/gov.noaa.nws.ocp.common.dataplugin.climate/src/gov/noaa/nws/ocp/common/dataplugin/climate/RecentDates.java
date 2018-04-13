package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Object containing most recent dates for a particular station. Adapted from
 * the use of daily_date, month_date, season_date, annual_date parameters in
 * adapt/climate/lib/src/qc_climate/find_date.ec
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date           Ticket#  Engineer    Description
 * -----------    -------- ----------- --------------------------
 * Oct 17, 2016   20636  wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class RecentDates {
    /**
     * Station id
     */
    @DynamicSerializeElement
    private int informId;

    /**
     * Date for most recent daily data.
     */
    @DynamicSerializeElement
    private ClimateDate dailyDate;

    /**
     * End date for most recent monthly data.
     */
    @DynamicSerializeElement
    private ClimateDate monthDate;

    /**
     * End date for most recent seasonal data.
     */
    @DynamicSerializeElement
    private ClimateDate seasonDate;

    /**
     * End date for most recent annual data.
     */
    @DynamicSerializeElement
    private ClimateDate annualDate;

    /**
     * @return informId
     */
    public int getInformId() {
        return informId;
    }

    /**
     * set informId
     * 
     * @param iInformId
     */
    public void setInformId(int iInformId) {
        informId = iInformId;
    }

    /**
     * @return dailyDate
     */
    public ClimateDate getDailyDate() {
        return dailyDate;
    }

    /**
     * @param iDailyDate
     *            the most recent daily date to set
     */
    public void setDailyDate(ClimateDate iDailyDate) {
        dailyDate = iDailyDate;
    }

    /**
     * @return monthDate
     */
    public ClimateDate getMonthDate() {
        return monthDate;
    }

    /**
     * @param iMonthDate
     *            the most recent month end date to set
     */
    public void setMonthDate(ClimateDate iMonthDate) {
        monthDate = iMonthDate;
    }

    /**
     * @return seasonDate
     */
    public ClimateDate getSeasonDate() {
        return seasonDate;
    }

    /**
     * @param iSeasonDate
     *            the most recent season end date to set
     */
    public void setSeasonDate(ClimateDate iSeasonDate) {
        seasonDate = iSeasonDate;
    }

    /**
     * @return annualDate
     */
    public ClimateDate getAnnualDate() {
        return annualDate;
    }

    /**
     * @param iAnnualDate
     *            the most recent year end date to set
     */
    public void setAnnualDate(ClimateDate iAnnualDate) {
        annualDate = iAnnualDate;
    }

    public void setMissing() {
        dailyDate = ClimateDate.getMissingClimateDate();
        monthDate = ClimateDate.getMissingClimateDate();
        seasonDate = ClimateDate.getMissingClimateDate();
        annualDate = ClimateDate.getMissingClimateDate();
    }

    public static RecentDates getMissingRecentDates() {
        RecentDates recentDates = new RecentDates();
        recentDates.setMissing();
        return recentDates;
    }
}
