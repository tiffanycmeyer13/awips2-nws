/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import com.raytheon.uf.edex.database.plugin.PluginFactory;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataRecord;
import gov.noaa.nws.ocp.common.localization.psh.PshBasin;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshExportType;

/**
 * Common utility values and methods for PSH edex.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 14 AUG 2017  #6930      jwu         Initial creation
 * 11 DEC 2017  #41998     jwu         Add exportProduct().
 * 07 MAR 2018  #47069     wpaintsil   Add getStormDataRecord().
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class PshEdexUtil {

    /**
     * The logger
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshEdexUtil.class);

    /**
     * Source for sending AlertViz message.
     */
    public static final String EDEX = "EDEX";

    /**
     * Category for sending AlertViz message
     */
    public static final String CATEGORY = "PSH";

    /**
     * Plugin name for sending AlertViz message
     */
    public static final String PLUGIN_ID = "PSH";

    /**
     * PSH default message type - "Routine".
     */
    public static final String MESSAGE_TYPE = "ROU";

    /**
     * PSH default route - "DEF".
     */
    public static final String DEFAULT_ROUTE = "DEF";

    /**
     * Directory/File name for PSH XML and text product, if "export" is
     * configured by the user.
     */
    public static final String PSH_PRODUCT_ROOT = "psh" + File.separator
            + "product";

    public static final String PSH_XML_FILE = "finalpsh.xml";

    public static final String PSH_TXT_FILE = "finalpsh.txt";

    /**
     * Send message to the AlertViz.
     * 
     * @param desc
     *            description about the message
     * @param msgBody
     *            formatted message contains details
     */
    public static void sendAlertVizMessage(Priority priority, String desc,
            String msgBody) {

        EDEXUtil.sendMessageAlertViz(priority, PLUGIN_ID, EDEX, CATEGORY, desc,
                msgBody, null);
    }

    /**
     * Export PSH product content to the configured directory. If any part of
     * the directory path does not exist, create it. If the file does not exist,
     * create it.
     * 
     * @param product
     *            Product content.
     * @param pdata
     *            PshData object.
     */
    public static void exportProduct(String product, PshData pdata,
            String fileName) {

        PshExportType export = PshConfigurationManager.getInstance()
                .getConfigHeader().getExportProduct();

        if (export == PshExportType.LOCALIZATION
                || export == PshExportType.USER) {

            String dir = PshConfigurationManager.getInstance().getConfigHeader()
                    .getExportDir();

            String basinDir = PshBasin.getPshBasin(pdata.getBasinName())
                    .getDirName();

            String sep = File.separator;

            String subDir = basinDir + sep + pdata.getYear() + sep
                    + pdata.getStormName();

            dir = dir + sep + PSH_PRODUCT_ROOT + sep + subDir + sep;

            Path file = Paths.get(dir + fileName);
            List<String> prdList = new ArrayList<>();
            prdList.add(product);

            try {
                Files.createDirectories(Paths.get(dir));
                Files.write(file, prdList, StandardCharsets.UTF_8);
            } catch (Exception ee) {
                logger.warn("PshEdexUtil - cannot export file:  " + dir);
            }
        }
    }

    /**
     * Get the StormDataRecord associated with the basin/year/stormName. The
     * refTime for a StormDataRecord will be the time it is first created and
     * stay as is.
     * 
     * @param basin
     * @param year
     * @param stormName
     * @return
     */
    public static StormDataRecord getStormDataRecord(String basin, int year,
            String stormName) {

        List<?> result = null;

        try {
            PluginDao dao = PluginFactory.getInstance()
                    .getPluginDao(StormDataRecord.pluginName);
            result = dao.queryByCriteria(
                    Arrays.asList(
                            new String[] { "basin", "year", "stormName" }),
                    Arrays.asList(new Object[] { basin, year, stormName }));
        } catch (PluginException | DataAccessLayerException e) {
            logger.warn("PshEdexUtil - Error retrieving PSH record", e);
        }

        if (result != null && result.size() > 0) {
            return (StormDataRecord) result.get(0);
        } else {
            StormDataRecord newRecord = new StormDataRecord(basin, year,
                    stormName);
            newRecord.setDataTime(new DataTime(TimeUtil.newCalendar()));

            return newRecord;
        }
    }

}