/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

/**
 * Dialog for View Fix Data.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04 2018  #45688      dmanzella   initial creation
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ViewFixDataDialog extends CaveJFACEDialog {
    /**
     * Label for OK button.
     */
    private static final String OK_LABEL = "OK";

    /**
     * Constructor
     * 
     * @param parent
     */
    public ViewFixDataDialog(Shell parent) {
        super(parent);

        setShellStyle(SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER
                | SWT.TITLE | SWT.RESIZE);

        // Set the default location relative to the main GUI.
        setDefaultLocation(parent);
    }

    /**
     * Create contents.
     * 
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {

        Composite comp = (Composite) super.createContents(parent);

        // Set the location of the dialog.
        getShell().setLocation(lastLocation);

        return comp;
    }

    /**
     * Create dialog area.
     * 
     * @param parent
     */
    @Override
    public Control createDialogArea(Composite parent) {
        /*
         * Sets dialog title
         */
        getShell().setText("View Fix Data");

        Composite top = (Composite) super.createDialogArea(parent);
        Display display = parent.getDisplay();

        /*
         * Create the main layout for the shell.
         */
        GridLayout mainLayout = new GridLayout(1, false);

        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        top.setLayoutData(mainLayoutData);

        createUpperLabels(top);

        createMainDisplay(top, display);

        return top;
    }

    /**
     * Creates topmost labels
     * 
     * @param top
     */
    private void createUpperLabels(Composite top) {

        Composite labelComposite = new Composite(top, SWT.NONE);
        labelComposite.setLayout(new GridLayout(5, true));
        labelComposite
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));

        Label dateTimeLabel = new Label(labelComposite, SWT.NONE);
        dateTimeLabel.setText("Date/Time");

        Label typeLabel = new Label(labelComposite, SWT.NONE);
        typeLabel.setText("Type");

        Label ctrlLabel = new Label(labelComposite, SWT.NONE);
        ctrlLabel.setText("Ctrl/Int");

        Label latLabel = new Label(labelComposite, SWT.NONE);
        latLabel.setText("Lat");

        Label lonLabel = new Label(labelComposite, SWT.NONE);
        lonLabel.setText("Lon");

    }

    /**
     * Creates the colored squares for the leftmost list
     * 
     * @param e
     * @param backGround
     * @param foreGround
     */
    public void createColoredSquare(PaintEvent e, Color backGround,
            Color foreGround) {
        e.gc.setBackground(backGround);
        e.gc.setForeground(foreGround);
        e.gc.fillRectangle(0, 0, 17, 16);
        e.gc.drawRectangle(0, 0, 17, 16);
    }

    /**
     * Creates the rest of the display
     * 
     * @param top
     * @param display
     */
    private void createMainDisplay(Composite top, Display display) {

        Device device = display.getCurrent();

        // Main composite
        Composite avComp = new Composite(top, SWT.NONE);
        avComp.setLayout(new GridLayout(2, false));
        avComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));

        // Composite for the labels with colored boxes to the left
        Composite avCompLeft = new Composite(avComp, SWT.NONE);
        avCompLeft.setLayout(new GridLayout(2, false));
        avCompLeft
                .setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));

        // GridData for the colored squares
        GridData colorRowsGridData = new GridData(SWT.BEGINNING, SWT.FILL,
                false, false);
        colorRowsGridData.horizontalIndent = 2;
        colorRowsGridData.widthHint = 20;
        colorRowsGridData.heightHint = 17;

        Canvas canvasRed = new Canvas(avCompLeft, SWT.NONE);
        canvasRed.setLayoutData(colorRowsGridData);
        canvasRed.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_RED),
                display.getSystemColor(SWT.COLOR_BLACK)));

        // GridData for the labels next to the colored squares
        GridData textHeightGridData = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        textHeightGridData.heightHint = 17;
        textHeightGridData.widthHint = 175;

        // All of the label/square pairs
        Label satLabel = new Label(avCompLeft, SWT.NONE);
        satLabel.setText("Satellite");
        satLabel.setLayoutData(textHeightGridData);

        Canvas canvasYellow = new Canvas(avCompLeft, SWT.NONE);
        canvasYellow.setLayoutData(colorRowsGridData);
        canvasYellow.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_YELLOW),
                display.getSystemColor(SWT.COLOR_BLACK)));

        Label dvorLabel = new Label(avCompLeft, SWT.NONE);
        dvorLabel.setText("Obj. Dvorak Fix");
        dvorLabel.setLayoutData(textHeightGridData);

        Canvas canvasBlue = new Canvas(avCompLeft, SWT.NONE);
        canvasBlue.setLayoutData(colorRowsGridData);
        canvasBlue.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_BLUE),
                display.getSystemColor(SWT.COLOR_BLACK)));
        Label micLabel = new Label(avCompLeft, SWT.NONE);
        micLabel.setText("Microwave");
        micLabel.setLayoutData(textHeightGridData);

        Canvas canvasMagenta = new Canvas(avCompLeft, SWT.NONE);
        canvasMagenta.setLayoutData(colorRowsGridData);
        canvasMagenta.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_MAGENTA),
                display.getSystemColor(SWT.COLOR_BLACK)));

        Label airLabel = new Label(avCompLeft, SWT.NONE);
        airLabel.setText("Aircraft");
        airLabel.setLayoutData(textHeightGridData);

        Canvas canvasGray = new Canvas(avCompLeft, SWT.NONE);
        canvasGray.setLayoutData(colorRowsGridData);
        canvasGray.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_DARK_GRAY),
                display.getSystemColor(SWT.COLOR_BLACK)));

        Label radLabel = new Label(avCompLeft, SWT.NONE);
        radLabel.setText("Radar");
        radLabel.setLayoutData(textHeightGridData);

        Canvas canvasGreen = new Canvas(avCompLeft, SWT.NONE);
        canvasGreen.setLayoutData(colorRowsGridData);
        canvasGreen.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_GREEN),
                display.getSystemColor(SWT.COLOR_BLACK)));

        Label anaLabel = new Label(avCompLeft, SWT.NONE);
        anaLabel.setText("Analysis");
        anaLabel.setLayoutData(textHeightGridData);

        Canvas canvasPink = new Canvas(avCompLeft, SWT.NONE);
        canvasPink.setLayoutData(colorRowsGridData);
        canvasPink.addPaintListener(e -> {
            Color pink = new Color(device, 255, 192, 203);
            createColoredSquare(e, pink,
                    display.getSystemColor(SWT.COLOR_BLACK));
        });

        Label scatLabel = new Label(avCompLeft, SWT.NONE);
        scatLabel.setText("Scatterometer");
        scatLabel.setLayoutData(textHeightGridData);

        Canvas canvasOrange = new Canvas(avCompLeft, SWT.NONE);
        canvasOrange.setLayoutData(colorRowsGridData);
        canvasOrange.addPaintListener(e -> {
            Color orange = new Color(device, 255, 128, 0);
            createColoredSquare(e, orange,
                    display.getSystemColor(SWT.COLOR_BLACK));
        });

        Label dropLabel = new Label(avCompLeft, SWT.NONE);
        dropLabel.setText("Dropsonde");
        dropLabel.setLayoutData(textHeightGridData);

        Canvas canvasYellow2 = new Canvas(avCompLeft, SWT.NONE);
        canvasYellow2.setLayoutData(colorRowsGridData);
        canvasYellow2.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_YELLOW),
                display.getSystemColor(SWT.COLOR_BLACK)));

        Label resLabel = new Label(avCompLeft, SWT.NONE);
        resLabel.setText("Research");
        resLabel.setLayoutData(textHeightGridData);

        Canvas canvasBeige = new Canvas(avCompLeft, SWT.NONE);
        canvasBeige.setLayoutData(colorRowsGridData);
        canvasBeige.addPaintListener(e -> {
            Color beige = new Color(device, 207, 207, 111);
            createColoredSquare(e, beige,
                    display.getSystemColor(SWT.COLOR_BLACK));
        });

        Label flagLabel = new Label(avCompLeft, SWT.NONE);
        flagLabel.setText("Flagged or Non Center Fix");
        flagLabel.setLayoutData(textHeightGridData);

        Label fillera = new Label(avCompLeft, SWT.NONE);
        fillera.setText(" ");
        fillera.setLayoutData(colorRowsGridData);
        Label fillerb = new Label(avCompLeft, SWT.NONE);
        fillerb.setText(" ");
        fillerb.setLayoutData(textHeightGridData);
        Label fillerc = new Label(avCompLeft, SWT.NONE);
        fillerc.setText(" ");
        fillerc.setLayoutData(colorRowsGridData);
        Label fillerd = new Label(avCompLeft, SWT.NONE);
        fillerd.setText(" ");
        fillerd.setLayoutData(textHeightGridData);

        Canvas canvasRed2 = new Canvas(avCompLeft, SWT.NONE);
        canvasRed2.setLayoutData(colorRowsGridData);
        canvasRed2.addPaintListener(e -> createColoredSquare(e,
                display.getSystemColor(SWT.COLOR_RED),
                display.getSystemColor(SWT.COLOR_BLACK)));

        Label goodLabel = new Label(avCompLeft, SWT.NONE);
        goodLabel.setText("Good");
        goodLabel.setLayoutData(textHeightGridData);

        Canvas canvasRed3 = new Canvas(avCompLeft, SWT.NONE);
        canvasRed3.setLayoutData(colorRowsGridData);
        canvasRed3.addPaintListener(e -> {
            int width = canvasRed3.getBounds().width;
            int height = canvasRed3.getBounds().height;
            e.gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
            e.gc.fillPolygon(
                    new int[] { 0, height, width, height, width / 2, 0 });
            e.gc.drawLine(0, height - 1, width, height - 1);
            e.gc.drawLine(width, height, width / 2, 0);
            e.gc.drawLine(0, height, width / 2, 0);
        });

        Label fairLabel = new Label(avCompLeft, SWT.NONE);
        fairLabel.setText("Fair");
        fairLabel.setLayoutData(textHeightGridData);

        Canvas canvasRed4 = new Canvas(avCompLeft, SWT.NONE);
        canvasRed4.setLayoutData(colorRowsGridData);
        canvasRed4.addPaintListener(e -> {
            int width = canvasRed4.getBounds().width;
            int height = canvasRed4.getBounds().height;
            e.gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
            e.gc.fillPolygon(new int[] { 0, height / 2, width / 2, height,
                    width, height / 2, width / 2, 0 });

            e.gc.drawLine(0, height / 2, width / 2, height);
            e.gc.drawLine(width / 2, height, width, height / 2);
            e.gc.drawLine(width, height / 2, width / 2, 0);
            e.gc.drawLine(width / 2, 0, 0, height / 2);

        });

        Label poorLabel = new Label(avCompLeft, SWT.NONE);
        poorLabel.setText("Poor");
        poorLabel.setLayoutData(textHeightGridData);

        // Right half of the display

        // creates the large textbox
        Composite avCompRight = new Composite(avComp, SWT.NONE);
        avCompRight.setLayout(new GridLayout(4, false));
        avCompRight.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.NONE, false, false));

        Label filler21 = new Label(avCompRight, SWT.NONE);
        filler21.setText(" ");

        GridData textWidthGridData = new GridData(SWT.FILL, SWT.NONE, true,
                false);
        textWidthGridData.heightHint = 140;
        textWidthGridData.horizontalIndent = 5;
        textWidthGridData.horizontalSpan = 3;
        textWidthGridData.verticalSpan = 4;
        Text textBox = new Text(avCompRight, SWT.V_SCROLL);
        textBox.setLayoutData(textWidthGridData);

        // creates the group of 4 check options
        Group checklist = new Group(avCompRight, SWT.NONE);
        checklist.setLayout(new GridLayout(1, false));

        GridData buttonsHeight = new GridData(SWT.FILL, SWT.NONE, false, false);
        buttonsHeight.verticalIndent = 8;

        Button confButton = new Button(checklist, SWT.CHECK);
        confButton.setText("Conf Circles");
        confButton.setLayoutData(buttonsHeight);

        Button windButton = new Button(checklist, SWT.CHECK);
        windButton.setText("Wind Radii");
        windButton.setLayoutData(buttonsHeight);

        Button autoButton = new Button(checklist, SWT.CHECK);
        autoButton.setText("Autolabel");
        autoButton.setLayoutData(buttonsHeight);

        Button fixesButton = new Button(checklist, SWT.CHECK);
        fixesButton.setText("Highlight Fixes");
        fixesButton.setLayoutData(buttonsHeight);

        Label filler1 = new Label(avCompRight, SWT.NONE);
        filler1.setText(" ");

        // creates the +/- drop down lists
        GridData ddlsSpanGridData = new GridData(SWT.FILL, SWT.NONE, true,
                false);
        ddlsSpanGridData.horizontalSpan = 2;
        Group ddlsGroup = new Group(avCompRight, SWT.NONE);
        ddlsGroup.setLayout(new GridLayout(3, false));
        ddlsGroup.setLayoutData(ddlsSpanGridData);

        Combo leftBox = new Combo(ddlsGroup, SWT.RIGHT);
        leftBox.setText("2009090106");

        Label plusMinusLabel = new Label(ddlsGroup, SWT.RIGHT);
        plusMinusLabel.setText("+/-       ");

        GridData smallBoxGridData = new GridData(SWT.CENTER, SWT.NONE, false,
                false);
        smallBoxGridData.widthHint = 90;
        Combo rightBox = new Combo(ddlsGroup, SWT.RIGHT);
        rightBox.setText("3");
        rightBox.setLayoutData(smallBoxGridData);

        Label filler3 = new Label(avCompRight, SWT.NONE);
        filler3.setText(" ");

        // creates the three display buttons
        GridData buttonsFillGridData = new GridData(SWT.CENTER, SWT.FILL, true,
                false);
        buttonsFillGridData.widthHint = 165;

        GridData groupIndent = new GridData(SWT.CENTER, SWT.FILL, true, false);
        groupIndent.horizontalIndent = 60;

        Group rightButtonsGroup = new Group(avCompRight, SWT.NONE);
        rightButtonsGroup.setLayout(new GridLayout(1, false));
        rightButtonsGroup.setLayoutData(groupIndent);

        Button displayButton = new Button(rightButtonsGroup, SWT.NONE);
        displayButton.setText("Display");
        displayButton.setLayoutData(buttonsFillGridData);

        Button removeButton = new Button(rightButtonsGroup, SWT.NONE);
        removeButton.setText("Remove");
        removeButton.setLayoutData(buttonsFillGridData);

        Button modifyButton = new Button(rightButtonsGroup, SWT.NONE);
        modifyButton.setText("Modify Image...");
        modifyButton.setLayoutData(buttonsFillGridData);
    }

    /**
     * Creates the OK button;
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, OK_LABEL, true);
    }

    /**
     * Close & remember the last location.
     */
    @Override
    public boolean close() {
        Shell shell = getShell();
        if (shell != null && !shell.isDisposed()) {
            Rectangle bounds = shell.getBounds();
            lastLocation = new Point(bounds.x, bounds.y);
        }

        return super.close();
    }

    /**
     * Set the default location.
     * 
     * @param parent
     */
    private void setDefaultLocation(Shell parent) {
        if (lastLocation == null) {
            lastLocation = parent.getLocation();
            lastLocation.y += 160;
            lastLocation.x += 400;
        }
    }

}
