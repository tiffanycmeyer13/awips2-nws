/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.DataAccessLayerException;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.PrepareComputeRequest;

/**
 * Request handler for PrepareComputeRequest.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Nov 19, 2019 70253      mporricelli  Initial creation
 * Aug 11, 2020 76541      mporricelli  Get directory defs from
 *                                      properties file
 *
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */

@DynamicSerialize
public class PrepareComputeHandler
        implements IRequestHandler<PrepareComputeRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PrepareComputeHandler.class);

    @Override
    public String handleRequest(PrepareComputeRequest request)
            throws Exception {

        AtcfEnvironmentConfig envConfig = AtcfConfigurationManager
                .getEnvConfig();

        String atcfStrmsDir = envConfig.getAtcfstrms();

        String atcfTmpDir = envConfig.getAtcftmp();

        List<ADeckRecord> currentRecords = request.getCurrentADeckRecords();

        Storm storm = request.getStorm();

        String stormComFile = atcfStrmsDir + File.separator
                + storm.getStormId().toLowerCase() + ".com";

        // Check if the directory to store temporary files exists.
        File tmpDir = new File(atcfTmpDir);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                throw new DataAccessLayerException(
                        "Failed to create directory: " + atcfTmpDir);
            }
        }

        File tmpComFile = null;
        try {
            tmpComFile = File.createTempFile(stormComFile, null, tmpDir);
        } catch (IOException e) {
            throw new DataAccessLayerException(
                    "Failed to create file: " + stormComFile, e);
        }

        // Write a-deck OFCL and CARQ data out to CSV .com file
        try (BufferedWriter fos = Files
                .newBufferedWriter(tmpComFile.toPath())) {
            Iterator<ADeckRecord> iter = currentRecords.iterator();
            while (iter.hasNext()) {
                fos.write(iter.next().toADeckString());
                fos.newLine();
            }
        } catch (IOException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error writing data to temporary compute file "
                            + tmpComFile,
                    e);
            return "Error writing data to temporary compute file " + tmpComFile;
        } finally {
            try {
                Files.move(tmpComFile.toPath(), Paths.get(stormComFile),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error moving " + tmpComFile + "to " + stormComFile, e);
            }

        }
        return "Success";
    }

}