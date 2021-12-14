/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog for Choose Data Type.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 15, 2018 #45686      dmanzella   initial creation
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChooseDataTypeDialog extends CaveSWTDialog {

    /**
     * Constructor
     * 
     * @param parent
     */
    public ChooseDataTypeDialog(Shell parent) {

        super(parent, SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER
                | SWT.TITLE | SWT.RESIZE);
        setText("Choose Data Type(s)");
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
        createDisplayRemoveButtons(mainComposite);
        createBottomGroup(mainComposite);
    }

    /**
     * Creates the top area of the GUI
     * 
     * @param parent
     */
    protected void createMainArea(Composite parent) {
        Label mainLabel = new Label(parent, SWT.NONE);
        mainLabel.setText("Choose Data Type(s):");

        GridData textWidthGridData = new GridData(SWT.NONE, SWT.NONE, true,
                false);
        textWidthGridData.widthHint = 710;
        textWidthGridData.heightHint = 300;
        textWidthGridData.horizontalSpan = 2;

        Text mainTextBox = new Text(parent, SWT.V_SCROLL);
        mainTextBox.setLayoutData(textWidthGridData);
        mainTextBox.setText(
                "Sample data \nSample data \nSample data \nSample data");
    }

    /**
     * Creates the Display and Remove buttons
     * 
     * @param parent
     */
    protected void createDisplayRemoveButtons(Composite parent) {

        Composite displayRemoveComposite = new Composite(parent, SWT.NONE);
        GridLayout displayRemoveGridLayout = new GridLayout(2, false);
        displayRemoveComposite.setLayout(displayRemoveGridLayout);
        GridData displayRemoveGridData = new GridData(SWT.CENTER, SWT.NONE,
                true, false);
        displayRemoveGridData.horizontalIndent = 20;
        displayRemoveComposite.setLayoutData(displayRemoveGridData);

        Button displayButton = new Button(displayRemoveComposite, SWT.CENTER);
        displayButton.setText("Display");
        displayButton.setLayoutData(displayRemoveGridData);

        Button removeButton = new Button(displayRemoveComposite, SWT.CENTER);
        removeButton.setText("Remove");
        removeButton.setLayoutData(displayRemoveGridData);
    }

    /**
     * Creates the bottom half of the GUI
     * 
     * @param parent
     */
    protected void createBottomGroup(Composite parent) {
        Composite bottomGroupComposite = new Composite(parent, SWT.NONE);
        GridLayout bottomGroupGridLayout = new GridLayout(2, false);
        bottomGroupComposite.setLayout(bottomGroupGridLayout);

        // Group to the left
        Group innerControls = new Group(bottomGroupComposite, SWT.NONE);
        innerControls.setLayout(new GridLayout(6, false));
        GridData groupGridData = new GridData(SWT.FILL, SWT.NONE, false, false);
        groupGridData.verticalSpan = 3;
        innerControls.setLayoutData(groupGridData);

        GridData ddlGridData = new GridData(SWT.RIGHT, SWT.NONE, false, false);
        ddlGridData.widthHint = 80;

        // First row of group

        GridData rightAlignText = new GridData(SWT.RIGHT, SWT.CENTER, true,
                false);
        rightAlignText.widthHint = 60;

        GridData rawDataTimeGridData = new GridData(SWT.RIGHT, SWT.CENTER, true,
                false);
        rawDataTimeGridData.horizontalIndent = 50;

        GridData hLabelWidth = new GridData(SWT.FILL, SWT.CENTER, true, false);
        hLabelWidth.widthHint = 130;

        Label placeHolder1 = new Label(innerControls, SWT.NONE);
        placeHolder1.setText(" ");

        Label rawDataTime = new Label(innerControls, SWT.NONE);
        rawDataTime.setText("Raw Data Time");
        rawDataTime.setLayoutData(rawDataTimeGridData);

        Combo rawDataTimeInput = new Combo(innerControls,
                SWT.DROP_DOWN | SWT.BORDER);
        rawDataTimeInput.setLayoutData(ddlGridData);

        Label utc = new Label(innerControls, SWT.LEFT);
        utc.setText("UTC");

        Label placeHolder2 = new Label(innerControls, SWT.NONE);
        placeHolder2.setText(" ");

        Label placeHolder3 = new Label(innerControls, SWT.NONE);
        placeHolder3.setText(" ");

        // Second row of group

        Label surfaceRpt = new Label(innerControls, SWT.NONE);
        surfaceRpt.setText(" Surface Rpt +/- ");
        surfaceRpt.setLayoutData(rightAlignText);

        Combo surfaceRptInput = new Combo(innerControls,
                SWT.DROP_DOWN | SWT.BORDER);
        surfaceRptInput.setLayoutData(ddlGridData);

        Label surfaceRptH = new Label(innerControls, SWT.NONE);
        surfaceRptH.setText("h");
        surfaceRptH.setLayoutData(hLabelWidth);

        Label raob = new Label(innerControls, SWT.NONE);
        raob.setText("    RAOB/PIBAL +/-");

        Combo raobInput = new Combo(innerControls, SWT.DROP_DOWN | SWT.BORDER);
        raobInput.setLayoutData(ddlGridData);

        Label raobH = new Label(innerControls, SWT.NONE);
        raobH.setText("h");

        // Third row of group

        Label scatWinds = new Label(innerControls, SWT.NONE);
        scatWinds.setText("  Scat Winds +/- ");
        scatWinds.setLayoutData(rightAlignText);

        Combo scatWindsInput = new Combo(innerControls,
                SWT.DROP_DOWN | SWT.BORDER);
        scatWindsInput.setLayoutData(ddlGridData);

        Label scatWindsH = new Label(innerControls, SWT.NONE);
        scatWindsH.setText("h");

        Label aC = new Label(innerControls, SWT.NONE);
        aC.setText("           A/C Rpt +/-");

        Combo aCInput = new Combo(innerControls, SWT.DROP_DOWN | SWT.BORDER);
        aCInput.setLayoutData(ddlGridData);

        Label aCH = new Label(innerControls, SWT.NONE);
        aCH.setText("h");

        // Fourth row of group
        Label trackWinds = new Label(innerControls, SWT.NONE);
        trackWinds.setText(" Track Winds +/- ");
        trackWinds.setLayoutData(rightAlignText);

        Combo trackWindsInput = new Combo(innerControls,
                SWT.DROP_DOWN | SWT.BORDER);
        trackWindsInput.setLayoutData(ddlGridData);

        Label trackWindsH = new Label(innerControls, SWT.NONE);
        trackWindsH.setText("h");

        Label altim = new Label(innerControls, SWT.NONE);
        altim.setText("Altim Sig Wave +/-");

        Combo altimInput = new Combo(innerControls, SWT.DROP_DOWN | SWT.BORDER);
        altimInput.setLayoutData(ddlGridData);

        Label altimH = new Label(innerControls, SWT.NONE);
        altimH.setText("h");

        // Outside group
        GridData buttonGridData = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        buttonGridData.widthHint = 125;
        buttonGridData.horizontalIndent = 5;

        GridData okButtonGridData = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        okButtonGridData.widthHint = 125;
        okButtonGridData.verticalIndent = 50;
        okButtonGridData.horizontalIndent = 5;

        Button satImage = new Button(bottomGroupComposite, SWT.NONE);
        satImage.setText("Sat Image...");
        satImage.setLayoutData(buttonGridData);

        Button okBtn = new Button(bottomGroupComposite, SWT.NONE);
        okBtn.setText("OK");
        okBtn.setLayoutData(okButtonGridData);

        Button cancel = new Button(bottomGroupComposite, SWT.NONE);
        cancel.setText("Cancel");
        cancel.setLayoutData(buttonGridData);
    }
}