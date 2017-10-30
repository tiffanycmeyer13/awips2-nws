/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ManualGenerateClimateProdRequest;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.DisplayStationPeriodDialog;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Abstract Display Station Period dialog action. Abstracted for common
 * pre-Display functionality.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 24 AUG 2016  20414      amoore      Initial creation
 * 14 SEP 2016  22136      amoore      Default custom dates should be valid previous period.
 * 05 DEC 2016  26345      astrakovsky Added work in progress cursor when getting climate data.
 * 15 MAR 2017  30162      amoore      Fix exception throwing.
 * 21 MAR 2017  30166      amoore      Integration with CPG workflow.
 * 04 APR 2017  30166      amoore      Change returned data type from EDEX.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 10 MAY 2017  33104      amoore      Change returned data type from EDEX.
 * 13 JUN 2017  35122      amoore      Fix Latest Period logic. Fix minor comments/organization.
 * 20 JUN 2017  35324      amoore      Month-day only date component was not saving or providing access to
 *                                     years. New functionality for component exposing a new date field
 *                                     with all date info.
 * 03 JUL 2017  35694      amoore      Hide Set Missing Date button.
 * 27 JUL 2017  33104      amoore      Do not use effectively final functionality, for 1.7 build.
 * 19 SEP 2017  38124      amoore      Use GC for text control sizes.
 * </pre>
 * 
 * @author amoore
 */
public abstract class DisplayStationPeriodAction extends AbstractHandler {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayStationPeriodAction.class);

    /**
     * Display date selection options for the dialog.
     * 
     * @param iPeriodType
     * @throws VizException
     * @throws ClimateInvalidParameterException
     */
    protected final void preDisplaySelection(final PeriodType iPeriodType)
            throws VizException, ClimateInvalidParameterException {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        /*
         * pre-display option for Latest Period Climate or Retrieve Climate for
         * a Prior Period (begin and end dates)
         */
        final Shell preDisplayDialog = new Shell(shell, SWT.APPLICATION_MODAL
                | ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE);
        preDisplayDialog.setText("Select Climate Run Date");

        RowLayout dialogRL = new RowLayout(SWT.VERTICAL);
        dialogRL.center = true;
        dialogRL.marginWidth = 5;
        dialogRL.marginBottom = 8;
        dialogRL.marginTop = 10;
        preDisplayDialog.setLayout(dialogRL);
        Point size = new Point(306, 290);
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

        // latest period button
        final Button latestPeriodButton = new Button(optionsComp, SWT.RADIO);
        /*
         * set latest period button, and also set default to-from dates to be
         * the most recent full applicable period.
         */
        final ClimateDates defaultFullPeriodDates;
        switch (iPeriodType) {
        case MONTHLY_RAD:
        case MONTHLY_NWWS:
            latestPeriodButton.setText("Latest Monthly Climate");
            defaultFullPeriodDates = ClimateDates.getPreviousMonthDates();
            break;
        case SEASONAL_RAD:
        case SEASONAL_NWWS:
            latestPeriodButton.setText("Latest Seasonal Climate");
            defaultFullPeriodDates = ClimateDates.getPreviousSeasonDates();
            break;
        case ANNUAL_RAD:
        case ANNUAL_NWWS:
            latestPeriodButton.setText("Latest Annual Climate");
            defaultFullPeriodDates = ClimateDates.getPreviousYearDates();
            break;
        default:
            throw new VizException(
                    "Unhandled period type [" + iPeriodType + "]");
        }

        final Button customDateButton = new Button(optionsComp, SWT.RADIO);
        customDateButton.setText("Retrieve Climate for a Prior Period");

        // date selection comps
        ClimateDate upperBoundDate = new ClimateDate(
                TimeUtil.newCalendar().getTime());

        Label fromLabel = new Label(optionsComp, SWT.NORMAL);
        fromLabel.setText("From:");

        final DateSelectionComp fromDateSelect = new DateSelectionComp(
                optionsComp, true, SWT.NONE, defaultFullPeriodDates.getStart(),
                null, upperBoundDate, true);

        Label toLabel = new Label(optionsComp, SWT.NORMAL);
        toLabel.setText("To:");

        final DateSelectionComp toDateSelect = new DateSelectionComp(
                optionsComp, true, SWT.NONE, defaultFullPeriodDates.getEnd(),
                null, upperBoundDate, true);
        // disable date selects by default
        fromDateSelect.setEnabled(false);
        toDateSelect.setEnabled(false);

        // actions for radio buttons
        latestPeriodButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // disable date select
                fromDateSelect.setEnabled(false);
                toDateSelect.setEnabled(false);
            }
        });
        customDateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // enable date select
                fromDateSelect.setEnabled(true);
                toDateSelect.setEnabled(true);
            }
        });
        // default to select non-custom date
        latestPeriodButton.setSelection(true);

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
                // 26345 - add work in progress cursor icon
                ClimateGUIUtils.setCursorWait(preDisplayDialog.getShell());
                try {
                    if (latestPeriodButton.getSelection()) {
                        constructLatest(iPeriodType, shell);
                    } else {
                        // construct from user-selected dates
                        try {
                            // custom-selected dates
                            ClimateDate startDate = fromDateSelect.getDate();
                            ClimateDate endDate = toDateSelect.getDate();

                            /*
                             * Determine from dates if this is a run of the most
                             * recent full applicable period.
                             */
                            if (startDate
                                    .equals(defaultFullPeriodDates.getStart())
                                    && endDate.equals(
                                            defaultFullPeriodDates.getEnd())) {
                                constructLatest(iPeriodType, shell);
                            } else {
                                openPeriodDialog(shell, false, iPeriodType,
                                        startDate, endDate);
                            }
                        } catch (ClimateInvalidParameterException e) {
                            logger.error("Invalid parameter", e);
                        } catch (SerializationException e) {
                            logger.error("Serialization error", e);
                        }
                    }
                } catch (ClimateQueryException e) {
                    logger.error("Query error during dialog construction.", e);
                } catch (ClimateInvalidParameterException e) {
                    logger.error(
                            "Error with parameters during dialog construction.",
                            e);
                } catch (SerializationException e) {
                    logger.error("Serialization error", e);
                } finally {
                    // 26345 - add work in progress cursor icon
                    ClimateGUIUtils.resetCursor(preDisplayDialog.getShell());
                    preDisplayDialog.close();
                }
            }
        });

        preDisplayDialog.open();
    }

    /**
     * Open the Daily dialog for manual Climate Product Generation process, and
     * start a Climate Product Generation session.
     * 
     * @param shell
     * @param mostRecentRun
     *            true if this is the most recent full period.
     * @param periodType
     *            period type.
     * @param beginDate
     *            begin date for processing.
     * @param endDate
     *            end date for processing.
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     * @throws SerializationException
     */
    private void openPeriodDialog(Shell shell, boolean mostRecentRun,
            PeriodType periodType, ClimateDate beginDate, ClimateDate endDate)
                    throws ClimateQueryException,
                    ClimateInvalidParameterException, SerializationException {
        ManualGenerateClimateProdRequest request = new ManualGenerateClimateProdRequest(
                periodType, beginDate, endDate, !mostRecentRun);

        try {
            DisplayStationPeriodDialog dialog = new DisplayStationPeriodDialog(
                    shell, (String) ThriftClient.sendRequest(request));

            dialog.setCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    // closing actions
                    logger.debug("Display dialog for type: [" + periodType
                            + "] and dates: [" + beginDate.toFullDateString()
                            + "][" + endDate.toFullDateString() + "] closed.");
                }
            });
            dialog.open();
        } catch (VizException e) {
            throw new ClimateQueryException("Error getting period data", e);
        }
    }

    /**
     * Construct for the latest full period available, either because the Latest
     * button was selected or user's custom date selection was equivalent to the
     * Latest.
     * 
     * @param iPeriodType
     * @param shell
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     * @throws SerializationException
     */
    private void constructLatest(final PeriodType iPeriodType,
            final Shell shell) throws ClimateQueryException,
                    ClimateInvalidParameterException, SerializationException {
        switch (iPeriodType) {
        case MONTHLY_RAD:
        case MONTHLY_NWWS:
            openPeriodDialog(shell, true, PeriodType.MONTHLY_RAD,
                    ClimateDate.getLocalDate(), ClimateDate.getLocalDate());
            break;
        case SEASONAL_RAD:
        case SEASONAL_NWWS:
            openPeriodDialog(shell, true, PeriodType.SEASONAL_RAD,
                    ClimateDate.getLocalDate(), ClimateDate.getLocalDate());
            break;
        case ANNUAL_RAD:
        case ANNUAL_NWWS:
            openPeriodDialog(shell, true, PeriodType.ANNUAL_RAD,
                    ClimateDate.getLocalDate(), ClimateDate.getLocalDate());
            break;
        default:
            logger.error("Unhandled period type [" + iPeriodType + "]");
            break;
        }
    }
}
