/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import com.raytheon.uf.common.dataplugin.persist.DefaultPathProvider;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;

/**
 * Path provider for storing psh data to HDF5
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jan 25, 2018 #45125      wpaintsil   Initial Creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1
 */
public class PshPathProvider extends DefaultPathProvider {

    private static PshPathProvider instance = new PshPathProvider();

    public static PshPathProvider getInstance() {
        return instance;
    }

    protected PshPathProvider() {

    }

    @Override
    public String getHDFFileName(String pluginName, IPersistable persistable) {
        if (persistable == null) {
            throw new IllegalArgumentException(
                    "Expected argument persistable is null");
        }

        if (!(persistable instanceof StormDataRecord)) {
            throw new IllegalArgumentException(
                    "Argument persistable is of wrong type. Expected "
                            + StormDataRecord.class + " but got "
                            + persistable.getClass());
        }

        if (pluginName == null) {
            throw new IllegalArgumentException(
                    "Expected argument pluginName not set on object "
                            + persistable.toString());
        }

        StormDataRecord pdo = (StormDataRecord) persistable;

        StringBuilder sb = new StringBuilder();
        sb.append(pdo.getPluginName()).append("-").append(pdo.getStormName())
                .append(".h5");

        return sb.toString();
    }
}
