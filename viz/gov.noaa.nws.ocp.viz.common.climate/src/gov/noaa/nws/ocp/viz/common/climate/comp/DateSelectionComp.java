/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.comp;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * This class allows date selection and, unlike the regular {@link DateTime}
 * widget, an empty selection. Automatically sets its own layout.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 13 MAY 2016  16003      amoore      Make month-day and month-day-year date selection options.
 * 22 JUL 2016  20414      amoore      Add upper/lower bounds option.
 * 05 AUG 2016  20999      amoore      Add calendar icon to date selection button.
 * 10 AUG 2016  20414      amoore      Add ability to toggle editability. Make modal to prevent
 *                                     interference in calling shell/dialog prior to date selection.
 *                                     Key listeners should apply to the text field.
 * 24 AUG 2016  20414      amoore      Use upper/lower bound for new date if selected date is out
 *                                     of bounds. Check upper/lower bound on manually-typed date.
 * 07 SEP 2016  20414      amoore      Add ability to toggle enabled-status.
 * 08 SEP 2016  20636      wpaintsil   Set focus on textfield after date picker dialog closes.
 * 19 SEP 2016  20636      wpaintsil   Tweak logic to allow for setting just the textfield unedibable.
 * 13 OCT 2016  20636      wpaintsil   Add option to display only a day of a particular month.
 * 10 NOV 2016  20636      wpaintsil   Move logic for single day number display to a separate class.
 * 22 FEB 2017  28609      amoore      Address TODOs.
 * 01 MAR 2017  29576      wkwock      Add month(s) validation.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 08 May 2017  33534      jwu         Add option to disable "Set Missing" button.
 * 12 MAY 2017  33104      amoore      Address minor FindBugs.
 * 16 JUN 2017  35182      amoore      Fix variable naming.
 * 20 JUN 2017  33104      amoore      Add return of full date chosen, otherwise missing year messes up
 *                                     later calculations. Safer handling of bounds.
 * 20 JUN 2017  35324      amoore      Month-day only date component was not saving or providing access to
 *                                     years. New functionality for component exposing a new date field
 *                                     with all date info.
 * 03 JUL 2017  35694      amoore      Large overhaul for above issue.
 * 27 JUL 2017  33104      amoore      Do not use effectively final functionality, for 1.7 build.
 * 03 AUG 2017  36726      amoore      Fix cutting off of some components.
 * 18 AUG 2017  36857      amoore      Center date selection text.
 * 19 SEP 2017  38124      amoore      Address review comments.
 * </pre>
 * 
 * @author amoore
 */
public class DateSelectionComp extends AbstractDateComp {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DateSelectionComp.class);

    /**
     * Calendar icon.
     */
    private static final Image CALENDAR_ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin(ClimateLayoutValues.PLUGIN_ID,
                    "icons/calendar.png")
            .createImage();

    /**
     * Text field for date.
     */
    protected final Text dateTF;

    /**
     * Flag for if year is important and should be displayed.
     */
    protected final boolean includeYear;

    /**
     * Lower bound date.
     */
    private final ClimateDate lowerBound;

    /**
     * Upper bound date.
     */
    private final ClimateDate upperBound;

    /**
     * valid months
     */
    private int[] validMonths = null;

    /**
     * Last selected date
     */
    private Calendar lastSelectedDate;

    /**
     * Date selection button.
     */
    protected final Button selectDateBtn;

    /**
     * Current full selected date.
     */
    private ClimateDate currDate = null;

    /**
     * Hide "Set Missing" button if true.
     */
    private final boolean disableMissingBtn;

    /**
     * Constructor. Automatically creates its own layout. Does not include year.
     * Uses missing month-day date string as initial text. No upper or lower
     * bound on date.
     * 
     * @param parent
     *            parent composite.
     * @param style
     *            style.
     */
    public DateSelectionComp(final Composite parent, int style) {
        this(parent, false, style, null, null);
    }

    /**
     * Constructor. Automatically creates its own layout. Does not include year.
     * Uses missing month-day date string as initial text.
     * 
     * @param parent
     *            parent composite.
     * @param style
     *            style.
     * @param lowerBound
     *            lower bound for date.
     * @param upperBound
     *            upper bound for date.
     */
    public DateSelectionComp(final Composite parent, int style,
            ClimateDate lowerBound, ClimateDate upperBound) {
        this(parent, false, style, lowerBound, upperBound);
    }

    /**
     * Constructor. Automatically creates its own layout. Uses missing date
     * string as initial text based on if year is to be included or not. No
     * upper or lower bound on date.
     * 
     * @param parent
     *            parent composite.
     * @param includeYear
     *            true if using year, false otherwise.
     * @param style
     *            style.
     */
    public DateSelectionComp(final Composite parent, boolean includeYear,
            int style) {
        this(parent, includeYear, style, null, null);
    }

    /**
     * Constructor. Automatically creates its own layout. Uses missing date
     * string as initial text based on if year is to be included or not.
     * 
     * @param parent
     *            parent composite.
     * @param includeYear
     *            true if using year, false otherwise.
     * @param style
     *            style.
     * @param lowerBound
     *            lower bound for date.
     * @param upperBound
     *            upper bound for date.
     */
    public DateSelectionComp(final Composite parent, boolean includeYear,
            int style, ClimateDate lowerBound, ClimateDate upperBound) {
        this(parent, includeYear, style, ClimateDate.getMissingClimateDate(),
                lowerBound, upperBound);
    }

    /**
     * Constructor. Automatically creates its own layout. No upper or lower
     * bound on date.
     * 
     * @param parent
     *            parent composite.
     * @param includeYear
     *            true if using year, false otherwise.
     * @param style
     *            style.
     * @param initialDate
     *            the initial date to display.
     */
    public DateSelectionComp(final Composite parent, boolean includeYear,
            int style, ClimateDate initialDate) {
        this(parent, includeYear, style, initialDate, null, null);
    }

    /**
     * Constructor. Automatically creates its own layout, with "Set Missing"
     * button enabled.
     * 
     * @param parent
     *            parent composite.
     * @param includeYear
     *            true if using year, false otherwise.
     * @param style
     *            style.
     * @param initialDate
     *            the initial date to display.
     * @param lowerBound
     *            lower bound for date.
     * @param upperBound
     *            upper bound for date.
     */
    public DateSelectionComp(final Composite parent, boolean includeYear,
            int style, ClimateDate initialDate, ClimateDate lowerBound,
            ClimateDate upperBound) {
        this(parent, includeYear, style, initialDate, lowerBound, upperBound,
                false);
    }

    /**
     * Constructor. Automatically creates its own layout. No upper or lower
     * bound on date.
     * 
     * @param parent
     *            parent composite.
     * @param includeYear
     *            true if using year, false otherwise.
     * @param style
     *            style.
     * @param initialDate
     *            the initial date to display.
     * @param lowerBound
     *            lower bound for date.
     * @param upperBound
     *            upper bound for date.
     * @param disableMissingBtn
     *            Hide "Set Missing" button if true.
     */
    public DateSelectionComp(final Composite parent, boolean includeYear,
            int style, ClimateDate initialDate, ClimateDate lowerBound,
            ClimateDate upperBound, final boolean disableMissingBtn) {
        super(parent, style);

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;

        this.includeYear = includeYear;

        RowLayout dateSelectorRL = new RowLayout(SWT.HORIZONTAL);
        dateSelectorRL.center = true;
        this.setLayout(dateSelectorRL);

        dateTF = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.CENTER);

        currDate = new ClimateDate(initialDate);

        if (this.includeYear) {
            dateTF.setText(this.currDate.toFullDateString());
        } else {
            dateTF.setText(this.currDate.toMonthDayDateString());
        }

        validateDateText();

        dateTF.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                validateDateText();
            }
        });

        selectDateBtn = new Button(this, SWT.PUSH);
        selectDateBtn.setImage(CALENDAR_ICON);

        this.disableMissingBtn = disableMissingBtn;

        // date selection
        selectDateBtn.addSelectionListener(new DateSelectionAdapter(this));
    }

    /**
     * Look at the current date text and validate. Set to missing date string if
     * invalid. Only validates that month is between 1 and 12, and day is
     * between 1 and 31. If including year, year must be positive and less than
     * 10000.
     * 
     * This should only be called on construction and from user-editing of the
     * field.
     */
    protected void validateDateText() {
        // validate the date text
        currDate = ClimateDate.getMissingClimateDate();
        String dateText = dateTF.getText();
        if (dateText.isEmpty()) {
            if (includeYear) {
                dateTF.setText(ClimateDate.MISSING_FULL_DATE_STRING);
            } else {
                dateTF.setText(ClimateDate.MISSING_MONTH_DAY_DATE_STRING);
            }
        } else {
            try {
                if (includeYear && !dateText
                        .equals(ClimateDate.MISSING_FULL_DATE_STRING)) {
                    /*
                     * full date is not empty or set to missing date string
                     */
                    validateFullDateText(dateText);
                } else if (!includeYear && !dateText
                        .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                    /*
                     * month-day date is not empty or set to missing date string
                     */
                    validateMonthDayDateText(dateText);
                }

            } catch (ParseException exception) {
                logger.warn("Could not parse date. " + exception.getMessage());

                // use missing data text
                dateTF.setText(
                        includeYear ? ClimateDate.MISSING_FULL_DATE_STRING
                                : ClimateDate.MISSING_MONTH_DAY_DATE_STRING);
            }
        }
    }

    /**
     * Validates and formats current date text for a month-day date (not
     * including year). Get year based on what works best for bounds.
     * 
     * @param dateText
     *            text to validate.
     * @throws ParseException
     *             on exception parsing date.
     */
    private void validateMonthDayDateText(String dateText)
            throws ParseException {
        ClimateDate parsedDate = ClimateDate
                .parseMonthDayDateFromString(dateText);
        /*
         * The following logic for inferring a user's desired year assumes
         * bounds are no more than a year apart.
         */
        if (upperBound == null && lowerBound == null) {
            // both bounds are null; use current date's year
            parsedDate.setYear(currDate.getYear());
        } else if (upperBound != null && lowerBound == null) {
            // non-null upper; use that bound's year
            parsedDate.setYear(upperBound.getYear());
        } else if (upperBound == null && lowerBound != null) {
            // non-null lower; use that bound's year
            parsedDate.setYear(lowerBound.getYear());
        } else {
            // neither bound null
            if ((lowerBound.getMon() > upperBound.getMon())
                    || (lowerBound.getMon() == upperBound.getMon()
                            && lowerBound.getDay() >= upperBound.getDay())) {
                /*
                 * ignoring years, lower bound is "after" upper bound. They must
                 * be different years.
                 */
                if ((parsedDate.getMon() < upperBound.getMon()) || (parsedDate
                        .getMon() == upperBound.getMon()
                        && parsedDate.getDay() <= upperBound.getDay())) {
                    // date month-day is before upper month-day. Use upper year.
                    parsedDate.setYear(upperBound.getYear());
                } else if ((parsedDate.getMon() > lowerBound.getMon())
                        || (parsedDate.getMon() == lowerBound.getMon()
                                && parsedDate.getDay() >= lowerBound
                                        .getDay())) {
                    // date month-day is after lower month-day. Use lower year.
                    parsedDate.setYear(lowerBound.getYear());
                } else {
                    // default to upper
                    parsedDate.setYear(upperBound.getYear());
                }
            } else {
                /*
                 * ignoring years, lower bound is still before upper; use upper
                 * year
                 */
                parsedDate.setYear(upperBound.getYear());
            }
        }
        currDate = validateDateBounds(parsedDate);

        dateTF.setText(currDate.toMonthDayDateString());
    }

    /**
     * Validates and formats current date text for a full date (including year).
     * 
     * @param dateText
     *            text to validate.
     * @throws ParseException
     *             on exception parsing date.
     */
    private void validateFullDateText(String dateText) throws ParseException {
        currDate = validateDateBounds(
                ClimateDate.parseFullDateFromString(dateText));

        dateTF.setText(currDate.toFullDateString());
    }

    /**
     * @return copy of the current full date represented in this component.
     */
    public ClimateDate getDate() {
        return new ClimateDate(currDate);
    }

    public void setDate(ClimateDate date) {
        if (includeYear && !date.toFullDateString()
                .equals(ClimateDate.MISSING_FULL_DATE_STRING)) {
            /*
             * full date is not empty or set to missing date string
             */
            currDate = validateDateBounds(date);

            dateTF.setText(currDate.toFullDateString());
        } else if (!includeYear && !date.toMonthDayDateString()
                .equals(ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
            /*
             * month-day date is not empty or set to missing date string
             */
            currDate = validateDateBounds(date);

            dateTF.setText(currDate.toMonthDayDateString());
        }
    }

    /**
     * Sets enabled status of this component by setting enabled status of the
     * date text field and date button. If this component has been set to
     * ineditable, the date button will remain disabled even if enabling the
     * component.
     * 
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dateTF.setEnabled(enabled);
        selectDateBtn.setEnabled(enabled && getEditable());
    }

    /**
     * Sets editability of the date text field.
     * 
     * @param editable
     */
    public void setEditable(boolean editable) {
        dateTF.setEditable(editable);

        // Gray background to visually indicate it's not editable.
        if (!editable) {
            dateTF.setBackground(Display.getCurrent()
                    .getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
        } else {
            dateTF.setBackground(
                    Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        }
    }

    /**
     * @return true if date text field is editable, false otherwise.
     */
    public boolean getEditable() {
        return dateTF.getEditable();
    }

    /**
     * Set text to missing.
     */
    public void setMissing() {
        if (includeYear) {
            dateTF.setText(ClimateDate.MISSING_FULL_DATE_STRING);
        } else {
            dateTF.setText(ClimateDate.MISSING_MONTH_DAY_DATE_STRING);
        }
    }

    /**
     * Add some listener. Assumed to be added to the {@link Text} field for this
     * composite.
     */
    @Override
    public void addListener(int eventType, Listener listener) {
        dateTF.addListener(eventType, listener);
    }

    /**
     * Add key listener to the associated {@link Text} field for this composite.
     */
    @Override
    public void addKeyListener(KeyListener listener) {
        dateTF.addKeyListener(listener);
    }

    /**
     * Add selection listener to the associated {@link Button} for this
     * composite.
     * 
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        selectDateBtn.addSelectionListener(listener);
    }

    /**
     * Validate the given date is within the lower/upper date bounds for this
     * component, if defined.
     * 
     * @param iDate
     * @return the original date, if set to missing or is within bounds; copy of
     *         the upper bound, if the given original date is after that bound;
     *         copy of the lower bound, if the given original date is before
     *         that bound.
     */
    protected ClimateDate validateDateBounds(ClimateDate iDate) {
        // check bounds

        // upper bound
        if (upperBound != null && iDate.after(upperBound)) {
            // we are after the upper bound
            logger.warn("User selected date [" + iDate.toFullDateString()
                    + "], which is after the upper bound ["
                    + upperBound.toFullDateString()
                    + "]. Using upper bound date.");
            return new ClimateDate(upperBound);
        }

        // lower bound
        if (lowerBound != null && iDate.before(lowerBound)) {
            // we are before the lower bound
            logger.warn("User selected date [" + iDate.toFullDateString()
                    + "], which is before the lower bound ["
                    + lowerBound.toFullDateString()
                    + "]. Using lower bound date.");
            return new ClimateDate(lowerBound);
        }

        return iDate;
    }

    /**
     * set valid months
     * 
     * @param months
     */
    public void setValidMonths(int[] months) {
        if (months != null) {
            validMonths = Arrays.copyOf(months, months.length);
        } else {
            validMonths = null;
        }
    }

    /**
     * 
     * @return valid months array. May be null.
     */
    private int[] getValidMonths() {
        return validMonths;
    }

    /**
     * @return true if date and logic should consider year
     */
    private boolean isIncludeYear() {
        return includeYear;
    }

    /**
     * @return Point to place Date Selection dialog.
     */
    private Point getDialogLocation() {
        Point buttonLocation = selectDateBtn.getLocation();
        return selectDateBtn.toDisplay(buttonLocation.x - 80,
                buttonLocation.y - 40);
    }

    /**
     * Set date text field to given text.
     * 
     * @param text
     */
    private void setText(String text) {
        dateTF.setText(text);
    }

    /**
     * Set date text field to get keyboard focus.
     */
    private void setTextFieldFocus() {
        dateTF.setFocus();
    }

    /**
     * @return true to disable "Missing" button
     */
    private boolean isDisableMissingBtn() {
        return disableMissingBtn;
    }

    /**
     * @param cal
     *            datetime to set last selected date to be
     */
    private void setLastSelectedDate(Calendar cal) {
        lastSelectedDate = cal;
    }

    /**
     * @return last selected date
     */
    private Calendar getLastSelectedDate() {
        return lastSelectedDate;
    }

    /**
     * Logic for when calendar icon is clicked.
     * 
     * @author amoore
     */
    private static class DateSelectionAdapter extends SelectionAdapter {

        /**
         * Parent composite.
         */
        private final DateSelectionComp parentComp;

        /**
         * OK button in dialog.
         */
        private Button buttonOK;

        /**
         * Constructor.
         */
        public DateSelectionAdapter(DateSelectionComp parentComp) {
            this.parentComp = parentComp;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Shell dateSelectionDialog = new Shell(parentComp.getShell(),
                    SWT.APPLICATION_MODAL
                            | ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE);
            dateSelectionDialog.setParent(parentComp);

            dateSelectionDialog.setText("Select a date");

            RowLayout dialogRL = new RowLayout(SWT.HORIZONTAL);
            dialogRL.center = true;
            dialogRL.marginLeft = 10;
            dialogRL.marginBottom = 8;
            dialogRL.marginTop = 8;
            dialogRL.marginRight = 10;
            dateSelectionDialog.setLayout(dialogRL);

            Point dialogPoint = parentComp.getDialogLocation();
            dateSelectionDialog.setLocation(dialogPoint.x, dialogPoint.y);

            DateTime dateTime = new DateTime(dateSelectionDialog,
                    SWT.BORDER | SWT.CALENDAR | SWT.DROP_DOWN);
            dateTime.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    validateMonths(dateTime);
                }
            });

            Composite buttonsComp = new Composite(dateSelectionDialog,
                    SWT.NONE);
            GridLayout buttonsGL = new GridLayout(1, false);
            buttonsGL.verticalSpacing = 40;
            buttonsGL.marginWidth = 12;
            buttonsComp.setLayout(buttonsGL);

            buttonOK = new Button(buttonsComp, SWT.PUSH);
            buttonOK.setText("Ok");
            GC gc = new GC(buttonOK);
            int gcMinWidth = gc.getFontMetrics().getAverageCharWidth() * 9;
            gc.dispose();
            GridData buttonGD1 = new GridData(SWT.FILL, SWT.FILL, true, true);
            buttonGD1.minimumWidth = gcMinWidth;
            buttonOK.setLayoutData(buttonGD1);

            buttonOK.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    // use selected date
                    // offset month due to DateTime using 0-11
                    ClimateDate newDate = new ClimateDate(dateTime.getDay(),
                            dateTime.getMonth() + 1, dateTime.getYear());

                    newDate = parentComp.validateDateBounds(newDate);
                    parentComp.setDate(newDate);

                    String selectedDate;
                    if (parentComp.isIncludeYear()) {
                        selectedDate = newDate.toFullDateString();
                    } else {
                        selectedDate = newDate.toMonthDayDateString();
                    }

                    parentComp.setText(selectedDate);

                    dateSelectionDialog.close();

                    parentComp.setTextFieldFocus();
                }
            });

            if (!parentComp.isDisableMissingBtn()) {
                final Button buttonSetMissing = new Button(buttonsComp,
                        SWT.PUSH);
                buttonSetMissing.setText("Missing");
                GridData buttonGD2 = new GridData(SWT.FILL, SWT.FILL, true,
                        true);
                buttonGD2.minimumWidth = gcMinWidth;
                buttonSetMissing.setLayoutData(buttonGD2);
                buttonSetMissing.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // use missing date string
                        if (parentComp.isIncludeYear()) {
                            parentComp.setText(
                                    ClimateDate.MISSING_FULL_DATE_STRING);
                        } else {
                            parentComp.setText(
                                    ClimateDate.MISSING_MONTH_DAY_DATE_STRING);
                        }

                        dateSelectionDialog.close();

                        parentComp.setTextFieldFocus();
                    }
                });
            }

            final Button buttonCancel = new Button(buttonsComp, SWT.PUSH);
            buttonCancel.setText("Cancel");
            GridData buttonGD3 = new GridData(SWT.FILL, SWT.FILL, true, true);
            buttonGD3.minimumWidth = gcMinWidth;
            buttonCancel.setLayoutData(buttonGD3);

            buttonCancel.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    dateSelectionDialog.close();
                    parentComp.setTextFieldFocus();
                }
            });

            ClimateDate currDate = parentComp.getDate();

            if (parentComp.isIncludeYear() && !currDate.toFullDateString()
                    .equals(ClimateDate.MISSING_FULL_DATE_STRING)) {
                // offset month due to DateTime using 0-11
                dateTime.setDate(currDate.getYear(), currDate.getMon() - 1,
                        currDate.getDay());
            } else if (!parentComp.isIncludeYear()
                    && !currDate.toMonthDayDateString().equals(
                            ClimateDate.MISSING_MONTH_DAY_DATE_STRING)) {
                // offset month due to DateTime using 0-11
                dateTime.setDate(currDate.getYear(), currDate.getMon() - 1,
                        currDate.getDay());
            }

            dateSelectionDialog.pack();
            dateSelectionDialog.open();
        }

        /**
         * validate the selected month
         * 
         * @param dateTime
         */
        private void validateMonths(DateTime dateTime) {
            int[] validMonths = parentComp.getValidMonths();

            if (!dateTime.isFocusControl() || validMonths == null) {
                return;
            }
            boolean isValid = false;
            for (int validMonth : validMonths) {
                if (dateTime.getMonth() == validMonth) {
                    isValid = true;
                    Calendar cal = Calendar.getInstance();
                    cal.clear();
                    cal.set(dateTime.getYear(), dateTime.getMonth(),
                            dateTime.getDay());
                    parentComp.setLastSelectedDate(cal);
                    break;
                }
            }
            if (!isValid) {
                String months[] = DateFormatSymbols.getInstance().getMonths();
                StringBuilder validMonthStr = new StringBuilder(
                        months[validMonths[0]]);
                for (int i = 1; i < validMonths.length; i++) {
                    validMonthStr.append(", ").append(months[validMonths[i]]);
                }
                String message = "User selected month ["
                        + months[dateTime.getMonth()]
                        + "] is invalid. Valid months are [" + validMonthStr
                        + "]";
                logger.warn(message);
                buttonOK.setEnabled(false);
                buttonOK.setToolTipText(message);
                // undo year, month, and day
                Calendar lastSelectedDate = parentComp.getLastSelectedDate();
                if (lastSelectedDate != null) {
                    dateTime.setYear(lastSelectedDate.get(Calendar.YEAR));
                    dateTime.setMonth(lastSelectedDate.get(Calendar.MONTH));
                    dateTime.setDay(
                            lastSelectedDate.get(Calendar.DAY_OF_MONTH));
                }
            } else {
                buttonOK.setEnabled(true);
                buttonOK.setToolTipText(null);
            }
        }
    }
}