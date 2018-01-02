/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.daily;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Display Station Daily Morning dialog action.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 16 AUG 2016  20414      amoore      Separate actions for different summary types.
 * 24 AUG 2016  20414      amoore      Pre-display date selection.
 * 21 MAR 2017  30166      amoore      Integration with CPG.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 20 JUN 2017  35324      amoore      Month-day only date component was not saving or providing access to
 *                                     years. New functionality for component exposing a new date field
 *                                     with all date info.
 * 03 JUL 2017  35694      amoore      Hide Set Missing Date button.
 * 19 SEP 2017  38124      amoore      Use GC for text control sizes.
 * 16 OCT 2017  39245      amoore      AM date selection default to previous day.
 * 15 NOV 2017  40416      dmanzella   Fixed waiting cursor
 * </pre>
 * 
 * @author amoore
 */

public class DisplayStationDailyMorningAction extends DisplayDailyAction {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayStationDailyMorningAction.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        preDisplaySelection();

        return null;
    }

    /**
     * Display date selection options for the dialog.
     */
    private void preDisplaySelection() {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        // pre-display option for Latest Morning Climate or Retrieve
        // Climate for a Prior Period (other date)
        final Shell preDisplayDialog = new Shell(shell, SWT.APPLICATION_MODAL
                | ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE);
        preDisplayDialog.setText("Select Climate Run Date");

        RowLayout dialogRL = new RowLayout(SWT.VERTICAL);
        dialogRL.center = true;
        dialogRL.marginWidth = 5;
        dialogRL.marginBottom = 8;
        dialogRL.marginTop = 10;
        preDisplayDialog.setLayout(dialogRL);
        Point size = new Point(306, 220);
        preDisplayDialog.setSize(size);

        // center on the current shell
        Point parentLocation = shell.getLocation();
        Point parentSize = shell.getSize();
        preDisplayDialog.setLocation(
                parentLocation.x + ((parentSize.x - size.x) / 2),
                parentLocation.y + ((parentSize.y - size.y) / 2));

        // options
        Composite optionsComp = new Composite(preDisplayDialog, SWT.BORDER);
        RowLayout optionsRL = new RowLayout(SWT.VERTICAL);
        optionsRL.marginWidth = 5;
        optionsRL.marginBottom = 8;
        optionsRL.marginTop = 10;
        optionsComp.setLayout(optionsRL);

        // latest morning button
        final Button latestMorningButton = new Button(optionsComp, SWT.RADIO);
        latestMorningButton.setText("Latest Morning Climate");
        final Button customDateButton = new Button(optionsComp, SWT.RADIO);
        customDateButton.setText("Retrieve Climate for a Prior Period");

        // date selection comp
        ClimateDate initialAndUpperBoundDate = ClimateDate.getPreviousDay();
        final DateSelectionComp dateSelect = new DateSelectionComp(optionsComp,
                true, SWT.NONE, initialAndUpperBoundDate, null,
                initialAndUpperBoundDate, true);
        // disable date select by default
        dateSelect.setEnabled(false);

        // actions for radio buttons
        latestMorningButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // disable date select
                dateSelect.setEnabled(false);
            }
        });
        customDateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // enable date select
                dateSelect.setEnabled(true);
            }
        });
        // default to select non-custom date
        latestMorningButton.setSelection(true);

        // ok/cancel buttons
        Composite buttonsComp = new Composite(preDisplayDialog, SWT.NONE);
        RowLayout buttonsRL = new RowLayout(SWT.HORIZONTAL);
        buttonsRL.center = true;
        buttonsRL.marginWidth = 5;
        buttonsRL.marginHeight = 8;
        buttonsRL.spacing = 50;
        buttonsComp.setLayout(buttonsRL);

        Button okButton = new Button(buttonsComp, SWT.PUSH);
        okButton.setText("OK");

        GC gc = new GC(okButton);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();
        RowData buttonRD = new RowData(9 * fontWidth, SWT.DEFAULT);

        okButton.setLayoutData(buttonRD);
        Button cancelButton = new Button(buttonsComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(buttonRD);

        // actions for ok/cancel buttons
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                preDisplayDialog.close();
            }
        });
        okButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                ClimateDate date = null;
                if (latestMorningButton.getSelection()) {
                    /*
                     * From CONOPS:
                     * 
                     * Choosing this option will execute Daily Climatological
                     * Formatting application for the previous calendar day.
                     */
                    date = ClimateDate.getPreviousDay();
                } else {
                    // date from user selection
                    date = dateSelect.getDate();
                }
                if (date != null) {
                    ClimateGUIUtils.setCursorWait(preDisplayDialog.getShell());
                    try {
                        openDailyDialog(PeriodType.MORN_RAD, date,
                                !date.equals(ClimateDate.getPreviousDay()));
                    } catch (ClimateException e) {
                        logger.error("Error building dialog.", e);
                    } catch (SerializationException e) {
                        logger.error("Serialization error.", e);
                    } finally {
                        ClimateGUIUtils
                                .resetCursor(preDisplayDialog.getShell());
                        preDisplayDialog.close();
                    }
                } else {
                    logger.error("Error constructing morning dialog");
                }
            }
        });

        preDisplayDialog.open();
    }

}
