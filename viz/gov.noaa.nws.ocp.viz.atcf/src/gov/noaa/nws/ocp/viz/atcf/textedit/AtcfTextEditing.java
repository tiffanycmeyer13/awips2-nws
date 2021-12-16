/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.textedit;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.texteditor.dialogs.TextEditorDialog;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog.CAVE;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Manages ATCF text editor life cycles.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2020 80896       dfriedman   Initial creation.
 * Oct 19, 2020 82721       jwu         Add AdvisoryType.
 * Nov 12, 2020 84446       dfriedman   Support multiple editors.
 * Mar 22, 2021 88518       dfriedman   Rework product headers.
 * </pre>
 *
 * @author dfriedman
 *
 */
public class AtcfTextEditing {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AtcfTextEditing.class);

    private static final int MIN_TOKEN = 50;

    private static final int MAX_TOKEN = 79;

    private static Map<Integer, AtcfTextEditor> editors = new HashMap<>();

    private AtcfTextEditing() {
    }

    public static void editProduct(String stormId, String text,
            AdvisoryType editType) {
        AtcfTextEditor dlg = getEditor();
        if (dlg != null) {
            dlg.beginEditingProduct(stormId, text, null, editType);
        }
    }

    private static AtcfTextEditor getEditor() {
        int t;
        for (t = MIN_TOKEN; t <= MAX_TOKEN; ++t) {
            if (!editors.containsKey(t)) {
                break;
            }
        }

        if (t > MAX_TOKEN) {
            statusHandler.error(
                    "Too many open ATCF editors.  Please close an existing editor.");
            return null;
        }

        final int token = t;
        AtcfTextEditor textEditorDlg = editors.get(token);
        if ((textEditorDlg == null) || textEditorDlg.isDisposed()) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            textEditorDlg = new AtcfTextEditor(shell, CAVE.INDEPENDENT_SHELL,
                    token);
            editors.put(token, textEditorDlg);

            textEditorDlg.addCloseCallback(ov -> {
                synchronized (TextEditorDialog.class) {
                    editors.remove(token);
                }
            });
        }

        return textEditorDlg;
    }

}