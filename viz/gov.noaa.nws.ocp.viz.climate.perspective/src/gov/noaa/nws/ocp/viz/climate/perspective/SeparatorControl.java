package gov.noaa.nws.ocp.viz.climate.perspective;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * The built-in separator element will not show in toolbars due to some unsolved
 * eclipse bug. Creating this control instead to use as a workaround.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 12, 2016  20744      wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 */

public class SeparatorControl extends WorkbenchWindowControlContribution {

    public SeparatorControl() {
    }

    public SeparatorControl(String id) {
        super(id);

    }

    @Override
    protected Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.SINGLE);
        composite.setLayout(new GridLayout(1, false));

        // Blank Space separator
        Label separator = new Label(composite, SWT.NONE);
        separator.setText("    ");

        return composite;
    }

}
