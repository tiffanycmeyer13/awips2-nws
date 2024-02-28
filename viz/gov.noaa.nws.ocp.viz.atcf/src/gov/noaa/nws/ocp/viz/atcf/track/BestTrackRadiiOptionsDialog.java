/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Track"=>"Best Track Radii Options".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2019 61789      dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class BestTrackRadiiOptionsDialog extends OcpCaveSWTDialog {

    private Map<String, java.util.List<BDeckRecord>> bdeckDataMap;

    /**
     * Lists of the date time groups
     */
    private List dtgList;

    private Storm storm;

    private AtcfResource atcfResource;

    private AtcfProduct prd;

    /**
     * The ROCI toggle button
     */
    private Button rociBtn;

    /**
     * The 34knot toggle button
     */
    private Button thirtyFourKnotRadiiBtn;

    /**
     * The 50knot toggle button
     */
    private Button fiftyKnotRadiiBtn;

    /**
     * The 64knot toggle button
     */
    private Button sixtyFourKnotRadiiBtn;

    /**
     * The RMW toggle button
     */
    private Button rmwBtn;

    /**
     * Constructor
     *
     * @param shell
     * @param bdeckDataMap
     */
    public BestTrackRadiiOptionsDialog(Shell shell,
            Map<String, java.util.List<BDeckRecord>> bdeckDataMap) {

        super(shell);
        this.bdeckDataMap = bdeckDataMap;

        // Find the current ATCF resource.
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        this.storm = AtcfSession.getInstance().getActiveStorm();

        setText("Best Track Radii Display Options - "
                + this.storm.getStormName() + " " + this.storm.getStormId());
    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        prd = atcfResource.getResourceData().getAtcfProduct(storm);
        createContents();
    }

    /**
     * Create contents.
     */
    protected void createContents() {

        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 8;
        mainLayout.marginWidth = 15;
        mainLayout.verticalSpacing = 40;
        mainComposite.setLayout(mainLayout);
        mainComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        createMainArea(mainComposite);
        createControlButtons(mainComposite);

    }

    /**
     * Creates the main area of the GUI
     *
     * @param parent
     */
    protected void createMainArea(Composite parent) {
        Group mainGroup = new Group(parent, SWT.CENTER);
        GridLayout mainGrpLayout = new GridLayout(2, false);
        mainGrpLayout.marginHeight = 8;
        mainGrpLayout.marginWidth = 15;
        mainGrpLayout.horizontalSpacing = 30;
        mainGrpLayout.verticalSpacing = 40;
        mainGroup.setLayout(mainGrpLayout);
        GridData groupGridData = new GridData();
        groupGridData.horizontalAlignment = SWT.CENTER;
        mainGroup.setLayoutData(groupGridData);

        // Composite that contains the DTG list on the left
        Composite dtgListComp = new Composite(mainGroup, SWT.NONE);
        GridLayout dtgListLayout = new GridLayout(1, false);
        dtgListComp.setLayout(dtgListLayout);
        dtgListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label dtgLabel = new Label(dtgListComp, SWT.NONE);
        dtgLabel.setText("DTG");

        dtgList = new List(dtgListComp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData dtgGridData = new GridData(110, 206);
        dtgList.setLayoutData(dtgGridData);

        for (String dtg : bdeckDataMap.keySet()) {
            dtgList.add(dtg);
        }

        if (dtgList.getItemCount() > 0) {
            setDtgSelections();
        }

        dtgList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        // Composite that contains the toggle buttons on the right
        Composite dispOptionsComp = new Composite(mainGroup, SWT.NONE);
        GridLayout dispOptionsGridLayout = new GridLayout(1, false);
        dispOptionsComp.setLayout(dispOptionsGridLayout);
        dispOptionsComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData dispBtnGridData = new GridData(125, 30);
        dispBtnGridData.horizontalAlignment = SWT.FILL;
        dispBtnGridData.grabExcessVerticalSpace = true;

        Label displayLabel = new Label(dispOptionsComp, SWT.NONE);
        displayLabel.setText("Display");
        displayLabel.setLayoutData(dispBtnGridData);

        rociBtn = new Button(dispOptionsComp, SWT.CHECK);
        rociBtn.setText("ROCI");
        rociBtn.setLayoutData(dispBtnGridData);
        rociBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        thirtyFourKnotRadiiBtn = new Button(dispOptionsComp, SWT.CHECK);
        thirtyFourKnotRadiiBtn.setText("34 knot radii");
        thirtyFourKnotRadiiBtn.setLayoutData(dispBtnGridData);
        thirtyFourKnotRadiiBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        fiftyKnotRadiiBtn = new Button(dispOptionsComp, SWT.CHECK);
        fiftyKnotRadiiBtn.setText("50 knot radii");
        fiftyKnotRadiiBtn.setLayoutData(dispBtnGridData);
        fiftyKnotRadiiBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        sixtyFourKnotRadiiBtn = new Button(dispOptionsComp, SWT.CHECK);
        sixtyFourKnotRadiiBtn.setText("64 knot radii");
        sixtyFourKnotRadiiBtn.setLayoutData(dispBtnGridData);
        sixtyFourKnotRadiiBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        rmwBtn = new Button(dispOptionsComp, SWT.CHECK);
        rmwBtn.setText("RMW");
        rmwBtn.setLayoutData(dispBtnGridData);
        rmwBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        if (prd.getBestTrackProperties() != null) {
            rociBtn.setSelection(prd.getBestTrackProperties().isDrawROCI());
            thirtyFourKnotRadiiBtn.setSelection(
                    prd.getBestTrackProperties().isRadiiFor34Knot());
            fiftyKnotRadiiBtn.setSelection(
                    prd.getBestTrackProperties().isRadiiFor50Knot());
            sixtyFourKnotRadiiBtn.setSelection(
                    prd.getBestTrackProperties().isRadiiFor64Knot());
            rmwBtn.setSelection(prd.getBestTrackProperties().isDrawRMW());
        }

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                adjustOptionBtnStatus();
            }
        });

    }

    /**
     * Creates the control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        // Button bar composite
        Composite controlButtons = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnGridLayout = new GridLayout(2, false);
        ctrlBtnGridLayout.marginWidth = 10;
        ctrlBtnGridLayout.horizontalSpacing = 40;
        controlButtons.setLayout(ctrlBtnGridLayout);
        GridData ctrlBtnCompGridData = AtcfVizUtil.horizontalFillGridData();
        ctrlBtnCompGridData.horizontalSpan = 2;
        controlButtons.setLayoutData(ctrlBtnCompGridData);

        AtcfVizUtil.createHelpButton(controlButtons,
                AtcfVizUtil.buttonGridData());

        Button doneButton = new Button(controlButtons, SWT.NONE);
        doneButton.setLayoutData(AtcfVizUtil.buttonGridData());
        doneButton.setText("Done");
        doneButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /**
     * Functionality for clicking on the list
     */
    private void drawRadiiOptions() {

        BestTrackProperties temp = prd.getBestTrackProperties();
        if (temp != null) {
            temp.setRadiiFor64Knot(sixtyFourKnotRadiiBtn.getSelection());
            temp.setRadiiFor50Knot(fiftyKnotRadiiBtn.getSelection());
            temp.setRadiiFor34Knot(thirtyFourKnotRadiiBtn.getSelection());
            temp.setDrawRMW(rmwBtn.getSelection());
            temp.setDrawROCI(rociBtn.getSelection());
            temp.setSelectedRadiiDTGs(dtgList.getSelection());
            BestTrackGenerator btkGen = new BestTrackGenerator(atcfResource,
                    temp, storm);
            btkGen.create();
        }
    }

    /**
     * Sets the initial selection of the dtg List
     */
    private void setDtgSelections() {
        if (prd.getBestTrackProperties() != null && prd.getBestTrackProperties()
                .getSelectedRadiiDTGs() != null) {
            for (String string : prd.getBestTrackProperties()
                    .getSelectedRadiiDTGs()) {
                dtgList.select(dtgList.indexOf(string));
            }
        } else {
            dtgList.selectAll();
            prd.getBestTrackProperties()
                    .setSelectedRadiiDTGs(dtgList.getItems());
        }
    }

    /**
     * Sets the buttons when the dialog comes back into focus
     */
    private void adjustOptionBtnStatus() {
        rociBtn.setSelection(prd.getBestTrackProperties().isDrawROCI());
        thirtyFourKnotRadiiBtn
                .setSelection(prd.getBestTrackProperties().isRadiiFor34Knot());
        fiftyKnotRadiiBtn
                .setSelection(prd.getBestTrackProperties().isRadiiFor50Knot());
        sixtyFourKnotRadiiBtn
                .setSelection(prd.getBestTrackProperties().isRadiiFor64Knot());
        rmwBtn.setSelection(prd.getBestTrackProperties().isDrawRMW());

        setDtgSelections();
    }

}