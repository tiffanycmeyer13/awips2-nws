/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represent a full forecaster initial entry in initials_full.dat,
 * which holds all forecasters' initial and first name from all ATCF sites.
 * (NHC/CPHC/WPC).
 *
 * <pre>
 *
 * The file format is as follows:
 *
 *   MJB BRENNAN
 *   ENR RAPPAPORT
 *   RJP PASCH
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2021 86476      jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class FullInitialEntry {

    /**
     * Initial
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String initial;

    /**
     * Retired flag
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Constructor.
     */
    public FullInitialEntry() {
    }

    /**
     * Constructor.
     * 
     * @param initial
     *            Forecaster's initial
     * @param name
     *            Forecaster's first name
     */
    public FullInitialEntry(String initial, String name) {
        this.initial = initial;
        this.name = name;
    }

    /**
     * @return the initial
     */
    public String getInitial() {
        return initial;
    }

    /**
     * @param initial
     *            the initial to set
     */
    public void setInitial(String initial) {
        this.initial = initial;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
