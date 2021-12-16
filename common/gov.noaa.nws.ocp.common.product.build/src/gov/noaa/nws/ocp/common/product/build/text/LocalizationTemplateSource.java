package gov.noaa.nws.ocp.common.product.build.text;

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.github.jknack.handlebars.io.TemplateSource;
import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.exception.LocalizationException;

/**
 * Localization-aware template source for Handlebars.
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
public class LocalizationTemplateSource implements TemplateSource {

    protected ILocalizationFile localizationFile;

    public LocalizationTemplateSource(LocalizationFile localizationFile) {
        this.localizationFile = localizationFile;
    }

    @Override
    public String content(Charset charset) throws IOException {
        try (InputStream openedStream = localizationFile.openInputStream()) {
            return IOUtils.toString(openedStream, charset);
        } catch (LocalizationException e) {
            throw new IOException(
                    String.format("reading localization file %s: %s", localizationFile, e));
        }
    }

    @Override
    public String filename() {
        return localizationFile.getPath();
    }

    @Override
    public long lastModified() {
        return localizationFile.getTimeStamp().getTime();
    }

}
