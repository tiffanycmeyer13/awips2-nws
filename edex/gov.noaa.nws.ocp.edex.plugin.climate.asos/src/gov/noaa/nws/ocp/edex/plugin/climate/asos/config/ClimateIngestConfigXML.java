package gov.noaa.nws.ocp.edex.plugin.climate.asos.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ClimateIngestConfigXML mapping class for Climate Ingest Configuration
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 3, 2016  20905      pwang     Initial creation
 *
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

    public ArrayList<ClimateIngestFilterXML> getFilters() {
        return filters;
    }

    public void setFFMPRun(ArrayList<ClimateIngestFilterXML> filters) {
        this.filters = filters;
    }

    public void add(ClimateIngestFilterXML filter) {
        if (filters == null) {
            filters = new ArrayList<ClimateIngestFilterXML>();
        }
        filters.add(filter);
    }

}
