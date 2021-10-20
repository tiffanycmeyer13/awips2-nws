/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.DeleteStormRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;

/**
 * Dialog to confirm to delete a storm.
 *
 * <pre>
 * Current implementation: 
 *
 * 1. A storm is delete-able if it is not ENDed and not ARCHIVEd.
 * 2. Delete from ATCF DB only, not from the master storm.table and deck files.
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
public class DeleteStormDialog extends CaveSWTDialog {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DeleteStormDialog.class);

    /**
     * Storm
     */
    private Storm storm;

    // Format for storm start and end date/time.
    private SimpleDateFormat dtgFormat = new SimpleDateFormat("yyyy-MM-dd-hh");

    /**
     * Constructor
     *
     * @param parent
     */
    public DeleteStormDialog(Shell parent, Storm storm) {
        super(parent);
        this.storm = storm;
        setText("Confirm Delete a Storm");
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

        Composite stormDateComp = new Composite(stormComp, SWT.NONE);
        GridLayout stormDateCompGL = new GridLayout(3, false);
        stormDateCompGL.marginWidth = 15;
        stormDateCompGL.marginBottom = 40;
        stormDateCompGL.horizontalSpacing = 20;
        stormDateComp.setLayout(stormDateCompGL);
        stormDateComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label startTitleLbl = new Label(stormDateComp, SWT.NONE);
        startTitleLbl.setText("Start Date/Time");
        startTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label endTitleLbl = new Label(stormDateComp, SWT.NONE);
        endTitleLbl.setText("End Date/Time");
        endTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label typeTitleLbl = new Label(stormDateComp, SWT.NONE);
        typeTitleLbl.setText("Storm Type");
        typeTitleLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label startLbl = new Label(stormDateComp, SWT.NONE);
        String startString = "9999-99-99-99";
        Calendar startCal = storm.getStartDTG();
        if (startCal != null) {
            startString = dtgFormat.format(startCal.getTime());
        }
        startLbl.setText(startString);
        startLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label endLbl = new Label(stormDateComp, SWT.NONE);
        String endString = "9999-99-99-99";
        Calendar endCal = storm.getEndDTG();
        if (endCal != null) {
            endString = dtgFormat.format(endCal);
        }
        endLbl.setText(endString);
        endLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label typeLbl = new Label(stormDateComp, SWT.NONE);
        typeLbl.setText(storm.getStormState());
        typeLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
    }

    /*
     * Create control buttons
     *
     * @param parent Parent composite
     */
    private void createControlButtons(Composite parent) {

        Label questionLbl = new Label(parent, SWT.NONE);
        questionLbl.setText("Do you really want to delete this storm?");
        questionLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

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
                deleteStorm(storm);
                close();
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
     * Delete a storm
     *
     * @param storm Storm
     */
    private void deleteStorm(Storm storm) {

        DeleteStormRequest deleteRequest = new DeleteStormRequest(
                storm.getRegion(), storm.getCycloneNum(), storm.getYear());

        try {
            ThriftClient.sendRequest(deleteRequest);

            // Delete from storm list.
            AtcfDataUtil.updateStormList(storm, true);
        } catch (VizException e) {
            logger.warn("DeleteStormDialog - Failed to delete storm "
                    + storm.getStormName() + "," + storm.getStormId() + e);
        }

    }

}
