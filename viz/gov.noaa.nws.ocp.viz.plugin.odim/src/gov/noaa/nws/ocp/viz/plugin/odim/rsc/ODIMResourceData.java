/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.plugin.odim.rsc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.alerts.AlertMessage;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * Provides the metadata and constructor for ODIM resource.
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
@XmlAccessorType(XmlAccessType.NONE)
public class ODIMResourceData extends AbstractRequestableResourceData {

    enum Mode {
        DEFAULT, SRM
    }

    // Persisted via get/setModeString
    private Mode mode = Mode.DEFAULT;

    @XmlAttribute
    protected boolean rangeRings = true;

    @Override
    protected AbstractVizResource<?, ?> constructResource(
            LoadProperties loadProperties, PluginDataObject[] objects)
            throws VizException {
        ODIMRadialResource resource;
        switch (mode) {
        case DEFAULT:
            resource = new ODIMRadialResource(this, loadProperties);
            break;
        case SRM:
            resource = new ODIMSRMResource(this, loadProperties);
            break;
        default:
            throw new VizException("Invalid ODIMResourceData mode " + mode);
        }
        for (PluginDataObject obj : objects) {
            resource.addRecord(obj);
        }
        return resource;
    }

    @XmlAttribute(name = "mode")
    public String getModeString() {
        return mode != null ? mode.toString() : "";
    }

    public void setModeString(String mode) {
        this.mode = mode != null && !mode.isEmpty() ? Mode.valueOf(mode)
                : Mode.DEFAULT;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void update(Object updateData) {
        super.update(updateData);
        /*
         * The available time cache will contain DataTime objects when it needs
         * to contain RadarDataTime objects so it must be invalidated.
         */
        invalidateAvailableTimesCache();
    }

    @Override
    public void update(AlertMessage... messages) {
        for (AlertMessage message : messages) {
            /*
             * Since radar dataTimes are expected to set the level value, need
             * to do that here.
             */
            Object timeObj = message.decodedAlert.get("dataTime");
            if (timeObj instanceof DataTime) {
                Object primaryElevationAngleObj = message.decodedAlert
                        .get("primaryElevationAngle");
                if (primaryElevationAngleObj instanceof Number) {
                    DataTime time = (DataTime) timeObj;
                    Number primaryElevationAngle = (Number) primaryElevationAngleObj;
                    time.setLevelValue(primaryElevationAngle.doubleValue());
                }
            }
        }
        super.update(messages);
        invalidateAvailableTimesCache();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ODIMResourceData other = (ODIMResourceData) obj;
        if (mode != other.mode) {
            return false;
        }
        return true;
    }

}
