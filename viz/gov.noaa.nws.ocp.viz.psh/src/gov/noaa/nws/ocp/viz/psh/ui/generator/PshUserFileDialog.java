package gov.noaa.nws.ocp.viz.psh.ui.generator;

import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * 
 * Dialog for the User File selection
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 17, 2017 #35463      wpaintsil   Initial creation.
 * Sep 19, 2017 #36924      astrakovsky Implemented user file parsing.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshUserFileDialog extends CaveJFACEDialog {

    /**
     * Contents loaded from selected file.
     */
    private List<String> fileStrings;

    /**
     * title and text for example message
     */
    private String messageTitle;

    private String messageText;

    public PshUserFileDialog(Shell parentShell, String messageTitle,
            String messageText) {
        super(parentShell);
        this.messageTitle = messageTitle;
        this.messageText = messageText;
    }

    @Override
    public Control createDialogArea(Composite parent) {

        Composite top = (Composite) super.createDialogArea(parent);

        /*
         * Create the main layout for the shell.
         */
        GridLayout mainLayout = new GridLayout(1, false);
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.CENTER, SWT.BEGINNING, true,
                false);
        top.setLayoutData(mainLayoutData);

        Label instructions = new Label(top, SWT.NONE);
        instructions.setText(
                "Please select Example to view the required format\nof the text file, or select Browse to look for the file.");

        createButtons(top);

        getShell().setText("Load a Comma-Delimited Text File");
        return top;
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
     * Create the buttons.
     * 
     * @param parent
     */
    private void createButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, false);
        buttonComp.setLayout(buttonLayout);

        Button exampleButton = new Button(buttonComp, SWT.PUSH);
        exampleButton.setText("Example");

        Point parentLocation = getParentShell().getLocation();
        Point parentSize = getParentShell().getSize();
        exampleButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // open the example message
                showExampleMessage(parentLocation, parentSize);
            }

        });

        Button browseButton = new Button(buttonComp, SWT.PUSH);
        browseButton.setText("Browse");
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // open file browser and read file
                browseAndOpenFile();
            }

        });

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PshUserFileDialog.this.close();
            }
        });
    }

    /**
     * Show the example message
     * 
     * @param parentLocation
     * @param parentSize
     */
    private void showExampleMessage(Point parentLocation, Point parentSize) {

        int exampleX = 800, exampleY = 350;

        MessageDialog exampleMessage = new MessageDialog(getShell(),
                messageTitle, null, messageText, MessageDialog.INFORMATION,
                new String[] { "OK" }, 2) {

            @Override
            protected void configureShell(Shell shell) {
                super.configureShell(shell);

                // center this dialog; this override method seems to
                // undo the centering of the dialog
                shell.setBounds(
                        parentLocation.x
                                + ((parentSize.x / 2) - (exampleX / 2)),
                        parentLocation.y
                                + ((parentSize.y / 2) - (exampleY / 2)),
                        exampleX, exampleY);

            }
        };

        exampleMessage.open();

    }

    /**
     * Browse for and read a file.
     */
    private void browseAndOpenFile() {

        // browse for file
        FileDialog browseDialog = new FileDialog(getShell(), SWT.OPEN);
        browseDialog.setText("Browse");
        String[] filterExt = { "*.txt" };
        browseDialog.setFilterExtensions(filterExt);
        String selected = browseDialog.open();

        // only try to read if file selected
        if (selected != null && !selected.isEmpty()) {

            // open selected file and read lines
            fileStrings = PshUtil.readFileAsList(Paths.get(selected));

            // show error message if null or empty result
            if (fileStrings == null) {
                MessageDialog dialog = new MessageDialog(getShell(), "Error",
                        null, "File could not be read.", MessageDialog.ERROR,
                        new String[] { "Ok" }, 0);
                dialog.open();
            } else if (fileStrings.isEmpty()) {
                MessageDialog dialog = new MessageDialog(getShell(), "Error",
                        null, "File is empty.", MessageDialog.ERROR,
                        new String[] { "Ok" }, 0);
                dialog.open();
            }

            // close the dialog
            PshUserFileDialog.this.close();
        }

    }

    /**
     * @return the fileStrings
     */
    public List<String> getFileStrings() {
        return fileStrings;
    }

    /**
     * @param fileStrings
     *            the fileStrings to set
     */
    public void setFileStrings(List<String> fileStrings) {
        this.fileStrings = fileStrings;
    }

}