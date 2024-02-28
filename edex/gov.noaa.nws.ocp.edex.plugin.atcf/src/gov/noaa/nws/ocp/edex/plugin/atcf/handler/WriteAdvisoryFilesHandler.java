/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.WriteAdvisoryFilesRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.AtcfFileUtil;

/**
 * Handler to write a list of advisory files into the directory defined in
 * atcfenv.properties and default is "/awips2/edex/data/atcf/nhc_messages".
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 19, 2020 82721      jwu         Initial creation
 * Nov 12, 2020 84446      dfriedman   Refactor error handling
 *
 * </pre>
 *
 * @author jwu
 *
 */
public class WriteAdvisoryFilesHandler
        implements IRequestHandler<WriteAdvisoryFilesRequest> {

    @Override
    public Void handleRequest(WriteAdvisoryFilesRequest request)
            throws Exception {

        AtcfFileUtil.writeAdvisoryFiles(request.getStormId(),
                request.getContents());
        return null;
    }
}