/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshProductTransmitRequest;
import gov.noaa.nws.ocp.edex.psh.transmitter.PshProductTransmitter;

/**
 * Handle request to transmit a PSH product.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2017 #36930     jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class PshProductTransmitHandler
        implements IRequestHandler<PshProductTransmitRequest> {

    @Override
    public Object handleRequest(PshProductTransmitRequest request)
            throws Exception {

        PshProductTransmitter sender = new PshProductTransmitter();

        return sender.transmit(request.getProduct(), request.isOperational());
    }

}