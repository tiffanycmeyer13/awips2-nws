/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IRenderableDisplayChangedListener;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;
import com.raytheon.viz.ui.tools.AbstractModalTool;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.notification.AtcfNotificationObserver;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResourceData;

/**
 * This singleton is intended to control an ATCF session's life cycle, with an
 * ATCF sidebar, a AtcfResource, an ATCF storm with its dataset (deck records).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jun 05, 2018 48178      jwu         Initial creation
 * Jun 28, 2018 51961      jwu         Test drawing B-Deck
 * Jul 05, 2018 52119      jwu         Link with active storm.
 * Aug 23, 2019 65564      jwu         Track user for session.
 * Aug 28, 2019 67881      jwu         Register ATCFNotificationObserver when session starts.
 * Jan 15, 2020 71722      jwu         Manage ATCF tool.
 * Apr 23, 2020 72252      jwu         Load all ATCF configurations.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AtcfSession implements IRenderableDisplayChangedListener {

    /*
     * The singleton instance
     */
    private static AtcfSession instance = null;

    /*
     * ATCF sidebar
     */
    private AtcfSideBar sideBar = null;

    /*
     * The editors
     */
    private List<AbstractEditor> editors = new ArrayList<>();

    /*
     * The perspective ATCF is activated within
     */
    private String perspectiveId = "";

    /*
     * Current Atcf User
     */
    private String uid = System.getProperty("user.name");

    /*
     * Current Atcf modal tool.
     */
    private AbstractModalTool atcfTool = null;

    /*
     * Hide default constructor
     */
    private AtcfSession() {
        AbstractVizPerspectiveManager pMngr = VizPerspectiveListener
                .getCurrentPerspectiveManager();
        if (pMngr != null) {
            setPerspectiveId(pMngr.getPerspectiveId());
        }
    }

    /**
     * Static method to get the AtcfSession instance
     * 
     * @return AtcfSession reference
     */
    public static synchronized AtcfSession getInstance() {

        if (instance == null) {
            instance = new AtcfSession();
        }

        return instance;
    }

    /**
     * Start the ATCF session.
     */
    public void start() {

        /*
         * Register AtcfNotificationObserver - it will fire
         * AtcfNotificationJobListeners when notification arrives.
         */
        AtcfNotificationObserver.register();

        // Load configurations
        AtcfConfigurationManager.getInstance().loadConfiguration();

        // Create & open sidebar
        if (sideBar == null) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            sideBar = new AtcfSideBar(shell);
        }

        if (!sideBar.isOpen()) {
            sideBar.open();
        } else {
            sideBar.bringToTop();
        }
    }

    /**
     * Gets an appropriate ATCF Resource. Returns the current ATCF Resource
     * registered with this ATCF Session if there is one. If not, looking for an
     * existing resource in the current editor. If one is not found, create a
     * new AtcfResource.
     * 
     * @return the rsc
     */
    public AtcfResource getAtcfResource() {

        AtcfResource rsc = AtcfVizUtil
                .findAtcfResource(AtcfVizUtil.getActiveEditor());

        if (rsc == null) {
            rsc = AtcfVizUtil.createNewResource();
        }

        return rsc;
    }

    /**
     * Get the ATCF Resource Data currently registered with the session
     * 
     * @return AtcfResourceData
     */
    public AtcfResourceData getAtcfResourceData() {
        return getAtcfResource().getResourceData();
    }

    /**
     * Add an editor to the editor list registered with the session
     */
    public void addEditor(AbstractEditor editor) {
        editors.add(editor);
    }

    /**
     * Get the list of editors registered with the session
     *
     * @return editors
     */
    public List<AbstractEditor> getEditors() {
        return editors;
    }

    /**
     * Register the given sidebar with the Session
     *
     * @param sbar
     */
    public void setSideBar(AtcfSideBar sbar) {
        sideBar = sbar;
    }

    /**
     * Return the sidebar dialog
     * 
     * @return AtcfSideBar
     */
    public AtcfSideBar getSideBar() {
        return sideBar;
    }

    /**
     * Remove the current sidebar from this Session
     */
    public void removeSideBar() {
        sideBar = null;
    }

    /**
     * End this Session
     */
    public void endSession() {
        // Deactivate ATCF tool.
        deactivateTool();
    }

    /**
     * Hide the sidebar
     */
    public void closeSideBar() {

        deactivateTool();

        if (sideBar != null) {
            sideBar.hide();
        }
    }

    /**
     * Deactivate the tool.
     */
    public void deactivateTool() {

        if (atcfTool != null) {
            atcfTool.deactivate();
        }

        atcfTool = null;
    }

    /**
     * Return the perspective ID
     * 
     * @return perspectiveId
     */
    public String getPerspectiveId() {
        return perspectiveId;
    }

    /**
     * Set the perspective ID
     * 
     * @param perspectiveId
     */
    public void setPerspectiveId(String perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    /**
     * Get the active storm.
     * 
     * @return perspectiveId
     */
    public Storm getActiveStorm() {
        return getAtcfResourceData().getActiveStorm();
    }

    /*
     * Remove ATCF handler when swapping to side view. Also open ATCF sidebar if
     * there is a ATCF resource when swapping to main editor.
     * 
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.core.IRenderableDisplayChangedListener#
     * renderableDisplayChanged(com.raytheon.uf.viz.core.IDisplayPane,
     * com.raytheon.uf.viz.core.drawables.IRenderableDisplay,
     * com.raytheon.uf.viz
     * .core.IRenderableDisplayChangedListener.DisplayChangeType)
     */
    @Override
    public void renderableDisplayChanged(IDisplayPane pane,
            IRenderableDisplay newRenderableDisplay, DisplayChangeType type) {
        // TODO - implement when needed
    }

    /**
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid
     *            the uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * @return the atcfTool
     */
    public AbstractModalTool getAtcfTool() {
        return atcfTool;
    }

    /**
     * @param atcfTool
     *            the atcfTool to set
     */
    public void setAtcfTool(AbstractModalTool atcfTool) {
        this.atcfTool = atcfTool;
    }
}