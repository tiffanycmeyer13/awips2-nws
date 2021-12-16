/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * IJsonValidator Interface
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2020  71720      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public interface IJsonValidator {
    /**
     * takes in a json and a schema and checks if json corresponds to schema
     * 
     * @param json
     *            a JSONObject
     * @param schema
     *            a JSON representation of a schema
     * @return true if json corresponds to schema. false otherwise
     */
    boolean validate(JsonElement json, JsonObject schema);

}
