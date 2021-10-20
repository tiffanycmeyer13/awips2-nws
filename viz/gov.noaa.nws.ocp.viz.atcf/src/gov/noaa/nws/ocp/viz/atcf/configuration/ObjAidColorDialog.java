/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfCustomColors;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechniques;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * Dialog to display colors for Obj. Aids within "Change Color" dialog.
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
public class ObjAidColorDialog extends CaveSWTDialog {

    /**
     * Represents no current selection
     */
    private static final int UNSELECTED = -9999;

    /**
     * pulls the color tables from localization
     */
    private AtcfCustomColors colorTableColors;

    /**
     * Number of cells in the color table
     */
    private static final int COLOR_TABLE_CELLS = 64;

    /**
     * List of indeces from the color table where all the colors on the
     * Objective Aids GUI are pulled from
     */
    private ObjectiveAidTechniques objectiveAidTechniques;

    /**
     * Height and width for custom created color buttons
     */
    private static final int BUTTON_WIDTH = 33;

    private static final int SQUARE_BUTTON_HEIGHT = 45;

    private static final int BUTTON_HEIGHT = 17;

    /**
     * Represents the index of the selected obj aid color
     */
    private int selectedChangeIndex = UNSELECTED;

    /**
     * Represents the index of the selected color table color
     */
    private int selectedColorTableIndex = UNSELECTED;

    /**
     * The paint portion of each colored square in the Objective Aids GUI
     */
    private ArrayList<Canvas> objAidCanvases;

    /**
     * The paint portion of each colored square in the color table in the
     * objective aid submenu
     */
    private ArrayList<Canvas> objAidColorTableCanvases;

    /**
     * List of all the non-retired Obj Aids tech entries.
     */
    private ArrayList<ObjectiveAidTechEntry> currentObjAidsNames;

    /**
     * Constructor
     *
     * @param parentShell
     */
    public ObjAidColorDialog(Shell parentShell) {
        super(parentShell, SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
        setText("ATCF - Objective Aids Color Preferences");
    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComposite.setLayoutData(mainLayoutData);

        objectiveAidTechniques = AtcfConfigurationManager.getInstance()
                .getObjectiveAidTechniques();

        if (AtcfConfigurationManager.getInstance() != null) {
            colorTableColors = AtcfConfigurationManager.getInstance()
                    .getAtcfCustomColors();
        }

        currentObjAidsNames = new ArrayList<>();
        objAidColorTableCanvases = new ArrayList<>();
        objAidCanvases = new ArrayList<>();

        // when the GUI is closed, dispose everything
        shell.addListener(SWT.Close, e -> {
            selectedChangeIndex = UNSELECTED;
            selectedColorTableIndex = UNSELECTED;
            disposeAll();
        });

        populateObjAids();
        createColoredSquares(shell);
        createControlButtons(shell);
    }

    /**
     * Create a list of objective aids from the non-retired entries
     */
    protected void populateObjAids() {
        for (ObjectiveAidTechEntry entry : objectiveAidTechniques
                .getObjectiveAidTechniques()) {
            if (!entry.isRetired()) {
                currentObjAidsNames.add(entry);
            }
        }
    }

    /**
     * Dispose all resources
     */
    protected void disposeAll() {
        for (Canvas canvas : objAidColorTableCanvases) {
            canvas.dispose();
        }
        for (Canvas canvas : objAidCanvases) {
            canvas.dispose();
        }
    }

    /**
     * Creates the colored squares
     *
     * @param parent
     */
    protected void createColoredSquares(Composite parent) {

        Composite topComposite = new Composite(parent, SWT.NONE);
        GridLayout topGridLayout = new GridLayout(2, false);
        topGridLayout.marginWidth = 10;
        topGridLayout.marginHeight = 10;
        topComposite.setLayout(topGridLayout);

        Group first = new Group(topComposite, SWT.NONE);
        first.setText("Objective Aids");
        first.setLayout(new GridLayout(1, false));
        GridData firstData = new GridData(SWT.FILL, SWT.FILL, true, false);
        firstData.widthHint = 775;
        first.setLayoutData(firstData);

        int numColumns = currentObjAidsNames.size() / 20;

        ScrolledComposite colorRowsScroll = new ScrolledComposite(first,
                SWT.H_SCROLL);
        colorRowsScroll.setLayout(new GridLayout(1, false));
        GridData scrollGridData = new GridData(SWT.FILL, SWT.FILL, true, true);

        colorRowsScroll.setLayoutData(scrollGridData);

        // First Composite
        Composite colorRowsComposite = new Composite(colorRowsScroll, SWT.NONE);
        GridLayout colorRowsGridLayout = new GridLayout(numColumns, true);

        colorRowsComposite.setLayout(colorRowsGridLayout);

        GridData labelRowsGridData = new GridData(SWT.NONE, SWT.FILL, false,
                true);
        labelRowsGridData.widthHint = SQUARE_BUTTON_HEIGHT;
        labelRowsGridData.heightHint = BUTTON_HEIGHT;

        GridData colorRowsGridData = new GridData(SWT.NONE, SWT.FILL, false,
                true);
        colorRowsGridData.widthHint = BUTTON_WIDTH;
        colorRowsGridData.heightHint = BUTTON_HEIGHT;

        Display display = parent.getDisplay();

        // Colored squares
        for (int ii = 0; ii < numColumns; ii++) {
            Composite tempComp = new Composite(colorRowsComposite, SWT.NONE);
            GridLayout tempGridLayout = new GridLayout(2, false);
            tempGridLayout.marginRight = 12;
            tempComp.setLayout(tempGridLayout);

            for (int jj = 0; jj < 20; jj++) {
                createObjectiveAidSquare(tempComp, colorRowsGridData, display,
                        16, 16, (jj + (20 * ii)));
                Label colorLabel = new Label(tempComp, SWT.NONE);
                colorLabel.setText(
                        currentObjAidsNames.get((jj + (20 * ii))).getName());
                colorLabel.setLayoutData(labelRowsGridData);
            }
        }
        colorRowsScroll.setContent(colorRowsComposite);
        colorRowsScroll.setExpandHorizontal(true);
        colorRowsScroll.setExpandVertical(true);
        colorRowsScroll.setMinSize(
                colorRowsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // Second Composite
        Composite colorTableComposite = new Composite(topComposite, SWT.NONE);
        GridLayout colorTableGridLayout = new GridLayout(4, false);
        colorTableGridLayout.marginWidth = 15;
        colorTableGridLayout.marginHeight = 15;
        GridData colorTableGridData = new GridData();
        colorTableComposite.setLayout(colorTableGridLayout);
        colorTableComposite.setLayoutData(colorTableGridData);

        GridData colorTableLabelGridData = new GridData(SWT.CENTER, SWT.FILL,
                false, true);
        colorTableLabelGridData.horizontalSpan = 4;

        Label colorTable = new Label(colorTableComposite, SWT.NONE);
        colorTable.setText("Color Table");
        colorTable.setLayoutData(colorTableLabelGridData);

        // Color Table
        for (int kk = 0; kk < COLOR_TABLE_CELLS; kk++) {
            int index = (16 * (Math.floorMod(kk, 4))) + kk / 4;

            createColorTable(index, display, colorTableComposite,
                    colorRowsGridData, kk);
        }

    }

    /**
     * Creates the color table
     *
     * @param kk
     * @param display
     * @param colorTableComposite
     * @param colorTableGridData
     */
    private void createColorTable(int index, Display display,
            Composite composite, GridData griddata, int selected) {
        Canvas canvas = new Canvas(composite, SWT.NONE);
        canvas.setLayoutData(griddata);

        canvas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                /*
                 * For highlighting the square clicked on, and un-highlighting
                 * the old one
                 */
                selectedColorTableIndex = index;

                for (Canvas canvas : objAidColorTableCanvases) {
                    canvas.setForeground(
                            display.getSystemColor(SWT.COLOR_BLACK));
                    canvas.redraw();
                }

                objAidColorTableCanvases.get(selected)
                        .setForeground(display.getSystemColor(SWT.COLOR_WHITE));
                objAidColorTableCanvases.get(selected).redraw();

                selectColor();
                if (selectedChangeIndex != UNSELECTED) {
                    objAidCanvases.get(selectedChangeIndex - 74).redraw();
                }
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

        objAidColorTableCanvases.add(canvas);
    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {

        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 190;
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
                        .saveObjectiveAidTechniques(objectiveAidTechniques);
                selectedChangeIndex = UNSELECTED;
                selectedColorTableIndex = UNSELECTED;
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
                selectedChangeIndex = UNSELECTED;
                selectedColorTableIndex = UNSELECTED;
                disposeAll();
                close();
            }
        });
        cancelButton.setFocus();
    }

    /*
     * Creates a colored square to be changed by the Color Table in the
     * Objective Aids GUI
     *
     * @param composite
     *
     * @param griddata
     *
     * @param display
     *
     * @param background
     *
     * @param width
     *
     * @param height
     *
     * @param index
     */
    private void createObjectiveAidSquare(Composite composite,
            GridData griddata, Display display, int width, int height,
            int index) {
        Canvas squareCanvas = new Canvas(composite, SWT.NONE);
        squareCanvas.setLayoutData(griddata);
        squareCanvas.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        squareCanvas.addPaintListener(e -> {
            ObjectiveAidTechEntry entry = currentObjAidsNames.get(index);
            Color colour = AtcfDataUtil.atcfCustomColorToColor(shell,
                    colorTableColors, entry.getColor());

            e.gc.setBackground(colour);
            e.gc.fillRectangle(0, 0, width, height);
            e.gc.drawRectangle(0, 0, width, height);

            colour.dispose();

        });

        squareCanvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Not used.
            }

            @Override
            public void mouseDown(MouseEvent e) {
                /*
                 * For highlighting the square clicked on, and un-highlighting
                 * the old one
                 */
                selectedChangeIndex = index + 74;

                for (Canvas canvas : objAidCanvases) {
                    canvas.setForeground(
                            display.getSystemColor(SWT.COLOR_BLACK));
                    canvas.redraw();
                }

                objAidCanvases.get(index)
                        .setForeground(display.getSystemColor(SWT.COLOR_WHITE));
            }

            @Override
            public void mouseUp(MouseEvent e) {
                // Not used.
            }
        });
        objAidCanvases.add(squareCanvas);
    }

    /**
     * The logic that goes into matching to save a color
     */
    private void selectColor() {
        // color selected
        List<ObjectiveAidTechEntry> tempObjectiveAidSelections = objectiveAidTechniques
                .getObjectiveAidTechniques();
        if (selectedChangeIndex > 73 && selectedChangeIndex != UNSELECTED) {
            currentObjAidsNames.get(selectedChangeIndex - 74)
                    .setColor(selectedColorTableIndex);
        }
        objectiveAidTechniques
                .setObjectiveAidTechniques(tempObjectiveAidSelections);
    }
}