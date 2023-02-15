/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.UnitConverter;

import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.dataplugin.radar.RadarRecord;
import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.DrawableImage;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IImage;
import com.raytheon.uf.viz.core.drawables.ext.colormap.IColormappedImageExtension;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.viz.awipstools.IToolChangedListener;
import com.raytheon.viz.awipstools.ToolsDataManager;
import com.raytheon.viz.awipstools.common.StormTrackData;
import com.raytheon.viz.radar.IRadarConfigListener;
import com.raytheon.viz.radar.rsc.image.RadarSRMResource.SRMSource;
import com.raytheon.viz.radar.ui.RadarDisplayControls;
import com.raytheon.viz.radar.ui.RadarDisplayManager;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.viz.odim.rsc.ODIMVizDataUtil.SRMValues;

/**
 * SRM display for ODIM plugin
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
public class ODIMSRMResource extends ODIMRadialResource
        implements IRadarConfigListener, IToolChangedListener {

    private static final NumberFormat azFormat = new DecimalFormat("##0");

    protected Map<DataTime, SRMValues> srmValuesMap = new ConcurrentHashMap<>();

    protected ODIMSRMResource(ODIMResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties);
        RadarDisplayManager.getInstance().addListener(this);
        ToolsDataManager.getInstance().addStormTrackChangedListener(this);

    }

    @Override
    protected void disposeInternal() {
        super.disposeInternal();
        RadarDisplayManager.getInstance().removeListener(this);
        ToolsDataManager.getInstance().removeStormTrackChangedListener(this);
    }

    @Override
    public void updateConfig() {
        clearData();
    }

    @Override
    public void toolChanged() {
        clearData();
    }

    @Override
    protected String[] getTextContributions(AugmentedRecord arec) {
        SRMValues srmValues = srmValuesMap.get(arec.rec.getDataTime());
        if (srmValues != null && srmValues.getSourceName() != null) {
            StringBuilder sourceBuff = new StringBuilder(
                    srmValues.getSourceName());
            sourceBuff.append(":  ");

            if (srmValues.getSpeed() == 0) {
                sourceBuff.append("STATIONARY");
            } else {
                sourceBuff.append(azFormat.format(srmValues.getDirection()))
                        .append("°  ").append(srmValues.getSpeed())
                        .append("kt");
            }
            return new String[] { sourceBuff.toString() };
        } else {
            return new String[0];
        }
    }

    private void clearData() {
        synchronized (this.images) {
            for (DrawableImage image : this.images.values()) {
                if (image != null) {
                    image.dispose();
                }
            }
        }
        images.clear();
        upperTextMap.clear();
        issueRefresh();
    }

    @Override
    public String getName() {
        // RadarResource (via name generator) just returns "Radar"
        ODIMRecord rec = getRepresentativeRecord(
                getDescriptor().getTimeForResource(this));
        if (rec == null) {
            return "ODIM";
        }
        return String.format("%s %s%s", rec.getNode(),
                (rec.getTrueElevationAngle() != null
                        ? String.format("%1.1f ", rec.getTrueElevationAngle())
                        : ""),
                "Storm Rel Vel");
    }

    @Override
    public void remove(DataTime dataTime) {
        super.remove(dataTime);
        srmValuesMap.remove(dataTime);
    }

    @Override
    protected void createTile(IGraphicsTarget target,
            AugmentedRecord populatedRecord) throws StorageException,
            IOException, ClassNotFoundException, VizException {
        loadSRMVelocity(populatedRecord.rec);
        super.createTile(target, populatedRecord);
        upperTextMap.remove(populatedRecord.rec.getDataTime());
    }

    private void loadSRMVelocity(ODIMRecord rec) {
        RadarDisplayControls currentSettings = RadarDisplayManager.getInstance()
                .getCurrentSettings();
        SRMSource settingsSource = currentSettings.getSrmSource();

        double direction = 0;
        double speed = 0;
        Date movementTime = null;
        String sourceName = null;

        // for custom direction/speed as set in the Radar Display Controls
        // dialog
        if (SRMSource.WARNGEN == settingsSource) {
            sourceName = "TRK";
            StormTrackData stormTrackData = ToolsDataManager.getInstance()
                    .getStormTrackData();
            if (stormTrackData != null && stormTrackData.isValid()
                    && stormTrackData.getMotionSpeed() < 100.0) {
                direction = (stormTrackData.getMotionDirection() + 180) % 360;
                speed = stormTrackData.getMotionSpeed();
                movementTime = stormTrackData.getDate();
            } else {
                /*
                 * If no WarnGen, then try STI. Currently, there is no support
                 * for STI-like data, but this documents the desired behavior in
                 * case it becomes possible in the future.
                 */
                settingsSource = SRMSource.STI;
            }
        }
        if (SRMSource.STI == settingsSource) {
            sourceName = "STI";
            // ODIM spec does not have anything like STI; Use custom track.
            settingsSource = SRMSource.CUSTOM;
        }
        if (SRMSource.CUSTOM == settingsSource) {
            sourceName = "USR";
            direction = currentSettings.getSrmDir();
            speed = currentSettings.getSrmSpeed();
            movementTime = SimulatedTime.getSystemTime().getTime();
        }

        srmValuesMap.put(rec.getDataTime(),
                new SRMValues(speed, direction, movementTime, sourceName));
    }

    @Override
    protected IImage createImage(IGraphicsTarget target,
            ColorMapParameters params, ODIMRecord rec, RadarRecord radaRecord,
            Rectangle rect) throws VizException {
        UnitConverter unitConverter = ODIMVizDataUtil.getConverter(rec,
                radaRecord, params);
        return target.getExtension(IColormappedImageExtension.class)
                .initializeRaster(new ODIMSRMDataRetrievalAdapter(rec,
                        unitConverter, params,
                        ODIMVizDataUtil.getImageFlagValues(rec, params), rect),
                        params);
    }

    protected class ODIMSRMDataRetrievalAdapter
            extends ODIMImageDataRetrievalAdapter {

        public ODIMSRMDataRetrievalAdapter(ODIMRecord rec,
                UnitConverter unitConverter,
                ColorMapParameters colorMapParameters, int[] imageFlagValues,
                Rectangle rect) {
            super(rec, unitConverter, colorMapParameters, imageFlagValues,
                    rect);
        }

        @Override
        public ColorMapData getColorMapData() {
            Buffer buffer = ODIMVizDataUtil.convertAndUpdateRecordDataSRM(rec,
                    unitConverter, colorMapParameters, imageFlagValues,
                    srmValuesMap.get(rec.getDataTime()));
            return new ColorMapData(buffer,
                    new int[] { rect.width, rect.height });
        }

    }

    @Override
    protected void prepareCompatibilityRecord(AugmentedRecord arec,
            CompatibilityVizRadarRecord compatibilityRecord) {
        super.prepareCompatibilityRecord(arec, compatibilityRecord);
        if (compatibilityRecord.srmData == null) {
            SRMValues srmValues = srmValuesMap.get(arec.rec.getDataTime());
            if (srmValues != null) {
                compatibilityRecord.srmData = ODIMVizDataUtil
                        .calculateSRM(arec.rec, srmValues);
            }
        }

    }

}
