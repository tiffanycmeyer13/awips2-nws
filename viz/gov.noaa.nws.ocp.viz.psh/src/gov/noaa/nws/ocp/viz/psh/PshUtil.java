/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.WordUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.auth.UserController;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshPreviewServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshProductServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshProductTransmitRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.RetrievePSHDataRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.SavePSHDataRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.response.PshProductServiceResponse;
import gov.noaa.nws.ocp.common.localization.psh.PshCities;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshStation;
import gov.noaa.nws.ocp.viz.psh.ui.setup.PshMultiFieldSetupDialog;
import gov.noaa.ocp.viz.psh.data.PshCountiesProvider;
import gov.noaa.ocp.viz.psh.data.PshCounty;

/**
 * Utilities for PSH.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 07, 2017 #34955      jwu         Initial creation.
 * Jul 05, 2017 #35465      astrakovsky Added autocomplete field creation method.
 * Jul 26, 2017 #36367      astrakovsky Added sql query method.
 * Aug 15, 2017 #36981      astrakovsky Altered autocomplete field method.
 * Aug 22, 2017 #36922      astrakovsky Added lat/lon distance methods.
 * Aug 29, 2017 #37366      astrakovsky Added checkbox field method.
 * Sep 08, 2017 #36923      astrakovsky Added geographic distance and direction calculation methods.
 * Sep 14, 2017 #37365      jwu         Add methods to save/retrieve/format/transmit PSH product.
 * Sep 25, 2017 #36924      astrakovsky Added method for reading a file.
 * Nov 03, 2017 #39988      astrakovsky Added alternate table field methods for tracking changes.
 * Nov 22, 2017 #40417      astrakovsky Added alternate method for reading a file.
 * Dec 08, 2017 #41955      astrakovsky Added static county list for storing county geometry data.
 * Feb 15, 2018 #46354      wpaintsil   Various refactorings.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */

public class PshUtil {

    /**
     * Logger.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PshUtil.class);

    private static final SingleTypeJAXBManager<PshData> jaxb = SingleTypeJAXBManager
            .createWithoutException(PshData.class);

    /**
     * Path for icons.
     */
    private static final String ICONS = "icons/";

    /**
     * Handbook for help content.
     */
    private static final String HANDBOOK = "handbook/psh_usersguide_latest.pdf";

    /**
     * List containing counties with geometry data.
     */
    private static List<PshCounty> countyGeodata;

    private static final double MAP_CONSTANT = 0.0006213712;

    /**
     * Display the help content in a web browser.
     */
    public static void displayHandbook() {

        Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        URL url = bundle.getEntry(HANDBOOK);
        try {
            URL fileURL = FileLocator.toFileURL(url);
            try {
                PlatformUI.getWorkbench().getBrowserSupport()
                        .getExternalBrowser().openURL(fileURL);
            } catch (PartInitException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Create an image from a given file.
     *
     * @param imgFile
     *            Filename for the image
     * 
     * @return Image Image created from the file
     */
    public static Image createImage(String imgFile) {

        ImageDescriptor id = Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, ICONS + imgFile);

        Image img = null;
        if (id != null) {
            img = id.createImage();
        }

        return img;
    }

    /**
     * Create SWT fonts. Use the default font name. The font must be disposed
     * when it is no longer needed.
     * 
     * @param size
     *            Font size
     * @param style
     *            Font style such as SWT.BOLD
     * @return Font
     */
    public static Font createFont(int size, int style) {
        return createFont(
                Display.getCurrent().getSystemFont().getFontData()[0].getName(),
                size, style);
    }

    /**
     * Create SWT fonts. The font must be disposed when it is no longer needed.
     * 
     * @param name
     *            Font name
     * 
     * @param size
     *            Font size
     * @param style
     *            Font style such as SWT.BOLD
     * @return Font
     */
    public static Font createFont(String name, int size, int style) {
        return new Font(Display.getCurrent(), new FontData(name, size, style));
    }

    /**
     * Creates a labeled field.
     * 
     * @param parent
     *            - Composite, parent composite.
     * @param labelText
     *            - String, field label.
     * @param fieldText
     *            - String, field text.
     * @param fieldWidth
     *            - int, width of the field.
     * @param editable
     *            - boolean, field editability.
     * @return textField - Text, the text field.
     */
    public static Text createLabeledField(Composite parent, String labelText,
            String fieldText, int fieldWidth, boolean editable) {

        Label fieldName = new Label(parent, SWT.RIGHT);
        fieldName.setText(labelText);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        fieldName.setLayoutData(gd);
        fieldName.setEnabled(editable);

        Text textField = new Text(parent, SWT.BORDER | SWT.LEFT);
        textField.setText(fieldText);
        gd = new GridData();
        gd.widthHint = fieldWidth;
        textField.setLayoutData(gd);
        textField.setEnabled(editable);

        return textField;

    }

    /**
     * Creates an editable field for a table. This will keep track of unsaved
     * changes.
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param fieldText
     *            - String, initial text in field.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @param dialog
     *            - PshMultiFieldSetupDialog, the dialog containing the table.
     * @return TableEditor - The text field editor.
     */
    public static TableEditor createTableField(Table table, TableItem item,
            int column, String fieldText, String tipText,
            PshMultiFieldSetupDialog dialog) {

        Text textField = new Text(table, SWT.BORDER);
        textField.setText(fieldText);
        if (fieldText.equals("")) {
            textField.setToolTipText(tipText);
        }

        TableEditor fieldEditor = new TableEditor(table);
        fieldEditor.horizontalAlignment = SWT.LEFT;
        fieldEditor.grabHorizontal = true;
        fieldEditor.setEditor(textField, item, column);

        // set boolean based on current field value matching starting field
        // value
        textField.setData("fieldChanged",
                !textField.getText().equals(Objects.toString(
                        item.getData(table.getColumn(column).getText()), "")));

        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setToolTipText(tipText);
                } else {
                    textField.setToolTipText("");
                }
                // set boolean based on current field value matching starting
                // field value
                textField
                        .setData("fieldChanged",
                                !textField.getText()
                                        .equals(Objects
                                                .toString(
                                                        item.getData(table
                                                                .getColumn(
                                                                        column)
                                                                .getText()),
                                                        "")));
                // set unsaved changes boolean based on text field change
                // booleans in row
                dialog.setUnsavedChanges(dialog.checkChangedItems());
                // update buttons
                dialog.updateButtonStates();
            }

        });

        return fieldEditor;
    }

    /**
     * Creates an editable field for a table
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param fieldText
     *            - String, initial text in field.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @return TableEditor - The text field editor.
     */
    public static TableEditor createTableField(Table table, TableItem item,
            int column, String fieldText, String tipText) {

        Text textField = new Text(table, SWT.BORDER);
        textField.setText(fieldText);
        if (fieldText.equals("")) {
            textField.setToolTipText(tipText);
        }

        TableEditor fieldEditor = new TableEditor(table);
        fieldEditor.horizontalAlignment = SWT.LEFT;
        fieldEditor.grabHorizontal = true;
        fieldEditor.setEditor(textField, item, column);

        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setToolTipText(tipText);
                } else {
                    textField.setToolTipText("");
                }
            }

        });

        return fieldEditor;
    }

    /**
     * Creates an editable field for a table with autocomplete options. This
     * will keep track of unsaved changes.
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param fieldText
     *            - String, initial text in field.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @param autoSuggestions
     *            - String[], autocomplete suggestions.
     * @param dialog
     *            - PshMultiFieldSetupDialog, the dialog containing the table.
     * @return TableEditor - The text field editor.
     */
    public static TableEditor createAutoCompleteTableField(Table table,
            TableItem item, int column, String fieldText, String tipText,
            String[] autoSuggestions, PshMultiFieldSetupDialog dialog) {

        TableEditor fieldEditor = createTableField(table, item, column, tipText,
                tipText);

        Text textField = (Text) fieldEditor.getEditor();

        // set boolean based on current field value matching starting field
        // value
        textField.setData("fieldChanged",
                !textField.getText().equals(Objects.toString(
                        item.getData(table.getColumn(column).getText()), "")));

        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                // set boolean based on current field value matching starting
                // field value
                textField
                        .setData("fieldChanged",
                                !textField.getText()
                                        .equals(Objects
                                                .toString(
                                                        item.getData(table
                                                                .getColumn(
                                                                        column)
                                                                .getText()),
                                                        "")));
                // set unsaved changes boolean based on text field change
                // booleans in row
                dialog.setUnsavedChanges(dialog.checkChangedItems());
                // update buttons
                dialog.updateButtonStates();
            }

        });

        if (autoSuggestions != null) {
            new AutoCompleteField(textField, new TextContentAdapter(),
                    autoSuggestions);
        }

        return fieldEditor;
    }

    /**
     * Creates an editable field for a table with autocomplete options
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param fieldText
     *            - String, initial text in field.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @param autoSuggestions
     *            - String[], autocomplete suggestions.
     * @return TableEditor - The text field editor.
     */
    public static TableEditor createAutoCompleteTableField(Table table,
            TableItem item, int column, String fieldText, String tipText,
            String[] autoSuggestions) {

        TableEditor fieldEditor = createTableField(table, item, column, tipText,
                tipText);

        Text textField = (Text) fieldEditor.getEditor();

        if (autoSuggestions != null) {
            new AutoCompleteField(textField, new TextContentAdapter(),
                    autoSuggestions);
        }

        return fieldEditor;
    }

    /**
     * Creates an editable field for a table with autocomplete options
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param fieldText
     *            - String, initial text in field.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @param autoSuggestions
     *            - List<String>, autocomplete suggestions.
     * @return TableEditor - The text field editor.
     */
    public static TableEditor createAutoCompleteTableField(Table table,
            TableItem item, int column, String fieldText, String tipText,
            List<String> autoSuggestions) {

        return createAutoCompleteTableField(table, item, column, fieldText,
                tipText, autoSuggestions.toArray(new String[] {}));

    }

    /**
     * Creates a checkbox for a table. This will keep track of unsaved changes.
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param checkText
     *            - String, text that corresponds to a check.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @param dialog
     *            - PshMultiFieldSetupDialog, the dialog containing the table.
     * @return TableEditor - The field editor.
     */
    public static TableEditor createTableCheckbox(Table table, TableItem item,
            int column, String checkText, String tipText,
            PshMultiFieldSetupDialog dialog) {

        Button checkBox = new Button(table, SWT.CHECK);
        checkBox.setSelection(item.getText(column).trim().equals(checkText));
        if (!checkBox.getSelection()) {
            checkBox.setToolTipText(tipText);
        }

        TableEditor fieldEditor = new TableEditor(table);
        fieldEditor.horizontalAlignment = SWT.LEFT;
        fieldEditor.grabHorizontal = true;
        fieldEditor.setEditor(checkBox, item, column);

        // set boolean based on current check state matching starting check
        // state
        checkBox.setData("fieldChanged",
                checkText.equals(Objects.toString(
                        item.getData(table.getColumn(column).getText()),
                        "")) != checkBox.getSelection());

        checkBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!checkBox.getSelection()) {
                    checkBox.setToolTipText(tipText);
                } else {
                    checkBox.setToolTipText("");
                }
                // set boolean based on current check state matching starting
                // check state
                checkBox.setData("fieldChanged",
                        checkText.equals(Objects.toString(
                                item.getData(table.getColumn(column).getText()),
                                "")) != checkBox.getSelection());
                // set unsaved changes boolean based on text field change
                // booleans in row
                dialog.setUnsavedChanges(dialog.checkChangedItems());
                // update buttons
                dialog.updateButtonStates();
            }

        });

        return fieldEditor;
    }

    /**
     * Creates a checkbox for a table
     * 
     * @param table
     *            - Table, parent table.
     * @param item
     *            - TableItem, table item to edit.
     * @param column
     *            - int, column number.
     * @param checkText
     *            - String, text that corresponds to a check.
     * @param tipText
     *            - String, tooltip for field if field is blank.
     * @return TableEditor - The field editor.
     */
    public static TableEditor createTableCheckbox(Table table, TableItem item,
            int column, String checkText, String tipText) {

        Button checkBox = new Button(table, SWT.CHECK);
        checkBox.setSelection(item.getText(column).trim().equals(checkText));
        if (!checkBox.getSelection()) {
            checkBox.setToolTipText(tipText);
        }

        TableEditor fieldEditor = new TableEditor(table);
        fieldEditor.horizontalAlignment = SWT.LEFT;
        fieldEditor.grabHorizontal = true;
        fieldEditor.setEditor(checkBox, item, column);

        checkBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!checkBox.getSelection()) {
                    checkBox.setToolTipText(tipText);
                } else {
                    checkBox.setToolTipText("");
                }
            }

        });

        return fieldEditor;
    }

    /**
     * Creates a dialog to confirm exit.
     * 
     * @param shell
     *            The shell.
     * @return boolean Confirmation of exit.
     */
    public static boolean exitConfirmed(Shell shell) {
        return MessageDialog.openQuestion(shell, "Exit?",
                "Are you sure you want to exit?");
    }

    /**
     * Read the contents of a file into a list of strings (one line per string).
     * 
     * @param file
     *            A file to be read.
     * @return List<String> Content of the file.
     */
    public static List<String> readFileAsList(Path file) {

        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(file)) {
            list = stream.collect(Collectors.<String> toList());
        } catch (IOException e) {
            statusHandler.warn("PshUtil: Cannot read files from "
                    + file.getFileName() + " due to ", e);
            return null;
        }

        return list;
    }

    /**
     * Read the contents of a file into a single string.
     * 
     * @param file
     *            A file to be read.
     * @return String - Content of the file.
     */
    public static String readFileAsString(Path file) {

        String fileContents;

        try {
            fileContents = new String(Files.readAllBytes(file));
        } catch (IOException e) {
            statusHandler.warn("PshUtil: Cannot read files from "
                    + file.getFileName() + " due to ", e);
            return null;
        }

        return fileContents;
    }

    /**
     * A generic function that will execute a provided sql query and parameter
     * map and return the results.
     * 
     * @param sql
     *            the sql statement to execute
     * @param database
     *            the name of the database to use
     * @param paramMap
     *            map of query parameters
     * @return the records that were retrieved if successful or NULL if the
     *         query failed
     */
    public static List<Object[]> executeSQLQuery(String sql, String database,
            Map<String, Object> paramMap) {
        try {
            return DirectDbQuery.executeQuery(sql, database,
                    DirectDbQuery.QueryLanguage.SQL, paramMap);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getMessage());
            return null;
        }
    }

    /**
     * A generic function that will execute a provided sql query and return the
     * results.
     * 
     * @param sql
     *            the sql statement to execute
     * @param database
     *            the name of the database to use
     * @return the records that were retrieved if successful or NULL if the
     *         query failed
     */
    public static List<Object[]> executeSQLQuery(String sql, String database) {
        try {
            return DirectDbQuery.executeQuery(sql, database,
                    DirectDbQuery.QueryLanguage.SQL);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "", e);
            return null;
        }
    }

    /**
     * Takes the result of a query for a geometry and parses out the coordinate.
     * 
     * @param data
     * @return
     */
    public static Coordinate getCoordinate(Object data) {
        if (data == null) {
            return null;
        }
        try {
            byte[] bytes = (byte[]) data;
            WKBReader reader = new WKBReader();
            Geometry geo = null;

            geo = reader.read(bytes);
            return geo.getCoordinate();
        } catch (ParseException | ClassCastException e) {
            statusHandler.handle(Priority.PROBLEM, "", e);
            return null;
        }

    }

    /**
     * Get the names of all counties associated with Psh cities.
     * 
     * @param cities
     * @return
     */
    public static Set<String> getCountyNamesFromCities(PshCities cities) {

        Set<String> countySet = new LinkedHashSet<>();

        for (PshCity city : cities.getCities()) {
            countySet.add(city.getCounty());
        }

        return countySet;

    }

    /**
     * Get the county geometry data, and retrieve it from DB if it is null.
     * 
     * @return
     */
    public static List<PshCounty> getCountyGeodata() {

        if (countyGeodata == null) {
            countyGeodata = PshCountiesProvider
                    .getCounties(getCountyNamesFromCities(
                            PshConfigurationManager.getInstance().getCities()));
        }

        return countyGeodata;

    }

    /**
     * clear the county geometry data.
     */
    public static void clearCountyGeodata() {

        countyGeodata = null;

    }

    /**
     * Calculate distance between two points of latitude and longitude, using
     * units supplied by the earth radius.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param earthRad
     * @return Distance in units of earth radius
     */
    public static double latLonDistance(double lat1, double lon1, double lat2,
            double lon2, double earthRad) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRad * c;

        return distance;
    }

    /**
     * Calculate distance between two points of latitude and longitude in
     * statute miles.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return Distance in statute miles
     */
    public static double latLonDistanceMiles(double lat1, double lon1,
            double lat2, double lon2) {

        return latLonDistance(lat1, lon1, lat2, lon2,
                MapUtil.AWIPS_EARTH_RADIUS * 0.0006213712);

    }

    /**
     * Calculate direction angle between two points of latitude and longitude.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return Direction angle (azimuth)
     */
    public static double latLonDirection(double lat1, double lon1, double lat2,
            double lon2) {

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double y = Math.sin(lonDistance) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad)
                - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(lonDistance);

        return (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0;
    }

    /**
     * Convert angle to string representation.
     * 
     * @param angle
     * @return
     */
    public static String angleToString(double angle) {

        String compassDir = "";

        if (angle > 349.0 && angle <= 360.0) {
            compassDir = "N";
        } else if (angle > 0.0 && angle <= 11.0) {
            compassDir = "N";
        } else if (angle > 11.0 && angle <= 33.0) {
            compassDir = "NNE";
        } else if (angle > 33.0 && angle <= 57.0) {
            compassDir = "NE";
        } else if (angle > 57.0 && angle <= 79.0) {
            compassDir = "ENE";
        } else if (angle > 79.0 && angle <= 101.0) {
            compassDir = "E";
        } else if (angle > 101.0 && angle <= 123.0) {
            compassDir = "ESE";
        } else if (angle > 123.0 && angle <= 147.0) {
            compassDir = "SE";
        } else if (angle > 147.0 && angle <= 169.0) {
            compassDir = "SSE";
        } else if (angle > 169.0 && angle <= 191.0) {
            compassDir = "S";
        } else if (angle > 191.0 && angle <= 213.0) {
            compassDir = "SSW";
        } else if (angle > 213.0 && angle <= 237.0) {
            compassDir = "SW";
        } else if (angle > 237.0 && angle <= 259.0) {
            compassDir = "WSW";
        } else if (angle > 259.0 && angle <= 281.0) {
            compassDir = "W";
        } else if (angle > 281.0 && angle <= 303.0) {
            compassDir = "WNW";
        } else if (angle > 303.0 && angle <= 327.0) {
            compassDir = "NW";
        } else if (angle > 327.0 && angle <= 349.0) {
            compassDir = "NNW";
        } else {
            compassDir = "None";
        }

        return compassDir;

    }

    /**
     * Convert direction string to angle.
     * 
     * @param direction
     * @return
     */
    public static double stringToAngle(String direction) {

        double angle;

        if (direction.equals("N")) {
            angle = 0.0;
        } else if (direction.equals("NNE")) {
            angle = 23.0;
        } else if (direction.equals("NE")) {
            angle = 45.0;
        } else if (direction.equals("ENE")) {
            angle = 68.0;
        } else if (direction.equals("E")) {
            angle = 90.0;
        } else if (direction.equals("ESE")) {
            angle = 113.0;
        } else if (direction.equals("SE")) {
            angle = 135.0;
        } else if (direction.equals("SSE")) {
            angle = 158.0;
        } else if (direction.equals("S")) {
            angle = 180.0;
        } else if (direction.equals("SSW")) {
            angle = 203.0;
        } else if (direction.equals("SW")) {
            angle = 225.0;
        } else if (direction.equals("WSW")) {
            angle = 248.0;
        } else if (direction.equals("W")) {
            angle = 270.0;
        } else if (direction.equals("WNW")) {
            angle = 293.0;
        } else if (direction.equals("NW")) {
            angle = 315.0;
        } else if (direction.equals("NNW")) {
            angle = 338.0;
        } else {
            angle = 9999.0;
        }

        return angle;

    }

    /**
     * This function computes the resulting coordinate (latitude/longitude
     * point) based on the latitude/longitude passed, a distance, and a
     * direction.
     * 
     * @param lat
     *            latitude of point
     * @param lon
     *            longitude of point
     * @param distance
     *            distance (dist / earth radius in same units)
     * @param dir
     *            direction (degrees from N)
     * @return newCoor coordinate of the new point created
     */
    public static Coordinate computePoint(double lat, double lon,
            double distance, double dir) {
        final double PI = Math.PI, HALFPI = PI / 2.0, TWOPI = 2.0 * PI;

        /*
         * Convert the input values to radians.
         */
        double direction = Math.toRadians(dir);
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        double dLat = Math.asin(Math.sin(latRad) * Math.cos(distance)
                + Math.cos(latRad) * Math.sin(distance) * Math.cos(direction));
        double dLon, dLt, dLn;

        /*
         * Make sure the longitude is between -180 and +180 degrees.
         */
        lonRad = lonRad - (double) ((double) ((int) (lonRad / TWOPI)) * TWOPI);
        if (lonRad < -PI)
            lonRad = lonRad + TWOPI;
        if (lonRad > PI)
            lonRad = lonRad - TWOPI;

        /*
         * Compute the delta longitude. If the initial latitude is either pole,
         * then use the original longitude, otherwise, compute the new
         * longitude.
         */
        if ((Math.abs(latRad - 90.0F) < 0.000001)
                || (Math.abs(-latRad - 90.0F) < 0.000001)) {
            dLon = lonRad;
        } else {
            dLon = Math.atan2(
                    Math.sin(direction) * Math.sin(distance) * Math.cos(latRad),
                    Math.cos(distance) - Math.sin(latRad) * Math.sin(dLat));
            dLon = (lonRad + dLon + PI % TWOPI) - PI;
        }

        /*
         * Make sure that latitude is between -90 and +90 degrees. Adjust the
         * longitude, if necessary.
         */
        dLt = dLat - (double) ((double) ((int) (dLat / PI)) * PI);

        if (dLt > HALFPI) {
            dLt = PI - dLt;
            dLon = -dLon;
        }
        if (dLt < -HALFPI) {
            dLt = -PI - dLt;
            dLon = -dLon;
        }

        /*
         * Make sure the longitude is between -180 and +180 degrees.
         */
        dLn = dLon - (double) ((double) ((int) (dLon / TWOPI)) * TWOPI);
        if (dLn < -PI)
            dLn = dLn + TWOPI;
        if (dLn > PI)
            dLn = dLn - TWOPI;

        // Convert the new position to degrees and create coordinate based on
        // new lat/lon
        Coordinate newCoor = new Coordinate((double) (Math.toDegrees(dLn)),
                (double) (Math.toDegrees(dLt)));
        return newCoor;
    }

    /**
     * This function computes the resulting coordinate (latitude/longitude
     * point) based on the latitude/longitude passed, a distance in statute
     * miles, and a direction in statute miles.
     * 
     * @param lat
     *            latitude of point
     * @param lon
     *            longitude of point
     * @param dist
     *            distance (statute miles)
     * @param dir
     *            direction (degrees from N)
     * @return newCoor coordinate of the new point created
     */
    public static Coordinate computePointMiles(double lat, double lon,
            double dist, double dir) {

        return computePoint(lat, lon,
                dist / (MapUtil.AWIPS_EARTH_RADIUS * MAP_CONSTANT), dir);

    }

    /**
     * Find closest PSH city to the entered coordinate.
     * 
     * @param lat
     * @param lon
     * @param cities
     * @return Closest city
     */
    public static PshCity closestCity(double lat, double lon,
            PshCities cities) {

        // make sure city list is not empty
        if (!cities.getCities().isEmpty()) {

            // get first city
            PshCity closest = cities.getCities().get(0);
            double shortest = latLonDistanceMiles(lat, lon, closest.getLat(),
                    closest.getLon());
            double distance;

            // look through all cities for closest
            for (PshCity city : cities.getCities()) {
                distance = latLonDistanceMiles(lat, lon, city.getLat(),
                        city.getLon());
                if (distance < shortest) {
                    shortest = distance;
                    closest = city;
                }
            }

            // return closest city
            return closest;
        }
        return null;

    }

    /**
     * Gets the current site id from the localization Manager
     * 
     * @return current site ID
     */
    public static String getCurrentSite() {

        String wfo = LocalizationManager.getInstance().getCurrentSite();
        if (wfo.isEmpty()) {
            wfo = "Your Site";
        }
        return wfo;
    }

    /**
     * Saves PSH data object into database and HDF5.
     * 
     * @param data
     *            PshData object
     * 
     * @return boolean Flag to indicate if saved successfully.
     */
    public static boolean savePshData(PshData data) {

        SavePSHDataRequest request = new SavePSHDataRequest(data);

        boolean success;

        try {
            success = (boolean) ThriftClient.sendRequest(request);
        } catch (Exception e) {
            success = false;
            statusHandler.error("PshUtil - Save PshData failed: ", e);
        }

        return success;
    }

    /**
     * Retrieve PSH data for a specific basin, year and storm name.
     * 
     * @param basin
     *            PSH basin name
     * @param year
     *            Storm year
     * @param storm
     *            Storm name
     * 
     * @return PshData object.
     */
    public static PshData retrievePshData(String basin, int year,
            String storm) {

        RetrievePSHDataRequest request = new RetrievePSHDataRequest(basin, year,
                storm);

        PshData result = null;

        try {
            result = (PshData) ThriftClient.sendRequest(request);
        } catch (Exception e) {
            statusHandler.info("No data retrieved for the storm, " + storm);
        }

        return result;
    }

    /**
     * Send a request to import data from an XML file.
     * 
     * @param filePath
     * @return
     */
    public static PshData importPshDataFromXml(String filePath) {
        PshData result = null;

        try {
            result = jaxb.unmarshalFromXmlFile(filePath);
        } catch (Exception e) {
            statusHandler.warn("importPshDataFromXml - Error opening the file, "
                    + filePath + ".", e.getMessage());
        }

        return result;
    }

    /**
     * Build a PSH report for a given PshData object.
     * 
     * @param pdata
     *            PshData object
     * 
     * @return String.
     */
    public static String buildPshReport(PshData pdata) {

        String productStr = "";
        PshProductServiceRequest request = new PshProductServiceRequest(pdata);

        PshProductServiceResponse response;
        try {
            response = (PshProductServiceResponse) ThriftClient
                    .sendRequest(request);
            productStr = response.getMessage();
        } catch (VizException e) {
            statusHandler.error("PshUtil - build PSH product failed: ", e);
        }

        return productStr;
    }

    public static String buildPshPreview(PshData pdata, PshDataCategory type) {
        String productStr = "";
        PshPreviewServiceRequest request = new PshPreviewServiceRequest(pdata,
                type);

        PshProductServiceResponse response;
        try {
            response = (PshProductServiceResponse) ThriftClient
                    .sendRequest(request);
            productStr = response.getMessage();
        } catch (VizException e) {
            statusHandler.error("PshUtil - build PSH preview failed: ", e);
        }

        return productStr;
    }

    /**
     * Calculate the width of the Text area needed to accommodate PSH final
     * product that has a maximum of 69 characters in a line.
     *
     * @param textArea
     *            Text field to display product
     * @param font
     *            Font used to display product
     * @return width needed to display product
     */
    public static int getProductWidth(Text textArea, Font font) {

        int width;
        GC gc = new GC(textArea);
        gc.setFont(font);
        width = gc.getFontMetrics().getAverageCharWidth() * 70;
        gc.dispose();

        return width;
    }

    /**
     * Transmit a PSH report built from a given PshData object.
     * 
     * @param pdata
     *            PshData object
     * 
     * @return boolean.
     */
    public static boolean transmitPshReport(PshData pdata,
            boolean operational) {

        PshProductTransmitRequest request = new PshProductTransmitRequest(pdata,
                operational, UserController.getUserObject());

        boolean success = false;
        try {
            success = (boolean) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            statusHandler.error("PshUtil - Transmit PSH product failed: ", e);
        }

        return success;
    }

    /**
     * Error handling for Float.parseFloat()
     * 
     * @param valueString
     *            the String to parse
     * @return a float
     */
    public static float parseFloat(String valueString) {
        float value = 0;
        try {
            value = Float.parseFloat(valueString);
        } catch (NumberFormatException e) {
            statusHandler.error(
                    "PshUtil could not parse " + valueString + " to float");
        }
        return value;
    }

    /**
     * Error handling for Integer.parseInt()
     * 
     * @param valueString
     *            the String to parse
     * @return an int
     */
    public static int parseInt(String valueString) {
        int value = 0;
        try {
            value = Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            statusHandler.error(
                    "PshUtil could not parse " + valueString + " to int");
        }
        return value;
    }

    /**
     * Built a full name from a PshStation in format of "code-name state", where
     * name will be in lower case and capitalized.
     * 
     * @return A full name
     */
    public static String buildStationFullName(PshStation station) {
        String code = station.getCode();
        String state = station.getState();
        String name = WordUtils.capitalizeFully(station.getName().trim());

        String fname = "";
        if (code != null && !code.isEmpty()) {
            fname = code.trim();
        }

        if (name != null && !name.isEmpty()) {

            if (fname.length() > 0) {
                fname += "-";
            }
            fname += name.trim();
        }

        if (state != null && !state.isEmpty()) {
            fname = (fname + " " + state).trim();
        }

        return fname;
    }

}
