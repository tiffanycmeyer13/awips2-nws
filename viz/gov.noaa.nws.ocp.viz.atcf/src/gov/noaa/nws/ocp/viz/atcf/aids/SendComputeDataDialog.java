/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

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
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.SendAtcfDataToWcossRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Dialog for "Send Compute Data" under "Aids"=>"Create Objective Forecast".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Aug 12, 2019  66071      mporricelli  Initial creation
 * Nov 21, 2019  69937      mporricelli  Updated for sending files
 *                                       to LDM rather than directly
 *                                       to WCOSS
 * Dec 13, 2019  71984      mporricelli  Notify user if storm guidance
 *                                       does not come back from WCOSS
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
public class SendComputeDataDialog extends OcpCaveChangeTrackDialog {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SendComputeDataDialog.class);

    private Storm storm;

    /**
     * Constructor
     *
     * @param parent
     */
    public SendComputeDataDialog(Shell parent, Storm storm) {

        super(parent);

        this.storm = storm;

        setText("Send Compute Data - " + storm.getStormName() + " "
                + storm.getStormId());
    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {

        createDataComp(shell);
        createControlButtons(shell);
    }

    /**
     * Create the layout.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, true);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginHeight = 5;
        dtgInfoLayout.marginWidth = 20;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        // Create label with storm information
        Label nameLbl = new Label(mainComp, SWT.NONE);
        String nameStr = "" + storm.getCycloneNum() + " " + storm.getYear()
                + " " + storm.getRegion() + " - " + storm.getStormName() + " ";
        nameLbl.setText(nameStr);
        GridData nameLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        nameLbl.setLayoutData(nameLblData);
    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite buttonComp2 = new Composite(parent, SWT.NONE);
        GridLayout buttonCompLayout2 = new GridLayout(2, true);
        buttonCompLayout2.marginWidth = 40;
        buttonCompLayout2.horizontalSpacing = 30;
        buttonComp2.setLayout(buttonCompLayout2);
        buttonComp2.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button sendButton = new Button(buttonComp2, SWT.PUSH);
        sendButton.setText("Send");
        sendButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        sendButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                Thread thread = new Thread(() -> {
                    try {
                        Object message = processStorm(storm);
                        statusHandler.handle(Priority.INFO, message.toString());

                    } catch (Exception ee) {
                        statusHandler.handle(Priority.SIGNIFICANT,
                                "ATCF: Problem sending data to WCOSS. ", ee);
                    }
                });
                thread.start();

                close();
            }
        });

        Button cancelButton = new Button(buttonComp2, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Initiate processes to get the relevant storm data and send to WCOSS
     *
     * @param storm
     * @return message of success or problems
     */
    private Object processStorm(Storm storm) {

        Object retval = null;
        List<String> baseDtgs = AtcfDataUtil.getDateTimeGroups(storm);
        try {
            String dtg = Collections.max(baseDtgs);
            /*
             * Send request to edex to run script to push files to LDM Server
             * for subsequent processing on WCOSS
             */
            SendAtcfDataToWcossRequest sendReq = new SendAtcfDataToWcossRequest(
                    storm, dtg);

            retval = ThriftClient.sendRequest(sendReq);
            if (retval instanceof String
                    && !((String) retval).contains("Success")) {
                statusHandler.handle(Priority.SIGNIFICANT, (String) retval);
            }
        } catch (VizException | NoSuchElementException e) {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "Exception while sending ATCF data to LDM.", e);
            retval = "Exception while sending ATCF data to LDM";
        }

        return retval;
    }

}
