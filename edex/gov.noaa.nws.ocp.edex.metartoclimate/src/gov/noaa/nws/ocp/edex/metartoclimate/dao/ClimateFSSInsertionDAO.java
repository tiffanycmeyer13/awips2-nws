/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAOValues;
import gov.noaa.nws.ocp.edex.common.climate.util.MetarUtils;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.QCMetar;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.RecentWx;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.SurfaceObs;

/**
 * Implementations converted from retrieve_OBS.C and related files. Insert Fixed
 * Surface Station/Flight Service Station data based on a METAR report.
 * 
 * METAR decoding resources referenced in addition to consulting Legacy code:
 * 
 * <pre>
 * http://www.moratech.com/aviation/metar-class/metar-pg13-rmk.html
 * http://www.met.tamu.edu/class/metar/quick-metar.html
 * https://math.la.asu.edu/~eric/workshop/METAR.html
 * https://www.aviationweather.gov/static/help/taf-decode.php
 * http://chesapeakesportpilot.com/wp-content/uploads/2015/03/military_wx_codes.pdf
 * http://meteocentre.com/doc/metar.html
 * http://www.nws.noaa.gov/om/forms/resources/SFCTraining.pdf
 * </pre>
 * 
 * In Legacy, Supplemental Climatological Data (SCD) reports were also queried
 * for to be placed into FSS tables. However, according to
 * (https://vlab.ncep.noaa.gov/documents/584952/600396/AWP.RLSN.OB16.2.1_Final.
 * pdf/) (search for "SCD") these products were discontinued 3 years prior to
 * that document (so in 2013).
 * 
 * No work will be done with SCD reports.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 10 FEB 2017  28609      amoore      Initial creation.
 * 21 FEB 2017  28609      amoore      Bug fixes from testing. Better logging.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 25 APR 2017  33104      amoore      Logging cleanup.
 * 02 MAY 2017  33104      amoore      Refactor queries into constants. Use query maps.
 *                                     Address FindBugs.
 * 09 MAY 2017  33104      amoore      If can't find station ID for station code, log
 *                                     warning and return, don't throw an exception.
 * 11 MAY 2017  33104      amoore      Parameterization. Add TODOs.
 * 23 MAY 2017  33104      amoore      Fix casting for report ID counter.
 * 24 MAY 2017  33104      amoore      Fix null pointer for present weather elements array access.
 * 24 MAY 2017  33104      amoore      Fix bad parameter name.
 * 24 MAY 2017  33104      amoore      Fix wrong table name.
 * 24 MAY 2017  33104      amoore      Fix latent Legacy bug with violating DB keys on duplicate reports.
 * 24 MAY 2017  33104      amoore      Safer use of sequence ID for fss_report.
 * 25 MAY 2017  33104      amoore      Lightning section does not need frequency prefix or a specific type.
 *                                     Added "W-NE" as a valid direction. Added more comments.
 * 25 MAY 2017  33104      amoore      Workaround for sequence ID for fss_report. Get ID separately prior
 *                                     to insertion. Fix Recent Weather regex's after testing a report.
 *                                     Weather time groupings may have begin and end time together.
 * 02 JUN 2017  33104      amoore      Re-arrange remarks checking to check for simple flags first. Some
 *                                     flags share prefix characters with more complex sections, so it is
 *                                     better to rule out the flags first than to get an error parsing
 *                                     a false-positive for a complex part.
 * 07 JUN 2017  33104      amoore      Add "SE-SW" valid direction. Lightning location is optional.
 *                                     Add "TS" as valid weather type. Weather type string could start
 *                                     with end time and end with begin time.
 * 13 JUN 2017  33104      amoore      Add "UP" valid weather type, and compare weather types from
 *                                     http://weather.cod.edu/notes/metar.html. Remove duplicates
 *                                     weather types. Adjust RVR regex. Recent weather can have more
 *                                     than two times after each weather marker. Variable visibility
 *                                     can start with a fraction and end with a whole number.
 * 19 JUN 2017  33104      amoore      Downgrade many missing main body items to warning. Make direction 
 *                                     able to be any combination of cardinal directions separated by 
 *                                     hyphens.
 * 07 JUL 2017  33104      amoore      Split class.
 * 24 JUL 2017  33104      amoore      Use 24-hour time.
 * 07 SEP 2017  37754      amoore      Exceptions instead of boolean returns. Get Hydromet IDs on
 *                                     construction. Minor review comments. Split decoding logic into
 *                                     separate class.
 * 08 SEP 2017  37809      amoore      For queries, cast to Number rather than specific number type.
 * 31 OCT 2017  38077      amoore      Fix missing weather issues.
 * 02 NOV 2017  37755      amoore      Peak wind speed was missing from final storage, post-decoding,
 *                                     when checking for different hydromet IDs.
 * 26 APR 2019  DR 21195   dfriedman   Handle both special case precipitation values.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateFSSInsertionDAO extends ClimateDAO {

    /**
     * Report type.
     */
    private static final String FSS_REPORT_TYPE = "MTR";

    /**
     * Special report subtype.
     */
    private static final String SPECIAL_SUBTYPE = "SPECI";

    /**
     * METAR report subtype.
     */
    private static final String METAR_SUBTYPE = "MTR";

    /**
     * Manned station flag.
     */
    private static final String MANNED_STATION_AUGMENTATION = "MAN";

    /**
     * Query for all METAR weather values.
     */
    private static final String METAR_QX_QUERY = "SELECT element_value, wx_full_string from"
            + " boolean_values where element_id = " + MetarUtils.METAR_WX;

    /**
     * Query for all METAR Hydromet IDs.
     */
    private static final String METAR_HYDROMET_ID_QUERY = "SELECT element_id FROM hydromet_element"
            + " WHERE product_name = 'MTR'";

    /**
     * FSS time zone ID.
     */
    private static final String FSS_TIME_ZONE_ID = "UT";

    /**
     * Variable wind direction value.
     */
    private static final int VARIABLE_WIND_DIR_VALUE = 99;

    /**
     * METAR wind direction value scale.
     */
    private static final int METAR_WIND_DIR_SCALE = 1;

    /**
     * METAR 3-hour pressure tendency value scale.
     */
    private static final int METAR_3HR_PRESS_TEND_SCALE = 1;

    /**
     * METAR horizontal visibility value scale. For data insertion.
     */
    private static final int METAR_HORIZ_VISIB_INSERT_SCALE = 10;

    /**
     * METAR vertical visibility value scale.
     */
    private static final int METAR_VERT_VISIB_SCALE = 1;

    /**
     * Vertical visibility cloud cover value. From store_METAR_report.c.
     */
    private static final float VERT_VISIB_CLOUD_COVER_VALUE = 1.1f;

    /**
     * All valid weather descriptors. From write_FSS_categ_multi.ecpp.
     */
    private static final List<String> VALID_WX_DESC = Arrays.asList("MI", "PR",
            "BC", "DR", "BL", "SH", "TS", "FZ");

    /**
     * The hydromet element ID's that correspond to the METAR observed weather
     * elements.
     */
    private final List<Integer> hydrometIDs = new ArrayList<>();

    /**
     * A map of WX_string_id structures. This array will contain all the
     * possible primitive weather groupings that are allowed in a METAR report.
     * This will be used for testing the validity of a METAR wx group.
     */
    private final Map<String, Integer> fssWeatherElements = new HashMap<>();

    /**
     * Constructor.
     * 
     * @throws ClimateQueryException
     */
    public ClimateFSSInsertionDAO() throws ClimateQueryException {
        super();

        /**
         * Get the hydromet element ID's that correspond to the METAR observed
         * weather elements. From find_hydromet_ids.ecpp.
         */
        try {
            Object[] results = getDao()
                    .executeSQLQuery(METAR_HYDROMET_ID_QUERY);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Number) {
                        try {
                            int oa = ((Number) result).intValue();
                            logger.debug("Adding Hydromet ID: [" + oa + "]");
                            hydrometIDs.add(oa);
                        } catch (Exception e) { // if casting failed
                            throw new Exception(
                                    "Unexpected return column type from hydromet element ID query.",
                                    e);
                        }

                    } else {
                        throw new Exception(
                                "Unexpected return type from hydromet element ID query, expected Integer, got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                throw new Exception("No results returned from query: ["
                        + METAR_HYDROMET_ID_QUERY + "].");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database hydromet element IDs",
                    e);
        }

        /*
         * Build an array of WX_string_id structures. This array will contain
         * all the possible primitive weather groupings that are allowed in a
         * METAR report. This will be used for testing the validity of a METAR
         * wx group. From get_wx_elements.ecpp.
         */
        try {
            Object[] results = getDao().executeSQLQuery(METAR_QX_QUERY);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        try {
                            Object[] oa = (Object[]) result;
                            Integer id = ((Number) oa[0]).intValue();
                            String element = (String) oa[1];
                            logger.debug("Adding weather element ID [" + id
                                    + "] and string [" + element
                                    + "] to list of weather types.");
                            fssWeatherElements.put(element, id);
                        } catch (Exception e) {
                            // if casting failed
                            throw new Exception(
                                    "Unexpected return column type from FSS weather elements query.",
                                    e);
                        }

                    } else {
                        throw new Exception(
                                "Unexpected return type from FSS weather elements query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                throw new Exception("No results returned from query: ["
                        + METAR_QX_QUERY + "].");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database FSS weather elements",
                    e);
        }

    }

    /**
     * With the given decoded report, insert into Fixed Surface Station tables.
     * 
     * @param surfaceObs
     * @param stationID
     * @param fssReportInstance
     * @throws ClimateException
     */
    public void storeFSSData(SurfaceObs surfaceObs, int stationID,
            int fssReportInstance) throws ClimateException {
        try {
            /*
             * Write the metar reports to the FSS (Fixed Surface Station)
             * tables.
             */
            // figure out report subtype
            String fssReportSubtype;
            switch (surfaceObs.getSpeciFlag()) {
            case 0:
                /* This report is NOT a special (SPECI). */
                fssReportSubtype = METAR_SUBTYPE;
                break;
            case 1:
                /* This report is a special (SPECI). */
                fssReportSubtype = SPECIAL_SUBTYPE;
                break;
            default:
                /* Unknown if this is a SPECI or not */
                fssReportSubtype = "";
                break;
            }

            /*
             * Determine the report augmentation (if any). There are three
             * possible values here: MAN for a manned station, A01 for an
             * automated station without a precipitation descriminator, and A02
             * for an automated station with a precipitation descriminator.
             */
            String fssAugmentation;
            switch (surfaceObs.getAutoFlag()) {
            case 0:
                /* The station is manned */
                fssAugmentation = MANNED_STATION_AUGMENTATION;
                break;
            case 1:
                /* The station is of type A01 */
                fssAugmentation = MetarDecoderUtil.A01_INDICATOR_STRING_NUM;
                break;
            case 2:
                /* The station is of type A02 */
                fssAugmentation = MetarDecoderUtil.A02_INDICATOR_STRING_NUM;
                break;
            default:
                /* Bad value or missing value */
                fssAugmentation = "";
                break;
            }

            /*
             * Determine whether or not the report is a correction (amendment)
             * to a previous report or if the report originated from a fully
             * automated ASOS site.
             */
            String fssCorrection;
            switch (surfaceObs.getCorFlag()) {
            case 0:
                /* A non-corrected report */
                fssCorrection = "F";
                break;
            case 1:
                /* A corrected report */
                fssCorrection = "T";
                break;
            case 2:
                /*
                 * A report from a fully automated station - cannot be corrected
                 */
                fssCorrection = "A";
                break;
            default:
                /* Invalid Information */
                fssCorrection = "";
                break;
            }

            /*
             * Determine the origin time of the observation and convert it into
             * the proper format. Optional field.
             */
            Calendar originCal = null;
            if (surfaceObs.getOriginTime() != 0) {
                originCal = TimeUtil.newCalendar();
                originCal.setTimeInMillis(surfaceObs.getOriginTime());
            } else {
                logger.warn("Millis origin time: [" + surfaceObs.getOriginTime()
                        + "] is not valid.");
            }

            /*
             * Determine the valid time of the observation and convert it into
             * the proper format.
             */
            Calendar observationCal = TimeUtil.newCalendar();
            observationCal.setTimeInMillis(surfaceObs.getObsTime());

            /*
             * Determine the nominal time of the observation and convert it into
             * the proper INFORMIX format.
             */
            Calendar nominalCal = TimeUtil.newCalendar();
            nominalCal.setTimeInMillis(surfaceObs.getNominalTime());

            /* Determine the product version of the AEV (an integer) */
            int fssProdVersion;
            if (System.getenv("AEV_PROD_VERSION") == null) {
                logger.warn(
                        "'AEV_PROD_VERSION' is not set! Product Version in FSS Report table will be null.");
                fssProdVersion = Integer.MIN_VALUE;
            } else {
                try {
                    fssProdVersion = Integer
                            .parseInt(System.getenv("AEV_PROD_VERSION"));
                } catch (NumberFormatException e) {
                    logger.error(
                            "Could not parse FSS Product Version from system.",
                            e);
                    fssProdVersion = Integer.MIN_VALUE;
                }
            }

            /* Process the source_status of the METAR report. */
            int fssSourceStatus = MetarDecoderUtil
                    .checkSourceStatus(surfaceObs.getQcMetar());

            /* Count the number of cloud layers in the report. */
            /*
             * Task #29187: legacy does not count numbered cloud layers for base
             * report
             */
            int fssCloudLayers = 0;

            if (surfaceObs
                    .getLowCloudHeight() != (float) MetarDecoderUtil.MISSING_DATA) {
                fssCloudLayers++;
            }

            if (surfaceObs
                    .getMidCloudHeight() != (float) MetarDecoderUtil.MISSING_DATA) {
                fssCloudLayers++;
            }

            if (surfaceObs
                    .getHighCloudHeight() != (float) MetarDecoderUtil.MISSING_DATA) {
                fssCloudLayers++;
            }

            // insert into FSS tables
            Map<String, Object> fssReportInsertParams = new HashMap<>();
            StringBuilder fssReportInsert = new StringBuilder("INSERT INTO ")
                    .append(ClimateDAOValues.FSS_REPORT_TABLE_NAME)
                    .append(" VALUES(:fssReportInstance,");
            fssReportInsertParams.put("fssReportInstance", fssReportInstance);
            fssReportInsert.append(":stationID,");
            fssReportInsertParams.put("stationID", stationID);
            fssReportInsert.append("'").append(FSS_REPORT_TYPE).append("',");
            fssReportInsert.append(":fssReportSubtype,");
            fssReportInsertParams.put("fssReportSubtype", fssReportSubtype);
            fssReportInsert.append(":fssAugmentation,");
            fssReportInsertParams.put("fssAugmentation", fssAugmentation);
            fssReportInsert.append(":fssCorrection,");
            fssReportInsertParams.put("fssCorrection", fssCorrection);
            fssReportInsert.append(":observationCal,");
            fssReportInsertParams.put("observationCal", observationCal);
            fssReportInsert.append(":nominalCal,");
            fssReportInsertParams.put("nominalCal", nominalCal);

            if (originCal != null) {
                fssReportInsert.append(":originCal,");
                fssReportInsertParams.put("originCal", originCal);
            } else {
                fssReportInsert.append("NULL,");
            }

            fssReportInsert.append("'").append(FSS_TIME_ZONE_ID).append("',");
            fssReportInsert.append("NULL,");

            if (fssProdVersion != Integer.MIN_VALUE) {
                fssReportInsert.append(":fssProdVersion,");
                fssReportInsertParams.put("fssProdVersion", fssProdVersion);
            } else {
                fssReportInsert.append("NULL,");
            }

            fssReportInsert.append("NULL,");
            fssReportInsert.append("NULL,");
            fssReportInsert.append("NULL,");
            fssReportInsert.append(":fssCloudLayers)");
            fssReportInsertParams.put("fssCloudLayers", fssCloudLayers);

            try {
                getDao().executeSQLUpdate(fssReportInsert.toString(),
                        fssReportInsertParams);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting basic FSS report data: ["
                                + fssReportInsert.toString() + "] and map: ["
                                + fssReportInsertParams + "].",
                        e);
            }

            /*
             * Now store the individual decoded METAR elements for this report
             * in the appropriate FSS tables. From
             * store_METAR_report.c#store_METAR_data.
             */
            int nominalHour = nominalCal.get(Calendar.HOUR_OF_DAY);
            /*
             * Loop through the element_ids, writing out the METAR data that
             * corresponds to each id.
             */
            boolean cloudsProcessed = false;
            QCMetar qcMetar = surfaceObs.getQcMetar();
            for (int hydrometID : hydrometIDs) {
                switch (hydrometID) {

                case MetarUtils.METAR_TEMP:
                    if (surfaceObs
                            .getTemp() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getTemp(), qcMetar.getTempDqd());
                    }
                    break;

                case MetarUtils.METAR_TEMP_2_TENTHS:
                    if (surfaceObs
                            .getTemp2Tenths() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getTemp2Tenths(),
                                qcMetar.getTemp2TenthsDqd());
                    }
                    break;

                case MetarUtils.METAR_1HR_PRECIP:
                    if (surfaceObs
                            .getPrecip1hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                        /*
                         * If the rain amount is a trace (0.00), then reset the
                         * rain amount to TRACE. This is done for purposes of
                         * better representation in the verification database.
                         */
                        if (surfaceObs.getPrecip1hr() == 0) {
                            surfaceObs.setPrecip1hr(MetarUtils.FSS_CONTIN_TRACE);
                        }
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPrecip1hr(),
                                qcMetar.getPrecip1hrDqd());
                    } else if (surfaceObs.getPrecipPresent() == 0) {
                        /*
                         * If the rain amount is missing and the PNO indicator
                         * is present in the METAR report, then write out a
                         * value of PNO_PRESENT to the database to indicate that
                         * the precipitation sensor was not working.
                         */
                        surfaceObs.setPrecip1hr(MetarUtils.PNO_PRESENT);
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPrecip1hr(),
                                qcMetar.getPrecip1hrDqd());
                    }
                    break;

                case MetarUtils.METAR_3HR_PRECIP:
                    /*
                     * make sure nominal hour is correct for 3-hour precip.
                     * METAR report makes no distinction between a 3 and 6
                     * hourly precip value; they are the same field.
                     */
                    if ((nominalHour % 6 != 0) && (nominalHour % 3 == 0)) {
                        if (surfaceObs
                                .getPrecip6hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                            /*
                             * If the rain amount is a trace (0.00), then reset
                             * the rain amount to TRACE. This is done for
                             * purposes of better representation in the
                             * verification database.
                             */
                            if (surfaceObs.getPrecip6hr() == 0) {
                                surfaceObs.setPrecip6hr(MetarUtils.FSS_CONTIN_TRACE);
                            }

                            writeFSSContinuousReal(fssReportInstance,
                                    hydrometID, surfaceObs.getPrecip6hr(),
                                    qcMetar.getPrecip3hrDqd());
                        } else if (surfaceObs.getPrecipPresent() == 0) {
                            /*
                             * If the rain amount is missing and the PNO
                             * indicator is present in the METAR report, then
                             * write out a value of PNO_PRESENT to the database
                             * to indicate that the precipitation sensor was not
                             * working at the time of this observation.
                             */
                            surfaceObs.setPrecip6hr(MetarUtils.PNO_PRESENT);
                            writeFSSContinuousReal(fssReportInstance,
                                    hydrometID, surfaceObs.getPrecip6hr(),
                                    qcMetar.getPrecip3hrDqd());
                        }
                    }
                    break;

                case MetarUtils.METAR_6HR_PRECIP:
                    /*
                     * make sure nominal hour is correct for 6-hour precip
                     */
                    if (nominalHour % 6 == 0) {
                        if (surfaceObs
                                .getPrecip6hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                            /*
                             * If the rain amount is a trace (0.00), then reset
                             * the rain amount to TRACE. This is done for
                             * purposes of better representation in the
                             * verification database.
                             */
                            if (surfaceObs.getPrecip6hr() == 0) {
                                surfaceObs.setPrecip6hr(MetarUtils.FSS_CONTIN_TRACE);
                            }

                            writeFSSContinuousReal(fssReportInstance,
                                    hydrometID, surfaceObs.getPrecip6hr(),
                                    qcMetar.getPrecip6hrDqd());

                        } else if (surfaceObs.getPrecipPresent() == 0) {
                            /*
                             * If the rain amount is missing and the PNO
                             * indicator is present in the METAR report, then
                             * write out a value of PNO_PRESENT to the database
                             * to indicate that the precipitation sensor was not
                             * working at the time of this observation.
                             */
                            surfaceObs.setPrecip6hr(MetarUtils.PNO_PRESENT);
                            writeFSSContinuousReal(fssReportInstance,
                                    hydrometID, surfaceObs.getPrecip6hr(),
                                    qcMetar.getPrecip6hrDqd());
                        }
                    }
                    break;
                case MetarUtils.METAR_24HR_PRECIP:
                    if (surfaceObs
                            .getPrecip24hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                        /*
                         * If the rain amount is a trace (0.00), then reset the
                         * rain amount to TRACE. This is done for purposes of
                         * better representation in the verification database.
                         */
                        if (surfaceObs.getPrecip24hr() == 0) {
                            surfaceObs.setPrecip24hr(MetarUtils.FSS_CONTIN_TRACE);
                        }

                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPrecip24hr(),
                                qcMetar.getPrecip24hrDqd());

                    } else if (surfaceObs.getPrecipPresent() == 0) {
                        /*
                         * If the rain amount is missing and the PNO indicator
                         * is present in the METAR report, then write out a
                         * value of PNO_PRESENT to the database to indicate that
                         * the precipitation sensor was not working at the time
                         * of this observation.
                         */
                        surfaceObs.setPrecip24hr(MetarUtils.PNO_PRESENT);
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPrecip24hr(),
                                qcMetar.getPrecip24hrDqd());
                    }
                    break;
                case MetarUtils.METAR_WX:
                    fssSourceStatus = writeFSSCategoryMulti(fssReportInstance,
                            hydrometID, surfaceObs.getPresentWx(),
                            fssSourceStatus);

                    /*
                     * Determine if there was any information regarding the
                     * begin and end times of precipitation in the remarks
                     * section of the METAR. If there was, then write this
                     * information out to the wx_period table in the climate
                     * database. From write_wx_period.ecpp.
                     */
                    fssSourceStatus = writeWxPeriod(fssReportInstance,
                            hydrometID, surfaceObs.getWeatherBeginEnd(),
                            fssSourceStatus);
                    break;
                case MetarUtils.METAR_WIND_SPEED:
                    if (surfaceObs
                            .getWindSpd() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getWindSpd(),
                                qcMetar.getWindSpdDqd());
                    }
                    break;
                case MetarUtils.METAR_WIND_DIRECTION:
                    if (surfaceObs
                            .getWindDir() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSCategorySingle(fssReportInstance, hydrometID,
                                surfaceObs.getWindDir(), METAR_WIND_DIR_SCALE,
                                qcMetar.getWindDirDqd());
                    } else if (surfaceObs.getVariableWindFlag() == 1) {
                        writeFSSCategorySingle(fssReportInstance, hydrometID,
                                VARIABLE_WIND_DIR_VALUE, METAR_WIND_DIR_SCALE,
                                qcMetar.getWindDirDqd());
                    }
                    break;
                case MetarUtils.METAR_PEAK_WIND_SPEED:
                    if (surfaceObs
                            .getPeakWindSpeed() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPeakWindSpeed(),
                                qcMetar.getPeakWindSpdDqd());
                    }
                    break;
                case MetarUtils.METAR_PEAK_WIND_DIR:
                    if (surfaceObs
                            .getPeakWindDir() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSCategorySingle(fssReportInstance, hydrometID,
                                surfaceObs.getPeakWindDir(),
                                METAR_WIND_DIR_SCALE,
                                qcMetar.getPeakWindDirDqd());
                    }
                    break;
                case MetarUtils.METAR_PEAK_WIND_TIME:
                    if (surfaceObs
                            .getPeakWindHHMM() != MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPeakWindHHMM(),
                                qcMetar.getPeakWindTimeDqd());
                    }
                    break;
                case MetarUtils.METAR_SUNSHINE_DURATION:
                    if (surfaceObs
                            .getSunshineDur() != MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getSunshineDur(),
                                qcMetar.getSunshineDurDqd());
                    }
                    break;
                case MetarUtils.METAR_CLOUD_COVER:
                case MetarUtils.METAR_CLOUD_HEIGHT:
                case MetarUtils.METAR_CLOUD_TYPE:
                    if (!cloudsProcessed) {
                        /*
                         * From store_METAR_report.c#process_cloud_layers.
                         */
                        int cloudLayers = 0;
                        boolean isClear = false;

                        if ((surfaceObs
                                .getLowCloudCover() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLowCloudHeight() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLowCloudType() != MetarDecoderUtil.MISSING_DATA)) {
                            if ((surfaceObs.getLowCloudCover() == 0)
                                    && (surfaceObs
                                            .getLowCloudHeight() == (float) MetarDecoderUtil.MISSING_DATA)) {
                                isClear = true;
                            }

                            cloudLayers++;

                            writeFSSCloudLayer(fssReportInstance,
                                    surfaceObs.getLowCloudHeight(),
                                    surfaceObs.getLowCloudCover(),
                                    surfaceObs.getLowCloudType(), cloudLayers,
                                    qcMetar.getLowCloudHgtDqd(),
                                    qcMetar.getLowCloudCoverDqd(),
                                    qcMetar.getLowCloudTypeDqd());
                        }

                        if (((surfaceObs
                                .getMidCloudCover() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getMidCloudHeight() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getMidCloudType() != MetarDecoderUtil.MISSING_DATA))
                                && !isClear) {

                            cloudLayers++;

                            writeFSSCloudLayer(fssReportInstance,
                                    surfaceObs.getMidCloudHeight(),
                                    surfaceObs.getMidCloudCover(),
                                    surfaceObs.getMidCloudType(), cloudLayers,
                                    qcMetar.getMidCloudHgtDqd(),
                                    qcMetar.getMidCloudCoverDqd(),
                                    qcMetar.getMidCloudTypeDqd());
                        }

                        if (((surfaceObs
                                .getHighCloudCover() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getHighCloudHeight() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getHighCloudType() != MetarDecoderUtil.MISSING_DATA))
                                && !isClear) {

                            cloudLayers++;

                            writeFSSCloudLayer(fssReportInstance,
                                    surfaceObs.getHighCloudHeight(),
                                    surfaceObs.getHighCloudCover(),
                                    surfaceObs.getHighCloudType(), cloudLayers,
                                    qcMetar.getHighCloudHgtDqd(),
                                    qcMetar.getHighCloudCoverDqd(),
                                    qcMetar.getHighCloudTypeDqd());
                        }

                        if (((surfaceObs
                                .getLayer4CloudCover() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLayer4CloudHeight() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLayer4CloudType() != MetarDecoderUtil.MISSING_DATA))
                                && !isClear) {

                            cloudLayers++;

                            writeFSSCloudLayer(fssReportInstance,
                                    surfaceObs.getLayer4CloudHeight(),
                                    surfaceObs.getLayer4CloudCover(),
                                    surfaceObs.getLayer4CloudType(),
                                    cloudLayers, qcMetar.getLayer4CloudHgtDqd(),
                                    qcMetar.getLayer4CloudCoverDqd(),
                                    qcMetar.getLayer4CloudTypeDqd());
                        }

                        if (((surfaceObs
                                .getLayer5CloudCover() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLayer5CloudHeight() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLayer5CloudType() != MetarDecoderUtil.MISSING_DATA))
                                && !isClear) {

                            cloudLayers++;

                            writeFSSCloudLayer(fssReportInstance,
                                    surfaceObs.getLayer5CloudHeight(),
                                    surfaceObs.getLayer5CloudCover(),
                                    surfaceObs.getLayer5CloudType(),
                                    cloudLayers, qcMetar.getLayer5CloudHgtDqd(),
                                    qcMetar.getLayer5CloudCoverDqd(),
                                    qcMetar.getLayer5CloudTypeDqd());
                        }

                        if (((surfaceObs
                                .getLayer6CloudCover() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLayer6CloudHeight() != (float) MetarDecoderUtil.MISSING_DATA)
                                || (surfaceObs
                                        .getLayer6CloudType() != MetarDecoderUtil.MISSING_DATA))
                                && !isClear) {

                            cloudLayers++;

                            writeFSSCloudLayer(fssReportInstance,
                                    surfaceObs.getLayer6CloudHeight(),
                                    surfaceObs.getLayer6CloudCover(),
                                    surfaceObs.getLayer6CloudType(),
                                    cloudLayers, qcMetar.getLayer6CloudHgtDqd(),
                                    qcMetar.getLayer6CloudCoverDqd(),
                                    qcMetar.getLayer6CloudTypeDqd());
                        }

                        cloudsProcessed = true;
                    }
                    break;
                case MetarUtils.METAR_VISIB:
                    if (surfaceObs
                            .getVisibility() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSCategorySingle(fssReportInstance, hydrometID,
                                surfaceObs.getVisibility(),
                                METAR_HORIZ_VISIB_INSERT_SCALE,
                                qcMetar.getVsbyDqd());
                    }
                    break;
                case MetarUtils.METAR_SNOW_DEPTH:
                    if (surfaceObs
                            .getSnowDepth() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getSnowDepth(),
                                qcMetar.getSnowDepthDqd());
                    }
                    break;
                case MetarUtils.METAR_VERT_VISIB:
                    if (surfaceObs
                            .getVerticalVisibility() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSCloudLayer(fssReportInstance,
                                surfaceObs.getVerticalVisibility()
                                        * MetarDecoderUtil.M_TO_100S_OF_FT,
                                VERT_VISIB_CLOUD_COVER_VALUE,
                                MetarDecoderUtil.MISSING_DATA,
                                METAR_VERT_VISIB_SCALE,
                                qcMetar.getVertVsbyDqd(),
                                QCMetar.COARSE_CHECKS_PASSED,
                                QCMetar.NO_QC_PERFORMED);
                    }
                    break;
                case MetarUtils.METAR_MSL_PRESS:
                    if (surfaceObs
                            .getSlp() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getSlp(), qcMetar.getSLPDqd());
                    }
                    break;
                case MetarUtils.METAR_ALT_SETTING:
                    if (surfaceObs
                            .getAltSetting() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getAltSetting(),
                                qcMetar.getAltSettingDqd());
                    }
                    break;
                case MetarUtils.METAR_3HR_PRESS_CHNG:
                    if (surfaceObs
                            .getPressureChange3hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getPressureChange3hr(),
                                qcMetar.getPresChg3hrDqd());
                    }
                    break;
                case MetarUtils.METAR_3HR_PRESS_TREND:
                    if (surfaceObs
                            .getPressureTendency() != MetarDecoderUtil.MISSING_DATA) {
                        writeFSSCategorySingle(fssReportInstance, hydrometID,
                                surfaceObs.getPressureTendency(),
                                METAR_3HR_PRESS_TEND_SCALE,
                                qcMetar.getPresChg3hrDqd());
                    }
                    break;
                case MetarUtils.METAR_DEWPOINT:
                    if (surfaceObs
                            .getDewPt() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getDewPt(), qcMetar.getDewPtDqd());
                    }
                    break;
                case MetarUtils.METAR_DEWPOINT_2_TENTHS:
                    if (surfaceObs
                            .getDewPt2Tenths() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getDewPt2Tenths(),
                                qcMetar.getDewPt2TenthsDqd());
                    }
                    break;
                case MetarUtils.METAR_6HR_MAXTEMP:
                    if (surfaceObs
                            .getMaxTemp6hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getMaxTemp6hr(),
                                qcMetar.getMaxTemp6hrDqd());
                    }
                    break;
                case MetarUtils.METAR_6HR_MINTEMP:
                    if (surfaceObs
                            .getMinTemp6hr() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getMinTemp6hr(),
                                qcMetar.getMinTemp6hrDqd());
                    }
                    break;
                case MetarUtils.METAR_24HR_MAXTEMP:
                    if (surfaceObs
                            .getMax24temp() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getMax24temp(),
                                qcMetar.getMaxTemp24hrDqd());
                    }
                    break;
                case MetarUtils.METAR_24HR_MINTEMP:
                    if (surfaceObs
                            .getMin24temp() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getMin24temp(),
                                qcMetar.getMinTemp24hrDqd());
                    }
                    break;
                case MetarUtils.METAR_MAX_WIND_GUST:
                    if (surfaceObs
                            .getGustSpd() != (float) MetarDecoderUtil.MISSING_DATA) {
                        writeFSSContinuousReal(fssReportInstance, hydrometID,
                                surfaceObs.getGustSpd(),
                                qcMetar.getGustSpdDqd());
                    }
                    break;
                default:
                    logger.warn("Unexpected hydromet element ID: [" + hydrometID
                            + "] for report instance: [" + fssReportInstance
                            + "]");
                    break;
                }
            }

            /* Update the weather element count info */
            // get the count
            Map<String, Object> wxEleCountParams = new HashMap<>();

            StringBuilder wxElementCountQuery = new StringBuilder(
                    "SELECT MAX(wx_ele_number) FROM ")
                            .append(ClimateDAOValues.FSS_CATEGORY_MULTI_TABLE_NAME)
                            .append(" WHERE fss_rpt_instance=:fssReportInstance")
                            .append(" AND element_id=")
                            .append(MetarUtils.METAR_WX).append(" AND dqd='")
                            .append(QCMetar.COARSE_CHECKS_PASSED).append("'");

            wxEleCountParams.put("fssReportInstance", fssReportInstance);

            int wxElementCount = ((Number) queryForOneValue(
                    wxElementCountQuery.toString(), wxEleCountParams,
                    Integer.MAX_VALUE)).intValue();
            if (wxElementCount == Integer.MAX_VALUE) {
                logger.debug("Query: [" + wxElementCountQuery.toString()
                        + "] and map: [" + wxEleCountParams
                        + "] returned no results.");
                wxElementCount = 0;
            }

            /*
             * From update_wx_count.ecpp.
             */
            Map<String, Object> wxEleCountUpdateParams = new HashMap<>();

            StringBuilder wxElementCountUpdate = new StringBuilder("UPDATE ")
                    .append(ClimateDAOValues.FSS_REPORT_TABLE_NAME)
                    .append(" SET wx_ele_count=:wxElementCount");
            wxEleCountUpdateParams.put("wxElementCount", wxElementCount);
            wxElementCountUpdate
                    .append(" WHERE fss_rpt_instance=:fssReportInstance");
            wxEleCountUpdateParams.put("fssReportInstance", fssReportInstance);
            try {
                getDao().executeSQLUpdate(wxElementCountUpdate.toString(),
                        wxEleCountUpdateParams);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error with query: [" + wxElementCountUpdate
                                + "] and map: [" + wxEleCountUpdateParams + "]",
                        e);
            }

            /* Update the decoder exit status */
            int decodeStatus = surfaceObs.getDecodeStatus();
            if (decodeStatus != 0) {
                decodeStatus = decodeStatus
                        | MetarDecoderUtil.DECODER_ERROR_SOURCE_STATUS;
            }

            /*
             * From update_source_status.ecpp.
             */
            Map<String, Object> sourceStatusParams = new HashMap<>();

            StringBuilder sourceStatusUpdate = new StringBuilder("UPDATE ")
                    .append(ClimateDAOValues.FSS_REPORT_TABLE_NAME)
                    .append(" SET source_status=:decodeStatus");
            sourceStatusParams.put("decodeStatus", decodeStatus);
            sourceStatusUpdate
                    .append(" WHERE fss_rpt_instance=:fssReportInstance");
            sourceStatusParams.put("fssReportInstance", fssReportInstance);
            try {
                getDao().executeSQLUpdate(sourceStatusUpdate.toString(),
                        sourceStatusParams);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error with query: [" + sourceStatusUpdate
                                + "] and map: [" + sourceStatusParams + "]",
                        e);
            }
        } catch (ClimateQueryException e) {
            throw new ClimateException(
                    "Error with a query on decoding/inserting/updating METAR report.",
                    e);
        }
    }

    /**
     * Insert decoded METAR cloud layer data into the FSS cloud layer table.
     * From write_FSS_cloud_layer.ecpp.
     * 
     * <pre>
     * MODULE NUMBER: 1
     * MODULE NAME:   write_FSS_cloud_layer
     * PURPOSE:
     *       This routine writes out the cloud information returned from a METAR
     *       report. This information includes the cloud coverage, the cloud height,
     *       and the cloud type. This information is written out to the 
     *       FSS_cloud_layer table in the INFORMIX database.
     *
     * </pre>
     * 
     * @param reportInstance
     * @param cloudHeight
     * @param cloudCover
     * @param cloudType
     * @param cloudLayers
     * @param cloudHgtDqd
     * @param cloudCoverDqd
     * @param cloudTypeDqd
     * @throws ClimateQueryException
     */

    private void writeFSSCloudLayer(int reportInstance, float cloudHeight,
            float cloudCover, int cloudType, int cloudLayers,
            String cloudHgtDqd, String cloudCoverDqd, String cloudTypeDqd)
                    throws ClimateQueryException {
        /* Write the cloud height information to the FSS_cloud_layer table. */
        if (cloudHeight != (float) MetarDecoderUtil.MISSING_DATA) {
            try {
                writeFSSCloudLayerSupport(reportInstance,
                        MetarUtils.METAR_CLOUD_HEIGHT, cloudLayers,
                        ClimateUtilities.nint(cloudHeight), cloudHgtDqd);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting FSS cloud layer height report data.",
                        e);
            }
        }

        /* Process the cloud cover information. */
        if (cloudCover != (float) MetarDecoderUtil.MISSING_DATA) {
            try {
                writeFSSCloudLayerSupport(reportInstance,
                        MetarUtils.METAR_CLOUD_COVER, cloudLayers,
                        ClimateUtilities.nint(cloudCover
                                * MetarDecoderUtil.METAR_CLOUD_COVER_SCALE),
                        cloudCoverDqd);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting FSS cloud layer cover report data.",
                        e);
            }
        }

        /* Process the cloud type information (if there is any). */
        if (cloudType != MetarDecoderUtil.MISSING_DATA) {
            try {
                writeFSSCloudLayerSupport(reportInstance,
                        MetarUtils.METAR_CLOUD_TYPE, cloudLayers, cloudType,
                        cloudTypeDqd);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting FSS cloud layer type report data.", e);
            }
        }
    }

    /**
     * Support for writing cloud layer data, checking if insert or update is
     * appropriate.
     * 
     * @param reportInstance
     * @param elementID
     * @param layerNumber
     * @param elementValue
     * @param dqd
     * @throws ClimateQueryException
     */
    private void writeFSSCloudLayerSupport(int reportInstance, int elementID,
            int layerNumber, int elementValue, String dqd)
                    throws ClimateQueryException {
        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder query = new StringBuilder("SELECT * FROM ")
                .append(ClimateDAOValues.FSS_CLOUD_LAYER_TABLE_NAME)
                .append(" WHERE fss_rpt_instance=:fss_rpt_instance");
        queryParams.put("fss_rpt_instance", reportInstance);
        query.append(" AND element_id=:element_id");
        queryParams.put("element_id", elementID);
        query.append(" AND layer_number=:layer_number");
        queryParams.put("layer_number", layerNumber);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);

            Map<String, Object> insertParams = new HashMap<>();
            insertParams.put("fss_rpt_instance", reportInstance);
            insertParams.put("element_id", elementID);
            insertParams.put("layer_number", layerNumber);
            insertParams.put("element_value", elementValue);
            insertParams.put("dqd", dqd);

            StringBuilder insert;
            if ((results != null) && (results.length >= 1)) {
                // update
                insert = new StringBuilder("UPDATE ")
                        .append(ClimateDAOValues.FSS_CLOUD_LAYER_TABLE_NAME)
                        .append(" SET element_value=:element_value, dqd=:dqd ")
                        .append(" WHERE fss_rpt_instance=:fss_rpt_instance")
                        .append(" AND element_id=:element_id")
                        .append(" AND layer_number=:layer_number");
            } else {
                // insert
                insert = new StringBuilder("INSERT INTO ")
                        .append(ClimateDAOValues.FSS_CLOUD_LAYER_TABLE_NAME)
                        .append(" VALUES(:fss_rpt_instance,")
                        .append(":element_id,").append(":layer_number,")
                        .append(":element_value,").append(":dqd)");
            }

            try {
                getDao().executeSQLUpdate(insert.toString(), insertParams);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting FSS cloud layer report data: ["
                                + insert.toString() + "] and map: ["
                                + insertParams + "].",
                        e);
            }
        } catch (ClimateQueryException e) {
            throw e;
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying for report existence with query: [" + query
                            + "] and map: [" + queryParams + "]",
                    e);
        }
    }

    /**
     * Write decoded METAR data to FSS category single table. From
     * write_FSS_categ_single.ecpp.
     * 
     * <pre>
     * MODULE NUMBER: 1
     * MODULE NAME:   write_FSS_categ_single
     * PURPOSE:       This routine writes a categorical or discrete weather element
     *                out to the FSS_categ_single table in the verification 
     *                database.
     * 
     * </pre>
     * 
     * @param reportInstance
     * @param elementID
     * @param value
     * @param scale
     * @param dqd
     * @throws ClimateQueryException
     */

    private void writeFSSCategorySingle(int reportInstance, int elementID,
            float value, float scale, String dqd) throws ClimateQueryException {
        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder query = new StringBuilder("SELECT * FROM ")
                .append(ClimateDAOValues.FSS_CATEGORY_SINGLE_TABLE_NAME)
                .append(" WHERE fss_rpt_instance=:fss_rpt_instance");
        queryParams.put("fss_rpt_instance", reportInstance);
        query.append(" AND element_id=:element_id");
        queryParams.put("element_id", elementID);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);

            Map<String, Object> insertParams = new HashMap<>();
            insertParams.put("reportInstance", reportInstance);
            insertParams.put("elementID", elementID);
            insertParams.put("value", (int) (value * scale));
            insertParams.put("dqd", dqd);

            StringBuilder insert;
            if ((results != null) && (results.length >= 1)) {
                // update
                insert = new StringBuilder("UPDATE ")
                        .append(ClimateDAOValues.FSS_CATEGORY_SINGLE_TABLE_NAME)
                        .append(" SET element_value=:value, dqd=:dqd WHERE ")
                        .append(" fss_rpt_instance=:reportInstance AND element_id=:elementID");
            } else {
                // insert
                insert = new StringBuilder("INSERT INTO ")
                        .append(ClimateDAOValues.FSS_CATEGORY_SINGLE_TABLE_NAME)
                        .append(" VALUES(");
                insert.append(":reportInstance,");
                insert.append(":elementID,");
                insert.append(":value,");
                insert.append(":dqd)");

            }
            try {
                getDao().executeSQLUpdate(insert.toString(), insertParams);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting FSS category single report data: ["
                                + insert.toString() + "] and map: ["
                                + insertParams + "].",
                        e);
            }
        } catch (ClimateQueryException e) {
            throw e;
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying for report existence with query: [" + query
                            + "] and map: [" + queryParams + "]",
                    e);
        }
    }

    /**
     * Write out decoded weather period METAR data to fss_wx_period table. Very
     * similar logic to
     * {@link #writeFSSCategoryMulti(int, int, List, String[], int)}. From
     * write_wx_period.ecpp.
     * 
     * <pre>
     * MODULE NUMBER: 1
     * MODULE NAME:   write_wx_period
     * PURPOSE:       The begin and end times of precipitation can be specified
     *                in the remarks section of a METAR report. For example, if
     *                rain began at 11:25 and ended at 11:40, then the sheduled
     *                METAR issued at 11:51 would have a group in its remarks
     *                section resembling RAB25E40. See section 8.5.5 in the
     *                December 1995 version of the Federal Meteorological
     *                Handbook Number 1 (FMH 1) for more details.
     *
     *                This routine will store the begin and end times of
     *                precipitation for up to 10 weather types. The times
     *                consist of a hour and a minute in the wx_period table in the
     *                hmdb database. Along with this information is the id of the
     *                weather element that the times correspond to and the id 
     *                of the METAR report that the times correspond to.
     *
     *                The addition of this information is necessary for 
     *                Enhanced Aviation Verification (EAV) in build 5.0 of 
     *                AWIPS.
     * </pre>
     * 
     * @param reportInstance
     * @param elementID
     * @param decodedWeatherElements
     * @param sourceStatus
     * @return new source status, as it may have changed.
     * @throws ClimateQueryException
     */

    private int writeWxPeriod(int reportInstance, int elementID,
            RecentWx[] decodedWeatherElements, int sourceStatus)
                    throws ClimateQueryException {
        int weatherElementCount = 0;

        for (int i = 0; (i < decodedWeatherElements.length)
                && (decodedWeatherElements[i] != null)
                && (!decodedWeatherElements[i].getRecentWeatherName()
                        .isEmpty()); i++) {
            // make a copy
            String decodedWeatherElement = decodedWeatherElements[i]
                    .getRecentWeatherName();

            /* Check for an intensity indicator. */
            String intensity = "";
            if (decodedWeatherElement.startsWith("+")
                    || decodedWeatherElement.startsWith("-")) {
                intensity = decodedWeatherElement.substring(0, 1);
                decodedWeatherElement = decodedWeatherElement.substring(1);
            }

            /* Check for a descriptor */
            String descriptor = "";
            for (String currDesc : VALID_WX_DESC) {
                if (decodedWeatherElement.startsWith(currDesc)) {
                    descriptor = currDesc;
                    decodedWeatherElement = decodedWeatherElement
                            .substring(currDesc.length());
                    break;
                }
            }

            /*
             * Process the individual weather components in the weather group.
             */
            String weatherElement = "";
            int passes = 0;
            do {
                StringBuilder weatherComponentBldr = new StringBuilder();

                if (decodedWeatherElement.length() >= 2) {
                    weatherElement += decodedWeatherElement.substring(0, 2);
                    decodedWeatherElement = decodedWeatherElement.substring(2);
                }

                /* Build the weather group. */
                if (!intensity.isEmpty() && passes == 0) {
                    weatherComponentBldr.append(intensity);
                }

                weatherComponentBldr.append(descriptor);
                weatherComponentBldr.append(weatherElement);
                Integer fssWeatherElementID = fssWeatherElements
                        .get(weatherComponentBldr.toString());

                if (fssWeatherElementID == null) {
                    logger.warn(weatherComponentBldr
                            + " not recognized as valid weather component.");
                    MetarDecoderUtil.setSourceStatus(QCMetar.DECODER_ERROR,
                            sourceStatus);
                } else {
                    weatherElementCount++;
                    int weatherElementNumber = weatherElementCount;
                    int fssElementValue = fssWeatherElementID;

                    String beginTime;
                    if (decodedWeatherElements[i]
                            .getBeginHour() != MetarDecoderUtil.MISSING_DATA) {
                        beginTime = "'" + new ClimateTime(
                                decodedWeatherElements[i].getBeginHour(),
                                decodedWeatherElements[i].getBeginMinute())
                                        .toHourMinString()
                                + "'";
                    } else {
                        beginTime = "NULL";
                    }

                    String endTime;
                    if (decodedWeatherElements[i]
                            .getEndHour() != MetarDecoderUtil.MISSING_DATA) {
                        endTime = "'"
                                + new ClimateTime(
                                        decodedWeatherElements[i].getEndHour(),
                                        decodedWeatherElements[i]
                                                .getEndMinute())
                                                        .toHourMinString()
                                + "'";
                    } else {
                        endTime = "NULL";
                    }

                    Map<String, Object> queryParams = new HashMap<>();

                    StringBuilder query = new StringBuilder("SELECT * FROM ")
                            .append(ClimateDAOValues.FSS_WX_PERIOD_TABLE_NAME)
                            .append(" WHERE fss_rpt_instance=:fss_rpt_instance");
                    queryParams.put("fss_rpt_instance", reportInstance);
                    query.append(" AND element_id=:element_id");
                    queryParams.put("element_id", elementID);
                    query.append(" AND element_value=:element_value");
                    queryParams.put("element_value", fssElementValue);
                    query.append(" AND element_num=:element_num");
                    queryParams.put("element_num", weatherElementNumber);

                    try {
                        Object[] results = getDao()
                                .executeSQLQuery(query.toString(), queryParams);

                        Map<String, Object> insertParams = new HashMap<>();
                        insertParams.put("reportInstance", reportInstance);
                        insertParams.put("elementID", elementID);
                        insertParams.put("fssElementValue", fssElementValue);
                        insertParams.put("weatherElementNumber",
                                weatherElementNumber);

                        StringBuilder insert;
                        if ((results != null) && (results.length >= 1)) {
                            // update
                            insert = new StringBuilder("UPDATE ")
                                    .append(ClimateDAOValues.FSS_WX_PERIOD_TABLE_NAME)
                                    .append(" SET wx_begin_dtime=")
                                    .append(beginTime).append(", wx_end_dtime=")
                                    .append(endTime)
                                    .append(" WHERE fss_rpt_instance=:reportInstance")
                                    .append(" AND element_id=:elementID")
                                    .append(" AND element_value=:fssElementValue")
                                    .append(" AND element_num=:weatherElementNumber");
                        } else {
                            // insert
                            insert = new StringBuilder("INSERT INTO ")
                                    .append(ClimateDAOValues.FSS_WX_PERIOD_TABLE_NAME)
                                    .append(" VALUES(");
                            insert.append(":reportInstance,");
                            insert.append(":elementID,");
                            insert.append(":fssElementValue,");
                            insert.append(":weatherElementNumber,");
                            insert.append(beginTime).append(",");
                            insert.append(endTime).append(")");
                        }

                        try {
                            getDao().executeSQLUpdate(insert.toString(),
                                    insertParams);
                        } catch (Exception e) {
                            throw new ClimateQueryException(
                                    "Error inserting FSS wx period data: ["
                                            + insert.toString() + "] and map: ["
                                            + insertParams + "].",
                                    e);
                        }
                    } catch (ClimateQueryException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying for report existence with query: ["
                                        + query + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                }

                passes++;
            } while (!decodedWeatherElement.isEmpty());
        }

        return sourceStatus;
    }

    /**
     * Write out decoded METAR data to fss_categ_multi table. From
     * write_FSS_categ_multi.ecpp.
     * 
     * <pre>
     * MODULE NUMBER: 1
     * MODULE NAME:   write_FSS_categ_multi
     * PURPOSE:       Given a two-dimensional array of METAR weather groups,
     *                this routine parses each group into its basic weather
     *                components. Once this is done, the routine searches 
     *                a master table of valid METAR weather data to determine
     *                if these weather components are valid. If a weather 
     *                component is valid, a numeric id representing it is written
     *                out to the FSS_categ_multi table. If the component is invalid
     *                the routine generates a warning message and sets a decoder
     *                error flag.
     *
     *                Examples: 
     *                Given the following weather group: SHRASNPE
     *                
     *                This routine produces the following weather components:
     *                SHRA, SHSN, SHPE.
     *                 
     *                Given the following weather group: +RASN
     *       
     *                This routine produces the following weather components:
     *                +RA, SN.
     * </pre>
     * 
     * @param reportInstance
     * @param elementID
     * @param decodedWeatherElements
     * @param sourceStatus
     * @return new source status, as it may have changed.
     * @throws ClimateQueryException
     */

    private int writeFSSCategoryMulti(int reportInstance, int elementID,
            String[] decodedWeatherElements, int sourceStatus)
                    throws ClimateQueryException {

        int weatherElementCount = 0;

        for (int i = 0; (i < decodedWeatherElements.length)
                && (decodedWeatherElements[i] != null)
                && (!decodedWeatherElements[i].isEmpty()); i++) {
            // make a copy
            String decodedWeatherElement = decodedWeatherElements[i];

            /* Check for an intensity indicator. */
            String intensity = "";
            if (decodedWeatherElement.startsWith("+")
                    || decodedWeatherElement.startsWith("-")) {
                intensity = decodedWeatherElement.substring(0, 1);
                decodedWeatherElement = decodedWeatherElement.substring(1);
            }

            /* Check for a descriptor */
            String descriptor = "";
            for (String currDesc : VALID_WX_DESC) {
                if (decodedWeatherElement.startsWith(currDesc)) {
                    descriptor = currDesc;
                    decodedWeatherElement = decodedWeatherElement
                            .substring(currDesc.length());
                    break;
                }
            }

            /*
             * Process the individual weather components in the weather group.
             */
            StringBuilder weatherElementBldr = new StringBuilder();
            int passes = 0;
            do {
                StringBuilder weatherComponentBldr = new StringBuilder();

                if (decodedWeatherElement.length() >= 2) {
                    weatherElementBldr
                            .append(decodedWeatherElement.substring(0, 2));
                    decodedWeatherElement = decodedWeatherElement.substring(2);
                }

                /* Build the weather group. */
                if (!intensity.isEmpty() && passes == 0) {
                    weatherComponentBldr.append(intensity);
                }

                weatherComponentBldr.append(descriptor);
                weatherComponentBldr.append(weatherElementBldr);
                Integer fssWeatherElementID = fssWeatherElements
                        .get(weatherComponentBldr.toString());

                if (fssWeatherElementID == null) {
                    logger.error(weatherComponentBldr
                            + " not recognized as valid weather component in report instance ["
                            + reportInstance + "].");

                    MetarDecoderUtil.setSourceStatus(QCMetar.DECODER_ERROR,
                            sourceStatus);
                } else {
                    logger.debug(weatherComponentBldr
                            + " is a valid weather component with ID ["
                            + fssWeatherElementID + "] in report instance ["
                            + reportInstance + "].");

                    weatherElementCount++;
                    int weatherElementNumber = weatherElementCount;
                    int fssElementValue = fssWeatherElementID;

                    Map<String, Object> queryParams = new HashMap<>();

                    StringBuilder query = new StringBuilder("SELECT * FROM ")
                            .append(ClimateDAOValues.FSS_CATEGORY_MULTI_TABLE_NAME)
                            .append(" WHERE fss_rpt_instance=:fss_rpt_instance");
                    queryParams.put("fss_rpt_instance", reportInstance);
                    query.append(" AND element_id=:element_id");
                    queryParams.put("element_id", elementID);
                    query.append(" AND wx_ele_number=:wx_ele_number");
                    queryParams.put("wx_ele_number", weatherElementNumber);

                    try {
                        Object[] results = getDao()
                                .executeSQLQuery(query.toString(), queryParams);

                        Map<String, Object> insertParams = new HashMap<>();
                        insertParams.put("reportInstance", reportInstance);
                        insertParams.put("elementID", elementID);
                        insertParams.put("weatherElementNumber",
                                weatherElementNumber);
                        insertParams.put("fssElementValue", fssElementValue);

                        StringBuilder insert;
                        if ((results != null) && (results.length >= 1)) {
                            // update
                            insert = new StringBuilder("UPDATE ")
                                    .append(ClimateDAOValues.FSS_CATEGORY_MULTI_TABLE_NAME)
                                    .append(" SET element_value=:fssElementValue,")
                                    .append(" dqd='")
                                    .append(QCMetar.COARSE_CHECKS_PASSED)
                                    .append("'")
                                    .append(" WHERE fss_rpt_instance=:reportInstance")
                                    .append(" AND element_id=:elementID")
                                    .append(" AND wx_ele_number=:weatherElementNumber");
                        } else {
                            // insert
                            insert = new StringBuilder("INSERT INTO ")
                                    .append(ClimateDAOValues.FSS_CATEGORY_MULTI_TABLE_NAME)
                                    .append(" VALUES(");
                            insert.append(":reportInstance,");
                            insert.append(":elementID,");
                            insert.append(":weatherElementNumber,");
                            insert.append(":fssElementValue,");
                            insert.append("'")
                                    .append(QCMetar.COARSE_CHECKS_PASSED)
                                    .append("')");
                        }
                        try {
                            getDao().executeSQLUpdate(insert.toString(),
                                    insertParams);
                        } catch (Exception e) {
                            throw new ClimateQueryException(
                                    "Error inserting FSS multi category data: ["
                                            + insert.toString() + "] and map: ["
                                            + insertParams + "].",
                                    e);
                        }
                    } catch (ClimateQueryException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying for report existence with query: ["
                                        + query + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                }

                passes++;
            } while (!decodedWeatherElement.isEmpty());
        }

        return sourceStatus;
    }

    /**
     * Write a floating value to appropriate FSS table. From
     * write_FSS_contin_real.ecpp.
     * 
     * <pre>
     * MODULE NUMBER: 1
    * MODULE NAME:   write_FSS_contin_real
    * PURPOSE:       This routine writes out a row to the FSS_CONTIN_REAL table. A
    *                A row in this table consists of a number representing the 
    *                report instance, a number representing the hydromet element
    *                identifier of the METAR element, a floating point data value,
    *                and a data quality descriptor flag.
    *
    *                Important..... It is the responsiblity of the calling routine
    *                to open and close the connection to the database containing
    *                the FSS_CONTIN_REAL table.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   Input  long        report_instance      Contains the unique numeric
    *                                           identifier of the METAR report
    *                                           (as defined in the FSS_REPORT 
    *                                           table).
    *   Input  int         element_id           Contains the hydromet_element
    *                                           table identifer of the METAR
    *                                           element.
    *   Input  float       data_value           The value of the METAR element
    *                                           being stored.
    *   Input  char        dqd                  The data quality descriptor flag.
     * </pre>
     * 
     * @param reportInstance
     * @param elementID
     * @param value
     * @param dqd
     * @throws ClimateQueryException
     */
    private void writeFSSContinuousReal(int reportInstance, int elementID,
            float value, String dqd) throws ClimateQueryException {
        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder query = new StringBuilder("SELECT * FROM ")
                .append(ClimateDAOValues.FSS_CONTIN_REAL_TABLE_NAME)
                .append(" WHERE fss_rpt_instance=:fss_rpt_instance");
        queryParams.put("fss_rpt_instance", reportInstance);
        query.append(" AND element_id=:element_id");
        queryParams.put("element_id", elementID);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);

            Map<String, Object> insertParams = new HashMap<>();
            insertParams.put("reportInstance", reportInstance);
            insertParams.put("elementID", elementID);
            insertParams.put("value", value);
            insertParams.put("dqd", dqd);

            StringBuilder insert;
            if ((results != null) && (results.length >= 1)) {
                // update
                insert = new StringBuilder("UPDATE ")
                        .append(ClimateDAOValues.FSS_CONTIN_REAL_TABLE_NAME)
                        .append(" SET element_value=:value, dqd=:dqd WHERE ")
                        .append(" fss_rpt_instance=:reportInstance AND element_id=:elementID");
            } else {
                // insert
                insert = new StringBuilder("INSERT INTO ")
                        .append(ClimateDAOValues.FSS_CONTIN_REAL_TABLE_NAME)
                        .append(" VALUES(");
                insert.append(":reportInstance,");
                insert.append(":elementID,");
                insert.append(":value,");
                insert.append(":dqd)");

            }
            try {
                getDao().executeSQLUpdate(insert.toString(), insertParams);
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error inserting FSS real report data: ["
                                + insert.toString() + "] and map: ["
                                + insertParams + "].",
                        e);
            }
        } catch (ClimateQueryException e) {
            throw e;
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying for report existence with query: [" + query
                            + "] and map: [" + queryParams + "]",
                    e);
        }
    }
}
