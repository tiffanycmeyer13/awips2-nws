/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.validation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

/**
 * Special Combo used in Metar/Non-Metar/Marine tabs.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 20, 2017 #39233      wpaintsil   Initial creation.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public class PshCombo extends PshAbstractControl {

    private CCombo textCombo;

    public PshCombo(Composite parent) {
        super(parent);
        textCombo = new CCombo(this, SWT.BORDER | SWT.READ_ONLY);
        textCombo
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Add listeners
        addFocusListener();
    }

    /**
     * @return the textCombo
     */
    public CCombo getTextCombo() {
        return textCombo;
    }

    protected void addFocusListener() {
        textCombo.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {

                highlightInput();
            }
        });

    }

    @Override
    public boolean highlightInput() {
        valid = !textCombo.getText().isEmpty();

        Color bgColor = valid
                ? textCombo.getDisplay().getSystemColor(SWT.COLOR_WHITE)
                : textCombo.getDisplay().getSystemColor(SWT.COLOR_YELLOW);

        textCombo.setBackground(bgColor);

        return valid;
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        textCombo.addListener(eventType, listener);

    }

    public void addSelectionListener(SelectionListener listener) {
        textCombo.addSelectionListener(listener);
    }

    @Override
    public String getText() {
        return textCombo.getText();
    }

    @Override
    public void setText(String value) {
        textCombo.setText(value);

    }

    public void add(String listItem) {
        textCombo.add(listItem);
    }

    public int getSelectionIndex() {
        return textCombo.getSelectionIndex();
    }

    public void select(int index) {
        textCombo.select(index);
    }

}
