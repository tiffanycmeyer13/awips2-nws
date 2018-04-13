/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc.checker;

import java.util.HashMap;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.CheckDataType;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.CheckResult;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.DataQualityCheckTriple;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.FieldTypeAndValue;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.QCOperator;

/**
 * DailyClimateDataQualityChecker
 * 
 * All DSM parameters defined in the qcparams.properties will be handled by this
 * checker
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

public class DailyClimateDataQualityChecker extends ClimateDataQualityChecker {

    /*
     * Mapping a DSM table column name to DailyClimateData getter and missing
     * values.
     */
    private static final Map<String, String[]> PARAM_ATTRIBUTE_MAP = new HashMap<>();

    // Supported daily parameters
    static {
        PARAM_ATTRIBUTE_MAP.put("maxtemp_cal", new String[] { "maxTemp",
                String.valueOf(ParameterFormatClimate.MISSING) });
        PARAM_ATTRIBUTE_MAP.put("mintemp_cal", new String[] { "minTemp",
                String.valueOf(ParameterFormatClimate.MISSING) });
        PARAM_ATTRIBUTE_MAP.put("min_press", new String[] { "minSlp",
                String.valueOf(ParameterFormatClimate.MISSING_SLP) });
        PARAM_ATTRIBUTE_MAP.put("equiv_water", new String[] { "precip",
                String.valueOf(ParameterFormatClimate.MISSING_PRECIP) });

        PARAM_ATTRIBUTE_MAP.put("twomin_wspd", new String[] { "avgWindSpeed",
                String.valueOf(ParameterFormatClimate.MISSING_SPEED) });
        PARAM_ATTRIBUTE_MAP.put("max2min_wdir", new String[] { "maxWind",
                String.valueOf(ParameterFormatClimate.MISSING) });
        PARAM_ATTRIBUTE_MAP.put("max2min_wspd", new String[] { "maxWind",
                String.valueOf(ParameterFormatClimate.MISSING_SPEED) });
        PARAM_ATTRIBUTE_MAP.put("pkwnd_dir", new String[] { "maxGust",
                String.valueOf(ParameterFormatClimate.MISSING) });
        PARAM_ATTRIBUTE_MAP.put("pkwnd_spd", new String[] { "maxGust",
                String.valueOf(ParameterFormatClimate.MISSING_SPEED) });

        PARAM_ATTRIBUTE_MAP.put("min_sun", new String[] { "minutesSun",
                String.valueOf(ParameterFormatClimate.MISSING) });
        PARAM_ATTRIBUTE_MAP.put("percent_sun", new String[] { "percentPossSun",
                String.valueOf(ParameterFormatClimate.MISSING) });
        PARAM_ATTRIBUTE_MAP.put("solid_precip", new String[] { "snowDay",
                String.valueOf(ParameterFormatClimate.MISSING_SNOW) });
        PARAM_ATTRIBUTE_MAP.put("snowdepth", new String[] { "snowGround",
                String.valueOf(ParameterFormatClimate.MISSING_SNOW) });
        PARAM_ATTRIBUTE_MAP.put("avg_sky_cover", new String[] { "skyCover",
                String.valueOf(ParameterFormatClimate.MISSING) });
    }

    /**
     * Constructor
     */
    public DailyClimateDataQualityChecker() {
        super();
    }

    @Override
    public CheckResult check(ClimateRunData data) throws Exception {
        CheckResult cresult = new CheckResult();

        Map<Integer, ClimateDailyReportData> reportData = ((ClimateRunDailyData) data)
                .getReportMap();
        // Check each stations's data
        for (ClimateDailyReportData entry : reportData.values()) {
            boolean checkPassed = checkDefinedParameters(entry, cresult);

            if (!checkPassed) {
                logger.debug("Some Climate QC checks did not pass: "
                        + cresult.getDetails());
                /*
                 * Currently set to stop checking on first failed station,
                 * though user requirements may expand to show all stations with
                 * failed QC checks
                 */
                break;
            }
        }

        return cresult;
    }

    /**
     * checkDefinedParameters
     * 
     * Check each defined parameter against given ClimateDailyReportData
     * 
     * @param report
     * @return true if passed all checks
     */
    private boolean checkDefinedParameters(ClimateDailyReportData report,
            CheckResult cresult) throws Exception {
        DailyClimateData data = report.getData();
        Station station = report.getStation();

        // Check each defined QC param on the report data
        for (DataQualityCheckTriple qcInfo : checkList) {
            boolean currCheckPass = true;

            if (qcInfo.getParamName() == null
                    || qcInfo.getParamName().isEmpty()) {
                // No parameter defined, ignore to check
                String msg = "Undefined parameter [" + qcInfo.getParamName()
                        + "] is not able to be checked for station ["
                        + station.getStationName() + "].";
                logger.warn(msg);
                cresult.addDetail(msg);
                continue;
            }

            QCOperator op = qcInfo.getCheckOp();
            Object checkValue = null;
            if (op == QCOperator.GT || op == QCOperator.LT) {
                checkValue = qcInfo.getParamValue();
            }

            switch (op) {
            case M:
                currCheckPass = isMissing(data, station, qcInfo.getParamName(),
                        cresult) ? false : true;
                break;
            case GT:
                if (!isMissing(data, station, qcInfo.getParamName(),
                        new CheckResult())) {
                    currCheckPass = isGreaterThan(data, station,
                            qcInfo.getParamName(), checkValue, cresult) ? false
                                    : true;
                } else {
                    String msg = "Parameter [" + qcInfo.getParamName()
                            + "] for station [" + station.getStationName()
                            + "] will not be checked for [>"
                            + checkValue.toString()
                            + "] as the parameter is missing.";

                    cresult.addDetail(msg);
                    logger.debug(msg);
                }
                break;
            case LT:
                if (!isMissing(data, station, qcInfo.getParamName(),
                        new CheckResult())) {
                    currCheckPass = isLessThan(data, station,
                            qcInfo.getParamName(), checkValue, cresult) ? false
                                    : true;
                } else {
                    String msg = "Parameter [" + qcInfo.getParamName()
                            + "] for station [" + station.getStationName()
                            + "] will not be checked for [<"
                            + checkValue.toString()
                            + "] as the parameter is missing.";

                    cresult.addDetail(msg);
                    logger.debug(msg);
                }
                break;
            default:
                String msg = "Unknown operand for parameter ["
                        + qcInfo.getParamName()
                        + "] will not be checked for station ["
                        + station.getStationName() + "]";
                cresult.addDetail(msg);
                break;
            }

            /*
             * Currently break out after first failed check, but
             * user-requirements may expand later to show all failed checks for
             * a station.
             */
            if (!currCheckPass) {
                return false;
            }
        }

        return true;
    }

    /**
     * getWindValue
     * 
     * Get real type and value for Wind parameters, including: twomin_wspd,
     * max2min_wspd, pkwnd_dir, pkwnd_spd
     * 
     * @param checkParamName
     * @param ftv
     */
    private void getWindValue(String checkParamName, FieldTypeAndValue ftv) {
        ClimateWind wind = (ClimateWind) ftv.getValue();
        String ds = checkParamName.substring(checkParamName.length() - 3);
        if (ds.equalsIgnoreCase("dir")) {
            ftv.setValue(wind.getDir());
            ftv.setType(CheckDataType.INT);
        } else if (ds.equalsIgnoreCase("spd")) {
            ftv.setValue(wind.getSpeed());
            ftv.setType(CheckDataType.FLOAT);
        }
    }

    /**
     * check if given param is missing
     * 
     * @param data
     * @param station
     * @param checkParam
     * @param cresult
     * @return
     * @throws Exception
     */
    private boolean isMissing(DailyClimateData data, Station station,
            String checkParamName, CheckResult cresult) throws Exception {
        boolean missing = false;
        FieldTypeAndValue value = null;

        String checkFieldName = PARAM_ATTRIBUTE_MAP.get(checkParamName)[0];

        try {
            value = this.getFieldValue(data, checkFieldName);
        } catch (Exception e) {
            throw new Exception("Failed to get value for the attribute: "
                    + checkParamName + " from DailyClimateData");
        }

        /*
         * Wind dir and speed are encapsulated in ClimateWind
         * 
         * Following call extracted their real type and value
         */
        if (value.getType() == CheckDataType.WINDOBJ) {
            getWindValue(checkParamName, value);
        }

        if (PARAM_ATTRIBUTE_MAP.get(checkParamName)[1]
                .equals(value.getValue().toString())) {
            String msg = "The parameter [" + checkParamName
                    + "] is missing for station [" + station.getStationName()
                    + "]";
            cresult.setPassed(false);
            cresult.addDetail(msg);
            missing = true;
        }
        return missing;
    }

    /**
     * isGreaterThan
     * 
     * @param data
     * @param station
     * @param checkParam
     * @param checkValue
     * @return
     * @throws Exception
     */
    private boolean isGreaterThan(DailyClimateData data, Station station,
            String checkParamName, Object checkValue, CheckResult cresult)
                    throws Exception {

        boolean greaterThan = false;
        String checkFieldName = PARAM_ATTRIBUTE_MAP.get(checkParamName)[0];
        FieldTypeAndValue tv = getFieldValue(data, checkFieldName);

        if (tv.getType() == CheckDataType.UNKNOWN || tv.getValue() == null) {
            throw new Exception("Data Type of the parameter " + checkParamName
                    + " is not supported for greater-than check");
        }

        /*
         * Wind dir and speed are encapsulated in ClimateWind Following call
         * extracted their real type and value
         */
        if (tv.getType() == CheckDataType.WINDOBJ) {
            getWindValue(checkParamName, tv);
        }

        String msg = "The value of [" + checkParamName + "] is greater than ";

        switch (tv.getType()) {
        case SHORT:
        case INT:
            int iv = (int) tv.getValue();
            int iV = Integer.valueOf((String) checkValue);
            if (iv > iV) {
                greaterThan = true;
                msg = msg + iV;
                cresult.setPassed(false);
            }
            break;
        case LONG:
            long lv = (long) tv.getValue();
            long lV = Long.valueOf((String) checkValue);
            if (lv > lV) {
                greaterThan = true;
                msg = msg + lV;
                cresult.setPassed(false);
            }
            break;
        case FLOAT:
            float fv = (float) tv.getValue();
            float fV = Float.valueOf((String) checkValue);
            if (fv > fV) {
                greaterThan = true;
                msg = msg + fV;
                cresult.setPassed(false);
            }
            break;
        case DOUBLE:
            double dv = (double) tv.getValue();
            double dV = Double.valueOf((String) checkValue);
            if (dv > dV) {
                greaterThan = true;
                msg = msg + dV;
                cresult.setPassed(false);

            }
            break;
        default:
            break;
        }

        if (greaterThan) {
            cresult.addDetail(
                    msg + " for station [" + station.getStationName() + "]");
        }

        return greaterThan;
    }

    /**
     * isLessThan
     * 
     * @param data
     * @param station
     * @param checkParamName
     * @param checkValue
     * @param cresult:
     *            a reference of CheckResult object,
     * @return
     * @throws Exception
     */
    private boolean isLessThan(DailyClimateData data, Station station,
            String checkParamName, Object checkValue, CheckResult cresult)
                    throws Exception {

        boolean lessThan = false;
        String checkFieldName = PARAM_ATTRIBUTE_MAP.get(checkParamName)[0];
        FieldTypeAndValue tv = getFieldValue(data, checkFieldName);

        if (tv.getType() == CheckDataType.UNKNOWN || tv.getValue() == null) {
            throw new Exception("Data Type of the parameter " + checkParamName
                    + " is not supported for less-than check");
        }

        /*
         * Wind dir and speed are encapsulated in ClimateWind Following call
         * extracted their real type and value
         */
        if (tv.getType() == CheckDataType.WINDOBJ) {
            getWindValue(checkParamName, tv);
        }

        String msg = "The value of [" + checkParamName + "] is less than ";
        switch (tv.getType()) {
        case SHORT:
        case INT:
            int iv = (int) tv.getValue();
            int iV = Integer.valueOf((String) checkValue);
            if (iv < iV) {
                lessThan = true;
                msg = msg + iV;
                cresult.setPassed(false);
            }
            break;
        case LONG:
            long lv = (long) tv.getValue();
            long lV = Long.valueOf((String) checkValue);
            if (lv < lV) {
                lessThan = true;
                msg = msg + lV;
                cresult.setPassed(false);
            }
            break;
        case FLOAT:
            float fv = (float) tv.getValue();
            float fV = Float.valueOf((String) checkValue);
            if (fv < fV) {
                lessThan = true;
                msg = msg + fV;
                cresult.setPassed(false);
            }
            break;
        case DOUBLE:
            double dv = (double) tv.getValue();
            double dV = Double.valueOf((String) checkValue);
            if (dv < dV) {
                lessThan = true;
                msg = msg + dV;
                cresult.setPassed(false);
            }
            break;
        default:
            break;
        }

        if (lessThan) {
            cresult.addDetail(
                    msg + " for station [" + station.getStationName() + "]");
        }

        return lessThan;
    }
}
