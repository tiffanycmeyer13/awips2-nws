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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWAConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.IfrLifrConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the IFR/LIFR composite.
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
public class IfrLifrComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(IfrLifrComp.class);

    /** developing check button */
    private Button dvlpgChk;

    /** coverage combo */
    private Combo coverageCbo;

    /** flight combo */
    private Combo flightCbo;

    /** CIG from combo */
    private Combo cigFromCbo;

    /** COG to combo */
    private Combo cigToCbo;

    /** visibility from combo */
    private Combo vsbyFromCbo;

    /** visibility to combo */
    private Combo vsbyToCbo;

    /** BR check button */
    private Button brChk;

    /** FG check button */
    private Button fgChk;

    /** HZ check button */
    private Button hzChk;

    /** DZ check button */
    private Button dzChk;

    /** RA check button */
    private Button raChk;

    /** SN check button */
    private Button snChk;

    /** FU check button */
    private Button fuChk;

    /** DU check button */
    private Button duChk;

    /** SS check button */
    private Button ssChk;

    /** no condition radio button */
    private Button noCondRdo;

    /** conditions continue beyond radio button */
    private Button contgRdo;

    /** conditions improve by radio button */
    private Button imprRdo;

    /** aircraft check button */
    private Button aircraftChk;

    /** additional information check button */
    private Button addnlInfoChk;

    /** additional information text field */
    private Text addnlInfoTxt;

    /** no update check button */
    private Button noUpdateChk;

    /** coverage items */
    private static final String CoverageItems[] = { "", "OCNL", "SCT",
            "WDSPRD" };

    /** flight items */
    private static final String flightItems[] = { "IFR", "IFR/PTCHY LIFR",
            "IFR/LIFR", "LIFR" };

    /** cig from items */
    private static final String cigFromItems[] = { "AOB ", "000", "002", "003",
            "004", "005" };

    /** cig to items */
    private static final String cigToItems[] = { "002", "003", "004", "005",
            "010" };

    /** visby from items */
    private static final String vsbyFromItems[] = { "AOB ", "LOCLY AOB ", "1/4",
            "1/2", "3/4", "1" };

    /** visby to items */
    private static final String vsbyToItems[] = { "1/4SM", "1/2SM", "3/4SM",
            "1SM", "1 1/2SM", "3SM" };

    /**
     * constructor
     * 
     * @param parent
     */
    public IfrLifrComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.IFRLIFR;
        setLayout(new GridLayout(1, false));

        createTimeComp();

        // developing
        Composite dvlpgComp = new Composite(this, SWT.NONE);
        dvlpgComp.setLayout(new GridLayout(3, false));
        dvlpgChk = new Button(dvlpgComp, SWT.CHECK);
        dvlpgChk.setText("DVLPG");

        Label coverageLbl = new Label(dvlpgComp, SWT.NONE);
        coverageLbl.setText("Coverage:");
        GridData coverageGd = new GridData();
        coverageGd.horizontalIndent = HORIZONTAL_INDENT;
        coverageLbl.setLayoutData(coverageGd);

        coverageCbo = new Combo(dvlpgComp, SWT.READ_ONLY);
        coverageCbo.setItems(CoverageItems);
        coverageCbo.select(0);

        // Flight rules
        Composite flightComp = new Composite(this, SWT.NONE);
        flightComp.setLayout(new GridLayout(6, false));
        Label flightLbl = new Label(flightComp, SWT.NONE);
        flightLbl.setText("Flight Rule:");

        flightCbo = new Combo(flightComp, SWT.READ_ONLY);
        flightCbo.setItems(flightItems);
        flightCbo.select(0);

        // CIG
        Label cigLbl = new Label(flightComp, SWT.NONE);
        cigLbl.setText("CIG:");
        GridData cigGd = new GridData();
        cigGd.horizontalIndent = HORIZONTAL_INDENT;
        cigLbl.setLayoutData(cigGd);

        cigFromCbo = new Combo(flightComp, SWT.READ_ONLY);
        cigFromCbo.setItems(cigFromItems);
        cigFromCbo.select(0);

        Label cigSlashLbl = new Label(flightComp, SWT.NONE);
        cigSlashLbl.setText("/");

        cigToCbo = new Combo(flightComp, SWT.READ_ONLY);
        cigToCbo.setItems(cigToItems);
        cigToCbo.select(0);

        // VSBY
        Composite vsbyComp = new Composite(this, SWT.NONE);
        vsbyComp.setLayout(new GridLayout(4, false));
        Label vsbyLbl = new Label(vsbyComp, SWT.NONE);
        vsbyLbl.setText("VSBY:");

        vsbyFromCbo = new Combo(vsbyComp, SWT.READ_ONLY);
        vsbyFromCbo.setItems(vsbyFromItems);
        vsbyFromCbo.select(0);

        Label vsbySlashLbl = new Label(vsbyComp, SWT.NONE);
        vsbySlashLbl.setText("/");

        vsbyToCbo = new Combo(vsbyComp, SWT.READ_ONLY);
        vsbyToCbo.setItems(vsbyToItems);
        vsbyToCbo.select(0);

        // check button group
        Composite checkBtnComp = new Composite(this, SWT.NONE);
        checkBtnComp.setLayout(new GridLayout(9, false));

        brChk = new Button(checkBtnComp, SWT.CHECK);
        brChk.setText("BR");

        fgChk = new Button(checkBtnComp, SWT.CHECK);
        fgChk.setText("FG");

        hzChk = new Button(checkBtnComp, SWT.CHECK);
        hzChk.setText("HZ");

        dzChk = new Button(checkBtnComp, SWT.CHECK);
        dzChk.setText("DZ");

        raChk = new Button(checkBtnComp, SWT.CHECK);
        raChk.setText("RA");

        snChk = new Button(checkBtnComp, SWT.CHECK);
        snChk.setText("SN");

        fuChk = new Button(checkBtnComp, SWT.CHECK);
        fuChk.setText("FU");

        duChk = new Button(checkBtnComp, SWT.CHECK);
        duChk.setText("DU");

        ssChk = new Button(checkBtnComp, SWT.CHECK);
        ssChk.setText("SS");

        // condition buttons
        Composite condComp = new Composite(this, SWT.NONE);
        condComp.setLayout(new GridLayout(3, false));

        noCondRdo = new Button(condComp, SWT.RADIO);
        noCondRdo.setText("No Conds Remark");
        noCondRdo.setSelection(true);

        contgRdo = new Button(condComp, SWT.RADIO);
        contgRdo.setText("Conds Contg Byd");

        imprRdo = new Button(condComp, SWT.RADIO);
        imprRdo.setText("Conds Impr By");

        // report composite
        Composite reportComp = new Composite(this, SWT.NONE);
        reportComp.setLayout(new GridLayout(4, false));

        aircraftChk = new Button(reportComp, SWT.CHECK);
        aircraftChk.setText("Rprtd by Aircraft");

        addnlInfoChk = new Button(reportComp, SWT.CHECK);
        addnlInfoChk.setText("Addnl Info to AIRMET SIERRA");
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
     * Create product text for IFR/LIFR
     * 
     * @param wmoId
     * @param header
     * @param fromline
     * @param body
     * @param cwsuId
     * @param productId
     * @param isCor
     * @return
     */
    @Override
    public String createText(String wmoId, String header, String fromline,
            String body, String cwsuId, String productId, boolean isCor,
            boolean isOperational, String type, double width, String stateIDs) {
        if (!(brChk.getSelection() || fgChk.getSelection()
                || hzChk.getSelection() || dzChk.getSelection()
                || raChk.getSelection() || snChk.getSelection()
                || fuChk.getSelection() || duChk.getSelection()
                || ssChk.getSelection())) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK);
            messageBox.setText("Select Visibility Options");
            messageBox.setMessage(
                    "Please select one or more of the visibility options:"
                            + "\nBR, FG, HZ, DZ, RA, SN, FU, DU, SS");
            messageBox.open();
            return "";
        }

        String endDateTime = getEndTime();

        // Evaluate obstructions to visibility and insert slashes if there is
        // more than one used.
        // Mixed-case variables like "wxFU" will be plotted in the CWA while the
        // lower case variables are
        // assigned by the GUI and are used for testing. When I tried adding the
        // slash to the lower
        // case variables, it would turn off the "selected" indicator in the
        // GUI.
        String wxSS = "";
        String wxDU = "";
        String wxFU = "";
        String wxSN = "";
        String wxRA = "";
        String wxDZ = "";
        String wxHZ = "";
        String wxFG = "";
        String wxBR = "";
        boolean obst = false;

        if (ssChk.getSelection()) {
            obst = true;
            wxSS = "SS";
        }
        if (duChk.getSelection() && obst) {
            wxDU = "DU/";
        } else if (duChk.getSelection()) {
            obst = true;
            wxDU = "DU";
        }
        if (fuChk.getSelection() && obst) {
            wxFU = "FU/";
        } else if (fuChk.getSelection()) {
            obst = true;
            wxFU = "FU";
        }

        if (snChk.getSelection() && obst) {
            wxSN = "SN/";
        } else if (snChk.getSelection()) {
            obst = true;
            wxSN = "SN";
        }
        if (raChk.getSelection() && obst) {
            wxRA = "RA/";
        } else if (raChk.getSelection()) {
            obst = true;
            wxRA = "RA";
        }
        if (dzChk.getSelection() && obst) {
            wxDZ = "DZ/";
        } else if (dzChk.getSelection()) {
            obst = true;
            wxDZ = "DZ";
        }
        if (hzChk.getSelection() && obst) {
            wxHZ = "HZ/";
        } else if (hzChk.getSelection()) {
            obst = true;
            wxHZ = "HZ";
        }
        if (fgChk.getSelection() && obst) {
            wxFG = "FG/";
        } else if (fgChk.getSelection()) {
            obst = true;
            wxFG = "FG";
        }
        if (brChk.getSelection() && obst) {
            wxBR = "BR/";
        } else if (brChk.getSelection()) {
            wxBR = "BR";
        }
        // ****** End of obstruction formatting *****
        // *** Add a dash after low CIG and VSBY values if they are not "AOB "
        // or "LOCLY AOB "
        String cigF = cigFromCbo.getText();
        if (!cigF.equals("AOB ") && !cigF.equals("LOCLY AOB ")) {
            cigF += "-";
        }

        String Lvsby = vsbyFromCbo.getText();
        if (!Lvsby.equals("AOB ") && !Lvsby.equals("LOCLY AOB ")) {
            Lvsby += "-";
        }
        // get next series ID
        CWAProduct cwaProduct = new CWAProduct(productId, cwsuId, isOperational,
                weatherType);
        int seriesId = cwaProduct.getNextSeriesId(isCor,
                cwaConfigs.getLocalTimeZone());

        StringBuilder output = new StringBuilder();
        output.append(getHeaderLines(wmoId, header, isCor));
        output.append(getValidLine(cwsuId, endDateTime, seriesId));
        output.append(fromline).append("\n");
        if (dvlpgChk.getSelection()) {
            output.append("DVLPG ");
        }
        output.append(body).append(" ");
        output.append(coverageCbo.getText()).append(" ")
                .append(flightCbo.getText()).append(" CONDS. ");
        output.append("CIGS ").append(cigF).append(cigToCbo.getText())
                .append(". ");
        output.append("VIS ").append(Lvsby).append(vsbyToCbo.getText())
                .append(" ");
        output.append(wxBR).append(wxFG).append(wxHZ).append(wxDZ).append(wxRA)
                .append(wxSN).append(wxFU).append(wxDU).append(wxSS)
                .append(". ");
        if (!noCondRdo.getSelection()) {
            if (contgRdo.getSelection()) {
                output.append("CONDS CONTG BYD ");
            } else {
                output.append("CONDS IMPR BY ");
            }
            output.append(endDateTime).append("Z.");
        }

        if (aircraftChk.getSelection()) {
            output.append(" RPRTD BY AIRCRAFT.");
        }
        if (this.addnlInfoChk.getSelection()) {
            output.append(" THIS IS ADDN INFO TO CNVTV SIGMET ");
            output.append(addnlInfoTxt.getText().trim());
        }

        if (noUpdateChk.getSelection()) {
            output.append(" NO UPDT AFT ").append(endDateTime).append("Z.");
        }
        if (stateIDs != null && !stateIDs.isEmpty()) {
            output.append(" ").append(stateIDs);
        }

        output.append(FINAL_LINE);

        return output.toString();
    }

    public AbstractCWAConfig getConfig() {
        IfrLifrConfig config = new IfrLifrConfig();
        config.setDvlpgChk(dvlpgChk.getSelection());
        config.setCoverage(
                coverageCbo.getItem(coverageCbo.getSelectionIndex()));
        config.setFlight(flightCbo.getItem(flightCbo.getSelectionIndex()));
        config.setCigFrom(cigFromCbo.getItem(cigFromCbo.getSelectionIndex()));
        config.setCigTo(cigToCbo.getItem(cigToCbo.getSelectionIndex()));
        config.setVsbyFrom(
                vsbyFromCbo.getItem(vsbyFromCbo.getSelectionIndex()));
        config.setVsbyTo(vsbyToCbo.getItem(vsbyToCbo.getSelectionIndex()));
        config.setBrChk(brChk.getSelection());
        config.setFgChk(fgChk.getSelection());
        config.setHzChk(hzChk.getSelection());
        config.setDzChk(dzChk.getSelection());
        config.setRaChk(raChk.getSelection());
        config.setSnChk(snChk.getSelection());
        config.setFuChk(fuChk.getSelection());
        config.setDuChk(duChk.getSelection());
        config.setSsChk(ssChk.getSelection());
        config.setNoCond(noCondRdo.getSelection());
        config.setContg(contgRdo.getSelection());
        config.setImpr(imprRdo.getSelection());
        config.setAircraftChk(aircraftChk.getSelection());

        addnlInfoChk.setSelection(config.getAddnlInfo() != null
                && !config.getAddnlInfo().isEmpty());
        if (addnlInfoChk.getSelection()) {
            config.setAddnlInfo(addnlInfoTxt.getText());
        } else {
            config.setAddnlInfo("");
        }

        config.setNoUpdateChk(noUpdateChk.getSelection());
        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWAConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse IfrLifr time.");
        }
        if (!(config instanceof IfrLifrConfig)) {
            return;
        }
        IfrLifrConfig ilc = (IfrLifrConfig) config;
        dvlpgChk.setSelection(ilc.isDvlpgChk());

        int index = coverageCbo.indexOf(ilc.getCoverage());
        if (index >= 0) {
            coverageCbo.select(index);
        } else {
            coverageCbo.select(0);
            logger.warn("Invalid coverage setting: " + ilc.getCoverage());
        }

        index = flightCbo.indexOf(ilc.getFlight());
        if (index >= 0) {
            flightCbo.select(index);
        } else {
            flightCbo.select(0);
            logger.warn("Invalid flight setting: " + ilc.getFlight());
        }

        index = cigFromCbo.indexOf(ilc.getCigFrom());
        if (index >= 0) {
            cigFromCbo.select(index);
        } else {
            cigFromCbo.select(0);
            logger.warn("Invalid cig to setting: " + ilc.getCigFrom());
        }

        index = cigToCbo.indexOf(ilc.getCigTo());
        if (index >= 0) {
            cigToCbo.select(index);
        } else {
            cigToCbo.select(0);
            logger.warn("Invalid cig to setting: " + ilc.getCigTo());
        }

        index = vsbyFromCbo.indexOf(ilc.getVsbyFrom());
        if (index >= 0) {
            vsbyFromCbo.select(index);
        } else {
            vsbyFromCbo.select(0);
            logger.warn(
                    "Invalid visibility from setting: " + ilc.getVsbyFrom());
        }

        index = vsbyToCbo.indexOf(ilc.getVsbyTo());
        if (index >= 0) {
            vsbyToCbo.select(index);
        } else {
            vsbyToCbo.select(0);
            logger.warn("Invalid visibility to setting: " + ilc.getVsbyTo());
        }

        brChk.setSelection(ilc.isBrChk());
        fgChk.setSelection(ilc.isFgChk());
        hzChk.setSelection(ilc.isHzChk());
        dzChk.setSelection(ilc.isDzChk());
        raChk.setSelection(ilc.isRaChk());
        snChk.setSelection(ilc.isSnChk());
        fuChk.setSelection(ilc.isFuChk());
        duChk.setSelection(ilc.isDuChk());
        ssChk.setSelection(ilc.isSsChk());

        noCondRdo.setSelection(ilc.isNoCond());
        contgRdo.setSelection(ilc.isContg());
        imprRdo.setSelection(ilc.isImpr());

        aircraftChk.setSelection(ilc.isAircraftChk());

        if (ilc.getAddnlInfo() == null || ilc.getAddnlInfo().isEmpty()) {
            addnlInfoChk.setSelection(false);
            addnlInfoTxt.setText("");
        } else {
            addnlInfoChk.setSelection(true);
            addnlInfoTxt.setText(ilc.getAddnlInfo());
        }

        noUpdateChk.setSelection(ilc.isNoUpdateChk());

    }
}
