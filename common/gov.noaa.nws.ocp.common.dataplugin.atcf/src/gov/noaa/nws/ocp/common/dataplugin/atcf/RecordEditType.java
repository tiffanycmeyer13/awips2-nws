/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 * Indicates deck record edit operation type.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 25, 2019 61590      dfriedman   Initial creation
 * Aug 05, 2019 66888      pwang       Add "UNDO".
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public enum RecordEditType {
    NEW, MODIFY, DELETE, UNDO;
}