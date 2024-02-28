/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Enumeration of storm state - GENEISIS, METWATCH, WARNING,or ARCHIVE. Note
 * that "GENESIS" is only used for geneses.
 *
 * Note - "TCFA" is removed since it looks like it is never used for any storms
 * since 1851.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2020 82623      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
@DynamicSerialize
public enum StormState {
    // Note: GENESIS is only used for geneses.
    GENESIS, METWATCH, WARNING, ARCHIVE;
}
