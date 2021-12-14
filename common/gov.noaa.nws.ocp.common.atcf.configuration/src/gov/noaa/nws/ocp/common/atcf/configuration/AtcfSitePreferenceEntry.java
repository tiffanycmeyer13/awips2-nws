/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences.PreferenceOptions;

/**
 * ATCF site preference file, the user could could copy the legacy site
 * preference file such as atcfsite.nam as atcfsite.prefs to be picked up in A2
 * ATCF
 *
 * <pre>
 *
 * The file format is as follows:
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 * forecast-center:        OFCL
 * station-code:           KNHC
 * forecast-ctr(old):      OFCO
 * ddn-code:               ATCM
 * center-name:            NOT USED FOR NHC
 * map-area:               AREAOFOPERAT
 * editor:                 kwrite -graphicssystem raster
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 28, 2019 59913      dmanzella   Created
 * Apr 04, 2019 62029      jwu         Add constructor with PreferenceOptions.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfSitePreferenceEntry {

    /**
     * Name for a preference entry.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Value for a preference entry.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String value;

    /**
     * Constructor.
     */
    public AtcfSitePreferenceEntry() {
    }

    /**
     * Constructor.
     *
     * @param name
     *            Type name
     * @param value
     *            Type value
     */
    public AtcfSitePreferenceEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param po
     *            PreferenceOptions
     */
    public AtcfSitePreferenceEntry(PreferenceOptions po) {
        this.name = po.getName();
        this.value = po.getDefault();
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
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}