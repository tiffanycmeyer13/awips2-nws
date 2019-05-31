/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.configparams.support;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;

/**
 * 
 * Dialog for "Edit User-Defined Values"
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/19/2016   20639    wkwock      Initial creation
 * 10/14/2015   20639    wkwock      Correct title
 * 10/18/2016   20639    wkwock      Remove background color, add listeners
 * 20 DEC 2016  20955    amoore      Clean up and usability.
 * 20 DEC 2016  26404    amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 27 DEC 2016  22450    amoore      Config should use common listeners where possible.
 * 09 FEB 2017  20640    jwu         Write out preference & set return value to indicate changes.
 * 22 FEB 2017  28609    amoore      Address TODOs.
 * 12 MAY 2017  33104    amoore      Address FindBugs.
 * 21 MAY 2019  DR21196  dfriedman   Use correct CaveSWTDialog life cycle functions.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class ThresholdsDialog extends ClimateCaveChangeTrackDialog {

    /**
     * width for a 'degree Fahrenheit' text widget
     */
    private final static int DEGREE_WIDTH = 40;

    /**
     * width for an 'inch' text widget
     */
    private final static int INCH_WIDTH = 60;

    /**
     * margin width
     */
    private final static int MARGIN = 10;

    /**
     * bold font
     */
    private Font boldFont;

    /**
     * italic font
     */
    private Font italicFont;

    /**
     * 1st maximum temperature with equal or above text
     */
    private Text maxTmpEqOrAbv1Txt;

    /**
     * 2nd maximum temperature with equal or above text
     */
    private Text maxTmpEqOrAbv2Txt;

    /**
     * maximum temperature with equal or below text
     */
    private Text maxTmpEqOrBlwTxt;

    /**
     * minimum temperature with equal or above text
     */
    private Text minTmpEqOrAbvTxt;

    /**
     * 1st minimum temperature with equal or below text
     */
    private Text minTmpEqOrBlw1Txt;

    /**
     * 2nd minimum temperature with equal or below text
     */
    private Text minTmpEqOrBlw2Txt;

    /**
     * 1st precipitation with equal or above text
     */
    private Text precipEqOrAbv1Txt;

    /**
     * 2nd precipitation with equal or above text
     */
    private Text precipEqOrAbv2Txt;

    /**
     * snow fall with equal or above text
     */
    private Text snowEqOrAbvTxt;

    /**
     * preferences
     */
    private ClimateGlobal preferenceValues;

    /**
     * Collection of listeners.
     */
    private final ClimateTextListeners myDisplayListeners = new ClimateTextListeners();

    /**
     * Constructor.
     * 
     * @param parent
     * @param preferenceVals
     */
    public ThresholdsDialog(Shell parent, ClimateGlobal preferenceVals) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        setText("Edit User-Defined Values");

        this.preferenceValues = preferenceVals;

        // Initialize "returnValue" to "false" to indicate no changes so far.
        this.setReturnValue(false);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout shellGl = new GridLayout(1, true);
        shellGl.verticalSpacing = 20;
        shellGl.marginWidth = MARGIN;
        shellGl.marginHeight = MARGIN;
        shell.setLayout(shellGl);
        GridData shellGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        shell.setLayoutData(shellGd);

        setupFonts();

        createMenuBar();

        Label dayLbl = new Label(shell, SWT.CENTER);
        GridData dayGd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        dayGd.horizontalIndent = 25;
        dayLbl.setLayoutData(dayGd);
        dayLbl.setText("Days With...");

        createMaxTempGrp();
        createMinTempGrp();
        createPrecipGrp();
        createSnowfallGrp();

        createBottomButtons();

        displayPreferences();
    }

    /**
     * set up fonts
     */
    private void setupFonts() {
        FontData fontData = shell.getFont().getFontData()[0];

        boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.BOLD));

        italicFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.ITALIC));

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                disposeFonts();
            }
        });
    }

    /**
     * dispose fonts
     */
    protected void disposeFonts() {
        if (boldFont != null) {
            boldFont.dispose();
        }

        if (italicFont != null) {
            italicFont.dispose();
        }
    }

    /**
     * create menu bar
     */
    private void createMenuBar() {
        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuItem.setText("&File");

        Menu fileMenu = new Menu(menuBar);
        fileMenuItem.setMenu(fileMenu);

        MenuItem closeMI = new MenuItem(fileMenu, SWT.NONE);
        closeMI.setText("&Cancel");
        closeMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuItem.setText("&Help");

        Menu helpMenu = new Menu(menuBar);
        helpMenuItem.setMenu(helpMenu);

        MenuItem versionMI = new MenuItem(helpMenu, SWT.NONE);
        versionMI.setText("&Version...");
        versionMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Task 29501: simply don't do any thing. Ask IWT to remove ?
            }
        });

        shell.setMenuBar(menuBar);
    }

    /**
     * create the maximum temperature group
     */
    private void createMaxTempGrp() {
        Group maxTempGrp = new Group(shell, SWT.SHADOW_IN);
        GridLayout grpGl = new GridLayout(3, false);
        grpGl.marginHeight = MARGIN;
        grpGl.marginWidth = MARGIN;
        maxTempGrp.setLayout(grpGl);
        maxTempGrp.setText("Maximum Temperature");
        maxTempGrp.setFont(boldFont);

        Label equalOrAbove1Lbl = new Label(maxTempGrp, SWT.NONE);
        equalOrAbove1Lbl.setText("Equal or Above:");

        maxTmpEqOrAbv1Txt = new Text(maxTempGrp, SWT.BORDER);
        GridData equalOrAbove1Gd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrAbove1Gd.widthHint = DEGREE_WIDTH;
        maxTmpEqOrAbv1Txt.setLayoutData(equalOrAbove1Gd);
        maxTmpEqOrAbv1Txt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        maxTmpEqOrAbv1Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        maxTmpEqOrAbv1Txt.addListener(SWT.Modify, changeListener);

        Label deg1Lbl = new Label(maxTempGrp, SWT.NONE);
        deg1Lbl.setFont(italicFont);
        deg1Lbl.setText("deg F");

        // 2nd line
        Label equalOrAbove2Lbl = new Label(maxTempGrp, SWT.NONE);
        equalOrAbove2Lbl.setText("Equal or Above:");

        maxTmpEqOrAbv2Txt = new Text(maxTempGrp, SWT.BORDER);
        GridData equalOrAbove2Gd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrAbove2Gd.widthHint = DEGREE_WIDTH;
        maxTmpEqOrAbv2Txt.setLayoutData(equalOrAbove2Gd);
        maxTmpEqOrAbv2Txt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        maxTmpEqOrAbv2Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        maxTmpEqOrAbv2Txt.addListener(SWT.Modify, changeListener);

        Label deg2Lbl = new Label(maxTempGrp, SWT.NONE);
        deg2Lbl.setFont(italicFont);
        deg2Lbl.setText("deg F");

        // 3rd line
        Label equalOrAbove3Lbl = new Label(maxTempGrp, SWT.NONE);
        equalOrAbove3Lbl.setText("Equal or Below:");

        maxTmpEqOrBlwTxt = new Text(maxTempGrp, SWT.BORDER);
        GridData equalOrBelowGd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrBelowGd.widthHint = DEGREE_WIDTH;
        maxTmpEqOrBlwTxt.setLayoutData(equalOrBelowGd);
        maxTmpEqOrBlwTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        maxTmpEqOrBlwTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        maxTmpEqOrBlwTxt.addListener(SWT.Modify, changeListener);

        Label deg3Lbl = new Label(maxTempGrp, SWT.NONE);
        deg3Lbl.setFont(italicFont);
        deg3Lbl.setText("deg F");
    }

    /**
     * create the minimum temperature group
     */
    private void createMinTempGrp() {
        Group minTempGrp = new Group(shell, SWT.SHADOW_IN);
        GridLayout grpGl = new GridLayout(3, false);
        grpGl.marginHeight = MARGIN;
        grpGl.marginWidth = MARGIN;
        minTempGrp.setLayout(grpGl);
        minTempGrp.setText("Minimum Temperature");
        minTempGrp.setFont(boldFont);

        Label equalOrAboveLbl = new Label(minTempGrp, SWT.NONE);
        equalOrAboveLbl.setText("Equal or Above:");

        minTmpEqOrAbvTxt = new Text(minTempGrp, SWT.BORDER);
        GridData equalOrAboveGd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrAboveGd.widthHint = DEGREE_WIDTH;
        minTmpEqOrAbvTxt.setLayoutData(equalOrAboveGd);
        minTmpEqOrAbvTxt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        minTmpEqOrAbvTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        minTmpEqOrAbvTxt.addListener(SWT.Modify, changeListener);

        Label deg1Lbl = new Label(minTempGrp, SWT.NONE);
        deg1Lbl.setFont(italicFont);
        deg1Lbl.setText("deg F");

        // 2nd line
        Label equalOrBelow1Lbl = new Label(minTempGrp, SWT.NONE);
        equalOrBelow1Lbl.setText("Equal or Below:");

        minTmpEqOrBlw1Txt = new Text(minTempGrp, SWT.BORDER);
        GridData equalOrBelow1Gd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrBelow1Gd.widthHint = DEGREE_WIDTH;
        minTmpEqOrBlw1Txt.setLayoutData(equalOrBelow1Gd);
        minTmpEqOrBlw1Txt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        minTmpEqOrBlw1Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        minTmpEqOrBlw1Txt.addListener(SWT.Modify, changeListener);

        Label deg2Lbl = new Label(minTempGrp, SWT.NONE);
        deg2Lbl.setFont(italicFont);
        deg2Lbl.setText("deg F");

        // 3rd line
        Label equalOrBelow2Lbl = new Label(minTempGrp, SWT.NONE);
        equalOrBelow2Lbl.setText("Equal or Below:");

        minTmpEqOrBlw2Txt = new Text(minTempGrp, SWT.BORDER);
        GridData equalOrBelow2Gd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrBelow2Gd.widthHint = DEGREE_WIDTH;
        minTmpEqOrBlw2Txt.setLayoutData(equalOrBelow2Gd);
        minTmpEqOrBlw2Txt.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        minTmpEqOrBlw2Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        minTmpEqOrBlw2Txt.addListener(SWT.Modify, changeListener);

        Label deg3Lbl = new Label(minTempGrp, SWT.NONE);
        deg3Lbl.setFont(italicFont);
        deg3Lbl.setText("deg F");
    }

    /**
     * create the precipitation group
     */
    private void createPrecipGrp() {
        Group precipGrp = new Group(shell, SWT.SHADOW_IN);
        GridLayout grpGl = new GridLayout(3, false);
        grpGl.marginHeight = MARGIN;
        grpGl.marginWidth = MARGIN;
        precipGrp.setLayout(grpGl);
        precipGrp.setText("Precipitation");
        precipGrp.setFont(boldFont);

        // 1st line
        Label equalOrAbove1Lbl = new Label(precipGrp, SWT.NONE);
        equalOrAbove1Lbl.setText("Equal or Above:");

        precipEqOrAbv1Txt = new Text(precipGrp, SWT.BORDER);
        GridData equalOrAbove1Gd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrAbove1Gd.widthHint = INCH_WIDTH;
        precipEqOrAbv1Txt.setLayoutData(equalOrAbove1Gd);
        precipEqOrAbv1Txt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        precipEqOrAbv1Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        precipEqOrAbv1Txt.addListener(SWT.Modify, changeListener);

        Label inch1Lbl = new Label(precipGrp, SWT.NONE);
        inch1Lbl.setFont(italicFont);
        inch1Lbl.setText("in.");

        // 2nd line
        Label equalOrAbove2Lbl = new Label(precipGrp, SWT.NONE);
        equalOrAbove2Lbl.setText("Equal or Above:");

        precipEqOrAbv2Txt = new Text(precipGrp, SWT.BORDER);
        GridData equalOrAbove2Gd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrAbove2Gd.widthHint = INCH_WIDTH;
        precipEqOrAbv2Txt.setLayoutData(equalOrAbove2Gd);
        precipEqOrAbv2Txt.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        precipEqOrAbv2Txt.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        precipEqOrAbv2Txt.addListener(SWT.Modify, changeListener);

        Label inch2Lbl = new Label(precipGrp, SWT.NONE);
        inch2Lbl.setFont(italicFont);
        inch2Lbl.setText("in.");
    }

    /**
     * create the snow fall group
     */
    private void createSnowfallGrp() {
        Group snowfallGrp = new Group(shell, SWT.SHADOW_IN);
        GridLayout grpGl = new GridLayout(3, false);
        grpGl.marginHeight = MARGIN;
        grpGl.marginWidth = MARGIN;
        snowfallGrp.setLayout(grpGl);
        snowfallGrp.setText("Snowfall");
        snowfallGrp.setFont(boldFont);

        Label equalOrAboveLbl = new Label(snowfallGrp, SWT.NONE);
        equalOrAboveLbl.setText("Equal or Above:");

        snowEqOrAbvTxt = new Text(snowfallGrp, SWT.BORDER);
        GridData equalOrAboveGd = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1);
        equalOrAboveGd.widthHint = INCH_WIDTH;
        snowEqOrAbvTxt.setLayoutData(equalOrAboveGd);
        snowEqOrAbvTxt.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        snowEqOrAbvTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        snowEqOrAbvTxt.addListener(SWT.Modify, changeListener);

        Label inchLbl = new Label(snowfallGrp, SWT.NONE);
        inchLbl.setFont(italicFont);
        inchLbl.setText("in.");
    }

    /**
     * create OK and cancel buttons
     */
    private void createBottomButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, true);
        gl.marginWidth = MARGIN;
        buttonComp.setLayout(gl);

        Button okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("Save");
        okBtn.setFont(boldFont);

        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                savePreferences();
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setFont(boldFont);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    @Override
    public boolean shouldClose() {
        if (this.changeListener.isChangesUnsaved()) {
            return MessageDialog.openQuestion(shell, "Unsaved Changes",
                    "Close this window? Unsaved changes will be lost.");
        } else {
            return true;
        }
    }

    /**
     * display the preferences to GUI
     */
    private void displayPreferences() {
        changeListener.setIgnoreChanges(true);

        maxTmpEqOrAbv1Txt.setText(String.valueOf(preferenceValues.getT1()));
        maxTmpEqOrAbv2Txt.setText(String.valueOf(preferenceValues.getT2()));
        maxTmpEqOrBlwTxt.setText(String.valueOf(preferenceValues.getT3()));

        minTmpEqOrAbvTxt.setText(String.valueOf(preferenceValues.getT4()));
        minTmpEqOrBlw1Txt.setText(String.valueOf(preferenceValues.getT5()));
        minTmpEqOrBlw2Txt.setText(String.valueOf(preferenceValues.getT6()));

        if (preferenceValues.getP1() == ParameterFormatClimate.TRACE) {
            precipEqOrAbv1Txt.setText(ParameterFormatClimate.TRACE_SYMBOL);
        } else {
            precipEqOrAbv1Txt
                    .setText(String.format("%1.2f", preferenceValues.getP1()));
        }

        if (preferenceValues.getP2() == ParameterFormatClimate.TRACE) {
            precipEqOrAbv2Txt.setText(ParameterFormatClimate.TRACE_SYMBOL);
        } else {
            precipEqOrAbv2Txt
                    .setText(String.format("%1.2f", preferenceValues.getP2()));
        }

        if (preferenceValues.getS1() == ParameterFormatClimate.TRACE) {
            snowEqOrAbvTxt.setText(ParameterFormatClimate.TRACE_SYMBOL);
        } else {
            snowEqOrAbvTxt
                    .setText(String.format("%1.1f", preferenceValues.getS1()));
        }

        changeListener.setIgnoreChanges(false);
        changeListener.setChangesUnsaved(false);
    }

    /**
     * save preferences from GUI to preferenceValues. It will be saved to file
     * in ConfigParamDialog.savePreferences().
     */
    protected void savePreferences() {
        int tmp1Val = Integer.parseInt(maxTmpEqOrAbv1Txt.getText());
        int tmp2Val = Integer.parseInt(maxTmpEqOrAbv2Txt.getText());
        int tmp3Val = Integer.parseInt(maxTmpEqOrBlwTxt.getText());
        int tmp4Val = Integer.parseInt(minTmpEqOrAbvTxt.getText());
        int tmp5Val = Integer.parseInt(minTmpEqOrBlw1Txt.getText());
        int tmp6Val = Integer.parseInt(minTmpEqOrBlw2Txt.getText());

        float precip1Val = parsePrecipSnow(precipEqOrAbv1Txt.getText());
        float precip2Val = parsePrecipSnow(precipEqOrAbv2Txt.getText());

        float snowVal = parsePrecipSnow(snowEqOrAbvTxt.getText());

        boolean save = MessageDialog.openQuestion(shell, "Continue to save?",
                "Existing monthly, seasonal, and annual climate data will not\nbe automatically regenerated with the new thresholds.\nDo you want to continue?");

        if (save) {
            preferenceValues.setT1(tmp1Val);
            preferenceValues.setT2(tmp2Val);
            preferenceValues.setT3(tmp3Val);
            preferenceValues.setT4(tmp4Val);
            preferenceValues.setT5(tmp5Val);
            preferenceValues.setT6(tmp6Val);

            preferenceValues.setP1(precip1Val);
            preferenceValues.setP2(precip2Val);

            preferenceValues.setS1(snowVal);

            /*
             * Write preference to " globalDay.properties" & set "returnValue"
             * to "true" to indicate values have been changed.
             */
            writePreferences(preferenceValues);

            changeListener.setChangesUnsaved(false);

            this.setReturnValue(true);

            close();
        }
    }

    /**
     * parse a string for precipitation or snow value
     * 
     * @param valStr
     * @return
     */
    private float parsePrecipSnow(String valStr) {
        float precipSnowVal;

        if (valStr.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
            precipSnowVal = ParameterFormatClimate.TRACE;
        } else {
            precipSnowVal = Float.parseFloat(valStr);
        }

        return precipSnowVal;
    }

    /**
     * Write preferences to file
     * 
     * @param preferenceValues
     */
    private void writePreferences(ClimateGlobal preferenceValues) {

        ClimateRequest cr = new ClimateRequest();
        cr.setRequestType(RequestType.SAVE_GLOBAL);
        cr.setClimateGlobal(preferenceValues);
        try {
            int status = (int) ThriftClient.sendRequest(cr);

            if (status == 0) {
                changeListener.setChangesUnsaved(false);
            } else if (status == -1) {
                logger.error(
                        "ThresholdsDailog: could not open preference file for writing.");
            } else if (status == -2) {
                logger.error(
                        "ThresholdsDailog: could not close preference file after writing.");
            }
        } catch (VizException e) {
            logger.error("ThresholdsDailog: failed to save preferences.", e);
        }
    }

}
