/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisState;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.GenesisToStormRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.UpdateGenesisRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.TrackColorUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResourceData;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;

/**
 * Dialog for Spawning an Invest from a Genesis
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 10, 2020 79571      wpaintsil   Initial creation.
 * Dec 10, 2020 85849      jwu         Use existing valid sandbox in getBDeckRecords().
 * Dec 16, 2020 86027      jwu         Update genesis list after spawning into storm.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class SpawnInvestDialog extends CaveSWTDialog {
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(StartGenesisDialog.class);

    private static final int STORM_NAME_LENGTH = Storm.NAME_SIZE;

    private static final String INVEST_STORM_NAME = "INVEST";

    /**
     * OK button
     */
    private Button okButton;

    private Label numberLbl;

    private Label yearLbl;

    private Label basinLbl;

    private Label stormNameLbl;

    private Text stormNameText;

    private Spinner stormNumSpinner;

    private Genesis genesis;

    /**
     * Constructor
     *
     * @param parent
     * @param genesis
     */
    public SpawnInvestDialog(Shell parent, Genesis genesis) {
        super(parent, SWT.SHEET);
        setText("Spawn an Invest Area");
        this.genesis = genesis;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createTopLabels(shell);
        createStormNameNum(shell);
        createControlButtons(shell);

        populateData();
    }

    /**
     * Display the data from the Genesis
     */
    private void populateData() {
        if (genesis != null) {
            numberLbl.setText(String.valueOf(genesis.getGenesisNum()));
            yearLbl.setText(String.valueOf(genesis.getYear()));
            basinLbl.setText(AtcfBasin.getDescByName(genesis.getRegion()));
            stormNameLbl.setText(genesis.getGenesisName());

            stormNumSpinner.setMinimum(90);
            stormNumSpinner.setMaximum(99);
            stormNumSpinner.setSelection(0);
            stormNameText.setText(INVEST_STORM_NAME);
        }
    }

    /**
     * Create the labels for the storm info.
     *
     * @param shell
     */
    private void createTopLabels(Shell shell) {
        Composite topLabelComp = new Composite(shell, SWT.NONE);
        topLabelComp.setLayout(new GridLayout(4, false));
        topLabelComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        new Label(topLabelComp, SWT.NONE).setText("#");
        new Label(topLabelComp, SWT.NONE).setText("Year");
        new Label(topLabelComp, SWT.NONE).setText("Basin");
        new Label(topLabelComp, SWT.NONE).setText("Storm Name");

        numberLbl = new Label(topLabelComp, SWT.NONE);
        yearLbl = new Label(topLabelComp, SWT.NONE);
        basinLbl = new Label(topLabelComp, SWT.NONE);
        stormNameLbl = new Label(topLabelComp, SWT.NONE);

    }

    /**
     * Create the Storm Name section.
     */
    private void createStormNameNum(Shell shell) {
        Composite stormNameComp = new Composite(shell, SWT.NONE);
        stormNameComp.setLayout(new GridLayout(1, true));
        stormNameComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        new Label(stormNameComp, SWT.NONE).setText("Enter new storm name");
        stormNameText = new Text(stormNameComp, SWT.BORDER);

        Composite stormNumComp = new Composite(shell, SWT.NONE);
        stormNumComp.setLayout(new GridLayout(1, true));
        stormNumComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        new Label(stormNumComp, SWT.NONE).setText("Enter new storm number");
        stormNumSpinner = new Spinner(stormNumComp, SWT.BORDER);

    }

    /**
     * Create bottom buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 100;

        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        okButton = new Button(buttonComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!verifyStormId() || !verifyName()) {
                    MessageBox errorDialog = new MessageBox(shell,
                            SWT.ICON_QUESTION | SWT.OK);
                    errorDialog.setText("Alert");

                    String errorString = "Please fix the following fields before saving :";
                    errorString += (verifyStormId() ? ""
                            : "\nThis Storm already exists.");

                    errorString += (verifyName() ? ""
                            : "\nName field is invalid or has been used.");

                    errorDialog.setMessage(errorString);
                    errorDialog.open();
                } else {
                    saveInvest();
                    close();
                }
            }
        });
        okButton.setFocus();

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        cancelButton.setFocus();
    }

    /**
     * Save a new Invest Storm to the database.
     */
    protected void saveInvest() {

        GenesisToStormRequest genesisToStormReq = new GenesisToStormRequest();
        genesisToStormReq.setCandidate(genesis);

        genesis.setCycloneNum(Integer.parseInt(stormNumSpinner.getText()));

        genesisToStormReq
                .setCycloneNum(Integer.parseInt(stormNumSpinner.getText()));

        genesisToStormReq.setStormName(stormNameText.getText());

        try {
            String investId = (String) ThriftClient
                    .sendRequest(genesisToStormReq);

            Storm newInvest = AtcfDataUtil.getStorm(investId);
            if (newInvest != null) {
                AtcfDataUtil.updateStormList(newInvest, false);

                // Set genesis state to 'TC' to indicate it is a storm now.
                UpdateGenesisRequest updateGenesisRequest = new UpdateGenesisRequest();
                genesis.setGenesisState(GenesisState.TC.toString());

                Calendar cal = TimeUtil.newGmtCalendar();
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                genesis.setEndDTG(cal);

                updateGenesisRequest.setGenesis(genesis);

                ThriftClient.sendRequest(updateGenesisRequest);

                AtcfDataUtil.updateGenesisList(genesis);

                selectStorm(newInvest);

                AtcfSession.getInstance().getSideBar().createStormSwitcher();

                MessageBox successDialog = new MessageBox(shell,
                        SWT.ICON_QUESTION | SWT.OK);
                successDialog.setText("Alert");

                successDialog.setMessage(
                        "Storm " + newInvest.getStormId() + " saved");

                successDialog.open();
            }
        } catch (Exception e) {
            logger.warn("SpawnInvestDialog - Adding New Storm Failed", e);
        }

    }

    /**
     * Select a storm into current AtcfResource.
     *
     * @param storm
     *            Storm
     */
    private void selectStorm(Storm storm) {

        AtcfResourceData rscData = AtcfSession.getInstance().getAtcfResource()
                .getResourceData();
        boolean stormInResource = rscData.stormInResource(storm);

        // Set to refresh the display
        boolean needRefresh = true;

        // Add the storm only if it is not in resource yet.
        if (!stormInResource) {
            AtcfProduct stormProduct = new AtcfProduct(storm);
            rscData.addProduct(stormProduct);
            stormProduct.setAccepted(true);

            Map<String, List<BDeckRecord>> bdeckData = AtcfDataUtil
                    .getGenesisBDeckRecordMap(genesis);

            if (bdeckData != null && !bdeckData.isEmpty()) {
                stormProduct.setBDeckData(bdeckData);

                // Retrieve preferences in atcfsite.prefs
                AtcfSitePreferences prefs = AtcfConfigurationManager
                        .getInstance().getPreferences();

                BestTrackProperties prop = new BestTrackProperties(prefs);

                // Bold is from this dialog.
                prop.setBoldLine(false);

                // Color is from colsel.dat
                List<Integer> usedColors = AtcfSession.getInstance()
                        .getAtcfResource().getResourceData()
                        .getUsedTrackColorIndexes();

                TrackColorUtil tcu = new TrackColorUtil(usedColors);
                prop.setTrackColor(tcu.getNextTrackColorIndex());

                prop.setStormSymbols(true);

                prop.setSpecialTypePosition(false);

                // Create storm track
                BestTrackGenerator btkGen = new BestTrackGenerator(
                        AtcfSession.getInstance().getAtcfResource(), prop,
                        storm);
                btkGen.create();
                needRefresh = false;
            } else {
                stormProduct.setBdeckSandboxID(-1);
            }
        } else {
            okButton.setEnabled(false);
        }

        // Erase the storm that is selected before but not accepted..
        if (needRefresh
                && EditorUtil.getActiveEditor() instanceof AbstractEditor) {
            ((AbstractEditor) EditorUtil.getActiveEditor()).refresh();
        }

    }

    /**
     * Makes sure the storm's ID is unique
     *
     * @return isUnique?
     */
    public boolean verifyStormId() {
        return !(AtcfDataUtil.isCycloneNumUsed(genesis.getRegion(),
                genesis.getYear(),
                Integer.parseInt(stormNumSpinner.getText())));
    }

    /**
     * Verifies Name field
     *
     * @return valid
     */
    public boolean verifyName() {
        boolean isValid = true;
        String text = stormNameText.getText();

        /*
         * TODO if the length is over 10, it will be truncated and use the storm
         * name lookup table
         */
        if (text.isEmpty() || text.length() > STORM_NAME_LENGTH) {
            return false;
        } else if (INVEST_STORM_NAME.equals(text)) {
            // Allow "INVEST" to be used for multiple times
            return true;
        }

        isValid = !(AtcfDataUtil.isStormNameUsed(genesis.getRegion(),
                genesis.getYear(), text));

        return isValid;
    }

}
