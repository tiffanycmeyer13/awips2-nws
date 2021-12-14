/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfSubregion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisBDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisState;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewGenesisRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Start a Genesis Area
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 08, 2018  51349      wpaintsil  Initial creation.
 * Jun 24, 2018  79571      wpaintsil  Add backend save functionality.
 * Dec 10, 2020  85849      jwu        Update GUI & verification.
 * Dec 15, 2020  86027      jwu        Overhaul the code & layout.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class StartGenesisDialog extends OcpCaveSWTDialog {

    /**
     * Widgets
     */
    private Spinner genesisSpinner;

    private Text nameText;

    private CCombo subBasinCmb;

    private Spinner stormNumSpinner;

    private CCombo yearCmb;

    private CCombo monthCmb;

    private CCombo dayCmb;

    private CCombo hourCmb;

    private Text latText;

    private Button latNBtn;

    private Text lonText;

    private Button lonWBtn;

    private Label genesisIdLbl;

    private List<String> prevGeneses = new ArrayList<>();

    private static final String ATCF_ID_LABEL = "ATCF ID = ";

    // Default basin and year etc.
    private static AtcfSubregion subRegion = AtcfSubregion.W;

    private Calendar currCal = TimeUtil.newGmtCalendar();

    private int currYear = currCal.get(Calendar.YEAR);

    private static String[] hourValues = { "00", "06", "12", "18" };

    private static String[] dayString = AtcfVizUtil.getEntries(1, 31, 1, "%d");

    /**
     * Collection of verify listeners.
     */
    private final AtcfTextListeners verifyListeners = new AtcfTextListeners();

    /**
     * Constructor
     * 
     * @param parent
     */
    public StartGenesisDialog(Shell parent) {
        super(parent, SWT.SHEET);
        setText("Start a Genesis Area");

    }

    @Override
    protected void initializeComponents(Shell shell) {

        Composite top = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginWidth = 15;
        mainLayout.verticalSpacing = 0;
        top.setLayout(mainLayout);
        top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createGenesisId(top);
        createGenesisInfo(top);
        createStormInfo(top);
        createGenesisDate(top);
        createGenesisLatLon(top);
        createControlButtons(top);

        setGenesisNumMinimum();
    }

    /**
     * Creates composite to show the Genesis Id
     * 
     * @param top
     */
    private void createGenesisId(Composite top) {

        Composite genesisIdComp = new Composite(top, SWT.NONE);
        genesisIdComp.setLayout(new GridLayout(1, true));
        genesisIdComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        genesisIdLbl = new Label(genesisIdComp, SWT.NONE);
        genesisIdLbl.setText(ATCF_ID_LABEL);
    }

    /**
     * Creates the options for genesis information
     * 
     * @param top
     */
    private void createGenesisInfo(Composite top) {
        Composite genesisInfoComp = new Composite(top, SWT.NONE);
        GridLayout genesisInfoGL = new GridLayout(2, false);
        genesisInfoGL.horizontalSpacing = 50;
        genesisInfoGL.verticalSpacing = 0;
        genesisInfoGL.marginTop = 20;
        genesisInfoComp.setLayout(genesisInfoGL);
        genesisInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

        Label genesisNoLbl = new Label(genesisInfoComp, SWT.CENTER);
        genesisNoLbl.setText("Genesis #");
        genesisNoLbl
                .setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        Label genesisNameLbl = new Label(genesisInfoComp, SWT.CENTER);
        genesisNameLbl.setText("Name");
        genesisNameLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

        genesisSpinner = new Spinner(genesisInfoComp, SWT.BORDER);
        genesisSpinner
                .setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        nameText = new Text(genesisInfoComp, SWT.BORDER);
        nameText.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));
        nameText.setEditable(false);

        genesisSpinner.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                nameText.setText("GENESIS"
                        + String.format("%03d", genesisSpinner.getSelection()));
                genesisIdLbl.setText("ATCF ID =           " + getGenesisID());
            }
        });

    }

    /**
     * Creates the options for sub basin and storm number information
     * 
     * @param top
     */
    private void createStormInfo(Composite top) {

        Composite stormInfoComp = new Composite(top, SWT.NONE);
        GridLayout stormInfoGL = new GridLayout(2, false);
        stormInfoGL.horizontalSpacing = 50;
        stormInfoGL.marginTop = 20;
        stormInfoGL.verticalSpacing = 0;
        stormInfoComp.setLayout(stormInfoGL);
        stormInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

        Label subBasin = new Label(stormInfoComp, SWT.CENTER);
        subBasin.setText("Sub Basin");
        subBasin.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        Label stormNumber = new Label(stormInfoComp, SWT.CENTER);
        stormNumber.setText("Storm #");
        stormNumber.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

        subBasinCmb = new CCombo(stormInfoComp, SWT.DROP_DOWN | SWT.BORDER);
        GridLayout subBasinGL = new GridLayout(1, false);
        subBasinGL.horizontalSpacing = 10;
        subBasinGL.marginWidth = 10;
        subBasinCmb.setLayout(subBasinGL);
        subBasinCmb.setItems(AtcfSubregion.getDescriptions());
        subBasinCmb.setText(subRegion.getDescription());
        subBasinCmb
                .setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
        subBasinCmb.setEditable(false);

        subBasinCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setGenesisNumMinimum();
            }
        });

        stormNumSpinner = new Spinner(stormInfoComp, SWT.BORDER);
        stormNumSpinner.setIncrement(1);
        stormNumSpinner.setEnabled(false);

        stormNumSpinner.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

    }

    /**
     * Creates the options for genesis date and time
     * 
     * @param top
     */
    private void createGenesisDate(Composite top) {

        Composite genesisDateComp = new Composite(top, SWT.NONE);
        GridLayout genesisDateGL = new GridLayout(1, false);
        genesisDateGL.horizontalSpacing = 15;
        genesisDateGL.marginTop = 30;
        genesisDateComp.setLayout(genesisDateGL);
        genesisDateComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label dateTime = new Label(genesisDateComp, SWT.NONE);
        dateTime.setText("Starting Date and Time");
        dateTime.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Composite groupComp = new Composite(genesisDateComp, SWT.NONE);
        groupComp.setLayout(new GridLayout(1, false));
        groupComp.setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, false));

        Group middleGroup = new Group(groupComp, SWT.NONE);
        middleGroup.setLayout(new GridLayout(1, true));

        Composite timeLblsComp = new Composite(middleGroup, SWT.NONE);
        GridLayout timeLblsGL = new GridLayout(4, false);
        timeLblsGL.marginHeight = 0;
        timeLblsGL.verticalSpacing = 0;
        timeLblsGL.horizontalSpacing = 30;

        timeLblsComp.setLayout(timeLblsGL);
        timeLblsComp
                .setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        Label yearLbl = new Label(timeLblsComp, SWT.None);
        yearLbl.setText("Year");
        yearLbl.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        Label monthLbl = new Label(timeLblsComp, SWT.None);
        monthLbl.setText("Month");
        monthLbl.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        Label dayLbl = new Label(timeLblsComp, SWT.None);
        dayLbl.setText("Day");
        dayLbl.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        Label hourLbl = new Label(timeLblsComp, SWT.None);
        hourLbl.setText("Hour");
        hourLbl.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        yearCmb = new CCombo(timeLblsComp, SWT.DROP_DOWN | SWT.BORDER);
        yearCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        yearCmb.add(String.valueOf(currYear));
        yearCmb.add(String.valueOf(currYear + 1));
        yearCmb.setEditable(false);
        yearCmb.select(0);

        monthCmb = new CCombo(timeLblsComp, SWT.DROP_DOWN | SWT.BORDER);
        for (int months = 1; months < 13; months++) {
            monthCmb.add(String.valueOf(months));
        }
        monthCmb.select(Calendar.getInstance().get(Calendar.MONTH));
        monthCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        monthCmb.setEditable(false);

        ModifyListener dayChangeListener = e -> populateDay();
        monthCmb.addModifyListener(dayChangeListener);
        yearCmb.addModifyListener(dayChangeListener);

        dayCmb = new CCombo(timeLblsComp, SWT.DROP_DOWN | SWT.BORDER);
        dayCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        dayCmb.setEditable(false);
        populateDay();

        hourCmb = new CCombo(timeLblsComp, SWT.DROP_DOWN | SWT.BORDER);
        for (String entry : hourValues) {
            hourCmb.add(entry);
        }
        hourCmb.select(0);
        hourCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        hourCmb.setEditable(false);
    }

    /**
     * Creates the options for genesis latitude and longitude
     * 
     * @param top
     */
    private void createGenesisLatLon(Composite top) {
        Composite genesisLatLonComp = new Composite(top, SWT.NONE);
        GridLayout latLonGL = new GridLayout(1, false);
        latLonGL.marginTop = 30;
        genesisLatLonComp.setLayout(latLonGL);
        genesisLatLonComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label locLbl = new Label(genesisLatLonComp, SWT.NONE);
        locLbl.setText("Center Lat/Lon");
        locLbl.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Composite groupComp = new Composite(genesisLatLonComp, SWT.NONE);
        groupComp.setLayout(new GridLayout(1, true));
        groupComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Group latLonGrp = new Group(groupComp, SWT.NONE);
        GridLayout latLonGrpGL = new GridLayout(4, false);
        latLonGrpGL.horizontalSpacing = 10;
        latLonGrpGL.marginWidth = 10;
        latLonGrp.setLayout(latLonGrpGL);
        latLonGrp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label latLbl = new Label(latLonGrp, SWT.None);
        latLbl.setText("Latitude");
        GridData latGD = new GridData();
        latGD.horizontalSpan = 2;
        latLbl.setLayoutData(latGD);

        Label lonLbl = new Label(latLonGrp, SWT.None);
        lonLbl.setText("Longitude");
        GridData lonGD = new GridData();
        lonGD.horizontalSpan = 2;
        lonLbl.setLayoutData(lonGD);

        latText = new Text(latLonGrp, SWT.BORDER);
        GridData latTextGD = new GridData();
        latTextGD.grabExcessHorizontalSpace = true;
        latText.setLayoutData(latTextGD);

        latText.addListener(SWT.Verify, verifyListeners.getLatVerifyListener());

        Group latGrp = new Group(latLonGrp, SWT.NONE);
        latGrp.setLayout(new RowLayout(SWT.HORIZONTAL));
        Button latSBtn = new Button(latGrp, SWT.RADIO);
        latSBtn.setText("N");
        latSBtn.setSelection(true);

        latNBtn = new Button(latGrp, SWT.RADIO);
        latNBtn.setText("S");

        lonText = new Text(latLonGrp, SWT.BORDER);
        GridData lonTextGD = new GridData();
        lonTextGD.grabExcessHorizontalSpace = true;
        lonText.setLayoutData(lonTextGD);

        lonText.addListener(SWT.Verify, verifyListeners.getLonVerifyListener());

        Group lonGrp = new Group(latLonGrp, SWT.NONE);
        lonGrp.setLayout(new RowLayout(SWT.HORIZONTAL));

        Button lonEBtn = new Button(lonGrp, SWT.RADIO);
        lonEBtn.setText("E");

        lonWBtn = new Button(lonGrp, SWT.RADIO);
        lonWBtn.setText("W");
        lonWBtn.setSelection(true);

    }

    /**
     * Create bottom buttons.
     * 
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnGL = new GridLayout(2, true);
        ctrlBtnGL.marginWidth = 40;
        ctrlBtnGL.marginHeight = 20;
        ctrlBtnGL.horizontalSpacing = 40;
        ctrlBtnComp.setLayout(ctrlBtnGL);

        Button okBtn = new Button(ctrlBtnComp, SWT.PUSH);
        okBtn.setText("Create");
        GridData okBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        okBtnGD.minimumWidth = 120;
        okBtn.setLayoutData(okBtnGD);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean canSave = true;

                if (!verifyGenesisId() || !verifyLatField() || !verifyLonField()
                        || !verifyName()) {
                    canSave = false;
                }

                if (canSave) {
                    saveGenesis();
                } else {
                    openError();
                }
            }
        });
        okBtn.setFocus();

        Button cancelBtn = new Button(ctrlBtnComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        cancelBtn.setFocus();
    }

    /**
     * Determine available genesis numbers by basin and year.
     * 
     * @return the minimum number based on what's available
     */
    private void setGenesisNumMinimum() {

        List<Genesis> existingGeneses = AtcfDataUtil.getGenesisBasinMap()
                .get(AtcfSubregion.getBasinNameByDesc(subBasinCmb.getText()));
        int minimumNum = 1;

        // Increment available genesis number according to what already exists.
        if (existingGeneses != null && !existingGeneses.isEmpty()) {
            minimumNum = existingGeneses.get(existingGeneses.size() - 1)
                    .getGenesisNum() + 1;
        }

        String paddedGenNum = String.format("%03d", minimumNum);

        genesisSpinner.setMinimum(minimumNum);
        genesisSpinner.setSelection(minimumNum);
        stormNumSpinner.setMinimum(minimumNum);
        stormNumSpinner.setSelection(minimumNum);
        genesisIdLbl.setText("ATCF ID =           " + getGenesisID());

        nameText.setText("GENESIS" + paddedGenNum);
    }

    /**
     * Creates genesis ID based on genesis name, number, and sub basin
     */
    private String getGenesisID() {
        return AtcfSubregion.getBasinNameByDesc(subBasinCmb.getText())
                + String.format("%02d",
                        Integer.parseInt(genesisSpinner.getText()))
                + yearCmb.getText();
    }

    /**
     * Validation error message
     */
    private void openError() {
        MessageBox errorDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.OK);
        errorDialog.setText("Alert");

        String errorString = "Please fix the following fields before saving :";
        errorString += (verifyGenesisId() ? ""
                : "\nThis Genesis already exists");

        errorString += (verifyName() ? "" : "\nName field is invalid");

        errorString += (verifyLatField() ? "" : "\nLatitude field is invalid");

        errorString += (verifyLonField() ? "" : "\nLongitude field is invalid");

        errorDialog.setMessage(errorString);
        errorDialog.open();
    }

    /**
     * Save a new Genesis and Genesis B-Deck
     */
    private void saveGenesis() {
        Genesis newGenesis = new Genesis();

        newGenesis.setGenesisName(nameText.getText());
        newGenesis.setGenesisNum(Integer.parseInt(genesisSpinner.getText()));
        newGenesis.setGenesisId(getGenesisID());
        newGenesis.setGenesisState(GenesisState.GENESIS.toString());

        String subg = subBasinCmb.getText();
        newGenesis.setRegion(AtcfSubregion.getBasinNameByDesc(subg));
        newGenesis.setSubRegion(AtcfSubregion.getNameByDesc(subg));

        Calendar cal = TimeUtil.newGmtCalendar();
        cal.set(Integer.parseInt(yearCmb.getText()),
                Integer.parseInt(monthCmb.getText()) - 1,
                Integer.parseInt(dayCmb.getText()),
                Integer.parseInt(hourCmb.getText()), 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        newGenesis.setStartDTG(cal);

        newGenesis.setYear(Integer.parseInt(yearCmb.getText()));

        NewGenesisRequest newGenRequest = new NewGenesisRequest();

        newGenRequest.setNewGenesis(newGenesis);

        List<GenesisBDeckRecord> newGenesisBDeck = getNewGenesisBDeckRecord();

        newGenRequest.setGenesisBDeckRecord(newGenesisBDeck);
        boolean success = false;
        try {
            success = (boolean) ThriftClient.sendRequest(newGenRequest);

        } catch (VizException e) {
            logger.warn("StartGenesisDialog - Adding New Genesis Failed", e);

        }
        MessageBox successDialog = new MessageBox(shell,
                (success ? SWT.ICON_INFORMATION : SWT.ICON_WARNING) | SWT.OK);
        successDialog.setText("Alert");
        successDialog.setMessage("Genesis " + getGenesisID()
                + (success ? "" : " could not be") + " saved");
        AtcfDataUtil.updateGenesisList(newGenesis);
        successDialog.open();
        close();
    }

    /**
     * Saves all the information to a new GenesisBDeckRecord
     * 
     * @return
     */
    private ArrayList<GenesisBDeckRecord> getNewGenesisBDeckRecord() {

        GenesisBDeckRecord genesisBDeckRecord = new GenesisBDeckRecord();
        genesisBDeckRecord
                .setCycloneNum(Integer.parseInt(stormNumSpinner.getText()));
        genesisBDeckRecord.setBasin(
                AtcfSubregion.getBasinNameByDesc(subBasinCmb.getText()));
        genesisBDeckRecord.setSubRegion(
                AtcfSubregion.getNameByDesc(subBasinCmb.getText()));
        genesisBDeckRecord.setStormName(nameText.getText());

        float tempLat = Float.parseFloat(latText.getText());
        if (latNBtn.getSelection()) {
            tempLat *= -1;
        }
        genesisBDeckRecord.setClat(tempLat);

        float tempLon = Float.parseFloat(lonText.getText());
        if (lonWBtn.getSelection()) {
            tempLon *= -1;
        }

        genesisBDeckRecord.setClon(tempLon);

        genesisBDeckRecord.setGenesisNum(genesisSpinner.getSelection());
        genesisBDeckRecord.setYear(Integer.parseInt(yearCmb.getText()));

        genesisBDeckRecord.setRefTime(Date.from(LocalDateTime
                .of(Integer.parseInt(yearCmb.getText()),
                        Integer.parseInt(monthCmb.getText()),
                        Integer.parseInt(dayCmb.getText()),
                        Integer.parseInt(hourCmb.getText()), 0)
                .toInstant(ZoneOffset.UTC)));

        ArrayList<GenesisBDeckRecord> recs = new ArrayList<>();
        recs.add(genesisBDeckRecord);

        return recs;
    }

    /*
     * Verifies the validity of the latitude text field for saving
     * 
     * @return validity
     */
    public boolean verifyLatField() {
        boolean valid = true;
        float temp = 0;
        try {
            temp = Float.parseFloat(latText.getText());
        } catch (Exception ex) {
            logger.warn("StartGenesisDialog - Invalid lat.", ex);
            return false;
        }

        if (Math.abs(temp) > 90) {
            valid = false;
        }

        return valid;
    }

    /**
     * Verifies the validity of the longitude text field for saving
     * 
     * @return validity
     */
    public boolean verifyLonField() {
        boolean valid = true;
        float temp = 0;
        try {
            temp = Float.parseFloat(lonText.getText());
        } catch (Exception ex) {
            logger.warn("StartGenesisDialog - Invalid lon.", ex);
            return false;
        }

        if (Math.abs(temp) > 180) {
            valid = false;
        }

        return valid;
    }

    /**
     * Makes sure the gensis ID field is unique
     * 
     * @return isUnique?
     */
    public boolean verifyGenesisId() {
        boolean isUnique = true;

        List<Genesis> genesesInList = AtcfDataUtil.getGenesisList();

        for (Genesis gis : genesesInList) {
            if (gis.getGenesisId().length() >= 4) {
                String item = gis.getRegion()
                        + gis.getGenesisId().substring(2, 4) + gis.getYear();
                if (getGenesisID().equals(item)) {
                    isUnique = false;
                    break;
                }
            }
        }
        for (String gisStr : prevGeneses) {
            if (getGenesisID().equals(gisStr)) {
                isUnique = false;
                break;
            }
        }
        return isUnique;
    }

    /**
     * Verifies Name field
     * 
     * @return valid
     */
    public boolean verifyName() {
        boolean isValid = true;
        String text = nameText.getText();

        if (text.isEmpty()) {
            return false;
        }

        if (text.length() > Storm.NAME_SIZE) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Populates the Day field based on the selected month and year.
     * 
     * @return valid
     */
    public void populateDay() {

        int selected;

        // If this is the first time the list is created, set select to the
        // current day. if not, save what was previously selected.
        if (dayCmb.getSelectionIndex() != -1) {
            selected = dayCmb.getSelectionIndex();
        } else {
            selected = currCal.get(Calendar.DAY_OF_MONTH) - 1;
        }

        dayCmb.removeAll();

        int month = Integer.parseInt(monthCmb.getText());
        int year = Integer.parseInt(yearCmb.getText());

        YearMonth yearMonthObj = YearMonth.of(year, month);
        int daysInMonth = yearMonthObj.lengthOfMonth();

        String[] items = Arrays.copyOfRange(dayString, 0, daysInMonth);

        dayCmb.setItems(items);

        if (selected >= dayCmb.getItems().length || selected < 0) {
            selected = items.length - 1;
        }

        dayCmb.select(selected);
    }

}
