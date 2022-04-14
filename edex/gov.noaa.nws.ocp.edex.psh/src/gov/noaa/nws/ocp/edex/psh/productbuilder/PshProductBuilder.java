/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.productbuilder;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.StringUtil;

import gov.noaa.nws.ocp.common.dataplugin.psh.EffectDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.FloodingDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.IssueType;
import gov.noaa.nws.ocp.common.dataplugin.psh.MarineDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.MetarDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.NonMetarDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallStormData;
import gov.noaa.nws.ocp.common.dataplugin.psh.TornadoDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.WaterLevelDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.response.PshProductServiceResponse;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigHeader;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;

/**
 * Build final PSH Report for a given storm.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 JUL 2017  #35738     jwu         Initial creation
 * 07 JUL 2017  #37098     jwu         Use PshData object.
 * 12 SEP 2017  #37365     jwu         Add some format changes.
 * 31 SEP 2017  #40221     jwu         Tune rainfall/tornado/flooding format.
 * 01 NOV 2017  #40359     jwu         Use one template for mixed-case & upper case product.
 * 08,NOV 2017  #40423     jwu         Replace tide/surge with water level.
 * 16,NOV 2017  #40987     jwu         Add length limits to location/county etc.
 * 20,NOV 2017  #41127     jwu         Tune final format to match legacy.
 * 23 JAN 2016  DCS19326   wpaintsil   Baseline version
 * 09 JUN 2021  DCS20652   wkwock      Change mixed case from capitalize to define by user, Lat/lon use 4 digit decimal
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class PshProductBuilder {

    /**
     * Plugin ID for alerts.
     */
    public static final String PLUGIN_ID = "PshProductBuilder";

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshProductBuilder.class);

    /**
     * Velocity template file.
     */
    private static final String VM_TEMPLATE = "/reportTemplates/psh_product_template.vm";

    private static final String VM_TEMPLATE_PREVIEW = "/reportTemplates/psh_preview_template.vm";

    /**
     * The maximum length for a single line in the report
     */
    private static final int MAX_LINE_LENGTH = 68;

    /**
     * Starting string for "Included Counties".
     */
    private static final String COUNTY_START = "COUNTIES INCLUDED";

    /**
     * Starting string for remarks.
     */
    private static final String REMARK_START = "REMARKS:";

    /**
     * Separator for "Included Counties".
     */
    private static final String COUNTY_SEPARATOR = "...";

    /**
     * Separator for normal word list.
     */
    private static final String WORD_SEPARATOR = " ";

    /**
     * Constructor.
     */
    public PshProductBuilder() {
    }

    /**
     * Builds a PSH text product from a PshData object.
     * 
     * @param pshData
     *            A PshData object
     * 
     * @return PshProductServiceResponse
     */
    public PshProductServiceResponse buildPshProduct(PshData pshData) {

        return new PshProductServiceResponse(generatePshProduct(pshData));
    }

    /**
     * Builds a preview PSH Data for a particular tab from a PshData object.
     * 
     * @param pshData
     *            A PshData object
     * 
     * @return PshProductServiceResponse
     */
    public PshProductServiceResponse buildPshPreview(PshData pshData,
            PshDataCategory type) {

        List<String> content = generatePshPreview(pshData, type);

        StringBuilder totalContents = new StringBuilder();
        for (String line : content) {
            totalContents.append(line).append(StringUtil.NEWLINE);
        }

        return new PshProductServiceResponse(totalContents.toString());
    }

    /**
     * Update the string to upper case if mixedCase is false.
     * 
     * @param string
     * @param mixedCase
     * @return
     */
    private static String updateCase(String string, boolean mixedCase) {
        if (!mixedCase) {
            return string.toUpperCase();
        }

        return string;
    }

    private static List<Map<String, String>> metarData(
            List<MetarDataEntry> metarDataList) {
        List<Map<String, String>> metarData = new ArrayList<>();

        if (metarDataList != null) {
            for (MetarDataEntry metarInfo : metarDataList) {
                MetarDataEntry metarEntry = metarInfo;

                Map<String, String> metarEntryMap = new HashMap<>();

                metarEntryMap.put("station", metarEntry.getSite());

                String latlon = String.format("%-5.2f %-7.2f",
                        metarEntry.getLat(), metarEntry.getLon());
                metarEntryMap.put("latlon", String.format("%-13s", latlon));

                metarEntryMap.put("mslp",
                        String.format("%-7s", metarEntry.getMinSeaLevelPres()));
                metarEntryMap.put("mslpDat", String.format("%-7s",
                        metarEntry.getMinSeaLevelPresTime()));
                metarEntryMap.put("mslpInp", String.format("%1s",
                        metarEntry.getMinSeaLevelComplete()));

                metarEntryMap.put("sustWnd",
                        String.format("%-8s", metarEntry.getSustWind()));
                metarEntryMap.put("sustWndDat",
                        String.format("%-7s", metarEntry.getSustWindTime()));
                metarEntryMap.put("sustWndInp",
                        String.format("%1s", metarEntry.getSustWindComplete()));

                metarEntryMap.put("pkWnd",
                        String.format("%-7s", metarEntry.getPeakWind()));
                metarEntryMap.put("pkWndDat",
                        String.format("%-7s", metarEntry.getPeakWindTime()));
                metarEntryMap.put("pkWndInp",
                        String.format("%1s", metarEntry.getPeakWindComplete()));

                metarData.add(metarEntryMap);
            }
        }
        return metarData;
    }

    private static List<Map<String, String>> nonMetarData(
            List<NonMetarDataEntry> nonmetarDataList) {
        List<Map<String, String>> nonMetarData = new ArrayList<>();

        if (nonmetarDataList != null) {
            for (NonMetarDataEntry nonmetarInfo : nonmetarDataList) {

                NonMetarDataEntry nonmetarEntry = nonmetarInfo;

                Map<String, String> nonMetarEntryMap = new HashMap<>();

                nonMetarEntryMap.put("station", nonmetarEntry.getSite());
                String latlon = String.format("%-5.2f %-7.2f",
                        nonmetarEntry.getLat(), nonmetarEntry.getLon());
                nonMetarEntryMap.put("latlon", String.format("%-13s", latlon));
                nonMetarEntryMap.put("mslp", String.format("%-7s",
                        nonmetarEntry.getMinSeaLevelPres()));
                nonMetarEntryMap.put("mslpDat", String.format("%-7s",
                        nonmetarEntry.getMinSeaLevelPresTime()));

                nonMetarEntryMap.put("mslpInp", String.format("%1s",
                        nonmetarEntry.getMinSeaLevelComplete()));

                String estWind = nonmetarEntry.getEstWind();

                nonMetarEntryMap.put("sustWnd",
                        String.format("%-8s", nonmetarEntry.getSustWind()));
                nonMetarEntryMap.put("sustWndDat",
                        String.format("%-7s", nonmetarEntry.getSustWindTime()));
                String inpSust = estWind;
                String inpPeak = estWind;
                if (!"E".equals(estWind)) {
                    inpSust = nonmetarEntry.getSustWindComplete();
                    inpPeak = nonmetarEntry.getPeakWindComplete();
                }

                nonMetarEntryMap.put("sustWndInp",
                        String.format("%1s", inpSust));

                nonMetarEntryMap.put("pkWnd",
                        String.format("%-7s", nonmetarEntry.getPeakWind()));
                nonMetarEntryMap.put("pkWndDat",
                        String.format("%-7s", nonmetarEntry.getPeakWindTime()));

                nonMetarEntryMap.put("pkWndInp", String.format("%1s", inpPeak));

                String anemHgmet = "";
                if (nonmetarEntry.getAnemHgmt() != null
                        && !nonmetarEntry.getAnemHgmt().isEmpty()) {
                    anemHgmet = String.format("%38s",
                            nonmetarEntry.getAnemHgmt());
                }
                nonMetarEntryMap.put("anemHgmt", anemHgmet);

                nonMetarData.add(nonMetarEntryMap);
            }
        }
        return nonMetarData;
    }

    private static List<Map<String, String>> marineData(
            List<MarineDataEntry> marineDataList) {
        List<Map<String, String>> marineData = new ArrayList<>();
        if (marineDataList != null) {
            for (MarineDataEntry marineInfo : marineDataList) {

                MarineDataEntry marineEntry = marineInfo;

                Map<String, String> marineEntryMap = new HashMap<>();

                marineEntryMap.put("station", marineEntry.getSite());
                String latlon = String.format("%-7.4f %-9.4f",
                        marineEntry.getLat(), marineEntry.getLon());
                marineEntryMap.put("latlon", String.format("%-13s", latlon));
                marineEntryMap.put("mslp", String.format("%-7s",
                        marineEntry.getMinSeaLevelPres()));
                marineEntryMap.put("mslpDat", String.format("%-7s",
                        marineEntry.getMinSeaLevelPresTime()));
                marineEntryMap.put("mslpInp", String.format("%1s",
                        marineEntry.getMinSeaLevelComplete()));

                marineEntryMap.put("sustWnd",
                        String.format("%-8s", marineEntry.getSustWind()));
                marineEntryMap.put("sustWndDat",
                        String.format("%-7s", marineEntry.getSustWindTime()));
                marineEntryMap.put("sustWndInp", String.format("%1s",
                        marineEntry.getSustWindComplete()));

                marineEntryMap.put("pkWnd",
                        String.format("%-7s", marineEntry.getPeakWind()));
                marineEntryMap.put("pkWndDat",
                        String.format("%-7s", marineEntry.getPeakWindTime()));
                marineEntryMap.put("pkWndInp", String.format("%1s",
                        marineEntry.getPeakWindComplete()));

                String anemHgmet = "";
                if (marineEntry.getAnemHgmt() != null
                        && !marineEntry.getAnemHgmt().isEmpty()) {
                    anemHgmet = String.format("%38s",
                            marineEntry.getAnemHgmt());
                }
                marineEntryMap.put("anemHgmt", anemHgmet);

                marineData.add(marineEntryMap);
            }
        }
        return marineData;
    }

    private static List<Map<String, String>> rainfallData(
            List<RainfallDataEntry> rainfallDataList, boolean mixedCase) {
        List<Map<String, String>> rainfallData = new ArrayList<>();

        if (rainfallDataList != null) {
            for (RainfallDataEntry rainInfo : rainfallDataList) {

                RainfallDataEntry rainEntry = rainInfo;
                PshCity city = rainEntry.getCity();
                Map<String, String> rainfallEntryMap = new HashMap<>();
                float dist = rainEntry.getDistance();
                String dir = rainEntry.getDirection().trim();
                String cityInfo = updateCase(city.getName(), mixedCase);

                if (dist > 0 && dir.length() > 0
                        && !dir.equalsIgnoreCase("None")) {
                    cityInfo = String.format("%d %s %s", Math.round(dist), dir,
                            cityInfo);
                }

                rainfallEntryMap.put("cityInfo",
                        String.format("%-28.24s", cityInfo));

                String county = updateCase(city.getCounty(), mixedCase);

                rainfallEntryMap.put("county",
                        String.format("%-19.19s", county));
                rainfallEntryMap.put("id",
                        String.format("%-12.12s", city.getStationID()));
                rainfallEntryMap.put("lat",
                        String.format("%-7.4f", city.getLat()));
                rainfallEntryMap.put("lon",
                        String.format("%9.4f", city.getLon()));
                rainfallEntryMap.put("rainfall",
                        String.format("%5.2f", rainEntry.getRainfall()));

                rainfallEntryMap.put("inp",
                        String.format("%1s", rainEntry.getIncomplete()));

                rainfallData.add(rainfallEntryMap);
            }
        }
        return rainfallData;
    }

    private static List<Map<String, String>> floodingData(
            List<FloodingDataEntry> floodingDataList, boolean mixedCase) {
        List<Map<String, String>> floodingData = new ArrayList<>();
        if (floodingDataList != null) {
            for (FloodingDataEntry floodingInfo : floodingDataList) {

                FloodingDataEntry floodingEntry = floodingInfo;

                // Add flooding data entries to the list
                Map<String, String> floodingEntryMap = new HashMap<>();

                String county = floodingEntry.getCounty().trim();
                county = updateCase(county, mixedCase);

                String flooding = county + COUNTY_SEPARATOR
                        + floodingEntry.getRemarks();
                floodingEntryMap.put("flooding", formatString(flooding,
                        MAX_LINE_LENGTH, null, WORD_SEPARATOR));

                floodingData.add(floodingEntryMap);
            }
        }
        return floodingData;

    }

    private static List<Map<String, String>> waterLevelData(
            List<WaterLevelDataEntry> waterLevelDataList, boolean mixedCase) {
        List<Map<String, String>> waterLevelData = new ArrayList<>();
        if (waterLevelDataList != null) {
            for (WaterLevelDataEntry waterLevelEntry : waterLevelDataList) {

                PshCity city = waterLevelEntry.getLocation();

                Map<String, String> waterLevelEntryMap = new HashMap<>();
                waterLevelEntryMap.put("id",
                        String.format("%-5.5s", city.getStationID()));

                String location = updateCase(city.getName(), mixedCase);
                waterLevelEntryMap.put("location",
                        String.format("%-15.15s", location));

                String county = updateCase(city.getCounty(), mixedCase);
                waterLevelEntryMap.put("county",
                        String.format("%-12.12s", county));

                waterLevelEntryMap.put("state",
                        String.format("%-2.2s", city.getState()));
                waterLevelEntryMap.put("waterLevel", String.format("%5.2f",
                        waterLevelEntry.getWaterLevel()));
                waterLevelEntryMap.put("datum",
                        String.format("%-6.6s", waterLevelEntry.getDatum()));
                waterLevelEntryMap.put("dt",
                        String.format("%-7s", waterLevelEntry.getDatetime()));
                waterLevelEntryMap.put("source",
                        String.format("%-6.6s", waterLevelEntry.getSource()));

                waterLevelEntryMap.put("inp",
                        String.format("%1s", waterLevelEntry.getIncomplete()));

                waterLevelEntryMap.put("lat",
                        String.format("%7.4f", city.getLat()));
                waterLevelEntryMap.put("lon",
                        String.format("%9.4f", city.getLon()));

                waterLevelData.add(waterLevelEntryMap);
            }
        }
        return waterLevelData;
    }

    private static List<Map<String, String>> tornadoData(
            List<TornadoDataEntry> tornadoDataList, boolean mixedCase) {

        List<Map<String, String>> tornadoData = new ArrayList<>();

        if (tornadoDataList != null) {
            for (TornadoDataEntry tornadoInfo : tornadoDataList) {

                TornadoDataEntry tornadoEntry = tornadoInfo;
                PshCity city = tornadoEntry.getLocation();

                Map<String, String> tornadoEntryMap = new HashMap<>();
                float dist = tornadoEntry.getDistance();
                String dir = tornadoEntry.getDirection().trim();
                String location = updateCase(city.getName(), mixedCase);
                if (dist > 0 && dir.length() > 0
                        && !dir.equalsIgnoreCase("None")) {
                    location = String.format("%d %s %s", Math.round(dist), dir,
                            location);
                }

                tornadoEntryMap.put("location",
                        String.format("%-28.25s", location));

                String county = updateCase(city.getCounty(), mixedCase);
                tornadoEntryMap.put("county",
                        String.format("%-16.16s", county));

                tornadoEntryMap.put("lat",
                        String.format("%5.2f", city.getLat()));
                tornadoEntryMap.put("lon",
                        String.format("%6.2f", city.getLon()));
                tornadoEntryMap.put("dt",
                        String.format("%-16s", tornadoEntry.getDatetime()));

                String magn = tornadoEntry.getMagnitude();
                if ("N/A".equals(magn)) {
                    magn = "";
                }
                tornadoEntryMap.put("efScale", String.format("%-3.3s", magn));
                tornadoEntryMap.put("inp",
                        String.format("%2s", tornadoEntry.getIncomplete()));
                tornadoEntryMap.put("tornadoDesp",
                        formatString(tornadoEntry.getRemarks(), MAX_LINE_LENGTH,
                                null, WORD_SEPARATOR));

                tornadoData.add(tornadoEntryMap);
            }
        }
        return tornadoData;
    }

    private static List<Map<String, String>> effectData(
            List<EffectDataEntry> effectDataList, boolean mixedCase) {

        List<Map<String, String>> effectData = new ArrayList<>();
        if (effectDataList != null) {
            for (EffectDataEntry effectInfo : effectDataList) {

                EffectDataEntry effectEntry = effectInfo;
                Map<String, String> effectsEntryMap = new HashMap<>();

                String county = updateCase(effectEntry.getCounty(), mixedCase);
                effectsEntryMap.put("county",
                        String.format("%-20.16s", county));

                effectsEntryMap.put("deaths",
                        String.format("%-17d", effectEntry.getDeaths()));
                effectsEntryMap.put("injuries",
                        String.format("%-21d", effectEntry.getInjuries()));
                effectsEntryMap.put("evacs",
                        String.format("%-5d", effectEntry.getEvacuations()));
                effectsEntryMap.put("desp",
                        formatString(effectEntry.getRemarks(), MAX_LINE_LENGTH,
                                null, WORD_SEPARATOR));

                effectData.add(effectsEntryMap);
            }
        }
        return effectData;

    }

    private <T> String getCountyType(List<T> dataList) {
        // get county type
        String countyType = "   County    ";
        for (Object obj : dataList) {
            String state = "";
            if (obj instanceof WaterLevelDataEntry) {
                state = ((WaterLevelDataEntry) obj).getLocation().getState();
            } else if (obj instanceof TornadoDataEntry) {
                state = ((TornadoDataEntry) obj).getLocation().getState();
            } else if (obj instanceof RainfallDataEntry) {
                state = ((RainfallDataEntry) obj).getCity().getState();
            }
            if ("LA".equalsIgnoreCase(state) || "MS".equalsIgnoreCase(state)) {
                countyType = "County/Parish";
                break;
            } else if ("VA".equalsIgnoreCase(state)) {
                countyType = " County/City ";
            }
        }
        return countyType;

    }

    /**
     * Build a preview with VM template.
     * 
     * Note: In legacy preview, mixed case is used for header while all upper
     * case is used for data entries.
     * 
     * @param dataRecords
     * @param type
     * @return
     */
    private List<String> generatePshPreview(PshData pshData,
            PshDataCategory type) {

        // Get configuration.
        PshConfigHeader headerInfo = PshConfigurationManager.getInstance()
                .getConfigHeader();

        // Check if "mixed case" is required.
        boolean mixedCase = headerInfo.isUseMixedCase();

        List<String> lines = new ArrayList<>();

        // Velocity engine and context
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        ve.init();

        VelocityContext context = new VelocityContext();

        switch (type) {

        case METAR:
            /**
             * List of METAR/non-METAR/Marine entries.
             * 
             * <pre>
             * Sample entry/Layout:
             * 
             * LOCATION  ID    MIN    DATE/     MAX      DATE/     PEAK    DATE/    
             * LAT  LON        PRES   TIME      SUST     TIME      GUST    TIME 
             * DEG DECIMAL     (MB)   (UTC)     (KT)     (UTC)     (KT)    (UTC)
             * 
             * KBKV-BROOKSVILLE FL 
             * 28.47 -82.45   1007.5 02/0222 I 210/033  02/0815 I 210/046 02/0815 I
             *
             * Variables in VM template:
             * 
             * $d.station $d.id
             * $d.lat $d.lon $d.mslp $d.mslpDat $d.mslpInp $d.maxSustWnd $d.maxSustWndDat $d.maxSustWndInp $d.maxPkGst $d.maxPkGstDat $d.maxPkGstInp
             * 
             * Note: only non-METAR/Marine have anemometer height. And it appears under MIN PRES if existing.
             * 
             * </pre>
             */
            List<MetarDataEntry> metarDataList = pshData.getMetar().getData();

            context.put("metarList", metarData(metarDataList));

            break;

        // non-METAR
        case NON_METAR:
            List<NonMetarDataEntry> nonmetarDataList = pshData.getNonmetar()
                    .getData();
            context.put("nonMetarList", nonMetarData(nonmetarDataList));

            break;

        // Marine.
        case MARINE:
            List<MarineDataEntry> marineDataList = pshData.getMarine()
                    .getData();
            context.put("marineList", marineData(marineDataList));

            break;

        case RAINFALL:
            /**
             * Storm rainfall entries.
             * 
             * <pre>
             * Sample entry/layout:
             * 
             * CITY/TOWN                    COUNTY               ID         RAINFALL
             * LAT LON                                                       (IN)   
             *
             * 1 ESE TARPON SPRINGS         PINELLAS            LAKE TAR     22.36 I
             * 28.13  -82.74
             * 
             * Variables in VM template:
             * 
             * $d.cityInfo $d.county $id $rainfall $inp
             * $d.lat  $d.lon
             * </pre>
             */
            RainfallStormData rainfall = pshData.getRainfall();
            List<RainfallDataEntry> rainfallDataList = rainfall.getData();

            // Rainfall period like "FROM 2100 UTC AUG 30 UNTIL 1600 UTC SEP 02"
            List<String> rainPeriod = getRainfallPeriod(rainfall);
            context.put("rainStart", rainPeriod.get(0));
            context.put("rainEnd", rainPeriod.get(1));
            context.put("rainfallList",
                    rainfallData(rainfallDataList, mixedCase));
            String countyType = getCountyType(rainfallDataList);
            context.put("rainCounty", countyType);

            break;

        case FLOODING:
            /**
             * Storm inland flooding entries.
             * 
             * <pre>
             * Sample entry:
             * 
             * PINELLAS...CARS STALLED AT 54TH AVENUE AND I-275 DUE TO EXCESS
             * RUNOFF AND HIGH WATER.      
             * 
             * Variables in VM template:
             * 
             * $d.flooding
             * </pre>
             */
            List<FloodingDataEntry> floodingDataList = pshData.getFlooding()
                    .getData();
            context.put("floodingList",
                    floodingData(floodingDataList, mixedCase));
            break;

        case WATER_LEVEL:
            /**
             * Storm surge/tide entries.
             * 
             * <pre>
             * Sample entry/layout:
             * 
             * ID     CITY/TOWN       COUNTY      STATE  WL DATUM  DATE/   SOURCE        
             *        OR LOCATION                       (FT)       TIME
             * ---------------------------------------------------------------------
             *        CEDAR KEY       LEVY         FL  4.41 MINOR  10/1010 NOS    I
             * 26.3500  -82.4100
             *         
             * Variables in VM template:
             * 
             *$d.id  $d.location $d.county $d.state $d.waterLevel $d.datum $d.dt $d.source $d.inp
             *$d.lat $d.lon
             * </pre>
             */
            List<WaterLevelDataEntry> waterLevelDataList = pshData
                    .getWaterLevel().getData();
            context.put("wlList",
                    waterLevelData(waterLevelDataList, mixedCase));

            // get county type
            countyType = getCountyType(waterLevelDataList);
            context.put("wlCounty", countyType);

            break;

        case TORNADO:
            /**
             * List of tornadoes entries.
             * 
             * <pre>
             * Sample entry/layout:
             * 
             * (DIST)CITY/TOWN              COUNTY           DATE/         EF SCALE 
             * LAT LON (DEG DECIMAL)                         TIME(UTC)    (IF KNOWN)
             * DESCRIPTION                                                          
             * 
             * 105 SSE BASKIN               LEE              02/1011          EF2 I
             * 26.48  -82.17
             *
             * Variables in VM template:
             * 
             * $d.location $d.county $d.dt $d.efScale $d.inp
             * $d.lat  $d.lon
             * </pre>
             */
            List<TornadoDataEntry> tornadoDataList = pshData.getTornado()
                    .getData();
            context.put("tornadoList", tornadoData(tornadoDataList, mixedCase));
            countyType = getCountyType(tornadoDataList);
            context.put("tCounty", countyType);
            break;

        case EFFECT:
            /**
             * Storm effects entries.
             * 
             * <pre>
             * Sample entry/layout:
             * 
             * COUNTY            DEATHS           INJURIES           EVACUATIONS  
             * DESCRIPTION
             * 
             * PINELLAS            20                20                  20    
            
             * CARS STALLED AT 54TH AVENUE AND I-275 DUE TO EXCESS RUNOFF AND HIGH
             * WATER. 
             *
             * Variables in VM template:
             * 
             * $d.county $d.deaths $d.injuries $d.evacs
             * $d.desp
             * </pre>
             */
            List<EffectDataEntry> effectDataList = pshData.getEffect()
                    .getData();
            context.put("impactList", effectData(effectDataList, mixedCase));
            break;

        case UNKNOWN:
            break;

        }

        context.put("tabType", type.getName());
        /*
         * Parse and format to generate report.
         */
        Template vmTemp;
        try {

            vmTemp = ve.getTemplate(VM_TEMPLATE_PREVIEW);

            /* now render the template into a Writer */
            StringWriter writer = new StringWriter();
            vmTemp.merge(context, writer);

            lines.add(writer.toString());

        } catch (VelocityException e) {
            logger.error("PshProductBuilder could not find template file "
                    + VM_TEMPLATE_PREVIEW, e);

            String action = "handle";

            if (e instanceof ResourceNotFoundException) {
                action = "find";
            } else if (e instanceof ParseErrorException) {
                action = "parse";
            } else if (e instanceof MethodInvocationException) {
                action = "invoke";
            }

            logger.error("PshProductBuilder could not " + action
                    + " template file " + VM_TEMPLATE_PREVIEW, e);
        }

        return lines;
    }

    /**
     * Build a product with VM template.
     * 
     * @param dataRecords
     * @param update
     * @return
     */
    private String generatePshProduct(PshData pshData) {

        // Get configuration.
        PshConfigHeader headerInfo = PshConfigurationManager.getInstance()
                .getConfigHeader();

        // Check if "mixed case" is required.
        boolean mixedCase = headerInfo.isUseMixedCase();

        List<String> lines = new ArrayList<>();

        // Velocity engine and context
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        ve.init();

        VelocityContext context = new VelocityContext();

        /*
         * Build storm content
         * 
         * "stormType" includes: Tropical Depression, Tropical Storm, Hurricane,
         * and Subtropical Storm. For "Tropical Depression", when a number (1 to
         * 20) is selected, it will become the storm's name.
         */
        String stormType = "";
        if (pshData.getStormType() != null) {
            stormType = pshData.getStormType().getDesc();
        }
        context.put("stormType", stormType);

        String stormName = "";
        if (pshData.getStormName() != null) {
            stormName = pshData.getStormName();
        }

        if (pshData.getStormNumber() > 0) {
            stormName = "Number " + pshData.getStormNumber();
        }

        stormName = WordUtils.capitalizeFully(stormName);

        context.put("stormName", stormName);

        // "status" will be "UPDATED", "CORRECTED" or "" if no update.
        String status = "";
        String pshStatus = pshData.getStatus();

        if (pshStatus != null && (pshStatus.equals(IssueType.UPDATED)
                || pshStatus.equals(IssueType.CORRECTED))) {
            status = "..." + WordUtils.capitalizeFully(pshStatus);
        }
        context.put("status", status);

        // station/site
        String station = WordUtils
                .capitalizeFully(headerInfo.getProductStation());
        context.put("reportStation", station);

        // Report date/time (current time as "408 PM EDT WED SEP 7 2016")
        context.put("reportTime",
                headerInfo.getTimeZone().getZonedTimeString());

        /*
         * County List - (1) each line should not exceed 68 characters; (2) the
         * first line needs to start with "COUNTIES INCLUDED"; (3) no trailing
         * "..." after last county.
         */
        List<String> pshCnty = pshData.getIncludedCounties();
        List<String> includedCnty = new ArrayList<>();
        for (String cnty : pshCnty) {
            includedCnty.add(cnty);
        }

        String counties = formatString(includedCnty, MAX_LINE_LENGTH,
                COUNTY_START, COUNTY_SEPARATOR);

        context.put("counties", counties);

        /**
         * Report update info.
         * 
         * The update/correction will list amended first then corrected. The new
         * amended/corrected info should be inserted at the right order. Each
         * line should not exceed 68 characters, otherwise it should be wrapped.
         * 
         * JUL 27...UPDATED FOR...REASON
         *
         * JUL 27...UPDATED FOR...REASON
         * 
         * JUL 28...CORRECTED FOR...REASON
         * 
         */
        List<String> updateInfoData = pshData.getUpdateInfoData();
        List<String> updateInfo = new ArrayList<>();
        for (String updt : updateInfoData) {
            updateInfo.add(
                    formatString(updt, MAX_LINE_LENGTH, null, WORD_SEPARATOR));
        }

        context.put("updateList", updateInfo);

        /**
         * List of METAR/non-METAR/Marine entries.
         * 
         * <pre>
         * Sample entry/Layout:
         * 
         * LOCATION  ID    MIN    DATE/     MAX      DATE/     PEAK    DATE/    
         * LAT  LON        PRES   TIME      SUST     TIME      GUST    TIME 
         * DEG DECIMAL     (MB)   (UTC)     (KT)     (UTC)     (KT)    (UTC)
         * 
         * KBKV-BROOKSVILLE FL 
         * 28.47 -82.45   1007.5 02/0222 I 210/033  02/0815 I 210/046 02/0815 I
         *
         * Variables in VM template:
         * 
         * $d.station $d.id
         * $d.lat $d.lon $d.mslp $d.mslpDat $d.mslpInp $d.maxSustWnd $d.maxSustWndDat $d.maxSustWndInp $d.maxPkGst $d.maxPkGstDat $d.maxPkGstInp
         * 
         * Note: only non-METAR/Marine have anemometer height. And it appears under MIN PRES if existing.
         * 
         * </pre>
         */
        List<MetarDataEntry> metarDataList = pshData.getMetar().getData();
        context.put("metarList", metarData(metarDataList));

        String metarRmk = pshData.getMetar().getRemarks();
        context.put("metarRmk", formatString(metarRmk, MAX_LINE_LENGTH,
                REMARK_START, WORD_SEPARATOR));

        // non-METAR
        List<NonMetarDataEntry> nonmetarDataList = pshData.getNonmetar()
                .getData();

        context.put("nonMetarList", nonMetarData(nonmetarDataList));

        String nonmetarRmk = pshData.getNonmetar().getRemarks();
        context.put("nonMetarRmk", formatString(nonmetarRmk, MAX_LINE_LENGTH,
                REMARK_START, WORD_SEPARATOR));

        // Marine.
        List<MarineDataEntry> marineDataList = pshData.getMarine().getData();
        context.put("marineList", marineData(marineDataList));

        String marineRmk = pshData.getMarine().getRemarks();
        context.put("marineRmk", formatString(marineRmk, MAX_LINE_LENGTH,
                REMARK_START, WORD_SEPARATOR));

        /**
         * Storm rainfall entries.
         * 
         * <pre>
         * Sample entry/layout:
         * 
         * CITY/TOWN                    COUNTY               ID         RAINFALL
         * LAT LON                                                       (IN)   
         *
         * 1 ESE TARPON SPRINGS         PINELLAS            LAKE TAR     22.36 I
         * 28.13  -82.74
         * 
         * Variables in VM template:
         * 
         * $d.cityInfo $d.county $id $rainfall $inp
         * $d.lat  $d.lon
         * </pre>
         */
        RainfallStormData rainfall = pshData.getRainfall();
        List<RainfallDataEntry> rainfallDataList = rainfall.getData();

        // Rainfall period like "FROM 2100 UTC AUG 30 UNTIL 1600 UTC SEP 02"
        List<String> rainPeriod = getRainfallPeriod(rainfall);
        context.put("rainStart", rainPeriod.get(0));
        context.put("rainEnd", rainPeriod.get(1));

        context.put("rainfallList", rainfallData(rainfallDataList, mixedCase));

        String rainfallRmk = pshData.getRainfall().getRemarks();
        context.put("rainfallRmk", formatString(rainfallRmk, MAX_LINE_LENGTH,
                REMARK_START, WORD_SEPARATOR));
        String countyType = getCountyType(rainfallDataList);
        context.put("rainCounty", countyType);

        /**
         * Storm inland flooding entries.
         * 
         * <pre>
         * Sample entry:
         * 
         * PINELLAS...CARS STALLED AT 54TH AVENUE AND I-275 DUE TO EXCESS
         * RUNOFF AND HIGH WATER.      
         * 
         * Variables in VM template:
         * 
         * $d.flooding
         * </pre>
         */
        List<FloodingDataEntry> floodingDataList = pshData.getFlooding()
                .getData();

        context.put("floodingList", floodingData(floodingDataList, mixedCase));

        /**
         * Storm maximum water level entries.
         * 
         * <pre>
         * Sample entry/layout:
         * 
         * ID     CITY/TOWN       COUNTY      STATE  WL DATUM  DATE/   SOURCE        
         *        OR LOCATION                       (FT)       TIME
         * ---------------------------------------------------------------------
         *        CEDAR KEY       LEVY         FL  4.41 MINOR  10/1010 NOS    I
         * 26.3500  -82.4100
         *         
         * Variables in VM template:
         * 
         *$d.id  $d.location $d.county $d.state $d.waterLevel $d.datum $d.dt $d.source $d.inp
         *$d.lat $d.lon
         * </pre>
         */
        List<WaterLevelDataEntry> waterLevelDataList = pshData.getWaterLevel()
                .getData();
        context.put("wlList", waterLevelData(waterLevelDataList, mixedCase));
        countyType = getCountyType(waterLevelDataList);
        context.put("wlCounty", countyType);

        String waterLevelRmk = pshData.getWaterLevel().getRemarks();
        context.put("wlRmk", formatString(waterLevelRmk, MAX_LINE_LENGTH,
                REMARK_START, WORD_SEPARATOR));

        /**
         * List of tornadoes entries.
         * 
         * <pre>
         * Sample entry/layout:
         * 
         * (DIST)CITY/TOWN              COUNTY           DATE/         EF SCALE 
         * LAT LON (DEG DECIMAL)                         TIME(UTC)    (IF KNOWN)
         * DESCRIPTION                                                          
         * 
         * 105 SSE BASKIN               LEE              02/1011          EF2 I
         * 26.48  -82.17
         *
         * Variables in VM template:
         * 
         * $d.location $d.county $d.dt $d.efScale $d.inp
         * $d.lat  $d.lon
         * </pre>
         */
        List<TornadoDataEntry> tornadoDataList = pshData.getTornado().getData();

        context.put("tornadoList", tornadoData(tornadoDataList, mixedCase));
        countyType = getCountyType(tornadoDataList);
        context.put("tCounty", countyType);

        /**
         * Storm effects entries.
         * 
         * <pre>
         * Sample entry/layout:
         * 
         * COUNTY            DEATHS           INJURIES           EVACUATIONS  
         * DESCRIPTION
         * 
         * PINELLAS            20                20                  20    
        
         * CARS STALLED AT 54TH AVENUE AND I-275 DUE TO EXCESS RUNOFF AND HIGH
         * WATER. 
         *
         * Variables in VM template:
         * 
         * $d.county $d.deaths $d.injuries $d.evacs
         * $d.desp
         * </pre>
         */
        List<EffectDataEntry> effectDataList = pshData.getEffect().getData();
        context.put("impactList", effectData(effectDataList, mixedCase));

        String forecaster = "";
        if (pshData.getForecaster() != null) {
            forecaster = WordUtils.capitalizeFully(pshData.getForecaster());
        }
        context.put("forecaster", forecaster);

        /*
         * Parse and format to generate report.
         */
        Template vmTemp;
        try {

            vmTemp = ve.getTemplate(VM_TEMPLATE);

            /* now render the template into a Writer */
            StringWriter writer = new StringWriter();
            vmTemp.merge(context, writer);

            String product = writer.toString();

            /*
             * If mixed case is not required, use all upper cases except for
             * legend part.
             */
            if (!mixedCase) {
                product = product.toUpperCase();
                product = matchLegend(product);
            }

            lines.add(product);

        } catch (ResourceNotFoundException ne) {
            logger.error("PshProductBuilder could not find template file "
                    + VM_TEMPLATE, ne);
        } catch (ParseErrorException pe) {
            logger.error("PshProductBuilder could not parse template file "
                    + VM_TEMPLATE, pe);
        } catch (MethodInvocationException me) {
            logger.error("PshProductBuilder could not invoke template file "
                    + VM_TEMPLATE, me);
        }

        return String.join(StringUtil.NEWLINE, lines) + StringUtil.NEWLINE;
    }

    /**
     * Format a string into lines of fixed-length with a given starter string
     * and a word separator.
     * 
     * @param inStr
     *            String to be formatted.
     * @param lineLength
     *            Maximum length of each line
     * @param startStr
     *            A starter string that will be added to the beginning of
     *            "inStr".
     * @param wordSep
     *            separator to delimit between words in the final string.
     * @return
     */
    private static String formatString(String inStr, int lineLength,
            String startStr, String wordSep) {

        /*
         * Split into words (white space or line separator delimited).
         */
        String patStr = "\\s*(\\s|\\n)\\s*";

        final Pattern pat = Pattern.compile(patStr);
        final String[] items = pat.split(inStr);

        return formatString(Arrays.asList(items), lineLength, startStr,
                wordSep);
    }

    /**
     * Format a list of strings (words) into lines of fixed-length with given
     * started string and word separator.
     * 
     * @param inStr
     *            List of strings (words) to be formatted.
     * @param lineLength
     *            Maximum length of each line
     * @param startStr
     *            A start string that will be added to the beginning of "inStr".
     * @param wordSep
     *            separator to delimit between words in the final string.
     * 
     * @return
     */
    private static String formatString(List<String> inStr, int lineLength,
            String startStr, String wordSep) {

        StringBuilder stb = new StringBuilder();
        List<String> includedWords = new ArrayList<>();

        String sep = " ";
        if (wordSep != null && !wordSep.isEmpty()) {
            sep = wordSep;
        }

        // Add starting string at the beginning.
        int startInd = 0;
        if (startStr != null && !startStr.isEmpty()) {
            includedWords.add(startStr);
            startInd = startStr.length() + sep.length();
        }
        includedWords.addAll(inStr);

        // Create lines and add new line separator.
        int ii = 0;
        String wordLine = "";

        for (String cts : includedWords) {

            String newWordLine = wordLine + cts;
            if (ii == 0 || (ii < includedWords.size() - 1)) {
                newWordLine += sep;
            }

            if (newWordLine.trim().length() > lineLength) {
                stb.append(wordLine.trim() + StringUtil.NEWLINE);
                wordLine = cts + sep;
            } else {
                wordLine = newWordLine;
            }

            if (ii >= (includedWords.size() - 1)) {
                stb.append(wordLine.trim());
            }

            ii++;
        }

        // In case.
        if (startInd > stb.length()) {
            startInd = stb.length();
        }

        // Remove the starting string.
        String finalStr = stb.substring(startInd).toString();

        return finalStr;

    }

    /**
     * Get the start/end time strings for rainfall, e.g, "2100 UTC AUG 30".
     * 
     * @param rainfall
     *            Rainfall storm data
     * @return start and end period string
     */
    private List<String> getRainfallPeriod(RainfallStormData rainfall) {

        Calendar currentCal = TimeUtil.newCalendar();

        String startMonth = rainfall.getStartMon();
        String startDay = rainfall.getStartDay();
        String startHour = rainfall.getStartHour();

        String endMonth = rainfall.getEndMon();
        String endDay = rainfall.getEndDay();
        String endHour = rainfall.getEndHour();

        SimpleDateFormat sdfMon = new SimpleDateFormat("MMM", Locale.ENGLISH);
        if (startMonth == null || startMonth.trim().length() != 3) {
            startMonth = sdfMon.format(currentCal.getTime());
        }

        if (endMonth == null || endMonth.trim().length() != 3) {
            endMonth = sdfMon.format(currentCal.getTime());
        }

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd", Locale.ENGLISH);
        if (startDay == null || startDay.trim().length() == 0) {
            startDay = sdfDay.format(currentCal.getTime());
        }

        if (endDay == null || endDay.trim().length() == 0) {
            endDay = sdfDay.format(currentCal.getTime());
        }

        if (startHour == null || startHour.trim().length() == 0) {
            startHour = "0000";
        }

        if (endHour == null || startHour.trim().length() == 0) {
            endHour = "0000";
        }

        // Rainfall periods "FROM 2100 UTC AUG 30 UNTIL 1600 UTC SEP 02"
        String rainStart = String.format("%4s UTC %3s %2s", startHour,
                startMonth, startDay);
        String rainEnd = String.format("%4s UTC %3s %2s", endHour, endMonth,
                endDay);

        List<String> periodList = new ArrayList<>();
        periodList.add(rainStart);

        periodList.add(rainEnd);

        return periodList;
    }

    /**
     * Matches legend part with legacy when mixed case is not in use. Legacy is
     * using all upper case for final product but the legend lines are using
     * mixed case.
     * 
     * @param product
     *            Product string to be checked
     * @return string
     */
    private String matchLegend(String product) {
        String outStr = product;
        if (product != null && !product.isEmpty()) {
            outStr = outStr.replace("LEGEND:", "Legend:");
            outStr = outStr.replace("I-INCOMPLETE DATA", "I-Incomplete Data");
            outStr = outStr.replace("E-ESTIMATED", "E-Estimated");
        }

        return outStr;
    }

}