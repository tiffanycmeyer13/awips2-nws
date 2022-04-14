/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.psh;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * List of storm names for a given basin/year.
 *
 * This class holds storm names as in a text file like "storm??.txt", where "??"
 * denotes the year. The file is under directory "PSH/[basin]/".
 *
 * <pre>
 *     2012
 *     ALBERTO
 *     BERYL
 *     CHRIS  
 *     DEBBY
 *     ERNESTO
 *     FLORENCE
 *     GORDON
 *     HELENE 
 *     ISAAC
 *     JOYCE
 *     KIRK
 *     LESLIE
 *     MICHAEL
 *     NADINE 
 *     OSCAR
 *     PATTY  
 *     RAFAEL
 *     SANDY
 *     TONY 
 *     VALERIE
 *
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 06 JUL 2017  #35269     jwu         Initial creation
 * 02 OCT 2017  #38429     astrakovsky Fixed error caused by constructor.
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHStormNames")
@XmlAccessorType(XmlAccessType.NONE)
public class PshStormNames {

    /**
     * Basin
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Basin")
    private PshBasin basin;

    /**
     * Year
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Year")
    private String year;

    /**
     * Storm names for the basin/year
     */
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Name", type = String.class) })
    private List<String> storms;

    /**
     * Constructor.
     */
    public PshStormNames() {
        storms = new ArrayList<>();
    }

    /**
     * Constructor.
     * 
     * @param basin
     *            storm's basin
     * @param year
     *            storm's year
     * @param storms
     *            list of storm names
     */
    public PshStormNames(PshBasin basin, String year, List<String> storms) {
        this.basin = basin;
        this.year = year;
        this.storms = storms;
    }

    /**
     * @return the basin
     */
    public PshBasin getBasin() {
        return basin;
    }

    /**
     * @param basin
     *            the basin to set
     */
    public void setBasin(PshBasin basin) {
        this.basin = basin;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the storms
     */
    public List<String> getStorms() {
        return storms;
    }

    /**
     * @param storms
     *            the storms to set
     */
    public void setStorms(List<String> storms) {
        this.storms = storms;
    }

    /**
     * @return a new object filled with default values.
     */
    public static PshStormNames getDefault() {
        PshStormNames storms = new PshStormNames();
        storms.setDataToDefault();
        return storms;
    }

    /**
     * Set data to default values.
     */
    public void setDataToDefault() {

        this.basin = PshBasin.AT;
        this.year = "";
        this.storms = new ArrayList<>();
    }

}