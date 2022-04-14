/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;
import com.raytheon.viz.ui.simulatedtime.SimulatedTimeOperations;

import gov.noaa.nws.ocp.common.dataplugin.psh.IssueType;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshStormType;
import gov.noaa.nws.ocp.viz.psh.PshPrintUtil;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Dialog for View/Send PSH.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2017 #35463     wpaintsil   Initial creation.
 * Aug 01, 2017 #35738     jwu         Build product with template.
 * Sep 14, 2017 #37365     jwu         Integrate with product builder/transmitter.
 * Sep 18, 2017 #36920     astrakovsky Added print button.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshViewSendDialog extends CaveJFACEDialog {

    /**
     * Dropdown list containing the types of storms.
     */
    private Combo typeCombo;

    /**
     * Dropdown list containing the routing types.
     */
    private Combo routeCombo;

    /**
     * Dropdown list containing the message types.
     */
    private Combo messageTypeCombo;

    /**
     * Dropdown list containing numbers for use with an unnamed tropical
     * depression.
     */
    private Combo depressionNumberCombo;

    /**
     * Label for tropical depression options
     */
    private Label depressionNumberLabel;

    /**
     * Checkbox for tropical depression options
     */
    private Button depressionCheckbox;

    /**
     * Font for the PSH View text
     */
    private Font productFont;

    /**
     * Text field to show the final PSH report.
     */
    private Text previewText;

    /**
     * The PSH View text
     */
    private String previewProduct = null;

    /**
     * Current PshData object.
     */
    private PshData pshData;

    /**
     * Current CAVE mode.
     */
    private boolean operationalMode;

    /**
     * Message types for update/correction dialog -
     * 
     * {@link}http://www.wmo.int/pages/prog/www/ois/Operational_Information/
     * Publications/WMO_386/AHLsymbols/AHLsymbols_en.html
     */
    private static final String[] REPORT_TYPES = { "ROU", "AAA", "AAB", "AAC",
            "AAD", "AAE", "AAF", "AAG", "CCA", "CCB", "CCC", "CCD", "CCE",
            "CCF", "CCG" };

    /**
     * Strings for AFOS routing code (handleOUP.py).
     */
    private static final String[] AFOS_ROUTING_CODE = { "ALL", "LOC", "000",
            "DEF", "CEN", "CES", "CSW", "EAS", "SOU", "WES" };

    /**
     * Constructor
     * 
     * @param parentShell
     *            Parent shell
     * @param pshData
     *            PshData object
     */
    protected PshViewSendDialog(Shell parentShell, PshData pshData) {
        super(parentShell);

        this.pshData = pshData;
        productFont = PshUtil.createFont("Courier", 12, SWT.BOLD);

        // Check CAVE Mode.
        CAVEMode mode = CAVEMode.getMode();
        operationalMode = (CAVEMode.OPERATIONAL.equals(mode)
                || CAVEMode.TEST.equals(mode));

        // Build the product.
        previewProduct = PshUtil.buildPshReport(pshData);
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
        mainLayout.verticalSpacing = 15;
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        top.setLayoutData(mainLayoutData);

        createStormTypeArea(top);
        createViewerArea(top);
        createTransmitArea(top);

        /*
         * Sets dialog title
         */
        getShell().setText("View/Send PSH");

        // Update GUI from PshData.
        updateGui(pshData);

        return top;
    }

    /**
     * Create a selection of storm types. If a tropical depression is selected,
     * the option to select a number for a depression with no name is enabled.
     * 
     * @param top
     */
    private void createStormTypeArea(Composite top) {
        Composite stormTypeAreaComp = new Composite(top, SWT.BORDER);
        GridLayout stormTypeLayout = new GridLayout(3, false);
        stormTypeAreaComp.setLayout(stormTypeLayout);
        stormTypeAreaComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite typeComp = new Composite(stormTypeAreaComp, SWT.NONE);
        GridLayout typeLayout = new GridLayout(2, false);
        typeComp.setLayout(typeLayout);
        typeComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        Label typeLabel = new Label(typeComp, SWT.NORMAL);
        typeLabel.setText("Storm Type:");

        typeCombo = new Combo(typeComp, SWT.BORDER | SWT.READ_ONLY);
        for (PshStormType pst : PshStormType.values()) {
            typeCombo.add(pst.getDesc());
        }

        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Enable the options for a tropical depression
                if (PshStormType.TROPICAL_DEPRESSION == PshStormType
                        .values()[typeCombo.getSelectionIndex()]) {
                    setDepressionControlsEnabled(true);

                } else {
                    setDepressionControlsEnabled(false);
                }

                updateStormType();
            }
        });

        typeCombo.select(0);

        Composite depressionComp = new Composite(stormTypeAreaComp, SWT.BORDER);
        GridLayout depressionLayout = new GridLayout(3, false);
        depressionComp.setLayout(depressionLayout);

        depressionCheckbox = new Button(depressionComp, SWT.CHECK);
        depressionCheckbox.setText(
                PshStormType.TROPICAL_DEPRESSION.getDesc() + "\nw/ No Name");

        depressionNumberLabel = new Label(depressionComp, SWT.NORMAL);
        depressionNumberLabel.setText("Number:");

        // selection of numbers from 1-20 as in the legacy GUI
        depressionNumberCombo = new Combo(depressionComp,
                SWT.BORDER | SWT.READ_ONLY);
        String[] numberSelections = new String[20];
        for (int i = 0; i < 20; i++) {
            numberSelections[i] = String.valueOf(i + 1);
        }
        depressionNumberCombo.setItems(numberSelections);

        depressionNumberCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateStormType();
            }
        });

        depressionNumberCombo.select(0);

        // Enable or disable the controls in this composite based on what storm
        // type is selected
        depressionComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        setDepressionControlsEnabled(
                PshStormType.TROPICAL_DEPRESSION == PshStormType
                        .values()[typeCombo.getSelectionIndex()]);

        // Enable/disable the number selection based on whether the box is
        // checked.
        depressionCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                depressionNumberLabel
                        .setEnabled(depressionCheckbox.getSelection());
                depressionNumberCombo
                        .setEnabled(depressionCheckbox.getSelection());

                if (depressionCheckbox.getSelection()) {
                    updateStormType();
                }
            }
        });

    }

    /**
     * Enable or disable the Tropical Depression options. The number combo is
     * enabled only when the checkbox is selected.
     * 
     * @param enabled
     */
    private void setDepressionControlsEnabled(boolean enabled) {
        depressionCheckbox
                .setSelection(enabled && depressionCheckbox.getSelection());
        depressionCheckbox.setEnabled(enabled);
        depressionNumberLabel
                .setEnabled(enabled && depressionCheckbox.getSelection());
        depressionNumberCombo
                .setEnabled(enabled && depressionCheckbox.getSelection());
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
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);

        textData.widthHint = PshUtil.getProductWidth(previewText, productFont);

        previewText.setLayoutData(textData);
        previewText.setEditable(false);
        previewText.setBackground(
                top.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        previewText.setForeground(
                top.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        previewText.setFont(productFont);
        textData.heightHint = previewText.getLineHeight() * 30;
        previewText.setText(previewProduct);
    }

    /**
     * Create the transmit area containing transmit options and the transmit
     * button.
     * 
     * @param top
     */
    private void createTransmitArea(Composite top) {
        Group transmitComp = new Group(top, SWT.SHADOW_IN);
        transmitComp.setText("Transmit PSH");
        GridLayout transmitLayout = new GridLayout(3, false);
        transmitLayout.marginRight = 10;
        transmitLayout.marginBottom = 5;
        transmitComp.setLayout(transmitLayout);
        transmitComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite typeRouteComp = new Composite(transmitComp, SWT.NONE);
        typeRouteComp.setLayout(new GridLayout(2, false));
        typeRouteComp
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        Composite messageTypeComp = new Composite(typeRouteComp, SWT.NONE);
        messageTypeComp.setLayout(new GridLayout(2, false));

        new Label(messageTypeComp, SWT.NORMAL).setText("Type:");
        messageTypeCombo = new Combo(messageTypeComp,
                SWT.BORDER | SWT.READ_ONLY);

        messageTypeCombo.setItems(REPORT_TYPES);
        messageTypeCombo.select(0);
        messageTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String typeId = messageTypeCombo.getText();
                IssueType issueType = new IssueType(typeId, IssueType.ROUTINE,
                        TimeUtil.newCalendar(), "");
                if (typeId.startsWith("AA")) {
                    issueType.setCategory(IssueType.UPDATED);
                } else if (typeId.startsWith("CC")) {
                    issueType.setCategory(IssueType.CORRECTED);
                }

                if (!issueType.isRoutine()) {
                    new PshCorrectUpdateDialog(getShell(), issueType, pshData)
                            .open();
                }

                /*
                 * The status has been recalculated when based on all message
                 * types. we need to save and rebuild now.
                 */
                updateProduct(true);
            }
        });

        Composite routeComp = new Composite(typeRouteComp, SWT.NONE);
        routeComp.setLayout(new GridLayout(2, false));
        new Label(routeComp, SWT.NORMAL).setText("Route:");
        routeCombo = new Combo(routeComp, SWT.BORDER | SWT.READ_ONLY);
        routeCombo.setItems(AFOS_ROUTING_CODE);
        routeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * Route change won't affect the final product content so only
                 * save is needed.
                 */
                pshData.setRoute(routeCombo.getText());
                updateProduct(false);
            }
        });

        routeCombo.select(0);

        Composite transmitCloseComp = new Composite(transmitComp, SWT.NONE);
        RowLayout transmitCloseLayout = new RowLayout();
        transmitCloseLayout.pack = false;
        transmitCloseLayout.marginHeight = 0;
        transmitCloseLayout.marginWidth = 0;
        transmitCloseComp.setLayout(transmitCloseLayout);
        transmitCloseComp.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, true, false));

        Button transmitButton = new Button(transmitCloseComp, SWT.PUSH);
        transmitButton.setText("Transmit");
        transmitButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                MessageDialog confirmDlg = new MessageDialog(getShell(),
                        "Transmit PSH Product", null,
                        "Are you sure you want to transmit this product?",
                        MessageDialog.QUESTION, new String[] { "Yes", "No" },
                        0);

                confirmDlg.open();

                if (confirmDlg.getReturnCode() == MessageDialog.OK) {

                    if (!SimulatedTimeOperations.isTransmitAllowed()) {
                        SimulatedTimeOperations.displayFeatureLevelWarning(
                                getShell(), "Transmission of PSH products");
                        return;
                    }

                    PshUtil.transmitPshReport(pshData, operationalMode);
                }
            }
        });

        Button printButton = new Button(transmitCloseComp, SWT.PUSH);
        printButton.setText("Print Report");
        printButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // print the preview product
                if (previewProduct != null && !previewProduct.isEmpty()) {
                    PshPrintUtil.getPshPrinter().printInput(previewProduct);
                }
            }
        });

        Button closeButton = new Button(transmitCloseComp, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

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

    /**
     * Update storm type.
     */
    private void updateStormType() {

        /*
         * Update storm type, storm number for subtropical depression.
         * 
         * Note - Change storm type will always reset the status to "Routine".
         */
        pshData.setStormType(
                PshStormType.values()[typeCombo.getSelectionIndex()]);
        pshData.setRoute(routeCombo.getText());

        int stormNumber = 0;
        if (depressionCheckbox.isEnabled()
                && depressionCheckbox.getSelection()) {

            stormNumber = Integer.parseInt(depressionNumberCombo.getText());
        }
        pshData.setStormNumber(stormNumber);

        // Reset the status to "Routine".
        pshData.setStatus(IssueType.ROUTINE);

        // Save & rebuild.
        updateProduct(true);
    }

    /**
     * Save and update the data and final product.
     */
    private void updateProduct(boolean rebuild) {

        // Save the product.
        PshUtil.savePshData(pshData);

        // Build report and display.
        if (rebuild) {
            previewProduct = PshUtil.buildPshReport(pshData);
            previewText.setText(previewProduct);
        }
    }

    /**
     * Update GUI selection from PshData.
     */
    private void updateGui(PshData pshData) {

        // Storm Type
        PshStormType stormType = pshData.getStormType();
        if (stormType != null) {
            typeCombo.setText(stormType.getDesc());

            // Storm number for tropical depression.
            if (stormType == PshStormType.TROPICAL_DEPRESSION) {
                depressionCheckbox.setEnabled(true);
                int stormNumber = pshData.getStormNumber();
                if (stormNumber > 0) {
                    depressionCheckbox.setSelection(true);
                    depressionNumberCombo.setText("" + stormNumber);
                } else {
                    depressionCheckbox.setSelection(false);
                    depressionNumberCombo.setText("1");
                }
            }
        }

        // Message Type - always start up with "ROU".
        pshData.setStatus(IssueType.ROUTINE);
        messageTypeCombo.select(0);

        // Route - always start up with "ALL".
        pshData.setRoute(AFOS_ROUTING_CODE[0]);
        routeCombo.select(0);

    }

}