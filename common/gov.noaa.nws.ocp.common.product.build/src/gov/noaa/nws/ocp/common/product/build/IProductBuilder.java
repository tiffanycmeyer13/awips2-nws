/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An interface for building text from JSON objects.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2020 71720       pwang      Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public interface IProductBuilder {

    /**
     * Build data with a String
     *
     * @param data
     * @param validate
     * @return
     * @throws Exception
     */
    String build(String data, boolean validate) throws ProductBuildException;

    /**
     * Build data with a JsonNode
     *
     * @param data
     * @param validate
     * @return
     * @throws Exception
     */
    String build(JsonNode data, boolean validate) throws ProductBuildException;

}