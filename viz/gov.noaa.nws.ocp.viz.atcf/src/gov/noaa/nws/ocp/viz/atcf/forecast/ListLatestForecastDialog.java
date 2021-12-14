/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

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

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Forecast => List Latest Forecast".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 14, 2020  84981      jwu          Initial creation
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class ListLatestForecastDialog extends OcpCaveSWTDialog {

    // Current storm.
    private Storm storm;

    // Dialog title with storm info.
    private String title;

    // Maximum radii.
    private static final int MAXIMUM_WIND_RADII = 995;

    private static final String NORTHWARD = "Northward";

    private StyledText dataInfoTxt;

    /**
     * Constructor
     *
     * @param parent
     */
    public ListLatestForecastDialog(Shell parent, Storm storm) {
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

        // StyledText to show current storm's latest forecast data.
        dataInfoComp.setLayout(dataInfoGL);
        dataInfoComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        dataInfoTxt = new StyledText(dataInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData dataInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataInfoGD.verticalIndent = 2;
        dataInfoGD.horizontalIndent = 5;
        dataInfoGD.widthHint = 690;
        dataInfoGD.heightHint = 800;

        dataInfoTxt.setLayoutData(dataInfoGD);
        dataInfoTxt.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        listLatestForecast(storm);
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
     * Display latest forecast info.
     *
     * @param storm
     */
    public void listLatestForecast(Storm storm) {

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

        // Forecasted storm direction & speed.
        ForecastTrackRecord frec = null;
        if (fcstTrackData != null && !fcstTrackData.isEmpty()) {
            for (List<ForecastTrackRecord> recs : fcstTrackData
                    .values()) {
                if (recs != null && !recs.isEmpty() && recs.get(0)
                        .getFcstHour() == AtcfTaus.TAU0.getValue()) {
                    frec = recs.get(0);
                    break;
                }
            }
        }

        String moving = String.format("Moving %3s degree () at %3s kts", "", "")
                + "\n";
        if (frec != null) {
            moving = (String.format("  Moving %3d degrees (%s) at %3d kts",
                    (int) frec.getStormDrct(),
                    getDirDescription(frec.getStormDrct()).toUpperCase(),
                    (int) frec.getStormSped())) + "\n";
        }

        String mfmt = "%" + (title.length() + 2) + "s";
        sbr.append(String.format(mfmt, moving)).append('\n');

        sbr.append(String.format("%5s%8s%8s%8s%20s", "Tau", "DTG", "Lat", "Lon",
                "Winds")).append('\n');

        StringBuilder stb = new StringBuilder();
        for (int ii = 0; ii < 86; ii++) {
            stb.append("-");
        }

        String sep = stb.toString();

        sbr.append(String.format("%86s", sep)).append('\n');

        // Forecast info for each DTG and each radii.
        for (Map.Entry<String, List<ForecastTrackRecord>> entry : fcstTrackData
                .entrySet()) {
            String dtg = entry.getKey();
            List<ForecastTrackRecord> recs = entry.getValue();

            if (recs != null && !recs.isEmpty()) {
                ForecastTrackRecord rec34 = recs.get(0);

                // Forecast location, and winds.
                int fcstHr = rec34.getFcstHour();
                float lat = rec34.getClat();
                float lon = rec34.getClon();

                String latStr = String.format("%4.1f%s", Math.abs(lat),
                        (lat < 0) ? "S" : "N");
                String lonStr = String.format("%5.1f%s", Math.abs(lon),
                        (lon > 0) ? "E" : "W");

                sbr.append(String.format(
                        "%4d%12s%7s%8s    Max Wind %3d kts Gusts to %3d kts",
                        fcstHr, dtg, latStr, lonStr, (int) rec34.getWindMax(),
                        (int) rec34.getGust())).append("\n\n");

                // Wind radii for 34/50/64kt.
                sbr.append(getWindRadiiInfo(rec34));

                if (recs.size() > 1) {
                    sbr.append(getWindRadiiInfo(recs.get(1)));
                }

                if (recs.size() > 2) {
                    sbr.append(getWindRadiiInfo(recs.get(2)));
                }

                // 12-ft wave radii.
                if (fcstHr == AtcfTaus.TAU0.getValue()
                        || fcstHr == AtcfTaus.TAU3.getValue()) {
                    sbr.append(getWaveRadiiInfo(rec34));
                }

                sbr.append(sep + "\n");
            }
        }

        dataInfoTxt.setText(sbr.toString());
    }

    /*
     * Get formatted wind radii information from a forecast track record.
     *
     * @ rec ForecastTrackRecord
     *
     * @return String
     */
    private String getWindRadiiInfo(ForecastTrackRecord rec) {
        StringBuilder sbr = new StringBuilder();
        int rd1 = (int) rec.getQuad2WindRad();
        int rd2 = (int) rec.getQuad2WindRad();
        int rd3 = (int) rec.getQuad3WindRad();
        int rd4 = (int) rec.getQuad4WindRad();
        if ((rd1 > 0 && rd1 < MAXIMUM_WIND_RADII)
                || (rd2 > 0 && rd2 < MAXIMUM_WIND_RADII)
                || (rd3 > 0 && rd3 < MAXIMUM_WIND_RADII)
                || (rd4 > 0 && rd4 < MAXIMUM_WIND_RADII)) {
            String radiiStr = String.format("Radius of %2d kt wind - ",
                    (int) rec.getRadWind());
            sbr.append(String.format("%58s%3d NM Northeast Quardant", radiiStr,
                    rd1)).append('\n');
            sbr.append(String.format("%61d NM Southeast Quardant", rd2))
                    .append('\n');
            sbr.append(String.format("%61d NM Southwest Quardant", rd3))
                    .append('\n');
            sbr.append(String.format("%61d NM Northwest Quardant", rd4))
                    .append('\n');
        }

        return sbr.toString();
    }

    /*
     * Get formatted wave radii information from a forecast track record.
     *
     * @ rec ForecastTrackRecord
     *
     * @return String
     */
    private String getWaveRadiiInfo(ForecastTrackRecord rec) {
        StringBuilder sbr = new StringBuilder();

        int rd1 = (int) rec.getQuad1WaveRad();
        int rd2 = (int) rec.getQuad2WaveRad();
        int rd3 = (int) rec.getQuad3WaveRad();
        int rd4 = (int) rec.getQuad4WaveRad();
        if ((rd1 > 0 && rd1 < MAXIMUM_WIND_RADII)
                || (rd2 > 0 && rd2 < MAXIMUM_WIND_RADII)
                || (rd3 > 0 && rd3 < MAXIMUM_WIND_RADII)
                || (rd4 > 0 && rd4 < MAXIMUM_WIND_RADII)) {

            String wave = String.format("%3s", "");
            if (rd1 >= 0 && rd1 <= MAXIMUM_WIND_RADII) {
                wave = String.format("%3d", rd1);
            }

            sbr.append(String.format("%58s%3s NM Northeast Quardant",
                    "Radius of 12 ft seas - ", wave)).append('\n');

            wave = String.format("%3s", "");
            if (rd2 >= 0 && rd2 <= MAXIMUM_WIND_RADII) {
                wave = String.format("%3d", rd2);
            }

            sbr.append(String.format("%61s NM Southeast Quardant", wave))
                    .append('\n');

            wave = String.format("%3s", "");
            if (rd3 >= 0 && rd3 <= MAXIMUM_WIND_RADII) {
                wave = String.format("%3d", rd3);
            }

            sbr.append(String.format("%61s NM Southwest Quardant", wave))
                    .append('\n');

            wave = String.format("%3s", "");
            if (rd4 >= 0 && rd4 <= MAXIMUM_WIND_RADII) {
                wave = String.format("%3d", rd4);
            }

            sbr.append(String.format("%61s NM Southwest Quardant", wave))
                    .append('\n');
        }

        return sbr.toString();
    }

    /**
     * Determine the description of a cardinal wind direction String.
     *
     * @param direction
     * @return String
     */
    private static String getDirDescription(float direction) {

        if (direction >= 0 && direction < 12) {
            return NORTHWARD;
        } else if (direction >= 12 && direction < 34) {
            return "North-Northeastward";
        } else if (direction >= 34 && direction < 57) {
            return "Northeastward";
        } else if (direction >= 57 && direction < 79) {
            return "East-Northeastward";
        } else if (direction >= 79 && direction < 102) {
            return "Eastward";
        } else if (direction >= 102 && direction < 124) {
            return "East-Southeastward";
        } else if (direction >= 124 && direction < 147) {
            return "Southeastward";
        } else if (direction >= 147 && direction < 169) {
            return "South-Southeastward";
        } else if (direction >= 169 && direction < 192) {
            return "Southward";
        } else if (direction >= 192 && direction < 214) {
            return "South-Southwestward";
        } else if (direction >= 214 && direction < 237) {
            return "Southwestward";
        } else if (direction >= 237 && direction < 259) {
            return "West-Southwestward";
        } else if (direction >= 259 && direction < 282) {
            return "Westward";
        } else if (direction >= 282 && direction < 304) {
            return "West-Northwestward";
        } else if (direction >= 304 && direction < 327) {
            return "Northwestward";
        } else if (direction >= 327 && direction < 349) {
            return "North-Northwestward";
        } else if (direction >= 349 && direction < 361) {
            return NORTHWARD;
        } else {
            return NORTHWARD;
        }
    }

}
