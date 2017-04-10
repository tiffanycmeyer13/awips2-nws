/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class containing data returned from the database along with a flag for
 * whether the query returned a row in the database.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2016  20636      wpaintsil   Initial creation
 * </pre>
 * 
 * @author wpaintsil
 */
@DynamicSerialize
public class QueryData {

    /**
     * Flag for whether a row was returned from the database.
     */
    @DynamicSerializeElement
    private boolean exists;

    /**
     * Generic object that can contain a data object such as PeriodData or
     * DailyClimateData returned from a query.
     */
    @DynamicSerializeElement
    private Object data;

    /**
     * Default constructor. Exists flag is false by default.
     */
    public QueryData() {
        this.exists = false;
    }

    /**
     * @return true if the data exists, false otherwise.
     */
    public boolean getExists() {
        return exists;
    }

    /**
     * @param exists
     *            set to true if the data exists, false otherwise.
     */
    public void setExists(boolean exists) {
        this.exists = exists;
    }

    /**
     * @return data.
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data
     *            data to set
     */
    public void setData(Object data) {
        this.data = data;
        exists = true;
    }

}
