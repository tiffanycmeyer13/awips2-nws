package gov.noaa.nws.obs.edex.plugin.geodata.description;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.raytheon.uf.edex.netcdf.description.data.mask.DiscreteDataMaskDescription;
import com.raytheon.uf.edex.netcdf.description.exception.InvalidDescriptionException;
import com.raytheon.uf.edex.netcdf.description.field.date.DataTimeDescription;
import com.raytheon.uf.edex.netcdf.description.field.direct.AttributeDescription;
import com.raytheon.uf.edex.netcdf.description.field.direct.VariableDescription;
import com.raytheon.uf.edex.netcdf.description.field.indirect.DelegateFieldDescription;

/**
 * ProductDescription (class)
 * 
 * A class that describes a single Geodata/GeoDB-related product. This class
 * provides the framework that allows for the mapping of NetCDF
 * Variables/Attributes, as well as user-defined values, into the fields of a
 * GeoDataRecord and the String/Float/IntegerAttribute classes that coincide
 * with the GeoDataRecord.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/27/2016     19064    mcomerford  Initial creation (DCS 19064)
 * 
 * </pre>
 * 
 * @author matt.comerford
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
public class ProductDescription {

    /**
     * Represents the user-defined "name" of the description in a DBGeo Product
     * Descriptions file. It is only used in conjunction with the "debug" flag,
     * so a user may understand which ProductDescription is throwing any
     * potential InvalidDescriptionException.
     */
    @XmlAttribute(required = true)
    private String name;

    /**
     * Purely for ProductDescription debug. If this flag is set to "true" in a
     * DBGeo ProductDescriptions file, any InvalidDescriptionException thrown
     * during the decoder process will be logged into the "edex-ingest-geodata"
     * logfile.
     */
    @XmlElement(name = "debug")
    private boolean debug = false;

    /**
     * Represents the Latitude Variable/Array in the NetCDF file which contains
     * the file's Geometries' coordinates.
     */
    @XmlElement
    private VariableDescription lat;

    /**
     * Represents the Longitude Variable/Array in the NetCDF file which contains
     * the file's Geometries' coordinates.
     */
    @XmlElement
    private VariableDescription lon;

    /**
     * Represents the Variable/Attribute that containing the time of observation
     * for the Geometry item. This field also allows the user to provide an
     * epoch string, representing the epoch used by the datafile.
     */
    @XmlElement
    private DataTimeDescription dataTime;

    /**
     * Represents the file-defined epoch offset (# seconds between the UNIX
     * epoch "1970-01-01 00:00:00" and the epoch used in the file. This is
     * useful in calculating the records dataTime by default (rather than
     * relying on the user-overriden epoch defined by the "dataTime" field).
     */
    @XmlElement
    private AttributeDescription epoch;

    /**
     * Represents the Variable/Attribute/Value that determines the Geometry
     * Type(s) for this description.
     */
    @XmlElement
    private DelegateFieldDescription geomType;

    /**
     * Represents the Geometry Offset(s) for this description. That is, this
     * field represents the value or list of values that comprise the index(s)
     * into the lat/lon arrays, i.e. where each Geometry's coordinates begin.
     */
    @XmlElement
    private DelegateFieldDescription geomOffset;

    /**
     * Represents the number of elements (lat/lon pairs) in the corresponding
     * Geometry.
     */
    @XmlElement
    private DelegateFieldDescription geomNels;

    /**
     * Represents the list of Geometry-related attribute(s) to set for each
     * generated GeoDataRecord. Any given GeoDataRecord may have one or more
     * associated attributes, but a GeoDataRecord won't
     */
    @XmlElementWrapper(name = "atts")
    @XmlElement(name = "geomAtt")
    private List<DelegateFieldDescription> geomNInfos = new ArrayList<>();

    /**
     * Represents the Variable/Attribute or user-overridden value for the
     * product name/type.
     */
    @XmlElement
    private DelegateFieldDescription product;

    /**
     * Represents the Variable/Attribute or user-overridden value for the source
     * of the product.
     */
    @XmlElement
    private DelegateFieldDescription source;

    /**
     * Represents the NetCDF Variable that contains data flags for each Geometry
     * observation. The user may specify a NetCDF Variable name that serves as
     * the data mask, and the value that serves as the "nokeep". That is, any
     * Geometry with this value in its mask will not have a GeoDataRecord
     * created.
     */
    @XmlElement
    private DiscreteDataMaskDescription geomMask = null;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug
     *            the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the lat
     */
    public VariableDescription getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(VariableDescription lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public VariableDescription getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(VariableDescription lon) {
        this.lon = lon;
    }

    /**
     * @return the dataTime
     */
    public DataTimeDescription getDataTime() {
        return dataTime;
    }

    /**
     * @param dataTime
     *            the dataTime to set
     */
    public void setDataTime(DataTimeDescription dataTime) {
        this.dataTime = dataTime;
    }

    /**
     * @return the epoch
     */
    public AttributeDescription getEpoch() {
        return epoch;
    }

    /**
     * @param epoch
     *            the epoch to set
     */
    public void setEpoch(AttributeDescription epoch) {
        this.epoch = epoch;
    }

    /**
     * @return the geomType
     */
    public DelegateFieldDescription getGeomType() {
        return geomType;
    }

    /**
     * @param geomType
     *            the geomType to set
     */
    public void setGeomType(DelegateFieldDescription geomType) {
        this.geomType = geomType;
    }

    /**
     * @return the geomOffset
     */
    public DelegateFieldDescription getGeomOffset() {
        return geomOffset;
    }

    /**
     * @param geomOffset
     *            the geomOffset to set
     */
    public void setGeomOffset(DelegateFieldDescription geomOffset) {
        this.geomOffset = geomOffset;
    }

    /**
     * @return the geomNels
     */
    public DelegateFieldDescription getGeomNels() {
        return geomNels;
    }

    /**
     * @param geomNels
     *            the geomNels to set
     */
    public void setGeomNels(DelegateFieldDescription geomNels) {
        this.geomNels = geomNels;
    }

    /**
     * @return the geomNInfos
     */
    public List<DelegateFieldDescription> getGeomNInfos() {
        return geomNInfos;
    }

    /**
     * @param geomNInfos
     *            the geomNInfos to set
     */
    public void setGeomNInfos(List<DelegateFieldDescription> geomNInfos) {
        this.geomNInfos = geomNInfos;
    }

    /**
     * @return the product
     */
    public DelegateFieldDescription getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(DelegateFieldDescription product) {
        this.product = product;
    }

    /**
     * @return the source
     */
    public DelegateFieldDescription getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(DelegateFieldDescription source) {
        this.source = source;
    }

    /**
     * @return the geomMask
     */
    public DiscreteDataMaskDescription getGeomMask() {
        return geomMask;
    }

    /**
     * @param geomMask
     *            the geomMask to set
     */
    public void setGeomMask(DiscreteDataMaskDescription geomMask) {
        this.geomMask = geomMask;
    }

    /**
     * Test to ensure all the *necessary* fields for creating Geometries are
     * present in the file
     * 
     * @throws InvalidDescriptionException
     */
    public void validateDescription() throws InvalidDescriptionException {

        /*
         * Empty "product," "source," and "geomNInfos" can still generate
         * Geometries, so they are not grounds for failing a description.
         */
        this.lat.validate();
        this.lon.validate();
        this.dataTime.validate();
        this.geomType.validate();
        this.geomOffset.validate();
        this.geomNels.validate();
        if (this.name == null) {
            throw new InvalidDescriptionException(
                    "ProductDescriptor \"name\" cannot be null.");
        }

    }
}