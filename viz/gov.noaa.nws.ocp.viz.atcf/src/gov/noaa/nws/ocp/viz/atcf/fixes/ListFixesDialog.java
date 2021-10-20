/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FixFormat;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RadarType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Fixes => List Fixes".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 14, 2020  85083      jwu          Initial creation
 * Nov 15, 2020  85197      jwu          Allow showing flagged fixes only.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class ListFixesDialog extends OcpCaveSWTDialog {

    // Current storm.
    private Storm storm;

    // Flag to show only "flagged" fixes.
    private boolean flagged;

    // Maximum wind direction, speed (intensity).
    private static final int MAX_WIND_SPEED = 250;

    // Maximum radii.
    private static final int MAXIMUM_WIND_RADII = 995;

    // Maximum characters for a fix comment.
    private static final int COMMENTS_LENGTH = 52;

    // Fix types for radar fixes.
    private static final String CONVENTIONAL_RADAR = "RDRC";

    private static final String DOPPLER_RADAR = "RDRD";

    private CCombo fmtCmb;

    private StyledText dataInfoTxt;

    /**
     * Constructor
     * 
     * @param parent
     * @param storm
     *            Storm
     * @param flagged
     *            true - only show flagged; false, show all.
     */
    public ListFixesDialog(Shell parent, Storm storm, boolean flagged) {
        super(parent);
        this.storm = storm;
        this.flagged = flagged;
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
     * Create a StyledText to list fixes data.
     *
     * @param parent
     */
    private void createDataInfoComp(Composite parent) {
        Composite dataInfoComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoGL = new GridLayout(1, false);
        dataInfoGL.verticalSpacing = 10;
        dataInfoComp.setLayout(dataInfoGL);
        dataInfoComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite fixFormatComp = new Composite(dataInfoComp, SWT.NONE);
        GridLayout fixFormatGL = new GridLayout(2, false);
        fixFormatGL.horizontalSpacing = 10;
        fixFormatComp.setLayout(fixFormatGL);
        fixFormatComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));

        Label fmtLbl = new Label(fixFormatComp, SWT.NONE);
        fmtLbl.setText("Fix Format:");
        fmtLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        fmtCmb = new CCombo(fixFormatComp, SWT.DROP_DOWN | SWT.BORDER);
        for (FixFormat fmt : FixFormat.values()) {
            fmtCmb.add(fmt.name().replace("_", " "));
        }
        fmtCmb.add("ALL", 0);
        fmtCmb.select(0);

        fmtCmb.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, false, false));
        fmtCmb.setEditable(false);
        fmtCmb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int sel = fmtCmb.getSelectionIndex();
                FixFormat fmt = null;
                if (sel > 0) {
                    fmt = FixFormat.values()[sel - 1];
                }

                listFixes(storm, flagged, fmt);
            }
        });

        // StyledText to show current storm's fixes data.
        dataInfoTxt = new StyledText(dataInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData dataInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataInfoGD.verticalIndent = 2;
        dataInfoGD.horizontalIndent = 5;
        dataInfoGD.widthHint = 1100;
        dataInfoGD.heightHint = 800;

        dataInfoTxt.setLayoutData(dataInfoGD);
        dataInfoTxt.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        listFixes(storm, flagged);
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
    private void setTitle() {

        StringBuilder titleBdr = new StringBuilder();
        titleBdr.append("Fixes for " + storm.getTcHLevel() + " "
                + storm.getStormName() + ", " + storm.getStormId());
        setText(titleBdr.toString());
    }

    /**
     * Display latest forecast info.
     *
     * @param storm
     */
    public void listFixes(Storm storm, boolean flagged) {
        this.storm = storm;
        this.flagged = flagged;

        fmtCmb.select(0);
        listFixes(storm, flagged, null);
    }

    /**
     * Display latest forecast info.
     *
     * @param storm
     */
    public void listFixes(Storm storm, boolean flagged, FixFormat fixFmt) {

        setTitle();

        // Get the current AtcfResource
        List<FDeckRecord> fixData = AtcfDataUtil
                .getFDeckRecords(storm, false);

        // Find the id of first fix record.
        int fid = 0;
        if (fixData != null && !fixData.isEmpty()) {
            fid = fixData.get(0).getId();
        }

        // Sort fix data by fix format.
        Map<FixFormat, List<FDeckRecord>> fixDataMap = groupFDeckDataByFormat(
                fixData, flagged);

        StringBuilder sbr = new StringBuilder();
        sbr.append(String.format("%80s\n", "FIXES for " + storm.getStormId()));

        FixFormat[] listFormats = FixFormat.values();
        if (fixFmt != null) {
            listFormats = new FixFormat[] { fixFmt };
        }

        // Display fixes by fix format.
        for (FixFormat fmt : listFormats) {

            List<FDeckRecord> recs = fixDataMap.get(fmt);

            if (fmt == FixFormat.RADAR) {
                sbr.append(listRadarFixes(recs, fid));
            } else {
                sbr.append(String.format("\n\n%80s\n",
                        fmt.name().replace('_', ' ') + " FIXES"));

                sbr.append(getFormatTitle(fmt, null));

                if (recs != null && !recs.isEmpty()) {

                    switch (fmt) {

                    case SUBJECTIVE_DVORAK:
                        sbr.append(getSubDvorkInfo(recs, fid));
                        break;
                    case OBJECTIVE_DVORAK:
                        sbr.append(getObjDvorkInfo(recs, fid));
                        break;
                    case AIRCRAFT:
                        sbr.append(getAircraftInfo(recs, fid));
                        break;
                    case ANALYSIS:
                        sbr.append(getAnalysisInfo(recs, fid));
                        break;
                    case DROPSONDE:
                        sbr.append(getDropsondeInfo(recs, fid));
                        break;
                    case MICROWAVE:
                    case SCATTEROMETER:
                        sbr.append(getMicrowaveOrScatterometerInfo(recs, fid));
                        break;
                    // case RADAR: handled separately.
                    default:
                        break;
                    }
                }
            }
        }

        dataInfoTxt.setText(sbr.toString());
    }

    /*
     * Group a list of FDeckRecord by FixFormat
     *
     * @param recs list of FDeckRecords
     *
     * @param flagged flag to only include "flagged" records.
     *
     * @return Map<FixFormat, List<FDeckRecord>>
     */
    private Map<FixFormat, List<FDeckRecord>> groupFDeckDataByFormat(
            List<FDeckRecord> recs, boolean flagged) {

        Map<FixFormat, List<FDeckRecord>> iniRecMap = new EnumMap<>(FixFormat.class);

        for (FixFormat fmt : FixFormat.values()) {
            iniRecMap.put(fmt, new ArrayList<>());
        }

        for (FDeckRecord rec : recs) {
            if (!flagged || rec.getFlaggedIndicator()
                    .toUpperCase().startsWith("F")) {
                FixFormat fixFmt = FixFormat
                        .getFixFormat(rec.getFixFormat());
                if (fixFmt != null) {
                    List<FDeckRecord> dataList = iniRecMap.get(fixFmt);
                    dataList.add(rec);
                }
            }
        }

        return iniRecMap;
    }

    /*
     * Get formatted title line for a FixFormat.
     *
     * @ fmt FixFormat
     * 
     * @ fixType FixType - Mainly for radar (RDRC, RDRD).
     *
     * @return String
     */
    private String getFormatTitle(FixFormat fmt, String fixType) {
        StringBuilder sbr = new StringBuilder();

        String head = String.format("%6s%6s%16s%8s", "NO.", "TIME",
                "FIX POSITION", "ACCRY");

        switch (fmt) {

        case SUBJECTIVE_DVORAK:
            sbr.append(String.format("\n%37s%16s%11s%10s%8s%6s%10s%10s\n", head,
                    "DVORAK CODE", "BIRD", "SENSOR", "CENTER", "SITE",
                    "INITIALS", "COMMENTS"));
            break;
        case OBJECTIVE_DVORAK:
            sbr.append(
                    String.format("\n%37s%4s%6s%9s%6s%6s%10s%7s%6s%10s%10s\n",
                            head, "CI", "TAvg", "AvgTime", "TRaw", "BIRD",
                            "SENSOR", "SCENE", "SITE", "INITIALS", "COMMENTS"));
            break;
        case AIRCRAFT:
            sbr.append(String.format(
                    "\n%43s%6s%6s%14s%17s%8s%6s%7s%13s%15s%7s\n", "FLT",
                    "700MB", "OBS", "MAX_SFC_WND", "MAX_FLT_LVL_WND", "ACCRY",
                    "EYE", "EYE", "EYE ORIEN-", "EYE TEMP(C)", "MSN"));
            sbr.append(String.format(
                    "%37s%6s%5s%8s%13s%16s%9s%6s%7s%13s%15s%5s%7s%10s\n", head,
                    "LVL", "HGT", "MSLP", "VEL/BRG/RNG", "DIR/VEL/BRG/RNG",
                    "NAV/MET", "CHAR", "SHAPE", "DIAM/TATION", "OUT/IN/DP/SST",
                    "NO", "INIT", "COMMENTS"));
            break;
        case ANALYSIS:
            sbr.append(String.format("\n%48s%12s%16s%9s%19s%16s\n", "START",
                    "END", "INTENSITY", "NEAREST", "OBSERVATION", "ANALYST"));
            sbr.append(String.format("%37s%10s%14s%14s%11s%15s%20s%10s%10s\n",
                    head, "TIME", "TIME", "ESTIMATE", "DATA(NM)", "SOURCE",
                    "INITIALS", "INITIALS", "COMMENTS"));
            break;
        case DROPSONDE:
            sbr.append(String.format("\n%43s%41s%16s%15s\n", "WIND", "LOWEST",
                    "150 M OF DROP", "0-500 M LAYER"));
            sbr.append(String.format(
                    "%37s%7s%6s%6s%6s%13s%10s%15s%15s%10s%10s\n", head, "SPEED",
                    "CONF", "MSLP", "CONF", "ENVIRONMENT", "MIDPOINT",
                    "MEAN_WIND_SPD", "MEAN_WIND_SPD", "INITIALS", "COMMENTS"));
            break;
        case MICROWAVE:
            sbr.append(String.format("\n%37s%8s%6s%17s%11s%8s%10s%10s\n", head,
                    "FXTYPE", "VMAX", "WIND RADII", "BIRD", "SITE", "INITIALS",
                    "COMMENTS"));
            break;
        case RADAR:
            if (fixType != null && CONVENTIONAL_RADAR.equals(fixType)) {
                sbr.append(String.format("\n%49s%6s\n", "EYE", "EYE"));
                sbr.append(String.format("%37s%7s%6s%6s%12s%16s%7s%10s%10s\n",
                        head, "RADAR", "SHAPE", "DIAM", "RADOB-CODE",
                        "SITE POSITION", "SITE", "INITIALS", "COMMENTS"));
            } else {
                sbr.append(String.format("\n%85s%7s\n", "EYE", "EYE"));
                sbr.append(String.format(
                        "%37s%7s%6s%7s%20s%10s%6s%16s%7s%10s%10s\n", head,
                        "RADAR", "VMAX", "ACCRY", "34 KT WIND RADII", "SHAPE",
                        "DIAM", "SITE POSITION", "SITE", "INITIALS",
                        "COMMENTS"));
            }
            break;
        case SCATTEROMETER:
            // TODO Part of microwave?
            sbr.append(String.format("\n%37s%8s%6s%17s%11s%8s%10s%10s\n", head,
                    "FXTYPE", "VMAX", "WIND RADII", "BIRD", "SITE", "INITIALS",
                    "COMMENTS"));
            break;
        default:
            break;
        }

        sbr.append("\n");

        return sbr.toString();
    }

    /*
     * Get the formatted info common in fix records.
     *
     * @param rec FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getCommInfo(FDeckRecord rec, int fid) {

        StringBuilder sbr = new StringBuilder();

        String dtg = AtcfDataUtil
                .calendarToLongDateTimeString(rec.getRefTimeAsCalendar());
        float lat = rec.getClat();
        if (Math.abs(lat) > 90.0) {
            lat = 0.0f;
        }

        float lon = rec.getClon();
        if (Math.abs(lon) > 180.0) {
            lon = 0.0f;
        }

        String latStr = String.format("%5.2f%s", Math.abs(lat),
                (lat < 0) ? "S" : "N");
        String lonStr = String.format("%6.2f%s", Math.abs(lon),
                (lon > 0) ? "E" : "W");

        // Position confidence
        String confStr = "CONF " + rec.getPositionConfidence();
        if (FixFormat.SUBJECTIVE_DVORAK.getValue().equals(rec.getFixFormat())) {
            confStr = "PCN " + rec.getPcnCode();
        }

        String idStr = String.format("%4d", (rec.getId() - fid));
        if (flagged) {
            idStr = "*F" + idStr;
        }
        sbr.append(String.format("%6s%8s%8s%8s%7s", idStr, dtg.substring(6),
                latStr, lonStr, confStr));

        return sbr.toString();
    }

    /*
     * Get the formatted info from subjective Dvorak (DVTS) fix records.
     *
     * @param recs list of FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getSubDvorkInfo(List<FDeckRecord> recs, int fid) {
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // TODO may need to be updated for Dvorak Code.
                // Subj. Dvorak specific info
                String dvkCode = rec.getDvorakCodeLongTermTrend();
                String dvkStr = "";
                if (dvkCode != null && dvkCode.length() >= 4) {
                    dvkStr = "T" + dvkCode.substring(0, 1) + "."
                            + dvkCode.substring(1, 2) + "/"
                            + dvkCode.substring(2, 3) + "."
                            + dvkCode.substring(3, 4) + " /    / HRS";
                }

                sbr.append(String.format("%21s%2s%-6s%2s%4s%8s%8s%7s%5s",
                        dvkStr, "", rec.getSatelliteType(), "",
                        rec.getSensorType(), rec.getCenterType(),
                        rec.getFixSite(), rec.getInitials(), ""));

                // Comments (max. 52 chars)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from objective Dvorak (DVTO) fix records.
     *
     * @param recs list of FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getObjDvorkInfo(List<FDeckRecord> recs, int fid) {
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // Obj. Dvorak specific info
                int ci = (int) rec.getCiNum();
                if (ci == (int) AbstractAtcfRecord.RMISSD) {
                    ci = 0;
                }
                String ciStr = String.format("%2.1f", ci / 10.0);

                int tAvg = (int) rec.gettNumAverage();
                if (tAvg == (int) AbstractAtcfRecord.RMISSD) {
                    tAvg = 0;
                }
                String tAvgStr = String.format("%2.1f", tAvg / 10.0);

                int tPd = (int) rec.gettNumAveragingTimePeriod();
                if (tPd == (int) AbstractAtcfRecord.RMISSD) {
                    tPd = 0;
                }
                String tPdStr = String.format("%d", tPd);

                String tRaw = rec.gettNumRaw();
                float tRawNum = 0.0f;
                try {
                    tRawNum = Float.parseFloat(tRaw) / 10;
                } catch (NumberFormatException e) {
                    // Use default 0.0
                }
                String tRawStr = String.format("%2.1f", tRawNum);

                sbr.append(String.format("%5s%5s%6s%8s%3s", ciStr, tAvgStr,
                        tPdStr, tRawStr, ""));

                sbr.append(String.format("%-6s%2s%4s%2s%6s%7s%7s%5s\n",
                        rec.getSatelliteType(), "", rec.getSensorType(), "",
                        rec.getSceneType(), rec.getFixSite(), rec.getInitials(),
                        ""));

                // Comments (max. 52 chars)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from a microwave or scatterometer fix record
     *
     * Note: Include SSMI/AMSR/WSAT/SSMS/TRMM/ASCT/GPMI/GPM/SATC/AMSU/OSCT
     * (depreciated --- ADOS, ALTI, ERS2, QSCT, SEAW).
     *
     * @param recs list of FDeckRecords
     *
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getMicrowaveOrScatterometerInfo(List<FDeckRecord> recs,
            int fid) {
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // Microwave specific info
                int maxWnd = (int) rec.getWindMax();
                if (maxWnd > MAX_WIND_SPEED) {
                    maxWnd = 0;
                }
                String wndStr = String.format("%4d", maxWnd);

                int rad1 = (int) rec.getWindRad1();
                if (rad1 > MAXIMUM_WIND_RADII) {
                    rad1 = 0;
                }

                int rad2 = (int) rec.getWindRad2();
                if (rad2 > MAXIMUM_WIND_RADII) {
                    rad2 = 0;
                }

                int rad3 = (int) rec.getWindRad3();
                if (rad3 > MAXIMUM_WIND_RADII) {
                    rad3 = 0;
                }

                int rad4 = (int) rec.getWindRad4();
                if (rad4 > MAXIMUM_WIND_RADII) {
                    rad4 = 0;
                }

                sbr.append(String.format(
                        "%7s%2s%4s%3s%4s%4d%4d%4d%4d%2s%-6s%2s%-4s%7s%5s",
                        rec.getFixType(), "", wndStr, "", rec.getWindCode(),
                        rad1, rad2, rad3, rad4, "", rec.getSatelliteType(), "",
                        rec.getFixSite(), rec.getInitials(), ""));

                // Comments (max. 52 chars)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from radar fix records (RDRC/RDRD, RDRT).
     *
     * @param recs list of FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String listRadarFixes(List<FDeckRecord> recs, int fid) {

        /*
         * Sort out conventional (RDRC) & non-conventional (RDRD etc.) fixes.
         */
        List<FDeckRecord> rdrcFixes = new ArrayList<>();
        List<FDeckRecord> rdrdFixes = new ArrayList<>();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                if (CONVENTIONAL_RADAR.equals(rec.getFixType())) {
                    rdrcFixes.add(rec);
                } else {
                    rdrdFixes.add(rec);
                }
            }
        }

        // Format conventional (RDRC) fixes
        StringBuilder sbr = new StringBuilder();
        sbr.append(String.format("\n\n%80s\n", "CONVENTIONAL RADAR FIXES"));

        sbr.append(getFormatTitle(FixFormat.RADAR, CONVENTIONAL_RADAR));
        sbr.append(getConventionalRadarInfo(rdrcFixes, fid));

        // Format doppler & other (RDRD) fixes
        sbr.append(String.format("\n\n%80s\n", "DOPPLER RADAR FIXES"));

        sbr.append(getFormatTitle(FixFormat.RADAR, DOPPLER_RADAR));

        sbr.append(getNonConventionalRadarInfo(rdrdFixes, fid));

        return sbr.toString();
    }

    /*
     * Get the formatted info from conventional radar fix records (RDRC).
     *
     * @param recs list of RDRC FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getConventionalRadarInfo(List<FDeckRecord> recs,
            int fid) {

        // Format conventional (RDRC) fixes
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // RDRC specific info
                String radarType = rec.getRadarType();
                RadarType rt = RadarType.getRadarType(radarType);
                if (rt != null) {
                    radarType = rt.getValue();
                }

                String eyeShape = rec.getEyeShape();

                int eyeDm = (int) rec.getEyeDiameterNM();
                String eyeDmStr = String.format("%4d", eyeDm);
                if (eyeDm == (int) AbstractAtcfRecord.RMISSD) {
                    eyeDmStr = String.format("%4d", 0);
                }

                String radob = rec.getRadobCode();

                float lat = rec.getRadarSitePosLat();
                if (Math.abs(lat) > 90.0) {
                    lat = 0.0f;
                }

                float lon = rec.getRadarSitePosLon();
                if (Math.abs(lon) > 180.0) {
                    lon = 0.0f;
                }

                String latStr = String.format("%5.2f%s", Math.abs(lat),
                        (lat < 0) ? "S" : "N");
                String lonStr = String.format("%6.2f%s", Math.abs(lon),
                        (lon > 0) ? "E" : "W");

                sbr.append(String.format("%6s%4s%3s%2s%2s%12s%8s%9s%6s%6s%6s",
                        radarType.toUpperCase(), eyeShape, "", eyeDmStr, "",
                        radob, latStr, lonStr, rec.getFixSite(),
                        rec.getInitials(), ""));

                // Comments (max. 52 chars)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from non-conventional radar fix records (RDRD
     * etc).
     *
     * @param recs list of non-conventional FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getNonConventionalRadarInfo(List<FDeckRecord> recs,
            int fid) {

        // Format non-conventional (RDRD etc) fixes
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // RDRD specific info
                String radarType = rec.getRadarType();
                RadarType rt = RadarType.getRadarType(radarType);
                if (rt != null) {
                    radarType = rt.getValue();
                }

                // Wind max & its confidence
                int maxWnd = (int) rec.getWindMax();
                if (maxWnd > MAX_WIND_SPEED) {
                    maxWnd = 0;
                }
                String wndStr = String.format("%4d", maxWnd);

                String confStr = "CONF " + rec.getWindMaxConfidence();

                String wndCode = rec.getWindCode();
                int rad1 = (int) rec.getWindRad1();
                if (rad1 > MAXIMUM_WIND_RADII) {
                    rad1 = 0;
                }

                int rad2 = (int) rec.getWindRad2();
                if (rad2 > MAXIMUM_WIND_RADII) {
                    rad2 = 0;
                }

                int rad3 = (int) rec.getWindRad3();
                if (rad3 > MAXIMUM_WIND_RADII) {
                    rad3 = 0;
                }

                int rad4 = (int) rec.getWindRad4();
                if (rad4 > MAXIMUM_WIND_RADII) {
                    rad4 = 0;
                }

                String eyeShape = rec.getEyeShape();

                int eyeDm = (int) rec.getEyeDiameterNM();
                String eyeDmStr = String.format("%4d", eyeDm);
                if (eyeDm == (int) AbstractAtcfRecord.RMISSD) {
                    eyeDmStr = String.format("%4d", 0);
                }

                float lat = rec.getRadarSitePosLat();
                if (Math.abs(lat) > 90.0) {
                    lat = 0.0f;
                }

                float lon = rec.getRadarSitePosLon();
                if (Math.abs(lon) > 180.0) {
                    lon = 0.0f;
                }

                String latStr = String.format("%5.2f%s", Math.abs(lat),
                        (lat < 0) ? "S" : "N");
                String lonStr = String.format("%6.2f%s", Math.abs(lon),
                        (lon > 0) ? "E" : "W");

                sbr.append(String.format(
                        "%6s%6s%3s%-6s%2s%4s%4d%4d%4d%4d%2s%3s%2s%4s%10s%9s%6s%7s%5s",
                        radarType, wndStr, "", confStr, "", wndCode, rad1, rad2,
                        rad3, rad4, "", eyeShape, "", eyeDmStr, latStr, lonStr,
                        rec.getFixSite(), rec.getInitials(), ""));

                // Comments (max. 52 chars)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from an aircraft fix record (AIRC).
     *
     * @param recs list of FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getAircraftInfo(List<FDeckRecord> recs, int fid) {
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // Aircraft specific info
                int flgLvlMb = (int) rec.getFlightLevelMillibars();
                String flgLvlStr = String.format("%3dMB", flgLvlMb);
                if (flgLvlMb == (int) AbstractAtcfRecord.RMISSD) {
                    flgLvlStr = String.format("%5d", 0);
                }

                int hgt700 = (int) rec.getFlightLevelMinimumHeightMeters();
                String hgtStr = String.format("%5d", hgt700);
                if (hgt700 == (int) AbstractAtcfRecord.RMISSD) {
                    hgtStr = String.format("%5d", 0);
                }

                int mslp = (int) rec.getMslp();
                String mslpStr = String.format("%4d", mslp);
                if (mslp == (int) AbstractAtcfRecord.RMISSD) {
                    mslpStr = String.format("%4d", 0);
                }

                sbr.append(
                        String.format("%7s%5s%6s", flgLvlStr, hgtStr, mslpStr));

                // Max surface wind
                int maxSfcWndInt = (int) rec
                        .getMaxSurfaceWindInboundLegIntensity();
                String maxSfcWndIntStr = String.format("%4d", maxSfcWndInt);
                if (maxSfcWndInt == (int) AbstractAtcfRecord.RMISSD) {
                    maxSfcWndIntStr = String.format("%4d", 0);
                }

                int maxSfcWndBrg = (int) rec
                        .getMaxSurfaceWindInboundLegBearing();
                String maxSfcWndBrgStr = String.format("%4d", maxSfcWndBrg);
                if (maxSfcWndBrg == (int) AbstractAtcfRecord.RMISSD) {
                    maxSfcWndBrgStr = String.format("%4d", 0);
                }

                int maxSfcWndRng = (int) rec
                        .getMaxSurfaceWindInboundLegRangeNM();
                String maxSfcWndRngStr = String.format("%4d", maxSfcWndRng);
                if (maxSfcWndRng == (int) AbstractAtcfRecord.RMISSD) {
                    maxSfcWndRngStr = String.format("%4d", 0);
                }

                sbr.append(String.format("%5s%4s%4s", maxSfcWndIntStr,
                        maxSfcWndBrgStr, maxSfcWndRngStr));

                // Maximum Flight Level Wind
                int maxFLDir = (int) rec.getMaxFLWindInboundDirection();
                String maxFLDirStr = String.format("%4d", maxFLDir);
                if (maxFLDir == (int) AbstractAtcfRecord.RMISSD) {
                    maxFLDirStr = String.format("%4d", 0);
                }

                int maxFLInt = (int) rec.getMaxFLWindInboundIntensity();
                String maxFLIntStr = String.format("%4d", maxFLInt);
                if (maxFLInt == (int) AbstractAtcfRecord.RMISSD) {
                    maxFLIntStr = String.format("%4d", 0);
                }

                int maxFLBrg = (int) rec.getMaxFLWindInboundBearing();
                String maxFLBrgStr = String.format("%4d", maxFLBrg);
                if (maxFLBrg == (int) AbstractAtcfRecord.RMISSD) {
                    maxFLBrgStr = String.format("%4d", 0);
                }

                int maxFLRng = (int) rec.getMaxFLWindInboundDirection();
                String maxFLRngStr = String.format("%4d", maxFLRng);
                if (maxFLRng == (int) AbstractAtcfRecord.RMISSD) {
                    maxFLRngStr = String.format("%4d", 0);
                }

                sbr.append(String.format("%6s%4s%4s%4s", maxFLDirStr,
                        maxFLIntStr, maxFLBrgStr, maxFLRngStr));

                // Accuracy - Navigational/Meteorological (tenths of nm)
                int navAcc = (int) rec.getAccuracyNavigational();
                if (navAcc == (int) AbstractAtcfRecord.RMISSD) {
                    navAcc = 0;
                }
                String navAccStr = String.format("%2.1f", navAcc / 10.0);

                int navMet = (int) rec.getAccuracyMeteorological();
                if (navMet == (int) AbstractAtcfRecord.RMISSD) {
                    navMet = 0;
                }
                String navMetStr = String.format("%2.1f", navMet / 10.0);

                String eyeChar = rec.getEyeCharacterOrWallCloudThickness();
                String eyeShape = rec.getEyeShape();

                int eyeDial = (int) rec.getEyeDiameterNM();
                if (eyeDial == (int) AbstractAtcfRecord.RMISSD) {
                    eyeDial = 0;
                }

                int eyeOrient = (int) rec.getEyeOrientation();
                if (eyeOrient == (int) AbstractAtcfRecord.RMISSD) {
                    eyeOrient = 0;
                }

                sbr.append(String.format("%5s%5s%5s%5s%8d%5d", navAccStr,
                        navMetStr, eyeChar, eyeShape, eyeDial, eyeOrient));

                // Temperature (outside/inside of eye, dew point, sea surface.
                int tempOut = (int) rec.getTempOutsideEye();
                if (tempOut == (int) AbstractAtcfRecord.RMISSD) {
                    tempOut = 0;
                }

                int tempIn = (int) rec.getTempInsideEye();
                if (tempIn == (int) AbstractAtcfRecord.RMISSD) {
                    tempIn = 0;
                }

                int tempDp = (int) rec.getDewPointTemp();
                if (tempDp == (int) AbstractAtcfRecord.RMISSD) {
                    tempDp = 0;
                }

                int tempSST = (int) rec.getSeaSurfaceTemp();
                if (tempSST == (int) AbstractAtcfRecord.RMISSD) {
                    tempSST = 0;
                }

                sbr.append(String.format("%6d%4d%4d%3d", tempOut, tempIn,
                        tempDp, tempSST));

                sbr.append(String.format("%4s%7s%3s", rec.getMissionNumber(),
                        rec.getInitials(), ""));

                // Comments (52 char)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from a dropsonde fix record (DRPS).
     *
     * @param recs list of FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getDropsondeInfo(List<FDeckRecord> recs, int fid) {
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // Dropsonde specific info
                int maxWnd = (int) rec.getWindMax();
                if (maxWnd > MAX_WIND_SPEED) {
                    maxWnd = 0;
                }
                String wndStr = String.format("%4d", maxWnd);

                String confStr = rec.getWindMaxConfidence();

                int mslp = (int) rec.getMslp();
                String mslpStr = String.format("%4d", mslp);
                if (mslp == (int) AbstractAtcfRecord.RMISSD) {
                    mslpStr = String.format("%4d", 0);
                }

                String mslpConf = rec.getPressureConfidence();

                String env = rec.getSondeEnvironment();

                int midP = (int) rec.getHeightMidpointLowest150m();
                if (midP == (int) AbstractAtcfRecord.RMISSD) {
                    midP = 0;
                }
                String midPStr = String.format("%3d", midP);

                int spd = (int) rec.getSpeedMeanWindLowest150mKt();
                if (spd == (int) AbstractAtcfRecord.RMISSD) {
                    spd = 0;
                }
                String spdStr = String.format("%3d", spd);

                int spd500 = (int) rec.getSpeedMeanWind0to500mKt();
                if (spd500 == (int) AbstractAtcfRecord.RMISSD) {
                    spd500 = 0;
                }
                String spd500Str = String.format("%3d", spd500);

                sbr.append(String.format(
                        "%6s%4s%-2s%6s%4s%2s%12s%9s%12s%15s%13s%5s", wndStr, "",
                        confStr, mslpStr, "", mslpConf, env, midPStr, spdStr,
                        spd500Str, rec.getInitials(), ""));

                // Comments (52 char)
                sbr.append(getComments(rec)).append('\n');

            }
        }

        return sbr.toString();
    }

    /*
     * Get the formatted info from an analysis fix record (ANAL).
     *
     * @param recs list of FDeckRecords
     * 
     * @param fid the record id of the first FDeckRecord
     *
     * @return String
     */
    private String getAnalysisInfo(List<FDeckRecord> recs, int fid) {
        StringBuilder sbr = new StringBuilder();
        if (recs != null && !recs.isEmpty()) {
            for (FDeckRecord rec : recs) {
                // Common info
                sbr.append(getCommInfo(rec, fid));

                // Analysis specific info
                Calendar sCal = TimeUtil.newGmtCalendar(rec.getStartTime());
                String stime = "";
                if (sCal != null) {
                    stime = AtcfDataUtil.calendarToLongDateTimeString(sCal);
                }

                Calendar eCal = TimeUtil.newGmtCalendar(rec.getEndTime());
                String etime = "";
                if (eCal != null) {
                    etime = AtcfDataUtil.calendarToLongDateTimeString(eCal);
                }

                sbr.append(String.format("%14s%14s", stime, etime));

                // TODO "Intensity Estimate"?
                int intEst = 0;

                int ndis = (int) rec.getDistanceToNearestDataNM();
                if (ndis == (int) AbstractAtcfRecord.RMISSD) {
                    ndis = 0;
                }

                sbr.append(String.format("%8d%10d%5s", intEst, ndis, ""));

                // Observation source (max. 24 chars)
                String obsSrc = rec.getObservationSources();
                sbr.append(String.format("%-24s%6s%5s%5s%5s", obsSrc,
                        rec.getAnalysisInitials(), "", rec.getInitials(), ""));

                // Comments (max. 52 chars)
                sbr.append(getComments(rec)).append('\n');
            }
        }

        return sbr.toString();
    }

    /*
     * Format "comments" in a FDeckRecord (max 52 char).
     *
     * @param rec FDeckRecord
     *
     * @return String
     */
    private String getComments(FDeckRecord rec) {

        // Comments (max. 52 chars)
        String comments = rec.getComments();
        String comStr = " ";
        if (comments != null && !comments.isEmpty()) {
            comStr = comments.substring(0,
                    Math.min(comments.length(), COMMENTS_LENGTH));
        }

        return comStr + "  ";
    }

}
