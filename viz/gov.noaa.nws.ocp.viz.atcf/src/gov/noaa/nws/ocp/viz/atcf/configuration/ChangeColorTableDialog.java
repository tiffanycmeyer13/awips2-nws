/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfCustomColors;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * Private dialog to edit the Color Table in the "Change Color" dialog
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2018 57338      dmanzella    Initial creation.
 * Apr 23, 2019 62025      dmanzella    Adjusted layout to match legacy
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChangeColorTableDialog extends CaveSWTDialog {

    /**
     * pulls the color tables from localization
     */
    private AtcfCustomColors colorTableColors;

    /**
     * Represents no current selection
     */
    private static final int UNSELECTED = -9999;

    /**
     * Number of cells in the color table
     */
    private static final int COLOR_TABLE_CELLS = 64;

    /**
     * Height and width for custom created color buttons
     */
    private static final int BUTTON_WIDTH = 33;

    private static final int BUTTON_HEIGHT = 17;

    /**
     * The paint portion of each colored square in the color table in the Color
     * Change submenu
     */
    private List<Canvas> editColorTableCanvases;

    /**
     * Represents the index of the selected color table color
     */
    private int selectedColorTableIndex = UNSELECTED;

    /**
     * Constructor
     *
     * @param parentShell
     */
    public ChangeColorTableDialog(Shell parentShell) {
        super(parentShell, SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
        setText("ATCF - Color Table");
    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        editColorTableCanvases = new ArrayList<>();
        if (AtcfConfigurationManager.getInstance() != null) {
            colorTableColors = AtcfConfigurationManager.getInstance()
                    .getAtcfCustomColors();
        }
        createColorTable(shell);
        createControlButtons(shell);

        shell.addListener(SWT.Close, e -> disposeAll());
    }

    /**
     * Creates the main color table
     *
     * @param parent
     */
    protected void createColorTable(Composite parent) {
        Composite topComposite = new Composite(parent, SWT.NONE);
        GridLayout topGridLayout = new GridLayout(2, false);
        topGridLayout.marginWidth = 10;
        topGridLayout.marginHeight = 10;
        topComposite.setLayout(topGridLayout);

        // Color table Composite
        Composite colorTableComposite = new Composite(topComposite, SWT.NONE);
        GridLayout colorTableGridLayout = new GridLayout(4, false);
        colorTableGridLayout.marginWidth = 15;
        colorTableGridLayout.marginHeight = 15;
        colorTableComposite.setLayout(colorTableGridLayout);

        GridData colorTableLabelGridData = new GridData(SWT.CENTER, SWT.FILL,
                false, true);
        colorTableLabelGridData.horizontalSpan = 4;

        Label colorTable = new Label(colorTableComposite, SWT.NONE);
        colorTable.setText("Color Table");
        colorTable.setLayoutData(colorTableLabelGridData);

        Display display = parent.getDisplay();

        // Color Table
        for (int kk = 0; kk < COLOR_TABLE_CELLS; kk++) {
            int index = (16 * (Math.floorMod(kk, 4))) + (kk / 4);

            GridData colorRowsGridData = new GridData(SWT.NONE, SWT.FILL, false,
                    true);
            colorRowsGridData.widthHint = BUTTON_WIDTH;
            colorRowsGridData.heightHint = BUTTON_HEIGHT;

            createColorTable(display, index, colorTableComposite,
                    colorRowsGridData, kk);
        }
    }

    /*
     * Handles adding the color picker to each canvas
     */
    private void addColorPicker() {
        ColorDialog color = new ColorDialog(shell);

        RGB currRgb = color.open();
        // only do all this if a color was selected
        if (currRgb != null && selectedColorTableIndex != UNSELECTED) {
            String hex = String.format("%02x%02x%02x", currRgb.red,
                    currRgb.green, currRgb.blue);

            List<String> colors = colorTableColors.getColors();

            colors.set(selectedColorTableIndex, hex);
            colorTableColors.setColors(colors);

            for (Canvas canvas : editColorTableCanvases) {
                canvas.redraw();
            }

        }
    }

    /*
     * Creates the color table
     *
     * @param display
     *
     * @param index
     *
     * @param composite
     *
     * @param griddata
     *
     * @param selected
     */
    private void createColorTable(Display display, int index,
            Composite composite, GridData griddata, int selected) {
        Canvas canvas = new Canvas(composite, SWT.NONE);
        canvas.setLayoutData(griddata);
        canvas.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                // Not used.
            }

            @Override
            public void mouseDown(MouseEvent e) {

                /*
                 * For highlighting the square clicked on, and un-highlighting
                 * the old one
                 */
                selectedColorTableIndex = index;

                for (Canvas canvas : editColorTableCanvases) {
                    canvas.setForeground(
                            display.getSystemColor(SWT.COLOR_BLACK));
                }

                editColorTableCanvases.get(selected)
                        .setForeground(display.getSystemColor(SWT.COLOR_WHITE));

                editColorTableCanvases.get(selected).redraw();
                addColorPicker();
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Not used
            }
        });

        canvas.addPaintListener(e -> {
            Color colour = AtcfDataUtil.atcfCustomColorToColor(shell,
                    colorTableColors, index);
            e.gc.setBackground(colour);
            e.gc.fillRectangle(0, 0, 32, 16);
            e.gc.drawRectangle(0, 0, 32, 16);

            colour.dispose();
        });
        editColorTableCanvases.add(canvas);
    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {

        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 15;
        buttonLayout.marginHeight = 10;
        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        Button okButton = new Button(buttonComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfConfigurationManager.getInstance()
                        .saveCustomColors(colorTableColors);
                disposeAll();
                close();
            }
        });

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                disposeAll();
                close();
            }
        });

        cancelButton.setFocus();
    }

    /**
     * Dispose all resources
     */
    protected void disposeAll() {
        for (Canvas canvas : editColorTableCanvases) {
            canvas.dispose();
        }
    }

}