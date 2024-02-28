/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.StormStateEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.StormStates;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryInfo;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Advisory Composition=>Forecast Type".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2020 82721      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class AdvisoryForecastTypeDialog extends OcpCaveSWTDialog {

    private StormStates stmStates = AtcfConfigurationManager.getInstance()
            .getStormStates().getAvailableStormStates();

    // List of AtcfTaus usable for storm state.
    private java.util.List<AtcfTaus> taus;

    // Number of visible items in tau and forecast type list.
    private static final int FCST_TYPE_ITEMS = 6;

    // Advisory information from Advisory Composition dialog.
    private AdvisoryInfo advInfo;

    // Current storm.
    private Storm storm;

    // List of the forecast hours.
    private List tauList;

    // List of the forecast hours.
    private java.util.List<Button> stateBtns;

    // List for all selected forecast hour/storm state pair.
    private List fcstTypeList;

    // Currently selected TAU.
    private AtcfTaus currentTau;

    // Currently selected storm state.
    private StormStateEntry currentState;

    // Current selections of storm states.
    private Map<AtcfTaus, StormStateEntry> selectedStates;

    /**
     * Constructor
     *
     * @param shell
     */
    public AdvisoryForecastTypeDialog(Shell shell,
            final java.util.List<AtcfTaus> taus, AdvisoryInfo advInfo) {

        super(shell);

        this.storm = AtcfSession.getInstance().getActiveStorm();

        this.taus = taus;

        this.advInfo = advInfo;

        this.selectedStates = new LinkedHashMap<>();

        setText("Advisory Forecast Type - " + this.storm.getStormName() + " "
                + this.storm.getStormId());
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
     * Create contents.
     */
    protected void createContents() {

        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 8;
        mainLayout.marginWidth = 10;
        mainLayout.horizontalSpacing = 25;
        mainLayout.verticalSpacing = 15;
        mainComposite.setLayout(mainLayout);
        GridData mainGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        mainComposite.setLayoutData(mainGD);

        createMainArea(mainComposite);

        createControlButtons(mainComposite);
    }

    /**
     * Creates the main area of the GUI
     *
     * @param parent
     */
    protected void createMainArea(Composite parent) {

        Font txtFont = JFaceResources.getTextFont();

        Group tauGrp = new Group(parent, SWT.NONE);
        GridLayout tauGrpLayout = new GridLayout(3, false);
        tauGrpLayout.marginHeight = 8;
        tauGrpLayout.marginWidth = 10;
        tauGrpLayout.horizontalSpacing = 20;
        tauGrp.setLayout(tauGrpLayout);
        GridData tauGrpGD = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        tauGrp.setLayoutData(tauGrpGD);

        // Composite to list all TAUs at left
        Composite tauListComp = new Composite(tauGrp, SWT.NONE);
        GridLayout tauListLayout = new GridLayout(1, true);
        tauListComp.setLayout(tauListLayout);
        tauListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label tauLabel = new Label(tauListComp, SWT.NONE);
        tauLabel.setText("TAU");

        tauList = new List(tauListComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        tauList.setFont(txtFont);
        int listHeight = FCST_TYPE_ITEMS * tauList.getItemHeight();
        GridData tauGD = new GridData(SWT.DEFAULT, listHeight);
        tauList.setLayoutData(tauGD);

        for (AtcfTaus tau : taus) {
            String tauStr = Integer.toString(tau.getValue());
            tauList.add(tauStr);
            tauList.setData(tauStr, tau);
        }

        tauList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currentTau = (AtcfTaus) tauList
                        .getData(tauList.getSelection()[0]);
                updateTaus();
            }
        });

        // Composite for the storm state buttons at middle
        Composite stateComp = new Composite(tauGrp, SWT.NONE);
        GridLayout stateCompGL = new GridLayout(1, false);
        stateComp.setLayout(stateCompGL);
        stateComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label stateLbl = new Label(stateComp, SWT.NONE);
        stateLbl.setText("Storm state:");

        Group stateGrp = new Group(stateComp, SWT.NONE);
        GridLayout stateGrpGL = new GridLayout(1, false);
        stateGrpGL.verticalSpacing = 8;
        stateGrp.setLayout(stateGrpGL);
        GridData stateGrpGD = new GridData();
        stateGrpGD.horizontalAlignment = SWT.LEFT;
        stateGrp.setLayoutData(stateGrpGD);

        stateBtns = new ArrayList<>(stmStates.getStormStates().size());
        for (StormStateEntry st : stmStates.getStormStates()) {
            Button stBtn = new Button(stateGrp, SWT.RADIO);
            stBtn.setText(st.getName());
            stBtn.setData(st);
            stBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    currentState = (StormStateEntry) e.widget.getData();
                    updateStates();
                }
            });

            stateBtns.add(stBtn);
        }

        // Composite showing selections at right.
        Composite selectionComp = new Composite(tauGrp, SWT.NONE);
        GridLayout selectionCompGL = new GridLayout(1, false);
        selectionComp.setLayout(selectionCompGL);
        selectionComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label selLbl = new Label(selectionComp, SWT.NONE);
        selLbl.setText("Selections:");

        fcstTypeList = new List(selectionComp,
                SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fcstTypeList.setFont(txtFont);
        int listHgt = FCST_TYPE_ITEMS * fcstTypeList.getItemHeight();
        GridData fcstTypeListGD = new GridData(SWT.DEFAULT, listHgt);
        fcstTypeList.setLayoutData(fcstTypeListGD);
        fcstTypeList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int ind = fcstTypeList.getSelectionIndex();
                currentTau = taus.get(ind);
                tauList.select(ind);
                updateTaus();
            }
        });

        // Initialize storm states.
        initializeStates();
    }

    /**
     * Creates the control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {

        // Composite for control buttons.
        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnGDLayout = new GridLayout(3, true);
        ctrlBtnComp.setLayout(ctrlBtnGDLayout);
        GridData ctrlBtnCompGD = AtcfVizUtil.horizontalFillGridData();
        ctrlBtnCompGD.horizontalSpan = 2;
        ctrlBtnComp.setLayoutData(ctrlBtnCompGD);

        AtcfVizUtil.createHelpButton(ctrlBtnComp, AtcfVizUtil.buttonGridData());

        Button okBtn = new Button(ctrlBtnComp, SWT.PUSH);
        okBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        okBtn.setText("Ok");
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Pass into Advisory Composition dialog.
                updateAdvInfo();
                close();
            }
        });

        Button cancelBtn = new Button(ctrlBtnComp, SWT.PUSH);
        cancelBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /*
     * Initialize the initial state for each TAU.
     */
    private void initializeStates() {

        java.util.List<String> fcstType = advInfo.getForecastType();
        java.util.List<String> tausUsed = advInfo.getTaus();

        String[] selected = new String[taus.size()];

        /*
         * For stormId.adv in new ATCF, it has TAUs saved in the same order of
         * storm state so they can match up.
         *
         * For legacy stormId.adv, it does not save TAUs. So we have to assumed
         * that the saved storm states match the TAUs one-by-one from the
         * beginning. If there are not enough storm states for all TAUs, then
         * the missing ones will be set as in default state.
         */
        int ii = 0;
        int jj = 0;
        if (!tausUsed.isEmpty()) {
            for (AtcfTaus tau : taus) {
                StormStateEntry ss = stmStates.getStormStatebyID("NO");
                int tauInd = tausUsed.indexOf(Integer.toString(tau.getValue()));
                if (tauInd >= 0) {
                    ss = stmStates.getStormStatebyID(fcstType.get(jj));
                    jj++;
                }
                selectedStates.put(tau, ss);

                selected[ii] = getTauStateString(tau, ss);

                ii++;
            }
        } else {
            for (AtcfTaus tau : taus) {
                StormStateEntry ss = stmStates.getStormStatebyID("NO");
                if (jj < fcstType.size()) {
                    ss = stmStates.getStormStatebyID(fcstType.get(jj));
                    jj++;
                }
                selectedStates.put(tau, ss);

                selected[ii] = getTauStateString(tau, ss);

                ii++;
            }
        }

        tauList.select(0);
        currentTau = taus.get(0);
        currentState = selectedStates.get(currentTau);

        updateStateBtnStatus();

        fcstTypeList.setItems(selected);
        fcstTypeList.setData(selectedStates);
        fcstTypeList.select(0);
    }

    /*
     * Updates the selections of TAU.
     */
    private void updateTaus() {
        currentState = selectedStates.get(currentTau);
        updateStateBtnStatus();
        updateSelections();
    }

    /*
     * Updates the selections of storm state for current TAU.
     */
    private void updateStates() {
        selectedStates.put(currentTau, currentState);
        updateSelections();
    }

    /*
     * Updates selected state list.
     */
    private void updateSelections() {
        int ind = taus.indexOf(currentTau);
        fcstTypeList.setItem(ind, getTauStateString(currentTau, currentState));
        fcstTypeList.select(ind);
    }

    /*
     * Updates status of storm state buttons.
     */
    private void updateStateBtnStatus() {
        for (Button btn : stateBtns) {
            btn.setSelection(currentState.equals(btn.getData()));
        }
    }

    /*
     * Formats a combo of tau & its selected storm state into a string.
     */
    private String getTauStateString(AtcfTaus tau, StormStateEntry st) {
        return String.format("tau%4d    %s", tau.getValue(), st.getName());
    }

    /*
     * Updates advisory information based on current selections.
     */
    private void updateAdvInfo() {
        java.util.List<String> fstType = advInfo.getForecastType();
        fstType.clear();

        java.util.List<String> tauStr = advInfo.getTaus();
        tauStr.clear();

        for (AtcfTaus tau : taus) {
            fstType.add(selectedStates.get(tau).getId());
            tauStr.add(Integer.toString(tau.getValue()));
        }
    }

}