/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.listeners;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * This class validates the user input for DTG (date-time-group) in a Text
 * Widget. A valid input should be in format of yyyyMMddHHmm (with "mm" as
 * optional). Optionally, it could be limited within a given date range and also
 * not one of the existing DTGs, if specified.
 *
 * This class intends to validate the user-typing inputs, programmatic use of
 * #setText() to set an invalid value could not be verified. Therefore, extra
 * care should be taken to ensure valid values are set using #setText.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#  Engineer    Description
 * -----------  -------- ----------- --------------------------
 * Jul 16, 2021 93152    jwu         Initial coding.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class DtgVerifyListener extends AbstractTextNumberListener {

    private static final String DTG_FORMAT = "yyyyMMddHH";

    private static final String DTG_FORMAT_LONG = "yyyyMMddHHmm";

    private static final DateTimeFormatter DTG_FORMATTER = DateTimeFormatter
            .ofPattern(DTG_FORMAT);

    private static final DateTimeFormatter DTG_FORMATTER_LONG = DateTimeFormatter
            .ofPattern(DTG_FORMAT_LONG);

    // A list of existing DTGs to be checked against, could be null.
    private List<String> existingDTGs;

    // Starting date and ending date to be checked against, could be null.
    private Date startDate;

    private Date endDate;

    /**
     * Constructor.
     *
     * Defines the criteria for the user's DTG input in a Text widget, which
     * should not be earlier than the "startDate" or later than the "endDate"
     * and also not be one of DTGs that already exist.
     *
     * @param eDtgs
     *            Existing DTGs
     * @param sDate
     *            earliest date allowed.
     * @param eDate
     *            latest date allowed.
     */
    public DtgVerifyListener(List<String> eDtgs, Date sDate, Date eDate) {
        this.existingDTGs = eDtgs;
        this.startDate = sDate;
        this.endDate = eDate;
    }

    @Override
    public void verifyText(Event event) {
        // verify user input only
        if (!((Control) event.widget).isFocusControl()) {
            return;
        }

        Text text = (Text) event.widget;

        // This is what's in the text field
        String originalText = text.getText();
        String newText = originalText.substring(0, event.start) + event.text
                + originalText.substring(event.end);

        /*
         * The text field should be digits only and less than 12 digits. Invalid
         * input will be highlighted as yellow.
         */
        int limit = DTG_FORMAT_LONG.length();
        if (!event.text.chars().allMatch(Character::isDigit)
                || (limit > 0 && newText.length() > limit)) {
            event.doit = false;
        } else {
            setBackground(text, isValidDtg(newText));
        }
    }

    @Override
    public void focusLost(Event e) {
        Text textField = (Text) e.widget;
        setBackground(textField, true);
    }

    /*
     * Check if the DTG is within the given range and not an existing DTG.
     *
     * @param dtg Input dtg string
     *
     * @return true - acceptable; false otherwise.
     */
    private boolean isValidDtg(String dtg) {
        boolean validDate = true;

        // Remove ending minutes if it is "00".
        if (dtg != null && dtg.length() == DTG_FORMAT_LONG.length()
                && dtg.endsWith("00")) {
            dtg = dtg.substring(0, DTG_FORMAT.length());
        }

        if (dtg == null
                || (dtg.length() != DTG_FORMAT.length()
                        && dtg.length() != DTG_FORMAT_LONG.length())
                || (existingDTGs != null && existingDTGs.contains(dtg))) {
            validDate = false;
        } else {
            Date dtgDate = parseDtg(dtg);
            if (dtgDate == null
                    || startDate != null && dtgDate.before(startDate)
                    || endDate != null && dtgDate.after(endDate)) {
                validDate = false;
            }
        }

        return validDate;
    }

    /*
     * Create a calendar from a given DTG String (yyyyMMddHH or yyyyMMddHHmm),
     * null if dtg is invalid.
     *
     * @param dtg DTG string in format of yyyyMMddHH or yyyyMMddHHmm
     *
     * @return Date
     */
    private Date parseDtg(String dtg) {
        DateTimeFormatter formatter = (dtg.length() > 10 ? DTG_FORMATTER_LONG
                : DTG_FORMATTER);

        try {
            return Date.from(LocalDateTime.parse(dtg, formatter)
                    .toInstant(ZoneOffset.UTC));
        } catch (DateTimeException e) { // NOSONAR
            return null;
        }
    }

}
