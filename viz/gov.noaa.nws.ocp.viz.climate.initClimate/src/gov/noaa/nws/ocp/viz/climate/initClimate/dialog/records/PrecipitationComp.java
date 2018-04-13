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

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * This class display the precipitation composite in the "MONTHLY NORMALS AND
 * EXTREMES" dialog for init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/06/2016  18469    wkwock      Initial creation
 * 10/27/2016  20635    wkwock      Clean up
 * 27 DEC 2016 22450    amoore      Init should use common listeners where possible.
 * 02 MAR 2017 29576    wkwock      Add month(s) validation.
 * 15 JUN 2017 35187    amoore      Handle trace symbol in text box.
 * 21 SEP 2017 38124    amoore      Use better abstraction logic for valid months.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public class PrecipitationComp extends AbstractClimateInitComp {
    /** logger */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(PrecipitationComp.class);

    /** total precipitation text */
    private Text totalTxt;

    /** average precipitation text */
    private Text avgTxt;

    /** Days with Precipitation>= 0.01 inches text */
    private Text days01Txt;

    /** Days with Precipitation>= 0.1 inches text */
    private Text days10Txt;

    /** Days with Precipitation>= 0.5 inches text */
    private Text days50Txt;

    /** Days with Precipitation>= 1.0 inches text */
    private Text days100Txt;

    /** max precipitation text */
    private Text maxPrecipTxt;

    /** date 1 max record text */
    private Text date1MaxRecordTxt;

    /** date 2 max record text */
    private Text date2MaxRecordTxt;

    /** date 3 max record text */
    private Text date3MaxRecordTxt;

    /** minimum precipitation text */
    private Text minPrecipTxt;

    /** date 1 minimum record text */
    private Text date1MinRecordTxt;

    /** date 2 minimum record text */
    private Text date2MinRecordTxt;

    /** date 3 minimum record text */
    private Text date3MinRecordTxt;

    /**
     * Constructor.
     * 
     * @param parent
     * @param changeListener
     */
    protected PrecipitationComp(Composite parent,
            UnsavedChangesListener changeListener) {
        super(parent, SWT.NONE, changeListener);
        GridLayout gl = new GridLayout(2, true);
        gl.horizontalSpacing = 50;
        setLayout(gl);

        Group normalGrp = createPrecipNormalControls();
        normalGrp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        Group recordGrp = createPrecipRecordControls();
        recordGrp.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
    }

    /**
     * create precipitation normal controls
     * 
     * @return
     */
    private Group createPrecipNormalControls() {
        Group normalGrp = new Group(this, SWT.SHADOW_IN);
        normalGrp.setText("Precipitation Normals");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        normalGrp.setLayout(gl);
        normalGrp.setFont(boldFont);

        Label totalLbl = new Label(normalGrp, SWT.LEFT);
        totalLbl.setText("Total\nPrecipitation (inches)");
        totalTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        totalTxt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        totalTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        totalTxt.addListener(SWT.Modify, changesListener);

        Label avgLbl = new Label(normalGrp, SWT.LEFT);
        avgLbl.setText("Daily Average\nPrecipitation (inches)");
        avgTxt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        avgTxt.addListener(SWT.Verify, myDisplayListeners.getPrecipListener());
        avgTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        avgTxt.addListener(SWT.Modify, changesListener);

        Label days01Lbl = new Label(normalGrp, SWT.LEFT);
        days01Lbl.setText("Days with Precipitation\nGE 0.01 inches");
        GridData gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.verticalIndent = 10;
        days01Lbl.setLayoutData(gd);
        days01Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        days01Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultDoubleListener());
        days01Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultDoubleListener());
        days01Txt.addListener(SWT.Modify, changesListener);

        Label days10Lbl = new Label(normalGrp, SWT.LEFT);
        days10Lbl.setText("Days with Precipitation\nGE 0.10 inches");
        days10Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        days10Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultDoubleListener());
        days10Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultDoubleListener());
        days10Txt.addListener(SWT.Modify, changesListener);

        Label days50Lbl = new Label(normalGrp, SWT.LEFT);
        days50Lbl.setText("Days with Precipitation\nGE 0.50 inches");
        days50Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        days50Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultDoubleListener());
        days50Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultDoubleListener());
        days50Txt.addListener(SWT.Modify, changesListener);

        Label days100Lbl = new Label(normalGrp, SWT.LEFT);
        days100Lbl.setText("Days with Precipitation\nGE 1.00 inches");
        days100Txt = new Text(normalGrp, SWT.LEFT | SWT.BORDER);
        days100Txt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultDoubleListener());
        days100Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultDoubleListener());
        days100Txt.addListener(SWT.Modify, changesListener);

        return normalGrp;
    }

    /**
     * create precipitation record controls
     * 
     * @return
     */
    private Group createPrecipRecordControls() {
        Group recordGrp = new Group(this, SWT.SHADOW_IN);
        recordGrp.setText("Precipitation Records");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        recordGrp.setLayout(gl);
        recordGrp.setFont(boldFont);

        Label maxPrecipLbl = new Label(recordGrp, SWT.LEFT);
        maxPrecipLbl.setText("Maximum Total\nPrecipitation (inches)");
        maxPrecipTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        maxPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        maxPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        maxPrecipTxt.addListener(SWT.Modify, changesListener);

        Label date1MaxRecordLbl = new Label(recordGrp, SWT.LEFT);
        date1MaxRecordLbl.setText("Date 1 Maximum\nPrecipitation Observed");
        GridData gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.verticalIndent = 10;
        date1MaxRecordLbl.setLayoutData(gd);
        date1MaxRecordTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        date1MaxRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date1MaxRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date1MaxRecordTxt.addListener(SWT.Modify, changesListener);

        Label date2MaxRecordLbl = new Label(recordGrp, SWT.LEFT);
        date2MaxRecordLbl.setText("Date 2 Maximum\nPrecipitation Observed");
        date2MaxRecordTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        date2MaxRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date2MaxRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date2MaxRecordTxt.addListener(SWT.Modify, changesListener);

        Label date3MaxRecordLbl = new Label(recordGrp, SWT.LEFT);
        date3MaxRecordLbl.setText("Date 3 Maximum\nPrecipitation Observed");
        date3MaxRecordTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        date3MaxRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date3MaxRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date3MaxRecordTxt.addListener(SWT.Modify, changesListener);

        Label minPrecipLbl = new Label(recordGrp, SWT.LEFT);
        minPrecipLbl.setText("Minimum Total\nPrecipitation (inches)");
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.verticalIndent = 10;
        minPrecipLbl.setLayoutData(gd);
        minPrecipTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        minPrecipTxt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        minPrecipTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        minPrecipTxt.addListener(SWT.Modify, changesListener);

        Label date1MinRecordLbl = new Label(recordGrp, SWT.LEFT);
        date1MinRecordLbl.setText("Date 1 Minimum\nPrecipitation Observed");
        gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.verticalIndent = 10;
        date1MinRecordLbl.setLayoutData(gd);
        date1MinRecordTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        date1MinRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date1MinRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date1MinRecordTxt.addListener(SWT.Modify, changesListener);

        Label date2MinRecordLbl = new Label(recordGrp, SWT.LEFT);
        date2MinRecordLbl.setText("Date 2 Minimum\nPrecipitation Observed");
        date2MinRecordTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        date2MinRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date2MinRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date2MinRecordTxt.addListener(SWT.Modify, changesListener);

        Label date3MinRecordLbl = new Label(recordGrp, SWT.LEFT);
        date3MinRecordLbl.setText("Date 3 Minimum\nPrecipitation Observed");
        date3MinRecordTxt = new Text(recordGrp, SWT.LEFT | SWT.BORDER);
        date3MinRecordTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        date3MinRecordTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        date3MinRecordTxt.addListener(SWT.Modify, changesListener);

        return recordGrp;
    }

    /**
     * display precipitation information
     * 
     * @param record
     */
    public void displayPrecipitationInfo(PeriodClimo record) {
        this.periodRcd = record;
        changesListener.setIgnoreChanges(true);

        totalTxt.setText(Float.toString(record.getPrecipPeriodNorm()));

        avgTxt.setText(Float.toString(record.getPrecipDayNorm()));
        days01Txt.setText(Float.toString(record.getNumPrcpGE01Norm()));
        days10Txt.setText(Float.toString(record.getNumPrcpGE10Norm()));
        days50Txt.setText(Float.toString(record.getNumPrcpGE50Norm()));
        days100Txt.setText(Float.toString(record.getNumPrcpGE100Norm()));
        maxPrecipTxt.setText(Float.toString(record.getPrecipPeriodMax()));

        List<ClimateDate> precipPeriodMaxYearList = record
                .getPrecipPeriodMaxYearList();
        date1MaxRecordTxt.setText(
                Integer.toString(precipPeriodMaxYearList.get(0).getYear()));
        date2MaxRecordTxt.setText(
                Integer.toString(precipPeriodMaxYearList.get(1).getYear()));
        date3MaxRecordTxt.setText(
                Integer.toString(precipPeriodMaxYearList.get(2).getYear()));

        minPrecipTxt.setText(Float.toString(record.getPrecipPeriodMin()));

        List<ClimateDate> precipPeriodMinYearList = record
                .getPrecipPeriodMinYearList();
        date1MinRecordTxt.setText(
                Integer.toString(precipPeriodMinYearList.get(0).getYear()));
        date2MinRecordTxt.setText(
                Integer.toString(precipPeriodMinYearList.get(1).getYear()));
        date3MinRecordTxt.setText(
                Integer.toString(precipPeriodMinYearList.get(2).getYear()));

        changesListener.setChangesUnsaved(false);
        changesListener.setIgnoreChanges(false);
    }

    /**
     * Get monthly/seasonal/annual precipitation info from the precipitation
     * composite. Keep other info (temperatures, snowfall degree) in monthlyRcd.
     * 
     * @param periodRcd
     * @return
     */
    public PeriodClimo getPrecipitationInfo() {
        try {
            periodRcd.setPrecipPeriodNorm(totalTxt.getText()
                    .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                            ? ParameterFormatClimate.TRACE
                            : Float.parseFloat(totalTxt.getText()));
            periodRcd.setPrecipDayNorm(avgTxt.getText()
                    .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                            ? ParameterFormatClimate.TRACE
                            : Float.parseFloat(avgTxt.getText()));
            periodRcd.setNumPrcpGE01Norm(Float.parseFloat(days01Txt.getText()));
            periodRcd.setNumPrcpGE10Norm(Float.parseFloat(days10Txt.getText()));
            periodRcd.setNumPrcpGE50Norm(Float.parseFloat(days50Txt.getText()));
            periodRcd.setNumPrcpGE100Norm(
                    Float.parseFloat(days100Txt.getText()));
            periodRcd.setPrecipPeriodMax(maxPrecipTxt.getText()
                    .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                            ? ParameterFormatClimate.TRACE
                            : Float.parseFloat(maxPrecipTxt.getText()));

            List<ClimateDate> precipPeriodMaxYearList = periodRcd
                    .getPrecipPeriodMaxYearList();
            precipPeriodMaxYearList.get(0)
                    .setYear(Integer.parseInt(date1MaxRecordTxt.getText()));
            precipPeriodMaxYearList.get(1)
                    .setYear(Integer.parseInt(date2MaxRecordTxt.getText()));
            precipPeriodMaxYearList.get(2)
                    .setYear(Integer.parseInt(date3MaxRecordTxt.getText()));

            periodRcd.setPrecipPeriodMin(minPrecipTxt.getText()
                    .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                            ? ParameterFormatClimate.TRACE
                            : Float.parseFloat(minPrecipTxt.getText()));

            List<ClimateDate> precipPeriodMinYearList = periodRcd
                    .getPrecipPeriodMinYearList();
            precipPeriodMinYearList.get(0)
                    .setYear(Integer.parseInt(date1MinRecordTxt.getText()));
            precipPeriodMinYearList.get(1)
                    .setYear(Integer.parseInt(date2MinRecordTxt.getText()));
            precipPeriodMinYearList.get(2)
                    .setYear(Integer.parseInt(date3MinRecordTxt.getText()));
        } catch (NumberFormatException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
            return null;
        }
        return periodRcd;
    }

    @Override
    protected void setDateSelectionValidMonths() {
        // no Date Selection composites in this pane
    }
}
