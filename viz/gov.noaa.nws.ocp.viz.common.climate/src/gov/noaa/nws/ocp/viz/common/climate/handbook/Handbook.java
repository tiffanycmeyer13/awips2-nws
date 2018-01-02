package gov.noaa.nws.ocp.viz.common.climate.handbook;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * This class display the handbook
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#   Engineer   Description
 * ----------- --------- ---------- --------------------------
 * 09/02/2016  20635     wkwock     Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */

public class Handbook {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(Handbook.class);

    private static final String HANDBOOK_PATH = "handbook";

    private static final String PLUGIN_ID = "gov.noaa.nws.ocp.viz.common.climate";

    public static void displayHandbook(String fileName) {
        Bundle bundle = Platform.getBundle(PLUGIN_ID);
        URL url = bundle.getEntry(HANDBOOK_PATH + "/" + fileName);
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
}
