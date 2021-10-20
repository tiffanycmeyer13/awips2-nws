/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Change Obj Aid Defaults
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2019 #58555      dmanzella   initial creation
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChangeObjAidDefaultsDialog extends OcpCaveSWTDialog {

    /**
     * Constructor
     *
     * @param shell
     */
    public ChangeObjAidDefaultsDialog(Shell shell) {

        super(shell, SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
                | SWT.RESIZE);
        setText("Change Objective Aids Defaults");
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
        GridLayout mainLayout = new GridLayout(2, false);
        mainComposite.setLayout(mainLayout);

        createMainArea(mainComposite);
        createControlButtons(mainComposite);
    }

    /**
     * Creates the main area of the GUI
     *
     * @param parent
     */
    protected void createMainArea(Composite parent) {
        // Left side Composite
        Composite left = new Composite(parent, SWT.NONE);
        left.setLayout(new GridLayout(1, false));

        Label test = new Label(left, SWT.NONE);
        test.setText("Select objective aids from master list: ");

        GridData scrollGridData = new GridData(SWT.NONE, SWT.NONE, true, false);
        scrollGridData.widthHint = 225;
        scrollGridData.heightHint = 190;

        ScrolledComposite leftScroll = new ScrolledComposite(left,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        leftScroll.setLayoutData(scrollGridData);
        Label content = new Label(leftScroll, SWT.NONE);
        content.setSize(800, 400);
        content.setText("Example string \n Example String \n Example String");
        leftScroll.setContent(content);

        // TODO fill in data....

        // Right side Composite
        Composite right = new Composite(parent, SWT.NONE);
        right.setLayout(new GridLayout(1, false));

        Label titleRight = new Label(right, SWT.NONE);
        titleRight.setText("Default aids list: ");

        ScrolledComposite rightScroll = new ScrolledComposite(right,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        rightScroll.setLayoutData(scrollGridData);

        Label rightContent = new Label(rightScroll, SWT.NONE);
        rightContent.setSize(800, 1400);
        rightContent
                .setText("Example string \n Example String \n Example String");
        rightScroll.setContent(rightContent);

        // TODO Add button functionality...
    }

    /**
     * Creates the control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        // Button bar composite
        Composite controlButtons = new Composite(parent, SWT.NONE);
        controlButtons.setLayout(new GridLayout(3, true));
        GridData controlButtonsCompositeGridData = AtcfVizUtil
                .horizontalFillGridData();
        controlButtonsCompositeGridData.horizontalSpan = 2;
        controlButtonsCompositeGridData.verticalIndent = 25;
        controlButtons.setLayoutData(controlButtonsCompositeGridData);

        GridData addButtonGridData = new GridData();
        addButtonGridData.widthHint = 60;
        addButtonGridData.horizontalIndent = 100;

        Button add = new Button(controlButtons, SWT.PUSH);
        add.setLayoutData(addButtonGridData);
        add.setText("Add");

        GridData deleteButtonGridData = new GridData();
        deleteButtonGridData.widthHint = 70;
        deleteButtonGridData.horizontalIndent = 100;

        Button delete = new Button(controlButtons, SWT.PUSH);
        delete.setLayoutData(deleteButtonGridData);
        delete.setText("Delete");

        GridData clearButtonGridData = new GridData();
        clearButtonGridData.widthHint = 70;
        clearButtonGridData.horizontalIndent = 15;

        Button clearButton = new Button(controlButtons, SWT.PUSH);
        clearButton.setLayoutData(clearButtonGridData);
        clearButton.setText("Clear All");

        GridData helpButtonGridData = new GridData();
        AtcfVizUtil.createHelpButton(controlButtons,
                AtcfVizUtil.buttonGridData());

        Button okButton = new Button(controlButtons, SWT.PUSH);
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.setText("OK");
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        Button cancelButton = new Button(controlButtons, SWT.PUSH);
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        // TODO Add button functionality
    }
}