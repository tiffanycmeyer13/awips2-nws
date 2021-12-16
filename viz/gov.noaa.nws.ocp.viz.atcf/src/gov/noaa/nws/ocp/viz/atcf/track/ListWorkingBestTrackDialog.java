/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import java.util.Collections;
import java.util.Comparator;
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
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Track => List Working Best Track".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 12, 2020  84922      jwu          Initial creation
 * Jul 07, 2021  94036      jnengel      Fixes windspeed group sorting
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class ListWorkingBestTrackDialog extends OcpCaveSWTDialog {

    private static final String DIRCTIONS = "NE   SE   SW   NW";

    // Current storm.
    private Storm storm;

    // Dialog title with storm info.
    private String title;

    // Maximum wind direction, speed (intensity).
    private static final int MAX_WIND_DRCT = 360;

    private static final int MAX_WIND_SPEED = 250;

    private StyledText dataInfoTxt;

    /**
     * Constructor
     *
     * @param parent
     */
    public ListWorkingBestTrackDialog(Shell parent, Storm storm) {
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
     * Create a StyledText to list best track data.
     *
     * @param parent
     */
    private void createDataInfoComp(Composite parent) {
        Composite dataInfoComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoGL = new GridLayout(1, false);

        // StyledText to show current storm's best track data.
        dataInfoComp.setLayout(dataInfoGL);
        dataInfoComp
        .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        dataInfoTxt = new StyledText(dataInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData dataInfoGD = new GridData(SWT.FILL, SWT.FILL, false, true);
        dataInfoGD.verticalIndent = 2;
        dataInfoGD.horizontalIndent = 5;
        dataInfoGD.widthHint = 1235;
        dataInfoGD.heightHint = 600;

        dataInfoTxt.setLayoutData(dataInfoGD);
        dataInfoTxt.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        listBestTrackData(storm);
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
        String status = storm.getTcHLevel();
        String stormState = StormDevelopment.getLongIntensityString(status);

        StringBuilder titleBdr = new StringBuilder();
        titleBdr.append("ATCF - Working Best Track for ");
        if (stormState != null) {
            titleBdr.append(stormState + " ");
        }
        titleBdr.append(storm.getStormId().toLowerCase());

        title = titleBdr.toString();

        setText(title);
    }

    /**
     * Display best track info.
     *
     * @param storm
     */
    public void listBestTrackData(Storm storm) {

        this.storm = storm;

        setTitle();

        Map<String, List<BDeckRecord>> currentBDeckRecords = AtcfDataUtil
                .getBDeckRecords(storm, false);

        StringBuilder sbr = new StringBuilder();
        String titleFmt = "\n%" + (title.length() + 2) + "s\n";
        sbr.append(String.format(titleFmt, title));

        sbr.append(String.format("%108s%21s%21s", "34 kt radii(nm)",
                "50 kt radii(nm)", "64 kt radii(nm)")).append("\n");

        sbr.append(String.format(
                "%-15s%8s%8s%6s%5s%5s%6s%8s%10s%10s%9s%19s%21s%21s ",
                "    DTG  ", "LAT ", "LONG ", "VMAX", "DIR", "SPD", "MSLP",
                "STATUS", "POCI(mb)", "ROCI(nm)", "RMW(nm)", DIRCTIONS,
                DIRCTIONS, DIRCTIONS)).append("\n");

        // Best track info for each DTG and each radii.
        BDeckRecord prevRec = null;
        for (Map.Entry<String, List<BDeckRecord>> entry : currentBDeckRecords
                .entrySet()) {
            List<BDeckRecord> recs = entry.getValue();
            Collections.sort(recs,
                    Comparator.comparing(BDeckRecord::getRadWind));

            if (recs != null && !recs.isEmpty()) {
                BDeckRecord rec34 = recs.get(0);

                // Calculate speed & direction
                int[] dirSpd = getSpeedDir(rec34, prevRec);

                prevRec = rec34;

                // Format information
                sbr.append(getRecInfo(entry.getKey(), rec34, dirSpd));
                if (recs.size() > 1) {
                    sbr.append(getWindRadiiInfo(recs.get(1)));

                    if (recs.size() > 2) {
                        sbr.append(getWindRadiiInfo(recs.get(2)));
                    }
                }

                sbr.append("\n");
            }
        }

        dataInfoTxt.setText(sbr.toString());
    }

    /*
     * Get a storm speed and direction from one BDeckRecord to another record.
     *
     * @param rec BDeckRecord
     *
     * @param prevRec BDeckRecord before rec
     *
     * @return int[] int[0] - direction, int[1] - speed
     */
    private int[] getSpeedDir(BDeckRecord rec, BDeckRecord prevRec) {
        int spd = (int) rec.getStormSped();
        int dir = (int) rec.getStormDrct();
        if ((rec.getStormSped() <= 0 || rec.getStormSped() > MAX_WIND_SPEED
                || rec.getStormDrct() > MAX_WIND_DRCT) && prevRec != null) {

            long hrs = (rec.getRefTime().getTime()
                    - prevRec.getRefTime().getTime())
                    / TimeUtil.MILLIS_PER_HOUR;
            Coordinate loc = new Coordinate(prevRec.getClon(),
                    prevRec.getClat());
            Coordinate loc1 = new Coordinate(rec.getClon(), rec.getClat());
            double[] distDir = AtcfVizUtil.getDistNDir(loc, loc1);

            dir = AtcfDataUtil.roundToNearest(distDir[1], 5);
            spd = (int) Math.round(distDir[0] / AtcfVizUtil.NM2M / hrs);
        }

        return new int[] { dir, spd };
    }

    /*
     * Get a formatted string for a best track record
     *
     * @param rec BDeckRecord
     *
     * @return string
     */
    private String getRecInfo(String dtg, BDeckRecord rec, int[] dirSpd) {
        StringBuilder sbr = new StringBuilder();
        sbr.append(String.format(
                " %-14s%8s%8s%6d%5d%5d%6d%8s%6d%4s%6d%4s%6d %s", dtg,
                String.format("%4.1f%s", Math.abs(rec.getClat()),
                        (rec.getClat() < 0) ? "S" : "N"),
                String.format("%5.1f%s", Math.abs(rec.getClon()),
                        (rec.getClon() > 0) ? "E" : "W"),
                (int) rec.getWindMax(), dirSpd[0], dirSpd[1],
                (int) rec.getMslp(), rec.getIntensity() + "  ",
                (int) rec.getClosedP(), "", (int) rec.getRadClosedP(), "",
                (int) rec.getMaxWindRad(), getWindRadiiInfo(rec)));
        return sbr.toString();
    }

    /*
     * Get a formatted string for wind radii in a best track record
     *
     * @param rec BDeckRecord
     *
     * @return string
     */
    private String getWindRadiiInfo(BDeckRecord rec) {
        StringBuilder sbr = new StringBuilder();
        sbr.append(String.format("%6d%5d%5d%5d", (int) rec.getQuad1WindRad(),
                (int) rec.getQuad2WindRad(), (int) rec.getQuad3WindRad(),
                (int) rec.getQuad4WindRad()));
        return sbr.toString();
    }

}
