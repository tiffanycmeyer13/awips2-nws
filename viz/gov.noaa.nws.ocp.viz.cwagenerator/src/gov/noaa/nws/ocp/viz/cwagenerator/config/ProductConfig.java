/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Class for product XML
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 5, 2020  75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
@XmlRootElement(name = "ProductConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class ProductConfig {
    @XmlElement(name = "author")
    private String author;

    @XmlElement(name = "time")
    private Long time;

    @XmlElement(name = "weatherName")
    private String weatherName;

    @XmlElement(name = "productTxt")
    private String productTxt;

    public ProductConfig() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getWeatherName() {
        return weatherName;
    }

    public void setWeatherName(String weatherName) {
        this.weatherName = weatherName;
    }

    public String getProductTxt() {
        return productTxt;
    }

    public void setProductTxt(String productTxt) {
        this.productTxt = productTxt;
    }
}