/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.product.build.validator.ObjectValidator;

/**
 * DataValidation Utility
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 2, 2021 #          awips     Initial creation
 *
 * </pre>
 *
 * @author awips
 * @version 1.0
 */

public class DataValidation {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DataValidation.class);

    public static boolean validateDataAgainstSchema(Path schemaFile,
            JsonElement data) {
        Gson gson = new Gson();
        JsonObject jschema = null;
        try (BufferedReader br = Files.newBufferedReader(schemaFile,
                StandardCharsets.UTF_8)) {
            Type type = new TypeToken<JsonObject>() {
            }.getType();
            jschema = gson.fromJson(br, type);
        } catch (IOException e) {
            logger.error("Failed to access the schema file: "
                    + schemaFile.getFileName() + " with excption " + e);
        }

        if (jschema == null) {
            return false;
        }
        /* validate input data */
        ObjectValidator v = new ObjectValidator();
        return v.validate(data, jschema);
    }

    public static boolean validateDataAgainstSchema(String schemaLoacation,
            String schemaFilename, String data) {
        // Get defined JSON schema for validation
        Path schemaPath = Paths.get(schemaLoacation, schemaFilename);

        // convert the stringified JSON dataset to a JsonElement object
        Gson gson = new Gson();
        Type type = new TypeToken<JsonElement>() {
        }.getType();

        JsonElement vdata = gson.fromJson(data, type);
        return validateDataAgainstSchema(schemaPath, vdata);
    }

}
