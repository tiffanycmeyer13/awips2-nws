/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.textedit;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.common.localization.ILocalizationPathObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.common.wmo.WMOHeader;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.texteditor.dialogs.TextEditorDialog;
import com.raytheon.viz.texteditor.msgs.ITextEditorCallback;
import com.raytheon.viz.texteditor.notify.NotifyExpiration;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;
import gov.noaa.nws.ocp.viz.atcf.advisory.AdvisoryUtil;

/**
 * TextWS-based editor with additional features to support ATCF.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2020 80896       dfriedman   Initial creation.
 * Oct 19, 2020 82721       jwu         Add AdvisoryType.
 * Nov 12, 2020 84446       dfriedman   Support product fragment editing.
 * Mar 22, 2021 88518       dfriedman   Rework product headers.
 * Jun 04, 2021 91765       jwu         Preserve edits in TCP sections.
 * </pre>
 *
 * @author dfriedman
 *
 */
public class AtcfTextEditor extends TextEditorDialog {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AtcfTextEditor.class);

    /**
     * Root directory of snippets in localization. Must not end with the path
     * component separator.
     */
    private static final String SNIPPETS_PATH = "atcf/config/snippets";

    private static final Pattern SECTION_HEADER = Pattern.compile("^-{3,}$");

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfTextEditor.class);

    private static final ITextEditorCallback textEditorCallback = new ITextEditorCallback() {
        @Override
        public void updateText(int teID, String newText) {
            // nothing
        }

        @Override
        public void restoreText(int teID) {
            // nothing
        }
    };

    private Menu snippetsMenu;

    private boolean snippetsMenuDirty = true;

    private SnippetsMenu snippets;

    private UpdateSnippetsMenuJob updateSnippetsJob;

    // storm ID of product to be edited.
    private String stormId;

    // Type of product to be edited.
    private AdvisoryType editType;

    /**
     * Set of localization directory paths to watch for changes to snippets. Set
     * to null when the dialog is disposed to indicate no new directories should
     * be watched. Synchronized on {@code observer}.
     */
    private HashSet<String> watchedSnippetPaths = new HashSet<>();

    private ILocalizationPathObserver snippetsPathsObserver = fil -> updateSnippetsJob
            .schedule(250);

    private Integer heightHint = null;

    public AtcfTextEditor(Shell parent, int additionalCaveStyle, int token) {
        /*
         * Token must be numeric because TextEditorDialog has
         * Integer.parseInt(token). For tokens other than "0" or "9", there must
         * have a non-null callback.
         */
        super(parent, "ATCF Editor", false, textEditorCallback,
                String.valueOf(token), true, false, additionalCaveStyle);
    }

    @Override
    protected void initializeComponents(final Shell shell) {
        super.initializeComponents(shell);

        Menu menuBar = shell.getMenuBar();
        int i = 0;
        for (MenuItem item : menuBar.getItems()) {
            if ("Scripts".equals(item.getText())) {
                break;
            }
            ++i;
        }

        MenuItem snippetsMenuBarItem = new MenuItem(menuBar, SWT.CASCADE, i);
        snippetsMenuBarItem.setText("Snippets");
        snippetsMenu = new Menu(snippetsMenuBarItem);
        snippetsMenu.addMenuListener(new MenuListener() {

            @Override
            public void menuShown(MenuEvent e) {
                if (snippetsMenuDirty) {
                    snippetsMenuDirty = false;
                    fillSnippetsMenu(snippetsMenu, snippets);
                }
            }

            @Override
            public void menuHidden(MenuEvent e) {
                // Not implemented
            }
        });
        snippetsMenuBarItem.setMenu(snippetsMenu);
        updateSnippetsJob = new UpdateSnippetsMenuJob();
        updateSnippetsJob.schedule();
    }

    /**
     * Background job to load currently available snippets from localization.
     */
    private class UpdateSnippetsMenuJob extends Job {

        public UpdateSnippetsMenuJob() {
            super("Update snippets menu");
            setSystem(true);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            IPathManager pm = PathManagerFactory.getPathManager();
            HashSet<String> newWatchedSnippetPaths = new HashSet<>();
            final SnippetsMenu snippetsCol = loadSnippetsMenu(pm, SNIPPETS_PATH,
                    newWatchedSnippetPaths);
            Set<String> newPaths = new HashSet<>(newWatchedSnippetPaths);
            synchronized (snippetsPathsObserver) {
                if (watchedSnippetPaths != null) {
                    newPaths.removeAll(watchedSnippetPaths);
                    for (String path : newPaths) {
                        pm.addLocalizationPathObserver(path,
                                snippetsPathsObserver);
                    }
                    watchedSnippetPaths.removeAll(newWatchedSnippetPaths);
                    for (String path : watchedSnippetPaths) {
                        pm.removeLocalizationPathObserver(path,
                                snippetsPathsObserver);
                    }
                    watchedSnippetPaths = newWatchedSnippetPaths;
                }
            }

            VizApp.runAsync(() -> {
                AtcfTextEditor.this.snippets = snippetsCol;
                AtcfTextEditor.this.snippetsMenuDirty = true;
                UpdateSnippetsMenuJob.this.done(Status.OK_STATUS);
            });

            return ASYNC_FINISH;
        }

    }

    /** Container for snippet item references. */
    private static class SnippetsMenu {
        private SnippetItem[] items;
    }

    /**
     * Represents an item in the Snippets menu. Either a snippet or a submenu.
     */
    private static class SnippetItem {
        private String name;

        /**
         * full localization path to the snippet
         */
        private String localizationPath;

        private SnippetsMenu menu;

        public String getName() {
            return name;
        }
    }

    /**
     * Recursively load the snippet items at the given path in localization.
     *
     * @param pm
     * @param path
     *            full localization path
     * @param watchedSnippetPaths
     *            output list of localization directories to watch
     * @return
     */
    private static SnippetsMenu loadSnippetsMenu(IPathManager pm, String path,
            Set<String> watchedSnippetPaths) {
        ArrayList<SnippetItem> items = new ArrayList<>();
        watchedSnippetPaths.add(path);
        for (LocalizationFile lf : pm.listStaticFiles(path, null, false,
                false)) {
            // The directory itself is included in the results so filter it out.
            if (path.equals(lf.getPath())) {
                continue;
            }

            SnippetItem item = new SnippetItem();
            String fileName = LocalizationUtil.extractName(lf.getPath());
            String name = fileName;
            if (name.endsWith(".txt")) {
                name = name.substring(0, name.length() - 4);
            }
            item.name = name;

            if (lf.isDirectory()) {
                item.menu = loadSnippetsMenu(pm,
                        path + IPathManager.SEPARATOR + fileName,
                        watchedSnippetPaths);
            } else {
                item.localizationPath = lf.getPath();
            }

            items.add(item);
        }
        SnippetsMenu menu = new SnippetsMenu();
        menu.items = items.toArray(new SnippetItem[items.size()]);
        Arrays.sort(menu.items, Comparator.comparing(SnippetItem::getName,
                String.CASE_INSENSITIVE_ORDER));
        return menu;
    }

    /**
     * Load the content of the given snippet.
     *
     * @param snippetPath
     *            full localization path
     * @return
     */
    private static String loadSnippet(String snippetPath) {
        LocalizationFile lf = PathManagerFactory.getPathManager()
                .getStaticLocalizationFile(snippetPath);
        return loadSnippet(lf);
    }

    private static String loadSnippet(LocalizationFile lf) {
        try (InputStream ins = lf.openInputStream()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            FileUtil.copy(ins, baos);
            return new String(baos.toByteArray());
        } catch (Exception e) {
            logger.error("error loading " + lf + ": " + e.getMessage(), e);
            return null;
        }
    }

    private void fillSnippetsMenu(Menu menu, SnippetsMenu snippets) {
        for (MenuItem mi : menu.getItems()) {
            mi.dispose();
        }
        if (snippets != null) {
            for (SnippetItem item : snippets.items) {
                MenuItem mi;
                if (item.menu != null) {
                    mi = new MenuItem(menu, SWT.CASCADE);
                    Menu subMenu = new Menu(mi);
                    mi.setMenu(subMenu);
                    fillSnippetsMenu(subMenu, item.menu);
                } else {
                    final SnippetItem s = item;
                    mi = new MenuItem(menu, SWT.PUSH);
                    mi.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            insertSnippet(s);
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            // nothing
                        }
                    });
                }
                mi.setText(item.name);
            }
        }
    }

    private void insertSnippet(SnippetItem s) {
        String content = loadSnippet(s.localizationPath);
        if (content != null) {
            insertText(content);
        }
    }

    @Override
    protected void disposed() {
        updateSnippetsJob.cancel();
        IPathManager pm = PathManagerFactory.getPathManager();
        synchronized (snippetsPathsObserver) {
            for (String path : watchedSnippetPaths) {
                pm.removeLocalizationPathObserver(path, snippetsPathsObserver);
            }
            watchedSnippetPaths = null;
        }
        super.disposed();
    }

    /**
     * Set up to edit a an advisory.
     *
     * @param stormId
     * @param afosId
     * @param product
     * @param notify
     * @param editType
     */
    public void beginEditingProduct(String stormId, String product,
            NotifyExpiration notify, AdvisoryType type) {

        this.stormId = stormId;
        this.editType = type;
        heightHint = StringUtils.countMatches(product, "\n") - 3;

        String note = String.format(
                "%s will be saved to file %s in ATCF product directory.",
                editType.getName(), stormId + editType.getSuffix());
        setEditorNote(note);
        super.beginEditingProduct(product, notify);
    }

    /**
     * When allowed attempt to save stored product.
     *
     * @param storedProduct
     * @return true product saved to server otherwise false
     */
    @Override
    protected boolean saveStoredTextProduct(StdTextProduct storedProduct) {
        String textToStore;
        if (editType.isFullProduct()) {
            textToStore = storedProduct.getProduct();
        } else {
            textToStore = getBodyText(storedProduct);
        }

        // For TCP & TCA-a, preserve the edits in sections.
        Map<AdvisoryType, String> advisories = new LinkedHashMap<>();
        if (editType == AdvisoryType.TCP || editType == AdvisoryType.TCP_A) {
            advisories.put(AdvisoryType.WARNINGS,
                    AdvisoryUtil.getTcpWatchWarningSection(textToStore));
            advisories.put(AdvisoryType.HEADLINE,
                    AdvisoryUtil.getTcpHeadlineSection(textToStore));
            advisories.put(AdvisoryType.HAZARDS,
                    AdvisoryUtil.getTcpHazardsSection(textToStore));
            advisories.put(AdvisoryType.TCP_DISCUSS,
                    AdvisoryUtil.getTcpDiscussionSection(textToStore));
        }

        advisories.put(editType, textToStore);
        boolean success = AdvisoryUtil.writeAdvisories(stormId, advisories);
        if (!success) {
            statusHandler.handle(Priority.ERROR,
                    "ATCFTextEditor - Error saving " + editType.name()
                            + " product for " + stormId);
        }

        return success;
    }

    /**
     * Get the text of the body of a product, skipping head header lines and
     * initial blank line.
     *
     * @param storedProduct
     * @return
     */
    private String getBodyText(StdTextProduct storedProduct) {
        String product = storedProduct.getProduct();
        String[] parts = product.split("\n", 5);
        int bodyStartLine = 0;

        if (parts.length > bodyStartLine
                && parts[bodyStartLine].startsWith("ZCZC ")) {
            bodyStartLine += 1;
        }

        if (parts.length > bodyStartLine
                && ((new WMOHeader((parts[bodyStartLine] + "\n").getBytes()))
                        .isValid()
                        || parts[bodyStartLine].startsWith("TTAA00 "))) {
            bodyStartLine += 1;
            if (parts.length > bodyStartLine) {
                String line = parts[bodyStartLine].trim();
                if (line.isEmpty()) {
                    bodyStartLine += 1;
                } else if ("NNNXXX".equals(line) || line.equals(
                        (storedProduct.getNnnid() + storedProduct.getXxxid())
                                .trim())) {
                    bodyStartLine += 1;
                    if (parts.length > bodyStartLine
                            && parts[bodyStartLine].trim().isEmpty()) {
                        bodyStartLine += 1;
                    }
                }
            }
        }

        return String.join("\n",
                Arrays.copyOfRange(parts, bodyStartLine, parts.length));
    }

    @Override
    protected void editHeader(String warning, boolean closeEditorOnCancel) {
        if (editType.isFullProduct()) {
            super.editHeader(warning, closeEditorOnCancel);
        } else {
            // nothing
        }
    }

    @Override
    protected void editorButtonMenuStates(boolean inEditMode) {
        super.editorButtonMenuStates(inEditMode);
        if (!editType.isFullProduct()) {
//            enableSend(false);
        }
    }

    @Override
    protected boolean saveTextProductInfo() {
        // do nothing
        return true;
    }

    @Override
    protected boolean isFullProduct() {
        return editType.isFullProduct();
    }

    @Override
    protected Integer getEditorHeightHint() {
        return heightHint;
    }

    @Override
    protected boolean isParagraphStart(int line) {
        return super.isParagraphStart(line) || (line > 0
                && SECTION_HEADER.matcher(getLine(line - 1)).matches());
    }

}