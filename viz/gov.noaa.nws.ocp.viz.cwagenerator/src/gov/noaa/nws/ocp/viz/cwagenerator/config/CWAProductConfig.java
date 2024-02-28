/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * class for CWA configuration list
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/12/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "CWAProductConfigXML")
@XmlAccessorType(XmlAccessType.NONE)
public class CWAProductConfig {
    @XmlElements({
            @XmlElement(name = "cwaProducts", type = AbstractCWAConfig.class) })
    private List<AbstractCWAConfig> cwaProducts;

    public CWAProductConfig() {
    }

    public List<AbstractCWAConfig> getCwaProducts() {
        return cwaProducts;
    }

    public void setCwaElementList(List<AbstractCWAConfig> cwaProducts) {
        this.cwaProducts = cwaProducts;
    }

}
