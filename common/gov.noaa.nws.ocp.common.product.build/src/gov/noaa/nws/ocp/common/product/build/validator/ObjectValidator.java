/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.validator;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Validate a JSON object.
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

public class ObjectValidator extends BaseValidator {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ObjectValidator.class);

    private PrimitiveValidator primitiveValidator;

    private ArrayValidator arrayValidator;

    private NullValidator nullValidator;

    public ObjectValidator() {
        super();
        this.primitiveValidator = new PrimitiveValidator();
        this.arrayValidator = new ArrayValidator();
        this.nullValidator = new NullValidator();
        this.valid = true;
    }

    /**
     * Takes in a jsonElement to be validated for a json Object against
     * maxProperties, minProperties, required, properties and
     * additionalProperties
     *
     * @param object
     *            a jsonElement to be validated
     * @param schema
     *            a schema containing the above mentioned keywords
     */
    public void validateObject(JsonElement object, JsonObject schema) {

        JsonObject jsonObject = object.getAsJsonObject();
        // adheres to section 5.4.1 of Json schema validation for maxProperties
        if (schema.has("maxProperties")) {
            Integer maxProperty = schema.get("maxProperties").getAsInt();
            Set<Map.Entry<String, JsonElement>> entry = jsonObject.entrySet();
            if (entry.size() > maxProperty) {
                valid = false;
            }
        }
        // adheres to section 5.4.2 of Json schema validation for minProperties
        if (schema.has("minProperties")) {
            Integer maxProperty = schema.get("minProperties").getAsInt();
            Set<Map.Entry<String, JsonElement>> entry = jsonObject.entrySet();
            if (entry.size() < maxProperty) {
                valid = false;
            }
        }

        if (schema.has("required")) {
            JsonArray requiredProperties = schema.get("required")
                    .getAsJsonArray();
            for (int i = 0; i < requiredProperties.size(); i++) {
                if (!(jsonObject
                        .has(requiredProperties.get(i).getAsString()))) {
                    valid = false;
                }
            }
        }
        if (schema.has("properties")) {
            for (Map.Entry<String, JsonElement> schemaEntry : schema
                    .get("properties").getAsJsonObject().entrySet()) {
                if (!valid) {
                    break;
                }
                if (schemaEntry.getValue().isJsonObject()
                        && jsonObject.has(schemaEntry.getKey())) {
                    valid = this.validate(jsonObject.get(schemaEntry.getKey()),
                            schemaEntry.getValue().getAsJsonObject());
                }
            }
        }

        // adheres to section 5.4.4 of Json schema validation for
        // additionalProperties

    }

    @Override
    protected void validEnum(JsonElement object, JsonObject schema) {
        JsonObject jsonObject = object.getAsJsonObject();
        // adheres to section 5.5.1 of Json schema validation for enum
        for (JsonElement enumReq : schema.get("enum").getAsJsonArray()) {
            if (enumReq.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : jsonObject
                        .entrySet()) {
                    for (Map.Entry<String, JsonElement> schemaEntry : enumReq
                            .getAsJsonObject().entrySet()) {
                        if (!entry.getKey().equals(schemaEntry.getKey())) {
                            valid = false;
                        }
                        if (!entry.getValue().equals(schemaEntry.getValue())) {
                            valid = false;
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

    }

    @Override
    public boolean validate(JsonElement json, JsonObject schema) {

        try {
            if (json.isJsonObject()) {
                this.validateObject(json, schema);

                if (schema.has("type") && valid) {
                    this.valid = typeValidator.typeValidation(json, schema,
                            valid);
                }
                if (schema.has("enum") && valid) {
                    this.validEnum(json, schema);
                }
                if (schema.has("allOf") && valid) {
                    this.allOf(json, schema);
                }
                if (schema.has("anyOf") && valid) {
                    this.anyOf(json, schema);
                }
                if (schema.has("oneOf") && valid) {
                    this.oneOf(json, schema);
                }
                if (schema.has("not") && valid) {
                    this.not(json, schema);
                }
            }

            nullValidator.setValid(this.valid);
            this.valid = nullValidator.validate(json, schema);

            arrayValidator.setValid(this.valid);
            this.valid = arrayValidator.validate(json, schema);

            primitiveValidator.setValid(this.valid);
            this.valid = primitiveValidator.validate(json, schema);

            boolean result = this.valid;
            this.valid = true;

            return result;
        } catch (Exception e) {
            logger.error("Failed to validate JSON Object: ", e);
            return false;
        }

    }

}
