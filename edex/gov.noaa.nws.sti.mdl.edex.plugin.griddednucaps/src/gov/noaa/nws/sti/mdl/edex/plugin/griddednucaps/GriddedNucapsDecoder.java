package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Instanceable class to be placed in the camel pipeline to create a python
 * decoder and pass back the records created by the decoder.
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
public class GriddedNucapsDecoder {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GriddedNucapsDecoder.class);

    private GriddedNucapsPythonDecoder decoder = new GriddedNucapsPythonDecoder();

    /**
     * Create a Python Decoder and provide the list of files in a comma
     * seperated string, and pass back the returned Record objects.
     *
     * @param files
     *            the files to be processed
     * @return the plugin data object[] an array of GridRecord Objects.
     * @throws Exception
     *             the exception
     */
    public PluginDataObject[] decode(String files) throws Exception {
        PluginDataObject[] records = decoder.decode(files);
        return records;

    }

}
