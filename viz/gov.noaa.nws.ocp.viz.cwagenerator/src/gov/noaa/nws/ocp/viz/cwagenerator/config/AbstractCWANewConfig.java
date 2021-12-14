/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;

/**
 * Abstract class for the CWA configuration. The old AbstractCWAConfig
 * can be removed once all CWSU sites no longer use the old configure format.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/03/2021  28802    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 */
@XmlRootElement(name = "CWAConfig")
@XmlSeeAlso({ BlduBlsaNewConfig.class, CancelManualNewConfig.class,
        IcingFrzaNewConfig.class, IfrLifrNewConfig.class,
        ThunderstormNewConfig.class, TurbLlwsNewConfig.class,
        VolcanoNewConfig.class, CWSNewConfig.class })
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractCWANewConfig {
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

    @XmlElement(name = "drawable")
    private AbstractDrawableComponent drawable;

    @XmlElement(name = "isRoutine")
    private boolean isRoutine;

    @XmlElement(name = "author")
    private String author;

    /** time created/save this configuration */
    @XmlElement(name = "time")
    private String time;

    protected AbstractCWANewConfig() {
    }

    protected AbstractCWANewConfig(AbstractCWAConfig oldConfig) {
        configName = oldConfig.getConfigName();
        startTimeChk = oldConfig.isStartTimeChk();
        startTime = oldConfig.getStartTime();
        endTime = oldConfig.getEndTime();
        productTxt = oldConfig.getProductTxt();
        isRoutine = oldConfig.isRoutine();
        author = oldConfig.getAuthor();
        time = oldConfig.getTime();
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

    public AbstractDrawableComponent getDrawable() {
        return drawable;
    }

    public void setDrawable(AbstractDrawableComponent drawable) {
        this.drawable = drawable;
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

    public static AbstractCWANewConfig getNewConfig(AbstractCWAConfig config) {
        if (config instanceof BlduBlsaConfig) {
            return new BlduBlsaNewConfig((BlduBlsaConfig) config);
        } else if (config instanceof CancelManualConfig) {
            return new CancelManualNewConfig((CancelManualConfig) config);
        } else if (config instanceof IcingFrzaConfig) {
            return new IcingFrzaNewConfig((IcingFrzaConfig) config);
        } else if (config instanceof IfrLifrConfig) {
            return new IfrLifrNewConfig((IfrLifrConfig) config);
        } else if (config instanceof ThunderstormConfig) {
            return new ThunderstormNewConfig((ThunderstormConfig) config);
        } else if (config instanceof TurbLlwsConfig) {
            return new TurbLlwsNewConfig((TurbLlwsConfig) config);
        } else if (config instanceof VolcanoConfig) {
            return new VolcanoNewConfig((VolcanoConfig) config);
        } else if (config instanceof CWSConfig) {
            return new CWSNewConfig((CWSConfig) config);
        }
        return null;
    }

}
