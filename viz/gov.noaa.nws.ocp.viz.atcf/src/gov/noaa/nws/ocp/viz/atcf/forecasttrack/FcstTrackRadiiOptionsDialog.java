/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecasttrack;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Graphic"=>"Forecast Wind Radii Options".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 04, 2019 70868      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class FcstTrackRadiiOptionsDialog extends OcpCaveSWTDialog {

    // Map for forecast track records sorted by DTG (ref. time + forecast hour).
    private Map<String, java.util.List<ForecastTrackRecord>> fcstTrackDataMap;

    // List of the forecast hours.
    private List tauList;

    // Current storm.
    private Storm storm;

    // Current storm's AtcfResource and AtcfProduct.
    private AtcfResource atcfResource;

    private AtcfProduct prd;

    // Current storm's FcstTrackProperties.
    private FcstTrackProperties fcstProp;

    // Button to toggle on/off 34 knot wind radii for selected TAU.
    private Button kt34Btn;

    // Button to toggle on/off 50 knot wind radii for selected TAU.
    private Button kt50Btn;

    // Button to toggle on/off 64 knot wind radii for selected TAU.
    private Button kt64Btn;

    /**
     * Constructor
     *
     * @param shell
     * @param bdeckDataMap
     */
    public FcstTrackRadiiOptionsDialog(Shell shell,
            Map<String, java.util.List<ForecastTrackRecord>> fcstTrackDataMap) {

        super(shell);
        this.fcstTrackDataMap = fcstTrackDataMap;
        this.storm = AtcfSession.getInstance().getActiveStorm();

        setText("Forecast Wind Radii Options - " + this.storm.getStormName()
                + " " + this.storm.getStormId());
    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {

        // Find the current ATCF resource and product.
        atcfResource = AtcfSession.getInstance().getAtcfResource();
        prd = atcfResource.getResourceData().getAtcfProduct(storm);

        // Find the current forecast track display properties.
        fcstProp = prd.getFcstTrackProperties();
        if (fcstProp == null) {
            fcstProp = new FcstTrackProperties();
            prd.setFcstTrackProperties(fcstProp);
        }

        createContents();
    }

    /**
     * Create contents.
     */
    protected void createContents() {

        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 8;
        mainLayout.marginWidth = 10;
        mainLayout.horizontalSpacing = 20;
        mainLayout.verticalSpacing = 15;
        mainComposite.setLayout(mainLayout);
        GridData mainGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        mainComposite.setLayoutData(mainGD);

        createMainArea(mainComposite);

        createControlButtons(mainComposite);

        // Draw forecast track.
        drawRadiiOptions();
    }

    /**
     * Creates the main area of the GUI
     *
     * @param parent
     */
    protected void createMainArea(Composite parent) {

        Group tauGrp = new Group(parent, SWT.CENTER);
        GridLayout tauGrpLayout = new GridLayout(2, false);
        tauGrpLayout.marginHeight = 8;
        tauGrpLayout.marginWidth = 10;
        tauGrpLayout.horizontalSpacing = 15;
        tauGrpLayout.verticalSpacing = 40;
        tauGrp.setLayout(tauGrpLayout);
        GridData tauGrpGD = new GridData();
        tauGrpGD.horizontalAlignment = SWT.CENTER;
        tauGrp.setLayoutData(tauGrpGD);

        // Composite to list all TAUs at left
        Composite tauListComp = new Composite(tauGrp, SWT.NONE);
        GridLayout tauListLayout = new GridLayout(1, false);
        tauListComp.setLayout(tauListLayout);
        tauListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label tauLabel = new Label(tauListComp, SWT.NONE);
        tauLabel.setText("TAU");

        tauList = new List(tauListComp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData tauGD = new GridData(30, 200);
        tauList.setLayoutData(tauGD);

        for (Map.Entry<String, java.util.List<ForecastTrackRecord>> entry : fcstTrackDataMap
                .entrySet()) {
            String dtg = entry.getKey();
            java.util.List<ForecastTrackRecord> recs = entry.getValue();
            if (!recs.isEmpty()) {
                String tau = String.valueOf(recs.get(0).getFcstHour());
                tauList.add(tau);
                tauList.setData(tau, dtg);
            }
        }

        // Select all TAUs by default.
        if (tauList.getItemCount() > 0) {
            setDtgSelections();
        }

        tauList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        // Composite for the 34/50/64 toggle buttons at right
        Composite radiiComp = new Composite(tauGrp, SWT.NONE);
        GridLayout radiiCompLayout = new GridLayout(1, false);
        radiiCompLayout.verticalSpacing = 35;
        radiiComp.setLayout(radiiCompLayout);
        radiiComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label displayLabel = new Label(radiiComp, SWT.NONE);
        displayLabel.setText("Display");

        kt34Btn = new Button(radiiComp, SWT.CHECK);
        kt34Btn.setText("34 knot radii");
        kt34Btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        kt50Btn = new Button(radiiComp, SWT.CHECK);
        kt50Btn.setText("50 knot radii");
        kt50Btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        kt64Btn = new Button(radiiComp, SWT.CHECK);
        kt64Btn.setText("64 knot radii");
        kt64Btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRadiiOptions();
            }
        });

        // Set status for the toggle buttons from FcstTrackProperties.
        if (fcstProp != null) {
            kt34Btn.setSelection(fcstProp.isRadiiFor34Knot());
            kt50Btn.setSelection(fcstProp.isRadiiFor50Knot());
            kt64Btn.setSelection(fcstProp.isRadiiFor64Knot());
        }

        // Composite at right to hold "All" buttons.
        Composite dispAllComp = new Composite(parent, SWT.NONE);
        GridLayout dispAllLayout = new GridLayout(1, true);
        dispAllLayout.verticalSpacing = 45;
        dispAllComp.setLayout(dispAllLayout);
        dispAllComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, true));

        Composite allRadiiComp = new Composite(dispAllComp, SWT.NONE);
        GridLayout allRadiiLayout = new GridLayout(1, true);
        allRadiiLayout.verticalSpacing = 10;
        allRadiiComp.setLayout(allRadiiLayout);
        allRadiiComp.setLayoutData(
                new GridData(SWT.LEFT, SWT.DEFAULT, false, true));

        Button allKt34Btn = new Button(allRadiiComp, SWT.PUSH);
        allKt34Btn.setText("All 34 kt");
        allKt34Btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawAllRadii(WindRadiiDisplayOptions.RADII_34_KNOT);
            }
        });

        Button allKt50Btn = new Button(allRadiiComp, SWT.PUSH);
        allKt50Btn.setText("All 50 kt");
        allKt50Btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawAllRadii(WindRadiiDisplayOptions.RADII_50_KNOT);
            }
        });

        Button allKt64Btn = new Button(allRadiiComp, SWT.PUSH);
        allKt64Btn.setText("All 64 kt");
        allKt64Btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawAllRadii(WindRadiiDisplayOptions.RADII_64_KNOT);
            }
        });

        Composite allClearComp = new Composite(dispAllComp, SWT.NONE);
        GridLayout allClearLayout = new GridLayout(1, true);
        allClearLayout.verticalSpacing = 10;
        allClearComp.setLayout(allClearLayout);
        allClearComp.setLayoutData(
                new GridData(SWT.LEFT, SWT.DEFAULT, false, true));

        Button dispAllBtn = new Button(allClearComp, SWT.PUSH);
        dispAllBtn.setText("Display All");
        dispAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawAllRadii(WindRadiiDisplayOptions.RADII_ALL_KNOT);
            }
        });

        Button clearAllBtn = new Button(allClearComp, SWT.PUSH);
        clearAllBtn.setText("Clear   All ");
        clearAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawAllRadii(WindRadiiDisplayOptions.RADII_NONE);
            }
        });

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                adjustOptionBtnStatus();
            }
        });

    }

    /**
     * Creates the control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {

        // Composite for control buttons.
        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnGDLayout = new GridLayout(2, false);
        ctrlBtnGDLayout.marginWidth = 10;
        ctrlBtnGDLayout.horizontalSpacing = 40;
        ctrlBtnComp.setLayout(ctrlBtnGDLayout);
        ctrlBtnComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(ctrlBtnComp, AtcfVizUtil.buttonGridData());

        Button doneButton = new Button(ctrlBtnComp, SWT.NONE);
        doneButton.setLayoutData(AtcfVizUtil.buttonGridData());
        doneButton.setText("Done");
        doneButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /**
     * Function to re-draw forecast track based on the selection on GUI.
     */
    private void drawRadiiOptions() {

        FcstTrackProperties temp = prd.getFcstTrackProperties();
        if (temp != null) {
            temp.setRadiiFor34Knot(kt34Btn.getSelection());
            temp.setRadiiFor50Knot(kt50Btn.getSelection());
            temp.setRadiiFor64Knot(kt64Btn.getSelection());

            String[] taus = tauList.getSelection();
            if (taus.length > 0) {
                String[] dtgs = new String[taus.length];
                int ii = 0;
                for (String tau : taus) {
                    dtgs[ii] = (String) tauList.getData(tau);
                    ii++;
                }

                temp.setSelectedRadiiDTGs(dtgs);
            }

            FcstTrackGenerator fstTrackGen = new FcstTrackGenerator(
                    atcfResource, temp, storm);
            fstTrackGen.create(true);
        }
    }

    /**
     * Sets the initial selection of the dtg List
     */
    private void setDtgSelections() {
        if (fcstProp != null && fcstProp.getSelectedRadiiDTGs() != null) {
            for (String dtg : fcstProp.getSelectedRadiiDTGs()) {
                int hour = fcstTrackDataMap.get(dtg).get(0).getFcstHour();
                tauList.select(tauList.indexOf(String.valueOf(hour)));
            }
        } else {
            tauList.selectAll();

            String[] taus = tauList.getItems();
            if (taus.length > 0) {
                String[] dtgs = new String[taus.length];
                int ii = 0;
                for (String tau : taus) {
                    dtgs[ii] = (String) tauList.getData(tau);
                    ii++;
                }

                if (fcstProp != null) {
                    fcstProp.setSelectedRadiiDTGs(dtgs);
                }
            }
        }
    }

    /**
     * Sets the buttons when the dialog comes back into focus
     */
    private void adjustOptionBtnStatus() {
        kt34Btn.setSelection(fcstProp.isRadiiFor34Knot());
        kt50Btn.setSelection(fcstProp.isRadiiFor50Knot());
        kt64Btn.setSelection(fcstProp.isRadiiFor64Knot());

        setDtgSelections();
    }

    /**
     * Function to re-draw forecast track based on the selection on GUI.
     *
     * @param radii
     *            WindRadiiDisplayOptions
     */
    private void drawAllRadii(WindRadiiDisplayOptions radii) {

        if (fcstProp != null) {
            switch (radii) {
            case RADII_34_KNOT:
                fcstProp.setRadiiFor34Knot(true);
                kt34Btn.setSelection(true);
                break;
            case RADII_50_KNOT:
                fcstProp.setRadiiFor50Knot(true);
                kt50Btn.setSelection(true);
                break;
            case RADII_64_KNOT:
                fcstProp.setRadiiFor64Knot(true);
                kt64Btn.setSelection(true);
                break;
            case RADII_ALL_KNOT:
                fcstProp.setRadiiFor34Knot(true);
                kt34Btn.setSelection(true);
                fcstProp.setRadiiFor50Knot(true);
                kt50Btn.setSelection(true);
                fcstProp.setRadiiFor64Knot(true);
                kt64Btn.setSelection(true);
                break;
            case RADII_NONE:
                fcstProp.setRadiiFor34Knot(false);
                kt34Btn.setSelection(false);
                fcstProp.setRadiiFor50Knot(false);
                kt50Btn.setSelection(false);
                fcstProp.setRadiiFor64Knot(false);
                kt64Btn.setSelection(false);
                break;
            default:
                break;
            }

            // Reselect all TAUs.
            tauList.selectAll();
            String[] taus = tauList.getItems();
            if (taus.length > 0) {
                String[] dtgs = new String[taus.length];
                int ii = 0;
                for (String tau : taus) {
                    dtgs[ii] = (String) tauList.getData(tau);
                    ii++;
                }

                fcstProp.setSelectedRadiiDTGs(dtgs);
            }

            // Re-Draw.
            FcstTrackGenerator fstTrackGen = new FcstTrackGenerator(
                    atcfResource, fcstProp, storm);
            fstTrackGen.create(true);
        }
    }

    /*
     * Class to represent all "Display All" options
     */
    private enum WindRadiiDisplayOptions {
        RADII_ALL_KNOT, RADII_34_KNOT, RADII_50_KNOT, RADII_64_KNOT, RADII_NONE;
    }

}