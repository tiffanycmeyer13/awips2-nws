/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Cleanup Storms Directory dialog
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 08, 2018  51349      wpaintsil  Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class CleanupStormsDialog extends CaveSWTDialog {
    /**
     * Widget for the list of storms
     */
    private List stormList;

    /**
     * OK button
     */
    private Button okButton;

    /**
     * Constructor
     * 
     * @param parent
     */
    public CleanupStormsDialog(Shell parent) {
        super(parent);
        setText("Cleanup Storms Directory");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createStormList(shell);
        createControlButtons(shell);
    }

    /**
     * Create the list of storms.
     * 
     * @param parent
     */
    private void createStormList(Composite parent) {
        Composite stormListComp = new Composite(parent, SWT.NONE);

        GridLayout listCompLayout = new GridLayout(1, false);
        stormListComp.setLayout(listCompLayout);
        stormListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label chooseLabel = new Label(stormListComp, SWT.NONE);
        chooseLabel.setText("Choose (a) storm(s) to remove:");

        stormList = new List(stormListComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

        for (int ii = 0; ii < 20; ii++) {
            stormList.add("Placeholder " + ii);
        }

        stormList.setLayoutData(new GridData(400, 300));

        stormList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (stormList.getSelectionCount() > 0 && okButton != null) {
                    okButton.setEnabled(true);
                }
            }
        });

    }

    private void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(2, true);
        buttonComp.setLayout(buttonLayout);
        buttonLayout.marginWidth = 100;

        buttonComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        okButton = new Button(buttonComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        okButton.setEnabled(
                stormList != null && stormList.getSelectionCount() > 0);

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();

            }
        });

        cancelButton.setFocus();
    }
}
