/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog for Choose Area.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 10, 2018 #45686      dmanzella   initial creation
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChooseAreaDialog extends CaveSWTDialog {

    /**
     * Constructor
     * 
     * @param shell
     */
    public ChooseAreaDialog(Shell shell) {

        super(shell, SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
                | SWT.RESIZE);
        setText("Choose Area");
    }

    /**
     * Initializes the components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createContents();
    }

    /**
     * Create contents.
     * 
     * @param parent
     */
    protected void createContents() {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComposite.setLayoutData(mainLayoutData);

        createMainArea(mainComposite);
        createOkCancelButtons(mainComposite);
    }

    /**
     * Creates the main area of the GUI
     * 
     * @param parent
     */
    protected void createMainArea(Composite parent) {
        Label mainLabel = new Label(parent, SWT.NONE);
        mainLabel.setText("Choose map area:");

        GridData textWidthGridData = new GridData(SWT.NONE, SWT.NONE, true,
                false);
        textWidthGridData.widthHint = 350;
        textWidthGridData.heightHint = 300;

        Text mainTextBox = new Text(parent, SWT.V_SCROLL);
        mainTextBox.setLayoutData(textWidthGridData);
        mainTextBox.setText(
                "AOR Plotter \nAOR+ \nNorth Atlantic \nNorth Indian Ocean");
    }

    /**
     * Creates the Ok and Cancel buttons.
     * 
     * @param parent
     */
    protected void createOkCancelButtons(Composite parent) {
        Composite okCancelButtonsComposite = new Composite(parent, SWT.NONE);
        GridLayout okCancelButtonsGridLayout = new GridLayout(2, true);
        okCancelButtonsComposite.setLayout(okCancelButtonsGridLayout);
        GridData okCancelGridData = new GridData(SWT.CENTER, SWT.FILL, true,
                false);

        okCancelButtonsComposite.setLayoutData(okCancelGridData);

        Button okButton = new Button(okCancelButtonsComposite, SWT.CENTER);
        okButton.setText("OK");
        okButton.setLayoutData(okCancelGridData);

        Button cancelButton = new Button(okCancelButtonsComposite, SWT.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(okCancelGridData);

    }
}