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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CopyStormRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * Dialog to copy a storm.
 *
 * Note: Any storm in the ATCF DB could be copied as a new storm with different
 * storm name and cyclone number within the same year and basin. The new storm
 * will be added into ATCF DB, not the master storm.table and and deck files via
 * this command.
 *
 * <pre>
 * Current implementation: 
 *
 * 1. Any storm in the ATCF DB could be copied.
 * 2. The new storm should have different storm name and cyclone number from the 
 *    original storm, within the same year and basin.
 * 3. The new storm is added into ATCF DB only, not the master storm.table and 
 *    deck files.
 *
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
public class CopyStormDialog extends CaveSWTDialog {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CopyStormDialog.class);

    /**
     * Storm
     */
    private Storm storm;

    /**
     * Storm name
     */
    private String stormName;

    private Text nameTxt;

    /**
     * Cyclone number
     */
    private int cycloneNum;

    private CCombo cylNumCmb;

    /**
     * Constructor
     * 
     * @param parent
     */
    public CopyStormDialog(Shell parent, Storm storm) {
        super(parent);
        this.storm = storm;
        this.stormName = storm.getStormName();
        this.cycloneNum = storm.getCycloneNum();

        setText("Copy a Storm");
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

        Composite stormNameComp = new Composite(stormComp, SWT.NONE);
        GridLayout stormNameCompGL = new GridLayout(1, false);
        stormNameCompGL.marginWidth = 15;
        stormNameComp.setLayout(stormNameCompGL);
        stormNameComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label newStormNameLbl = new Label(stormNameComp, SWT.NONE);
        newStormNameLbl.setText("Enter new storm name");
        newStormNameLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        nameTxt = new Text(stormNameComp, SWT.BORDER);
        nameTxt.setText(stormName);
        nameTxt.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Composite cycleNumComp = new Composite(stormComp, SWT.NONE);
        GridLayout cycleNumCompGL = new GridLayout(1, false);
        cycleNumCompGL.marginWidth = 15;
        cycleNumCompGL.marginBottom = 40;
        cycleNumComp.setLayout(cycleNumCompGL);
        cycleNumComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label cylNumLbl = new Label(cycleNumComp, SWT.NONE);
        cylNumLbl.setText("Select new storm number");
        cylNumLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        cylNumCmb = new CCombo(cycleNumComp, SWT.BORDER);
        cylNumCmb.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        String[] cylNumbers = AtcfVizUtil.getEntries(1, 99, 1, "%02d");
        cylNumCmb.setItems(cylNumbers);
        cylNumCmb.setEditable(false);

        int index = cylNumCmb.indexOf(String.format("%02d", cycloneNum));
        if (index < 0) {
            index = 0;
        }

        cylNumCmb.select(index);
        cylNumCmb.setText(cylNumbers[index]);
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
                if (copyStorm(storm, nameTxt.getText(), cylNumCmb.getText())) {
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
     * Copy a storm
     * 
     * @param storm Storm
     * 
     * @param newName New storm name.
     * 
     * @param newNum New cyclone number.
     */
    private boolean copyStorm(Storm storm, String nameIn, String numStr) {

        boolean valid = false;
        String validName = AtcfDataUtil.getValidStormName(shell, storm, nameIn,
                false);

        if (validName != null) {
            int validNum = AtcfDataUtil.getValidCycloneNumber(shell, storm,
                    numStr, false);
            if (validNum > 0) {

                valid = true;

                // Copy the storm.
                Storm newStorm = storm.copy();
                newStorm.setStormName(validName);
                newStorm.setCycloneNum(validNum);

                CopyStormRequest copyRequest = new CopyStormRequest(storm,
                        newStorm);
                try {
                    ThriftClient.sendRequest(copyRequest);

                    // Add to storm list.
                    AtcfDataUtil.updateStormList(newStorm, false);
                } catch (VizException e) {
                    logger.warn("CopyStormDialog - Failed to copy storm "
                            + storm.getStormName() + "," + storm.getStormId(),
                            e);
                }
            }
        }

        return valid;
    }

}