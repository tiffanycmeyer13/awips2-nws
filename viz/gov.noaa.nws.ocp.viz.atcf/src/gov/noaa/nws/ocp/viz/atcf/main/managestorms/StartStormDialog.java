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
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfSubregion;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.NewStormRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Start a Storm.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2018 45688      dmanzella   initial creation
 * Jun 20, 2019 64507      dmanzella   implemented backend functionality
 * Aug 16, 2019 67323      jwu         Add verify listeners.
 * Nov 09, 2020 84705      jwu         Update with other storm management.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class StartStormDialog extends OcpCaveSWTDialog {

    /*
     * TODO Storm name length is limited to 10; if the length is over 10, it
     * will be truncated and use the storm name lookup table
     */
    private static final int STORM_NAME_LENGTH = Storm.NAME_SIZE;

    private CCombo subBasinCmb;

    private CCombo stormNumberCmb;

    private Text stormNameText;

    private CCombo yearCmb;

    private CCombo monthCmb;

    private CCombo dayCmb;

    private CCombo hourCmb;

    private Button lonWestButton;

    private Button latSouthButton;

    private Text latText;

    private Text lonText;

    private Text intensityText;

    private String stormIdText = "";

    private Label stormIdLabel;

    // Default basin and year etc.
    private AtcfSubregion subRegion = AtcfSubregion.W;

    private int currYear = TimeUtil.newGmtCalendar().get(Calendar.YEAR);

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
     * @param stormList
     */
    public StartStormDialog(Shell parent) {
        super(parent);
    }

    /**
     * Initializes the components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createContents();
    }

    /**
     * Create dialog area.
     * 
     */
    public void createContents() {

        getShell().setText("Start a Storm");
        Composite top = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.verticalSpacing = 0;
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        top.setLayoutData(mainLayoutData);

        createStormId(top);
        createStormInfo(top);
        createStormDate(top);
        createStormLatLon(top);
        createControlButtons(top);

        updateStormNumber();
    }

    /**
     * Creates the Id for the storm
     * 
     * @param top
     */
    private void createStormId(Composite top) {
        Composite availableComp = new Composite(top, SWT.NONE);
        GridLayout idGridLayout = new GridLayout(1, true);
        idGridLayout.marginBottom = 15;

        availableComp.setLayout(idGridLayout);
        availableComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        stormIdLabel = new Label(availableComp, SWT.NONE);
    }

    /**
     * Creates the options for storm information
     * 
     * @param top
     */
    private void createStormInfo(Composite top) {

        Composite stormInfoComposite = new Composite(top, SWT.NONE);
        GridLayout stormInfoGridLayout = new GridLayout(3, false);
        stormInfoGridLayout.horizontalSpacing = 50;
        stormInfoGridLayout.verticalSpacing = 0;
        stormInfoComposite.setLayout(stormInfoGridLayout);
        stormInfoComposite.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

        GridData subBasinWidthGridData = new GridData(SWT.NONE, SWT.NONE, false,
                false);
        Label subBasin = new Label(stormInfoComposite, SWT.CENTER);
        subBasin.setText("Sub Basin");
        subBasin.setLayoutData(subBasinWidthGridData);

        GridData stormNumberWidthGridData = new GridData(SWT.CENTER, SWT.NONE,
                false, false);
        Label stormNumber = new Label(stormInfoComposite, SWT.CENTER);
        stormNumber.setText("Number");
        stormNumber.setLayoutData(stormNumberWidthGridData);

        GridData stormNameWidthGridData = new GridData(SWT.CENTER, SWT.NONE,
                false, false);
        Label stormName = new Label(stormInfoComposite, SWT.CENTER);
        stormName.setText("Name");
        stormName.setLayoutData(stormNameWidthGridData);

        subBasinCmb = new CCombo(stormInfoComposite,
                SWT.DROP_DOWN | SWT.BORDER);
        GridLayout subBasinGridLayout = new GridLayout(1, false);
        subBasinGridLayout.horizontalSpacing = 10;
        subBasinGridLayout.marginWidth = 10;
        subBasinCmb.setLayout(subBasinGridLayout);
        subBasinCmb.setItems(AtcfSubregion.getDescriptions());
        subBasinCmb.setText(subRegion.getDescription());
        subBasinCmb.setLayoutData(subBasinWidthGridData);
        subBasinCmb.setEditable(false);
        subBasinCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                subRegion = AtcfSubregion
                        .getSubregionByDesc(subBasinCmb.getText());
                updateStormNumber();
            }
        });

        stormNumberCmb = new CCombo(stormInfoComposite,
                SWT.DROP_DOWN | SWT.BORDER);
        GridLayout stormNumberGridLayout = new GridLayout(1, false);
        stormNumberCmb.setLayout(stormNumberGridLayout);
        String[] cylNumbers = AtcfVizUtil.getEntries(1, 99, 1, "%02d");
        stormNumberCmb.setItems(cylNumbers);
        stormNumberCmb.setEditable(false);
        stormNumberCmb.setLayoutData(stormNumberWidthGridData);
        stormNumberCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateStormID();
            }
        });

        stormNameText = new Text(stormInfoComposite, SWT.BORDER);
        stormNameText.setLayoutData(stormNameWidthGridData);
        stormNameText.setTextLimit(STORM_NAME_LENGTH);
        stormNameText.addVerifyListener(e -> e.text = e.text.toUpperCase());
    }

    /**
     * Creates the options for storm date and time
     * 
     * @param top
     */
    private void createStormDate(Composite top) {

        Composite stormDateComposite = new Composite(top, SWT.NONE);
        GridLayout dateCompLayout = new GridLayout(1, false);
        dateCompLayout.horizontalSpacing = 15;
        dateCompLayout.marginTop = 30;
        stormDateComposite.setLayout(dateCompLayout);
        stormDateComposite
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label dateTime = new Label(stormDateComposite, SWT.NONE);
        dateTime.setText("Starting Date and Time");
        dateTime.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Composite groupComposite = new Composite(stormDateComposite, SWT.NONE);
        groupComposite.setLayout(new GridLayout(1, false));
        groupComposite
                .setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, false));

        Group dateGroup = new Group(groupComposite, SWT.NONE);
        GridLayout dateGroupLayout = new GridLayout(1, true);
        dateGroup.setLayout(dateGroupLayout);

        Composite timeLabelsComposite = new Composite(dateGroup, SWT.NONE);
        GridLayout timeLabelsGridLayout = new GridLayout(4, false);
        timeLabelsGridLayout.marginHeight = 0;
        timeLabelsGridLayout.verticalSpacing = 0;
        timeLabelsGridLayout.horizontalSpacing = 30;

        timeLabelsComposite.setLayout(timeLabelsGridLayout);
        timeLabelsComposite
                .setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));

        Label yearLabel = new Label(timeLabelsComposite, SWT.None);
        yearLabel.setText("Year");
        yearLabel.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        Label monthLabel = new Label(timeLabelsComposite, SWT.None);
        monthLabel.setText("Month");
        monthLabel
                .setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        Label dayLabel = new Label(timeLabelsComposite, SWT.None);
        dayLabel.setText("Day");
        dayLabel.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        Label hourLabel = new Label(timeLabelsComposite, SWT.None);
        hourLabel.setText("Hour");
        hourLabel.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        yearCmb = new CCombo(timeLabelsComposite, SWT.DROP_DOWN);
        yearCmb.add(String.valueOf(currYear - 1));
        yearCmb.add(String.valueOf(currYear));
        yearCmb.add(String.valueOf(currYear + 1));
        yearCmb.setEditable(false);
        yearCmb.select(1);
        yearCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        yearCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currYear = Integer.parseInt(yearCmb.getText());
                updateStormNumber();
            }
        });

        monthCmb = new CCombo(timeLabelsComposite, SWT.DROP_DOWN);
        for (int months = 1; months < 13; months++) {
            monthCmb.add(String.valueOf(months));
        }
        monthCmb.select(Calendar.getInstance().get(Calendar.MONTH));
        monthCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        monthCmb.setEditable(false);
        ModifyListener dayChangeListener = e -> populateDay();
        monthCmb.addModifyListener(dayChangeListener);
        yearCmb.addModifyListener(dayChangeListener);
        dayCmb = new CCombo(timeLabelsComposite, SWT.DROP_DOWN);

        populateDay();

        dayCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
        dayCmb.setEditable(false);

        hourCmb = new CCombo(timeLabelsComposite, SWT.DROP_DOWN);
        for (String entry : hourValues) {
            hourCmb.add(entry);
        }
        hourCmb.select(0);
        hourCmb.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));
        hourCmb.setEditable(false);
    }

    /**
     * Creates the options for storm latitude, longitude, and intensity
     * 
     * @param top
     */
    private void createStormLatLon(Composite top) {
        Composite stormLatLonComposite = new Composite(top, SWT.NONE);
        GridLayout latLonGridLayout = new GridLayout(1, false);
        latLonGridLayout.marginTop = 30;
        stormLatLonComposite.setLayout(latLonGridLayout);
        stormLatLonComposite
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label dateTime = new Label(stormLatLonComposite, SWT.NONE);
        dateTime.setText("Starting Lat/Lon and Intensity");
        dateTime.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Composite groupComposite = new Composite(stormLatLonComposite,
                SWT.NONE);
        GridLayout groupGridLayout = new GridLayout(1, true);
        groupComposite.setLayout(groupGridLayout);
        groupComposite
                .setLayoutData(new GridData(SWT.NONE, SWT.FILL, true, true));

        Group latLonGroup = new Group(groupComposite, SWT.NONE);
        GridLayout latLonGroupGridLayout = new GridLayout(6, false);
        latLonGroupGridLayout.horizontalSpacing = 10;
        latLonGroupGridLayout.marginWidth = 10;
        latLonGroup.setLayout(latLonGroupGridLayout);
        latLonGroup.setLayoutData(new GridData(SWT.NONE, SWT.FILL, true, true));

        Label latLabel = new Label(latLonGroup, SWT.None);
        GridData latGridData = new GridData();
        latGridData.horizontalSpan = 2;
        latLabel.setText("Latitude");
        latLabel.setLayoutData(latGridData);

        GridData lonGridData = new GridData();
        lonGridData.horizontalSpan = 2;
        Label lonLabel = new Label(latLonGroup, SWT.None);
        lonLabel.setText("Longitude");
        lonLabel.setLayoutData(lonGridData);

        GridData intGridData = new GridData();
        intGridData.horizontalSpan = 2;
        Label intensityLabel = new Label(latLonGroup, SWT.None);
        intensityLabel.setText("Intensity");
        intensityLabel.setLayoutData(intGridData);

        latText = new Text(latLonGroup, SWT.BORDER);
        GridData latTextGridData = new GridData();
        latTextGridData.grabExcessHorizontalSpace = true;
        latText.setLayoutData(latTextGridData);

        latText.addListener(SWT.Verify, verifyListeners.getLatVerifyListener());

        Group latGroup = new Group(latLonGroup, SWT.NONE);
        latGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        Button latNorthButton = new Button(latGroup, SWT.RADIO);
        latNorthButton.setSelection(true);
        latNorthButton.setText("N");

        latSouthButton = new Button(latGroup, SWT.RADIO);
        latSouthButton.setText("S");

        GridData lonTextGridData = new GridData();
        lonTextGridData.grabExcessHorizontalSpace = true;
        lonText = new Text(latLonGroup, SWT.BORDER);
        lonText.setLayoutData(lonTextGridData);

        lonText.addListener(SWT.Verify, verifyListeners.getLonVerifyListener());

        Group lonGroup = new Group(latLonGroup, SWT.NONE);
        lonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));

        Button lonEastButton = new Button(lonGroup, SWT.RADIO);
        lonEastButton.setText("E");

        lonWestButton = new Button(lonGroup, SWT.RADIO);
        lonWestButton.setText("W");
        lonWestButton.setSelection(true);

        GridData intenTextGridData = new GridData();
        intenTextGridData.grabExcessHorizontalSpace = true;
        intensityText = new Text(latLonGroup, SWT.BORDER);
        intensityText.setLayoutData(intenTextGridData);

        intensityText.addListener(SWT.Verify,
                verifyListeners.getWindTextVerifyListener());

        Label knots = new Label(latLonGroup, SWT.NONE);
        knots.setText("knots");

    }

    /**
     * Creates the Create and Cancel buttons.
     * 
     * @param top
     */
    protected void createControlButtons(Composite top) {
        GridLayout okCancelGridLayout = new GridLayout(2, true);
        okCancelGridLayout.marginWidth = 130;
        okCancelGridLayout.marginTop = 30;
        Composite okCancelComposite = new Composite(top, SWT.NONE);
        okCancelComposite.setLayout(okCancelGridLayout);

        Button createButton = new Button(okCancelComposite, SWT.NONE);
        GridData saveGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        saveGridData.minimumWidth = 150;
        createButton.setLayoutData(saveGridData);
        createButton.setToolTipText("Save the new storm");
        createButton.setText("Create");
        createButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveStorm();
            }
        });

        Button cancelButton = new Button(okCancelComposite, SWT.NONE);
        GridData cancelGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        cancelButton.setLayoutData(cancelGridData);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /*
     * Creates new BDeckRecord(s) based one user input.
     * 
     * @return List<BDeckRecord>
     */
    private ArrayList<BDeckRecord> createNewBDeckRecord() {

        BDeckRecord bDeckRecord = new BDeckRecord();

        bDeckRecord.setRadWind(34);

        // Basin, sub-basin, storm number, and storm name
        String subg = subBasinCmb.getText();
        bDeckRecord.setBasin(AtcfSubregion.getBasinNameByDesc(subg));
        bDeckRecord.setSubRegion(AtcfSubregion.getNameByDesc(subg));

        int stormNum = AtcfVizUtil.getStringAsInt(stormNumberCmb.getText(),
                false, true, 90);
        bDeckRecord.setCycloneNum(stormNum);

        bDeckRecord.setStormName(stormNameText.getText());

        // Latitude & longitude
        float tempLat = AtcfVizUtil.getStringAsFloat(latText.getText(),
                latSouthButton.getSelection(), true, 0.0F);
        bDeckRecord.setClat(tempLat);

        float tempLon = AtcfVizUtil.getStringAsFloat(lonText.getText(),
                lonWestButton.getSelection(), true, 0.0F);
        bDeckRecord.setClon(tempLon);

        bDeckRecord.setYear(Integer.parseInt(yearCmb.getText()));

        // Max wind and intensity
        float maxWind = AtcfVizUtil.getStringAsFloat(intensityText.getText(),
                false, true, 0.0F);
        bDeckRecord.setWindMax(maxWind);
        bDeckRecord.setIntensity(StormDevelopment.getIntensity(maxWind));

        // Storm reference time
        bDeckRecord.setRefTime(Date.from(LocalDateTime
                .of(Integer.parseInt(yearCmb.getText()),
                        Integer.parseInt(monthCmb.getText()),
                        Integer.parseInt(dayCmb.getText()),
                        Integer.parseInt(hourCmb.getText()), 0)
                .toInstant(ZoneOffset.UTC)));

        ArrayList<BDeckRecord> recs = new ArrayList<>();
        recs.add((BDeckRecord) bDeckRecord);

        // Add records for 50KT and 64KT, if needed.
        if (maxWind >= 50) {
            BDeckRecord rec50 = new BDeckRecord(bDeckRecord);
            rec50.setRadWind(50);
            recs.add(rec50);

            if (maxWind >= 64) {
                BDeckRecord rec64 = new BDeckRecord(bDeckRecord);
                rec64.setRadWind(64);
                recs.add(rec64);
            }
        }

        return recs;
    }

    /*
     * Save the new storm to the storm table
     */
    private void saveStorm() {

        // Check if all fields are valid.
        String verifyMsg = getVerificationMessage();
        boolean canSave = verifyMsg.isEmpty();

        // Save.
        if (canSave) {
            Storm storm = new Storm();

            storm.setStormName(stormNameText.getText());
            storm.setStormId(stormIdText);
            storm.setStormState("METWATCH");
            String desc = subBasinCmb.getText();
            storm.setSubRegion(AtcfSubregion.getNameByDesc(desc));
            int num = stormNumberCmb.getSelectionIndex() + 1;
            Calendar start = Calendar.getInstance();
            start.clear(Calendar.ZONE_OFFSET);
            start.set(Calendar.YEAR, Integer.parseInt(yearCmb.getText()));
            start.set(Calendar.MONTH, Integer.parseInt(monthCmb.getText()) - 1);
            start.set(Calendar.DAY_OF_MONTH,
                    Integer.parseInt(dayCmb.getText()));
            start.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(hourCmb.getText()));
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            storm.setStartDTG(start);
            storm.setCycloneNum(num);
            storm.setYear(Integer.parseInt(yearCmb.getText()));
            storm.setRegion(AtcfSubregion.getBasinNameByDesc(desc));

            NewStormRequest request = new NewStormRequest();
            request.setNewStorm(storm);

            ArrayList<BDeckRecord> tempRecord = createNewBDeckRecord();

            if (!tempRecord.isEmpty()) {
                request.setBdeckRecord(tempRecord);
                updateStormID();

                try {
                    ThriftClient.sendRequest(request);
                    AtcfDataUtil.updateStormList(storm, false);

                    MessageBox successDialog = new MessageBox(shell,
                            SWT.ICON_QUESTION | SWT.OK);
                    successDialog.setText("Alert");
                    successDialog.setMessage("Storm " + stormIdText
                            + " has been created sucessfully.");
                    successDialog.open();

                    close();
                } catch (Exception e) {
                    logger.warn("AtcfDataUtil - Adding New Storm Failed", e);
                }

            }
        } else {
            MessageBox errorDialog = new MessageBox(shell,
                    SWT.ICON_QUESTION | SWT.OK);
            errorDialog.setText("Alert");

            String errorString = "Please fix the following fields before saving :\n";
            errorString += verifyMsg;
            errorDialog.setMessage(errorString);

            errorDialog.open();
        }

    }

    /*
     * Creates storm ID based on storm name, number, and sub basin
     */
    private void updateStormID() {
        String bsn = AtcfSubregion
                .getBasinNameByDesc(subRegion.getDescription());
        stormIdText = bsn + stormNumberCmb.getText() + yearCmb.getText();
        stormIdLabel.setText("ATCF ID =           " + stormIdText);
    }

    /*
     * Update storm number to next unused TEST one for the selected basin and
     * year.
     */
    private void updateStormNumber() {
        String bsn = AtcfSubregion
                .getBasinNameByDesc(subRegion.getDescription());
        int stNum = AtcfDataUtil.getNextUnusedStormNumber(bsn, currYear);
        String numText = String.format("%02d", stNum);

        stormNumberCmb.setText(numText);

        updateStormID();
    }

    /*
     * Verifies the validity of the latitude text field for saving
     * 
     * @return validity
     */
    private boolean verifyLatField() {
        boolean valid = true;
        float temp = 0;
        try {
            temp = Float.parseFloat(latText.getText());
        } catch (Exception ex) {
            logger.warn("StartStormDialog - Invalid lat.", ex);
            return false;
        }
        if (Math.abs(temp) > 90) {
            valid = false;
        }

        return valid;
    }

    /*
     * Verifies the validity of the longitude text field for saving
     * 
     * @return validity
     */
    private boolean verifyLonField() {
        boolean valid = true;
        float temp = 0;
        try {
            temp = Float.parseFloat(lonText.getText());
        } catch (Exception ex) {
            logger.warn("StartStormDialog - Invalid lat.", ex);
            return false;
        }

        if (Math.abs(temp) > 180) {
            valid = false;
        }

        return valid;
    }

    /*
     * Verifies the validity of the Intensity field for saving
     * 
     * @return validity
     */
    private boolean verifyIntensityField() {
        boolean valid = false;

        float intensity = AtcfVizUtil.getStringAsFloat(intensityText.getText(),
                false, true, 0.0F);
        if (intensity > 0) {
            valid = true;
        }

        return valid;
    }

    /*
     * Populates the Day field based on the selected month
     * 
     * @return valid
     */
    private void populateDay() {

        int selected;

        // If this is the first time the list is created, set select to the
        // current day. if not, save what was previously selected.
        if (dayCmb.getSelectionIndex() != -1) {
            selected = dayCmb.getSelectionIndex();
        } else {
            selected = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
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

    /*
     * Verifies name field
     * 
     * @return valid
     */
    private boolean validStormName() {
        boolean isValid = true;
        String text = stormNameText.getText();

        /*
         * TODO if the length is over 10, it will be truncated and use the storm
         * name lookup table
         */
        if (text.isEmpty() || text.length() > STORM_NAME_LENGTH) {
            return false;
        }

        return isValid;
    }

    /*
     * Verify all user input.
     */
    private String getVerificationMessage() {

        StringBuilder msgBuilder = new StringBuilder();

        // Check if all fields are valid.
        boolean stormUsed = AtcfDataUtil.isCycloneNumUsed(
                stormIdText.substring(0, 2),
                Integer.parseInt(yearCmb.getText()),
                Integer.parseInt(stormNumberCmb.getText()));

        boolean validStormName = validStormName();

        boolean stormNameUsed = false;
        if (validStormName) {
            stormNameUsed = AtcfDataUtil.isStormNameUsed(
                    stormIdText.substring(0, 2),
                    Integer.parseInt(yearCmb.getText()),
                    stormNameText.getText());
        }

        boolean validLatField = verifyLatField();
        boolean validLonField = verifyLonField();
        boolean validIntenityField = verifyIntensityField();

        msgBuilder.append(!stormUsed ? ""
                : "\nStorm " + stormIdText + " already exists.");

        msgBuilder
                .append(validStormName ? "" : "\nStorm name field is invalid");
        if (validStormName) {
            msgBuilder.append(!stormNameUsed ? ""
                    : "\nThe storm name has been used for "
                            + subBasinCmb.getText() + " in "
                            + yearCmb.getText());
        }

        msgBuilder.append(validLatField ? "" : "\nLatitude field is invalid");
        msgBuilder.append(validLonField ? "" : "\nLongitude field is invalid");
        msgBuilder.append(
                validIntenityField ? "" : "\nIntensity field is invalid");

        return msgBuilder.toString();
    }
}
