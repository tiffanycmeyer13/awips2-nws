package gov.noaa.nws.ocp.common.product.build.text;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateLoader;

import gov.noaa.nws.ocp.common.product.build.DataValidateException;
import gov.noaa.nws.ocp.common.product.build.IProductBuilder;
import gov.noaa.nws.ocp.common.product.build.ProductBuildException;

/**
 * Build a text product.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2020 71720      pwang       Initial creation
 * Oct 29, 2020 81820      wpaintsil   Add new helpers for lat/lon direction.
 * Nov 23, 2020 85264      wpaintsil   Add rounding helper.
 * Mar 22, 2021 88518      dfriedman   Add wrap helper.
 * Sep 02, 2021 95849      pwang       Replace DataSchema with a Utility DataValidation
 *                                     Addressed other code review comments
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class TextProductBuilder implements IProductBuilder {

    private String templateBaseDir;

    private String templateFileName;

    private String templateSuffix = "hbs";

    private String schemaFileName;


    /**
     * Constructor
     *
     * @param templateBaseDir
     * @param templateFileName
     * @param templateSuffix
     * @param schemaFileName
     */
    public TextProductBuilder(String templateBaseDir, String templateFileName,
            String templateSuffix, String schemaFileName) {
        this.templateBaseDir = templateBaseDir;
        this.templateFileName = templateFileName;
        if (templateSuffix != null) {
            this.templateSuffix = templateSuffix;
        }
        this.schemaFileName = schemaFileName;
    }

    @Override
    public String build(JsonNode data, boolean validate)
            throws ProductBuildException {
        boolean valid = validate ? DataValidation.validateDataAgainstSchema(
                templateBaseDir, schemaFileName, data.toString()) : true;

        /* get handlebars template */
        ProductTemplate prodTemplate = new ProductTemplate(templateBaseDir,
                templateSuffix);
        String prodString = "";

        if (valid) {
            TemplateLoader loader = prodTemplate.getTemplateLoader();

            // a newline unless we call prettyPrint(true).
            Handlebars handlebars = new Handlebars(loader).prettyPrint(true);

            // No escaping.
            handlebars = handlebars.with(EscapingStrategy.NOOP);

            registerHelpers(handlebars);

            Context context = Context.newBuilder(data)
                    .resolver(JsonNodeValueResolver.INSTANCE,
                            JavaBeanValueResolver.INSTANCE,
                            FieldValueResolver.INSTANCE,
                            MapValueResolver.INSTANCE,
                            MethodValueResolver.INSTANCE)
                    .build();

            try {
                // get the template file
                Template template = handlebars.compile(templateFileName);
                prodString = template.apply(context);
            } catch (IOException e) {
                throw new ProductBuildException(
                        "Filed to access the Template: " + templateFileName, e);
            }
        } else {
            throw new DataValidateException(
                    "Validation input JSON data is failed");
        }
        return prodString;
    }

    @Override
    public String build(String data, boolean validate)
            throws ProductBuildException {

        JsonNode inputData = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            inputData = mapper.readValue(data, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new ProductBuildException(
                    "Filed to map JSON string to a JsonNode", e);
        }

        return build(inputData, validate);

    }

    /*
     * Register helpers for the HandleBar
     *
     * @return the templateBaseDir
     */
    private void registerHelpers(Handlebars handlebars) {
        handlebars.registerHelper("left", StringHelpers.ljust);
        handlebars.registerHelper("right", StringHelpers.rjust);
        handlebars.registerHelper("format", StringHelpers.stringFormat);
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
        handlebars.registerHelper("shortLatDir",
                TextProductHelpers.shortLatitudeDirStr);
        handlebars.registerHelper("shortLonDir",
                TextProductHelpers.shortLongitudeDirStr);
        handlebars.registerHelper("fullLatDir",
                TextProductHelpers.fullLatitudeDirStr);
        handlebars.registerHelper("fullLonDir",
                TextProductHelpers.fullLongitudeDirStr);
        handlebars.registerHelper("round", TextProductHelpers.round);
        handlebars.registerHelper("myif", TextProductHelpers.myif);
        handlebars.registerHelper("wrap", TextProductHelpers.wrap);

        for (ConditionalHelpers hlp : ConditionalHelpers.values()) {
            handlebars.registerHelper(hlp.name(), hlp);
        }

        for (StringHelpers hlp : StringHelpers.values()) {
            handlebars.registerHelper(hlp.name(), hlp);
        }

    }

    /**
     *
     * @return the templateBaseDir
     */
    public String getTemplateBaseDir() {
        return templateBaseDir;
    }

    /**
     *
     * @return the templateSuffix
     */
    public String getTemplateSuffix() {
        return templateSuffix;
    }

    /**
     *
     * @return the schemaFileName
     */
    public String getSchemaFileName() {
        return schemaFileName;
    }


    /**
     *
     * @return the templateFileName
     */
    public String getTemplateFileName() {
        return templateFileName;
    }


}
