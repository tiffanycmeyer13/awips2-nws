package gov.noaa.nws.mdl.viz.awipsref;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;

/**
 * Adds the contextual menu to the product legend for AWIPS II Reference System
 * and invokes the browser to display the results.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2016           jburks       Initial creation
 * Jul 09, 2016           jburks       Update error handing and refactor to awips reference
 * Jun 01, 2020  DCS21907 jburks       Added url parameter for perspective
 * 
 * </pre>
 * 
 * @author jburks
 * @version 1.0
 */

public class AWIPSRefAction extends AbstractRightClickAction {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AWIPSRefAction.class);

    private static final String MENU_NAME = "Reference on Product";

    private Menu menu = null;

    AWIPSRefConfiguration jittConfiguration;

    public AWIPSRefAction() {
        super();
        jittConfiguration = AWIPSRefConfigurationManager.getInstance()
                .loadConfiguration();
        if (jittConfiguration == null) {
            this.setEnabled(false);
        }
    }

    public AWIPSRefAction(IDisplayPaneContainer container) {
        super(IAction.AS_DROP_DOWN_MENU);
        jittConfiguration = AWIPSRefConfigurationManager.getInstance()
                .loadConfiguration();
        if (jittConfiguration == null) {
            this.setEnabled(false);
        }
        setContainer(container);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#getText()
     */
    @Override
    public String getText() {
        return MENU_NAME;
    }

    private String getDataURI(AbstractVizResource rsc) {
        AbstractResourceData rscdata = rsc.getResourceData();

        if (rscdata instanceof AbstractRequestableResourceData) {
            AbstractRequestableResourceData rrd = (AbstractRequestableResourceData) rscdata;
            try {
                DataTime[] avail = rrd.getAvailableTimes();
                PluginDataObject[] objs = rrd.getLatestPluginDataObjects(avail,
                        new DataTime[] { new DataTime() });
                if (objs.length > 0) {
                    return (objs[0].getDataURI());
                }
            } catch (VizException e) {
                // ignore the error, because some products such as maps don't
                // have datauri.
            }

        }
        return "";
    }
    
    public String getPerspectiveName(){
    	IWorkbench wb = PlatformUI.getWorkbench();
    	IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
    	IWorkbenchPage page = win.getActivePage();
    	IPerspectiveDescriptor perspective = page.getPerspective();
    	String label = replaceSpaceWithUnderscore(perspective.getLabel().trim());
    	return label;
    }

    @Override
    public void run() {

        // Get Localization file that contains the server
        String siteBase = jittConfiguration.getServerLocation();
        StringBuilder sb = new StringBuilder(siteBase);
        sb.append("?");

        boolean added = false;
        String dataURI = getDataURI(getSelectedRsc());
        if (dataURI != null && dataURI.equals("") != true) {
            try {
                sb.append("datauri=" + URLEncoder.encode(dataURI, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                statusHandler.warn("Problem encoding the datauri");
            }
            added = true;
        }
        String keywords = selectedRsc.getResource().getName();
        if (keywords != null) {
            keywords = replaceSpaceWithComma(keywords);
            if (added) {
                sb.append("&");
            }
            added = true;

            try {
                sb.append("keywords=" + URLEncoder.encode(keywords, "UTF-8"));
            } catch (UnsupportedEncodingException e) {

                statusHandler
                        .error("Problem encoding keywords into url format for AWIPS Reference");

            }
        }

        String location = getLocation();
        if (location != null) {
            if (added) {
                sb.append("&");
            }
            added = true;
            sb.append("location=" + location);
        }

        String site = LocalizationManager.getInstance().getCurrentSite();

        if (added) {
            sb.append("&");
        }
        sb.append("site=" + site);
        added = true;
        
        String perspectiveName = getPerspectiveName();
        if (added) {
            sb.append("&");
        }
        sb.append("perspective=").append(perspectiveName);
        
        String endURL = sb.toString();

        try {
            Runtime.getRuntime().exec(
                    jittConfiguration.getBrowserPath() + " " + endURL);

        } catch (IOException e) {
            statusHandler
                    .warn("Problem opening browser to display product reference");
        }
    }

    private String replaceSpaceWithComma(String input) {
        return input.replaceAll("\\s+", ",");
    }
    
    private String replaceSpaceWithUnderscore(String input) {
        return input.replaceAll("\\s+", "_");
    }

    private String getLocation() {
        if (this.getDescriptor() instanceof MapDescriptor) {
            MapDescriptor mdesc = (MapDescriptor) this.getDescriptor();
            IRenderableDisplay disp = mdesc.getRenderableDisplay();

            double[] ul = mdesc.pixelToWorld(new double[] {
                    disp.getExtent().getMinX(), disp.getExtent().getMinY() });

            double[] lr = mdesc.pixelToWorld(new double[] {
                    disp.getExtent().getMaxX(), disp.getExtent().getMaxY() });

            return ul[0] + "," + ul[1] + "," + lr[0] + "," + lr[1];
        }
        return null;
    }

}
