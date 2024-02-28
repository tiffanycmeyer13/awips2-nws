/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

/**
 * Define major types for the drawable elements.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public enum DrawableType {
    LINE,
    SYMBOL,
    KINKLINE,
    TEXT,
    ARC,
    TRACK,
    VECTOR,
    ANY,
    COMBO_SYMBOL,
    TCA,
    TCM_FCST
}
