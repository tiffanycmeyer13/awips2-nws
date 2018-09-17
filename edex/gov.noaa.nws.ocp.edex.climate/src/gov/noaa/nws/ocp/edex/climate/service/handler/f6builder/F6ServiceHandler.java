/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.f6builder;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.F6ServiceRequest;
import gov.noaa.nws.ocp.edex.climate.f6builder.F6Builder;

/**
 * Handle an F6 request.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 3, 2015             xzhang      Initial creation
 * 22 FEB 2017  28609      amoore      Address TODOs. Fix comments.
 * 10 MAR 2017  30130      amoore      F6 should not keep output in awips directory, and should
 *                                     delete after printing. Send to textDB on EDEX side, not VIZ.
 * 28 AUG 2018  DR 20861  dfriedman    Add option to enable transmission of products.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

public class F6ServiceHandler implements IRequestHandler<F6ServiceRequest> {

    @Override
    public Object handleRequest(F6ServiceRequest request) throws Exception {
        return new F6Builder().buildF6ForStations(request.getStations(),
                request.getAdate(), request.getRemarks(), request.isPrint(),
                request.isTransmit(), request.isOperational());
    }
}
