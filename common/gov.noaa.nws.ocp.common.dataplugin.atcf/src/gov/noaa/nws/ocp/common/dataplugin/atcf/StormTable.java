/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;

/**
 * Class to hold all storms information in a map.
 *
 * <pre>
 *
 * A storm is uniquely defined by Basin + year + cycloneNum
 *
 * Each storm has one descriptive record which is a brief summary of the storm
 * history including storm name, track type, maximum development level, etc.
 *
 * The information is stored in the file storms.txt for all active storms, and
 * storms.archive (storm.table) for all storms for a selected year.
 *
 * The data format is as follows:
 *
 * STORM NAME,RE,X,R2,R3,R4,R5,CY,YYYY,TY,I,YYY1MMDDHH,YYY2MMDDHH,SIZE,GENESIS_NUM,PAR1,PAR2,PRIORITY,STORM_STATE,WT_NUMBER,STORMID
 * where
 *     STORM NAME = Literal storm name, "INVEST", or "GENESISxxx" where xxx is a number
 *     RE = Region (basin) code: WP, IO, SH, CP, EP, AL, LS.
 *     X =  Subregion code: W, A, B, S, P, C, E, L, Q.
 *     R2 = Region 2 code: WP, IO, SH, CP, or EP. This and R3-R5 are codes for basins entered
 *                         subsequent to the original basin where the storm was generated.
 *     R3 = Region 3 code: WP, IO, SH, CP, or EP.
 *     R4 = Region 4 code: WP, IO, SH, CP, or EP.
 *     R5 = Region 5 code: WP, IO, SH, CP, or EP.
 *     CY = Annual cyclone number: 01 through 99.
 *     YYYY = Cyclone Year: 0000 through 9999.
 *     TY = Highest level of tc development: TD, TS, TY, ST, TC, HU, SH, XX (unknown).
 *     I = S, R, O; straight mover, recurver, odd mover.
 *     YYY1MMDDHH = Starting DTG: 0000010100 through 9999123123.
 *     YYY2MMDDHH = Ending DTG: 0000010100 through 9999123123.
 *     SIZE = Storm Size (MIDG (midget) , GIAN (giant), etc.).
 *     GENESIS_NUM = Annual genesis number: 001 through 999.
 *     PAR1 = UNUSED.
 *     PAR2 = UNUSED.
 *     PRIORITY = Priority for model runs (e.g., GFDN, GFDL, COAMPS-TC, H-WRF): 1-9.
 *     STORM_STATE = Storm state: METWATCH,TCFA,WARNING or ARCHIVE
 *     WT_NUMBER = Minute of warning or TCFA (00-59)
 *     STORMID = Storm ID composed of basin designator and annual cyclone number (e.g. WP081993)
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 01, 2020 82623      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class StormTable {

    /**
     * Map of storms in storm.table - from year 1851 to present. Key is storm ID
     * (i.e., AL112017)
     */
    @DynamicSerializeElement
    private static Map<String, Storm> stormTable;

    /**
     * Retrieve the storm map from storm.table (1851 ~ present).
     *
     * @return Map<String, Storm>
     */
    public static synchronized Map<String, Storm> getStormTable() {

        if (stormTable == null) {
            List<String> stormEntries = AtcfConfigurationManager.getInstance()
                    .loadStormTable();

            stormTable = new LinkedHashMap<>();

            for (String entry : stormEntries) {
                Storm storm = Storm.build(entry);
                if (storm != null) {
                    stormTable.put(storm.getStormId().toUpperCase(), storm);
                }
            }
        }

        return stormTable;
    }

    /**
     * Retrieve the storm with the storm ID.
     *
     * @param stormId
     *            storm ID
     *
     * @return Storm
     */
    public static Storm getStorm(String stormId) {
        return getStormTable().get(stormId);
    }

    /**
     * Add a storm into the table.
     *
     * @param stormId
     *            storm ID
     *
     * @return Storm
     */
    public static Storm addStorm(String stormId, Storm storm) {
        return getStormTable().put(stormId, storm);
    }

}
