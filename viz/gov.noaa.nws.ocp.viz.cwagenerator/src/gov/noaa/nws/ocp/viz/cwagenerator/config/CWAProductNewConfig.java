/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * class for the CWA configuration list. The old CWAProductConfig
 * can be removed once all CWSU sites no longer use the old configure format.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/10/2021  28802    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "CWAProductConfigXML")
@XmlAccessorType(XmlAccessType.NONE)
public class CWAProductNewConfig {
    @XmlElements({
            @XmlElement(name = "cwaProducts", type = AbstractCWANewConfig.class) })
    private List<AbstractCWANewConfig> cwaProducts;

    public CWAProductNewConfig() {
    }

    public List<AbstractCWANewConfig> getCwaProducts() {
        return cwaProducts;
    }

    public void setCwaElementList(List<AbstractCWANewConfig> cwaProducts) {
        this.cwaProducts = cwaProducts;
    }

    public void addConfig(AbstractCWANewConfig config) {
        if (cwaProducts == null) {
            cwaProducts = new ArrayList<>();
        }
        cwaProducts.add(config);
    }

}
