/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAGeneratorConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.WeatherType;

/**
 * Abstract class for CWA composites
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 02/02/2016  17469    wkwock      Initial creation
 * 06/27/2021  92561    wkwock      Correct end time calculation and remove 'Z' from the 'until' line
 * 09/10/2021  28802    wkwock      Use new configuration format
 * 
 * </pre>
 * 
 * @author wkwock
 */
public abstract class AbstractCWAComp extends Composite {
    public static final String MOV_LTL = "MOV LTL";

    public static final String FINAL_LINE = "\n\n= \n";

    /** start time check button */
    private Button startTimeChk;

    /** day Spinner */
    private Spinner daySpinner;

    /** start time */
    private HourMinuteComp startTime;

    private HourMinuteComp endTime;

    /** horizontal indent */
    protected static final int HORIZONTAL_INDENT = 20;

    /** date format */
    private SimpleDateFormat sdfDate = new SimpleDateFormat("ddHHmm");

    /** base calendar */
    private Calendar baseCalendar = TimeUtil.newGmtCalendar();

    protected WeatherType weatherType;

    protected CWAGeneratorConfig cwaConfigs;

    /**
     * Constructor
     * 
     * @param parent
     * @param style
     */
    protected AbstractCWAComp(Composite parent, int style,
            CWAGeneratorConfig cwaConfigs) {
        super(parent, style);
        this.cwaConfigs = cwaConfigs;
        sdfDate.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * create the start time composite
     */
    protected void createTimeComp() {
        Composite timeComp = new Composite(this, SWT.NONE);
        timeComp.setLayout(new GridLayout(7, false));
        // Start time
        startTimeChk = new Button(timeComp, SWT.CHECK);
        startTimeChk.setText("Start Time:");
        daySpinner = new Spinner(timeComp, SWT.BORDER);
        daySpinner.setMinimum(1);
        daySpinner.setMaximum(
                baseCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        daySpinner.setSelection(baseCalendar.get(Calendar.DAY_OF_MONTH));
        GridData dayGd = new GridData();
        daySpinner.setTextLimit(2);
        daySpinner.setLayoutData(dayGd);

        startTime = new HourMinuteComp(timeComp, TimeUtil.GMT_TIME_ZONE);
        Label zLbl = new Label(timeComp, SWT.NONE);
        zLbl.setText("Z");

        // Duration
        Label durationLbl = new Label(timeComp, SWT.NONE);
        durationLbl.setText("End Time:");
        GridData durationGd = new GridData();
        durationGd.horizontalIndent = HORIZONTAL_INDENT;
        durationLbl.setLayoutData(durationGd);

        endTime = new HourMinuteComp(timeComp, TimeUtil.GMT_TIME_ZONE);
        Calendar endCal = TimeUtil.newGmtCalendar();
        if (endCal.get(Calendar.MINUTE) > 30) {
            endCal.add(Calendar.HOUR_OF_DAY, 2);
        } else {
            endCal.add(Calendar.HOUR_OF_DAY, 1);
        }
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);
        endTime.setTime(endCal.getTime());

        Label z2Lbl = new Label(timeComp, SWT.NONE);
        z2Lbl.setText("Z");

    }

    /**
     * get date time
     * 
     * @return Date
     */
    public Date getDateTime() {
        Calendar calendar = TimeUtil.newGmtCalendar();

        if (startTimeChk.getSelection()) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar = (Calendar) baseCalendar.clone();
            if (day == maxDay && daySpinner.getSelection() == 1) {
                // This must be for next month
                calendar.add(Calendar.MONTH, 1);
            }
            calendar.set(Calendar.DAY_OF_MONTH, daySpinner.getSelection());
            Calendar tmpCal = TimeUtil.newGmtCalendar(startTime.getTime());
            calendar.set(Calendar.HOUR_OF_DAY,
                    tmpCal.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, tmpCal.get(Calendar.MINUTE));
        }

        return calendar.getTime();
    }

    public boolean getStartTimeChk() {
        return startTimeChk.getSelection();
    }

    public void setStartTimeChk(boolean selected) {
        startTimeChk.setSelection(selected);
    }

    /**
     * get start time
     * 
     * @return start time
     */
    protected String getStartTime() {
        return sdfDate.format(getDateTime());
    }

    /**
     * get end time
     * 
     * @return end time
     */
    protected String getEndTime() {
        Date startCal = getDateTime();
        Calendar endCal = TimeUtil.newGmtCalendar(endTime.getTime());
        if (endCal.before(startCal)) {
            endCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return sdfDate.format(endCal.getTime());
    }

    /**
     * get header lines
     * 
     * @param wmoId
     * @return header lines
     */
    protected String getHeaderLines(String wmoId, String header,
            boolean isCor) {
        String startDateTime = getStartTime();
        StringBuilder headerLines = new StringBuilder();
        headerLines.append(wmoId).append(" ").append(startDateTime)
                .append(" \n");
        headerLines.append(header).append(" ").append(startDateTime);
        if (isCor) {
            headerLines.append(" COR");
        }
        headerLines.append(" \n");

        return headerLines.toString();
    }

    /**
     * get the valid line
     * 
     * @param cwsuId
     * @param endDateTime
     * @param seriesId
     * @return the valid line
     */
    protected String getValidLine(String cwsuId, String endDateTime,
            int seriesId) {
        StringBuilder validLine = new StringBuilder();
        validLine.append(cwsuId).append(" CWA ").append(seriesId)
                .append(" VALID UNTIL ").append(endDateTime).append(" \n");

        return validLine.toString();
    }

    /**
     * Create text product
     * 
     * @param wmoId
     * @param header
     * @param fromline
     * @param body
     * @param cwsuId
     * @param productId
     * @param isCor
     * @return
     */
    protected abstract String createText(String wmoId, String header,
            String fromline, String body, String cwsuId, String productId,
            boolean isCor, boolean isOperational, DrawingType type,
            double width, String stateIds);

    public abstract AbstractCWANewConfig getConfig();

    public String getWeatherName() {
        return weatherType.getName();
    }

    public void updateProductConfig(AbstractCWANewConfig config)
            throws ParseException {
        setStartTimeChk(config.isStartTimeChk());
        if (config.isStartTimeChk()) {
            SimpleDateFormat sdf = new SimpleDateFormat("ddHHmm");
            Date d = sdf.parse(config.getStartTime());
            Calendar tmpCal = TimeUtil.newGmtCalendar(d);
            startTime.setTime(tmpCal.getTime());
            daySpinner.setSelection(tmpCal.get(Calendar.DAY_OF_MONTH));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        Date d = sdf.parse(config.getEndTime().substring(2, 6));

        endTime.setTime(d);
    }
}
