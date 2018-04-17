/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc.checker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.CheckDataType;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.CheckResult;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.DataQualityCheckTriple;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.FieldTypeAndValue;

/**
 * ClimateDataQualityChecker An abstract class for Daily, Monthly (currently not
 * supported) QC checkers
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2017  35729      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public abstract class ClimateDataQualityChecker {

    /** The logger */
    protected final IUFStatusHandler logger = UFStatus.getHandler(getClass());

    protected List<DataQualityCheckTriple> checkList = new ArrayList<>();

    /**
     * Empty constructor
     */
    public ClimateDataQualityChecker() {
    }

    // child class must override this method
    public abstract CheckResult check(ClimateRunData data) throws Exception;

    /**
     * @return the checkList
     */
    public List<DataQualityCheckTriple> getCheckList() {
        return checkList;
    }

    /**
     * @param checkList
     *            the checkList to set
     */
    public void setCheckList(List<DataQualityCheckTriple> checkList) {
        this.checkList = checkList;
    }

    /**
     * addOneDataQualityCheckTriple
     * 
     * @param dqc
     */
    public void addOneDataQualityCheckTriple(DataQualityCheckTriple dqc) {
        checkList.add(dqc);
    }

    /**
     * getFieldValue get value object by given field name
     * 
     * @param obj
     * @param name
     * @return
     * @throws Exception
     */
    protected FieldTypeAndValue getFieldValue(Object obj, String name)
            throws Exception {

        FieldTypeAndValue tv = new FieldTypeAndValue();
        Object rval = null;
        Field f = null;

        String method = Character.toUpperCase(name.charAt(0))
                + name.substring(1);
        Class<?> clazz = obj.getClass();
        try {
            Method m;
            try {
                f = clazz.getDeclaredField(name);
                // Try common 'get' first...
                m = clazz.getMethod("get" + method);
            } catch (NoSuchMethodException e) {
                // Try 'is' as a prefix
                m = clazz.getMethod("is" + method);
            }

            rval = m.invoke(obj);
        } catch (Exception e) {
            throw new Exception(e);
        }

        if (f != null) {
            tv.setType(getFieldDataType(f));
            tv.setValue(rval);
        }

        return tv;
    }

    /**
     * Map the filed type to enum CheckDataType
     * 
     * @param f
     * @return
     */
    private CheckDataType getFieldDataType(Field f) {

        CheckDataType type = CheckDataType.UNKNOWN;

        String typeName = f.getType().getSimpleName();
        if (typeName.equalsIgnoreCase("Short")) {
            type = CheckDataType.SHORT;
        } else if (typeName.equalsIgnoreCase("Integer")
                || typeName.equalsIgnoreCase("int")) {
            type = CheckDataType.INT;
        } else if (typeName.equalsIgnoreCase("Long")) {
            type = CheckDataType.LONG;
        } else if (typeName.equalsIgnoreCase("Float")) {
            type = CheckDataType.FLOAT;
        } else if (typeName.equalsIgnoreCase("Double")) {
            type = CheckDataType.DOUBLE;
        } else if (typeName.equalsIgnoreCase("ClimateWind")) {
            type = CheckDataType.WINDOBJ;
        }

        return type;
    }

}
