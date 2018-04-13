/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.handler;

import java.util.Calendar;

import com.raytheon.uf.common.datastorage.StorageStatus;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import com.raytheon.uf.edex.database.plugin.PluginFactory;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataRecord;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.SavePSHDataRequest;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.edex.psh.util.PshEdexUtil;

/**
 * RetrievePSHDataHandler
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 04, 2017            pwang       Initial creation
 * Dec 05, 2017 #41620     wpaintsil   Add export/import capability
 * Dec 11, 2017 #41998     jwu         Move export to PshEdexUtil
 * Mar 06, 2018 #47069     wpaintsil   Revise handler for the removal 
 *                                     of the dataURI column.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class SavePSHDataHandler implements IRequestHandler<SavePSHDataRequest> {

    private final SingleTypeJAXBManager<PshData> jaxb = SingleTypeJAXBManager
            .createWithoutException(PshData.class);

    private final static String PSH = "psh";

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshConfigurationManager.class);

    @Override
    public Object handleRequest(SavePSHDataRequest request) throws Exception {

        PluginDao dao = PluginFactory.getInstance().getPluginDao(PSH);

        PshData pdata = request.getPshData();

        // Retrieve the record associated with the basin/year/stormName.
        StormDataRecord rec = PshEdexUtil.getStormDataRecord(
                pdata.getBasinName(), pdata.getYear(), pdata.getStormName());
        rec.setForecaster(pdata.getForecaster());

        String xmlString = "";
        try {
            xmlString = jaxb.marshalToXml(pdata);
        } catch (Exception e1) {
            throw new Exception(
                    "SavePshDataHandler - Failed to marshal PshObject to XML "
                            + rec.getDataURI(),
                    e1);
        }

        rec.setStormDataXML(xmlString);

        // Current date time
        Calendar now = TimeUtil.newCalendar();
        rec.setInsertTime(now);

        rec.setOverwriteAllowed(true);

        StormDataRecord[] records = new StormDataRecord[1];
        records[0] = rec;
        try {
            StorageStatus status = dao.persistToHDF5(records);

            if (status.getExceptions().length > 0) {
                logger.warn(status.getExceptions().toString());

                return false;
            } else {
                dao.persistToDatabase(records);
            }
        } catch (Exception e) {
            throw new Exception(
                    "SavePshDataHandler - Failed to store " + rec.getDataURI(),
                    e);
        }

        // Export if desired.
        PshEdexUtil.exportProduct(xmlString, pdata, PshEdexUtil.PSH_XML_FILE);

        return true;
    }

}
