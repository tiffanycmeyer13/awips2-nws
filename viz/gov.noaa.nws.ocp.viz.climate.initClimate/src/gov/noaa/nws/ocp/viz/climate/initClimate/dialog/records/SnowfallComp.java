/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * This class display the snow fall composite in the "MONTHLY NORMALS AND
 * EXTREMES" dialog for init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/06/2016  18469    wkwock      Initial creation
 * 10/27/2016  20635    wkwock      Clean up
 * 27 DEC 2016 22450    amoore      Init should use common listeners where possible.
 * 12 JAN 2017 26411    wkwock      Merge Modify and Add button to Save button
 * 02 MAR 2017 29576    wkwock      Add month(s) validation.
 * 15 JUN 2017 35187    amoore      Handle trace symbol in text box. Fix bug where
 *                                  total snow would not be saved from user's input.
 * 20 JUN 2017 35324    amoore      Month-day only date component was not saving or providing access to
 *                                  years. New functionality for component exposing a new date field
 *                                  with all date info.
 * 03 JUL 2017 35694    amoore      Alter to take into account new {@link DateSelectionComp} API.
 * 21 SEP 2017 38124    amoore      Use better abstraction logic for valid months.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */

public class SnowfallComp extends AbstractClimateInitComp {
    /** total snow fall text */
    private Text totalTxt;

    /** water equivalent text */
    private Text waterTxt;

    /** snow depth text */
    private Text snowDepthTxt;

    /** any text */
    private Text anyTxt;

    /** Days with snow fall >= 1.0 inch text */
    private Text days10Txt;

    /** total record text */
    private Text totalRecordTxt;

    /** date 1 total text */
    private Text date1TotalTxt;

    /** date 2 total text */
    private Text date2TotalTxt;

    /** date 3 total text */
    private Text date3TotalTxt;

    /** 24 hour total text */
    private Text hour24TotalTxt;

    /** date 1 begin date selector */
    private DateSelectionComp date1BeginDS;

    /** date 1 end date selector */
    private DateSelectionComp date1EndDS;

    /** date 1 year text */
    private Text date1YearTxt;

    /** date 2 begin date selector */
    private DateSelectionComp date2BeginDS;

    /** date 2 end date selector */
    private DateSelectionComp date2EndDS;

    /** date 2 year text */
    private Text date2YearTxt;

    /** date 3 begin date selector */
    private DateSelectionComp date3BeginDS;

    /** date 3 end date selector */
    private DateSelectionComp date3EndDS;

    /** date 3 year text */
    private Text date3YearTxt;

    /** snow depth record text */
    private Text snowDepthRecordTxt;

    /** date 1 depth date selector */
    private DateSelectionComp date1DepthDS;

    /** date 1 depth date selector */
    private DateSelectionComp date2DepthDS;

    /** date 1 depth date selector */
    private DateSelectionComp date3DepthDS;

    /**
     * Constructor.
     * 
     * @param parent
     * @param changeListener
     */
    protected SnowfallComp(Composite parent,
            UnsavedChangesListener changeListener) {
        super(parent, SWT.NONE, changeListener);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 5;
        gl.horizontalSpacing = 20;
        setLayout(gl);

        createSnowNormalControls();
        createSnowRecordControls();
    }

    /**
     * create snow normal controls
     * 
     * @return
     */
    private Group createSnowNormalControls() {
        Group normalGrp = new Group(this, SWT.SHADOW_IN);
        normalGrp.setText("Snowfall Normals");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        normalGrp.setLayout(gl);
        normalGrp
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        normalGrp.setFont(boldFont);

        Label totalLbl = new Label(normalGrp, SWT.LEFT);
        totalLbl.setText("Total\nSnowfall (inches)");
        totalTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        totalTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        totalTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        totalTxt.addListener(SWT.Modify, changesListener);

        Label waterLbl = new Label(normalGrp, SWT.LEFT);
        waterLbl.setText("Total Water\nEquivalent (inches)");
        waterTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        waterTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        waterTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        waterTxt.addListener(SWT.Modify, changesListener);

        Label snowDepthLbl = new Label(normalGrp, SWT.LEFT);
        snowDepthLbl.setText("Snow Depth\n(inches)");
        snowDepthTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        snowDepthTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowDepthListener());
        snowDepthTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowDepthListener());
        snowDepthTxt.addListener(SWT.Modify, changesListener);

        Label anyLbl = new Label(normalGrp, SWT.LEFT);
        anyLbl.setText("Days with any\nSnowfall");
        anyTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        anyTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        anyTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        anyTxt.addListener(SWT.Modify, changesListener);

        Label days10Lbl = new Label(normalGrp, SWT.LEFT);
        days10Lbl.setText("Days with Snowfall\nGE 1.0 inch");
        days10Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        days10Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        days10Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        days10Txt.addListener(SWT.Modify, changesListener);

        return normalGrp;
    }

    /**
     * Create the snow fall records display
     * 
     * @return the snowfall records group display
     */
    private Group createSnowRecordControls() {
        Group recordGrp = new Group(this, SWT.SHADOW_IN);
        recordGrp.setText("Snowfall Records");
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        recordGrp.setLayout(gl);
        recordGrp.setFont(boldFont);

        // the snow fall total text and labels
        Composite sfTotalComp = new Composite(recordGrp, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        sfTotalComp.setLayout(gl);

        Label totalLbl = new Label(sfTotalComp, SWT.LEFT);
        totalLbl.setText("Total\nSnowfall (inches)");
        totalRecordTxt = new Text(sfTotalComp, SWT.LEFT | SWT.BORDER);
        totalRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        totalRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        totalRecordTxt.addListener(SWT.Modify, changesListener);

        Label date1TotalLbl = new Label(sfTotalComp, SWT.LEFT);
        date1TotalLbl.setText("Date 1 Total\nSnowfall Observed");
        date1TotalTxt = new Text(sfTotalComp, SWT.LEFT | SWT.BORDER);
        date1TotalTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date1TotalTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date1TotalTxt.addListener(SWT.Modify, changesListener);

        Label date2TotalLbl = new Label(sfTotalComp, SWT.LEFT);
        date2TotalLbl.setText("Date 2 Total\nSnowfall Observed");
        date2TotalTxt = new Text(sfTotalComp, SWT.LEFT | SWT.BORDER);
        date2TotalTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date2TotalTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date2TotalTxt.addListener(SWT.Modify, changesListener);

        Label date3TotalLbl = new Label(sfTotalComp, SWT.LEFT);
        date3TotalLbl.setText("Date 3 Total\nSnowfall Observed");
        date3TotalTxt = new Text(sfTotalComp, SWT.LEFT | SWT.BORDER);
        date3TotalTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date3TotalTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date3TotalTxt.addListener(SWT.Modify, changesListener);

        Label hour24TotalLbl = new Label(sfTotalComp, SWT.LEFT);
        hour24TotalLbl.setText("Total 24-Hour\nSnowfall Observed");
        hour24TotalTxt = new Text(sfTotalComp, SWT.LEFT | SWT.BORDER);
        hour24TotalTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        hour24TotalTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        hour24TotalTxt.addListener(SWT.Modify, changesListener);

        // The dates texts and labels
        Composite dateComp = new Composite(recordGrp, SWT.NONE);
        gl = new GridLayout(4, false);
        gl.verticalSpacing = 5;
        dateComp.setLayout(gl);

        Label placeHolderLbl = new Label(dateComp, SWT.LEFT);
        placeHolderLbl.setVisible(false);

        Label beginDateLbl = new Label(dateComp, SWT.LEFT);
        beginDateLbl.setText("Begin Date ");
        Label endDateLbl = new Label(dateComp, SWT.LEFT);
        endDateLbl.setText("End Date");
        Label yearLbl = new Label(dateComp, SWT.LEFT);
        yearLbl.setText("Year");

        Label date1Lbl = new Label(dateComp, SWT.LEFT);
        date1Lbl.setText("Date 1:");
        date1BeginDS = new DateSelectionComp(dateComp, false, SWT.NONE);
        date1BeginDS.addListener(SWT.Modify, changesListener);

        date1EndDS = new DateSelectionComp(dateComp, false, SWT.NONE);
        date1EndDS.addListener(SWT.Modify, changesListener);

        date1YearTxt = new Text(dateComp, SWT.BORDER);
        date1YearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date1YearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date1YearTxt.addListener(SWT.Modify, changesListener);

        Label date2Lbl = new Label(dateComp, SWT.LEFT);
        date2Lbl.setText("Date 2:");
        date2BeginDS = new DateSelectionComp(dateComp, false, SWT.NONE);
        date2BeginDS.addListener(SWT.Modify, changesListener);

        date2EndDS = new DateSelectionComp(dateComp, false, SWT.NONE);
        date2EndDS.addListener(SWT.Modify, changesListener);

        date2YearTxt = new Text(dateComp, SWT.BORDER);
        date2YearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date2YearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date2YearTxt.addListener(SWT.Modify, changesListener);

        Label date3Lbl = new Label(dateComp, SWT.LEFT);
        date3Lbl.setText("Date 3:");
        date3BeginDS = new DateSelectionComp(dateComp, false, SWT.NONE);
        date3BeginDS.addListener(SWT.Modify, changesListener);

        date3EndDS = new DateSelectionComp(dateComp, false, SWT.NONE);
        date3EndDS.addListener(SWT.Modify, changesListener);

        date3YearTxt = new Text(dateComp, SWT.BORDER);
        date3YearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date3YearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date3YearTxt.addListener(SWT.Modify, changesListener);

        // The depth texts and labels
        Composite depthComp = new Composite(recordGrp, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        depthComp.setLayout(gl);

        Label snowDepthLbl = new Label(depthComp, SWT.NONE);
        snowDepthLbl.setText("SnowDepth\n(inches)");
        snowDepthRecordTxt = new Text(depthComp, SWT.LEFT | SWT.BORDER);
        snowDepthRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowDepthListener());
        snowDepthRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowDepthListener());
        snowDepthRecordTxt.addListener(SWT.Modify, changesListener);

        Label date1DepthLbl = new Label(depthComp, SWT.LEFT);
        date1DepthLbl.setText("Date 1 Snow\nDepth Observed");
        date1DepthDS = new DateSelectionComp(depthComp, true, SWT.NONE);
        date1DepthDS.addListener(SWT.Modify, changesListener);

        Label date2DepthLbl = new Label(depthComp, SWT.LEFT);
        date2DepthLbl.setText("Date 2 Snow\nDepth Observed");
        date2DepthDS = new DateSelectionComp(depthComp, true, SWT.NONE);
        date2DepthDS.addListener(SWT.Modify, changesListener);

        Label date3DepthLbl = new Label(depthComp, SWT.LEFT);
        date3DepthLbl.setText("Date 3 Snow\nDepth Observed");
        date3DepthDS = new DateSelectionComp(depthComp, true, SWT.NONE);
        date3DepthDS.addListener(SWT.Modify, changesListener);

        return recordGrp;
    }

    /**
     * display monthly snow fall information
     * 
     * @param record
     */
    public void displayMonthlySnowFallInfo(PeriodClimo record) {
        this.periodRcd = record;
        changesListener.setIgnoreChanges(true);

        totalTxt.setText(Float.toString(record.getSnowPeriodNorm()));
        waterTxt.setText(Float.toString(record.getSnowWaterPeriodNorm()));
        snowDepthTxt.setText(Integer
                .toString(ClimateUtilities.nint(record.getSnowGroundNorm())));
        anyTxt.setText(Integer
                .toString(ClimateUtilities.nint(record.getNumSnowGETRNorm())));
        days10Txt.setText(Integer
                .toString(ClimateUtilities.nint(record.getNumSnowGE1Norm())));

        totalRecordTxt.setText(Float.toString(record.getSnowPeriodRecord()));

        List<ClimateDate> snowPeriodMaxYearList = record
                .getSnowPeriodMaxYearList();
        date1TotalTxt.setText(
                Integer.toString(snowPeriodMaxYearList.get(0).getYear()));
        date2TotalTxt.setText(
                Integer.toString(snowPeriodMaxYearList.get(1).getYear()));
        date3TotalTxt.setText(
                Integer.toString(snowPeriodMaxYearList.get(2).getYear()));

        hour24TotalTxt.setText(Float.toString(record.getSnowMax24HRecord()));

        ClimateDates cds = record.getSnow24HList().get(0);
        date1BeginDS.setDate(cds.getStart());
        date1EndDS.setDate(cds.getEnd());
        date1YearTxt.setText(Integer.toString(cds.getEnd().getYear()));

        cds = record.getSnow24HList().get(1);
        date2BeginDS.setDate(cds.getStart());
        date2EndDS.setDate(cds.getEnd());
        date2YearTxt.setText(Integer.toString(cds.getEnd().getYear()));

        cds = record.getSnow24HList().get(2);
        date3BeginDS.setDate(cds.getStart());
        date3EndDS.setDate(cds.getEnd());
        date3YearTxt.setText(Integer.toString(cds.getEnd().getYear()));

        snowDepthRecordTxt.setText(Integer.toString(record.getSnowGroundMax()));
        date1DepthDS.setDate(record.getDaySnowGroundMaxList().get(0));
        date2DepthDS.setDate(record.getDaySnowGroundMaxList().get(1));
        date3DepthDS.setDate(record.getDaySnowGroundMaxList().get(2));

        changesListener.setChangesUnsaved(false);
        changesListener.setIgnoreChanges(false);
    }

    /**
     * get snow fall information from GUI
     * 
     * @return
     */
    public PeriodClimo getSnowFallInfo() {
        periodRcd.setSnowPeriodNorm(totalTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(totalTxt.getText()));
        periodRcd.setSnowWaterPeriodNorm(waterTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(waterTxt.getText()));
        periodRcd.setSnowGroundNorm(Float.parseFloat(snowDepthTxt.getText()));
        periodRcd.setNumSnowGETRNorm(Float.parseFloat(anyTxt.getText()));
        periodRcd.setNumSnowGE1Norm(Float.parseFloat(days10Txt.getText()));

        periodRcd.setSnowPeriodRecord(totalRecordTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(totalRecordTxt.getText()));

        List<ClimateDate> snowPeriodMaxYearList = periodRcd
                .getSnowPeriodMaxYearList();
        snowPeriodMaxYearList.get(0)
                .setYear(Integer.parseInt(date1TotalTxt.getText()));
        snowPeriodMaxYearList.get(1)
                .setYear(Integer.parseInt(date2TotalTxt.getText()));
        snowPeriodMaxYearList.get(2)
                .setYear(Integer.parseInt(date3TotalTxt.getText()));

        periodRcd.setSnowMax24HRecord(hour24TotalTxt.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(hour24TotalTxt.getText()));

        ClimateDates cds = periodRcd.getSnow24HList().get(0);
        cds.setStart(date1BeginDS.getDate());

        cds.setEnd(date1EndDS.getDate());
        cds.getEnd().setYear(Integer.parseInt(date1YearTxt.getText()));

        cds = periodRcd.getSnow24HList().get(1);
        cds.setStart(date2BeginDS.getDate());

        cds.setEnd(date2EndDS.getDate());
        cds.getEnd().setYear(Integer.parseInt(date2YearTxt.getText()));

        cds = periodRcd.getSnow24HList().get(2);
        cds.setStart(date3BeginDS.getDate());

        cds.setEnd(date3EndDS.getDate());
        cds.getEnd().setYear(Integer.parseInt(date3YearTxt.getText()));

        periodRcd.setSnowGroundMax(
                Integer.parseInt(snowDepthRecordTxt.getText()));

        periodRcd.getDaySnowGroundMaxList().set(0, date1DepthDS.getDate());
        periodRcd.getDaySnowGroundMaxList().set(1, date2DepthDS.getDate());
        periodRcd.getDaySnowGroundMaxList().set(2, date3DepthDS.getDate());

        return periodRcd;
    }

    @Override
    protected void setDateSelectionValidMonths() {
        int[] months = getValidMonths();

        date3DepthDS.setValidMonths(months);
        date2DepthDS.setValidMonths(months);
        date1DepthDS.setValidMonths(months);
        date3EndDS.setValidMonths(months);
        date3BeginDS.setValidMonths(months);
        date2EndDS.setValidMonths(months);
        date2BeginDS.setValidMonths(months);
        date1EndDS.setValidMonths(months);
        date1BeginDS.setValidMonths(months);
    }
}
