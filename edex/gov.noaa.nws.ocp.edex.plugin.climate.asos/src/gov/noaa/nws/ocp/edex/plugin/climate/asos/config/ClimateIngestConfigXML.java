package gov.noaa.nws.ocp.edex.plugin.climate.asos.config;

import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * ClimateIngestConfigXML mapping class for Climate Ingest Configuration
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 3, 2016  20905      pwang       Initial creation
 * Nov 3, 2017  36736      amoore      Get rid of unneeded method.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@XmlRootElement(name = "ClimateIngestConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateIngestConfigXML {

    @XmlElements({
            @XmlElement(name = "filter", type = ClimateIngestFilterXML.class) })
    private ArrayList<ClimateIngestFilterXML> filters;

    /**
     * Empty constructor.
     */
    public ClimateIngestConfigXML() {
    }

    public ArrayList<ClimateIngestFilterXML> getFilters() {
        return filters;
    }

    public void setFilters(ArrayList<ClimateIngestFilterXML> filters) {
        this.filters = filters;
    }
}
