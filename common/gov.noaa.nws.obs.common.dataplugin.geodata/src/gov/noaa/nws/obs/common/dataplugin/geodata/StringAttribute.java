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
 * StringAttribute
 *
 * Class which describes a single piece of Geometry trivia that holds a string
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
@SequenceGenerator(initialValue = 1, name = StringAttribute.ID_GEN, sequenceName = "geodatastattseq")
@Table(name = "geodata_att_string")
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class StringAttribute {

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
    protected String value;

    @ManyToOne
    @JoinColumn(name = "geodatarecord_id")
    protected GeoDataRecord record;

    /**
     * Default constructor.
     */
    public StringAttribute() {

    }

    /**
     * Constructor setting name and value.
     *
     * @param name
     *            Name of the Attribute.
     * @param value
     *            Value of the Attribute.
     */
    public StringAttribute(String name, String value) {
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
    public StringAttribute(String name, String value, GeoDataRecord record) {
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
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
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
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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

        StringAttribute other = (StringAttribute) obj;

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "StringAttribute [name : \"" + this.getName() + "\", value : \""
                + this.getValue() + "\" ]";
    }

}
