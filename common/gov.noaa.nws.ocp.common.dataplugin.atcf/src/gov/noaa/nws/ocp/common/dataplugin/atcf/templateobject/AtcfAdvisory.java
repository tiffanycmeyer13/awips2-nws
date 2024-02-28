/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Super class for ATCF advisories for holding common data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 22, 2021 87781      jwu         Initial creation.
 * Mar 04, 2021 88931      jwu         Add "extended" for TAU>120.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
@DynamicSerialize
public class AtcfAdvisory {

    /**
     * Type for this advisory
     */
    @DynamicSerializeElement
    private AdvisoryType type;

    /**
     * Header - site/wfo/node/issuedBy/time zone, etc.
     */
    @DynamicSerializeElement
    private AdvisoryHeader header;

    /**
     * Timezone
     */
    private String timeZone;

    /**
     * Watches and Warnings
     */
    @DynamicSerializeElement
    private String watchWarn;

    /**
     * Track data at TAU 0
     */
    @DynamicSerializeElement
    private ForecastData tau0;

    /**
     * Track data at TAU 3
     */
    @DynamicSerializeElement
    private ForecastData tau3;

    /**
     * Track data from TAU 12 to 96
     */
    @DynamicSerializeElement
    private List<ForecastData> forecast;

    /**
     * Track data for outlook 96 & 120.
     */
    @DynamicSerializeElement
    private List<ForecastData> outlook;

    /**
     * Track data after 120
     */
    @DynamicSerializeElement
    private List<ForecastData> extended;

    /**
     * Time for next intermediate advisory.
     */
    @DynamicSerializeElement
    private ForecastTime nextIntermTime;

    /**
     * Time for next complete advisory.
     */
    @DynamicSerializeElement
    private ForecastTime nextAdvTime;

    /**
     * An empty string used for indentation in handlerBar template
     */
    @DynamicSerializeElement
    private String blank;

    /**
     * A new line character used to force a new line in handlerBar template.
     */
    @DynamicSerializeElement
    private String newLine;

    /**
     * Constructor
     */
    public AtcfAdvisory() {
        this.type = null;
        this.header = new AdvisoryHeader();
        this.timeZone = "UTC";
        this.watchWarn = "";
        this.tau0 = null;
        this.tau3 = null;
        this.forecast = null;
        this.outlook = null;
        this.extended = null;
        this.nextIntermTime = null;
        this.nextAdvTime = null;
        this.blank = "";
        this.newLine = "\n";
    }

    /**
     * @return the type
     */
    public AdvisoryType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(AdvisoryType type) {
        this.type = type;
    }

    /**
     * @return the header
     */
    public AdvisoryHeader getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(AdvisoryHeader header) {
        this.header = header;
    }

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the watchWarn
     */
    public String getWatchWarn() {
        return watchWarn;
    }

    /**
     * @param watchWarn
     *            the watchWarn to set
     */
    public void setWatchWarn(String watchWarn) {
        this.watchWarn = watchWarn;
    }

    /**
     * @return the tau0
     */
    public ForecastData getTau0() {
        return tau0;
    }

    /**
     * @param tau0
     *            the tau0 to set
     */
    public void setTau0(ForecastData tau0) {
        this.tau0 = tau0;
    }

    /**
     * @return the tau3
     */
    public ForecastData getTau3() {
        return tau3;
    }

    /**
     * @param tau3
     *            the tau3 to set
     */
    public void setTau3(ForecastData tau3) {
        this.tau3 = tau3;
    }

    /**
     * @return the forecast
     */
    public List<ForecastData> getForecast() {
        return forecast;
    }

    /**
     * @param forecast
     *            the forecast to set
     */
    public void setForecast(List<ForecastData> forecast) {
        this.forecast = forecast;
    }

    /**
     * @return the outlook
     */
    public List<ForecastData> getOutlook() {
        return outlook;
    }

    /**
     * @param outlook
     *            the outlook to set
     */
    public void setOutlook(List<ForecastData> outlook) {
        this.outlook = outlook;
    }

    /**
     * @return the extended
     */
    public List<ForecastData> getExtended() {
        return extended;
    }

    /**
     * @param extended
     *            the extended to set
     */
    public void setExtended(List<ForecastData> extended) {
        this.extended = extended;
    }

    /**
     * @return the nextIntermTime
     */
    public ForecastTime getNextIntermTime() {
        return nextIntermTime;
    }

    /**
     * @param nextIntermTime
     *            the nextIntermTime to set
     */
    public void setNextIntermTime(ForecastTime nextIntermTime) {
        this.nextIntermTime = nextIntermTime;
    }

    /**
     * @return the nextAdvTime
     */
    public ForecastTime getNextAdvTime() {
        return nextAdvTime;
    }

    /**
     * @param nextAdvTime
     *            the nextAdvTime to set
     */
    public void setNextAdvTime(ForecastTime nextAdvTime) {
        this.nextAdvTime = nextAdvTime;
    }

    /**
     * @return the blank
     */
    public String getBlank() {
        return blank;
    }

    /**
     * @param blank
     *            the blank to set
     */
    public void setBlank(String blank) {
        this.blank = blank;
    }

    /**
     * @return the newLine
     */
    public String getNewLine() {
        return newLine;
    }

    /**
     * @param newLine
     *            the newLine to set
     */
    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }

}
