package gov.noaa.nws.obs.common.dataplugin.geodata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * GeoDataRecord
 *
 * Class that defines/describes a single record in the postgres database. Also
 * provides mappings to database tables for Float, Integer, and String
 * attributes of a given GeoDataRecord.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/25/2016     19064      jburks    Initial creation (DCS 19064)
 * 08/20/2016     19064    mcomerford  Added method to return all Attribute names
 *
 * </pre>
 *
 * @author jason.burks
 * @version 1.0
 */

@Entity
@SequenceGenerator(initialValue = 1, name = PersistablePluginDataObject.ID_GEN, sequenceName = "geodbSeq")
@Table(name = GeoDataRecord.PLUGIN_NAME, uniqueConstraints = {
        @UniqueConstraint(columnNames = { "refTime", "source", "product",
                "geometry" }) })
@org.hibernate.annotations.Table(appliesTo = GeoDataRecord.PLUGIN_NAME, indexes = {
        @Index(name = "geodb_refTimeIndex", columnNames = { "refTime",
                "forecastTime" }) })
@DynamicSerialize
public class GeoDataRecord extends PersistablePluginDataObject {

    private static final long serialVersionUID = 1L;

    protected static final String PLUGIN_NAME = "geodata";

    @Column
    @DynamicSerializeElement
    @DataURI(position = 1)
    private String source;

    @Column
    @DynamicSerializeElement
    @DataURI(position = 2)
    private String product;

    @Column(name = "geometry", columnDefinition = "geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DataURI(position = 3)
    @DynamicSerializeElement
    private Geometry geometry;

    @OneToMany(mappedBy = "record", targetEntity = StringAttribute.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElement
    @DynamicSerializeElement
    private Set<StringAttribute> stringAtt = new HashSet<>();

    @OneToMany(mappedBy = "record", targetEntity = FloatAttribute.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElement
    @DynamicSerializeElement
    private Set<FloatAttribute> floatAtt = new HashSet<>();

    @OneToMany(mappedBy = "record", targetEntity = IntegerAttribute.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElement
    @DynamicSerializeElement
    private Set<IntegerAttribute> integerAtt = new HashSet<>();

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
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @param geometry
     *            the geometry to set
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * @return the stringAtt
     */
    public Set<StringAttribute> getStringAtt() {
        return stringAtt;
    }

    /**
     * @param stringAtt
     *            the stringAtt to set
     */
    public void setStringAtt(Set<StringAttribute> stringAtt) {
        this.stringAtt = stringAtt;
    }

    /**
     * @return the floatAtt
     */
    public Set<FloatAttribute> getFloatAtt() {
        return floatAtt;
    }

    /**
     * @param floatAtt
     *            the floatAtt to set
     */
    public void setFloatAtt(Set<FloatAttribute> floatAtt) {
        this.floatAtt = floatAtt;
    }

    /**
     * @return the integerAtt
     */
    public Set<IntegerAttribute> getIntegerAtt() {
        return integerAtt;
    }

    /**
     * @param integerAtt
     *            the integerAtt to set
     */
    public void setIntegerAtt(Set<IntegerAttribute> integerAtt) {
        this.integerAtt = integerAtt;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    /**
     * Return a list of the Attribute(s) names that correspond to this
     * GeoDataRecord.
     *
     * @return The list of Attribute names (String)
     */
    public List<String> getAttNames() {

        List<String> names = new ArrayList<String>();
        for (IntegerAttribute intAtt : getIntegerAtt()) {
            names.add(intAtt.getName());
        }
        for (FloatAttribute floatAtt : getFloatAtt()) {
            names.add(floatAtt.getName());
        }
        for (StringAttribute stringAtt : getStringAtt()) {
            names.add(stringAtt.getName());
        }

        return names;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime * result + ((product == null) ? 0 : product.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        GeoDataRecord other = (GeoDataRecord) obj;

        if (geometry == null) {
            if (other.geometry != null) {
                return false;
            }
        } else if (!geometry.equals(other.geometry)) {
            return false;
        }
        if (product == null) {
            if (other.product != null) {
                return false;
            }
        } else if (!product.equals(other.product)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }

        return true;
    }

}
