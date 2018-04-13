package gov.noaa.nws.ocp.edex.plugin.climate.asos.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * SourceIngestXML, a mapping class for hold a group of filter group: SITE /
 * STATION dataKey: Site / Station code
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

@XmlAccessorType(XmlAccessType.NONE)
public class SourceIngestXML {

    /* SITE / STATION */
    @XmlAttribute(name = "group")
    protected String sourceGroup;

    @XmlElements({ @XmlElement(name = "dataKey") })
    private ArrayList<String> dataKey;

    public void setDataKey(ArrayList<String> dataKey) {
        this.dataKey = dataKey;
    }

    public ArrayList<String> getDataKey() {
        return dataKey;
    }

    public void addDataKey(String datakey) {
        if (dataKey == null) {
            dataKey = new ArrayList<String>();
        }
        dataKey.add(datakey);
    }

    public void removeDataKey(String datakey) {
        dataKey.remove(datakey);
    }

    public String getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(String sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    /**
     * matchDataKey return true when a Site or a Station is configured If the
     * dataKey is null, or a dataKey = "ALL", always return true
     * 
     * @param key
     * @return
     */
    public boolean matchDataKey(String key) {
        if (this.dataKey == null || this.dataKey.isEmpty()) {
            // No filter defined means allowed all
            return true;
        }
        for (String dk : dataKey) {
            if (dk.equalsIgnoreCase("ALL") || dk.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

}
