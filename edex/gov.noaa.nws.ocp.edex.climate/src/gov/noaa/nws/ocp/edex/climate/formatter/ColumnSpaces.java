/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;

/**
 * Migrated from TYPE_column_stops.l, set_tabs.f and TYPE_tab_sets.h.
 * 
 * Defines tabs and spacing for NWWS product.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2017 21099      wpaintsil   Initial creation
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class ColumnSpaces {

    private int posActual;

    private int posTime;

    private int posRecord;

    private int posYear;

    private int posNorm;

    private int posDepart;

    private int posLast;

    private int widthActual;

    private int widthTime;

    private int widthRecord;

    private int widthYear;

    private int widthNorm;

    private int widthDepart;

    private int widthLast;

    private boolean time;

    private boolean record;

    private boolean year;

    private boolean norm;

    private boolean depart;

    private boolean last;

    private int posValue;

    private int posActDate;

    private int posLastYr;

    private int posLastDate;

    private static final int DEFAULT_PERIOD_VALUE = 14;

    private static final int DEFAULT_PERIOD_ACT_DATE = 24;

    private static final int DEFAULT_PERIOD_NORM = 32;

    private static final int DEFAULT_PERIOD_DEPART = 40;

    private static final int DEFAULT_PERIOD_LAST_YR = 49;

    private static final int DEFAULT_PERIOD_LAST_DATE = 58;

    private static final int DEFAULT_NONE = 0;

    public int getPosActual() {
        return posActual;
    }

    public void setPosActual(int posActual) {
        this.posActual = posActual;
    }

    public int getPosTime() {
        return posTime;
    }

    public void setPosTime(int posTime) {
        this.posTime = posTime;
    }

    public int getPosRecord() {
        return posRecord;
    }

    public void setPosRecord(int posRecord) {
        this.posRecord = posRecord;
    }

    public int getPosYear() {
        return posYear;
    }

    public void setPosYear(int posYear) {
        this.posYear = posYear;
    }

    public int getPosNorm() {
        return posNorm;
    }

    public void setPosNorm(int posNorm) {
        this.posNorm = posNorm;
    }

    public int getPosDepart() {
        return posDepart;
    }

    public void setPosDepart(int posDepart) {
        this.posDepart = posDepart;
    }

    public int getPosLast() {
        return posLast;
    }

    public void setPosLast(int posLast) {
        this.posLast = posLast;
    }

    public int getWidthActual() {
        return widthActual;
    }

    public void setWidthActual(int widthActual) {
        this.widthActual = widthActual;
    }

    public int getWidthTime() {
        return widthTime;
    }

    public void setWidthTime(int widthTime) {
        this.widthTime = widthTime;
    }

    public int getWidthRecord() {
        return widthRecord;
    }

    public void setWidthRecord(int widthRecord) {
        this.widthRecord = widthRecord;
    }

    public int getWidthYear() {
        return widthYear;
    }

    public void setWidthYear(int widthYear) {
        this.widthYear = widthYear;
    }

    public int getWidthNorm() {
        return widthNorm;
    }

    public void setWidthNorm(int widthNorm) {
        this.widthNorm = widthNorm;
    }

    public int getWidthDepart() {
        return widthDepart;
    }

    public void setWidthDepart(int widthDepart) {
        this.widthDepart = widthDepart;
    }

    public int getWidthLast() {
        return widthLast;
    }

    public void setWidthLast(int widthLast) {
        this.widthLast = widthLast;
    }

    public boolean isTime() {
        return time;
    }

    public void setTime(boolean time) {
        this.time = time;
    }

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }

    public boolean isYear() {
        return year;
    }

    public void setYear(boolean year) {
        this.year = year;
    }

    public boolean isNorm() {
        return norm;
    }

    public void setNorm(boolean norm) {
        this.norm = norm;
    }

    public boolean isDepart() {
        return depart;
    }

    public void setDepart(boolean depart) {
        this.depart = depart;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    /**
     * Migrated from TYPE_tab_sets.h. Used to set spacing for period data in
     * NWWS report in ClimateFormatter.
     */
    public void setPeriodTabs() {

        posValue = DEFAULT_PERIOD_VALUE;
        posActDate = DEFAULT_PERIOD_ACT_DATE;
        posNorm = DEFAULT_PERIOD_NORM;
        posDepart = DEFAULT_PERIOD_DEPART;
        posLastYr = DEFAULT_PERIOD_LAST_YR;
        posLastDate = DEFAULT_PERIOD_LAST_DATE;
    }

    /**
     * Migrated from set_tabs.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *
    *   Purpose:  This routine sets up the tab stops, field widths, and
    *             flags which control whether a particular column is 
    *             included in the NWWS climate summary and the position
    *             of that column.
    * 
     * </pre>
     * 
     * Used to set spacing for daily NWWS report.
     * 
     * @param settings
     */
    public void setDailyTabs(ClimateProductType settings,
            ClimateDate beginDate) {
        boolean snowReport = ClimateNWWSFormat
                .reportWindow(settings.getControl().getSnowDates(), beginDate);

        if (settings.getControl().getTempControl().getMeanTemp().isMeasured()
                || settings.getControl().getTempControl().getMaxTemp()
                        .isMeasured()
                || settings.getControl().getTempControl().getMinTemp()
                        .isMeasured()
                || settings.getControl().getPrecipControl().getPrecipTotal()
                        .isMeasured()
                || settings.getControl().getSnowControl().getSnowTotal()
                        .isMeasured()
                || settings.getControl().getDegreeDaysControl().getTotalHDD()
                        .isMeasured()
                || settings.getControl().getDegreeDaysControl().getTotalCDD()
                        .isMeasured()) {
            posActual = 25;
            widthActual = 6;

        } else {
            posActual = DEFAULT_NONE;
            widthActual = DEFAULT_NONE;

        }

        if (settings.getControl().getTempControl().getMaxTemp()
                .isTimeOfMeasured()
                || settings.getControl().getTempControl().getMinTemp()
                        .isTimeOfMeasured()
                || settings.getControl().getTempControl().getMeanTemp()
                        .isTimeOfMeasured()) {
            widthTime = 8;
            posTime = posActual + widthTime + 1;
            time = true;
        } else {
            widthTime = 1;
            posTime = posActual + widthTime + 1;
            time = false;
        }

        if (settings.getControl().getTempControl().getMaxTemp().isRecord()
                || settings.getControl().getTempControl().getMinTemp()
                        .isRecord()
                || settings.getControl().getTempControl().getMeanTemp()
                        .isRecord()
                || settings.getControl().getPrecipControl().getPrecipTotal()
                        .isRecord()
                || settings.getControl().getPrecipControl().getPrecipMonth()
                        .isRecord()
                || settings.getControl().getPrecipControl().getPrecipSeason()
                        .isRecord()
                || settings.getControl().getPrecipControl().getPrecipYear()
                        .isRecord()
                || (settings.getControl().getSnowControl().getSnowTotal()
                        .isRecord() && snowReport)
                || (settings.getControl().getSnowControl().getSnowMonth()
                        .isRecord() && snowReport)
                || (settings.getControl().getSnowControl().getSnowSeason()
                        .isRecord() && snowReport)
                || (settings.getControl().getSnowControl().getSnowYear()
                        .isRecord() && snowReport)) {
            widthRecord = 6;
            posRecord = posTime + widthRecord;
            record = true;

            if (settings.getControl().getTempControl().getMaxTemp()
                    .isRecordYear()
                    || settings.getControl().getTempControl().getMinTemp()
                            .isRecordYear()
                    || settings.getControl().getPrecipControl().getPrecipTotal()
                            .isRecordYear()
                    || settings.getControl().getPrecipControl().getPrecipMonth()
                            .isRecordYear()
                    || settings.getControl().getPrecipControl()
                            .getPrecipSeason().isRecordYear()
                    || settings.getControl().getPrecipControl().getPrecipYear()
                            .isRecordYear()
                    || (settings.getControl().getSnowControl().getSnowTotal()
                            .isRecordYear() && snowReport)
                    || (settings.getControl().getSnowControl().getSnowMonth()
                            .isRecordYear() && snowReport)
                    || (settings.getControl().getSnowControl().getSnowSeason()
                            .isRecordYear() && snowReport)
                    || (settings.getControl().getSnowControl().getSnowYear()
                            .isRecordYear() && snowReport)) {
                widthYear = 4;
                posYear = posRecord + widthYear;
                year = true;
            } else {
                widthYear = DEFAULT_NONE;
                posYear = posRecord + widthYear - 1;
                year = false;
            }
        } else {
            widthRecord = DEFAULT_NONE;
            widthYear = DEFAULT_NONE;
            posRecord = posTime + widthRecord - 1;
            posYear = posRecord + widthYear - 1;
            record = false;
            year = false;
        }

        if (settings.getControl().getTempControl().getMaxTemp().isNorm()
                || settings.getControl().getTempControl().getMinTemp().isNorm()
                || settings.getControl().getTempControl().getMeanTemp().isNorm()
                || settings.getControl().getPrecipControl().getPrecipTotal()
                        .isNorm()
                || settings.getControl().getPrecipControl().getPrecipMonth()
                        .isNorm()
                || settings.getControl().getPrecipControl().getPrecipSeason()
                        .isNorm()
                || settings.getControl().getPrecipControl().getPrecipYear()
                        .isNorm()
                || (settings.getControl().getSnowControl().getSnowTotal()
                        .isNorm() && snowReport)
                || (settings.getControl().getSnowControl().getSnowMonth()
                        .isNorm() && snowReport)
                || (settings.getControl().getSnowControl().getSnowSeason()
                        .isNorm() && snowReport)
                || (settings.getControl().getSnowControl().getSnowYear()
                        .isNorm() && snowReport)) {
            widthNorm = 6;
            posNorm = posYear + widthNorm;
            norm = true;
        } else {
            widthNorm = DEFAULT_NONE;
            posNorm = posYear + widthNorm;
            norm = false;
        }

        if (settings.getControl().getTempControl().getMaxTemp().isDeparture()
                || settings.getControl().getTempControl().getMinTemp()
                        .isDeparture()
                || settings.getControl().getTempControl().getMeanTemp()
                        .isDeparture()
                || settings.getControl().getPrecipControl().getPrecipTotal()
                        .isDeparture()
                || settings.getControl().getPrecipControl().getPrecipMonth()
                        .isDeparture()
                || settings.getControl().getPrecipControl().getPrecipSeason()
                        .isDeparture()
                || settings.getControl().getPrecipControl().getPrecipYear()
                        .isDeparture()
                || (settings.getControl().getSnowControl().getSnowTotal()
                        .isDeparture() && snowReport)
                || (settings.getControl().getSnowControl().getSnowMonth()
                        .isDeparture() && snowReport)
                || (settings.getControl().getSnowControl().getSnowSeason()
                        .isDeparture() && snowReport)
                || (settings.getControl().getSnowControl().getSnowYear()
                        .isDeparture() && snowReport)) {
            widthDepart = 11;
            posDepart = posNorm + widthDepart;
            depart = true;
        } else {
            widthDepart = DEFAULT_NONE;
            posDepart = posNorm + widthDepart - 1;
            depart = false;
        }

        if (settings.getControl().getTempControl().getMaxTemp().isLastYear()
                || settings.getControl().getTempControl().getMinTemp()
                        .isLastYear()
                || settings.getControl().getTempControl().getMeanTemp()
                        .isLastYear()
                || settings.getControl().getPrecipControl().getPrecipTotal()
                        .isLastYear()
                || settings.getControl().getPrecipControl().getPrecipMonth()
                        .isLastYear()
                || settings.getControl().getPrecipControl().getPrecipSeason()
                        .isLastYear()
                || settings.getControl().getPrecipControl().getPrecipYear()
                        .isLastYear()
                || (settings.getControl().getSnowControl().getSnowTotal()
                        .isLastYear() && snowReport)
                || (settings.getControl().getSnowControl().getSnowMonth()
                        .isLastYear() && snowReport)
                || (settings.getControl().getSnowControl().getSnowSeason()
                        .isLastYear() && snowReport)
                || (settings.getControl().getSnowControl().getSnowYear()
                        .isLastYear() && snowReport)) {
            widthLast = 4;
            if (!depart) {
                widthLast = 8;
            }

            posLast = posDepart + widthLast + 2;
            last = true;
        } else {
            widthLast = DEFAULT_NONE;
            posLast = posDepart + widthLast + 3;
            last = false;
        }
    }

    /**
     * @return the posValue
     */
    public int getPosValue() {
        return posValue;
    }

    /**
     * @param posValue
     *            the posValue to set
     */
    public void setPosValue(int posValue) {
        this.posValue = posValue;
    }

    /**
     * @return the posActDate
     */
    public int getPosActDate() {
        return posActDate;
    }

    /**
     * @param posActDate
     *            the posActDate to set
     */
    public void setPosActDate(int posActDate) {
        this.posActDate = posActDate;
    }

    /**
     * @return the posLastYr
     */
    public int getPosLastYr() {
        return posLastYr;
    }

    /**
     * @param posLastYr
     *            the posLastYr to set
     */
    public void setPosLastYr(int posLastYr) {
        this.posLastYr = posLastYr;
    }

    /**
     * @return the posLastDate
     */
    public int getPosLastDate() {
        return posLastDate;
    }

    /**
     * @param posLastDate
     *            the posLastDate to set
     */
    public void setPosLastDate(int posLastDate) {
        this.posLastDate = posLastDate;
    }
}