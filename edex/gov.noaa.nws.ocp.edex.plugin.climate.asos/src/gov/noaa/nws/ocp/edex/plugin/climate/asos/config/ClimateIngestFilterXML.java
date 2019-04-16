package gov.noaa.nws.ocp.edex.plugin.climate.asos.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * ClimateIngestFilterXML, mapping class for Climate Ingest Filters
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 3, 2016  20905      pwang     Initial creation
 * 12 OCT 2018  20941      pwang     Removed "site level" ingest station filter to avoid confusion
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
public class ClimateIngestFilterXML {

    @XmlElements({
            @XmlElement(name = "sourceIngest", type = SourceIngestXML.class) })
    private ArrayList<SourceIngestXML> sourceFilterGroups;

    public ArrayList<SourceIngestXML> getSourceFilterGroups() {
        return sourceFilterGroups;
    }

    public void setSourceFilterGroups(
            ArrayList<SourceIngestXML> sourceFilterGroups) {
        this.sourceFilterGroups = sourceFilterGroups;
    }

    public void addSourceFilterGroup(SourceIngestXML source) {
        if (this.sourceFilterGroups == null) {
            this.sourceFilterGroups = new ArrayList<SourceIngestXML>();
        }

        this.sourceFilterGroups.add(source);
    }

    /**
     * getSourceFilterGroup return either SITE or STATION filter group
     * 
     * @param groupName
     *            ("SITE" / "STATION")
     * @return
     */
    public SourceIngestXML getSourceFilterGroup(String groupName) {
        if (sourceFilterGroups == null) {
            return null;
        }
        for (SourceIngestXML sf : sourceFilterGroups) {
            if (sf.getSourceGroup().equalsIgnoreCase(groupName)) {
                return sf;
            }
        }

        return null;
    }

    /**
     * ingestSourceStationAllowed return if the DSM/MSM data from given site is
     * allowed to ingest If the SITE filter is not configured, assume all SITEs
     * allowed Key is case insensitive
     * 
     * @param siteKey
     * @return
     */
    public boolean ingestSourceStationAllowed(String stationKey) {
        SourceIngestXML stg = getSourceFilterGroup("STATION");
        if (stg != null && !stg.getDataKey().isEmpty()) {
            return stg.matchDataKey(stationKey);
        }

        // No limit to station if not defined
        return true;
    }

}
