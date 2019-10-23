/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.review.dialog;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.ModeListener;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct.ProductStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductType;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.DeleteClimateProdAfterReviewRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ReviewClimateProdRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.SaveModifiedClimateProdAfterReviewRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.SendNWWSClimateProductsRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.SendClimateProductsResponse;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;

/**
 * Dialog for NWWS climate product review
 *
 * Task 29418: It is desired that this dialog follow the same style as all other
 * Climate dialogs, and that the icon for the Find dialog be replaced. However,
 * due to constraints in baseline code, this change was not possible in initial
 * migration.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 21 FEB 2017   22162     astrakovsky Initial Creation.
 * 13 APR 2017   33104     amoore      Address comments from review.
 * 13 APR 2017   30167     astrakovsky Integration of Review into CPG workflow.
 * 16 MAY 2017   33104     amoore      Minor text prompt correction.
 * 16 MAY 2017   33104     amoore      FindBugs and rename.
 * 06 JUN 2017   30167     astrakovsky Minor changes to GUI.
 * 27 JUL 2017   33104     amoore      Do not use effectively final functionality, for 1.7 build.
 * 19 SEP 2017   38124     amoore      Use GC for text control sizes.
 * 02 OCT 2017   38582     amoore      Correct use of Font/FontData.
 * 23 OCT 2019   DR21683   wpaintsil   Refresh product map and table after "Send All" 
 *                                     procedure, so that warning pop-up doesn't appear.
 * </pre>
 * 
 * @author astrakovsky
 */

public class NWWSClimateReviewDialog extends ClimateReviewDialog {

    /**
     * Map containing climate products
     */
    protected Map<String, ClimateProduct> productMap;

    /**
     * Sash form for resizing list and text editor
     */
    private SashForm mainSashForm;

    /**
     * Buttons to clear, delete, save, and send.
     */
    private Button clearButton;

    private Button deleteButton;

    private Button saveButton;

    private Button sendButton;

    /**
     * Table for displaying climate products.
     */
    protected Table fileTable;

    /**
     * Table selection index.
     */
    protected int tableIndex;

    /**
     * Rectangle marking area applicable to the current table tooltip.
     */
    private Rectangle toolTipArea;

    /**
     * The session ID.
     */
    private String sessionID;

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(NWWSClimateReviewDialog.class);

    public NWWSClimateReviewDialog(Shell parentShell, String sessionID) {
        // these parameters would be preferred:
        /*
         * super(parentShell, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
         * ClimateLayoutValues.CLIMATE_DIALOG_CAVE_STYLE);
         */
        super(parentShell);
        this.sessionID = sessionID;
        setText("Review NWWS Climate Products");
    }

    @Override
    protected void initializeComponents(Shell oldshell) {

        // Task 29418: Extending a common AWIPS text editor resulted in not
        // being able to
        // directly edit style or icon. Constructing a new shell is a workaround
        // for this.
        shell = new Shell(getDisplay(),
                ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE);
        final DisposeListener disposeListener = new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                shell.dispose();
            }
        };
        getParent().addDisposeListener(disposeListener);
        shell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                getParent().removeDisposeListener(disposeListener);

            }
        });

        new ModeListener(shell);

        // Create the main layout for the shell.
        shell.setLayout(constructShellLayout());
        shell.setLayoutData(constructShellLayoutData());

        // end workaround

        // Initialize all of the controls and layouts
        setTextFont(new FontData("Monospace", 10, SWT.NORMAL));

        createMenus();

        createSashForm();

        createFileTableControl();

        createTextControl(mainSashForm, 800, 700);

        mainSashForm.setWeights(new int[] { 20, 80 });

        createButtonControls();

        // Listener when dialog closes.
        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (checkProductStatus(ProductStatus.PENDING)) {
                    MessageDialog dialog = new MessageDialog(shell, "Close?",
                            null,
                            "Are you sure you want to exit? Pending products will not be sent.",
                            MessageDialog.QUESTION,
                            new String[] { "Yes", "No" }, 1);
                    if (dialog.open() != MessageDialog.OK) {
                        event.doit = false;
                    }
                }
            }
        });

    }

    @Override
    protected void opened() {
        // if no products, show message indicating no products
        if (productMap.isEmpty()) {
            MessageDialog dialog = new MessageDialog(shell, "No Products", null,
                    "There are no NWWS products to display.",
                    MessageDialog.ERROR, new String[] { "OK" }, 0);
            dialog.open();
        }
    }

    /**
     * Check if there is a product with the specified status
     */
    private boolean checkProductStatus(ProductStatus status) {
        for (Map.Entry<String, ClimateProduct> entry : productMap.entrySet()) {
            if (entry.getValue().getStatus().equals(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Import the list of climate products
     */
    @SuppressWarnings("unchecked")
    private void importClimateProducts() {
        productMap = new HashMap<>();
        try {
            ReviewClimateProdRequest request = new ReviewClimateProdRequest(
                    sessionID, System.getProperty("user.name"));
            Map<String, ClimateProduct> tempMap = (Map<String, ClimateProduct>) ThriftClient
                    .sendRequest(request);
            PeriodType prodType;
            for (Map.Entry<String, ClimateProduct> entry : tempMap.entrySet()) {
                prodType = entry.getValue().getProdType();
                if (prodType != null
                        && PeriodType.isNWWS(prodType.getSource())) {
                    productMap.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (VizException e) {
            logger.error("Could not get NWWS climate products.", e);
        }
    }

    /**
     * Populate the file table
     */
    private void populateTable() {
        if (!productMap.isEmpty()) {
            // populate the table if there are products
            TableItem productItem;
            fileTable.removeAll();
            for (Map.Entry<String, ClimateProduct> entry : productMap
                    .entrySet()) {
                productItem = new TableItem(fileTable, SWT.BORDER);
                productItem.setText(0, entry.getKey());
                productItem.setText(1,
                        new SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a zzz")
                                .format(entry.getValue().getTime().getTime()));
                productItem.setText(2, entry.getValue().getStatus().toString());
            }
        }
        fileTable.setSortDirection(SWT.UP);
        sortTable();
        fileTable.deselectAll();
        tableIndex = -1;
    }

    /**
     * Sort the table by product status
     */
    protected void sortTable() {
        TableItem[] items = fileTable.getItems();
        Collator collator = Collator.getInstance(Locale.getDefault());
        boolean compResult;
        boolean displacedSelection = false;
        for (int i = 1; i < items.length; i++) {
            String value1 = items[i].getText(2);
            for (int j = 0; j < i; j++) {
                String value2 = items[j].getText(2);
                // compare based on sort direction
                if (fileTable.getSortDirection() == SWT.UP) {
                    compResult = collator.compare(value1, value2) < 0;
                } else {
                    compResult = collator.compare(value2, value1) < 0;
                }
                if (compResult) {
                    String[] values = { items[i].getText(0),
                            items[i].getText(1), items[i].getText(2) };
                    // check if selection was displaced
                    if (fileTable.getSelection().length > 0
                            && fileTable.getSelection()[0].equals(items[i])) {
                        displacedSelection = true;
                    }
                    items[i].dispose();
                    TableItem item = new TableItem(fileTable, SWT.NONE, j);
                    item.setText(values);
                    items = fileTable.getItems();
                    // preserve selection if necessary
                    if (displacedSelection) {
                        fileTable.setSelection(item);
                        displacedSelection = false;
                    }
                    break;
                }
            }
        }
        tableIndex = fileTable.getSelectionIndex();
    }

    /**
     * Create the sash form
     */
    private void createSashForm() {
        mainSashForm = new SashForm(shell, SWT.VERTICAL);
        mainSashForm.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 800;
        mainSashForm.setLayoutData(gd);
        mainSashForm.SASH_WIDTH = 5;
    }

    /**
     * Create and populate the table
     */
    private void createFileTableControl() {
        fileTable = new Table(mainSashForm,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        fileTable.setHeaderVisible(true);
        fileTable.setLinesVisible(true);
        fileTable.setFont(getTextFont());
        GridData fileTableGd = new GridData(SWT.FILL, SWT.FILL, true, true, 2,
                1);
        fileTableGd.heightHint = 120;
        fileTable.setLayoutData(fileTableGd);

        final TableColumn fileNameColumn = new TableColumn(fileTable, SWT.FILL);
        fileNameColumn.setWidth(340);
        fileNameColumn.setText("NWWS Climate Product");

        final TableColumn dateColumn = new TableColumn(fileTable, SWT.NONE);
        dateColumn.setWidth(340);
        dateColumn.setText("Date");

        final TableColumn statusColumn = new TableColumn(fileTable, SWT.NONE);
        statusColumn.setWidth(120);
        statusColumn.setText("Status");
        statusColumn.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (fileTable.getSortDirection() == SWT.UP) {
                    fileTable.setSortDirection(SWT.DOWN);
                } else {
                    fileTable.setSortDirection(SWT.UP);
                }
                sortTable();
            }
        });

        // import climate products from session
        importClimateProducts();

        // populate table with climate products
        populateTable();

        // read product contents from selection
        fileTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // check if user wants to abandon changes to current product
                if (tableIndex >= 0 && !getEditorStTxt().getText()
                        .equals(productMap
                                .get(fileTable.getItem(tableIndex).getText(0))
                                .getProdText())) {
                    MessageDialog dialog = new MessageDialog(shell,
                            "Switch Product?", null,
                            "Are you sure you want to switch to a different product? Your changes will not be saved.",
                            MessageDialog.WARNING, new String[] { "Yes", "No" },
                            1);
                    if (dialog.open() != MessageDialog.OK) {
                        // don't switch product
                        fileTable.setSelection(tableIndex);
                    } else {
                        // switch product and abandon changes
                        getEditorStTxt().setText(
                                productMap.get(((TableItem) e.item).getText(0))
                                        .getProdText());
                        tableIndex = fileTable.getSelectionIndex();
                    }
                }
                // switch product if no changes
                else {
                    getEditorStTxt().setText(
                            productMap.get(((TableItem) e.item).getText(0))
                                    .getProdText());
                    tableIndex = fileTable.getSelectionIndex();
                }
            }
        });

        // resize table columns as table is resized
        fileTable.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                int tableWidth = fileTable.getSize().x
                        - 2 * fileTable.getBorderWidth()
                        - 2 * fileTable.getGridLineWidth();
                if (fileTable.getVerticalBar() != null) {
                    tableWidth -= (fileTable.getVerticalBar().getSize().x
                            + 2 * fileTable.getBorderWidth());
                }
                fileNameColumn.setWidth((int) (tableWidth * 0.35));
                dateColumn.setWidth((int) (tableWidth * 0.45));
                statusColumn.setWidth(tableWidth - fileNameColumn.getWidth()
                        - dateColumn.getWidth() - 20);
            }
        });

        // show product status description on hover
        fileTable.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseHover(MouseEvent e) {
                Point coords = new Point(e.x, e.y);
                TableItem item = fileTable.getItem(coords);
                if (item == null) {
                    fileTable.setToolTipText("");
                    return;
                }
                toolTipArea = item.getBounds(2);
                if (toolTipArea.contains(coords)) {
                    fileTable.setToolTipText(
                            productMap.get((item).getText(0)).getStatusDesc());
                } else {
                    fileTable.setToolTipText("");
                }
            }

            @Override
            public void mouseExit(MouseEvent e) {
                fileTable.setToolTipText("");
            }

        });

        // clear tooltip to change location
        fileTable.addMouseMoveListener(new MouseMoveListener() {

            @Override
            public void mouseMove(MouseEvent e) {
                if (!(toolTipArea == null)) {
                    Point coords = new Point(e.x, e.y);
                    if (!toolTipArea.contains(coords)) {
                        fileTable.setToolTipText("");
                    }
                }
            }

        });

    }

    /**
     * Create the control buttons.
     */
    private void createButtonControls() {
        // -------------------------------------------
        // Create a button composite for the buttons
        // -------------------------------------------
        Composite buttonComp = new Composite(shell, SWT.NONE);

        GridLayout gl = new GridLayout(4, true);
        buttonComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        buttonComp.setLayoutData(gd);

        GC gc = new GC(buttonComp);
        int buttonWidth = 18 * gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        clearButton = new Button(buttonComp, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.setToolTipText("Clear text area");
        clearButton.setLayoutData(gd);
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                clearText();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        deleteButton = new Button(buttonComp, SWT.PUSH);
        deleteButton.setText("Delete");
        deleteButton.setToolTipText("Delete product");
        deleteButton.setLayoutData(gd);
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                deleteProduct();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setToolTipText("Save product");
        saveButton.setLayoutData(gd);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveProduct();
            }
        });

        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        sendButton = new Button(buttonComp, SWT.PUSH);
        sendButton.setText("Send All");
        sendButton.setToolTipText("Send all products");
        sendButton.setLayoutData(gd);
        sendButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sendProducts();
            }
        });

    }

    /**
     * Delete the currently selected product
     */
    protected void deleteProduct() {
        TableItem[] selected = fileTable.getSelection();
        if (selected.length == 1) {
            MessageDialog dialog = new MessageDialog(shell, "Delete Product?",
                    null,
                    "Are you sure you want to delete this product? This action cannot be undone.",
                    MessageDialog.WARNING, new String[] { "Yes", "No" }, 1);
            if (dialog.open() == MessageDialog.OK) {
                try {
                    DeleteClimateProdAfterReviewRequest request = new DeleteClimateProdAfterReviewRequest(
                            sessionID, ClimateProductType.NWWS,
                            selected[0].getText(0));
                    ThriftClient.sendRequest(request);
                    // product deleted, remove from GUI
                    productMap.remove(selected[0].getText(0));
                    fileTable.remove(fileTable.getSelectionIndex());
                    fileTable.deselectAll();
                    tableIndex = -1;
                    getEditorStTxt().setText("");
                } catch (VizException e) {
                    logger.error("Could not delete product: "
                            + selected[0].getText(0), e);
                }
            }
        } else {
            MessageDialog dialog = new MessageDialog(shell, "Invalid Selection",
                    null, "No product selected to delete.",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0);
            dialog.open();
        }
    }

    /**
     * Save the currently selected product
     */
    protected void saveProduct() {
        TableItem[] selected = fileTable.getSelection();
        if (selected.length == 1) {
            if (!getEditorStTxt().getText().equals(
                    productMap.get(selected[0].getText(0)).getProdText())) {
                MessageDialog dialog = new MessageDialog(shell, "Save Product?",
                        null,
                        "Are you sure you want to overwrite this product? This action cannot be undone.",
                        MessageDialog.QUESTION, new String[] { "Yes", "No" },
                        1);
                if (dialog.open() == MessageDialog.OK) {
                    // backup text being overwritten in case save fails
                    String prodTextBackup = productMap
                            .get(selected[0].getText(0)).getProdText();
                    try {
                        // update product text
                        productMap.get(selected[0].getText(0))
                                .setProdText(getEditorStTxt().getText());
                        // create and send save request
                        SaveModifiedClimateProdAfterReviewRequest request = new SaveModifiedClimateProdAfterReviewRequest(
                                sessionID, ClimateProductType.NWWS,
                                selected[0].getText(0),
                                productMap.get(selected[0].getText(0)));
                        ThriftClient.sendRequest(request);
                    } catch (VizException e) {
                        // reset text if request failed
                        productMap.get(selected[0].getText(0))
                                .setProdText(prodTextBackup);
                        // log error
                        logger.error("Could not save product: "
                                + selected[0].getText(0), e);
                    }
                }
            } else {
                MessageDialog dialog = new MessageDialog(shell, "No Changes",
                        null, "No changes to save.", MessageDialog.INFORMATION,
                        new String[] { "OK" }, 0);
                dialog.open();
            }
        } else {
            MessageDialog dialog = new MessageDialog(shell, "Invalid Selection",
                    null, "No product selected to save.",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0);
            dialog.open();
        }
    }

    /**
     * Send finalized products
     */
    protected void sendProducts() {
        MessageDialog dialog = new MessageDialog(shell, "Send Products?", null,
                "Are you sure you are ready to send these products? This action cannot be undone.",
                MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
        if (dialog.open() == MessageDialog.OK) {
            try {
                SendNWWSClimateProductsRequest request = new SendNWWSClimateProductsRequest(
                        sessionID, System.getProperty("user.name"));
                // check if request should be operational
                if (CAVEMode.getMode().equals(CAVEMode.OPERATIONAL)
                        || CAVEMode.getMode().equals(CAVEMode.TEST)) {
                    request.setOperational(true);
                }
                // send products and get response
                SendClimateProductsResponse response = (SendClimateProductsResponse) ThriftClient
                        .sendRequest(request);
                // Check if there are errors and update product statuses if so
                if (response.hasError()) {
                    // check if error status is indicated
                    if (response.getSetLevelStatus() != null) {
                        // handle error products
                        ProductStatus status;
                        StringBuilder errProdList = new StringBuilder("");
                        for (int i = 0; i < fileTable.getItemCount(); i++) {
                            // update statuses
                            status = response.getSendingProducts()
                                    .get(fileTable.getItem(i).getText(0))
                                    .getStatus();
                            fileTable.getItem(i).setText(2, status.toString());
                            productMap.get(fileTable.getItem(i).getText(0))
                                    .setStatus(status);
                            if (status.equals(ProductStatus.ERROR)) {
                                errProdList.append(
                                        "\n" + fileTable.getItem(i).getText(0));
                            }
                        }
                        // error message listing bad products
                        MessageDialog errorDialog = new MessageDialog(shell,
                                "Error!", null,
                                response.getSetLevelStatus().getDescription()
                                        + "\nThe following products could not be sent:"
                                        + errProdList.toString(),
                                MessageDialog.ERROR, new String[] { "OK" }, 0);
                        errorDialog.open();
                    } else {
                        // more generic error message since product status was
                        // not returned
                        MessageDialog errorDialog = new MessageDialog(shell,
                                "Error!", null, "Products could not be sent!",
                                MessageDialog.ERROR, new String[] { "OK" }, 0);
                        errorDialog.open();
                    }
                    throw new VizException();
                }
                // Refresh product map and table to reflect newly sent products.
                importClimateProducts();
                populateTable();
                // close window after sending if no error
                close();
            } catch (VizException e) {
                logger.error("Error sending NWWS products.", e);
            }
        }
    }

}