/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

import java.nio.file.Paths;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.viz.psh.PshPrintUtil;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Dialog for viewing/printing historical PSH reports.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 20, 2017 #40417     astrakovsky Initial creation.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 *
 */
public class PshViewHistoricalDialog extends CaveJFACEDialog {

    /**
     * Font for the PSH View text
     */
    private Font productFont;

    /**
     * Text field to show the historical PSH report.
     */
    private Text previewText;

    /**
     * The PSH View text.
     */
    private String productText = null;

    /**
     * Boolean indicating if a file was successfully opened.
     */
    private boolean fileOpened = false;

    /**
     * The directory to start in when opening files.
     */
    private static String startFilePath = "";

    /**
     * Constructor
     * 
     * @param parentShell
     *            Parent shell
     */
    protected PshViewHistoricalDialog(Shell parentShell) {
        super(parentShell);

        productFont = PshUtil.createFont("Courier", 12, SWT.BOLD);

        // find a file to open and view
        browseAndOpenFile(startFilePath);
    }

    /**
     * Override open method to avoid opening dialog if there is no file.
     * 
     * @return int - The return code.
     */
    @Override
    public int open() {
        if (fileOpened) {
            return super.open();
        } else {
            PshViewHistoricalDialog.this.close();
            return CANCEL;
        }
    }

    /**
     * Override the method from the parent Dialog class to implement the GUI.
     * 
     * @param parent
     * @return Control - the dialog area.
     */
    @Override
    public Control createDialogArea(Composite parent) {
        Composite top = (Composite) super.createDialogArea(parent);

        /*
         * Create the main layout for the shell.
         */
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.verticalSpacing = 15;
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        top.setLayoutData(mainLayoutData);

        createViewerArea(top);
        createButtonArea(top);

        /*
         * Sets dialog title
         */
        getShell().setText("View Historical Reports");

        return top;
    }

    /**
     * Create composite and text field for the PSH product text.
     * 
     * @param top
     */
    private void createViewerArea(Composite top) {
        Group viewerComp = new Group(top, SWT.SHADOW_IN);
        viewerComp.setText("PSH Viewer");
        GridLayout viewerLayout = new GridLayout(1, false);
        viewerLayout.marginWidth = 15;
        viewerLayout.marginTop = 15;
        viewerLayout.marginBottom = 16;
        viewerComp.setLayout(viewerLayout);
        viewerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        previewText = new Text(viewerComp,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);

        previewText.setLayoutData(textData);
        previewText.setEditable(false);
        previewText.setBackground(
                top.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        previewText.setForeground(
                top.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        previewText.setFont(productFont);
        textData.heightHint = previewText.getLineHeight() * 20;
        previewText.setText(productText);
    }

    /**
     * Create the button area containing the print and close buttons.
     * 
     * @param top
     */
    private void createButtonArea(Composite top) {
        Group buttonComp = new Group(top, SWT.SHADOW_IN);
        buttonComp.setText("Print Historical PSH");
        GridLayout buttonLayout = new GridLayout(3, false);
        buttonLayout.marginRight = 10;
        buttonLayout.marginBottom = 5;
        buttonComp.setLayout(buttonLayout);
        buttonComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite buttonCloseComp = new Composite(buttonComp, SWT.NONE);
        RowLayout buttonCloseLayout = new RowLayout();
        buttonCloseLayout.marginHeight = 0;
        buttonCloseLayout.marginWidth = 0;
        buttonCloseLayout.pack = false;
        buttonCloseComp.setLayout(buttonCloseLayout);

        Button printButton = new Button(buttonCloseComp, SWT.PUSH);
        printButton.setText("Print Report");
        printButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // print the preview product
                if (productText != null && !productText.isEmpty()) {
                    PshPrintUtil.getPshPrinter().printInput(productText);
                }
            }
        });

        Button closeButton = new Button(buttonCloseComp, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PshViewHistoricalDialog.this.close();
            }
        });

    }

    /**
     * Browse for and read a file to view as report.
     * 
     * @param startPath
     *            The path to start in.
     */
    private void browseAndOpenFile(String startPath) {

        // browse for file
        FileDialog browseDialog = new FileDialog(getParentShell(), SWT.OPEN);
        browseDialog.setText("Browse");
        if (startPath != null && !startPath.isEmpty()) {
            browseDialog.setFilterPath(startPath);
        }
        String[] filterExt = { "*.txt" };
        browseDialog.setFilterExtensions(filterExt);
        String selected = browseDialog.open();

        // only try to read if file selected
        if (selected != null && !selected.isEmpty()) {

            // update last-used directory
            startFilePath = selected.substring(0, selected.lastIndexOf("/"));

            // open selected file and read to string
            productText = PshUtil.readFileAsString(Paths.get(selected));

            // show error message if null or empty result
            if (productText == null) {
                MessageDialog dialog = new MessageDialog(getParentShell(),
                        "Error", null, "File could not be read.",
                        MessageDialog.ERROR, new String[] { "Ok" }, 0);
                dialog.open();
                fileOpened = false;
            } else if (productText.isEmpty()) {
                MessageDialog dialog = new MessageDialog(getParentShell(),
                        "Error", null, "File is empty.", MessageDialog.ERROR,
                        new String[] { "Ok" }, 0);
                dialog.open();
                fileOpened = false;
            } else {
                // indicate that file was opened
                fileOpened = true;
            }
        }
    }

    /**
     * Remove the default OK/Cancel buttons.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        GridLayout layout = (GridLayout) parent.getLayout();
        layout.marginHeight = 0;
    }

    /**
     * Close the dialog and dispose resources.
     */
    @Override
    public boolean close() {
        boolean returnValue = super.close();

        if (productFont != null) {
            productFont.dispose();
        }

        return returnValue;
    }

    /**
     * Make this dialog resizable.
     */
    @Override
    protected boolean isResizable() {
        return true;

    }

    /**
     * Prevent the controls in the dialog from being cut off when resizing.
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setMinimumSize(600, 400);
    }

}