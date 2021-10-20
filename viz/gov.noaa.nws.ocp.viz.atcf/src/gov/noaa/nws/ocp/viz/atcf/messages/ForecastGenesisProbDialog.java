/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.messages;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisEDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.UpdateGenesisEDeckRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for TC Genesis probability forecast
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 21, 2021 #90624      jnengel     Initial creation.
 * Jun 21, 2021 #91551      jnengel     Implemented backend.
 * Jul 01, 2021 #93662      jnengel     Fixed issue with record duplication.
 *
 * </pre>
 *
 * @author jnengel
 *
 */

public class ForecastGenesisProbDialog extends OcpCaveSWTDialog {

    private static final int GENESIS_DISPLAY_TIME_LIMIT = 168;

    // Enum representing forecast time, in hours, for genesis probability.
    public enum GenesisProbTime {
        TWO_DAY(48),

        FIVE_DAY(120),

        SEVEN_DAY(168);

        private int hour;

        private GenesisProbTime(int hour) {
            this.hour = hour;
        }

        public int getHour() {
            return hour;
        }
    }

    private AtcfBasin initialBasin;

    private Composite genesisComp;

    private CCombo basinCmb;

    private CCombo forecasterCmb;

    private Map<Genesis, GenEntry> genRecords;

    private String synTime;

    private String prevBasin;

    private final String[] probEntries = AtcfVizUtil.getEntries(100, 10, "%d");

    private boolean warnUserBeforeSwap = false;

    /**
     * Constructor
     *
     * @param shell
     * @param basin
     *            Which basin the menu will initialize to. Is "AL" when called
     *            from the forecast menu, and "AL", "EP", or "CP" from the
     *            relevant messages menu.
     */
    public ForecastGenesisProbDialog(Shell shell, AtcfBasin basin) {
        super(shell, SWT.DIALOG_TRIM, OCP_DIALOG_CAVE_STYLE);
        setText("TC Genesis Probabilities");
        initialBasin = basin;
    }

    /**
     * Builds the window.
     */
    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComp = new Composite(shell, SWT.RESIZE);
        GridData mainCompGD = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
                1);
        mainComp.setLayoutData(mainCompGD);
        GridLayout mainCompGL = new GridLayout(1, false);
        mainCompGL.marginHeight = 5;
        mainCompGL.marginWidth = 5;
        mainComp.setLayout(mainCompGL);

        createBasinCmb(mainComp);
        createGenesisComp(mainComp);
        createForecasterCombo(mainComp);
        createControlBtns(mainComp);

        basinCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (prevBasin.equals(basinCmb.getText())) {
                    // User selected the same basin. Do nothing.
                } else if (warnUserBeforeSwap) {
                    String warnMsg = "Changes are not saved when swapping basins. Save now?";
                    MessageDialog confirmDlg = new MessageDialog(shell,
                            "Alert!", null, warnMsg, MessageDialog.CONFIRM,
                            new String[] { "Yes", "No" }, 1);
                    confirmDlg.open();

                    if (confirmDlg.getReturnCode() == Window.OK) {
                        updateGenesisEDeck();
                    }
                    swapBasins(basinCmb.getText());
                    warnUserBeforeSwap = false;
                } else {
                    swapBasins(basinCmb.getText());
                }
            }
        });

        AtcfBasin[] basins = AtcfBasin.values();
        for (int i = 0; i < basins.length; i++) {
            if (initialBasin == basins[i]) {
                basinCmb.select(i);
                break;
            }
        }

        swapBasins(basinCmb.getText());

    }

    /**
     * Builds the bar for selecting the basin. The time is the closest synoptic
     * time to the current UTC date. See AtcfVizUtil.getSnyopticTime().
     *
     * @param parent
     *            The composite to add the objects to
     */
    private void createBasinCmb(Composite parent) {
        Composite selectionRowComp = new Composite(parent, SWT.NONE);
        selectionRowComp.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        selectionRowComp.setLayout(new GridLayout(3, false));

        Label basinLbl = new Label(selectionRowComp, SWT.NONE);
        basinLbl.setAlignment(SWT.CENTER);
        GridData basinLblGD = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        basinLblGD.widthHint = 80;
        basinLbl.setLayoutData(basinLblGD);
        basinLbl.setText("Basin:");

        basinCmb = new CCombo(selectionRowComp, SWT.BORDER);
        GridData basinCmbGD = new GridData(SWT.LEFT, SWT.CENTER, false, false,
                1, 1);
        basinCmbGD.widthHint = 220;
        basinCmb.setLayoutData(basinCmbGD);
        basinCmb.setItems(AtcfBasin.getDescriptions());
        basinCmb.setEditable(false);

        Label dateLbl = new Label(selectionRowComp, SWT.NONE);
        dateLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));

        synTime = AtcfDataUtil.getClosestSynopticTime();
        dateLbl.setText(synTime);

    }

    /**
     * Initializes the group containing the genesis menu options
     *
     * @param parent
     *            The composite to add the box to
     */
    private void createGenesisComp(Composite parent) {
        Composite genesisDisplayComp = new Composite(parent, SWT.NONE);
        genesisDisplayComp.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        genesisDisplayComp.setLayout(new GridLayout(1, false));
        Composite genesisLabelsComp = new Composite(genesisDisplayComp,
                SWT.NONE);
        GridLayout genesisLabelsGL = new GridLayout(6, true);
        genesisLabelsGL.horizontalSpacing = 10;
        genesisLabelsComp.setLayout(genesisLabelsGL);
        GridData genesisLabelsGD = new GridData(SWT.FILL, SWT.TOP, true, false,
                1, 1);
        genesisLabelsComp.setLayoutData(genesisLabelsGD);

        for (String labelText : new String[] { "", "Genesis #", "Storm ID",
                "2 Day", "5 Day", "7 Day" }) {
            boolean left = "Genesis #".equals(labelText)
                    || "Storm ID".equals(labelText);
            Label lbl = new Label(genesisLabelsComp, SWT.CENTER);
            lbl.setLayoutData(new GridData(left ? SWT.LEFT : SWT.CENTER,
                    SWT.CENTER, true, false, 1, 1));
            lbl.setAlignment(SWT.CENTER);
            lbl.setText(labelText);
        }

        ScrolledComposite scrollComp = new ScrolledComposite(genesisDisplayComp,
                SWT.V_SCROLL | SWT.BORDER);
        GridData scrollCompGD = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
                1);
        scrollCompGD.heightHint = 275;
        scrollComp.setLayoutData(scrollCompGD);

        genesisComp = new Composite(scrollComp, SWT.NONE);
        genesisComp.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        GridLayout genCompGL = new GridLayout(1, false);
        genCompGL.marginWidth = 0;
        genCompGL.horizontalSpacing = 0;
        genesisComp.setLayout(genCompGL);

        scrollComp.setContent(genesisComp);
        scrollComp.setExpandHorizontal(true);
        scrollComp.setExpandVertical(true);

        /*
         * Sets the point where the scroll-bar appears dynamically, so it only
         * appears when the scroll composite can't display everything
         */
        scrollComp.addListener(SWT.Resize, event -> {
            int width = genesisComp.getClientArea().width;
            scrollComp.setMinSize(genesisComp.computeSize(width, SWT.DEFAULT));
        });

    }

    /**
     * Creates the combo for selecting the forecaster.
     *
     * @param parent
     *            The composite to add the objects to
     */
    private void createForecasterCombo(Composite parent) {
        Composite forecasterRowComp = new Composite(parent, SWT.NONE);
        forecasterRowComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
        forecasterRowComp.setLayout(new GridLayout(2, false));

        Label forecasterLbl = new Label(forecasterRowComp, SWT.NONE);
        forecasterLbl.setAlignment(SWT.CENTER);
        GridData forecasterLblGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false, 1, 1);
        forecasterLblGD.widthHint = 80;
        forecasterLbl.setLayoutData(forecasterLblGD);
        forecasterLbl.setText("Forecaster:");

        forecasterCmb = new CCombo(forecasterRowComp, SWT.BORDER);
        GridData forecasterCmbGD = new GridData(SWT.CENTER, SWT.BOTTOM, false,
                true, 1, 1);
        forecasterCmbGD.widthHint = 220;
        forecasterCmb.setLayoutData(forecasterCmbGD);
        forecasterCmb.setItems(
                AtcfConfigurationManager.getInstance().getForecasterInitials()
                        .getInitials().toArray(new String[] {}));
        forecasterCmb.setEditable(false);
        forecasterCmb.select(0);
    }

    /**
     * Creates the Help, Save, Submit, and Cancel buttons.
     *
     * @param parent
     *            The composite to add the buttons to
     */
    private void createControlBtns(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridData buttonGD = new GridData(SWT.CENTER, SWT.BOTTOM, false, false,
                1, 1);
        buttonGD.widthHint = 340;
        buttonComp.setLayoutData(buttonGD);
        GridLayout buttonGroupGL = new GridLayout(4, true);
        buttonGroupGL.horizontalSpacing = 10;
        buttonComp.setLayout(buttonGroupGL);

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        Button saveBtn = new Button(buttonComp, SWT.NONE);
        saveBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        saveBtn.setText("Save");

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkRecords()) {
                    updateGenesisEDeck();
                    refreshNewRecs();
                    warnUserBeforeSwap = false;
                }
            }
        });

        Button submitBtn = new Button(buttonComp, SWT.NONE);
        submitBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        submitBtn.setText("Submit");
        submitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkRecords()) {
                    updateGenesisEDeck();
                    close();
                }
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.NONE);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Pulls newly-created and saved records back from database and set them
     * into local "genRecords" for any future update.
     */
    private void refreshNewRecs() {
        for (Map.Entry<Genesis, GenEntry> entry : genRecords.entrySet()) {
            if (entry.getValue().records.get(synTime) == null) {

                entry.getValue().setRecords(
                        AtcfDataUtil.getGenesisEDeckRecordMap(entry.getKey()));
            }
        }
    }

    /**
     * Populates the tool line with the genesis in the given basin.
     *
     * @param basinDescription
     *            The string in the basin selection combo, used to identify the
     *            basin itself
     */
    private void swapBasins(String basinDescription) {
        for (Control control : genesisComp.getChildren()) {
            control.dispose();
        }

        List<Genesis> genList = AtcfDataUtil.getGenesisBasinMap()
                .get(AtcfBasin.getBasinbyDesc(basinDescription).toString());

        genRecords = new IdentityHashMap<>();

        if (genList != null) {
            for (Genesis gen : genList) {
                genRecords.put(gen, new GenEntry(
                        AtcfDataUtil.getGenesisEDeckRecordMap(gen)));

                Composite genesisRowComp = new Composite(genesisComp, SWT.NONE);
                GridLayout genesisRowGL = new GridLayout(1, false);
                genesisRowGL.marginWidth = 0;
                genesisRowGL.marginHeight = 0;
                genesisRowComp.setLayout(genesisRowGL);
                genesisRowComp.setLayoutData(
                        new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

                Composite genesisEntryComp = new Composite(genesisRowComp,
                        SWT.NONE);
                genesisEntryComp.setLayoutData(
                        new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
                genesisEntryComp.setLayout(new GridLayout(6, true));

                Button genCheckbox = new Button(genesisEntryComp,
                        SWT.CHECK | SWT.CENTER);
                genCheckbox.setLayoutData(
                        new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
                genCheckbox.setAlignment(SWT.LEFT);
                genCheckbox.setSelection(true);
                genCheckbox.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        genRecords.get(gen).checked = genCheckbox
                                .getSelection();
                    }
                });

                Label genNumLbl = new Label(genesisEntryComp, SWT.NONE);
                genNumLbl.setLayoutData(
                        new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
                genNumLbl.setText(Integer.toString(gen.getGenesisNum()));

                Label stormIdLbl = new Label(genesisEntryComp, SWT.NONE);
                stormIdLbl.setLayoutData(
                        new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
                stormIdLbl.setText(Integer.toString(gen.getCycloneNum()) + " "
                        + gen.getYear());

                buildOneGenComp(genesisEntryComp, gen);

            }
        } else {
            Label noActivesLbl = new Label(genesisComp, SWT.NONE);
            noActivesLbl.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
            noActivesLbl.setText("No active genesis for this basin.");
        }

        prevBasin = basinCmb.getText();
        genesisComp.layout(true);

    }

    /**
     * Builds one row of the Genesis forecast table
     *
     * @param comp
     *            Parent comp
     * @param gen
     *            Genesis to build
     */
    private void buildOneGenComp(Composite comp, Genesis gen) {
        for (GenesisProbTime hour : GenesisProbTime.values()) {
            Combo comb = new Combo(comp, SWT.NONE);

            comb.setItems(probEntries);
            GridData combGD = new GridData(SWT.CENTER, SWT.CENTER, true, true,
                    1, 1);
            combGD.widthHint = 60;
            comb.setLayoutData(combGD);
            comb.setText(Integer.toString(genRecords.get(gen).getProb(hour)));

            comb.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int combProb = Integer.parseInt(comb.getText());
                    if (combProb != genRecords.get(gen).getProb(hour)) {
                        warnUserBeforeSwap = true;
                        genRecords.get(gen).setProb(hour, combProb);
                        genRecords.get(gen).shouldBeSaved = true;
                    }
                }
            });

        }
    }

    /**
     * Checks for if there are any records out of order, and build a warning
     * dialog if they are
     */
    private boolean checkRecords() {
        boolean valid = true;
        StringBuilder errs = new StringBuilder("");

        for (Map.Entry<Genesis, GenEntry> entry : genRecords.entrySet()) {
            GenEntry info = entry.getValue();
            Genesis gen = entry.getKey();
            if (info.prob2 > info.prob5) {
                valid = false;
                errs.append("Genesis " + gen.getGenesisNum() + ": 2-day prob "
                        + info.prob2 + " > 5-day prob " + info.prob5 + "\n");
            }
            if (info.prob5 > info.prob7) {
                valid = false;
                errs.append("Genesis " + gen.getGenesisNum() + ": 5-day prob "
                        + info.prob5 + " > 7-day prob " + info.prob7 + "\n");
            }
        }
        if (!valid) {
            String failMsg = "Probabilities have not been save - entries must increase over time.\n\nPlease correct the following errors before saving.\n\n";
            MessageBox failureDialog = new MessageBox(shell,
                    SWT.ICON_WARNING | SWT.OK);
            failureDialog.setText("Alert");
            failureDialog.setMessage(failMsg + errs.toString());
            failureDialog.open();
        }
        return valid;
    }

    /**
     * Determines records to update, gathers the data and sends it to a handler
     * for updating
     */
    private void updateGenesisEDeck() {
        UpdateGenesisEDeckRequest newRequest = new UpdateGenesisEDeckRequest();

        for (Map.Entry<Genesis, GenEntry> entry : genRecords.entrySet()) {
            Genesis gen = entry.getKey();
            GenEntry info = entry.getValue();
            if (!info.checked
                    || (!info.shouldBeSaved && !info.records.isEmpty())) {
                continue;
            }

            for (GenesisProbTime hour : GenesisProbTime.values()) {
                updateRecord(newRequest, hour, info, gen);
            }
        }

        boolean success = false;
        try {
            success = (boolean) ThriftClient.sendRequest(newRequest);
        } catch (VizException e) {
            logger.error("TC Genesis Forecaster Dialog - Update edeck failed: ",
                    e);
        }

        if (!success) {
            MessageBox successDialog = new MessageBox(shell,
                    SWT.ICON_INFORMATION | SWT.ICON_WARNING);
            successDialog.setText("Alert");
            successDialog.setMessage(
                    "ForecastGenssisProbDialog - failed to save genesis EDeck records.");
            successDialog.open();
        }
    }

    /**
     * Update genesis EDeck record data and set into request for updating.
     *
     * @param newRequest
     *
     * @param time
     *
     * @param info
     *
     * @param gen
     */
    private void updateRecord(UpdateGenesisEDeckRequest newRequest,
            GenesisProbTime hour, GenEntry info, Genesis gen) {

        GenesisEDeckRecord recToUpdate = null;
        for (GenesisEDeckRecord rec : info.records.getOrDefault(synTime,
                new ArrayList<>())) {
            if (rec.getFcstHour() == hour.getHour()) {
                recToUpdate = rec;
                recToUpdate.setForecaster(forecasterCmb.getText());
                recToUpdate.setProbability(info.getProb(hour));
            }
        }

        if (recToUpdate == null) {
            recToUpdate = newGenEDeck(gen, info, hour);
        }

        ModifiedDeckRecord mrec = new ModifiedDeckRecord();
        mrec.setEditType(RecordEditType.MODIFY);
        mrec.setRecord(recToUpdate);
        newRequest.addRecord(mrec);
    }

    /**
     * Constructs a single new GenesisEDeckRecord from the given data
     *
     * @param gen
     *            The Genesis
     * @param entry
     *            A subclass containing info taken from the GUI
     * @param probHours
     *            Whether it's for 48, 120, or 168 hour forecast
     * @return The new record
     */
    private GenesisEDeckRecord newGenEDeck(Genesis gen, GenEntry entry,
            GenesisProbTime hour) {
        GenesisEDeckRecord rec = new GenesisEDeckRecord();

        Date refTime = AtcfDataUtil
                .parseDtg(AtcfDataUtil.getClosestSynopticTime());

        rec.setBasin(AtcfBasin.getBasinbyDesc(basinCmb.getText()).toString());
        rec.setCycloneNum(gen.getCycloneNum());
        rec.setGenesisNum(gen.getGenesisNum());
        rec.setRefTime(refTime);
        rec.setProbFormat("GN");
        rec.setTechnique("OFCL");
        rec.setFcstHour(hour.getHour());
        rec.setProbability(entry.getProb(hour));
        rec.setProbabilityItem(hour.getHour());
        rec.setForecaster(forecasterCmb.getText());

        rec.setYear(refTime.toInstant().atOffset(ZoneOffset.UTC).getYear());

        return rec;
    }

    /**
     * A subclass for holding info on each Genesis, including info from the GUI
     * and whether it's been modified
     */
    private class GenEntry {
        private Map<String, List<GenesisEDeckRecord>> records;

        private boolean shouldBeSaved;

        private boolean checked;

        private int prob2;

        private int prob5;

        private int prob7;

        GenEntry(Map<String, List<GenesisEDeckRecord>> entries) {
            shouldBeSaved = false;
            checked = true;
            if (entries != null) {
                records = entries;
                prob2 = getMostRecentProb(records, GenesisProbTime.TWO_DAY);
                prob5 = getMostRecentProb(records, GenesisProbTime.FIVE_DAY);
                prob7 = getMostRecentProb(records, GenesisProbTime.SEVEN_DAY);
            } else {
                entries = new LinkedHashMap<>();
                prob2 = 0;
                prob5 = 0;
                prob7 = 0;
            }
        }

        /**
         *
         * @param prob
         * @param hour
         */
        void setProb(GenesisProbTime hour, int prob) {
            if (hour == GenesisProbTime.TWO_DAY) {
                prob2 = prob;
            } else if (hour == GenesisProbTime.FIVE_DAY) {
                prob5 = prob;
            } else if (hour == GenesisProbTime.SEVEN_DAY) {
                prob7 = prob;
            }
        }

        int getProb(GenesisProbTime hour) {
            if (hour == GenesisProbTime.TWO_DAY) {
                return prob2;
            } else if (hour == GenesisProbTime.FIVE_DAY) {
                return prob5;
            } else if (hour == GenesisProbTime.SEVEN_DAY) {
                return prob7;
            } else {
                return -1;
            }
        }

        void setRecords(Map<String, List<GenesisEDeckRecord>> records) {
            this.records = records;
        }

        /**
         * Retrieve the most recent probability entry by checking for the
         * existence of a GenesisEDeck Record at each synoptic time, starting
         * with the current time.
         *
         * @param allRecords
         *            Map of GenesisEDeckRecords, sorted by time.
         * @param forecastHour
         *            48, 120, or 168 hours.
         * @return
         */
        int getMostRecentProb(Map<String, List<GenesisEDeckRecord>> allRecords,
                GenesisProbTime hour) {
            int offset = 0;
            while (offset < GENESIS_DISPLAY_TIME_LIMIT) {
                List<GenesisEDeckRecord> recsByTime = allRecords.getOrDefault(
                        AtcfDataUtil.getNewDTG(synTime, -1 * offset),
                        new ArrayList<>());
                for (GenesisEDeckRecord rec : recsByTime) {
                    if (rec.getFcstHour() == hour.getHour()) {
                        return (int) rec.getProbability();
                    }
                }
                offset += 6;
            }
            return 0;
        }

    }

}