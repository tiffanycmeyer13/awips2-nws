/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ListComputeDataRequest;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "List Compute Data" under "Aids"=>"Create Obj Aid Forecasts"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Jun 15, 2020 79543      mporricelli  Initial creation
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */

public class ListComputeDataDialog extends OcpCaveSWTDialog {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ListComputeDataDialog.class);

    private static final String PRT_CARQ = "_prtcarq.txt";

    private Storm storm;

    private StyledText carqInfoText;

    /**
     * Constructor
     * 
     * @param parent
     */
    public ListComputeDataDialog(Shell parent, Storm storm) {

        super(parent);
        this.storm = storm;

    }

    /**
     * Initialize the dialog components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {

        createDataComp(shell);
        createMenuBar(shell);
        createCarqInfoComp(shell);

    }

    /**
     * Create the layout.
     * 
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoLayout = new GridLayout(1, false);
        mainComp.setLayout(dataInfoLayout);
        mainComp.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true));

    }

    /**
     * @param shell
     */
    private void createMenuBar(Shell shell) {
        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        // TODO Add file submenu and functions, e.g. email the product
        fileMenuItem.setText("File");

        // Create the File menu
        Menu fileMenu = new Menu(menuBar);
        fileMenuItem.setMenu(fileMenu);

        shell.setMenuBar(menuBar);
    }

    /**
     * Create the com file display layout.
     *
     * @param parent
     */
    private void createCarqInfoComp(Composite parent) {
        Composite carqInfoComp = new Composite(parent, SWT.NONE);
        GridLayout carqInfoGL = new GridLayout(1, false);
        // List to show current active storm data.

        carqInfoComp.setLayout(carqInfoGL);
        carqInfoComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        carqInfoText = new StyledText(carqInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData carqInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        carqInfoGD.widthHint = 600;
        carqInfoGD.minimumHeight = 400;

        carqInfoText.setLayoutData(carqInfoGD);
        carqInfoText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        updateData(storm);

    }

    /**
     * Create output file and display contents
     * 
     * @param storm
     */
    public void updateData(Storm storm) {

        String fileContents = null;

        setText("ATCF - " + storm.getStormId().toLowerCase() + PRT_CARQ);

        // Parse com file and retrieve formatted data
        fileContents = createPrtCarqFile(storm);

        if (fileContents != null && !fileContents.isEmpty()) {
            try {
                carqInfoText.setText(fileContents);
                carqInfoText.setSelection(0);
            } catch (IllegalArgumentException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "List Compute: No data to display in textbox. ", e);
            }
        } else {
            statusHandler.handle(Priority.PROBLEM,
                    "List Compute: No data retrieved for display");
            carqInfoText.setText("***Warning: No compute data found for storm "
                    + storm.getStormId() + "***");
        }
    }

    /**
     * Retrieve text and create ..prtcarq.txt file
     *
     * @param storm
     * @return text to be displayed
     */
    private String createPrtCarqFile(Storm storm) {

        String carqText = null;
        try {

            ListComputeDataRequest listCompReq = new ListComputeDataRequest(
                    storm);

            carqText = (String) ThriftClient.sendRequest(listCompReq);
            if (carqText.isEmpty()) {
                statusHandler.handle(Priority.SIGNIFICANT, "Failed to create "
                        + storm.getStormId().toLowerCase() + PRT_CARQ);

            }
        } catch (Exception e) {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "Exception while creating " + storm.getStormId() + PRT_CARQ,
                    e);
        }

        return carqText;
    }

}