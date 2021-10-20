/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.text;

import java.io.IOException;

import com.github.jknack.handlebars.io.AbstractTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;

/**
 * Localization-aware template loader for Handlebars.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 24, 2020 76600      dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 */
public class LocalizationTemplateLoader extends AbstractTemplateLoader implements TemplateLoader {

    protected LocalizationContext[] contexts;

    /**
     * Construct a template loader using default localization context(s) for
     * static files
     *
     * @param prefix
     * @param suffix
     */
    public LocalizationTemplateLoader(String prefix, String suffix) {
        setPrefix(prefix);
        setSuffix(suffix);
    }

    /**
     * Construct a template loader using the given localization contexts.
     *
     * @param prefix
     * @param suffix
     */
    public LocalizationTemplateLoader(String prefix, String suffix,
            LocalizationContext[] contexts) {
        this(prefix, suffix);
        this.contexts = contexts;
    }

    @Override
    public TemplateSource sourceAt(String location) throws IOException {
        String resolved = resolve(location);
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile lf;
        if (contexts == null) {
            lf = pm.getStaticLocalizationFile(resolved);
        } else {
            lf = pm.getStaticLocalizationFile(contexts, resolved);
        }
        if (lf != null) {
            return new LocalizationTemplateSource(lf);
        } else {
            return null;
        }
    }

}
