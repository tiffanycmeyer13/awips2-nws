/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.handler;

import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.StringDataRecord;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import com.raytheon.uf.edex.database.plugin.PluginFactory;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataRecord;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.RetrievePSHDataRequest;
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
 * Mar 06, 2018 #47069     wpaintsil   Revise handler for the removal 
 *                                     of the dataURI column.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class RetrievePSHDataHandler
        implements IRequestHandler<RetrievePSHDataRequest> {

    private final SingleTypeJAXBManager<PshData> jaxb = SingleTypeJAXBManager
            .createWithoutException(PshData.class);

    @Override
    public Object handleRequest(RetrievePSHDataRequest request)
            throws Exception {

        IDataRecord records = null;

        try {
            StormDataRecord record = PshEdexUtil.getStormDataRecord(
                    request.getBasin(), request.getYear(),
                    request.getStormName());

            PluginDao dao = PluginFactory.getInstance()
                    .getPluginDao(StormDataRecord.pluginName);

            IDataStore dataStore = dao.getDataStore(record);

            records = dataStore.retrieve(record.getDataURI(),
                    StormDataRecord.STORMDATA_XML, Request.ALL);

        } catch (Exception e) {
            throw new Exception("Could not retrieve PSH Data request", e);
        }

        /*
         * For a retrieve by given Basin and year and storm Name Only one XML
         * record should be returned
         */
        String[] stormDataXML = ((StringDataRecord) records).getStringData();

        if (stormDataXML != null && stormDataXML.length > 0) {

            // JAXB unmarshal back to PshData object
            return jaxb.unmarshalFromXml(stormDataXML[0]);
        }

        return null;
    }

}
