/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.widgets.SpinScale;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Track"=>"Compute Objective Aid CPAs".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2019 59317      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ComputeBestTrackCPADialog extends OcpCaveSWTDialog {

    // Size for spin-scale.
    private static final int SPIN_SCALE_WIDTH = 200;

    private static final int SPIN_SCALE_HEIGHT = 35;

    private Storm storm;

    // Widgets.
    private Label distLbl;

    private SpinScale distSpinScale;

    private List locNameList;

    private SpinScale latSpinScale;

    private SpinScale lonSpinScale;

    /**
     * Selection Counter
     */
    private int selectionCounter = 0;

    /**
     * Constructor
     *
     * @param parent
     */
    public ComputeBestTrackCPADialog(Shell parent, Storm storm) {
        super(parent);

        this.storm = storm;

        setText("Compute Best Track CPAs - " + storm.getStormName() + " "
                + storm.getStormId());

    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createDataComp(shell);
        createControlButtons(shell);
    }

    /**
     * Create the data input section.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.BORDER);
        GridLayout mainLayout = new GridLayout(3, true);
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 15;
        mainComp.setLayout(mainLayout);
        mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Use selected distance.
        createDistanceComp(mainComp);

        // Use selected location name.
        createLocationNameComp(mainComp);

        // Use selected lat/lon.
        createLatLonComp(mainComp);

    }

    /**
     * Create the composite to select distance and its unit.
     *
     * @param parent
     */
    private void createDistanceComp(Composite parent) {

        Composite distanceComp = new Composite(parent, SWT.NONE);
        GridLayout distanceLayout = new GridLayout(1, false);
        distanceLayout.verticalSpacing = 14;
        distanceLayout.marginHeight = 10;
        distanceLayout.marginWidth = 10;
        distanceComp.setLayout(distanceLayout);
        distanceComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Composite unitComp = new Composite(distanceComp, SWT.NONE);
        GridLayout unitLayout = new GridLayout(1, false);
        unitLayout.verticalSpacing = 5;
        unitLayout.marginTop = 0;
        unitLayout.marginBottom = 5;
        unitLayout.marginWidth = 10;
        unitComp.setLayout(unitLayout);
        unitComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // Select unit
        Label unitLbl = new Label(unitComp, SWT.None);
        GridData unitLblGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        unitLbl.setLayoutData(unitLblGD);
        unitLbl.setText("Units");

        Group unitGrp = new Group(unitComp, SWT.NONE);
        GridLayout unitGrpLayout = new GridLayout(1, true);
        unitGrpLayout.verticalSpacing = 4;
        unitGrpLayout.marginHeight = 0;
        unitGrpLayout.marginWidth = 15;
        unitGrp.setLayout(unitGrpLayout);
        unitGrp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Button nmRadioBtn = new Button(unitGrp, SWT.RADIO);
        nmRadioBtn.setText("nm");
        GridData nmRadioBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        nmRadioBtn.setLayoutData(nmRadioBtnGD);
        nmRadioBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.widget).getSelection()) {
                    distLbl.setText("Max CPA Distance (nm)");
                }
            }
        });

        nmRadioBtn.setSelection(true);

        Button miRadioBtn = new Button(unitGrp, SWT.RADIO);
        miRadioBtn.setText("mi");
        GridData miRadioBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        miRadioBtn.setLayoutData(miRadioBtnGD);
        miRadioBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.widget).getSelection()) {
                    distLbl.setText("Max CPA Distance (mi)");
                }
            }
        });

        Button kmRadioBtn = new Button(unitGrp, SWT.RADIO);
        kmRadioBtn.setText("km");
        GridData kmRadioBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        kmRadioBtn.setLayoutData(kmRadioBtnGD);
        kmRadioBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.widget).getSelection()) {
                    distLbl.setText("Max CPA Distance (km)");
                }
            }
        });

        // Select distance
        Composite distComp = new Composite(distanceComp, SWT.NONE);
        GridLayout distLayout = new GridLayout(1, false);
        distLayout.verticalSpacing = 5;
        distLayout.marginHeight = 5;
        distLayout.marginWidth = 5;
        distComp.setLayout(distLayout);
        distComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        distLbl = new Label(distComp, SWT.None);
        GridData distLblGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        distLbl.setLayoutData(distLblGD);
        distLbl.setText("Max CPA Distance (nm)");

        distSpinScale = new SpinScale(distComp, SWT.HORIZONTAL);
        distSpinScale.setLayoutData(
                new GridData(SPIN_SCALE_WIDTH, SPIN_SCALE_HEIGHT));
        distSpinScale.setValues(400, 0, 1000, 0, 1, 5);

        // Calculate with distance
        Button useDistBtn = new Button(distanceComp, SWT.PUSH);
        useDistBtn.setText("Use Selected Distance");
        useDistBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        useDistBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Perform calculation with distance.
            }
        });

    }

    /**
     * Create the composite to select location name.
     *
     * @param parent
     */
    private void createLocationNameComp(Composite parent) {

        Composite locComp = new Composite(parent, SWT.NONE);
        GridLayout locationLayout = new GridLayout(2, false);
        locationLayout.marginHeight = 0;
        locationLayout.marginWidth = 0;
        locationLayout.horizontalSpacing = 0;
        locComp.setLayout(locationLayout);

        locComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label separator = new Label(locComp, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

        Composite locNameComp = new Composite(locComp, SWT.NONE);

        GridLayout locNameLayout = new GridLayout(1, false);
        locNameLayout.verticalSpacing = 21;
        locNameLayout.marginHeight = 10;
        locNameLayout.marginWidth = 20;
        locNameComp.setLayout(locNameLayout);
        locNameComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label locNameLbl = new Label(locNameComp, SWT.None);
        GridData locNameGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        locNameLbl.setLayoutData(locNameGD);
        locNameLbl.setText("Location Name");

        // Location names
        locNameList = new List(locNameComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        locNameList.setToolTipText("Select a location");
        GridData locNameListGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        locNameListGD.widthHint = 170;
        locNameListGD.heightHint = 140;
        locNameList.setLayoutData(locNameListGD);
        // TODO replace with real location names....
        locNameList.add("Acapulco");
        locNameList.add("Hagatna");
        locNameList.add("Ailinglapalap");
        locNameList.add("Alamahgan");
        locNameList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectionCounter++;
            }
        });

        // Calculate with info from location name
        Button useLocNameBtn = new Button(locNameComp, SWT.PUSH);
        useLocNameBtn.setText("Use Location Name");
        useLocNameBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        useLocNameBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Perform calculation with location name.
            }
        });

    }

    /**
     * Create the composite to select latitude and longitude.
     *
     * @param parent
     */
    private void createLatLonComp(Composite parent) {

        Composite locComp = new Composite(parent, SWT.NONE);
        GridLayout locationLayout = new GridLayout(2, false);
        locationLayout.marginHeight = 0;
        locationLayout.marginWidth = 0;
        locationLayout.horizontalSpacing = 0;
        locComp.setLayout(locationLayout);

        locComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label separator = new Label(locComp, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

        Composite latlonComp = new Composite(locComp, SWT.NONE);
        GridLayout latlonLayout = new GridLayout(1, false);
        latlonLayout.verticalSpacing = 38;
        latlonLayout.marginHeight = 10;
        latlonLayout.marginWidth = 10;
        latlonComp.setLayout(latlonLayout);
        latlonComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // Select latitude
        Composite latComp = new Composite(latlonComp, SWT.NONE);
        GridLayout latLayout = new GridLayout(1, false);
        latLayout.verticalSpacing = 8;
        latLayout.marginHeight = 5;
        latLayout.marginWidth = 5;
        latComp.setLayout(latLayout);
        latComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label latLbl = new Label(latComp, SWT.None);
        GridData latLblGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        latLbl.setLayoutData(latLblGD);
        latLbl.setText("Latitude (south)");

        latSpinScale = new SpinScale(latComp, SWT.HORIZONTAL);
        latSpinScale.setLayoutData(
                new GridData(SPIN_SCALE_WIDTH, SPIN_SCALE_HEIGHT));
        latSpinScale.setValues(-900, -900, 900, 1, 1, 5);
        latSpinScale.setTextLimit(5);
        latSpinScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (latSpinScale.getSelection() < 0) {
                    latLbl.setText("Latitude (south)");
                } else {
                    latLbl.setText("Latitude (north)");
                }
            }
        });

        // Select longitude
        Composite lonComp = new Composite(latlonComp, SWT.NONE);
        GridLayout lonLayout = new GridLayout(1, false);
        lonLayout.verticalSpacing = 8;
        lonLayout.marginHeight = 5;
        lonLayout.marginWidth = 5;
        lonComp.setLayout(lonLayout);
        latComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label lonLbl = new Label(lonComp, SWT.None);
        GridData lonLblGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        lonLbl.setLayoutData(lonLblGD);
        lonLbl.setText("Longitude (west)");

        lonSpinScale = new SpinScale(lonComp, SWT.HORIZONTAL);
        lonSpinScale.setLayoutData(
                new GridData(SPIN_SCALE_WIDTH, SPIN_SCALE_HEIGHT));
        lonSpinScale.setValues(-1800, -1800, 1800, 1, 1, 10);
        lonSpinScale.setTextLimit(6);

        lonSpinScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lonSpinScale.getSelection() < 0) {
                    lonLbl.setText("Longitude (west)");
                } else {
                    lonLbl.setText("Longitude (east)");
                }
            }
        });

        // Calculate with lat and lon
        Button useLatlonBtn = new Button(latlonComp, SWT.PUSH);
        useLatlonBtn.setText("Use Lat and Lon");
        useLatlonBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        useLatlonBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Perform calculation with lat/lon.
            }
        });

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnCompLayout = new GridLayout(2, true);
        ctrlBtnCompLayout.marginWidth = 90;
        ctrlBtnCompLayout.marginHeight = 15;
        ctrlBtnCompLayout.horizontalSpacing = 50;
        ctrlBtnComp.setLayout(ctrlBtnCompLayout);
        ctrlBtnComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(ctrlBtnComp, AtcfVizUtil.buttonGridData());

        Button doneButton = new Button(ctrlBtnComp, SWT.PUSH);
        doneButton.setText("Done");
        doneButton.setLayoutData(AtcfVizUtil.buttonGridData());
        doneButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Perform actions with updated data.
                close();
            }
        });
    }

    /**
     * @return the selectedDistanance
     */
    public int getSelectedDistanance() {
        return distSpinScale.getSelection();
    }

    /**
     * @return the selectedLocation
     */
    public String getSelectedLocation() {
        return locNameList.getSelection()[0];
    }

    /**
     * @return the selectedLat
     */
    public float getSelectedLat() {
        return (float) (latSpinScale.getSelection() / 10.0);
    }

    /**
     * @return the selectedLon
     */
    public float getSelectedLon() {
        return (float) (lonSpinScale.getSelection() / 10.0);
    }

}