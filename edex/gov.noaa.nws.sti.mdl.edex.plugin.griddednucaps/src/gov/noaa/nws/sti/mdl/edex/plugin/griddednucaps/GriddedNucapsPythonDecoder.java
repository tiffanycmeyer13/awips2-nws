package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.grid.GridRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.python.decoder.PythonDecoder;

import jep.NDArray;

/**
 * Java interface to python GriddedNucapsDecoder
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- -----------------
 * Oct 4, 2018   DCS-18691 jburks  Initial creation
 *
 * </pre>
 *
 * @author jburks
 */
public class GriddedNucapsPythonDecoder extends PythonDecoder {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GriddedNucapsPythonDecoder.class);

    /**
     * Instantiates a new gridded nucaps python decoder.
     */
    public GriddedNucapsPythonDecoder() {
        super();
        setPluginName("griddednucaps");
        setPluginFQN("gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps");
        setModuleName("GriddedNucapsDecoder");
        setRecordClassname(GridRecord.class.toString());
        setCache(false);

    }

    /**
     * Call the actual python decoder and cast the message data in the record
     * class so that it can be propagated through the GridDAO.
     *
     * @param files
     *            the comma seperated list of nucaps files to decode
     * @return the plugin data object[] array of GridRecords
     * @throws Exception
     *             the exception
     */
    public PluginDataObject[] decode(String files) throws Exception {
        Map<String, Object> argMap = new HashMap<String, Object>(1);

        argMap.put("files", files);
        try {
            PluginDataObject[] pdos = decode(argMap);
            GridRecord[] records = new GridRecord[pdos.length];
            for (int i = 0; i < pdos.length; i++) {
                records[i] = (GridRecord) pdos[i];
                if (records[i].getMessageData() instanceof NDArray) {
                    records[i].setMessageData(
                            ((NDArray<?>) records[i].getMessageData())
                                    .getData());
                }
            }

            return records;
        } catch (Exception e) {
            throw new Exception("Failed to decode file: [" + files + "]", e);
        }
    }

}
