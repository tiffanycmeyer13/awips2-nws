/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.atcf.configuration.GeographyPoint;
import gov.noaa.nws.ocp.common.atcf.configuration.GeographyPoints;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * Class to represent the information configured on Advisory Composition dialog
 * for a given storm, which will be stored into a text file
 * NHCMESSAGE/stormId.adv in format as:
 *
 * <pre>
 *
 * This file contains data for creating NHC advisory products.
 * The forecast_type line contains changes in the storm state for
 * TAU's 12, 24, 36, 48, 72, 96, 120, 144 and 168.
 * The public frequency can be on either a 6, 3 or 2 hour schedule.
 *
 * START_OF_DATA:

 * special        : NO
 * special_HHMM   : 1200
 * special_TAU    :  3
 * advisory#      :  49
 * AWIPS_bin#     : 1
 * forecaster     : JLB
 * daylight_saving: YES
 * pressure       :  965
 * center_accuracy:  20
 * forecast_type  : IN IN IN PI IN IN IN PI DD
 * geography_ref1 :  29.1N  83.1W CEDAR KEY FLORIDA
 * geography_ref2 :  28.0N  82.5W TAMPA FLORIDA
 * public_freq    : 3
 * final_advisory : NO
 * ww_US          : YES
 * ww_Intl        : YES
 * TAU_0_DTG      : 2017091106
 * Potential_TC   : NO
 *
 * Note - added TAUS line with one-to-one relationship to "forecast_type".
 *
 * TAUs           : 12 24 36 48 72 96 120 144 168
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2020 82721      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AdvisoryInfo {

    private static final String[] HEADER = new String[] {
            "This file contains data for creating NHC advisory products.",
            "The forecast_type line contains changes in the storm state for",
            "TAU's 12, 24, 36, 48, 72, 96, 120, 144 and 168.",
            "The public frequency can be on either a 6 or 3 hour schedule",
            "", "START_OF_DATA:", "" };

    private static final String INFO_SEPARATOR = ":";

    // Default TAUs
    private static final List<String> DEFAULT_TAUS = Arrays.asList("12", "24",
            "36", "48", "60", "72", "96", "120", "144", "168");

    /*
     * Maximum awips bin number allowed - this is the last character used in the
     * product header's 10-character PIL and legacy ATCF allows up to 5. So the
     * final numeric digit is assigned on a rotating basis of 5 by cyclone
     * number, i.e., WTNT31 KNHC would be used for the first, sixth, and
     * eleventh Atlantic system that NHC has written advisories on in a given
     * year, while WTNT32 KNHC would be used for the second, seventh, or twelfth
     * system, and so on.
     */
    private static final int MAX_AWIPS_BIN_NUMBER = 5;

    private Storm storm;

    private String dtg;

    private boolean specialAdv;

    private String specialAdvTime;

    private int advNum;

    private int specialTAU;

    private int awipsBinNum;

    private boolean potentialTC;

    private String forecaster;

    private boolean daylightSaving;

    private int pres;

    private int centerAccuracy;

    private List<String> forecastType;

    private GeographyPoint geoRef1;

    private GeographyPoint geoRef2;

    private int frequency;

    private boolean wwUS;

    private boolean wwIntl;

    private boolean finalAdv;

    private List<String> taus;

    /**
     * Default constructor
     */
    public AdvisoryInfo() {
        this.storm = null;
        this.dtg = "";
        this.specialAdv = false;
        this.specialAdvTime = "0000";
        this.advNum = 1;
        this.specialTAU = 3;
        this.awipsBinNum = 1;
        this.potentialTC = false;
        this.forecaster = "OOO";
        this.daylightSaving = false;
        this.pres = 850;
        this.centerAccuracy = 0;
        this.forecastType = new ArrayList<>();
        this.geoRef1 = null;
        this.geoRef2 = null;
        this.frequency = 6;
        this.wwUS = false;
        this.wwIntl = false;
        this.finalAdv = false;
        this.taus = new ArrayList<>();
    }

    /**
     * Constructor
     */
    public AdvisoryInfo(Storm storm, String dtg, boolean specialAdv,
            String advTime, int advNum, int specialTAU, int awipsBinNum,
            boolean potentialTC, String forecaster, boolean daylightSaving,
            int pres, int centerAccuracy, List<String> forecastType,
            GeographyPoint geoRef1, GeographyPoint geoRef2, int frequency,
            boolean finalAdv, boolean wwUS, boolean wwIntl, List<String> taus) {
        this.storm = storm;
        this.dtg = dtg;
        this.specialAdv = specialAdv;
        this.specialAdvTime = advTime;
        this.advNum = advNum;
        this.specialTAU = specialTAU;
        this.awipsBinNum = awipsBinNum;
        this.potentialTC = potentialTC;
        this.forecaster = forecaster;
        this.daylightSaving = daylightSaving;
        this.pres = pres;
        this.centerAccuracy = centerAccuracy;
        this.forecastType = forecastType;
        this.geoRef1 = geoRef1;
        this.geoRef2 = geoRef2;
        this.frequency = frequency;
        this.wwUS = wwUS;
        this.wwIntl = wwIntl;
        this.finalAdv = finalAdv;
        this.taus = taus;
    }

    /**
     * @return the header
     */
    public static String[] getHeader() {
        return HEADER;
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    /**
     * @return the dtg
     */
    public String getDtg() {
        return dtg;
    }

    /**
     * @param dtg
     *            the dtg to set
     */
    public void setDtg(String dtg) {
        this.dtg = dtg;
    }

    /**
     * @return the specialAdv
     */
    public boolean isSpecialAdv() {
        return specialAdv;
    }

    /**
     * @param specialAdv
     *            the specialAdv to set
     */
    public void setSpecialAdv(boolean specialAdv) {
        this.specialAdv = specialAdv;
    }

    /**
     * @return the specilaAdvTime
     */
    public String getSpecialAdvTime() {
        return specialAdvTime;
    }

    /**
     * @param specialAdvTime
     *            the specialAdvTime to set
     */
    public void setSpecialAdvTime(String specialAdvTime) {
        this.specialAdvTime = specialAdvTime;
    }

    /**
     * @return the advNum
     */
    public int getAdvNum() {
        return advNum;
    }

    /**
     * @param advNum
     *            the advNum to set
     */
    public void setAdvNum(int advNum) {
        this.advNum = advNum;
    }

    /**
     * @return the specialTAU
     */
    public int getSpecialTAU() {
        return specialTAU;
    }

    /**
     * @param specialTAU
     *            the specialTAU to set
     */
    public void setSpecialTAU(int specialTAU) {
        this.specialTAU = specialTAU;
    }

    /**
     * @return the awipsBinNum
     */
    public int getAwipsBinNum() {
        return awipsBinNum;
    }

    /**
     * @param awipsBinNum
     *            the awipsBinNum to set
     */
    public void setAwipsBinNum(int awipsBinNum) {
        this.awipsBinNum = awipsBinNum;
    }

    /**
     * @return the potentialTC
     */
    public boolean isPotentialTC() {
        return potentialTC;
    }

    /**
     * @param potentialTC
     *            the potentialTC to set
     */
    public void setPotentialTC(boolean potentialTC) {
        this.potentialTC = potentialTC;
    }

    /**
     * @return the forecaster
     */
    public String getForecaster() {
        return forecaster;
    }

    /**
     * @param forecaster
     *            the forecaster to set
     */
    public void setForecaster(String forecaster) {
        this.forecaster = forecaster;
    }

    /**
     * @return the daylightSaving
     */
    public boolean isDaylightSaving() {
        return daylightSaving;
    }

    /**
     * @param daylightSaving
     *            the daylightSaving to set
     */
    public void setDaylightSaving(boolean daylightSaving) {
        this.daylightSaving = daylightSaving;
    }

    /**
     * @return the pres
     */
    public int getPres() {
        return pres;
    }

    /**
     * @param pres
     *            the pres to set
     */
    public void setPres(int pres) {
        this.pres = pres;
    }

    /**
     * @return the centerAccuracy
     */
    public int getCenterAccuracy() {
        return centerAccuracy;
    }

    /**
     * @param centerAccuracy
     *            the centerAccuracy to set
     */
    public void setCenterAccuracy(int centerAccuracy) {
        this.centerAccuracy = centerAccuracy;
    }

    /**
     * @return the forecastType
     */
    public List<String> getForecastType() {
        return forecastType;
    }

    /**
     * @param forecastType
     *            the forecastType to set
     */
    public void setForecastType(List<String> forecastType) {
        this.forecastType = forecastType;
    }

    /**
     * @return the geoRef1
     */
    public GeographyPoint getGeoRef1() {
        return geoRef1;
    }

    /**
     * @param geoRef1
     *            the geoRef1 to set
     */
    public void setGeoRef1(GeographyPoint geoRef1) {
        this.geoRef1 = geoRef1;
    }

    /**
     * @return the geoRef2
     */
    public GeographyPoint getGeoRef2() {
        return geoRef2;
    }

    /**
     * @param geoRef2
     *            the geoRef2 to set
     */
    public void setGeoRef2(GeographyPoint geoRef2) {
        this.geoRef2 = geoRef2;
    }

    /**
     * @return the frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @param frequency
     *            the frequency to set
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the wwUS
     */
    public boolean isWwUS() {
        return wwUS;
    }

    /**
     * @param wwUS
     *            the wwUS to set
     */
    public void setWwUS(boolean wwUS) {
        this.wwUS = wwUS;
    }

    /**
     * @return the wwIntl
     */
    public boolean isWwIntl() {
        return wwIntl;
    }

    /**
     * @param wwIntl
     *            the wwIntl to set
     */
    public void setWwIntl(boolean wwIntl) {
        this.wwIntl = wwIntl;
    }

    /**
     * @return the finalAdv
     */
    public boolean isFinalAdv() {
        return finalAdv;
    }

    /**
     * @param finalAdv
     *            the finalAdv to set
     */
    public void setFinalAdv(boolean finalAdv) {
        this.finalAdv = finalAdv;
    }

    /**
     * @return the taus
     */
    public List<String> getTaus() {
        return taus;
    }

    /**
     * @param taus
     *            the taus to set
     */
    public void setTaus(List<String> taus) {
        this.taus = taus;
    }

    /**
     * Find the forecast type for a given TAU.
     *
     * @param tau
     *            the tau
     * @return Forecast type.
     */
    public String getForecastType(int tau) {
        String tauStr = String.format("%d", tau);
        String fcstType = "";
        List<String> useTaus = taus;
        if (taus.isEmpty()) {
            useTaus = DEFAULT_TAUS;
        }

        int index = useTaus.indexOf(tauStr);
        if (index >= 0 && index < forecastType.size()) {
            fcstType = forecastType.get(index);
        }

        return fcstType;
    }

    /**
     * Constructs a string representation of class in pre-defined format as in
     * stormId.adv file.
     *
     * @return String
     */
    @Override
    public String toString() {

        StringBuilder typeStr = new StringBuilder();
        for (String typ : forecastType) {
            typeStr.append(String.format("%-3s", typ));
        }

        StringBuilder tauStr = new StringBuilder();
        for (String tau : taus) {
            String fmt = "%-" + (tau.length() + 1) + "s";
            tauStr.append(String.format(fmt, tau));
        }
        StringBuilder sb = new StringBuilder();
        for (String str : HEADER) {
            sb.append(String.format("%s\n", str));
        }

        addTableItem(sb, "special", "%-3s", specialAdv ? "YES" : "NO");
        addTableItem(sb, "special_HHMM", "%4s", specialAdvTime);
        addTableItem(sb, "special_TAU", "%-3s", specialTAU);
        addTableItem(sb, "advisory#", "%-3d", advNum);
        addTableItem(sb, "AWIPS_bin#", "%-3d", awipsBinNum);
        addTableItem(sb, "forecaster", "%s", forecaster);
        addTableItem(sb, "daylight_saving", "%-3s", daylightSaving ? "YES" : "NO");
        addTableItem(sb, "pressure", "%-5d", pres);
        addTableItem(sb, "center_accuracy", "%-4d", centerAccuracy);
        addTableItem(sb, "forecast_type", "%s", typeStr);
        addTableItem(sb, "geography_ref1", "%s", geoRef1 == null ? "" : geoRef1.toString());
        addTableItem(sb, "geography_ref2", "%s", geoRef2 == null ? "" : geoRef2.toString());
        addTableItem(sb, "public_freq", "%-2d", frequency);
        addTableItem(sb, "final_advisory", "%-3s", finalAdv ? "YES" : "NO");
        addTableItem(sb, "ww_US", "%-3s", wwUS ? "YES" : "NO");
        addTableItem(sb, "ww_Intl", "%-3s", wwIntl ? "YES" : "NO");
        addTableItem(sb, "TAU_0_DTG", "%-10s", dtg);
        addTableItem(sb, "Potential_TC", "%-3s", potentialTC ? "YES" : "NO");
        addTableItem(sb, "TAUs", "%s", tauStr);

        return sb.toString();
    }

    /**
     * Construct an AdvisoryInfo instance from a list of information strings
     * read from the pre-defined format as in stormId.adv file.
     *
     * @param storm
     *            Storm
     *
     * @param List<String>
     *            Info read from stormId.adv
     * @param geoPoints
     *            GeographyPoints
     *
     * @return AdvisoryInfo
     */
    public static AdvisoryInfo construct(final Storm storm, List<String> info,
            final GeographyPoints geoPoints) {

        AdvisoryInfo advInfo = new AdvisoryInfo();
        advInfo.storm = storm;

        Map<String, String> infoMap = new HashMap<>();

        for (String str : info) {
            String[] tokens = str.split(INFO_SEPARATOR);
            if (tokens.length > 1) {
                infoMap.put(tokens[0].trim(), tokens[1].trim());
            }
        }

        String specialAdv = infoMap.get("special");
        if (specialAdv != null && "YES".equalsIgnoreCase(specialAdv)) {
            advInfo.setSpecialAdv(true);
        }

        String advTime = infoMap.get("special_HHMM");
        if (advTime != null) {
            advInfo.setSpecialAdvTime(advTime);
        }

        String specialTAU = infoMap.get("special_TAU");
        if (specialTAU != null) {
            int specTau = Integer.parseInt(specialTAU);
            advInfo.setSpecialTAU(specTau);
        }

        String advNum = infoMap.get("advisory#");
        if (advNum != null) {
            int num = Integer.parseInt(advNum);
            advInfo.setAdvNum(num);
        }

        /*
         * Note: The number is used as the last digit in product header. The
         * final numeric digit is assigned on a rotating basis by cyclone
         * number, i.e., WTNT31 KNHC would be used for the first, sixth, and
         * eleventh Atlantic system that NHC has written advisories on in a
         * given year, while WTNT32 KNHC would be used for the second, seventh,
         * or twelfth system, and so on.
         */
        String awipsBinNum = infoMap.get("AWIPS_bin#");
        int binNum = storm.getCycloneNum() % MAX_AWIPS_BIN_NUMBER;
        if (awipsBinNum != null) {
            binNum = Integer.parseInt(awipsBinNum);
        }
        advInfo.setAwipsBinNum(binNum);

        String forecaster = infoMap.get("forecaster");
        if (forecaster != null) {
            advInfo.setForecaster(forecaster);
        }

        String daylightSaving = infoMap.get("daylight_saving");
        if (daylightSaving != null && "YES".equalsIgnoreCase(daylightSaving)) {
            advInfo.setDaylightSaving(true);
        }

        String pres = infoMap.get("pressure");
        if (pres != null) {
            int num = Integer.parseInt(pres);
            advInfo.setPres(num);
        }

        String centerAccuracy = infoMap.get("center_accuracy");
        if (centerAccuracy != null) {
            int num = Integer.parseInt(centerAccuracy);
            advInfo.setCenterAccuracy(num);
        }

        String geoRef1 = infoMap.get("geography_ref1");
        if (geoRef1 != null) {
            GeographyPoint pt1 = geoPoints.getPointByDesp(geoRef1);
            advInfo.setGeoRef1(pt1);

        }

        String geoRef2 = infoMap.get("geography_ref2");
        if (geoRef2 != null) {
            GeographyPoint pt2 = geoPoints.getPointByDesp(geoRef2);
            advInfo.setGeoRef2(pt2);
        }

        String frequency = infoMap.get("public_freq");
        if (frequency != null) {
            int num = Integer.parseInt(frequency);
            advInfo.setFrequency(num);
        }

        String finalAdv = infoMap.get("final_advisory");
        if (finalAdv != null && "YES".equalsIgnoreCase(finalAdv)) {
            advInfo.setFinalAdv(true);
        }

        String wwUS = infoMap.get("ww_US");
        if (wwUS != null && "YES".equalsIgnoreCase(wwUS)) {
            advInfo.setWwUS(true);
        }

        String wwIntl = infoMap.get("ww_Intl");
        if (wwIntl != null && "YES".equalsIgnoreCase(wwIntl)) {
            advInfo.setWwIntl(true);
        }

        String dtg = infoMap.get("TAU_0_DTG");
        if (dtg != null) {
            advInfo.setDtg(dtg); // ???
        }

        String potentialTC = infoMap.get("Potential_TC");
        if (potentialTC != null && "YES".equalsIgnoreCase(potentialTC)) {
            advInfo.setPotentialTC(true);
        }

        String typeStr = infoMap.get("forecast_type");
        if (typeStr != null && !typeStr.trim().isEmpty()) {
            String[] types = typeStr.trim().split("\\s+");
            for (String type : types) {
                advInfo.getForecastType().add(type);
            }
        }

        String taus = infoMap.get("TAUs");
        if (taus != null && !taus.trim().isEmpty()) {
            String[] ts = taus.trim().split("\\s+");
            for (String tau : ts) {
                advInfo.getTaus().add(tau);
            }
        }

        return advInfo;
    }

    private static void addTableItem(StringBuilder sb, String heading, String format,
            Object value) {
        sb.append(String.format("%-15s: " + format, heading, value)).append('\n');
    }

}