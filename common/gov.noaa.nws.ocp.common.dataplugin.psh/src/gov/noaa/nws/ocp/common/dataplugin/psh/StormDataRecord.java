/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * StormDataRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2017            pwang       Initial creation
 * Aug 04, 2017            pwang       Persistent in metadata
 * Jan 05, 2017            jwu         Remove "insertDateTime"
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * Mar 06, 2018 47069      wpaintsil   Remove dataURI column.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "pshdataseq")
@Table(name = "pshdata", uniqueConstraints = {
        @UniqueConstraint(name = "uk_psh_datauri_fields", columnNames = {
                "basin", "year", "stormName" }) })

@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class StormDataRecord extends PersistablePluginDataObject {

    private static final long serialVersionUID = 4961497688L;

    public static final String pluginName = "psh";

    public static final String STORMDATA_XML = "StormDataXML";

    @DataURI(position = 1)
    @Column(nullable = false)
    @DynamicSerializeElement
    private String basin;

    @DataURI(position = 2)
    @Column(nullable = false)
    @DynamicSerializeElement
    private int year;

    @DataURI(position = 3)
    @Column(nullable = false)
    @DynamicSerializeElement
    private String stormName;

    @DynamicSerializeElement
    @Column
    private String forecaster;

    /* serialized PshData Object */
    @Transient
    private String stormDataXML;

    /**
     * Empty constructor
     */
    public StormDataRecord() {
        super.setDataTime(getRefTime());

    }

    /**
     * Constructor
     *
     * @param uri
     */
    public StormDataRecord(String uri) {
        super(uri);
    }

    /**
     * Constructor
     *
     * @param basin
     * @param year
     * @param stormName
     */
    public StormDataRecord(String basin, int year, String stormName) {
        this.basin = basin;
        this.year = year;
        this.stormName = stormName;
    }

    private DataTime getRefTime() {
        Calendar now = TimeUtil.newCalendar();
        DataTime dt = new DataTime(now.getTime());
        return dt;
    }

    /**
     * @return the basin
     */
    public String getBasin() {
        return basin;
    }

    /**
     * @param basin
     *            the basin to set
     */
    public void setBasin(String basin) {
        this.basin = basin;
    }

    /**
     * @return the year
     */
    public int getYear() {
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
     * @return the stormDataXML
     */
    public String getStormDataXML() {
        return stormDataXML;
    }

    /**
     * @param stormDataXML
     *            the stormDataXML to set
     */
    public void setStormDataXML(String stormDataXML) {
        this.stormDataXML = stormDataXML;
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

    @Override
    public String getPluginName() {
        return pluginName;
    }

}
