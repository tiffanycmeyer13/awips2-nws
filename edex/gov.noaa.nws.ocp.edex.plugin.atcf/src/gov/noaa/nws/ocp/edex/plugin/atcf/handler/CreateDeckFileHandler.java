/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CreateDeckFileRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.AtcfFileUtil;

/**
 * Create the CSV-formatted deck file (e.g. aal201711.dat) for A, B, E, F or fst
 * file
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2020 79366      mporricelli Initial creation
 *
 * </pre>
 *
 * @author porricel
 *
 */
public class CreateDeckFileHandler
        implements IRequestHandler<CreateDeckFileRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateDeckFileHandler.class);

    @Override
    public Boolean handleRequest(CreateDeckFileRequest request)
            throws Exception {
        try {
            return AtcfFileUtil.createDeckFile(request.getDeckRecs(),
                    request.getDeckType(), request.getDeckFileFullPath());

        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Writing of deck file "
                    + request.getDeckFileFullPath() + " failed.", e);
            return false;
        }

    }
}
