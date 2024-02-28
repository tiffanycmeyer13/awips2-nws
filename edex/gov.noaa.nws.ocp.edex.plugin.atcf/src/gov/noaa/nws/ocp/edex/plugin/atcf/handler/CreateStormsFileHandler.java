/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CreateStormsFileRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.AtcfFileUtil;

/**
 * Create the CSV-formatted storms.txt file
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
public class CreateStormsFileHandler
        implements IRequestHandler<CreateStormsFileRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CreateStormsFileHandler.class);

    @Override
    public Boolean handleRequest(CreateStormsFileRequest request)
            throws Exception {
        try {
            return AtcfFileUtil.createStormListFile(request.getStormsRecs(),
                    request.getStormsFileFullPath());

        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Writing of storm list file "
                    + request.getStormsFileFullPath() + " failed.", e);
            return false;
        }

    }

}
