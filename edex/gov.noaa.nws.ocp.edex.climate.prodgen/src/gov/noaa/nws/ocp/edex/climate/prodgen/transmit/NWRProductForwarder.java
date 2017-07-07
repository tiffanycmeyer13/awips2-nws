/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.transmit;

import java.io.File;
import java.io.IOException;

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
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public final class NWRProductForwarder extends ClimateProductNWRSender {

    /** The logger */
    public static final IUFStatusHandler logger = UFStatus
            .getHandler(NWRProductForwarder.class);

    private static final String DEFAULT_FXA_DATA_ROOT = "/data/fxa/";

    public String destDirectory;

    /**
     * Empty Constructor
     */
    public NWRProductForwarder() {
        String fxaData = System.getenv("FXA_DATA");
        if (fxaData == null || fxaData.isEmpty()) {
            fxaData = DEFAULT_FXA_DATA_ROOT;
        }
        this.destDirectory = DEFAULT_FXA_DATA_ROOT + "workFiles/nwr/pending/";

        logger.info("NWR pending dir = " + this.destDirectory);

        dao = new ClimateProdSendRecordDAO();
    }

    /**
     * Constructor
     * 
     * @param targetDir
     */
    public NWRProductForwarder(String targetDir) {
        String fxaData = System.getenv("FXA_DATA");
        if (fxaData == null || fxaData.isEmpty()) {
            fxaData = DEFAULT_FXA_DATA_ROOT;
        }
        this.destDirectory = DEFAULT_FXA_DATA_ROOT + "workFiles/nwr/"
                + targetDir.toLowerCase() + "/";

        logger.info("Target NWRWave dir = " + this.destDirectory);

        dao = new ClimateProdSendRecordDAO();
    }

    /**
     * Forward session's product set to NWR Waves.
     * 
     * @param nwrProds
     * @param res
     */
    public static void forwardToNWR(ClimateProdGenerateSession session,
            ClimateProductSet nwrProdSet, SendClimateProductsResponse res,
            String targetNWRDir, String user) {

        NWRProductForwarder trans = new NWRProductForwarder(targetNWRDir);

        // Check if the bmhStagingDirectory exists
        File destPath = new File(trans.getDestDirectory());

        if (!destPath.exists() || !destPath.isDirectory()) {
            String msg = "NWR Pending Directory " + trans.getDestDirectory()
                    + " is not accessible";

            res.setSetLevelStatus(ProductSetStatus.FATAL_ERROR, msg);
            session.sendAlertVizMessage(Priority.PROBLEM, msg, null);
            logger.error(msg);
            return;
        }

        for (String fileName : nwrProdSet.getUnsentProducts().keySet()) {
            ClimateProduct cp = nwrProdSet.getUnsentProducts().get(fileName);

            try {
                if (trans.verifyHeader(cp)) {

                    // Save prod to NWR peding
                    trans.writeProductToFile(trans.destDirectory, cp.getName(),
                            cp.getProdText());

                    // insert a record into DB
                    trans.recordSentNWRProduct(fileName, cp, user);

                    cp.setStatus(ProductStatus.SENT);

                    logger.info("NWR prod copied to " + destPath
                            + ", a record is inserted in the DB ");
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
                String errorMsg = "Fialed to forward NWR product " + fileName
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

    /**
     * @param destDirectory
     *            the destDirectory to set
     */
    public void setDestDirectory(String destDirectory) {
        this.destDirectory = destDirectory;
    }

}
