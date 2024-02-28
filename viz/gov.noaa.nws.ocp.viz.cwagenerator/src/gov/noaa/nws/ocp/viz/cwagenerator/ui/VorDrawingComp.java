/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ncep.viz.common.ui.color.ColorButtonSelector;
import gov.noaa.nws.ocp.viz.cwagenerator.action.CWAGeneratorResource;
import gov.noaa.nws.ocp.viz.cwagenerator.action.IUpdateFormatter;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;

/**
 * class for VOR drawing configuration composites
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 08/30/2021  28802    wkwock      Initial creation
 * 09/01/2023  2036158  wkwock      Change width to width/diam and allow change of width all time 
 * 
 * </pre>
 * 
 * @author wkwock
 */
public class VorDrawingComp extends Composite {
    public static final String LINE_SEPERATER = ":::";

    private DrawingType drawingType = DrawingType.AREA;

    private static final String WIDTH = "10.00";

    private float widthVal = 10;

    private float radiusVal = 0;

    protected ColorButtonSelector cs = null;

    private IUpdateFormatter ownerFormatter;

    private Button areaBttn;

    private Button lineBttn;

    private Button isolatedBttn;

    private Text widthTxt;

    public VorDrawingComp(Composite parent, CWAGeneratorResource cwaResource,
            IUpdateFormatter formatter) {
        super(parent, SWT.NONE);
        this.ownerFormatter = formatter;

        this.setLayout(new GridLayout(1, false));

        Composite topComp = new Composite(this, SWT.NONE);
        GridLayout mainLayout = new GridLayout(8, false);
        topComp.setLayout(mainLayout);
        GridData topGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        topComp.setLayoutData(topGd);

        areaBttn = new Button(topComp, SWT.RADIO);
        areaBttn.setText("Area");
        areaBttn.setSelection(true);

        lineBttn = new Button(topComp, SWT.RADIO);
        lineBttn.setText("Line");

        isolatedBttn = new Button(topComp, SWT.RADIO);
        isolatedBttn.setText("Isolated  ");

        Label widthLbl = new Label(topComp, SWT.LEFT);
        widthLbl.setText("Width/Diam: ");
        widthTxt = new Text(topComp, SWT.SINGLE | SWT.BORDER);
        widthTxt.setText(WIDTH);
        widthTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.keyCode == SWT.CR || arg0.keyCode == SWT.KEYPAD_CR) {
                    adjustWidth();
                }
            }
        });
        widthTxt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                adjustWidth();
            }
        });

        areaBttn.addListener(SWT.Selection, (Event e) -> {
            if (DrawingType.AREA != drawingType) {
                ownerFormatter.clearDrawings();
                drawingType = DrawingType.AREA;
                cwaResource.setDrawType(drawingType);
            }
        });

        lineBttn.addListener(SWT.Selection, (Event e) -> {
            if (DrawingType.LINE != drawingType) {
                ownerFormatter.clearDrawings();
                drawingType = DrawingType.LINE;
                cwaResource.setDrawType(drawingType);
            }
        });

        isolatedBttn.addListener(SWT.Selection, (Event e) -> {
            if (DrawingType.ISOLATED != drawingType) {
                ownerFormatter.clearDrawings();
                drawingType = DrawingType.ISOLATED;
                cwaResource.setDrawType(drawingType);
            }
        });

        GridData gd3 = new GridData();
        gd3.horizontalIndent = 10;
        Label rprLbl = new Label(topComp, SWT.NONE);
        rprLbl.setText("Ref Point Radius:");
        rprLbl.setLayoutData(gd3);

        Text rprTxt = new Text(topComp, SWT.BORDER);
        GridData gd4 = new GridData();
        gd4.widthHint = 35;
        rprTxt.setLayoutData(gd4);
        rprTxt.setText("0");
        Label nmLbl = new Label(topComp, SWT.NONE);
        nmLbl.setText("nm");
        rprTxt.addTraverseListener((TraverseEvent te) -> {
            if (te.detail == SWT.TRAVERSE_RETURN) {
                try {
                    float tmpVal = Float.parseFloat(rprTxt.getText().trim());
                    if (tmpVal < 0) {
                        rprTxt.setText(Float.toString(0));
                    } else if (tmpVal > 400) {
                        rprTxt.setText(Float.toString(400));
                    } else if (tmpVal != radiusVal) {
                        radiusVal = tmpVal;
                        cwaResource.updateRefPointRadius(radiusVal);
                    }
                } catch (NumberFormatException nfe) {
                    rprTxt.setText(Float.toString(radiusVal));
                }
            }
        });

        Composite topButtonBar = new Composite(this, SWT.NONE);
        GridLayout topButtonLayout = new GridLayout(3, false);
        topButtonBar.setLayout(topButtonLayout);
        GridData topButtonGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        topButtonBar.setLayoutData(topButtonGd);

        Button clearBttn = new Button(topButtonBar, SWT.PUSH);
        clearBttn.setText("Clear");
        clearBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ownerFormatter.clearDrawings();
                ownerFormatter.changeMouseMode(true);// draw VORs mode
                areaBttn.setEnabled(true);
                lineBttn.setEnabled(true);
                isolatedBttn.setEnabled(true);
                if (lineBttn.getSelection()) {
                    widthTxt.setEnabled(true);
                }
            }
        });

        Button saveBttn = new Button(topButtonBar, SWT.PUSH);
        saveBttn.setText("Save");
        saveBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ownerFormatter.saveVORs();
            }
        });
        Button openBttn = new Button(topButtonBar, SWT.PUSH);
        openBttn.setText("Open");
        openBttn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ownerFormatter.clearDrawings();
                ownerFormatter.openVORs();
                areaBttn.setEnabled(false);
                lineBttn.setEnabled(false);
                isolatedBttn.setEnabled(false);
                widthTxt.setEnabled(false);
            }
        });
    }

    private void adjustWidth() {
        try {
            float tmpVal = Float.parseFloat(widthTxt.getText().trim());
            if (tmpVal < 1) {
                widthTxt.setText(Float.toString(widthVal));
            } else if (tmpVal != widthVal) {
                widthVal = tmpVal;
            }
        } catch (NumberFormatException nfe) {
            widthTxt.setText(Float.toString(widthVal));
        }
    }

    public DrawingType getDrawingType() {
        return this.drawingType;
    }

    public void setDrawingType(DrawingType type) {
        this.drawingType = type;
        areaBttn.setSelection(false);
        lineBttn.setSelection(false);
        isolatedBttn.setSelection(false);
        switch (drawingType) {
        case AREA:
            areaBttn.setSelection(true);
            break;
        case LINE:
            lineBttn.setSelection(true);
            break;
        default:
            isolatedBttn.setSelection(true);
        }
    }

    public double getWidth() {
        return widthVal;
    }

    public void setWidth(float tmpWidth) {
        if (tmpWidth < 1) {
            widthTxt.setText("1.0");
            widthVal = 1.0f;
        } else if (tmpWidth != widthVal) {
            widthVal = tmpWidth;
            widthTxt.setText(Float.toString(tmpWidth));
        }
    }

}
