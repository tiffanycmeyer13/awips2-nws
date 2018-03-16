/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This enum holds the period report type (other, daily, monthly, seasonal,
 * annual). From add_suffix.f. While Other is not explicitly defined, test
 * against a period type value of "0" was used throughout the Legacy Climate
 * code.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 22 JUL 2016  20414      amoore      Initial creation
 * 31 AUG 2016  20414      amoore      Added all definitions of period type
 *                                     from add_suffix.f.
 * 19 SEP 2016  21378      amoore      Added convenience methods to check type.
 * 12 JAN 2017  20640      jwu         Add xml annotations & more attributes.
 * 23 JAN 2017  22134      amoore      Added necessary setter for new XML attribute.
 * 06 NOV 2017  35731      pwang       added static method to return meaningful product name
 * </pre>
 * 
 * @author amoore
 */
@DynamicSerialize
@XmlRootElement(name = "PeriodType")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum PeriodType {

    /**
     * Other period type.
     */
    OTHER(0, "Other", "Other", "other"),

    /**
     * Morning weather radio daily climate summary.
     */
    MORN_RAD(1, "am", "Daily Morning", "NWR"),

    /**
     * Evening weather radio daily climate summary.
     */
    EVEN_RAD(2, "pm", "Daily Evening", "NWR"),

    /**
     * Morning NWWS daily climate summary.
     */
    MORN_NWWS(3, "am", "Daily Morning", "NWWS"),

    /**
     * Evening NWWS daily climate summary.
     */
    EVEN_NWWS(4, "pm", "Daily Evening", "NWWS"),

    /**
     * Monthly radio climate summary.
     */
    MONTHLY_RAD(5, "mon", "Monthly", "NWR"),

    /**
     * Monthly NWWS climate summary.
     */
    MONTHLY_NWWS(6, "mon", "Monthly", "NWWS"),

    /**
     * Seasonal radio climate summary.
     */
    SEASONAL_RAD(7, "sea", "Seasonal", "NWR"),

    /**
     * Seasonal NWWS climate summary.
     */
    SEASONAL_NWWS(8, "sea", "Seasonal", "NWWS"),

    /**
     * Annual radio climate summary.
     */
    ANNUAL_RAD(9, "ann", "Annual", "NWR"),

    /**
     * Intermediate radio daily climate summary.
     */
    INTER_RAD(10, "im", "Daily Intermediate", "NWR"),

    /**
     * Intermediate NWWS daily climate summary.
     */
    INTER_NWWS(11, "im", "Daily Intermediate", "NWWS"),

    /**
     * Annual NWWS climate summary.
     */
    ANNUAL_NWWS(12, "ann", "Annual", "NWWS");

    /**
     * Unique periods.
     */
    public static final String[] CLIMATE_PERIODS = new String[] {
            "Daily Morning", "Daily Intermediate", "Daily Evening", "Monthly",
            "Seasonal", "Annual" };

    /**
     * Unique sources.
     */
    public static final String[] CLIMATE_SOURCES = new String[] { "NWR",
            "NWWS" };

    @DynamicSerializeElement
    private int value;

    @DynamicSerializeElement
    private String periodName;

    @DynamicSerializeElement
    private String periodDescriptor;

    @DynamicSerializeElement
    private String source;

    /**
     * @param iValue
     */
    private PeriodType(final int iValue, final String periodName,
            final String periodDesciptor, final String source) {
        this.value = iValue;
        this.periodName = periodName;
        this.periodDescriptor = periodDesciptor;
        this.source = source;
    }

    /**
     * @return enum value.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param iValue
     *            value.
     */
    public void setValue(int iValue) {
        value = iValue;
    }

    /**
     * @return the periodName
     */
    public String getPeriodName() {
        return periodName;
    }

    /**
     * @param periodName
     *            the periodName to set
     */
    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    /**
     * @return the periodDescriptor
     */
    public String getPeriodDescriptor() {
        return periodDescriptor;
    }

    /**
     * @param periodDescriptor
     *            the periodDescriptor to set
     */
    public void setPeriodDescriptor(String periodDesciptor) {
        this.periodDescriptor = periodDesciptor;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setType(String source) {
        this.source = source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return true if this period is of a daily type (morning, intermediate, or
     *         evening). False otherwise.
     */
    public boolean isDaily() {
        switch (this) {
        case MORN_NWWS:
        case MORN_RAD:
        case INTER_NWWS:
        case INTER_RAD:
        case EVEN_NWWS:
        case EVEN_RAD:
            return true;
        default:
            return false;
        }
    }

    /**
     * @return true if this period is of a ranged period type (monthly,
     *         seasonal, or annual). False otherwise.
     */
    public boolean isPeriod() {
        switch (this) {
        case MONTHLY_NWWS:
        case MONTHLY_RAD:
        case SEASONAL_NWWS:
        case SEASONAL_RAD:
        case ANNUAL_NWWS:
        case ANNUAL_RAD:
            return true;
        default:
            return false;
        }
    }

    /**
     * @return true if this period is of a morning period type. False otherwise.
     */
    public boolean isMorning() {
        switch (this) {
        case MORN_NWWS:
        case MORN_RAD:
            return true;
        default:
            return false;
        }
    }

    /**
     * @param period
     * 
     * @return true if input string is "Daily Morning". False otherwise.
     */
    public static boolean isMorning(String period) {
        return period.equals(CLIMATE_PERIODS[0]);
    }

    /**
     * @param period
     * 
     * @return true if input string is daily type. False otherwise.
     */
    public static boolean isDaily(String period) {
        return period.equals(CLIMATE_PERIODS[0])
                || period.equals(CLIMATE_PERIODS[1])
                || period.equals(CLIMATE_PERIODS[2]);
    }

    /**
     * @param period
     * 
     * @return true if input string is period type. False otherwise.
     */
    public static boolean isPeriod(String period) {
        return period.equals(CLIMATE_PERIODS[3])
                || period.equals(CLIMATE_PERIODS[4])
                || period.equals(CLIMATE_PERIODS[5]);
    }

    /**
     * @return true if input string is "NWR". False otherwise.
     */
    public static boolean isNWR(String source) {
        return source.equals(CLIMATE_SOURCES[0]);
    }

    /**
     * @return true if input string is "NWR". False otherwise.
     */
    public static boolean isNWWS(String source) {
        return source.equals(CLIMATE_SOURCES[1]);
    }

    /**
     * Identify a PeriodType from report source and period.
     * 
     * @param source
     *            report source
     * @param period
     *            report period
     * @return getPeriodType().
     */
    public static PeriodType getPeriodType(String source, String period) {
        PeriodType ptype = PeriodType.OTHER;
        for (PeriodType typ : PeriodType.values()) {
            if (typ.source.equals(source)
                    && typ.periodDescriptor.equals(period)) {
                ptype = typ;
                break;
            }
        }

        return ptype;
    }

    /**
     * get the period type description
     * 
     * @param periodNum
     * 
     * @return
     */
    public static String getPeriodTypeDesc(int periodValue) {
        PeriodType ptype = PeriodType.OTHER;

        for (PeriodType typ : PeriodType.values()) {
            if (typ.value == periodValue) {
                ptype = typ;
                break;
            }
        }
        return ptype.getPeriodDescriptor();
    }

}
