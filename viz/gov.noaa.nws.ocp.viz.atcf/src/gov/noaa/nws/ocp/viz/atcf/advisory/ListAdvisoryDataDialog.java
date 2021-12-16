/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.util.Collections;
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

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Advisory => List Advisory Data".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 13, 2020  84966      jwu          Initial creation
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class ListAdvisoryDataDialog extends OcpCaveSWTDialog {

    // Current storm.
    private Storm storm;

    // Dialog title with storm info.
    private String title;

    // Maximum wind direction, speed (intensity).
    private static final int MAX_WIND_DRCT = 360;

    private static final int MAX_WIND_SPEED = 250;

    // Number to indicate 12 ft seas forecast.
    private static final int RAD_WAVE = 12;

    // Maximum radii.
    private static final int MAXIMUM_WIND_RADII = 995;

    private StyledText dataInfoTxt;

    /**
     * Constructor
     *
     * @param parent
     */
    public ListAdvisoryDataDialog(Shell parent, Storm storm) {
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
     * Create a StyledText to list advisory data.
     *
     * @param parent
     */
    private void createDataInfoComp(Composite parent) {
        Composite dataInfoComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoGL = new GridLayout(1, false);

        // StyledText to show current storm's advisory data.
        dataInfoComp.setLayout(dataInfoGL);
        dataInfoComp
        .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        dataInfoTxt = new StyledText(dataInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        dataInfoTxt.setFont(JFaceResources.getTextFont());
        GridData dataInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataInfoTxt.setLayoutData(dataInfoGD);

        listAdvisoryData(storm);
    }

    /*
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite btnComp = new Composite(parent, SWT.NONE);
        GridLayout btnGL = new GridLayout(1, true);

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
     *
     * @param dtg
     */
    private void setTitle(String dtg) {

        StringBuilder titleBdr = new StringBuilder();
        titleBdr.append("Forecast for " + storm.getTcHLevel() + " "
                + storm.getStormName() + " (" + storm.getCycloneNum() + ") at "
                + dtg);

        title = titleBdr.toString();

        setText(title);
    }

    /**
     * Display advisory data info.
     *
     * @param storm
     */
    public void listAdvisoryData(Storm storm) {

        this.storm = storm;

        // Get forecast track.
        Map<String, List<ForecastTrackRecord>> fcstTrackData = AtcfDataUtil
                .getFcstTrackRecords(storm, false);
        String cdtg = "";
        if (fcstTrackData != null && !fcstTrackData.isEmpty()) {
            cdtg = Collections.min(fcstTrackData.keySet());
        }

        setTitle(cdtg);

        StringBuilder sbr = new StringBuilder();
        String titleFmt = "\n%" + (title.length() + 2) + "s\n\n";
        sbr.append(String.format(titleFmt, title));

        sbr.append(String.format("%5s%8s%20s%12s%13s%7s%24s", "Tau", "day/hr",
                "center position", "motion", "wind(kt)", "spd",
                "wind speed radii(nm)")).append('\n');

        sbr.append(String.format("%12s%8s%8s%8s%9s%8s%6s%6s%22s", "(UTC)",
                "NHC", "WPC", "FINAL", "dir/spd", "1-min", "gust", "kt",
                "NE   SE   SW   NW")).append('\n');

        StringBuilder stb = new StringBuilder();
        for (int ii = 0; ii < 90; ii++) {
            stb.append("-");
        }

        String sep = stb.toString();

        sbr.append(String.format("%90s", sep)).append('\n');

        // Advisory info for each DTG and each radii.
        ForecastTrackRecord prevRec = null;
        for (Map.Entry<String, List<ForecastTrackRecord>> entry : fcstTrackData
                .entrySet()) {
            List<ForecastTrackRecord> recs = entry.getValue();

            if (recs != null && !recs.isEmpty()) {
                ForecastTrackRecord rec34 = recs.get(0);

                int fcstHr = rec34.getFcstHour();
                float lat = rec34.getClat();
                float lon = rec34.getClon();

                // Calculate speed & direction
                int spd = (int) rec34.getStormSped();
                int dir = (int) rec34.getStormDrct();
                if (prevRec != null && (rec34.getStormSped() <= 0
                        || rec34.getStormSped() > MAX_WIND_SPEED
                        || rec34.getStormDrct() > MAX_WIND_DRCT)) {

                    long hrs = (rec34.getRefTime().getTime()
                            - prevRec.getRefTime().getTime())
                            / TimeUtil.MILLIS_PER_HOUR;
                    Coordinate loc = new Coordinate(prevRec.getClon(),
                            prevRec.getClat());
                    Coordinate loc1 = new Coordinate(rec34.getClon(),
                            rec34.getClat());
                    double[] distDir = AtcfVizUtil.getDistNDir(loc, loc1);

                    dir = AtcfDataUtil.roundToNearest(distDir[1], 5);
                    spd = (int) Math.round(distDir[0] / AtcfVizUtil.NM2M / hrs);
                }

                prevRec = rec34;

                String latStr = String.format("%4.1f%s", Math.abs(lat),
                        (lat < 0) ? "S" : "N");
                String dirSpd = String.format("%3d/%03d", dir, spd);

                // Format information
                sbr.append(String.format("%4d%8s%9s%15s%9s%7d%6d%7d", fcstHr,
                        entry.getKey().substring(6), latStr, "", dirSpd,
                        (int) rec34.getWindMax(), (int) rec34.getGust(), 64));

                if (recs.size() > 2) {
                    sbr.append(getWindRadiiInfo(recs.get(2)));
                }

                String lonStr = String.format("%5.1f%s", Math.abs(lon),
                        (lon > 0) ? "E" : "W");
                sbr.append('\n').append(String.format("%21s%44d", lonStr, 50));

                if (recs.size() > 1) {
                    sbr.append(getWindRadiiInfo(recs.get(1)));
                }

                sbr.append('\n').append(String.format("%65d", 34));
                sbr.append(getWindRadiiInfo(rec34));

                // 12-ft wave radii.
                if (fcstHr == AtcfTaus.TAU3.getValue()) {
                    sbr.append('\n').append(String.format("%65d", RAD_WAVE));
                    sbr.append(getWaveRadiiInfo(rec34));
                }

                sbr.append("\n" + sep + "\n");
            }
        }

        dataInfoTxt.setText(sbr.toString());
    }

    /*
     * Get a formatted string for wind radii in a ForecastTrackRecord
     *
     * @param rec ForecastTrackRecord
     *
     * @return string
     */
    private String getWindRadiiInfo(ForecastTrackRecord rec) {
        StringBuilder sbr = new StringBuilder();
        sbr.append(String.format("%7d%5d%5d%5d", (int) rec.getQuad1WindRad(),
                (int) rec.getQuad2WindRad(), (int) rec.getQuad3WindRad(),
                (int) rec.getQuad4WindRad()));
        return sbr.toString();
    }

    /*
     * Get a formatted string for wave radii in a ForecastTrackRecord
     *
     * @param rec ForecastTrackRecord
     *
     * @return string
     */
    private String getWaveRadiiInfo(ForecastTrackRecord rec) {

        StringBuilder sbr = new StringBuilder();
        int rad1 = (int) rec.getQuad1WaveRad();
        if (rad1 >= 0 && rad1 <= MAXIMUM_WIND_RADII) {
            sbr.append(String.format("%7d", rad1));
        } else {
            sbr.append(String.format("%7s", ""));
        }

        int rad2 = (int) rec.getQuad2WaveRad();
        if (rad2 >= 0 && rad2 <= MAXIMUM_WIND_RADII) {
            sbr.append(String.format("%5d", rad2));
        } else {
            sbr.append(String.format("%5s", ""));
        }

        int rad3 = (int) rec.getQuad3WaveRad();
        if (rad3 >= 0 && rad3 <= MAXIMUM_WIND_RADII) {
            sbr.append(String.format("%5d", rad3));
        } else {
            sbr.append(String.format("%5s", ""));
        }

        int rad4 = (int) rec.getQuad4WaveRad();
        if (rad4 >= 0 && rad4 <= MAXIMUM_WIND_RADII) {
            sbr.append(String.format("%5d", rad4));
        } else {
            sbr.append(String.format("%5s", ""));
        }

        return sbr.toString();
    }
}
