/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.transmit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.raytheon.uf.common.util.FileUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdSendRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateProdSendRecordDAO;

/**
 * Handle transmission of the NWR climate products to BMH for broadcasting
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 14, 2017  20637     pwang       Initial creation
 * May 05, 2017  34790     pwang       Re-designed status and error handling
 * Jul 26, 2017  33104     amoore      Better logging.
 * Aug 22, 2017  37242     amoore      Better pathing for writing files. Use File
 *                                     constructors for pathing rather than string
 *                                     concatenation.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public abstract class ClimateProductNWRSender {

    public static final Pattern BEGIN_PATTERN = Pattern
            .compile("^\\x1b\\x61.*");

    public static final Pattern END_PATTERN = Pattern.compile(".*\\x1b\\x62$");

    protected ClimateProdSendRecordDAO dao;

    /**
     * Empty Constructor
     */
    public ClimateProductNWRSender() {
    }

    /**
     * Check if the NWR product text are correctly formatted
     * 
     * @param prod
     * @return
     * @throws Exception
     */
    public boolean verifyHeader(final ClimateProduct prod) throws Exception {
        boolean foundBeginPattern = false;
        boolean foundEndPattern = false;

        if (prod.getProdText() == null || prod.getProdText().isEmpty()) {
            throw new Exception("Null or empty product text for product: ["
                    + prod.getName() + "]");
        }
        String[] lines = prod.getProdText().split("\\r?\\n");

        /*
         * NWR text should have a head and tail special characters
         */
        for (String line : lines) {
            if (BEGIN_PATTERN.matcher(line).matches()) {
                foundBeginPattern = true;
            }
            if (END_PATTERN.matcher(line).matches()) {
                foundEndPattern = true;
            }
        }

        if (!foundBeginPattern && !foundEndPattern) {
            throw new Exception(
                    "Missing begin and end pattern characters in product: ["
                            + prod.getProdText() + "]");
        } else if (!foundBeginPattern) {
            throw new Exception("Missing begin pattern characters in product: ["
                    + prod.getProdText() + "]");
        } else if (!foundEndPattern) {
            throw new Exception("Missing end pattern characters in product: ["
                    + prod.getProdText() + "]");
        }

        return true;
    }

    /**
     * reformatTextForNWR
     * 
     * 1) remove newline; 2) add "ZCZC" PIL at beginning; 3) append "NNNN" at
     * the end
     * 
     * @param prod
     * @return
     */
    public String reformatTextForNWR(final ClimateProduct prod) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZCZC").append(prod.getPil());

        String nwrTextNoNewline = prod.getProdText().replace("\n", "");
        sb.append(nwrTextNoNewline);
        sb.append("NNNN");

        return sb.toString();
    }

    /**
     * writeAsProdFile
     * 
     * @param nwrProdText
     * @param fileName
     * @throws IOException
     */
    public void writeAsProdFile(String destPath, String nwrProdText,
            String fileName) throws IOException {
        Files.write(Paths.get(destPath, fileName), nwrProdText.getBytes(),
                StandardOpenOption.WRITE);
    }

    /**
     * Write out product data to file.
     * 
     * @param destDirectory
     * @param prodID
     * @param product
     * @throws Exception
     */
    public void writeProductToFile(String destDirectory, String prodID,
            String product) throws Exception {
        File fl = new File(destDirectory, prodID);
        FileUtil.bytes2File(product.getBytes(), fl, false);
    }

    /**
     * Record the sent product.
     * 
     * @param fileName
     * @param prod
     * @throws Exception
     */
    public void recordSentNWRProduct(String fileName, ClimateProduct prod,
            String user) throws Exception {
        ClimateProdSendRecord rec = new ClimateProdSendRecord();
        rec.setProd_id(prod.getPil());
        rec.setPeriod_type(prod.getProdType().name());
        rec.setProd_type("NWR");
        rec.setFile_name(fileName);
        rec.setProd_text(prod.getProdText());

        LocalDateTime sendTime = LocalDateTime.now();
        rec.setSend_time(Timestamp.valueOf(sendTime));
        rec.setUser_id(user);

        // Save a record into the database
        if (dao == null) {
            String expMsg = "DAO is null, can't insert the record: "
                    + rec.getProd_id() + " into DB";
            throw new Exception(expMsg);
        }

        try {
            dao.insertSentClimateProdRecord(rec);
        } catch (ClimateQueryException e) {
            String expMsg = "Insert the record: " + rec.getProd_id()
                    + " failed";
            throw new Exception(expMsg, e);
        }
    }

}
