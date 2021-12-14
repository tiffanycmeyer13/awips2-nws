/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * Request to import fixes for a storm
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 14, 2020 81449      jwu     Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class ImportFixesRequest implements IServerRequest {

    @DynamicSerializeElement
    private Storm storm;

    @DynamicSerializeElement
    private String fixType;

    /*
     * Flag to allow importing "fBBccYYYY.dat" file, where "BB" is basin, "cc"
     * is storm number, and "YYYY" is storm year. Default as true.
     */
    @DynamicSerializeElement
    private boolean acceptDatFile = true;

    /**
     * Constructor
     */
    public ImportFixesRequest() {
    }

    /**
     * Constructor
     * 
     * @param storm
     *            Storm
     * @param type
     *            fix type
     */
    public ImportFixesRequest(Storm storm, String type) {
        this.storm = storm;
        this.fixType = type;
    }

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    /**
     * @return the fixType
     */
    public String getFixType() {
        return fixType;
    }

    /**
     * @param fixType
     *            the fixType to set
     */
    public void setFixType(String fixType) {
        this.fixType = fixType;
    }

    /**
     * @return the acceptDatFile
     */
    public boolean isAcceptDatFile() {
        return acceptDatFile;
    }

    /**
     * @param acceptDatFile
     *            the acceptDatFile to set
     */
    public void setAcceptDatFile(boolean acceptDatFile) {
        this.acceptDatFile = acceptDatFile;
    }

}