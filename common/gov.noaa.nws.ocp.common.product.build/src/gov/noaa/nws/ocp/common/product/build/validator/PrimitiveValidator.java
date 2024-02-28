/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.validator;

import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * PrimitiveValidator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2020  71720      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class PrimitiveValidator extends BaseValidator {

    public PrimitiveValidator() {
        super();
    }

    /**
     * Takes in a jsonElement to be validated, and the schema that it validates
     * which contains maxLength, minLength, and pattern
     *
     * @param schema
     *            a schema containing the above mentioned keywords
     * @param string
     *            a jsonElement to be validated
     */
    public void validateString(JsonPrimitive string, JsonObject schema) {

        if (string.isString()) {
            // adheres to section 5.2.1 of Json schema validation for maxLength
            if (schema.has("maxLength")) {
                valid = this.validateLength(string, schema, "maxLength");
            }
            // adheres to section 5.2.2 of Json schema validation for minLength
            if (schema.has("minLength")) {
                valid = this.validateLength(string, schema, "minLength");
            }
            // add pattern validation, good for control formated number string
            if (schema.has("pattern")) {
                valid = this.validatePattern(string, schema);
            }
        }

    }

    /**
     * Takes in a jsonElement to be validated, and the schema that it validates
     * which contains exclusiveMaximum, maximum, multipleOf, minimum or
     * exclusiveMinimum
     *
     * @param schema
     *            a schema containing the above mentioned keywords
     * @param number
     *            a jsonElement to be validated
     */
    public void validateNumber(JsonPrimitive number, JsonObject schema) {

        // if jsonElement is a primitve, check if it is a number, if it's a
        // string, ignore it

        if (number.isNumber()) {
            // adheres to section 5.1.1 of Json schema validation for multiple
            // of
            if (schema.has("multipleOf")) {
                valid = this.validateMultipleOf(number, schema);
            }
            // adheres to section 5.1.2 of Json schema validation for max and
            // exclusive max
            if (schema.has("maximum") || schema.has("exclusiveMaximum")) {
                valid = this.validateMax(number, schema);
            }
            // adheres to section 5.1.3 of Json schema validation for min and
            // exclusive min
            if (schema.has("minimum") || schema.has("exclusiveMinimum")) {
                valid = this.validateMin(number, schema);
            }
        }

    }

    /**
     * adheres to section 5.1.1 of Json schema validation for multiple of
     *
     * @param element
     *            a jsonElement to be validated
     * @param schema
     *            a schema containing the above mentioned keywords
     * @return true if Json adheres to schema, false otherwise
     */
    private boolean validateMultipleOf(JsonElement element, JsonObject schema) {
        Double json = element.getAsDouble();
        Double validate = schema.get("multipleOf").getAsDouble();
        if ((json < 1 && json > 0) || (validate < 1 && validate > 0)) {
            if (json < validate) {
                while (json < 1) {
                    json = json * 10;
                    validate = validate * 10;
                }
            }
            if (json > validate) {
                while (validate < 1) {
                    json = json * 10;
                    validate = validate * 10;
                }
            }
        }

        if (json % validate != 0) {
            return false;
        }
        return true;
    }

    /**
     * adheres to section 5.2.1 of Json schema validation for maxLength adheres
     * to section 5.2.2 of Json schema validation for minLength
     *
     * @param element
     *            a jsonElement to be validated
     * @param schema
     *            a schema containing the above mentioned keywords
     * @param type
     *            whether validation is for max or min
     * @return true if Json adheres to schema, false otherwise
     */
    private boolean validateLength(JsonElement element, JsonObject schema,
            String type) {
        String json = element.getAsString();
        Integer validate = schema.get(type).getAsInt();
        if (json.length() > validate && "maxLength".equals(type)) {
            return false;
        }
        if (json.length() < validate && "minLength".equals(type)) {
            return false;
        }

        return true;
    }

    /**
     * adheres to section 5.1.3 of Json schema validation for min and exclusive
     * min
     *
     * @param element
     *            a jsonElement to be validated
     * @param schema
     *            a schema containing the above mentioned keywords
     * @return true if Json adheres to schema, false otherwise
     */
    private boolean validateMin(JsonElement element, JsonObject schema) {
        Double json = element.getAsDouble();
        Double validate = schema.get("minimum").getAsDouble();
        if (json < validate) {
            return false;
        }
        if (schema.has("exclusiveMinimum") && json.equals(validate)) {
            return false;
        }
        return true;
    }

    /**
     * adheres to section 5.1.2 of Json schema validation for max and exclusive
     * max
     *
     * @param element
     *            a jsonElement to be validated
     * @param schema
     *            a schema containing the above mentioned keywords
     * @return true if Json adheres to schema, false otherwise
     */
    private boolean validateMax(JsonElement element, JsonObject schema) {
        Double json = element.getAsDouble();
        Double validate = schema.get("maximum").getAsDouble();
        if (json > validate) {
            return false;
        }
        if (schema.has("exclusiveMaximum") && json.equals(validate)) {
            return false;
        }
        return true;
    }

    /**
     * 
     * @param string
     * @param schema
     * @return
     */
    private boolean validatePattern(JsonPrimitive string, JsonObject schema) {
        if (string.isString()) {
            if (schema.has("pattern")) {
                String p = schema.get("pattern").getAsString();
                return Pattern.matches(p, string.getAsString());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean validate(JsonElement json, JsonObject schema) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive element = json.getAsJsonPrimitive();
            if (schema.has("multipleOf") || schema.has("maximum")
                    || schema.has("exclusiveMaximum") || schema.has("minimum")
                    || schema.has("exclusiveMinimum")) {
                this.validateNumber(element, schema);
            }
            if (schema.has("maxLength") || schema.has("minLength")
                    || schema.has("pattern")) {
                this.validateString(element, schema);
            }
            if (schema.has("type") && valid) {
                valid = this.typeValidator.typeValidation(json, schema, valid);
            }
            if (schema.has("enum") && valid) {
                this.validEnum(element, schema);
            }
            if (schema.has("allOf") && valid) {
                this.allOf(element, schema);
            }
            if (schema.has("anyOf") && valid) {
                this.anyOf(element, schema);
            }
            if (schema.has("oneOf") && valid) {
                this.oneOf(element, schema);
            }
            if (schema.has("not") && valid) {
                this.not(element, schema);
            }
        }

        boolean result = valid;
        this.valid = true;

        return result;
    }
}
