/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

/**
 * Implementation of a PSH example form dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 01, 2017 #34294     astrakovsky  Initial creation.
 * Jun 07, 2017 #34955     jwu          Refactor to use common utility methods.
 * Jun 14, 2017 #34873     astrakovsky  Refactored to work as a generic 
 *                                      example form generator.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshSetupExampleFormDialog extends CaveJFACEDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int BUTTON_HEIGHT = 28;

    private static final int BUTTON_WIDTH = 120;

    private static final int TITLE_SPACING = 14;

    private static final int NOTES_SPACING = 24;

    /**
     * The dialog title.
     */
    private String dialogTitle;

    /**
     * The dialog label.
     */
    private String dialogLabel;

    /**
     * Lists holding strings to populate the fields
     */
    private List<String> fieldsList;

    private List<String> notesList;

    /**
     * Number of fields in a row.
     */
    private int rowWidth;

    /**
     * Number of fields per set (sets exist if not null).
     */
    private Integer fieldsPerSet;

    /**
     * Constructor
     * 
     * @param parentShell
     * @param dialogTitle
     * @param dialogLabel
     * @param fieldsList
     * @param notesList
     * @param rowWidth
     * @param fieldsPerSet
     */
    public PshSetupExampleFormDialog(Shell parentShell, String dialogTitle,
            String dialogLabel, List<String> fieldsList, List<String> notesList,
            int rowWidth, Integer fieldsPerSet) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);

        this.dialogTitle = dialogTitle;
        this.dialogLabel = dialogLabel;
        this.fieldsList = fieldsList;
        this.notesList = notesList;
        this.rowWidth = rowWidth;
        this.fieldsPerSet = fieldsPerSet;
    }

    /**
     * Constructor
     * 
     * @param parentShell
     * @param dialogTitle
     * @param dialogLabel
     * @param fieldsList
     * @param notesList
     * @param rowWidth
     */
    public PshSetupExampleFormDialog(Shell parentShell, String dialogTitle,
            String dialogLabel, List<String> fieldsList, List<String> notesList,
            int rowWidth) {
        this(parentShell, dialogTitle, dialogLabel, fieldsList, notesList,
                rowWidth, null);
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

        // Create the dismiss button
        Composite buttonsGroup = new Composite(top, SWT.NONE);
        createButtons(buttonsGroup);

        // create title label
        Label titleLabel = new Label(top, SWT.CENTER);
        titleLabel.setText(dialogLabel);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        titleLabel.setLayoutData(gd);

        // Create the fields
        Composite fieldsGroup = new Composite(top, SWT.NONE);
        createExampleFields(fieldsGroup);

        // Create the notes
        Composite notesGroup = new Composite(top, SWT.NONE);
        createNotes(notesGroup);

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
     * Creates the example fields.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    private void createExampleFields(Composite parent) {

        if (fieldsPerSet != null) {

            // set up layout
            GridLayout gl = new GridLayout(1, false);
            gl.marginWidth = 0;
            gl.marginTop = TITLE_SPACING;
            parent.setLayout(gl);

            // composite for each set of fields
            Composite fieldGroup = new Composite(parent, SWT.BORDER);
            ;
            gl = new GridLayout(rowWidth * 2, false);
            gl.marginWidth = 0;
            fieldGroup.setLayout(gl);

            // create the fields
            for (int i = 0; i < fieldsList.size(); i += 2) {

                // check when to create a new container
                if (i != 0 && i % 2 == 0 && (i / 2) % fieldsPerSet == 0) {
                    fieldGroup = new Composite(parent, SWT.BORDER);
                    fieldGroup.setLayout(gl);
                }

                // create field
                createExampleField(fieldGroup, fieldsList.get(i),
                        fieldsList.get(i + 1));
            }

        } else {

            // set up layout
            GridLayout gl = new GridLayout(rowWidth * 2, false);
            gl.marginWidth = 0;
            gl.marginTop = TITLE_SPACING;
            parent.setLayout(gl);

            // create the fields
            for (int i = 0; i < fieldsList.size(); i += 2) {
                createExampleField(parent, fieldsList.get(i),
                        fieldsList.get(i + 1));
            }
        }

    }

    /**
     * Creates the notes.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    private void createNotes(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginTop = NOTES_SPACING;
        parent.setLayout(gl);

        // create notes from list
        for (int i = 0; i < notesList.size(); i += 2) {
            createExampleField(parent, notesList.get(i), notesList.get(i + 1));
        }

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

    /**
     * Creates an example field.
     * 
     * @param parent
     *            - Composite, parent composite.
     * @param labelText
     *            - String, field label.
     * @param fieldText
     *            - String, sample field text.
     */
    void createExampleField(Composite parent, String labelText,
            String fieldText) {

        Label fieldName = new Label(parent, SWT.RIGHT);
        fieldName.setText(labelText);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        fieldName.setLayoutData(gd);

        Label exampleField = new Label(parent, SWT.LEFT);
        exampleField.setText(fieldText);
        gd = new GridData();
        exampleField.setLayoutData(gd);

    }

}
