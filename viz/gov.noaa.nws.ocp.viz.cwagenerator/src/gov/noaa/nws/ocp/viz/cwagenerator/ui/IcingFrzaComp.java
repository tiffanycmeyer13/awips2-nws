/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.IcingFrzaNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the Icing/Freezing composite.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 12/02/2016  17469    wkwock      Initial creation
 * 09/10/2021  28802    wkwock      Use new configuration format
 * 04/05/2022  22989    wkwock      Update flight level and additional info text
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class IcingFrzaComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(IcingFrzaComp.class);

    /** frequency combo */
    private Combo freqCbo;

    /** intensity combo */
    private Combo intstyCbo;

    /** type combo */
    private Combo typeCbo;

    /** flight from bombo */
    private Combo flightFromCbo;

    /** flight to combo */
    private Combo flightToCbo;

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

    /** frequency items */
    private static final String[] freqItems = { " OCNL", " FQT", " CONS" };

    /** intensity items */
    private static final String[] intstyItems = { "MOD", "MOD/SEV", "SEV",
            "EXTRM" };

    /** type items */
    private static final String[] typeItems = { "CLR ICE", "RIME ICE",
            "MXD ICE", "FZDZ", "FZRA", "SLT" };

    /**
     * Constructor
     * 
     * @param parent
     */
    public IcingFrzaComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.ICIINGFRZA;
        setLayout(new GridLayout(1, false));

        createTimeComp();

        // Frequency
        Composite freqComp = new Composite(this, SWT.NONE);
        freqComp.setLayout(new GridLayout(6, false));
        Label freqLbl = new Label(freqComp, SWT.NONE);
        freqLbl.setText("Freq:");

        freqCbo = new Combo(freqComp, SWT.READ_ONLY);
        freqCbo.setItems(freqItems);

        // intsty
        Label intstyLbl = new Label(freqComp, SWT.NONE);
        intstyLbl.setText("Intst:");
        GridData intstyGd = new GridData();
        intstyGd.horizontalIndent = HORIZONTAL_INDENT;
        intstyLbl.setLayoutData(intstyGd);

        intstyCbo = new Combo(freqComp, SWT.READ_ONLY);
        intstyCbo.setItems(intstyItems);
        intstyCbo.select(0);

        // type
        Label typeLbl = new Label(freqComp, SWT.NONE);
        typeLbl.setText("Type:");
        GridData typeGd = new GridData();
        typeGd.horizontalIndent = HORIZONTAL_INDENT;
        typeLbl.setLayoutData(typeGd);

        typeCbo = new Combo(freqComp, SWT.READ_ONLY);
        typeCbo.setItems(typeItems);
        typeCbo.select(0);

        // from flight
        Composite flightComp = new Composite(this, SWT.NONE);
        flightComp.setLayout(new GridLayout(4, false));
        Label fromFlightLbl = new Label(flightComp, SWT.NONE);
        fromFlightLbl.setText("Flight Level From:");

        flightFromCbo = new Combo(flightComp, SWT.READ_ONLY);
        flightFromCbo.add("SFC");
        for (int i = 10; i <= 440; i += 10) {
            flightFromCbo.add(String.format("%03d", i));
        }
        flightFromCbo.select(0);
        flightFromCbo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateToFlightCbo();
            }
        });

        // to flight
        Label toFlightLbl = new Label(flightComp, SWT.NONE);
        toFlightLbl.setText("To:");

        flightToCbo = new Combo(flightComp, SWT.READ_ONLY);
        for (int i = 10; i <= 450; i += 10) {
            flightToCbo.add(String.format("%03d", i));
        }
        flightToCbo.select(0);

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
        Composite reportGrp = new Composite(this, SWT.NONE);
        reportGrp.setLayout(new GridLayout(4, false));

        aircraftChk = new Button(reportGrp, SWT.CHECK);
        aircraftChk.setText("Rprtd by Aircraft");

        addnlInfoChk = new Button(reportGrp, SWT.CHECK);
        addnlInfoChk.setText("Addnl Info to AIRMET ZULU");
        addnlInfoTxt = new Text(reportGrp, SWT.BORDER);
        GridData gd = new GridData();
        GC gc = new GC(addnlInfoTxt);
        gd.widthHint = (int) (gc.getFontMetrics().getAverageCharacterWidth()
                * 10);
        addnlInfoTxt.setLayoutData(gd);
        gc.dispose();

        noUpdateChk = new Button(reportGrp, SWT.CHECK);
        noUpdateChk.setText("No Updt Aft");
    }

    /**
     * update 'To: Flight Levels'
     */
    private void updateToFlightCbo() {

        int initLevel = (flightFromCbo.getSelectionIndex() + 1) * 10;

        int flightToLevel = Integer.parseInt(flightToCbo.getText());

        flightToCbo.removeAll();
        for (int i = initLevel; i <= 450; i += 10) {
            flightToCbo.add(String.format("%03d", i));
        }
        if (flightToLevel < initLevel) {
            flightToCbo.select(0);
        } else {
            int index = (flightToLevel - initLevel) / 10;
            flightToCbo.select(index);
        }
    }

    /**
     * Create product text for icing/frza
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
            boolean isOperational, DrawingType type, double width,
            String stateIDs) {

        String endDateTime = getEndTime();

        String frmLvl = "BLW ";
        if (flightFromCbo.getSelectionIndex() != 0) {
            int flightFrom = Integer.parseInt(flightFromCbo.getText());
            if (flightFrom < 180) {
                frmLvl = flightFromCbo.getText() + "-";
            } else {
                frmLvl = "FL" + flightFromCbo.getText() + "-";
            }
        }

        String toLvl = "";
        int flightTo = Integer.parseInt(flightToCbo.getText());
        if (flightTo >= 180) {
            toLvl = "FL";
        }
        toLvl += flightToCbo.getText();

        // get next series ID
        CWAProduct cwaProduct = new CWAProduct(productId, cwsuId, isOperational,
                weatherType);

        int seriesId = cwaProduct.getNextSeriesId(isCor,
                cwaConfigs.getLocalTimeZone(), isResetIssuance());

        StringBuilder output = new StringBuilder();
        output.append(getHeaderLines(wmoId, header, isCor));
        output.append(getValidLine(cwsuId, endDateTime, seriesId));
        output.append(fromline).append("\n");
        output.append(body);
        output.append(freqCbo.getText()).append(" ").append(intstyCbo.getText())
                .append(" ").append(typeCbo.getText()).append(" ");
        output.append(frmLvl).append(toLvl);

        if (!noCondRdo.getSelection()) {
            if (contgRdo.getSelection()) {
                output.append(" CONDS CONTG BYD ");
            } else {
                output.append(" CONDS IMPR BY ");
            }
            output.append(endDateTime).append("Z.");
        }
        if (aircraftChk.getSelection()) {
            output.append(" RPRTD BY AIRCRAFT.");
        }
        if (addnlInfoChk.getSelection()) {
            output.append(" THIS IS ADDN INFO TO AIRMET ZULU ");
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

    public AbstractCWANewConfig getConfig() {
        IcingFrzaNewConfig config = new IcingFrzaNewConfig();
        config.setFreq(freqCbo.getItem(freqCbo.getSelectionIndex()));
        config.setIntsty(intstyCbo.getItem(intstyCbo.getSelectionIndex()));
        config.setType(typeCbo.getItem(typeCbo.getSelectionIndex()));
        config.setFlightFrom(
                flightFromCbo.getItem(flightFromCbo.getSelectionIndex()));
        config.setFlightTo(
                flightToCbo.getItem(flightToCbo.getSelectionIndex()));
        config.setNoCond(noCondRdo.getSelection());
        config.setContg(contgRdo.getSelection());
        config.setImpr(imprRdo.getSelection());
        config.setAircraft(aircraftChk.getSelection());

        if (addnlInfoChk.getSelection()) {
            config.setAddnlInfo(addnlInfoTxt.getText());
        } else {
            config.setAddnlInfo("");
        }

        config.setNoUpdate(noUpdateChk.getSelection());

        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWANewConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse icing/fraza time.");
        }

        if (!(config instanceof IcingFrzaNewConfig)) {
            return;
        }

        IcingFrzaNewConfig ifc = (IcingFrzaNewConfig) config;
        int index = freqCbo.indexOf(ifc.getFreq());
        if (index >= 0) {
            freqCbo.select(index);
        } else {
            freqCbo.select(0);
            logger.warn("Invalid frequency setting: " + ifc.getFreq());
        }

        index = intstyCbo.indexOf(ifc.getIntsty());
        if (index >= 0) {
            intstyCbo.select(index);
        } else {
            intstyCbo.select(0);
            logger.warn("Invalid intensity setting: " + ifc.getIntsty());
        }

        index = typeCbo.indexOf(ifc.getType());
        if (index >= 0) {
            typeCbo.select(index);
        } else {
            typeCbo.select(0);
            logger.warn("Invalid type setting: " + ifc.getType());
        }

        index = flightFromCbo.indexOf(ifc.getFlightFrom());
        if (index >= 0) {
            flightFromCbo.select(index);
        } else {
            flightFromCbo.select(0);
            logger.warn("Invalid flight from setting: " + ifc.getFlightFrom());
        }

        index = flightToCbo.indexOf(ifc.getFlightTo());
        if (index >= 0) {
            flightToCbo.select(index);
        } else {
            flightToCbo.select(0);
            logger.warn("Invalid flight to setting: " + ifc.getFlightTo());
        }

        noCondRdo.setSelection(ifc.isNoCond());
        contgRdo.setSelection(ifc.isContg());
        imprRdo.setSelection(ifc.isImpr());

        aircraftChk.setSelection(ifc.isAircraft());

        if (ifc.getAddnlInfo() == null || ifc.getAddnlInfo().isEmpty()) {
            addnlInfoChk.setSelection(false);
            addnlInfoTxt.setText("");
        } else {
            addnlInfoChk.setSelection(true);
            addnlInfoTxt.setText(ifc.getAddnlInfo());
        }

        noUpdateChk.setSelection(ifc.isNoUpdate());
    }
}
