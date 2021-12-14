/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Aids => List Forecast Development Type/Phase". It displays
 * objective aid (OFCL) max wind and intensity forecast for a storm.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 16, 2020  85018      jwu          Initial creation
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class ListForecastDevelopmentDialog extends OcpCaveSWTDialog {

    // Current storm.
    private Storm storm;

    // Tech & wind radii.
    private static final String TECH = "OFCL";

    private static final WindRadii RADII = WindRadii.RADII_34_KNOT;

    private StyledText dataInfoTxt;

    /**
     * Constructor
     *
     * @param parent
     */
    public ListForecastDevelopmentDialog(Shell parent, Storm storm) {
        super(parent);
        this.storm = storm;
    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {

        // Add a scroll-able composite.
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        final Composite mainComp = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 0;
        mainComp.setLayout(mainLayout);
        mainComp.setData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Add sub-composites within the scroll-able composite.
        createDataInfoComp(mainComp);
        createControlButtons(mainComp);

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Create a StyledText to list latest forecast data.
     *
     * @param parent
     */
    private void createDataInfoComp(Composite parent) {

        Composite dataInfoComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoGL = new GridLayout(1, false);

        // StyledText to show current storm's forecast development.
        dataInfoComp.setLayout(dataInfoGL);
        dataInfoComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        dataInfoTxt = new StyledText(dataInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData dataInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataInfoGD.verticalIndent = 2;
        dataInfoGD.horizontalIndent = 5;
        dataInfoGD.widthHint = 910;
        dataInfoGD.heightHint = 800;

        dataInfoTxt.setLayoutData(dataInfoGD);
        dataInfoTxt.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        listForecastPhase(storm);
    }

    /*
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite btnComp = new Composite(parent, SWT.NONE);
        GridLayout btnGL = new GridLayout(1, true);
        btnGL.marginWidth = 30;

        btnComp.setLayout(btnGL);
        btnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        closeBtn.setFocus();
    }

    /*
     * Set the storm info into dialog title.
     */
    private void setTitle() {

        StringBuilder titleBdr = new StringBuilder();
        titleBdr.append("Forecast Development Type/Phase for "
                + storm.getTcHLevel() + " " + storm.getStormName() + " ("
                + storm.getStormId() + ")");
        String title = titleBdr.toString();

        setText(title);
    }

    /**
     * Display OFCL objective aid's max wind and intensity info.
     *
     * @param storm
     */
    public void listForecastPhase(Storm storm) {

        this.storm = storm;

        setTitle();

        // Get OFCL obj aids.
        List<ADeckRecord> ofclAidsData = AtcfDataUtil
                .getADeckRecords(storm, TECH, RADII.getValue());

        // Prepare to list the info.
        Map<RecordKey, ADeckRecord> recordMap = new HashMap<>();
        List<String> dtgList = new ArrayList<>();
        for (ADeckRecord rec : ofclAidsData) {
            String dtg = AtcfDataUtil
                    .calendarToDateTimeString(rec.getRefTimeAsCalendar());
            int tau = rec.getFcstHour();
            RecordKey key = new RecordKey(TECH, dtg, tau, RADII.getValue());
            if (!dtgList.contains(dtg)) {
                dtgList.add(dtg);
            }
            recordMap.put(key, rec);
        }

        // Sort DTG in descending order.
        Collections.sort(dtgList, Comparator.reverseOrder());

        // OFCL intensity info for each DTG and each forecast hour.
        StringBuilder sbr = new StringBuilder();
        sbr.append("REPORT for: " + TECH + "\n\n");
        sbr.append(String.format("%12s", "Date/Time "));
        for (AtcfTaus tau : AtcfTaus.getForecastSeasTaus()) {
            String tauStr = String.format("%03d  ", tau.getValue());
            sbr.append(String.format("%10s", tauStr));
        }
        sbr.append("\n");

        for (String dtg : dtgList) {
            sbr.append('\n').append(String.format("%12s", dtg));
            for (AtcfTaus tau : AtcfTaus.getForecastSeasTaus()) {
                ADeckRecord rec = getRecordByDtgNFcstHr(recordMap, dtg, tau);
                String intenStr = " ----- ";
                if (rec != null) {
                    intenStr = String.format("%3d(%2s)", (int) rec.getWindMax(),
                            rec.getIntensity());
                }

                sbr.append(String.format("%10s", intenStr));
            }
        }

        dataInfoTxt.setText(sbr.toString());
    }

    /*
     * Find the ADeckRecord stored in the map for the dtg/forecast hour.
     *
     * @param recordMap Map of ADeckRecord by DTG
     *
     * @param dtg DTG
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @return ADeckRecord
     */
    private ADeckRecord getRecordByDtgNFcstHr(
            Map<RecordKey, ADeckRecord> recordMap, String dtg, AtcfTaus tau) {
        RecordKey key = new RecordKey(TECH, dtg, tau.getValue(),
                RADII.getValue());
        return recordMap.get(key);
    }

}
