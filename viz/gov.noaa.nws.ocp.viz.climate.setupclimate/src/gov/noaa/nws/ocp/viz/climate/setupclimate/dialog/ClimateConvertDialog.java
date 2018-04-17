/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductTypeManager;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * 
 * Dialog for the user to point out where the legacy product type files exist so
 * they can be converted into the new XML format.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#   Engineer    Description
 * ------------ --------  ----------- --------------------------
 * Mar 27, 2017 29748     jwu         Initial creation
 * 17 MAY 2017  33104     amoore      FindBugs. Package reorg.
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class ClimateConvertDialog extends ClimateCaveDialog {
    /**
     * Default directory to look for legacy climate product types.
     */
    private static final String DEFAULT_SOURCE_DIR = "/awips/adapt/climate/data";

    /**
     * Directory to send XML climate product types to SITE level localization.
     */
    private static final String DEST_DIR_SITE = "/awips2/edex/data/utility/common_static/site/";

    private static final String DEST_DIR_CLIMATE = "/climate/productTypes";

    protected Text sourceDirTxt;

    protected Text destinationDirTxt;

    protected Button toSiteBtn;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent shell for this dialog.
     */
    public ClimateConvertDialog(Shell parent) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        setText("Select Source and Destination Directories");
    }

    /**
     * Initialize the dialog components.
     * 
     * @param shell
     *            parent shell for this dialog.
     */
    @Override
    protected void initializeComponents(final Shell shell) {

        GridLayout mainLayout = new GridLayout(8, true);
        mainLayout.verticalSpacing = 20;
        mainLayout.marginTop = 25;
        mainLayout.marginWidth = 10;

        shell.setLayout(mainLayout);

        GridData data1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);

        // Create the text box to show source directory
        Label fromLbl = new Label(shell, SWT.NONE);
        fromLbl.setText("From:");
        fromLbl.setLayoutData(data1);

        sourceDirTxt = new Text(shell, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 6;
        sourceDirTxt.setLayoutData(data);
        sourceDirTxt.setText(DEFAULT_SOURCE_DIR);

        // Browse button to select a source directory
        Button srcBrowseBtn = new Button(shell, SWT.PUSH);
        srcBrowseBtn.setText("Browse...");
        srcBrowseBtn.setLayoutData(data1);
        srcBrowseBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dlg = new DirectoryDialog(shell);

                // Set the initial filter path.
                dlg.setFilterPath(sourceDirTxt.getText());

                dlg.setText("Select the directory to read legacy products");

                dlg.setMessage("Select the directory to read legacy products");

                String dir = dlg.open();
                if (dir != null) {
                    sourceDirTxt.setText(dir);
                }
            }
        });

        // Create the text box to show destination path
        Label toLbl = new Label(shell, SWT.NONE);
        toLbl.setText("To:");
        toLbl.setLayoutData(data1);

        destinationDirTxt = new Text(shell, SWT.BORDER);
        destinationDirTxt.setLayoutData(data);
        destinationDirTxt.setText(getCurrentSizeLevelDir());

        // Browse button to select a destination directory
        Button destBrowseBtn = new Button(shell, SWT.PUSH);
        destBrowseBtn.setText("Browse...");
        destBrowseBtn.setLayoutData(data1);
        destBrowseBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dlg = new DirectoryDialog(shell);

                // Set the initial filter path.
                dlg.setFilterPath(destinationDirTxt.getText());

                dlg.setText("Select the directory to write XML products");
                dlg.setMessage("Select the directory to write XML products");

                String dir = dlg.open();
                if (dir != null) {
                    destinationDirTxt.setText(dir);
                }
            }
        });

        // Create a check button to force writing into SITE.
        Label placeHolder = new Label(shell, SWT.NONE);
        placeHolder.setText("");
        GridData data2 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        placeHolder.setLayoutData(data2);

        toSiteBtn = new Button(shell, SWT.CHECK);
        toSiteBtn.setText("Write to SITE: ");
        toSiteBtn.setLayoutData(data1);
        toSiteBtn.setSelection(true);
        toSiteBtn.setToolTipText(
                "Check this button to write product types into the current WFO's localization SITE level.");
        toSiteBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (toSiteBtn.getSelection()) {
                    destinationDirTxt.setText(getCurrentSizeLevelDir());
                }
            }
        });

        Text siteTxt = new Text(shell, SWT.BORDER);
        siteTxt.setLayoutData(data1);
        siteTxt.setEditable(false);
        siteTxt.setText(ClimateGUIUtils.getCurrentSite());

        Label placeHolder2 = new Label(shell, SWT.NONE);
        placeHolder.setText("");
        GridData data3 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        placeHolder2.setLayoutData(data3);

        // Create control buttons
        createControlButtons();
    }

    /**
     * Create control buttons - convert and cancel.
     */
    private void createControlButtons() {

        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 8, 1));

        final Button convertBtn = new Button(shell, SWT.PUSH);
        GridData convertGd = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        convertBtn.setLayoutData(convertGd);
        convertBtn.setText("Convert");

        convertBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (convert()) {
                    close();
                }
            }
        });

        Button cancelBtn = new Button(shell, SWT.PUSH);
        cancelBtn.setLayoutData(convertGd);
        cancelBtn.setText("Cancel");

        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * Convert product types.
     */
    protected boolean convert() {

        boolean success = true;

        // Validate the source directory.
        String src = sourceDirTxt.getText();
        String msg = null;
        if (src.isEmpty()) {
            msg = "No source has been specified.";
        } else {
            File checkDir = new File(src);
            if (!checkDir.exists()) {
                msg = "Directory " + src + " does not exist.";
            } else if (!checkDir.isDirectory()) {
                msg = src + " is not a directory.";
            } else if (!checkDir.canRead()) {
                msg = "You do not have permission to read directory " + src;
            }
        }

        if (msg != null) {
            MessageDialog.openInformation(shell, "Convert Product Types",
                    msg + "\nPlease specify a valid source directory.");

            return false;
        }

        // Validate the destination directory.
        String dest = destinationDirTxt.getText();
        if (toSiteBtn.getSelection()) {
            dest = getCurrentSizeLevelDir();
        }

        boolean toSiteLevel = dest.trim().equals(getCurrentSizeLevelDir());

        if (dest.isEmpty()) {
            MessageDialog.openInformation(shell, "Convert Product Types",
                    "No destination has been specified.\nPlease specify a valid destination directory.");
            return false;
        } else {
            File checkDir = new File(dest);

            // Create the directory if not existing.
            if (!(checkDir.exists() && checkDir.isDirectory())
                    && !toSiteLevel) {
                if (checkDir.mkdirs()) {

                    if (checkDir.setReadable(true, false)) {
                        if (!checkDir.setWritable(true, false)) {
                            MessageDialog.openInformation(shell,
                                    "Convert Product Types",
                                    "Chosen directory does not have appropriate write permissions.");
                            return false;
                        }
                    } else {
                        MessageDialog.openInformation(shell,
                                "Convert Product Types",
                                "Chosen directory does not have appropriate read permissions.");
                        return false;
                    }
                } else {
                    MessageDialog.openInformation(shell,
                            "Convert Product Types",
                            "Chosen directory does not exist and could not be created.");
                    return false;
                }
            }
        }

        // Now do the conversion.
        ClimateProductConverter converter = new ClimateProductConverter(src,
                dest);
        java.util.List<ClimateProductType> ptyps = converter.convert();

        // Write to localization at SITE level or a local directory.
        if (ptyps.size() > 0) {
            if (toSiteLevel) {
                for (ClimateProductType ptyp : ptyps) {
                    ClimateProductTypeManager.getInstance()
                            .saveProductType(ptyp);
                }
            } else {
                converter.writeXMLProducts(ptyps, dest);
            }

            MessageDialog.openInformation(shell, "Convert Product Types",
                    "" + ptyps.size()
                            + " product types have been converted to:\n"
                            + dest);
        } else {
            MessageDialog.openInformation(shell, "Convert Product Types",
                    "There are no valid product types existing in " + src
                            + ".\nPlease specify a valid source directory.");

            return false;
        }

        return success;
    }

    /**
     * Get the SITE level directory where the current site stores its
     * localization files.
     * 
     * @return Full directory to current office's localization SITE level.
     */
    protected String getCurrentSizeLevelDir() {
        return DEST_DIR_SITE + ClimateGUIUtils.getCurrentSite()
                + DEST_DIR_CLIMATE;
    }

}