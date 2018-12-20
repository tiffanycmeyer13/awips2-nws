/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.validation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Special text field used in PSH Generator tables.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2017 VL39233     wpaintsil  Initial creation.
 * Dec 17, 2018 DR20981     jwu        Allow immediate type-in in date/time
 *                                     wind text field when user tabs
 *                                     over to such a field.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public abstract class PshAbstractText extends PshAbstractControl {

    protected Text textField;

    protected boolean valid = true;

    protected boolean emptyAllowed;

    private VerifyListener verifyListener;

    /**
     * Constructor; by default, the text field can be left empty.
     * 
     * @param parent
     */
    public PshAbstractText(Composite parent) {
        this(parent, true);

    }

    /**
     * Constructor
     * 
     * @param parent
     * @param emptyAllowed
     *            true if the text field can be left empty; false otherwise.
     */
    public PshAbstractText(Composite parent, boolean emptyAllowed) {
        super(parent);

        this.emptyAllowed = emptyAllowed;

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        this.setLayout(layout);
        this.setLayoutData(layoutData);

        textField = new Text(this, SWT.BORDER);
        textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        verifyListener = verifyListener();

    }

    protected void addFocusListener() {

        // move cursor to the beginning of the text if empty
        textField.addMouseListener(new MouseAdapter() {

            public void mouseUp(MouseEvent e) {
                if (template() != null
                        && textField.getText().equals(template())) {
                    textField.setSelection(0);
                }

            }
        });

        textField.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {

                // VerifyListener interferes with setText()
                if (verifyListener != null) {
                    textField.removeVerifyListener(verifyListener);
                }

                // Remove the input template if there was no input.
                if (template() != null
                        && textField.getText().equals(template())) {
                    textField.setText("");
                }

                highlightInput();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Show the input template if the text field is empty.
                if (template() != null && textField.getText().isEmpty()) {
                    textField.setText(template());
                }

                if (verifyListener != null) {
                    textField.addVerifyListener(verifyListener);
                }
            }
        });

        /*
         * DR20981 - Allow user to type in immediately when user tabs to this
         * text field.
         */
        textField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.TAB) {
                    // when tabs over, set cursor at beginning.
                    textField.setSelection(0);
                }
            }
        });

    }

    /**
     * Change background color to red if the input is invalid.
     * 
     * @return true if the input is valid, false otherwise.
     */
    public boolean highlightInput() {
        valid = (validationRegex() != null
                && textField.getText().matches(validationRegex()))
                || (emptyAllowed && textField.getText().isEmpty())
                || (validationRegex() == null && !emptyAllowed
                        && !textField.getText().isEmpty());

        Color bgColor = valid
                ? textField.getDisplay().getSystemColor(SWT.COLOR_WHITE)
                : textField.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

        textField.setBackground(bgColor);

        return valid;
    }

    /**
     * Add a listener to the text field.
     * 
     * @param eventType
     * @param listener
     */
    public void addListener(int eventType, Listener listener) {
        textField.addListener(eventType, listener);
    }

    /**
     * Return the text field text.
     * 
     * @return textField
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * Set the text field text.
     * 
     * @param value
     */
    public void setText(String value) {
        if (textField != null) {
            textField.setText(value);
        }
    }

    /**
     * @return true if the input is valid, false otherwise.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return true if the text field can be left empty; false otherwise.
     */
    public boolean isEmptyAllowed() {
        return emptyAllowed;
    }

    /**
     * @param emptyAllowed
     *            true if the text field can be left empty; false otherwise.
     */
    public void setEmptyAllowed(boolean emptyAllowed) {
        this.emptyAllowed = emptyAllowed;
    }

    public void addModifyListener(ModifyListener modifyListener) {
        if (textField != null) {
            textField.addModifyListener(modifyListener);
        }

    }

    @Override
    public boolean isFocusControl() {
        if (textField != null) {
            return textField.isFocusControl();
        }
        return false;
    }

    /**
     * @return a VerifyListener for the input
     */
    protected abstract VerifyListener verifyListener();

    /**
     * @return a regular expression used to check whether the text is valid
     */
    protected abstract String validationRegex();

    /**
     * @return a template guiding the user on how to enter the text
     */
    protected abstract String template();
}
