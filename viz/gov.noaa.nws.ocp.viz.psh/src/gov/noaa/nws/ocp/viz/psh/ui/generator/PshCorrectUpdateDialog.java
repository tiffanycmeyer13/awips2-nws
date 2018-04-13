/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.common.dataplugin.psh.IssueType;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;

/**
 * Dialog for Corrections/Updates to the final PSH product in the View/Send
 * Dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2017 #35463      wpaintsil   Initial creation.
 * Sep 14, 2017 #37365      jwu         Integrate PshData object.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshCorrectUpdateDialog extends CaveJFACEDialog {

    private IssueType issueType;

    private PshData pshData;

    private Text reasonText;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    protected PshCorrectUpdateDialog(Shell parentShell, IssueType issueType,
            PshData pshData) {
        super(parentShell);
        this.issueType = issueType;
        this.pshData = pshData;
    }

    /**
     * Override the method from the parent Dialog class to implement the GUI.
     */
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

        createTextArea(top);

        /*
         * Sets dialog title
         */
        getShell().setText("View/Send PSH");

        return top;
    }

    /**
     * Create text area.
     * 
     * @param top
     *            parent composite
     */
    private void createTextArea(Composite top) {
        Label textLabel = new Label(top, SWT.NORMAL);
        textLabel.setText((DateFormatSymbols.getInstance()
                .getShortMonths()[issueType.getDate().get(Calendar.MONTH)] + " "
                + issueType.getDate().get(Calendar.DATE) + "..."
                + issueType.getCategory()).toUpperCase() + " FOR...");

        reasonText = new Text(top,
                SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        FontMetrics textMetrics = new GC(reasonText).getFontMetrics();

        textLayoutData.widthHint = textMetrics.getAverageCharWidth() * 50;
        textLayoutData.heightHint = textMetrics.getHeight() * 10;

        reasonText.setLayoutData(textLayoutData);
    }

    /**
     * Save/Cancel button for "Save" a product file.
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        // Set text on buttons.
        String okText = "Save Update";
        if (issueType.isCorrected()) {
            okText = "Save Correction";
        }

        createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, false);

        createButton(parent, IDialogConstants.OK_ID, okText, true);
    }

    /**
     * Save new issue type and update status.
     */
    @Override
    protected void buttonPressed(int buttonId) {

        if (buttonId == IDialogConstants.OK_ID) {
            if (reasonText.getText() != null) {
                issueType.setReason(reasonText.getText());
            }

            pshData.getUpdateInfo().add(issueType);

            pshData.updateStatus();
        }

        super.close();
    }
}