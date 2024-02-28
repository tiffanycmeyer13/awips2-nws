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
 * This class represent an objective aid technique entry in techlist.dat, which
 * is used for displaying the objective aid.
 *
 * <pre>
 *
 * The file format is as follows:
 *
 * NUM TECH ERRS RETIRED COLOR DEFAULTS INT-DEFS RADII-DEFS LONG-NAME
 *  00 CARQ   0      0     0      0        0         1                 Combined ARQ Position
 *  00 WRNG   0      0     0      0        0         1                 Warning
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
public class ObjectiveAidTechEntry {

    /**
     * Num
     */
    @DynamicSerializeElement
    @XmlAttribute
    private int num;

    /**
     * Name
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Errs
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean errs;

    /**
     * Retired
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean retired;

    /**
     * Errs
     */
    @DynamicSerializeElement
    @XmlAttribute
    private int color;

    /**
     * As default obj aid
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean aidDflt;

    /**
     * As default obj aid for intensity
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean intDflt;

    /**
     * As default obj aid for wind radii
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean radiiDflt;

    /**
     * Description
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String description;

    /**
     * Constructor.
     */
    public ObjectiveAidTechEntry() {
    }

    /**
     * Constructor.
     * 
     * @param num
     *            obj aid tech num
     * @param name
     *            obj aid tech name
     * @param errs
     *            if used for errs
     * @param retired
     *            if this obj aid tech retired or developmental
     * @param color
     *            color to display this obj aid tech
     * @param aidDflt
     *            if used as default obj aid tech
     * @param intDflt
     *            if used for intensity
     * @param radiiDflt
     *            if used for wind radii
     * @param description
     *            description of the tech
     */
    public ObjectiveAidTechEntry(int num, String name, boolean errs,
            boolean retired, int color, boolean aidDflt, boolean intDflt,
            boolean radiiDflt, String description) {

        this.num = num;
        this.name = name;
        this.errs = errs;
        this.retired = retired;
        this.color = color;
        this.aidDflt = aidDflt;
        this.intDflt = intDflt;
        this.radiiDflt = radiiDflt;
        this.description = description;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isErrs() {
        return errs;
    }

    public void setErrs(boolean errs) {
        this.errs = errs;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isAidDflt() {
        return aidDflt;
    }

    public void setAidDflt(boolean aidDflt) {
        this.aidDflt = aidDflt;
    }

    public boolean isIntDflt() {
        return intDflt;
    }

    public void setIntDflt(boolean intDflt) {
        this.intDflt = intDflt;
    }

    public boolean isRadiiDflt() {
        return radiiDflt;
    }

    public void setRadiiDflt(boolean radiiDflt) {
        this.radiiDflt = radiiDflt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}