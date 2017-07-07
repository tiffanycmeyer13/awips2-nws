/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;

/**
 * A wrapper class for list of ClimateProductType
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2017 20637      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateProdSetting {

    @DynamicSerializeElement
    @XmlAttribute
    private List<ClimateProductType> prodSettingList;

    public ClimateProdSetting() {

    }

    public ClimateProdSetting(List<ClimateProductType> settings) {
        this.prodSettingList = settings;
    }

    /**
     * @return the prodSettingList
     */
    public List<ClimateProductType> getProdSettingList() {
        return prodSettingList;
    }

    /**
     * @param prodSettingList
     *            the prodSettingList to set
     */
    public void setProdSettingList(List<ClimateProductType> prodSettingList) {
        this.prodSettingList = prodSettingList;
    }

}
