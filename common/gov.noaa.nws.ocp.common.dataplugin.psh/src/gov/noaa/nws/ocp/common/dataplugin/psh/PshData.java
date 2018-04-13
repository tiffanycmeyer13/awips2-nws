/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.localization.psh.PshBasin;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigHeader;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;

/**
 * PshData - main class to represent the data used to generate a PSH product.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2017            pwang       Initial creation
 * Aug 15, 2017  #35738    jwu         Add getters/setters
 * Aug 30, 2017            pwang       To simplify XML marshal, storm data model changed from MAP to separate lists
 * Sep 14, 2017  #37365    jwu         Add status & other methods
 * Oct 20, 2017  #39468    jwu         Make "remarks" independent of data entries
 * Nov 08, 2017  #40423    jwu         Replace tide/surge with water level.
 * Jan 11, 2018  DCS19326  jwu         Baseline version.
 * Mar 06, 2018  #47069    wpaintsil   Remove initialize(). Check for null in getters instead.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@XmlRootElement(name = "PshData")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class PshData {

    @DynamicSerializeElement
    @XmlAttribute
    private String basinName;

    @DynamicSerializeElement
    @XmlAttribute
    private int year;

    @DynamicSerializeElement
    @XmlAttribute
    private String stormName;

    @DynamicSerializeElement
    @XmlAttribute
    private String forecaster;

    @DynamicSerializeElement
    @XmlAttribute
    private PshStormType stormType;

    @DynamicSerializeElement
    @XmlAttribute
    private int stormNumber;

    @DynamicSerializeElement
    @XmlAttribute
    private String status;

    @DynamicSerializeElement
    @XmlAttribute
    private String route;

    @DynamicSerializeElement
    @XmlElementWrapper(name = "IncludedCounties")
    @XmlElements(@XmlElement(name = "county", type = String.class))
    private List<String> includedCounties;

    @DynamicSerializeElement
    @XmlElementWrapper(name = "UpdateInfoSet")
    @XmlElements(@XmlElement(name = "updateInfo", type = IssueType.class))
    private List<IssueType> updateInfo;

    @DynamicSerializeElement
    @XmlElement(name = "Metar")
    private MetarStormData metar;

    @DynamicSerializeElement
    @XmlElement(name = "NonMetar")
    private NonMetarStormData nonmetar;

    @DynamicSerializeElement
    @XmlElement(name = "Marine")
    private MarineStormData marine;

    @DynamicSerializeElement
    @XmlElement(name = "Rainfall")
    private RainfallStormData rainfall;

    @DynamicSerializeElement
    @XmlElement(name = "Flooding")
    private FloodingStormData flooding;

    @DynamicSerializeElement
    @XmlElement(name = "WaterLevel")
    private WaterLevelStormData waterLevel;

    @DynamicSerializeElement
    @XmlElement(name = "Tornado")
    private TornadoStormData tornado;

    @DynamicSerializeElement
    @XmlElement(name = "StormEffect")
    private EffectStormData effect;

    /**
     * Constructor
     */
    public PshData() {
    }

    /**
     * @return the basinName
     */
    public String getBasinName() {
        if (basinName == null) {
            basinName = PshBasin.AT.getName();
        }
        return basinName;
    }

    /**
     * @param basinName
     *            the basinName to set
     */
    public void setBasinName(String basinName) {
        this.basinName = basinName;
    }

    /**
     * @return the year
     */
    public int getYear() {
        if (year == 0) {
            year = TimeUtil.newCalendar().get(Calendar.YEAR);
        }
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the stormName
     */
    public String getStormName() {
        if (stormName == null) {
            stormName = "";
        }
        return stormName;
    }

    /**
     * @param stormName
     *            the stormName to set
     */
    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

    /**
     * @return the forecaster
     */
    public String getForecaster() {
        return forecaster;
    }

    /**
     * @param forecaster
     *            the forecaster to set
     */
    public void setForecaster(String forecaster) {
        this.forecaster = forecaster;
    }

    /**
     * @return the stormType
     */
    public PshStormType getStormType() {
        if (stormType == null) {
            stormType = PshStormType.HURRICANE;
        }
        return stormType;
    }

    /**
     * @param stormType
     *            the stormType to set
     */
    public void setStormType(PshStormType stormType) {
        this.stormType = stormType;
    }

    /**
     * @return the stormNumber
     */
    public int getStormNumber() {
        return stormNumber;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        if (status == null) {
            status = IssueType.ROUTINE;
        }
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @param stormNumber
     *            the stormNumber to set
     */
    public void setStormNumber(int stormNumber) {
        this.stormNumber = stormNumber;
    }

    /**
     * @return the route
     */
    public String getRoute() {
        if (route == null) {
            route = "ALL";
        }
        return route;
    }

    /**
     * @param route
     *            the route to set
     */
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * @return the includedCounties
     */
    public List<String> getIncludedCounties() {
        if (includedCounties == null) {
            includedCounties = new ArrayList<>();
        }

        return includedCounties;
    }

    /**
     * @param includedCounties
     *            the includedCounties to set
     */
    public void setIncludedCounties(List<String> includedCounties) {
        this.includedCounties = includedCounties;
    }

    /**
     * @return the updateInfo
     */
    public List<IssueType> getUpdateInfo() {
        if (updateInfo == null) {
            updateInfo = new ArrayList<>();
        }
        return updateInfo;
    }

    /**
     * @param updateInfo
     *            the updateInfo to set
     */
    public void setUpdateInfo(List<IssueType> updateInfo) {
        if (updateInfo != null) {
            this.updateInfo = updateInfo;
        } else {
            this.updateInfo = new ArrayList<>();
        }
    }

    /**
     * @return the metar
     */
    public MetarStormData getMetar() {
        if (metar == null) {
            metar = new MetarStormData();
        }
        return metar;
    }

    /**
     * @param metar
     *            the metar to set
     */
    public void setMetar(MetarStormData metar) {
        this.metar = metar;
    }

    /**
     * @return the nonmetar
     */
    public NonMetarStormData getNonmetar() {
        if (nonmetar == null) {
            nonmetar = new NonMetarStormData();
        }
        return nonmetar;
    }

    /**
     * @param nonmetar
     *            the nonmetar to set
     */
    public void setNonmetar(NonMetarStormData nonmetar) {
        this.nonmetar = nonmetar;
    }

    /**
     * @return the rainfall
     */
    public RainfallStormData getRainfall() {
        if (rainfall == null) {
            rainfall = new RainfallStormData();
        }
        return rainfall;
    }

    /**
     * @param rainfall
     *            the rainfall to set
     */
    public void setRainfall(RainfallStormData rainfall) {
        this.rainfall = rainfall;
    }

    /**
     * @return the marine
     */
    public MarineStormData getMarine() {
        if (marine == null) {
            marine = new MarineStormData();
        }
        return marine;
    }

    /**
     * @param marine
     *            the marine to set
     */
    public void setMarine(MarineStormData marine) {
        this.marine = marine;
    }

    /**
     * @return the flooding
     */
    public FloodingStormData getFlooding() {
        if (flooding == null) {
            flooding = new FloodingStormData();
        }
        return flooding;
    }

    /**
     * @param flooding
     *            the flooding to set
     */
    public void setFlooding(FloodingStormData flooding) {
        this.flooding = flooding;
    }

    /**
     * @return the waterLevel
     */
    public WaterLevelStormData getWaterLevel() {
        if (waterLevel == null) {
            waterLevel = new WaterLevelStormData();
        }
        return waterLevel;
    }

    /**
     * @param waterLevel
     *            the waterLevel to set
     */
    public void setWaterLevel(WaterLevelStormData waterLevel) {
        this.waterLevel = waterLevel;
    }

    /**
     * @return the tornado
     */
    public TornadoStormData getTornado() {
        if (tornado == null) {
            tornado = new TornadoStormData();
        }
        return tornado;
    }

    /**
     * @param tornado
     *            the tornado to set
     */
    public void setTornado(TornadoStormData tornado) {
        this.tornado = tornado;
    }

    /**
     * @return the effect
     */
    public EffectStormData getEffect() {
        if (effect == null) {
            effect = new EffectStormData();
        }
        return effect;
    }

    /**
     * @param effect
     *            the effect to set
     */
    public void setEffect(EffectStormData effect) {
        this.effect = effect;
    }

    /**
     * Update the status, used when a new issue type is selected.
     * 
     */
    public void updateStatus() {
        // "status" will be "Updated", "Corrected", or "Routine" if no update.
        Calendar dt = null;
        IssueType lastIssue = null;
        for (IssueType isp : updateInfo) {
            if (dt == null) {
                dt = isp.getDate();
                lastIssue = isp;
                continue;
            }

            if (dt.before(isp.getDate())) {
                dt = isp.getDate();
                lastIssue = isp;
            }

            if (lastIssue != null && !lastIssue.isRoutine()) {
                setStatus(lastIssue.getCategory());
            }
        }
    }

    /**
     * Format update info into strings to be used by the product builder.
     * 
     * @return List of string
     */
    public List<String> getUpdateInfoData() {
        List<String> updateInfoData = new ArrayList<>();
        SimpleDateFormat tf = new SimpleDateFormat("MMM d");

        PshConfigHeader headerInfo = PshConfigurationManager.getInstance()
                .getConfigHeader();
        TimeZone tz = TimeZone
                .getTimeZone(headerInfo.getTimeZone().getZonedId());

        tf.setTimeZone(tz);

        // Add "Update" first
        for (IssueType isp : updateInfo) {
            if (isp.isUpdated()) {
                StringBuilder sb = new StringBuilder();
                sb.append(tf.format(isp.getDate().getTime()));
                sb.append("..." + isp.getCategory());
                sb.append(" for..." + isp.getReason());
                updateInfoData.add(sb.toString());
            }
        }

        // Add "Corrected" now
        for (IssueType isp : updateInfo) {
            if (isp.isCorrected()) {
                StringBuilder sb = new StringBuilder();
                sb.append(tf.format(isp.getDate().getTime()));
                sb.append("..." + isp.getCategory());
                sb.append(" for..." + isp.getReason());
                updateInfoData.add(sb.toString());
            }
        }

        return updateInfoData;
    }

    /**
     * Get the latest message type.
     * 
     * Message types include: "ROU", "AAA", "AAB", "AAC", "AAD", "AAE", "AAF",
     * "AAG", "CCA", "CCB", "CCC", "CCD", "CCE", "CCF", "CCG"
     * 
     * @return latest message type
     */
    public String getLatestMessageType() {

        String issueType = null;
        Calendar dt = null;
        IssueType lastIssue = null;
        if (updateInfo != null) {
            for (IssueType isp : updateInfo) {
                if (dt == null) {
                    dt = isp.getDate();
                    lastIssue = isp;
                    continue;
                }

                if (dt.before(isp.getDate())) {
                    dt = isp.getDate();
                    lastIssue = isp;
                }

                if (lastIssue != null && !lastIssue.isRoutine()) {
                    issueType = lastIssue.getId();
                }
            }
        }

        return issueType;
    }

}