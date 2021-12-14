/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import java.util.Map;

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

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Track"=>"Display Partial Best Track".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 20, 2019 60741      dmanzella         Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class DisplayPartialBestTrackDialog extends OcpCaveSWTDialog {

    private Map<String, java.util.List<BDeckRecord>> bdeckDataMap;

    private List dtgList;

    private Storm storm;

    private AtcfResource atcfResource;

    /**
     * Constructor
     *
     * @param shell
     * @param bdeckDataMap
     */
    public DisplayPartialBestTrackDialog(Shell shell,
            Map<String, java.util.List<BDeckRecord>> bdeckDataMap) {

        super(shell);
        this.bdeckDataMap = bdeckDataMap;
        // Find the current ATCF resource.
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        this.storm = AtcfSession.getInstance().getActiveStorm();
        setText("Current Storm Track Segment - " + this.storm.getStormName()
                + " " + this.storm.getStormId());
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
     */
    protected void createContents() {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
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

        GridData displayLabelGridData = new GridData();
        displayLabelGridData.widthHint = 300;
        displayLabelGridData.horizontalAlignment = SWT.CENTER;

        Label infoLabel = new Label(left, SWT.CENTER);
        infoLabel.setLayoutData(displayLabelGridData);
        infoLabel.setText("Display a segment of the current storm track.");

        GridData dtgLabelGridData = new GridData();
        dtgLabelGridData.verticalIndent = 15;
        dtgLabelGridData.horizontalAlignment = SWT.CENTER;
        Label dtgLabel = new Label(left, SWT.CENTER);
        dtgLabel.setText("DTG");
        dtgLabel.setLayoutData(dtgLabelGridData);

        dtgList = new List(left, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData dtgGridData = new GridData();
        dtgGridData.widthHint = 150;
        dtgGridData.heightHint = 205;
        dtgGridData.horizontalIndent = 10;
        dtgGridData.verticalIndent = 5;
        dtgGridData.horizontalAlignment = SWT.CENTER;
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
                displayPartial();
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
        controlButtons.setLayout(new GridLayout(2, true));
        GridData ctrlBtnCompGridData = AtcfVizUtil.horizontalFillGridData();
        ctrlBtnCompGridData.horizontalSpan = 2;
        ctrlBtnCompGridData.verticalIndent = 5;
        controlButtons.setLayoutData(ctrlBtnCompGridData);

        GridData displayButtonGridData = AtcfVizUtil.buttonGridData();
        displayButtonGridData.horizontalSpan = 2;

        Button displayButton = new Button(controlButtons, SWT.NONE);
        displayButton.setText("Display Entire Track");
        displayButton.setLayoutData(displayButtonGridData);
        displayButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayAll();
            }
        });

        AtcfVizUtil.createHelpButton(controlButtons,
                AtcfVizUtil.buttonGridData());

        Button doneButton = new Button(controlButtons, SWT.PUSH);
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
     * Functionality for the display all button
     */
    private void displayAll() {
        AtcfProduct prd = atcfResource.getResourceData().getAtcfProduct(storm);

        BestTrackProperties temp = prd.getBestTrackProperties();

        if (temp != null) {
            temp.setSelectedDTGs(null);
            BestTrackGenerator btkGen = new BestTrackGenerator(atcfResource,
                    temp, storm);
            btkGen.create();
            dtgList.deselectAll();
        }
    }

    /**
     * Functionality for the display partial button
     */
    private void displayPartial() {
        if (dtgList.getSelectionCount() > 0) {
            AtcfProduct prd = atcfResource.getResourceData()
                    .getAtcfProduct(storm);

            BestTrackProperties temp = prd.getBestTrackProperties();
            if (temp != null) {
                temp.setSelectedDTGs(dtgList.getSelection());

                BestTrackGenerator btkGen = new BestTrackGenerator(atcfResource,
                        temp, storm);
                btkGen.create();
            }
        }
    }

    /**
     * Sets the initial selection of the dtg List
     */
    private void setDtgSelections() {
        AtcfProduct prd = atcfResource.getResourceData().getAtcfProduct(storm);
        if (prd.getBestTrackProperties() != null
                && prd.getBestTrackProperties().getSelectedDTGs() != null) {
            for (String string : prd.getBestTrackProperties()
                    .getSelectedDTGs()) {
                dtgList.select(dtgList.indexOf(string));
            }
        }
    }

}