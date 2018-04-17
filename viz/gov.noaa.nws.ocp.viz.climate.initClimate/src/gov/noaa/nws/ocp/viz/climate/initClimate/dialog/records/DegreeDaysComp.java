/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * This class display the degree composite in the "MONTHLY NORMALS AND EXTREMES"
 * dialog for init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/06/2016  18469    wkwock      Initial creation
 * 10/27/2016  20635    wkwock      Clean up
 * 27 DEC 2016 22450    amoore      Init should use common listeners where possible.
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
 * 
 */
public class DegreeDaysComp extends AbstractClimateInitComp {
    /**
     * heating text
     */
    private Text heatingTxt;

    /**
     * cooling text
     */
    private Text coolingTxt;

    /**
     * early normal date
     */
    private DateSelectionComp earlyNormDate;

    /**
     * late normal date
     */
    private DateSelectionComp lateNormDate;

    /**
     * early record date
     */
    private DateSelectionComp earlyRecordDate;

    /**
     * late record date
     */
    private DateSelectionComp lateRecordDate;

    /**
     * Constructor.
     * 
     * @param parent
     * @param changeListener
     */
    protected DegreeDaysComp(Composite parent,
            UnsavedChangesListener changeListener) {
        super(parent, SWT.NONE, changeListener);
        GridLayout gl = new GridLayout(2, true);
        gl.horizontalSpacing = 20;
        gl.marginWidth = 10;
        setLayout(gl);

        Group degreeNormalGrp = createDegreeNormalControls(this);
        degreeNormalGrp
                .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        Composite rightComp = new Composite(this, SWT.NONE);
        rightComp.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        RowLayout rightRl = new RowLayout(SWT.VERTICAL);
        rightRl.spacing = 15;
        rightComp.setLayout(rightRl);
        createFreezeNormalControls(rightComp);
        createFreezeRecordControls(rightComp);
    }

    /**
     * create degree normal controls
     * 
     * @param parent
     * 
     * @return
     */
    private Group createDegreeNormalControls(Composite parent) {
        Group normalGrp = new Group(parent, SWT.SHADOW_IN);
        normalGrp.setText("Degree Days Normals");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        normalGrp.setLayout(gl);
        normalGrp.setFont(boldFont);

        Label heatingLbl = new Label(normalGrp, SWT.CENTER);
        heatingLbl.setText("Heating Degree Days");
        heatingTxt = new Text(normalGrp, SWT.CENTER | SWT.BORDER);
        heatingTxt.addListener(SWT.Verify,
                myDisplayListeners.getDegreeDaysListener());
        heatingTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDegreeDaysListener());
        heatingTxt.addListener(SWT.Modify, changesListener);

        Label coolingLbl = new Label(normalGrp, SWT.CENTER);
        coolingLbl.setText("Cooling Degree days");
        coolingTxt = new Text(normalGrp, SWT.CENTER | SWT.BORDER);
        coolingTxt.addListener(SWT.Verify,
                myDisplayListeners.getDegreeDaysListener());
        coolingTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDegreeDaysListener());
        heatingTxt.addListener(SWT.Modify, changesListener);

        return normalGrp;
    }

    /**
     * create freeze normal controls
     * 
     * @param parent
     * 
     * @return
     */
    private Group createFreezeNormalControls(Composite parent) {
        Group normalGrp = new Group(parent, SWT.SHADOW_IN);
        normalGrp.setText("Freeze Dates Normals");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        normalGrp.setLayout(gl);
        normalGrp.setFont(boldFont);

        Label earlyLbl = new Label(normalGrp, SWT.CENTER);
        earlyLbl.setText("Early Freeze Date");
        earlyNormDate = new DateSelectionComp(normalGrp, SWT.NONE);
        earlyNormDate.addListener(SWT.Modify, changesListener);

        Label lateLbl = new Label(normalGrp, SWT.CENTER);
        lateLbl.setText("Late Freeze Date");
        lateNormDate = new DateSelectionComp(normalGrp, SWT.NONE);
        lateNormDate.addListener(SWT.Modify, changesListener);

        return normalGrp;
    }

    /**
     * create freeze record controls
     * 
     * @param parent
     * 
     * @return
     */
    private Group createFreezeRecordControls(Composite parent) {
        Group recordGrp = new Group(parent, SWT.SHADOW_IN);
        recordGrp.setText("Freeze Dates Records");
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 10;
        gl.marginBottom = 5;
        recordGrp.setLayout(gl);
        recordGrp.setFont(boldFont);

        Label earlyLbl = new Label(recordGrp, SWT.CENTER);
        earlyLbl.setText("Early Freeze Date");
        earlyRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        earlyRecordDate.addListener(SWT.Modify, changesListener);

        Label lateLbl = new Label(recordGrp, SWT.CENTER);
        lateLbl.setText("Late Freeze Date");
        lateRecordDate = new DateSelectionComp(recordGrp, true, SWT.NONE);
        lateRecordDate.addListener(SWT.Modify, changesListener);

        return recordGrp;
    }

    /**
     * display degree information
     * 
     * @param record
     */
    public void displayDegreeInfo(PeriodClimo record) {
        periodRcd = record;
        changesListener.setIgnoreChanges(true);

        heatingTxt.setText(Integer.toString(record.getNumHeatPeriodNorm()));
        coolingTxt.setText(Integer.toString(record.getNumCoolPeriodNorm()));

        earlyNormDate.setDate(record.getEarlyFreezeNorm());
        lateNormDate.setDate(record.getLateFreezeNorm());

        earlyRecordDate.setDate(record.getEarlyFreezeRec());
        lateRecordDate.setDate(record.getLateFreezeRec());

        changesListener.setChangesUnsaved(false);
        changesListener.setIgnoreChanges(false);
    }

    /**
     * get degree information from GUI
     * 
     * @return PeriodClimo with degree information
     */
    public PeriodClimo getDegreeInfo() {
        periodRcd.setNumHeatPeriodNorm(Integer.parseInt(heatingTxt.getText()));

        periodRcd.setNumCoolPeriodNorm(Integer.parseInt(coolingTxt.getText()));

        periodRcd.setEarlyFreezeNorm(earlyNormDate.getDate());

        periodRcd.setLateFreezeNorm(lateNormDate.getDate());

        periodRcd.setEarlyFreezeRec(earlyRecordDate.getDate());

        periodRcd.setLateFreezeRec(lateRecordDate.getDate());

        return periodRcd;
    }

    @Override
    protected void setDateSelectionValidMonths() {
        int[] months = getValidMonths();

        lateRecordDate.setValidMonths(months);
        earlyRecordDate.setValidMonths(months);
        lateNormDate.setValidMonths(months);
        earlyNormDate.setValidMonths(months);
    }
}
