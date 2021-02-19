/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWAConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CancelManualConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the cancel/manual composite.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 06/03/2020  75767    wkwock      Moved this class from PGEN to NWS
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class CancelManualComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CancelManualComp.class);

    /** cancel check button */
    private Button cancelChk;

    /** see check button */
    private Button seeChk;

    /** additional information text field */
    private Text addnlInfoTxt;

    /** no update check button */
    private Button noUpdateChk;

    /**
     * constructor
     * 
     * @param parent
     */
    public CancelManualComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.CANMAN;

        setLayout(new GridLayout(1, false));

        Label guideLbl = new Label(this, SWT.NONE);
        guideLbl.setText(
                "Click 'Create New Text' to pre-format a manual product.");

        createTimeComp();

        // report composite
        Composite reportComp = new Composite(this, SWT.NONE);
        reportComp.setLayout(new GridLayout(4, false));

        cancelChk = new Button(reportComp, SWT.CHECK);
        cancelChk.setText("Cancel ZHU CWA");

        seeChk = new Button(reportComp, SWT.CHECK);
        seeChk.setText("SEE CONVECTIVE SIGMET #");
        addnlInfoTxt = new Text(reportComp, SWT.BORDER);
        GridData gd = new GridData();
        GC gc = new GC(addnlInfoTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 10);
        addnlInfoTxt.setLayoutData(gd);
        gc.dispose();

        noUpdateChk = new Button(reportComp, SWT.CHECK);
        noUpdateChk.setText("No Updt Aft");
    }

    /**
     * Create product text for cancel/manual
     * 
     * @param wmoId
     * @param header
     * @param fromline
     * @param body
     * @param cwsuId
     * @param productId
     * @param isCor
     * @return product text
     */
    @Override
    public String createText(String wmoId, String header, String fromline,
            String body, String cwsuId, String productId, boolean isCor,
            boolean isOperational, String type, double width, String stateIDs) {
        String endDateTime = getEndTime();

        // get next series ID
        CWAProduct cwaProduct = new CWAProduct(productId, cwsuId, isOperational,
                weatherType);
        int seriesId = cwaProduct.getNextSeriesId(isCor,
                cwaConfigs.getLocalTimeZone());

        // Check for isolated cell over VOR. Length would be 3 if it
        // is true.
        if (fromline.length() == 3) {
            fromline = "OVR " + fromline;
        }

        String prevalid = "";
        if (cancelChk.getSelection()) {
            prevalid = String.valueOf(seriesId - 1);
        }

        StringBuilder output = new StringBuilder();
        output.append(getHeaderLines(wmoId, header, isCor));
        output.append(getValidLine(cwsuId, endDateTime, seriesId));

        if (this.cancelChk.getSelection()) {
            output.append("CANCEL ").append(cwsuId).append(" CWA ")
                    .append(prevalid).append(". ");
        } else {
            output.append(fromline);
        }

        if (this.seeChk.getSelection()) {
            output.append("\nSEE CONVECTIVE SIGMET ")
                    .append(addnlInfoTxt.getText());
        }
        if (noUpdateChk.getSelection()) {
            if (!this.seeChk.getSelection()) {
                // new line if no new line above
                output.append("\n");
            }
            output.append("NO UPDT AFT ").append(endDateTime).append("Z. ");
        }

        output.append(FINAL_LINE);
        return output.toString();
    }

    /**
     * update CWSU ID
     * 
     * @param cwsuId
     */
    public void updateCwsuId(String cwsuId) {
        cancelChk.setText("Cancel " + cwsuId + " CWA");
    }

    public AbstractCWAConfig getConfig() {
        CancelManualConfig config = new CancelManualConfig();
        config.setCancelChk(cancelChk.getSelection());
        config.setSeeChk(seeChk.getSelection());
        config.setAddnlInfo(addnlInfoTxt.getText());
        config.setNoUpdateChk(noUpdateChk.getSelection());

        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWAConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse cancel/manual product time.");
        }
        if (!(config instanceof CancelManualConfig)) {
            return;
        }

        CancelManualConfig cmc = (CancelManualConfig) config;
        cancelChk.setSelection(cmc.isCancelChk());
        seeChk.setSelection(cmc.isSeeChk());
        addnlInfoTxt.setText(cmc.getAddnlInfo());
        noUpdateChk.setSelection(cmc.isNoUpdateChk());
    }
}
