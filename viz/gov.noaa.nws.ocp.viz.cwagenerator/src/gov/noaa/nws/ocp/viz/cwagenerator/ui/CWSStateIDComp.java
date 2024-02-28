/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWSNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * 
 * Class for the CWS/MIS composite with state IDs
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 1, 2020  7767       wkwock      Initial creation
 * Sep 10, 2021 28802      wkwock      Use new configuration format
 * Apr 05, 2022 22989      wkwock      Add issuance# reset
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWSStateIDComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWSStateIDComp.class);

    /** type combo */
    private Combo typeCbo;

    /** intensity combo */
    private Combo intstCbo;

    /** wind direction combo */
    private Combo dirCbo;

    /** wind speed combo */
    private Combo spdCbo;

    /** Tops from combo */
    private Combo topsFromCbo;

    /** Tops to Combo */
    private Combo topsToCbo;

    /** type items */
    private static final String[] typeItems = { "SHRA/TSRA", "TSRA", "TS" };

    /** intensity items */
    private static final String[] intstItems = { "---", "MOD", "MOD TO HVY",
            "HVY", "HVY TO EXTRM", "EXTRM" };

    private Spinner afterSpinner;

    /**
     * constructor
     * 
     * @param parent
     */
    public CWSStateIDComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.MIS;
        setLayout(new GridLayout(1, false));

        this.cwaConfigs = cwaConfigs;
        createTimeComp();

        // coverage
        Composite coverageComp = new Composite(this, SWT.NONE);
        coverageComp.setLayout(new GridLayout(4, false));
        coverageComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
        // Type
        Label typeLbl = new Label(coverageComp, SWT.NONE);
        typeLbl.setText("Type:");

        typeCbo = new Combo(coverageComp, SWT.READ_ONLY);
        typeCbo.setItems(typeItems);
        typeCbo.select(1);

        // intst
        Label intstLbl = new Label(coverageComp, SWT.NONE);
        intstLbl.setText("  Intst:");

        intstCbo = new Combo(coverageComp, SWT.READ_ONLY);
        intstCbo.setItems(intstItems);
        intstCbo.select(0);

        // wind direction and speed
        Composite windComp = new Composite(this, SWT.NONE);
        windComp.setLayout(new GridLayout(9, false));
        windComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        Label windLbl = new Label(windComp, SWT.NONE);
        windLbl.setText("Dir/Spd:");

        dirCbo = new Combo(windComp, SWT.READ_ONLY);
        dirCbo.add("---");
        for (int i = 10; i <= 360; i += 10) {
            dirCbo.add(String.format("%03d", i));
        }
        dirCbo.select(0);

        Label slashLbl = new Label(windComp, SWT.NONE);
        slashLbl.setText("/");

        spdCbo = new Combo(windComp, SWT.READ_ONLY);
        spdCbo.add(MOV_LTL);
        for (int i = 5; i <= 60; i += 5) {
            spdCbo.add(String.format("%03d", i));
        }
        spdCbo.select(0);

        // Tops
        Label topsLbl = new Label(windComp, SWT.NONE);
        topsLbl.setText("  Tops:");

        topsFromCbo = new Combo(windComp, SWT.READ_ONLY);
        topsFromCbo.add("---");
        for (int i = 180; i <= 550; i += 10) {
            topsFromCbo.add(String.format("%03d", i));
        }
        topsFromCbo.select(0);

        Label slash2Lbl = new Label(windComp, SWT.NONE);
        slash2Lbl.setText("/");

        topsToCbo = new Combo(windComp, SWT.READ_ONLY);
        for (int i = 250; i <= 600; i += 10) {
            topsToCbo.add(String.format("%03d", i));
        }
        topsToCbo.select(0);

        // report composite
        Composite reportComp = new Composite(this, SWT.NONE);
        reportComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        reportComp.setLayout(new GridLayout(3, false));

        Label contLbl = new Label(reportComp, SWT.NONE);
        contLbl.setText("Continue after");
        afterSpinner = new Spinner(reportComp, SWT.BORDER);
        afterSpinner.setMinimum(1);
        afterSpinner.setMaximum(24);
        afterSpinner.setSelection(1);
        Label zLbl = new Label(reportComp, SWT.NONE);
        zLbl.setText("Z");
    }

    @Override
    public String createText(String wmoId, String header, String fromline,
            String body, String cwsuId, String productId, boolean isCor,
            boolean isOperational, DrawingType type, double width,
            String stateIDs) {
        int topsTo = Integer.parseInt(topsToCbo.getText());
        int topsFrom = 0;
        if (topsFromCbo.getSelectionIndex() > 0) {
            topsFrom = Integer.parseInt(topsFromCbo.getText());
        }

        if (topsTo <= topsFrom) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK);
            messageBox.setText("TOPS Selection");
            messageBox.setMessage("The Tops values " + topsToCbo.getText() + "/"
                    + topsFromCbo.getText() + " are invalid."
                    + "\nPlease reselect the Tops values.");
            messageBox.open();
            return "";
        }

        String endDateTime = getEndTime();

        // get next series ID
        CWAProduct cwaProduct = new CWAProduct(productId, cwsuId, isOperational,
                WeatherType.MIS);
        int seriesId = cwaProduct.getNextSeriesId(isCor,
                cwaConfigs.getLocalTimeZone(), isResetIssuance());

        // Check for isolated cell over VOR. Length would be 3 if it
        // is true.
        if (fromline.length() == 3) {
            fromline = "OVR " + fromline;
        }

        StringBuilder output = new StringBuilder();
        output.append(getHeaderLines(wmoId, header, isCor));
        output.append(getValidLine(cwsuId, endDateTime, seriesId));
        output.append(fromline).append("\n");
        if (body.startsWith("A") || body.startsWith("I")) {
            String body2 = body.substring(4);

            if (body.startsWith("A")) {
                output.append(body).append(" ");
            }

            output.append(typeCbo.getText());

            if (body.startsWith("I")) {
                output.append(body2);
            }

            if (intstCbo.getSelectionIndex() == 0) {
                output.append(". ");
            } else {
                output.append(" WITH ").append(intstCbo.getText())
                        .append(" PCPN. ");
            }

            if (dirCbo.getText().equals("---")
                    || MOV_LTL.equals(spdCbo.getText())) {
                output.append(MOV_LTL).append(".\n");
            } else {
                String speed = spdCbo.getText().substring(1);
                output.append("MOV FROM ");
                output.append(dirCbo.getText()).append(speed);
                output.append("KT.\n");
            }

            output.append("TOPS");
            if (topsFromCbo.getSelectionIndex() == 0) {
                output.append(" TO FL");
                output.append(topsToCbo.getText());
            } else {
                output.append(" FL");
                output.append(topsFromCbo.getText()).append("-")
                        .append(topsToCbo.getText());
            }
            output.append(". ");

            output.append("CONT AFTER ").append(afterSpinner.getText())
                    .append("Z.  ");

            if (stateIDs != null && !stateIDs.isEmpty()) {
                output.append(stateIDs);
            }

            output.append(FINAL_LINE);
        }
        return output.toString();
    }

    @Override
    public AbstractCWANewConfig getConfig() {
        CWSNewConfig config = new CWSNewConfig();
        config.setType(typeCbo.getText());
        config.setIntst(intstCbo.getText());
        config.setDir(dirCbo.getText());
        config.setSpd(spdCbo.getText());
        config.setTopsFrom(topsFromCbo.getText());
        config.setTopsTo(topsToCbo.getText());
        config.setContAfter(afterSpinner.getSelection());
        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWANewConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse CWS product time.");
        }

        if (!(config instanceof CWSNewConfig)) {
            return;
        }

        CWSNewConfig cc = (CWSNewConfig) config;
        int index = typeCbo.indexOf(cc.getType());
        if (index >= 0) {
            typeCbo.select(index);
        } else {
            typeCbo.select(0);
            logger.warn("Invalid type setting: " + cc.getType());
        }

        index = intstCbo.indexOf(cc.getIntst());
        if (index >= 0) {
            intstCbo.select(index);
        } else {
            intstCbo.select(0);
            logger.warn("Invalid intensity setting: " + cc.getIntst());
        }

        index = dirCbo.indexOf(cc.getDir());
        if (index >= 0) {
            dirCbo.select(index);
        } else {
            dirCbo.select(0);
            logger.warn("Invalid direction setting: " + cc.getDir());
        }

        index = spdCbo.indexOf(cc.getSpd());
        if (index >= 0) {
            spdCbo.select(index);
        } else {
            spdCbo.select(0);
            logger.warn("Invalid speed setting: " + cc.getSpd());
        }

        index = topsToCbo.indexOf(cc.getTopsTo());
        if (index >= 0) {
            topsToCbo.select(index);
        } else {
            topsToCbo.select(0);
            logger.warn("Invalid tops to setting: " + cc.getTopsTo());
        }

        index = topsFromCbo.indexOf(cc.getTopsFrom());
        if (index >= 0) {
            topsFromCbo.select(index);
        } else {
            topsFromCbo.select(0);
            logger.warn("Invalid tops from setting: " + cc.getTopsFrom());
        }

        afterSpinner.setSelection(cc.getContAfter());
    }

}