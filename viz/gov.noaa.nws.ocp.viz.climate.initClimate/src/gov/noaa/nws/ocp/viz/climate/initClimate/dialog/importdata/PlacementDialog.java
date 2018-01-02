/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.importdata;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.TextIntListener;

/**
 * Display the "Column Placement" dialog
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ---------- --------------------------
 * 05/09/2016   18469       wkwock     Initial creation.
 * 10/27/2016   20635       wkwock     Clean up
 * 15 MAY 2017  33104       amoore     Class rename.
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class PlacementDialog extends ClimateCaveDialog {

    /** Column text */
    private Text columnTxt;

    /**
     * constructor
     * 
     * @param shell
     */
    protected PlacementDialog(Shell shell) {
        super(shell);
        String hostname = System.getenv("HOSTNAME");
        setText("Column Placement (on " + hostname + ")");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        mainLayout.verticalSpacing = 5;
        shell.setLayout(mainLayout);

        Composite topComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 10;
        topComp.setLayout(gl);

        Label columnLbl = new Label(topComp, SWT.CENTER);
        columnLbl.setText("Enter\nColumn Number");

        TextIntListener columnListener = new TextIntListener(0, 28, 0);

        columnTxt = new Text(topComp, SWT.CENTER | SWT.BORDER);
        columnTxt.addListener(SWT.Verify, columnListener);
        columnTxt.addListener(SWT.FocusOut, columnListener);

        Label separateLbl = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomComp = new Composite(shell, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginWidth = 10;
        bottomComp.setLayout(gl);

        Button okBtn = new Button(bottomComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                getColumnNum(true);
            }
        });

        Button cancelBttn = new Button(bottomComp, SWT.PUSH);
        cancelBttn.setText("Cancel");
        cancelBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                getColumnNum(false);
            }
        });
    }

    /**
     * get the column number
     * 
     * @param isOk
     */
    private void getColumnNum(boolean isOk) {
        int returnVal = -1;
        if (isOk) {
            returnVal = Integer.parseInt(columnTxt.getText());
        }

        setReturnValue(returnVal);
        close();
    }
}
