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
 * Handle transmit the NWR climate products to BMH staging for broadcasting
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 14, 2017 20637      pwang       Initial creation
 * May 05, 2017 20642      pwang       Re-designed status and error handling
 * Aug 4, 2017  33104      amoore      Address review comments.
 * Aug 22, 2017 37242      amoore      Better logging and field access.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public final class NWRProductTransmitter extends ClimateProductNWRSender {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(NWRProductTransmitter.class);

    // TODO: BMH_STAGING_DIR should be configurable
    private static final String DEFAULT_BMH_STAGING_DIR = "px1f:/awips2/bmh/data/nwr/ready/";

    private String bmhStagingDirectory = DEFAULT_BMH_STAGING_DIR;

    /**
     * Constructor
     */
    public NWRProductTransmitter() {
        dao = new ClimateProdSendRecordDAO();
    }

    /**
     * Constructor
     * 
     * @param bmhStagingDir
     */
    public NWRProductTransmitter(String bmhStagingDir) {
        this.bmhStagingDirectory = bmhStagingDir;
        dao = new ClimateProdSendRecordDAO();
    }

    /**
     * This static method will transmit one or more NWR product(s) to BMH
     * 
     * @param nwrProds
     * @throws Exception
     */
    public static void transmitToBMH(ClimateProdGenerateSession session,
            ClimateProductSet nwrProdSet, SendClimateProductsResponse res,
            String user) {

        NWRProductTransmitter trans = new NWRProductTransmitter();

        // Check if the bmhStagingDirectory exists
        File destPath = new File(trans.getBmhStagingDirectory());

        if (!destPath.exists() || !destPath.isDirectory()) {
            String msg = "BMH Staging Directory "
                    + trans.getBmhStagingDirectory() + " is not accessible";
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
                    // Get product text
                    String nwrText = trans.reformatTextForNWR(cp);

                    // Save
                    trans.writeAsProdFile(trans.getBmhStagingDirectory(),
                            nwrText, cp.getName());
                    // insert a record into DB
                    trans.recordSentNWRProduct(fileName, cp, user);

                    cp.setStatus(ProductStatus.SENT);

                    logger.info("NWR product " + fileName + " copied to "
                            + destPath + ", a record is inserted in the DB ");

                } else {
                    String errorMsg = "Check Header failed, NWR product "
                            + fileName + " has wrong format";
                    cp.setStatus(ProductStatus.ERROR);
                    cp.setStatusDesc(errorMsg);
                    logger.error(errorMsg);
                }
            } catch (IOException e) {
                String errorMsg = "Failed to save NWR product file " + fileName
                        + " into BMH staging directory"
                        + e.getLocalizedMessage();

                cp.setStatus(ProductStatus.ERROR);
                cp.setStatusDesc(errorMsg);
                logger.error(errorMsg);
            } catch (Exception e2) {
                String errorMsg = "Fialed to transmit NWR product " + fileName
                        + " to BMH" + e2.getLocalizedMessage();

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
     * @return the bmhStagingDirectory
     */
    public String getBmhStagingDirectory() {
        return bmhStagingDirectory;
    }

    /**
     * @param bmhStagingDirectory
     *            the bmhStagingDirectory to set
     */
    public void setBmhStagingDirectory(String bmhStagingDirectory) {
        this.bmhStagingDirectory = bmhStagingDirectory;
    }

}
