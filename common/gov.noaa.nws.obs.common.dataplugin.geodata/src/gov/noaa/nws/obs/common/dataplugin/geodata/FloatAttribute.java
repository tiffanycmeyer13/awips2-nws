package gov.noaa.nws.obs.common.dataplugin.geodata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * FloatAttribute
 *
 * Class which describes a single piece of Geometry trivia that holds a float
 * value.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/25/2016     19064      jburks      Initial creation
 *
 * </pre>
 *
 * @author jason.burks
 * @version 1.0
 */

@Entity
@SequenceGenerator(initialValue = 1, name = FloatAttribute.ID_GEN, sequenceName = "geodataflattseq")
@Table(name = "geodata_att_float")
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class FloatAttribute {

    public static final String ID_GEN = "idgen";

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_GEN)
    @Id
    @DynamicSerializeElement
    protected int id;

    @Column
    @DynamicSerializeElement
    @XmlElement
    protected String name;

    @Column
    @DynamicSerializeElement
    @XmlElement
    protected float value;

    @ManyToOne
    @JoinColumn(name = "geodatarecord_id")
    protected GeoDataRecord record;

    /**
     * Default constructor
     */
    public FloatAttribute() {

    }

    /**
     * Constructor setting name and value of Attribute.
     *
     * @param name
     *            Name of the Attribute.
     * @param value
     *            Value of the Attribute.
     */
    public FloatAttribute(String name, float value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructor setting name, value and GeoDataRecord.
     *
     * @param name
     *            Name of the Attribute.
     * @param value
     *            Value of the Attribute.
     * @param record
     *            GeoDataRecord this Attribute belongs to.
     */
    public FloatAttribute(String name, float value, GeoDataRecord record) {
        this.name = name;
        this.value = value;
        this.record = record;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

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
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(float value) {
        this.value = value;
    }

    /**
     * @return the record
     */
    public GeoDataRecord getRecord() {
        return record;
    }

    /**
     * @param record
     *            the record to set
     */
    public void setRecord(GeoDataRecord record) {
        this.record = record;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Float.floatToIntBits(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        FloatAttribute other = (FloatAttribute) obj;

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "FloatAttribute [name : \"" + this.getName() + "\", value : \""
                + String.valueOf(this.getValue()) + "\" ]";
    }

}
