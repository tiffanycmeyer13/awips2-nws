/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * NullValidator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2020 71720       pwang      Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class NullValidator extends BaseValidator {

    public NullValidator() {
        super();
        this.valid = true;
    }

    @Override
    public boolean validate(JsonElement json, JsonObject schema) {
        /*
         * instead of writing in the PrimitiveValidator class, it made more
         * sense to place this in a class of it's own for readability since it
         * has a validation specific to it
         */
        if (json.isJsonNull()) {
            if (schema.has("enum")) {
                this.validEnum(json, schema);
            }
            if (schema.has("type")) {
                valid = typeValidator.typeValidation(json, schema, valid);
            }
        }

        boolean result = this.valid;
        this.valid = true;

        return result;

    }
}
