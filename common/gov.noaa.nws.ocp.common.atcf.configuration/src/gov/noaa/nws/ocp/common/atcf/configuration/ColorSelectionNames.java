/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represents the color selection names in colsel.dat, which are used
 * for map display (MAPDSPLY), printer (PRINTER_COLORS), and plotter
 * (PLOTTER_COLORS).
 *
 * <pre>
 *
 * This file defines the colors for the MAP DISPLAY program.
 * The objective aid colors are specified in the techlist.dat file.
 * The color numbers are defined as follows:
 *  Refer to AtcfCustomColors (colortable.dat) for the color number definitions.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  MAPDSPLY
 *  background   : 1
 *  ocean        :31
 *  ....
 *  PRINTER_COLORS
 *  background   : 1
 *  ocean        :31
 *  ....
 *
 *  PLOTTER_COLORS
 *  background   : 1
 *  ocean        :31
 *  ....
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 #52692     jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ColorSelectionNames")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum ColorSelectionNames {

    BACKGROUND("background"),
    OCEAN("ocean"),
    OCEAN2("ocean2"),
    OCEAN3("ocean3"),
    LAND("land"),
    COASTLINES("map/coastlnes"),
    LATLONLINES("latlonlines"),
    TITLES("titles"),
    ATCF_ID("ATCF_ID"),
    LABEL("label"),
    PROMPT_STATUS("prompt/status"),
    FORECAST("forecast"),
    FORECAST_LABEL("forecast_labl"),
    TCFA_BOX("tcfa_box"),
    OVERLAY("overly_colors"),
    TRACK("track_colors"),
    WIND_RAD_34("34_wind_rad"),
    WIND_RAD_50("50_wind_rad"),
    WIND_RAD_64("64_wind_rad"),
    WIND_RAD_100("100_wind_rad"),
    RE_BEST_TRACK("re-best_track"),
    OBJ_BEST_TRACK("obj_best_trk"),
    OLD_OBJ_BEST_TRACK("old_obj_bt"),
    DANGER_AREA("danger_area"),
    TWELVE_FOOT_SEA("12_ft_seas"),
    RMW("RMW"),
    ROCI("ROCI"),
    AIRCRAFT("aircraft"),
    SATELLITE("satellite"),
    OBJ_DVORAK("obj_dvorak"),
    MICROWAVE("microwave"),
    RADAR("radar"),
    SYNOPTIC("synoptic"),
    SCATTEROMETER("scatterometer"),
    DROPSONDE("dropsonde"),
    RESEARCH_FIX("research_fix"),
    NONCENTER_FIX("noncenter_fix"),
    FLAGGED_FIX("flagged_fix"),
    OLD_FIX("old_fix"),
    RAW_DATA_WND0("raw_data_wnd0"),
    RAW_DATA_WND1("raw_data_wnd1"),
    RAW_DATA_WND2("raw_data_wnd2"),
    RAW_DATA_WND3("raw_data_wnd3"),
    PRESYNOPTIC("presynoptic"),
    SYNOPTIC_OB("synoptic_ob"),
    POSTSYNOPTIC("postsynoptic"),
    PREUPPERAIR("preupperair"),
    UPPERAIR("upperair"),
    POSTUPPERAIR("postupperair"),
    PRE_AC_OB("preAC_ob"),
    CURRENT_AC_OB("currentAC_ob"),
    POST_AC_OB("postAC_ob"),
    CAT_1("cat-1"),
    CAT_2("cat-2"),
    CAT_3("cat-3"),
    CAT_4("cat-4"),
    CAT_5("cat-5");

    @DynamicSerializeElement
    private String name;

    /**
     * @param iValue
     */
    private ColorSelectionNames(final String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}