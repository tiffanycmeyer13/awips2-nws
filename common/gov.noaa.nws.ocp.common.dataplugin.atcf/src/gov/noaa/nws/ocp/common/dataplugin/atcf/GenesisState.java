/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Enumeration of genesis state - GENEISIS, TC, ARCHIVE.
 *
 * Note: When a genesis is spawned into a storm (TC), it will be automatically
 * ended with its end time being set.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 14, 2020 86027      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
@DynamicSerialize
public enum GenesisState {
    /**
     * Genesis state: GENESIS, TC, END, ARCHIVE
     */
    GENESIS, TC, END, ARCHIVE;
}
