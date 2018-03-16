/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdSendRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * A simple view to show the info and content of a sent climate product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 01, 2017 33534      jwu         Initial creation
 * Oct 23, 2017 39792      wpaintsil   Remove word wrap for product text field.
 * Nov 16, 2017 40113      dmanzella   Minor formatting fixes
 * 
 * </pre>
 * 
 * @author jwu
 */

public class ClimateProductSentViewer extends ViewPart {

    /**
     * View ID.
     */
    public static final String ID = "gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateProductSentViewer";

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProductSentViewer.class);

    /**
     * Main composites.
     */
    private Composite viewer;

    private ScrolledComposite scroll;

    private static final int MIN_VIEWER_WIDTH = 300;

    private static final int MIN_VIEWER_HEIGHT = 1000;

    /**
     * Styled text editor control.
     */
    private Composite productInfoComp;

    private Label idLblContent;

    private Label periodLblContent;

    private Label typeLblContent;

    private Label userLblContent;

    private Label fileLblContent;

    private Label timeLblContent;

    private StyledText productTxtContent;

    private Font textFont;

    private static final int TEXT_WINDOW_WIDTH = 675;

    private static final int TEXT_WINDOW_HEIGHT = 900;

    /**
     * Default constructor.
     */
    public ClimateProductSentViewer() {
    }

    /**
     * Initialize this View.
     * 
     * @param site
     *            Site this view belongs to
     */
    @Override
    public void init(IViewSite site) throws PartInitException {

        try {
            super.init(site);
        } catch (PartInitException pie) {
            logger.error(
                    "ClimateProductSentViewer: failed to initialize the monitor.",
                    pie);
        }

        // Create resource
        textFont = new Font(Display.getCurrent(), "Monospace", 10, SWT.NORMAL);
    }

    /**
     * Sets up the SWT controls.
     * 
     * @param parent
     *            Parent composite
     */
    @Override
    public void createPartControl(Composite parent) {

        viewer = parent;

        // A scroll-able composite to hold all widgets.
        scroll = new ScrolledComposite(viewer,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        scroll.setMinSize(MIN_VIEWER_WIDTH, MIN_VIEWER_HEIGHT);
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);

        Composite mainComp = new Composite(scroll, SWT.NONE);

        scroll.setContent(mainComp);

        // Main layout with a single column, no equal width.
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginTop = 20;
        mainLayout.verticalSpacing = 10;
        mainLayout.horizontalSpacing = 5;
        mainComp.setLayout(mainLayout);

        // Create product info
        createProductInfo(mainComp);

        // Create text window to show product content.
        createTextControl(mainComp, TEXT_WINDOW_WIDTH, TEXT_WINDOW_HEIGHT);

    }

    /**
     * Create a section to show product information.
     * 
     * @param parent
     *            Parent composite
     */
    private void createProductInfo(Composite parent) {
        productInfoComp = new Composite(parent, SWT.NONE);

        GridLayout prdGL = new GridLayout(2, false);
        prdGL.horizontalSpacing = 8;
        prdGL.verticalSpacing = 3;
        prdGL.marginWidth = 3;
        productInfoComp.setLayout(prdGL);

        productInfoComp
                .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        GridData gd1 = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        Label idLbl = new Label(productInfoComp, SWT.NORMAL);
        idLbl.setText("ID:");
        idLbl.setLayoutData(gd1);
        idLblContent = new Label(productInfoComp, SWT.NORMAL);
        GC gc = new GC(idLblContent);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        int minContentWidth = 24 * fontWidth;
        gc.dispose();
        GridData gdc1 = new GridData(SWT.LEFT, SWT.LEFT, true, false);
        gdc1.minimumWidth = minContentWidth;
        idLblContent.setLayoutData(gdc1);

        GridData gd2 = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        GridData gdc2 = new GridData(SWT.LEFT, SWT.LEFT, true, false);
        gdc2.minimumWidth = minContentWidth;
        Label periodLbl = new Label(productInfoComp, SWT.NORMAL);
        periodLbl.setText("Period:");
        periodLbl.setLayoutData(gd2);
        periodLblContent = new Label(productInfoComp, SWT.NORMAL);
        periodLblContent.setLayoutData(gdc2);

        GridData gd3 = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        GridData gdc3 = new GridData(SWT.LEFT, SWT.LEFT, true, false);
        gdc3.minimumWidth = minContentWidth;
        Label typeLbl = new Label(productInfoComp, SWT.NORMAL);
        typeLbl.setText("Type:");
        typeLbl.setLayoutData(gd3);
        typeLblContent = new Label(productInfoComp, SWT.NORMAL);
        typeLblContent.setLayoutData(gdc3);

        GridData gd4 = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        GridData gdc4 = new GridData(SWT.LEFT, SWT.LEFT, true, false);
        gdc4.minimumWidth = minContentWidth;
        Label userLbl = new Label(productInfoComp, SWT.NORMAL);
        userLbl.setText("User:");
        userLbl.setLayoutData(gd4);
        userLblContent = new Label(productInfoComp, SWT.NORMAL);
        userLblContent.setLayoutData(gdc4);

        GridData gd5 = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        GridData gdc5 = new GridData(SWT.LEFT, SWT.LEFT, true, false);
        gdc5.minimumWidth = minContentWidth;
        Label fileLbl = new Label(productInfoComp, SWT.NORMAL);
        fileLbl.setText("File:");
        fileLbl.setLayoutData(gd5);
        fileLblContent = new Label(productInfoComp, SWT.NORMAL);
        fileLblContent.setLayoutData(gdc5);

        GridData gd6 = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        GridData gdc6 = new GridData(SWT.LEFT, SWT.LEFT, true, false);
        gdc6.minimumWidth = minContentWidth;
        Label timeLbl = new Label(productInfoComp, SWT.NORMAL);
        timeLbl.setText("Time:");
        timeLbl.setLayoutData(gd6);
        timeLblContent = new Label(productInfoComp, SWT.NORMAL);
        timeLblContent.setLayoutData(gdc6);

    }

    /**
     * Create the styled text control.
     * 
     * @param parent
     *            Parent composite
     * @param width
     *            Hint for text window width
     * @param Height
     *            Hint for text window height
     */
    private void createTextControl(Composite parent, int width, int height) {
        GridData gd = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        gd.widthHint = width;
        gd.heightHint = height;
        productTxtContent = new StyledText(parent,
                SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        productTxtContent.setFont(textFont);
        productTxtContent.setEditable(false);
        productTxtContent.setLayoutData(gd);
    }

    /**
     * Disposes resource.
     */
    @Override
    public void dispose() {

        if (textFont != null) {
            textFont.dispose();
        }

        super.dispose();
    }

    /**
     * Sets focus.
     */
    @Override
    public void setFocus() {
        viewer.setFocus();
    }

    /**
     * Identify a PeriodType by its String value.
     * 
     * @param itype
     *            Value of a PeriodType
     * @return getPeriodType() PeriodType
     */
    private static PeriodType getPeriodType(String itype) {

        PeriodType ptype = PeriodType.OTHER;

        for (PeriodType typ : PeriodType.values()) {
            if (typ.toString().equals(itype)) {
                ptype = typ;
                break;
            }
        }

        return ptype;
    }

    /**
     * Update the content of the view.
     * 
     * @param record
     *            ClimateProdSendRecord
     * 
     */
    public void update(ClimateProdSendRecord record) {

        // Clear all.
        productTxtContent.setText("");
        idLblContent.setText("");
        periodLblContent.setText("");
        typeLblContent.setText("");
        userLblContent.setText("");
        fileLblContent.setText("");
        timeLblContent.setText("");

        // Update.
        if (record != null) {
            if (record.getProd_text() != null) {
                productTxtContent.setText(record.getProd_text());
            }

            if (record.getProd_id() != null) {
                idLblContent.setText(record.getProd_id());
            }

            String typStr = record.getPeriod_type();
            if (typStr != null) {
                PeriodType ptyp = getPeriodType(typStr);
                periodLblContent.setText(ptyp.getPeriodName().toUpperCase());
            }

            if (record.getProd_type() != null) {
                typeLblContent.setText(record.getProd_type().toUpperCase());
            }

            if (record.getUser_id() != null) {
                userLblContent.setText(record.getUser_id());
            }

            if (record.getFile_name() != null) {
                fileLblContent.setText(record.getFile_name());
            }

            if (record.getSend_time() != null) {
                String timeStr = record.getSend_time().toString();
                int endLoc = timeStr.lastIndexOf(":");
                String sentTime = timeStr.substring(0, endLoc);

                timeLblContent.setText(sentTime);
            }

            productInfoComp.layout(true, true);

        }
    }

}