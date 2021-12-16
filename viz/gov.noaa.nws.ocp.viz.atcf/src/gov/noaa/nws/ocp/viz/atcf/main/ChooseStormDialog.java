/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

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
import org.eclipse.ui.IEditorPart;

import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.TrackColorUtil;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.StormManagementDialog;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResourceData;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Choose A Storm dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 10, 2018 45691      wpaintsil   Initial creation.
 * Jun 21, 2018 51863      jwu         Test data retrieval from storms table.
 * Jun 28, 2018 51961      jwu         Test drawing B-Deck
 * Jul 05, 2018 52119      jwu         Link with ATCF session/resource.
 * Aug 31, 2018 53950      jwu         Use sandbox to checkin/checkout B Deck data.
 * Oct 02, 2018 55733      jwu         Reset active sandbox in AtcfResourceData for new storm.
 * Feb 05, 2019 58990      jwu         Add support for storm switcher.
 * Feb 19, 2019 60097      jwu         Update the logic for selecting a storm.
 * Feb 26, 2019 60613      jwu         Use BestTrackGenerator for drawing.
 * Mar 12, 2019 61359      jwu         Implement "Toggle Bold Track Line".
 * Mar 18, 2019 61605      jwu         Add track color option.
 * Sep 16, 2019 68603      jwu         Apply site preferences as default drawing options.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class ChooseStormDialog extends OcpCaveSWTDialog {

    /**
     * Widget for the list of storms
     */
    private java.util.List<Storm> storms = null;

    /**
     * Widget for the list of storms
     */
    private List stormList;

    /**
     * OK button
     */
    private Button okButton;

    /**
     * "ToggleBoldLine" checkbox.
     */
    private Button toggleBoldLineChkBtn;

    /**
     * Selection Counter
     */
    private int stormSelectionCounter = 0;

    /**
     * Archived Storms Dialog
     */
    private StormManagementDialog archivedStormsDialog;

    // Get an ATCF Resource
    private AtcfResource drawingLayer = null;

    // Active Editor Part.
    private IEditorPart editor = null;

    // Current storm and previous storm.
    private Storm curStorm;

    /**
     * Called when OK button is selected.
     */
    private Runnable okSelectedHandler;

    /**
     * Constructor
     *
     * @param parentShell
     */
    public ChooseStormDialog(Shell parentShell) {
        super(parentShell, SWT.SHEET);
        setText("Choose a Storm");

        // Find the current ATCF resource.
        drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Activate Editor Part.
        editor = EditorUtil.getActiveEditor();
    }

    /**
     * Initialize the dialog components.
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createStormList(shell);
        createControlButtons(shell);

        initialize();
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
        GridData stormListGD = new GridData(SWT.CENTER, SWT.DEFAULT, false,
                false);
        stormListComp.setLayoutData(stormListGD);

        Label chooseLabel = new Label(stormListComp, SWT.NONE);
        chooseLabel.setText("Choose a storm:");

        stormList = new List(stormListComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

        storms = AtcfDataUtil.getFullStormList();

        for (Storm strm : storms) {
            String item = strm.getStormId().substring(2, 4) + " "
                    + strm.getYear() + " " + strm.getRegion() + " - "
                    + strm.getStormName();
            stormList.add(item);
        }

        stormList.setLayoutData(new GridData(400, 310));

        stormList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                /*
                 * Select storm as into ATCF resource and then draw the b-deck
                 * for this storm.
                 */
                if (stormList.getSelectionCount() > 0 && okButton != null) {
                    stormSelectionCounter++;
                    /*
                     * TODO SWT.List always select the first item. We want the
                     * list open with no item selected so a counter is used here
                     * to get around that. Maybe there is a better way to do it?
                     */
                    if (stormSelectionCounter > 1) {
                        curStorm = storms.get(stormList.getSelectionIndex());
                        selectStorm(curStorm);
                    } else {
                        stormList.deselectAll();
                    }
                }
            }
        });

        toggleBoldLineChkBtn = new Button(stormListComp, SWT.CHECK);
        toggleBoldLineChkBtn.setText("Toggle Bold Track Line");
        toggleBoldLineChkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfProduct prd = drawingLayer.getResourceData()
                        .getAtcfProduct(curStorm);

                if (prd != null) {
                    BestTrackProperties prop = prd.getBestTrackProperties();
                    prop.setBoldLine(toggleBoldLineChkBtn.getSelection());
                    BestTrackGenerator btkGen = new BestTrackGenerator(
                            drawingLayer, prop, curStorm);
                    btkGen.create();
                }
            }
        });

    }

    /*
     * Create control buttons at the bottom.
     *
     * @param parent parent composite
     */
    private void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(4, true);
        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        Button moreButton = new Button(buttonComp, SWT.PUSH);
        moreButton.setText("More Storms");
        moreButton.setLayoutData(AtcfVizUtil.buttonGridData());
        moreButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                if ((archivedStormsDialog == null)
                        || archivedStormsDialog.getShell().isDisposed()) {
                    archivedStormsDialog = new StormManagementDialog(shell,
                            "Archived Storms");
                    archivedStormsDialog.addCloseCallback(
                            ov -> archivedStormsDialog = null);
                }
                archivedStormsDialog.open();

            }
        });

        okButton = new Button(buttonComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.setEnabled(false);
        okButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                // Accept this storm and update sidebar.
                AtcfProduct prd = drawingLayer.getResourceData()
                        .getAtcfProduct(curStorm);

                prd.setAccepted(true);

                AtcfSession.getInstance().getSideBar().createStormSwitcher();

                close();

                if (okSelectedHandler != null) {
                    okSelectedHandler.run();
                }
            }
        });

        Button closeButton = new Button(buttonComp, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.setLayoutData(AtcfVizUtil.buttonGridData());
        closeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (okButton.getEnabled()) {

                    // Remove the storm product
                    drawingLayer.removeUnacceptedAtcfProduct();
                    if (editor instanceof AbstractEditor) {
                        ((AbstractEditor) editor).refresh();
                    }

                    drawingLayer.getResourceData().refreshLegend();

                    // Update sidebar.
                    AtcfSession.getInstance().getSideBar()
                            .createStormSwitcher();
                }

                close();
            }
        });

        closeButton.setFocus();
    }

    /**
     * Set the client handler for clicking the "Ok" button. Currently only one
     * handler may be set.
     *
     * @param listener
     */
    public void setOkSelectedHandler(Runnable okSelectedHandler) {
        if (this.okSelectedHandler != null) {
            throw new IllegalStateException("ChooseStormDialog already has an OK button handler");
        }
        this.okSelectedHandler = okSelectedHandler;
    }

    /**
     * Select a storm into current AtcfResource.
     *
     * @param storm
     *            Storm
     */
    private void selectStorm(Storm storm) {

        AtcfResourceData rscData = drawingLayer.getResourceData();
        boolean stormInResource = rscData.stormInResource(storm);

        // Remove the storm that is selected before but not accepted.
        boolean needRefresh = drawingLayer.removeUnacceptedAtcfProduct();

        boolean isGenesis = storm.getStormName().startsWith("GENESIS");

        // Add the storm only if it is not in resource yet.
        if (!stormInResource) {
            okButton.setEnabled(true);
            AtcfProduct stormProduct = new AtcfProduct(storm);

            rscData.addProduct(stormProduct);

            Map<String, java.util.List<BDeckRecord>> bdeckData = null;

            if (!isGenesis) {
                bdeckData = AtcfDataUtil.getBDeckRecords(storm);
            } else {
                // Retrieve Genesis and then convert into BDeckRecord
                Genesis gis = AtcfDataUtil.getGenesis(storm.getStormId());
                if (gis != null) {
                    bdeckData = AtcfDataUtil.getGenesisBDeckRecordMap(gis);
                }
            }

            // Use non-bold line for new track.
            toggleBoldLineChkBtn.setSelection(false);

            if (bdeckData != null && !bdeckData.isEmpty()) {
                stormProduct.setBDeckData(bdeckData);

                // Retrieve preferences in atcfsite.prefs
                AtcfSitePreferences prefs = AtcfConfigurationManager
                        .getInstance().getPreferences();

                BestTrackProperties prop = new BestTrackProperties(prefs);

                // Bold is from this dialog.
                prop.setBoldLine(toggleBoldLineChkBtn.getSelection());

                // Color is from colsel.dat
                prop.setTrackColor(getTrackColorIndex());

                // Create storm track
                BestTrackGenerator btkGen = new BestTrackGenerator(drawingLayer,
                        prop, storm);
                btkGen.create();
                needRefresh = false;
            } else {
                stormProduct.setBdeckSandboxID(-1);
            }
        } else {
            okButton.setEnabled(false);
            toggleBoldLineChkBtn.setSelection(rscData.getAtcfProduct(storm)
                    .getBestTrackProperties().isBoldLine());
        }

        // Erase the storm that is selected before but not accepted..
        if (needRefresh && editor instanceof AbstractEditor) {
            ((AbstractEditor) editor).refresh();
        }

    }

    /**
     * Initialize the dialog when it opens up.
     */
    private void initialize() {

        AtcfProduct prd = drawingLayer.getResourceData().getActiveAtcfProduct();

        // If there are active storm, set it as the selected.
        if (prd != null) {
            int index = -1;
            for (Storm strm : storms) {
                index++;
                if (strm == prd.getStorm()) {
                    break;
                }
            }

            if (index >= 0 && index < storms.size()) {
                stormList.select(index);
                curStorm = storms.get(index);
                stormSelectionCounter++;
            }

            toggleBoldLineChkBtn
                    .setSelection(prd.getBestTrackProperties().isBoldLine());

        } else {
            stormList.deselectAll();
        }

    }

    /**
     * Find the next un-used track color index in colsel.dat ("track_colors").
     *
     * Note - The first color in "track_colors" is reserved for active storm.
     *
     * @return index
     */
    private int getTrackColorIndex() {

        java.util.List<Integer> usedColors = drawingLayer.getResourceData()
                .getUsedTrackColorIndexes();

        TrackColorUtil tcu = new TrackColorUtil(usedColors);

        return tcu.getNextTrackColorIndex();
    }

}