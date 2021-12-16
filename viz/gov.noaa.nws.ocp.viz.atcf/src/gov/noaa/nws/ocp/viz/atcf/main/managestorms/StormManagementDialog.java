/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main.managestorms;

import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Provide the ability to implement various versions of the same Storm
 * Management dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 10, 2018  45691      wpaintsil  Initial creation.
 * Jun 06, 2018  50987      wpaintsil  Change the class name from
 *                                     AtcfArchivedStormsDlg to StormManagementDialog.
 *                                     These dialogs have the same appearance.
 * Aug 10, 2020  79571      wpaintsil  Implement Genesis backend.
 * Oct 28, 2020  82623      jwu        Implement storm GUI & action.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class StormManagementDialog extends OcpCaveSWTDialog {
    /**
     * Widget for the list of storms
     */
    private List stormList;

    /**
     * The current storm
     */
    private Storm currentStorm;

    /**
     * The current genesis
     */
    private Genesis currentGenesis;

    /**
     * The current selected basin
     */
    private String basin;

    /**
     * The current selected year
     */
    private int year;

    /**
     * OK button
     */
    private Button okButton;

    /**
     * Flag for if this is managing geneses rather than storms.
     */
    private boolean isGenesis = false;

    /**
     * Used to determine window status
     */
    private StormManagementDialogStatusType windowStatus = StormManagementDialogStatusType.CANCEL;

    /**
     * Window Status
     */
    public enum StormManagementDialogStatusType {
        OK, CANCEL
    }

    /**
     * List of Geneses
     */
    private java.util.List<Genesis> geneses = new ArrayList<>();

    private java.util.List<Genesis> selectedGeneses = new ArrayList<>();

    /**
     * List of storms loaded in ATCF DB.
     */
    private java.util.List<Storm> storms = new ArrayList<>();

    private java.util.List<Storm> selectedStorms = new ArrayList<>();

    /**
     * Minimum year for the list of available years.
     */
    private static final int MIN_YEAR = 1851;

    /**
     * Indicator for "All Basins".
     */
    private static final String ALL_BASIN = "All Basins";

    /**
     * Constructor
     *
     * @param parentShell
     */
    public StormManagementDialog(Shell parentShell, String title) {
        this(parentShell, title, new ArrayList<>());
    }

    /**
     * Constructor
     *
     * @param shell
     * @param title
     * @param storms
     */
    public StormManagementDialog(Shell shell, String title,
            java.util.List<?> dataList) {
        super(shell, SWT.SHEET);
        setText(title);

        refreshDataList(dataList);
    }

    /**
     * Initialize the dialog components.
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createStormList(shell);
        createControlButtons(shell);
    }

    /**
     * Create list of storms.
     *
     * @param parent
     */
    private void createStormList(Composite parent) {
        Composite stormListComp = new Composite(parent, SWT.NONE);

        GridLayout listCompLayout = new GridLayout(1, false);
        listCompLayout.marginHeight = 10;
        listCompLayout.marginWidth = 10;
        listCompLayout.verticalSpacing = 15;
        stormListComp.setLayout(listCompLayout);
        stormListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Composite stormBasinComp = new Composite(stormListComp, SWT.NONE);
        stormBasinComp.setLayout(new GridLayout(1, false));
        stormBasinComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        Label basinLabel = new Label(stormBasinComp, SWT.NONE);
        basinLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        basinLabel.setText("Storm Basin");
        CCombo basinCombo = new CCombo(stormBasinComp, SWT.BORDER);
        basinCombo.setEditable(false);
        String[] allRegions = AtcfBasin.getDescriptions();
        basinCombo.setItems(allRegions);
        basinCombo.add(ALL_BASIN, 0);

        basin = ALL_BASIN;
        basinCombo.select(0);

        basinCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String preBasin = basin;
                basin = ALL_BASIN;
                if (basinCombo.getSelectionIndex() > 0) {
                    basin = AtcfBasin.getBasinbyDesc(basinCombo.getText())
                            .name();
                }

                if (!preBasin.equals(basin)) {
                    updateStormList();
                }
            }
        });

        Composite stormYearComp = new Composite(stormListComp, SWT.NONE);
        stormYearComp.setLayout(new GridLayout(1, false));
        stormYearComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        Label yearLabel = new Label(stormYearComp, SWT.CENTER);
        yearLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));
        yearLabel.setText("Storm Year");
        CCombo yearCombo = new CCombo(stormYearComp, SWT.BORDER);
        yearCombo.setEditable(false);

        int currentYear = TimeUtil.newCalendar().get(Calendar.YEAR);
        for (int ii = currentYear; ii >= MIN_YEAR; ii--) {
            yearCombo.add(String.valueOf(ii));
        }

        year = currentYear;
        yearCombo.select(0);

        yearCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                int preYr = year;
                year = Integer.parseInt(yearCombo.getText());

                if (preYr != year) {
                    updateStormList();
                }
            }
        });

        Composite chooseStormComp = new Composite(stormListComp, SWT.NONE);
        chooseStormComp.setLayout(new GridLayout(1, false));
        Label chooseLabel = new Label(chooseStormComp, SWT.NONE);
        if (isGenesis) {
            chooseLabel.setText("Choose a genesis:");
        } else {
            chooseLabel.setText("Choose a storm:");
        }

        stormList = new List(chooseStormComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        stormList.setLayoutData(new GridData(400, 300));

        updateStormList();

        stormList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (stormList.getSelectionCount() > 0 && okButton != null) {
                    int selIndex = stormList.getSelectionIndex();
                    if (selIndex >= 0) {
                        if (isGenesis) {
                            currentGenesis = selectedGeneses.get(selIndex);
                        } else {
                            currentStorm = selectedStorms.get(selIndex);
                        }

                        okButton.setEnabled(true);
                    }
                }
            }
        });

    }

    /**
     * Create bottom buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 40;

        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        okButton = new Button(buttonComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.setEnabled(
                stormList != null && stormList.getSelectionCount() > 0);
        okButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                windowStatus = StormManagementDialogStatusType.OK;
                close();
            }
        });
        okButton.setFocus();

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
        cancelButton.setFocus();
    }

    /*
     * Update the storm/geneses list based on currently selected basin and year.
     */
    private void updateStormList() {

        stormList.removeAll();
        if (okButton != null) {
            okButton.setEnabled(false);
        }

        if (isGenesis) {
            selectedGeneses.clear();
            for (Genesis ges : geneses) {
                if (year == ges.getYear() && (basin.equals(ALL_BASIN)
                        || basin.equals(ges.getRegion().toUpperCase()))) {
                    selectedGeneses.add(ges);
                }
            }

            for (Genesis gen : selectedGeneses) {
                stormList.add(gen.getGenesisNum() + " " + gen.getYear() + " - "
                        + gen.getGenesisName());
            }

        } else {
            selectedStorms.clear();
            for (Storm stm : storms) {
                if (year == stm.getYear() && (basin.equals(ALL_BASIN)
                        || basin.equals(stm.getRegion().toUpperCase()))) {
                    selectedStorms.add(stm);
                }
            }

            for (Storm sto : selectedStorms) {
                stormList.add(sto.getCycloneNum() + " " + sto.getYear() + " - "
                        + sto.getStormName());
            }
        }
    }

    /**
     * Refresh the storm/genesis list in case new ones are added and some are
     * deleted.
     *
     * @param dataList
     *            the dataList to refresh
     */
    @SuppressWarnings("unchecked")
    public void refreshDataList(java.util.List<?> dataList) {

        if (dataList != null && !dataList.isEmpty()) {
            if (dataList.get(0) instanceof Storm) {
                this.storms = (java.util.List<Storm>) dataList;
                this.selectedStorms.addAll((java.util.List<Storm>) dataList);
            } else if (dataList.get(0) instanceof Genesis) {
                this.geneses = (java.util.List<Genesis>) dataList;
                this.selectedGeneses.addAll((java.util.List<Genesis>) dataList);
                isGenesis = true;
            }
        }

        if (stormList != null && !stormList.isDisposed()) {
            updateStormList();
        }
    }

    /**
     * @return the windowStatus
     */
    public StormManagementDialogStatusType getWindowStatus() {
        return windowStatus;
    }

    /**
     * @return the currentStorm
     */
    public Storm getCurrentStorm() {
        return currentStorm;
    }

    /**
     * @param currentStorm
     *            the currentStorm to set
     */
    public void setCurrentStorm(Storm currentStorm) {
        this.currentStorm = currentStorm;
    }

    /**
     * @return the currentGenesis
     */
    public Genesis getCurrentGenesis() {
        return currentGenesis;
    }

    /**
     * @param currentGenesis
     *            the currentGenesis to set
     */
    public void setCurrentGenesis(Genesis currentGenesis) {
        this.currentGenesis = currentGenesis;
    }

}
