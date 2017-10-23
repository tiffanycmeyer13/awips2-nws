/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.firewx.rsc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.bufrua.UAObs;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * Resource data for Firewx plot
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * FEB 07, 2017  18784    wkwock    Initial creation
 *
 * </pre>
 *
 * @author wkwock
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class FirewxResourceData extends AbstractRequestableResourceData {
    @XmlAttribute
    private String plotSource;

    @Override
    protected AbstractVizResource<?, ?> constructResource(
            LoadProperties loadProperties, PluginDataObject[] objects)
                    throws VizException {
        FirewxResource resource = new FirewxResource(this, loadProperties);
        if (objects instanceof UAObs[]) {
            resource.addRecords((UAObs[]) objects);
        }
        return resource;
    }

    /**
     * get plotSource
     * 
     * @return the plotSource
     */
    public String getPlotSource() {
        return plotSource;
    }

    /**
     * Set the plotSource
     * 
     * @param plotSource
     */
    public void setPlotSource(String plotSource) {
        this.plotSource = plotSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (obj instanceof FirewxResourceData) {
                FirewxResourceData other = (FirewxResourceData) obj;
                if (this.plotSource == null || other.plotSource == null) {
                    return false;
                } else {
                    return plotSource.equals(other.plotSource);
                }
            }
        }

        return false;
    }
}
