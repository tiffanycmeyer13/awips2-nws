package gov.noaa.nws.obs.edex.plugin.geodata.description;

import com.raytheon.uf.edex.netcdf.decoder.util.NetcdfDecoderUtils;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

/**
 * VariableDescriptor
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
 * 08/20/2016     19064     mcomerford  Initial creation (DCS 19064)
 *
 * </pre>
 *
 * @author matt.comerford
 * @version 1.0
 */

public class VariableDescriptor {

    /* The (potential) Array corresponding to the NetCDF Variable. */
    private Array array = null;

    /* The _FillValue corresponding to the NetCDF Variable. */
    private Number fillValue;

    /*
     * The offset value to add to each value in the Array (or the default
     * value).
     */
    private Number addOffset;

    /*
     * The scale factory by which to scale each value in the Array (or the
     * default value).
     */
    private Number scaleFactor;

    /* The NetCDF name of the corresponding Variable */
    private String name = null;

    /*
     * The (optional) default value (to set if we are working with a constant
     * value).
     */
    private Object defaultValue = null;

    /* The DataType of the NetCDF Variable. */
    private DataType dType;

    public VariableDescriptor(Variable variable, String name) {
        super();
        this.fillValue = NetcdfDecoderUtils.getNoDataValue(variable,
                Double.NaN);
        this.addOffset = NetcdfDecoderUtils.getAddOffset(variable);
        this.scaleFactor = NetcdfDecoderUtils.getScaleFactor(variable);
        this.dType = variable.getDataType();
        this.name = name;
    }

    public VariableDescriptor(String name) {
        super();
        this.name = name;
    }

    /**
     * @return the fillValue
     */
    public Number getFillValue() {
        return fillValue;
    }

    /**
     * @param fillValue
     *            the fillValue to set
     */
    public void setFillValue(Number fillValue) {
        this.fillValue = fillValue;
    }

    /**
     * @return the addOffset
     */
    public Number getAddOffset() {
        return addOffset;
    }

    /**
     * @param addOffset
     *            the addOffset to set
     */
    public void setAddOffset(Number addOffset) {
        this.addOffset = addOffset;
    }

    /**
     * @return the scaleFactor
     */
    public Number getScaleFactor() {
        return scaleFactor;
    }

    /**
     * @param scaleFactor
     *            the scaleFactor to set
     */
    public void setScaleFactor(Number scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    /**
     * @return the array
     */
    public Array getArray() {
        return array;
    }

    /**
     * @param array
     *            the array to set
     */
    public void setArray(Array array) {
        this.array = array;
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
     * @return the defaultValue
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the dType
     */
    public DataType getdType() {
        return dType;
    }

    /**
     * @param dType
     *            the dType to set
     */
    public void setdType(DataType dType) {
        this.dType = dType;
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

        VariableDescriptor other = (VariableDescriptor) obj;

        if (this.getArray() == null) {
            if (other.getArray() != null) {
                return false;
            }
        } else if (!this.getArray().equals(other.getArray())) {
            return true;
        }

        if (this.getFillValue() == null) {
            if (other.getFillValue() != null) {
                return false;
            }
        } else if (!this.getFillValue().equals(other.getFillValue())) {
            return false;
        }

        if (this.getAddOffset() == null) {
            if (other.getAddOffset() != null) {
                return false;
            }
        } else if (!this.getAddOffset().equals(other.getAddOffset())) {
            return false;
        }

        if (this.getScaleFactor() == null) {
            if (other.getScaleFactor() != null) {
                return false;
            }
        } else if (!this.getScaleFactor().equals(other.getScaleFactor())) {
            return false;
        }

        if (this.getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!this.getName().equals(other.getName())) {
            return false;
        }

        if (this.getDefaultValue() == null) {
            if (other.getDefaultValue() != null) {
                return false;
            }
        } else if (!this.getDefaultValue().equals(other.getDefaultValue())) {
            return false;
        }

        if (this.getdType() == null) {
            if (other.getdType() != null) {
                return false;
            }
        } else if (!this.getdType().equals(other.getdType())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        String string = "VariableDescriptor\n\tname : \"" + name
                + "\",\n\tdatatype : \"" + dType.toString()
                + "\",\n\tdefaultVal : \"" + String.valueOf(defaultValue)
                + "\",\n\tfillValue : \""
                + String.valueOf(fillValue.floatValue())
                + "\",\n\taddOffset : \""
                + String.valueOf(addOffset.floatValue())
                + "\",\n\tscaleFactor : \""
                + String.valueOf(scaleFactor.floatValue()) + "\",\n\tArray : \""
                + array.toString() + "\"";
        return string;
    }
}
