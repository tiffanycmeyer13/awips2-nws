/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.firewx.rsc;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.dataplugin.bufrua.UAObs;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.viz.core.DescriptorMap;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.drawables.AbstractRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.procedures.Bundle;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.d2d.nsharp.rsc.BufruaNSharpResourceData;
import com.raytheon.viz.ui.BundleProductLoader;
import com.raytheon.viz.ui.EditorTypeInfo;
import com.raytheon.viz.ui.UiUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.InputAdapter;

import gov.noaa.nws.ncep.ui.nsharp.display.NsharpSkewTPaneDescriptor;
import gov.noaa.nws.ncep.ui.nsharp.display.NsharpSkewTPaneDisplay;

/**
 * Input handler for Firewx resource. This class loads the sounding resource and
 * open the NSHARP display.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 07, 2017 18784      wkwock      Initial creation
 * Apr 25, 2022 8791       mapeters    Update determination of editor type to load to
 *
 * </pre>
 *
 * @author wkwock
 */
public class FirewxInputHandler extends InputAdapter {

    /** firewx resource */
    private final FirewxResource resource;

    /** closest station */
    private UAObs closestRecord = null;

    /** location where mouse was down */
    private int downX, downY;

    /**
     * constructor
     *
     * @param resource
     */
    public FirewxInputHandler(FirewxResource resource) {
        this.resource = resource;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        if (resource != null) {
            Collection<UAObs> stations = resource.getCurrentRecords();

            boolean wasClosest = closestRecord != null;
            closestRecord = null;

            if (stations != null && !stations.isEmpty()) {
                double radius = 5;
                Coordinate c = new Coordinate(x, y);
                double bestDist = Double.MAX_VALUE;
                for (UAObs station : stations) {
                    double[] pixel = resource.getResourceContainer()
                            .translateInverseClick(
                                    new Coordinate(station.getLongitude(),
                                            station.getLatitude()));
                    Coordinate p = new Coordinate(pixel[0], pixel[1]);
                    double dist = p.distance(c);
                    if (dist < bestDist && dist < radius) {
                        closestRecord = station;
                        bestDist = dist;
                    }
                }
            }

            if (wasClosest && closestRecord == null) {
                getShell().setCursor(null);
            } else if (!wasClosest && closestRecord != null) {
                Cursor handCursor = getShell().getDisplay()
                        .getSystemCursor(SWT.CURSOR_HAND);
                getShell().setCursor(handCursor);
            }
        }
        return super.handleMouseMove(x, y);
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        if (mouseButton == 1) {
            downX = x;
            downY = y;
        }
        return super.handleMouseDown(x, y, mouseButton);
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {
        int downX = this.downX;
        int downY = this.downY;
        this.downX = this.downY = -1;

        if (closestRecord != null && mouseButton == 1 && downX == x
                && downY == y) {
            loadSoundingResource(closestRecord.getStationId());
            closestRecord = null;
            return true;
        }
        return super.handleMouseUp(x, y, mouseButton);
    }

    /**
     * Loading sounding resource
     *
     * @param stationName
     */
    private void loadSoundingResource(String stationName) {
        // Build metadata map for sounding resource
        HashMap<String, RequestConstraint> metadataMap = new HashMap<>();
        metadataMap.put("pluginName",
                resource.getResourceData().getMetadataMap().get("pluginName"));

        RequestConstraint stationNameConstraint = new RequestConstraint();
        stationNameConstraint.setConstraintValue(stationName);
        stationNameConstraint.setConstraintType(ConstraintType.EQUALS);
        metadataMap.put("stationName", stationNameConstraint);

        metadataMap.put("reportType",
                resource.getResourceData().getMetadataMap().get("reportType"));

        BufruaNSharpResourceData nsResourceData = new BufruaNSharpResourceData();
        nsResourceData.setMetadataMap(metadataMap);
        ResourcePair pair = new ResourcePair();
        pair.setResourceData(nsResourceData);
        pair.setLoadProperties(new LoadProperties());
        NsharpSkewTPaneDisplay display = new NsharpSkewTPaneDisplay();
        display.setDescriptor(new NsharpSkewTPaneDescriptor());
        display.getDescriptor().getResourceList().add(pair);
        String editorId = DescriptorMap.getEditorId(display);
        EditorTypeInfo editorTypeInfo = new EditorTypeInfo(editorId, false);
        AbstractEditor editor = UiUtil.createOrOpenEditor(editorTypeInfo,
                display.cloneDisplay());
        Bundle b = new Bundle();
        b.setDisplays(new AbstractRenderableDisplay[] { display });
        Job j = new BundleProductLoader(editor, b);
        j.schedule();
    }

    @Override
    public boolean handleMouseExit(Event event) {
        getShell().setCursor(null);
        return super.handleMouseExit(event);
    }

    /**
     * get shell
     *
     * @return shell
     */
    private Shell getShell() {
        IDisplayPaneContainer container = resource.getResourceContainer();
        if (container instanceof IWorkbenchPart) {
            return ((IWorkbenchPart) container).getSite().getShell();
        }

        return VizWorkbenchManager.getInstance().getCurrentWindow().getShell();
    }

    /**
     * get closest record to the mouse pointer
     *
     * @return
     */
    protected UAObs getClosestRecord() {
        return closestRecord;
    }
}
