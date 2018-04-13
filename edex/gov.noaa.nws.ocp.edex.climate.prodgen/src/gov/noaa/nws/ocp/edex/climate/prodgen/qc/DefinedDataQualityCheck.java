/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.qc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.checker.ClimateDataQualityChecker;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.checker.DailyClimateDataQualityChecker;
import gov.noaa.nws.ocp.edex.climate.prodgen.qc.checker.MonthlyClimateDataQualityChecker;

/**
 * SiteDefinedDataQualityCheck
 * 
 * Part of auto ClimateProdGenerateSession, used by session to check site
 * defined QC parameters before move on after Climate data is generated.
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

public class DefinedDataQualityCheck {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DefinedDataQualityCheck.class);

    /**
     * Daily property pattern.
     */
    private static final Pattern DAILY_PARAM = Pattern.compile("daily\\..+");

    /**
     * Period property pattern.
     */
    private static final Pattern PERIOD_PARAM = Pattern.compile("period\\..+");

    /**
     * Folder for file.
     */
    private static final String CLIMATE_FOLDER = "climate" + File.separator;

    /**
     * QC properties file name.
     */
    private static final String DEFINED_QC_FILE = "qcparams.properties";

    /**
     * QC properties file path.
     */
    private static final String DEFINED_QC_PATH = CLIMATE_FOLDER
            + DEFINED_QC_FILE;

    /**
     * Localization levels to try to load, in order
     */
    private static final LocalizationLevel[] LOCALIZATIONS_TO_TRY = new LocalizationLevel[] {
            LocalizationLevel.SITE, LocalizationLevel.REGION,
            LocalizationLevel.BASE };

    private boolean check = false;

    private PeriodType periodType;

    private ClimateDataQualityChecker checker;

    /**
     * Constructor
     * 
     * @param pType
     */
    public DefinedDataQualityCheck(PeriodType pType) {
        this.periodType = pType;

        Properties props = this.loadDefinedQCProperties();
        if (check && props != null) {
            populateQCTriples(props);
        } else {
            logger.warn(
                    "Null or empty Climate QC properties. No QC checking will be performed.");
        }
    }

    /**
     * Load QC properties.
     */
    private Properties loadDefinedQCProperties() {
        IPathManager pm = PathManagerFactory.getPathManager();

        boolean success = false;

        File qcParamFile = null;

        for (int i = 0; (i < LOCALIZATIONS_TO_TRY.length) && !success; i++) {
            LocalizationLevel level = LOCALIZATIONS_TO_TRY[i];

            LocalizationContext lc = pm
                    .getContext(LocalizationType.COMMON_STATIC, level);

            qcParamFile = pm.getFile(lc, DEFINED_QC_PATH);

            if (qcParamFile.exists() && qcParamFile.isFile()) {
                success = true;
            }
        }

        if (!success) {
            logger.warn("No [" + DEFINED_QC_PATH
                    + "] common localization file found in the levels: ["
                    + Arrays.toString(LOCALIZATIONS_TO_TRY)
                    + "]. No QC check will be performed.");
            return null;
        }

        Properties qcParams = new Properties();
        try (InputStream is = new FileInputStream(qcParamFile)) {
            qcParams.load(is);
        } catch (Exception e) {
            logger.error("Failed to load defined QC parameters from file: ["
                    + qcParamFile.getAbsolutePath() + "].", e);
        }

        if (qcParams.size() > 0) {
            // There are site defined QC parameters
            check = true;
            logger.debug("Loaded [" + qcParams.size()
                    + "] Climate QC properties file ["
                    + qcParamFile.getAbsolutePath() + "]");
        }

        return qcParams;
    }

    /**
     * populateQCTriples
     * 
     * @param props
     */
    private void populateQCTriples(Properties props) {
        Pattern propertyPattern = null;

        if (periodType.isDaily()) {
            // Only populate dsm properties
            checker = new DailyClimateDataQualityChecker();
            propertyPattern = DAILY_PARAM;
        } else if (periodType.isPeriod()) {
            // Only populate msm properties
            checker = new MonthlyClimateDataQualityChecker();
            propertyPattern = PERIOD_PARAM;
        } else {
            logger.warn("Unsupported period type for QC: ["
                    + periodType.toString() + "]");
            return;
        }

        for (Entry<Object, Object> entry : props.entrySet()) {
            logger.debug("Examining QC property: [" + entry.getKey().toString()
                    + "],[" + entry.getValue().toString() + "]");

            if (propertyPattern.matcher(entry.getKey().toString()).matches()) {
                checker.addOneDataQualityCheckTriple(
                        new DataQualityCheckTriple(entry.getKey().toString(),
                                entry.getValue().toString()));
            } else {
                logger.debug("Property entry did not match pattern: ["
                        + propertyPattern.toString() + "]");
            }
        }
    }

    /**
     * check Based on given parameters and conditions for checking, perform
     * following checking: 1) simply check missing value(s) 2) check if a value
     * of given parameter exceed the range ( >, < )
     * 
     * @return false: if any checking parameter meet the criteria true (pass):
     *         if everything is fine defined checking parameters.
     */
    public CheckResult check(ClimateRunData data) throws Exception {
        if (!check || checker == null) {
            /*
             * when no parameter defined by the site, or no file found, no
             * checking
             */
            logger.info(
                    "No Climate QC parameters defined or no QC file found. No check will be performed.");
            return new CheckResult();
        } else {
            // need to check something
            if (!checker.getCheckList().isEmpty()) {
                return checker.check(data);
            } else {
                logger.info(
                        "No Climate QC parameters defined for period type: ["
                                + periodType.toString()
                                + "]. No check will be performed.");
                return new CheckResult();
            }
        }
    }

    /**
     * @return the check
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * @param check
     *            the check to set
     */
    public void setCheck(boolean check) {
        this.check = check;
    }

    /**
     * @return the periodType
     */
    public PeriodType getPeriodType() {
        return periodType;
    }

    /**
     * @param periodType
     *            the periodType to set
     */
    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    /**
     * @return the checker
     */
    public ClimateDataQualityChecker getChecker() {
        return checker;
    }

    /**
     * @param checker
     *            the checker to set
     */
    public void setChecker(ClimateDataQualityChecker checker) {
        this.checker = checker;
    }

}
