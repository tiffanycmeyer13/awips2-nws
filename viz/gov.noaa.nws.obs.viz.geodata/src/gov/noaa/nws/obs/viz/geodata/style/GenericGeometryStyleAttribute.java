package gov.noaa.nws.obs.viz.geodata.style;

import java.text.ParsePosition;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.units.UnitConv;

import tech.units.indriya.format.SimpleUnitFormat;

/**
 * GenericGeometryAttribute
 *
 * Class which defines the fields and methods necessary for matching a
 * GeoDataRecord's Attribute(s) to mappings provided in the StyleRules. These
 * allow the user to convert the Attribute's value (which is stored in the
 * Postgres DB) into the appropriate units, as well as transform the Attribute's
 * name to one that the user prefers (for sampling data).
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08/20/2016     19064    mcomerford   Initial creation (DCS 19064)
 *
 * </pre>
 *
 * @author matt.comerford
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
public class GenericGeometryStyleAttribute {

    @XmlElement
    String dataName;

    @XmlElement
    String dataUnits;

    @XmlElement
    String displayName;

    @XmlElement
    String displayUnits = "";

    /**
     * Default constructor.
     */
    public GenericGeometryStyleAttribute() {

    }

    /**
     * @return the dataName
     */
    public String getDataName() {
        return dataName;
    }

    /**
     * @param dataName
     *            the dataName to set
     */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the displayUnits
     */
    public String getDisplayUnits() {
        return displayUnits;
    }

    /**
     * @param displayUnits
     *            the displayUnits to set
     */
    public void setDisplayUnits(String displayUnits) {
        this.displayUnits = displayUnits;
    }

    /**
     * @return the dataUnits
     */
    public String getDataUnits() {
        return dataUnits;
    }

    /**
     * @param dataUnits
     *            the dataUnits to set
     */
    public void setDataUnits(String dataUnits) {
        this.dataUnits = dataUnits;
    }

    /**
     * Convert a value from the dataUnit to the displayUnit
     * 
     * @param value
     *            The value in its original units.
     * @return The converted value in its new units.
     */
    public double convertValue(double value) {

        /* No conversion if units aren't specified. */
        if (this.dataUnits == null || this.displayUnits == null) {
            return value;
        }
        Unit<?> convertFrom = SimpleUnitFormat.getInstance(SimpleUnitFormat.Flavor.ASCII)
                .parseObject(this.dataUnits, new ParsePosition(0));
        Unit<?> convertTo = SimpleUnitFormat.getInstance(SimpleUnitFormat.Flavor.ASCII)
                .parseObject(this.displayUnits, new ParsePosition(0));
        UnitConverter converter = UnitConv.getConverterToUnchecked(convertFrom,
                convertTo);

        return converter.convert(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataName == null) ? 0 : dataName.hashCode());
        result = prime * result
                + ((dataUnits == null) ? 0 : dataUnits.hashCode());
        result = prime * result
                + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result
                + ((displayUnits == null) ? 0 : displayUnits.hashCode());

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
        if (getClass() != obj.getClass()) {
            return false;
        }

        GenericGeometryStyleAttribute other = (GenericGeometryStyleAttribute) obj;

        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (displayUnits == null) {
            if (other.displayUnits != null) {
                return false;
            }
        } else if (!displayUnits.equals(other.displayUnits)) {
            return false;
        }
        if (dataName == null) {
            if (other.dataName != null) {
                return false;
            }
        } else if (!dataName.equals(other.dataName)) {
            return false;
        }
        if (dataUnits == null) {
            if (other.dataUnits != null) {
                return false;
            }
        } else if (!dataUnits.equals(other.dataUnits)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "GenericGeometryAttribute [dataName : \"" + dataName
                + "\", dataUnits : \"" + dataUnits + "\", displayName : \""
                + displayName + "\", displayUnits : \"" + displayUnits + "\" ]";
    }

}
