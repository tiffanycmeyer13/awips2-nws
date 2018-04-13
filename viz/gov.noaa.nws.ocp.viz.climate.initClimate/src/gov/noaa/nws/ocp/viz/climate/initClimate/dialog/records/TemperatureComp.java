/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * Monthly/Seasonal/Annual temperature composite in the "MONTHLY NORMALS AND
 * EXTREMES" window
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ---------- --------- ----------- --------------------------
 * 05/03/2016  18469    wkwock      Initial creation
 * 10/27/2016  20635    wkwock      Clean up
 * 27 DEC 2016 22450    amoore      Init should use common listeners where possible.
 * 12 JAN 2017 26406    wkwock      Make temperature records editable.
 * 02 MAR 2017 29576    wkwock      Add month(s) validation.
 * 20 JUN 2017 35324    amoore      Month-day only date component was not saving or providing access to
 *                                  years. New functionality for component exposing a new date field
 *                                  with all date info.
 * 03 JUL 2017 35694    amoore      Alter to take into account new {@link DateSelectionComp} API.
 * 21 SEP 2017 38124    amoore      Use better abstraction logic for valid months.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class TemperatureComp extends AbstractClimateInitComp {
    /** max temperature text */
    private Text maxTxt;

    /** average temperature text */
    private Text averageTxt;

    /** Days with Maximum Temperature GE 90Â°F text */
    private Text max90Txt;

    /** Days with Maximum Temperature GE 32Â°F text */
    private Text max32Txt;

    /** minimum temperature text */
    private Text minTxt;

    /** mean average temperature text */
    private Text mavgMinTxt;

    /** Days with Minimum Temperature GE 32Â°F text */
    private Text min32Txt;

    /** Days with Minimum Temperature GE 0Â°F text */
    private Text min0Txt;

    /** mean temperature text */
    private Text meanTmpTxt;

    /** max record value text */
    private Text maxRecordTxt;

    /** date 1 max record */
    private DateSelectionComp date1MaxRecordDate;

    /** date 2 max record */
    private DateSelectionComp date2MaxRecordDate;

    /** date 3 max record */
    private DateSelectionComp date3MaxRecordDate;

    /** minimum record value text */
    private Text minRecordValueTxt;

    /** date 1 minimum record */
    private DateSelectionComp date1MinRecordDate;

    /** date 2 minimum record */
    private DateSelectionComp date2MinRecordDate;

    /** date 3 minimum record */
    private DateSelectionComp date3MinRecordDate;

    /**
     * Constructor.
     * 
     * @param parent
     * @param changeListener
     */
    protected TemperatureComp(Composite parent,
            UnsavedChangesListener changeListener) {
        super(parent, SWT.NONE, changeListener);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 5;
        gl.horizontalSpacing = 20;
        setLayout(gl);

        // prepare boldFont.
        FontData fontData = getShell().getFont().getFontData()[0];
        boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.BOLD));
        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                boldFont.dispose();
            }
        });

        createTempNormalControls();
        createTempRecordControls();
    }

    /**
     * create temperature normal controls
     * 
     * @return
     */
    private Group createTempNormalControls() {
        Group normalGrp = new Group(this, SWT.SHADOW_IN);
        normalGrp.setText("Temperature Normals");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        normalGrp.setLayout(gl);
        normalGrp.setFont(boldFont);

        Label maxLbl = new Label(normalGrp, SWT.LEFT);
        maxLbl.setText("Maximum\nTemperature (°F)");
        maxTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        maxTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        maxTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        maxTxt.addListener(SWT.Modify, changesListener);

        Label avgMaxLbl = new Label(normalGrp, SWT.LEFT);
        avgMaxLbl.setText("Average Maximum\nTemperature (°F)");
        averageTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        averageTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        averageTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        averageTxt.addListener(SWT.Modify, changesListener);

        Label max90Lbl = new Label(normalGrp, SWT.LEFT);
        max90Lbl.setText("Days with Maximum\nTemperature GE 90°F");
        GridData gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        max90Lbl.setLayoutData(gd);
        max90Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        max90Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        max90Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        max90Txt.addListener(SWT.Modify, changesListener);

        Label max32Lbl = new Label(normalGrp, SWT.LEFT);
        max32Lbl.setText("Days with Maximum\nTemperature LE 32°F");
        max32Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        max32Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        max32Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        max32Txt.addListener(SWT.Modify, changesListener);

        Label minLbl = new Label(normalGrp, SWT.LEFT);
        minLbl.setText("Minimum\nTemperature (°F)");
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        minLbl.setLayoutData(gd);
        minTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        minTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        minTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        minTxt.addListener(SWT.Modify, changesListener);

        Label avgMinLbl = new Label(normalGrp, SWT.LEFT);
        avgMinLbl.setText("Average Minimum\nTemperature (°F)");
        mavgMinTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        mavgMinTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        mavgMinTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        mavgMinTxt.addListener(SWT.Modify, changesListener);

        Label min32Lbl = new Label(normalGrp, SWT.LEFT);
        min32Lbl.setText("Days with Minimum\nTemperature LE 32°F");
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        min32Lbl.setLayoutData(gd);
        min32Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        min32Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        min32Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        min32Txt.addListener(SWT.Modify, changesListener);

        Label min0Lbl = new Label(normalGrp, SWT.LEFT);
        min0Lbl.setText("Days with Minimum\nTemperature LE 0°F");
        min0Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        min0Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        min0Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        min0Txt.addListener(SWT.Modify, changesListener);

        Label meanTmpLbl = new Label(normalGrp, SWT.LEFT);
        meanTmpLbl.setText("Mean\nTemperature (°F)");
        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.verticalIndent = 10;
        meanTmpLbl.setLayoutData(gd);
        meanTmpTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        meanTmpTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempPeriodAverageListener());
        meanTmpTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempPeriodAverageListener());
        meanTmpTxt.addListener(SWT.Modify, changesListener);

        return normalGrp;
    }

    /**
     * create temperature record controls
     * 
     * @return
     */
    private Group createTempRecordControls() {
        Group recordGrp = new Group(this, SWT.SHADOW_IN | SWT.TOP);
        recordGrp.setText("Temperature Records");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        recordGrp.setLayout(gl);
        recordGrp
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        recordGrp.setFont(boldFont);

        Label maxRecordLbl = new Label(recordGrp, SWT.LEFT);
        maxRecordLbl.setText("Maximuim Temperature (°F)");
        maxRecordTxt = new Text(recordGrp, SWT.BORDER);
        maxRecordTxt.addListener(SWT.Modify, changesListener);

        Label date1MaxRecordLbl = new Label(recordGrp, SWT.LEFT);
        date1MaxRecordLbl.setText("Date 1 Maximum\nTemperature Observed");
        date1MaxRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        date1MaxRecordDate.addListener(SWT.Modify, changesListener);

        Label date2MaxRecordLbl = new Label(recordGrp, SWT.LEFT);
        date2MaxRecordLbl.setText("Date 2 Maximum\nTemperature Observed");
        date2MaxRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        date2MaxRecordDate.addListener(SWT.Modify, changesListener);

        Label date3MaxRecordLbl = new Label(recordGrp, SWT.LEFT);
        date3MaxRecordLbl.setText("Date 3 Maximum\nTemperature Observed");
        date3MaxRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        date3MaxRecordDate.addListener(SWT.Modify, changesListener);

        Label minRecordLbl = new Label(recordGrp, SWT.LEFT);
        minRecordLbl.setText("Minimum Temperature (°F)");
        minRecordValueTxt = new Text(recordGrp, SWT.BORDER);
        minRecordValueTxt.addListener(SWT.Modify, changesListener);

        Label date1MinRecordLbl = new Label(recordGrp, SWT.LEFT);
        date1MinRecordLbl.setText("Date 1 Minimum\nTemperature Observed");
        date1MinRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        date1MinRecordDate.addListener(SWT.Modify, changesListener);

        Label date2MinRecordLbl = new Label(recordGrp, SWT.LEFT);
        date2MinRecordLbl.setText("Date 2 Minimum\nTemperature Observed");
        date2MinRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        date2MinRecordDate.addListener(SWT.Modify, changesListener);

        Label date3MinRecordLbl = new Label(recordGrp, SWT.LEFT);
        date3MinRecordLbl.setText("Date 3 Minimum\nTemperature Observed");
        date3MinRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        date3MinRecordDate.addListener(SWT.Modify, changesListener);

        return recordGrp;
    }

    /**
     * display temperature information
     * 
     * @param record
     */
    public void displayTemperatureInfo(PeriodClimo record) {
        this.periodRcd = record;
        changesListener.setIgnoreChanges(true);

        maxTxt.setText(Float.toString(record.getMaxTempNorm()));
        averageTxt.setText(Float.toString(record.getNormMeanMaxTemp()));
        max90Txt.setText(Integer
                .toString(ClimateUtilities.nint(record.getNormNumMaxGE90F())));
        max32Txt.setText(Integer
                .toString(ClimateUtilities.nint(record.getNormNumMaxLE32F())));
        minTxt.setText(Float.toString(record.getMinTempNorm()));
        mavgMinTxt.setText(Float.toString(record.getNormMeanMinTemp()));
        min32Txt.setText(Float.toString(record.getNormNumMinLE32F()));
        min0Txt.setText(Float.toString(record.getNormNumMinLE0F()));
        meanTmpTxt.setText(Float.toString(record.getNormMeanTemp()));

        maxRecordTxt.setText(Integer.toString(record.getMaxTempRecord()));
        date1MaxRecordDate.setDate(record.getDayMaxTempRecordList().get(0));
        date2MaxRecordDate.setDate(record.getDayMaxTempRecordList().get(1));
        date3MaxRecordDate.setDate(record.getDayMaxTempRecordList().get(2));

        minRecordValueTxt.setText(Integer.toString(record.getMinTempRecord()));
        date1MinRecordDate.setDate(record.getDayMinTempRecordList().get(0));
        date2MinRecordDate.setDate(record.getDayMinTempRecordList().get(1));
        date3MinRecordDate.setDate(record.getDayMinTempRecordList().get(2));

        changesListener.setChangesUnsaved(false);
        changesListener.setIgnoreChanges(false);
    }

    /**
     * get temperature information from GUI
     * 
     * @return
     */
    public PeriodClimo getTemperatureInfo() {
        periodRcd.setMaxTempNorm(Float.parseFloat(maxTxt.getText()));
        periodRcd.setNormMeanMaxTemp(Float.parseFloat(averageTxt.getText()));
        periodRcd.setNormNumMaxGE90F(Float.parseFloat(max90Txt.getText()));
        periodRcd.setNormNumMaxLE32F(Float.parseFloat(max32Txt.getText()));
        periodRcd.setMinTempNorm(Float.parseFloat(minTxt.getText()));
        periodRcd.setNormMeanMinTemp(Float.parseFloat(mavgMinTxt.getText()));
        periodRcd.setNormNumMinLE32F(Float.parseFloat(min32Txt.getText()));
        periodRcd.setNormNumMinLE0F(Float.parseFloat(min0Txt.getText()));
        periodRcd.setNormMeanTemp(Float.parseFloat(meanTmpTxt.getText()));

        periodRcd.setMaxTempRecord(
                Integer.parseInt(this.maxRecordTxt.getText()));

        List<ClimateDate> cds = periodRcd.getDayMaxTempRecordList();
        ClimateDate maxRec1 = date1MaxRecordDate.getDate();
        cds.set(0, maxRec1);

        ClimateDate maxRec2 = date2MaxRecordDate.getDate();
        cds.set(1, maxRec2);

        ClimateDate maxRec3 = date3MaxRecordDate.getDate();
        cds.set(2, maxRec3);

        periodRcd.setMinTempRecord(
                Integer.parseInt(minRecordValueTxt.getText()));

        cds = periodRcd.getDayMinTempRecordList();
        ClimateDate minRec1 = date1MinRecordDate.getDate();
        cds.set(0, minRec1);

        ClimateDate minRec2 = date2MinRecordDate.getDate();
        cds.set(1, minRec2);

        ClimateDate minRec3 = date3MinRecordDate.getDate();
        cds.set(2, minRec3);

        return periodRcd;
    }

    @Override
    protected void setDateSelectionValidMonths() {
        int[] months = getValidMonths();

        date1MaxRecordDate.setValidMonths(months);
        date2MaxRecordDate.setValidMonths(months);
        date3MaxRecordDate.setValidMonths(months);
        date1MinRecordDate.setValidMonths(months);
        date2MinRecordDate.setValidMonths(months);
        date3MinRecordDate.setValidMonths(months);
    }
}
