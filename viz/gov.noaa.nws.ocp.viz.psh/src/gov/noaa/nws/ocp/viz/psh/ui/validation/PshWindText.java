/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Special text field for wind direction/speed input in PSH Generator tables.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2017 #39233      wpaintsil   Initial creation.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshWindText extends PshAbstractText {
    /**
     * Regular expression for what the user is allowed to input.
     */
    private static final String WIND_INPUT_REGEX = "\\w{3}[/]\\w{3}";

    /**
     * Regular expression for what is a valid direction/speed
     */
    private static final String WIND_VALIDATION_REGEX = "(\\d{3}|VRB)[/]\\d{3}";

    private static final String DEFAULT_WIND = "000/000";

    private static final String WIND_TEMPLATE = "___/___";

    public PshWindText(Composite parent, boolean emptyAllowed) {
        super(parent, emptyAllowed);

        // Add listeners
        addFocusListener();

        textField.setToolTipText(
                "Valid wind format: '###/###' (wind direction and speed)."
                        + "\nExamples: 321/012, VRB/012");
    }

    @Override
    protected VerifyListener verifyListener() {

        return new VerifyListener() {

            // create the pattern for verification
            Pattern pattern = Pattern.compile(WIND_INPUT_REGEX);

            // ignore event when caused by inserting text inside event
            // handler
            boolean ignore;

            @Override
            public void verifyText(VerifyEvent event) {
                // verify user input only
                if (!((Control) event.widget).isFocusControl()) {
                    return;
                }
                if (ignore) {
                    return;
                }
                event.doit = false;
                if (event.start > 7 || event.end > 8) {
                    return;
                }
                StringBuilder builder = new StringBuilder(event.text);

                // handle backspace
                if (event.character == '\b') {
                    for (int ii = event.start; ii < event.end; ii++) {
                        // skip over separator
                        if (ii == 3) {
                            if (event.start + 1 == event.end) {
                                builder.append(new char[] { '_', '/' });
                                event.start--;
                            } else {
                                builder.append('/');
                            }
                        } else {
                            builder.append('_');
                        }
                    }
                    textField.setSelection(event.start,
                            event.start + builder.length());
                    ignore = true;
                    textField.insert(builder.toString());
                    ignore = false;
                    // move cursor backwards over separator
                    if (event.start == 4)
                        event.start--;
                    textField.setSelection(event.start, event.start);
                    return;
                }

                // handle delete
                if (event.character == (char) 127) {
                    for (int ii = event.start; ii < event.end; ii++) {
                        // skip over separator
                        if (ii == 3) {
                            if (event.start + 1 == event.end) {
                                builder.append(new char[] { '_' });
                                event.start++;
                            } else {
                                builder.append('/');
                            }
                        } else {
                            builder.append('_');
                        }
                    }
                    textField.setSelection(event.start,
                            event.start + builder.length());
                    ignore = true;
                    textField.insert(builder.toString());
                    ignore = false;
                    textField.setSelection(event.start, event.start);
                    return;
                }

                StringBuffer newText = new StringBuffer(DEFAULT_WIND);
                char[] chars = event.text.toCharArray();
                int index = event.start - 1;
                for (int ii = 0; ii < event.text.length(); ii++) {
                    index++;

                    if (index == 3) {
                        if (chars[ii] == '/') {
                            continue;
                        }
                        index++;
                    }

                    if (index >= newText.length())
                        return;
                    newText.setCharAt(index, chars[ii]);
                }
                // if text is selected, do not paste beyond range of selection
                if (event.start < event.end && index + 1 != event.end)
                    return;
                Matcher matcher = pattern.matcher(newText);
                if (matcher.lookingAt() || event.character == ' ') {
                    textField.setSelection(event.start, index + 1);
                    ignore = true;
                    // handle white space
                    if (event.character == ' ') {
                        if (event.start == 3) {
                            textField.insert("/_");
                        } else {
                            textField.insert("_");
                        }
                    } else {
                        textField.insert(
                                newText.substring(event.start, index + 1)
                                        .toUpperCase());
                        // "VRB" might be an input.
                        // Make all letter inputs uppercase.
                    }
                    ignore = false;
                }
            }
        };

    }

    @Override
    protected String validationRegex() {
        return WIND_VALIDATION_REGEX;
    }

    @Override
    protected String template() {
        return WIND_TEMPLATE;
    }

}
