/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.dataplugin.persist.IHDFFilePathProvider;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Plugin data object for ODIM data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "odimseq")
@Table(name = ODIMRecord.PLUGIN_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = { "dataURI" }) }, indexes = {
                @Index(name = "odim_lookupIndex", columnList = "node,quantity,primaryElevationAngle"),
                /*
                 * Both refTime and forecastTime are included in the
                 * refTimeIndex since forecastTime is unlikely to be used.
                 */
                @Index(name = "odim_refTimeIndex", columnList = "refTime,forecastTime") })
@DynamicSerialize
public class ODIMRecord extends PersistablePluginDataObject
        implements Cloneable {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "odim";

    @Column(length = 7, nullable = false)
    @DataURI(position = 1)
    @DynamicSerializeElement
    private String node;

    @Column(length = 12, nullable = false)
    @DataURI(position = 2)
    @DynamicSerializeElement
    private String quantity;

    @Column
    @DataURI(position = 3)
    @DynamicSerializeElement
    private Double primaryElevationAngle;

    @Column
    @DynamicSerializeElement
    private Double trueElevationAngle;

    @Column
    @DynamicSerializeElement
    private Integer elevationNumber;

    @Column
    @DynamicSerializeElement
    private Date startTime;

    @Column
    @DynamicSerializeElement
    private Date endTime;

    @Column
    @DynamicSerializeElement
    private Float latitude;

    @Column
    @DynamicSerializeElement
    private Float longitude;

    // Station elevation
    @Column
    @DynamicSerializeElement
    private Float elevation;

    @Column
    @DynamicSerializeElement
    private Integer gateResolution;

    @Column(length = 7)
    @DynamicSerializeElement
    private String volumeCoveragePattern;

    @Column
    @DynamicSerializeElement
    private Integer numLevels;

    @Column
    @DynamicSerializeElement
    private Integer numRadials;

    @Column
    @DynamicSerializeElement
    private Integer numBins;

    @Column(length = 16)
    @DynamicSerializeElement
    private String unit;

    @Column
    @DynamicSerializeElement
    private Double encodingScale;

    @Column
    @DynamicSerializeElement
    private Double encodingOffset;

    @Column
    @DynamicSerializeElement
    private Integer missingValue;

    @Column
    @DynamicSerializeElement
    private Integer noDataValue;

    @Transient
    protected transient ODIMStoredData storedData;

    public ODIMRecord() {
        storedData = new ODIMStoredData();
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public IHDFFilePathProvider getHDFPathProvider() {
        return ODIMPathProvider.getInstance();
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Integer getElevationNumber() {
        return elevationNumber;
    }

    public void setElevationNumber(Integer elevationNumber) {
        this.elevationNumber = elevationNumber;
    }

    public Double getPrimaryElevationAngle() {
        return primaryElevationAngle;
    }

    public void setPrimaryElevationAngle(Double primaryElevationAngle) {
        this.primaryElevationAngle = primaryElevationAngle;
    }

    public Double getTrueElevationAngle() {
        return trueElevationAngle;
    }

    public void setTrueElevationAngle(Double trueElevationAngle) {
        this.trueElevationAngle = trueElevationAngle;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getElevation() {
        return elevation;
    }

    public void setElevation(Float elevation) {
        this.elevation = elevation;
    }

    public Integer getGateResolution() {
        return gateResolution;
    }

    public void setGateResolution(Integer gateResolution) {
        this.gateResolution = gateResolution;
    }

    public String getVolumeCoveragePattern() {
        return volumeCoveragePattern;
    }

    public void setVolumeCoveragePattern(String volumeCoveragePattern) {
        this.volumeCoveragePattern = volumeCoveragePattern;
    }

    public Integer getNumLevels() {
        return numLevels;
    }

    public void setNumLevels(Integer numLevels) {
        this.numLevels = numLevels;
    }

    public Integer getNumRadials() {
        return numRadials;
    }

    public void setNumRadials(Integer numRadials) {
        this.numRadials = numRadials;
    }

    public Integer getNumBins() {
        return numBins;
    }

    public void setNumBins(Integer numBins) {
        this.numBins = numBins;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getEncodingScale() {
        return encodingScale;
    }

    public void setEncodingScale(Double encodingScale) {
        this.encodingScale = encodingScale;
    }

    public Double getEncodingOffset() {
        return encodingOffset;
    }

    public void setEncodingOffset(Double encodingOffset) {
        this.encodingOffset = encodingOffset;
    }

    public ODIMRecord shallowClone() {
        try {
            return (ODIMRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    public ODIMStoredData getStoredData() {
        return storedData;
    }

    public void setStoredData(ODIMStoredData storedData) {
        this.storedData = storedData;
    }

    public byte[] getRawData() {
        return getStoredData().getRawData();
    }

    public void setRawData(byte[] data) {
        getStoredData().setRawData(data);
    }

    public short[] getRawShortData() {
        return getStoredData().getRawShortData();
    }

    public void setRawShortData(short[] data) {
        getStoredData().setRawShortData(data);
    }

    public float[] getAngleData() {
        return getStoredData().getAngleData();
    }

    public void setAngleData(float[] angleData) {
        getStoredData().setAngleData(angleData);
    }

    public Integer getMissingValue() {
        return missingValue;
    }

    public void setMissingValue(Integer undetectedValue) {
        this.missingValue = undetectedValue;
    }

    public Integer getNoDataValue() {
        return noDataValue;
    }

    public void setNoDataValue(Integer noDataValue) {
        this.noDataValue = noDataValue;
    }

    @Override
    @Column
    @Access(AccessType.PROPERTY)
    public String getDataURI() {
        return super.getDataURI();
    }
}
