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
 * This class represent a fix site entry in fixsites.dat, which is used for
 * entering and displaying satellite fixes and for computing error statistics.
 *
 * <pre>
 *
 * The file format is as follows:
 * fix site - The specific site from which the fix comes (4 or 5 digits).
 * retired/developmental flag - 1 means don't use the fix site in normal operations.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  #SITE RETIRED
 *  TAFB 0
 *  SAB 0
 *  KNHC 0
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 #52692     jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class FixSiteEntry {

    /**
     * Name
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Retired flag
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean retired;

    /**
     * Constructor.
     */
    public FixSiteEntry() {
    }

    /**
     * Constructor.
     * 
     * @param name
     *            site name
     * @param retired
     *            If the site is retired or not
     */
    public FixSiteEntry(String name, boolean retired) {
        this.name = name;
        this.retired = retired;
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

    /**
     * @return the retired
     */
    public boolean isRetired() {
        return retired;
    }

    /**
     * @param retired
     *            the retired to set
     */
    public void setRetired(boolean retired) {
        this.retired = retired;
    }

}