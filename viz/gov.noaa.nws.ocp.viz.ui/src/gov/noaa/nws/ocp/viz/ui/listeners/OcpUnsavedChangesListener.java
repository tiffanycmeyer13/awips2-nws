/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.listeners;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class is meant to be added to any field that is modifiable by users, and
 * flip a flag to true to indicate if there are potentially unsaved changes. A
 * flag for ignoring changes is available for cases where modification is
 * expected and should be ignored, such as when data is being loaded.
 *
 * Dialogs that use this listener should instantiate only once, and add that
 * instance to all applicable fields.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 08, 2016 20414      amoore      Initial creation.
 * Jan 30, 2019 59222      jwu         Adapted from OCP Climate project.
 * </pre>
 *
 * @author amoore
 */
public class OcpUnsavedChangesListener implements Listener {

    /**
     * Flag for potentially unsaved changes.
     */
    private boolean myChangesUnsaved = false;

    /**
     * Flag for ignoring changes, such as during loading of data.
     */
    private boolean myIgnoreChanges = false;

    @Override
    public void handleEvent(Event event) {
        if (!myIgnoreChanges) {
            myChangesUnsaved = true;
        }
    }

    /**
     * @return the ChangesUnsaved flag
     */
    public boolean isChangesUnsaved() {
        return myChangesUnsaved;
    }

    /**
     * @param iChangesUnsaved
     *            the ChangesUnsaved to set. Should be set to false whenever
     *            changes are saved/reset by user for appropriate functionality.
     */
    public void setChangesUnsaved(boolean iChangesUnsaved) {
        this.myChangesUnsaved = iChangesUnsaved;
    }

    /**
     * @return the ignore flag
     */
    public boolean isIgnoreChanges() {
        return myIgnoreChanges;
    }

    /**
     * @param iIgnoreChanges
     *            the ignore flag to set. Should be set to true in instances
     *            where changes should be ignored, such as during loading of
     *            data.
     */
    public void setIgnoreChanges(boolean iIgnoreChanges) {
        this.myIgnoreChanges = iIgnoreChanges;
    }
}
