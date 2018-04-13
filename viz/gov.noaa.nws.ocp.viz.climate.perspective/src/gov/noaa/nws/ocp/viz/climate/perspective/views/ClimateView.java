/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdSendRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateProdSendRecordRequest;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;

/**
 * A view at left side of climate perspective to show climate products that have
 * been sent, including NWR, NWWS, F6, and RER products.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 08, 2016 20744      wpaintsil   Initial creation
 * May 04, 2016 33534      jwu         Implemented to show sent products.
 * 20 JUN 2017  35324      amoore      Month-day only date component was not saving or providing access to
 *                                     years. New functionality for component exposing a new date field
 *                                     with all date info.
 * 03 JUL 2017  35694      amoore      Alter to take into account new {@link DateSelectionComp} API.
 * 07 AUG 2017  33104      amoore      Fix comments and logic for missing dates.
 * </pre>
 * 
 * @author wpaintsil
 */

public class ClimateView extends ViewPart {

    /**
     * View ID.
     */
    public static final String ID = "gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateView";

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateView.class);

    /**
     * Main composites and constants.
     */
    private Composite viewer;

    private Composite mainComp;

    private ScrolledComposite scroll;

    private static final int MIN_VIEWER_WIDTH = 300;

    private static final int MIN_VIEWER_HEIGHT = 1000;

    /**
     * The view to show the content of a product.
     */
    private ClimateProductSentViewer sentView;

    /**
     * Fonts.
     */
    private Font size12Font;

    private Font size12FontBold;

    private Font size10Font;

    private Font size10FontBold;

    /**
     * Product period.
     */
    private DateSelectionComp startDateSelect;

    private DateSelectionComp endDateSelect;

    /**
     * Product List.
     */
    private List<ClimateProdSendRecord> productList;

    private Map<ClimateProductType, List<ClimateProdSendRecord>> productMap;

    /**
     * Selected Product.
     */
    private ClimateProdSendRecord selectedProduct;

    /**
     * Groups/Tables to list each type of product.
     */
    private Map<ClimateProductType, Group> productGroupMap;

    private Map<ClimateProductType, Table> productTableMap;

    /**
     * Default constructor.
     */
    public ClimateView() {
    }

    /**
     * Initialize this View.
     */
    @Override
    public void init(IViewSite site) throws PartInitException {

        try {
            super.init(site);
        } catch (PartInitException pie) {
            logger.error("ClimateView: failed to initialize the monitor.", pie);
        }

        // Create resource
        createResources();

        // Get the ClimateProductSentViewer
        sentView = (ClimateProductSentViewer) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage()
                .findView(ClimateProductSentViewer.ID);

        // Initialize product list.
        productList = new ArrayList<>();
        productMap = new HashMap<>();
        for (ClimateProductType prdType : ClimateProductType.values()) {
            productMap.put(prdType, new ArrayList<ClimateProdSendRecord>());
        }

        productGroupMap = new HashMap<>();
        productTableMap = new HashMap<>();

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

        mainComp = new Composite(scroll, SWT.NONE);

        scroll.setContent(mainComp);

        // Main layout with a single column, no equal width.
        RowLayout mainLayout = new RowLayout(SWT.VERTICAL);
        mainLayout.spacing = 20;
        mainLayout.marginTop = 10;
        mainLayout.marginWidth = 10;
        mainLayout.fill = true;
        mainLayout.center = true;
        mainComp.setLayout(mainLayout);

        // Group for user to pick product start/end dates.
        createDateGroup(mainComp, "Product Period");

        // Retrieve sent products.
        retrieveProducts();

        // Groups for NWR/NWWS/F6/RER products.
        for (ClimateProductType prdType : ClimateProductType.values()) {
            if (!(prdType == ClimateProductType.UNKNOWN)) {
                createProductGroup(mainComp, prdType, productMap.get(prdType));
            }
        }

        mainComp.layout();

    }

    /**
     * Sets the focus on this view.
     */
    @Override
    public void setFocus() {
        viewer.setFocus();
    }

    /**
     * Update the view.
     */
    public void update() {
        refreshView();
    }

    /**
     * Create fonts and colors used in this view.
     */
    private void createResources() {

        Display currentDisplay = Display.getCurrent();
        FontData fontData = currentDisplay.getSystemFont().getFontData()[0];

        size12Font = new Font(currentDisplay,
                new FontData(fontData.getName(), 12, SWT.NORMAL));
        size12FontBold = new Font(currentDisplay,
                new FontData(fontData.getName(), 12, SWT.BOLD));

        size10Font = new Font(currentDisplay,
                new FontData(fontData.getName(), 10, SWT.NORMAL));
        size10FontBold = new Font(currentDisplay,
                new FontData(fontData.getName(), 10, SWT.BOLD));
    }

    /**
     * Disposes resource.
     */
    @Override
    public void dispose() {
        if (size12Font != null) {
            size12Font.dispose();
        }
        if (size12FontBold != null) {
            size12FontBold.dispose();
        }
        if (size10Font != null) {
            size10Font.dispose();
        }
        if (size10FontBold != null) {
            size10FontBold.dispose();
        }

        super.dispose();
    }

    /**
     * Create a Group to pick product dates.
     * 
     * @param parent
     *            Parent composite
     * @param title
     *            Group title
     * @return createDateGroup() Group
     */
    private Group createDateGroup(Composite parent, String title) {

        // Group for user to pick product start/end dates.
        Group productDateGrp = new Group(mainComp, SWT.NONE);
        GridLayout timeGL = new GridLayout(3, false);
        timeGL.horizontalSpacing = 12;

        productDateGrp.setLayout(timeGL);
        productDateGrp.setText("Product Period");
        productDateGrp.setFont(size10FontBold);

        // Start date.
        ClimateDate startDate = ClimateDate.getPreviousDay();

        Label startLabel = new Label(productDateGrp, SWT.NORMAL);
        startLabel.setText("Start:");

        GridData lblGd1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,
                1);
        startLabel.setLayoutData(lblGd1);

        startDateSelect = new DateSelectionComp(productDateGrp, true, SWT.NONE,
                startDate, null, null, true);

        // "Refresh" button to force update from database.
        Button refreshBtn = new Button(productDateGrp, SWT.PUSH);
        GridData refreshBtnGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                false, 1, 2);
        refreshBtn.setText("Refresh");
        refreshBtn.setLayoutData(refreshBtnGd);
        refreshBtn.setFont(size10FontBold);

        // End date.
        Calendar cal = TimeUtil.newCalendar();

        Label endLabel = new Label(productDateGrp, SWT.NORMAL);
        endLabel.setText("End:  ");

        GridData lblGd2 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,
                1);
        endLabel.setLayoutData(lblGd2);

        ClimateDate endDate = new ClimateDate(cal.getTime());
        endDateSelect = new DateSelectionComp(productDateGrp, true, SWT.NONE,
                endDate, null, null, true);

        // Listener for "Refresh" button.
        refreshBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                refreshView();
            }
        });

        return productDateGrp;
    }

    /**
     * Retrieves sent climate product records within a given time period.
     * Default period will the beginning of previous day to the end of today (a
     * 48 hour span).
     */
    @SuppressWarnings("unchecked")
    private void retrieveProducts() {

        List<ClimateProdSendRecord> prdList = new ArrayList<>();

        ClimateDate startDate = null;
        ClimateDate endDate = null;

        // Dates from user selection.
        startDate = startDateSelect.getDate();

        endDate = endDateSelect.getDate();

        Calendar startCal = TimeUtil.newCalendar();
        startCal.add(Calendar.DAY_OF_MONTH, -1);

        if (startDate != null) {
            if (startDate.isMissing()) {
                logger.warn(
                        "Using the previous day for Climate sent product view since user selected a bad or missing date.");
                startDateSelect.setDate(ClimateDate.getPreviousDay());
            } else {
                startCal.set(startDate.getYear(), startDate.getMon(),
                        startDate.getDay(), 0, 0, 0);
                startCal.add(Calendar.MONTH, -1);
            }
        }

        Calendar endCal = TimeUtil.newCalendar();
        if (endDate == null) {
            endCal.add(Calendar.DAY_OF_MONTH, 2);
        } else {
            if (endDate.isMissing()) {
                logger.warn(
                        "Using current local date for Climate sent product view since user selected a bad or missing date.");
                endDateSelect.setDate(ClimateDate.getLocalDate());
            } else {
                endCal.set(endDate.getYear(), endDate.getMon(),
                        endDate.getDay(), 0, 0, 0);
                endCal.add(Calendar.MONTH, -1);
                endCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        /*
         * In case the start date is after the end date. Do a 48 hour span from
         * the start date.
         */
        if (startCal.after(endCal)) {
            endCal.set(startCal.get(Calendar.YEAR),
                    startCal.get(Calendar.MONTH),
                    startCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            endCal.add(Calendar.DAY_OF_MONTH, 2);
        }

        // Make the request to the database.
        Date sdate = startCal.getTime();
        Date edate = endCal.getTime();
        ClimateProdSendRecordRequest getSendRecordRequest = new ClimateProdSendRecordRequest(
                sdate, edate);

        try {
            prdList = (List<ClimateProdSendRecord>) ThriftClient
                    .sendRequest(getSendRecordRequest);
        } catch (VizException e) {
            logger.warn("ClimateView: No sent climate products retrieved.", e);
        }

        // Store as global to update the status in the table.
        productList.clear();
        for (List<ClimateProdSendRecord> rec : productMap.values()) {
            rec.clear();
        }

        for (ClimateProdSendRecord prd : prdList) {
            productList.add(prd);
            ClimateProductType prdType = getClimateProductType(
                    prd.getProd_type());
            productMap.get(prdType).add(prd);
        }

        for (ClimateProductType ptyp : productMap.keySet()) {
            Collections.sort(productMap.get(ptyp));
            Collections.reverse(productMap.get(ptyp));
        }
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
     * Identify a ClimateProductType by its String value.
     * 
     * @param itype
     *            Value of a PeriodType
     * @return getClimateProductType() ClimateProductType
     */
    private static ClimateProductType getClimateProductType(String itype) {

        ClimateProductType ptype = ClimateProductType.UNKNOWN;

        for (ClimateProductType typ : ClimateProductType.values()) {
            if (typ.toString().equals(itype.toUpperCase())) {
                ptype = typ;
                break;
            }
        }

        return ptype;
    }

    /**
     * Create a Group with given product list.
     * 
     * @param parent
     *            Parent composite
     * @param prdtbl
     *            Table to be generated in this group
     * @param title
     *            Group title
     * @param products
     *            List of products
     * 
     * @return createProductGroup() Group .
     */
    private void createProductGroup(Composite parent, ClimateProductType ptype,
            List<ClimateProdSendRecord> products) {

        Group prdGrp = new Group(parent, SWT.NONE);
        GridLayout prdGL = new GridLayout(1, false);
        prdGL.horizontalSpacing = 5;
        prdGL.marginWidth = 3;
        prdGrp.setLayout(prdGL);
        prdGrp.setText(ptype.toString());
        prdGrp.setFont(size10FontBold);

        Table prdTbl = new Table(prdGrp, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.HIDE_SELECTION);
        prdTbl.setHeaderVisible(true);
        prdTbl.setLinesVisible(true);
        prdTbl.setData(ptype);
        GridData prdTblGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                1, 1);
        prdTblGd.heightHint = prdTbl.getItemHeight() * 5;
        prdTbl.setLayoutData(prdTblGd);

        GC gc = new GC(prdTbl);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        TableColumn idColumn = new TableColumn(prdTbl, SWT.NONE);
        idColumn.setWidth(fontWidth * 20);
        idColumn.setText("\t\tID");
        idColumn.setAlignment(SWT.CENTER);

        TableColumn sentTimeColumn = new TableColumn(prdTbl, SWT.NONE);
        sentTimeColumn.setWidth(fontWidth * 22);
        sentTimeColumn.setText("Sent Time");
        sentTimeColumn.setAlignment(SWT.CENTER);

        if (products != null && products.size() > 0) {
            for (ClimateProdSendRecord prd : products) {
                addRecordToTable(prdTbl, prd);
            }
        }

        prdTbl.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem selected = (TableItem) e.item;

                ClimateProductType typeClicked = (ClimateProductType) selected
                        .getParent().getData();

                for (ClimateProductType prdType : productTableMap.keySet()) {
                    if (prdType != typeClicked) {
                        productTableMap.get(prdType).deselectAll();
                    }
                }

                selectedProduct = (ClimateProdSendRecord) selected.getData();

                // Get the ClimateProductSentViewer
                if (sentView == null) {
                    sentView = (ClimateProductSentViewer) PlatformUI
                            .getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage()
                            .findView(ClimateProductSentViewer.ID);
                }

                // Show the product viewer.
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage()
                            .showView(ClimateProductSentViewer.ID);
                    if (sentView != null) {
                        sentView.update(selectedProduct);
                    }
                } catch (PartInitException e1) {
                    logger.warn("ClimateView: cannot show product ["
                            + selectedProduct.getProd_id() + "]", e1);
                }
            }
        });

        productTableMap.put(ptype, prdTbl);
        productGroupMap.put(ptype, prdGrp);

    }

    /**
     * Add a record to a product table.
     * 
     * @param prdTbl
     * @param prd
     */
    private void addRecordToTable(Table prdTbl, ClimateProdSendRecord prd) {
        PeriodType ptyp = getPeriodType(prd.getPeriod_type());
        String idStr = prd.getProd_id();
        if (ptyp != PeriodType.OTHER) {
            idStr += ", " + ptyp.getPeriodName().toUpperCase();
        }

        String timeStr = prd.getSend_time().toString();
        int endLoc = timeStr.lastIndexOf(":");
        String sentTime = timeStr.substring(0, endLoc);

        TableItem item = new TableItem(prdTbl, SWT.NONE);
        item.setText(new String[] { idStr, sentTime });

        item.setData(prd);
    }

    /**
     * Refresh products from DB and update the view.
     * 
     */
    private void refreshView() {

        // Retrieve from DB.
        retrieveProducts();

        // Re-create all table items.
        for (Entry<ClimateProductType, Table> ptmEntry : productTableMap
                .entrySet()) {

            ClimateProductType prdType = ptmEntry.getKey();

            // Dispose all items.
            Table prdTable = ptmEntry.getValue();
            for (TableItem item : prdTable.getItems()) {
                item.dispose();
            }

            // Add products as new items into the table.
            int nitems = 0;
            for (ClimateProdSendRecord prd : productMap.get(prdType)) {
                addRecordToTable(prdTable, prd);

                // Keep the original selection.
                if (selectedProduct != null) {
                    if (prd.getProd_id().equals(selectedProduct.getProd_id())
                            && prd.getSend_time().toString()
                                    .equals(selectedProduct.getSend_time()
                                            .toString())) {
                        prdTable.select(nitems);
                    }
                }

                nitems++;
            }
        }
    }

}