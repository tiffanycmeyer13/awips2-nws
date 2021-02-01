/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWAConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWSConfig;

/**
 * Class for save VORs dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/15/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class SaveVorsDlg extends CaveSWTDialog {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SaveVorsDlg.class);

    /** suggested name for this configuration */
    private String suggestedName;

    private CWAProductConfig configXML = null;

    private AbstractCWAConfig config;

    private Text nameTxt;

    private List namesList;

    /** product configuration file name */
    private String fileName;

    /**
     * Constructor
     * 
     * @param display
     * @param productId
     */
    public SaveVorsDlg(Shell shell, String suggestedName,
            AbstractCWAConfig config) {
        super(shell, SWT.DIALOG_TRIM | CAVE.DO_NOT_BLOCK);
        this.suggestedName = suggestedName;
        this.config = config;
        fileName = CWAGeneratorUtil.CWA_PRODUCT_CONFIG_FILE;
        if (config instanceof CWSConfig) {
            fileName = CWAGeneratorUtil.CWS_PRODUCT_CONFIG_FILE;
        }
        try {
            configXML = CWAGeneratorUtil.readProductConfigurations(fileName);
        } catch (SerializationException | LocalizationException | IOException
                | JAXBException e1) {
            logger.error("Failed to read " + fileName, e1);
        }

        setText("Save Configuration");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout mainLayout = new GridLayout(1, false);
        shell.setLayout(mainLayout);
        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        shell.setLayoutData(gd);

        Label selectLbl = new Label(shell, SWT.NONE);
        selectLbl.setText("Select an exsting product configuration name:");

        namesList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.heightHint = namesList.getItemHeight() * 14;
        GC gc = new GC(namesList);
        int width = (int) (gc.getFontMetrics().getAverageCharacterWidth() * 40);
        gd.widthHint = width;
        gc.dispose();

        namesList.setLayoutData(gd);

        Composite enterComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginTop = 10;
        enterComp.setLayout(gl);

        Label enterLbl = new Label(enterComp, SWT.NONE);
        enterLbl.setText("Or enter a new product configuration name:");
        nameTxt = new Text(enterComp, SWT.BORDER | SWT.CENTER);
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gd.widthHint = width;
        nameTxt.setLayoutData(gd);

        namesList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                nameTxt.setText(
                        namesList.getItem(namesList.getSelectionIndex()));
            }
        });

        Composite bottomComp = new Composite(shell, SWT.NONE);
        bottomComp.setLayout(new GridLayout(2, true));
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        bottomComp.setLayoutData(gd);

        Button saveBttn = new Button(bottomComp, SWT.PUSH);
        saveBttn.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        saveBttn.setText("Save");
        saveBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveToXML();
            }
        });

        Button cancelBttn = new Button(bottomComp, SWT.PUSH);
        cancelBttn
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        cancelBttn.setText("Cancel");
        cancelBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    @Override
    public void opened() {
        if (configXML != null && configXML.getCwaProducts() != null) {
            for (AbstractCWAConfig productConfig : configXML.getCwaProducts()) {
                namesList.add(productConfig.getConfigName());
            }
        }
        nameTxt.setText(suggestedName);
        nameTxt.setFocus();
    }

    private void saveToXML() {
        String newName = nameTxt.getText().trim();
        if (newName.isEmpty()) {
            MessageBox messageBox = new MessageBox(shell, SWT.OK);
            messageBox.setText("Invalid Configuration Name");
            messageBox.setMessage("Please type in a proper name");
            messageBox.open();
            return;
        }
        // Go through the existing list, warn user if there's a match
        // one
        AbstractCWAConfig foundConfig = null;
        if (configXML != null && configXML.getCwaProducts() != null) {
            for (AbstractCWAConfig productConfig : configXML.getCwaProducts()) {
                if (newName.equals(productConfig.getConfigName())) {
                    foundConfig = productConfig;
                    break;
                }
            }
        } else {
            if (configXML == null) {
                configXML = new CWAProductConfig();
            }
            if (configXML.getCwaProducts() == null) {
                ArrayList<AbstractCWAConfig> configList = new ArrayList<>();
                configXML.setCwaElementList(configList);
            }
        }

        java.util.List<AbstractCWAConfig> configList = configXML
                .getCwaProducts();
        if (foundConfig != null) {
            MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO);
            messageBox.setText("Over write existing configuration");
            messageBox.setMessage(
                    "Over write existing configuration " + newName + "?");
            int result = messageBox.open();
            if (result != SWT.YES) {
                return;
            }

            configList.remove(foundConfig);
        }
        config.setConfigName(newName);
        // save to file
        configList.add(config);
        try {
            CWAGeneratorUtil.saveProductConfigurations(configXML, fileName);
        } catch (SerializationException | LocalizationException | IOException
                | JAXBException e) {
            logger.error("Failed to save product configuration", e);
        }

        close();
    }

}
