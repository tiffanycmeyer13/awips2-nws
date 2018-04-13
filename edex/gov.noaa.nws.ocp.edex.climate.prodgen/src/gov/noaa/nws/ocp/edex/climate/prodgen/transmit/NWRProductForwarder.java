/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.transmit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct.ProductStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProductSet;
import gov.noaa.nws.ocp.common.dataplugin.climate.ProductSetStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.SendClimateProductsResponse;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateProdSendRecordDAO;

/**
 * Forwarding NWR Climate products to legacy NWRBrowser
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 25, 2017 20637      pwang       Initial creation
 * May 05, 2017 20642      pwang       Re-designed status and error handling
 * Aug 4, 2017  33104      amoore      Address review comments.
 * Aug 16, 2017 37117      amoore      Always use Legacy FXA DATA root, since
 *                                     AWIPS2 EDEX sets its own version based
 *                                     on EDEX HOME that NWR WAVES cannot and
 *                                     does not access.
 * Aug 22, 2017 37242      amoore      Better logging and field access.
 * Oct 10, 2017 39153      amoore      Less action-blocking from dissemination flag.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public final class NWRProductForwarder extends ClimateProductNWRSender {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(NWRProductForwarder.class);

    private static final String DEFAULT_FXA_DATA_ROOT = "/data/fxa/";

    private static final String DEFAULT_TARGET_DIRECTORY = "pending";

    private String destDirectory;

    /**
     * Empty Constructor. Use default directory.
     */
    public NWRProductForwarder() {
        this(DEFAULT_TARGET_DIRECTORY);
    }

    /**
     * Constructor
     * 
     * @param targetDir
     */
    public NWRProductForwarder(String targetDir) {
        /*
         * Task 37687: when NWR Waves is migrated, both NWR Waves and this code
         * should use the environment variable FXA_DATA, and fall back on
         * /data/fxa.
         */
        // String fxaData = System.getenv("FXA_DATA");
        // if (fxaData == null || fxaData.isEmpty()) {
        String fxaData = DEFAULT_FXA_DATA_ROOT;
        // }
        Path destPath = Paths.get(fxaData, "workFiles/nwr/",
                targetDir.toLowerCase());
        this.destDirectory = destPath.toAbsolutePath().toString();

        logger.debug("Target NWRWave dir = " + this.destDirectory);

        dao = new ClimateProdSendRecordDAO();
    }

    /**
     * Forward session's product set to NWR Waves.
     * 
     * @param session
     * @param nwrProdSet
     * @param res
     * @param targetNWRDir
     * @param user
     * @param disseminate
     */
    public static void forwardToNWR(ClimateProdGenerateSession session,
            ClimateProductSet nwrProdSet, SendClimateProductsResponse res,
            String targetNWRDir, String user, boolean disseminate) {
        NWRProductForwarder trans = new NWRProductForwarder(targetNWRDir);

        // Check if the bmhStagingDirectory exists
        File destPath = new File(trans.getDestDirectory());

        if (disseminate && (!destPath.exists() || !destPath.isDirectory())) {
            String msg;

            if (!destPath.exists()) {
                msg = "NWR Pending Directory " + trans.getDestDirectory()
                        + " does not exist!";
            } else {
                msg = "NWR Pending Directory " + trans.getDestDirectory()
                        + " is not a directory!";
            }

            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            ClimateProdGenerateSession.sendAlertVizMessage(Priority.PROBLEM,
                    msg, null);
            logger.error(msg);
            return;
        }

        for (String fileName : nwrProdSet.getUnsentProducts().keySet()) {
            ClimateProduct cp = nwrProdSet.getUnsentProducts().get(fileName);

            try {
                if (trans.verifyHeader(cp)) {
                    if (disseminate) {

                        // Save prod to NWR pending
                        trans.writeProductToFile(trans.getDestDirectory(),
                                cp.getName(), cp.getProdText());

                        logger.info("NWR product " + fileName + " copied to "
                                + destPath
                                + ", a record will be inserted in the DB.");
                    } else {
                        logger.info("NWR product " + fileName
                                + " will not be copied to NWR WAVES, "
                                + "as dissemination is disabled.");
                    }
                    cp.setStatus(ProductStatus.SENT);

                    // insert a record into DB
                    trans.recordSentNWRProduct(fileName, cp, user);
                } else {
                    String errorMsg = "Check Header failed, NWR product "
                            + fileName + " has wrong format";

                    cp.setStatus(ProductStatus.ERROR);
                    cp.setStatusDesc(errorMsg);
                    logger.error(errorMsg);
                }
            } catch (IOException e) {
                String errorMsg = "Failed to save NWR product file " + fileName
                        + " into NWR pending directory"
                        + e.getLocalizedMessage();

                cp.setStatus(ProductStatus.ERROR);
                cp.setStatusDesc(errorMsg);
                logger.error(errorMsg);

            } catch (Exception e2) {
                String errorMsg = "Failed to forward NWR product " + fileName
                        + " to NWR" + e2.getLocalizedMessage();

                cp.setStatus(ProductStatus.ERROR);
                cp.setStatusDesc(errorMsg);
                logger.error(errorMsg);
            }
        }
        nwrProdSet.updateSetLevelStatusFromProductStatus();
        res.setSetLevelStatus(nwrProdSet.getProdStatus(),
                nwrProdSet.getProdStatus().getDescription());
    }

    /**
     * @return the destDirectory
     */
    public String getDestDirectory() {
        return destDirectory;
    }
}
