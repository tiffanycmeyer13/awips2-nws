/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.review.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.avnconfig.FindReplaceDlg;
import com.raytheon.viz.avnconfig.TextEditorSetupDlg;

import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;

/**
 * Dialog for climate product review
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
 * 23 FEB 2017  22162      astrakovsky Initial Creation.
 * 16 MAY 2017  33104      amoore      FindBugs and rename.
 * 02 OCT 2017  38582      amoore      Correct use of Font/FontData.
 * 21 MAY 2019  DR21196    dfriedman   Call ancestor disposed method.
 * </pre>
 * 
 * @author astrakovsky
 */

public abstract class ClimateReviewDialog extends TextEditorSetupDlg {

    /**
     * Font for the styled text control.
     */
    private Font textFont;

    /**
     * Styled text editor control.
     */
    protected StyledText editorStTxt;

    /**
     * Editing toggle for text editor control.
     */
    protected boolean editMode;

    /**
     * Popup menu for cut, copy, paste, undo, and redo.
     */
    protected Menu popupMenu;

    protected List<Map<String, Object>> undoStack;

    protected List<Map<String, Object>> redoStack;

    private static final int UNDO_STACK_SIZE = 10;

    protected FindReplaceDlg findDlg;

    public ClimateReviewDialog(Shell parentShell) {
        // these parameters would be preferred:
        /*
         * super(parentShell, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
         * ClimateLayoutValues.CLIMATE_DIALOG_CAVE_STYLE);
         */
        super(parentShell);
    }

    @Override
    protected void disposed() {
        super.disposed();
        if (textFont != null) {
            textFont.dispose();
        }
    }

    /**
     * Create the menus at the top of the display.
     */
    protected final void createMenus() {
        Menu menuBar = new Menu(shell, SWT.BAR);

        createFileMenu(menuBar);
        createEditMenu(menuBar);
        createHelpMenu(menuBar);

        shell.setMenuBar(menuBar);
    }

    /**
     * Create the File menu.
     * 
     * @param menuBar
     *            Menu bar.
     */
    private void createFileMenu(Menu menuBar) {
        // -------------------------------------
        // Create the file menu
        // -------------------------------------
        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuItem.setText("&File");

        // Create the File menu item with a File "dropdown" menu
        Menu fileMenu = new Menu(menuBar);
        fileMenuItem.setMenu(fileMenu);

        // -------------------------------------------------
        // Create all the items in the File dropdown menu
        // -------------------------------------------------

        // Print menu item
        MenuItem printMI = new MenuItem(fileMenu, SWT.NONE);
        printMI.setText("&Print");
        printMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PrintDialog pd = new PrintDialog(shell);
                pd.open();
            }
        });

        // Separator bar
        new MenuItem(fileMenu, SWT.SEPARATOR);

        // Close menu item
        MenuItem closeMI = new MenuItem(fileMenu, SWT.NONE);
        closeMI.setText("&Close");
        closeMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.close();
            }
        });
    }

    /**
     * Create the Edit menu.
     * 
     * @param menuBar
     *            Menu bar.
     */
    private void createEditMenu(Menu menuBar) {
        // -------------------------------------
        // Create the file menu
        // -------------------------------------
        MenuItem editMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        editMenuItem.setText("&Edit");

        // Create the File menu item with a File "dropdown" menu
        Menu editMenu = new Menu(menuBar);
        editMenuItem.setMenu(editMenu);

        // -------------------------------------------------
        // Create all the items in the File dropdown menu
        // -------------------------------------------------

        // Cut menu item
        MenuItem cutMI = new MenuItem(editMenu, SWT.NONE);
        cutMI.setText("&Cut");
        cutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editorStTxt.cut();
            }
        });

        // Copy menu item
        MenuItem copyMI = new MenuItem(editMenu, SWT.NONE);
        copyMI.setText("&Copy");
        copyMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editorStTxt.copy();
            }
        });

        // Paste menu item
        MenuItem pasteMI = new MenuItem(editMenu, SWT.NONE);
        pasteMI.setText("&Paste");
        pasteMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editorStTxt.paste();
            }
        });

        // Find menu item
        MenuItem findMI = new MenuItem(editMenu, SWT.NONE);
        findMI.setText("&Find...");
        findMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Use the AvnFPS Find/Replace dialog
                if (mustCreate(findDlg)) {
                    findDlg = new FindReplaceDlg(shell, editorStTxt);
                    findDlg.open();
                } else {
                    findDlg.bringToTop();
                }
            }
        });

        // Undo menu item
        MenuItem undoMI = new MenuItem(editMenu, SWT.NONE);
        undoMI.setText("&Undo");
        undoMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                undoText();
            }
        });

        // Redo menu item
        MenuItem redoMI = new MenuItem(editMenu, SWT.NONE);
        redoMI.setText("&Redo");
        redoMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                redoText();
            }
        });
    }

    /**
     * Create the Help menu.
     * 
     * @param menuBar
     *            Menu bar.
     */
    private void createHelpMenu(Menu menuBar) {
        // -------------------------------------
        // Create the Help menu
        // -------------------------------------
        MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuItem.setText("&Help");

        // Create the File menu item with a File "dropdown" menu
        Menu helpMenu = new Menu(menuBar);
        helpMenuItem.setMenu(helpMenu);

        // -------------------------------------------------
        // Create all the items in the Help dropdown menu
        // -------------------------------------------------

        // Handbook menu item
        MenuItem aboutMI = new MenuItem(helpMenu, SWT.NONE);
        aboutMI.setText("Handbook");
        aboutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("execute_AM_PM_climate.html");
            }
        });
    }

    /**
     * Create the styled text control.
     */
    protected final void createTextControl(Composite parent, int width,
            int height) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = width;
        gd.heightHint = height;

        editorStTxt = new StyledText(parent,
                SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        editorStTxt.setWordWrap(true);
        editorStTxt.setFont(textFont);
        editorStTxt.setEditable(false);
        editorStTxt.setLayoutData(gd);
        editMode = false;

        undoStack = new ArrayList<Map<String, Object>>(UNDO_STACK_SIZE);
        redoStack = new ArrayList<Map<String, Object>>(UNDO_STACK_SIZE);

        editorStTxt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                if (editorStTxt.getEditable() && e.button == 3) {
                    popupMenu.setVisible(true);
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (!editMode && e.button == 1) {
                    MessageDialog dialog = new MessageDialog(shell,
                            "Edit Products?", null,
                            "Do you want to start editing?",
                            MessageDialog.QUESTION,
                            new String[] { "Yes", "No" }, 0);
                    if (dialog.open() == MessageDialog.OK) {
                        editMode = true;
                        editorStTxt.setEditable(true);
                    }
                }
            }

        });

        editorStTxt.addExtendedModifyListener(new ExtendedModifyListener() {
            @Override
            public void modifyText(ExtendedModifyEvent e) {
                // Check the modifyFlag, it will be set to false if we're
                // currently in the process of an undo or redo action, it should
                // be true otherwise.
                if (modifyFlag) {
                    int start = e.start;
                    int length = e.length;
                    String replacedText = e.replacedText;
                    Map<String, Object> undoData = new HashMap<>();
                    undoData.put("start", start);
                    undoData.put("length", length);
                    undoData.put("replacedText", replacedText);

                    if (undoStack.size() == UNDO_STACK_SIZE) {
                        undoStack.remove(0);
                    }

                    undoStack.add(undoData);
                    redoStack.clear();
                }
            }
        });

        createEditorPopupMenu(shell);

    }

    /**
     * Create a popup menu for the specified editor.
     * 
     * @param editorTextComp
     *            The text editor.
     */
    private void createEditorPopupMenu(Composite editorTextComp) {
        popupMenu = new Menu(editorTextComp);

        MenuItem cutMI = new MenuItem(popupMenu, SWT.NONE);
        cutMI.setText("Cut");
        cutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editorStTxt.cut();
            }
        });

        MenuItem copyMI = new MenuItem(popupMenu, SWT.NONE);
        copyMI.setText("Copy");
        copyMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editorStTxt.copy();
            }
        });

        MenuItem pasteMI = new MenuItem(popupMenu, SWT.NONE);
        pasteMI.setText("Paste");
        pasteMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editorStTxt.paste();
            }
        });

        // Find menu item
        MenuItem findMI = new MenuItem(popupMenu, SWT.NONE);
        findMI.setText("Find...");
        findMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Use the AvnFPS Find/Replace dialog
                if (mustCreate(findDlg)) {
                    findDlg = new FindReplaceDlg(shell, editorStTxt);
                    findDlg.open();
                } else {
                    findDlg.bringToTop();
                }
            }
        });

        MenuItem undoMI = new MenuItem(popupMenu, SWT.NONE);
        undoMI.setText("Undo");
        undoMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                undoText();
            }
        });

        MenuItem redoMI = new MenuItem(popupMenu, SWT.NONE);
        redoMI.setText("Redo");
        redoMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                redoText();
            }
        });

        editorTextComp.setMenu(popupMenu);
    }

    /**
     * The call back action for the Undo button.
     */
    protected final void undoText() {
        if (undoStack.size() > 0) {
            Map<String, Object> undoData = undoStack
                    .remove(undoStack.size() - 1);
            int start = (Integer) undoData.get("start");
            int length = (Integer) undoData.get("length");
            String text = (String) undoData.get("replacedText");
            undoData.clear();
            undoData.put("start", start);
            undoData.put("length", text.length());
            undoData.put("replacedText",
                    editorStTxt.getTextRange(start, length));
            redoStack.add(undoData);
            // Set the modifyFlag to false so the action of undoing the last
            // modification does not update the undoStack.
            modifyFlag = false;
            editorStTxt.replaceTextRange(start, length, text);
            editorStTxt.setCaretOffset(start + text.length());
            // Reset the modifyFlag so that subsequent modifications do update
            // the undoStack.
            modifyFlag = true;
        }
    }

    /**
     * Call back action for the Redo button.
     */
    protected final void redoText() {
        if (redoStack.size() > 0) {
            Map<String, Object> redoData = redoStack
                    .remove(redoStack.size() - 1);
            int start = (Integer) redoData.get("start");
            int length = (Integer) redoData.get("length");
            String text = (String) redoData.get("replacedText");
            redoData.clear();
            redoData.put("start", start);
            redoData.put("length", text.length());
            redoData.put("replacedText",
                    editorStTxt.getTextRange(start, length));
            undoStack.add(redoData);
            // Set the modifyFlag to false so the action of redoing the last
            // modification does not update the undoStack
            modifyFlag = false;
            editorStTxt.replaceTextRange(start, length, text);
            editorStTxt.setCaretOffset(start + text.length());
            // Reset the modifyFlag so that subsequent modifications do update
            // the undoStack.
            modifyFlag = true;
        }
    }

    /**
     * Clear the text field
     */
    protected final void clearText() {
        if (!editMode) {
            MessageDialog dialog = new MessageDialog(shell, "Edit Products?",
                    null, "Do you want to start editing?",
                    MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
            if (dialog.open() == MessageDialog.OK) {
                editMode = true;
                editorStTxt.setEditable(true);
            } else {
                return;
            }
        }
        MessageDialog dialog = new MessageDialog(shell, "Clear Text?", null,
                "Are you sure you want to clear the text field? Your changes will not be saved.",
                MessageDialog.WARNING, new String[] { "Yes", "No" }, 1);
        if (dialog.open() == MessageDialog.OK) {
            editorStTxt.setText("");
        }
    }

    /**
     * @return the textFont
     */
    protected final Font getTextFont() {
        return textFont;
    }

    /**
     * @param fontData
     *            the textFont to set
     */
    protected final void setTextFont(FontData fontData) {
        if (this.textFont != null) {
            this.textFont.dispose();
        }

        this.textFont = new Font(getDisplay(), fontData);
    }

    /**
     * @return the editorStTxt
     */
    protected final StyledText getEditorStTxt() {
        return editorStTxt;
    }

    /**
     * @param editorStTxt
     *            the editorStTxt to set
     */
    protected final void setEditorStTxt(StyledText editorStTxt) {
        this.editorStTxt = editorStTxt;
    }

    /**
     * @return the editMode
     */
    protected final boolean getEditMode() {
        return editMode;
    }

    /**
     * @param editMode
     *            the editMode to set
     */
    protected final void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    /**
     * Save the currently selected product
     */
    protected abstract void saveProduct();

    /**
     * Delete the currently selected product
     */
    protected abstract void deleteProduct();

    /**
     * Send finalized products
     */
    protected abstract void sendProducts();
}