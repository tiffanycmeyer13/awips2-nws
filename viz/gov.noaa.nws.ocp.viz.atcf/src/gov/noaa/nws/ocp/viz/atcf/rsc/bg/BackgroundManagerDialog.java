/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc.bg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionNames;

/**
 * Allows user control of ATCF's display background.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 22, 2021 87890       dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 */
public class BackgroundManagerDialog extends CaveSWTDialog {

    protected static final int CONTRAST_THRESHOLD_SCALE = 500;

    protected BackgroundManager manager;

    /**
     * Background mode radio group
     */
    protected Button[] modeButtons;

    /**
     * Ocean color radio group
     */
    protected Button[] oceanColorButtons;

    protected Button autoAdjustResourceColorsButton;

    protected Scale constrastThresholdScale;

    protected Label constrastThresholdLabel;

    protected BackgroundManagerDialog(Shell parent, BackgroundManager manager) {
        super(parent, SWT.DIALOG_TRIM, CAVE.NONE);
        this.manager = manager;
        setText("ATFC Background");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite parent = shell;
        GridLayout gl = new GridLayout(2, false);
        Composite c;
        Label l;
        GridData gd;

        shell.setLayout(gl);

        l = new Label(parent, SWT.LEFT);
        l.setText("Mode:");
        c = new Composite(parent, SWT.NONE);
        c.setLayout(new RowLayout(SWT.HORIZONTAL));
        BackgroundManager.Mode[] modes = BackgroundManager.Mode.values();
        modeButtons = new Button[modes.length];

        SelectionListener modeListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onModeSelected(e);
            }
        };

        int i = 0;
        for (BackgroundManager.Mode mode : modes) {
            Button b = new Button(c, SWT.RADIO);
            b.setData(mode);
            b.setText(mode.getLabel());
            b.addSelectionListener(modeListener);
            modeButtons[i] = b;
            i++;
        }

        l = new Label(parent, SWT.LEFT);
        l.setText("Ocean Color:");
        c = new Composite(parent, SWT.NONE);
        c.setLayout(new RowLayout(SWT.HORIZONTAL));
        ColorSelectionNames[] oceanColors = { ColorSelectionNames.OCEAN,
                ColorSelectionNames.OCEAN2, ColorSelectionNames.OCEAN3 };
        oceanColorButtons = new Button[oceanColors.length];

        SelectionListener ocListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onOceanColorSelected(e);
            }
        };

        int j = 0;
        for (ColorSelectionNames color : oceanColors) {
            Button b = new Button(c, SWT.RADIO);
            b.setData(color);
            b.setText(color.getName());
            b.addSelectionListener(ocListener);
            oceanColorButtons[j] = b;
            j++;
        }

        gd = new GridData();
        gd.horizontalSpan = 2;
        autoAdjustResourceColorsButton = new Button(parent, SWT.CHECK);
        autoAdjustResourceColorsButton.setText("Auto-adjust resource colors");
        autoAdjustResourceColorsButton.setLayoutData(gd);
        autoAdjustResourceColorsButton
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        manager.setAutoAdjustResourceColors(
                                autoAdjustResourceColorsButton.getSelection());
                    }
                });

        l = new Label(parent, SWT.LEFT);
        l.setText("Threshold");

        c = new Composite(parent, SWT.NONE);
        gd = new GridData(CONTRAST_THRESHOLD_SCALE / 2, SWT.DEFAULT);
        gd.grabExcessHorizontalSpace = true;
        c.setLayoutData(gd);
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        c.setLayout(rl);

        constrastThresholdScale = new Scale(c, SWT.HORIZONTAL);
        constrastThresholdScale.setMinimum(0);
        constrastThresholdScale.setMaximum(CONTRAST_THRESHOLD_SCALE);
        constrastThresholdScale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                manager.setContrastThreshold(
                        constrastThresholdScale.getSelection()
                                / (float) CONTRAST_THRESHOLD_SCALE);
            }
        });
        RowData rd = new RowData(CONTRAST_THRESHOLD_SCALE / 2, SWT.DEFAULT);
        constrastThresholdScale.setLayoutData(rd);

        constrastThresholdLabel = new Label(c, SWT.LEFT);
        constrastThresholdLabel.setText("0.999");

        realizeState();
    }

    protected void onModeSelected(SelectionEvent e) {
        Object data = e.widget.getData();
        if (data instanceof BackgroundManager.Mode) {
            manager.setMode((BackgroundManager.Mode) data);
        }
    }

    protected void onOceanColorSelected(SelectionEvent e) {
        Object data = e.widget.getData();
        if (data instanceof ColorSelectionNames) {
            manager.setOceanColorSelection((ColorSelectionNames) data);
        }
    }

    /**
     * Set control state from BackgroundManager.
     */
    protected void realizeState() {
        if (!shell.isDisposed() && manager != null) {
            BackgroundManager.Mode mode = manager.getMode();
            for (Button b : modeButtons) {
                b.setSelection(b.getData() == mode);
            }
            ColorSelectionNames oceanColor = manager.getOceanColorSelection();
            for (Button b : oceanColorButtons) {
                b.setSelection(b.getData() == oceanColor);
            }
            autoAdjustResourceColorsButton
                    .setSelection(manager.isAutoAdjustResourceColors());
            constrastThresholdScale.setSelection(Math.round(
                    manager.getContrastThreshold() * CONTRAST_THRESHOLD_SCALE));
            constrastThresholdLabel.setText(
                    String.format("%.3f", manager.getContrastThreshold()));
        }
    }

}
