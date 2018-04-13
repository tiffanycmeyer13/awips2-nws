/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.producttype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Climate product flags (a set of boolean flags to indicate if an element needs
 * to be reported).
 *
 * <pre>
 *
 **   TYPE_do_weather_element.h/TYPE_do_p_weather_element.h
 *
 *   DESCRIPTION
 *        Contains flags for determining which weather elements are reported
 *        upon in the climate summary
 *
 *        do_weather_element- derived type variable containing flags which 
 *                              determine which climate variables are reported
 *
 *         do_weather_element
 *             measured     - flag for reporting the measured values of a 
 *                            given climatological variable
 *                            = TRUE report this measured variable
 *                            = FALSE don't report this measured variable
 *             time_of_measured - flag for reporting the time a given
 *                                climatological variable was observed
 *                            = TRUE  report the time of occurrence
 *                            = FALSE don't report the time of occurrence
 *             norm         - flag for reporting the normal (either daily or
 *                            monthly) values for this variable
 *                            = TRUE report the normal value for this variable
 *                            = FALSE don't report the normal value for this
 *                              variable
 *             record       - flag for reporting the record values for this
 *                            climatological variable
 *                            = TRUE  report the record values for this 
 *                                    climatological variable
 *                            = FALSE don't report the record values of this
 *                                    climatological variable
 *             recordYear   - flag for reporting the recordYear(s) in which the record 
 *
 *                            observed; note this variable is automatically set
 *                            to FALSE if record=FALSE
 *                            = TRUE  report the recordYear(s) in which the record event
 *                                    for this variable was observed
 *                            = FALSE don't report the recordYear in which the record
 *                                    event for this variable was observed
 *                            set to FALSE if the "record" is set to FALSE                   
 *             departure    - flag for reporting the departure from normal
 *                            for a particular climatological variable; note
 *                            that this variable is automatically set to 
 *                            FALSE if "norm" is set to FALSE
 *                            = TRUE   report the departure from the mean
 *                            = FALSE  don't report the departure from the
 *                                     mean; this is automatically set to 
 *                                     FALSE if norm=FALSE
 *             last_year    - flag for reporting last recordYear's value of a 
 *                            particular climatological variable
 *                            = TRUE   report last recordYear's value
 *                            = FALSE  don't report last recordYear's value
 *             total_month  - flag for reporting the cumulative value for the
 *                            month
 *                            = TRUE   report the month's cumulative value
 *                            = FALSE  don't report the month's accumulation
 *             total_season - flag for reporting the total accumulation of a
 *                            particular climatological variable for the 
 *                            season to date
 *                            = TRUE  report the total for the season
 *                            = FALSE don't report the total for the season
 *             total_year   - flag for reporting the total accumulation of a
 *                            particular climatological variable for the 
 *                            recordYear to date
 *                            = TRUE  report the recordYear to date total
 *                            = FALSE don't report the recordYear to date total
 *
 *             date_of_last - flag for reporting the date in which last recordYear's 
 *                            value of a particular climatological variable was 
 *                            observed; note this variable is automatically set
 *                            to FALSE if record=FALSE
 *                            = TRUE  report the date in which this variable was 
 *                    observed
 *                            = FALSE don't report the date in which the 
 *                    variable was observed
 *                            set to FALSE if the "record" is set to FALSE
 * Note:
 * 
 * "date_of_last" is included in "TYPE_do_p_weather_element.h" only and "total_month",
 * "total_season", & "total_year" are included only in ""TYPE_do_weather_element.h". We 
 * put all of them in thi single class.
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ----------- ----------- ------------ ----------------------
 * Nov 30, 2016 20640      jwu          Initial creation
 * Feb 17, 2017 21099      wpaintsil    Rename getYear to getRecordYear to avoid serialization error.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimateProductFlags")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateProductFlags {

    /**
     * Flag for reporting the measured values of a given climatological variable
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Measured")
    private boolean measured;

    /**
     * Flag for reporting the time a given climatological variable was observed.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "TimeOfMeasured")
    private boolean timeOfMeasured;

    /**
     * Flag for reporting the normal (either daily or monthly) values for this
     * variable.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Norm")
    private boolean norm;

    /**
     * Flag for reporting the record values for this climatological variable.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Record")
    private boolean record;

    /**
     * Flag for reporting the recordYear(s) in which the record observed; note
     * this variable is automatically set to FALSE if record=FALSE.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "RecordYear")
    private boolean recordYear;

    /**
     * Flag for reporting the departure from normal for a particular
     * climatological variable; note that this variable is automatically set to
     * FALSE if "norm" is set to FALSE.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Departure")
    private boolean departure;

    /**
     * Flag for reporting last recordYear's value of a particular climatological
     * variable.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "LastYear")
    private boolean lastYear;

    /**
     * Flag for reporting the date in which last recordYear's value of a
     * particular climatological variable was observed; note this variable is
     * automatically set to FALSE if record=FALSE.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "DateOfLast")
    private boolean dateOfLast;

    /**
     * Flag for reporting the cumulative value for the month.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "TotalMonth")
    private boolean totalMonth;

    /**
     * Flag for reporting the total accumulation of a particular climatological
     * variable for the season to date.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "TotalSeason")
    private boolean totalSeason;

    /**
     * Flag for reporting the total accumulation of a particular climatological
     * variable for the recordYear to date.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "TotalYear")
    private boolean totalYear;

    /**
     * Default constructor
     */
    public ClimateProductFlags() {
    }

    /**
     * @return the measured
     */
    public boolean isMeasured() {
        return measured;
    }

    /**
     * @param measured
     *            the measured to set
     */
    public void setMeasured(boolean measured) {
        this.measured = measured;
    }

    /**
     * @return the timeOfMeasured
     */
    public boolean isTimeOfMeasured() {
        return timeOfMeasured;
    }

    /**
     * @param timeOfMeasured
     *            the timeOfMeasured to set
     */
    public void setTimeOfMeasured(boolean timeOfMeasured) {
        this.timeOfMeasured = timeOfMeasured;
    }

    /**
     * @return the norm
     */
    public boolean isNorm() {
        return norm;
    }

    /**
     * @param norm
     *            the norm to set
     */
    public void setNorm(boolean norm) {
        this.norm = norm;
    }

    /**
     * @return the record
     */
    public boolean isRecord() {
        return record;
    }

    /**
     * @param reportRecord
     *            the record to set
     */
    public void setRecord(boolean record) {
        this.record = record;
    }

    /**
     * @return the recordYear
     */
    public boolean isRecordYear() {
        return recordYear;
    }

    /**
     * @param recordYear
     *            the recordYear to set
     */
    public void setRecordYear(boolean year) {
        this.recordYear = year;
    }

    /**
     * @return the departure
     */
    public boolean isDeparture() {
        return departure;
    }

    /**
     * @param departure
     *            the departure to set
     */
    public void setDeparture(boolean departure) {
        this.departure = departure;
    }

    /**
     * @return the lastYear
     */
    public boolean isLastYear() {
        return lastYear;
    }

    /**
     * @param lastYear
     *            the lastYear to set
     */
    public void setLastYear(boolean lastYear) {
        this.lastYear = lastYear;
    }

    /**
     * @return the dateOfLast
     */
    public boolean isDateOfLast() {
        return dateOfLast;
    }

    /**
     * @param dateOfLast
     *            the dateOfLast to set
     */
    public void setDateOfLast(boolean dateOfLast) {
        this.dateOfLast = dateOfLast;
    }

    /**
     * @return the totalMonth
     */
    public boolean isTotalMonth() {
        return totalMonth;
    }

    /**
     * @param totalMonth
     *            the totalMonth to set
     */
    public void setTotalMonth(boolean totalMonth) {
        this.totalMonth = totalMonth;
    }

    /**
     * @return the totalSeason
     */
    public boolean isTotalSeason() {
        return totalSeason;
    }

    /**
     * @param totalSeason
     *            the totalSeason to set
     */
    public void setTotalSeason(boolean totalSeason) {
        this.totalSeason = totalSeason;
    }

    /**
     * @return the totalYear
     */
    public boolean isTotalYear() {
        return totalYear;
    }

    /**
     * @param totalYear
     *            the totalYear to set
     */
    public void setTotalYear(boolean totalYear) {
        this.totalYear = totalYear;
    }

    /**
     * @return a new object filled with default values.
     */
    public static ClimateProductFlags getDefaultClimateProductFlags() {
        ClimateProductFlags flags = new ClimateProductFlags();
        flags.setDataToDefault();
        return flags;
    }

    /**
     * Set data to default values.
     * 
     */
    public void setDataToDefault() {

        this.measured = false;
        this.timeOfMeasured = false;
        this.norm = false;
        this.record = false;
        this.recordYear = false;
        this.departure = false;
        this.lastYear = false;
        this.dateOfLast = false;
        this.totalMonth = false;
        this.totalSeason = false;
        this.totalYear = false;
    }

    /**
     * @return a deep copy of this object.
     */
    public ClimateProductFlags copy() {

        ClimateProductFlags flags = new ClimateProductFlags();
        flags.measured = this.measured;
        flags.timeOfMeasured = this.timeOfMeasured;
        flags.norm = this.norm;
        flags.record = this.record;
        flags.recordYear = this.recordYear;
        flags.departure = this.departure;
        flags.lastYear = this.lastYear;
        flags.dateOfLast = this.dateOfLast;
        flags.totalMonth = this.totalMonth;
        flags.totalSeason = this.totalSeason;
        flags.totalYear = this.totalYear;

        return flags;
    }

    /**
     * @return a default of this object with all flags set to false.
     */
    public static ClimateProductFlags getDefaultFlags() {
        ClimateProductFlags flags = new ClimateProductFlags();
        flags.setDataToDefault();
        return flags;
    }

}
