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
import gov.noaa.nws.ocp.viz.cwagenerator.config.TurbLlwsNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * This class displays the turbulence/LLWS composite.
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
public class TurbLlwsComp extends AbstractCWAComp {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(TurbLlwsComp.class);

    /** frequency combo */
    private Combo freqCbo;

    /** intensity combo */
    private Combo intstyCbo;

    /** flight from combo */
    private Combo flightFromCbo;

    /** flight to combo */
    private Combo flightToCbo;

    /** Low Level Wind Shear check button */
    private Button llwsChk;

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
    private static final String[] intstyItems = { "MOD", "MOD ISOL SEV",
            "MOD OCNL SEV", "MOD/SEV", "SEV", "EXTRM" };

    /**
     * constructor
     * 
     * @param parent
     */
    public TurbLlwsComp(Composite parent, CWAGeneratorConfig cwaConfigs) {
        super(parent, SWT.NONE, cwaConfigs);
        weatherType = WeatherType.TURBLLWS;
        setLayout(new GridLayout(1, false));

        createTimeComp();

        // Frequency
        Composite freqComp = new Composite(this, SWT.NONE);
        freqComp.setLayout(new GridLayout(4, false));
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

        // Add LLWS
        llwsChk = new Button(this, SWT.CHECK);
        llwsChk.setText("Add LLWS");

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
        addnlInfoChk.setText("Addnl Info to AIRMET TANGO");
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
     * Create product text for Turbulence/LLWS
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

        String frmLVL = "BLW ";
        if (flightFromCbo.getSelectionIndex() != 0) {
            int flightFrom = Integer.parseInt(flightFromCbo.getText());
            if (flightFrom < 180) {
                frmLVL = flightFromCbo.getText() + "-";
            } else {
                frmLVL = "FL" + flightFromCbo.getText() + "-";
            }
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
        output.append(body);
        output.append(freqCbo.getText()).append(" ").append(intstyCbo.getText())
                .append(" TURB ");
        output.append(frmLVL).append("FL").append(flightToCbo.getText());
        if (llwsChk.getSelection()) {
            output.append(" AND LLWS");
        }

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
            output.append(" THIS IS ADDN INFO TO AIRMET TANGO ");
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
        TurbLlwsNewConfig config = new TurbLlwsNewConfig();
        if (this.addnlInfoChk.getSelection()) {
            config.setAddnlInfo(addnlInfoTxt.getText());
        } else {
            config.setAddnlInfo("");
        }
        config.setAircraftChk(aircraftChk.getSelection());
        config.setContg(contgRdo.getSelection());
        config.setEndTime(getEndTime());
        config.setFlightFrom(
                flightFromCbo.getItem(flightFromCbo.getSelectionIndex()));
        config.setFlightTo(
                flightToCbo.getItem(flightToCbo.getSelectionIndex()));
        config.setFreq(freqCbo.getText());
        config.setImpr(imprRdo.getSelection());
        config.setIntsty(intstyCbo.getItem(intstyCbo.getSelectionIndex()));
        config.setLlwsChk(llwsChk.getSelection());
        config.setNoCond(noCondRdo.getSelection());
        config.setNoUpdateChk(noUpdateChk.getSelection());
        return config;
    }

    @Override
    public void updateProductConfig(AbstractCWANewConfig config) {
        try {
            super.updateProductConfig(config);
        } catch (ParseException e) {
            logger.error("Failed to parse Turbllws time.");
        }
        if (!(config instanceof TurbLlwsNewConfig)) {
            return;
        }
        TurbLlwsNewConfig tlc = (TurbLlwsNewConfig) config;
        int index = freqCbo.indexOf(tlc.getFreq());
        if (index >= 0) {
            freqCbo.select(index);
        } else {
            freqCbo.select(0);
            logger.warn("Invalid frequency setting: " + tlc.getFreq());
        }

        index = intstyCbo.indexOf(tlc.getIntsty());
        if (index >= 0) {
            intstyCbo.select(index);
        } else {
            intstyCbo.select(0);
            logger.warn("Invalid intensity setting: " + tlc.getIntsty());
        }

        index = flightFromCbo.indexOf(tlc.getFlightFrom());
        if (index >= 0) {
            flightFromCbo.select(index);
        } else {
            flightFromCbo.select(0);
            logger.warn("Invalid flight from setting: " + tlc.getFlightFrom());
        }

        index = flightToCbo.indexOf(tlc.getFlightTo());
        if (index >= 0) {
            flightToCbo.select(index);
        } else {
            flightToCbo.select(0);
            logger.warn("Invalid flight to setting: " + tlc.getFlightTo());
        }

        llwsChk.setSelection(tlc.isLlwsChk());

        noCondRdo.setSelection(tlc.isNoCond());
        contgRdo.setSelection(tlc.isContg());
        imprRdo.setSelection(tlc.isImpr());

        aircraftChk.setSelection(tlc.isAircraftChk());

        if (tlc.getAddnlInfo() == null || tlc.getAddnlInfo().isEmpty()) {
            addnlInfoChk.setSelection(false);
            addnlInfoTxt.setText("");
        } else {
            addnlInfoChk.setSelection(true);
            addnlInfoTxt.setText(tlc.getAddnlInfo());
        }

        noUpdateChk.setSelection(tlc.isNoUpdateChk());
    }
}
