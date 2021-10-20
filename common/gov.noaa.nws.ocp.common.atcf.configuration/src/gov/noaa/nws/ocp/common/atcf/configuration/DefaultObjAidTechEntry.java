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
 * This class represent a default objective aid technique entry in
 * default_aids.dat, which is used for displaying satellite the objective aid.
 *
 * <pre>
 *
 * The file format is as follows:
 *     The first field is the technique name.
 *     The second field is the technique description.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  FSSE  FSU Superensemble
 *  HWRF  HWRF model
 *  ....
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
public class DefaultObjAidTechEntry {

    /**
     * Name
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Description
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String description;

    /**
     * Constructor.
     */
    public DefaultObjAidTechEntry() {
    }

    /**
     * Constructor.
     * 
     * @param name
     *            obj aid tech name
     * @param description
     *            description of the tech
     */
    public DefaultObjAidTechEntry(String name, String description) {
        this.name = name;
        this.description = description;
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}