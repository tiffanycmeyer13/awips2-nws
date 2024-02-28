/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim;

import org.apache.commons.lang3.Validate;

import com.raytheon.uf.common.dataplugin.persist.DefaultPathProvider;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;

/**
 * Data store path provider for ODIM data
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMPathProvider extends DefaultPathProvider {

    private static ODIMPathProvider instance = new ODIMPathProvider();

    public static ODIMPathProvider getInstance() {
        return instance;
    }

    @Override
    public String getHDFFileName(String pluginName, IPersistable persistable) {
        Validate.notNull(persistable, "Argument 'persistable' is null");
        Validate.isInstanceOf(ODIMRecord.class, persistable);
        Validate.notNull(pluginName, "Argument 'pluginName' is null");

        ODIMRecord pdo = (ODIMRecord) persistable;
        StringBuilder sb = new StringBuilder(64);
        sb.append(pluginName);
        sb.append("-");
        sb.append(pdo.getNode());
        sb.append(fileNameFormat.get().format(pdo.getDataTime().getRefTime()));
        sb.append(".h5");

        return sb.toString();
    }
}
