/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfColorSelections;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfCustomColors;
import gov.noaa.nws.ocp.common.atcf.configuration.ColorSelectionEntry;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Change Color.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 07, 2018 57338       dmanzella   initial creation
 * Apr 23, 2019 62025       dmanzella   Adjusted layout to match legacy
 * Nov 10, 2020 84442       wpaintsil   Add Scrollbars.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChangeColorDialog extends OcpCaveSWTDialog {

    /**
     * Represents no current selection
     */
    private static final int UNSELECTED = -9999;

    private static final String RAW_DATA_WIND = "Raw Data Wind";

    private static final String[] firstColumnNames = { "Map Background",
            "Ocean Color 1", "Ocean Color 2", "Ocean Color 3", "Land",
            "Map Coastlines", "Lat / Lon lines", "Storm and Chart Titles",
            "Warning/CPA labels", "Prompt / Status line", "ATCF ID logo",
            "Forecast Track", "Forecast Labels", "TCFA", "34 knot winds",
            "50 knot winds", "64 knot winds", "Re-Best Track",
            "Objective Best Track", "Old Obj. Best Track", "Danger Area",
            "12 ft seas radii", "Radius of Max Wind" };

    private static final String[] secondColumnNames = {
            "Outermost Closed Isobar", "Aircraft Fix", "Satellite Fix",
            "Microwave Fix", "Radar Fix", "Analysis Fix", "Scatterometer Fix",
            "Obj. Dvorak Fix", "Dropsonde Fix", "Research Fix",
            "Non-Center Fix", "Flagged Fix", "Old Fix", RAW_DATA_WIND,
            RAW_DATA_WIND, RAW_DATA_WIND, RAW_DATA_WIND, "Pre-Synoptic",
            "Synoptic Ob", "Post-Synoptic", "Early UpperAir",
            "Current UpperAir", "Late UpperAir", "Early Aircraft",
            "Current Aircraft", "Late Aircraft" };

    private static final String[] columnTitles = { "Fields", " Best Tracks",
            "Catagory 1 - 5 Hurricanes" };

    private Button helpButton;

    private Button okButton;

    private Button cancelButton;

    private Button objAidButton;

    private Button colorTableButton;

    /**
     * Represents the index of the selected item to change
     */
    private int selectedChangeIndex = UNSELECTED;

    /**
     * Represents the index of the selected color table color
     */
    private int selectedColorTableIndex = UNSELECTED;

    /**
     * Number of cells in the color table
     */
    private static final int COLOR_TABLE_CELL = 64;

    /**
     * Height and width for custom created color buttons
     */
    private static final int BUTTON_WIDTH = 33;

    private static final int BUTTON_HEIGHT = 17;

    /**
     * pulls the color tables from localization
     */
    private AtcfCustomColors colorTableColors;

    /**
     * List of indices from the color table where the colors on the main GUI are
     * pulled from
     */
    private AtcfColorSelections colorSelections;

    /**
     * The paint portion of each colored square
     */
    private List<Canvas> canvases;

    /**
     * The paint portion of each colored square in the color table
     */
    private List<Canvas> colorTableCanvases;

    /**
     * Maps a square on the GUI (0-73) to it's ColorSelectionEntry. This is
     * necessary because the colorSelectionEntries are stored out of order and
     * there are unused ones that don't map to anything
     */
    private Map<Integer, ColorSelectionEntry> screenColorSelectionIndeces;

    /**
     * Same as screenColorSelectionIndeces, but for the plotter entries
     */
    private Map<Integer, ColorSelectionEntry> plotterColorSelectionIndeces;

    /**
     * Same as screenColorSelectionIndeces, but for the printer entries
     */
    private Map<Integer, ColorSelectionEntry> printerColorSelectionIndeces;

    /**
     * The current display option, can be either Screen, Printer, or Plotter
     */
    private displayType currDisplay = displayType.SCREEN;

    /**
     * defines the different color display options
     */
    enum displayType {
        SCREEN, PRINTER, PLOTTER
    }

    /**
     * Constructor
     *
     * @param parent
     */
    public ChangeColorDialog(Shell parent) {
        super(parent);
        setText("ATCF - Map Display Color Preferences");
    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite mainComposite = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComposite.setLayoutData(mainLayoutData);

        if (AtcfConfigurationManager.getInstance() != null) {
            colorTableColors = AtcfConfigurationManager.getInstance()
                    .getAtcfCustomColors();
            colorSelections = AtcfConfigurationManager.getInstance()
                    .getAtcfColorSelections();
        }

        canvases = new ArrayList<>();
        colorTableCanvases = new ArrayList<>();

        mapColorSelections();
        createDisplayArea(mainComposite);
        createControlButtons(mainComposite);

        // When the shell comes back into focus, redraw changes made
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                redrawAll();
            }
        });

        scrollComposite.setContent(mainComposite);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setMinSize(
                mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    /**
     * Redraw everything
     */
    public void redrawAll() {
        colorTableColors = AtcfConfigurationManager.getInstance()
                .getAtcfCustomColors();

        for (int ii = 0; ii < canvases.size(); ii++) {
            ColorSelectionEntry entry;

            if (currDisplay == displayType.SCREEN) {
                entry = screenColorSelectionIndeces.get(ii);
            } else if (currDisplay == displayType.PRINTER) {
                entry = printerColorSelectionIndeces.get(ii);

            } else {
                entry = plotterColorSelectionIndeces.get(ii);
            }

            Color color = AtcfDataUtil.atcfCustomColorToColor(shell,
                    colorTableColors, entry.getColorIndex()[0]);
            canvases.get(ii).setBackground(color);
            canvases.get(ii).redraw();

            color.dispose();
        }

        for (int jj = 0; jj < colorTableCanvases.size(); jj++) {
            Color color = AtcfDataUtil.atcfCustomColorToColor(shell,
                    colorTableColors, jj);

            colorTableCanvases.get(jj).setBackground(color);
            colorTableCanvases.get(jj).redraw();

            color.dispose();

        }
    }

    /**
     * maps the color selections
     */
    protected void mapColorSelections() {
        screenColorSelectionIndeces = new HashMap<>();
        printerColorSelectionIndeces = new HashMap<>();
        plotterColorSelectionIndeces = new HashMap<>();

        for (int ii = 0; ii < colorSelections.getMapDisplayColors()
                .size(); ii++) {

            ColorSelectionEntry screenEntry = colorSelections
                    .getMapDisplayColors().get(ii);

            ColorSelectionEntry plotEntry = colorSelections.getPlotterColors()
                    .get(ii);

            ColorSelectionEntry printerEntry = colorSelections
                    .getPrinterColors().get(ii);

            if (ii < 8 || (ii > 10 && ii < 14)) {
                // Map Background -> Storm Chart Titles, Forecast Track -> TCFA
                screenColorSelectionIndeces.put(ii, screenEntry);
                plotterColorSelectionIndeces.put(ii, plotEntry);
                printerColorSelectionIndeces.put(ii, printerEntry);
            } else if (ii == 8) {
                // ATCF ID logo
                screenColorSelectionIndeces.put(10, screenEntry);
                plotterColorSelectionIndeces.put(10, plotEntry);
                printerColorSelectionIndeces.put(10, printerEntry);
            } else if (ii > 8 && ii < 11) {
                // Warning/CPA labels -> Prompt/Status line
                screenColorSelectionIndeces.put(ii - 1, screenEntry);
                plotterColorSelectionIndeces.put(ii - 1, plotEntry);
                printerColorSelectionIndeces.put(ii - 1, printerEntry);
            } else if (ii > 15 && ii < 19) {
                // 34 knot winds -> 64 knot winds
                screenColorSelectionIndeces.put(ii - 2, screenEntry);
                plotterColorSelectionIndeces.put(ii - 2, plotEntry);
                printerColorSelectionIndeces.put(ii - 2, printerEntry);
            } else if ((ii > 19 && ii < 29) || (ii > 33 && ii < 52)) {
                // Re-Best Track -> Satellite Fix, Research Fix -> Late Aircraft
                screenColorSelectionIndeces.put(ii - 3, screenEntry);
                plotterColorSelectionIndeces.put(ii - 3, plotEntry);
                printerColorSelectionIndeces.put(ii - 3, printerEntry);
            } else if (ii == 29) {
                // Obj. Dvorak Fix
                screenColorSelectionIndeces.put(30, screenEntry);
                plotterColorSelectionIndeces.put(30, plotEntry);
                printerColorSelectionIndeces.put(30, printerEntry);
            } else if (ii > 29 && ii < 34) {
                // Microwave Fix -> Scatterometer Fix
                screenColorSelectionIndeces.put(ii - 4, screenEntry);
                plotterColorSelectionIndeces.put(ii - 4, plotEntry);
                printerColorSelectionIndeces.put(ii - 4, printerEntry);
            } else if (ii > 51) {
                // Catagory 1 - 5 Hurricanes
                screenColorSelectionIndeces.put(ii + 17, screenEntry);
                plotterColorSelectionIndeces.put(ii + 17, plotEntry);
                printerColorSelectionIndeces.put(ii + 17, printerEntry);
            } else if (ii == 14) {
                for (int jj = 49; jj < 59; jj++) {
                    // Fields
                    screenColorSelectionIndeces.put(jj, screenEntry);
                    plotterColorSelectionIndeces.put(jj, plotEntry);
                    printerColorSelectionIndeces.put(jj, printerEntry);
                }
            } else if (ii == 15) {
                for (int jj = 59; jj < 69; jj++) {
                    // Best Tracks
                    screenColorSelectionIndeces.put(jj, screenEntry);
                    plotterColorSelectionIndeces.put(jj, plotEntry);
                    printerColorSelectionIndeces.put(jj, printerEntry);
                }
            }
        }

    }

    /**
     * This method populates the visual elements
     *
     * @param parent
     */
    protected void createDisplayArea(Composite parent) {
        Composite topComposite = new Composite(parent, SWT.NONE);
        GridLayout topGridLayout = new GridLayout(5, false);
        topGridLayout.marginWidth = 15;
        topGridLayout.marginHeight = 15;
        GridData topGridData = new GridData(975, 650);
        topComposite.setLayout(topGridLayout);
        topComposite.setLayoutData(topGridData);

        Display display = parent.getDisplay();

        Font fixedWidthFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        // Composite for far left column, Map Background -> Radius of Max Wind

        Composite oceanGrpComp = new Composite(topComposite, SWT.NONE);
        GridLayout oceanGrpLayout = new GridLayout();
        oceanGrpLayout.marginBottom = 290;
        oceanGrpComp.setLayout(oceanGrpLayout);

        Group oceanColorBtnsGrp = new Group(oceanGrpComp, SWT.NONE);
        GridData oceanGridData = new GridData();
        oceanGridData.verticalSpan = 24;

        oceanColorBtnsGrp.setLayout(new RowLayout(SWT.VERTICAL));
        oceanColorBtnsGrp.setLayoutData(oceanGridData);

        Button oceanOneBtn = new Button(oceanColorBtnsGrp, SWT.RADIO);
        oceanOneBtn.setSelection(true);
        Button oceanTwoBtn = new Button(oceanColorBtnsGrp, SWT.RADIO);
        Button oceanThreeBtn = new Button(oceanColorBtnsGrp, SWT.RADIO);

        Composite leftNamesComposite = new Composite(topComposite, SWT.NONE);
        GridLayout leftTopGridLayout = new GridLayout(2, false);
        leftTopGridLayout.marginWidth = 15;
        leftTopGridLayout.marginHeight = 15;
        leftNamesComposite.setLayout(leftTopGridLayout);

        // GridData for the colored squares
        GridData colorRowsGridData = new GridData(SWT.LEFT, SWT.FILL, true,
                true);
        colorRowsGridData.widthHint = BUTTON_WIDTH;
        colorRowsGridData.heightHint = BUTTON_HEIGHT;

        GridData labelRowsGridData = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        labelRowsGridData.widthHint = 185;

        Group colorModeGroup = new Group(leftNamesComposite, SWT.BORDER);
        colorModeGroup.setLayout(new RowLayout(SWT.VERTICAL));
        GridData groupGridData = new GridData(SWT.NONE, SWT.NONE, false, false);
        groupGridData.horizontalSpan = 2;
        colorModeGroup.setLayoutData(groupGridData);

        Button screen = new Button(colorModeGroup, SWT.RADIO);
        screen.setText("Screen Colors");
        screen.setSelection(true);
        screen.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currDisplay = displayType.SCREEN;
                redrawAll();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                currDisplay = displayType.SCREEN;
            }
        });

        Button printer = new Button(colorModeGroup, SWT.RADIO);
        printer.setText("Printer Colors");
        printer.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currDisplay = displayType.PRINTER;
                redrawAll();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                currDisplay = displayType.PRINTER;
            }
        });

        Button plotter = new Button(colorModeGroup, SWT.RADIO);
        plotter.setText("Plotter Colors");
        plotter.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currDisplay = displayType.PLOTTER;
                redrawAll();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                currDisplay = displayType.PLOTTER;
            }
        });

        // Creates first column of colors
        for (int aa = 0; aa < firstColumnNames.length; aa++) {
            java.awt.Color c;
            if (currDisplay == displayType.SCREEN) {
                c = colorTableColors.getColor(
                        screenColorSelectionIndeces.get(aa).getColorIndex()[0]);
            } else if (currDisplay == displayType.PRINTER) {
                c = colorTableColors.getColor(printerColorSelectionIndeces
                        .get(aa).getColorIndex()[0]);
            } else {
                c = colorTableColors.getColor(plotterColorSelectionIndeces
                        .get(aa).getColorIndex()[0]);
            }

            createColoredSquare(leftNamesComposite, colorRowsGridData, display,
                    c, 32, 16, aa);

            Label canvasLabel = new Label(leftNamesComposite, SWT.NONE);
            canvasLabel.setLayoutData(labelRowsGridData);
            canvasLabel.setFont(fixedWidthFont);
            canvasLabel.setText(firstColumnNames[aa]);
        }

        // Composite for second column, Outermost Closed Isobar -> Late Aircraft
        Composite middleNamesComposite = new Composite(topComposite, SWT.NONE);
        GridLayout middleNamesGridLayout = new GridLayout(2, false);
        middleNamesGridLayout.marginWidth = 15;
        middleNamesGridLayout.marginHeight = 15;
        middleNamesComposite.setLayout(middleNamesGridLayout);

        // Creates the second column of colors
        for (int bb = 0; bb < secondColumnNames.length; bb++) {
            java.awt.Color c;
            if (currDisplay == displayType.SCREEN) {
                c = colorTableColors.getColor(screenColorSelectionIndeces
                        .get(bb + 23).getColorIndex()[0]);
            } else if (currDisplay == displayType.PRINTER) {
                c = colorTableColors.getColor(printerColorSelectionIndeces
                        .get(bb + 23).getColorIndex()[0]);
            } else {
                c = colorTableColors.getColor(plotterColorSelectionIndeces
                        .get(bb + 23).getColorIndex()[0]);
            }

            createColoredSquare(middleNamesComposite, colorRowsGridData,
                    display, c, 32, 16, (bb + firstColumnNames.length));

            Label canvasLabel = new Label(middleNamesComposite, SWT.NONE);
            canvasLabel.setLayoutData(labelRowsGridData);
            canvasLabel.setFont(fixedWidthFont);
            canvasLabel.setText(secondColumnNames[bb]);
        }

        // Composite for third column, Fields -> Cat 1-5 Hurricanes, and buttons
        Composite namesColorTableComposite = new Composite(topComposite,
                SWT.NONE);
        GridLayout namesColorTableGridLayout = new GridLayout(2, false);
        namesColorTableGridLayout.marginRight = 100;
        namesColorTableGridLayout.marginHeight = 15;
        namesColorTableComposite.setLayout(namesColorTableGridLayout);

        Composite mapComposite = new Composite(namesColorTableComposite,
                SWT.NONE);
        GridLayout mapGridLayout = new GridLayout(1, false);
        mapGridLayout.marginLeft = 60;
        GridData mapCompGridData = new GridData();
        mapCompGridData.horizontalSpan = 2;
        mapComposite.setLayoutData(mapCompGridData);
        mapComposite.setLayout(mapGridLayout);

        Label map = new Label(mapComposite, SWT.BORDER);

        Image mapImage = AtcfVizUtil.createImage("mapDisplay.jpg");
        map.setImage(mapImage);

        Composite groupedNamesComposite = new Composite(
                namesColorTableComposite, SWT.NONE);
        GridLayout groupedNamesGridLayout = new GridLayout(5, false);
        groupedNamesGridLayout.marginWidth = 15;
        groupedNamesGridLayout.marginHeight = 15;
        groupedNamesComposite.setLayout(groupedNamesGridLayout);

        GridData labelGridData = new GridData(SWT.CENTER, SWT.FILL, false,
                true);
        labelGridData.widthHint = 170;
        labelGridData.horizontalSpan = 5;
        labelGridData.verticalIndent = 30;

        // Creates the third Column of color groups
        GridData controlButtonGridData = new GridData(SWT.CENTER, SWT.NONE,
                true, false);

        controlButtonGridData.horizontalSpan = 5;
        controlButtonGridData.verticalIndent = 20;

        for (int ii = 0; ii < 3; ii++) {
            Label label = new Label(groupedNamesComposite, SWT.NONE);
            label.setText(columnTitles[ii]);
            label.setLayoutData(labelGridData);

            // Third column of color groups
            for (int jj = 0; jj < 10; jj++) {
                if (ii < 2 || jj < 5) {

                    int dispType;
                    int index = (49 + (10 * ii) + jj);

                    if (currDisplay == displayType.SCREEN) {
                        dispType = screenColorSelectionIndeces.get(index)
                                .getColorIndex()[0];
                    } else if (currDisplay == displayType.PRINTER) {
                        dispType = printerColorSelectionIndeces.get(index)
                                .getColorIndex()[0];
                    } else {
                        dispType = plotterColorSelectionIndeces.get(index)
                                .getColorIndex()[0];
                    }

                    int key = dispType;
                    if (ii < 2) {
                        if (currDisplay == displayType.SCREEN) {
                            dispType = screenColorSelectionIndeces.get(index)
                                    .getColorIndex()[jj];
                        } else if (currDisplay == displayType.PRINTER) {
                            dispType = printerColorSelectionIndeces.get(index)
                                    .getColorIndex()[jj];
                        } else {
                            dispType = plotterColorSelectionIndeces.get(index)
                                    .getColorIndex()[jj];
                        }

                        key = dispType;
                    }
                    createColoredSquare(groupedNamesComposite,
                            colorRowsGridData, display,
                            colorTableColors.getColor(key), 32, 16, index);
                }
            }
        }
        objAidButton = new Button(groupedNamesComposite, SWT.CENTER);
        objAidButton.setText("Obj. Aids Colors...");
        objAidButton.setLayoutData(controlButtonGridData);
        objAidButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selectedChangeIndex != UNSELECTED) {
                    canvases.get(selectedChangeIndex).setForeground(
                            display.getSystemColor(SWT.COLOR_BLACK));
                    canvases.get(selectedChangeIndex).redraw();
                }
                selectedChangeIndex = UNSELECTED;
                if (selectedColorTableIndex != UNSELECTED) {
                    colorTableCanvases.get(selectedColorTableIndex)
                            .setForeground(getDisplay()
                                    .getSystemColor(SWT.COLOR_BLACK));
                    colorTableCanvases.get(selectedColorTableIndex).redraw();
                }
                selectedColorTableIndex = UNSELECTED;
                ObjAidColorDialog profileDlg = new ObjAidColorDialog(
                        getShell());
                profileDlg.open();
            }
        });

        colorTableButton = new Button(groupedNamesComposite, SWT.CENTER);
        colorTableButton.setText("Color Table...");
        colorTableButton.setLayoutData(controlButtonGridData);
        colorTableButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selectedChangeIndex != UNSELECTED) {
                    canvases.get(selectedChangeIndex).setForeground(
                            getDisplay().getSystemColor(SWT.COLOR_BLACK));
                    canvases.get(selectedChangeIndex).redraw();
                }
                selectedChangeIndex = UNSELECTED;
                if (selectedColorTableIndex != UNSELECTED) {
                    colorTableCanvases.get(selectedColorTableIndex)
                            .setForeground(getDisplay()
                                    .getSystemColor(SWT.COLOR_BLACK));
                    colorTableCanvases.get(selectedColorTableIndex).redraw();
                }
                selectedColorTableIndex = UNSELECTED;
                ChangeColorTableDialog profileDlg = new ChangeColorTableDialog(
                        getShell());
                profileDlg.open();

            }
        });

        // Composite for the Color Table
        Composite colorTableComposite = new Composite(namesColorTableComposite,
                SWT.NONE);
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

        // Creates the color Table
        for (int kk = 0; kk < COLOR_TABLE_CELL; kk++) {

            int index = (16 * (Math.floorMod(kk, 4))) + kk / 4;

            createColorTableSquare(colorTableComposite, colorRowsGridData,
                    display, 32, 16, index, kk);
        }

        shell.addListener(SWT.CLOSE, e -> fixedWidthFont.dispose());

    }

    /**
     * Creates the ok, help, and cancel buttons
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite controlButtonsComposite = new Composite(parent, SWT.NONE);
        GridLayout controlButtonCompositeGridLayout = new GridLayout(3, false);
        controlButtonCompositeGridLayout.marginWidth = 190;
        controlButtonsComposite.setLayout(controlButtonCompositeGridLayout);
        controlButtonsComposite.setLayoutData(AtcfVizUtil.horizontalFillGridData());


        AtcfVizUtil.createHelpButton(controlButtonsComposite,
                AtcfVizUtil.buttonGridData());

        okButton = new Button(controlButtonsComposite, SWT.CENTER);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfConfigurationManager.getInstance()
                        .saveColorSelections(colorSelections);
                close();
            }
        });

        cancelButton = new Button(controlButtonsComposite, SWT.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /**
     * Creates a colored square to be changed by the Color Table
     *
     * @param composite
     * @param griddata
     * @param display
     * @param background
     * @param width
     * @param height
     * @param index
     */
    public void createColoredSquare(Composite composite, GridData griddata,
            Display display, java.awt.Color background, int width, int height,
            int index) {
        Canvas squareCanvas = new Canvas(composite, SWT.NONE);
        squareCanvas.setLayoutData(griddata);
        squareCanvas
                .setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        squareCanvas.addPaintListener(e -> {
            ColorSelectionEntry entry;

            if (currDisplay == displayType.SCREEN) {
                entry = screenColorSelectionIndeces.get(index);
            } else if (currDisplay == displayType.PRINTER) {
                entry = printerColorSelectionIndeces.get(index);
            } else {
                entry = plotterColorSelectionIndeces.get(index);
            }

            java.awt.Color c = background;

            try {
                if (index < 49 || index > 68) {
                    c = java.awt.Color.decode("#" + colorTableColors
                            .getColorByIndex(entry.getColorIndex()[0])
                            .toUpperCase());
                } else if (index > 48 && index < 59) {
                    c = java.awt.Color
                            .decode("#" + colorTableColors.getColorByIndex(
                                    entry.getColorIndex()[index - 49]));
                } else if (index > 58 && index < 69) {
                    c = java.awt.Color
                            .decode("#" + colorTableColors.getColorByIndex(
                                    entry.getColorIndex()[index - 59]));
                }
            } catch (NumberFormatException ee) {
                // Skip
            }

            Color colour = new Color(display, c.getRed(), c.getGreen(),
                    c.getBlue());

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
                 * When you click on a colored square, change the currently
                 * selected index
                 */
                int prevIndex = selectedChangeIndex;

                selectedChangeIndex = index;

                // Change the currently selected color square to the color table

                canvases.get(index).setForeground(
                        getDisplay().getSystemColor(SWT.COLOR_WHITE));
                if (prevIndex != UNSELECTED && prevIndex != index) {
                    canvases.get(prevIndex).setForeground(
                            getDisplay().getSystemColor(SWT.COLOR_BLACK));
                }

            }

            @Override
            public void mouseUp(MouseEvent e) {
                // Not used.
            }

        });

        canvases.add(squareCanvas);
    }

    /*
     * Creates a colored square for the Color Table
     *
     * @param composite
     *
     * @param griddata
     *
     * @param display
     *
     * @param width
     *
     * @param height
     *
     * @param index
     *
     * @param selected
     *
     * @return canvas
     */
    private void createColorTableSquare(Composite composite, GridData griddata,
            Display display, int width, int height, int index, int selected) {

        Canvas colorTableCanvas = new Canvas(composite, SWT.NONE);
        colorTableCanvas.setLayoutData(griddata);
        colorTableCanvas.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Not used.
            }

            @Override
            public void mouseDown(MouseEvent e) {

                // Change the currently selected color square to the color table
                selectedColorTableIndex = index;
                for (Canvas canvas : colorTableCanvases) {
                    canvas.setForeground(
                            getDisplay().getSystemColor(SWT.COLOR_BLACK));
                }
                colorTableCanvases.get(selected).setForeground(
                        getDisplay().getSystemColor(SWT.COLOR_WHITE));
                // separated out to avoid duplications
                selectColor();

                // Redraw the correct Canvas
                if (selectedChangeIndex != UNSELECTED) {
                    canvases.get(selectedChangeIndex).redraw();
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                // Not Used.
            }

        });

        colorTableCanvas.addPaintListener(e -> {
            java.awt.Color c = java.awt.Color
                    .decode("#" + colorTableColors.getColorByIndex(index));
            Color colour = new Color(display, c.getRed(), c.getGreen(),
                    c.getBlue());

            e.gc.setBackground(colour);
            e.gc.fillRectangle(0, 0, width, height);
            e.gc.drawRectangle(0, 0, width, height);

            colour.dispose();
        });
        colorTableCanvases.add(colorTableCanvas);
    }

    /**
     * The logic that goes into matching to save a color
     */
    private void selectColor() {
        // color selected
        List<ColorSelectionEntry> tempColorSelections = colorSelections
                .getMapDisplayColors();

        if ((selectedChangeIndex > -1 && selectedChangeIndex < 49)
                || (selectedChangeIndex > 68 && selectedChangeIndex < 74)) {
            int[] tempColorList = { selectedColorTableIndex };
            if (currDisplay == displayType.SCREEN) {
                screenColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            } else if (currDisplay == displayType.PRINTER) {
                printerColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            } else {
                plotterColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            }

            /*
             * indexes 49 - 68 are handled differently in the localization text
             * file, so they need to be done separately.
             */
        } else if (selectedChangeIndex > 48 && selectedChangeIndex < 59) {
            /*
             * Instead of mapping an index to a box, these 20 boxes are stored
             * in two groups of 10
             */
            List<Integer> tempColor = new ArrayList<>();
            for (int ii = 0; ii < 10; ii++) {
                if (ii + 49 != selectedChangeIndex) {
                    if (currDisplay == displayType.SCREEN) {
                        tempColor.add(screenColorSelectionIndeces
                                .get(selectedChangeIndex).getColorIndex()[ii]);
                    } else if (currDisplay == displayType.PRINTER) {
                        tempColor.add(printerColorSelectionIndeces
                                .get(selectedChangeIndex).getColorIndex()[ii]);
                    } else {
                        tempColor.add(plotterColorSelectionIndeces
                                .get(selectedChangeIndex).getColorIndex()[ii]);
                    }

                } else {
                    tempColor.add(selectedColorTableIndex);
                }
            }
            int[] tempColorList = new int[10];
            for (int ii = 0; ii < 10; ii++) {
                tempColorList[ii] = tempColor.get(ii);
            }
            if (currDisplay == displayType.SCREEN) {
                screenColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            } else if (currDisplay == displayType.PRINTER) {
                printerColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            } else {
                plotterColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            }
        } else if (selectedChangeIndex > 58 && selectedChangeIndex < 69) {
            List<Integer> tempColor = new ArrayList<>();
            for (int ii = 0; ii < 10; ii++) {
                if (ii + 59 != selectedChangeIndex) {
                    if (currDisplay == displayType.SCREEN) {
                        tempColor.add(screenColorSelectionIndeces
                                .get(selectedChangeIndex).getColorIndex()[ii]);
                    } else if (currDisplay == displayType.PRINTER) {
                        tempColor.add(printerColorSelectionIndeces
                                .get(selectedChangeIndex).getColorIndex()[ii]);
                    } else {
                        tempColor.add(plotterColorSelectionIndeces
                                .get(selectedChangeIndex).getColorIndex()[ii]);
                    }

                } else {
                    tempColor.add(selectedColorTableIndex);
                }
            }
            int[] tempColorList = new int[10];
            for (int ii = 0; ii < 10; ii++) {
                tempColorList[ii] = tempColor.get(ii);
            }

            if (currDisplay == displayType.SCREEN) {
                screenColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            } else if (currDisplay == displayType.PRINTER) {
                printerColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            } else {
                plotterColorSelectionIndeces.get(selectedChangeIndex)
                        .setColorIndex(tempColorList);
            }
        }
        colorSelections.setMapDisplayColors(tempColorSelections);
    }

}