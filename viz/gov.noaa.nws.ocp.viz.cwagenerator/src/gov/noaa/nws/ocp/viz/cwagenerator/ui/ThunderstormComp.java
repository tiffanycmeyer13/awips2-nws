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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CondType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ThunderstormNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the thunderstorm composite.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 06/02/2020  75767    wkwock      Migrated from PGEN to NWS
 * 09/10/2021  28802    wkwock      Use new configuration format
 * 04/05/2022  22989    wkwock      Add issuance# reset
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class ThunderstormComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ThunderstormComp.class);

    /** developing check button */
    private Button dvlpgChk;

    /** embedded check button */
    private Button embdChk;

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

    /** estimated check button */
    private Button estChk;

    /** tornado check button */
    private Button tornadoChk;

    /** hail check button */
    private Button hailChk;

    /** gust check button */
    private Button gustChk;

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

    /** type items */
    private static final String[] typeItems = { "SHRA/TSRA", "TSRA", "TS" };

    /** intensity items */
    private static final String[] intstItems = { "---", "MOD", "MOD TO HVY",
            "HVY", "HVY TO EXTRM", "EXTRM" };

    /**
     * constructor
     * 
     * @param parent
     */
    public ThunderstormComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.THUNDERSTORM;
        setLayout(new GridLayout(1, false));

        createTimeComp();

        Composite line2Comp = new Composite(this, SWT.NONE);
        line2Comp.setLayout(new GridLayout(5, false));
        // DVLPG
        dvlpgChk = new Button(line2Comp, SWT.CHECK);
        dvlpgChk.setText("DVLPG");
        // EMBD
        embdChk = new Button(line2Comp, SWT.CHECK);
        embdChk.setText("EMBD");

        // coverage
        Composite coverageComp = new Composite(this, SWT.NONE);
        coverageComp.setLayout(new GridLayout(4, false));

        // Type
        Label typeLbl = new Label(coverageComp, SWT.NONE);
        typeLbl.setText("  Type:");

        typeCbo = new Combo(coverageComp, SWT.READ_ONLY);
        typeCbo.setItems(typeItems);
        typeCbo.select(1);
        if (cwaConfigs.getThunderstormType() != null) {
            int i = 0;
            for (String type : typeCbo.getItems()) {
                if (type.compareToIgnoreCase(
                        cwaConfigs.getThunderstormType()) == 0) {
                    typeCbo.select(i);
                    break;
                }
                i++;
            }
        }

        // intst
        Label intstLbl = new Label(coverageComp, SWT.NONE);
        intstLbl.setText("  Intst:");

        intstCbo = new Combo(coverageComp, SWT.READ_ONLY);
        intstCbo.setItems(intstItems);
        intstCbo.select(0);

        // wind direction and speed
        Composite windComp = new Composite(this, SWT.NONE);
        windComp.setLayout(new GridLayout(9, false));
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
        topsFromCbo.add("TO");
        topsFromCbo.add("ABV");
        for (int i = 10; i <= 550; i += 10) {
            topsFromCbo.add(String.format("%03d", i));
        }
        topsFromCbo.select(0);

        Label slash2Lbl = new Label(windComp, SWT.NONE);
        slash2Lbl.setText("/");

        topsToCbo = new Combo(windComp, SWT.READ_ONLY);
        for (int i = 10; i <= 600; i += 10) {
            topsToCbo.add(String.format("%03d", i));
        }
        topsToCbo.select(0);

        // est
        estChk = new Button(windComp, SWT.CHECK);
        estChk.setText("Est");

        // severe
        Group severeGrp = new Group(this, SWT.NONE);
        severeGrp.setLayout(new GridLayout(3, false));
        severeGrp.setText("Severe");

        tornadoChk = new Button(severeGrp, SWT.CHECK);
        tornadoChk.setText("Tornado");

        hailChk = new Button(severeGrp, SWT.CHECK);
        hailChk.setText("Large Hail");

        gustChk = new Button(severeGrp, SWT.CHECK);
        gustChk.setText("> 50 Kt Sfc Gusts");

        // condition composite
        Composite conditionComp = new Composite(this, SWT.NONE);
        conditionComp.setLayout(new GridLayout(3, false));

        noCondRdo = new Button(conditionComp, SWT.RADIO);
        noCondRdo.setText("No Conds Remark");
        noCondRdo.setSelection(true);

        contgRdo = new Button(conditionComp, SWT.RADIO);
        contgRdo.setText("Conds Contg Byd");

        imprRdo = new Button(conditionComp, SWT.RADIO);
        imprRdo.setText("Conds Impr By");

        // report composite
        Composite reportComp = new Composite(this, SWT.NONE);
        reportComp.setLayout(new GridLayout(4, false));

        aircraftChk = new Button(reportComp, SWT.CHECK);
        aircraftChk.setText("Rprtd by Aircraft");

        addnlInfoChk = new Button(reportComp, SWT.CHECK);
        addnlInfoChk.setText("Addnl Info for SIGMET");
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
     * create thunderstorm product text
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
            boolean isOperational, DrawingType drawTytpe, double drawWidth,
            String stateIDs) {

        int topsTo = Integer.parseInt(topsToCbo.getText());
        int topsFrom = 0;
        if (topsFromCbo.getSelectionIndex() > 1) {
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
                weatherType);
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

        // 4th line
        if (dvlpgChk.getSelection()) {
            output.append("DVLPG ");
        }

        if ((drawTytpe == DrawingType.AREA)
                || (drawTytpe == DrawingType.LINE && drawWidth >= 20.0)) {
            output.append("AREA ");
        } else if (drawTytpe == DrawingType.ISOLATED) {
            output.append("ISOL ");
        } else { // this must be line type
            output.append("LINE ");
        }

        if (embdChk.getSelection()) {
            output.append("EMBD ");
        }

        if (tornadoChk.getSelection() || hailChk.getSelection()
                || gustChk.getSelection()) {
            output.append("SEV ");
        }

        output.append(typeCbo.getText());

        int tmpDrawWidth = (int) drawWidth; // truncated to whole number
        if (drawTytpe == DrawingType.LINE && drawWidth < 20.0) {
            output.append(" ").append(tmpDrawWidth).append("NM WIDE");
        } else if (drawTytpe == DrawingType.ISOLATED) {
            output.append(" DIAM ").append(tmpDrawWidth).append("NM");
        }

        if (intstCbo.getSelectionIndex() == 0) {
            output.append(". ");
        } else {
            output.append(" WITH ").append(intstCbo.getText())
                    .append(" PCPN. ");
        }

        // the mov/move portion of line 4
        if (dirCbo.getText().equals("---")
                || MOV_LTL.equals(spdCbo.getText())) {
            output.append(MOV_LTL);
        } else {
            String speed = spdCbo.getText().substring(1); // remove the 0
            output.append("MOV FROM ");
            output.append(dirCbo.getText()).append(speed);
            output.append("KT");
        }

        output.append(" TOPS ");
        if (estChk.getSelection()) {
            output.append("EST ");
        }
        if (topsFromCbo.getSelectionIndex() == 0) {
            output.append("TO ");
        } else if (topsFromCbo.getSelectionIndex() == 1) {
            output.append("ABV ");
        } else {
            if (topsFrom >= 180) {
                output.append("FL");
            }
            output.append(topsFromCbo.getText()).append("-");
        }
        if (topsTo >= 180) {
            output.append("FL");
        }
        output.append(topsToCbo.getText()).append(".");

        if (tornadoChk.getSelection()) {
            output.append(" TORNADO POSS.");
        }
        if (hailChk.getSelection()) {
            output.append(" LARGE HAIL POSS.");
        }
        if (gustChk.getSelection()) {
            output.append(" OVR 50KT WIND GUST POSS.");
        }

        if (contgRdo.getSelection()) {
            output.append(" CONDS CONTG BYD ");
            output.append(endDateTime).append("Z.");
        } else if (imprRdo.getSelection()) {
            output.append(" CONDS IMPR BY ");
            output.append(endDateTime).append("Z.");
        }

        if (aircraftChk.getSelection()) {
            output.append(" RPRTD BY AIRCRAFT.");
        }

        if (addnlInfoChk.getSelection()) {
            output.append(" THIS IS ADDN INFO TO CONVECTIVE SIGMET ")
                    .append(addnlInfoTxt.getText()).append(".");
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

    public AbstractCWANewConfig getConfig() {
        ThunderstormNewConfig config = new ThunderstormNewConfig();
        config.setEndTime(getEndTime());
        config.setDvlpgChk(dvlpgChk.getSelection());
        config.setEmbdChk(embdChk.getSelection());
        config.setType(typeCbo.getText());
        config.setIntst(intstCbo.getText());
        config.setDir(dirCbo.getText());
        config.setSpd(spdCbo.getText());
        config.setTopsTo(topsToCbo.getText());
        config.setTopsFrom(topsFromCbo.getText());
        config.setEstChk(estChk.getSelection());
        config.setTornadoChk(tornadoChk.getSelection());
        config.setHailChk(hailChk.getSelection());
        config.setGustChk(gustChk.getSelection());
        if (noCondRdo.getSelection()) {
            config.setCond(CondType.NO_CONDS);
        } else if (this.contgRdo.getSelection()) {
            config.setCond(CondType.CONDS_CONTG);
        } else {
            config.setCond(CondType.CONDS_IMPR);
        }
        config.setAircraftChk(aircraftChk.getSelection());
        if (this.addnlInfoChk.getSelection()) {
            config.setAddnlInfo(addnlInfoTxt.getText());
        }
        config.setNoUpdateChk(noUpdateChk.getSelection());

        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWANewConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse Thunderstorm time.");
        }

        if (!(config instanceof ThunderstormNewConfig)) {
            return;
        }
        ThunderstormNewConfig tsc = (ThunderstormNewConfig) config;
        dvlpgChk.setSelection(tsc.isDvlpgChk());
        embdChk.setSelection(tsc.isEmbdChk());

        int index = typeCbo.indexOf(tsc.getType());
        if (index >= 0) {
            typeCbo.select(index);
        } else {
            typeCbo.select(0);
            logger.warn("Invalid type setting: " + tsc.getType());
        }

        index = intstCbo.indexOf(tsc.getIntst());
        if (index >= 0) {
            intstCbo.select(index);
        } else {
            intstCbo.select(0);
            logger.warn("Invalid intensity setting: " + tsc.getIntst());
        }

        index = dirCbo.indexOf(tsc.getDir());
        if (index >= 0) {
            dirCbo.select(index);
        } else {
            dirCbo.select(0);
            logger.warn("Invalid direction setting: " + tsc.getDir());
        }

        index = spdCbo.indexOf(tsc.getSpd());
        if (index >= 0) {
            spdCbo.select(index);
        } else {
            spdCbo.select(0);
            logger.warn("Invalid speed setting: " + tsc.getSpd());
        }

        index = topsToCbo.indexOf(tsc.getTopsTo());
        if (index >= 0) {
            topsToCbo.select(index);
        } else {
            topsToCbo.select(0);
            logger.warn("Invalid tops to setting: " + tsc.getTopsTo());
        }

        index = topsFromCbo.indexOf(tsc.getTopsFrom());
        if (index >= 0) {
            topsFromCbo.select(index);
        } else {
            topsFromCbo.select(0);
            logger.warn("Invalid tops from setting: " + tsc.getTopsFrom());
        }

        estChk.setSelection(tsc.isEstChk());
        tornadoChk.setSelection(tsc.isTornadoChk());
        hailChk.setSelection(tsc.isHailChk());
        gustChk.setSelection(tsc.isGustChk());

        switch (tsc.getCond()) {
        case NO_CONDS:
            noCondRdo.setSelection(true);
            break;
        case CONDS_CONTG:
            contgRdo.setSelection(true);
            break;
        case CONDS_IMPR:
            imprRdo.setSelection(true);
        }

        aircraftChk.setSelection(tsc.isAircraftChk());

        if (tsc.getAddnlInfo() == null || tsc.getAddnlInfo().isEmpty()) {
            addnlInfoTxt.setText("");
            addnlInfoChk.setSelection(false);
        } else {
            addnlInfoTxt.setText(tsc.getAddnlInfo());
            addnlInfoChk.setSelection(true);
        }

        noUpdateChk.setSelection(tsc.isNoUpdateChk());
    }

}
