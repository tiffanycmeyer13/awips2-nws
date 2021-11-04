/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.validator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * ArrayValidator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2020 71720      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class ArrayValidator extends BaseValidator {

    public ArrayValidator() {
        super();
    }

    /**
     * adheres to section 5.3.1 of Json schema validation for items and
     * additionalItems for Array
     *
     * @param properties
     *            Array of json values
     * @param schema
     *            schema to validate against
     */
    private void validItemsArray(JsonArray properties, JsonObject schema) {

        JsonArray setOfItemType = schema.get("items").getAsJsonArray();
        for (int i = 0; i < properties.size(); i++) {
            if (i >= setOfItemType.size()) {
                if (schema.has("additionalItems")) {
                    valid = this.validAdditionalItems(properties, schema, i,
                            valid);
                } else {
                    break;
                }
            } else {
                valid = typeValidator.typeValidation(properties.get(i),
                        setOfItemType.get(i).getAsJsonObject(), valid);
            }
            if (!valid) {
                break;
            }
        }

    }

    /**
     * adheres to section 5.3.1 of Json schema validation for items and
     * additionalItems for Object
     *
     * @param properties
     *            Array of json values
     * @param schema
     *            schema to validate against
     */
    private void validItemsObject(JsonArray properties, JsonObject schema) {
        for (JsonElement property : properties) {
            // use generic validation for type
            valid = typeValidator.typeValidation(property,
                    schema.get("items").getAsJsonObject(), valid);
            if (!valid) {
                break;
            }
        }

    }

    /**
     * helper function specifically for additional items keyword
     *
     * @param properties
     *            Array of json values
     * @param schema
     *            schema to validate against
     * @param index
     *            index of the position of the additional object
     * @param valid
     *            current status of validation
     * @return result of additionalItems validation
     */
    private boolean validAdditionalItems(JsonArray properties,
            JsonObject schema, int index, boolean valid) {
        if (schema.get("additionalItems").isJsonPrimitive() && schema
                .get("additionalItems").getAsJsonPrimitive().isBoolean()) {
            if (!schema.get("additionalItems").getAsBoolean()) {
                valid = false;
            }
        } else {
            valid = typeValidator.typeValidation(properties.get(index),
                    schema.get("additionalItems").getAsJsonObject(), valid);
        }
        return valid;
    }

    @Override
    public boolean validate(JsonElement array, JsonObject schema) {

        if (array.isJsonArray()) {
            JsonArray properties = array.getAsJsonArray();
            // adheres to section 5.3.1 of Json schema validation for items and
            // additionalItems
            if (schema.has("items")) {
                if (schema.get("items").isJsonArray()) {
                    this.validItemsArray(properties, schema);
                }
                if (schema.get("items").isJsonObject()) {
                    this.validItemsObject(properties, schema);
                }
            }
            // adheres to section 5.3.2 of Json schema validation for maxItems
            if (schema.has("maxItems") && (properties.size() > schema.get("maxItems").getAsInt())) {
                valid = false;
            }
            // adheres to section 5.3.3 of Json schema validation for minItems
            if (schema.has("minItems") && (properties.size() < schema.get("minItems").getAsInt())) {
                valid = false;
            }
            // adheres to section 5.3.4 of Json schema validation for
            // uniqueItems
            if (schema.has("type") && valid) {
                valid = typeValidator.typeValidation(array, schema, valid);
            }
            if (schema.has("enum") && valid) {
                this.validEnum(array, schema);
            }
            if (schema.has("allOf") && valid) {
                this.allOf(array, schema);
            }
            if (schema.has("anyOf") && valid) {
                this.anyOf(array, schema);
            }
            if (schema.has("oneOf") && valid) {
                this.oneOf(array, schema);
            }
            if (schema.has("not") && valid) {
                this.not(array, schema);
            }

        }

        boolean result = this.valid;
        this.valid = true;

        return result;
    }
}
