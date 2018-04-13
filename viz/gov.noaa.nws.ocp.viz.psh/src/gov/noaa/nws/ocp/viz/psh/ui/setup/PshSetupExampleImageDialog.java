/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Implementation of a PSH example dialog that shows an image.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------- ----------- --------------------------
 * Jun 05, 2017 #34294      astrakovsky Initial creation.
 * Jun 15, 2017 #34873      astrakovsky Refactored to work as a 
 *                                      generic example image generator.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshSetupExampleImageDialog extends CaveJFACEDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int BUTTON_HEIGHT = 28;

    private static final int BUTTON_WIDTH = 120;

    /**
     * The dialog title.
     */
    private String dialogTitle;

    /**
     * Image creation variables.
     */
    private String imagePath;

    private Label exampleLabel;

    private Image exampleImage = null;

    /**
     * Constructor
     * 
     * @param parentShell
     * @param imagePath
     */
    public PshSetupExampleImageDialog(Shell parentShell, String dialogTitle,
            String imagePath) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);
        this.dialogTitle = dialogTitle;
        this.imagePath = imagePath;
    }

    @Override
    public Control createDialogArea(Composite parent) {

        Composite top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 5;
        mainLayout.marginWidth = 10;
        top.setLayout(mainLayout);
        top.setLayoutData(
                new GridData(SWT.CENTER, SWT.BEGINNING, false, false));

        initializeComponents(top);

        return top;
    }

    /**
     * Creates buttons, and other controls in the dialog area
     * 
     * @param top
     */
    private void initializeComponents(Composite top) {

        // Set dialog title
        getShell().setText(dialogTitle);

        // Create the buttons
        Composite buttonsGroup = new Composite(top, SWT.NONE);
        createButtons(buttonsGroup);

        // Create the image
        createExampleImage(top);

        buttonsGroup.pack();

    }

    /**
     * Create the example image.
     * 
     * @param parent
     */
    private void createExampleImage(Composite parent) {
        Composite imageComp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        GridData layoutData = new GridData(SWT.CENTER, SWT.FILL, true, false);

        imageComp.setLayout(layout);
        imageComp.setLayoutData(layoutData);

        exampleLabel = new Label(imageComp, SWT.NORMAL);

        exampleImage = PshUtil.createImage(imagePath);
        exampleLabel.setImage(exampleImage);

        // dispose example image
        exampleLabel.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (exampleImage != null && !exampleImage.isDisposed()) {
                    exampleImage.dispose();
                    exampleImage = null;
                }
            }

        });

    }

    /**
     * Creates the buttons.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    private void createButtons(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        parent.setLayout(gl);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        parent.setLayoutData(gd);

        // create the button
        Button dismissButton = new Button(parent, SWT.PUSH);
        dismissButton.setText("Dismiss");
        dismissButton.setLayoutData(new GridData(BUTTON_WIDTH, BUTTON_HEIGHT));

        dismissButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Override button bar creation to do nothing.
     * 
     * @param parent
     */
    @Override
    protected Control createButtonBar(Composite parent) {
        // do nothing, no button bar needed
        return null;
    }

}
