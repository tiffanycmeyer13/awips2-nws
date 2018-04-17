/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao.data;

/**
 * Decoded METAR data Recent Weather, from METAR report text. From
 * dbtypedefs.h#Recent_Wx
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 01 FEB 2017  28609      amoore      Initial creation
 * </pre>
 *
 * @author amoore
 * @version 1.0
 */
public class RecentWx {

    /** Recent weather token */
    private String recentWeatherName;

    /** Begining hour of recent weather */
    private int beginHour;

    /** Begining minute of recent weather */
    private int beginMinute;

    /** Ending hour of recent weather */
    private int endHour;

    /** Ending minute of recent weather */
    private int endMinute;

    /**
     * Empty constructor.
     */
    public RecentWx() {
    }

    /**
     * @return the recentWeatherName
     */
    public String getRecentWeatherName() {
        return recentWeatherName;
    }

    /**
     * @param recentWeatherName
     *            the recentWeatherName to set
     */
    public void setRecentWeatherName(String recentWeatherName) {
        this.recentWeatherName = recentWeatherName;
    }

    /**
     * @return the beginHour
     */
    public int getBeginHour() {
        return beginHour;
    }

    /**
     * @param beginHour
     *            the beginHour to set
     */
    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    /**
     * @return the beginMinute
     */
    public int getBeginMinute() {
        return beginMinute;
    }

    /**
     * @param beginMinute
     *            the beginMinute to set
     */
    public void setBeginMinute(int beginMinute) {
        this.beginMinute = beginMinute;
    }

    /**
     * @return the endHour
     */
    public int getEndHour() {
        return endHour;
    }

    /**
     * @param endHour
     *            the endHour to set
     */
    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    /**
     * @return the endMinute
     */
    public int getEndMinute() {
        return endMinute;
    }

    /**
     * @param endMinute
     *            the endMinute to set
     */
    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }
}
