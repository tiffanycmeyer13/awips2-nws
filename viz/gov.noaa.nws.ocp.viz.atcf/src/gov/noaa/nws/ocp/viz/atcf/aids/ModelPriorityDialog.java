/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ModelPriorityRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Dialog for "Model Priority under "Aids"=>"Create Objective Forecast".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * May 30, 2020  78922      mporricelli  Initial creation
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
public class ModelPriorityDialog extends OcpCaveChangeTrackDialog {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ModelPriorityDialog.class);

    private Storm storm;

    private String priority = "3";

    private static final int MISSING_INT_DD = 99;

    private static final String MISSING_DTG = "9999999999";

    // Titles for storm information parameters
    private static final String[] stormParameters = new String[] {
            "         Name", " Basin", " Num", "Sub\nBas", " Year", "Dev",
            "  Begin DTG", "  End DTG", "    State", "   WT",
            " Priority         " };

    // List to show current active storm data.
    private Text stormInfoText;

    /**
     * Constructor
     *
     * @param parent
     */
    public ModelPriorityDialog(Shell parent, Storm storm) {

        super(parent);

        this.storm = storm;

        setText("NWP Model Priority - " + storm.getStormName() + " "
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
        createStormInfoComp(shell);
        createPrioritySelectionComp(shell);
        createControlButtons(shell);
    }

    /**
     * Create the composite to select the priority.
     *
     * @param parent
     */
    private void createPrioritySelectionComp(Composite parent) {
        Composite prioritySelectComp = new Composite(parent, SWT.NONE);
        GridLayout prioritySelectLayout = new GridLayout(2, true);
        prioritySelectLayout.marginWidth = 5;
        prioritySelectLayout.marginHeight = 0;
        prioritySelectComp.setLayout(prioritySelectLayout);
        prioritySelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label priorityLabel = new Label(prioritySelectComp, SWT.NONE);
        priorityLabel.setText("      Priority: ");
        GridData priorityLabelData = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false);
        priorityLabel.setLayoutData(priorityLabelData);

        CCombo priorityCombo = new CCombo(prioritySelectComp,
                SWT.NONE | SWT.READ_ONLY);

        String[] priorityItems = new String[8];
        for (int ii = 0; ii < priorityItems.length; ii++) {
            priorityItems[ii] = String.valueOf(ii + 1);
        }
        priorityCombo.add("0");
        priorityCombo.setItems(priorityItems);
        priorityCombo.select(2);

        GridData priorityComboData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        priorityCombo.setLayoutData(priorityComboData);

        priorityCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                priority = priorityCombo.getText();
            }
        });
    }

    /**
     * Create the layout.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoLayout = new GridLayout(1, true);
        dataInfoLayout.verticalSpacing = 10;
        dataInfoLayout.marginHeight = 5;
        dataInfoLayout.marginWidth = 20;
        mainComp.setLayout(dataInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 200;

        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        Button sendButton = new Button(buttonComp, SWT.PUSH);
        sendButton.setText("OK");
        sendButton.setLayoutData(AtcfVizUtil.buttonGridData());

        sendButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Thread thread = new Thread(() -> {
                    try {
                        Object message = processStorm(storm, priority);
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

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Create the storm priority list info.
     *
     * @param parent
     */
    private void createStormInfoComp(Composite parent) {
        Composite stormInfoComp = new Composite(parent, SWT.BORDER);
        GridLayout stormInfoGL = new GridLayout(stormParameters.length, false);
        stormInfoGL.marginWidth = 5;
        stormInfoGL.marginHeight = 15;
        stormInfoGL.horizontalSpacing = 30;
        stormInfoGL.verticalSpacing = 5;
        stormInfoComp.setLayout(stormInfoGL);
        stormInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        for (String param : stormParameters) {
            Label infoLbl = new Label(stormInfoComp, SWT.NONE);
            infoLbl.setText(param);
            GridData infoLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false);
            infoLbl.setLayoutData(infoLblData);
        }

        stormInfoText = new Text(stormInfoComp,
                SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
        stormInfoText.setEditable(false);
        GridData stormInfoGD = new GridData(400, 180);
        stormInfoGD.horizontalAlignment = SWT.FILL;
        stormInfoGD.verticalAlignment = SWT.FILL;
        stormInfoGD.horizontalSpan = stormParameters.length + 10;
        stormInfoText.setLayoutData(stormInfoGD);

        stormInfoText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        stormInfoText.append("\tActive storms for ALL.\n\n");

        // Use only active storms
        List<Storm> stormRecs = AtcfDataUtil.getActiveStorms();
        Storm.sortStormList(stormRecs);
        Storm rec;
        for (Storm stormRec : stormRecs) {
            rec = stormRec;
            updateStormInfoList(rec);
        }
    }

    /**
     * Update the information in the information list
     *
     * @param rec
     */
    private void updateStormInfoList(Storm rec) {

        if (stormInfoText == null || stormInfoText.isDisposed()) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%9s  ", rec.getStormName()));
        sb.append(String.format("%6s", rec.getRegion()));
        sb.append(String.format("%8s", rec.getCycloneNum()));
        sb.append(String.format("%7s", rec.getSubRegion()));

        sb.append(String.format("%9s", rec.getYear()));
        sb.append(String.format("%6s", rec.getTcHLevel()));
        SimpleDateFormat datef = new SimpleDateFormat("yyyyMMddhh");
        sb.append(
                (rec.getStartDTG() == null ? String.format("%15s", MISSING_DTG)
                        : String.format("%15s",
                                datef.format(rec.getStartDTG().getTime()))));
        sb.append(String.format("%12s", MISSING_DTG));
        sb.append(String.format(" %10s", rec.getStormState()));
        sb.append(rec.getWtNum() == MISSING_INT_DD ? String.format("%5s", "-")
                : String.format("%5s", rec.getWtNum()));
        sb.append(
                rec.getPriority() == MISSING_INT_DD ? String.format("%8s", "-")
                        : String.format("%8s", rec.getPriority()));

        stormInfoText.append(sb.toString() + "   \n");
    }

    /**
     * Initiate processes to send data to WCOSS
     *
     * @param storm
     * @param priority
     * @return message of success or problems
     */
    private Object processStorm(Storm storm, String priority) {

        Object retval = null;
        Map<String, List<BDeckRecord>> retrievedBDeckRecords = AtcfDataUtil
                .getBDeckRecords(storm, false);

        try {
            String dtg = Collections.max(retrievedBDeckRecords.keySet());
            /*
             * Send request to edex to run script to push files to LDM Server
             * for subsequent processing on WCOSS
             */
            ModelPriorityRequest modPriorityReq = new ModelPriorityRequest(
                    storm, dtg, priority);

            retval = ThriftClient.sendRequest(modPriorityReq);
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