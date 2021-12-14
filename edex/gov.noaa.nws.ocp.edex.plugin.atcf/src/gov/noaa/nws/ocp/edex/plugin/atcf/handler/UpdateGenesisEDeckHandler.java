/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisEDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.UpdateGenesisEDeckRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * Handler to receive update requests for the genesisedeck table
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 11, 2021 #91551      jnengel     initial creation.
 *
 * </pre>
 *
 * @author jnengel
 *
 */

public class UpdateGenesisEDeckHandler
        implements IRequestHandler<UpdateGenesisEDeckRequest> {

    @Override
    public Boolean handleRequest(UpdateGenesisEDeckRequest request)
            throws Exception {
        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new AtcfException(
                    "UpdateGenesisEDeckHandler - failed to create AtcfProcessDao object: ",
                    e);
        }

        List<GenesisEDeckRecord> mrecs = new ArrayList<>();
        for (ModifiedDeckRecord mdr : request
                .getModifiedGenesisEDeckRecords()) {
            mrecs.add((GenesisEDeckRecord) mdr.getRecord());
        }

        return dao.updateGenesisEDeck(mrecs);

    }

}
