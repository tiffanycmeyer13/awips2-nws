/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * This class displays the CSIG dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CSIGDlg extends CaveSWTDialog {
    /** product text */
    private Text productTxt;

    /** product ID */
    private String productId;

    /** is operational */
    private boolean isOperational;

    /**
     * Constructor
     * 
     * @param parent
     *            the parent shell
     * @param productId
     *            product ID
     * @param isOperational
     *            is this in operational mode or practice mode
     */
    public CSIGDlg(Shell parent, String productId, boolean isOperational) {
        super(parent, SWT.DIALOG_TRIM | CAVE.DO_NOT_BLOCK | SWT.PRIMARY_MODAL);
        setText(productId);
        this.productId = productId;
        this.isOperational = isOperational;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout mainLayout = new GridLayout(1, false);
        shell.setLayout(mainLayout);

        productTxt = new Text(shell,
                SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.heightHint = productTxt.getLineHeight() * 10;
        GC gc = new GC(productTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 90);
        productTxt.setLayoutData(gd);
        productTxt.setEditable(false);
        gc.dispose();

        Button exitBtn = new Button(shell, SWT.PUSH | SWT.CENTER);
        exitBtn.setText("Exit");
        GridData csigGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        exitBtn.setLayoutData(csigGd);

        exitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        displayProduct();
    }

    /**
     * display product
     */
    private void displayProduct() {
        CWAProduct cwaProduct = new CWAProduct(productId, "", isOperational,
                null);
        productTxt.setText(cwaProduct.getProductTxt());
    }
}
