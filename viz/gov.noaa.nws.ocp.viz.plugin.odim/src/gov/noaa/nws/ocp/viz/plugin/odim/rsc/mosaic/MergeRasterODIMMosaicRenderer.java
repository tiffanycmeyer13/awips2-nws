/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.plugin.odim.rsc.mosaic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;

import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.groups.BestResResource;
import com.raytheon.viz.radar.rsc.MosaicPaintProperties;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.viz.plugin.odim.rsc.ODIMRadialResource;
import gov.noaa.nws.ocp.viz.plugin.odim.rsc.ODIMVizDataUtil;
import gov.noaa.nws.ocp.viz.plugin.odim.rsc.mosaic.ODIMMosaicRendererFactory.IRadarMosaicRenderer;

/**
 * Copied from com.raytheon.viz.radar.rsc.mosaic.MergeRasterRadarMosaicRenderer
 * and modified.
 *
 * mosaic renderer that merges large low res raster products with smaller
 * highres raster products.
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
public class MergeRasterODIMMosaicRenderer implements IRadarMosaicRenderer {

    private ResourcePair largest = null;

    private Set<List<ODIMRecord>> recordLists = new HashSet<>();

    @Override
    public void dispose() {

    }

    @Override
    public void mosaic(IGraphicsTarget target, PaintProperties paintProps,
            ODIMMosaicResource mosaicToRender) throws VizException {
        if (((MosaicPaintProperties) paintProps).isForceRepaint()) {
            double largestSize = Double.MIN_VALUE;
            List<ODIMRecord> records = new ArrayList<>();
            // collect the records.
            for (ResourcePair rp : mosaicToRender.getResourceList()) {
                if (rp.getResource() != null) {
                    AbstractVizResource<?, ?> rsc = rp.getResource();
                    DataTime time = mosaicToRender.getTimeForResource(rsc);
                    if (rsc instanceof BestResResource) {
                        rsc = ((BestResResource) rsc).getBestResResource(time);
                    }
                    if (rsc instanceof ODIMRadialResource) {

                        ODIMRecord rr = ((ODIMRadialResource) rsc)
                                .getRecord(time);
                        if (rr != null) {
                            records.add(rr);
                            double size = ODIMVizDataUtil.calculateExtent(rr);
                            if (size > largestSize) {
                                largestSize = size;
                                largest = rp;
                            }
                        }
                    }
                }
            }
            // Sort them from lowest res to highest res
            Collections.sort(records, (rec1, rec2) -> {
                Integer res1 = rec1.getGateResolution();
                Integer res2 = rec2.getGateResolution();
                return res1.compareTo(res2);
            });
            // merge each lower res one with a high res one
            if (!recordLists.contains(records)) {
                for (ODIMRecord record : records) {
                    Iterator<List<ODIMRecord>> itr = recordLists.iterator();
                    while (itr.hasNext()) {
                        List<ODIMRecord> recordList = itr.next();
                        if (recordList.contains(record)) {
                            itr.remove();
                        }
                    }
                }
                recordLists.add(records);
            }
            String format = null;
            if (!records.isEmpty()) {
                format = ODIMVizDataUtil.getRadarFormat(records.get(0));
            }
            Set<DataTime> timesToClear = null;
            if ("Raster".equals(format)) {
                timesToClear = mergeRaster(records);
            } else if ("Radial".equals(format)) {
                timesToClear = mergeRadial(records);
            }
            for (ResourcePair rp : mosaicToRender.getResourceList()) {
                if (rp.getResource() != null) {
                    AbstractVizResource<?, ?> rsc = rp.getResource();
                    for (DataTime time : timesToClear) {
                        if (rsc instanceof BestResResource) {
                            rsc = ((BestResResource) rsc)
                                    .getBestResResource(time);
                        }
                        if (rsc == null) {
                            continue;
                        }
                        if (rsc instanceof ODIMRadialResource) {
                            ((ODIMRadialResource) rsc).redoImage(time);
                        }
                    }
                }
            }
        }
        for (ResourcePair rp : mosaicToRender.getResourceList()) {
            if (rp.getResource() != null) {
                AbstractVizResource<?, ?> rsc = rp.getResource();
                DataTime time = mosaicToRender.getTimeForResource(rsc);
                if (rsc instanceof BestResResource) {
                    rsc = ((BestResResource) rsc).getBestResResource(time);
                }
                if (rsc == null) {
                    continue;
                }
                paintProps.setDataTime(time);
                if (rp == largest || !(rsc instanceof ODIMRadialResource)) {
                    rsc.paint(target, paintProps);
                } else {
                    ((ODIMRadialResource) rsc).paintRadar(target, paintProps);
                }
            }

        }
    }

    private Set<DataTime> mergeRaster(List<ODIMRecord> records)
            throws VizException {
        Set<DataTime> result = new HashSet<>();
        for (int c = 1; c < records.size(); c++) {
            ODIMRecord rr1 = records.get(c - 1);
            ODIMRecord rr2 = records.get(c);

            Unit<?> unit1 = ODIMVizDataUtil.getRecordDataUnit(rr1);
            Unit<?> unit2 = ODIMVizDataUtil.getRecordDataUnit(rr2);
            UnitConverter converter;
            try {
                converter = unit2.getConverterToAny(unit1);
            } catch (IncommensurableException | UnconvertibleException e) { // NOSONAR
                // No joining can occur
                continue;
            }

            int ratio = rr2.getGateResolution() / rr1.getGateResolution();
            int nx = rr1.getNumBins() / ratio;
            int ny = rr1.getNumRadials() / ratio;
            byte[] rawData1 = rr1.getRawData();
            byte[] rawData2 = rr2.getRawData();

            boolean rawData1Changed = false;
            boolean rawData2Changed = false;
            int cx = rr1.getNumBins() / 2;
            int cy = rr1.getNumRadials() / 2;
            // 99/200 is magic from A1. It makes the acceptable circle
            // slightly smaller(like 3 pixels) than the size of the data
            int rSqr = (Math.max(rr1.getNumBins(), rr1.getNumRadials())) * 99
                    / 200;
            rSqr = rSqr * rSqr;
            // i,j is the coordinate in the highres
            // i2,j2 is the coordinate in the low res
            // copy the low res pixels into highres outside the radius
            for (int i = 0; i < rr1.getNumBins(); i++) {
                int i2 = ((rr2.getNumBins() - nx) / 2) + i / ratio;
                for (int j = 0; j < rr1.getNumRadials(); j++) {
                    int j2 = ((rr2.getNumRadials() - ny) / 2) + j / ratio;
                    int dSqr = (cx - i) * (cx - i) + (cy - j) * (cy - j);
                    int index1 = j * rr1.getNumBins() + i;
                    int index2 = j2 * rr2.getNumBins() + i2;
                    if (dSqr >= rSqr && rawData1[index1] == 0
                            && rawData2[index2] != 0) {
                        rawData1[index1] = (byte) converter
                                .convert(rawData2[index2]);
                        rawData1Changed = true;
                    }
                }
            }
            // blank out the middle of low res
            for (int i = 0; i < rr1.getNumBins(); i++) {
                int i2 = ((rr2.getNumBins() - nx) / 2) + i / ratio;
                for (int j = 0; j < rr1.getNumRadials(); j++) {
                    int j2 = ((rr2.getNumRadials() - ny) / 2) + j / ratio;
                    int index2 = j2 * rr2.getNumBins() + i2;
                    if (rawData2[index2] != 0) {
                        rawData2[index2] = 0;
                        rawData2Changed = true;
                    }
                }
            }
            if (rawData1Changed || rawData2Changed) {
                result.add(rr1.getDataTime());
            }
            if (rawData2Changed) {
                result.add(rr2.getDataTime());
            }
        }
        return result;
    }

    private Set<DataTime> mergeRadial(List<ODIMRecord> records)
            throws VizException {
        Set<DataTime> result = new HashSet<>();
        for (int c = 1; c < records.size(); c++) {
            ODIMRecord rr1 = records.get(c - 1);
            ODIMRecord rr2 = records.get(c);

            int numBinsToClear = rr1.getNumBins() * rr1.getGateResolution()
                    / rr2.getGateResolution();

            byte[] rawData2 = rr2.getRawData();
            boolean rawData2Changed = false;

            // blank out the middle of low res
            for (int i = 0; i < numBinsToClear; i++) {
                for (int j = 0; j < rr2.getNumRadials(); j++) {
                    int index = j * rr2.getNumBins() + i;
                    if (rawData2[index] != 0) {
                        rawData2[index] = 0;
                        rawData2Changed = true;
                    }
                }
            }

            if (rawData2Changed) {
                result.add(rr2.getDataTime());
            }
        }
        return result;
    }

}
