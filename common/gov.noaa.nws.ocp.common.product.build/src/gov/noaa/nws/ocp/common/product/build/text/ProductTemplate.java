/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.text;

import com.github.jknack.handlebars.io.TemplateLoader;

/**
 * Process a template file.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 23, 2020 71720      pwang       Initial creation
 * Mar 24, 2020 76600      dfriedman   Improve product formatting localization support.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class ProductTemplate {

    /**
     * The template file extension
     */
    private String templateFileSuffix;

    /**
     * The template file path
     */
    private String templateFileLocation;

    /**
     * Constructor
     *
     * @param location
     * @param suffix
     */
    public ProductTemplate(String location, String suffix) {

        this.templateFileLocation = location;
        this.templateFileSuffix = suffix;

    }

    /**
     *
     * @return the templateFileSuffix
     */
    public String getTemplateFileSuffix() {
        return templateFileSuffix;
    }

    /**
     *
     * @return the templateFileLocation
     */
    public String getTemplateFileLocation() {
        return templateFileLocation;
    }

    /**
     *
     * @return the TemplateLoader
     */
    public TemplateLoader getTemplateLoader() {
        return new LocalizationTemplateLoader(templateFileLocation, templateFileSuffix);
    }

}
