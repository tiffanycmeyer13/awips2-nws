/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.advisory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * Compute CPAs dialog
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 09, 2018  52149      wpaintsil  Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class CPAsDialog extends CaveSWTDialog {

    private Label distanceLabel;

    private Button nmRdoButton;

    private Button miRdoButton;

    private Button kmRdoButton;

    private Slider cpaDistanceSlider;

    private Slider lonSlider;

    private Label eastWestLabel;

    private Label northSouthLabel;

    private Slider latSlider;

    private Label distUnitLabel;

    private Label latValueLabel;

    private Label lonValueLabel;

    private static final int LAT_MAX = 1810;

    private static final int LON_MAX = 3610;

    private static final int CPA_DIST_MAX = 40;

    /**
     * @param parent
     */
    public CPAsDialog(Shell parent, Storm storm) {
        super(parent);
        setText("CPAs - " + storm.getStormName() + " " + storm.getStormId());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createMainComposite(shell);
        createControlButtons(shell);
    }

    /**
     * Create the Composite holding most of the dialog content.
     *
     * @param parent
     */
    private void createMainComposite(Composite parent) {
        Composite mainComp = new Composite(parent, SWT.BORDER);
        GridLayout mainLayout = new GridLayout(3, true);
        mainLayout.horizontalSpacing = 0;
        mainLayout.marginHeight = 0;
        mainComp.setLayout(mainLayout);

        createDistanceComp(mainComp);
        createLocationComp(mainComp);
        createLatLonComp(mainComp);
    }

    /**
     * Create the left section with the unit radio buttons and distance slider.
     *
     * @param parent
     */
    private void createDistanceComp(Composite parent) {
        Composite distanceComp = new Composite(parent, SWT.NONE);
        GridLayout distanceLayout = new GridLayout(1, false);
        distanceComp.setLayout(distanceLayout);
        distanceComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite unitsComp = new Composite(distanceComp, SWT.NONE);
        unitsComp.setLayout(new GridLayout(1, false));
        unitsComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label unitsLabel = new Label(unitsComp, SWT.NONE);
        unitsLabel.setText("Units");
        unitsLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Composite unitRdoComp = new Composite(unitsComp, SWT.BORDER);
        GridLayout unitRdoLayout = new GridLayout(1, false);
        unitRdoComp.setLayout(unitRdoLayout);

        nmRdoButton = new Button(unitRdoComp, SWT.RADIO);
        nmRdoButton.setText("nm");

        miRdoButton = new Button(unitRdoComp, SWT.RADIO);
        miRdoButton.setText("mi");

        kmRdoButton = new Button(unitRdoComp, SWT.RADIO);
        kmRdoButton.setText("km");

        nmRdoButton.setSelection(true);

        Composite cpaDistanceComp = new Composite(distanceComp, SWT.NONE);
        GridLayout cpaDistanceLayout = new GridLayout(1, false);
        cpaDistanceLayout.verticalSpacing = 0;
        cpaDistanceComp.setLayout(cpaDistanceLayout);
        cpaDistanceComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label cpaDistanceLabel = new Label(cpaDistanceComp, SWT.NONE);
        cpaDistanceLabel.setText("Max CPA Distance");
        cpaDistanceLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        cpaDistanceSlider = new Slider(cpaDistanceComp, SWT.HORIZONTAL);
        cpaDistanceSlider.setMinimum(0);
        cpaDistanceSlider.setMaximum(CPA_DIST_MAX);
        cpaDistanceSlider.setIncrement(1);
        cpaDistanceSlider.setPageIncrement(10);
        cpaDistanceSlider.setSelection(0);
        cpaDistanceSlider.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));

        Composite distUnitComp = new Composite(cpaDistanceComp, SWT.NONE);
        GridLayout distUnitLayout = new GridLayout(2, false);
        distUnitComp.setLayout(distUnitLayout);
        distUnitComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        distanceLabel = new Label(distUnitComp, SWT.NONE);
        distanceLabel.setLayoutData(
                new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

        /*
         * Separate the number and the unit type for aesthetics. If they are in
         * one label, the unit type string shifts around as the length of the
         * distance number changes.
         */
        distUnitLabel = new Label(distUnitComp, SWT.NONE);
        distUnitLabel.setLayoutData(
                new GridData(SWT.LEFT, SWT.DEFAULT, true, false));

        cpaSliderLabelChange();

        nmRdoButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cpaSliderLabelChange();
            }
        });
        miRdoButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cpaSliderLabelChange();
            }
        });
        kmRdoButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cpaSliderLabelChange();
            }
        });

        cpaDistanceSlider.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cpaSliderLabelChange();
            }
        });

        Button distanceButton = new Button(distanceComp, SWT.PUSH);
        distanceButton.setText("Use Selected Distance");
        distanceButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
    }

    /**
     * Update the distance label when the slider is selected. Update the unit
     * type when a radio button is selected.
     */
    private void cpaSliderLabelChange() {
        String unitString;

        if (miRdoButton.getSelection()) {
            unitString = miRdoButton.getText();
        } else if (kmRdoButton.getSelection()) {
            unitString = kmRdoButton.getText();
        } else {
            unitString = nmRdoButton.getText();
        }

        distanceLabel.setText(StringUtils.center(
                String.valueOf(cpaDistanceSlider.getSelection() * 50), 9));
        distUnitLabel.setText(unitString);
    }

    /**
     * Create the middle section containing the Location Name list.
     *
     * @param parent
     */
    private void createLocationComp(Composite parent) {

        Composite centerComp = new Composite(parent, SWT.BORDER);
        GridLayout locationLayout = new GridLayout(2, false);
        locationLayout.marginHeight = 0;
        locationLayout.marginWidth = 0;
        locationLayout.horizontalSpacing = 0;
        centerComp.setLayout(locationLayout);

        centerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite nameComp = new Composite(centerComp, SWT.NONE);
        nameComp.setLayout(new GridLayout(1, false));
        nameComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite nameListComp = new Composite(nameComp, SWT.NONE);
        nameListComp.setLayout(new GridLayout(1, false));
        nameListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label nameLabel = new Label(nameListComp, SWT.NONE);
        nameLabel.setText("Location Name");
        nameLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        List nameList = new List(nameListComp, SWT.SINGLE | SWT.V_SCROLL);
        GridData nameListData = new GridData(SWT.FILL, SWT.FILL, false, false);
        Rectangle trim = nameList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(nameList) * 3,
                nameList.getItemHeight() * 3);
        nameListData.widthHint = trim.width;
        nameListData.heightHint = trim.height;
        nameList.setLayoutData(nameListData);
        for (int ii = 0; ii < 20; ii++) {
            nameList.add("Placeholder");
        }

        Button useNameButton = new Button(nameComp, SWT.PUSH);
        useNameButton.setText("Use Name");
        useNameButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

    }

    /**
     * Create the right section containing the latitude and longitude sliders.
     *
     * @param parent
     */
    private void createLatLonComp(Composite parent) {
        Composite leftComp = new Composite(parent, SWT.NONE);
        GridLayout leftLayout = new GridLayout(2, false);
        leftLayout.marginHeight = 0;
        leftLayout.marginWidth = 0;
        leftLayout.horizontalSpacing = 0;
        leftComp.setLayout(leftLayout);
        leftComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite latLonComp = new Composite(leftComp, SWT.NONE);
        GridLayout latLonLayout = new GridLayout(1, false);
        latLonComp.setLayout(latLonLayout);
        latLonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite latComp = new Composite(latLonComp, SWT.NONE);
        GridLayout latLayout = new GridLayout(1, true);
        latLayout.verticalSpacing = 0;
        latComp.setLayout(latLayout);
        latComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label latLabel = new Label(latComp, SWT.NONE);
        latLabel.setText("Latitude");
        GridData latLblData = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        latLabel.setLayoutData(latLblData);

        Composite latSliderComp = new Composite(latComp, SWT.NONE);
        GridLayout latSliderLayout = new GridLayout(3, false);
        latSliderLayout.marginHeight = 0;
        latSliderComp.setLayout(latSliderLayout);
        latSliderComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));

        Label latMinLabel = new Label(latSliderComp, SWT.NONE);
        latMinLabel.setText("-90");

        latSlider = new Slider(latSliderComp, SWT.HORIZONTAL);
        latSlider.setMinimum(0);
        latSlider.setMaximum(LAT_MAX);
        // TODO: set the appropriate value in the slider
        latSlider.setSelection(900);
        latSlider.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));

        Label latMaxLabel = new Label(latSliderComp, SWT.NONE);
        latMaxLabel.setText("90");

        Composite northSouthComp = new Composite(latComp, SWT.NONE);
        GridLayout northSouthLayout = new GridLayout(2, false);
        northSouthLayout.marginHeight = 0;
        northSouthComp.setLayout(northSouthLayout);
        northSouthComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        latValueLabel = new Label(northSouthComp, SWT.NONE);
        latValueLabel.setLayoutData(
                new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
        latValueLabel.setText(StringUtils.center("0.0", 8));

        northSouthLabel = new Label(northSouthComp, SWT.NONE);
        northSouthLabel.setLayoutData(
                new GridData(SWT.LEFT, SWT.DEFAULT, true, false));
        northSouthLabel.setText("north ");

        latSlider.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                float latValue = Float.valueOf(latSlider.getSelection() - 900f)
                        / 10;
                if (latValue >= 0) {
                    latValueLabel.setText(
                            StringUtils.center(String.valueOf(latValue), 8));
                    northSouthLabel.setText("north ");

                } else {
                    latValueLabel.setText(StringUtils
                            .center(String.valueOf(-1 * latValue), 8));
                    northSouthLabel.setText("south ");
                }

            }
        });

        Composite lonComp = new Composite(latLonComp, SWT.NONE);
        GridLayout lonLayout = new GridLayout(1, true);
        lonLayout.verticalSpacing = 0;
        lonComp.setLayout(lonLayout);
        lonComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Label lonLabel = new Label(lonComp, SWT.NONE);
        lonLabel.setText("Longitude");
        GridData lonLblData = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        lonLabel.setLayoutData(lonLblData);

        Composite lonSliderComp = new Composite(lonComp, SWT.NONE);
        GridLayout lonSliderLayout = new GridLayout(3, false);
        lonSliderLayout.marginHeight = 0;
        lonSliderComp.setLayout(lonSliderLayout);
        lonSliderComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));

        new Label(lonSliderComp, SWT.NONE).setText("-180");

        lonSlider = new Slider(lonSliderComp, SWT.HORIZONTAL);
        lonSlider.setMinimum(0);
        lonSlider.setMaximum(LON_MAX);
        // TODO: set the appropriate value in the slider
        lonSlider.setSelection(1800);
        lonSlider
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(lonSliderComp, SWT.NONE).setText("180");

        Composite eastWestComp = new Composite(lonComp, SWT.NONE);
        GridLayout eastWestLayout = new GridLayout(2, false);
        eastWestLayout.marginHeight = 0;
        eastWestComp.setLayout(eastWestLayout);
        eastWestComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        lonValueLabel = new Label(eastWestComp, SWT.NONE);
        lonValueLabel.setLayoutData(
                new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
        // TODO: set this to the appropriate value in the database
        lonValueLabel.setText(StringUtils.center("0.0", 8));

        eastWestLabel = new Label(eastWestComp, SWT.NONE);
        eastWestLabel.setLayoutData(
                new GridData(SWT.LEFT, SWT.DEFAULT, true, false));
        eastWestLabel.setText("east ");

        lonSlider.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                float lonValue = Float.valueOf(lonSlider.getSelection() - 1800f)
                        / 10;
                if (lonValue >= 0) {
                    lonValueLabel.setText(
                            StringUtils.center(String.valueOf(lonValue), 8));
                    eastWestLabel.setText("east ");
                } else {
                    lonValueLabel.setText(StringUtils
                            .center(String.valueOf(-1 * lonValue), 8));
                    eastWestLabel.setText("west ");
                }

            }
        });

        Button latLonButton = new Button(latLonComp, SWT.PUSH);
        latLonButton.setText("Use Lat and Lon");
        latLonButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
    }

    /**
     * Create bottom buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(2, true);

        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.buttonGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        Button doneButton = new Button(buttonComp, SWT.PUSH);
        doneButton.setText("Done");
        doneButton.setLayoutData(AtcfVizUtil.buttonGridData());

        doneButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        doneButton.setFocus();

    }

}
