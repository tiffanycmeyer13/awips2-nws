/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog.support;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * This class handles manual changes to data fields that are associated to a
 * {@link DataValueOrigin} combo box, setting the selected value to
 * {@link DataValueOrigin#OTHER} when values are manually changed. Additionally
 * can be used as a selection listener (such as for button presses when
 * activating {@link DateSelectionComp}) to perform the same task.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 10 AUG 2016  20414      amoore      Initial creation
 * 20 NOV 2017  41128      amoore      Moved to separate file.
 * </pre>
 * 
 * @author amoore
 */
public class DataFieldListener implements KeyListener, SelectionListener {
    /**
     * The combo box to change selection for.
     */
    private final ComboViewer myComboBox;

    /**
     * The unsaved changes listener for the dialog, for flagging unsaved changes
     * on key presses.
     */
    private final UnsavedChangesListener unsavedChangesListener;

    /**
     * Constructor.
     * 
     * @param iComboBox
     *            combo box to load data for.
     * @param unsavedChangesListener
     */
    public DataFieldListener(ComboViewer iComboBox,
            UnsavedChangesListener unsavedChangesListener) {
        myComboBox = iComboBox;
        this.unsavedChangesListener = unsavedChangesListener;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // any user-defined values means changes have been made
        unsavedChangesListener.setChangesUnsaved(true);

        // automatically switch to "Other" setting
        setComboViewerSelection(myComboBox, DataValueOrigin.OTHER);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // unimplemented
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        setComboViewerSelection(myComboBox, DataValueOrigin.OTHER);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // unimplemented
    }

    /**
     * Set current selection of a combo viewer. Not simple enough to be worth
     * retyping every call.
     * 
     * @param iComboBox
     *            combo box to set selection for.
     * @param iSelection
     *            selection to set.
     */
    public static void setComboViewerSelection(ComboViewer iComboBox,
            Object iSelection) {
        iComboBox.setSelection(new StructuredSelection(iSelection), true);
    }
}