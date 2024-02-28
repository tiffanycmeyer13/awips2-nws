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
 * class for save a CWA product text to XML file
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 06/12/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "CWAProductConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class ProductsConfig {
    @XmlElements({ @XmlElement(name = "cwaProducts", type = ProductConfig.class) })
    private List<ProductConfig> productsConfig;

    public List<ProductConfig> getProductsConfig() {
        return productsConfig;
    }

    public void setProductsConfig(List<ProductConfig> productsConfig) {
        this.productsConfig = productsConfig;
    }
}
