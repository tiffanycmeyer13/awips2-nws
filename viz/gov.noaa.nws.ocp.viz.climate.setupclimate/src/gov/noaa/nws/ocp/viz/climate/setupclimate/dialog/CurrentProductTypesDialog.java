/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;

/**
 * This dialog displays the climate product types for the current report period
 * (am/im/pm/mon/sea/ann) from the "Options" menu in climate setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#   Engineer    Description
 * -----------  --------- ----------- --------------------------
 * Jan 24, 2017 20640     jwu         Initial creation
 * 17 MAY 2017  33104     amoore      FindBugs. Package reorg.
 * 19 SEP 2017  38124     amoore      Use GC for text control sizes.
 *                                    Fix cutting off.
 * 15 NOV 2017   39338    amoore      When loading a default product, use current localization site as node.
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 * 
 */
public class CurrentProductTypesDialog extends ClimateCaveDialog {

    /**
     * Label to show the current report period.
     */
    private Label rptPeriodLbl;

    /**
     * Label to hint the user to double-click a product type.
     */
    protected Label hintLbl;

    /**
     * Composite to list product types for the current report period.
     */
    private Composite productTypesComp;

    /**
     * ClimateSetupDialog that invokes this dialog.
     */
    protected ClimateSetupDialog climateSetupDlg;

    /**
     * List of current product type for the selected report period.
     */
    private List<ClimateProductType> productTypes;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent shell
     * @param climateSetupDlg
     *            The ClimateSetupDialog invoking this dialog
     * @param pTypes
     *            List of ClimateProductType to be displayed.
     */
    public CurrentProductTypesDialog(Shell parent,
            ClimateSetupDialog climateSetupDlg,
            java.util.List<ClimateProductType> pTypes) {
        super(parent);

        setText("Current Product Types");

        this.climateSetupDlg = climateSetupDlg;
        this.productTypes = pTypes;
    }

    /**
     * Initialize the dialog components
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                close();
            }
        });

        // Create the main content.
        createComponents(shell);
    }

    /**
     * Create the main controls.
     * 
     * @param parent
     *            Composite
     */
    private void createComponents(Composite parent) {
        Composite controlComp = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 10;
        controlComp.setLayout(gl);

        GridData controlCompGD = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        controlCompGD.heightHint = 410;
        controlComp.setLayoutData(controlCompGD);

        // "Current Products" label
        GridData lblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label currentPrdLbl = new Label(controlComp, SWT.CENTER);
        currentPrdLbl.setText("Current Products:");
        currentPrdLbl.setLayoutData(lblGD);

        // A separator
        GridData sepGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label separatorLbl = new Label(controlComp,
                SWT.HORIZONTAL | SWT.SEPARATOR);
        separatorLbl.setLayoutData(sepGD);

        // Label to show report period
        rptPeriodLbl = new Label(controlComp, SWT.CENTER);
        rptPeriodLbl.setText("Daily Morning");
        rptPeriodLbl.setLayoutData(lblGD);

        // Label to show Prod ID and Type
        GridData idGD = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        idGD.horizontalIndent = 23;
        Label idTypeLbl = new Label(controlComp, SWT.CENTER);
        idTypeLbl.setText("Prod ID         Type");
        idTypeLbl.setLayoutData(idGD);

        // A separator
        Label separatorLbl1 = new Label(controlComp,
                SWT.HORIZONTAL | SWT.SEPARATOR | SWT.SHADOW_OUT);
        separatorLbl1.setLayoutData(sepGD);

        // Scrollable composite to show product types
        ScrolledComposite productTypesScroll = new ScrolledComposite(
                controlComp, SWT.BORDER | SWT.V_SCROLL);

        GC gc = new GC(productTypesScroll);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        productTypesScroll.setLayout(new GridLayout(1, false));
        GridData scrollGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        scrollGD.minimumWidth = 20 * fontWidth;
        productTypesScroll.setLayoutData(scrollGD);

        productTypesComp = new Composite(productTypesScroll, SWT.NONE);

        GridLayout contentLayout = new GridLayout(2, true);
        contentLayout.horizontalSpacing = 36;
        contentLayout.marginLeft = 16;
        contentLayout.marginRight = 6;
        productTypesComp.setLayout(contentLayout);
        productTypesComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Add each product types as an item.
        createProductTypeItems(productTypesComp);

        // Set the composite as content for the scrollable composite.
        productTypesScroll.setContent(productTypesComp);
        productTypesScroll.setExpandHorizontal(true);
        productTypesScroll.setExpandVertical(true);
        productTypesScroll.setMinSize(
                productTypesComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // Hint label for "Double-Click".
        hintLbl = new Label(controlComp, SWT.CENTER);
        hintLbl.setText("<Double-Click\nto Open Product>");
        GridData hintLblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        hintLblGD.heightHint = 40;
        hintLbl.setLayoutData(hintLblGD);
        hintLbl.setVisible(false);

        // A separator
        Label separatorLbl2 = new Label(controlComp,
                SWT.HORIZONTAL | SWT.SEPARATOR);
        separatorLbl2.setLayoutData(sepGD);

        // Close button
        Button closeBtn = new Button(controlComp, SWT.PUSH);
        GridData closeGD = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        closeBtn.setLayoutData(closeGD);
        closeBtn.setText("Close");
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

    }

    /**
     * Update the product types in the composite.
     * 
     * @param rptPeriod
     *            Descriptor of the report period
     * @param ptyps
     *            List of ClimateProductType
     */
    protected void updateProductTypes(String rptPeriod,
            List<ClimateProductType> ptyps) {

        Control[] wids = productTypesComp.getChildren();
        if (wids != null) {
            for (int kk = 0; kk < wids.length; kk++) {
                wids[kk].dispose();
            }
        }

        this.productTypes = ptyps;

        rptPeriodLbl.setText(rptPeriod);

        createProductTypeItems(productTypesComp);

        productTypesComp.pack();
        productTypesComp.layout();
    }

    /**
     * Add all current product types into the composite.
     * 
     * @param comp
     *            Composite to add the product types
     */
    private void createProductTypeItems(Composite comp) {

        GridData itemGD = new GridData(SWT.FILL, SWT.BEGINNING, true, false);

        GC gc = new GC(comp);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        itemGD.widthHint = 8 * fontWidth;
        for (final ClimateProductType ptyp : productTypes) {
            Label prodIDLbl = new Label(comp, SWT.LEFT);
            prodIDLbl.setText(ptyp.getProdId());
            prodIDLbl.setLayoutData(itemGD);
            prodIDLbl.setData(ptyp);

            // Show double-click hint
            prodIDLbl.addMouseTrackListener(new MouseTrackAdapter() {
                @Override
                public void mouseExit(MouseEvent e) {
                    hintLbl.setVisible(false);
                }

                @Override
                public void mouseEnter(MouseEvent e) {
                    hintLbl.setVisible(true);
                }
            });

            // Double-click to open the product type.
            prodIDLbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    climateSetupDlg.openProductType(ptyp);
                }
            });

            Label prodSrcLbl = new Label(comp, SWT.LEFT);
            prodSrcLbl.setText(ptyp.getReportType().getSource());
            prodSrcLbl.setLayoutData(itemGD);
        }
    }

}
