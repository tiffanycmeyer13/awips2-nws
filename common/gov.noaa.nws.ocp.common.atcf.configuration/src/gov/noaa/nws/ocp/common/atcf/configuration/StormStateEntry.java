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
 * This class represent a storm state entry, which is used to compose advisory
 * product.
 *
 * <pre>
 *  Entry format:
 *      <State id="IN" retired="false" name="Inland" description="inland"/>
 *  where:
 *      id         -  Unique identifier
 *      retired     - Flag to indicate if the entry should be shown in GUI. 
 *                   false: show in GUI; true - do not show in GUI
 *      name        - Name shown in GUI
 *      description - Description shown in an advisory
 *
 *  Note:
 *      1. The entries in stormstates.xml are taking from nhc_writeadv.f.
 *      2. The "retired" flag is set to "true" for entries that does NOT
 *         appear in Advisory Composition => Forecast Type dialog. Otherwise,
 *         it is set to "false".
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2020 82721      jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class StormStateEntry {

    /**
     * ID - unique identifier
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String id;

    /**
     * Retired flag - false: show in GUI; true - do not show in GUI
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean retired;

    /**
     * Name shown in GUI
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Description shown in advisory
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String description;

    /**
     * Constructor.
     */
    public StormStateEntry() {
    }

    /**
     * Constructor.
     *
     * @param id
     *            Entry id
     * @param retired
     *            If the site is retired or not
     * @param name
     *            Entry name
     * @param description
     *            Entry description
     */
    public StormStateEntry(String id, boolean retired, String name,
            String description) {
        this.id = id;
        this.retired = retired;
        this.name = name;
        this.description = description;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
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