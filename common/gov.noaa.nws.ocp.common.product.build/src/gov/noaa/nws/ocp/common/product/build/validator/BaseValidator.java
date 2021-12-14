/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * TODO Add Description
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

public abstract class BaseValidator implements IJsonValidator {

    protected boolean valid;

    protected TypeValidator typeValidator;

    public BaseValidator() {
        this.typeValidator = new TypeValidator();
    }

    /**
     * @param json
     * @param schema
     */
    protected void validEnum(JsonElement json, JsonObject schema) {
        for (JsonElement enumReq : schema.get("enum").getAsJsonArray()) {
            if (enumReq.equals(json)) {
                valid = true;
                return;
            }
        }
        valid = false;
    }

    /**
     * @param json
     * @param schema
     */
    protected void allOf(JsonElement json, JsonObject schema) {
        for (JsonElement req : schema.get("allOf").getAsJsonArray()) {
            if (!this.validate(json, req.getAsJsonObject())) {
                this.valid = false;
                return;
            }
        }
    }

    /**
     * @param json
     * @param schema
     */
    protected void anyOf(JsonElement json, JsonObject schema) {
        for (JsonElement req : schema.get("anyOf").getAsJsonArray()) {
            if (this.validate(json, req.getAsJsonObject())) {
                this.valid = true;
                return;
            }
        }
        this.valid = false;

    }

    /**
     * @param json
     * @param schema
     */
    protected void oneOf(JsonElement json, JsonObject schema) {
        int count = 0;

        for (JsonElement req : schema.get("oneOf").getAsJsonArray()) {
            if (this.validate(json, req.getAsJsonObject())) {
                count++;
            }
            if (count > 1) {
                break;
            }
        }
        this.valid = count == 1;
    }

    /**
     * @param json
     * @param schema
     */
    protected void not(JsonElement json, JsonObject schema) {
        valid = !this.validate(json, schema.get("not").getAsJsonObject());
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }
}
