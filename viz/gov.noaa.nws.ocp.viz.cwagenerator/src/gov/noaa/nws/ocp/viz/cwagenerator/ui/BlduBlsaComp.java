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

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.BlduBlsaNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the Blowing dust/blowing sand composite.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 09/10/2021  28802    wkwock      Use new configuration format
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class BlduBlsaComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(BlduBlsaComp.class);

    /** Coverage combo */
    private Combo coverageCbo;

    /** type combo */
    private Combo typeCbo;

    /** wind direction combo */
    private Combo dirCbo;

    /** wind gust combo */
    private Combo gustCbo;

    /** visibility from combo */
    private Combo vsbyFromCbo;

    /** visibility to combo */
    private Combo vsbyToCbo;

    /** condition from combo */
    private Combo condFromCbo;

    /** condition to combo */
    private Combo condToCbo;

    /**
     * Constructor
     * 
     * @param parent
     */
    public BlduBlsaComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.BLDUBLSA;
        setLayout(new GridLayout(1, false));

        createTimeComp();

        Composite coverageComp = new Composite(this, SWT.NONE);
        coverageComp.setLayout(new GridLayout(5, false));
        Label coverageLbl = new Label(coverageComp, SWT.NONE);
        coverageLbl.setText("Coverage:");

        coverageCbo = new Combo(coverageComp, SWT.READ_ONLY);
        String[] coverageItems = { "", "OCNL", "SCT", "WDSPRD" };
        coverageCbo.setItems(coverageItems);
        coverageCbo.select(0);

        // type
        Label typeLbl = new Label(coverageComp, SWT.NONE);
        typeLbl.setText("Type:");
        GridData typeGd = new GridData();
        typeGd.horizontalIndent = HORIZONTAL_INDENT;
        typeLbl.setLayoutData(typeGd);

        typeCbo = new Combo(coverageComp, SWT.READ_ONLY);
        String[] typeItems = { "BLDU", "BLSA" };
        typeCbo.setItems(typeItems);
        typeCbo.select(0);

        Composite windComp = new Composite(this, SWT.NONE);
        windComp.setLayout(new GridLayout(4, false));
        Label windLbl = new Label(windComp, SWT.NONE);
        windLbl.setText("Wind Dir:");

        dirCbo = new Combo(windComp, SWT.READ_ONLY);
        for (int i = 10; i <= 360; i += 10) {
            dirCbo.add(String.format("%03d", i));
        }
        dirCbo.select(0);

        Label gustLbl = new Label(windComp, SWT.NONE);
        gustLbl.setText("Gusts:");
        GridData gustGd = new GridData();
        gustGd.horizontalIndent = HORIZONTAL_INDENT;
        gustLbl.setLayoutData(gustGd);

        gustCbo = new Combo(windComp, SWT.READ_ONLY);
        String[] gustItems = { "20-30 KTS", "30-40 KTS", "40-50 KTS",
                "50-60 KTS" };
        gustCbo.setItems(gustItems);
        gustCbo.select(0);

        // VSBY
        Composite vsbyComp = new Composite(this, SWT.NONE);
        vsbyComp.setLayout(new GridLayout(7, false));
        Label vsbyLbl = new Label(vsbyComp, SWT.NONE);
        vsbyLbl.setText("VSBY:");

        vsbyFromCbo = new Combo(vsbyComp, SWT.READ_ONLY);
        String[] vsbyFromItems = { "AOB", "LOCLY AOB", "1/4", "1/2", "3/4" };
        vsbyFromCbo.setItems(vsbyFromItems);
        vsbyFromCbo.select(0);

        Label slash2Lbl = new Label(vsbyComp, SWT.NONE);
        slash2Lbl.setText("/");

        vsbyToCbo = new Combo(vsbyComp, SWT.READ_ONLY);
        String[] vsbyToItems = { "1/2", "3/4", "1", "1 1/2", "3", "5" };
        vsbyToCbo.setItems(vsbyToItems);
        vsbyToCbo.select(0);

        Label condLbl = new Label(vsbyComp, SWT.NONE);
        condLbl.setText("Conditions:");
        GridData condGd = new GridData();
        condGd.horizontalIndent = HORIZONTAL_INDENT;
        condLbl.setLayoutData(condGd);

        condFromCbo = new Combo(vsbyComp, SWT.READ_ONLY);
        String[] condFromItems = { "", "SPRDG", "???" };
        condFromCbo.setItems(condFromItems);
        condFromCbo.select(0);

        condToCbo = new Combo(vsbyComp, SWT.READ_ONLY);
        String[] condToItems = { "NWD", "NEWD", "EWD", "SEWD", "SWD", "SWWD",
                "WWD", "NWWD" };
        condToCbo.setItems(condToItems);
        condToCbo.select(0);
    }

    /**
     * Create product text for BLDU/BLSA
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
            boolean isOperational, DrawingType type, double width,
            String stateIDs) {
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

        String lvsby = vsbyFromCbo.getText();
        if (!lvsby.equals("AOB ") && !lvsby.equals("LOCLY AOB ")) {
            lvsby += "-";
        }

        StringBuilder output = new StringBuilder();
        output.append(getHeaderLines(wmoId, header, isCor));
        output.append(getValidLine(cwsuId, endDateTime, seriesId));
        output.append(fromline).append("\n");
        if (body.startsWith("A") || body.startsWith("I")) {

            if (body.startsWith("A")) {
                output.append(body).append(" ");
            }

            output.append(typeCbo.getText());
            output.append(" WITH SFC WNDS MOV FROM ");
            output.append(dirCbo.getText());

            output.append(" GUSTS ").append(gustCbo.getText()).append("\n");
            output.append("VIS ").append(lvsby).append(vsbyToCbo.getText())
                    .append("SM\n");
            output.append("CONDS ").append(condFromCbo.getText()).append(" ")
                    .append(condToCbo.getText()).append(".");

            if (stateIDs != null && !stateIDs.isEmpty()) {
                output.append(" ").append(stateIDs);
            }

            output.append(FINAL_LINE);
        }
        return output.toString();
    }

    public AbstractCWANewConfig getConfig() {

        BlduBlsaNewConfig config = new BlduBlsaNewConfig();
        config.setCoverage(
                coverageCbo.getItem(coverageCbo.getSelectionIndex()));
        config.setType(typeCbo.getItem(typeCbo.getSelectionIndex()));
        config.setDir(dirCbo.getItem(dirCbo.getSelectionIndex()));
        config.setGust(gustCbo.getItem(gustCbo.getSelectionIndex()));
        config.setVsbyFrom(
                vsbyFromCbo.getItem(vsbyFromCbo.getSelectionIndex()));
        config.setVsbyTo(vsbyToCbo.getItem(vsbyToCbo.getSelectionIndex()));
        config.setCondFrom(
                condFromCbo.getItem(condFromCbo.getSelectionIndex()));
        config.setCondTo(condToCbo.getItem(condToCbo.getSelectionIndex()));

        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWANewConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse BlduBlsa time.");
        }

        if (!(config instanceof BlduBlsaNewConfig)) {
            return;
        }

        BlduBlsaNewConfig bbc = (BlduBlsaNewConfig) config;
        int index = coverageCbo.indexOf(bbc.getCoverage());
        if (index >= 0) {
            coverageCbo.select(index);
        } else {
            coverageCbo.select(0);
            logger.warn("Invalid coverage setting: " + bbc.getCoverage());
        }

        index = typeCbo.indexOf(bbc.getType());
        if (index >= 0) {
            typeCbo.select(index);
        } else {
            typeCbo.select(0);
            logger.warn("Invalid type setting: " + bbc.getType());
        }

        index = dirCbo.indexOf(bbc.getDir());
        if (index >= 0) {
            dirCbo.select(index);
        } else {
            dirCbo.select(0);
            logger.warn("Invalid direction setting: " + bbc.getDir());
        }

        index = gustCbo.indexOf(bbc.getGust());
        if (index >= 0) {
            gustCbo.select(index);
        } else {
            gustCbo.select(0);
            logger.warn("Invalid gust setting: " + bbc.getGust());
        }

        index = vsbyFromCbo.indexOf(bbc.getVsbyFrom());
        if (index >= 0) {
            vsbyFromCbo.select(index);
        } else {
            vsbyFromCbo.select(0);
            logger.warn(
                    "Invalid visibility from setting: " + bbc.getVsbyFrom());
        }

        index = vsbyToCbo.indexOf(bbc.getVsbyTo());
        if (index >= 0) {
            vsbyToCbo.select(index);
        } else {
            vsbyToCbo.select(0);
            logger.warn("Invalid visibility to setting: " + bbc.getVsbyTo());
        }

        index = condFromCbo.indexOf(bbc.getCondFrom());
        if (index >= 0) {
            condFromCbo.select(index);
        } else {
            condFromCbo.select(0);
            logger.warn("Invalid cond from setting: " + bbc.getCondFrom());
        }

        index = condToCbo.indexOf(bbc.getCondTo());
        if (index >= 0) {
            condToCbo.select(index);
        } else {
            condToCbo.select(0);
            logger.warn("Invalid cond to setting: " + bbc.getCondTo());
        }
    }
}
