/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim;

/**
 * Bulk data for ODIM records
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMStoredData {
    public static final String RAW_DATA_ID = "Data";

    public static final String SHORT_DATA_ID = "ShortData";

    public static final String ANGLE_DATA_ID = "Angles";

    private byte[] rawData;

    private short[] rawShortData;

    private float[] angleData;

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public short[] getRawShortData() {
        return rawShortData;
    }

    public void setRawShortData(short[] rawShortData) {
        this.rawShortData = rawShortData;
    }

    public float[] getAngleData() {
        return angleData;
    }

    public void setAngleData(float[] angleData) {
        this.angleData = angleData;
    }
}
