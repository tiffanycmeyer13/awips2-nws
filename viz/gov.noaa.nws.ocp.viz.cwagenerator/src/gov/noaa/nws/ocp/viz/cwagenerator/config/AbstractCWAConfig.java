/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import gov.noaa.nws.ncep.ui.pgen.file.Products;

/**
 * Abstract class for CWA configuration
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 04/12/2020  75767    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "CWAConfig")
@XmlSeeAlso({ BlduBlsaConfig.class, CancelManualConfig.class,
        IcingFrzaConfig.class, IfrLifrConfig.class, ThunderstormConfig.class,
        TurbLlwsConfig.class, VolcanoConfig.class, CWSConfig.class })
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractCWAConfig {
    @XmlElement(name = "configName")
    private String configName;

    /** start time check button */
    @XmlElement(name = "startTimeChk")
    private boolean startTimeChk;

    /** start time */
    @XmlElement(name = "startTime")
    private String startTime;

    /** duration */
    @XmlElement(name = "endTime")
    private String endTime;

    @XmlElement(name = "productTxt")
    private String productTxt;

    @XmlElement(name = "vorProduct")
    private Products vorProduct;

    @XmlElement(name = "isRoutine")
    private boolean isRoutine;

    @XmlElement(name = "author")
    private String author;

    @XmlElement(name = "time")
    private String time;

    public AbstractCWAConfig() {
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public boolean isStartTimeChk() {
        return startTimeChk;
    }

    public void setStartTimeChk(boolean startTimeChk) {
        this.startTimeChk = startTimeChk;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getProductTxt() {
        return productTxt;
    }

    public void setProductTxt(String productTxt) {
        this.productTxt = productTxt;
    }

    public Products getVorProduct() {
        return vorProduct;
    }

    public void setVorProduct(Products vorProduct) {
        this.vorProduct = vorProduct;
    }

    public boolean isRoutine() {
        return isRoutine;
    }

    public void setRoutine(boolean isRoutine) {
        this.isRoutine = isRoutine;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
