/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfSubregion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.StormMotion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.StormState;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.UpdateStormRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * Dialog to update a storm, including "Correct a Storm", "Renumber a Storm",
 * and "Name an Existing Storm".
 *
 * <pre>
 *
 * Current implementation: 
 *
 * 1. A storm is update-able if it is not ARCHIVEd.
 * 2. Only updates storms in ATCF DB, not in the master storm.table 
 *    and and deck files.
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2020 82623      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class UpdateStormDialog extends CaveSWTDialog {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(UpdateStormDialog.class);

    /*
     * Storm
     */
    private Storm storm;

    /*
     * Storm name
     */
    private Text nameTxt;

    /*
     * Cyclone number
     */
    private CCombo cylNumCmb;

    /*
     * Sub-basin
     */
    private CCombo subBasinCmb;

    /*
     * Storm state
     */
    private CCombo stateCmb;

    /*
     * Storm motion
     */
    private String motion = "";

    /*
     * WT number
     */
    private CCombo wtCmb;

    /**
     * Constructor
     * 
     * @param parent
     */
    public UpdateStormDialog(Shell parent, Storm storm, String title) {
        super(parent);
        this.storm = storm;
        this.motion = storm.getMover();

        setText(title);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createStormInfoComp(shell);
        createControlButtons(shell);
    }

    /*
     * Create a composite for storm information.
     *
     * @param parent
     */
    private void createStormInfoComp(Composite parent) {

        Composite stormComp = new Composite(parent, SWT.NONE);

        GridLayout stormCompGL = new GridLayout(1, false);
        stormCompGL.verticalSpacing = 15;
        stormComp.setLayout(stormCompGL);
        stormComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Composite stormInfoComp = new Composite(stormComp, SWT.NONE);
        GridLayout stormInfoCompGL = new GridLayout(4, false);
        stormInfoCompGL.horizontalSpacing = 20;
        stormInfoComp.setLayout(stormInfoCompGL);
        stormInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label cycloneNumTitleLbl = new Label(stormInfoComp, SWT.NONE);
        cycloneNumTitleLbl.setText("#");
        cycloneNumTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label yearTitleLbl = new Label(stormInfoComp, SWT.NONE);
        yearTitleLbl.setText("Year");
        yearTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label basinTitleLbl = new Label(stormInfoComp, SWT.CENTER);
        basinTitleLbl.setText("Basin");
        basinTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label stormNameTitleLbl = new Label(stormInfoComp, SWT.NONE);
        stormNameTitleLbl.setText("Storm Name");
        stormNameTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label cycloneNumLbl = new Label(stormInfoComp, SWT.NONE);
        cycloneNumLbl.setText("" + storm.getCycloneNum());
        cycloneNumLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label yearLbl = new Label(stormInfoComp, SWT.NONE);
        yearLbl.setText("" + storm.getYear());
        yearLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label basinLbl = new Label(stormInfoComp, SWT.CENTER);
        String bsn = AtcfBasin.getDescByName(storm.getRegion());
        basinLbl.setText(bsn);
        basinLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label stormNameLbl = new Label(stormInfoComp, SWT.NONE);
        stormNameLbl.setText(storm.getStormName());
        stormNameLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        // Storm Name & cyclone number
        Composite nameNumberComp = new Composite(stormComp, SWT.NONE);
        GridLayout nameNumberGL = new GridLayout(2, false);
        nameNumberGL.marginLeft = 20;
        nameNumberGL.horizontalSpacing = 60;
        nameNumberComp.setLayout(nameNumberGL);
        nameNumberComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label nameLbl = new Label(nameNumberComp, SWT.NONE);
        nameLbl.setText("Enter new storm name");
        nameLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label cylNumLbl = new Label(nameNumberComp, SWT.NONE);
        cylNumLbl.setText("Select new storm number");
        cylNumLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        nameTxt = new Text(nameNumberComp, SWT.BORDER);
        nameTxt.setText(storm.getStormName());
        nameTxt.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        nameTxt.addVerifyListener(e -> e.text = e.text.toUpperCase());

        cylNumCmb = new CCombo(nameNumberComp, SWT.BORDER);
        cylNumCmb.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        String[] cylNumbers = AtcfVizUtil.getEntries(1, 99, 1, "%02d");
        cylNumCmb.setItems(cylNumbers);
        int index = cylNumCmb
                .indexOf(String.format("%02d", storm.getCycloneNum()));
        if (index < 0) {
            index = 0;
        }

        cylNumCmb.select(index);
        cylNumCmb.setText(cylNumbers[index]);

        // Sub-basin and storm state
        Composite basinStateComp = new Composite(stormComp, SWT.NONE);
        GridLayout basinStateGL = new GridLayout(2, false);
        basinStateGL.marginWidth = 10;
        basinStateGL.horizontalSpacing = 50;
        basinStateComp.setLayout(basinStateGL);
        basinStateComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label subBasinLbl = new Label(basinStateComp, SWT.NONE);
        subBasinLbl.setText("Sub Basin");
        subBasinLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label stateLbl = new Label(basinStateComp, SWT.NONE);
        stateLbl.setText("Select storm state");
        stateLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        subBasinCmb = new CCombo(basinStateComp, SWT.BORDER);
        subBasinCmb.setEditable(false);
        subBasinCmb.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        subBasinCmb.setItems(AtcfSubregion.getDescriptions());

        AtcfSubregion subg = AtcfSubregion.getSubregion(storm.getSubRegion());
        int subInd = 0;
        if (subg != null) {
            subInd = subBasinCmb.indexOf(subg.getDescription());
        }

        if (subInd < 0) {
            subInd = 0;
        }

        subBasinCmb.select(subInd);

        stateCmb = new CCombo(basinStateComp, SWT.BORDER);
        stateCmb.setEditable(false);
        stateCmb.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        for (StormState state : StormState.values()) {
            if (state != StormState.GENESIS) {
                stateCmb.add(state.name());
            }
        }

        String stormState = storm.getStormState();
        if (stormState.isEmpty()) {
            stormState = StormState.METWATCH.name();
        }

        int stateInd = 0;
        for (String state : stateCmb.getItems()) {
            if (stormState.equalsIgnoreCase(state)) {
                break;
            }

            stateInd++;
        }
        stateCmb.select(stateInd);

        // Storm motion and wt number
        Composite motionWtComp = new Composite(stormComp, SWT.NONE);
        GridLayout motionWtCompGL = new GridLayout(2, false);
        motionWtCompGL.marginWidth = 5;
        motionWtCompGL.horizontalSpacing = 90;
        motionWtComp.setLayout(motionWtCompGL);
        motionWtComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label stormMotionLbl = new Label(motionWtComp, SWT.NONE);
        stormMotionLbl.setText("Select storm motion");
        stormMotionLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label wtLbl = new Label(motionWtComp, SWT.NONE);
        wtLbl.setText("Select wt number");
        wtLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Group stormMotionGrp = new Group(motionWtComp, SWT.NONE);
        GridLayout stormMotionGrpGL = new GridLayout(1, false);
        stormMotionGrpGL.marginWidth = 20;
        stormMotionGrp.setLayout(stormMotionGrpGL);
        stormMotionGrp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        if (motion.isEmpty()) {
            motion = StormMotion.O.name();
        }

        for (StormMotion mover : StormMotion.values()) {
            Button motionBtn = new Button(stormMotionGrp, SWT.RADIO);
            motionBtn.setText(mover.getValue());
            motionBtn.setData(mover);
            motionBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    StormMotion stm = (StormMotion) e.widget.getData();
                    motion = stm.name();
                }
            });

            if (motion.toUpperCase().equals(mover.name())) {
                motionBtn.setSelection(true);
            }
        }

        wtCmb = new CCombo(motionWtComp, SWT.BORDER);
        wtCmb.setEditable(false);
        wtCmb.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        String[] wtNumbers = AtcfVizUtil.getEntries(1, 10, 1, "%d");
        wtCmb.setItems(wtNumbers);
        wtCmb.add("none");

        int wti = wtCmb.getItemCount() - 1;
        for (int ii = 0; ii < wtNumbers.length; ii++) {
            int num = Integer.parseInt(wtNumbers[ii]);
            if (num == storm.getWtNum()) {
                wti = ii;
                break;
            }
        }
        wtCmb.select(wti);
        wtCmb.setText(wtCmb.getItem(wti));
    }

    /*
     * Create control buttons
     * 
     * @param parent Parent composite
     */
    private void createControlButtons(Composite parent) {
        Composite btnComp = new Composite(parent, SWT.NONE);
        GridLayout btnGL = new GridLayout(2, true);
        btnGL.marginWidth = 15;
        btnGL.horizontalSpacing = 40;
        btnComp.setLayout(btnGL);
        btnGL.marginWidth = 100;

        btnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Button okButton = new Button(btnComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, true));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (updateStorm(storm)) {
                    close();
                }
            }
        });

        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setText("  Cancel  ");
        cancelBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, true));
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        cancelBtn.setFocus();
    }

    /*
     * Update a storm
     * 
     * @param storm Storm
     * 
     * @param motion Storm motion string.
     */
    private boolean updateStorm(Storm storm) {

        boolean valid = false;

        String validName = AtcfDataUtil.getValidStormName(shell, storm,
                nameTxt.getText(), true);

        if (validName != null) {

            int validNum = AtcfDataUtil.getValidCycloneNumber(shell, storm,
                    cylNumCmb.getText(), true);

            if (validNum > 0) {

                valid = true;

                // Copy the storm.
                Storm newStorm = storm.copy();
                newStorm.setStormName(validName);
                newStorm.setCycloneNum(validNum);

                String sbg = AtcfSubregion.getNameByDesc(subBasinCmb.getText());
                newStorm.setSubRegion(sbg);

                String st = StormState.valueOf(stateCmb.getText()).name();
                newStorm.setStormState(st);

                newStorm.setMover(motion);

                Integer wtNum = null;
                try {
                    wtNum = Integer.parseInt(wtCmb.getText());
                } catch (NumberFormatException ne) {
                    // null
                }
                newStorm.setWtNum(wtNum);

                UpdateStormRequest updateRequest = new UpdateStormRequest(storm,
                        newStorm);

                // Update the storm.
                try {
                    ThriftClient.sendRequest(updateRequest);

                    // Update storm list.
                    AtcfDataUtil.updateStormList(storm, true);
                    AtcfDataUtil.updateStormList(newStorm, false);

                } catch (VizException e) {
                    logger.warn("UpdateStormDialog - Failed to update storm "
                            + storm.getStormName() + "," + storm.getStormId());
                }
            }
        }

        return valid;
    }

}
