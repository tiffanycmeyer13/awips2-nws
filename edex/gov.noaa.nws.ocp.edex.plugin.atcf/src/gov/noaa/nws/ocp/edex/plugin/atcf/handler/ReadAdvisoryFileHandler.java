/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ReadAdvisoryFileRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.AtcfFileUtil;

/**
 * Handler to write an advisory file into its storage directory defined in
 * atcfenv.properties and default is "/awips2/edex/data/atcf/nhc_messages".
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 02, 2020 82721      jwu         Initial creation
 * Nov 12, 2020 84446      dfriedman   Refactor error handling
 *
 * </pre>
 *
 * @author jwu
 *
 */
public class ReadAdvisoryFileHandler
        implements IRequestHandler<ReadAdvisoryFileRequest> {

    @Override
    public List<String> handleRequest(ReadAdvisoryFileRequest request)
            throws Exception {

        String fileName = request.getStormId() + request.getFileExtention();

        return AtcfFileUtil.readAdvInfoFile(fileName);
    }
}