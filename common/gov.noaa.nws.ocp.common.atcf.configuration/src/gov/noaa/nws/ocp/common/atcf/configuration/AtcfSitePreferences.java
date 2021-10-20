/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class holds ATCF site preference in atcfsite.prefs.xml, which is
 * converted from atcfsite.prefs (which is a copy of legacy ATCF site preference
 * file such as atcfsite.nam used for storing and saving site preferences).
 *
 * The format in original atcfsite.prefs (copy from atcfsite.nam) is as
 * following:
 *
 * <pre>
 * The file format is as follows:
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 * forecast-center:        OFCL
 * station-code:           KNHC
 * forecast-ctr(old):      OFCO
 * ddn-code:               ATCM
 * center-name:            NOT USED FOR NHC
 * map-area:               AREAOFOPERAT
 * editor:                 kwrite -graphicssystem raster 
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 12, 2019 59913      dmanzella   Created
 * Apr 04, 2019 62029      jwu         Add default/description for
 *                                          PreferenceOptions
 * May 20, 2019 63773      dmanzella   Minor bug fix
 * Sep 16, 2019 68603      jwu         Replace getBoolean() with parseBoolean().
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 * Mar 04, 2021 88931      jwu         Add forecast/outlook range.
 * Mar 24, 2021 88727      mporricelli Add tau limits for wind radii fcst
 * May 14, 2021 88584      wpaintsil   Correct property name.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "AtcfSitePreferences")
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfSitePreferences {

    private static final String VALUE_FALSE = "FALSE";

    // Header
    @DynamicSerializeElement
    @XmlElement(name = "Header", type = String.class)
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "Preference", type = AtcfSitePreferenceEntry.class) })
    private List<AtcfSitePreferenceEntry> preferences;

    /**
     * Constructor
     */
    public AtcfSitePreferences() {
        preferences = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param preferenceList
     *            List of preference entries
     */
    public AtcfSitePreferences(List<AtcfSitePreferenceEntry> preferenceList) {
        preferences = new ArrayList<>(preferenceList);
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Created to help interpret the preference string options
     */
    public enum PreferenceOptions {
        FORECASTCENTER("forecast-center", "Forecaster center", "OFCL"),
        STATIONCODE("station-code", "Station Code", "KNHC"),
        FORECASTCTR("forecast-ctr(old)", "Forecast center (old)", "OFCO"),
        DDNCODE(
                "ddn-code",
                "ATCF Requestor code for ATCF DDN messages to FNMOC",
                "ATCM"),
        CENTERNAME("center-name", "Forecast center name", "NOTUSEDFORNHC"),
        MAPAREA("map-area", "Default map area", "AREAOFOPERAT"),
        EDITOR("editor", "Text file editor", "kwrite-graphicssystemraster"),
        HIGHRESOLUTIONMAP(
                "high-resolution-map",
                "Higher resoluton map",
                "TRUE"),
        HIGHRESTHRESHOLD(
                "high-res-threshold(nm)",
                "Threshold value to use higher resolution map",
                "1000"),
        MAXZOOMLIMIT("max-zoom-limit(nm)", "Maximum zoom limit", "30"),
        COLORPRINTER("color-printer", "Color printer", "TRUE"),
        LOGO("logo", "Display logo", VALUE_FALSE),
        LOGOFILE("logofile", "Image logo file name", "noaalogo75.bmp"),
        LOGOPOSITION("logo-position", "Logo position", "upper right"),
        ATCFIDPOSITION("ATCF-id-position", "ATCF ID position", "lower right"),
        CENTERIDPOSITION(
                "center-id-position",
                "Center ID position",
                "lower right"),
        GRAPHICLABEL(
                "graphic-label",
                "Add a label to the top of ATCF graphic",
                "TRUE"),
        AUTORELOCATELABELS(
                "auto-relocate-labels",
                "Automatically relocate boxed labels when scaling",
                "TRUE"),
        RELOCATELABELOFFSET(
                "relocate-label-offset",
                "Number of characters to offset labels",
                "2"),
        LATLONLINEFREQ("latlon-line-freq", "Lat/lon line frequency", "1"),
        MAPLINESBOLD(
                "map-lines-bold",
                "Bold Lines (for printing, all aids)",
                "TRUE"),
        MAPLINESWIDTH("map-lines-width", "Width of map lines in pixels", "2"),
        GEOGRAPHYLABELS("geography-labels", "Geography Labels", VALUE_FALSE),
        MAPWINWIDTH(
                "map-win-width(pixels)",
                "Storm window width in pixels",
                "1024"),
        MAPWINHEIGHT(
                "map-win-height(pixels)",
                "storm window height in pixels",
                "768"),
        LOOPWINWIDTH(
                "loopwin-width(pixels)",
                "Two panel loop window width in pixels",
                "761"),
        LOOPWINHEIGHT(
                "loopwin-height(pixels)",
                "wo panel loop window height in pixels",
                "782"),
        PLOTTERLANDSCAPE(
                "plotter-landscape",
                "Plotter page orientation",
                "TRUE"),
        PLOTTERWIDTH(
                "plotter-width(inches)",
                "Plotter page width in inches",
                "40"),
        PLOTTERHEIGHT(
                "plotter-height(inches)",
                "Plotter page height in inches",
                "36"),
        BTRACKDASHNDOTSON(
                "btrack-dashndots-on",
                "Storm track as dots (TD), dashes (TS) and solid lines (TY or HU).",
                "TRUE"),
        BTRACKINTENSITIESON(
                "btrack-intensities-on",
                "Best track intensities.",
                VALUE_FALSE),
        BTRACKWINDRADIION(
                "btrack-windradii-on",
                "Best track wind radii.",
                "TRUE"),
        BTRACKSPECIALSTTYPE(
                "btrack-special-sttype",
                "SD, SS, LO, WV, ET, EX, PT, MD, at special storm type positions.",
                VALUE_FALSE),
        OLDFIXESGREYEDOUT(
                "old-fixes-greyed-out",
                "Grey out old fixes.",
                VALUE_FALSE),
        OLDFIXESTIMERANGE(
                "old-fixes-time-range",
                "Age of fixes to grey out.",
                "30"),
        NEWFIXDISPLAYONLY(
                "new-fix-display-only",
                "Display fixes within selected time range.",
                "TRUE"),
        NEWFIXTIMERANGE("new-fix-time-range", "Age of fixes for display.", "3"),
        CONFRADTIMERANGE(
                "conf-rad-time-range",
                "Time range for fix confidence circles.",
                "3"),
        FIXCONFRADIION(
                "fix-conf-radii-on",
                "Display fix confidence circles.",
                VALUE_FALSE),
        FIXWINDRADIION(
                "fix-windradii-on",
                "Display fix wind radii.",
                VALUE_FALSE),
        FIXRADTIMERANGE(
                "fix-rad-time-range",
                "Time range for fix wind radii.",
                "7"),
        FIXAUTOLABELON("fix-autolabel-on", "Autolabel fixes.", VALUE_FALSE),
        FIXAUTOLABTIMERANGE(
                "fix-autolab-time-range",
                "Time range for fix autolabels.",
                "30"),
        FIXSITELABELON(
                "fix-site-label-on",
                "Include site in fix label.",
                "TRUE"),
        FIXTNUMLABELON(
                "fix-tnum-label-on",
                "Include T Num and CI Num in fix label.",
                "TRUE"),
        FIXHIDENONCENTER(
                "fix-hide-noncenter",
                "Hide non-center fixes.",
                VALUE_FALSE),
        FIXNONCENTERNATIVE(
                "fix-noncenter-native",
                "Display non-center fixes in native fix color.",
                VALUE_FALSE),
        FIXSELECTDUPS(
                "fix-select-dups",
                "Have user select from multiple fixes for mouse-over. ",
                VALUE_FALSE),
        RIGHTMARGINSIZE(
                "right-margin-size",
                "Num of characters for labels in right margin.",
                "40"),
        BOGUSPRIORITYLEVELS(
                "bogus-priority-levels",
                "Priority levels for bogus/compuete.",
                "8"),
        USETAU60("useTAU60", "Allow forecast for TAU 60.", VALUE_FALSE),
        FORECAST_TAU_LABELS(
                "forecast_tau_labels",
                "Show TAU labels on obj aid tracks during forecast.",
                VALUE_FALSE),
        FORECAST_COMPLETE_TRAC(
                "forecast_complete_trac",
                "Show complete obj aid tracks during forecast.",
                VALUE_FALSE),
        FORECAST_CONSENSUS_AID(
                "forecast_consensus_aid",
                "Show concensus obj aid tracks during forecast.",
                "TRUE"),
        CONSENSUSAIDNAME(
                "consensusAidName",
                "Consencus aid default name for track.",
                "TVCN"),
        CONSENSUSINTNAME(
                "consensusIntName",
                "Consencus aid default name for intensity.",
                "IVCN"),
        CONSENSUSRADNAME(
                "consensusRadName",
                "Consencus aid default name for wind radii.",
                "RVCN"),
        AIDSLOOP(
                "aidsLoop",
                "Field loop on during forecast track.",
                VALUE_FALSE),
        AIDSPROB("aidsProb", "GPCE probability on.", VALUE_FALSE),
        AIDSPROBCLIM(
                "aidsProbClim",
                "GPCE climatology probability on.",
                VALUE_FALSE),
        AIDSGPCE_AX("aidsGPCE_AX", "GPCE climatology AX on.", "TRUE"),
        AIDSPEST("aidsPEST", "PEST probability aid on.", VALUE_FALSE),
        AIDSDASHESDOTS(
                "aidsDashesDots",
                "Aid line styles, solid tau 0-72, dashes 72-120, dots 120+.",
                "TRUE"),
        TECHLISTFILE("techlistfile", "Techlist file name.", "techlist.dat"),
        SYNOPTIC_DATA_RANGE(
                "synoptic_data_range",
                "Range (hours) to plot synoptic data.",
                "0"),
        SCATTEROMETER_RANGE(
                "scatterometer_range",
                "Range (hours) to plot scatterometer data.",
                "1"),
        CLOUD_TRACK_WIND_RANGE(
                "cloud_track_wind_range",
                "Range (hours) to plot cloud track wind range data.",
                "2"),
        RAOBPIBAL_RANGE(
                "raob/pibal_range",
                "Range (hours) to plot raob range data.",
                "3"),
        AIRCRAFT_REPORT_RANGE(
                "aircraft_report_range",
                "Range (hours) to plot a/c data.",
                "4"),
        ALT_SIG_WAVE_HT_RANGE(
                "alt_sig_wave_ht_range",
                "Range (hours) to plot altimeter sig wave heights.",
                "5"),

        DISPSTORMSYMBOLS(
                "disp-storm-symbols",
                "Display storm symbols.",
                VALUE_FALSE),
        DISPSTORMNUMBER(
                "disp-storm-number",
                "Display storm number.",
                VALUE_FALSE),
        DISPTRACKLINESLEGEND(
                "disp-tracklines-legend",
                "Display track lines legend...",
                VALUE_FALSE),
        BTRACKCOLORINTENSITY(
                "btrack-color-intensity",
                "Storm track in colors based on intensity (TD, TS, and TY/HU)",
                VALUE_FALSE),
        DISPTRACKCOLORLEG(
                "disp-track-color-leg",
                "Display track color legend.",
                VALUE_FALSE),
        STRACKCOLORSSSSCALE(
                "strack-colors-ss-scale",
                "Storm track in colors based on Saffir-Simpson scale (Cat 1 - Cat 5)",
                VALUE_FALSE),
        DISPSSCOLORLEGEND(
                "disp-ss-color-legend",
                "Display Saffir-Simpson color legend ...",
                VALUE_FALSE),
        DISPTRACKLABELS(
                "disp-track-labels",
                "Display tracks label ...",
                VALUE_FALSE),
        BTRACKLABELSON("btrack-labels-on", "Best track labels", VALUE_FALSE),
        OBJTRACK("obj-track", "Obj Track", "0"),
        OBJTRACKINT("obj-track-int", "Obj Track Int", "0"),
        OBJTRACKRADII("obj-track-radii", "Obj Track Radii", "0"),
        FCSTTRACKLABELTYPE(
                "fcstLabelSolidDashDot",
                "Forecast Track label type:",
                "solid"),
        ADVISORYFORECASTRANGE(
                "advisory-fcst-range",
                "Ending TAU for forecast in advisories",
                "72"),
        ADVISORYOUTLOOKRANGE(
                "advisory-outlook-range",
                "Ending TAU for outlook in advisories",
                "120"),
        TAU_LIMIT_64KT_WINDRADII(
                "tauLimit64KtWindRadii",
                "Highest TAU for 64kt wind radii fcst",
                "48"),
        TAU_LIMIT_ALL_WINDRADII(
                "tauLimitAllWindRadii",
                "Highest TAU for all wind radii fcst",
                "72");

        private final String name;

        private final String description;

        private final String dflt;

        private PreferenceOptions(String name, String desc, String dflt) {
            this.name = name;
            this.description = desc;
            this.dflt = dflt;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return the default value
         */
        public String getDefault() {
            return dflt;
        }

        public boolean equalsName(String otherName) {
            return name.equals(otherName);
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * A series of getters to return the correct data type for each
     * PreferenceOption
     * 
     * @return
     */
    public String getForecastCenter() {
        return getPreference(PreferenceOptions.FORECASTCENTER.toString())
                .getValue();
    }

    public String getStationCode() {
        return getPreference(PreferenceOptions.STATIONCODE.toString())
                .getValue();
    }

    public String getForecastCtr() {
        return getPreference(PreferenceOptions.FORECASTCTR.toString())
                .getValue();
    }

    public String getDdnCode() {
        return getPreference(PreferenceOptions.DDNCODE.toString()).getValue();
    }

    public String getCenterName() {
        return getPreference(PreferenceOptions.CENTERNAME.toString())
                .getValue();
    }

    public String getMapArea() {
        return getPreference(PreferenceOptions.MAPAREA.toString()).getValue();
    }

    public String getEditor() {
        return getPreference(PreferenceOptions.EDITOR.toString()).getValue();
    }

    public boolean getHighResolutionMap() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.HIGHRESOLUTIONMAP.toString())
                        .getValue());
    }

    public int getHighResThreshold() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.HIGHRESTHRESHOLD.toString())
                        .getValue());
    }

    public int getMaxZoomLimit() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.MAXZOOMLIMIT.toString())
                        .getValue());
    }

    public boolean getColorPrinter() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.COLORPRINTER.toString())
                        .getValue());
    }

    public boolean getLogo() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.LOGO.toString()).getValue());
    }

    public String getLogoFile() {
        return getPreference(PreferenceOptions.LOGOFILE.toString()).getValue();
    }

    public String getLogoPosition() {
        return getPreference(PreferenceOptions.LOGOPOSITION.toString())
                .getValue();
    }

    public String getAtcfIdPosition() {
        return getPreference(PreferenceOptions.ATCFIDPOSITION.toString())
                .getValue();
    }

    public String getCenterIdPosition() {
        return getPreference(PreferenceOptions.CENTERIDPOSITION.toString())
                .getValue();
    }

    public boolean getGraphicLabel() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.GRAPHICLABEL.toString())
                        .getValue());
    }

    public boolean getAutoRelocateLabels() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.AUTORELOCATELABELS.toString())
                        .getValue());
    }

    public int getRelocateLabelOffset() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.RELOCATELABELOFFSET.toString())
                        .getValue());
    }

    public int getLatlonLineFreq() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.LATLONLINEFREQ.toString())
                        .getValue());
    }

    public boolean getMapLinesBold() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.MAPLINESBOLD.toString())
                        .getValue());
    }

    public int getMapLinesWidth() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.MAPLINESWIDTH.toString())
                        .getValue());
    }

    public boolean getGeographyLabels() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.GEOGRAPHYLABELS.toString())
                        .getValue());
    }

    public int getMapWinWidth() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.MAPWINWIDTH.toString())
                        .getValue());
    }

    public int getMapWinHeight() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.MAPWINHEIGHT.toString())
                        .getValue());
    }

    public int getLoopWinWidth() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.LOOPWINWIDTH.toString())
                        .getValue());
    }

    public int getLoopWinHeight() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.LOOPWINHEIGHT.toString())
                        .getValue());
    }

    public boolean getPlotterLandscape() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.PLOTTERLANDSCAPE.toString())
                        .getValue());
    }

    public int getPlotterWidth() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.PLOTTERWIDTH.toString())
                        .getValue());
    }

    public int getPlotterHeight() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.PLOTTERHEIGHT.toString())
                        .getValue());
    }

    public boolean getBtrackDashnDotsOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKDASHNDOTSON.toString())
                        .getValue());
    }

    public boolean getBtrackIntensitiesOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKINTENSITIESON.toString())
                        .getValue());
    }

    public boolean getBtrackWindRadiiOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKWINDRADIION.toString())
                        .getValue());
    }

    public boolean getBtrackSpecialStype() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKSPECIALSTTYPE.toString())
                        .getValue());
    }

    public boolean getOldFixesGreyedOut() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.OLDFIXESGREYEDOUT.toString())
                        .getValue());
    }

    public int getOldFixesTimeRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.OLDFIXESTIMERANGE.toString())
                        .getValue());
    }

    public boolean getNewFixDisplayOnly() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.NEWFIXDISPLAYONLY.toString())
                        .getValue());
    }

    public int getNewFixTimeRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.NEWFIXTIMERANGE.toString())
                        .getValue());
    }

    public int getConfRadTimeRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.CONFRADTIMERANGE.toString())
                        .getValue());
    }

    public boolean getFixConfRadiiOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXCONFRADIION.toString())
                        .getValue());
    }

    public boolean getFixWindRadiiOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXWINDRADIION.toString())
                        .getValue());
    }

    public int getFixRadTimeRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.FIXRADTIMERANGE.toString())
                        .getValue());
    }

    public boolean getFixAutolabelOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXAUTOLABELON.toString())
                        .getValue());
    }

    public int getFixAutolabelTimeRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.FIXAUTOLABTIMERANGE.toString())
                        .getValue());
    }

    public boolean getFixSiteLabelOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXSITELABELON.toString())
                        .getValue());
    }

    public boolean getFixTnumLabelOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXTNUMLABELON.toString())
                        .getValue());
    }

    public boolean getFixHideNoncenter() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXHIDENONCENTER.toString())
                        .getValue());
    }

    public boolean getFixNoncenterNative() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXNONCENTERNATIVE.toString())
                        .getValue());
    }

    public boolean getFixSelectDups() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FIXSELECTDUPS.toString())
                        .getValue());
    }

    public int getRightMarginSize() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.RIGHTMARGINSIZE.toString())
                        .getValue());
    }

    public int getBogusPriorityLevels() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.BOGUSPRIORITYLEVELS.toString())
                        .getValue());
    }

    public boolean getUseTau60() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.USETAU60.toString())
                        .getValue());
    }

    public boolean getForecastTauLabels() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.FORECAST_TAU_LABELS.toString())
                        .getValue());
    }

    public boolean getForecastCompleteTrack() {
        return Boolean.parseBoolean(getPreference(
                PreferenceOptions.FORECAST_COMPLETE_TRAC.toString())
                        .getValue());
    }

    public boolean getForecastConsensusAid() {
        return Boolean.parseBoolean(getPreference(
                PreferenceOptions.FORECAST_CONSENSUS_AID.toString())
                        .getValue());
    }

    public String getConsensusAidName() {
        return getPreference(PreferenceOptions.CONSENSUSAIDNAME.toString())
                .getValue();
    }

    public String getConsensusIntName() {
        return getPreference(PreferenceOptions.CONSENSUSINTNAME.toString())
                .getValue();
    }

    public String getConsensusRadName() {
        return getPreference(PreferenceOptions.CONSENSUSRADNAME.toString())
                .getValue();
    }

    public String getFcstTrackLabelType() {
        return getPreference(PreferenceOptions.FCSTTRACKLABELTYPE.toString())
                .getValue();
    }

    public boolean getAidsProb() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.AIDSPROB.toString())
                        .getValue());
    }

    public boolean getAidsProbClim() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.AIDSPROBCLIM.toString())
                        .getValue());
    }

    public boolean getAidsGpceAX() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.AIDSGPCE_AX.toString())
                        .getValue());
    }

    public boolean getAidsPest() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.AIDSPEST.toString())
                        .getValue());
    }

    public boolean getAidsDashesDots() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.AIDSDASHESDOTS.toString())
                        .getValue());
    }

    public String getTechListFile() {
        return getPreference(PreferenceOptions.TECHLISTFILE.toString())
                .getValue();
    }

    public int getSynopticDataRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.SYNOPTIC_DATA_RANGE.toString())
                        .getValue());
    }

    public int getScatterometerRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.SCATTEROMETER_RANGE.toString())
                        .getValue());
    }

    public int getCloudTrackWindRange() {
        return Integer.parseInt(getPreference(
                PreferenceOptions.CLOUD_TRACK_WIND_RANGE.toString())
                        .getValue());
    }

    public int getRaobPibalRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.RAOBPIBAL_RANGE.toString())
                        .getValue());
    }

    public int getAircraftReportRange() {
        return Integer.parseInt(getPreference(
                PreferenceOptions.AIRCRAFT_REPORT_RANGE.toString()).getValue());
    }

    public int getAltSigWaveHtRange() {
        return Integer.parseInt(getPreference(
                PreferenceOptions.ALT_SIG_WAVE_HT_RANGE.toString()).getValue());
    }

    public boolean getBtrackLabelsOn() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKLABELSON.toString())
                        .getValue());
    }

    public boolean getDispTrackLabels() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.DISPTRACKLABELS.toString())
                        .getValue());
    }

    public boolean getDispSSColorLegend() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.DISPSSCOLORLEGEND.toString())
                        .getValue());
    }

    public boolean getStrackColorsSSScale() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.STRACKCOLORSSSSCALE.toString())
                        .getValue());
    }

    public boolean getDispTracklinesLegend() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.DISPTRACKLINESLEGEND.toString())
                        .getValue());
    }

    public boolean getBtrackColorIntensity() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKCOLORINTENSITY.toString())
                        .getValue());
    }

    public boolean getBtrackSolidDashDot() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.BTRACKDASHNDOTSON.toString())
                        .getValue());
    }

    public boolean getDispStormNumber() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.DISPSTORMNUMBER.toString())
                        .getValue());
    }

    public boolean getDispStormSymbols() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.DISPSTORMSYMBOLS.toString())
                        .getValue());
    }

    public boolean getDispTrackColorLegend() {
        return Boolean.parseBoolean(
                getPreference(PreferenceOptions.DISPTRACKCOLORLEG.toString())
                        .getValue());
    }

    public int getObjTrack() {
        return Integer
                .parseInt(getPreference(PreferenceOptions.OBJTRACK.toString())
                        .getValue());
    }

    public int getObjTrackInt() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.OBJTRACKINT.toString())
                        .getValue());
    }

    public int getObjTrackRadii() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.OBJTRACKRADII.toString())
                        .getValue());
    }

    public int getAdvisoryForecastRange() {
        return Integer.parseInt(getPreference(
                PreferenceOptions.ADVISORYFORECASTRANGE.toString()).getValue());
    }

    public int getAdvisoryOutlookRange() {
        return Integer.parseInt(
                getPreference(PreferenceOptions.ADVISORYOUTLOOKRANGE.toString())
                        .getValue());
    }

    public int getTauLimit64ktWindRadii() {
        return Integer.parseInt(getPreference(
                PreferenceOptions.TAU_LIMIT_64KT_WINDRADII.toString())
                        .getValue());
    }

    public int getTauLimitAllWindRadii() {
        return Integer.parseInt(getPreference(
                PreferenceOptions.TAU_LIMIT_ALL_WINDRADII.toString())
                        .getValue());
    }

    /**
     * @return the preferences
     */
    public List<AtcfSitePreferenceEntry> getPreferences() {
        return preferences;
    }

    /**
     * Get all preferences' names.
     *
     * @return all preferences' names
     */
    public String[] getPreferenceNames() {
        List<String> pref = new ArrayList<>();
        for (AtcfSitePreferenceEntry st : preferences) {
            pref.add(st.getName());
        }

        return pref.toArray(new String[pref.size()]);
    }

    /**
     * Get all the preferences' values
     * 
     * @return all the preferences' values
     */
    public String[] getPreferenceValues() {
        List<String> pref = new ArrayList<>();
        for (AtcfSitePreferenceEntry st : preferences) {
            pref.add(st.getValue());
        }

        return pref.toArray(new String[pref.size()]);

    }

    /**
     * @param preferences
     *            the preferences to set
     */
    public void setPreferences(List<AtcfSitePreferenceEntry> preferences) {
        this.preferences = preferences;
    }

    /**
     * Find the preference with the given name.
     *
     * @return the preference
     */
    public AtcfSitePreferenceEntry getPreference(String prefName) {
        AtcfSitePreferenceEntry pref = null;
        for (AtcfSitePreferenceEntry st : preferences) {
            if (st.getName().equals(prefName)) {
                pref = st;
                break;
            }
        }

        return pref;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * atcfsite.nam
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        sb.append(newline);
        for (AtcfSitePreferenceEntry fs : preferences) {
            sb.append(String.format("%-24s", fs.getName() + ":"));

            sb.append(String.format("%s", fs.getValue()));

            sb.append(newline);
        }

        return sb.toString();
    }

}