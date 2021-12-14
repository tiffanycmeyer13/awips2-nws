/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.RollbackMergedDeckRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.decoder.AtcfDeckProcessor;

/**
 * MergeDeckHandler Return the DeckMergeLog id
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 4, 2020  #78298     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class RollbackMergedDeckHandler
        implements IRequestHandler<RollbackMergedDeckRequest> {

    @Override
    public Boolean handleRequest(RollbackMergedDeckRequest request)
            throws Exception {

        AtcfDeckProcessor decoder = new AtcfDeckProcessor();

        try {
            return decoder.rollbackMergedDeck(request.getDeckType(),
                    request.getDeckMergeLogId());

        } catch (Exception de) {
            throw new Exception("Rollback merged "
                    + request.getDeckType().getValue().toUpperCase()
                    + " deck failed", de);
        }

    }

}
