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
 * IntegerAttribute
 *
 * Class which describes a single piece of Geometry trivia that holds an integer
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
@SequenceGenerator(initialValue = 1, name = IntegerAttribute.ID_GEN, sequenceName = "geodataintattseq")
@Table(name = "geodata_att_integer")
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class IntegerAttribute {

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
    protected int value;

    @ManyToOne
    @JoinColumn(name = "geodatarecord_id")
    protected GeoDataRecord record;

    /**
     * Default constructor.
     */
    public IntegerAttribute() {

    }

    /**
     * Constructor setting name and value of Attribute.
     *
     * @param name
     *            Name of the Attribute.
     * @param value
     *            Value of the Attribute.
     */
    public IntegerAttribute(String name, int value) {
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
    public IntegerAttribute(String name, int value, GeoDataRecord record) {
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
    public int getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(int value) {
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
        result = prime * result + value;
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

        IntegerAttribute other = (IntegerAttribute) obj;

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (value != other.value) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "IntegerAttribute [name : \"" + this.getName() + "\", value : \""
                + String.valueOf(this.getValue()) + "\" ]";
    }

}
