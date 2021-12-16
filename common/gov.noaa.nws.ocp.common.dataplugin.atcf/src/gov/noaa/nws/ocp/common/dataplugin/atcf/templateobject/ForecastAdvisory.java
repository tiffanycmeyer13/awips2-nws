/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * An Object for holding Forecast Advisory data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 29, 2020 81820       wpaintsil   Initial creation.
 * Jan 26, 2021 86746       jwu         Add/remove a few fields.
 * Feb 12, 2021 87783       jwu         Overhaul data representation.
 * Feb 22, 2021 87781       jwu         Extends from AtcfAdvisory.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
@DynamicSerialize
public class ForecastAdvisory extends AtcfAdvisory {

    /**
     * Center accuracy
     */
    @DynamicSerializeElement
    private String accuracy;

    /**
     * Storm eye diameter
     */
    @DynamicSerializeElement
    private String eyeSize;

    /**
     * Storm direction
     */
    @DynamicSerializeElement
    private String direction;

    /**
     * Storm direction in degrees
     */
    @DynamicSerializeElement
    private String degrees;

    /**
     * Storm speed in Kt
     */
    @DynamicSerializeElement
    private String movementKt;

    /**
     * Cyclone minimum sea level pressure
     */
    @DynamicSerializeElement
    private String pressureMb;

    /**
     * Average track error for day 4
     */
    @DynamicSerializeElement
    private String avgTrkErr4Day;

    /**
     * Average track error for day 5
     */
    @DynamicSerializeElement
    private String avgTrkErr5Day;

    /**
     * Average intensity error
     */
    @DynamicSerializeElement
    private String avgIntensityErr;

    /**
     * Next intermediate public advisory's header.
     */
    @DynamicSerializeElement
    private String intermPubHeader;

    /**
     * Constructor
     */
    public ForecastAdvisory() {
        super();

        setType(AdvisoryType.TCM);
        this.accuracy = "";
        this.eyeSize = "";
        this.direction = "";
        this.degrees = "";
        this.movementKt = "";
        this.pressureMb = "";
        this.avgTrkErr4Day = "";
        this.avgTrkErr5Day = "";
        this.avgIntensityErr = "";
        this.intermPubHeader = "";
    }

    /**
     * @return the accuracy
     */
    public String getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * @return the eyeSize
     */
    public String getEyeSize() {
        return eyeSize;
    }

    /**
     * @param eyeSize the eyeSize to set
     */
    public void setEyeSize(String eyeSize) {
        this.eyeSize = eyeSize;
    }

    /**
     * @return the direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * @return the degrees
     */
    public String getDegrees() {
        return degrees;
    }

    /**
     * @param degrees the degrees to set
     */
    public void setDegrees(String degrees) {
        this.degrees = degrees;
    }

    /**
     * @return the movementKt
     */
    public String getMovementKt() {
        return movementKt;
    }

    /**
     * @param movementKt the movementKt to set
     */
    public void setMovementKt(String movementKt) {
        this.movementKt = movementKt;
    }

    /**
     * @return the pressureMb
     */
    public String getPressureMb() {
        return pressureMb;
    }

    /**
     * @param pressureMb the pressureMb to set
     */
    public void setPressureMb(String pressureMb) {
        this.pressureMb = pressureMb;
    }

    /**
     * @return the avgTrkErr4Day
     */
    public String getAvgTrkErr4Day() {
        return avgTrkErr4Day;
    }

    /**
     * @param avgTrkErr4Day the avgTrkErr4Day to set
     */
    public void setAvgTrkErr4Day(String avgTrkErr4Day) {
        this.avgTrkErr4Day = avgTrkErr4Day;
    }

    /**
     * @return the avgTrkErr5Day
     */
    public String getAvgTrkErr5Day() {
        return avgTrkErr5Day;
    }

    /**
     * @param avgTrkErr5Day the avgTrkErr5Day to set
     */
    public void setAvgTrkErr5Day(String avgTrkErr5Day) {
        this.avgTrkErr5Day = avgTrkErr5Day;
    }

    /**
     * @return the avgIntensityErr
     */
    public String getAvgIntensityErr() {
        return avgIntensityErr;
    }

    /**
     * @param avgIntensityErr the avgIntensityErr to set
     */
    public void setAvgIntensityErr(String avgIntensityErr) {
        this.avgIntensityErr = avgIntensityErr;
    }

    /**
     * @return the intermPubHeader
     */
    public String getIntermPubHeader() {
        return intermPubHeader;
    }

    /**
     * @param intermPubHeader the intermPubHeader to set
     */
    public void setIntermPubHeader(String intermPubHeader) {
        this.intermPubHeader = intermPubHeader;
    }
}
